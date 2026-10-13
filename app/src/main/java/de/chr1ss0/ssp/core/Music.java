package de.chr1ss0.ssp.core;

/**
 * Original 8-bit compositions in MML.
 * Channel 0 = lead pulse, 1 = harmony pulse, 2 = triangle bass, 3 = noise drums
 * (drum notes: c kick, d snare, e closed hat, f open hat, g crash).
 */
public final class Music {
    private static final Mml.Track[] CACHE = new Mml.Track[6];

    public static synchronized Mml.Track track(int id) {
        if (CACHE[id] == null) CACHE[id] = build(id);
        return CACHE[id];
    }

    private static Mml.Track build(int id) {
        switch (id) {
            case Sound.M_TITLE: return title();
            case Sound.M_MAP: return map();
            case Sound.M_BATTLE: return battle();
            case Sound.M_BOSS: return boss();
            case Sound.M_VICTORY: return victory();
            default: return gameOver();
        }
    }

    // --------------------------------------------------------------- Titel: C-Dur, heroisch
    private static Mml.Track title() {
        String lead = "@1 v13 k5 s5 q7 o5 l8"
                + " c8 c8 c8 g4. e8 g8 | >c2 r4 <g4 L"
                + " c4 e8 g8 >c4< g4 | b4 a8 g8 d4 g4 | a4 >c8 e8 d4 c4< | a4 g8 f8 g2"
                + " | c4 e8 g8 >c4 e4< | >d4 c8 <b8 >d4< g4 | a4 >c4< b4 >d4< | >c2< r4 g4"
                + " | e4 a8 b8 >c4 <a4 | g4 e8 g8 b4 g4 | a4 >c8 d8 c4 <a4 | g2 e4 c4"
                + " | d4 f8 a8 >d4 c4< | b4 a8 g8 b4 >d4< | g+4 b8 >e8 d4 <b4 | g2 f4 d4";
        String harm = "@0 v8 k4 s4 q6 o4 l16"
                + " e8 e8 e8 e4. c8 e8 | g2 r4 d4 L"
                + " [c e g e]4 | [<b >d g d]4 | [c e a e]4 | [c f a f]4"
                + " | [c e g e]4 | [<b >d g d]4 | [c f a f]2 [<b >d g d]2 | [c e g e]4"
                + " | [c e a e]4 | [<b >e g e]4 | [c f a f]4 | [c e g e]4"
                + " | [d f a f]4 | [<b >d g d]4 | [<b >e g+ e]4 | [<b >d f d]4";
        String bass = "v15 o3 l8 q6"
                + " c8 c8 c8 c4. <g8 >c8 | c2 r4 <g4> L"
                + " [c >c<]4 | [<g >g]4 | [<a >a]4 | [<f >f]4"
                + " | [c >c<]4 | [<g >g]4 | [<f >f]2 [<g >g]2 | [c >c<]4"
                + " | [<a >a]4 | [<e >e]4 | [<f >f]4 | [c >c<]4"
                + " | [d >d<]4 | [<g >g]4 | [<e >e]4 | [<g >g]4";
        String drums = "v10 o4 l8"
                + " c c c c4. d d | g2 r4 d d L"
                + " [c e d e c e d e]7 c e d e d d d d"
                + " [c e d e c e d e]7 c e d e d16 d16 d16 d16 d d";
        return Mml.parse(132, lead, harm, bass, drums);
    }

    // --------------------------------------------------------------- Route: F-Dur, gemütlich
    private static Mml.Track map() {
        String lead = "@2 v11 k4 s5 q6 o5 l8 L"
                + " a4 f8 a8 >c4< a4 | g4 e8 g8 >c4< g4 | f4 d8 f8 a4 f4 | b-4 a8 g8 f4 d4"
                + " | c4 f8 a8 >c4 f4< | >e4 d8 c8< g4 e4 | d4 f4 e4 g4 | f2 r2"
                + " | a4 >d8 c8< a4 f4 | e4 a8 >c8 e4 c4< | d4 f8 b-8 >d4 c4< | b-4 a8 g8 e4 c4"
                + " | f4 a8 >c8 f4 e4< | >d4 c8 <a8 f4 d4 | d4 b-4 >c4 <e4 | f2. r4";
        String harm = "@1 v7 k3 s3 q5 o4 l8 L"
                + " [r a r >c<]2 | [r e r g]2 | [r f r a]2 | [r f r b-]2"
                + " | [r a r >c<]2 | [r e r g]2 | r f r b- r e r g | [r a r >c<]2"
                + " | [r f r a]2 | [r e r a]2 | [r f r b-]2 | [r e r b-]2"
                + " | [r a r >c<]2 | [r f r a]2 | r f r b- r e r g | [r a r >c<]2";
        String bass = "v14 o3 l4 q7 L"
                + " f a >c< a | c e g e | d f a f | <b- >d f d"
                + " | f a >c< a | c e g e | <b- >d c e | f c <f >c"
                + " | d f a f | <a >c e c | <b- >d f d | c e g b-"
                + " | f a >c< a | d f a f | <b- >d c e | f c f2";
        String drums = "v6 o4 l8 L [c e e e d e e e]16";
        return Mml.parse(112, lead, harm, bass, drums);
    }

    // --------------------------------------------------------------- Kampf: a-Moll, treibend
    private static Mml.Track battle() {
        String lead = "@1 v13 k4 s6 q7 o6 l16"
                + " e d+ d c+ c <b a+ a g+ g f+ f e d+ d c+ | o4 a8 a8 r8 a8 >c4 e4 L"
                + " o5 l8 a8 r8 a8 >c8 e4 d8 c8< | b8 >c8< a8 e8 a4 r4 | a8 >c8 f4 e8 d8 c8< a8 | b8 >d8 g4 f8 e8 d8< b8"
                + " | >e4 d8 c8 d4 c8< b8 | a4 e8 a8 >c4 e4< | f4 a8 >c8 f4 e8 d8< | e4 d+8 e8 g+4 b4"
                + " | >d4 c8 d8 f4 a4< | >e4 c8 e8 a4 e4< | >f4 e8 d8 c4 d4< | b4 g+8 b8 >e4< b4"
                + " | >c4 <a8 >c8 f4 c4< | >d4 <b8 >d8 g4 d4< | >e8 d8 c8 <b8 a8 g8 a8 b8 | g+4 e4 b4 e4";
        String harm = "@0 v8 k3 s4 q6 o4 l16"
                + " r1 | e8 e8 r8 e8 a4 >c4< L"
                + " [a >c e c<]4 | [a >c e c<]4 | [a >c f c<]4 | [b >d g d<]4"
                + " | [a >c e c<]4 | [a >c e c<]4 | [a >c f c<]4 | [g+ b >e< b]4"
                + " | [a >d f d<]4 | [a >c e c<]4 | [a >d f d<]4 | [g+ b >e< b]4"
                + " | [a >c f c<]4 | [b >d g d<]4 | [a >c e c<]4 | [g+ b >e< b]4";
        String bass = "v15 o2 l8 q5"
                + " r1 | a8 a8 r8 a8 a4 >a4< L"
                + " [a >a<]4 | [a >a<]4 | [f >f<]4 | [g >g<]4"
                + " | [a >a<]4 | [a >a<]4 | [f >f<]4 | [e >e<]4"
                + " | [d >d<]4 | [a >a<]4 | [d >d<]4 | [e >e<]4"
                + " | [f >f<]4 | [g >g<]4 | [a >a<]4 | [e >e<]4";
        String drums = "v10 o4 l8"
                + " r2 d8 d8 d16 d16 d16 d16 | c8 c8 r8 c8 g4 d4 L"
                + " [c e d e c c d e]7 c e d e d16 d16 d16 d16 d d"
                + " [c e d e c c d e]7 c e d e d16 d16 d16 d16 d d";
        return Mml.parse(168, lead, harm, bass, drums);
    }

    // --------------------------------------------------------------- Boss: d-Moll, düster
    private static Mml.Track boss() {
        String lead = "@1 v14 k4 s6 q7 o5 l8"
                + " d8 r8 d8 r8 d8 c+8 d4 | f4 e4 c+4 <a4 L"
                + " o5 d8 f8 a8 >d8 c4 <a4 | b-8 a8 g8 f8 d4 f4 | e8 g8 >c8 e8 d4 c4< | c+4 e8 a8 g4 e4"
                + " | >d4 c8 <a8 >f4 e8 d8< | >d4 c8 <b-8 >d4 f4< | g4 f8 d8 b-4 g4 | a4 g8 a8 >c+2<"
                + " | b-4 >d8 c8 <b-4 g4 | a4 f8 a8 >d4< a4 | b-4 >d8 f8 b-4 a4< | >g4 e8 c8 <g4 e4"
                + " | d4 g8 b-8 >d4 c4< | c+4 e8 a8 >c+4 e4< | >d8 c8 <a8 f8 d8 f8 a8 >c8< | c+4 e4 a4 g4";
        String harm = "@0 v8 k3 s4 q6 o4 l16"
                + " r1 | a4 g4 e4 c+4 L"
                + " [d f a f]4 | [d f b- f]4 | [e g >c< g]4 | [c+ e a e]4"
                + " | [d f a f]4 | [d f b- f]4 | [d g b- g]4 | [c+ e a e]4"
                + " | [d g b- g]4 | [d f a f]4 | [d f b- f]4 | [e g >c< g]4"
                + " | [d g b- g]4 | [c+ e a e]4 | [d f a f]4 | [c+ e a e]4";
        String bass = "v15 o2 l8 q5"
                + " [d >d<]4 | a4 >a4< a4 >a4< L"
                + " [d >d<]4 | [b- >b-<]4 | o3 [c >c<]4 o2 | [a >a<]4"
                + " | [d >d<]4 | [b- >b-<]4 | [g >g<]4 | [a >a<]4"
                + " | [g >g<]4 | [d >d<]4 | [b- >b-<]4 | o3 [c >c<]4 o2"
                + " | [g >g<]4 | [a >a<]4 | [d >d<]4 | [a >a<]4";
        String drums = "v11 o4 l8"
                + " c r c r c r c r | c4 c4 d8 d8 d16 d16 d16 d16 L"
                + " [c e d e c c d c]7 c c d d d16 d16 d16 d16 d d"
                + " [c e d e c c d c]7 c c d d d16 d16 d16 d16 d d";
        return Mml.parse(176, lead, harm, bass, drums);
    }

    // --------------------------------------------------------------- Sieg: C-Dur Fanfare
    private static Mml.Track victory() {
        String lead = "@2 v13 k5 s5 q7 o5 l8"
                + " g8 g8 g8 >c4. <a8 b8 | >c4 e4 g2< L"
                + " e4 g4 e8 d8 c4 | c4 e4 a2 | a4 >c4< a8 g8 f4 | g4 b4 >d2<"
                + " | e4 g4 >c4< g4 | a4 >c4 e4 c4< | a4 f4 g4 b4 | >c2< r2";
        String harm = "@1 v8 k4 s4 q6 o4 l8"
                + " e8 e8 e8 e4. f8 f8 | e4 g4 >c2< L"
                + " [r e r g]2 | [r e r a]2 | [r f r a]2 | [r g r b]2"
                + " | [r e r g]2 | [r e r a]2 | r f r a r g r b | [r e r g]2";
        String bass = "v14 o3 l4 q6"
                + " c8 c8 c8 c4. f8 g8 | c4 e4 c2 L"
                + " c g e g | <a >e c e | <f >c <a >c | <g >d <b >d"
                + " | c g e g | <a >e c e | <f >c <g >d | c2 r2";
        String drums = "v8 o4 l8"
                + " c c c c4. d d | g2 r2 L [c e e e d e e e]8";
        return Mml.parse(140, lead, harm, bass, drums);
    }

    // --------------------------------------------------------------- Game Over: a-Moll, traurig
    private static Mml.Track gameOver() {
        String lead = "@2 v12 k5 s5 q7 o5 l4 e d c <b | a2 >c <b | a1";
        String harm = "@1 v7 k4 s4 o4 l4 c <b a g+ | f2 a g+ | e1";
        String bass = "v13 o3 l2 q7 a e | f e | <a1";
        String drums = "v5 o4 l4 r1 | r1 | g1";
        return Mml.parse(76, lead, harm, bass, drums);
    }

    private Music() {}
}
