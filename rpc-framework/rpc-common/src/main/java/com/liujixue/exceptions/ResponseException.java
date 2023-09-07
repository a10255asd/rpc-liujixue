package com.liujixue.exceptions;

/**
 * @Author LiuJixue
 * @Date 2023/9/7 16:15
 * @ClassName: ResponseException
 * @Description: TODO
 */
public class ResponseException extends RuntimeException {
    private byte code;
    private  String msg;

    public ResponseException(byte code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
