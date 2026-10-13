package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.*;

import java.io.FileOutputStream;
import java.io.IOException;

/** Verifies the MML tracks (equal channel lengths) and renders WAV files. */
public final class AudioCheck {
    public static void main(String[] args) throws IOException {
        String out = args.length > 0 ? args[0] : "out";
        String[] names = {"title", "map", "battle", "boss", "victory", "gameover"};
        boolean ok = true;
        for (int id = 0; id < names.length; id++) {
            Mml.Track t = Music.track(id);
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < 4; c++) sb.append(" ch").append(c).append('=').append((int) t.channelLength[c]);
            boolean eq = true;
            for (int c = 1; c < 4; c++) if (t.channelLength[c] != t.channelLength[0]) eq = false;
            System.out.println(names[id] + ": bars=" + (t.length / Mml.WHOLE) + " loop=" + (t.loopTick / Mml.WHOLE) + sb + (eq ? " OK" : " MISMATCH"));
            ok &= eq;
            Sound s = new Sound();
            s.setVolumes(3, 3);
            s.music(id);
            int seconds = (int) Math.min(40, t.length / (t.tempo / 60.0 * Mml.QUARTER) + 4);
            short[] pcm = new short[Sound.RATE * seconds];
            short[] buf = new short[512];
            int peak = 0;
            for (int i = 0; i < pcm.length; i += buf.length) {
                int n = Math.min(buf.length, pcm.length - i);
                s.render(buf, n);
                System.arraycopy(buf, 0, pcm, i, n);
                for (int k = 0; k < n; k++) peak = Math.max(peak, Math.abs(buf[k]));
            }
            System.out.println("   peak=" + peak);
            wav(out + "/" + names[id] + ".wav", pcm);
        }
        // sfx
        Sound s = new Sound();
        s.setVolumes(0, 3);
        short[] pcm = new short[Sound.RATE * 14];
        short[] buf = new short[512];
        int pos = 0;
        for (int id = 0; id < Sound.S_ATK + Element.COUNT; id++) {
            s.sfx(id);
            for (int k = 0; k < 22 && pos < pcm.length; k++) {
                int n = Math.min(buf.length, pcm.length - pos);
                s.render(buf, n);
                System.arraycopy(buf, 0, pcm, pos, n);
                pos += n;
            }
        }
        wav(out + "/sfx.wav", pcm);
        System.out.println(ok ? "ALL TRACKS OK" : "TRACK LENGTH MISMATCH");
    }

    static void wav(String path, short[] pcm) throws IOException {
        new java.io.File(path).getParentFile().mkdirs();
        FileOutputStream f = new FileOutputStream(path);
        int dataLen = pcm.length * 2;
        byte[] h = new byte[44];
        put(h, 0, "RIFF"); le32(h, 4, 36 + dataLen); put(h, 8, "WAVE"); put(h, 12, "fmt ");
        le32(h, 16, 16); le16(h, 20, 1); le16(h, 22, 1); le32(h, 24, Sound.RATE); le32(h, 28, Sound.RATE * 2);
        le16(h, 32, 2); le16(h, 34, 16); put(h, 36, "data"); le32(h, 40, dataLen);
        f.write(h);
        byte[] d = new byte[dataLen];
        for (int i = 0; i < pcm.length; i++) { d[i * 2] = (byte) pcm[i]; d[i * 2 + 1] = (byte) (pcm[i] >> 8); }
        f.write(d);
        f.close();
    }

    static void put(byte[] b, int o, String s) { for (int i = 0; i < 4; i++) b[o + i] = (byte) s.charAt(i); }
    static void le32(byte[] b, int o, int v) { for (int i = 0; i < 4; i++) b[o + i] = (byte) (v >> (8 * i)); }
    static void le16(byte[] b, int o, int v) { b[o] = (byte) v; b[o + 1] = (byte) (v >> 8); }
}
