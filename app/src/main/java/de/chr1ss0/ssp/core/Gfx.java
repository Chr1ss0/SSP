package de.chr1ss0.ssp.core;

/**
 * Software framebuffer. Everything in the game is drawn into this low-res
 * ARGB pixel array, which the platform layer then scales up with
 * nearest-neighbour filtering (Game-Boy look on every screen size).
 */
public final class Gfx {
    public final int w, h;
    public final int[] px;
    private int cx0, cy0, cx1, cy1;
    /** Translation applied to all drawing calls (used for screen shake / scrolling). */
    public int tx, ty;

    public Gfx(int w, int h) {
        this.w = w;
        this.h = h;
        this.px = new int[w * h];
        noClip();
    }

    public void noClip() {
        cx0 = 0; cy0 = 0; cx1 = w; cy1 = h;
    }

    public void clip(int x, int y, int cw, int ch) {
        x += tx; y += ty;
        cx0 = Math.max(0, x);
        cy0 = Math.max(0, y);
        cx1 = Math.min(w, x + cw);
        cy1 = Math.min(h, y + ch);
    }

    public void clear(int c) {
        java.util.Arrays.fill(px, c);
    }

    private static int blend(int dst, int src) {
        int a = src >>> 24;
        if (a == 255) return src;
        if (a == 0) return dst;
        int ia = 255 - a;
        int r = (((src >> 16) & 255) * a + ((dst >> 16) & 255) * ia) / 255;
        int g = (((src >> 8) & 255) * a + ((dst >> 8) & 255) * ia) / 255;
        int b = ((src & 255) * a + (dst & 255) * ia) / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public void pset(int x, int y, int c) {
        x += tx; y += ty;
        if (x < cx0 || y < cy0 || x >= cx1 || y >= cy1) return;
        int i = y * w + x;
        px[i] = (c >>> 24) == 255 ? c : blend(px[i], c);
    }

    public void fill(int x, int y, int fw, int fh, int c) {
        x += tx; y += ty;
        int x0 = Math.max(cx0, x), y0 = Math.max(cy0, y);
        int x1 = Math.min(cx1, x + fw), y1 = Math.min(cy1, y + fh);
        if (x0 >= x1 || y0 >= y1) return;
        boolean opaque = (c >>> 24) == 255;
        for (int yy = y0; yy < y1; yy++) {
            int row = yy * w;
            if (opaque) {
                java.util.Arrays.fill(px, row + x0, row + x1, c);
            } else {
                for (int xx = x0; xx < x1; xx++) px[row + xx] = blend(px[row + xx], c);
            }
        }
    }

    public void rect(int x, int y, int rw, int rh, int c) {
        if (rw <= 0 || rh <= 0) return;
        fill(x, y, rw, 1, c);
        fill(x, y + rh - 1, rw, 1, c);
        fill(x, y + 1, 1, rh - 2, c);
        fill(x + rw - 1, y + 1, 1, rh - 2, c);
    }

    public void hline(int x, int y, int len, int c) { fill(x, y, len, 1, c); }
    public void vline(int x, int y, int len, int c) { fill(x, y, 1, len, c); }

    public void line(int x0, int y0, int x1, int y1, int c) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            pset(x0, y0, c);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    public void thickLine(int x0, int y0, int x1, int y1, int t, int c) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        int o = t / 2;
        while (true) {
            fill(x0 - o, y0 - o, t, t, c);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    public void fillEllipse(int cx, int cy, int rx, int ry, int c) {
        if (rx <= 0 || ry <= 0) { fill(cx, cy, 1, 1, c); return; }
        for (int y = -ry; y <= ry; y++) {
            double f = 1.0 - (y * y) / (double) (ry * ry);
            int hw = (int) Math.round(rx * Math.sqrt(Math.max(0, f)));
            fill(cx - hw, cy + y, hw * 2 + 1, 1, c);
        }
    }

    public void fillCircle(int cx, int cy, int r, int c) { fillEllipse(cx, cy, r, r, c); }

    public void circle(int cx, int cy, int r, int c) {
        int x = r, y = 0, err = 1 - r;
        while (x >= y) {
            pset(cx + x, cy + y, c); pset(cx + y, cy + x, c);
            pset(cx - y, cy + x, c); pset(cx - x, cy + y, c);
            pset(cx - x, cy - y, c); pset(cx - y, cy - x, c);
            pset(cx + y, cy - x, c); pset(cx + x, cy - y, c);
            y++;
            if (err < 0) err += 2 * y + 1;
            else { x--; err += 2 * (y - x) + 1; }
        }
    }

    // ---------------------------------------------------------------- sprites

    public static final int MODE_NORMAL = 0;
    /** Draw all opaque pixels in a single colour (hit flash, silhouettes). */
    public static final int MODE_SOLID = 1;

    public void sprite(Sprite s, int x, int y) {
        sprite(s, x, y, 1, false, MODE_NORMAL, 0);
    }

    public void sprite(Sprite s, int x, int y, int scale) {
        sprite(s, x, y, scale, false, MODE_NORMAL, 0);
    }

    public void sprite(Sprite s, int x, int y, int scale, boolean flip, int mode, int color) {
        if (s == null) return;
        for (int sy = 0; sy < s.h; sy++) {
            for (int sx = 0; sx < s.w; sx++) {
                int c = s.px[sy * s.w + sx];
                if ((c >>> 24) == 0) continue;
                if (mode == MODE_SOLID) c = color;
                int dx = flip ? (s.w - 1 - sx) : sx;
                if (scale == 1) pset(x + dx, y + sy, c);
                else fill(x + dx * scale, y + sy * scale, scale, scale, c);
            }
        }
    }

    /** Draws only the rows [fromRow, toRow) of a sprite (used for slide-out faint animation). */
    public void spriteRows(Sprite s, int x, int y, int scale, int fromRow, int toRow) {
        for (int sy = Math.max(0, fromRow); sy < Math.min(s.h, toRow); sy++) {
            for (int sx = 0; sx < s.w; sx++) {
                int c = s.px[sy * s.w + sx];
                if ((c >>> 24) == 0) continue;
                fill(x + sx * scale, y + sy * scale, scale, scale, c);
            }
        }
    }

    // ---------------------------------------------------------------- effects

    /** Blend the whole screen toward a colour (0..255 amount). */
    public void tint(int color, int amount) {
        if (amount <= 0) return;
        if (amount > 255) amount = 255;
        int c = (amount << 24) | (color & 0xFFFFFF);
        for (int i = 0; i < px.length; i++) px[i] = blend(px[i], c);
    }

    public void tintRect(int x, int y, int rw, int rh, int color, int amount) {
        if (amount <= 0) return;
        fill(x, y, rw, rh, ((Math.min(255, amount)) << 24) | (color & 0xFFFFFF));
    }

    // ---------------------------------------------------------------- text helpers

    public int text(String s, int x, int y, int c) {
        return Font.draw(this, s, x, y, c, 1);
    }

    public int textCenter(String s, int cx, int y, int c) {
        int tw = Font.width(s, 1);
        return Font.draw(this, s, cx - tw / 2, y, c, 1);
    }

    public int textRight(String s, int rx, int y, int c) {
        int tw = Font.width(s, 1);
        return Font.draw(this, s, rx - tw, y, c, 1);
    }

    public void textShadow(String s, int x, int y, int c, int shadow, int scale) {
        Font.draw(this, s, x + scale, y + scale, shadow, scale);
        Font.draw(this, s, x, y, c, scale);
    }

    /** Text with a 1px outline all around (titles). */
    public void textOutline(String s, int x, int y, int c, int outline, int scale) {
        for (int oy = -1; oy <= 1; oy++)
            for (int ox = -1; ox <= 1; ox++)
                if (ox != 0 || oy != 0) Font.draw(this, s, x + ox * scale, y + oy * scale, outline, scale);
        Font.draw(this, s, x, y, c, scale);
    }
}
