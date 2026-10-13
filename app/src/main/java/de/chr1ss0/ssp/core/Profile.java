package de.chr1ss0.ssp.core;

import java.util.HashMap;

/** Persistent player data: character, card collection, weapon dex, stats, options and the current run. */
public final class Profile {
    public static final int NAME_MAX = 8;

    public String name = "HELD";
    public Look look = new Look();
    public boolean created;
    public final int[] cards = new int[BonusDef.ALL.length];
    public final boolean[] cardSeen = new boolean[BonusDef.ALL.length];
    public final boolean[] seen = new boolean[WeaponDef.ALL.length];
    public final boolean[] owned = new boolean[WeaponDef.ALL.length];
    public int runs, bestStage, totalWins, roundsWon, bossesBeaten, champion;
    public int musicVol = 2, sfxVol = 3, textSpeed = 1;
    public boolean vibrate = true;
    /** 0 = show every drawn card, 1 = only rare and better, 2 = text only. */
    public int cardPopup = 0;
    public Run run;

    public Profile() {
        seen[WeaponDef.STEIN_ID] = seen[WeaponDef.SCHERE_ID] = seen[WeaponDef.PAPIER_ID] = true;
        owned[WeaponDef.STEIN_ID] = owned[WeaponDef.SCHERE_ID] = owned[WeaponDef.PAPIER_ID] = true;
    }

    public int totalCards() {
        int n = 0;
        for (int c : cards) n += c;
        return n;
    }

    public int distinctCards() {
        int n = 0;
        for (boolean b : cardSeen) if (b) n++;
        return n;
    }

    public int seenCount() {
        int n = 0;
        for (boolean b : seen) if (b) n++;
        return n;
    }

    public int ownedCount() {
        int n = 0;
        for (boolean b : owned) if (b) n++;
        return n;
    }

    /** Copies of a card not equipped in the current run. */
    public int available(int cardId) {
        int used = run != null ? run.player.countCard(cardId) : 0;
        return cards[cardId] - used;
    }

    public void addCard(int id) {
        cards[id]++;
        cardSeen[id] = true;
    }

    // ---------------------------------------------------------------- serialisation

    private static String bits(boolean[] b) {
        StringBuilder sb = new StringBuilder();
        for (boolean x : b) sb.append(x ? '1' : '0');
        return sb.toString();
    }

    private static void readBits(String s, boolean[] out) {
        if (s == null) return;
        for (int i = 0; i < out.length && i < s.length(); i++) if (s.charAt(i) == '1') out[i] = true;
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        sb.append("v=1\n");
        sb.append("name=").append(name).append('\n');
        sb.append("look=").append(look.key()).append('\n');
        sb.append("created=").append(created ? 1 : 0).append('\n');
        sb.append("cards=");
        for (int i = 0; i < cards.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(cards[i]);
        }
        sb.append('\n');
        sb.append("cardSeen=").append(bits(cardSeen)).append('\n');
        sb.append("seen=").append(bits(seen)).append('\n');
        sb.append("owned=").append(bits(owned)).append('\n');
        sb.append("stats=").append(runs).append(',').append(bestStage).append(',').append(totalWins).append(',')
                .append(roundsWon).append(',').append(bossesBeaten).append(',').append(champion).append('\n');
        sb.append("opt=").append(musicVol).append(',').append(sfxVol).append(',').append(textSpeed).append(',')
                .append(vibrate ? 1 : 0).append(',').append(cardPopup).append('\n');
        if (run != null) run.encode(sb);
        return sb.toString();
    }

    public static Profile decode(String data) {
        Profile p = new Profile();
        if (data == null || data.length() == 0) return p;
        HashMap<String, String> m = new HashMap<String, String>();
        for (String line : data.split("\n")) {
            int eq = line.indexOf('=');
            if (eq > 0) m.put(line.substring(0, eq), line.substring(eq + 1));
        }
        try {
            if (m.containsKey("name")) {
                String n = m.get("name").trim();
                if (n.length() > 0) p.name = n.length() > NAME_MAX ? n.substring(0, NAME_MAX) : n;
            }
            if (m.containsKey("look")) p.look = Look.parse(m.get("look"));
            p.created = "1".equals(m.get("created"));
            if (m.containsKey("cards")) {
                String[] c = m.get("cards").split(",");
                for (int i = 0; i < c.length && i < p.cards.length; i++) p.cards[i] = Math.max(0, Integer.parseInt(c[i].trim()));
            }
            readBits(m.get("cardSeen"), p.cardSeen);
            readBits(m.get("seen"), p.seen);
            readBits(m.get("owned"), p.owned);
            for (int i = 0; i < p.cards.length; i++) if (p.cards[i] > 0) p.cardSeen[i] = true;
            if (m.containsKey("stats")) {
                String[] s = m.get("stats").split(",");
                p.runs = Integer.parseInt(s[0]);
                p.bestStage = Integer.parseInt(s[1]);
                p.totalWins = Integer.parseInt(s[2]);
                p.roundsWon = Integer.parseInt(s[3]);
                p.bossesBeaten = Integer.parseInt(s[4]);
                p.champion = Integer.parseInt(s[5]);
            }
            if (m.containsKey("opt")) {
                String[] o = m.get("opt").split(",");
                p.musicVol = Math.max(0, Math.min(3, Integer.parseInt(o[0])));
                p.sfxVol = Math.max(0, Math.min(3, Integer.parseInt(o[1])));
                p.textSpeed = Math.max(0, Math.min(2, Integer.parseInt(o[2])));
                p.vibrate = "1".equals(o[3]);
                if (o.length > 4) p.cardPopup = Math.max(0, Math.min(2, Integer.parseInt(o[4])));
            }
            p.run = Run.decode(m, p);
        } catch (RuntimeException e) {
            // corrupt save: keep what could be read
        }
        // safety: equipped cards can never exceed owned cards
        if (p.run != null) {
            for (WeaponInst w : p.run.player.weapons) {
                for (int i = 0; i < w.slots.length; i++) {
                    int id = w.slots[i];
                    if (id >= 0 && p.run.player.countCard(id) > p.cards[id]) w.slots[i] = -1;
                }
            }
        }
        return p;
    }
}
