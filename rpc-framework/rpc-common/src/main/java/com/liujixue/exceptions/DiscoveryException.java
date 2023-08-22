package com.liujixue.exceptions;

/**
 * @Author LiuJixue
 * @Date 2023/8/22 09:07
 * @PackageName:com.liujixue.exceptions
 * @ClassName: DiscoveryException
 * @Description: TODO
 */
public class DiscoveryException extends RuntimeException{
    public DiscoveryException() {
    }

    public DiscoveryException(Throwable cause) {
        super(cause);
    }

    public DiscoveryException(String message) {
        super(message);
    }
}
