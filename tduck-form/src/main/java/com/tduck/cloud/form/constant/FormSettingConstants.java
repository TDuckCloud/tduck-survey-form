package com.tduck.cloud.form.constant;

/**
 * @author : wangqing
 * @description : 表单设置项常量
 * @create :  2022/07/07 11:49
 **/
public interface FormSettingConstants {

    /**
     * 表单提交后显示类型字段名称
     */
    String SUBMIT_SHOW_TYPE_KEY = "submitShowType";

    /**
     * 自定义提示页面内容
     */
    String SUBMIT_SHOW_CUSTOM_PAGE_KEY = "submitShowCustomPage";

    /**
     * 密码输入错误提示
     */
    String PWD_INPUT_ERROR_MSG = "密码输入错误";

    /**
     * 密码保护提示
     */
    String PWD_PROTECTED_MSG = "表单已开启密码保护，请输入密码";

    /**
     * 表单填写密码请求头
     */
    String HEADER_FORM_WRITE_PWD = "Form-Write-Pwd";
}
