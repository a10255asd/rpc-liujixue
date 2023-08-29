package com.liujixue.compress;

/**
 * @Author LiuJixue
 * @Date 2023/8/29 15:30
 * @PackageName:com.liujixue.compress
 * @ClassName: Compressor
 * @Description: TODO
 */
public interface Compressor {
    /**
     * 对字节码进行压缩
     * @param bytes 待压缩的字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] bytes);

    /**
     * 对字节码进行解压缩
     * @param bytes 待解压缩的字节数组
     * @return 解压缩后的字节数组
     */
    byte[] decompress(byte[] bytes);
}
