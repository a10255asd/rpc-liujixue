package liujixue;

import com.liujixue.annotation.RpcService;
import com.liujixue.proxy.RpcProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @Author LiuJixue
 * @Date 2023/9/10 17:29
 * @ClassName: RpcProxyBeanPostprocessor
 */
@Component
public class RpcProxyBeanPostprocessor implements BeanPostProcessor {
    // 会拦截所有的bean的创建，会在每一个bean初始化后被调用
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 生成一个代理
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcService rpcService = field.getAnnotation(RpcService.class);
            if(rpcService!=null){
                // 获取一个代理
                Class<?> type = field.getType();
                Object proxy = RpcProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
