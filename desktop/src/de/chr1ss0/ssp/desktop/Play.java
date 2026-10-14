package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.*;

/**
 * Plays complete runs through the real UI with a simple strategy and records
 * screenshots of key moments (boss fight, game over, new run preparation).
 * Usage: Play outDir [height] [runs]
 */
public final class Play {
    public static void main(String[] args) throws Exception {
        Shots.out = args.length > 0 ? args[0] : "out";
        int h = args.length > 1 ? Integer.parseInt(args[1]) : 520;
        int runs = args.length > 2 ? Integer.parseInt(args[2]) : 2;
        Shots.FakePlatform pf = new Shots.FakePlatform();
        Shots.game = new Game(pf, h);
        Game game = Shots.game;
        game.profile.created = true;
        Shots.step(300);
        Shots.tap(2); // new game
        Shots.step(800);
        int gameOvers = 0;
        boolean bossShot = false, bossIntroShot = false, prepShot = false, replaceShot = false, seerShot = false;
        int lastStage = 0;
        long actions = 0;
        while (gameOvers < runs && actions < 200000) {
            actions++;
            String t = Shots.top();
            Run run = game.profile.run;
            if (run != null && run.stage != lastStage) {
                lastStage = run.stage;
            }
            switch (t) {
                case "MapScreen":
                    Shots.tap(1);
                    Shots.step(900);
                    break;
                case "ArsenalScreen":
                    if (!prepShot) { Shots.step(200); Shots.shot("prep_arsenal"); prepShot = true; }
                    Shots.tap(2); // auto
                    Shots.tap(1); // los
                    Shots.step(400);
                    break;
                case "BattleScreen": {
                    Fighter en = null;
                    if (game.ui.find(1) != null) {
                        // choose the weapon with the best matchup
                        run = game.profile.run;
                        boolean boss = Enemies.isBoss(run.stage);
                        if (boss && !bossShot) { Shots.shot("boss_choose_stage" + run.stage); bossShot = true; }
                        int best = 1;
                        double bestScore = -1e9;
                        Fighter me = run.player;
                        Fighter e = run.createEnemy();
                        for (int i = 0; i < me.weapons.size(); i++) {
                            if (!me.weapons.get(i).available()) continue; // used special
                            int[] m = Combat.matchup(me.weapons.get(i), e.weapons);
                            double s = m[0] * (1 + me.weapons.get(i).stats().atk / 10.0) - m[2] + Math.random() * 0.2;
                            if (s > bestScore) { bestScore = s; best = i + 1; }
                        }
                        if (!seerShot && me.passive().seer > 0) { Shots.shot("seer_battle"); seerShot = true; }
                        Shots.tap(best);
                        Shots.step(300);
                    } else {
                        if (!bossIntroShot && run != null && Enemies.isBoss(run.stage)) {
                            Shots.step(900);
                            Shots.shot("boss_intro");
                            bossIntroShot = true;
                        }
                        Shots.tapAt(120, game.H / 2);
                        Shots.step(150);
                    }
                    break;
                }
                case "RewardScreen":
                    Shots.step(600);
                    if (game.ui.find(1) != null) { // closed box
                        Shots.tap(1);
                        Shots.step(2300);
                    } else if (game.ui.find(2) != null) { // weapon shown: into the arsenal
                        Shots.tap(2);
                        Shots.step(800);
                    } else if (game.ui.find(4) != null) { // cards: auto-equip, continue
                        if (game.ui.find(5) != null) Shots.tap(5);
                        Shots.tap(4);
                        Shots.step(400);
                    }
                    break;
                case "EquipScreen":
                    Shots.tap(100 + 3 * 10);
                    Shots.step(300);
                    if (Shots.top().equals("EquipScreen")) game.back();
                    break;
                case "ReplaceScreen":
                    if (!replaceShot) { Shots.shot("replace"); replaceShot = true; }
                    Shots.tap(100 + new java.util.Random().nextInt(3));
                    Shots.step(900);
                    break;
                case "DialogScreen":
                    Shots.tap(0);
                    Shots.step(300);
                    break;
                case "GameOverScreen":
                    Shots.step(1200);
                    Shots.shot("gameover_" + gameOvers);
                    System.out.println("run " + gameOvers + " ended at stage " + game.profile.bestStage + " cards=" + game.profile.totalCards()
                            + " owned=" + game.profile.ownedCount() + " seen=" + game.profile.seenCount());
                    gameOvers++;
                    Shots.tap(1); // new run
                    Shots.step(900);
                    break;
                default:
                    System.out.println("unexpected screen " + t);
                    game.back();
                    Shots.step(300);
            }
        }
        System.out.println("actions=" + actions + " best=" + game.profile.bestStage + " runs=" + game.profile.runs);
        Profile back = Profile.decode(pf.data);
        System.out.println("save ok: " + (back.totalCards() == game.profile.totalCards()));
    }
}
