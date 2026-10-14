package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.List;

/** Scrollable game manual. */
public final class HelpScreen extends Screen {
    private static final int B_BACK = 1;
    private static final int SCROLL = 5;

    private static final String[][] SECTIONS = {
            {"ZIEL", "Besiege mit deinem Monster ein gegnerisches Monster nach dem anderen und erobere die zehn Arenen. Jede fünfte Etappe wartet ein Arenaleiter. Nach Etappe 50 geht es endlos weiter!"},
            {"DEIN MONSTER", "Im Editor gestaltest du dein Monster: Art (Knuddel, Katze, Drache, Hase, Frosch, Vogel, Bär, Fuchs), Farben, Muster, Augen, Kopfschmuck, Schwanz, Hut und Extras."},
            {"DEIN ARSENAL", "Jeder Lauf beginnt mit Stein, Schere und Papier. Sie sind immer dabei und unbegrenzt einsetzbar. Jede Kiste bringt eine neue Spezialwaffe, bis du 6 Waffen hast; danach kannst du tauschen. Dein Gegner hat immer genauso viele Waffen wie du."},
            {"SPEZIALWAFFEN", "Jede Spezialwaffe (1\u00D7) darf pro Gegner nur einmal eingesetzt werden. Das gilt auch für den Gegner: oben siehst du, welche Waffen er schon benutzt hat. Nur Spezialwaffen tragen Bonuskarten."},
            {"EINE RUNDE", "Wähle eine deiner Waffen. Dein Gegner wählt gleichzeitig zufällig eine seiner noch verfügbaren Waffen. Dann heißt es: Schnick, Schnack, Schnuck! Unter jeder deiner Waffen siehst du, welche Waffen des Gegners sie schlägt (grau = schon benutzt). Halte eine Waffe lange gedrückt, um alle Details und ihre Bonuskarten zu sehen."},
            {"TYPEN", "Jede Waffe hat einen von 9 Typen: Stein, Klinge, Papier, Feuer, Wasser, Blitz, Pflanze, Stich und Eis. Jeder Typ schlägt genau vier andere und verliert gegen vier. Gleicher Typ heißt Unentschieden. Die Typentabelle findest du im Waffendex."},
            {"SCHADEN", "Wer die Runde gewinnt, verursacht Schaden in Höhe seines Angriffs minus der Abwehr des Gegners. Dein Monster wird mit jeder Etappe stärker."},
            {"BONUSKARTEN", "Für jeden besiegten Gegner erhältst du 2 zufällige Bonuskarten. Rüste sie auf deine Spezialwaffen aus: aus einer Waffe mit STEINBRECHER wird z.B. eine Steinbrecher-Waffe, die auch Steine besiegt. Es gibt 100 Karten: Brecher, Immunitäten, Materialien wie Diamant, Gift, Vampir, Phönix und viele mehr."},
            {"KISTEN", "Nach jedem Sieg öffnest du eine Holz-, Eisen- oder Goldkiste mit einer Spezialwaffe. Neue Waffen werden im Waffendex freigeschaltet. Es gibt 50 Waffen vom Streichholz bis zum Meteoriten."},
            {"NIEDERLAGE", "Verlierst du einen Kampf, beginnt ein neuer Lauf bei Etappe 1 mit Stein, Schere und Papier. Deine Bonuskarten behältst du für immer und verteilst sie auf die neuen Waffen."},
            {"TIPPS", "Spare deine Spezialwaffen für passende Momente. Passive Karten (Herz, Moos, Hellseher ...) wirken, egal auf welcher Waffe sie liegen. Mit AUTO-VERTEILEN im Arsenal geht es schnell."},
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
        Ui.background(g);
        Ui.header(g, "ANLEITUNG", W());
        int top = 22, h = H() - top - 34;
        int off = ui.beginScroll(g, SCROLL, 0, top, W(), h, contentH);
        for (Object[] o : laid) {
            int y = top + 6 + (Integer) o[1] - off;
            if (y < top - 12 || y > top + h) continue;
            boolean head = (Boolean) o[2];
            if (head) {
                g.roundRect(10, y - 2, W() - 20, 12, 5, Pal.ACCENT);
                g.text((String) o[0], 15, y, Pal.WHITE);
            } else g.text((String) o[0], 14, y, Pal.INK);
        }
        ui.endScroll(g, SCROLL);
        ui.button(g, B_BACK, 60, H() - 28, W() - 120, 24, "ZURÜCK", Ui.BTN_NORMAL);
    }
}
