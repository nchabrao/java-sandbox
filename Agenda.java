import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;


public class Agenda {
    //TODO: Write a function that takes a date as input and returns an object/hash containing the doctor's available slots for the next seven days, starting on the input date
    public static void main(String[] args){
        LocalDate dateToday = LocalDate.now();
        System.out.println(dateToday);
        //TODO 1: Identify the date to check for doctor's availability.
        Period period = Period.between(dateToday, dateToday.plusDays(7));
        System.out.println(period);
        //TODO 2: Fetch the doctor's availability slots from source for the given period
        Agenda agenda = new Agenda();
        PostgreClient client = new PostgreClient();
        Connection session = null;
        try{
            session = client.getSession();
            boolean answer = agenda.isAvailable(session,1,LocalDateTime.of(2024, 3, 13, 10, 0, 0),LocalDateTime.now().plusHours(1));
            System.out.println("Practitioner #1 availabilty: "+answer);
        }
        catch(SQLException e){
            System.out.println("Database access error: "+e.getMessage());
            e.printStackTrace();
        }
        finally{
            try{
                if (session != null){
                session.close();
                }
            }
            catch(SQLException e){
                System.out.println("Could not close session because of SQLException: "+e.getMessage());
                e.printStackTrace();
            }
        }
        //TODO 3: Validate the input against the acceptance criteria

    }

    public boolean isAvailable(Connection session, int hpId, LocalDateTime start, LocalDateTime end) throws SQLException{
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
}
