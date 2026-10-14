package de.chr1ss0.ssp.core;

import java.util.HashMap;

/**
 * Procedurally builds the battle creatures: front view (opponents, editor,
 * title) and back view (the player's creature in battle), holding a 32x32
 * weapon icon. Every part is stamped with lighting, fur texture and a tinted
 * outline. Drawn with {@code g.sprite(s, x, y, 1)} or {@code spriteHD} at half size.
 */
public final class CharArt {
    public static final int FRONT_W = 80, FRONT_H = 96;
    public static final int BACK_W = 96, BACK_H = 84;

    private static final HashMap<String, Sprite> CACHE = new HashMap<String, Sprite>();

    private static final int LINE = 0xFF231A2A;
    private static final int BONE = 0xFFF3E6C4;
    private static final int LEAF = 0xFF5BC25A;
    private static final int FLAME = 0xFFF5772F, FLAME_IN = 0xFFFFD34D;
    private static final int CRYSTAL = 0xFF7FE0F0;
    private static final int GEM = 0xFFE5484D;
    private static final int BEAK = 0xFFF5A623;
    private static final int GOLD = 0xFFF2C14E;
    private static final int METAL = 0xFFB8C2D6;
    private static final int WIZARD = 0xFF6A4FD0;
    private static final int BAND = 0xFFE5484D;
    private static final int MOUTH = 0xFF8C2F3E, TONGUE = 0xFFE66A77;
    private static final int INNER_EAR = 0xFFFFB3C6;
    private static final int NOSE = 0xFFE07A93;
    /** Accent colour (caps, scarves) per body colour. */
    private static final int[] ACCENT = {1, 0, 5, 1, 3, 9, 0, 0, 4, 5, 2, 0};

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

    /** Blends the pixels under the current mask toward a colour (soft details like blush). */
    private static void tintMask(Stamp c, int color, float amount) {
        int[] before = c.px.clone();
        c.paint(0xFF00FF01); // marker
        for (int i = 0; i < c.px.length; i++) {
            if (c.px[i] == 0xFF00FF01) c.px[i] = (before[i] >>> 24) == 0 ? 0 : Pal.mix(before[i], color, amount);
        }
    }

    /** Drawing context: canvas, colours and the placement of head and body. */
    private static final class Ctx {
        Stamp c;
        Look l;
        boolean back;
        int col, dark, tipDark, belly, accent, seed;
        /** Head centre, unit size and radii. */
        double hx, hy, u, hrx, hry;
        /** Body ellipse. */
        double bx, by, brx, bry;
        /** Head and body are a single round shape (Knuddel, Vogel). */
        boolean round;
        int head = -1, body = -1, ears = -1, tail = -1;
        /** Ear tips (x, y, r) for the tips pattern. */
        double[] tips = new double[0];
        double tailTipX, tailTipY;
        /** Eye centres (front). */
        double ex0, ex1, ey;

        double X(double d) {
            return hx + d * u;
        }

        double Y(double d) {
            return hy + d * u;
        }

        double top() {
            return hy - hry;
        }
    }

    private static Ctx ctx(Stamp c, Look l, boolean back) {
        Ctx k = new Ctx();
        k.c = c;
        k.l = l;
        k.back = back;
        k.col = Look.COLORS[l.color];
        k.dark = tone(k.col, 0.72f);
        k.tipDark = tone(k.col, 0.5f);
        k.belly = Look.BELLY[l.belly];
        k.accent = Look.COLORS[ACCENT[l.color]];
        k.seed = l.key().hashCode() + (back ? 77 : 0);
        k.round = l.species == Look.KNUDDEL || l.species == Look.VOGEL;
        return k;
    }

    // ================================================================== front

    private static Sprite buildFront(Look l, int weaponId) {
        Ctx k = ctx(new Stamp(FRONT_W, FRONT_H), l, false);
        Stamp c = k.c;
        int sp = l.species;
        k.u = 1;
        switch (sp) {
            case Look.KNUDDEL:
                k.bx = 49; k.by = 67; k.brx = 24; k.bry = 24;
                k.hx = 49; k.hy = 60; k.hrx = 20; k.hry = 17;
                k.ex0 = 40; k.ex1 = 58; k.ey = 59;
                break;
            case Look.VOGEL:
                k.bx = 49; k.by = 65; k.brx = 22; k.bry = 25;
                k.hx = 49; k.hy = 57; k.hrx = 19; k.hry = 17;
                k.ex0 = 41; k.ex1 = 57; k.ey = 55;
                break;
            case Look.FROSCH:
                k.bx = 49; k.by = 79; k.brx = 16; k.bry = 12;
                k.hx = 49; k.hy = 54; k.hrx = 23; k.hry = 14;
                k.ex0 = 37; k.ex1 = 61; k.ey = 42;
                break;
            default:
                k.bx = 49; k.by = 77; k.brx = sp == Look.BAER ? 18 : 16; k.bry = sp == Look.BAER ? 15 : 14;
                k.hx = 49; k.hy = 46; k.hrx = 20; k.hry = 17;
                k.ex0 = 41; k.ex1 = 57; k.ey = sp == Look.BAER ? 45 : 47;
                break;
        }

        // ---- behind everything: tail, wings, ears, crest feathers
        tail(k, k.bx + k.brx * 0.55, k.by + k.bry * 0.45, 1, 1);
        if (sp == Look.DRACHE) wings(k);
        ears(k);
        if (sp == Look.VOGEL) {
            c.ellipse(k.hx - 5, k.top() - 3, 2.6, 6).ellipse(k.hx, k.top() - 5, 2.8, 7.5).ellipse(k.hx + 5, k.top() - 2, 2.4, 5.5)
                    .shaded(tone(k.col, 0.9f), 0.04f, k.seed + 5);
        }

        // ---- body (or the single round shape)
        if (k.round) {
            c.ellipse(k.bx, k.by, k.brx, k.bry);
            if (sp == Look.KNUDDEL) {
                double t = k.by - k.bry;
                c.tri(k.hx - 6, t + 3, k.hx - 1, t + 2, k.hx - 7, t - 6).tri(k.hx - 2, t + 2, k.hx + 3, t + 2, k.hx + 1, t - 8)
                        .tri(k.hx + 2, t + 2, k.hx + 7, t + 3, k.hx + 8, t - 5);
            }
            k.body = k.head = c.shaded(k.col, 0.05f, k.seed);
            bellyPatch(k, k.bx, k.by + k.bry * 0.38, k.brx * 0.62, k.bry * 0.5);
        } else {
            k.body = c.ellipse(k.bx, k.by, k.brx, k.bry).shaded(k.col, 0.05f, k.seed + 1);
            bellyPatch(k, k.bx, k.by + 2, k.brx * 0.62, k.bry * 0.72);
            if (sp == Look.DRACHE) {
                for (int i = -1; i <= 2; i++) c.rect((int) k.bx - 6, (int) k.by + i * 4, 12, 1).onlyRegion(k.body).paint(tone(k.belly, 0.84f));
            }
        }

        // ---- feet, hanging arm, raised arm
        feet(k);
        if (sp == Look.VOGEL) {
            c.poly(k.bx + k.brx - 7, k.by - 10, k.bx + k.brx + 4, k.by - 1, k.bx + k.brx - 3, k.by + 9).shaded(tone(k.col, 0.9f), 0.04f, k.seed + 6);
        } else {
            double ax = k.round ? k.bx + k.brx - 2 : k.bx + k.brx - 1, ay = k.round ? k.by + 2 : k.by - 2;
            c.ellipse(ax, ay, 4.4, 6.2).shaded(k.col, 0.04f, k.seed + 7);
        }
        int shX = k.round ? 33 : 38, shY = k.round ? 64 : (sp == Look.FROSCH ? 73 : 69);
        c.line(shX, shY, 22, 45, 7).shaded(k.col, 0.04f, k.seed + 8);

        // ---- head
        if (!k.round) {
            head(k);
        }
        face(k);
        pattern(k);

        // ---- crest, hat, extras
        crest(k);
        hat(k);
        extraFront(k);

        // ---- weapon + paw
        if (weaponId >= 0) c.sprite(WeaponArt.icon(weaponId), 5, 9);
        int paw = c.ellipse(21, 42, 5.2, 4.8).shaded(k.col, 0.04f, k.seed + 9);
        c.rect(19, 39, 1, 4).rect(22, 39, 1, 4).onlyRegion(paw).paint(tone(k.col, 0.78f));
        return c.toSprite();
    }

    private static void bellyPatch(Ctx k, double x, double y, double rx, double ry) {
        Stamp c = k.c;
        c.ellipse(x + 0.6, y + 0.8, rx, ry).onlyRegion(k.body).paint(tone(k.belly, 0.9f));
        c.ellipse(x, y, rx, ry).onlyRegion(k.body).paint(k.belly);
    }

    private static void head(Ctx k) {
        Stamp c = k.c;
        int sp = k.l.species;
        c.ellipse(k.hx, k.hy, k.hrx, k.hry);
        switch (sp) {
            case Look.FROSCH:
                c.ellipse(k.X(-12), k.Y(-12), 8, 8).ellipse(k.X(12), k.Y(-12), 8, 8);
                break;
            case Look.FUCHS:
                c.tri(k.X(-21), k.Y(5), k.X(-14), k.Y(-1), k.X(-14), k.Y(11)).tri(k.X(21), k.Y(5), k.X(14), k.Y(-1), k.X(14), k.Y(11));
                break;
            case Look.DRACHE:
                if (!k.back) c.ellipse(k.hx, k.Y(8), 10, 6.5);
                break;
            default:
                break;
        }
        k.head = c.shaded(k.col, 0.05f, k.seed + 2);
    }

    /** Eyes, muzzle, mouth and cheeks of the front view. */
    private static void face(Ctx k) {
        Stamp c = k.c;
        int sp = k.l.species;
        int head = k.head;
        double hx = k.hx;
        // muzzles first (eyes may overlap them)
        switch (sp) {
            case Look.FUCHS:
                c.ellipse(hx, k.hy + 8, 11, 6.5).onlyRegion(head).paint(k.belly);
                break;
            case Look.BAER:
                c.ellipse(hx, k.hy + 8, 8, 5.6).onlyRegion(head).paint(k.belly);
                break;
            case Look.KATZE:
            case Look.HASE:
                c.ellipse(hx - 2.6, k.hy + 8, 3.4, 2.6).ellipse(hx + 2.6, k.hy + 8, 3.4, 2.6).onlyRegion(head).paint(k.belly);
                break;
            case Look.DRACHE:
                c.ellipse(hx, k.hy + 9, 8, 4).onlyRegion(head).paint(Pal.mix(k.col, k.belly, 0.45f));
                break;
            default:
                break;
        }
        if (k.l.extra != Look.EX_SHADES) {
            int iris = Look.EYE_COLORS[k.l.eyes];
            eye(c, k.ex0, k.ey, iris);
            eye(c, k.ex1, k.ey, iris);
        }
        // nose + mouth
        int my;
        switch (sp) {
            case Look.KNUDDEL:
                my = 67;
                c.ellipse(hx, my, 3.2, 2.6).paint(MOUTH);
                c.ellipse(hx, my + 1.2, 2, 1.2).paint(TONGUE);
                break;
            case Look.VOGEL: {
                my = 61;
                int beak = c.poly(hx - 5, my - 2, hx + 5, my - 2, hx, my + 6).shaded(BEAK, 0, k.seed + 3);
                c.rect((int) hx - 4, my + 1, 9, 1).onlyRegion(beak).paint(tone(BEAK, 0.7f));
                break;
            }
            case Look.FROSCH:
                my = (int) k.hy + 5;
                c.rect((int) hx - 9, my, 19, 1).paint(MOUTH);
                c.set((int) hx - 10, my - 1, MOUTH);
                c.set((int) hx + 10, my - 1, MOUTH);
                break;
            case Look.DRACHE:
                my = (int) k.hy + 11;
                c.set((int) hx - 3, (int) k.hy + 7, LINE);
                c.set((int) hx + 3, (int) k.hy + 7, LINE);
                c.rect((int) hx - 4, my, 9, 1).paint(MOUTH);
                c.set((int) hx - 5, my - 1, MOUTH);
                c.set((int) hx + 5, my - 1, MOUTH);
                c.set((int) hx + 2, my + 1, 0xFFFFFFFF);
                break;
            default: {
                my = (int) k.hy + 9;
                // nose
                if (sp == Look.BAER || sp == Look.FUCHS) c.ellipse(hx, k.hy + 5.5, 2.6, 1.8).paint(LINE);
                else c.tri(hx - 1.8, k.hy + 5.5, hx + 1.8, k.hy + 5.5, hx, k.hy + 7.5).paint(NOSE);
                // "w" mouth
                int x = (int) hx;
                c.set(x, my - 1, LINE);
                c.set(x - 1, my, LINE); c.set(x - 2, my, LINE); c.set(x - 3, my - 1, LINE);
                c.set(x + 1, my, LINE); c.set(x + 2, my, LINE); c.set(x + 3, my - 1, LINE);
                if (sp == Look.HASE) c.rect(x - 1, my + 1, 3, 2).paint(0xFFFFFFFF);
                if (sp == Look.KATZE) {
                    int w = tone(k.col, 0.55f);
                    c.line(x - 11, my - 2, x - 19, my - 3, 1).line(x - 11, my, x - 19, my + 1, 1)
                            .line(x + 11, my - 2, x + 19, my - 3, 1).line(x + 11, my, x + 19, my + 1, 1).paint(w);
                }
                break;
            }
        }
        // cheeks
        double cy = k.ey + 6;
        c.ellipse(k.ex0 - 4, cy, 3.2, 1.8).onlyRegion(head);
        tintMask(c, 0xFFFF7E96, 0.5f);
        c.ellipse(k.ex1 + 4, cy, 3.2, 1.8).onlyRegion(head);
        tintMask(c, 0xFFFF7E96, 0.5f);
    }

    private static void eye(Stamp c, double x, double y, int iris) {
        c.ellipse(x, y, 3.9, 4.9).paint(LINE);
        c.ellipse(x, y + 0.3, 3.0, 4.0).paint(iris);
        c.ellipse(x, y + 0.9, 1.8, 2.6).paint(Shade.scaleColor(iris, 0.32f));
        c.ellipse(x - 1.1, y - 1.6, 1.4, 1.6).paint(0xFFFFFFFF);
        c.set((int) (x + 1.3), (int) (y + 2.3), Pal.lighten(iris, 0.55f));
    }

    private static void feet(Ctx k) {
        Stamp c = k.c;
        int sp = k.l.species;
        int footCol = k.l.pattern == Look.PAT_TIPS ? tone(k.col, 0.6f) : tone(k.col, 0.95f);
        double fy = k.by + k.bry - 1;
        if (sp == Look.VOGEL) {
            for (int s = -1; s <= 1; s += 2) {
                int x = (int) (k.bx + s * 8), y = (int) fy;
                c.line(x - 3, y + 3, x, y - 1, 2).line(x, y - 1, x, y + 4, 2).line(x + 3, y + 3, x, y - 1, 2).shaded(BEAK, 0, k.seed);
            }
            return;
        }
        double rx = sp == Look.FROSCH ? 7.5 : (sp == Look.KNUDDEL ? 5.5 : 6.2), ry = sp == Look.FROSCH ? 3.4 : 3.8;
        double dx = sp == Look.KNUDDEL ? 10 : 8.5;
        int f = c.ellipse(k.bx - dx, fy, rx, ry).ellipse(k.bx + dx, fy, rx, ry).shaded(footCol, 0.03f, k.seed + 4);
        int toe = tone(footCol, 0.7f);
        for (int s = -1; s <= 1; s += 2) {
            int x = (int) (k.bx + s * dx);
            c.rect(x - 2, (int) fy, 1, 3).rect(x + 1, (int) fy, 1, 3).onlyRegion(f).paint(toe);
        }
    }

    // ================================================================== back

    private static Sprite buildBack(Look l, int weaponId) {
        Ctx k = ctx(new Stamp(BACK_W, BACK_H), l, true);
        Stamp c = k.c;
        int sp = l.species;
        k.u = 1.25;
        if (k.round) {
            k.bx = 40; k.by = 60; k.brx = sp == Look.VOGEL ? 31 : 34; k.bry = 33;
            k.hx = 40; k.hy = 45; k.hrx = 27; k.hry = 18;
        } else if (sp == Look.FROSCH) {
            k.bx = 40; k.by = 84; k.brx = 30; k.bry = 24;
            k.hx = 40; k.hy = 44; k.hrx = 28; k.hry = 17;
        } else {
            k.bx = 40; k.by = 82; k.brx = sp == Look.BAER ? 32 : 30; k.bry = 25;
            k.hx = 40; k.hy = 36; k.hrx = 25; k.hry = 21;
        }

        tail(k, 21, 74, -1, 1.3);
        ears(k);
        if (sp == Look.VOGEL) {
            c.ellipse(k.hx - 6, k.top() - 3, 3.2, 7.5).ellipse(k.hx, k.top() - 6, 3.5, 9).ellipse(k.hx + 6, k.top() - 2, 3, 7)
                    .shaded(tone(k.col, 0.9f), 0.04f, k.seed + 5);
        }
        if (k.round) {
            c.ellipse(k.bx, k.by, k.brx, k.bry);
            if (sp == Look.KNUDDEL) {
                double t = k.by - k.bry;
                c.tri(k.hx - 8, t + 4, k.hx - 1, t + 3, k.hx - 9, t - 8).tri(k.hx - 3, t + 3, k.hx + 4, t + 3, k.hx + 1, t - 10)
                        .tri(k.hx + 3, t + 3, k.hx + 9, t + 4, k.hx + 10, t - 7);
            }
            k.body = k.head = c.shaded(k.col, 0.05f, k.seed);
        } else {
            k.body = c.ellipse(k.bx, k.by, k.brx, k.bry).shaded(k.col, 0.05f, k.seed + 1);
            if (sp == Look.DRACHE) wings(k);
        }
        // raised arm (viewer's right)
        int shX = k.round ? 64 : 61, shY = k.round ? 62 : 70;
        int armCol = sp == Look.VOGEL ? tone(k.col, 0.92f) : k.col;
        c.line(shX, shY, 72, 56, 11).line(72, 56, 79, 45, 9).ellipse(72, 56, 5.5, 5.5).shaded(armCol, 0.04f, k.seed + 8);
        if (!k.round) head(k);
        if (sp == Look.DRACHE) {
            // spine ridge
            for (int i = 0; i < 5; i++) {
                double y = k.hy + 2 + i * 8;
                c.tri(k.hx - 3.5, y + 3, k.hx + 3.5, y + 3, k.hx, y - 4);
            }
            c.shaded(BONE, 0, k.seed + 12);
        }
        pattern(k);
        crest(k);
        hat(k);
        extraBack(k);

        if (weaponId >= 0) c.sprite(WeaponArt.icon(weaponId), 63, 4);
        int paw = c.ellipse(80, 38, 6.2, 5.8).shaded(k.col, 0.04f, k.seed + 9);
        c.rect(77, 34, 1, 5).rect(81, 34, 1, 5).onlyRegion(paw).paint(tone(k.col, 0.78f));
        return c.toSprite();
    }

    // ================================================================== shared parts

    private static void ears(Ctx k) {
        Stamp c = k.c;
        double u = k.u;
        switch (k.l.species) {
            case Look.KATZE:
                c.tri(k.X(-18), k.Y(-6), k.X(-5), k.Y(-15), k.X(-17), k.Y(-28))
                        .tri(k.X(18), k.Y(-6), k.X(5), k.Y(-15), k.X(17), k.Y(-28));
                k.ears = c.shaded(k.col, 0.05f, k.seed + 11);
                if (!k.back) {
                    c.tri(k.X(-15), k.Y(-10), k.X(-9), k.Y(-14), k.X(-15.5), k.Y(-23))
                            .tri(k.X(15), k.Y(-10), k.X(9), k.Y(-14), k.X(15.5), k.Y(-23)).onlyRegion(k.ears).paint(INNER_EAR);
                }
                k.tips = new double[]{k.X(-17), k.Y(-26), 4 * u, k.X(17), k.Y(-26), 4 * u};
                break;
            case Look.FUCHS:
                c.tri(k.X(-20), k.Y(-3), k.X(-6), k.Y(-14), k.X(-21), k.Y(-31))
                        .tri(k.X(20), k.Y(-3), k.X(6), k.Y(-14), k.X(21), k.Y(-31));
                k.ears = c.shaded(k.col, 0.05f, k.seed + 11);
                if (!k.back) {
                    c.tri(k.X(-17), k.Y(-7), k.X(-10), k.Y(-13), k.X(-18.5), k.Y(-25))
                            .tri(k.X(17), k.Y(-7), k.X(10), k.Y(-13), k.X(18.5), k.Y(-25)).onlyRegion(k.ears).paint(tone(k.belly, 0.92f));
                }
                k.tips = new double[]{k.X(-20.5), k.Y(-28), 4.5 * u, k.X(20.5), k.Y(-28), 4.5 * u};
                break;
            case Look.HASE:
                c.ellipse(k.X(-8), k.Y(-26), 5 * u, 14 * u).ellipse(k.X(8), k.Y(-26), 5 * u, 14 * u);
                k.ears = c.shaded(k.col, 0.05f, k.seed + 11);
                if (!k.back) {
                    c.ellipse(k.X(-8), k.Y(-25), 2.4, 10).ellipse(k.X(8), k.Y(-25), 2.4, 10).onlyRegion(k.ears).paint(INNER_EAR);
                }
                k.tips = new double[]{k.X(-8), k.Y(-36), 5 * u, k.X(8), k.Y(-36), 5 * u};
                break;
            case Look.BAER:
                c.ellipse(k.X(-15), k.Y(-13), 6.5 * u, 6.5 * u).ellipse(k.X(15), k.Y(-13), 6.5 * u, 6.5 * u);
                k.ears = c.shaded(k.col, 0.05f, k.seed + 11);
                if (!k.back) {
                    c.ellipse(k.X(-15), k.Y(-13), 3.4, 3.4).ellipse(k.X(15), k.Y(-13), 3.4, 3.4).onlyRegion(k.ears).paint(k.belly);
                }
                k.tips = new double[]{k.X(-16), k.Y(-16), 4 * u, k.X(16), k.Y(-16), 4 * u};
                break;
            case Look.DRACHE:
                c.tri(k.X(-12), k.Y(-11), k.X(-5), k.Y(-15), k.X(-15), k.Y(-27))
                        .tri(k.X(12), k.Y(-11), k.X(5), k.Y(-15), k.X(15), k.Y(-27));
                k.ears = c.shaded(BONE, 0.03f, k.seed + 11);
                k.tips = new double[0];
                break;
            default:
                k.tips = new double[0];
                break;
        }
    }

    private static void wings(Ctx k) {
        Stamp c = k.c;
        int wc = tone(k.col, 0.86f);
        int id;
        if (!k.back) {
            double x = k.bx, y = k.by;
            id = c.poly(x - 12, y - 8, x - 29, y - 22, x - 26, y - 9, x - 23, y - 3, x - 15, y + 1)
                    .poly(x + 12, y - 8, x + 29, y - 22, x + 26, y - 9, x + 23, y - 3, x + 15, y + 1).shaded(wc, 0.03f, k.seed + 13);
            c.line((int) x - 27, (int) y - 19, (int) x - 19, (int) y - 4, 1).line((int) x + 27, (int) y - 19, (int) x + 19, (int) y - 4, 1)
                    .onlyRegion(id).paint(tone(wc, 0.75f));
        } else {
            double x = k.hx, y = 64;
            id = c.poly(x - 6, y, x - 30, y - 20, x - 25, y - 4, x - 22, y + 4, x - 10, y + 8)
                    .poly(x + 6, y, x + 26, y - 18, x + 23, y - 3, x + 20, y + 4, x + 10, y + 8).shaded(wc, 0.03f, k.seed + 13);
            c.line((int) x - 27, (int) y - 16, (int) x - 13, (int) y + 4, 1).line((int) x + 23, (int) y - 14, (int) x + 13, (int) y + 4, 1)
                    .onlyRegion(id).paint(tone(wc, 0.75f));
        }
    }

    /**
     * Tail anchored at (ax, ay) growing up and to the side given by dir
     * (+1 = viewer's right, -1 = left), scaled by s.
     */
    private static void tail(Ctx k, double ax, double ay, int dir, double s) {
        int type = k.l.tail;
        if (type == Look.TAIL_NONE) return;
        if (type == Look.TAIL_NORMAL) {
            switch (k.l.species) {
                case Look.KATZE: type = 10; break;
                case Look.DRACHE: type = 11; break;
                case Look.VOGEL: type = 12; break;
                case Look.FUCHS: type = 13; break;
                case Look.HASE: type = Look.TAIL_PUFF; break;
                default: return;
            }
        }
        Stamp c = k.c;
        int col = k.col;
        switch (type) {
            case 10: // cat
                seg(c, ax, ay, dir, s, 4, 0, 0, 8, -6, 11, -16, 8, -24, 10, -28);
                k.tail = c.shaded(col, 0.05f, k.seed + 20);
                tip(k, ax, ay, dir, s, 10, -27, 3);
                break;
            case 11: { // dragon
                c.poly(px(ax, dir, s, -2), py(ay, s, -2), px(ax, dir, s, 2), py(ay, s, 6), px(ax, dir, s, 12), py(ay, s, -2),
                        px(ax, dir, s, 15), py(ay, s, -12), px(ax, dir, s, 11), py(ay, s, -12));
                k.tail = c.shaded(col, 0.05f, k.seed + 20);
                c.poly(px(ax, dir, s, 11), py(ay, s, -11), px(ax, dir, s, 19), py(ay, s, -14), px(ax, dir, s, 18), py(ay, s, -23),
                        px(ax, dir, s, 12), py(ay, s, -18)).shaded(tone(col, 0.85f), 0.03f, k.seed + 21);
                break;
            }
            case 12: // bird feathers
                c.tri(px(ax, dir, s, 0), py(ay, s, -2), px(ax, dir, s, 14), py(ay, s, -9), px(ax, dir, s, 6), py(ay, s, 4))
                        .tri(px(ax, dir, s, 0), py(ay, s, 0), px(ax, dir, s, 16), py(ay, s, -1), px(ax, dir, s, 8), py(ay, s, 6))
                        .tri(px(ax, dir, s, 0), py(ay, s, 2), px(ax, dir, s, 13), py(ay, s, 7), px(ax, dir, s, 4), py(ay, s, 8));
                k.tail = c.shaded(tone(col, 0.9f), 0.04f, k.seed + 20);
                break;
            case 13: // fox
                c.ellipse(px(ax, dir, s, 6), py(ay, s, -2), 6 * s, 7 * s).ellipse(px(ax, dir, s, 11), py(ay, s, -11), 7.5 * s, 8.5 * s)
                        .ellipse(px(ax, dir, s, 13), py(ay, s, -22), 6.5 * s, 8.5 * s);
                k.tail = c.shaded(col, 0.06f, k.seed + 20);
                c.ellipse(px(ax, dir, s, 13.5), py(ay, s, -28), 6 * s, 5.5 * s).onlyRegion(k.tail).paint(k.l.pattern == Look.PAT_TIPS ? k.tipDark : k.belly);
                break;
            case Look.TAIL_BOLT:
                seg(c, ax, ay, dir, s, 4, 0, 0, 9, -5, 5, -12, 15, -18, 12, -25);
                c.tri(px(ax, dir, s, 9), py(ay, s, -26), px(ax, dir, s, 15), py(ay, s, -24), px(ax, dir, s, 17), py(ay, s, -33));
                k.tail = c.shaded(col, 0.05f, k.seed + 20);
                tip(k, ax, ay, dir, s, 14, -28, 3);
                break;
            case Look.TAIL_FLAME:
                seg(c, ax, ay, dir, s, 3, 0, 0, 8, -8, 10, -17);
                k.tail = c.shaded(col, 0.05f, k.seed + 20);
                flame(c, px(ax, dir, s, 10), py(ay, s, -22), s * 0.9, k.seed);
                break;
            case Look.TAIL_LEAF:
                seg(c, ax, ay, dir, s, 3, 0, 0, 8, -8, 10, -16);
                k.tail = c.shaded(col, 0.05f, k.seed + 20);
                c.ellipse(px(ax, dir, s, 11), py(ay, s, -22), 4 * s, 7 * s).shaded(LEAF, 0.03f, k.seed + 22);
                c.line((int) px(ax, dir, s, 11), (int) py(ay, s, -27), (int) px(ax, dir, s, 11), (int) py(ay, s, -17), 1).paint(tone(LEAF, 0.7f));
                break;
            default: // puff
                seg(c, ax, ay, dir, s, 4, 0, 0, 5, -4);
                c.shaded(col, 0.05f, k.seed + 20);
                k.tail = c.ellipse(px(ax, dir, s, 8), py(ay, s, -7), 6 * s, 6 * s).shaded(k.belly, 0.06f, k.seed + 21);
                break;
        }
    }

    private static double px(double ax, int dir, double s, double dx) {
        return ax + dir * dx * s;
    }

    private static double py(double ay, double s, double dy) {
        return ay + dy * s;
    }

    /** Thick polyline through relative points (x0, y0, x1, y1, ...). */
    private static void seg(Stamp c, double ax, double ay, int dir, double s, int thick, double... p) {
        int t = Math.max(2, (int) Math.round(thick * s));
        for (int i = 0; i + 3 < p.length; i += 2) {
            c.line((int) px(ax, dir, s, p[i]), (int) py(ay, s, p[i + 1]), (int) px(ax, dir, s, p[i + 2]), (int) py(ay, s, p[i + 3]), t);
        }
    }

    private static void tip(Ctx k, double ax, double ay, int dir, double s, double dx, double dy, double r) {
        k.tailTipX = px(ax, dir, s, dx);
        k.tailTipY = py(ay, s, dy);
        if (k.l.pattern == Look.PAT_TIPS && k.tail >= 0) {
            k.c.ellipse(k.tailTipX, k.tailTipY, r * s + 1, r * s + 1).onlyRegion(k.tail).paint(k.tipDark);
        }
    }

    private static void flame(Stamp c, double x, double y, double s, int seed) {
        c.poly(x - 5 * s, y + 5 * s, x - 6 * s, y - 1 * s, x - 2.5 * s, y - 6 * s, x - 1.5 * s, y - 2 * s, x + 1 * s, y - 10 * s,
                x + 4.5 * s, y - 4 * s, x + 6 * s, y + 0 * s, x + 5 * s, y + 5 * s, x, y + 7 * s).shaded(FLAME, 0.03f, seed + 30);
        c.ellipse(x, y + 2 * s, 2.8 * s, 3.6 * s).paint(FLAME_IN);
    }

    private static void pattern(Ctx k) {
        Stamp c = k.c;
        int pat = k.l.pattern;
        if (pat == Look.PAT_NONE) return;
        double top = k.top();
        if (pat == Look.PAT_STRIPES) {
            double len = k.back ? 14 : 7;
            for (int i = -1; i <= 1; i++) {
                c.tri(k.X(i * 7 - 2.3), top - 2, k.X(i * 7 + 2.3), top - 2, k.X(i * 6), top + len * k.u);
            }
            c.onlyRegion(k.head).paint(k.dark);
            if (!k.round) {
                if (k.back) {
                    for (int i = 0; i < 3; i++) {
                        int y = (int) (k.by - k.bry + 8 + i * 6);
                        c.line((int) k.bx - 12, y, (int) k.bx + 12, y + 1, 2);
                    }
                } else {
                    for (int i = 0; i < 2; i++) {
                        int y = (int) (k.by - 5 + i * 6);
                        c.line((int) (k.bx - k.brx + 1), y, (int) (k.bx - k.brx + 6), y + 1, 2)
                                .line((int) (k.bx + k.brx - 6), y + 1, (int) (k.bx + k.brx - 1), y, 2);
                    }
                }
                c.onlyRegion(k.body).paint(k.dark);
            }
        } else if (pat == Look.PAT_SPOTS) {
            int spot = Pal.mix(k.col, k.belly, 0.6f);
            double u = k.u;
            c.ellipse(k.X(-12), k.Y(-8), 2.6 * u, 2.1 * u).ellipse(k.X(13), k.Y(-6), 2.1 * u, 1.8 * u).ellipse(k.X(5), k.Y(-13), 1.6 * u, 1.4 * u);
            if (k.back) c.ellipse(k.X(-6), k.Y(4), 2.6 * u, 2.2 * u).ellipse(k.X(9), k.Y(6), 2 * u, 1.7 * u);
            c.onlyRegion(k.head).paint(spot);
            if (!k.round) {
                c.ellipse(k.bx - k.brx * 0.7, k.by - 3, 2.4 * u, 2 * u).ellipse(k.bx + k.brx * 0.72, k.by + 3, 2 * u, 1.8 * u);
                if (k.back) c.ellipse(k.bx - 8, k.by - k.bry + 9, 2.5 * u, 2 * u).ellipse(k.bx + 10, k.by - k.bry + 13, 2.2 * u, 1.8 * u);
                c.onlyRegion(k.body).paint(spot);
            }
        } else {
            for (int i = 0; i + 2 < k.tips.length; i += 3) c.ellipse(k.tips[i], k.tips[i + 1], k.tips[i + 2], k.tips[i + 2]);
            if (k.ears >= 0) c.onlyRegion(k.ears).paint(k.tipDark);
            else c.clearMask();
            if (k.round) {
                // tuft / crest feathers get dark tips
                c.ellipse(k.hx, k.by - k.bry - 2, 9 * k.u, 5 * k.u).onlyRegion(k.head).paint(k.tipDark);
            }
        }
    }

    private static void crest(Ctx k) {
        Stamp c = k.c;
        double t = k.top() + (k.l.species == Look.FROSCH ? 2 : 0);
        double hx = k.hx, u = k.u;
        switch (k.l.crest) {
            case Look.CREST_HORNS:
                if (k.l.species == Look.DRACHE) break;
                c.tri(hx - 9 * u, t + 5 * u, hx - 4 * u, t + 3 * u, hx - 12 * u, t - 7 * u)
                        .tri(hx + 9 * u, t + 5 * u, hx + 4 * u, t + 3 * u, hx + 12 * u, t - 7 * u).shaded(BONE, 0.03f, k.seed + 40);
                break;
            case Look.CREST_LEAF: {
                c.line((int) hx, (int) (t + 2 * u), (int) (hx + u), (int) (t - 6 * u), Math.max(2, (int) (2 * u))).shaded(tone(LEAF, 0.8f), 0, k.seed + 41);
                int lf = c.ellipse(hx - 4.5 * u, t - 7 * u, 4.8 * u, 2.6 * u).ellipse(hx + 5.5 * u, t - 9 * u, 4.8 * u, 2.6 * u).shaded(LEAF, 0.03f, k.seed + 42);
                c.line((int) (hx - 8 * u), (int) (t - 7 * u), (int) (hx - u), (int) (t - 7 * u), 1)
                        .line((int) (hx + 2 * u), (int) (t - 9 * u), (int) (hx + 9 * u), (int) (t - 9 * u), 1).onlyRegion(lf).paint(tone(LEAF, 0.72f));
                break;
            }
            case Look.CREST_FLAME:
                flame(c, hx, t - 4 * u, u, k.seed + 43);
                break;
            case Look.CREST_ANTENNA: {
                int ac = tone(k.col, 0.6f);
                int th = Math.max(1, (int) Math.round(1.4 * u));
                c.line((int) (hx - 5 * u), (int) (t + 2 * u), (int) (hx - 9 * u), (int) (t - 9 * u), th)
                        .line((int) (hx + 5 * u), (int) (t + 2 * u), (int) (hx + 9 * u), (int) (t - 9 * u), th).paint(ac);
                c.ellipse(hx - 9.5 * u, t - 10 * u, 2.8 * u, 2.8 * u).ellipse(hx + 9.5 * u, t - 10 * u, 2.8 * u, 2.8 * u).shaded(FLAME_IN, 0.02f, k.seed + 44);
                break;
            }
            case Look.CREST_CRYSTAL: {
                int id = c.poly(hx, t - 13 * u, hx + 5 * u, t - 4 * u, hx, t + 3 * u, hx - 5 * u, t - 4 * u).shaded(CRYSTAL, 0, k.seed + 45);
                c.line((int) hx, (int) (t - 11 * u), (int) hx, (int) (t + u), 1).onlyRegion(id).paint(Pal.lighten(CRYSTAL, 0.5f));
                break;
            }
            case Look.CREST_GEM:
                if (k.back) break;
                c.ellipse(hx, t + 7 * u, 3.2 * u, 2.7 * u).shaded(GEM, 0, k.seed + 46);
                c.ellipse(hx - u, t + 6 * u, 1.1 * u, 0.9 * u).paint(0xFFFFFFFF);
                break;
            default:
                break;
        }
    }

    private static void hat(Ctx k) {
        Stamp c = k.c;
        double hx = k.hx, hy = k.hy, u = k.u, rx = k.hrx, t = k.top();
        int acc = k.accent;
        switch (k.l.hat) {
            case Look.HAT_CAP: {
                int dome = c.ellipse(hx, t + 10 * u, rx * 0.86, 12 * u).above((int) (t + 9 * u)).shaded(acc, 0.02f, k.seed + 50);
                if (!k.back) {
                    c.roundRect(hx - rx * 0.95, t + 6 * u, rx * 1.9, 4 * u, 2 * u).shaded(tone(acc, 0.8f), 0, k.seed + 51);
                    c.ellipse(hx, t + 2 * u, 4 * u, 3.4 * u).onlyRegion(dome).paint(0xFFFFFFFF);
                    c.ellipse(hx, t + 2 * u, 2 * u, 1.7 * u).onlyRegion(dome).paint(BAND);
                } else {
                    c.roundRect(hx - 5 * u, t + 5 * u, 10 * u, 4 * u, 2 * u).onlyRegion(dome).paint(tone(acc, 0.6f));
                    c.rect((int) (hx - 3 * u), (int) (t + 6 * u), (int) (6 * u), 2).onlyRegion(dome).paint(k.col);
                }
                break;
            }
            case Look.HAT_BEANIE: {
                int id = c.ellipse(hx, t + 11 * u, rx * 0.9, 13 * u).above((int) (t + 11 * u)).shaded(acc, 0.04f, k.seed + 52);
                for (double x = hx - rx; x < hx + rx; x += 3 * u) c.rect((int) x, (int) (t - 3 * u), 1, (int) (14 * u)).onlyRegion(id).paint(tone(acc, 0.82f));
                c.roundRect(hx - rx * 0.92, t + 7 * u, rx * 1.84, 5 * u, 2.5 * u).shaded(Pal.lighten(acc, 0.25f), 0.03f, k.seed + 53);
                c.ellipse(hx, t - 3 * u, 4.2 * u, 4.2 * u).shaded(0xFFF4F4FA, 0.05f, k.seed + 54);
                break;
            }
            case Look.HAT_BAND: {
                c.roundRect(hx - rx - 2, t + 6 * u, 2 * rx + 4, 4.5 * u, 2 * u).inEllipse(hx, hy, rx + 1.5, k.hry + 1.5).shaded(BAND, 0, k.seed + 55);
                if (!k.back) {
                    double ex = hx + rx * 0.85, ey = t + 8 * u;
                    c.tri(ex, ey - 2 * u, ex, ey + 2 * u, ex + 9 * u, ey + 5 * u).tri(ex, ey - 2 * u, ex, ey + 2 * u, ex + 7 * u, ey - 6 * u).shaded(BAND, 0, k.seed + 56);
                } else {
                    double ey = t + 8 * u;
                    c.poly(hx - 1.5 * u, ey, hx + 1 * u, ey, hx - 5 * u, ey + 11 * u, hx - 7.5 * u, ey + 10 * u)
                            .poly(hx - 1 * u, ey, hx + 1.5 * u, ey, hx + 7.5 * u, ey + 10 * u, hx + 5 * u, ey + 11 * u).shaded(BAND, 0, k.seed + 56);
                    c.ellipse(hx, ey + u, 2.4 * u, 2.2 * u).shaded(tone(BAND, 0.85f), 0, k.seed + 57);
                }
                break;
            }
            case Look.HAT_CROWN: {
                double b = t + 3 * u;
                int id = c.tri(hx - 11 * u, b, hx - 5 * u, b, hx - 11 * u, b - 11 * u).tri(hx - 6 * u, b, hx + 6 * u, b, hx, b - 13 * u)
                        .tri(hx + 5 * u, b, hx + 11 * u, b, hx + 11 * u, b - 11 * u).rect((int) (hx - 11 * u), (int) (b - 4 * u), (int) (22 * u), (int) (6 * u))
                        .shaded(GOLD, 0, k.seed + 57);
                c.rect((int) (hx - 10 * u), (int) (b - 3 * u), (int) (20 * u), 1).onlyRegion(id).paint(tone(GOLD, 1.25f));
                if (!k.back) {
                    c.ellipse(hx, b - u, 2.2 * u, 2.2 * u).paint(GEM);
                    c.ellipse(hx - 7 * u, b - u, 1.6 * u, 1.6 * u).ellipse(hx + 7 * u, b - u, 1.6 * u, 1.6 * u).paint(0xFF3D7BFF);
                }
                break;
            }
            case Look.HAT_WIZARD: {
                int id = c.tri(hx - 13 * u, t + 5 * u, hx + 13 * u, t + 5 * u, hx + 7 * u, t - 24 * u).shaded(WIZARD, 0.03f, k.seed + 58);
                c.ellipse(hx, t + 5 * u, rx + 5 * u, 4 * u).shaded(WIZARD, 0.03f, k.seed + 59);
                if (!k.back) {
                    double sx = hx + 2 * u, sy = t - 6 * u;
                    c.poly(sx, sy - 4 * u, sx + 1.2 * u, sy - 1.2 * u, sx + 4 * u, sy - u, sx + 1.8 * u, sy + u, sx + 2.5 * u, sy + 4 * u,
                            sx, sy + 2 * u, sx - 2.5 * u, sy + 4 * u, sx - 1.8 * u, sy + u, sx - 4 * u, sy - u, sx - 1.2 * u, sy - 1.2 * u)
                            .onlyRegion(id).paint(GOLD);
                }
                break;
            }
            case Look.HAT_HELMET: {
                int bottom = (int) (hy - 3 * u);
                if (!k.back) bottom = Math.min(bottom, (int) (k.ey - 6));
                int id = c.ellipse(hx, hy - u, rx + 1.5, k.hry + 1.5).above(bottom).shaded(METAL, 0.04f, k.seed + 60);
                c.rect((int) (hx - 2 * u), (int) (t - 2), (int) (4 * u), (int) (k.hry - 2)).onlyRegion(id).paint(Pal.lighten(METAL, 0.45f));
                c.ellipse(hx - 8 * u, t + 5 * u, 3.5 * u, 1.8 * u).onlyRegion(id).paint(0xFFFFFFFF);
                break;
            }
            default:
                break;
        }
    }

    private static double neckY(Ctx k) {
        if (k.l.species == Look.KNUDDEL) return k.back ? 66 : 74;
        if (k.l.species == Look.VOGEL) return k.back ? 64 : 71;
        return k.hy + k.hry + (k.back ? -3 : 1);
    }

    private static void extraFront(Ctx k) {
        Stamp c = k.c;
        double ny = neckY(k);
        switch (k.l.extra) {
            case Look.EX_GLASSES: {
                double y = k.ey - 5;
                int x0 = (int) (k.ex0 - 5.5), x1 = (int) (k.ex1 - 5.5);
                c.rect(x0 + 1, (int) y + 1, 9, 8).rect(x1 + 1, (int) y + 1, 9, 8);
                tintMask(c, 0xFFE8F4FF, 0.3f);
                c.roundRect(x0, y, 11, 10, 3).cut(x0 + 1, (int) y + 1, 9, 8).roundRect(x1, y, 11, 10, 3).cut(x1 + 1, (int) y + 1, 9, 8)
                        .rect(x0 + 11, (int) y + 3, x1 - x0 - 11, 1).paint(0xFF2B2F45);
                break;
            }
            case Look.EX_SHADES: {
                double y = k.ey - 4;
                int x0 = (int) (k.ex0 - 6), x1 = (int) (k.ex1 - 6);
                c.roundRect(x0, y, 12, 8, 3).roundRect(x1, y, 12, 8, 3).shaded(0xFF20232F, 0, 0);
                c.rect(x0 + 12, (int) y + 2, x1 - x0 - 12, 1).paint(0xFF20232F);
                c.rect(x0 + 2, (int) y + 1, 4, 1).rect(x1 + 2, (int) y + 1, 4, 1).paint(0xFF6F7CB8);
                break;
            }
            case Look.EX_SCARF: {
                double w = k.round ? 18 : 15;
                int id = c.ellipse(k.hx, ny, w, 3.8).shaded(k.accent, 0.03f, k.seed + 70);
                c.rect((int) (k.hx - w), (int) ny, (int) (2 * w), 1).onlyRegion(id).paint(tone(k.accent, 0.8f));
                int ex = (int) (k.hx + w * 0.45);
                int end = c.roundRect(ex, ny, 5, 11, 2).shaded(tone(k.accent, 0.92f), 0.03f, k.seed + 71);
                c.rect(ex + 1, (int) ny + 9, 1, 2).rect(ex + 3, (int) ny + 9, 1, 2).onlyRegion(end).paint(Pal.lighten(k.accent, 0.4f));
                break;
            }
            case Look.EX_BOW: {
                double x = k.hx, y = ny + 1;
                c.tri(x, y, x - 7, y - 4, x - 7, y + 4).tri(x, y, x + 7, y - 4, x + 7, y + 4).shaded(k.accent, 0, k.seed + 72);
                c.ellipse(x, y, 2, 2).shaded(tone(k.accent, 0.85f), 0, k.seed + 73);
                break;
            }
            default:
                break;
        }
    }

    private static void extraBack(Ctx k) {
        Stamp c = k.c;
        switch (k.l.extra) {
            case Look.EX_SCARF: {
                double ny = neckY(k);
                double w = k.round ? 26 : 20;
                c.ellipse(k.hx, ny, w, 5).shaded(k.accent, 0.03f, k.seed + 70);
                double ex = k.hx - w * 0.55;
                c.poly(ex, ny, ex + 6, ny, ex + 3, ny + 14, ex - 3, ny + 13).poly(ex + 3, ny + 1, ex + 9, ny + 1, ex + 9, ny + 12, ex + 4, ny + 13)
                        .shaded(tone(k.accent, 0.92f), 0.03f, k.seed + 71);
                break;
            }
            default:
                break;
        }
    }

    private CharArt() {}
}
