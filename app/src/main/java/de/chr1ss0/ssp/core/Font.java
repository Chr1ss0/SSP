package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Anti-aliased UI font (generated from Outfit Bold, see FontGen). Metrics are
 * given in logical pixels; glyphs are stored at 2x physical resolution in three
 * sizes (scale 1, 2, 3).
 */
public final class Font {
    /** Physical pixels per logical pixel the glyph data was generated for. */
    public static final int K = 2;
    public static final int LINE = 10;
    public static final int CAP = 7;

    /** Special symbols usable in strings. */
    public static final char CURSOR = '▶';   // ▶
    public static final char MORE = '▼';     // ▼
    public static final char UP = '▲';       // ▲
    public static final char HEART = '♥';    // ♥
    public static final char STAR = '★';     // ★
    public static final char LEFT = '◀';     // ◀
    public static final char DOT = '·';      // ·
    public static final char SWORD = '†';    // attack symbol
    public static final char SHIELD = '■';   // defence symbol
    public static final char CHECK = '✓';    // ✓
    public static final char CROSS = '✗';    // ✗

    private static final class Glyph {
        int adv, ox, oy, w, h;
        byte[] alpha; // 0..15 per pixel
    }

    @SuppressWarnings("unchecked")
    private static final HashMap<Character, Glyph>[] SIZES = new HashMap[3];

    static {
        StringBuilder sb = new StringBuilder();
        for (String p : FontData.PARTS) sb.append(p);
        byte[] data = base64(sb.toString());
        int[] pos = {0};
        int sizes = data[pos[0]++];
        for (int s = 0; s < sizes && s < SIZES.length; s++) {
            HashMap<Character, Glyph> map = new HashMap<Character, Glyph>();
            u16(data, pos); // cap
            int n = u16(data, pos);
            for (int i = 0; i < n; i++) {
                char ch = (char) u16(data, pos);
                Glyph g = new Glyph();
                g.adv = s16(data, pos);
                g.ox = s16(data, pos);
                g.oy = s16(data, pos);
                g.w = s16(data, pos);
                g.h = s16(data, pos);
                int cnt = g.w * g.h;
                g.alpha = new byte[cnt];
                for (int j = 0; j < cnt; j += 2) {
                    int b = data[pos[0]++] & 255;
                    g.alpha[j] = (byte) (b >> 4);
                    if (j + 1 < cnt) g.alpha[j + 1] = (byte) (b & 15);
                }
                map.put(ch, g);
            }
            SIZES[s] = map;
        }
    }

    private static int u16(byte[] d, int[] p) {
        int v = ((d[p[0]] & 255) << 8) | (d[p[0] + 1] & 255);
        p[0] += 2;
        return v;
    }

    private static int s16(byte[] d, int[] p) {
        return (short) u16(d, p);
    }

    private static byte[] base64(String s) {
        int[] map = new int[128];
        java.util.Arrays.fill(map, -1);
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        for (int i = 0; i < abc.length(); i++) map[abc.charAt(i)] = i;
        int len = s.length();
        while (len > 0 && s.charAt(len - 1) == '=') len--;
        byte[] out = new byte[len * 3 / 4];
        int bits = 0, acc = 0, o = 0;
        for (int i = 0; i < len; i++) {
            int v = map[s.charAt(i)];
            if (v < 0) continue;
            acc = (acc << 6) | v;
            bits += 6;
            if (bits >= 8) {
                bits -= 8;
                if (o < out.length) out[o++] = (byte) (acc >> bits);
            }
        }
        return out;
    }

    private static HashMap<Character, Glyph> size(int scale) {
        int i = Math.max(1, Math.min(3, scale)) - 1;
        return SIZES[i];
    }

    private static Glyph glyph(HashMap<Character, Glyph> m, char c) {
        Glyph g = m.get(c);
        if (g == null) g = m.get('?');
        return g;
    }

    /** Width of a string in physical pixels. */
    public static int widthP(String s, int scale) {
        HashMap<Character, Glyph> m = size(scale);
        int best = 0, cur = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') { best = Math.max(best, cur); cur = 0; continue; }
            cur += glyph(m, c).adv;
        }
        return Math.max(best, cur);
    }

    /** Width in logical pixels of a (single-line) string. */
    public static int width(String s, int scale) {
        return (widthP(s, scale) + K - 1) / K;
    }

    public static int charWidth(char c) {
        return (glyph(size(1), c).adv + K - 1) / K;
    }

    /** Draws text with the cap top at logical (x, y); returns the logical x after the text. */
    public static int draw(Gfx g, String s, int x, int y, int color, int scale) {
        int endP = drawP(g, s, g.toPX(x), g.toPY(y), color, scale);
        return (endP + g.k - 1) / g.k - g.tx;
    }

    /** Draws text at physical coordinates (cap top-left); returns the physical end x. */
    public static int drawP(Gfx g, String s, int px, int py, int color, int scale) {
        HashMap<Character, Glyph> m = size(scale);
        int ca = color >>> 24;
        int rgb = color & 0xFFFFFF;
        int cx = px, cy = py;
        int lineP = LINE * K * scale;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') { cx = px; cy += lineP; continue; }
            Glyph gl = glyph(m, c);
            int gx = cx + gl.ox, gy = cy + gl.oy;
            for (int yy = 0; yy < gl.h; yy++) {
                int row = yy * gl.w;
                for (int xx = 0; xx < gl.w; xx++) {
                    int a = gl.alpha[row + xx];
                    if (a == 0) continue;
                    g.ppix(gx + xx, gy + yy, ((a * 17 * ca / 255) << 24) | rgb);
                }
            }
            cx += gl.adv;
        }
        return cx;
    }

    /** Greedy word wrap. Respects explicit '\n'. */
    public static List<String> wrap(String text, int maxWidth) {
        List<String> out = new ArrayList<String>();
        String[] paragraphs = text.split("\n", -1);
        for (String p : paragraphs) {
            String[] words = p.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (word.length() == 0) continue;
                String cand = line.length() == 0 ? word : line + " " + word;
                if (width(cand, 1) <= maxWidth || line.length() == 0) {
                    if (line.length() == 0 && width(word, 1) > maxWidth) {
                        String rest = word;
                        while (width(rest, 1) > maxWidth) {
                            int n = rest.length() - 1;
                            while (n > 1 && width(rest.substring(0, n) + "-", 1) > maxWidth) n--;
                            out.add(rest.substring(0, n) + "-");
                            rest = rest.substring(n);
                        }
                        line.setLength(0);
                        line.append(rest);
                    } else {
                        line.setLength(0);
                        line.append(cand);
                    }
                } else {
                    out.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                }
            }
            out.add(line.toString());
        }
        return out;
    }

    private Font() {}
}
