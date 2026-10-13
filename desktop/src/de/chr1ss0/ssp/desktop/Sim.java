package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.*;

import java.util.Random;

/** Balance simulation: plays many runs with a simple strategy and persistent cards. */
public final class Sim {
    public static void main(String[] args) {
        int players = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        int runsPer = args.length > 1 ? Integer.parseInt(args[1]) : 15;
        Random r = new Random(42);
        int[][] reached = new int[runsPer][players];
        long rounds = 0, battles = 0;
        for (int pl = 0; pl < players; pl++) {
            Profile p = new Profile();
            for (int run = 0; run < runsPer; run++) {
                RunLogic.newRun(p, r);
                RunLogic.autoEquip(p, p.run.player);
                while (true) {
                    Run rn = p.run;
                    Fighter me = rn.player;
                    Fighter en = rn.createEnemy();
                    for (WeaponInst w : en.weapons) p.seen[w.def.id] = true;
                    me.resetBattle();
                    en.resetBattle();
                    Combat c = new Combat(r);
                    int guard = 0;
                    battles++;
                    while (me.alive() && en.alive() && guard++ < 200) {
                        WeaponInst best = null;
                        double bestScore = -1e9;
                        for (WeaponInst w : me.weapons) {
                            int[] m = Combat.matchup(w, en.weapons);
                            double s = m[0] * (1 + w.stats().atk / 10.0) - m[2] + r.nextDouble() * 0.3;
                            if (s > bestScore) { bestScore = s; best = w; }
                        }
                        WeaponInst ew = en.weapons.get(r.nextInt(en.weapons.size()));
                        Combat.Result res = c.round(me, best, en, ew);
                        rounds++;
                        if (res.outcome == Combat.WIN) {
                            RunLogic.drawCard(p, r);
                            RunLogic.autoEquip(p, me);
                        }
                    }
                    if (!me.alive()) {
                        reached[run][pl] = rn.stage;
                        RunLogic.onDefeat(p);
                        break;
                    }
                    RunLogic.onVictory(p, r);
                    int box = rn.rewardBox;
                    int wid = Cards.randomWeapon(r, box, me);
                    p.owned[wid] = true;
                    if (me.weapons.size() < Run.MAX_WEAPONS) me.weapons.add(new WeaponInst(wid));
                    else {
                        // replace the weakest non-classic weapon if the new one is rarer
                        int worst = -1;
                        for (int i = 0; i < me.weapons.size(); i++) {
                            WeaponInst w = me.weapons.get(i);
                            if (w.def.rarity < WeaponDef.get(wid).rarity && w.def.slots < 3 && (worst < 0 || w.def.rarity < me.weapons.get(worst).def.rarity)) worst = i;
                        }
                        if (worst >= 0) me.weapons.set(worst, new WeaponInst(wid));
                    }
                    RunLogic.autoEquip(p, me);
                    rn.phase = Run.PHASE_MAP;
                    rn.rewardBox = -1;
                    if (rn.stage > 120) { reached[run][pl] = rn.stage; RunLogic.onDefeat(p); break; }
                }
            }
        }
        System.out.println("avg rounds per battle: " + (rounds / (double) battles));
        for (int run = 0; run < runsPer; run++) {
            int sum = 0, min = 999, max = 0;
            for (int pl = 0; pl < players; pl++) {
                sum += reached[run][pl];
                min = Math.min(min, reached[run][pl]);
                max = Math.max(max, reached[run][pl]);
            }
            System.out.printf("run %2d: died at stage avg %.1f (min %d, max %d)%n", run + 1, sum / (double) players, min, max);
        }
    }
}
