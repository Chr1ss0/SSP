package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Resolves one round of rock-paper-scissors between two fighters. The result
 * is a list of presentation events which the battle screen plays back.
 */
public final class Combat {
    public static final int WIN = 1, DRAW = 0, LOSE = -1;
    public static final int P = 0, E = 1; // sides

    public static final class Ev {
        public static final int MSG = 0, ATTACK = 1, HIT = 2, HEAL = 3, BLOCK = 4, FAINT = 5, STATUS = 6;
        public final int kind, side, amount, hpAfter, type;
        public final boolean crit;
        public final String text;

        Ev(int kind, int side, int amount, int hpAfter, int type, boolean crit, String text) {
            this.kind = kind;
            this.side = side;
            this.amount = amount;
            this.hpAfter = hpAfter;
            this.type = type;
            this.crit = crit;
            this.text = text;
        }
    }

    public static final class Result {
        public int outcome;
        public final List<Ev> events = new ArrayList<Ev>();
    }

    private final Random rng;
    private Result res;
    private final Fighter[] f = new Fighter[2];
    private final WeaponInst[] w = new WeaponInst[2];
    private final Stats[] st = new Stats[2];
    private final Stats[] ps = new Stats[2];

    public Combat(Random rng) {
        this.rng = rng;
    }

    private boolean roll(int pct) {
        return pct > 0 && rng.nextInt(100) < pct;
    }

    private void msg(String s) {
        res.events.add(new Ev(Ev.MSG, 0, 0, 0, 0, false, s));
    }

    private String wn(int side) {
        return w[side].upper();
    }

    private String fn(int side) {
        return f[side].name;
    }

    private static String findCard(WeaponInst wi, int kind, int value) {
        for (int id : wi.slots) {
            if (id < 0) continue;
            for (Effect e : BonusDef.get(id).effects)
                if (e.kind == kind && (value < 0 || e.value == value)) return BonusDef.get(id).upper();
        }
        return null;
    }

    public Result round(Fighter player, WeaponInst pw, Fighter enemy, WeaponInst ew) {
        res = new Result();
        f[P] = player; f[E] = enemy;
        w[P] = pw; w[E] = ew;
        st[P] = pw.stats(); st[E] = ew.stats();
        ps[P] = player.passive(); ps[E] = enemy.passive();
        boolean firstRound = player.rounds == 0;
        player.rounds++;
        enemy.rounds++;

        int tp = pw.def.type, te = ew.def.type;
        boolean[] beats = new boolean[2];
        boolean[] broke = new boolean[2];
        beats[P] = Element.beats(tp, te);
        beats[E] = Element.beats(te, tp);
        if (!beats[P] && (st[P].breaks & (1 << te)) != 0) { beats[P] = true; broke[P] = true; }
        if (!beats[E] && (st[E].breaks & (1 << tp)) != 0) { beats[E] = true; broke[E] = true; }

        for (int s = 0; s < 2; s++) {
            int o = 1 - s;
            int myType = w[s].def.type;
            if (beats[s] && (st[o].immune & (1 << myType)) != 0) {
                if (st[s].antiImmune) {
                    msg(wn(s) + " durchbricht die Immunität!");
                } else {
                    beats[s] = false;
                    broke[s] = false;
                    msg(wn(o) + " ist immun gegen " + Element.UPPER[myType] + "!");
                }
            }
        }

        int outcome;
        if (beats[P] && !beats[E]) outcome = WIN;
        else if (beats[E] && !beats[P]) outcome = LOSE;
        else outcome = DRAW;

        if (outcome == DRAW) {
            if (beats[P] && beats[E]) msg("Beide Waffen brechen sich gegenseitig!");
            if (roll(st[P].lucky)) {
                outcome = WIN;
                msg("Glückstreffer! " + wn(P) + " nutzt die Lücke!");
            } else if (roll(st[E].lucky)) {
                outcome = LOSE;
                msg("Glückstreffer für " + fn(E) + "!");
            }
        } else if (outcome == LOSE && roll(st[P].mirror)) {
            outcome = DRAW;
            msg("SPIEGEL! " + wn(P) + " lenkt den Angriff ab!");
        } else if (outcome == WIN && roll(st[E].mirror)) {
            outcome = DRAW;
            msg("SPIEGEL! " + wn(E) + " lenkt den Angriff ab!");
        }
        res.outcome = outcome;

        if (outcome == DRAW) {
            if (tp == te) msg("Gleiche Kraft! Unentschieden!");
            else if (!(beats[P] && beats[E])) msg("Unentschieden!");
            for (int s = 0; s < 2; s++) drawEffects(s);
        } else {
            int a = outcome == WIN ? P : E;
            if (broke[a]) {
                String card = findCard(w[a], Effect.BREAK, w[1 - a].def.type);
                if (card != null) msg(card + " wirkt!");
            }
            msg(wn(a) + " " + Element.VERB[w[a].def.type] + " " + wn(1 - a) + "!");
            attack(a, firstRound);
        }

        // end of round effects
        for (int s = 0; s < 2; s++) {
            Fighter x = f[s];
            if (!x.alive() || !f[1 - s].alive()) continue;
            if (x.poisonTurns > 0 && x.poisonDmg > 0) {
                msg(fn(s) + " leidet unter Gift!");
                damage(s, x.poisonDmg, false, -1);
                x.poisonTurns--;
                if (x.poisonTurns <= 0) x.poisonDmg = 0;
            }
        }
        for (int s = 0; s < 2; s++) {
            if (f[s].alive() && f[1 - s].alive() && ps[s].regen > 0 && f[s].hp < f[s].maxHp) {
                heal(s, ps[s].regen, null);
            }
        }
        for (int s = 0; s < 2; s++) {
            if (!f[s].alive()) res.events.add(new Ev(Ev.FAINT, s, 0, 0, 0, false, null));
        }
        return res;
    }

    private void drawEffects(int s) {
        int o = 1 - s;
        Stats x = st[s];
        if (x.drawDmg > 0 && f[o].alive()) {
            msg(wn(s) + " trifft trotzdem!");
            res.events.add(new Ev(Ev.ATTACK, s, 0, 0, w[s].def.type, false, null));
            damage(o, x.drawDmg, false, w[s].def.type);
        }
        if (x.drawHeal > 0 && f[s].alive() && f[s].hp < f[s].maxHp) {
            heal(s, x.drawHeal, fn(s) + " sammelt Kraft!");
        }
        if (x.focus > 0) {
            f[s].focus += x.focus;
            msg(fn(s) + " konzentriert sich! (+" + f[s].focus + ")");
        }
    }

    private void attack(int a, boolean firstRound) {
        int d = 1 - a;
        Fighter A = f[a], D = f[d];
        Stats as = st[a], ds = st[d];
        int type = w[a].def.type;

        int dmg = as.atk + A.focus;
        A.focus = 0;
        dmg = dmg * A.dmgPct / 100;
        if (firstRound && as.first > 0) {
            dmg = dmg * (100 + as.first) / 100;
            msg("Überraschungsangriff!");
        }
        if (as.rage > 0 && A.hp * 100 < A.maxHp * 30) {
            dmg = dmg * (100 + as.rage) / 100;
            msg(fn(a) + " ist wütend!");
        }
        dmg += as.combo * A.streak;
        boolean crit = roll(as.crit);
        if (crit) dmg *= 2;
        int defv = as.pierce ? 0 : ds.def;
        dmg = Math.max(1, dmg - defv);

        res.events.add(new Ev(Ev.ATTACK, a, 0, 0, type, crit, null));
        int total = 0;
        if (w[d].shieldUsed < ds.shield) {
            w[d].shieldUsed++;
            res.events.add(new Ev(Ev.BLOCK, d, 0, D.hp, 0, false, null));
            msg("SCHILD! " + wn(d) + " blockt den Treffer!");
        } else {
            if (crit) msg("Volltreffer!");
            total += damage(d, dmg, crit, type);
            if (D.alive() && roll(as.dbl)) {
                msg("Doppelschlag!");
                res.events.add(new Ev(Ev.ATTACK, a, 0, 0, type, false, null));
                total += damage(d, Math.max(1, dmg / 2), false, type);
            }
        }
        if (total > 0 && as.lifesteal > 0 && A.alive()) {
            int h = Math.max(1, total * as.lifesteal / 100);
            heal(a, h, fn(a) + " saugt Lebenskraft ab!");
        }
        if (as.winHeal > 0 && A.alive() && A.hp < A.maxHp) {
            heal(a, as.winHeal, null);
        }
        if (total > 0 && as.poisonDmg > 0 && D.alive()) {
            D.poisonDmg = Math.max(D.poisonDmg, as.poisonDmg);
            D.poisonTurns = Math.max(D.poisonTurns, as.poisonTurns);
            msg(fn(d) + " wurde vergiftet!");
            res.events.add(new Ev(Ev.STATUS, d, 0, 0, 0, false, null));
        }
        if (total > 0 && ds.thorns > 0 && A.alive()) {
            int refl = Math.max(1, total * ds.thorns / 100);
            msg("Dornen! " + fn(a) + " verletzt sich!");
            damage(a, refl, false, -1);
        }
        if (as.execute > 0 && D.alive() && D.hp * 100 <= D.maxHp * as.execute) {
            msg("EXEKUTION!");
            D.hp = 0;
            res.events.add(new Ev(Ev.HIT, d, 0, 0, type, true, null));
        }
        A.streak++;
        D.streak = 0;
    }

    /** Applies damage with survival checks. Returns damage dealt. */
    private int damage(int side, int dmg, boolean crit, int type) {
        Fighter x = f[side];
        int before = x.hp;
        int hp = x.hp - dmg;
        String survive = null;
        if (hp <= 0) {
            if (ps[side].phoenix && !x.phoenixUsed) {
                x.phoenixUsed = true;
                hp = 1;
                survive = "PHÖNIX! " + x.name + " steigt aus der Asche!";
            } else if (ps[side].cat > 0 && !x.catUsed && roll(ps[side].cat)) {
                x.catUsed = true;
                hp = 1;
                survive = "Katzenglück! " + x.name + " landet auf den Füßen!";
            }
        }
        x.hp = Math.max(0, hp);
        res.events.add(new Ev(Ev.HIT, side, before - x.hp, x.hp, type, crit, null));
        if (survive != null) msg(survive);
        return before - x.hp;
    }

    private void heal(int side, int amount, String text) {
        Fighter x = f[side];
        int before = x.hp;
        x.hp = Math.min(x.maxHp, x.hp + amount);
        if (x.hp > before) {
            if (text != null) msg(text);
            res.events.add(new Ev(Ev.HEAL, side, x.hp - before, x.hp, 0, false, null));
        }
    }

    /** Expected outcome score of weapon w against an arsenal (for hints): wins, draws, losses. */
    public static int[] matchup(WeaponInst mine, List<WeaponInst> theirs) {
        int win = 0, draw = 0, lose = 0;
        for (WeaponInst t : theirs) {
            int o = outcome(mine, t);
            if (o == WIN) win++;
            else if (o == LOSE) lose++;
            else draw++;
        }
        return new int[]{win, draw, lose};
    }

    /** Type outcome of one weapon against another, including breaker and immunity cards. */
    public static int outcome(WeaponInst mine, WeaponInst theirs) {
        Stats ms = mine.stats(), ts = theirs.stats();
        int a = mine.def.type, b = theirs.def.type;
        boolean pb = Element.beats(a, b) || (ms.breaks & (1 << b)) != 0;
        boolean eb = Element.beats(b, a) || (ts.breaks & (1 << a)) != 0;
        if (pb && (ts.immune & (1 << a)) != 0 && !ms.antiImmune) pb = false;
        if (eb && (ms.immune & (1 << b)) != 0 && !ts.antiImmune) eb = false;
        if (pb && !eb) return WIN;
        if (eb && !pb) return LOSE;
        return DRAW;
    }
}
