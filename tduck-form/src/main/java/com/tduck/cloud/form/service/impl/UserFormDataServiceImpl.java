package com.tduck.cloud.form.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.tduck.cloud.common.constant.CommonConstants;
import com.tduck.cloud.common.util.*;
import com.tduck.cloud.form.entity.UserFormDataEntity;
import com.tduck.cloud.form.entity.UserFormItemEntity;
import com.tduck.cloud.form.entity.enums.FormItemTypeEnum;
import com.tduck.cloud.form.entity.struct.UploadResultStruct;
import com.tduck.cloud.form.mapper.UserFormDataMapper;
import com.tduck.cloud.form.request.QueryFormResultRequest;
import com.tduck.cloud.form.service.UserFormDataService;
import com.tduck.cloud.form.service.UserFormItemService;
import com.tduck.cloud.form.util.FormAuthUtils;
import com.tduck.cloud.form.util.FormDataUtils;
import com.tduck.cloud.form.util.FormWebHookUtils;
import com.tduck.cloud.form.vo.FormDataTableVO;
import com.tduck.cloud.form.vo.FormFieldVO;
import com.tduck.cloud.storage.cloud.OssStorageFactory;
import com.tduck.cloud.storage.util.StorageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tduck.cloud.form.constant.FormRedisKeyConstants.FORM_RESULT_NUMBER;
import static com.tduck.cloud.webhook.constant.WebhookEventConstants.WEBHOOK_EVENT_TYPE_FORM_DATA_ADD;
import static com.tduck.cloud.webhook.constant.WebhookEventConstants.WEBHOOK_EVENT_TYPE_FORM_DATA_UPDATE;

/**
 * 表单表单项(FormResult)表服务实现类
 *
 * @author smalljop
 * @since 2020-11-23 14:09:22
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserFormDataServiceImpl extends ServiceImpl<UserFormDataMapper, UserFormDataEntity> implements UserFormDataService {

    private final UserFormItemService userFormItemService;
    private final CacheUtils redisUtils;
    private final FormDataUtils formDataUtils;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveFormResult(UserFormDataEntity entity) {
        HashMap<String, Object> result = MapUtil.newHashMap();
        String formKey = entity.getFormKey();
        entity.setSubmitAddress(AddressUtils.getRealAddressByIP(entity.getSubmitRequestIp()));
        entity.setSerialNumber(redisUtils.incr(StrUtil.format(FORM_RESULT_NUMBER, formKey), CommonConstants.ConstantNumber.ONE));
        this.save(entity);
        formDataUtils.syncSaveFormData(entity);
        result.put("id", entity.getId());
        FormWebHookUtils.pushFormDataSaveWebHook(entity, WEBHOOK_EVENT_TYPE_FORM_DATA_ADD);
        return result;
    }


    /**
     * 下载表单结果中的附件
     *
     * @param request
     * @return
     */
    @Override
    public Result downloadFormResultFile(QueryFormResultRequest request) {
        request.validateSqlInjection();
        String uuid = IdUtil.simpleUUID();
        List<UserFormItemEntity> userFormItemEntityList = userFormItemService.list(Wrappers.<UserFormItemEntity>lambdaQuery().eq(UserFormItemEntity::getFormKey, request.getFormKey()).in(UserFormItemEntity::getType, CollUtil.newArrayList(FormItemTypeEnum.UPLOAD.toString(), FormItemTypeEnum.IMAGE_UPLOAD.toString())));
        //结果
        List<Map> rows = null;
        if (ObjectUtil.isNull(request.getCurrent()) && ObjectUtil.isNull(request.getSize())) {
            rows = formDataUtils.searchAll(request);
        } else {
            FormDataTableVO formDataTableVO = this.listFormDataTable(request);
            rows = formDataTableVO.getRows();
        }
        if (CollectionUtil.isEmpty(rows) || CollectionUtil.isEmpty(userFormItemEntityList)) {
            return Result.failed("暂无收集附件，无法下载");
        }

        // 预检：选中的数据中是否存在已填写的有效附件
        boolean hasFiles = rows.stream().anyMatch(result ->
                userFormItemEntityList.stream().anyMatch(item -> {
                    Object val = MapUtil.get(result, item.getFormItemId(), Object.class);
                    if (val == null) {
                        return false;
                    }
                    try {
                        List<UploadResultStruct> uploadResults = JsonUtils.jsonToList(JsonUtils.objToJson(val), UploadResultStruct.class);
                        return CollectionUtil.isNotEmpty(uploadResults) && uploadResults.stream().anyMatch(u -> StrUtil.isNotBlank(u.getUrl()));
                    } catch (Exception e) {
                        return false;
                    }
                })
        );
        if (!hasFiles) {
            return Result.failed("选中的数据中未包含任何附件，无法下载");
        }

        List<Map> finalRows = rows;
        ThreadUtil.execAsync(() -> {
            TimeInterval timer = DateUtil.timer();
            List<String> paths = new ArrayList<>();
            List<InputStream> ins = new ArrayList<>();
            try {
                finalRows.forEach(result -> {
                    int index = 0;
                    userFormItemEntityList.forEach(item -> {
                        List<UploadResultStruct> uploadResults = JsonUtils.jsonToList(JsonUtils.objToJson(MapUtil.get(result, item.getFormItemId(), List.class)), UploadResultStruct.class);
                        if (CollectionUtil.isNotEmpty(uploadResults)) {
                            uploadResults.forEach(uFile -> {
                                String fileUrl = uFile.getUrl();
                                if (StrUtil.isNotBlank(fileUrl)) {
                                    // 1. 校验是否属于本系统存储域名/路径
                                    if (!OssStorageFactory.isAllowedStorageUrl(fileUrl)) {
                                        log.warn("检测到非本系统存储附件 URL，跳过下载: {}", fileUrl);
                                        return;
                                    }
                                    // 2. 安全读取/下载存储附件（本地存储优先读磁盘，远程存储走安全 HTTP）
                                    byte[] bytes = OssStorageFactory.downloadBytes(fileUrl);
                                    if (bytes != null && bytes.length > 0) {
                                        paths.add(FileNameUtil.getName(fileUrl));
                                        ins.add(IoUtil.toStream(bytes));
                                    }
                                }
                            });
                        }
                    });
                    AsyncProcessUtils.setProcess(uuid, ++index / finalRows.size());
                });

                if (CollectionUtil.isEmpty(paths) || CollectionUtil.isEmpty(ins)) {
                    log.warn("未获取到有效的附件文件内容，跳过压缩打包: uuid={}", uuid);
                    AsyncProcessUtils.setProcess(uuid, "EMPTY_FILES");
                    return;
                }

                // 压缩上传oss
                ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
                ZipUtil.zip(zipOutputStream, paths.toArray(new String[]{}), ins.toArray(new InputStream[]{}));
                String downloadUrl = OssStorageFactory.getStorageService().upload(zipOutputStream.toByteArray(), StorageUtils.generateFileName("download", ".zip"));
                AsyncProcessUtils.setProcess(uuid, downloadUrl);
                log.info("export file cost time: {}", timer.interval());
            } catch (Exception e) {
                log.error("download file error", e);
                AsyncProcessUtils.setProcess(uuid, "ERROR");
            }
        });
        return Result.success(uuid);
    }


    @Override
    public FormDataTableVO listFormDataTable(QueryFormResultRequest request) {
        return formDataUtils.search(request);
    }


    @Override
    public Boolean deleteByIds(List<String> dataIdList, String formKey) {
        baseMapper.deleteBatchIds(dataIdList);
        formDataUtils.asyncDeleteEsDocument(dataIdList, formKey);
        FormWebHookUtils.pushFormDataDeleteWebHook(dataIdList, formKey);
        return true;
    }

    @Override
    public Boolean updateFormResult(UserFormDataEntity entity) {
        UserFormDataEntity dataEntity = this.getById(entity.getId());
        dataEntity.setOriginalData(entity.getOriginalData());
        dataEntity.setUpdateBy(entity.getUpdateBy());
        boolean update = this.updateById(dataEntity);
        // 查询数据 同步到es 避免数据变空被覆盖
        formDataUtils.asyncUpdateEsDocument(dataEntity);
        FormWebHookUtils.pushFormDataSaveWebHook(entity, WEBHOOK_EVENT_TYPE_FORM_DATA_UPDATE);
        return update;
    }

    @Override
    public Result getFormDataDetails(String dataId) {
        Map<String, Object> result = Maps.newHashMap();
        UserFormDataEntity dataEntity = this.getById(dataId);
        if (ObjectUtil.isNull(dataEntity)) {
            return Result.failed("数据不存在");
        }
        // 权限校验
        FormAuthUtils.hasPermission(dataEntity.getFormKey());
        List<FormFieldVO> formFields = userFormItemService.listFormFields(dataEntity.getFormKey());
        // 表单字段
        result.put("formFields", formFields);
        // 表单填写数据
        Map<String, Object> originalData = dataEntity.getOriginalData();
        dataEntity.setOriginalData(null);
        originalData.putAll(BeanUtil.beanToMap(dataEntity, false, true));
        result.put("formData", originalData);
        return Result.success(result);
    }
}
