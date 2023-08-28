package com.liujixue.channelHandler;

import com.liujixue.channelHandler.handler.MySimpleChannelInboundHandler;
import com.liujixue.channelHandler.handler.RpcRequestEncoder;
import com.liujixue.channelHandler.handler.RpcResponseDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Author LiuJixue
 * @Date 2023/8/23 14:29
 * @PackageName:com.liujixue.channelHandler
 * @ClassName: ConsumerChannelInitializer
 * @Description: 消费端channel
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel
                    .pipeline()
                    // netty 自带的日志处理器
                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                    // 消息编码器（出栈）
                    .addLast(new RpcRequestEncoder())
                    // 入栈解码器
                    .addLast(new RpcResponseDecoder())
                    // 处理结果
                    .addLast(new MySimpleChannelInboundHandler());
    }
}
