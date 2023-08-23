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
    public final static short HEADER_LENGTH = (byte) (MAGIC.length + VERSION + 2 + 4 +1 +1 + 1 +1 + 8);

}
