package musico.services.user.config.kafka;

import lombok.RequiredArgsConstructor;
import musico.services.user.models.EventDTO;
import musico.services.user.models.KafkaResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
import org.springframework.kafka.support.converter.BytesJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class EventDTOKafkaConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public ReplyingKafkaTemplate<String, String, KafkaResponse<List<EventDTO>>> replyingTemplate(
            ProducerFactory<String, String> pf,
            ConcurrentKafkaListenerContainerFactory<String, KafkaResponse<List<EventDTO>>> containerFactory
    ) {
        containerFactory.setRecordMessageConverter(jsonBytesMessageConverter());
        containerFactory.setConsumerFactory(KafkaResponseConsumerFactory());
        ConcurrentMessageListenerContainer<String, KafkaResponse<List<EventDTO>>> repliesContainer =
                containerFactory.createContainer("events-recommendation-response");
        repliesContainer.getContainerProperties().setGroupId("event-group");
        ReplyingKafkaTemplate<String, String, KafkaResponse<List<EventDTO>>> replyTemplate = new ReplyingKafkaTemplate<>(pf, repliesContainer);
        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }

    @Bean
    public ReplyingKafkaTemplate<String, EventDTO, KafkaResponse<List<EventDTO>>> searchEventReplyingKafkaTemplate(
            ProducerFactory<String, EventDTO> pf,
            ConcurrentKafkaListenerContainerFactory<String, KafkaResponse<List<EventDTO>>> containerFactory
    ) {
        containerFactory.setRecordMessageConverter(jsonBytesMessageConverter());
        containerFactory.setConsumerFactory(KafkaResponseConsumerFactory());
        ConcurrentMessageListenerContainer<String, KafkaResponse<List<EventDTO>>> repliesContainer =
                containerFactory.createContainer("events-search-response");
        repliesContainer.getContainerProperties().setGroupId("event-group");

        ReplyingKafkaTemplate<String, EventDTO, KafkaResponse<List<EventDTO>>> replyTemplate =
                new ReplyingKafkaTemplate<>(pf, repliesContainer);
        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }

    @Bean
    public BytesJsonMessageConverter jsonBytesMessageConverter() {
        return new BytesJsonMessageConverter();
    }

    @Bean
    public ConsumerFactory<String, KafkaResponse<List<EventDTO>>> KafkaResponseConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        JsonDeserializer<KafkaResponse<List<EventDTO>>> deserializer = new JsonDeserializer<>(KafkaResponse.class);
        deserializer.addTrustedPackages("*");
        props.put("group.id", "user-service");
        props.put("bootstrap.servers", bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }


    @Bean
    public ConsumerFactory<String, List<EventDTO>> eventDTOConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put("group.id", "user-service");
        props.put("bootstrap.servers", bootstrapAddress);
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                "org.apache.kafka.clients.consumer.RoundRobinAssignor");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(List.class));
    }

    @Bean
    public ConsumerFactory<String, EventDTO> consumerFactoryEventDTO() {
        Map<String, Object> props = new HashMap<>();
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put("group.id", "user-service");
        props.put("bootstrap.servers", bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(EventDTO.class)
        );
    }

    @Bean
    public ProducerFactory<String, EventDTO> eventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        JsonSerializer<EventDTO> serializer = new JsonSerializer<>();
        serializer.setAddTypeInfo(false);
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), serializer);
    }

    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new StringSerializer());
    }
}
