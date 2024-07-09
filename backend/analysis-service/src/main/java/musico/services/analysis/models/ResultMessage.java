package musico.services.analysis.models;

import lombok.Data;

import java.time.LocalDate;

public record ResultMessage (String requestID,
                             String userId,
                             String firstName,
                             String username,
                             String surname,
                             LocalDate birthdate,
                             String[] genres,
                             String[] instruments,
                             String description,
                             String profilePicturePath,
                             String spotify,
                             String youtube,
                             String soundcloud,
                             String appleMusic,
                             String tidal,
                             String amazonMusic
){}
