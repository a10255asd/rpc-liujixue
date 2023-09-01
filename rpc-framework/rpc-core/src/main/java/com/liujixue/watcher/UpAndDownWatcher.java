package com.liujixue.watcher;

import com.liujixue.NettyBootstrapInitializer;
import com.liujixue.RpcBootstrap;
import com.liujixue.discovery.Registry;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;

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
        if(event.getType() == Event.EventType.NodeChildrenChanged){
            if(log.isDebugEnabled()){
                log.debug("检测服务【{}】到有节点上/下线，将重新拉取服务列表...",event.getPath());
            }
            // 重新拉取服务列表
            String ServiceName = getServiceName(event.getPath());
            Registry registry = RpcBootstrap.getInstance().getRegistry();
            List<InetSocketAddress> addresses = registry.lookup(ServiceName);
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
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length -1];
    }
}
