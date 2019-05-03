import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class YouTubeChannelVideoSourceTest {

    @Test
    void testGetVideosPublishedSince() throws GeneralSecurityException, IOException {
        VideoSource simone = new YouTubeChannelVideoSource("UC3KEoMzNz8eYnwBC34RaKCQ",
                YouTubeChannelVideoSource.ID_Type.UUID);

        LocalDate the90s = LocalDate.parse("1999-01-01");

        LocalDate year2019 = LocalDate.parse("2019-01-01");

        List<Video> allSimoneVids = simone.getVideos(the90s, LocalDate.now());

        List<Video> recentSimoneVids = simone.getVideos(year2019, LocalDate.now());

        assertTrue(allSimoneVids.size() > 70);
        assertTrue(allSimoneVids.size()-recentSimoneVids.size() > 10);

        for(Video v : allSimoneVids){
            assertNotNull(v.id);
            assertEquals(v.id.length(), 11);
        }
    }
}
