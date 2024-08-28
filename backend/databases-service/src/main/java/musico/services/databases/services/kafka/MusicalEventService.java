package musico.services.databases.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.config.OntologyModel;
import musico.services.databases.models.MusicalEvent;
import musico.services.databases.models.Users;
import musico.services.databases.models.kafka.KafkaResponse;
import musico.services.databases.models.kafka.MusicalEventDTO;
import musico.services.databases.repositories.MusicalEventRepository;
import musico.services.databases.repositories.UserRepository;
import musico.services.databases.utils.DataRetriever;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicalEventService {

    private final DataRetriever dataRetriever;
    private final MusicalEventRepository musicalEventRepository;
    private final UserRepository userRepository;

    public KafkaResponse<List<MusicalEventDTO>> getRecommendedEvents(String userId) {
        KafkaResponse.KafkaResponseBuilder<List<MusicalEventDTO>> response = KafkaResponse.builder();
        Users user = userRepository.findByUserId(userId);
        if (user == null) return response.status(404).message("User not found in sql").build();
        GraphPatternNotTriples query = GraphPatterns.and(GraphPatterns.tp(user.getIRI(),
                Values.iri(Objects.requireNonNull(OntologyModel.getNamespace("musicoo")).getName() +
                        "gets_recommended_events"),
                SparqlBuilder.var("musicalEvent")));
        List<BindingSet> events = dataRetriever.createAndExecuteSelectQuery(query);
        List<MusicalEventDTO> results = new ArrayList<>();
        if (events == null || events.isEmpty()) return response.status(404).message("No events found").build();
        for (BindingSet event : events) {
            String iris = event.getValue("musicalEvent").stringValue();
            String id = iris.substring(iris.lastIndexOf("/") + 1);
            MusicalEventDTO musicalEventDTO = getMusicalEventDTO(id);
            if (musicalEventDTO != null) results.add(musicalEventDTO);
        }
        return response.status(200).message("OK").payload(results).build();
    }

    private MusicalEventDTO getMusicalEventDTO(String id) {
        // Get the event from the SQL database
        MusicalEvent event = musicalEventRepository.findById(id).orElse(null);
        if (event == null) return null;
        return combineWithGraphData(event);
    }

    private MusicalEventDTO combineWithGraphData(MusicalEvent event) {
        // Get the event from the RDF database
        List<BindingSet> eventGraphData = dataRetriever.createAndExecuteSelectQuery(
                event.buildGenericQueryGraphPattern(event));
        MusicalEventDTO.MusicalEventDTOBuilder builder = processEventGraphResults(eventGraphData);
        // Combine the two results
        // TODO: Add other fields
        builder.eventId(event.getEventId());
        return builder.build();
    }

    private MusicalEventDTO.MusicalEventDTOBuilder processEventGraphResults(List<BindingSet> params) {
        MusicalEventDTO.MusicalEventDTOBuilder response = MusicalEventDTO.builder();
        if (params == null || params.isEmpty()) {
            log.info("Response is empty");
            return response;
        }
        for (BindingSet row : params) {
            for (Field field : response.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    if (row.hasBinding(field.getName())) {
                        // Check if the field is a list
                        if (field.getType().isArray()) {
                            String[] values = field.get(response) != null ?
                                    (String[]) field.get(response) : new String[0];
                            Set<String> list = new HashSet<>(Arrays.asList(values));
                            list.add(row.getValue(field.getName() + "_name").stringValue());
                            field.set(response, list.toArray(new String[0]));
                            continue;
                        }
                        field.set(response, row.getValue(field.getName() + "_name").stringValue());
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error: {}", e.getMessage());
                }
            }
        }
        return response;
    }

    public KafkaResponse<List<MusicalEventDTO>> saveEvent(MusicalEventDTO event) {
        MusicalEvent musicalEvent = MusicalEvent.builder()
                .eventId(UUID.randomUUID().toString().substring(0, 8))
                .name(event.name())
                .build();
        log.info("Saving event: {}", musicalEvent.toString());
        try {
            musicalEventRepository.save(musicalEvent);
        } catch (Exception e) {
            log.error("Error saving event: {}", e.getMessage());
            return KafkaResponse.<List<MusicalEventDTO>>builder()
                    .status(500)
                    .message("Error saving event")
                    .build();
        }
        List<TriplePattern> insertQuery = new ArrayList<>();
        insertQuery.add(
                GraphPatterns.tp(
                        musicalEvent.getIRI(),
                        RDF.TYPE,
                        Values.iri(MusicalEvent.getClassIRI())
                ));
        insertQuery.addAll(musicalEvent.buildInsertQueryGraphPattern(musicalEvent));
        dataRetriever.createAndExecuteInsertQuery(insertQuery);
        return KafkaResponse.<List<MusicalEventDTO>>builder()
                .status(200)
                .message("OK")
                .payload(List.of(combineWithGraphData(musicalEvent)))
                .build();
    }

    public void deleteEvent(String eventId) {
        // Delete the event from the SQL database
        MusicalEvent event = musicalEventRepository.deleteByEventId(eventId);
        // Delete the event from the RDF database
        dataRetriever.executeQuery(
                Queries.DELETE(
                        GraphPatterns.tp(
                                event.getIRI(),
                                SparqlBuilder.var("p"),
                                SparqlBuilder.var("o")
                        ),
                        GraphPatterns.tp(
                                SparqlBuilder.var("s"),
                                SparqlBuilder.var("p"),
                                event.getIRI()
                        )
                ).where(
                        GraphPatterns.tp(
                                event.getIRI(),
                                SparqlBuilder.var("p"),
                                SparqlBuilder.var("o")
                        ),
                        GraphPatterns.tp(
                                SparqlBuilder.var("s"),
                                SparqlBuilder.var("p"),
                                event.getIRI()
                        )
                ).getQueryString()
        );
    }

    public void updateEvent(MusicalEventDTO event) {
    }

    private MusicalEvent getOntEntity(MusicalEventDTO event) {
        return MusicalEvent.builder()
                .eventId(event.eventId())
                .name(event.name())
                .build();
    }

    public KafkaResponse<List<MusicalEventDTO>> searchEvents(MusicalEventDTO event) {
        List<MusicalEventDTO> results = new ArrayList<>();
        // Get the event from the SQL database
        // Now only by name
        Example<MusicalEvent> example = Example.of(getOntEntity(event));
        List<MusicalEvent> eventsSql = musicalEventRepository.findBy(example,
                q -> q.stream().toList());
        eventsSql.forEach(e -> log.debug("Event: {}", e.toString()));
        if (eventsSql.isEmpty()) return KafkaResponse.<List<MusicalEventDTO>>builder()
                .status(404)
                .message("No events found in SQL")
                .build();
        // Query the RDF database
        List<BindingSet> eventGraphData = dataRetriever.createAndExecuteSelectQuery(
                getOntEntity(event).buildGenericQueryGraphPattern(getOntEntity(event))
        );
        // Filter the SQL results by the RDF results
        if (eventGraphData == null) return KafkaResponse.<List<MusicalEventDTO>>builder()
                .status(404)
                .message("No events found in GraphDB")
                .build();
        eventsSql = eventsSql.stream().filter(
                e -> eventGraphData.stream().anyMatch(
                        row -> row.getValue("musicalEvent").stringValue().endsWith(e.getEventId())
                )
        ).toList();
        for (MusicalEvent e : eventsSql) {
            results.add(combineWithGraphData(e));
        }
        if (results.isEmpty()) return KafkaResponse.<List<MusicalEventDTO>>builder()
                .status(404)
                .message("No events found")
                .build();
        return KafkaResponse.<List<MusicalEventDTO>>builder()
                .status(200)
                .message("OK")
                .payload(results)
                .build();
    }

    public MusicalEventDTO joinEvent(MusicalEventDTO event) {
        event = getMusicalEventDTO(event.eventId());
        if (event == null) return null;
        MusicalEvent musicalEvent = getOntEntity(event);
        Users user = userRepository.findByUserId(event.userId());
        if (user == null) return null;
        List<TriplePattern> insertQuery = new ArrayList<>();
        insertQuery.add(
                GraphPatterns.tp(
                        user.getIRI(),
                        Values.iri(Objects.requireNonNull(OntologyModel.getNamespace("musicoo")).getName() +
                                "interested_in"),
                        musicalEvent.getIRI()
                ));
        dataRetriever.createAndExecuteInsertQuery(insertQuery);
        return event;
    }


}
