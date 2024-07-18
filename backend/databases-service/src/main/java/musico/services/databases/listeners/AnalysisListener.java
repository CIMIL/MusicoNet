package musico.services.databases.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.models.Users;
import musico.services.databases.models.kafka.MusicalWorkQueryParams;
import musico.services.databases.models.kafka.UsersQueryParams;
import musico.services.databases.services.UserProfileService;
import musico.services.databases.services.kafka.MusicalWorkQueryParamsService;
import musico.services.databases.services.kafka.UsersQueryParamsService;
import musico.services.databases.utils.DataRetriever;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalysisListener {

    private final KafkaTemplate<String, UsersQueryParams> kafkaTemplate;
    private final DataRetriever dataRetriever;
    private final MusicalWorkQueryParamsService musicalWorkQueryParamsService;
    private final UserProfileService userProfileService;


    /**
     * This method is annotated with @KafkaListener, which means it is a listener for Kafka messages.
     * It listens to the topic "analysis-query_params" and belongs to the group "databases-service".
     * The containerFactory "kafkaListenerContainerFactory" is used to create the listener container.
     * The method receives parameters that will be used to query the database and send a message for
     * each user that matches the query.
     *
     * @param message The payload of the Kafka message, which is of type MusicalWorkQueryParams.
     */
    @KafkaListener(topics = "analysis-query_params", groupId = "databases-service",
            containerFactory = "musicalWorkQueryParamsListener")
    public void listen(MusicalWorkQueryParams message) {
        log.debug("Received message from {}", message);
        try {
            GraphPatternNotTriples query = musicalWorkQueryParamsService.buildQueryGraphPattern(message);
            HashSet<String> alreadySent = new HashSet<>();
            for (BindingSet row : dataRetriever.createAndExecuteSelectQuery(query)) {
                log.debug("Row: {}", row);
                if (row.getValue("user").toString().contains("ex") || row.getValue("user").toString().contains(message.requestId())) {
                    continue;
                }
                String id = row.getValue("user").stringValue().split("/")[row.getValue("user").stringValue().split("/").length - 1];
                if (alreadySent.contains(id)) {
                    continue;
                }else {
                    alreadySent.add(id);
                }
                UsersQueryParams user = UsersQueryParams.builder()
                        .requestID(message.requestId())
                        .userId(id)
                        .build();
                UsersQueryParams response = userProfileService.getUserProfile(user);
                if (response == null) {
                    continue;
                }
                kafkaTemplate.send("analysis-query_params_response", response);
            }
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            Arrays.asList(e.getStackTrace()).forEach(
                    stackTraceElement -> log.error("Error: {}", stackTraceElement.toString())
            );
        }
    }
}
