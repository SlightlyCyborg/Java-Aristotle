import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DBResultExtractor<O> {
    public void extractInstancesFromDBResult(ResultSet rs) throws SQLException;
    public List<O> getInstances();
}
