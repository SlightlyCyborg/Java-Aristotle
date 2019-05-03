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
                    Video video = new Video(details.getVideoId());
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
}
