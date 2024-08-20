package musico.services.databases.services.kafka;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.config.OntEntity;
import musico.services.databases.config.OntologyModel;
import musico.services.databases.models.Genre;
import musico.services.databases.models.MWork;
import musico.services.databases.models.Users;
import musico.services.databases.models.kafka.MusicalWorkQueryParams;
import musico.services.databases.repositories.GenreRepository;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class MusicalWorkQueryParamsService {
    private final GenreRepository genreRepository;

    public GraphPatternNotTriples buildQueryGraphPattern(MusicalWorkQueryParams params) {
        GraphPatternNotTriples res = getQueryPreface();
        res.and(getQueryBody(params));
        return res;
    }

    private OntEntity getOntEntity(MusicalWorkQueryParams queryParams) {
        // TODO: Change in order to use more Genres, only one is used for now
        Genre genre = genreRepository.findByGenreNameLike(queryParams.genres()[0]);
        log.info("Genre: {}", genre.toString());
        return MWork.builder()
                .genre(genre)
                .mood(queryParams.mood())
                .bpm(queryParams.bpm())
                .key(queryParams.tonality().key() + queryParams.tonality().scale())
                .build();
    }

    private GraphPatternNotTriples getQueryPreface() {
        Variable musicalWork = SparqlBuilder.var("musicalWork");
        Variable user = SparqlBuilder.var("user");
        Variable musParticipation = SparqlBuilder.var("musParticipation");
        return GraphPatterns.and(user.has(
                        p -> p.pred(Values.iri(Objects.requireNonNull(OntologyModel.getNamespace("musicoo")).getName() + "in_participation")),
                        musParticipation),
                musParticipation.has(
                        p -> p.pred(Values.iri(Objects.requireNonNull(OntologyModel.getNamespace("musicoo")).getName() + "played_musical_work")),
                        musicalWork)
        );
    }

    public GraphPatternNotTriples getQueryBody(MusicalWorkQueryParams params) {
        MWork dataToQuery = (MWork) getOntEntity(params);
        log.info(dataToQuery.toString());
        return dataToQuery.buildGenericQueryGraphPattern(dataToQuery);
    }

    public List<TriplePattern> getSaveAudioProfileQuery(MusicalWorkQueryParams params, Users user) {
        MWork dataToQuery = (MWork) getOntEntity(params);
        dataToQuery.setId(String.valueOf(UUID.randomUUID()));

        List<TriplePattern> query = new ArrayList<>(
                Collections.singletonList(
                        GraphPatterns.tp(Values.iri(user.getIRI()+"/audio_profile"),
                                Values.iri(Objects.requireNonNull(OntologyModel.getNamespace("musicoo")).getName() + "played_musical_work"),
                                dataToQuery.getIRI())
                )
        );
        query.addAll(dataToQuery.buildInsertQueryGraphPattern(dataToQuery));
        for (TriplePattern triplePattern : query) {
            log.info(triplePattern.getQueryString());
        }
        return query;
    }

    public String getCountAudioProfileQueryString(Users user) {
        Expression count = Expressions.count(SparqlBuilder.var("musicalWork"));
        Variable countAgg = SparqlBuilder.var("count");
        return Queries.SELECT(count.as(countAgg))
                .where(GraphPatterns.tp(
                        Values.iri(user.getIRI()+"/audio_profile"),
                        Values.iri(Objects.requireNonNull(OntologyModel.getNamespace("musicoo")).getName() + "played_musical_work"),
                        SparqlBuilder.var("musicalWork")
                ))
                .getQueryString();

    }

}
