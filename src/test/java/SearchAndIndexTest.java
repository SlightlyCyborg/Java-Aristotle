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
    void testIndexAndSearch() throws IOException, SolrServerException, GeneralSecurityException {


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

        Video mayNotBeCleanForTest = new Video("7x5XRQ07sjU");
        mayNotBeCleanForTest.instanceUsername = instance.getUsername();
        // Clean video
        mayNotBeCleanForTest.unmarkAsHavingBeenIndexed();


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

        assertEquals(1, videos.size());

        boolean atLeastSomeVideosHadBlocks = false;
        for(Video v: videos){
            assertTrue(v.hasBeenIndexedP());

            assertEquals(v.id.length(), 11);
            if(v.blocks.size() > 0) {
                atLeastSomeVideosHadBlocks = true;
                assertTrue(v.blocks.get(0).words.length() > 0);
                assertTrue(v.blocks.get(0).id.length() > 0);
                assertNotNull(v.blocks.get(0).startTime);
                assertNotNull(v.blocks.get(0).stopTime);
            }

            v.unmarkAsHavingBeenIndexed();
        }

        assertTrue(atLeastSomeVideosHadBlocks);
    }
}