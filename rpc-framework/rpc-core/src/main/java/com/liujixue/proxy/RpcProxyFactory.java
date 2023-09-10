package com.liujixue.proxy;

import com.liujixue.ReferenceConfig;
import com.liujixue.RpcBootstrap;
import com.liujixue.discovery.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixue
 * @Date 2023/9/10 17:35
 * @ClassName: ProxyFactory
 */
public class RpcProxyFactory {
    private static Map<Class<?>,Object> cache = new ConcurrentHashMap<>(32);
    public static <T> T getProxy(Class<T> clazz){
        Object bean = cache.get(clazz);
        if(bean !=null){
            return (T) bean;
        }
        // 想尽一切办法获取代理对象, 使用 ReferenceConfig 进行封装
        // reference 一定有生成代理的模版方法（get()方法）
        ReferenceConfig<T> reference = new ReferenceConfig();
        reference.setInterface(clazz);
        // 代理做了些什么：1、连接注册中心 2、拉取服务列表 3、选择服务并建立连接 4、发送请求，携带一些信息(接口名，参数列表，方法名字)
        RpcBootstrap.getInstance()
                .application("first-rpc-consumer")
                .registry(new RegistryConfig("zookeeper://101.42.50.241:2181"))
                .serialize("jdk")
                .compress("gzip")
                .group("primary")
                .reference(reference);
        System.out.println("------------------------------------------------------------");
        // 获取一个代理对象
        T t = reference.get();
        cache.put(clazz,t);
        return t;
    }
}
