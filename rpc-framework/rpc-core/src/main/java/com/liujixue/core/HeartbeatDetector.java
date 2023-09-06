package com.liujixue.core;

import com.liujixue.NettyBootstrapInitializer;
import com.liujixue.RpcBootstrap;
import com.liujixue.compress.CompressorFactory;
import com.liujixue.discovery.Registry;
import com.liujixue.enumeration.RequestType;
import com.liujixue.serialize.SerializerFactory;
import com.liujixue.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author LiuJixue
 * @Date 2023/8/31 11:13
 * @PackageName:com.liujixue.core
 * @ClassName: HeartbeatDetector
 * @Description: 心跳检测器，核心目的是探活，感知那些服务器是正常的那些是不正常的
 */
@Slf4j
public class HeartbeatDetector {
    public static void detectHeartbeat(String serviceName){
        // 1.从注册中心拉取服务列表并简历连接
        Registry registry = RpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        List<InetSocketAddress> addresses = registry.lookup(serviceName);
        // 2.将连接进行缓存
        for (InetSocketAddress address : addresses) {
            try {
                if(!RpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    RpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Thread thread = new Thread(() ->
            // 3.定期发送消息
            new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000)
                ,"Rpc-Heartbeat-Detect-Thread");
        thread.setDaemon(true);
        thread.start();
    }
    private static class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // 将响应时常的map进行清空
            RpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            // 遍历所有的channel
            Map<InetSocketAddress, Channel> cache = RpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress,Channel> entry :cache.entrySet()){
                // 定义一个重试的次数
                int tryTimes = 3;
                while (tryTimes>0) {
                    Channel channel = entry.getValue();
                    long start = System.currentTimeMillis();
                    // 构建一个心跳请求
                    RpcRequest rpcRequest = RpcRequest.builder()
                            .requestId(RpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                            .compressType(CompressorFactory.getCompressor(RpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                            .requestType(RequestType.HEARTBEAT.getId())
                            .serializeType(SerializerFactory.getSerializer(RpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                            .timeStamp(start)
                            .build();
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    // 将completableFuture 挂起且暴露，并且得到服务提供方相应的时候调用 complete 方法
                    RpcBootstrap.PADDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);
                    channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
                        // 只需要处理异常就行
                        if (!promise.isSuccess()) {
                            completableFuture.completeExceptionally(promise.cause());
                        }
                    });
                    //
                    Long endTime = 0L;
                    try {
                        // 不想一直阻塞可以添加参数
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        // 一旦发生问题，需要优先重试
                        tryTimes--;
                        log.error("和地址为【{}】的主机连接发生异常，正在进行第【{}】次重试", channel.remoteAddress(),3-tryTimes);
                        // 将是失效的地址移除服务列表
                        if(tryTimes == 0){
                            RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        // 尝试等待一段时间后重试
                        try {
                            Thread.sleep(10*(new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                    Long time = endTime - start;
                    // 使用treemap进行缓存
                    RpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                    log.debug("和【{}】服务器的响应时间是：------》【{}】", entry.getKey(), time);
                    break;
                }
            }
            log.info("------------------------响应时间的treemap--------------------------");
            for (Map.Entry<Long,Channel> entry :RpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()){
                if(log.isDebugEnabled()){
                    log.debug("【{}】--------->channelID【{}】",entry.getKey(),entry.getValue().id());
                }
            }
        }
    }
}
