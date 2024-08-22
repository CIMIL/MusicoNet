package musico.services.user.controller;

import lombok.RequiredArgsConstructor;
import musico.services.user.models.EventDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final ReplyingKafkaTemplate<String, String, List<EventDTO>> recommendedEventsTemplate;

    @GetMapping("/recommendations")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<List<EventDTO>> getRecommendations(Principal principal) {
        // Get User ID from principal
        String userId = principal.getName();
        ProducerRecord<String, String> record = new ProducerRecord<>("events-recommendation", userId, userId);
        record.headers().add(KafkaHeaders.REPLY_TOPIC, "events-recommendation-response".getBytes());
        RequestReplyFuture<String, String, List<EventDTO>> future = recommendedEventsTemplate.sendAndReceive(record);
        try {
            future.getSendFuture().get(10, TimeUnit.SECONDS);
            ConsumerRecord<String, List<EventDTO>> response = future.get(10, TimeUnit.SECONDS);
            return ResponseEntity.ok(response.value());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
