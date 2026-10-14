package de.chr1ss0.ssp.core;

import static de.chr1ss0.ssp.core.Element.*;

/** Static definition of one of the 50 weapons. */
public final class WeaponDef {
    public static final int GEWOEHNLICH = 0, SELTEN = 1, EPISCH = 2, LEGENDAER = 3;
    public static final String[] RARITY_NAME = {"Gewöhnlich", "Selten", "Episch", "Legendär"};

    public final int id;
    public final String name;
    public final int type;
    public final int rarity;
    public final int atk, def;
    public final int slots;
    public final Effect[] innate;
    public final String desc;

    private WeaponDef(int id, String name, int type, int rarity, int atk, int def, int slots, String desc, Effect... innate) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.atk = atk;
        this.def = def;
        this.slots = slots;
        this.innate = innate;
        this.desc = desc;
    }

    public String upper() {
        return name.toUpperCase(java.util.Locale.GERMAN);
    }

    public boolean isClassic() {
        return id == STEIN_ID || id == SCHERE_ID || id == PAPIER_ID;
    }

    public static final int STEIN_ID = 0, SCHERE_ID = 6, PAPIER_ID = 12;
    public static final int[] CLASSICS = {STEIN_ID, SCHERE_ID, PAPIER_ID};
    /** Special weapons every player starts with (Fackel, Wasserpistole, Bambus). */
    public static final int[] STARTERS = {19, 24, 35};

    public static final WeaponDef[] ALL;

    private static int n = 0;

    private static WeaponDef w(String name, int type, int rarity, int atk, int def, String desc, Effect... innate) {
        int slots = rarity == LEGENDAER ? 3 : 2;
        return new WeaponDef(n++, name, type, rarity, atk, def, slots, desc, innate);
    }

    /** Stein, Schere, Papier: always available, unlimited use, no card slots. */
    private static WeaponDef classic(String name, int type, int atk, int def, String desc) {
        return new WeaponDef(n++, name, type, GEWOEHNLICH, atk, def, 0, desc);
    }

    static {
        ALL = new WeaponDef[]{
                // ---------------- STEIN
                classic("Stein", STEIN, 10, 2, "Der Klassiker. Hart, ehrlich, unkaputtbar."),
                w("Hammer", STEIN, GEWOEHNLICH, 12, 1, "Für jedes Problem ein Nagel."),
                w("Keule", STEIN, GEWOEHNLICH, 11, 2, "Modernste Steinzeit-Technologie."),
                w("Bowlingkugel", STEIN, SELTEN, 13, 2, "Strike! Rollt alles nieder.", Effect.of(Effect.CRIT, 15)),
                w("Amboss", STEIN, EPISCH, 16, 4, "Fällt am liebsten von ganz oben."),
                w("Meteorit", STEIN, LEGENDAER, 20, 4, "Ein Gruß aus den Tiefen des Alls.", Effect.of(Effect.CRIT, 15)),
                // ---------------- KLINGE
                classic("Schere", KLINGE, 10, 1, "Schnipp, schnapp! Der Klassiker."),
                w("Messer", KLINGE, GEWOEHNLICH, 11, 1, "Scharf genug für Butter und mehr."),
                w("Schwert", KLINGE, SELTEN, 14, 2, "Die Waffe echter Heldinnen und Helden."),
                w("Axt", KLINGE, SELTEN, 15, 1, "Holzfäller-Qualität."),
                w("Kettensäge", KLINGE, EPISCH, 17, 1, "Laut, gefährlich, effektiv.", Effect.of(Effect.DOUBLE, 20)),
                w("Katana", KLINGE, LEGENDAER, 18, 2, "Tausendfach gefaltet.", Effect.of(Effect.CRIT, 25)),
                // ---------------- PAPIER
                classic("Papier", PAPIER, 10, 2, "Der Klassiker. Wickelt Steine ein."),
                w("Zeitung", PAPIER, GEWOEHNLICH, 10, 3, "Druckfrisch und gut gerollt."),
                w("Klopapier", PAPIER, GEWOEHNLICH, 8, 4, "Weich, dreilagig, unterschätzt.", Effect.of(Effect.DRAW_HEAL, 3)),
                w("Netz", PAPIER, SELTEN, 12, 2, "Fängt alles, was zappelt."),
                w("Origami", PAPIER, EPISCH, 14, 2, "Ein Kranich bringt Glück.", Effect.of(Effect.LUCKY, 25)),
                w("Zauberbuch", PAPIER, LEGENDAER, 17, 3, "Uralte Sprüche saugen Lebenskraft.", Effect.of(Effect.LIFESTEAL, 25)),
                // ---------------- FEUER
                w("Streichholz", FEUER, GEWOEHNLICH, 9, 0, "Klein, aber zündend.", Effect.of(Effect.CRIT, 10)),
                w("Fackel", FEUER, GEWOEHNLICH, 11, 1, "Bringt Licht in jeden Kampf."),
                w("Feuerball", FEUER, SELTEN, 14, 0, "Heiß, rund, zielsicher."),
                w("Feuerwerk", FEUER, SELTEN, 12, 1, "Knallt gleich doppelt.", Effect.of(Effect.DOUBLE, 20)),
                w("Flammenwerfer", FEUER, EPISCH, 17, 2, "Brennt nach.", Effect.of(Effect.POISON, 2, 2)),
                w("Vulkan", FEUER, LEGENDAER, 20, 3, "Glühende Lava verbrennt lange.", Effect.of(Effect.POISON, 4, 3)),
                // ---------------- WASSER
                w("Wasserpistole", WASSER, GEWOEHNLICH, 10, 1, "Spritzig und erfrischend."),
                w("Eimer", WASSER, GEWOEHNLICH, 10, 3, "Ein Schwall eiskaltes Wasser."),
                w("Feuerlöscher", WASSER, SELTEN, 13, 2, "Der Albtraum jeder Flamme."),
                w("Dreizack", WASSER, EPISCH, 16, 2, "Herrscht über die Meere.", Effect.of(Effect.CRIT, 10)),
                w("Tsunami", WASSER, LEGENDAER, 20, 3, "Eine Welle, die alles mitreißt."),
                // ---------------- BLITZ
                w("Batterie", BLITZ, GEWOEHNLICH, 9, 2, "Voll aufgeladen."),
                w("Stromkabel", BLITZ, GEWOEHNLICH, 11, 1, "Nicht anfassen! Steht unter Strom."),
                w("Taser", BLITZ, SELTEN, 13, 1, "Bzzzzt!", Effect.of(Effect.CRIT, 10)),
                w("Tesla-Spule", BLITZ, EPISCH, 16, 2, "Erfinder-Genie in Spulenform."),
                w("Gewitterwolke", BLITZ, LEGENDAER, 19, 2, "Donnert immer zweimal.", Effect.of(Effect.DOUBLE, 25)),
                // ---------------- PFLANZE
                w("Kaktus", PFLANZE, GEWOEHNLICH, 10, 3, "Bitte nicht umarmen.", Effect.of(Effect.THORNS, 20)),
                w("Bambus", PFLANZE, GEWOEHNLICH, 11, 2, "Wächst schneller als du denkst."),
                w("Rose", PFLANZE, SELTEN, 12, 1, "Schön, aber mit Dornen.", Effect.of(Effect.THORNS, 30)),
                w("Venusfalle", PFLANZE, EPISCH, 15, 2, "Hat immer Hunger.", Effect.of(Effect.LIFESTEAL, 25)),
                w("Weltenbaum", PFLANZE, LEGENDAER, 18, 5, "Seine Wurzeln reichen ewig.", Effect.of(Effect.REGEN, 2)),
                // ---------------- STICH
                w("Nadel", STICH, GEWOEHNLICH, 9, 0, "Klein und gemein: trifft genau.", Effect.of(Effect.CRIT, 20)),
                w("Gabel", STICH, GEWOEHNLICH, 10, 1, "Vier Zinken, null Gnade."),
                w("Dartpfeil", STICH, GEWOEHNLICH, 10, 0, "Volle 180!", Effect.of(Effect.CRIT, 10)),
                w("Bogen", STICH, SELTEN, 13, 1, "Pfeil und Bogen aus Eibenholz."),
                w("Speer", STICH, SELTEN, 14, 2, "Hält Gegner auf Abstand."),
                w("Lanze", STICH, EPISCH, 16, 3, "Für edle Turniere."),
                // ---------------- EIS
                w("Schneeball", EIS, GEWOEHNLICH, 9, 1, "Mit extra viel Wumms gerollt."),
                w("Eiszapfen", EIS, GEWOEHNLICH, 11, 1, "Spitz und bitterkalt."),
                w("Eiskristall", EIS, SELTEN, 13, 2, "Funkelt eisig.", Effect.of(Effect.CRIT, 10)),
                w("Frostkanone", EIS, EPISCH, 16, 2, "Schießt pure Kälte."),
                w("Gletscher", EIS, LEGENDAER, 19, 5, "Langsam, aber unaufhaltsam."),
        };
    }

    public static WeaponDef get(int id) {
        return ALL[id];
    }

    /** Ids of all weapons of a type. */
    public static int[] ofType(int type) {
        int c = 0;
        for (WeaponDef d : ALL) if (d.type == type) c++;
        int[] out = new int[c];
        c = 0;
        for (WeaponDef d : ALL) if (d.type == type) out[c++] = d.id;
        return out;
    }
}
