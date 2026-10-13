package de.chr1ss0.ssp.core;

/** Immutable-ish ARGB pixel image (alpha 0 = transparent). */
public final class Sprite {
    public final int w, h;
    public final int[] px;

    public Sprite(int w, int h) {
        this.w = w;
        this.h = h;
        this.px = new int[w * h];
    }

    public Sprite(int w, int h, int[] px) {
        this.w = w;
        this.h = h;
        this.px = px;
    }

    public int get(int x, int y) {
        if (x < 0 || y < 0 || x >= w || y >= h) return 0;
        return px[y * w + x];
    }

    /**
     * Parses string art. Each char is looked up in {@link Pal}; '.' and ' ' are
     * transparent. If {@code outline} is set, every transparent pixel touching an
     * opaque one (4-neighbourhood) becomes the outline colour.
     */
    public static Sprite parse(String[] rows, boolean outline) {
        int h = rows.length, w = 0;
        for (String r : rows) w = Math.max(w, r.length());
        int pad = outline ? 1 : 0;
        Sprite s = new Sprite(w + pad * 2, h + pad * 2);
        for (int y = 0; y < h; y++) {
            String r = rows[y];
            for (int x = 0; x < r.length(); x++) {
                s.px[(y + pad) * s.w + x + pad] = Pal.color(r.charAt(x));
            }
        }
        if (outline) s.addOutline(Pal.OUTLINE);
        return s;
    }

    /** Parses art into exactly w*h, centered, with outline added inside the canvas. */
    public static Sprite parseFixed(String[] rows, int w, int h) {
        Sprite s = new Sprite(w, h);
        int ah = rows.length, aw = 0;
        for (String r : rows) aw = Math.max(aw, r.length());
        int ox = (w - aw) / 2, oy = (h - ah) / 2;
        for (int y = 0; y < ah; y++) {
            String r = rows[y];
            for (int x = 0; x < r.length(); x++) {
                int xx = x + ox, yy = y + oy;
                if (xx >= 0 && yy >= 0 && xx < w && yy < h) s.px[yy * w + xx] = Pal.color(r.charAt(x));
            }
        }
        s.addOutline(Pal.OUTLINE);
        return s;
    }

    public void addOutline(int color) {
        int[] src = px.clone();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((src[y * w + x] >>> 24) != 0) continue;
                boolean edge = (x > 0 && (src[y * w + x - 1] >>> 24) != 0)
                        || (x < w - 1 && (src[y * w + x + 1] >>> 24) != 0)
                        || (y > 0 && (src[(y - 1) * w + x] >>> 24) != 0)
                        || (y < h - 1 && (src[(y + 1) * w + x] >>> 24) != 0);
                if (edge) px[y * w + x] = color;
            }
        }
    }

    public Sprite copy() {
        return new Sprite(w, h, px.clone());
    }

    /** Returns a copy with every pixel of colour {@code from} replaced. */
    public Sprite recolor(int from, int to) {
        Sprite s = copy();
        for (int i = 0; i < s.px.length; i++) if (s.px[i] == from) s.px[i] = to;
        return s;
    }

    /** Integer upscale (used for the launcher icon). */
    public Sprite scaled(int f) {
        Sprite s = new Sprite(w * f, h * f);
        for (int y = 0; y < s.h; y++)
            for (int x = 0; x < s.w; x++)
                s.px[y * s.w + x] = px[(y / f) * w + x / f];
        return s;
    }
}
