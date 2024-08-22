package musico.services.user.models;

import java.time.LocalDateTime;

public record EventDTO(String id, String name, String description, LocalDateTime datetime, String location, String genre,  UserProfileDTO[] attendees, String hostId) {
}
