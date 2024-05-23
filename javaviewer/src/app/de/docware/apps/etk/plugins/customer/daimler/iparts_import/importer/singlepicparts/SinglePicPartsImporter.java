/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.singlepicparts;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartList;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueList;
import de.docware.util.collections.diskmappedlist.compare.DiskMappedKeyValueListCompare;
import de.docware.util.file.DWFile;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Importer für Sachnummern für Einzelteilbilder über einen WebService
 */
public class SinglePicPartsImporter extends AbstractDataImporter {

    private static final String DIVIDER_ES1KEY = "es1Key";
    private static final String DIVIDER_ES2KEY = "es2Key";

    private int importRecordCount;
    private Collection<String> picMatNumberList;
    private boolean doSave = true;
    private boolean isBufferdSave = true;

    public SinglePicPartsImporter(EtkProject project) {
        super(project, "!!Sachnummern für Einzelteilbilder");
        progressMessageType = ProgressMessageType.READING;
    }

    /**
     * Einsprungspunkt für den Importer
     *
     * @param picMatNumberList
     */
    public void doImport(Collection<String> picMatNumberList) {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        this.picMatNumberList = new ArrayList<>(picMatNumberList.size());
        for (String matNr : picMatNumberList) {
            if (matNr.contains(DIVIDER_ES1KEY) || matNr.contains(DIVIDER_ES2KEY)) {
                String baseMatNr, es1Key, es2Key;
                if (matNr.contains(DIVIDER_ES1KEY) && !matNr.contains(DIVIDER_ES2KEY)) { // Nur ES1
                    baseMatNr = StrUtils.stringUpToCharacter(matNr, DIVIDER_ES1KEY);
                    es1Key = StrUtils.stringAfterCharacter(matNr, DIVIDER_ES1KEY);
                    es2Key = "";
                } else if (!matNr.contains(DIVIDER_ES1KEY) && matNr.contains(DIVIDER_ES2KEY)) { // Nur ES2
                    baseMatNr = StrUtils.stringUpToCharacter(matNr, DIVIDER_ES2KEY);
                    es1Key = "";
                    es2Key = StrUtils.stringAfterCharacter(matNr, DIVIDER_ES2KEY);
                } else { // ES1 & ES2
                    baseMatNr = StrUtils.stringUpToCharacter(matNr, DIVIDER_ES1KEY);
                    es1Key = StrUtils.stringBetweenStrings(matNr, DIVIDER_ES1KEY, DIVIDER_ES2KEY);
                    es2Key = StrUtils.stringAfterCharacter(matNr, DIVIDER_ES2KEY);
                }
                matNr = numberHelper.getPartNoWithES1AndESKeys(baseMatNr, es1Key, es2Key);
            }
            this.picMatNumberList.add(matNr);
        }
        setBufferedSave(isBufferdSave);
        importMasterData(null);
    }

    protected boolean doCompareAndSave() {
        EtkDataPartList totalDataPartList = new EtkDataPartList();
        if (!doCompare(totalDataPartList)) {
            return false;
        }
        if (!totalDataPartList.isEmpty()) {
            totalDataPartList.forEach((data) -> {
                if (doSave) {
                    saveToDB(data);
                }
            });
        }
        return true;
    }

    protected boolean doCompare(EtkDataPartList totalDataPartList) {
        //no Equals
        DiskMappedKeyValueListCompare listComp = new DiskMappedKeyValueListCompare(MAX_ROWS_IN_MEMORY, true, false, false);

        try {
            importRecordCount = fillComparer(listComp);
            addMessage("!!Vergleiche Sachnummern aus den Importdaten mit denen aus der Datenbank...");
            if (importRecordCount > 0) {
                int currentRecordCounter = 0;
                totalDataPartList.clear(DBActionOrigin.FROM_DB);
                if (listComp.getOnlyInFirstItems().size() > 0) {
                    // Neue Materialien in den Importdaten -> Flag M_IMAGE_AVAILABLE setzen
                    addMessage("!!Setze das Flag \"Einzelteilbilder vorhanden\" bei %1 neuen Sachnummern...",
                               String.valueOf(listComp.getOnlyInFirstItems().size()));
                    currentRecordCounter = modifyParts(listComp.getOnlyInFirstItems(), true, totalDataPartList, currentRecordCounter);
                    if (currentRecordCounter == -1) {
                        return false;
                    }
                }

                if (listComp.getOnlyInSecondItems().size() > 0) {
                    // Materialien nicht mehr in den Importdaten vorhanden  -> Flag M_IMAGE_AVAILABLE entfernen
                    addMessage("!!Entferne das Flag \"Einzelteilbilder vorhanden\" bei %1 vorhandenen Sachnummern...",
                               String.valueOf(listComp.getOnlyInSecondItems().size()));
                    currentRecordCounter = modifyParts(listComp.getOnlyInSecondItems(), false, totalDataPartList, currentRecordCounter);
                    if (currentRecordCounter == -1) {
                        return false;
                    }
                }
            } else {
                addMessage("!!Keine Änderungen");
            }
        } catch (Exception e) {
            addError(e.getMessage());
            return false;
        } finally {
            listComp.cleanup();
        }

        return true;
    }

    private int modifyParts(DiskMappedKeyValueList keyValueList, boolean imageAvailable, EtkDataPartList totalDataPartList,
                            int currentRecordCounter) {
        Iterator<DiskMappedKeyValueEntry> iter = keyValueList.getIterator();
        while (iter.hasNext()) {
            if (Thread.currentThread().isInterrupted()) {
                cancelImport("!!Import-Thread wurde frühzeitig beendet");
            }
            if (cancelled) {
                return -1;
            }

            DiskMappedKeyValueEntry entry = iter.next();
            String materialNumber = entry.getKey();
            if (!modifyPart(materialNumber, imageAvailable, totalDataPartList)) {
                addWarning("!!Sachnummer \"%1\" ist in der Datenbank nicht vorhanden.", materialNumber);
                reduceRecordCount();
            }
            currentRecordCounter++;
            updateProgress(currentRecordCounter, importRecordCount);
        }
        return currentRecordCounter;
    }

    private boolean modifyPart(String matNr, boolean imageAvailable, EtkDataPartList totalDataPartList) {
        PartId partID = new PartId(matNr, "");
        EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getProject(), partID);
        // existiert MatStamm nicht, dann ignorieren
        if (dataPart.existsInDB()) {
            dataPart.setFieldValueAsBoolean(iPartsConst.FIELD_M_IMAGE_AVAILABLE, imageAvailable, DBActionOrigin.FROM_EDIT);
            totalDataPartList.add(dataPart, DBActionOrigin.FROM_EDIT);
            return true;
        }
        return false;
    }

    private int fillComparer(DiskMappedKeyValueListCompare listComp) {
        addMessage("!!Lade Sachnummern mit Flag \"Einzelteilbilder vorhanden\"...");
        for (String picMatNumber : picMatNumberList) {
            listComp.putFirst(picMatNumber, buildValueForComparer(picMatNumber));
        }
        // etwas Speicher sparen
        picMatNumberList = null;

        loadPartsWithPic(listComp);
        return listComp.getOnlyInFirstItems().size() + listComp.getOnlyInSecondItems().size();
    }

    private String buildValueForComparer(String value) {
        return value; // Materialnummer genügt
    }

    private void loadPartsWithPic(DiskMappedKeyValueListCompare listComp) {
        // Da es sehr viele Materialnummern werden können, diese über einen FoundAttributesCallback direkt zum DiskMappedKeyValueListCompare
        // hinzufügen
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String matNumber = attributes.getFieldValue(iPartsConst.FIELD_M_MATNR);
                listComp.putSecond(matNumber, buildValueForComparer(matNumber));
                return false;
            }
        };

        EtkDataPartList dataPartList = new EtkDataPartList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR, false, false));
        String[] whereFields = new String[]{ TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_IMAGE_AVAILABLE) };
        String[] whereValues = new String[]{ SQLStringConvert.booleanToPPString(true) };
        dataPartList.searchSortAndFillWithJoin(getProject(), null, selectFields, whereFields, whereValues, false, null,
                                               false, false, false, foundAttributesCallback);
    }


    /**
     * Main-Routine für den Importer
     * nimmt die Log-Ausgaben und Standardabfragen vor
     *
     * @param importer
     * @return
     */
    @Override
    protected boolean importMasterData(AbstractKeyValueRecordReader importer) {
        DWFile runningLogFile = null;
        try {
            EtkMessageLog messageLog = new EtkMessageLog();
            runningLogFile = importJobRunning();
            initImport(messageLog);

            addMessage("!!Import %1 gestartet", translateForLog(importName));
            skippedRecords = 0;
            if (!doCompareAndSave()) {
                addError("!!Fehler");
            }

            postImportTask();
            if (getErrorCount() == 0) {
                logImportRecordsFinished(importRecordCount);
            }
            return true;
        } finally {
            getMessageLog().hideProgress();
            finishImport(true);
            if (runningLogFile != null) {
                if (isCancelled()) {
                    iPartsJobsManager.getInstance().jobCancelled(runningLogFile, false);
                } else if (getErrorCount() > 0) {
                    iPartsJobsManager.getInstance().jobError(runningLogFile);
                } else {
                    iPartsJobsManager.getInstance().jobProcessed(runningLogFile);
                }
            }
        }
    }

    private void addMessage(String translationsKey, String... placeHolderTexts) {
        addLogMsg(MessageLogType.tmlMessage, translationsKey, placeHolderTexts);
    }

    private void addWarning(String translationsKey, String... placeHolderTexts) {
        addLogMsg(MessageLogType.tmlWarning, translationsKey, placeHolderTexts);
    }

    private void addError(String translationsKey, String... placeHolderTexts) {
        addLogMsg(MessageLogType.tmlError, translationsKey, placeHolderTexts);
    }

    private void addLogMsg(MessageLogType logType, String translationsKey, String... placeHolderTexts) {
        getMessageLog().fireMessage(translateForLog(translationsKey, placeHolderTexts), logType, MessageLogOption.TIME_STAMP);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        return true;
    }
}
