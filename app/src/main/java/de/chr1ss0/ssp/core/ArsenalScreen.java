package de.chr1ss0.ssp.core;

/**
 * The run's arsenal: Stein, Schere, Papier (fixed, unlimited) plus up to three
 * special weapons from loot boxes, which carry the bonus cards.
 */
public final class ArsenalScreen extends Screen {
    private static final int B_BACK = 1, B_AUTO = 2, B_CLEAR = 3, B_CLASSIC = 50, B_SPECIAL = 100;

    public ArsenalScreen(Game game, boolean prep) {
        super(game);
    }

    private boolean locked() {
        Run run = game.profile.run;
        return run != null && run.phase == Run.PHASE_BATTLE;
    }

    @Override
    public void update(float dt) {
        final Profile p = game.profile;
        if (p.run == null) {
            game.pop();
            return;
        }
        final Fighter f = p.run.player;
        int c = ui.click();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.save();
            game.pop();
        } else if (c == B_AUTO && !locked()) {
            int n = RunLogic.autoEquip(p, f);
            game.sound.sfx(n > 0 ? Sound.S_LEVELUP : Sound.S_ERROR);
            game.save();
        } else if (c == B_CLEAR && !locked()) {
            game.sound.sfx(Sound.S_BACK);
            RunLogic.unequipAll(f);
            game.save();
        } else if (c >= B_SPECIAL && c < B_SPECIAL + Run.SPECIALS && f.special(c - B_SPECIAL) != null) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new WeaponScreen(game, f.special(c - B_SPECIAL), -1, !locked()));
        } else if (c >= B_CLASSIC && c < B_CLASSIC + 3) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new WeaponScreen(game, f.weapons.get(c - B_CLASSIC), -1, false));
        }
    }

    @Override
    public void render(Gfx g) {
        Ui.background(g);
        Profile p = game.profile;
        if (p.run == null) return;
        Fighter f = p.run.player;
        Ui.header(g, "ARSENAL  " + f.weapons.size() + "/" + Run.MAX_WEAPONS, W());
        int y = 24;
        int free = 0;
        for (int i = 0; i < p.cards.length; i++) free += Math.max(0, p.available(i));
        g.text(Font.HEART + " " + f.hp + " / " + f.maxHp + " KP", 8, y, Pal.INK);
        g.textRight("Freie Karten: " + free, W() - 8, y, Pal.INK_SOFT);
        y += 14;

        // ---- classics
        g.text("FEST " + Font.DOT + " IMMER EINSETZBAR", 8, y, Pal.INK_SOFT);
        y += 11;
        int cw = (W() - 20) / 3;
        for (int i = 0; i < 3; i++) {
            WeaponInst w = f.weapons.get(i);
            int x = 6 + i * (cw + 4);
            int id = B_CLASSIC + i;
            boolean pr = ui.isPressed(id);
            Ui.panel(g, x, y + (pr ? 1 : 0), cw, 30, pr ? Pal.SELECT : Pal.WHITE, Pal.INK);
            Ui.weaponTile(g, w.def.id, x + 4, y + 5 + (pr ? 1 : 0), true);
            g.text(w.def.name, x + 27, y + 6 + (pr ? 1 : 0), Pal.INK);
            g.text(Font.SWORD + "" + w.stats().atk + "  \u221E", x + 27, y + 17 + (pr ? 1 : 0), Pal.INK_SOFT);
            ui.region(id, x, y, cw, 30);
        }
        y += 38;

        // ---- specials
        g.text("SPEZIALWAFFEN " + Font.DOT + " JE 1\u00D7 PRO GEGNER", 8, y, Pal.ACCENT);
        y += 11;
        int avail = H() - y - 66;
        int rowH = Math.max(42, Math.min(54, avail / Run.SPECIALS));
        for (int s = 0; s < Run.SPECIALS; s++) {
            WeaponInst w = f.special(s);
            int id = B_SPECIAL + s;
            if (w == null) {
                g.roundRect(6, y, W() - 12, rowH - 4, 8, 0x80FFFFFF);
                g.roundBorder(6, y, W() - 12, rowH - 4, 8, 1, 0xFFC9CDE0);
                Ui.emptySlot(g, 11, y + (rowH - 4 - 26) / 2, 26);
                int ty = y + (rowH - 4) / 2 - 10;
                g.text("Freier Platz", 42, ty, Pal.GRAY);
                g.text("Gewinne Kisten für neue Waffen!", 42, ty + 11, Pal.GRAY);
                y += rowH;
                continue;
            }
            boolean pr = ui.isPressed(id);
            int ry = y + (pr ? 1 : 0);
            Ui.panel(g, 6, ry, W() - 12, rowH - 4, pr ? Pal.SELECT : Pal.WHITE, 0xFFB9A9FF);
            Ui.weaponTileBig(g, w.def.id, 11, ry + (rowH - 4 - 26) / 2, 26);
            int tx = 42;
            int ty = ry + (rowH - 4) / 2 - 10;
            g.text(BattleScreen.ellipsize(w.displayName(), 104), tx, ty, Pal.INK);
            Stats st = w.stats();
            g.text(Element.NAME[w.def.type], tx, ty + 11, Pal.shade(Element.COLOR[w.def.type], 0.75f));
            g.text(Font.SWORD + "" + st.atk + "  " + Font.SHIELD + st.def, tx + 44, ty + 11, Pal.INK_SOFT);
            for (int k = 0; k < w.slots.length; k++) {
                int sx = W() - 12 - (w.slots.length - k) * 23, sy = ry + (rowH - 4 - 20) / 2;
                if (w.slots[k] >= 0) Ui.cardTile(g, BonusDef.get(w.slots[k]), sx, sy);
                else Ui.emptySlot(g, sx, sy, 20);
            }
            ui.region(id, 6, y, W() - 12, rowH - 4);
            y += rowH;
        }
        if (locked()) {
            g.textCenter("Im Kampf kann das Arsenal nicht geändert werden.", W() / 2, y + 2, Pal.RED);
        }
        int by = H() - 58;
        int half = (W() - 18) / 2;
        int st = locked() || f.special(0) == null ? Ui.BTN_DISABLED : Ui.BTN_NORMAL;
        ui.button(g, B_AUTO, 6, by, half, 24, "AUTO-VERTEILEN", st);
        ui.button(g, B_CLEAR, 12 + half, by, half, 24, "ALLE ABLEGEN", st);
        ui.button(g, B_BACK, 6, by + 28, W() - 12, 26, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
