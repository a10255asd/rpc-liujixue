package com.liujixue.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuJixue
 * @Date 2023/8/29 15:37
 * @PackageName:com.liujixue.compress
 * @ClassName: CompressWrapper
 * @Description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompressWrapper {
    private byte code;
    private String type;
    private Compressor compressor;
}
