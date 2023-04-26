import junit.framework.TestCase;

import java.net.MalformedURLException;

public class SolrConfigTest extends TestCase {
    public void testDb(){
        SolrConfig config = new SolrConfig();
        config.setHost("foo.com");
        config.setPort(6969);
        config.setCore("god");

        //Previously failed test would leave an entry
        config.deleteFromDB();

        assertFalse(config.isAlreadyInDB());

        //Should succeed
        assertTrue(config.saveToDB());

        //redundant check to fully excersize isAlreadyInDB
        assertTrue(config.isAlreadyInDB());

        //Already saved
        assertFalse(config.saveToDB());

        //Clean Up
        config.deleteFromDB();
        assertFalse(config.isAlreadyInDB());

    }

    public void testGetById() throws MalformedURLException {
        SolrConfig config = SolrConfig.fromID(5);
        assertEquals(config.getURL().toString(), "https://test:6969/solr/video");
    }
}
