import java.util.ArrayList;
import java.util.List;

public abstract class SimpleDBResultExtractor<O> implements DBResultExtractor<O>{
    List<O> instances;

    public SimpleDBResultExtractor(){
        instances = new ArrayList<O>();
    }

    public List<O> getInstances() {
        return instances;
    }
}
