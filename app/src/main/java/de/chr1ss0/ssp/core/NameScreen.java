package de.chr1ss0.ssp.core;

/** Game Boy style name entry grid. */
public final class NameScreen extends Screen {
    private static final String[] UPPER = {"ABCDEFGHI", "JKLMNOPQR", "STUVWXYZ-", "ÄÖÜ.!?'& ", "0123456789"};
    private static final String[] LOWER = {"abcdefghi", "jklmnopqr", "stuvwxyz-", "äöüß!?'& ", "0123456789"};
    private static final int B_CASE = 1000, B_DEL = 1001, B_OK = 1002;

    private final EditorScreen editor;
    private final StringBuilder name;
    private boolean lower;

    public NameScreen(Game game, EditorScreen editor) {
        super(game);
        this.editor = editor;
        this.name = new StringBuilder(editor.name);
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c < 0) return;
        if (c == B_CASE) {
            lower = !lower;
            game.sound.sfx(Sound.S_CURSOR);
        } else if (c == B_DEL) {
            if (name.length() > 0) name.setLength(name.length() - 1);
            game.sound.sfx(Sound.S_BACK);
        } else if (c == B_OK) {
            String n = name.toString().trim();
            if (n.length() == 0) {
                game.sound.sfx(Sound.S_ERROR);
                return;
            }
            editor.name = n;
            game.sound.sfx(Sound.S_SELECT);
            game.pop();
        } else {
            String[] rows = lower ? LOWER : UPPER;
            char ch = rows[c / 100].charAt(c % 100);
            if (name.length() < Profile.NAME_MAX) {
                name.append(ch);
                game.sound.sfx(Sound.S_CURSOR);
            } else game.sound.sfx(Sound.S_ERROR);
        }
    }

    @Override
    public void render(Gfx g) {
        Ui.background(g);
        Ui.header(g, "WIE HEISST DU?", W());
        int y = 30;
        Ui.box(g, 20, y, W() - 40, 36);
        int cx = W() / 2 - (Profile.NAME_MAX * 14) / 2;
        for (int i = 0; i < Profile.NAME_MAX; i++) {
            int x = cx + i * 14;
            if (i < name.length()) Font.draw(g, String.valueOf(name.charAt(i)), x + 1, y + 10, Pal.INK, 2);
            boolean cursor = i == name.length() && ((int) (game.timeMs / 400) & 1) == 0;
            g.fill(x, y + 27, 11, 2, cursor ? Pal.RED : Pal.INK);
        }
        y += 46;
        String[] rows = lower ? LOWER : UPPER;
        int cell = 24;
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int x0 = (W() - row.length() * cell) / 2;
            for (int i = 0; i < row.length(); i++) {
                char ch = row.charAt(i);
                int x = x0 + i * cell;
                int id = r * 100 + i;
                boolean p = ui.isPressed(id);
                Ui.panel(g, x + 1, y, cell - 2, 22, p ? Pal.SELECT : Pal.WHITE, Pal.GRAY);
                g.textCenter(ch == ' ' ? "_" : String.valueOf(ch), x + cell / 2, y + 8, Pal.INK);
                ui.region(id, x, y, cell, 23);
            }
            y += 25;
        }
        y += 8;
        int bw = (W() - 40) / 3;
        ui.button(g, B_CASE, 14, y, bw, 24, lower ? "ABC" : "abc", Ui.BTN_NORMAL);
        ui.button(g, B_DEL, 20 + bw, y, bw, 24, "LÖSCHEN", Ui.BTN_NORMAL);
        ui.button(g, B_OK, 26 + bw * 2, y, bw, 24, "OK", Ui.BTN_PRIMARY);
    }
}
