package com.liujixue;

import com.liujixue.netty.MyWatcherTest;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Author LiuJixue
 * @Date 2023/8/17 15:01
 * @PackageName:com.liujixue
 * @ClassName: ZookeeperTest
 * @Description: TODO
 */
public class ZookeeperTest {
    ZooKeeper zooKeeper;
    @Before
    public void CreateZk(){
        // 定义连接参数
        String connetString = "101.42.50.241:2881";
        // 定义超时时间
        int timeOut = 1000000;
        try {
            // new MyWatcher() 默认的监听器
            zooKeeper = new ZooKeeper(connetString,timeOut,new MyWatcherTest());
        } catch (IOException e) {
            throw new RuntimeException(e);
        };
    }
    @Test
    public void testCreatePNode(){
        try {
            String s = zooKeeper.create("/liujixueTest01", "hello".getBytes(StandardCharsets.UTF_8)
                    , ZooDefs.Ids.OPEN_ACL_UNSAFE
                    , CreateMode.PERSISTENT);
            System.out.println(s);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                if (zooKeeper!=null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void testDeletePNode(){
        try {
            // Version : cas mysql 乐观锁，也可以无视版本号，-1
            zooKeeper.delete("/liujixueTest01",-1);

        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                if (zooKeeper!=null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Test
    public void testExistPNode(){
        try {
            // Version : cas mysql 乐观锁，也可以无视版本号，-1
            Stat exists = zooKeeper.exists("/liujixueTest01", true);
            zooKeeper.setData("/liujixueTest01", "无视版本号".getBytes(StandardCharsets.UTF_8),-1);
            int version = exists.getVersion();
            // 当前节点的acl数据版本
            int aversion = exists.getAversion();
            // 当前子节点的数据版本
            int cversion = exists.getCversion();
            System.out.println("版本号:---->" + version);
            System.out.println("当前节点的acl数据版本:---->" + aversion);
            System.out.println("当前子节点的数据版本:----->" + cversion);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                if (zooKeeper!=null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Test
    public void testWatcher(){
        try {
            zooKeeper.exists("/liujixueTest01", true);
            while (true){
                Thread.sleep(10000);
            }
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                if (zooKeeper!=null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
