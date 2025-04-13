package font;

import doom.DoomMain;
import v.renderers.DoomScreen;

import static data.Defines.HU_FONTSIZE;
import static data.Defines.HU_FONTSTART;
import static v.renderers.DoomScreen.FG;

public record HudFont(DoomMain<?, ?> doom) implements DoomFont {
    @Override
    public void drawString(DoomScreen screen, String string, int x, int y) {
        var font = doom.headsUp.getHUFonts();
        // draw some of the text onto the screen
        int cx = x, cy = y;
        final char[] ch = string.toCharArray();

        for (int i = 0; i < ch.length; i++) {
            int c = ch[i];
            if (c == 0) {
                break;
            }
            if (c == '\n') {
                cx = 10;
                cy += 11;
                continue;
            }

            c = Character.toUpperCase(c) - HU_FONTSTART;
            if (c < 0 || c > HU_FONTSIZE) {
                cx += 4;
                continue;
            }

            if (cx + font[c].width > doom.vs.getScreenWidth()) {
                continue;
            }
            doom.graphicSystem.DrawPatchScaled(FG, font[c], doom.vs, cx, cy);
            cx += font[c].width;
        }
    }

    @Override
    public void drawCentered(DoomScreen screen, String string1, int x, int y) {
        var font = doom.headsUp.getHUFonts();
        // draw some of the text onto the screen
        int cx, cy = y;
        for (var string : string1.split("\n")) {
            cx = x + getWidth(string) / 2;

            final char[] ch = string.toCharArray();
            for (int i = 0; i < ch.length; i++) {
                int c = ch[i];
                if (c == 0) {
                    break;
                }
                c = Character.toUpperCase(c) - HU_FONTSTART;
                if (c < 0 || c > HU_FONTSIZE) {
                    cx += 4;
                    continue;
                }

                if (cx + font[c].width > doom.vs.getScreenWidth()) {
                    continue;
                }
                doom.graphicSystem.DrawPatchScaled(FG, font[c], doom.vs, cx, cy);
                cx += font[c].width;
            }
            cy += 11;
        }
    }

    @Override
    public int getWidth(String string) {
        var font = doom.headsUp.getHUFonts();
        // draw some of the text onto the screen
        int maxWidth = 0;
        int cx = 0;
        final char[] ch = string.toCharArray();

        for (int i = 0; i < ch.length; i++) {
            int c = ch[i];
            if (c == 0) {
                break;
            }
            if (c == '\n') {
                maxWidth = Math.max(maxWidth, cx);
                cx = 0;
                continue;
            }

            c = Character.toUpperCase(c) - HU_FONTSTART;
            if (c < 0 || c > HU_FONTSIZE) {
                cx += 4;
                continue;
            }
            cx += font[c].width;
        }

        return Math.max(maxWidth, cx);
    }

    @Override
    public int getHeight(String string) {
        return string.split("\n").length * 11;
    }
}
