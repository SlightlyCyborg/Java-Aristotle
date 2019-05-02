import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import subtitleDownloader.Runner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
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

    InstanceConfig config;

    Instance() throws MalformedURLException {
        config = new InstanceConfig();
        renderer = Renderer.getInstance();
        initializeSQL();
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

        config.setVideoConfig(videoConfig);
        config.setBlockConfig(blockConfig);

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

    public void save(){
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

    public String getBackButtonURL() {
        return config.getBackButtonURL();
    }

    public void setBackButtonURL(String backButtonURL) {
        config.setBackButtonURL(backButtonURL);
    }

    public String getBackButtonText() {
        return config.getBackButtonText();
    }

    public void setBackButtonText(String backButtonText) {
        config.setBackButtonText(backButtonText);
    }

    public String getSearchBarText() {
        return config.getSearchBarText();
    }

    public void setSearchBarText(String searchBarText) {
        config.setSearchBarText(searchBarText);
    }


}
