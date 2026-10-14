package com.tduck.cloud.common.util;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.tduck.cloud.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.URI;

/**
 * SSRF 安全防护校验工具（轻量实用型）
 *
 * @author tduck
 */
@Slf4j
public class SsrfValidator {

    /**
     * 高危本地与受限主机黑名单（避免扫描本地服务与云厂商元数据）
     */
    private static final String[] BLOCKED_HOSTS = {
            "localhost", "0.0.0.0", "169.254.169.254", "metadata.google.internal", "100.100.100.200"
    };

    /**
     * 校验 URL（防止恶意协议、本地回环及局域网 IP 攻击；纯内存校验，不阻塞 DNS 解析）
     *
     * @param urlStr 目标 URL
     */
    public static void validateUrl(String urlStr) {
        if (StrUtil.isBlank(urlStr)) {
            throw new BaseException("目标 URL 不能为空");
        }
        URI uri;
        try {
            uri = URI.create(urlStr.trim());
        } catch (Exception e) {
            throw new BaseException("URL 格式不合法");
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new BaseException("仅支持 HTTP 或 HTTPS 协议");
        }

        String host = uri.getHost();
        if (StrUtil.isBlank(host)) {
            throw new BaseException("无效的目标域名或 IP");
        }

        // 1. 拦截明确的受限/元数据主机名
        for (String blocked : BLOCKED_HOSTS) {
            if (blocked.equalsIgnoreCase(host)) {
                log.warn("SSRF 拦截高危请求 host={}", host);
                throw new BaseException("禁止访问本机或受限地址");
            }
        }

        // 2. 如果输入的是字面量 IP 地址，纯内存校验是否属于局域网或私网网段（不发起任何网络 DNS 请求）
        if (Validator.isIpv4(host) || Validator.isIpv6(host)) {
            try {
                InetAddress address = InetAddress.getByName(host);
                boolean isInner = address.isLoopbackAddress()
                        || address.isAnyLocalAddress()
                        || address.isSiteLocalAddress()
                        || address.isLinkLocalAddress();
                if (!isInner && Validator.isIpv4(host)) {
                    isInner = NetUtil.isInnerIP(host);
                }
                if (isInner) {
                    log.warn("SSRF 拦截局域网/私网 IP 地址 host={}", host);
                    throw new BaseException("禁止访问局域网或私有内网 IP 地址");
                }
            } catch (BaseException e) {
                throw e;
            } catch (Exception ignored) {
            }
        }
    }
}
