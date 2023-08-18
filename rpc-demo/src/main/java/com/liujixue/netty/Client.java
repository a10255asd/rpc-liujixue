package com.liujixue.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * @Author LiuJixue
 * @Date 2023/8/16 16:01
 * @PackageName:com.liujixue.netty
 * @ClassName: Client
 * @Description: TODO
 */
public class Client {
    public void run() throws InterruptedException {
        // 启动一个客户端需要一个辅助类，bootstrap
        Bootstrap bootstrap = new Bootstrap()
                // 定义线程池，EventLoopGroup
                .group(new NioEventLoopGroup())
                .remoteAddress(new InetSocketAddress(18888))
                // 选择初始化一个什么样的channel
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MyChannelHandlerClient());
                    }
                });
        // 尝试连接服务器
        ChannelFuture channelFuture = bootstrap.connect().sync();
        // 获取 channel ，并且写出数据
        Channel channel = channelFuture.channel();
        channel.writeAndFlush(Unpooled.copiedBuffer("hello netty!".getBytes(StandardCharsets.UTF_8)));
        // 阻塞程序，等待接受消息
        channel.closeFuture().sync();
    }

    public static void main(String[] args) throws InterruptedException {
        new Client().run();
    }

}
