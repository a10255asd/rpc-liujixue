package com.liujixue.config;

import com.liujixue.compress.Compressor;
import com.liujixue.compress.CompressorFactory;
import com.liujixue.loadbalancer.LoadBalancer;
import com.liujixue.serialize.Serializer;
import com.liujixue.serialize.SerializerFactory;
import com.liujixue.spi.SpiHandler;

import java.util.List;

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
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        // 将其放入工厂
        if(loadBalancerWrappers != null && loadBalancerWrappers.size()>0){
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> compressorObjectWrappers = SpiHandler.getList(Compressor.class);
        if (compressorObjectWrappers!=null){
            compressorObjectWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerObjectWrappers = SpiHandler.getList(Serializer.class);
        if (serializerObjectWrappers!=null){
            serializerObjectWrappers.forEach(SerializerFactory::addSerializer);
        }

    }
}
