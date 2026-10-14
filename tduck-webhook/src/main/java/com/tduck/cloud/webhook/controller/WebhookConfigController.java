package com.tduck.cloud.webhook.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tduck.cloud.common.exception.BaseException;
import com.tduck.cloud.common.util.Result;
import com.tduck.cloud.common.util.SpringContextUtils;
import com.tduck.cloud.common.util.SsrfValidator;
import com.tduck.cloud.common.validator.ValidatorUtils;
import com.tduck.cloud.webhook.constant.WebhookEventConstants;
import com.tduck.cloud.webhook.entity.WebhookConfigEntity;
import com.tduck.cloud.webhook.event.SendTestWebhookEvent;
import com.tduck.cloud.webhook.service.WebhookConfigService;
import com.tduck.cloud.webhook.service.sender.WebhookSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Webhook配置
 *
 * @author tduck-gen
 * @since 2023-03-27 13:50:06
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook/config")
public class WebhookConfigController {

    private final WebhookConfigService webhookConfigService;
    private final WebhookSenderService webhookSenderService;

    /**
     * 获取Webhook配置详细信息
     */
    @GetMapping(value = "/")
    public Result<WebhookConfigEntity> getInfo(@RequestParam String sourceType, @RequestParam String sourceId) {
        checkPermission(sourceType, sourceId);
        return Result.success(webhookConfigService.getOne(Wrappers.<WebhookConfigEntity>lambdaQuery()
                .eq(WebhookConfigEntity::getSourceType, sourceType)
                .eq(WebhookConfigEntity::getSourceId, sourceId)));
    }

    /**
     * 新增Webhook配置
     */
    @PostMapping("save")
    public Result<Boolean> save(@RequestBody WebhookConfigEntity webhookConfig) {
        ValidatorUtils.validateEntity(webhookConfig);
        checkPermission(webhookConfig.getSourceType(), webhookConfig.getSourceId());
        SsrfValidator.validateUrl(webhookConfig.getUrl());
        return Result.success(webhookConfigService.saveWebhookConfig(webhookConfig));
    }


    /**
     * 发送测试消息
     */
    @PostMapping("sendTest")
    public Result<Boolean> sendTest(@RequestBody WebhookConfigEntity webhookConfig) {
        ValidatorUtils.validateEntity(webhookConfig);
        checkPermission(webhookConfig.getSourceType(), webhookConfig.getSourceId());
        SsrfValidator.validateUrl(webhookConfig.getUrl());
        // 暂时只有一种 固定下 以后有需要再改
        SendTestWebhookEvent sendTestWebhookEvent = new SendTestWebhookEvent(webhookConfig, WebhookEventConstants.WEBHOOK_EVENT_TYPE_FORM_DATA_ADD);
        SpringContextUtils.publishEvent(sendTestWebhookEvent);
        return Result.success(true);
    }

    /**
     * 校验资源归属权限（防水平越权）
     */
    private void checkPermission(String sourceType, String sourceId) {
        if ("FORM".equalsIgnoreCase(sourceType)) {
            try {
                Class<?> authUtilsClass = Class.forName("com.tduck.cloud.form.util.FormAuthUtils");
                Method method = authUtilsClass.getMethod("hasPermission", String.class);
                method.invoke(null, sourceId);
            } catch (ClassNotFoundException ignored) {
                // 如果未引入表单模块则跳过
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException) e.getTargetException();
                }
                throw new BaseException("无权操作此表单配置");
            } catch (Exception e) {
                log.error("校验表单 Webhook 权限异常", e);
                throw new BaseException("校验权限异常");
            }
        }
    }

}
