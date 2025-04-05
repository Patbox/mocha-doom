package data;

import java.util.*;
import java.util.function.Function;

public class Registry<T> implements Iterable<T> {
    private final List<T> entries = new ArrayList<>();
    private final IdentityHashMap<T, Integer> ids = new IdentityHashMap<>();
    private final Map<String, T> byName = new HashMap<>();
    private final Map<String, Integer> idByName = new HashMap<>();
    private final IdentityHashMap<T, String> names = new IdentityHashMap<>();

    public Registry() {
    }

    public Registry(Function<T, String> nameExtractor, T[] entries) {
        for (var entry : entries) {
            this.add(nameExtractor.apply(entry), entry);
        }
    }

    public void add(String name, T entry) {
        var numId = entries.size();
        ids.put(entry, numId);
        entries.add(entry);
        if (name != null) {
            idByName.put(name, numId);
            this.byName.put(name, entry);
            this.names.put(entry, name);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return this.entries.iterator();
    }

    public T get(int id) {
        return this.entries.get(id);
    }

    public T get(String name) {
        return this.byName.get(name);
    }

    public boolean constainsId(int musicnum) {
        return this.entries.size() > musicnum && musicnum > 0;
    }

    public String getName(T entry) {
        return this.names.get(entry);
    }

    public boolean containsName(String name) {
        return this.byName.containsKey(name);
    }

    public int getIdByName(String name) {
        return this.idByName.get(name);
    }
}
