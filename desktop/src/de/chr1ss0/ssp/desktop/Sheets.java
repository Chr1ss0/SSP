package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.*;

/** Renders art sheets for visual review. Usage: Sheets <outDir> */
public final class Sheets {
    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "out";

        // font sheet
        Gfx g = new Gfx(240, 120);
        g.clear(Pal.BG);
        g.text("ABCDEFGHIJKLMNOPQRSTUVWXYZ ÄÖÜ", 4, 4, Pal.INK);
        g.text("abcdefghijklmnopqrstuvwxyz äöüß", 4, 16, Pal.INK);
        g.text("0123456789 .,!?:;-+=/()[]\"'%#*<>_&~", 4, 28, Pal.INK);
        g.text("" + Font.CURSOR + Font.LEFT + Font.MORE + Font.UP + Font.HEART + Font.STAR + Font.DOT + Font.SWORD + Font.SHIELD + Font.CHECK + Font.CROSS, 4, 40, Pal.INK);
        g.text("SCHERE zerschneidet PAPIER! Volltreffer!", 4, 52, Pal.INK);
        g.text("Steinbrecher-Schere, Diamant-Papier, Größe", 4, 64, Pal.INK);
        g.textOutline("SCHNICK", 4, 80, Pal.WHITE, Pal.INK, 2);
        Png.write(g, 4, out + "/font.png");

        // weapon icons
        int n = WeaponDef.ALL.length;
        Gfx w = new Gfx(10 * 24 + 4, 5 * 34 + 4);
        w.clear(Pal.BG);
        for (int i = 0; i < n; i++) {
            int cx = 4 + (i % 10) * 24, cy = 4 + (i / 10) * 34;
            w.fill(cx - 1, cy - 1, 18, 18, Element.COLOR[WeaponDef.get(i).type]);
            w.sprite(WeaponArt.icon(i), cx, cy);
            String nm = WeaponDef.get(i).name;
            if (nm.length() > 4) nm = nm.substring(0, 4);
            w.text(nm, cx - 2, cy + 19, Pal.INK);
        }
        Png.write(w, 4, out + "/weapons.png");
        // characters: hair styles (row 1-2), hats (row 3-4), extras (row 5)
        Gfx c = new Gfx(8 * 50 + 4, 5 * 52 + 4);
        c.clear(Pal.BG);
        for (int i = 0; i < 8; i++) {
            Look l = new Look(i, i % Look.HAIR_COLORS.length, i % 6, i, (i + 3) % 12, 0, 0);
            c.sprite(CharArt.front(l, i * 6), 4 + i * 50, 4);
            c.sprite(CharArt.back(l, i * 6), 4 + i * 50, 56);
        }
        for (int i = 0; i < 7; i++) {
            Look l = new Look(i % 8, (i + 2) % 10, (i + 1) % 6, (i + 4) % 12, (i + 7) % 12, i, 0);
            c.sprite(CharArt.front(l, 40 + i), 4 + i * 50, 108);
            c.sprite(CharArt.back(l, 40 + i), 4 + i * 50, 160);
        }
        for (int i = 0; i < 5; i++) {
            Look l = new Look(0, 1, 2, 3, 4, 0, i);
            c.sprite(CharArt.front(l, -1), 4 + i * 50, 212);
        }
        Png.write(c, 4, out + "/chars.png");

        // bonus icons + type icons + boxes
        Gfx b = new Gfx(10 * 22 + 4, 10 * 20 + 70);
        b.clear(Pal.BG);
        for (int i = 0; i < BonusDef.ALL.length; i++) {
            b.sprite(Art.bonusIcon(BonusDef.get(i)), 4 + (i % 10) * 22, 4 + (i / 10) * 20);
        }
        for (int t = 0; t < Element.COUNT; t++) b.sprite(Art.typeIcon(t), 4 + t * 14, 208);
        for (int t = 0; t < 3; t++) {
            b.sprite(Art.box(t, 0), 4 + t * 26, 240);
            b.sprite(Art.box(t, 1), 4 + t * 26, 232);
        }
        Png.write(b, 4, out + "/bonus.png");

        System.out.println("weapons: " + n + ", art: " + WeaponArt.count() + ", bonuses: " + BonusDef.ALL.length);
    }
}
