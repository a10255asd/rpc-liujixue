package com.liujixue.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器的接口
 * 应该具备根据服务列表找到一个可用服务的功能
 */
public interface LoadBalancer {
    /**
     * 根据服务名获取一个可用的服务
     * @param serviceName 服务名称
     * @return
     */
    InetSocketAddress selectServerAddress(String serviceName);

    /**
     * 当感知节点发生了动态上下线，我们需要重新进行负载均衡
     * @param serviceName 服务的名称
     */
    void reloadBalance(String serviceName,List<InetSocketAddress> addresses);
}
