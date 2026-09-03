# Chunk Mob Challenge – manuelle Testcheckliste

Diese Checkliste testet dieselbe Plugin-JAR auf Paper 26.1.2 und 26.2. Alle
Tests sollten zunächst auf 26.1.2 und danach noch einmal auf 26.2 ausgeführt
werden.

## Aktueller Teststand

Letzte Aktualisierung: **3. September 2026**

- Manuell getestet: **Paper 26.2 Build 121**, Solo mit `playmonkeei`
- Paper **26.1.2 Build 74**: Server- und Pluginstart mit 89 zur Laufzeit
  erkannten EntityTypes bestätigt; Start-Smoke-Test mit Hoglin, Bossbar,
  Chunkborder und standardmäßig deaktivierten Nametags/Glowing bestanden. Ein
  Command-Teleport aus dem Chunk wurde serverseitig blockiert; die natürliche
  Hoglin-zu-Zoglin-Transformation übertrug die aktive Runde korrekt. Nach einem
  Serverneustart waren Spielerposition, Blickrichtung, Zoglin-UUID und 38 HP
  exakt erhalten; Offline-Zeit wurde nicht mitgezählt. Zoglin-Sieg und atomarer
  Start einer zweiten Runde wurden ebenfalls live bestätigt. Auch eine
  Creeper-Selbstexplosion beendete exakt einmal die Runde ohne Recovery.
- Automatisch gebaut/getestet: **Paper API 26.1.2 Build 74**
- Gegenkompiliert: **Paper API 26.2 Build 121**
- `[x]` bedeutet praktisch bestätigt; `[ ]` bleibt offen oder muss auf der
  zweiten Serverversion erneut geprüft werden.
- Einziger noch offener Funktionsblock: Koop beziehungsweise Verhalten mit zwei
  gleichzeitig verbundenen Spielern.
- Der vollständige Solo-Run auf Paper 26.2 wurde nach **02:43:39 aktiver
  Spielzeit** mit **42 Mobs**, **56 Runden** und **15 Toden** durch den echten
  Vanilla-Enderdragon abgeschlossen.

## 1. Testserver vorbereiten

Voraussetzungen:

- Java 25
- Minecraft-Java-Clients für 26.1.2 und 26.2
- Zustimmung zu [Mojangs EULA](https://aka.ms/MinecraftEULA)

Einmalig beide Paper-Versionen herunterladen, Prüfsummen kontrollieren, das
Plugin bauen und in beide Server kopieren:

```bash
./scripts/setup-test-servers.sh
```

Paper 26.1.2 starten:

```bash
./scripts/start-test-server.sh 26.1.2
```

Verbindung: `localhost` beziehungsweise `127.0.0.1:25565`

Paper 26.2 starten:

```bash
./scripts/start-test-server.sh 26.2
```

Verbindung: `localhost` beziehungsweise `127.0.0.1:25565`

Beide Testserver benutzen den Standardport 25565 und müssen deshalb nacheinander,
nicht gleichzeitig, gestartet werden.

Beim ersten Beitritt in der Serverkonsole ausführen:

```text
op playmonkeei
```

Wichtig: Einen laufenden Persistenztest immer mit dem Server-Konsolenbefehl
`stop` pausieren. `/cc stop` beendet und löscht den Challenge-Run absichtlich.

## 2. Installation und Mobpool

- [x] Server startet ohne Plugin-Stacktrace.
- [x] Im Log steht `ChunkMobChallenge ist bereit.`.
- [x] `/plugins` zeigt `ChunkMobChallenge` grün an (auf Paper 26.2 und 26.1.2
      bestätigt).
- [x] `plugins/ChunkMobChallenge/mobs.yml` wurde automatisch erzeugt.
- [x] Die Datei enthält echte Mobs, unter anderem Cow, Zombie, Wither und
      Enderdragon, aber keine Items, Pfeile, Boote oder Displays.
- [x] `/cc status` meldet vor dem Start, dass keine Challenge vorhanden ist
      (auf Paper 26.1.2 bestätigt).

## 3. Solo-Grundablauf

- [x] `/cc start` erzeugt genau einen Mob im aktuellen Chunk.
- [x] Der Mob besitzt standardmäßig keinen sichtbaren Nametag und leuchtet nicht.
- [x] Bei normalen Mobs erscheint eine Bossbar und folgt den Lebenspunkten.
- [x] Ein Challenge-Enderdragon zeigt genau eine Bossbar, auch wenn Paper nach
      Logout oder Serverneustart keine native Bossbar mehr bereitstellt.
- [x] Ein Challenge-Wither zeigt genau eine Vanilla-Bossbar, auch nach Logout und
      Serverneustart; UUID, Runde und Angriffszustand bleiben erhalten.
- [x] Die Border umfasst exakt den aktuellen 16×16-Chunk.
- [x] Bauen nach oben und Graben nach unten bleibt möglich.
- [x] Die Chunkgrenze kann zu Fuß und sprintend nicht überschritten werden.
- [x] Die Chunkgrenze kann mit Elytra nicht überschritten werden.
- [x] Enderperle und Chorus Fruit können den aktiven Chunk nicht verlassen.
- [x] `/tp` aus dem aktiven Chunk wird blockiert. Die Vanilla-Erfolgsmeldung kann
      erscheinen, die gemessene Spielerposition bleibt jedoch im Challenge-Chunk.
- [x] Natürlich gespawnte beziehungsweise unmarkierte Mobs können die Chunkgrenze
      frei überqueren (Zombified Piglin bis `X=-0.783`).
- [x] Der Tod eines unmarkierten normalen Mobs löst keinen Rundensieg aus und
      verändert die aktive Challenge-Runde nicht.
- [x] Der Challenge-Mob kann den Chunk nicht verlassen.
- [x] An der Chunkkante bleibt der Challenge-Mob beweglich und wird nach innen
      zurückgelenkt, statt festzustecken.
- [x] Nach dem Tod des Challenge-Mobs verschwinden Border und Bossbar.
- [x] Der Spieler kann sich danach frei im aktuellen Chunk bewegen.
- [x] Erst der Eintritt in einen anderen Chunk startet genau eine neue Runde.
- [x] Rückkehr in einen früher besuchten Chunk startet erneut eine Runde.

## 4. Beliebige Todesursache und Sondermobs

- [x] Ein direkter Spieler-Kill beendet die Runde.
- [x] Eine andere tatsächliche Todesursache beendet die Runde ebenfalls:
      Zombie Horse durch Sonnenbrand und Hoglin durch Piglin bestätigt; weitere
      Ursachen bleiben optional.
- [x] Ein Challenge-Creeper beendet seine Runde durch die eigene Explosion und
      wird vom Recovery-Wächter nicht erneut erzeugt.
- [x] Ein Wasser-Mob darf an Land erscheinen und sein Ersticken zählt (Nautilus
      bestätigt).
- [x] Bei einer normalen Entity-Transformation wird die Challenge-Markierung auf
      den neuen Mob übertragen; es entsteht weder ein Sieg noch eine zweite Runde.
- [x] Bei der Transformation werden Bossbar und sichtbarer Nametag auf den neuen
      EntityType aktualisiert (nach Neustart überall `Villager`).
- [x] Enderman, Ghast und Phantom bleiben trotz Teleport-/Flug-KI im Chunk.
      Das Phantom flog mit derselben UUID bis nahe an die nordöstliche Kante und
      anschließend wieder nach innen, blieb beweglich und griff weiter an.
- [x] Giant und Illusioner können aus `mobs.yml` geladen und gespawnt werden;
      Bossbar, Markierung und jeweiliges Vanilla-Verhalten bleiben funktionsfähig.
      Der Illusioner setzte auf `hard` zusätzlich korrekt Blindheit ein.
- [x] Ein zufälliger Enderdragon kann als Challenge-Mob gespawnt werden.
- [x] Wither und Warden können als gleich gewichtete Challenge-Mobs erscheinen,
      greifen normal an und bleiben innerhalb ihrer Chunkgrenze.
- [x] Normale Mobs erscheinen auf einer sicheren Bodenfläche, nicht auf Blättern,
      Baumstämmen oder zufällig in einer Höhle.
- [x] Im Nether erscheinen neue Challenge-Mobs auf einer vom Spieler erreichbaren
      Bodenfläche nahe seiner Höhe und nicht auf der oberen Bedrock-Decke.
- [x] Ein bereits auf der Nether-Decke gespeicherter Challenge-Mob wird vom
      Recovery-Wächter zurück in den Spielbereich versetzt (Hoglin von `Y=128`
      auf `Y=63`).
- [x] Große Mobs wie Ghasts werden nicht zwischen Bäumen oder in anderen zu engen
      Blockräumen gespawnt; geraten sie später hinein, werden sie befreit. Nach
      dem Fix bewegte sich dieselbe Ghast-UUID innerhalb von 27 Sekunden frei von
      `[1314.34, 77.54, 2429.18]` nach `[1314.77, 80.90, 2428.94]`.
- [x] Ein Challenge-Enderdragon erscheint oberhalb des höchsten Geländes im Chunk
      und wird nicht unter die Oberfläche gedrückt.
- [x] Ein Challenge-Enderdragon außerhalb eines Vanilla-DragonBattle wählt einen
      Teilnehmer als Ziel, reagiert auf Pfeil-/Nahkampfschaden und greift mit
      Drachenfeuerbällen an.
- [x] Drachenfeuerbälle entstehen sichtbar unterhalb der Dragon-Hitbox, fliegen
      bis zum Boden und erzeugen dort die Atemwolke; vorherige Wolken verschwinden
      beim nächsten Schuss.
- [x] Ein Challenge-Enderdragon bleibt in einem kontrollierten Hover über der
      Chunkmitte, ist zuverlässig treffbar und wird beim Kampf nicht ständig von
      der Border zurückteleportiert.
- [x] Ein Challenge-Enderdragon ist auf halbe Größe skaliert und seine gesamte
      Hitbox bleibt innerhalb der Chunkgrenze.
- [x] Der Tod eines zufällig gespawnten Enderdragons beendet nicht den Run.

Tipp für gezielte Tests: Vor dem Serverstart in `mobs.yml` vorübergehend nur den
gewünschten Mob stehen lassen. Danach `/cc reload` verwenden. Die Änderung gilt
erst für die nächste Runde.

## 5. Wichtigster Test: Abend stoppen, morgen fortsetzen

1. Mit `/cc start` eine Runde starten.
2. Dem Mob Schaden zufügen, ihn aber nicht töten.
3. `/cc status` ausführen und folgende Werte notieren:
   - Spielerwelt und XYZ-Position
   - Blickrichtung
   - aktueller Chunk
   - Mobtyp und Mob-Lebenspunkte
   - Challenge-Spielzeit
   - besiegte Mobs, Runden und Tode
4. Optional besondere Mob-Ausrüstung oder Variante notieren.
5. In der Serverkonsole `stop` eingeben.
6. Prüfen, dass `plugins/ChunkMobChallenge/state.yml` existiert.
7. Mindestens einige Minuten warten; diese Zeit darf nicht zum Timer zählen.
8. Denselben Server mit demselben Startscript erneut starten und einloggen.

Erwartungen:

- [x] Spieler erscheint in derselben Welt an derselben XYZ-Position.
- [x] Blickrichtung entspricht dem gespeicherten Wert.
- [x] Derselbe Chunk ist wieder gesperrt.
- [x] Derselbe EntityType mit denselben HP ist vorhanden.
- [x] Die Mob-Variante ist erhalten (Zombie Villager: Plains, Shepherd, Level 1).
- [x] Vorhandene Mob-Ausrüstung ist erhalten.
- [x] Bossbar und Border sind wieder sichtbar.
- [x] Statistiken entsprechen nach Logout und Serverneustart dem gespeicherten State.
- [x] Die Offline-Zeit wurde nicht zur Challenge-Spielzeit addiert.
- [x] Es existiert genau ein Challenge-Mob, kein Duplikat.
- [x] Der Kampf lässt sich nach Logout und Serverneustart weiterführen.

Den gleichen Ablauf zusätzlich testen, indem zuerst ausgeloggt, der Server noch
kurz weiterlaufen gelassen und erst danach mit `stop` beendet wird.

## 6. Nicht-Hardcore-Tod

- [x] Allein in einer Runde sterben: Alte verwaiste Runde und Mob werden entfernt.
- [x] Nach dem Respawn startet im Respawn-Chunk sofort eine neue Runde.
- [x] Tode-Statistik erhöht sich um eins.
- [ ] Zwei Spieler teilen eine Runde; einer stirbt: Mob und Runde bleiben für den
      Überlebenden bestehen.
- [ ] Der gestorbene Spieler startet nach dem Respawn seine eigene Runde oder
      tritt einer dort bereits aktiven Runde bei.

## 7. Hardcore-Tod

Für diesen Test `hardcore=true` in der jeweiligen `server.properties` setzen und
eine frische Testwelt verwenden.

Hinweis zum Test auf Paper 26.1.2: Der erste Versuch mit der bereits vorhandenen
Nicht-Hardcore-Welt war nicht wertbar. Obwohl `hardcore=true` in
`server.properties` stand, blieb die laufende Welt eine Nicht-Hardcore-Welt und
der Spieler konnte normal respawnen. Der gültige Test wird deshalb mit der neuen
separaten Welt `world-hardcore` und Schwierigkeit `hard` durchgeführt.

- [x] Paper 26.1.2 erzeugt für den gültigen Test eine neue Welt: Im Startlog stehen
      `No existing world data, creating new world` und
      `Preparing level "world-hardcore"`.
- [x] Kontroll-Run startet in der frischen Welt regulär: Pufferfish-Runde,
      `RUNNING`, Spielzeit `00:00:02`, 0 Mobs, 1 Runde und 0 Tode.
- [x] Der Client zeigt in dieser Welt die Hardcore-Herzen.

- [x] Der Tod von `playmonkeei` beendet den gesamten Run unmittelbar mit
      `RUN GESCHEITERT`; Spielzeit `00:00:34`, 0 Mobs, 1 Runde und 1 Tod.
- [x] Worldborder und Bossbar werden nach der Hardcore-Niederlage entfernt.
- [x] Ein überlebender Challenge-Mob wird normalisiert und bleibt bestehen: Die
      Cow mit derselben UUID lebt nach der Hardcore-Niederlage weiter,
      `BukkitValues` und `CustomName` fehlen, `PersistenceRequired` ist `0b` und
      Glowing ist nicht mehr aktiv.
- [x] `/cc status` zeigt `FAILED`, Spielzeit `00:00:34`, 0 Mobs, 1 Runde,
      1 Tod und 0 aktive/pausierte Chunk-Runden.

Der Pufferfish war bei den anschließenden Abfragen nach `BukkitValues` und
`PersistenceRequired` nicht mehr im Umkreis vorhanden. Ein erster gezielter
Cow-Nachtest wurde versehentlich in der bereits
wieder aktivierten normalen Welt gestartet und ist für den Hardcore-Punkt nicht
wertbar. Sein Ausgangsstatus war `RUNNING`, 0 Mobs, 1 Runde, 0 Tode. Die Kuh
besitzt dort UUID
`1165974014/-1984936792/-2115810426/578803494`, beide Challenge-PDC-IDs,
`PersistenceRequired: 1b` und `Glowing: 1b`.

Der gültige Cow-Nachtest läuft nach einem bestätigten Start von
`world-hardcore`: `RUNNING`, Cow-Runde im Chunk `-1/-1`, Nametags und Glowing
aktiviert. Referenz-UUID der Kuh:
`-1708053565/2058436661/-1541867906/1894832394`.
Nach der Niederlage meldete `/cc status` `FAILED`; dieselbe Cow-UUID blieb
vorhanden, während Challenge-PDC und CustomName fehlten,
`PersistenceRequired: 0b` war und kein aktiver Glowing-Wert mehr vorlag.

Danach `hardcore=false` zurücksetzen.

## 8. Koop und parallele Runden

- [ ] Beim Start werden alle Online-Spieler zum Starter teleportiert.
- [ ] Spieler im selben aktiven Chunk teilen Mob und Bossbar.
- [ ] Nach dem Sieg bleibt Spieler A im sicheren Chunk.
- [ ] Spieler B betritt Chunk B und startet dort eine Runde.
- [ ] Spieler A betritt stattdessen Chunk C und startet parallel eine zweite Runde.
- [ ] Beide Runden besitzen getrennte Mobs, Borders und Bossbars.
- [ ] Betritt ein Spieler einen bereits umkämpften Chunk, tritt er der vorhandenen
      Runde bei und erzeugt keinen zweiten Mob.
- [ ] Loggt der letzte Spieler einer Runde aus, friert diese Runde ein.
- [ ] Andere parallele Runden laufen weiter.
- [ ] Beim erneuten Login wird der Spieler seiner ursprünglichen Runde zugeordnet.

## 9. Portale und Dimensionen

- [x] Ein Command-Teleport in eine andere Dimension wird während einer aktiven
      Runde blockiert; die tatsächliche Spieler-Dimension bleibt unverändert.
- [x] Nach dem Mob-Tod funktioniert derselbe Command-Dimensionswechsel und startet
      im Zielchunk sofort genau eine neue Runde.
- [x] Ein Netherportal kann während einer aktiven Runde nicht benutzt werden.
- [x] Nach dem Mob-Tod funktioniert das Netherportal.
- [x] Im Nether-Zielchunk startet sofort eine neue Runde.
- [x] Rückkehr in die Overworld startet am Ziel ebenfalls eine Runde.
- [x] Ein Endportal kann während einer aktiven Runde nicht benutzt werden.
- [x] Nach dem Mob-Tod funktioniert dasselbe Endportal und startet im End eine
      neue Runde.
- [x] Overworld- und Nether-Chunks mit denselben X/Z-Koordinaten werden als
      unterschiedliche Runden behandelt; das End bleibt noch offen.

## 10. Enderdragon und Sieg

- [x] Der normale DragonBattle-Enderdragon im End bleibt vorhanden; Paper meldete
      beim ersten Eintritt ausdrücklich, dass er noch nicht getötet wurde.
- [x] Ein zufälliger Challenge-Enderdragon kann getötet werden, ohne den Run zu
      beenden.
- [x] Nur der Tod des vom Vanilla-DragonBattle verwalteten Enderdragons erzeugt
      `CHALLENGE GESCHAFFT!`.
- [x] Abschlussmeldung zeigt Spielzeit, Mobs, Runden und Tode (bestätigt mit
      `02:43:39`, 42 Mobs, 56 Runden und 15 Toden).
- [x] Borders und Bossbars werden nach dem Sieg entfernt; weitere Chunkwechsel
      starten bei Status `COMPLETED` keine neue Runde.

## 11. Konfiguration und Commands

- [x] Einen Mob aus `mobs.yml` entfernen und `/cc reload` ausführen: Er wird in
      zukünftigen Runden nicht mehr gewählt.
- [x] Unbekannten Key eintragen: Konsolenwarnung, aber kein Plugin-Absturz.
- [x] Doppelten Key eintragen: Konsolenwarnung, nur einmal im Pool.
- [x] Nur ungültige Einträge eintragen: Reload wird abgelehnt und der vorherige
      Pool bleibt aktiv.
- [x] `mobs.yml` bei gestopptem Server löschen: Beim nächsten Start wird die
      vollständige versionsabhängige Liste neu erzeugt.
- [x] `/cc status` zeigt eigene Runde und globale Statistiken.
- [x] `/cc tags disable` blendet aktuelle und zukünftige Mob-Nametags aus.
- [x] `/cc tags enable` blendet die Nametags wieder ein.
- [x] Die Nametag-Einstellung bleibt nach Logout und Serverneustart erhalten.
- [x] `/cc glowing enable` hebt aktuelle und zukünftige Challenge-Mobs durch
      Wände sichtbar hervor.
- [x] `/cc glowing disable` entfernt den Leuchteffekt wieder.
- [x] Die Glowing-Einstellung bleibt nach Logout und Serverneustart erhalten.
- [x] Nach einem Rundensieg tragen verbleibende Split-/Transformationsmobs keinen
      Glowing-Effekt mehr.
      Gezielter Nachtest auf Paper 26.1.2 gestartet: Magma-Cube-Runde im Chunk
      `-8/-6`, Nametags und Glowing aktiviert. Der Elternmob wurde kontrolliert
      auf `Size: 3` gesetzt und besitzt UUID
      `97312321/78006272/-1803271013/-1447317515`, `Glowing: 1b` sowie beide
      Challenge-PDC-IDs. Nach seinem Tod entstanden vier Magma Cubes; die drei
      Split-Nachkommen hatten keine PDC-, CustomName- oder Glowing-Ausgabe und
      `PersistenceRequired: 0b`. Der einzelne markierte Würfel gehörte nach einem
      Chunkwechsel bereits zur neuen Runde 2 und trug eine andere Round-ID
      (`e80e14a4-...` statt `8ed2cb60-...`). Visuell waren an den Split-Würfeln
      weder Nametag noch Glowing vorhanden; Border und Bossbar verschwanden und
      es erschien genau eine Siegesmeldung.
- [x] Kleine Slime-/Magmawürfel verlieren nach dem Tod des Challenge-Elternmobs
      Nametag, PDC-Markierung und künstliche Persistenz.
- [x] Bereits gespeicherte Legacy-Namen wie `Challenge: Magma Cube` werden beim
      Laden ihres Chunks entfernt.
- [x] Drachenfeuerbälle einer Runde werden bei Sieg, Todauflösung oder `/cc stop`
      entfernt und können danach keinen Spieler mehr treffen.
- [x] `/cc stop` entfernt State, Border, Bossbar und Markierungen.
- [x] Nach `/cc stop` bleiben noch lebende Challenge-Entities als normale Mobs.
- [x] Nach `/cc stop` existiert keine `state.yml` mehr.

## 12. Testergebnis dokumentieren

Für jeden Fehler notieren:

- Paper-Version und Build
- Plugin-Version
- Einzelspieler oder Anzahl Koop-Spieler
- ausgeführte Schritte
- erwartetes und tatsächliches Verhalten
- relevante Konsolenmeldungen/Stacktrace
- Inhalt von `state.yml` und betroffener `mobs.yml`, sofern relevant
