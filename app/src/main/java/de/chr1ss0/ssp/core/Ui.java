package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Immediate-mode UI helper: widgets register hit regions while drawing; touch
 * events are matched against the regions of the previous frame. Also provides
 * the Game-Boy style drawing primitives (boxes, buttons, HP bars ...).
 */
public final class Ui {
    private static final int DRAG_THRESHOLD = 5;

    private static final class Region {
        int id, x, y, w, h;
        int scrollId; // -1 = none
    }

    private static final class Scroll {
        int x, y, w, h, content;
        float offset, velocity;
        boolean enabled;
    }

    private ArrayList<Region> regions = new ArrayList<Region>();
    private ArrayList<Region> building = new ArrayList<Region>();
    private final HashMap<Integer, Scroll> scrolls = new HashMap<Integer, Scroll>();
    private final ArrayList<Integer> frameScrolls = new ArrayList<Integer>();
    private int scrollContext = -1;
    private boolean active = true;

    private int pressed = -1;
    private int clicked = -1;
    private boolean down, dragging;
    private int downX, downY, lastY;
    private int activeScroll = -1;
    private long lastMoveTime;
    public int touchX, touchY;

    // ---------------------------------------------------------------- frame / input

    public void beginFrame() {
        building.clear();
        frameScrolls.clear();
        scrollContext = -1;
    }

    public void endFrame() {
        ArrayList<Region> t = regions;
        regions = building;
        building = t;
        // drop scroll state of areas not shown anymore
        if (!down) {
            java.util.Iterator<Integer> it = scrolls.keySet().iterator();
            while (it.hasNext()) if (!frameScrolls.contains(it.next())) it.remove();
        }
    }

    public void reset() {
        pressed = -1;
        clicked = -1;
        down = false;
        dragging = false;
        activeScroll = -1;
        scrolls.clear();
    }

    private Region hit(int x, int y) {
        for (int i = regions.size() - 1; i >= 0; i--) {
            Region r = regions.get(i);
            if (x >= r.x && y >= r.y && x < r.x + r.w && y < r.y + r.h) return r;
        }
        return null;
    }

    public void onDown(int x, int y) {
        down = true;
        dragging = false;
        downX = x; downY = y; lastY = y;
        touchX = x; touchY = y;
        Region r = hit(x, y);
        pressed = r != null ? r.id : -1;
        activeScroll = -1;
        if (r != null && r.scrollId >= 0) activeScroll = r.scrollId;
        if (activeScroll < 0) {
            for (Integer sid : scrolls.keySet()) {
                Scroll s = scrolls.get(sid);
                if (s.enabled && x >= s.x && y >= s.y && x < s.x + s.w && y < s.y + s.h) activeScroll = sid;
            }
        }
        if (activeScroll >= 0) scrolls.get(activeScroll).velocity = 0;
    }

    public void onMove(int x, int y) {
        touchX = x; touchY = y;
        if (!down) return;
        if (!dragging && activeScroll >= 0 && Math.abs(y - downY) > DRAG_THRESHOLD) {
            dragging = true;
            pressed = -1;
        }
        if (dragging && activeScroll >= 0) {
            Scroll s = scrolls.get(activeScroll);
            if (s != null) {
                int dy = y - lastY;
                s.offset -= dy;
                s.velocity = -dy;
            }
        }
        lastY = y;
        lastMoveTime = System.currentTimeMillis();
    }

    public void onUp(int x, int y) {
        if (!down) return;
        down = false;
        if (!dragging && pressed >= 0) {
            Region r = hit(x, y);
            if (r != null && r.id == pressed) clicked = pressed;
        }
        if (dragging && activeScroll >= 0 && System.currentTimeMillis() - lastMoveTime > 80) {
            Scroll s = scrolls.get(activeScroll);
            if (s != null) s.velocity = 0;
        }
        pressed = -1;
        dragging = false;
    }

    /** Returns and consumes the id clicked since last call (-1 if none). */
    public int click() {
        int c = clicked;
        clicked = -1;
        return c;
    }

    public boolean isPressed(int id) {
        return active && down && pressed == id;
    }

    public boolean isDown() {
        return down;
    }

    public void update(float dt) {
        for (Scroll s : scrolls.values()) {
            if (!down && Math.abs(s.velocity) > 0.1f) {
                s.offset += s.velocity;
                s.velocity *= 0.9f;
            }
            int max = Math.max(0, s.content - s.h);
            if (s.offset < 0) { s.offset = 0; s.velocity = 0; }
            if (s.offset > max) { s.offset = max; s.velocity = 0; }
        }
    }

    // ---------------------------------------------------------------- regions

    /** Rect {x, y, w, h} of a region from the last frame, or null (used by tests). */
    public int[] find(int id) {
        for (int i = regions.size() - 1; i >= 0; i--) {
            Region r = regions.get(i);
            if (r.id == id) return new int[]{r.x, r.y, r.w, r.h};
        }
        return null;
    }

    /** All region ids of the last frame (used by tests). */
    public int[] ids() {
        int[] out = new int[regions.size()];
        for (int i = 0; i < out.length; i++) out[i] = regions.get(i).id;
        return out;
    }

    public void setActive(boolean a) {
        active = a;
    }

    public void region(int id, int x, int y, int w, int h) {
        if (!active) return;
        Region r = new Region();
        r.id = id; r.x = x; r.y = y; r.w = w; r.h = h;
        r.scrollId = scrollContext;
        building.add(r);
    }

    /** Begins a scroll area; returns the current scroll offset. */
    public int beginScroll(Gfx g, int id, int x, int y, int w, int h, int contentHeight) {
        Scroll s = scrolls.get(id);
        if (s == null) {
            s = new Scroll();
            scrolls.put(id, s);
        }
        s.x = x; s.y = y; s.w = w; s.h = h; s.content = contentHeight;
        s.enabled = active;
        int max = Math.max(0, contentHeight - h);
        if (s.offset > max) s.offset = max;
        if (s.offset < 0) s.offset = 0;
        frameScrolls.add(id);
        scrollContext = id;
        g.clip(x, y, w, h);
        return (int) s.offset;
    }

    public void endScroll(Gfx g, int id) {
        g.noClip();
        scrollContext = -1;
        Scroll s = scrolls.get(id);
        if (s != null && s.content > s.h) {
            int trackH = s.h - 4;
            int barH = Math.max(12, trackH * s.h / s.content);
            int barY = s.y + 2 + (int) ((trackH - barH) * (s.offset / Math.max(1, s.content - s.h)));
            g.fill(s.x + s.w - 3, s.y + 2, 2, trackH, Pal.LIGHT);
            g.fill(s.x + s.w - 3, barY, 2, barH, Pal.INK_SOFT);
        }
    }

    public void scrollTo(int id, int offset) {
        Scroll s = scrolls.get(id);
        if (s == null) {
            s = new Scroll();
            scrolls.put(id, s);
        }
        s.offset = offset;
    }

    /** Regions registered inside a scroll area must also be clipped to it. */
    public void regionClipped(int id, int x, int y, int w, int h, int cx, int cy, int cw, int ch) {
        int x0 = Math.max(x, cx), y0 = Math.max(y, cy);
        int x1 = Math.min(x + w, cx + cw), y1 = Math.min(y + h, cy + ch);
        if (x1 > x0 && y1 > y0) region(id, x0, y0, x1 - x0, y1 - y0);
    }

    // ---------------------------------------------------------------- drawing primitives

    /** Game Boy style double-bordered box. */
    public static void box(Gfx g, int x, int y, int w, int h) {
        g.fill(x + 1, y + 1, w - 2, h - 2, Pal.WHITE);
        g.rect(x + 1, y, w - 2, h, Pal.INK);
        g.rect(x, y + 1, w, h - 2, Pal.INK);
        g.rect(x + 3, y + 3, w - 6, h - 6, Pal.INK);
        g.pset(x + 3, y + 3, Pal.WHITE);
        g.pset(x + w - 4, y + 3, Pal.WHITE);
        g.pset(x + 3, y + h - 4, Pal.WHITE);
        g.pset(x + w - 4, y + h - 4, Pal.WHITE);
    }

    /** Simple rounded panel with single border. */
    public static void panel(Gfx g, int x, int y, int w, int h, int fill, int border) {
        g.fill(x + 1, y + 1, w - 2, h - 2, fill);
        g.hline(x + 1, y, w - 2, border);
        g.hline(x + 1, y + h - 1, w - 2, border);
        g.vline(x, y + 1, h - 2, border);
        g.vline(x + w - 1, y + 1, h - 2, border);
    }

    public static final int BTN_NORMAL = 0, BTN_PRIMARY = 1, BTN_DANGER = 2, BTN_GHOST = 3, BTN_DISABLED = 4;

    public boolean button(Gfx g, int id, int x, int y, int w, int h, String label, int style) {
        boolean p = isPressed(id) && style != BTN_DISABLED;
        int fill, text, shadow;
        switch (style) {
            case BTN_PRIMARY: fill = 0xFFE84848; text = Pal.WHITE; shadow = 0xFF982020; break;
            case BTN_DANGER: fill = 0xFF606878; text = Pal.WHITE; shadow = 0xFF303440; break;
            case BTN_GHOST: fill = Pal.BG; text = Pal.INK; shadow = Pal.LIGHT; break;
            case BTN_DISABLED: fill = Pal.LIGHT; text = Pal.GRAY; shadow = 0xFFB8B8C0; break;
            default: fill = Pal.WHITE; text = Pal.INK; shadow = 0xFFB0B0BC; break;
        }
        if (p) fill = style == BTN_NORMAL || style == BTN_GHOST ? Pal.SELECT : Pal.lighten(fill, 0.2f);
        int oy = p ? 1 : 0;
        if (!p) panel(g, x, y + 1, w, h, shadow, shadow);
        panel(g, x, y + oy, w, h - 1, fill, Pal.INK);
        int ty = y + oy + (h - 1 - Font.CAP) / 2;
        g.textCenter(label, x + w / 2, ty, text);
        if (style != BTN_DISABLED) region(id, x, y, w, h);
        return p;
    }

    public static int hpColor(int hp, int max) {
        if (hp * 2 > max) return Pal.HP_GREEN;
        if (hp * 5 > max) return Pal.HP_YELLOW;
        return Pal.HP_RED;
    }

    /** "KP" bar in Gen-1 style. hp may be fractional for animation. */
    public static void hpBar(Gfx g, int x, int y, int w, float hp, int max) {
        g.fill(x, y, 15, 7, Pal.INK);
        Font.draw(g, "KP", x + 2, y, Pal.HP_YELLOW, 1);
        int bx = x + 15;
        int bw = w - 15;
        g.fill(bx, y, bw, 7, Pal.INK);
        g.fill(bx + 1, y + 2, bw - 2, 3, Pal.WHITE);
        int fw = max <= 0 ? 0 : Math.round((bw - 2) * Math.max(0, Math.min(1, hp / max)));
        if (fw > 0) {
            int c = hpColor(Math.round(hp), max);
            g.fill(bx + 1, y + 2, fw, 3, c);
            g.fill(bx + 1, y + 2, fw, 1, Pal.lighten(c, 0.4f));
        }
    }

    public static void typeBadge(Gfx g, int type, int x, int y, int w) {
        int c = Element.COLOR[type];
        panel(g, x, y, w, 11, c, Pal.shade(c, 0.6f));
        g.textCenter(Element.UPPER[type], x + w / 2, y + 2, Element.TEXT[type]);
    }

    public static void rarityBadge(Gfx g, int rarity, int x, int y) {
        String s = WeaponDef.RARITY_NAME[rarity].toUpperCase(java.util.Locale.GERMAN);
        int w = Font.width(s, 1) + 6;
        panel(g, x, y, w, 11, Pal.RARITY[rarity], Pal.shade(Pal.RARITY[rarity], 0.6f));
        g.text(s, x + 3, y + 2, Pal.WHITE);
    }

    /** Weapon icon in a type-coloured frame (size 20x20). */
    public static void weaponTile(Gfx g, int weaponId, int x, int y, boolean known) {
        int type = WeaponDef.get(weaponId).type;
        int c = known ? Element.COLOR[type] : Pal.LIGHT;
        panel(g, x, y, 20, 20, Pal.lighten(c, 0.35f), Pal.shade(c, 0.6f));
        if (known) g.sprite(WeaponArt.icon(weaponId), x + 2, y + 2);
        else {
            g.sprite(WeaponArt.icon(weaponId), x + 2, y + 2, 1, false, Gfx.MODE_SOLID, Pal.GRAY);
        }
    }

    public static void cardTile(Gfx g, BonusDef b, int x, int y) {
        int c = Pal.RARITY[b.rarity];
        panel(g, x, y, 20, 20, Pal.RARITY_LIGHT[b.rarity], c);
        g.sprite(Art.bonusIcon(b), x + 2, y + 2);
    }

    public static void emptySlot(Gfx g, int x, int y, int size) {
        panel(g, x, y, size, size, Pal.BG, Pal.GRAY);
        g.textCenter("+", x + size / 2, y + (size - Font.CAP) / 2, Pal.GRAY);
    }

    /** Draws wrapped text; returns height used. */
    public static int paragraph(Gfx g, String text, int x, int y, int w, int color) {
        List<String> lines = Font.wrap(text, w);
        int yy = y;
        for (String l : lines) {
            g.text(l, x, yy, color);
            yy += Font.LINE;
        }
        return yy - y;
    }

    /** Header bar used on menu screens. */
    public static void header(Gfx g, String title, int w) {
        g.fill(0, 0, w, 18, Pal.INK);
        g.fill(0, 18, w, 1, Pal.DARK);
        g.textCenter(title, w / 2, 6, Pal.WHITE);
    }
}
