package de.chr1ss0.ssp.core;

import java.util.Random;

/** Victory reward: open a loot box that contains a new weapon. */
public final class RewardScreen extends Screen {
    private static final int B_BOX = 1, B_TAKE = 2, B_LEAVE = 3;
    private static final int S_CLOSED = 0, S_SHAKE = 1, S_OPEN = 2, S_SHOW = 3;

    private final int healed;
    private int st = S_CLOSED;
    private float stT;
    private int weaponId = -1;
    private int tier;

    public RewardScreen(Game game) {
        this(game, 0);
    }

    public RewardScreen(Game game, int healed) {
        super(game);
        this.healed = healed;
    }

    @Override
    public int music() {
        return Sound.M_VICTORY;
    }

    @Override
    public void enter() {
        Run run = game.profile.run;
        if (run == null) return;
        tier = Math.max(0, run.rewardBox);
        if (weaponId < 0) {
            Random r = new Random(run.seed * 13 + run.stage * 101L);
            weaponId = Cards.randomWeapon(r, tier, run.player);
        }
        // returning from the replace screen after taking the weapon
        if (st == S_SHOW && run.phase == Run.PHASE_MAP) finish();
    }

    private void finish() {
        Run run = game.profile.run;
        run.phase = Run.PHASE_MAP;
        run.rewardBox = -1;
        game.save();
        game.set(new MapScreen(game), Game.TR_FADE);
    }

    @Override
    public void update(float dt) {
        Run run = game.profile.run;
        if (run == null) {
            Flow.toTitle(game);
            return;
        }
        stT += dt;
        int c = ui.click();
        switch (st) {
            case S_CLOSED:
                if (c == B_BOX) {
                    st = S_SHAKE;
                    stT = 0;
                }
                break;
            case S_SHAKE:
                if (((int) (stT / 300)) != ((int) ((stT - dt) / 300)) && stT < 900) game.sound.sfx(Sound.S_BOX_SHAKE);
                if (stT > 950) {
                    st = S_OPEN;
                    stT = 0;
                    game.sound.sfx(Sound.S_BOX_OPEN);
                    game.flash(150);
                    game.vibrate(60);
                    Profile p = game.profile;
                    p.seen[weaponId] = true;
                    p.owned[weaponId] = true;
                    game.save();
                }
                break;
            case S_OPEN:
                if (stT > 900) {
                    st = S_SHOW;
                    stT = 0;
                }
                break;
            default:
                if (c == B_TAKE) {
                    game.sound.sfx(Sound.S_LEVELUP);
                    if (run.player.weapons.size() < Run.MAX_WEAPONS) {
                        run.player.weapons.add(new WeaponInst(weaponId));
                        finish();
                    } else {
                        game.push(new ReplaceScreen(game, weaponId));
                    }
                } else if (c == B_LEAVE) {
                    game.sound.sfx(Sound.S_BACK);
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onBack() {
        return true;
    }

    @Override
    public void render(Gfx g) {
        g.clear(0xFFF0F4E0);
        Run run = game.profile.run;
        if (run == null) return;
        Ui.header(g, "SIEG!  ETAPPE " + (run.stage - 1) + " GESCHAFFT", W());
        int y = 26;
        String info = "Max-KP +" + Run.HP_PER_STAGE + (healed > 0 ? "   " + Font.HEART + " +" + healed + " KP geheilt" : "");
        g.textCenter(info, W() / 2, y, Pal.INK);
        y += 12;
        g.textCenter("Du erhältst eine " + Art.BOX_NAME[tier].toUpperCase(java.util.Locale.GERMAN) + "!", W() / 2, y, Pal.INK_SOFT);

        int extra = Math.max(0, H() - 360);
        int boxCx = W() / 2;
        int boxBottom = 150 + extra / 2;
        int scale = H() >= 520 ? 4 : 3;
        Sprite body = Art.box(tier, 0), lid = Art.box(tier, 1);
        int bw = body.w * scale;
        int bx = boxCx - bw / 2;
        int byBody = boxBottom - body.h * scale;
        int shakeX = 0;
        if (st == S_SHAKE) shakeX = (int) (Math.sin(stT / 25.0) * (2 + stT / 300));
        if (st == S_CLOSED) shakeX = ((int) (game.timeMs / 900)) % 3 == 0 ? (int) (Math.sin(game.timeMs / 30.0) * 1.5) : 0;

        // rays
        if (st >= S_OPEN) {
            float p = st == S_OPEN ? Math.min(1f, stT / 600f) : 1f;
            int len = (int) (80 * p);
            for (int i = 0; i < 12; i++) {
                double a = i / 12.0 * Math.PI * 2 + game.timeMs / 2000.0;
                int x1 = boxCx + (int) (Math.cos(a) * len), y1 = byBody - 6 + (int) (Math.sin(a) * len * 0.8);
                g.thickLine(boxCx, byBody - 6, x1, y1, 3, i % 2 == 0 ? 0xFFF8E8A0 : 0xFFFFF4D0);
            }
        }
        g.fillEllipse(boxCx, boxBottom, bw / 2 + 6, 5, Pal.GROUND_DARK);
        g.sprite(body, bx + shakeX, byBody, scale);
        if (st < S_OPEN) {
            g.sprite(lid, bx + shakeX, byBody - lid.h * scale + 3, scale);
        } else {
            float p = Math.min(1f, (st == S_OPEN ? stT : 900) / 400f);
            int lidW = lid.w * scale;
            int restX = Math.max(2, bx - lidW + 8);
            int lx = bx + (int) ((restX - bx) * p), ly = byBody - lid.h * scale + 3 - (int) (Math.sin(p * Math.PI * 0.8) * 50);
            if (p < 1f) g.sprite(lid, lx, ly, scale);
            else g.sprite(lid, restX, byBody + body.h * scale - lid.h * scale, scale);
            // weapon rising
            float wp = st == S_OPEN ? Math.min(1f, stT / 700f) : 1f;
            int wy = byBody - (int) (wp * 64) - 4;
            int bob = st == S_SHOW ? (int) (Math.sin(game.timeMs / 300.0) * 2) : 0;
            g.sprite(WeaponArt.icon(weaponId), boxCx - 8 * scale, wy + bob - (scale - 3) * 16, scale);
        }
        if (st == S_CLOSED) {
            ui.region(B_BOX, bx - 10, byBody - 40, bw + 20, body.h * scale + 50);
            if (((int) (game.timeMs / 500) & 1) == 0) g.textCenter("Tippe auf die Kiste!", W() / 2, boxBottom + 12, Pal.INK);
        }

        if (st == S_SHOW) {
            WeaponDef d = WeaponDef.get(weaponId);
            int py = boxBottom + 12;
            int ph = Math.max(110, H() - py - 40);
            ph = Math.min(ph, 150);
            Ui.box(g, 4, py, W() - 8, ph);
            int ix = 14, iy = py + 10;
            g.text(d.name.toUpperCase(java.util.Locale.GERMAN), ix, iy, Pal.INK);
            Ui.rarityBadge(g, d.rarity, W() - 14 - Font.width(WeaponDef.RARITY_NAME[d.rarity].toUpperCase(java.util.Locale.GERMAN), 1) - 6, iy - 2);
            iy += 13;
            Ui.typeBadge(g, d.type, ix, iy, 56);
            g.text(Font.SWORD + " Angriff " + d.atk + "   " + Font.SHIELD + " Abwehr " + d.def, ix + 62, iy + 2, Pal.INK);
            iy += 15;
            iy += Ui.paragraph(g, d.desc, ix, iy, W() - 30, Pal.INK_SOFT);
            for (Effect e : d.innate) {
                g.text(Font.STAR + " " + e.describe(), ix, iy, Pal.BLUE);
                iy += Font.LINE;
            }
            iy += 2;
            g.text("Schlägt: " + Element.joinNames(Element.beatsList(d.type)), ix, iy, Pal.GREEN);
            iy += Font.LINE;
            g.text("Verliert: " + Element.joinNames(Element.losesList(d.type)), ix, iy, Pal.RED);
            int by = Math.min(H() - 30, py + ph + 4);
            boolean have = run.player.hasWeapon(weaponId);
            ui.button(g, B_TAKE, 12, by, 104, 24, run.player.weapons.size() >= Run.MAX_WEAPONS ? "TAUSCHEN" : "NEHMEN",
                    have ? Ui.BTN_DISABLED : Ui.BTN_PRIMARY);
            ui.button(g, B_LEAVE, 124, by, 104, 24, "LIEGEN LASSEN", Ui.BTN_NORMAL);
        }
    }
}
