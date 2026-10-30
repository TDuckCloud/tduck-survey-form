package com.tduck.cloud.api.web.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.tduck.cloud.api.util.HttpUtils;
import com.tduck.cloud.common.util.CacheUtils;
import com.tduck.cloud.common.util.JsonUtils;
import com.tduck.cloud.common.util.Result;
import com.tduck.cloud.form.entity.UserFormSettingEntity;
import com.tduck.cloud.form.entity.struct.FormSettingSchemaStruct;
import com.tduck.cloud.form.request.CheckWritePwdRequest;
import com.tduck.cloud.form.service.UserFormSettingService;
import com.tduck.cloud.form.util.FormAuthUtils;
import com.tduck.cloud.wx.mp.constant.WxMpRedisKeyConstants;
import com.tduck.cloud.wx.mp.request.WxMpQrCodeGenRequest;
import com.tduck.cloud.wx.mp.service.WxMpUserService;
import com.tduck.cloud.wx.mp.vo.WxMpUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表单设置
 *
 * @author : smalljop
 * @description : 表单设置
 * @create : 2020-11-18 18:17
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserFormSettingController {

    private final UserFormSettingService userFormSettingService;
    private final WxMpUserService wxMpUserService;
    private final CacheUtils cacheUtils;
    private final WxMpService wxMpService;


    /**
     * 保存表单设置
     */
    @PostMapping("/user/form/setting/save")
    public Result<Boolean> saveFormSetting(@RequestBody Map<String, Object> setting) {
        String formKey = setting.get("formKey").toString();
        FormAuthUtils.hasPermission(formKey);
        return Result.success(userFormSettingService.saveFormSetting(setting));
    }

    /**
     * 表单提交设置查询
     */
    @GetMapping("/user/form/setting/{key}")
    public Result<Map<String, Object>> queryFormSettingByKey(@PathVariable("key") String formKey) {
        FormAuthUtils.hasPermission(formKey);
        UserFormSettingEntity setting = userFormSettingService.getFormSettingByKey(formKey);
        if (ObjectUtil.isNull(setting)) {
            return Result.success();
        }
        Map<String, Object> settings = setting.getSettings();
        settings.put(UserFormSettingEntity.Fields.formKey, formKey);
        return Result.success(settings);
    }


    /**
     * 当前填写设置的状态
     *
     * @param formKey  表单key
     * @param wxOpenId 微信openid
     * @param type     类型 1公开填写 2.指定填写
     */
    @GetMapping("/user/form/setting-status")
    @PermitAll
    public Result<Boolean> querySettingStatus(@RequestParam String formKey, @RequestParam(required = false) String wxOpenId, @RequestParam(required = false) Integer type, HttpServletRequest request) {
        return userFormSettingService.getUserFormWriteSettingStatus(formKey, HttpUtils.getIpAddr(request), wxOpenId, type);
    }


    /**
     * 填写微信通知二维码
     */
    @GetMapping("/user/form/wx/notify-qrcode")
    public Result<String> getWxNotifyQrCode(@RequestParam("key") String formKey) throws WxErrorException {
        FormAuthUtils.hasPermission(formKey);
        try {
            String loginSceneStr = JsonUtils.objToJson(new WxMpQrCodeGenRequest(WxMpQrCodeGenRequest.QrCodeType.SUB_NOTIFY, formKey));
            //5分钟有效
            WxMpQrCodeTicket ticket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(loginSceneStr, 10 * 60);
            String subNotifyQrcodeUrl = wxMpService.getQrcodeService().qrCodePictureUrl(ticket.getTicket());
            return Result.success(subNotifyQrcodeUrl);
        } catch (Exception e) {
            log.error("获取微信通知二维码失败", e);
            return Result.success("获取微信通知二维码失败");
        }
    }


    /**
     * 填写微信通知二维码
     */
    @PostMapping("/user/form/wx/delete/notify-user")
    public Result<Boolean> deleteWxNotifyQrCode(@RequestParam("key") String key, @RequestParam("openId") String openId) {
        FormAuthUtils.hasPermission(key);
        cacheUtils.removeList(StrUtil.format(WxMpRedisKeyConstants.WX_MP_SUB_NOTIFY, key), openId);
        return Result.success(true);
    }

    /**
     * 获取表单微信通知用户
     */
    @GetMapping("/user/form/wx/notify-user")
    public Result<List<WxMpUserVO>> getWxNotifyUser(@RequestParam("key") String formKey, @RequestParam(required = false) String openIdStr) {
        FormAuthUtils.hasPermission(formKey);
        Set<Object> subNotifyUsers = null;
        if (StrUtil.isNotBlank(openIdStr)) {
            subNotifyUsers = Sets.newHashSet(StrUtil.splitTrim(openIdStr, ";"));
        } else {
            List coll = cacheUtils.getList(StrUtil.format(WxMpRedisKeyConstants.WX_MP_SUB_NOTIFY, formKey), String.class);
            subNotifyUsers = Collections.singleton(coll.stream().collect(Collectors.toSet()));
        }
        return Result.success(wxMpUserService.listWxMpUserByOpenId(subNotifyUsers).stream().map(item -> new WxMpUserVO(item.getNickname(), item.getHeadImgUrl(), item.getOpenId())).collect(Collectors.toList()));
    }


    /**
     * 公开接口
     * 表单填写时需要的设置
     */
    @GetMapping("/user/form/public/settings/{key}")
    @PermitAll
    public Result queryPublicFormSettingByKey(@PathVariable("key") String formKey) {
        FormSettingSchemaStruct formSettingSchema = userFormSettingService.getFormSettingSchema(formKey);
        return Result.success(formSettingSchema);
    }

    /**
     * 公开接口
     * 检查填写密码是否正确
     */
    @PostMapping("/user/form/public/checkWritePwd")
    @PermitAll
    public Result<Boolean> checkWritePwd(@RequestBody @Validated CheckWritePwdRequest request) {
        return userFormSettingService.checkFormWritePwd(request.getFormKey(), request.getPassword());
    }


}
