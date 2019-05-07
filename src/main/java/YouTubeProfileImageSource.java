import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ThumbnailDetails;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class YouTubeProfileImageSource implements ProfileImageSource{

    static final Duration DEFAULT_CACHE_LIFE = Duration.ofDays(1);
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String DEVELOPER_KEY = "AIzaSyBKLyvIBmbu_cA9xGV_aNkljlP7D8OrAJ8";

    private YouTube youtubeService;

    private enum Type{
        CHANNEL_ID,
        USERNAME
    }

    private URL cache;
    private LocalDateTime lastCached;
    private Type type;
    private Lock cacheUpdaterLock = new ReentrantLock();

    String usernameOrId;
    Duration cacheLife;

    public static YouTubeProfileImageSource fromChannelID(String id, Duration cacheLife){
        return new YouTubeProfileImageSource(id, Type.CHANNEL_ID, cacheLife);
    }

    public static YouTubeProfileImageSource fromUsername(String username, Duration cacheLife){
        return new YouTubeProfileImageSource(username, Type.USERNAME, cacheLife);
    }

    public static YouTubeProfileImageSource fromChannelID(String id){
        return new YouTubeProfileImageSource(id, Type.CHANNEL_ID, DEFAULT_CACHE_LIFE);
    }

    public static YouTubeProfileImageSource fromUsername(String username){
        return new YouTubeProfileImageSource(username, Type.USERNAME, DEFAULT_CACHE_LIFE);
    }

    private YouTubeProfileImageSource(String usernameOrId, Type t, Duration cacheLife){
        type = t;
        this.cacheLife = cacheLife;
        this.usernameOrId = usernameOrId;
        try {
            buildYouTubeService();
            Thread fetcher = updateCache();
            fetcher.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public URL getProfileImageURL() {
        if(mustRecache()) updateCache();
        return cache;
    }

    private URL fetchThumbnail(){
        URL thumbnail = null;
        try {
            YouTube.Channels.List request = youtubeService.channels().list("snippet").setKey(DEVELOPER_KEY);

            switch(type){
                case USERNAME:
                    request.setForUsername(usernameOrId);
                    break;
                case CHANNEL_ID:
                    request.setId(usernameOrId);
            }

            ChannelListResponse response = request.execute();
            if (response.getItems().size() == 0) throw new Exception("Can not find chan to fetch YouTube thumbnail.");

            ThumbnailDetails thumbnails = response.getItems().get(0).getSnippet().getThumbnails();

            if(thumbnails.getMedium() != null){
                thumbnail = new URL(thumbnails.getMedium().getUrl());
            } else {
                thumbnail = new URL(thumbnails.getStandard().getUrl());
            }

        } catch (Exception e) {
            cacheUpdaterLock.lock();
            if(cache == null){
                String spec = "https://i.stack.imgur.com/34AD2.jpg";
                try { thumbnail = new URL(spec); } catch (MalformedURLException ex){/*Will never happen*/}
            }
            cacheUpdaterLock.unlock();
        }
        return thumbnail;
    }

    private Thread updateCache() {
        Thread t = new Thread(){
            public void run(){
                URL thumbnail = fetchThumbnail();
                lastCached = LocalDateTime.now();
                cacheUpdaterLock.lock();
                cache = thumbnail;
                cacheUpdaterLock.unlock();
            }
        };

        t.start();
        return t;
    }

    private boolean mustRecache(){
        LocalDateTime nextCache = LocalDateTime.from(cacheLife.addTo(lastCached));
        if(LocalDateTime.now().isAfter(nextCache)){
            return true;
        }

        return false;
    }

    private void buildYouTubeService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        youtubeService =  new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName("aristotle")
                .build();
    }

    public LocalDateTime getLastTimeCached(){
        return lastCached;
    }
}
