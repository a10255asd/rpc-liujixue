package com.liujixue.serialize;

import com.liujixue.serialize.impl.HessianSerializer;
import com.liujixue.serialize.impl.JDKSerializer;
import com.liujixue.serialize.impl.JSONSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixue
 * @Date 2023/8/29 09:17
 * @PackageName:com.liujixue.serialize
 * @ClassName: SerializeFactory
 */
@Slf4j
public class SerializerFactory {
    private final static ConcurrentHashMap<String,SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte,SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);
    static {
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JDKSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JSONSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());

        SERIALIZER_CACHE.put("jdk",jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);

        SERIALIZER_CACHE_CODE.put((byte) 1,jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2,json);
        SERIALIZER_CACHE_CODE.put((byte) 3,hessian);


    }

    /**
     * 使用工厂方法获取一个 SerializerWrapper
     * @param serializeType 序列化的类型
     * @return SerializerWrapper
     */
    public static SerializerWrapper getSerializer(String serializeType) {
        if(SERIALIZER_CACHE.get(serializeType) != null){
            return SERIALIZER_CACHE.get(serializeType);
        }
        log.error("未找到您配置的序列化方式【{}】使用默认jdk序列化方式",serializeType);
        return SERIALIZER_CACHE.get("jdk");
    }
    public static SerializerWrapper getSerializer(byte serializeCode) {
        if(SERIALIZER_CACHE_CODE.get(serializeCode) != null){
            return SERIALIZER_CACHE_CODE.get(serializeCode);
        }
        log.error("未找到您配置的序列化方式类型【{}】使用默认jdk序列化方式",serializeCode);
        return SERIALIZER_CACHE_CODE.get((byte)1);
    }
}
