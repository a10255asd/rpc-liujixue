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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @Author LiuJixue
 * @Date 2023/8/31 11:13
 * @PackageName:com.liujixue.core
 * @ClassName: HeartbeatDetector
 * @Description: 心跳检测器
 */
@Slf4j
public class HeartbeatDetector {
    public static void detectHeartbeat(String serviceName){
        // 1.从注册中心拉取服务列表并简历连接
        Registry registry = RpcBootstrap.getInstance().getRegistry();
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
        // 3.定期发送消息
        new Timer().schedule(new MyTimerTask(),2000);
    }
    private static class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // 遍历所有的channel
            Map<InetSocketAddress, Channel> cache = RpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress,Channel> entry :cache.entrySet()){
                Channel channel = entry.getValue();
                long start = System.currentTimeMillis();
                // 构建一个心跳请求
                RpcRequest rpcRequest = RpcRequest.builder()
                        .requestId(RpcBootstrap.ID_GENERATOR.getId())
                        .compressType(CompressorFactory.getCompressor(RpcBootstrap.COMPRESS_TYPE).getCode())
                        .requestType(RequestType.HEARTBEAT.getId())
                        .serializeType(SerializerFactory.getSerializer(RpcBootstrap.SERIALIZE_TYPE).getCode())
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
                    endTime = (Long)completableFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                Long time = endTime -start;
                log.debug("和【{}】服务器的响应时间是：------》【{}】",entry.getKey(),time );
            }
        }
    }
}
