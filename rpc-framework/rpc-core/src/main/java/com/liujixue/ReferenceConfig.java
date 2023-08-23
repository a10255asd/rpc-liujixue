package com.liujixue;

import com.liujixue.discovery.Registry;
import com.liujixue.proxy.handler.RpcConsumerInvocationHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Proxy;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 09:59
 * @PackageName:com.liujixue
 * @ClassName: ReferenceConfig
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    @Getter
    private Registry registry;
    /**
     * 代理设计模式，生成一个api接口的代理对象，返回一个代理对象
     * @return
     */
    public T get() {
        // 动态代理
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        // 使用动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new RpcConsumerInvocationHandler(registry,interfaceRef));
        return (T) helloProxy;
    }
    public Class<T> getInterface() {
        return interfaceRef;
    }
    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
