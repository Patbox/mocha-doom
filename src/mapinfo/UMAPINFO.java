package mapinfo;

import doom.MapId;
import utils.StringParser;
import utils.TriState;
import w.CacheableDoomObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class UMAPINFO implements MapInfo, CacheableDoomObject {
    private final Map<String, MapEntry> maps = new HashMap<>();
    private final List<EpisodeEntry> episodes = new ArrayList<>();

    @Override
    public Map<String, MapEntry> maps() {
        return this.maps;
    }

    @Override
    public List<EpisodeEntry> episodes() {
        return this.episodes;
    }

    @Override
    public void unpack(ByteBuffer buf) throws IOException {
        var parser = new StringParser(new String(buf.array()));
        while (!parser.end()) {
            parser.skipWhitespaceAndComments();
            if (!parser.readLowerIdentifier().equals("map")) {
                throw new RuntimeException("Invalid data!");
            }
            parser.skipWhitespaceAndComments();
            var map = new MapEntry();
            map.mapId = parser.readIdentifier();
            EpisodeEntry episode = null;
            parser.skipWhitespaceAndComments();
            parser.expect('{');
            while (parser.peek() != '}' && !parser.end()) {
                parser.skipWhitespaceAndComments();
                var key = parser.readLowerIdentifier();
                parser.skipWhitespaceAndComments();
                parser.expect('=');
                parser.skipWhitespaceAndComments();

                switch (key) {
                    case "levelname" -> map.levelName = parser.readString();
                    case "levelpic" -> map.levelpic = parser.readString();
                    case "next" -> map.nextMap = MapId.parse(parser.readString());
                    case "nextsecret" -> map.nextSecretMap = MapId.parse(parser.readString());
                    case "skytexture" -> map.skyTexture = parser.readString();
                    case "music" -> map.music = parser.readString();
                    case "exitpic" -> map.exitPic = parser.readString();
                    case "enterpic" -> map.enterPic = parser.readString();
                    case "partime" -> map.partime = parser.readInt();
                    case "endgame" -> map.endgame = TriState.from(parser.readBoolean());
                    case "endpic" -> map.endpic = parser.readString();
                    case "endbunny" -> map.endbunny = TriState.from(parser.readBoolean());
                    case "endcast" -> map.endcast = TriState.from(parser.readBoolean());
                    case "nointermission" -> map.nointermission = TriState.from(parser.readBoolean());
                    case "intertext" -> map.intertext = readIntertext(parser);
                    case "intertextsecret" -> map.intertextsecret = readIntertext(parser);
                    case "intermusic" -> map.intermusic = parser.readString();
                    case "episode" -> {
                        if (episode == null) {
                            episode = new EpisodeEntry();
                            episode.map = MapId.parse(map.mapId);
                        }
                        if (checkClear(parser)) {
                            episode.clearPrevious = true;
                            break;
                        }
                        episode.texture = parser.readString();
                        parser.skipWhitespaceAndComments();
                        parser.expect(',');
                        parser.skipWhitespaceAndComments();
                        episode.name = parser.readString();
                        parser.skipWhitespaceAndComments();
                        parser.expect(',');
                        parser.skipWhitespaceAndComments();
                        episode.key = parser.readString();
                    }
                    case "bossaction" -> {
                        if (map.bossaction == null) {
                            map.bossaction = new MapEntry.BossAction(new ArrayList<>(), false);
                        }
                        if (checkClear(parser)) {
                            map.bossaction = new MapEntry.BossAction(map.bossaction.entries(), true);
                            break;
                        }
                        var thing = parser.readString();
                        parser.skipWhitespaceAndComments();
                        parser.expect(',');
                        parser.skipWhitespaceAndComments();
                        var linespecial = parser.readInt();
                        parser.skipWhitespaceAndComments();
                        parser.expect(',');
                        parser.skipWhitespaceAndComments();
                        var tag = parser.readInt();

                        map.bossaction.entries().add(new MapEntry.BossActionEntry(ThingsMap.get(thing), linespecial, tag));
                    }

                    default -> parser.skipUntil('\n');
                }

                parser.skipWhitespaceAndComments();
            }
            parser.expect('}');
            parser.skipWhitespaceAndComments();

            this.maps.put(map.mapId, map);
            if (episode != null) {
                this.episodes.add(episode);
            }
        }
    }

    private static boolean checkClear(StringParser parser) {
        try {
            var pos = parser.pos();
            var tmp = parser.readIdentifier();
            if (tmp.equalsIgnoreCase("clear")) {
                return true;
            }
            parser.pos(pos);
        } catch (Throwable ignored) {}
        return false;
    }

    private static Optional<String[]> readIntertext(StringParser parser) {
        try {
            var pos = parser.pos();
            var tmp = parser.readIdentifier();
            if (tmp.equalsIgnoreCase("clear")) {
                return Optional.empty();
            }
            parser.pos(pos);
        } catch (Throwable ignored) {}
        var list = new ArrayList<String>();

        while (true) {
            parser.skipWhitespaceAndComments();
            var txt = parser.readString();
            list.add(txt + "\n");
            parser.skipWhitespaceAndComments();
            if (parser.peek() != ',') {
                break;
            } else {
                parser.next();
            }
        }
        return Optional.of(list.toArray(new String[0]));
    }
}
