package musico.services.analysis.config.kafka;

import musico.services.analysis.models.AnalysisMessage;
import musico.services.analysis.models.ResultMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;


    @Bean
    public ConsumerFactory<String, AnalysisMessage> analysisMessageConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "analysis-service");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(AnalysisMessage.class));
    }


    @Bean
    public ConsumerFactory<String, ResultMessage> queryResultConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "analysis-service");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(ResultMessage.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,AnalysisMessage> analysisMessageContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AnalysisMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(analysisMessageConsumerFactory());
        return factory;
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ResultMessage> queryResultContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ResultMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(queryResultConsumerFactory());
        return factory;
    }
}