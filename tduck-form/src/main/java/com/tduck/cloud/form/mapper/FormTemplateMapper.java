package com.tduck.cloud.form.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tduck.cloud.form.entity.FormTemplateEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 表单模板(FormTemplate)表数据库访问层
 *
 * @author smalljop
 * @since 2021-01-06 10:43:00
 */
public interface FormTemplateMapper extends BaseMapper<FormTemplateEntity> {

    /**
     * 联表分页查询模板列表（包含分类名称）
     *
     * @param page    分页对象
     * @param wrapper 条件构造器
     * @return 分页结果
     */
    @Select("SELECT t.*, c.name AS category_name " +
            "FROM fm_form_template t " +
            "LEFT JOIN fm_form_template_category c ON t.category_id = c.id " +
            "${ew.customSqlSegment}")
    Page<FormTemplateEntity> selectPageWithCategory(Page<FormTemplateEntity> page, @Param(Constants.WRAPPER) Wrapper<FormTemplateEntity> wrapper);

}