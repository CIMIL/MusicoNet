package musico.services.databases.models;

import jakarta.persistence.*;
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

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class Users implements OntEntity {

    @Id
    @Size(max = 50)
    @Column(name = "userId", nullable = false, length = 50)
    private String userId;

    @Size(max = 50)
    @Column(name = "username", length = 50)
    private String username;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Size(max = 16)
    @Column(name = "codice_fiscale", length = 16)
    private String codiceFiscale;

    @OntEntityField(type = OntEntityField.DataType.OBJECT, pred = "foaf:based_near")
    @Column(name = "based_near")
    private Integer basedNear;

    @Size(max = 100)
    @Column(name = "professional_level", length = 100)
    private String professionalLevel;

    @Size(max = 100)
    @Column(name = "expertise_level", length = 100)
    private String expertiseLevel;

    @Size(max = 100)
    @Column(name = "`multi-instrumentalism_level`", length = 100)
    private String multiInstrumentalismLevel;

    @Size(max = 100)
    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "createdAt")
    private Instant createdAt;

    @Column(name = "updatedAt")
    private Instant updatedAt;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Lob
    @Column(name = "description")
    private String description;

    @Size(max = 100)
    @Column(name = "profile_picture_path", length = 100)
    private String profilePicturePath;

    @Size(max = 100)
    @Column(name = "soundcloud", length = 100)
    private String soundcloud;

    @Size(max = 100)
    @Column(name = "youtube", length = 100)
    private String youtube;

    @Size(max = 100)
    @Column(name = "spotify", length = 100)
    private String spotify;

    @Size(max = 100)
    @Column(name = "apple_music", length = 100)
    private String appleMusic;

    @Size(max = 100)
    @Column(name = "tidal", length = 100)
    private String tidal;

    @Size(max = 100)
    @Column(name = "amazon_music", length = 100)
    private String amazonMusic;

    //    @Id
//    @Column(name = "userId", nullable = false)
//    private String userId;
//
////    @MapsId
////    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
////    @JoinColumn(name = "userId", nullable = false)
////    private Artist artist;
//
//    @Column(name = "first_name", length = 50)
////    @OntEntityField(type = OntEntityField.DataType.DATA, pred = "foaf:firstName")
//    private String firstName;
//
//    @Column(name = "last_name", length = 50)
//    @OntEntityField(type = OntEntityField.DataType.DATA, pred = "foaf:surname")
//    private String surname;
//
//    @Column(name = "codice_fiscale", length = 16)
//    private String codiceFiscale;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "based_near")
//    private Position based_near;
//
//    @Column(name = "professional_level", length = 100)
//    private String professionalLevel;
//
//    @Column(name = "expertise_level", length = 100)
//    private String expertiseLevel;
//
//    @Column(name = "`multi-instrumentalism_level`", length = 100)
//    private String multiInstrumentalismLevel;
//
//    @Column(name = "createdAt")
//    private Instant createdAt;
//
//    @Column(name = "updatedAt")
//    private Instant updatedAt;
//
//   /* @OneToMany(mappedBy = "receiver")
//    private Set<ChatMessage> chatMessages;*/
//
//    @OneToMany(mappedBy = "user2")
//    private Set<Friendship> friendships;
//
//  /*  @ManyToMany
//    @JoinTable(name = "group_user",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "group_id"))
//    private Set<MusicianGroup> musicianGroups;
//*/
//    //    @ManyToMany
////    @JoinTable(name = "user_has_instrument",
////            joinColumns = @JoinColumn(name = "user_id"),
////            inverseJoinColumns = @JoinColumn(name = "instrument_id"))
    @Transient
    @OntEntityField(type = OntEntityField.DataType.OBJECT, pred = "musicoo:plays_instrument")
    private Set<Instrument> instruments;
//
//    //    @ManyToMany
////    @JoinTable(name = "user_plays_genre",
////            joinColumns = @JoinColumn(name = "user_id"),
////            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @Transient
    @OntEntityField(type = OntEntityField.DataType.OBJECT, pred = "musicoo:plays_genre")
    private Set<Genre> genres;
//
//  /*  @OneToMany(mappedBy = "user")
//    private Set<UserSetting> userSettings;
//*/
//    @Column(name = "username", nullable = false, length = 50)
//    private String username;
//
//    @Column(name = "email", length = 100)
//    private String email;
//
//    @Column(name = "password", length = 100)
//    private String password;
//
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "users", fetch = FetchType.EAGER)
//    private Set<UserRole> userRoles;
//
//    @Column(name = "birthdate")
//    private LocalDate birthdate;
//
//    @Lob
//    @Column(name = "description")
//    private String description;
//
//    @Size(max = 100)
//    @Column(name = "profile_picture_path", length = 100)
//    private String profilePicturePath;
//
//    @Size(max = 100)
//    @Column(name = "soundcloud", length = 100)
//    private String soundcloud;
//
//    @Size(max = 100)
//    @Column(name = "youtube", length = 100)
//    private String youtube;
//
//    @Size(max = 100)
//    @Column(name = "spotify", length = 100)
//    private String spotify;
//
//    @Size(max = 100)
//    @Column(name = "apple_music", length = 100)
//    private String appleMusic;
//
//    @Size(max = 100)
//    @Column(name = "tidal", length = 100)
//    private String tidal;
//
//    @Size(max = 100)
//    @Column(name = "amazon_music", length = 100)
//    private String amazonMusic;
//
//    @Column(name = "based_near")
//    private Integer basedNear;
//
    @Override
    public IRI getIRI() {
        Namespace musinco = OntologyModel.getNamespace("");
        assert musinco != null;
        return Values.iri(musinco.getName() + "Users/" + userId);
    }

    public static Map<String, String> getFieldsPredicate() {
        Map<String, String> fields = new HashMap<>();
        for (Field field : Users.class.getDeclaredFields()) {
            OntEntityField annotation = field.getAnnotation(OntEntityField.class);
            if (annotation != null) {
                fields.put(field.getName().toLowerCase(), OntologyModel.getPredicate(annotation.pred()));
            }
        }
        return fields;
    }

    @Override
    public Variable getVar() {
        return SparqlBuilder.var("user");
    }

    public static String getClassIRI() {
        Namespace musinco = OntologyModel.getNamespace("musico");
        assert musinco != null;
        return musinco.getName() + "HumanMusician";
    }
}
