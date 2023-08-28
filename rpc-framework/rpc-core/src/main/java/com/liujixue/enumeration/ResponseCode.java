package com.liujixue.enumeration;

import lombok.Data;

/**
 * @Author LiuJixue
 * @Date 2023/8/28 09:06
 * @PackageName:com.liujixue.enumeration
 * @ClassName: ResponseCode
 * @Description: 返回枚举
 */

public enum ResponseCode {
    SUCCESS((byte)1,"成功"),FAIL((byte)2,"失败");
    // 1. 成功、2. 已成
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
