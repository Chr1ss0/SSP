package de.chr1ss0.ssp.core;

/**
 * Procedural pixel-art canvas: shapes are collected into a mask and then
 * "stamped" with an automatic 1px outline and bottom-right shading. Stamping
 * parts back-to-front gives clean internal outlines where parts overlap.
 */
public final class Stamp {
    public final int w, h;
    public final int[] px;
    private final boolean[] mask;
    /** Part id per pixel (0 = none / outline). */
    private final int[] region;
    private int nextRegion = 1;
    /** Light direction (towards the light), normalised. */
    private static final float LX = -0.55f, LY = -0.83f;

    public Stamp(int w, int h) {
        this.w = w;
        this.h = h;
        this.px = new int[w * h];
        this.mask = new boolean[w * h];
        this.region = new int[w * h];
    }

    public Stamp clearMask() {
        java.util.Arrays.fill(mask, false);
        return this;
    }

    private void m(int x, int y) {
        if (x >= 0 && y >= 0 && x < w && y < h) mask[y * w + x] = true;
    }

    public Stamp ellipse(double cx, double cy, double rx, double ry) {
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double dx = (x + 0.5 - cx) / rx, dy = (y + 0.5 - cy) / ry;
                if (dx * dx + dy * dy <= 1.0) mask[y * w + x] = true;
            }
        return this;
    }

    public Stamp rect(int x, int y, int rw, int rh) {
        for (int yy = y; yy < y + rh; yy++) for (int xx = x; xx < x + rw; xx++) m(xx, yy);
        return this;
    }

    /** Rectangle with rounded top corners of radius r. */
    public Stamp roundTop(int x, int y, int rw, int rh, int r) {
        for (int yy = y; yy < y + rh; yy++)
            for (int xx = x; xx < x + rw; xx++) {
                int dy = yy - y;
                if (dy < r) {
                    double cy = y + r - 0.5;
                    double cxl = x + r - 0.5, cxr = x + rw - r - 0.5;
                    if (xx < x + r) {
                        double ddx = xx - cxl, ddy = yy - cy;
                        if (ddx * ddx + ddy * ddy > r * r) continue;
                    } else if (xx >= x + rw - r) {
                        double ddx = xx - cxr, ddy = yy - cy;
                        if (ddx * ddx + ddy * ddy > r * r) continue;
                    }
                }
                m(xx, yy);
            }
        return this;
    }

    public Stamp tri(double x0, double y0, double x1, double y1, double x2, double y2) {
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double px0 = x + 0.5, py0 = y + 0.5;
                double d1 = (px0 - x1) * (y0 - y1) - (x0 - x1) * (py0 - y1);
                double d2 = (px0 - x2) * (y1 - y2) - (x1 - x2) * (py0 - y2);
                double d3 = (px0 - x0) * (y2 - y0) - (x2 - x0) * (py0 - y0);
                boolean neg = d1 < 0 || d2 < 0 || d3 < 0;
                boolean pos = d1 > 0 || d2 > 0 || d3 > 0;
                if (!(neg && pos)) mask[y * w + x] = true;
            }
        return this;
    }

    public Stamp line(int x0, int y0, int x1, int y1, int t) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        int o = (t - 1) / 2;
        while (true) {
            for (int yy = 0; yy < t; yy++) for (int xx = 0; xx < t; xx++) m(x0 - o + xx, y0 - o + yy);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
        return this;
    }

    /** Rounded rectangle (all corners). */
    public Stamp roundRect(double x, double y, double rw, double rh, double r) {
        for (int yy = 0; yy < h; yy++)
            for (int xx = 0; xx < w; xx++) {
                double px0 = xx + 0.5, py0 = yy + 0.5;
                if (px0 < x || py0 < y || px0 > x + rw || py0 > y + rh) continue;
                double dx = px0 < x + r ? x + r - px0 : (px0 > x + rw - r ? px0 - (x + rw - r) : 0);
                double dy = py0 < y + r ? y + r - py0 : (py0 > y + rh - r ? py0 - (y + rh - r) : 0);
                if (dx * dx + dy * dy <= r * r) mask[yy * w + xx] = true;
            }
        return this;
    }

    /** Convex or concave polygon (even-odd rule). */
    public Stamp poly(double... xy) {
        int n = xy.length / 2;
        for (int yy = 0; yy < h; yy++)
            for (int xx = 0; xx < w; xx++) {
                double px0 = xx + 0.5, py0 = yy + 0.5;
                boolean in = false;
                for (int i = 0, j = n - 1; i < n; j = i++) {
                    double xi = xy[2 * i], yi = xy[2 * i + 1], xj = xy[2 * j], yj = xy[2 * j + 1];
                    if ((yi > py0) != (yj > py0) && px0 < (xj - xi) * (py0 - yi) / (yj - yi) + xi) in = !in;
                }
                if (in) mask[yy * w + xx] = true;
            }
        return this;
    }

    /** Keeps only mask pixels inside the ellipse. */
    public Stamp inEllipse(double cx, double cy, double rx, double ry) {
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double dx = (x + 0.5 - cx) / rx, dy = (y + 0.5 - cy) / ry;
                if (dx * dx + dy * dy > 1.0) mask[y * w + x] = false;
            }
        return this;
    }

    /** Removes mask pixels inside the ellipse. */
    public Stamp cutEllipse(double cx, double cy, double rx, double ry) {
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double dx = (x + 0.5 - cx) / rx, dy = (y + 0.5 - cy) / ry;
                if (dx * dx + dy * dy <= 1.0) mask[y * w + x] = false;
            }
        return this;
    }

    /** Keeps only mask pixels belonging to the given part. */
    public Stamp onlyRegion(int id) {
        for (int i = 0; i < mask.length; i++) if (region[i] != id) mask[i] = false;
        return this;
    }

    /** Keeps only mask pixels with y < maxY. */
    public Stamp above(int maxY) {
        for (int y = Math.max(0, maxY); y < h; y++) for (int x = 0; x < w; x++) mask[y * w + x] = false;
        return this;
    }

    /** Removes mask pixels inside a rectangle. */
    public Stamp cut(int x, int y, int rw, int rh) {
        for (int yy = y; yy < y + rh; yy++)
            for (int xx = x; xx < x + rw; xx++)
                if (xx >= 0 && yy >= 0 && xx < w && yy < h) mask[yy * w + xx] = false;
        return this;
    }

    private boolean in(int x, int y) {
        return x >= 0 && y >= 0 && x < w && y < h && mask[y * w + x];
    }

    /** Stamps the current mask: outline ring, fill, shade on bottom-right edges. Clears mask. */
    public Stamp stamp(int fill, int shade) {
        int ol = Pal.OUTLINE;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                if (mask[y * w + x]) continue;
                if (in(x - 1, y) || in(x + 1, y) || in(x, y - 1) || in(x, y + 1)) px[y * w + x] = ol;
            }
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                if (!mask[y * w + x]) continue;
                boolean edge = !in(x + 1, y) || !in(x, y + 1);
                px[y * w + x] = (edge && shade != 0) ? shade : fill;
            }
        return clearMask();
    }

    /**
     * Stamps the mask as a shaded part: tinted outline, 4-tone lighting from a
     * distance field (light from the top left) and optional texture noise.
     * Returns the part id.
     */
    public int shaded(int base, float texture, int seed) {
        int id = nextRegion++;
        float[] d = new float[w * h];
        float INF = 1e6f;
        int top = h, bottom = 0;
        for (int i = 0; i < d.length; i++) {
            d[i] = mask[i] ? INF : 0;
            if (mask[i]) { top = Math.min(top, i / w); bottom = Math.max(bottom, i / w); }
        }
        if (top > bottom) return id;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                if (d[i] == 0) continue;
                float v = d[i];
                if (x == 0 || y == 0) v = Math.min(v, 1);
                if (x > 0) v = Math.min(v, d[i - 1] + 1);
                if (y > 0) v = Math.min(v, d[i - w] + 1);
                if (x > 0 && y > 0) v = Math.min(v, d[i - w - 1] + 1.414f);
                if (x < w - 1 && y > 0) v = Math.min(v, d[i - w + 1] + 1.414f);
                d[i] = v;
            }
        for (int y = h - 1; y >= 0; y--)
            for (int x = w - 1; x >= 0; x--) {
                int i = y * w + x;
                if (d[i] == 0) continue;
                float v = d[i];
                if (x == w - 1 || y == h - 1) v = Math.min(v, 1);
                if (x < w - 1) v = Math.min(v, d[i + 1] + 1);
                if (y < h - 1) v = Math.min(v, d[i + w] + 1);
                if (x < w - 1 && y < h - 1) v = Math.min(v, d[i + w + 1] + 1.414f);
                if (x > 0 && y < h - 1) v = Math.min(v, d[i + w - 1] + 1.414f);
                d[i] = v;
            }
        int dark = Shade.scaleColor(base, 0.36f);
        // outline ring
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                if (mask[i]) continue;
                if (in(x - 1, y) || in(x + 1, y) || in(x, y - 1) || in(x, y + 1)) {
                    px[i] = dark;
                    region[i] = 0;
                }
            }
        float span = Math.max(1, bottom - top);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                if (!mask[i]) continue;
                float gx = dd(d, x + 1, y) - dd(d, x - 1, y);
                float gy = dd(d, x, y + 1) - dd(d, x, y - 1);
                float len = (float) Math.sqrt(gx * gx + gy * gy);
                float lit = 0;
                if (len > 0.01f) lit = (-gx / len) * LX + (-gy / len) * LY;
                float e = (float) Math.exp(-(d[i] - 1) / 3.2f);
                float f = 1f + 0.34f * lit * e + 0.10f * (0.45f - (y - top) / span);
                float tone;
                if (f < 0.80f) tone = 0.72f;
                else if (f < 0.93f) tone = 0.86f;
                else if (f < 1.08f) tone = 1.0f;
                else tone = 1.13f;
                if (texture > 0) tone += texture * Shade.noise(x, y, seed);
                px[i] = Shade.scaleColor(base, tone);
                region[i] = id;
            }
        clearMask();
        return id;
    }

    private float dd(float[] d, int x, int y) {
        if (x < 0 || y < 0 || x >= w || y >= h) return 0;
        return d[y * w + x];
    }

    /** Fills the mask without outline (details like belts, stripes). */
    public Stamp paint(int fill) {
        for (int i = 0; i < mask.length; i++) if (mask[i]) px[i] = fill;
        return clearMask();
    }

    public void set(int x, int y, int c) {
        if (x >= 0 && y >= 0 && x < w && y < h) px[y * w + x] = c;
    }

    public void sprite(Sprite s, int x, int y) {
        for (int yy = 0; yy < s.h; yy++)
            for (int xx = 0; xx < s.w; xx++) {
                int c = s.px[yy * s.w + xx];
                if ((c >>> 24) != 0) set(x + xx, y + yy, c);
            }
    }

    public Sprite toSprite() {
        return new Sprite(w, h, px.clone());
    }
}
