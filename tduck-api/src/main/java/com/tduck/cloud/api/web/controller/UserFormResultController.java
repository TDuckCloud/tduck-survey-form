package com.tduck.cloud.api.web.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tduck.cloud.api.util.HttpUtils;
import com.tduck.cloud.common.constant.CommonConstants;
import com.tduck.cloud.common.constant.ResponseCodeConstants;
import com.tduck.cloud.common.email.MailService;
import com.tduck.cloud.common.util.CacheUtils;
import com.tduck.cloud.common.util.Result;
import com.tduck.cloud.common.util.SecurityUtils;
import com.tduck.cloud.common.validator.ValidatorUtils;
import com.tduck.cloud.form.constant.FormSettingConstants;
import com.tduck.cloud.form.entity.UserFormDataEntity;
import com.tduck.cloud.form.entity.UserFormEntity;
import com.tduck.cloud.form.entity.UserFormViewCountEntity;
import com.tduck.cloud.form.entity.struct.FormSettingSchemaStruct;
import com.tduck.cloud.form.request.ExportRequest;
import com.tduck.cloud.form.request.QueryFormResultRequest;
import com.tduck.cloud.form.service.UserFormDataService;
import com.tduck.cloud.form.service.UserFormService;
import com.tduck.cloud.form.service.UserFormSettingService;
import com.tduck.cloud.form.service.UserFormViewCountService;
import com.tduck.cloud.form.util.FormAuthUtils;
import com.tduck.cloud.form.util.FormDataExportUtils;
import com.tduck.cloud.form.util.FormDataImportUtils;
import com.tduck.cloud.wx.mp.service.WxMpUserMsgService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 表单数据
 *
 * @author : smalljop
 * @description : 表单数据页 当前用户自己使用接口
 * @create : 2020-11-18 18:17
 **/

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/form/data")
public class UserFormResultController {
    private final UserFormDataService formResultService;
    private final UserFormSettingService userFormSettingService;
    private final FormDataImportUtils formDataImportUtils;
    private final FormDataExportUtils formDataExportUtils;
    private final UserFormService userFormService;
    private final UserFormViewCountService userFormViewCountService;
    private final WxMpUserMsgService userMsgService;
    private final ConcurrentMap<String, Integer> viewFormMap = new ConcurrentHashMap<>();

    /***
     * 查看表单
     * 记录查看的IP 统计查看用户数
     */
    @GetMapping("view/{formKey}")
    @PermitAll
    public Result<Void> viewForm(HttpServletRequest request, @PathVariable("formKey") String formKey) {
        if (viewFormMap.containsKey(formKey)) {
            userFormViewCountService.increment(formKey);
        } else {
            // 不存在则添加
            Long count = userFormViewCountService.count(formKey);
            if (count == 0) {
                UserFormViewCountEntity entity = new UserFormViewCountEntity();
                entity.setFormKey(formKey);
                entity.setCount(1L);
                userFormViewCountService.save(entity);
            }
            viewFormMap.put(formKey, 1);
        }
        return Result.success();
    }

    /**
     * 查询数据
     *
     * @param request 查询条件
     * @return 数据
     */
    @PostMapping("query")
    public Result queryFormDataTable(@RequestBody QueryFormResultRequest request) {
        FormAuthUtils.hasPermission(request.getFormKey());
        return Result.success(formResultService.listFormDataTable(request));
    }

    /**
     * 获取某条数据详情
     *
     * @param dataId 数据ID
     * @return 数据详情
     */
    @GetMapping("details/{dataId}")
    public Result getFormDataDetails(@PathVariable("dataId") String dataId) {
        return formResultService.getFormDataDetails(dataId);
    }


    /**
     * 填写附件导出
     *
     * @param request 请求
     * @return 文件
     */
    @PostMapping("/download/file")
    public Result downloadFormResultFile(@RequestBody QueryFormResultRequest request) {
        FormAuthUtils.hasPermission(request.getFormKey());
        return formResultService.downloadFormResultFile(request);
    }


    /**
     * 填写
     *
     * @param entity  填写数据
     * @param request 请求
     * @return
     */
    @PostMapping("/create")
    public Result createFormResult(@RequestBody UserFormDataEntity entity, HttpServletRequest request) {
        ValidatorUtils.validateEntity(entity);
        entity.setSubmitRequestIp(HttpUtils.getIpAddr(request));
        // 如果已经登陆了也记录用户信息 try catch 避免抛出异常
        entity.setCreateBy(SecurityUtils.getUserId() != null ? String.valueOf(SecurityUtils.getUserId()) : null);
        Map<String, Object> result = formResultService.saveFormResult(entity);
        return Result.success(result);
    }


    /**
     * 公开填写
     *
     * @param entity  填写数据
     * @param request 请求
     */
    @PostMapping("/public/create")
    @PermitAll
    public Result<Map<String, Object>> createPublicFormResult(@RequestBody UserFormDataEntity entity, HttpServletRequest request) {
        ValidatorUtils.validateEntity(entity);
        entity.setSubmitRequestIp(HttpUtils.getIpAddr(request));
        // 校验表单填写密码
        String writePassword = StrUtil.isNotBlank(entity.getWritePassword()) ? entity.getWritePassword() : request.getHeader(FormSettingConstants.HEADER_FORM_WRITE_PWD);
        Result<Boolean> pwdCheckResult = userFormSettingService.checkFormWritePwd(entity.getFormKey(), writePassword);
        if (ObjectUtil.isNotNull(pwdCheckResult) && (pwdCheckResult.getCode() != ResponseCodeConstants.SUCCESS || StrUtil.isNotBlank(pwdCheckResult.getMsg()))) {
            return Result.failed(pwdCheckResult.getMsg());
        }
        Result<Boolean> userFormSettingStatus = userFormSettingService.getUserFormWriteSettingStatus(entity.getFormKey(), entity.getSubmitRequestIp(), entity.getWxOpenId(), CommonConstants.ConstantNumber.ONE);
        if (StrUtil.isNotBlank(userFormSettingStatus.getMsg())) {
            return Result.failed(userFormSettingStatus.getMsg());
        }
        // 如果已经登陆了也记录用户信息 try catch 避免抛出异常
        try {
            entity.setCreateBy(SecurityUtils.getUserId() != null ? String.valueOf(SecurityUtils.getUserId()) : null);
        } catch (Exception ignored) {
        }

        Map<String, Object> result = formResultService.saveFormResult(entity);
        ThreadUtil.execAsync(() -> {
            sendWriteResultNotify(entity.getFormKey());
        });
        return Result.success(result);
    }


    /**
     * 批量删除
     *
     * @param formKey    表单key
     * @param dataIdList 数据ID
     * @return Result
     */
    @PostMapping("/delete/{formKey}")
    public Result deleteFormData(@RequestBody List<String> dataIdList, @PathVariable("formKey") String formKey) {
        FormAuthUtils.hasPermission(formKey);
        formResultService.deleteByIds(dataIdList, formKey);
        return Result.success();
    }


    /**
     * 更新
     *
     * @param entity  填写数据
     * @param request 请求
     * @return Result
     */
    @PostMapping("/update")
    public Result<Void> updateFormResult(@RequestBody UserFormDataEntity entity, HttpServletRequest request) {
        ValidatorUtils.validateEntity(entity);
        try {
            entity.setUpdateBy(String.valueOf(SecurityUtils.getUserId()));
        } catch (Exception ignored) {
        }
        formResultService.updateFormResult(entity);
        return Result.success();
    }

    /**
     * 下载导入模板
     *
     * @param response 响应
     * @param formKey  表单key
     */
    @GetMapping("/import/template")
    public void downloadImportTemplate(HttpServletResponse response, String formKey) {
        FormAuthUtils.hasPermission(formKey);
        formDataImportUtils.importTemplateExcel(response, formKey);
    }

    /**
     * 导出表单数据
     */
    @PostMapping("export")
    public void exportFormData(@RequestBody ExportRequest.FormData exportRequest) {
        FormAuthUtils.hasPermission(exportRequest.getFormKey());
        formDataExportUtils.exportData(exportRequest);
    }

    /**
     * 导入表单数据
     *
     * @param file 文件
     * @return Result
     */
    @PostMapping("import")
    public Result importFormData(@RequestParam("file") MultipartFile file, UserFormDataEntity dataEntity) throws IOException {
        FormAuthUtils.hasPermission(dataEntity.getFormKey());
        return Result.success(formDataImportUtils.importFile(file.getInputStream(), dataEntity.getFormKey()));
    }


    @SneakyThrows
    private void sendWriteResultNotify(String formKey) {
        FormSettingSchemaStruct formSettingSchema = userFormSettingService.getFormSettingSchema(formKey);
        if (ObjectUtil.isNull(formSettingSchema)) {
            return;
        }
        UserFormEntity form = userFormService.getByKey(formKey);
        if (StrUtil.isNotBlank(formSettingSchema.getNewWriteNotifyEmail())) {
            MailService.sendTemplateHtmlMail(formSettingSchema.getNewWriteNotifyEmail(), "新回复通知", "mail/form-write-notify", MapUtil.of("projectName", form.getName()));
        }

        if (StrUtil.isNotBlank(formSettingSchema.getNewWriteNotifyWx())) {
            List<String> openIdList = StrUtil.splitTrim(formSettingSchema.getNewWriteNotifyWx(), ";");
            openIdList.stream().forEach(openId -> {
                userMsgService.sendKfTextMsg("", openId, "收到新的反馈，请去Pc端查看");
            });
        }
    }

}
