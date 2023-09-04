package com.liujixue.impl;


import com.liujixue.HelloRpc;
import com.liujixue.annotation.RpcApi;

/**
 * @Author LiuJixue
 * @Date 2023/8/18 15:21
 * @PackageName:com.liujixue.impl
 * @ClassName: HelloRpcImpl
 */
@RpcApi
public class HelloRpcImpl implements HelloRpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer" +  msg;
    }
}
