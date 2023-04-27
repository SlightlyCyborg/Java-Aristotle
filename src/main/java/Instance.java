import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Instance {
    
    YouTubeProfileImageSource imageSource;

    URL solrVideoURL, solrBlockURL;

    List<VideoSource> videoSources;

    List<URL> urlsToIndex;

    Searcher searcher;
    Indexer indexer;
    Renderer renderer;

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

    public Instance(Admin.InstanceConfig config) throws MalformedURLException {
        setUsername(config.username);
        setName(config.name);
        setBackButtonURL(config.backButtonUrl);
        setBackButtonText(config.backButtonText);
        setSearchBarText(config.searchBarText);
        active = true;

        solrVideo = new SolrConfig();
        solrVideo.setCore("videos");

        solrBlock = new SolrConfig();
        solrBlock.setCore("blocks");

        initializeSolr(solrVideo, solrBlock);
        renderer = Renderer.getInstance();

        try {
            YouTubeURL url = new YouTubeURL(config.youtubeUrl);
            indexer.addVideoSource(YouTubeChannelVideoSource.getByYouTubeURL(url));
            imageSource = YouTubeProfileImageSource.fromYouTubeURL(url);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static List<Instance> fromDB() throws Exception {
        InstanceDBExtractor extractor = new InstanceDBExtractor();

        String sql = "SELECT * from instances where active=true";

        try {
        	DBConnection.makeQuery(extractor, sql);
        } catch (DBConnection.DBQueryException e){
        	throw new Exception("unable to get instances from db");
        }

        List<Instance> instances = extractor.getInstances();
        return instances;
    }
    
    public static Instance fromDB(String username) throws Exception {
    	InstanceDBExtractor extractor = new InstanceDBExtractor();

        String unformated = "SELECT * from instances where active=true AND username='%s'";
        String sql = String.format(unformated, username);

        try {
        	DBConnection.makeQuery(extractor, sql);
        } catch (DBConnection.DBQueryException e){
        	throw new Exception("unable to get instances from db");
        }

        List<Instance> instances = extractor.getInstances();
        try {
        	return instances.get(0);
        } catch(Exception e) {
        	throw new Exception("instance isn't in DB");
        }

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


        searcher = new Searcher(username, videoConnection, blockConnection);
        indexer = new Indexer(this, videoConnection, blockConnection);
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
        indexer.saveSources();
        return numInserted;
    }

    public String getImgSource(){
        return imageSource.getProfileImageURL().toString();
    }

    static class InstanceDBExtractor extends SimpleDBResultExtractor<Instance>{

        @Override
        public void extractInstancesFromDBResult(ResultSet rs) {
            try {
                while (rs.next()) {
                    Instance instance = new Instance();
                    instance.setName(rs.getString("name"));
                    instance.setUsername(rs.getString("username"));
                    instance.setBackButtonText(rs.getString("backButtonText"));
                    instance.setBackButtonURL(rs.getString("backButtonURL"));
                    instance.setSearchBarText(rs.getString("searchBarText"));
                    int videoConfigId = rs.getInt("videoSolrConfigID");
                    int blockConfigId = rs.getInt("blockSolrConfigID");
                    SolrConfig solrVideo = SolrConfig.fromID(videoConfigId);
                    SolrConfig solrBlock = SolrConfig.fromID(blockConfigId);

                    instance.initializeSolr(solrVideo, solrBlock);

                    List<YouTubeURL> sourceUrls = YouTubeURL.getForUsername(instance.getUsername());
                    for(YouTubeURL url: sourceUrls){
                        try {
                            instance.indexer.addVideoSource(YouTubeChannelVideoSource.getByYouTubeURL(url));
                        } catch (GeneralSecurityException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if(sourceUrls.size() > 0){
                        YouTubeURL url = sourceUrls.get(0);
                        switch(url.identifierType()){
                            case USERNAME:
                                instance.imageSource = YouTubeProfileImageSource.fromUsername(url.identifier());
                                break;
                            case UUID:
                                instance.imageSource = YouTubeProfileImageSource.fromChannelID(url.identifier());
                                break;
                        }
                    }

                    instances.add(instance);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

	public void removeFromDB(String username) {
		String query = "DELETE from instances WHERE username=?";
		DBConnection.makeUpdate(query, username);
	}
}
