import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface VideoSource {
    public List<Video> getVideos(LocalDate earliest, LocalDate latest);
    public String getID();
}


