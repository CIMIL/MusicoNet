package musico.services.databases.models.kafka;

public record UserSearchParams(
        String username,
        Integer maxAge,
        Integer minAge,
        String[] genres,
        String[] instruments,
        String multi_instrumentalism_level,
        String gender
) { }
