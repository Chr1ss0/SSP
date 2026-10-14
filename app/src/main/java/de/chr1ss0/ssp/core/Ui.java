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

    private static int[] bgCache;
    private static int bgW, bgH;

    /** Full-screen soft gradient background with a subtle diagonal pattern (cached). */
    public static void background(Gfx g) {
        if (bgCache == null || bgW != g.pw || bgH != g.ph) {
            bgW = g.pw;
            bgH = g.ph;
            bgCache = new int[bgW * bgH];
            for (int y = 0; y < bgH; y++) {
                int base = Pal.mix(Pal.BG, Pal.BG2, y / (float) Math.max(1, bgH - 1));
                int stripe = Pal.lighten(base, 0.25f);
                for (int x = 0; x < bgW; x++) {
                    boolean on = ((x + y) / 18) % 4 == 0;
                    bgCache[y * bgW + x] = on ? Pal.mix(base, stripe, 0.35f) : base;
                }
            }
        }
        System.arraycopy(bgCache, 0, g.px, 0, Math.min(bgCache.length, g.px.length));
    }

    /** Main content card (white, rounded, soft shadow). */
    public static void box(Gfx g, int x, int y, int w, int h) {
        g.shadow(x, y, w, h, 7, 60);
        g.roundRect(x, y, w, h, 7, Pal.WHITE);
        g.roundBorder(x, y, w, h, 7, 1, Pal.PANEL_BORDER);
    }

    /** Rounded panel; an INK border is drawn as a soft modern border. */
    public static void panel(Gfx g, int x, int y, int w, int h, int fill, int border) {
        int r = Math.min(6, Math.max(2, Math.min(w, h) / 4));
        if (h >= 24 && w >= 40 && (fill >>> 24) == 255) g.shadow(x, y, w, h, r, 34);
        int top = Pal.lighten(fill, 0.08f), bottom = fill;
        g.roundRectGrad(x, y, w, h, r, (fill >>> 24) == 255 ? top : fill, bottom);
        int b = border == Pal.INK ? 0xFFC3C9DE : border;
        if (b != fill) g.roundBorder(x, y, w, h, r, 1, b);
    }

    public static final int BTN_NORMAL = 0, BTN_PRIMARY = 1, BTN_DANGER = 2, BTN_GHOST = 3, BTN_DISABLED = 4;

    public boolean button(Gfx g, int id, int x, int y, int w, int h, String label, int style) {
        boolean p = isPressed(id) && style != BTN_DISABLED;
        int top, bottom, text, border;
        switch (style) {
            case BTN_PRIMARY: top = 0xFFFF7E73; bottom = 0xFFEC4058; text = Pal.WHITE; border = 0xFFD0344B; break;
            case BTN_DANGER: top = 0xFF737B9C; bottom = 0xFF525977; text = Pal.WHITE; border = 0xFF464C68; break;
            case BTN_GHOST: top = 0x00FFFFFF; bottom = 0x00FFFFFF; text = Pal.INK_SOFT; border = 0xFFC3C9DE; break;
            case BTN_DISABLED: top = 0xFFE9EBF2; bottom = 0xFFE1E4EE; text = 0xFFA8ADC2; border = 0xFFD8DCE8; break;
            default: top = 0xFFFFFFFF; bottom = 0xFFEEF0F8; text = Pal.INK; border = 0xFFC3C9DE; break;
        }
        int r = Math.min(7, h / 2);
        int oy = p ? 1 : 0;
        if (p) {
            top = Pal.mixA(top, 0xFF000000, 0.08f);
            bottom = Pal.mixA(bottom, 0xFF000000, 0.08f);
            if (style == BTN_NORMAL || style == BTN_GHOST) { top = 0xFFFFF4C8; bottom = 0xFFFFEAA8; }
        } else if (style != BTN_GHOST && style != BTN_DISABLED) {
            g.shadow(x, y, w, h - 1, r, 55);
        }
        g.roundRectGrad(x, y + oy, w, h - 1, r, top, bottom);
        g.roundBorder(x, y + oy, w, h - 1, r, 1, border);
        if (style == BTN_PRIMARY || style == BTN_DANGER) {
            g.roundRect(x + 2, y + oy + 1, w - 4, 3, 2, 0x30FFFFFF);
        }
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

    /** Rounded HP bar with "KP" label. hp may be fractional for animation. */
    public static void hpBar(Gfx g, int x, int y, int w, float hp, int max) {
        g.roundRect(x, y, 15, 8, 3, 0xFF2A2E45);
        g.textCenter("KP", x + 7, y, Pal.HP_YELLOW);
        int bx = x + 17;
        int bw = w - 17;
        g.roundRect(bx, y, bw, 8, 4, 0xFF2A2E45);
        int fw = max <= 0 ? 0 : Math.round((bw - 2) * Math.max(0, Math.min(1, hp / max)));
        if (fw > 0) {
            int c = hpColor(Math.round(hp), max);
            g.roundRectGrad(bx + 1, y + 1, Math.max(2, fw), 6, 3, Pal.lighten(c, 0.35f), Pal.shade(c, 0.85f));
        }
    }

    public static void typeBadge(Gfx g, int type, int x, int y, int w) {
        int c = Element.COLOR[type];
        g.roundRectGrad(x, y, w, 11, 5, Pal.lighten(c, 0.18f), Pal.shade(c, 0.9f));
        int tw = Font.width(Element.UPPER[type], 1);
        if (tw + 12 <= w - 6) {
            // icon and name centred as one group
            int gx = x + (w - (tw + 12)) / 2;
            g.spriteHD(Art.typeIcon(type), gx, y + 1);
            g.text(Element.UPPER[type], gx + 12, y + 2, Element.TEXT[type]);
        } else {
            g.textCenter(Element.UPPER[type], x + w / 2, y + 2, Element.TEXT[type]);
        }
    }

    public static void rarityBadge(Gfx g, int rarity, int x, int y) {
        String s = WeaponDef.RARITY_NAME[rarity].toUpperCase(java.util.Locale.GERMAN);
        int w = Font.width(s, 1) + 8;
        g.roundRectGrad(x, y, w, 11, 5, Pal.lighten(Pal.RARITY[rarity], 0.2f), Pal.RARITY[rarity]);
        g.text(s, x + 4, y + 2, Pal.WHITE);
    }

    /** Width a rarity badge will take. */
    public static int rarityBadgeWidth(int rarity) {
        return Font.width(WeaponDef.RARITY_NAME[rarity].toUpperCase(java.util.Locale.GERMAN), 1) + 8;
    }

    /** Weapon icon (32x32 HD art) in a type-coloured tile of 20x20 logical pixels. */
    public static void weaponTile(Gfx g, int weaponId, int x, int y, boolean known) {
        int type = WeaponDef.get(weaponId).type;
        int c = known ? Element.COLOR[type] : Pal.LIGHT;
        g.roundRectGrad(x, y, 20, 20, 4, Pal.lighten(c, 0.55f), Pal.lighten(c, 0.25f));
        g.roundBorder(x, y, 20, 20, 4, 1, known ? Pal.shade(c, 0.75f) : Pal.GRAY);
        if (known) g.spriteHD(WeaponArt.icon(weaponId), x + 2, y + 2);
        else g.spriteHD(WeaponArt.icon(weaponId), x + 2, y + 2, 1, Gfx.MODE_SOLID, 0xFF9FA4BC);
    }

    /** Weapon icon in a larger tile (size logical px, icon drawn at 1x or 2x HD). */
    public static void weaponTileBig(Gfx g, int weaponId, int x, int y, int size) {
        int type = WeaponDef.get(weaponId).type;
        int c = Element.COLOR[type];
        g.roundRectGrad(x, y, size, size, 6, Pal.lighten(c, 0.55f), Pal.lighten(c, 0.2f));
        g.roundBorder(x, y, size, size, 6, 1, Pal.shade(c, 0.75f));
        int sc = size >= 36 ? 2 : 1;
        int is = 16 * sc;
        g.spriteHD(WeaponArt.icon(weaponId), x + (size - is) / 2, y + (size - is) / 2, sc);
    }

    public static void cardTile(Gfx g, BonusDef b, int x, int y) {
        int c = Pal.RARITY[b.rarity];
        g.roundRect(x, y, 20, 20, 4, Pal.RARITY_LIGHT[b.rarity]);
        g.roundBorder(x, y, 20, 20, 4, 2, c);
        g.spriteHD(Art.bonusIcon(b), x + 2, y + 2);
    }

    /** Small card icon (12x12 logical) used under weapon names. */
    public static void cardMini(Gfx g, BonusDef b, int x, int y) {
        g.roundRect(x, y, 12, 12, 3, Pal.RARITY_LIGHT[b.rarity]);
        g.roundBorder(x, y, 12, 12, 3, 1, Pal.RARITY[b.rarity]);
        g.spriteHD(Art.bonusIconSmall(b), x + 1, y + 1);
    }

    public static void emptySlot(Gfx g, int x, int y, int size) {
        g.roundRect(x, y, size, size, 4, 0xFFF4F5FA);
        g.roundBorder(x, y, size, size, 4, 1, 0xFFC3C9DE);
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
        g.gradientV(0, 0, w, 19, 0xFF6F61FF, Pal.ACCENT_DARK);
        g.fill(0, 19, w, 1, 0x40000000);
        g.textCenter(title, w / 2, 6, Pal.WHITE);
    }
}
