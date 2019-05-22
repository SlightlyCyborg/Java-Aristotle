import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class DBConnectionTest extends TestCase{

    public static class DBTestObject{
        String id;
        DBTestObject(String id){
            this.id = id;
        }
    }

    public static class DBTestObjectExtractor extends SimpleDBResultExtractor<DBTestObject>{

        @Override
        public void extractInstancesFromDBResult(ResultSet rs) {
            //TODO increase speed of this by using indices into the rs instead of column names
            try {
                while (rs.next()) {
                    instances.add(new DBTestObject(rs.getString("id")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void testQuery(){
        DBTestObjectExtractor extractor = new DBTestObjectExtractor();
        try {
        	DBConnection.makeQuery(extractor, "SELECT * from \"db-connection-test\"");
        } catch(Exception e) {
        	fail(e.getMessage());
        }
        List<DBTestObject> testObjs = extractor.getInstances();
        assertEquals(testObjs.get(0).id, "foo");
        assertEquals(testObjs.get(1).id, "bar");
        assertEquals(testObjs.get(2).id, "baz");
    }

    public void testUpdate(){
        //prep just in case cleanup failed
        int deleted = DBConnection.makeUpdate("delete from \"db-connection-test\" where id='ding'");

        DBTestObjectExtractor extractor = new DBTestObjectExtractor();

        int inserted = DBConnection.makeUpdate("insert into \"db-connection-test\" VALUES ('ding')");
        try {
        	DBConnection.makeQuery(extractor, "select * from \"db-connection-test\" where id='ding'");
        } catch (DBConnection.DBQueryException e) {
        	fail(e.getMessage());
        }
        List<DBTestObject> testObjs = extractor.getInstances();
        assertEquals(testObjs.size(), 1);

        //cleanup
        deleted = DBConnection.makeUpdate("delete from \"db-connection-test\" where id='ding'");
    }
}
