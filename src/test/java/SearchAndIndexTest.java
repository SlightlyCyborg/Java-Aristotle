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
import java.security.GeneralSecurityException;
import java.time.LocalDate;
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

        Searcher searcher = new Searcher(null, videoConnection, blockConnection);

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
    void getVideosToIndex() throws GeneralSecurityException, IOException {

        String videoURL = "http://localhost:8983/solr/test-videos";
        String blockURL = "http://localhost:8983/solr/test-blocks";

        SolrClient videoClient = new HttpSolrClient.Builder(videoURL).build();
        SolrClient blockClient = new HttpSolrClient.Builder(blockURL).build();

        Instance instance = new Instance();
        instance.setUsername("last-indexed-test-user");

        Indexer indexer = new Indexer(instance, videoClient, blockClient);

        VideoSource simone = new YouTubeChannelVideoSource("UC3KEoMzNz8eYnwBC34RaKCQ",
                YouTubeChannelVideoSource.ID_Type.UUID);

        indexer.addVideoSource(simone);

        List<URL> urls = indexer.getUrlsToIndexAsOfDate(LocalDate.parse("2019-05-01"));

        assertEquals(urls.size(), 3);
    }

    @Test
    void downloadNewCaptionsAsOfDate() throws GeneralSecurityException, IOException {
        String videoURL = "http://localhost:8983/solr/test-videos";
        String blockURL = "http://localhost:8983/solr/test-blocks";

        SolrClient videoClient = new HttpSolrClient.Builder(videoURL).build();
        SolrClient blockClient = new HttpSolrClient.Builder(blockURL).build();

        Instance instance = new Instance();
        instance.setUsername("last-indexed-test-user");

        Indexer indexer = new Indexer(instance, videoClient, blockClient);

        VideoSource simone = new YouTubeChannelVideoSource("UC3KEoMzNz8eYnwBC34RaKCQ",
                YouTubeChannelVideoSource.ID_Type.UUID);

        indexer.addVideoSource(simone);

        File outputDir = indexer.downloadNewCaptionsAsOfDate(LocalDate.parse("2019-05-01"));
        File[] srts = outputDir.listFiles();
        assertTrue(srts.length>0);
    }

    @Disabled
    @Test
    void testIndexRun() throws IOException, SolrServerException, GeneralSecurityException {


        String videoURL = "http://localhost:8983/solr/test-videos";
        String blockURL = "http://localhost:8983/solr/test-blocks";

        SolrClient videoClient = new HttpSolrClient.Builder(videoURL).build();
        SolrClient blockClient = new HttpSolrClient.Builder(blockURL).build();

        videoClient.deleteByQuery("*:*");
        blockClient.deleteByQuery("*:*");

        videoClient.commit();
        blockClient.commit();

        Instance instance = new Instance();
        instance.setUsername("last-indexed-test-user");

        Indexer indexer = new Indexer(instance, videoClient, blockClient);
        Searcher searcher = new Searcher("last-indexed-test-user", videoClient, blockClient);

        VideoSource simone = new YouTubeChannelVideoSource("UC3KEoMzNz8eYnwBC34RaKCQ",
                YouTubeChannelVideoSource.ID_Type.UUID);

        indexer.addVideoSource(simone);

        SearchResult result = searcher.search("robot");
        List<Video> videos = result.getVideos();

        assertEquals(0, videos.size());

        indexer.indexAllSinceDate(LocalDate.parse("2019-03-01"));

        result = searcher.search("Brian");
        videos = result.getVideos();

        assertNotEquals(0, videos.size());

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