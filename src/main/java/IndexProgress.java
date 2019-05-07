import java.util.Queue;

public class IndexProgress {

    public IndexProgress(Instance current, Queue<Instance> instancesRemaining){
        currentlyIndexing = current;
        this.instancesRemaining = instancesRemaining;
    }

    private Instance currentlyIndexing;
    private Queue<Instance> instancesRemaining;
    String toHTML(){
        return "";
    }
}
