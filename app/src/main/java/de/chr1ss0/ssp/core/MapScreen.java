package de.chr1ss0.ssp.core;

/** Route overview between battles: progress through the arena, next opponent, player status. */
public final class MapScreen extends Screen {
    private static final int B_FIGHT = 1, B_ARSENAL = 2, B_CARDS = 3, B_MENU = 4;
    private static final int B_ENEMY_WEAPON = 100;

    private Fighter enemy;
    private int infoWeapon = -1;

    public MapScreen(Game game) {
        super(game);
    }

    @Override
    public void enter() {
        Run run = game.profile.run;
        if (run == null) return;
        run.syncPlayer(game.profile);
        enemy = run.createEnemy();
        for (WeaponInst w : enemy.weapons) game.profile.seen[w.def.id] = true;
    }

    @Override
    public int music() {
        return Sound.M_MAP;
    }

    @Override
    public void update(float dt) {
        if (game.profile.run == null) {
            Flow.toTitle(game);
            return;
        }
        int c = ui.click();
        if (c < 0) return;
        if (c >= B_ENEMY_WEAPON) {
            infoWeapon = infoWeapon == c - B_ENEMY_WEAPON ? -1 : c - B_ENEMY_WEAPON;
            game.sound.sfx(Sound.S_CURSOR);
            return;
        }
        switch (c) {
            case B_FIGHT:
                game.sound.sfx(Sound.S_SELECT);
                game.profile.run.phase = Run.PHASE_BATTLE;
                game.save();
                Flow.startBattle(game);
                break;
            case B_ARSENAL:
                game.sound.sfx(Sound.S_SELECT);
                game.push(new ArsenalScreen(game, false));
                break;
            case B_CARDS:
                game.sound.sfx(Sound.S_SELECT);
                game.push(new AlbumScreen(game));
                break;
            case B_MENU:
                game.sound.sfx(Sound.S_SELECT);
                game.push(new PauseScreen(game, false));
                break;
            default: break;
        }
    }

    @Override
    public boolean onBack() {
        game.sound.sfx(Sound.S_SELECT);
        game.push(new PauseScreen(game, false));
        return true;
    }

    @Override
    public void render(Gfx g) {
        Run run = game.profile.run;
        Ui.background(g);
        if (run == null || enemy == null) return;
        int stage = run.stage;
        Ui.header(g, "ARENA " + (((stage - 1) / 5) + 1) + " " + Font.DOT + " " + Enemies.arenaName(stage).toUpperCase(java.util.Locale.GERMAN), W());
        // route at the top, buttons at the bottom, panels spread in between
        int extra = Math.max(0, H() - 360);
        int ry = 24 + Math.min(8, extra / 20);
        int by = H() - 58 - Math.min(8, extra / 20);
        int ph = 112, mh = 44, rh = 30;
        int spare = by - 4 - (ry + 76) - ph - mh;
        boolean strip = spare >= rh + 3 * 10;
        if (strip) spare -= rh;
        int gap = Math.max(2, spare / (strip ? 4 : 3));

        // ---- route
        int base = ((stage - 1) / 5) * 5 + 1;
        int nodeY = ry + 56;
        Ui.panel(g, 6, ry + 26, W() - 12, 44, 0xFFE9F6DD, 0xFFB9DDA0);
        for (int i = 0; i < 4; i++) {
            int x = 28 + i * 46;
            for (int dx = 10; dx < 38; dx += 5) g.roundRect(x + dx, nodeY + 1, 3, 2, 1, 0xFFA6C793);
        }
        for (int i = 0; i < 5; i++) {
            int s = base + i;
            int x = 28 + i * 46;
            boolean boss = Enemies.isBoss(s);
            boolean done = s < stage;
            boolean cur = s == stage;
            int r = boss ? 8 : 6;
            int fill = done ? Pal.HP_GREEN : (cur ? Pal.WHITE : 0xFFDDE3EC);
            if (boss && !done) fill = cur ? Pal.WHITE : 0xFFFFD9D9;
            g.fillEllipse(x, nodeY + 3, r + 1, r + 1, 0x40000000);
            g.fillEllipse(x, nodeY + 2, r + 1, r + 1, done ? 0xFF1E9E58 : (boss ? 0xFFE0626A : 0xFF9AA3BC));
            g.fillEllipse(x, nodeY + 2, r, r, fill);
            if (done) g.textCenter(String.valueOf(Font.CHECK), x, nodeY - 1, Pal.WHITE);
            if (boss) {
                int cx = x - 5, cy = nodeY - 16;
                g.roundRect(cx, cy + 3, 11, 4, 1, Pal.GOLD);
                g.fill(cx, cy, 2, 3, Pal.GOLD); g.fill(cx + 4, cy - 1, 3, 4, Pal.GOLD); g.fill(cx + 9, cy, 2, 3, Pal.GOLD);
            }
            g.textCenter(String.valueOf(s), x, nodeY + 13, cur ? Pal.RED : Pal.INK_SOFT);
            if (cur) {
                int bob = (int) (Math.sin(game.timeMs / 250.0) * 1.5);
                g.fillEllipse(x, nodeY + 1, 10, 3, 0x40000000);
                g.spriteHD(CharArt.front(game.profile.look, run.player.special(0).def.id), x - 24, nodeY - 47 + bob);
            }
        }

        // ---- enemy card
        int py = ry + 76 + gap;
        boolean boss = Enemies.isBoss(stage);
        Ui.box(g, 6, py, W() - 12, ph);
        if (boss) {
            g.roundRect(12, py + 6, Font.width("ARENA-BOSS", 1) + 10, 11, 5, Pal.RED);
            g.text("ARENA-BOSS", 17, py + 8, Pal.WHITE);
        } else {
            g.text("NÄCHSTER GEGNER", 12, py + 7, Pal.INK_SOFT);
        }
        g.fillEllipse(50, py + ph - 9, 32, 5, 0x30000000);
        g.sprite(CharArt.front(enemy.look, -1), 10, py + 12, 1);
        int ix = 98;
        g.text(enemy.title, ix, py + 20, Pal.INK_SOFT);
        g.text(enemy.name, ix, py + 31, Pal.INK);
        g.text("Lv." + enemy.level + "   " + Font.HEART + " " + enemy.maxHp + " KP", ix, py + 43, Pal.INK);
        g.text("ARSENAL", ix, py + 56, Pal.INK_SOFT);
        for (int i = 0; i < enemy.weapons.size(); i++) {
            int wx = ix + (i % 6) * 22, wy = py + 66;
            WeaponInst w = enemy.weapons.get(i);
            Ui.weaponTile(g, w.def.id, wx, wy, true);
            if (w.equipped() > 0) g.fillEllipse(wx + 17, wy + 3, 2, 2, Pal.RARITY[3]);
            if (infoWeapon == i) g.roundBorder(wx - 1, wy - 1, 22, 22, 5, 2, Pal.RED);
            ui.region(B_ENEMY_WEAPON + i, wx, wy, 20, 20);
        }
        if (infoWeapon >= 0 && infoWeapon < enemy.weapons.size()) {
            WeaponInst w = enemy.weapons.get(infoWeapon);
            g.text(BattleScreen.ellipsize(w.displayName(), W() - ix - 12), ix, py + 89, Pal.INK);
            g.text("Schlägt:", ix, py + 100, Pal.GREEN);
            int[] bl = Element.beatsList(w.def.type);
            int k2 = 0;
            for (int k = 0; k < bl.length; k++) g.spriteHD(Art.typeIcon(bl[k]), ix + 38 + (k2++) * 11, py + 99);
            int extraTypes = w.stats().breaks;
            for (int t = 0; t < Element.COUNT; t++) {
                if ((extraTypes & (1 << t)) != 0 && !Element.beats(w.def.type, t)) g.spriteHD(Art.typeIcon(t), ix + 38 + (k2++) * 11, py + 99);
            }
        } else {
            Ui.paragraph(g, boss ? "Sieg: Goldkiste, 2 Karten + volle KP" : "Tippe eine Waffe für Infos", ix, py + 90, W() - ix - 12, Pal.INK_SOFT);
        }

        // ---- reward preview
        int my = py + ph + gap;
        if (strip) {
            drawPrize(g, my, rh, boss);
            my += rh + gap;
        }

        // ---- player panel
        Fighter pl = run.player;
        Ui.box(g, 6, my, W() - 12, 44);
        g.text(pl.name, 13, my + 6, Pal.INK);
        g.textRight("Lv." + stage, W() - 13, my + 6, Pal.ACCENT);
        Ui.hpBar(g, 13, my + 18, 120, pl.hp, pl.maxHp);
        g.text(pl.hp + " / " + pl.maxHp, 140, my + 18, Pal.INK);
        g.text("Freie Karten: " + freeCards(), 13, my + 30, Pal.INK_SOFT);
        for (int s = 0; s < Run.SPECIALS; s++) {
            WeaponInst w = pl.special(s);
            int wx = W() - 13 - (Run.SPECIALS - s) * 15;
            g.roundRect(wx, my + 28, 13, 13, 3, Pal.lighten(Element.COLOR[w.def.type], 0.5f));
            g.spriteHD(WeaponArt.iconSmall(w.def.id), wx + 1, my + 29);
        }

        // ---- buttons
        String fight = run.phase == Run.PHASE_BATTLE ? "WEITERKÄMPFEN!" : (boss ? "BOSS HERAUSFORDERN!" : "KÄMPFEN!");
        ui.button(g, B_FIGHT, 12, by, W() - 24, 28, fight, Ui.BTN_PRIMARY);
        int bw = (W() - 24 - 12) / 3;
        ui.button(g, B_ARSENAL, 12, by + 32, bw, 22, "ARSENAL", Ui.BTN_NORMAL);
        ui.button(g, B_CARDS, 18 + bw, by + 32, bw, 22, "KARTEN", Ui.BTN_NORMAL);
        ui.button(g, B_MENU, 24 + bw * 2, by + 32, bw, 22, "MENÜ", Ui.BTN_NORMAL);
    }

    /** What a victory brings: two bonus cards and a box. */
    private void drawPrize(Gfx g, int y, int h, boolean boss) {
        Ui.box(g, 6, y, W() - 12, h);
        String cards = Run.CARDS_PER_WIN + " Bonuskarten";
        String box = boss ? "Goldkiste" : "Kiste";
        Sprite body = Art.box(boss ? Art.BOX_GOLD : Art.BOX_WOOD, 0), lid = Art.box(boss ? Art.BOX_GOLD : Art.BOX_WOOD, 1);
        int lw = Font.width("SIEG:", 1), cw = Font.width(cards, 1), plus = Font.width("+", 1), bw = Font.width(box, 1);
        int total = lw + 6 + 19 + 4 + cw + 6 + plus + 6 + body.w / 2 + 4 + bw;
        int x = (W() - total) / 2;
        int ty = y + h / 2 - 4;
        g.text("SIEG:", x, ty, Pal.INK_SOFT);
        x += lw + 6;
        for (int i = 0; i < 2; i++) {
            int cx = x + i * 7, cy = y + h / 2 - 8 + (i == 0 ? 1 : -1);
            g.roundRectGrad(cx, cy, 12, 16, 3, 0xFF8C80FF, Pal.ACCENT_DARK);
            g.roundBorder(cx, cy, 12, 16, 3, 1, Pal.WHITE);
            g.textCenter(String.valueOf(Font.STAR), cx + 6, cy + 4, 0xFFFFD86A);
        }
        x += 19 + 4;
        g.text(cards, x, ty, Pal.INK);
        x += cw + 6;
        g.text("+", x, ty, Pal.INK_SOFT);
        x += plus + 6;
        int bx = x, byy = y + h / 2 - 1;
        g.spriteHD(body, bx, byy);
        g.spriteHD(lid, bx, byy - lid.h / 2 + 2);
        x += body.w / 2 + 4;
        g.text(box, x, ty, boss ? 0xFFC98A00 : Pal.INK);
    }

    private int freeCards() {
        Profile p = game.profile;
        int n = 0;
        for (int i = 0; i < p.cards.length; i++) n += Math.max(0, p.available(i));
        return n;
    }
}
