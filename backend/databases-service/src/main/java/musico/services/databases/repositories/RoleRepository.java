package musico.services.databases.repositories;


import musico.services.databases.config.ERole;
import musico.services.databases.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Roles, Long> {
    Roles findFirstByName(ERole name);

}
