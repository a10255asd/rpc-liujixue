package com.liujixue.netty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author LiuJixue
 * @Date 2023/8/23 08:31
 * @PackageName:com.liujixue.netty
 * @ClassName: CompletableFuture
 * @Description: CompletableFuture 使用 demo
 */
public class CompletableDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        // 如何在子线程中获取到 8
        /**
         * 可以获取子线程中的返回，过程中的结果，并可以在主线程中获得结果。get() 方法是一个阻塞方法。
         */
        CompletableFuture<Integer> completableFuture = new CompletableFuture();
        new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int i = 8;
            completableFuture.complete(i);
        }).start();
        Integer num = completableFuture.get(3, TimeUnit.MICROSECONDS);
        System.out.println(num);
    }
}
