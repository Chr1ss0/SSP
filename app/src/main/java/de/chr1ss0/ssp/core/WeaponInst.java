package de.chr1ss0.ssp.core;

import java.util.Locale;

/** A weapon owned by a fighter, with its equipped bonus cards. */
public final class WeaponInst {
    public final WeaponDef def;
    public final int[] slots;
    /** Battle-temporary: shield hits used. */
    public int shieldUsed;

    public WeaponInst(int defId) {
        this.def = WeaponDef.get(defId);
        this.slots = new int[def.slots];
        java.util.Arrays.fill(slots, -1);
    }

    public int equipped() {
        int n = 0;
        for (int s : slots) if (s >= 0) n++;
        return n;
    }

    public int freeSlot() {
        for (int i = 0; i < slots.length; i++) if (slots[i] < 0) return i;
        return -1;
    }

    public int countCard(int bonusId) {
        int n = 0;
        for (int s : slots) if (s == bonusId) n++;
        return n;
    }

    public BonusDef mainBonus() {
        BonusDef best = null;
        for (int s : slots) {
            if (s < 0) continue;
            BonusDef b = BonusDef.get(s);
            if (best == null || b.rarity > best.rarity) best = b;
        }
        return best;
    }

    public String displayName() {
        BonusDef b = mainBonus();
        if (b == null) return def.name;
        String n = b.prefix + "-" + def.name;
        int extra = equipped() - 1;
        if (extra > 0) n += " +" + extra;
        return n;
    }

    public String upper() {
        return displayName().toUpperCase(Locale.GERMAN);
    }

    /** Stats of this weapon (non-passive effects). */
    public Stats stats() {
        Stats s = new Stats();
        s.atk = def.atk;
        s.def = def.def;
        for (Effect e : def.innate) s.add(e);
        for (int id : slots) {
            if (id < 0) continue;
            for (Effect e : BonusDef.get(id).effects) s.add(e);
        }
        s.clamp();
        return s;
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        sb.append(def.id);
        for (int s : slots) sb.append(':').append(s);
        return sb.toString();
    }

    public static WeaponInst decode(String s) {
        String[] p = s.split(":");
        int id = Integer.parseInt(p[0]);
        if (id < 0 || id >= WeaponDef.ALL.length) return null;
        WeaponInst w = new WeaponInst(id);
        for (int i = 0; i < w.slots.length && i + 1 < p.length; i++) {
            int b = Integer.parseInt(p[i + 1]);
            w.slots[i] = (b >= 0 && b < BonusDef.ALL.length) ? b : -1;
        }
        return w;
    }
}
