package de.chr1ss0.ssp.core;

/** Choose which of the three special-weapon slots receives a newly unlocked weapon. */
public final class ReplaceScreen extends Screen {
    private static final int B_BACK = 1, B_ROW = 100;
    private final int newWeapon;

    public ReplaceScreen(Game game, int newWeapon) {
        super(game);
        this.newWeapon = newWeapon;
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (c >= B_ROW && c < B_ROW + Run.SPECIALS) {
            Run run = game.profile.run;
            run.setSpecial(game.profile, c - B_ROW, newWeapon);
            run.phase = Run.PHASE_MAP;
            game.sound.sfx(Sound.S_LEVELUP);
            game.save();
            game.pop();
        }
    }

    @Override
    public void render(Gfx g) {
        Ui.background(g);
        Ui.header(g, "INS ARSENAL", W());
        int y = 26;
        Ui.box(g, 6, y, W() - 12, 38);
        Ui.weaponTileBig(g, newWeapon, 12, y + 4, 30);
        WeaponDef d = WeaponDef.get(newWeapon);
        g.text(d.name, 48, y + 8, Pal.INK);
        g.text("Welchen Platz soll " + d.name + " belegen?", 48, y + 21, Pal.INK_SOFT);
        y += 48;
        Fighter f = game.profile.run.player;
        for (int s = 0; s < Run.SPECIALS; s++) {
            WeaponInst w = f.special(s);
            int id = B_ROW + s;
            boolean pr = ui.isPressed(id);
            int ry = y + (pr ? 1 : 0);
            Ui.panel(g, 6, ry, W() - 12, 40, pr ? Pal.SELECT : Pal.WHITE, Pal.INK);
            g.text("PLATZ " + (s + 1), 12, ry + 4, Pal.ACCENT);
            Ui.weaponTile(g, w.def.id, 12, ry + 15, true);
            g.text(BattleScreen.ellipsize(w.displayName(), 120), 38, ry + 15, Pal.INK);
            g.text(Element.NAME[w.def.type] + "  " + Font.SWORD + w.stats().atk + "  Karten: " + w.equipped(), 38, ry + 27, Pal.INK_SOFT);
            g.textRight("ERSETZEN " + Font.CURSOR, W() - 14, ry + 16, Pal.RED);
            ui.region(id, 6, y, W() - 12, 40);
            y += 46;
        }
        Ui.paragraph(g, "Ausgerüstete Karten wandern mit auf die neue Waffe (soweit Plätze frei sind). Die alte Waffe bleibt in deiner Sammlung.",
                10, y + 2, W() - 20, Pal.INK_SOFT);
        ui.button(g, B_BACK, 60, H() - 30, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
