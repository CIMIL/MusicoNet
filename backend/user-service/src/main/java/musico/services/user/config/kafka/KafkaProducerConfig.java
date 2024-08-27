package musico.services.user.config.kafka;

import musico.services.user.models.EventDTO;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public JsonMessageConverter jsonMessageConverter() {
        return new ByteArrayJsonMessageConverter();
    }
//    @Bean
//    public ConsumerFactory<String, List<?>> listConsumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(
//                JsonDeserializer.TRUSTED_PACKAGES,
//                "*");
//        props.put(
//                "group.id",
//                "user-service"
//        );
//        props.put(
//                "bootstrap.servers",
//                bootstrapAddress);
//        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(List.class));
//    }
}