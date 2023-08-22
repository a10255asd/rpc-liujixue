package com.liujixue.discovery;

import com.liujixue.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * @Author LiuJixue
 * @Date 2023/8/22 08:33
 * @PackageName:com.liujixue.discovery
 * @ClassName: Registry
 * @Description: 注册中心
 */
public interface Registry {
    /**
     * 注册服务
     * @param serviceConfig 配置内容
     */
    void register(ServiceConfig<?> serviceConfig);
    /**
     * 从注册中心拉取一个可用的服务
     * @param serviceName 服务的名称
     * @return 服务的地址
     */
    InetSocketAddress lookup(String serviceName);
}
