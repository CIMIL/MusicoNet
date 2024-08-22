package musico.services.databases.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.models.kafka.MusicalEventDTO;
import musico.services.databases.services.kafka.MusicalEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    private final MusicalEventService musicalEventService;

    @KafkaListener(topics = "events-recommendation", groupId = "databases-service",
            containerFactory = "musicalEventDTOContainerFactory", splitIterables = false)
    @SendTo
    public List<MusicalEventDTO> recommendedEventByUserId(String userId) {
        log.info("Received recommendation request for user: {}", userId);
        return musicalEventService.getRecommendedEvents(userId);
    }

    @KafkaListener(topics = "events-save", groupId = "databases-service",
            containerFactory = "musicalEventDTOContainerFactory", splitIterables = false)
    public void save(MusicalEventDTO event) {
        log.info("Received event save request: {}", event);
        musicalEventService.saveEvent(event);
    }

    @KafkaListener(topics ="events-get", groupId = "databases-service",
            containerFactory = "musicalEventDTOContainerFactory", splitIterables = false)
    @SendTo
    public List<MusicalEventDTO> getEvent(MusicalEventDTO event) {
        log.info("Received event get request for event: {}", event);
        return musicalEventService.getEvents(event);
    }

    @KafkaListener(topics = "events-delete", groupId = "databases-service",
            containerFactory = "musicalEventDTOContainerFactory", splitIterables = false)
    public void delete(MusicalEventDTO event) {
        log.info("Received event delete request: {}", event);
        musicalEventService.deleteEvent(event.eventId());
    }
}
