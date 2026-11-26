# Rush Hour – Kétjátékosos konzolos kiadás

Kétjátékosos, parancssori **Rush Hour** logikai játék **Java 21** és **Maven** alapokon, rétegzett architektúrával, külső JSON pályakezeléssel, menet közbeni játékállás-mentéssel és beágyazott **SQLite** toplistával.

---

## Főbb funkciók

* **Kétjátékosos, körökre osztott játékmenet:**
  * **1. játékos** céljárműve az **`A`** (vízszintes tájolás, kijárat jobbra: `==> EXIT A`).
  * **2. játékos** céljárműve a **`B`** (függőleges tájolás, kijárat lefelé: `EXIT B`).
  * A játékosok felváltva lépnek: a saját körükben mozgathatják a saját céljárművüket vagy bármelyik akadályt, **az ellenfél céljárművét azonban nem**.
  * Az a játékos nyer, aki elsőként kijuttatja a saját céljárművét a tábláról.
* **N × M méretű pályák és szigorú validáció:**
  * Tetszőleges téglalap alakú táblák (minimum `4×4`).
  * Ütközés-, pályahatár- és irányvalidáció.
  * Az `1×1` méretű járművek tiltottak (minden jármű hossza legalább 2).
* **Külső JSON pályák betöltése:**
  * A beépített pályák külső JSON fájlokból töltődnek be (`src/main/resources/levels/`).
  * A főmenüből tetszőleges saját `.json` pályafájl is betölthető elérési út megadásával.
* **Mentés és betöltés név alapján (`saves/<név>.json`):**
  * Játék közben bármikor elmenthető az állás megadott néven.
  * A mentés tartalmazza a tábla teljes állapotát, mindkét játékos nevét, a lépésszámot és azt is, **melyik játékos van éppen lépésen** (`currentPlayer`).
* **SQLite toplista és játékosrangsor:**
  * Győzelem esetén a győztes eredménye automatikusan bekerül a helyi `rush-hour-scores.db` adatbázisba.
  * Lekérdezhető a legkevesebb lépésből elért toplista és a győzelmek száma szerinti rangsor.
* **Naplózás:** SLF4J + Logback naplózás fájlba (`rush-hour.log`).

---

## Technológiai stack

| Terület | Technológia |
| --- | --- |
| Nyelv és JDK | Java 21 |
| Build és csomagolás | Maven, Maven Assembly Plugin (Fat JAR) |
| JSON kezelés | Jackson (`jackson-databind`) |
| Adatbázis | SQLite (`sqlite-jdbc`) |
| Naplózás | SLF4J + Logback |
| Tesztelés | JUnit 5, Mockito |
| Kódminőség és lefedettség | Checkstyle (`checkstyle.xml`), JaCoCo (80%+ elvárt küszöb) |

---

## Architektúra és tervezési minták

A projekt felelősségi körök szerint négy fő csomagra tagolódik (`hu.nye.progtech.rushhour`):

* **`model`**: Megváltoztathatatlan (immutable) tartományi modell (`Board`, `Vehicle`, `Position`, `Orientation`, `Direction`, `PlayerTurn`, `GameState`).
* **`service`**: Üzleti logika és lépésvalidáció (`GameService`, `MoveValidator`, `StandardMoveValidator`, `Move`, `MoveResult`, `InvalidMoveException`, `LevelDefinition`, `LevelFactory`).
* **`persistence`**: JSON pályabetöltés, játékállás-mentés és SQLite adatbázis-elérés (`LevelLoader`, `JsonLevelLoader`, `GameStateRepository`, `JsonGameStateRepository`, `ScoreRepository`, `SqliteScoreRepository`, `GamePersistenceService`, `ScoreEntry`, `PlayerRanking`).
* **`ui`**: Konzolos megjelenítés, parancsértelmezés és interaktív vezérlés (`BoardRenderer`, `CommandParser`, `GameCommand`, `ConsoleGameController`).

### Alkalmazott tervezési minták
* **Builder:** `Board.Builder` a validált, megváltoztathatatlan táblák létrehozásához.
* **Strategy:** `MoveValidator` interfész és `StandardMoveValidator` implementáció a lépésszabályok leválasztására.
* **Factory:** `LevelFactory` és a `GameCommand` statikus gyári metódusai.
* **Repository:** `GameStateRepository` és `ScoreRepository` az adattárolás absztrakciójára.
* **DTO:** `SavedGameDto`, `SavedVehicleDto` a JSON szerializációhoz.
* **Dependency Injection:** Konstruktoron keresztüli függőséginjektálás a szerviz-, perzisztencia- és UI-rétegek között (a `Main` osztályban összekötve).

---

## Fordítás, tesztelés és futtatás

### Előfeltételek
* **Java 21 (JDK 21)**
* **Maven 3.9+**

### Build, Checkstyle, tesztek és JaCoCo riport
```bash
mvn clean install
```

A parancs sorban lefuttatja:
1. a **Checkstyle** kódstílus-ellenőrzést,
2. az összes **JUnit 5 / Mockito** egység- és integrációs tesztet (52 teszt),
3. a **JaCoCo** tesztlefedettségi ellenőrzést (elvárt minimum: 80%, elért utasítás-lefedettség: **89%**),
4. a futtatható, függőségekkel egybecsomagolt **Fat JAR** elkészítését.

A JaCoCo HTML riport a build után itt érhető el:
```text
target/site/jacoco/index.html
```

### Alkalmazás indítása Fat JAR-ból
```bash
java -jar target/rush-hour-game-1.0-SNAPSHOT-jar-with-dependencies.jar
```

*(Ha a rendszer alapértelmezett `java` parancsa régebbi verzióra mutat, a Java 21-es `java` bináris teljes elérési útjával, vagy közvetlenül IntelliJ IDEA-ból a `Main` osztály futtatásával indítható.)*

---

## Konzolos parancsok játék közben

| Parancs | Példa | Leírás |
| --- | --- | --- |
| `<jármű> <irány> [lépés]` | `A jobbra 2`, `B le 1`, `C fel` | Jármű mozgatása (irányok: `fel`/`le`/`balra`/`jobbra`, `up`/`down`/`left`/`right` vagy `f`/`l`/`b`/`j`) |
| `move <jármű> <irány> [lépés]` | `move B down 3` | Lépés teljes parancsformával |
| `save <név>` / `mentes <név>` | `save esti-meccs` | Játékállás és aktuális lépő mentése `saves/<név>.json` fájlba |
| `load <név>` / `betoltes <név>` | `load esti-meccs` | Korábban mentett állás betöltése név alapján |
| `moves` / `lepesek` | `moves` | Az aktuális játékos szabályos lépéseinek listázása |
| `scores` / `toplista` | `scores` | SQLite toplista és játékosrangsor megjelenítése |
| `help` / `sugo` | `help` | Parancsok súgója |
| `exit` / `kilepes` | `exit` | Aktuális meccs megszakítása és visszatérés a főmenübe |

---

## Külső JSON pályafájl formátuma

A főmenü **2.** menüpontjával tetszőleges, az alábbi szerkezetű `.json` pályafájl betölthető:

```json
{
  "id": 9,
  "name": "Sajat palya",
  "difficulty": "Kezdo",
  "rows": 5,
  "cols": 6,
  "vehicles": [
    {
      "id": "A",
      "row": 2,
      "col": 0,
      "length": 2,
      "orientation": "HORIZONTAL",
      "target": true
    },
    {
      "id": "B",
      "row": 0,
      "col": 3,
      "length": 2,
      "orientation": "VERTICAL",
      "target": true
    },
    {
      "id": "C",
      "row": 1,
      "col": 2,
      "length": 2,
      "orientation": "VERTICAL",
      "target": false
    }
  ]
}
```
