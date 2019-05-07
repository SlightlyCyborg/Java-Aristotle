import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestYouTubeURL {
    @Test
    void mainTest(){
        try {
            YouTubeURL me = new YouTubeURL("https://www.youtube.com/channel/UChsxOQf3j6Jw_BbzWcsIQPg?view_as=subscriber");
            YouTubeURL jarron = new YouTubeURL("https://www.youtube.com/channel/UCpuc5y6UMrBHFs1S4Qg9xjA/");
            YouTubeURL jre = new YouTubeURL("https://www.youtube.com/user/PowerfulJRE");

            assertEquals("UChsxOQf3j6Jw_BbzWcsIQPg", me.identifier());
            assertEquals(YouTubeChannelVideoSource.ID_Type.UUID, me.identifierType());

            assertEquals("UCpuc5y6UMrBHFs1S4Qg9xjA", jarron.identifier());
            assertEquals(YouTubeChannelVideoSource.ID_Type.UUID, jarron.identifierType());


            assertEquals("PowerfulJRE", jre.identifier());
            assertEquals(YouTubeChannelVideoSource.ID_Type.USERNAME, jre.identifierType());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
