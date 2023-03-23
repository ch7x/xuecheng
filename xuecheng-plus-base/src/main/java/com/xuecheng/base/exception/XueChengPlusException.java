package com.xuecheng.base.exception;

import lombok.Data;

/**
 * 本项目自定义异常类型
 */
@Data
public class XueChengPlusException extends RuntimeException{
    private String errMessage;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public static void cast(String message){
        throw new XueChengPlusException(message);
    }

    public static void cast(CommonError error){
        throw new XueChengPlusException(error.getErrMessage());
    }
}
