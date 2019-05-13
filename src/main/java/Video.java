import org.apache.commons.io.input.BOMInputStream;
import org.apache.solr.common.SolrDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class Video {

    public enum Source {
        YOUTUBE
    };

    private void copyAttributes(Video v){
        title       = v.title;
        description = v.description;
        thumbnail   = v.thumbnail;
        uploaded    = v.uploaded;
        channel     = v.channel;
        views       = v.views;
        likes       = v.likes;
    }

    static class VideoExtractor extends SimpleDBResultExtractor<Video>{

        @Override
        public void extractInstancesFromDBResult(ResultSet rs) throws SQLException {
            while(rs.next()){
                Video v = new Video(rs.getString("id"));
                v.indexedDate = LocalDate.parse(rs.getString("date-indexed"));
                v.lastUpdated = LocalDate.parse(rs.getString("last-updated"));
                v.instanceUsername = rs.getString("instance-username");
                instances.add(v);
            }
        }
    }

    public URL url;
    String id;
    String title;
    String description;
    String channel;
    String instanceUsername;
    String thumbnail;
    String captions;
    String uploaded;
    LocalDate indexedDate;
    LocalDate lastUpdated;
    List<VideoBlock> blocks;
    int likes;
    int views;

    Source source;

    Video(SolrDocument doc){
        title = (String) doc.getFieldValue("title_t");
        description = (String) doc.getFieldValue("description_t");
        //uploaded =  (String) doc.getFieldValue("uploaded_dt");
        likes = (int) doc.getFieldValue("likes_i");
        views = (int) doc.getFieldValue("views_i");
        channel = (String) doc.getFieldValue("channel_title_s");
        captions = (String) doc.getFieldValue("captions_t");
        id = (String) doc.getFieldValue("video_id_s");
        thumbnail = (String) doc.getFieldValue("thumbnail_s");
        if (thumbnail == null){
            thumbnail = String.format("http://img.youtube.com/vi/%s/0.jpg", id);
        }

        blocks = new ArrayList<>();
    }

    Video (String id, Source source){
        this.source = source;
        this.id = id;
        if(source == Source.YOUTUBE){
            try {
                url = new URL(String.format("http://youtube.com/watch?v=%s", id));
            } catch(Exception e){
                System.err.println("Couldn't form a proper url");
            }
        }
    }

    Video (String id){
        this.id = id;
    }

    Video (File srt) throws FileNotFoundException {
        id = srt.getName().substring(0, 11);
        source = Source.YOUTUBE;
        blocks = new ArrayList<VideoBlock>();
        BOMInputStream stream = new BOMInputStream(new FileInputStream(srt));
        Scanner sc = new Scanner(stream);
        sc.useDelimiter("\\Z");
        String content = sc.next();
        loadCaptionBlocks(content);
        captions = combineCaptionBlocksIntoString();
    }

    private String combineCaptionBlocksIntoString() {
        StringBuilder sb = new StringBuilder();
        for(VideoBlock b: getBlocks()){
            sb.append(b.getWords());
            sb.append(" ");
        }
        return sb.toString();
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

    private static String makeLastIndexedSQL(String instanceUsername){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * from \"indexed-videos\" where \"instance-username\"=");
        sb.append(String.format("'%s' ", instanceUsername));
        sb.append("ORDER BY \"date-indexed\" DESC ");
        sb.append("LIMIT 1");
        return sb.toString();
    }

    public static Video getLastIndexed(String instanceUsername){
        //TODO move to Instance as a non-static method.
        VideoExtractor extractor = new VideoExtractor();
        DBConnection.makeQuery(extractor, makeLastIndexedSQL(instanceUsername));
        if(extractor.getInstances().size()>0) {
            return extractor.getInstances().get(0);
        } else {
            return null;
        }
    }

    public boolean readyToBeIndexed(){
        boolean hasTitle, hasBlocks, hasThumbnail;

        hasTitle = getTitle() != null;
        hasBlocks = getBlocks() != null;
        hasThumbnail = getThumbnail() != null;



        return hasTitle && hasBlocks && hasThumbnail;
    }

    private String makeInsertSQL(){
        StringBuilder sb = new StringBuilder();
        sb.append("insert into \"indexed-videos\" VALUES (?, ?, ?, ?)");
        return sb.toString();
    }

    private Object[] toObjectArrayForSQL(){
        Object[] arr = new Object[4];
        arr[0] = getID();
        arr[1] = LocalDate.now();
        arr[2] = LocalDate.now();
        arr[3] = instanceUsername;
        return arr;
    }

    public void markAsHavingBeenIndexed(){
        Object[] sqlObjArray = toObjectArrayForSQL();
        String sql = makeInsertSQL();
        DBConnection.makeUpdate(sql, sqlObjArray);
    }

    public void unmarkAsHavingBeenIndexed(){
        //DBConnection
        //TODO only delete if instance-username is the same
        String parameterizedSQL = "DELETE from \"indexed-videos\" where id=? AND \"instance-username\"=?";
        DBConnection.makeUpdate(parameterizedSQL, id, instanceUsername);
    }

    public boolean hasBeenIndexedP(){
        //TODO check across instance-username as well
        VideoExtractor extractor = new VideoExtractor();
        String unformatedSQL = "SELECT * from \"indexed-videos\" where id='%s' AND \"instance-username\"='%s'";
        DBConnection.makeQuery(extractor, String.format(unformatedSQL, id, instanceUsername));
        if (extractor.getInstances().size() > 0){
            return true;
        }
        return false;
    }

    public boolean isFullyInstanciatedFromYT(){
        return id != null &&
                title != null &&
                description != null &&
                thumbnail != null &&
                uploaded != null &&
                channel != null;
    }

}
