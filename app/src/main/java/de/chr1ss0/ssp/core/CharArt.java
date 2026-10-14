package de.chr1ss0.ssp.core;

import java.util.HashMap;

/**
 * Procedurally builds the textured character sprites: front view (trainers,
 * editor) and back view (hero in battle), holding a 32x32 weapon icon.
 * Drawn with {@code g.sprite(s, x, y, 1)} (one art pixel = one logical pixel)
 * or with {@code spriteHD} at half size.
 */
public final class CharArt {
    public static final int FRONT_W = 80, FRONT_H = 96;
    public static final int BACK_W = 96, BACK_H = 84;

    private static final HashMap<String, Sprite> CACHE = new HashMap<String, Sprite>();

    private static final int SHOES = 0xFF3A3346;
    private static final int GOLD = 0xFFF2C14E;
    private static final int METAL = 0xFFB8C2D6;
    private static final int WIZARD = 0xFF6A4FD0;
    private static final int BAND = 0xFFE5484D;
    private static final int EYE_WHITE = 0xFFFBFBFF;
    private static final int LINE = 0xFF231A2A;
    private static final int MOUTH = 0xFF9C3B48;
    private static final int TONGUE = 0xFFE66A77;

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

    private static int tone(int c, float f) {
        return Shade.scaleColor(c, f);
    }

    /** Blends existing pixels of the mask toward a colour (soft details like blush). */
    private static void tintMask(Stamp c, int color, float amount) {
        // implemented via paint over a copy: read current pixels and mix
        int[] before = c.px.clone();
        c.paint(0xFF00FF01); // marker
        for (int i = 0; i < c.px.length; i++) {
            if (c.px[i] == 0xFF00FF01) c.px[i] = (before[i] >>> 24) == 0 ? 0 : Pal.mix(before[i], color, amount);
        }
    }

    // ================================================================== front

    private static Sprite buildFront(Look l, int weaponId) {
        Stamp c = new Stamp(FRONT_W, FRONT_H);
        int skin = Look.SKIN[l.skin];
        int hair = Look.HAIR_COLORS[l.hairColor];
        int shirt = Look.CLOTH[l.shirt];
        int pants = Look.CLOTH[l.pants];
        int eye = Look.EYE_COLORS[l.eyes];
        double hx = 48, hy = 35;
        int seed = l.key().hashCode();

        // ---- behind the body: long hair, ponytail, afro, hood
        switch (l.hair) {
            case 2: c.roundRect(hx - 21, hy - 10, 42, 42, 10).ellipse(hx, hy - 2, 20, 19).shaded(hair, 0, seed); break;
            case 3: c.ellipse(hx + 20, hy - 1, 7, 15).shaded(hair, 0, seed); break;
            case 4: c.ellipse(hx, hy - 6, 27, 24).shaded(hair, 0.05f, seed); break;
            case 7: c.roundRect(hx - 21, hy - 8, 42, 30, 8).shaded(hair, 0, seed); break;
            default: break;
        }
        if (l.top == 1) c.ellipse(hx, 55, 17, 7).shaded(tone(shirt, 0.85f), 0.03f, seed);

        // ---- legs + shoes
        int legL = c.roundRect(37, 75, 11, 15, 3).shaded(pants, 0.03f, seed + 1);
        c.roundRect(48, 75, 11, 15, 3).shaded(pants, 0.03f, seed + 2);
        c.rect(40, 79, 1, 6).onlyRegion(legL).paint(tone(pants, 0.8f));
        c.roundRect(34, 87, 14, 8, 3).shaded(SHOES, 0, seed);
        c.roundRect(48, 87, 14, 8, 3).shaded(SHOES, 0, seed);
        c.rect(35, 93, 12, 1).rect(49, 93, 12, 1).paint(0xFFE8E6F0);

        // ---- torso
        int torso = c.roundRect(33, 53, 30, 27, 7).shaded(shirt, 0.035f, seed + 3);
        topDetailsFront(c, l.top, shirt, torso, seed);
        // belt line
        c.rect(34, 78, 28, 2).onlyRegion(torso).paint(tone(pants, 0.7f));

        // ---- hanging arm (viewer's right)
        c.roundRect(59, 55, 9, 15, 4).shaded(l.top == 0 ? shirt : tone(shirt, 0.95f), 0.03f, seed + 4);
        if (l.top == 0) c.roundRect(60, 67, 7, 9, 3).shaded(skin, 0, seed);
        c.ellipse(63.5, 78.5, 4.2, 4.2).shaded(skin, 0, seed);

        // ---- raised arm (viewer's left)
        c.line(36, 60, 27, 53, 8).shaded(shirt, 0.03f, seed + 5);
        c.line(24, 52, 21, 43, 6).shaded(l.top == 0 ? skin : tone(shirt, 0.92f), 0, seed);

        // ---- neck, ears, head
        int neck = c.roundRect(43, 46, 10, 10, 2).shaded(skin, 0, seed);
        c.rect(43, 46, 10, 3).onlyRegion(neck).paint(tone(skin, 0.8f));
        c.ellipse(31, 37, 3.6, 4.6).ellipse(65, 37, 3.6, 4.6).shaded(skin, 0, seed);
        int head = c.ellipse(hx, hy, 17, 16.5).shaded(skin, 0, seed);

        // ---- face
        face(c, l, hx, hy, eye, hair, skin, head);

        // ---- hair front, hat, extras
        frontHair(c, l.hair, hx, hy, hair, skin, seed);
        frontHat(c, l.hat, hx, hy, shirt, pants, seed);
        frontExtra(c, l.extra, hair);

        // ---- weapon + fist
        if (weaponId >= 0) c.sprite(WeaponArt.icon(weaponId), 5, 8);
        int fist = c.ellipse(21, 40, 5.2, 4.8).shaded(skin, 0, seed);
        c.rect(19, 37, 1, 4).rect(22, 37, 1, 4).onlyRegion(fist).paint(tone(skin, 0.78f));
        return c.toSprite();
    }

    private static void topDetailsFront(Stamp c, int top, int shirt, int torso, int seed) {
        int dark = tone(shirt, 0.72f), light = tone(shirt, 1.15f);
        switch (top) {
            case 1: // hoodie: pocket + strings
                c.roundRect(39, 67, 18, 9, 3).onlyRegion(torso).paint(tone(shirt, 0.86f));
                c.rect(39, 67, 18, 1).onlyRegion(torso).paint(dark);
                c.rect(44, 55, 1, 8).rect(51, 55, 1, 8).onlyRegion(torso).paint(0xFFF2F2F7);
                c.rect(44, 63, 1, 1).rect(51, 63, 1, 1).paint(0xFFC9C9D6);
                break;
            case 2: // open jacket over a white shirt
                c.poly(43, 53, 53, 53, 52, 80, 44, 80).onlyRegion(torso).paint(0xFFF4F4FA);
                c.rect(48, 56, 1, 24).onlyRegion(torso).paint(0xFFDADAE6);
                c.poly(43, 53, 46, 53, 44, 64, 41, 58).onlyRegion(torso).paint(light);
                c.poly(50, 53, 53, 53, 55, 58, 52, 64).onlyRegion(torso).paint(tone(shirt, 0.9f));
                c.rect(43, 57, 1, 23).rect(53, 57, 1, 23).onlyRegion(torso).paint(dark);
                break;
            default: // t-shirt: collar + chest stripe
                c.ellipse(48, 53.5, 6, 3.4).onlyRegion(torso).paint(dark);
                c.rect(34, 64, 28, 3).onlyRegion(torso).paint(light);
                break;
        }
    }

    private static void face(Stamp c, Look l, double hx, double hy, int eye, int hair, int skin, int head) {
        int[] ex = {41, 55};
        int ey = 39;
        for (int k = 0; k < 2; k++) {
            int x = ex[k];
            if (l.extra == 3 && k == 1) continue; // eye patch
            c.ellipse(x + 0.5, ey, 3.3, 4.3).paint(EYE_WHITE);
            c.ellipse(x + 0.5 + (k == 0 ? 0.6 : -0.6), ey + 0.6, 2.3, 3.3).paint(eye);
            c.ellipse(x + 0.5 + (k == 0 ? 0.6 : -0.6), ey + 1.0, 1.3, 2.0).paint(0xFF15131F);
            c.set(x - 1 + (k == 0 ? 1 : 0), ey - 2, 0xFFFFFFFF);
            c.set(x + (k == 0 ? 1 : 0), ey - 2, 0xFFFFFFFF);
            c.set(x + 1 + (k == 0 ? 1 : -1), ey + 3, Pal.lighten(eye, 0.4f));
            // upper lid + lashes
            c.line(x - 3, ey - 4, x + 3, ey - 4, 1).paint(LINE);
            if (k == 0) c.set(x - 4, ey - 5, LINE);
            else c.set(x + 5, ey - 5, LINE);
            // eyebrow
            int brow = Shade.scaleColor(hair, 0.6f);
            if (k == 0) c.line(x - 3, ey - 8, x + 2, ey - 9, 1).paint(brow);
            else c.line(x - 1, ey - 9, x + 4, ey - 8, 1).paint(brow);
        }
        // nose
        c.set(49, 44, tone(skin, 0.78f));
        c.set(48, 45, tone(skin, 0.85f));
        // mouth (smile)
        c.rect(45, 48, 7, 1).paint(MOUTH);
        c.rect(46, 49, 5, 1).paint(TONGUE);
        c.set(44, 47, MOUTH);
        c.set(52, 47, MOUTH);
        // blush
        c.ellipse(38, 45, 3, 1.6).onlyRegion(head);
        tintMask(c, 0xFFFF8E9E, 0.45f);
        c.ellipse(59, 45, 3, 1.6).onlyRegion(head);
        tintMask(c, 0xFFFF8E9E, 0.45f);
    }

    private static void hairShine(Stamp c, int id, double cx, double cy, double rx, double ry, int hair) {
        c.ellipse(cx, cy, rx, ry).cutEllipse(cx + 2, cy + 2, rx * 0.8, ry * 0.6).onlyRegion(id).paint(tone(hair, 1.32f));
    }

    private static void strands(Stamp c, int id, double hx, double top, double bottom, int hair, int n, double spread) {
        int dark = tone(hair, 0.7f);
        for (int i = 0; i < n; i++) {
            double t = n == 1 ? 0.5 : i / (double) (n - 1);
            int x0 = (int) (hx + (t - 0.5) * spread * 0.5);
            int x1 = (int) (hx + (t - 0.5) * spread);
            c.line(x0, (int) top, x1, (int) bottom, 1).onlyRegion(id).paint(dark);
        }
    }

    private static void frontHair(Stamp c, int style, double hx, double hy, int hair, int skin, int seed) {
        int top = (int) (hy - 6);
        int id;
        switch (style) {
            case 0: // short with fringe
                id = c.ellipse(hx, hy - 3, 19, 17.5).above(top).rect(29, top - 2, 5, 13).rect(62, top - 2, 5, 13)
                        .tri(32, top - 1, 40, top - 1, 35, top + 6).tri(39, top - 1, 48, top - 1, 44, top + 5)
                        .tri(47, top - 1, 56, top - 1, 52, top + 6).tri(55, top - 1, 64, top - 1, 61, top + 5)
                        .shaded(hair, 0.03f, seed);
                strands(c, id, hx, hy - 18, hy - 7, hair, 5, 30);
                hairShine(c, id, hx - 5, hy - 12, 10, 4, hair);
                break;
            case 1: // spiky
                id = c.tri(26, top + 4, 36, top - 2, 22, hy - 22).tri(32, top - 4, 44, top - 6, 33, hy - 30)
                        .tri(42, top - 6, 54, top - 6, 47, hy - 33).tri(52, top - 6, 64, top - 4, 62, hy - 30)
                        .tri(60, top - 2, 70, top + 4, 74, hy - 22)
                        .ellipse(hx, hy - 3, 19, 17.5).above(top).rect(29, top - 2, 5, 12).rect(62, top - 2, 5, 12)
                        .tri(31, top - 1, 41, top - 1, 36, top + 7).tri(40, top - 1, 50, top - 1, 46, top + 6)
                        .tri(49, top - 1, 59, top - 1, 54, top + 7).tri(57, top - 1, 66, top - 1, 62, top + 5)
                        .shaded(hair, 0.03f, seed);
                strands(c, id, hx, hy - 26, hy - 8, hair, 6, 40);
                hairShine(c, id, hx - 6, hy - 14, 10, 4, hair);
                break;
            case 2: // long
                id = c.ellipse(hx, hy - 3, 19.5, 17.5).above(top).rect(28, top - 2, 7, 26).rect(61, top - 2, 7, 26)
                        .tri(34, top - 1, 50, top - 1, 40, top + 6).shaded(hair, 0.03f, seed);
                strands(c, id, hx, hy - 18, hy - 6, hair, 5, 30);
                c.line(31, top + 4, 31, top + 22, 1).line(64, top + 4, 64, top + 22, 1).onlyRegion(id).paint(tone(hair, 0.7f));
                hairShine(c, id, hx - 5, hy - 12, 10, 4, hair);
                break;
            case 3: // ponytail
                id = c.ellipse(hx, hy - 3, 19, 17.5).above(top).rect(29, top - 2, 5, 10).rect(62, top - 2, 5, 10)
                        .tri(33, top - 1, 52, top - 1, 38, top + 6).shaded(hair, 0.03f, seed);
                strands(c, id, hx, hy - 18, hy - 7, hair, 4, 26);
                c.roundRect(64, top - 4, 5, 5, 2).shaded(BAND, 0, seed);
                hairShine(c, id, hx - 5, hy - 12, 10, 4, hair);
                break;
            case 4: // afro
                id = c.ellipse(hx, hy - 7, 21, 17).above(top + 1).shaded(hair, 0.06f, seed);
                for (int i = 0; i < 14; i++) {
                    int x = 30 + (i * 37) % 36, y = (int) (hy - 20 + (i * 13) % 14);
                    c.ellipse(x, y, 2, 1.5).onlyRegion(id).paint(tone(hair, 0.78f));
                }
                hairShine(c, id, hx - 6, hy - 16, 9, 4, hair);
                break;
            case 5: // mohawk
                id = c.roundRect(hx - 5, hy - 36, 10, 26, 5).shaded(hair, 0.03f, seed);
                strands(c, id, hx, hy - 34, hy - 12, hair, 2, 4);
                break;
            case 6: // bald shine
                c.ellipse(hx - 6, hy - 10, 4, 2.2).paint(Pal.lighten(skin, 0.45f));
                break;
            default: // bob
                id = c.ellipse(hx, hy - 3, 20, 18).above(top + 2).rect(28, top - 2, 7, 20).rect(61, top - 2, 7, 20)
                        .shaded(hair, 0.03f, seed);
                c.rect(33, top + 1, 30, 1).onlyRegion(id).paint(tone(hair, 0.72f));
                strands(c, id, hx, hy - 18, hy - 5, hair, 6, 32);
                hairShine(c, id, hx - 5, hy - 12, 11, 4, hair);
                break;
        }
    }

    private static void frontHat(Stamp c, int hat, double hx, double hy, int shirt, int pants, int seed) {
        int top = (int) (hy - 6);
        switch (hat) {
            case Look.HAT_CAP: {
                int id = c.ellipse(hx, hy - 7, 19, 14).above(top - 1).shaded(shirt, 0.02f, seed);
                c.roundRect(27, top - 4, 42, 5, 2).shaded(tone(shirt, 0.8f), 0, seed);
                c.ellipse(hx, hy - 17, 5, 4).onlyRegion(id).paint(0xFFFFFFFF);
                c.ellipse(hx, hy - 17, 2.5, 2).onlyRegion(id).paint(BAND);
                break;
            }
            case Look.HAT_BEANIE: {
                int id = c.ellipse(hx, hy - 6, 19.5, 17).above(top + 1).shaded(pants, 0.04f, seed);
                for (int x = 31; x < 66; x += 3) c.rect(x, (int) (hy - 21), 1, 14).onlyRegion(id).paint(tone(pants, 0.82f));
                c.roundRect(28, top - 3, 40, 6, 3).shaded(Pal.lighten(pants, 0.25f), 0.03f, seed);
                c.ellipse(hx, hy - 24, 5, 5).shaded(0xFFF4F4FA, 0.05f, seed);
                break;
            }
            case Look.HAT_BAND:
                c.roundRect(29, top - 4, 38, 5, 2).shaded(BAND, 0, seed);
                c.tri(66, top - 4, 66, top + 1, 74, top + 4).tri(66, top - 3, 66, top + 1, 72, top - 7).shaded(BAND, 0, seed);
                break;
            case Look.HAT_CROWN: {
                int id = c.tri(32, 18, 39, 18, 32, 4).tri(38, 18, 50, 18, 44, 1).tri(49, 18, 58, 18, 54, 3)
                        .tri(56, 18, 64, 18, 64, 4).rect(32, 14, 32, 7).shaded(GOLD, 0, seed);
                c.ellipse(48, 17, 2.4, 2.4).paint(0xFFE5484D);
                c.ellipse(38, 17, 1.8, 1.8).paint(0xFF3D7BFF);
                c.ellipse(58, 17, 1.8, 1.8).paint(0xFF3D7BFF);
                c.rect(33, 15, 30, 1).onlyRegion(id).paint(tone(GOLD, 1.25f));
                break;
            }
            case Look.HAT_WIZARD: {
                int id = c.tri(hx - 15, hy - 12, hx + 15, hy - 12, hx + 8, -2).shaded(WIZARD, 0.03f, seed);
                c.ellipse(hx, hy - 12, 24, 4.5).shaded(WIZARD, 0.03f, seed);
                c.poly(54, 8, 56, 12, 60, 12, 57, 15, 58, 19, 54, 16, 50, 19, 51, 15, 48, 12, 52, 12).onlyRegion(id).paint(GOLD);
                break;
            }
            case Look.HAT_HELMET: {
                int id = c.ellipse(hx, hy - 3, 20, 18.5).above(top + 2).rect(28, top, 6, 18).rect(62, top, 6, 18)
                        .shaded(METAL, 0.04f, seed);
                c.rect(46, (int) (hy - 21), 4, 16).onlyRegion(id).paint(Pal.lighten(METAL, 0.5f));
                c.ellipse(hx - 7, hy - 13, 4, 2).onlyRegion(id).paint(0xFFFFFFFF);
                break;
            }
            default: break;
        }
    }

    private static void frontExtra(Stamp c, int extra, int hair) {
        switch (extra) {
            case 1: // glasses
                c.rect(37, 35, 9, 8).rect(51, 35, 9, 8);
                tintMask(c, 0xFFE8F4FF, 0.3f);
                c.roundRect(36, 34, 11, 10, 3).cut(37, 35, 9, 8).roundRect(50, 34, 11, 10, 3).cut(51, 35, 9, 8)
                        .rect(47, 37, 3, 1).paint(0xFF2B2F45);
                break;
            case 2: // sunglasses
                c.roundRect(35, 35, 12, 8, 3).roundRect(49, 35, 12, 8, 3).shaded(0xFF20232F, 0, 0);
                c.rect(47, 37, 2, 1).paint(0xFF20232F);
                c.rect(37, 36, 4, 1).rect(51, 36, 4, 1).paint(0xFF6F7CB8);
                break;
            case 3: // eye patch
                c.ellipse(55.5, 39, 4.5, 4.5).shaded(0xFF1C1C24, 0, 0);
                c.line(31, 31, 66, 33, 1).paint(0xFF1C1C24);
                break;
            case 4: // moustache
                c.poly(41, 47, 48, 45, 55, 47, 53, 49, 48, 47, 43, 49).shaded(Shade.scaleColor(hair, 0.8f), 0, 0);
                break;
            default: break;
        }
    }

    // ================================================================== back

    private static Sprite buildBack(Look l, int weaponId) {
        Stamp c = new Stamp(BACK_W, BACK_H);
        int skin = Look.SKIN[l.skin];
        int hair = Look.HAIR_COLORS[l.hairColor];
        int shirt = Look.CLOTH[l.shirt];
        int pants = Look.CLOTH[l.pants];
        double bx = 40, by = 34;
        int seed = l.key().hashCode() + 99;

        // ---- torso (cut off by the bottom of the canvas)
        int torso = c.roundTop(8, 58, 66, 28, 18).shaded(shirt, 0.035f, seed);
        c.rect(40, 66, 1, 18).onlyRegion(torso).paint(tone(shirt, 0.78f));
        if (l.top == 1) {
            c.ellipse(40, 62, 17, 10).shaded(tone(shirt, 0.88f), 0.03f, seed + 1);
            c.ellipse(40, 61, 11, 5).paint(tone(shirt, 0.65f));
        } else if (l.top == 2) {
            c.rect(14, 72, 54, 2).onlyRegion(torso).paint(tone(shirt, 0.8f));
            c.roundRect(30, 58, 20, 5, 2).shaded(tone(shirt, 0.85f), 0, seed);
        } else {
            c.ellipse(40, 59, 9, 3).onlyRegion(torso).paint(tone(shirt, 0.72f));
        }
        // ---- raised arm (viewer's right)
        c.line(64, 67, 78, 56, 9).shaded(shirt, 0.03f, seed + 2);
        c.line(80, 54, 80, 42, 7).shaded(l.top == 0 ? skin : tone(shirt, 0.92f), 0, seed);

        // ---- neck, ears, head
        c.roundRect(32, 48, 16, 14, 3).shaded(skin, 0, seed);
        c.ellipse(18.5, 38, 4, 5.5).ellipse(61.5, 38, 4, 5.5).shaded(skin, 0, seed);
        c.ellipse(bx, by, 21, 20.5).shaded(skin, 0, seed);

        // ---- hair (back)
        int id;
        int dark = tone(hair, 0.7f);
        switch (l.hair) {
            case 0:
                id = c.ellipse(bx, by - 1, 22, 21.5).above((int) by + 13).shaded(hair, 0.03f, seed);
                backStrands(c, id, bx, by, dark, 7);
                hairShine(c, id, bx - 8, by - 12, 12, 5, hair);
                break;
            case 1:
                id = c.tri(16, 26, 26, 16, 6, 6).tri(24, 16, 36, 12, 22, -2).tri(34, 12, 46, 12, 41, -4)
                        .tri(44, 12, 56, 16, 58, -1).tri(54, 16, 64, 26, 74, 6)
                        .ellipse(bx, by - 1, 22, 21.5).above((int) by + 13).shaded(hair, 0.03f, seed);
                backStrands(c, id, bx, by, dark, 8);
                hairShine(c, id, bx - 8, by - 12, 12, 5, hair);
                break;
            case 2:
                id = c.ellipse(bx, by - 1, 22, 21.5).roundRect(bx - 21, by, 42, 36, 9).shaded(hair, 0.03f, seed);
                backStrands(c, id, bx, by + 18, dark, 8);
                hairShine(c, id, bx - 8, by - 12, 12, 5, hair);
                break;
            case 3:
                id = c.ellipse(bx, by - 1, 22, 21.5).above((int) by + 13).shaded(hair, 0.03f, seed);
                backStrands(c, id, bx, by, dark, 6);
                int tail = c.ellipse(bx, by + 21, 6.5, 14).shaded(hair, 0.03f, seed);
                c.line((int) bx, (int) by + 12, (int) bx, (int) by + 32, 1).onlyRegion(tail).paint(dark);
                c.roundRect(bx - 5, by + 8, 10, 4, 2).shaded(BAND, 0, seed);
                hairShine(c, id, bx - 8, by - 12, 12, 5, hair);
                break;
            case 4:
                id = c.ellipse(bx, by - 4, 28, 25).shaded(hair, 0.06f, seed);
                for (int i = 0; i < 22; i++) {
                    int x = 16 + (i * 41) % 48, y = 10 + (i * 17) % 40;
                    c.ellipse(x, y, 2.3, 1.7).onlyRegion(id).paint(tone(hair, 0.78f));
                }
                hairShine(c, id, bx - 9, by - 16, 12, 5, hair);
                break;
            case 5:
                id = c.roundRect(bx - 5, 0, 10, by + 14, 5).shaded(hair, 0.03f, seed);
                c.line((int) bx, 2, (int) bx, (int) by + 10, 1).onlyRegion(id).paint(dark);
                break;
            case 6:
                c.ellipse(bx - 9, by - 12, 5, 3).paint(Pal.lighten(skin, 0.45f));
                break;
            default:
                id = c.ellipse(bx, by - 1, 22.5, 22).above((int) by + 19).shaded(hair, 0.03f, seed);
                c.rect(19, (int) by + 17, 42, 1).onlyRegion(id).paint(dark);
                backStrands(c, id, bx, by + 6, dark, 8);
                hairShine(c, id, bx - 8, by - 12, 12, 5, hair);
                break;
        }

        // ---- hat (back)
        switch (l.hat) {
            case Look.HAT_CAP: {
                int cap = c.ellipse(bx, by - 5, 22.5, 18).above((int) by - 1).shaded(shirt, 0.02f, seed);
                c.roundRect(bx - 6, by - 7, 12, 5, 2).onlyRegion(cap).paint(tone(shirt, 0.6f));
                c.rect((int) bx - 4, (int) by - 6, 8, 2).onlyRegion(cap).paint(skin);
                break;
            }
            case Look.HAT_BEANIE: {
                int b = c.ellipse(bx, by - 3, 23, 20).above((int) by + 3).shaded(pants, 0.04f, seed);
                for (int x = 20; x < 62; x += 3) c.rect(x, 12, 1, (int) by - 10).onlyRegion(b).paint(tone(pants, 0.82f));
                c.roundRect(17, by - 4, 46, 7, 3).shaded(Pal.lighten(pants, 0.25f), 0.03f, seed);
                c.ellipse(bx, by - 25, 6, 6).shaded(0xFFF4F4FA, 0.05f, seed);
                break;
            }
            case Look.HAT_BAND:
                c.roundRect(18, by - 9, 44, 5, 2).shaded(BAND, 0, seed);
                c.poly(37, by - 6, 41, by - 6, 39, by + 8, 34, by + 7).poly(40, by - 6, 44, by - 6, 47, by + 6, 42, by + 7).shaded(BAND, 0, seed);
                break;
            case Look.HAT_CROWN: {
                c.tri(22, 14, 30, 14, 22, 0).tri(29, 14, 51, 14, 40, -2).tri(50, 14, 58, 14, 58, 0).rect(22, 9, 36, 8).shaded(GOLD, 0, seed);
                c.ellipse(40, 13, 2.4, 2.4).paint(0xFFE5484D);
                break;
            }
            case Look.HAT_WIZARD:
                c.tri(bx - 17, by - 12, bx + 17, by - 12, bx - 4, -6).shaded(WIZARD, 0.03f, seed);
                c.ellipse(bx, by - 12, 28, 5).shaded(WIZARD, 0.03f, seed);
                break;
            case Look.HAT_HELMET: {
                int hm = c.ellipse(bx, by - 1, 23, 22).above((int) by + 15).shaded(METAL, 0.04f, seed);
                c.rect(38, 4, 4, (int) by + 8).onlyRegion(hm).paint(Pal.lighten(METAL, 0.5f));
                break;
            }
            default: break;
        }

        // ---- weapon + fist
        if (weaponId >= 0) c.sprite(WeaponArt.icon(weaponId), 63, 4);
        int fist = c.ellipse(80, 37, 6, 5.6).shaded(skin, 0, seed);
        c.rect(77, 33, 1, 5).rect(81, 33, 1, 5).onlyRegion(fist).paint(tone(skin, 0.8f));
        return c.toSprite();
    }

    private static void backStrands(Stamp c, int id, double bx, double by, int dark, int n) {
        for (int i = 0; i < n; i++) {
            double t = i / (double) (n - 1) - 0.5;
            int x0 = (int) (bx + t * 10), x1 = (int) (bx + t * 40);
            c.line(x0, (int) (by - 18), x1, (int) (by + 12), 1).onlyRegion(id).paint(dark);
        }
    }

    private CharArt() {}
}
