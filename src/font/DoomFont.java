package font;

import v.renderers.DoomScreen;

public interface DoomFont {
    void drawString(DoomScreen screen, String string, int x, int y);
    int getWidth(String string);
    int getHeight(String string);

    default void drawCentered(DoomScreen screen, String string, int x, int y) {
        drawString(screen, string, x + getWidth(string) / 2, y);
    }

}
