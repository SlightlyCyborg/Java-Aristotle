import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InstanceConfig {

    private String name, username;
    private String backButtonURL, backButtonText, searchBarText;

    SolrConfig videoConfig, blockConfig;





    private String toSQLValues(){
        String values = String.format("%s, %s, %s, %s, %s",
                username, name, backButtonURL, backButtonText, searchBarText);
        return values;
    }

    private String makeInsertionQuery(){
        StringBuilder sb = new StringBuilder();
        sb.append("insert into instances values (");
        sb.append(toSQLValues());
        sb.append(")");

        return sb.toString();
    }

    public int insertIntoDB(){
        int numInserted = DBConnection.makeUpdate(makeInsertionQuery());
        return numInserted;
    }

    /*
     Getters and Setters
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBackButtonURL() {
        return backButtonURL;
    }

    public void setBackButtonURL(String backButtonURL) {
        this.backButtonURL = backButtonURL;
    }

    public String getBackButtonText() {
        return backButtonText;
    }

    public void setBackButtonText(String backButtonText) { this.backButtonText = backButtonText; }

    public String getSearchBarText() {
        return searchBarText;
    }

    public void setSearchBarText(String searchBarText) {
        this.searchBarText = searchBarText;
    }

    public void setVideoConfig(SolrConfig videoConfig) { this.videoConfig = videoConfig; }

    public SolrConfig getVideoConfig(){ return videoConfig; }

    public void setBlockConfig(SolrConfig blockConfig) { this.blockConfig = blockConfig; }

    public SolrConfig getBlockConfig(){ return blockConfig; }
}
