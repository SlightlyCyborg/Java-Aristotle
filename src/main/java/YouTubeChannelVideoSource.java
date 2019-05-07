import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YouTubeChannelVideoSource implements VideoSource{


    enum ID_Type{
        USERNAME,
        UUID
    }

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    String idOrUsername;
    ID_Type idType;

    private YouTube youtubeService;

    private static final String DEVELOPER_KEY = "AIzaSyBKLyvIBmbu_cA9xGV_aNkljlP7D8OrAJ8";

    public static YouTubeChannelVideoSource getByYouTubeURL(String youtubeURL) throws GeneralSecurityException, IOException {
        YouTubeURL url = new YouTubeURL(youtubeURL);
        String identifier = url.identifier();
        ID_Type type = url.identifierType();
        return new YouTubeChannelVideoSource(identifier, type);
    }

    YouTubeChannelVideoSource(String idOrUsername, ID_Type idType) throws GeneralSecurityException, IOException {
        this.idOrUsername = idOrUsername;
        this.idType = idType;
        buildYouTubeService();
    }

    public String findPlaylistForAllChannelUploads() throws IOException {
        YouTube.Channels.List request = youtubeService.channels()
                .list("contentDetails");

        request.setKey(DEVELOPER_KEY);

        if(idType == ID_Type.USERNAME) {
            request.setForUsername(idOrUsername);
        } else {
            request.setId(idOrUsername);
        }

         ChannelListResponse response = request.execute();

        ChannelContentDetails.RelatedPlaylists playlists =
                response.getItems().get(0).getContentDetails().getRelatedPlaylists();

        return playlists.getUploads();
    }

    @Override
    public List<Video> getVideos(LocalDate earliest, LocalDate latest) {

        try {
            String uploadPlaylistID = findPlaylistForAllChannelUploads();
            return findVideosInPlaylist(uploadPlaylistID, earliest, latest);
        } catch(Exception e){}
        return null;
    }

    private List<Video> findVideosInPlaylist(String uploadPlaylistID,
                                             LocalDate earliest, LocalDate latest) throws IOException {
        List<Video> rv = new ArrayList<Video>();
        String nextToken = "";
        boolean nextPageExists = true;

        while(nextPageExists) {

            YouTube.PlaylistItems.List request = youtubeService.playlistItems().list("contentDetails");

             request.setKey(DEVELOPER_KEY)
                    .setPlaylistId(uploadPlaylistID)
                    .setMaxResults((long) 50)
                    .execute();

            if(nextToken != ""){
                request.setPageToken(nextToken);
            }

            PlaylistItemListResponse response = request.execute();

            nextToken = response.getNextPageToken();
            for(Iterator<PlaylistItem> it = response.getItems().iterator(); it.hasNext();){
                PlaylistItemContentDetails details = it.next().getContentDetails();
                OffsetDateTime published = OffsetDateTime.parse(details.getVideoPublishedAt().toString());
                LocalDate publishedDate= published.toLocalDate();

                if(publishedDate.compareTo(earliest) > 0 && publishedDate.compareTo(latest) < 0) {
                    Video video = new Video(details.getVideoId(), Video.Source.YOUTUBE);
                    rv.add(video);
                }
            }

            if(nextToken == null){
                nextPageExists = false;
            }

        }

        return rv;
    }

    @Override
    public String getID() {
        return null;
    }

    private void buildYouTubeService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        youtubeService =  new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName("aristotle")
                .build();
    }


    public static Video getByID(String id) throws GeneralSecurityException, IOException {
        YouTube service;
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        service =  new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName("aristotle")
                .build();
        YouTube.Videos.List request = service.videos().list("snippet, statistics");

        request.setId(id);

        request.setKey(DEVELOPER_KEY);
        VideoListResponse response = request.execute();
        List<com.google.api.services.youtube.model.Video> items = response.getItems();

        for(com.google.api.services.youtube.model.Video ytVideo: items){
            Video aristotleVideo = new Video(id, Video.Source.YOUTUBE);
            aristotleVideo.title = ytVideo.getSnippet().getTitle();
            aristotleVideo.description = ytVideo.getSnippet().getDescription();
            try {
                aristotleVideo.thumbnail = ytVideo.getSnippet().getThumbnails().getStandard().getUrl();
            } catch(Exception e) {
                aristotleVideo.thumbnail = ytVideo.getSnippet().getThumbnails().getDefault().getUrl();
            }
            aristotleVideo.uploaded = ytVideo.getSnippet().getPublishedAt().toString();
            aristotleVideo.channel = ytVideo.getSnippet().getChannelTitle();
            aristotleVideo.views = ytVideo.getStatistics().getViewCount().intValue();
            aristotleVideo.likes = ytVideo.getStatistics().getLikeCount().intValue();
            return aristotleVideo;
        }
        return null;
    }
}
