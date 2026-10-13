package de.chr1ss0.ssp.core;

/**
 * Sound effect definitions. Each effect is a list of segments:
 * {duration ms, start freq, end freq, wave (0-3 pulse duty, 4 noise, 5 triangle, 6 metallic noise), start vol, end vol}.
 */
public final class Sfx {
    private static final float[][][] FX = new float[Sound.S_ATK + Element.COUNT][][];

    private static float[] s(float ms, float f0, float f1, int wave, float v0, float v1) {
        return new float[]{ms, f0, f1, wave, v0, v1};
    }

    static {
        FX[Sound.S_CURSOR] = new float[][]{s(28, 1400, 1400, 1, .45f, .3f)};
        FX[Sound.S_SELECT] = new float[][]{s(40, 988, 988, 1, .5f, .5f), s(70, 1480, 1480, 1, .5f, 0)};
        FX[Sound.S_BACK] = new float[][]{s(40, 740, 740, 1, .5f, .5f), s(70, 494, 494, 1, .5f, 0)};
        FX[Sound.S_HIT] = new float[][]{s(40, 6000, 4000, 4, 1f, .8f), s(140, 2500, 600, 4, .8f, 0)};
        FX[Sound.S_CRIT] = new float[][]{s(50, 9000, 9000, 4, 1f, 1f), s(60, 1800, 900, 3, .7f, .6f), s(180, 3000, 300, 4, 1f, 0)};
        FX[Sound.S_DRAW] = new float[][]{s(30, 1700, 1700, 6, .7f, .5f), s(50, 1300, 1300, 3, .45f, .3f), s(90, 1300, 1250, 3, .3f, 0)};
        FX[Sound.S_HEAL] = new float[][]{s(55, 523, 523, 2, .45f, .45f), s(55, 659, 659, 2, .45f, .45f),
                s(55, 784, 784, 2, .45f, .45f), s(160, 1047, 1047, 2, .45f, 0)};
        FX[Sound.S_FAINT] = new float[][]{s(650, 900, 90, 2, .6f, 0)};
        FX[Sound.S_TICK] = new float[][]{s(45, 9000, 7000, 4, .6f, 0)};
        FX[Sound.S_REVEAL] = new float[][]{s(50, 660, 660, 1, .5f, .5f), s(90, 1320, 1320, 1, .5f, 0)};
        FX[Sound.S_CARD] = new float[][]{s(45, 1047, 1047, 1, .45f, .45f), s(45, 1319, 1319, 1, .45f, .45f),
                s(45, 1568, 1568, 1, .45f, .45f), s(180, 2093, 2093, 1, .45f, 0)};
        FX[Sound.S_BOX_SHAKE] = new float[][]{s(90, 1200, 700, 4, .7f, 0)};
        FX[Sound.S_BOX_OPEN] = new float[][]{s(90, 523, 523, 2, .5f, .5f), s(90, 659, 659, 2, .5f, .5f),
                s(90, 784, 784, 2, .5f, .5f), s(110, 1047, 1047, 2, .55f, .55f), s(260, 1568, 1568, 1, .5f, 0)};
        FX[Sound.S_LEVELUP] = new float[][]{s(70, 784, 784, 2, .5f, .5f), s(70, 988, 988, 2, .5f, .5f),
                s(70, 1175, 1175, 2, .5f, .5f), s(240, 1568, 1568, 2, .5f, 0)};
        FX[Sound.S_ERROR] = new float[][]{s(90, 220, 220, 2, .5f, .5f), s(120, 165, 165, 2, .5f, 0)};
        FX[Sound.S_BLOCK] = new float[][]{s(30, 2500, 2500, 6, .8f, .6f), s(160, 900, 700, 3, .5f, 0)};
        int a = Sound.S_ATK;
        FX[a + Element.STEIN] = new float[][]{s(60, 1500, 700, 4, 1f, .9f), s(220, 700, 150, 4, .9f, 0)};
        FX[a + Element.KLINGE] = new float[][]{s(160, 20000, 4000, 4, .7f, 0), s(60, 2400, 2400, 0, .3f, 0)};
        FX[a + Element.PAPIER] = new float[][]{s(220, 2000, 6000, 4, .2f, .6f), s(80, 6000, 3000, 4, .6f, 0)};
        FX[a + Element.FEUER] = new float[][]{s(300, 3500, 1200, 4, .8f, 0), s(80, 900, 300, 4, .5f, 0)};
        FX[a + Element.WASSER] = new float[][]{s(45, 300, 900, 2, .5f, .4f), s(45, 400, 1000, 2, .5f, .4f),
                s(45, 500, 1200, 2, .5f, .4f), s(70, 600, 1500, 2, .4f, 0)};
        FX[a + Element.BLITZ] = new float[][]{s(40, 3000, 3000, 6, .8f, .8f), s(220, 1800, 80, 3, .7f, 0), s(80, 6000, 2000, 4, .6f, 0)};
        FX[a + Element.PFLANZE] = new float[][]{s(90, 200, 500, 1, .5f, .4f), s(120, 300, 700, 1, .5f, 0), s(80, 4000, 2000, 4, .3f, 0)};
        FX[a + Element.STICH] = new float[][]{s(60, 1600, 3200, 0, .6f, .5f), s(120, 3200, 3000, 0, .5f, 0)};
        FX[a + Element.EIS] = new float[][]{s(60, 2093, 2093, 1, .45f, .4f), s(60, 2637, 2637, 1, .45f, .4f), s(200, 3136, 3136, 1, .45f, 0)};
    }

    public static float[][] get(int id) {
        if (id < 0 || id >= FX.length) return null;
        return FX[id];
    }

    private Sfx() {}
}
