package com.liujixue.exceptions;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 17:09
 * @PackageName:com.liujixue.exceptions
 * @ClassName: NetworkException
 * @Description: TODO
 */
public class NetworkException extends RuntimeException {
    public NetworkException() {
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }

    public NetworkException(String message) {
        super(message);
    }
}
