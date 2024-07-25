package musico.services.user.models;

import lombok.Data;

@Data
public class UserParams {
    private Integer maxAge;
    private Integer minAge;
    private String[] Genres;
    private String[] Instruments;
    private String multi_instrumentalism_level;
    private String gender;

}
