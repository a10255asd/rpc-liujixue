package com.liujixue.loadbalancer;

import com.liujixue.RpcBootstrap;
import com.liujixue.discovery.Registry;
import com.liujixue.loadbalancer.impl.RoundRobinLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalancer implements LoadBalancer{

    // 一个服务会匹配一个 selector
    private Map<String,Selector> cache = new ConcurrentHashMap<>(8);
    @Override
    public InetSocketAddress selectServerAddress(String serviceName) {
        // 1. 优先从缓存中获取一个选择器
        Selector selector = cache.get(serviceName);
        // 如果没有就需要为这个service创建一个selector，并放到缓存中
        if(selector == null){
            // 对于这个负载均衡器，内部应该维护服务列表作为缓存
            List<InetSocketAddress> serviceList = RpcBootstrap.getInstance().getRegistry().lookup(serviceName);
            // 提供一些算法 负责
            selector = getSelector(serviceList);
            // 将 selector 放入缓存当中
            cache.put(serviceName,selector);

        }
        // 获取可用节点
        return selector.getNext();
    }

    /**
     * 由子类进行扩展
     * @param serviceList
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);
}
