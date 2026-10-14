package de.chr1ss0.ssp.core;

/**
 * HD art pipeline: edge-preserving 2x upscaling (EPX/Scale2x), volume shading
 * from a distance field (light from the top left), material texture and
 * selective (colour-tinted) outlines.
 */
public final class Shade {

    /** EPX / Scale2x upscaling: smooth diagonals while keeping the pixel-art look. */
    public static Sprite scale2x(Sprite s) {
        Sprite o = new Sprite(s.w * 2, s.h * 2);
        for (int y = 0; y < s.h; y++) {
            for (int x = 0; x < s.w; x++) {
                int p = s.get(x, y);
                int a = s.get(x, y - 1), b = s.get(x + 1, y), c = s.get(x - 1, y), d = s.get(x, y + 1);
                int e0 = p, e1 = p, e2 = p, e3 = p;
                if (c == a && c != d && a != b) e0 = a;
                if (a == b && a != c && b != d) e1 = b;
                if (d == c && d != b && c != a) e2 = c;
                if (b == d && b != a && d != c) e3 = d;
                o.px[(2 * y) * o.w + 2 * x] = e0;
                o.px[(2 * y) * o.w + 2 * x + 1] = e1;
                o.px[(2 * y + 1) * o.w + 2 * x] = e2;
                o.px[(2 * y + 1) * o.w + 2 * x + 1] = e3;
            }
        }
        return o;
    }

    /** Places a sprite centered on a canvas of the given size. */
    public static Sprite center(Sprite s, int w, int h) {
        Sprite o = new Sprite(w, h);
        int ox = (w - s.w) / 2, oy = (h - s.h) / 2;
        for (int y = 0; y < s.h; y++)
            for (int x = 0; x < s.w; x++) {
                int xx = x + ox, yy = y + oy;
                if (xx >= 0 && yy >= 0 && xx < w && yy < h) o.px[yy * w + xx] = s.px[y * s.w + x];
            }
        return o;
    }

    private static int hash(int x, int y, int seed) {
        int h = x * 374761393 + y * 668265263 + seed * 2147483647;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    /** Noise in [-1, 1]. */
    static float noise(int x, int y, int seed) {
        return ((hash(x, y, seed) & 0xFFFF) / 32767.5f) - 1f;
    }

    /** Chamfer distance to the nearest transparent pixel (inside = positive). */
    static float[] distance(Sprite s) {
        int w = s.w, h = s.h;
        float[] d = new float[w * h];
        float INF = 1e6f;
        for (int i = 0; i < d.length; i++) d[i] = (s.px[i] >>> 24) > 40 ? INF : 0;
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
        return d;
    }

    private static int lum(int c) {
        return (((c >> 16) & 255) * 3 + ((c >> 8) & 255) * 6 + (c & 255)) / 10;
    }

    private static float sat(int c) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        int mx = Math.max(r, Math.max(g, b)), mn = Math.min(r, Math.min(g, b));
        return mx == 0 ? 0 : (mx - mn) / (float) mx;
    }

    static int scaleColor(int c, float f) {
        int r = (int) (((c >> 16) & 255) * f), g = (int) (((c >> 8) & 255) * f), b = (int) ((c & 255) * f);
        if (f > 1) {
            // brighten toward white instead of clipping hue
            float t = f - 1;
            r = (int) (((c >> 16) & 255) + (255 - ((c >> 16) & 255)) * t);
            g = (int) (((c >> 8) & 255) + (255 - ((c >> 8) & 255)) * t);
            b = (int) ((c & 255) + (255 - (c & 255)) * t);
        }
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        return (c & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /**
     * Adds volume shading, subtle material texture and a selective outline.
     * The sprite needs a 1px transparent margin for the outline.
     *
     * @param strength shading strength (0..1)
     */
    public static Sprite enhance(Sprite src, float strength, int seed) {
        int w = src.w, h = src.h;
        Sprite o = src.copy();
        float[] d = distance(src);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                int c = src.px[i];
                if ((c >>> 24) == 0) continue;
                // surface normal from the distance gradient (points outward)
                float gx = dist(d, w, h, x + 1, y) - dist(d, w, h, x - 1, y);
                float gy = dist(d, w, h, x, y + 1) - dist(d, w, h, x, y - 1);
                float len = (float) Math.sqrt(gx * gx + gy * gy);
                float lit = 0;
                if (len > 0.01f) {
                    float nx = -gx / len, ny = -gy / len;
                    lit = (nx * -0.7071f + ny * -0.7071f); // light from top-left
                }
                float edge = (float) Math.exp(-(d[i] - 1) / 2.2f);
                float f = 1f + strength * (0.30f * lit * edge);
                // gentle vertical gradient for depth
                f += strength * 0.10f * (0.5f - y / (float) h);
                // rim light / rim shade on the first inner pixel
                if (d[i] <= 1.01f) f += lit > 0.3f ? 0.12f * strength : (lit < -0.3f ? -0.14f * strength : 0);
                // texture by material guess
                int l = lum(c);
                float s = sat(c);
                float tex;
                if (s < 0.18f && l < 215) tex = 0.07f * noise(x, y, seed);                 // stone / metal
                else if (s > 0.25f && ((c >> 16) & 255) > ((c & 255) + 40) && l < 170)
                    tex = 0.06f * noise(x / 3, y, seed + 7);                                // wood-ish grain
                else tex = 0.025f * noise(x, y, seed + 3);
                f += tex * strength;
                o.px[i] = scaleColor(c, f);
            }
        }
        // specular glints on bright, round areas
        for (int y = 1; y < h - 1; y++)
            for (int x = 1; x < w - 1; x++) {
                int i = y * w + x;
                if ((src.px[i] >>> 24) == 0) continue;
                if (d[i] >= 2.5f && d[i] <= 4.2f) {
                    float gx = dist(d, w, h, x + 1, y) - dist(d, w, h, x - 1, y);
                    float gy = dist(d, w, h, x, y + 1) - dist(d, w, h, x, y - 1);
                    if (gx > 0.6f && gy > 0.6f && lum(src.px[i]) > 110 && (hash(x, y, seed) & 7) == 0) {
                        o.px[i] = scaleColor(o.px[i], 1.35f);
                    }
                }
            }
        outline(o, src);
        return o;
    }

    private static float dist(float[] d, int w, int h, int x, int y) {
        if (x < 0 || y < 0 || x >= w || y >= h) return 0;
        return d[y * w + x];
    }

    /** Selective outline: dark, tinted by the neighbouring colour. */
    static void outline(Sprite o, Sprite src) {
        int w = o.w, h = o.h;
        int[] base = src.px;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                if ((base[i] >>> 24) != 0) continue;
                int n = 0, rs = 0, gs = 0, bs = 0;
                int[][] nb = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] dd : nb) {
                    int xx = x + dd[0], yy = y + dd[1];
                    if (xx < 0 || yy < 0 || xx >= w || yy >= h) continue;
                    int c = base[yy * w + xx];
                    if ((c >>> 24) == 0) continue;
                    n++;
                    rs += (c >> 16) & 255;
                    gs += (c >> 8) & 255;
                    bs += c & 255;
                }
                if (n == 0) continue;
                int r = rs / n, g = gs / n, b = bs / n;
                // dark, slightly saturated version of the neighbour colour
                r = (int) (r * 0.22f + 14);
                g = (int) (g * 0.22f + 12);
                b = (int) (b * 0.22f + 24);
                o.px[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
    }

    /** Area-averaging downscale (premultiplied alpha). */
    public static Sprite downscale(Sprite s, int nw, int nh) {
        Sprite o = new Sprite(nw, nh);
        float sx = s.w / (float) nw, sy = s.h / (float) nh;
        for (int y = 0; y < nh; y++)
            for (int x = 0; x < nw; x++) {
                float x0 = x * sx, x1 = (x + 1) * sx, y0 = y * sy, y1 = (y + 1) * sy;
                float a = 0, r = 0, g = 0, b = 0, tot = 0;
                for (int yy = (int) y0; yy < Math.ceil(y1); yy++)
                    for (int xx = (int) x0; xx < Math.ceil(x1); xx++) {
                        float wx = Math.min(x1, xx + 1) - Math.max(x0, xx);
                        float wy = Math.min(y1, yy + 1) - Math.max(y0, yy);
                        float wt = wx * wy;
                        if (wt <= 0) continue;
                        int c = s.get(xx, yy);
                        float ca = (c >>> 24) / 255f;
                        a += ca * wt;
                        r += ((c >> 16) & 255) * ca * wt;
                        g += ((c >> 8) & 255) * ca * wt;
                        b += (c & 255) * ca * wt;
                        tot += wt;
                    }
                if (a <= 0.001f) continue;
                int A = Math.min(255, Math.round(a / tot * 255));
                o.px[y * nw + x] = (A << 24) | (Math.min(255, Math.round(r / a)) << 16)
                        | (Math.min(255, Math.round(g / a)) << 8) | Math.min(255, Math.round(b / a));
            }
        return o;
    }

    private Shade() {}
}
