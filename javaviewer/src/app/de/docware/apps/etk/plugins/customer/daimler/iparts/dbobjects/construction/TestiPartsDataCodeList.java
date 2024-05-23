/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.CodeRule;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.test.AbstractTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suedkamp on 26.01.2016.
 */
public class TestiPartsDataCodeList extends AbstractTest {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    public void testIsFittingDate() {
        String[][] codeDataList;
        List<CodeRule> codeRules;
        CodeRule codeRule;

        // Ende offen
        codeDataList = new String[][]{
                { "20010101", "" }
        };
        codeRules = createCodeRules(codeDataList);
        codeRule = iPartsDataCodeList.getMatchingCodeRule(codeRules, "20050101");
        assertEquals("20010101", codeRule.getCodeDataId().getSdata());

        // Ende begrenzt
        codeDataList = new String[][]{
                { "20010101", "20100101" }
        };
        codeRules = createCodeRules(codeDataList);
        codeRule = iPartsDataCodeList.getMatchingCodeRule(codeRules, "20050101");
        assertEquals("20010101", codeRule.getCodeDataId().getSdata());

        // mehrere Zeiträume
        codeDataList = new String[][]{
                { "20010101", "20100101" },
                { "20100101", "20150101" }
        };
        codeRules = createCodeRules(codeDataList);
        codeRule = iPartsDataCodeList.getMatchingCodeRule(codeRules, "20120101");
        assertEquals("20100101", codeRule.getCodeDataId().getSdata());

        // mehrere Zeiträume; Datum genau auf Grenze (Enddatum ist exklusiv)
        codeDataList = new String[][]{
                { "20010101", "20100101" },
                { "20100101", "20150101" }
        };
        codeRules = createCodeRules(codeDataList);
        codeRule = iPartsDataCodeList.getMatchingCodeRule(codeRules, "20100101");
        assertEquals("20100101", codeRule.getCodeDataId().getSdata());

        // außerhalb Zeitraum; laut Herrn Müller Regel mit kleinstem von-datum nehmen
        codeDataList = new String[][]{
                { "20010101", "20100101" },
                { "20100101", "20150101" }
        };
        codeRules = createCodeRules(codeDataList);
        codeRule = iPartsDataCodeList.getMatchingCodeRule(codeRules, "20170101");
        assertEquals("20010101", codeRule.getCodeDataId().getSdata());


    }

    private static List<CodeRule> createCodeRules(String[][] codeDataList) {
        List<CodeRule> codeRules = new ArrayList<CodeRule>();
        for (String[] codeData : codeDataList) {
            String codeId = ""; // interessiert nicht
            String seriesNo = ""; // interessiert nicht
            String prodGroup = ""; // interessiert nicht
            String dateFrom = codeData[0];
            String dateTo = codeData[1];
            iPartsCodeDataId id = new iPartsCodeDataId(codeId, seriesNo, prodGroup, dateFrom, iPartsImportDataOrigin.UNKNOWN);
            CodeRule codeRule = new CodeRule(id, dateTo, new EtkMultiSprache());
            codeRules.add(codeRule);
        }
        return codeRules;
    }
}
