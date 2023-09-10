package com.liujixue.channelHandler.handler;

import com.liujixue.RpcBootstrap;
import com.liujixue.ServiceConfig;
import com.liujixue.core.ShutDownHolder;
import com.liujixue.enumeration.RequestType;
import com.liujixue.enumeration.ResponseCode;
import com.liujixue.protection.RateLimiter;
import com.liujixue.protection.TokenBuketRateLimiter;
import com.liujixue.transport.message.RequestPayload;
import com.liujixue.transport.message.RpcRequest;
import com.liujixue.transport.message.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

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
        // 1.封装部分响应
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        rpcResponse.setCompressType(rpcRequest.getCompressType());
        rpcResponse.setSerializeType(rpcRequest.getSerializeType());
        Channel channel = channelHandlerContext.channel();
        // 查看关闭的挡板是否打开,如果打开，直接返回一个错误的响应
        if(ShutDownHolder.BAFFLE.get()){
            rpcResponse.setCode(ResponseCode.BE_CLOSING.getCode());
            channel.writeAndFlush(rpcResponse);
        }
        // 计数器加一
        ShutDownHolder.REQUEST_COUNTER.increment();
        // 2. 完成限流相关的操作
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter = RpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if(rateLimiter == null){
             rateLimiter = new TokenBuketRateLimiter(500,5000);
            everyIpRateLimiter.put(socketAddress,rateLimiter);
        }
        boolean allowRequest = rateLimiter.allowRequest();
        // 不处理请求的逻辑
        // 限流
        if (!allowRequest){
            // 需要封装响应，并且返回
            rpcResponse.setCode(ResponseCode.RATE_LIMIT.getCode());
        } else if(rpcRequest.getRequestType() == RequestType.HEARTBEAT.getId()){
            // 需要封装响应，并且返回
            // 心跳处理
            rpcResponse.setCode(ResponseCode.SUCCESS_HEART_BEAT.getCode());
        }else {
            //--------------------------- 具体调用过程--------------------------
            // 正常调用
            // 3. 获取负载内容
            RequestPayload requestPayload = rpcRequest.getRequestPayload();

            //  根据负载内容进行方法调用
            try{
            Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经完成方法调用", rpcRequest.getRequestId());
                }
                // 4. 封装响应
                rpcResponse.setCode(ResponseCode.SUCCESS.getCode());
                rpcResponse.setBody(result);
            }catch (Exception e){
                log.error("请求编号为：【{}】的请求在调用过程中发生异常",rpcRequest.getRequestId());
                rpcResponse.setCode(ResponseCode.FAIL.getCode());
            }
        }
        // 5. 写出响应
        channel.writeAndFlush(rpcResponse);
        // 计数器减一
        ShutDownHolder.REQUEST_COUNTER.decrement();
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
