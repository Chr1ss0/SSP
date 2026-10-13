package de.chr1ss0.ssp.core;

/** Overview of the run's weapons and their bonus card slots. */
public final class ArsenalScreen extends Screen {
    private static final int B_BACK = 1, B_AUTO = 2, B_CLEAR = 3, B_ROW = 100;
    private final boolean prep;

    public ArsenalScreen(Game game, boolean prep) {
        super(game);
        this.prep = prep;
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
            game.sound.sfx(prep ? Sound.S_SELECT : Sound.S_BACK);
            game.save();
            game.pop();
        } else if (c == B_AUTO) {
            int n = RunLogic.autoEquip(p, f);
            game.sound.sfx(n > 0 ? Sound.S_LEVELUP : Sound.S_ERROR);
            game.save();
        } else if (c == B_CLEAR) {
            game.sound.sfx(Sound.S_BACK);
            RunLogic.unequipAll(f);
            game.save();
        } else if (c >= B_ROW && c < B_ROW + f.weapons.size()) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new WeaponScreen(game, f.weapons.get(c - B_ROW), -1));
        }
    }

    @Override
    public void render(Gfx g) {
        g.clear(Pal.BG);
        Profile p = game.profile;
        if (p.run == null) return;
        Fighter f = p.run.player;
        Ui.header(g, "ARSENAL  " + f.weapons.size() + "/" + Run.MAX_WEAPONS, W());
        int y = 24;
        if (prep) {
            y += Ui.paragraph(g, "Neuer Lauf! Verteile deine Bonuskarten auf deine Waffen.", 8, y, W() - 16, Pal.INK);
            y += 2;
        }
        int free = 0;
        for (int i = 0; i < p.cards.length; i++) free += Math.max(0, p.available(i));
        g.text(Font.HEART + " " + f.hp + "/" + f.maxHp + " KP", 8, y, Pal.INK);
        g.textRight("Freie Karten: " + free, W() - 8, y, Pal.INK_SOFT);
        y += 13;
        int avail = H() - y - 64;
        int rowH = Math.max(38, Math.min(48, avail / Math.max(1, f.weapons.size())));
        for (int i = 0; i < f.weapons.size(); i++) {
            WeaponInst w = f.weapons.get(i);
            int id = B_ROW + i;
            boolean pr = ui.isPressed(id);
            int ry = y + (pr ? 1 : 0);
            Ui.panel(g, 6, ry, W() - 12, rowH - 4, pr ? Pal.SELECT : Pal.WHITE, Pal.INK);
            Ui.weaponTile(g, w.def.id, 10, ry + (rowH - 24) / 2, true);
            int tx = 34;
            int ty = ry + (rowH - 4) / 2 - 10;
            g.text(BattleScreen.ellipsize(w.displayName(), 118), tx, ty, Pal.INK);
            Stats st = w.stats();
            g.text(Element.NAME[w.def.type], tx, ty + 11, Element.COLOR[w.def.type] == 0xFFE8E0C8 ? Pal.INK_SOFT : Pal.shade(Element.COLOR[w.def.type], 0.75f));
            g.text(Font.SWORD + "" + st.atk + " " + Font.SHIELD + st.def, tx + 46, ty + 11, Pal.INK_SOFT);
            for (int s = 0; s < w.slots.length; s++) {
                int sx = W() - 10 - (w.slots.length - s) * 22, sy = ry + (rowH - 4 - 20) / 2;
                if (w.slots[s] >= 0) Ui.cardTile(g, BonusDef.get(w.slots[s]), sx, sy);
                else Ui.emptySlot(g, sx, sy, 20);
            }
            ui.region(id, 6, y, W() - 12, rowH - 4);
            y += rowH;
        }
        int by = H() - 58;
        int half = (W() - 18) / 2;
        ui.button(g, B_AUTO, 6, by, half, 24, "AUTO-VERTEILEN", Ui.BTN_NORMAL);
        ui.button(g, B_CLEAR, 12 + half, by, half, 24, "ALLE ABLEGEN", Ui.BTN_NORMAL);
        ui.button(g, B_BACK, 6, by + 28, W() - 12, 26, prep ? "LOS GEHT'S!" : "ZURÜCK", prep ? Ui.BTN_PRIMARY : Ui.BTN_NORMAL);
    }
}
