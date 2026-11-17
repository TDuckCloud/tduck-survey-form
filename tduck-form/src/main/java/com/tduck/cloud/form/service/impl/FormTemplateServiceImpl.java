package com.tduck.cloud.form.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tduck.cloud.form.entity.*;
import com.tduck.cloud.form.entity.enums.FormSourceTypeEnum;
import com.tduck.cloud.form.entity.enums.FormStatusEnum;
import com.tduck.cloud.form.entity.enums.FormTypeEnum;
import com.tduck.cloud.form.mapper.FormTemplateMapper;
import com.tduck.cloud.form.request.QueryFormTemplateRequest;
import com.tduck.cloud.form.service.*;
import com.tduck.cloud.form.util.ShortIdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 表单表(FormTemplate)表服务实现类
 *
 * @author smalljop
 * @since 2021-01-06 10:43:02
 */
@Service
@RequiredArgsConstructor
public class FormTemplateServiceImpl extends ServiceImpl<FormTemplateMapper, FormTemplateEntity> implements FormTemplateService {
    private final UserFormItemService userFormItemService;
    private final UserFormService userFormService;
    private final UserFormThemeService userFormThemeService;
    private final UserFormLogicService userFormLogicService;

    @Override
    public Page<FormTemplateEntity> queryPage(QueryFormTemplateRequest.Page request) {
        QueryWrapper<FormTemplateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(ObjectUtil.isNotNull(request.getType()), "t.category_id", request.getType())
                .like(StrUtil.isNotBlank(request.getName()), "t.name", request.getName())
                .orderByDesc("t.create_time");
        return this.baseMapper.selectPageWithCategory(request.toMybatisPage(), wrapper);
    }

    @Override
    public FormTemplateEntity getByKey(String key) {
        return this.getOne(Wrappers.<FormTemplateEntity>lambdaQuery().eq(FormTemplateEntity::getFormKey, key));
    }

    @Override
    @Transactional
    public FormTemplateEntity createFormTemplate(FormTemplateEntity formTemplateEntity) {
        //保存基础信息和问题
        String formKey = formTemplateEntity.getFormKey();
        formTemplateEntity.setFormKey(ShortIdUtils.genId());
        // 查询表单
        UserFormEntity userFormEntity = userFormService.getByKey(formKey);
        List<UserFormItemEntity> itemEntityList = userFormItemService.listByFormKey(formKey);
        // 主题
        UserFormThemeEntity themeEntity = userFormThemeService.getByKey(formKey);
        FormTemplateEntity.Definition definition = new FormTemplateEntity.Definition();
        definition.setFormType(userFormEntity.getType().getValue());
        definition.setFormItems(itemEntityList);
        definition.setUserFormTheme(themeEntity);
        definition.setUserFormLogic(userFormLogicService.getOne(Wrappers.<UserFormLogicEntity>lambdaQuery().eq(UserFormLogicEntity::getFormKey, formKey)));
        formTemplateEntity.setScheme(definition);
        this.save(formTemplateEntity);
        return formTemplateEntity;
    }

    @Override
    public UserFormEntity createFormByTemplate(FormTemplateEntity request, Long userId) {
        String templateKey = request.getFormKey();
        FormTemplateEntity formTemplateEntity = this.getByKey(templateKey);
        String newFormKey = ShortIdUtils.genId();
        UserFormEntity userFormEntity = new UserFormEntity();
        BeanUtil.copyProperties(formTemplateEntity, userFormEntity, UserFormEntity.Fields.status);
        userFormEntity.setId(null);
        userFormEntity.setSourceType(FormSourceTypeEnum.TEMPLATE);
        userFormEntity.setSourceId(formTemplateEntity.getId().toString());
        userFormEntity.setFormKey(newFormKey);
        userFormEntity.setUserId(userId);
        userFormEntity.setStatus(FormStatusEnum.CREATE);
        if (ObjectUtil.isNotNull(formTemplateEntity.getScheme().getFormType())) {
            userFormEntity.setType(EnumUtil.getBy(FormTypeEnum::getValue, formTemplateEntity.getScheme().getFormType()));
        }
        userFormService.save(userFormEntity);
        List<UserFormItemEntity> formItems = formTemplateEntity.getScheme().getFormItems();
        formItems.forEach(item -> {
            item.setId(null);
            item.setFormKey(userFormEntity.getFormKey());
        });
        userFormItemService.saveBatch(formItems);
        // 主题
        if (ObjectUtil.isNotNull(formTemplateEntity.getScheme().getUserFormTheme())) {
            UserFormThemeEntity userFormTheme = formTemplateEntity.getScheme().getUserFormTheme();
            userFormTheme.setId(null);
            userFormTheme.setFormKey(newFormKey);
            userFormThemeService.save(userFormTheme);
        }
        // 逻辑
        UserFormLogicEntity userFormLogic = formTemplateEntity.getScheme().getUserFormLogic();
        if(ObjectUtil.isNotNull(userFormLogic)){
            userFormLogic.setId(null);
            userFormLogic.setFormKey(newFormKey);
            userFormLogicService.save(userFormLogic);
        }
        return userFormEntity;
    }
}
