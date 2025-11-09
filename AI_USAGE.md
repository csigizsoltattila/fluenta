AI Használat Dokumentációja

Ez a dokumentum leírja, hogyan és mire használja a rendszer a Generatív AI-t (Google Gemini).

Integrációs Pont

Az AI integráció a mapping-service szolgáltatásban, a POST /mapping/suggest végponton keresztül történik.

A folyamat a következő:

A Frontend elküld két JSON objektumot: sourceData (BankCorp) és targetSchema (InsureTech).

Az AiSuggestionService (Java) megkapja ezeket.

Felépít egy "system prompt"-ot és egy "user prompt"-ot.

Meghívja a Gemini API-t, kérve, hogy a válasz application/json formátumú legyen.

Az AI elemzi a két struktúrát és szemantikailag megpróbálja kitalálni a leképezéseket.

Az AI visszaad egy JSON választ, ami megfelel az általunk kért MappingSuggestionResponse DTO-nak.

A Java ezt a JSON-t deszerializálja és visszaküldi a frontendnek.

AI Promptok (Példa)

1. System Prompt

A system prompt adja meg az AI-nak a kontextust és a kimeneti formátumra vonatkozó szigorú szabályokat.

You are an expert data mapping assistant. Your task is to analyze two JSON structures, a source and a target.
You must generate a mapping between them in a structured JSON format.
Your response MUST be a valid JSON object adhering to the requested schema, and nothing else.
The JSON object must contain three keys: 'fieldMappings', 'transformationRules', and 'validationRules'.
Transformation rules should handle things like date formatting (FORMAT_DATE), name splitting (SPLIT_NAME), or concatenation (CONCAT).
Validation rules should suggest checks like IS_EMAIL, NOT_NULL, or REGEX.


2. User Prompt (Példa)

A user prompt tartalmazza a konkrét adatokat, amikkel az AI-nak dolgoznia kell.

Here is the source data sample (BankCorp):
```json
{
  "Customer": {
    "CIF": "12345",
    "PersonalData": {
      "FullName": "Nagy János",
      "BirthDate": "1985-03-15",
      "TaxID": "8765432109"
    },
    "ContactInfo": {
      "PrimaryPhone": "+36301234567",
      "EmailAddr": "nagy.janos@email.hu"
    },
    "Address": {
      "PostalCode": "1011",
      "City": "Budapest",
      "StreetAddress": "Fő utca 1."
    }
  }
}


Here is the target data sample (InsureTech):

{
  "client": {
    "id": "INS-98765",
    "personal": {
      "firstName": "János",
      "lastName": "Nagy",
      "dateOfBirth": "15/03/1985",
      "nationalId": "8765432109"
    },
    "contact": {
      "mobile": "06301234567",
      "email": "nagy.janos@email.hu"
    },
    "location": {
      "zip": "1011",
      "city": "Budapest",
      "street": "Fő utca",
      "number": "1"
    }
  }
}


Please provide the fieldMappings, transformationRules, and validationRules in the specified JSON format. Pay attention to a-sync fields like 'FullName' -> 'firstName'/'lastName' and 'BirthDate' -> 'dateOfBirth' format.


## Várt AI Válasz (JSON Séma)

Az AI-tól egy, a `MappingSuggestionResponse` DTO-nak megfelelő JSON struktúrát várunk vissza:

```json
{
  "fieldMappings": [
    {
      "sourceField": "Customer.CIF",
      "targetField": "client.id"
    },
    {
      "sourceField": "Customer.PersonalData.TaxID",
      "targetField": "client.personal.nationalId"
    }
  ],
  "transformationRules": [
    {
      "targetField": "client.personal.firstName",
      "ruleType": "SPLIT_NAME",
      "parameters": {
        "sourceField": "Customer.PersonalData.FullName",
        "part": "FIRST"
      }
    },
    {
      "targetField": "client.personal.lastName",
      "ruleType": "SPLIT_NAME",
      "parameters": {
        "sourceField": "Customer.PersonalData.FullName",
        "part": "LAST"
      }
    },
    {
      "targetField": "client.personal.dateOfBirth",
      "ruleType": "FORMAT_DATE",
      "parameters": {
        "sourceField": "Customer.PersonalData.BirthDate",
        "format": "dd/MM/yyyy"
      }
    }
  ],
  "validationRules": [
    {
      "targetField": "client.contact.email",
      "ruleType": "IS_EMAIL",
      "parameters": null
    },
    {
      "targetField": "client.location.zip",
      "ruleType": "REGEX",
      "parameters": {
        "pattern": "^\\d{4}$"
      }
    }
  ]
}


## Használt eszközök
        
        - Gemini kód generálás, futtatási problémák kezelése
        - OpenAI - Code review, tervezés, értékelés, környezeti problémák feloldása

Felhasznált promptok a generáláshoz:

1.) (Gemini)

Szia! Lesz egy új projektem ami egy cross-company-data-mapper projekt. A projekt felépítése a következő:



project/

├── mapping-service/      # Első backend nyelv

├── validation-service/   # Második backend nyelv

├── frontend/            # React app

├── docker-compose.yml

├── README.md           # Futtatási útmutató

├── AI_USAGE.md        # AI használat dokumentáció

└── ARCHITECTURE.md    # Döntések indoklása (max 1 oldal)



Ezt mást fel is építettem közben. Az elsődleges backend nyelv: Java 21, Rust a másodlagos. Angular a frontend. Téged használlak elsődlegesen mint AI segítség.



## ⚙️ Technikai elvárások



### Minimális követelmények:

- ✅ Legalább 2 különböző backend nyelv

- ✅ React + TypeScript frontend

- ✅ AI API integráció (OpenAI/Claude/Gemini/Groq)

- ✅ Docker Compose

- ✅ RESTful API design



### Opcionális extrák:

- WebSocket real-time mapping status

- Batch processing képesség

- Export/import mapping templates

- Swagger/OpenAPI dokumentáció





Feladat:



Két vállalat szeretne ügyféladatokat megosztani egymással:

- **BankCorp**: Hagyományos banki formátum (XML-szerű struktúra)

- **InsureTech**: Modern biztosító (REST/JSON)



Ezen szenárió szerint szeretnék felépíteni egy POC szintű rendszer alapjait amely rendszer képes automatikusan map-elni a különböző formátumokat.



Első lépésként az első backend osztályt szeretném felépíteni ami JAVA 21.



A következő kötelező elemeket kell tudnom kezelni:



- Két különböző adatformátum fogadása

- AI-alapú field mapping javaslatok generálása

- Transformation rules végrehajtása

- Mapping template-ek tárolása (in-memory elég)



- POST `/mapping/suggest` - AI javaslatok generálása

- POST `/mapping/transform` - Adattranszformáció végrehajtása

- GET `/mapping/templates` - Mentett mapping-ek listázása



Alap json minta ami lehetséges:



### BankCorp Format (XML-style JSON):

```json

{

  "Customer": {

    "CIF": "12345",

    "PersonalData": {

      "FullName": "Nagy János",

      "BirthDate": "1985-03-15",

      "TaxID": "8765432109"

    },

    "ContactInfo": {

      "PrimaryPhone": "+36301234567",

      "EmailAddr": "nagy.janos@email.hu"

    },

    "Address": {

      "PostalCode": "1011",

      "City": "Budapest",

      "StreetAddress": "Fő utca 1."

    }

  }

}

```



### InsureTech Format (Modern JSON):

```json

{

  "client": {

    "id": "INS-98765",

    "personal": {

      "firstName": "János",

      "lastName": "Nagy",

      "dateOfBirth": "15/03/1985",

      "nationalId": "8765432109"

    },

    "contact": {

      "mobile": "06301234567",

      "email": "nagy.janos@email.hu"

    },

    "location": {

      "zip": "1011",

      "city": "Budapest",

      "street": "Fő utca",

      "number": "1"

    }

  }

}





2. **Data Transformation Suggestions**

- Az AI javasoljon transformation rule-okat (pl. dátum formátum konverzió)

- Kezelje a név szétbontást/összevonást



3. **Validation Rules Generation**

- AI generáljon validation szabályokat a mező típusok alapján



ezek amikre az adott rétegnek még tudnia kell.



Tegyél nekem javaslatokat a felépítésre, illetve írd meg a skeletont hozzá. Legyen generálva minden osztályban alap deklaráció üzleti logika nem fontos, viszont legyenek hozzá teszt osztályok is generálva amin keresztül tudom tesztelni az adatokat. Nincs mögötte backend tehát mock-olt adatokkal fogjuk a teljes rendszert felépíteni jelenleg.

2.) (Gemini)

Az általad írt pom.xml nem helyes, nem kérted el sem a verziókat sem az általam generált dependency-ket illetve a projekt adatokat. Kérlek ezek alapján frissítsd azokat...

3.) (OpenAI)

Itt most csak beszélgetni akarok. A promptokat máshol fogom kérdezni és generáltatni, itt csak ki akarom értékelni veled mindent. Te ezt hogyan kezdenéd felépíteni? Csináltam egy új projektet jelenleg jdk 21-es környezettel. Mire lesz még szükségem?


## AI hibák és javítások

 - Gemini a generálás könnyen ment az első input jól átadta az elvárt igényeket, viszont többször nem kérdezett vissza pl a pom tartalmára, hogyan generáltam az alap modulokat emiatt többször okozott helyi hibát (fordítási) és így sok idő elment ezek nyomozására.
 - OpenAI inkább beszélgetés volt, de az API key-es megoldásban segítségét kértem ott viszont többször tévesztett és nem értette a környezeti változó elnevezése már elkészült és erőltette a sajátját.

## Időmegtakarítás

 - Maga a projekt tervezésében is órákat tudtam spórolni, magában a kód bázis kialakításában viszont szerintem akár 1-2 napot is meg spóroltam, mire mindent felépítettem volna. De ez csak becslés részemről mivel teljesen új projekt. Ha ezt sokadig alkalommal csináltam volna úgy sokkal hamarabb végig mentem volna a skeleton kialakításán.