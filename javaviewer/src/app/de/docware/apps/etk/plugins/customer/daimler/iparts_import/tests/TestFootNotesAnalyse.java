/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.tests;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCrossRefFootnoteHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.test.AbstractTest;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tauber on 28.06.2018.
 */
public class TestFootNotesAnalyse extends AbstractTest {

    private enum TestCases {
        TEST_01("FARBNUMMERN UND LACKCODES FUER AUSSENTEILE SIEHE FUSSNOTE 901", true, "901"),
        TEST_02("AB FGST 016491-020643 AUSSERDEM EINGEBAUT IN SIEHE FUSSNOTE 11 MIT AUSNAHME VON SIEHE FUSSNOTE 4", true, "011,004"),
        TEST_03("SIEHE FUSSNOTEN : 051 / 622", true, "051,622"),
        TEST_04("FARBNUMMERN UND LACKCODES FUER AUSSENTEILE SIEHE FUSSNOTE 990 + 991", true, "990,991"),
        TEST_05("FARBNUMMERN UND LACKCODES FUER AUSSENTEILE SIEHE FUSSNOTE 905/910", true, "905,910"),
        TEST_06("SIEHE FUSSNOTEN : 152 / 180 / 525", true, "152,180,525"),
        TEST_07("FARBNUMMERN UND LACKCODES FUER AUSSENTEILE SIEHE FUSSNOTE 901 BEI CODE 955; FUSSNOTE 902 BEI CODE 954", true, "901,902"),
        TEST_08("SIEHE FUSSNOTE 002,004,005,007,008", true, "002,004,005,007,008"),
        TEST_09("NICHT GUELTIG BEI FGST SIEHE FUSSNOTE 207,211,214", true, "207,211,214"),
        TEST_10("SIEHE FUSSNOTE 004,005,006,007", true, "004,005,006,007"),
        TEST_11("SIEHE FUSSN 004,005,006,007", true, "004,005,006,007"),
        TEST_12("SIEHE FUSSN. 004,005,006,007", true, "004,005,006,007"),
        TEST_13("AB FGST:123.023 157707 AUSSERDEM EINGEBAUT IN SIEHE FUSSN.94; 123.033 064086 AUSSERDEM EINGEBAUT IN SIEHE FUSSN.95; UND FUSSN.91", true, "094,095,091"),
        TEST_14("SCHWEDEN:107.024 BIS FGST 019071 AUSSERDEM EINGEBAUT IN SIEHE FUSSN.13;UND FUSSN.12", true, "013,012"),
        TEST_15("AB FGST:123.023 120397-157706 MIT AUSNAHME VON SIEHE FUSSN.94; 123.033 049637-064085 MIT AUSNAHME VON SIEHE FUSSN.95; UND FUSSN.87", true, "094,095,087"),
        TEST_16("BIS FGST 352936; AUSSERDEM EINGEBAUT SIEHE FUSSN.4", true, "004");

        private String text;
        private boolean result;
        private String resultNumbers;

        TestCases(String text, boolean result, String resultNumbers) {
            this.text = text;
            this.result = result;
            this.resultNumbers = resultNumbers;
        }
    }

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    public void testSpecialNumberFromMADAndELDAS() {
        Set<String> crossRefFootnoteNumbers = new LinkedHashSet<>();
        for (TestCases testCase : TestCases.values()) {
            boolean result = iPartsCrossRefFootnoteHelper.analyzeFootNoteCrossreference(testCase.text, crossRefFootnoteNumbers);
            assertEquals(result, testCase.result);
            if (result) {
                List<String> resultList = StrUtils.toStringList(testCase.resultNumbers, ",", true);
                assertEquals(resultList.size(), crossRefFootnoteNumbers.size());
                List<String> currentResult = new DwList<>(crossRefFootnoteNumbers);
                assertEquals(true, resultList.containsAll(currentResult));
            }
        }
    }
}
