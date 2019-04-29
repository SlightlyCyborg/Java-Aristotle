import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
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

class SearchAndIndexTest {

    @Test
    void SolrConfigGetURL(){
        SolrConfig config = new SolrConfig();
        config.setSSL(true);
        config.setHost("localhost");
        config.setPort(2020);
        config.setCore("jordan");

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
    void search() throws IOException, SolrServerException {

        SolrClient videoConnection = new HttpSolrClient.Builder("http://localhost:8983/solr/yang").build();
        SolrClient blockConnection = new HttpSolrClient.Builder("http://localhost:8983/solr/yang-video-blocks").build();

        Searcher searcher = new Searcher(videoConnection, blockConnection);

        SearchResult result = searcher.search("freedom dividend");
        List<Video> videos = result.getVideos();

        boolean atLeastSomeVideosHadBlocks = false;
        for(Video v: videos){
            assertNotNull(v.id);
            assertEquals(v.id.length(), 11);
            if(v.blocks.size() > 0) {
                atLeastSomeVideosHadBlocks = true;
                assertTrue(v.blocks.get(0).words.length() > 0);
                assertTrue(v.blocks.get(0).id.length() > 0);
                assertNotNull(v.blocks.get(0).startTime);
                assertNotNull(v.blocks.get(0).stopTime);
            }
            assertTrue(atLeastSomeVideosHadBlocks);
        }
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