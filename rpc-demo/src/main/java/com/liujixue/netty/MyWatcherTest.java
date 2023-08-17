package com.liujixue.netty;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * @Author LiuJixue
 * @Date 2023/8/17 16:16
 * @PackageName:com.liujixue
 * @ClassName: MyWatcherTest
 * @Description: Watcher只编写自己关心的事件就可以了
 */
public class MyWatcherTest implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        // 判断事件类型,是否是连接类型的事件
        if(watchedEvent.getType() == Event.EventType.None){
            if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                System.out.println(watchedEvent.getPath() + "zookeeper 连接成功");
            }else if(watchedEvent.getState() == Event.KeeperState.AuthFailed){
                System.out.println(watchedEvent.getPath() + "zookeeper 认证失败");
            }else if(watchedEvent.getState() == Event.KeeperState.Closed){
                System.out.println(watchedEvent.getPath() + "zookeeper 断开连接");
            }
        }else if(watchedEvent.getType() == Event.EventType.NodeCreated){
            System.out.println(watchedEvent.getPath() + "zookeeper 节点创建");
        }else if(watchedEvent.getType() == Event.EventType.NodeDeleted){
            System.out.println(watchedEvent.getPath() + "zookeeper 节点删除");
        }else if(watchedEvent.getType() == Event.EventType.NodeDataChanged){
            System.out.println(watchedEvent.getPath() + "zookeeper 节点更改更改");
        }else if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
            System.out.println(watchedEvent.getPath() + "zookeeper 子节点数据更改");
        }
    }
}
