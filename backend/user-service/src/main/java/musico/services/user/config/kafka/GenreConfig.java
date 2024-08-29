package musico.services.user.config.kafka;

import musico.services.user.models.KafkaResponse;
import org.apache.kafka.common.serialization.IntegerSerializer;
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
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class GenreConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public ReplyingKafkaTemplate<String, Integer, KafkaResponse<List<String>>> genreKafkaTemplate(
            ProducerFactory<String, Integer> genreProducerFactory,
            ConcurrentKafkaListenerContainerFactory<String, KafkaResponse<List<String>>> containerFactory) {
        containerFactory.setConsumerFactory(genreConsumerFactory());
        ConcurrentMessageListenerContainer<String, KafkaResponse<List<String>>> container =
                containerFactory.createContainer("data-genres-all-response"
                , "data-instruments-all-response");
        ReplyingKafkaTemplate<String, Integer, KafkaResponse<List<String>>> replyTemplate =
                new ReplyingKafkaTemplate<>(genreProducerFactory, container);
        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }

    @Bean
    public ConsumerFactory<String, KafkaResponse<List<String>>> genreConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put("group.id", "user-service");
        props.put("bootstrap.servers", bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(KafkaResponse.class)
        );
    }

    @Bean
    public ProducerFactory<String, Integer> genreProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        IntegerSerializer integerSerializer = new IntegerSerializer();
        configProps.put("bootstrap.servers", bootstrapAddress);
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), integerSerializer);
    }
}
