package com.liujixue.discovery.impl;

import com.liujixue.RpcBootstrap;
import com.liujixue.ServiceConfig;
import com.liujixue.discovery.AbstractRegistry;
import com.liujixue.exceptions.DiscoveryException;
import com.liujixue.utils.NetUtils;
import com.liujixue.utils.zookeeper.ZookeeperNode;
import com.liujixue.utils.zookeeper.ZookeeperUtils;
import com.liujixue.watcher.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import java.net.InetSocketAddress;
import java.util.List;
import static com.liujixue.Constant.BASE_PROVIDERS_PATH;

/**
 * @Author LiuJixue
 * @Date 2023/8/22 08:37
 * @PackageName:com.liujixue.discovery.impl
 * @ClassName: ZookeeperRegistry
 * @Description: TODO
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    // 维护一个zk实例
    private ZooKeeper zooKeeper;
    public ZookeeperRegistry() {
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public ZookeeperRegistry(String connectString,int timeout) {
        this.zooKeeper = ZookeeperUtils.createZookeeper(connectString,timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        // 服务名称的节点，为持久节点
        String parentNode = BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();
        if (!ZookeeperUtils.exists(parentNode, zooKeeper, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }
        // 创建本机的临时节点，ip:port 服务提供方的端口一般自己设定，我们还需要一个获取ip的方法
        // ip通常需要一个局域网ip，不是127.0.0.1，也不是ipv6
        // TODO: 端口后续处理
        String node = parentNode + "/" + NetUtils.getIp() + ":" + RpcBootstrap.PORT;
        if (!ZookeeperUtils.exists(node, zooKeeper, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }
        if (log.isDebugEnabled()) {
            log.debug("服务:【{}】,已经被注册", service.getInterface().getName());
        }
    }

    /**
     * 注册中心的目的是什么？ 拉取合适服务列表
     * @param serviceName 服务的名称
     * @return 服务列表
     */
    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        // 1. 找到服务对应的节点
        String serviceNode = BASE_PROVIDERS_PATH + "/" + serviceName;
        // 2. 从zk中获取他的子节点
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher() {

        });
        // 获取了所有的可用的服务列表
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.valueOf(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).toList();
        if(inetSocketAddresses.size() == 0){
            throw new DiscoveryException("未发现任何可用的服务主机");
        }
        return inetSocketAddresses;
    }
}
