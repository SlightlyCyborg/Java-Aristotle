import java.net.MalformedURLException;
import java.net.URL;

public class YouTubeURL {

    private YouTubeChannelVideoSource.ID_Type type;
    private String identifier;

    YouTubeURL(String urlToParse) throws MalformedURLException {
        URL url = new URL(urlToParse);
        String path = url.getPath();
        String[] segments = path.split("\\/");
        int index = findTypeSegmentIndex(segments);
        if(index>-1 && segments.length>=index+2){
            identifier = segments[index+1];
        }
        type = getType(segments[index]);
    }

    private YouTubeChannelVideoSource.ID_Type getType(String type) {
        switch (type){
            case "channel":
                return YouTubeChannelVideoSource.ID_Type.UUID;
            case "user":
                return YouTubeChannelVideoSource.ID_Type.USERNAME;
        }
        return null;
    }

    private static int findTypeSegmentIndex(String[] segments) {
        for(int i=0; i<segments.length; i++){
            switch(segments[i]){
                case "channel":
                    return i;
                case "user":
                    return i;
            }
        }
        return -1;
    }

    public String identifier() {
        return identifier;
    }

    public YouTubeChannelVideoSource.ID_Type identifierType() {
        return type;
    }
}
