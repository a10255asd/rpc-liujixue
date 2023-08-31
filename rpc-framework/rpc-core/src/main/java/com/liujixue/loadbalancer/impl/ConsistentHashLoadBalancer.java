package com.liujixue.loadbalancer.impl;

import com.liujixue.RpcBootstrap;
import com.liujixue.exceptions.LoadBalancerException;
import com.liujixue.loadbalancer.AbstractLoadBalancer;
import com.liujixue.loadbalancer.Selector;
import com.liujixue.transport.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 一致性哈希算法
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentSelector(serviceList,128);
    }

    /**
     * 一致性hash的具体算法实现
     */
    private static class ConsistentSelector implements Selector {
        // hash 环，用来存储服务器节点
        private SortedMap<Integer,InetSocketAddress> circle = new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;


        public ConsistentSelector(List<InetSocketAddress> serviceList,int virtualNodes) {
            // 我们应该尝试将节点转化为虚拟节点进行挂在
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : serviceList) {
                // 需要把每一个节点加入到 hash 环中
                addNodeToCircle(inetSocketAddress);
            }
        }

        /**
         * 将每个节点挂在到 hash环 上
         * @param inetSocketAddress 节点的地址
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i <virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 挂载到hash环上
                circle.put(hash,inetSocketAddress);
                if(log.isDebugEnabled()){
                    log.debug("hash为【{}】的节点已经挂载到了hash环上",hash);
                }
            }
        }
        /**
         * 将节点从 hash环 上移除
         * @param inetSocketAddress 节点的地址
         */
        private void removeNodeToCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i <virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 挂载到hash环上
                circle.remove(hash,inetSocketAddress);

            }
        }

        /**
         * 具体的hash算法 TODO 这样也是不均匀的
         * @param s
         * @return
         */
        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());
            // 通过 md5 的到的结果是一个字节数组，但是我们想要一个int，4个字节
            int res = 0;
            for (int i = 0; i < 4; i++) {
                if(digest[i] < 0){
                    res = res | (digest[i] & 255);
                }else {
                    res = res << 8;
                    res = res | digest[i];
                }

            }
            return res;
        }

        @Override
        public InetSocketAddress getNext() {
            // 1、hash已经建立好了，接下来需要对请求的要素做处理
            // 使用 threadLocal
            RpcRequest rpcRequest = RpcBootstrap.REQUEST_THREAD_LOCAL.get();
            // 根据请求的特征来选择服务器
            String requestId = Long.toString(rpcRequest.getRequestId());
            // 对请求的 id 做 hash，字符串默认的 hash 不太好
            int hash = hash(requestId);
            // 判断该 hash 值是否能直接落在一个服务器上，和服务器的hash一样
            if (!circle.containsKey(hash)) {
                // 寻找最近的节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash =  tailMap.isEmpty()?circle.firstKey():tailMap.firstKey();
            }
            return circle.get(hash);
        }

        @Override
        public void reBalance() {

        }

        private String toBinary(int i){
            String binaryString = Integer.toBinaryString(i);
            int index = 32 - binaryString.length();
            StringBuffer sb =new StringBuffer();
            for (int j = 0; j < index; j++) {
                sb.append(0);
            }
            sb.append(binaryString);
            return sb.toString();
        }
    }
}
