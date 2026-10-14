package de.chr1ss0.ssp.core;

import java.util.ArrayList;

/** Choose an unlocked special weapon for one of the three arsenal slots. */
public final class WeaponPickScreen extends Screen {
    private static final int B_BACK = 1, B_W = 1000;
    private static final int SCROLL = 9;
    private final int slot;
    private final ArrayList<Integer> list = new ArrayList<Integer>();

    public WeaponPickScreen(Game game, int slot) {
        super(game);
        this.slot = slot;
    }

    @Override
    public void enter() {
        list.clear();
        Profile p = game.profile;
        for (WeaponDef d : WeaponDef.ALL) if (!d.isClassic() && p.owned[d.id]) list.add(d.id);
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        Profile p = game.profile;
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (c >= B_W && p.run != null) {
            int id = c - B_W;
            if (p.isInLoadout(id)) {
                game.sound.sfx(Sound.S_ERROR);
                return;
            }
            p.run.setSpecial(p, slot, id);
            game.sound.sfx(Sound.S_LEVELUP);
            game.save();
            game.pop();
        }
    }

    @Override
    public void render(Gfx g) {
        Ui.background(g);
        Profile p = game.profile;
        Ui.header(g, "WAFFE FÜR PLATZ " + (slot + 1), W());
        g.textCenter("Freigeschaltet: " + list.size() + " von " + (WeaponDef.ALL.length - 3), W() / 2, 24, Pal.INK_SOFT);
        int top = 36, h = H() - top - 34;
        int rowH = 32;
        int off = ui.beginScroll(g, SCROLL, 0, top, W(), h, list.size() * rowH + 4);
        for (int i = 0; i < list.size(); i++) {
            int id = list.get(i);
            WeaponDef d = WeaponDef.get(id);
            int ry = top + 2 + i * rowH - off;
            if (ry + rowH < top || ry > top + h) continue;
            boolean inUse = p.isInLoadout(id);
            boolean current = p.loadout[slot] == id;
            int bid = B_W + id;
            boolean pr = !inUse && ui.isPressed(bid);
            Ui.panel(g, 6, ry, W() - 14, rowH - 3, pr ? Pal.SELECT : (inUse ? 0xFFF0F1F6 : Pal.WHITE), Pal.INK);
            Ui.weaponTile(g, id, 10, ry + 4, true);
            g.text(d.name, 34, ry + 5, inUse ? Pal.INK_SOFT : Pal.INK);
            g.text(Element.NAME[d.type] + "  " + Font.SWORD + d.atk + "  " + Font.SHIELD + d.def + "  " + d.slots + " Plätze", 34, ry + 16, Pal.INK_SOFT);
            if (current) g.textRight("AKTUELL", W() - 14, ry + 10, Pal.ACCENT);
            else if (inUse) g.textRight("IM ARSENAL", W() - 14, ry + 10, Pal.GRAY);
            else {
                int rw = Ui.rarityBadgeWidth(d.rarity);
                Ui.rarityBadge(g, d.rarity, W() - 12 - rw, ry + 9);
            }
            ui.regionClipped(bid, 6, ry, W() - 14, rowH - 3, 0, top, W(), h);
        }
        ui.endScroll(g, SCROLL);
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
