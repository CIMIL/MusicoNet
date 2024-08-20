package musico.services.analysis.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.analysis.models.AnalysisMessage;
import musico.services.analysis.models.ResultMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
@Slf4j
@AllArgsConstructor
public class AnalysisListener {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "audio_analysis", groupId = "analysis-service",
            containerFactory = "analysisMessageContainerFactory")
    public void listen(AnalysisMessage message) {
        log.info("Received message from audio_analysis: {}", message.getUser());
        messagingTemplate.convertAndSendToUser(message.getUser(), "/queue/analysis/result", message);
    }

    @KafkaListener(topics = "analysis-query_params_response", groupId = "analysis-service",
            containerFactory = "queryResultContainerFactory")
    public void result(ResultMessage message) {
        log.info("Received query results: {}", message);
        messagingTemplate.convertAndSendToUser(message.requestID(),"/queue/query/result", message);
    }
    @MessageMapping("/info")
    @SendToUser("/queue/info")
    public String giveInfo(@Header("simpSessionId") String sessionId,@Payload String message, Principal principal) {
        log.info("Received message from user: {}, {}",sessionId, message);
        return "Hello " + principal.getName() + "!";
    }
}
