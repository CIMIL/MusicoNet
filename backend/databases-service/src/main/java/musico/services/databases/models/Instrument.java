package musico.services.databases.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import musico.services.databases.config.OntEntity;
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
@AllArgsConstructor
@Table(name = "instruments")
public class Instrument implements OntEntity {
    @Id
    @Column(name = "instrument_id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @Column(name = "instrument_name", length = 100)
    private String instrumentName;

    @Size(max = 50)
    @Column(name = "manufacturer", length = 50)
    private String manufacturer;

    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    public Instrument() {
    }

    @Override
    public IRI getIRI() {
        Namespace musinco = OntologyModel.getNamespace("");
        assert musinco != null;
        return Values.iri(musinco.getName() + "Instruments/" + id);
    }

    @Override
    public Variable getVar() {
        return SparqlBuilder.var("instrument");
    }
}