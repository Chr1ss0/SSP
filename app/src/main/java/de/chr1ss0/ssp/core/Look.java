package de.chr1ss0.ssp.core;

import java.util.Random;

/** Appearance of a battle creature (the player's partner or an opponent's). */
public final class Look {
    public static final int KNUDDEL = 0, KATZE = 1, DRACHE = 2, HASE = 3, FROSCH = 4, VOGEL = 5, BAER = 6, FUCHS = 7;
    public static final String[] SPECIES_NAMES = {"Knuddel", "Katze", "Drache", "Hase", "Frosch", "Vogel", "Bär", "Fuchs"};

    public static final int[] COLORS = {
            0xFFE8574E, 0xFF4F8BEB, 0xFF5CC46A, 0xFFF4CC3C, 0xFF9B6CE3, 0xFFF29541,
            0xFF4B4B5E, 0xFFEEF0F6, 0xFFF59BC0, 0xFF46C6C8, 0xFFA8744C, 0xFF9AA2B4
    };
    public static final String[] COLOR_NAMES = {
            "Rot", "Blau", "Grün", "Gelb", "Lila", "Orange", "Schwarz", "Weiß", "Rosa", "Türkis", "Braun", "Grau"
    };
    public static final int[] BELLY = {
            0xFFFFF0D4, 0xFFFFFFFF, 0xFFFFE48A, 0xFFFFD2E1, 0xFFCFF5DC, 0xFFD3E7FF, 0xFFE7DAFF, 0xFFFFD8B6
    };
    public static final String[] BELLY_NAMES = {"Creme", "Weiß", "Gelb", "Rosa", "Mint", "Hellblau", "Flieder", "Pfirsich"};
    public static final String[] PATTERN_NAMES = {"Keins", "Streifen", "Punkte", "Spitzen"};
    public static final int[] EYE_COLORS = {0xFF6B3E26, 0xFF3D7BFF, 0xFF2FA05A, 0xFF5E6478, 0xFF8A4DDB, 0xFFE09A1F};
    public static final String[] EYE_NAMES = {"Braun", "Blau", "Grün", "Grau", "Violett", "Bernstein"};
    public static final String[] CREST_NAMES = {"Keins", "Hörner", "Blatt", "Flamme", "Antennen", "Kristall", "Juwel"};
    public static final String[] TAIL_NAMES = {"Normal", "Blitz", "Flamme", "Blatt", "Puschel", "Keiner"};
    public static final String[] HAT_NAMES = {"Keine", "Kappe", "Mütze", "Stirnband", "Krone", "Zauberhut", "Helm"};
    public static final String[] EXTRA_NAMES = {"Keins", "Brille", "Sonnenbrille", "Schal", "Fliege"};

    public static final int PAT_NONE = 0, PAT_STRIPES = 1, PAT_SPOTS = 2, PAT_TIPS = 3;
    public static final int CREST_NONE = 0, CREST_HORNS = 1, CREST_LEAF = 2, CREST_FLAME = 3, CREST_ANTENNA = 4,
            CREST_CRYSTAL = 5, CREST_GEM = 6;
    public static final int TAIL_NORMAL = 0, TAIL_BOLT = 1, TAIL_FLAME = 2, TAIL_LEAF = 3, TAIL_PUFF = 4, TAIL_NONE = 5;
    public static final int HAT_NONE = 0, HAT_CAP = 1, HAT_BEANIE = 2, HAT_BAND = 3, HAT_CROWN = 4, HAT_WIZARD = 5, HAT_HELMET = 6;
    public static final int EX_NONE = 0, EX_GLASSES = 1, EX_SHADES = 2, EX_SCARF = 3, EX_BOW = 4;

    /** Names opponents give their creatures, per species. */
    private static final String[][] CREATURE_NAMES = {
            {"PUFFEL", "MOPSI", "BOBBEL", "FLAUSCH", "KNUDDI", "PLÜSCHI"},
            {"MAUZI", "SCHNURRI", "MIEZI", "TATZE", "KRALLI", "FAUCHI"},
            {"DRACHI", "SCHUPPI", "GLUTZAHN", "DRAKO", "FUNKO", "ZÜNDI"},
            {"HOPPEL", "MÜMMEL", "FLITZI", "LÖFFEL", "MOHRLI", "SPRINGI"},
            {"QUAKI", "PLATSCH", "HÜPFI", "GLUBSCH", "SCHLABBI", "FROSCHI"},
            {"PIEPO", "FLATTI", "ZWITSCHI", "SCHNABEL", "GEFIEDO", "PICKI"},
            {"BRUMMI", "TAPSI", "HONIGO", "TATZO", "KNURRI", "BÄRCHEN"},
            {"FUCHSI", "REINO", "SCHLAUI", "FLINKI", "RÖTEL", "LUNTI"},
    };

    public int species, color, belly, pattern, eyes, crest, tail, hat, extra;

    public Look() {
        species = KATZE; color = 0; belly = 0; pattern = PAT_NONE; eyes = 1; crest = CREST_NONE;
        tail = TAIL_NORMAL; hat = HAT_CAP; extra = EX_NONE;
    }

    public Look(int species, int color, int belly, int pattern, int eyes, int crest, int tail, int hat, int extra) {
        this.species = species; this.color = color; this.belly = belly; this.pattern = pattern; this.eyes = eyes;
        this.crest = crest; this.tail = tail; this.hat = hat; this.extra = extra;
    }

    public Look copy() {
        return new Look(species, color, belly, pattern, eyes, crest, tail, hat, extra);
    }

    public void set(Look o) {
        species = o.species; color = o.color; belly = o.belly; pattern = o.pattern; eyes = o.eyes;
        crest = o.crest; tail = o.tail; hat = o.hat; extra = o.extra;
    }

    public static Look random(Random r) {
        Look l = new Look();
        l.species = r.nextInt(SPECIES_NAMES.length);
        l.color = r.nextInt(COLORS.length);
        l.belly = r.nextInt(BELLY.length);
        l.pattern = r.nextInt(3) == 0 ? 1 + r.nextInt(PATTERN_NAMES.length - 1) : PAT_NONE;
        l.eyes = r.nextInt(EYE_COLORS.length);
        l.crest = r.nextInt(3) == 0 ? 1 + r.nextInt(CREST_NAMES.length - 1) : CREST_NONE;
        l.tail = r.nextInt(4) == 0 ? 1 + r.nextInt(TAIL_NAMES.length - 1) : TAIL_NORMAL;
        int h = r.nextInt(10);
        l.hat = h < 6 ? HAT_NONE : (h < 7 ? HAT_CAP : (h < 8 ? HAT_BEANIE : (h < 9 ? HAT_BAND : HAT_HELMET)));
        int e = r.nextInt(10);
        l.extra = e < 6 ? EX_NONE : (e < 7 ? EX_GLASSES : (e < 8 ? EX_SHADES : (e < 9 ? EX_SCARF : EX_BOW)));
        return l;
    }

    /** A name an opponent might give a creature of this species. */
    public static String creatureName(int species, Random r) {
        String[] n = CREATURE_NAMES[Math.max(0, Math.min(CREATURE_NAMES.length - 1, species))];
        return n[r.nextInt(n.length)];
    }

    public String key() {
        return "m:" + species + "," + color + "," + belly + "," + pattern + "," + eyes + "," + crest + "," + tail + ","
                + hat + "," + extra;
    }

    public static Look parse(String s) {
        Look l = new Look();
        try {
            if (s.startsWith("m:")) {
                String[] p = s.substring(2).split(",");
                l.species = clamp(Integer.parseInt(p[0]), SPECIES_NAMES.length);
                l.color = clamp(Integer.parseInt(p[1]), COLORS.length);
                l.belly = clamp(Integer.parseInt(p[2]), BELLY.length);
                l.pattern = clamp(Integer.parseInt(p[3]), PATTERN_NAMES.length);
                l.eyes = clamp(Integer.parseInt(p[4]), EYE_COLORS.length);
                l.crest = clamp(Integer.parseInt(p[5]), CREST_NAMES.length);
                l.tail = clamp(Integer.parseInt(p[6]), TAIL_NAMES.length);
                l.hat = clamp(Integer.parseInt(p[7]), HAT_NAMES.length);
                l.extra = clamp(Integer.parseInt(p[8]), EXTRA_NAMES.length);
            } else {
                // older saves stored a human character: hair,hairColor,skin,shirt,pants,hat,extra[,eyes,top]
                String[] p = s.split(",");
                l.species = clamp(Integer.parseInt(p[0]), SPECIES_NAMES.length);
                l.color = clamp(Integer.parseInt(p[3]), COLORS.length);
                l.belly = clamp(Integer.parseInt(p[2]), BELLY.length);
                l.hat = clamp(Integer.parseInt(p[5]), HAT_NAMES.length);
                int ex = Integer.parseInt(p[6]);
                l.extra = ex == 1 ? EX_GLASSES : (ex == 2 ? EX_SHADES : EX_NONE);
                if (p.length > 8) l.eyes = clamp(Integer.parseInt(p[7]), EYE_COLORS.length);
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
