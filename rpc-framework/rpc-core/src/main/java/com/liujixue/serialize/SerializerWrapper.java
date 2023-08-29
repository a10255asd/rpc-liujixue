package com.liujixue.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuJixue
 * @Date 2023/8/29 09:43
 * @PackageName:com.liujixue.serialize
 * @ClassName: SerializerWrapper
 * @Description: TODO
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SerializerWrapper {
    private byte code;
    private String type;
    private Serializer serializer;
}
