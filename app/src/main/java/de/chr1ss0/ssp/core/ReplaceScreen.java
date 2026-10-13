package de.chr1ss0.ssp.core;

/** Arsenal is full: choose a weapon to give up for the new one. */
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
        } else if (c >= B_ROW) {
            final int idx = c - B_ROW;
            final Fighter f = game.profile.run.player;
            WeaponInst old = f.weapons.get(idx);
            game.sound.sfx(Sound.S_SELECT);
            game.dialog(old.def.name.toUpperCase(java.util.Locale.GERMAN) + " gegen " + WeaponDef.get(newWeapon).upper()
                            + " tauschen? Ausgerüstete Karten kommen zurück in die Sammlung.",
                    new String[]{"TAUSCHEN", "NEIN"}, new Game.Choice() {
                        @Override
                        public void chosen(int index) {
                            if (index != 0) return;
                            f.weapons.set(idx, new WeaponInst(newWeapon));
                            f.recalcMaxHp();
                            game.profile.run.phase = Run.PHASE_MAP;
                            game.save();
                            game.pop();
                        }
                    });
        }
    }

    @Override
    public void render(Gfx g) {
        g.clear(Pal.BG);
        Ui.header(g, "ARSENAL VOLL", W());
        int y = 26;
        Ui.weaponTile(g, newWeapon, 10, y, true);
        y += Ui.paragraph(g, "Welche Waffe gibst du für " + WeaponDef.get(newWeapon).upper() + " ab?", 36, y + 1, W() - 46, Pal.INK);
        y = Math.max(y, 50) + 6;
        Fighter f = game.profile.run.player;
        for (int i = 0; i < f.weapons.size(); i++) {
            WeaponInst w = f.weapons.get(i);
            int id = B_ROW + i;
            boolean pr = ui.isPressed(id);
            Ui.panel(g, 6, y + (pr ? 1 : 0), W() - 12, 34, pr ? Pal.SELECT : Pal.WHITE, Pal.INK);
            Ui.weaponTile(g, w.def.id, 10, y + 7, true);
            g.text(w.displayName(), 36, y + 7, Pal.INK);
            g.text(Element.NAME[w.def.type] + "  " + Font.SWORD + w.stats().atk + "  " + Font.SHIELD + w.stats().def
                    + "  Karten: " + w.equipped(), 36, y + 19, Pal.INK_SOFT);
            ui.region(id, 6, y, W() - 12, 34);
            y += 38;
        }
        ui.button(g, B_BACK, 60, H() - 30, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
