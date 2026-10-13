package de.chr1ss0.ssp.core;

/** Settings: volumes, text speed, vibration, reset. */
public final class OptionsScreen extends Screen {
    private static final int B_BACK = 1, B_RESET = 2, B_PREV = 100, B_NEXT = 200;
    private static final String[] VOL = {"Aus", "Leise", "Mittel", "Laut"};
    private static final String[] SPEED = {"Langsam", "Normal", "Schnell"};
    private static final String[] LABELS = {"Musik", "Soundeffekte", "Text-Tempo", "Vibration", "Neue Karten"};
    private static final String[] POPUP = {"Alle zeigen", "Ab Selten", "Nur Text"};

    public OptionsScreen(Game game) {
        super(game);
    }

    private String value(int row) {
        Profile p = game.profile;
        switch (row) {
            case 0: return VOL[p.musicVol];
            case 1: return VOL[p.sfxVol];
            case 2: return SPEED[p.textSpeed];
            case 3: return p.vibrate ? "An" : "Aus";
            default: return POPUP[p.cardPopup];
        }
    }

    private void change(int row, int d) {
        Profile p = game.profile;
        switch (row) {
            case 0: p.musicVol = (p.musicVol + d + 4) % 4; break;
            case 1: p.sfxVol = (p.sfxVol + d + 4) % 4; break;
            case 2: p.textSpeed = (p.textSpeed + d + 3) % 3; break;
            case 3: p.vibrate = !p.vibrate; if (p.vibrate) game.vibrate(60); break;
            default: p.cardPopup = (p.cardPopup + d + 3) % 3; break;
        }
        game.applyVolumes();
        game.save();
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        } else if (c >= B_NEXT) {
            change(c - B_NEXT, 1);
            game.sound.sfx(Sound.S_CURSOR);
        } else if (c >= B_PREV) {
            change(c - B_PREV, -1);
            game.sound.sfx(Sound.S_CURSOR);
        } else if (c == B_RESET) {
            game.sound.sfx(Sound.S_ERROR);
            game.dialog("Wirklich ALLES löschen? Charakter, Karten, Waffendex und Lauf gehen verloren!",
                    new String[]{"LÖSCHEN", "ABBRECHEN"}, new Game.Choice() {
                        @Override
                        public void chosen(int index) {
                            if (index != 0) return;
                            game.dialog("Bist du dir ganz sicher?", new String[]{"JA, LÖSCHEN", "NEIN"}, new Game.Choice() {
                                @Override
                                public void chosen(int i2) {
                                    if (i2 != 0) return;
                                    Profile fresh = new Profile();
                                    fresh.musicVol = game.profile.musicVol;
                                    fresh.sfxVol = game.profile.sfxVol;
                                    game.profile = fresh;
                                    game.save();
                                    game.set(new TitleScreen(game), Game.TR_FADE);
                                }
                            });
                        }
                    });
        }
    }

    @Override
    public void render(Gfx g) {
        g.clear(Pal.BG);
        Ui.header(g, "OPTIONEN", W());
        int y = 28;
        for (int r = 0; r < LABELS.length; r++) {
            Ui.panel(g, 6, y, W() - 12, 30, Pal.WHITE, Pal.INK);
            g.text(LABELS[r], 14, y + 11, Pal.INK);
            ui.button(g, B_PREV + r, 120, y + 4, 22, 22, String.valueOf(Font.LEFT), Ui.BTN_NORMAL);
            g.textCenter(value(r), 175, y + 11, Pal.INK);
            ui.button(g, B_NEXT + r, 208, y + 4, 22, 22, String.valueOf(Font.CURSOR), Ui.BTN_NORMAL);
            y += 34;
        }
        y += 6;
        Profile p = game.profile;
        g.text("STATISTIK", 10, y, Pal.INK);
        y += 12;
        String[] stats = {
                "Läufe: " + p.runs, "Rekord-Etappe: " + p.bestStage, "Siege: " + p.totalWins,
                "Rundensiege: " + p.roundsWon, "Bosse besiegt: " + p.bossesBeaten, "Champion-Titel: " + p.champion
        };
        for (int i = 0; i < stats.length; i++) {
            g.text(stats[i], 14 + (i % 2) * 112, y + (i / 2) * 11, Pal.INK_SOFT);
        }
        ui.button(g, B_RESET, 30, H() - 58, W() - 60, 24, "SPIELSTAND LÖSCHEN", Ui.BTN_DANGER);
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
