package musico.services.databases.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.models.kafka.KafkaResponse;
import musico.services.databases.models.kafka.MusicalEventDTO;
import musico.services.databases.services.kafka.MusicalEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    private final MusicalEventService musicalEventService;

    @KafkaListener(topics = "events-recommendation", groupId = "databases-service",
            splitIterables = false)
    @SendTo
    public KafkaResponse<List<MusicalEventDTO>> recommendedEventByUserId(String userId) {
        log.info("Received recommendation request for user: {}", userId);
        KafkaResponse<List<MusicalEventDTO>> events = musicalEventService.getRecommendedEvents(userId);
        addTestData(events);
        return events;
    }

    @KafkaListener(topics = "events-save", groupId = "databases-service",
            containerFactory = "musicalEventDTOContainerFactory", splitIterables = false)
    public KafkaResponse<List<MusicalEventDTO>> save(MusicalEventDTO event) {
        log.info("Received event save request: {}", event);
        return musicalEventService.saveEvent(event);
    }

    @KafkaListener(topics = "events-search", groupId = "databases-service",
            containerFactory = "musicalEventDTOContainerFactory", splitIterables = false)
    @SendTo
    public KafkaResponse<List<MusicalEventDTO>> searchEvent(MusicalEventDTO event) {
        log.info("Received event get request for event: {}", event);
        return musicalEventService.searchEvents(event);
    }

    @KafkaListener(topics = "events-delete", groupId = "databases-service",
            containerFactory = "musicalEventDTOContainerFactory", splitIterables = false)
    public void delete(MusicalEventDTO event) {
        log.info("Received event delete request: {}", event);
        musicalEventService.deleteEvent(event.eventId());
    }

    @KafkaListener(topics = "events-join", groupId = "databases-service",
            containerFactory = "musicalEventDTOContainerFactory", splitIterables = false)
    public KafkaResponse<List<MusicalEventDTO>> join(MusicalEventDTO event) {
        log.info("Received event join request: {}", event);
        musicalEventService.joinEvent(event);
        return KafkaResponse.<List<MusicalEventDTO>>builder()
                .payload(List.of(event))
                .message("OK")
                .status(200)
                .build();
    }

    private static void addTestData(KafkaResponse<List<MusicalEventDTO>> events) {
        if (events.getPayload() == null) {
            return;
        }
        events.getPayload().add(MusicalEventDTO.builder()
                .eventId("1")
                .name("Test event")
                .description("Test description")
                .datetime(LocalDateTime.now())
                .location("Test location")
                .build());
    }
}
