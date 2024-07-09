package musico.services.databases.config;

import org.slf4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class OntologyModelConfig implements ApplicationRunner {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(OntologyModelConfig.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initializing ontology model ...");
        OntologyModel model = new OntologyModel();
        model.init();
    }
}
