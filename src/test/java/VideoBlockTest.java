import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VideoBlockTest {
    @Test
    void testBlockTime_toString(){
        VideoBlock.BlockTime simple = new VideoBlock.BlockTime();
        simple.hour = 1;
        simple.minute = 2;
        simple.second = 3;
        simple.millis = 4;

        String actual = simple.toString();
        System.out.println(actual);
    }

    @Test
    void testBlockTime_new(){
        String representation = "01:20:15,215";
        VideoBlock.BlockTime t = new VideoBlock.BlockTime(representation);
        assertEquals(1, t.hour);
        assertEquals(20, t.minute);
        assertEquals(15, t.second);
        assertEquals(215, t.millis);

        String malformed = "01:55:20";

    }
}
