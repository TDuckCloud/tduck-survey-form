package com.tduck.cloud.storage.cloud;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.tduck.cloud.storage.exception.StorageException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Oss基础客户端
 *
 * @author : smalljop
 * @since : 2018-10-18 13:57
 **/
public abstract class OssStorageService {

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
     * 下载文件
     *
     * @param url 文件地址
     * @return 文件
     */
    public InputStream download(String url) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpUtil.download(url, output, false);
        return new ByteArrayInputStream(output.toByteArray());
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
