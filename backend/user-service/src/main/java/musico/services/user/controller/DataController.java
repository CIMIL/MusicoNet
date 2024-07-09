package musico.services.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/data")
public class DataController {

    @GetMapping("/genres")
    public List<String> getGenres(){
        return List.of("Rock", "Pop", "Jazz", "Blues", "Classical", "Country", "Hip-Hop", "Rap", "Reggae", "Metal", "Punk", "Folk", "Electronic", "Dance", "Techno", "House");
    }
    @GetMapping("/instruments")
    public List<String> getInstruments(){
        return List.of("Guitar", "Bass", "Drums", "Piano", "Keyboard", "Violin", "Cello", "Saxophone", "Trumpet", "Trombone", "Flute", "Clarinet", "Harp", "Accordion", "Harmonica", "Bagpipes", "Didgeridoo", "Banjo", "Mandolin", "Ukulele", "Ocarina", "Theremin", "Xylophone", "Marimba", "Vibraphone", "Timpani", "Conga", "Bongo", "Djembe", "Tabla", "Cajon", "Tambourine", "Triangle", "Cowbell", "Woodblock", "Tambour", "Castanets", "Maracas", "Guiro", "Cabasa", "Claves", "Ratchet", "Siren", "Whistle", "Slide Whistle", "Kazoo", "Jaw Harp", "Didgeridoo", "Ocarina", "Theremin", "Xylophone", "Marimba", "Vibraphone", "Timpani", "Conga", "Bongo", "Djembe", "Tabla", "Cajon", "Tambourine", "Triangle", "Cowbell", "Woodblock", "Tambour", "Castanets", "Maracas", "Guiro", "Cabasa", "Claves", "Ratchet", "Siren", "Whistle", "Slide Whistle", "Kazoo", "Jaw Harp");
    }
}
