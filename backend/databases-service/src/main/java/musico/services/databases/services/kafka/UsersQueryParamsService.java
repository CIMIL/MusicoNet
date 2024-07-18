package musico.services.databases.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.config.OntEntity;
import musico.services.databases.models.Genre;
import musico.services.databases.models.Instrument;
import musico.services.databases.models.Users;
import musico.services.databases.models.kafka.UsersQueryParams.UsersQueryParamsBuilder;
import musico.services.databases.models.kafka.UsersQueryParams;
import musico.services.databases.repositories.GenreRepository;
import musico.services.databases.repositories.InstrumentRepository;
import org.eclipse.rdf4j.model.Triple;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersQueryParamsService {
    private final GenreRepository genreRepository;
    private final InstrumentRepository instrumentRepository;

    public Users getOntEntity(UsersQueryParams queryParams) {
        Set<Genre> genres = new HashSet<>();
        Set<Instrument> instruments = new HashSet<>();
        if(queryParams.genres() != null)
            genres = Arrays.stream(queryParams.genres()).map(genreRepository::findByGenreNameLike).filter(Objects::nonNull).collect(Collectors.toSet());
        if(queryParams.instruments() != null)
            instruments = Arrays.stream(queryParams.instruments()).map(instrumentRepository::findByInstrumentNameLike).filter(Objects::nonNull).collect(Collectors.toSet());
        return Users.builder()
                .userId(queryParams.userId())
                .genres(genres)
                .firstName(queryParams.firstName())
                .lastName(queryParams.surname())
                .instruments(instruments)
                .build();
    }

    public GraphPatternNotTriples buildQueryGraphPattern(UsersQueryParams params) {
        GraphPatternNotTriples res = getQueryPreface(params);
        res.and(getQueryBody(params));
        return res;
    }

    public List<TriplePattern> buildInsertQueryGraphPattern(UsersQueryParams params) {
        OntEntity dataToQuery = getOntEntity(params);
        List<TriplePattern> res = new ArrayList<>(
                Collections.singletonList(
                        GraphPatterns.tp(dataToQuery.getIRI(), RDF.TYPE, Values.iri(Users.getClassIRI()))
                )
        );
        res.addAll(dataToQuery.buildInsertQueryGraphPattern(dataToQuery));
        return res;
    }

    private GraphPatternNotTriples getQueryPreface(UsersQueryParams params) {
        if (params.userId() == null) {
            Variable user = SparqlBuilder.var("user");
            return GraphPatterns.and(GraphPatterns.tp(user, RDF.TYPE, Values.iri(Users.getClassIRI())));
        }
        return GraphPatterns.and();
//        else {
//            Users dataToQuery = getOntEntity(params);
//            return GraphPatterns.and(GraphPatterns.tp(dataToQuery.getIRI(), RDF.TYPE, Values.iri(Users.getClassIRI())));
//        }
    }

    private GraphPatternNotTriples getQueryBody(UsersQueryParams params) {
        Users dataToQuery = getOntEntity(params);
        return dataToQuery.buildGenericQueryGraphPattern(dataToQuery);
    }

    public UsersQueryParamsBuilder getResponseMessageFromQueryResults(List<BindingSet> params) {
        UsersQueryParamsBuilder response = UsersQueryParams.builder();
        if (params == null || params.isEmpty() ) {
            log.info("Response is empty");
            return response;
        }
        for (BindingSet row : params) {
            log.info("Row: {}", row.toString());
            for (Field field : response.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    if (row.hasBinding(field.getName())) {
                        // Check if the field is a list
                        if (field.getType().isArray()) {
                            String[] values = field.get(response) != null ? (String[]) field.get(response) : new String[0];
                            Set<String> list = new HashSet<>(Arrays.asList(values));
                            list.add(row.getValue(field.getName() + "_name").stringValue());
                            field.set(response, list.toArray(new String[0]));
                            continue;
                        }
                        field.set(response, row.getValue(field.getName()+"_name").stringValue());
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error: {}", e.getMessage());
                }

            }
        }
        return response;
    }

    public GraphPatternNotTriples checkUserExists(UsersQueryParams params) {
        Users dataToQuery = getOntEntity(params);
        return GraphPatterns.and(GraphPatterns.tp(dataToQuery.getIRI(), RDF.TYPE, SparqlBuilder.var("type")));
    }
}
