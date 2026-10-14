package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.*;

/** Generates the launcher icons from the game's own HD pixel art. Usage: IconGen resDir */
public final class IconGen {
    public static void main(String[] args) throws Exception {
        String res = args.length > 0 ? args[0] : "app/src/main/res";
        // legacy icon: 48 logical px rendered at 4x (192 px)
        Gfx lg = new Gfx(48, 48, 4);
        lg.clear(0);
        lg.roundRectGrad(1, 1, 46, 46, 10, 0xFF6F61FF, 0xFFFF8C7A);
        lg.roundBorder(1, 1, 46, 46, 10, 2, 0xFF2A2350);
        lg.spriteHD(WeaponArt.icon(WeaponDef.SCHERE_ID), 15, 5, 2);
        lg.spriteHD(WeaponArt.icon(WeaponDef.STEIN_ID), 4, 24, 2);
        lg.spriteHD(WeaponArt.icon(WeaponDef.PAPIER_ID), 26, 24, 2);
        Sprite big = new Sprite(lg.pw, lg.ph, lg.px.clone());
        String[] dirs = {"mipmap-mdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi"};
        int[] sizes = {48, 96, 144, 192};
        for (int i = 0; i < dirs.length; i++) {
            Sprite s = sizes[i] == 192 ? big : Shade.downscale(big, sizes[i], sizes[i]);
            Png.write(s, 1, res + "/" + dirs[i] + "/ic_launcher.png", true);
        }
        // adaptive foreground: 108 logical px at 4x (432 px), content in the central 66
        Gfx fg = new Gfx(108, 108, 4);
        fg.clear(0);
        fg.spriteHD(WeaponArt.icon(WeaponDef.SCHERE_ID), 38, 22, 4);
        fg.spriteHD(WeaponArt.icon(WeaponDef.STEIN_ID), 21, 52, 4);
        fg.spriteHD(WeaponArt.icon(WeaponDef.PAPIER_ID), 55, 52, 4);
        Sprite fgs = new Sprite(fg.pw, fg.ph, fg.px.clone());
        int[] fsizes = {108, 216, 324, 432};
        for (int i = 0; i < dirs.length; i++) {
            Sprite s = fsizes[i] == 432 ? fgs : Shade.downscale(fgs, fsizes[i], fsizes[i]);
            Png.write(s, 1, res + "/" + dirs[i] + "/ic_launcher_foreground.png", true);
        }
        Png.write(big, 2, "build/icon_preview.png", true);
        System.out.println("icons written to " + res);
    }
}
