package com.liujixue;

/**
 * @Author LiuJixue
 * @Date 2023/8/18 16:38
 * @PackageName:com.liujixue.impl.com.liujixue
 * @ClassName: Application
 * @Description: TODO
 */
public class Application {
    public static void main(String[] args) {
        // 服务提供方，需要注册服务，启动服务
        // 1. 封装要发布的服务
        // 2. 定义注册中心
        // 3. 通过启动引导程序，启动服务提供方
        // 3.1. 配置: 应用的名称，注册中心，序列化协议，压缩方式
        // 3.2. 发布
        RpcBootstrap.getInstance()
                .application("first-rpc-provider")
                // 配置注册中心
                .registry()
                .protocal()
                // 发布服务
                .publish()
                // 启动服务
                .start();
    }
}
