package com.liujixue.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Author LiuJixue
 * @Date 2023/8/16 16:26
 * @PackageName:com.liujixue.netty
 * @ClassName: ServerClient
 * @Description: TODO
 */
public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start(){
        //1. 创建 EventLoopGroup
        // boss 只负责处理请求，之后会将请求分发给worker
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);
        try {
        //2. 需要一个服务器引导程序
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // 3. 配置服务器
        serverBootstrap
                .channel(NioServerSocketChannel.class)
                .group(boss,worker)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MyChannelHandler());
                    }
                });
        // 4. 绑定端口
        ChannelFuture channelFuture = serverBootstrap.bind(port);

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Server(18888).start();
    }
}
