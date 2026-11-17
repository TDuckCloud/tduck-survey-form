package com.tduck.cloud.api.web.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tduck.cloud.common.util.SecurityUtils;
import com.tduck.cloud.common.entity.PageRequest;
import com.tduck.cloud.common.util.QueryWrapperUtils;
import com.tduck.cloud.common.util.Result;
import com.tduck.cloud.form.entity.FormTemplateCategoryEntity;
import com.tduck.cloud.form.entity.FormTemplateEntity;
import com.tduck.cloud.form.request.QueryFormTemplateRequest;
import com.tduck.cloud.form.request.QueryFormTemplateTypeRequest;
import com.tduck.cloud.form.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * 表单模板
 *
 * @author : smalljop
 * @description : 表单模板接口
 * @create :  2021/01/06 10:54
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
public class FormTemplateController {

    private final FormTemplateService formTemplateService;
    private final FormTemplateCategoryService formTemplateCategoryService;
    private final UserFormItemService userFormItemService;
    private final UserFormService userFormService;
    private final UserFormLogicService userFormLogicService;
    private final UserFormThemeService userFormThemeService;

    /**
     * 删除模板
     *
     * @param templateEntity
     * @return
     */
    @PostMapping("/form/template/delete")
    public Result deleteFormTemplate(@RequestBody FormTemplateEntity templateEntity) {
        Assert.isTrue(SecurityUtils.isAdmin(), "无权限访问");
        formTemplateService.remove(Wrappers.<FormTemplateEntity>lambdaQuery().eq(FormTemplateEntity::getFormKey, templateEntity.getFormKey()));
        return Result.success(templateEntity.getFormKey());
    }

    /**
     * 分页查询项目分类
     *
     * @return
     */
    @GetMapping("/form/template/type/list")
    public Result queryFormTemplateTypes(QueryFormTemplateTypeRequest.List request) {
        return Result.success(formTemplateCategoryService.list(Wrappers.<FormTemplateCategoryEntity>lambdaQuery()
                .orderByDesc(FormTemplateCategoryEntity::getSort)));
    }

    /**
     * 分页查询项目模板
     *
     * @return
     */
    @GetMapping("/form/template/page")
    public Result queryFormTemplates(QueryFormTemplateRequest.Page request) {
        return Result.success(
                formTemplateService.page(request.toMybatisPage(),
                        Wrappers.<FormTemplateEntity>lambdaQuery()
                                .eq(ObjectUtil.isNotNull(request.getType()), FormTemplateEntity::getCategoryId, request.getType())
                                .like(StrUtil.isNotBlank(request.getName()), FormTemplateEntity::getName, request.getName())
                                .orderByDesc(FormTemplateEntity::getCreateTime)));
    }


    /**
     * 表单另存为为模板
     *
     * @param userId 用户id
     * @return
     */
    @PostMapping("/form/template/create")
    public Result createFormTemplate(@RequestBody FormTemplateEntity formTemplateEntity, @RequestAttribute Long userId) {
        Assert.isTrue(SecurityUtils.isAdmin(), "仅超级管理员可以保存公开模板，普通用户只能保存个人模板");
        FormTemplateEntity formTemplate = formTemplateService.createFormTemplate(formTemplateEntity);
        return Result.success(formTemplate.getFormKey());
    }

    /**
     * 查询项目模板详情
     * 包含项目信息 项目保单项信息
     *
     * @param key 项目key
     * @return
     */
    @GetMapping("/form/template/details/{key}")
    public Result queryFormTemplateDetails(@PathVariable @NotBlank String key) {
        FormTemplateEntity templateEntity = formTemplateService.getByKey(key);
        return Result.success(templateEntity.getScheme());
    }


    /**
     * 查询项目模板分类列表
     */
    @GetMapping("/form/template/category/page")
    public Result queryCategoryPage(PageRequest pageRequest, FormTemplateCategoryEntity fmFormTemplateCategory) {
        return Result.success(formTemplateCategoryService.page(pageRequest.toMybatisPage(), QueryWrapperUtils.toSimpleQuery(fmFormTemplateCategory)));
    }

    /**
     * 获取项目模板分类详细信息
     */
    @GetMapping(value = "/form/template/category/{id}")
    public Result getCategoryInfo(@PathVariable("id") Long id) {
        return Result.success(formTemplateCategoryService.getById(id));
    }

    /**
     * 新增项目模板分类
     */
    @PostMapping("/form/template/category")
    public Result saveCategory(@RequestBody FormTemplateCategoryEntity fmFormTemplateCategory) {
        Assert.isTrue(SecurityUtils.isAdmin(), "无权限访问");
        return Result.success(formTemplateCategoryService.save(fmFormTemplateCategory));
    }

    /**
     * 修改项目模板分类
     */
    @PutMapping("/form/template/category")
    public Result updateCategory(@RequestBody FormTemplateCategoryEntity fmFormTemplateCategory) {
        Assert.isTrue(SecurityUtils.isAdmin(), "无权限访问");
        return Result.success(formTemplateCategoryService.updateById(fmFormTemplateCategory));
    }

    /**
     * 删除项目模板分类
     */
    @DeleteMapping("/form/template/category/{ids}")
    public Result deleteCategory(@PathVariable List<Long> ids) {
        Assert.isTrue(SecurityUtils.isAdmin(), "无权限访问");
        return Result.success(formTemplateCategoryService.removeByIds(ids));
    }

}
