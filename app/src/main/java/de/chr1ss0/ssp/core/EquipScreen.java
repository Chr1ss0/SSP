package de.chr1ss0.ssp.core;

/** Choose a weapon slot for a specific card (after drawing it). */
public final class EquipScreen extends Screen {
    private static final int B_BACK = 1, B_SLOT = 100;
    private final BonusDef card;

    public EquipScreen(Game game, BonusDef card) {
        super(game);
        this.card = card;
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (c >= B_SLOT) {
            Profile p = game.profile;
            Fighter f = p.run.player;
            int wi = (c - B_SLOT) / 10, si = (c - B_SLOT) % 10;
            WeaponInst w = f.weapons.get(wi);
            if (p.available(card.id) <= 0 && w.slots[si] != card.id) {
                game.sound.sfx(Sound.S_ERROR);
                return;
            }
            RunLogic.equip(p, f, w, si, card.id);
            game.sound.sfx(Sound.S_LEVELUP);
            game.save();
            game.pop();
            game.pop();
        }
    }

    @Override
    public void render(Gfx g) {
        g.clear(Pal.BG);
        Ui.header(g, "WOHIN MIT " + card.upper() + "?", W());
        Fighter f = game.profile.run.player;
        int y = 24;
        Ui.cardTile(g, card, 8, y);
        g.text(card.describe().replace('\n', ','), 32, y + 6, Pal.INK);
        y += 26;
        g.text("Tippe auf einen Kartenplatz:", 8, y, Pal.INK_SOFT);
        y += 12;
        int rowH = Math.min(44, (H() - y - 34) / Math.max(1, f.weapons.size()));
        for (int i = 0; i < f.weapons.size(); i++) {
            WeaponInst w = f.weapons.get(i);
            Ui.panel(g, 6, y, W() - 12, rowH - 3, Pal.WHITE, Pal.INK);
            Ui.weaponTile(g, w.def.id, 10, y + (rowH - 23) / 2, true);
            g.text(BattleScreen.ellipsize(w.displayName(), 100), 34, y + (rowH - 3) / 2 - 4, Pal.INK);
            for (int s = 0; s < w.slots.length; s++) {
                int sx = W() - 12 - (w.slots.length - s) * 24, sy = y + (rowH - 25) / 2;
                int id = B_SLOT + i * 10 + s;
                boolean pr = ui.isPressed(id);
                if (w.slots[s] >= 0) Ui.cardTile(g, BonusDef.get(w.slots[s]), sx, sy + (pr ? 1 : 0));
                else Ui.emptySlot(g, sx, sy + (pr ? 1 : 0), 20);
                if (pr) g.rect(sx - 1, sy, 22, 22, Pal.RED);
                ui.region(id, sx - 2, y, 24, rowH - 3);
            }
            y += rowH;
        }
        ui.button(g, B_BACK, 60, H() - 30, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
