import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

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

    public String home() {
        try {
            Template template = getTemplate("home.ftl");
            StringWriter out = new StringWriter();
            template.process(this, out);
            return out.toString();
        }  catch (IOException e) {
            e.printStackTrace();
           return "" ;
        } catch (TemplateException e){
            e.printStackTrace();
            return "" ;
        }
    }

    public String search(SearchResult result) {
        return "<html></html>";
    }
}
