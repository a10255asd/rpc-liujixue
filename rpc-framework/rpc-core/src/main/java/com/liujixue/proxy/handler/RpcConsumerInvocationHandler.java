package com.liujixue.proxy.handler;

import com.liujixue.IdGenerator;
import com.liujixue.NettyBootstrapInitializer;
import com.liujixue.RpcBootstrap;
import com.liujixue.compress.CompressorFactory;
import com.liujixue.discovery.Registry;
import com.liujixue.enumeration.RequestType;
import com.liujixue.exceptions.DiscoveryException;
import com.liujixue.exceptions.NetworkException;
import com.liujixue.serialize.SerializerFactory;
import com.liujixue.transport.message.RequestPayload;
import com.liujixue.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author LiuJixue
 * @Date 2023/8/23 14:00
 * @PackageName:com.liujixue.proxy.handler
 * @ClassName: RpcConsumerInvocationHandler
 * @Description: 封装了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在 invoke 方法中
 * 1. 发现可用服务
 * 2. 建立连接
 * 3. 发送请求
 * 4. 得到结果
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    // 此处需要一个注册中心，和一个接口
    private final Registry registry;
    private final Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 获取当前配置的负载均衡器，选取一个可用节点
        InetSocketAddress address = RpcBootstrap.LOAD_BALANCER.selectServerAddress(interfaceRef.getName());
        if (log.isDebugEnabled()) {
            log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
        }
        // 2. 尝试获取一个可用通道
        Channel channel = getAvailableChannel(address);
        if (log.isDebugEnabled()) {
            log.debug("获取了和【{}】建立的连接通道,准备发送数据", address);
        }
        // TODO ------------------封装报文-----------------
        // 建造者设计模式
        RequestPayload requestPayload = RequestPayload
                .builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args).returnType(method.getReturnType())
                .build();
        // TODO 对 请求id 和各种类型做处理
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(RpcBootstrap.ID_GENERATOR.getId())
                .compressType(CompressorFactory.getCompressor(RpcBootstrap.COMPRESS_TYPE).getCode())
                .requestType(RequestType.REQUEST.getId())
                .serializeType(SerializerFactory.getSerializer(RpcBootstrap.SERIALIZE_TYPE).getCode())
                .requestPayload(requestPayload)
                .build();
        // 4. 写出报文
        // ---------------异步策略------------
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        // 将completableFuture 挂起且暴露，并且得到服务提供方相应的时候调用 complete 方法
        RpcBootstrap.PADDING_REQUEST.put(1L, completableFuture);
        // 这里直接 writeAndFlush 写出一个请求，这个请求的实例就会进入 pipeline ，pipeline执行出栈的一系列操作
        // 事实上，第一个pipeline一定是将 rpcRequest 这个请求对象转换为二进制的报文
        channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
            // 只需要处理异常就行
            if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
            }
        });
        // 如果没有地方处理 completableFuture 这里会阻塞，等待 complete 方法的执行
        // 5. 获得结果
        return completableFuture.get(10, TimeUnit.SECONDS);
    }

    /**
     * 根据地址获取一个可用的通道
     *
     * @param address InetSocketAddress 类型的地址
     * @return channel 通道
     */
    private static Channel getAvailableChannel(InetSocketAddress address) {
        // 1. 尝试从缓存中获取一个 channel
        Channel channel = RpcBootstrap.CHANNEL_CACHE.get(address);
        // 2. 拿不到建立连接，存入缓存
        if (channel == null) {
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                if (promise.isDone()) {
                    // 使用 addListener 执行的异步的操作
                    if (!log.isDebugEnabled()) {
                        log.debug("已经和【{}】建立了连接", address);
                    }
                    channelFuture.complete(promise.channel());
                } else if (promise.isSuccess()) {
                    channelFuture.completeExceptionally(promise.cause());
                }
            });
            // 阻塞获取 channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时发生异常，{}", e);
                throw new DiscoveryException(e);
            }
            // 缓存 channel 本身
            RpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        if (channel == null) {
            log.error("获取或建立与【{}】的通道时发生异常", address);
            throw new NetworkException("获取通道时发生异常");
        }
        return channel;
    }
}
