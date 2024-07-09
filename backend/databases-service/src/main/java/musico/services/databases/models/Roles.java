package musico.services.databases.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import musico.services.databases.config.ERole;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "roles")
public class Roles {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToMany(mappedBy = "roles")
    private Set<UserRole> userRoles = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "updatedAt", nullable = false)
    private Instant updatedAt;

    public Roles() {}
}
