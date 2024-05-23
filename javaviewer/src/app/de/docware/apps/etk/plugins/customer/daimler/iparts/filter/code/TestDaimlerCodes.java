/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.containers.XMLConfigContainer;
import de.docware.util.misc.booleanfunctionparser.BooleanFunction;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.test.tags.iPartsTest;
import org.junit.jupiter.api.BeforeAll;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestDaimlerCodes {

    @BeforeAll
    protected static void globalSetUp() {
        iPartsPlugin iPartsPlugin = new iPartsPlugin();
        ConfigBase pluginConfig = new ConfigBase(XMLConfigContainer.getInstanceInMemory());
        iPartsPlugin.initPlugin(pluginConfig);
    }

    @iPartsTest
    public void testCodeSplit() {
        DaimlerCodes.getCodeSet("IL8-/GC8/GC9)");
        DaimlerCodes.getCodeSet("IL8-/GC8/GC9)+M22");
        // Beispiele von EvoBus
        compareLists(new String[]{ "636K", "662K", "663K", "802", "939K", "940K", "B03" }, DaimlerCodes.getCodeSet("-636K-662K-663K-802-939K+940K+B03;"));
        compareLists(new String[]{ "636K", "662K", "663K", "802", "939K", "940K", "B03" }, DaimlerCodes.getCodeSet("-636K+-662K+-663K+-802+-939K+940K+B03;"));

        // Beispiele von Daimler
        compareLists(new String[]{ "234", "249", "275", "494", "237", "500", "460", "623", "876", "M12" }, DaimlerCodes.getCodeSet("(234+249+275+494/237+249+275+494)+-500+-460+-623+-876+-M12;"));
    }

    @iPartsTest
    public void testCodeMatch() {
        try {
            Set<String> saCodes = new HashSet<>();
            saCodes.add("940K");
            saCodes.add("B03");

            assertTrue(DaimlerCodes.isCodeMatch(saCodes, "-636K-662K-663K-802-939K+940K+B03;"/*, null*/));
            assertTrue(DaimlerCodes.isCodeMatch(saCodes, "-636K+-662K+-663K+-802+-939K+940K+B03;"/*, null*/));
            assertFalse(DaimlerCodes.isCodeMatch(saCodes, "-636K-662K-663K-802-939K-B03;"/*, null*/));


            saCodes = new HashSet<>();
            saCodes.add("L46");
            saCodes.add("ZG2");

            assertTrue(DaimlerCodes.isCodeMatch(saCodes, "-ZU7-ZU8+L46+ZG1/-ZU7-ZU8+L46+ZG2/-ZU7-ZU8+L46+ZG3/-ZU7-ZU8+L46+ZG4;"/*, null*/));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Erzeuge die disjunctive Normalform aus einem Code und Teste, ob in allen Varianten das identische Ergebnis rauskommt
     */
    @iPartsTest
    public void testDisjunctiveNormalForm() {
        try {
            List<String> codes = new ArrayList<>();

            codes.add("((A/B)+(C/D)/C/D)+E;");
            codes.add("A/A+B;");

            // Für diesen Code wird eine falsche DNF geliefert
            codes.add("(F205/F213/F238/F253)+M014+-(920/M014);");

            // Extremer Codes, die eine sehr lange disjunktive Normalform ergeben. Der Test dauert Tage
            codes.add("821+-(U98/M276/M274+(241/242/275/299+483/550/551+882/889/Z90/228/450/460/494/965))+-B01;");

            // Extremen Code etwas vereinfacht
            codes.add("821+-(U98/M276/M274+(241/242/275/299+483/550/551+882/889/450/460/965))+-B01;");
            codes.add("821+-(U98/M276/M274+(241/242/275/299+483/551+882/228/Z90/228))+-B01;");
            codes.add("821+-(U98/M276/M274+(241/242/275/299+483/551+882/889/Z90/228/450/965))+-B01;");
            codes.add("821+-(U98/M276/M274+(241/242/275/299+483/551+882/228))+-B01;");
            codes.add("ME06/ME04/M005/M274+M16/M626");
            codes.add("(M651/M626)+-(B16/U21/ME06/805)");
            codes.add("M651+-B16+-U21+-ME06+-805");
            codes.add("-(M177/M274+M20/M276/M651+M22+M014/M654+M20+M014/460/494/835/916/933)");
            codes.add("M651+M22+075+017+421+-(M013/M014/M005/M020/421);");
            codes.add("940K");
            codes.add("-ZU7-ZU8+L46+ZG1/-ZU7-ZU8+L46+ZG2/-ZU7-ZU8+L46+ZG3/-ZU7-ZU8+L46+ZG4;");
            codes.add("M651+-(M005/877K);");
            codes.add("M626+-877K;");
            codes.add("(M651+M005)+-877K;");
            codes.add("M651+M005+421;");
            codes.add("M651+-(450/M005/877K);");
            codes.add("((M651/M654)+965)+-(877K/M005);");
            codes.add("M626+-877K;");
            codes.add("M651+M005+421;");
            codes.add("(M651+M005/M651+M005+965)+-877K;");
            codes.add("(M264/M274)+M005+700;");
            codes.add("M651+-M005;");
            codes.add("M626;");
            codes.add("M651+M005;");
            codes.add("-M651;");
            codes.add("M651+M005;");
            codes.add("-(M626/M177/M274+M005/M654+M005);");
            codes.add("M626;");
            codes.add("-M005;");
            codes.add("M005;");
            codes.add("-(M651/M654/M177/M005);");
            codes.add("-(M651/M654/M177/M005);");
            codes.add("M651+-M005;");
            codes.add("M651+-M005;");
            codes.add("M651+-M005;");
            codes.add("(M651+421)+-M005;");
            codes.add("M651+421+(807+057/808);");
            codes.add("M626+425;");
            codes.add("M626+425;");
            codes.add("(M264/M274/M654)+421;");
            codes.add("M651+421+(807+057/808);");
            codes.add("(427+M005)+-(M276+M016);");
            codes.add("(427+M005)+-(M276+M016);");
            codes.add("421+M005+-M016;");
            codes.add("421+M005+-M016;");
            codes.add("-M005;");
            codes.add("M005;");
            codes.add("-M005;");
            codes.add("M005;");
            codes.add("M276+M30+M016+M005+421;");
            codes.add("M276+M30+M016+M005+421;");
            codes.add("M626+425;");
            codes.add("M626+427;");
            codes.add("(M651+425)+-M005;");
            codes.add("M651+427+-(ME04/M005);");
            codes.add("M651+421+-M005;");
            codes.add("M651+M005+(427/427+(494/460));");
            codes.add("M651+M005+(421/421+(494/460));");
            codes.add("-M005;");
            codes.add("M005;");
            codes.add("M651+421+(805/806/807)+-M005;");
            codes.add("M651+421+(805/806/807)+-M005;");
            codes.add("M626;");
            codes.add("M626;");
            codes.add("M626;");
            codes.add("M626;");
            codes.add("M626;");
            codes.add("M626;");
            codes.add("M626;");
            codes.add("427+-(M177/M005);");
            codes.add("427+-(M177/M005);");
            codes.add("((421+-(M177/M651/M654)/421+((M651/M654)+(ME04/ME05)/ME06)))+-M005;");
            codes.add("((M651/M654)+421)+-M005;");
            codes.add("(427/421)+-M005;");
            codes.add("(427/421)+-M005;");
            codes.add("A+B/A-B;");
            codes.add("A+B/A;");
            codes.add("A+B-C/-C+A-B;");
            codes.add("A+(F205/F204/-(-F205+M005));");
            codes.add("B03/ME04/ME06");
            codes.add("-a+-b/a+-c/b+-c/c;");
            // Test mit vielen Variablen
            codes.add("a+b+c+d+e+f+g+h+i+j+k+l+m+n+o+p+q+r+s+t+u+v+w+x+y+z;");

            // Ein Code mit Leerzeichen (gibt es, sollte aber nicht vorkommen!!)
            codes.add("- (577/W66/W77/X47/X48)");
            codes.add(" -(580/581)");
            // + am Anfang gibt es, sollte aber auch nicht vorkommen
            codes.add("+809");
            codes.add("(IN2+UR1+(ZU7/ZU8))+-((IN1/IN3)+(UR1/V41/ZK4));");
            codes.add("((Z11/Z12/Z13/Z14)+-(Z11+Z12+Z13+Z14))+Z07;");
            codes.add("M271+-M010/M271+M010+M013/M272+-(M35/M005)/M272+M005+(M25/M30/M35)/M274/M642+M005/M646+M006/M651+M005+-M014/M651+M014+M005/M276+M005");
            codes.add("(F204+-(M005/062/801)/F207+-(051/062))+-(802/803)/F212+-(802/803)/F906+-(181/U41)/F204+801+064/F204+801+062+M014;");
            codes.add("(M271/M272/M646)/(M651+423)+-(M014/M005)/M651+-(M013/M014/M005)/M651+M013/M651+M014+M005/M276+-M005;");
            codes.add("(F204+-(062/801)/F207)+-(802/803)/F212+-(800/802/803)/F906+-(181/U41)/F204+801+064/F204+801+062+M014;");
            codes.add("819+-(494/498/710/800)/800+(819/494)+-(498/352);");
            codes.add("(M112/M113/M275/M137)+228+487+-(M113+M55+M001/M275+M60);");
            // Beispiel zur Resolventenmethode aus Wikipedia
            codes.add("b+c+d/a+-b/a+b+-c/a+b+c+-d;");
            codes.add("((IT3/IT4)+Z12/IT4+((A50+Z12)/XL8+(Z12/ZU7/ZU8/ZU9))/IT5)+-(ZG1/ZG2/ZG3/ZG4);");
            codes.add("M214+M214");
            codes.add("(M214/M213)+A1/(M214/A1)+M213");
            codes.add("A111+((M214/M213)+A1/(M214/A1)+M213)");
            codes.add("(001A/018A/131A/201A/218A/101A/115A/118A/201A/215A/218A/301A/318A)+(FW/FS/FV);");
            codes.add("805+(425/425+235+(238/501/266/463/476))+-(055/421/427);");
            codes.add("805+425+235+-(055/421/427);");
            codes.add("463+-(165/184/700/830/494/460/P86/P29+241A/P29+641A/U34);");

            // Beispiel von Daimler
            codes.add("531+-(805/806);");

            for (String code : codes) {
                BooleanFunction parser = DaimlerCodes.getFunctionParser(code);
                BooleanFunction parserNormal = parser.cloneMe();
                parserNormal.convertToDisjunctiveNormalForm(true);
                assertTrue(truthTableIsEqual(parser, parserNormal), "Error testing code with simplification: " + code);
            }

            // Jetzt noch ein paar Codes ohne Vereinfachung der DNF
            codes.clear();
            codes.add("((A/B)+(C/D)/C/D)+E;");
            codes.add("A/A+B;");

            // Für diesen Code wird eine falsche DNF geliefert
            codes.add("(F205/F213/F238/F253)+M014+-(920/M014);");

            // Beispiel zur Resolventenmethode aus Wikipedia
            codes.add("b+c+d/a+-b/a+b+-c/a+b+c+-d;");
            codes.add("((IT3/IT4)+Z12/IT4+((A50+Z12)/XL8+(Z12/ZU7/ZU8/ZU9))/IT5)+-(ZG1/ZG2/ZG3/ZG4);");
            codes.add("M214+M214");
            codes.add("(M214/M213)+A1/(M214/A1)+M213");
            codes.add("A111+((M214/M213)+A1/(M214/A1)+M213)");
            codes.add("(001A/018A/131A/201A/218A/101A/115A/118A/201A/215A/218A/301A/318A)+(FW/FS/FV);");
            codes.add("805+(425/425+235+(238/501/266/463/476))+-(055/421/427);");
            codes.add("805+425+235+-(055/421/427);");
            codes.add("463+-(165/184/700/830/494/460/P86/P29+241A/P29+641A/U34);");
            codes.add("531+-(805/806);");

            // Beispiel von Daimler
            codes.add("531+-(805/806);");

            for (String code : codes) {
                BooleanFunction parser = DaimlerCodes.getFunctionParser(code);
                BooleanFunction parserNormal = parser.cloneMe();
                parserNormal.convertToDisjunctiveNormalForm(false);
                assertTrue(truthTableIsEqual(parser, parserNormal), "Error testing code without simplification: " + code);
            }

            BooleanFunction parser = DaimlerCodes.getFunctionParser("A+B+(C+D/E)");
            BooleanFunction parserNormal = DaimlerCodes.getFunctionParser("A+B+(C/E)+(D/E)");
            assertTrue(truthTableIsEqual(parser, parserNormal));
            parserNormal = DaimlerCodes.getFunctionParser("A+B+C+D/A+B+E");
            assertTrue(truthTableIsEqual(parser, parserNormal));


        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @iPartsTest
    public void testCodeSimplification() {
        Map<String, String> noSimplification = new HashMap<>();
        noSimplification.put("M214+M214", "M214");
        noSimplification.put("M214/M214", "M214");
        noSimplification.put("(M214/M213)+A1/(M214/A1)+M213", "A1 and M214 or A1 and M213 or M213 and M214");
        noSimplification.put("A111+((M214/M213)+A1/(M214/A1)+M213)/(M200/-M200)+A2", "A111 and A1 and M214 or A111 and A1 and M213 or A111 and M213 and M214 or A2 and M200 or A2 and not M200");
        noSimplification.put("805+(425/425+235+(238/501/266/463/476))+-(055/421/427);", "805 and 425 and not 055 and not 421 and not 427 or 805 and 425 and 235 and 238 and not 055 and not 421 and not 427 or 805 and 425 and 235 and 501 and not 055 and not 421 and not 427 or 805 and 425 and 235 and 266 and not 055 and not 421 and not 427 or 805 and 425 and 235 and 463 and not 055 and not 421 and not 427 or 805 and 425 and 235 and 476 and not 055 and not 421 and not 427");

        Map<String, String> simplified = new HashMap<>();
        simplified.put("M214+M214", "M214");
        simplified.put("M214/M214", "M214");
        simplified.put("(M214/M213)+A1/(M214/A1)+M213", "A1 and M214 or A1 and M213 or M213 and M214");
        simplified.put("A111+((M214/M213)+A1/(M214/A1)+M213)/(M200/-M200)+A2", "A111 and A1 and M214 or A111 and A1 and M213 or A111 and M213 and M214 or A2");
        simplified.put("805+(425/425+235+(238/501/266/463/476))+-(055/421/427);", "805 and 425 and not 055 and not 421 and not 427");

        try {
            for (Map.Entry<String, String> code : noSimplification.entrySet()) {
                BooleanFunction parser = DaimlerCodes.getFunctionParser(code.getKey());
                BooleanFunction parserNormal = parser.cloneMe();
                parserNormal.convertToDisjunctiveNormalForm(false);
                assertEquals(code.getValue(), parserNormal.toString());
                assertTrue(truthTableIsEqual(parser, parserNormal), "Error testing code: " + code);
                parserNormal = parser.cloneMe();
                parserNormal.convertToDisjunctiveNormalForm(true);
                assertEquals(simplified.get(code.getKey()), parserNormal.toString());
                assertTrue(truthTableIsEqual(parser, parserNormal), "Error testing code: " + code);
            }
            String code = "805+(425/425+235+(238/501/266/463/476))+-(055/421/427);";
            int codeSimplificationLimit = 41;
            BooleanFunction parser = DaimlerCodes.getFunctionParser(code);
            BooleanFunction parserNormal = parser.cloneMe();
            parserNormal.convertToDisjunctiveNormalForm(codeSimplificationLimit);
            assertEquals(noSimplification.get(code), parserNormal.toString());
            // Unterschreite das Limit ab dem nicht gekürzt wird
            codeSimplificationLimit--;
            parserNormal = parser.cloneMe();
            parserNormal.convertToDisjunctiveNormalForm(codeSimplificationLimit);
            assertEquals(simplified.get(code), parserNormal.toString());


        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Alle Möglichkeiten ausprobieren und Testen, ob beide Parser das gleiche zurückliefern
     */
    private boolean truthTableIsEqual(BooleanFunction parser1, BooleanFunction parser2) {
        Random rand = new Random();
        // erstmal testen, ob die Parameter identisch sind. Falls nicht, dann ist eh Hopfen und Malz...
        Set<String> params1 = parser1.getVariableNames();
        Set<String> params2 = parser2.getVariableNames();

        Set<String> allParams = new HashSet<>();
        allParams.addAll(params1);
        allParams.addAll(params2);

        // Bei mehr als 14 Variablen test nicht mehr alle Möglichkeiten, sondern nur noch Zufallswerte
        // Sonst würde der Test einfach zu lange dauern
        int maxRange = (int)Math.pow(2, 14);
        // Ermittele den maximalen Wertebereich. 8 Parameter = 256 etc.
        int range = (int)Math.pow(2, allParams.size());

        for (int i = 0; i < range; i++) {
            if (i >= maxRange) {
                break;
            }

            int testValue = i;

            if (maxRange < range) {
                // Es werden nicht alle getestet, deshalb hier einen Zufallswert
                testValue = rand.nextInt(maxRange);
            }

            int bit = 0; // Index des Bits
            for (String paramName : allParams) {
                // Den Bitwert aus dem i extrahieren und diesen Wert in den parsern setzen

                boolean value = ((testValue >> bit) & 1) == 1;

                parser1.setVariableValue(paramName, value);
                parser2.setVariableValue(paramName, value);
                bit++;
            }

            // Jetzt das Ergebnis testen
            boolean result1;
            try {
                result1 = parser1.calculateWithSyntaxCheck();
            } catch (BooleanFunctionSyntaxException e) {
                System.err.println("BooleanFunction: Syntax error");
                return false;
            }
            boolean result2;
            try {
                result2 = parser2.calculateWithSyntaxCheck();
            } catch (BooleanFunctionSyntaxException e) {
                System.err.println("BooleanFunction: Syntax error");
                return false;
            }
            if (result1 != result2) {
                System.err.println("Error testing thruthtable. Value: " + Long.toString(testValue));
                return false;
            }
        }

        return true;
    }


    private static void compareLists(String[] expectedCodes, Set<String> actualCodes) {
        assertEquals(expectedCodes.length, actualCodes.size());
        int counter = 0;
        for (String actualCode : actualCodes) {
            assertEquals(expectedCodes[counter], actualCode);
            counter++;
        }
    }

    @iPartsTest
    public void testRemoveCodes() {
        Set<String> codesToRemove = new HashSet<>();
        codesToRemove.add("ET");
        codesToRemove.add("TZ");
        codesToRemove.add("TZA");

        // Tests die erfolgreich sein sollen
        _testRemoveCodes(codesToRemove, "ET;", ";", false, "ET");
        _testRemoveCodes(codesToRemove, "ET+(ET+(ET+ET+(ET+ET+(ET))));", ";", false, "ET");
        _testRemoveCodes(codesToRemove, "ET+531+-(805/806);", "531+-(805/806);", false, "ET");
        _testRemoveCodes(codesToRemove, "ET+-(040U/P60+-(197U/033U)/(P55/M016)+-197U);", "-(040U/P60+-(197U/033U)/(P55/M016)+-197U);", false, "ET");
        _testRemoveCodes(codesToRemove, "(ET+531+-(805/806));", "531+-805+-806;", false, "ET");
        _testRemoveCodes(codesToRemove, "(531+-(TZ/806));", "531+-806;", true, "TZ");
        _testRemoveCodes(codesToRemove, "(531+-(TZ/TZA));", "531;", true, "TZ", "TZA");
        _testRemoveCodes(codesToRemove, "M651+M22+075+017+421+-(M013/M014/M005/M020/421);", "M651+M22+075+017+421+-(M013/M014/M005/M020/421);", false, "");
        _testRemoveCodes(codesToRemove, "ET/ET+700+-M177+-M178;", "700+-M177+-M178;", false, "ET");
        _testRemoveCodes(codesToRemove, "(ET/ET+700)+-M177+-M178;", "-M177+-M178/-M177+-M178+700;", false, "ET");
        _testRemoveCodes(codesToRemove, "+ET+531+-(805/806);", "531+-(805/806);", false, "ET");  // Syntaxfehler in Originalstring

        // Tests die fehlschlagen sollen
        _testRemoveCodes(codesToRemove, "-ET+531+-(805/806);", "531+-(805/806);", true, "ET");   // negativer zu entfernender Code

        // und jetzt noch ein paar zufällige Codestrings, die ich in den Daten gefunden habe, die nicht unbedingt neue Testfälle darstellen
        _testRemoveCodes(codesToRemove, "(802/803/804)+ET+(234/237)+(460/494/623);", "(802/803/804)+(234/237)+(460/494/623);", false, "ET");
        _testRemoveCodes(codesToRemove, "(802/803/804)+(ET/ET+(249/234/237))+(460/494);", "802+460/802+494/802+249+460/802+249+494/802+234+460/802+234+494/802+237+460/802+237+494/803+460/803+494/803+249+460/803+249+494/803+234+460/803+234+494/803+237+460/803+237+494/804+460/804+494/804+249+460/804+249+494/804+234+460/804+234+494/804+237+460/804+237+494;", false, "ET");


        _testRemoveCodes(codesToRemove, "V9B+SH1+(SB1/SB5/SF1)/ZH6+SH1+SB5;", "V9B+SH1+(SB1/SB5/SF1)/ZH6+SH1+SB5;", false);

    }


    private void _testRemoveCodes(Set<String> codesToRemove, String codeString, String expectedReducedCodeString, boolean shouldFail, String... expectedCodesRemoved) {
        try {
            Set<String> codesRemoved = new HashSet<>();
            // DaimlerCodes.removeCodes() baut die Coderegel immer neu auf ohne ; am Ende
            assertEquals(expectedReducedCodeString, DaimlerCodes.removeCodes(codeString, codesToRemove, codesRemoved));
            assertEquals(expectedReducedCodeString, DaimlerCodes.removeCodesSafe(codeString, codesToRemove, codesRemoved));
            for (String code : expectedCodesRemoved) {
                if (!code.isEmpty()) {
                    assertTrue(codesRemoved.contains(code));
                }
            }
            if (shouldFail) {
                fail("Fehler bei sicherem Entfernen erwartet für Codestring \"" + codeString + "\".");
            }
        } catch (DaimlerCodesException e) {
            if (!shouldFail) {
                fail("Fehler für Codestring \"" + codeString + "\" mit Meldung: " + e.getLocalizedMessage());
            }
        }
    }

    @iPartsTest
    public void testEqualsCodeString() {
        assertTrue(DaimlerCodes.equalsCodeString(null, ""));
        assertTrue(DaimlerCodes.equalsCodeString("", ";"));
        assertTrue(DaimlerCodes.equalsCodeString(";", ";"));
        assertTrue(DaimlerCodes.equalsCodeString(" ;", ";  "));

        assertTrue(DaimlerCodes.equalsCodeString("A ;", " A  "));
        assertTrue(DaimlerCodes.equalsCodeString("A++B;", "A + B"));
        assertTrue(DaimlerCodes.equalsCodeString("A+-B;", " A -B ; "));

        assertFalse(DaimlerCodes.equalsCodeString(";", "A"));
        assertFalse(DaimlerCodes.equalsCodeString("A", null));
        assertFalse(DaimlerCodes.equalsCodeString("A+B;", "A;"));
        assertFalse(DaimlerCodes.equalsCodeString("A+B;", "A-B;"));
        assertFalse(DaimlerCodes.equalsCodeString("A+B;", "A/B;"));
        assertFalse(DaimlerCodes.equalsCodeString("A+-B;", "A+B;"));
    }

    @iPartsTest
    public void testSyntaxIsOK() {
        assertFalse(DaimlerCodes.syntaxIsOK("A--B"));
        assertFalse(DaimlerCodes.syntaxIsOK("A//B"));
        assertFalse(DaimlerCodes.syntaxIsOK("M213;;"));
        assertFalse(DaimlerCodes.syntaxIsOK("((M214/M213)"));
        assertFalse(DaimlerCodes.syntaxIsOK("(M214/M213)+A1;/(M214/A1)+M213;"));
        assertFalse(DaimlerCodes.syntaxIsOK("(M214/M213)-+A1/(M214/A1)+M213;"));

        assertTrue(DaimlerCodes.syntaxIsOK("(M214/M213)+A1/(M214/A1)+M213"));
        assertTrue(DaimlerCodes.syntaxIsOK("(M214/M213)+-A1/(M214/A1)+M213;"));
        assertTrue(DaimlerCodes.syntaxIsOK("((M214/M213))"));
        assertTrue(DaimlerCodes.syntaxIsOK("(-(M214/M213))"));
    }
}