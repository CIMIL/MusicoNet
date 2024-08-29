package musico.services.databases.repositories;

import musico.services.databases.models.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

    Genre findByGenreNameLike(String name);
    // Get all data from the genre table
    Page<Genre> findAllBy(Pageable pageable);
}