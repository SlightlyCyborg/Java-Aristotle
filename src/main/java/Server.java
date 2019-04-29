import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@EnableAutoConfiguration
public class Server {

    Map<String, Instance> instanceMap;

    @RequestMapping("/{siteName}")
    String home(@PathVariable("siteName") String siteName, @RequestParam("terms") String terms) throws IOException, SolrServerException {
        if(terms != null){
            return instanceMap.get(siteName).search(terms);
        } else {
            return instanceMap.get(siteName).home();
        }
    }

    @PostConstruct
    private void init(){
        List<Instance> instances = InstanceFactory.fromDirectory(new File("instances"));
        instanceMap = new HashMap<>();
        for(Instance instance: instances){
            instanceMap.put(instance.getUsername(), instance);
        }
    }


    private Instance getInstance(String name){
        return instanceMap.get(name);
    }

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }
}
