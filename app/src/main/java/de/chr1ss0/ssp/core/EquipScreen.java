package de.chr1ss0.ssp.core;

/** Choose a slot of one of the special weapons for a specific card. */
public final class EquipScreen extends Screen {
    private static final int B_BACK = 1, B_SLOT = 100;
    private final BonusDef card;
    private final int pops;

    public EquipScreen(Game game, BonusDef card, int pops) {
        super(game);
        this.card = card;
        this.pops = pops;
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
            for (int i = 0; i < pops; i++) game.pop();
        }
    }

    @Override
    public void render(Gfx g) {
        Ui.background(g);
        Ui.header(g, "WOHIN MIT " + card.upper() + "?", W());
        Fighter f = game.profile.run.player;
        int y = 26;
        Ui.box(g, 6, y, W() - 12, 34);
        Ui.cardTile(g, card, 12, y + 7);
        g.text(card.name, 38, y + 7, Pal.RARITY[card.rarity]);
        g.text(BattleScreen.ellipsize(card.describe().replace("\n", ", "), W() - 52), 38, y + 18, Pal.INK);
        y += 42;
        g.text("Karten passen auf deine 3 Spezialwaffen:", 8, y, Pal.INK_SOFT);
        y += 13;
        int rowH = 46;
        for (int i = 0; i < f.weapons.size(); i++) {
            WeaponInst w = f.weapons.get(i);
            if (w.isClassic()) continue;
            Ui.panel(g, 6, y, W() - 12, rowH - 4, Pal.WHITE, Pal.INK);
            Ui.weaponTile(g, w.def.id, 12, y + (rowH - 24) / 2, true);
            g.text(BattleScreen.ellipsize(w.displayName(), 104), 38, y + 10, Pal.INK);
            g.text(Element.NAME[w.def.type] + "  " + Font.SWORD + w.stats().atk + "  " + Font.SHIELD + w.stats().def, 38, y + 22, Pal.INK_SOFT);
            for (int s = 0; s < w.slots.length; s++) {
                int sx = W() - 12 - (w.slots.length - s) * 25, sy = y + (rowH - 26) / 2;
                int id = B_SLOT + i * 10 + s;
                boolean pr = ui.isPressed(id);
                if (w.slots[s] >= 0) Ui.cardTile(g, BonusDef.get(w.slots[s]), sx, sy + (pr ? 1 : 0));
                else Ui.emptySlot(g, sx, sy + (pr ? 1 : 0), 20);
                if (pr) g.roundBorder(sx - 1, sy, 22, 22, 5, 2, Pal.RED);
                ui.region(id, sx - 2, y, 24, rowH - 4);
            }
            y += rowH;
        }
        if (f.special(0) == null) Ui.paragraph(g, "Du hast noch keine Spezialwaffe. Neue Waffen gibt es in den Kisten nach jedem Sieg.", 8, y + 2, W() - 16, Pal.INK_SOFT);
        else g.text("Belegte Plätze werden ersetzt.", 8, y + 2, Pal.INK_SOFT);
        ui.button(g, B_BACK, 60, H() - 30, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
