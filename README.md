Cross-Company Data Mapper POC

Ez a projekt egy Proof of Concept (POC) egy adat-összevezető (mapper) rendszerhez, amely lehetővé teszi két különböző vállalati adatformátum (pl. BankCorp és InsureTech) közötti automatikus és AI-támogatott adatleképezést.

Architektúra

A rendszer három fő komponensből áll:

Mapping Service (/mapping-service)

Nyelv: Java 21 (Spring Boot)

Feladat: AI (Gemini) hívása a mapping javaslatokért, transzformációs szabályok tárolása és végrehajtása.

Port: 8080

Validation Service (/validation-service)

Nyelv: Rust (Axum)

Feladat: A transzformált adatok validálása a kapott szabályrendszer alapján.

Port: 8081

Frontend (/frontend)

Nyelv: Angular (TypeScript)

Feladat: Felhasználói felület a forrás és cél adatok bevitelére, valamint az AI javaslatok megjelenítésére.

Port: 4200

Futtatás Docker Compose segítségével

A teljes alkalmazás-stack egyetlen paranccsal elindítható a Docker és a Docker Compose segítségével.

Előfeltételek

Docker Desktop: Telepítve és futva.

AI API Kulcs: Szükséged lesz egy Google Gemini (vagy más) AI API kulcsra.

Indítás

API Kulcs beállítása (Kötelező):

A projekt gyökérmappájában (a docker-compose.yml fájl mellett) hozz létre egy új fájlt, és nevezd el .env -nek.

(Figyelem: a fájlnév ponttal kezdődik, és nincs más neve, csak ez a "kiterjesztése".)

Nyisd meg ezt az .env fájlt egy szövegszerkesztővel (pl. VS Code, Jegyzettömb).

Illeszd bele a következő sort, a YOUR_API_KEY_HERE részt lecserélve a saját Google Gemini API kulcsodra:

GEMINI_API_KEY=YOUR_API_KEY_HERE


Mentsd el a fájlt. A Docker Compose automatikusan be fogja olvasni ezt a fájlt indításkor, és átadja az értéket a mapping-service konténernek.

Alkalmazás építése és indítása:

Nyiss egy terminált a projekt gyökerében.

Futtasd a következő parancsot:

docker-compose up --build


Az --build kapcsoló kényszeríti a Docker-t, hogy újraépítse a konténereket a legfrissebb forráskód alapján. Az első indítás több percig is eltarhat, amíg letölti a Java, Rust, Node alap image-eket és lefordítja a kódokat.

Elérhetőségek

Sikeres indítás után a szolgáltatások a következő címeken érhetők el:

Frontend (Felhasználói felület): http://localhost:4200

Mapping Service (Java API): http://localhost:8080

Validation Service (Rust API): http://localhost:8081