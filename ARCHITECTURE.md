Architektúra Döntések (ADR)

Ez a dokumentum a "Cross-Company Data Mapper" projekt során hozott magas szintű technológiai és architekturális döntéseket indokolja.

1. Döntés: Mikroszolgáltatás-alapú felépítés

A rendszert három különálló szolgáltatásra bontottuk (Mapping, Validation, Frontend).

Indoklás:

Technológiai sokszínűség (Polyglot): Lehetővé teszi, hogy minden feladathoz a legmegfelelőbb nyelvet válasszuk (Java az üzleti logikára, Rust a teljesítmény-kritikus validációra).

Skálázhatóság: A különböző szolgáltatások egymástól függetlenül skálázhatók. Pl. ha az AI javaslatkérés lassú, a mapping-service-ből indíthatunk több példányt.

Hibaelhárítás: Egy komponens hibája (pl. a Rust validátor összeomlik) nem állítja le a teljes rendszert.

2. Döntés: Mapping Service - Java 21 + Spring Boot 3

Az elsődleges backend és az AI-kapcsolat nyelve a Java 21 Spring Boot-tal.

Indoklás:

Ökoszisztéma: A Spring Boot robusztus, érett ökoszisztémát biztosít (dependency injection, web szerver, adatbázis-kezelés).

Spring AI: A spring-ai projekt jelentősen leegyszerűsíti a különböző AI modellek (Gemini, OpenAI, stb.) integrációját és a válaszok kezelését.

Karrier: Nagyvállalati (enterprise) környezetben a Java továbbra is domináns, így a tudás jól átültethető.

3. Döntés: Validation Service - Rust + Axum

A második backend szolgáltatás, ami a tiszta validációt végzi, Rust nyelven íródott.

Indoklás:

Teljesítmény: A Rust rendkívül gyors, C/C++ szintű teljesítményt nyújt, ami ideális nagy mennyiségű adat gyors validálására (pl. batch feldolgozás).

Biztonság: A Rust fordítója garantálja a memóriabiztonságot (nincs null pointer, nincs data race), ami kritikus egy megbízható validációs rétegnél.

JSON Kezelés: A serde és serde_json könyvtárak villámgyors és biztonságos JSON-kezelést tesznek lehetővé.

Modernitás: Jól demonstrálja a modern, polyglot architektúra előnyeit.

4. Döntés: AI-alapú Mapping (Gemini)

A mezők összevezetését (mapping) és a transzformációs szabályok generálását egy generatív AI (LLM) végzi.

Indoklás:

Rugalmasság: A hagyományos, szabály-alapú (pl. ETL) rendszerek törékenyek. Ha egy mező neve megváltozik ("FullName" -> "Name"), a szabály elromlik.

Szemantikus megértés: Az AI képes szemantikailag megérteni a mezőket ("BirthDate" és "dateOfBirth" ugyanazt jelenti). Képes kezelni az 1-N (pl. "FullName" -> "firstName", "lastName") és N-1 (pl. "street", "number" -> "StreetAddress") leképezéseket.

Strukturált kimenet: A modern modellek (mint a Gemini) képesek garantáltan JSON formátumban válaszolni, ami gépi feldolgozásra alkalmas.

5. Döntés: Docker Compose Orchestráció

A teljes rendszert egy docker-compose.yml fogja össze.

Indoklás:

Környezetfüggetlenség: Megszünteti a "nálam működik" problémát. A fejlesztői gépen, a teszt szerveren és élesben is ugyanaz a környezet fut.

Egyszerű indítás: Egyetlen docker-compose up paranccsal a teljes, többnyelvű rendszer elindul.

Hálózatkezelés: A Compose automatikusan létrehoz egy belső hálózatot, ahol a szolgáltatások név alapján (mapping-service) érhetik el egymást.