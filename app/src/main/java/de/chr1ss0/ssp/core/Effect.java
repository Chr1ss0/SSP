package de.chr1ss0.ssp.core;

/** A single effect component of a bonus card or of a weapon's innate ability. */
public final class Effect {
    public static final int BREAK = 0;       // type: beats type
    public static final int IMMUNE = 1;      // type: loss -> draw
    public static final int ATK = 2;         // value
    public static final int DEF = 3;         // value
    public static final int CRIT = 4;        // %
    public static final int LIFESTEAL = 5;   // %
    public static final int THORNS = 6;      // %
    public static final int POISON = 7;      // value dmg, value2 rounds
    public static final int DOUBLE = 8;      // %
    public static final int LUCKY = 9;       // % draw -> win
    public static final int MIRROR = 10;     // % loss -> draw
    public static final int DRAW_DMG = 11;   // value
    public static final int DRAW_HEAL = 12;  // value
    public static final int WIN_HEAL = 13;   // value
    public static final int SHIELD = 14;     // value (hits per battle)
    public static final int RAGE = 15;       // %
    public static final int COMBO = 16;      // value per streak
    public static final int FIRST = 17;      // %
    public static final int EXECUTE = 18;    // % hp threshold
    public static final int PIERCE = 19;
    public static final int ANTI_IMMUNE = 20;
    public static final int MAXHP = 21;      // passive
    public static final int REGEN = 22;      // passive
    public static final int LUCK = 23;       // passive
    public static final int SEER = 24;       // passive %
    public static final int PHOENIX = 25;    // passive
    public static final int CAT = 26;        // passive %
    public static final int FOCUS = 27;      // value

    public final int kind, value, value2;

    public Effect(int kind, int value, int value2) {
        this.kind = kind;
        this.value = value;
        this.value2 = value2;
    }

    public static Effect of(int kind, int value) { return new Effect(kind, value, 0); }
    public static Effect of(int kind) { return new Effect(kind, 0, 0); }
    public static Effect of(int kind, int value, int value2) { return new Effect(kind, value, value2); }

    public boolean isPassive() {
        return kind == MAXHP || kind == REGEN || kind == LUCK || kind == SEER || kind == PHOENIX || kind == CAT;
    }

    private static String signed(int v) { return v >= 0 ? "+" + v : String.valueOf(v); }

    public String describe() {
        switch (kind) {
            case BREAK: return "Besiegt auch " + Element.NAME[value];
            case IMMUNE: return "Immun gegen " + Element.NAME[value];
            case ATK: return signed(value) + " Angriff";
            case DEF: return signed(value) + " Abwehr";
            case CRIT: return value + "% Volltreffer";
            case LIFESTEAL: return value + "% Lebensraub";
            case THORNS: return value + "% Rückschaden";
            case POISON: return "Gift: " + value + " KP, " + value2 + " Runden";
            case DOUBLE: return value + "% Doppelschlag";
            case LUCKY: return value + "%: Patt wird Sieg";
            case MIRROR: return value + "%: Niederlage wird Patt";
            case DRAW_DMG: return "Patt: " + value + " Schaden";
            case DRAW_HEAL: return "Patt: heilt " + value + " KP";
            case WIN_HEAL: return "Sieg: heilt " + value + " KP";
            case SHIELD: return "Blockt " + value + (value == 1 ? " Treffer" : " Treffer") + "/Kampf";
            case RAGE: return "+" + value + "% Schaden bei <30% KP";
            case COMBO: return "+" + value + " Schaden je Siegesserie";
            case FIRST: return "+" + value + "% Schaden in Runde 1";
            case EXECUTE: return "Erledigt Gegner unter " + value + "% KP";
            case PIERCE: return "Ignoriert Abwehr";
            case ANTI_IMMUNE: return "Durchbricht Immunität";
            case MAXHP: return "+" + value + " Max-KP (passiv)";
            case REGEN: return "+" + value + " KP pro Runde (passiv)";
            case LUCK: return "Bessere Karten (passiv)";
            case SEER: return value + "%: Sieht Gegnerwahl (passiv)";
            case PHOENIX: return "Überlebt 1x/Kampf mit 1 KP (passiv)";
            case CAT: return value + "%: Überlebt K.O. (passiv)";
            case FOCUS: return "Patt: +" + value + " Schaden für nächsten Sieg";
            default: return "?";
        }
    }
}
