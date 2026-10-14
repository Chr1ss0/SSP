package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.*;

import java.util.Random;

/**
 * Drives the real game headless: scripted walkthrough with screenshots,
 * then a random "monkey" test to catch crashes. Usage: Shots outDir [height] [nomonkey|actionsPerHeight]
 */
public final class Shots {
    static final class FakePlatform implements Platform {
        String data;
        public String loadSave() { return data; }
        public void writeSave(String d) { data = d; Profile.decode(d); }
        public void vibrate(int ms) {}
        public void quit() {}
    }

    static Game game;
    static String out;
    static int shotNo;

    public static void step(int ms) {
        for (int t = 0; t < ms; t += 16) game.frame(16);
    }

    public static void shot(String name) throws Exception {
        game.frame(16);
        Png.write(game.g, 3, String.format("%s/%02d_%s.png", out, ++shotNo, name));
    }

    public static boolean tap(int id) {
        int[] r = game.ui.find(id);
        if (r == null) return false;
        int x = r[0] + r[2] / 2, y = r[1] + r[3] / 2;
        game.touchDown(x, y);
        game.frame(16);
        game.touchUp(x, y);
        step(40);
        return true;
    }

    /** Holds a region long enough for a long press. */
    public static boolean longTap(int id) {
        int[] r = game.ui.find(id);
        if (r == null) return false;
        int x = r[0] + r[2] / 2, y = r[1] + r[3] / 2;
        game.touchDown(x, y);
        step((int) Ui.LONG_MS + 100);
        game.touchUp(x, y);
        step(40);
        return true;
    }

    public static void tapAt(int x, int y) {
        game.touchDown(x, y);
        game.frame(16);
        game.touchUp(x, y);
        step(40);
    }

    public static String top() {
        return game.top().getClass().getSimpleName();
    }

    static boolean waitFor(String screen, int maxMs) {
        for (int t = 0; t < maxMs; t += 50) {
            if (top().equals(screen)) return true;
            step(50);
        }
        return top().equals(screen);
    }

    public static void main(String[] args) throws Exception {
        out = args.length > 0 ? args[0] : "out";
        int h = args.length > 1 ? Integer.parseInt(args[1]) : 520;
        FakePlatform pf = new FakePlatform();
        game = new Game(pf, h);
        step(500);
        shot("title");
        tap(2); // new game -> editor
        step(400);
        shot("editor");
        tap(1); // name
        step(300);
        tap(0);
        tap(101);
        shot("name");
        tap(1002);
        step(400);
        tap(3); // done -> new run
        step(800);
        shot("map");
        tap(1); // fight
        step(1200);
        shot("battle_intro");
        int guard = 0;
        boolean choose = false;
        while (guard++ < 60) {
            if (game.ui.find(1) != null && top().equals("BattleScreen")) { choose = true; break; }
            tapAt(120, 200);
            step(300);
        }
        System.out.println("reached choose: " + choose);
        shot("battle_choose");
        tap(31);
        shot("battle_enemyinfo");
        longTap(2);
        waitFor("WeaponScreen", 1500);
        step(400);
        shot("battle_longpress");
        game.back();
        waitFor("BattleScreen", 1500);
        step(700);
        tap(1); // Stein
        step(250);
        shot("battle_chant");
        step(1100);
        shot("battle_reveal");
        boolean shotAttack = false, shotUsed = false;
        Random rr = new Random(3);
        int victoryGuard = 0;
        while (victoryGuard++ < 4000) {
            String t = top();
            if (t.equals("RewardScreen") || t.equals("GameOverScreen")) break;
            if (t.equals("BattleScreen") && game.ui.find(1) != null) {
                if (!shotUsed) { shot("battle_used"); shotUsed = true; }
                int n = 1 + rr.nextInt(game.profile.run.player.weapons.size());
                tap(n);
                step(400);
                continue;
            }
            if (!shotAttack && t.equals("BattleScreen")) {
                step(120);
                shot("battle_action");
                shotAttack = true;
            }
            tapAt(120, 200);
            step(200);
        }
        System.out.println("after battle: " + top());
        if (top().equals("RewardScreen")) {
            step(1000);
            shot("box_closed");
            tap(1);
            step(1300);
            shot("box_open");
            step(1200);
            shot("box_show");
            tap(2); // into the arsenal
            step(1400);
            shot("reward_cards");
            tap(10);
            step(400);
            shot("equip");
            tap(100 + 3 * 10);
            step(500);
            shot("reward_cards_equipped");
            tap(4);
            waitFor("MapScreen", 3000);
            step(400);
            shot("map2");
        } else {
            step(1500);
            shot("gameover");
            tap(1);
            step(800);
        }
        if (top().equals("MapScreen")) {
            tap(2);
            step(400);
            shot("arsenal");
            tap(100);
            step(400);
            shot("weapon");
            tap(10);
            step(400);
            shot("cardpick");
            game.back();
            step(400);
            game.back();
            step(400);
            game.back();
            step(400);
        }
        System.out.println("now: " + top());
        if (top().equals("MapScreen")) {
            tap(4);
            step(300);
            shot("pause");
            tap(1);
            step(300);
            tap(3);
            step(400);
            shot("album");
            tap(1000 + firstCard());
            shot("album_sel");
            game.back();
            step(400);
        }
        game.set(new TitleScreen(game), Game.TR_NONE);
        step(300);
        shot("title2");
        tap(4);
        step(400);
        shot("dex");
        tap(1000 + 19);
        step(400);
        shot("dex_weapon");
        game.back();
        waitFor("DexScreen", 1500);
        step(700);
        tap(2);
        waitFor("ChartScreen", 1500);
        step(400);
        shot("chart");
        tap(101);
        shot("chart_sel");
        game.back();
        waitFor("DexScreen", 1500);
        step(700);
        game.back();
        waitFor("TitleScreen", 1500);
        step(700);
        tap(6);
        waitFor("OptionsScreen", 1500);
        step(400);
        shot("options");
        game.back();
        waitFor("TitleScreen", 1500);
        step(700);
        tap(7);
        waitFor("HelpScreen", 1500);
        step(400);
        shot("help");
        game.back();
        step(300);

        if (args.length > 2 && args[2].equals("nomonkey")) return;
        int perHeight = args.length > 2 ? Integer.parseInt(args[2]) : 25000;
        // ---- monkey test
        Random r = new Random(7);
        long frames = 0;
        int[] heights = {360, 400, 480, 560, 640};
        for (int round = 0; round < heights.length; round++) {
            game.resize(heights[round]);
            for (int i = 0; i < perHeight; i++) {
                int[] ids = game.ui.ids();
                if (ids.length > 0 && r.nextInt(3) > 0) {
                    int id = ids[r.nextInt(ids.length)];
                    // avoid wiping the save in the monkey run
                    if (top().equals("OptionsScreen") && id == 2) continue;
                    if (top().equals("DialogScreen") && r.nextInt(4) == 0) id = 1;
                    tap(id);
                } else if (r.nextInt(10) == 0) {
                    if (!game.back()) game.set(new TitleScreen(game), Game.TR_NONE);
                } else {
                    tapAt(r.nextInt(240), r.nextInt(game.H));
                }
                step(r.nextInt(400));
                frames++;
            }
            System.out.println("monkey h=" + heights[round] + " ok, top=" + top() + " best=" + game.profile.bestStage
                    + " cards=" + game.profile.totalCards() + " runs=" + game.profile.runs);
        }
        System.out.println("monkey actions: " + frames);
        Profile back = Profile.decode(pf.data);
        System.out.println("save roundtrip: cards=" + back.totalCards() + " run=" + (back.run != null ? back.run.stage : -1));
    }

    static int firstCard() {
        for (int i = 0; i < game.profile.cards.length; i++) if (game.profile.cards[i] > 0) return i;
        return 0;
    }
}
