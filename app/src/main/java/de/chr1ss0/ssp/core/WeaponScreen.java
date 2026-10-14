package de.chr1ss0.ssp.core;

/**
 * Weapon details: stats, matchups and (for the three special weapons) the
 * bonus card slots plus swapping the weapon. Dex entries are read-only.
 */
public final class WeaponScreen extends Screen {
    private static final int B_BACK = 1, B_SWAP = 2, B_SLOT = 10;
    private final WeaponInst inst;
    private final WeaponDef def;
    private final int specialSlot;
    private final boolean editable;

    public WeaponScreen(Game game, WeaponInst inst, int dexId) {
        this(game, inst, dexId, -1, false);
    }

    public WeaponScreen(Game game, WeaponInst inst, int dexId, int specialSlot, boolean editable) {
        super(game);
        this.inst = inst;
        this.def = inst != null ? inst.def : WeaponDef.get(dexId);
        this.specialSlot = specialSlot;
        this.editable = editable;
    }

    /** The instance currently in the slot (it changes after a swap). */
    private WeaponInst current() {
        if (specialSlot >= 0 && game.profile.run != null) return game.profile.run.player.special(specialSlot);
        return inst;
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        WeaponInst w = current();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (c == B_SWAP && editable && specialSlot >= 0) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new WeaponPickScreen(game, specialSlot));
        } else if (editable && w != null && c >= B_SLOT && c < B_SLOT + w.slots.length) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new CardPickScreen(game, w, c - B_SLOT));
        }
    }

    @Override
    public void render(Gfx g) {
        Ui.background(g);
        WeaponInst w = current();
        WeaponDef d = w != null ? w.def : def;
        String title = w != null ? w.upper() : "#" + (d.id + 1) + " " + d.upper();
        Ui.header(g, BattleScreen.ellipsize(title, W() - 10), W());
        int y = 26;
        Ui.weaponTileBig(g, d.id, 8, y, 56);
        int x = 72;
        Ui.typeBadge(g, d.type, x, y, 64);
        Ui.rarityBadge(g, d.rarity, x + 68, y);
        Stats st = w != null ? w.stats() : null;
        int atk = st != null ? st.atk : d.atk, dfn = st != null ? st.def : d.def;
        g.text(Font.SWORD + " Angriff  " + atk + (atk != d.atk ? "  (" + d.atk + (atk - d.atk >= 0 ? "+" : "") + (atk - d.atk) + ")" : ""), x, y + 16, Pal.INK);
        g.text(Font.SHIELD + " Abwehr   " + dfn + (dfn != d.def ? "  (" + d.def + (dfn - d.def >= 0 ? "+" : "") + (dfn - d.def) + ")" : ""), x, y + 27, Pal.INK);
        g.text(d.isClassic() ? "Immer einsetzbar (∞)" : "1× pro Gegner " + Font.DOT + " " + d.slots + " Kartenplätze", x, y + 39, d.isClassic() ? Pal.INK_SOFT : Pal.ACCENT);
        y += 62;
        y += Ui.paragraph(g, d.desc, 8, y, W() - 16, Pal.INK_SOFT);
        for (Effect e : d.innate) {
            g.text(Font.STAR + " Fähigkeit: " + e.describe(), 8, y, Pal.BLUE);
            y += Font.LINE;
        }
        y += 3;
        g.text("SCHLÄGT:", 8, y, Pal.GREEN);
        y += 10;
        y = chips(g, Element.beatsList(d.type), st != null ? st.breaks : 0, y, true);
        g.text("VERLIERT GEGEN:", 8, y, Pal.RED);
        y += 10;
        y = chips(g, Element.losesList(d.type), st != null ? st.immune : 0, y, false);

        if (w != null && d.isClassic()) {
            y += 4;
            Ui.paragraph(g, "Stein, Schere und Papier sind fest in deinem Arsenal und unbegrenzt einsetzbar. Bonuskarten tragen nur deine drei Spezialwaffen.",
                    8, y, W() - 16, Pal.INK_SOFT);
        } else if (w != null && specialSlot >= 0) {
            y += 2;
            g.text(editable ? "BONUSKARTEN (tippen zum Ändern):" : "BONUSKARTEN:", 8, y, Pal.INK);
            y += 11;
            int rowH = Math.max(26, Math.min(32, (H() - y - 64) / Math.max(1, w.slots.length)));
            for (int s = 0; s < w.slots.length; s++) {
                int id = B_SLOT + s;
                boolean pr = editable && ui.isPressed(id);
                int ry = y + (pr ? 1 : 0);
                Ui.panel(g, 6, ry, W() - 12, rowH - 3, pr ? Pal.SELECT : Pal.WHITE, Pal.INK);
                int b = w.slots[s];
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
                if (editable) ui.region(id, 6, y, W() - 12, rowH - 3);
                y += rowH;
            }
            if (editable) ui.button(g, B_SWAP, 30, H() - 58, W() - 60, 24, "WAFFE TAUSCHEN", Ui.BTN_PRIMARY);
        }
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }

    /** Draws type chips; extra types from cards (breaks/immunities) are appended. */
    private int chips(Gfx g, int[] types, int extraMask, int y, boolean beats) {
        int x = 8;
        int cw = 56;
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
