/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.tests;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.epep.helper.ePEPImportHelper;
import de.docware.util.test.AbstractTest;

public class TestEpepKemConversion extends AbstractTest {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    /**
     * Überprüft die korrekte Umwandlung der ePEP-KEMs vom Speicherformat ins Eingabeformat.
     */
    public void testEpepKemConversion() {

        ePEPImportHelper helper = new ePEPImportHelper(null, null, null, null);

        // "" => ""
        String testKemMemoryFormat = "";
        String testKemInputFormat = "";
        testKemInputFormat = helper.convertKemFromMemoryFormatToInputFormat(testKemMemoryFormat);
        assertEquals(testKemInputFormat, ""); // <=== erwartetes Ergebnis im Eingabeformat
        System.out.println(String.format("Test 1, Speicherformat [%s] korrekt umgewandelt in [%s]", testKemMemoryFormat, testKemInputFormat));

        //                    "11 ABCJJ12345nn" => "ABC12345JJNnn"
        testKemMemoryFormat = "11 ABCJJ12345nn         "; // <<=== umzuwandelndes Speicherformat
        testKemInputFormat = helper.convertKemFromMemoryFormatToInputFormat(testKemMemoryFormat);
        assertEquals(testKemInputFormat, "ABC12345JJNnn"); // <=== erwartetes Ergebnis im Eingabeformat
        System.out.println(String.format("Test 2, Speicherformat [%s] korrekt umgewandelt in [%s]", testKemMemoryFormat, testKemInputFormat));

        //                    "11 ABCJJ12345" => "ABC12345JJ";
        testKemMemoryFormat = "11 ABCJJ12345           "; // <<=== umzuwandelndes Speicherformat
        testKemInputFormat = helper.convertKemFromMemoryFormatToInputFormat(testKemMemoryFormat);
        assertEquals(testKemInputFormat, "ABC12345JJ"); // <=== erwartetes Ergebnis im Eingabeformat
        System.out.println(String.format("Test 3, Speicherformat [%s] korrekt umgewandelt in [%s]", testKemMemoryFormat, testKemInputFormat));

        //                  "11 ZAN170930001" => "ZAN930017N01"
        testKemMemoryFormat = "11 ZAN170930001         "; // <<=== umzuwandelndes Speicherformat
        testKemInputFormat = helper.convertKemFromMemoryFormatToInputFormat(testKemMemoryFormat);
        assertEquals(testKemInputFormat, "ZAN930017N01"); // <=== erwartetes Ergebnis im Eingabeformat
        System.out.println(String.format("Test 4, Speicherformat [%s] korrekt umgewandelt in [%s]", testKemMemoryFormat, testKemInputFormat));

        //                    "11 ZFN1800002" => "ZFN218"
        testKemMemoryFormat = "11 ZFN1800002           "; // <<=== umzuwandelndes Speicherformat
        testKemInputFormat = helper.convertKemFromMemoryFormatToInputFormat(testKemMemoryFormat);
        assertEquals(testKemInputFormat, "ZFN218"); // <=== erwartetes Ergebnis im Eingabeformat
        System.out.println(String.format("Test 5, Speicherformat [%s] korrekt umgewandelt in [%s]", testKemMemoryFormat, testKemInputFormat));

        //                    "11 BAT1800582" => "BAT58218"
        testKemMemoryFormat = "11 BAT1800582           "; // <<=== umzuwandelndes Speicherformat
        testKemInputFormat = helper.convertKemFromMemoryFormatToInputFormat(testKemMemoryFormat);
        assertEquals(testKemInputFormat, "BAT58218"); // <=== erwartetes Ergebnis im Eingabeformat
        System.out.println(String.format("Test 6, Speicherformat [%s] korrekt umgewandelt in [%s]", testKemMemoryFormat, testKemInputFormat));


        // FIN-Datumsumwandlung, erwartet wird ein Datum im Format: "yyyyMMdd"
        // Leeres Datum.
        String inputFinDate = "";
        String outputFinDate = helper.handleDateValue(inputFinDate);
        assertEquals(outputFinDate, "");
        System.out.println(String.format("Test 7, FIN-Datum [%s] korrekt umgewandelt in [%s]", inputFinDate, outputFinDate));

        // Ungültiges Datumsformat, soll "yyyyMMdd" ist "ddMMyyyy"
        inputFinDate = "31121987";
        outputFinDate = helper.handleDateValue(inputFinDate);
        assertEquals(outputFinDate, "");
        System.out.println(String.format("Test 7, FIN-Datumsformat [%s] korrekt umgewandelt in [%s]", inputFinDate, outputFinDate));

        // Blödsinniges Datum.
        inputFinDate = "99999999";
        outputFinDate = helper.handleDateValue(inputFinDate);
        assertEquals(outputFinDate, "");
        System.out.println(String.format("Test 8, Falsches FIN-Datum [%s] korrekt umgewandelt in [%s]", inputFinDate, outputFinDate));

        // Passendes Datum.
        inputFinDate = "19660704";
        outputFinDate = helper.handleDateValue(inputFinDate);
        assertEquals(outputFinDate, "19660704");
        System.out.println(String.format("Test 9, Falsches FIN-Datum [%s] korrekt umgewandelt in [%s]", inputFinDate, outputFinDate));

        // fertsch!
        System.out.println("testEpepKemConversion()-Test erfolgreich");
    }
}
