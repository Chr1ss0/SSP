package de.chr1ss0.ssp.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import de.chr1ss0.ssp.core.Platform;

/** Single activity hosting the game view. */
public final class MainActivity extends Activity implements Platform {
    private static final String PREFS = "ssp";
    private static final String KEY = "save";

    private GameView view;
    private AudioOut audio;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        view = new GameView(this, this);
        setContentView(view);
        hideSystemUi();
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUi() {
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    /** Called by the view once the game exists. */
    void startAudio() {
        if (audio == null && view.game() != null) {
            audio = new AudioOut(view.game().sound);
            audio.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        view.resume();
        if (audio != null) audio.setPaused(false);
    }

    @Override
    protected void onPause() {
        view.pause();
        if (audio != null) audio.setPaused(true);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (audio != null) audio.shutdown();
        audio = null;
        super.onDestroy();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (!view.back()) finish();
    }

    // ------------------------------------------------------------------ Platform

    @Override
    public String loadSave() {
        SharedPreferences p = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return p.getString(KEY, null);
    }

    @Override
    public void writeSave(String data) {
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, data).apply();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void vibrate(int ms) {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) v.vibrate(ms);
        } catch (RuntimeException ignored) {
            // vibration is optional
        }
    }

    @Override
    public void quit() {
        finish();
    }

    static boolean atLeast(int api) {
        return Build.VERSION.SDK_INT >= api;
    }
}
