package com.liujixue.exceptions;

/**
 * @Author LiuJixue
 * @Date 2023/8/28 17:13
 * @PackageName:com.liujixue.exceptions
 * @ClassName: SerializeException
 * @Description: TODO
 */
public class CompressException extends RuntimeException{
    public CompressException(Throwable cause) {
        super(cause);
    }
}
