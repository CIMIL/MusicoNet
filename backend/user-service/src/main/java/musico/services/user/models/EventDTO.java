package musico.services.user.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDTO {
    String eventId;
    String name;
    String description;
    LocalDateTime datetime;
    String location;
    String genre;

}
