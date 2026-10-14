package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** Pick a card from the collection for a weapon slot. */
public final class CardPickScreen extends Screen {
    private static final int B_BACK = 1, B_REMOVE = 2, B_CARD = 1000;
    private static final int SCROLL = 7;
    private final WeaponInst weapon;
    private final int slot;
    private final ArrayList<Integer> list = new ArrayList<Integer>();

    public CardPickScreen(Game game, WeaponInst weapon, int slot) {
        super(game);
        this.weapon = weapon;
        this.slot = slot;
    }

    @Override
    public void enter() {
        list.clear();
        Profile p = game.profile;
        for (int i = 0; i < p.cards.length; i++) if (p.available(i) > 0) list.add(i);
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                int ra = BonusDef.get(a).rarity, rb = BonusDef.get(b).rarity;
                return ra != rb ? rb - ra : a - b;
            }
        });
    }

    @Override
    public void update(float dt) {
        Profile p = game.profile;
        int c = ui.click();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (c == B_REMOVE) {
            RunLogic.equip(p, p.run.player, weapon, slot, -1);
            game.sound.sfx(Sound.S_BACK);
            game.save();
            game.pop();
        } else if (c >= B_CARD) {
            int id = c - B_CARD;
            RunLogic.equip(p, p.run.player, weapon, slot, id);
            game.sound.sfx(Sound.S_LEVELUP);
            game.save();
            game.pop();
        }
    }

    @Override
    public void render(Gfx g) {
        Ui.background(g);
        Ui.header(g, "KARTE FÜR " + weapon.def.upper(), W());
        int y = 22;
        int cur = weapon.slots[slot];
        if (cur >= 0) {
            ui.button(g, B_REMOVE, 8, y, W() - 16, 22, "KARTE " + BonusDef.get(cur).upper() + " ABLEGEN", Ui.BTN_DANGER);
            y += 26;
        }
        int listH = H() - y - 34;
        int rowH = 32;
        if (list.isEmpty()) {
            Ui.paragraph(g, "Keine freien Karten. Gewinne Runden im Kampf, um neue Bonuskarten zu ziehen!", 12, y + 20, W() - 24, Pal.INK_SOFT);
        } else {
            int off = ui.beginScroll(g, SCROLL, 0, y, W(), listH, list.size() * rowH + 4);
            for (int i = 0; i < list.size(); i++) {
                int ry = y + 2 + i * rowH - off;
                if (ry + rowH < y || ry > y + listH) continue;
                int id = list.get(i);
                BonusDef b = BonusDef.get(id);
                int bid = B_CARD + id;
                boolean pr = ui.isPressed(bid);
                Ui.panel(g, 6, ry, W() - 14, rowH - 3, pr ? Pal.SELECT : Pal.WHITE, Pal.RARITY[b.rarity]);
                Ui.cardTile(g, b, 9, ry + 4);
                g.text(b.name, 33, ry + 4, Pal.RARITY[b.rarity]);
                g.textRight("x" + game.profile.available(id), W() - 14, ry + 4, Pal.INK);
                g.text(BattleScreen.ellipsize(b.describe().replace("\n", ", "), W() - 50), 33, ry + 16, Pal.INK_SOFT);
                ui.regionClipped(bid, 6, ry, W() - 14, rowH - 3, 0, y, W(), listH);
            }
            ui.endScroll(g, SCROLL);
        }
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
