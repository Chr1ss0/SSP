package de.chr1ss0.ssp.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The Gen-1 inspired battle screen. */
public final class BattleScreen extends Screen {
    private static final int P = Combat.P, E = Combat.E;

    // states
    private static final int ST_INTRO = 0, ST_CHOOSE = 1, ST_ACTION = 2, ST_DONE = 3;

    // actions
    private static final int A_MSG = 0, A_WAIT = 1, A_HOLD = 2, A_CHANT = 3, A_REVEAL = 4, A_ATTACK = 5, A_HIT = 6,
            A_HEAL = 7, A_BLOCK = 8, A_STATUS = 9, A_FAINT = 10, A_VICTORY = 12, A_DEFEAT = 13,
            A_MUSIC = 14, A_CHOOSE = 15, A_SLIDE_IN = 16;

    private static final class Act {
        int kind, side, a, b;
        boolean crit;
        String text;
        float t;
        boolean started;

        Act(int kind) { this.kind = kind; }
    }

    private static final class Part {
        float x, y, vx, vy, g, life, max;
        int color, size, kind; // kind 0 square, 1 circle, 2 diamond
    }

    private static final class Floater {
        float x, y, t;
        String text;
        int color;
    }

    // buttons
    private static final int B_WEAPON = 1, B_ARSENAL = 20, B_MENU = 21, B_ENEMY = 30;

    // layout
    private static final int EN_X = 150, PL_X = 6, TB_H = 52;
    private int oy, enY = 4, plY = 88, tbY = 172, menuY = 228;

    private final Profile prof;
    private final Run run;
    private Fighter pl, en;
    private final Combat combat;
    private final Random rng;
    private boolean initialized;

    private int state = ST_INTRO;
    private final ArrayList<Act> queue = new ArrayList<Act>();
    private Act cur;

    private final float[] dispHp = new float[2];
    private final int[] hold = {-1, -1};
    private final float[] lunge = new float[2];
    private final float[] blink = new float[2];
    private final float[] faint = new float[2];
    private final float[] bob = new float[2];
    private final float[] glow = new float[2];
    private float slideIn;
    private int lastHitTint;

    private String text = "";
    private float shown;
    private float autoMs;
    private float waitAfter;
    private boolean tapped;

    private int preEnemy = -1;
    private int lastWonRound;
    private String chooseText;
    private final ArrayList<Part> parts = new ArrayList<Part>();
    private final ArrayList<Floater> floaters = new ArrayList<Floater>();
    private final ArrayList<int[]> bolts = new ArrayList<int[]>();
    private float boltT;
    private int slashType = -1;
    private float slashT;
    private int slashSide;

    public BattleScreen(Game game) {
        super(game);
        prof = game.profile;
        run = prof.run;
        rng = game.rng;
        combat = new Combat(rng);
    }

    @Override
    public int music() {
        return run != null && Enemies.isBoss(run.stage) ? Sound.M_BOSS : Sound.M_BATTLE;
    }

    @Override
    public void enter() {
        if (initialized) {
            // returned from an overlay (card, arsenal, menu)
            if (pl != null) {
                if (state == ST_CHOOSE) dispHp[P] = pl.hp;
                else dispHp[P] = Math.min(dispHp[P], pl.maxHp);
            }
            return;
        }
        initialized = true;
        if (run == null) return;
        run.syncPlayer(prof);
        pl = run.player;
        en = run.createEnemy();
        pl.resetBattle();
        en.resetBattle();
        for (WeaponInst w : en.weapons) prof.seen[w.def.id] = true;
        dispHp[P] = pl.hp;
        dispHp[E] = en.hp;
        hold[P] = -1;
        hold[E] = -1;
        boolean resumed = run.enemyHp > 0;
        if (resumed) {
            applyMask(pl, run.usedMask);
            applyMask(en, run.enemyUsedMask);
        } else {
            run.usedMask = 0;
            run.enemyUsedMask = 0;
        }
        run.phase = Run.PHASE_BATTLE;
        run.enemyHp = en.hp;
        game.save();

        add(new Act(A_SLIDE_IN));
        String who = en.fullName();
        if (resumed) msg("Der Kampf gegen " + who + " geht weiter!", 1400);
        else if (Enemies.isBoss(run.stage)) {
            msg(who + " erwartet dich in der Arena " + Enemies.arenaName(run.stage).toUpperCase(java.util.Locale.GERMAN) + "!", -1);
            msg(en.name + ": \"" + bossLine() + "\"", -1);
        } else msg(who + " fordert dich heraus!", 1500);
        Act ch = new Act(A_CHOOSE);
        add(ch);
        state = ST_INTRO;
    }

    private static void applyMask(Fighter f, int mask) {
        for (int i = 0; i < f.weapons.size(); i++) {
            WeaponInst w = f.weapons.get(i);
            if (!w.isClassic() && (mask & (1 << i)) != 0) w.used = true;
        }
    }

    private static int maskOf(Fighter f) {
        int m = 0;
        for (int i = 0; i < f.weapons.size(); i++) if (f.weapons.get(i).used) m |= 1 << i;
        return m;
    }

    private String bossLine() {
        switch (Enemies.arenaIndex(run.stage)) {
            case 0: return "Meine Steine sind härter als dein Wille!";
            case 1: return "Gegen meine Wellen gehst du unter!";
            case 2: return "Ich stehe voll unter Strom!";
            case 3: return "Meine Pflanzen wachsen dir über den Kopf!";
            case 4: return "Spürst du die Hitze?";
            case 5: return "Hier oben friert jeder ein!";
            case 6: return "Ich finde jede Schwachstelle!";
            case 7: return "Meine Klingen sind messerscharf!";
            case 8: return "Wissen ist Macht. Papier ist Wissen!";
            default: return "Zeig mir, was du gelernt hast!";
        }
    }

    private void layout() {
        oy = Math.max(0, Math.min(56, (H() - 380) / 3));
        enY = 4 + oy;
        plY = 88 + oy;
        tbY = 172 + oy;
        menuY = tbY + TB_H + 4;
    }

    // ------------------------------------------------------------------ queue helpers

    private void add(Act a) {
        queue.add(a);
    }

    private void msg(String t, int auto) {
        Act a = new Act(A_MSG);
        a.text = t;
        a.a = auto;
        add(a);
    }

    private Act act(int kind, int side) {
        Act a = new Act(kind);
        a.side = side;
        add(a);
        return a;
    }

    // ------------------------------------------------------------------ round

    private void startRound(int weaponIndex) {
        WeaponInst pw = pl.weapons.get(weaponIndex);
        if (!pw.available()) {
            game.sound.sfx(Sound.S_ERROR);
            setText(pw.upper() + " wurde gegen " + en.name + " schon eingesetzt!", true);
            return;
        }
        int ei = preEnemy >= 0 && en.weapons.get(preEnemy).available() ? preEnemy : en.randomAvailable(rng);
        preEnemy = -1;
        WeaponInst ew = en.weapons.get(ei);
        state = ST_ACTION;
        hold[P] = pw.def.id;
        game.sound.sfx(Sound.S_SELECT);
        msg(pl.name + " wählt " + pw.upper() + "!", 650);
        act(A_CHANT, 0);
        Act rv = act(A_REVEAL, E);
        rv.a = ew.def.id;
        msg(en.name + " wählt " + ew.upper() + "!", 750);

        Combat.Result res = combat.round(pl, pw, en, ew);
        if (!pw.isClassic()) pw.used = true;
        if (!ew.isClassic()) ew.used = true;
        run.usedMask = maskOf(pl);
        run.enemyUsedMask = maskOf(en);
        for (Combat.Ev ev : res.events) {
            switch (ev.kind) {
                case Combat.Ev.MSG: msg(ev.text, 1100); break;
                case Combat.Ev.ATTACK: {
                    Act a = act(A_ATTACK, ev.side);
                    a.a = ev.type;
                    a.crit = ev.crit;
                    break;
                }
                case Combat.Ev.HIT: {
                    Act a = act(A_HIT, ev.side);
                    a.a = ev.hpAfter;
                    a.b = ev.amount;
                    a.crit = ev.crit;
                    break;
                }
                case Combat.Ev.HEAL: {
                    Act a = act(A_HEAL, ev.side);
                    a.a = ev.hpAfter;
                    a.b = ev.amount;
                    break;
                }
                case Combat.Ev.BLOCK: act(A_BLOCK, ev.side); break;
                case Combat.Ev.STATUS: act(A_STATUS, ev.side); break;
                default: break;
            }
        }
        if (res.outcome == Combat.DRAW && res.events.isEmpty()) msg("Unentschieden!", 900);
        lastWonRound = res.outcome == Combat.WIN ? 1 : 0;
        if (res.outcome == Combat.WIN) {
            run.roundsWon++;
            prof.roundsWon++;
        }

        boolean enemyDead = !en.alive();
        boolean playerDead = !pl.alive();
        if (enemyDead && playerDead) {
            pl.hp = 1;
            playerDead = false;
            Act h = act(A_HEAL, P);
            h.a = 1;
            h.b = 1;
            msg(pl.name + " hält sich mit letzter Kraft auf den Beinen!", -1);
        }
        if (enemyDead) {
            act(A_FAINT, E);
            Act m = act(A_MUSIC, 0);
            m.a = Sound.M_VICTORY;
            msg(en.fullName() + " wurde besiegt!", -1);
            if (Enemies.isBoss(run.stage)) {
                msg("Du hast die Arena " + Enemies.arenaName(run.stage).toUpperCase(java.util.Locale.GERMAN) + " erobert!", -1);
            }
            msg("Belohnung: " + Run.CARDS_PER_WIN + " Bonuskarten und eine Kiste!", 1200);
            act(A_VICTORY, 0);
        } else if (playerDead) {
            act(A_FAINT, P);
            Act m = act(A_MUSIC, 0);
            m.a = Sound.M_GAMEOVER;
            msg(pl.name + " ist erschöpft...", -1);
            act(A_DEFEAT, 0);
        } else {
            act(A_CHOOSE, 0);
        }
        // persist battle progress
        run.enemyHp = en.alive() ? en.hp : -1;
        if (!playerDead) game.save();
    }

    private void enterChoose() {
        state = ST_CHOOSE;
        hold[E] = -1;
        chooseText = "Welche Waffe wählt " + pl.name + "?";
        Stats ps = pl.passive();
        if (ps.seer > 0 && rng.nextInt(100) < ps.seer) {
            preEnemy = en.randomAvailable(rng);
            chooseText = "Hellsicht! " + en.name + " plant " + en.weapons.get(preEnemy).upper() + "!";
        }
        setText(chooseText, true);
    }

    private void setText(String t, boolean instant) {
        text = t;
        shown = instant ? t.length() : 0;
    }

    // ------------------------------------------------------------------ update

    private float charsPerMs() {
        switch (prof.textSpeed) {
            case 0: return 0.035f;
            case 2: return 0.2f;
            default: return 0.07f;
        }
    }

    @Override
    public void update(float dt) {
        layout();
        if (pl == null) {
            Flow.toTitle(game);
            return;
        }
        // animations
        for (int s = 0; s < 2; s++) {
            if (blink[s] > 0) blink[s] -= dt;
            if (glow[s] > 0) glow[s] -= dt;
            if (bob[s] > 0) bob[s] = Math.max(0, bob[s] - dt * 0.02f);
        }
        updateParticles(dt);

        int c = ui.click();
        if (state == ST_CHOOSE) {
            if (c >= B_WEAPON && c < B_WEAPON + pl.weapons.size()) {
                startRound(c - B_WEAPON);
            } else if (c == B_ARSENAL) {
                game.sound.sfx(Sound.S_SELECT);
                game.push(new ArsenalScreen(game, false));
            } else if (c == B_MENU) {
                game.sound.sfx(Sound.S_SELECT);
                game.push(new PauseScreen(game, true));
            } else if (c >= B_ENEMY && c < B_ENEMY + en.weapons.size()) {
                game.sound.sfx(Sound.S_CURSOR);
                WeaponInst w = en.weapons.get(c - B_ENEMY);
                String info = w.upper() + " (" + Element.NAME[w.def.type] + "): schlägt "
                        + Element.joinNames(Element.beatsList(w.def.type)) + ".";
                BonusDef b = w.mainBonus();
                if (b != null) info += " Bonus: " + b.describe().replace('\n', ',') + ".";
                setText(info, true);
            }
            if (shown < text.length()) shown = Math.min(text.length(), shown + dt * charsPerMs());
            return;
        }
        // process queue
        if (cur == null) {
            if (queue.isEmpty()) return;
            cur = queue.remove(0);
            cur.t = 0;
            cur.started = false;
        }
        if (!cur.started) {
            cur.started = true;
            startAct(cur);
        }
        cur.t += dt;
        if (stepAct(cur, dt)) {
            cur = null;
            tapped = false;
        }
    }

    private void startAct(Act a) {
        switch (a.kind) {
            case A_MSG:
                setText(a.text, false);
                autoMs = a.a;
                tapped = false;
                break;
            case A_HOLD:
                hold[a.side] = a.a;
                break;
            case A_REVEAL:
                hold[E] = a.a;
                game.sound.sfx(Sound.S_REVEAL);
                glow[E] = 300;
                break;
            case A_ATTACK:
                game.sound.sfx(Sound.S_ATK + a.a);
                spawnEffect(a.side, a.a);
                break;
            case A_HIT:
                if (a.b > 0) {
                    game.sound.sfx(a.crit ? Sound.S_CRIT : Sound.S_HIT);
                    blink[a.side] = 480;
                    if (a.crit) game.shake(300, 3);
                    if (a.side == P) game.vibrate(a.crit ? 80 : 40);
                    floater(a.side, "-" + a.b, Pal.RED);
                }
                break;
            case A_HEAL:
                game.sound.sfx(Sound.S_HEAL);
                floater(a.side, "+" + a.b, Pal.GREEN);
                sparkle(a.side);
                break;
            case A_BLOCK:
                game.sound.sfx(Sound.S_BLOCK);
                glow[a.side] = 500;
                break;
            case A_STATUS:
                bubbles(a.side);
                break;
            case A_FAINT:
                game.sound.sfx(Sound.S_FAINT);
                break;
            case A_MUSIC:
                game.sound.music(a.a);
                break;
            case A_VICTORY: {
                int healed = RunLogic.onVictory(prof, rng);
                game.save();
                game.set(new RewardScreen(game, healed), Game.TR_FADE);
                state = ST_DONE;
                break;
            }
            case A_DEFEAT: {
                int stage = run.stage;
                String by = en.fullName();
                RunLogic.onDefeat(prof);
                game.save();
                game.set(new GameOverScreen(game, stage, by, run), Game.TR_FADE);
                state = ST_DONE;
                break;
            }
            case A_CHOOSE:
                enterChoose();
                break;
            default:
                break;
        }
    }

    /** Returns true when the action is finished. */
    private boolean stepAct(Act a, float dt) {
        switch (a.kind) {
            case A_SLIDE_IN:
                slideIn = Math.min(1f, a.t / 700f);
                return a.t >= 750;
            case A_MSG: {
                if (shown < text.length()) {
                    shown = Math.min(text.length(), shown + dt * charsPerMs());
                    if (tapped) { shown = text.length(); tapped = false; }
                    waitAfter = 0;
                    return false;
                }
                waitAfter += dt;
                if (tapped) return true;
                return autoMs >= 0 && waitAfter >= autoMs;
            }
            case A_WAIT:
                return a.t >= a.a;
            case A_HOLD:
                return true;
            case A_CHANT: {
                int beat = Math.min(2, (int) (a.t / 380));
                String[] words = {"Schnick...", "Schnick... Schnack...", "Schnick... Schnack... Schnuck!"};
                if (!text.equals(words[beat])) {
                    setText(words[beat], true);
                    game.sound.sfx(Sound.S_TICK);
                    bob[P] = 4;
                    bob[E] = 4;
                }
                if (((int) (a.t / 70)) % 2 == 0) hold[E] = en.weapons.get(en.randomAvailable(rng)).def.id;
                return a.t >= 1140;
            }
            case A_REVEAL:
                return a.t >= 250;
            case A_ATTACK: {
                float p = Math.min(1f, a.t / 260f);
                lunge[a.side] = (float) Math.sin(p * Math.PI) * 12;
                if (a.t >= 520) {
                    lunge[a.side] = 0;
                    return true;
                }
                return false;
            }
            case A_HIT:
            case A_HEAL: {
                float target = a.a;
                float speed = Math.max(0.02f, Math.abs(dispHp[a.side] - target) / 450f) * dt;
                if (dispHp[a.side] > target) dispHp[a.side] = Math.max(target, dispHp[a.side] - speed);
                else dispHp[a.side] = Math.min(target, dispHp[a.side] + speed);
                return a.t >= 520 && Math.abs(dispHp[a.side] - target) < 0.01f;
            }
            case A_BLOCK:
                return a.t >= 450;
            case A_STATUS:
                return a.t >= 400;
            case A_FAINT:
                faint[a.side] = Math.min(1f, a.t / 650f);
                return a.t >= 750;
            case A_MUSIC:
                return true;
            case A_CHOOSE:
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onUp(int x, int y) {
        if (state != ST_CHOOSE) tapped = true;
    }

    @Override
    public boolean onBack() {
        if (state == ST_CHOOSE) {
            game.sound.sfx(Sound.S_SELECT);
            game.push(new PauseScreen(game, true));
        }
        return true;
    }

    // ------------------------------------------------------------------ effects

    private int cx(int side) {
        return side == E ? EN_X + 40 : PL_X + 48;
    }

    private int cy(int side) {
        return side == E ? enY + 46 : plY + 40;
    }

    private Part part(float x, float y, float vx, float vy, float life, int color, int size, int kind) {
        Part p = new Part();
        p.x = x; p.y = y; p.vx = vx; p.vy = vy; p.life = life; p.max = life;
        p.color = color; p.size = size; p.kind = kind;
        parts.add(p);
        return p;
    }

    private float rnd(float a, float b) {
        return a + rng.nextFloat() * (b - a);
    }

    private void spawnEffect(int attacker, int type) {
        int t = 1 - attacker;
        float x = cx(t), y = cy(t);
        switch (type) {
            case Element.STEIN:
                for (int i = 0; i < 4; i++) {
                    Part p = part(x + rnd(-24, 24), y - 70 - i * 18, 0, rnd(0.05f, 0.1f), 700, i % 2 == 0 ? 0xFF908880 : 0xFF706860, 6, 1);
                    p.g = 0.0009f;
                }
                game.shake(350, 2);
                break;
            case Element.KLINGE:
                slashType = type;
                slashT = 0;
                slashSide = t;
                break;
            case Element.PAPIER:
                slashType = type;
                slashT = 0;
                slashSide = t;
                break;
            case Element.FEUER:
                for (int i = 0; i < 26; i++) {
                    int col = i % 3 == 0 ? 0xFFF8D030 : (i % 3 == 1 ? 0xFFF08020 : 0xFFE03020);
                    part(x + rnd(-26, 26), y + rnd(10, 30), rnd(-0.02f, 0.02f), rnd(-0.12f, -0.05f), rnd(400, 800), col, (int) rnd(2, 5), 1);
                }
                break;
            case Element.WASSER:
                for (int i = 0; i < 24; i++) {
                    double ang = rnd(0, (float) Math.PI * 2);
                    Part p = part(x, y - 6, (float) Math.cos(ang) * rnd(0.04f, 0.12f), (float) Math.sin(ang) * rnd(0.04f, 0.1f) - 0.08f,
                            rnd(500, 800), i % 2 == 0 ? 0xFF3880F0 : 0xFF90D0F8, (int) rnd(2, 5), 1);
                    p.g = 0.0004f;
                }
                break;
            case Element.BLITZ:
                bolts.clear();
                int bx = (int) x + (int) rnd(-10, 10), by = -4;
                while (by < y) {
                    int nx = bx + (int) rnd(-12, 12), ny = by + (int) rnd(8, 16);
                    bolts.add(new int[]{bx, by, nx, ny});
                    bx = nx;
                    by = ny;
                }
                boltT = 450;
                game.flash(120);
                break;
            case Element.PFLANZE:
                for (int i = 0; i < 18; i++) {
                    double ang = i / 18.0 * Math.PI * 2;
                    Part p = part(x + (float) Math.cos(ang) * 34, y + (float) Math.sin(ang) * 30, -(float) Math.cos(ang) * 0.07f,
                            -(float) Math.sin(ang) * 0.06f, 520, i % 2 == 0 ? 0xFF48B040 : 0xFFA0E060, 4, 2);
                    p.g = 0;
                }
                break;
            case Element.STICH: {
                float sx = cx(attacker), sy = cy(attacker);
                for (int i = 0; i < 6; i++) {
                    Part p = part(sx, sy, (x - sx) / 300f, (y - sy) / 300f, 300 + i * 12, i == 0 ? 0xFFF8F8F8 : 0xFFC0C0D0, i == 0 ? 3 : 2, 0);
                    p.x -= (x - sx) * i * 0.03f;
                    p.y -= (y - sy) * i * 0.03f;
                }
                break;
            }
            case Element.EIS:
                for (int i = 0; i < 16; i++) {
                    double ang = i / 16.0 * Math.PI * 2;
                    float r = 46;
                    part(x + (float) Math.cos(ang) * r, y + (float) Math.sin(ang) * r, -(float) Math.cos(ang) * r / 450f,
                            -(float) Math.sin(ang) * r / 450f, 450, i % 2 == 0 ? 0xFFD0F0FF : 0xFF70D0F8, 5, 2);
                }
                break;
            default:
                break;
        }
        // generic impact star
        for (int i = 0; i < 8; i++) {
            double ang = i / 8.0 * Math.PI * 2;
            Part p = part(x, y, (float) Math.cos(ang) * 0.09f, (float) Math.sin(ang) * 0.09f, 300, Pal.WHITE, 3, 0);
            p.life = 300;
        }
    }

    private void sparkle(int side) {
        float x = cx(side), y = cy(side);
        for (int i = 0; i < 12; i++) {
            part(x + rnd(-24, 24), y + rnd(-10, 30), 0, rnd(-0.06f, -0.03f), rnd(500, 800), i % 2 == 0 ? 0xFF60E070 : 0xFFE0FFE0, 2, 2);
        }
    }

    private void bubbles(int side) {
        float x = cx(side), y = cy(side);
        for (int i = 0; i < 10; i++) {
            part(x + rnd(-20, 20), y + rnd(0, 26), 0, rnd(-0.05f, -0.02f), rnd(500, 900), i % 2 == 0 ? 0xFFA040C0 : 0xFF70C040, 3, 1);
        }
    }

    private void floater(int side, String t, int color) {
        Floater f = new Floater();
        f.x = cx(side) + rnd(-10, 10);
        f.y = cy(side) - 20;
        f.text = t;
        f.color = color;
        floaters.add(f);
    }

    private void updateParticles(float dt) {
        for (int i = parts.size() - 1; i >= 0; i--) {
            Part p = parts.get(i);
            p.vy += p.g * dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.life -= dt;
            if (p.life <= 0) parts.remove(i);
        }
        for (int i = floaters.size() - 1; i >= 0; i--) {
            Floater f = floaters.get(i);
            f.t += dt;
            f.y -= dt * 0.025f;
            if (f.t > 900) floaters.remove(i);
        }
        if (boltT > 0) boltT -= dt;
        if (slashType >= 0) {
            slashT += dt;
            if (slashT > 500) slashType = -1;
        }
    }

    // ------------------------------------------------------------------ render

    private int[] sceneCache;
    private int sceneH = -1;

    /** Sky, hills, meadow and platforms (cached per screen height). */
    private void drawScene(Gfx g) {
        int rows = tbY * g.k;
        if (sceneCache == null || sceneH != g.ph) {
            Gfx s = new Gfx(g.w, tbY, g.k);
            int horizon = enY + 58;
            s.gradientV(0, 0, s.w, horizon, 0xFF7DBDFF, 0xFFD7EEFF);
            // clouds
            int[][] clouds = {{30, enY + 18, 16}, {44, enY + 14, 12}, {200, enY + 66 - 52, 10}, {120, enY + 30, 9}, {132, enY + 27, 11}};
            for (int[] c : clouds) s.fillEllipse(c[0], c[1], c[2], c[2] * 0.55f, 0xCCFFFFFF);
            // hills
            s.fillEllipse(40, horizon + 6, 70, 22, 0xFFB6DDB3);
            s.fillEllipse(190, horizon + 8, 80, 26, 0xFFB6DDB3);
            s.fillEllipse(120, horizon + 14, 90, 22, 0xFF9DD49A);
            // meadow
            s.gradientV(0, horizon, s.w, tbY - horizon, 0xFFAEE08A, 0xFF74C25A);
            for (int y = horizon + 4; y < tbY; y += 6) {
                for (int x = (y * 7) % 13; x < s.w; x += 13) s.fill(x, y, 2, 1, 0x30FFFFFF);
            }
            // platforms
            platform(s, EN_X + 40, enY + 90, 46, 10);
            platform(s, PL_X + 48, tbY - 6, 62, 13);
            sceneCache = new int[rows * g.pw];
            System.arraycopy(s.px, 0, sceneCache, 0, Math.min(sceneCache.length, s.px.length));
            sceneH = g.ph;
        }
        System.arraycopy(sceneCache, 0, g.px, 0, Math.min(sceneCache.length, g.px.length));
    }

    private static void platform(Gfx s, int cx, int cy, int rx, int ry) {
        s.fillEllipse(cx, cy + 3, rx + 2, ry + 1, 0x40000000);
        s.fillEllipse(cx, cy, rx, ry, 0xFF6FAE52);
        s.fillEllipse(cx, cy - 1, rx - 2, ry - 2, 0xFFC5EDA0);
        s.fillEllipse(cx - 4, cy - 2, rx - 14, ry - 5, 0xFFD8F5BA);
    }

    @Override
    public void render(Gfx g) {
        layout();
        Ui.background(g);
        if (pl == null) return;
        int w = W();
        drawScene(g);

        float s = state == ST_INTRO ? slideIn : 1f;
        int ex = (int) (EN_X - (1 - s) * 230) + (int) (lunge[E] * -0.8f);
        int ey = enY + (int) (lunge[E] * 0.6f) + (int) bob[E];
        int px = (int) (PL_X + (1 - s) * 240) + (int) lunge[P];
        int py = plY - (int) (lunge[P] * 0.5f) + (int) bob[P];

        drawFighter(g, CharArt.front(en.look, hold[E]), ex, ey, E);
        drawFighter(g, CharArt.back(pl.look, hold[P]), px, py, P);
        drawEffects(g);

        if (state != ST_INTRO || slideIn >= 1f) {
            drawEnemyInfo(g);
            drawPlayerInfo(g);
        }

        // text box (dark, modern)
        g.shadow(4, tbY + 2, w - 8, TB_H - 4, 8, 70);
        g.roundRectGrad(4, tbY + 2, w - 8, TB_H - 4, 8, 0xF6343A58, 0xF6252940);
        g.roundBorder(4, tbY + 2, w - 8, TB_H - 4, 8, 1, 0xFF4B5276);
        List<String> lines = Font.wrap(text.substring(0, Math.min(text.length(), (int) shown)), w - 28);
        int start = Math.max(0, lines.size() - 3);
        for (int i = start; i < lines.size(); i++) g.text(lines.get(i), 13, tbY + 10 + (i - start) * 12, 0xFFF4F6FF);
        if (cur != null && cur.kind == A_MSG && shown >= text.length() && cur.a < 0 && ((int) (game.timeMs / 300) & 1) == 0) {
            g.text(String.valueOf(Font.MORE), w - 20, tbY + TB_H - 16, Pal.GOLD);
        }

        drawMenu(g);
    }

    private void drawFighter(Gfx g, Sprite sp, int x, int y, int side) {
        boolean visible = !(blink[side] > 0 && ((int) (blink[side] / 80)) % 2 == 0);
        if (!visible) return;
        if (faint[side] > 0) {
            int rows = sp.h;
            int cut = (int) (rows * faint[side]);
            int bottom = side == E ? enY + sp.h : tbY;
            g.clip(0, 0, W(), bottom);
            g.spriteRows(sp, x, y + cut, 1, 0, rows - cut);
            g.noClip();
            return;
        }
        if (side == P) g.clip(0, 0, W(), tbY + 3);
        if (glow[side] > 0 && ((int) (glow[side] / 60)) % 2 == 0) {
            g.sprite(sp, x, y, 1, false, Gfx.MODE_SOLID, 0xFFFFF4B0);
        } else {
            g.sprite(sp, x, y, 1);
        }
        g.noClip();
    }

    private void drawEffects(Gfx g) {
        for (Part p : parts) {
            int x = (int) p.x, y = (int) p.y, sz = p.size;
            int col = p.color;
            if (p.life < p.max * 0.3f && ((int) p.life / 40) % 2 == 0) continue;
            switch (p.kind) {
                case 1:
                    g.fillCircle(x, y, Math.max(1, sz / 2), Pal.OUTLINE);
                    g.fillCircle(x, y, Math.max(0, sz / 2 - 1), col);
                    if (sz <= 2) g.pset(x, y, col);
                    break;
                case 2:
                    for (int i = 0; i <= sz; i++) {
                        g.hline(x - (sz - i), y - i, (sz - i) * 2 + 1, col);
                        g.hline(x - (sz - i), y + i, (sz - i) * 2 + 1, col);
                    }
                    break;
                default:
                    g.fill(x - sz / 2, y - sz / 2, sz, sz, col);
            }
        }
        if (boltT > 0) {
            int col = ((int) (boltT / 60)) % 2 == 0 ? 0xFFF8E040 : Pal.WHITE;
            for (int[] b : bolts) {
                g.thickLine(b[0], b[1], b[2], b[3], 3, Pal.INK);
            }
            for (int[] b : bolts) g.line(b[0], b[1], b[2], b[3], col);
        }
        if (slashType >= 0) {
            int x = cx(slashSide), y = cy(slashSide);
            float p = slashT / 500f;
            if (slashType == Element.KLINGE) {
                for (int k = 0; k < 3; k++) {
                    float pk = Math.min(1f, Math.max(0f, p * 3 - k * 0.6f));
                    if (pk <= 0) continue;
                    int ox = -18 + k * 14;
                    int x0 = x + ox - 14, y0 = y - 26;
                    int x1 = (int) (x0 + 28 * pk), y1 = (int) (y0 + 50 * pk);
                    g.thickLine(x0, y0, x1, y1, 3, Pal.INK);
                    g.line(x0, y0, x1, y1, Pal.WHITE);
                }
            } else {
                for (int k = 0; k < 4; k++) {
                    float pk = Math.min(1f, Math.max(0f, p * 2.5f - k * 0.35f));
                    if (pk <= 0) continue;
                    int bw = (int) (70 * pk);
                    int yy = y - 24 + k * 14;
                    g.fill(x - bw / 2, yy, bw, 7, Pal.INK);
                    g.fill(x - bw / 2 + 1, yy + 1, Math.max(0, bw - 2), 5, 0xFFF0EAD8);
                }
            }
        }
        for (Floater f : floaters) {
            int x = (int) f.x, y = (int) f.y;
            g.textOutline(f.text, x - Font.width(f.text, 1) / 2, y, f.color, Pal.WHITE, 1);
        }
    }

    private static void infoPanel(Gfx g, int x, int y, int w, int h) {
        g.shadow(x, y, w, h, 7, 50);
        g.roundRectGrad(x, y, w, h, 7, 0xF2FFFFFF, 0xF2EEF1FA);
        g.roundBorder(x, y, w, h, 7, 1, 0xFFC9CFE3);
    }

    private static void levelPill(Gfx g, int level, int rightX, int y) {
        String s = "Lv." + level;
        int w = Font.width(s, 1) + 8;
        g.roundRect(rightX - w, y - 1, w, 10, 5, Pal.ACCENT);
        g.text(s, rightX - w + 4, y, Pal.WHITE);
    }

    private void drawEnemyInfo(Gfx g) {
        int x = 6, y = enY + 3, w = 126;
        infoPanel(g, x, y, w, 30);
        g.text(ellipsize(en.name, 70), x + 7, y + 5, Pal.INK);
        if (en.poisonTurns > 0) {
            g.roundRect(x + 10 + Font.width(en.name, 1), y + 4, 22, 10, 5, 0xFFA040C0);
            g.text("GIFT", x + 12 + Font.width(en.name, 1), y + 5, Pal.WHITE);
        }
        levelPill(g, en.level, x + w - 6, y + 5);
        Ui.hpBar(g, x + 7, y + 17, w - 14, dispHp[E], en.maxHp);
        // enemy arsenal (used specials are crossed out)
        for (int i = 0; i < en.weapons.size(); i++) {
            int wx = x + i * 21, wy = y + 33;
            WeaponInst wi = en.weapons.get(i);
            Ui.weaponTile(g, wi.def.id, wx, wy, true);
            if (wi.equipped() > 0) g.fillEllipse(wx + 17, wy + 3, 2, 2, Pal.RARITY[3]);
            if (!wi.available()) {
                g.roundRect(wx, wy, 20, 20, 4, 0xB0EEF0F6);
                g.textCenter(String.valueOf(Font.CROSS), wx + 10, wy + 7, Pal.RED);
            }
            if (state == ST_CHOOSE) ui.region(B_ENEMY + i, wx, wy, 20, 20);
        }
    }

    private void drawPlayerInfo(Gfx g) {
        int x = 108, y = plY + 22, w = 126;
        infoPanel(g, x, y, w, 38);
        g.text(ellipsize(pl.name, 70), x + 7, y + 5, Pal.INK);
        if (pl.poisonTurns > 0) {
            g.roundRect(x + 10 + Font.width(pl.name, 1), y + 4, 22, 10, 5, 0xFFA040C0);
            g.text("GIFT", x + 12 + Font.width(pl.name, 1), y + 5, Pal.WHITE);
        }
        levelPill(g, run.stage, x + w - 6, y + 5);
        Ui.hpBar(g, x + 7, y + 17, w - 14, dispHp[P], pl.maxHp);
        String hp = Math.round(dispHp[P]) + " / " + pl.maxHp;
        g.textRight(hp, x + w - 7, y + 27, Pal.INK_SOFT);
        if (pl.focus > 0) g.text("FOKUS +" + pl.focus, x + 7, y + 27, Pal.BLUE);
    }

    private void drawMenu(Gfx g) {
        int w = W();
        int bottomH = 24;
        int n = pl.weapons.size();
        int cols = 3;
        int colW = (w - 16) / cols;
        int avail = H() - menuY - bottomH - 6;
        int rowH = Math.max(48, Math.min(56, avail / 2));
        int top = Math.max(menuY, H() - bottomH - 6 - 2 * rowH);
        boolean active = state == ST_CHOOSE;

        // tall screens: enemy arsenal overview in the free space
        int gapTop = menuY + 2, gapBottom = top - 14;
        int listRows = (en.weapons.size() + 2) / 3;
        int panelH = 18 + listRows * 22;
        if (gapBottom - gapTop >= panelH + 4) {
            int py = gapTop + (gapBottom - gapTop - panelH) / 2;
            Ui.panel(g, 6, py, w - 12, panelH, 0xFFF7F8FC, Pal.PANEL_BORDER);
            g.text("ARSENAL VON " + en.name, 12, py + 5, Pal.INK_SOFT);
            g.textRight("Runde " + (pl.rounds + (state == ST_CHOOSE ? 1 : 0)), w - 12, py + 5, Pal.INK_SOFT);
            int cw = (w - 20) / 3;
            for (int i = 0; i < en.weapons.size(); i++) {
                WeaponInst ew = en.weapons.get(i);
                int x = 10 + (i % 3) * cw, y = py + 17 + (i / 3) * 22;
                Ui.weaponTile(g, ew.def.id, x, y, true);
                g.text(ellipsize(ew.displayName(), cw - 26), x + 23, y + 2, ew.available() ? Pal.INK : Pal.GRAY);
                if (ew.available()) {
                    int[] bl = Element.beatsList(ew.def.type);
                    for (int k = 0; k < bl.length; k++) g.spriteHD(Art.typeIcon(bl[k]), x + 23 + k * 10, y + 11);
                } else {
                    g.text("benutzt", x + 23, y + 12, Pal.GRAY);
                }
                if (state == ST_CHOOSE) ui.region(B_ENEMY + i, x, y, cw - 2, 20);
            }
        }
        if (active && top > menuY + 12) {
            g.textCenter("WÄHLE DEINE WAFFE", w / 2, top - 11, Pal.INK_SOFT);
        }

        for (int i = 0; i < n; i++) {
            WeaponInst wi = pl.weapons.get(i);
            int x = 6 + (i % cols) * (colW + 2), y = top + (i / cols) * rowH;
            int id = B_WEAPON + i;
            boolean usable = active && wi.available();
            boolean pressed = usable && ui.isPressed(id);
            int bh = rowH - 4;
            int yy = y + (pressed ? 1 : 0);
            if (!pressed && usable) g.shadow(x, y, colW, bh, 7, 40);
            int topC = pressed ? 0xFFFFF4C8 : 0xFFFFFFFF, botC = pressed ? 0xFFFFE9A8 : 0xFFEEF1FA;
            g.roundRectGrad(x, yy, colW, bh, 7, topC, botC);
            g.roundBorder(x, yy, colW, bh, 7, 1, wi.isClassic() ? 0xFFC3C9DE : 0xFFB9A9FF);
            // badge: unlimited classics vs once-per-opponent specials
            String badge = wi.isClassic() ? "\u221E" : "1\u00D7";
            int bc = wi.isClassic() ? 0xFF8A90A8 : Pal.ACCENT;
            g.roundRect(x + colW - 16, yy + 3, 13, 10, 5, bc);
            g.textCenter(badge, x + colW - 9, yy + 4, Pal.WHITE);
            Ui.weaponTile(g, wi.def.id, x + (colW - 20) / 2, yy + 4, true);
            g.textCenter(ellipsize(wi.displayName(), colW - 6), x + colW / 2, yy + 27, Pal.INK);
            int cy = yy + 37;
            if (wi.isClassic()) {
                String tn = Element.NAME[wi.def.type] + "  " + Font.SWORD + wi.stats().atk;
                g.textCenter(tn, x + colW / 2, cy + 2, Pal.INK_SOFT);
            } else {
                int slots = wi.slots.length;
                int sx = x + (colW - (slots * 13 - 1)) / 2;
                for (int k = 0; k < slots; k++) {
                    if (wi.slots[k] >= 0) Ui.cardMini(g, BonusDef.get(wi.slots[k]), sx + k * 13, cy);
                    else {
                        g.roundRect(sx + k * 13, cy, 12, 12, 3, 0xFFF1F2F8);
                        g.roundBorder(sx + k * 13, cy, 12, 12, 3, 1, 0xFFD3D7E6);
                    }
                }
            }
            if (!wi.available()) {
                g.roundRect(x, yy, colW, bh, 7, 0xE6EEF0F6);
                int pw = Math.min(colW - 6, Font.width("BENUTZT", 1) + 12);
                g.roundRect(x + (colW - pw) / 2, yy + 24, pw, 13, 6, 0xFF8A90A8);
                g.textCenter("BENUTZT", x + colW / 2, yy + 27, Pal.WHITE);
            } else if (!active) {
                g.roundRect(x, yy, colW, bh, 7, 0x60EEF0F6);
            }
            if (usable) ui.region(id, x, y, colW, bh);
        }
        int by = H() - bottomH - 2;
        int half = (w - 18) / 2;
        ui.button(g, B_ARSENAL, 6, by, half, 22, "ARSENAL", active ? Ui.BTN_NORMAL : Ui.BTN_DISABLED);
        ui.button(g, B_MENU, 12 + half, by, half, 22, "MENÜ", active ? Ui.BTN_NORMAL : Ui.BTN_DISABLED);
    }

    static String ellipsize(String s, int maxW) {
        if (Font.width(s, 1) <= maxW) return s;
        while (s.length() > 1 && Font.width(s + ".", 1) > maxW) s = s.substring(0, s.length() - 1);
        return s + ".";
    }
}
