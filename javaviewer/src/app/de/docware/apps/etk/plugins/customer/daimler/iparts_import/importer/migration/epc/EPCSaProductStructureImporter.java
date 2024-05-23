/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductSAsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * EPC Importer für die Produkt-SA-Struktur (BM_SAIDX)
 */
public class EPCSaProductStructureImporter extends AbstractEPCDataImporter implements iPartsConst, EtkDbConst {

    private static final String CATNUM = "CATNUM";
    private static final String GROUPNUM = "GROUPNUM";
    private static final String SANUM = "SANUM";
    private static final String CODETWO = "CODETWO";
    private static final String DESCIDX = "DESCIDX";
    private static final String CODEONE = "CODEONE";

    private static final String DEST_TABLENAME = TABLE_DA_PRODUCT_SAS;// Die Zieltabelle

    // Ein kleiner Cache, damit die Produkte nicht immer wieder aus der DB geladen werden.
    private Map<iPartsProductId, Boolean> productRelevanceCache = new HashMap<>();
    private Set<String> handledSAs;
    private Set<String> createdSAs;
    private Set<String> constKitsNumbers;  // Baukasten-Nummern

    private boolean importConstKits = false; // Baukästen-Verbindungen speichern?
    private boolean importToDB = true;
    private boolean doBufferSave = true;


    protected EPCSaProductStructureImporter(EtkProject project) {
        super(project, "EPC SA-Produkt-Structure", "!!EPC SA-Produkt-Struktur (BM_SAIDX)", DEST_TABLENAME, true, true);
    }

    @Override
    protected HashMap<String, String> initMapping() {
        // Hier kein Mapping notwendig, da alle Werte im Schlüssel sind
        return new HashMap<>();
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{
                CATNUM,
                GROUPNUM,
                SANUM,
                CODETWO,
                DESCIDX,
                CODEONE
        };
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustFields = new String[]{ CATNUM, GROUPNUM, SANUM };
        importer.setMustExists(mustFields);
        importer.setMustHaveData(mustFields);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
        productRelevanceCache.clear();
        handledSAs = new HashSet<>();
        createdSAs = new LinkedHashSet<>();
        constKitsNumbers = new LinkedHashSet<>();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SaProductStructureImportHelper importHelper = new SaProductStructureImportHelper(getProject(), getFieldMapping());
        String productNumber = importHelper.handleValueOfSpecialField(CATNUM, importRec).trim();

        // Logische Prüfung mit Ausgabe von Meldungen
        if (!importHelper.isProductRelevantForImport(this, productNumber, productRelevanceCache, recordNo)) {
            reduceRecordCount();
            return;
        }
        String saRawNumber = importHelper.handleValueOfSpecialField(SANUM, importRec);
        boolean isSaNumberValid = isSARawNumberValid(saRawNumber, recordNo);
        String saNumber;
        if (isSaNumberValid) {
            saNumber = importHelper.makeSANumberFromEPCValue(saRawNumber);
        } else {
            // Baukastennummer
            saNumber = saRawNumber;
            constKitsNumbers.add(saNumber);
            if (!importConstKits) {
                reduceRecordCount();
                return;
            }
        }

        String kgNumber = importHelper.handleValueOfSpecialField(GROUPNUM, importRec);
        iPartsProductSAsId productSaId = new iPartsProductSAsId(productNumber, saNumber, kgNumber);
        iPartsDataProductSAs dataProductSAs = new iPartsDataProductSAs(getProject(), productSaId);
        if (!dataProductSAs.existsInDB()) {
            dataProductSAs.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataProductSAs.setFieldValue(FIELD_DPS_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
        } else {
            // Alle Daten nur importieren, falls die entsprechende SA nicht aus MAD migriert wurde.
            iPartsImportDataOrigin saSource = iPartsImportDataOrigin.getTypeFromCode(dataProductSAs.getFieldValue(FIELD_DPS_SOURCE));

            // Für SAs zu denen bereits Daten aus MAD migriert wurden, werden keine Daten importiert
            if ((saSource != iPartsImportDataOrigin.EPC) && (saSource != iPartsImportDataOrigin.APP_LIST) &&
                (saSource != iPartsImportDataOrigin.UNKNOWN)) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 Produkt-SA \"%2, %3, %4\" mit Quelle \"%5\"" +
                                                            " wird nicht überschrieben!",
                                                            String.valueOf(recordNo),
                                                            productNumber, saNumber, kgNumber, saSource.getOrigin()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                reduceRecordCount();
                return;
            }
        }
        saveToDB(dataProductSAs);

        if (isSaNumberValid) {
            saveSaIfNecessary(importHelper, saNumber, importRec, recordNo);
        }
    }

    private boolean isSARawNumberValid(String saRawNumber, int recordNo) {
        if (!StrUtils.isValid(saRawNumber)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 SA-Nummer ist leer. Record wird ignoriert.",
                                                        String.valueOf(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                        MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return false;
        }
        // SA-Nummern sind 6-stellig. Alles andere ist nicht valide.
        if (saRawNumber.length() != 6) {
            if (!importConstKits) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 Baukastennummer (%2) wird ignoriert.",
                                                            String.valueOf(recordNo), saRawNumber),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
            return false;
        }
        return true;
    }

    private void saveSaIfNecessary(SaProductStructureImportHelper importHelper, String saNumber, Map<String, String> importRec, int recordNo) {
        if (!handledSAs.contains(saNumber)) {
            handledSAs.add(saNumber);
            boolean saveSA = importHelper.isSARelevantForImport(null, saNumber, recordNo, false);
            if (saveSA) {
                String epcTextId = importHelper.handleValueOfSpecialField(DESCIDX, importRec);
                if (StrUtils.isValid(saNumber, epcTextId)) {
                    iPartsSaId saId = new iPartsSaId(saNumber);
                    iPartsDataSa saData = new iPartsDataSa(getProject(), saId);
                    if (!saData.existsInDB()) {
                        saData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        saData.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
                        createdSAs.add(saNumber);
                    } else {
                        // MultiLang nachladen
                        saData.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
                    }
                    EtkMultiSprache epcText = importHelper.searchEPCTextWithEPCId(DictTextKindEPCTypes.SA_DICTIONARY, epcTextId);
                    String codes = importHelper.getCodeValueFromMultipleCodeFields(importRec, CODEONE, CODETWO);
                    importHelper.fillSaMasterDataObject(saData, codes, epcText);
                    saveToDB(saData);
                } else {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 Werte nicht gültig um einen" +
                                                                " Datensatz zu erzeugen. SA: %2, EPC TextId: %3",
                                                                String.valueOf(recordNo), saNumber, epcTextId),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    return;
                }
            }
        }
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        if (!isCancelled()) {
            productRelevanceCache.clear();
            if (!constKitsNumbers.isEmpty()) {
                if (importConstKits) {
                    getMessageLog().fireMessage(translateForLog("!!Es wurden %1 Verbindungen zwischen Produkten und Baukästen angelegt",
                                                                String.valueOf(constKitsNumbers.size())),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                } else {
                    getMessageLog().fireMessage(translateForLog("!!Verbindungen zwischen Produkten und Baukästen wurden ignoriert",
                                                                String.valueOf(constKitsNumbers.size())),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }
                StringBuilder str = iPartsMainImportHelper.buildNumberListForLogFile(constKitsNumbers);
                getMessageLog().fireMessage(translateForLog("!!Folgende Baukästen wurden angesprochen:"),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                getMessageLog().fireMessage(str.toString(),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
            if (!createdSAs.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Es wurden %1 SAs angesprochen und %2 angelegt",
                                                            String.valueOf(handledSAs.size()), String.valueOf(createdSAs.size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                StringBuilder str = iPartsMainImportHelper.buildNumberListForLogFile(createdSAs);
                getMessageLog().fireMessage(translateForLog("!!Folgende SAs wurden neu angelegt:"),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                getMessageLog().fireMessage(str.toString(),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }
        handledSAs = null;
        createdSAs = null;
        constKitsNumbers = null;
    }

    /**
     * Ruft saveToDB auf und korrigiert skippedRecords (reduceRecordCount() in saveToDB())
     *
     * @param dataObject
     */
    @Override
    public boolean saveToDB(EtkDataObject dataObject) {
        if (importToDB) {
            boolean isBuffered = super.saveToDB(dataObject);
            if (!(dataObject instanceof iPartsDataProductSAs) && !isBuffered) {
                skippedRecords--;
            }
            return isBuffered;
        }
        return false;
    }

    /**
     * @param importFileType
     * @return
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (isRemoveAllExistingData()) {
            getProject().getDbLayer().delete(DEST_TABLENAME, new String[]{ FIELD_DPS_SOURCE },
                                             new String[]{ iPartsImportDataOrigin.EPC.getOrigin() });
        }
        return true;
    }

    private class SaProductStructureImportHelper extends EPCImportHelper {

        private SaProductStructureImportHelper(EtkProject project, Map<String, String> mapping) {
            super(project, mapping, DEST_TABLENAME);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }
            if (sourceField.equals(SANUM)) {
                value = StrUtils.trimRight(value);
//                value = makeSANumberFromEPCValue(StrUtils.trimRight(value));

            }
            return value;
        }
    }
}
