package com.tduck.cloud.form.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tduck.cloud.form.entity.FormTemplateEntity;
import com.tduck.cloud.form.entity.UserFormEntity;
import com.tduck.cloud.form.request.QueryFormTemplateRequest;

/**
 * 表单表(FormTemplate)表服务接口
 *
 * @author smalljop
 * @since 2021-01-06 10:43:01
 */
public interface FormTemplateService extends IService<FormTemplateEntity> {


    /**
     * 分页查询项目模板（包含分类名称）
     *
     * @param request 查询参数
     * @return 模板分页列表
     */
    Page<FormTemplateEntity> queryPage(QueryFormTemplateRequest.Page request);

    /**
     * 根據key獲取
     *
     * @param key 唯一标识
     * @return 模板
     */
    FormTemplateEntity getByKey(String key);


    /**
     * 创建模板
     *
     * @param formTemplate 模板
     * @return 模板
     */
    FormTemplateEntity createFormTemplate(FormTemplateEntity formTemplate);

    /**
     * 根据模板创建表单
     *
     * @param formTemplate 模板
     * @return 表单
     */
    UserFormEntity createFormByTemplate(FormTemplateEntity formTemplate,Long userId);

}