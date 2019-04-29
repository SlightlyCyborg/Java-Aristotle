import java.net.MalformedURLException;
import java.net.URL;

public class SolrConfig {
    boolean ssl = false;
    private String host;
    private String core;
    private int port = 8983;

    SolrConfig(){}

    SolrConfig(SolrConfig toBeCopied){
        ssl = toBeCopied.ssl;
        host = toBeCopied.host;
        core = toBeCopied.core;
        port = toBeCopied.port;
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
}
