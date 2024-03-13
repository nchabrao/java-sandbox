import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AgendaSlot {

    private LocalDateTime start;
    private LocalDateTime end;
    private Duration duration;


    public AgendaSlot(LocalDateTime start, LocalDateTime end){
        this.start = start;
        this.end = end;
        this.duration = Duration.between(start, end);
    }

    public String toString(){
        return "Start: "+start.toString()+" - End: "+end.toString()+" - Duration: "+duration.toMinutes()+" min";
    }

    public LocalDate getDate() {
        return start.toLocalDate();
    }
}
