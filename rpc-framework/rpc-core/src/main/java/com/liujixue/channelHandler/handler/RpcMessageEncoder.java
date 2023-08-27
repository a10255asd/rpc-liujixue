package com.liujixue.channelHandler.handler;

import com.liujixue.enumeration.RequestType;
import com.liujixue.transport.message.MessageFormatConstant;
import com.liujixue.transport.message.RequestPayload;
import com.liujixue.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @Author LiuJixue
 * @Date 2023/8/23 16:02
 * @PackageName:com.liujixue.channelHandler.handler
 * @ClassName: RpcMessageEncoder
 * @Description:
 * magic(魔数) 5byte ---> ljx-
 * version(版本) 1byte ---> 1
 * header length(首部的长度) 2byte
 * full length(报文总长度) 4byte
 * serialize 1byte
 * compress 1byte
 * requestType 1byte
 * requestId 8byte
 * body
 * 出栈时需要编码，进栈需要解码
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcRequest> implements Serializable {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        // 4个字节的魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 2个字节的 header 长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);

        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        // 三个类型
        byteBuf.writeByte(rpcRequest.getRequestType());
        byteBuf.writeByte(rpcRequest.getSerializeType());
        byteBuf.writeByte(rpcRequest.getCompressType());
        // 8 字节的请求id
        byteBuf.writeLong(rpcRequest.getRequestId());
        // body，写入请求体
        byte[] body = getBodyBytes(rpcRequest.getRequestPayload());
        // 如果是心跳请求不处理请求体
        if(body != null){
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null ? 0: body.length;
        // 重新处理报文的总长度
        // 保存当前写指针的位置
        int writerIndex = byteBuf.writerIndex();
        // 将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(
                MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH
                + MessageFormatConstant.HEADER_FIELD_LENGTH
        );
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        // 将写指针归位
        byteBuf.writerIndex(writerIndex);
    }

    /**
     * 序列化，将对象转化为byte数组
     * @param requestPayload 负载
     * @return
     */
    private byte[] getBodyBytes(RequestPayload requestPayload) {
        //  针对不同的消息类型，需要做不同的处理：心跳请求(没有requestPayload)
        if(requestPayload == null){
            return null;
        }
        // 对象转字节数组。序列化、压缩
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(requestPayload);
            // TODO 压缩
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常，【{}】",e);
            throw new RuntimeException(e);
        }
    }
}
