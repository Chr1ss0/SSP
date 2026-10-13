package de.chr1ss0.ssp.core;

import java.util.ArrayList;

/**
 * Minimal Music Macro Language parser.
 * Commands: o4 &lt; &gt; l8 v12 @2 (duty) q7 (gate /8) k3 (decay) s4 (sustain /8)
 * L (loop point) [..]n (repeat) notes c d e f g a b with + # - and lengths/dots, ^ tie, r rest, | ignored.
 */
public final class Mml {
    public static final int QUARTER = 48;
    public static final int WHOLE = QUARTER * 4;

    public static final class Note {
        public double start;
        public int len, pitch, vol, duty, gate, decay, sustain;
    }

    public static final class Track {
        public final Note[][] channels = new Note[4][];
        public double length;
        public double loopTick = -1;
        public double tempo;
        public final double[] channelLength = new double[4];
    }

    private static final double[] DECAY = {0, 0.03, 0.06, 0.12, 0.25, 0.5, 1.0, 2.0};

    public static double decayTime(int d) {
        return DECAY[Math.max(0, Math.min(DECAY.length - 1, d))];
    }

    public static float freq(int pitch) {
        return (float) (440.0 * Math.pow(2, (pitch - 69) / 12.0));
    }

    /** Drum kit on the noise channel, selected by pitch class. */
    public static float noiseRate(int pitch) {
        switch (pitch % 12) {
            case 0: return 900f;     // c kick
            case 2: return 7000f;    // d snare
            case 4: return 22050f;   // e closed hat
            case 5: return 16000f;   // f open hat
            case 7: return 11000f;   // g crash
            default: return 4000f;
        }
    }

    public static float noiseGain(int pitch) {
        switch (pitch % 12) {
            case 0: return 1.0f;
            case 2: return 0.85f;
            case 4: return 0.4f;
            case 5: return 0.45f;
            case 7: return 0.55f;
            default: return 0.7f;
        }
    }

    public static double noiseDecay(int pitch) {
        switch (pitch % 12) {
            case 0: return 0.07;
            case 2: return 0.09;
            case 4: return 0.025;
            case 5: return 0.12;
            case 7: return 0.45;
            default: return 0.1;
        }
    }

    public static Track parse(double tempo, String... ch) {
        Track t = new Track();
        t.tempo = tempo;
        for (int c = 0; c < 4; c++) {
            double[] info = new double[]{0, -1};
            t.channels[c] = parseChannel(c < ch.length ? ch[c] : "", info);
            t.channelLength[c] = info[0];
            t.length = Math.max(t.length, info[0]);
            if (c == 0) t.loopTick = info[1];
        }
        return t;
    }

    static String expand(String s) {
        while (true) {
            int close = s.indexOf(']');
            if (close < 0) return s;
            int open = s.lastIndexOf('[', close);
            if (open < 0) throw new IllegalArgumentException("unbalanced ] in MML");
            int i = close + 1;
            int n = 0;
            while (i < s.length() && Character.isDigit(s.charAt(i))) n = n * 10 + (s.charAt(i++) - '0');
            if (n == 0) n = 2;
            String body = s.substring(open + 1, close);
            StringBuilder sb = new StringBuilder();
            sb.append(s, 0, open);
            for (int k = 0; k < n; k++) sb.append(body).append(' ');
            sb.append(s.substring(i));
            s = sb.toString();
        }
    }

    private static final int[] SEMI = {9, 11, 0, 2, 4, 5, 7}; // a b c d e f g

    private static Note[] parseChannel(String src, double[] info) {
        String s = expand(src);
        ArrayList<Note> out = new ArrayList<Note>();
        int octave = 4, deflen = 8, vol = 12, duty = 2, gate = 7, decay = 0, sustain = 8;
        double tick = 0;
        int[] pos = {0};
        int n = s.length();
        while (pos[0] < n) {
            char c = s.charAt(pos[0]++);
            switch (c) {
                case ' ': case '\n': case '\t': case '|': break;
                case 'o': octave = num(s, pos, octave); break;
                case '<': octave--; break;
                case '>': octave++; break;
                case 'l': deflen = num(s, pos, deflen); break;
                case 'v': vol = num(s, pos, vol); break;
                case '@': duty = num(s, pos, duty); break;
                case 'q': gate = num(s, pos, gate); break;
                case 'k': decay = num(s, pos, decay); break;
                case 's': sustain = num(s, pos, sustain); break;
                case 't': num(s, pos, 120); break;
                case 'L': info[1] = tick; break;
                case 'r': {
                    int len = length(s, pos, deflen);
                    Note nt = new Note();
                    nt.start = tick;
                    nt.len = len;
                    nt.pitch = -1;
                    out.add(nt);
                    tick += len;
                    break;
                }
                default:
                    if (c >= 'a' && c <= 'g') {
                        int semi = SEMI[c - 'a'];
                        while (pos[0] < n) {
                            char a = s.charAt(pos[0]);
                            if (a == '+' || a == '#') { semi++; pos[0]++; }
                            else if (a == '-') { semi--; pos[0]++; }
                            else break;
                        }
                        int len = length(s, pos, deflen);
                        Note nt = new Note();
                        nt.start = tick;
                        nt.len = len;
                        nt.pitch = 12 * (octave + 1) + semi;
                        nt.vol = vol;
                        nt.duty = duty;
                        nt.gate = gate;
                        nt.decay = decay;
                        nt.sustain = sustain;
                        out.add(nt);
                        tick += len;
                    } else {
                        throw new IllegalArgumentException("bad MML char '" + c + "' at " + (pos[0] - 1));
                    }
            }
        }
        info[0] = tick;
        return out.toArray(new Note[0]);
    }

    private static int num(String s, int[] pos, int def) {
        int i = pos[0], v = 0;
        boolean any = false;
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            v = v * 10 + (s.charAt(i) - '0');
            i++;
            any = true;
        }
        pos[0] = i;
        return any ? v : def;
    }

    private static int length(String s, int[] pos, int deflen) {
        int den = num(s, pos, deflen);
        int len = WHOLE / den;
        int add = len;
        while (pos[0] < s.length() && s.charAt(pos[0]) == '.') {
            add /= 2;
            len += add;
            pos[0]++;
        }
        while (pos[0] < s.length() && s.charAt(pos[0]) == '^') {
            pos[0]++;
            len += length(s, pos, deflen);
        }
        return len;
    }

    private Mml() {}
}
