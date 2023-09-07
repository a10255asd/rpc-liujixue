package com.liujixue.protection;

import java.util.Map;

/**
 * @Author LiuJixue
 * @Date 2023/9/7 08:57
 * @ClassName: TokenBuketRateLimiter
 * @Description: 基于令牌桶算法的限流器
 */
public class TokenBuketRateLimiter implements RateLimiter{
    // 1. 代表令牌的数量 > 0 说明有令牌能放行，放行就减1，==0无令牌阻拦
    private int tokens;
    // 限流本质，令牌数
    private int capacity;
    // 令牌桶的令牌，如果没了要怎么办？按照一定的速率给令牌桶加令牌，如美妙加500个。不能超过总数
    // 方案一：可以用定时任务去加--> 启动一个定时任务，美妙执行一次，tokens + 500 。不能超过 capacity
    // 对于单机版限流器可以有更简单的操作。每一次有请求要发送的时候增加
    private int rate;
    // 上一次放令牌的时间
    private Long lastTokenTime;
    public TokenBuketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = capacity;
    }

    /**
     * 判断请求是否可以放行
     * @return true 放行，false 拦截
     */
    public synchronized boolean allowRequest(){
        // 1. 给令牌桶添加令牌
        // 计算从现在到上一次的时间间隔需要添加的令牌数
        Long currentTime = System.currentTimeMillis();
        long timeInterval = currentTime - lastTokenTime;
        // 如果间隔时间超过一秒, 放令牌
        if(timeInterval >= 1000/rate){
            int needAddTokens = (int)(timeInterval * rate /1000);
            System.out.println("needAddTokens =" + needAddTokens);
            // 给令牌桶添加令牌
            tokens = Math.min(capacity,tokens + needAddTokens);
            System.out.println("tokens =" + tokens);
            // 标记最后一次放入令牌的时间
            this.lastTokenTime = System.currentTimeMillis();
        }
        // 2. 自己获取令牌,如果令牌桶中有令牌则放行，否则拦截。
        if(tokens >0){
            tokens--;
            System.out.println("请求被放行-----------");
            return true;
        }else {
            System.out.println("请求被拦截-----------");
            return false;
        }
    }

    public static void main(String[] args) {
        TokenBuketRateLimiter tokenBuketRateLimiter = new TokenBuketRateLimiter(10,10);
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(10);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            boolean allowRequest = tokenBuketRateLimiter.allowRequest();
            System.out.println("allowRequest:--------" + allowRequest);
        }
    }
}
