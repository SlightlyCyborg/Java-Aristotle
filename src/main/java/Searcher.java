import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import javax.management.Query;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class Searcher {
    private SolrClient videoConnection, blockConnection;



    Searcher(SolrClient videoConnection, SolrClient blockConnection){
        this.videoConnection = videoConnection;
        this.blockConnection = blockConnection;
    }

    SearchResult search(String searchText) throws IOException, SolrServerException {
        SearchResult rv = new SearchResult();

        QueryResponse videoResponse = searchVideos(searchText); //make a network request
        SolrDocumentList videoDocs = videoResponse.getResults();
        for(Iterator<SolrDocument> it = videoDocs.iterator(); it.hasNext();){
            Video videoToAdd = new Video(it.next());
            QueryResponse blockResponse = searchBlocks(videoToAdd, searchText); //makes a network request
            SolrDocumentList blockDocs = blockResponse.getResults();

            for(Iterator<SolrDocument> blockIt = blockDocs.iterator(); blockIt.hasNext();){
                VideoBlock block = new VideoBlock(blockIt.next());
                videoToAdd.addBlock(block);
            }
            rv.addVideo(videoToAdd);
        }

        //code is bad because it makes videoDocs.size()+1 number of network requests

        return rv;
    }

    QueryResponse searchVideos(String searchText) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(improveQ(searchText));
        return videoConnection.query(query);
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

    QueryResponse searchBlocks(Video context, String searchText) throws IOException, SolrServerException {
        SolrQuery blockQuery = new SolrQuery();
        String q = makeBlockQ(context, searchText);
        blockQuery.set("q", q);
        blockQuery.set("sort", "start_time_s asc");
        blockQuery.set("h", "on");
        blockQuery.set("hl", "on");
        blockQuery.set("hl-fl", "viewable_words_t");

        return blockConnection.query(blockQuery);
    }

    String makeBlockQ(Video context, String searchTerm){
        StringBuilder rv = new StringBuilder();
        rv.append("captions_t:\"");
        rv.append(searchTerm);
        rv.append("\"");
        rv.append(" AND video_id_s:\"");
        rv.append(context.getID());
        rv.append("\"");
        return rv.toString();
    }
}
