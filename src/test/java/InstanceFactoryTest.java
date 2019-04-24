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
            boolean foundJBP=false, foundEmpty=false;
            for(Iterator<Instance> it = factoryResult.iterator(); it.hasNext(); ){
                Instance possibleMatch = it.next();
                if(possibleMatch.getName().equals("Jordan Peterson"))foundJBP = true;
                if(possibleMatch.getName().equals("Empty")) foundEmpty = true;
            }
            assertTrue(foundJBP&&foundEmpty);
        } catch (Exception e){
            fail(e.getMessage());
        }
    }
}