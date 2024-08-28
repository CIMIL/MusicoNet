package musico.services.user.config.kafka;

import lombok.RequiredArgsConstructor;
import musico.services.user.models.KafkaResponse;
import musico.services.user.models.UserProfileDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.converter.BytesJsonMessageConverter;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.serializer.DelegatingSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class UserProfileDTOKafkaConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    private final JsonMessageConverter jsonMessageConverter;
    private final BytesJsonMessageConverter jsonBytesMessageConverter;

    @Bean
    public KafkaTemplate<String, UserProfileDTO> kafkaTemplateUserProfileDTO() {
        return new KafkaTemplate<>(producerFactoryUserProfileDTO());
    }

    @Bean
    public ProducerFactory<String, UserProfileDTO> producerFactoryUserProfileDTO() {
        Map<String, Object> configProps = new HashMap<>();
        JsonSerializer<UserProfileDTO> serializer = new JsonSerializer<>();
        serializer.setAddTypeInfo(false);
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                DelegatingSerializer.VALUE_SERIALIZATION_SELECTOR,
                "UserProfileDTO,org.springframework.kafka.support.serializer.JsonSerializer"
        );
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), serializer);
    }


    @Bean //register and configure replying kafka template
    public ReplyingKafkaTemplate<String, UserProfileDTO, KafkaResponse<UserProfileDTO>> replyingTemplateUserProfileDTO(
            ProducerFactory<String, UserProfileDTO> pf,
            ConcurrentMessageListenerContainer<String, KafkaResponse<UserProfileDTO>> repliesContainer) {
        ReplyingKafkaTemplate<String, UserProfileDTO, KafkaResponse<UserProfileDTO>> replyTemplate = new
        ReplyingKafkaTemplate<>(pf, repliesContainer);
        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }

    @Bean
    public ConsumerFactory<String, KafkaResponse<UserProfileDTO>> consumerFactoryUserProfileDTO() {
        Map<String, Object> props = new HashMap<>();
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put("group.id", "user-service");
        props.put("bootstrap.servers", bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(KafkaResponse.class)
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String,KafkaResponse<UserProfileDTO>> repliesContainerUserProfileDTO(
            ConcurrentKafkaListenerContainerFactory<String, KafkaResponse<UserProfileDTO>> containerFactory) {
        containerFactory.setRecordMessageConverter(jsonBytesMessageConverter);
        containerFactory.setConsumerFactory(consumerFactoryUserProfileDTO());
        ConcurrentMessageListenerContainer<String,KafkaResponse<UserProfileDTO>> repliesContainer =
                containerFactory.createContainer("profile-get-response");
        repliesContainer.getContainerProperties().setGroupId("auth-reply-group");
        return repliesContainer;
    }

    @Bean
    public ReplyingKafkaTemplate<String, UserProfileDTO, KafkaResponse<List<UserProfileDTO>>> ListUserProfileRKT(
            ProducerFactory<String, UserProfileDTO> pf,
            ConcurrentMessageListenerContainer<String, KafkaResponse<List<UserProfileDTO>>> repliesContainer) {
        ReplyingKafkaTemplate<String, UserProfileDTO, KafkaResponse<List<UserProfileDTO>>> replyTemplate = new
        ReplyingKafkaTemplate<>(pf, repliesContainer);
        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }
}
