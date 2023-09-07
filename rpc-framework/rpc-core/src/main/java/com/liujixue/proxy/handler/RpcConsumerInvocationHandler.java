package com.liujixue.proxy.handler;

import com.liujixue.IdGenerator;
import com.liujixue.NettyBootstrapInitializer;
import com.liujixue.RpcBootstrap;
import com.liujixue.annotation.TryTimes;
import com.liujixue.compress.CompressorFactory;
import com.liujixue.discovery.Registry;
import com.liujixue.enumeration.RequestType;
import com.liujixue.exceptions.DiscoveryException;
import com.liujixue.exceptions.NetworkException;
import com.liujixue.protection.CircuitBreaker;
import com.liujixue.serialize.SerializerFactory;
import com.liujixue.transport.message.RequestPayload;
import com.liujixue.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
    private String group;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef,String group) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
        this.group = group;
    }

    /**
     * 所有得放啊调用本质都会走到这里
     *
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@code Method} instance corresponding to
     *               the interface method invoked on the proxy instance.  The declaring
     *               class of the {@code Method} object will be the interface that
     *               the method was declared in, which may be a superinterface of the
     *               proxy interface that the proxy class inherits the method through.
     * @param args   an array of objects containing the values of the
     *               arguments passed in the method invocation on the proxy instance,
     *               or {@code null} if interface method takes no arguments.
     *               Arguments of primitive types are wrapped in instances of the
     *               appropriate primitive wrapper class, such as
     *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 代表不重试
        int tryTimes = 0;
        int intervalTime = 0;
        // 从接口中判断是否需要重试
        TryTimes tryTimesAnnotation = method.getAnnotation(TryTimes.class);
        if (tryTimesAnnotation != null) {
            tryTimes = tryTimesAnnotation.tryTimes();
            intervalTime = tryTimesAnnotation.intervalTime();
        }
        while (true) {
            // 什么情况下需要重试 1. 本身发生异常 2. 响应有问题 code == 500

            // ------------------封装报文-----------------
            // 建造者设计模式
            RequestPayload requestPayload = RequestPayload
                    .builder()
                    .interfaceName(interfaceRef.getName())
                    .methodName(method.getName())
                    .parametersType(method.getParameterTypes())
                    .parametersValue(args).returnType(method.getReturnType())
                    .build();
            // 对 请求id 和各种类型做处理
            RpcRequest rpcRequest = RpcRequest.builder()
                    .requestId(RpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                    .compressType(CompressorFactory.getCompressor(RpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                    .requestType(RequestType.REQUEST.getId())
                    .serializeType(SerializerFactory.getSerializer(RpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                    .timeStamp(System.currentTimeMillis())
                    .requestPayload(requestPayload)
                    .build();
            // 将请求存入本地线程，需要在合适的时候调用 remove 方法
            RpcBootstrap.REQUEST_THREAD_LOCAL.set(rpcRequest);
            // 2. 从注册中心拉取服务列表，并通过客户端负载均衡寻找一个可用的服务
            InetSocketAddress address = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer().selectServerAddress(interfaceRef.getName(),group);
            if (log.isDebugEnabled()) {
                log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
            }
            // 获取当前地址所对应的断路器
            Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = RpcBootstrap.getInstance().getConfiguration().getEveryIpCircuitBreaker();
            CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(address);
            if(circuitBreaker == null){
                circuitBreaker = new CircuitBreaker(10,0.5F);
                everyIpCircuitBreaker.put(address,circuitBreaker);
            }

            try {
                // 如果断路器是打开的，直接返回
                if (rpcRequest.getRequestType()!= RequestType.HEARTBEAT.getId() && circuitBreaker.isBreak()) {
                    // 定期打开
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            RpcBootstrap.getInstance()
                                    .getConfiguration().getEveryIpCircuitBreaker()
                                    .get(address).reset();
                        }
                    }, 5000);
                    throw new RuntimeException("当前断路器已经开启，无法发送请求");
                }
                // 3. 尝试获取一个可用通道
                Channel channel = getAvailableChannel(address);
                if (log.isDebugEnabled()) {
                    log.debug("获取了和【{}】建立的连接通道,准备发送数据", address);
                }
                // 4. 写出报文
                // ---------------异步策略------------
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                // 将completableFuture 挂起且暴露，并且得到服务提供方相应的时候调用 complete 方法
                RpcBootstrap.PADDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);
                // 这里直接 writeAndFlush 写出一个请求，这个请求的实例就会进入 pipeline ，pipeline执行出栈的一系列操作
                // 事实上，第一个pipeline一定是将 rpcRequest 这个请求对象转换为二进制的报文
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
                    // 只需要处理异常就行
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });
                // 如果没有地方处理 completableFuture 这里会阻塞，等待 complete 方法的执行
                // 5. 清理ThreadLocal
                RpcBootstrap.REQUEST_THREAD_LOCAL.remove();
                // 6. 获得结果
                Object result = completableFuture.get(10, TimeUnit.SECONDS);
                // 记录成功的请求
                circuitBreaker.recordRequest();
                return result;
            } catch (Exception e) {
                // 次数减一，并且等待固定时间。固定时间有一定的问题，可能引起重试风暴
                tryTimes--;
                // 记录错误的次数
                circuitBreaker.recordErrorRequest();
                try {
                    Thread.sleep(intervalTime);
                } catch (InterruptedException ex) {
                    log.error("在进行重试时发生异常【{}】", ex);
                }
                if (tryTimes < 0) {
                    log.error("对方法【{}】进行远程调用时，重试【{}】次，依然不可调用", method.getName(), tryTimes, e);
                    break;
                }
                log.error("进行第【{}】次重试时发生异常", 3 - tryTimes, e);
            }
        }
        throw new RuntimeException("执行远程方法" + method + "调用失败");
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
