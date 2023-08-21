package com.liujixue;

import com.liujixue.utils.zookeeper.ZookeeperUtils;
import com.liujixue.utils.zookeeper.ZookeeperNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 11:00
 * @PackageName:com.liujixue
 * @ClassName: Application
 * @Description: zookeeper集群注册中心的管理页面
 */
@Slf4j
public class Application {

    public static void main(String[] args) {

        //  创建一个zookeeper实例
        ZooKeeper zookeeper = ZookeeperUtils.createZookeeper();
        // 定义节点和数据
        String basePath = "/liujixue-rpc-metadata";
        String providerPath = basePath + "/provider";
        String consumersPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode("/liujixue-rpc-metadata", null);
        ZookeeperNode providerNode = new ZookeeperNode(providerPath, null);
        ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, null);
        // 创建节点
        List.of(baseNode, providerNode, consumersNode).forEach(node -> {
            ZookeeperUtils.createNode(zookeeper, node, null, CreateMode.PERSISTENT);
        });
        // 关闭连接
        ZookeeperUtils.close(zookeeper);
    }
}
