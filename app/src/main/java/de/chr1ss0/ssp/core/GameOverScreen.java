package de.chr1ss0.ssp.core;

/** Shown after losing a run. Cards are kept. */
public final class GameOverScreen extends Screen {
    private static final int B_NEW = 1, B_TITLE = 2;
    private final int stage;
    private final String by;
    private final Run run;

    public GameOverScreen(Game game, int stage, String by, Run run) {
        super(game);
        this.stage = stage;
        this.by = by;
        this.run = run;
    }

    @Override
    public int music() {
        return Sound.M_GAMEOVER;
    }

    @Override
    public void update(float dt) {
        if (time < 800) {
            ui.click();
            return;
        }
        int c = ui.click();
        if (c == B_NEW) {
            game.sound.sfx(Sound.S_SELECT);
            Flow.newRun(game);
        } else if (c == B_TITLE) {
            game.sound.sfx(Sound.S_SELECT);
            Flow.toTitle(game);
        }
    }

    @Override
    public boolean onBack() {
        Flow.toTitle(game);
        return true;
    }

    @Override
    public void render(Gfx g) {
        g.clear(0xFF202030);
        Profile p = game.profile;
        int y = 22;
        g.textOutline("NIEDERLAGE", (W() - Font.width("NIEDERLAGE", 3)) / 2, y, 0xFFE84848, Pal.INK, 3);
        y += 34;
        Sprite hero = CharArt.front(p.look, -1);
        g.sprite(hero, W() / 2 - 24, y, 1, false, Gfx.MODE_SOLID, 0xFF505068);
        y += 54;
        g.textCenter(p.name + " wurde von", W() / 2, y, Pal.WHITE);
        y += 11;
        g.textCenter(by + " besiegt.", W() / 2, y, Pal.WHITE);
        y += 18;
        Ui.panel(g, 14, y, W() - 28, 66, 0xFF303048, 0xFF606080);
        int sy = y + 6;
        row(g, "Erreichte Etappe", String.valueOf(stage), sy);
        row(g, "Besiegte Gegner", String.valueOf(run != null ? run.wins : 0), sy + 11);
        row(g, "Rundensiege", String.valueOf(run != null ? run.roundsWon : 0), sy + 22);
        row(g, "Gezogene Karten", String.valueOf(run != null ? run.cardsDrawn : 0), sy + 33);
        row(g, "Rekord", "Etappe " + p.bestStage, sy + 44);
        y += 74;
        y += Ui.paragraph(g, "Deine " + p.totalCards() + " Bonuskarten bleiben dir erhalten! Verteile sie im nächsten Lauf neu auf deine Waffen.",
                16, y, W() - 32, 0xFFF0D870);
        int by2 = Math.max(y + 8, H() - 64);
        ui.button(g, B_NEW, 20, by2, W() - 40, 26, "NEUER LAUF", Ui.BTN_PRIMARY);
        ui.button(g, B_TITLE, 20, by2 + 32, W() - 40, 24, "ZUM TITEL", Ui.BTN_NORMAL);
    }

    private void row(Gfx g, String k, String v, int y) {
        g.text(k, 22, y, 0xFFC0C0D8);
        g.textRight(v, W() - 22, y, Pal.WHITE);
    }
}
