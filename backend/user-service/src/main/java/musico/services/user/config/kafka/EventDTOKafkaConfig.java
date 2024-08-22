package musico.services.user.config.kafka;

import lombok.RequiredArgsConstructor;
import musico.services.user.models.EventDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.time.Duration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class EventDTOKafkaConfig {

    @Bean
    public ReplyingKafkaTemplate<String, String, List<EventDTO>> eventReplyingKafkaTemplate(
            ProducerFactory<String, String> pf,
            ConcurrentMessageListenerContainer<String, List<EventDTO>> repliesContainer
    ) {
        ReplyingKafkaTemplate<String, String, List<EventDTO>> replyTemplate = new ReplyingKafkaTemplate<>(pf, repliesContainer);
        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, List<EventDTO>> eventListenerContainer(
            ConcurrentKafkaListenerContainerFactory<String, List<EventDTO>> containerFactory
    ) {
        ConcurrentMessageListenerContainer<String, List<EventDTO>> repliesContainer = containerFactory.createContainer("event-response");
        repliesContainer.getContainerProperties().setGroupId("event-reply-group");
        return repliesContainer;
    }

}
