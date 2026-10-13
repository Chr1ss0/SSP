package de.chr1ss0.ssp.core;

import java.util.List;

/** Modal message box with up to 4 buttons. */
public final class DialogScreen extends Screen {
    private final String text;
    private final String[] options;
    private final Game.Choice choice;

    public DialogScreen(Game game, String text, String[] options, Game.Choice choice) {
        super(game);
        this.text = text;
        this.options = options;
        this.choice = choice;
    }

    @Override
    public boolean isOverlay() {
        return true;
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c >= 0 && c < options.length) {
            game.sound.sfx(Sound.S_SELECT);
            game.pop();
            if (choice != null) choice.chosen(c);
        }
    }

    @Override
    public boolean onBack() {
        game.sound.sfx(Sound.S_BACK);
        game.pop();
        return true;
    }

    @Override
    public void render(Gfx g) {
        g.tint(Pal.INK, 130);
        int w = 208;
        List<String> lines = Font.wrap(text, w - 20);
        int btnRows = options.length <= 2 ? 1 : options.length;
        int h = 18 + lines.size() * Font.LINE + 8 + btnRows * 26;
        int x = (W() - w) / 2, y = (H() - h) / 2;
        Ui.box(g, x, y, w, h);
        int ty = y + 10;
        for (String l : lines) {
            g.textCenter(l, W() / 2, ty, Pal.INK);
            ty += Font.LINE;
        }
        ty += 6;
        if (options.length <= 2) {
            int bw = (w - 24 - (options.length - 1) * 8) / options.length;
            for (int i = 0; i < options.length; i++) {
                ui.button(g, i, x + 12 + i * (bw + 8), ty, bw, 22, options[i], i == 0 ? Ui.BTN_PRIMARY : Ui.BTN_NORMAL);
            }
        } else {
            for (int i = 0; i < options.length; i++) {
                ui.button(g, i, x + 12, ty + i * 26, w - 24, 22, options[i], Ui.BTN_NORMAL);
            }
        }
    }
}
