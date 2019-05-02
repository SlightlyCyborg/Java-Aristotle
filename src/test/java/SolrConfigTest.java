import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

public class SolrConfigTest {
    @Test
    void db(){
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

    @Test
    void getById() throws MalformedURLException {
        SolrConfig config = SolrConfig.fromID(5);
        assertEquals(config.getURL().toString(), "https://test:6969/solr/video");
    }
}
