package com.liujixue.utils.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 15:50
 * @PackageName:com.liujixue.utils.zookeeper
 * @ClassName: ZookeeperNode
 * @Description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZookeeperNode {
    private String nodePath;
    private byte[] data;
}
