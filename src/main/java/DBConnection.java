import java.sql.*;
import java.util.function.Function;

public class DBConnection {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.postgresql.Driver";
    static final String DB_URL = "jdbc:postgresql://localhost/aristotle";

    //  Database credentials
    static final String USER = "aristotle";
    static final String PASS = "97obw!nmusk>";

    public static int makeUpdate(String sql){
        int numberOfDocsInserted;
        Connection conn = null;
        Statement stmt = null;
        try{
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();
            numberOfDocsInserted = stmt.executeUpdate(sql);

            stmt.close();
            conn.close();

            return numberOfDocsInserted;
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try

        return -1; //indicate error
    }

    public static int makeUpdate(String parameterizedSQL, Object... objects){
        int numberOfDocsInserted;
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.prepareStatement(parameterizedSQL);
            //numberOfDocsInserted = stmt.executeUpdate(sql);
            int i = 1;
            for (Object o:objects){
                stmt.setObject(i, o);
                i++;
            }

            numberOfDocsInserted = stmt.executeUpdate();

            stmt.close();
            conn.close();

            return numberOfDocsInserted;
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try

        return -1; //indicate error
    }


    public static <O> void makeQuery(DBResultExtractor<O> extractor, String sql) {
        Connection conn = null;
        Statement stmt = null;
        Object rv;
        try{
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            //STEP 5: Extract data from result set
            extractor.extractInstancesFromDBResult(rs);

            //STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
    }
}
