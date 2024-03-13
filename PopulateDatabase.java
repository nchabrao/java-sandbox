import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Timestamp;

public class PopulateDatabase {

    
    private LocalDate start; 
    private int days;

    public PopulateDatabase(){
        this.start = LocalDate.now();
        this.days = 30;
    }

    public PopulateDatabase(LocalDate start, int days){
        this.start = start;
        this.days = days;
    }
   
    public static void main(String[] args){
        String password = args[0];
        LocalDate start_date = LocalDate.of(2024,10,5);
        int duration = 22;
        PopulateDatabase db_pop = new PopulateDatabase(start_date,duration);
        db_pop.populate(new PostgreClient(password));
    }
    
    public int getDays() {
        return days;
    }
    
    public LocalDate getStart(){
        return start;
    }

    public void populate(PostgreClient client){
        Connection session = null;
        PreparedStatement statement = null;
        try {
            session = client.getSession();
            statement = session.prepareStatement("INSERT INTO events (event_type, hp_id, start_datetime, end_datetime) values ('opening', 1, ?, ?)");
            LocalDate currentDate = AgendaUtils.skipWeekend(getStart());
            for (int i=0; i < getDays(); i++) {
                statement.setTimestamp(1, Timestamp.valueOf(currentDate.atTime(9,0)));
                statement.setTimestamp(2, Timestamp.valueOf(currentDate.atTime(18,0)));
                System.out.println(statement);
                statement.executeUpdate();
                currentDate = AgendaUtils.skipWeekend(currentDate.plusDays(1));
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
            if (statement != null){
                try {
                    statement.close();
                }
                catch(SQLException e) {
                    System.out.println("Database access error: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    
}
