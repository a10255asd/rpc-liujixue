package com.liujixue.loadbalancer.impl;

import com.liujixue.RpcBootstrap;
import com.liujixue.exceptions.LoadBalancerException;
import com.liujixue.loadbalancer.AbstractLoadBalancer;
import com.liujixue.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }
    private static class MinimumResponseTimeSelector implements Selector {


        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = RpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry !=null) {
                return (InetSocketAddress)entry.getValue().remoteAddress();
            }
            // 直接从缓存中获取一个可用的就行了
            Channel channel = (Channel)RpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress)channel.remoteAddress();
        }

        @Override
        public void reBalance() {

        }
    }
}
