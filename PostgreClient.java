import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class PostgreClient {

    private String url = "jdbc:postgresql://localhost:5432/sandbox";
    private String user = "postgres";
    private String password;
    private Connection session = null;

    public PostgreClient(String password){
        this.password = password;
    }
    
    public PostgreClient(String url, String user, String password){
        this.url = url;
        this.user = user;
        this.password = password;
    }
    
    public Connection getSession() throws SQLTimeoutException, SQLException {
        if (session == null) {
            try{
                session = DriverManager.getConnection(this.url, this.user, this.password);
            }
            catch(SQLTimeoutException e) {
                System.out.println("Database connection attempt timed out: "+e.getMessage());
                throw e;
            }
            catch(SQLException e){
                System.out.println("Database access error: "+e.getMessage());
                throw e;
            }
        }
        return session;
    }
    

}
