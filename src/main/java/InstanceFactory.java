import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstanceFactory {
    public static List<Instance> fromDirectory(File dir){
      List<Instance> instances = new ArrayList<Instance>();

      if(!dir.isDirectory()){
          throw new IllegalArgumentException();
      }

      File[] xmls = dir.listFiles();

      for(int i=0; i<xmls.length; i++){
          try {
              instances.add(fromFile(xmls[i]));
          } catch(Exception e){}
      }

      return instances;
    }

    public static Instance fromFile(File xml) throws IOException, SAXException, ParserConfigurationException {
        Instance rv = new Instance();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document config = db.parse(xml);

        Element instance = config.getDocumentElement();

        rv.setName(instance.getAttribute("name"));
        rv.setUsername(instance.getAttribute("username"));

        return rv;
    }
}
