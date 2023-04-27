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
import java.util.*;

public class YouTubeChannelVideoSource implements VideoSource{

    static boolean alreadyRetried=false;

    public static final int VIDEO_LIST_BATCH_SIZE = 50;

    enum ID_Type{
        USERNAME,
        UUID
    }

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    String idOrUsername;
    ID_Type idType;

    private YouTube youtubeService;

    private static final String DEVELOPER_KEY = "REPLACE ME";

    public static YouTubeChannelVideoSource getByYouTubeURL(String youtubeURL) throws GeneralSecurityException, IOException {
        YouTubeURL url = new YouTubeURL(youtubeURL);
        return getByYouTubeURL(url);
    }

    public static YouTubeChannelVideoSource getByYouTubeURL(YouTubeURL url) throws GeneralSecurityException, IOException {
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
        } catch(Exception e){
           System.out.println(e);
        }
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

    public YouTubeURL getYouTubeURL(){
        return new YouTubeURL(idOrUsername, idType);
    }

    private static List<List<Video>> partitionVideosForYouTubeQuery(List<Video> videos){
        int maximumBatchSizeForYouTubeAPI = VIDEO_LIST_BATCH_SIZE;
        List<List<Video>> partitions = new ArrayList<>();
        for(int i=0; i<videos.size(); i++){

            int partitionNumber = i/maximumBatchSizeForYouTubeAPI;
            if(i%maximumBatchSizeForYouTubeAPI == 0) partitions.add(new ArrayList<>());

            partitions.get(partitionNumber).add(videos.get(i));
        }
        return partitions;
    }

    public static void initializeDetailsForVideos(List<Video> videos){
        List<List<Video>> partitions = partitionVideosForYouTubeQuery(videos);
        List<String> ids = new ArrayList<>();
        for(List<Video> batch: partitions){
           initializeDetailsForBatchOfVideos(batch);
        }
    }

    public static void initializeDetailsForBatchOfVideos(List<Video> batch){
        StringBuilder idBuilder = new StringBuilder();
        Map<String, Video> videoMap = mapifyBatch(batch);
        for(int i=0; i<batch.size(); i++){
            idBuilder.append(batch.get(i).getID());
            if(i < batch.size()-1){
                idBuilder.append(",");
            }
        }
        try {
            YouTube service;
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            service = new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                    .setApplicationName("aristotle")
                    .build();
            YouTube.Videos.List request = service.videos().list("snippet, statistics");

            request.setMaxResults((long)50).setId(idBuilder.toString());

            request.setKey(DEVELOPER_KEY);
            VideoListResponse response = request.execute();
            List<com.google.api.services.youtube.model.Video> items = response.getItems();

            for (com.google.api.services.youtube.model.Video ytVideo : items) {
                try {
                    Video aristotleVideo = videoMap.get(ytVideo.getId());
                    aristotleVideo.source = Video.Source.YOUTUBE;
                    aristotleVideo.title = ytVideo.getSnippet().getTitle();
                    aristotleVideo.description = ytVideo.getSnippet().getDescription();
                    aristotleVideo.thumbnail = handleThumbnails(ytVideo);
                    aristotleVideo.uploaded = ytVideo.getSnippet().getPublishedAt().toString();
                    aristotleVideo.channel = ytVideo.getSnippet().getChannelTitle();
                    aristotleVideo.views = ytVideo.getStatistics().getViewCount().intValue();
                    aristotleVideo.likes = ytVideo.getStatistics().getLikeCount().intValue();
                } catch(Exception e){
                    System.err.println("video cant be added");
                }
            }
        }catch(Exception e){
            if(!alreadyRetried){
                alreadyRetried = true;
               initializeDetailsForVideos(batch);
            }
        }
        alreadyRetried = false;
    }

    private static String handleThumbnails(com.google.api.services.youtube.model.Video ytVideo){
        try {
            ThumbnailDetails thumbnails = ytVideo.getSnippet().getThumbnails();
            if (thumbnails.getMaxres() != null) {
                return thumbnails.getMaxres().getUrl();
            }

            if (thumbnails.getHigh() != null) {
                return thumbnails.getHigh().getUrl();
            }

            if (thumbnails.getMedium() != null) {
                return thumbnails.getMedium().getUrl();
            }

            if (thumbnails.getStandard() != null) {
                return thumbnails.getStandard().getUrl();
            }

            if (thumbnails.getDefault() != null) {
                return thumbnails.getDefault().getUrl();
            }

            return "";
        } catch(NullPointerException e){
            return "";
        }
    }

    private static Map<String, Video> mapifyBatch(List<Video> batch){
        Map<String, Video> rv= new HashMap<>();
        for(Video v: batch){
            rv.put(v.getID(), v);
        }
        return rv;
    }
}
