package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.Random;

import static de.chr1ss0.ssp.core.Element.*;

/** Trainer classes, arena bosses and enemy generation per stage. */
public final class Enemies {

    private static final String[] CLASS_NAME = {
            "Schüler", "Bauarbeiter", "Gärtnerin", "Feuerspucker", "Elektriker", "Schneiderin", "Bücherwurm",
            "Koch", "Eisläuferin", "Schwimmer", "Pfadfinderin", "Bergsteiger", "Rocker", "Ninja", "Zauberer",
            "Feuerwehrfrau"
    };
    private static final int[][] CLASS_TYPES = {
            {STEIN, KLINGE, PAPIER}, {STEIN, KLINGE}, {PFLANZE, KLINGE, WASSER}, {FEUER, STEIN},
            {BLITZ, KLINGE}, {STICH, KLINGE, PAPIER}, {PAPIER, BLITZ}, {KLINGE, STICH, FEUER},
            {EIS, WASSER}, {WASSER, EIS}, {PFLANZE, FEUER, STICH}, {STEIN, EIS}, {BLITZ, STEIN},
            {KLINGE, STICH}, {STEIN, KLINGE, PAPIER, FEUER, WASSER, BLITZ, PFLANZE, STICH, EIS},
            {WASSER, KLINGE}
    };
    private static final int[] CLASS_HAT = {
            Look.HAT_CAP, Look.HAT_HELMET, Look.HAT_BAND, Look.HAT_NONE, Look.HAT_CAP, Look.HAT_NONE, Look.HAT_NONE,
            Look.HAT_BEANIE, Look.HAT_BEANIE, Look.HAT_NONE, Look.HAT_CAP, Look.HAT_BEANIE, Look.HAT_NONE,
            Look.HAT_BAND, Look.HAT_WIZARD, Look.HAT_HELMET
    };
    /** Preferred creature species / crest / extra per trainer class (-1 = random). */
    private static final int[] CLASS_SPECIES = {-1, Look.BAER, Look.HASE, Look.DRACHE, Look.FUCHS, Look.KATZE, Look.VOGEL,
            Look.BAER, Look.KATZE, Look.FROSCH, Look.FUCHS, -1, -1, Look.KATZE, Look.KNUDDEL, Look.DRACHE};
    private static final int[] CLASS_CREST = {-1, Look.CREST_HORNS, Look.CREST_LEAF, Look.CREST_FLAME, Look.CREST_ANTENNA, -1, -1,
            -1, Look.CREST_CRYSTAL, -1, -1, Look.CREST_HORNS, -1, -1, Look.CREST_GEM, Look.CREST_FLAME};
    private static final int[] CLASS_EXTRA = {-1, -1, -1, -1, -1, Look.EX_BOW, Look.EX_GLASSES, Look.EX_SCARF, Look.EX_SCARF, -1, -1,
            Look.EX_SCARF, Look.EX_SHADES, Look.EX_SHADES, -1, -1};

    private static final String[] NAMES = {
            "KEVIN", "JANA", "LUKAS", "MIA", "FINN", "LEA", "JONAS", "EMMA", "PAUL", "LINA", "BEN", "SOPHIE",
            "NOAH", "HANNA", "TIM", "LARA", "MAX", "NELE", "TOM", "CLARA", "OLE", "IDA", "EMIL", "GRETA",
            "KARL", "FRIDA", "MORITZ", "PAULA", "JAN", "ELLA", "ALI", "AYLA", "LEON", "MILA", "NIKO", "ZOE"
    };

    public static final String[] ARENA = {
            "Kieselberg", "Seeufer", "Kraftwerk", "Dschungel", "Vulkanhöhle",
            "Gletscherpass", "Nadelwald", "Klingenturm", "Bibliothek", "Champions-Arena"
    };
    private static final String[] BOSS_NAME = {"KURT", "PIA", "VIKTOR", "FLORA", "FIONA", "FRANZ", "NORA", "KLAUS", "OSKAR", "SCHNUCK"};
    private static final int[] BOSS_TYPE = {STEIN, WASSER, BLITZ, PFLANZE, FEUER, EIS, STICH, KLINGE, PAPIER, -1};
    private static final Look[] BOSS_LOOK = {
            new Look(Look.BAER, 11, 0, Look.PAT_NONE, 0, Look.CREST_HORNS, Look.TAIL_NORMAL, Look.HAT_HELMET, Look.EX_NONE),
            new Look(Look.FROSCH, 1, 5, Look.PAT_SPOTS, 1, Look.CREST_NONE, Look.TAIL_NONE, Look.HAT_NONE, Look.EX_SCARF),
            new Look(Look.FUCHS, 3, 1, Look.PAT_TIPS, 3, Look.CREST_ANTENNA, Look.TAIL_BOLT, Look.HAT_NONE, Look.EX_SHADES),
            new Look(Look.HASE, 2, 4, Look.PAT_NONE, 2, Look.CREST_LEAF, Look.TAIL_LEAF, Look.HAT_BAND, Look.EX_NONE),
            new Look(Look.DRACHE, 0, 2, Look.PAT_NONE, 5, Look.CREST_FLAME, Look.TAIL_FLAME, Look.HAT_NONE, Look.EX_NONE),
            new Look(Look.KATZE, 9, 1, Look.PAT_STRIPES, 1, Look.CREST_CRYSTAL, Look.TAIL_NORMAL, Look.HAT_BEANIE, Look.EX_SCARF),
            new Look(Look.VOGEL, 4, 6, Look.PAT_TIPS, 4, Look.CREST_NONE, Look.TAIL_NORMAL, Look.HAT_NONE, Look.EX_GLASSES),
            new Look(Look.KATZE, 6, 1, Look.PAT_STRIPES, 5, Look.CREST_HORNS, Look.TAIL_NORMAL, Look.HAT_NONE, Look.EX_SHADES),
            new Look(Look.KNUDDEL, 7, 0, Look.PAT_SPOTS, 0, Look.CREST_NONE, Look.TAIL_NONE, Look.HAT_WIZARD, Look.EX_GLASSES),
            new Look(Look.DRACHE, 4, 2, Look.PAT_TIPS, 4, Look.CREST_GEM, Look.TAIL_NORMAL, Look.HAT_CROWN, Look.EX_SCARF),
    };
    private static final String[] BOSS_CREATURE = {"FELSBÄR", "QUAPPO", "VOLTFUX", "BLATTI", "GLUTDRA", "EISKATZ", "STACHEL",
            "SCHNEIDO", "BÜCHO", "MAJESTO"};

    public static boolean isBoss(int stage) {
        return stage % 5 == 0;
    }

    public static int arenaIndex(int stage) {
        return ((stage - 1) / 5) % ARENA.length;
    }

    public static String arenaName(int stage) {
        int loop = (stage - 1) / 50;
        return ARENA[arenaIndex(stage)] + (loop > 0 ? " " + roman(loop + 1) : "");
    }

    private static String roman(int n) {
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n < r.length ? r[n] : String.valueOf(n);
    }

    public static String bossName(int stage) {
        return BOSS_NAME[arenaIndex(stage)];
    }

    public static Look bossLook(int stage) {
        return BOSS_LOOK[arenaIndex(stage)];
    }

    /**
     * Deterministic enemy for a stage of a run. It carries as many weapons as
     * the player: Stein, Schere, Papier plus {@code weaponCount - 3} specials.
     */
    public static Fighter create(long runSeed, int stage, int weaponCount) {
        Random r = new Random(runSeed * 31 + stage * 7919L);
        boolean boss = isBoss(stage);
        int arena = arenaIndex(stage);
        Fighter f;
        int[] types;
        if (boss) {
            boolean champ = BOSS_TYPE[arena] < 0;
            f = new Fighter(BOSS_CREATURE[arena], false, BOSS_LOOK[arena].copy());
            f.title = champ ? "GROSSMEISTER" : "ARENALEITER";
            f.trainer = BOSS_NAME[arena];
            if (champ) {
                types = new int[]{STEIN, KLINGE, PAPIER, FEUER, WASSER, BLITZ, PFLANZE, STICH, EIS};
            } else {
                types = new int[]{BOSS_TYPE[arena]};
            }
        } else {
            int cls;
            if (stage <= 2) cls = 0;
            else {
                int at = BOSS_TYPE[arena];
                cls = 1 + r.nextInt(CLASS_NAME.length - 1);
                if (at >= 0 && r.nextBoolean()) {
                    for (int tries = 0; tries < 20; tries++) {
                        int c = 1 + r.nextInt(CLASS_NAME.length - 1);
                        if (contains(CLASS_TYPES[c], at)) { cls = c; break; }
                    }
                }
            }
            Look l = Look.random(r);
            l.hat = CLASS_HAT[cls] != Look.HAT_NONE && r.nextInt(3) > 0 ? CLASS_HAT[cls] : (r.nextInt(4) == 0 ? l.hat : Look.HAT_NONE);
            if (CLASS_SPECIES[cls] >= 0 && r.nextInt(3) > 0) l.species = CLASS_SPECIES[cls];
            if (CLASS_CREST[cls] >= 0 && r.nextInt(3) > 0) l.crest = CLASS_CREST[cls];
            if (CLASS_EXTRA[cls] >= 0 && r.nextInt(2) == 0) l.extra = CLASS_EXTRA[cls];
            String trainer = NAMES[r.nextInt(NAMES.length)];
            f = new Fighter(Look.creatureName(l.species, r), false, l);
            f.title = CLASS_NAME[cls].toUpperCase(java.util.Locale.GERMAN);
            f.trainer = trainer;
            types = CLASS_TYPES[cls];
        }

        // same structure as the player: Stein, Schere, Papier + the same number of specials
        for (int id : WeaponDef.CLASSICS) addWeapon(f, id);
        int count = Math.max(3, Math.min(Run.MAX_WEAPONS, weaponCount));
        int maxRarity = stage < 4 ? 0 : (stage < 10 ? 1 : (stage < 20 ? 2 : 3));
        if (boss) maxRarity = Math.min(3, maxRarity + 1);

        ArrayList<Integer> pool = new ArrayList<Integer>();
        for (WeaponDef d : WeaponDef.ALL) {
            if (d.isClassic() || d.rarity > maxRarity || !contains(types, d.type)) continue;
            pool.add(d.id);
        }
        // boss cover weapon: something that beats the counters of the main type
        if (boss && types.length == 1 && count > 4) {
            int[] counters = Element.losesList(types[0]);
            int cover = counters[r.nextInt(counters.length)];
            int[] coverTypes = Element.losesList(cover);
            int ct = coverTypes[r.nextInt(coverTypes.length)];
            int[] ids = WeaponDef.ofType(ct);
            for (int tries = 0; tries < 10; tries++) {
                int id = ids[r.nextInt(Math.min(ids.length, 3))];
                if (!WeaponDef.get(id).isClassic()) { addWeapon(f, id); break; }
            }
        }
        while (f.weapons.size() < count && !pool.isEmpty()) {
            int idx = pickWeighted(pool, r);
            int id = pool.remove(idx);
            if (!f.hasWeapon(id)) addWeapon(f, id);
        }
        // fallback (pool exhausted)
        while (f.weapons.size() < count) {
            int id = r.nextInt(WeaponDef.ALL.length);
            if (!f.hasWeapon(id) && !WeaponDef.get(id).isClassic() && WeaponDef.get(id).rarity <= maxRarity) addWeapon(f, id);
        }

        // bonus cards
        int cards = f.weapons.size() > 3 ? Math.max(0, (stage - 1) / 3) + (boss ? 1 : 0) : 0;
        for (int i = 0; i < cards; i++) {
            BonusDef b = Cards.randomCard(r, Math.min(2, stage / 12));
            if (b.effects[0].isPassive() && b.effects[0].kind != Effect.MAXHP && b.effects[0].kind != Effect.REGEN) continue;
            WeaponInst w = f.weapons.get(3 + r.nextInt(f.weapons.size() - 3));
            int slot = w.freeSlot();
            if (slot >= 0) w.slots[slot] = b.id;
        }

        f.level = stage;
        int hp = 15 + 4 * stage;
        if (boss) hp = hp * 3 / 2;
        f.baseMaxHp = hp;
        f.maxHp = 0;
        f.recalcMaxHp();
        f.hp = f.maxHp;
        // gentle start (the run begins with Stein, Schere, Papier only), then steadily harder
        f.dmgPct = Math.min(400, 75 + 5 * (stage - 1));
        return f;
    }

    private static void addWeapon(Fighter f, int id) {
        if (!f.hasWeapon(id)) f.weapons.add(new WeaponInst(id));
    }

    private static int pickWeighted(ArrayList<Integer> pool, Random r) {
        int total = 0;
        for (int id : pool) total += weight(WeaponDef.get(id).rarity);
        int x = r.nextInt(total);
        for (int i = 0; i < pool.size(); i++) {
            x -= weight(WeaponDef.get(pool.get(i)).rarity);
            if (x < 0) return i;
        }
        return pool.size() - 1;
    }

    private static int weight(int rarity) {
        switch (rarity) {
            case 0: return 10;
            case 1: return 6;
            case 2: return 3;
            default: return 1;
        }
    }

    static boolean contains(int[] a, int v) {
        for (int x : a) if (x == v) return true;
        return false;
    }

    private Enemies() {}
}
