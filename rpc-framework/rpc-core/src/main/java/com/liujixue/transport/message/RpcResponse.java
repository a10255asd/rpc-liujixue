package com.liujixue.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuJixue
 * @Date 2023/8/28 09:02
 * @PackageName:com.liujixue.transport.message
 * @ClassName: RpcResponse
 * @Description: 服务提供方回复的响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcResponse {
    // 请求的 id
    private Long requestId;
    // 请求的类型，压缩的类型，序列化的方式
    private byte compressType;
    private byte serializeType;
    // 1 成功 2 异常
    private byte code;
    // 具体的消息体
    private Object body;
}
