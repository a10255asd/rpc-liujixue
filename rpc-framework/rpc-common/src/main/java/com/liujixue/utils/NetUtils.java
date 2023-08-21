package com.liujixue.utils;

import com.liujixue.exceptions.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @Author LiuJixue
 * @Date 2023/8/21 16:58
 * @PackageName:com.liujixue.utils
 * @ClassName: NetUtils
 * @Description: 网络工具类
 */
@Slf4j
public class NetUtils {
    public static String getIp(){
        try {
            // 获取所有的网卡信息
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()){
                NetworkInterface iface = interfaces.nextElement();
                // 过滤非回环接口和虚拟接口
                if(iface.isLoopback()||iface.isVirtual()||!iface.isUp()){
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress addr = addresses.nextElement();
                    if(addr instanceof Inet6Address || addr.isLoopbackAddress()){
                        continue;
                    }
                    String ipAddress = addr.getHostAddress();
                    log.info("局域网ip地址:【{}】", ipAddress);
                    return ipAddress;
                }
            }
            throw new NetworkException();
        } catch (SocketException e) {
            log.error("获取局域网ip时发生异常，【{}】",e);
            throw new NetworkException(e);
        }
    }
}
