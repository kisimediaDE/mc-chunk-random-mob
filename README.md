# Chunk Mob Challenge

Sessionübergreifende Minecraft-Challenge für Paper 26.1 und 26.2. Beim Betreten
eines neuen Chunks wird der Spieler in diesem Chunk eingeschlossen und muss einen
zufälligen Mob besiegen. Solo und verzweigtes kooperatives Multiplayer-Spiel
werden von demselben Run-State unterstützt.

## Voraussetzungen und Build

- Paper 26.1 oder 26.2
- Java 25

```bash
./gradlew build
```

Die fertige JAR liegt danach unter `build/libs/chunk-mob-challenge-1.0.0.jar`.

## Lokale Testserver

Das Projekt enthält vorbereitete Paper-Testserver für 26.1.2 und 26.2. Setup,
Prüfsummenprüfung, Build und Plugin-Installation erfolgen mit:

```bash
./scripts/setup-test-servers.sh
```

Danach kann jeweils eine Version gestartet werden:

```bash
./scripts/start-test-server.sh 26.1.2  # localhost:25565
./scripts/start-test-server.sh 26.2    # localhost:25565
```

Beide Versionsserver verwenden bewusst den Minecraft-Standardport. Daher immer
nur einen der beiden Server gleichzeitig starten.

Die vollständigen manuellen Prüfschritte stehen in [`TESTING.md`](TESTING.md).

## Commands

- `/chunkchallenge start` (`/cc start`)
- `/chunkchallenge stop`
- `/chunkchallenge status`
- `/chunkchallenge reload`
- `/chunkchallenge tags enable|disable`
- `/chunkchallenge glowing enable|disable`

Die Permissions heißen entsprechend `chunkchallenge.start`, `.stop`, `.status`
`.reload`, `.tags` und `.glowing`. `chunkchallenge.admin` enthält alle Rechte.
Standardmäßig besitzen OPs diese Berechtigungen. Mob-Nametags und Glowing sind
bei einem neuen Run standardmäßig deaktiviert.

## Stream pausieren und fortsetzen

Ein normaler Serverstopp beendet die Challenge **nicht**. Beim Herunterfahren
werden Spielerposition, Blickrichtung, Welt, Chunk-Runde, Mob-UUID, Mobzustand,
Statistiken und aktive Spielzeit in
`plugins/ChunkMobChallenge/state.yml` gespeichert. Beim nächsten Serverstart und
Login wird genau diese Runde fortgesetzt. Die Zeit zwischen den Sessions zählt
nicht zur Challenge-Zeit.

`/chunkchallenge stop` ist davon bewusst verschieden: Dieser Command beendet den
Run und löscht `state.yml`. Noch lebende Challenge-Mobs bleiben danach als normale
Mobs in der Welt.

## Mobpool

Beim ersten Start entsteht `plugins/ChunkMobChallenge/mobs.yml` aus allen auf der
laufenden Paper-Version spawnbaren Mob-EntityTypes. Alle Einträge sind gleich
wahrscheinlich. Entfernte Zeilen werden nicht wieder ergänzt. Um die komplette
Liste neu zu erzeugen, die Datei bei gestopptem Server löschen und den Server
starten.

Unbekannte oder ungeeignete Einträge werden mit einer Konsolenwarnung ignoriert.
`/chunkchallenge reload` lädt die Liste neu und beeinflusst nur zukünftige Runden.

## Wichtige Spielregeln

- Jede Todesursache des Challenge-Mobs zählt.
- Wasser-Mobs dürfen an Land erscheinen.
- Der Challenge-Mob bleibt im Chunk; natürliche Mobs werden nicht eingeschränkt.
- Während einer aktiven Runde sind Portale und Teleports aus dem Chunk blockiert.
- Wither und Enderdragon verwenden ihre Vanilla-Bossbar, solange Paper eine
  bereitstellt. Bei künstlichen Enderdrachen ohne native Bar stellt das Plugin
  nach einem Neustart genau eine Ersatz-Bossbar mit Titel, HP und Zuschauern her.
- Ein künstlicher Enderdragon ohne Vanilla-DragonBattle erhält eine eigene
  Zielsteuerung und greift Rundenteilnehmer regelmäßig mit Drachenfeuerbällen an.
- Challenge-Enderdrachen werden auf halbe Größe skaliert, damit Körper und Hitbox
  in einen 16×16-Chunk passen; der echte End-Drache bleibt unverändert.
- Zufällig gespawnte Enderdragons zählen nicht als Spielabschluss. Nur der vom
  Vanilla-`DragonBattle` verwaltete ursprüngliche Enderdragon beendet den Run.
- Auf Hardcore beendet ein Spielertod den gesamten Run. Ohne Hardcore startet der
  gestorbene Spieler am Respawn eine neue Runde; überlebende Teammitglieder führen
  ihre bisherige Runde fort.
