import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import subtitleDownloader.Runner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Instance {

    URL solrVideoURL, solrBlockURL;

    List<VideoSource> videoSources;

    List<URL> urlsToIndex;

    Searcher searcher;
    Indexer indexer;
    Renderer renderer;
    Runner stlRunner;

    File stlURLInput;
    File stlDir;

    private String name, username;
    private String backButtonURL, backButtonText, searchBarText;

    Instance() throws MalformedURLException {
        renderer = Renderer.getInstance();
        initializeSQL();
    }

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
              System.err.println("Exception caught while making an instance from directory");
          }
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

        String backButtonURL, backButtonText, searchBarText;

        backButtonURL = instance.getElementsByTagName("back-button-url")
                .item(0)
                .getTextContent();

        backButtonText = instance.getElementsByTagName("back-button-text")
                .item(0)
                .getTextContent();

        searchBarText = instance.getElementsByTagName("search-bar-text")
                .item(0)
                .getTextContent();

        rv.setBackButtonURL(backButtonURL);
        rv.setBackButtonText(backButtonText);
        rv.setSearchBarText(searchBarText);


        SolrConfig videoConfig = SolrConfig.getSolrConfig(instance, "solr-connection");
        SolrConfig blockConfig = SolrConfig.getSolrConfig(instance, "solr-block-connection");

        rv.initializeSolr(videoConfig, blockConfig);
        return rv;
    }

    public static List<Instance> fromDB() throws MalformedURLException {
        ArrayList<Instance> rv = new ArrayList<Instance>();

        /*
        List<InstanceConfig> configs = getInstanceConfigsFromConnection();
        for(InstanceConfig config: configs){
            Instance instance = fromConfig(config);
            rv.add(instance);
        }
        */

        return rv;
    }

    public String home(){
        return renderer.home(this);
    }

    public String search(String searchText) throws IOException, SolrServerException {
        SearchResult result = searcher.search(searchText);
        return renderer.search(this,result);
    }

    private void initializeSQL(){}

    public void initializeSolr(SolrConfig videoConfig, SolrConfig blockConfig) throws MalformedURLException {

        solrVideoURL = videoConfig.getURL();
        solrBlockURL = blockConfig.getURL();

        SolrClient videoConnection = new HttpSolrClient.Builder(videoConfig.getURL().toString()).build();
        SolrClient blockConnection = new HttpSolrClient.Builder(blockConfig.getURL().toString()).build();


        searcher = new Searcher(videoConnection, blockConnection);
        indexer = new Indexer(videoConnection, blockConnection);
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    void downloadSRTs(){
        stlRunner = new Runner(stlURLInput.getAbsolutePath(), stlDir.getAbsolutePath());
    }

    void generateURLsToIndex(){
        urlsToIndex = new ArrayList<URL>();
        for(Iterator<VideoSource> it = videoSources.iterator(); it.hasNext();){
            VideoSource source = it.next();
            OffsetDateTime lastIndexedDate = getLastIndexedVideoFromSource(source.getID());
            List<Video> toAdd = source.getVideosPublishedSince(lastIndexedDate);

            addVideosToURLsToIndex(toAdd);
        }
    }

    private void addVideosToURLsToIndex(List<Video> toAdd) {
        for(Iterator<Video> it = toAdd.iterator(); it.hasNext();){
            urlsToIndex.add(it.next().getUrl());
        }
    }

    private OffsetDateTime getLastIndexedVideoFromSource(String sourceId) {
        return OffsetDateTime.now();
    }

    URL getSolrVideoURL()  {
        return solrVideoURL;
    }

    URL getSolrBlockURL(){
        return solrBlockURL;
    }

    public String getBackButtonURL() { return backButtonURL; }

    public void setBackButtonURL(String backButtonURL) { this.backButtonURL = backButtonURL;  }

    public String getBackButtonText() { return backButtonText; }

    public void setBackButtonText(String backButtonText) { this.backButtonText = backButtonText; }

    public String getSearchBarText() { return searchBarText; }

    public void setSearchBarText(String searchBarText) { this.searchBarText=searchBarText; }

    private void setBlockConfig(SolrConfig fromID) {

    }

    private void setVideoConfig(SolrConfig fromID) {
    }

    private String toSQLValues(){
        String values = String.format("%s, %s, %s, %s, %s",
                username, name, backButtonURL, backButtonText, searchBarText);
        return values;
    }

    private String makeInsertionQuery(){
        StringBuilder sb = new StringBuilder();
        sb.append("insert into instances values (");
        sb.append(toSQLValues());
        sb.append(")");

        return sb.toString();
    }

    public int save(){
        int numInserted = DBConnection.makeUpdate(makeInsertionQuery());
        return numInserted;
    }


    static class InstanceDBExtractor extends SimpleDBResultExtractor<Instance>{

        @Override
        public void extractInstancesFromDBResult(ResultSet rs) {
            try {
                while (rs.next()) {
                    Instance config = new Instance();
                    config.setName(rs.getString("name"));
                    config.setUsername(rs.getString("username"));
                    config.setBackButtonText(rs.getString("back-button-text"));
                    config.setBackButtonURL(rs.getString("back-button-url"));
                    config.setSearchBarText(rs.getString("set-search-bar-text"));
                    int videoConfigId = rs.getInt("videoSolrConfigID");
                    int blockConfigId = rs.getInt("blockSolrConfigID");
                    config.setVideoConfig(SolrConfig.fromID(videoConfigId));
                    config.setBlockConfig(SolrConfig.fromID(blockConfigId));

                    instances.add(config);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
