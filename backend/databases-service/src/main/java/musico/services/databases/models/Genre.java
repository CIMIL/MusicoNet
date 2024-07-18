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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "genre")
public class Genre implements OntEntity {
    @Id
    @Column(name = "genre_id", nullable = false)
    private String id;

    @Size(max = 50)
    @NotNull
    @Column(name = "genre_name", nullable = false, length = 50)
    @OntEntityField(pred = "schema:name", type = OntEntityField.DataType.OBJECT)
    private String genreName;

    @Override
    public IRI getIRI() {
        String musinco = OntologyModel.getNamespaceString("");
        return Values.iri(musinco + "Genre/" + id);
    }

    @Override
    public Variable getVar() {
        return SparqlBuilder.var("genre");
    }

    public String getClassIRI() {
        String musinco = OntologyModel.getNamespaceString("mo");
        return musinco + "Genre";
    }

    //TODO [JPA Buddy] generate columns from DB
}