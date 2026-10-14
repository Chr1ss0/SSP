package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/** Game rules around a run that are independent of the UI. */
public final class RunLogic {

    /** Draws a bonus card and adds it to the collection. */
    public static BonusDef drawCard(Profile p, Random r) {
        int luck = p.run != null ? p.run.player.passive().luck : 0;
        BonusDef b = Cards.randomCard(r, luck);
        p.addCard(b.id);
        if (p.run != null) p.run.cardsDrawn++;
        return b;
    }

    /**
     * Rolls the weapon inside a box: a special weapon that is not in the
     * arsenal yet, rarity by box tier. Weapons never found before are a bit
     * more likely so the collection keeps growing.
     */
    public static int rollBoxWeapon(Profile p, Random r, int tier) {
        Fighter pl = p.run != null ? p.run.player : null;
        for (int attempt = 0; attempt < 80; attempt++) {
            int rarity = Cards.rollRarity(r, Cards.boxWeights(tier));
            boolean onlyNew = attempt < 40 && r.nextInt(100) < 35;
            int count = 0;
            for (WeaponDef d : WeaponDef.ALL) if (boxCandidate(d, rarity, onlyNew, p, pl)) count++;
            if (count == 0) continue;
            int pick = r.nextInt(count);
            for (WeaponDef d : WeaponDef.ALL) {
                if (!boxCandidate(d, rarity, onlyNew, p, pl)) continue;
                if (pick-- == 0) return d.id;
            }
        }
        for (WeaponDef d : WeaponDef.ALL) if (!d.isClassic() && (pl == null || !pl.hasWeapon(d.id))) return d.id;
        return -1;
    }

    private static boolean boxCandidate(WeaponDef d, int rarity, boolean onlyNew, Profile p, Fighter pl) {
        if (d.isClassic() || d.rarity != rarity) return false;
        if (pl != null && pl.hasWeapon(d.id)) return false;
        return !onlyNew || !p.owned[d.id];
    }

    /** Applies a victory: stats, level up, healing and the reward box. Returns HP healed. */
    public static int onVictory(Profile p, Random r) {
        Run run = p.run;
        boolean boss = Enemies.isBoss(run.stage);
        p.totalWins++;
        run.wins++;
        if (boss) p.bossesBeaten++;
        if (boss && Enemies.arenaIndex(run.stage) == Enemies.ARENA.length - 1) p.champion++;
        Fighter pl = run.player;
        pl.baseMaxHp += Run.HP_PER_STAGE;
        pl.recalcMaxHp();
        int before = pl.hp;
        int heal = boss ? pl.maxHp : Math.max(1, pl.maxHp * 40 / 100);
        pl.hp = Math.min(pl.maxHp, pl.hp + heal + Run.HP_PER_STAGE);
        if (boss) run.rewardBox = Art.BOX_GOLD;
        else run.rewardBox = r.nextInt(100) < 20 + run.stage * 2 ? Art.BOX_IRON : Art.BOX_WOOD;
        // two bonus cards per defeated opponent
        run.pendingCards = new int[Run.CARDS_PER_WIN];
        for (int i = 0; i < Run.CARDS_PER_WIN; i++) run.pendingCards[i] = drawCard(p, r).id;
        run.usedMask = 0;
        run.enemyUsedMask = 0;
        run.stage++;
        pl.dmgPct = Run.playerDmgPct(run.stage);
        run.enemyHp = -1;
        run.phase = Run.PHASE_REWARD;
        p.bestStage = Math.max(p.bestStage, run.stage - 1);
        return pl.hp - before;
    }

    /** Run lost: cards and unlocked weapons stay (cards get redistributed in the next run). */
    public static void onDefeat(Profile p) {
        p.runs++;
        if (p.run != null) p.bestStage = Math.max(p.bestStage, p.run.stage - 1);
        p.run = null;
    }

    public static Run newRun(Profile p, Random r) {
        p.run = Run.start(p, r);
        return p.run;
    }

    public static boolean canEquip(Profile p, int cardId) {
        return p.available(cardId) > 0;
    }

    public static void equip(Profile p, Fighter f, WeaponInst w, int slot, int cardId) {
        if (slot < 0 || slot >= w.slots.length) return;
        if (cardId >= 0 && p.available(cardId) <= 0 && w.slots[slot] != cardId) return;
        w.slots[slot] = cardId;
        f.recalcMaxHp();
    }

    /** Greedy auto-distribution of free cards into empty slots. */
    public static int autoEquip(Profile p, Fighter f) {
        ArrayList<Integer> pool = new ArrayList<Integer>();
        for (int id = 0; id < p.cards.length; id++) {
            int a = p.available(id);
            for (int i = 0; i < a; i++) pool.add(id);
        }
        Collections.sort(pool, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                int ra = BonusDef.get(a).rarity, rb = BonusDef.get(b).rarity;
                if (ra != rb) return rb - ra;
                return a - b;
            }
        });
        int placed = 0;
        for (WeaponInst w : f.weapons) {
            for (int s = 0; s < w.slots.length; s++) {
                if (w.slots[s] >= 0) continue;
                for (int i = 0; i < pool.size(); i++) {
                    int id = pool.get(i);
                    if (!useful(w, id)) continue;
                    w.slots[s] = id;
                    pool.remove(i);
                    placed++;
                    break;
                }
            }
        }
        f.recalcMaxHp();
        return placed;
    }

    /** Avoids pointless combinations (e.g. a STEIN weapon "breaking" what it already beats). */
    private static boolean useful(WeaponInst w, int cardId) {
        BonusDef b = BonusDef.get(cardId);
        for (Effect e : b.effects) {
            if (e.kind == Effect.BREAK && Element.beats(w.def.type, e.value)) return false;
            if (e.kind == Effect.IMMUNE && !Element.beats(e.value, w.def.type)) return false;
        }
        if (w.countCard(cardId) > 0 && (b.effects[0].kind == Effect.BREAK || b.effects[0].kind == Effect.IMMUNE
                || b.effects[0].kind == Effect.PIERCE || b.effects[0].kind == Effect.PHOENIX)) return false;
        return true;
    }

    /** Unequips everything (used when a new run starts). */
    public static void unequipAll(Fighter f) {
        for (WeaponInst w : f.weapons) java.util.Arrays.fill(w.slots, -1);
        f.recalcMaxHp();
    }

    private RunLogic() {}
}
