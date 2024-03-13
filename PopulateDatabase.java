import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class PopulateDatabase {

    private String url = "jdbc:postgresql://localhost:5432/sandbox";
    private String user = "postgres";
    private String password = "eikochan";
    private LocalDate start; 
    private int days;
    private Connection session = null;

    public PopulateDatabase(){
        this.start = LocalDate.now();
        this.days = 30;
    }

    public PopulateDatabase(LocalDate start, int days){
        this.start = start;
        this.days = days;
    }

    
    public static void main(String[] args){
        //PopulateDatabase db_pop = new PopulateDatabase(LocalDate.of(2024,5,18),100);
        //db_pop.populate();
        try{
           boolean answer = isAvailable(new PopulateDatabase().getSession(),1,LocalDateTime.of(2024, 3, 13, 10, 0, 0),LocalDateTime.now().plusHours(1));
           System.out.println("Practitioner #1 availabilty: "+answer);
        }
        catch(SQLException e){
            System.out.println("Database access error: "+e.getMessage());
            e.printStackTrace();
        }
    }

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
    
    public int getDays() {
        return days;
    }
    public LocalDate getStart(){
        return start;
    }

    public static boolean isAvailable(Connection session, int hpId, LocalDateTime start, LocalDateTime end) throws SQLException{
        PreparedStatement checkAvailability = session.prepareStatement("SELECT count(*) FROM events "
                                                                        +"WHERE event_type='opening' AND hp_id=? AND start_datetime <= ? AND end_datetime >= ? "
                                                                        +"AND NOT EXISTS ("
                                                                            +"SELECT event_id FROM events "
                                                                            +"WHERE event_type='appointment' AND hp_id=? "
                                                                            +"AND ((start_datetime >= ? AND start_datetime <= ?)"
                                                                                +"OR (end_datetime >= ? AND end_datetime <= ?)));");
        checkAvailability.setInt(1, hpId);
        checkAvailability.setTimestamp(2, Timestamp.valueOf(start));
        checkAvailability.setTimestamp(3, Timestamp.valueOf(end));
        checkAvailability.setInt(4, hpId);
        checkAvailability.setTimestamp(5, Timestamp.valueOf(start));
        checkAvailability.setTimestamp(6, Timestamp.valueOf(end));
        checkAvailability.setTimestamp(7, Timestamp.valueOf(start));
        checkAvailability.setTimestamp(8, Timestamp.valueOf(end));
        System.err.println(checkAvailability);
        ResultSet results = checkAvailability.executeQuery();
        if (results.next()){
            if (results.getInt(1) == 1) {
                return true;
            }
        }
        return false;
    }

    public void populate(){
        Connection session = getSession();
        PreparedStatement query = null;
        try {
            query = session.prepareStatement("INSERT INTO events (event_type, hp_id, start_datetime, end_datetime) values ('opening', 1, ?, ?)");
            LocalDate currentDate = skipWeekend(getStart());
            for (int i=0; i < getDays(); i++) {
                query.setTimestamp(1, Timestamp.valueOf(currentDate.atTime(9,0)));
                query.setTimestamp(2, Timestamp.valueOf(currentDate.atTime(18,0)));
                System.out.println(query);
                query.executeUpdate();
                currentDate = skipWeekend(currentDate.plusDays(1));
            }
        }
        catch(SQLException e) {
            System.out.println("Database access error: "+e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (session != null) {
                try {
                    session.close();
                }
                catch(SQLException e) {
                    System.out.println("Database access error: "+e.getMessage());
                    e.printStackTrace();
                }
            }
            if (query != null){
                try {
                    query.close();
                }
                catch(SQLException e) {
                    System.out.println("Database access error: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static LocalDate skipWeekend(LocalDate date){
        DayOfWeek day = date.getDayOfWeek();
        LocalDate newDate = date;
        switch (day){
            case DayOfWeek.SATURDAY:
            newDate = date.plusDays(2);
            break;
            case DayOfWeek.SUNDAY:
            newDate = date.plusDays(1);
            break;
        }
        return newDate;
    }
}
