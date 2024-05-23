/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyDialogHmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementKEMHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacementConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.ChangeSetModificatorImport;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Importer für die DIALOG Ersetzungen und Mitlieferteile am Teilestamm
 */
public class VTNVDataImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    // Felder der DIALOG Ersetzungen und Mitlieferteile am Teilestamm
    public static final String DIALOG_TABLENAME = "VTNV";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    public static final String VTNV_TEIL = "VTNV_TEIL";  // PK
    public static final String VTNV_SDATA = "VTNV_SDATA";  // PK
    public static final String VTNV_VOR_KZ_K = "VTNV_VOR_KZ_K";
    public static final String VTNV_VOR_SNR = "VTNV_VOR_SNR";
    public static final String VTNV_VOR_ET_RFME = "VTNV_VOR_ET_RFME";
    public static final String VTNV_AKT_ET_RFME = "VTNV_AKT_ET_RFME";
    public static final String VTNV_SPERR_KZ = "VTNV_SPERR_KZ";
    public static final String VTNV_IDENT_ANFO = "VTNV_IDENT_ANFO";
    public static final String VTNV_MIT_SNR1 = "VTNV_MIT_SNR1";  // eigene Tabelle
    public static final String VTNV_MIT_MG1 = "VTNV_MIT_MG1";    // eigene Tabelle
    public static final String VTNV_MIT_SNR2 = "VTNV_MIT_SNR2";  // eigene Tabelle
    public static final String VTNV_MIT_MG2 = "VTNV_MIT_MG2";    // eigene Tabelle
    public static final String VTNV_MIT_SNR3 = "VTNV_MIT_SNR3";  // eigene Tabelle
    public static final String VTNV_MIT_MG3 = "VTNV_MIT_MG3";    // eigene Tabelle
    public static final String VTNV_MIT_SNR4 = "VTNV_MIT_SNR4";  // eigene Tabelle
    public static final String VTNV_MIT_MG4 = "VTNV_MIT_MG4";    // eigene Tabelle
    public static final String VTNV_MIT_SNR5 = "VTNV_MIT_SNR5";  // eigene Tabelle
    public static final String VTNV_MIT_MG5 = "VTNV_MIT_MG5";    // eigene Tabelle

    private static final String VTNV_MIT_SNR_PREFIX = "VTNV_MIT_SNR";
    private static final String VTNV_MIT_MG_PREFIX = "VTNV_MIT_MG";
    private static final int MAX_SUFFIX_COUNT = 5;

    private HashMap<String, String> dialogMapping;
    private String tableName;
    private boolean importToDB = true; // sollen die Daten abgespeichert werden?
    private boolean doBufferedSave = true;

    private final iPartsDIALOGPositionsHelper missingDialogPositionsHelper = new iPartsDIALOGPositionsHelper(null);
    private final ObjectInstanceLRUList<HmMSmId, iPartsDIALOGPositionsHelper> dialogPositionsHelperMap = new ObjectInstanceLRUList<>(500, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);
    private final ObjectInstanceLRUList<iPartsDialogBCTEPrimaryKey, ConstructionPLEAndBCTEKeysForKemChain> bcteKeysForKemChainMap = new ObjectInstanceLRUList<>(1000,
                                                                                                                                                                iPartsConst.MAX_CACHE_LIFE_TIME_CORE);
    private GenericEtkDataObjectList dataObjectListForImporter;
    private List<ChangeSetModificator.ChangeSetModificationTask> changeSetModificationTasks;
    private VTNVHelper importHelper;
    // Set zum Sammeln der IDs der bereits importierten Datensätze zur Vermeidung von Primärschlüsselverletzungen
    private Set<iPartsReplaceConstMatId> alreadyImportedRecords;


    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public VTNVDataImporter(EtkProject project) {
        super(project, "!!DIALOG Ersetzungen und Mitlieferteile am Teilestamm (VTNV)",
              new FilesImporterFileListType(TABLE_DA_REPLACE_CONST_MAT, DVTNV_REPLACEMENTS, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        this.tableName = TABLE_DA_REPLACE_CONST_MAT;
        dialogMapping = new HashMap<>();
        dialogMapping.put(FIELD_DRCM_PRE_PART_NO, VTNV_VOR_SNR);   // PK
        dialogMapping.put(FIELD_DRCM_VOR_KZ_K, VTNV_VOR_KZ_K);
        dialogMapping.put(FIELD_DRCM_PRE_RFME, VTNV_VOR_ET_RFME);
        dialogMapping.put(FIELD_DRCM_RFME, VTNV_AKT_ET_RFME);
        dialogMapping.put(FIELD_DRCM_LOCK_FLAG, VTNV_SPERR_KZ);
        dialogMapping.put(FIELD_DRCM_ANFO, VTNV_IDENT_ANFO);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExists = new String[]{ VTNV_TEIL, VTNV_SDATA };
        String[] mustHaveData = new String[]{ VTNV_TEIL, VTNV_SDATA };

        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME)
                   || importer.getTableNames().get(0).equals(tableName)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        dataObjectListForImporter = new GenericEtkDataObjectList();
        changeSetModificationTasks = new DwList<>();
        alreadyImportedRecords = new HashSet<>();
        setBufferedSave(doBufferedSave);
        importHelper = new VTNVHelper(getProject(), dialogMapping, tableName);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        iPartsReplaceConstMatId replaceConstMatId = importHelper.getReplaceConstMatId(importRec);
        if (!StrUtils.isValid(replaceConstMatId.getPartNo())) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültiger Wert für die Teilenummer)",
                                                        String.valueOf(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        // Zur Vermeidung von Primärschlüsselverletzungen die IDs der bereits importierten Records prüfen und ggf. überspringen.
        if (alreadyImportedRecords.contains(replaceConstMatId)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (doppelter Datensatz) "
                                                        + "Teilenummer %2, SDATA %3",
                                                        String.valueOf(recordNo), replaceConstMatId.getPartNo(),
                                                        replaceConstMatId.getsDatA()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        alreadyImportedRecords.add(replaceConstMatId);

        iPartsDataReplaceConstMat dataReplaceConstMat = new iPartsDataReplaceConstMat(getProject(), replaceConstMatId);
        if (!dataReplaceConstMat.existsInDB()) {
            dataReplaceConstMat.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        importHelper.fillOverrideAndFillIncludeParts(dataReplaceConstMat, importRec);

        // VORHER auf Änderung prüfen (inkl. Mitlieferteile), da der Status beim saveToDB zurückgesetzt wird, falls der Datensatz
        // sofort gespeichert wird.
        boolean isUpdateRetailReplacements = (dataReplaceConstMat.isModifiedWithChildren() || dataReplaceConstMat.isNew());

        if (importToDB) {
            saveToDB(dataReplaceConstMat);
        }

        if (isDIALOGDeltaDataImport() && isUpdateRetailReplacements) {
            updateRetailReplacements(importHelper, dataReplaceConstMat);
        }
    }

    /**
     * Aktualisiert alle Retail-Ersetzungen, die sich aus dieser Konstruktionsersetung ergeben haben. Bzw.
     * legt sie an falls sie noch nicht existieren. Wenn die Retail-Ersetzungen nicht geprüft werden müssen,
     * werden sie direkt freigegeben (siehe {@link #autoReleaseReplacement}).
     *
     * @param importHelper
     * @param importedReplaceConstMat
     */
    private void updateRetailReplacements(final VTNVHelper importHelper, iPartsDataReplaceConstMat importedReplaceConstMat) {
        ASUsageHelper asUsageHelper = importHelper.getAsUsageHelper();

        String predecessorPartNo = importedReplaceConstMat.getFieldValue(FIELD_DRCM_PRE_PART_NO);
        String sData = importedReplaceConstMat.getAsId().getsDatA();  //getFieldValue(FIELD_DRCM_SDATA);
        String successorPartNo = importedReplaceConstMat.getAsId().getPartNo(); //getFieldValue(FIELD_DRCM_PART_NO);

        // Alle konkreten Ersetzungen in der Konstruktion zu diesem Datensatz bestimmen
        iPartsDataDialogDataList potentialPredecessors = iPartsDataDialogDataList.loadDialogDataForMatNrAndSDATB(getProject(),
                                                                                                                 predecessorPartNo,
                                                                                                                 sData);
        iPartsDataDialogDataList potentialSuccessors = iPartsDataDialogDataList.loadDialogDataForMatNrAndSDATA(getProject(),
                                                                                                               successorPartNo,
                                                                                                               sData);

        List<iPartsDataPartListEntry> potentialPredecessorPLEs = potentialPredecessors.getAsList().stream()
                .map(dataDialog -> createDummyConstEntry(dataDialog, predecessorPartNo))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<iPartsDataPartListEntry> potentialSuccessorPLEs = potentialSuccessors.getAsList().stream()
                .map(dataDialog -> createDummyConstEntry(dataDialog, successorPartNo))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Potenzielle Ersetzungen ermitteln
        List<iPartsReplacementConst> allConstReplacements = iPartsReplacementHelper.getConstReplacementsForPotentialPLEs(importedReplaceConstMat,
                                                                                                                         true,
                                                                                                                         potentialPredecessorPLEs,
                                                                                                                         potentialSuccessorPLEs);
        for (iPartsReplacementConst replacementConst : allConstReplacements) {
            if (replacementConst.isValid()) {
                // Mitlieferteile an die Konstruktions-Ersetzung kopieren
                iPartsDataIncludeConstMatList includeConstMatList = importedReplaceConstMat.getIncludeConstMats();
                if (includeConstMatList != null) {
                    for (iPartsDataIncludeConstMat dataIncludeConstMat : includeConstMatList) {
                        replacementConst.addIncludePart(new iPartsReplacementConst.IncludePartConstMat(dataIncludeConstMat.getAttributes()));
                    }
                }
                replacementConst.setIncludePartsLoaded(true);
            }
        }

        for (final iPartsReplacementConst replacementConst : allConstReplacements) {
            // Änderungen an den Echtdaten vornehmen
            updateRetailReplacements(getProject(), dataObjectListForImporter, null, asUsageHelper, replacementConst);

            // Änderungen in allen aktiven ChangeSets vormerken. Werden dann zusammen in postImportTask() ausgeführt.
            final iPartsDialogBCTEPrimaryKey predecessorBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(replacementConst.predecessorEntry.getAsId().getKLfdnr());
            final iPartsDialogBCTEPrimaryKey successorBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(replacementConst.successorEntry.getAsId().getKLfdnr());

            Set<String> relevantChangeSetIds = getRelevantChangeSetIds(replacementConst, asUsageHelper, predecessorBCTEKey, successorBCTEKey);

            // Schonmal ein Callback zur aktuellen Konstruktionsersetzung anlegen. Dieses wird in postImportTask() erst aufgerufen,
            // damit dort schon alle echten Datenbankänderungen passiert sind.
            for (String relevantChangeSetId : relevantChangeSetIds) {
                ChangeSetModificator.ChangeSetModificationTask changeSetModificationTask = new ChangeSetModificator.ChangeSetModificationTask(relevantChangeSetId) {
                    @Override
                    public void modifyChangeSet(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                GenericEtkDataObjectList dataObjectListForChangeSet) {
                        deleteOrphanedASReplacements(projectForChangeSet, authorOrderChangeSet, dataObjectListForChangeSet);

                        ASUsageHelper asUsageHelperForChangeSet = new ASUsageHelper(projectForChangeSet);
                        updateRetailReplacements(projectForChangeSet, dataObjectListForChangeSet, authorOrderChangeSet,
                                                 asUsageHelperForChangeSet, replacementConst);
                    }

                    private void deleteOrphanedASReplacements(EtkProject projectForChangeSet,
                                                              iPartsRevisionChangeSet authorOrderChangeSet,
                                                              GenericEtkDataObjectList dataObjectListForChangeSet) {
                        // Alle im ChangeSet gelöschten Stücklisteneinträge für die KEM-Kette des Vorgängers bestimmen
                        Set<iPartsDialogBCTEPrimaryKey> predecessorBCTEKeysForKemChain = getBCTEKeysForKemChain(predecessorBCTEKey, null);
                        List<EtkDataPartListEntry> deletedPredecessorsInChangeset = new DwList<>();
                        for (iPartsDialogBCTEPrimaryKey predecessorBCTEKeyForKemChain : predecessorBCTEKeysForKemChain) {
                            List<EtkDataPartListEntry> deletedPredecessorsForKemChain = ChangeSetModificator.getDeletedASPartListEntriesForBCTEKey(projectForChangeSet,
                                                                                                                                                   authorOrderChangeSet,
                                                                                                                                                   predecessorBCTEKeyForKemChain);
                            if (deletedPredecessorsForKemChain != null) {
                                deletedPredecessorsInChangeset.addAll(deletedPredecessorsForKemChain);
                            }
                        }

                        // Und jetzt für die Nachfolger
                        Set<iPartsDialogBCTEPrimaryKey> successorBCTEKeysForKemChain = getBCTEKeysForKemChain(successorBCTEKey, null);
                        List<EtkDataPartListEntry> deletedSuccessorsInChangeset = new DwList<>();
                        for (iPartsDialogBCTEPrimaryKey successorBCTEKeyForKemChain : successorBCTEKeysForKemChain) {
                            List<EtkDataPartListEntry> deletedSuccessorsForKemChain = ChangeSetModificator.getDeletedASPartListEntriesForBCTEKey(projectForChangeSet,
                                                                                                                                                 authorOrderChangeSet,
                                                                                                                                                 successorBCTEKeyForKemChain);
                            if (deletedSuccessorsForKemChain != null) {
                                deletedSuccessorsInChangeset.addAll(deletedSuccessorsForKemChain);
                            }
                        }

                        if (!deletedPredecessorsInChangeset.isEmpty() || !deletedSuccessorsInChangeset.isEmpty()) {
                            // Vorgänger(stand) oder Nachfolger(stand) wurde gelöscht. Weil wir im ChangeSet sind, muss
                            // hier die im Import neu erzeugte Ersetzung gelöscht werden. Die Stände der Ersetzung,
                            // die bereits in der Datenbank existierten, als der Vorgänger(stand)/Nachfolger(stand)
                            // gelöscht wurde, sind nämlich bereits gelöscht im ChangeSet. Ansonsten würde die gerade
                            // importierte Ersetzung bei der Freigabe des Autorenauftrags übrig bleiben, obwohl ihr
                            // Vorgänger(stand)/Nachfolger(stand)gar nicht mehr existiert.
                            // Außerdem müssen evtl. vorhandene dazugehörige Einträge in DA_DIALOG_CHANGES gelöscht werden.
                            replacePartLoop:
                            for (Object dataObject : dataObjectListForImporter) {
                                if (dataObject instanceof iPartsDataReplacePart) {
                                    iPartsDataReplacePart replacePart = (iPartsDataReplacePart)dataObject;
                                    Map<AssemblyId, Boolean> assemblyIsPSKMap = new HashMap<>();
                                    for (EtkDataPartListEntry deletedPredecessor : deletedPredecessorsInChangeset) {
                                        // Bei Positionen in PSK Produkten werden keine Updates auf die Ersetzungen
                                        // gemacht, d.h. diese Positionen können hier übersprungen werden
                                        if (ASUsageHelper.isPSKAssembly(projectForChangeSet, deletedPredecessor.getOwnerAssemblyId(), assemblyIsPSKMap)) {
                                            continue;
                                        }
                                        if (replacePart.getPredecessorPartListEntryId().equals(deletedPredecessor.getAsId())) {
                                            deleteOrphanedASReplacement(replacePart, deletedPredecessor, authorOrderChangeSet,
                                                                        dataObjectListForChangeSet);
                                            continue replacePartLoop; // Einmal löschen reicht...
                                        }
                                    }
                                    for (EtkDataPartListEntry deletedSuccessor : deletedSuccessorsInChangeset) {
                                        // Bei Positionen in PSK Produkten werden keine Updates auf die Ersetzungen
                                        // gemacht, d.h. diese Positionen können hier übersprungen werden
                                        if (ASUsageHelper.isPSKAssembly(projectForChangeSet, deletedSuccessor.getOwnerAssemblyId(), assemblyIsPSKMap)) {
                                            continue;
                                        }
                                        if (replacePart.getSuccessorPartListEntryId().equals(deletedSuccessor.getAsId())) {
                                            deleteOrphanedASReplacement(replacePart, deletedSuccessor, authorOrderChangeSet,
                                                                        dataObjectListForChangeSet);
                                            continue replacePartLoop; // Einmal löschen reicht...
                                        }
                                    }
                                }
                            }
                        }
                    }

                    private void deleteOrphanedASReplacement(iPartsDataReplacePart replacePartToBeDeleted, EtkDataPartListEntry deletedPartListEntry,
                                                             iPartsRevisionChangeSet authorOrderChangeSet, GenericEtkDataObjectList dataObjectListForChangeSet) {
                        dataObjectListForChangeSet.delete(replacePartToBeDeleted, true, DBActionOrigin.FROM_EDIT);

                        // Das Haupt-Projekt des Importers benutzen für das Laden der retailAssembly (nicht projectForChangeSet),
                        // da die Stückliste ohne aktives ChangeSet geladen werden muss, um den gelöschten Vorgänger bzw.
                        // Nachfolger zu finden
                        EtkDataAssembly retailAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), deletedPartListEntry.getOwnerAssemblyId());

                        // DA_DIALOG_CHANGES-Eintrag für den Vorgänger löschen
                        EtkDataPartListEntry predecessorPLE = retailAssembly.getPartListEntryFromKLfdNrUnfiltered(replacePartToBeDeleted.getPredecessorPartListEntryId().getKLfdnr());
                        if (predecessorPLE != null) {
                            iPartsDataDIALOGChange dialogChange = importHelper.createDialogChange(replacePartToBeDeleted,
                                                                                                  predecessorPLE,
                                                                                                  authorOrderChangeSet);
                            dataObjectListForChangeSet.delete(dialogChange, true, DBActionOrigin.FROM_EDIT);
                        }

                        // DA_DIALOG_CHANGES-Eintrag für den Nachfolger löschen
                        EtkDataPartListEntry successorPLE = retailAssembly.getPartListEntryFromKLfdNrUnfiltered(replacePartToBeDeleted.getSuccessorPartListEntryId().getKLfdnr());
                        if (successorPLE != null) {
                            iPartsDataDIALOGChange dialogChange = importHelper.createDialogChange(replacePartToBeDeleted,
                                                                                                  successorPLE,
                                                                                                  authorOrderChangeSet);
                            dataObjectListForChangeSet.delete(dialogChange, true, DBActionOrigin.FROM_EDIT);
                        }
                    }
                };
                changeSetModificationTasks.add(changeSetModificationTask);
            }
        }
    }

    /**
     * Liefert die Ids aller ChangeSets, in denen mindestens eine AS-Verwendung des übergebenen Vorgängers oder Nachfolgers
     * der Konstruktionsersetzung vorkommt. In diesen ChangeSets kann es notwendig sein, dass eine komplett neue AS-Ersetzung aus der
     * Konstruktionsersetzung erzeugt wird. Außerdem werden alle Ids von ChangeSets zurückgeliefert, in denen möglicherweise eine AS-Ersetzung vorkommt,
     * die durch den Import geupdated werden muss. Das muss separat gemacht werden, da in diesen ChangeSets nicht unbedingt auch
     * der Vorgänger oder Nachfolger bearbeitet wurde und die Info damit nicht direkt über das FELD DCE_SOURCE_GUID ausgelesen werden kann.
     * Da die {@link iPartsReplacePartId} der AS-Ersetzungen leider nur die {@link PartListEntryId} des Vorgängers enthält, ist nicht 100% sicher,
     * ob dieses ChangeSet auch wirklich die Ersetzung in Bearbeitung hat, die geupdated werden soll, da diese eventuell einen Nachfolger mit einer
     * anderen Source-GUID hat, als die der Konstruktionsersetzung. In diesem seltenen Fall wäre das ChangeSet nur unnötig geladen und es würden
     * sich in dessen {@link ChangeSetModificator.ChangeSetModificationTask} keine Änderungen am ChangeSet ergeben.
     *
     * @param replacementConst
     * @param asUsageHelper
     * @param predecessorBCTEKey
     * @param successorBCTEKey
     * @return
     */
    private Set<String> getRelevantChangeSetIds(iPartsReplacementConst replacementConst, ASUsageHelper asUsageHelper,
                                                iPartsDialogBCTEPrimaryKey predecessorBCTEKey, iPartsDialogBCTEPrimaryKey successorBCTEKey) {
        // Nicht nur gelöschte oder neue Stücklisteneinträge betrachten, sondern auch veränderte, weil die "PEM auswerten"-Flags
        // vom Autor verändert worden sein könnten
        Set<String> relevantChangeSetIds = new TreeSet<>(); // TreeSet für bessere Reproduzierbarkeit bei Tests

        // Alle BCTE-Schlüssel für die Vorgängerstände und Nachfolgerstände bestimmen und dafür die relevanten ChangeSets suchen
        relevantChangeSetIds.addAll(getRelevantChangeSetIdsForKemChain(predecessorBCTEKey, replacementConst.predecessorEntry, asUsageHelper));
        relevantChangeSetIds.addAll(getRelevantChangeSetIdsForKemChain(successorBCTEKey, replacementConst.successorEntry, asUsageHelper));

        return relevantChangeSetIds;
    }

    /**
     * Liefert die Ids aller ChangeSets, in denen mindestens eine AS-Verwendung des übergebenen Konstruktions-Stücklisteneintrags
     * inkl. dessen KEM-Kette vorkommt.
     *
     * @param bcteKey
     * @param replacementConstructionPLE
     * @param asUsageHelper
     * @return
     */
    private Set<String> getRelevantChangeSetIdsForKemChain(iPartsDialogBCTEPrimaryKey bcteKey, iPartsDataPartListEntry replacementConstructionPLE,
                                                           ASUsageHelper asUsageHelper) {
        Set<String> relevantChangeSetIds = new TreeSet<>();
        Set<iPartsDialogBCTEPrimaryKey> bcteKeysForKemChain = getBCTEKeysForKemChain(bcteKey, replacementConstructionPLE);
        for (iPartsDialogBCTEPrimaryKey bcteKeyForKemChain : bcteKeysForKemChain) {
            Set<String> changeSetIds = asUsageHelper.getChangeSetIdsForPartListEntriesUsedInActiveChangeSets(bcteKeyForKemChain);
            if (changeSetIds != null) {
                relevantChangeSetIds.addAll(changeSetIds);
            }
        }

        return relevantChangeSetIds;
    }

    /**
     * Liefert alle BCTE-Schlüssel für die KEM-Kette zum übergebenen Konstruktions-Stücklisteneintrag mit dem übergebenen
     * BCTE-Schlüssel zurück.
     *
     * @param bcteKey
     * @param replacementConstructionPLE Falls nicht {@code null} werden die Daten dieses Konstruktions-Stücklisteneintrags
     *                                   mit den Daten des geladenen Konstruktions-Stücklisteneintrags aus der DIALOG-Stückliste
     *                                   inkl. Datumsangeben von der KEM-Kette überschrieben
     * @return
     */
    private Set<iPartsDialogBCTEPrimaryKey> getBCTEKeysForKemChain(iPartsDialogBCTEPrimaryKey bcteKey, iPartsDataPartListEntry replacementConstructionPLE) {
        ConstructionPLEAndBCTEKeysForKemChain constructionPLEAndBCTEKeysForKemChain = bcteKeysForKemChainMap.get(bcteKey);
        if (constructionPLEAndBCTEKeysForKemChain == null) {
            HmMSmId hmMSmId = bcteKey.getHmMSmId();
            iPartsDIALOGPositionsHelper dialogPositionsHelper = dialogPositionsHelperMap.get(hmMSmId);
            if (dialogPositionsHelper == null) {
                String virtuelIdString = iPartsVirtualNode.getVirtualIdString(hmMSmId);
                AssemblyId constructionAssemblyId = new AssemblyId(virtuelIdString, "");

                // Das Haupt-Projekt des Importers kann hier benutzt werden, da für die Konstruktions-Stücklisten die
                // ChangeSets irrelevant sind und daher nicht berücksichtigt werden müssen
                EtkDataAssembly constructionAssembly = EtkDataObjectFactory.createDataAssembly(getProject(),
                                                                                               constructionAssemblyId,
                                                                                               false);

                if (constructionAssembly.existsInDB()) {
                    dialogPositionsHelper = new iPartsDIALOGPositionsHelper(constructionAssembly.getPartListUnfiltered(null));
                    dialogPositionsHelperMap.put(hmMSmId, dialogPositionsHelper);
                } else {
                    getMessageLog().fireMessage(TranslationHandler.translate("!!DIALOG-Stückliste fehlt für HM/M/SM-Knoten %1",
                                                                             hmMSmId.toString("/")),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    dialogPositionsHelper = missingDialogPositionsHelper;
                    dialogPositionsHelperMap.put(hmMSmId, missingDialogPositionsHelper);
                }
            }

            constructionPLEAndBCTEKeysForKemChain = new ConstructionPLEAndBCTEKeysForKemChain();
            constructionPLEAndBCTEKeysForKemChain.bcteKeysForKemChain = new TreeSet<>();
            if (dialogPositionsHelper != missingDialogPositionsHelper) {
                EtkDataPartListEntry constructionPLE = dialogPositionsHelper.getPositionVariantByBCTEKey(bcteKey);
                if (constructionPLE != null) {
                    Set<EtkDataPartListEntry> constPLEsForKemChain = EditConstructionToRetailHelper.calculateMinMaxKEMDatesWithoutCache(constructionPLE,
                                                                                                                                        dialogPositionsHelper);
                    for (EtkDataPartListEntry constPLEForKemChain : constPLEsForKemChain) {
                        iPartsDialogBCTEPrimaryKey bcteKeyForKemChain = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(constPLEForKemChain);
                        if (bcteKeyForKemChain != null) {
                            constructionPLEAndBCTEKeysForKemChain.bcteKeysForKemChain.add(bcteKeyForKemChain);
                        }
                    }

                    constructionPLEAndBCTEKeysForKemChain.constructionPartListEntry = constructionPLE;
                }
            } else {
                // DIALOG-Stückliste nicht gefunden -> nur nach konkretem BCTE-Schlüssel suchen
                constructionPLEAndBCTEKeysForKemChain.bcteKeysForKemChain.add(bcteKey);
            }
            bcteKeysForKemChainMap.put(bcteKey, constructionPLEAndBCTEKeysForKemChain);
        }

        // Alle Daten vom Konstruktions-Stücklisteneintrag mit berechnete KEM-Kette an den übergebenen Konstruktions-Stücklisteneintrag
        // übertragen
        if ((replacementConstructionPLE != null) && (constructionPLEAndBCTEKeysForKemChain.constructionPartListEntry != null)) {
            // Das Haupt-Projekt des Importers kann hier benutzt werden, da für die Konstruktions-Stücklisten die
            // ChangeSets irrelevant sind und daher nicht berücksichtigt werden müssen
            replacementConstructionPLE.assignRecursively(getProject(), constructionPLEAndBCTEKeysForKemChain.constructionPartListEntry,
                                                         DBActionOrigin.FROM_DB);

            replacementConstructionPLE.setOwnerAssembly(constructionPLEAndBCTEKeysForKemChain.constructionPartListEntry.getOwnerAssembly());
        }

        return constructionPLEAndBCTEKeysForKemChain.bcteKeysForKemChain;
    }

    private List<iPartsDataReplacePart> updateRetailReplacements(EtkProject projectForChangeSet, GenericEtkDataObjectList resultingDataObjectList,
                                                                 iPartsRevisionChangeSet authorOrderChangeSet, ASUsageHelper asUsageHelper,
                                                                 iPartsReplacementConst replacementConst) {
        // Jeweils zum Vorgänger und zum Nachfolger der Konstruktion inkl. aller Vorgänger- und Nachfolgerstände der KEM-Kette
        // schauen, ob es diesen auch im AS gibt.
        // Falls einer von beiden nicht im AS vorkommt, kann es auch nie eine AS-Ersetzung geben.

        // Zuerst die Vorgänger
        final String predecessorGUID = replacementConst.predecessorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
        final iPartsDialogBCTEPrimaryKey predecessorBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(predecessorGUID);
        if (predecessorBCTEKey == null) {
            return null;
        }

        final List<EtkDataPartListEntry> predecessorASEntries = new DwList<>();
        Set<iPartsDialogBCTEPrimaryKey> predecessorBCTEKeysForKemChain = getBCTEKeysForKemChain(predecessorBCTEKey, replacementConst.predecessorEntry);
        for (iPartsDialogBCTEPrimaryKey predecessorBCTEKeyForKemChain : predecessorBCTEKeysForKemChain) {
            // Hier nur die Positionen bestimmen, die NICHT in PSK Produkten/Stücklisten existieren
            List<EtkDataPartListEntry> predecessorASEntriesForKemChain = asUsageHelper.getPartListEntriesUsedInASWithoutPSKProducts(predecessorBCTEKeyForKemChain, false); // bei PSK dürfen die Ersetzungen nicht erzeugt werden
            if (predecessorASEntriesForKemChain != null) {
                predecessorASEntries.addAll(predecessorASEntriesForKemChain);
            }
        }

        if (predecessorASEntries.isEmpty()) {
            return null;
        }

        // Und jetzt die Nachfolger
        final String successorGUID = replacementConst.successorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
        final iPartsDialogBCTEPrimaryKey successorBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(successorGUID);
        if (successorBCTEKey == null) {
            return null;
        }

        final List<EtkDataPartListEntry> successorASEntries = new DwList<>();
        Set<iPartsDialogBCTEPrimaryKey> successorBCTEKeysForKemChain = getBCTEKeysForKemChain(successorBCTEKey, replacementConst.successorEntry);
        for (iPartsDialogBCTEPrimaryKey successorBCTEKeyForKemChain : successorBCTEKeysForKemChain) {
            // Hier nur die Positionen bestimmen, die NICHT in PSK Produkten/Stücklisten existieren
            List<EtkDataPartListEntry> successorASEntriesForKemChain = asUsageHelper.getPartListEntriesUsedInASWithoutPSKProducts(successorBCTEKeyForKemChain, false); // bei PSK dürfen die Ersetzungen nicht erzeugt werden
            if (successorASEntriesForKemChain != null) {
                successorASEntries.addAll(successorASEntriesForKemChain);
            }
        }

        if (successorASEntries.isEmpty()) {
            return null;
        }

        Map<AssemblyId, List<EtkDataPartListEntry>> asAssemblyToPredeccessorsInChangeSetMap = groupASPartListEntriesByAssembly(predecessorASEntries);
        Map<AssemblyId, List<EtkDataPartListEntry>> asAssemblyToSuccessorsInChangeSetMap = groupASPartListEntriesByAssembly(successorASEntries);

        VTNVHelper importHelperForChangeSet = new VTNVHelper(projectForChangeSet, dialogMapping, tableName);
        return createAndAddAllRetailReplacements(projectForChangeSet, importHelperForChangeSet, replacementConst, asAssemblyToPredeccessorsInChangeSetMap,
                                                 asAssemblyToSuccessorsInChangeSetMap, resultingDataObjectList, authorOrderChangeSet);
    }

    private List<iPartsDataReplacePart> createAndAddAllRetailReplacements(EtkProject project, VTNVHelper importHelper, iPartsReplacementConst replacementConst,
                                                                          Map<AssemblyId, List<EtkDataPartListEntry>> asAssemblyToPredeccessorsMap,
                                                                          Map<AssemblyId, List<EtkDataPartListEntry>> asAssemblyToSuccessorsMap,
                                                                          GenericEtkDataObjectList resultDataObjectList,
                                                                          iPartsRevisionChangeSet authorOrderChangeSet) {
        Map<AssemblyId, iPartsDataReplacePartList> replacementsRetailForAssemblyMap = new HashMap<>();
        Map<AssemblyId, iPartsDataIncludePartList> includePartsRetailForAssemblyMap = new HashMap<>();
        List<iPartsDataReplacePart> allNewRetailReplacements = new DwList<>();
        for (Map.Entry<AssemblyId, List<EtkDataPartListEntry>> destAssembly : asAssemblyToPredeccessorsMap.entrySet()) {
            AssemblyId destAssemblyId = destAssembly.getKey();
            List<EtkDataPartListEntry> existingPredecessorsRetail = destAssembly.getValue();
            List<EtkDataPartListEntry> existingSuccessorsRetail = asAssemblyToSuccessorsMap.get(destAssemblyId);

            // Falls nur Vorgänger in den KG/TU übernommen wurde, besteht keine Ersetzung und es kann zum nächsten Vorgänger
            // -Datensatz übergegangen werden
            if (existingSuccessorsRetail == null) {
                continue;
            }

            // Hier haben wir die konkrete zu übernehmende Konstruktionsersetzung und eine AS-Stückliste in der der Vorgänger
            // und der Nachfolger der konkreten Konstruktionsersetzung vorkommen. Zwischen diesen muss eine Retail-Ersetzung
            // angelegt/aktualisiert werden. SDATA/SDATB oder die AA braucht man also nicht mehr prüfen, da sonst auch keine
            // Konstruktionsersetzung existiert hätte. Wir können so tun, also ob gerade Stücklisteneinträge in AS übernommen wurden
            // und jetzt die Konstruktionsersetzung übernommen wird. Also einfach die Methode aus der Übernahme verwenden.
            // Damit ist auch sichergestellt, dass das Ergebnis bei manueller Übernahme gleich dem der Delta-Ladung ist.
            // Da es mehrere Ersetzungen mit identischem Vorgänger geben kann, die Daten aber erst in postImportTask() in
            // die DB geschrieben werden, müssen wir uns die Ersetzungen (und praktischerweise auch die Mitlieferteile) in
            // Maps pro AssemblyId merken, damit keine doppelten Sequenznummern für die Ersetzungen verwendet werden.
            iPartsDataReplacePartList replacementsRetailForAssembly = replacementsRetailForAssemblyMap.get(destAssemblyId);
            if (replacementsRetailForAssembly == null) {
                replacementsRetailForAssembly = iPartsDataReplacePartList.loadReplacementsForAssembly(project, destAssemblyId);
                replacementsRetailForAssemblyMap.put(destAssemblyId, replacementsRetailForAssembly);
            }
            iPartsDataIncludePartList includePartsRetailForAssembly = includePartsRetailForAssemblyMap.get(destAssemblyId);
            if (includePartsRetailForAssembly == null) {
                includePartsRetailForAssembly = iPartsDataIncludePartList.loadIncludePartsForAssembly(project, destAssemblyId);
                includePartsRetailForAssemblyMap.put(destAssemblyId, includePartsRetailForAssembly);
            }

            // Basierend auf existingPredecessorsRetail und existingSuccessorsRetail müssen wir die jeweils besten Vorgängerstände
            // und Nachfolgerstände pro Hotspot suchen und für diese die Ersetzungen anlegen bzw. aktualisieren
            DBDataObjectList<EtkDataPartListEntry> dummyPartList = new DBDataObjectList<>();
            Map<String, List<EtkDataPartListEntry>> dummyPartListSourceGUIDMap = new HashMap<>();
            addPartListEntriesForBestKemChainEntryCalculation(existingPredecessorsRetail, dummyPartList, dummyPartListSourceGUIDMap);
            addPartListEntriesForBestKemChainEntryCalculation(existingSuccessorsRetail, dummyPartList, dummyPartListSourceGUIDMap);
            iPartsReplacementKEMHelper replacementHelper = new iPartsReplacementKEMHelper(dummyPartList);
            Map<String, EtkDataPartListEntry> hotspotToBestPredecessorRetailMap =
                    replacementHelper.getBestPredecessorForHotspotMap(replacementConst, dummyPartListSourceGUIDMap);
            Map<String, EtkDataPartListEntry> hotspotToBestSuccessorRetailMap =
                    replacementHelper.getBestSuccessorForHotspotMap(replacementConst, dummyPartListSourceGUIDMap);

            // Die Hotspots spielen hier keine Rolle, weil hier keine Ersetzungen auf bessere Vorgängerstände oder Nachfolgerstände
            // verschoben werden müssen, sondern auf den existierenden Ständen (oder bei neuen Ersetzungen komplett neu)
            // die Ersetzungen nur aktualisiert bzw. neu angelegt werden müssen -> flache Listen erstellen
            List<iPartsDataReplacePart> allNewReplacementsForAssembly = createAndAddAllRetailReplacements(project, importHelper,
                                                                                                          replacementConst,
                                                                                                          replacementsRetailForAssembly,
                                                                                                          includePartsRetailForAssembly,
                                                                                                          hotspotToBestPredecessorRetailMap.values(),
                                                                                                          hotspotToBestSuccessorRetailMap.values(),
                                                                                                          resultDataObjectList,
                                                                                                          authorOrderChangeSet);
            allNewRetailReplacements.addAll(allNewReplacementsForAssembly);
        }
        return allNewRetailReplacements;
    }

    private void addPartListEntriesForBestKemChainEntryCalculation(List<EtkDataPartListEntry> partListEntries,
                                                                   DBDataObjectList<EtkDataPartListEntry> dummyPartList,
                                                                   Map<String, List<EtkDataPartListEntry>> dummyPartListSourceGUIDMap) {
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            dummyPartList.add(partListEntry, DBActionOrigin.FROM_DB);
            String sourceGUID = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
            List<EtkDataPartListEntry> dummyPartListEntriesForSourceGUID = dummyPartListSourceGUIDMap.get(sourceGUID);
            if (dummyPartListEntriesForSourceGUID == null) {
                dummyPartListEntriesForSourceGUID = new DwList<>();
                dummyPartListSourceGUIDMap.put(sourceGUID, dummyPartListEntriesForSourceGUID);
            }
            dummyPartListEntriesForSourceGUID.add(partListEntry);
        }
    }

    /**
     * Erzeugt aus einer Konstruktionsersetzung alle Retailersetzungen und legt jeweils für Vorgänger und Nachfolger einer Retailersetzung
     * einen Eintrag in DA_DIALOG_CHANGES an, falls deren Status nicht "freigegeben" ist. Die Einträge in DA_DIALOG_CHANGES werden innerhalb
     * vom ChangeSet angelegt, da nur der Autor dessen Autorenauftrag gerade aktiv ist, auch den geänderten Geschäftsfall in der Konstruktion
     * und den Änderungsgrund im AS sehen darf, da nur er die AS-Ersetzung in Bearbeitung hat.
     *
     * @param project
     * @param importHelper
     * @param replacementConst
     * @param replacementsRetail
     * @param includePartsRetail
     * @param existingPredecessorsRetail
     * @param existingSuccessorsRetail
     * @param resultDataObjectList
     * @param authorOrderChangeSet       falls {@code null} werden die Einträge in DA_DIALOG_CHANGES mit leerem DDC_CHANGE_SET_GUID
     *                                   angelegt. Andernfalls wird die ChangeSet-GUID hineingeschreiben. In dem Fall muss die übergebene
     *                                   Data-Object-Liste die des übergebenen ChangeSets sein.
     * @return Alle in diesem Methodenaufruf neu erzeugten Retail-Ersetzungen
     */
    public List<iPartsDataReplacePart> createAndAddAllRetailReplacements(EtkProject project, VTNVHelper importHelper,
                                                                         iPartsReplacementConst replacementConst,
                                                                         iPartsDataReplacePartList replacementsRetail,
                                                                         iPartsDataIncludePartList includePartsRetail,
                                                                         Collection<EtkDataPartListEntry> existingPredecessorsRetail,
                                                                         Collection<EtkDataPartListEntry> existingSuccessorsRetail,
                                                                         GenericEtkDataObjectList resultDataObjectList,
                                                                         iPartsRevisionChangeSet authorOrderChangeSet) {
        List<iPartsDataReplacePart> allNewRetailReplacements = new DwList<>();
        for (EtkDataPartListEntry successorRetail : existingSuccessorsRetail) {
            // Entsprechende Vorgänger sind in Retail-Stückliste enthalten --> für jeden existierenden Eintrag eine Ersetzung erzeugen
            for (EtkDataPartListEntry predecessorRetail : existingPredecessorsRetail) {
                // Ersetzungs- und Mitlieferteile-Datenobjekte erstellen, die am Ende in der DB gespeichert werden.
                iPartsDataReplacePart newRetailDataReplacement = EditConstructionToRetailHelper.createAndAddRetailReplacement(project,
                                                                                                                              replacementsRetail,
                                                                                                                              includePartsRetail,
                                                                                                                              predecessorRetail,
                                                                                                                              successorRetail,
                                                                                                                              replacementConst);
                if (newRetailDataReplacement != null) {
                    boolean isReleased = autoReleaseReplacement(replacementsRetail, predecessorRetail, successorRetail, newRetailDataReplacement);
                    if (!isReleased) {
                        iPartsDataDIALOGChange dialogChange = importHelper.createDialogChange(newRetailDataReplacement, predecessorRetail, authorOrderChangeSet);
                        resultDataObjectList.add(dialogChange, DBActionOrigin.FROM_DB);
                        dialogChange = importHelper.createDialogChange(newRetailDataReplacement, successorRetail, authorOrderChangeSet);
                        resultDataObjectList.add(dialogChange, DBActionOrigin.FROM_DB);
                    }
                    allNewRetailReplacements.add(newRetailDataReplacement);
                }
            }
        }
        // FROM_DB verwenden, damit das hinzufügen zur Liste (die nur zum Sammeln dient) allein nicht isModified ergibt.
        resultDataObjectList.addAll(replacementsRetail, true, true, DBActionOrigin.FROM_DB);
        resultDataObjectList.addAll(includePartsRetail, true, true, DBActionOrigin.FROM_DB);
        resultDataObjectList.addAll(existingPredecessorsRetail, true, DBActionOrigin.FROM_DB);
        resultDataObjectList.addAll(existingSuccessorsRetail, true, DBActionOrigin.FROM_DB);
        return allNewRetailReplacements;
    }

    /**
     * @param replacementsRetail
     * @param predecessorRetail
     * @param successorRetail
     * @param newRetailDataReplacement
     */
    private boolean autoReleaseReplacement(iPartsDataReplacePartList replacementsRetail, EtkDataPartListEntry predecessorRetail,
                                           EtkDataPartListEntry successorRetail, iPartsDataReplacePart newRetailDataReplacement) {
        // erst einmal wird davon ausgegangen, dass nicht freigegeben werden kann (isFinalReleaseState = false).
        iPartsReplacementHelper.updateStates(replacementsRetail, newRetailDataReplacement, false);

        // PEM-ab/PEM-bis nochmal durchrechnen. Dafür nur die bisher freigegebenen und den neuen Datensatz
        // betrachten, der im Nachgang evtl. freigegeben wird.
        Set<String> validStates = new HashSet<>();
        validStates.add(iPartsDataReleaseState.RELEASED.getDbValue());
        validStates.add(iPartsDataReleaseState.NEW.getDbValue());

        boolean oldPEMTo = predecessorRetail.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED);
        List<iPartsDataReplacePart> successorReplacementsOfPredecessor = replacementsRetail.getSuccessorsFromList(predecessorRetail.getAsId());
        filterByState(successorReplacementsOfPredecessor, validStates);
        boolean newPEMTo = iPartsReplacementHelper.calculateEvalPEMTo(successorReplacementsOfPredecessor);
        if (!oldPEMTo && newPEMTo) {
            return false;
        }

        boolean oldPEMFrom = successorRetail.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED);
        List<iPartsDataReplacePart> predecessorReplacementsOfSuccessor = replacementsRetail.getPredecessorsFromList(successorRetail.getAsId());
        filterByState(predecessorReplacementsOfSuccessor, validStates);
        boolean newPEMFrom = iPartsReplacementHelper.calculateEvalPEMFrom(predecessorReplacementsOfSuccessor);
        if (!oldPEMFrom && newPEMFrom) {
            return false;
        }

        // Die PEM-ab/bis-Auswerten Flags sind gleichgeblieben oder nur deaktiviert worden. Es kann also freigegeben
        // werden und die aktualisierten PEM-ab/bis-Auswerten Flags können übernommen werden.
        iPartsReplacementHelper.updateStates(replacementsRetail, newRetailDataReplacement, true);
        return true;
    }

    private void filterByState(List<iPartsDataReplacePart> replacements, Set<String> validStates) {
        replacements.removeIf(replacement -> !validStates.contains(replacement.getFieldValue(FIELD_DRP_STATUS)));
    }

    /**
     * Erzeugt einen einzelnen Dummy-Konstruktionsstücklisteneintrag aus den Konstruktionsdaten mit der übergenenen Materialnummer
     *
     * @param dialogData
     * @param matNr
     * @return
     */
    private iPartsDataPartListEntry createDummyConstEntry(iPartsDataDialogData dialogData, String matNr) {
        iPartsDialogBCTEPrimaryKey dialogGuid = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogData.getFieldValue(FIELD_DD_GUID));
        if (dialogGuid == null) {
            return null;
        }
        iPartsSeriesId seriesId = new iPartsSeriesId(dialogGuid.seriesNo);
        iPartsVirtualNode seriesNode = new iPartsVirtualNode(iPartsNodeType.DIALOG_HMMSM, seriesId);
        iPartsVirtualNode hmMSmNode = new iPartsVirtualNode(iPartsNodeType.HMMSM, dialogGuid.getHmMSmId());
        DwList<iPartsVirtualNode> virtualNodesPath = new DwList<>();
        virtualNodesPath.add(seriesNode);
        virtualNodesPath.add(hmMSmNode);
        iPartsAssemblyId constAssemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(seriesNode, hmMSmNode), "");

        // Das Haupt-Projekt des Importers kann hier benutzt werden, da für die Konstruktions-Stücklisten die
        // ChangeSets irrelevant sind und daher nicht berücksichtigt werden müssen
        iPartsVirtualAssemblyDialogHmMSm constructionAssembly = new iPartsVirtualAssemblyDialogHmMSm(getProject(), virtualNodesPath,
                                                                                                     constAssemblyId);

        DBDataObjectAttributes predecessorMaterialAttributes = new DBDataObjectAttributes();
        predecessorMaterialAttributes.addField(FIELD_M_MATNR, matNr,
                                               false, DBActionOrigin.FROM_DB);
        predecessorMaterialAttributes.addField(FIELD_M_VER, "",
                                               false, DBActionOrigin.FROM_DB);
        predecessorMaterialAttributes.addField(FIELD_M_BESTNR, matNr,
                                               false, DBActionOrigin.FROM_DB);
        return (iPartsDataPartListEntry)constructionAssembly.createDialogEntry(dialogData.getAttributes(), predecessorMaterialAttributes);
    }

    private Map<AssemblyId, List<EtkDataPartListEntry>> groupASPartListEntriesByAssembly(List<EtkDataPartListEntry> asPartListEntries) {
        Map<AssemblyId, List<EtkDataPartListEntry>> asAssemblyToPartListEntryMap = new TreeMap<>();
        for (EtkDataPartListEntry asPartListEntry : asPartListEntries) {
            AssemblyId ownerAssemblyId = asPartListEntry.getOwnerAssemblyId();
            List<EtkDataPartListEntry> partListEntriesOfASAssembly = asAssemblyToPartListEntryMap.get(ownerAssemblyId);
            if (partListEntriesOfASAssembly == null) {
                partListEntriesOfASAssembly = new DwList<>();
                asAssemblyToPartListEntryMap.put(ownerAssemblyId, partListEntriesOfASAssembly);
            }
            partListEntriesOfASAssembly.add(asPartListEntry);
        }
        return asAssemblyToPartListEntryMap;
    }

    @Override
    public void postImportTask() {
        if (!isCancelled()) {
            if (!dataObjectListForImporter.isEmptyIncludingDeletedList() && dataObjectListForImporter.isModifiedWithChildren()) {
                getMessageLog().fireMessage(translateForLog("!!Es ergaben sich Änderungen in AS-Stücklisten. " +
                                                            "Automatische Änderungen in den AS-Stücklisten werden gespeichert..."),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                if (importToDB) {
                    iPartsRevisionChangeSet changeSet = saveToTechnicalChangeSet(dataObjectListForImporter, iPartsConst.TECHNICAL_USER_DIALOG_DELTA_SUPPLY);
                    if (changeSet != null) {
                        getMessageLog().fireMessage(translateForLog("!!AS-Änderungen wurden gespeichert. Änderungsset-ID: %1",
                                                                    changeSet.getChangeSetId().getGUID()),
                                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    }
                }
            } else {
                getMessageLog().fireMessage(translateForLog("!!Es ergaben sich keine Änderungen in AS-Stücklisten. " +
                                                            "Keine automatischen Änderungen in den AS-Stücklisten nötig."),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
        }
        // hier wird in die DB gespeichert ...
        super.postImportTask();

        if (!isCancelled()) {
            try {
                // Bisherige Transaktion explizit committen, damit mit den Daten in den simulierten ChangeSets gearbeitet werden kann
                getProject().getDbLayer().commit();
                getProject().getDbLayer().startTransaction();
                ChangeSetModificatorImport helper = new ChangeSetModificatorImport(iPartsImportPlugin.LOG_CHANNEL_DEBUG,
                                                                                   this, false);
                helper.executeChangesInAllChangeSets(changeSetModificationTasks, true,
                                                     iPartsConst.TECHNICAL_USER_DIALOG_DELTA_SUPPLY);

                // Änderungen in den ChangeSets auf jeden Fall auch committen, wenn die vorherige Transaktion committed wurde,
                // um Inkonsistenzen zu vermeiden
                getProject().getDbLayer().commit();
                getProject().getDbLayer().startTransaction();
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                cancelImport(e.getMessage());
            }
        }

        changeSetModificationTasks.clear();
        dialogPositionsHelperMap.clear();
        alreadyImportedRecords.clear();
        importHelper = null;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class VTNVHelper extends DIALOGImportHelper {

        public VTNVHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public iPartsReplaceConstMatId getReplaceConstMatId(Map<String, String> importRec) {
            return new iPartsReplaceConstMatId(handleValueOfSpecialField(VTNV_TEIL, importRec),
                                               handleValueOfSpecialField(VTNV_SDATA, importRec));
        }

        /**
         * Erstellt einen neuen Eintrag in DA_DIALOG_CHANGES. Die Besonderheit hier ist, dass auch die ChangeSet-GUID
         * direkt gesetzt wird, falls sie nicht leer oder {@code null} ist, da in diesem Importer Einträge evtl. innerhalb vom
         * ChangeSet geschrieben werden, in dem eine Ersetzung bearbeitet wird, womit die ChangeSet-GUID schon bekannt ist.
         *
         * @param replacePart
         * @param partListEntry
         * @param authorOrderChangeSet
         * @return
         */
        private iPartsDataDIALOGChange createDialogChange(iPartsDataReplacePart replacePart, EtkDataPartListEntry partListEntry,
                                                          iPartsRevisionChangeSet authorOrderChangeSet) {
            String seriesNo = "";
            String sourceGUID = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);
            if (bctePrimaryKey != null) {
                seriesNo = bctePrimaryKey.getHmMSmId().getSeries();
            }
            iPartsDataDIALOGChange dataDIALOGChange = createChangeRecord(iPartsDataDIALOGChange.ChangeType.REPLACEMENT_AS, replacePart.getAsId(),
                                                                         seriesNo, sourceGUID, "", partListEntry.getAsId().toDBString());
            String changeSetGUID = "";
            if (authorOrderChangeSet != null) {
                changeSetGUID = authorOrderChangeSet.getChangeSetId().getGUID();
            }
            dataDIALOGChange.setFieldValue(FIELD_DDC_CHANGE_SET_GUID, changeSetGUID, DBActionOrigin.FROM_EDIT);
            return dataDIALOGChange;
        }

        /**
         * Befüllt das übergebene Ersetzungsobjekt mit den Daten aus dem übergebenen <code>importRec</code>. Zusätzlich
         * werden die Mitlieferteile aus dem <code>importRec</code> extrahiert und der Ersetzung hinzugefügt.
         *
         * @param dataReplaceConstMat
         * @param importRec
         */
        public void fillOverrideAndFillIncludeParts(iPartsDataReplaceConstMat dataReplaceConstMat, Map<String, String> importRec) {
            fillOverrideCompleteDataForDIALOGReverse(dataReplaceConstMat, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
            fillIncludeParts(dataReplaceConstMat, importRec);
        }

        /**
         * Befüllt das übergebene Ersetzungsobjekt mit den dazugehörigen Mitlieferteilen aus dem <code>importRec</code>.
         *
         * @param dataReplaceConstMat
         * @param importRec
         */
        public void fillIncludeParts(iPartsDataReplaceConstMat dataReplaceConstMat, Map<String, String> importRec) {
            // zuerst die vorhandenen Mitlieferteile holen
            List<iPartsDataIncludeConstMat> oldDataIncludeConstMatList = dataReplaceConstMat.getIncludeConstMats().getAsList();
            Map<iPartsIncludeConstMatId, iPartsDataIncludeConstMat> oldIncludeMatMap = new HashMap<>();
            for (iPartsDataIncludeConstMat dataIncludeConstMat : oldDataIncludeConstMatList) {
                oldIncludeMatMap.put(dataIncludeConstMat.getAsId(), dataIncludeConstMat);
            }

            dataReplaceConstMat.getIncludeConstMats().clear(DBActionOrigin.FROM_DB);
            Set<iPartsIncludeConstMatId> addedValues = new HashSet<>(); // zum Erkennen von doppelten Mitlieferteilen an einem Record (Datenfehler)
            // neue Werte übernehmen
            for (int index = 1; index <= MAX_SUFFIX_COUNT; index++) {
                String part = handleValueOfSpecialField(VTNV_MIT_SNR_PREFIX + index, importRec);
                if (StrUtils.isValid(part)) {
                    String quantity = handleValueOfSpecialField(VTNV_MIT_MG_PREFIX + index, importRec);
                    iPartsIncludeConstMatId includeConstMatId = new iPartsIncludeConstMatId(dataReplaceConstMat.getAsId(), part);
                    iPartsDataIncludeConstMat dataIncludeConstMat = oldIncludeMatMap.get(includeConstMatId);
                    if (dataIncludeConstMat != null) {
                        // bereits vorhanden
                        oldIncludeMatMap.remove(includeConstMatId);
                    } else {
                        // neu hinzugekommen
                        dataIncludeConstMat = new iPartsDataIncludeConstMat(getProject(), includeConstMatId);
                        dataIncludeConstMat.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    }
                    if (!addedValues.contains(includeConstMatId)) {
                        // Werte setzen
                        dataIncludeConstMat.setFieldValue(FIELD_DICM_INCLUDE_PART_QUANTITY, quantity, DBActionOrigin.FROM_EDIT);
                        // in Replace-Mat eintragen
                        dataReplaceConstMat.addIncludeConstMat(dataIncludeConstMat, DBActionOrigin.FROM_DB);
                        addedValues.add(includeConstMatId);
                    } else {
                        getMessageLog().fireMessage(translateForLog("!!Doppelter Mitlieferteilesatz \"%1\" (wird ignoriert)",
                                                                    includeConstMatId.getIncludePartNo()),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }
                }
            }
            // übriggebliebene löschen
            for (iPartsDataIncludeConstMat oldDataIncludeConstMat : oldIncludeMatMap.values()) {
                dataReplaceConstMat.getIncludeConstMats().delete(oldDataIncludeConstMat, true, DBActionOrigin.FROM_EDIT);
            }
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(VTNV_SDATA)) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.startsWith(VTNV_MIT_MG_PREFIX)) {
                value = checkQuantityFormat(value);
            } else if (sourceField.equals(VTNV_TEIL) || sourceField.equals(VTNV_VOR_SNR) ||
                       sourceField.startsWith(VTNV_MIT_SNR_PREFIX)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }
    }


    /**
     * Datenklasse für den Konstruktions-Stücklisteneintrag sowie alle BCTE-Schlüssel der KEM-Kette für einen konkreten
     * BCTE-Schlüssel.
     */
    private static class ConstructionPLEAndBCTEKeysForKemChain {

        public EtkDataPartListEntry constructionPartListEntry;
        public Set<iPartsDialogBCTEPrimaryKey> bcteKeysForKemChain;
    }
}
