package com.liujixue.serialize;

import com.liujixue.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @Author LiuJixue
 * @Date 2023/8/28 16:56
 * @PackageName:com.liujixue.serialize
 * @ClassName: SerializeUtil
 * @Description: TODO
 */
@Slf4j
public class SerializeUtil {
    /**
     * 序列化，将对象转化为byte数组
     * @param object
     * @return
     */
    public static byte[] serialize(Object object) {
        //  针对不同的消息类型，需要做不同的处理：心跳请求(没有requestPayload)
        if(object == null){
            return null;
        }
        // 对象转字节数组。序列化、压缩
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(object);
            // TODO 压缩
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常，【{}】",e);
            throw new RuntimeException(e);
        }
    }
}
