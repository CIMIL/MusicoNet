package musico.services.analysis.models;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AnalysisMessage {
    private String user;
    private Integer bpm;
    private Float danceability;
    private String [] genres;
    private String mood;
}
