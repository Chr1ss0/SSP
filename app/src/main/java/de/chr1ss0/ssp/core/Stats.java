package de.chr1ss0.ssp.core;

/** Aggregated effect values of a weapon (base + innate + equipped cards). */
public final class Stats {
    public int atk, def, crit, lifesteal, thorns, poisonDmg, poisonTurns, dbl, lucky, mirror;
    public int drawDmg, drawHeal, winHeal, shield, rage, combo, first, execute, focus;
    public boolean pierce, antiImmune;
    public int breaks, immune; // type bitmasks
    // passive (summed over the whole arsenal)
    public int maxHp, regen, luck, seer, cat;
    public boolean phoenix;

    public void add(Effect e) {
        switch (e.kind) {
            case Effect.BREAK: breaks |= 1 << e.value; break;
            case Effect.IMMUNE: immune |= 1 << e.value; break;
            case Effect.ATK: atk += e.value; break;
            case Effect.DEF: def += e.value; break;
            case Effect.CRIT: crit += e.value; break;
            case Effect.LIFESTEAL: lifesteal += e.value; break;
            case Effect.THORNS: thorns += e.value; break;
            case Effect.POISON:
                poisonDmg = Math.max(poisonDmg, 0) + e.value;
                poisonTurns = Math.max(poisonTurns, e.value2);
                break;
            case Effect.DOUBLE: dbl += e.value; break;
            case Effect.LUCKY: lucky += e.value; break;
            case Effect.MIRROR: mirror += e.value; break;
            case Effect.DRAW_DMG: drawDmg += e.value; break;
            case Effect.DRAW_HEAL: drawHeal += e.value; break;
            case Effect.WIN_HEAL: winHeal += e.value; break;
            case Effect.SHIELD: shield += e.value; break;
            case Effect.RAGE: rage += e.value; break;
            case Effect.COMBO: combo += e.value; break;
            case Effect.FIRST: first += e.value; break;
            case Effect.EXECUTE: execute = Math.max(execute, e.value); break;
            case Effect.PIERCE: pierce = true; break;
            case Effect.ANTI_IMMUNE: antiImmune = true; break;
            case Effect.MAXHP: maxHp += e.value; break;
            case Effect.REGEN: regen += e.value; break;
            case Effect.LUCK: luck += e.value; break;
            case Effect.SEER: seer += e.value; break;
            case Effect.PHOENIX: phoenix = true; break;
            case Effect.CAT: cat += e.value; break;
            case Effect.FOCUS: focus += e.value; break;
            default: break;
        }
    }

    public void clamp() {
        crit = Math.min(crit, 75);
        dbl = Math.min(dbl, 60);
        lucky = Math.min(lucky, 75);
        mirror = Math.min(mirror, 60);
        lifesteal = Math.min(lifesteal, 100);
        thorns = Math.min(thorns, 100);
        seer = Math.min(seer, 75);
        cat = Math.min(cat, 75);
        luck = Math.min(luck, 3);
    }
}
