import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TestYouTubeProfileImageSource {
    @Test
    void testFromUsername(){
        YouTubeProfileImageSource imgSource = YouTubeProfileImageSource.fromUsername("JordanPetersonVideos");
        String expected = "https://yt3.ggpht.com/a/AGF-l79xqV5D7Q1Iyq-zH6CSkNFAEfcx5HKWOf0ytA=s240-mo-c-c0xffffffff-rj-k-no";
        doesProfileImageReturnCorrectly(imgSource, expected);
    }

    @Test
    void testFromChannelID(){
        YouTubeProfileImageSource imgSource = YouTubeProfileImageSource.fromChannelID("UC3KEoMzNz8eYnwBC34RaKCQ");
        String expected = "https://yt3.ggpht.com/a/AGF-l79qi5YAvOj6ebtkB-jZMhziLo1j8IGDzUynYQ=s240-mo-c-c0xffffffff-rj-k-no";
        doesProfileImageReturnCorrectly(imgSource, expected);
    }

    @Test
    void testByUsernameCachingUpdatesProperly(){
        Duration cacheTime = Duration.ofSeconds(1);
        LocalDateTime start = LocalDateTime.now();
        YouTubeProfileImageSource imgSource = YouTubeProfileImageSource.fromUsername("JordanPetersonVideos", cacheTime);
        String expected = "https://yt3.ggpht.com/a/AGF-l79xqV5D7Q1Iyq-zH6CSkNFAEfcx5HKWOf0ytA=s240-mo-c-c0xffffffff-rj-k-no";
        runCacheUpdateTestHelper(imgSource, expected, cacheTime, start);
    }

    @Test
    void testByChannelIDCachingUpdatesProperly(){
        Duration cacheTime = Duration.ofSeconds(1);
        LocalDateTime start = LocalDateTime.now();
        YouTubeProfileImageSource imgSource = YouTubeProfileImageSource.fromChannelID("UC3KEoMzNz8eYnwBC34RaKCQ", cacheTime);
        String expected = "https://yt3.ggpht.com/a/AGF-l79qi5YAvOj6ebtkB-jZMhziLo1j8IGDzUynYQ=s240-mo-c-c0xffffffff-rj-k-no";
        runCacheUpdateTestHelper(imgSource, expected, cacheTime, start);
    }

    void runCacheUpdateTestHelper(YouTubeProfileImageSource imgSource, String expected,
                                  Duration cacheTime, LocalDateTime start){
        try {
            Thread.sleep(2000);
            doesProfileImageReturnCorrectly(imgSource, expected);
            //Wait for updateCache() thread to finish
            Thread.sleep(2500);
            LocalDateTime nextCache = LocalDateTime.from(cacheTime.addTo(start));
            assertTrue(imgSource.getLastTimeCached().isAfter(nextCache));
        } catch (InterruptedException e) {
            fail("Sleeping failed. Target code may be ok, test may just be broken");
        }
    }

    void doesProfileImageReturnCorrectly(YouTubeProfileImageSource imgSource, String expected){
        URL imgURL= imgSource.getProfileImageURL();
        assertEquals(expected ,imgURL.toString());
    }
}
