package com.liujixue.channelHandler.handler;

import com.liujixue.compress.Compressor;
import com.liujixue.compress.CompressorFactory;
import com.liujixue.enumeration.ResponseCode;
import com.liujixue.serialize.Serializer;
import com.liujixue.serialize.SerializerFactory;
import com.liujixue.transport.message.MessageFormatConstant;
import com.liujixue.transport.message.RequestPayload;
import com.liujixue.transport.message.RpcRequest;
import com.liujixue.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

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
 * cd
 * serialize 1byte
 * compress 1byte
 * requestType 1byte
 * requestId 8byte
 * body
 * 出栈时需要编码，进栈需要解码
 */
@Slf4j
public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> implements Serializable {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        // 4个字节的魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 2个字节的 header 长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);

        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        // 三个类型
        byteBuf.writeByte(rpcResponse.getCode());
        byteBuf.writeByte(rpcResponse.getSerializeType());
        byteBuf.writeByte(rpcResponse.getCompressType());
        // 8 字节的请求id
        byteBuf.writeLong(rpcResponse.getRequestId());
        // 9. 时间戳
        byteBuf.writeLong(new Date().getTime());
        // body，写入请求体
        // 对响应做序列化
        byte[] body = null;
        if(rpcResponse.getBody() != null){
            Serializer serializer = SerializerFactory.getSerializer(rpcResponse.getSerializeType()).getSerializer();
            body = serializer.serialize(rpcResponse.getBody());
            // 压缩
            Compressor compressor = CompressorFactory.getCompressor(rpcResponse.getCompressType()).getCompressor();
            body = compressor.compress(body);
        }
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
        if (log.isDebugEnabled()) {
            log.debug("响应【{}】已经完成编码工作",rpcResponse.getRequestId());
        }
    }


}
