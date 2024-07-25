package musico.services.databases.repositories;

import musico.services.databases.models.Users;
import musico.services.databases.models.kafka.UsersQueryParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface UserRepository extends JpaRepository<Users, String> {
    Users findFirstByUsername(String username);
    List<Users> findByUsername(String username);
    Users findByUserId(String userId);
    Users findFirstByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    List<Users> findAllByBirthdateBetween(LocalDate minBirthdate, LocalDate maxBirthdate);
}
