package de.chr1ss0.ssp.core;

/** Weapon encyclopedia ("Waffendex") with all 50 weapons. */
public final class DexScreen extends Screen {
    private static final int B_BACK = 1, B_CHART = 2, B_W = 1000;

    public DexScreen(Game game) {
        super(game);
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (c == B_CHART) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new ChartScreen(game));
        } else if (c >= B_W) {
            int id = c - B_W;
            if (game.profile.seen[id]) {
                game.sound.sfx(Sound.S_SELECT);
                game.push(new WeaponScreen(game, null, id));
            } else game.sound.sfx(Sound.S_ERROR);
        }
    }

    @Override
    public void render(Gfx g) {
        Ui.background(g);
        Profile p = game.profile;
        Ui.header(g, "WAFFENDEX", W());
        g.textCenter("GESEHEN " + p.seenCount() + "/50   " + Font.STAR + " FREIGESCHALTET " + p.ownedCount() + "/50", W() / 2, 24, Pal.INK);
        int cols = 9, cell = 25;
        int x0 = (W() - cols * cell) / 2;
        int y0 = 38;
        for (int i = 0; i < WeaponDef.ALL.length; i++) {
            int x = x0 + (i % cols) * cell, y = y0 + (i / cols) * (cell + 8);
            boolean seen = p.seen[i];
            Ui.weaponTile(g, i, x, y, seen);
            if (p.owned[i]) g.text(String.valueOf(Font.STAR), x + 14, y - 3, Pal.GOLD);
            g.textCenter(String.valueOf(i + 1), x + 10, y + 22, Pal.INK_SOFT);
            ui.region(B_W + i, x, y, 22, 30);
        }
        int y = y0 + 6 * (cell + 8) + 4;
        y += Ui.paragraph(g, "Waffen werden \"gesehen\", wenn ein Gegner sie trägt, und für immer freigeschaltet (\u2605), wenn du sie aus einer Kiste ziehst. Freigeschaltete Waffen kannst du im Arsenal einsetzen.",
                10, y, W() - 20, Pal.INK_SOFT);
        int by = H() - 56;
        ui.button(g, B_CHART, 30, by, W() - 60, 24, "TYPENTABELLE", Ui.BTN_NORMAL);
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
