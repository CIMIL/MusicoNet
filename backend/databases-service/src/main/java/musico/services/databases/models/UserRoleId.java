package musico.services.databases.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Builder
@Embeddable
@AllArgsConstructor
public class UserRoleId implements Serializable {
    @Serial
    private static final long serialVersionUID = 2279551224631562028L;
    @Column(name = "roleId", nullable = false)
    private Integer roleId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    public UserRoleId () {}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserRoleId entity = (UserRoleId) o;
        return Objects.equals(this.roleId, entity.roleId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, userId);
    }

    @Override
    public String toString() {
        return "UserRoleId{" +
                "roleId=" + roleId +
                ", userId=" + userId +
                '}';
    }
}