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
import java.time.LocalDate;
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
    private SolrConfig solrBlock;
    private SolrConfig solrVideo;
    private boolean active = true;

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

    static String activeInstanceSQLStr;

    public static List<Instance> fromDB() throws MalformedURLException {
        InstanceDBExtractor extractor = new InstanceDBExtractor();

        String sql = "SELECT * from instances where active=true";

        DBConnection.makeQuery(extractor, sql);
        List<Instance> instances = extractor.getInstances();

        return instances;
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

        //Needed for saving
        setVideoConfig(videoConfig);
        setBlockConfig(blockConfig);

        SolrClient videoConnection = new HttpSolrClient.Builder(videoConfig.getURL().toString()).build();
        SolrClient blockConnection = new HttpSolrClient.Builder(blockConfig.getURL().toString()).build();


        searcher = new Searcher(videoConnection, blockConnection);
        indexer = new Indexer(username, videoConnection, blockConnection);
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

    private LocalDate getLastIndexedVideoFromSource(String sourceId) {
        return LocalDate.now();
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

    private void setBlockConfig(SolrConfig config) {
        solrBlock = config;
    }

    private void setVideoConfig(SolrConfig config) {
        solrVideo = config;
    }

    private String toSQLValues(){
        String values = String.format("'%s', '%s', '%s', '%s', '%s', %d, %d, %b",
                username, name, backButtonURL, backButtonText, searchBarText,
                solrVideo.getId(), solrBlock.getId(), true);
        return values;
    }

    private String makeInsertionQuery(){
        StringBuilder sb = new StringBuilder();
        sb.append("insert into instances values (");
        sb.append("?, ?, ?, ?, ?, ?, ?, ?");
        sb.append(")");

        return sb.toString();
    }

    public int save(){
        if(solrVideo == null || solrBlock == null){
            throw new IllegalStateException("An instance without initialized solr configs cannot be saved");
        }
        if(!solrVideo.isAlreadyInDB()){
            solrVideo.saveToDB();
        }
        if(!solrBlock.isAlreadyInDB()){
            solrBlock.saveToDB();
        }
        int numInserted = DBConnection.makeUpdate(makeInsertionQuery(),
                username, name, backButtonURL, backButtonText, searchBarText,
                solrVideo.getId(), solrBlock.getId(), active);
        return numInserted;
    }


    static class InstanceDBExtractor extends SimpleDBResultExtractor<Instance>{

        @Override
        public void extractInstancesFromDBResult(ResultSet rs) {
            try {
                while (rs.next()) {
                    Instance video = new Instance();
                    video.setName(rs.getString("name"));
                    video.setUsername(rs.getString("username"));
                    video.setBackButtonText(rs.getString("backButtonText"));
                    video.setBackButtonURL(rs.getString("backButtonURL"));
                    video.setSearchBarText(rs.getString("searchBarText"));
                    int videoConfigId = rs.getInt("videoSolrConfigID");
                    int blockConfigId = rs.getInt("blockSolrConfigID");
                    SolrConfig solrVideo = SolrConfig.fromID(videoConfigId);
                    SolrConfig solrBlock = SolrConfig.fromID(blockConfigId);

                    video.initializeSolr(solrVideo, solrBlock);

                    instances.add(video);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
