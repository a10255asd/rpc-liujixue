package com.liujixue;

/**
 * @Author LiuJixue
 * @Date 2023/8/18 17:16
 * @PackageName:com.liujixue
 * @ClassName: RpcBootStrap
 * @Description: TODO
 */
public class RpcBootstrap {
    // RpcBootStrap 是个单例，我们希望没个应用程序只有一个实例

    private static RpcBootstrap rpcBootstrap = new RpcBootstrap();

    // 私有化构造器
    public RpcBootstrap() {
        // 构造启动引导程序时需要做一些初始化的事
    }

    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }
    /***
     * 用来定义当前应用的名字
     * @return this
     */
    public RpcBootstrap application(String appName) {
        return this;
    }
    /***
     * 用来配置一个注册中心
     * @return this当前实例
     */
    public RpcBootstrap registry( ) {
    }
    public RpcBootstrap registry() {
    }
}
