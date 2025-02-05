package com.jason.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author Jason
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -737231449183458309L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;
}
