package com.liujixue.compress;

import com.liujixue.compress.impl.GZIPCompressor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixue
 * @Date 2023/8/29 09:17
 * @PackageName:com.liujixue.serialize
 * @ClassName: SerializeFactory
 */
@Slf4j
public class CompressorFactory {
    private final static ConcurrentHashMap<String, CompressWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte,CompressWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);
    static {
        CompressWrapper gzip = new CompressWrapper((byte) 1, "gzip", new GZIPCompressor());

        COMPRESSOR_CACHE.put("gzip",gzip);


        COMPRESSOR_CACHE_CODE.put((byte) 1,gzip);



    }

    /**
     * 使用工厂方法获取一个 SerializerWrapper
     * @param compressorType 序列化的类型
     * @return CompressWrapper
     */
    public static CompressWrapper getCompressor(String compressorType) {
        if( COMPRESSOR_CACHE.get(compressorType) != null){
            return COMPRESSOR_CACHE.get(compressorType);
        }
        log.error("未找到您配置的压缩方式【{}】使用默认gzip压缩方式",compressorType);
        return COMPRESSOR_CACHE.get("gzip");
    }
    public static CompressWrapper getCompressor(byte compressorCode) {
        if( COMPRESSOR_CACHE_CODE.get(compressorCode) != null){
            return COMPRESSOR_CACHE_CODE.get(compressorCode);
        }
        log.error("未找到您配置的压缩方式编码【{}】使用默认gzip压缩方式",compressorCode);
        return COMPRESSOR_CACHE_CODE.get((byte)1);
    }
}
