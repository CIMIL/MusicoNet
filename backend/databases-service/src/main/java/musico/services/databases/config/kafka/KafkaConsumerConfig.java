package musico.services.databases.config.kafka;

import lombok.RequiredArgsConstructor;
import musico.services.databases.models.kafka.MusicalWorkQueryParams;
import musico.services.databases.models.kafka.UserSearchParams;
import musico.services.databases.models.kafka.UsersQueryParams;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    private final KafkaTemplate<String, UsersQueryParams> usersQueryParamsTemplate;

    @Bean
    public ConsumerFactory<String, MusicalWorkQueryParams> musicalWorkQueryParamsConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "database-service");
        props.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class.getName()
        );
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(MusicalWorkQueryParams.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MusicalWorkQueryParams> musicalWorkQueryParamsListener() {
        ConcurrentKafkaListenerContainerFactory<String, MusicalWorkQueryParams> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(musicalWorkQueryParamsConsumerFactory());
//        factory.setReplyTemplate(kafkaTemplate);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UsersQueryParams> usersQueryParamsConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        // Add trusted package for deserialization
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "database-service");
        props.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class.getName()
        );
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(UsersQueryParams.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UsersQueryParams> usersQueryParamsListener() {
        ConcurrentKafkaListenerContainerFactory<String, UsersQueryParams> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(usersQueryParamsConsumerFactory());
        factory.setReplyTemplate(usersQueryParamsTemplate);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserSearchParams> userSearchParamsListener() {
        ConcurrentKafkaListenerContainerFactory<String, UserSearchParams> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userSearchParamsConsumerFactory());
        factory.setReplyTemplate(usersQueryParamsTemplate);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UserSearchParams> userSearchParamsConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        // Add trusted package for deserialization
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "database-service");
        props.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class.getName()
        );
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(UserSearchParams.class));
    }

}