package com.liujixue.config;

import com.liujixue.loadbalancer.LoadBalancer;
import com.liujixue.spi.SpiHandler;

/**
 * @Author LiuJixue
 * @Date 2023/9/5 16:13
 * @PackageName:com.liujixue.config
 * @ClassName: SpiResolver
 * @Description: TODO
 */
public class SpiResolver {
    /**
     * 通过spi的方式加载配置项
     * @param configuration 配置上下文
     */
    public void loadFromSpi(Configuration configuration) {
        // spi文件中配置了很多实现（自由定义，只能配置一个实现还是多个）
        LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);
        //configuration.setLoadBalancer();
    }
}
