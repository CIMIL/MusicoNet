package musico.services.databases.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.enums.REGISTRATION_ENUMS;
import musico.services.databases.models.Users;
import musico.services.databases.models.kafka.UsersQueryParams;
import musico.services.databases.services.UserProfileService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
// TODO: Handle Errors and Exceptions in Methods
public class UserProfileListener {

    private final UserProfileService userProfileService;
    private final KafkaTemplate<String, UsersQueryParams> kafkaTemplate;

    @KafkaListener(topics = "profile-creation", groupId = "databases-service",
            containerFactory = "usersQueryParamsListener")
    public void listenRegistration(UsersQueryParams signupData) {
        if (signupData == null) {
            log.error("Received null registration request");
            return;
        }
        log.info("Received registration request: {}", signupData);
        // Check if profile already exists
        REGISTRATION_ENUMS check = userProfileService.checkProfileAlreadyExists(signupData);
//        if (check != REGISTRATION_ENUMS.CHECK_VALID) {
//            log.error("User already exists: {}", check);
//            return;
//        }
        // Create user profile on GraphDB
        userProfileService.createUserProfile(signupData);
    }

    @KafkaListener(topics = "profile-get", groupId = "databases-service",
            containerFactory = "usersQueryParamsListener")
    @SendTo
    public UsersQueryParams getProfile(UsersQueryParams userSignup) {
        log.info("Received getProfile request: {}", userSignup.toString());
        UsersQueryParams response = userProfileService.getUserProfile(userSignup);
        if(response == null) {
            log.error("No results found for user: {}", userSignup);
            return UsersQueryParams.builder().userId("NOT_FOUND").build();
        }
        log.info("Response: {}", response);
        return response;
    }

    @KafkaListener(topics = "profile-get-username", groupId = "databases-service",
            containerFactory = "usersQueryParamsListener")
    @SendTo
    public List<UsersQueryParams> getProfilesByUsername(UsersQueryParams userSignup) {
        log.info("Received getProfileByUsername request: {}", userSignup.toString());
        return userProfileService.getUsersProfileByUsername(userSignup);
    }
}
