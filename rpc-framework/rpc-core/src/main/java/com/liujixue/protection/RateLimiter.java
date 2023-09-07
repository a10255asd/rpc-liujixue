package com.liujixue.protection;

/**
 * @Author LiuJixue
 * @Date 2023/9/7 11:02
 * @ClassName: RateLimiter
 * @Description: TODO
 */
public interface RateLimiter {
    /**
     * 是否允许新请求进入
     * @return true 可以进入， false 拦截
     */
    boolean allowRequest();
}
