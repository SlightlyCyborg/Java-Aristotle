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
import java.util.ServiceConfigurationError;

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
          } catch(Exception e){
              System.out.println(e.getClass());
          }
      }

      return instances;
    }

    private static SolrConfig parseSolrConnection(Node connection){
        SolrConfig cfg = new SolrConfig();

        NodeList attrs = connection.getChildNodes();
        for(int i=0; i<attrs.getLength(); i++){
            Node n = attrs.item(i);
            String name = n.getNodeName();
            switch(name){
                case "core":
                    cfg.setCore(n.getTextContent());
                    break;
                case "address":
                    cfg.setHost(n.getTextContent());
                    break;
                default:
                    break;
            }
        }
        return cfg;
    }

    public static SolrConfig getSolrConfig(Element instance, String configTagName) {
        NodeList nodes = instance.getElementsByTagName(configTagName);
        Node connection = nodes.item(0);
        if (connection == null){
            throw new IllegalStateException(
                    String.format("config for `%s` doesn't have solr configuration tag named `%s`",
                    instance.getAttribute("username"),
                    configTagName));
        }
        return parseSolrConnection(connection);
    }

    public static Instance fromFile(File xml) throws IOException, SAXException, ParserConfigurationException {
        Instance rv = new Instance();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document config = db.parse(xml);

        Element instance = config.getDocumentElement();

        rv.setName(instance.getAttribute("name"));
        rv.setUsername(instance.getAttribute("username"));


        SolrConfig videoConfig = getSolrConfig(instance, "solr-connection");
        SolrConfig blockConfig = getSolrConfig(instance, "solr-block-connection");

        rv.initializeSolr(videoConfig, blockConfig);
        return rv;
    }
}
