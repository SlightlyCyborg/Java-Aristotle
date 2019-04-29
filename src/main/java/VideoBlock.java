import org.apache.solr.common.SolrDocument;

public class VideoBlock {
    public static class BlockTime {
        int hour, minute, second, millis;
        public BlockTime() {}

        public BlockTime(String timeText){
            String [] hourMinSecMillis = timeText.split("[:,]");
            if (hourMinSecMillis.length != 4){
                String err = String.format(
                        "%s does not represent h:m:s:mil", timeText);
                throw new IllegalArgumentException(err);
            }
            hour = Integer.parseInt(hourMinSecMillis[0]);
            minute = Integer.parseInt(hourMinSecMillis[1]);
            second = Integer.parseInt(hourMinSecMillis[2]);
            millis = Integer.parseInt(hourMinSecMillis[3]);
        }

        @Override
        public String toString(){
            return  String.format("%03d:%02d:%02d,%03d",
                    hour, minute, second, millis);
        }
    }

    String id, words;
    BlockTime startTime;
    BlockTime stopTime;

    public VideoBlock(SolrDocument doc){
        id = (String) doc.getFieldValue("id");
        words = (String) doc.getFieldValue("captions_t");
        startTime = new BlockTime((String)doc.getFieldValue("start_time_s"));
        stopTime = new BlockTime((String)doc.getFieldValue("stop_time_s"));

        //Viewable words should work differently.
        //:viewable_words_t (block ::viewable-words) video.clj:103

    }

    public VideoBlock(String id, String words, String time){
        this.id = id;
        this.words = words;
        String[] startAndStop = parseStartAndStopTime(time);
        startTime = new BlockTime(startAndStop[0]);
        startTime = new BlockTime(startAndStop[1]);
    }

    private String[] parseStartAndStopTime(String time) {
        return time.split(" --> ");
    }

}
