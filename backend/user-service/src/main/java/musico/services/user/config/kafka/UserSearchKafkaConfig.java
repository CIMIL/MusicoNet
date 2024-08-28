package musico.services.user.config.kafka;

import lombok.RequiredArgsConstructor;
import musico.services.user.models.KafkaResponse;
import musico.services.user.models.UserParams;
import musico.services.user.models.UserProfileDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
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
public class UserSearchKafkaConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    private final JsonMessageConverter jsonMessageConverter;

    @Bean
    public ReplyingKafkaTemplate<String, UserParams, KafkaResponse<List<UserProfileDTO>>> searchReplyingKafkaTemplate(
            ProducerFactory<String, UserParams> pf,
            ConcurrentMessageListenerContainer<String, KafkaResponse<List<UserProfileDTO>>> repliesContainer) {
        ReplyingKafkaTemplate<String, UserParams, KafkaResponse<List<UserProfileDTO>>> replyTemplate = new
        ReplyingKafkaTemplate<>(pf, repliesContainer);
        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }

    @Bean
    public ConsumerFactory<String, KafkaResponse<List<UserProfileDTO>>> listConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        JsonDeserializer<KafkaResponse<List<UserProfileDTO>>> deserializer = new JsonDeserializer<>(KafkaResponse.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put("group.id", "user-service");
        props.put("bootstrap.servers", bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, KafkaResponse<List<UserProfileDTO>>> searchListenerContainer(
            ConcurrentKafkaListenerContainerFactory<String, KafkaResponse<List<UserProfileDTO>>> containerFactory) {
        containerFactory.setRecordMessageConverter(jsonMessageConverter);
        containerFactory.setConsumerFactory(listConsumerFactory());
        ConcurrentMessageListenerContainer<String, KafkaResponse<List<UserProfileDTO>>> repliesContainer =
        containerFactory.createContainer("user-search-response");
        repliesContainer.getContainerProperties().setGroupId("search-reply-group");
        return repliesContainer;
    }

    @Bean
    public ProducerFactory<String, UserParams> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        JsonSerializer<UserParams> serializer = new JsonSerializer<>();
        serializer.setAddTypeInfo(false);
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                DelegatingSerializer.VALUE_SERIALIZATION_SELECTOR,
                "UserParams,org.springframework.kafka.support.serializer.JsonSerializer"
        );
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), serializer);
    }
}
