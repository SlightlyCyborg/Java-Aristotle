import org.apache.commons.io.input.BOMInputStream;
import org.apache.solr.common.SolrDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Video {

    public URL url;
    String id;
    String title;
    String description;
    String channel;
    String thumbnail;
    String captions;
    String uploaded;
    List<VideoBlock> blocks;
    int likes;
    int views;

    Video(SolrDocument doc){
        title = (String) doc.getFieldValue("title_t");
        description = (String) doc.getFieldValue("description_t");
        //uploaded =  (String) doc.getFieldValue("uploaded_dt");
        likes = (int) doc.getFieldValue("likes_i");
        views = (int) doc.getFieldValue("views_i");
        channel = (String) doc.getFieldValue("channel_title_s");
        captions = (String) doc.getFieldValue("captions_t");
        thumbnail = (String) doc.getFieldValue("thumbnail_s");
        id = (String) doc.getFieldValue("id");

        blocks = new ArrayList<>();
    }

    Video (String id){
        this.id = id;
    }

    Video (File srt) throws FileNotFoundException {
        id = srt.getName().substring(0, 11);
        blocks = new ArrayList<VideoBlock>();
        captions = "";
        BOMInputStream stream = new BOMInputStream(new FileInputStream(srt));
        Scanner sc = new Scanner(stream);
        sc.useDelimiter("\\Z");
        String content = sc.next();
        loadCaptionBlocks(content);
    }

    void addBlock(VideoBlock block){
        this.blocks.add(block);
    }

    void loadCaptionBlocks(String content){
        String[] unparsedBlocks = content.split("\r\n\r\n");
        for(int i = 0; i < unparsedBlocks.length; i++){
            String[] parsedBlock = unparsedBlocks[i].split("\r\n");
            String id, words, time;
            id = parsedBlock[0];
            time = parsedBlock[1];
            words = parsedBlock[2] ;
            blocks.add(new VideoBlock(id, words, time));
        }
    }

    String getID(){
        return id;
    }

    public List<VideoBlock> getBlocks() {
        return blocks;
    }

    public URL getUrl(){
        return url;
    }

    public String getThumbnail(){
        return thumbnail;
    }

    public String getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }
}
