package de.chr1ss0.ssp.core;

/** Global colour palette and string-art colour codes. */
public final class Pal {
    public static final int CLEAR = 0x00000000;
    public static final int OUTLINE = 0xFF202028;

    // UI colours (modern light theme)
    public static final int BG = 0xFFF2F0FA;
    public static final int BG2 = 0xFFE3E9F6;
    public static final int INK = 0xFF1E2238;
    public static final int INK_SOFT = 0xFF697090;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int GRAY = 0xFFA3A8C0;
    public static final int LIGHT = 0xFFDDE1EE;
    public static final int DARK = 0xFF3A3F5C;
    public static final int BEZEL = 0xFF1E2238;
    public static final int PANEL_BORDER = 0xFFD3D8E8;
    public static final int HP_GREEN = 0xFF34D07A;
    public static final int HP_YELLOW = 0xFFFFC23D;
    public static final int HP_RED = 0xFFFF5A5F;
    public static final int GROUND = 0xFFCDE7B0;
    public static final int GROUND_DARK = 0xFFA9D18A;
    public static final int SELECT = 0xFFFFF4C8;
    public static final int RED = 0xFFF0475A;
    public static final int BLUE = 0xFF4C7DFF;
    public static final int GREEN = 0xFF1FAE6B;
    public static final int GOLD = 0xFFF5A623;
    public static final int ACCENT = 0xFF6A5CFF;
    public static final int ACCENT_DARK = 0xFF4A3DC9;

    public static final int[] RARITY = {0xFF8A90A8, 0xFF3D7BFF, 0xFFA24DFF, 0xFFF5A623};
    public static final int[] RARITY_LIGHT = {0xFFF1F2F7, 0xFFE3ECFF, 0xFFF2E6FF, 0xFFFFF3D6};

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

    /** Mixes two colours including their alpha channels. */
    public static int mixA(int a, int b, float t) {
        int aa = a >>> 24, ba = b >>> 24;
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (aa + (ba - aa) * t) << 24) | ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    public static int alpha(int c, int a) {
        return (a << 24) | (c & 0xFFFFFF);
    }

    private Pal() {}
}
