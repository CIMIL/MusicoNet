package musico.services.databases.services;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.models.Users;
import musico.services.databases.utils.DataGenerator;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSenderService {

    private final KafkaTemplate<String, String> producer;
    private static final DataGenerator ttlGen = new DataGenerator();

    public void sendUser(Users user) {
        try {
            List<String> data = ttlGen.genUserData(user);
            String fullData = String.join("", data);
            CompletableFuture<SendResult<String, String>> future = producer.send(new ProducerRecord<>("gdb-add", fullData));
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Error sending user data: {}", ex.getMessage());
                } else {
                    log.info("User data sent successfully: {}", result);
                }
            });
        } catch (IllegalAccessException e) {
            log.error("Error sending user data: {}", e.getMessage());
        }
    }

    public void sendData(List<String> data) {
        for (String row : data) {
            CompletableFuture<SendResult<String, String>> future = producer.send(new ProducerRecord<>("gdb-add", row));
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Error sending data: {}", ex.getMessage());
                } else {
                    log.info("Data sent successfully: {}", result);
                }
            });
        }
    }
}
