/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für Rückmeldedaten (RMDA) und Ausreißer (RMID)
 */
public class ResponseDataImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    private enum IMPORT_TYPE {IMPORT_RESPONSE_DATA, IMPORT_RESPONSE_SPIKES, IMPORT_UNKNOWN}

    public static final String RMDA_PREFIX = "RMDA";
    public static final String RMID_PREFIX = "RMID";
    public static final String TABLENAME_RMDA = TABLE_NAME_PREFIX + RMDA_PREFIX;
    public static final String TABLENAME_RMID = TABLE_NAME_PREFIX + RMID_PREFIX;

    private static final String FACTORY_NUMBER_AS = "000"; // Werksnummer "000" kennzeichnet AS Rückmeldedaten/Ausreißer

    public static final String WN = "WN";
    public static final String BR = "BR";
    public static final String AA = "AA";
    public static final String BMAA = "BMAA";
    public static final String FZGA = "FZGA";
    public static final String L = "L";
    public static final String PEM = "PEM";
    public static final String ADAT = "ADAT";
    // Nur RMDA
    public static final String RMDA_TEXT = "RMDA_TEXT";
    public static final String RMDA_G = "RMDA_G";
    // Nur RMID
    public static final String RMID_IDAB = "RMID_IDAB";

    private final Map<String, Set<String>> asPemToFactoriesMap = new HashMap<>();
    private final String importTableInXML;

    private IMPORT_TYPE importType;
    private HashMap<String, String> mapping;
    private String[] primaryKeysForImport;
    private String prefixForImporterInstance;
    private String importTableInDB;
    private boolean importToDB = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public ResponseDataImporter(EtkProject project, String importTable) {
        super(project, "Invalid Importer");
        this.importTableInXML = importTable;
        initImporter();
    }

    private void initImporter() {
        mapping = new HashMap<>();
        String[] specificPrimaryKeys = null;
        String nameForImport = "";

        if (importTableInXML.equals(TABLENAME_RMDA)) {
            importType = IMPORT_TYPE.IMPORT_RESPONSE_DATA;
            importTableInDB = TABLE_DA_RESPONSE_DATA;
            prefixForImporterInstance = RMDA_PREFIX + "_";
            specificPrimaryKeys = new String[]{};
            importName = "!!DIALOG-Rückmeldedaten (RMDA)";
            nameForImport = DRD_RESPONSE_DATA;

            // das Mapping darf die PK-Felder nicht enthalten da sonst eine angepasste ID im DataObject bei
            // fillOverrideCompleteDataForDIALOGReverse() mit den Werten aus dem Importrecord überschrieben wird.
            mapping.put(FIELD_DRD_STEERING, prefixForImporterInstance + L);
            mapping.put(FIELD_DRD_TEXT, RMDA_TEXT);
            mapping.put(FIELD_DRD_AGG_TYPE, RMDA_G);

        } else if (importTableInXML.equals(TABLENAME_RMID)) {
            importType = IMPORT_TYPE.IMPORT_RESPONSE_SPIKES;
            importTableInDB = TABLE_DA_RESPONSE_SPIKES;
            prefixForImporterInstance = RMID_PREFIX + "_";
            specificPrimaryKeys = new String[]{ RMID_IDAB };
            importName = "!!DIALOG-Rückmeldedaten Ausreißer (RMID)";
            nameForImport = DRS_RESPONSE_SPIKES;

            // das Mapping darf die PK-Felder nicht enthalten da sonst eine angepasste ID im DataObject bei
            // fillOverrideCompleteDataForDIALOGReverse() mit den Werten aus dem Importrecord überschrieben wird.
            mapping.put(FIELD_DRS_STEERING, prefixForImporterInstance + L);
        } else {
            importType = IMPORT_TYPE.IMPORT_UNKNOWN;
        }

        primaryKeysForImport = StrUtils.mergeArrays(new String[]{ prefixForImporterInstance + WN, prefixForImporterInstance + BR,
                                                                  prefixForImporterInstance + FZGA, prefixForImporterInstance + PEM,
                                                                  prefixForImporterInstance + ADAT },
                                                    (specificPrimaryKeys != null) ? specificPrimaryKeys : new String[0]);

        importFileTypes = new FilesImporterFileListType[]{ new FilesImporterFileListType(importTableInDB,
                                                                                         nameForImport, false, false, true,
                                                                                         new String[]{ MimeTypes.EXTENSION_XML }) };

    }

    @Override
    protected void preImportTask() {
        asPemToFactoriesMap.clear();
        super.preImportTask();
    }

    @Override
    protected void postImportTask() {
        asPemToFactoriesMap.clear();
        super.postImportTask();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysForImport);
        importer.setMustHaveData(primaryKeysForImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return importType != IMPORT_TYPE.IMPORT_UNKNOWN;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ResponseImportHelper helper = new ResponseImportHelper(getProject(), mapping, importTableInDB);
        EtkDataObject dataObject;
        EtkDataObject dataObjectWithOtherASData;
        String sourceField;
        String statusField;
        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!helper.checkImportRelevanceForSeries(prefixForImporterInstance + BR, importRec, getInvalidSeriesSet(), this)) {
            return;
        }

        // Werk bestimmen
        String pem = helper.handleValueOfSpecialField(prefixForImporterInstance + PEM, importRec);
        String br = helper.handleValueOfSpecialField(prefixForImporterInstance + BR, importRec);
        String aa = helper.handleValueOfSpecialField(prefixForImporterInstance + AA, importRec);
        String factoryNumber = helper.handleValueOfSpecialField(prefixForImporterInstance + WN, importRec);
        boolean isASData = factoryNumber.equals(FACTORY_NUMBER_AS) || helper.isASPem(pem);

        Set<String> factories = helper.getFactoriesForPem(pem, br, aa);
        if (factories == null) {
            getMessageLog().fireMessage(translateForLog("!!Für die PEM \"%1\" in Baureihe \"%2\" und Ausführungsart \"%3\" wird direkt die Werksnummer aus den Importdaten verwendet, da keine Werksnummer über das Mapping ermittelt werden konnte: %4",
                                                        pem, br, aa, factoryNumber),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            factories = new LinkedHashSet<>();
            factories.add(factoryNumber);
        }

        boolean isInitialDataImport = isDIALOGInitialDataImport();
        iPartsDataReleaseState datasetState;

        // für jedes gefundene Werk wird ein Rückmeldedatensatz erzeugt
        for (String factory : factories) {
            switch (importType) {
                case IMPORT_RESPONSE_DATA:
                    iPartsResponseDataId responseDataId = helper.getResponseDataId(importRec, factory, isASData);
                    iPartsDialogSeries seriesCache = iPartsDialogSeries.getInstance(getProject(), new iPartsSeriesId(br));
                    String aggregateTyp = seriesCache.getAggregateType();
                    DCAggregateTypes dcAggregateType = DCAggregateTypes.getDCAggregateTypeByAggregateType(aggregateTyp);
                    boolean responseIdentWellFormed = helper.isResponseIdentFormallyValid(responseDataId.getIdent(), br, dcAggregateType);
                    if (responseIdentWellFormed) {
                        datasetState = iPartsDataReleaseState.RELEASED;
                    } else {
                        datasetState = iPartsDataReleaseState.NOT_RELEVANT;
                    }
                    dataObject = new iPartsDataResponseData(getProject(), responseDataId);
                    sourceField = FIELD_DRD_SOURCE;
                    statusField = iPartsConst.FIELD_DRD_STATUS;
                    iPartsResponseDataId responseDataIdWithOtherASData = helper.getResponseDataId(importRec, factory, !isASData);
                    dataObjectWithOtherASData = new iPartsDataResponseData(getProject(), responseDataIdWithOtherASData);
                    break;
                case IMPORT_RESPONSE_SPIKES:
                    iPartsResponseSpikeId spikeId = helper.getResponseSpikeId(importRec, factory, isASData);
                    dataObject = new iPartsDataResponseSpike(getProject(), spikeId);
                    sourceField = FIELD_DRS_SOURCE;
                    statusField = iPartsConst.FIELD_DRS_STATUS;
                    iPartsResponseSpikeId responseSpikeIdWithOtherASData = helper.getResponseSpikeId(importRec, factory, !isASData);
                    dataObjectWithOtherASData = new iPartsDataResponseSpike(getProject(), responseSpikeIdWithOtherASData);
                    // DAIMLER-5931: Neue/geänderte Ausreißer-Daten RMID (DA_RESPONSE_SPIKES) sollen immer mit Status "freigegeben" eingebucht werden.
                    datasetState = iPartsDataReleaseState.RELEASED;
                    break;
                default:
                    cancelImport(translateForLog("!!Record %1 fehlerhaft (ungültiger ImportTyp)",
                                                 String.valueOf(recordNo)));
                    return;
            }

            if (!dataObject.existsInDB()) {
                dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            helper.deleteContentIfMADSource(dataObject, sourceField, false);

            // Non-PK-Felder, soweit in Mapping zugeordnet, aus dem Import-Record zuweisen; deshalb dürfen die PK-Felder nicht
            // im Mapping enthalten sein.
            // m.E. sollte diese Methode PK-Felder gar nicht antasten, dann würde es keine Rolle spielen ob sie im Mapping drin sind oder nicht
            helper.fillOverrideCompleteDataForDIALOGReverse(dataObject, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
            // Setze die Herkunft
            dataObject.setFieldValue(sourceField, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);

            // Status setzen falls leer oder Urladung
            if (isInitialDataImport || dataObject.getFieldValue(statusField).isEmpty()) {
                dataObject.setFieldValue(statusField, datasetState.getDbValue(), DBActionOrigin.FROM_EDIT);
            }

            if (importToDB) {
                if (isInitialDataImport) {
                    // DAIMLER-7678: Bei der Urladung einen evtl. existierenden MAD Datensatz mit anderem AS_DATA löschen.
                    if (dataObjectWithOtherASData.existsInDB()) {
                        if (iPartsImportDataOrigin.getTypeFromCode(dataObjectWithOtherASData.getFieldValue(sourceField)) == iPartsImportDataOrigin.MAD) {
                            dataObjectWithOtherASData.deleteFromDB();
                        }
                    }
                }
                saveToDB(dataObject);
            }
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(importTableInDB)) {
            deleteLanguageEntriesOfTable(importTableInDB);  // hier brauchen wir keine Unterscheidung DIALOG- vs. MAD-Daten da MAD-Importe keine Texte mitbringen
            String sourceField;
            if (importType == IMPORT_TYPE.IMPORT_RESPONSE_DATA) {
                sourceField = FIELD_DRD_SOURCE;
            } else if (importType == IMPORT_TYPE.IMPORT_RESPONSE_SPIKES) {
                sourceField = FIELD_DRS_SOURCE;
            } else {
                return false;
            }
            getProject().getDB().delete(importTableInDB, new String[]{ sourceField }, new String[]{ iPartsImportDataOrigin.DIALOG.getOrigin() });
            return true;
        }
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(importTableInDB)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class ResponseImportHelper extends DIALOGImportHelper {


        public ResponseImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(prefixForImporterInstance + BR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            if (sourceField.equals(prefixForImporterInstance + ADAT)) {
                value = getDIALOGDateTimeValue(value);
            }
            return value;
        }

        /**
         * Bei Fahrzeugen dürfen die Idents 7-Stellen besitzen
         * Bei Motoren dürfen die Idents 7 oder 8-Stellen besitzen
         * Bei anderen Aggregaten (Getriebe, etc.) dürfen die Idents 8-Stellen besitzen
         * <p>
         * Falls kein Baureihen-Typ vorhanden ist, den Präfix für Baumuster abfragen
         * <p>
         * Die erste Stelle darf Buchstaben und Zahlen enthalten
         * Der Rest muss aus Zahlen bestehen
         *
         * @param responseIdent
         * @param aggregateType
         * @return
         */
        public boolean isResponseIdentFormallyValid(String responseIdent, String br, DCAggregateTypes aggregateType) {
            if ((aggregateType == DCAggregateTypes.VEHICLE) ||
                ((aggregateType == DCAggregateTypes.UNKNOWN) && iPartsModel.isVehicleModel(br))) {

                if (responseIdent.length() != 7) {
                    return false;
                }
            } else if ((aggregateType == DCAggregateTypes.ENGINE) ||
                       ((aggregateType == DCAggregateTypes.UNKNOWN) && iPartsModel.isAggregateModel(br))) {

                if (responseIdent.length() != 7) {
                    if (responseIdent.length() != 8) {
                        return false;
                    }
                }
            } else { // andere Aggregate
                if (responseIdent.length() != 8) {
                    return false;
                }
            }
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            return numberHelper.isIdentWellFormed(responseIdent);
        }


        public iPartsResponseDataId getResponseDataId(Map<String, String> importRec, String factory, boolean asData) {
            return new iPartsResponseDataId(factory,
                                            handleValueOfSpecialField(prefixForImporterInstance + BR, importRec),
                                            handleValueOfSpecialField(prefixForImporterInstance + AA, importRec),
                                            handleValueOfSpecialField(prefixForImporterInstance + BMAA, importRec),
                                            handleValueOfSpecialField(prefixForImporterInstance + PEM, importRec),
                                            handleValueOfSpecialField(prefixForImporterInstance + ADAT, importRec),
                                            handleValueOfSpecialField(prefixForImporterInstance + FZGA, importRec),
                                            asData);
        }

        public iPartsResponseSpikeId getResponseSpikeId(Map<String, String> importRec, String factory, boolean asData) {
            return new iPartsResponseSpikeId(factory,
                                             handleValueOfSpecialField(prefixForImporterInstance + BR, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + AA, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + BMAA, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + FZGA, importRec),
                                             handleValueOfSpecialField(RMID_IDAB, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + PEM, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + ADAT, importRec),
                                             asData);
        }

        /**
         * Liefert das Werk zur PEM bzw. ggf. mehrere Werke für AS-PEMs.
         *
         * @param pem
         * @param br  Baureihe, nur relevant für AS-PEMs
         * @param aa  AA, nur relevant für AS-PEMs
         * @return
         */
        public Set<String> getFactoriesForPem(String pem, String br, String aa) {
            Set<String> factories = null;
            if (isASPem(pem)) {
                if (StrUtils.isValid(br)) {
                    factories = getFactoriesForASPemFromCache(asPemToFactoriesMap, pem, br, aa);
                    if (factories == null) {
                        // Werkseinsatzdaten abfragen und zu Cache hinzufügen
                        addToPemToFactoriesCacheForSeries(pem, br);
                        factories = getFactoriesForASPemFromCache(asPemToFactoriesMap, pem, br, aa);
                    }
                }

                // Wenn nichts gefunden dann Werk "006"
                if (factories == null) {
                    factories = new HashSet<>();
                    factories.add("006");
                }
            } else {
                // Der "normale" Weg, um an das Werk zu einer PEM zu kommen
                String factory = iPartsFactories.getInstance(getProject()).getFactoryNumberForPEMAndDataSource(pem, iPartsImportDataOrigin.MAD);
                if (factory != null) {
                    factories = new HashSet<>();
                    factories.add(factory);
                }
            }
            return factories;
        }

        /**
         * Cache für PEM,BR,AA ->> Werke aus den übergebenen Werkseinsatzdaten aufbauen.
         *
         * @param pem
         * @param br
         */
        private void addToPemToFactoriesCacheForSeries(String pem, String br) {
            iPartsDataFactoryDataList factoryDataList = iPartsDataFactoryDataList.loadAfterSalesFactoryDataForSeriesNoAndPEM(getProject(), br, pem,
                                                                                                                             iPartsImportDataOrigin.DIALOG.getOrigin(),
                                                                                                                             false);  // sollte egal sein
            if (factoryDataList.size() > 0) {
                for (iPartsDataFactoryData factoryData : factoryDataList) {
                    String pema = factoryData.getFieldValue(FIELD_DFD_PEMA);
                    String pemb = factoryData.getFieldValue(FIELD_DFD_PEMB);
                    String factory = factoryData.getFieldValue(FIELD_DFD_FACTORY);
                    String aaFromDataObject = factoryData.getFieldValue(FIELD_DFD_AA);
                    addASPemToFactoriesCacheEntry(asPemToFactoriesMap, pema, br, aaFromDataObject, factory);
                    addASPemToFactoriesCacheEntry(asPemToFactoriesMap, pemb, br, aaFromDataObject, factory);

                    // für nicht-leere AA brauchen wir auch nochmal die Beziehung ohne AA
                    if (StrUtils.isValid(aaFromDataObject)) {
                        addASPemToFactoriesCacheEntry(asPemToFactoriesMap, pema, br, "", factory);
                        addASPemToFactoriesCacheEntry(asPemToFactoriesMap, pemb, br, "", factory);
                    }
                }
            }

            iPartsDataColorTableFactoryList colorTableFactoryList = iPartsDataColorTableFactoryList.loadColorTableFactoryASForSeriesAndPEM(getProject(), br, pem);
            if (colorTableFactoryList.size() > 0) {
                for (iPartsDataColorTableFactory colorTableFactory : colorTableFactoryList) {
                    String pema = colorTableFactory.getFieldValue(FIELD_DCCF_PEMA);
                    String pemb = colorTableFactory.getFieldValue(FIELD_DCCF_PEMB);
                    String factory = colorTableFactory.getFieldValue(FIELD_DCCF_FACTORY);
                    addASPemToFactoriesCacheEntry(asPemToFactoriesMap, pema, br, "", factory);
                    addASPemToFactoriesCacheEntry(asPemToFactoriesMap, pemb, br, "", factory);
                }
            }
        }
    }
}
