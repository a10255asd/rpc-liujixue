package com.liujixue;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 11:09
 * @PackageName:com.liujixue
 * @ClassName: Constant
 * @Description: TODO
 */
public class Constant {
    // zookeeper默认连接地址
    public static final String DEFAULT_ZK_CONNECT = "101.42.50.241:2181";
    // zookeeper默认连接超时事件
    public static final int TIME_OUT = 10000;
    // 服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PROVIDERS_PATH = "/rpc-metadata/providers";
    public static final String BASE_CONSUMERS_PATH = "/rpc-metadata/consumers";
}
