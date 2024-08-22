package musico.services.databases.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.databases.config.OntEntityField;
import musico.services.databases.enums.REGISTRATION_ENUMS;
import musico.services.databases.models.Users;
import musico.services.databases.models.kafka.MusicalWorkQueryParams;
import musico.services.databases.models.kafka.UserSearchParams;
import musico.services.databases.models.kafka.UsersQueryParams;
import musico.services.databases.services.kafka.MusicalWorkQueryParamsService;
import musico.services.databases.services.kafka.UsersQueryParamsService;
import musico.services.databases.utils.DataRetriever;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.query.DeleteDataQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.InsertDataQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserProfileService {
    private final DataRetriever dataRetriever;
    private final UsersQueryParamsService usersQueryParamsService;
    private final MusicalWorkQueryParamsService musicalWorkQueryParamsService;
    private final UserService userService;

    public REGISTRATION_ENUMS checkProfileAlreadyExists(UsersQueryParams userID) {
        GraphPatternNotTriples query = usersQueryParamsService.checkUserExists(userID);
        List<BindingSet> results = dataRetriever.createAndExecuteSelectQuery(query);
        if (!results.isEmpty()) {
            return REGISTRATION_ENUMS.CHECK_USER_EXISTS;
        }
        return REGISTRATION_ENUMS.CHECK_VALID;
    }

    public void createUserProfile(UsersQueryParams signupData) {
        List<TriplePattern> query = usersQueryParamsService.buildInsertQueryGraphPattern(signupData);
        userService.createUserProfile(signupData);
        dataRetriever.createAndExecuteInsertQuery(query);
    }

    public UsersQueryParams getUserProfile(UsersQueryParams userSignup) {
        Users user = userService.getUserProfile(userSignup.userId());
        if (user == null) {
            log.error("User not found: {}", userSignup.userId());
            return null;
        }
        GraphPatternNotTriples userQuery = usersQueryParamsService.buildQueryGraphPattern(userSignup);
        List<BindingSet> results = dataRetriever.createAndExecuteSelectQuery(userQuery);
        log.debug("results found for user: {}", results);
        UsersQueryParams.UsersQueryParamsBuilder builder = usersQueryParamsService.processUserGraphResults(results);
        builder.userId(userSignup.userId())
                .requestID(userSignup.requestID())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .surname(user.getLastName())
                .amazonMusic(user.getAmazonMusic())
                .appleMusic(user.getAppleMusic())
                .birthdate(user.getBirthdate())
                .description(user.getDescription())
                .profilePicturePath(user.getProfilePicturePath())
                .soundcloud(user.getSoundcloud())
                .spotify(user.getSpotify())
                .tidal(user.getTidal())
                .youtube(user.getYoutube());
        return builder.build();

    }

    public List<UsersQueryParams> getUsersProfileByUsername(UsersQueryParams params) {
        List<UsersQueryParams> response = new ArrayList<>();
        List<Users> users = userService.getUsersProfileByUsername(params.username());
        for (Users user : users) {
            log.info("User: {}", user.getUsername());
            GraphPatternNotTriples userQuery = usersQueryParamsService.buildQueryGraphPattern(params);
            log.info("User Query: {}", userQuery.getQueryString());
            List<BindingSet> results = dataRetriever.createAndExecuteSelectQuery(userQuery);
            if (results.isEmpty()) {
                log.error("No results found for user: {}", params);
            } else {
                UsersQueryParams.UsersQueryParamsBuilder builder = usersQueryParamsService.processUserGraphResults(results);
                builder.userId(user.getUserId())
                        .requestID(params.requestID())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .surname(user.getLastName())
                        .amazonMusic(user.getAmazonMusic())
                        .appleMusic(user.getAppleMusic())
                        .birthdate(user.getBirthdate())
                        .description(user.getDescription())
                        .profilePicturePath(user.getProfilePicturePath())
                        .soundcloud(user.getSoundcloud())
                        .spotify(user.getSpotify())
                        .tidal(user.getTidal())
                        .youtube(user.getYoutube());
                response.add(builder.build());
            }
        }
        return response;
    }

    public List<UsersQueryParams> searchUsers(UserSearchParams params) {
        List<UsersQueryParams> response = new ArrayList<>();
        List<Users> resultSQL = userService.getUsersOnSql(params.username(), params.minAge(), params.maxAge(), params.gender());
        if (resultSQL.isEmpty()) {
            log.error("No results found for search in SQL: {}", params);
            return response;
        }
        log.info("SQL Results found for search: {}", resultSQL);
        UsersQueryParams usersQueryParams = UsersQueryParams.builder()
                .genres(params.genres())
                .instruments(params.instruments())
                .build();
        GraphPatternNotTriples userQuery = usersQueryParamsService.buildQueryGraphPattern(usersQueryParams);
        List<BindingSet> results = dataRetriever.createAndExecuteSelectQuery(userQuery);
        if (results == null) {
            log.error("No results found for search in Graph: {}", params);
            return response;
        }
        resultSQL = resultSQL.stream()
                .filter(users -> results.stream()
                        .anyMatch(bindingSet -> bindingSet.getValue("user").stringValue().contains(users.getUserId())))
                .toList();
        log.info("Filtered SQL Results found for search: {}", resultSQL);
        for (Users user : resultSQL) {
            GraphPatternNotTriples userQuerySQL = usersQueryParamsService.buildQueryGraphPattern(UsersQueryParams.builder().userId(user.getUserId()).build());
            List<BindingSet> resultsSQL = dataRetriever.createAndExecuteSelectQuery(userQuerySQL);
            UsersQueryParams.UsersQueryParamsBuilder builder = usersQueryParamsService.processUserGraphResults(resultsSQL);
            builder.userId(user.getUserId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .surname(user.getLastName())
                    .amazonMusic(user.getAmazonMusic())
                    .appleMusic(user.getAppleMusic())
                    .birthdate(user.getBirthdate())
                    .description(user.getDescription())
                    .profilePicturePath(user.getProfilePicturePath())
                    .soundcloud(user.getSoundcloud())
                    .spotify(user.getSpotify())
                    .tidal(user.getTidal())
                    .youtube(user.getYoutube());
            response.add(builder.build());
        }
        // Join the results from SQL and RDF
        return response;
    }

    public void addAudioData(MusicalWorkQueryParams audioData) {
        Users user = userService.getUserProfile(audioData.requestId());
        // TODO: Check Max Number of Audio
        String numberQuery = musicalWorkQueryParamsService.getCountAudioProfileQueryString(user);
        log.debug("Number Query: {}", numberQuery);
        List<BindingSet> results = dataRetriever.executeQuery(numberQuery);
        if (results == null) {
            log.error("Error getting number of audio profiles");
            return;
        }
        int count = Integer.parseInt(results.get(0).getValue("count").stringValue());
        if (count >= 5) {
            log.error("Max number of audio profiles reached: {}", count);
            return;
        } else {
            log.info("Number of audio profiles: {}", count);
        }
        List<TriplePattern> query = musicalWorkQueryParamsService.getSaveAudioProfileQuery(audioData, user);
        dataRetriever.createAndExecuteInsertQuery(query);
    }

    public void updateProfile(UsersQueryParams updateData) {
        List<TriplePattern> delete = new ArrayList<>();
        List<TriplePattern> insert = new ArrayList<>();
        UsersQueryParams userUQP = getUserProfile(UsersQueryParams.builder().userId(updateData.userId()).build());
        Users user = usersQueryParamsService.getOntEntity(userUQP);
        Users updateUser = usersQueryParamsService.getOntEntity(updateData);
        user.getInstruments().forEach(instrument -> log.debug("Instrument: {}", instrument.getIRI()));
        log.debug("Update User: {}", updateUser);
        for (Field field : Users.class.getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(OntEntityField.class)) {
                continue;
            }
            try {
                log.debug("Field Value: {}", field.get(updateUser));
                if (field.get(updateUser) != null && !field.get(updateUser).equals("") && !((Collection<?>) field.get(updateUser)).isEmpty()) {
                    delete.add(user.getFieldTriplePattern(user, field));
                    insert.add(updateUser.getFieldTriplePattern(updateUser, field));

                }
            } catch (IllegalAccessException e) {
                log.error("Error updating profile: {}", e.getMessage());
            }
        }
        DeleteDataQuery delQuery = Queries.DELETE_DATA();
        InsertDataQuery insQuery = Queries.INSERT_DATA();
        delete.forEach(delQuery::deleteData);
        insert.forEach(insQuery::insertData);
        String query = delQuery.getQueryString() +" ; "+ insQuery.getQueryString();
        log.debug("Update Query: {}", query);
        dataRetriever.executeQuery(query);
    }

    public List<UsersQueryParams> getRecommendedUsers(UsersQueryParams userSignup) {
        Users user = userService.getUserProfile(userSignup.userId());
        List<UsersQueryParams> response = new ArrayList<>();
        List<BindingSet> recommendedUsers = dataRetriever.createAndExecuteSelectQuery(
                usersQueryParamsService.getRecommendedUsersIds(user.getIRI()),
                "users");
        for (BindingSet binding : recommendedUsers) {
            String iri =binding.getValue("users").stringValue();
            String userId = iri.substring(iri.lastIndexOf("/") + 1);
            UsersQueryParams userParams = UsersQueryParams.builder().userId(userId).build();
            UsersQueryParams userResult = getUserProfile(userParams);
            if (userResult != null) {
                response.add(userResult);
            }
        }
        return response;
    }
}
