package de.chr1ss0.ssp.core;

import java.util.Random;

/** Random draws of bonus cards and weapons (boxes). */
public final class Cards {
    /** Rarity weights (common, rare, epic, legendary) per luck level 0..3. */
    private static final int[][] CARD_WEIGHTS = {
            {60, 28, 10, 2},
            {45, 33, 17, 5},
            {32, 36, 24, 8},
            {22, 36, 30, 12},
    };

    public static int rollRarity(Random r, int[] weights) {
        int total = 0;
        for (int w : weights) total += w;
        int x = r.nextInt(total);
        for (int i = 0; i < weights.length; i++) {
            x -= weights[i];
            if (x < 0) return i;
        }
        return 0;
    }

    public static BonusDef randomCard(Random r, int luck) {
        int rarity = rollRarity(r, CARD_WEIGHTS[Math.max(0, Math.min(3, luck))]);
        int count = 0;
        for (BonusDef b : BonusDef.ALL) if (b.rarity == rarity) count++;
        int pick = r.nextInt(count);
        for (BonusDef b : BonusDef.ALL) {
            if (b.rarity != rarity) continue;
            if (pick-- == 0) return b;
        }
        return BonusDef.ALL[0];
    }

    /** Box odds per box tier (wood, iron, gold). */
    private static final int[][] BOX_WEIGHTS = {
            {62, 30, 7, 1},
            {30, 45, 20, 5},
            {0, 40, 42, 18},
    };

    /** Random weapon id from a box, excluding the given owned ids. */
    public static int randomWeapon(Random r, int boxTier, Fighter owner) {
        for (int attempt = 0; attempt < 50; attempt++) {
            int rarity = rollRarity(r, BOX_WEIGHTS[boxTier]);
            int count = 0;
            for (WeaponDef d : WeaponDef.ALL) if (d.rarity == rarity && !owner.hasWeapon(d.id)) count++;
            if (count == 0) continue;
            int pick = r.nextInt(count);
            for (WeaponDef d : WeaponDef.ALL) {
                if (d.rarity != rarity || owner.hasWeapon(d.id)) continue;
                if (pick-- == 0) return d.id;
            }
        }
        for (WeaponDef d : WeaponDef.ALL) if (!owner.hasWeapon(d.id)) return d.id;
        return 0;
    }

    private Cards() {}
}
