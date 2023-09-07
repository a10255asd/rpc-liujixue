package com.liujixue.enumeration;

import lombok.Data;

/**
 * @Author LiuJixue
 * @Date 2023/8/28 09:06
 * @ClassName: ResponseCode
 * @Description: 返回枚举
 * 响应码需要做统一的处理
 * 成功码 20(方法成功调用) 21(心跳成功返回)
 * 错误码(服务端错误) 50(请求的方法不存在)
 * 错误码(客户端错误) 44
 * 负载码 31(服务器负载过高被限流)
 */

public enum ResponseCode {
    SUCCESS((byte)20,"成功"),
    SUCCESS_HEART_BEAT((byte)21,"心跳检测成功返回"),
    RATE_LIMIT((byte)31,"服务被限流"),
    RESOURCE_NOT_FOUND((byte)44,"请求的资源不存在"),

    FAIL((byte)50,"调用方法发生异常");
    // 1. 成功、2. 失败
    private byte code;
    private String desc;
    //
    private ResponseCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
