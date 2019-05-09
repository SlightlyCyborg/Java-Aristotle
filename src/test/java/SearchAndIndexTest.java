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
import java.util.ArrayList;
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

    static class IndexerAndSearcher{
        Indexer indexer;
        Searcher searcher;
    }

    static String INDEX_AND_SEARCH_TEST_VIDEO_ID = "7x5XRQ07sjU";
    static String INSTANCE_NAME = "test-index-and-search";

    /*
     * First the test cleans up from before by deleting the indexed results for this user from Solr and the DB.
     * Searches for a keyword in the video for the username test-index-and-search, but none exists.
     * It then runs the indexer up to a certian date, which will index INDEX_AND_SEARCH_TEST_VIDEO_ID.
     * Finally it researches for the keyword and finds a result.
     */
    @Test
    void testIndexAndSearch() throws IOException, SolrServerException, GeneralSecurityException {

        IndexerAndSearcher indexerAndSearcher = initializeIndexerAndSearcher();
        Searcher searcher = indexerAndSearcher.searcher;
        Indexer indexer = indexerAndSearcher.indexer;

        assertSearchReturnsNothing(searcher);
        indexer.indexAllSinceDate(LocalDate.parse("2019-03-01"));
        List<Video> videos = assertSearchReturnsAVideo(searcher);
        assertSomeVideosHadBlocks(videos);
    }

    private void assertSearchReturnsNothing(Searcher searcher) {
        SearchResult result = null;
        try {
            result = searcher.search("Brian");
            List<Video> videos = result.getVideos();
            assertEquals(0, videos.size());
        } catch (Exception e) {
            fail("unable to search for empty result");
        }
    }

    private List<Video> assertSearchReturnsAVideo(Searcher searcher){
        SearchResult result = null;
        try {
            result = searcher.search("Brian");
            List<Video> videos = result.getVideos();
            assertEquals(1, videos.size());
            return videos;
        } catch (Exception e) {
            fail("search threw and exception");
        }
        return null;
    }

    private void assertSomeVideosHadBlocks(List<Video> videos){
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

            assertTrue(atLeastSomeVideosHadBlocks);
        }
    }

    private static IndexerAndSearcher initializeIndexerAndSearcher(){
        try {
            String videoURL = "http://localhost:8983/solr/test-videos";
            String blockURL = "http://localhost:8983/solr/test-blocks";

            SolrClient videoClient = new HttpSolrClient.Builder(videoURL).build(); // Takes a long time.
            SolrClient blockClient = new HttpSolrClient.Builder(blockURL).build();

            cleanSolrForIndexAndSearchTest(videoClient, blockClient);

            Instance instance = new Instance();
            instance.setUsername(INSTANCE_NAME);

            cleanDBforIndexAndSearchTest(instance);

            Indexer indexer = new Indexer(instance, videoClient, blockClient);
            Searcher searcher = new Searcher(instance.getUsername(), videoClient, blockClient);

            VideoSource simone = new YouTubeChannelVideoSource("UC3KEoMzNz8eYnwBC34RaKCQ",
                    YouTubeChannelVideoSource.ID_Type.UUID);

            indexer.addVideoSource(simone);

            IndexerAndSearcher rv = new IndexerAndSearcher();
            rv.indexer = indexer;
            rv.searcher = searcher;
            return rv;
        } catch (Exception e){
            fail("unable to initialize IndexerAndSearcher");
        }
        return null;
    }

    private static void cleanSolrForIndexAndSearchTest(SolrClient videoClient, SolrClient blockClient){
        try {
            videoClient.deleteByQuery("*:*");
            blockClient.deleteByQuery("*:*");

            videoClient.commit();
            blockClient.commit();
        } catch (Exception e) {
            fail("unable to delete solr indices required to be deleted for this test");
        }
    }

    private static void cleanDBforIndexAndSearchTest(Instance instance){
        Video toDelete = new Video(INDEX_AND_SEARCH_TEST_VIDEO_ID);
        toDelete.instanceUsername = instance.getUsername();
        toDelete.unmarkAsHavingBeenIndexed();
    }
}