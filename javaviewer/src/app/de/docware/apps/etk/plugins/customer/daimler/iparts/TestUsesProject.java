/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPEMPseudoDateCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.tests.AbstractTestSuiteWithIPartsProject;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.framework.utils.VarParam;
import de.docware.util.file.DWFile;
import junit.framework.Test;

import java.util.Set;

public class TestUsesProject extends AbstractTestSuiteWithIPartsProject {

    // Per Java Umgebungsvariable Tests aus lokalem Verzeichnis ausführen
    public static final String LOCAL_DIR = "D:/DataNoS/JAVA_UnitTests";
    public static final String VM_PARAMETER_USE_LOCAL_DIR = "dw_TestUsesProject_useLocalDir";

    /**
     * Eine manuelle {@link junit.framework.TestSuite} und {@link de.docware.util.test.FrameworkTestSetup} erzeugen, um
     * komplette Tests mit einem globalen SetUp und TearDown zu ermöglichen inkl. Nachtests.
     *
     * @return
     */
    public static Test suite() {
        return createTestSuite(TestUsesProject.class);
    }

    public TestUsesProject() {
    }

    public TestUsesProject(TestUsesProject globalTest, String methodName) {
        super(globalTest, methodName);
    }

    public void testPartNumber() {
        EtkProject project = getProject();
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        VarParam<String> resultSearchText = new VarParam<>();

        assertTrue(numberHelper.testPartNumber("A4472670111", resultSearchText, project));
        assertEquals("A4472670111", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("*A4472670111", resultSearchText, project));
        assertEquals("A4472670111", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("*A4472670111*", resultSearchText, project));
        assertEquals("A4472670111", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("*A4472*670111*", resultSearchText, project));
        assertEquals("A4472670111", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("*A44726*111*", resultSearchText, project));
        assertEquals("A44726*111*", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("A447*6*0111*", resultSearchText, project));
        assertEquals("A447*6*0111*", resultSearchText.getValue());

        assertFalse(numberHelper.testPartNumber("A447*6*111*", resultSearchText, project));
        assertEquals("A447*6*111*", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("A4472*", resultSearchText, project));
        assertEquals("A4472*", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("*A4472*", resultSearchText, project));
        assertEquals("A4472*", resultSearchText.getValue());

        assertFalse(numberHelper.testPartNumber("*A4*472*", resultSearchText, project));
        assertEquals("*A4*472*", resultSearchText.getValue());

        assertFalse(numberHelper.testPartNumber("FESTSTELLBR*", resultSearchText, project));
        assertEquals("FESTSTELLBR*", resultSearchText.getValue());

        assertFalse(numberHelper.testPartNumber("FESTSTELLBREMSE", resultSearchText, project));
        assertEquals("FESTSTELLBREMSE", resultSearchText.getValue());

        assertFalse(numberHelper.testPartNumber("FESTS TELLBREMSE", resultSearchText, project));
        assertEquals("FESTS TELLBREMSE", resultSearchText.getValue());

        assertFalse(numberHelper.testPartNumber("FESTS*TELLBREMSE", resultSearchText, project));
        assertEquals("FESTS*TELLBREMSE", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("A 447 267 01 11", resultSearchText, project));
        assertEquals("A4472670111", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("4472*", resultSearchText, project));
        assertEquals("4472*", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("*4472*", resultSearchText, project));
        assertEquals("*4472*", resultSearchText.getValue());

        assertTrue(numberHelper.testPartNumber("**4472**", resultSearchText, project));
        assertEquals("*4472*", resultSearchText.getValue());

        assertFalse(numberHelper.testPartNumber("*4*472*", resultSearchText, project));
        assertEquals("*4*472*", resultSearchText.getValue());
    }

    public void testDictTextCache() {
        // Der DictTextCache braucht ein MqProject
        iPartsPlugin.__internal_setMqProject(getProject());

        assertFalse(DictTextCache.hasInstance(DictTextKindTypes.FOOTNOTE, Language.DE.getCode(), false));
        DictTextCache textCache = DictTextCache.getInstance(DictTextKindTypes.FOOTNOTE, Language.DE.getCode());
        assertFalse(textCache.getAllTextsToTextIds().isEmpty());

        Set<String> texts = textCache.searchTexts("rot").keySet();
        assertFalse(texts.isEmpty());
        for (String text : texts) {
            assertTrue(text.toLowerCase().equals("rot"));
        }

        texts = textCache.searchTexts("ro*").keySet();
        assertFalse(texts.isEmpty());
        boolean redFound = false;
        for (String text : texts) {
            assertTrue(text.toLowerCase().startsWith("ro"));
            if (text.toLowerCase().startsWith("rot")) { // "rot" muss auch dabei sein
                redFound = true;
            }
        }
        assertTrue(redFound);

        texts = textCache.searchTexts("*ot").keySet();
        assertFalse(texts.isEmpty());
        redFound = false;
        for (String text : texts) {
            assertTrue(text.toLowerCase().endsWith("ot"));
            if (text.toLowerCase().endsWith("rot")) { // "rot" muss auch dabei sein
                redFound = true;
            }
        }
        assertTrue(redFound);

        texts = textCache.searchTexts("*o*").keySet();
        assertFalse(texts.isEmpty());
        redFound = false;
        for (String text : texts) {
            assertTrue(text.toLowerCase().contains("o"));
            if (text.toLowerCase().contains("rot")) { // "rot" muss auch dabei sein
                redFound = true;
            }
        }
        assertTrue(redFound);

        assertTrue(textCache.searchTexts("*foooooooo*").isEmpty());
    }

    /**
     * Ab DAIMLER-13914 sollen bei den Pseudo-Terminen nur noch die ersten 8 Stellen berücksichtigt werden.
     */
    public void testPseudoPemCache() {

/* Die Werte für Pseudo-PEM-Datumsangaben aus der Datenbank:
    20440404000000
    20440401000000
    20440405000000
    20440504000000
    20440403000000
    20440408000000
    20440409000000
    20440410000000
    20440412000000
    20440101
    20440523000000
    20441212000000
 */
        // Datumsangabe viel zu kurz
        assertEquals(false, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("123"));

        // Keine Datumsangabe
        assertEquals(false, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate(""));

        // Nicht enthaltene Datumsangaben müssen alle auf Fehler laufen
        // Fehler im Jahr
        assertEquals(false, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20540404000000"));
        assertEquals(false, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20540404"));
        // Fehler im Monat
        assertEquals(false, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440904000000"));
        assertEquals(false, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440904"));
        // Fehler im Tag
        assertEquals(false, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440413000000"));
        assertEquals(false, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440413"));

        // 20440404000000
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440404"));
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("2044040499"));
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440404000000"));
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440404999999"));

        // 20440401000000
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440401"));
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("2044040199"));
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440401000000"));

        // 20440101
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440101"));
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("2044010199"));
        assertEquals(true, iPartsPEMPseudoDateCache.getInstance(getProject()).isPEMPseudoDate("20440101000000"));
    }

    // Nimm die Webservice Config und Datenbank um ein EtkProject zu erhalten

    @Override
    protected String getTestCaseRelativeDir() {
        return ("de_docware_apps_etk_plugins_customer_daimler_iparts_webservice_iPartsWebservicePlugin" + "_V" + getTestVersion());
    }

    /**
     * Gibt den erwarteten Namen der Konfigurationsdatei zurück.
     * Alle .config gehen auf dieselbe DWK - es ist also egal welche der vorhandenen wir nehmen
     *
     * @return
     */
    @Override
    protected String getConfigFileName() {
        return "etk_viewer_edit.config";
    }

    // createTempDir() überschreiben, wenn man die Daten direkt von einem lokalen Verzeichnis ohne Kopieren in ein temporäres
    // Testverzeichnis verwenden will
    @Override
    protected DWFile createTempDir() {
        if (StartParameter.getSystemPropertyBoolean(VM_PARAMETER_USE_LOCAL_DIR, false)) {
            setDeleteTmpBaseDir(false);
            return DWFile.get(LOCAL_DIR).getChild(getTestCaseRelativeDir());
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
