package musico.services.user.models;

import lombok.Builder;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("profile_pictures")
@Data
@Builder
public class ProfilePicture {
    @Id
    private String id;
    private String userId;
    private String fileSize;
    private byte[] image;
}
