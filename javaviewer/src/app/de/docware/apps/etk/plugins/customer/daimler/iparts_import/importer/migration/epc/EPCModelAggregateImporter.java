/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;


import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEPCLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * EPC Importer für Fahrzeug-Aggregate-Struktur
 */

public class EPCModelAggregateImporter extends AbstractEPCDataImporter {

    // Die Spalten der Importdatei BM_SPM
    private final static String MODEL_AGG_MODEL = "MODEL";
    private final static String MODEL_AGG_CATNUM = "CATNUM";
    private final static String MODEL_AGG_LANG = "LANG";
    private final static String MODEL_AGG_SEQNUM = "SEQNUM";
    private final static String MODEL_AGG_FRONTAX = "FRONTAX";
    private final static String MODEL_AGG_STEER = "STEER";
    private final static String MODEL_AGG_REARAX = "REARAX";
    private final static String MODEL_AGG_CONTFLG = "CONTFLG";
    private final static String MODEL_AGG_A_BODY = "A_BODY";
    private final static String MODEL_AGG_DIST = "DIST";
    private final static String MODEL_AGG_ENGINE = "ENGINE";
    private final static String MODEL_AGG_PLATFRM = "PLATFRM";
    private final static String MODEL_AGG_AUTO = "AUTO";
    private final static String MODEL_AGG_SALESD = "SALESD";
    private final static String MODEL_AGG_NUMRECS = "NUMRECS";
    private final static String MODEL_AGG_MANUAL = "MANUAL";
    private final static String MODEL_AGG_TEXT = "TEXT";
    private final static String MODEL_AGG_FUELCELL = "FUELCELL";
    private final static String MODEL_AGG_HVBATTERY = "HVBATTERY";
    private final static String MODEL_AGG_EMOTOR = "EMOTOR";
    private final static String MODEL_AGG_EXHAUSTSYS = "EXHAUSTSYS";

    // Die Spalten der Importdatei BM_AGGCHASS
    private final static String MODEL_AGG_CHASS_MODEL = "MODEL";
    private final static String MODEL_AGG_CHASS_CATNUM = "CATNUM";
    private final static String MODEL_AGG_CHASS_CHASSIS = "CHASSIS";

    // Ein kleiner Cache, damit die Produkte nicht immer wieder aus der DB Geladen werden.
    private Map<iPartsProductId, Boolean> productRelevanceCache = new HashMap<>();

    private CombinedSPMImportRecord currentSPMImportRecord;

    // Set, das alle vor dem Import in der Datenbank existierenden Baumusternummern mit Sachnummernkennbuchstaben enthält.
    // Das Set wird laufend mit allen neu hinzugekommenen aus BEIDEN Importern befüllt.
    private Set<String> allExistingModelNumbers = new HashSet<>();
    private Map<String, iPartsDataModel> allEPCOrAPPLISTModels = new HashMap<>();
    private Map<EPCAggregateTypes, String> aggImportNamesMap = new HashMap<>();
    private Set<String> invalidModelNumbers = new HashSet<>();
    private Set<iPartsModelsAggsId> handledModelAggsSet;

    private static final String TABLENAME_BM_AGGCHASS = "BM_AGGCHASS";
    private static final String TABLENAME_BM_SPM = "BM_SPM";

    boolean importToDB = true;

    /**
     * extrahiert alle Baumusternummern, ohne Prefix, aus dem übergebenen String
     *
     * @param importer
     * @param modelNumbersUnformatted
     * @return
     */
    public static Set<String> parseModelNumberString(AbstractDataImporter importer, String modelNumbersUnformatted) {

        Set<String> allModelNumbers = new HashSet<>();
        String remainingString = modelNumbersUnformatted;
        int indexOfDot = remainingString.indexOf('.');
        if (indexOfDot < 0) {
            // Sollte nicht passieren, dass kein Punkt existiert.
            return new HashSet<>();
        }
        String currentSeriesNumber;
        while (true) {
            int startOfSeriesNumber = indexOfDot - 3;
            if ((startOfSeriesNumber < 0) || (startOfSeriesNumber >= remainingString.length())) {
                return allModelNumbers;
            }
            currentSeriesNumber = remainingString.substring(startOfSeriesNumber, indexOfDot);
            // +1 um den Punkt zu überspringen. Damit enthält der restliche String dann am Anfang alle Nummern, die zusammen mit der
            // Baureihennummer, die Baumusternummer ergeben.
            int indexAfterDot = indexOfDot + 1;
            if ((indexAfterDot < 0) || (indexAfterDot >= remainingString.length())) {
                return allModelNumbers;
            }
            remainingString = remainingString.substring(indexAfterDot, remainingString.length());
            indexOfDot = remainingString.indexOf('.');
            // Der restliche String wird bis 3 Stellen vor dem nächsten Punkt, oder bis zum Ende interpretiert, da die 3 Stellen
            // vor dem nächsten Punkt die nächste Baureihennummer darstellen, die im nächsten Durchgang interpretiert wird.
            if (indexOfDot >= 6) {
                combineSeriesAndModelNumbers(importer, allModelNumbers, currentSeriesNumber, remainingString.substring(0, indexOfDot - 3));
            } else {
                combineSeriesAndModelNumbers(importer, allModelNumbers, currentSeriesNumber, remainingString);
                break;
            }
        }
        return allModelNumbers;
    }


    public static void combineSeriesAndModelNumbers(AbstractDataImporter importer, Set<String> allModelNumbers,
                                                    String currentSeriesNumber, String currentModelNumbersWithoutSeries) {
        // Alles rausschmeißen, was keine Zahl oder Minus ist, da Trennzeichen nicht einheitlich sind und stellenweise doppelt oder gar nicht vorkommen.
        String modelNumbersWithoutSeries = currentModelNumbersWithoutSeries.replaceAll("[^0-9^-]", "");
        // Liste der Elemente mit Minuszeichen bilden
        List<String> minusList = new DwList<>(modelNumbersWithoutSeries.split("-"));
        // Minusliste auf Modulo 3 abchecken
        boolean isValid = !minusList.isEmpty();
        if (isValid) {
            for (String test : minusList) {
                if ((test.length() % 3) != 0) {
                    isValid = false;
                    break;
                }
            }
        }

        if (!isValid) {
            if (importer != null) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Fehler in den Importdaten. Baumusternummern konnten nicht interpretiert werden: %1. Sie werden nicht importiert",
                                                                              currentModelNumbersWithoutSeries),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
            return;
        }
        // 1. Element der MinusListe in 3er Gruppen zerlegen
        List<String> currentModelNumbersWithoutSeriesList = StrUtils.splitStringIntoSubstrings(minusList.get(0), 3);
        String ninerModelPrefix = "";
        if (currentModelNumbersWithoutSeriesList.size() == 1) {
            // nur 1 Element vorhanden => merken als Prefix
            ninerModelPrefix = currentModelNumbersWithoutSeriesList.get(0);
        }
        // Schleife über die restlichen Elemente der MinusListe
        for (int lfdNr = 1; lfdNr < minusList.size(); lfdNr++) {
            String ninerModel = "";
            // aktuelles Element der MinusListe in 3er Gruppen zerlegen
            List<String> helpList = StrUtils.splitStringIntoSubstrings(minusList.get(lfdNr), 3);
            if (helpList.size() > 1) {
                // mehr als eine 3er Gruppe vorhanden => hole Prefix aus dem vorherigen Element
                ninerModelPrefix = currentModelNumbersWithoutSeriesList.get(currentModelNumbersWithoutSeriesList.size() - 1);
                // bilde 9er BM
                ninerModel = ninerModelPrefix + helpList.get(0);
                currentModelNumbersWithoutSeriesList.set(currentModelNumbersWithoutSeriesList.size() - 1, ninerModel);
            } else {
                // nur eine 3er Gruppe vorhanden => bilde 9er BM
                if (StrUtils.isEmpty(ninerModelPrefix)) {
                    // hole Prefix aus vorherigem Element
                    ninerModelPrefix = currentModelNumbersWithoutSeriesList.get(currentModelNumbersWithoutSeriesList.size() - 1);
                }
                // bilde 9er BM
                ninerModel = ninerModelPrefix + helpList.get(0);
                if (lfdNr > 1) {
                    // mehrere - Elemente nacheinander => addiere zur Liste
                    currentModelNumbersWithoutSeriesList.add(ninerModel);
                } else {
                    // zum ersten Mal aufgetreten => ändere vorheriges Element
                    currentModelNumbersWithoutSeriesList.set(currentModelNumbersWithoutSeriesList.size() - 1, ninerModel);
                }
            }
            // entferne bearbeitete Element aus der 3er Gruppen Liste
            helpList.remove(0);
            // addiere den Rest dazu
            currentModelNumbersWithoutSeriesList.addAll(helpList);
        }

        // bilde richtige BM Nummer
        for (String modelNumberWithoutSeries : currentModelNumbersWithoutSeriesList) {
            allModelNumbers.add(currentSeriesNumber + modelNumberWithoutSeries);
        }
    }

    public EPCModelAggregateImporter(EtkProject project) {
        // Die Zieltabelle (keine konkrete Tabelle angegeben, da sowohl in DA_PRODUCT_MODELS als auch in DA_MODELS_AGGS importiert wird)
        super(project, "EPC Model-Agg-Structure",
              new FilesImporterFileListType(TABLENAME_BM_AGGCHASS, "!!EPC BM_AGGCHASS", true, true, true,
                                            new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_GZ,
                                                          MimeTypes.EXTENSION_ALL_FILES }),
              new FilesImporterFileListType(TABLENAME_BM_SPM, "!!EPC BM_SPM", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_GZ,
                                                          MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    @Override
    protected String[] getHeaderNames() {
        if (getTablename().equals(TABLENAME_BM_SPM)) {
            return new String[]{
                    MODEL_AGG_MODEL,
                    MODEL_AGG_CATNUM,
                    MODEL_AGG_LANG,
                    MODEL_AGG_SEQNUM,
                    MODEL_AGG_FRONTAX,
                    MODEL_AGG_STEER,
                    MODEL_AGG_REARAX,
                    MODEL_AGG_CONTFLG,
                    MODEL_AGG_A_BODY,
                    MODEL_AGG_DIST,
                    MODEL_AGG_ENGINE,
                    MODEL_AGG_PLATFRM,
                    MODEL_AGG_AUTO,
                    MODEL_AGG_SALESD,
                    MODEL_AGG_NUMRECS,
                    MODEL_AGG_MANUAL,
                    MODEL_AGG_TEXT,
                    MODEL_AGG_FUELCELL,
                    MODEL_AGG_EXHAUSTSYS
            };
        } else {
            return new String[]{
                    MODEL_AGG_CHASS_MODEL,
                    MODEL_AGG_CHASS_CATNUM,
                    MODEL_AGG_CHASS_CHASSIS
            };
        }
    }

    @Override
    protected HashMap<String, String> initMapping() {
        aggImportNamesMap = new HashMap<>();
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_FRONTAX, MODEL_AGG_FRONTAX);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_STEER, MODEL_AGG_STEER);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_REARAX, MODEL_AGG_REARAX);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_A_BODY, MODEL_AGG_A_BODY);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_DIST, MODEL_AGG_DIST);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_ENGINE, MODEL_AGG_ENGINE);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_PLATFRM, MODEL_AGG_PLATFRM);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_AUTO, MODEL_AGG_AUTO);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_MANUAL, MODEL_AGG_MANUAL);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_FUELCELL, MODEL_AGG_FUELCELL);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_HVBATTERY, MODEL_AGG_HVBATTERY);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_EMOTOR, MODEL_AGG_EMOTOR);
        aggImportNamesMap.put(EPCAggregateTypes.AGGREGATE_TYPE_EXHAUSTSYS, MODEL_AGG_EXHAUSTSYS);
        // Hier kein Mapping notwendig, da alle Werte manuell gesetzt werden müssen
        return new HashMap<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        if (getTablename().equals(TABLENAME_BM_SPM)) {
            String[] mustFields = new String[]{ MODEL_AGG_MODEL, MODEL_AGG_CATNUM };
            importer.setMustExists(mustFields);
            importer.setMustHaveData(mustFields);
        } else {
            String[] mustFields = new String[]{ MODEL_AGG_CHASS_MODEL, MODEL_AGG_CHASS_CATNUM, MODEL_AGG_CHASS_CHASSIS };
            importer.setMustExists(mustFields);
            importer.setMustHaveData(mustFields);
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        setTablename(importFileType.getFileListType());
        setHeaderNames(getHeaderNames());
        setFieldMapping(initMapping());
        return super.importFile(importFileType, importFile);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        // Bevor der AGGCHASS-Importer startet alle Baumuster einmalig aus der Datenbank laden. Die Liste wird dann für
        // beide Importer verwendet und im Laufe beider weiter befüllt, wenn neue Datenbankeinträge angelegt werden.
        if (getTablename().equals(TABLENAME_BM_AGGCHASS)) {
            allExistingModelNumbers = new HashSet<>();
            handledModelAggsSet = new HashSet<>();
            iPartsDataModelList modelList = iPartsDataModelList.loadAllDataModelList(getProject(), DBDataObjectList.LoadType.COMPLETE);
            for (iPartsDataModel dataModel : modelList) {
                if (isSourceEPCorAPPLIST(dataModel)) {
                    allEPCOrAPPLISTModels.put(dataModel.getAsId().getModelNumber(), dataModel);
                }
                allExistingModelNumbers.add(dataModel.getAsId().getModelNumber());
            }
        }
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        if (getTablename().equals(TABLENAME_BM_SPM)) {
            importBMSPMRecord(importRec, recordNo);
        } else {
            importBMAGGCHASSRecord(importRec, recordNo);
        }
    }

    /**
     * Importer für BM_AGGCHASS. Legt die Verknüpfungen zwischen Produkt zu Fahrzeugbaumuster (TABLE_DA_PRODUCT_MODELS)
     * und Fahrzeugbaumuster zu Aggregatebaumuster (TABLE_DA_MODELS_AGGS) an.
     *
     * @param importRec
     * @param recordNo
     */
    private void importBMAGGCHASSRecord(Map<String, String> importRec, int recordNo) {
        EPCImportHelper helper = new ModelAggregateImportHelper(getProject(), getFieldMapping());
        String productNo = helper.handleValueOfSpecialField(MODEL_AGG_CHASS_CATNUM, importRec).trim();

        if (!helper.isProductRelevantForImport(this, productNo, productRelevanceCache, recordNo)) {
            reduceRecordCount();
            return;
        }

        String chassisModelNo = iPartsModel.MODEL_NUMBER_PREFIX_CAR + helper.handleValueOfSpecialField(MODEL_AGG_CHASS_CHASSIS, importRec).trim();
        String aggregateModelNo = iPartsModel.MODEL_NUMBER_PREFIX_AGGREGATE + helper.handleValueOfSpecialField(MODEL_AGG_CHASS_MODEL, importRec).trim();
        if (!areModelNumbersValid(recordNo, chassisModelNo, aggregateModelNo)) {
            reduceRecordCount();
            return;
        }
        updateOrCreateModelIfNotExists(recordNo, chassisModelNo, null, null);
        updateOrCreateModelIfNotExists(recordNo, aggregateModelNo, null, null);

        iPartsProductModelsId productModelsId = new iPartsProductModelsId(productNo, chassisModelNo);
        iPartsDataProductModels productModels = new iPartsDataProductModels(getProject(), productModelsId);

        boolean importProductModels = false;
        boolean importModelAggs = false;
        // EPC-Produkt wurde durch EPC oder Applikationsliste angelegt, sonst wären wir hier schon rausgeflogen.
        if (!productModels.existsInDB()) {
            // Zu diesem für den Import gültigen Produkt jetzt den fehlenden Eintrag in DA_PRODUCT_MODEL anlegen.
            productModels.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
            importProductModels = true;
        }

        iPartsModelsAggsId modelsAggsId = new iPartsModelsAggsId(chassisModelNo, aggregateModelNo);
        iPartsDataModelsAggs modelsAggs = new iPartsDataModelsAggs(getProject(), modelsAggsId);
        if (!handledModelAggsSet.contains(modelsAggsId)) {
            if (!modelsAggs.existsInDB()) {
                modelsAggs.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                modelsAggs.setFieldValue(iPartsConst.FIELD_DMA_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
                importModelAggs = true;
            } else if (isSourceEPCorAPPLIST(modelsAggs, recordNo)) {
                importModelAggs = modelsAggs.isModifiedWithChildren();
            }
            handledModelAggsSet.add(modelsAggsId);
        }

        if (!importProductModels && !importModelAggs) {
            // Der ImportRecord hat keine einzige Änderung in der Datenbank bewirkt
            reduceRecordCount();
            return;
        }

        if (importToDB) {
            boolean isSkipped = false;
            if (importProductModels) {
                isSkipped = !saveToDB(productModels);
            }
            if (importModelAggs) {
                saveToDB(modelsAggs);
                if (isSkipped) {
                    // geradeziehen
                    skippedRecords--;
                }
            }
            addInfoText(recordNo, importProductModels, productModels, importModelAggs, modelsAggs);
        }
    }

    private boolean areModelNumbersValid(int recordNo, String chassisModelNo, String aggregateModelNo) {
        boolean isModelNumberValid = isEPCModelNumberValid(chassisModelNo);
        boolean isAggModelNumberValid = isEPCModelNumberValid(aggregateModelNo);
        if (!isModelNumberValid || !isAggModelNumberValid) {
            String msg;
            if (!isModelNumberValid) {
                if (!isAggModelNumberValid) {
                    msg = translateForLog("!!Record %1 mit ungültiger Baumuster- und Aggregate-Baumuster Nummer \"%2\", \"%3\" übersprungen.",
                                          String.valueOf(recordNo), chassisModelNo.substring(1), aggregateModelNo.substring(1));
                } else {
                    msg = translateForLog("!!Record %1 mit ungültiger Baumusternummer \"%2\" übersprungen.",
                                          String.valueOf(recordNo), chassisModelNo.substring(1));
                }
            } else {
                msg = translateForLog("!!Record %1 mit ungültiger Aggregate-Baumusternummer \"%2\" übersprungen.",
                                      String.valueOf(recordNo), aggregateModelNo.substring(1));
            }
            getMessageLog().fireMessage(msg, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return false;
        }
        return true;
    }

    private boolean isEPCModelNumberValid(String modelNo) {
        boolean isModelNoValid = iPartsModel.isModelNumberValid(modelNo);
        if (!isModelNoValid) {
            // spezielle Überprüfung, da isModelNumberValid() bei 'CFA1060' false liefert
            isModelNoValid = !modelNo.contains("-");
        }
        return isModelNoValid;
    }

    private void addInfoText(int recordNo, boolean importProductModels, iPartsDataProductModels productModels, boolean importModelAggs, iPartsDataModelsAggs modelsAggs) {
        String buildText = "";
        if (importProductModels) {
            if (importModelAggs) {
                buildText = translateForLog("!!Record %1; Neuen Eintrag in Product_Models (Produkt \"%2\", BM \"%3\") und Model_Aggs (BM \"%4\", Agg-BM \"%5\") angelegt",
                                            String.valueOf(recordNo),
                                            productModels.getAsId().getProductNumber(), productModels.getAsId().getModelNumber(),
                                            modelsAggs.getAsId().getModelNumber(), modelsAggs.getAsId().getAggregateModelNumber());
            } else {
                buildText = translateForLog("!!Record %1; Neuen Eintrag in Product_Models (Produkt \"%2\", BM \"%3\") angelegt",
                                            String.valueOf(recordNo),
                                            productModels.getAsId().getProductNumber(), productModels.getAsId().getModelNumber());
            }
        } else {
            if (importModelAggs) {
                buildText = translateForLog("!!Record %1; Neuer Eintrag in Model_Aggs (BM \"%2\", Agg-BM \"%3\") angelegt",
                                            String.valueOf(recordNo),
                                            modelsAggs.getAsId().getModelNumber(), modelsAggs.getAsId().getAggregateModelNumber());
            }
        }
        if (!buildText.isEmpty()) {
            // Meldung nur ausgeben, wenn etwas angelegt wurde
            getMessageLog().fireMessage(buildText, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                        MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }
    }

    /**
     * Legt ein neues EPC-Baumuster an, falls es noch nicht in der Datenbank existiert, oder updated bereits in der Datenbank
     * oder durch den Importer bekannte Baumuster mit den übergebenen Werten. Nur Baumuster, die Quelle EPC oder APPLIST haben
     * werden geupdatet.
     *
     * @param recordNo
     * @param modelNo
     * @param modelSalesDesc
     * @param aggregateType  der zu setzende Aggregatetyp. Wird nur bei D-Baumustern, gesetzt.
     *                       Bei C-Baumustern wird immer AGGREGATE_TYPE_CAR gesetzt
     */
    private void updateOrCreateModelIfNotExists(int recordNo, String modelNo, EtkMultiSprache modelSalesDesc, String aggregateType) {
        iPartsModelId modelId = new iPartsModelId(modelNo);
        iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
        if (!allExistingModelNumbers.contains(modelNo)) {
            if (!dataModel.existsInDB()) {
                dataModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            allExistingModelNumbers.add(modelNo);
            // Baumuster existiert noch nicht also wird es mit Quelle EPC angelegt
            allEPCOrAPPLISTModels.put(modelNo, dataModel);
        }
        dataModel = allEPCOrAPPLISTModels.get(modelNo);
        if (dataModel == null) {
            // Baumuster existiert mit Quelle ungleich EPC oder APPLIST --> darf nicht geändert werden.
            return;
        }

        if (iPartsModel.isVehicleModel(modelNo)) {
            // Den Typ eines C-Baumusters ist immer "F" (Fahrzeug)
            dataModel.setFieldValue(FIELD_DM_MODEL_TYPE, AGGREGATE_TYPE_CAR, DBActionOrigin.FROM_EDIT);
        } else if ((aggregateType != null) && !aggregateType.isEmpty()) {
            String currentAggregateType = dataModel.getFieldValue(FIELD_DM_MODEL_TYPE);
            if (!StrUtils.isValid(currentAggregateType) || (iPartsModel.isAggregateModel(modelNo) && currentAggregateType.equals(AGGREGATE_TYPE_CAR))) {
                dataModel.setFieldValue(FIELD_DM_MODEL_TYPE, aggregateType, DBActionOrigin.FROM_EDIT);
            } else {
                if (!currentAggregateType.equals(aggregateType)) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1ff: AggregateTyp %2 in bestehendem BM %3 unterscheidet sich bei SPM (%4)",
                                                                String.valueOf(currentSPMImportRecord.startRecordNo),
                                                                currentAggregateType, dataModel.getAsId().getModelNumber(), aggregateType),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }
            }
        }

        dataModel.setFieldValue(FIELD_DM_SERIES_NO, StrUtils.copySubString(modelNo, 0, 4), DBActionOrigin.FROM_EDIT);
        dataModel.setFieldValue(FIELD_DM_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
        if (modelSalesDesc != null) {
            // Lexikontext für Verkaufsbezeichnung des Baumusters anlegen
            DictImportTextIdHelper dictImportTextIdHelper = new DictImportTextIdHelper(getProject());
            if (dictImportTextIdHelper.handleDictTextIdForEPC(DictTextKindEPCTypes.MODEL_DICTIONARY, modelSalesDesc,
                                                              "", DictHelper.getEPCForeignSource(),
                                                              TableAndFieldName.make(TABLE_DA_MODEL, FIELD_DM_SALES_TITLE))) {
                dataModel.setFieldValueAsMultiLanguage(FIELD_DM_SALES_TITLE, modelSalesDesc, DBActionOrigin.FROM_EDIT);
            } else {
                if (dictImportTextIdHelper.hasWarnings()) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1ff: Fehler beim Importieren der Verkaufsbezeichnung für " +
                                                                "das Baumuster \"%2\". Genauere Fehlerbeschreibung im Log.",
                                                                String.valueOf(currentSPMImportRecord.startRecordNo), modelNo),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    for (String message : dictImportTextIdHelper.getWarnings()) {
                        getMessageLog().fireMessage(translateForLog("!!Warnung zu \"%1\": %2",
                                                                    dataModel.getAsId().toString("|"), message),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                    MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }
                }
            }
        }
        if (dataModel.isNew()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1: Neuen Eintrag in Model (BM \"%2\") angelegt",
                                                        String.valueOf(recordNo),
                                                        dataModel.getAsId().getModelNumber()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                        MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }

    }

    private void importBMSPMRecord(Map<String, String> importRec, int recordNo) {

        EPCImportHelper helper = new ModelAggregateImportHelper(getProject(), getFieldMapping());
        String productNo = helper.handleValueOfSpecialField(MODEL_AGG_CATNUM, importRec).trim();

        if (!helper.isProductRelevantForImport(this, productNo, productRelevanceCache, recordNo)) {
            reduceRecordCount();
            return;
        }

        Set<String> modelNumbers = getModelNumbersWithPrefix(helper, importRec);

        for (String modelNumber : modelNumbers) {
            importBMSPMRecordForModel(recordNo, productNo, modelNumber, importRec, helper);
        }
    }

    private void importBMSPMRecordForModel(int recordNo, String productNo, String modelNo, Map<String, String> importRec, EPCImportHelper helper) {
        if (modelNo.isEmpty()) {
            return;
        }

        boolean isNewRecord = (currentSPMImportRecord == null) || (!productNo.equals(currentSPMImportRecord.productNumber) || !modelNo.equals(currentSPMImportRecord.modelNumber));
        // Produkt und Baumuster sind vorhanden, also starte den Import
        if (isNewRecord) {
            if (currentSPMImportRecord != null) {
                saveSPMImportRecord();
            }
            // Komplett neuer Datensatz fängt an
            currentSPMImportRecord = new CombinedSPMImportRecord(recordNo, productNo, modelNo);
        }

        iPartsEPCLanguageDefs langDef = iPartsEPCLanguageDefs.getType(helper.handleValueOfSpecialField(MODEL_AGG_LANG, importRec));
        if ((langDef == iPartsEPCLanguageDefs.EPC_UNKNOWN) || (langDef == iPartsEPCLanguageDefs.EPC_NEUTRAL)) {
            return;
        }

        // Texte vormerken, die in DA_MODEL und DA_PRODUCT_MODEL später gespeichert werden
        String productModelText = helper.handleValueOfSpecialField(MODEL_AGG_TEXT, importRec);
        currentSPMImportRecord.addProductModelText(langDef, productModelText);

        String modelSalesDesc = helper.handleValueOfSpecialField(MODEL_AGG_SALESD, importRec).trim();
        currentSPMImportRecord.addModelSalesDesc(langDef, modelSalesDesc);

        boolean isRedundant = currentSPMImportRecord.modelSalesDesc.getSprachenCount() > 1;

        // Wenn eine Zeile mit einer neuen Sprache kommt, dann kommen nur noch redundante Daten,
        // die sich nur noch in den Texten unterscheiden.
        if (!isRedundant) {
            if (iPartsModel.isVehicleModel(modelNo)) {
                // Es kann nur zu Fahrzeugbaumustern zugehörige Aggregatebaumuster geben.
                for (EPCAggregateTypes epcAggType : EPCAggregateTypes.values()) {
                    String importFieldName = aggImportNamesMap.get(epcAggType);
                    if (importFieldName != null) {
                        setAggregateModels(helper, importFieldName, importRec, epcAggType);
                    }
                }
            }
        } else {
            reduceRecordCount();
        }

    }

    private void saveSPMImportRecord() {

        String modelNumber = currentSPMImportRecord.modelNumber;

        updateOrCreateModelIfNotExists(currentSPMImportRecord.startRecordNo, modelNumber, currentSPMImportRecord.modelSalesDesc, null);

        iPartsProductModelsId productModelId = new iPartsProductModelsId(currentSPMImportRecord.productNumber, modelNumber);
        iPartsDataProductModels productModel = new iPartsDataProductModels(getProject(), productModelId);
        if (!productModel.loadFromDB(productModelId)) {
            productModel.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        } else {
            // MultiLang nachladen
            productModel.getFieldValueAsMultiLanguage(FIELD_DPM_TEXTNR);
        }

        // Lexikontext für Benennung von Produkt zu Baumuster anlegen
        DictImportTextIdHelper dictImportTextIdHelper = new DictImportTextIdHelper(getProject());
        if (dictImportTextIdHelper.handleDictTextIdForEPC(DictTextKindEPCTypes.MODEL_DICTIONARY, currentSPMImportRecord.productModelText,
                                                          "", DictHelper.getEPCForeignSource(), TableAndFieldName.make(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_TEXTNR))) {
            // Zu diesem für den Import gültigen Produkt wird der fehlende Eintrag in DA_PRODUCT_MODEL angelegt.
            productModel.setFieldValueAsMultiLanguage(FIELD_DPM_TEXTNR, currentSPMImportRecord.productModelText, DBActionOrigin.FROM_EDIT);
            if (importToDB) {
                if (saveToDB(productModel)) {
                    if (productModel.isNew()) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1ff: Neuen Eintrag in Product_Models (Produkt \"%2\", BM \"%3\") angelegt",
                                                                    String.valueOf(currentSPMImportRecord.startRecordNo),
                                                                    productModel.getAsId().getProductNumber(), productModel.getAsId().getModelNumber()),
                                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                                    MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }
                } else {
                    skippedRecords--;
                }
            }
        } else {
            if (dictImportTextIdHelper.hasWarnings()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1ff: Fehler beim Importieren der Benennung für " +
                                                            "Baumuster zu Produkt \"%2\". Genauere Fehlerbeschreibung im Log.",
                                                            String.valueOf(currentSPMImportRecord.startRecordNo),
                                                            productModel.getAsId().toString("|")),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                for (String message : dictImportTextIdHelper.getWarnings()) {
                    getMessageLog().fireMessage(translateForLog("!!Warnung zu \"%1\": %2",
                                                                productModel.getAsId().toString("|"), message),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
            }
        }

        if (iPartsModel.isVehicleModel(modelNumber)) {
            // Zu jedem Aggregatebaumuster die entsprechenden Einträge in DA_MODEL und DA_MODELS_AGGS anlegen
            int count = 0;
            for (Map.Entry<String, Set<String>> aggTypeWithAggModelNo : currentSPMImportRecord.aggTypesWithAggModelNo.entrySet()) {
                Set<String> aggregateModelNumbers = new HashSet<>();
                String aggregateType = aggTypeWithAggModelNo.getKey();
                for (String aggregateModelNumbersUnformatted : aggTypeWithAggModelNo.getValue()) {
                    aggregateModelNumbers.addAll(parseModelNumberString(this, aggregateModelNumbersUnformatted));
                }
                for (String aggregateModelNumber : aggregateModelNumbers) {
                    aggregateModelNumber = iPartsModel.MODEL_NUMBER_PREFIX_AGGREGATE + aggregateModelNumber;

                    updateOrCreateModelIfNotExists(currentSPMImportRecord.startRecordNo, aggregateModelNumber, null, aggregateType);

                    iPartsModelsAggsId modelAggId = new iPartsModelsAggsId(modelNumber, aggregateModelNumber);
                    iPartsDataModelsAggs modelAgg = new iPartsDataModelsAggs(getProject(), modelAggId);
                    boolean modelAggExists = modelAgg.existsInDB();
                    if (!modelAggExists || isSourceEPCorAPPLIST(modelAgg)) {
                        if (!modelAggExists) {
                            modelAgg.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        }
                        modelAgg.setFieldValue(FIELD_DMA_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
                        if (importToDB) {
                            if (saveToDB(modelAgg)) {
                                if (modelAgg.isNew()) {
                                    getMessageLog().fireMessage(translateForLog("!!Record %1ff: Neuen Eintrag in Model_Aggs (BM \"%2\", Agg-BM \"%3\") angelegt",
                                                                                String.valueOf(currentSPMImportRecord.startRecordNo),
                                                                                modelAgg.getAsId().getModelNumber(), modelAgg.getAsId().getAggregateModelNumber()),
                                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                                                MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                                }
                            } else {
                                if (count > 0) {
                                    skippedRecords--;
                                }
                            }
                            count++;
                        }
                    }
                }
            }
            if (count == 0) {
                reduceRecordCount();
            }
        } else {
            reduceRecordCount();
        }
    }

    /**
     * Bestimmen der Baumusternummer(n). In der MODEL Spalte steht eine (möglicherweise auch mehrere) Baumusternummer ohne
     * Sachnummernkennbuchstabe. Die Methode liefert alle Baumusternummern mit Sachnummernkennbuchstabe.
     * Was dieser ist wissen wir, weil vorher über den BM_AGGCHASS Importer den Baumustern ihre Sachnummernkennbuchstaben
     * zugeteilt wurden und in allExistingModelNumbers gemerkt wurden (und in der DB angelegt). Bei nicht bereits bekannten
     * Baumustern, werden die Sachnummernkennbuchstaben mithilfe der Aggregatebaumusterspalten bestimmt.
     * Es ist auch möglich, dass für eine einzelne Baumusternummer sowohl ein C-Baumuster, als auch ein D-Baumuster
     * zurückgeliefert wird, wenn beide in der Datenbank existieren.
     *
     * @param helper
     * @param importRec
     * @return
     */
    private Set<String> getModelNumbersWithPrefix(EPCImportHelper helper, Map<String, String> importRec) {

        Set<String> modelNumbersWithPrefix = new HashSet<>();
        Set<String> modelNumbersWithoutPrefix = parseModelNumberString(this, helper.handleValueOfSpecialField(MODEL_AGG_MODEL, importRec).trim());

        for (String modelNoWithoutPrefix : modelNumbersWithoutPrefix) {
            boolean prefixFound = false;
            String modelNo = iPartsModel.MODEL_NUMBER_PREFIX_CAR + modelNoWithoutPrefix;
            if (allExistingModelNumbers.contains(modelNo)) {
                modelNumbersWithPrefix.add(modelNo);
                prefixFound = true;
            }
            modelNo = iPartsModel.MODEL_NUMBER_PREFIX_AGGREGATE + modelNoWithoutPrefix;
            if (allExistingModelNumbers.contains(modelNo)) {
                modelNumbersWithPrefix.add(modelNo);
                prefixFound = true;
            }

            if (!prefixFound) {
                String prefix;
                if (allAggModelsEmpty(helper, importRec)) {
                    prefix = iPartsModel.MODEL_NUMBER_PREFIX_AGGREGATE;
                } else {
                    prefix = iPartsModel.MODEL_NUMBER_PREFIX_CAR;
                }
                modelNo = prefix + modelNoWithoutPrefix;
                modelNumbersWithPrefix.add(modelNo);

                if (!invalidModelNumbers.contains(modelNoWithoutPrefix)) {
                    getMessageLog().fireMessage(translateForLog("!!Zur Baumusternummer \"%1\" wurde kein " +
                                                                "Fahrzeug- oder Aggregatebaumuster gefunden (%2). " +
                                                                "Sachnummernkennbuchstabe (%3) wurde aus den BM_SPM Daten bestimmt.",
                                                                modelNoWithoutPrefix,
                                                                helper.handleValueOfSpecialField(MODEL_AGG_MODEL, importRec).trim(),
                                                                prefix),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    invalidModelNumbers.add(modelNoWithoutPrefix);
                }
            }
        }
        return modelNumbersWithPrefix;
    }

    private boolean allAggModelsEmpty(EPCImportHelper helper, Map<String, String> importRec) {
        for (EPCAggregateTypes epcAggType : EPCAggregateTypes.values()) {
            String importFieldName = aggImportNamesMap.get(epcAggType);
            if (importFieldName != null) {
                String aggregateModel = helper.handleValueOfSpecialField(importFieldName, importRec).trim();
                if (!aggregateModel.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setAggregateModels(EPCImportHelper helper, String sourceField, Map<String, String> importRec, EPCAggregateTypes epcAggType) {
        String aggregateModelsFromImport = helper.handleValueOfSpecialField(sourceField, importRec).trim();
        currentSPMImportRecord.setAggregateModels(aggregateModelsFromImport, epcAggType.getEpcValue());
    }

    @Override
    protected void postImportTask() {
        if (getTablename().equals(TABLENAME_BM_SPM)) {
            // Den letzten Record noch speichern, da es natürlich keinen ImportRecord() Aufruf nach dem letzen Record gibt,
            // in welchem sonst gespeichert werden würde.
            if (currentSPMImportRecord != null) {
                saveSPMImportRecord();
            }
            // Alle im Import aufgesammelten EPC-Baumuster speichern
            if (importToDB) {
                for (iPartsDataModel model : allEPCOrAPPLISTModels.values()) {
                    if (!saveToDB(model)) {
                        skippedRecords--;
                    }
                }
            }
            // allExistingModelNumbers erst nach dem SPM Importer zurücksetzen, weil es von beiden Importern verwendet wird.
            allExistingModelNumbers = null;
            allEPCOrAPPLISTModels = null;
        }
        super.postImportTask();
        invalidModelNumbers.clear();
        handledModelAggsSet = null;
        getMessageLog().fireMessage(translateForLog("!!Überprüfung der Aggregatetypen zu jedem EPC Produkt."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        EPCImportHelper.calculateAggTypesForEPCProductsFromModels(getProject(), null, true);
    }


    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (!isRemoveAllExistingData() || importFileType.getFileListType().equals(TABLENAME_BM_SPM)) {
            return true;
        }

        iPartsDataProductList epcProducts = iPartsDataProductList.loadAllEPCProductList(getProject());

        getMessageLog().fireMessage(translateForLog("!!Lösche Baumuster-Aggregate-Strukturen für %1 EPC-Produkte",
                                                    String.valueOf(epcProducts.size())),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

        for (iPartsDataProduct product : epcProducts) {
            getProject().getDbLayer().delete(TABLE_DA_PRODUCT_MODELS, new String[]{ FIELD_DPM_PRODUCT_NO },
                                             new String[]{ product.getAsId().getProductNumber() });
        }

        getProject().getDbLayer().delete(TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_SOURCE },
                                         new String[]{ iPartsImportDataOrigin.EPC.getOrigin() });

        getProject().getDbLayer().delete(TABLE_DA_MODEL, new String[]{ FIELD_DM_SOURCE },
                                         new String[]{ iPartsImportDataOrigin.EPC.getOrigin() });

        return true;
    }

    private boolean isSourceEPCorAPPLIST(iPartsDataModelsAggs modelAgg, int recordNo) {
        iPartsImportDataOrigin modelAggSource = iPartsImportDataOrigin.getTypeFromCode(modelAgg.getFieldValue(iPartsConst.FIELD_DMA_SOURCE));
        if ((modelAggSource != iPartsImportDataOrigin.EPC) && (modelAggSource != iPartsImportDataOrigin.APP_LIST) && (modelAggSource != iPartsImportDataOrigin.UNKNOWN)) {
            // RT: Warning unterdrücken, da zuviele und keine Warnung bei ProductModels
//            getMessageLog().fireMessage(translateForLog("!!Record %1; Zu Model_Aggs (BM \"%2\", Agg-BM \"%3\") existiert schon ein Eintrag " +
//                                                        "mit der Quelle \"%4\". Der Eintrag wird nicht angelegt",
//                                                        String.valueOf(recordNo),
//                                                        modelAgg.getAsId().getModelNumber(), modelAgg.getAsId().getAggregateModelNumber(),
//                                                        modelAggSource.getOrigin()),
//                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return false;
        } else {
            // Quelle ist EPC oder APP_LIST oder UNKNOWN
            return true;
        }
    }

    private boolean isSourceEPCorAPPLIST(iPartsDataModelsAggs modelAgg) {
        iPartsImportDataOrigin modelAggSource = iPartsImportDataOrigin.getTypeFromCode(modelAgg.getFieldValue(iPartsConst.FIELD_DMA_SOURCE));
        if ((modelAggSource != iPartsImportDataOrigin.EPC) && (modelAggSource != iPartsImportDataOrigin.APP_LIST) && (modelAggSource != iPartsImportDataOrigin.UNKNOWN)) {
            // RT: Warning unterdrücken, da zuviele und keine Warnung bei ProductModels
//            getMessageLog().fireMessage(translateForLog("!!Zu Model_Aggs (BM \"%1\", Agg-BM \"%2\") existiert schon ein Eintrag " +
//                                                        "mit der Quelle \"%3\". Der Eintrag wird nicht angelegt",
//                                                        modelAgg.getAsId().getModelNumber(), modelAgg.getAsId().getAggregateModelNumber(),
//                                                        modelAggSource.getOrigin()),
//                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return false;
        } else {
            // Quelle ist EPC oder APP_LIST oder UNKNOWN
            return true;
        }
    }


    private boolean isSourceEPCorAPPLIST(iPartsDataModel model) {
        iPartsImportDataOrigin modelSource = iPartsImportDataOrigin.getTypeFromCode(model.getFieldValue(iPartsConst.FIELD_DM_SOURCE));
        if ((modelSource != iPartsImportDataOrigin.EPC) && (modelSource != iPartsImportDataOrigin.APP_LIST) && (modelSource != iPartsImportDataOrigin.UNKNOWN)) {
            // RT: Warning unterdrücken, da zuviele
//            getMessageLog().fireMessage(translateForLog("!!Zum BM \"%1\" existiert schon ein Eintrag " +
//                                                        "mit der Quelle \"%2\". Der Eintrag wird nicht angelegt",
//                                                        model.getAsId().getModelNumber(), modelSource.getOrigin()),
//                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return false;
        } else {
            // Quelle ist EPC oder APP_LIST oder UNKNOWN
            return true;
        }
    }


    private class ModelAggregateImportHelper extends EPCImportHelper {

        private ModelAggregateImportHelper(EtkProject project, Map<String, String> mapping) {
            super(project, mapping, TABLENAME_BM_AGGCHASS);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value == null) {
                return "";
            }
            if ((value.length() > 1) && StrUtils.stringContainsOnly(value, 'x')) {
                value = "";
            }
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }
            return value;
        }
    }

    // Klasse, die einen zusammenhängenden Import-Eintrag repräsentiert.
    private class CombinedSPMImportRecord {

        private int startRecordNo;
        private String productNumber;
        private String modelNumber;
        private EtkMultiSprache productModelText;
        private EtkMultiSprache modelSalesDesc;
        // Map die pro Aggregatetyp die Aggregatebaumusternummern mit diesem Typ hält
        private Map<String, Set<String>> aggTypesWithAggModelNo;

        private CombinedSPMImportRecord(int recordNo, String productNumber, String modelNumber) {
            this.startRecordNo = recordNo;
            this.productNumber = productNumber;
            this.modelNumber = modelNumber;
            aggTypesWithAggModelNo = new HashMap<>();
            productModelText = new EtkMultiSprache();
            modelSalesDesc = new EtkMultiSprache();
        }

        protected void addProductModelText(iPartsEPCLanguageDefs langDef, String addProductModelText) {
            addText(productModelText, langDef.getDbValue().getCode(), addProductModelText);
        }

        protected void addModelSalesDesc(iPartsEPCLanguageDefs langDef, String addModelSalesDesc) {
            addText(modelSalesDesc, langDef.getDbValue().getCode(), addModelSalesDesc);
        }

        protected void setAggregateModels(String aggregateModelsFromImport, String aggregateType) {
            Set<String> aggregateModels = aggTypesWithAggModelNo.get(aggregateType);
            if (aggregateModels == null) {
                aggregateModels = new HashSet<>();
                aggTypesWithAggModelNo.put(aggregateType, aggregateModels);
            }
            if (!aggregateModelsFromImport.isEmpty()) {
                aggregateModels.add(aggregateModelsFromImport);
            }
        }

        private void addText(EtkMultiSprache multiLang, String language, String addText) {
            addText = StrUtils.trimRight(addText);
            if (multiLang.spracheExists(language)) {
                String prevText = multiLang.getText(language);
                if (!prevText.equals(addText)) {
                    // Der Text aus diesem ImportRecord ist ein anderer als der aus dem vorherigen ImportRecord
                    // für die gleiche Sprache. Also wissen wir, dass der Text fortgeführt werden muss.
                    addText = prevText + addText;
                }
            }
            multiLang.setText(language, addText);
        }
    }

}
