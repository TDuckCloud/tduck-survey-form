package com.tduck.cloud.webhook.service.sender;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import com.tduck.cloud.common.util.JsonUtils;
import com.tduck.cloud.common.util.SsrfValidator;
import com.tduck.cloud.webhook.constant.WebhookEventConstants;
import com.tduck.cloud.webhook.entity.WebhookConfigEntity;
import com.tduck.cloud.webhook.entity.WebhookEventEntity;
import com.tduck.cloud.webhook.exception.SendException;
import com.tduck.cloud.webhook.service.WebhookEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Webhook发送器
 *
 * @author : tduck
 * @since :  2023/03/27 16:17
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookSenderService {

    private final WebhookEventService webhookEventService;
    private RetryTemplate retryTemplate = null;

    @PostConstruct
    public void init() {
        retryTemplate = RetryTemplate.builder()
                // 最多重试3次
                .maxAttempts(3)
                // 指数退避策略
                .exponentialBackoff(100, 3, 10000)
                .retryOn(SendException.class)
                .traversingCauses()
                .build();
    }

    public void sendWebhook(WebhookConfigEntity webhookConfig, WebhookEventEntity webhookEvent) {
        // RetryCallback 重试回调方法
        retryTemplate.execute(
                retryContext -> {
                    try {
                        boolean success = sendHttp(webhookConfig, webhookEvent);
                        if (success) {
                            webhookEvent.setStatus(WebhookEventConstants.WEBHOOK_EVENT_STATUS_SUCCESS);
                        } else {
                            webhookEvent.setStatus(WebhookEventConstants.WEBHOOK_EVENT_STATUS_FAILED);
                            throw new SendException("返回状态码不是200");
                        }
                    } catch (Exception e) {
                        webhookEvent.setStatus(WebhookEventConstants.WEBHOOK_EVENT_STATUS_FAILED);
                        String msg = "http 推送异常" + e.getMessage();
                        webhookEvent.setLastError(msg);
                        throw new SendException(msg);
                    } finally {
                        webhookEvent.setRetryTimes(webhookEvent.getRetryTimes() + 1);
                        webhookEvent.setUpdateTime(LocalDateTimeUtil.now());
                        webhookEventService.updateById(webhookEvent);
                    }
                    return false;
                },
                // RecoveryCallback 异常回调方法
                retryContext -> false);
    }


    /**
     * http 推送
     *
     * @return boolean 是否成功
     */
    public boolean sendHttp(WebhookConfigEntity webhookConfig, WebhookEventEntity webhookEvent) {
        String url = webhookConfig.getUrl();
        // SSRF 防御二次校验
        SsrfValidator.validateUrl(url);

        HttpRequest request = null;
        if (webhookConfig.getRequestType().equals(WebhookConfigEntity.RequestTypeEnum.GET.toString())) {
            request = HttpUtil.createGet(url);
        } else {
            request = HttpUtil.createPost(url);
        }
        request.timeout(3000);
        request.setFollowRedirects(false);

        Map<String, Object> eventData = JsonUtils.jsonToMap(webhookEvent.getEventData());
        if (eventData == null) {
            eventData = MapUtil.newHashMap();
        }
        eventData.put("eventType", webhookEvent.getEventType());
        eventData.put("eventTime", LocalDateTimeUtil.toEpochMilli(webhookEvent.getCreateTime()));
        request.body(JsonUtils.objToJson(eventData));
        HttpResponse execute = request.execute();
        log.debug("http 推送结果：{}", execute.body());
        return execute.getStatus() == HttpStatus.HTTP_OK;
    }

}
