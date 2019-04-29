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

    Instance() throws MalformedURLException {
        renderer = Renderer.getInstance();
        initializeSQL();
    }

    public String home(){
        return renderer.home();
    }

    public String search(String searchText) throws IOException, SolrServerException {
        SearchResult result = searcher.search(searchText);
        return renderer.search(result);
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


    String getName(){
        return name;
    }

    void setName(String name){
        this.name = name;
    }

    String getUsername(){
        return username;
    }

    void setUsername(String username){
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
}
