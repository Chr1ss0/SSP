package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.*;

import java.util.Random;

/** Balance simulation: plays many runs with a simple strategy and persistent progress. */
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
                chooseLoadout(p);
                RunLogic.newRun(p, r);
                RunLogic.autoEquip(p, p.run.player);
                while (true) {
                    Run rn = p.run;
                    Fighter me = rn.player;
                    Fighter en = rn.createEnemy();
                    me.resetBattle();
                    en.resetBattle();
                    Combat c = new Combat(r);
                    int guard = 0;
                    battles++;
                    while (me.alive() && en.alive() && guard++ < 300) {
                        int best = pick(me, en, r);
                        int ei = en.randomAvailable(r);
                        WeaponInst pw = me.weapons.get(best), ew = en.weapons.get(ei);
                        c.round(me, pw, en, ew);
                        if (!pw.isClassic()) pw.used = true;
                        if (!ew.isClassic()) ew.used = true;
                        rounds++;
                    }
                    if (!me.alive()) {
                        reached[run][pl] = rn.stage;
                        RunLogic.onDefeat(p);
                        break;
                    }
                    RunLogic.onVictory(p, r);
                    int wid = RunLogic.rollBoxWeapon(p, r, rn.rewardBox);
                    if (wid >= 0) p.unlock(wid);
                    else RunLogic.drawCard(p, r);
                    // upgrade a slot if the new weapon is better
                    if (wid >= 0) {
                        int worst = -1;
                        for (int s = 0; s < Run.SPECIALS; s++) {
                            WeaponInst w = me.special(s);
                            if (WeaponDef.get(wid).rarity > w.def.rarity && (worst < 0 || w.def.rarity < me.special(worst).def.rarity)) worst = s;
                        }
                        if (worst >= 0) rn.setSpecial(p, worst, wid);
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

    /** Best 3 unlocked specials by rarity. */
    static void chooseLoadout(Profile p) {
        int[] best = {-1, -1, -1};
        for (int k = 0; k < 3; k++) {
            int b = -1;
            for (WeaponDef d : WeaponDef.ALL) {
                if (d.isClassic() || !p.owned[d.id]) continue;
                boolean taken = false;
                for (int j = 0; j < k; j++) if (best[j] == d.id) taken = true;
                if (taken) continue;
                if (b < 0 || d.rarity > WeaponDef.get(b).rarity || (d.rarity == WeaponDef.get(b).rarity && d.atk > WeaponDef.get(b).atk)) b = d.id;
            }
            best[k] = b;
        }
        for (int k = 0; k < 3; k++) if (best[k] >= 0) p.loadout[k] = best[k];
    }

    /** Uses a special when it beats most of what the enemy can still pick, else a classic. */
    static int pick(Fighter me, Fighter en, Random r) {
        java.util.ArrayList<WeaponInst> avail = new java.util.ArrayList<WeaponInst>();
        for (WeaponInst w : en.weapons) if (w.available()) avail.add(w);
        int best = 0;
        double bestScore = -1e9;
        for (int i = 0; i < me.weapons.size(); i++) {
            WeaponInst w = me.weapons.get(i);
            if (!w.available()) continue;
            int[] m = Combat.matchup(w, avail);
            double s = (m[0] - m[2]) / (double) avail.size() * (1 + w.stats().atk / 15.0);
            if (!w.isClassic()) s -= 0.15; // save specials for good moments
            s += r.nextDouble() * 0.05;
            if (s > bestScore) { bestScore = s; best = i; }
        }
        return best;
    }
}
