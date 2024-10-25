package com.jason.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author Jason
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -334399325737052753L;
    private String userAccount;
    private String userPassword;
}
