package com.liujixue.serialize.impl;

import com.alibaba.fastjson.JSON;
import com.liujixue.serialize.Serializer;
import com.liujixue.transport.message.RequestPayload;

import java.util.Arrays;

/**
 * @Author LiuJixue
 * @Date 2023/8/29 09:24
 * @PackageName:com.liujixue.serialize
 * @ClassName: JSONSerializer
 * @Description: FASTJSON 序列化
 */
public class JSONSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if(object == null){
            return null;
        }
        byte[] jsonBytes = JSON.toJSONBytes(object);
        return jsonBytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null | clazz ==null){
            return null;
        }
        Object object = JSON.parseObject(bytes, clazz);
        return (T)object;
    }

    public static void main(String[] args) {
        Serializer serializer = new JSONSerializer();
        RequestPayload requestPayload = new RequestPayload();
        requestPayload.setInterfaceName("XXXX");
        requestPayload.setMethodName("YYYY");
        byte[] serialize = serializer.serialize(requestPayload);
        System.out.println(Arrays.toString(serialize));
        RequestPayload deserialize = serializer.deserialize(serialize, RequestPayload.class);
        System.out.println(deserialize);
    }
}
