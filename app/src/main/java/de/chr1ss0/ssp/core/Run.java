package de.chr1ss0.ssp.core;

import java.util.HashMap;
import java.util.Random;

/** State of the current roguelike run. */
public final class Run {
    public static final int PHASE_MAP = 0, PHASE_BATTLE = 1, PHASE_REWARD = 2;
    public static final int MAX_WEAPONS = 6;
    public static final int BASE_HP = 50, HP_PER_STAGE = 4;

    public long seed;
    public int stage = 1;
    public int phase = PHASE_MAP;
    public Fighter player;
    /** Enemy HP when a battle was interrupted (-1 = fresh). */
    public int enemyHp = -1;
    public int rewardBox = -1;
    public int wins, roundsWon, cardsDrawn;

    public static Run start(Profile p, Random r) {
        Run run = new Run();
        run.seed = r.nextLong();
        run.player = new Fighter(p.name, true, p.look);
        run.player.weapons.add(new WeaponInst(WeaponDef.STEIN_ID));
        run.player.weapons.add(new WeaponInst(WeaponDef.SCHERE_ID));
        run.player.weapons.add(new WeaponInst(WeaponDef.PAPIER_ID));
        run.player.baseMaxHp = BASE_HP;
        run.player.maxHp = 0;
        run.player.recalcMaxHp();
        run.player.hp = run.player.maxHp;
        return run;
    }

    public Fighter createEnemy() {
        Fighter e = Enemies.create(seed, stage);
        if (enemyHp > 0) e.hp = Math.min(e.maxHp, enemyHp);
        return e;
    }

    public void syncPlayer(Profile p) {
        player.name = p.name;
        player.look = p.look;
        player.level = stage;
    }

    void encode(StringBuilder sb) {
        sb.append("run=").append(seed).append(',').append(stage).append(',').append(phase).append(',')
                .append(player.hp).append(',').append(player.baseMaxHp).append(',').append(enemyHp).append(',')
                .append(rewardBox).append(',').append(wins).append(',').append(roundsWon).append(',')
                .append(cardsDrawn).append('\n');
        sb.append("weapons=");
        for (int i = 0; i < player.weapons.size(); i++) {
            if (i > 0) sb.append(';');
            sb.append(player.weapons.get(i).encode());
        }
        sb.append('\n');
    }

    static Run decode(HashMap<String, String> m, Profile p) {
        String r = m.get("run");
        String ws = m.get("weapons");
        if (r == null || ws == null || ws.length() == 0) return null;
        try {
            String[] a = r.split(",");
            Run run = new Run();
            run.seed = Long.parseLong(a[0]);
            run.stage = Math.max(1, Integer.parseInt(a[1]));
            run.phase = Integer.parseInt(a[2]);
            run.player = new Fighter(p.name, true, p.look);
            for (String w : ws.split(";")) {
                WeaponInst wi = WeaponInst.decode(w);
                if (wi != null && !run.player.hasWeapon(wi.def.id) && run.player.weapons.size() < MAX_WEAPONS)
                    run.player.weapons.add(wi);
            }
            if (run.player.weapons.isEmpty()) return null;
            run.player.baseMaxHp = Integer.parseInt(a[4]);
            run.player.maxHp = 0;
            run.player.recalcMaxHp();
            run.player.hp = Math.max(1, Math.min(run.player.maxHp, Integer.parseInt(a[3])));
            run.enemyHp = Integer.parseInt(a[5]);
            run.rewardBox = Integer.parseInt(a[6]);
            run.wins = Integer.parseInt(a[7]);
            run.roundsWon = Integer.parseInt(a[8]);
            run.cardsDrawn = Integer.parseInt(a[9]);
            if (run.phase < PHASE_MAP || run.phase > PHASE_REWARD) run.phase = PHASE_MAP;
            run.player.level = run.stage;
            return run;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
