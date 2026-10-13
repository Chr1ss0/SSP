package de.chr1ss0.ssp.core;

/**
 * Tiny Game-Boy-like synthesizer: 2 pulse channels, 1 triangle, 1 noise for
 * music (driven by an MML sequencer) plus 2 voices for sound effects.
 * The platform audio thread pulls PCM via {@link #render(short[], int)}.
 */
public final class Sound {
    public static final int RATE = 22050;

    // music tracks
    public static final int M_TITLE = 0, M_MAP = 1, M_BATTLE = 2, M_BOSS = 3, M_VICTORY = 4, M_GAMEOVER = 5;

    // sound effects
    public static final int S_CURSOR = 0, S_SELECT = 1, S_BACK = 2, S_HIT = 3, S_CRIT = 4, S_DRAW = 5, S_HEAL = 6,
            S_FAINT = 7, S_TICK = 8, S_REVEAL = 9, S_CARD = 10, S_BOX_SHAKE = 11, S_BOX_OPEN = 12, S_LEVELUP = 13,
            S_ERROR = 14, S_BLOCK = 15, S_ATK = 16; // S_ATK + type (9 sounds)

    private static final float[] VOL = {0f, 0.35f, 0.65f, 1f};

    // ------------------------------------------------------------ voices

    private static final class Voice {
        int wave; // 0..3 pulse duty, 4 noise, 5 triangle, 6 short noise
        float freq, phase, vol;
        float env = 1f, decay, sustain = 1f, envT;
        boolean on;
        int lfsr = 0x7FFF;
        float noiseAcc;
        float out;

        float sample() {
            if (!on && vol <= 0) return 0;
            float v;
            switch (wave) {
                case 4:
                case 6: {
                    float rate = Math.min(RATE, freq);
                    noiseAcc += rate / RATE;
                    while (noiseAcc >= 1f) {
                        noiseAcc -= 1f;
                        int bit = (lfsr ^ (lfsr >> 1)) & 1;
                        lfsr = (lfsr >> 1) | (bit << 14);
                        if (wave == 6) lfsr = (lfsr & ~(1 << 6)) | (bit << 6);
                        out = (lfsr & 1) == 0 ? 1f : -1f;
                    }
                    v = out;
                    break;
                }
                case 5:
                    phase += freq / RATE;
                    if (phase >= 1f) phase -= (int) phase;
                    v = phase < 0.5f ? (phase * 4f - 1f) : (3f - phase * 4f);
                    // 4-bit quantisation like the GB wave channel
                    v = Math.round(v * 7.5f) / 7.5f;
                    break;
                default: {
                    phase += freq / RATE;
                    if (phase >= 1f) phase -= (int) phase;
                    float duty = wave == 0 ? 0.125f : wave == 1 ? 0.25f : wave == 2 ? 0.5f : 0.75f;
                    v = phase < duty ? 1f : -1f;
                }
            }
            return v * vol * env;
        }

        void stepEnv() {
            envT += 1f / RATE;
            if (decay > 0) {
                float e = (float) Math.exp(-envT / decay);
                env = sustain + (1f - sustain) * e;
            } else env = 1f;
            if (!on) {
                vol *= 0.995f; // release
                if (vol < 0.001f) vol = 0;
            }
        }
    }

    // ------------------------------------------------------------ music state

    private final Voice[] mus = new Voice[4];
    private final Voice[] fx = new Voice[2];
    private Mml.Track track;
    private int trackId = -1;
    private double tick;
    private double ticksPerSample;
    private final int[] evIdx = new int[4];
    private final double[] noteOff = new double[4];
    private float musicGain = 0.65f, sfxGain = 1f;
    private boolean paused;
    private float lp;

    // sfx sequencing
    private static final class SfxPlay {
        float[][] segs;
        int seg = -1;
        float segT;
    }

    private final SfxPlay[] fxPlay = {new SfxPlay(), new SfxPlay()};
    private int nextFx;

    public Sound() {
        for (int i = 0; i < 4; i++) mus[i] = new Voice();
        for (int i = 0; i < 2; i++) fx[i] = new Voice();
    }

    public synchronized void setVolumes(int music, int sfx) {
        musicGain = VOL[Math.max(0, Math.min(3, music))];
        sfxGain = VOL[Math.max(0, Math.min(3, sfx))];
    }

    public synchronized void pause(boolean p) {
        paused = p;
    }

    public synchronized int currentMusic() {
        return trackId;
    }

    public synchronized void music(int id) {
        if (id == trackId) return;
        trackId = id;
        if (id < 0) {
            track = null;
            for (Voice v : mus) { v.on = false; v.vol = 0; }
            return;
        }
        track = Music.track(id);
        tick = 0;
        ticksPerSample = track.tempo / 60.0 * Mml.QUARTER / RATE;
        for (int c = 0; c < 4; c++) {
            evIdx[c] = 0;
            noteOff[c] = -1;
            mus[c].on = false;
            mus[c].vol = 0;
        }
    }

    public synchronized void sfx(int id) {
        float[][] segs = Sfx.get(id);
        if (segs == null) return;
        int v = nextFx;
        nextFx = (nextFx + 1) % fx.length;
        // prefer an idle voice
        for (int i = 0; i < fx.length; i++) if (fxPlay[i].seg < 0) { v = i; break; }
        SfxPlay p = fxPlay[v];
        p.segs = segs;
        p.seg = 0;
        p.segT = 0;
        startSeg(v);
    }

    private void startSeg(int v) {
        SfxPlay p = fxPlay[v];
        float[] s = p.segs[p.seg];
        Voice vo = fx[v];
        vo.wave = (int) s[3];
        vo.on = true;
        vo.env = 1f;
        vo.decay = 0;
        vo.envT = 0;
        vo.freq = s[1];
        vo.vol = s[4];
    }

    // ------------------------------------------------------------ render

    public synchronized void render(short[] buf, int n) {
        if (paused) {
            java.util.Arrays.fill(buf, 0, n, (short) 0);
            return;
        }
        for (int i = 0; i < n; i++) {
            if (track != null) advanceMusic();
            float m = 0;
            for (int c = 0; c < 4; c++) {
                Voice v = mus[c];
                m += v.sample() * (c == 2 ? 0.30f : (c == 3 ? 0.16f : 0.20f));
                v.stepEnv();
            }
            float s = 0;
            for (int c = 0; c < fx.length; c++) {
                SfxPlay p = fxPlay[c];
                Voice v = fx[c];
                if (p.seg >= 0) {
                    float[] seg = p.segs[p.seg];
                    float t = p.segT / seg[0];
                    v.freq = seg[1] + (seg[2] - seg[1]) * t;
                    v.vol = seg[4] + (seg[5] - seg[4]) * t;
                    p.segT += 1000f / RATE;
                    if (p.segT >= seg[0]) {
                        p.seg++;
                        p.segT = 0;
                        if (p.seg >= p.segs.length) {
                            p.seg = -1;
                            v.on = false;
                            v.vol = 0;
                        } else startSeg(c);
                    }
                    s += v.sample() * 0.28f;
                }
            }
            float out = m * musicGain + s * sfxGain;
            lp += (out - lp) * 0.6f;
            out = lp;
            if (out > 1f) out = 1f;
            if (out < -1f) out = -1f;
            buf[i] = (short) (out * 30000);
        }
    }

    private void advanceMusic() {
        Mml.Track t = track;
        for (int c = 0; c < 4; c++) {
            Mml.Note[] notes = t.channels[c];
            Voice v = mus[c];
            if (noteOff[c] >= 0 && tick >= noteOff[c]) {
                v.on = false;
                noteOff[c] = -1;
            }
            while (evIdx[c] < notes.length && notes[evIdx[c]].start <= tick) {
                Mml.Note nt = notes[evIdx[c]++];
                if (nt.pitch < 0) {
                    v.on = false;
                    continue;
                }
                v.wave = c == 2 ? 5 : (c == 3 ? (nt.duty == 1 ? 6 : 4) : nt.duty);
                v.freq = c == 3 ? Mml.noiseRate(nt.pitch) : Mml.freq(nt.pitch);
                v.vol = nt.vol / 15f * (c == 3 ? Mml.noiseGain(nt.pitch) : 1f);
                v.on = true;
                v.env = 1f;
                v.envT = 0;
                v.decay = (float) (c == 3 ? Mml.noiseDecay(nt.pitch) : Mml.decayTime(nt.decay));
                v.sustain = c == 3 ? 0f : nt.sustain / 8f;
                noteOff[c] = nt.start + nt.len * nt.gate / 8.0;
            }
        }
        tick += ticksPerSample;
        if (tick >= t.length) {
            if (t.loopTick < 0) {
                // one-shot jingle: silence afterwards
                track = null;
                for (Voice v : mus) v.on = false;
                return;
            }
            tick = t.loopTick + (tick - t.length);
            for (int c = 0; c < 4; c++) {
                Mml.Note[] notes = t.channels[c];
                int i = 0;
                while (i < notes.length && notes[i].start < t.loopTick) i++;
                evIdx[c] = i;
            }
        }
    }
}
