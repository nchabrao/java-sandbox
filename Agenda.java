import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Agenda {
    //TODO: Write a function that takes a date as input and returns an object/hash containing the doctor's available slots for the next seven days, starting on the input date
    public static void main(String[] args){
        Console c = System.console();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }
        
        String url = c.readLine("Enter the database connection url: ");
        String login = c.readLine("Enter the database connection login: ");
        char [] password = c.readPassword("Enter the database connection password: ");
        
        PostgreClient client = new PostgreClient(url,login,new String(password));
        System.out.println("Connection established to "+client.getDBName());

        int hpId = Integer.parseInt(c.readLine("Enter the health professional ID: "));
        LocalDate startDate = LocalDate.now();
        int periodDays = Integer.parseInt(c.readLine("Enter the period (in days) to check for available timeslots: "));
        
        /* Instanciate an agenda object to run the business logic in a non static context */
        Agenda agenda = new Agenda();

        System.out.println("Checking available slots for " + periodDays + " days starting on " + startDate);
        
        Connection session = null;
        try{
            session = client.getSession();
            System.out.println("Session opened on database "+session.getSchema());
            List<AgendaSlot> slots = agenda.getNextFreeSlots(session, hpId, startDate, periodDays);
            printSlots(slots);
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

    }

    /** Static method that prints a given list of AgendaSlot objects to the standard output */
    public static void printSlots(List<AgendaSlot> slots){
        Iterator<AgendaSlot> iter = slots.iterator();
        if (!iter.hasNext()){
            System.out.println("There are no available time slots for the given practitioner and period");
        }
        AgendaSlot currentSlot = null;
        LocalDate currentDate = null;
        while(iter.hasNext()){
            currentSlot = iter.next();
            if (currentSlot.getDate() != currentDate){
                System.out.println("********** "+currentSlot.getDate()+" **********");
                currentDate = currentSlot.getDate();
            }
            System.out.println(currentSlot);
        }
    }

    public static String listSlots(List<AgendaSlot> slots){
        StringBuilder result = new StringBuilder();
        Iterator<AgendaSlot> iter = slots.iterator();
        if (!iter.hasNext()){
            result.append("There are no available time slots for the given practitioner and period</br>");
        }
        AgendaSlot currentSlot = null;
        LocalDate currentDate = null;
        while(iter.hasNext()){
            currentSlot = iter.next();
            if (currentSlot.getDate() != currentDate){
                result.append("********** "+currentSlot.getDate()+" **********</br>");
                currentDate = currentSlot.getDate();
            }
            result.append(currentSlot+"</br>");
        }
        return result.toString();
    }

    /** Method that checks if a health professional is available between two timestamps.
     *  Parameters:
     *      session - A database session to execute the read queries
     *      hpId - The unique identifier of the corresponding health professional in the health_professionals database table
     *      start - The starting date and time of the period over which the health professional's availibilty should be checked
     *      end - The starting date and time of the period over which the health professional's availibilty should be checked
     */
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
    
    /** Method that builds and returns a list of available timeslots for a given health professional over a given period.
     *  Parameters:
     *      session - A database session to execute the read queries
     *      hpId - The unique identifier of the corresponding health professional in the health_professionals database table
     *      start - The starting date and time of the period over which the health professional's availibilty should be checked
     *      periodDays - The number of days forward to check for available timeslots
     *      slotSize - The standard slot size in minutes to use.
     */
    public List<AgendaSlot> getNextFreeSlots(Connection session, int hpId, LocalDate startDate, int periodDays) throws SQLException{
        List<AgendaSlot> slotsList = new ArrayList<AgendaSlot>();
        LocalDateTime start = startDate.atTime(0,0);
        LocalDateTime end = start.plusDays(periodDays);
        PreparedStatement stmt = session.prepareStatement("SELECT start_datetime, end_datetime, slot_size FROM events "
                                                            +"WHERE event_type='opening' AND hp_id=? AND start_datetime >= ? AND end_datetime <= ? "
                                                            +"ORDER BY 2");
        
        stmt.setInt(1, hpId);
        stmt.setTimestamp(2, Timestamp.valueOf(start));
        stmt.setTimestamp(3, Timestamp.valueOf(end));

        System.out.println(stmt);
        
        ResultSet rs = stmt.executeQuery();
        if(!rs.next()){
            System.out.println("There are no available time slots for practitioner #"+hpId+" over " + periodDays +" days starting "+startDate);
        }
        else{
            do{
            LocalDateTime openingStart = rs.getTimestamp(1).toLocalDateTime();
            LocalDateTime openingEnd = rs.getTimestamp(2).toLocalDateTime();
            Duration duration = Duration.between(openingStart, openingEnd);
            int slotSize = rs.getInt(3);
            int numSlots = (int)(duration.toMinutes() / slotSize);
            LocalDateTime slotStart;
            LocalDateTime slotEnd;
            LocalDateTime current = openingStart;
            for (int i = 0; i < numSlots; i++) {
                slotStart = current;
                slotEnd = current.plusMinutes(slotSize);
                if (isAvailable(session, hpId, slotStart, slotEnd)) {
                    slotsList.add(new AgendaSlot(slotStart,slotEnd));
                }
                current = slotEnd;
            }
            } while(rs.next());
        }
        
        return slotsList;
    }

    public List<AgendaSlot> getNextFreeSlots(Connection session, int hpId) throws SQLException{
        return this.getNextFreeSlots(session, hpId, LocalDate.now(), 7);
    }
}
