package com.liujixue.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author LiuJixue
 * @Date 2023/8/23 15:02
 * @PackageName:com.liujixue.transport.massage
 * @ClassName: RequestPayload
 * @Description: 用来描述请求调用方所请求的接口方法的描述
 * helloRpc.sayHi("你好") com.liujixue.HelloRpc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {
    // 1. 接口名名称
    private String interfaceName;
    // 2. 调用方法名称 -- com.liujixue.HelloRpc
    private String methodName;
    // 3. 参数列表,参数分为参数类型和具体的参数
    // 参数类型用来确定重载方法，具体参数用来执行方法调用
    private Class<?>[] parametersType; // --{java.long.String}
    private Object[] parametersValue;  // --{java.long.String}
    // 4. 返回值的封装 --{java.long.String}
    private Class<?> returnType;
}
