/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.util.test.AbstractTest;

import static de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.iPartsMigrationFootnotesHandler.parseResponseDataStr;

/**
 * Created by suedkamp on 30.05.2016.
 */
public class TestMadTal4XABaseImporter extends AbstractTest {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    public void testParseSaNumberValidity() {
        assertEquals(" 12345", MadTal4XABaseImporter.parseSaNumberValidity(" 12345  ,")); // 6-stellige SA-Nr mit führendem Leerzeichen
        assertEquals("123456", MadTal4XABaseImporter.parseSaNumberValidity("123456  ,")); // normale 6-stellige SA-Nr
    }

    public void testParseResponseDataStr() {
        String[][] dataArray = {
                // Tests die erolgreich sein sollen
                // diese Strings aufgrund der Daimler Beschreibung oder gefundener Testdaten

                // Hinweis: die folgenden Tests enthalten noch Beispiele für das separate Parsen von Werkskennzeichen und Seriennummer
                // mittlerweile wird nur noch der kombinierte String aus Werkskennzeichen und Seriennummer ermittelt

                // fehlendes Werks-Kennzeichen
                { "AF 123456", "AF", "123456", "" },
                { "AG 123456", "AG", "123456", "" }, // kein Fahrzeug

                // Werks-Kennzeichen und Ident direkt hintereinander
                { "AF 1234567", "AF", "1234567", "" },
                { "AF *123456", "AF", "*123456", "" },
                { "AF C123456", "AF", "C123456", "" },
                { "AF L123456  (WHC: WDB)", "AF", "L123456", "WDB" },
                { "AF L123456(WHC: WDB)", "AF", "L123456", "WDB" },

                // Werks-Kennzeichen und Ident durch Leerzeichen getrennt
                { "BF C 123456", "BF", "C123456", "" },
                { "AF L 123456  (WHC: WDB)", "AF", "L123456", "WDB" },
                { "AF L 123456  (WHC:  WDB)", "AF", "L123456", "WDB" },
                { "AF L 123456  (WHC:WDB)", "AF", "L123456", "WDB" },
                { "BF   L 439439  (WHC: WDB)", "BF", "L439439", "WDB" },
                { "AF G 621673  (WHC: Z9M)", "AF", "G621673", "Z9M" },
                { "BF 0 042809(WHC: WDB)", "BF", "0042809", "WDB" },

                // nach dem Aggregate-Typ muss kein Leerzeichen folgen
                { "BM025556", "BM", "025556", "" }, // kein Fahrzeug
                { "BM0025556", "BM", "0025556", "" }, // kein Fahrzeug

                // Seriennummern für Aggregate können 6-8 Stellen lang sein, je nach Aggregateart
                // ohne WKZ
                { "BM123456", "BM", "123456", "" },
                { "BM1234567", "BM", "1234567", "" },
                { "BM12345678", "BM", "12345678", "" },

                // mit WKZ (hier "0")
                { "BM0123456", "BM", "0123456", "" },
                { "BM01234567", "BM", "01234567", "" },
                { "BM012345678", "BM", "012345678", "" },

                // Tests die fehlschlagen sollen
                // Für Strings die fehlschlagen sollen, Leerstring als 2. Element setzen
                { "AY 123456", "", "", "" } // ungültiges Aggregatekennzeichen
        };

        for (String[] data : dataArray) {
            _testParseResponseDataStr(data);
        }
    }

    private void _testParseResponseDataStr(String[] data) {
        iPartsMigrationFootnotesHandler.ResponseData responseData = parseResponseDataStr(data[0]);
        if (responseData != null) {
            System.out.println(data[0]);
            assertEquals(data[1], responseData.getType());
            assertEquals(data[2], responseData.getIdent());
            assertEquals(data[3], responseData.getWhc());
        } else {
            // Parsen fehlgeschlagen
            boolean shouldFail = data[1].equals("");
            if (!shouldFail) {
                fail("'" + data[0] + "' is no valid response data string");
            }
        }
    }
}
