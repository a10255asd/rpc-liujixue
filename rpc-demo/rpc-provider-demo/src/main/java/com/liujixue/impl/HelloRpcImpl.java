package com.liujixue.impl;


import com.liujixue.HelloRpc;

/**
 * @Author LiuJixue
 * @Date 2023/8/18 15:21
 * @PackageName:com.liujixue.impl
 * @ClassName: HelloRpcImpl
 */
public class HelloRpcImpl implements HelloRpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer" +  msg;
    }
}
