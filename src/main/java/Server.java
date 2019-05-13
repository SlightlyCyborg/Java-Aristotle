import freemarker.template.TemplateException;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @RequestMapping("/")
    String landing(){
        try {
            String text = new String(Files.readAllBytes(Paths.get("templates/home.html")), StandardCharsets.UTF_8);
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequestMapping("/{siteName}")
    String home(@PathVariable("siteName") String siteName, @RequestParam("terms") Optional<String> terms) throws IOException, SolrServerException {
        if(terms.isPresent()){
            return instanceMap.get(siteName).search(terms.get());
        } else {
            return instanceMap.get(siteName).home();
        }
    }

    @GetMapping("/admin/indexer-progress")
    String indexerProgress(){
       return indexerProcess.progress().toHTML();
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
    String addInstance(@RequestParam String username,
                       @RequestParam String name,
                       @RequestParam String backButtonUrl,
                       @RequestParam String backButtonText,
                       @RequestParam String searchBarText,
                       @RequestParam String youtubeUrl,
                       @RequestParam String password
    ) throws IOException, TemplateException {
        if(!password.equals("aristotle2k19")){
            return "unauthorized";
        }
        Admin.InstanceConfig config = new Admin.InstanceConfig();
        config.username = username;
        config.name = name;
        config.backButtonText = backButtonText;
        config.backButtonUrl = backButtonUrl;
        config.searchBarText = searchBarText;
        config.youtubeUrl = youtubeUrl;


        Instance instance = Admin.addInstance(config);
        if(instance != null) {
            indexerProcess.addInstanceToIndex(instance);
            instanceMap.put(instance.getUsername(), instance);
        }
        return admin();
    }

    @PostConstruct
    private void init(){
        List<Instance> instances = null;
        try {
            instances = Instance.fromDB();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
