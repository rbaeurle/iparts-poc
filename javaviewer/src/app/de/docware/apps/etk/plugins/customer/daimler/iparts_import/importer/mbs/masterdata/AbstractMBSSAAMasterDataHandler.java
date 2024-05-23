/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.AbstractMBSDataHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstrakte Klasse zum Verarbeiten von GrundStücklisten und SAA Stämme aus SAP-MBS
 */
public abstract class AbstractMBSSAAMasterDataHandler extends AbstractMBSDataHandler {

    private static final String INVALID_TRIGGER_ELEMENT = "IPARTS_INVALID_TRIGGER_ELEMENT";
    private String mainTriggerForDataBlock;
    private Map<String, iPartsDataSa> storedSas;

    public AbstractMBSSAAMasterDataHandler(EtkProject project, String mainTriggerForDataBlock, MBSDataImporter importer, String importName) {
        super(project, INVALID_TRIGGER_ELEMENT, importer, importName, TABLE_DA_SAA);
        this.mainTriggerForDataBlock = mainTriggerForDataBlock;
        this.storedSas = new HashMap<>();
    }

    @Override
    public String getMainXMLTag() {
        return mainTriggerForDataBlock;
    }

    @Override
    protected void handleCurrentRecord() {
        // Liefert die SAA Nummer von den spezifischen Unter-Handler
        String number = getNumberFromSpecificElement();
        if (!isValidAction(number)) {
            return;
        }
        // Wir speichern nur SAAs und Grundstücklisten in DA_SAA ab. Hier checken, ob die Nummer mit "Z" oder "G" anfängt
        if (!number.startsWith(SAA_NUMBER_PREFIX) && !number.startsWith(BASE_LIST_NUMBER_PREFIX)) {
            return;
        }

        // Unterschiedliche Retail-SAA-Nummer vorhanden für die Konstruktions-SAA-Nummer?
        String constructionSaaNumber = null;
        String retailSaaNumber = getImportHelper().getNumberHelper().getDifferentRetailSAA(number);
        if (retailSaaNumber != null) {
            constructionSaaNumber = number;
            number = retailSaaNumber;
        }

        // Den DA_SAA Datensatz verarbeiten
        iPartsDataSaa dataSaa = new iPartsDataSaa(getProject(), new iPartsSaaId(number));
        if (dataSaa.existsInDB()) {
            if (!isValidSource(dataSaa)) {
                handleConstructionSAAForInvalidRecord(dataSaa, constructionSaaNumber);
                return;
            }
            // Nur weitermachen, wenn ReleaseDateFrom >= DS_EDAT. Zur Sicherheit wird auch geprüft, ob das Datum ab
            // unendlich ist
            String currentEDAT = dataSaa.getFieldValue(FIELD_DS_EDAT);
            // Neues Datum ist unendlich -> weitermachen
            // Neues Datum ist nicht unendlich und bestehendes Datum ist unendlich -> herausspringen
            // Neues Datum ist nicht unendlich und bestehendes Datum ist nicht unendlich -> herausspringen, wenn bestehedes
            // Datum jünger als neues Datum
            if (StrUtils.isValid(getReleaseDateFrom()) && (StrUtils.isEmpty(currentEDAT) || (getReleaseDateFrom().compareTo(currentEDAT) < 0))) {
                handleConstructionSAAForInvalidRecord(dataSaa, constructionSaaNumber);
                return;
            }
        } else {
            dataSaa.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        }

        // Konstruktions-SAA-Nummer setzen
        if (constructionSaaNumber == null) {
            constructionSaaNumber = "";
        }
        dataSaa.setFieldValue(FIELD_DS_CONST_SAA, constructionSaaNumber, DBActionOrigin.FROM_EDIT);

        // KG und SAA Referenz extrahieren
        boolean containsConGroup = number.contains(MBS_CON_GROUP_DELIMITER);
        if (containsConGroup) {
            List<String> values = StrUtils.toStringList(number, MBS_CON_GROUP_DELIMITER, false, true);
            if (values.size() == 2) {
                dataSaa.setFieldValue(FIELD_DS_SAA_REF, values.get(0), DBActionOrigin.FROM_EDIT);
                dataSaa.setFieldValue(FIELD_DS_KG, values.get(1), DBActionOrigin.FROM_EDIT);
            } else {
                writeMessage(TranslationHandler.translate("!!KG und Referenz SAA konnten nicht extrahiert werden. " +
                                                          "Datensatz mit \"%1\" wird übersprungen!", number),
                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                // Hier nicht handleConstructionSAAForInvalidRecord() aufrufen, weil es keine gültige SAA mit KG ist
                return;
            }
        }
        getImportHelper().fillOverrideCompleteDataForSAPMBS(dataSaa, getCurrentRecord());
        dataSaa.setFieldValue(FIELD_DS_EDAT, getReleaseDateFrom(), DBActionOrigin.FROM_EDIT);
        dataSaa.setFieldValue(FIELD_DS_ADAT, getReleaseDateFrom(), DBActionOrigin.FROM_EDIT);
        dataSaa.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.SAP_MBS.getOrigin(), DBActionOrigin.FROM_EDIT);
        if (dataSaa.isModifiedWithChildren()) {
            saveDataObject(dataSaa);
        }
        // Nur SAAs ohne KG prüfen
        if (number.startsWith("Z") && !containsConGroup) {
            handleSaFromSaa(dataSaa);
        }
    }

    /**
     * Liefert die SAA/GS Nummer zum aktuellen SAA/GS XML Element
     *
     * @return
     */
    protected String getNumberFromSpecificElement() {
        return getCurrentRecord().get(getSpecificXMLElementForNumber());
    }

    private void handleConstructionSAAForInvalidRecord(iPartsDataSaa dataSaa, String constructionSaaNumber) {
        if (constructionSaaNumber != null) {
            dataSaa.setFieldValue(FIELD_DS_CONST_SAA, constructionSaaNumber, DBActionOrigin.FROM_EDIT);
            if (dataSaa.isModified()) {
                getImporter().saveToDB(dataSaa, false);
                handleConstructionSAForInvalidRecord(dataSaa);
            }
        }
    }

    private void handleConstructionSAForInvalidRecord(iPartsDataSaa dataSaa) {
        // SA aktualisieren falls diese in der DB existiert
        String saNumber = iPartsNumberHelper.convertSAAtoSANumber(dataSaa.getAsId().getSaaNumber());
        if (StrUtils.isValid(saNumber)) {
            iPartsDataSa dataSa = new iPartsDataSa(getProject(), new iPartsSaId(saNumber));
            if (dataSa.existsInDB()) {
                getImportHelper().transferConstructionSaaData(dataSaa, dataSa);
                if (dataSa.isModified()) {
                    getImporter().saveToDB(dataSa, false);
                }
            }
        }
    }

    /**
     * Verarbeitet die SA zur übergebenen SAA.
     *
     * @param dataSaa
     */
    private void handleSaFromSaa(iPartsDataSaa dataSaa) {
        String saaNumber = dataSaa.getAsId().getSaaNumber();
        // Sa DataObject auf Basis des SAA DataObjects erzeugen/laden
        iPartsDataSa dataSa = getImportHelper().extractSaFromSaa(dataSaa, iPartsImportDataOrigin.SAP_MBS);
        if (dataSa == null) {
            return;
        }
        if (isValidSource(dataSa)) {
            String saNumber = dataSa.getAsId().getSaNumber();
            // Wurde die SAA schon einmal verarbeitet, wird geprüft, ob die Texte gleich sind
            iPartsDataSa alreadyStoredSa = storedSas.get(saNumber);
            if (alreadyStoredSa != null) {
                checkIfSameText(alreadyStoredSa, dataSa, saaNumber);
                return;
            } else {
                if (storedSas.size() >= MAX_DB_OBJECTS_CACHE_SIZE) {
                    storedSas.clear();
                }
            }

            if (dataSa.isNew() || dataSa.isModifiedWithChildren()) {
                dataSa.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.SAP_MBS.getOrigin(), DBActionOrigin.FROM_EDIT);
                saveDataObject(dataSa);
                storedSas.put(saNumber, dataSa);
            }
        } else {
            handleConstructionSAForInvalidRecord(dataSaa);
        }
    }

    /**
     * Check, ob die beiden übergebenen {@link iPartsDataSa}s den gleichen Entwicklungstext haben
     *
     * @param alreadyStoredSa
     * @param dataSa
     * @param saaNumber
     * @return
     */
    private void checkIfSameText(iPartsDataSa alreadyStoredSa, iPartsDataSa dataSa, String saaNumber) {
        EtkMultiSprache existingText = alreadyStoredSa.getFieldValueAsMultiLanguage(FIELD_DS_CONST_DESC);
        EtkMultiSprache newText = dataSa.getFieldValueAsMultiLanguage(FIELD_DS_CONST_DESC);
        if (!existingText.equalContent(newText)) {
            writeMessage(TranslationHandler.translate("!!Zur SAA \"%1\" wurden unterschiedliche " +
                                                      "Entwicklungsbenennungen gefunden: \"%2\" und \"%3\"",
                                                      saaNumber, existingText.getText(getProject().getDBLanguage()),
                                                      newText.getText(getProject().getDBLanguage())),
                         MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
        }
    }


    private boolean isValidSource(EtkDataObject dataObject) {
        iPartsImportDataOrigin currentSaSource = iPartsImportDataOrigin.getTypeFromCode(dataObject.getFieldValue(FIELD_DS_SOURCE));
        // BOM-DB darf nie überschrieben werden
        return currentSaSource != iPartsImportDataOrigin.EDS;
    }

    @Override
    public void onEndDocument() {
        super.onEndDocument();
        storedSas.clear();
    }

    /**
     * Jeder Unter-Handler hat sein eigenes XML Element, in dem die Nummer steht (SAA oder Grundstückliste). Um den
     * Wert aus dem currentRecord zu erhalten wird über diese Methode das jeweilige Element bestimmt.
     *
     * @return
     */
    protected abstract String getSpecificXMLElementForNumber();

    @Override
    protected void initMapping(Map<String, String> mapping) {
        mapping.put(FIELD_DS_CONST_DESC, DESCRIPTION);
    }
}
