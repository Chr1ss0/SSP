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

    public Stamp(int w, int h) {
        this.w = w;
        this.h = h;
        this.px = new int[w * h];
        this.mask = new boolean[w * h];
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
