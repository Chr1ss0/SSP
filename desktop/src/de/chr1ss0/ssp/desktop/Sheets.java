package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.*;

/** Renders art sheets for visual review. Usage: Sheets <outDir> */
public final class Sheets {
    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "out";
        int K = Font.K;

        // font sheet
        Gfx g = new Gfx(240, 120, K);
        Ui.background(g);
        g.text("ABCDEFGHIJKLMNOPQRSTUVWXYZ ÄÖÜ", 4, 4, Pal.INK);
        g.text("abcdefghijklmnopqrstuvwxyz äöüß", 4, 16, Pal.INK);
        g.text("0123456789 .,!?:;-+=/()[]\"'%#*<>_&~", 4, 28, Pal.INK);
        g.text("" + Font.CURSOR + Font.LEFT + Font.MORE + Font.UP + Font.HEART + Font.STAR + Font.DOT + Font.SWORD + Font.SHIELD + Font.CHECK + Font.CROSS, 4, 40, Pal.INK);
        g.text("SCHERE zerschneidet PAPIER! Volltreffer!", 4, 52, Pal.INK);
        g.text("Steinbrecher-Schere, Diamant-Papier, Größe", 4, 64, Pal.INK);
        g.textOutline("SCHNICK", 4, 80, Pal.WHITE, Pal.INK, 2);
        g.textOutline("SCHNACK", 110, 76, 0xFFFF5A5F, Pal.INK, 3);
        Png.write(g, 2, out + "/font.png");

        // weapon icons at HD 1x and 2x
        int n = WeaponDef.ALL.length;
        Gfx w = new Gfx(10 * 24 + 4, 5 * 30 + 4, K);
        Ui.background(w);
        for (int i = 0; i < n; i++) {
            int cx = 4 + (i % 10) * 24, cy = 4 + (i / 10) * 30;
            Ui.weaponTile(w, i, cx, cy, true);
            String nm = WeaponDef.get(i).name;
            if (nm.length() > 5) nm = nm.substring(0, 5);
            w.text(nm, cx - 1, cy + 21, Pal.INK);
        }
        Png.write(w, 2, out + "/weapons.png");
        Gfx w2 = new Gfx(10 * 36 + 4, 5 * 36 + 4, K);
        Ui.background(w2);
        for (int i = 0; i < n; i++) {
            w2.spriteHD(WeaponArt.icon(i), 4 + (i % 10) * 36, 4 + (i / 10) * 36, 2);
        }
        Png.write(w2, 2, out + "/weapons_big.png");

        // bonus icons + type icons + boxes
        Gfx b = new Gfx(10 * 22 + 4, 10 * 22 + 80, K);
        Ui.background(b);
        for (int i = 0; i < BonusDef.ALL.length; i++) {
            Ui.cardTile(b, BonusDef.get(i), 4 + (i % 10) * 22, 4 + (i / 10) * 22);
        }
        for (int t = 0; t < Element.COUNT; t++) {
            Ui.typeBadge(b, t, 4 + (t % 4) * 54, 228 + (t / 4) * 14, 52);
        }
        for (int t = 0; t < 3; t++) {
            b.spriteHD(Art.box(t, 0), 120 + t * 34, 262, 2);
            b.spriteHD(Art.box(t, 1), 120 + t * 34, 252, 2);
        }
        for (int i = 0; i < 8; i++) Ui.cardMini(b, BonusDef.get(i * 12), 4 + i * 14, 268);
        Png.write(b, 2, out + "/bonus.png");

        // characters: front + back for hair styles, hats, extras, tops
        Gfx c = new Gfx(4 * 100 + 4, 4 * 196 + 4, K);
        Ui.background(c);
        for (int i = 0; i < 16; i++) {
            Look l = new Look(i % 8, i % Look.HAIR_COLORS.length, i % 6, (i * 5) % 12, (i * 7 + 3) % 12, i < 8 ? (i % 7) : 0, i >= 8 ? (i % 5) : 0);
            l.eyes = i % 6;
            l.top = i % 3;
            int x = 4 + (i % 4) * 100, y = 4 + (i / 4) * 196;
            c.sprite(CharArt.front(l, i * 3), x, y, 1);
            c.sprite(CharArt.back(l, i * 3), x, y + 100, 1);
        }
        Png.write(c, 2, out + "/chars.png");
        System.out.println("weapons: " + n + ", bonuses: " + BonusDef.ALL.length);
    }
}
