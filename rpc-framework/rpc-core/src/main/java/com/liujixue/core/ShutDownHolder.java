package com.liujixue.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @Author LiuJixue
 * @Date 2023/9/8 17:27
 * @ClassName: ShutDownHolder
 */
public class ShutDownHolder {
    // 用来标记请求的挡板
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);
    // 用于请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();

}
