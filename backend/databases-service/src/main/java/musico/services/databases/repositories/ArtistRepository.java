package musico.services.databases.repositories;

import musico.services.databases.models.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Boolean existsByArtistName(String artistName);
}
