import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstanceTest {

    @Test void saveAndLoadInstanceFromDB() throws ParserConfigurationException, SAXException, IOException {
        File jbpXML = new File("test_data/Instance/saveAndLoad.xml");
        Instance saveAndLoad = InstanceFactory.fromFile(jbpXML);
        try {
            List<Instance> activeInstances = InstanceFactory.fromDB();
            assertFalse(instanceExistsWithID(activeInstances, saveAndLoad.getUsername()));

            saveAndLoad.save();
            activeInstances = InstanceFactory.fromDB();
            assertTrue(instanceExistsWithID(activeInstances, saveAndLoad.getUsername()));



        } catch(Exception e) {
            fail(e.getMessage());
        } finally {
            DBConnection.makeUpdate(String.format("delete from \"instances\" where username=%s",
                    saveAndLoad.getUsername()));
        }

    }

    private boolean instanceExistsWithID(List<Instance> instances, String id){
        boolean exists = false;
        for(Instance instance: instances) {
            if (instance.getUsername() == id){
                exists = true;
            }
        }

        return exists;
    }

    @Test
    void fromFile(){

        File jbpXML = new File("test_data/InstanceFactory/jordanBPeterson.xml");

        try {
            Instance jordanBPeterson = InstanceFactory.fromFile(jbpXML);
            assertNotNull(jordanBPeterson);
            assertEquals("Jordan Peterson", jordanBPeterson.getName());
            assertEquals("jordan-peterson", jordanBPeterson.getUsername());
            assertEquals("http://solr.daemon.life:8983/solr/videos", jordanBPeterson.getSolrVideoURL().toString());
            assertEquals("http://solr.daemon.life:8983/solr/video_blocks", jordanBPeterson.getSolrBlockURL().toString());
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void fromDirectory(){
        File dir = new File("test_data/InstanceFactory");

        try{
            List<Instance> factoryResult = InstanceFactory.fromDirectory(dir);
            assertEquals(2, factoryResult.size());
            boolean foundJBP=false, justEnough=false;
            for(Iterator<Instance> it = factoryResult.iterator(); it.hasNext(); ){
                Instance possibleMatch = it.next();
                if(possibleMatch.getName().equals("Jordan Peterson"))foundJBP = true;
                if(possibleMatch.getName().equals("JustEnough")) justEnough = true;
            }
            assertTrue(foundJBP&&justEnough);
        } catch (Exception e){
            fail(e.getMessage());
        }
    }
}