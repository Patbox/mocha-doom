package mapinfo;

import doom.DoomMain;

import java.util.List;
import java.util.Map;

public interface MapInfo {
    Map<String, MapEntry> maps();

    List<EpisodeEntry> episodes();
}
