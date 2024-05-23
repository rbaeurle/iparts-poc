/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.containers.XMLConfigContainer;
import de.docware.util.StrUtils;
import de.docware.util.test.AbstractTest;

import java.util.HashSet;
import java.util.Set;

/**
 * Testklasse für das Parsen der Baumusterlisten im EPC Importer für Produkt-Baumuster-Zuordnung
 */
public class TestEPCModelAggregateImporter extends AbstractTest {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    @Override
    protected void globalSetUp() throws Exception {
        super.globalSetUp();
        iPartsPlugin iPartsPlugin = new iPartsPlugin();
        ConfigBase pluginConfig = new ConfigBase(XMLConfigContainer.getInstanceInMemory());
        iPartsPlugin.initPlugin(pluginConfig);
    }


    public void testParseModelNumberStringSingleModel() {

        String singleModelNo = "730.702";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("730702");
        Set<String> result = EPCModelAggregateImporter.parseModelNumberString(null, singleModelNo);
        assertEquals(expectedResult, result);

        singleModelNo = "343.919-001";
        expectedResult.clear();
        expectedResult.add("343919001");
        result = EPCModelAggregateImporter.parseModelNumberString(null, singleModelNo);
        assertEquals(expectedResult, result);
    }

    public void testParseModelNumberStringMissingComma() {

        String modelNumbers = "730.702730";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("730702");
        expectedResult.add("730730");
        Set<String> result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);

    }

    public void testParseModelNumberStringMutlipleCommaSeparated() {

        String modelNumbers = "314.820,830, 316.821,831";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("314820");
        expectedResult.add("314830");
        expectedResult.add("316821");
        expectedResult.add("316831");
        Set<String> result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);

    }

    public void testParseModelNumberStringMutlipleCommaAndSpaceSeparated() {

        String modelNumbers = "314.910,944 964,965";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("314910");
        expectedResult.add("314944");
        expectedResult.add("314964");
        expectedResult.add("314965");
        Set<String> result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);

    }


    public void testParseModelNumberStringMutlipleCommaSeparated9Chars() {

        String modelNumbers = "712.001006,030031,033035";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("712001");
        expectedResult.add("712006");
        expectedResult.add("712030");
        expectedResult.add("712031");
        expectedResult.add("712033");
        expectedResult.add("712035");
        Set<String> result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);

    }

    public void testParseModelNumberStringWithMinusInMiddle() {

        String modelNumbers = "731.001005-007010,011";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("731001");
        expectedResult.add("731005007");
        expectedResult.add("731010");
        expectedResult.add("731011");
        Set<String> result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);

    }

    public void testParseModelNumberStringWithMinusAtEnd() {

        String modelNumbers = "731.000001,002004,005007,009010-012";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("731000");
        expectedResult.add("731001");
        expectedResult.add("731002");
        expectedResult.add("731004");
        expectedResult.add("731005");
        expectedResult.add("731007");
        expectedResult.add("731009");
        expectedResult.add("731010012");
//        expectedResult.add("731012");
        Set<String> result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);

    }

    public void testParseModelNumberStringWithMultipleMinus() {
        String modelNumbers = "396.900-008, -020         -022";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("396900008");
        expectedResult.add("396900020");
        expectedResult.add("396900022");
        Set<String> result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);

        // konstruierter Fall
        modelNumbers = "396.900-008, -020         -022,731.000732.001,002004,005007,009010-012";
        expectedResult = new HashSet<>();
        expectedResult.add("396900008");
        expectedResult.add("396900020");
        expectedResult.add("396900022");
        expectedResult.add("731000");
        expectedResult.add("732001");
        expectedResult.add("732002");
        expectedResult.add("732004");
        expectedResult.add("732005");
        expectedResult.add("732007");
        expectedResult.add("732009");
        expectedResult.add("732010012");
        result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);
    }

    public void testParseModelNumberStringWithDoubleSeries() {

        String modelNumbers = "731.000732.001,002004,005007,009010-012";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("731000");
        expectedResult.add("732001");
        expectedResult.add("732002");
        expectedResult.add("732004");
        expectedResult.add("732005");
        expectedResult.add("732007");
        expectedResult.add("732009");
        expectedResult.add("732010012");
        //expectedResult.add("732012");
        Set<String> result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);

        modelNumbers = "731.000001,002732.004,005007,009010-012";
        expectedResult = new HashSet<>();
        expectedResult.add("731000");
        expectedResult.add("731001");
        expectedResult.add("731002");
        expectedResult.add("732004");
        expectedResult.add("732005");
        expectedResult.add("732007");
        expectedResult.add("732009");
        expectedResult.add("732010012");
//        expectedResult.add("732012");
        result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);

        modelNumbers = "731.000001,732.002004,005007,009010-012";
        expectedResult = new HashSet<>();
        expectedResult.add("731000");
        expectedResult.add("731001");
        expectedResult.add("732002");
        expectedResult.add("732004");
        expectedResult.add("732005");
        expectedResult.add("732007");
        expectedResult.add("732009");
        expectedResult.add("732010012");
        //expectedResult.add("732012");
        result = EPCModelAggregateImporter.parseModelNumberString(null, modelNumbers);
        assertEquals(expectedResult, result);
    }

    public void testParseModelNumbersFromSaaImproter() {
        EPCImportHelper helper = new EPCImportHelper(null, null, "tableName");
        // Test für EPCSaaMasterDataImporter DAIMLER-6694
        // BIS in Modellist
        String modelNumbers = "000020099";
        String seriesNumber = "123";
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add(seriesNumber + "000");
        expectedResult.add(seriesNumber + "020");
        expectedResult.add(seriesNumber + "099");
        Set<String> result = new HashSet<>();
        helper.combineSeriesAndModelNumbersForSAA(null, result,
                                                  seriesNumber, modelNumbers);
        assertEquals(expectedResult, result);

        modelNumbers = "000BIS099";
        expectedResult = new HashSet<>();
        for (int lfdNr = 0; lfdNr <= 99; lfdNr++) {
            expectedResult.add(seriesNumber + StrUtils.leftFill(String.valueOf(lfdNr), 3, '0'));
        }
        result = new HashSet<>();
        helper.combineSeriesAndModelNumbersForSAA(null, result,
                                                  seriesNumber, modelNumbers);
        assertEquals(expectedResult, result);

        modelNumbers = "000BIS199300BIS499";
        expectedResult = new HashSet<>();
        for (int lfdNr = 0; lfdNr <= 199; lfdNr++) {
            expectedResult.add(seriesNumber + StrUtils.leftFill(String.valueOf(lfdNr), 3, '0'));
        }
        for (int lfdNr = 300; lfdNr <= 499; lfdNr++) {
            expectedResult.add(seriesNumber + StrUtils.leftFill(String.valueOf(lfdNr), 3, '0'));
        }
        result = new HashSet<>();
        helper.combineSeriesAndModelNumbersForSAA(null, result,
                                                  seriesNumber, modelNumbers);
        assertEquals(expectedResult, result);
    }

    public void testParseCodeNumbersFromSaImproter() {
        // Test für EPCSaProductStructureImporter DAIMLER-6556
        // BIS in CodeList
        String codeNumbers = "422580";
        String expectedResult = "422/580";
        EPCImportHelper helper = new EPCImportHelper(null, null, "tableName");
        String result = helper.calculateCodes(codeNumbers);
        assertEquals(expectedResult, result);

        codeNumbers = "C79X02X12BISX15X23X35X38X79";
        expectedResult = "C79/X02/X12-X15/X23/X35/X38/X79";
        result = helper.calculateCodes(codeNumbers);
        assertEquals(expectedResult, result);

        codeNumbers = "NB0NB1NB2NB3NB7NB8ND0BISND9N04NB9";
        expectedResult = "NB0/NB1/NB2/NB3/NB7/NB8/ND0-ND9/N04/NB9";
        result = helper.calculateCodes(codeNumbers);
        assertEquals(expectedResult, result);

    }
}