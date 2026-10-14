package de.chr1ss0.ssp.core;

import java.util.Random;

/**
 * Victory rewards: first a loot box with a special weapon that can join the
 * arsenal (up to six weapons), then the two bonus cards of the victory.
 */
public final class RewardScreen extends Screen {
    private static final int B_BOX = 1, B_ARSENAL = 2, B_LEAVE = 3, B_NEXT = 4, B_AUTO = 5, B_EQUIP = 10;
    private static final int S_CLOSED = 0, S_SHAKE = 1, S_OPEN = 2, S_SHOW = 3, S_CARDS = 4;

    private final int healed;
    private int st = S_CLOSED;
    private float stT;
    private int weaponId = -1;
    private int tier;
    private boolean initialized, wasNew;

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
        if (!initialized) {
            initialized = true;
            if (run.rewardBox >= 0) {
                tier = run.rewardBox;
                Random r = new Random(run.seed * 13 + run.stage * 101L);
                weaponId = RunLogic.rollBoxWeapon(game.profile, r, tier);
            } else {
                // box already opened before the app was closed
                toCards();
            }
        }
        // back from the replace screen
        if (st == S_SHOW && weaponId >= 0 && run.player.hasWeapon(weaponId)) toCards();
    }

    private void toCards() {
        Run run = game.profile.run;
        if (run.pendingCards.length == 0) {
            finish();
            return;
        }
        st = S_CARDS;
        stT = 0;
    }

    private void finish() {
        Run run = game.profile.run;
        run.phase = Run.PHASE_MAP;
        run.rewardBox = -1;
        run.pendingCards = new int[0];
        game.save();
        game.set(new MapScreen(game), Game.TR_FADE);
    }

    /** Opening the box unlocks the weapon in the dex; the box is used up. */
    private void openBox() {
        Profile p = game.profile;
        if (weaponId >= 0) {
            wasNew = !p.owned[weaponId];
            p.unlock(weaponId);
        }
        p.run.rewardBox = -1;
        game.save();
    }

    private int freeCards() {
        Profile p = game.profile;
        int n = 0;
        for (int i = 0; i < p.cards.length; i++) n += Math.max(0, p.available(i));
        return n;
    }

    private boolean hasEmptySlot() {
        for (WeaponInst w : game.profile.run.player.weapons) if (w.freeSlot() >= 0) return true;
        return false;
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
                    openBox();
                }
                break;
            case S_OPEN:
                if (stT > 900) {
                    st = S_SHOW;
                    stT = 0;
                }
                break;
            case S_SHOW:
                if (c == B_ARSENAL && weaponId >= 0) {
                    if (run.arsenalFull()) {
                        game.sound.sfx(Sound.S_SELECT);
                        game.push(new ReplaceScreen(game, weaponId));
                    } else {
                        run.addWeapon(weaponId);
                        game.sound.sfx(Sound.S_LEVELUP);
                        game.save();
                        toCards();
                    }
                } else if (c == B_LEAVE) {
                    game.sound.sfx(Sound.S_SELECT);
                    toCards();
                }
                break;
            default:
                if (c >= B_EQUIP && c < B_EQUIP + run.pendingCards.length) {
                    game.sound.sfx(Sound.S_SELECT);
                    game.push(new EquipScreen(game, BonusDef.get(run.pendingCards[c - B_EQUIP]), 1));
                } else if (c == B_AUTO) {
                    int n = RunLogic.autoEquip(game.profile, run.player);
                    game.sound.sfx(n > 0 ? Sound.S_LEVELUP : Sound.S_ERROR);
                    game.save();
                } else if (c == B_NEXT) {
                    game.sound.sfx(Sound.S_SELECT);
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
        Ui.background(g);
        Run run = game.profile.run;
        if (run == null) return;
        Ui.header(g, "SIEG!  ETAPPE " + (run.stage - 1) + " GESCHAFFT", W());
        int y = 26;
        String info = "Max-KP +" + Run.HP_PER_STAGE + (healed > 0 ? "    " + Font.HEART + " +" + healed + " KP geheilt" : "");
        g.textCenter(info, W() / 2, y, Pal.INK);
        if (st == S_CARDS) renderCards(g, run);
        else renderBox(g, run);
    }

    private void renderCards(Gfx g, Run run) {
        int n = run.pendingCards.length;
        g.textCenter("Du erhältst " + n + " Bonuskarten!", W() / 2, 40, Pal.INK_SOFT);
        int cw = 108, ch = 160, gap = 8;
        int total = n * cw + (n - 1) * gap;
        int x0 = (W() - total) / 2;
        int y = Math.max(56, (H() - ch - 130) / 2);
        float t = stT;
        for (int i = 0; i < n; i++) {
            BonusDef b = BonusDef.get(run.pendingCards[i]);
            int x = x0 + i * (cw + gap);
            float p = Math.min(1f, Math.max(0f, (t - i * 250) / 450f));
            if (p < 1f) {
                int vis = (int) (cw * Math.abs(Math.cos((1 - p) * Math.PI / 2)));
                g.roundRect(x + (cw - vis) / 2, y, Math.max(2, vis), ch, 8, p < 0.5f ? 0xFF3B3F7A : Pal.RARITY_LIGHT[b.rarity]);
                continue;
            }
            CardScreen.drawCard(g, b, x, y, cw, ch, game.profile.cards[b.id], game.timeMs);
            boolean can = game.profile.available(b.id) > 0 && run.player.special(0) != null;
            ui.button(g, B_EQUIP + i, x + 4, y + ch + 6, cw - 8, 22, "AUSRÜSTEN", can ? Ui.BTN_PRIMARY : Ui.BTN_DISABLED);
        }
        int by = Math.min(H() - 60, y + ch + 36);
        int free = freeCards();
        if (run.player.special(0) == null) {
            Ui.paragraph(g, "Karten passen nur auf Spezialwaffen. Hol dir eine aus der nächsten Kiste!", 14, by, W() - 28, Pal.INK_SOFT);
        } else {
            g.textCenter("Freie Karten: " + free, W() / 2, by, Pal.INK_SOFT);
        }
        by += 24;
        boolean auto = free > 0 && hasEmptySlot();
        ui.button(g, B_AUTO, 10, by, 108, 26, "AUTO-VERTEILEN", auto ? Ui.BTN_NORMAL : Ui.BTN_DISABLED);
        ui.button(g, B_NEXT, 122, by, 108, 26, "WEITER", Ui.BTN_PRIMARY);
    }

    private void renderBox(Gfx g, Run run) {
        g.textCenter("Du erhältst eine " + Art.BOX_NAME[tier].toUpperCase(java.util.Locale.GERMAN) + "!", W() / 2, 40, Pal.INK_SOFT);
        int extra = Math.max(0, H() - 360);
        int boxCx = W() / 2;
        int boxBottom = 150 + extra / 2;
        int scale = H() >= 520 ? 4 : 3;
        Sprite body = Art.box(tier, 0), lid = Art.box(tier, 1);
        int bw = body.w * scale / 2;
        int bh = body.h * scale / 2;
        int lh = lid.h * scale / 2;
        int bx = boxCx - bw / 2;
        int byBody = boxBottom - bh;
        int shakeX = 0;
        if (st == S_SHAKE) shakeX = (int) (Math.sin(stT / 25.0) * (2 + stT / 300));
        if (st == S_CLOSED) shakeX = ((int) (game.timeMs / 900)) % 3 == 0 ? (int) (Math.sin(game.timeMs / 30.0) * 1.5) : 0;

        if (st >= S_OPEN) {
            float p = st == S_OPEN ? Math.min(1f, stT / 600f) : 1f;
            int len = (int) (Math.min(90, (byBody - 10 - 52) / 0.8f) * p); // keep the rays below the subtitle
            for (int i = 0; i < 14; i++) {
                double a = i / 14.0 * Math.PI * 2 + game.timeMs / 2000.0;
                int x1 = boxCx + (int) (Math.cos(a) * len), y1 = byBody - 10 + (int) (Math.sin(a) * len * 0.8);
                g.thickLine(boxCx, byBody - 10, x1, y1, 3, i % 2 == 0 ? 0xFFFFE9A0 : 0xFFFFF6D6);
            }
            g.fillEllipse(boxCx, byBody - 12, 30 * p, 24 * p, 0x60FFFFFF);
        }
        g.fillEllipse(boxCx, boxBottom, bw / 2 + 8, 5, 0x40000000);
        g.spriteHD(body, bx + shakeX, byBody, scale);
        if (st < S_OPEN) {
            g.spriteHD(lid, bx + shakeX, byBody - lh + 3, scale);
        } else {
            float p = Math.min(1f, (st == S_OPEN ? stT : 900) / 400f);
            int lidW = lid.w * scale / 2;
            int restX = Math.max(2, bx - lidW + 8);
            int lx = bx + (int) ((restX - bx) * p), ly = byBody - lh + 3 - (int) (Math.sin(p * Math.PI * 0.8) * 50);
            if (p < 1f) g.spriteHD(lid, lx, ly, scale);
            else g.spriteHD(lid, restX, byBody + bh - lh, scale);
            float wp = st == S_OPEN ? Math.min(1f, stT / 700f) : 1f;
            int wy = byBody - (int) (wp * 64) - 6;
            int bob = st == S_SHOW ? (int) (Math.sin(game.timeMs / 300.0) * 2) : 0;
            if (weaponId >= 0) g.spriteHD(WeaponArt.icon(weaponId), boxCx - 24, wy + bob, 3);
        }
        if (st == S_CLOSED) {
            ui.region(B_BOX, bx - 10, byBody - 40, bw + 20, bh + 50);
            if (((int) (game.timeMs / 500) & 1) == 0) g.textCenter("Tippe auf die Kiste!", W() / 2, boxBottom + 12, Pal.INK);
        }

        if (st == S_SHOW && weaponId >= 0) {
            int py = boxBottom + 12;
            int ph = Math.min(150, Math.max(104, H() - py - 40));
            Ui.box(g, 6, py, W() - 12, ph);
            int ix = 16, iy = py + 9;
            WeaponDef d = WeaponDef.get(weaponId);
            String tag = wasNew ? "NEU FREIGESCHALTET!" : "WAFFE GEFUNDEN";
            g.roundRect(ix - 2, iy - 2, Font.width(tag, 1) + 8, 11, 5, wasNew ? Pal.GOLD : Pal.ACCENT);
            g.text(tag, ix + 2, iy - 1, Pal.WHITE);
            g.textRight("Arsenal " + run.player.weapons.size() + "/" + Run.MAX_WEAPONS, W() - 16, iy - 1, Pal.INK_SOFT);
            iy += 13;
            g.text(d.name.toUpperCase(java.util.Locale.GERMAN), ix, iy, Pal.INK);
            Ui.rarityBadge(g, d.rarity, W() - 16 - Ui.rarityBadgeWidth(d.rarity), iy - 2);
            iy += 13;
            Ui.typeBadge(g, d.type, ix, iy, 62);
            g.text(Font.SWORD + " " + d.atk + "   " + Font.SHIELD + " " + d.def + "   Plätze: " + d.slots, ix + 68, iy + 2, Pal.INK);
            iy += 15;
            iy += Ui.paragraph(g, d.desc, ix, iy, W() - 32, Pal.INK_SOFT);
            for (Effect e : d.innate) {
                g.text(Font.STAR + " " + e.describe(), ix, iy, Pal.BLUE);
                iy += Font.LINE;
            }
            iy += Ui.paragraph(g, "Schlägt: " + Element.joinNames(Element.beatsList(d.type)), ix, iy, W() - 32, Pal.GREEN);
            Ui.paragraph(g, "Verliert: " + Element.joinNames(Element.losesList(d.type)), ix, iy, W() - 32, Pal.RED);
            int by = Math.min(H() - 30, py + ph + 4);
            ui.button(g, B_ARSENAL, 10, by, 108, 24, run.arsenalFull() ? "ERSETZEN" : "INS ARSENAL", Ui.BTN_PRIMARY);
            ui.button(g, B_LEAVE, 122, by, 108, 24, "LIEGEN LASSEN", Ui.BTN_NORMAL);
        }
    }
}
