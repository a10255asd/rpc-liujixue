package com.liujixue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

/**
 * @Author: LiuJixue
 * @Date: 2023/8/16 14:50
 * @PackageName:com.liujixue
 * @ClassName: NettyTest
 * @Description: netty 测试类
 */
public class NettyTest {
    @Test
    public void testByteBuf() {
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();
        // 通过逻辑组装，而不是物理拷贝，实现在JVM中零拷贝
        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.addComponents(header,body);
    }
    @Test
    public void testWrapper(){
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];
        // 共享byte数组的内容而不是拷贝，这也算零拷贝
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);
    }
    @Test
    public void testSlice(){
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];
        // 共享byte数组的内容，而不是拷贝，这也算是零拷贝
    }
}
