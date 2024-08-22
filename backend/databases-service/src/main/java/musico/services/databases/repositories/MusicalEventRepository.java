package musico.services.databases.repositories;

import musico.services.databases.models.MusicalEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MusicalEventRepository extends JpaRepository<MusicalEvent, String> {
     List<MusicalEvent> findAllByName(String name);
     MusicalEvent deleteByEventId(String eventId);
}