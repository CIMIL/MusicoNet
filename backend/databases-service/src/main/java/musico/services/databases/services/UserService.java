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

    /**
     * Retrieves a list of user profiles based on the provided search parameters.
     *
     * @param username the username to search for, can be null or empty
     * @param minAge   the minimum age of the users to search for
     * @param maxAge   the maximum age of the users to search for
     * @param gender   the gender of the users to search for (currently unused)
     * @return a list of Users that match the search criteria
     */
    public List<Users> getUsersOnSql(String username, Integer minAge, Integer maxAge, String gender) {
        LocalDate minBirthdate = LocalDate.now().minusYears(maxAge);
        LocalDate maxBirthdate = LocalDate.now().minusYears(minAge);

        if (username == null || username.isEmpty()) {
            return userRepository.findAllByBirthdateBetween(minBirthdate, maxBirthdate);
        } else {
            return userRepository.findAllByBirthdateBetweenAndUsernameLike(minBirthdate, maxBirthdate, username);
        }
    }
}