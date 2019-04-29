import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstanceFactoryTest {

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