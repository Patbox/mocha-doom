package mapinfo;

import data.mobjtype_t;
import p.floor_e;
import doom.MapId;
import utils.TriState;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MapEntry {
    public String mapId = "";
    public String levelName = null;
    public String author = null;
    public Optional<String> label = null;
    public String levelpic = null;
    public MapId nextMap = null;
    public MapId nextSecretMap = null;
    public String skyTexture = null;
    public String music = null;
    public String exitPic = null;
    public String enterPic = null;
    public int partime = -1;
    public TriState endgame = TriState.DEFAULT;
    public String endpic = null;
    public TriState endbunny = TriState.DEFAULT;
    public TriState endcast = TriState.DEFAULT;
    public TriState nointermission = TriState.DEFAULT;
    public Optional<String[]> intertext = null;
    public Optional<String[]> intertextsecret = null;
    public String interbackdrop = null;
    public String intermusic = null;
    public BossAction bossaction = null;

    public record BossActionEntry(mobjtype_t actor, int linespecial, int tag) {};
    public record BossAction(List<BossActionEntry> entries, boolean reset) {};
}
