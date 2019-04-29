import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;

public class VideoTest {

    @Test
    void fromFileTest() throws FileNotFoundException {
        File srt = new File("test_data/Video/oVvPfq0MoaQ_0_en.srt");
        Video vid = new Video(srt);
        assertEquals(vid.id, "oVvPfq0MoaQ");
        assertEquals(vid.blocks.get(0).words,"good morning Hank it's Tuesday your book");
        assertEquals(vid.blocks.get(111).words, "up and Hank I will<font color=\"#CCCCCC\"> see you on Friday</font>");
        assertEquals(vid.blocks.get(111).id, 112);
    }
}
