package musico.services.databases.config;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataRetrieverConfig {
    @Value("${graphdb.repository.url}")
    private String repositoryUrl;
    @Value("${graphdb.repository.update.url}")
    private String repositoryUpdateUrl;

    @Bean
    public RepositoryConnection connection() {
        Repository repository = new SPARQLRepository(repositoryUrl, repositoryUpdateUrl);

        repository.init();
        return repository.getConnection();
    }
}
