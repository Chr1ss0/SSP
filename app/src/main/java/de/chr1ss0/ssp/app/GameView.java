package de.chr1ss0.ssp.app;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import de.chr1ss0.ssp.core.Game;
import de.chr1ss0.ssp.core.Gfx;
import de.chr1ss0.ssp.core.Pal;

/**
 * Renders the game's low-res framebuffer scaled up with nearest-neighbour
 * filtering and forwards touch input. Everything runs on the UI thread.
 */
@SuppressLint("ViewConstructor")
public final class GameView extends View {
    private final MainActivity activity;
    private Game game;
    private Bitmap bitmap;
    private final Paint paint = new Paint();
    private final Rect src = new Rect(), dst = new Rect();
    private float scale = 1f;
    private int offX, offY;
    private long lastNanos;
    private boolean running = true;

    public GameView(MainActivity activity, MainActivity platform) {
        super(activity);
        this.activity = activity;
        paint.setFilterBitmap(false);
        paint.setAntiAlias(false);
        paint.setDither(false);
        setFocusable(true);
        setKeepScreenOn(true);
    }

    Game game() {
        return game;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        // fill the full width; one logical pixel = 'scale' screen pixels (fractional, nearest neighbour)
        scale = w / (float) Game.W;
        int vh = (int) (h / scale);
        if (vh < Game.MIN_H) {
            scale = h / (float) Game.MIN_H;
            vh = Game.MIN_H;
        }
        if (game == null) {
            game = new Game(activity, vh);
            activity.startAudio();
        } else {
            game.resize(vh);
        }
        int pw = game.g.pw, ph = game.g.ph;
        if (bitmap == null || bitmap.getWidth() != pw || bitmap.getHeight() != ph) {
            bitmap = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888);
        }
        int dw = Math.round(Game.W * scale), dh = Math.round(game.H * scale);
        offX = (w - dw) / 2;
        offY = Math.max(0, (h - dh) / 2);
        src.set(0, 0, pw, ph);
        dst.set(offX, offY, offX + dw, offY + dh);
        lastNanos = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (game == null || bitmap == null) {
            canvas.drawColor(Pal.BEZEL);
            return;
        }
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 16f : (now - lastNanos) / 1_000_000f;
        lastNanos = now;
        try {
            game.frame(dt);
        } catch (RuntimeException e) {
            // keep the app alive; reset to a safe state
            android.util.Log.e("SSP", "frame failed", e);
        }
        Gfx g = game.g;
        if (bitmap.getWidth() != g.pw || bitmap.getHeight() != g.ph) {
            bitmap = Bitmap.createBitmap(g.pw, g.ph, Bitmap.Config.ARGB_8888);
            src.set(0, 0, g.pw, g.ph);
        }
        bitmap.setPixels(g.px, 0, g.pw, 0, 0, g.pw, g.ph);
        canvas.drawColor(Pal.BEZEL);
        canvas.drawBitmap(bitmap, src, dst, paint);
        if (running) postInvalidateOnAnimation();
    }

    private int vx(float x) {
        return (int) ((x - offX) / scale);
    }

    private int vy(float y) {
        return (int) ((y - offY) / scale);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent e) {
        if (game == null) return true;
        int x = vx(e.getX()), y = vy(e.getY());
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                game.touchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                game.touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                game.touchUp(x, y);
                break;
            case MotionEvent.ACTION_CANCEL:
                game.touchUp(-1000, -1000);
                break;
            default:
                break;
        }
        return true;
    }

    boolean back() {
        return game == null || game.back();
    }

    void pause() {
        running = false;
        if (game != null) game.onPause();
    }

    void resume() {
        running = true;
        lastNanos = 0;
        if (game != null) game.onResume();
        postInvalidateOnAnimation();
    }
}
