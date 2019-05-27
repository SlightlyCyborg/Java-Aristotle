import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class Admin {

    static class InstanceConfig{
        public String username;
        public String name;
        public String backButtonUrl;
        public String backButtonText;
        public String searchBarText;
        public String youtubeUrl;
    }

    static Renderer renderer = Renderer.getInstance();

    static Instance addInstance(InstanceConfig config){
        try {
            Instance toAdd = new Instance(config);
            toAdd.save();
            return toAdd;
        } catch(IllegalArgumentException e){

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
