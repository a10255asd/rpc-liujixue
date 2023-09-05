package com.liujixue.spi;

import com.liujixue.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixue
 * @Date 2023/9/5 16:20
 * @PackageName:com.liujixue.spi
 * @ClassName: SpiHandler
 * @Description: 实现一个简易版本的 spi
 */
@Slf4j
public class SpiHandler {
    // 定义一个basePath
    private static final String BASE_PATH = "META-INF/rpc-services";
    // 先定义一个缓存，用来保存spi相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);

    // 加载当前类之后，需要将spi信息进行保存，避免运行时频繁执行 IO
    static {
        // TODO 怎么加载当前工程和jar包中的classPath中的资源
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        if (fileUrl != null) {
            File file = new File(fileUrl.getPath());
            File[] children = file.listFiles();
            if (children != null && children.length > 0) {
                for (File child : children) {
                    String key = child.getName();
                    List<String> value = getImplNames(child);
                    SPI_CONTENT.put(key, value);
                }
            }
        }
    }

    private static List<String> getImplNames(File child) {
        try (
                FileReader fileReader = new FileReader(child);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
        ) {
            List<String> implNames = new ArrayList<>();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || "".equals(line)) {
                    implNames.add(line);
                }
                return implNames;
            }
        } catch (IOException e) {
            log.error("读取spi文件时发生异常", e);
        }
        return null;
    }

    public static <T> T get(Class<T> clazz) {
        String name = clazz.getName();

        return null;
    }

    public static void main(String[] args) {

    }
}
