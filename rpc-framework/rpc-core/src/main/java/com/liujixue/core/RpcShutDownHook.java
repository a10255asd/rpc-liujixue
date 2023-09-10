package com.liujixue.core;

import java.util.concurrent.TimeUnit;

/**
 * @Author LiuJixue
 * @Date 2023/9/8 17:12
 * @ClassName: RpcShutDownHook
 */
public class RpcShutDownHook extends Thread{
    @Override
    public void run() {
        // 1. 打开挡板（boolean 需要线程安全）
        ShutDownHolder.BAFFLE.set(true);
        // 2. 等待计数器归零（正常的请求处理结束）可以用atomicInteger
        long start = System.currentTimeMillis();
        while(true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (ShutDownHolder.REQUEST_COUNTER.sum() == 0L
            || System.currentTimeMillis() -start > 10000){
                break;
            }
        }
    }

}
