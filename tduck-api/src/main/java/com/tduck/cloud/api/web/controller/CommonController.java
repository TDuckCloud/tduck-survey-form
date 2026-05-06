package com.tduck.cloud.api.web.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.IdUtil;
import com.tduck.cloud.api.annotation.NotLogin;
import com.tduck.cloud.common.util.AsyncProcessUtils;
import com.tduck.cloud.common.util.Result;
import com.tduck.cloud.storage.cloud.OssStorageFactory;
import com.tduck.cloud.storage.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author : smalljop
 * @description : 通用
 * @create :  2021/06/08 15:32
 **/
@RestController
@RequestMapping("/common/")
public class CommonController {


    /**
     * 获取异步处理进度
     */
    @GetMapping("/process")
    public Result getProcess(@RequestParam String key) {
        return Result.success(AsyncProcessUtils.getProcess(key));
    }


    /**
     * 上传
     *
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    @NotLogin
    public Result avatar(@RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            OssStorageFactory.checkAllowedExtension(file, MimeTypeUtils.MimeTypeEnum.DEFAULT.getExtensions());
            String path = IdUtil.simpleUUID() +
                    CharUtil.DOT +
                    FileUtil.extName(file.getOriginalFilename());
            String url = OssStorageFactory.getStorageService().upload(file.getInputStream(), path);
            return Result.success(url);
        }
        return Result.failed("上传文件异常，请联系管理员");
    }



}
