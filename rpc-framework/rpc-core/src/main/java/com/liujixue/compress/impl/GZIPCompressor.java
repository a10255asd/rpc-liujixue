package com.liujixue.compress.impl;

import com.liujixue.compress.Compressor;
import com.liujixue.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author LiuJixue
 * @Date 2023/8/29 15:44
 * @PackageName:com.liujixue.compress.impl
 * @ClassName: GZIPCompressor
 * @Description: 使用gzip算法实现压缩解压缩
 */
@Slf4j
public class GZIPCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对字节数组进行了压缩，长度变化【{}】-->【{}】", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常【{}】", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(bais);) {
            byte[] result = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) {
                log.debug("对字节数组进行了解压缩，长度变化【{}】-->【{}】", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行解压缩时发生异常【{}】", e);
            throw new CompressException(e);
        }
    }
}
