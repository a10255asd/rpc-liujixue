package com.liujixue.transport.message;

import java.nio.charset.StandardCharsets;

/**
 * @Author LiuJixue
 * @Date 2023/8/23 16:17
 * @PackageName:com.liujixue.transport.message
 * @ClassName: MessageFormatConstant
 * @Description: TODO
 */
public class MessageFormatConstant {
    public final static byte[] MAGIC = "ljx-".getBytes(StandardCharsets.UTF_8);
    public final static byte VERSION = 1;
    public final static short HEADER_LENGTH = (byte) (MAGIC.length + 1 + 2 + 4 +1 +1 +1 + 8);
    public final static int MAX_FRAME_LENGTH = 1024 * 1024;
    public static final int VERSION_LENGTH = 1;
    // 头部信息长度占用的字节数
    public static final int HEADER_FIELD_LENGTH = 2;
    public static final int FULL_FIELD_LENGTH = 4;
}
