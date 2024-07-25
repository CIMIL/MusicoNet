package musico.services.user.config.kafka;

import musico.services.user.models.UserParams;
import musico.services.user.models.UserProfileDTO;
import org.apache.catalina.User;
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
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.serializer.DelegatingSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public ProducerFactory<String, UserProfileDTO> producerFactoryAuthProcess() {
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

    @Bean
    public KafkaTemplate<String, UserProfileDTO> kafkaTemplateAuthProcess() {
        return new KafkaTemplate<>(producerFactoryAuthProcess());
    }

    @Bean //register and configure replying kafka template
    public ReplyingKafkaTemplate<String, UserProfileDTO, UserProfileDTO> replyingTemplateAuthProcess(
            ProducerFactory<String, UserProfileDTO> pf,
            ConcurrentMessageListenerContainer<String, UserProfileDTO> repliesContainer) {
        ReplyingKafkaTemplate<String, UserProfileDTO, UserProfileDTO> replyTemplate = new ReplyingKafkaTemplate<>(pf, repliesContainer);
        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }

    @Bean
    public ConsumerFactory<String, UserProfileDTO> consumerFactoryAuthProcess() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "*");
        props.put(
                "group.id",
                "user-service"
        );
        props.put(
                "bootstrap.servers",
                bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(UserProfileDTO.class));
    }

    @Bean //register ConcurrentMessageListenerContainer bean
    public ConcurrentMessageListenerContainer<String, UserProfileDTO> repliesContainerAuthProcess(
            ConcurrentKafkaListenerContainerFactory<String, UserProfileDTO> containerFactory) {
        containerFactory.setRecordMessageConverter(jsonMessageConverter());
        containerFactory.setConsumerFactory(consumerFactoryAuthProcess());
        ConcurrentMessageListenerContainer<String, UserProfileDTO> repliesContainer = containerFactory.createContainer("profile-get-response");
        repliesContainer.getContainerProperties().setGroupId("auth-reply-group");
        return repliesContainer;
    }
    @Bean
    public JsonMessageConverter jsonMessageConverter() {
        return new ByteArrayJsonMessageConverter();
    }

    @Bean
    public ReplyingKafkaTemplate<String, UserParams, List<UserProfileDTO> > searchReplyingKafkaTemplate(
            ProducerFactory<String, UserParams> pf,
            ConcurrentMessageListenerContainer<String, List<UserProfileDTO>> repliesContainer) {
        ReplyingKafkaTemplate<String, UserParams, List<UserProfileDTO>> replyTemplate = new ReplyingKafkaTemplate<>(pf, repliesContainer);
        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, List<UserProfileDTO>> searchListenerContainer(
            ConcurrentKafkaListenerContainerFactory<String, List<UserProfileDTO>> containerFactory) {
        containerFactory.setRecordMessageConverter(jsonMessageConverter());
        containerFactory.setConsumerFactory(searchConsumerFactory());
        ConcurrentMessageListenerContainer<String, List<UserProfileDTO>> repliesContainer = containerFactory.createContainer("user-search-response");
        repliesContainer.getContainerProperties().setGroupId("search-reply-group");
        return repliesContainer;
    }

    @Bean
    public ConsumerFactory<String, List<UserProfileDTO>> searchConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "*");
        props.put(
                "group.id",
                "user-service"
        );
        props.put(
                "bootstrap.servers",
                bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(List.class));
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