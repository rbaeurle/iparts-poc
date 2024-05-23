/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTermIdHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModelsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictMultilineText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/*
 *  Importer für die Zuordnung Produkt (=Katalog) zu den enthaltenen 7-stelligen After Sales Baumustern.
 */

public class MigrationDialogProductModelsImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    // Die importierbaren Spalten der DIALOG-Datei: [BMRE_TDAT], Migrationsdaten, Produkt zu Baumuster Referenz
    private static final String BMRE_NR = "BMRE_NR";                    // Produkt
    private static final String BMRE_BM = "BMRE_BM";                    // Baumuster, 7-stellig = Aftersales Baumusternummer
    private static final String BMRE_DISPLAY = "BMRE_DISPLAY";          // Darf das Baumuster angezeigt werden oder nicht (mgl Werte: 'Y' oder 'N')
    private static final String TDAT_LKG = "TDAT_LKG";                  // Lenkung, R, L, (null)
    private static final String TDAT_ZUTX = "TDAT_ZUTX";                // Zusatztext
    private static final String TDAT_TEXT_ID = "TDAT_TEXT_ID";          // Text-ID
    private static final String TDAT_SDATA = "TDAT_SDATA";              // Datum ab : [2016-02-23-17.12.25.818624]
    private static final String TDAT_SDATB = "TDAT_SDATB";              // Datum bis: [2016-02-23-17.12.25.859956]
    private static final String TDAT_PTAB = "TDAT_PTAB";                // Gültigkeit von
    private static final String TDAT_PTBI = "TDAT_PTBI";                // Gültigkeit bis

    // Die Spaltenüberschriften, Spalteninhalte in der Reihenfolge, in der sie in der einzulesenden Datei vorkommen.
    private static final String[] headerNames = new String[]{ BMRE_NR,
                                                              BMRE_BM,
                                                              BMRE_DISPLAY,
                                                              TDAT_LKG,
                                                              TDAT_ZUTX,
                                                              TDAT_TEXT_ID,
                                                              TDAT_SDATA,
                                                              TDAT_SDATB,
                                                              TDAT_PTAB,
                                                              TDAT_PTBI };

    // Die Zuordnungstabellen
    private HashMap<String, String> pmRefMapping;
    private HashMap<String, String> modelMapping;
    private Map<iPartsProductModelsId, String> productModelLastChangeDates;

    private String tableName = TABLE_DA_PRODUCT_MODELS;
    private String importFileName;

    private boolean isSingleCall = false;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean doBufferSave = true;

    private Map<String, Set<String>> productToModelsMap;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     * @param withHeader : Enhtält die zu importierende Datei eine Kopfzeile, oder nicht.
     */
    public MigrationDialogProductModelsImporter(EtkProject project, boolean withHeader) {

        super(project, "DIALOG BMRE/TDAT", withHeader,
              new FilesImporterFileListType(TABLE_DA_PRODUCT_MODELS, "!!DIALOG BMRE/TDAT", true, false, false, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_CSV }));

        initMapping();
    }

    /**
     * Das Mapping für die Felder in die verschiedenen Datenbanktabellen
     */
    private void initMapping() {

        // DA_PRODUCT_MODELS - Referenz Produkt zu Baumuster
        pmRefMapping = new HashMap<>();
        //
        pmRefMapping.put(FIELD_DPM_PRODUCT_NO, BMRE_NR); // Produkt
        pmRefMapping.put(FIELD_DPM_MODEL_NO, BMRE_BM);   // Baumuster
        pmRefMapping.put(FIELD_DPM_STEERING, TDAT_LKG);  // Lenkung
        pmRefMapping.put(FIELD_DPM_TEXTNR, TDAT_ZUTX);  // Zusatztext, mehrsprachig mit externer ...
        //DAIMLER-3148: freischalten wenn Importdata vorhanden
        pmRefMapping.put(FIELD_DPM_VALID_FROM, TDAT_PTAB);  // Gültigkeit von
        pmRefMapping.put(FIELD_DPM_VALID_TO, TDAT_PTBI);  // Gültigkeit bis

        // Mapping DB-Felder für [Baumuster = DA_MODEL = TABLE_DA_MODEL]
        modelMapping = new HashMap<>();
        //
        modelMapping.put(FIELD_DM_MODEL_NO, BMRE_BM);
        modelMapping.put(FIELD_DM_MODEL_VISIBLE, BMRE_DISPLAY);
//        modelMapping.put(FIELD_DM_DATA, TDAT_SDATA); // DM_DATA aktuell nicht importieren, um vorhandene Daten nicht zu überschreiben
//        modelMapping.put(FIELD_DM_DATB, TDAT_SDATB); // DM_DATB aktuell nicht importieren, um vorhandene Daten nicht zu überschreiben
    }


    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

        String[] mustExists = new String[]{ BMRE_NR, BMRE_BM, BMRE_DISPLAY, TDAT_LKG, TDAT_ZUTX, TDAT_TEXT_ID };
        String[] mustHaveData = new String[]{ BMRE_NR, BMRE_BM, BMRE_DISPLAY };

        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {

        //nur für den EinzelTest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
            isSingleCall = true;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }


    @Override
    protected void preImportTask() {
        productModelLastChangeDates = new HashMap<>();
        productToModelsMap = new HashMap<>();
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        ProductModelsImportHelper pmRefHelper = new ProductModelsImportHelper(getProject(), pmRefMapping, tableName);

        // Checken, ob das Produkt bereits existiert, ...
        String productIDStr = pmRefHelper.handleValueOfSpecialField(BMRE_NR, importRec);
        iPartsProductId productIdObj = new iPartsProductId(productIDStr);
        iPartsDataProduct productObj;
        if (isSingleCall) {
            productObj = getCatalogImportWorker().createProductData(this, productIDStr, DBActionOrigin.FROM_EDIT);
        } else {
            productObj = getCatalogImportWorker().getProductData(productIdObj);
        }
        // ... und falls nicht, überspringen.
        if (productObj == null) {
            if (!Thread.currentThread().isInterrupted()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, Produkt %2 existiert nicht!", String.valueOf(recordNo),
                                                            productIdObj.toStringForLogMessages()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
            reduceRecordCount();
            return;
        }

        // Baumuster (BMRE_BM) Stringlänge überprüfen und im Fehlerfall den Import des Records überspringen.
        String modelIdStr = pmRefHelper.handleValueOfSpecialField(BMRE_BM, importRec);
        final int constModelIdStringLen = 7;
        if (modelIdStr.length() != constModelIdStringLen) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wegen Baumuster \"%2\" übersprungen, Stringlänge Baumuster %3 <> %4", String.valueOf(recordNo), modelIdStr,
                                                        String.valueOf(modelIdStr.length()), String.valueOf(constModelIdStringLen)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        // Check ob Baumuster in DB -> wenn nicht vorhanden, dann ABBRUCH!!
        iPartsDataModel dataModel = getCatalogImportWorker().addModel(modelIdStr);
        if (dataModel == null) {
            // Baumuster existiert nicht in DB
            getMessageLog().fireMessage(translateForLog("!!Record %1, AS-Baumuster-Stammdaten \"%2\" nicht vorhanden ==> wird ignoriert", String.valueOf(recordNo), modelIdStr));
            reduceRecordCount();
            return;
        }

        // Produkt zu Baumuster Zuordnung erzeugen und mit Daten füllen
        iPartsDataProductModels dataProductModel = null;
        iPartsProductModelsId productModelId = new iPartsProductModelsId(productObj.getAsId().getProductNumber(), modelIdStr);

        // Baumuster zum Produkt merken, um später alle nicht mehr dem Produkt zugeordneten Baumuster aus dem Produkt zu entfernen
        Set<String> modelsSet = productToModelsMap.get(productModelId.getProductNumber());
        if (modelsSet == null) {
            modelsSet = new TreeSet<>();
            productToModelsMap.put(productModelId.getProductNumber(), modelsSet);
        }
        modelsSet.add(productModelId.getModelNumber());

        String lastModifiedDate = productModelLastChangeDates.get(productModelId);
        // Kein HandleSpacialValues, da sonst die Millisekunden in dem String abgeschnitten werden
        String currentModifiedDate = importRec.get(TDAT_SDATB);

        /**
         * Wir übernehmen nur den neuesten Änderungsstand. Neuere Änderungsstände überschreiben frühere Zustände. Am
         * Ende haben wir also nur die Daten des neuesten Standes gespeichert.
         */
        if ((lastModifiedDate == null) || (0 > lastModifiedDate.compareTo(currentModifiedDate))) {
            // neuerer Stand gefunden

            productModelLastChangeDates.put(productModelId, currentModifiedDate);

            // Suche in der Liste der Models im Produkt, ob dieses Model schon in der Liste ist; wenn nicht neu erzeugen
            for (iPartsDataProductModels model : productObj.getProductModelsList()) {
                if (model.getAsId().equals(productModelId)) {
                    // Das Model ist schon vorhanden, aber veraltet
                    dataProductModel = model;
                    break;
                }
            }
            if (dataProductModel == null) {
                dataProductModel = new iPartsDataProductModels(getProject(), productModelId);
                getCatalogImportWorker().addProductModel(productObj, dataProductModel, DBActionOrigin.FROM_EDIT);
                dataProductModel.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
            }

            // dieses Baumuster mit Record aktualisieren
            pmRefHelper.fillOverrideCompleteDataForMADReverse(dataProductModel, importRec, iPartsMADLanguageDefs.MAD_DE);

            // Den Text zusammen mit der Text-ID übernehmen
            String foreignTextId = pmRefHelper.handleValueOfSpecialField(TDAT_TEXT_ID, importRec);
            String additionalText = pmRefHelper.handleValueOfSpecialField(TDAT_ZUTX, importRec);
            // Den Text mit samt der Text-ID übernehmen, falls etwas enthalten ist.
            if ((foreignTextId != null) && (!foreignTextId.isEmpty()) && (!additionalText.isEmpty())) {

                DictTextKindTypes importType = DictTextKindTypes.DIALOG_MODEL_ADDTEXT;

                EtkMultiSprache multiEdit = dataProductModel.getFieldValueAsMultiLanguage(FIELD_DPM_TEXTNR);
                if (multiEdit == null) {
                    multiEdit = new EtkMultiSprache();
                    multiEdit.setText(Language.DE, additionalText);
                }
                DictImportTextIdHelper dictImportHelper = new DictImportTextIdHelper(getProject());
                boolean dictSuccessful = dictImportHelper.handleDictTextId(importType, multiEdit, foreignTextId, DictHelper.getMADForeignSource(),
                                                                           false, TableAndFieldName.make(tableName, FIELD_DPM_TEXTNR));

                if (!dictSuccessful || dictImportHelper.hasWarnings()) {
                    //Fehler beim Dictionary Eintrag
                    for (String str : dictImportHelper.getWarnings()) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }

                    if (!dictSuccessful) {
                        // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                        String msg = translateForLog("!!Fehler beim Importieren von %1, Record Nr %2, Text-ID: \"%3\", Text: \"%4\"",
                                                     importFileName, String.valueOf(recordNo), foreignTextId, additionalText);
                        cancelImport(msg);
                    } else {
                        reduceRecordCount();
                    }
                    return; // Import nicht abbrechen, aber diesen Datensatz aufgrund der Warnungen überspringen
                }

                dataProductModel.setFieldValueAsMultiLanguage(FIELD_DPM_TEXTNR, multiEdit, DBActionOrigin.FROM_EDIT);

            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 %2 enthält weder Text-ID noch Suchtext.", String.valueOf(recordNo),
                                                            productIdObj.toStringForLogMessages()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }

        // Die Daten (speziell das Flag DM_MODEL_VISIBLE) für das AS-Baumuster aktualisieren
        ProductModelsImportHelper modelHelper = new ProductModelsImportHelper(getProject(), modelMapping, TABLE_DA_MODEL);
        modelHelper.fillOverrideCompleteDataForMADReverse(dataModel, importRec, iPartsMADLanguageDefs.MAD_DE);
        if (importToDB) {
            saveToDB(dataModel);
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        super.postImportTask();

        if (!isCancelled()) {
            // Nachbehandlung für alle Product speziell für AggregateTyp = 'G' (wird umgesetzt nach GA oder GM)
            // Speichern ist unnötig, da alle Produkte nach POSD-Importer gespeichert werden
            for (iPartsDataProduct dataProduct : getCatalogImportWorker().getProductList()) {
                iPartsDataProductModelsList productModelsList = iPartsDataProductModelsList.loadDataProductModelsList(getProject(), dataProduct.getAsId());

                // Nicht mehr dem Produkt zugeordnete Baumuster aus dem Produkt entfernen
                String productNumber = dataProduct.getAsId().getProductNumber();
                Set<String> modelsSet = productToModelsMap.get(productNumber);
                if (modelsSet != null) {
                    boolean modelRemoved = false;

                    // getAsList() ist notwendig, weil aus productModelsList Einträge entfernt werden sollen
                    for (iPartsDataProductModels dataProductModel : productModelsList.getAsList()) {
                        String modelNumber = dataProductModel.getAsId().getModelNumber();
                        if (!modelsSet.contains(modelNumber)) {
                            getMessageLog().fireMessage(translateForLog("!!Baumuster \"%1\" ist nicht mehr dem Produkt \"%2\" zugeordnet und wird daraus entfernt",
                                                                        modelNumber, productNumber),
                                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            productModelsList.delete(dataProductModel, DBActionOrigin.FROM_EDIT);
                            modelRemoved = true;
                        }
                    }

                    if (modelRemoved) {
                        productModelsList.saveToDB(getProject(), false);
                    }
                }

                String aggType;
                if (dataProduct.getAggType().equalsIgnoreCase("G")) {
                    aggType = "";
                } else {
                    aggType = dataProduct.getAggType();
                }

                List<String> warnings = new DwList<>();
                for (iPartsDataProductModels dataProductModel : productModelsList) {
                    iPartsDataModel dataModel = getCatalogImportWorker().getModel(new iPartsModelId(dataProductModel.getAsId().getModelNumber()));
                    if (dataModel != null) {
                        if (aggType.isEmpty()) {
                            aggType = dataModel.getModelType();
                        } else {
                            if (!aggType.equals(dataModel.getModelType())) {
                                // für Meldung
                                warnings.add(translateForLog("!!%1 (%2)", dataModel.getAsId().getModelNumber(), dataModel.getModelType()));
                            }
                        }
                    }
                }
                if (!aggType.isEmpty()) {
                    String currentAggType = dataProduct.getAggType();
                    if (!currentAggType.equalsIgnoreCase(aggType)) {
                        // Umsetzung melden
                        getMessageLog().fireMessage(translateForLog("!!Produkt \"%1\" Aggregatetyp umgesetzt zu \"%2\"", productNumber, aggType),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                        dataProduct.setFieldValue(FIELD_DP_AGGREGATE_TYPE, aggType, DBActionOrigin.FROM_EDIT);
                    }

                    if (!warnings.isEmpty()) {
                        StringBuilder str = new StringBuilder();
                        str.append(translateForLog("!!Beim Produkt \"%1\" mit Aggregatetyp (%2) besitzen folgende Baumuster einen unterschiedlichen Aggregatetyp:",
                                                   productNumber, dataProduct.getAggType()));
                        for (String warn : warnings) {
                            str.append(" ");
                            str.append(warn);
                        }
                        getMessageLog().fireMessage(str.toString(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }
                }
            }
        }
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.DIALOG_MODEL_ADDTEXT));
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }


    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        importFileName = importFile.extractFileName(true);
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', withHeader, headerNames));
        }
        return false;
    }


    /**
     * Der Helper zur Umwandlung der zu importierenden Daten
     */
    private class ProductModelsImportHelper extends MADImportHelper {

        public ProductModelsImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {

            // In den Beispieldaten war anfangs für leere Felder der String "(null)" enthalten.
            // Diesen Sonderfall und richtige NULL-Werte abfangen.
            if ((StrUtils.stringContains(value, MAD_NULL_VALUE)) || (value == null)) {
                value = "";

                // Display auf gültige Werte überprüfen
            } else if (sourceField.equals(BMRE_DISPLAY)) {
                if (!(value.equals("Y")) && (!(value.equals("N")))) {
                    value = "";
                }

                // Das SDATA-Datum "2016-02-23-17.12.25.818624" konvertieren
            } else if (sourceField.equals(TDAT_SDATA)) {
                value = getMADDateTimeValue(value);

                // Das SDATB-Datum "2016-02-23-17.12.25.859956" konvertieren
            } else if (sourceField.equals(TDAT_SDATB)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(TDAT_PTAB)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(TDAT_PTBI)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(TDAT_ZUTX)) {
                value = DictMultilineText.getInstance().convertDictText(DictTextKindTypes.DIALOG_MODEL_ADDTEXT, value);
            } else if (sourceField.equals(TDAT_TEXT_ID)) {
                value = iPartsTermIdHandler.removeLeadingZerosFromTermId(value);
            }

            return value;
        }

    }

}