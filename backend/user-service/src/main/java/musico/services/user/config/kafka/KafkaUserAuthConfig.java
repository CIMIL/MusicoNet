package musico.services.user.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class KafkaUserAuthConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    //    @Bean
//    public ProducerFactory<String, UserAuth> producerFactoryUserAuth() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(
//                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
//                bootstrapAddress);
//        configProps.put(
//                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//                StringSerializer.class);
//        configProps.put(
//                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    @Bean //register and configure replying kafka template
//    public ReplyingKafkaTemplate<String, UserAuth, UserAuth> replyingTemplateUserAuth(
//            ProducerFactory<String, UserAuth> pf,
//            ConcurrentMessageListenerContainer<String, UserAuth> repliesContainer) {
//        ReplyingKafkaTemplate<String, UserAuth, UserAuth> replyTemplate = new ReplyingKafkaTemplate<>(pf, repliesContainer);
//        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
//        replyTemplate.setSharedReplyTopic(true);
//        return replyTemplate;
//    }
//
//    @Bean //register ConcurrentMessageListenerContainer bean
//    public ConcurrentMessageListenerContainer<String, UserAuth> repliesContainerUserAuth(
//            ConcurrentKafkaListenerContainerFactory<String, UserAuth> containerFactory) {
//        ConcurrentMessageListenerContainer<String, UserAuth> repliesContainer = containerFactory.createContainer("user-login-reply");
//        repliesContainer.getContainerProperties().setGroupId("auth-reply-group");
//        repliesContainer.setAutoStartup(false);
//        return repliesContainer;
//    }
//
    @Bean
    public NewTopic UserAuthLogin() {
        return new NewTopic("user-login", 1, (short) 1);
    }
}
