import java.util.Queue;

public class IndexProgress {

    public IndexProgress(Instance current, Queue<Instance> instancesRemaining){
        currentlyIndexing = current;
        this.instancesRemaining = instancesRemaining;
    }

    private Instance currentlyIndexing;
    private Queue<Instance> instancesRemaining;
    String toHTML(){
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>Indexer Progress</h1>");
        sb.append("<h2>Indexing in progress for: ");
        if(currentlyIndexing != null){
            sb.append(currentlyIndexing.getUsername());
        } else {
            sb.append("null");
        }
        sb.append("</h2>");
        sb.append("<h2>Instances Remaining</h2>");
        sb.append("<ul>");
        for(Instance remaining: instancesRemaining){
            sb.append("<li>");
            sb.append(remaining.getUsername());
            sb.append("</li>");
        }
        sb.append("</ul></body></html>");
        return sb.toString();
    }
}
