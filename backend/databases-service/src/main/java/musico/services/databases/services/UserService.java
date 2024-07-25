package musico.services.databases.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.models.Users;
import musico.services.databases.models.kafka.UsersQueryParams;
import musico.services.databases.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;

    public Users getUserProfile(String userId) {
        return userRepository.findByUserId(userId);
    }

    public List<Users> getUsersProfileByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void createUserProfile(UsersQueryParams user) {
        Users userToSave = Users.builder()
                .userId(user.userId())
                .username(user.username())
                .firstName(user.firstName())
                .lastName(user.surname())
                .birthdate(user.birthdate())
                .description(user.description())
                .profilePicturePath(user.profilePicturePath())
                .spotify(user.spotify())
                .youtube(user.youtube())
                .soundcloud(user.soundcloud())
                .appleMusic(user.appleMusic())
                .tidal(user.tidal())
                .amazonMusic(user.amazonMusic())
                .build();
        userRepository.save(userToSave);
    }

    public List<Users> getUserProfileParams(Integer minAge, Integer maxAge, String gender) {
        // Transform age in birthdate
        LocalDate minBirthdate = LocalDate.now().minusYears(maxAge);
        LocalDate maxBirthdate = LocalDate.now().minusYears(minAge);
        return userRepository.findAllByBirthdateBetween(minBirthdate, maxBirthdate);
    }
}