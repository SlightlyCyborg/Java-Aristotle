import freemarker.template.TemplateException;
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
import java.util.Optional;


@RestController
@EnableAutoConfiguration
public class Server {
    //This is single process (not a list) due to the "no faster than human" YouTube bot rule.
    IndexerProcess indexerProcess;

    Map<String, Instance> instanceMap;
    Renderer renderer;

    @RequestMapping("/{siteName}")
    String home(@PathVariable("siteName") String siteName, @RequestParam("terms") Optional<String> terms) throws IOException, SolrServerException {
        if(terms.isPresent()){
            return instanceMap.get(siteName).search(terms.get());
        } else {
            return instanceMap.get(siteName).home();
        }
    }

    @GetMapping("/admin")
    String admin() throws IOException, TemplateException {
        return renderer.admin(instanceMap.values());
    }

    @GetMapping("/admin/add-instance")
    String showAddInstance() throws IOException, TemplateException {
        return Admin.addInstance();
    }

    @PostMapping("/admin/add-instance")
    String addInstance(@RequestBody String username,
                       @RequestBody String name,
                       @RequestBody String backButtonUrl,
                       @RequestBody String backButtonText,
                       @RequestBody String searchBarText,
                       @RequestBody String youtubeUrl) throws IOException, TemplateException {

        Admin.InstanceConfig config = new Admin.InstanceConfig();
        config.username = username;
        config.name = name;
        config.backButtonText = backButtonText;
        config.backButtonUrl = backButtonUrl;
        config.searchBarText = searchBarText;
        config.youtubeUrl = youtubeUrl;


        //Instance instance = Admin.addInstance(config);
        //indexerProcess.addInstanceToIndex(instance);
        return admin();
    }

    @PostConstruct
    private void init(){
        List<Instance> instances = Instance.fromDirectory(new File("instances"));
        instanceMap = new HashMap<>();
        for(Instance instance: instances){
            instanceMap.put(instance.getUsername(), instance);
        }
        renderer = Renderer.getInstance();
        indexerProcess = new IndexerProcess();
        indexerProcess.start();
    }


    private Instance getInstance(String name){
        return instanceMap.get(name);
    }

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }
}
