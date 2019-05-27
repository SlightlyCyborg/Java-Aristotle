import java.util.ArrayList;
import java.util.List;

public class SearchResult {

    List<Video> videos;
    String terms;

    SearchResult(){
        videos = new ArrayList<Video>();
    }
    
    SearchResult(String terms){
    	this.terms = terms;
        videos = new ArrayList<Video>();
    }

    List<Video> getVideos(){
        return videos;
    }

    public void addVideo(Video video) {
        videos.add(video);
    
    }
    
    String getTerms() {
    	return terms;
    }
}

