import com.google.common.io.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tomcat.jni.Time;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.*;

public class Indexer {
	
    Logger log = LogManager.getLogger();

    private SolrClient videoConnection, blockConnection;

    private List<VideoSource> videoSources;

    Instance instance;
    
    int BATCH_SIZE = 100;

    Indexer(Instance instance, SolrClient videoConnection, SolrClient blockConnection){
    	log.info("Creating Indexer for instance: {}", instance.getName());
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
        UpdateResponse videoUpdateResponse = videoConnection.commit();
        for(Iterator<Video> it=videos.iterator(); it.hasNext();){
            indexVideoBlocks(it.next());
            blockConnection.commit();
            try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				log.warn("cant sleep for 50ms after each video block commit");
			}
        }

        markVideosAsIndexIfSuccessful(videoUpdateResponse, videos);
    }

    private void markVideosAsIndexIfSuccessful(UpdateResponse response, List<Video> videos) {
        if(response.getStatus() == 0){
            for(Video v: videos){
                if(v.readyToBeIndexed()) {
                    v.markAsHavingBeenIndexed();
                }
            }
        }
    }

    private void indexVideoBlocks(Video v) throws IOException, SolrServerException {
        if(v.readyToBeIndexed()){
            List<VideoBlock> blocks = v.getBlocks();
            Iterator<VideoBlock> it = blocks.iterator();

            List<SolrInputDocument> docs = new ArrayList<>();

            for (it = blocks.iterator(); it.hasNext(); ) {
                VideoBlock toBeIndexed = it.next();
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", String.format("%s-%s", v.id, toBeIndexed.id));
                doc.addField("video_id_s", v.id);
                doc.addField("captions_t", toBeIndexed.words);
                //Viewable words should work differently.
                //:viewable_words_t (block ::viewable-words) video.clj:103
                doc.addField("start_time_s", toBeIndexed.startTime.toString());
                doc.addField("stop_time_s", toBeIndexed.stopTime.toString());
                docs.add(doc);
            }
            blockConnection.add(docs);
        }
    }

    void indexFullVideos(List<Video> videos) throws IOException, SolrServerException {
    	log.info("indexing Video objects");
    	
        List<SolrInputDocument> docs = new ArrayList<>();

        for(Iterator<Video> it = videos.iterator(); it.hasNext();){
            Video vidToIndex = it.next();
            if(vidToIndex.readyToBeIndexed()) {
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
                docs.add(documentToIndex);
            }
        }
        if(docs.size() > 0) {
            videoConnection.add(docs);
        }
        log.info("added Video docs");
    }

    public static Map<String, List<String>> getQueryParams(String url) throws Exception {
        try {
            Map<String, List<String>> params = new HashMap<String, List<String>>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }

                    List<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        params.put(key, values);
                    }
                    values.add(value);
                }
            }

            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    public static void writeURLsToFile(List<URL> urls, File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<urls.size(); i++){
            try {
                Map<String, List<String>> params = getQueryParams(urls.get(i).toString());
                String id = params.get("v").get(0);
                sb.append(id);
                if (i != urls.size() - 1) {
                    sb.append("\n");
                }
            } catch (Exception e){}
        }

        FileWriter writer = new FileWriter(f);
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }

    public File downloadCaptionUrls(List<URL> urls){
        File src = null;
        File captionDir = null;
        try {
            src = File.createTempFile("urls", ".tmp");
            captionDir = Files.createTempDir();
            writeURLsToFile(urls, src);
            //Runner runner = new Runner(src.getAbsolutePath(), captionDir.getAbsolutePath());
            //runner.run();
            log.info("Downloading captions for {}", instance.getName());
            Process proc = Runtime.getRuntime().exec(String.format("python3 SubtitleDownloader.py -i %s -o %s",
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
                
                if(stdOut.length()>0 && stdOut.charAt(stdOut.length()-1) == '\n') {
                	log.debug("captionDL: " + stdOut.toString());
                	stdOut = new StringBuilder();
                }
                if(stdErr.length()>0 && stdErr.charAt(stdErr.length()-1) == '\n') {
                	log.warn("captionDL: " + stdErr.toString());
                	stdErr = new StringBuilder();
                }
            }
            log.info("Finished Downloading Captions");
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
    
    private <T> List<List<T>> batchify(List<T> toBeBatched, int batchSize){
    	List<List<T>> rv = new ArrayList<>();
    	for(int i=0; i<toBeBatched.size(); i++) {
    		int batch = i/batchSize;
    		if(i%batchSize == 0){
    			rv.add(new ArrayList<T>());
    		}
    		rv.get(batch).add(toBeBatched.get(i));
    	}
    	return rv;
    }

    public void indexAllSinceDate(LocalDate latest) throws IOException, SolrServerException, GeneralSecurityException {
    	String logMsg;
        List<URL> urls = getUrlsToIndexAsOfDate(latest);
    	List<List<URL>> batches = batchify(urls, BATCH_SIZE);
    	for(List<URL> batch: batches) {
    		File captionDir = downloadCaptionUrls(batch);
    		List<File> captionsToIndex = getRelevantCaptionsFromDir(captionDir);
    		List<Video> videos = new ArrayList<>();
    		for(File srt: captionsToIndex){
    			Video v = new Video(srt);
    			v.source = Video.Source.YOUTUBE;
    			videos.add(v);
    		}
    		logMsg = "finished loading srts into Video objects for {}";
    		log.info(logMsg, instance.getName());
    		YouTubeChannelVideoSource.initializeDetailsForVideos(videos);
    		logMsg = "finished initializingDetailsForVideos for instance {}";
    		log.info(logMsg);
    		index(videos);
    	}
    }

    private static boolean isEnglishSub(String name){
        return name.matches(".+_en\\..+");
    }

    private static boolean isSRTVersion(int versionNo, String filename){
        return versionNo == Integer.parseInt(filename.substring(12, 13));
    }

    private List<File> getRelevantCaptionsFromDir(File captionDir) {
        File[] files = captionDir.listFiles();
        return Arrays.stream(files).toList();
    }


    public List<URL> getUrlsToIndex(){
        LocalDate latestIsPresentDate = LocalDate.now();
        return getUrlsToIndexAsOfDate(latestIsPresentDate);
    }

    public List<URL> getUrlsToIndexAsOfDate(LocalDate latest){
    	log.info("fetching urlsToIndex for {} as of {}", instance.getName(), latest);
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
