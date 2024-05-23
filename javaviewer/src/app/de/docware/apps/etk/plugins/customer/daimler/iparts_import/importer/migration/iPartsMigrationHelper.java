/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsScoringHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.SteeringIdentKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.TransmissionIdentKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class iPartsMigrationHelper implements iPartsConst {

    private final static String[] HIDDEN_SA_MODULE_PREFIXES = new String[]{ "SV", "EVO" };
    // Wert für die y Koordinate, wo nur ein "X" im zu verarbeiteten String vorkommt
    private static final int INDEX_FOR_EMPTY_STEERING_GEARBOX_VALUE = 0;
    private static final String STEERING_GEARBOX_ERROR = "STEERING_GEARBOX_ERROR";
    // Matrix für die Zuordnung der Lenkung und Getriebeartwerte
    private static SteeringAndGearboxValue[][] steeringAndGearboxMatrixForModel;
    private static SteeringAndGearboxValue[][] steeringAndGearboxMatrixForSA;

    // Felder, die beim Vergleich von Stücklisteneinträgen in entryIsSimilar() ignoriert werden sollen
    private static final Set<String> ignoreFieldsForEntryComparison = new HashSet<>();

    static {
        ignoreFieldsForEntryComparison.add(DBConst.FIELD_STAMP);
        ignoreFieldsForEntryComparison.add(FIELD_K_LFDNR);
        ignoreFieldsForEntryComparison.add(FIELD_K_SEQNR);
        ignoreFieldsForEntryComparison.add(FIELD_K_SA_VALIDITY);
    }

    private static Map<String, String> bm2AssortmentClassForSpecialCatalog = new HashMap<>(3);
    // Zuordnung Pseudo Baumuster zu Sortimentsklasse für Lacke und Betriebsstoffe
    private static List<String> allAssortmentClassesForSpecialCatalog = new DwList<>(3);

    static {
        bm2AssortmentClassForSpecialCatalog.put("D999998", ASSORTMENT_CLASS_CAR);
        bm2AssortmentClassForSpecialCatalog.put("D999999", ASSORTMENT_CLASS_TRUCK);
        bm2AssortmentClassForSpecialCatalog.put("D999001", ASSORTMENT_CLASS_SMART);
        allAssortmentClassesForSpecialCatalog.add(ASSORTMENT_CLASS_CAR);
        allAssortmentClassesForSpecialCatalog.add(ASSORTMENT_CLASS_TRUCK);
        allAssortmentClassesForSpecialCatalog.add(ASSORTMENT_CLASS_SMART);

        /**
         * Aufbauen der Lenkungs und Getriebeart Matrix nach den Vorgaben im Confluence (DAIMLER-1853)
         * TAL46A hat nur Lenkungsangaben
         */
        String steeringL = SteeringIdentKeys.STEERING_LEFT;
        String steeringR = SteeringIdentKeys.STEERING_RIGHT;
        String gearboxGM = TransmissionIdentKeys.TRANSMISSION_MECHANICAL;
        String gearboxGA = TransmissionIdentKeys.TRANSMISSION_AUTOMATED;
        String spaceValue = "";

        // Falls der String leer ist, findet keine Verarbeitung des Strings statt. x Wert kann nie SPACE sein, da mind. ein "X" im String vorkommt.
        // Matrix:
        //            130a 130b 130c 130d
        //      SPACE
        //      130a
        //      130b
        //      130c
        //      130d
        steeringAndGearboxMatrixForModel = new SteeringAndGearboxValue[4][5];

        steeringAndGearboxMatrixForModel[0][INDEX_FOR_EMPTY_STEERING_GEARBOX_VALUE] = new SteeringAndGearboxValue(steeringL, gearboxGM); // "X" in Felder: 130a          Lenkung: L      Gebtriebart: GM
        steeringAndGearboxMatrixForModel[1][INDEX_FOR_EMPTY_STEERING_GEARBOX_VALUE] = new SteeringAndGearboxValue(steeringL, gearboxGA); // "X" in Felder: 130b          Lenkung: L      Gebtriebart: GA
        steeringAndGearboxMatrixForModel[2][INDEX_FOR_EMPTY_STEERING_GEARBOX_VALUE] = new SteeringAndGearboxValue(steeringR, gearboxGM); // "X" in Felder: 130c          Lenkung: R      Gebtriebart: GM
        steeringAndGearboxMatrixForModel[3][INDEX_FOR_EMPTY_STEERING_GEARBOX_VALUE] = new SteeringAndGearboxValue(steeringR, gearboxGA); // "X" in Felder: 130d          Lenkung: R      Gebtriebart: GA
        steeringAndGearboxMatrixForModel[0][1] = new SteeringAndGearboxValue(steeringL, spaceValue);                                     // "X" in Felder: 130a, 130b    Lenkung: L      Gebtriebart: leer
        steeringAndGearboxMatrixForModel[0][2] = new SteeringAndGearboxValue(spaceValue, gearboxGM);                                     // "X" in Felder: 130a, 130c    Lenkung: leer   Gebtriebart: GM
        steeringAndGearboxMatrixForModel[0][3] = new SteeringAndGearboxValue(STEERING_GEARBOX_ERROR, STEERING_GEARBOX_ERROR);            // "X" in Felder: 130a, 130d    expliziter Fehler
        steeringAndGearboxMatrixForModel[1][2] = new SteeringAndGearboxValue(STEERING_GEARBOX_ERROR, STEERING_GEARBOX_ERROR);            // "X" in Felder: 130b, 130c    expliziter Fehler
        steeringAndGearboxMatrixForModel[1][3] = new SteeringAndGearboxValue(spaceValue, gearboxGA);                                     // "X" in Felder: 130b, 130d    Lenkung: leer   Gebtriebart: GA
        steeringAndGearboxMatrixForModel[2][3] = new SteeringAndGearboxValue(steeringR, spaceValue);                                     // "X" in Felder: 130c, 130d    Lenkung: R      Gebtriebart: leer

        steeringAndGearboxMatrixForSA = new SteeringAndGearboxValue[4][5];
        steeringAndGearboxMatrixForSA[0][INDEX_FOR_EMPTY_STEERING_GEARBOX_VALUE] = new SteeringAndGearboxValue(steeringL, spaceValue);// "X" in Felder: 130a          Lenkung: L      Gebtriebart: leer
        steeringAndGearboxMatrixForSA[2][INDEX_FOR_EMPTY_STEERING_GEARBOX_VALUE] = new SteeringAndGearboxValue(steeringR, spaceValue);// "X" in Felder: 130c          Lenkung: R      Gebtriebart: leer
        steeringAndGearboxMatrixForSA[0][2] = new SteeringAndGearboxValue(spaceValue, spaceValue);                                    // "X" in Felder: 130a, 130c    Lenkung: leer   Gebtriebart: leer

    }


    public static boolean isOnlyOmmitedPartData(AbstractDataImporter importer, String partNumber, String omittedPart, int currentRecordNo) {
        if (StrUtils.isEmpty(partNumber) && !StrUtils.isEmpty(omittedPart)) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Überspringe Entfallteil \"%1\" in Record %2",
                                                                          omittedPart, String.valueOf(currentRecordNo)),
                                                 MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return true;
        }
        return false;
    }

    public static boolean isYPartListEntry(String partNumber) {
        return StrUtils.countCharacters(partNumber.toUpperCase().trim(), 'Y') == partNumber.trim().length();
    }

    public static boolean isVTextPartListEntry(String partNumber) {
        return StrUtils.countCharacters(partNumber.toUpperCase().trim(), 'V') == partNumber.trim().length();
    }

    public static boolean isDifferentSteeringAndGearValue(String steeringAndGearboxValue, String steeringAndGearboxValueLastRec) {
        return !steeringAndGearboxValue.equals(steeringAndGearboxValueLastRec);
    }

    public static boolean hasRealQuantityValues(List<String> quantityValues) {
        for (String quantity : quantityValues) {
            quantity = quantity.trim();
            if (!quantity.isEmpty() && !quantity.equals("000") && !quantity.equals("-")) {
                return true;
            }
        }
        return false;
    }

    public static void copyValuesFromPreviousDataset(Map<String, String> importRec,
                                                     Map<String, String> lastCompletePartListEntryImportRec,
                                                     boolean isSteeringAndGearboxDifferent, String keyFieldToCompare) {
        for (Map.Entry<String, String> lastRecEntry : lastCompletePartListEntryImportRec.entrySet()) {
            String key = lastRecEntry.getKey();
            if (importRec.get(key).isEmpty() && (!isSteeringAndGearboxDifferent || !key.equals(keyFieldToCompare))) {
                importRec.put(key, lastRecEntry.getValue());
            }
        }
    }

    public static boolean handleUnsupportedFields(AbstractDataImporter importer, boolean isTextPos,
                                                  int currentRecordNo, String indentValue, String replaceFlag,
                                                  String replacePart, String includeParts,
                                                  String indentValueFromPreviousEntry, String lastImportRecPartNumber) {
        if (!isTextPos) { // Diese Felder sind für Textpositionen irrelevant
            // Mögliche bisher nicht unterstützte Felder, die bei der vorangehenden Teileposition ergänzt werden können
            Map<String, String> unsupportedFieldValues = new LinkedHashMap<>();
            unsupportedFieldValues.put("EINRUECKZAHL", indentValue);
            if (replaceFlag.equals("R")) { // Ersetzungsflag gesetzt?
                unsupportedFieldValues.put("REP_TNR_1", replacePart);
                unsupportedFieldValues.put("REP_TNR_N", includeParts);
            }

            // Überprüfen, ob diese bisher nicht unterstützten Felder gültige Werte haben und diese dann in die Warnung übernehmen
            StringBuilder unsupportedFieldsWarning = new StringBuilder();
            for (Map.Entry<String, String> unsupportedFieldValueEntry : unsupportedFieldValues.entrySet()) {
                String fieldName = unsupportedFieldValueEntry.getKey();
                String value = unsupportedFieldValueEntry.getValue().trim();
                boolean validValue;
                if (fieldName.equals("EINRUECKZAHL")) { // Sonderbehandlung für "0" und gleichen Wert in der echten Teileposition
                    validValue = !value.isEmpty() && !value.equals("0") && !value.equals(indentValueFromPreviousEntry);
                } else {
                    validValue = !value.isEmpty();
                }
                if (validValue) {
                    if (unsupportedFieldsWarning.length() > 0) {
                        unsupportedFieldsWarning.append("; ");
                    }
                    unsupportedFieldsWarning.append(fieldName);
                    unsupportedFieldsWarning.append(": ");
                    unsupportedFieldsWarning.append(value);
                }
            }

            // bisher nicht unterstützte Felder haben gültige Werte -> Warnung ausgeben
            if (unsupportedFieldsWarning.length() > 0) {
                lastImportRecPartNumber = StrUtils.replaceSubstring(lastImportRecPartNumber, " ", "");
                importer.getMessageLog().fireMessage((importer.translateForLog("!!Teileposition aus Record %1 ist eine Y-Teileposition, die die vorangehende echte Teileposition \"%2\" um folgende noch nicht unterstützte Felder erweitert: %3",
                                                                               String.valueOf(currentRecordNo), lastImportRecPartNumber,
                                                                               unsupportedFieldsWarning.toString())),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                     MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return false;
            }
        }
        return true;
    }

    public static boolean handleSaaValidity(List<EtkDataPartListEntry> lastCompletePartListEntry, Set<String> saaValidityExtension) {
        if (!saaValidityExtension.isEmpty()) {
            for (EtkDataPartListEntry partListEntry : lastCompletePartListEntry) {
                EtkDataArray saaValues = partListEntry.getFieldValueAsArrayOriginal(FIELD_K_SA_VALIDITY);
                Set<String> saaValidity = new LinkedHashSet<String>(saaValues.getArrayAsStringList());
                saaValidity.addAll(saaValidityExtension);

                // neues EtkDataArray verwenden, da ansonsten in setFieldValueAsArray() die Änderung des Arrays nicht erkannt wird
                EtkDataArray saaValuesWithExtension = new EtkDataArray();
                saaValuesWithExtension.add(saaValidity);
                partListEntry.setFieldValueAsArray(FIELD_K_SA_VALIDITY, saaValuesWithExtension, DBActionOrigin.FROM_EDIT);
            }
            return true;
        }
        return false;
    }

//    private Set<String> getSaaBkValidityFromImportRec(AbstractDataImporter importer, String alias, Map<String, String> importRec) {
//        Set<String> saaBkValidity = new LinkedHashSet<String>();
//        if (talType == MadTal4XABaseImporter.Tal4XAType.TAL40A) {
//            // In den BM-Katalogen gibt es zwei Möglichkeiten, SAAs oder Baumkästen
//            if (importRec.get(alias + "." + TAL4XA_ART).equals("SA")) {
//                // Es stehen SAs drin
//                List<String> saValues = getAsArray(importRec.get(alias + "." + TAL40A_SA_SNR), true);
//                for (String sa : saValues) {
//                    // Links darf nicht getrimmt werden, da SAs auch mit leer !!! anfangen können.
//                    sa = StrUtils.trimRight(sa);
//                    if (!sa.trim().isEmpty()) {
//                        // Rumpf ermitteln: die ersten 6 Stellen sind die Rumpf-SAA oder SA (ohne Sachnummernkennzeichen "Z")
//                        // Danach kommen 9 mal 2 Stellen, das sind die gültigen Strich-Ausführungen zu dieser Rumpf-SAA
//                        String saRumpf = null;
//                        try {
//                            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
//                            saRumpf = numberHelper.unformatSaForDB(sa.substring(0, 6));
//                        } catch (RuntimeException e) {
//                            importer.getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
//                            continue;
//                        }
//
//                        int offset = 6;
//                        while (sa.length() > offset + 1) { // 2 Stellen (offset und eine dazu) müssen in sa noch verfügbar sein
//                            String saStrich = sa.substring(offset, offset + 2);
//                            if (!saStrich.trim().isEmpty()) {
//                                String newSa = saRumpf + saStrich;
//                                saaBkValidity.add(newSa);
//                            }
//                            offset += 2;
//                        }
//                    }
//                }
//            } else if (importRec.get(alias + "." + TAL4XA_ART).equals("BK")) {
//                // Es stehen Baukästen drin
//                List<String> bkValues = getAsArray(importRec.get(alias + "." + TAL40A_BK_SNR), false);
//                for (String component : bkValues) {
//                    saaBkValidity.add(component);
//                }
//            }
//        }
//        return saaBkValidity;
//    }

    public static EtkMultiSprache handleNeutralText(AbstractDataImporter importer, DictImportTextIdHelper importTextIDHelper,
                                                    String textFromDataset, int currentRecordNo) {
        EtkMultiSprache neutralText = null;
        String neutralTextString = StrUtils.trimRight(textFromDataset);
        if (!neutralTextString.isEmpty()) {
            neutralText = new EtkMultiSprache();
            neutralText.setText(Language.DE, neutralTextString);
            boolean dictSuccessful = importTextIDHelper.handleNeutralTextWithCache(neutralText, TableAndFieldName.make(TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT));
            if (!checkImportTextID(importer, importTextIDHelper, dictSuccessful, currentRecordNo)) {
                neutralText = null;
            }
        }
        return neutralText;
    }

    public static boolean checkImportTextID(AbstractDataImporter importer, DictImportTextIdHelper importTextIDHelper,
                                            boolean dictSuccessful, int currentRecordNo) {
        if (importTextIDHelper.hasInfos()) {
            for (String info : importTextIDHelper.getInfos()) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1: \"%2\"", String.valueOf(currentRecordNo), info),
                                                     MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }

        if (importTextIDHelper.hasWarnings()) {
            // Wenn Warnungen aufkommen, dann setze keinen Text
            for (String warning : importTextIDHelper.getWarnings()) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1: \"%2\"", String.valueOf(currentRecordNo), warning),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
            return false;
        }

        if (!dictSuccessful) {
            importer.cancelImport();
            return false;
        }
        return true;
    }

    private static iPartsFootNoteId parseTuvFootNoteValue(String tuvValue) {
        if (tuvValue.isEmpty()) {
            return null;
        }
        String mappedFootnoteNumber = getFootnoteNumberForTUVValue(tuvValue);
        if (StrUtils.isValid(mappedFootnoteNumber)) {
            return new iPartsFootNoteId(mappedFootnoteNumber);
        }
        return null;
    }

    /**
     * Liefert die gemappte Fußnotennummer zum übergebenen TUV Wert
     *
     * @param tuvValue
     * @return
     */
    public static String getFootnoteNumberForTUVValue(String tuvValue) {
        if (StrUtils.isValid(tuvValue)) {
            switch (tuvValue) {
                case "T":
                    return "402";
                case "U":
                    return "412";
                case "V":
                    return "417";
            }
        }
        return "";
    }

    public static void handleTUVFootnote(String tuv, Set<iPartsFootNoteId> footNoteIds) {
        iPartsFootNoteId tuvFootNoteId = parseTuvFootNoteValue(tuv);
        if (tuvFootNoteId != null) {
            footNoteIds.add(tuvFootNoteId);
        }
    }

    public static iPartsFootNoteId getFootNoteIdForProduct(String prefix, String delimiter, String productNumber, String kg, String footNoteNumber) {
        return new iPartsFootNoteId(prefix + delimiter + productNumber + delimiter + kg + delimiter + footNoteNumber);
    }

    public static iPartsFootNoteId getFootNoteIdForSA(String prefix, String delimiter, String footNoteNumber, String saNumber) {
        return new iPartsFootNoteId(prefix + delimiter + saNumber + delimiter + footNoteNumber);
    }

    public static void fillWWSets(List<String> wwPartNumbersFormatted, Map<String, Set<String>> currentWWPartNumbersToWWPartNumbersInAssembly, String wwPartNumber) {
        List<String> wwPartNumbers = new ArrayList<>(wwPartNumbersFormatted.size());
        for (String wwPartNumberFormatted : wwPartNumbersFormatted) {
            wwPartNumbers.add(StrUtils.replaceSubstring(wwPartNumberFormatted, " ", ""));
        }

        // Map für Wahlweise-Behandlung befüllen
        Set<String> wwPartNumbersSet = currentWWPartNumbersToWWPartNumbersInAssembly.get(wwPartNumber);
        if (wwPartNumbersSet == null) {
            wwPartNumbersSet = new TreeSet<>();
            currentWWPartNumbersToWWPartNumbersInAssembly.put(wwPartNumber, wwPartNumbersSet);
        }
        wwPartNumbersSet.addAll(wwPartNumbers);
    }

    public static String getShelfLife(boolean currentProductIsSpecialCatalog, List<String> quantityValues) {
        if (currentProductIsSpecialCatalog) {
            String shelfLife = "";
            for (int i = 0; i < quantityValues.size(); i++) {
                String value = quantityValues.get(i).trim();
                if (!StrUtils.isEmpty(value)) {
                    if (shelfLife.isEmpty()) {
                        // Falls die Haltbarkeit noch nicht bestimmt werden konnte, versuche es hier
                        // 99 = ewig haltbar, das machen wir zu leer
                        // Theoretisch könnte man die Haltbarkeit für jede Sortimentsklasse einzeln angeben, das ist technischer Unfug, deshalb nehmen wir hier die erste die ein Integer ist
                        if (StrUtils.isInteger(value)) {
                            int intValue = Integer.parseInt(value);
                            if (intValue != 99) {
                                shelfLife = Integer.toString(intValue);
                            }
                        }
                    }

                    // Wegen Missbrauchs des Mengenfeldes für die Haltbarkeit hier Menge auf 1
                    quantityValues.set(i, "1");
                }
            }
            return shelfLife;
        }
        return null;
    }

    public static boolean handleWWSets(String partNumber, String wwFlag, List<String> wwPartNumbersFormatted,
                                       Map<String, Set<String>> currentWWPartNumbersToWWPartNumbersInAssembly) {
        if (wwFlag.equals("W")) {
            iPartsMigrationHelper.fillWWSets(wwPartNumbersFormatted, currentWWPartNumbersToWWPartNumbersInAssembly, partNumber);
            return true;
        }
        return false;
    }

    public static QuantityForModelOrSAA handleQuantityForModel(AbstractDataImporter importer,
                                                               List<String> modelsOrTypesForQuantity, List<String> quantityValues,
                                                               Map<String, Set<iPartsModelId>> currentModelsForTypeInGlobalModelProduct,
                                                               String productNumber, boolean currentProductIsGlobalModel) {
        QuantityForModelOrSAA result = new QuantityForModelOrSAA();
        for (int i = 0; i < modelsOrTypesForQuantity.size(); i++) {
            // Für jedes Model muss die Menge ermitelt werden
            // Das Array quantityValues ist nicht unbedingt so lange, wie die Anzahl der Baumuster
            // weil leer Werte hinten evtl. abgeschnitten sein können
            String quantity = "";
            if (i < quantityValues.size()) {
                quantity = quantityValues.get(i);
            }
            quantity = formatQuantityValue(quantity);
            String modelOrTypeNumber = modelsOrTypesForQuantity.get(i);
            // es können aber im Array auch leere Werte sein, diese überspringen
            if (!modelOrTypeNumber.isEmpty()) {
                if (currentProductIsGlobalModel) { // bei Globalbaumuster: modelOrTypeNumber ist eine Typkennzahl -> alle gültigen Baumuster dafür hinzufügen
                    // Gültige Baumuster pro Typkennzahl einmalig in einer Map speichern
                    Set<iPartsModelId> models = currentModelsForTypeInGlobalModelProduct.get(modelOrTypeNumber);
                    if (models != null) {
                        for (iPartsModelId model : models) {
                            result.add(quantity, model.getModelNumber());
                        }
                    } else {
                        importer.getMessageLog().fireMessage(importer.translateForLog("!!Keine gültigen Baumuster im Produkt \"%1\" für Typkennzahl \"%2\" gefunden",
                                                                                      productNumber, modelOrTypeNumber),
                                                             MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }
                } else { // kein Globalbaumuster: modelOrTypeNumber ist ein Baumuster
                    result.add(quantity, modelOrTypeNumber);
                }
            }
        }

        // Falls kein Eintrag in quantityForModelOrSAA vorhanden ist, einen Dummy-Eintrag für eine Menge erzeugen, die
        // nicht vom Baumuster abhängig ist (z.B. ein Globalbaumuster mit nur einer Typkennzahl) -> sonst wird der
        // Stücklisteneintrag überhaupt nicht importiert
        if (result.getQuantities().isEmpty()) {
            String quantity = "";
            if (!quantityValues.isEmpty()) {
                quantity = quantityValues.get(0);
            }
            quantity = formatQuantityValue(quantity);
            result.add(quantity, "");
        }
        return result;
    }

    public static String formatQuantityValue(String quantityValue) {
        quantityValue = quantityValue.trim();
        if (!quantityValue.isEmpty()) {
            quantityValue = StrUtils.removeLeadingCharsFromString(quantityValue, '0');
            if (quantityValue.isEmpty()) { // ursprünglicher Wert hat nur aus Nullen bestanden -> 0
                quantityValue = "0";
            }
        }
        return quantityValue;

    }

    public static Map<String, Set<iPartsModelId>> loadAllModelsForTypeInGlobaldModelProduct(EtkProject project, String productNumber) {
        DBDataObjectList<iPartsDataProductModels> productModelsList = iPartsDataProductModelsList.loadDataProductModelsList(project, new iPartsProductId(productNumber));
        return loadAllModelsForTypeInGlobaldModelProduct(productModelsList);
    }

    public static Map<String, Set<iPartsModelId>> loadAllModelsForTypeInGlobaldModelProduct(DBDataObjectList<iPartsDataProductModels> productModelsList) {
        Map<String, Set<iPartsModelId>> currentModelsForTypeInGlobalModelProduct = new HashMap<>();
        for (iPartsDataProductModels dataProductModel : productModelsList) {
            String modelNumber = dataProductModel.getAsId().getModelNumber();
            String typeNumber = modelNumber.substring(0, 4); // Sachnummernkennbuchstabe und erste 3 Stellen ergeben die Typkennzahl
            Set modelsForType = currentModelsForTypeInGlobalModelProduct.get(typeNumber);
            if (modelsForType == null) {
                // Baumuster sortiert in einem Set ablegen
                modelsForType = new TreeSet<iPartsModelId>(new Comparator<iPartsModelId>() {
                    @Override
                    public int compare(iPartsModelId o1, iPartsModelId o2) {
                        return o1.getModelNumber().compareTo(o2.getModelNumber());
                    }
                });
                currentModelsForTypeInGlobalModelProduct.put(typeNumber, modelsForType);
            }
            modelsForType.add(new iPartsModelId(modelNumber));
        }
        return currentModelsForTypeInGlobalModelProduct;
    }

    public static String getSourceGUID(String kVari, String kLfdnr, String delimiter) {
        return kVari + delimiter + kLfdnr;
    }

    public static void setModelValidity(EtkDataPartListEntry destPartListEntry, QuantityForModelOrSAA quantityForModelOrSAA,
                                        String currentQuantity, boolean currentProductIsSpecialCatalog, int modelsAmountForProduct) {
        // Wenn die aktuelle Menge genauso viele Baumuster hat, wie am Produkt hängen, dann ist die Stücklistenposition
        // immer gültig. Die Baumuster müssen nicht eingetragen werden.
        List<String> modelsForCurrentQuantity = quantityForModelOrSAA.getNumbers(currentQuantity);
        List<String> modelsForArray = new ArrayList<>();
        // Nur echte Baumuster durchgeben
        for (String model : modelsForCurrentQuantity) {
            if (StrUtils.isValid(model)) {
                modelsForArray.add(model);
            }
        }

        if (!modelsForArray.isEmpty() && (modelsForArray.size() != modelsAmountForProduct)) {
            EtkDataArray modelValues = new EtkDataArray();
            modelValues.add(modelsForArray);
            if (currentProductIsSpecialCatalog) {
                // Bei den Spezialkatalogen ist die Gültigkeit der Baumuster die AS-Produktklasse
                // Die Baumustergültigkeit wurde hier zweckentfremdet

                // Ummappen der Dummybaumuster zu den Produktklassen
                List<String> productClasses = mapModelValuesToPClasses(modelValues);
                destPartListEntry.setFieldValueAsSetOfEnum(FIELD_K_PCLASSES_VALIDITY, productClasses, DBActionOrigin.FROM_EDIT);

            } else {
                destPartListEntry.setFieldValueAsArray(FIELD_K_MODEL_VALIDITY, modelValues, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    /**
     * Die Dummybaumuster D999998, D999999 und D999100 stehen für die Sortimentsklasse
     * https://confluence.docware.de/confluence/x/JQJGAQ
     *
     * @param modelValues
     * @return
     */
    private static List<String> mapModelValuesToPClasses(EtkDataArray modelValues) {
        List<String> result = new ArrayList<>();

        for (String value : modelValues.getArrayAsStringList()) {
            String assortmentClass = bm2AssortmentClassForSpecialCatalog.get(value);
            if (!StrUtils.isEmpty(assortmentClass)) {
                result.add(assortmentClass);
            }
        }
        return result;
    }

    public static void setPClassValidity(EtkDataPartListEntry destPartListEntry) {
        if (destPartListEntry.getFieldValue(FIELD_K_PCLASSES_VALIDITY).isEmpty()) {
            destPartListEntry.setFieldValueAsSetOfEnum(FIELD_K_PCLASSES_VALIDITY, allAssortmentClassesForSpecialCatalog, DBActionOrigin.FROM_EDIT);
        }
    }

    public static void setBestFlag(EtkDataPartListEntry destPartListEntry, boolean setFalseBestFlag, String currentQuantity) {
        if (setFalseBestFlag || currentQuantity.equals("-")) { // Textpositionen sind prinzipiell nicht bestellbar
            destPartListEntry.setFieldValueAsBoolean(FIELD_K_BESTFLAG, false, DBActionOrigin.FROM_EDIT);
        } else {
            destPartListEntry.setFieldValueAsBoolean(FIELD_K_BESTFLAG, true, DBActionOrigin.FROM_EDIT);
        }
    }

    public static void handleFieldRelatedToTextValue(EtkDataPartListEntry destPartListEntry, Set<String> saaBkValidity,
                                                     String partNumber, String posNr, boolean isTextPos, boolean isVTextPos) {
        handleFieldRelatedToTextValue(destPartListEntry, saaBkValidity, partNumber, posNr, isTextPos, isVTextPos, false);

    }

    public static void handleFieldRelatedToTextValue(EtkDataPartListEntry destPartListEntry, Set<String> saaBkValidity,
                                                     String partNumber, String posNr, boolean isTextPos, boolean isVTextPos,
                                                     boolean isXPartPosition) {
        if (!isTextPos) { // Diese Felder müssen bei Textpositionen nicht gesetzt werden
            // SAA-Gültigkeit bei den Stücklisteneinträgen speichern
            if (!saaBkValidity.isEmpty()) {
                EtkDataArray saaValues = new EtkDataArray();
                saaValues.add(saaBkValidity);
                destPartListEntry.setFieldValueAsArray(FIELD_K_SA_VALIDITY, saaValues, DBActionOrigin.FROM_EDIT);
            }
            if (!isXPartPosition) {
                // Teilenummer setzen
                destPartListEntry.setFieldValue(FIELD_K_MATNR, partNumber, DBActionOrigin.FROM_EDIT);
            }

            // Positionsnummern, die nur "-" Zeichen enthalten sollen keine Hotspotnummer erhalten
            if (StrUtils.stringContainsOnly(posNr, '-')) {
                posNr = "";
            }
            destPartListEntry.setFieldValue(FIELD_K_POS, posNr, DBActionOrigin.FROM_EDIT);

            // Bei Werkseinsatzdaten aus TAL4X die Auswertung von Ident ab und Ident bis immer aktivieren
            destPartListEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM, true, DBActionOrigin.FROM_EDIT);
            destPartListEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO, true, DBActionOrigin.FROM_EDIT);
        } else { // Virtuelles Material für Textpositionen auf "Zwischenüberschriften" (V-Textposition) bzw. "Überschriften" (Y-Textposition) setzen
            if (isVTextPos) {
                destPartListEntry.setFieldValue(FIELD_K_VIRTUAL_MAT_TYPE, VirtualMaterialType.TEXT_SUB_HEADING.getDbValue(), DBActionOrigin.FROM_EDIT);
            } else {
                destPartListEntry.setFieldValue(FIELD_K_VIRTUAL_MAT_TYPE, VirtualMaterialType.TEXT_HEADING.getDbValue(), DBActionOrigin.FROM_EDIT);
            }
        }
    }

    /**
     * Zuordnung Lenkung und Getriebeart zu Teileposition
     *
     * @param steeringAndGearboxValue
     * @param destPartListEntry
     * @param partNumber
     */
    public static void assignSteeringAndGearboxType(AbstractDataImporter importer, String steeringAndGearboxValue,
                                                    EtkDataPartListEntry destPartListEntry, String partNumber, boolean forModel) {
        if ((steeringAndGearboxValue != null) && !steeringAndGearboxValue.trim().isEmpty()) {
            // x und y Wert für die Matrix bestimmen. Erstes "X" = x, zweites "X" = y Bsp: " X X" -> x=1, y=3
            int x = 0;
            int y = INDEX_FOR_EMPTY_STEERING_GEARBOX_VALUE;
            int[] coordinates = StrUtils.getIndexOfCharInString(steeringAndGearboxValue.toUpperCase(), 'X');
            int coordinatesLength = coordinates.length;
            if (coordinatesLength > 2) {
                // Im Normalfall kommen nur 1, 2 oder 4 'X' vor. Falls 3 aufkommen -> Hinweis im LOG
                if (coordinatesLength == 3) {
                    importer.getMessageLog().fireMessage(importer.translateForLog("!!Unerlaubte Anzahl von X-Marker für" +
                                                                                  " die Zuordnung Lenkung und Getriebart zur Teileposition " +
                                                                                  "\"%1\". Teilenummer: \"%2\", Marker: \"%3\"",
                                                                                  destPartListEntry.getAsId().toString(), partNumber,
                                                                                  steeringAndGearboxValue),
                                                         MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                         MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
                return;
            }

            switch (coordinatesLength) {
                case 1:
                    x = coordinates[0];
                    break;
                case 2:
                    x = coordinates[0];
                    y = coordinates[1];
                    break;
            }

            SteeringAndGearboxValue matrixValues;
            try {
                // Hole den Lenkungs- und Getriebeartwert
                matrixValues = forModel ? steeringAndGearboxMatrixForModel[x][y] : steeringAndGearboxMatrixForSA[x][y];
            } catch (ArrayIndexOutOfBoundsException oobE) {
                // Falls ungültiger Index -> Fehler ausgeben
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Index-Verletzung bei der" +
                                                                              " Zuordnungsmatrix für Lenkung und Getriebeart " +
                                                                              "zur Teileposition \"%1\". Teilenummer: \"%2\", Marker: \"%3\"",
                                                                              destPartListEntry.getAsId().toString(), partNumber,
                                                                              steeringAndGearboxValue),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                     MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return;
            }

            String steering = STEERING_GEARBOX_ERROR;
            String gearboxType = STEERING_GEARBOX_ERROR;
            if (matrixValues != null) {
                steering = matrixValues.getSteering();
                gearboxType = matrixValues.getGearboxType();
            }

            // Falls ein Matrix-Feld gewählt wurde, das nicht befüllt ist oder ein Feld gewählt wurde, das explizit
            // zu einem Fehler führt (siehe Confluence, DAIMLER-1853), dann Ausgabe in Log und nichts zur Teileposition übernehmen
            if (steering.equals(STEERING_GEARBOX_ERROR) && gearboxType.equals(STEERING_GEARBOX_ERROR)) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Unerlaubte Konstellation von Lenkung und Getriebeart zur Teileposition \"1\". Teilenummer: \"%2\", Marker in Feldern: %3 und %4",
                                                                              destPartListEntry.getAsId().toString(), partNumber,
                                                                              ((x == 0) ? "130a" : "130b"), ((y == 2) ? "130c" : "130d")),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                     MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return;
            }

            // Alles OK -> Lenkung und Getriebeart setzen
            destPartListEntry.setFieldValue(FIELD_K_STEERING, steering, DBActionOrigin.FROM_EDIT);
            destPartListEntry.setFieldValue(FIELD_K_GEARBOX_TYPE, gearboxType, DBActionOrigin.FROM_EDIT);
        }
    }

    public static void assignHierarchyValue(AbstractDataImporter importer, String value, EtkDataPartListEntry destPartListEntry, int currentRecordNo) {
        int hierarchy = StrUtils.strToIntDef(value, -1) + 1; // Einrückung um 1 erhöhen

        // ungültiger Wert für die Einrückzahl
        if ((hierarchy < 1) || (hierarchy > 9)) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Ungültige Einrückzahl \"%1\" im Record %2 gefunden (zulässig sind nur Werte von 0 bis 8, was korrigiert 1 bis 9 ergibt)",
                                                                          value, String.valueOf(currentRecordNo)),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                 MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        } else {
            destPartListEntry.setFieldValue(FIELD_K_HIERARCHY, String.valueOf(hierarchy), DBActionOrigin.FROM_EDIT);
        }
    }

    public static void assignCodeValue(String value, EtkDataPartListEntry destPartListEntry) {
        destPartListEntry.setFieldValue(FIELD_K_CODES, value, DBActionOrigin.FROM_EDIT);
    }

    public static void addPartListEntryIfWW(EtkDataPartListEntry destPartListEntry, String partNumber,
                                            Map<String, List<EtkDataPartListEntry>> currentWWPartNumbersToPLEntriesInAssembly,
                                            boolean isWW) {
        if (isWW) {
            // Map für Wahlweise-Behandlung befüllen
            List<EtkDataPartListEntry> partListEntryList = currentWWPartNumbersToPLEntriesInAssembly.get(partNumber);
            if (partListEntryList == null) {
                partListEntryList = new DwList<>();
                currentWWPartNumbersToPLEntriesInAssembly.put(partNumber, partListEntryList);
            }
            partListEntryList.add(destPartListEntry);
        }
    }

    public static void handleReplacementAndIncludeParts(AbstractDataImporter importer, EtkDataPartListEntry destPartListEntry,
                                                        String partNumber, String replacePart, List<String> includeParts,
                                                        Map<PartListEntryId, iPartsReplacement> replacementsForAssembly,
                                                        int currentRecordNo) {

        iPartsReplacement partReplacement = new iPartsReplacement();
        partReplacement.source = iPartsReplacement.Source.MAD;
        partReplacement.releaseState = iPartsDataReleaseState.RELEASED;
        partReplacement.predecessorEntry = destPartListEntry;
        // In den Testdaten gab es den Fall, dass ein Datensatz den Ersetzungskenner "R" hatte aber keine
        // Nachfolgermaterialnummer angegeben war
        if (!replacePart.isEmpty()) {
            partReplacement.setSuccessorPartNumber(replacePart);
            if (!includeParts.isEmpty()) {
                for (String includePartString : includeParts) {
                    partReplacement.addIncludePart(makeIncludePartForReplacement(importer, destPartListEntry.getAsId(), includePartString, currentRecordNo));
                }
            }
            replacementsForAssembly.put(destPartListEntry.getAsId(), partReplacement);
        } else {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Keine Nachfolger-Materialnummer für Ersetzung angegeben in Record %1. Ersetzung wird übersprungen. Stücklisteneintrag: %2; Materialnummer: %3",
                                                                          String.valueOf(currentRecordNo), destPartListEntry.getAsId().toString(", "),
                                                                          partNumber),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }

    }

    public static iPartsReplacement.IncludePart makeIncludePartForReplacement(AbstractDataImporter importer,
                                                                              PartListEntryId partListEntryId,
                                                                              String includePartString,
                                                                              int currentRecordNo) {
        iPartsReplacement.IncludePart includePart = new iPartsReplacement.IncludePart();
        if (includePartString.length() < 16) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1: Menge und Materialnummer des Mitlieferteils sind zu kurz. Stücklisteneintrag: %2; Menge und Materialnummer: %3",
                                                                          String.valueOf(currentRecordNo), partListEntryId.toString(", "),
                                                                          includePartString),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return null;
        }

        includePart.partNumber = includePartString.substring(3).replace(" ", "").trim();
        if (StrUtils.isEmpty(includePart.partNumber)) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1: Materialnummer des Mitlieferteils ist leer und wird nicht importiert. Stücklisteneintrag: %2",
                                                                          String.valueOf(currentRecordNo), partListEntryId.toString(", ")),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return null;
        }

        String quantityString = includePartString.substring(0, 3).trim();
        if (StrUtils.isEmpty(quantityString)) {
            includePart.quantity = "";
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1: Menge des Mitlieferteils ist leer. Mitlieferteil wird mit leerer Menge importiert. Stücklisteneintrag: %2; Materialnummer: %3",
                                                                          String.valueOf(currentRecordNo), partListEntryId.toString(", "),
                                                                          includePart.partNumber),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        } else {
            includePart.quantity = StrUtils.removeLeadingCharsFromString(quantityString, '0');
        }
        return includePart;
    }

    public static void finishSinglePartListEntry(AbstractDataImporter importer, EtkProject project,
                                                 EtkDataPartListEntry destPartListEntry, EtkMultiSprache additionalText,
                                                 int currentAdditionalTextSeqNo, EtkMultiSprache neutralText,
                                                 int currentNeutralTextSeqNo, boolean isYPartPos,
                                                 List<EtkDataPartListEntry> lastCompletePartListEntry,
                                                 Set<iPartsFootNoteId> footNoteIds,
                                                 iPartsMigrationFootnotesHandler footnotesHandler) {
        // Ergänzungstext speichern
        if (additionalText != null) {
            iPartsDataCombText dataCombText = new iPartsDataCombText(project, destPartListEntry.getAsId(), additionalText,
                                                                     null, currentAdditionalTextSeqNo);
            importer.saveToDB(dataCombText, false);
        }

        // Sprachneutralen Text speichern
        if (neutralText != null) {
            iPartsDataCombText dataCombText = new iPartsDataCombText(project, destPartListEntry.getAsId(), neutralText,
                                                                     null, currentNeutralTextSeqNo);
            importer.saveToDB(dataCombText, false);
        }

        // Fußnoten an die Stücklisteneinträge hängen
        if ((footNoteIds != null) && !footNoteIds.isEmpty()) {
            footnotesHandler.createFootNoteRefs(destPartListEntry.getAsId(), footNoteIds);
        }

        // bei einer echten Teileposition die Liste lastCompletePartListEntry aufbauen für spätere Ergänzungen
        // durch Y-Teilepositionen
        if (!isYPartPos) {
            lastCompletePartListEntry.add(destPartListEntry);
        }
    }

    /**
     * Verarbeitet die Modul-Meta-Daten
     *
     * @param currentAssembly
     * @param moduleVariantsVisibleMap
     */
    public static void handleModuleMetaData(iPartsDataAssembly currentAssembly, Map<iPartsModuleId, Boolean> moduleVariantsVisibleMap) {
        iPartsModuleId moduleId = new iPartsModuleId(currentAssembly.getModuleMetaData().getAsId().getModuleNumber());
        Boolean variantsVisible = moduleVariantsVisibleMap.get(moduleId);
        iPartsDataModule dataModule = currentAssembly.getModuleMetaData();
        if (variantsVisible != null) {
            dataModule.setFieldValueAsBoolean(FIELD_DM_VARIANTS_VISIBLE, variantsVisible, DBActionOrigin.FROM_EDIT);
        }
        // Seit "DAIMLER-6435, Ausgabe PRIMUS Farbinformationen standardmäßig deaktivieren"
        // wird FIELD_DM_USE_COLOR_TABLEFN beim Import fest auf "TRUE" gesetzt.
        dataModule.setFieldValueAsBoolean(FIELD_DM_USE_COLOR_TABLEFN, true, DBActionOrigin.FROM_EDIT);

        dataModule.saveToDB();
    }

    public static void handleWWSetsForAssembly(AbstractDataImporter importer, iPartsDataAssembly currentAssembly,
                                               Map<String, Set<String>> currentWWPartNumbersToWWPartNumbersInAssembly,
                                               Set<String> currentPartNumbersInAssembly, Map<String, List<EtkDataPartListEntry>> currentWWPartNumbersToPLEntriesInAssembly) {
        int currentWWCounter = 0;
        Set<String> wwPartNumbersAlreadyHandledSet = new HashSet<>(); // Diese WW-Teilenummern wurden alle schon behandelt
        for (Map.Entry<String, Set<String>> wwPartNumberEntry : currentWWPartNumbersToWWPartNumbersInAssembly.entrySet()) {
            String wwPartNumber = wwPartNumberEntry.getKey();

            // Wurde diese WW-Teilenummer schon behandelt?
            if (wwPartNumbersAlreadyHandledSet.contains(wwPartNumber)) {
                continue;
            }

            Set<String> wwRefPartNumbers = wwPartNumberEntry.getValue();

            // Dieses Set muss für alle WW-Teile des Sets identisch sein
            Set<String> wwCompletePartNumbersSet = new TreeSet<>();
            wwCompletePartNumbersSet.add(wwPartNumber);
            wwCompletePartNumbersSet.addAll(wwRefPartNumbers);

            boolean wwPartNumberSetValid = true;
            for (String wwRefPartNumber : wwRefPartNumbers) { // für alle WW-Teile überprüfen, ob ein identisches WW-Teile-Set entsteht
                if (!currentPartNumbersInAssembly.contains(wwRefPartNumber)) { // WW-Teil existiert nicht im Modul
                    wwPartNumberSetValid = false;
                    importer.getMessageLog().fireMessage(importer.translateForLog("!!Die Wahlweise-Teilenummer \"%1\" referenziert von der Teilenummer \"%2\" existiert nicht im Modul \"%3\" -> Wahlweise-Set ist ungültig",
                                                                                  wwRefPartNumber, wwPartNumber, currentAssembly.getAsId().getKVari()),
                                                         MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                } else { // WW-Teile-Set überprüfen
                    Set<String> wwRefRefPartNumbers = currentWWPartNumbersToWWPartNumbersInAssembly.get(wwRefPartNumber);
                    if (wwRefRefPartNumbers != null) {
                        Set<String> wwRefCompletePartNumbersSet = new TreeSet<>();
                        wwRefCompletePartNumbersSet.add(wwRefPartNumber);
                        wwRefCompletePartNumbersSet.addAll(wwRefRefPartNumbers);
                        if (!wwCompletePartNumbersSet.equals(wwRefCompletePartNumbersSet)) { // WW-Teile-Sets sind unterschiedlich
                            wwPartNumberSetValid = false;
                            importer.getMessageLog().fireMessage(importer.translateForLog("!!Das Wahlweise-Set \"%1\" referenziert von der Teilenummer \"%2\" ist inkonsistent zu den Wahlweise-Set \"%3\" von der Teilenummer \"%4\" im Modul \"%5\" -> Wahlweise-Set ist ungültig",
                                                                                          StrUtils.stringListToString(wwRefCompletePartNumbersSet, ", "), wwRefPartNumber,
                                                                                          StrUtils.stringListToString(wwCompletePartNumbersSet, ", "), wwPartNumber,
                                                                                          currentAssembly.getAsId().getKVari()),
                                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        }
                    } else { // Referenziertes WW-Teil ist selbst kein WW-Teil
                        wwPartNumberSetValid = false;
                        importer.getMessageLog().fireMessage(importer.translateForLog("!!Die Wahlweise-Teilenummer \"%1\" referenziert von der Teilenummer \"%2\" im Modul \"%3\" ist selbst kein Wahlweise-Teil -> Wahlweise-Set ist ungültig",
                                                                                      wwRefPartNumber, wwPartNumber, currentAssembly.getAsId().getKVari()),
                                                             MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }
                }
            }

            if (wwPartNumberSetValid) {
                currentWWCounter++;
            }
            String wwGUID = String.valueOf(currentWWCounter);

            // Wahlweise-Werte (GUID bzw. Extra Wahlweise-Teilenummern) für alle Stücklisteneinträge der WW-Teilenummer
            // und referenzierten WW-Teilenummern setzen
            setWWValuesForPartListEntries(wwPartNumber, wwRefPartNumbers, wwPartNumberSetValid, wwGUID, currentWWPartNumbersToPLEntriesInAssembly);
            for (String wwRefPartNumber : wwRefPartNumbers) {
                if (!wwPartNumbersAlreadyHandledSet.contains(wwRefPartNumber)) {
                    setWWValuesForPartListEntries(wwRefPartNumber, currentWWPartNumbersToWWPartNumbersInAssembly.get(wwRefPartNumber),
                                                  wwPartNumberSetValid, wwGUID, currentWWPartNumbersToPLEntriesInAssembly);
                }
            }

            wwPartNumbersAlreadyHandledSet.addAll(wwCompletePartNumbersSet);
        }
    }

    private static void setWWValuesForPartListEntries(String wwPartNumber, Set<String> wwRefPartNumbers,
                                                      boolean wwPartNumberSetValid, String wwGUID,
                                                      Map<String, List<EtkDataPartListEntry>> currentWWPartNumbersToPLEntriesInAssembly) {
        List<EtkDataPartListEntry> partListEntryList = currentWWPartNumbersToPLEntriesInAssembly.get(wwPartNumber);
        if (partListEntryList != null) {
            for (EtkDataPartListEntry partListEntry : partListEntryList) {
                if (wwPartNumberSetValid) { // GUID vom Wahlweise-Set setzen
                    partListEntry.setFieldValue(FIELD_K_WW, wwGUID, DBActionOrigin.FROM_EDIT);
                } else if (wwRefPartNumbers != null) { // Set ist ungültig -> Extra Wahlweise-Teilenummern setzen
                    partListEntry.setFieldValue(FIELD_K_WW_EXTRA_PARTS, StrUtils.stringListToString(wwRefPartNumbers, ", "),
                                                DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    public static void handleReplacementAndIncludePartsForAssembly(EtkProject project, AbstractDataImporter importer,
                                                                   iPartsDataAssembly currentAssembly,
                                                                   Map<PartListEntryId, iPartsReplacement> replacementsForAssembly) {
        if (!replacementsForAssembly.isEmpty()) {
            Map<String, List<EtkDataPartListEntry>> successors = new HashMap<String, List<EtkDataPartListEntry>>();
            iPartsDataReplacePartList replacePartList = new iPartsDataReplacePartList();
            iPartsDataIncludePartList includePartList = new iPartsDataIncludePartList();

            // Alle zu ersetzenden Materialnummern als Schlüssel in einer Map halten
            for (iPartsReplacement replacement : replacementsForAssembly.values()) {
                successors.put(replacement.successorPartNumber, new DwList<EtkDataPartListEntry>());
            }

            // Stückliste durchsuchen nach Materialnummern, die in den Ersetzungen vorkommen
            for (EtkDataPartListEntry entry : currentAssembly.getPartListUnfiltered(null, false, false)) {
                List<EtkDataPartListEntry> successorEntries = successors.get(entry.getPart().getAsId().getMatNr());
                if (successorEntries != null) {
                    successorEntries.add(entry);
                }
            }

            for (iPartsReplacement partReplacement : replacementsForAssembly.values()) {
                List<EtkDataPartListEntry> successorEntries = successors.get(partReplacement.successorPartNumber);
                if (successorEntries.size() > 1) {
                    // Wenn mehrere Nachfolger-Stücklistenpositionen gefunden wurden -> Bestimme den Stücklisteneintrag
                    // mit der größten Ähnlichkeit zum Vorgänger
                    List<EtkDataPartListEntry> equalEntries = iPartsScoringHelper.getMostEqualPartListEntries(partReplacement.predecessorEntry,
                                                                                                              successorEntries);
                    if (equalEntries.size() > 1) {
                        String equalPartListEntries = "";
                        for (EtkDataPartListEntry entry : equalEntries) {
                            if (!equalPartListEntries.isEmpty()) {
                                equalPartListEntries += " | ";
                            }
                            equalPartListEntries += entry.getAsId().toString(", ");
                        }
                        importer.getMessageLog().fireMessage(importer.translateForLog("!!Mehrere gleichgewichtige Nachfolger möglich. Vorgänger: %1; mögliche Nachfolger: %2; gewählter Nachfolger: %3",
                                                                                      partReplacement.predecessorEntry.getAsId().toString(", "),
                                                                                      equalPartListEntries, equalEntries.get(0).getAsId().toString(", ")),
                                                             MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                    }

                    // Wenn mehrere Stücklisteneinträge die gleiche Ähnlichkeit zum Original-Eintrag haben -> nehme den ersten
                    partReplacement.successorEntry = equalEntries.get(0);
                } else if (successorEntries.size() == 1) {
                    // Wenn nur ein Nachfolger gefunden wurde, dann wird dieser eingetragen
                    partReplacement.successorEntry = successorEntries.get(0);
                }

                // Wenn kein Nachfolger in der Stückliste gefunden wurde, dann wird eine Ersetzungsbeziehung erstellt
                // bei der der Nachfolger keine laufende Nummer besitzt
                partReplacement.setReplacementSeqNo(1);
                replacePartList.add(partReplacement.getAsDataReplacePart(project), DBActionOrigin.FROM_EDIT);
                includePartList.addAll(partReplacement.getIncludePartsAsDataIncludePartList(project).getAsList(),
                                       DBActionOrigin.FROM_EDIT);
            }

            replacePartList.saveToDB(project, false);
            includePartList.saveToDB(project, false);
            replacementsForAssembly.clear();
        }
    }

    /**
     * Speichert die aktuelle BM-Assembly, die während einem Import erstellt wurde
     *
     * @param importer
     * @param currentAssembly
     * @param currentAssembliesForKgInProduct
     * @param picReferenceIdsForAssembly
     */
    public static boolean storeFinishedAssemblyForModel(AbstractDataImporter importer, iPartsDataAssembly currentAssembly,
                                                        List<iPartsDataAssembly> currentAssembliesForKgInProduct,
                                                        Map<AssemblyId, Set<iPartsPicReferenceId>> picReferenceIdsForAssembly,
                                                        boolean messagesOnlyInLogFile) {
        boolean hasValidPartListEntries = currentAssembly.getPartListUnfiltered(null, false, false).size() > 0;
        // Laut DAIMLER-6992 sollen nun TUs, die nur Y-Positionen besitzen doch angelegt werden
        // Nicht speichern, wenn das Modul keine oder nur Y-Stücklistenpositionen (Teilenummer leer) besitzt
//        for (EtkDataPartListEntry partListEntry : currentAssembly.getPartListUnfiltered(null, false, false)) {
//            String partNumber = partListEntry.getFieldValue(FIELD_K_MATNR);
//            if (StrUtils.isValid(partNumber)) {
//                hasValidPartListEntries = true;
//                break;
//            }
//        }
        if (hasValidPartListEntries) {
            // Bei TAL40A werden alle Module einer KG zusammen in saveCurrentAssembliesForKgInProduct() gespeichert -> hier nur merken
            currentAssembliesForKgInProduct.add(currentAssembly);
        } else {
            handleEmptyAssembly(importer, currentAssembly, picReferenceIdsForAssembly, messagesOnlyInLogFile);
        }
        return hasValidPartListEntries;
    }

    public static boolean storeFinishedAssemblyForModel(AbstractDataImporter importer, iPartsDataAssembly currentAssembly,
                                                        List<iPartsDataAssembly> currentAssembliesForKgInProduct,
                                                        Map<AssemblyId, Set<iPartsPicReferenceId>> picReferenceIdsForAssembly) {
        return storeFinishedAssemblyForModel(importer, currentAssembly, currentAssembliesForKgInProduct, picReferenceIdsForAssembly, false);
    }

    /**
     * Speichert die aktuelle SA-Assembly, die während einem Import erstellt wurde
     *
     * @param importer
     * @param currentAssembly
     * @param picReferenceIdsForAssembly
     * @param footnotesHandler
     * @param allCurrentColorTablefootnotes
     * @param checkIfStandardFootnote
     */
    public static void storeFinishedAssemblyForSa(AbstractDataImporter importer, iPartsDataAssembly currentAssembly,
                                                  Map<AssemblyId, Set<iPartsPicReferenceId>> picReferenceIdsForAssembly,
                                                  iPartsMigrationFootnotesHandler footnotesHandler,
                                                  Map<String, List<MadTal4XABaseImporter.ColortTablefootnote>> allCurrentColorTablefootnotes,
                                                  Map<iPartsFootNoteId, Set<List<iPartsFootNoteId>>> allFootnoteGroupsForFootnoteNumber,
                                                  boolean messagesOnlyInLogFile, boolean checkIfStandardFootnote) {
        // Nicht speichern, wenn das Modul keine Stücklistenpositionen besitzt
        int partListEntriesAmount = currentAssembly.getPartListUnfiltered(null, false, false).size();
        if (partListEntriesAmount > 0) {
            // Bei TAL46A Modul für die SA speichern
            currentAssembly.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
            // aufgesammelte Fußnoten speichern
            footnotesHandler.saveCurrentFootNotesForPartListEntries(allCurrentColorTablefootnotes, allFootnoteGroupsForFootnoteNumber, checkIfStandardFootnote); // alle Fußnoten für die SA speichern

            String message = importer.translateForLog("!!%1 wurde importiert.", currentAssembly.getAsId().getKVari());
            importer.getMessageLog().fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);

        } else {
            handleEmptyAssembly(importer, currentAssembly, picReferenceIdsForAssembly, messagesOnlyInLogFile);
        }
    }

    public static void storeFinishedAssemblyForSa(AbstractDataImporter importer, iPartsDataAssembly currentAssembly,
                                                  Map<AssemblyId, Set<iPartsPicReferenceId>> picReferenceIdsForAssembly,
                                                  iPartsMigrationFootnotesHandler footnotesHandler,
                                                  Map<String, List<MadTal4XABaseImporter.ColortTablefootnote>> allCurrentColorTablefootnotes,
                                                  boolean checkIfStandardFootnote) {
        storeFinishedAssemblyForSa(importer, currentAssembly, picReferenceIdsForAssembly, footnotesHandler,
                                   allCurrentColorTablefootnotes, null, false, checkIfStandardFootnote);

    }

    private static void handleEmptyAssembly(AbstractDataImporter importer, iPartsDataAssembly currentAssembly,
                                            Map<AssemblyId, Set<iPartsPicReferenceId>> picReferenceIdsForAssembly,
                                            boolean messageOnlyInLog) {
        MessageLogOption[] options;
        if (messageOnlyInLog) {
            options = new MessageLogOption[]{ MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE };
        } else {
            options = new MessageLogOption[]{ MessageLogOption.TIME_STAMP };
        }
        // Modul wurde schon angelegt. Da keine Stücklistenpositionen vorhanden sind, muss es hier wieder gelöscht werden
        importer.getMessageLog().fireMessage(importer.translateForLog("!!Modul \"%1\" enthält keine Stücklistenpositionen und wird nicht importiert",
                                                                      currentAssembly.getAsId().getKVari()), MessageLogType.tmlMessage,
                                             options);
        currentAssembly.delete_iPartsAssembly(true);
        if (picReferenceIdsForAssembly != null) {
            // Entferne die Bildreferenze zu diesem Modul. Dadurch werden unnötige MQ Anfrage vermieden.
            picReferenceIdsForAssembly.remove(currentAssembly.getAsId());
        }
    }

    public static void saveCurrentAssembliesForKgInProduct(AbstractDataImporter importer,
                                                           List<iPartsDataAssembly> currentAssembliesForKgInProduct,
                                                           String currentKgInProduct) {
        saveCurrentAssembliesForKgInProduct(importer, currentAssembliesForKgInProduct, currentKgInProduct, false);

    }

    public static void saveCurrentAssembliesForKgInProduct(AbstractDataImporter importer,
                                                           List<iPartsDataAssembly> currentAssembliesForKgInProduct,
                                                           String currentKgInProduct, boolean messageOnlyInLog) {
        // Module für die KG speichern
        if (!currentAssembliesForKgInProduct.isEmpty()) {
            int modulesCountForKg = currentAssembliesForKgInProduct.size();
            MessageLogOption[] options;
            if (messageOnlyInLog) {
                options = new MessageLogOption[]{ MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE };
            } else {
                options = new MessageLogOption[]{ MessageLogOption.TIME_STAMP };
            }
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Speichere %1 Modul(e) für KG %2...", String.valueOf(modulesCountForKg), currentKgInProduct),
                                                 MessageLogType.tmlMessage, options);
            importer.getMessageLog().fireProgress(0, modulesCountForKg, "", true, true);
            int counter = 0;
            for (iPartsDataAssembly assemblyForKg : currentAssembliesForKgInProduct) {
                if (Thread.currentThread().isInterrupted()) {
                    importer.getMessageLog().hideProgress();
                    importer.getMessageLog().fireMessage("!!Speichern der Module abgebrochen, da der Thread frühzeitig beendet wurde",
                                                         MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                    return;
                }

                assemblyForKg.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);

                counter++;
                importer.getMessageLog().fireProgress(counter, modulesCountForKg, "", true, true);
            }
            importer.getMessageLog().hideProgress();

            currentAssembliesForKgInProduct.clear();
        }
    }

    public static int handleAddOrNeutralTextForYPartPosition(EtkProject project, AbstractDataImporter importer,
                                                             EtkMultiSprache multiText, int currentCombinedTextSeqNo,
                                                             List<EtkDataPartListEntry> lastCompletePartListEntry) {
        if (multiText != null) {
            for (EtkDataPartListEntry partListEntry : lastCompletePartListEntry) {
                iPartsDataCombText dataCombText = new iPartsDataCombText(project, partListEntry.getAsId(),
                                                                         multiText, null, currentCombinedTextSeqNo);
                importer.saveToDB(dataCombText, false);
            }
            return currentCombinedTextSeqNo + 1;
        }
        return currentCombinedTextSeqNo;
    }

    public static void handleFootNotesForYPartPosition(Set<iPartsFootNoteId> footNoteIds, Set<iPartsFootNoteId> currentFootNotesForPartListEntry, List<EtkDataPartListEntry> lastCompletePartListEntry, iPartsMigrationFootnotesHandler footnotesHandler) {
        if (footNoteIds != null) {
            footNoteIds.removeAll(currentFootNotesForPartListEntry); // im Stücklisteneintrag bereits vorhandene Fußnotennummern entfernen
            for (EtkDataPartListEntry partListEntry : lastCompletePartListEntry) {
                footnotesHandler.createFootNoteRefs(partListEntry.getAsId(), footNoteIds);
            }
            currentFootNotesForPartListEntry.addAll(footNoteIds);
        }
    }

    public static void handleCodeValueForYPartPosition(String codeValue, List<EtkDataPartListEntry> lastCompletePartListEntry) {
        if ((codeValue != null) && !codeValue.isEmpty()) {
            for (EtkDataPartListEntry partListEntry : lastCompletePartListEntry) {
                String existingCode = partListEntry.getFieldValue(FIELD_K_CODES);
                partListEntry.setFieldValue(FIELD_K_CODES, existingCode + codeValue, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    public static void handleReplacePartAndIncludePartsForYPartPosition(AbstractDataImporter importer,
                                                                        List<EtkDataPartListEntry> lastCompletePartListEntry,
                                                                        Map<PartListEntryId, iPartsReplacement> replacementsForAssembly,
                                                                        String replacePart, List<String> includeParts,
                                                                        int recordNo) {
        // Ersetzungsobjekte in lastCompletePartListEntry holen und um Mitlieferteile ergänzen
        for (EtkDataPartListEntry partListEntry : lastCompletePartListEntry) {
            iPartsReplacement partReplacement = replacementsForAssembly.get(partListEntry.getAsId());
            // Falls die Y-Position eine MatNr (Ersetzung) hat und diese ungleich der MatNr des vorherigen
            // Stücklisteneintrags ist -> Fehlermeldung
            if (!StrUtils.isEmpty(replacePart) && !replacePart.equals(partReplacement.successorPartNumber)) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Y-Position-Ersetzung \"%1\" " +
                                                                              "stimmt nicht mit Stücklistenposition-Ersetzung" +
                                                                              " \"%2\" überein im Record %3.",
                                                                              replacePart, partReplacement.successorPartNumber, String.valueOf(recordNo)),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
            if (!includeParts.isEmpty()) {
                for (String includePartString : includeParts) {
                    partReplacement.addIncludePart(makeIncludePartForReplacement(importer, partReplacement.predecessorEntry.getAsId(),
                                                                                 includePartString, recordNo));
                }
            }
        }
    }

    public static Set<String> handleSasOrBkValidityForModel(AbstractDataImporter importer, String type, List<String> values) {
        Set<String> saaBkValidity = new LinkedHashSet<>();
        // In den BM-Katalogen gibt es zwei Möglichkeiten, SAAs oder Baumkästen
        if (type.equals("SA")) {
            // Es stehen SAs drin
            for (String sa : values) {
                // Links darf nicht getrimmt werden, da SAs auch mit leer !!! anfangen können.
                sa = StrUtils.trimRight(sa);
                if (!sa.trim().isEmpty()) {
                    // Rumpf ermitteln: die ersten 6 Stellen sind die Rumpf-SAA oder SA (ohne Sachnummernkennzeichen "Z")
                    // Danach kommen 9 mal 2 Stellen, das sind die gültigen Strich-Ausführungen zu dieser Rumpf-SAA
                    String saRumpf = null;
                    try {
                        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                        saRumpf = numberHelper.unformatSaForDB(sa.substring(0, 6));
                    } catch (RuntimeException e) {
                        importer.getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                        continue;
                    }

                    int offset = 6;
                    while (sa.length() > offset + 1) { // 2 Stellen (offset und eine dazu) müssen in sa noch verfügbar sein
                        String saStrich = sa.substring(offset, offset + 2);
                        if (!saStrich.trim().isEmpty()) {
                            String newSa = saRumpf + saStrich;
                            saaBkValidity.add(newSa);
                        }
                        offset += 2;
                    }
                }
            }
        } else if (type.equals("BK")) {
            // Es stehen Baukästen drin
            for (String component : values) {
                saaBkValidity.add(component);
            }
        }
        return saaBkValidity;

    }

    /**
     * Prüft, ob es sich bei der SA mit der übergebenen ID um eine ausgeblendete SA handelt.
     *
     * @param saModulesId
     * @param project
     * @return
     */
    public static boolean isSAHidden(iPartsSAModulesId saModulesId, EtkProject project) {
        // Über den deutschen Text der SA-Benennung überprüfen, ob das SA-Modul ausgeblendet werden soll
        String saName = iPartsSA.getInstance(project, saModulesId).getTitle(project).getText(Language.DE.getCode());

        if (!StrUtils.isEmpty(saName)) {
            for (String prefix : HIDDEN_SA_MODULE_PREFIXES) {
                if (StrUtils.stringStartsWith(saName, prefix, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static EtkDataPartListEntry findSimilarEntryinSaa(iPartsDataAssembly currentAssembly, EtkDataPartListEntry searchEntry) {
        for (EtkDataPartListEntry entry : currentAssembly.getPartListUnfiltered(null, false, false)) {
            // wegen Performance erstmal die Matnr prüfen
            if (entry.getFieldValue(FIELD_K_MATNR).equals(searchEntry.getFieldValue(FIELD_K_MATNR))) {
                // Jetzt die anderen Felder
                if (entryIsSimilar(searchEntry, entry)) {
                    return entry;
                }
            }
        }
        return null;
    }

    public static boolean entryIsSimilar(EtkDataPartListEntry entry1, EtkDataPartListEntry entry2) {
        for (DBDataObjectAttribute field1 : entry1.getAttributes().getFields()) {
            if (!field1.isVirtual() && !ignoreFieldsForEntryComparison.contains(field1.getName())) {
                DBDataObjectAttribute field2 = entry2.getAttribute(field1.getName(), false);

                if (field2 == null) {
                    return false;
                }

                if (!field1.equalContent(field2)) {
                    return false;
                }
            }

        }
        // Keinen Unterschied gefunden
        return true;
    }

    public static boolean handleSimiliarSaPartlistEntry(iPartsDataAssembly currentAssembly, EtkDataPartListEntry destPartListEntry) {
        EtkDataPartListEntry similarEntry = iPartsMigrationHelper.findSimilarEntryinSaa(currentAssembly, destPartListEntry);
        if (similarEntry != null) {
            // Gleiches gefunden, hänge die SAA-Gültigkeiten an
            EtkDataArray saValuesNew = destPartListEntry.getFieldValueAsArrayOriginal(FIELD_K_SA_VALIDITY);
            EtkDataArray saValuesOld = similarEntry.getFieldValueAsArray(FIELD_K_SA_VALIDITY); // Array wird verändert -> nicht getFieldValueAsArrayOriginal() verwenden

            // Testen, ob in den alten Values schon ein Wert der neuen drin ist
            // Das kann nur poassieren, wenn identische Datensätze in dem gleichen Intervall vorkommen,
            // dann ist das so zu interpretieren, dass diese Datensätze doppelt sind und auch zweimal angezeigt werden müssen
            for (DBDataObjectAttribute attribute1 : saValuesNew.getAttributes()) {
                for (DBDataObjectAttribute attribute2 : saValuesOld.getAttributes()) {
                    if (attribute1.getAsString().equals(attribute2.getAsString())) {
                        // Die Datensätze sind identisch, aber sollen in der Stückliste zweimal angezeigt werden
                        // also ganz normal abspeichern und nicht in einen Datensatz verdichten
                        similarEntry = null;
                    }
                }
            }

            if (similarEntry != null) {

                for (DBDataObjectAttribute attribute : saValuesNew.getAttributes()) {
                    saValuesOld.add(attribute.getAsString());
                }

                similarEntry.setFieldValueAsArray(FIELD_K_SA_VALIDITY, saValuesOld, DBActionOrigin.FROM_EDIT);

                return true;
            }
        }
        return false;
    }

    public static class QuantityForModelOrSAA {

        private Map<String, List<String>> values;

        public QuantityForModelOrSAA() {
            values = new LinkedHashMap<>(); // Linked, damit die Reihenfolge für die Mengen erhalten bleibt
        }

        public void add(String quantity, String number) {
            List<String> value = values.get(quantity);
            if (value == null) {
                value = new ArrayList<>();
                values.put(quantity, value);
            }
            value.add(number);
        }

        public Set<String> getQuantities() {
            return values.keySet();
        }

        public List<String> getNumbers(String quantity) {
            return values.get(quantity);
        }

    }

    /**
     * Klasse für die matrixbezogene Zuordnung von Lenkung und Getriebeart zu Teileposition
     */
    private static class SteeringAndGearboxValue {

        private String steering;
        private String gearboxType;

        public SteeringAndGearboxValue(String steering, String gearboxType) {
            this.steering = steering;
            this.gearboxType = gearboxType;
        }

        public String getSteering() {
            return steering;
        }

        public String getGearboxType() {
            return gearboxType;
        }
    }
}
