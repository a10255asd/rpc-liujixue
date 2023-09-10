package com.liujixue.channelHandler.handler;

import com.liujixue.RpcBootstrap;
import com.liujixue.enumeration.ResponseCode;
import com.liujixue.exceptions.ResponseException;
import com.liujixue.loadbalancer.LoadBalancer;
import com.liujixue.protection.CircuitBreaker;
import com.liujixue.transport.message.RpcRequest;
import com.liujixue.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @Author LiuJixue
 * @Date 2023/8/23 14:26
 * @PackageName:com.liujixue.channelHandler
 * @ClassName: MySimpleChannelInboundHandler
 * @Description: 这是一个用来测试的类
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        // 从全局的挂起的请求中寻找与之匹配的待处理的 CompletableFuture
        CompletableFuture<Object> completableFuture = RpcBootstrap.PADDING_REQUEST.get(rpcResponse.getRequestId());
        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        CircuitBreaker circuitBreaker = RpcBootstrap.getInstance().getConfiguration().getEveryIpCircuitBreaker().get(socketAddress);
        byte code = rpcResponse.getCode();
        if(code == ResponseCode.FAIL.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求返回错误的结果，响应码【{}】",rpcResponse.getRequestId(),rpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.FAIL.getDesc());
        }else if(code == ResponseCode.RATE_LIMIT.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求被限流，响应码【{}】",rpcResponse.getRequestId(),rpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.FAIL.getDesc());
        } else if (code == ResponseCode.RESOURCE_NOT_FOUND.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求未找到目标资源，响应码【{}】",rpcResponse.getRequestId(),rpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.FAIL.getDesc());
        } else if (code == ResponseCode.SUCCESS.getCode()) {
            // 服务提供方，给予的结果
            Object returnValue = rpcResponse.getBody();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()) {
                log.debug("已寻找到编号为【{}】的completableFuture,处理响应结果", rpcResponse.getRequestId());
            }
        }else if (code == ResponseCode.SUCCESS_HEART_BEAT.getCode()){
            if (log.isDebugEnabled()) {
                log.debug("已寻找到编号为【{}】的completableFuture,处理心跳检测", rpcResponse.getRequestId());
            }
            completableFuture.complete(null);
        }else if (code == ResponseCode.BE_CLOSING.getCode()){
            if (log.isDebugEnabled()) {
                log.debug("已寻找到编号为【{}】的请求，访问被拒绝，目标服务器正处于关闭中【{}】", rpcResponse.getRequestId(),rpcResponse.getCode());
            }
            completableFuture.complete(null);
            // 修正负载均衡器
            // 从健康列表中移除
            RpcBootstrap.CHANNEL_CACHE.remove(socketAddress);
            // 找到负载均衡器进行 reloadBalance
            LoadBalancer loadBalancer = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            // 重新进行负载均衡
            RpcRequest rpcRequest = RpcBootstrap.REQUEST_THREAD_LOCAL.get();
            loadBalancer.reloadBalance(rpcRequest.getRequestPayload().getInterfaceName(),RpcBootstrap.CHANNEL_CACHE.keySet().stream().toList());
            throw new ResponseException(code,ResponseCode.FAIL.getDesc());
        }

    }
}
