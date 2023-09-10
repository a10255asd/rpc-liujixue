package com.liujixue;

import com.liujixue.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @Author LiuJixue
 * @Date 2023/9/10 16:55
 * @ClassName: RpcStarter
 */
@Component
@Slf4j
public class RpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.debug("rpc 开始启动");
        RpcBootstrap.getInstance()
                .application("first-rpc-provider")
                // 配置注册中心
                .registry(new RegistryConfig("zookeeper://101.42.50.241:2181"))
                .serialize("jdk")
                .scan("com.liujixue.impl")
                // 启动服务
                .start();
    }
}
