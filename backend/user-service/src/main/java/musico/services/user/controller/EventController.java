package musico.services.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.user.models.EventDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/events")
public class EventController {

    private final ReplyingKafkaTemplate<String, String, List<EventDTO>> recommendedEventsTemplate;
    private final ReplyingKafkaTemplate<String, EventDTO, List<EventDTO>> searchEventsTemplate;
    private final KafkaTemplate<String,EventDTO> saveEventTemplate;

    @GetMapping("/recommendations")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<List<EventDTO>> getRecommendations(Principal principal) {
        // Get User ID from principal
        String userId = principal.getName();
        ProducerRecord<String, String> record = new ProducerRecord<>("events-recommendation", userId, userId);
        record.headers().add(
                new RecordHeader(KafkaHeaders.REPLY_TOPIC, "events-recommendation-response".getBytes())
        );
        RequestReplyFuture<String, String, List<EventDTO>> future = recommendedEventsTemplate.sendAndReceive(record);
        try {
            future.getSendFuture().get(10, TimeUnit.SECONDS);
            log.info("Sent: {}", record);
            ConsumerRecord<String, List<EventDTO>> response = future.get(15, TimeUnit.SECONDS);
            return ResponseEntity.ok(response.value());
        } catch (Exception e) {
            log.error("Error while getting recommendations", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<List<EventDTO>> searchEvents(@RequestBody EventDTO event) {
        // Get User ID from principal
        ProducerRecord<String, EventDTO> record = new ProducerRecord<>("events-search", event);
        record.headers().add(
                new RecordHeader(KafkaHeaders.REPLY_TOPIC, "events-search-response".getBytes())
        );
        RequestReplyFuture<String, EventDTO, List<EventDTO>> future = searchEventsTemplate.sendAndReceive(record);
        try {
            ConsumerRecord<String, List<EventDTO>> response = future.get(10, TimeUnit.SECONDS);
            return ResponseEntity.ok(response.value());
        } catch (Exception e) {
            log.error("Error while searching events", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<EventDTO> saveEvent(@RequestBody EventDTO event) {
        ProducerRecord<String, EventDTO> record = new ProducerRecord<>("events-save", event);
        saveEventTemplate.send(record);
        return ResponseEntity.ok(event);
    }

}