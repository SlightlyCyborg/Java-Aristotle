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

        public void setForYTURL(){

        }

        public String getForYTURL(){
            return String.format("%dh%dm$ds", hour, minute, second);
        }

        public void setForJSPlayer(){}

        public String getForJSPlayer(){
            return String.format("%d", hour*60*60 + minute*60 + second);
        }

        public void setForDisplay(){}

        public String getForDisplay(){
            return String.format("%01d:%02d:%02d", hour, minute, second);
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
        this.words = removeXmlTags(words);
        String[] startAndStop = parseStartAndStopTime(time);
        startTime = new BlockTime(startAndStop[0]);
        stopTime = new BlockTime(startAndStop[1]);
    }

    private String[] parseStartAndStopTime(String time) {
        return time.split(" --> ");
    }

    public String getWords(){
        return words;
    }

    public String getId(){
        return id;
    }

    public BlockTime getStartTime(){
        return startTime;
    }

    public BlockTime getStopTime(){
        return stopTime;
    }

    public static String removeXmlTags(String input){
        String tagRegex = "<[^>]+>";
        String output = input.replaceAll(tagRegex, "");
        return output;
    }
}
