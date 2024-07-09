package musico.services.databases.repositories;

import musico.services.databases.models.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Integer> {

        Instrument findByInstrumentNameLike(String name);
}