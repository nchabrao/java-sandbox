import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Agenda {
    //TODO: Write a function that takes a date as input and returns an object/hash containing the doctor's available slots for the next seven days, starting on the input date
    public static void main(String[] args){
        String password = "eikochan";
        int hpId = 1;
        LocalDate startDate = LocalDate.now();
        Agenda agenda = new Agenda();

        //TODO 1: Identify the date to check for doctor's availability.
        System.out.println("Checking available slots for 7 days starting on "+startDate);
        
        //TODO 2: Fetch the doctor's availability slots from source for the given period
        
        PostgreClient client = new PostgreClient(password);
        Connection session = null;
        try{
            session = client.getSession();
            System.out.println("Session opened on database "+session.getSchema());
            /* 
            LocalDateTime start = LocalDateTime.of(2024, 3, 13, 10, 0, 0);
            LocalDateTime end = start.plusHours(1);
            boolean isAvailable = agenda.isAvailable(session,1,start,end);
            System.out.println("Practitioner #1 availabilty: "+answer);
            */
            List<AgendaSlot> results = agenda.getNextSevenFreeSlots(session, hpId, startDate);
            Iterator<AgendaSlot> iter = results.iterator();
            if (!iter.hasNext()){
                System.out.println("There are no available time slots for practitioner #"+hpId+" over 7 days starting "+startDate);
            }
            LocalDate currentDate = startDate;
            AgendaSlot currentSlot;
            while(iter.hasNext()){
                currentSlot = iter.next();
                if (currentSlot.getDate() != currentDate){
                    System.out.println("********** "+currentSlot.getDate()+" **********");
                    currentDate = currentSlot.getDate();
                }
                System.out.println(currentSlot);
            }
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
        //TODO 3: Validate the output against the acceptance criteria

    }

    public boolean isAvailable(Connection session, int hpId, LocalDateTime start, LocalDateTime end) throws SQLException{
        PreparedStatement stmt = session.prepareStatement("SELECT count(*) FROM events "
                                                                        +"WHERE event_type='opening' AND hp_id=? AND start_datetime <= ? AND end_datetime >= ? "
                                                                        +"AND NOT EXISTS ("
                                                                            +"SELECT event_id FROM events "
                                                                            +"WHERE event_type='appointment' AND hp_id=? "
                                                                            +"AND ((start_datetime >= ? AND start_datetime <= ?)"
                                                                                +"OR (end_datetime >= ? AND end_datetime <= ?)));");
        stmt.setInt(1, hpId);
        stmt.setTimestamp(2, Timestamp.valueOf(start));
        stmt.setTimestamp(3, Timestamp.valueOf(end));
        stmt.setInt(4, hpId);
        stmt.setTimestamp(5, Timestamp.valueOf(start));
        stmt.setTimestamp(6, Timestamp.valueOf(end));
        stmt.setTimestamp(7, Timestamp.valueOf(start));
        stmt.setTimestamp(8, Timestamp.valueOf(end));
        System.err.println(stmt);
        ResultSet results = stmt.executeQuery();
        if (results.next()){
            if (results.getInt(1) == 1) {
                return true;
            }
        }
        return false;
    }
    
    public List<AgendaSlot> getNextSevenFreeSlots(Connection session, int hpId, LocalDate startDate) throws SQLException{
        List<AgendaSlot> slotsList = new ArrayList<AgendaSlot>();
        LocalDateTime start = startDate.atTime(0,0);
        LocalDateTime end = start.plusHours(1);
        PreparedStatement stmt = session.prepareStatement("SELECT start_datetime, end_datetime FROM events "
                                                            +"WHERE event_type='opening' AND hp_id=? AND start_datetime <= ? AND end_datetime >= ? "
                                                            +"AND NOT EXISTS ("
                                                                +"SELECT event_id FROM events "
                                                                +"WHERE event_type='appointment' AND hp_id=? "
                                                                +"AND ((start_datetime >= ? AND start_datetime <= ?)"
                                                                    +"OR (end_datetime >= ? AND end_datetime <= ?)));");
        
        stmt.setInt(1, hpId);
        stmt.setTimestamp(2, Timestamp.valueOf(start));
        stmt.setTimestamp(3, Timestamp.valueOf(end));
        stmt.setInt(4, hpId);
        stmt.setTimestamp(5, Timestamp.valueOf(start));
        stmt.setTimestamp(6, Timestamp.valueOf(end));
        stmt.setTimestamp(7, Timestamp.valueOf(start));
        stmt.setTimestamp(8, Timestamp.valueOf(end));

        ResultSet rs = stmt.executeQuery();
        LocalDateTime slotStart;
        LocalDateTime slotEnd;
        while (rs.next()) {
            slotStart = rs.getTimestamp(1).toLocalDateTime();
            slotEnd = rs.getTimestamp(2).toLocalDateTime();
            slotsList.add(new AgendaSlot(slotStart,slotEnd)); // Replace with actual column name
        }
        return slotsList;
    }
}
