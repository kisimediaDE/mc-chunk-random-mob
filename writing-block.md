# Minecraft Chunk Mob Challenge – Anforderungen

## 1. Grundidee

Es soll ein eigenes **Paper-Plugin** für Minecraft entwickelt werden.

Unterstützte Minecraft-Versionen:

- Minecraft Java Edition 26.1
- Minecraft Java Edition 26.2
- Paper Server
- möglichst eine gemeinsame Plugin-JAR für beide Versionen

Arbeitstitel:

**Chunk Mob Challenge**

Ziel der Challenge ist es, Minecraft normal durchzuspielen und den Enderdrachen zu besiegen. Der Spieler wird dabei jedoch beim Betreten jedes Chunks mit einem zufälligen Mob konfrontiert.

---

# 2. Grundlegender Spielablauf

## Challenge starten

Die Challenge wird über einen Command gestartet, beispielsweise:

`/chunkchallenge start`

Beim Start:

1. Der aktuelle Chunk des Spielers wird ermittelt.
2. Der Spieler wird auf genau diesen Chunk beschränkt.
3. Ein zufälliger Mob aus dem konfigurierten Mob-Pool wird erzeugt.
4. Dieser Mob wird zum aktuellen Challenge-Mob.
5. Für den Mob erscheint eine Bossbar.

Beispiel:

Spieler befindet sich bei:

- Chunk X: 12
- Chunk Z: -5

Dann darf er sich nur innerhalb dieses 16×16-Blöcke-Chunks bewegen.

---

# 3. Chunk-Lock

Solange der aktuelle Challenge-Mob lebt, darf der Spieler den Chunk nicht verlassen.

Der erlaubte Bereich entspricht exakt:

`Chunk X/Z`

also einem Bereich von:

`16 × 16 Blöcken`

Die Begrenzung soll für den Spieler deutlich sichtbar sein.

Bevorzugt soll dafür die normale Minecraft-Worldborder verwendet werden, sofern sie sich technisch sinnvoll exakt auf einen Chunk anwenden lässt.

Der Spieler darf:

- nach oben bauen
- nach unten graben
- Höhlen erkunden, solange sie innerhalb des Chunks liegen

Es gibt keine Begrenzung der Y-Achse.

Nur X und Z werden beschränkt.

---

# 4. Mob besiegt

Sobald der Challenge-Mob stirbt:

1. Bossbar entfernen.
2. Chunk-Begrenzung entfernen.
3. Spieler darf den Chunk verlassen.
4. Es wird noch kein neuer Mob gespawnt.

Erst wenn der Spieler tatsächlich einen anderen Chunk betritt, beginnt die nächste Runde.

---

# 5. Nächsten Chunk betreten

Beim Wechsel von:

`Chunk A`

nach:

`Chunk B`

muss unmittelbar folgendes passieren:

1. Chunkwechsel erkennen.
2. Spieler auf Chunk B beschränken.
3. Zufälligen Mob auswählen.
4. Mob innerhalb Chunk B spawnen.
5. Bossbar anzeigen.
6. Neue Challenge-Runde beginnen.

Wichtig:

Der Spieler soll nicht erst mehrere Chunks weiterlaufen können.

Der Chunkwechsel muss deshalb möglichst unmittelbar erkannt werden.

---

# 6. Bereits besuchte Chunks

Es gibt bewusst **keinen dauerhaften Cleared-Status**.

Wenn ein Spieler einen Chunk bereits geschafft hat und später erneut betritt, startet dort erneut eine Challenge.

Beispiel:

`Chunk A → Kuh besiegt`

danach:

`Chunk B → Zombie besiegt`

danach zurück:

`Chunk A`

Dann wird Chunk A erneut gesperrt und es erscheint wieder ein zufälliger Mob.

Der neue Mob muss nicht derselbe Mob wie vorher sein.

Chunks werden also nicht dauerhaft als erledigt gespeichert.

---

# 7. Mob-Auswahl

Bei jeder neuen Runde wird genau **ein zufälliger Mob** ausgewählt.

Alle aktivierten Mobs haben zunächst die gleiche Wahrscheinlichkeit.

Beispiele:

- Cow
- Sheep
- Pig
- Chicken
- Zombie
- Skeleton
- Creeper
- Spider
- Enderman
- Blaze
- Ghast
- Guardian
- Elder Guardian
- Ravager
- Warden
- Wither
- Giant
- Illusioner
- usw.

Der Pool soll grundsätzlich möglichst viele echte, lebende Minecraft-EntityTypes enthalten.

Nicht enthalten sein sollen Dinge wie:

- Items
- Pfeile
- Boote
- Minecarts
- Item Frames
- Projectiles
- Area Effect Clouds
- Display Entities
- Experience Orbs
- Spieler
- sonstige technische Entities

Relevant sind `LivingEntity`-Typen, die tatsächlich als Gegner/Challenge gespawnt werden können.

---

# 8. Versionsabhängige Mob-Liste

Die Liste soll **nicht ausschließlich als feste Java-Liste programmiert werden**.

Stattdessen soll das Plugin beim Start prüfen, welche EntityTypes die aktuell verwendete Paper-/Minecraft-Version tatsächlich kennt.

Dadurch soll dasselbe Plugin möglichst mit Minecraft 26.1 und 26.2 funktionieren.

Vanilla-interne Mobs dürfen enthalten sein, wenn sie in der verwendeten Minecraft-/Paper-Version noch als EntityType vorhanden und spawnbar sind.

Beispiele:

- Giant
- Illusioner

Wenn ein Mob in einer späteren Minecraft-Version nicht mehr existiert, darf das Plugin daran nicht abstürzen.

Unbekannte EntityTypes aus der Konfiguration:

- ignorieren
- Warnung in der Serverkonsole ausgeben

---

# 9. Mob-Konfigurationsdatei

Beim ersten Pluginstart soll automatisch eine Datei erzeugt werden.

Beispielsweise:

`plugins/ChunkMobChallenge/mobs.yml`

oder alternativ:

`mobs.json`

Bevorzugt wird eine für Menschen einfach editierbare Datei.

Beispiel:

```yaml
mobs:
  - minecraft:cow
  - minecraft:sheep
  - minecraft:pig
  - minecraft:zombie
  - minecraft:skeleton
  - minecraft:creeper
  - minecraft:giant
  - minecraft:illusioner
  - minecraft:wither
```

Der Serveradministrator kann anschließend einzelne Zeilen entfernen.

Beispiel:

Wenn kein Wither erscheinen soll:

```yaml
mobs:
  - minecraft:cow
  - minecraft:sheep
  - minecraft:zombie
```

`minecraft:wither` wird einfach gelöscht.

Beim nächsten Serverstart darf das Plugin die entfernten Mobs **nicht erneut hinzufügen**.

---

# 10. Datei neu generieren

Wird `mobs.yml` komplett gelöscht, muss das Plugin beim nächsten Start automatisch wieder eine neue Standarddatei erzeugen.

Diese enthält wieder den vollständigen Standard-Mob-Pool der jeweiligen Minecraft-Version.

Dadurch funktioniert:

`Datei löschen → Server starten → Standardliste wiederherstellen`

---

# 11. Bossbar

Jeder Challenge-Mob erhält eine Bossbar.

Das gilt auch für normale Tiere und Monster.

Beispiele:

`Kuh`

`Zombie`

`Wither`

`Giant`

Die Bossbar zeigt mindestens:

**Name des Mobs**

und dessen aktuelle Lebenspunkte grafisch an.

Beispiel:

`Zombie`

████████████████

Bei Schaden muss sich die Bossbar entsprechend reduzieren.

Technisch:

`progress = currentHealth / maxHealth`

Beim Tod oder Entfernen des Challenge-Mobs muss die Bossbar verschwinden.

Bei echten Bossen wie Wither oder Ender Dragon darf keine störende doppelte Bossbar entstehen. Hier muss entschieden werden, ob die eigene Challenge-Bossbar zusätzlich oder anstelle der Vanilla-Bossbar verwendet wird.

---

# 12. Name des Challenge-Mobs

Der Mob sollte eindeutig erkennbar sein.

Optional kann er zusätzlich einen sichtbaren Custom Name erhalten.

Beispielsweise:

`§cChallenge: Zombie`

Die Bossbar reicht grundsätzlich aber als Kennzeichnung.

Der Challenge-Mob muss intern über PersistentDataContainer oder eine vergleichbare robuste Kennzeichnung identifiziert werden.

Nicht lediglich:

„Der erste Zombie in diesem Chunk ist der Challenge-Mob.“

---

# 13. Spawnposition

Der Mob muss innerhalb des aktuellen Chunks erscheinen.

Die Spawnposition muss so gewählt werden, dass der Mob möglichst erreichbar ist.

Der Spawnalgorithmus sollte vermeiden:

- innerhalb fester Blöcke
- Lava
- Ersticken
- direkt außerhalb des Chunks
- unerreichbare Positionen

Ideal wäre eine Position mit etwas Abstand zum Spieler.

Beispielsweise ungefähr:

`4–10 Blöcke`

vom Spieler entfernt, sofern innerhalb des Chunks möglich.

Falls keine geeignete Position gefunden wird, soll ein sinnvoller Fallback verwendet werden.

---

# 14. Challenge-Mob darf nicht verloren gehen

Der Challenge-Mob darf nicht einfach verschwinden.

Deshalb:

- Persistence aktivieren
- natürliches Despawnen verhindern
- Chunk solange nötig geladen halten oder anderweitig sicherstellen, dass das Entity erhalten bleibt

Wenn der Mob durch einen Fehler verschwindet, darf die Challenge nicht dauerhaft hängen bleiben.

Es sollte dafür eine Recovery-Strategie geben.

Beispielsweise:

`Challenge-Mob existiert nicht mehr → neuen Mob erzeugen`

anstatt den Spieler für immer einzusperren.

---

# 15. Teleportation

Während ein Chunk gesperrt ist, darf der Spieler den Lock nicht beispielsweise durch folgende Mechaniken umgehen:

- Enderperle
- Chorus Fruit
- Commands
- Netherportal
- Endportal
- Teleport durch andere Plugins

Wenn ein Teleport außerhalb des erlaubten Chunks stattfindet, muss abhängig von der Art des Teleports definiert werden, ob er verhindert oder als legitimer Dimensionswechsel behandelt wird.

Portale sind besonders wichtig, da Minecraft zum Durchspielen Nether und End benötigt.

Siehe offene Fragen.

---

# 16. Dimensionen

Die Challenge soll grundsätzlich funktionieren in:

- Overworld
- Nether
- End

Chunks werden dimensionsabhängig betrachtet.

Beispielsweise:

`Overworld Chunk 0/0`

und

`Nether Chunk 0/0`

sind unterschiedliche Challenge-Chunks.

Beim Betreten einer neuen Dimension soll ebenfalls unmittelbar eine neue Challenge-Runde entstehen.

---

# 17. Netherportal

Da Minecraft normal durchgespielt werden soll, müssen Netherportale verwendbar bleiben.

Vorgeschlagener Ablauf:

1. Spieler besiegt Mob im Overworld-Chunk.
2. Border verschwindet.
3. Spieler betritt Netherportal.
4. Spieler erscheint im Nether.
5. Zielchunk im Nether wird erkannt.
6. Chunk wird gesperrt.
7. Neuer Random Mob erscheint.

Während noch ein aktiver Challenge-Mob lebt, sollte ein Dimensionswechsel grundsätzlich verhindert werden.

---

# 18. Endportal

Gleiches Prinzip beim Endportal.

Nach Betreten des End:

1. Spieler erscheint auf der Endplattform.
2. Zielchunk wird gesperrt.
3. Random Challenge-Mob erscheint.

Damit läuft die Challenge auch im End weiter.

---

# 19. Enderdragon und Challenge-Ziel

Das normale Minecraft-Spiel soll möglichst unangetastet bleiben.

Der Vanilla-Enderdragon bleibt das eigentliche Spielziel.

Der Spieler gewinnt die Challenge, sobald der normale Enderdragon besiegt wurde.

Es sollte hierfür eine Erfolgsmeldung geben.

Beispielsweise:

`CHALLENGE GESCHAFFT!`

`Du hast den Enderdrachen besiegt.`

Optional zusätzlich:

- benötigte Zeit
- besiegte Challenge-Mobs
- betretene Chunks
- Anzahl Tode

---

# 20. Commands

Mindestens folgende Commands:

### Challenge starten

`/chunkchallenge start`

Alias optional:

`/cc start`

---

### Challenge stoppen

`/chunkchallenge stop`

Entfernt:

- Border
- Bossbar
- aktiven Challenge-Mob optional
- Challenge-State

---

### Status

`/chunkchallenge status`

Beispiel:

```text
Challenge: Aktiv
Dimension: minecraft:overworld
Chunk: 12 / -5
Mob: Zombie
HP: 13 / 20
Besiegte Mobs: 17
```

---

### Mob-Konfiguration neu laden

`/chunkchallenge reload`

Lädt `mobs.yml` neu, ohne Serverneustart.

Damit können Mobs während des Betriebs entfernt werden.

Die Änderung betrifft erst die nächste Mob-Auswahl.

---

# 21. Permissions

Beispielsweise:

```text
chunkchallenge.admin
chunkchallenge.start
chunkchallenge.stop
chunkchallenge.reload
```

OP-Spieler dürfen standardmäßig alle Commands verwenden.

---

# 22. Challenge-State

Intern sollte es einen klaren State geben.

Beispielsweise:

```java
ChallengeState {
    boolean active;
    UUID player;
    UUID challengeMob;
    UUID world;
    int lockedChunkX;
    int lockedChunkZ;
    ChallengePhase phase;
    long startedAt;
    int defeatedMobs;
    int enteredChunks;
    int deaths;
}
```

Mögliche Phasen:

```text
STOPPED
WAITING_FOR_CHUNK
LOCKED
COMPLETED
```

---

# 23. Wichtiger Unterschied: Chunk besucht vs. Runde

Eine Runde startet **bei jedem Eintritt in einen anderen Chunk**.

Das bedeutet:

A → B = neue Runde

B → A = neue Runde

A → B = wieder neue Runde

Es braucht deshalb keine Datenbank sämtlicher besuchter Chunks.

Relevant ist hauptsächlich:

`Welcher Chunk war unmittelbar vorher aktiv?`

---

# 24. Race Conditions verhindern

Der Chunkwechsel und das Spawnen müssen atomar behandelt werden.

Es darf beispielsweise nicht passieren:

1. Spieler überschreitet Chunkgrenze.
2. Event wird mehrfach ausgelöst.
3. drei Random Mobs spawnen.

Beim Eintritt in einen Chunk darf exakt **eine** neue Runde angelegt werden.

Dafür sollte ein State-/Lock-Mechanismus vorhanden sein.

---

# 25. Tod des Spielers

Der Spieler kann während einer Runde sterben.

Das Plugin muss damit umgehen können, ohne den State zu zerstören.

Das konkrete Verhalten wird noch festgelegt.

Siehe offene Fragen.

---

# 26. Tod des Mobs

Entscheidend ist der Tod des aktuell markierten Challenge-Mobs.

Normale andere Mobs im Chunk zählen nicht.

Beispiel:

Challenge-Mob:

`Zombie A`

Natürlich gespawnter Mob:

`Zombie B`

Spieler tötet Zombie B:

→ nichts passiert

Spieler tötet Zombie A:

→ Chunk freigegeben

---

# 27. Andere Todesursachen des Challenge-Mobs

Es muss festgelegt werden, ob nur ein Kill durch den Spieler zählt oder jeder Tod des Challenge-Mobs.

Beispiele:

- Fallschaden
- Lava
- Ertrinken
- Iron Golem
- andere Monster
- Explosion
- Sonnenlicht
- Border
- Void

Empfohlene Variante:

**Der Mob muss lediglich sterben. Die Todesursache ist egal.**

Das verhindert Softlocks, wenn beispielsweise ein Zombie tagsüber verbrennt.

---

# 28. Random-Auswahl

Standard:

Jeder Mob besitzt Gewicht:

`1`

Somit:

```text
Cow = gleiche Wahrscheinlichkeit
Zombie = gleiche Wahrscheinlichkeit
Warden = gleiche Wahrscheinlichkeit
Wither = gleiche Wahrscheinlichkeit
```

Später sollte die Architektur optional Gewichtungen ermöglichen.

Beispielsweise:

```yaml
mobs:
  minecraft:cow:
    enabled: true
    weight: 10

  minecraft:zombie:
    enabled: true
    weight: 10

  minecraft:wither:
    enabled: true
    weight: 1
```

Für Version 1 muss Gewichtung aber nicht zwingend verwendet werden.

---

# 29. Besondere Mobs

Die Implementierung muss berücksichtigen, dass verschiedene Mobs ungewöhnliches Verhalten besitzen.

Beispiele:

### Ghast

Kann fliegen und sich weit entfernen.

### Phantom

Kann den Chunk verlassen.

### Enderman

Kann teleportieren.

### Fish / Dolphin / Squid

Benötigen Wasser.

### Blaze

Kann fliegen.

### Wither

Kann Blöcke zerstören und fliegen.

### Warden

Kann sich eingraben/despawnen.

### Slime / Magma Cube

Teilen sich beim Tod.

Der ursprünglich gespawnte Mob ist trotzdem der Challenge-Mob.

Wenn beispielsweise ein Slime stirbt und sich in kleinere Slimes teilt, zählt der ursprüngliche Entity-Death als besiegt.

### Giant

Existiert weiterhin als EntityType, besitzt aber ungewöhnliches Vanilla-Verhalten.

### Illusioner

Existiert weiterhin als EntityType, spawnt normalerweise nicht natürlich.

Solche Entities dürfen trotzdem Bestandteil des Pools sein, sofern die Serverversion sie unterstützt.

---

# 30. Mob innerhalb des Challenge-Bereichs halten

Es sollte verhindert werden, dass der Challenge-Mob dauerhaft aus dem Chunk verschwindet.

Empfohlene Umsetzung:

Wenn der Challenge-Mob außerhalb seines Challenge-Chunks landet, wird er wieder an eine sichere Position innerhalb des Chunks teleportiert.

Damit können insbesondere:

- Ghast
- Phantom
- Enderman
- Wither
- fliegende Mobs

nicht einfach davonlaufen.

Das sollte nicht jeden Tick erfolgen, sondern beispielsweise regelmäßig oder bei relevanten Movement-/Teleport-Ereignissen.

---

# 31. Vanilla Gameplay

Das Plugin soll möglichst wenig am normalen Minecraft verändern.

Nicht verändern:

- Loot
- Crafting
- Advancements
- Hunger
- Schaden
- Mob-Drops
- Erfahrung
- Weltgeneration
- Strukturen
- Villager
- Nether
- End
- Enderdragon

Der gespawnte Challenge-Mob soll normale Vanilla-Drops und XP besitzen.

---

# 32. Mob Spawn Rules

Challenge-Mobs werden bewusst unabhängig von normalen Vanilla-Spawnregeln erzeugt.

Beispiele:

Zombie am Tag:

→ erlaubt

Blaze in Overworld:

→ erlaubt

Cow im Nether:

→ erlaubt

Warden an der Oberfläche:

→ erlaubt

Illusioner:

→ erlaubt

Giant:

→ erlaubt

Damit hängt die Zufallsauswahl nicht von:

- Biom
- Tageszeit
- Dimension
- Lichtlevel

ab.

---

# 33. Konfiguration

Neben `mobs.yml` sollte eine allgemeine `config.yml` existieren.

Beispiel:

```yaml
challenge:
  bossbar: true
  mob-custom-name: false
  keep-mob-inside-chunk: true

mob:
  min-spawn-distance: 4
  max-spawn-distance: 10

bossbar:
  title: "<red><mob>"
  show-health: true

statistics:
  enabled: true
```

Die Konfiguration muss beim ersten Start erzeugt werden.

---

# 34. Speicherung

Der Challenge-State sollte auf Platte gespeichert werden.

Grund:

Bei einem Serverneustart während einer laufenden Challenge soll die Challenge nicht kaputtgehen.

Beispielsweise:

`data.yml`

Speichern:

- Challenge aktiv/inaktiv
- Spieler UUID
- Welt
- Chunk
- aktueller Challenge-Mob
- Startzeit
- Statistik

Beim Neustart:

Wenn Challenge aktiv war:

1. State laden.
2. Prüfen, ob Challenge-Mob noch existiert.
3. Wenn ja → Bossbar/Lock wiederherstellen.
4. Wenn nein → neuen Challenge-Mob für aktuellen Chunk erzeugen.

---

# 35. Technische Zielplattform

Implementierung als:

**Paper Plugin**

Nicht als zwingend benötigte Client-Mod.

Ziel:

Vanilla-/Fabric-Clients sollen ohne zusätzliche Installation verbinden können.

Buildsystem bevorzugt:

**Gradle**

Programmiersprache:

**Java**

Eine gemeinsame Codebasis für Minecraft 26.1 und 26.2.

Keine NMS-Abhängigkeiten verwenden, wenn sich etwas sauber über Paper API lösen lässt.

Paper stellt eine versionsabhängige Entity-Type-Registry bereit; diese sollte bevorzugt werden, statt Minecraft-Entities anhand selbst gepflegter Stringlisten anzunehmen.

---

# 36. Architekturvorschlag

```text
ChunkMobChallengePlugin
│
├── ChallengeManager
│   ├── startChallenge()
│   ├── stopChallenge()
│   ├── enterChunk()
│   ├── completeCurrentRound()
│   └── restoreChallenge()
│
├── ChunkLockManager
│   ├── lockChunk()
│   ├── unlockChunk()
│   └── validatePlayerPosition()
│
├── ChallengeMobManager
│   ├── selectRandomMob()
│   ├── spawnMob()
│   ├── removeMob()
│   ├── validateMobPosition()
│   └── recoverMissingMob()
│
├── MobRegistry
│   ├── discoverAvailableMobs()
│   ├── generateDefaultConfig()
│   └── loadConfiguredMobs()
│
├── BossBarManager
│
├── ChallengeStorage
│
├── StatisticsManager
│
└── Commands
```

---

# 37. Events

Voraussichtlich relevante Events:

```text
PlayerMoveEvent
PlayerTeleportEvent
PlayerChangedWorldEvent
PlayerPortalEvent
EntityDeathEvent
PlayerDeathEvent
EntityTeleportEvent
EntityDamageEvent
EntityRemoveFromWorldEvent / Paper entsprechende API
ChunkUnloadEvent
Server startup/shutdown
```

Beim `PlayerMoveEvent` nicht bei jeder kleinen Positionsänderung komplette Logik ausführen.

Nur reagieren wenn:

```text
oldChunkX != newChunkX
oder
oldChunkZ != newChunkZ
```

---

# 38. Performance

Das Plugin soll keine dauerhaften schweren Tick-Loops verwenden.

Insbesondere:

- nicht ständig sämtliche Entities durchsuchen
- Challenge-Mob direkt per UUID referenzieren
- Chunkwechsel mathematisch feststellen
- Bossbar nur bei tatsächlicher HP-Änderung aktualisieren oder effizient eventbasiert
- Mob-Position nur mit sinnvoller Frequenz kontrollieren

Ziel ist ein extrem geringer Performance-Impact.

---

# 39. Logging

Sinnvolle Servermeldungen:

```text
[ChunkMobChallenge] Challenge started by Simon
[ChunkMobChallenge] Locked chunk 12/-5 in minecraft:overworld
[ChunkMobChallenge] Spawned minecraft:illusioner
[ChunkMobChallenge] Challenge mob defeated
[ChunkMobChallenge] Entered chunk 13/-5
```

Fehler:

```text
[ChunkMobChallenge] Unknown configured entity type: minecraft:xyz
[ChunkMobChallenge] Entity is not a LivingEntity: minecraft:arrow
```

---

# 40. Definition of Done

Die erste Version gilt als fertig, wenn:

1. Plugin auf Paper 26.1 startet.
2. Plugin auf Paper 26.2 startet.
3. `/chunkchallenge start` funktioniert.
4. Aktueller Chunk wird gesperrt.
5. Random Mob erscheint.
6. Bossbar erscheint.
7. Bossbar reagiert auf HP.
8. Mob-Tod hebt Sperre auf.
9. Betreten eines anderen Chunks startet unmittelbar nächste Runde.
10. Rückkehr in alten Chunk startet ebenfalls eine neue Runde.
11. `mobs.yml` wird automatisch erstellt.
12. Entfernte Mobs bleiben entfernt.
13. Löschen von `mobs.yml` erzeugt wieder Standardliste.
14. Giant und Illusioner funktionieren, sofern von der jeweiligen Serverversion unterstützt.
15. Nether funktioniert.
16. End funktioniert.
17. Serverneustart erzeugt keinen Softlock.
18. Challenge kann gestoppt werden.
19. Vanilla-Enderdragon kann regulär besiegt werden.
20. Besiegen des Enderdrachen kann die Challenge als erfolgreich abschließen.