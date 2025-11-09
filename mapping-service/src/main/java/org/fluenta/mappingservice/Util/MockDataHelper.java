package org.fluenta.mappingservice.Util;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class MockDataHelper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getBankCorpJson() {
        return """
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
        """;
    }

    public static String getInsureTechJson() {
        return """
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
        """;
    }

    public static Object getBankCorpAsObject() {
        try {
            return mapper.readValue(getBankCorpJson(), Object.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getInsureTechAsObject() {
        try {
            return mapper.readValue(getInsureTechJson(), Object.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
