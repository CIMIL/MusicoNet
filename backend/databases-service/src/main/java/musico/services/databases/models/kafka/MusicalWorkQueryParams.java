package musico.services.databases.models.kafka;


public record MusicalWorkQueryParams(String requestId, String[] genre, Integer bpm, Float danceability) { }

