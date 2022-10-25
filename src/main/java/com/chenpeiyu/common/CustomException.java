package com.chenpeiyu.common;

/**
 * 自定义异常类
 */
public class CustomException extends RuntimeException {
    public CustomException(String message) {
        //发送异常信息
        super(message);
    }
}
