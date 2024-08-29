package musico.services.databases.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.models.Genre;
import musico.services.databases.models.Instrument;
import musico.services.databases.models.kafka.KafkaResponse;
import musico.services.databases.repositories.GenreRepository;
import musico.services.databases.repositories.InstrumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataListener {

    private final GenreRepository genreRepository;
    private final InstrumentRepository instrumentRepository;

    @KafkaListener(topics = "data-genres-all", groupId = "databases-service",
         containerFactory = "genreContainerFactory")
    @SendTo
    public KafkaResponse<List<String>> getAllGenres(Integer offset) {
        KafkaResponse.KafkaResponseBuilder<List<String>> response = KafkaResponse.builder();
        Page<Genre> data = genreRepository.findAllBy(Pageable.ofSize(10).withPage(offset));
        if (data.isEmpty()) {
            log.error("No genres found");
            return response.status(404).message("No genres found").build();
        }
        response.payload(data.getContent().stream().map(Genre::getGenreName).toList());
        log.info("Response: {}", response);
        return response.status(200).message("OK").build();
    }

    @KafkaListener(topics = "data-instruments-all", groupId = "databases-service",
            containerFactory = "genreContainerFactory")
    @SendTo
    public KafkaResponse<List<String>> getAllInstruments(Integer offset) {
        KafkaResponse.KafkaResponseBuilder<List<String>> response = KafkaResponse.builder();
        Page<Instrument> data = instrumentRepository.findAllBy(Pageable.ofSize(10).withPage(offset));
        if (data.isEmpty()) {
            log.error("No genres found");
            return response.status(404).message("No genres found").build();
        }
        response.payload(data.getContent().stream().map(Instrument::getInstrumentName).toList());
        log.info("Response: {}", response);
        return response.status(200).message("OK").build();
    }


}
