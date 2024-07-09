package musico.services.databases.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "musical_work")
public class MusicalWork {
    @Id
    @Size(max = 10)
    @Column(name = "musical_work_id", nullable = false, length = 10)
    private String musicalWorkId;

    @Size(max = 400)
    @NotNull
    @Column(name = "title", nullable = false, length = 400)
    private String title;

    @Size(max = 20)
    @Column(name = "iswc", length = 20)
    private String iswc;

    @Column(name = "genre_id")
    private Integer genreId;

    @Size(max = 20)
    @Column(name = "release_type", length = 20)
    private String releaseType;

    @Size(max = 10)
    @Column(name = "`key`", length = 10)
    private String key;

    @Column(name = "bpm")
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

}