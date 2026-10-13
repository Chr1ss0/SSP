package de.chr1ss0.ssp.core;

/** Pause menu (overlay) for map and battle. */
public final class PauseScreen extends Screen {
    private static final int B_RESUME = 1, B_OPTIONS = 2, B_HELP = 3, B_CHART = 4, B_TITLE = 5, B_GIVEUP = 6;
    private final boolean inBattle;

    public PauseScreen(Game game, boolean inBattle) {
        super(game);
        this.inBattle = inBattle;
    }

    @Override
    public boolean isOverlay() {
        return true;
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c < 0) return;
        game.sound.sfx(Sound.S_SELECT);
        switch (c) {
            case B_RESUME: game.pop(); break;
            case B_OPTIONS: game.push(new OptionsScreen(game)); break;
            case B_HELP: game.push(new HelpScreen(game)); break;
            case B_CHART: game.push(new ChartScreen(game)); break;
            case B_TITLE: Flow.toTitle(game); break;
            case B_GIVEUP:
                game.dialog("Lauf wirklich aufgeben? Deine Bonuskarten bleiben erhalten.", new String[]{"AUFGEBEN", "NEIN"},
                        new Game.Choice() {
                            @Override
                            public void chosen(int index) {
                                if (index != 0) return;
                                Run run = game.profile.run;
                                int stage = run != null ? run.stage : 1;
                                RunLogic.onDefeat(game.profile);
                                game.save();
                                game.set(new GameOverScreen(game, stage, "der eigenen Aufgabe", run), Game.TR_FADE);
                            }
                        });
                break;
            default: break;
        }
    }

    @Override
    public boolean onBack() {
        game.pop();
        return true;
    }

    @Override
    public void render(Gfx g) {
        g.tint(Pal.INK, 140);
        int w = 170, h = 6 * 28 + 28;
        int x = (W() - w) / 2, y = (H() - h) / 2;
        Ui.box(g, x, y, w, h);
        g.textCenter("PAUSE", W() / 2, y + 9, Pal.INK);
        int by = y + 24;
        ui.button(g, B_RESUME, x + 12, by, w - 24, 24, "WEITER", Ui.BTN_PRIMARY);
        ui.button(g, B_OPTIONS, x + 12, by + 28, w - 24, 24, "OPTIONEN", Ui.BTN_NORMAL);
        ui.button(g, B_HELP, x + 12, by + 56, w - 24, 24, "ANLEITUNG", Ui.BTN_NORMAL);
        ui.button(g, B_CHART, x + 12, by + 84, w - 24, 24, "TYPENTABELLE", Ui.BTN_NORMAL);
        ui.button(g, B_TITLE, x + 12, by + 112, w - 24, 24, "ZUM TITEL", Ui.BTN_NORMAL);
        ui.button(g, B_GIVEUP, x + 12, by + 140, w - 24, 24, "LAUF AUFGEBEN", Ui.BTN_DANGER);
    }
}
