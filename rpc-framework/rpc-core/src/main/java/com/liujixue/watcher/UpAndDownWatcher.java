package com.liujixue.watcher;

import com.liujixue.NettyBootstrapInitializer;
import com.liujixue.RpcBootstrap;
import com.liujixue.discovery.Registry;
import com.liujixue.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @Author LiuJixue
 * @Date 2023/9/1 17:03
 * @PackageName:com.liujixue.watcher
 * @ClassName: UpAndDownWatcher
 * @Description: 动态感知节点上下线的watcher
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        // 判断当前的节点是否发生了变化
        if(event.getType() == Event.EventType.NodeChildrenChanged){
            if(log.isDebugEnabled()){
                log.debug("检测服务【{}】到有节点上/下线，将重新拉取服务列表...",event.getPath());
            }
            // 重新拉取服务列表
            String serviceName = getServiceName(event.getPath());
            Registry registry = RpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
            List<InetSocketAddress> addresses = registry.lookup(serviceName,
                    RpcBootstrap.getInstance().getConfiguration().getGroup());
            // 处理新增的节点
            for (InetSocketAddress address : addresses) {
                // 新增的节点会在 addresses 中，不再 cache 中
                // 下线的节点可能在 cache中，不再 addresses 中
                if (!RpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    // 根据地址建立连接，并且缓存
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    RpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }
            // 处理下线的节点
            for (Map.Entry<InetSocketAddress,Channel> entry :RpcBootstrap.CHANNEL_CACHE.entrySet()){
                if(!addresses.contains(entry.getKey())){
                    RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }
            // 获得负载均衡器，进行重新的loadBalance
            LoadBalancer loadBalancer = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reloadBalance(serviceName,addresses);
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length -1];
    }
}
