package de.chr1ss0.ssp.core;

/** Character editor: name, hair, colours, headwear, extras. */
public final class EditorScreen extends Screen {
    private static final int B_NAME = 1, B_RANDOM = 2, B_DONE = 3, B_PREVIEW = 4;
    private static final int B_PREV = 100, B_NEXT = 200;
    private static final String[] LABELS = {"Frisur", "Haarfarbe", "Hautton", "Oberteil", "Hose", "Kopf", "Extra"};
    private static final int[] PREVIEW = {WeaponDef.SCHERE_ID, WeaponDef.STEIN_ID, WeaponDef.PAPIER_ID};

    private final boolean firstTime;
    final Look look;
    String name;
    private int previewIdx;

    public EditorScreen(Game game, boolean firstTime) {
        super(game);
        this.firstTime = firstTime;
        this.look = game.profile.look.copy();
        this.name = game.profile.name;
    }

    @Override
    public int music() {
        return Sound.M_MAP;
    }

    private int count(int row) {
        switch (row) {
            case 0: return Look.HAIR_NAMES.length;
            case 1: return Look.HAIR_COLORS.length;
            case 2: return Look.SKIN.length;
            case 3: case 4: return Look.CLOTH.length;
            case 5: return Look.HAT_NAMES.length;
            default: return Look.EXTRA_NAMES.length;
        }
    }

    private int get(int row) {
        switch (row) {
            case 0: return look.hair;
            case 1: return look.hairColor;
            case 2: return look.skin;
            case 3: return look.shirt;
            case 4: return look.pants;
            case 5: return look.hat;
            default: return look.extra;
        }
    }

    private void set(int row, int v) {
        int n = count(row);
        v = ((v % n) + n) % n;
        switch (row) {
            case 0: look.hair = v; break;
            case 1: look.hairColor = v; break;
            case 2: look.skin = v; break;
            case 3: look.shirt = v; break;
            case 4: look.pants = v; break;
            case 5: look.hat = v; break;
            default: look.extra = v; break;
        }
    }

    private String valueName(int row) {
        int v = get(row);
        switch (row) {
            case 0: return Look.HAIR_NAMES[v];
            case 1: return Look.HAIR_COLOR_NAMES[v];
            case 2: return Look.SKIN_NAMES[v];
            case 3: case 4: return Look.CLOTH_NAMES[v];
            case 5: return Look.HAT_NAMES[v];
            default: return Look.EXTRA_NAMES[v];
        }
    }

    private int swatch(int row) {
        int v = get(row);
        switch (row) {
            case 1: return Look.HAIR_COLORS[v];
            case 2: return Look.SKIN[v];
            case 3: case 4: return Look.CLOTH[v];
            default: return 0;
        }
    }

    @Override
    public void update(float dt) {
        int c = ui.click();
        if (c < 0) return;
        if (c >= B_NEXT) {
            set(c - B_NEXT, get(c - B_NEXT) + 1);
            game.sound.sfx(Sound.S_CURSOR);
        } else if (c >= B_PREV) {
            set(c - B_PREV, get(c - B_PREV) - 1);
            game.sound.sfx(Sound.S_CURSOR);
        } else if (c == B_PREVIEW) {
            previewIdx = (previewIdx + 1) % PREVIEW.length;
            game.sound.sfx(Sound.S_CURSOR);
        } else if (c == B_NAME) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new NameScreen(game, this));
        } else if (c == B_RANDOM) {
            game.sound.sfx(Sound.S_SELECT);
            Look r = Look.random(game.rng);
            look.hair = r.hair; look.hairColor = r.hairColor; look.skin = r.skin; look.shirt = r.shirt;
            look.pants = r.pants; look.hat = r.hat; look.extra = r.extra;
        } else if (c == B_DONE) {
            game.sound.sfx(Sound.S_LEVELUP);
            Profile p = game.profile;
            p.look = look.copy();
            p.name = name;
            p.created = true;
            if (p.run != null) p.run.syncPlayer(p);
            game.save();
            if (firstTime) Flow.newRun(game);
            else game.pop();
        }
    }

    @Override
    public void render(Gfx g) {
        g.clear(Pal.BG);
        Ui.header(g, firstTime ? "ERSTELLE DEINEN HELDEN" : "CHARAKTER", W());
        int extra = Math.max(0, H() - 360);
        int py = 22 + extra / 4;
        // preview panel
        Ui.panel(g, 6, py, W() - 12, 102, 0xFFE8F0D8, Pal.INK);
        g.fillEllipse(52, py + 96, 30, 4, Pal.GROUND_DARK);
        g.fillEllipse(176, py + 96, 36, 4, Pal.GROUND_DARK);
        int wid = PREVIEW[previewIdx];
        g.sprite(CharArt.front(look, wid), 12, py + 4, 2);
        g.sprite(CharArt.back(look, wid), 128, py + 14, 2);
        ui.region(B_PREVIEW, 6, py, W() - 12, 102);
        g.text("Tippen: Waffe wechseln", 10, py + 104, Pal.INK_SOFT);

        int y = py + 118;
        g.text("NAME:", 12, y + 4, Pal.INK_SOFT);
        g.text(name, 50, y + 4, Pal.INK);
        ui.button(g, B_NAME, 150, y, 78, 18, "ÄNDERN", Ui.BTN_NORMAL);
        y += 22;
        int rowH = 21 + Math.min(4, extra / 40);
        for (int r = 0; r < LABELS.length; r++) {
            g.text(LABELS[r], 12, y + 5, Pal.INK);
            ui.button(g, B_PREV + r, 92, y, 22, 18, String.valueOf(Font.LEFT), Ui.BTN_NORMAL);
            ui.button(g, B_NEXT + r, 206, y, 22, 18, String.valueOf(Font.CURSOR), Ui.BTN_NORMAL);
            int sw = swatch(r);
            String v = valueName(r);
            if (sw != 0) {
                int tw = Font.width(v, 1) + 10;
                int sx = 160 - tw / 2;
                g.fill(sx, y + 5, 7, 7, Pal.INK);
                g.fill(sx + 1, y + 6, 5, 5, sw);
                g.text(v, sx + 10, y + 5, Pal.INK);
            } else {
                g.textCenter(v, 160, y + 5, Pal.INK);
            }
            y += rowH;
        }
        int by = Math.max(y + 6, H() - 30);
        ui.button(g, B_RANDOM, 12, by, 100, 24, "ZUFALL", Ui.BTN_NORMAL);
        ui.button(g, B_DONE, 128, by, 100, 24, firstTime ? "LOS GEHT'S!" : "FERTIG", Ui.BTN_PRIMARY);
    }
}
