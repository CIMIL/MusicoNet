package musico.services.databases.repositories;

import musico.services.databases.models.UserRole;
import musico.services.databases.models.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

}