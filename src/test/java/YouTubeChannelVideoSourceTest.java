import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import junit.framework.TestCase;

public class YouTubeChannelVideoSourceTest extends TestCase{

    public void testGetVideosPublishedSince() throws GeneralSecurityException, IOException {
        VideoSource simone = new YouTubeChannelVideoSource("UC3KEoMzNz8eYnwBC34RaKCQ",
                YouTubeChannelVideoSource.ID_Type.UUID);
        simoneTest(simone);
    }

    public void testYouTubeChannelVideoSource_getByYouTubeURL() throws GeneralSecurityException, IOException {
        VideoSource simone = YouTubeChannelVideoSource.getByYouTubeURL("https://www.youtube.com/channel/UC3KEoMzNz8eYnwBC34RaKCQ");
        simoneTest(simone);
    }

    void simoneTest(VideoSource simone){
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

    public void testInstanciateDetailsForVideos(){
        List<Video> videos= new ArrayList<>();
        try {
            Scanner ids = new Scanner(new FileInputStream("test_data/YouTubeChannelVideoSource/ids")).useDelimiter("\n");
            while(ids.hasNext()){
                videos.add(new Video(ids.next()));
            }
            YouTubeChannelVideoSource.initializeDetailsForVideos(videos);
            for(Video v: videos){
                assertTrue(v.isFullyInstanciatedFromYT());
            }

        } catch(FileNotFoundException e){

        }
    }
}
