package de.chr1ss0.ssp.core;

/**
 * Software framebuffer. Layout code works in logical pixels (240 wide);
 * the buffer itself has {@link #k} physical pixels per logical pixel so that
 * text, UI shapes and HD sprites can use the finer resolution.
 */
public final class Gfx {
    /** Logical size. */
    public final int w, h;
    /** Physical pixels per logical pixel. */
    public final int k;
    /** Physical size. */
    public final int pw, ph;
    public final int[] px;
    private int cx0, cy0, cx1, cy1; // physical clip
    /** Logical translation applied to all drawing (screen shake). */
    public int tx, ty;

    public Gfx(int w, int h) {
        this(w, h, 1);
    }

    public Gfx(int w, int h, int k) {
        this.w = w;
        this.h = h;
        this.k = k;
        this.pw = w * k;
        this.ph = h * k;
        this.px = new int[pw * ph];
        noClip();
    }

    public void noClip() {
        cx0 = 0; cy0 = 0; cx1 = pw; cy1 = ph;
    }

    public void clip(int x, int y, int cw, int ch) {
        int x0 = (x + tx) * k, y0 = (y + ty) * k;
        cx0 = Math.max(0, x0);
        cy0 = Math.max(0, y0);
        cx1 = Math.min(pw, x0 + cw * k);
        cy1 = Math.min(ph, y0 + ch * k);
    }

    public void clear(int c) {
        java.util.Arrays.fill(px, c);
    }

    static int blend(int dst, int src) {
        int a = src >>> 24;
        if (a == 255) return src;
        if (a == 0) return dst;
        int ia = 255 - a;
        int r = (((src >> 16) & 255) * a + ((dst >> 16) & 255) * ia) / 255;
        int g = (((src >> 8) & 255) * a + ((dst >> 8) & 255) * ia) / 255;
        int b = ((src & 255) * a + (dst & 255) * ia) / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    static int withAlpha(int c, int a) {
        int ca = c >>> 24;
        return ((ca * a / 255) << 24) | (c & 0xFFFFFF);
    }

    // ---------------------------------------------------------------- physical primitives

    /** Physical pixel, coordinates already in physical space (translation included). */
    public void ppix(int x, int y, int c) {
        if (x < cx0 || y < cy0 || x >= cx1 || y >= cy1) return;
        int i = y * pw + x;
        px[i] = (c >>> 24) == 255 ? c : blend(px[i], c);
    }

    /** Physical rectangle (physical coordinates, translation included). */
    public void pfill(int x, int y, int fw, int fh, int c) {
        int x0 = Math.max(cx0, x), y0 = Math.max(cy0, y);
        int x1 = Math.min(cx1, x + fw), y1 = Math.min(cy1, y + fh);
        if (x0 >= x1 || y0 >= y1) return;
        if ((c >>> 24) == 255) {
            for (int yy = y0; yy < y1; yy++) java.util.Arrays.fill(px, yy * pw + x0, yy * pw + x1, c);
        } else {
            for (int yy = y0; yy < y1; yy++) {
                int row = yy * pw;
                for (int xx = x0; xx < x1; xx++) px[row + xx] = blend(px[row + xx], c);
            }
        }
    }

    private int PX(int x) { return (x + tx) * k; }
    private int PY(int y) { return (y + ty) * k; }

    // ---------------------------------------------------------------- logical primitives

    public void pset(int x, int y, int c) {
        pfill(PX(x), PY(y), k, k, c);
    }

    public void fill(int x, int y, int fw, int fh, int c) {
        if (fw <= 0 || fh <= 0) return;
        pfill(PX(x), PY(y), fw * k, fh * k, c);
    }

    public void rect(int x, int y, int rw, int rh, int c) {
        if (rw <= 0 || rh <= 0) return;
        fill(x, y, rw, 1, c);
        fill(x, y + rh - 1, rw, 1, c);
        fill(x, y + 1, 1, rh - 2, c);
        fill(x + rw - 1, y + 1, 1, rh - 2, c);
    }

    /** Rectangle outline with a 1-physical-pixel line. */
    public void rectThin(int x, int y, int rw, int rh, int c) {
        int x0 = PX(x), y0 = PY(y), w0 = rw * k, h0 = rh * k;
        pfill(x0, y0, w0, 1, c);
        pfill(x0, y0 + h0 - 1, w0, 1, c);
        pfill(x0, y0 + 1, 1, h0 - 2, c);
        pfill(x0 + w0 - 1, y0 + 1, 1, h0 - 2, c);
    }

    public void hline(int x, int y, int len, int c) { fill(x, y, len, 1, c); }
    public void vline(int x, int y, int len, int c) { fill(x, y, 1, len, c); }

    /** Thin (physical) horizontal line. */
    public void hlineThin(int x, int y, int len, int c) { pfill(PX(x), PY(y), len * k, 1, c); }

    public void line(int x0, int y0, int x1, int y1, int c) {
        thickLineP(PX(x0) + k / 2, PY(y0) + k / 2, PX(x1) + k / 2, PY(y1) + k / 2, k, c);
    }

    public void thickLine(int x0, int y0, int x1, int y1, int t, int c) {
        thickLineP(PX(x0) + k / 2, PY(y0) + k / 2, PX(x1) + k / 2, PY(y1) + k / 2, t * k, c);
    }

    private void thickLineP(int x0, int y0, int x1, int y1, int t, int c) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        int o = t / 2;
        while (true) {
            pfillOpaqueOnce(x0 - o, y0 - o, t, t, c);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    // lines with translucent colours would over-blend where squares overlap; keep them simple
    private void pfillOpaqueOnce(int x, int y, int w0, int h0, int c) {
        pfill(x, y, w0, h0, (c >>> 24) == 255 ? c : c | 0xFF000000);
    }

    /** Anti-aliased filled ellipse at physical resolution (centre and radii in logical px). */
    public void fillEllipse(float cx, float cy, float rx, float ry, int c) {
        float pcx = (cx + tx + 0.5f) * k, pcy = (cy + ty + 0.5f) * k;
        float prx = Math.max(0.5f, rx * k + k * 0.5f), pry = Math.max(0.5f, ry * k + k * 0.5f);
        int y0 = (int) Math.floor(pcy - pry), y1 = (int) Math.ceil(pcy + pry);
        int x0 = (int) Math.floor(pcx - prx), x1 = (int) Math.ceil(pcx + prx);
        int ca = c >>> 24;
        for (int y = y0; y <= y1; y++) {
            if (y < cy0 || y >= cy1) continue;
            float dy = (y + 0.5f - pcy) / pry;
            for (int x = x0; x <= x1; x++) {
                if (x < cx0 || x >= cx1) continue;
                float dx = (x + 0.5f - pcx) / prx;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                // signed distance in physical pixels (approx.)
                float edge = (1f - d) * Math.min(prx, pry);
                if (edge <= -0.5f) continue;
                int a = edge >= 0.5f ? ca : (int) (ca * (edge + 0.5f));
                if (a <= 0) continue;
                ppix(x, y, (a << 24) | (c & 0xFFFFFF));
            }
        }
    }

    public void fillCircle(int cx, int cy, int r, int c) {
        fillEllipse(cx, cy, r, r, c);
    }

    public void circle(int cx, int cy, int r, int c) {
        fillEllipse(cx, cy, r, r, c);
    }

    /** Anti-aliased rounded rectangle (logical rect and radius). */
    public void roundRect(int x, int y, int rw, int rh, int r, int c) {
        roundRectGrad(x, y, rw, rh, r, c, c);
    }

    /** Rounded rectangle with a vertical gradient. */
    public void roundRectGrad(int x, int y, int rw, int rh, int r, int top, int bottom) {
        int x0 = PX(x), y0 = PY(y), w0 = rw * k, h0 = rh * k;
        if (w0 <= 0 || h0 <= 0) return;
        float R = Math.min(r * k, Math.min(w0, h0) / 2f);
        for (int yy = 0; yy < h0; yy++) {
            int py = y0 + yy;
            if (py < cy0 || py >= cy1) continue;
            int c = top == bottom ? top : Pal.mixA(top, bottom, h0 <= 1 ? 0 : yy / (float) (h0 - 1));
            int ca = c >>> 24;
            float fy = yy + 0.5f;
            float dyc = fy < R ? R - fy : (fy > h0 - R ? fy - (h0 - R) : 0);
            if (dyc == 0) {
                int a = Math.max(cx0, x0), b = Math.min(cx1, x0 + w0);
                if (ca == 255) { if (a < b) java.util.Arrays.fill(px, py * pw + a, py * pw + b, c); }
                else for (int xx = a; xx < b; xx++) px[py * pw + xx] = blend(px[py * pw + xx], c);
                continue;
            }
            for (int xx = 0; xx < w0; xx++) {
                float fx = xx + 0.5f;
                float dxc = fx < R ? R - fx : (fx > w0 - R ? fx - (w0 - R) : 0);
                int a = ca;
                if (dxc > 0) {
                    float d = (float) Math.sqrt(dxc * dxc + dyc * dyc);
                    float cov = R - d + 0.5f;
                    if (cov <= 0) continue;
                    if (cov < 1) a = (int) (ca * cov);
                } else {
                    float cov = R - dyc + 0.5f;
                    if (cov <= 0) continue;
                    if (cov < 1) a = (int) (ca * cov);
                }
                ppix(x0 + xx, py, (a << 24) | (c & 0xFFFFFF));
            }
        }
    }

    /** Rounded border of given physical thickness. */
    public void roundBorder(int x, int y, int rw, int rh, int r, int thick, int c) {
        int x0 = PX(x), y0 = PY(y), w0 = rw * k, h0 = rh * k;
        float R = Math.min(r * k, Math.min(w0, h0) / 2f);
        int ca = c >>> 24;
        for (int yy = 0; yy < h0; yy++) {
            int py = y0 + yy;
            if (py < cy0 || py >= cy1) continue;
            float fy = yy + 0.5f;
            for (int xx = 0; xx < w0; xx++) {
                float fx = xx + 0.5f;
                // distance from the rounded-rect boundary, inside positive
                float dxc = fx < R ? R - fx : (fx > w0 - R ? fx - (w0 - R) : 0);
                float dyc = fy < R ? R - fy : (fy > h0 - R ? fy - (h0 - R) : 0);
                float inside;
                if (dxc > 0 && dyc > 0) inside = R - (float) Math.sqrt(dxc * dxc + dyc * dyc);
                else inside = Math.min(Math.min(fx, w0 - fx), Math.min(fy, h0 - fy));
                if (inside < -0.5f || inside > thick + 0.5f) continue;
                float cov = Math.min(1f, Math.min(inside + 0.5f, thick + 0.5f - inside));
                if (cov <= 0) continue;
                ppix(x0 + xx, py, ((int) (ca * cov) << 24) | (c & 0xFFFFFF));
            }
        }
    }

    /** Soft drop shadow under a rounded rectangle. */
    public void shadow(int x, int y, int rw, int rh, int r, int strength) {
        for (int i = 3; i >= 1; i--) {
            roundRect(x - i + 1, y + i, rw + 2 * i - 2, rh, r + i, (strength / (i + 1)) << 24);
        }
    }

    /** Vertical gradient fill (logical rect). */
    public void gradientV(int x, int y, int rw, int rh, int top, int bottom) {
        int x0 = PX(x), y0 = PY(y), h0 = rh * k;
        for (int yy = 0; yy < h0; yy++) {
            int c = Pal.mixA(top, bottom, h0 <= 1 ? 0 : yy / (float) (h0 - 1));
            pfill(x0, y0 + yy, rw * k, 1, c);
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

    /** Low-res sprite: one art pixel = {@code scale} logical pixels. */
    public void sprite(Sprite s, int x, int y, int scale, boolean flip, int mode, int color) {
        if (s == null) return;
        blit(s, PX(x), PY(y), scale * k, flip, mode, color, 0, s.h);
    }

    /** HD sprite: one art pixel = {@code scale} physical pixels, placed at a logical position. */
    public void spriteHD(Sprite s, int x, int y, int scale) {
        if (s == null) return;
        blit(s, PX(x), PY(y), scale, false, MODE_NORMAL, 0, 0, s.h);
    }

    public void spriteHD(Sprite s, int x, int y) {
        spriteHD(s, x, y, 1);
    }

    public void spriteHD(Sprite s, int x, int y, int scale, int mode, int color) {
        if (s == null) return;
        blit(s, PX(x), PY(y), scale, false, mode, color, 0, s.h);
    }

    /** Draws only the rows [fromRow, toRow) of a low-res sprite (faint animation). */
    public void spriteRows(Sprite s, int x, int y, int scale, int fromRow, int toRow) {
        blit(s, PX(x), PY(y), scale * k, false, MODE_NORMAL, 0, Math.max(0, fromRow), Math.min(s.h, toRow));
    }

    private void blit(Sprite s, int px0, int py0, int ps, boolean flip, int mode, int color, int row0, int row1) {
        for (int sy = row0; sy < row1; sy++) {
            int dy = py0 + sy * ps;
            if (dy + ps <= cy0 || dy >= cy1) continue;
            for (int sx = 0; sx < s.w; sx++) {
                int c = s.px[sy * s.w + sx];
                int a = c >>> 24;
                if (a == 0) continue;
                if (mode == MODE_SOLID) c = (color & 0xFFFFFF) | (Math.min(a, color >>> 24) << 24);
                int dx = px0 + (flip ? (s.w - 1 - sx) : sx) * ps;
                if (ps == 1) ppix(dx, dy, c);
                else pfill(dx, dy, ps, ps, c);
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
        Font.drawP(this, s, PX(x) + k, PY(y) + k, shadow, scale);
        Font.draw(this, s, x, y, c, scale);
    }

    /** Text with an outline all around (titles). */
    public void textOutline(String s, int x, int y, int c, int outline, int scale) {
        int o = Math.max(1, scale * k / 2 + (scale > 1 ? 1 : 0));
        int bx = PX(x), by = PY(y);
        for (int oy = -o; oy <= o; oy++)
            for (int ox = -o; ox <= o; ox++)
                if ((ox != 0 || oy != 0) && ox * ox + oy * oy <= o * o + 1) Font.drawP(this, s, bx + ox, by + oy, outline, scale);
        Font.drawP(this, s, bx, by, c, scale);
    }

    /** Physical x of a logical x (for code drawing directly in physical space). */
    public int toPX(int x) {
        return PX(x);
    }

    public int toPY(int y) {
        return PY(y);
    }
}
