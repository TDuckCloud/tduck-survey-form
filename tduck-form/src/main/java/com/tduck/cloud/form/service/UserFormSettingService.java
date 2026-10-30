package com.tduck.cloud.form.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tduck.cloud.common.util.Result;
import com.tduck.cloud.form.entity.UserFormSettingEntity;
import com.tduck.cloud.form.entity.struct.FormSettingSchemaStruct;

import java.util.Map;

/**
 * 表单设置
 *
 * @author smalljop
 * @since 2020-11-30 14:00:52
 */
public interface UserFormSettingService extends IService<UserFormSettingEntity> {
    /**
     * 保存表单设置
     */
    Boolean saveFormSetting(Map<String, Object> params);


    /**
     * 表单设置
     *
     * @param formKey 表单key
     * @return 设置项
     */
    UserFormSettingEntity getFormSettingByKey(String formKey);

    /**
     * 设置具体定义设置项
     *
     * @param formKey 表单key
     * @return 设置项
     */
    FormSettingSchemaStruct getFormSettingSchema(String formKey);

    /**
     * 获取当前项目设置的状态
     * 是否可以填写等
     *
     * @param formKey   表单key
     * @param requestIp 请求ip
     * @param wxOpenId  微信openid
     * @param type      类型 1公开填写 2.指定填写
     * @return 是否可以填写
     */
    Result<Boolean> getUserFormWriteSettingStatus(String formKey, String requestIp, String wxOpenId, Integer type);


    /**
     * 校验表单填写密码
     *
     * @param formKey       表单key
     * @param writePassword 用户输入的填写密码
     * @return 校验结果
     */
    Result<Boolean> checkFormWritePwd(String formKey, String writePassword);


    /**
     * 删除表单所有设置
     *
     * @param key 表单key
     * @return 是否删除成功
     */
    Boolean deleteAllSetting(String key);
}