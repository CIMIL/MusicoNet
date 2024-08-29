package musico.services.databases.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.models.kafka.KafkaResponse;
import musico.services.databases.models.kafka.MusicalWorkQueryParams;
import musico.services.databases.models.kafka.UsersQueryParams;
import musico.services.databases.services.UserProfileService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileListener {

    private final UserProfileService userProfileService;

    @KafkaListener(topics = "profile-creation", groupId = "databases-service",
            containerFactory = "usersQueryParamsListener")
    public KafkaResponse<List<UsersQueryParams>> listenRegistration(UsersQueryParams signupData) {
        KafkaResponse.KafkaResponseBuilder<List<UsersQueryParams>> response = KafkaResponse.builder();
        if (signupData == null) {
            log.error("Received null registration request");
            return response.status(400).message("Received null registration request").build();
        }
        log.info("Received registration request: {}", signupData);
        return userProfileService.createUserProfile(signupData);
    }


    /**
     * Listens for messages on the "profile-get" topic and retrieves the user profile.
     *
     * @param userSignup The user profile data containing the user ID to search for.
     * @return The UsersQueryParams object containing the user profile data, or a UsersQueryParams object with userId "NOT_FOUND" if no profile is found.
     */
    @KafkaListener(topics = "profile-get", groupId = "databases-service",
            containerFactory = "usersQueryParamsListener")
    @SendTo
    public KafkaResponse<UsersQueryParams> getProfile(UsersQueryParams userSignup) {
        KafkaResponse.KafkaResponseBuilder<UsersQueryParams> response = KafkaResponse.builder();
        log.info("Received getProfile request: {}", userSignup.toString());
        UsersQueryParams data = userProfileService.getUserProfile(userSignup);
        if (data == null) {
            log.error("No results found for user: {}", userSignup);
            return response.status(404).message("No results found for user: " + userSignup).build();
        }
        log.info("Response: {}", response);
        return response.payload(data).status(200).message("OK").build();
    }

    /**
     * Listens for messages on the "profile-get-username" topic and retrieves user profiles by username.
     *
     * @param userSignup The user profile data containing the username to search for.
     * @return A list of UsersQueryParams that match the provided username.
     */
    @KafkaListener(topics = "profile-get-username", groupId = "databases-service",
            containerFactory = "usersQueryParamsListener")
    @SendTo
    public List<UsersQueryParams> getProfilesByUsername(UsersQueryParams userSignup) {
        log.info("Received getProfileByUsername request: {}", userSignup.toString());
        return userProfileService.getUsersProfileByUsername(userSignup);
    }

    /**
     * Listens for messages on the "profile-update" topic and updates the user profile.
     *
     * @param userSignup The user profile data to update.
     *                   Data as arrays will be completely replaced. Make sure to include all data in the request,
     *                   even if it is not being updated.
     */
    @KafkaListener(topics = "profile-update", groupId = "databases-service",
            containerFactory = "usersQueryParamsListener")
    public void updateProfile(UsersQueryParams userSignup) {
        log.info("Received updateProfile request: {}", userSignup.toString());
        userProfileService.updateProfile(userSignup);
    }

    @KafkaListener(topics = "recommendation-user", groupId = "databases-service",
            containerFactory = "usersQueryParamsListener")
    @SendTo
    public KafkaResponse<List<UsersQueryParams>> getRecommendations(UsersQueryParams userSignup) {
        KafkaResponse.KafkaResponseBuilder<List<UsersQueryParams>> response = KafkaResponse.builder();
        log.info("Received getRecommendations request: {}", userSignup.toString());
        List<UsersQueryParams> data = userProfileService.getRecommendedUsers(userSignup);
        if (data == null) {
            log.error("No recommended users found for user: {}", userSignup);
            return response.status(404).message("No results found for user: " + userSignup).build();
        }
        return response.payload(data).status(200).message("OK").build();
    }

    /**
     * Listens for messages on the "audio-profile" topic and processes the audio profile data.
     *
     * @param audioData The audio profile data to process.
     */
    @KafkaListener(topics = "audio-profile", groupId = "databases-service",
            containerFactory = "musicalWorkQueryParamsListener")
    public void listenAudioProfile(MusicalWorkQueryParams audioData) {
        if (audioData == null) {
            log.error("Received null audio profile request");
            return;
        }
        log.info("Received audio profile request: {}", audioData);
        userProfileService.addAudioData(audioData);
    }
}
