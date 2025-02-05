package com.jason.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 前后端交互通用返回类型
 *
 * @param <T> 返回给前端的 data 泛型实例
 * @author Jason
 */
@Data
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = -6265617791295744921L;

    private int code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }

}
