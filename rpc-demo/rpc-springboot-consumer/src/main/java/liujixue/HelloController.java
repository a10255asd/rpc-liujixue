package liujixue;

import com.liujixue.HelloRpc;
import com.liujixue.annotation.RpcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author LiuJixue
 * @Date 2023/9/10 17:11
 * @ClassName: HelloController
 * @Description: TODO
 */
@RestController
public class HelloController {
    // 需要注入一个代理对象
    @RpcService
    private HelloRpc helloRpc;
    @GetMapping("hello")
    public  String hello(){
        return helloRpc.sayHi("hello provider");
    }
}
