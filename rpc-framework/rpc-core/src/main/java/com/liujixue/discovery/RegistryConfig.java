package com.liujixue.discovery;

import com.liujixue.discovery.impl.NacosRegistry;
import com.liujixue.discovery.impl.ZookeeperRegistry;
import com.liujixue.exceptions.DiscoveryException;
import io.netty.util.Constant;

import static com.liujixue.Constant.TIME_OUT;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 09:01
 * @PackageName:com.liujixue
 * @ClassName: RegistryConfig
 * @Description: TODO
 */
public class RegistryConfig {
    // 定义连接的 url zookeeper://127.0.0.1:2181,redis://127.0.0.1:6379
    private final String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }
    /**
     * 可以使用简单工厂来完成
     * @return 返回具体注册中心实例
     */
    public Registry getRegistry() {
        // 1. 获取注册中心的类型
        String registryType = getRegistryType(connectString,true).toUpperCase().trim();
        if (registryType.equals("ZOOKEEPER")){
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host, TIME_OUT);
        }else if (registryType.equals("NACOS")){
            String host = getRegistryType(connectString, false);
            return new NacosRegistry(host, TIME_OUT);
        }
        throw new DiscoveryException("未发现合适的注册中心");
    }
    private String getRegistryType(String connectString,boolean ifType){
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length!=2){
            throw new RuntimeException("给定的注册中心连接url不合法！");
        }
        if (ifType) {
            return typeAndHost[0];
        }else {
            return typeAndHost[1];
        }

    }
}
