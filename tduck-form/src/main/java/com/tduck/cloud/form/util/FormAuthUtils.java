package com.tduck.cloud.form.util;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tduck.cloud.common.exception.BaseException;
import com.tduck.cloud.common.util.SecurityUtils;
import com.tduck.cloud.common.util.SpringContextUtils;
import com.tduck.cloud.form.entity.UserFormEntity;
import com.tduck.cloud.form.service.UserFormService;
import lombok.experimental.UtilityClass;


/**
 * 表单权限工具类
 *
 * @author tduck
 */
@UtilityClass
public class FormAuthUtils {

    /**
     * 无表单权限提示信息
     */
    public static final String NO_FORM_PERMISSION_MSG = "无表单权限";

    /**
     * 是否拥有表单的权限
     */
    public void hasPermission(String formKey) {
        Long currentUserId = SecurityUtils.getUserId();
        // 是否是超级管理员
        if (SecurityUtils.isAdmin(currentUserId)) {
            return;
        }
        if (StrUtil.isBlank(formKey) || ObjectUtil.isNull(currentUserId)) {
            throw new BaseException(NO_FORM_PERMISSION_MSG);
        }
        UserFormService userFormService = SpringContextUtils.getBean(UserFormService.class);
        UserFormEntity userFormEntity = userFormService.getByKey(formKey);
        if (ObjectUtil.isNull(userFormEntity)) {
            throw new BaseException(NO_FORM_PERMISSION_MSG);
        }
        // 是否是所有者
        if (ObjectUtil.notEqual(userFormEntity.getUserId(), currentUserId)) {
            throw new BaseException(NO_FORM_PERMISSION_MSG);
        }
    }


}
