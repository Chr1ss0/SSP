# Schnick Schnack Schnuck – Das Waffen-Duell

Ein Schere-Stein-Papier-Roguelike für Android, aufgebaut wie die Kämpfe der ersten
Pokémon-Spiele (Info-Boxen mit KP-Balken, Textbox, Held von hinten, Gegner von vorn),
aber in einem modernen, hochauflösenden Pixel-Look mit 8-Bit-Musik.
Inspiriert von Roguelike-Deckbuildern wie *Balatro*: Karten sammeln, Waffen
verbessern, so weit wie möglich kommen.

**APK:** [`dist/SchnickSchnackSchnuck.apk`](dist/SchnickSchnackSchnuck.apk)
(Android 6.0+, keine Berechtigungen außer Vibration)

## Installation

1. APK aufs Handy laden (z. B. direkt hier von GitHub).
2. Öffnen und „Installation aus unbekannten Quellen“ für den Browser/Dateimanager erlauben.
3. Installieren und spielen. Der Spielstand wird automatisch gespeichert.

## Spielprinzip

- **Runden:** Du wählst eine Waffe, der Gegner wählt gleichzeitig zufällig aus seinem
  (sichtbaren) Arsenal. Dann heißt es *Schnick… Schnack… Schnuck!* Dein Held hält die
  gewählte Waffe in der Hand.
- **Arsenal (6 Waffen, für dich und jeden Gegner gleich):**
  - **Stein, Schere, Papier** sind fest im Arsenal und **immer** einsetzbar (∞).
  - **3 Spezialwaffen** belegst du selbst mit freigeschalteten Waffen. Nur sie tragen
    Bonuskarten, und jede darf **nur einmal pro Gegner** eingesetzt werden (1×).
    Für den Gegner gilt dieselbe Regel.
- **9 Typen:** Stein, Klinge, Papier, Feuer, Wasser, Blitz, Pflanze, Stich, Eis.
  Jeder Typ schlägt genau 4 andere und verliert gegen 4 (ausbalancierte Typentabelle
  im Waffendex). Gleicher Typ = Unentschieden.
- **50 Waffen** vom Streichholz über Nadel und Flammenwerfer bis zum Meteoriten,
  jede mit Typ, Angriff, Abwehr, Seltenheit und Kartenplätzen, teils mit Fähigkeit.
- **100 Bonuskarten:** Für jeden besiegten Gegner bekommst du **2 Bonuskarten** und
  rüstest sie auf deine Spezialwaffen aus. Aus einer Waffe wird z. B. eine
  **Steinbrecher-Fackel** (besiegt auch Stein) oder ein **Diamant-Bambus** (+8 Angriff,
  +3 Abwehr). Es gibt Brecher, Immunitäten („Feuerfest“, „Gummi“ gegen Blitz …),
  Materialien, Gift, Vampir, Dornen, Doppelschlag, Hellseher, Phönix und mehr.
  Im Kampf siehst du unter jeder Spezialwaffe die Symbole ihrer Karten.
- **Kisten:** Nach jedem Sieg öffnest du eine Holz-, Eisen- oder Goldkiste. Sie schaltet
  eine neue Waffe **dauerhaft** frei, die du direkt ins Arsenal legen kannst.
- **Arenen:** 16 Trainerklassen, alle 5 Etappen ein Arenaleiter mit eigenem Typ,
  nach Etappe 50 der Großmeister, danach endlos.
- **Niederlage:** Der Lauf beginnt neu. **Bonuskarten und freigeschaltete Waffen
  behältst du** und verteilst die Karten neu (oder per „Auto-Verteilen“).
- **Charakter-Editor:** Name, 8 Frisuren, 10 Haarfarben, 6 Augenfarben, 6 Hauttöne,
  Oberteil (T-Shirt, Hoodie, Jacke) und Hose in 12 Farben, Kopfbedeckung (Kappe,
  Mütze, Krone, Zauberhut …), Brille & Co. Die Figuren sind schattiert und texturiert.
- **Grafik:** Alle Waffen-, Karten- und Typ-Symbole in 32×32 (vorher 16×16), weiche
  Schrift (Outfit), abgerundete Panels, Verläufe und Schatten.
- **8-Bit-Sound:** eigener Synthesizer (2× Pulse, Dreieck, Rauschen wie beim Game Boy),
  6 selbst komponierte Musikstücke (Titel, Route, Kampf, Boss, Sieg, Game Over) und
  über 20 Soundeffekte.

## Bauen

### Ohne Gradle (so wurde die APK gebaut)

```bash
./build.sh   # -> dist/SchnickSchnackSchnuck.apk
```

Benötigt JDK 8+ und die Android-Build-Tools (`aapt2`, `d8` oder `dx`, `zipalign`,
`apksigner`) sowie eine `android.jar` (API 23+), gesucht unter `$ANDROID_HOME`.
Unter Debian/Ubuntu reicht:

```bash
sudo apt install aapt zipalign apksigner dalvik-exchange android-sdk-platform-23
```

Signiert wird mit `keystore/ssp-release.jks` (Hobby-Schlüssel, Passwort in `build.sh`),
damit spätere Versionen als Update über die alte installiert werden können.
Für eine Veröffentlichung im Play Store bitte einen eigenen Schlüssel verwenden.

### Mit Android Studio

Das Projekt enthält auch eine Standard-Gradle-Konfiguration (`settings.gradle`,
`app/build.gradle`, Gradle-Wrapper) zum Öffnen in Android Studio. Sie wurde in der
Build-Umgebung dieser APK nicht ausgeführt (kein Zugriff auf Googles Maven-Repository).

## Projektstruktur

```
app/src/main/java/de/chr1ss0/ssp/core/   plattformunabhängiges Spiel (reines Java)
    Gfx, Font, FontData, Ui               Software-Renderer (240×H logisch, 2× intern), Schrift, UI
    Sprite, Stamp, Shade                  Sprites, Formen, HD-Veredelung (Scale2x, Licht, Textur)
    WeaponArt, CharArt, Art               Pixel-Art (Waffen, Charaktere, Karten, Kisten)
    Element, WeaponDef, BonusDef, Effect  Typen, 50 Waffen, 100 Karten
    Combat, Enemies, RunLogic, Cards      Kampfregeln, Gegner, Lauf-Logik
    Sound, Mml, Music, Sfx                8-Bit-Synthesizer, MML-Sequenzer, Musik, Effekte
    *Screen                               alle Bildschirme
app/src/main/java/de/chr1ss0/ssp/app/    Android-Hülle (Activity, View, AudioTrack)
desktop/src/                              Desktop-Werkzeuge (nicht in der APK)
tools/desk.sh                             kompiliert + startet ein Desktop-Werkzeug
```

Desktop-Werkzeuge (`tools/desk.sh <Name> [Argumente]`):

| Werkzeug     | Zweck                                                                 |
|--------------|-----------------------------------------------------------------------|
| `Sheets`     | rendert Font, Waffen-, Karten- und Charakter-Sheets als PNG           |
| `Shots`      | spielt das Spiel headless durch, macht Screenshots + Monkey-Test      |
| `Play`       | spielt komplette Läufe über die echte UI (Boss, Niederlage, Neustart) |
| `Sim`        | Balance-Simulation über viele Läufe                                   |
| `AudioCheck` | prüft die Musikstücke und rendert sie als WAV                         |
| `IconGen`    | erzeugt die Launcher-Icons aus der Pixel-Art                          |
| `FontGen`    | rendert die Schrift aus `tools/fonts` nach `core/FontData.java`       |

## Schriften

Outfit Bold und Inter Bold (beide SIL Open Font License, siehe `tools/fonts/`) werden
beim Generieren in Glyphen-Bitmaps umgewandelt und sind so in der APK enthalten.
