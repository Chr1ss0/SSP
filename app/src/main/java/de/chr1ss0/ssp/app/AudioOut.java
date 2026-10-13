package de.chr1ss0.ssp.app;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import de.chr1ss0.ssp.core.Sound;

/** Streams the synthesizer output to an AudioTrack on a background thread. */
final class AudioOut extends Thread {
    private final Sound sound;
    private volatile boolean alive = true;
    private volatile boolean paused;
    private AudioTrack track;

    AudioOut(Sound sound) {
        super("ssp-audio");
        this.sound = sound;
        setDaemon(true);
    }

    void setPaused(boolean p) {
        paused = p;
        synchronized (this) {
            notifyAll();
        }
    }

    void shutdown() {
        alive = false;
        setPaused(false);
        interrupt();
    }

    @Override
    public void run() {
        int min = AudioTrack.getMinBufferSize(Sound.RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int bufBytes = Math.max(min, 4096);
        try {
            track = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setSampleRate(Sound.RATE)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(bufBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
        } catch (RuntimeException e) {
            android.util.Log.e("SSP", "audio init failed", e);
            return;
        }
        short[] buf = new short[512];
        try {
            track.play();
            while (alive) {
                if (paused) {
                    track.pause();
                    synchronized (this) {
                        while (paused && alive) {
                            try {
                                wait(500);
                            } catch (InterruptedException ignored) {
                                // re-check state
                            }
                        }
                    }
                    if (!alive) break;
                    track.play();
                }
                sound.render(buf, buf.length);
                int written = track.write(buf, 0, buf.length);
                if (written < 0) break;
            }
        } catch (RuntimeException e) {
            android.util.Log.e("SSP", "audio failed", e);
        } finally {
            try {
                track.stop();
            } catch (RuntimeException ignored) {
            }
            track.release();
        }
    }
}
