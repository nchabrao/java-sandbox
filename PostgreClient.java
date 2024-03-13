import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class PostgreClient {

    private String url = "jdbc:postgresql://localhost:5432/sandbox";
    private String user = "postgres";
    private String password = "eikochan";
    private Connection session = null;

    public Connection getSession() {
        if (session == null) {
            try{
                Class.forName("org.postgresql.Driver");
                session = DriverManager.getConnection(this.url, this.user, this.password);
            }
            catch(SQLTimeoutException e) {
                System.out.println("Database connection attempt timed out: "+e.getMessage());
                e.printStackTrace();
            }
            catch(SQLException e){
                System.out.println("Database access error: "+e.getMessage());
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                System.out.println("Driver not found: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return session;
    }
    

}
