package musico.services.databases.listeners;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.models.kafka.UserSearchParams;
import musico.services.databases.models.kafka.UsersQueryParams;
import musico.services.databases.services.UserProfileService;
import musico.services.databases.services.UserService;
import org.apache.catalina.User;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchListener {

    private final UserProfileService userProfileService;

    @KafkaListener(topics = "user-search", groupId = "databases-service",
            containerFactory = "userSearchParamsListener", splitIterables = false)
    @SendTo
    public List<UsersQueryParams> listen(UserSearchParams params) {
        log.info("Received search request: {}", params);
        return userProfileService.searchUsers(params);
    }
}
