package musico.services.databases.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSetting {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Id
    private Users user;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "email_notifications")
    private Boolean emailNotifications;


}