package musico.services.databases.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import musico.services.databases.config.OntEntity;
import musico.services.databases.config.OntEntityField;
import musico.services.databases.config.OntologyModel;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "musical_event")
public class MusicalEvent implements OntEntity {
    @Id
    @Size(max = 10)
    @Column(name = "event_id", nullable = false, length = 10)
    private String eventId;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    @OntEntityField(type = OntEntityField.DataType.DATA, pred = "schema:name")
    private String name;

    @Override
    public IRI getIRI() {
        Namespace musinco = OntologyModel.getNamespace("");
        assert musinco != null;
        return Values.iri(musinco.getName() + "MusicalEvent/" + eventId);
    }

    @Override
    public Variable getVar() {
        return SparqlBuilder.var("musicalEvent");
    }
    public static String getClassIRI() {
        Namespace musinco = OntologyModel.getNamespace("musicoo");
        assert musinco != null;
        return musinco.getName() + "MusicalEvent";
    }
}