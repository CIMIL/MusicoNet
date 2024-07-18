package musico.services.databases.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Component
@AllArgsConstructor
public class DataRetriever {

    private final RepositoryConnection connection;

    private void testConnection() {
        String query = Queries.SELECT().where(GraphPatterns.tp(
                SparqlBuilder.var("s"),
                SparqlBuilder.var("p"),
                SparqlBuilder.var("o")
        )).limit(1).getQueryString();
        TupleQuery tupleQuery = connection.prepareTupleQuery(query);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                log.debug("Binding set: {}", bindingSet);
            }
        } catch (Exception e) {
            log.error("Testing connection failed: {}", e.getMessage());
        }
    }

    public void createAndExecuteInsertQuery(List<TriplePattern> triples) {
        String query = Queries.INSERT_DATA().insertData(triples.toArray(new TriplePattern[0])).getQueryString();
        log.debug("Insert Query: {}", query);
        executeInsertQuery(query);
    }

    public List<BindingSet> createAndExecuteSelectQuery(GraphPatternNotTriples graphPattern, String... fields) {
        Variable[] vars = Arrays.stream(fields).map(SparqlBuilder::var).toArray(Variable[]::new);
        String query = Queries.SELECT(vars)
                .where(graphPattern).getQueryString();
        log.debug("Query: {}", query);
        return executeQuery(query);
    }

    private void executeInsertQuery(String query) {
        try {
            connection.begin();
            connection.prepareUpdate(query).execute();
            connection.commit();
        } catch (Exception e) {
            log.error("Error executing insert query: {}", e.getMessage());
        }
    }

    private List<BindingSet> executeQuery(String query) {
        ArrayList<BindingSet> res = new ArrayList<>();
        TupleQuery tupleQuery = connection.prepareTupleQuery(query);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                if (bindingSet.isEmpty()) {
                    continue;
                }
                res.add(bindingSet);
            }
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
        }
        log.debug("Results: {}", res);
        return res.isEmpty() ? null : res;
    }
}
