/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.dialog;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualCalcFieldDocuRel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterPartsEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.tests.AbstractTestSuiteWithIPartsProject;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import junit.framework.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit-Tests für die iParts DIALOG Filter
 */
public class TestDIALOGModelFilter extends AbstractTestSuiteWithIPartsProject {

    private static final String VM_PARAMETER_USE_LOCAL_DIR = "dw_TestDIALOGModelFilter_useLocalDir";

    /**
     * Eine manuelle {@link junit.framework.TestSuite} und {@link de.docware.util.test.FrameworkTestSetup} erzeugen, um
     * komplette Tests mit einem globalen SetUp und TearDown zu ermöglichen inkl. Nachtests.
     *
     * @return
     */
    public static Test suite() {
        return createTestSuite(TestDIALOGModelFilter.class);
    }

    public TestDIALOGModelFilter() {
    }

    public TestDIALOGModelFilter(TestDIALOGModelFilter globalTest, String methodName) {
        super(globalTest, methodName);
    }

    // setUp() überschreiben, wenn man die Daten von einem lokalen Verzeichnis in ein temporäres Testverzeichnis kopieren will
//    @Override
//    protected void setUp() throws Exception {
//        setOverriddenLocalTestcaseBaseDir(DWFile.get("E:/Testprojekte"));
//        super.setUp();
//    }

    @Override
    protected DWFile createTempDir() {
        if (StartParameter.getSystemPropertyBoolean(VM_PARAMETER_USE_LOCAL_DIR, false)) {
            setDeleteTmpBaseDir(false);
            return DWFile.get("D:/DataNoS/JAVA_UnitTests").getChild(getTestCaseRelativeDir());
        } else {
            useTestCaseFilesFromVCS();
            return super.createTempDir();
        }
    }

    @Override
    protected String getTestCaseRelativeDir() {
        return ("de_docware_apps_etk_plugins_customer_daimler_iparts_webservice_iPartsWebservicePlugin" + "_V" + getTestVersion());
    }

    @Override
    protected boolean isReadOnlyCloneDB() {
        return true;
    }

    public void testGetPositionVariants() {
        iPartsAssemblyId assemblyId = new iPartsAssemblyId("C01_21_300_00001", "");
        iPartsDataAssembly dataAssembly = new iPartsDataAssembly(getProject(), assemblyId.getKVari(), assemblyId.getKVer(), false);
        System.out.println(dataAssembly.getEbeneName());
        if (dataAssembly.loadFromDB(assemblyId)) {
            for (EtkDataPartListEntry partListEntry : dataAssembly.getPartListUnfiltered(null, true, true).getAsList()) {
                String k_lfdnr = partListEntry.getAsId().getKLfdnr();
                if (k_lfdnr.equals("00001")) {
                    List<EtkDataPartListEntry> pvList = dataAssembly.getPositionVariants(partListEntry, false);
                    assertEquals(4, pvList.size());
                    assertTrue(isLfdnrInList("00001", pvList));
                    assertTrue(isLfdnrInList("00002", pvList));
                    assertTrue(isLfdnrInList("00003", pvList));
                    assertTrue(isLfdnrInList("00004", pvList));
                    pvList = dataAssembly.getPositionVariants(partListEntry, true);
                    assertEquals(3, pvList.size());
                    assertTrue(isLfdnrInList("00002", pvList));
                    assertTrue(isLfdnrInList("00003", pvList));
                    assertTrue(isLfdnrInList("00004", pvList));
                }
            }
        } else {
            fail("Failed to load assembly from DB");
        }
    }

    private boolean isLfdnrInList(String lfdnr, List<EtkDataPartListEntry> pvList) {
        for (EtkDataPartListEntry pv : pvList) {
            if (pv.getAsId().getKLfdnr().equals(lfdnr)) {
                return true;
            }
        }
        return false;
    }

    public void testScoringInModelFilter() {
        // Test BM-Scoring im Baumusterfilter (technische Gruppen, bm-bildende Code, usw)
        boolean withAAModelBuildingCode = false; // optional mit oder ohne AA als bm-bildenden Code
        String currentModel = "C205001";
        DIALOGModelFilter modelFilter = new DIALOGModelFilter(currentModel, withAAModelBuildingCode);
        modelFilter.createModelBuildingCodeSets(getProject());
        List<iPartsDataPartListEntry> positionVariants = new DwList<>();

        iPartsDataPartListEntry validPartListEntry = createTestPartListEntry("1", "M654+M16;", "10");
        validPartListEntry.setFieldValue(iPartsConst.FIELD_K_MATNR, "A2075400099", DBActionOrigin.FROM_DB);
        positionVariants.add(validPartListEntry);
        positionVariants.add(createTestPartListEntry("2", "HA", "10"));
        // Damit sie in der gleichen Zeitscheibe landen
        for (iPartsDataPartListEntry pv : positionVariants) {
            pv.setTimeSliceDates(true, 0, Long.MAX_VALUE);
        }
        iPartsFilterPartsEntries filterPartsEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        Set<String> modelBuildingCodes = getModelBuildingCodes(withAAModelBuildingCode, currentModel);
        modelFilter.filterByCodeRule(filterPartsEntries, modelBuildingCodes, -1, null);
        assertEquals(1, filterPartsEntries.getVisibleEntries().size());
        assertTrue(filterPartsEntries.isEntryVisible(validPartListEntry));

        //###############DAIMLER-7654####################
        // Echtes Beispiel
        positionVariants.clear();
        modelBuildingCodes.clear();
        currentModel = "C909632";
        modelFilter = new DIALOGModelFilter(currentModel, withAAModelBuildingCode);
        modelFilter.createModelBuildingCodeSets(getProject());
        validPartListEntry = createTestPartListEntry("00013", "IT3/IT4+-(N05/N07/ZG1/ZG2/ZG3/ZG4);", "100");
        iPartsDataPartListEntry notValidPartListEntry = createTestPartListEntry("00010", "-(N05/N07/ZG1/ZG2/ZG3/ZG4);", "100");
        validPartListEntry.setTimeSliceDates(true, 0, Long.MAX_VALUE);
        notValidPartListEntry.setTimeSliceDates(true, 0, Long.MAX_VALUE);
        positionVariants.add(validPartListEntry);
        positionVariants.add(notValidPartListEntry);

        filterPartsEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        modelBuildingCodes = getModelBuildingCodes(withAAModelBuildingCode, currentModel);
        modelFilter.filterByCodeRule(filterPartsEntries, modelBuildingCodes, -1, null);
        assertEquals(1, filterPartsEntries.getVisibleEntries().size());
        assertTrue(filterPartsEntries.isEntryVisible(validPartListEntry));

        // 1. ausgedachtes Beispiel von Herr Müller

        positionVariants.clear();
        modelBuildingCodes.clear();
        currentModel = "C117346";
        modelFilter = new DIALOGModelFilter(currentModel, withAAModelBuildingCode);
        modelFilter.createModelBuildingCodeSets(getProject());
        iPartsDataPartListEntry firstEntry = createTestPartListEntry("00001", "M270+M20+490;", "100");
        iPartsDataPartListEntry secondEntry = createTestPartListEntry("00002", "M270+490;", "100");
        iPartsDataPartListEntry thirdEntry = createTestPartListEntry("00003", "490;", "100");
        iPartsDataPartListEntry fourthEntry = createTestPartListEntry("00004", "580;", "100");
        iPartsDataPartListEntry fifthEntry = createTestPartListEntry("00005", "M270+640;", "100");

        positionVariants.add(firstEntry);
        positionVariants.add(secondEntry);
        positionVariants.add(thirdEntry);
        positionVariants.add(fourthEntry);
        positionVariants.add(fifthEntry);

        // Damit sie in der gleichen Zeitscheibe landen
        for (iPartsDataPartListEntry pv : positionVariants) {
            pv.setTimeSliceDates(true, 0, Long.MAX_VALUE);
        }

        filterPartsEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        modelBuildingCodes = getModelBuildingCodes(withAAModelBuildingCode, currentModel);
        modelFilter.filterByCodeRule(filterPartsEntries, modelBuildingCodes, -1, null);

        assertEquals(3, filterPartsEntries.getVisibleEntries().size());
        // 00001, 00002 und 00003 werden aufgrund des techn. Code "490" in eine Gruppe gelegt. 00001 hat 2 bm-bildende Code
        // und verdrängt somit 00002 und 00003
        assertTrue(filterPartsEntries.isEntryVisible(firstEntry));
        // 00004 und 00005 kommen jeweils in ihre eigenen Gruppen: "580" und "640". Weil sonst keine Position in diese
        // Gruppen kommt, kommen beide weiter.
        assertTrue(filterPartsEntries.isEntryVisible(fourthEntry));
        assertTrue(filterPartsEntries.isEntryVisible(fifthEntry));

        // Jetzt der Test mit einer Wegfallsachnummer. 00002 würde eigentlich gegen 00001 verlieren. Nachdem 00002 jetzt
        // eine Wegfallsachnummer bekommt, müsste ihre Trefferanzahl "3" sein. Dadurch werden 00001 und 00003 in der Gruppe
        // "490" verdrängt.
        secondEntry.getPart().setPKValues("A0000101099", "", DBActionOrigin.FROM_DB);
        filterPartsEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        modelFilter.filterByCodeRule(filterPartsEntries, modelBuildingCodes, -1, null);
        assertEquals(3, filterPartsEntries.getVisibleEntries().size());
        // 00001, 00002 und 00003 werden aufgrund des techn. Code "490" in eine Gruppe gelegt. 00002 hat nun eine
        // Wegfallsachnummer und somit die maximal Anzahl von möglichen bm-bildenden Code (Anzahl bm-bildende Code am Baumuster)
        // und verdrängt somit 00001 und 00003
        assertTrue(filterPartsEntries.isEntryVisible(secondEntry));
        // 00004 und 00005 kommen jeweils in ihre eigenen Gruppen: "580" und "640". Weil sonst keine Position in diese
        // Gruppen kommt, kommen beide weiter.
        assertTrue(filterPartsEntries.isEntryVisible(fourthEntry));
        assertTrue(filterPartsEntries.isEntryVisible(fifthEntry));


        // 2. ausgedachtes Beispiel von Herr Müller
        positionVariants.clear();

        firstEntry = createTestPartListEntry("00001", "M270;", "100");
        secondEntry = createTestPartListEntry("00002", "M270+M20+490;", "100");
        thirdEntry = createTestPartListEntry("00003", "M20+580;", "100");
        fourthEntry = createTestPartListEntry("00004", "M270+M005;", "100");
        fifthEntry = createTestPartListEntry("00005", ";", "100");
        iPartsDataPartListEntry sixthEntry = createTestPartListEntry("00006", "680;", "100");

        positionVariants.add(firstEntry);
        positionVariants.add(secondEntry);
        positionVariants.add(thirdEntry);
        positionVariants.add(fourthEntry);
        positionVariants.add(fifthEntry);
        positionVariants.add(sixthEntry);

        // Damit sie in der gleichen Zeitscheibe landen
        for (iPartsDataPartListEntry pv : positionVariants) {
            pv.setTimeSliceDates(true, 0, Long.MAX_VALUE);
        }

        filterPartsEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        modelFilter.filterByCodeRule(filterPartsEntries, modelBuildingCodes, -1, null);

        assertEquals(3, filterPartsEntries.getVisibleEntries().size());
        // Beim Filtern enstehen nun vier Gruppen:
        // "490" -> 00002
        // "580" -> 00003
        // "680" -> 00006
        // nur bm-bildende Code haben -> 00001,00004 und 00005
        //
        // Am Ende der Gruppenbildung werden die Teilkonjunktionen mit nur bm-bildenden Code in alle anderen Gruppen gelegt,
        // weil diese auch gruppenübergreifend Positionen verdrängen können. Das führt zu folgender Konstellation:
        // "490" -> 00002,00001,00004 und 00005
        // "580" -> 00003,00001,00004 und 00005
        // "680" -> 00006,00001,00004 und 00005
        // nur bm-bildende Code haben -> 00001,00004 und 00005

        // Position 00002 bleibt erhalten, weil sie in ihrer Gruppe zwei Treffer hat: M270 und M20. Keine andere
        // Teilkonjunktion hat mehr. Außerdem handelt es sich hier um eine Teilkonjunktion, die techn. und bm-bildende Code
        // besitzt. Diese dürfen von Teilkonjunktionen mit nur bm-bildenden Code nur verdrängt werden, wenn die
        // verdrängende Teilkonjunktion ALLE bm-bildende Code (vom Baumuster) besitzt (und keine Wegfallsachnummer ist,
        // da diese ja automatisch alle bm-bildende Code bekommen).
        assertTrue(filterPartsEntries.isEntryVisible(secondEntry));
        // Position 00003 bleibt erhalten, obwohl sie nur einen Treffer in ihrer Gruppe hat. 00004 hat zwar mehr Treffer,
        // darf aber 00003 nicht verdrängen, weil es sich bei 00003 um eine Teilkonjunktion mit techn. und bm-bildenden
        // Code handelt. Diese Konstellation darf nur von Teilkonjunktionen mit allen bm-bildenden Code verdrängt werden.
        assertTrue(filterPartsEntries.isEntryVisible(thirdEntry));
        // Position 00004 bleibt erhalten, weil sie unter den Teilkonjunktionen mit nur bm-bildenden Code zwei und keine
        // andere Teilkonjunktion mehr Treffer hat.
        assertTrue(filterPartsEntries.isEntryVisible(fourthEntry));

        // Zusätzlicher Test für die Positionen 00003 und 00002, wenn die verdrängende Teilkonjunktion (Position 00004)
        // die maximale Anzahl von  bm-bildenden Code besitzt (und keine Wegfallsachnummer ist)
        positionVariants.remove(fourthEntry);
        fourthEntry = createTestPartListEntry("00004", "M270+M005+M20;", "100");
        fourthEntry.setTimeSliceDates(true, 0, Long.MAX_VALUE);
        positionVariants.add(fourthEntry);
        filterPartsEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        modelFilter.filterByCodeRule(filterPartsEntries, modelBuildingCodes, -1, null);

        // INFO: Seit DAIMLER-8305 dürfen normale Teilpositionen mit nur bm-bildenden Code Teilpositionen mit techn.
        // UND bm-bildende Code nicht mehr verdrängen. D.h. die Position 00004, die nur bm-bildende Code hat, darf 00002
        // und 00003 nicht mehr verdrängen. Das neue Ergebnis ist somit 3 und nicht 1!
        assertNotSame(1, filterPartsEntries.getVisibleEntries().size());
        assertEquals(3, filterPartsEntries.getVisibleEntries().size());
        // Position 00004 hat ja nun 3 Treffer und verdängt somit alle anderen Positionen (egal welche Konstellationen
        // die Teilkonjunktionen haben)
        assertTrue(filterPartsEntries.isEntryVisible(fourthEntry));

        //###############DAIMLER-8305####################

        // DAIMLER-8305: Neue Logik für Teilpositionen mit techn. und bm-bildenden Code. Diese dürfen von Teilpositionen
        // mit nur bm-bildenden Code gar nicht mehr verdrängt werden.
        // Beispiele von Herr Müller:
        // Beispiel 1:
        positionVariants.clear();
        firstEntry = createTestPartListEntry("00001", "M270+M20+490;", "100");
        secondEntry = createTestPartListEntry("00002", "M270+490;", "100");
        thirdEntry = createTestPartListEntry("00003", "490;", "100");
        fourthEntry = createTestPartListEntry("00004", "580;", "100");
        fifthEntry = createTestPartListEntry("00005", "M270+640;", "100");
        sixthEntry = createTestPartListEntry("00006", ";", "100");

        positionVariants.add(firstEntry);
        positionVariants.add(secondEntry);
        positionVariants.add(thirdEntry);
        positionVariants.add(fourthEntry);
        positionVariants.add(fifthEntry);
        positionVariants.add(sixthEntry);

        // Damit sie in der gleichen Zeitscheibe landen
        for (iPartsDataPartListEntry pv : positionVariants) {
            pv.setTimeSliceDates(true, 0, Long.MAX_VALUE);
        }

        filterPartsEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        modelFilter.filterByCodeRule(filterPartsEntries, modelBuildingCodes, -1, null);
        assertEquals(4, filterPartsEntries.getVisibleEntries().size());
        // 00004 und 00005 bleiben erhalten, weil sie in ihren jeweiligen Gruppen (580/640) die einzigen Positionen sind
        // und es keine Teilkonjunktion mit nur bm-bildenden Code gibt.
        assertTrue(filterPartsEntries.isEntryVisible(fourthEntry));
        assertTrue(filterPartsEntries.isEntryVisible(fifthEntry));
        // 00001 bleibt erhalten, weil sie in ihrer Gruppe (480) von allen Teilkonjunktionen die meisten bm-bildenden Code
        // hat (Anzahl:2)
        assertTrue(filterPartsEntries.isEntryVisible(firstEntry));
        // 00006 bleibt erhalten, weil leere Teilkonjunktionen nur von Teilkonjunktionen mit rein bm-bildenden Code verdrängt
        // werden dürfen
        assertTrue(filterPartsEntries.isEntryVisible(sixthEntry));

        // Beispiel 2:
        positionVariants.clear();
        firstEntry = createTestPartListEntry("00001", "M270;", "100");
        secondEntry = createTestPartListEntry("00002", "M270+M20+490;", "100");
        thirdEntry = createTestPartListEntry("00003", "M20+580;", "100");
        fourthEntry = createTestPartListEntry("00004", "M270+M20+M005;", "100");
        fifthEntry = createTestPartListEntry("00005", ";", "100");
        sixthEntry = createTestPartListEntry("00005", "680;", "100");

        positionVariants.add(firstEntry);
        positionVariants.add(secondEntry);
        positionVariants.add(thirdEntry);
        positionVariants.add(fourthEntry);
        positionVariants.add(fifthEntry);
        positionVariants.add(sixthEntry);

        // Damit sie in der gleichen Zeitscheibe landen
        for (iPartsDataPartListEntry pv : positionVariants) {
            pv.setTimeSliceDates(true, 0, Long.MAX_VALUE);
        }

        filterPartsEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        modelFilter.filterByCodeRule(filterPartsEntries, modelBuildingCodes, -1, null);
        assertEquals(3, filterPartsEntries.getVisibleEntries().size());
        // 00004 bleibt erhalten, weil die Teilkonjunktion 3 bm-bildende Code besitzt und somit 00001,00005 und 00006
        // verdrängt. 00002 und 00003 dürfen von ihr nicht verdrängt werden, weil es sich hierbei um Teilkonjunktionen
        // handelt, die techn. sowie bm-bildende Code besitzen. Solche Teilkonjunktionen sind ein Sonderfall im Scoring
        // und dürfen nur von Wegfallsachnummern und Teilkonjunktionen mit den gleichen techn.Code und mehr bm-bildenden
        // Code verdrängt werden.
        assertTrue(filterPartsEntries.isEntryVisible(fourthEntry));
        // 00002 und 00003 bleiben erhalten, weil sie die einzigen Teilkonjunktione in ihren Gruppen sind: Bei beiden
        // handelt es sich um den Sonderfall "Teilkonjunktion mit techn. und bm-bildenden Code". Somit darf 00004 beide
        // nicht verdrängen.
        assertTrue(filterPartsEntries.isEntryVisible(secondEntry));
        assertTrue(filterPartsEntries.isEntryVisible(thirdEntry));
    }

    private Set<String> getModelBuildingCodes(boolean withAAModelBuildingCode, String modelNumber) {
        Set<String> result = new HashSet<>();
        if (withAAModelBuildingCode) {
            result.addAll(iPartsModel.getInstance(getProject(), new iPartsModelId(modelNumber)).getCodeSetWithAA());
        } else {
            result.addAll(iPartsModel.getInstance(getProject(), new iPartsModelId(modelNumber)).getCodeSetWithoutAA());
        }
        return result;
    }

    public void testBadCodeDetection() {
        iPartsDataPartListEntry testPartListEntry = new iPartsDataPartListEntry(getProject(), new PartListEntryId("", "", ""));
        testPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO, "C204", DBActionOrigin.FROM_DB);
        iPartsVirtualCalcFieldDocuRel testIPartsVirtualCalcFieldDocuRel = new iPartsVirtualCalcFieldDocuRel(getProject(), testPartListEntry);

        // Bad-Code für die Baureihe C204 sind aktuell:
        // BR   AA  BAD-Code    Verfallsdatum   permanent
        // C204		M651;	    29991231
        // C204		801;	    29990901
        // C204	FS	ME02+460;	29991231
        // C204		800;	    20180901
        // C204	FW	MEXX;	    29991231
        // C204		Q40+498;		            Ja
        // C204	FW	Q30;			            Ja


        // Nur eine Teilkonjunktion, die den permanenten BAD-Code Q30 enthält
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "Q30;", DBActionOrigin.FROM_DB);
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Nur eine Teilkonjunktion, die den permanenten BAD-Code Q30 enthält
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "Q30+777;", DBActionOrigin.FROM_DB);
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Zwei Teilkonjunktionen, eine davon (580) enthält keine BAD-Code
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "Q30+777/580", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Nur eine Teilkonjunktion, die den permanenten BAD-Code Q30 enthält. Dieser gilt aber nicht für die AA der Teilepos
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FS", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "Q30+777;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Zwei Teilkonjunktionen, eine davon (Q40) enthält aber aus keinen BAD-Code Set alle BAD-Code, sondern nur einen
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "Q30+777/Q40;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Zwei Teilkonjunktionen. Die erste enthält Q30. Q40+498+777 enthält alle BAD-Code des BAD-Code Sets "Q40+498"
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "Q30+777/Q40+498+777;", DBActionOrigin.FROM_DB);
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Zwei Teilkonjunktionen. Q30+777 enthält den permanenten BAD-Code Q30. MEXX enthält einen noch nicht abgelaufenen BAD-Code.
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "Q30+777/MEXX;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Nur eine Teilkonjunktion die den BAD-Code MEXX enthält. Dieser ist nicht dauerhaft aber aktuell noch gültig
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "MEXX;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Nur eine Teilkonjunktion die alle BAD-Code eines BAD-Code Sets enthält. Dieses ist nicht dauerhaft, aber aktuell noch gültig
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FS", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "ME02+580+460;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Zwei Teilkonjunktionen, eine davon (Q40) enthält aber aus keinen BAD-Code Set alle BAD-Code, sondern nur einen
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "Q40/ME02;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Teilkonjunktion Q40+777+999 hat keine Treffer bei permanenten oder aktuell gültigen BAD-Coden
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "(Q40+777/MEXX/Q30+580/M651)+999;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // 800 ist nur bis 1.9.2018 gültig
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "800;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // 801 ist aktuell gültig (bis 31.12.2999)
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "801;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // keinen Treffer, da Q30 negativ angeschrieben
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "-Q30;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // keinen Treffer bei zweiter Teilkonjunktion, da 498 negativ angeschrieben
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "FW", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "Q30+777/Q40+-498+777;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

        // Es ist ein aktuell gültiger BAD-Code in der Coderegel enthalten.
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, "", DBActionOrigin.FROM_DB);
        testPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, "802+801-111;", DBActionOrigin.FROM_DB);
        assertFalse(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes());
        assertTrue(testIPartsVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes());

    }
}
