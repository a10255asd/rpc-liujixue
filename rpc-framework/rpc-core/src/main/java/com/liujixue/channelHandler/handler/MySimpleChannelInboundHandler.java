package com.liujixue.channelHandler.handler;

import com.liujixue.RpcBootstrap;
import com.liujixue.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
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
        // 服务提供方，给予的结果
        Object returnValue = rpcResponse.getBody();
        // 从全局的挂起的请求中寻找与之匹配的待处理的 CompletableFuture
        CompletableFuture<Object> completableFuture = RpcBootstrap.PADDING_REQUEST.get(rpcResponse.getRequestId());
        completableFuture.complete(returnValue);
        if (log.isDebugEnabled()) {
            log.debug("已寻找到编号为【{}】的completableFuture,处理响应结果",rpcResponse.getRequestId());
        }
    }
}
