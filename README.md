# Schnick Schnack Schnuck – Das Waffen-Duell

Ein Schere-Stein-Papier-Roguelike für Android im Look der ersten Pokémon-Kämpfe
(Game-Boy-Pixelgrafik, Info-Boxen mit KP-Balken, Textbox, 8-Bit-Musik).
Inspiriert von Roguelike-Deckbuildern wie *Balatro*: Karten sammeln, Waffen
verbessern, so weit wie möglich kommen.

**APK:** [`dist/SchnickSchnackSchnuck.apk`](dist/SchnickSchnackSchnuck.apk)
(Android 6.0+, ca. 120 KB, keine Berechtigungen außer Vibration)

## Installation

1. APK aufs Handy laden (z. B. direkt hier von GitHub).
2. Öffnen und „Installation aus unbekannten Quellen“ für den Browser/Dateimanager erlauben.
3. Installieren und spielen. Der Spielstand wird automatisch gespeichert.

## Spielprinzip

- **Runden:** Du wählst eine deiner Waffen, der Gegner wählt gleichzeitig zufällig
  aus seinem (sichtbaren) Arsenal. Dann heißt es *Schnick… Schnack… Schnuck!*
  Dein Held hält die gewählte Waffe in der Hand.
- **9 Typen:** Stein, Klinge, Papier, Feuer, Wasser, Blitz, Pflanze, Stich, Eis.
  Jeder Typ schlägt genau 4 andere und verliert gegen 4 (ausbalancierte Typentabelle
  im Waffendex). Gleicher Typ = Unentschieden.
- **50 Waffen** vom Streichholz über Nadel und Flammenwerfer bis zum Meteoriten –
  jede mit Typ, Angriff, Abwehr, Seltenheit, teils Spezialfähigkeit.
- **100 Bonuskarten:** Nach jeder gewonnenen Runde ziehst du eine zufällige Karte und
  rüstest sie auf eine Waffe aus. Aus Schere wird z. B. **Steinbrecher-Schere** (besiegt
  auch Stein), aus Papier **Diamant-Papier** (+8 Angriff, +3 Abwehr). Es gibt Brecher,
  Immunitäten („Feuerfest“, „Gummi“ gegen Blitz …), Materialien, Gift, Vampir,
  Dornen, Doppelschlag, Hellseher, Phönix und vieles mehr.
- **Kisten:** Nach jedem Sieg öffnest du eine Holz-, Eisen- oder Goldkiste mit einer
  neuen Waffe (max. 6 Waffen im Arsenal).
- **Arenen:** 16 Trainerklassen, alle 5 Etappen ein Arenaleiter mit eigenem Typ,
  nach Etappe 50 der Großmeister – danach endlos.
- **Niederlage:** Der Lauf beginnt neu mit Stein, Schere und Papier – **die Bonuskarten
  behältst du** und verteilst sie neu (oder per „Auto-Verteilen“).
- **Charakter-Editor:** Name, 8 Frisuren, 10 Haarfarben, 6 Hauttöne, Ober- und
  Unterteil in 12 Farben, Kopfbedeckung (Kappe, Mütze, Krone, Zauberhut …), Brille & Co.
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
    Gfx, Font, Sprite, Stamp, Ui          Software-Renderer 240×H Pixel, Pixel-Font, UI
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
