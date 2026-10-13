package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.Gfx;
import de.chr1ss0.ssp.core.Sprite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Writes framebuffers / sprites to PNG files (desktop only). */
public final class Png {
    public static void write(Gfx g, int scale, String path) throws IOException {
        write(g.px, g.w, g.h, scale, path, false);
    }

    public static void write(Sprite s, int scale, String path, boolean alpha) throws IOException {
        write(s.px, s.w, s.h, scale, path, alpha);
    }

    public static void write(int[] px, int w, int h, int scale, String path, boolean alpha) throws IOException {
        BufferedImage img = new BufferedImage(w * scale, h * scale, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h * scale; y++)
            for (int x = 0; x < w * scale; x++)
                img.setRGB(x, y, px[(y / scale) * w + x / scale]);
        File f = new File(path);
        if (f.getParentFile() != null) f.getParentFile().mkdirs();
        ImageIO.write(img, "png", f);
    }

    private Png() {}
}
