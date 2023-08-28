package com.liujixue.serialize;

/**
 * @Author LiuJixue
 * @Date 2023/8/28 17:02
 * @PackageName:com.liujixue.serialize
 * @ClassName: Serializer
 * @Description: 序列化器
 */
public interface Serializer {
    /**
     * 抽象的用来做序列化的方法
     * @param object 等待序列化的对象实例
     * @return 字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化的方法
     * @param bytes 待反序列化的字节数组
     * @param clazz 目标类的 clss 队形
     * @return 目标实例
     * @param <T> 目标类范型
     */
    <T> T  deserialize(byte[] bytes,Class<T> clazz);
}
