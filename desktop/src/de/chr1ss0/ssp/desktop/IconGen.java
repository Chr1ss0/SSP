package de.chr1ss0.ssp.desktop;

import de.chr1ss0.ssp.core.*;

/** Generates the launcher icons from the game's own pixel art. Usage: IconGen resDir */
public final class IconGen {
    public static void main(String[] args) throws Exception {
        String res = args.length > 0 ? args[0] : "app/src/main/res";
        int bg = 0xFFF8D048;

        // legacy icon 48x48
        Sprite legacy = new Sprite(48, 48);
        Gfx lg = new Gfx(48, 48);
        lg.clear(0);
        Ui.panel(lg, 1, 1, 46, 46, bg, Pal.OUTLINE);
        lg.fill(2, 40, 44, 6, 0xFFE8B830);
        lg.sprite(WeaponArt.icon(WeaponDef.SCHERE_ID), 16, 5);
        lg.sprite(WeaponArt.icon(WeaponDef.STEIN_ID), 5, 25);
        lg.sprite(WeaponArt.icon(WeaponDef.PAPIER_ID), 27, 25);
        // rounded corners
        int[] corners = {0, 0, 47, 0, 0, 47, 47, 47};
        for (int i = 0; i < 8; i += 2) lg.px[corners[i + 1] * 48 + corners[i]] = 0;
        System.arraycopy(lg.px, 0, legacy.px, 0, legacy.px.length);

        String[] dirs = {"mipmap-mdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi"};
        int[] scales = {1, 2, 3, 4};
        for (int i = 0; i < dirs.length; i++) {
            Png.write(legacy, scales[i], res + "/" + dirs[i] + "/ic_launcher.png", true);
        }

        // adaptive foreground 108x108 (safe zone: central 66)
        Gfx fg = new Gfx(108, 108);
        fg.clear(0);
        fg.sprite(WeaponArt.icon(WeaponDef.SCHERE_ID), 38, 22, 2);
        fg.sprite(WeaponArt.icon(WeaponDef.STEIN_ID), 21, 52, 2);
        fg.sprite(WeaponArt.icon(WeaponDef.PAPIER_ID), 55, 52, 2);
        Sprite fgs = new Sprite(108, 108, fg.px.clone());
        for (int i = 0; i < dirs.length; i++) {
            Png.write(fgs, scales[i], res + "/" + dirs[i] + "/ic_launcher_foreground.png", true);
        }
        Png.write(legacy, 8, "build/icon_preview.png", true);
        System.out.println("icons written to " + res);
    }
}
