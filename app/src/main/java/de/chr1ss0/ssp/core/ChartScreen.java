package de.chr1ss0.ssp.core;

/** 9x9 type effectiveness chart. */
public final class ChartScreen extends Screen {
    private static final int B_BACK = 1, B_ROW = 100;
    private int sel = -1;

    public ChartScreen(Game game) {
        super(game);
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (c >= B_ROW) {
            sel = c - B_ROW;
            game.sound.sfx(Sound.S_CURSOR);
        }
    }

    @Override
    public void render(Gfx g) {
        g.clear(Pal.BG);
        Ui.header(g, "TYPENTABELLE", W());
        int y = 24;
        g.text("Zeile = deine Waffe, Spalte = Gegner", 8, y, Pal.INK_SOFT);
        y += 12;
        int cell = 22, lx = 38;
        for (int d = 0; d < Element.COUNT; d++) {
            int x = lx + d * cell;
            Ui.panel(g, x, y, cell - 1, 14, Element.COLOR[d], Pal.shade(Element.COLOR[d], 0.6f));
            g.sprite(Art.typeIcon(d), x + 6, y + 2);
        }
        y += 16;
        for (int a = 0; a < Element.COUNT; a++) {
            int ry = y + a * cell;
            boolean hi = a == sel;
            Ui.panel(g, 4, ry, 32, cell - 1, Element.COLOR[a], hi ? Pal.RED : Pal.shade(Element.COLOR[a], 0.6f));
            g.sprite(Art.typeIcon(a), 6, ry + 6);
            g.text(Element.NAME[a].substring(0, 2), 17, ry + 7, Element.TEXT[a]);
            for (int d = 0; d < Element.COUNT; d++) {
                int x = lx + d * cell;
                int fill, txt;
                String s;
                if (Element.beats(a, d)) { fill = 0xFFC8F0C0; txt = Pal.GREEN; s = String.valueOf(Font.UP); }
                else if (Element.beats(d, a)) { fill = 0xFFF8D0D0; txt = Pal.RED; s = String.valueOf(Font.MORE); }
                else { fill = 0xFFE8E8E8; txt = Pal.GRAY; s = "="; }
                if (hi) fill = Pal.lighten(fill, -0.0f);
                g.fill(x, ry, cell - 1, cell - 1, fill);
                if (hi) g.rect(x, ry, cell - 1, cell - 1, Pal.INK);
                g.textCenter(s, x + cell / 2, ry + 7, txt);
            }
            ui.region(B_ROW + a, 0, ry, W(), cell);
        }
        y += Element.COUNT * cell + 6;
        if (sel >= 0) {
            g.text(Element.NAME[sel] + " schlägt: " + Element.joinNames(Element.beatsList(sel)), 8, y, Pal.GREEN);
            g.text(Element.NAME[sel] + " verliert gegen: " + Element.joinNames(Element.losesList(sel)), 8, y + 11, Pal.RED);
        } else {
            Ui.paragraph(g, "Jeder Typ schlägt genau 4 andere und verliert gegen 4. Gleicher Typ = Unentschieden.", 8, y, W() - 16, Pal.INK_SOFT);
        }
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
