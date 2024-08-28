package musico.services.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.user.models.KafkaResponse;
import musico.services.user.models.UserParams;
import musico.services.user.models.UserProfileDTO;
import org.apache.catalina.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/user")
@Slf4j
public class SearchController {

    private final ReplyingKafkaTemplate<String, UserParams, KafkaResponse<List<UserProfileDTO>>> replyingKafkaTemplate;

    @PostMapping(path = "/search")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<List<UserProfileDTO>> searchUsers(@RequestBody UserParams userParams) throws ExecutionException, InterruptedException, TimeoutException {
        log.info("Searching users with Params: {}", userParams);
        ProducerRecord<String, UserParams> record = new ProducerRecord<>("user-search", userParams);
        RequestReplyFuture<String, UserParams, KafkaResponse<List<UserProfileDTO>>> future = replyingKafkaTemplate.sendAndReceive(record);
        future.getSendFuture().get(10, java.util.concurrent.TimeUnit.SECONDS);
        log.info("Sent: {}", record.value()) ;
        try{
            ConsumerRecord<String, KafkaResponse<List<UserProfileDTO>>> response = future.get(10, TimeUnit.SECONDS);
            log.info("Response: {}", response.value());
            return ResponseEntity.status(response.value().getStatus()).body(response.value().getPayload());
        }catch (Exception e){
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
