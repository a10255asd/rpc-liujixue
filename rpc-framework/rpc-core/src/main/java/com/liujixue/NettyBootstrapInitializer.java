package com.liujixue;

import com.liujixue.RpcBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author LiuJixue
 * @Date 2023/8/22 16:37
 * @PackageName:com.liujixue.discovery
 * @ClassName: NettyBootstrapInitializer
 * @Descriptio 提供 BootStrap 单例
 */
public class NettyBootstrapInitializer {
    private static final Bootstrap bootstrap = new Bootstrap();

    private NettyBootstrapInitializer() {
    }

    static {
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        bootstrap
                // 定义线程池，EventLoopGroup
                .group(nioEventLoopGroup)
                // 选择初始化一个什么样的channel
                .channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(null);
                    }
                });
    }
    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
