package de.chr1ss0.ssp.core;

/** Services the host platform (Android / desktop) provides to the game. */
public interface Platform {
    String loadSave();

    void writeSave(String data);

    void vibrate(int ms);

    /** Leave the app (Android: finish activity). */
    void quit();
}
