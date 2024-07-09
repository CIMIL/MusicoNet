package musico.services.analysis.controller;

import lombok.AllArgsConstructor;
import musico.services.analysis.models.AnalysisMessage;
import musico.services.analysis.models.ResultMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;


@Component
@Slf4j
@AllArgsConstructor
public class AnalysisListener {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "audio_analysis", groupId = "analysis-service",
            containerFactory = "analysisMessageContainerFactory")
    public void listen(AnalysisMessage message) {
        log.info("Received message from audio_analysis: {}", message.getUser());
        messagingTemplate.convertAndSendToUser(message.getUser(), "/analysis/result", message.getDanceability());
    }

    @KafkaListener(topics = "analysis-query_params_response", groupId = "analysis-service",
            containerFactory = "queryResultContainerFactory")
    public void result(ResultMessage message) {
        log.info("Received query results: {}", message);
        messagingTemplate.convertAndSendToUser(message.userId(),"/query/result", message);
    }
}
