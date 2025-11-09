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
