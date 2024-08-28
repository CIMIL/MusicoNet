package musico.services.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.user.models.EventDTO;
import musico.services.user.models.KafkaResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.http.ResponseEntity;
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

    private final ReplyingKafkaTemplate<String, String, KafkaResponse<List<EventDTO>>> stringKafkaTemplate;
    private final ReplyingKafkaTemplate<String, EventDTO, KafkaResponse<List<EventDTO>>> eventKafkaTemplate;

    @GetMapping("/recommendations")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<List<EventDTO>> getRecommendations(Principal principal) {
        // Get User ID from principal
        String userId = principal.getName();
        ProducerRecord<String, String> record = new ProducerRecord<>("events-recommendation", userId, userId);
        record.headers().add(
                new RecordHeader(KafkaHeaders.REPLY_TOPIC, "events-recommendation-response".getBytes())
        );
        RequestReplyFuture<String, String, KafkaResponse<List<EventDTO>>> future = stringKafkaTemplate.sendAndReceive(record);
        try {
            future.getSendFuture().get(10, TimeUnit.SECONDS);
            log.info("Sent: {}", record);
            ConsumerRecord<String, KafkaResponse<List<EventDTO>>> response = future.get(15, TimeUnit.SECONDS);
            log.info("Received recommendation: {}", response);
            return ResponseEntity.status(response.value().getStatus()).body(response.value().getPayload());
        } catch (Exception e) {
            log.error("Error while getting recommendations", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<List<EventDTO>> searchEvents(@RequestBody EventDTO event) {
        log.info("Searching for events: {}", event);
        // Get User ID from principal
        ProducerRecord<String, EventDTO> record = new ProducerRecord<>("events-search", event);
        record.headers().add(
                new RecordHeader(KafkaHeaders.REPLY_TOPIC, "events-search-response".getBytes())
        );
        RequestReplyFuture<String, EventDTO, KafkaResponse<List<EventDTO>>> future = eventKafkaTemplate.sendAndReceive(record);
        try {
            future.getSendFuture().get(10, TimeUnit.SECONDS);
            log.info("Sent search request: {}", record);
            ConsumerRecord<String, KafkaResponse<List<EventDTO>>> response = future.get(10, TimeUnit.SECONDS);
            log.info("Received search results: {}", response);
            return ResponseEntity.status(response.value().getStatus()).body(response.value().getPayload());
        } catch (Exception e) {
            log.error("Error while searching events", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<EventDTO> saveEvent(@RequestBody EventDTO event) {
        ProducerRecord<String, EventDTO> record = new ProducerRecord<>("events-save", event);
        try {
            RequestReplyFuture<String, EventDTO, KafkaResponse<List<EventDTO>>> future = eventKafkaTemplate.sendAndReceive(record);
            future.getSendFuture().get(10, TimeUnit.SECONDS);
            log.info("Sent save request: {}", record);
            ConsumerRecord<String, KafkaResponse<List<EventDTO>>> response = future.get(10, TimeUnit.SECONDS);
            log.info("Received save response: {}", response);
            return ResponseEntity.status(response.value().getStatus()).body(response.value().getPayload().get(0));
        } catch (Exception e) {
            log.error("Error while saving event", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/join")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<EventDTO> joinEvent(@RequestBody EventDTO event, Principal principal) {
        if (event.getEventId() == null) {
            return ResponseEntity.badRequest().build();
        }
        log.info("Joining event: {}", event);
        // Get User ID from principal
        event.setUserId(principal.getName());
        try {
            ProducerRecord<String, EventDTO> record = new ProducerRecord<>("events-join", event);
            RequestReplyFuture<String, EventDTO, KafkaResponse<List<EventDTO>>> future = eventKafkaTemplate.sendAndReceive(record);
            future.getSendFuture().get(10, TimeUnit.SECONDS);
            log.info("Sent join request: {}", record);
            ConsumerRecord<String, KafkaResponse<List<EventDTO>>> response = future.get(10, TimeUnit.SECONDS);
            log.info("Received join response: {}", response);
            return ResponseEntity.status(response.value().getStatus()).body(response.value().getPayload().get(0));
        } catch (Exception e) {
            log.error("Error while joining event", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}