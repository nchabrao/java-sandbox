import java.sql.*;
import java.util.Calendar;

public class DataQuery {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost/test";
        String user = "username";
        String password = "password";

        try (
            Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement("SELECT * FROM events WHERE start BETWEEN ? AND ?")) {

            // Set start date to now
            Timestamp startDate = new Timestamp(System.currentTimeMillis());
            pst.setTimestamp(1, startDate);

            // Set end date to 7 days later
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.DATE, 7);
            Timestamp endDate = new Timestamp(cal.getTime().getTime());
            pst.setTimestamp(2, endDate);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                System.out.println(rs.getString(1)); // Replace with actual column name
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}