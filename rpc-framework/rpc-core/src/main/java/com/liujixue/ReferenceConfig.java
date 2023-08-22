package com.liujixue;

import com.liujixue.discovery.Registry;
import com.liujixue.exceptions.NetworkException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 09:59
 * @PackageName:com.liujixue
 * @ClassName: ReferenceConfig
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    @Getter
    private Registry registry;

    /**
     * 代理设计模式，生成一个api接口的代理对象，返回一个代理对象
     *
     * @return
     */
    public T get() {
        // 动态代理
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        // 使用动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 调用 sayHi 方法，会走到这
                // 已知 method，args[]
                log.info("method------>{}", method.getName());
                log.info("args------>{}", args);
                // 1. 发现服务，注册中心，寻找一个可用的服务
                // 传入服务的名字,返回 ip + 端口
                // TODO：1、每次调用方法都需要去注册中心拉取服务列表吗？ 2、如何合理的选择一个可用的服务，而不是只获取第一个
                InetSocketAddress address = registry.lookup(interfaceRef.getName());
                if (log.isDebugEnabled()) {
                    log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
                }
                // 2. 使用netty连接服务器，发送调用的 服务的名字 + 方法名字 + 参数列表 得到结果
                // 启动一个客户端需要一个辅助类，bootstrap
                // TODO：整个连接过程放在这里意味着每次调用都会建立一个新的netty连接。需要考虑如何缓存连接
                // TODO：意味着建立一个新的连接是不合适的。
                // TODO：解决方案：缓存 channel , 尝试从缓存中获取channel，如果未获取则创建新的连接，并进行缓存
                // 1. 尝试从全局缓存中获取一个通道
                Channel channel = RpcBootstrap.CHANNEL_CACHE.get(address);
                if (channel == null) {
                    // await 会等待连接成功再返回，netty还提供了异步处理的逻辑
                    // sync()和await()都会阻塞当前县城，获取返回值（连接的过程是异步的，发送数据的过程是异步的）
                    // 如果发生了异常 sync 会主动再主线程抛出异常，await的异常在子线程中处理，需要使用future。
//                    channel = NettyBootstrapInitializer
//                            .getBootstrap()
//                            .connect(address)
//                            .await()
//                            .channel();
                    NettyBootstrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                        if (promise.isDone()) {
                            // 异步的，
                        }
                    });
                    // 如何等待
                    // 缓存 channel 本身

                    RpcBootstrap.CHANNEL_CACHE.put(address, channel).;
                }
                if (channel == null) {
                    throw new NetworkException("获取通道时发生异常");
                }
                /* ---------------同步策略------------
                ChannelFuture channelFuture = channel.writeAndFlush(new Object());
                // get 阻塞获取结果，getNow 获取当前的结果，如果未处理完成返回null
                if(channelFuture.isDone()){
                    Object object = channelFuture.getNow();
                } else if(!channelFuture.isSuccess()){
                    // 需要捕获异常,子线程可以捕获异步任务中的异常
                    throw new RuntimeException(channelFuture.cause());
                }
                */
                // ---------------异步策略------------
                channel.writeAndFlush(new Object()).addListener((ChannelFutureListener) promise -> {
                    promise.isDone()
                });
                return null;
            }
        });
        return (T) helloProxy;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
