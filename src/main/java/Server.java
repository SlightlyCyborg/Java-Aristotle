import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;


@RestController
@EnableAutoConfiguration
public class Server {

    HashMap<String, Instance> instances;

    @RequestMapping("/{siteName}")
    String home(@PathVariable("siteName") String siteName) {
        return "hello " + siteName;
    }

    @PostConstruct
    private void init(){
    }


    private Instance getInstance(String name){
        return instances.get(name);
    }

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }
}
