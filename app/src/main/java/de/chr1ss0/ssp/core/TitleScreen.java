package de.chr1ss0.ssp.core;

/** Title screen with logo, animated hero and main menu. */
public final class TitleScreen extends Screen {
    private static final int B_CONTINUE = 1, B_NEW = 2, B_CHAR = 3, B_DEX = 4, B_CARDS = 5, B_OPTIONS = 6, B_HELP = 7;
    private static final int[] CYCLE = {WeaponDef.SCHERE_ID, WeaponDef.STEIN_ID, WeaponDef.PAPIER_ID};
    private static final String[] WORDS = {"SCHNICK", "SCHNACK", "SCHNUCK"};
    private static final int[] WORD_COLORS = {0xFFE84848, 0xFF3878E8, 0xFFF0B020};

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
        g.clear(Pal.BG);
        int w = W(), h = H();
        // checkered border band
        for (int x = 0; x < w; x += 8) {
            int c = ((x / 8) & 1) == 0 ? 0xFFE8E0C8 : 0xFFF0EAD8;
            g.fill(x, 0, 8, 4, c);
            g.fill(x, h - 4, 8, 4, c);
        }
        // logo (pushed down a little on tall screens)
        int ly = 10 + Math.max(0, Math.min(40, (h - 480) / 3));
        for (int i = 0; i < 3; i++) {
            String word = WORDS[i];
            int ww = Font.width(word, 3);
            int x = (w - ww) / 2 + (i == 1 ? 10 : -10);
            int bob = (int) (Math.sin((game.timeMs / 300.0) + i * 1.3) * 1.5);
            Font.draw(g, word, x + 2, ly + 2 + bob, 0xFF8C8478, 3);
            g.textOutline(word, x, ly + bob, WORD_COLORS[i], Pal.INK, 3);
            int ix = i == 1 ? x - 38 : x + ww + 6;
            g.sprite(WeaponArt.icon(CYCLE[i]), ix, ly - 6 + bob, 2);
            ly += 27;
        }
        g.textCenter("Das Waffen-Duell", w / 2, ly + 2, Pal.INK_SOFT);

        // menu geometry
        Profile p = game.profile;
        boolean cont = p.run != null;
        int rows = cont ? 5 : 4;
        int menuTop = h - 12 - rows * 26;
        int recordY = menuTop - 13;

        // hero
        int frame = (int) (game.timeMs / 700) % 3;
        Sprite hero = CharArt.front(game.profile.look, CYCLE[frame]);
        int bounce = (int) Math.abs(Math.sin(game.timeMs / 700.0 * Math.PI) * 4);
        int heroTop = ly + 12;
        int heroBottom = (p.bestStage > 0 ? recordY : menuTop) - 4;
        int space = heroBottom - heroTop;
        int scale = space >= 150 ? 3 : (space >= 98 ? 2 : 1);
        int sh = hero.h * scale;
        int hy = heroTop + Math.max(0, (space - sh) / 2);
        g.fillEllipse(w / 2, hy + sh - 3, 16 * scale, 3 * scale, Pal.GROUND);
        g.sprite(hero, (w - hero.w * scale) / 2 + 4 * scale, hy - bounce, scale);

        int by = menuTop;
        int bx = 20, bw = w - 40, half = (bw - 6) / 2;
        if (cont) {
            ui.button(g, B_CONTINUE, bx, by, bw, 23, "WEITER (Etappe " + p.run.stage + ")", Ui.BTN_PRIMARY);
            by += 26;
        }
        ui.button(g, B_NEW, bx, by, bw, 23, "NEUES SPIEL", cont ? Ui.BTN_NORMAL : Ui.BTN_PRIMARY);
        by += 26;
        ui.button(g, B_CHAR, bx, by, half, 23, "CHARAKTER", Ui.BTN_NORMAL);
        ui.button(g, B_DEX, bx + half + 6, by, half, 23, "WAFFENDEX", Ui.BTN_NORMAL);
        by += 26;
        ui.button(g, B_CARDS, bx, by, half, 23, "KARTEN", Ui.BTN_NORMAL);
        ui.button(g, B_OPTIONS, bx + half + 6, by, half, 23, "OPTIONEN", Ui.BTN_NORMAL);
        by += 26;
        ui.button(g, B_HELP, bx, by, bw, 23, "ANLEITUNG", Ui.BTN_GHOST);
        if (p.bestStage > 0) {
            g.textCenter("Rekord: Etappe " + p.bestStage + "  " + Font.DOT + "  Karten: " + p.totalCards(), w / 2, recordY, Pal.INK_SOFT);
        }
    }
}
