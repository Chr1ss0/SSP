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
            A_HEAL = 7, A_BLOCK = 8, A_STATUS = 9, A_FAINT = 10, A_CARD = 11, A_VICTORY = 12, A_DEFEAT = 13,
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
            if (cur != null && cur.kind == A_CARD) cur.t = 99999;
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
        int ei = preEnemy >= 0 ? preEnemy : rng.nextInt(en.weapons.size());
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
        if (res.outcome == Combat.WIN) run.roundsWon++;

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
            if (lastWonRound == 1) {
                msg("Du ziehst eine Bonuskarte!", 700);
                act(A_CARD, 0);
            }
            act(A_VICTORY, 0);
        } else if (playerDead) {
            act(A_FAINT, P);
            Act m = act(A_MUSIC, 0);
            m.a = Sound.M_GAMEOVER;
            msg(pl.name + " ist erschöpft...", -1);
            act(A_DEFEAT, 0);
        } else {
            if (lastWonRound == 1) {
                msg("Rundensieg! Du ziehst eine Bonuskarte!", 700);
                act(A_CARD, 0);
            }
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
            preEnemy = rng.nextInt(en.weapons.size());
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
            case A_CARD: {
                BonusDef b = RunLogic.drawCard(prof, rng);
                game.sound.sfx(Sound.S_CARD);
                game.save();
                boolean popup = prof.cardPopup == 0 || (prof.cardPopup == 1 && b.rarity >= WeaponDef.SELTEN);
                if (popup) {
                    game.push(new CardScreen(game, b));
                } else {
                    // short notice instead of the overlay
                    a.t = 99999;
                    Act m = new Act(A_MSG);
                    m.text = "Karte: " + b.upper() + " (" + WeaponDef.RARITY_NAME[b.rarity] + ") - " + b.describe().replace('\n', ',');
                    m.a = 1300;
                    queue.add(0, m);
                }
                break;
            }
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
                if (((int) (a.t / 70)) % 2 == 0) hold[E] = en.weapons.get(rng.nextInt(en.weapons.size())).def.id;
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
            case A_CARD:
                return a.t >= 99999;
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

    @Override
    public void render(Gfx g) {
        layout();
        g.clear(Pal.BG);
        if (oy > 0) {
            // soft sky band above the scene on tall screens
            g.fill(0, 0, W(), oy, 0xFFE8F0F8);
            g.hline(0, oy, W(), 0xFFD8E4F0);
        }
        if (pl == null) return;
        int w = W();

        // ground
        float s = state == ST_INTRO ? slideIn : 1f;
        int ex = (int) (EN_X - (1 - s) * 230) + (int) (lunge[E] * -0.8f);
        int ey = enY + (int) (lunge[E] * 0.6f) + (int) bob[E];
        int px = (int) (PL_X + (1 - s) * 240) + (int) lunge[P];
        int py = plY - (int) (lunge[P] * 0.5f) + (int) bob[P];
        g.fillEllipse(EN_X + 40 + (int) ((s - 1) * 230), enY + 92, 40, 7, Pal.GROUND);
        g.fillEllipse(EN_X + 40 + (int) ((s - 1) * 230), enY + 92, 34, 4, Pal.GROUND_DARK);
        g.fillEllipse(PL_X + 50 + (int) ((1 - s) * 240), tbY - 2, 52, 8, Pal.GROUND);

        // enemy sprite
        Sprite es = CharArt.front(en.look, hold[E]);
        drawFighter(g, es, ex, ey, E);
        // player sprite
        Sprite ps = CharArt.back(pl.look, hold[P]);
        drawFighter(g, ps, px, py, P);

        // effects
        drawEffects(g);

        // info boxes (hidden during intro slide)
        if (state != ST_INTRO || slideIn >= 1f) {
            drawEnemyInfo(g);
            drawPlayerInfo(g);
        }

        // text box
        Ui.box(g, 0, tbY, w, TB_H);
        List<String> lines = Font.wrap(text.substring(0, Math.min(text.length(), (int) shown)), w - 22);
        int start = Math.max(0, lines.size() - 3);
        for (int i = start; i < lines.size(); i++) g.text(lines.get(i), 10, tbY + 9 + (i - start) * 12, Pal.INK);
        if (cur != null && cur.kind == A_MSG && shown >= text.length() && cur.a < 0 && ((int) (game.timeMs / 300) & 1) == 0) {
            g.text(String.valueOf(Font.MORE), w - 16, tbY + TB_H - 13, Pal.INK);
        }

        // menu
        drawMenu(g);
    }

    private void drawFighter(Gfx g, Sprite sp, int x, int y, int side) {
        boolean visible = !(blink[side] > 0 && ((int) (blink[side] / 80)) % 2 == 0);
        if (!visible) return;
        int scale = 2;
        if (faint[side] > 0) {
            int rows = sp.h;
            int cut = (int) (rows * faint[side]);
            int bottom = side == E ? enY + sp.h * scale : tbY;
            g.clip(0, 0, W(), bottom);
            g.spriteRows(sp, x, y + cut * scale, scale, 0, rows - cut);
            g.noClip();
            return;
        }
        if (side == P) g.clip(0, 0, W(), tbY);
        if (glow[side] > 0 && ((int) (glow[side] / 60)) % 2 == 0) {
            g.sprite(sp, x, y, scale, false, Gfx.MODE_SOLID, 0xFFF8F0A0);
        } else {
            g.sprite(sp, x, y, scale);
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

    private void drawEnemyInfo(Gfx g) {
        int x = 4, y = 4 + oy;
        g.text(en.name, x + 4, y + 2, Pal.INK);
        g.textRight(":L" + en.level, x + 124, y + 2, Pal.INK);
        if (en.poisonTurns > 0) g.text("GIF", x + 4 + Font.width(en.name, 1) + 4, y + 2, 0xFFA040C0);
        Ui.hpBar(g, x + 14, y + 14, 108, dispHp[E], en.maxHp);
        // Gen-1 style bracket
        g.vline(x, y + 12, 14, Pal.INK);
        g.hline(x, y + 25, 126, Pal.INK);
        g.pset(x + 126, y + 24, Pal.INK);
        g.pset(x + 127, y + 23, Pal.INK);
        // enemy arsenal
        for (int i = 0; i < en.weapons.size(); i++) {
            int wx = x + 2 + i * 21, wy = y + 30;
            Ui.weaponTile(g, en.weapons.get(i).def.id, wx, wy, true);
            if (en.weapons.get(i).equipped() > 0) g.fill(wx + 15, wy + 1, 4, 4, Pal.RARITY[3]);
            if (state == ST_CHOOSE) ui.region(B_ENEMY + i, wx, wy, 20, 20);
        }
    }

    private void drawPlayerInfo(Gfx g) {
        int x = 108, y = 112 + oy;
        g.text(pl.name, x + 6, y, Pal.INK);
        g.textRight(":L" + run.stage, x + 126, y, Pal.INK);
        if (pl.poisonTurns > 0) g.text("GIF", x + 6 + Font.width(pl.name, 1) + 4, y, 0xFFA040C0);
        Ui.hpBar(g, x + 16, y + 12, 110, dispHp[P], pl.maxHp);
        String hp = Math.round(dispHp[P]) + "/" + pl.maxHp;
        g.textRight(hp, x + 126, y + 23, Pal.INK);
        if (pl.focus > 0) g.text("FOKUS +" + pl.focus, x + 6, y + 23, Pal.BLUE);
        // bracket
        g.vline(x + 128, y + 10, 26, Pal.INK);
        g.hline(x + 2, y + 35, 127, Pal.INK);
        g.pset(x + 1, y + 34, Pal.INK);
        g.pset(x, y + 33, Pal.INK);
    }

    private void drawMenu(Gfx g) {
        int w = W();
        int bottomH = 24;
        int n = pl.weapons.size();
        int rows = (n + 1) / 2;
        int avail = H() - menuY - bottomH - 8;
        int rowH = Math.max(32, Math.min(44, avail / 3));
        int top = Math.max(menuY, H() - bottomH - 6 - rows * rowH);
        int colW = (w - 18) / 2;
        boolean active = state == ST_CHOOSE;
        if (active && top > menuY + 12) {
            g.textCenter("WÄHLE DEINE WAFFE", w / 2, top - 11, Pal.INK_SOFT);
        }
        // tall screens: enemy arsenal overview in the free space
        int gapTop = menuY + 2, gapBottom = top - 16;
        int en_n = en.weapons.size();
        int listRows = (en_n + 1) / 2;
        int panelH = 16 + listRows * 22;
        if (gapBottom - gapTop >= panelH + 4) {
            int py = gapTop + (gapBottom - gapTop - panelH) / 2;
            Ui.panel(g, 6, py, w - 12, panelH, 0xFFF0EEE4, Pal.GRAY);
            g.text("ARSENAL VON " + en.name, 12, py + 4, Pal.INK_SOFT);
            g.textRight("Runde " + (pl.rounds + (state == ST_CHOOSE ? 1 : 0)), w - 12, py + 4, Pal.INK_SOFT);
            for (int i = 0; i < en_n; i++) {
                WeaponInst ew = en.weapons.get(i);
                int x = 10 + (i % 2) * ((w - 20) / 2), y = py + 15 + (i / 2) * 22;
                Ui.weaponTile(g, ew.def.id, x, y, true);
                g.text(ellipsize(ew.displayName(), (w - 20) / 2 - 30), x + 24, y + 2, Pal.INK);
                int[] bl = Element.beatsList(ew.def.type);
                g.text(Font.UP + "", x + 24, y + 12, Pal.GREEN);
                for (int k = 0; k < bl.length; k++) g.sprite(Art.typeIcon(bl[k]), x + 31 + k * 10, y + 11);
                if (state == ST_CHOOSE) ui.region(B_ENEMY + i, x, y, (w - 20) / 2 - 2, 20);
            }
        }
        for (int i = 0; i < n; i++) {
            WeaponInst wi = pl.weapons.get(i);
            int x = 6 + (i % 2) * (colW + 6), y = top + (i / 2) * rowH;
            int id = B_WEAPON + i;
            boolean pressed = ui.isPressed(id);
            int fill = !active ? Pal.LIGHT : (pressed ? Pal.SELECT : Pal.WHITE);
            Ui.panel(g, x, y + (pressed ? 1 : 0), colW, rowH - 3, fill, active ? Pal.INK : Pal.GRAY);
            int iy = y + (rowH - 3 - 20) / 2 + (pressed ? 1 : 0);
            Ui.weaponTile(g, wi.def.id, x + 3, iy, true);
            String name = ellipsize(wi.displayName(), colW - 30);
            int ty = y + (rowH - 3) / 2 - 9 + (pressed ? 1 : 0);
            g.text(name, x + 27, ty, active ? Pal.INK : Pal.INK_SOFT);
            int[] m = Combat.matchup(wi, en.weapons);
            int hx = x + 27, hy = ty + 11;
            g.text(Font.UP + "" + m[0], hx, hy, Pal.GREEN);
            g.text("=" + m[1], hx + 16, hy, Pal.INK_SOFT);
            g.text(Font.MORE + "" + m[2], hx + 32, hy, Pal.RED);
            Stats st = wi.stats();
            g.textRight(Font.SWORD + "" + st.atk, x + colW - 4, hy, Pal.INK_SOFT);
            if (active) ui.region(id, x, y, colW, rowH - 3);
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
