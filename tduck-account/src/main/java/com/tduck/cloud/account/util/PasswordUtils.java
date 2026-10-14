package com.tduck.cloud.account.util;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.tduck.cloud.account.entity.UserEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 密码工具类
 */
public class PasswordUtils {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 强密码正则：8-20位，必须同时包含字母和数字
     */
    public static final String STRONG_PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$";
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(STRONG_PASSWORD_REGEX);

    /**
     * 密码最小长度
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * 密码最大长度
     */
    public static final int MAX_PASSWORD_LENGTH = 20;

    /**
     * 常见弱口令黑名单
     */
    private static final Set<String> WEAK_PASSWORD_SET = new HashSet<>(Arrays.asList(
            "123456", "1123456", "12345678", "123456789", "88888888", "66666666",
            "11111111", "00000000", "admin123", "password", "admin888", "123123123",
            "qwertyui", "qwerty123"
    ));

    /**
     * 校验密码强度
     *
     * @param password 待检测密码
     * @return 错误提示信息，若校验通过则返回 null
     */
    public static String validatePasswordStrength(String password) {
        if (StrUtil.isBlank(password)) {
            return "密码不能为空";
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return "密码长度必须在" + MIN_PASSWORD_LENGTH + "-" + MAX_PASSWORD_LENGTH + "位之间";
        }
        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            return "密码必须同时包含字母和数字";
        }
        if (WEAK_PASSWORD_SET.contains(password.toLowerCase())) {
            return "密码过于简单，不能使用常见弱密码";
        }
        return null;
    }


    /**
     * 加密密码
     *
     * @param password 密码
     * @return 加密后的密码
     */
    public static String encode(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 密码匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }


    /**
     * 检查密码是否正确
     */
    public static boolean checkPassword(UserEntity userEntity, String inputPassword) {
        //  加密模式
        if (userEntity.getPasswordType() == 0) {
            DigestUtil.sha256Hex(inputPassword);
            return StrUtil.equals(userEntity.getPassword(), DigestUtil.sha256Hex(inputPassword));
        }
        //  bcrypt模式
        return matches(inputPassword, userEntity.getPassword());
    }


}
