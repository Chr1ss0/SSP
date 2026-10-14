package de.chr1ss0.ssp.core;

/** Character editor: name, hair, colours, headwear, extras. */
public final class EditorScreen extends Screen {
    private static final int B_NAME = 1, B_RANDOM = 2, B_DONE = 3, B_PREVIEW = 4;
    private static final int B_PREV = 100, B_NEXT = 200;
    private static final String[] LABELS = {"Frisur", "Haarfarbe", "Augen", "Hautton", "Oberteil", "Farbe", "Hose", "Kopf", "Extra"};
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
            case 2: return Look.EYE_COLORS.length;
            case 3: return Look.SKIN.length;
            case 4: return Look.TOP_NAMES.length;
            case 5: case 6: return Look.CLOTH.length;
            case 7: return Look.HAT_NAMES.length;
            default: return Look.EXTRA_NAMES.length;
        }
    }

    private int get(int row) {
        switch (row) {
            case 0: return look.hair;
            case 1: return look.hairColor;
            case 2: return look.eyes;
            case 3: return look.skin;
            case 4: return look.top;
            case 5: return look.shirt;
            case 6: return look.pants;
            case 7: return look.hat;
            default: return look.extra;
        }
    }

    private void set(int row, int v) {
        int n = count(row);
        v = ((v % n) + n) % n;
        switch (row) {
            case 0: look.hair = v; break;
            case 1: look.hairColor = v; break;
            case 2: look.eyes = v; break;
            case 3: look.skin = v; break;
            case 4: look.top = v; break;
            case 5: look.shirt = v; break;
            case 6: look.pants = v; break;
            case 7: look.hat = v; break;
            default: look.extra = v; break;
        }
    }

    private String valueName(int row) {
        int v = get(row);
        switch (row) {
            case 0: return Look.HAIR_NAMES[v];
            case 1: return Look.HAIR_COLOR_NAMES[v];
            case 2: return Look.EYE_NAMES[v];
            case 3: return Look.SKIN_NAMES[v];
            case 4: return Look.TOP_NAMES[v];
            case 5: case 6: return Look.CLOTH_NAMES[v];
            case 7: return Look.HAT_NAMES[v];
            default: return Look.EXTRA_NAMES[v];
        }
    }

    private int swatch(int row) {
        int v = get(row);
        switch (row) {
            case 1: return Look.HAIR_COLORS[v];
            case 2: return Look.EYE_COLORS[v];
            case 3: return Look.SKIN[v];
            case 5: case 6: return Look.CLOTH[v];
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
            look.pants = r.pants; look.hat = r.hat; look.extra = r.extra; look.eyes = r.eyes; look.top = r.top;
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
        Ui.background(g);
        Ui.header(g, firstTime ? "ERSTELLE DEINEN HELDEN" : "CHARAKTER", W());
        int extra = Math.max(0, H() - 380);
        int py = 24 + extra / 5;
        // preview stage
        g.shadow(6, py, W() - 12, 104, 8, 60);
        g.roundRectGrad(6, py, W() - 12, 104, 8, 0xFF8FC9FF, 0xFFD8F0FF);
        g.roundRectGrad(6, py + 70, W() - 12, 34, 8, 0xFFA8DE84, 0xFF79C45E);
        g.fillEllipse(52, py + 96, 32, 6, 0x40000000);
        g.fillEllipse(170, py + 98, 40, 6, 0x40000000);
        int wid = PREVIEW[previewIdx];
        g.sprite(CharArt.front(look, wid), 10, py + 4, 1);
        g.sprite(CharArt.back(look, wid), 124, py + 18, 1);
        ui.region(B_PREVIEW, 6, py, W() - 12, 104);
        g.textRight("Tippen: Waffe wechseln", W() - 10, py + 4, 0xFF33507A);

        int y = py + 110;
        g.text("NAME", 12, y + 5, Pal.INK_SOFT);
        g.text(name, 18 + Font.width("NAME", 1), y + 5, Pal.INK);
        ui.button(g, B_NAME, 150, y, 82, 18, "ÄNDERN", Ui.BTN_NORMAL);
        y += 21;
        int rowH = 19 + Math.min(4, extra / 50);
        for (int r = 0; r < LABELS.length; r++) {
            if ((r & 1) == 0) g.roundRect(6, y - 1, W() - 12, rowH, 4, 0x40FFFFFF);
            g.text(LABELS[r], 12, y + 5, Pal.INK);
            ui.button(g, B_PREV + r, 92, y, 22, 17, String.valueOf(Font.LEFT), Ui.BTN_NORMAL);
            ui.button(g, B_NEXT + r, 210, y, 22, 17, String.valueOf(Font.CURSOR), Ui.BTN_NORMAL);
            int sw = swatch(r);
            String v = valueName(r);
            if (sw != 0) {
                int tw = Font.width(v, 1) + 11;
                int sx = 162 - tw / 2;
                g.roundRect(sx, y + 4, 8, 8, 3, sw);
                g.roundBorder(sx, y + 4, 8, 8, 3, 1, 0x60000000);
                g.text(v, sx + 11, y + 5, Pal.INK);
            } else {
                g.textCenter(v, 162, y + 5, Pal.INK);
            }
            y += rowH;
        }
        int by = Math.max(y + 4, H() - 30);
        ui.button(g, B_RANDOM, 12, by, 100, 24, "ZUFALL", Ui.BTN_NORMAL);
        ui.button(g, B_DONE, 128, by, 100, 24, firstTime ? "LOS GEHT'S!" : "FERTIG", Ui.BTN_PRIMARY);
    }
}
