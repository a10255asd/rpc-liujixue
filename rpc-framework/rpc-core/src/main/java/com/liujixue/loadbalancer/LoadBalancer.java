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
}
