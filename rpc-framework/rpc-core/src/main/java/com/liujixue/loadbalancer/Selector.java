package com.liujixue.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

public interface Selector {
    /**
     * 根据服务列表执行一种算法，获取一个服务节点
     * @return 具体的服务节点
     */
    InetSocketAddress getNext();


    // TODO 服务动态上下线需要进行 reBalance
    void reBalance();
}
