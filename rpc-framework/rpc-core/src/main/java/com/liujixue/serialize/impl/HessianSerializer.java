package com.liujixue.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.liujixue.exceptions.SerializeException;
import com.liujixue.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @Author LiuJixue
 * @Date 2023/8/28 17:07
 * @PackageName:com.liujixue.serialize
 * @ClassName: JDKSerializer
 * @Description: Hession 的序列化实现
 */
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try (
                // 将流的定义写try()里会自动关闭。不需要再写finally
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            if (log.isDebugEnabled()) {
                log.debug("对象【{}】使用hessian已经完成了序列化", object);
            }
            hessian2Output.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            log.info("使用hessian序列化对象【{}】时发生异常", object);
            throw new SerializeException();
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        try (
                // 将流的定义写try()里会自动关闭。不需要再写finally
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ) {
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            T t = (T) hessian2Input.readObject();
            if (log.isDebugEnabled()) {
                log.debug("类【{}】已使用hessian经完成了反序列化", clazz);
            }
            return t;
        } catch (IOException e) {
            log.info("使用hessian反序列化对象【{}】时发生异常", clazz);
            throw new SerializeException();
        }
    }
}
