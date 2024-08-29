package musico.services.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import musico.services.user.models.KafkaResponse;
import musico.services.user.models.ProfilePicture;
import musico.services.user.models.UserProfileDTO;
import musico.services.user.services.StorageService;
import org.apache.catalina.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequestMapping(path = "/user/profile")
public class ProfileController {

    private final ReplyingKafkaTemplate<String, UserProfileDTO, KafkaResponse<UserProfileDTO>> replyingKafkaTemplate;
    private final ReplyingKafkaTemplate<String, UserProfileDTO, KafkaResponse<List<UserProfileDTO>>> recommendedUsersTemplate;
    private final StorageService storageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProfileController(
            ReplyingKafkaTemplate<String, UserProfileDTO, KafkaResponse<UserProfileDTO>> replyingKafkaTemplate,
            ReplyingKafkaTemplate<String, UserProfileDTO, KafkaResponse<List<UserProfileDTO>>> recommendedUsersTemplate,
            StorageService storageService) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.recommendedUsersTemplate = recommendedUsersTemplate;
        this.storageService = storageService;
    }

    @PostMapping(path = "/create")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<UserProfileDTO> createProfile(@RequestBody UserProfileDTO profileDTO, HttpServletRequest principal) {
        // Get User ID from principal
        profileDTO.setUserId(principal.getUserPrincipal().getName());
        try {
            ProducerRecord<String, UserProfileDTO> record = new ProducerRecord<>("profile-creation", profileDTO);
            record.headers().add(KafkaHeaders.REPLY_TOPIC, "profile-create-response".getBytes());
            RequestReplyFuture<String, UserProfileDTO, KafkaResponse<UserProfileDTO>> future = replyingKafkaTemplate.sendAndReceive(record);
            future.getSendFuture().get(10, TimeUnit.SECONDS);
            log.info("Sent profile: {}", record.value());
            ConsumerRecord<String, KafkaResponse<UserProfileDTO>> response = future.get(10, TimeUnit.SECONDS);
            log.info("Profile created: {}", response.value());
            return ResponseEntity.status(response.value().getStatus()).body(response.value().getPayload());
        } catch (Exception e) {
            log.error("Error while creating profile: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(path = "/update")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<UserProfileDTO> updateProfile(@RequestBody UserProfileDTO profileDTO, HttpServletRequest principal) {
        // Get User ID from principal
        profileDTO.setUserId(principal.getUserPrincipal().getName());
        try {
            ProducerRecord<String, UserProfileDTO> record = new ProducerRecord<>("profile-update", profileDTO);
            record.headers().add(KafkaHeaders.REPLY_TOPIC, "profile-update-response".getBytes());
            RequestReplyFuture<String, UserProfileDTO, KafkaResponse<UserProfileDTO>> future = replyingKafkaTemplate.sendAndReceive(record);
            future.getSendFuture().get(10, TimeUnit.SECONDS);
            log.info("Sent profile updated: {}", record.value());
            ConsumerRecord<String, KafkaResponse<UserProfileDTO>> response = future.get(10, TimeUnit.SECONDS);
            log.info("Profile updated: {}", response.value());
            return ResponseEntity.status(response.value().getStatus()).body(response.value().getPayload());
        } catch (Exception e) {
            log.error("Error while updating profile: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(path = "/get")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<UserProfileDTO> getProfile(HttpServletRequest principal) throws ExecutionException, InterruptedException, TimeoutException {
        log.info("Principal: {}", principal);
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setUserId(principal.getUserPrincipal().getName());
        ProducerRecord<String, UserProfileDTO> record = new ProducerRecord<>("profile-get", profileDTO);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "profile-get-response".getBytes()));
        RequestReplyFuture<String, UserProfileDTO, KafkaResponse<UserProfileDTO>> future = replyingKafkaTemplate.sendAndReceive(record);
        future.getSendFuture().get(10, TimeUnit.SECONDS);
        log.info("Sent: {}", record.value());
        try {
            ConsumerRecord<String, KafkaResponse<UserProfileDTO>> response = future.get(10, TimeUnit.SECONDS);
            log.info("Response: {}", response.value());
            UserProfileDTO profile = objectMapper.configure(
                    com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
            ).convertValue(response.value().getPayload(), UserProfileDTO.class);
            return ResponseEntity.status(response.value().getStatus()).body(profile);
        } catch (Exception e) {
            log.error("Error while getting profile: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(path = "/recommendation/user")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<List<UserProfileDTO>> getRecommendedUsers(HttpServletRequest principal) throws ExecutionException, InterruptedException, TimeoutException {
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setUserId(principal.getUserPrincipal().getName());
        ProducerRecord<String, UserProfileDTO> record = new ProducerRecord<>("recommendation-user", profileDTO);
        record.headers().add(KafkaHeaders.REPLY_TOPIC, "recommendation-user-response".getBytes());
        RequestReplyFuture<String, UserProfileDTO, KafkaResponse<List<UserProfileDTO>>> future = recommendedUsersTemplate.sendAndReceive(record);
        future.getSendFuture().get(10, TimeUnit.SECONDS);
        try {
            ConsumerRecord<String, KafkaResponse<List<UserProfileDTO>>> response = future.get(10, TimeUnit.SECONDS);
            log.info("Recommended users: {}", response.value());
            return ResponseEntity.status(response.value().getStatus()).body(response.value().getPayload());
        } catch (Exception e) {
            log.error("Error in receiving recommended users: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(path = "/profile-picture")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file, HttpServletRequest principal) {
        try {
            String id = storageService.storeProfilePicture(principal.getUserPrincipal().getName(), file);
            log.info("Profile picture uploaded with ID: {}", id);
        } catch (Exception e) {
            log.error("Error uploading profile picture: {}", e.getMessage());
            return "Error uploading profile picture";
        }
        // Get User ID from principal
        return "Profile picture uploaded";
    }

    @GetMapping(path = "/profile-picture")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<ByteArrayResource> getProfilePicture(HttpServletRequest principal) {
        try {
            ProfilePicture profilePicture = storageService.getProfilePicture(principal.getUserPrincipal().getName());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .headers(headers -> headers.setContentDispositionFormData("attachment", "profile.jpg"))
                    .body(new ByteArrayResource(profilePicture.getImage()));

        } catch (Exception e) {
            log.error("Error getting profile picture: {}", e.getMessage());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/audio")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public String uploadAudio(@RequestParam("file") MultipartFile file, HttpServletRequest principal) {
        try {
            String id = storageService.storeAudio(principal.getUserPrincipal().getName(), file);
            log.info("Audio uploaded with ID: {}", id);
        } catch (Exception e) {
            log.error("Error uploading audio: {}", e.getMessage());
            return "Error uploading audio";
        }
        // Get User ID from principal
        return "Audio uploaded";
    }

    @GetMapping(path = "/audio")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    public ResponseEntity<ByteArrayResource> getAudio(HttpServletRequest principal) {
        try {
            ProfilePicture profilePicture = storageService.getAudio(principal.getUserPrincipal().getName());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mp3"))
                    .headers(headers -> headers.setContentDispositionFormData("attachment", "audio.mp3"))
                    .body(new ByteArrayResource(profilePicture.getImage()));

        } catch (Exception e) {
            log.error("Error getting audio: {}", e.getMessage());
        }
        return ResponseEntity.notFound().build();
    }


}
