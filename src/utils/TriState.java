package utils;

public enum TriState {
    TRUE,
    FALSE,
    DEFAULT;

    public static TriState from(boolean b) {
        return b ? TRUE : FALSE;
    }
}
