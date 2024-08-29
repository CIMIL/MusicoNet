package musico.services.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.user.models.KafkaResponse;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/data")
@RequiredArgsConstructor
public class DataController {
    private final ReplyingKafkaTemplate<String, Integer, KafkaResponse<List<String>>> kafkaTemplate;

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getGenres() {
        ProducerRecord<String, Integer> record = new ProducerRecord<>("data-genres-all", 0);
        record.headers().add(KafkaHeaders.REPLY_TOPIC, "data-genres-all-response".getBytes());
        try {
            KafkaResponse<List<String>> response = kafkaTemplate.sendAndReceive(record).get().value();
            return ResponseEntity.status(response.getStatus()).body(response.getPayload());
        } catch (Exception e) {
            log.error("Error getting genres", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/instruments")
    public ResponseEntity<List<String>> getInstruments() {
        ProducerRecord<String, Integer> record = new ProducerRecord<>("data-instruments-all", 0);
        record.headers().add(KafkaHeaders.REPLY_TOPIC, "data-instruments-all-response".getBytes());
        try {
            KafkaResponse<List<String>> response = kafkaTemplate.sendAndReceive(record).get().value();
            return ResponseEntity.status(response.getStatus()).body(response.getPayload());
        } catch (Exception e) {
            log.error("Error getting instruments", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
