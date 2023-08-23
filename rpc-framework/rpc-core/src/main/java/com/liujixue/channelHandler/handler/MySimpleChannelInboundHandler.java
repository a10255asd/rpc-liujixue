package com.liujixue.channelHandler.handler;

import com.liujixue.RpcBootstrap;
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
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        String result = msg.toString(Charset.defaultCharset());
        log.info("msg-------->{}",msg.toString(Charset.defaultCharset()));
        // 从全局的挂起的请求中寻找与之匹配的待处理的 CompletableFuture
        CompletableFuture<Object> completableFuture = RpcBootstrap.PADDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
