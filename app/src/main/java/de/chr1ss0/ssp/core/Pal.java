package de.chr1ss0.ssp.core;

/** Global colour palette (Game Boy Color inspired) and string-art colour codes. */
public final class Pal {
    public static final int CLEAR = 0x00000000;
    public static final int OUTLINE = 0xFF202028;

    // UI colours
    public static final int BG = 0xFFF8F8F0;
    public static final int INK = 0xFF202028;
    public static final int INK_SOFT = 0xFF606070;
    public static final int WHITE = 0xFFF8F8F8;
    public static final int GRAY = 0xFFA0A0A8;
    public static final int LIGHT = 0xFFD8D8E0;
    public static final int DARK = 0xFF484850;
    public static final int BEZEL = 0xFF30303C;
    public static final int HP_GREEN = 0xFF30C048;
    public static final int HP_YELLOW = 0xFFF8C020;
    public static final int HP_RED = 0xFFE83030;
    public static final int GROUND = 0xFFD8E0B0;
    public static final int GROUND_DARK = 0xFFB8C890;
    public static final int SELECT = 0xFFFFF0B0;
    public static final int RED = 0xFFE83030;
    public static final int BLUE = 0xFF3870E0;
    public static final int GREEN = 0xFF30A040;
    public static final int GOLD = 0xFFE8A800;

    public static final int[] RARITY = {0xFF888890, 0xFF3070E0, 0xFFA040E0, 0xFFE8A000};
    public static final int[] RARITY_LIGHT = {0xFFE8E8EC, 0xFFD8E4FF, 0xFFF0DCFF, 0xFFFFF0C0};

    private static final int[] MAP = new int[128];

    static {
        set('k', OUTLINE);
        set('x', 0xFF101010);
        set('K', 0xFF484850);
        set('G', 0xFF707078);
        set('g', 0xFFA0A0A8);
        set('l', 0xFFC8C8D0);
        set('W', 0xFFE0E0E8);
        set('w', 0xFFF8F8F8);
        set('B', 0xFF603818);
        set('b', 0xFF905828);
        set('n', 0xFFC88850);
        set('N', 0xFFE8C890);
        set('Y', 0xFFC89010);
        set('y', 0xFFF8D030);
        set('z', 0xFFFFF4B0);
        set('O', 0xFFD06010);
        set('o', 0xFFF89030);
        set('R', 0xFFA01818);
        set('r', 0xFFE83030);
        set('P', 0xFFB03878);
        set('p', 0xFFF890B0);
        set('V', 0xFF603098);
        set('v', 0xFFA060E0);
        set('U', 0xFF203888);
        set('u', 0xFF3870E0);
        set('C', 0xFF2898C8);
        set('c', 0xFF70D0F8);
        set('i', 0xFFD0F0FF);
        set('E', 0xFF287028);
        set('e', 0xFF48B040);
        set('f', 0xFFA0E060);
        set('s', 0xFFF8C8A0);
        set('S', 0xFFD89870);
    }

    private static void set(char c, int argb) { MAP[c] = argb; }

    public static int color(char c) {
        if (c >= 128) return 0;
        return MAP[c];
    }

    /** Darkens a colour by factor (0..1 = amount kept). */
    public static int shade(int c, float f) {
        int r = (int) (((c >> 16) & 255) * f), g = (int) (((c >> 8) & 255) * f), b = (int) ((c & 255) * f);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int lighten(int c, float f) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        r = (int) (r + (255 - r) * f); g = (int) (g + (255 - g) * f); b = (int) (b + (255 - b) * f);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int mix(int a, int b, float t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return 0xFF000000 | ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    private Pal() {}
}
