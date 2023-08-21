package com.liujixue;

import com.liujixue.utils.NetUtils;
import com.liujixue.utils.zookeeper.ZookeeperUtils;
import com.liujixue.utils.zookeeper.ZookeeperNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

import static com.liujixue.Constant.BASE_PROVIDERS_PATH;

/**
 * @Author LiuJixue
 * @Date 2023/8/18 17:16
 * @PackageName:com.liujixue
 * @ClassName: RpcBootStrap
 */
@Slf4j
public class RpcBootstrap {

    // RpcBootStrap 是个单例，我们希望没个应用程序只有一个实例
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();
    // 定义相关的基础配置
    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port =8088;
    // 维护一个zookeeper实例
    private ZooKeeper zooKeeper;

    // 私有化构造器
    public RpcBootstrap() {
        // 构造启动引导程序时,需要做一些初始化的事
    }

    /**
     * ----------------------- 服务提供方的相关api----------------------
     */
    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

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
     * @return this当前实例
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        // TODO 这里维护一个 zookeeper 实例，如果这样写就会将zookeeper和当前工程耦合，我们希望以后可以扩展更多不同的实现
        zooKeeper = ZookeeperUtils.createZookeeper();
        this.registryConfig = registryConfig;
        return this;
    }

//    public RpcBootstrap registry(Registry registry) {
//    }

    /**
     * 配置当前暴露的服务使用的协议
     *
     * @param protocolConfig 协议的封装
     * @return this当前实例
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了：【{}】,进行序列化",protocolConfig.toString());
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
        // 服务名称的节点，为持久节点
        String parentNode = BASE_PROVIDERS_PATH + "/" +  service.getInterface().getName();
        if(!ZookeeperUtils.exists(parentNode,zooKeeper,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode,null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }
        // 创建本机的临时节点，ip:port 服务提供方的端口一般自己设定，我们还需要一个获取ip的方法
        // ip通常需要一个局域网ip，不是127.0.0.1，也不是ipv6
        String node = parentNode +  "/" + NetUtils.getIp() + ":" + port;
        if(!ZookeeperUtils.exists(node,zooKeeper,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(node,null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }
        if (log.isDebugEnabled()) {
            log.debug("服务:【{}】,已经被注册",service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布
     *
     * @param list 独立封装需要的发布的服务
     * @return this当前实例
     */
    public RpcBootstrap publish(List<?> list) {
        return this;
    }

    /**
     * 启动 netty 服务
     */
    public void start() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ----------------------- 服务调用方的相关api----------------------
     */

    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        // 在方法里是是否可以拿到相关的配置项：注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        return this;
    }
}
