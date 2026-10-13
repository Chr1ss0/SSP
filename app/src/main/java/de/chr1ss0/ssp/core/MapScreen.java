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
        g.clear(Pal.BG);
        if (run == null || enemy == null) return;
        int stage = run.stage;
        Ui.header(g, "ARENA " + (((stage - 1) / 5) + 1) + ": " + Enemies.arenaName(stage).toUpperCase(java.util.Locale.GERMAN), W());
        int extra = Math.max(0, H() - 360);
        int gap = Math.min(18, extra / 4);
        int total = 84 + gap + 112 + 6 + gap + 38 + gap + 58;
        int ry = 20 + Math.max(2, (H() - 20 - total) / 2);

        // ---- route
        int base = ((stage - 1) / 5) * 5 + 1;
        int nodeY = ry + 58;
        // grass strip
        g.fill(0, nodeY - 4, W(), 18, 0xFFD8E8B8);
        for (int x = 0; x < W(); x += 6) g.pset(x + ((x / 6) & 1) * 3, nodeY + 10, 0xFFA8C888);
        for (int i = 0; i < 5; i++) {
            int x = 28 + i * 46;
            if (i < 4) {
                for (int dx = 8; dx < 38; dx += 4) g.fill(x + dx, nodeY + 1, 2, 2, Pal.GRAY);
            }
        }
        for (int i = 0; i < 5; i++) {
            int s = base + i;
            int x = 28 + i * 46;
            boolean boss = Enemies.isBoss(s);
            boolean done = s < stage;
            boolean cur = s == stage;
            int r = boss ? 8 : 6;
            g.fillCircle(x, nodeY + 2, r + 1, Pal.INK);
            int fill = done ? Pal.HP_GREEN : (cur ? Pal.WHITE : Pal.LIGHT);
            if (boss && !done) fill = cur ? Pal.WHITE : 0xFFE8C8C8;
            g.fillCircle(x, nodeY + 2, r, fill);
            if (done) g.text(String.valueOf(Font.CHECK), x - 2, nodeY - 1, Pal.WHITE);
            if (boss) {
                // tiny crown
                int cx = x - 4, cy = nodeY - 14;
                g.fill(cx, cy + 3, 9, 3, Pal.GOLD);
                g.pset(cx, cy + 2, Pal.GOLD); g.pset(cx + 4, cy + 1, Pal.GOLD); g.pset(cx + 8, cy + 2, Pal.GOLD);
                g.pset(cx + 4, cy + 2, Pal.GOLD);
            }
            g.textCenter(String.valueOf(s), x, nodeY + 16, cur ? Pal.RED : Pal.INK_SOFT);
            if (cur) {
                int bob = (int) (Math.sin(game.timeMs / 250.0) * 1.5);
                Sprite hero = CharArt.front(game.profile.look, run.player.weapons.get(0).def.id);
                g.sprite(hero, x - 24, nodeY - 46 + bob);
            }
        }

        // ---- enemy panel
        int py = ry + 84 + gap;
        int ph = 112;
        boolean boss = Enemies.isBoss(stage);
        Ui.panel(g, 6, py, W() - 12, ph, boss ? 0xFFFBE8E0 : Pal.WHITE, Pal.INK);
        g.text(boss ? "ARENA-BOSS!" : "NÄCHSTER GEGNER", 12, py + 5, boss ? Pal.RED : Pal.INK_SOFT);
        g.fillEllipse(48, py + ph - 8, 30, 4, Pal.GROUND);
        g.sprite(CharArt.front(enemy.look, -1), 8, py + 14, 2);
        int ix = 98;
        g.text(enemy.title, ix, py + 18, Pal.INK_SOFT);
        Font.draw(g, enemy.name, ix, py + 29, Pal.INK, 1);
        g.text("Lv." + enemy.level + "   KP " + enemy.maxHp, ix, py + 41, Pal.INK);
        g.text("WAFFEN:", ix, py + 54, Pal.INK_SOFT);
        for (int i = 0; i < enemy.weapons.size(); i++) {
            int wx = ix + (i % 6) * 22, wy = py + 64 + (i / 6) * 22;
            WeaponInst w = enemy.weapons.get(i);
            Ui.weaponTile(g, w.def.id, wx, wy, true);
            if (w.equipped() > 0) g.fill(wx + 15, wy + 1, 4, 4, Pal.RARITY[3]);
            ui.region(B_ENEMY_WEAPON + i, wx, wy, 20, 20);
        }
        if (infoWeapon >= 0 && infoWeapon < enemy.weapons.size()) {
            WeaponInst w = enemy.weapons.get(infoWeapon);
            g.text(BattleScreen.ellipsize(w.displayName(), W() - ix - 10), ix, py + 88, Pal.INK);
            g.text("Schlägt:", ix, py + 99, Pal.GREEN);
            int[] bl = Element.beatsList(w.def.type);
            for (int k = 0; k < bl.length; k++) g.sprite(Art.typeIcon(bl[k]), ix + 42 + k * 11, py + 98);
            int extraTypes = w.stats().breaks;
            int k2 = bl.length;
            for (int t = 0; t < Element.COUNT; t++) {
                if ((extraTypes & (1 << t)) != 0 && !Element.beats(w.def.type, t)) g.sprite(Art.typeIcon(t), ix + 42 + (k2++) * 11, py + 98);
            }
        } else {
            g.text(boss ? "Sieg: Goldkiste + volle KP" : "Tippe Waffen für Infos", ix, py + 92, Pal.INK_SOFT);
        }

        // ---- player panel
        int my = py + ph + 6 + gap;
        Fighter pl = run.player;
        Ui.panel(g, 6, my, W() - 12, 38, Pal.WHITE, Pal.INK);
        g.text(pl.name, 12, my + 5, Pal.INK);
        g.textRight("Lv." + stage, W() - 12, my + 5, Pal.INK);
        Ui.hpBar(g, 12, my + 17, 120, pl.hp, pl.maxHp);
        g.text(pl.hp + "/" + pl.maxHp, 138, my + 17, Pal.INK);
        g.text("Waffen " + pl.weapons.size() + "/" + Run.MAX_WEAPONS + "   Karten frei: " + freeCards(), 12, my + 27, Pal.INK_SOFT);

        // ---- buttons
        int by = my + 46 + gap;
        String fight = run.phase == Run.PHASE_BATTLE ? "WEITERKÄMPFEN!" : (boss ? "BOSS HERAUSFORDERN!" : "KÄMPFEN!");
        ui.button(g, B_FIGHT, 12, by, W() - 24, 28, fight, Ui.BTN_PRIMARY);
        int bw = (W() - 24 - 12) / 3;
        ui.button(g, B_ARSENAL, 12, by + 32, bw, 22, "ARSENAL", Ui.BTN_NORMAL);
        ui.button(g, B_CARDS, 18 + bw, by + 32, bw, 22, "KARTEN", Ui.BTN_NORMAL);
        ui.button(g, B_MENU, 24 + bw * 2, by + 32, bw, 22, "MENÜ", Ui.BTN_NORMAL);
    }

    private int freeCards() {
        Profile p = game.profile;
        int n = 0;
        for (int i = 0; i < p.cards.length; i++) n += Math.max(0, p.available(i));
        return n;
    }
}
