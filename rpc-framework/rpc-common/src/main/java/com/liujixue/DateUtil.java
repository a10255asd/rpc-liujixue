package com.liujixue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * @Author LiuJixue
 * @Date 2023/8/28 14:31
 * @PackageName:com.liujixue
 * @ClassName: DateUtil
 * @Description: 时间工具类
 */
public class DateUtil {
    public static Date get(String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return  sdf.parse(pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
