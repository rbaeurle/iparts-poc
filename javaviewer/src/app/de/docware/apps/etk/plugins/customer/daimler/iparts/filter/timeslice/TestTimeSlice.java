/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.timeslice;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterPartsEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.tests.AbstractTestSuiteWithIPartsProject;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import junit.framework.Test;

import java.util.List;

/**
 * Test für iPartsFilterTimeSliceHelper.isInTimeSlice auch mit unendlich Werten
 */
public class TestTimeSlice extends AbstractTestSuiteWithIPartsProject {

    private static final String VM_PARAMETER_USE_LOCAL_DIR = "dw_TestTimeSlice_useLocalDir";

    /**
     * Eine manuelle {@link junit.framework.TestSuite} und {@link de.docware.util.test.FrameworkTestSetup} erzeugen, um
     * komplette Tests mit einem globalen SetUp und TearDown zu ermöglichen inkl. Nachtests.
     *
     * @return
     */
    public static Test suite() {
        return createTestSuite(TestTimeSlice.class);
    }

    public TestTimeSlice() {
    }

    public TestTimeSlice(TestTimeSlice globalTest, String methodName) {
        super(globalTest, methodName);
    }

    public void testTimeSlices() {
        String[] masterTime = new String[]{ "20160101", "20161231" };

        assertEquals(false, testOneTimeSlice(new String[]{ "20150101", "20151231" }, masterTime)); //liegt außerhalb
        assertEquals(true, testOneTimeSlice(new String[]{ "20150101", "20160201" }, masterTime)); //liegt teilweise drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160101", "20160201" }, masterTime)); //liegt mit Vorder-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160201", "20160301" }, masterTime)); //liegt komplett drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160501", "20161231" }, masterTime)); //liegt mit Hinter-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20161001", "20170201" }, masterTime)); //liegt teilweise drin
        assertEquals(false, testOneTimeSlice(new String[]{ "20171001", "20171201" }, masterTime)); //liegt außerhalb
        assertEquals(false, testOneTimeSlice(new String[]{ "20161231", "20170201" }, masterTime)); //liegt mit Vorder-Kante gerade nicht mehr drin
        assertEquals(false, testOneTimeSlice(new String[]{ "20150101", "20160101" }, masterTime)); //liegt mit Hinter-Kante gerade nicht mehr drin
        assertEquals(true, testOneTimeSlice(masterTime, masterTime)); //gleicher Wert

        // Baumuster-Zeitscheibe
        assertEquals(false, testOneModelTimeSlice(new String[]{ "20150101", "20151231" }, masterTime)); //liegt außerhalb
        assertEquals(false, testOneModelTimeSlice(new String[]{ "20171001", "20171201" }, masterTime)); //liegt außerhalb
        assertEquals(true, testOneModelTimeSlice(new String[]{ "20161231", "20170201" }, masterTime)); //liegt mit Vorder-Kante gerade noch drin
        assertEquals(true, testOneModelTimeSlice(new String[]{ "20150101", "20160101" }, masterTime)); //liegt mit Hinter-Kante gerade noch drin
        assertEquals(true, testOneModelTimeSlice(masterTime, masterTime)); //gleicher Wert

        assertEquals(false, testOneTimeSlice(new String[]{ "20150101", "20151231" }, masterTime)); //liegt außerhalb
        assertEquals(true, testOneTimeSlice(new String[]{ "20150101", "" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20150101", "20160201" }, masterTime)); //liegt teilweise drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160101", "20160201" }, masterTime)); //liegt mit Vorder-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160101", "" }, masterTime)); //liegt mit Vorder-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160201", "20160301" }, masterTime)); //liegt komplett drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160501", "20161231" }, masterTime)); //liegt mit Hinter-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160501", "" }, masterTime)); //liegt mit Hinter-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20161001", "20170201" }, masterTime)); //liegt teilweise drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20161001", "" }, masterTime)); //liegt teilweise drin
        assertEquals(false, testOneTimeSlice(new String[]{ "20171001", "20171201" }, masterTime)); //liegt außerhalb
        assertEquals(false, testOneTimeSlice(new String[]{ "20171001", "" }, masterTime)); //liegt außerhalb

        // nun mit Unendlich in masterTime (0)
        masterTime = new String[]{ "20160101", "" };
        assertEquals(false, testOneTimeSlice(new String[]{ "20150101", "20151231" }, masterTime)); //liegt außerhalb
        assertEquals(true, testOneTimeSlice(new String[]{ "20150101", "" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20150101", "20160201" }, masterTime)); //liegt teilweise drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160101", "20160201" }, masterTime)); //liegt mit Vorder-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160101", "" }, masterTime)); //liegt mit Vorder-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160201", "20160301" }, masterTime)); //liegt komplett drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160501", "20161231" }, masterTime)); //liegt mit Hinter-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160501", "" }, masterTime)); //liegt mit Hinter-Kante drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20161001", "20170201" }, masterTime)); //liegt teilweise drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20161001", "" }, masterTime)); //liegt teilweise drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20171001", "20171201" }, masterTime)); //liegt nicht mehr außerhalb
        assertEquals(true, testOneTimeSlice(new String[]{ "20171001", "" }, masterTime)); //liegt nicht mehr außerhalb
        assertEquals(true, testOneTimeSlice(masterTime, masterTime)); //gleicher Wert

        // Baumuster-Zeitscheibe
        assertEquals(false, testOneModelTimeSlice(new String[]{ "20150101", "20151231" }, masterTime)); //liegt außerhalb
        assertEquals(true, testOneModelTimeSlice(new String[]{ "20150101", "" }, masterTime)); //liegt teilweise drin
        assertEquals(true, testOneModelTimeSlice(masterTime, masterTime)); //gleicher Wert

        // nun mit -/+Unendlich in masterTime (0, 0)
        masterTime = new String[]{ "", "" };
        assertEquals(true, testOneTimeSlice(new String[]{ "20150101", "20151231" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20150101", "" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "", "20150101" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20150101", "20160201" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160101", "20160201" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160101", "" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160201", "20160301" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160501", "20161231" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20160501", "" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20161001", "20170201" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20161001", "" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20171001", "20171201" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(new String[]{ "20171001", "" }, masterTime)); //liegt drin
        assertEquals(true, testOneTimeSlice(masterTime, masterTime)); //gleicher Wert

        // Baumuster-Zeitscheibe
        assertEquals(true, testOneModelTimeSlice(new String[]{ "", "20150101" }, masterTime)); //liegt drin
        assertEquals(true, testOneModelTimeSlice(masterTime, masterTime)); //gleicher Wert
    }

    private boolean testOneTimeSlice(String[] testTime, String masterTime[]) {
        return iPartsFilterTimeSliceHelper.isInTimeSlice(StrUtils.strToLongDef(testTime[0], 0),
                                                         StrUtils.strToLongDef(testTime[1], 0),
                                                         StrUtils.strToLongDef(masterTime[0], 0),
                                                         StrUtils.strToLongDef(masterTime[1], 0));
    }

    private boolean testOneModelTimeSlice(String[] testTime, String masterTime[]) {
        return iPartsFilterTimeSliceHelper.isInModelTimeSlice(StrUtils.strToLongDef(testTime[0], 0),
                                                              StrUtils.strToLongDef(testTime[1], 0),
                                                              StrUtils.strToLongDef(masterTime[0], 0),
                                                              StrUtils.strToLongDef(masterTime[1], 0));
    }

    // nur nötig, damit ohne Projectcc nicht laufend eine Exception geworfen wird
    private class MyEtkDataPartListEntry extends iPartsDataPartListEntry {

        public MyEtkDataPartListEntry(EtkProject project, PartListEntryId id) {
            super(project, id);
            attributes = new DBDataObjectAttributes();
            attributes.addField(iPartsConst.FIELD_K_VARI, id.getKVari(), DBActionOrigin.FROM_DB);
            attributes.addField(iPartsConst.FIELD_K_VER, id.getKVer(), DBActionOrigin.FROM_DB);
            attributes.addField(iPartsConst.FIELD_K_LFDNR, id.getKLfdnr(), DBActionOrigin.FROM_DB);
            attributes.addField(iPartsConst.FIELD_K_EVAL_PEM_FROM, "1", DBActionOrigin.FROM_DB);
            attributes.addField(iPartsConst.FIELD_K_EVAL_PEM_TO, "1", DBActionOrigin.FROM_DB);
            attributes.addField(iPartsConst.FIELD_K_MATNR, id.getKVari(), DBActionOrigin.FROM_DB);
            attributes.addField(iPartsConst.FIELD_K_MVER, "", DBActionOrigin.FROM_DB);
            attributes.addField(iPartsConst.FIELD_K_SOURCE_GUID, "", DBActionOrigin.FROM_DB);
            isLoaded = true;
        }
    }

    private static long TIME_ONE = 20140328132741L;
    private static long TIME_TWO = 20150416101626L;
    private static long TIME_THREE = 20150616095231L;
    private static long TIME_FOUR = 0L;
    private static long TIME_FIVE = 20140516151501L;
    private static long TIME_SIX = 20150711000000L;
    private static long TIME_SEVEN = 20150711000000L;
    private static long TIME_EIGHT = 0L;

    private List<iPartsDataPartListEntry> preparePartListEntries() {
        // 1-----------------------2                                                                               00001
        //                            3--------------------------------------------------------------------------4 00002
        //      5-------------------------6                                                                        00003
        //                                7----------------------------------------------------------------------8 00004
        // 1 "20140328132741"
        // 2 "20150416101626"
        // 3 "20150616095231"
        // 4 0 (unendlich = Long.MAX_VALUE)
        // 5 "20140516151501"
        // 6 "20150710142936"
        // 7 "20150710142936"
        // 8 0 (unendlich = Long.MAX_VALUE)
        // Ergebnis
        // 1----5                                                                                                    00001
        //      5------------------2                                                                                 00001/00003
        //                         5--3                                                                              00003
        //                            3---6                                                                          00002/00003
        //                                7----------------------------------------------------------------------4/8 00002/00004
        List<iPartsDataPartListEntry> pvList = new DwList<>();
        MyEtkDataPartListEntry partListEntry_One = new MyEtkDataPartListEntry(getProject(), new PartListEntryId("C01_21_300_00001", "", "00001"));
        iPartsFactoryData factoryDataForRetail = new iPartsFactoryData();
        List<iPartsFactoryData.DataForFactory> dataForFactoryList = new DwList<iPartsFactoryData.DataForFactory>();
        iPartsFactoryData.DataForFactory dataForFactory = new iPartsFactoryData.DataForFactory();
        dataForFactory.pemFrom = "A123";
        dataForFactory.dateFrom = TIME_ONE;
        dataForFactoryList.add(dataForFactory);
        dataForFactory = new iPartsFactoryData.DataForFactory();
        dataForFactory.pemTo = "A123";
        dataForFactory.dateTo = TIME_TWO;
        dataForFactoryList.add(dataForFactory);
        factoryDataForRetail.setDataForFactory("000", dataForFactoryList);
        partListEntry_One.setFactoryDataForRetailUnfiltered(factoryDataForRetail);
        pvList.add(partListEntry_One);

        MyEtkDataPartListEntry partListEntry_Two = new MyEtkDataPartListEntry(getProject(), new PartListEntryId("C01_21_300_00001", "", "00002"));
        factoryDataForRetail = new iPartsFactoryData();
        dataForFactoryList = new DwList<iPartsFactoryData.DataForFactory>();
        dataForFactory = new iPartsFactoryData.DataForFactory();
        dataForFactory.pemFrom = "A123";
        dataForFactory.dateFrom = TIME_THREE;
        dataForFactoryList.add(dataForFactory);
        dataForFactory = new iPartsFactoryData.DataForFactory();
        dataForFactory.pemTo = "";
        dataForFactory.dateTo = TIME_FOUR;
        dataForFactoryList.add(dataForFactory);
        factoryDataForRetail.setDataForFactory("000", dataForFactoryList);
        partListEntry_Two.setFactoryDataForRetailUnfiltered(factoryDataForRetail);
        pvList.add(partListEntry_Two);

        MyEtkDataPartListEntry partListEntry_Three = new MyEtkDataPartListEntry(getProject(), new PartListEntryId("C01_21_300_00001", "", "00003"));
        factoryDataForRetail = new iPartsFactoryData();
        dataForFactoryList = new DwList<iPartsFactoryData.DataForFactory>();
        dataForFactory = new iPartsFactoryData.DataForFactory();
        dataForFactory.pemFrom = "A123";
        dataForFactory.dateFrom = TIME_FIVE;
        dataForFactoryList.add(dataForFactory);
        dataForFactory = new iPartsFactoryData.DataForFactory();
        dataForFactory.pemTo = "A123";
        dataForFactory.dateTo = TIME_SIX;
        dataForFactoryList.add(dataForFactory);
        factoryDataForRetail.setDataForFactory("000", dataForFactoryList);
        partListEntry_Three.setFactoryDataForRetailUnfiltered(factoryDataForRetail);
        pvList.add(partListEntry_Three);

        // Spezialfall PEM Termin AB = PEM Termin BIS
        MyEtkDataPartListEntry partListEntry_Four = new MyEtkDataPartListEntry(getProject(), new PartListEntryId("C01_21_300_00001", "", "00004"));
        factoryDataForRetail = new iPartsFactoryData();
        dataForFactoryList = new DwList<iPartsFactoryData.DataForFactory>();
        dataForFactory = new iPartsFactoryData.DataForFactory();
        dataForFactory.pemFrom = "A123";
        dataForFactory.pemTo = "A123";
        dataForFactory.dateFrom = TIME_SIX;
        dataForFactory.dateTo = TIME_SIX;
        dataForFactoryList.add(dataForFactory);
        factoryDataForRetail.setDataForFactory("000", dataForFactoryList);
        partListEntry_Four.setFactoryDataForRetailUnfiltered(factoryDataForRetail);
        pvList.add(partListEntry_Four);

        MyEtkDataPartListEntry partListEntry_Five = new MyEtkDataPartListEntry(getProject(), new PartListEntryId("C01_21_300_00001", "", "00005"));
        factoryDataForRetail = new iPartsFactoryData();
        dataForFactoryList = new DwList<iPartsFactoryData.DataForFactory>();
        dataForFactory = new iPartsFactoryData.DataForFactory();
        dataForFactory.pemFrom = "A123";
        dataForFactory.dateFrom = TIME_SEVEN;
        dataForFactoryList.add(dataForFactory);
        dataForFactory = new iPartsFactoryData.DataForFactory();
        dataForFactory.pemTo = "";
        dataForFactory.dateTo = TIME_EIGHT;
        dataForFactoryList.add(dataForFactory);
        factoryDataForRetail.setDataForFactory("000", dataForFactoryList);
        partListEntry_Five.setFactoryDataForRetailUnfiltered(factoryDataForRetail);
        pvList.add(partListEntry_Five);

        return pvList;
    }

    private List<iPartsPositionVariantsTimeLine> prepareTestPVList(List<iPartsDataPartListEntry> pvList) {
        List<iPartsPositionVariantsTimeLine> testPVTimeLineList = new DwList<iPartsPositionVariantsTimeLine>();
        List<EtkDataPartListEntry> testPVList = new DwList<EtkDataPartListEntry>();
        testPVList.add(pvList.get(0));
//        iPartsPositionVariantsTimeLine testPVTimeLine = new iPartsPositionVariantsTimeLine(20140328132741L, 20140516151501L, testPVList);
        iPartsPositionVariantsTimeLine testPVTimeLine = new iPartsPositionVariantsTimeLine(TIME_ONE, TIME_FIVE, testPVList);
        testPVTimeLineList.add(testPVTimeLine);

        testPVList = new DwList<EtkDataPartListEntry>();
        testPVList.add(pvList.get(0));
        testPVList.add(pvList.get(2));
        //testPVTimeLine = new iPartsPositionVariantsTimeLine(20140516151501L, 20150416101626L, testPVList);
        testPVTimeLine = new iPartsPositionVariantsTimeLine(TIME_FIVE, TIME_TWO, testPVList);
        testPVTimeLineList.add(testPVTimeLine);

        testPVList = new DwList<EtkDataPartListEntry>();
        testPVList.add(pvList.get(2));
        //testPVTimeLine = new iPartsPositionVariantsTimeLine(20150416101626L, 20150616095231L, testPVList);
        testPVTimeLine = new iPartsPositionVariantsTimeLine(TIME_TWO, TIME_THREE, testPVList);
        testPVTimeLineList.add(testPVTimeLine);

        testPVList = new DwList<EtkDataPartListEntry>();
        testPVList.add(pvList.get(2));
        testPVList.add(pvList.get(1));
        //testPVTimeLine = new iPartsPositionVariantsTimeLine(20150616095231L, 20150711000000L, testPVList);
        testPVTimeLine = new iPartsPositionVariantsTimeLine(TIME_THREE, TIME_SIX, testPVList);
        testPVTimeLineList.add(testPVTimeLine);

        testPVList = new DwList<EtkDataPartListEntry>();
        testPVList.add(pvList.get(1));
        testPVList.add(pvList.get(3));
        testPVList.add(pvList.get(4));
        //testPVTimeLine = new iPartsPositionVariantsTimeLine(20150711000000L, 20150711235959L, testPVList);
        testPVTimeLine = new iPartsPositionVariantsTimeLine(TIME_SIX, TIME_SIX + 235959, testPVList);
        testPVTimeLineList.add(testPVTimeLine);

        testPVList = new DwList<EtkDataPartListEntry>();
        testPVList.add(pvList.get(1));
        testPVList.add(pvList.get(4));
        testPVTimeLine = new iPartsPositionVariantsTimeLine(TIME_SIX + 235959, Long.MAX_VALUE, testPVList);
        testPVTimeLineList.add(testPVTimeLine);

        return testPVTimeLineList;
    }

    public void testCalcTimeSliceMapFromPVList() {
        final List<iPartsDataPartListEntry> pvList = preparePartListEntries();
        List<iPartsPositionVariantsTimeLine> testPVTimeLineList = prepareTestPVList(pvList);

        // Wenn alle Stücklistenpositionen ausgefiltert wurden, dann werden keine Zeitscheiben erzeugt
        iPartsFilterPartsEntries filterEntries = new iPartsFilterPartsEntries() {
            @Override
            public List<iPartsDataPartListEntry> getAllPositionsVariantsAsSingleList() {
                return pvList;
            }

            @Override
            public boolean isEntryVisible(EtkDataPartListEntry partListEntry) {
                return false;
            }
        };
        List<iPartsPositionVariantsTimeLine> pvTimeLineList = iPartsFilterTimeSliceHelper.calcTimeSliceMapFromPVList(filterEntries.getAllPositionsVariantsAsSingleList(),
                                                                                                                     filterEntries, false);
        assertFalse(pvTimeLineList == null);
        assertTrue(pvTimeLineList.isEmpty());


        filterEntries = new iPartsFilterPartsEntries() {
            @Override
            public List<iPartsDataPartListEntry> getAllPositionsVariantsAsSingleList() {
                return pvList;
            }

            @Override
            public boolean isEntryVisible(EtkDataPartListEntry partListEntry) {
                return true;
            }
        };

        pvTimeLineList = iPartsFilterTimeSliceHelper.calcTimeSliceMapFromPVList(filterEntries.getAllPositionsVariantsAsSingleList(),
                                                                                filterEntries, false);
        assertEquals(false, pvTimeLineList == null);
        assertEquals(false, pvTimeLineList.isEmpty());
        assertEquals(6, pvTimeLineList.size());
        int lfdNr = 0;
        for (iPartsPositionVariantsTimeLine pvTimeLine : pvTimeLineList) {
            iPartsPositionVariantsTimeLine testPVTimeLine = testPVTimeLineList.get(lfdNr);
            assertEquals(testPVTimeLine.getFromDate(), pvTimeLine.getFromDate());
            assertEquals(testPVTimeLine.getToDate(), pvTimeLine.getToDate());
            assertEquals(testPVTimeLine.getPositionVariants().size(), pvTimeLine.getPositionVariants().size());
            for (EtkDataPartListEntry testPartListEntry : testPVTimeLine.getPositionVariants()) {
                assertEquals(true, pvTimeLine.getPositionVariants().contains(testPartListEntry));
            }
            lfdNr++;
        }
    }

    public void testTimesliceMergeForWW() {
        EtkDataPartListEntry plA = new EtkDataPartListEntry(getProject(), new PartListEntryId("A", "", "A"));
        EtkDataPartListEntry plB = new EtkDataPartListEntry(getProject(), new PartListEntryId("B", "", "B"));
        EtkDataPartListEntry plC = new EtkDataPartListEntry(getProject(), new PartListEntryId("C", "", "C"));
        EtkDataPartListEntry plD = new EtkDataPartListEntry(getProject(), new PartListEntryId("D", "", "D"));

        iPartsFilterTimeSliceHelper.TimeSliceContent A = new iPartsFilterTimeSliceHelper.TimeSliceContent(1, 8, plA);
        iPartsFilterTimeSliceHelper.TimeSliceContent B = new iPartsFilterTimeSliceHelper.TimeSliceContent(2, 8, plB);
        iPartsFilterTimeSliceHelper.TimeSliceContent C = new iPartsFilterTimeSliceHelper.TimeSliceContent(7, 9, plC);
        iPartsFilterTimeSliceHelper.TimeSliceContent D = new iPartsFilterTimeSliceHelper.TimeSliceContent(20, 21, plD);

        List<iPartsFilterTimeSliceHelper.TimeSliceContent> timeSlices = new DwList<iPartsFilterTimeSliceHelper.TimeSliceContent>();
        timeSlices.add(C);
        timeSlices.add(B);
        timeSlices.add(A);

        List<List<EtkDataPartListEntry>> result = iPartsFilterTimeSliceHelper.mergeAnyOverlappingTimeSlices(timeSlices);
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).size());
        assertTrue(result.get(0).contains(plA));
        assertTrue(result.get(0).contains(plB));
        assertTrue(result.get(0).contains(plC));

        timeSlices.add(D);
        List<List<EtkDataPartListEntry>> result2 = iPartsFilterTimeSliceHelper.mergeAnyOverlappingTimeSlices(timeSlices);
        assertEquals(2, result2.size());
        assertEquals(3, result2.get(0).size());
        assertTrue(result2.get(0).contains(plA));
        assertTrue(result2.get(0).contains(plB));
        assertTrue(result2.get(0).contains(plC));
        assertEquals(1, result2.get(1).size());
        assertTrue(result2.get(1).contains(plD));

        iPartsFilterTimeSliceHelper.TimeSliceContent AB = new iPartsFilterTimeSliceHelper.TimeSliceContent(1, 8, plA);
        AB.content.add(plB);

        List<iPartsFilterTimeSliceHelper.TimeSliceContent> mergedTimeSlices = new DwList<iPartsFilterTimeSliceHelper.TimeSliceContent>();
        mergedTimeSlices.add(B);
        mergedTimeSlices.add(AB);
        List<iPartsFilterTimeSliceHelper.TimeSliceContent> result3 = iPartsFilterTimeSliceHelper.removeAlreadyIncludedTimeSlices(mergedTimeSlices);
        assertEquals(1, result3.size());
        assertEquals(2, result3.get(0).content.size());
        assertTrue(result3.get(0).content.contains(plA));
        assertTrue(result3.get(0).content.contains(plB));
    }

    // Nimm die Webservice Config und Datenbank um ein EtkProject zu erhalten

    @Override
    protected String getTestCaseRelativeDir() {
        return ("de_docware_apps_etk_plugins_customer_daimler_iparts_webservice_iPartsWebservicePlugin" + "_V" + getTestVersion());
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
    protected boolean isReadOnlyCloneDB() {
        return true;
    }

}
