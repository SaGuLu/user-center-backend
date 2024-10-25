package com.jason.usercenter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class UserCenterApplicationTests {

    /**
     * 测试加密
     *
     * @throws NoSuchAlgorithmException
     */
    @Test
    void testDigest() throws NoSuchAlgorithmException {
        // 指定 MD5 加密算法
        // MessageDigest md5 = MessageDigest.getInstance("MD5");

        // Spring 自带的工具类，生成的是16进制的密码。salt + entity.getPassword
        String newPassword = DigestUtils.md5DigestAsHex(("salt" + "myPassWD").getBytes());
        System.out.println(newPassword);
    }

    /**
     * 测试账号校验
     */
    @Test
    void testUserAccount() {
        // ^[a-zA-Z0-9_]*$
        String validPattern = "\\w";
        Matcher matcher = Pattern.compile(validPattern).matcher("123456@qq.com");
        Matcher matcher2 = Pattern.compile(validPattern).matcher("1  23456qq.com");
        Matcher matcher3 = Pattern.compile(validPattern).matcher("123456qq_com");
        System.out.println(matcher.find());
        System.out.println(matcher2.find());
        System.out.println(matcher3.find());
    }

    @Test
    void contextLoads() {
    }

}
