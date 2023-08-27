package com.liujixue.channelHandler.handler;

import com.liujixue.RpcBootstrap;
import com.liujixue.ServiceConfig;
import com.liujixue.transport.message.RequestPayload;
import com.liujixue.transport.message.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author LiuJixue
 * @Date 2023/8/25 13:42
 * @PackageName:com.liujixue.channelHandler.handler
 * @ClassName: MethodCallHandler
 * @Description:
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        // 1. 获取负载内容
        RequestPayload requestPayload = rpcRequest.getRequestPayload();
        // 2. 根据负载内容进行方法调用
        Object object = callTargetMethod(requestPayload);
        // 3. TODO :封装响应
        // 4. TODO :写出响应
        channelHandlerContext.channel().writeAndFlush(object);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();
        // 寻找到匹配的暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = RpcBootstrap.SERVICES_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();
        // 通过反射调用 1.获取方法对象 2. 执行invoke方法
        Class<?> aClass = refImpl.getClass();
        Method method = null;
        Object returnValue = null;
        try {
            method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (NoSuchMethodException | IllegalAccessException |InvocationTargetException e) {
            log.error("调用服务【{}】的方法【{}】时发生异常",interfaceName,methodName);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
