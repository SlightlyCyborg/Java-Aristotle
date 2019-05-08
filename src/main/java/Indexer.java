import com.google.common.io.Files;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Indexer {

    private SolrClient videoConnection, blockConnection;

    private List<VideoSource> videoSources;

    Instance instance;

    Indexer(Instance instance, SolrClient videoConnection, SolrClient blockConnection){
        this.instance = instance;
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
        UpdateResponse videoUpdateResponse = videoConnection.commit();
        blockConnection.commit();

        markVideosAsIndexIfSuccessful(videoUpdateResponse, videos);
    }

    private void markVideosAsIndexIfSuccessful(UpdateResponse response, List<Video> videos) {
        if(response.getStatus() == 0){
            for(Video v: videos){
                v.markAsHavingBeenIndexed();
            }
        }
    }

    private void indexVideoBlocks(Video v) throws IOException, SolrServerException {
        List<VideoBlock> blocks = v.getBlocks();

        Iterator<VideoBlock> it = blocks.iterator();
        for(it=blocks.iterator(); it.hasNext();) {
            VideoBlock toBeIndexed = it.next();
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", String.format("%s-%s", v.id, toBeIndexed.id));
            doc.addField("video_id_s", v.id);
            doc.addField("captions_t", toBeIndexed.words);
            //Viewable words should work differently.
            //:viewable_words_t (block ::viewable-words) video.clj:103
            doc.addField("start_time_s", toBeIndexed.startTime.toString());
            doc.addField("stop_time_s", toBeIndexed.stopTime.toString());
            blockConnection.add(doc);
        }
    }

    void indexFullVideos(List<Video> videos) throws IOException, SolrServerException {

        for(Iterator<Video> it = videos.iterator(); it.hasNext();){
            Video vidToIndex = it.next();
            vidToIndex.instanceUsername = instance.getUsername();
            SolrInputDocument documentToIndex = new SolrInputDocument();
            documentToIndex.addField("video_id_s", vidToIndex.id);
            documentToIndex.addField("title_t", vidToIndex.title);
            documentToIndex.addField("description_t", vidToIndex.description);
            documentToIndex.addField("uploaded_dt", vidToIndex.uploaded);
            documentToIndex.addField("likes_i", vidToIndex.likes);
            documentToIndex.addField("views_i", vidToIndex.views);
            documentToIndex.addField("channel_title_s", vidToIndex.channel);
            documentToIndex.addField("captions_t", vidToIndex.captions);
            documentToIndex.addField("thumbnail_s", vidToIndex.thumbnail);
            documentToIndex.addField("instance_username_ss", vidToIndex.instanceUsername);

            videoConnection.add(documentToIndex);
        }
    }

    public static void writeURLsToFile(List<URL> urls, File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<urls.size(); i++){
           sb.append(urls.get(i).toString());
           if(i != urls.size()-1) {
               sb.append("\n");
           }
        }

        FileWriter writer = new FileWriter(f);
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }

    public File downloadNewCaptionsAsOfDate(LocalDate latest){
        List<URL> urls = getUrlsToIndexAsOfDate(latest);
        File src = null;
        File captionDir = null;
        try {
            src = File.createTempFile("urls", ".tmp");
            captionDir = Files.createTempDir();
            writeURLsToFile(urls, src);
            //Runner runner = new Runner(src.getAbsolutePath(), captionDir.getAbsolutePath());
            //runner.run();
            Process proc = Runtime.getRuntime().exec(String.format("java -jar SubtitleDownloader.jar -i %s -o %s",
                    src.getAbsolutePath(), captionDir.getAbsolutePath()));
            InputStream out = proc.getInputStream();
            InputStream err = proc.getErrorStream();

            StringBuilder stdOut = new StringBuilder();
            StringBuilder stdErr = new StringBuilder();
            while(proc.isAlive()){
                if(out.available()>0) {
                    stdOut.appendCodePoint(out.read());
                }
                if(err.available()>0) {
                    stdErr.appendCodePoint(err.read());
                }
            }
            return captionDir;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(src != null) {
                src.delete();
            }
        }

        return null;
    }

    public void indexAllSinceDate(LocalDate latest) throws IOException, SolrServerException, GeneralSecurityException {
        
        File captionDir = downloadNewCaptionsAsOfDate(latest);
        List<File> captionsToIndex = getRelevantCaptionsFromDir(captionDir);
        List<Video> videos = new ArrayList<>();
        for(File srt: captionsToIndex){
            String videoId = getIdFromCaptionFile(srt);
            Video v = new Video(srt);
            v.initializeFromId();
            v.source = Video.Source.YOUTUBE;
            videos.add(v);

        }
        index(videos);
    }

    private String getIdFromCaptionFile(File caption) {
            String name = caption.getName();
            String id = name.substring(0, 11);
            return id;
    }

    private static boolean isEnglishSub(String name){
        return name.matches(".+_en\\..+");
    }

    private static boolean isSRTVersion(int versionNo, String filename){
        return versionNo == Integer.parseInt(filename.substring(12, 13));
    }

    private List<File> getRelevantCaptionsFromDir(File captionDir) {
        File[] files = captionDir.listFiles();

        List<File> rv = new ArrayList<>();

        for(int i=0; i<files.length; i++){
            if(isEnglishSub(files[i].getName()) &&
                    files[i].isFile() &&
                    isSRTVersion(0, files[i].getName())){
                rv.add(files[i]);
            }
        }
        return rv;
    }


    public List<URL> getUrlsToIndex(){
        LocalDate latestIsPresentDate = LocalDate.now();
        return getUrlsToIndexAsOfDate(latestIsPresentDate);
    }

    public List<URL> getUrlsToIndexAsOfDate(LocalDate latest){
        ArrayList<URL> urls = new ArrayList<>();

        for(VideoSource source: videoSources){
            Video lastIndexed = Video.getLastIndexed(instance.getUsername());
            LocalDate indexedDate;
            if(lastIndexed == null){
                indexedDate = LocalDate.parse("1999-12-31");
            } else {
                indexedDate = lastIndexed.indexedDate;
            }
            List<Video> videos = source.getVideos(indexedDate, latest);
            for(Video video: videos){
                urls.add(video.getUrl());
            }
        }

        return urls;
    }

    public void delete(List<Video> videos) {
    }

    public void saveSources(){
        for(VideoSource source: videoSources) {
            if (source instanceof YouTubeChannelVideoSource){
                YouTubeURL url = ((YouTubeChannelVideoSource) source).getYouTubeURL();
                url.saveToDBForUsername(instance.getUsername());
            }
        }
    }
}
