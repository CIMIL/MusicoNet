package musico.services.user.repo;

import musico.services.user.models.TokenDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokensRedisRepository extends CrudRepository<TokenDto, String> {
}