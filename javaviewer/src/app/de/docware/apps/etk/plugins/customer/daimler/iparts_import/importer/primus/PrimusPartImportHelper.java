/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.primus;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTermIdHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindPRIMUSTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.iPartsMaterialImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class PrimusPartImportHelper extends AbstractPrimusImportHelper {

    static class PrimusPartData {

        PrimusBasicPartData partData = new PrimusBasicPartData();
        String termId = "";
        EtkMultiSprache multi = new EtkMultiSprache();
        String lastLanguage = "";
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
    }

    private Set<String> primusRedundantFields;
    private PrimusPartData currentData;
    private final iPartsDictTextKindId txtKindId;

    public PrimusPartImportHelper(EtkProject project, iPartsDictTextKindId txtKindId, String logLanguage, EtkMessageLog messageLog) {
        super(project, messageLog, logLanguage, getMapping(), TABLE_MAT);

        primusRedundantFields = new HashSet<>();
        primusRedundantFields.add(FIELD_M_SECURITYSIGN);
        primusRedundantFields.add(FIELD_M_CERTREL);
        primusRedundantFields.add(FIELD_M_THEFTREL);
        primusRedundantFields.add(FIELD_M_THEFTRELINFO);
        primusRedundantFields.add(FIELD_M_VEDOCSIGN);
        primusRedundantFields.add(FIELD_M_ESD_IND);

        this.txtKindId = txtKindId;

        currentData = new PrimusPartData();
    }

    private static HashMap<String, String> getMapping() {
        HashMap<String, String> mappingPrimusPart = new HashMap<>();
        mappingPrimusPart.put(FIELD_M_BRAND, MSG_BRD);
        mappingPrimusPart.put(FIELD_M_STATE, MSG_STATE);
        mappingPrimusPart.put(FIELD_M_QUANTUNIT, MSG_QUANTUNIT);
        mappingPrimusPart.put(FIELD_M_REMAN_IND, MSG_REMAN);
        mappingPrimusPart.put(FIELD_M_CHINA_IND, MSG_CHINA);
        mappingPrimusPart.put(FIELD_M_NATO_NO, MSG_NATO);
        mappingPrimusPart.put(FIELD_M_SVHC_IND, MSG_SVHC);
        mappingPrimusPart.put(FIELD_M_ARMORED_IND, MSG_TECH_INFO);
        mappingPrimusPart.put(FIELD_M_SECURITYSIGN, MSG_SECURITY);
        mappingPrimusPart.put(FIELD_M_CERTREL, MSG_CERTIFICATION);
        mappingPrimusPart.put(FIELD_M_THEFTREL, MSG_THEFT_REL);
        mappingPrimusPart.put(FIELD_M_THEFTRELINFO, MSG_THEFT_REL_INFO);
        mappingPrimusPart.put(FIELD_M_VEDOCSIGN, MSG_FDOK_REL);
        mappingPrimusPart.put(FIELD_M_ESD_IND, MSG_ESD_IND);
        // DAIMLER-12220, Neue Teilestammattribute aus PRIMUS
        mappingPrimusPart.put(FIELD_M_WEIGHT, MSG_WEIGHT);
        mappingPrimusPart.put(FIELD_M_LENGTH, MSG_LENGTH);
        mappingPrimusPart.put(FIELD_M_WIDTH, MSG_WIDTH);
        mappingPrimusPart.put(FIELD_M_HEIGHT, MSG_HEIGHT);
        mappingPrimusPart.put(FIELD_M_VOLUME, MSG_VOLUME);
        // DAIMLER-14252, Import Gefahrgutkenner aus PRIMUS
        mappingPrimusPart.put(FIELD_M_HAZARDOUS_GOODS_INDICATOR, MSG_HAZ_GOOD_IND);

        return mappingPrimusPart;
    }

    @Override
    protected boolean handleCurrentTag(String tagPath, String tagContent, int tagCount, Map<String, String> currentRecord) {
        boolean processedTag = fillOverrideCompleteData(currentData, tagPath, tagContent);
        if (tagPath.equals(MSG_PTN)) {
            processedTag = formatPrimusPartNo(tagContent, currentData.partData);
        } else if (tagPath.equals(MSG_TERMID)) {
            if (tagContent.startsWith("4")) {
                messageLog.fireMessage(translateForLog("!!Tag %1: TermId \"%2\" wird nicht übernommen",
                                                       String.valueOf(tagCount), currentData.termId),
                                       MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            } else {
                currentData.termId = iPartsTermIdHandler.removeLeadingZerosFromTermId(tagContent);
                processedTag = true;
            }
        } else if (tagPath.equals(MSG_LANGUAGE)) {
            int index = Language.getIndexByLanguageCode(tagContent);
            if (index != Language.NO_LANGUAGE) {
                currentData.lastLanguage = Language.values()[index].getCode();
                processedTag = true;
            } else {
                currentData.lastLanguage = "";
            }
        } else if (tagPath.equals(MSG_DESCRIPTION)) {
            if (!currentData.lastLanguage.isEmpty() && !tagContent.isEmpty()) {
                currentData.multi.setText(currentData.lastLanguage, tagContent);
                currentData.lastLanguage = "";
                processedTag = true;
            }
        }
        return processedTag;
    }

    /**
     * Speicherroutine
     *
     * @return
     */
    @Override
    protected GenericEtkDataObjectList<EtkDataObject> handleRecord(Map<String, String> currentRecord) {
        GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved;
        if (getCurrentAction(currentRecord) == PrimusAction.DELETE) {
            dataObjectsToBeSaved = deleteData();
        } else {
            dataObjectsToBeSaved = saveData();
        }
        currentData = new PrimusPartData();
        return dataObjectsToBeSaved;
    }

    @Override
    public void initActionMapping(Map<String, PrimusAction> actionMapping) {
        actionMapping.put("PIN", PrimusAction.INSERT);
        actionMapping.put("PUP", PrimusAction.UPDATE);
        actionMapping.put("PDEL", PrimusAction.DELETE);
    }

    private GenericEtkDataObjectList<EtkDataObject> deleteData() {
        GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved = new GenericEtkDataObjectList<>();
        if (!StrUtils.isEmpty(currentData.partData.partNo)) {
            // Eine gültige Teilnummer wurde gefunden, speichere die Daten zu dem Teil
            EtkDataPart part = createDataPart(currentData.partData);
            if (part.existsInDB()) {
                part.setFieldValueAsBoolean(FIELD_M_IS_DELETED, true, DBActionOrigin.FROM_EDIT);
                dataObjectsToBeSaved.add(part, DBActionOrigin.FROM_EDIT);
            }
            deletePrimusColortable();
        }
        return dataObjectsToBeSaved;
    }

    private GenericEtkDataObjectList<EtkDataObject> saveData() {
        if (StrUtils.isEmpty(currentData.partData.partNo)) {
            messageLog.fireMessage(translateForLog("!!Teilenummer konnte nicht bestimmt werden."),
                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return null;
        }

        // Eine gültige Teilnummer wurde gefunden, speichere die Daten zu dem Teil
        EtkDataPart part = createDataPart(currentData.partData);

        setBasicPartFieldValues(part, currentData.partData);

        boolean hasTermId = !currentData.termId.isEmpty(); // hat der neue Datensatz eine Term-ID?
        boolean termIdExists = StrUtils.isValid(part.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR).getTextId()); // Besitzt der aktuelle DB Datensatz schon eine Term-ID?
        boolean isOnlyPrimus = (part.getFieldValueAsSetOfEnum(FIELD_M_SOURCE).size() == 1)  // handelt es sich um ein Teil, das nur von PRIMUS versorgt wurde?
                               && part.containsFieldValueSetOfEnumValue(FIELD_M_SOURCE,
                                                                        iPartsImportDataOrigin.PRIMUS.getOrigin());
        boolean datasetHasText = !part.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR).isEmpty(); // existiert für den Datensatz schon ein Text?
        // Abgleich mit Lexikon immer
        // Nur Importieren, wenn
        // 1. Teil noch nicht existiert
        // 2. XML eine Term-ID enthält und der Datensatz in der DB noch keinen (außer wenn als Quelle nur PRIMUS
        //   gesetzt ist, das wird aber mit "isOnlyPrimus" abgedeckt)
        // 3. Teil noch keine Benennung hat
        // 4. Teil ausschließlich aus PRIMUS versorgt wurde
        // 5. Teilestamm noch nicht von SRM angepasst worden ist (Text-Id via SRM Importer) - ist eigentlich unmöglich,
        // weil 4. im Moment 5. ausschließt
        if ((!part.existsInDB() || (hasTermId && !termIdExists) || isOnlyPrimus || !datasetHasText)
            && !currentData.multi.allStringsAreEmpty()) {
            if (!iPartsMaterialImportHelper.hasSRMTextId(part)) {
                DictImportTextIdHelper importTextIdHelper = new DictImportTextIdHelper(getProject());
                importTextIdHelper.handleDictTextId(DictTextKindPRIMUSTypes.MAT_AFTER_SALES, txtKindId, currentData.multi, currentData.termId,
                                                    DictHelper.getPRIMUSForeignSource(), TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR));
                if (importTextIdHelper.hasWarnings()) {
                    //Fehler beim Dictionary Eintrag
                    for (String str : importTextIdHelper.getWarnings()) {
                        messageLog.fireMessage(str, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }
                    return null;
                }
                part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, currentData.multi, DBActionOrigin.FROM_EDIT);
            } else {
                // SRM verhindert eine Übernahme des Textes
                messageLog.fireMessage(translateForLog("!!Für Teilenummer %1 konnte" +
                                                       " die Benennung nicht übernommen werden, " +
                                                       "da sie schon von SRM gesetzt wurde!",
                                                       part.getAsId().getMatNr()),
                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                       MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }

        // Die Primusattribute setzen
        // Regeln siehe DAIMLER-4195
        boolean isDialogPart = part.containsFieldValueSetOfEnumValue(FIELD_M_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin());
        boolean isEDSPart = part.containsFieldValueSetOfEnumValue(FIELD_M_SOURCE, iPartsImportDataOrigin.EDS.getOrigin());
        for (DBDataObjectAttribute attribute : currentData.attributes.getFields()) {
            if ((isDialogPart || isEDSPart) && primusRedundantFields.contains(attribute.getName())) {
                // Dieses Attribut ist nur Redundant in Primus, führend ist Dialog/BOM-DB
                // Speichere das deshalb nur, wenn das Teil noch nicht von Dialog/BOM-DB besetzt wurde. Ansonsten überspringen
                continue;
            }
            part.setFieldValue(attribute.getName(), attribute.getAsString(), DBActionOrigin.FROM_EDIT);
        }

        // Erzeuge Pseudo-Farbtabellen
        GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved = createPrimusColortable(part);

        dataObjectsToBeSaved.add(part, DBActionOrigin.FROM_EDIT);

        return dataObjectsToBeSaved;
    }

    private void deletePrimusColortable() {
        // Teilenummer oder ES2 darf nicht leer sein
        if (StrUtils.isEmpty(currentData.partData.partNo) || StrUtils.isEmpty(currentData.partData.es2)) {
            return;
        }
        messageLog.fireMessage(translateForLog("!!Lösche Verknüpfung Farbnummer \"%1\" zu Farbtabelle \"%2\".",
                                               currentData.partData.es2, currentData.partData.partNo),
                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        iPartsColorTableDataId colorTableDataId = new iPartsColorTableDataId(currentData.partData.partNo);
        iPartsDataColorTableContentList colorTableContentList =
                iPartsDataColorTableContentList.loadColorTableContentListForColorTable(getProject(), colorTableDataId);

        iPartsDataColorTableContentList colorTableContentsForRemoval = new iPartsDataColorTableContentList();
        for (iPartsDataColorTableContent colorTableContent : colorTableContentList) {
            if (colorTableContent.getColorNumber().equals(currentData.partData.es2)) {
                // Zwei Einträge mit gleicher Farbnummer -> sollte eigentlich nicht sein
                if (colorTableContentsForRemoval.size() >= 1) {
                    messageLog.fireMessage(translateForLog("!!Zwei gleiche Farbnummern zu einer PRIMUS Farbtabelle. Farbtabelle: %1, Farbnummer: %2",
                                                           currentData.partData.partNo, currentData.partData.es2),
                                           MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }
                colorTableContentsForRemoval.add(colorTableContent, DBActionOrigin.FROM_EDIT);
            }
        }
        if (!colorTableContentsForRemoval.isEmpty()) {
            try {
                // Wir haben alle Farbnummern zu dieser Farbtabelle entfernt -> Entferne somit alle Teile-Verknüpfungen sowie
                // die Farbtabelle an sich
                if (colorTableContentList.size() == colorTableContentsForRemoval.size()) {
                    // Verknüpfung Farbe zu Fabtabelle löschen
                    colorTableContentList.deleteFromDB(getProject());
                    messageLog.fireMessage(translateForLog("!!Farbtabelle \"%1\" enthält keine Farbnummern mehr. Lösche komplette Farbtabelle samt Verknüpfungen zu Teilenummern.",
                                                           currentData.partData.partNo),
                                           MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                    // Verknüpfung Teile zu Fabtabelle löschen
                    iPartsDataColorTableToPartList colorTableToPartList = iPartsDataColorTableToPartList.loadColorTableToPartListForColortableId(getProject(), colorTableDataId);
                    iPartsDataColorTableToPartList colorTableToPartListForRemoval = new iPartsDataColorTableToPartList();
                    for (iPartsDataColorTableToPart colorTableToPart : colorTableToPartList) {
                        String partNumber = colorTableToPart.getPartNumber();
                        if (!StrUtils.isEmpty(partNumber) && partNumber.equals(currentData.partData.partNo)) {
                            colorTableToPartListForRemoval.add(colorTableToPart, DBActionOrigin.FROM_EDIT);
                        }
                    }
                    colorTableToPartListForRemoval.deleteFromDB(getProject(), true);
                    // Farbtabelle löschen
                    iPartsDataColorTableData colorTableData = new iPartsDataColorTableData(getProject(), colorTableDataId);
                    if (colorTableData.existsInDB()) {
                        colorTableData.deleteFromDB();
                    } else {
                        messageLog.fireMessage(translateForLog("!!Fehler beim Löschen der Farbtabelle \"%1\". Farbtabelle existiert nicht!",
                                                               colorTableDataId.getColorTableId()),
                                               MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }


                } else {
                    // Verknüpfung Farbe zu Fabtabelle löschen
                    colorTableContentsForRemoval.deleteFromDB(getProject(), true);
                }

                // Beim Start vom Importer wird bereits immer eine Transaktion gestartet -> Commit und neue Transaktion starten
                getProject().getDbLayer().commit();
                getProject().getDbLayer().startTransaction();
            } catch (RuntimeException e) {
                messageLog.fireMessage(translateForLog("!!Fehler beim Löschen der Farbtabelle \"%1\".",
                                                       currentData.partData.partNo),
                                       MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                throw e;
            }
        }
    }

    /**
     * Erzeugt aus der PRIMUS Teilenummer und dem ES2 Schlüssel eine PSEUDO Farbtabelle, sowie die Verknüpfungen Teil zu Farbtabelle und Farbtabelle zu Inhalt
     *
     * @param part
     */
    private GenericEtkDataObjectList<EtkDataObject> createPrimusColortable(EtkDataPart part) {
        GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved = new GenericEtkDataObjectList<>();

        // ES1 muss leer sein und ES2 darf nicht leer sein
        if (!StrUtils.isEmpty(currentData.partData.es1) || StrUtils.isEmpty(currentData.partData.es2)) {
            return dataObjectsToBeSaved;
        }

        // Theoretisch besteht die Variantentabellennummer aus Teilenummer und ES1. Aktuell sollen laut Herrn Nimz aber
        // nur dann Variantentabellen angelegt werden, wenn der ES1 leer ist, weil die Variantentabellen in iParts
        // ansonsten sowieso nie angezeigt werden könnten (in der Stückliste ist nur die Teilenummer ohne ES1 drin)
        // -> Deswegen oben die Abfrage auf einen leeren ES1
        String colorTableNumber = currentData.partData.partNo + currentData.partData.es1;
        iPartsColorTableDataId colorTableId = new iPartsColorTableDataId(colorTableNumber);
        iPartsDataColorTableData colorTableData = new iPartsDataColorTableData(getProject(), colorTableId);

        // Farbtabelle
        if (!colorTableData.existsInDB()) {
            colorTableData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            colorTableData.setFieldValue(FIELD_DCTD_SOURCE, iPartsImportDataOrigin.PRIMUS.getOrigin(), DBActionOrigin.FROM_EDIT);
            // Sachnummernbenennung als Farbtabellenbenennung nutzen
            colorTableData.setFieldValueAsMultiLanguage(FIELD_DCTD_DESC, part.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR), DBActionOrigin.FROM_EDIT);
            dataObjectsToBeSaved.add(colorTableData, DBActionOrigin.FROM_EDIT);
        }

        // Verbindung Teil zu Farbtabelle
        iPartsColorTableToPartId colorTablePartId = new iPartsColorTableToPartId(colorTableId.getColorTableId(), "", "");
        iPartsDataColorTableToPart colorTableToPart = new iPartsDataColorTableToPart(getProject(), colorTablePartId);
        if (!colorTableToPart.existsInDB()) {
            colorTableToPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            colorTableToPart.setFieldValue(FIELD_DCTP_SOURCE, iPartsImportDataOrigin.PRIMUS.getOrigin(), DBActionOrigin.FROM_EDIT);
            colorTableToPart.setFieldValue(FIELD_DCTP_PART, colorTableNumber, DBActionOrigin.FROM_EDIT); // referenzierte Teilenummer entspricht der colorTableNumber
            colorTableToPart.setFieldValue(FIELD_DCTP_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            dataObjectsToBeSaved.add(colorTableToPart, DBActionOrigin.FROM_EDIT);
        }

        // Verbindung Inhalt zu Farbtabelle
        iPartsDataColorTableContentList contentList = iPartsDataColorTableContentList.loadColorTableContentListForColorTable(getProject(), colorTableId);
        boolean colorNumberAlreadyExist = false;
        // Überprüfen, ob der Farbtabelleninhalt mit der angegebene Farbnummer (ES2) schon existiert
        for (iPartsDataColorTableContent colorTableContent : contentList) {
            if (colorTableContent.getColorNumber().equals(currentData.partData.es2)) {
                colorNumberAlreadyExist = true;
                break;
            }
        }

        if (!colorNumberAlreadyExist) {
            // Weil zu einer Pseudo-Farbtabelle mehrere Farbnummern existieren können (und die Farbnummer nicht Teil des Schlüssels ist),
            // muss hier eine Positionsnummer generiert und eine freie gefunden werden
            int pos = contentList.size() + 1;
            boolean colorContentAlreadyExists = true;
            iPartsDataColorTableContent colorTableContent = null;
            while (colorContentAlreadyExists) {
                iPartsColorTableContentId colorTableContentId = new iPartsColorTableContentId(colorTableNumber, makePositionValueForContent(pos), "");
                colorTableContent = new iPartsDataColorTableContent(getProject(), colorTableContentId);
                colorContentAlreadyExists = colorTableContent.existsInDB();
                pos++;
            }
            colorTableContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            colorTableContent.setFieldValue(FIELD_DCTC_SOURCE, iPartsImportDataOrigin.PRIMUS.getOrigin(), DBActionOrigin.FROM_EDIT);
            colorTableContent.setFieldValue(FIELD_DCTC_COLOR_VAR, currentData.partData.es2, DBActionOrigin.FROM_EDIT);
            colorTableContent.setFieldValue(FIELD_DCTC_CODE, ";", DBActionOrigin.FROM_EDIT);
            colorTableContent.setFieldValue(FIELD_DCTC_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            dataObjectsToBeSaved.add(colorTableContent, DBActionOrigin.FROM_EDIT);
        }
        return dataObjectsToBeSaved;
    }

    private String makePositionValueForContent(int pos) {
        return StrUtils.prefixStringWithCharsUpToLength(String.valueOf(pos), '0', 4);
    }

    @Override
    protected String handleValueOfSpecialField(String sourceField, String value) {
        value = value.trim();

        if (sourceField.equals(MSG_REMAN)) {
            value = SQLStringConvert.booleanToPPString(value.equals("J"));
        } else if (sourceField.equals(MSG_TECH_INFO)) {
            value = SQLStringConvert.booleanToPPString(value.startsWith("SS"));
        } else if (sourceField.equals(MSG_SECURITY)) {
            value = SQLStringConvert.booleanToPPString(value.equals("J"));
        } else if (sourceField.equals(MSG_CERTIFICATION)) {
            value = SQLStringConvert.booleanToPPString(value.equals("J"));
        } else if (sourceField.equals(MSG_THEFT_REL)) {
            value = SQLStringConvert.booleanToPPString(value.equals("J"));
        } else if (sourceField.equals(MSG_THEFT_REL_INFO)) {
            if (StrUtils.isEmpty(value) || value.equals("000")) {
                // Leer und '000' soll laut Hr Nimz auf N geändert werden
                value = "N";
            }
        } else if (sourceField.equals(MSG_FDOK_REL)) {
            value = SQLStringConvert.booleanToPPString(value.equals("J"));
        } else if (sourceField.equals(MSG_ESD_IND)) {
            value = SQLStringConvert.booleanToPPString(value.equals("J"));
        } else if (sourceField.equals(MSG_QUANTUNIT)) {
            if (StrUtils.isValid(value)) {
                value = StrUtils.leftFill(value, 2, '0');
            }
        }

        return value;
    }


    /**
     * Wir haben hier kein DataObject, deshalb muss diese Methode etwas anders laufen als sonst
     *
     * @param data
     * @param tagPath
     * @param tagContent
     * @return
     */
    public boolean fillOverrideCompleteData(PrimusPartData data, String tagPath, String tagContent) {
        boolean result = false;
        // über alle Mapping Felder
        for (Map.Entry<String, String> mappingForField : mapping.entrySet()) {
            String dbDestFieldName = mappingForField.getKey();
            String importFieldName = mappingForField.getValue();
            if (tagPath.equals(importFieldName)) {
                tagContent = handleValueOfSpecialField(importFieldName, tagContent);
                data.attributes.addField(dbDestFieldName, tagContent, DBActionOrigin.FROM_DB);
                result = true;
            }
        }
        return result;
    }

}
