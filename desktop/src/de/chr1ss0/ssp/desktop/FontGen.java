package de.chr1ss0.ssp.desktop;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;

/**
 * Renders the UI font (Outfit Bold, OFL; symbols from Inter Bold, OFL) into
 * anti-aliased 4-bit glyph bitmaps for three sizes and writes them as the
 * generated class core/FontData.java.
 * Usage: FontGen fontDir outJavaFile
 */
public final class FontGen {
    static final char CURSOR = '▶', LEFT = '◀', MORE = '▼', UP = '▲', HEART = '♥',
            STAR = '★', DOT = '·', SWORD = '†', SHIELD = '■', CHECK = '✓', CROSS = '✗';

    public static void main(String[] args) throws Exception {
        String dir = args.length > 0 ? args[0] : "tools/fonts";
        String out = args.length > 1 ? args[1] : "app/src/main/java/de/chr1ss0/ssp/core/FontData.java";
        Font main = Font.createFont(Font.TRUETYPE_FONT, new File(dir, "Outfit-Bold.ttf"));
        Font sym = Font.createFont(Font.TRUETYPE_FONT, new File(dir, "Inter-Bold.otf"));
        StringBuilder chars = new StringBuilder();
        for (char c = 32; c < 127; c++) chars.append(c);
        chars.append("ÄÖÜäöüß");
        chars.append(CURSOR).append(LEFT).append(MORE).append(UP).append(HEART).append(STAR).append(DOT)
                .append(SWORD).append(SHIELD).append(CHECK).append(CROSS).append('\u221E').append('\u00D7');

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(bos);
        int sizes = 3;
        d.writeByte(sizes);
        for (int s = 1; s <= sizes; s++) {
            int cap = 14 * s;
            float mainSize = sizeFor(main, cap);
            float symSize = sizeFor(sym, cap);
            Font fm = main.deriveFont(mainSize), fs = sym.deriveFont(symSize);
            d.writeShort(cap);
            d.writeShort(chars.length());
            int pad = cap;
            for (int i = 0; i < chars.length(); i++) {
                char ch = chars.charAt(i);
                Font f = fm.canDisplay(ch) && !isSymbol(ch) ? fm : fs;
                int imgW = cap * 3, imgH = cap * 3;
                BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g.setColor(Color.WHITE);
                int baseX = pad, baseY = pad + cap; // cap top at y = pad
                int adv;
                if (ch == SWORD || ch == SHIELD) {
                    adv = drawCustom(g, ch, baseX, pad, cap);
                } else {
                    g.setFont(f);
                    FontRenderContext frc = g.getFontRenderContext();
                    GlyphVector gv = f.createGlyphVector(frc, String.valueOf(ch));
                    float a = gv.getGlyphMetrics(0).getAdvance();
                    float yOff = 0;
                    if (f == fs && isSymbol(ch)) {
                        // centre symbols vertically on the cap height
                        Rectangle2D vb = gv.getVisualBounds();
                        yOff = (float) (-(vb.getY() + vb.getHeight() / 2) - cap / 2.0);
                    }
                    g.drawGlyphVector(gv, baseX, baseY - yOff);
                    adv = Math.round(a);
                    if (ch == ' ') adv = Math.round(cap * 0.32f);
                }
                g.dispose();
                // bounds
                int minX = imgW, minY = imgH, maxX = -1, maxY = -1;
                for (int y = 0; y < imgH; y++)
                    for (int x = 0; x < imgW; x++)
                        if ((img.getRGB(x, y) >>> 24) > 8) {
                            minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                            minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                        }
                d.writeChar(ch);
                d.writeShort(adv + (s == 1 ? 1 : s));
                if (maxX < 0) {
                    d.writeShort(0); d.writeShort(0); d.writeShort(0); d.writeShort(0);
                    continue;
                }
                int bw = maxX - minX + 1, bh = maxY - minY + 1;
                d.writeShort(minX - baseX);
                d.writeShort(minY - pad);
                d.writeShort(bw);
                d.writeShort(bh);
                int nibble = -1;
                for (int y = minY; y <= maxY; y++)
                    for (int x = minX; x <= maxX; x++) {
                        int a = img.getRGB(x, y) >>> 24;
                        int q = Math.min(15, (a * 15 + 127) / 255);
                        if (nibble < 0) nibble = q << 4;
                        else { d.writeByte(nibble | q); nibble = -1; }
                    }
                if (nibble >= 0) d.writeByte(nibble);
            }
        }
        d.flush();
        String b64 = java.util.Base64.getEncoder().encodeToString(bos.toByteArray());
        StringBuilder src = new StringBuilder();
        src.append("package de.chr1ss0.ssp.core;\n\n");
        src.append("/** GENERATED by desktop FontGen from Outfit Bold + Inter Bold (SIL Open Font License). Do not edit. */\n");
        src.append("final class FontData {\n    static final String[] PARTS = {\n");
        for (int i = 0; i < b64.length(); i += 30000) {
            src.append("            \"").append(b64, i, Math.min(b64.length(), i + 30000)).append("\",\n");
        }
        src.append("    };\n\n    private FontData() {}\n}\n");
        FileWriter fw = new FileWriter(out);
        fw.write(src.toString());
        fw.close();
        System.out.println("font data: " + bos.size() + " bytes, base64 " + b64.length() + " -> " + out);
    }

    static boolean isSymbol(char ch) {
        return ch == CURSOR || ch == LEFT || ch == MORE || ch == UP || ch == HEART || ch == STAR || ch == CHECK || ch == CROSS
                || ch == '\u221E';
    }

    static float sizeFor(Font f, int capPx) {
        Font big = f.deriveFont(200f);
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        GlyphVector gv = big.createGlyphVector(g.getFontRenderContext(), "H");
        double h = gv.getVisualBounds().getHeight();
        g.dispose();
        return (float) (200.0 * capPx / h);
    }

    static int drawCustom(Graphics2D g, char ch, int x, int top, int cap) {
        float u = cap / 14f;
        if (ch == SWORD) {
            // diagonal sword (tip up-right) so it cannot be mistaken for a plus sign
            float cx = x + 6.5f * u, cy = top + 7.5f * u;
            java.awt.geom.AffineTransform old = g.getTransform();
            g.rotate(Math.toRadians(45), cx, cy);
            float t = cy - 8.5f * u;
            GeneralPath b = new GeneralPath();
            b.moveTo(cx, t);
            b.lineTo(cx + 2f * u, t + 2.6f * u);
            b.lineTo(cx + 2f * u, t + 11 * u);
            b.lineTo(cx - 2f * u, t + 11 * u);
            b.lineTo(cx - 2f * u, t + 2.6f * u);
            b.closePath();
            g.fill(b);
            g.fill(new java.awt.geom.RoundRectangle2D.Float(cx - 4.6f * u, t + 11 * u, 9.2f * u, 2.4f * u, u, u));
            g.fill(new java.awt.geom.RoundRectangle2D.Float(cx - 1.4f * u, t + 13 * u, 2.8f * u, 3.6f * u, u, u));
            g.setTransform(old);
            return Math.round(14 * u);
        }
        GeneralPath p = new GeneralPath();
        p.moveTo(x + 1 * u, top + 1 * u);
        p.lineTo(x + 11 * u, top + 1 * u);
        p.lineTo(x + 11 * u, top + 7 * u);
        p.quadTo(x + 11 * u, top + 12 * u, x + 6 * u, top + 14 * u);
        p.quadTo(x + 1 * u, top + 12 * u, x + 1 * u, top + 7 * u);
        p.closePath();
        g.fill(p);
        return Math.round(13 * u);
    }
}
