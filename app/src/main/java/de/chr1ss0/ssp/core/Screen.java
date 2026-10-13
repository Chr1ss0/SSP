package de.chr1ss0.ssp.core;

/** Base class of all game screens. */
public abstract class Screen {
    protected final Game game;
    protected final Ui ui;
    protected float time; // ms since enter

    protected Screen(Game game) {
        this.game = game;
        this.ui = game.ui;
    }

    /** Called when the screen becomes the top screen (also after a pop back to it). */
    public void enter() {}

    public void update(float dt) {}

    public abstract void render(Gfx g);

    /** Raw touch hooks (most screens only use ui.click()). */
    public void onDown(int x, int y) {}

    public void onUp(int x, int y) {}

    /** Hardware back. Return true if handled. */
    public boolean onBack() {
        return false;
    }

    /** Music track to play while this screen is shown (-1 = keep). */
    public int music() {
        return -1;
    }

    /** Overlay screens render the screen below them first. */
    public boolean isOverlay() {
        return false;
    }

    protected int W() {
        return Game.W;
    }

    protected int H() {
        return game.H;
    }
}
