package musico.services.databases.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import musico.services.databases.config.OntEntity;
import musico.services.databases.config.OntEntityField;
import musico.services.databases.config.OntologyModel;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

@Getter
@Setter
@Entity
@ToString
@Builder
@Table(name = "musical_work")
@NoArgsConstructor
@AllArgsConstructor
public class MWork implements OntEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "musical_work_id", nullable = false)
    private Integer id;

    @Size(max = 400)
    @NotNull
    @Column(name = "title", nullable = false, length = 400)
    private String title;

    @Size(max = 20)
    @Column(name = "iswc", length = 20)
    private String iswc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    @OntEntityField(type = OntEntityField.DataType.OBJECT, pred = "mo:genre")
    private Genre genre;

    @Size(max = 20)
    @Column(name = "release_type", length = 20)
    private String releaseType;

    @Size(max = 10)
    @Column(name = "`key`", length = 10)
    private String key;

    @Column(name = "bpm")
    @OntEntityField(type = OntEntityField.DataType.DATA, pred = "mo:bpm")
    private Integer bpm;

    @Size(max = 10)
    @Column(name = "meter", length = 10)
    private String meter;

    @Size(max = 4)
    @Column(name = "tempo", length = 4)
    private String tempo;

    @Size(max = 100)
    @Column(name = "album", length = 100)
    private String album;

    @Size(max = 100)
    @Column(name = "artist", length = 100)
    private String artist;

    @Override
    public IRI getIRI() {
        String namespace = OntologyModel.getNamespaceString("");
        return Values.iri(namespace + "MusicalWork/" + id);
    }

    @Override
    public Variable getVar() {
        return SparqlBuilder.var("musicalWork");
    }

    public String getClassIRI() {
        String namespace = OntologyModel.getNamespaceString("mo");
        return namespace + "MusicalWork";
    }
}