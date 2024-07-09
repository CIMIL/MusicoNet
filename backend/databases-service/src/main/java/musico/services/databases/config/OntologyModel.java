package musico.services.databases.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;


@Slf4j
public class OntologyModel {
    @Getter
    private static final Model model = new TreeModel();
    private static final String ONTOLOGY_DIR = "ontologies";
    private static final RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);

    public void init() {
        // Load ontology model
        // Get the folder where the ontologies are stored in the resource directory
        rdfParser.setRDFHandler(new StatementCollector(model));
        File folder = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(ONTOLOGY_DIR)).getFile());
        loadOntologiesFromDir(folder);
    }

    private void loadOntologiesFromDir(final  File folder) {
        for (final File entry : Objects.requireNonNull(folder.listFiles())) {
            log.info("Loading ontology model from: {}", entry.getName());
            if (entry.isDirectory()) {
                loadOntologiesFromDir(entry);
            } else {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(folder.getName() + "/" + entry.getName())) {
                    rdfParser.parse(in, "");
                    model.getNamespaces().clear();
                } catch (Exception e) {
                    log.error("Error loading ontology model: {}", e.getMessage());
                }
            }
        }
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("musinco5.rdf")) {
            rdfParser.parse(in, "");
        } catch (Exception e) {
            log.error("Error loading main ontology model: {}", e.getMessage());
        }
        checkPrefixes();
        checkImportedOntology();
    }

    public static String getDefaultNamespace() {
        if (model.getNamespace("").isPresent()) {
            return model.getNamespace("").get().getName();
        }
        return "http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/";
    }

    private void checkPrefixes() {
        // Check if the ontology model has been imported
        if (model.getNamespaces().isEmpty()) {
            log.error("Ontology model prefixes not imported");
            throw new RuntimeException("Ontology model prefixes not imported");
        } else {
            log.info("Ontology model prefixes imported: {}", model.getNamespaces().size());
        }
    }

    private void checkImportedOntology() {
        // Check if the ontology model has been imported
        if (model.isEmpty()) {
            log.error("Ontology model not imported");
            throw new RuntimeException("Ontology model not imported");
        } else {
            log.info("Ontology model imported {} triples", model.size());

        }
    }


    public static  Namespace getNamespace(String prefix) {
        if (model.getNamespace(prefix).isPresent()) {
            return model.getNamespace(prefix).get();
        } else {
            return null;
        }
    }

    public static String getNamespaceString(String prefix) {
        Namespace ns = getNamespace(prefix);
        if (ns != null) {
            return ns.getName();
        }
        return getDefaultNamespace();
    }

    public static String getPredicate(String property) {
        String[] parts = property.split(":");
        Namespace ns = getNamespace(parts[0]);
        if (ns != null) {
            return ns.getName() + parts[1];
        }
        return null;
    }


}
