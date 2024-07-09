package musico.services.databases.repositories;

import musico.services.databases.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

    Genre findByGenreNameLike(String name);
    
}