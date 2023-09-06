package com.liujixue.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author LiuJixue
 * @Date 2023/9/6 17:07
 * @PackageName:com.liujixue.annotation
 * @ClassName: TryTimes
 * @Description: TODO
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TryTimes {
    int tryTimes() default 3;
    int intervalTime() default 2000;
}
