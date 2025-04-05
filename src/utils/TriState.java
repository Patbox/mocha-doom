package utils;

public enum TriState {
    TRUE,
    FALSE,
    DEFAULT;

    public static TriState from(boolean b) {
        return b ? TRUE : FALSE;
    }

    public boolean get(boolean defaultValue) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            case DEFAULT -> defaultValue;
        };
    }
}
