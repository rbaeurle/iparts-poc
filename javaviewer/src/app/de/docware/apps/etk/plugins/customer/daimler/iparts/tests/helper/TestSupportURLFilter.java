/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SupportURLInterpreter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.tests.AbstractTestSuiteWithIPartsProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.TestiPartsWebservices;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.util.file.DWFile;

/**
 * Unittests für SupportURL_Interpreter
 */
public class TestSupportURLFilter extends AbstractTestSuiteWithIPartsProject {

    private static String[] TEST_CASES = new String[]{
            // Altes Format Version 1
            "https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=WDC2539091F134418:NAV=41,015",
            "https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=WDB2102261B011012,AS=P,CAT=65X:AGG=D613961,AS=PKW,CAT=65C:NAV=20,015",
            "https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=259909:NAV=41,015",
            "https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=WDB96763610103076:NAV=42,Z M03.930",
            "https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=WD4PG2EE3G3138415:NAV=41,015",// VIN
            "https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=205140,AS=P,CAT=66E",
            // Neues Format Version 2
            "https://xentry.daimler.com/xhpi/support/start;bm=3MB3680541M040191;cat=92R;nav=24,060;img=drawing_PV000.002.920.057_version_002",
            "https://xentry.daimler.com/xhpi/support/start;bm=205205;cat=C204;agg=D651921;aggcat=69L;nav=07,013;img=drawing_B07013000032",
            "https://xentry.daimler.com/xhpi/start;bm=C963406;cat=S01;nav=47,Z%20M02.580;img=drawing_SM0258000001",
            "https://xentry.daimler.com/xhpi/start;bm=906991C0991501;cat=06V;nav=13,060;img=drawing_B13060000089"
    };

    public void testSupportURL() {
        int startindex = 0;
        int endindex = TEST_CASES.length;
//        startindex = 1;
//        endindex = startindex+1;
        for (int index = startindex; index < endindex; index++) {
            String testCase = TEST_CASES[index];
            SupportURLInterpreter interpreter = new SupportURLInterpreter(testCase);
            assertEquals(interpreter.isValid(), true);
            switch (index) {
                // Das alte Format Version 1
                case 0:
                    assertEquals(interpreter.getFinOrVinOrBm(), "WDC2539091F134418");
                    assertEquals(interpreter.isFinOrVinIdValid(), true);
                    assertEquals(interpreter.getModelId(getProject()).getModelNumber(), "C253909");
                    assertEquals(interpreter.getKg(), "41");
                    assertEquals(interpreter.getTu(), "015");
                    break;
                case 1:
                    assertEquals(interpreter.getFinOrVinOrBm(), "WDB2102261B011012");
                    assertEquals(interpreter.isFinOrVinIdValid(), true);
                    assertEquals(interpreter.getModelId(getProject()).getModelNumber(), "C210226");
                    assertEquals(interpreter.getKg(), "20");
                    assertEquals(interpreter.getTu(), "015");
                    assertEquals(interpreter.getAs(), "P");
                    assertEquals(interpreter.getProduct(), "65X");
                    assertEquals(interpreter.getAggBM(), "D613961");
                    assertEquals(interpreter.getAggAs(), "PKW");
                    assertEquals(interpreter.getAggProduct(), "65C");
                    break;
                case 2:
                    assertEquals(interpreter.getFinOrVinOrBm(), "259909");
                    assertEquals(interpreter.isFinOrVinIdValid(), false);
//                    assertEquals(interpreter.getModelId(getProject()).getModelNumber(), "C259909");
                    assertEquals(interpreter.getModelId(getProject()).getModelNumber(), ""); // BM nicht in DB
                    assertEquals(interpreter.getKg(), "41");
                    assertEquals(interpreter.getTu(), "015");
                    break;
                case 3:
                    assertEquals(interpreter.getFinOrVinOrBm(), "WDB96763610103076");
                    assertEquals(interpreter.isFinOrVinIdValid(), true);
                    assertEquals(interpreter.getModelId(getProject()).getModelNumber(), "C967636");
                    assertEquals(interpreter.getKg(), "42");
                    assertEquals(interpreter.getTu(), "Z M03.930");
                    break;
                case 4:
                    assertEquals(interpreter.getFinOrVinOrBm(), "WD4PG2EE3G3138415");
                    assertEquals(interpreter.isFinOrVinIdValid(), true);
                    assertEquals(interpreter.getVinId().isValidId(), true);
                    assertEquals(interpreter.getModelId(getProject()).getModelNumber(), "");
                    assertEquals(interpreter.getKg(), "41");
                    assertEquals(interpreter.getTu(), "015");
                    break;
                case 5:
                    assertEquals(interpreter.isFinOrVinIdValid(), false);
                    assertEquals(interpreter.getVinId().isValidId(), false);
                    assertEquals(interpreter.isFinIdValid(), false);
                    assertEquals(interpreter.getModelId(getProject()).getModelNumber(), "C205140");
                    assertEquals(interpreter.getProduct(), "66E");
                    assertEquals(interpreter.getKg(), "");
                    assertEquals(interpreter.getTu(), "");
                    break;

                // Das neue Format Version 2
                case 6:
                    assertEquals(interpreter.getFinOrVinOrBm(), "3MB3680541M040191");
                    assertEquals(interpreter.isFinOrVinIdValid(), true);
                    assertEquals(interpreter.getModelId(getProject()).getModelNumber(), "C368054");
                    assertEquals(interpreter.getProduct(), "92R");
                    assertEquals(interpreter.getKg(), "24");
                    assertEquals(interpreter.getTu(), "060");
                    break;
                case 7:
                    assertEquals(interpreter.getFinOrVinOrBm(), "205205");
                    assertEquals(interpreter.isFinOrVinIdValid(), false);
                    assertEquals(interpreter.getProduct(), "C204");
                    assertEquals(interpreter.getAggBM(), "D651921");
                    assertEquals(interpreter.getAggProduct(), "69L");
                    assertEquals(interpreter.getKg(), "07");
                    assertEquals(interpreter.getTu(), "013");
                    break;
                case 8:
                    assertEquals(interpreter.getFinOrVinOrBm(), "C963406");
                    assertEquals(interpreter.isFinOrVinIdValid(), false);
                    assertEquals(interpreter.getProduct(), "S01");
                    assertEquals(interpreter.getKg(), "47");
                    assertEquals(interpreter.getTu(), "Z M02.580");
                    break;
                case 9:
                    assertEquals(interpreter.getFinOrVinOrBm(), "906991C0991501");
                    assertEquals(interpreter.isFinOrVinIdValid(), false);
                    assertEquals(interpreter.getProduct(), "06V");
                    assertEquals(interpreter.getKg(), "13");
                    assertEquals(interpreter.getTu(), "060");
                    break;
            }
        }
    }

    @Override
    protected String getTestCaseRelativeDir() {
        return ("de_docware_apps_etk_plugins_customer_daimler_iparts_webservice_iPartsWebservicePlugin" + "_V" + getTestVersion());
    }

    // createTempDir() überschreiben, wenn man die Daten direkt von einem lokalen Verzeichnis ohne Kopieren in ein temporäres
    // Testverzeichnis verwenden will
    @Override
    protected DWFile createTempDir() {
        // Verzeichnis und VM Option der Webservices übernehmen
        if (StartParameter.getSystemPropertyBoolean(TestiPartsWebservices.VM_PARAMETER_USE_LOCAL_DIR, false)) {
            setDeleteTmpBaseDir(false);
            return DWFile.get(TestiPartsWebservices.LOCAL_DIR).getChild(getTestCaseRelativeDir());
        } else {
            useTestCaseFilesFromVCS();
            return super.createTempDir();
        }
    }

    @Override
    protected boolean isReadOnlyCloneDB() {
        return true;
    }
}
