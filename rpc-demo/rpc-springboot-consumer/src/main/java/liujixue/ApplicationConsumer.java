package liujixue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author LiuJixue
 * @Date 2023/9/10 16:26
 * @ClassName: ApplicationProvider
 */
@SpringBootApplication
@RestController
public class ApplicationConsumer {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationConsumer.class,args);
    }
    @GetMapping("test")
    public String hello(){
        return "hello consumer";
    }
}
