package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.util.test.AbstractTest;

/**
 *
 */
public class TestDIALOGImportHelper extends AbstractTest {

    private static iPartsNumberHelper numberHelper = new iPartsNumberHelper();

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    public void testFormatSaSAA() {
        try {
            assertEquals(numberHelper.unformatSaaForDB("Z 1234567"), "Z 1234567");
            assertEquals(numberHelper.unformatSaaForDB(" 1234567"), "Z 1234567");
            assertEquals(numberHelper.unformatSaaForDB("Z3146030"), "ZZ3146030");

        } catch (RuntimeException e) {
            fail(e.toString());
        }
    }

    public void testValidateSaSaa() {

        // korrekte Fälle
        try {
            // SA
            numberHelper.validateSaSaa("Z123456", iPartsNumberHelper.SA_PATTERN, "egal");
            numberHelper.validateSaSaa("Z 12345", iPartsNumberHelper.SA_PATTERN, "egal");
            numberHelper.validateSaSaa("ZM12345", iPartsNumberHelper.SA_PATTERN, "egal");

            // SAA
            numberHelper.validateSaSaa("Z12345678", iPartsNumberHelper.SAA_PATTERN, "egal");
            numberHelper.validateSaSaa("ZZ3146030", iPartsNumberHelper.SAA_PATTERN, "egal");

            numberHelper.unformatSaaForDB("ZZ3146030");
        } catch (Exception e) {
            fail(e.toString());
        }

        // fehlerhafte SA Fälle

        /**
         * SA
         */
        String saSaa;
        try {
            saSaa = "z123456";
            numberHelper.validateSaSaa(saSaa, iPartsNumberHelper.SA_PATTERN, "egal");
            fail("'" + saSaa + "' wurde fälschlicherweise als korrekt angesehen");
        } catch (Exception e) {
        }

        try {
            saSaa = "Z1234567";
            numberHelper.validateSaSaa(saSaa, iPartsNumberHelper.SA_PATTERN, "egal");
            fail("'" + saSaa + "' wurde fälschlicherweise als korrekt angesehen");
        } catch (Exception e) {
        }

        try {
            saSaa = "Z1234";
            numberHelper.validateSaSaa(saSaa, iPartsNumberHelper.SA_PATTERN, "egal");
            fail("'" + saSaa + "' wurde fälschlicherweise als korrekt angesehen");
        } catch (Exception e) {
        }

        try {
            saSaa = "Z123 45";
            numberHelper.validateSaSaa(saSaa, iPartsNumberHelper.SA_PATTERN, "egal");
            fail("'" + saSaa + "' wurde fälschlicherweise als korrekt angesehen");
        } catch (Exception e) {
        }

        /**
         * SAA
         */

        try {
            saSaa = "Z123456AB";
            numberHelper.validateSaSaa(saSaa, iPartsNumberHelper.SAA_PATTERN, "egal");

            fail("'" + saSaa + "' wurde fälschlicherweise als korrekt angesehen");
        } catch (Exception e) {
        }

        try {
            saSaa = "Z314603";
            numberHelper.validateSaSaa(saSaa, iPartsNumberHelper.SAA_PATTERN, "egal");

            fail("'" + saSaa + "' wurde fälschlicherweise als korrekt angesehen");
        } catch (Exception e) {
        }
    }
}
