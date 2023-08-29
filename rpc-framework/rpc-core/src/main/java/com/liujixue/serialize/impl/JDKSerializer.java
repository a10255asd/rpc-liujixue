package com.liujixue.serialize.impl;

import com.liujixue.exceptions.SerializeException;
import com.liujixue.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOException;
import java.io.*;

/**
 * @Author LiuJixue
 * @Date 2023/8/28 17:07
 * @PackageName:com.liujixue.serialize
 * @ClassName: JDKSerializer
 * @Description: JDK 的序列化实现
 */
@Slf4j
public class JDKSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try (
                // 将流的定义写try()里会自动关闭。不需要再写finally
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);) {
            if(log.isDebugEnabled()){
                log.debug("对象【{}】已经完成了序列化",object);
            }
            objectOutputStream.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            log.info("序列化对象【{}】时发生异常",object);
           throw new SerializeException();
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null || clazz == null){
            return null;
        }
        try (
                // 将流的定义写try()里会自动关闭。不需要再写finally
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(bais);
        ) {
            Object object = objectInputStream.readObject();
            if(log.isDebugEnabled()){
                log.debug("类【{}】已经完成了反序列化",object);
            }
            return (T)object;
        } catch (IOException |ClassNotFoundException e ) {
            log.info("反序列化对象【{}】时发生异常",clazz);
            throw new SerializeException();
        }
    }
}
