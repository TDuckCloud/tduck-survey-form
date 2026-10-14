package com.tduck.cloud.storage.cloud;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import com.tduck.cloud.storage.exception.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

/**
 * Oss基础客户端
 *
 * @author : smalljop
 * @since : 2018-10-18 13:57
 **/
@Slf4j
public abstract class OssStorageService {

    /**
     * 云厂商元数据地址与特殊受限地址列表（绝对禁止访问）
     */
    private static final String[] METADATA_HOSTS = {
            "169.254.169.254", "metadata.google.internal", "100.100.100.200"
    };

    /**
     * oss配置
     */
    public OssStorageConfig config;


    /**
     * 文件上传
     *
     * @param is   文件
     * @param path 相对于存储系统的路径
     * @return 文件地址
     */
    public abstract String upload(InputStream is, String path);


    /**
     * 文件上传
     *
     * @param data 文件 上传路径
     * @param path 相对于存储系统的路径
     * @return 文件地址
     */
    public abstract String upload(byte[] data, String path);


    /**
     * 判断附件 URL 是否属于本平台合法的存储域（完全比对域名、端口及路径前缀）
     *
     * @param fileUrl 文件地址
     * @return 是否属于本系统合法存储域
     */
    public boolean isAllowedStorageUrl(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return false;
        }
        // 本地存储相对路径（支持 /u/、/file/ 及配置的 accessPathPattern 前缀）
        if (fileUrl.startsWith("/u/") || fileUrl.startsWith("/file/")) {
            return true;
        }
        if (config != null && StrUtil.isNotBlank(config.getAccessPathPattern())) {
            String patternPrefix = StrUtil.subBefore(config.getAccessPathPattern(), "*", false);
            if (StrUtil.isNotBlank(patternPrefix) && fileUrl.startsWith(patternPrefix)) {
                return true;
            }
        }
        if (config == null || StrUtil.isBlank(config.getDomain())) {
            return false;
        }

        try {
            URI fileUri = URI.create(fileUrl);
            String fileHost = fileUri.getHost();
            if (StrUtil.isBlank(fileHost)) {
                return false;
            }

            // 阻断云元数据地址
            for (String meta : METADATA_HOSTS) {
                if (meta.equalsIgnoreCase(fileHost)) {
                    log.warn("禁止访问云元数据服务: {}", fileUrl);
                    return false;
                }
            }

            // 解析配置的 domain
            String configDomain = config.getDomain();
            URI configUri;
            if (configDomain.startsWith("http://") || configDomain.startsWith("https://")) {
                configUri = URI.create(configDomain);
            } else {
                configUri = URI.create("http://" + configDomain);
            }

            String configHost = configUri.getHost();
            if (StrUtil.isBlank(configHost)) {
                return false;
            }

            // 1. 完全比对 Host（忽略大小写）
            if (!configHost.equalsIgnoreCase(fileHost)) {
                return false;
            }

            // 2. 完全比对端口
            int filePort = getEffectivePort(fileUri);
            int configPort = getEffectivePort(configUri);
            if (filePort != configPort) {
                return false;
            }

            // 3. 如果配置的 domain 包含路径前缀（如 /u），附件 URL 的 path 也必须以此前缀开头
            String configPath = configUri.getPath();
            if (StrUtil.isNotBlank(configPath) && !"/".equals(configPath)) {
                String filePath = fileUri.getPath();
                if (filePath == null || !filePath.startsWith(configPath)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.warn("附件 URL 存储域比对异常: fileUrl={}, 原因: {}", fileUrl, e.getMessage());
            return false;
        }
    }

    /**
     * 获取 URI 的有效端口号（缺省时补充 http/https 默认端口）
     */
    private int getEffectivePort(URI uri) {
        int port = uri.getPort();
        if (port != -1) {
            return port;
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    /**
     * 下载文件
     *
     * @param url 文件地址
     * @return 文件输入流
     */
    public InputStream download(String url) {
        byte[] bytes = downloadBytes(url);
        return bytes != null ? new ByteArrayInputStream(bytes) : null;
    }

    /**
     * 下载文件字节数组（完全校验存储域、阻断元数据、超时控制与禁止重定向）
     *
     * @param url 文件地址
     * @return 文件字节
     */
    public byte[] downloadBytes(String url) {
        if (!isAllowedStorageUrl(url)) {
            log.warn("检测到非本系统存储附件 URL，拒绝下载: {}", url);
            return null;
        }
        return safeDownloadHttpBytes(url);
    }

    /**
     * 安全 HTTP 下载文件字节（禁止重定向、设置超时）
     *
     * @param url 文件地址
     * @return 字节数组，失败返回 null
     */
    protected byte[] safeDownloadHttpBytes(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                log.warn("仅支持 HTTP/HTTPS 协议下载: {}", url);
                return null;
            }

            HttpResponse response = HttpUtil.createGet(url)
                    .timeout(5000)
                    .setFollowRedirects(false)
                    .execute();
            if (response.getStatus() == HttpStatus.HTTP_OK) {
                return response.bodyBytes();
            } else {
                log.warn("下载文件 HTTP 状态异常: status={}, url={}", response.getStatus(), url);
            }
        } catch (Exception e) {
            log.warn("下载文件异常: url={}, 原因: {}", url, e.getMessage());
        }
        return null;
    }

    /**
     * 删除文件
     *
     * @param path 相对于存储系统的路径
     */
    public abstract void delete(String path);


    /**
     * 校验文件后缀
     *
     * @param path
     */
    protected void checkExtension(String path) {
        String ext = FileUtil.extName(path);
        if ("jsp".equalsIgnoreCase(ext) || "html".equalsIgnoreCase(ext)) {
            throw new StorageException("不支持上传该类型文件");
        }
    }
}
