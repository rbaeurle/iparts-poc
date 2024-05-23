/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.util.test.AbstractTest;

/**
 * Testklasse für {@link de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper}.
 */
public class TestiPartsNumberHelper extends AbstractTest {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    public void testFormatSA() {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        assertEquals("Z123456", numberHelper.unformatSaForDB("Z123456"));
        assertEquals("Z123456", numberHelper.unformatSaForDB("Z 123.456"));
        assertEquals("Z 23456", numberHelper.unformatSaForDB("Z 23456"));
        assertEquals("Z 23456", numberHelper.unformatSaForDB("Z  23.456"));
        assertEquals("ZZ23456", numberHelper.unformatSaForDB("ZZ23456"));
        assertEquals("ZZ23456", numberHelper.unformatSaForDB("Z Z23.456"));
    }

    public void testFormatSAA() {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        assertEquals("Z12345678", numberHelper.unformatSaaForDB("Z12345678"));
        assertEquals("Z12345678", numberHelper.unformatSaaForDB("Z 123.456/78"));
        assertEquals("Z 2345678", numberHelper.unformatSaaForDB("Z 2345678"));
        assertEquals("Z 2345678", numberHelper.unformatSaaForDB("Z  23.456/78"));
        assertEquals("ZZ2345678", numberHelper.unformatSaaForDB("ZZ2345678"));
        assertEquals("ZZ2345678", numberHelper.unformatSaaForDB("Z Z23.456/78"));
    }

    public void testConvertQuantityFormat() {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();

        // ---------------------------
        // Spezialfälle
        // ---------------------------
        assertEquals("", numberHelper.convertQuantityFormat(""));
        assertEquals("", numberHelper.convertQuantityFormat(".000"));
        assertEquals("0", numberHelper.convertQuantityFormat("000."));
        assertEquals("0", numberHelper.convertQuantityFormat("000"));
        assertEquals("7", numberHelper.convertQuantityFormat("007"));
        assertEquals("1234", numberHelper.convertQuantityFormat("001234"));

        // ---------------------------
        // Dezimaltrenner Punkt: '.'
        // ---------------------------
        assertEquals("0", numberHelper.convertQuantityFormat("0.00000000"));
        assertEquals("0.000000001", numberHelper.convertQuantityFormat("0.000000001"));
        assertEquals("0.00000000000000000000000000000000000001", numberHelper.convertQuantityFormat("0.000000000000000000000000000000000000010000"));
        //
        assertEquals("0.1", numberHelper.convertQuantityFormat("0.10"));
        assertEquals("0.1", numberHelper.convertQuantityFormat("0.100"));
        assertEquals("0.1", numberHelper.convertQuantityFormat("0.10000000"));
        //
        assertEquals("0.01", numberHelper.convertQuantityFormat("0.01"));
        assertEquals("0.01", numberHelper.convertQuantityFormat("0.010"));
        assertEquals("0.01", numberHelper.convertQuantityFormat("0.0100000000"));
        //
        assertEquals("0.001", numberHelper.convertQuantityFormat("0.001"));
        assertEquals("0.001", numberHelper.convertQuantityFormat("0.0010"));
        assertEquals("0.001", numberHelper.convertQuantityFormat("0.001000000000"));
        assertEquals("0.001", numberHelper.convertQuantityFormat("0.001"));


        // ---------------------------
        // Dezimaltrenner Komma: ','
        // ---------------------------
        assertEquals("0", numberHelper.convertQuantityFormat("0,00000000"));
        assertEquals("0.000000001", numberHelper.convertQuantityFormat("0,000000001"));
        //
        assertEquals("0.1", numberHelper.convertQuantityFormat("0,10"));
        assertEquals("0.1", numberHelper.convertQuantityFormat("0,100"));
        assertEquals("0.1", numberHelper.convertQuantityFormat("0,10000000"));
        //
        assertEquals("0.01", numberHelper.convertQuantityFormat("0,01"));
        assertEquals("0.01", numberHelper.convertQuantityFormat("0,010"));
        assertEquals("0.01", numberHelper.convertQuantityFormat("0,0100000000"));
        //
        assertEquals("0.001", numberHelper.convertQuantityFormat("0,001"));
        assertEquals("0.001", numberHelper.convertQuantityFormat("0,0010"));
        assertEquals("0.001", numberHelper.convertQuantityFormat("0,001000000000"));
        assertEquals("0.001", numberHelper.convertQuantityFormat("0,001"));

        // --------------------------------
        // Dezimaltrenner Strichpunkt: ','
        // --------------------------------
        assertEquals("0", numberHelper.convertQuantityFormat("0;00000000"));
        assertEquals("0.000000001", numberHelper.convertQuantityFormat("0;000000001"));
    }

    public void testSuccNu() {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        assertEquals(true, numberHelper.containsNumberSequence("*A4475*"));
        assertEquals(true, numberHelper.containsNumberSequence("A4474*"));
        assertEquals(false, numberHelper.containsNumberSequence("*A4*475*"));
        assertEquals(true, numberHelper.containsNumberSequence("*4474*"));
        assertEquals(false, numberHelper.containsNumberSequence("FESTSTELLBR*"));
        assertEquals(true, numberHelper.containsNumberSequence("A4475050130"));
        assertEquals(false, numberHelper.containsNumberSequence("*447*"));
        assertEquals(true, numberHelper.containsNumberSequence("A123xyz1234"));
    }

}
