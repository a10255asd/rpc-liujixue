package com.liujixue;

import com.liujixue.annotation.RpcApi;
import com.liujixue.channelHandler.handler.MethodCallHandler;
import com.liujixue.channelHandler.handler.RpcRequestDecoder;
import com.liujixue.channelHandler.handler.RpcResponseEncoder;
import com.liujixue.core.HeartbeatDetector;
import com.liujixue.discovery.RegistryConfig;
import com.liujixue.loadbalancer.LoadBalancer;
import com.liujixue.transport.message.RpcRequest;
import com.liujixue.config.Configuration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @Author LiuJixue
 * @Date 2023/8/18 17:16
 * @PackageName:com.liujixue
 * @ClassName: RpcBootStrap
 */
@Slf4j
public class RpcBootstrap {
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();
    // 全局的配置中心
    @Getter
    private Configuration configuration;
    // 保存 request 对象可以在当前线程中随时获取
    public static final ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    // 连接的缓存，使用InetSocketAddress做key一定要看有没有重写equals和toString
    public static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    public static TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();
    // 维护已经发布且暴露的服务列表，映射关系：key--> interface的全限定名称，value--->serviceConfig
    public static Map<String, ServiceConfig<?>> SERVICES_LIST = new ConcurrentHashMap<>(16);
    // 定义全局的对外挂起的 CompletableFuture
    public static Map<Long, CompletableFuture<Object>> PADDING_REQUEST = new ConcurrentHashMap<>(8);

    // 私有化构造器
    public RpcBootstrap() {
        // 构造启动引导程序时,需要做一些初始化的事
        configuration = new Configuration();
    }

    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    /**
     * ----------------------- 服务提供方的相关api----------------------
     */
    /**
     * 用来定义当前应用的名字
     * @return this
     */
    public RpcBootstrap application(String appName) {
        configuration.setAppName(appName);
        return this;
    }
    /**
     * 用来配置 注册中心
     * @return this 当前实例
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        return this;
    }
    /**
     * 用来配置 负载均衡策略
     * @return this 当前实例
     */
    public RpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }
    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议的封装
     * @return this当前实例
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
       configuration.setProtocolConfig(protocolConfig);
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了：【{}】,进行序列化", protocolConfig.toString());
        }
        return this;
    }
    /**
     * 发布服务,将接口--> 实现，注册到服务中心
     * @param service 独立封装需要的发布的服务
     * @return this当前实例
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        // 抽象注册中心，使用注册中心的一个实现完成注册
        configuration.getRegistryConfig().getRegistry().register(service);
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
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort());
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
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        return this;
    }
    /**
     * 配置序列化的方式
     * @param serializeType
     * @return
     */
    public RpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if(log.isDebugEnabled()){
            log.debug("我们配置了使用的序列化的方法为【{}】",serializeType);
        }
        return this;
    }
    /**
     * 配置压缩方式
     * @param compressType
     * @return
     */
    public RpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if(log.isDebugEnabled()){
            log.debug("我们配置了使用的解压缩的方法为【{}】",compressType);
        }
        return this;
    }

    /**
     * 扫描包进行批量注册
     * @param packageName 包名
     * @return this
     */
    public RpcBootstrap scan(String packageName) {
        // 1. 通过 packageName 获取其下的所有的类的全限定名称
        List<String> classNames = getAllClassNames(packageName);
        // 2. 通过反射获取接口，构建具体实现
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(RpcApi.class) != null)
                .collect(Collectors.toList());
        for (Class<?> clazz : classes) {
            // 获取他的接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            List<ServiceConfig<?>> serviceConfigs = new ArrayList<>();
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                serviceConfigs.add(serviceConfig);
                if(log.isDebugEnabled()){
                    log.debug("---->已经通过包扫描将服务【{}】，发布",anInterface);
                }
                publish(serviceConfig);
            }
            // 3. 进行发布 批量发布
            // publish(serviceConfigs);
        }
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        // 1. 通过 packageName 获得绝对路径
        // com.liujixue.xxx.xxx ---> /com/liujixue/xxx/xxx
        String basePath = packageName.replaceAll("\\.","\\/");
        System.out.println(basePath);
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if(url == null){
            throw new RuntimeException("包扫描时路径不存在");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath,classNames,basePath);

        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames,String basePath) {
        // 获取文件
        File file = new File(absolutePath);
        // 判断文件是否是文件夹
        if(file.isDirectory()){
            // 找到文件夹的所有的文件
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if(children == null || children.length == 0){
                return classNames;
            }
            for (File child : children) {
                if (child.isDirectory()){
                    // 递归调用
                    recursionFile(child.getAbsolutePath(),classNames,basePath);
                }else {
                    // 文件的路径转换为类的全限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(),basePath);
                    classNames.add(className);
                }
            }
        }else {
            String className = getClassNameByAbsolutePath(absolutePath,basePath);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath,String basePath) {
         String fileName = absolutePath
                 .substring(absolutePath.indexOf(basePath))
                 .replaceAll("/",".");
         fileName = fileName.substring(0,fileName.indexOf(".class"));
        System.out.println(fileName);
        return fileName;
    }

    public static void main(String[] args) {
        List<String> allClassNames = RpcBootstrap.getInstance().getAllClassNames("com.liujixue");
        System.out.println(allClassNames);
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
