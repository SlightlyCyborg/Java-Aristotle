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
        String unparsedId = segments[segments.length-1];
        boolean isUsername = unparsedId.charAt(0) == ('@');
        if(isUsername) {
           this.identifier = unparsedId.substring(1);
           this.type = YouTubeChannelVideoSource.ID_Type.USERNAME;
        } else {
            this.identifier = unparsedId.substring(0);
            this.type = YouTubeChannelVideoSource.ID_Type.UUID;
        }
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
      try {
      DBConnection.makeQuery(extractor, sql);
      } catch (DBConnection.DBQueryException e) {
    	  
      }
      return extractor.getInstances();
    }
}
