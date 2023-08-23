package com.liujixue.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuJixue
 * @Date 2023/8/23 14:59
 * @PackageName:com.liujixue.transport.massage
 * @ClassName: RpcRequest
 * @Description: 服务调用方发起的请求内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcRequest {
    // 请求的 id
    private Long requestId;
    // 请求的类型，压缩的类型，序列化的方式
    private byte requestType;
    private byte compressType;
    private byte serializeType;
    // 具体的消息体
    private RequestPayload requestPayload;
}
