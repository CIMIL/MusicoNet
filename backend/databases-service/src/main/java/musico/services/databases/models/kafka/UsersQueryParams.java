package musico.services.databases.models.kafka;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UsersQueryParams (String requestID,
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
) {}
