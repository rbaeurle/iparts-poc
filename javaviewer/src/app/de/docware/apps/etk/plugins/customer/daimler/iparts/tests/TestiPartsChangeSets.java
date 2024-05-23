/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.db.cache.EtkDbsCacheElems;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.mechanic.*;
import de.docware.apps.etk.base.project.mechanic.drawing.DataImageId;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImageList;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDbObjectsLayer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsRevisionsLogger;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.db.etkrecord.EtkRecords;
import de.docware.framework.modules.db.serialization.*;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;
import junit.framework.Assert;
import junit.framework.Test;

import java.util.*;

/**
 * Unit-Tests für die ChangeSets, Beispiel war die Klasse: [TestiPartsWebservices]
 */
public class TestiPartsChangeSets extends AbstractTestSuiteWithIPartsProject implements iPartsConst {

    private String CHANGESETID = "UNITTESTCHANGESETS";

    private static final String moduleNoWithOneImage = "C01_73_060_00001";
    private static final String moduleNoWithTwoImages = "C01_35_075_00001";
    private static final String moduleNoWithNoImage = "62E_00_001_00001";


    // -----------------------------------------------------------------------------------------------------------------
    // Der Pfad für die Datenbank und eventuelle result::: - Dateien:
    //   I:\PP\IPP\testcases\de_docware_apps_etk_plugins_customer_daimler_iparts_iPartsChangeSets_V1
    // --
    // Die Datenbank kam ursprünglich von:
    //   I:\PP\IPP\testcases\de_docware_apps_etk_plugins_customer_daimler_iparts_iPartsChangeSets_V1
    // -----------------------------------------------------------------------------------------------------------------

    private static final String VM_PARAMETER_USE_LOCAL_DIR = "dw_TestiPartsChangeSets_useLocalDir";

    private static final SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);

    /**
     * Eine manuelle {@link junit.framework.TestSuite} und {@link de.docware.util.test.FrameworkTestSetup} erzeugen, um
     * komplette Tests mit einem globalen SetUp und TearDown zu ermöglichen inkl. Nachtests.
     *
     * @return
     */
    public static Test suite() {
        return createTestSuite(TestiPartsChangeSets.class);
    }

    public TestiPartsChangeSets() {
    }

    public TestiPartsChangeSets(TestiPartsChangeSets globalTest, String methodName) {
        super(globalTest, methodName);
    }

    @Override
    public void globalSetUp() throws Exception {
        // Damit die Transaktionen unter H2 aktiviert sind den Autoserver verwenden und die Transkationen einschalten
        // Diese Sachen müssen vor dem super.setUp() gesetzt werden, weil im setup die Datenbank geöffnet wird
        DBH2Database.setForceAutoServer(true);
        DBH2Database.setTransactionsEnabled(true);
        EtkRevisionsHelper.getRecordsForRevisionsActive = true;
        iPartsPlugin.forceRevisionsHelperForTesting = true;

        super.globalSetUp();

        Logger.getLogger().addChannel(EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Sicherstellen, dass die getRecords()-Simulation für einfache Joins aktiv ist pro Test
        EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
    }

    /**
     * Gibt den erwarteten Namen der Konfigurationsdatei zurück.
     *
     * @return
     */
    @Override
    protected String getConfigFileName() {
        return "etk_viewer_edit_ChangeSets.config";
    }

    // createTempDir() überschreiben, wenn man die Daten direkt von einem lokalen Verzeichnis
    // ohne vorheriges Kopieren in ein temporäres Testverzeichnis verwenden will
    @Override
    protected DWFile createTempDir() {
        if (StartParameter.getSystemPropertyBoolean(VM_PARAMETER_USE_LOCAL_DIR, false)) {
            setDeleteTmpBaseDir(false);
            DWFile dwFile = DWFile.get("D:/DataNoS/JAVA_UnitTests").getChild(getTestCaseRelativeDir());
            return dwFile;
        } else {
            useTestCaseFilesFromVCS();
            return super.createTempDir();
        }
    }

    @Override
    protected String getTestCaseRelativeDir() {
        return ("de_docware_apps_etk_plugins_customer_daimler_iparts_webservice_iPartsWebservicePlugin" + "_V" + getTestVersion());
//        return ("de_docware_apps_etk_plugins_customer_daimler_iparts_iPartsChangeSets" + "_V" + getTestVersion());
    }

    private iPartsChangeSetId getChangeSetId() {
        return new iPartsChangeSetId(CHANGESETID /*"UNITTESTCHANGESETS"*/);
    }

    public void startPseudoTransactionForActiveChangeSet() {
        startPseudoTransactionForActiveChangeSet(true);
    }

    public void startPseudoTransactionForActiveChangeSet(boolean startDelayed) {
        EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
        if (revisionsHelper != null) {
            revisionsHelper.startPseudoTransactionForActiveChangeSet(getProject(), startDelayed);
        }
    }

    public void stopPseudoTransactionForActiveChangeSet() {
        EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
        if (revisionsHelper != null) {
            revisionsHelper.stopPseudoTransactionForActiveChangeSet(getProject());
        }
    }

    /**
     * Zum Vergleichen der FieldValues zweier EtkRecords
     *
     * @param recs1
     * @param recs2
     * @return
     */
    private boolean compareRecs(EtkRecords recs1, EtkRecords recs2) {
        if (recs1.size() != recs2.size()) {
            return false;
        }

        for (int i = 0; i < recs1.size(); i++) {
            EtkRecord rec = recs1.get(i);
            List<String> fieldNames = new ArrayList<String>();
            fieldNames.addAll(rec.getFieldNames());

            List<String> values = rec.getFieldValues(fieldNames);

            int index = fieldNames.indexOf(EtkDbConst.FIELD_STAMP);
            if (index >= 0) {
                fieldNames.remove(index);
                values.remove(index);
            }

            index = recs2.indexOf(fieldNames, values, 0);

            if (index < 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Legt eine Instanz des Helpers an und aktiviert das ChangeSet
     *
     * @return EtkRevisionsHelper
     */
    private EtkRevisionsHelper initializeRevisionHelper() {

        // Tabelle DA_CHANGE_SET
//            SQLQuery query1 = getProject().getEtkDbs().getNewQuery().delete().from(new Tables(iPartsConst.TABLE_DA_CHANGE_SET)).where(new Condition(iPartsConst.FIELD_DCS_GUID, "=", CHANGESETID));
        getProject().getEtkDbs().delete(iPartsConst.TABLE_DA_CHANGE_SET, new String[]{ iPartsConst.FIELD_DCS_GUID }, new String[]{ CHANGESETID });

        // Tabelle DA_CHANGE_SET_ENTRY
//            SQLQuery query2 = getProject().getEtkDbs().getNewQuery().delete().from(new Tables(iPartsConst.TABLE_DA_CHANGE_SET_ENTRY)).where(new Condition(iPartsConst.FIELD_DCE_GUID, "=", Condition.PARAMETER_SIGN));
        getProject().getEtkDbs().delete(iPartsConst.TABLE_DA_CHANGE_SET_ENTRY, new String[]{ iPartsConst.FIELD_DCE_GUID }, new String[]{ CHANGESETID });


        EtkRevisionsHelper revisionsHelper = getProject().getEtkDbs().getRevisionsHelper();
        assertNotNull(revisionsHelper);

        iPartsRevisionChangeSet revChangeSet = null;
        iPartsChangeSetId changeSetId = getChangeSetId();
        if (!changeSetId.isEmpty()) {
            revChangeSet = new iPartsRevisionChangeSet(changeSetId, getProject());
            revChangeSet.createInDBIfNotExists(iPartsChangeSetSource.UNITTESTS);
        }
        assertNotNull(revChangeSet);

        // Das ChangeSet aktivieren.
        revisionsHelper.setActiveRevisionChangeSet(revChangeSet, getProject());
        return revisionsHelper;
    }

    /**
     * Deaktiviert das Change Set und löscht es direkt aus den beteiligten Datenbanktabellen.
     *
     * @param revisionsHelper
     */
    private void finalizeRevisionHelper(EtkRevisionsHelper revisionsHelper) {
        if (revisionsHelper != null) {
            // Das Changeset deaktivieren:
            revisionsHelper.setActiveRevisionChangeSet(null, getProject());

            // Tabelle DA_CHANGE_SET
            getProject().getEtkDbs().delete(iPartsConst.TABLE_DA_CHANGE_SET, new String[]{ iPartsConst.FIELD_DCS_GUID }, new String[]{ CHANGESETID });

            // Tabelle DA_CHANGE_SET_ENTRY
            getProject().getEtkDbs().delete(iPartsConst.TABLE_DA_CHANGE_SET_ENTRY, new String[]{ iPartsConst.FIELD_DCE_GUID }, new String[]{ CHANGESETID });
        }
    }

    private void logActiveRevisionChangeSets(EtkRevisionsHelper revisionsHelper, String logTitle) {
        if (revisionsHelper != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(OsUtils.NEWLINE);
            stringBuffer.append("########## ");
            stringBuffer.append(logTitle);
            stringBuffer.append(" ##########");
            stringBuffer.append(OsUtils.NEWLINE);

            for (AbstractRevisionChangeSet activeChangeSet : revisionsHelper.getActiveRevisionChangeSets()) {
                stringBuffer.append("ChangeSet: ");
                stringBuffer.append(activeChangeSet.getChangeSetId().getGUID());
                stringBuffer.append(OsUtils.NEWLINE);

                for (SerializedDBDataObject serializedDBDataObject : (Collection<SerializedDBDataObject>)activeChangeSet.getSerializedDataObjectsMap().values()) {
                    iPartsRevisionsLogger.logChangeSetEntry(serializedDBDataObject, stringBuffer, getProject());
                }

                stringBuffer.append(OsUtils.NEWLINE);
                stringBuffer.append(TranslationKeys.LINE_SEPARATOR);
                stringBuffer.append(OsUtils.NEWLINE);
            }
            System.out.println(stringBuffer.toString());
        }
    }

// ---------------------------------------------------------------------------------------------------------------------
// Testroutinen-Rumpf zum Wegkopieren.
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * Testroutine No #
     */
//    public void testChangeSets() {
//        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
//        try {
//            // Hier den Testcode einfügen.
//        } finally {
//            finalizeRevisionHelper(revisionsHelper);
//        }
//    }

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------


    /**
     * Distinct Testroutine case sensitive
     */
    public void testDistinctCaseSensitive() {
        System.out.println("testDistinct(case sensitive)");
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            // select * from MAT where M_MATNR like 'A9602417%';
//            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, new String[]{ "M_BASE_MATNR" }, null, null , new String[]{ EtkDbConst.FIELD_M_BESTNR }, new String[]{ "A9602417*" }, null, null, true, -1);
            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, new String[]{ EtkDbConst.FIELD_M_MATNR }, null, null, new String[]{ EtkDbConst.FIELD_M_MATNR }, new String[]{ "A9602417*" }, null, null, true, -1);

            EtkRecords recsPdeudoTransaction = new EtkRecords();

            startPseudoTransactionForActiveChangeSet();
            try {
//                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, new String[]{ "M_BASE_MATNR" }, null, null , new String[]{ EtkDbConst.FIELD_M_BESTNR }, new String[]{ "A9602417*" }, null, null, true, -1);
                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, new String[]{ EtkDbConst.FIELD_M_MATNR }, null, null, new String[]{ EtkDbConst.FIELD_M_MATNR }, new String[]{ "A9602417*" }, null, null, true, -1);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            if (!compareRecs(recsPdeudoTransaction, recsGetRecords)) {
                throw new RuntimeException("Fehler");
            }


            // Hier den Testcode einfügen.
        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    /**
     * Distinct Testroutine case IN-sensitive
     */
    public void testDistinctCaseInsensitive() {
        System.out.println("testDistinct(case insensitive)");
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            // select * from MAT where M_MATNR like 'A9602417%';
//            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, new String[]{ "M_BASE_MATNR" }, null, null , new String[]{ EtkDbConst.FIELD_M_BESTNR }, new String[]{ "A9602417*" }, null, null, true, -1);
            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, new String[]{ EtkDbConst.FIELD_M_MATNR }, null, null, new String[]{ EtkDbConst.FIELD_M_BESTNR }, new String[]{ "A9602417*" }, null, null, true, -1);

            EtkRecords recsPdeudoTransaction = new EtkRecords();

            startPseudoTransactionForActiveChangeSet();
            try {
//                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, new String[]{ "M_BASE_MATNR" }, null, null , new String[]{ EtkDbConst.FIELD_M_BESTNR }, new String[]{ "A9602417*" }, null, null, true, -1);
                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, new String[]{ EtkDbConst.FIELD_M_MATNR }, null, null, new String[]{ EtkDbConst.FIELD_M_BESTNR }, new String[]{ "a9602417*" }, null, null, true, -1);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            if (!compareRecs(recsPdeudoTransaction, recsGetRecords)) {
                throw new RuntimeException("Fehler");
            }


            // Hier den Testcode einfügen.
        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    /**
     * Testroutine No 1
     */
    public void testChangeSets001() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {

            // ---------------------------
            // Hier den Testcode einfügen.
            // ---------------------------

            // Der Testfall:
            EtkDataPart part = EtkDataObjectFactory.createDataPart();
            part.init(getProject());
            String matNo = "Unittest4711";
            String testValue = "Lalala";
            boolean recordExists = part.loadFromDB(new iPartsPartId(matNo, ""));
            if (!recordExists) {
                part.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }

            part.setFieldValue(iPartsConst.FIELD_M_BESTNR, testValue, DBActionOrigin.FROM_EDIT);

            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

            // Checken, ob der Datensatz in der DB gelandet ist:
            // Ein Datensatz
            EtkRecord rec = getProject().getEtkDbs().getRecord(EtkDbConst.TABLE_MAT,
                                                               new String[]{ EtkDbConst.FIELD_M_MATNR },
                                                               new String[]{ matNo });

            String resStr = "";
            assertNotNull(rec);
            resStr = rec.getField(iPartsConst.FIELD_M_BESTNR).getAsString();

            assertEquals(testValue, resStr);

            startPseudoTransactionForActiveChangeSet();
            try {
                rec = getProject().getEtkDbs().getRecord(EtkDbConst.TABLE_MAT,
                                                         new String[]{ EtkDbConst.FIELD_M_MATNR },
                                                         new String[]{ matNo });
//                resStr = "";
                assertNotNull(rec);
                resStr = rec.getField(iPartsConst.FIELD_M_BESTNR).getAsString();

                assertEquals(testValue, resStr);

            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            // ---------------------------
            // ---------------------------

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }


    static final String matno = "00Q_14_120_00001";


    /**
     * TextId austauschen
     */
    public void testChangeTextId() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {

            String newTextId = "107527";

            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (!part.existsInDB()) {
                fail(matno + " not in DB");
            }

            EtkMultiSprache value = getProject().getDbLayer().getLanguagesTextsByTextId(newTextId);
            part.setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

            EtkDataPart part1 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
            EtkMultiSprache value1 = part1.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            EtkMultiSprache value2 = new EtkMultiSprache();
            EtkDataPart part2 = new EtkDataPart();

            startPseudoTransactionForActiveChangeSet();
            try {
                part2 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
                value2 = part2.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(value1.equalContent(value2));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testMultiLanguageNew() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {

            String matno = "Otto";

            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
            part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            EtkMultiSprache value = new EtkMultiSprache();
            value.setText(Language.DE, "Deutsch");
            value.setText(Language.EN, "Englisch");
            part.setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

            EtkDataPart part1 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
            EtkMultiSprache value1 = part1.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            EtkMultiSprache value2 = new EtkMultiSprache();
            EtkDataPart part2 = new EtkDataPart();

            startPseudoTransactionForActiveChangeSet();
            try {
                part2 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
                value2 = part2.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(value1.equalContent(value2));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testLike() {
        internTestLike("AN*");
        internTestLike("AN%");
        internTestLike("ABN*");
        internTestLikeCaseInsensitive("AN*");
        internTestLikeCaseInsensitive("AN%");
        internTestLikeCaseInsensitive("ABN*");
    }

    public void internTestLike(String searchString) {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (!part.existsInDB()) {
                fail(matno + " not in DB");
            }

            part.setFieldValue("M_BASE_MATNR", "ANDI", DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_BASE_MATNR" }, new String[]{ searchString }, null, null, false, -1);

            EtkRecords recsPdeudoTransaction = new EtkRecords();

            startPseudoTransactionForActiveChangeSet();
            try {
                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_BASE_MATNR" }, new String[]{ searchString }, null, null, false, -1);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(compareRecs(recsPdeudoTransaction, recsGetRecords));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    /**
     * Like bei caseinsensitiven Feldern
     *
     * @param searchString
     */
    public void internTestLikeCaseInsensitive(String searchString) {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (!part.existsInDB()) {
                fail(matno + " not in DB");
            }

            part.setFieldValue(EtkDbConst.FIELD_M_BESTNR, "AnDi", DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ EtkDbConst.FIELD_M_BESTNR }, new String[]{ searchString }, null, null, false, -1);

            EtkRecords recsPdeudoTransaction = new EtkRecords();

            startPseudoTransactionForActiveChangeSet();
            try {
                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ EtkDbConst.FIELD_M_BESTNR }, new String[]{ searchString }, null, null, false, -1);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(compareRecs(recsPdeudoTransaction, recsGetRecords));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testdelete() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (!part.existsInDB()) {
                fail(matno + " not in DB");
            }

            revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(part);

            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_MATNR" }, new String[]{ matno }, null, null, false, -1);

            EtkRecords recsPdeudoTransaction = new EtkRecords();

            startPseudoTransactionForActiveChangeSet();
            try {
                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_MATNR" }, new String[]{ matno }, null, null, false, -1);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(compareRecs(recsPdeudoTransaction, recsGetRecords));
            assertTrue(recsGetRecords.size() == 0);

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }

        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

        if (!part.existsInDB()) {
            fail(matno + " was deleted from DB");
        }
    }

    public void testInsertAndDelete() {
        internTestInsertAndDelete(false);
        internTestInsertAndDelete(true);
    }

    public void internTestInsertAndDelete(boolean useTwoChangeSets) {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            String matno = "otto";
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (part.existsInDB()) {
                fail(matno + " exists in DB");
            }

            part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

            if (useTwoChangeSets) {
                addTempChangeSetToRevisionHelper(revisionsHelper);
            }

            EtkRecords recs = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_MATNR" }, new String[]{ matno }, null, null, false, -1);

            assertTrue(recs.size() == 1);

            part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
            revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(part);

            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_MATNR" }, new String[]{ matno }, null, null, false, -1);

            EtkRecords recsPdeudoTransaction = new EtkRecords();

            startPseudoTransactionForActiveChangeSet();
            try {
                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_MATNR" }, new String[]{ matno }, null, null, false, -1);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(compareRecs(recsPdeudoTransaction, recsGetRecords));
            assertTrue(recsGetRecords.size() == 0);

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    private void addTempChangeSetToRevisionHelper(EtkRevisionsHelper revisionsHelper) {
        // Temporäres ChangeSet für RelatedEdit erzeugen
        AbstractRevisionChangeSet changeSetForRelatedEdit = revisionsHelper.createTempChangeSet(getProject());

        // Temporäres ChangeSet für RelatedEdit hinzufügen ohne DataChangedEvents (das neue ChangeSet ist ja leer)
        Collection<AbstractRevisionChangeSet> activeChangeSets = revisionsHelper.getActiveRevisionChangeSets();
        Collection<AbstractRevisionChangeSet> activeChangeSetsForRelatedEdit = new DwList<AbstractRevisionChangeSet>(activeChangeSets);
        activeChangeSetsForRelatedEdit.add(changeSetForRelatedEdit);
        revisionsHelper.setActiveRevisionChangeSets(activeChangeSetsForRelatedEdit, changeSetForRelatedEdit, false,
                                                    getProject());
    }

    public void testDeleteAndInsert() {
        internTestDeleteAndInsert(false);
        internTestDeleteAndInsert(true);
    }

    public void internTestDeleteAndInsert(boolean useTwoChangeSets) {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (!part.existsInDB()) {
                fail(matno + " not in DB");
            }

            revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(part);

            EtkRecords recs = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_MATNR" }, new String[]{ matno }, null, null, false, -1);
            assertTrue(recs.size() == 0);

            if (useTwoChangeSets) {
                addTempChangeSetToRevisionHelper(revisionsHelper);
            }


            part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
            part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            part.setFieldValue("M_MATNR", matno, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_MATNR" }, new String[]{ matno }, null, null, false, -1);

            EtkRecords recsPdeudoTransaction = new EtkRecords();

            startPseudoTransactionForActiveChangeSet();
            try {
                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(EtkDbConst.TABLE_MAT, null, null, null, new String[]{ "M_MATNR" }, new String[]{ matno }, null, null, false, -1);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(compareRecs(recsPdeudoTransaction, recsGetRecords));
            assertTrue(recsGetRecords.size() == 1);

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }

        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

        if (!part.existsInDB()) {
            fail(matno + " was deleted from DB");
        }
    }

    public void testDeleteAndInsertDaSeries() {
        internTestDeleteAndInsertDaSeries(false);
        internTestDeleteAndInsertDaSeries(true);
    }

    public void internTestDeleteAndInsertDaSeries(boolean useTwoChangeSets) {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        String serNo = "C204";

        try {
            iPartsDataSeries series = new iPartsDataSeries(getProject(), new iPartsSeriesId(serNo));

            if (!series.existsInDB()) {
                fail(serNo + " not in DB");
            }

            revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(series);

            EtkRecords recs = getProject().getEtkDbs().getRecords(iPartsConst.TABLE_DA_SERIES, null, null, null,
                                                                  new String[]{ "DS_SERIES_NO" }, new String[]{ serNo },
                                                                  null, null, false, -1);
            assertTrue(recs.size() == 0);

            if (useTwoChangeSets) {
                addTempChangeSetToRevisionHelper(revisionsHelper);
            }

            series = new iPartsDataSeries(getProject(), new iPartsSeriesId(serNo));
            series.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            series.setFieldValue("DS_SERIES_NO", serNo, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(series);

            EtkRecords recsGetRecords = getProject().getEtkDbs().getRecords(iPartsConst.TABLE_DA_SERIES, null, null, null,
                                                                            new String[]{ "DS_SERIES_NO" }, new String[]{ serNo },
                                                                            null, null, false, -1);

            EtkRecords recsPdeudoTransaction = new EtkRecords();

            startPseudoTransactionForActiveChangeSet();
            try {
                recsPdeudoTransaction = getProject().getEtkDbs().getRecords(iPartsConst.TABLE_DA_SERIES, null, null, null,
                                                                            new String[]{ "DS_SERIES_NO" }, new String[]{ serNo },
                                                                            null, null, false, -1);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(compareRecs(recsPdeudoTransaction, recsGetRecords));
            assertTrue(recsGetRecords.size() == 1);

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }

        iPartsDataSeries series = new iPartsDataSeries(getProject(), new iPartsSeriesId(serNo));

        if (!series.existsInDB()) {
            fail(serNo + " was deleted from DB");
        }
    }

    /**
     * Einen Text ändern
     */
    public void testMultiLangChangeOne() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            String deText = "Herbert";

            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (!part.existsInDB()) {
                fail(matno + " not in DB");
            }

            EtkMultiSprache value = part.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            value.setText(Language.DE, deText);
            value.setTextId(""); // In der DB vorhandene Texte mit Text-ID können in ChangeSets nicht verändert werden -> Text-ID entfernen
            part.setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);


            EtkDataPart part1 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
            EtkMultiSprache value1 = part1.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            assertEquals(deText, value1.getText(Language.DE.getCode()));

            startPseudoTransactionForActiveChangeSet();
            try {
                EtkDataPart part2 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
                EtkMultiSprache value2 = part2.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

                assertEquals(deText, value2.getText(Language.DE.getCode()));

                assertTrue(value1.equalContent(value2));

            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    /**
     * Testet, ob es auch mit S_BENENN_LANG geht
     */
    public void testMultiLangChangeOneLongText() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            String deText = "";

            for (int i = 0; i < 1000; i++) {
                deText += " Herbert";
            }

            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (!part.existsInDB()) {
                fail(matno + " not in DB");
            }

            EtkMultiSprache value = part.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            value.setText(Language.DE, deText);
            value.setTextId(""); // In der DB vorhandene Texte mit Text-ID können in ChangeSets nicht verändert werden -> Text-ID entfernen
            part.setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);


            EtkDataPart part1 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
            EtkMultiSprache value1 = part1.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            assertEquals(deText, value1.getText(Language.DE.getCode()));

            startPseudoTransactionForActiveChangeSet();
            try {
                EtkDataPart part2 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
                EtkMultiSprache value2 = part2.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

                assertEquals(deText, value2.getText(Language.DE.getCode()));

                assertTrue(value1.equalContent(value2));

            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testCacheForGetRecords() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper();
        try {
            // Zwei Materialien anlegen und ins changeset speichern.
            // Bei jedem mal speichern müsste sich die entriesPerTable Map löschen
            // Wenn beide Materialien aber mit getRecords abgerufen werden muss der zweite Abruf über die Map laufen

            // erstes Material
            EtkDataPart part = EtkDataObjectFactory.createDataPart();
            part.init(getProject());
            String matNo = "Unittest4711";
            String testValue = "Lalala";
            boolean recordExists = part.loadFromDB(new iPartsPartId(matNo, ""));
            // loadFromDB sollte auch einen Eintrag im Cache erzeugen
            AbstractRevisionChangeSet activeChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
            assertEquals(1, activeChangeSet.getSerializedDataObjectPerTableMap().size());
            if (!recordExists) {
                part.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            part.setFieldValue(iPartsConst.FIELD_M_BESTNR, testValue, DBActionOrigin.FROM_EDIT);

            // zum changeset hinzufügen (map muss dabei geleert werden)
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);
            assertTrue(activeChangeSet.getSerializedDataObjectPerTableMap().isEmpty());

            // zweites material
            EtkDataPart part2 = EtkDataObjectFactory.createDataPart();
            part2.init(getProject());
            String matNo2 = "Unittest47112222";
            recordExists = part2.loadFromDB(new iPartsPartId(matNo2, ""));
            // loadFromDB sollte auch einen Eintrag im Cache erzeugen
            assertEquals(1, activeChangeSet.getSerializedDataObjectPerTableMap().size());
            if (!recordExists) {
                part2.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            part2.setFieldValue(iPartsConst.FIELD_M_BESTNR, testValue, DBActionOrigin.FROM_EDIT);

            // zum changeset hinzufügen (map muss dabei geleert werden)
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part2);
            assertTrue(activeChangeSet.getSerializedDataObjectPerTableMap().isEmpty());

            // ersten Datensatz abrufen (dabei muss die map mit beiden Einträgen aufgebaut werden)
            EtkRecord rec = getProject().getEtkDbs().getRecord(EtkDbConst.TABLE_MAT,
                                                               new String[]{ EtkDbConst.FIELD_M_MATNR },
                                                               new String[]{ matNo });
            assertEquals(1, activeChangeSet.getSerializedDataObjectPerTableMap().size());
            Map<String, List<SerializedDBDataObject>> serializedDataObjectPerTableMap = activeChangeSet.getSerializedDataObjectPerTableMap();
            assertEquals(2, serializedDataObjectPerTableMap.get(EtkDbConst.TABLE_MAT).size());

            // zweiten Datensatz abrufen (dabei muss die map verwendet werden)
            EtkRecord rec2 = getProject().getEtkDbs().getRecord(EtkDbConst.TABLE_MAT,
                                                                new String[]{ EtkDbConst.FIELD_M_MATNR },
                                                                new String[]{ matNo2 });
            assertEquals(1, activeChangeSet.getSerializedDataObjectPerTableMap().size());
            Map<String, List<SerializedDBDataObject>> serializedDataObjectPerTableMap2 = activeChangeSet.getSerializedDataObjectPerTableMap();
            assertEquals(2, serializedDataObjectPerTableMap2.get(EtkDbConst.TABLE_MAT).size());

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testMultiLangAddNew() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (!part.existsInDB()) {
                fail(matno + " not in DB");
            }

            EtkMultiSprache value = new EtkMultiSprache();
            value.setText(Language.DE, "DE");
            value.setText(Language.EN, "EN");
            value.setText(Language.FR, "FR");
            part.setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);


            EtkDataPart part1 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
            EtkMultiSprache value1 = part1.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            startPseudoTransactionForActiveChangeSet();
            try {
                EtkDataPart part2 = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
                EtkMultiSprache value2 = part2.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
                assertTrue(value1.equalContent(value2));

            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    /**
     * Testest mergeTempSerializedDataObjects. Hier werden von zwei verschiedenen Parts an der gleichen TextId Änderungen vorgenommen
     */
    public void testMultiLangAddNewInTwoParts() {
        String matNo1 = "00Q_01_010_00001";
        String matNo2 = "00R_01_010_00001";
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!

        // Anlegen von Texten mit Text-ID in ChangeSets explizit erlauben
        boolean oldTextsWithIdCreatableInChangeSets = iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS;
        iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS = true;
        try {

            String textId;
            {
                EtkDataPart part1 = EtkDataObjectFactory.createDataPart(getProject(), matNo1, "");

                if (!part1.existsInDB()) {
                    fail(matNo1 + " not in DB");
                }

                EtkMultiSprache value1 = part1.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
                textId = value1.getTextId();

                // Text mit der Text-ID in der DB löschen, damit er im ChangeSet neu angelegt werden kann
                getProject().getDbLayer().delete(EtkDbConst.TABLE_SPRACHE, new String[]{ FIELD_S_TEXTID }, new String[]{ textId });

                EtkMultiSprache value = new EtkMultiSprache();
                value.setText(Language.DE, "DE");
                value.setText(Language.EN, "EN");
                value.setText(Language.FR, "FR");
                value.setTextId(textId);
                part1.setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, value, DBActionOrigin.FROM_EDIT);
                revisionsHelper.addDataObjectToActiveChangeSetForEdit(part1);

                addTempChangeSetToRevisionHelper(revisionsHelper);
            }

            {
                EtkDataPart part2 = EtkDataObjectFactory.createDataPart(getProject(), matNo2, "");

                if (!part2.existsInDB()) {
                    fail(matNo2 + " not in DB");
                }

                EtkMultiSprache value = part2.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
                value.setText(Language.ES, "ES");
                value.setTextId(textId);
                part2.setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, value, DBActionOrigin.FROM_EDIT);
                revisionsHelper.addDataObjectToActiveChangeSetForEdit(part2);
            }


            EtkDataPart part1 = EtkDataObjectFactory.createDataPart(getProject(), matNo1, "");
            EtkMultiSprache value1 = part1.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);

            assertEquals("ES", value1.getText(Language.ES.getCode()));
            assertEquals("EN", value1.getText(Language.EN.getCode()));

            startPseudoTransactionForActiveChangeSet();
            try {
                EtkDataPart part2 = EtkDataObjectFactory.createDataPart(getProject(), matNo1, "");
                EtkMultiSprache value2 = part2.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
                assertTrue(value1.equalContent(value2));

            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

        } finally {
            iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS = oldTextsWithIdCreatableInChangeSets;
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testMultiLangUpdateTextId() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            iPartsCodeDataId codeId = new iPartsCodeDataId("C19", "C454", "F", "20020114", iPartsImportDataOrigin.MAD);
            String matno = "60V_82_345_00001";

            iPartsDataCode code = new iPartsDataCode(getProject(), codeId);

            if (!code.existsInDB()) {
                fail("code not in DB");
            }

            EtkMultiSprache value = code.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DC_DESC);


            value.setText(Language.DE, "DE");
            value.setText(Language.EN, "EN");
            value.setText(Language.FR, "FR");
            code.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DC_DESC, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(code);


            // Die Texte der Textid wurden geändert, deshalb muss jetzt beim Code und beim Material, welches die gleiche TextId verwendet der neue Text drin sein

            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");

            if (!part.existsInDB()) {
                fail(matno + " not in DB");
            }
            code = new iPartsDataCode(getProject(), codeId);

            EtkMultiSprache value1 = part.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
            EtkMultiSprache value2 = code.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DC_DESC);
            EtkMultiSprache value3 = null;
            EtkMultiSprache value4 = null;


            startPseudoTransactionForActiveChangeSet();
            try {

                part = EtkDataObjectFactory.createDataPart(getProject(), matno, "");
                code = new iPartsDataCode(getProject(), codeId);

                value3 = part.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
                value4 = code.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DC_DESC);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(value1.equalContent(value2));
            assertTrue(value3.equalContent(value4));
            assertTrue(value1.equalContent(value3));
            assertTrue(value2.equalContent(value4));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    /**
     * @return
     */
    public EtkRevisionsHelper getRevisionsHelper() {
        if (getProject() != null) {
            return getProject().getEtkDbs().getRevisionsHelper();
        } else {
            return null;
        }
    }

    /**
     * Einen Text ändern
     */
    public void testArrayChangeOne() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            PartListEntryId id = new PartListEntryId("S01_24_060_00001", "", "00001");

            String newArrayValue = "FCN";

            EtkDataPartListEntry part = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);

            if (!part.existsInDB()) {
                fail(id.toString() + " not in DB");
            }

            EtkDataArray value = part.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            value.add(newArrayValue);
            part.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);


            EtkDataPartListEntry part1 = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
            EtkDataArray value1 = part1.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            assertEquals(newArrayValue, value1.getArrayAsStringList().get(1));

            EtkDataPartListEntry part2;
            EtkDataArray value2;

            startPseudoTransactionForActiveChangeSet();
            try {

                part2 = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
                value2 = part2.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

                assertEquals(newArrayValue, value2.getArrayAsStringList().get(1));


            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(value1.equalValues(value2));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    /**
     * Testet, ob der Merge bei doppelten Änderungen richtig funktioniert
     */
    public void testArrayChangeTwice() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            PartListEntryId id = new PartListEntryId("S01_24_060_00001", "", "00001");

            String newArrayValue = "FCN";

            EtkDataPartListEntry part = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);

            if (!part.existsInDB()) {
                fail(id.toString() + " not in DB");
            }

            EtkDataArray value = part.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            value.add("Otto");
            part.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

            addTempChangeSetToRevisionHelper(revisionsHelper);

            // Jetzt im zweiten Changeset nochmal ändern
            part = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);

            value = part.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            value.clear(true);
            value.add(newArrayValue);
            value.add(newArrayValue);
            part.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);


            EtkDataPartListEntry part1 = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
            EtkDataArray value1 = part1.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            assertEquals(newArrayValue, value1.getArrayAsStringList().get(1));

            EtkDataPartListEntry part2;
            EtkDataArray value2;

            startPseudoTransactionForActiveChangeSet();
            try {

                part2 = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
                value2 = part2.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

                assertEquals(newArrayValue, value2.getArrayAsStringList().get(1));


            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(value1.equalValues(value2));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    /**
     * Test füegt ein Element hinzu und löscht alles wieder und fürgt wieder eins hinzu
     */
    public void testArrayChangeTwiceWithClear() {
        internTestArrayChangeTwiceWithClear(false);
        internTestArrayChangeTwiceWithClear(true);
    }

    public void internTestArrayChangeTwiceWithClear(boolean useMultipleChangeSets) {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            PartListEntryId id = new PartListEntryId("S01_24_060_00001", "", "00001");

            String newArrayValue = "FCN";
            String newArrayValue2 = "... wird geschlagen von Greuther Fürth :)";

            EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);

            if (!partListEntry.existsInDB()) {
                fail(id + " not in DB");
            }

            EtkDataArray value = partListEntry.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            value.add("Otto");
            partListEntry.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(partListEntry);

            if (useMultipleChangeSets) {
                addTempChangeSetToRevisionHelper(revisionsHelper);
            }

            // Jetzt im (neuen) Array Changeset nochmal ändern
            partListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);

            value = partListEntry.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            value.clear(true);
            value.add(newArrayValue);
            value.add(newArrayValue2);
            partListEntry.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(partListEntry);


            EtkDataPartListEntry pleCheckJoinSim = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
            EtkDataArray valueCheckJoinSim = pleCheckJoinSim.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            assertEquals(2, valueCheckJoinSim.getArrayAsStringList().size());
            assertEquals(newArrayValue, valueCheckJoinSim.getArrayAsStringList().get(0));
            assertEquals(newArrayValue2, valueCheckJoinSim.getArrayAsStringList().get(1));

            EtkDataPartListEntry pleCheckPseudoTrans;
            EtkDataArray valueCheckPseudoTrans;

            startPseudoTransactionForActiveChangeSet(false);
            try {
                pleCheckPseudoTrans = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
                valueCheckPseudoTrans = pleCheckPseudoTrans.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

                assertEquals(2, valueCheckPseudoTrans.getArrayAsStringList().size());
                assertEquals(newArrayValue, valueCheckPseudoTrans.getArrayAsStringList().get(0));
                assertEquals(newArrayValue2, valueCheckPseudoTrans.getArrayAsStringList().get(1));
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(valueCheckJoinSim.equalValues(valueCheckPseudoTrans));

            if (useMultipleChangeSets) {
                addTempChangeSetToRevisionHelper(revisionsHelper);
            }

            // Jetzt im (neuen) Changeset Array nochmal komplett ersetzen
            partListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);

            String finalValue = "LEGO macht viel mehr Spaß als Fußball";
            value = new EtkDataArray();
            value.add(finalValue);
            partListEntry.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(partListEntry);


            pleCheckJoinSim = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
            valueCheckJoinSim = pleCheckJoinSim.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            assertEquals(1, valueCheckJoinSim.getArrayAsStringList().size());
            assertEquals(finalValue, valueCheckJoinSim.getArrayAsStringList().get(0));

            startPseudoTransactionForActiveChangeSet(false);
            try {
                pleCheckPseudoTrans = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
                valueCheckPseudoTrans = pleCheckPseudoTrans.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

                assertEquals(1, valueCheckPseudoTrans.getArrayAsStringList().size());
                assertEquals(finalValue, valueCheckPseudoTrans.getArrayAsStringList().get(0));
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(valueCheckJoinSim.equalValues(valueCheckPseudoTrans));

            if (useMultipleChangeSets) {
                addTempChangeSetToRevisionHelper(revisionsHelper);
            }

            // Jetzt im (neuen) Changeset das Array komplett entfernen
            partListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);

            partListEntry.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, null, DBActionOrigin.FROM_EDIT);
            partListEntry.setIdForArray(iPartsConst.FIELD_K_SA_VALIDITY, "", DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(partListEntry);


            pleCheckJoinSim = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
            valueCheckJoinSim = pleCheckJoinSim.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            assertTrue(valueCheckJoinSim.getArrayAsStringList().isEmpty());
            assertEquals("", valueCheckJoinSim.getArrayId());

            startPseudoTransactionForActiveChangeSet(false);
            try {
                pleCheckPseudoTrans = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
                valueCheckPseudoTrans = pleCheckPseudoTrans.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

                assertTrue(valueCheckPseudoTrans.getArrayAsStringList().isEmpty());
                assertEquals("", valueCheckPseudoTrans.getArrayId());
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }
        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testArrayAddNew() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            PartListEntryId id = new PartListEntryId("S01_24_060_00001", "", "00001");

            String newArrayValue = "FCN";

            EtkDataPartListEntry part = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);

            if (!part.existsInDB()) {
                fail(id.toString() + " not in DB");
            }

            EtkDataArray value = new EtkDataArray();

            value.add("Nix");
            value.add(newArrayValue);
            part.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);


            EtkDataPartListEntry part1 = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
            EtkDataArray value1 = part1.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            assertEquals(newArrayValue, value1.getArrayAsStringList().get(1));

            EtkDataPartListEntry part2;
            EtkDataArray value2;

            startPseudoTransactionForActiveChangeSet();
            try {

                part2 = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
                value2 = part2.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

                assertEquals(newArrayValue, value2.getArrayAsStringList().get(1));


            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(value1.equalValues(value2));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testArrayAllNew() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            PartListEntryId id = new PartListEntryId("Otto", "", "00001");

            String newArrayValue = "FCN";

            EtkDataPartListEntry part = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
            part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            EtkDataArray value = new EtkDataArray();

            value.add("Nix");
            value.add(newArrayValue);
            part.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, value, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

            EtkDataPartListEntry part1 = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
            EtkDataArray value1 = part1.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

            assertEquals(newArrayValue, value1.getArrayAsStringList().get(1));

            EtkDataPartListEntry part2;
            EtkDataArray value2;

            startPseudoTransactionForActiveChangeSet();
            try {

                part2 = EtkDataObjectFactory.createDataPartListEntry(getProject(), id);
                value2 = part2.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);

                assertEquals(newArrayValue, value2.getArrayAsStringList().get(1));


            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(value1.equalValues(value2));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testBlobAllNew() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            PoolId id = new PoolId("Otto", "Duddo", "", "");

            byte[] newBlob = createBlob();

            EtkDataPool pool = EtkDataObjectFactory.createDataPool();
            pool.init(getProject());
            if (pool.loadFromDB(id)) {
                fail(id.toString() + " is in DB");
            }
            pool.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            pool.setFieldValueAsBlob(FIELD_P_DATA, newBlob, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(pool);

            EtkDataPool pool1 = EtkDataObjectFactory.createDataPool();
            pool1.init(getProject());
            pool1.loadFromDB(id);

            byte[] value1 = pool1.getFieldValueAsBlob(FIELD_P_DATA);

            assertTrue(ArrayUtil.areEqual(value1, newBlob));

            EtkDataPool pool2;
            byte[] value2;

            startPseudoTransactionForActiveChangeSet();
            try {

                pool2 = EtkDataObjectFactory.createDataPool();
                pool2.init(getProject());
                pool2.loadFromDB(id);
                value2 = pool2.getFieldValueAsBlob(FIELD_P_DATA);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue(ArrayUtil.areEqual(value1, value2));

        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testBlobUpdate() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            PoolId id = new PoolId("B01010000130.tif", "", "", "");

            byte[] newBlob = createBlob();

            EtkDataPool pool = EtkDataObjectFactory.createDataPool();
            pool.init(getProject());
            if (!pool.loadFromDB(id)) {
                fail(id.toString() + " is not in DB");
            }

            pool.setFieldValueAsBlob(FIELD_P_DATA, newBlob, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(pool);

            String oldBlobValue = revisionsHelper.getActiveRevisionChangeSetForEdit().getSerializedDataObject(pool.getAsId()).getAttributes().iterator().next().getOldValue();
            assertTrue("No old value for modified BLOB", StrUtils.isValid(oldBlobValue));

            // Auslesen mit GetRecords-Simulation
            pool = EtkDataObjectFactory.createDataPool();
            pool.init(getProject());
            pool.loadFromDB(id);

            byte[] value1 = pool.getFieldValueAsBlob(FIELD_P_DATA);

            assertTrue("BLOB was not updated with getRecords() simulation", ArrayUtil.areEqual(value1, newBlob));

            EtkDataPool pool2;
            byte[] value2;

            // Auslesen mit Pseudo-Transaktion
            startPseudoTransactionForActiveChangeSet();
            try {

                pool2 = EtkDataObjectFactory.createDataPool();
                pool2.init(getProject());
                pool2.loadFromDB(id);
                value2 = pool2.getFieldValueAsBlob(FIELD_P_DATA);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            assertTrue("BLOB was not updated with pseudo transaction", ArrayUtil.areEqual(value1, value2));

            // Primärschlüsseländerung mit einem veränderten BLOB-Attribut
            id = new PoolId("B01010000130_2.tif", "", "", "");
            pool.setId(id, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(pool);

            // Auslesen mit GetRecords-Simulation
            pool = EtkDataObjectFactory.createDataPool();
            pool.init(getProject());
            pool.loadFromDB(id);

            value1 = pool.getFieldValueAsBlob(FIELD_P_DATA);
            if ((value1 == null) || (value1.length == 0)) {
                fail("BLOB was not loaded before PK change with getRecords() simulation");
            }
            assertTrue("BLOB is different after PK change with getRecords() simulation", ArrayUtil.areEqual(value1, newBlob));

            // Auslesen mit Pseudo-Transaktion
            startPseudoTransactionForActiveChangeSet();
            try {

                pool2 = EtkDataObjectFactory.createDataPool();
                pool2.init(getProject());
                pool2.loadFromDB(id);
                value2 = pool2.getFieldValueAsBlob(FIELD_P_DATA);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            if ((value2 == null) || (value2.length == 0)) {
                fail("BLOB was not loaded before PK change with pseudo transaction");
            }
            assertTrue("BLOB is different after PK change with pseudo transaction", ArrayUtil.areEqual(value1, value2));
        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    public void testBlobWithPKChange() {
        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            PoolId id = new PoolId("B01010000130.tif", "", "", "");

            // Primärschlüsseländerung mit einem vorhandenen BLOB-Attribut
            EtkDataPool pool = EtkDataObjectFactory.createDataPool();
            pool.init(getProject());
            if (!pool.loadFromDB(id)) {
                fail(id.toString() + " is not in DB");
            }
            byte[] originalValue = pool.getFieldValueAsBlob(FIELD_P_DATA);
            if ((originalValue == null) || (originalValue.length == 0)) {
                fail(id.toString() + " has no image data in DB");
            }

            // Neuen EtkDataPool mit derselben ID erzeugen, wo der BLOB noch nicht geladen ist
            pool = EtkDataObjectFactory.createDataPool();
            pool.init(getProject());
            pool.loadFromDB(id);
            id = new PoolId("B01010000130_2.tif", "", "", ""); // neuer Primärschlüssel
            pool.setId(id, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(pool);

            // Auslesen mit GetRecords-Simulation
            pool = EtkDataObjectFactory.createDataPool();
            pool.init(getProject());
            pool.loadFromDB(id);

            byte[] value1 = pool.getFieldValueAsBlob(FIELD_P_DATA);
            if ((value1 == null) || (value1.length == 0)) {
                fail("BLOB was not loaded before PK change with getRecords() simulation");
            }
            assertTrue("BLOB is different after PK change with getRecords() simulation", ArrayUtil.areEqual(value1, originalValue));

            // Auslesen mit Pseudo-Transaktion
            byte[] value2;
            startPseudoTransactionForActiveChangeSet();
            try {

                EtkDataPool pool2 = EtkDataObjectFactory.createDataPool();
                pool2.init(getProject());
                pool2.loadFromDB(id);
                value2 = pool2.getFieldValueAsBlob(FIELD_P_DATA);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            if ((value2 == null) || (value2.length == 0)) {
                fail("BLOB was not loaded before PK change with pseudo transaction");
            }
            assertTrue("BLOB is different after PK change with pseudo transaction", ArrayUtil.areEqual(value1, value2));
        } finally {
            finalizeRevisionHelper(revisionsHelper);
        }
    }

    private byte[] createBlob() {
        int size = 300000;
        Random rand = new Random();
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte)(rand.nextInt(255));
        }
        return result;
    }

    /**
     * DAIMLER-5099 Test 1a
     */
    public void testPKChanges1a() {
        // 1a) Keine Zeichnungen vorhanden (z.B. 62E_00_001_00001) -> Zeichnung A hinzufügen + speichern -> Zeichnung B hinzufügen
        // und Zeichnung A hinter Zeichnung B schieben + speichern -> Kontrolle (B, A) -> Zeichnung B löschen + speichern -> Kontrolle (A)
        // -> Zeichnung A löschen + speichern -> Kontrolle (leer)
        AbstractPKChangeTest test = new AbstractPKChangeTest() {
            @Override
            public void doActions(int permutation) {
                boolean useParentObject = false;
                boolean useSecondSequence = false;

                switch (permutation) {
                    case 0:
                        useParentObject = false;
                        useSecondSequence = false;
                        break;
                    case 1:
                        useParentObject = true;
                        useSecondSequence = false;
                        break;
                    case 2:
                        useParentObject = false;
                        useSecondSequence = true;
                        break;
                    case 3:
                        useParentObject = true;
                        useSecondSequence = true;
                        break;
                }

                System.out.println("Executing test testPKChanges1a() with useParentObject=" + useParentObject + " and useSecondSequence="
                                   + useSecondSequence);

                String moduleNo = moduleNoWithNoImage;
                DataImageId k1 = new DataImageId(moduleNo, "", "KEY1");
                DataImageId k2 = new DataImageId(moduleNo, "", "KEY2");

                if (useParentObject) {
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""));
                    if (!assembly.existsInDB()) {
                        fail("Assembly: " + moduleNo + " not in DB");
                    }
                    setParentObject(assembly);
                }

                insert(k1, "A");
                save();
                testExists(k1, "A");

                insert(k2, "B");
                move("A", k1, k2, false);
                move("B", k2, k1, false);

                save();

                testExists(k2, "A");
                testExists(k1, "B");

                if (useSecondSequence) {
                    move("A", k2, k1, true);
                    delete(k1, "B");
                } else {
                    delete(k1, "B");
                    move("A", k2, k1, true);
                }
                save();

                testExists(k1, "A");
                testNotExists(k2);

                delete(k1, "A");
                save();

                testNotExists(k1);
                testNotExists(k2);
            }

            @Override
            protected int getNumberOfPermutations() {
                return 4;
            }

            @Override
            protected EtkDataObject createDataObject() {
                return EtkDataObjectFactory.createDataImage(getProject(), new AssemblyId("", ""), "", "", "");
            }

            @Override
            protected String getPayLoadAttribute() {
                return FIELD_I_IMAGES;
            }

            @Override
            protected DBDataObjectList getObjectsList() {
                if (getParentObject() != null) {
                    return ((EtkDataAssembly)getParentObject()).getUnfilteredImages();
                }
                return new EtkDataImageList();
            }
        };
        test.excecute();
    }

    /**
     * DAIMLER-5099 Test 1b
     */
    public void testPKChanges1b() {
        // Keine Zeichnungen vorhanden (z.B. 62E_00_001_00001) -> Zeichnung A, B und C hinzufügen + speichern ->
        // Zeichnung A hinter Zeichnung B schieben + speichern ->
        // Zeichnung A hinter Zeichnung C schieben + speichern ->
        // Zeichnung B löschen + speichern -> Kontrolle (C, A) ->
        // Zeichnung C löschen + speichern -> Kontrolle (A) ->
        // Zeichnung A löschen + speichern -> Kontrolle (leer)
        AbstractPKChangeTest test = new AbstractPKChangeTest() {
            @Override
            public void doActions(int permutation) {
                boolean useParentObject = false;
                boolean useSecondSequence = false;

                switch (permutation) {
                    case 0:
                        useParentObject = false;
                        useSecondSequence = false;
                        break;
                    case 1:
                        useParentObject = true;
                        useSecondSequence = false;
                        break;
                    case 2:
                        useParentObject = false;
                        useSecondSequence = true;
                        break;
                    case 3:
                        useParentObject = true;
                        useSecondSequence = true;
                        break;
                }

                System.out.println("Executing test testPKChanges1b() with useParentObject=" + useParentObject + " and useSecondSequence="
                                   + useSecondSequence);

                String moduleNo = moduleNoWithNoImage;
                DataImageId key1 = new DataImageId(moduleNo, "", "KEY1");
                DataImageId key2 = new DataImageId(moduleNo, "", "KEY2");
                DataImageId key3 = new DataImageId(moduleNo, "", "KEY3");

                if (useParentObject) {
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""));
                    if (!assembly.existsInDB()) {
                        fail("Assembly: " + moduleNo + " not in DB");
                    }
                    setParentObject(assembly);
                }

                insert(key1, "A");
                insert(key2, "B");
                insert(key3, "C");
                save();
                testExists(key1, "A");
                testExists(key2, "B");
                testExists(key3, "C");

                if (useSecondSequence) {
                    move("A", key1, key2, false);
                    move("B", key2, key1, false);
                } else {
                    move("B", key2, key1, false);
                    move("A", key1, key2, false);
                }
                save();
                testExists(key1, "B");
                testExists(key2, "A");
                testExists(key3, "C");


                if (useSecondSequence) {
                    move("A", key2, key3, false);
                    move("C", key3, key2, false);
                } else {
                    move("C", key3, key2, false);
                    move("A", key2, key3, false);
                }
                save();
                testExists(key1, "B");
                testExists(key2, "C");
                testExists(key3, "A");

                if (useSecondSequence) {
                    delete(key1, "B");
                    move("C", key2, key1, false);
                    move("A", key3, key2, true);
                } else {
                    delete(key1, "B");
                    move("A", key3, key2, true);
                    move("C", key2, key1, false);
                }

                save();
                testExists(key1, "C");
                testExists(key2, "A");
                testNotExists(key3);


                delete(key1, "C");
                move("A", key2, key1, true);
                save();
                testExists(key1, "A");
                testNotExists(key2);
                testNotExists(key3);


                delete(key1, "A");
                save();
                testNotExists(key1);
                testNotExists(key2);
                testNotExists(key3);
            }

            @Override
            protected int getNumberOfPermutations() {
                return 4;
            }

            @Override
            protected EtkDataObject createDataObject() {
                return EtkDataObjectFactory.createDataImage(getProject(), new AssemblyId("", ""), "", "", "");
            }

            @Override
            protected String getPayLoadAttribute() {
                return FIELD_I_IMAGES;
            }

            @Override
            protected DBDataObjectList getObjectsList() {
                if (getParentObject() != null) {
                    return ((EtkDataAssembly)getParentObject()).getUnfilteredImages();
                }
                return new EtkDataImageList();
            }
        };

        test.excecute();
    }

    /**
     * DAIMLER-5099 Test 1c
     */
    public void testPKChanges1c() {
        // Eine Zeichnungen A vorhanden (z.B. Modul SA-M03063) ->
        // Zeichnung B hinzufügen und Zeichnung A hinter Zeichnung B schieben + speichern - Kontrolle (B, A) ->
        // Zeichnung B löschen + speichern -> Kontrolle (A) -> Zeichnung A löschen + speichern -> Kontrolle (leer)
        AbstractPKChangeTest test = new AbstractPKChangeTest() {
            @Override
            public void doActions(int permutation) {
                boolean useParentObject = false;
                boolean useSecondSequence = false;

                switch (permutation) {
                    case 0:
                        useParentObject = false;
                        useSecondSequence = false;
                        break;
                    case 1:
                        useParentObject = true;
                        useSecondSequence = false;
                        break;
                    case 2:
                        useParentObject = false;
                        useSecondSequence = true;
                        break;
                    case 3:
                        useParentObject = true;
                        useSecondSequence = true;
                        break;
                }

                System.out.println("Executing test testPKChanges1c() with useParentObject=" + useParentObject + " and useSecondSequence="
                                   + useSecondSequence);

                String moduleNo = moduleNoWithOneImage;
                DataImageId key1 = new DataImageId(moduleNo, "", "KEY1");
                DataImageId keyBefore = new DataImageId(moduleNo, "", "00001");

                String payLoadBefore = "B73060000179";

                if (useParentObject) {
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""));
                    if (!assembly.existsInDB()) {
                        fail("Assembly: " + moduleNo + " not in DB");
                    }
                    setParentObject(assembly);
                }

                // Ist ohne Änderung das erste Bild des Moduls wie erwartet?
                testExists(keyBefore, payLoadBefore);

                insert(key1, "B");

                if (useSecondSequence) {
                    move("B", key1, keyBefore, false);
                    move(payLoadBefore, keyBefore, key1, false);
                } else {
                    move(payLoadBefore, keyBefore, key1, false);
                    move("B", key1, keyBefore, false);
                }
                save();
                testExists(key1, payLoadBefore);
                testExists(keyBefore, "B");

                delete(keyBefore, "B");
                move(payLoadBefore, key1, keyBefore, true);
                save();
                testExists(keyBefore, payLoadBefore);
                testNotExists(key1);

                delete(keyBefore, payLoadBefore);
                save();
                testNotExists(keyBefore);
                testNotExists(key1);
            }

            @Override
            protected int getNumberOfPermutations() {
                return 4;
            }

            @Override
            protected EtkDataObject createDataObject() {
                return EtkDataObjectFactory.createDataImage(getProject(), new AssemblyId("", ""), "", "", "");
            }

            @Override
            protected String getPayLoadAttribute() {
                return FIELD_I_IMAGES;
            }

            @Override
            protected DBDataObjectList getObjectsList() {
                if (getParentObject() != null) {
                    return ((EtkDataAssembly)getParentObject()).getUnfilteredImages();
                }
                return new EtkDataImageList();
            }
        };

        test.excecute();
    }

    /**
     * DAIMLER-5099 Test 1d
     */
    public void testPKChanges1d() {
        // Zwei Zeichnungen A und B vorhanden (z.B. Modul C01_35_075_00001) ->
        // Zeichnung A und B vertauschen + speichern -> Kontrolle (B, A) ->
        // Zeichnung B und A vertauschen + speichern -> Kontrolle (A, B) ->
        // Zeichnung B löschen + speichern -> Kontrolle (A) ->
        // Zeichnung A löschen + speichern -> Kontrolle (leer)
        AbstractPKChangeTest test = new AbstractPKChangeTest() {
            @Override
            public void doActions(int permutation) {
                boolean useParentObject = false;
                boolean useSecondSequence = false;

                switch (permutation) {
                    case 0:
                        useParentObject = false;
                        useSecondSequence = false;
                        break;
                    case 1:
                        useParentObject = true;
                        useSecondSequence = false;
                        break;
                    case 2:
                        useParentObject = false;
                        useSecondSequence = true;
                        break;
                    case 3:
                        useParentObject = true;
                        useSecondSequence = true;
                        break;
                }

                System.out.println("Executing test testPKChanges1d() with useParentObject=" + useParentObject + " and useSecondSequence="
                                   + useSecondSequence);

                String moduleNo = moduleNoWithTwoImages;
                DataImageId key1 = new DataImageId(moduleNo, "", "00001");
                DataImageId key2 = new DataImageId(moduleNo, "", "00002");

                String payLoad1 = "B35075000189";
                String payLoad2 = "B35075000190";

                if (useParentObject) {
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""));
                    if (!assembly.existsInDB()) {
                        fail("Assembly: " + moduleNo + " not in DB");
                    }
                    setParentObject(assembly);
                }

                // Ist ohne Änderung das erste Bild des Moduls wie erwartet?
                testExists(key1, payLoad1);
                testExists(key2, payLoad2);

                if (useSecondSequence) {
                    move(payLoad1, key1, key2, false);
                    move(payLoad2, key2, key1, false);
                } else {
                    move(payLoad2, key2, key1, false);
                    move(payLoad1, key1, key2, false);
                }
                save();
                testExists(key1, payLoad2);
                testExists(key2, payLoad1);


                if (useSecondSequence) {
                    move(payLoad2, key1, key2, false);
                    move(payLoad1, key2, key1, false);
                } else {
                    move(payLoad1, key2, key1, false);
                    move(payLoad2, key1, key2, false);
                }
                save();
                testExists(key1, payLoad1);
                testExists(key2, payLoad2);


                delete(key2, payLoad2);
                save();
                testExists(key1, payLoad1);
                testNotExists(key2);

                delete(key1, payLoad1);
                save();
                testNotExists(key2);
                testNotExists(key1);
            }

            @Override
            protected int getNumberOfPermutations() {
                return 4;
            }

            @Override
            protected EtkDataObject createDataObject() {
                return EtkDataObjectFactory.createDataImage(getProject(), new AssemblyId("", ""), "", "", "");
            }

            @Override
            protected String getPayLoadAttribute() {
                return FIELD_I_IMAGES;
            }

            @Override
            protected DBDataObjectList getObjectsList() {
                if (getParentObject() != null) {
                    return ((EtkDataAssembly)getParentObject()).getUnfilteredImages();
                }
                return new EtkDataImageList();
            }
        };

        test.excecute();
    }

    public void testDeleteTwoRecords() {
        // Zwei Zeichnungen A und B vorhanden (z.B. Modul C01_35_075_00001) ->
        // Beide Zeichnungen löschen
        // Getestet wird mit einer Abfrage, die alle Zeichnungen der Baugruppe zurückliefern muss.
        // Wenn die Zeichnungen gelöscht sind soll dann natürlich keine Zeichnung zurückkommen.
        // Der Test prüft hauptsächlich DBAttributesListEtkRecordsWrapper.delete(index)
        AbstractPKChangeTest test = new AbstractPKChangeTest() {
            @Override
            public void doActions(int permutation) {
                boolean useParentObject = false;
                boolean useSecondSequence = false;

                switch (permutation) {
                    case 0:
                        useParentObject = false;
                        useSecondSequence = false;
                        break;
                    case 1:
                        useParentObject = true;
                        useSecondSequence = false;
                        break;
                    case 2:
                        useParentObject = false;
                        useSecondSequence = true;
                        break;
                    case 3:
                        useParentObject = true;
                        useSecondSequence = true;
                        break;
                }

                System.out.println("Executing test testDeleteTwoRecords() with useParentObject=" + useParentObject + " and useSecondSequence="
                                   + useSecondSequence);

                final String moduleNo = moduleNoWithTwoImages;
                DataImageId key1 = new DataImageId(moduleNo, "", "00001");
                DataImageId key2 = new DataImageId(moduleNo, "", "00002");

                String payLoad1 = "B35075000189";
                String payLoad2 = "B35075000190";

                if (useParentObject) {
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""));
                    if (!assembly.existsInDB()) {
                        fail("Assembly: " + moduleNo + " not in DB");
                    }
                    setParentObject(assembly);
                }

                // Ist ohne Änderung das erste Bild des Moduls wie erwartet?
                testExists(key1, payLoad1);
                testExists(key2, payLoad2);

                if (useSecondSequence) {
                    delete(key2, payLoad2);
                    delete(key1, payLoad1);
                } else {
                    delete(key1, payLoad1);
                    delete(key2, payLoad2);
                }

                save();
                testNotExists(key1);
                testNotExists(key2);
            }

            @Override
            protected int getNumberOfPermutations() {
                return 4;
            }

            @Override
            protected EtkDataObject createDataObject() {
                return EtkDataObjectFactory.createDataImage(getProject(), new AssemblyId("", ""), "", "", "");
            }

            @Override
            protected String getPayLoadAttribute() {
                return FIELD_I_IMAGES;
            }

            @Override
            protected DBDataObjectList getObjectsList() {
                if (getParentObject() != null) {
                    return ((EtkDataAssembly)getParentObject()).getUnfilteredImages();
                }
                return new EtkDataImageList();
            }
        };

        test.excecute();
    }

    public void testPKChangesWithInsert() {
        // Keine Zeichnungen vorhanden (z.B. 62E_00_001_00001) -> Zeichnung A hinzufügen + speichern ->
        // Zeichnung A nach Pos 2 schieben + An Pos 1 Zeichnung B einfügen  + speichern
        AbstractPKChangeTest test = new AbstractPKChangeTest() {
            @Override
            public void doActions(int permutation) {
                boolean useParentObject = false;

                switch (permutation) {
                    case 0:
                        useParentObject = false;
                        break;
                    case 1:
                        useParentObject = true;
                        break;
                }

                System.out.println("Executing test testPKChangesWithInsert() with useParentObject=" + useParentObject);

                String moduleNo = moduleNoWithNoImage;
                DataImageId key1 = new DataImageId(moduleNo, "", "KEY1");
                DataImageId key2 = new DataImageId(moduleNo, "", "KEY2");

                if (useParentObject) {
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""));
                    if (!assembly.existsInDB()) {
                        fail("Assembly: " + moduleNo + " not in DB");
                    }
                    setParentObject(assembly);
                }

                insert(key1, "A");
                save();
                testExists(key1, "A");

                move("A", key1, key2, true);
                insert(key1, "B", true);
                save();
                testExists(key2, "A");
                testExists(key1, "B");
            }

            @Override
            protected int getNumberOfPermutations() {
                return 2;
            }

            @Override
            protected EtkDataObject createDataObject() {
                return EtkDataObjectFactory.createDataImage(getProject(), new AssemblyId("", ""), "", "", "");
            }

            @Override
            protected String getPayLoadAttribute() {
                return FIELD_I_IMAGES;
            }

            @Override
            protected DBDataObjectList getObjectsList() {
                if (getParentObject() != null) {
                    return ((EtkDataAssembly)getParentObject()).getUnfilteredImages();
                }
                return new EtkDataImageList();
            }
        };

        test.excecute();
    }

    public void testPKChangesDeleteOldId() {
        // Keine Zeichnungen vorhanden (z.B. 62E_00_001_00001) -> Zeichnung A, B und C hinzufügen + speichern ->
        // Zeichnung A hinter Zeichnung B schieben + speichern ->
        // Zeichnung A nach Position D schieben und alte ID löschen
        AbstractPKChangeTest test = new AbstractPKChangeTest() {
            @Override
            public void doActions(int permutation) {
                boolean useParentObject = false;
                boolean useSecondSequence = false;

                switch (permutation) {
                    case 0:
                        useParentObject = false;
                        useSecondSequence = false;
                        break;
                    case 1:
                        useParentObject = true;
                        useSecondSequence = false;
                        break;
                    case 2:
                        useParentObject = false;
                        useSecondSequence = true;
                        break;
                    case 3:
                        useParentObject = true;
                        useSecondSequence = true;
                        break;
                }

                System.out.println("Executing test testPKChangesDeleteOldId() with useParentObject=" + useParentObject + " and useSecondSequence="
                                   + useSecondSequence);

                String moduleNo = moduleNoWithNoImage;
                DataImageId a = new DataImageId(moduleNo, "", "A");
                DataImageId b = new DataImageId(moduleNo, "", "B");
                DataImageId c = new DataImageId(moduleNo, "", "C");
                DataImageId d = new DataImageId(moduleNo, "", "D");

                if (useParentObject) {
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""));
                    if (!assembly.existsInDB()) {
                        fail("Assembly: " + moduleNo + " not in DB");
                    }
                    setParentObject(assembly);
                }

                insert(a, "A");
                insert(b, "B");
                insert(c, "C");
                save();
                testExists(a, "A");
                testExists(b, "B");
                testExists(c, "C");

                if (useSecondSequence) {
                    move("A", a, b, false);
                    move("B", b, a, false);
                } else {
                    move("B", b, a, false);
                    move("A", a, b, false);
                }
                save();
                testExists(a, "B");
                testExists(b, "A");
                testExists(c, "C");


                move("A", b, d, true);
                save();
                testExists(a, "B");
                testExists(d, "A");
                testExists(c, "C");
                testNotExists(b);
            }

            @Override
            protected int getNumberOfPermutations() {
                return 4;
            }

            @Override
            protected EtkDataObject createDataObject() {
                return EtkDataObjectFactory.createDataImage(getProject(), new AssemblyId("", ""), "", "", "");
            }

            @Override
            protected String getPayLoadAttribute() {
                return FIELD_I_IMAGES;
            }

            @Override
            protected DBDataObjectList getObjectsList() {
                if (getParentObject() != null) {
                    return ((EtkDataAssembly)getParentObject()).getUnfilteredImages();
                }
                return new EtkDataImageList();
            }
        };

        test.excecute();
    }

    public void testPKChangesReplaceExistingId() {
        // Eine Zeichnungen A vorhanden (z.B. Modul SA-M03063) ->
        // Zeichnung A löschen und B mit dem Primärschlüssel von A hinzufügen sowie eine neue Zeichnung C (optional mit Tausch)
        // + speichern - Kontrolle (B, C) ->
        // Zeichnung D hinzufügen + speichern -> Kontrolle (B, C, D) ->
        // Zeichnung B, C und D löschen, wobei die Zeichnung auf Primärschlüssel 1 noch temporär auf einen neuen Primärschlüssel 4 verändert wird + speichern -> Kontrolle (leer)
        AbstractPKChangeTest test = new AbstractPKChangeTest() {
            @Override
            public void doActions(int permutation) {
                boolean useParentObject = false;
                boolean useSecondSequence = false;

                switch (permutation) {
                    case 0:
                        useParentObject = false;
                        useSecondSequence = false;
                        break;
                    case 1:
                        useParentObject = true;
                        useSecondSequence = false;
                        break;
                    case 2:
                        useParentObject = false;
                        useSecondSequence = true;
                        break;
                    case 3:
                        useParentObject = true;
                        useSecondSequence = true;
                        break;
                }

                System.out.println("Executing test testPKChangesReplaceExistingId() with useParentObject=" + useParentObject + " and useSecondSequence="
                                   + useSecondSequence);

                String moduleNo = moduleNoWithOneImage;
                DataImageId keyBefore = new DataImageId(moduleNo, "", "00001");
                DataImageId key2 = new DataImageId(moduleNo, "", "KEY2");
                DataImageId key3 = new DataImageId(moduleNo, "", "KEY3");
                DataImageId key4 = new DataImageId(moduleNo, "", "KEY4");
                String payLoad1 = "B";
                String payLoad2 = "C";
                String payLoad3 = "D";

                String payLoadBefore = "B73060000179";

                if (useParentObject) {
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""));
                    if (!assembly.existsInDB()) {
                        fail("Assembly: " + moduleNo + " not in DB");
                    }
                    setParentObject(assembly);
                }

                // Ist ohne Änderung das erste Bild des Moduls wie erwartet?
                testExists(keyBefore, payLoadBefore);

                delete(keyBefore, payLoadBefore);
                insert(keyBefore, payLoad1, true);
                insert(key2, payLoad2);

                if (useSecondSequence) {
                    move(payLoad1, keyBefore, key2, false);
                    move(payLoad2, key2, keyBefore, false);
                }

                save();
                if (useSecondSequence) {
                    testExists(keyBefore, payLoad2);
                    testExists(key2, payLoad1);
                } else {
                    testExists(keyBefore, payLoad1);
                    testExists(key2, payLoad2);
                }

                insert(key3, payLoad3);

                save();
                if (useSecondSequence) {
                    testExists(keyBefore, payLoad2);
                    testExists(key2, payLoad1);
                } else {
                    testExists(keyBefore, payLoad1);
                    testExists(key2, payLoad2);
                }
                testExists(key3, payLoad3);

                if (useSecondSequence) {
                    delete(key3, payLoad3);
                    delete(key2, payLoad1);
                    move(payLoad2, keyBefore, key4, true);
                } else {
                    move(payLoad1, keyBefore, key4, true);
                    delete(key4, payLoad1);
                    delete(key2, payLoad2);
                    delete(key3, payLoad3);
                }

                save();

                if (useSecondSequence) {
                    delete(key4, payLoad2);
                    save();
                }

                testNotExists(keyBefore);
                testNotExists(key2);
                testNotExists(key3);
                testNotExists(key4);
            }

            @Override
            protected int getNumberOfPermutations() {
                return 4;
            }

            @Override
            protected EtkDataObject createDataObject() {
                return EtkDataObjectFactory.createDataImage(getProject(), new AssemblyId("", ""), "", "", "");
            }

            @Override
            protected String getPayLoadAttribute() {
                return FIELD_I_IMAGES;
            }

            @Override
            protected DBDataObjectList getObjectsList() {
                if (getParentObject() != null) {
                    return ((EtkDataAssembly)getParentObject()).getUnfilteredImages();
                }
                return new EtkDataImageList();
            }
        };

        test.excecute();
    }

    public void testStateCommitted() {
        final EtkProject project = getProject();
        iPartsDialogId dialogId = new iPartsDialogId("testStateCommitted");

        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper();
        try {
            // DA_DIALOG-Datensatz vorbereiten
            iPartsDataDialogData dataDialogData = new iPartsDataDialogData(project, dialogId);
            if (!dataDialogData.existsInDB()) {
                dataDialogData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            dataDialogData.setFieldValue(FIELD_DD_DOCU_RELEVANT, "", DBActionOrigin.FROM_EDIT);
            dataDialogData.saveToDB();

            // Leeren Inhalt vom DA_DIALOG-Datensatz überprüfen
            dataDialogData = new iPartsDataDialogData(project, dialogId);
            if (!dataDialogData.existsInDB()) {
                fail("DA_DIALOG dataset doesn't exist");
            }
            assertEquals("", dataDialogData.getFieldValue(FIELD_DD_DOCU_RELEVANT));

            // DA_DIALOG-Datensatz verändern
            final iPartsDataDialogData committedDataDialogData = new iPartsDataDialogData(project, dialogId);
            if (!committedDataDialogData.existsInDB()) {
                fail("DA_DIALOG dataset doesn't exist");
            }
            committedDataDialogData.setFieldValue(FIELD_DD_DOCU_RELEVANT, iPartsDocuRelevant.DOCU_RELEVANT_YES.getDbValue(), DBActionOrigin.FROM_EDIT);

            // Speichern im ChangeSet mit Zustand COMMITTED
            final EtkDbObjectsLayer dbLayer = project.getDbLayer();
            dbLayer.startTransaction();
            dbLayer.startBatchStatement();

            GenericEtkDataObjectList genericList = new GenericEtkDataObjectList();
            genericList.add(committedDataDialogData, DBActionOrigin.FROM_EDIT);
            AbstractRevisionChangeSet changeSetForEdit = revisionsHelper.getActiveRevisionChangeSetForEdit();
            changeSetForEdit.addDataObjectListCommitted(genericList);

            // Daten direkt in der DB ohne aktive ChangeSets speichern
            project.executeWithoutActiveChangeSets(new Runnable() {
                @Override
                public void run() {
                    try {
                        committedDataDialogData.saveToDB();
                        dbLayer.endBatchStatement();
                        dbLayer.commit();
                    } catch (Exception e) {
                        dbLayer.cancelBatchStatement();
                        dbLayer.rollback();
                        throw e;
                    }
                }
            }, false);

            // ChangeSet-Inhalt überprüfen
            Map serializedDataObjectsMap = changeSetForEdit.getSerializedDataObjectsMap();
            assertEquals(1, serializedDataObjectsMap.size());
            SerializedDBDataObject serializedDBDataObject = (SerializedDBDataObject)serializedDataObjectsMap.values().iterator().next();
            assertEquals(dialogId, serializedDBDataObject.createId());
            assertEquals(SerializedDBDataObjectState.COMMITTED, serializedDBDataObject.getState());
            Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
            assertEquals(1, attributes.size());
            SerializedDBDataObjectAttribute attribute = attributes.iterator().next();
            assertEquals(FIELD_DD_DOCU_RELEVANT, attribute.getName());
            assertEquals(iPartsDocuRelevant.DOCU_RELEVANT_YES.getDbValue(), attribute.getValue());

            // Inhalt vom DA_DIALOG-Datensatz überprüfen
            iPartsDataDialogData modifiedDataDialogData = new iPartsDataDialogData(project, dialogId);
            if (!modifiedDataDialogData.existsInDB()) {
                fail("DA_DIALOG dataset doesn't exist");
            }
            assertEquals(iPartsDocuRelevant.DOCU_RELEVANT_YES.getDbValue(), modifiedDataDialogData.getFieldValue(FIELD_DD_DOCU_RELEVANT));


            // Inhalt vom DA_DIALOG-Datensatz OHNE akives ChangeSet überprüfen (muss identisch sein, weil die Änderung ja
            // direkt in der DB erfolgt ist)
            revisionsHelper.clearActiveRevisionChangeSets(project, false);
            modifiedDataDialogData = new iPartsDataDialogData(project, dialogId);
            if (!modifiedDataDialogData.existsInDB()) {
                fail("DA_DIALOG dataset doesn't exist");
            }
            assertEquals(iPartsDocuRelevant.DOCU_RELEVANT_YES.getDbValue(), modifiedDataDialogData.getFieldValue(FIELD_DD_DOCU_RELEVANT));
            revisionsHelper.setActiveRevisionChangeSet(changeSetForEdit, project);

            // DA_DIALOG-Datensatz direkt in der DB erneut verändern, um zu überprüfen, ob der COMMITTED-Wert aus dem
            // ChangeSet auch tatsächlich nicht simuliert wird
            modifiedDataDialogData.setFieldValue(FIELD_DD_DOCU_RELEVANT, iPartsDocuRelevant.DOCU_RELEVANT_NO.getDbValue(), DBActionOrigin.FROM_EDIT);
            modifiedDataDialogData.saveToDB();

            // Inhalt vom DA_DIALOG-Datensatz erneut überprüfen (muss DOCU_RELEVANT_NO enthalten und NICHT DOCU_RELEVANT_YES
            // aus dem ChangeSet, weil dort Zustand COMMITTED)
            modifiedDataDialogData = new iPartsDataDialogData(project, dialogId);
            if (!modifiedDataDialogData.existsInDB()) {
                fail("DA_DIALOG dataset doesn't exist");
            }
            assertEquals(iPartsDocuRelevant.DOCU_RELEVANT_NO.getDbValue(), modifiedDataDialogData.getFieldValue(FIELD_DD_DOCU_RELEVANT));

            // ... und nochmal ohne aktives Changeset
            revisionsHelper.clearActiveRevisionChangeSets(project, false);
            modifiedDataDialogData = new iPartsDataDialogData(project, dialogId);
            if (!modifiedDataDialogData.existsInDB()) {
                fail("DA_DIALOG dataset doesn't exist");
            }
            assertEquals(iPartsDocuRelevant.DOCU_RELEVANT_NO.getDbValue(), modifiedDataDialogData.getFieldValue(FIELD_DD_DOCU_RELEVANT));

            // Test-Datensatz mit Zustand DELETED_COMMITTED wieder löschen
            revisionsHelper.setActiveRevisionChangeSet(changeSetForEdit, project);
            final iPartsDataDialogData toBeDeletedDataDialogData = new iPartsDataDialogData(project, dialogId);
            dbLayer.startTransaction();
            dbLayer.startBatchStatement();
            genericList = new GenericEtkDataObjectList();
            genericList.delete(toBeDeletedDataDialogData, true, DBActionOrigin.FROM_EDIT);
            changeSetForEdit.addDataObjectListCommitted(genericList);

            // Daten direkt in der DB ohne aktive ChangeSets speichern
            project.executeWithoutActiveChangeSets(new Runnable() {
                @Override
                public void run() {
                    try {
                        toBeDeletedDataDialogData.deleteFromDB();
                        dbLayer.endBatchStatement();
                        dbLayer.commit();
                    } catch (Exception e) {
                        dbLayer.cancelBatchStatement();
                        dbLayer.rollback();
                        throw e;
                    }
                }
            }, false);

            // ChangeSet-Inhalt überprüfen
            serializedDataObjectsMap = changeSetForEdit.getSerializedDataObjectsMap();
            assertEquals(1, serializedDataObjectsMap.size());
            serializedDBDataObject = (SerializedDBDataObject)serializedDataObjectsMap.values().iterator().next();
            assertEquals(dialogId, serializedDBDataObject.createId());
            assertEquals(SerializedDBDataObjectState.DELETED_COMMITTED, serializedDBDataObject.getState());

            // Gelöschten DA_DIALOG-Datensatz mit akivem ChangeSet überprüfen
            iPartsDataDialogData deletedDataDialogData = new iPartsDataDialogData(project, dialogId);
            if (deletedDataDialogData.existsInDB()) {
                fail("DA_DIALOG dataset must not exist");
            }

            // Gelöschten DA_DIALOG-Datensatz OHNE akives ChangeSet überprüfen (muss identisch sein, weil die Änderung ja
            // direkt in der DB erfolgt ist)
            revisionsHelper.clearActiveRevisionChangeSets(project, false);
            deletedDataDialogData = new iPartsDataDialogData(project, dialogId);
            if (deletedDataDialogData.existsInDB()) {
                fail("DA_DIALOG dataset must not exist");
            }
        } finally {
            finalizeRevisionHelper(revisionsHelper);

            // Test-Datensatz wieder löschen
            iPartsDataDialogData dataDialogData = new iPartsDataDialogData(project, dialogId);
            dataDialogData.deleteFromDB(true);
        }
    }

    /**
     * Reservierung von Primärschlüsseln über ChangeSets hinweg testen
     */
    public void testReservedPrimaryKeys() {
        System.out.println("testReservedPrimaryKeys()");
        EtkProject project = getProject();

        PartListEntryId partListEntryId = new PartListEntryId("Test", "", "00001");
        iPartsDataReplacePart dataReplacePart = null;
        iPartsDataReplacePart newDataReplacePart = null;
        try {
            // 1. ChangeSet erzeugen und aktivieren
            CHANGESETID = "UNITTESTCHANGESETS";
            EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
            try {
                // Ersetzung im Retail anlegen
                iPartsReplacePartId replacePartId = new iPartsReplacePartId(partListEntryId, EtkDbsHelper.formatLfdNr(1));
                dataReplacePart = new iPartsDataReplacePart(project, replacePartId);
                dataReplacePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                dataReplacePart.saveToDB();

                // Nächste freie Sequenznummer muss 2 sein
                String nextSeqNo = iPartsReplacementHelper.getNextReplacementSeqNo(project, partListEntryId);
                assertEquals(EtkDbsHelper.formatLfdNr(2), nextSeqNo);
                iPartsReplacePartId newReplacePartId = new iPartsReplacePartId(partListEntryId, nextSeqNo);

                // PK-Reservierung in der DB prüfen
                iPartsDataReservedPK dataReservedPK = new iPartsDataReservedPK(project, new iPartsReservedPKId(newReplacePartId));
                assertTrue(dataReservedPK.existsInDB());
                assertEquals(revisionsHelper.getActiveRevisionChangeSetForEdit().getChangeSetId().getGUID(), dataReservedPK.getChangeSetId());
            } finally {
                finalizeRevisionHelper(revisionsHelper);
            }

            EtkDbsCacheElems.clearAllCaches(); // Sonst würde die lfdNr evtl. schon über den Cache hochgezählt werden

            // 2. ChangeSet erzeugen und aktivieren
            CHANGESETID = "UNITTESTCHANGESETS2";
            revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
            try {
                // Nächste freie Sequenznummer muss 3 sein
                String nextSeqNo = iPartsReplacementHelper.getNextReplacementSeqNo(project, partListEntryId);
                assertEquals(EtkDbsHelper.formatLfdNr(3), nextSeqNo);
                iPartsReplacePartId newReplacePartId = new iPartsReplacePartId(partListEntryId, nextSeqNo);

                // Neue Ersetzung erzeugen und zum ChangeSet hinzufügen (damit dieses nicht leer ist)
                newDataReplacePart = new iPartsDataReplacePart(project, newReplacePartId);
                newDataReplacePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                project.addDataObjectToActiveChangeSetForEdit(newDataReplacePart);

                // PK-Reservierung in der DB prüfen
                iPartsDataReservedPK dataReservedPK = new iPartsDataReservedPK(project, new iPartsReservedPKId(newReplacePartId));
                assertTrue(dataReservedPK.existsInDB());
                assertEquals(revisionsHelper.getActiveRevisionChangeSetForEdit().getChangeSetId().getGUID(), dataReservedPK.getChangeSetId());

                // Autoren-Auftrag freigeben
                AbstractRevisionChangeSet editChangeSet = project.getRevisionsHelper().getActiveRevisionChangeSetForEdit();
                revisionsHelper.setActiveRevisionChangeSet(null, getProject());
                editChangeSet.commit();

                // PK-Reservierung muss aus der DB gelöscht worden sein
                dataReservedPK = new iPartsDataReservedPK(project, new iPartsReservedPKId(newReplacePartId));
                assertFalse(dataReservedPK.existsInDB());

                // Ersetzunge muss dafür in der DB sein
                newDataReplacePart = new iPartsDataReplacePart(project, newReplacePartId);
                assertTrue(newDataReplacePart.existsInDB());

                EtkDbsCacheElems.clearAllCaches(); // Sonst würde die lfdNr evtl. schon über den Cache hochgezählt werden

                // Nächste freie Sequenznummer müsste 4 sein
                nextSeqNo = iPartsReplacementHelper.getNextReplacementSeqNo(project, partListEntryId);
                assertEquals(EtkDbsHelper.formatLfdNr(4), nextSeqNo);
            } finally {
                finalizeRevisionHelper(revisionsHelper);
            }
        } finally {
            CHANGESETID = "UNITTESTCHANGESETS";
            iPartsDataReservedPKList.deletePrimaryKeysForChangeSet(project, getChangeSetId());
            dataReplacePart.deleteFromDB(true);
            newDataReplacePart.deleteFromDB(true);
        }
    }


    // Ab hier Tests für die Simulation von Joins mittels getRecords()
    private AbstractGetJSONFromDataObjectList createGetJSONForTestSimpleJoin(String whereValue, boolean isLeftOuterJoin) {
        return new AbstractGetJSONFromDataObjectList() {
            @Override
            protected EtkDisplayFields createSelectFields() {
                EtkDisplayFields selectFields = new EtkDisplayFields();
                selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false));
                selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_GUID, false, false));
                selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_BESTFLAG, false, false));
                selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_RFMEA, false, false));
                return selectFields;
            }

            @Override
            protected EtkDataObjectList createDataObjectList() {
                // Join von KATALOG-Tabelle auf DA_REPLACE_PART-Tabelle
                EtkDataPartListEntryList dataObjectList = new EtkDataPartListEntryList();
                dataObjectList.searchSortAndFillWithJoin(getProject(), null, getSelectFields(), new String[]{ FIELD_K_VARI },
                                                         TABLE_DA_REPLACE_PART, new String[]{ FIELD_DRP_VARI }, isLeftOuterJoin, false,
                                                         new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_MATNR),
                                                                       TableAndFieldName.make(TABLE_KATALOG, FIELD_K_BESTFLAG) },
                                                         new String[]{ whereValue, SQLStringConvert.booleanToPPString(true) }, false,
                                                         new String[]{ FIELD_K_SOURCE_GUID, TableAndFieldName.make(TABLE_DA_REPLACE_PART, FIELD_DRP_SEQNO) },
                                                         false);
                return dataObjectList;
            }
        };
    }

    public void testSimpleInnerJoinForNewDataObjects() {
        internalTestSimpleJoinForNewDataObjects(false);
    }

    public void internalTestSimpleJoinForNewDataObjects(boolean isLeftOuterJoin) {
        // Neuer Stücklisteneintrag mit Ersetzungen, der zur where-Bedingung passt -> anderes Ergebnis mit Changeset
        internalTestSimpleJoinForNewDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", true, true, true);

        // Neuer Stücklisteneintrag mit Ersetzungen, der aber nicht zur where-Bedingung passt (diese findet gar keine Datensätze
        // für den Join) -> gleiches Ergebnis wie ohne ChangeSet
        internalTestSimpleJoinForNewDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", true, false, true);

        // Neuer Stücklisteneintrag mit Ersetzungen, der aber nicht zur where-Bedingung passt (diese findet aber andere
        // Datensätze für die Materialnummer A1233260926 in der DB) -> gleiches Ergebnis wie ohne ChangeSet
        internalTestSimpleJoinForNewDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", true, false, true);

        // Neuer Stücklisteneintrag mit Ersetzungen, der zur where-Bedingung passt -> nur bei isLeftOuterJoin anderes Ergebnis mit Changeset
        internalTestSimpleJoinForNewDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", false, isLeftOuterJoin, true);

        // Neuer Stücklisteneintrag mit Ersetzungen, der aber nicht zur where-Bedingung passt (diese findet gar keine Datensätze
        // für den Join) -> gleiches Ergebnis wie ohne ChangeSet
        internalTestSimpleJoinForNewDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", false, false, true);

        // Neuer Stücklisteneintrag mit Ersetzungen, der aber nicht zur where-Bedingung passt (diese findet aber andere
        // Datensätze für die Materialnummer A1233260926 in der DB) -> gleiches Ergebnis wie ohne ChangeSet
        internalTestSimpleJoinForNewDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", false, false, true);
    }

    private void internalTestSimpleJoinForNewDataObjects(boolean isLeftOuterJoin, String matNr, String whereValue,
                                                         boolean addReplacements, boolean changeSetMustHaveDifferentJoinResult,
                                                         boolean withPseudoTransactionComparison) {
        AbstractGetJSONFromDataObjectList resultJSON = createGetJSONForTestSimpleJoin(whereValue, isLeftOuterJoin);

        // Neuen Stücklisteneintrag zunächst aus der DB löschen falls vorhanden
        EtkProject project = getProject();
        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("Test123",
                                                                                                                       "", "00001"));
        if (partListEntry.existsInDB()) {
            partListEntry.deleteFromDB(true);
        }

        // Zwei neue passende Ersetzungen und eine nicht passende Ersetzung zunächst aus der DB löschen falls vorhanden
        iPartsDataReplacePart replacement = prepareReplacementDOForJoinSim(partListEntry.getAsId(), project, "00001");
        iPartsDataReplacePart replacement2 = prepareReplacementDOForJoinSim(partListEntry.getAsId(), project, "00002");
        iPartsDataReplacePart irrelevantReplacement = prepareReplacementDOForJoinSim(new PartListEntryId("Test456", "", "00001"),
                                                                                     project, "00001");

        // Join ohne ChangeSet durchführen
        String resultWithoutChangeSet = resultJSON.getJSON();

        EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
        try {
            // Test für einen neuen Stücklisteneintrag und zwei neue passende Ersetzungen sowie eine nicht passende Ersetzung
            // mit dazugehörigem Join
            if (addReplacements) {
                addNewReplacementToChangeSetForJoinSim(replacement, revisionsHelper, "RFMEA_foo");
                addNewReplacementToChangeSetForJoinSim(replacement2, revisionsHelper, "RFMEA_bla");
                addNewReplacementToChangeSetForJoinSim(irrelevantReplacement, revisionsHelper, "RFMEA_irr");
            }

            partListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            partListEntry.setFieldValue(FIELD_K_MATNR, matNr, DBActionOrigin.FROM_EDIT); // A1233260926
            partListEntry.setFieldValue(FIELD_K_SOURCE_GUID, "SOURCE_GUID_foo", DBActionOrigin.FROM_EDIT);
            partListEntry.setFieldValueAsBoolean(FIELD_K_BESTFLAG, true, DBActionOrigin.FROM_EDIT);
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(partListEntry);

            if (withPseudoTransactionComparison) {
                EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
            }
            String resultWithChangeSetGetRecords;
            try {
                // Join mit ChangeSet durchführen
                resultWithChangeSetGetRecords = resultJSON.getJSON();
            } finally {
                if (withPseudoTransactionComparison) {
                    EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
                }
            }

            assertFalse(resultWithoutChangeSet.contains("Test123"));
            assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_irr"));

            // Test123 und dessen Daten müssen mit den Änderungen aus dem ChangeSet im Ergebnis vorhanden sein und vorher nicht
            if (changeSetMustHaveDifferentJoinResult) {
                assertFalse(resultWithoutChangeSet.equals(resultWithChangeSetGetRecords));
                assertTrue(resultWithChangeSetGetRecords.contains("Test123"));
                if (addReplacements) {
                    assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_foo"));
                    assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_bla"));
                } else {
                    assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_foo"));
                    assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_bla"));
                }
                assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
            } else {
                assertEquals(resultWithoutChangeSet, resultWithChangeSetGetRecords);
                assertFalse(resultWithChangeSetGetRecords.contains("Test123"));
                assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_foo"));
                assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_bla"));
                assertFalse(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
            }

            if (withPseudoTransactionComparison) {
                // Ergebnis muss ohne und mit Pseudo-Transaktion identisch sein
                String resultWithChangeSetPseudoTransaction = resultJSON.getJSON();
                assertEquals(resultWithChangeSetPseudoTransaction, resultWithChangeSetGetRecords);
            }
        } finally {
            finalizeRevisionHelper(revisionsHelper);
            partListEntry.deleteFromDB(true);
            replacement.deleteFromDB(true);
            replacement2.deleteFromDB(true);
            irrelevantReplacement.deleteFromDB(true);
        }
    }

    private void addNewReplacementToChangeSetForJoinSim(iPartsDataReplacePart replacement, EtkRevisionsHelper revisionsHelper,
                                                        String rfmea_foo) {
        replacement.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        replacement.setFieldValue(FIELD_DRP_REPLACE_RFMEA, rfmea_foo, DBActionOrigin.FROM_EDIT);
        revisionsHelper.addDataObjectToActiveChangeSetForEdit(replacement);
    }

    private iPartsDataReplacePart prepareReplacementDOForJoinSim(PartListEntryId partListEntryId, EtkProject project, String rSeqNo) {
        iPartsDataReplacePart replacement = new iPartsDataReplacePart(project, new iPartsReplacePartId(partListEntryId, rSeqNo));
        if (replacement.existsInDB()) {
            replacement.deleteFromDB(true);
        }
        return replacement;
    }

    // Nur für Performance-Tests und nicht relevant für den normalen Unittest-Betrieb
    public void _testPerformanceOfSimpleInnerJoinForNewDataObjects() {
        Logger.getLogger().removeChannel(EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION);
        try {
            int loopCount = 100;

            // Mit Pseudo-Transaktionen
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < loopCount; i++) {
                internalTestSimpleJoinForNewDataObjects(false, "A1233260926", "A1233260926", true, true, false);
            }
            System.out.println("Duration for " + loopCount + " times simulating a simple join for new dataObjects with pseudo transactions: "
                               + DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false,
                                                                    Language.DE.getCode()));

            // Mit getRecords()-Simulation
            EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
            try {
                startTime = System.currentTimeMillis();
                for (int i = 0; i < loopCount; i++) {
                    internalTestSimpleJoinForNewDataObjects(false, "A1233260926", "A1233260926", true, true, false);
                }
                System.out.println("Duration for " + loopCount + " times simulating a simple join for new dataObjects with getRecords(): "
                                   + DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false,
                                                                        Language.DE.getCode()));
            } finally {
                EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
            }
        } finally {
            Logger.getLogger().addChannel(EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION);
        }
    }

    public void testSimpleInnerJoinForModifiedDataObjects() {
        internalTestSimpleJoinForModifiedDataObjects(false);
    }

    public void internalTestSimpleJoinForModifiedDataObjects(boolean isLeftOuterJoin) {
        // Veränderter Stücklisteneintrag mit Ersetzungen -> anderes Ergebnis mit Changeset
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", true, false, false, true, true);

        // Veränderter Stücklisteneintrag mit Ersetzungen, der aber nicht zur where-Bedingung passt (diese findet gar keine Datensätze
        // für den Join) -> gleiches Ergebnis wie ohne ChangeSet
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", true, false, false, false, true);

        // Veränderter Stücklisteneintrag mit Ersetzungen, der aber nicht zur where-Bedingung passt (diese findet aber andere
        // Datensätze für die Materialnummer A1233260926 in der DB) -> gleiches Ergebnis wie ohne ChangeSet
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", true, false, false, false, true);

        // Tests wie oben nur mit geänderten Ersetzungen
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", false, true, false, true, true);
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", false, true, false, false, true);
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", false, true, false, false, true);

        // Tests wie oben nur mit geändertem Stücklisteneintrag und geänderten Ersetzungen
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", true, true, false, true, true);
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", true, true, false, false, true);
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", true, true, false, false, true);

        // Tests wie oben nur mit veränderter irrelevanter Ersetzung -> gleiches Ergebnis wie ohne ChangeSet außer beim zweiten Test
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", false, false, true, false, true);
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", false, false, true, true, true);
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", false, false, true, false, true);

        // Tests wie oben nur mit verändertem Stücklisteneintrag, Ersetzungen und irrelevanten DataObjects -> nur beim dritten
        // Test gleiches Ergebnis
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", true, true, true, true, true);
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", true, true, true, true, true);
        internalTestSimpleJoinForModifiedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", true, true, true, false, true);
    }

    private void internalTestSimpleJoinForModifiedDataObjects(boolean isLeftOuterJoin, String matNr, String whereValue,
                                                              boolean modifyPartListEntry, boolean modifyRelevantReplacements,
                                                              boolean modifyIrrelevantDataObjects, boolean changeSetMustHaveDifferentJoinResult,
                                                              boolean withPseudoTransactionComparison) {
        AbstractGetJSONFromDataObjectList resultJSON = createGetJSONForTestSimpleJoin(whereValue, isLeftOuterJoin);

        // Neuen Stücklisteneintrag in der DB anlegen
        EtkProject project = getProject();
        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("Test123",
                                                                                                                       "", "00001"));
        EtkDataPartListEntry irrelevantPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("Test456",
                                                                                                                                 "", "00001"));
        iPartsDataReplacePart replacement = prepareReplacementDOForJoinSim(partListEntry.getAsId(), project, "00001");
        iPartsDataReplacePart replacement2 = prepareReplacementDOForJoinSim(partListEntry.getAsId(), project, "00002");
        iPartsDataReplacePart irrelevantReplacement = prepareReplacementDOForJoinSim(irrelevantPartListEntry.getAsId(),
                                                                                     project, "00001");

        try {
            // Zwei neue Stücklisteneinträge in der DB anlegen
            addPartListEntrytoDBForJoinSim(partListEntry, matNr, "SOURCE_GUID_foo");
            addPartListEntrytoDBForJoinSim(irrelevantPartListEntry, "A1233260927", "SOURCE_GUID_irr");

            // Zwei neue passende Ersetzungen und eine nicht passende Ersetzung in der DB anlegen
            addNewReplacementToDBForJoinSim(replacement, "RFMEA_foo");
            addNewReplacementToDBForJoinSim(replacement2, "RFMEA_bla");
            addNewReplacementToDBForJoinSim(irrelevantReplacement, "RFMEA_irr");

            // Join ohne ChangeSet durchführen
            String resultWithoutChangeSet = resultJSON.getJSON();

            EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
            try {
                // Test für das Verändern vom Stücklisteneintrag und/oder Ersetzungen mit dazugehörigem Join
                if (modifyPartListEntry) {
                    partListEntry.setFieldValue(FIELD_K_SOURCE_GUID, "SOURCE_GUID_modified", DBActionOrigin.FROM_EDIT);
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(partListEntry);
                }
                if (modifyRelevantReplacements) {
                    replacement.setFieldValue(FIELD_DRP_REPLACE_RFMEA, "RFMEA_mod", DBActionOrigin.FROM_EDIT);
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(replacement);
                }
                if (modifyIrrelevantDataObjects) {
                    irrelevantPartListEntry.setFieldValue(FIELD_K_SOURCE_GUID, "SOURCE_GUID_imod", DBActionOrigin.FROM_EDIT);
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(irrelevantPartListEntry);

                    irrelevantReplacement.setFieldValue(FIELD_DRP_REPLACE_RFMEA, "RFMEA_imod", DBActionOrigin.FROM_EDIT);
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(irrelevantReplacement);
                }

                if (withPseudoTransactionComparison) {
                    EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
                }
                String resultWithChangeSetGetRecords;
                try {
                    // Join mit ChangeSet durchführen
                    resultWithChangeSetGetRecords = resultJSON.getJSON();
                } finally {
                    if (withPseudoTransactionComparison) {
                        EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
                    }
                }

                if (matNr.equals(whereValue)) {
                    assertTrue(resultWithoutChangeSet.contains("Test123"));
                }

                // Der Feldwert für K_SOURCE_GUID von Test123 muss mit den Änderungen aus dem ChangeSet ein anderer sein als vorher
                if (changeSetMustHaveDifferentJoinResult) {
                    assertFalse(resultWithoutChangeSet.equals(resultWithChangeSetGetRecords));
                    if (matNr.equals(whereValue)) {
                        if (modifyPartListEntry) {
                            assertFalse(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
                            assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_modified"));
                        } else {
                            assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
                            assertFalse(resultWithChangeSetGetRecords.contains("SOURCE_GUID_modified"));
                        }
                        if (modifyRelevantReplacements) {
                            assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_foo"));
                            assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_mod"));
                        } else {
                            assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_foo"));
                            assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_mod"));
                        }
                    } else {
                        assertFalse(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
                        assertFalse(resultWithChangeSetGetRecords.contains("SOURCE_GUID_modified"));
                        assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_foo"));
                        assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_mod"));
                    }
                } else {
                    assertEquals(resultWithoutChangeSet, resultWithChangeSetGetRecords);
                    if (matNr.equals(whereValue)) {
                        assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
                    }
                    assertFalse(resultWithChangeSetGetRecords.contains("SOURCE_GUID_modified"));
                    assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_mod"));
                }

                // Test456 und dessen Daten müssen mit den Änderungen aus dem ChangeSet im Ergebnis vorhanden sein, wenn
                // nach dessen Materialnummer A1233260927 gesucht wird und die irrelevanten DataObjects nicht im ChangeSet
                // verändert wurden
                if (whereValue.equals("A1233260927")) {
                    assertTrue(resultWithoutChangeSet.contains("Test456"));
                    assertTrue(resultWithChangeSetGetRecords.contains("Test456"));
                    if (modifyIrrelevantDataObjects) {
                        assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_imod"));
                        assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_imod"));
                    } else {
                        assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_irr"));
                        assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_irr"));
                    }
                }

                if (withPseudoTransactionComparison) {
                    // Ergebnis muss ohne und mit Pseudo-Transaktion identisch sein
                    String resultWithChangeSetPseudoTransaction = resultJSON.getJSON();
                    assertEquals(resultWithChangeSetPseudoTransaction, resultWithChangeSetGetRecords);
                }
            } finally {
                finalizeRevisionHelper(revisionsHelper);
            }
        } finally {
            partListEntry.deleteFromDB(true);
            irrelevantPartListEntry.deleteFromDB(true);
            replacement.deleteFromDB(true);
            replacement2.deleteFromDB(true);
            irrelevantReplacement.deleteFromDB(true);
        }
    }

    private void addPartListEntrytoDBForJoinSim(EtkDataPartListEntry partListEntry, String matNr, String sourceGUID) {
        if (!partListEntry.existsInDB()) {
            partListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        partListEntry.setFieldValue(FIELD_K_MATNR, matNr, DBActionOrigin.FROM_EDIT);
        partListEntry.setFieldValue(FIELD_K_SOURCE_GUID, sourceGUID, DBActionOrigin.FROM_EDIT);
        partListEntry.setFieldValueAsBoolean(FIELD_K_BESTFLAG, true, DBActionOrigin.FROM_EDIT);
        partListEntry.saveToDB();
    }

    private void addNewReplacementToDBForJoinSim(iPartsDataReplacePart replacement, String rfmea_foo) {
        if (!replacement.existsInDB()) {
            replacement.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        replacement.setFieldValue(FIELD_DRP_REPLACE_RFMEA, rfmea_foo, DBActionOrigin.FROM_EDIT);
        replacement.saveToDB();
    }


    // Nur für Performance-Tests und nicht relevant für den normalen Unittest-Betrieb
    public void _testPerformanceOfSimpleJoinForModifiedDataObjects() {
        Logger.getLogger().removeChannel(EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION);
        try {
            int loopCount = 100;

            // Mit Pseudo-Transaktionen
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < loopCount; i++) {
                internalTestSimpleJoinForModifiedDataObjects(false, "A1233260926", "A1233260926", true, false, false, true, false);
            }
            System.out.println("Duration for " + loopCount + " times simulating a simple join for modified dataObjects with pseudo transactions: "
                               + DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false,
                                                                    Language.DE.getCode()));

            // Mit getRecords()-Simulation
            EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
            try {
                startTime = System.currentTimeMillis();
                for (int i = 0; i < loopCount; i++) {
                    internalTestSimpleJoinForModifiedDataObjects(false, "A1233260926", "A1233260926", true, false, false, true, false);
                }
                System.out.println("Duration for " + loopCount + " times simulating a simple join for modified dataObjects with getRecords(): "
                                   + DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false,
                                                                        Language.DE.getCode()));
            } finally {
                EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
            }
        } finally {
            Logger.getLogger().addChannel(EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION);
        }
    }

    public void testSimpleInnerJoinForModifiedDataObjectsForDifferentResultSet() {
        internalTestSimpleJoinForModifiedDataObjectsForDifferentResultSet(false);
    }

    public void internalTestSimpleJoinForModifiedDataObjectsForDifferentResultSet(boolean isLeftOuterJoin) {
        // Veränderter Stücklisteneintrag mit Ersetzungen, der nach der Veränderung nicht mehr zur where-Bedingung passt
        // -> anderes Ergebnis mit Changeset
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260926",
                                                                           true, false, false, true, true);

        // Veränderter Stücklisteneintrag mit Ersetzungen, der nach der Veränderung zur where-Bedingung passt -> anderes Ergebnis mit Changeset
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260927",
                                                                           true, false, false, true, true);

        // Veränderter Stücklisteneintrag mit Ersetzungen, der aber nicht zur where-Bedingung passt (diese findet aber andere
        // Datensätze für die Materialnummer A1233260926 in der DB) -> gleiches Ergebnis wie ohne ChangeSet
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260927", "A1233260926",
                                                                           true, false, false, false, true);

        // Tests wie oben nur mit veänderten Ersetzungen
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260926",
                                                                           false, true, false, true, true);
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260927",
                                                                           false, true, false, true, true);
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260927", "A1233260926",
                                                                           false, true, false, false, true);

        // Tests wie oben nur mit veändertem Stücklisteneintrag und veänderten Ersetzungen
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260926",
                                                                           true, true, false, true, true);
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260927",
                                                                           true, true, false, true, true);
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260927", "A1233260926",
                                                                           true, true, false, false, true);

        // Tests wie oben nur mit veränderter irrelevanter Ersetzung -> gleiches Ergebnis wie ohne ChangeSet außer beim zweiten Test
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260926",
                                                                           false, false, true, false, true);
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260927",
                                                                           false, false, true, true, true);
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260927", "A1233260926",
                                                                           false, false, true, false, true);

        // Tests wie oben nur mit verändertem Stücklisteneintrag, Ersetzungen und irrelevanten DataObjects -> in allen
        // Fällen anderes Ergebnis mit Changeset
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260926",
                                                                           true, true, true, true, true);
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260926", "A1233260927",
                                                                           true, true, true, true, true);
        internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(isLeftOuterJoin, "A1233260927", "A1233260926",
                                                                           true, true, true, true, true);
    }

    private void internalTestSimpleJoinForModifiedDataObjectsWithDifferentResultSet(boolean isLeftOuterJoin, String matNr, String whereValue,
                                                                                    boolean modifyPartListEntry,
                                                                                    boolean modifyRelevantReplacements,
                                                                                    boolean modifyIrrelevantDataObjects,
                                                                                    boolean changeSetMustHaveDifferentJoinResult,
                                                                                    boolean withPseudoTransactionComparison) {
        AbstractGetJSONFromDataObjectList resultJSON = createGetJSONForTestSimpleJoin(whereValue, isLeftOuterJoin);

        // Neuen Stücklisteneintrag in der DB anlegen
        EtkProject project = getProject();
        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("Test123",
                                                                                                                       "", "00001"));
        EtkDataPartListEntry invalidPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("TestInvalid",
                                                                                                                              "", "00001"));
        EtkDataPartListEntry irrelevantPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("Test456",
                                                                                                                                 "", "00001"));
        iPartsDataReplacePart replacement = prepareReplacementDOForJoinSim(partListEntry.getAsId(), project, "00001");
        iPartsDataReplacePart replacement2 = prepareReplacementDOForJoinSim(partListEntry.getAsId(), project, "00002");
        iPartsDataReplacePart invalidReplacement = prepareReplacementDOForJoinSim(invalidPartListEntry.getAsId(), project, "00001");
        iPartsDataReplacePart irrelevantReplacement = prepareReplacementDOForJoinSim(irrelevantPartListEntry.getAsId(),
                                                                                     project, "00001");

        try {
            // Die Stücklisteneinträge in der DB anlegen
            addPartListEntrytoDBForJoinSim(partListEntry, matNr, "SOURCE_GUID_foo");

            addPartListEntrytoDBForJoinSim(invalidPartListEntry, "A1233260929", "SOURCE_GUID_invalid");
            invalidPartListEntry.setFieldValueAsBoolean(FIELD_K_BESTFLAG, false, DBActionOrigin.FROM_EDIT);

            addPartListEntrytoDBForJoinSim(irrelevantPartListEntry, "A1233260927", "SOURCE_GUID_irr");

            // Zwei neue passende Ersetzungen und eine nicht passende Ersetzung in der DB anlegen
            addNewReplacementToDBForJoinSim(replacement, "RFMEA_foo");
            addNewReplacementToDBForJoinSim(replacement2, "RFMEA_bla");
            addNewReplacementToDBForJoinSim(irrelevantReplacement, "RFMEA_irr");

            // Join ohne ChangeSet durchführen
            String resultWithoutChangeSet = resultJSON.getJSON();

            EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
            try {
                // Test für das Verändern vom Stücklisteneintrag und/oder Ersetzungen mit dazugehörigem Join
                if (modifyPartListEntry) {
                    partListEntry.setFieldValue(FIELD_K_MATNR, "A1233260927", DBActionOrigin.FROM_EDIT);
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(partListEntry);
                }
                if (modifyRelevantReplacements) {
                    replacement.setFieldValue(FIELD_DRP_VARI, "Test456", DBActionOrigin.FROM_EDIT);
                    replacement.setFieldValue(FIELD_DRP_SEQNO, "00002", DBActionOrigin.FROM_EDIT); // überschreibt sonst irrelevantReplacement
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(replacement);
                }
                if (modifyIrrelevantDataObjects) {
                    irrelevantPartListEntry.setFieldValue(FIELD_K_MATNR, "A1233260928", DBActionOrigin.FROM_EDIT);
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(irrelevantPartListEntry);

                    irrelevantReplacement.setFieldValue(FIELD_DRP_VARI, "Test789", DBActionOrigin.FROM_EDIT);
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(irrelevantReplacement);
                }

                // Das Verändern vom invalidPartListEntry darf nie zu einem Unterschied führen, weil die Materialnummer
                // nie zum whereValue passt
                invalidPartListEntry.setFieldValueAsBoolean(FIELD_K_BESTFLAG, true, DBActionOrigin.FROM_EDIT);
                revisionsHelper.addDataObjectToActiveChangeSetForEdit(invalidPartListEntry);

                if (withPseudoTransactionComparison) {
                    EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
                }
                String resultWithChangeSetGetRecords;
                try {
                    // Join mit ChangeSet durchführen
                    resultWithChangeSetGetRecords = resultJSON.getJSON();
                } finally {
                    if (withPseudoTransactionComparison) {
                        EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
                    }
                }

                // Nur simple Tests für das erwartete Ergebnis ohne Unterschied zwischen mit und ohne ChangeSet, da die
                // getRecords()-Simulation vom Join durch den Vergleich mit der Pseudo-Transaktion weiter unten stattfindet
                assertFalse(resultWithChangeSetGetRecords.contains("TestInvalid"));
                if (matNr.equals(whereValue)) {
                    assertTrue(resultWithoutChangeSet.contains("Test123"));
                }
                if (!changeSetMustHaveDifferentJoinResult) {
                    assertEquals(resultWithoutChangeSet, resultWithChangeSetGetRecords);
                    if (matNr.equals(whereValue)) {
                        assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
                    }
                }

                if (withPseudoTransactionComparison) {
                    // Ergebnis muss ohne und mit Pseudo-Transaktion identisch sein
                    String resultWithChangeSetPseudoTransaction = resultJSON.getJSON();
                    assertEquals(resultWithChangeSetPseudoTransaction, resultWithChangeSetGetRecords);
                }
            } finally {
                finalizeRevisionHelper(revisionsHelper);
            }
        } finally {
            partListEntry.deleteFromDB(true);
            invalidPartListEntry.deleteFromDB(true);
            irrelevantPartListEntry.deleteFromDB(true);
            replacement.deleteFromDB(true);
            replacement2.deleteFromDB(true);
            invalidReplacement.deleteFromDB(true);
            irrelevantReplacement.deleteFromDB(true);
        }
    }

    public void testSimpleInnerJoinForDeletedDataObjects() {
        internalTestSimpleInnerJoinForDeletedDataObjects(false);
    }

    public void internalTestSimpleInnerJoinForDeletedDataObjects(boolean isLeftOuterJoin) {
        // Gelöschter Stücklisteneintrag mit Ersetzungen -> anderes Ergebnis mit Changeset
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", true, false, false, true, true);

        // Gelöschter Stücklisteneintrag mit Ersetzungen, der aber nicht zur where-Bedingung passt (diese findet gar keine Datensätze
        // für den Join) -> gleiches Ergebnis wie ohne ChangeSet
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", true, false, false, false, true);

        // Gelöschter Stücklisteneintrag mit Ersetzungen, der aber nicht zur where-Bedingung passt (diese findet aber andere
        // Datensätze für die Materialnummer A1233260926 in der DB) -> gleiches Ergebnis wie ohne ChangeSet
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", true, false, false, false, true);

        // Tests wie oben nur mit gelöschten Ersetzungen
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", false, true, false, true, true);
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", false, true, false, false, true);
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", false, true, false, false, true);

        // Tests wie oben nur mit gelöschtem Stücklisteneintrag und gelöschten Ersetzungen
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", true, true, false, true, true);
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", true, true, false, false, true);
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", true, true, false, false, true);

        // Tests wie oben nur mit gelöschter irrelevanter Ersetzung -> gleiches Ergebnis wie ohne ChangeSet außer beim zweiten Test
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", false, false, true, false, true);
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", false, false, true, true, true);
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", false, false, true, false, true);

        // Tests wie oben nur mit gelöschtem Stücklisteneintrag, Ersetzungen und irrelevanten DataObjects -> nur beim dritten
        // Test gleiches Ergebnis
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260926", true, true, true, true, true);
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260926", "A1233260927", true, true, true, true, true);
        internalTestSimpleJoinForDeletedDataObjects(isLeftOuterJoin, "A1233260927", "A1233260926", true, true, true, false, true);
    }

    private void internalTestSimpleJoinForDeletedDataObjects(boolean isLeftOuterJoin, String matNr, String whereValue, boolean deletePartListEntry,
                                                             boolean deleteRelevantReplacements, boolean deleteIrrelevantDataObjects,
                                                             boolean changeSetMustHaveDifferentJoinResult, boolean withPseudoTransactionComparison) {
        AbstractGetJSONFromDataObjectList resultJSON = createGetJSONForTestSimpleJoin(whereValue, isLeftOuterJoin);

        // Neuen Stücklisteneintrag in der DB anlegen
        EtkProject project = getProject();
        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("Test123",
                                                                                                                       "", "00001"));
        EtkDataPartListEntry irrelevantPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("Test456",
                                                                                                                                 "", "00001"));
        iPartsDataReplacePart replacement = prepareReplacementDOForJoinSim(partListEntry.getAsId(), project, "00001");
        iPartsDataReplacePart replacement2 = prepareReplacementDOForJoinSim(partListEntry.getAsId(), project, "00002");
        iPartsDataReplacePart irrelevantReplacement = prepareReplacementDOForJoinSim(irrelevantPartListEntry.getAsId(),
                                                                                     project, "00001");

        try {
            // Zwei neue Stücklisteneinträge in der DB anlegen
            addPartListEntrytoDBForJoinSim(partListEntry, matNr, "SOURCE_GUID_foo");
            addPartListEntrytoDBForJoinSim(irrelevantPartListEntry, "A1233260927", "SOURCE_GUID_irr");

            // Zwei neue passende Ersetzungen und eine nicht passende Ersetzung in der DB anlegen
            addNewReplacementToDBForJoinSim(replacement, "RFMEA_foo");
            addNewReplacementToDBForJoinSim(replacement2, "RFMEA_bla");
            addNewReplacementToDBForJoinSim(irrelevantReplacement, "RFMEA_irr");

            // Join ohne ChangeSet durchführen
            String resultWithoutChangeSet = resultJSON.getJSON();

            EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
            try {
                // Test für das Löschen vom Stücklisteneintrag und/oder Ersetzungen mit dazugehörigem Join
                if (deletePartListEntry) {
                    revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(partListEntry);
                }
                if (deleteRelevantReplacements) {
                    revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(replacement);
                    revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(replacement2);
                }
                if (deleteIrrelevantDataObjects) {
                    revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(irrelevantPartListEntry);
                    revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(irrelevantReplacement);
                }

                if (withPseudoTransactionComparison) {
                    EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
                }
                String resultWithChangeSetGetRecords;
                try {
                    // Join mit ChangeSet durchführen
                    resultWithChangeSetGetRecords = resultJSON.getJSON();
                } finally {
                    if (withPseudoTransactionComparison) {
                        EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
                    }
                }

                if (matNr.equals(whereValue)) {
                    assertTrue(resultWithoutChangeSet.contains("Test123"));
                }

                // Test123 und dessen Daten dürfen mit den Änderungen aus dem ChangeSet im Ergebnis nicht vorhanden sein,
                // vorher aber schon
                if (changeSetMustHaveDifferentJoinResult) {
                    assertFalse(resultWithoutChangeSet.equals(resultWithChangeSetGetRecords));
                    if (!isLeftOuterJoin || deletePartListEntry || !matNr.equals(whereValue)) {
                        assertFalse(resultWithChangeSetGetRecords.contains("Test123"));
                        assertFalse(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
                    } else {
                        assertTrue(resultWithChangeSetGetRecords.contains("Test123"));
                        assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
                    }
                    if (!isLeftOuterJoin || deletePartListEntry || deleteRelevantReplacements || !matNr.equals(whereValue)) {
                        assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_foo"));
                        assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_bla"));
                    } else {
                        assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_foo"));
                        assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_bla"));
                    }
                } else {
                    assertEquals(resultWithoutChangeSet, resultWithChangeSetGetRecords);
                    if (matNr.equals(whereValue)) {
                        assertTrue(resultWithChangeSetGetRecords.contains("Test123"));
                        assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_foo"));
                        assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_bla"));
                        assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_foo"));
                    }
                }

                // Test456 und dessen Daten müssen mit den Änderungen aus dem ChangeSet im Ergebnis vorhanden sein, wenn
                // nach dessen Materialnummer A1233260927 gesucht wird und die irrelevanten DataObjects nicht im ChangeSet
                // gelöscht wurden
                if (whereValue.equals("A1233260927")) {
                    assertTrue(resultWithoutChangeSet.contains("Test456"));
                    if (deleteIrrelevantDataObjects) {
                        assertFalse(resultWithChangeSetGetRecords.contains("Test456"));
                        assertFalse(resultWithChangeSetGetRecords.contains("SOURCE_GUID_irr"));
                        assertFalse(resultWithChangeSetGetRecords.contains("RFMEA_irr"));
                    } else {
                        assertTrue(resultWithChangeSetGetRecords.contains("Test456"));
                        assertTrue(resultWithChangeSetGetRecords.contains("SOURCE_GUID_irr"));
                        assertTrue(resultWithChangeSetGetRecords.contains("RFMEA_irr"));
                    }
                }

                if (withPseudoTransactionComparison) {
                    // Ergebnis muss ohne und mit Pseudo-Transaktion identisch sein
                    String resultWithChangeSetPseudoTransaction = resultJSON.getJSON();
                    assertEquals(resultWithChangeSetPseudoTransaction, resultWithChangeSetGetRecords);
                }
            } finally {
                finalizeRevisionHelper(revisionsHelper);
            }
        } finally {
            partListEntry.deleteFromDB(true);
            irrelevantPartListEntry.deleteFromDB(true);
            replacement.deleteFromDB(true);
            replacement2.deleteFromDB(true);
            irrelevantReplacement.deleteFromDB(true);
        }
    }

    // Nur für Performance-Tests und nicht relevant für den normalen Unittest-Betrieb
    public void _testPerformanceOfSimpleJoinForDeletedDataObjects() {
        Logger.getLogger().removeChannel(EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION);
        try {
            int loopCount = 100;

            // Mit Pseudo-Transaktionen
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < loopCount; i++) {
                internalTestSimpleJoinForDeletedDataObjects(false, "A1233260926", "A1233260926", true, false, false, true, false);
            }
            System.out.println("Duration for " + loopCount + " times simulating a simple join for deleted dataObjects with pseudo transactions: "
                               + DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false,
                                                                    Language.DE.getCode()));

            // Mit getRecords()-Simulation
            EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
            try {
                startTime = System.currentTimeMillis();
                for (int i = 0; i < loopCount; i++) {
                    internalTestSimpleJoinForDeletedDataObjects(false, "A1233260926", "A1233260926", true, false, false, true, false);
                }
                System.out.println("Duration for " + loopCount + " times simulating a simple join for deleted dataObjects with getRecords(): "
                                   + DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false,
                                                                        Language.DE.getCode()));
            } finally {
                EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
            }
        } finally {
            Logger.getLogger().addChannel(EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION);
        }
    }

    // Ab hier Tests mit LeftOuter-Joins
    public void testSimpleLeftOuterJoinForNewDataObjects() {
        internalTestSimpleJoinForNewDataObjects(true);
    }

    public void testSimpleLeftOuterJoinForModifiedDataObjects() {
        internalTestSimpleJoinForModifiedDataObjects(true);
    }

    public void testSimpleLeftOuterJoinForModifiedDataObjectsForDifferentResultSet() {
        internalTestSimpleJoinForModifiedDataObjectsForDifferentResultSet(true);
    }

    public void testSimpleLeftOuterJoinForDeletedDataObjects() {
        internalTestSimpleInnerJoinForDeletedDataObjects(true);
    }

    // Ab hier Tests mit Joins für mehrsprachige Texte
    private AbstractGetJSONFromDataObjectList createGetJSONForTestSimpleJoinWithMultiLang(String whereValue, boolean withJoin,
                                                                                          boolean isLeftOuterJoin, String language) {
        return new AbstractGetJSONFromDataObjectList() {
            @Override
            protected EtkDisplayFields createSelectFields() {
                EtkDisplayFields selectFields = new EtkDisplayFields();
                selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_BESTFLAG, false, false));
                selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false));
                selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_CONST_DESC, true, false));
                if (withJoin) {
                    selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_GUID, false, false));
                }
                return selectFields;
            }

            @Override
            protected EtkDataObjectList createDataObjectList() {
                EtkDataObjectList<? extends EtkDataObject> dataObjectList;
                if (withJoin) {
                    // Join von KATALOG-Tabelle auf MAT-Tabelle inkl. implizitem Join von MAT-Tabelle auf SPRACHE-Tabelle
                    // über die mehrsprachigen Felder
                    dataObjectList = new EtkDataPartListEntryList();
                    dataObjectList.searchSortAndFillWithJoin(getProject(), language, getSelectFields(),
                                                             new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_MATNR) },
                                                             new String[]{ whereValue }, false, null, false, null,
                                                             new EtkDataObjectList.JoinData(TABLE_MAT, new String[]{ FIELD_K_MATNR },
                                                                                            new String[]{ FIELD_M_MATNR },
                                                                                            isLeftOuterJoin, false));
                } else {
                    // Impliziter Join von MAT-Tabelle auf SPRACHE-Tabelle über die mehrsprachigen Felder
                    dataObjectList = new EtkDataPartList();
                    dataObjectList.searchSortAndFillWithJoin(getProject(), language, getSelectFields(),
                                                             new String[]{ TableAndFieldName.make(TABLE_MAT, FIELD_M_MATNR) },
                                                             new String[]{ whereValue }, false, null, false, null);
                }

                // Die mehrsprachigen Felder explizit auf "vollständig geladen" setzen, weil diese ansonsten bei der
                // Serialisierung aus der DB nachgeladen werden würden, was das Ergebnis verfälscht
                for (EtkDataObject dataObject : dataObjectList) {
                    dataObject.getAttribute(FIELD_M_TEXTNR).setMultiLanguageCompleteLoaded(true);
                    dataObject.getAttribute(FIELD_M_CONST_DESC).setMultiLanguageCompleteLoaded(true);
                }
                return dataObjectList;
            }
        };
    }

    public void testSimpleJoinForNewMultiLang() {
        // Anlegen von Texten mit Text-ID in ChangeSets explizit erlauben
        boolean oldTextsWithIdCreatableInChangeSets = iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS;
        iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS = true;
        try {
            // Neuer und abgefragter deutscher Text -> anderes Ergebnis mit Changeset
            internalTestSimpleJoinForNewMultiLang(false, false, "TestMat123", Language.DE.getCode(), true, true);

            // Neuer und abgefragter englischer Text -> anderes Ergebnis mit Changeset
            internalTestSimpleJoinForNewMultiLang(false, false, "TestMat123", Language.EN.getCode(), true, true);

            // Where-Bedingung passt gar nicht -> gleiches Ergebnis mit Changeset
            internalTestSimpleJoinForNewMultiLang(false, false, "foo", Language.DE.getCode(), false, true);

            // Tests wie oben nur mit einem expliziten Inner-Join
            internalTestSimpleJoinForNewMultiLang(true, false, "TestMat123", Language.DE.getCode(), true, true);
            internalTestSimpleJoinForNewMultiLang(true, false, "TestMat123", Language.EN.getCode(), true, true);
            internalTestSimpleJoinForNewMultiLang(true, false, "foo", Language.DE.getCode(), false, true);

            // Tests wie oben nur mit einem expliziten LeftOuter-Join
            internalTestSimpleJoinForNewMultiLang(true, true, "TestMat123", Language.DE.getCode(), true, true);
            internalTestSimpleJoinForNewMultiLang(true, true, "TestMat123", Language.EN.getCode(), true, true);

            // Bei LeftOuter-Join gibt es hier einen Unterschied mit Changeset, weil der Stücklisteneintrag explizit mit dem
            // where-Value als Materialnummer angelegt wird ohne dazu existierendes Material, was also leere MAT-Felder ergibt
            internalTestSimpleJoinForNewMultiLang(true, true, "foo", Language.DE.getCode(), true, true);
        } finally {
            iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS = oldTextsWithIdCreatableInChangeSets;
        }
    }

    private void internalTestSimpleJoinForNewMultiLang(boolean withJoin, boolean isLeftOuterJoin, String whereValue,
                                                       String dbLanguage, boolean changeSetMustHaveDifferentJoinResult,
                                                       boolean withPseudoTransactionComparison) {
        AbstractGetJSONFromDataObjectList resultJSON = createGetJSONForTestSimpleJoinWithMultiLang(whereValue, withJoin,
                                                                                                   isLeftOuterJoin, dbLanguage);

        // Neues Material mit neuem Text in der DB anlegen
        EtkProject project = getProject();
        EtkDataPart part = EtkDataObjectFactory.createDataPart(project, new PartId("TestMat123", ""));
        if (part.existsInDB()) {
            part.deleteFromDB(true);
        }
        part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        project.getDbLayer().delete(EtkDbConst.TABLE_SPRACHE, new String[]{ FIELD_S_TEXTID }, new String[]{ "TestMat123TextId" });

        EtkMultiSprache multiLang = new EtkMultiSprache("TestMat123TextId");
        multiLang.setText(Language.EN.getCode(), "Test-Text EN"); // Absichtlich nur Englisch wegen Test der Rückfallsprache
        EtkMultiSprache multiLang2 = multiLang.cloneMe();
        part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, multiLang, DBActionOrigin.FROM_EDIT);
        part.setFieldValueAsMultiLanguage(FIELD_M_CONST_DESC, multiLang2, DBActionOrigin.FROM_EDIT);

        // Optional neuen Stücklisteneintrag in der DB anlegen für das Material
        EtkDataPartListEntry partListEntry = null;
        if (withJoin) {
            partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("Test123", "", "00001"));
            if (partListEntry.existsInDB()) {
                partListEntry.deleteFromDB(true);
            }
            partListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            partListEntry.setFieldValue(FIELD_K_MATNR, whereValue, DBActionOrigin.FROM_EDIT);
            partListEntry.setFieldValue(FIELD_K_SOURCE_GUID, "SOURCE_GUID_foo", DBActionOrigin.FROM_EDIT);
        }

        try {
            // Join ohne ChangeSet durchführen
            String resultWithoutChangeSetText = resultJSON.getJSON(true);

            EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
            try {
                // Test für ein neues DataObject mit Text mit dazugehörigem Join und optionalem Stücklisteneintrag dazu
                revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);
                if (partListEntry != null) {
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(partListEntry);
                }

                if (withPseudoTransactionComparison) {
                    EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
                }
                String resultWithChangeSetGetRecordsText;
                String resultWithChangeSetGetRecords;
                try {
                    // Join mit ChangeSet durchführen
                    resultWithChangeSetGetRecordsText = resultJSON.getJSON(true);
                    resultWithChangeSetGetRecords = resultJSON.getJSON();
                } finally {
                    if (withPseudoTransactionComparison) {
                        EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
                    }
                }

                assertFalse(resultWithoutChangeSetText.contains("Test-Text"));
                if (whereValue.equals(part.getAsId().getMatNr())) {
                    assertTrue(resultWithChangeSetGetRecordsText.contains("Test-Text EN"));
                }
                if (withJoin) {
                    if (whereValue.equals(part.getAsId().getMatNr()) || isLeftOuterJoin) {
                        assertTrue(resultWithChangeSetGetRecordsText.contains("SOURCE_GUID_foo"));
                    } else {
                        assertFalse(resultWithChangeSetGetRecordsText.contains("SOURCE_GUID_foo"));
                    }
                }

                if (changeSetMustHaveDifferentJoinResult) {
                    assertFalse(resultWithoutChangeSetText.equals(resultWithChangeSetGetRecordsText));
                } else {
                    assertEquals(resultWithoutChangeSetText, resultWithChangeSetGetRecordsText);
                }

                if (withPseudoTransactionComparison) {
                    // Ergebnis muss ohne und mit Pseudo-Transaktion identisch sein
                    String resultWithChangeSetPseudoTransaction = resultJSON.getJSON();
                    assertEquals(resultWithChangeSetPseudoTransaction, resultWithChangeSetGetRecords);
                }
            } finally {
                finalizeRevisionHelper(revisionsHelper);
            }
        } finally {
            // Eigentlich unnötig, weil nur Teil vom Changeset, aber sicher ist sicher...
            part.deleteFromDB(true);
            if (partListEntry != null) {
                partListEntry.deleteFromDB(true);
            }
            project.getDbLayer().delete(EtkDbConst.TABLE_SPRACHE, new String[]{ FIELD_S_TEXTID }, new String[]{ "TestMat123TextId" });
        }
    }

    // Veränderter und abgefragter deutscher Text -> anderes Ergebnis mit Changeset
    public void testSimpleJoinForModifiedMultiLang1() {
        internalTestSimpleJoinForModifiedMultiLang(false, false, "TestMat123", Language.DE.getCode(), Language.DE.getCode(), true, true);
    }

    // Veränderter und abgefragter englischer Text -> anderes Ergebnis mit Changeset
    public void testSimpleJoinForModifiedMultiLang2() {
        internalTestSimpleJoinForModifiedMultiLang(false, false, "TestMat123", Language.EN.getCode(), Language.EN.getCode(), true, true);
    }

    // Veränderter englischer Text, aber abgefragter deutscher Text -> gleiches Ergebnis mit Changeset
    public void testSimpleJoinForModifiedMultiLang3() {
        internalTestSimpleJoinForModifiedMultiLang(false, false, "TestMat123", Language.DE.getCode(), Language.EN.getCode(), false, true);
    }

    // Where-Bedingung passt gar nicht -> gleiches Ergebnis mit Changeset
    public void testSimpleJoinForModifiedMultiLang4() {
        internalTestSimpleJoinForModifiedMultiLang(false, false, "foo", Language.DE.getCode(), Language.DE.getCode(), false, true);
    }

    // Tests wie oben nur mit einem expliziten Inner-Join
    public void testSimpleJoinForModifiedMultiLang5a() {
        internalTestSimpleJoinForModifiedMultiLang(true, false, "TestMat123", Language.DE.getCode(), Language.DE.getCode(), true, true);
    }

    public void testSimpleJoinForModifiedMultiLang5b() {
        internalTestSimpleJoinForModifiedMultiLang(true, false, "TestMat123", Language.EN.getCode(), Language.EN.getCode(), true, true);
    }

    public void testSimpleJoinForModifiedMultiLang5c() {
        internalTestSimpleJoinForModifiedMultiLang(true, false, "TestMat123", Language.DE.getCode(), Language.EN.getCode(), false, true);
    }

    public void testSimpleJoinForModifiedMultiLang5d() {
        internalTestSimpleJoinForModifiedMultiLang(true, false, "foo", Language.DE.getCode(), Language.DE.getCode(), false, true);
    }

    // Tests wie oben nur mit einem expliziten LeftOuter-Join
    public void testSimpleJoinForModifiedMultiLang6a() {
        internalTestSimpleJoinForModifiedMultiLang(true, true, "TestMat123", Language.DE.getCode(), Language.DE.getCode(), true, true);
    }

    public void testSimpleJoinForModifiedMultiLang6b() {
        internalTestSimpleJoinForModifiedMultiLang(true, true, "TestMat123", Language.EN.getCode(), Language.EN.getCode(), true, true);
    }

    public void testSimpleJoinForModifiedMultiLang6c() {
        internalTestSimpleJoinForModifiedMultiLang(true, true, "TestMat123", Language.DE.getCode(), Language.EN.getCode(), false, true);
    }

    public void testSimpleJoinForModifiedMultiLang6d() {
        internalTestSimpleJoinForModifiedMultiLang(true, true, "foo", Language.DE.getCode(), Language.DE.getCode(), false, true);
    }

    private void internalTestSimpleJoinForModifiedMultiLang(boolean withJoin, boolean isLeftOuterJoin, String whereValue,
                                                            String dbLanguage, String modifiedLanguage, boolean changeSetMustHaveDifferentJoinResult,
                                                            boolean withPseudoTransactionComparison) {
        AbstractGetJSONFromDataObjectList resultJSON = createGetJSONForTestSimpleJoinWithMultiLang(whereValue, withJoin,
                                                                                                   isLeftOuterJoin, dbLanguage);

        // Neues Material mit neuem Text in der DB anlegen
        EtkProject project = getProject();
        EtkDataPart part = EtkDataObjectFactory.createDataPart(project, new PartId("TestMat123", ""));
        if (!part.existsInDB()) {
            part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }

        EtkMultiSprache multiLang = new EtkMultiSprache("TestMat123TextId");
        multiLang.setText(Language.DE.getCode(), "Test-Text DE");
        multiLang.setText(Language.EN.getCode(), "Test-Text EN");
        EtkMultiSprache multiLang2 = multiLang.cloneMe();
        part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, multiLang, DBActionOrigin.FROM_EDIT);
        part.setFieldValueAsMultiLanguage(FIELD_M_CONST_DESC, multiLang2, DBActionOrigin.FROM_EDIT);
        part.saveToDB();

        // Optional neuen Stücklisteneintrag in der DB anlegen für das Material
        EtkDataPartListEntry partListEntry = null;
        if (withJoin) {
            partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("Test123", "", "00001"));
            addPartListEntrytoDBForJoinSim(partListEntry, whereValue, "SOURCE_GUID_foo");
        }

        // Anlegen von Texten mit Text-ID in ChangeSets explizit erlauben
        boolean oldTextsWithIdCreatableInChangeSets = iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS;
        iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS = true;
        try {
            // Join ohne ChangeSet durchführen
            String resultWithoutChangeSetText = resultJSON.getJSON(true);

            EtkRevisionsHelper revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
            try {
                // Test für das Verändern vom Text mit dazugehörigem Join
                // In der DB vorhandene Texte mit Text-ID können in ChangeSets nicht verändert werden -> Text-ID auf eine
                // andere Text-ID ändern
                String newTextId = changeSetMustHaveDifferentJoinResult ? "TestMat123TextIdNeu" : multiLang.getTextId();
                part = EtkDataObjectFactory.createDataPart(project, new PartId("TestMat123", ""));
                EtkMultiSprache modifiedMultiLang = part.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR);
                modifiedMultiLang.setTextId(newTextId);
                modifiedMultiLang.setText(modifiedLanguage, "Veränderter Text " + modifiedLanguage);
                part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, modifiedMultiLang, DBActionOrigin.FROM_EDIT);
                revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);

                // M_CONST_DESC nur dann auf die neue Text-ID setzen, wenn auch der Text in der aktuellen DB-Sprache verändert wird
                if (changeSetMustHaveDifferentJoinResult) {
                    // Neue Text-ID auch bei M_CONST_DESC setzen (und explizit sagen, dass der mehrsprachige Text noch nicht
                    // vollständig geladen wurde, damit er über das ChangeSet nachgeladen wird mit den gleichen Werten wie in
                    // M_TEXTNR
                    multiLang2 = new EtkMultiSprache(newTextId);
                    part.setFieldValueAsMultiLanguage(FIELD_M_CONST_DESC, multiLang2, DBActionOrigin.FROM_EDIT);
                    part.getAttribute(FIELD_M_CONST_DESC).setMultiLanguageCompleteLoaded(false); // Vorhandene Texte müssen nachgeladen werden!
                    String constDescDEValue = part.getFieldValue(FIELD_M_CONST_DESC, modifiedLanguage, false);
                    assertEquals("Veränderter Text " + modifiedLanguage, constDescDEValue);
                    revisionsHelper.addDataObjectToActiveChangeSetForEdit(part);
                }

                if (withPseudoTransactionComparison) {
                    EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
                }
                String resultWithChangeSetGetRecordsText;
                String resultWithChangeSetGetRecords;
                try {
                    // Join mit ChangeSet durchführen
                    resultWithChangeSetGetRecordsText = resultJSON.getJSON(true);
                    resultWithChangeSetGetRecords = resultJSON.getJSON();
                } finally {
                    if (withPseudoTransactionComparison) {
                        EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
                    }
                }

                if (whereValue.equals(part.getAsId().getMatNr())) {
                    if (dbLanguage.equals(modifiedLanguage)) {
                        assertTrue(resultWithoutChangeSetText.contains("Test-Text"));
                        assertTrue(resultWithChangeSetGetRecordsText.contains("Veränderter Text " + dbLanguage));
                        assertFalse(resultWithChangeSetGetRecordsText.contains("Test-Text " + dbLanguage));
                    } else {
                        assertFalse(resultWithChangeSetGetRecordsText.contains("Veränderter Text " + dbLanguage));
                        assertTrue(resultWithChangeSetGetRecordsText.contains("Test-Text " + dbLanguage));
                    }
                }
                if (withJoin) {
                    if (whereValue.equals(part.getAsId().getMatNr()) || isLeftOuterJoin) {
                        assertTrue(resultWithChangeSetGetRecordsText.contains("SOURCE_GUID_foo"));
                    } else {
                        assertFalse(resultWithChangeSetGetRecordsText.contains("SOURCE_GUID_foo"));
                    }
                }

                if (changeSetMustHaveDifferentJoinResult) {
                    assertFalse(resultWithoutChangeSetText.equals(resultWithChangeSetGetRecordsText));
                } else {
                    assertEquals(resultWithoutChangeSetText, resultWithChangeSetGetRecordsText);
                }

                if (withPseudoTransactionComparison) {
                    // Ergebnis muss ohne und mit Pseudo-Transaktion identisch sein
                    String resultWithChangeSetPseudoTransaction = resultJSON.getJSON();
                    assertEquals(resultWithChangeSetPseudoTransaction, resultWithChangeSetGetRecords);
                }
            } finally {
                finalizeRevisionHelper(revisionsHelper);
            }
        } finally {
            iPartsDbObjectsLayer.TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS = oldTextsWithIdCreatableInChangeSets;
            part.deleteFromDB(true);
            if (partListEntry != null) {
                partListEntry.deleteFromDB(true);
            }
            project.getDbLayer().delete(EtkDbConst.TABLE_SPRACHE, new String[]{ FIELD_S_TEXTID }, new String[]{ "TestMat123TextId" });
        }
    }

    // Nur für Performance-Tests und nicht relevant für den normalen Unittest-Betrieb
    public void _testPerformanceOfSimpleJoinForModifiedMultiLang() {
        Logger.getLogger().removeChannel(EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION);
        try {
            int loopCount = 100;

            // Mit Pseudo-Transaktionen
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < loopCount; i++) {
                internalTestSimpleJoinForModifiedMultiLang(false, false, "TestMat123", Language.DE.getCode(), Language.DE.getCode(),
                                                           true, false);
            }
            System.out.println("Duration for " + loopCount + " times simulating a simple join for modified dataObjects with pseudo transactions: "
                               + DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false,
                                                                    Language.DE.getCode()));

            // Mit getRecords()-Simulation
            EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = false;
            try {
                startTime = System.currentTimeMillis();
                for (int i = 0; i < loopCount; i++) {
                    internalTestSimpleJoinForModifiedMultiLang(false, false, "TestMat123", Language.DE.getCode(), Language.DE.getCode(),
                                                               true, false);
                }
                System.out.println("Duration for " + loopCount + " times simulating a simple join for modified dataObjects with getRecords(): "
                                   + DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false,
                                                                        Language.DE.getCode()));
            } finally {
                EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS = true;
            }
        } finally {
            Logger.getLogger().addChannel(EtkDataObjectList.LOG_CHANNEL_CHANGE_SET_JOIN_SIMULATION);
        }
    }


    /**
     * Klasse, die die Test für die Primärschlüsseländerungen vereinfacht. Anstatt die Objekte selbst zu erzeugen wird hier mit
     * einfachen Befehlen insert move delete gearbeitet. Im Hintergrund werden damit DBObjecte erzeugt und die wie im Editor gespeichert.
     */
    abstract class AbstractPKChangeTest {

        EtkDataObject parentObject = null;
        DBDataObjectList objects;
        EtkRevisionsHelper revisionsHelper;
        boolean useMultiChangeSets = false;
        int currentPermutation;

        /**
         * Liste der Aktionen, insert, update etc.
         *
         * @param permutation
         */
        protected abstract void doActions(int permutation);

        /**
         * Falls ein Test nur kleine Unterschiede im Ablauf hat, dann kann man das mit zwei Durchläufen machen
         *
         * @return
         */
        protected abstract int getNumberOfPermutations();

        /**
         * Erzeugt ein typisiertes DBObjekt mit welchem gearbeitet werden soll.
         *
         * @return
         */
        protected abstract EtkDataObject createDataObject();

        /**
         * Typisierte Liste der DBObjecte erzeugen
         *
         * @return
         */
        protected abstract DBDataObjectList getObjectsList();

        /**
         * Hilfsfeld (kann ein beliebiges des Datensatzes sein), welches als Identifizierung des Datensatzes verwendet wird.
         * In diesem Feld muss nach einer Primärschlüsseländerung das gleiche enthalten sein wie vorher. Es ist stellvertretend für die Nutzdaten.
         *
         * @return
         */
        protected abstract String getPayLoadAttribute();


        protected void insert(IdWithType id, String payLoad) {
            insert(id, payLoad, false);
        }

        /**
         * DBObject mit ID neu anlegen
         *
         * @param id
         * @param payLoad
         */
        protected void insert(IdWithType id, String payLoad, boolean allowExists) {

            if (!allowExists) {
                // Erstmal testen, ob es schon in der Datenbank ist
                EtkDataObject obj = createDataObject();
                obj.setId(id, DBActionOrigin.FROM_DB);
                if (obj.loadFromDB(id)) {
                    fail(id.toString() + " is in DB");
                }
            }

            EtkDataObject obj = createDataObject();
            obj.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            obj.setId(id, DBActionOrigin.FROM_EDIT);

            // Alte ID würde ansonsten nur aus leeren Strings bestehen, aber es handelt sich ja um ein neues EtkDataObject
            // -> oldId = id
            obj.updateOldId();

            obj.setFieldValue(getPayLoadAttribute(), payLoad, DBActionOrigin.FROM_EDIT);

            getOrCreateObjectList().add(obj, DBActionOrigin.FROM_EDIT);
        }


        /**
         * DBObject löschen
         *
         * @param id
         * @param payLoad
         */
        protected void delete(IdWithType id, String payLoad) {
            EtkDataObject obj = findObjectByPayLoad(id, payLoad);
            if (obj == null) {
                obj = loadObj(id, payLoad);
            }
            getOrCreateObjectList().delete(obj, DBActionOrigin.FROM_EDIT);
        }


        /**
         * Primärschlüssel ändern
         *
         * @param payLoad
         * @param oldId
         * @param newId
         * @param deleteOldId
         */
        protected void move(String payLoad, IdWithType oldId, IdWithType newId, boolean deleteOldId) {
            EtkDataObject obj = findObjectByPayLoad(oldId, payLoad);

            if (obj == null) {
                obj = loadObj(oldId, payLoad);
            }

            obj.setId(newId, DBActionOrigin.FROM_EDIT);
            obj.setDeleteOldId(deleteOldId);
        }

        /**
         * Änderungen im Changeset speichern
         */
        protected void save() {
            if (getParentObject() != null) {
                revisionsHelper.addDataObjectToActiveChangeSetForEdit(getParentObject());
            } else {
                revisionsHelper.addDataObjectListToActiveChangeSetForEdit(getOrCreateObjectList());
            }
            objects = null;

            if (useMultiChangeSets) {
                addTempChangeSetToRevisionHelper(revisionsHelper);
            }
        }

        /**
         * Test, ob die Id mit dem Payload in der Datenbank ist
         *
         * @param id
         * @param payLoad
         */
        protected void testExists(IdWithType id, String payLoad) {
            EtkDataObject obj = createDataObject();
            if (!obj.loadFromDB(id)) {
                fail(id.toString() + " is not in DB with getRecords() simulation");
            }
            assertEquals(id.toString() + " has not payload \"" + payLoad + "\" in DB with getRecords() simulation", payLoad,
                         obj.getAttribute(getPayLoadAttribute()).getAsString());

            startPseudoTransactionForActiveChangeSet();
            try {

                obj = createDataObject();
                if (!obj.loadFromDB(id)) {
                    fail(id.toString() + " is not in DB with pseudo transaction");
                }
                assertEquals(id.toString() + " has not payload \"" + payLoad + "\" in DB with pseudo transaction", payLoad,
                             obj.getAttribute(getPayLoadAttribute()).getAsString());
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            // Teste, ob eine Abfrage, die mehr als einen Datensatz zurückliefern kann auch das gleiche Ergebnis liefert
            // Bei der pdeusotransaktion ist der Test nicht nötig, da hier im Fehlerfall eine primärschlüsselverletzung auf der Datenbank kommen würde

            String[] pkKeys = obj.getPKFields();
            String[] pkValues = id.toStringArrayWithoutType();

            String[] pkKeysShort = Arrays.copyOf(pkKeys, pkKeys.length - 1);
            String[] pkValuesShort = Arrays.copyOf(pkValues, pkValues.length - 1);

            EtkRecords recs = getProject().getDB().getRecords(obj.getTableName(), pkKeysShort, pkValuesShort);

            int index = recs.indexOf(pkKeys, pkValues);
            if (index >= 0) {
                String value = recs.get(index).getField(getPayLoadAttribute()).getAsString();
                if (!payLoad.equals(value)) {
                    fail(id.toString() + " is not in DB with getRecords() simulation and getRecords with ShortKey");
                }


                int index2 = recs.indexOf(pkKeys, pkValues, index + 1);
                if (index2 >= 0) {
                    // Zwei Datensätze mit gleichem Primärschlüssel -> Da stimmt die getRecordsSimulation nicht
                    fail(id.toString() + " is not unique in DB with getRecords() simulation and getRecords with ShortKey");
                }

            } else {
                fail(id.toString() + " is not in DB with getRecords() simulation and getRecords with ShortKey");
            }
        }

        /**
         * Test, ob die Id nicht mehr in der Datenbank ist
         *
         * @param id
         */
        protected void testNotExists(IdWithType id) {
            EtkDataObject obj = createDataObject();
            if (obj.loadFromDB(id)) {
                fail(id.toString() + " is in DB with getRecords() simulation");
            }

            startPseudoTransactionForActiveChangeSet();
            try {
                obj = createDataObject();
                if (obj.loadFromDB(id)) {
                    fail(id.toString() + " is in DB with pseudo transaction");
                }
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            // Teste, ob eine Abfrage, die mehr als einen Datensatz zurückliefern kann auch das gleiche Ergebnis liefert
            // Bei der pdeusotransaktion ist der Test nicht nötig, da hier im Fehlerfall eine primärschlüsselverletzung auf der Datenbank kommen würde

            String[] pkKeys = obj.getPKFields();
            String[] pkValues = id.toStringArrayWithoutType();

            String[] pkKeysShort = Arrays.copyOf(pkKeys, pkKeys.length - 1);
            String[] pkValuesShort = Arrays.copyOf(pkValues, pkValues.length - 1);

            EtkRecords recs = getProject().getDB().getRecords(obj.getTableName(), pkKeysShort, pkValuesShort);

            int index = recs.indexOf(pkKeys, pkValues);
            if (index >= 0) {
                fail(id.toString() + " is in DB with getRecords() simulation and getRecords with ShortKey");
            }
        }


        private DBDataObjectList getOrCreateObjectList() {
            if (objects == null) {
                objects = getObjectsList();
            }
            return objects;
        }

        public EtkDataObject getParentObject() {
            return parentObject;
        }

        /**
         * Die Liste der Objecte ist ein Memberliste dieses Parentobject. getObjectList muss dann auch die interne Liste des Parents sein
         *
         * @param parentObject
         */
        public void setParentObject(EtkDataObject parentObject) {
            this.parentObject = parentObject;

            // Wenn der Parent gesetzt wird muss auch die objectliste zurückgesetzt werden, da die Objectliste dann ja ein Member des Parents ist
            objects = null;
        }

        /**
         * Startet den Test mit den verschiedenen Permutationen
         */
        public void excecute() {
            for (int permutation = 0; permutation < getNumberOfPermutations(); permutation++) {
                excecute(false, permutation);
                excecute(true, permutation);
            }
        }

        /**
         * Eigentliche Testroutine
         *
         * @param useMultiChangeSets
         * @param permutation
         */
        protected void excecute(boolean useMultiChangeSets, int permutation) {
            this.currentPermutation = permutation;
            System.out.println("Executing AbstractPKChangeTest with useMultiChangeSets=" + useMultiChangeSets + " and permutation=" + permutation);
            this.useMultiChangeSets = useMultiChangeSets;
            revisionsHelper = initializeRevisionHelper(); // <== aktiviert auch das ChangeSet!
            try {
                setParentObject(null);
                objects = null;
                doActions(permutation);
            } finally {
                finalizeRevisionHelper(revisionsHelper);
            }
        }

        private EtkDataObject loadObj(IdWithType id, String payLoad) {
            EtkDataObject obj = createDataObject();

            if (!obj.loadFromDB(id)) {
                fail(id.toString() + " is not in DB");
            }
            assertEquals(null, payLoad, obj.getAttribute(getPayLoadAttribute()).getAsString());

            getOrCreateObjectList().add(obj, DBActionOrigin.FROM_DB);

            return obj;
        }

        private EtkDataObject findObjectByPayLoad(IdWithType id, String payLoad) {
            EtkDataObject result = null;
            List<EtkDataObject> list = getOrCreateObjectList().getAsList();
            for (EtkDataObject obj : list) {
                if (payLoad.equals(obj.getAttribute(getPayLoadAttribute()).getAsString())) {
                    if (!obj.getAsId().equals(id)) {
                        fail(payLoad + " has the wrong id. Expected: " + id.toString() + " actual: " + obj.getAsId().toString());
                    }
                    if (result != null) {
                        fail(payLoad + " not unique in objectList");
                    }
                    result = obj;
                }
            }
            return result;
        }

        String getTestPermutationString() {
            String result = "Current testpermutation: ";

            if (useMultiChangeSets) {
                result += "MultiChangeSets, ";
            } else {
                result += "SingleChangeSets, ";
            }

            result += "Permutation: " + currentPermutation;

            return result;

        }

        public void fail(String message) {
            Assert.fail(getTestPermutationString() + "\n" + message);
        }

        public void assertEquals(String message, String expected, String actual) {
            Assert.assertEquals(getTestPermutationString() + (StrUtils.isValid(message) ? "; " + message : ""), expected, actual);
        }
    }

    /**
     * Abstrakte Klasse, um für eine {@link EtkDataObjectList} (die sich aus einer DB-Abfrage ergibt) beim Aufruf von {@link #getJSON()}
     * das JSON der serialisierten {@link EtkDataObjectList} zurückzugeben.
     */
    abstract class AbstractGetJSONFromDataObjectList {

        private EtkDisplayFields selectFields;
        private Set<String> selectFieldNames;

        private Comparator<SerializedDBDataObjectAttribute> attributeNameComparator = new Comparator<SerializedDBDataObjectAttribute>() {
            @Override
            public int compare(SerializedDBDataObjectAttribute o1, SerializedDBDataObjectAttribute o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };

        public AbstractGetJSONFromDataObjectList() {
            selectFields = createSelectFields();
            selectFieldNames = new TreeSet<>();
            for (EtkDisplayField field : selectFields.getFields()) {
                selectFieldNames.add(field.getKey().getFieldName());
            }
        }

        public EtkDisplayFields getSelectFields() {
            return selectFields;
        }

        private void clearTimeStampAndRemoveUnwantedAttributes(SerializedDBDataObjectList<SerializedDBDataObject> serializedDataObjectList) {
            if (serializedDataObjectList.getList() == null) {
                return;
            }

            DBExtendedDataTypeProvider extendedDataTypeProviderForTextIds = EtkDataObject.getExtendedDataTypeProviderForTextIds(getProject());
            for (SerializedDBDataObject serializedDBDataObject : serializedDataObjectList.getList()) {
                // Zeitstempel entfernen, damit Vergleiche möglich sind
                serializedDBDataObject.setTimeStamp(null);

                // Ungewünschte Attribute, die nicht in den ursprünglichen selectFields enthalten sind, entfernen und die
                // Attribute nach Namen sortieren, damit Vergleiche möglich sind
                Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
                if (attributes != null) {
                    Set<SerializedDBDataObjectAttribute> correctedAttributes = new TreeSet<>(attributeNameComparator);
                    for (SerializedDBDataObjectAttribute attribute : attributes) {
                        if (selectFieldNames.contains(attribute.getName())) {
                            // Mehrspachige Texte explizit laden (falls noch nicht im SerializedDBDataObjectAttribute enthalten)
                            // über den Aufruf von getLanguagesAndTexts()
                            if (attribute.getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                                attribute.getMultiLanguage().loadAndGetLanguagesAndTexts(extendedDataTypeProviderForTextIds);
                            }
                            correctedAttributes.add(attribute);
                        }
                    }
                    if (correctedAttributes.isEmpty()) {
                        serializedDBDataObject.setAttributes(null);
                    } else {
                        serializedDBDataObject.setAttributes(correctedAttributes);
                    }
                }

                List<SerializedDBDataObjectList<SerializedDBDataObject>> childSerializedDBDataObjectLists = serializedDBDataObject.getCompositeChildren();
                if (childSerializedDBDataObjectLists != null) {
                    for (SerializedDBDataObjectList childSerializedDBDataObjectList : childSerializedDBDataObjectLists) {
                        clearTimeStampAndRemoveUnwantedAttributes(childSerializedDBDataObjectList);
                    }
                }
            }
        }

        protected String getJSON() {
            return getJSON(false);
        }


        protected String getJSON(boolean withTexts) {
            SerializedDBDataObjectList serializedDataObjectList = createDataObjectList().serialize(false, false, false, false, false);
            if (serializedDataObjectList != null) {
                serializedDataObjectList.inheritUserAndDateTime(null, null, true);
                clearTimeStampAndRemoveUnwantedAttributes(serializedDataObjectList);

                if (withTexts) {
                    // Nicht getAsJSON() aufufen, da dadurch die mehrsprachigen Texte bei vorhandener Text-ID entfernt werden
                    // würden, was für die Unittest-Vergleiche aber benötigt wird
                    return serializedDbDataObjectAsJSON.getGenson().serialize(serializedDataObjectList);
                } else {
                    return serializedDbDataObjectAsJSON.getAsJSON(serializedDataObjectList);
                }
            } else {
                return "";
            }
        }

        protected abstract EtkDisplayFields createSelectFields();

        protected abstract EtkDataObjectList createDataObjectList();
    }
}