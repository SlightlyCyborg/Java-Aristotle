import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class Searcher {
    public static class SolrConfig{
        boolean ssl = false;
        String username;
        String password;
        String host;
        String core;
        int port;

        SolrConfig(){}

        SolrConfig(SolrConfig toBeCopied){
            ssl = toBeCopied.ssl;
            username = toBeCopied.username;
            password = toBeCopied.password;
            host = toBeCopied.host;
            core = toBeCopied.core;
            port = toBeCopied.port;
        }

        URL getURL() throws MalformedURLException {
            String protocol;
            if(ssl){
                protocol = "https";
            } else {
                protocol = "http";
            }

            URL rv = new URL(
                    protocol,
                    host,
                    port,
                    "/solr/"+core
            );

            return rv;
        }
    }

    SolrClient videoConnection, blockConnection;
    SolrConfig videoConfig, blockConfig;

    Searcher(SolrConfig videoConfig, SolrConfig blockConfig) throws MalformedURLException {
        this.videoConfig = videoConfig;
        this.blockConfig = blockConfig;

        videoConnection = new HttpSolrClient.Builder(videoConfig.getURL().toString()).build();
        blockConnection = new HttpSolrClient.Builder(blockConfig.getURL().toString()).build();
    }

    SearchResult search(String searchText) throws IOException, SolrServerException {
        SearchResult rv = new SearchResult();

        QueryResponse videoResponse = searchVideos(searchText); //make a network request
        SolrDocumentList videoDocs = videoResponse.getResults();
        for(Iterator<SolrDocument> it = videoDocs.iterator(); it.hasNext();){
            Video toAdd = new Video(it.next());
            searchBlocks(toAdd, searchText); //makes a network request
            rv.addVideo(toAdd);
        }

        //code is bad because it makes videoDocs.size()+1 number of network requests

        return rv;
    }

    QueryResponse searchVideos(String searchText) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(improveQ(searchText));
        return videoConnection.query(query);
    }

    void searchBlocks(Video context, String searchText){

    }

    static String improveQ(String q){
        StringBuilder rv = new StringBuilder();
        rv.append("title_t:\"");
        rv.append(q);
        rv.append("\"^10 ");

        rv.append("captions_t:\"");
        rv.append(q);
        rv.append("\" ");

        rv.append("\"");
        rv.append(q);
        rv.append("\"~10000^5");

        return rv.toString();
    }
}
