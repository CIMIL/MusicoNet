package musico.services.databases.models.kafka;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MusicalEventDTO(String eventId,
                              String userId,
                              String name,
                              String description,
                              LocalDateTime datetime,
                              String location,
                              String genre
                             ) {
}
