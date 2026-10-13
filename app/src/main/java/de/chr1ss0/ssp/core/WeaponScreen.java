package de.chr1ss0.ssp.core;

/**
 * Weapon details: stats, matchups and (for owned weapons) the bonus card slots.
 * Either an owned instance (editable) or a dex entry (read-only).
 */
public final class WeaponScreen extends Screen {
    private static final int B_BACK = 1, B_SLOT = 10;
    private final WeaponInst inst;
    private final WeaponDef def;

    public WeaponScreen(Game game, WeaponInst inst, int dexId) {
        super(game);
        this.inst = inst;
        this.def = inst != null ? inst.def : WeaponDef.get(dexId);
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (inst != null && c >= B_SLOT && c < B_SLOT + inst.slots.length) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new CardPickScreen(game, inst, c - B_SLOT));
        }
    }

    @Override
    public void render(Gfx g) {
        g.clear(Pal.BG);
        String title = inst != null ? inst.upper() : "#" + (def.id + 1) + " " + def.upper();
        Ui.header(g, BattleScreen.ellipsize(title, W() - 10), W());
        int y = 24;
        int tc = Element.COLOR[def.type];
        Ui.panel(g, 8, y, 56, 56, Pal.lighten(tc, 0.5f), Pal.shade(tc, 0.6f));
        g.sprite(WeaponArt.icon(def.id), 12, y + 4, 3);
        int x = 72;
        Ui.typeBadge(g, def.type, x, y, 60);
        Ui.rarityBadge(g, def.rarity, x + 64, y);
        Stats st = inst != null ? inst.stats() : null;
        int atk = st != null ? st.atk : def.atk, dfn = st != null ? st.def : def.def;
        g.text(Font.SWORD + " Angriff  " + atk + (atk != def.atk ? "  (" + def.atk + "+" + (atk - def.atk) + ")" : ""), x, y + 16, Pal.INK);
        g.text(Font.SHIELD + " Abwehr   " + dfn + (dfn != def.def ? "  (" + def.def + (dfn - def.def >= 0 ? "+" : "") + (dfn - def.def) + ")" : ""), x, y + 27, Pal.INK);
        g.text("Kartenplätze: " + def.slots, x, y + 38, Pal.INK_SOFT);
        y += 62;
        y += Ui.paragraph(g, def.desc, 8, y, W() - 16, Pal.INK_SOFT);
        for (Effect e : def.innate) {
            g.text(Font.STAR + " Fähigkeit: " + e.describe(), 8, y, Pal.BLUE);
            y += Font.LINE;
        }
        y += 3;
        g.text("SCHLÄGT:", 8, y, Pal.GREEN);
        y += 10;
        y = chips(g, Element.beatsList(def.type), st != null ? st.breaks : 0, y, true);
        g.text("VERLIERT GEGEN:", 8, y, Pal.RED);
        y += 10;
        y = chips(g, Element.losesList(def.type), st != null ? st.immune : 0, y, false);

        if (inst != null) {
            y += 2;
            g.text("BONUSKARTEN (tippen zum Ändern):", 8, y, Pal.INK);
            y += 11;
            int rowH = Math.max(26, Math.min(32, (H() - y - 36) / inst.slots.length));
            for (int s = 0; s < inst.slots.length; s++) {
                int id = B_SLOT + s;
                boolean pr = ui.isPressed(id);
                int ry = y + (pr ? 1 : 0);
                Ui.panel(g, 6, ry, W() - 12, rowH - 3, pr ? Pal.SELECT : Pal.WHITE, Pal.INK);
                int b = inst.slots[s];
                int ty = ry + (rowH - 3) / 2 - 4;
                if (b >= 0) {
                    BonusDef bd = BonusDef.get(b);
                    Ui.cardTile(g, bd, 9, ry + (rowH - 23) / 2);
                    g.text(bd.name, 33, ty - 5, Pal.RARITY[bd.rarity]);
                    g.text(BattleScreen.ellipsize(bd.describe().replace("\n", ", "), W() - 50), 33, ty + 6, Pal.INK_SOFT);
                } else {
                    Ui.emptySlot(g, 9, ry + (rowH - 23) / 2, 20);
                    g.text("Leerer Platz", 33, ty, Pal.GRAY);
                }
                ui.region(id, 6, y, W() - 12, rowH - 3);
                y += rowH;
            }
        }
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }

    /** Draws type chips; extra types from cards (breaks/immunities) are appended. */
    private int chips(Gfx g, int[] types, int extraMask, int y, boolean beats) {
        int x = 8;
        int cw = 54;
        for (int t : types) {
            Ui.typeBadge(g, t, x, y, cw - 3);
            x += cw;
        }
        y += 14;
        if (extraMask != 0) {
            StringBuilder sb = new StringBuilder(beats ? "+ Bonus: besiegt auch " : "+ Bonus: immun gegen ");
            boolean first = true;
            for (int t = 0; t < Element.COUNT; t++) {
                if ((extraMask & (1 << t)) == 0) continue;
                if (!first) sb.append(", ");
                sb.append(Element.NAME[t]);
                first = false;
            }
            g.text(sb.toString(), 8, y, beats ? Pal.GREEN : Pal.BLUE);
            y += 11;
        }
        return y;
    }
}
