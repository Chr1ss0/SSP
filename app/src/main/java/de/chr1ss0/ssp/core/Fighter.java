package de.chr1ss0.ssp.core;

import java.util.ArrayList;

/** Player or enemy in a battle. */
public final class Fighter {
    public String name;
    /** Trainer of an opponent's creature: class (e.g. "SCHÜLER") and name. */
    public String title = "", trainer = "";
    public final boolean isPlayer;
    public Look look;
    public int hp, baseMaxHp, maxHp;
    public int level = 1;
    /** Damage scaling in percent (enemies get stronger with the stage). */
    public int dmgPct = 100;
    public final ArrayList<WeaponInst> weapons = new ArrayList<WeaponInst>();

    // battle-temporary state
    public int streak, poisonDmg, poisonTurns, focus, rounds;
    public boolean phoenixUsed, catUsed;

    public Fighter(String name, boolean isPlayer, Look look) {
        this.name = name;
        this.isPlayer = isPlayer;
        this.look = look;
    }

    /** The trainer's full name (e.g. "SCHÜLER PAUL"), or the creature's name without a trainer. */
    public String fullName() {
        if (trainer.length() > 0) return title.length() > 0 ? title + " " + trainer : trainer;
        return title.length() > 0 ? title + " " + name : name;
    }

    /** Passive effects of all equipped cards / innate abilities. */
    public Stats passive() {
        Stats s = new Stats();
        for (WeaponInst w : weapons) {
            for (Effect e : w.def.innate) if (e.isPassive()) s.add(e);
            for (int id : w.slots) {
                if (id < 0) continue;
                for (Effect e : BonusDef.get(id).effects) if (e.isPassive()) s.add(e);
            }
        }
        s.clamp();
        return s;
    }

    /** Recomputes max HP from passives; gaining max HP also heals by the gain. */
    public void recalcMaxHp() {
        int old = maxHp;
        maxHp = baseMaxHp + passive().maxHp;
        if (old > 0 && maxHp > old) hp += maxHp - old;
        if (hp > maxHp) hp = maxHp;
    }

    public void resetBattle() {
        streak = 0;
        poisonDmg = 0;
        poisonTurns = 0;
        focus = 0;
        rounds = 0;
        phoenixUsed = false;
        catUsed = false;
        for (WeaponInst w : weapons) {
            w.shieldUsed = 0;
            w.used = false;
        }
    }

    /** Indices of weapons usable right now. */
    public int[] availableIndices() {
        int n = 0;
        for (WeaponInst w : weapons) if (w.available()) n++;
        int[] out = new int[n];
        n = 0;
        for (int i = 0; i < weapons.size(); i++) if (weapons.get(i).available()) out[n++] = i;
        return out;
    }

    /** Random usable weapon index (the enemy's choice). */
    public int randomAvailable(java.util.Random r) {
        int[] a = availableIndices();
        return a[r.nextInt(a.length)];
    }

    public WeaponInst special(int slot) {
        int n = 0;
        for (WeaponInst w : weapons) {
            if (w.isClassic()) continue;
            if (n++ == slot) return w;
        }
        return null;
    }

    /** Weapon shown in the character's hand outside of battles (first special, else Schere). */
    public int showcaseWeapon() {
        WeaponInst w = special(0);
        return w != null ? w.def.id : WeaponDef.SCHERE_ID;
    }

    public int specialIndex(int slot) {
        int n = 0;
        for (int i = 0; i < weapons.size(); i++) {
            if (weapons.get(i).isClassic()) continue;
            if (n++ == slot) return i;
        }
        return -1;
    }

    public boolean alive() {
        return hp > 0;
    }

    public int countCard(int bonusId) {
        int n = 0;
        for (WeaponInst w : weapons) n += w.countCard(bonusId);
        return n;
    }

    public boolean hasWeapon(int defId) {
        for (WeaponInst w : weapons) if (w.def.id == defId) return true;
        return false;
    }
}
