import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class YouTubeChannelVideoSourceTest {

    @Test
    void testGetVideosPublishedSince() throws GeneralSecurityException, IOException {
        VideoSource simone = new YouTubeChannelVideoSource("UC3KEoMzNz8eYnwBC34RaKCQ",
                YouTubeChannelVideoSource.ID_Type.UUID);
        OffsetDateTime the90s = OffsetDateTime.of(1999, 01, 01,
                0,0,0,0, ZoneOffset.UTC);
        List<Video> allSimoneVids = simone.getVideosPublishedSince(the90s);

        OffsetDateTime year2019 = OffsetDateTime.of(2019, 01, 01,
                0,0,0,0, ZoneOffset.UTC);

        List<Video> recentSimoneVids = simone.getVideosPublishedSince(year2019);

        assertTrue(allSimoneVids.size() > 70);
        assertTrue(allSimoneVids.size()-recentSimoneVids.size() > 10);

        for(Video v : allSimoneVids){
            assertNotNull(v.id);
            assertEquals(v.id.length(), 11);
        }
    }
}
