package de.chr1ss0.ssp.core;

import java.util.HashMap;

/**
 * Procedurally builds character sprites (front view for trainers / editor,
 * back view for the hero in battle) holding a weapon in the raised hand.
 */
public final class CharArt {
    public static final int FRONT_W = 40, FRONT_H = 48;
    public static final int BACK_W = 48, BACK_H = 42;

    private static final HashMap<String, Sprite> CACHE = new HashMap<String, Sprite>();

    private static int sh(int c) { return Pal.shade(c, 0.72f); }

    private static final int MOUTH = 0xFFA03838;
    private static final int BLUSH = 0xFFF0A098;
    private static final int SHOES = 0xFF483830;
    private static final int GOLD = 0xFFF0C030;
    private static final int METAL = 0xFFB0B8C8;
    private static final int WIZARD = 0xFF6848C0;
    private static final int BAND = 0xFFE03030;

    /** weaponId -1 = empty fist. */
    public static Sprite front(Look l, int weaponId) {
        String key = "F" + l.key() + "|" + weaponId;
        Sprite s = CACHE.get(key);
        if (s == null) {
            s = buildFront(l, weaponId);
            CACHE.put(key, s);
        }
        return s;
    }

    public static Sprite back(Look l, int weaponId) {
        String key = "B" + l.key() + "|" + weaponId;
        Sprite s = CACHE.get(key);
        if (s == null) {
            s = buildBack(l, weaponId);
            CACHE.put(key, s);
        }
        return s;
    }

    public static void clearCache() {
        CACHE.clear();
    }

    // ------------------------------------------------------------------ front

    private static Sprite buildFront(Look l, int weaponId) {
        Stamp c = new Stamp(FRONT_W, FRONT_H);
        int skin = Look.SKIN[l.skin], skinS = sh(skin);
        int hair = Look.HAIR_COLORS[l.hairColor], hairS = sh(hair);
        int shirt = Look.CLOTH[l.shirt], shirtS = sh(shirt);
        int pants = Look.CLOTH[l.pants], pantsS = sh(pants);
        double hx = 24.0, hy = 16.5;

        // hair behind the body
        switch (l.hair) {
            case 2: c.rect(15, 13, 18, 14).ellipse(hx, hy - 1, 9, 8.5).stamp(hair, hairS); break;
            case 3: c.ellipse(hx + 9, hy - 1, 3, 6).stamp(hair, hairS); break;
            case 4: c.ellipse(hx, hy - 3, 12, 10.5).stamp(hair, hairS); break;
            case 7: c.rect(15, 12, 18, 11).stamp(hair, hairS); break;
            default: break;
        }
        // legs + shoes
        c.rect(18, 37, 12, 8).stamp(pants, pantsS);
        c.rect(23, 40, 2, 5).paint(Pal.OUTLINE);
        c.rect(17, 44, 6, 3).stamp(SHOES, 0);
        c.rect(25, 44, 6, 3).stamp(SHOES, 0);
        // torso
        c.roundTop(16, 25, 16, 13, 4).stamp(shirt, shirtS);
        c.rect(17, 35, 14, 1).paint(pantsS);
        // hanging arm (viewer's right)
        c.rect(31, 27, 3, 6).stamp(shirt, shirtS);
        c.ellipse(32.5, 35, 2.2, 2.2).stamp(skin, skinS);
        // raised arm (viewer's left)
        c.line(17, 28, 13, 26, 3).stamp(shirt, shirtS);
        c.line(11, 25, 10, 20, 3).stamp(skin, skinS);
        // neck, ears, head
        c.rect(22, 22, 4, 4).stamp(skin, skinS);
        c.ellipse(16, hy + 1, 1.8, 2.2).ellipse(32, hy + 1, 1.8, 2.2).stamp(skin, skinS);
        c.ellipse(hx, hy, 7.8, 7.6).stamp(skin, skinS);
        // face
        int ex1 = 21, ex2 = 26, ey = 16;
        c.set(ex1, ey, 0xFF101010); c.set(ex1, ey + 1, 0xFF101010);
        c.set(ex2, ey, 0xFF101010); c.set(ex2, ey + 1, 0xFF101010);
        c.set(23, 20, MOUTH); c.set(24, 20, MOUTH);
        c.set(19, 19, BLUSH); c.set(28, 19, BLUSH);
        // hair front
        frontHair(c, l.hair, hx, hy, hair, hairS, skin, skinS);
        // hat
        frontHat(c, l.hat, hx, hy, shirt, pants);
        // extras
        int ink = Pal.OUTLINE;
        switch (l.extra) {
            case 1: // glasses
                c.rect(19, 15, 4, 4).stamp(0xFFE8F4FF, 0);
                c.rect(25, 15, 4, 4).stamp(0xFFE8F4FF, 0);
                c.set(20, 16, 0xFF101010); c.set(20, 17, 0xFF101010);
                c.set(26, 16, 0xFF101010); c.set(26, 17, 0xFF101010);
                break;
            case 2: // sunglasses
                c.rect(19, 15, 4, 3).rect(25, 15, 4, 3).stamp(0xFF282830, 0);
                c.rect(23, 15, 2, 1).paint(ink);
                c.set(19, 15, 0xFF6070A0); c.set(25, 15, 0xFF6070A0);
                break;
            case 3: // eye patch
                c.rect(25, 15, 3, 3).paint(0xFF181818);
                c.line(17, 13, 31, 13, 1).paint(0xFF181818);
                break;
            case 4: // moustache
                c.rect(21, 19, 6, 1).rect(20, 20, 2, 1).rect(26, 20, 2, 1).paint(hairS);
                break;
            default: break;
        }
        // weapon + fist
        if (weaponId >= 0) c.sprite(WeaponArt.icon(weaponId), 2, 4);
        c.ellipse(10, 18, 2.6, 2.6).stamp(skin, skinS);
        return c.toSprite();
    }

    private static void frontHair(Stamp c, int style, double hx, double hy, int hair, int hairS, int skin, int skinS) {
        int top = (int) (hy - 4);
        switch (style) {
            case 0: // short
                c.ellipse(hx, hy - 1.5, 8.6, 8).above(top).rect(16, top - 1, 2, 5).rect(30, top - 1, 2, 5).stamp(hair, hairS);
                fringe(c, top, hair);
                break;
            case 1: // spiky
                c.tri(15, 12, 19, 12, 14, 3).tri(18, 10, 23, 10, 19, 0).tri(22, 10, 27, 10, 26, 0)
                        .tri(26, 10, 31, 11, 32, 2).tri(29, 12, 33, 13, 35, 7);
                c.ellipse(hx, hy - 1.5, 8.6, 8).above(top).rect(16, top - 1, 2, 5).rect(30, top - 1, 2, 5).stamp(hair, hairS);
                fringe(c, top, hair);
                break;
            case 2: // long
                c.ellipse(hx, hy - 1.5, 8.8, 8).above(top).rect(15, top - 1, 3, 12).rect(30, top - 1, 3, 12).stamp(hair, hairS);
                fringe(c, top, hair);
                break;
            case 3: // ponytail
                c.ellipse(hx, hy - 1.5, 8.6, 8).above(top).rect(16, top - 1, 2, 4).rect(30, top - 1, 2, 4).stamp(hair, hairS);
                c.rect(31, 13, 2, 2).paint(BAND);
                fringe(c, top, hair);
                break;
            case 4: // afro
                c.ellipse(hx, hy - 3.5, 9.6, 7.6).above(top).stamp(hair, hairS);
                break;
            case 5: // mohawk
                c.roundTop(22, 1, 4, 11, 2).stamp(hair, hairS);
                break;
            case 6: // bald: shine
                c.set(20, 11, Pal.lighten(skin, 0.6f));
                c.set(21, 10, Pal.lighten(skin, 0.6f));
                break;
            case 7: // bob
                c.ellipse(hx, hy - 1.5, 9, 8.2).above(top + 1).rect(15, top - 1, 3, 10).rect(30, top - 1, 3, 10).stamp(hair, hairS);
                break;
            default: break;
        }
    }

    private static void fringe(Stamp c, int top, int hair) {
        c.rect(18, top, 3, 1).rect(23, top, 3, 1).rect(28, top, 2, 1).paint(hair);
        c.set(18, top + 1, hair); c.set(24, top + 1, hair);
    }

    private static void frontHat(Stamp c, int hat, double hx, double hy, int shirt, int pants) {
        int top = (int) (hy - 4);
        switch (hat) {
            case Look.HAT_CAP:
                c.ellipse(hx, hy - 4, 8.7, 6.4).above(top).stamp(shirt, sh(shirt));
                c.rect(14, top - 1, 19, 2).stamp(sh(shirt), 0);
                c.rect(22, top - 6, 4, 3).paint(Pal.WHITE);
                break;
            case Look.HAT_BEANIE:
                c.ellipse(hx, hy - 3, 9, 7.8).above(top + 1).stamp(pants, sh(pants));
                c.rect(15, top - 1, 18, 3).stamp(Pal.lighten(pants, 0.25f), sh(pants));
                c.ellipse(hx, hy - 12, 2.6, 2.6).stamp(Pal.WHITE, Pal.LIGHT);
                break;
            case Look.HAT_BAND:
                c.rect(15, top - 2, 18, 2).stamp(BAND, sh(BAND));
                c.tri(32, top - 2, 32, top + 1, 36, top + 2).stamp(BAND, sh(BAND));
                break;
            case Look.HAT_CROWN:
                c.tri(17, 8, 21, 8, 17, 1).tri(21, 8, 27, 8, 24, 0).tri(27, 8, 31, 8, 31, 1).rect(17, 6, 14, 4).stamp(GOLD, sh(GOLD));
                c.set(24, 7, Pal.RED); c.set(19, 7, Pal.BLUE); c.set(29, 7, Pal.BLUE);
                break;
            case Look.HAT_WIZARD:
                c.tri(hx - 7, hy - 6, hx + 7, hy - 6, hx + 3, -1).stamp(WIZARD, sh(WIZARD));
                c.ellipse(hx, hy - 6, 11, 2).stamp(WIZARD, sh(WIZARD));
                c.set(24, 5, GOLD); c.set(23, 4, GOLD); c.set(25, 4, GOLD); c.set(24, 3, GOLD);
                break;
            case Look.HAT_HELMET:
                c.ellipse(hx, hy - 2, 9.2, 8.6).above(top + 1).rect(15, top, 3, 8).rect(30, top, 3, 8).stamp(METAL, sh(METAL));
                c.rect(23, 4, 2, 8).paint(Pal.lighten(METAL, 0.5f));
                break;
            default: break;
        }
    }

    // ------------------------------------------------------------------ back

    private static Sprite buildBack(Look l, int weaponId) {
        Stamp c = new Stamp(BACK_W, BACK_H);
        int skin = Look.SKIN[l.skin], skinS = sh(skin);
        int hair = Look.HAIR_COLORS[l.hairColor], hairS = sh(hair);
        int shirt = Look.CLOTH[l.shirt], shirtS = sh(shirt);
        double bx = 20.0, by = 16.0;

        // torso (cut off by the screen bottom)
        c.roundTop(4, 29, 32, 14, 8).stamp(shirt, shirtS);
        c.rect(19, 33, 2, 9).paint(shirtS);
        // raised arm (viewer's right)
        c.line(32, 32, 38, 27, 4).stamp(shirt, shirtS);
        c.line(38, 26, 38, 19, 3).stamp(skin, skinS);
        // neck, ears, head
        c.rect(16, 23, 8, 7).stamp(skin, skinS);
        c.ellipse(9.5, by + 1, 2, 2.6).ellipse(30.5, by + 1, 2, 2.6).stamp(skin, skinS);
        c.ellipse(bx, by, 10, 10).stamp(skin, skinS);
        // hair
        switch (l.hair) {
            case 0:
                c.ellipse(bx, by - 0.5, 10.6, 10.2).above((int) by + 6).stamp(hair, hairS);
                break;
            case 1:
                c.tri(9, 12, 13, 8, 6, 3).tri(12, 8, 18, 6, 12, 0).tri(17, 6, 23, 6, 21, -1)
                        .tri(22, 6, 28, 8, 28, 0).tri(26, 8, 31, 12, 34, 3);
                c.ellipse(bx, by - 0.5, 10.6, 10.2).above((int) by + 6).stamp(hair, hairS);
                break;
            case 2:
                c.ellipse(bx, by - 0.5, 10.6, 10.2).rect(10, (int) by, 20, 16).stamp(hair, hairS);
                break;
            case 3:
                c.ellipse(bx, by - 0.5, 10.6, 10.2).above((int) by + 6).stamp(hair, hairS);
                c.ellipse(bx, by + 10, 2.8, 6).stamp(hair, hairS);
                c.rect(18, (int) by + 4, 4, 2).paint(BAND);
                break;
            case 4:
                c.ellipse(bx, by - 2, 13.5, 12).stamp(hair, hairS);
                break;
            case 5:
                c.roundTop(18, 1, 4, (int) by + 6, 2).stamp(hair, hairS);
                break;
            case 6:
                c.set(15, 9, Pal.lighten(skin, 0.6f));
                c.set(16, 8, Pal.lighten(skin, 0.6f));
                break;
            case 7:
                c.ellipse(bx, by - 0.5, 11.2, 10.6).above((int) by + 9).stamp(hair, hairS);
                break;
            default: break;
        }
        // hat
        int pants = Look.CLOTH[l.pants];
        switch (l.hat) {
            case Look.HAT_CAP:
                c.ellipse(bx, by - 3, 10.8, 8.4).above((int) by - 1).stamp(shirt, shirtS);
                c.rect(17, (int) by - 3, 6, 2).paint(Pal.OUTLINE);
                c.rect(18, (int) by - 3, 4, 1).paint(skin);
                break;
            case Look.HAT_BEANIE:
                c.ellipse(bx, by - 2, 11, 9.4).above((int) by + 1).stamp(pants, sh(pants));
                c.rect(9, (int) by - 2, 22, 3).stamp(Pal.lighten(pants, 0.25f), sh(pants));
                c.ellipse(bx, by - 12, 3, 3).stamp(Pal.WHITE, Pal.LIGHT);
                break;
            case Look.HAT_BAND:
                c.rect(9, (int) by - 4, 22, 2).stamp(BAND, sh(BAND));
                c.rect(18, (int) by - 2, 2, 5).rect(21, (int) by - 2, 2, 4).stamp(BAND, sh(BAND));
                break;
            case Look.HAT_CROWN:
                c.tri(12, 8, 16, 8, 12, 1).tri(16, 8, 24, 8, 20, 0).tri(24, 8, 28, 8, 28, 1).rect(12, 5, 16, 4).stamp(GOLD, sh(GOLD));
                c.set(20, 6, Pal.RED);
                break;
            case Look.HAT_WIZARD:
                c.tri(bx - 8, by - 6, bx + 8, by - 6, bx - 2, -2).stamp(WIZARD, sh(WIZARD));
                c.ellipse(bx, by - 6, 13, 2.4).stamp(WIZARD, sh(WIZARD));
                break;
            case Look.HAT_HELMET:
                c.ellipse(bx, by - 1, 11, 10.6).above((int) by + 7).stamp(METAL, sh(METAL));
                c.rect(19, 3, 2, 18).paint(Pal.lighten(METAL, 0.5f));
                break;
            default: break;
        }
        // weapon + fist
        if (weaponId >= 0) c.sprite(WeaponArt.icon(weaponId), 30, 2);
        c.ellipse(38, 17, 2.8, 2.8).stamp(skin, skinS);
        return c.toSprite();
    }

    private CharArt() {}
}
