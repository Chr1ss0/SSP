package de.chr1ss0.ssp.core;

/** Collection of all 100 bonus cards. */
public final class AlbumScreen extends Screen {
    private static final int B_BACK = 1, B_CARD = 1000;
    private static final int SCROLL = 3;
    private int sel = -1;

    public AlbumScreen(Game game) {
        super(game);
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (c >= B_CARD) {
            int id = c - B_CARD;
            if (game.profile.cardSeen[id]) {
                sel = id;
                game.sound.sfx(Sound.S_CURSOR);
            } else {
                sel = -1;
                game.sound.sfx(Sound.S_ERROR);
            }
        }
    }

    @Override
    public void render(Gfx g) {
        Ui.background(g);
        Profile p = game.profile;
        Ui.header(g, "KARTENALBUM  " + p.distinctCards() + "/" + BonusDef.ALL.length, W());
        g.textCenter("Karten insgesamt: " + p.totalCards(), W() / 2, 23, Pal.INK_SOFT);
        int top = 34;
        int detailH = 96;
        int gridH = H() - top - detailH - 34;
        int cols = 9, cell = 25;
        int x0 = (W() - cols * cell) / 2;
        int rows = (BonusDef.ALL.length + cols - 1) / cols;
        int off = ui.beginScroll(g, SCROLL, 0, top, W(), gridH, rows * cell + 4);
        for (int i = 0; i < BonusDef.ALL.length; i++) {
            int x = x0 + (i % cols) * cell, y = top + 2 + (i / cols) * cell - off;
            if (y + cell < top || y > top + gridH) continue;
            BonusDef b = BonusDef.get(i);
            if (p.cardSeen[i]) {
                Ui.cardTile(g, b, x, y);
                if (p.cards[i] > 1) {
                    String n = String.valueOf(p.cards[i]);
                    int tw = Font.width(n, 1);
                    g.fill(x + 19 - tw, y + 13, tw + 2, 8, Pal.INK);
                    g.text(n, x + 20 - tw, y + 14, Pal.WHITE);
                }
            } else {
                Ui.panel(g, x, y, 20, 20, Pal.LIGHT, Pal.GRAY);
                g.textCenter("?", x + 10, y + 7, Pal.GRAY);
            }
            if (i == sel) g.rect(x - 2, y - 2, 24, 24, Pal.RED);
            ui.regionClipped(B_CARD + i, x, y, 22, 22, 0, top, W(), gridH);
        }
        ui.endScroll(g, SCROLL);

        int dy = top + gridH + 4;
        Ui.box(g, 4, dy, W() - 8, detailH);
        if (sel >= 0) {
            BonusDef b = BonusDef.get(sel);
            Ui.cardTile(g, b, 12, dy + 10);
            g.text(b.upper(), 38, dy + 10, Pal.INK);
            g.text(WeaponDef.RARITY_NAME[b.rarity], 38, dy + 21, Pal.RARITY[b.rarity]);
            int ty = dy + 36;
            ty += Ui.paragraph(g, b.describe(), 12, ty, W() - 28, Pal.INK);
            int eq = p.run != null ? p.run.player.countCard(sel) : 0;
            g.text("Besitz: " + p.cards[sel] + "   Ausgerüstet: " + eq, 12, dy + detailH - 16, Pal.INK_SOFT);
            g.textRight("\"" + b.prefix + "-...\"", W() - 14, dy + 21, Pal.INK_SOFT);
        } else {
            Ui.paragraph(g, "Tippe auf eine Karte für Details. Für jeden besiegten Gegner erhältst du 2 Bonuskarten. Karten bleiben auch nach einer Niederlage erhalten!",
                    12, dy + 10, W() - 24, Pal.INK_SOFT);
        }
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
