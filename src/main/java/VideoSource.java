import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class VideoSource {
    public abstract List<Video> getVideosPublishedSince(OffsetDateTime lastIndexedDate);
    public abstract String getID();
}


