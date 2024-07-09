package musico.services.databases.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import musico.services.databases.config.OntologyModel;
import org.eclipse.rdf4j.model.Namespace;

import java.time.Instant;

@Getter
@Entity
@Setter
@Builder
@AllArgsConstructor
@Table(name = "artist")
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_id")
    private Integer artistId;

    @Column(name = "createdAt")
    private Instant createdAt;

    @Column(name = "updatedAt")
    private Instant updatedAt;

    @Column(name = "artist_name", length = 100)
    private String artistName;

    public Artist() {
    }

    public String getArtistIRI() {
        Namespace musinco = OntologyModel.getNamespace("");
        assert musinco != null;
        return musinco.getName() +
                "Artist/" +
                artistId;
    }
}
