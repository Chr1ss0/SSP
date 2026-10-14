package de.chr1ss0.ssp.core;

import java.util.List;

/** Overlay that reveals a freshly drawn bonus card. */
public final class CardScreen extends Screen {
    private static final int B_EQUIP = 1, B_KEEP = 2;
    private final BonusDef card;

    public CardScreen(Game game, BonusDef card) {
        super(game);
        this.card = card;
    }

    @Override
    public boolean isOverlay() {
        return true;
    }

    @Override
    public void update(float dt) {
        if (time < 500) {
            ui.click();
            return;
        }
        int c = ui.click();
        if (c == B_EQUIP) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new EquipScreen(game, card, 2));
        } else if (c == B_KEEP) {
            game.sound.sfx(Sound.S_SELECT);
            game.pop();
        }
    }

    @Override
    public boolean onBack() {
        game.pop();
        return true;
    }

    @Override
    public void render(Gfx g) {
        g.tint(Pal.INK, 150);
        float p = Math.min(1f, time / 450f);
        int cw = 150, ch = 196;
        int x = (W() - cw) / 2;
        int y = (H() - ch - 60) / 2;
        // flip-in animation: card grows horizontally
        int vis = (int) (cw * Math.abs(Math.cos((1 - p) * Math.PI / 2)));
        if (p < 1f) {
            int vx = x + (cw - vis) / 2;
            Ui.panel(g, vx, y, Math.max(2, vis), ch, p < 0.5f ? 0xFF404870 : Pal.RARITY_LIGHT[card.rarity], Pal.INK);
            return;
        }
        drawCard(g, card, x, y, cw, ch, game.profile.cards[card.id], game.timeMs);
        int tw = Font.width("NEUE BONUSKARTE!", 1) + 12;
        Ui.panel(g, (W() - tw) / 2, y - 18, tw, 13, Pal.INK, Pal.GOLD);
        g.textCenter("NEUE BONUSKARTE!", W() / 2, y - 15, Pal.GOLD);
        int by = y + ch + 10;
        ui.button(g, B_EQUIP, x - 20, by, 92, 24, "AUSRÜSTEN", Ui.BTN_PRIMARY);
        ui.button(g, B_KEEP, x + cw - 72, by, 92, 24, "BEHALTEN", Ui.BTN_NORMAL);
    }

    /** Shared card renderer (rewards, album). */
    public static void drawCard(Gfx g, BonusDef card, int x, int y, int cw, int ch, int owned, float timeMs) {
        int rc = Pal.RARITY[card.rarity];
        g.shadow(x, y, cw, ch, 9, 80);
        g.roundRectGrad(x, y, cw, ch, 9, Pal.lighten(rc, 0.25f), Pal.shade(rc, 0.8f));
        g.roundRectGrad(x + 4, y + 4, cw - 8, ch - 8, 7, 0xFFFFFFFF, Pal.RARITY_LIGHT[card.rarity]);
        String rn = WeaponDef.RARITY_NAME[card.rarity].toUpperCase(java.util.Locale.GERMAN);
        int tw = Font.width(rn, 1) + 10;
        g.roundRect(x + (cw - tw) / 2, y + 8, tw, 11, 5, rc);
        g.textCenter(rn, x + cw / 2, y + 10, Pal.WHITE);
        // art window
        int aw = Math.min(64, cw - 24), ah = 56;
        int ax = x + (cw - aw) / 2, ay = y + 24;
        g.roundRectGrad(ax, ay, aw, ah, 7, Pal.lighten(card.color, 0.75f), Pal.lighten(card.color, 0.45f));
        g.roundBorder(ax, ay, aw, ah, 7, 1, Pal.shade(rc, 0.8f));
        int bob = (int) (Math.sin(timeMs / 300.0) * 2);
        g.spriteHD(Art.bonusIcon(card), ax + (aw - 48) / 2, ay + 4 + bob, 3);
        if (card.rarity == WeaponDef.LEGENDAER) {
            float t = (timeMs % 1600) / 1600f;
            g.fillEllipse(ax + 8 + (int) (t * (aw - 16)), ay + 10, 1.5f, 1.5f, 0xE0FFFFFF);
        }
        int ty = ay + ah + 6;
        g.textCenter(card.upper(), x + cw / 2, ty, Pal.INK);
        ty += 11;
        g.textCenter("\"" + card.prefix + "-...\"", x + cw / 2, ty, Pal.INK_SOFT);
        ty += 14;
        for (Effect e : card.effects) {
            List<String> lines = Font.wrap(e.describe(), cw - 16);
            for (String l : lines) {
                g.textCenter(l, x + cw / 2, ty, Pal.INK);
                ty += Font.LINE;
            }
        }
        if (owned >= 0) g.textCenter("Im Besitz: " + owned, x + cw / 2, y + ch - 16, Pal.INK_SOFT);
    }
}
