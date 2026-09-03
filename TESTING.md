# Chunk Mob Challenge – manuelle Testcheckliste

Diese Checkliste testet dieselbe Plugin-JAR auf Paper 26.1.2 und 26.2. Alle
Tests sollten zunächst auf 26.1.2 und danach noch einmal auf 26.2 ausgeführt
werden.

## Aktueller Teststand

Letzte Aktualisierung: **1. September 2026**

- Manuell getestet: **Paper 26.2 Build 121**, Solo mit `playmonkeei`
- Automatisch gebaut/getestet: **Paper API 26.1.2 Build 74**
- Gegenkompiliert: **Paper API 26.2 Build 121**
- `[x]` bedeutet praktisch bestätigt; `[ ]` bleibt offen oder muss auf der
  zweiten Serverversion erneut geprüft werden.
- Wichtigste noch offene Blöcke: Paper-26.1.2-Livetest, Koop, Hardcore,
  Portale/Dimensionen und echter Vanilla-Enderdrachen-Sieg.

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
- [ ] `/plugins` zeigt `ChunkMobChallenge` grün an.
- [x] `plugins/ChunkMobChallenge/mobs.yml` wurde automatisch erzeugt.
- [x] Die Datei enthält echte Mobs, unter anderem Cow, Zombie, Wither und
      Enderdragon, aber keine Items, Pfeile, Boote oder Displays.
- [ ] `/cc status` meldet vor dem Start, dass keine Challenge vorhanden ist.

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
- [x] Ein Challenge-Enderdragon ist auf halbe Größe skaliert und seine gesamte
      Hitbox bleibt innerhalb der Chunkgrenze.
- [ ] Der Tod eines zufällig gespawnten Enderdragons beendet nicht den Run.

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

- [ ] Der Tod eines Teilnehmers beendet den gesamten Run.
- [ ] Alle Borders und Bossbars werden entfernt.
- [ ] Übrige Challenge-Mobs verlieren Markierung und künstliche Persistenz, bleiben
      aber als normale Mobs bestehen.
- [ ] `/cc status` zeigt den Run als `FAILED`.

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
- [ ] Dasselbe Verhalten gilt für das Endportal.
- [x] Overworld- und Nether-Chunks mit denselben X/Z-Koordinaten werden als
      unterschiedliche Runden behandelt; das End bleibt noch offen.

## 10. Enderdragon und Sieg

- [ ] Der normale DragonBattle-Enderdragon im End bleibt vorhanden.
- [ ] Ein zufälliger Challenge-Enderdragon kann getötet werden, ohne den Run zu
      beenden.
- [ ] Nur der Tod des vom Vanilla-DragonBattle verwalteten Enderdragons erzeugt
      `CHALLENGE GESCHAFFT!`.
- [ ] Abschlussmeldung zeigt Spielzeit, Mobs, Runden und Tode.
- [ ] Borders und Bossbars werden nach dem Sieg entfernt.

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
- [ ] Nach einem Rundensieg tragen verbleibende Split-/Transformationsmobs keinen
      Glowing-Effekt mehr.
- [x] Kleine Slime-/Magmawürfel verlieren nach dem Tod des Challenge-Elternmobs
      Nametag, PDC-Markierung und künstliche Persistenz.
- [x] Bereits gespeicherte Legacy-Namen wie `Challenge: Magma Cube` werden beim
      Laden ihres Chunks entfernt.
- [ ] Drachenfeuerbälle einer Runde werden bei Sieg, Todauflösung oder `/cc stop`
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
