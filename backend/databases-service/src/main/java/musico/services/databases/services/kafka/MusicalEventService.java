package musico.services.databases.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.config.OntologyModel;
import musico.services.databases.models.MusicalEvent;
import musico.services.databases.models.Users;
import musico.services.databases.models.kafka.MusicalEventDTO;
import musico.services.databases.repositories.MusicalEventRepository;
import musico.services.databases.utils.DataRetriever;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicalEventService {

    private final DataRetriever dataRetriever;
    private final MusicalEventRepository musicalEventRepository;

    public List<MusicalEventDTO> getRecommendedEvents(String userId) {
        Users user = Users.builder().userId(userId).build();
        GraphPatternNotTriples query = GraphPatterns.and(GraphPatterns.tp(user.getIRI(),
                Values.iri(Objects.requireNonNull(OntologyModel.getNamespace("musicoo")).getName() + "gets_recommended_events"),
                SparqlBuilder.var("musicalEvent")));
        List<BindingSet> events = dataRetriever.createAndExecuteSelectQuery(query);
        List<MusicalEventDTO> results = new ArrayList<>();
        for (BindingSet event : events) {
            String iris = event.getValue("musicalEvent").stringValue();
            String id = iris.substring(iris.lastIndexOf("/") + 1);
            MusicalEventDTO musicalEventDTO = getMusicalEvent(id);
            if (musicalEventDTO != null) results.add(musicalEventDTO);
        }
        return results;
    }

    private MusicalEventDTO getMusicalEvent(String id) {
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

    public void saveEvent(MusicalEventDTO event) {
        MusicalEvent musicalEvent = MusicalEvent.builder()
                .eventId(event.eventId())
                .name(event.name())
                .build();
        log.info("Saving event: {}", musicalEvent.toString());
//        musicalEventRepository.save(musicalEvent);
        List<TriplePattern> insertQuery = new ArrayList<>();
        insertQuery.add(
                GraphPatterns.tp(
                        musicalEvent.getIRI(),
                        RDF.TYPE,
                        Values.iri(MusicalEvent.getClassIRI())
                ));
        insertQuery.addAll(musicalEvent.buildInsertQueryGraphPattern(musicalEvent));
        dataRetriever.createAndExecuteInsertQuery(insertQuery);
    }

    public void deleteEvent(String eventId) {
        // Delete the event from the SQL database
        MusicalEvent event = musicalEventRepository.deleteByEventId(eventId);
        // Delete the event from the RDF database
        dataRetriever.executeQuery(
                GraphPatterns.and(
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
                .eventId(event.eventId() != null ? event.eventId() : null)
                .name(event.name())
                .build();
    }

    public List<MusicalEventDTO> getEvents(MusicalEventDTO event) {
        List<MusicalEventDTO> results = new ArrayList<>();
        // Get the event from the SQL database
        // Now only by name
        // TODO: Implement by other fields
        List<MusicalEvent> eventsSql = musicalEventRepository.findAllByName(event.name());
        // Query the RDF database
        List<BindingSet> eventGraphData = dataRetriever.createAndExecuteSelectQuery(
                getOntEntity(event).buildGenericQueryGraphPattern(getOntEntity(event))
        );
        // Filter the SQL results by the RDF results
        eventsSql = eventsSql.stream().filter(
                e -> eventGraphData.stream().anyMatch(
                        row -> row.getValue("musicalEvent").stringValue().endsWith(e.getEventId())
                )
        ).toList();
        for (MusicalEvent e : eventsSql) {
            results.add(combineWithGraphData(e));
        }
        return results;
    }
}
