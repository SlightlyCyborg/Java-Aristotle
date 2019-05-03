import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Indexer {

    private SolrClient videoConnection, blockConnection;

    private List<VideoSource> videoSources;

    String instanceUsername;

    Indexer(String instanceUsername, SolrClient videoConnection, SolrClient blockConnection){
        this.instanceUsername = instanceUsername;
        videoSources = new ArrayList<VideoSource>();
        this.videoConnection = videoConnection;
        this.blockConnection = blockConnection;
    }

    void addVideoSource(VideoSource source){
        videoSources.add(source);
    }

    void index(List<Video> videos) throws IOException, SolrServerException {
        indexFullVideos(videos);
        for(Iterator<Video> it=videos.iterator(); it.hasNext();){
            indexVideoBlocks(it.next());
        }
    }

    private void indexVideoBlocks(Video v) {
        List<VideoBlock> blocks = v.getBlocks();

        Iterator<VideoBlock> it = blocks.iterator();
        for(it=blocks.iterator(); it.hasNext();) {
            VideoBlock toBeIndexed = it.next();
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", String.format("%s-%d", v.id, toBeIndexed.id));
            doc.addField("video_id_s", v.id);
            doc.addField("captions_t", toBeIndexed.words);
            //Viewable words should work differently.
            //:viewable_words_t (block ::viewable-words) video.clj:103
            doc.addField("start_time_s", toBeIndexed.startTime.toString());
            doc.addField("stop_time_s", toBeIndexed.stopTime.toString());
        }
    }

    void indexFullVideos(List<Video> videos) throws IOException, SolrServerException {

        for(Iterator<Video> it = videos.iterator(); it.hasNext();){
            Video vidToIndex = it.next();
            SolrInputDocument documentToIndex = new SolrInputDocument();
            documentToIndex.addField("title_t", vidToIndex.title);
            documentToIndex.addField("description_t", vidToIndex.description);
            documentToIndex.addField("uploaded_dt", vidToIndex.uploaded);
            documentToIndex.addField("likes_i", vidToIndex.likes);
            documentToIndex.addField("views_i", vidToIndex.views);
            documentToIndex.addField("channel_title_s", vidToIndex.channel);
            documentToIndex.addField("captions_t", vidToIndex.captions);
            documentToIndex.addField("thumbnail_s", vidToIndex.thumbnail);

            videoConnection.add(documentToIndex);
        }
    }

    public List<URL> getUrlsToIndex(){
        LocalDate latestIsPresentDate = LocalDate.now();
        return getUrlsToIndexAsOfDate(latestIsPresentDate);
    }

    public List<URL> getUrlsToIndexAsOfDate(LocalDate latest){
        ArrayList<URL> urls = new ArrayList<>();

        for(VideoSource source: videoSources){
            Video lastIndexed = Video.getLastIndexed(instanceUsername);
            List<Video> videos = source.getVideos(lastIndexed.indexedDate, latest);
            for(Video video: videos){
                urls.add(video.getUrl());
            }
        }

        return urls;
    }
}
