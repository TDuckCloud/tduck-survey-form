package com.tduck.cloud.storage.cloud;


import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tduck.cloud.common.util.JsonUtils;
import com.tduck.cloud.common.util.ServletUtils;
import com.tduck.cloud.common.util.SpringContextUtils;
import com.tduck.cloud.envconfig.constant.ConfigConstants;
import com.tduck.cloud.envconfig.service.SysEnvConfigService;
import com.tduck.cloud.storage.enums.OssTypeEnum;
import com.tduck.cloud.storage.exception.InvalidExtensionException;
import com.tduck.cloud.storage.exception.StorageException;
import com.tduck.cloud.storage.util.MimeTypeUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;


/**
 * 文件上传Factory
 *
 * @author tduck
 */
@Slf4j
public final class OssStorageFactory {

    private static OssStorageService storageService;
    @Getter
    private static OssStorageConfig config;

    public static final int DEFAULT_FILE_NAME_LENGTH = 100;


    static {
        build();
    }

    public static synchronized void build() {
        try {
            OssStorageConfig config = JsonUtils.jsonToObj(SpringContextUtils.getBean(SysEnvConfigService.class).getValueByKey(ConfigConstants.FILE_ENV_CONFIG), OssStorageConfig.class);
            if (ObjectUtil.isNull(config)) {
                return;
            }
            OssStorageFactory.config = config;
            if (config.getOssType() == OssTypeEnum.QINIU) {
                storageService = new QiniuCloudStorageService(config);
            } else if (config.getOssType() == OssTypeEnum.ALIYUN) {
                storageService = new AliyunOssStorageService(config);
            } else if (config.getOssType() == OssTypeEnum.UPYUN) {
                storageService = new UpyunStorageService(config);
            } else if (config.getOssType() == OssTypeEnum.S3) {
                storageService = new S3StorageService(config);
            } else {
                // 本地存储默认配置
                if (StrUtil.isBlank(config.getUploadFolder())) {
                    ApplicationHome ah = new ApplicationHome(OssStorageFactory.class);
                    config.setUploadFolder(ah.getDir().getAbsolutePath() + "/upload");
                }
                if (StrUtil.isBlank(config.getDomain())) {
                    String domain = ServletUtils.getDomain(ServletUtils.getRequest());
                    config.setDomain(domain + "/u");
                }
                storageService = new LocalStorageService(config);
            }
            log.info("更新存储配置完成:{}", JsonUtils.objToJson(config));
        } catch (Exception exception) {
            log.error("更新存储配置失败:{}", exception.getMessage());
        }
    }

    public static OssStorageService getStorageService() {
        if (null == storageService) {
            build();
        }
        return storageService;
    }


    /**
     * 校验文件格式是否正常
     */
    public static void checkAllowedExtension(MultipartFile file, String[] allowedExtension) {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > DEFAULT_FILE_NAME_LENGTH) {
            throw new StorageException("上传文件名称超出长度，最多允许100个字符");
        }
        String extension = FileNameUtil.extName(file.getOriginalFilename());
        if (StrUtil.isEmpty(extension)) {
            extension = MimeTypeUtils.getExtension(Objects.requireNonNull(file.getContentType()));
        }
        String fileName = file.getOriginalFilename();
        if (allowedExtension != null && !ArrayUtil.contains(allowedExtension, extension)) {
            if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION) {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            } else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION) {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            } else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION) {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            } else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION) {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            } else {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            }
        }
    }

    /**
     * 判断附件 URL 是否属于本平台合法的存储域
     *
     * @param fileUrl 文件地址
     * @return 是否属于本系统合法存储域
     */
    public static boolean isAllowedStorageUrl(String fileUrl) {
        OssStorageService service = getStorageService();
        return service != null && service.isAllowedStorageUrl(fileUrl);
    }

    /**
     * 安全下载存储附件字节
     *
     * @param fileUrl 文件地址
     * @return 文件字节
     */
    public static byte[] downloadBytes(String fileUrl) {
        OssStorageService service = getStorageService();
        return service != null ? service.downloadBytes(fileUrl) : null;
    }

}
