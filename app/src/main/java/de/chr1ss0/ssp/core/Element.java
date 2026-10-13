package de.chr1ss0.ssp.core;

/**
 * The nine weapon types. The chart is a regular tournament: every type beats
 * exactly four others, loses to four and draws against itself.
 */
public final class Element {
    public static final int STEIN = 0, KLINGE = 1, PAPIER = 2, FEUER = 3, WASSER = 4,
            BLITZ = 5, PFLANZE = 6, STICH = 7, EIS = 8;
    public static final int COUNT = 9;

    public static final String[] NAME = {
            "Stein", "Klinge", "Papier", "Feuer", "Wasser", "Blitz", "Pflanze", "Stich", "Eis"
    };
    public static final String[] UPPER = {
            "STEIN", "KLINGE", "PAPIER", "FEUER", "WASSER", "BLITZ", "PFLANZE", "STICH", "EIS"
    };
    /** Verb used in "A <verb> B!" when a weapon of this type wins. */
    public static final String[] VERB = {
            "zertrümmert", "zerschneidet", "umhüllt", "verbrennt", "überflutet",
            "elektrisiert", "umrankt", "durchsticht", "vereist"
    };
    public static final int[] COLOR = {
            0xFFA08868, 0xFF8898B8, 0xFFE8E0C8, 0xFFF06828, 0xFF3880F0,
            0xFFF0C000, 0xFF40B040, 0xFFC060A8, 0xFF70D8F0
    };
    /** Text colour readable on top of COLOR. */
    public static final int[] TEXT = {
            0xFFFFFFFF, 0xFFFFFFFF, 0xFF404040, 0xFFFFFFFF, 0xFFFFFFFF,
            0xFF403000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF104050
    };

    private static final int[][] BEATS_LIST = {
            /* STEIN   */ {KLINGE, FEUER, STICH, EIS},
            /* KLINGE  */ {PAPIER, PFLANZE, STICH, FEUER},
            /* PAPIER  */ {STEIN, BLITZ, WASSER, EIS},
            /* FEUER   */ {PAPIER, PFLANZE, EIS, STICH},
            /* WASSER  */ {STEIN, KLINGE, FEUER, STICH},
            /* BLITZ   */ {STEIN, KLINGE, FEUER, WASSER},
            /* PFLANZE */ {STEIN, PAPIER, WASSER, BLITZ},
            /* STICH   */ {PAPIER, BLITZ, PFLANZE, EIS},
            /* EIS     */ {KLINGE, WASSER, BLITZ, PFLANZE},
    };

    private static final boolean[][] BEATS = new boolean[COUNT][COUNT];

    static {
        for (int a = 0; a < COUNT; a++) for (int b : BEATS_LIST[a]) BEATS[a][b] = true;
    }

    public static boolean beats(int a, int b) {
        return BEATS[a][b];
    }

    /** Types that type {@code t} beats. */
    public static int[] beatsList(int t) {
        return BEATS_LIST[t];
    }

    /** Types that beat type {@code t}. */
    public static int[] losesList(int t) {
        int[] out = new int[4];
        int n = 0;
        for (int a = 0; a < COUNT; a++) if (BEATS[a][t]) out[n++] = a;
        return out;
    }

    public static String joinNames(int[] types) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(NAME[types[i]]);
        }
        return sb.toString();
    }

    private Element() {}
}
