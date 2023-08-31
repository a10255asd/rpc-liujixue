package com.liujixue;

import com.liujixue.channelHandler.handler.MethodCallHandler;
import com.liujixue.channelHandler.handler.RpcRequestDecoder;
import com.liujixue.channelHandler.handler.RpcResponseEncoder;
import com.liujixue.core.HeartbeatDetector;
import com.liujixue.discovery.Registry;
import com.liujixue.discovery.RegistryConfig;
import com.liujixue.loadbalancer.LoadBalancer;
import com.liujixue.loadbalancer.impl.ConsistentHashLoadBalancer;
import com.liujixue.loadbalancer.impl.MinimumResponseTimeLoadBalancer;
import com.liujixue.loadbalancer.impl.RoundRobinLoadBalancer;
import com.liujixue.transport.message.RpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Author LiuJixue
 * @Date 2023/8/18 17:16
 * @PackageName:com.liujixue
 * @ClassName: RpcBootStrap
 */
@Slf4j
public class RpcBootstrap {


    public static final int PORT = 8089;
    // RpcBootStrap 是个单例，我们希望没个应用程序只有一个实例
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();
    // 定义相关的基础配置
    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    public static String SERIALIZE_TYPE = "jdk";

    public static String COMPRESS_TYPE = "gzip";
    public static final ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    // 端口
    public static final IdGenerator ID_GENERATOR = new IdGenerator(1,2);
    // 注册中心
    private Registry registry;
    public static  LoadBalancer LOAD_BALANCER;
    // 连接的缓存，使用InetSocketAddress做key一定要看有没有重写equals和toString
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    public final static TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();
    // 维护已经发布且暴露的服务列表，映射关系：key--> interface的全限定名称，value--->serviceConfig
    public static final Map<String, ServiceConfig<?>> SERVICES_LIST = new ConcurrentHashMap<>(16);
    // 定义全局的对外挂起的 CompletableFuture
    public final static Map<Long, CompletableFuture<Object>> PADDING_REQUEST = new ConcurrentHashMap<>(8);

    // 私有化构造器
    public RpcBootstrap() {
        // 构造启动引导程序时,需要做一些初始化的事
    }

    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    /**
     * ----------------------- 服务提供方的相关api----------------------
     */

    /**
     * 用来定义当前应用的名字
     *
     * @return this
     */
    public RpcBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @return this 当前实例
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        // 尝试使用获取一个注册中心，类似于工厂设计模式
        this.registry = registryConfig.getRegistry();
        // TODO
        // RpcBootstrap.LOAD_BALANCER = new RoundRobinLoadBalancer();
        RpcBootstrap.LOAD_BALANCER = new MinimumResponseTimeLoadBalancer();
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     *
     * @param protocolConfig 协议的封装
     * @return this当前实例
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了：【{}】,进行序列化", protocolConfig.toString());
        }
        return this;
    }

    /**
     * 发布服务,将接口--> 实现，注册到服务中心
     *
     * @param service 独立封装需要的发布的服务
     * @return this当前实例
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        // 抽象注册中心，使用注册中心的一个实现完成注册
        registry.register(service);
        // 1. 服务调用方通过接口方法名，以及具体的方法参数列表发起调用，提供方怎么知道使用哪一个实现
        // 方式一：new()
        // 方式二：spring beanFactory.getBean(Class)
        // 方式三：尝试自己维护映射关系
        SERVICES_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布
     *
     * @param services 独立封装需要的发布的服务
     * @return this当前实例
     */
    public RpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动 netty 服务
     */
    public void start() {
        //1. 创建 EventLoopGroup
        // boss 只负责处理请求，之后会将请求分发给worker
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            //2. 需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 3. 配置服务器
            serverBootstrap
                    .channel(NioServerSocketChannel.class)
                    .group(boss, worker)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new RpcRequestDecoder())
                                    // 根据请求进行方法调用
                                    .addLast(new MethodCallHandler())
                                    // 响应编码
                                    .addLast(new RpcResponseEncoder());
                        }
                    });
            // 4. 绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(PORT);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ----------------------- 服务调用方的相关api----------------------
     */

    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        // 开启对这个服务的心跳检测
        HeartbeatDetector.detectHeartbeat(reference.getInterface().getName());
        // 在方法里是是否可以拿到相关的配置项：注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        // 1. reference 需要一个注册中心
        reference.setRegistry(registry);
        return this;
    }

    /**
     * 配置序列化的方式
     * @param serializeType
     * @return
     */
    public RpcBootstrap serialize(String serializeType) {
        SERIALIZE_TYPE = serializeType;
        if(log.isDebugEnabled()){
            log.debug("我们配置了使用的序列化的方法为【{}】",serializeType);
        }
        return this;
    }

    public RpcBootstrap compress(String compressType) {
        COMPRESS_TYPE = compressType;
        if(log.isDebugEnabled()){
            log.debug("我们配置了使用的解压缩的方法为【{}】",compressType);
        }
        return this;
    }

    public Registry getRegistry() {
        return registry;
    }
}
