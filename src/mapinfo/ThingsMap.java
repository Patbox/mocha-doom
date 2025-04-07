package mapinfo;

import data.info;
import data.mobjinfo_t;
import data.mobjtype_t;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThingsMap {
    private static final Map<String, mobjtype_t> MAP = new HashMap<>();

    public static mobjtype_t get(String name) {
        return MAP.get(name);
    }

    static {
        var stream = ThingsMap.class.getClassLoader().getResourceAsStream("thingtypes.txt");
        if (stream == null) {
            try {
                stream = Files.newInputStream(Paths.get("src", "thingtypes.txt"));
            } catch (IOException e) {

            }
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            int i = 0;
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                MAP.put(line,mobjtype_t.values()[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
