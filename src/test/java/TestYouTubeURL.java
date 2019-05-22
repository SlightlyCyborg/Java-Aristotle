import java.net.MalformedURLException;
import java.util.List;
import junit.framework.TestCase;

public class TestYouTubeURL extends TestCase{
    public void testURLCreation(){
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

    public void testPersistence(){
       try {

           DBConnection.makeUpdate("delete from \"youtube-urls\" where \"instance-username\"='test'");

           YouTubeURL me = new YouTubeURL("https://www.youtube.com/channel/UChsxOQf3j6Jw_BbzWcsIQPg?view_as=subscriber");
           YouTubeURL jarron = new YouTubeURL("https://www.youtube.com/channel/UCpuc5y6UMrBHFs1S4Qg9xjA/");

           me.saveToDBForUsername("test");
           jarron.saveToDBForUsername("test");

           List<YouTubeURL> urls = YouTubeURL.getForUsername("test");

            assertEquals(2, urls.size());
            boolean foundMe, foundJarron;
            foundMe = true;
            foundJarron = true;

            for(YouTubeURL url: urls){
                assertNotNull(url.identifierType());
                assertNotNull(url.identifier());
                if(url.identifier() == "UChsxOQf3j6Jw_BbzWcsIQPg"){
                    foundMe = true;
                }
                if(url.identifier() == "UCpuc5y6UMrBHFs1S4Qg9xjA") {
                    foundJarron = true;
                }
            }
            assertTrue(foundMe && foundJarron);

       } catch (MalformedURLException e) {
           e.printStackTrace();
       } finally {
           DBConnection.makeUpdate("delete from \"youtube-urls\" where \"instance-username\"='test'");
       }
    }
}
