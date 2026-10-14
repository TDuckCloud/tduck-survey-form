package com.tduck.cloud.api.web.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 上传文件访问安全过滤器
 * <p>
 * 为本地上传文件访问增加安全响应头，禁止浏览器执行内嵌 JavaScript，防御存储型 XSS
 *
 * @author tduck
 */
@Component
@Order(-100)
public class StaticFileSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/u/")) {
            // 禁止浏览器进行 MIME 类型嗅探推断
            response.setHeader("X-Content-Type-Options", "nosniff");
            // 严格沙箱 CSP，禁止执行任何内嵌 JavaScript
            response.setHeader("Content-Security-Policy", "default-src 'none'; img-src 'self' data: blob:; media-src 'self'; style-src 'unsafe-inline'; sandbox");
        }
        filterChain.doFilter(request, response);
    }
}
