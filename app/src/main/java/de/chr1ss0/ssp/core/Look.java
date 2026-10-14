package de.chr1ss0.ssp.core;

import java.util.Random;

/** Appearance of a character (hero or trainer). */
public final class Look {
    public static final String[] HAIR_NAMES = {"Kurz", "Stachelig", "Lang", "Zopf", "Afro", "Irokese", "Glatze", "Bob"};
    public static final int[] HAIR_COLORS = {
            0xFF383038, 0xFF784020, 0xFFE8C050, 0xFFD04820, 0xFFE0E0E8,
            0xFF4868E0, 0xFF40B060, 0xFFF070B0, 0xFF8850C8, 0xFFF08820
    };
    public static final String[] HAIR_COLOR_NAMES = {
            "Schwarz", "Braun", "Blond", "Rot", "Weiß", "Blau", "Grün", "Pink", "Lila", "Orange"
    };
    public static final int[] SKIN = {0xFFFCE0C8, 0xFFF8C8A0, 0xFFE8B080, 0xFFC88850, 0xFF956038, 0xFF684028};
    public static final String[] SKIN_NAMES = {"Hell", "Pfirsich", "Sand", "Karamell", "Braun", "Dunkel"};
    public static final int[] CLOTH = {
            0xFFE03838, 0xFF3868D8, 0xFF38A048, 0xFFF0C830, 0xFF8848C8, 0xFFF08830,
            0xFF404048, 0xFFF0F0F0, 0xFFF080B0, 0xFF48C0D8, 0xFF8C5830, 0xFF8890A0
    };
    public static final String[] CLOTH_NAMES = {
            "Rot", "Blau", "Grün", "Gelb", "Lila", "Orange", "Schwarz", "Weiß", "Rosa", "Türkis", "Braun", "Grau"
    };
    public static final String[] HAT_NAMES = {"Keine", "Kappe", "Mütze", "Stirnband", "Krone", "Zauberhut", "Helm"};
    public static final String[] EXTRA_NAMES = {"Keins", "Brille", "Sonnenbrille", "Augenklappe", "Schnurrbart"};
    public static final int[] EYE_COLORS = {0xFF6B3E26, 0xFF3D7BFF, 0xFF2FA05A, 0xFF707890, 0xFF8A4DDB, 0xFFE09A1F};
    public static final String[] EYE_NAMES = {"Braun", "Blau", "Grün", "Grau", "Violett", "Bernstein"};
    public static final String[] TOP_NAMES = {"T-Shirt", "Hoodie", "Jacke"};

    public static final int HAT_NONE = 0, HAT_CAP = 1, HAT_BEANIE = 2, HAT_BAND = 3, HAT_CROWN = 4, HAT_WIZARD = 5, HAT_HELMET = 6;

    public int hair, hairColor, skin, shirt, pants, hat, extra;
    public int eyes, top;

    public Look() {
        hair = 1; hairColor = 0; skin = 1; shirt = 0; pants = 1; hat = HAT_CAP; extra = 0; eyes = 1; top = 1;
    }

    public Look(int hair, int hairColor, int skin, int shirt, int pants, int hat, int extra) {
        this.hair = hair; this.hairColor = hairColor; this.skin = skin; this.shirt = shirt;
        this.pants = pants; this.hat = hat; this.extra = extra;
    }

    public Look copy() {
        Look l = new Look(hair, hairColor, skin, shirt, pants, hat, extra);
        l.eyes = eyes;
        l.top = top;
        return l;
    }

    public static Look random(Random r) {
        Look l = new Look();
        l.hair = r.nextInt(HAIR_NAMES.length);
        l.hairColor = r.nextInt(5) == 0 ? 5 + r.nextInt(5) : r.nextInt(5);
        l.skin = r.nextInt(SKIN.length);
        l.shirt = r.nextInt(CLOTH.length);
        l.pants = r.nextInt(CLOTH.length);
        if (l.pants == l.shirt) l.pants = (l.pants + 5) % CLOTH.length;
        int h = r.nextInt(10);
        l.hat = h < 5 ? HAT_NONE : (h < 7 ? HAT_CAP : (h < 8 ? HAT_BEANIE : (h < 9 ? HAT_BAND : HAT_HELMET)));
        int e = r.nextInt(10);
        l.extra = e < 6 ? 0 : (e < 8 ? 1 : (e < 9 ? 2 : 4));
        l.eyes = r.nextInt(EYE_COLORS.length);
        l.top = r.nextInt(TOP_NAMES.length);
        return l;
    }

    public String key() {
        return hair + "," + hairColor + "," + skin + "," + shirt + "," + pants + "," + hat + "," + extra + "," + eyes + "," + top;
    }

    public static Look parse(String s) {
        Look l = new Look();
        try {
            String[] p = s.split(",");
            l.hair = clamp(Integer.parseInt(p[0]), HAIR_NAMES.length);
            l.hairColor = clamp(Integer.parseInt(p[1]), HAIR_COLORS.length);
            l.skin = clamp(Integer.parseInt(p[2]), SKIN.length);
            l.shirt = clamp(Integer.parseInt(p[3]), CLOTH.length);
            l.pants = clamp(Integer.parseInt(p[4]), CLOTH.length);
            l.hat = clamp(Integer.parseInt(p[5]), HAT_NAMES.length);
            l.extra = clamp(Integer.parseInt(p[6]), EXTRA_NAMES.length);
            if (p.length > 8) {
                l.eyes = clamp(Integer.parseInt(p[7]), EYE_COLORS.length);
                l.top = clamp(Integer.parseInt(p[8]), TOP_NAMES.length);
            }
        } catch (RuntimeException e) {
            return new Look();
        }
        return l;
    }

    private static int clamp(int v, int n) {
        return v < 0 || v >= n ? 0 : v;
    }
}
