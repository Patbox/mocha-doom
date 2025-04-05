package mapinfo;

import data.info;
import data.mobjinfo_t;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThingsMap {
    private static final Map<String, mobjinfo_t> MAP = new HashMap<>();

    public static mobjinfo_t get(String name) {
        return MAP.get(name);
    }

    static {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ThingsMap.class.getClassLoader().getResourceAsStream("thingtypes.txt")))) {
            int i = 0;
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                MAP.put(line, info.mobjinfo[i]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
