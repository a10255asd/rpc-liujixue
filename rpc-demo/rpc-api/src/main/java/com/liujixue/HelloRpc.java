package com.liujixue;

import com.liujixue.annotation.TryTimes;

/**
 * @Author LiuJixue
 * @Date 2023/8/18 15:18
 * @PackageName:PACKAGE_NAME
 * @ClassName: com.liujixue.HelloRpc
 */
public interface HelloRpc {
    /***
     * 通用接口，server 和 client 都需要依赖
     * @param msg 发送的具体的消息
     * @return 返回的结果
     */
    @TryTimes(tryTimes = 3 ,intervalTime = 3000)
    String sayHi(String msg);
}
