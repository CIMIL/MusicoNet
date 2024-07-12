package musico.services.databases.models.kafka;


public record MusicalWorkQueryParams(String requestId, String[] genres, String mood, Integer bpm, Float danceability, Tonality tonality) { }

