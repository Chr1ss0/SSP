package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.Random;

/** Root object: screen stack, transitions, input routing, saving, audio. */
public final class Game {
    public static final int W = 240;
    public static final int MIN_H = 360;

    public int H;
    public Gfx g;
    public final Ui ui = new Ui();
    public final Platform platform;
    public final Sound sound = new Sound();
    public final Random rng = new Random();
    public Profile profile;
    public float timeMs;

    private final ArrayList<Screen> stack = new ArrayList<Screen>();

    public static final int TR_NONE = 0, TR_FADE = 1, TR_STRIPES = 2, TR_WHITE = 3;
    private static final int OP_SET = 0, OP_PUSH = 1, OP_POP = 2;
    private int trKind, trPhase, op;
    private boolean pending;
    private float trT, trDur;
    private Screen opScreen;
    private int shakeMs, shakeAmp;
    private int flashMs;

    public interface Choice {
        void chosen(int index);
    }

    public Game(Platform platform, int height) {
        this.platform = platform;
        resize(height);
        String data = null;
        try {
            data = platform.loadSave();
        } catch (RuntimeException e) {
            data = null;
        }
        profile = Profile.decode(data);
        applyVolumes();
        stack.add(new TitleScreen(this));
        stack.get(0).enter();
        sound.music(stack.get(0).music());
    }

    public void resize(int height) {
        H = Math.max(MIN_H, height);
        if (g == null || g.h != H) g = new Gfx(W, H, Font.K);
    }

    public void applyVolumes() {
        sound.setVolumes(profile.musicVol, profile.sfxVol);
    }

    // ---------------------------------------------------------------- navigation

    public Screen top() {
        return stack.isEmpty() ? null : stack.get(stack.size() - 1);
    }

    public void set(Screen s, int transition) {
        startOp(OP_SET, s, transition);
    }

    public void push(Screen s) {
        startOp(OP_PUSH, s, s.isOverlay() ? TR_NONE : TR_FADE);
    }

    public void pushWith(Screen s, int transition) {
        startOp(OP_PUSH, s, transition);
    }

    public void pop() {
        startOp(OP_POP, null, top() != null && top().isOverlay() ? TR_NONE : TR_FADE);
    }

    private void startOp(int op, Screen s, int transition) {
        // finish a still pending navigation first
        if (pending) applyOp();
        this.op = op;
        this.opScreen = s;
        this.trKind = transition;
        this.pending = true;
        if (transition == TR_NONE) {
            trPhase = 0;
            applyOp();
        } else {
            trPhase = 1;
            trT = 0;
            trDur = transition == TR_STRIPES ? 520 : (transition == TR_WHITE ? 300 : 160);
        }
        ui.reset();
    }

    private void applyOp() {
        if (!pending) return;
        pending = false;
        int o = op;
        Screen s = opScreen;
        opScreen = null;
        switch (o) {
            case OP_SET: stack.clear(); stack.add(s); break;
            case OP_PUSH: stack.add(s); break;
            case OP_POP: if (stack.size() > 1) stack.remove(stack.size() - 1); break;
            default: break;
        }
        ui.reset();
        Screen t = top();
        if (t != null) {
            t.time = 0;
            int m = t.music();
            if (m >= 0) sound.music(m);
            // enter() may itself navigate (start a new op); that is safe now
            t.enter();
        }
    }

    public void dialog(String text, String[] options, Choice choice) {
        push(new DialogScreen(this, text, options, choice));
    }

    // ---------------------------------------------------------------- effects

    public void shake(int ms, int amp) {
        shakeMs = ms;
        shakeAmp = amp;
    }

    public void flash(int ms) {
        flashMs = ms;
    }

    public void vibrate(int ms) {
        if (profile.vibrate) {
            try {
                platform.vibrate(ms);
            } catch (RuntimeException ignored) {
            }
        }
    }

    public void save() {
        try {
            platform.writeSave(profile.encode());
        } catch (RuntimeException ignored) {
        }
    }

    // ---------------------------------------------------------------- loop

    public void frame(float dt) {
        if (dt > 100) dt = 100;
        timeMs += dt;
        ui.update(dt);
        if (trPhase == 1) {
            trT += dt;
            if (trT >= trDur) {
                trPhase = 2;
                trT = 0;
                applyOp();
            }
        } else if (trPhase == 2) {
            trT += dt;
            if (trT >= trDur) trPhase = 0;
        }
        Screen t = top();
        if (t != null && trPhase != 1) {
            t.time += dt;
            t.update(dt);
        }
        if (shakeMs > 0) shakeMs -= dt;
        if (flashMs > 0) flashMs -= dt;
        render();
    }

    private void render() {
        ui.beginFrame();
        g.noClip();
        g.tx = 0;
        g.ty = 0;
        if (shakeMs > 0) {
            g.tx = rng.nextInt(shakeAmp * 2 + 1) - shakeAmp;
            g.ty = rng.nextInt(shakeAmp * 2 + 1) - shakeAmp;
        }
        int base = stack.size() - 1;
        while (base > 0 && stack.get(base).isOverlay()) base--;
        for (int i = base; i < stack.size(); i++) {
            ui.setActive(i == stack.size() - 1 && trPhase == 0);
            stack.get(i).render(g);
        }
        ui.setActive(true);
        g.tx = 0;
        g.ty = 0;
        g.noClip();
        if (flashMs > 0) g.tint(Pal.WHITE, Math.min(200, flashMs * 3));
        if (trPhase != 0) drawTransition();
        ui.endFrame();
    }

    private void drawTransition() {
        float p = Math.min(1f, trT / trDur);
        float a = trPhase == 1 ? p : 1f - p;
        switch (trKind) {
            case TR_STRIPES: {
                int bands = 12;
                int bh = (H + bands - 1) / bands;
                for (int i = 0; i < bands; i++) {
                    int w = (int) (W * Math.min(1f, Math.max(0f, a * 1.6f - (i % 2) * 0.3f)));
                    if ((i & 1) == 0) g.fill(0, i * bh, w, bh, Pal.INK);
                    else g.fill(W - w, i * bh, w, bh, Pal.INK);
                }
                break;
            }
            case TR_WHITE:
                g.tint(Pal.WHITE, (int) (a * 255));
                break;
            default:
                g.tint(Pal.INK, (int) (a * 255));
                break;
        }
    }

    // ---------------------------------------------------------------- input

    public void touchDown(int x, int y) {
        if (trPhase != 0) return;
        ui.onDown(x, y);
        Screen t = top();
        if (t != null) t.onDown(x, y);
    }

    public void touchMove(int x, int y) {
        ui.onMove(x, y);
    }

    public void touchUp(int x, int y) {
        if (trPhase != 0) {
            ui.reset();
            return;
        }
        ui.onUp(x, y);
        Screen t = top();
        if (t != null) t.onUp(x, y);
    }

    /** Returns false if the app should close. */
    public boolean back() {
        if (trPhase != 0) return true;
        Screen t = top();
        if (t != null && t.onBack()) return true;
        if (stack.size() > 1) {
            sound.sfx(Sound.S_BACK);
            pop();
            return true;
        }
        return false;
    }

    public void onPause() {
        save();
        sound.pause(true);
    }

    public void onResume() {
        sound.pause(false);
    }
}
