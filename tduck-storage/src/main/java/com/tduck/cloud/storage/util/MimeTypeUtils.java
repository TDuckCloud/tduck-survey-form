package com.tduck.cloud.storage.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 媒体类型工具类
 *
 * @author ruoyi
 */
public class MimeTypeUtils {
    public static final String IMAGE_PNG = "image/png";

    public static final String IMAGE_JPG = "image/jpg";

    public static final String IMAGE_JPEG = "image/jpeg";

    public static final String IMAGE_BMP = "image/bmp";

    public static final String IMAGE_GIF = "image/gif";

    public static final String[] IMAGE_EXTENSION = {"bmp", "gif", "jpg", "jpeg", "png", "tiff", "webp", "tif"};

    public static final String[] FLASH_EXTENSION = {"swf", "flv"};

    public static final String[] MEDIA_EXTENSION = {"swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg", "asf", "rm", "rmvb"};

    public static final String[] VIDEO_EXTENSION = {"mp4", "avi", "rmvb", "webm", "ogg", "mpg", "mpeg", "mov", "wmv", "flv", "mkv"};
    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            // 图片
            "bmp", "gif", "jpg", "jpeg", "png", "tiff", "webp", "tif",
            // word excel powerpoint
            "doc", "docx", "xls", "xlsx", "csv", "ppt", "pptx", "txt", "odp",
            // 压缩文件
            "rar", "zip", "gz", "bz2", "tar",
            // 视频格式
            "swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "mpg", "asf", "rm",
            "mp4", "avi", "rmvb", "webm", "ogg", "mpg", "mpeg", "mov", "wmv", "flv", "mkv",
            // pdf
            "pdf",

    };

    public static final String[] CSS_EXTENSION = {"css"};

    public static String getExtension(String prefix) {
        switch (prefix) {
            case IMAGE_PNG:
                return "png";
            case IMAGE_JPG:
                return "jpg";
            case IMAGE_JPEG:
                return "jpeg";
            case IMAGE_BMP:
                return "bmp";
            case IMAGE_GIF:
                return "gif";
            default:
                return "";
        }
    }

    /**
     * 维护文件类型与对应关系枚举 前端传入通配类型 服务端转换为对应类型
     */
    @Getter
    @AllArgsConstructor
    public static enum MimeTypeEnum {
        IMAGE(IMAGE_EXTENSION),
        FLASH(FLASH_EXTENSION),
        MEDIA(MEDIA_EXTENSION),
        VIDEO(VIDEO_EXTENSION),
        CSS(CSS_EXTENSION),
        DEFAULT(DEFAULT_ALLOWED_EXTENSION);
        private final String[] extensions;
    }
}
