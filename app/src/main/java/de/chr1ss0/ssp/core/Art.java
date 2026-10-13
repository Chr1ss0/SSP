package de.chr1ss0.ssp.core;

import java.util.HashMap;

/**
 * Misc. pixel art: bonus-card icons (recoloured per card), type symbols,
 * loot boxes. 'Q' = card colour, 'Z' = lighter, 'q' = darker.
 */
public final class Art {

    private static final String[][] BONUS = {
            // IC_BREAK
            {"QQQQQQQQQQQQ", "QZZZZZkZZZZq", "QZQQQkQQQQQq", "QZQQQQkQQQQq", "QZQQQkQQQQQq", "QZQQkQQQQQQq",
                    "QZQQQkkQQQQq", "QZQQQQQkQQQq", "QZQQQQkQQQQq", "QZQQQQQkQQQq", "QZQQQQkQQQQq", "qqqqqqkqqqqq"},
            // IC_IMMUNE
            {"..llllllllll..", ".lQQQQQQQQQQl.", ".lQZZZQQQQQql.", ".lQZQQQQQQQql.", ".lQZQQQQQQQql.",
                    ".lQQQQQQQQQql.", "..lQQQQQQQQl..", "..lQQQQQQQql..", "...lQQQQQql...", "....lQQQql....",
                    ".....lQql.....", "......ll......"},
            // IC_GEM
            {"...ZZZZZZZZ...", "..ZwZQQQQZqq..", ".ZZZZQQQQqqqq.", "QQQQQQQQQqqqqq", ".QQQQQQQqqqqq.",
                    "..QQQQQQqqqq..", "...QQQQQqqq...", "....QQQQqq....", ".....QQqq.....", "......Qq......"},
            // IC_ARMOR
            {"..QQ......QQ..", ".QZZQ....QZqQ.", ".QZQQQQQQQQqQ.", ".QZQQQQQQQQqQ.", "..QZQQQQQQqQ..",
                    "..QZQQQQQQqQ..", "..QZQQkkQQqQ..", "..QZQQQQQQqQ..", "..QQQQQQQQqQ..", "...QQQQQQqQ...",
                    "....qqqqqq...."},
            // IC_CRIT
            {"....QQQQQ.....", "..QQqqqqqQQ...", ".QQ.......QQ..", ".Q....r....Q..", "QQ...rrr...QQ.",
                    "Q...rrwrr...Q.", "QQ...rrr...QQ.", ".Q....r....Q..", ".QQ.......QQ..", "..QQqqqqqQQ...",
                    "....QQQQQ....."},
            // IC_FANG
            {"QQQQQQQQQQQQ", "QZZZZZZZZZZq", "qqqqqqqqqqqq", ".ww......ww.", ".ww......ww.", "..w......w..",
                    "..r......r..", "..........r.", "..r........."},
            // IC_THORN
            {"......Q.......", "..Q...Q...Q...", "...Q.QQQ.Q....", "....QQZQQ.....", ".QQQQZQQQQQQ..",
                    "....QQQQq.....", "...Q.QqQ.Q....", "..Q...Q...Q...", "......Q......."},
            // IC_POISON
            {".....bb.......", ".....bb.......", "....wllw......", ".....ll.......", "....lQQl......",
                    "...lQZQQl.....", "..lQZQQQQl....", "..lQQQQQql....", "..lQQQQqql....", "...lqqqql....."},
            // IC_DOUBLE
            {"w..........w", ".w........w.", "..w......w..", "...w....w...", "....w..w....", ".....ww.....",
                    ".....ww.....", "....Q..Q....", "...Q....Q...", "..QQ....QQ..", ".Q........Q."},
            // IC_CLOVER
            {"..QQQ..QQQ..", ".QZZQQQZZQq.", ".QZQQQQQQQq.", "..QQQqqQQQ..", "..QQQqqQQQ..", ".QZQQQQQQQq.",
                    ".QQQq..QQqq.", "..QQ...QQq..", "......q.....", ".....q......", "....q......."},
            // IC_MIRROR
            {"....QQQQ....", "...QccccQ...", "..QcwccccQ..", "..QcwcccCQ..", "..QccccCCQ..", "..QcccCCCQ..",
                    "...QccCCQ...", "....QQQQ....", ".....QQ.....", "....QQQQ...."},
            // IC_DRAW
            {"QQQQQQQQQQ", "QZZZZZZZZq", "qqqqqqqqqq", "..........", "QQQQQQQQQQ", "QZZZZZZZZq", "qqqqqqqqqq"},
            // IC_HEAL
            {"....QQQQ....", "....QZZq....", "....QZQq....", "QQQQQZQqQQQQ", "QZZZZZQQQQQq", "QZQQQQQQQQQq",
                    "QqqqqQQqqqqq", "....QQQq....", "....QQQq....", "....qqqq...."},
            // IC_RAGE
            {".QQ....QQ.", "QZQ....QqQ", "QQ......QQ", "..........", "..........", "QQ......QQ",
                    "QZQ....QqQ", ".QQ....QQ."},
            // IC_COMBO
            {".QQQQ.......", "QZ..qQ......", "Q....QQQ....", "Q...QQ..Q...", ".QQQQ...Q...", "....Q...QQQQ",
                    ".....QQQ...Q", "........Q..Q", ".........QQ."},
            // IC_BOOT
            {"...QQQ.......", "...QZQ..w....", "...QQQ.ww....", "...QQQwww....", "...QQQQ......",
                    "...QQQQQQQ...", "..QQQQQQQQQ..", "..qqqqqqqqqq."},
            // IC_SCYTHE (skull)
            {"...wwwwww...", "..wwwwwwwww.", ".wwwwwwwwwwW", ".wwkkwwkkwwW", ".wwkkwwkkwwW", ".wwwwwkwwwwW",
                    "..wwwwwwwwW.", "...wkwkwkW..", "...wwwwwwW.."},
            // IC_DRILL
            {"QQQQQQ........", "QZZZZZQQQ.....", "QQQQQQQQQQQ...", "QZZZZZZZZZZQQw", "QQQQQQQQQQQ...",
                    "QZZZZZQQQ.....", "QQQQQQ........"},
            // IC_HEART
            {".QQQ...QQQ.", "QZZQQ.QQQQq", "QZQQQQQQQQq", "QQQQQQQQQqq", ".QQQQQQQqq.", "..QQQQQqq..",
                    "...QQQqq...", "....Qqq....", ".....q....."},
            // IC_LEAF
            {"........QQQ.", "......QQZQq.", "....QQZZQqq.", "...QZZQQqqq.", "..QZQQQqqq..", "..QQQqqqq...",
                    ".QQqqqq.....", ".q..........", "q..........."},
            // IC_STAR
            {"......Q......", ".....QZQ.....", ".....QZQ.....", "QQQQQZZQQQQQQ", ".QZZZZZQQQqq.", "..QZZQQQQqq..",
                    "...QQQQQqq...", "..QQQqqQQqq..", "..QQq...QQq..", ".Qq.......Qq."},
            // IC_EYE
            {"....kkkkk.....", "..kwwwwwwwk...", ".kwwwQQQwwwk..", "kwwwQQkQQwwwk.", ".kwwwQQQwwwk..",
                    "..kwwwwwwwk...", "....kkkkk....."},
            // IC_PHOENIX
            {"y.........y", "oy.......yo", "ooy..y..yoo", ".ooyyyyyoo.", "..ooyrryoo.", "...orrrro..",
                    "....rrrr...", ".....rr....", "....r..r..."},
            // IC_CAT
            {".Q.......Q.", ".QQ.....QQ.", ".QZQQQQQqQ.", "QQQQQQQQQQQ", "QQyQQQQQyQQ", "QQkQQQQQkQQ",
                    "QQQQQpQQQQQ", ".QQQQQQQQQ.", "..QQQQQQQ.."},
            // IC_FOCUS
            {"...QQQQQQ...", ".QQQQQQQQQQ.", ".QQwwwwwwQQ.", "QQwwQQQQwwQq", "QQwQQZZQQwQq", "QQwQQZZQQwQq",
                    "QQwwQQQQwwQq", ".QQwwwwwwQq.", ".QQQQQQQQqq.", "...qqqqqq..."},
            // IC_ROCKET
            {".....w.....", "....wQw....", "...wQQQq...", "...QQcQq...", "...QcccQ...", "...QQcQq...",
                    "...QQQQq...", "..QQQQQQq..", ".QQ.QQQ.qq.", ".Q..yoy..q.", ".....y....."},
            // IC_SHIELD
            {"....qqqqqq....", "..qqQQQQQQqq..", ".qQQZZZZQQQQq.", ".qQZQQQQQQQQq.", "qQZQQQyyQQQQQq",
                    "qQZQQyzyyQQQQq", "qQQQQyyyYQQQQq", "qQQQQQyYQQQQQq", ".qQQQQQQQQQQq.", ".qQQQQQQQQQQq.",
                    "..qqQQQQQQqq..", "....qqqqqq...."},
    };

    private static final HashMap<Long, Sprite> CACHE = new HashMap<Long, Sprite>();

    private static Sprite parseColored(String[] rows, int w, int h, int color) {
        int light = Pal.lighten(color, 0.45f), dark = Pal.shade(color, 0.7f);
        Sprite s = new Sprite(w, h);
        int ah = rows.length, aw = 0;
        for (String r : rows) aw = Math.max(aw, r.length());
        int ox = (w - aw) / 2, oy = (h - ah) / 2;
        for (int y = 0; y < ah; y++) {
            String r = rows[y];
            for (int x = 0; x < r.length(); x++) {
                char ch = r.charAt(x);
                int c;
                if (ch == 'Q') c = color;
                else if (ch == 'Z') c = light;
                else if (ch == 'q') c = dark;
                else c = Pal.color(ch);
                int xx = x + ox, yy = y + oy;
                if (xx >= 0 && yy >= 0 && xx < w && yy < h) s.px[yy * w + xx] = c;
            }
        }
        s.addOutline(Pal.OUTLINE);
        return s;
    }

    public static Sprite bonusIcon(BonusDef b) {
        long key = ((long) b.icon << 32) | (b.color & 0xFFFFFFFFL);
        Sprite s = CACHE.get(key);
        if (s == null) {
            s = parseColored(BONUS[b.icon], 16, 16, b.color);
            CACHE.put(key, s);
        }
        return s;
    }

    // ------------------------------------------------------------ type symbols (8x8)

    private static final String[][] TYPE = {
            {".gggg.", "gllggG", "glgggG", "ggggGG", ".GGGG."},
            {"....w", "...wl", "..wl.", "ywl..", ".Y...", "Y.Y.."},
            {"wwwl.", "wwwwl", "wggww", "wwwww", "wgggw", "wwwww"},
            {"..y..", ".yo.y", ".oyoo", "orzyo", "oryyr", ".rrr."},
            {"..c..", "..c..", ".ccc.", "cwccC", "cccCC", ".CCC."},
            {"...yy", "..yy.", ".yyyy", "..yy.", ".yy..", ".y..."},
            {"...ee", "..efe", ".efeE", "eeEE.", "E...."},
            {"....w", "...l.", "..l..", ".n...", "n...."},
            {"..c..", "c.c.c", ".ccc.", "ccwcc", ".ccc.", "c.c.c", "..c.."},
    };
    private static final Sprite[] TYPE_CACHE = new Sprite[Element.COUNT];

    public static Sprite typeIcon(int type) {
        Sprite s = TYPE_CACHE[type];
        if (s == null) {
            s = Sprite.parseFixed(TYPE[type], 9, 9);
            TYPE_CACHE[type] = s;
        }
        return s;
    }

    // ------------------------------------------------------------ loot boxes

    public static final int BOX_WOOD = 0, BOX_IRON = 1, BOX_GOLD = 2;
    public static final String[] BOX_NAME = {"Holzkiste", "Eisenkiste", "Goldkiste"};
    public static final int[] BOX_COLOR = {0xFFB07038, 0xFF8890A8, 0xFFF0B828};

    private static final String[] BOX_BODY = {
            "QQqQQQQQQQQQQQQQqQQ",
            "QZgZZZZZyyyZZZZZgZq",
            "QQgQQQQyKKKyQQQQgQq",
            "QQgQQQQyyKyyQQQQgQq",
            "QQgQQQQQyyyQQQQQgQq",
            "QQgQQQQQQQQQQQQQgQq",
            "QQgQQQQQQQQQQQQQgQq",
            "QQgQQQQQQQQQQQQQgQq",
            "qqgqqqqqqqqqqqqqgqq",
    };
    private static final String[] BOX_LID = {
            "..QQQQQQQQQQQQQQQ..",
            ".QZZZZZZZZZZZZZZZQ.",
            "QZZgZZZZZZZZZZZgZZq",
            "QQQgQQQQQyQQQQQgQQq",
            "qqqgqqqqyyyqqqqgqqq",
    };
    private static final Sprite[][] BOX_CACHE = new Sprite[3][2];

    /** part 0 = body, 1 = lid. */
    public static Sprite box(int tier, int part) {
        Sprite s = BOX_CACHE[tier][part];
        if (s == null) {
            String[] a = part == 0 ? BOX_BODY : BOX_LID;
            int aw = a[0].length() + 2, ah = a.length + 2;
            s = parseColored(a, aw, ah, BOX_COLOR[tier]);
            BOX_CACHE[tier][part] = s;
        }
        return s;
    }

    private Art() {}
}
