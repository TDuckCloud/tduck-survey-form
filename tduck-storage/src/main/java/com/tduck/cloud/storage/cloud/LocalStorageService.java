package com.tduck.cloud.storage.cloud;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.tduck.cloud.storage.exception.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 本地文件存储
 *
 * @author : smalljop
 * @since : 2018-10-18 14:01
 */
@Slf4j
public class LocalStorageService extends OssStorageService {
    public LocalStorageService(OssStorageConfig config) {
        this.config = config;
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        checkExtension(path);
        log.info("上传文件到本地服务器{}",config.getUploadFolder());
        File file = FileUtil.file(config.getUploadFolder() + File.separator + path);
        //目录是否存在
        if (!FileUtil.exist(file.getParent())) {
            FileUtil.mkdir(file.getParent());
        }
        FileUtil.writeFromStream(inputStream, file);
        return config.getDomain() + "/" + path;
    }

    @Override
    public String upload(byte[] data, String path) {
        checkExtension(path);
        File file = FileUtil.file(config.getUploadFolder() + File.separator + path);
        //目录是否存在
        if (!FileUtil.exist(file.getParent())) {
            FileUtil.mkdir(file.getParent());
        }
        FileUtil.writeBytes(data, file);
        return config.getDomain() + "/" + path;
    }


    @Override
    public void delete(String path) {
        FileUtil.del(config.getUploadFolder() + File.separator + path);
    }

    @Override
    public InputStream download(String url) {
        if (!isAllowedStorageUrl(url)) {
            log.warn("检测到非本系统存储附件 URL，拒绝下载: {}", url);
            return null;
        }
        // 本地存储优先从磁盘直接打开文件流
        File file = getSafeLocalFile(extractRelativePath(url));
        if (file != null && FileUtil.exist(file) && file.isFile()) {
            return FileUtil.getInputStream(file);
        }
        return super.download(url);
    }

    @Override
    public byte[] downloadBytes(String url) {
        if (!isAllowedStorageUrl(url)) {
            log.warn("检测到非本系统存储附件 URL，拒绝下载: {}", url);
            return null;
        }
        // 本地存储优先从磁盘直接读取
        File file = getSafeLocalFile(extractRelativePath(url));
        if (file != null && FileUtil.exist(file) && file.isFile()) {
            return FileUtil.readBytes(file);
        }
        // 本地文件未找到时兜底走 HTTP 下载
        return super.downloadBytes(url);
    }

    /**
     * 获取安全的本地文件对象，严格防御目录穿越（Path Traversal）
     */
    private File getSafeLocalFile(String path) {
        if (StrUtil.isBlank(path)) {
            return null;
        }
        String safePath = FileUtil.normalize(path);
        if (safePath.contains("..")) {
            log.warn("检测到非法目录穿越路径: {}", path);
            return null;
        }
        File baseDir = FileUtil.file(config.getUploadFolder());
        File targetFile = FileUtil.file(baseDir, safePath);
        try {
            // 确保解析后的真实绝对路径严格位于 uploadFolder 基础目录内，杜绝穿越到系统其他目录
            String baseCanonical = baseDir.getCanonicalPath();
            String targetCanonical = targetFile.getCanonicalPath();
            if (!targetCanonical.startsWith(baseCanonical)) {
                log.warn("目标文件路径超出存储根目录范围: target={}, base={}", targetCanonical, baseCanonical);
                return null;
            }
            return targetFile;
        } catch (IOException e) {
            log.warn("解析本地文件路径异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从本地附件 URL 中提取相对于 uploadFolder 的相对路径
     */
    private String extractRelativePath(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        String cleanUrl = StrUtil.subBefore(url, "?", false);
        String domain = config != null ? config.getDomain() : null;
        if (StrUtil.isNotBlank(domain) && cleanUrl.startsWith(domain)) {
            cleanUrl = StrUtil.removePrefix(cleanUrl, domain);
        }
        // 支持 /u/ 与 /file/ 前缀
        if (cleanUrl.startsWith("/u/")) {
            cleanUrl = StrUtil.removePrefix(cleanUrl, "/u/");
        } else if (cleanUrl.startsWith("u/")) {
            cleanUrl = StrUtil.removePrefix(cleanUrl, "u/");
        } else if (cleanUrl.startsWith("/file/")) {
            cleanUrl = StrUtil.removePrefix(cleanUrl, "/file/");
        } else if (cleanUrl.startsWith("file/")) {
            cleanUrl = StrUtil.removePrefix(cleanUrl, "file/");
        } else if (config != null && StrUtil.isNotBlank(config.getAccessPathPattern())) {
            String patternPrefix = StrUtil.subBefore(config.getAccessPathPattern(), "*", false);
            if (StrUtil.isNotBlank(patternPrefix) && cleanUrl.startsWith(patternPrefix)) {
                cleanUrl = StrUtil.removePrefix(cleanUrl, patternPrefix);
            }
        }
        if (cleanUrl.startsWith("/")) {
            cleanUrl = StrUtil.removePrefix(cleanUrl, "/");
        }
        return cleanUrl;
    }
}
