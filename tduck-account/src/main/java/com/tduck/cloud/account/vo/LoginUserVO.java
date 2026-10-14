package com.tduck.cloud.account.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : smalljop
 * @description : 登录用户VO
 * @create : 2020-11-12 14:34
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserVO {
    /**
     * 头像
     */
    private String avatar;
    /**
     * 性别
     */
    private String name;

    /**
     * token
     */
    private String token;

    private boolean isAdmin;

    /**
     * 是否需要修改密码
     */
    private Boolean needChangePassword;

    public LoginUserVO(String avatar, String name, String token, boolean isAdmin) {
        this.avatar = avatar;
        this.name = name;
        this.token = token;
        this.isAdmin = isAdmin;
    }
}
