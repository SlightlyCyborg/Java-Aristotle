import java.util.ArrayList;
import java.util.List;

public class SearchResult {

    List<Video> videos;

    SearchResult(){
        videos = new ArrayList<Video>();
    }

    List<Video> getVideos(){
        return videos;
    }

    public void addVideo(Video video) {
        videos.add(video);
    }
}

