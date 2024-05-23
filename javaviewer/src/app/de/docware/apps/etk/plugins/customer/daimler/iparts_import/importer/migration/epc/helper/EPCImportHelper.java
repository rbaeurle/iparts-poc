/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEPCLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.os.OsUtils;

import java.io.IOException;
import java.util.*;

/**
 * Hilfsklasse für das Umsetzen der ImportRecord Data auf die Attribute-Data via Mapping
 * Erweiterung für Migration von EPC
 */
public class EPCImportHelper extends iPartsMainImportHelper {

    protected static final String EPC_NULL_VALUE = "null";
    protected static final String EPC_SA_PREFIX = "Z";
    public static final char EPC_IMPORT_DATA_SEPARATOR = '¬';
    private DictImportTextIdHelper dictImportTextIdHelper;

    public EPCImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
        super(project, mapping, tableName);
        this.dictImportTextIdHelper = new DictImportTextIdHelper(getProject());
    }

    /**
     * Die Import-Werte komplett in einen Datensatz übertragen (EPC spezifisch)
     * Das Mapping besitzt dabei den folgenden Aufbau:
     * Key = DB-FieldName, Value = Import-Spaltenname
     *
     * @param dataObject
     * @param importRec
     * @param langDef    EPC spezifisch
     */
    public void fillOverrideCompleteDataForEPCReverse(EtkDataObject dataObject, Map<String, String> importRec, iPartsEPCLanguageDefs langDef) {
        Language language = (langDef != null) ? langDef.getDbValue() : null;
        fillOverrideCompleteDataReverse(dataObject, importRec, language);
    }

//    /**
//     * Check, ob es sich um einen validen Text handelt
//     *
//     * @param text
//     * @return
//     */
//    public boolean isTextValid(String text) {
//        if (StrUtils.isEmpty(text)) {
//            return false;
//        }
//        String tempText = StrUtils.removeCharsFromString(text, new char[]{ '.', ',', '*' });
//        if (StrUtils.isEmpty(tempText.trim())) {
//            return false;
//        }
//        return true;
//    }

    public String makeSANumberFromEPCValue(String epcSAValue) {
        if (StrUtils.isValid(epcSAValue) && !epcSAValue.startsWith(EPC_SA_PREFIX)) {
            String result = StrUtils.leftFill(epcSAValue, 6, ' ');
            result = EPC_SA_PREFIX + result;
            return result;
        }
        return epcSAValue;
    }

    /**
     * Nur Produkte mit dreistelliger Produktnummer zum Import zulassen,
     * falls sie nicht bereits aus MAD migriert wurden.
     *
     * @param importer
     * @param productNo
     * @param productRelevanceCacheFromImporter
     * @return
     */
    public boolean isProductRelevantForImport(AbstractDataImporter importer, String productNo,
                                              Map<iPartsProductId, Boolean> productRelevanceCacheFromImporter,
                                              int recordNo) {
        iPartsProductId productId = new iPartsProductId(productNo);

        // Wurde schon einmal gelesen, das bereits gefundene Ergebnis zurückgeben.
        if (productRelevanceCacheFromImporter.containsKey(productId)) {
            return productRelevanceCacheFromImporter.get(productId);
        }
        // Nur Produkte mit 3-stelliger Nr. importieren
        if (productNo.length() != 3) {

            // Meldung nur ausgeben, wenn sie sich von den Vorgängermeldungen unterscheidet.
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1 mit ungültiger Produktnummer \"%2\"" +
                                                                          " übersprungen.", String.valueOf(recordNo), productNo),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                 MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            productRelevanceCacheFromImporter.put(productId, false);
            return false;
        }
        // Dann doch lesen:
        iPartsDataProduct product = new iPartsDataProduct(getProject(), productId);
        if (!product.existsInDB()) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1 Produktnummer \"%2\" existiert nicht" +
                                                                          " und wird übersprungen!", String.valueOf(recordNo), productNo),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                 MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            productRelevanceCacheFromImporter.put(productId, false);
            return false;
        }

        // Alle Daten nur importieren, falls das entsprechende Produkt nicht aus MAD migriert wurde.
        // ==> Nur Quelle == "APPL_LIST" oder "EPC" oder Produkt nicht vorhanden übernehmen.
        iPartsImportDataOrigin productSource = iPartsImportDataOrigin.getTypeFromCode(product.getFieldValue(FIELD_DP_SOURCE));
        boolean resultValue;
        if ((productSource == iPartsImportDataOrigin.APP_LIST) || (productSource == iPartsImportDataOrigin.EPC)) {
            // Kam das Produkt via Applikationsliste rein, dann muss hier die Source auf "EPC" gesetzt werden. Quelländerung
            // wird auch nur einmal pro Produkt gemacht, da wir ja nur beim ersten Vorkommen des Produkts hier reinkommen.
            if (productSource != iPartsImportDataOrigin.EPC) {
                product.setFieldValue(FIELD_DP_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
            }
            setProductTitle(product, "");
            importer.saveToDB(product);
            resultValue = true;
        } else {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1 Produktnummer \"%2\" mit Quelle \"%3\"" +
                                                                          " wird nicht überschrieben!", String.valueOf(recordNo),
                                                                          productNo, productSource.getOrigin()),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                 MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            resultValue = false;
        }
        productRelevanceCacheFromImporter.put(productId, resultValue);
        return resultValue;
    }

    /**
     * SAs nur zulassen, falls sie nicht bereits aus MAD migriert wurden.
     * Selbe Logik, wie in {@link #isSARelevantForImport(AbstractDataImporter, String, int, boolean)}, nur mit Verwendung eines Caches
     *
     * @param importer
     * @param saNumber
     * @param invalidSas
     * @param recordNo
     * @return
     */
    public boolean isSARelevantForImport(AbstractDataImporter importer, String saNumber,
                                         Set<String> invalidSas, int recordNo) {
        // Wurde schon einmal gelesen, das bereits gefundene Ergebnis zurückgeben.
        if (invalidSas.contains(saNumber)) {
            return false;
        }

        if (!isSARelevantForImport(importer, saNumber, recordNo, true)) {
            invalidSas.add(saNumber);
            return false;
        }
        return true;
    }

    /**
     * SAs nur zulassen, falls sie nicht bereits aus MAD migriert wurden.
     *
     * @param importer
     * @param saNumber
     * @param recordNo
     * @return
     */
    public boolean isSARelevantForImport(AbstractDataImporter importer, String saNumber, int recordNo, boolean setSourceIfNotExist) {

        iPartsSaId saId = new iPartsSaId(saNumber);
        iPartsDataSa saData = new iPartsDataSa(getProject(), saId);

        if (saData.existsInDB()) {
            // Alle Daten nur importieren, falls die entsprechende SA nicht aus MAD migriert wurde.
            iPartsImportDataOrigin saSource = iPartsImportDataOrigin.getTypeFromCode(saData.getFieldValue(FIELD_DS_SOURCE));

            // Für SAs zu denen bereits Daten aus MAD migriert wurden, werden keine Daten importiert
            if ((saSource != iPartsImportDataOrigin.EPC) && (saSource != iPartsImportDataOrigin.APP_LIST)) {
                if (importer != null) {
                    importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1 SA-Nummer \"%2\" mit Quelle \"%3\"" +
                                                                                  " wird nicht überschrieben!", String.valueOf(recordNo),
                                                                                  saNumber, saSource.getOrigin()),
                                                         MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                         MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
                return false;
            } else if (saSource == iPartsImportDataOrigin.APP_LIST) {
                // SAs werden von der Applikationsliste eigentlich nicht angelegt. Sollte das in Zukunft aber passieren,
                // wären wir hier auf der sicheren Seite.
                saData.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
                importer.saveToDB(saData);
            }
        } else {
            if (setSourceIfNotExist) {
                saData.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                saData.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
                importer.saveToDB(saData);
            }
        }
        return true;
    }

    public List<String> getAsArray(String value, int length, boolean trim, boolean withBlanks) {
        if (StrUtils.isValid(value)) {
            return StrUtils.splitStringIntoSubstrings(value, length, trim, withBlanks);
        }
        return new ArrayList<>();
    }

    /**
     * Verarbeitet die während dem Import aufgesammelten Teile. Falls die Teile nicht vorkommen, werden sie neu angelegt
     *
     * @param importer
     * @param partNumber
     * @param termIdPartDesc
     * @param partsDone
     * @param shelfLife
     * @param isPseudoPosition
     */
    public void handlePartNumber(AbstractDataImporter importer, String partNumber, String termIdPartDesc, Set<PartId> partsDone, String shelfLife, boolean isPseudoPosition) {
        partNumber = StrUtils.replaceSubstring(partNumber, " ", "");

        PartId partId = new PartId(partNumber, "");

        if (!partsDone.contains(partId)) {
            partsDone.add(partId);

            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), partId);

            // Nur anlegen, falls das Teil noch nicht da ist.
            if (!part.existsInDB()) {
                part = EtkDataObjectFactory.createDataPart(getProject(), partId);
                part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                // Pseudo-Teile haben eine eigene ID bzw. Sachnummer. Diese darf nicht zerlegt werden
                if (!isPseudoPosition) {
                    // Verarbeite mögliche ES1 und/oder ES2 Schlüssel an der Teilenummer
                    DIALOGImportHelper.handleESKeysInDataPart(getProject(), part, importer.getMessageLog(), importer.getLogLanguage());
                }
                // Benennung aus Datensatzart9
                EtkMultiSprache description = searchEPCTextWithEPCId(DictTextKindEPCTypes.PART_DESCRIPTION, termIdPartDesc);

                if (description != null) {
                    part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, description, DBActionOrigin.FROM_EDIT);
                }
                // Datenquelle setzen
                part.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);

            }
            // Damit bei Pseudo-Teilen die selbst erstellte Sachnummer nicht angezeigt wird, darf M_BESTNR nicht belegt werden
            if (!isPseudoPosition) {
                part.setFieldValue(FIELD_M_BESTNR, part.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);
            }
            if (shelfLife != null) {
                part.setFieldValue(FIELD_M_SHELF_LIFE, shelfLife, DBActionOrigin.FROM_EDIT);
            }

            importer.saveToDB(part, false);
        }

    }

    public EtkMultiSprache searchEPCTextWithEPCId(DictTextKindEPCTypes dictTextKindEPCType, String epcTermId) {
        return dictImportTextIdHelper.searchEPCTextWithEPCId(dictTextKindEPCType, epcTermId);
    }

    /**
     * In den Referenzdaten der EPC Fußnoten werden Tabellenfußnoten anhand von Gruppierung dargestellt. Damit unsere
     * Fußnotenlogik greift, werden diese Gruppeninformationen an den jeweiligen Datensätzen gespeichert. Wenn eine
     * Tabellenfußnote beginnt, bekommt die Gruppeninformation ein "S" am Ende. Wenn sie endet, dann bekommt die
     * Gruppeninformation ein "Z" (angelehnt an die MAD Daten von DAIMLER).
     *
     * @param previousDataObject
     * @param currentDataObject
     * @param groupNum
     * @param groupNameFieldname
     */
    public void setGroupNumber(EtkDataObject previousDataObject, EtkDataObject currentDataObject, String groupNum, String groupNameFieldname, boolean sameStructure) {
        String previousGroupNumber = (previousDataObject != null) ? previousDataObject.getFieldValue(groupNameFieldname) : "";
        String splittedPreviousGroupNumber = "";
        boolean previousGroupNoIsValid = StrUtils.isValid(previousGroupNumber);
        boolean previousIsStart = false;
        if (previousGroupNoIsValid) {
            String[] currentGroupSplittedValue = StrUtils.toStringArray(previousGroupNumber, "|", false);
            if (currentGroupSplittedValue.length == 2) {
                previousIsStart = currentGroupSplittedValue[1].equals("S");
            }
            splittedPreviousGroupNumber = currentGroupSplittedValue[0];
        }
        boolean newGroupNoIsValid = StrUtils.isValid(groupNum);
        if (!previousGroupNoIsValid && !newGroupNoIsValid) {
            return;
        } else if (!previousGroupNoIsValid && newGroupNoIsValid) {
            currentDataObject.setFieldValue(groupNameFieldname, groupNum + "|" + "S", DBActionOrigin.FROM_EDIT);
        } else if (previousGroupNoIsValid && !newGroupNoIsValid) {
            if (previousDataObject != null) {
                if (previousIsStart) {
                    previousDataObject.setFieldValue(groupNameFieldname, splittedPreviousGroupNumber, DBActionOrigin.FROM_EDIT);
                } else {
                    previousDataObject.setFieldValue(groupNameFieldname, splittedPreviousGroupNumber + "|" + "Z", DBActionOrigin.FROM_EDIT);
                }
            }
        } else {
            if (sameStructure && splittedPreviousGroupNumber.equals(groupNum)) {
                currentDataObject.setFieldValue(groupNameFieldname, groupNum, DBActionOrigin.FROM_EDIT);
            } else {
                if (previousIsStart && (previousDataObject != null)) {
                    previousDataObject.setFieldValue(groupNameFieldname, splittedPreviousGroupNumber, DBActionOrigin.FROM_EDIT);
                } else if (previousGroupNoIsValid && (previousDataObject != null)) {
                    previousDataObject.setFieldValue(groupNameFieldname, splittedPreviousGroupNumber + "|" + "Z", DBActionOrigin.FROM_EDIT);
                }
                currentDataObject.setFieldValue(groupNameFieldname, groupNum + "|" + "S", DBActionOrigin.FROM_EDIT);
            }
        }
    }

    /**
     * Liefert den Code-String, der in EPC Importdaten über mehrere Spalten verteilt sein kann
     *
     * @param importRec
     * @param fieldNames
     * @return
     */
    public String getCodeValueFromMultipleCodeFields(Map<String, String> importRec, String... fieldNames) {
        StringBuilder builder = new StringBuilder();
        for (String fieldName : fieldNames) {
            String tempCodeValue = handleValueOfSpecialField(fieldName, importRec);
            if (StrUtils.isValid(tempCodeValue)) {
                builder.append(tempCodeValue);
            }
        }
        return calculateCodes(builder.toString());
    }

    /**
     * Zerlegt die Code (Feld CODEONE und CODETWO) in 3er-Gruppen und anschließend in einen Slash-separierten String
     * Besonderheit: "X12BISX15" wird zerlegt in "X12-X15"
     *
     * @param codes
     * @return
     */
    public String calculateCodes(String codes) {
        if (StrUtils.isEmpty(codes)) {
            return "";
        }
        List<String> codeList = StrUtils.splitStringIntoSubstrings(codes, 3);
        List<String> resultList = new DwList<>();
        String previousSuffix = "";
        boolean isRange = false;
        for (String currentCode : codeList) {
            if (isRange) {
                resultList.remove(resultList.size() - 1);
                resultList.add(previousSuffix + "-" + currentCode);
                previousSuffix = "";
                isRange = false;
                continue;
            }
            if (!previousSuffix.isEmpty()) {
                if (currentCode.toUpperCase().equals("BIS")) {
                    isRange = true;
                    continue;
                }
            }
            resultList.add(currentCode);
            previousSuffix = currentCode;

        }
        return StrUtils.stringListToString(resultList, "/");
    }

    /**
     * Befüllt das übergebene {@link iPartsDataSa} Objekt mit den übergebenen Codes und dem übergebenen EPC-Text
     *
     * @param saData
     * @param codes
     * @param epcText
     */
    public void fillSaMasterDataObject(iPartsDataSa saData, String codes, EtkMultiSprache epcText) {
        saData.setFieldValue(FIELD_DS_CODES, codes, DBActionOrigin.FROM_EDIT);
        saData.setFieldValueAsMultiLanguage(FIELD_DS_DESC, epcText, DBActionOrigin.FROM_EDIT);
        saData.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
        String aDat = saData.getFieldValue(FIELD_DS_ADAT);
        if (saData.isNew() || aDat.isEmpty() || !aDat.equals(saData.getFieldValue(FIELD_DS_EDAT))) {
            String currentTime = DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss);
            saData.setFieldValue(FIELD_DS_ADAT, currentTime, DBActionOrigin.FROM_EDIT);
            saData.setFieldValue(FIELD_DS_EDAT, currentTime, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Aus Baureihen-Prefix und der Liste den BM-Suffixes die Baumuster bilden
     * Sonderfall Regionen mit "000BIS099"
     *
     * @param importer
     * @param allModelNumbers
     * @param currentSeriesNumber
     * @param currentModelNumbersWithoutSeries
     */
    public void combineSeriesAndModelNumbersForSAA(AbstractDataImporter importer, Set<String> allModelNumbers,
                                                   String currentSeriesNumber, String currentModelNumbersWithoutSeries) {
        if ((currentModelNumbersWithoutSeries.length() % 3) != 0) {
            if (importer != null) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Fehler in den Importdaten. Baumusternummern konnten nicht interpretiert werden: %1. Sie werden nicht importiert",
                                                                              currentModelNumbersWithoutSeries),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
            return;
        }
        List<String> currentModelNumbersWithoutSeriesList = StrUtils.splitStringIntoSubstrings(currentModelNumbersWithoutSeries, 3);
        String previousSuffix = "";
        boolean isRange = false;
        for (String modelNumberWithoutSeries : currentModelNumbersWithoutSeriesList) {
            boolean addModel;
            if (isRange) {
                isRange = false;
                if (StrUtils.isInteger(previousSuffix) && StrUtils.isInteger(modelNumberWithoutSeries)) {
                    try {
                        int startIndex = Integer.valueOf(previousSuffix) + 1;
                        int endIndex = Integer.valueOf(modelNumberWithoutSeries);
                        if (startIndex <= endIndex) {
                            for (int lfdNr = startIndex; lfdNr <= endIndex; lfdNr++) {
                                allModelNumbers.add(currentSeriesNumber + StrUtils.leftFill(String.valueOf(lfdNr), 3, '0'));
                            }
                        }
                        previousSuffix = "";
                        continue;
                    } catch (NumberFormatException e) {
                        Logger.getLogger().handleRuntimeException(e);
                    }
                }
            }
            if (!previousSuffix.isEmpty() && modelNumberWithoutSeries.toUpperCase().equals("BIS")) {
                // Intervall gefunden -> nächsten Suffix checken
                isRange = true;
                continue;
            } else {
                addModel = true;
            }
            if (addModel) {
                allModelNumbers.add(currentSeriesNumber + modelNumberWithoutSeries);
                previousSuffix = modelNumberWithoutSeries;
            }
        }
    }

    /**
     * Bestimmt den Aggregatetyp zu jedem EPC Produkt über die verknüpften Baumuster.
     *
     * @param project
     * @param messageLogExtern
     * @param withLogFile
     */
    public static void calculateAggTypesForEPCProductsFromModels(EtkProject project, EtkMessageLog messageLogExtern, boolean withLogFile) {
        EtkMessageLog messageLog;
        if (messageLogExtern != null) {
            messageLog = messageLogExtern;
        } else {
            // Pseudo-MessgeLog
            messageLog = new EtkMessageLog();
        }
        DWFile logFile = null;
        if (withLogFile) {
            // Logdatei
            final DWFile tempLogFile = iPartsJobsManager.getInstance().jobRunning(TranslationHandler.translate("!!Aggregatetyp für EPC Produkte"));
            messageLog.addMessageEventListener(new MessageEvent() {
                @Override
                public void fireEvent(MessageEventData event) {
                    try {
                        String logEntry = event.getFormattedMessage(iPartsPlugin.LOG_FILES_LANGUAGE) + OsUtils.NEWLINE;
                        tempLogFile.appendTextFile(logEntry.getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
                    } catch (IOException e) {
                        Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    }
                }
            });
            logFile = tempLogFile;
        }

        messageLog.fireMessage(TranslationHandler.translate("!!Lade alle EPC " +
                                                            "Produkte mit leerem Aggregatetyp..."),
                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        // Bestimme alle EPC Produkte
        iPartsDataProductList allEPCProducts = iPartsDataProductList.loadAllEPCProductListWithEmptyAggTypes(project);
        messageLog.fireMessage(TranslationHandler.translate("!!%1 EPC Produkte" +
                                                            " geladen.",
                                                            String.valueOf(allEPCProducts.size())),
                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        project.getDbLayer().startTransaction();
        project.getDbLayer().startBatchStatement();
        try {
            // Variablen für die Statistik am Ende der Datei
            int productsWithValidAggType = 0;
            int productsWithValidAndEmptyAggTypes = 0;
            int productsWithoutModels = 0;
            int productsWithOnlyEmptyAggTypes = 0;
            int productsWihtDifferentAggTypes = 0;
            int productCounter = 0;
            messageLog.fireMessage(TranslationHandler.translate("!!Starte Bestimmung der Aggregatetypen"),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            for (iPartsDataProduct epcProduct : allEPCProducts) {
                // Allle Baumuster zum Produkt
                iPartsDataModelList allModelsForProduct = iPartsDataModelList.loadModelsForProductWithModelSource(project,
                                                                                                                  epcProduct.getAsId().getProductNumber(),
                                                                                                                  null);
                messageLog.fireMessage(TranslationHandler.translate("!!%1 Baumuster " +
                                                                    "zu EPC Produkt \"%2\" geladen.",
                                                                    String.valueOf(allModelsForProduct.size()),
                                                                    epcProduct.getAsId().getProductNumber()),
                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                if (!allModelsForProduct.isEmpty()) {
                    Set<String> aggTypes = new HashSet<>();
                    // Kenner, ob auch leere Aggregatewerte vorhanden sind
                    boolean hasEmptyAggTypes = false;
                    // Durchlaufe alle Baumuster und sammle alle möglichen Aggregatetypen
                    for (iPartsDataModel model : allModelsForProduct) {
                        String aggType = model.getFieldValue(FIELD_DM_MODEL_TYPE);
                        if (StrUtils.isValid(aggType)) {
                            aggTypes.add(aggType);
                        } else {
                            hasEmptyAggTypes = true;
                        }
                    }
                    if (aggTypes.isEmpty()) {
                        // 1.Fall: Alle Werte waren leer
                        messageLog.fireMessage(TranslationHandler.translate("!!Zum EPC Produkt \"%1\" existieren nur Baumuster mit leeren Baumusterarten",
                                                                            epcProduct.getAsId().getProductNumber()),
                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        productsWithOnlyEmptyAggTypes++;
                    } else if (aggTypes.size() == 1) {
                        // 2.Fall: Ein Aggregatetyp konnte bestimmt werden
                        String aggType = aggTypes.iterator().next();
                        if (hasEmptyAggTypes) {
                            // 2a. Mindestens ein Baumuster hatte einen leeren Aggregatetyp
                            messageLog.fireMessage(TranslationHandler.translate("!!Für das EPC Produkt \"%1\" wurde der " +
                                                                                "Aggregatetyp \"%2\" bestimmt obwohl einzelne " +
                                                                                "Baumuster einen leeren Aggregatetyp enthielten.",
                                                                                epcProduct.getAsId().getProductNumber(), aggType),
                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                            productsWithValidAndEmptyAggTypes++;
                        } else {
                            // 2b. Alle Baumuster hatten den gleichen Aggregatetyp
                            messageLog.fireMessage(TranslationHandler.translate("!!Für das EPC Produkt \"%1\" wurde der " +
                                                                                "Aggregatetyp \"%2\" bestimmt",
                                                                                epcProduct.getAsId().getProductNumber(), aggType),
                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                            productsWithValidAggType++;
                        }
                        epcProduct.setFieldValue(FIELD_DP_AGGREGATE_TYPE, aggType, DBActionOrigin.FROM_EDIT);
                        epcProduct.saveToDB();
                    } else {
                        // 3.Fall: Es wurden mehr als ein möglicher Aggregatewert gefunden
                        String allAggTypes = StrUtils.stringListToString(aggTypes, ", ");
                        messageLog.fireMessage(TranslationHandler.translate("!!Für das EPC Produkt \"%1\" konnte kein Aggregatetyp " +
                                                                            "bestimmt werden, da mehr als eine mögliche Baumusterart gefunden wurden: %2",
                                                                            epcProduct.getAsId().getProductNumber(), allAggTypes),
                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        productsWihtDifferentAggTypes++;

                    }
                } else {
                    // Zum Produkt in DA_PRODUCT_MODELS gibt es keine Baumuster
                    messageLog.fireMessage(TranslationHandler.translate("!!Zum EPC Produkt \"%1\" existieren keine Baumusterverknüpfungen in der Datenbank",
                                                                        epcProduct.getAsId().getProductNumber()),
                                           MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    productsWithoutModels++;
                }
                productCounter++;
                messageLog.fireProgress(productCounter, allEPCProducts.size(), "", true, true);
            }
            messageLog.hideProgress();
            messageLog.fireMessage(TranslationKeys.LINE_SEPARATOR);
            messageLog.fireMessage(TranslationHandler.translate("!!Insgesamt konnte für %1 EPC Produkte die Baumusterart ermittelt werden:",
                                                                String.valueOf(productsWithValidAggType + productsWithValidAndEmptyAggTypes)),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            messageLog.fireMessage(TranslationHandler.translate("!!Für %1 EPC Produkte konnte ein eindeutiger Aggregatetyp bestimmt werden.",
                                                                String.valueOf(productsWithValidAggType)),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            messageLog.fireMessage(TranslationHandler.translate("!!Für %1 EPC Produkte konnte ein Aggregatetyp bestimmt werden obwohl einzelne Baumuster leere Baumusterarten enthielten.",
                                                                String.valueOf(productsWithValidAndEmptyAggTypes)),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            messageLog.fireMessage(TranslationHandler.translate("!!Insgesamt konnte für %1 EPC Produkte die Baumusterart nicht ermittelt werden:",
                                                                String.valueOf(productsWithOnlyEmptyAggTypes + productsWihtDifferentAggTypes + productsWithoutModels)),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            messageLog.fireMessage(TranslationHandler.translate("!!Zu %1 EPC Produkten existieren nur Baumuster mit leeren Baumusterarten.",
                                                                String.valueOf(productsWithOnlyEmptyAggTypes)),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            messageLog.fireMessage(TranslationHandler.translate("!!Für %1 EPC Produkte konnte kein Aggregatetyp bestimmt werden, da mehrere unterschiedliche Baumusterarten gefunden wurden.",
                                                                String.valueOf(productsWihtDifferentAggTypes)),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            messageLog.fireMessage(TranslationHandler.translate("!!Zu %1 EPC Produkten existieren keine Baumusterverknüpfungen in der Datenbank.",
                                                                String.valueOf(productsWithoutModels)),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            messageLog.fireMessageWithSeparators(TranslationHandler.translate("!!Bestimmung der Aggregatetypen der EPC Produkte abgeschlossen"), MessageLogOption.TIME_STAMP);
            // Produkt-Cache löschen und Anzeige neuladen
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT));
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());

            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();

            if (withLogFile && (logFile != null)) {
                iPartsJobsManager.getInstance().jobProcessed(logFile);
            }
        } catch (Exception e) {
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            if (withLogFile && (logFile != null)) {
                iPartsJobsManager.getInstance().jobError(logFile);
            }
            throw e;
        }
    }

    /**
     * Liefert zurück, ob für das übergebene Feld der EPC spezifische "null" Wert eingelesen wurde
     *
     * @param importRec
     * @param fieldname
     * @return
     */
    public boolean isEPCNullValue(Map<String, String> importRec, String fieldname) {
        return StrUtils.isValid(importRec.get(fieldname)) && importRec.get(fieldname).equals(EPC_NULL_VALUE);
    }
}
