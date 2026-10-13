package de.chr1ss0.ssp.core;

import static de.chr1ss0.ssp.core.Effect.*;
import static de.chr1ss0.ssp.core.WeaponDef.*;

/** One of the 100 bonus cards that can be equipped onto weapons. */
public final class BonusDef {
    // icon codes (see Art.bonusIcon)
    public static final int IC_BREAK = 0, IC_IMMUNE = 1, IC_GEM = 2, IC_ARMOR = 3, IC_CRIT = 4, IC_FANG = 5,
            IC_THORN = 6, IC_POISON = 7, IC_DOUBLE = 8, IC_CLOVER = 9, IC_MIRROR = 10, IC_DRAW = 11,
            IC_HEAL = 12, IC_RAGE = 13, IC_COMBO = 14, IC_BOOT = 15, IC_SCYTHE = 16, IC_DRILL = 17,
            IC_HEART = 18, IC_LEAF = 19, IC_STAR = 20, IC_EYE = 21, IC_PHOENIX = 22, IC_CAT = 23,
            IC_FOCUS = 24, IC_ROCKET = 25, IC_SHIELD = 26;

    public final int id;
    public final String name;
    public final String prefix;
    public final int rarity;
    public final int icon;
    public final int color;
    public final Effect[] effects;

    private BonusDef(int id, String name, String prefix, int rarity, int icon, int color, Effect[] effects) {
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.rarity = rarity;
        this.icon = icon;
        this.color = color;
        this.effects = effects;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < effects.length; i++) {
            if (i > 0) sb.append("\n");
            sb.append(effects[i].describe());
        }
        return sb.toString();
    }

    public String upper() {
        return name.toUpperCase(java.util.Locale.GERMAN);
    }

    /** Type the card relates to (break/immune cards), else -1. */
    public int relatedType() {
        for (Effect e : effects) if (e.kind == BREAK || e.kind == IMMUNE) return e.value;
        return -1;
    }

    public static final BonusDef[] ALL;
    private static int n = 0;

    private static BonusDef b(String name, String prefix, int rarity, int icon, int color, Effect... fx) {
        return new BonusDef(n++, name, prefix, rarity, icon, color, fx);
    }

    private static BonusDef b(String name, int rarity, int icon, int color, Effect... fx) {
        return b(name, name, rarity, icon, color, fx);
    }

    static {
        int[] tc = Element.COLOR;
        ALL = new BonusDef[]{
                // ---- Brecher (beat an extra type)
                b("Steinbrecher", SELTEN, IC_BREAK, tc[0], Effect.of(BREAK, Element.STEIN)),
                b("Klingenbrecher", SELTEN, IC_BREAK, tc[1], Effect.of(BREAK, Element.KLINGE)),
                b("Papierreißer", SELTEN, IC_BREAK, tc[2], Effect.of(BREAK, Element.PAPIER)),
                b("Flammenbändiger", SELTEN, IC_BREAK, tc[3], Effect.of(BREAK, Element.FEUER)),
                b("Dammbauer", SELTEN, IC_BREAK, tc[4], Effect.of(BREAK, Element.WASSER)),
                b("Blitzableiter", SELTEN, IC_BREAK, tc[5], Effect.of(BREAK, Element.BLITZ)),
                b("Unkrautjäter", SELTEN, IC_BREAK, tc[6], Effect.of(BREAK, Element.PFLANZE)),
                b("Spitzenbrecher", SELTEN, IC_BREAK, tc[7], Effect.of(BREAK, Element.STICH)),
                b("Eisbrecher", SELTEN, IC_BREAK, tc[8], Effect.of(BREAK, Element.EIS)),
                // ---- Immunities (loss -> draw)
                b("Felsenfest", GEWOEHNLICH, IC_IMMUNE, tc[0], Effect.of(IMMUNE, Element.STEIN)),
                b("Schnittfest", GEWOEHNLICH, IC_IMMUNE, tc[1], Effect.of(IMMUNE, Element.KLINGE)),
                b("Seife", "Seifen", GEWOEHNLICH, IC_IMMUNE, tc[2], Effect.of(IMMUNE, Element.PAPIER)),
                b("Feuerfest", GEWOEHNLICH, IC_IMMUNE, tc[3], Effect.of(IMMUNE, Element.FEUER)),
                b("Wasserdicht", GEWOEHNLICH, IC_IMMUNE, tc[4], Effect.of(IMMUNE, Element.WASSER)),
                b("Gummi", GEWOEHNLICH, IC_IMMUNE, tc[5], Effect.of(IMMUNE, Element.BLITZ)),
                b("Rankenfest", GEWOEHNLICH, IC_IMMUNE, tc[6], Effect.of(IMMUNE, Element.PFLANZE)),
                b("Stichfest", GEWOEHNLICH, IC_IMMUNE, tc[7], Effect.of(IMMUNE, Element.STICH)),
                b("Thermo", GEWOEHNLICH, IC_IMMUNE, tc[8], Effect.of(IMMUNE, Element.EIS)),
                // ---- Materials
                b("Holz", GEWOEHNLICH, IC_GEM, 0xFFB07840, Effect.of(ATK, 2)),
                b("Kupfer", GEWOEHNLICH, IC_GEM, 0xFFD87840, Effect.of(ATK, 3)),
                b("Bronze", GEWOEHNLICH, IC_GEM, 0xFFC09040, Effect.of(ATK, 3), Effect.of(DEF, 1)),
                b("Eisen", GEWOEHNLICH, IC_GEM, 0xFF8890A0, Effect.of(ATK, 4)),
                b("Granit", GEWOEHNLICH, IC_GEM, 0xFF908888, Effect.of(ATK, 1), Effect.of(DEF, 3)),
                b("Stahl", SELTEN, IC_GEM, 0xFFA8B8C8, Effect.of(ATK, 5)),
                b("Silber", SELTEN, IC_GEM, 0xFFD0D8E8, Effect.of(ATK, 4), Effect.of(CRIT, 5)),
                b("Gold", SELTEN, IC_GEM, 0xFFF0C030, Effect.of(ATK, 6)),
                b("Kristall", SELTEN, IC_GEM, 0xFFA0E8F8, Effect.of(ATK, 3), Effect.of(CRIT, 15)),
                b("Bernstein", SELTEN, IC_GEM, 0xFFF0A020, Effect.of(ATK, 3), Effect.of(REGEN, 1)),
                b("Platin", EPISCH, IC_GEM, 0xFFE0E8F0, Effect.of(ATK, 7)),
                b("Titan", EPISCH, IC_GEM, 0xFF8898B0, Effect.of(ATK, 5), Effect.of(DEF, 3)),
                b("Mithril", EPISCH, IC_GEM, 0xFFB0D0F0, Effect.of(ATK, 6), Effect.of(DEF, 4)),
                b("Obsidian", EPISCH, IC_GEM, 0xFF483858, Effect.of(ATK, 7), Effect.of(CRIT, 10)),
                b("Diamant", LEGENDAER, IC_GEM, 0xFFC8F8FF, Effect.of(ATK, 8), Effect.of(DEF, 3)),
                b("Adamant", LEGENDAER, IC_GEM, 0xFF70E0A0, Effect.of(ATK, 11)),
                // ---- Defence
                b("Leder", GEWOEHNLICH, IC_ARMOR, 0xFFA06838, Effect.of(DEF, 2)),
                b("Ritter", SELTEN, IC_ARMOR, 0xFFB0B8C8, Effect.of(DEF, 4)),
                b("Bollwerk", SELTEN, IC_ARMOR, 0xFF707880, Effect.of(DEF, 6), Effect.of(ATK, -2)),
                b("Panzer", EPISCH, IC_ARMOR, 0xFF607048, Effect.of(DEF, 5), Effect.of(MAXHP, 5)),
                b("Schild", SELTEN, IC_SHIELD, 0xFF4880E0, Effect.of(SHIELD, 1)),
                b("Aegis", LEGENDAER, IC_SHIELD, 0xFFF0C030, Effect.of(SHIELD, 2), Effect.of(DEF, 2)),
                // ---- Crit
                b("Adler", GEWOEHNLICH, IC_CRIT, 0xFFC08040, Effect.of(CRIT, 10)),
                b("Präzision", "Präzisions", SELTEN, IC_CRIT, 0xFFE04040, Effect.of(CRIT, 15)),
                b("Falke", "Falken", SELTEN, IC_CRIT, 0xFF906040, Effect.of(CRIT, 20)),
                b("Ninja", EPISCH, IC_CRIT, 0xFF404058, Effect.of(CRIT, 25), Effect.of(ATK, 1)),
                b("Assassine", "Assassinen", LEGENDAER, IC_CRIT, 0xFF802040, Effect.of(CRIT, 35)),
                // ---- Lifesteal
                b("Mücke", "Mücken", GEWOEHNLICH, IC_FANG, 0xFFC04848, Effect.of(LIFESTEAL, 15)),
                b("Egel", SELTEN, IC_FANG, 0xFF805050, Effect.of(LIFESTEAL, 25)),
                b("Vampir", EPISCH, IC_FANG, 0xFFB01830, Effect.of(LIFESTEAL, 40)),
                b("Dracula", LEGENDAER, IC_FANG, 0xFF700818, Effect.of(LIFESTEAL, 60)),
                // ---- Thorns
                b("Igel", GEWOEHNLICH, IC_THORN, 0xFF906848, Effect.of(THORNS, 20)),
                b("Dornen", SELTEN, IC_THORN, 0xFF408030, Effect.of(THORNS, 35)),
                b("Stachel", EPISCH, IC_THORN, 0xFF606870, Effect.of(THORNS, 50), Effect.of(DEF, 1)),
                // ---- Poison
                b("Gift", GEWOEHNLICH, IC_POISON, 0xFF80C040, Effect.of(POISON, 2, 3)),
                b("Skorpion", SELTEN, IC_POISON, 0xFFC08030, Effect.of(POISON, 3, 3)),
                b("Kobra", EPISCH, IC_POISON, 0xFF60A030, Effect.of(POISON, 4, 4)),
                b("Basilisk", LEGENDAER, IC_POISON, 0xFF208060, Effect.of(POISON, 6, 4)),
                // ---- Double strike
                b("Zwilling", "Zwillings", SELTEN, IC_DOUBLE, 0xFF70A0E0, Effect.of(DOUBLE, 20)),
                b("Turbo", SELTEN, IC_DOUBLE, 0xFFF06030, Effect.of(DOUBLE, 15), Effect.of(ATK, 2)),
                b("Hydra", EPISCH, IC_DOUBLE, 0xFF30A080, Effect.of(DOUBLE, 35)),
                // ---- Luck (draw -> win)
                b("Glück", "Glücks", SELTEN, IC_CLOVER, 0xFF50C050, Effect.of(LUCKY, 25)),
                b("Kleeblatt", EPISCH, IC_CLOVER, 0xFF30A030, Effect.of(LUCKY, 40)),
                b("Joker", LEGENDAER, IC_CLOVER, 0xFFA040E0, Effect.of(LUCKY, 50), Effect.of(CRIT, 10)),
                // ---- Mirror (loss -> draw)
                b("Spiegel", SELTEN, IC_MIRROR, 0xFFA0D8F0, Effect.of(MIRROR, 20)),
                b("Kaleidoskop", EPISCH, IC_MIRROR, 0xFFE070C0, Effect.of(MIRROR, 35)),
                // ---- Draw damage
                b("Funken", GEWOEHNLICH, IC_DRAW, 0xFFF0B030, Effect.of(DRAW_DMG, 3)),
                b("Echo", SELTEN, IC_DRAW, 0xFF8080E0, Effect.of(DRAW_DMG, 6)),
                b("Donnerhall", EPISCH, IC_DRAW, 0xFFE0C020, Effect.of(DRAW_DMG, 10)),
                // ---- Draw heal
                b("Zen", GEWOEHNLICH, IC_HEAL, 0xFF80C0A0, Effect.of(DRAW_HEAL, 5)),
                b("Mönch", "Mönchs", SELTEN, IC_HEAL, 0xFFE09030, Effect.of(DRAW_HEAL, 10)),
                // ---- Win heal
                b("Kräuter", GEWOEHNLICH, IC_LEAF, 0xFF60B040, Effect.of(WIN_HEAL, 3)),
                b("Sanitäter", SELTEN, IC_HEAL, 0xFFE04040, Effect.of(WIN_HEAL, 6)),
                b("Engel", "Engels", EPISCH, IC_HEAL, 0xFFF8E070, Effect.of(WIN_HEAL, 10)),
                // ---- Rage
                b("Wut", SELTEN, IC_RAGE, 0xFFE03020, Effect.of(RAGE, 50)),
                b("Berserker", EPISCH, IC_RAGE, 0xFFA01010, Effect.of(RAGE, 100), Effect.of(ATK, 2)),
                // ---- Combo
                b("Combo", SELTEN, IC_COMBO, 0xFFF09020, Effect.of(COMBO, 3)),
                b("Lawine", "Lawinen", EPISCH, IC_COMBO, 0xFFC0E8F8, Effect.of(COMBO, 5)),
                // ---- First strike
                b("Sprinter", GEWOEHNLICH, IC_BOOT, 0xFF40A0E0, Effect.of(FIRST, 100)),
                b("Überraschung", "Überraschungs", SELTEN, IC_BOOT, 0xFFE060A0, Effect.of(FIRST, 200)),
                // ---- Execute
                b("Henker", EPISCH, IC_SCYTHE, 0xFF505060, Effect.of(EXECUTE, 15)),
                b("Sensenmann", LEGENDAER, IC_SCYTHE, 0xFF303040, Effect.of(EXECUTE, 25)),
                // ---- Pierce
                b("Bohrer", SELTEN, IC_DRILL, 0xFFA0A8B0, Effect.of(PIERCE)),
                b("Laser", EPISCH, IC_DRILL, 0xFFF03050, Effect.of(PIERCE), Effect.of(ATK, 3)),
                b("Plasma", EPISCH, IC_DRILL, 0xFFB060F0, Effect.of(ANTI_IMMUNE), Effect.of(ATK, 3)),
                // ---- Max HP
                b("Herz", GEWOEHNLICH, IC_HEART, 0xFFF06080, Effect.of(MAXHP, 10)),
                b("Löwenherz", SELTEN, IC_HEART, 0xFFF0A030, Effect.of(MAXHP, 20)),
                b("Titanenherz", EPISCH, IC_HEART, 0xFF9098B0, Effect.of(MAXHP, 35)),
                // ---- Regen
                b("Moos", GEWOEHNLICH, IC_LEAF, 0xFF70A040, Effect.of(REGEN, 1)),
                b("Troll", EPISCH, IC_LEAF, 0xFF608050, Effect.of(REGEN, 3)),
                // ---- Card luck
                b("Glitzer", SELTEN, IC_STAR, 0xFFF0D040, Effect.of(LUCK, 1)),
                b("Sternschnuppe", "Sternschnuppen", EPISCH, IC_STAR, 0xFFF8E880, Effect.of(LUCK, 2)),
                // ---- Seer
                b("Hellseher", SELTEN, IC_EYE, 0xFF9060D0, Effect.of(SEER, 20)),
                b("Orakel", LEGENDAER, IC_EYE, 0xFF6030B0, Effect.of(SEER, 40)),
                // ---- Survival
                b("Phönix", LEGENDAER, IC_PHOENIX, 0xFFF06020, Effect.of(PHOENIX)),
                b("Katze", "Katzen", EPISCH, IC_CAT, 0xFF404048, Effect.of(CAT, 50)),
                // ---- Focus
                b("Fokus", GEWOEHNLICH, IC_FOCUS, 0xFF6080D0, Effect.of(FOCUS, 4)),
                b("Meditation", "Meditations", SELTEN, IC_FOCUS, 0xFF8060C0, Effect.of(FOCUS, 8)),
                // ---- Misc
                b("Glaskanone", "Glaskanonen", SELTEN, IC_ROCKET, 0xFFA0E0F0, Effect.of(ATK, 12), Effect.of(DEF, -4)),
                b("Rakete", "Raketen", EPISCH, IC_ROCKET, 0xFFE04030, Effect.of(ATK, 8), Effect.of(CRIT, 10)),
                b("Quanten", LEGENDAER, IC_STAR, 0xFF40D0E0, Effect.of(CRIT, 15), Effect.of(DOUBLE, 15), Effect.of(LUCKY, 15)),
                b("Regenbogen", EPISCH, IC_STAR, 0xFFF080C0, Effect.of(ATK, 3), Effect.of(DEF, 3), Effect.of(MAXHP, 10)),
        };
    }

    public static BonusDef get(int id) {
        return ALL[id];
    }
}
