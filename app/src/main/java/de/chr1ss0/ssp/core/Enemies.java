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
            new Look(1, 1, 2, 11, 10, Look.HAT_HELMET, 0),
            new Look(3, 5, 1, 9, 1, Look.HAT_NONE, 0),
            new Look(4, 2, 4, 3, 6, Look.HAT_NONE, 2),
            new Look(2, 6, 3, 2, 10, Look.HAT_BAND, 0),
            new Look(1, 3, 1, 0, 5, Look.HAT_NONE, 0),
            new Look(0, 4, 0, 7, 9, Look.HAT_BEANIE, 0),
            new Look(7, 7, 2, 8, 4, Look.HAT_NONE, 1),
            new Look(5, 0, 5, 6, 11, Look.HAT_NONE, 3),
            new Look(6, 4, 1, 7, 10, Look.HAT_WIZARD, 4),
            new Look(2, 4, 3, 4, 6, Look.HAT_CROWN, 0),
    };

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

    /** Deterministic enemy for a stage of a run. */
    public static Fighter create(long runSeed, int stage) {
        Random r = new Random(runSeed * 31 + stage * 7919L);
        boolean boss = isBoss(stage);
        int arena = arenaIndex(stage);
        Fighter f;
        int[] types;
        if (boss) {
            boolean champ = BOSS_TYPE[arena] < 0;
            f = new Fighter(BOSS_NAME[arena], false, BOSS_LOOK[arena].copy());
            f.title = champ ? "GROSSMEISTER" : "ARENALEITER";
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
            f = new Fighter(NAMES[r.nextInt(NAMES.length)], false, l);
            f.title = CLASS_NAME[cls].toUpperCase(java.util.Locale.GERMAN);
            types = CLASS_TYPES[cls];
        }

        int count = stage <= 8 ? 3 : (stage <= 18 ? 4 : (stage <= 30 ? 5 : 6));
        if (boss) count = Math.min(6, count + 1);
        int maxRarity = stage < 4 ? 0 : (stage < 10 ? 1 : (stage < 20 ? 2 : 3));
        if (boss) maxRarity = Math.min(3, maxRarity + 1);

        ArrayList<Integer> pool = new ArrayList<Integer>();
        for (WeaponDef d : WeaponDef.ALL) {
            if (d.rarity > maxRarity || !contains(types, d.type)) continue;
            if (stage <= 2 && !boss && d.id != WeaponDef.STEIN_ID && d.id != WeaponDef.SCHERE_ID && d.id != WeaponDef.PAPIER_ID) continue;
            pool.add(d.id);
        }
        // boss cover weapons: something that beats the counters of the main type
        if (boss && types.length == 1) {
            int[] counters = Element.losesList(types[0]);
            int cover = counters[r.nextInt(counters.length)];
            int[] coverTypes = Element.losesList(cover);
            int ct = coverTypes[r.nextInt(coverTypes.length)];
            int[] ids = WeaponDef.ofType(ct);
            addWeapon(f, ids[r.nextInt(Math.min(ids.length, 3))]);
        }
        while (f.weapons.size() < count && !pool.isEmpty()) {
            int idx = pickWeighted(pool, r);
            int id = pool.remove(idx);
            if (!f.hasWeapon(id)) addWeapon(f, id);
        }
        // fallback (pool exhausted)
        while (f.weapons.size() < count) {
            int id = r.nextInt(WeaponDef.ALL.length);
            if (!f.hasWeapon(id) && WeaponDef.get(id).rarity <= maxRarity) addWeapon(f, id);
        }

        // bonus cards
        int cards = Math.max(0, (stage - 1) / 2) + (boss ? 2 : 0);
        for (int i = 0; i < cards; i++) {
            BonusDef b = Cards.randomCard(r, Math.min(2, stage / 12));
            if (b.effects[0].isPassive() && b.effects[0].kind != Effect.MAXHP && b.effects[0].kind != Effect.REGEN) continue;
            WeaponInst w = f.weapons.get(r.nextInt(f.weapons.size()));
            int slot = w.freeSlot();
            if (slot >= 0) w.slots[slot] = b.id;
        }

        f.level = stage;
        int hp = 20 + 7 * stage;
        if (boss) hp = hp * 8 / 5;
        f.baseMaxHp = hp;
        f.maxHp = 0;
        f.recalcMaxHp();
        f.hp = f.maxHp;
        f.dmgPct = Math.min(500, 100 + 5 * (stage - 1));
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
