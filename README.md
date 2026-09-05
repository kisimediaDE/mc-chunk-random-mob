<div align="center">

# Chunk Mob Challenge

### Besiege in jedem Chunk einen zufälligen Mob

<p>
  <a href="#kompatibilität"><img alt="Minecraft 26.1.2 und 26.2" src="https://img.shields.io/badge/Minecraft-26.1.2%20%7C%2026.2-62B47A?style=for-the-badge&logo=minecraft&logoColor=white"></a>&nbsp;&nbsp;<a href="https://papermc.io/"><img alt="Paper Server" src="https://img.shields.io/badge/Server-Paper-2C2E35?style=for-the-badge"></a>&nbsp;&nbsp;<a href="#voraussetzungen"><img alt="Java 25" src="https://img.shields.io/badge/Java-25-E76F00?style=for-the-badge&logo=openjdk&logoColor=white"></a>&nbsp;&nbsp;<a href="#kompatibilität"><img alt="Status Beta" src="https://img.shields.io/badge/Status-Beta-F2B134?style=for-the-badge"></a>
</p>

**Betritt einen Chunk, besiege seinen zufälligen Gegner und kämpfe dich<br>
bis zum echten Enderdragon vor – über beliebig viele Server-Sessions hinweg.**

</div>

> [!IMPORTANT]
> Das Plugin befindet sich noch in der Beta-Phase. Sichere vor einem längeren
> Durchlauf immer Welt und Plugin-Daten gemeinsam.

## Was ist die Chunk Mob Challenge?

Die Chunk Mob Challenge verwandelt jeden neu betretenen Chunk in eine kleine
Arena. Sobald eine Runde beginnt, erscheint ein zufälliger Mob und eine
individuelle Worldborder schließt die Teilnehmer im 16×16-Blöcke großen Chunk
ein. Erst nach dem Tod des Challenge-Mobs öffnet sich der Weg in den nächsten
Chunk.

Ein Run kann allein oder kooperativ gespielt werden. Mehrere Spieler dürfen
dieselbe Runde bestreiten oder sich nach einem Sieg auf unterschiedliche Chunks
und parallele Kämpfe verteilen. Die aktive Spielzeit, Positionen, Runden,
Mobzustände und Statistiken bleiben über Logout und Serverneustart erhalten.

Das endgültige Ziel ist der echte, von Vanillas `DragonBattle` verwaltete
Enderdragon. Ein zufällig als Challenge-Mob gezogener Enderdragon zählt nur als
gewöhnliche Chunk-Runde.

## Die wichtigsten Regeln

- Beim Start nehmen alle aktuell verbundenen Spieler gemeinsam teil.
- Ein neuer Chunk erzeugt genau eine Runde mit genau einem zufälligen Mob.
- Spieler im selben aktiven Chunk teilen Mob und Bossbar.
- Parallele Runden in unterschiedlichen Chunks sind möglich.
- Während des Kampfes verhindert die Worldborder das Verlassen des Chunks.
- Enderperlen, Chorusfrüchte, Portale und fremde Teleports umgehen den Lock nicht.
- Jede echte Todesursache des Challenge-Mobs zählt – auch Feuer, Ertrinken,
  Ersticken oder eine Creeper-Selbstexplosion.
- Natürlich vorkommende Mobs bleiben uneingeschränkt.
- Wasser-Mobs dürfen an Land erscheinen und dort auf natürliche Weise sterben.
- In Hardcore beendet der Tod eines Teilnehmers den gesamten Run.
- Ohne Hardcore verlässt nur der gestorbene Spieler seine bisherige Runde und
  beginnt nach dem Respawn im Respawn-Chunk neu.
- Gewonnen ist der Run ausschließlich nach dem Tod des echten Enderdrachens.

> [!TIP]
> Nametags, Leuchtkontur und Bossbars lassen sich während des Runs unabhängig
> voneinander ein- und ausschalten. Die Auswahl wird gespeichert.

## Voraussetzungen

- Minecraft Java **26.1.2** oder **26.2**
- ein [Paper-Server](https://papermc.io/downloads/paper/)
- **Java 25**
- das Plugin-JAR aus den
  [GitHub Releases](https://github.com/kisimediaDE/mc-chunk-random-mob/releases)

Folia, Spigot, Vanilla, Fabric, Forge und NeoForge werden nicht als
Serverplattform unterstützt. Client-Mods sind grundsätzlich möglich, solange
sie das normale Multiplayer-Verhalten nicht verändern.

## Installation

### 1. Paper vorbereiten

Richte einen Paper-Server für Minecraft 26.1.2 oder 26.2 ein und starte ihn
einmal. Akzeptiere anschließend die Minecraft-EULA in `eula.txt`.

Für einen Hardcore-Run muss bereits bei der Erzeugung der Welt in
`server.properties` stehen:

```properties
hardcore=true
difficulty=hard
```

Das nachträgliche Aktivieren von Hardcore macht eine schon vorhandene normale
Welt nicht zuverlässig zu einer Hardcore-Welt. Verwende dafür eine frisch
erzeugte Welt.

### 2. Plugin installieren

1. Stoppe den Server vollständig.
2. Lade unter
   [Releases](https://github.com/kisimediaDE/mc-chunk-random-mob/releases)
   das aktuelle Plugin-JAR herunter.
3. Kopiere das JAR in den Ordner `plugins`.
4. Starte den Server wieder.
5. Prüfe in der Konsole, ob `ChunkMobChallenge ist bereit` erscheint.
6. Erteile dem Spielleiter bei Bedarf Operatorrechte und starte mit `/cc start`.

Der erste Start erzeugt automatisch:

```text
plugins/
└── ChunkMobChallenge/
    └── mobs.yml
```

Sobald ein Run gestartet wird, kommt der persistente Zustand hinzu:

```text
plugins/ChunkMobChallenge/state.yml
```

### Server starten

Unter macOS und Linux beispielsweise:

```bash
java -Xms2G -Xmx2G -jar paper.jar --nogui
```

Unter Windows in PowerShell oder der Eingabeaufforderung:

```bat
java -Xms2G -Xmx2G -jar paper.jar --nogui
```

Kontrolliere bei mehreren installierten Java-Versionen vorher mit
`java -version`, dass Java 25 verwendet wird.

## Spielen

### Run starten

Der Spielleiter führt als Spieler aus:

```text
/cc start
```

Alle zu diesem Zeitpunkt verbundenen Spieler werden sicher zu ihm teleportiert
und starten gemeinsam in seinem Chunk. Ist der ursprüngliche Enderdragon der
Welt bereits besiegt, lehnt das Plugin einen neuen Run ab.

### Chunk-Runden

Besiegt den angezeigten Mob. Danach verschwinden Border und Bossbar für die
Teilnehmer dieser Runde. Wer anschließend einen neuen Chunk betritt, startet
dort sofort die nächste Runde.

Spieler dürfen im sicheren Chunk zurückbleiben, gemeinsam folgen oder in
unterschiedlichen Chunks parallele Runden eröffnen. Betritt jemand einen bereits
umkämpften Chunk, tritt er der vorhandenen Runde bei; es entsteht kein zweiter
Challenge-Mob.

### Anzeigen anpassen

```text
/cc tags enable
/cc tags disable
/cc glowing enable
/cc glowing disable
/cc bossbar enable
/cc bossbar disable
```

Nametags und Glowing sind bei einem neuen Run standardmäßig ausgeschaltet. Die
Bossbar ist standardmäßig eingeschaltet. Der Bossbar-Schalter gilt auch für die
Vanilla-Bossbars eines Challenge-Withers oder Challenge-Enderdrachens.

### Stream pausieren und fortsetzen

Ein normaler Serverstopp beendet die Challenge **nicht**. Beende Paper abends
mit dem Konsolenbefehl:

```text
stop
```

Beim Herunterfahren speichert das Plugin unter anderem:

- Spielerposition und Blickrichtung,
- Welt, Chunk und zugeordnete Runde,
- Mobtyp, UUID, Position und Lebenspunkte,
- kritische Varianten- und Ausrüstungsdaten,
- Todesfälle, Siege, Rundenzahl und aktive Spielzeit,
- Nametag-, Glowing- und Bossbar-Einstellung.

Beim nächsten Serverstart und Login wird derselbe Zustand fortgesetzt. Die Zeit
zwischen den Sessions zählt nicht zur Challenge-Zeit.

> [!WARNING]
> `/cc stop` ist nicht dasselbe wie der Paper-Konsolenbefehl `stop`.
> `/cc stop` beendet den laufenden Run bewusst und löscht dessen `state.yml`.

### Challenge abschließen

Der Run endet erfolgreich, sobald der echte Vanilla-Enderdragon stirbt. Das
Plugin zeigt anschließend aktive Spielzeit, besiegte Challenge-Mobs, gestartete
Runden und Todesfälle an. Borders, Bossbars, Projektilreste und
Challenge-Markierungen werden entfernt.

## Befehle

Alle Befehle verwenden wahlweise `/chunkchallenge` oder den Alias `/cc`.

| Befehl                         | Funktion                                                                  |
| ------------------------------ | ------------------------------------------------------------------------- |
| `/cc start`                    | Startet einen neuen Run; muss von einem Spieler ausgeführt werden.        |
| `/cc stop`                     | Beendet und löscht den aktuell laufenden Run.                             |
| `/cc status`                   | Zeigt Spielzeit, Statistiken, Anzeigeoptionen und die eigene Runde.       |
| `/cc reload`                   | Lädt `mobs.yml` für zukünftige Runden neu.                                |
| `/cc tags enable\|disable`    | Schaltet sichtbare Namen der Challenge-Mobs an oder aus.                  |
| `/cc glowing enable\|disable` | Schaltet die Leuchtkontur der Challenge-Mobs an oder aus.                 |
| `/cc bossbar enable\|disable` | Schaltet Plugin- und Vanilla-Bossbars der Challenge-Mobs an oder aus.     |

Diese Befehle sind standardmäßig nur für Operatoren freigegeben.

| Permission                   | Zweck                            |
| ---------------------------- | -------------------------------- |
| `chunkchallenge.start`       | Run starten                      |
| `chunkchallenge.stop`        | Run beenden und State löschen    |
| `chunkchallenge.status`      | Status anzeigen                  |
| `chunkchallenge.reload`      | Mobpool neu laden                |
| `chunkchallenge.tags`        | Mob-Nametags umschalten          |
| `chunkchallenge.glowing`     | Mob-Glowing umschalten           |
| `chunkchallenge.bossbar`     | Mob-Bossbars umschalten          |
| `chunkchallenge.admin`       | Enthält alle aufgeführten Rechte |

## Mobpool konfigurieren

Beim ersten Start erzeugt das Plugin `plugins/ChunkMobChallenge/mobs.yml` aus
allen Mob-EntityTypes, die auf der laufenden Paper-Version tatsächlich vorhanden
und spawnbar sind. Alle Einträge besitzen dieselbe Wahrscheinlichkeit – auch
Wither, Warden, Giant, Illusioner und Enderdragon.

Einträge dürfen entfernt werden und werden nicht automatisch ergänzt. Nach einer
Änderung lädt `/cc reload` die Datei neu. Bereits aktive oder pausierte Runden
behalten ihren bisherigen Mobtyp.

Unbekannte, doppelte oder ungeeignete Entity-Keys werden mit einer
Konsolenwarnung ignoriert. Enthält die Datei keinen gültigen Mob, bleibt der
bisherige Pool aktiv.

Um den vollständigen Pool wiederherzustellen:

1. Stoppe den Server.
2. Lösche `plugins/ChunkMobChallenge/mobs.yml`.
3. Starte den Server erneut.

Das Plugin erzeugt daraufhin die zur installierten Minecraft-Version passende
Standardliste neu.

## Besondere Mobs

- Challenge-Mobs despawnen nicht natürlich und bleiben im zugehörigen Chunk.
- Flugmobs werden an der Chunkgrenze umgelenkt, statt dort dauerhaft
  festzustecken.
- Große Mobs werden aus Blöcken und unzugänglichen Bereichen gerettet.
- Nether-Mobs erscheinen nicht unzugänglich auf dem Bedrockdach, wenn die Runde
  darunter ausgelöst wurde.
- Slime- und Magma-Cube-Nachkommen verlieren nach dem Rundensieg alle
  Challenge-Markierungen.
- Transformationen wie Zombie Villager zu Villager übernehmen die laufende
  Runde, Lebenspunkteanzeige und Markierungen korrekt.
- Challenge-Enderdrachen werden auf halbe Größe skaliert und über dem Chunk
  gehalten. Eine eigene Zielsteuerung sorgt außerhalb eines Vanilla-DragonBattle
  für sichtbare Drachenfeuerbälle und Atemwolken am Boden.
- Wither und Enderdragon verwenden ihre Vanilla-Bossbar, sofern Paper sie
  bereitstellt. Für einen Challenge-Enderdrachen ohne native Bossbar erzeugt das
  Plugin genau eine Ersatz-Bossbar.

## Backups und Wiederherstellung

Der Pluginzustand liegt in:

```text
plugins/ChunkMobChallenge/state.yml
```

Die Challenge-Mobs selbst werden zusätzlich von Minecraft in den Weltdateien
gespeichert. Sichere deshalb immer die Weltordner und den gesamten Ordner
`plugins/ChunkMobChallenge` gemeinsam.

### Sicheres Backup

1. Beende Paper mit dem Konsolenbefehl `stop`.
2. Sichere die zusammengehörigen Weltordner.
3. Sichere den vollständigen Ordner `plugins/ChunkMobChallenge`.

### Wiederherstellen

1. Stoppe den Server vollständig.
2. Stelle Weltordner und Pluginordner aus demselben Backup wieder her.
3. Starte Paper erneut.
4. Verbinde dich und kontrolliere `/cc status`.

Eine Welt und eine `state.yml` aus unterschiedlichen Zeitpunkten können zu
fehlenden oder doppelt rekonstruierten Challenge-Mobs führen.

## Lokale Entwicklung und Tests

Das Projekt verwendet Gradle mit Kotlin DSL. Die fertige JAR entsteht mit:

```bash
./gradlew build
```

Sie liegt anschließend unter:

```text
build/libs/chunk-mob-challenge-1.0.0.jar
```

Die vorbereiteten Testserver für beide Zielversionen werden eingerichtet mit:

```bash
./scripts/setup-test-servers.sh
```

Danach lässt sich jeweils genau eine Version starten:

```bash
./scripts/start-test-server.sh 26.1.2
./scripts/start-test-server.sh 26.2
```

Beide verwenden `localhost:25565` und dürfen deshalb nicht gleichzeitig laufen.
Die vollständigen praktischen Prüfschritte und Ergebnisse stehen in
[`TESTING.md`](TESTING.md).

## Kompatibilität

| Umgebung                                     | Status                                             |
| -------------------------------------------- | -------------------------------------------------- |
| Paper 26.2 Build 121 + Java 25 auf macOS     | ✅ kompletter Solo-Run und Gameplay getestet       |
| Paper 26.1.2 Build 74 + Java 25 auf macOS    | ✅ Solo, Persistenz, Hardcore und Koop getestet     |
| Paper 26.1.0 / 26.1.1                        | ⚠️ nicht als eigene Zielversion getestet           |
| Folia / Spigot / Vanilla / Fabric / Forge    | ❌ nicht als Serverplattform unterstützt           |

Ein einziges Plugin-JAR unterstützt Paper 26.1.2 und 26.2. Neue Mobtypen aus
26.2 werden ausschließlich zur Laufzeit entdeckt und erscheinen auf 26.1.2
nicht im Pool.

## Fehler melden

Bitte gib bei einem Fehler möglichst Folgendes an:

- exakte Minecraft- und Paper-Version,
- Ausgabe von `java -version`,
- relevante Server-Konsolenmeldungen,
- Ausgabe von `/cc status`,
- aktueller Mob und Chunk,
- Schritte, mit denen sich das Problem wiederholen lässt,
- einen Screenshot, falls Border, Nametag, Glowing oder Bossbar betroffen sind.

Veröffentliche keine vollständige `state.yml`, bevor du geprüft hast, ob sie
Spieler-UUIDs oder andere Informationen enthält, die du nicht teilen möchtest.

---

<div align="center">

**Viel Erfolg – und hoffentlich wartet im nächsten Chunk keine Wither-Runde.**

</div>
