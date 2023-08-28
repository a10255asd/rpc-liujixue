package com.liujixue;

import java.util.Date;
import java.util.concurrent.atomic.LongAdder;

/**
 * @Author LiuJixue
 * @Date 2023/8/28 11:23
 * @PackageName:com.liujixue
 * @ClassName: IdGenerator
 * @Description: 请求id生成器
 */
public class IdGenerator {
    /**
     * 单机版本的线程安全，一旦变成集群状态就不行了
     *
     *
     * private static LongAdder longAdder = new LongAdder();
     public static long getId(){
     longAdder.increment();
     return longAdder.sum();
     }
     */
    /***
     * 雪花算法-- 世界上没有一片雪花是一样的
     * 组成：
     * 机房号（数据中心）： 5 bit 32
     * 机器号：5 bit 32
     * 时间戳（Long 1970-1-1 - 至今毫秒数 ）：原本64位表示的时间，必须要减少（42）自由选择一个比较近的时间，比如我们公司成立的日期
     * 同一个机房的同一个机器号的不同时间的同一个时间因为并发量很大需要多个id
     * 序列号：12 bit
     * 5 + 5 + 12 +42
     */
    // 起始时间戳
    private static final long START_STAMP = DateUtil.get("2022-1-1").getTime();

    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BIT = 12L;
    // 最大值 Math.pow(2,5) -1
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long DATA_MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);
    // 时间戳(42) + 机房号(5)  + 机器号(5) + 序列号(12)
    // 101010101010101010101010101010101010101011 10101 10101 101011010101
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT +SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();
    // TODO:时钟回拨的问题，我们需要去处理
    private long lastTimeStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId) {
        // 判断传入的参数是否合法
        if(dataCenterId > DATA_CENTER_MAX || machineId > DATA_MACHINE_MAX){
            throw new IllegalArgumentException("您传入的数据中心编号或机器号不合法");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }
    public  long getId(){
        // 第一步： 处理时间戳的问题
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime - START_STAMP;
        // 判断时钟回拨
        if(timeStamp < lastTimeStamp){
            throw new RuntimeException("您的服务器进行了时钟回调");
        }
        // sequenceId 需要做一些处理，如果是同一个时间节点，必须自增
        if(timeStamp == lastTimeStamp){
            sequenceId.increment();
            if(sequenceId.sum() >= SEQUENCE_MAX){
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        }else {
            sequenceId.reset();
        }
        // 执行结束将时间戳赋值给 lastTimeStamp
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT
                | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    /**
     * 获得下一个时间戳
     * @return
     */
    private long getNextTimeStamp() {
        // 获取当前的时间戳
        long current = System.currentTimeMillis();
        // 如果一样就一直循环，直到下一个时间戳
        while(current == lastTimeStamp){
            current = System.currentTimeMillis();
        }
        return current;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1L,2L);
        for (int i = 0; i < 1000; i++) {
            new Thread(()->{
                System.out.println(idGenerator.getId());
            }).start();
        }
    }
}
