package com.liujixue.utils.zookeeper;

import com.liujixue.Constant;
import com.liujixue.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 15:33
 * @PackageName:com.liujixue.utils
 * @ClassName: ZookeeperUtil
 * @Description: zookeeper 工具类
 */
@Slf4j
public class ZookeeperUtils {
    /**
     * 使用默认配置创建 zookeeper 实例
     */
    public static ZooKeeper createZookeeper() {
        // 定义连接参数
        String connetString = Constant.DEFAULT_ZK_CONNECT;
        // 定义超时时间
        int timeOut = Constant.TIME_OUT;
        return createZookeeper(connetString, timeOut);
    }

    public static ZooKeeper createZookeeper(String connetString, int timeOut) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            // 创建zookeeper实例，建立连接
            final ZooKeeper zooKeeper = new ZooKeeper(connetString, timeOut, event -> {
                // 只有连接成功才放
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.info("客户端连接成功！");
                    countDownLatch.countDown();
                }
            });
            // 等待连接成功
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper实例时发生异常：【{}】", e);
            throw new ZookeeperException();
        }
    }
    /**
     * 创建一个节点的工具方法
     * @param zooKeeper zookeeper 实例
     * @param node 节点
     * @param watcher watcher
     * @param createMode 节点的类型
     * @return ture: 成功创建 false 存在，异常直接抛出
     */
    public static Boolean createNode(ZooKeeper zooKeeper
            , ZookeeperNode node
            , Watcher watcher
            , CreateMode createMode) {
        try {
            if (zooKeeper.exists(node.getNodePath(), watcher) == null) {
                String s = zooKeeper.create(node.getNodePath(), node.getData()
                        , ZooDefs.Ids.OPEN_ACL_UNSAFE
                        , createMode);
                log.info("节点成功创建，【{}】", s);
                return true;
            }else {
                if(log.isDebugEnabled()){
                    log.info("节点已经存在：【{}】",node.getNodePath());
                }
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录时发生异常，【{}", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 关闭zookeeper方法
     * @param zooKeeper zooKeeper实例
     */
    public static void close (ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper时发生异常：【{}】",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 判断节点是否存在
     * @param node 节点
     * @param zooKeeper zooKeeper实例
     * @param watcher watcher
     * @return true 存在， false 不存在
     */
    public static Boolean exists(String node,ZooKeeper zooKeeper,Watcher watcher){
        try {
            return zooKeeper.exists(node,watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点是否存在发生异常：【{}】，节点【{}】",e,node);
            throw new ZookeeperException(e);
        }
    }

    /**
     * 查询一个节点的子元素
     *
     * @param zooKeeper   zk实例
     * @param serviceNode 服务节点
     * @return 子元素列表
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode, Watcher watcher) {
        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取节点【{}】的子元素时发生异常，【{}】",serviceNode,e);
            throw new ZookeeperException(e);
        }


    }
}
