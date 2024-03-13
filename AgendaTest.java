import java.time.LocalDate;
import java.time.Period;

public class AgendaTest {
    //TODO: Write a function that takes a date as input and returns an object/hash containing the doctor's available slots for the next seven days, starting on the input date
    public static void main(String[] args){
        LocalDate dateToday = LocalDate.now();
        System.out.println(dateToday);
        //TODO 1: Identify the period to check for doctor's availability.
        Period period = Period.between(dateToday, dateToday.plusDays(7));
        System.out.println(period);
        //TODO 2: Fetch the doctor's availability slots from source for the given period

        //TODO 3: Validate the input against the 

    }


}
