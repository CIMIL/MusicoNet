package musico.services.user.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserProfileDTO  {
    /*
    * generalInfo: {
      fullname: '',
      username: '',
      birthDate: new Date(),
      location: '',
      description: '',
      profilePicture: '',
    },
    playedInstruments: [],
    genres: [],
    personalMusic: {
      file: '',
      title: '',
    },
    otherPlatforms: {
      soundcloud: '',
      youtube: '',
      spotify: '',
      appleMusic: '',
      tidal: '',
      amazonMusic: '',
    }
  });
    *
    * */
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("username")
    private String username;
    @JsonProperty("genres")
    private List<String> genres;
    @JsonProperty("instruments")
    private List<String> instruments;
    @JsonProperty("location")
    private String location;
    @JsonProperty("description")
    private String description;

    @JsonProperty("soundCloud")
    private String soundCloud;
    @JsonProperty("youtube")
    private String youtube;
    @JsonProperty("spotify")
    private String spotify;
    @JsonProperty("appleMusic")
    private String appleMusic;
    @JsonProperty("tidal")
    private String tidal;
    @JsonProperty("amazonMusic")
    private String amazonMusic;


}
