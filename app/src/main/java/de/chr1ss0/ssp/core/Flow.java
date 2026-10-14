package de.chr1ss0.ssp.core;

/** Navigation helpers shared by several screens. */
public final class Flow {

    public static void continueRun(Game game) {
        Run run = game.profile.run;
        if (run == null) {
            newRun(game);
            return;
        }
        run.syncPlayer(game.profile);
        if (run.phase == Run.PHASE_REWARD && (run.rewardBox >= 0 || run.pendingCards.length > 0)) {
            game.set(new RewardScreen(game), Game.TR_FADE);
        } else {
            game.set(new MapScreen(game), Game.TR_FADE);
        }
    }

    public static void newRun(Game game) {
        Profile p = game.profile;
        RunLogic.newRun(p, game.rng);
        p.run.syncPlayer(p);
        game.save();
        game.set(new MapScreen(game), Game.TR_FADE);
    }

    public static void startBattle(Game game) {
        game.set(new BattleScreen(game), Game.TR_STRIPES);
    }

    public static void toTitle(Game game) {
        game.save();
        game.set(new TitleScreen(game), Game.TR_FADE);
    }

    private Flow() {}
}
