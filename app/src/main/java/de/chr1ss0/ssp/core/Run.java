package de.chr1ss0.ssp.core;

import java.util.HashMap;
import java.util.Random;

/**
 * State of the current roguelike run. The player's arsenal is always
 * Stein, Schere, Papier (fixed) plus three chosen special weapons.
 */
public final class Run {
    public static final int PHASE_MAP = 0, PHASE_BATTLE = 1, PHASE_REWARD = 2;
    public static final int SPECIALS = 3;
    public static final int MAX_WEAPONS = 6;
    public static final int BASE_HP = 50, HP_PER_STAGE = 5;
    public static final int CARDS_PER_WIN = 2;

    public long seed;
    public int stage = 1;
    public int phase = PHASE_MAP;
    public Fighter player;
    /** Enemy HP when a battle was interrupted (-1 = fresh). */
    public int enemyHp = -1;
    /** Specials already used in the interrupted battle (bit i = weapon index i). */
    public int usedMask, enemyUsedMask;
    public int rewardBox = -1;
    /** Cards won in the last victory (shown on the reward screen). */
    public int[] pendingCards = new int[0];
    public int wins, roundsWon, cardsDrawn;

    public static Run start(Profile p, Random r) {
        Run run = new Run();
        run.seed = r.nextLong();
        run.player = new Fighter(p.name, true, p.look);
        buildArsenal(run.player, p.loadout);
        run.player.baseMaxHp = BASE_HP;
        run.player.maxHp = 0;
        run.player.recalcMaxHp();
        run.player.hp = run.player.maxHp;
        run.player.dmgPct = playerDmgPct(1);
        return run;
    }

    /** Fills a fighter's arsenal: classics + the given special weapons. */
    static void buildArsenal(Fighter f, int[] specials) {
        f.weapons.clear();
        for (int id : WeaponDef.CLASSICS) f.weapons.add(new WeaponInst(id));
        for (int id : specials) f.weapons.add(new WeaponInst(id));
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
        player.dmgPct = playerDmgPct(stage);
    }

    /** The hero grows stronger with every stage (also boosts Stein/Schere/Papier). */
    public static int playerDmgPct(int stage) {
        return Math.min(400, 100 + 3 * (stage - 1));
    }

    /** Replaces the special weapon in a slot; compatible cards move over to the new weapon. */
    public void setSpecial(Profile p, int slot, int weaponId) {
        int idx = player.specialIndex(slot);
        if (idx < 0) return;
        WeaponInst old = player.weapons.get(idx);
        WeaponInst nw = new WeaponInst(weaponId);
        int k = 0;
        for (int s : old.slots) {
            if (s >= 0 && k < nw.slots.length) nw.slots[k++] = s;
        }
        player.weapons.set(idx, nw);
        p.loadout[slot] = weaponId;
        player.recalcMaxHp();
    }

    void encode(StringBuilder sb) {
        sb.append("run=").append(seed).append(',').append(stage).append(',').append(phase).append(',')
                .append(player.hp).append(',').append(player.baseMaxHp).append(',').append(enemyHp).append(',')
                .append(rewardBox).append(',').append(wins).append(',').append(roundsWon).append(',')
                .append(cardsDrawn).append(',').append(usedMask).append(',').append(enemyUsedMask).append('\n');
        sb.append("weapons=");
        for (int i = 0; i < player.weapons.size(); i++) {
            if (i > 0) sb.append(';');
            sb.append(player.weapons.get(i).encode());
        }
        sb.append('\n');
        sb.append("pending=");
        for (int i = 0; i < pendingCards.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(pendingCards[i]);
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
            buildArsenal(run.player, p.loadout);
            // restore specials (and their cards) from the save; old saves may hold any weapons
            int slot = 0;
            for (String w : ws.split(";")) {
                WeaponInst wi = WeaponInst.decode(w);
                if (wi == null || wi.isClassic() || slot >= SPECIALS) continue;
                boolean dup = false;
                for (int i = 0; i < slot; i++) if (run.player.special(i).def.id == wi.def.id) dup = true;
                if (dup) continue;
                run.player.weapons.set(run.player.specialIndex(slot), wi);
                p.loadout[slot] = wi.def.id;
                p.owned[wi.def.id] = true;
                slot++;
            }
            run.player.baseMaxHp = Integer.parseInt(a[4]);
            run.player.maxHp = 0;
            run.player.recalcMaxHp();
            run.player.hp = Math.max(1, Math.min(run.player.maxHp, Integer.parseInt(a[3])));
            run.enemyHp = Integer.parseInt(a[5]);
            run.rewardBox = Integer.parseInt(a[6]);
            run.wins = Integer.parseInt(a[7]);
            run.roundsWon = Integer.parseInt(a[8]);
            run.cardsDrawn = Integer.parseInt(a[9]);
            if (a.length > 11) {
                run.usedMask = Integer.parseInt(a[10]);
                run.enemyUsedMask = Integer.parseInt(a[11]);
            }
            String pend = m.get("pending");
            if (pend != null && pend.length() > 0) {
                String[] pc = pend.split(",");
                run.pendingCards = new int[pc.length];
                for (int i = 0; i < pc.length; i++) run.pendingCards[i] = Integer.parseInt(pc[i].trim());
            }
            if (run.phase < PHASE_MAP || run.phase > PHASE_REWARD) run.phase = PHASE_MAP;
            run.player.level = run.stage;
            run.player.dmgPct = playerDmgPct(run.stage);
            return run;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
