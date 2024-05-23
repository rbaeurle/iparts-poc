/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.datacard;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterPartsEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.tests.AbstractTestSuiteWithIPartsProject;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import junit.framework.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests für Datenkarten Filter
 */
public class TestDatacardFilter extends AbstractTestSuiteWithIPartsProject {

    private static final String VM_PARAMETER_USE_LOCAL_DIR = "dw_TestDatacardFilter_useLocalDir";

    /**
     * Eine manuelle {@link junit.framework.TestSuite} und {@link de.docware.util.test.FrameworkTestSetup} erzeugen, um
     * komplette Tests mit einem globalen SetUp und TearDown zu ermöglichen inkl. Nachtests.
     *
     * @return
     */
    public static Test suite() {
        return createTestSuite(TestDatacardFilter.class);
    }

    public TestDatacardFilter() {
    }

    public TestDatacardFilter(TestDatacardFilter globalTest, String methodName) {
        super(globalTest, methodName);
    }

    public void testScoring() {

        float posWeight = 1;
        float emptyWeight = 0.5f;

        String code;
        float expectedScore;
        Set<String> datacardCodes;

        datacardCodes = new HashSet<String>(StrUtils.toStringList("C1,C2,C3,C4,C5", ","));
        code = "(C1)/(C2+C3)/(C1+C2+C3+C4+C5)/(C2+C3+C4)";
        expectedScore = 5;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        datacardCodes = new HashSet<String>(StrUtils.toStringList("C1,C2,C3,C4", ","));
        code = "(C1)/(C2+C3)/(C1+C2+C3+C4+C5)/(C2+C3+C4)";
        // Weil hier nicht mehr gekürzt wird, ist das Ergebnis 3 und nicht 4
        expectedScore = 3;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        code = "(C1+-C5)/(C2+-C6)";
        expectedScore = 1;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        code = "-C5";
        expectedScore = 0;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        code = ";";
        expectedScore = 0.5f;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        code = " ";
        expectedScore = 0.5f;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        code = "";
        expectedScore = 0.5f;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        code = "C7";
        expectedScore = -1;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        datacardCodes = new HashSet<String>(StrUtils.toStringList("FW,M005,M014,M020,M22,M651", ","));
        code = "(M651+M005)+-877K;";
        expectedScore = 2;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        code = "(M651/M651+965)+M005+-421;";
        expectedScore = 2;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        code = "(M651+M005/M651+M005+965)+-(877K/421);";
        expectedScore = 2;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));


        datacardCodes = new HashSet<String>(StrUtils.toStringList("513,628,805", ","));
        code = "((608/628)+(476/513)/476/513)+805+-055;";
        expectedScore = 3;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

        datacardCodes = new HashSet<String>(StrUtils.toStringList("513,628,805", ","));
        code = "(608/628)+805+-055;";
        expectedScore = 2;
        assertEquals(expectedScore, DatacardFilter.scoreConjunctions(code, datacardCodes, posWeight, emptyWeight));

    }


    public void testFilter() {
        Set<String> datacardCodes = new HashSet<String>(StrUtils.toStringList("C1,C2,C3", ","));
        List<iPartsDataPartListEntry> positionVariants = new DwList<iPartsDataPartListEntry>();
        List<iPartsDataPartListEntry> positionVariantsUnfiltered = new DwList<iPartsDataPartListEntry>();

        // zwei Positionsvarianten mit gleichem score
        positionVariants.add(createTestPartListEntry("1", "C1+C2", "10"));
        positionVariants.add(createTestPartListEntry("2", "C2+C3", "10"));
        positionVariantsUnfiltered.addAll(positionVariants);

        iPartsFilterPartsEntries filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, null, null);
        positionVariants = filterEntries.getVisibleEntries();

        assertEquals(2, positionVariants.size());
        assertEquals(positionVariantsUnfiltered, positionVariants);

        // zwei Positionsvarianten mit unterschiedlichem score
        positionVariants.clear();
        positionVariantsUnfiltered.clear();
        positionVariants.add(createTestPartListEntry("1", "C1+C2", "10"));
        positionVariants.add(createTestPartListEntry("2", "C2", "10"));
        positionVariantsUnfiltered.addAll(positionVariants);
        positionVariantsUnfiltered.remove(1);

        filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, null, null);
        positionVariants = filterEntries.getVisibleEntries();

        assertEquals(1, positionVariants.size());
        assertEquals(positionVariantsUnfiltered, positionVariants);

        // zwei Positionsvarianten mit unterschiedlichem score + 1 die false ergibt
        positionVariants.clear();
        positionVariantsUnfiltered.clear();
        positionVariants.add(createTestPartListEntry("1", "C1+C2", "10"));
        positionVariants.add(createTestPartListEntry("2", "C2", "10"));
        positionVariants.add(createTestPartListEntry("3", "C4", "10"));
        positionVariantsUnfiltered.addAll(positionVariants);
        positionVariantsUnfiltered.remove(2);
        positionVariantsUnfiltered.remove(1);

        filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, null, null);
        positionVariants = filterEntries.getVisibleEntries();

        assertEquals(1, positionVariants.size());
        assertEquals(positionVariantsUnfiltered, positionVariants);

        // unterschiedliche Hotpots innerhalb der Positionsvariante mit gleichem score
        positionVariants.clear();
        positionVariantsUnfiltered.clear();
        positionVariants.add(createTestPartListEntry("1", "C1+C2", "10"));
        positionVariants.add(createTestPartListEntry("2", "C2+C3", "20"));
        positionVariantsUnfiltered.addAll(positionVariants);

        filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, null, null);
        positionVariants = filterEntries.getVisibleEntries();

        assertEquals(2, positionVariants.size());
        assertEquals(positionVariantsUnfiltered, positionVariants);

        // Hotspots die nicht besetzt sind "----"
        positionVariants.clear();
        positionVariantsUnfiltered.clear();
        positionVariants.add(createTestPartListEntry("1", "C1+C2", "----"));
        positionVariants.add(createTestPartListEntry("2", "C2+C3", "----"));
        positionVariantsUnfiltered.addAll(positionVariants);

        filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, null, null);
        positionVariants = filterEntries.getVisibleEntries();

        assertEquals(2, positionVariants.size());
        assertEquals(positionVariantsUnfiltered, positionVariants);

        // mit negativen Codes
        positionVariants.clear();
        positionVariantsUnfiltered.clear();
        positionVariants.add(createTestPartListEntry("1", "C1+-C6", "20"));
        positionVariants.add(createTestPartListEntry("2", "C2+C3+-C6", "20"));
        positionVariantsUnfiltered.addAll(positionVariants);
        positionVariantsUnfiltered.remove(0);

        filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, null, null);
        positionVariants = filterEntries.getVisibleEntries();

        assertEquals(1, positionVariants.size());
        assertEquals(positionVariantsUnfiltered, positionVariants);

        // Code mit bm-bildenden Code
        Set<String> bmBuildingCodes = new HashSet<String>(StrUtils.toStringList("M005,M020,M50", ","));
        datacardCodes.add("M005");
        positionVariants.clear();
        positionVariants.add(createTestPartListEntry("1", "C1+M005+C2", "10"));
        positionVariants.add(createTestPartListEntry("2", "C2+C3", "10"));
        positionVariantsUnfiltered.addAll(positionVariants);

        // Test ohne "omitted Codes"
        filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, null, null);
        positionVariants = filterEntries.getVisibleEntries();
        assertEquals(1, positionVariants.size());
        assertFalse(positionVariantsUnfiltered.size() == positionVariants.size());

        positionVariants.clear();
        positionVariantsUnfiltered.clear();
        positionVariants.add(createTestPartListEntry("1", "C1+M005+C2", "10"));
        positionVariants.add(createTestPartListEntry("2", "C2+C3", "10"));
        positionVariantsUnfiltered.addAll(positionVariants);

        filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, bmBuildingCodes, null);
        positionVariants = filterEntries.getVisibleEntries();
        assertEquals(2, positionVariants.size());
        assertEquals(positionVariantsUnfiltered, positionVariants);

        // mit halbwegs echtem code und zwei Teilkonjunktionen
        datacardCodes.clear();
        datacardCodes.add("489");
        datacardCodes.add("M005");
        positionVariants.clear();
        positionVariantsUnfiltered.clear();
        positionVariants.add(createTestPartListEntry("1", "488/(489+M005)", "10"));
        positionVariants.add(createTestPartListEntry("2", "489", "10"));
        positionVariantsUnfiltered.addAll(positionVariants);
        positionVariantsUnfiltered.remove(1);

        filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, null, null);
        positionVariants = filterEntries.getVisibleEntries();

        assertEquals(1, positionVariants.size());
        assertEquals(positionVariantsUnfiltered, positionVariants);

        // leerer Code
        datacardCodes.clear();
        datacardCodes.add("C1");
        positionVariants.clear();
        positionVariantsUnfiltered.clear();
        positionVariants.add(createTestPartListEntry("1", ";", "10"));
        positionVariants.add(createTestPartListEntry("2", "C1", "10"));
        positionVariants.add(createTestPartListEntry("3", "C2", "10"));
        positionVariantsUnfiltered.addAll(positionVariants);
        positionVariantsUnfiltered.remove(2);
        positionVariantsUnfiltered.remove(0);

        filterEntries = iPartsFilterPartsEntries.getInstanceForDataCardTest(positionVariants);
        DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, null, null);
        positionVariants = filterEntries.getVisibleEntries();

        assertEquals(1, positionVariants.size());
        assertEquals(positionVariantsUnfiltered, positionVariants);
    }

    // createTempDir() überschreiben, wenn man die Daten direkt von einem lokalen Verzeichnis ohne Kopieren in ein temporäres
    // Testverzeichnis verwenden will
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

}