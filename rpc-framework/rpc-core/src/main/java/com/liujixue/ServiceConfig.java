package com.liujixue;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 09:53
 * @PackageName:com.liujixue
 * @ClassName: ServiceConfig
 * @Description: TODO
 */
public class ServiceConfig<T> {
    private Class<T> interfaceProvider;
    private Object ref;

    public Class<T> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
