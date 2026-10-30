package com.tduck.cloud.form.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tduck.cloud.common.constant.CommonConstants;
import com.tduck.cloud.common.util.Result;
import com.tduck.cloud.common.util.SecurityUtils;
import com.tduck.cloud.form.constant.FormSettingConstants;
import com.tduck.cloud.form.entity.UserFormDataEntity;
import com.tduck.cloud.form.entity.UserFormEntity;
import com.tduck.cloud.form.entity.UserFormSettingEntity;
import com.tduck.cloud.form.entity.enums.FormStatusEnum;
import com.tduck.cloud.form.entity.struct.FormSettingSchemaStruct;
import com.tduck.cloud.form.mapper.UserFormSettingMapper;
import com.tduck.cloud.form.service.UserFormDataService;
import com.tduck.cloud.form.service.UserFormService;
import com.tduck.cloud.form.service.UserFormSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 表单表单项(UserFormSetting)表服务实现类
 *
 * @author smalljop
 * @since 2020-11-30 14:00:53
 */
@Service
@RequiredArgsConstructor
public class UserFormSettingServiceImpl extends ServiceImpl<UserFormSettingMapper, UserFormSettingEntity> implements UserFormSettingService {
    private final UserFormService userFormService;
    private final UserFormDataService userFormDataService;

    @Override
    public Boolean saveFormSetting(Map<String, Object> params) {
        String formKey = params.get("formKey").toString();
        UserFormSettingEntity entity = this.getOne(Wrappers.<UserFormSettingEntity>lambdaQuery().eq(UserFormSettingEntity::getFormKey, formKey));
        if (ObjectUtil.isNull(entity)) {
            UserFormSettingEntity setting = new UserFormSettingEntity();
            setting.setFormKey(formKey);
            setting.setSettings(params);
            return this.save(setting);
        }
        Map<String, Object> settings = entity.getSettings();
        settings.putAll(params);
        entity.setSettings(settings);
        return this.updateById(entity);
    }


    @Override
    public UserFormSettingEntity getFormSettingByKey(String formKey) {
        return this.getOne(Wrappers.<UserFormSettingEntity>lambdaQuery().eq(UserFormSettingEntity::getFormKey, formKey));
    }

    @Override
    public FormSettingSchemaStruct getFormSettingSchema(String formKey) {
        UserFormSettingEntity settingEntity = getFormSettingByKey(formKey);
        if (ObjectUtil.isNull(settingEntity)) {
            return null;
        }
        return BeanUtil.toBean(settingEntity.getSettings(), FormSettingSchemaStruct.class);
    }


    @Override
    public Result<Boolean> getUserFormWriteSettingStatus(String formKey, String requestIp, String wxOpenId, Integer type) {
        UserFormEntity userFormEntity = userFormService.getByKey(formKey);
        boolean checkPublish = Objects.equals(type, CommonConstants.ConstantNumber.ONE) &&
                (ObjectUtil.isNull(userFormEntity) || userFormEntity.getStatus() != FormStatusEnum.RELEASE);
        // 非公开填写 不校验发布状态
        if (checkPublish) {
            return Result.success(null, "表单暂时无法填写");
        }
        UserFormSettingEntity settingEntity = getFormSettingByKey(formKey);
        if (ObjectUtil.isNull(settingEntity)) {
            return Result.success(true);
        }
        FormSettingSchemaStruct settingSchemaStruct = BeanUtil.toBean(settingEntity.getSettings(), FormSettingSchemaStruct.class);
        // 填写时间限制
        boolean writeInterviewTime = isWriteInterviewTime(settingSchemaStruct);
        if (!writeInterviewTime) {
            return Result.success(null, StrUtil.blankToDefault(settingSchemaStruct.getWriteInterviewTimeText(), "不在答题时间范围内，有问题请与表单发布者联系"));
        }
        // 每个微信答题次数限制
        if (settingSchemaStruct.isWxWriteCountLimitStatus()) {
            LambdaQueryWrapper<UserFormDataEntity> wrapper = Wrappers.<UserFormDataEntity>lambdaQuery().eq(UserFormDataEntity::getFormKey, formKey).eq(UserFormDataEntity::getWxOpenId, wxOpenId);
            String rangeTypeSql = FormSettingSchemaStruct.DateRangeType.getDateSql(settingSchemaStruct.getWxWriteCountLimitDateType());
            wrapper.apply(StrUtil.isNotBlank(rangeTypeSql), rangeTypeSql);
            long writeCount = userFormDataService.count(wrapper);
            if (writeCount >= settingSchemaStruct.getWxWriteCountLimit()) {
                return Result.success(null, StrUtil.blankToDefault(settingSchemaStruct.getWxWriteCountLimitText(), "该微信已经提交过数据，不可重复提交，有问题请与表单发布者联系"));
            }
        }
        // 每个IP答题次数限制
        if (settingSchemaStruct.isIpWriteCountLimitStatus()) {
            LambdaQueryWrapper<UserFormDataEntity> wrapper = Wrappers.<UserFormDataEntity>lambdaQuery().eq(UserFormDataEntity::getFormKey, formKey).eq(UserFormDataEntity::getSubmitRequestIp, requestIp);
            String rangeTypeSql = FormSettingSchemaStruct.DateRangeType.getDateSql(settingSchemaStruct.getIpWriteCountLimitDateType());
            wrapper.apply(StrUtil.isNotBlank(rangeTypeSql), rangeTypeSql);
            long writeCount = userFormDataService.count(wrapper);
            if (writeCount >= settingSchemaStruct.getIpWriteCountLimit()) {
                return Result.success(null, StrUtil.blankToDefault(settingSchemaStruct.getIpWriteCountLimitText(), "该IP已经提交过数据，不可重复提交，有问题请与表单发布者联系"));
            }
        }
        // 总答题次数限制
        if (settingSchemaStruct.isTotalWriteCountLimitStatus()) {
            LambdaQueryWrapper<UserFormDataEntity> wrapper = Wrappers.<UserFormDataEntity>lambdaQuery().eq(UserFormDataEntity::getFormKey, formKey);
            String rangeTypeSql = FormSettingSchemaStruct.DateRangeType.getDateSql(settingSchemaStruct.getTotalWriteCountLimitDateType());
            wrapper.apply(StrUtil.isNotBlank(rangeTypeSql), rangeTypeSql);
            long writeCount = userFormDataService.count(wrapper);
            if (writeCount >= settingSchemaStruct.getTotalWriteCountLimit()) {
                return Result.success(null, StrUtil.blankToDefault(settingSchemaStruct.getTotalWriteCountLimitText(), "该表单收集数据已经达到上限，有问题请与表单发布者联系"));
            }
        }
        // 每个账号答题次数限制
        if (settingSchemaStruct.isAccountWriteCountLimitStatus()) {
            LambdaQueryWrapper<UserFormDataEntity> wrapper = Wrappers.<UserFormDataEntity>lambdaQuery().eq(UserFormDataEntity::getFormKey, formKey)
                    .eq(UserFormDataEntity::getCreateBy, SecurityUtils.getUserId());
            String rangeTypeSql = FormSettingSchemaStruct.DateRangeType.getDateSql(settingSchemaStruct.getAccountWriteCountLimitDateType());
            wrapper.apply(StrUtil.isNotBlank(rangeTypeSql), rangeTypeSql);
            long writeCount = userFormDataService.count(wrapper);
            if (writeCount >= settingSchemaStruct.getAccountWriteCountLimit()) {
                return Result.success(null, StrUtil.blankToDefault(settingSchemaStruct.getAccountWriteCountLimitText(), "该账号已经提交过数据，不可重复提交，有问题请与表单发布者联系"));
            }
        }
        return Result.success(true);
    }

    /**
     * 是否在设置的答题时间内
     *
     * @return true 在答题时间内
     */
    private boolean isWriteInterviewTime(FormSettingSchemaStruct settingSchemaStruct) {
        // 答题时间限制
        if (settingSchemaStruct.isWriteInterviewTimeStatus()) {
            // 是否每天时间范围限制
            if (settingSchemaStruct.isWriteInterviewDayTimeStatus()) {
                // 是否在允许访问的天内
                List<String> writeInterviewDateRange = settingSchemaStruct.getWriteInterviewDateRange();
                if (CollUtil.isEmpty(writeInterviewDateRange) || DateUtil.isIn(DateUtil.date(), DateUtil.parse(writeInterviewDateRange.get(0)), DateUtil.parse(writeInterviewDateRange.get(1)))) {
                    // 是否在允许访问的小时内
                    List<String> writeInterviewTimeRange = settingSchemaStruct.getWriteInterviewTimeRange();
                    LocalTime now = LocalTime.now();
                    boolean isRange = CollUtil.isNotEmpty(writeInterviewDateRange) && now.isBefore(LocalTime.parse(writeInterviewTimeRange.get(0))) || now.isAfter(LocalTime.parse(writeInterviewTimeRange.get(1)));
                    if (isRange) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                // 是否在允许访问的天内
                List<String> writeInterviewDateTimeRange = settingSchemaStruct.getWriteInterviewDateTimeRange();
                if (CollUtil.isNotEmpty(writeInterviewDateTimeRange) && !DateUtil.isIn(DateUtil.date(), DateUtil.parse(writeInterviewDateTimeRange.get(0)), DateUtil.parse(writeInterviewDateTimeRange.get(1)))) {
                    return false;
                }
            }
            // 是否是每周允许访问的周几
            List<String> writeInterviewTimeWhichDays = settingSchemaStruct.getWriteInterviewTimeWhichDays();
            if (CollUtil.isNotEmpty(writeInterviewTimeWhichDays)) {
                // 获取今天是每周的第几天
                int day = DateUtil.dayOfWeek(DateUtil.date());
                return writeInterviewTimeWhichDays.contains(String.valueOf(day));
            }
        }
        return true;
    }


    /**
     * 删除全部的表单数据
     *
     * @param formKey 表单key
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Boolean deleteAllSetting(String formKey) {
        this.remove(Wrappers.<UserFormSettingEntity>lambdaQuery().eq(UserFormSettingEntity::getFormKey, formKey));
        return true;
    }

    @Override
    public Result<Boolean> checkFormWritePwd(String formKey, String writePassword) {
        FormSettingSchemaStruct formSettingSchema = getFormSettingSchema(formKey);
        // 如果未配置设置或未开启密码填写，则无需密码，直接通过
        if (ObjectUtil.isNull(formSettingSchema) || !formSettingSchema.isPasswordWriteStatus()) {
            return Result.success(true);
        }
        // 若开启了密码填写但未设置密码，也视为无需密码校验
        if (StrUtil.isBlank(formSettingSchema.getWritePassword())) {
            return Result.success(true);
        }
        // 开启了密码保护，校验客户端提供的密码
        if (StrUtil.isBlank(writePassword)) {
            return Result.failed(FormSettingConstants.PWD_PROTECTED_MSG);
        }
        if (formSettingSchema.getWritePassword().equals(writePassword)) {
            return Result.success(true);
        }
        return Result.failed(FormSettingConstants.PWD_INPUT_ERROR_MSG);
    }

}
