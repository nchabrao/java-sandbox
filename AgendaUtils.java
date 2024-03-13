import java.time.DayOfWeek;
import java.time.LocalDate;

public class AgendaUtils {
    
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
            default:
            break;
        }
        return newDate;
    }
}
