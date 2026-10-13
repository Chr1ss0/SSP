package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.List;

/** Scrollable game manual. */
public final class HelpScreen extends Screen {
    private static final int B_BACK = 1;
    private static final int SCROLL = 5;

    private static final String[][] SECTIONS = {
            {"ZIEL", "Besiege einen Gegner nach dem anderen und erobere die zehn Arenen. Jede fünfte Etappe wartet ein Arena-Boss. Nach Etappe 50 geht es endlos weiter!"},
            {"EINE RUNDE", "Wähle eine deiner Waffen. Dein Gegner wählt gleichzeitig zufällig eine Waffe aus seinem Arsenal (oben links zu sehen). Danach heißt es: Schnick, Schnack, Schnuck!"},
            {"TYPEN", "Jede Waffe hat einen von 9 Typen: Stein, Klinge, Papier, Feuer, Wasser, Blitz, Pflanze, Stich und Eis. Jeder Typ schlägt genau vier andere und verliert gegen vier. Gleicher Typ heißt Unentschieden. Die Typentabelle findest du im Waffendex."},
            {"SCHADEN", "Wer die Runde gewinnt, verursacht Schaden in Höhe seines Angriffs minus der Abwehr des Gegners. Sinken die KP auf 0, ist der Kampf vorbei."},
            {"HINWEISE", Font.UP + " = so viele gegnerische Waffen schlägt deine Waffe, = Unentschieden, " + Font.MORE + " = so viele schlagen dich. Wähle klug!"},
            {"BONUSKARTEN", "Nach jeder gewonnenen Runde ziehst du eine zufällige Bonuskarte. Rüste sie auf deine Waffen aus, z.B. wird aus Schere mit STEINBRECHER eine Steinbrecher-Schere, die auch Steine besiegt. Es gibt 100 Karten: Brecher, Immunitäten, Materialien wie Diamant, Gift, Vampir, Phönix und viele mehr."},
            {"KISTEN", "Nach jedem Sieg erhältst du eine Kiste mit einer neuen Waffe: Holz-, Eisen- oder Goldkiste. Es gibt 50 Waffen vom Streichholz bis zum Meteoriten. Du kannst höchstens 6 Waffen tragen."},
            {"NIEDERLAGE", "Verlierst du einen Kampf, beginnt ein neuer Lauf mit Stein, Schere und Papier. Deine Bonuskarten behältst du aber für immer und verteilst sie einfach neu!"},
            {"TIPPS", "Tippe im Kampf auf die gegnerischen Waffen, um ihre Stärken zu sehen. Passive Karten (Herz, Moos, Hellseher ...) wirken, egal auf welcher Waffe sie liegen. Mit AUTO-VERTEILEN im Arsenal geht es schnell."},
    };

    private final List<Object[]> laid = new ArrayList<Object[]>();
    private int contentH;

    public HelpScreen(Game game) {
        super(game);
    }

    @Override
    public void enter() {
        laid.clear();
        int y = 0;
        for (String[] s : SECTIONS) {
            laid.add(new Object[]{s[0], y, Boolean.TRUE});
            y += 13;
            for (String l : Font.wrap(s[1], W() - 28)) {
                laid.add(new Object[]{l, y, Boolean.FALSE});
                y += Font.LINE;
            }
            y += 8;
        }
        contentH = y + 8;
    }

    @Override
    public void update(float dt) {
        if (ui.click() == B_BACK) {
            game.sound.sfx(Sound.S_BACK);
            game.pop();
        }
    }

    @Override
    public void render(Gfx g) {
        g.clear(Pal.BG);
        Ui.header(g, "ANLEITUNG", W());
        int top = 22, h = H() - top - 34;
        int off = ui.beginScroll(g, SCROLL, 0, top, W(), h, contentH);
        for (Object[] o : laid) {
            int y = top + 6 + (Integer) o[1] - off;
            if (y < top - 12 || y > top + h) continue;
            boolean head = (Boolean) o[2];
            if (head) {
                g.fill(10, y - 2, W() - 20, 11, 0xFFE8E0C8);
                g.text((String) o[0], 14, y, Pal.RED);
            } else g.text((String) o[0], 14, y, Pal.INK);
        }
        ui.endScroll(g, SCROLL);
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
