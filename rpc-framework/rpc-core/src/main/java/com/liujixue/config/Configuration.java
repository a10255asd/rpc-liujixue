package com.liujixue.config;

import com.liujixue.IdGenerator;
import com.liujixue.ProtocolConfig;
import com.liujixue.compress.Compressor;
import com.liujixue.compress.impl.GZIPCompressor;
import com.liujixue.discovery.RegistryConfig;
import com.liujixue.loadbalancer.LoadBalancer;
import com.liujixue.loadbalancer.impl.RoundRobinLoadBalancer;
import com.liujixue.protection.CircuitBreaker;
import com.liujixue.protection.RateLimiter;
import com.liujixue.protection.TokenBuketRateLimiter;
import com.liujixue.serialize.Serializer;
import com.liujixue.serialize.impl.JDKSerializer;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixue
 * @Date 2023/9/5 08:35
 * @PackageName:com.liujixue
 * @ClassName: Configuration
 * @Description: 全局配置类、上下文（配置类）
 * 代码配置 -> xml配置 -> 默认项
 */
@Data
@Slf4j
@Builder
public class Configuration {
    // 配置信息 ---> 端口号
    private int port = 8089;
    // 配置信息 ---> 应用程序的名字
    private String appName = "default";
    // 分组信息
    private String group = "default";
    // 配置信息 ---> 注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://101.42.50.241:2181");
    // 配置信息 ---> 序列化协议
    private String serializeType = "jdk";
    // 配置信息 ---> 压缩方式
    private String compressType = "gzip";
    // 配置信息 ---> ID 生成器
    private IdGenerator idGenerator = new IdGenerator(1, 2);
    // 配置信息 ---> 负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();
    // 为每一个ip配置一个限流器
    private Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);
    // 为每一个 ip 配置一个断路器
    private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);
    // 读取 xml
    public Configuration() {
        // 1.成员变量的默认配置项
        // 2.loadFromSpi(this)
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);
        // 3.读取 xml 获得上面的信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);
        // 4.编程配置项，RpcBootstrap提供
    }
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }

}
