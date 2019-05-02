import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SolrConfig {

    public static class SolrConfigExtractor implements DBResultExtractor<SolrConfig>{

        List<SolrConfig> configs;

        @Override
        public void extractInstancesFromDBResult(ResultSet rs) throws SQLException {
            configs = new ArrayList<>();
            while(rs.next()){
                SolrConfig config = new SolrConfig();
                config.setHost(rs.getString("host"));
                config.setCore(rs.getString("core"));
                config.setPort(rs.getInt("port"));
                config.setSSL(rs.getBoolean("ssl"));
                config.setId(rs.getInt("id"));
                configs.add(config);
            }
        }

        @Override
        public List<SolrConfig> getInstances() {
            return configs;
        }
    }

    boolean ssl = false;
    private String host;
    private String core;
    private int port = 8983;
    private int id = -1;

    SolrConfig(){}

    SolrConfig(SolrConfig toBeCopied){
        ssl = toBeCopied.ssl;
        host = toBeCopied.host;
        core = toBeCopied.core;
        port = toBeCopied.port;
    }

    public int getId(){return id;}

    //Private because this field is only for DB stuff.
    private void setId(int id){this.id = id;}

    private boolean hasValidID(){
        if(getId()<0){
            return false;
        }
        return true;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setCore(String core) {
        this.core = core;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSSL(boolean ssl) {
        this.ssl = ssl;
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

    private String makeInsertQuery(){
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO \"solr-configs\"");
        sb.append(" (host, core, port, ssl) VALUES ");

        sb.append(String.format("('%s', '%s', '%s', '%s')",
                host, core, port, ssl));

        return sb.toString();
    }

    public boolean saveToDB(){
        if(!isAlreadyInDB()) {
            DBConnection.makeUpdate(makeInsertQuery());
            setId(getDBSingleton(this).id);
            return true;
        }
        return false;
    }

    public boolean isAlreadyInDB(){
        if(hasValidID()) {
            return true;
        }

        SolrConfig config = getDBSingleton(this);
        if (config != null){
            id = config.id;
            return true;
        }
        return false;
    }

    private static SolrConfig getDBSingleton(SolrConfig config){
        SolrConfigExtractor extractor = new SolrConfigExtractor();
        String query = makeAlreadyInDBQuery(config);
        DBConnection.makeQuery(extractor, query);
        List<SolrConfig> configs = extractor.getInstances();
        if(configs.size()>0){
            return configs.get(0) ;
        }
        return null;
    }

    private static String makeAlreadyInDBQuery(SolrConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * from \"solr-configs\" WHERE ");
        sb.append(String.format("host='%s' AND core='%s' AND port=%d AND ssl=%b",
                config.host, config.core, config.port, config.ssl ));
        return sb.toString();
    }


    public boolean deleteFromDB() {
        if(isAlreadyInDB()){
            DBConnection.makeUpdate(String.format("DELETE from \"solr-configs\" where id=%d", getId()));
            id = -1;
            return true;
        }
        return false;
    }


    public static SolrConfig fromID(int id) {
        SolrConfigExtractor extractor = new SolrConfigExtractor();
        DBConnection.makeQuery(extractor, String.format("SELECT * from \"solr-configs\" where id=%d", id));
        List<SolrConfig> instances = extractor.getInstances();
        if(instances.size() == 0){
            return null;
        }
        return instances.get(0);
    }
}
