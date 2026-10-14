package de.chr1ss0.ssp.core;

/** Title screen with logo, animated hero and main menu. */
public final class TitleScreen extends Screen {
    private static final int B_CONTINUE = 1, B_NEW = 2, B_CHAR = 3, B_DEX = 4, B_CARDS = 5, B_OPTIONS = 6, B_HELP = 7;
    private static final int[] CYCLE = {WeaponDef.SCHERE_ID, WeaponDef.STEIN_ID, WeaponDef.PAPIER_ID};
    private static final String[] WORDS = {"SCHNICK", "SCHNACK", "SCHNUCK"};
    private static final int[] WORD_COLORS = {0xFFFF5A6E, 0xFF5AB4FF, 0xFFFFC93D};

    public TitleScreen(Game game) {
        super(game);
    }

    @Override
    public int music() {
        return Sound.M_TITLE;
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c < 0) return;
        game.sound.sfx(Sound.S_SELECT);
        final Profile p = game.profile;
        switch (c) {
            case B_CONTINUE:
                Flow.continueRun(game);
                break;
            case B_NEW:
                if (!p.created) {
                    game.push(new EditorScreen(game, true));
                } else if (p.run != null) {
                    game.dialog("Aktuellen Lauf aufgeben und neu starten? Deine Bonuskarten bleiben erhalten.",
                            new String[]{"NEU STARTEN", "ABBRECHEN"}, new Game.Choice() {
                                @Override
                                public void chosen(int index) {
                                    if (index == 0) {
                                        RunLogic.onDefeat(p);
                                        Flow.newRun(game);
                                    }
                                }
                            });
                } else {
                    Flow.newRun(game);
                }
                break;
            case B_CHAR: game.push(new EditorScreen(game, false)); break;
            case B_DEX: game.push(new DexScreen(game)); break;
            case B_CARDS: game.push(new AlbumScreen(game)); break;
            case B_OPTIONS: game.push(new OptionsScreen(game)); break;
            case B_HELP: game.push(new HelpScreen(game)); break;
            default: break;
        }
    }

    @Override
    public void render(Gfx g) {
        int w = W(), h = H();
        g.gradientV(0, 0, w, h, 0xFF5446D6, 0xFFFF8C7A);
        // drifting weapon icons in the background
        for (int i = 0; i < 9; i++) {
            int id = (i * 11 + 3) % WeaponDef.ALL.length;
            float t = (float) ((game.timeMs / 9000.0 + i * 0.37) % 1.0);
            int x = (i * 53) % (w - 10);
            int y = (int) (h - t * (h + 40));
            g.spriteHD(WeaponArt.icon(id), x, y, 1, Gfx.MODE_SOLID, 0x30FFFFFF);
        }
        // logo
        int ly = 14 + Math.max(0, Math.min(40, (h - 480) / 3));
        for (int i = 0; i < 3; i++) {
            String word = WORDS[i];
            int ww = Font.width(word, 3);
            int x = (w - ww) / 2 + (i == 1 ? 10 : -10);
            int bob = (int) (Math.sin((game.timeMs / 300.0) + i * 1.3) * 1.5);
            Font.draw(g, word, x + 2, ly + 3 + bob, 0x60201040, 3);
            g.textOutline(word, x, ly + bob, WORD_COLORS[i], 0xFF1E1A3A, 3);
            int ix = i == 1 ? x - 38 : x + ww + 6;
            g.spriteHD(WeaponArt.icon(CYCLE[i]), ix, ly - 6 + bob, 2);
            ly += 28;
        }
        String sub = "DAS WAFFEN-DUELL";
        int sw = Font.width(sub, 1) + 16;
        g.roundRect((w - sw) / 2, ly + 1, sw, 13, 6, 0x50000000);
        g.textCenter(sub, w / 2, ly + 4, Pal.WHITE);

        // menu geometry
        Profile p = game.profile;
        boolean cont = p.run != null;
        int rows = cont ? 5 : 4;
        int menuTop = h - 12 - rows * 26;
        int recordY = menuTop - 14;

        // hero on a platform
        int frame = (int) (game.timeMs / 700) % 3;
        Sprite hero = CharArt.front(game.profile.look, CYCLE[frame]);
        int bounce = (int) Math.abs(Math.sin(game.timeMs / 700.0 * Math.PI) * 4);
        int heroTop = ly + 20;
        int heroBottom = (p.bestStage > 0 ? recordY : menuTop) - 6;
        int space = heroBottom - heroTop;
        int sh = hero.h;
        int hy = heroTop + Math.max(0, (space - sh) / 2);
        g.fillEllipse(w / 2, hy + sh - 3, 40, 8, 0x40000000);
        g.fillEllipse(w / 2, hy + sh - 5, 36, 6, 0x50FFFFFF);
        g.sprite(hero, (w - hero.w) / 2 + 8, hy - bounce, 1);

        int by = menuTop;
        int bx = 20, bw = w - 40, half = (bw - 6) / 2;
        if (cont) {
            ui.button(g, B_CONTINUE, bx, by, bw, 23, "WEITER (Etappe " + p.run.stage + ")", Ui.BTN_PRIMARY);
            by += 26;
        }
        ui.button(g, B_NEW, bx, by, bw, 23, "NEUES SPIEL", cont ? Ui.BTN_NORMAL : Ui.BTN_PRIMARY);
        by += 26;
        ui.button(g, B_CHAR, bx, by, half, 23, "MONSTER", Ui.BTN_NORMAL);
        ui.button(g, B_DEX, bx + half + 6, by, half, 23, "WAFFENDEX", Ui.BTN_NORMAL);
        by += 26;
        ui.button(g, B_CARDS, bx, by, half, 23, "KARTEN", Ui.BTN_NORMAL);
        ui.button(g, B_OPTIONS, bx + half + 6, by, half, 23, "OPTIONEN", Ui.BTN_NORMAL);
        by += 26;
        ui.button(g, B_HELP, bx, by, bw, 23, "ANLEITUNG", Ui.BTN_NORMAL);
        if (p.bestStage > 0) {
            String sep = " " + Font.DOT + " ";
            String rec = "Rekord: " + p.bestStage + (p.bestStage == 1 ? " Etappe" : " Etappen") + sep + "Karten: " + p.totalCards() + sep + "Waffen: " + p.ownedCount() + "/50";
            if (Font.width(rec, 1) > w - 8) rec = "Rekord: " + p.bestStage + sep + "Karten: " + p.totalCards() + sep + "Waffen: " + p.ownedCount();
            g.textCenter(rec, w / 2, recordY, 0xE0FFFFFF);
        }
    }
}
