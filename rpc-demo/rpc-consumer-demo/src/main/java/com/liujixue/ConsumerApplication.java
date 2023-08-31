package com.liujixue;

import com.liujixue.core.HeartbeatDetector;
import com.liujixue.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.liujixue.RpcBootstrap.SERVICES_LIST;

/**
 * @Author LiuJixue
 * @Date 2023/8/18 16:51
 * @PackageName:com.liujixue
 * @ClassName: Application
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        // 想尽一切办法获取代理对象, 使用 ReferenceConfig 进行封装
        // reference 一定有生成代理的模版方法（get()方法）
        ReferenceConfig<HelloRpc> reference = new ReferenceConfig();
        reference.setInterface(HelloRpc.class);
        // 代理做了些什么：1、连接注册中心 2、拉取服务列表 3、选择服务并建立连接 4、发送请求，携带一些信息(接口名，参数列表，方法名字)
        RpcBootstrap.getInstance()
                .application("first-rpc-consumer")
                .registry(new RegistryConfig("zookeeper://101.42.50.241:2181"))
                .serialize("jdk")
                .compress("gzip")
                .reference(reference);
        // 获取一个代理对象
        HelloRpc helloRpc = reference.get();
        for (int i = 0; i < 10; i++) {
            String sayHi = helloRpc.sayHi("你好");
            log.info("sayHi------->{}",sayHi);
        }

        System.out.println("-----------------开始心跳检测-----------------");
        // 启动
        for (Map.Entry<String,ServiceConfig<?>> entry : SERVICES_LIST.entrySet()){
            HeartbeatDetector.detectHeartbeat(entry.getKey());
        }
    }
}
