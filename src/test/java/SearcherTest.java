import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearcherTest {

    @Test
    void SolrConfigGetURL(){
        Searcher.SolrConfig config = new Searcher.SolrConfig();
        config.ssl = true;
        config.host = "localhost";
        config.port = 2020;
        config.core = "jordan";

        try {
            URL withSSL = config.getURL();
            assertEquals("https://localhost:2020/solr/jordan", withSSL.toString());

            config.ssl = false;

            URL withoutSSL = config.getURL();
            assertEquals("http://localhost:2020/solr/jordan", withoutSSL.toString());

        } catch(Exception e){
            fail(e.getMessage());
        }
    }

    @Test
    void search() throws MalformedURLException {
        Searcher.SolrConfig videos = new Searcher.SolrConfig();
        videos.ssl = false;
        videos.host = "localhost";
        videos.port = 2020;
        videos.core = "jordan";

        Searcher.SolrConfig blocks = new Searcher.SolrConfig(videos);
        blocks.core = "jordan-blocks";

        Searcher searcher = new Searcher(videos, blocks);

        String query = "clean your room";

        SearchResult result = searcher.search(query);

        List<Video> searchVideos = result.getVideos();

        assertEquals(10, searchVideos.size());

    }

    @Test
    void improveQuery() throws ParserConfigurationException, IOException, SAXException {
        File xml = new File("test_data/Searcher/improveQuery.xml");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document config = db.parse(xml);

        Element query = config.getDocumentElement();
        Node original = query.getElementsByTagName("original").item(0);
        Node expectedImproved = query.getElementsByTagName("improved").item(0);

        String actualImprovedQ = Searcher.improveQ(original.getTextContent());

        assertEquals(expectedImproved.getTextContent(), actualImprovedQ);

    }
}