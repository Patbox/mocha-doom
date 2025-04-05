package doom;

import utils.StringParser;

import java.util.Locale;

public record MapId(String lumpName, int episode, int map) {

    public static MapId parse(String s) {
        if (s.toLowerCase(Locale.ROOT).startsWith("map")) {
            return new MapId(s, 1, Integer.parseInt(s.substring("map".length())));
        }
        var parser = new StringParser(s);

        int episode = 0;
        int map = 0;
        if (parser.peek() == 'E' || parser.peek() == 'e') {
            parser.next();
            episode = parser.readInt();
        }
        if (parser.peek() == 'M' || parser.peek() == 'm') {
            parser.next();
            map = parser.readInt();
        }

        return new MapId(s, episode, map);
    }

    public MapId withMap(int i) {
        if (this.lumpName.startsWith("MAP")) {
            return MapId.parse("MAP" + (i < 10 ? "0" : "") + i);
        } else if (this.lumpName.startsWith("E")) {
            return MapId.parse("E" + this.episode + "M" + i);
        }
        return this;
    }

    public MapId withNextMap() {
        return withMap(this.map + 1);
    }
}
