import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class YouTubeURL {

    private YouTubeChannelVideoSource.ID_Type type;
    private String identifier;

    YouTubeURL(String urlToParse) throws MalformedURLException {
        URL url = new URL(urlToParse);
        String path = url.getPath();
        String[] segments = path.split("\\/");
        int index = findTypeSegmentIndex(segments);
        if(index>-1 && segments.length>=index+2){
            identifier = segments[index+1];
        }
        type = getType(segments[index]);
    }

    private YouTubeChannelVideoSource.ID_Type getType(String type) {
        switch (type){
            case "channel":
                return YouTubeChannelVideoSource.ID_Type.UUID;
            case "user":
                return YouTubeChannelVideoSource.ID_Type.USERNAME;
        }
        return null;
    }

    private static int findTypeSegmentIndex(String[] segments) {
        for(int i=0; i<segments.length; i++){
            switch(segments[i]){
                case "channel":
                    return i;
                case "user":
                    return i;
            }
        }
        return -1;
    }

    public YouTubeURL(String id, YouTubeChannelVideoSource.ID_Type type){
        this.identifier = id;
        this.type = type;
    }

    public String identifier() {
        return identifier;
    }

    public YouTubeChannelVideoSource.ID_Type identifierType() {
        return type;
    }

    //DB Stuff

    static class YouTubeURLExtractor extends SimpleDBResultExtractor<YouTubeURL>{

        @Override
        public void extractInstancesFromDBResult(ResultSet rs) throws SQLException {
            while(rs.next()){
                String id = rs.getString("id");
                String typeStr = rs.getString("id-type");
                YouTubeChannelVideoSource.ID_Type type = YouTubeChannelVideoSource.ID_Type.valueOf(typeStr);

                YouTubeURL url = new YouTubeURL(id, type);
                instances.add(url);
            }
        }
    }

    public void saveToDBForUsername(String username){
        String sql = "insert into \"youtube-urls\" values (?, ?, ?)";
        DBConnection.makeUpdate(sql, username, identifier(), identifierType().name());
    }

    public static List<YouTubeURL> getForUsername(String username){
      String sql = String.format("select * from \"youtube-urls\" where \"instance-username\"='%s'", username);
      YouTubeURLExtractor extractor = new YouTubeURLExtractor();
      DBConnection.makeQuery(extractor, sql);
      return extractor.getInstances();
    }
}
