/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.tests;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.util.test.AbstractTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestNumberHelper extends AbstractTest {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    private static String[] primusPartNoMemoryNoES_Good = new String[]{
            "A11122333  44",
            "A111223337744",
            "H111223337744",
            "R11122333  44",
            "W111223337744",
            "W11122333  44",
            "C333111444",
            "K3333333311",
            "N333333111111",
            "U333333111111",
            "X3333333333",
            "B31442222",
            "Q333333333333",
            "QPKW3333333333",
            "Q33333333333333333"
    };
    private static String[] primusPartNoMemoryNoES_Bad = new String[]{
            "A11122333  01eee",
            "A111223334401e",
            "A1112233  014",
            "A11122333440",
            "H111223334401e",
            "H1112233 401",
            "R00011222ee33",
            "W000  2223344",
            "W00011222  44eer",
            "C00011122",
            "K3333330011eeee",
            "N000 00001111",
            "U333333   111",
//            "X333333111",
            "B 1223333",
//            "Q012  5678912",
            "Q01234567891234567ee",
            "Z37833"
    };
    private static String[] primusPartNoMemoryWithES_Good = new String[]{
            "A11122333  44556666",
            "A11122333  4455",
            "A111223337744556666",
            "A11122333774455",
            "H111223337744556666",
            "H11122333774455",
            "W111223337744556666",
            "W11122333774455",
            "W11122333  44556666",
            "W11122333  4455",
            "C333111444   556666",
            "C333111444   55",
            "K3333333311  55",
            "N33333311111155",
            "U333333111111556666",
            "U33333311111155",
            "X3333333333  556666",
            "X3333333333  55",
            "B31442222    55",
            "Q333333333333556666",
            "Q33333333333355"
    };
    private static String[] primusPartNoMemoryWithES_Bad = new String[]{
            "A111223334401xy111",
            "H1112233344  012345",
            "W000112223344xx123",
            "W0001122   e44xx1234",
            "C000111222eeexx1234",
            "K3333330011eexxe",
            "K3333330011eexx",
            "N333333001111xxee",
            "U333333  1111xx0123",
            "X3333330011eexx0123",
            "B01223333  eexx",
            "Q012345678912xx0123e"
    };

    private static String[] primusPartNoPrintNoES_Good = new String[]{
            "A   333 111 44 22",
            "A77 333 111 44 22",
            "H77 333 111 44 22",
            "R   333 111 44 22",
            "W77 333 111 44 22",
            "W   333 111 44 22",
            "C333 111 444",
            "K 3333333311",
            "N 333333 111111",
            "U 333333 111111",
            "X3333333333",
            "B3 1 44 2222",
            "Q333333333333",
            "QPKW3333333333",
            "Q33333333333333333"
    };
    private static String[] primusPartNoPrintNoES_Bad = new String[]{
            "Ae  333 111e22 01",
            "A55e333 111 22",
            "H55 333 111 22e01ee",
            "Ree 333e111 22 01",
            "W333 111 22 01eee",
            "W55e333 111 22 01",
            "C111 222e333",
            "K   11111122",
            "N 111 111 222 222",
            "U 111111222222",
            "X1111 1111 11",
            "B1233ee4444"
//            "Q1111111111",
//            "Q11111eeeee11111112",
//            "Q11112333"
    };
    private static String[] primusPartNoPrintWithES_Good = new String[]{
            "A   333 111 44 22 55 6666",
            "A   333 111 44 22 55",
            "A77 333 111 44 22 55 6666",
            "A77 333 111 44 22 55",
            "H77 333 111 44 22 55 6666",
            "H77 333 111 44 22 55",
            "W77 333 111 44 22 55 6666",
            "W77 333 111 44 22 55",
            "W   333 111 44 22 55 6666",
            "W   333 111 44 22 55",
            "C333 111 444      55 6666",
            "C333 111 444      55",
            "K 3333333311      55",
            "N 333333 111111   55",
            "U 333333 111111   55 6666",
            "U 333333 111111   55",
            "X3333333333       55 6666",
            "X3333333333       55",
            "B3 1 44 2222      55",
            "Q333333333333     55 6666",
            "Q333333333333     55"
    };
    private static String[] primusPartNoPrintWithES_Bad = new String[]{
            "A   333 111 22 01 a",
            "A55 333 111 22 01aa bbbb",
            "A55 333 111 22 01aab",
            "H55 333 111 22 01aabbbb",
            "H55 333 111 22 01aa",
            "W   333 111 22 01 aabbbb",
            "W   333e111 2201 aae",
            "W55 333 111e22 01 aabbbb",
            "W55e333e111 22 01 aaeeee",
            "C111 222 333   aa bbbb",
            "C111 222 333  eee aa bbbb",
            "C111 222 333e aa",
            "K 1111111122ee    aa",
            "N 111111 222222  eaa",
            "U 111111 222222ee aabbbb",
            "U 111111 222222aa",
            "X1111111111 eee   aa bbbb",
            "X1111111111   e   aaeee",
            "B1 2 33 4444 eee aa"
    };

    private static String[] primusRealDataMemory = new String[]{
            "A67810941  01",
            "A67810941  0179",
            "A68006940  08  7D53",
            "N001587006009",
            "N006799012001",
            "A46001205  28",
            "A46001205  29", // PIN
            "A67810941  01",
            "A67810941  0179",
            "A67810941  0192",
            "A68006940  08  7D53",
            "A68006940  08  9B51",
            "A68006940  09  9B51",
            "A68006940  10  9B51",
            "A68006940  13  9B51",
            "A68006940  14  9B51",
            "A68006940  15  7D53",
            "A68006940  15  9B51",
            "A68006940  18  9B51",
            "A68006940  19  9B51",
            "A68006940  25  9B51",
            "A68006940  26  9B51",
            "A68006940  27  9B51",
            "A68006940  28  9B51",
            "A68006940  29  9B51",
            "A68006940  30  9B51",
            "A68006940  31  9B51",
            "A68006940  37  9B51",
            "A68006940  40  9B51",
            "A68006940  42  9B51",
            "A68006940  43  9B51",
            "A68039940  03  7C45",
            "A68039940  03  9051",
            "A68039940  04  7C45",
            "A68039940  04  9051",
            "A68039940  05  7C45",
            "A68039940  05  9051",
            "A68039940  06  7C45",
            "A68039940  06  9051",
            "A68039940  11  7C45",
            "A68039940  11  9051",
            "A68039940  13  7J90",
            "A68039940  13  8L63",
            "A68039940  13  2A44",
            "A68039940  14  7J90",
            "A68039940  14  8L63",
            "A68039940  14  2A44",
            "A68039940  16  7J90",
            "A68039940  16  8L63",
            "A68039940  16  2A44",
            "A68039940  17  7J90",
            "A68039940  17  8L63",
            "A68039940  17  2A44",
            "A29250124  00",
            "N006799012001",
            "A90602205  36",
            "A46001205  28",
            "A46001205  29", // PUP
            "A46001205  28",
            "A68039940  13  7J90",
            "A68039940  13  8L63",
            "A68039940  13  2A44",
            "A68039940  14  7J90",
            "A68039940  14  8L63",
            "N006799012001",
            "A90602205  36"
    };

    private static String[] primusRealDataPrint = new String[]{
            "A   941 678 01 10",
            "A   941 678 01 10 79",
            "A   940 680 08 06    7D53",
            "N 001587 006009",
            "N 006799 012001",
            "A   205 460 28 01",
            "A   205 460 29 01",// PIN
            "A   941 678 01 10",
            "A   941 678 01 10 79",
            "A   941 678 01 10 92",
            "A   940 680 08 06    7D53",
            "A   940 680 08 06    9B51",
            "A   940 680 09 06    9B51",
            "A   940 680 10 06    9B51",
            "A   940 680 13 06    9B51",
            "A   940 680 14 06    9B51",
            "A   940 680 15 06    7D53",
            "A   940 680 15 06    9B51",
            "A   940 680 18 06    9B51",
            "A   940 680 19 06    9B51",
            "A   940 680 25 06    9B51",
            "A   940 680 26 06    9B51",
            "A   940 680 27 06    9B51",
            "A   940 680 28 06    9B51",
            "A   940 680 29 06    9B51",
            "A   940 680 30 06    9B51",
            "A   940 680 31 06    9B51",
            "A   940 680 37 06    9B51",
            "A   940 680 40 06    9B51",
            "A   940 680 42 06    9B51",
            "A   940 680 43 06    9B51",
            "A   940 680 03 39    7C45",
            "A   940 680 03 39    9051",
            "A   940 680 04 39    7C45",
            "A   940 680 04 39    9051",
            "A   940 680 05 39    7C45",
            "A   940 680 05 39    9051",
            "A   940 680 06 39    7C45",
            "A   940 680 06 39    9051",
            "A   940 680 11 39    7C45",
            "A   940 680 11 39    9051",
            "A   940 680 13 39    7J90",
            "A   940 680 13 39    8L63",
            "A   940 680 13 39    2A44",
            "A   940 680 14 39    7J90",
            "A   940 680 14 39    8L63",
            "A   940 680 14 39    2A44",
            "A   940 680 16 39    7J90",
            "A   940 680 16 39    8L63",
            "A   940 680 16 39    2A44",
            "A   940 680 17 39    7J90",
            "A   940 680 17 39    8L63",
            "A   940 680 17 39    2A44",
            "A   124 292 00 50",
            "N 006799 012001",
            "A   205 906 36 02",
            "A   205 460 28 01",
            "A   205 460 29 01", // PUP
            "A   205 460 28 01",
            "A   940 680 13 39    7J90",
            "A   940 680 13 39    8L63",
            "A   940 680 13 39    2A44",
            "A   940 680 14 39    7J90",
            "A   940 680 14 39    8L63",
            "N 006799 012001",
            "A   205 906 36 02"
    };

    private static String[] primusSpecialPartNoMemory_Good = new String[]{
            "QV21",
            "QEX25",
            "QEHZ11",
            "QEHZ200",
            "QAS3R4KW",
            "QAP102105",
            "QAP1820006",
            "QAKA232A201",
            "QA240PAK3322",
            "QAP6665677H06",
            "QPM35000133706",
            "QAP8695561TLR08",
            "QME554669    64",
            "QAPC8000100000084",
            "Q0000009V007CA1A00",
            "QV213061B1A3A  1A0B",
            "X002040320",
            "XKL91000041",
            "XVV70040450  80",
            "X002000015   700203"
    };

    private static String[] primusSpecialPartNoMemory_bad = new String[]{
            "QV21               1",
            "QEX25              1",
            "QEHZ11             1",
            "QEHZ200            1",
            "QAS3R4KW           1",
            "QAP102105          1",
            "QAP1820006         1",
            "QAKA232A201        1",
            "QA240PAK3322       1",
            "QAP6665677H06      1",
            "QPM35000133706     1",
            "QAP8695561TLR08    1",
            "QME554669    64    1",
            "QAPC8000100000084  1",
            "Q0000009V007CA1A00 1",
            "QV213061B1A3A  1A0B1",
            "X002040320         1",
            "XKL91000041        1",
            "XVV70040450  80    1",
            "X002000015   7002031"
    };

    private TestObjectForNumberConverterTest helper;

    private HashMap<String, String> numbers = new HashMap<String, String>();
    private List<String> wrongFormat = new ArrayList<String>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        helper = new TestObjectForNumberConverterTest();
        numbers.clear();
        numbers.put("06N000000003826N        ", "N000000003826");
        numbers.put("06N000000003826N", "N000000003826");
        numbers.put("06N000000003762N        ", "N000000003762");
        numbers.put("09Q0000009V006 Q        ", "Q0000009V006");
        numbers.put("09Q0000009V006 Q", "Q0000009V006");
        numbers.put("09Q0000055V004 Q        ", "Q0000055V004");
        numbers.put("09B66007109    B        ", "B66007109");
        numbers.put("09B66470546    B        ", "B66470546");
        numbers.put("09CKL91000219  X        ", "XKL91000219");
        numbers.put("09CKL91000232  X        ", "XKL91000232");
        numbers.put("06 00026  19901A        ", "A1990000126");
        numbers.put("06 00099  21330A        ", "A2130003099");
        numbers.put("06 00070TP00022H        ", "HTP0000002270");
        numbers.put("06 00070TP00022H", "HTP0000002270");
        numbers.put("06 00074TP00022H        ", "HTP0000002274");
        numbers.put("02C002011501   C        ", "C002011501");
        numbers.put("02C002800022   C        ", "C002800022");
        numbers.put("02C002800022   CHT334444", "C002800022   HT334444");
        numbers.put("F1 Q293                 ", "Q293");
        numbers.put("F1 QRGT                 ", "QRGT");
        numbers.put("Q  000                  ", "Q  000");
        numbers.put("Q  020                  ", "Q  020");
        numbers.put("14I122222      I", "I122222");
        numbers.put("03D1112223     D", "D1112223");
        numbers.put("06P1222333445  P", "P1222333445");
        numbers.put("A0011596601", "A0011596601");
        numbers.put("QFT091L0600", "QFT091L0600");
        numbers.put("A123456789", "A123456789");
        numbers.put("X12345", "X12345");
        numbers.put("IPL", "IPL");
        numbers.put("09Q112233445566Q88", "Q11223344556688");
        numbers.put("09Q112233445566Q889999", "Q11223344556688  9999");
        numbers.put("09Q112233445566Q889", "Q11223344556688  9");
        numbers.put("09Q11223344556 Q88", "Q11223344556 88");
        numbers.put("09Q11223344556 Q889999", "Q11223344556 88  9999");
        numbers.put("09Q11223344556 Q8899", "Q11223344556 88  99");
        numbers.put("09Q0000009V007CQ08B00", "Q0000009V007C08B00");
        numbers.put("Q0008741V002", "Q0008741V002");

        wrongFormat.add("  Q90501               ");
        wrongFormat.add("     QADBL01610           A");
        wrongFormat.add("06N0000003826N        ");
//        wrongFormat.add("N000000003762N        ");  // N könnte Teil eines ES1 Schlüssel sein
        wrongFormat.add("09Q0000009V006Q");
        wrongFormat.add("09Q0000055V004 W        ");
        wrongFormat.add("09B66007109  B     HH");


    }

    public void testSpecialNumberFromMADAndELDAS() {
        for (String storageFormat : numbers.keySet()) {
            String converterResult = helper.getResultOfNumberConverterForGivenNumber(storageFormat);
            assertEquals(numbers.get(storageFormat), converterResult);
        }
    }

    public void testInvalidStorageFormat() {
        for (String storageFormat : wrongFormat) {
            String result = helper.getResultOfNumberConverterForGivenNumber(storageFormat);
            assertEquals("", result);
        }
    }

    // Echte Daten für Speicherformat
    public void testPRIMUSMemoryFormatReal() {
        for (String s : primusRealDataMemory) {
            assertTrue(helper.isPRIMUSPartNoMemoryFormatValid(s, false));
        }
    }

    // Echte Daten für Printformat
    public void testPRIMUSPrintFormatReal() {
        for (String s : primusRealDataPrint) {
            assertTrue(helper.isPRIMUSPartNoPrintFormatValid(s, false));
        }
    }

    // Echte Daten Konvertierung print - memory
    public void testPRIMUSReal_Memory_Print() {
        for (int i = 0; i < primusRealDataPrint.length; i++) {
            String source = primusRealDataPrint[i];
            String expected = primusRealDataMemory[i];
            String result = helper.convertPRIMUSPartNoPrintToMemory(source);
            assertEquals(expected, result);
        }
        doLoopMemoryToPrint(primusPartNoMemoryNoES_Good, primusPartNoPrintNoES_Good);
    }

    // Echte Daten Konvertierung memory - print
    public void testPRIMUSReal_Print_Memory() {
        doLoopMemoryToPrint(primusRealDataMemory, primusRealDataPrint);
    }


    // Memory Format
    public void testPRIMUSMemoryFormatWithoutESGoodCases() {
        for (String s : primusPartNoMemoryNoES_Good) {
            assertTrue(helper.isPRIMUSPartNoMemoryFormatValid(s, false));
        }

    }

    public void testPRIMUSMemoryFormatWithoutESBadCases() {
        for (String s : primusPartNoMemoryNoES_Bad) {
            assertFalse(helper.isPRIMUSPartNoMemoryFormatValid(s, false));
        }
    }

    public void testPRIMUSMemoryFormatWithESGoodCases() {
        for (String s : primusPartNoMemoryWithES_Good) {
            assertTrue(helper.isPRIMUSPartNoMemoryFormatValid(s, false));
        }
    }

    public void testPRIMUSMemoryFormatWithESBadCases() {
        for (String s : primusPartNoMemoryWithES_Bad) {
            assertFalse(helper.isPRIMUSPartNoMemoryFormatValid(s, false));

        }
    }

    // Print Format
    public void testPRIMUSPrintFormatWithoutESGoodCases() {
        for (String s : primusPartNoPrintNoES_Good) {
            assertTrue(helper.isPRIMUSPartNoPrintFormatValid(s, false));
        }
    }

    public void testPRIMUSPrintFormatWithoutESBadCases() {
        for (String s : primusPartNoPrintNoES_Bad) {
            assertFalse(helper.isPRIMUSPartNoPrintFormatValid(s, false));
        }
    }

    public void testPRIMUSPrintFormatWithESGoodCases() {
        for (String s : primusPartNoPrintWithES_Good) {
            assertTrue(helper.isPRIMUSPartNoPrintFormatValid(s, false));
        }
    }

    public void testPRIMUSPrintFormatWithESBadCases() {

        for (String s : primusPartNoPrintWithES_Bad) {
            assertFalse(helper.isPRIMUSPartNoPrintFormatValid(s, false));
        }
    }

    // Print To Memory
    public void testPRIMUSConvertPrintToMemory() {
        doLoopPrintToMemory(primusPartNoPrintNoES_Good, primusPartNoMemoryNoES_Good);
    }

    public void testPRIMUSConvertPrintToMemoryWithES() {
        doLoopPrintToMemory(primusPartNoPrintWithES_Good, primusPartNoMemoryWithES_Good);
    }

    // Memory to Print
    public void testPRIMUSConvertMemoryToPrint() {
        doLoopMemoryToPrint(primusPartNoMemoryNoES_Good, primusPartNoPrintNoES_Good);
    }

    public void testPRIMUSConvertMemoryToPrintWithES() {
        doLoopMemoryToPrint(primusPartNoMemoryWithES_Good, primusPartNoPrintWithES_Good);
    }

    private void doLoopMemoryToPrint(String[] sourceValues, String[] expectedValues) {
        for (int i = 0; i < sourceValues.length; i++) {
            String source = sourceValues[i];
            String expected = expectedValues[i];
            String result = helper.convertPRIMUSPartNoMemoryToPrint(source);
            assertEquals(expected, result);
        }
    }

    private void doLoopPrintToMemory(String[] sourceValues, String[] expectedValues) {
        for (int i = 0; i < sourceValues.length; i++) {
            String source = sourceValues[i];
            String expected = expectedValues[i];
            String result = helper.convertPRIMUSPartNoPrintToMemory(source);
            assertEquals(expected, result);
        }
    }

    public void testPRIMUSSpecialMemoryFormatGoodCases() {
        for (String s : primusSpecialPartNoMemory_Good) {
            assertTrue(helper.isPRIMUSPartNoMemoryFormatValid(s, false));
        }
    }

    public void testPRIMUSSpecialMemoryFormatBadCases() {
        for (String s : primusSpecialPartNoMemory_bad) {
            assertFalse(helper.isPRIMUSPartNoMemoryFormatValid(s, false));
        }
    }


}
