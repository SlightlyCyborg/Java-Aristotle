import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer extends Configuration {

    public static Renderer single_instance;

    static {
        try {
            single_instance = new Renderer();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static Renderer getInstance(){
        return single_instance;
    }

    private Renderer() throws IOException {
        super();
        setDirectoryForTemplateLoading(new File("templates"));
        setDefaultEncoding("UTF-8");
        setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    }

    private String site(Map<String, Object> root){
        try {
            Template template = getTemplate("home.ftl");
            StringWriter out = new StringWriter();
            template.process(root, out);
            return out.toString();
        }  catch (IOException e) {
            e.printStackTrace();
           return "" ;
        } catch (TemplateException e){
            e.printStackTrace();
            return "" ;
        }
    }

    public String home(Instance instance) {
        Map<String, Object> root = new HashMap<>();
        root.put("instance", instance);
        root.put("hasResults", false);
        return site(root) ;
    }

    public String search(Instance instance, SearchResult result) {
        Map<String, Object> root = new HashMap<>();
        root.put("instance", instance);
        root.put("hasResults", true);
        root.put("videos", result.getVideos());
        return site(root);
    }

    public String admin(Collection<Instance> instances) throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        root.put("instances", instances);
        Template template = getTemplate("admin.ftl");
        StringWriter out = new StringWriter();
        template.process(root, out);
        return out.toString();
    }
    
    public String addInstance(){
        String error = null;
        try {
            Map<String, Object> dataModel = new HashMap<String, Object>();
            Template template = getTemplate("add-instance.ftl");
            StringWriter out = new StringWriter();
            template.process(dataModel, out);
            return out.toString();
        } catch (IOException e) {
            error = e.getMessage();
        } catch (TemplateException e) {
            error = e.getMessage();
        }

        return error;
    }
}
