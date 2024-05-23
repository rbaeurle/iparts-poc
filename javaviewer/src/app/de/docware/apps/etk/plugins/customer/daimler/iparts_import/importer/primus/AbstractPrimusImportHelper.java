/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.primus;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.iPartsXMLPartImportTags;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPrimusImportHelper extends iPartsMainImportHelper implements iPartsXMLPartImportTags {

    protected static class PrimusBasicPartData {

        String partNo = "";  // Grundsachnummer = Materialnummer ohne ES1 und/oder ES2
        String es1 = "";
        String es2 = "";
    }

    protected EtkMessageLog messageLog;
    protected String messageLogLanguage;
    private Map<String, PrimusAction> actionMapping;

    protected AbstractPrimusImportHelper(EtkProject project, EtkMessageLog messageLog, String messageLogLanguage,
                                         Map<String, String> mapping, String tableName) {
        super(project, mapping, tableName);
        this.messageLog = messageLog;
        this.messageLogLanguage = messageLogLanguage;
        this.actionMapping = new HashMap<>();
        initActionMapping(actionMapping);
    }


    protected void setBasicPartFieldValues(EtkDataPart part, PrimusBasicPartData partData) {
        // Nur initialisieren, falls das Teil noch nicht da ist.
        if (!part.existsInDB()) {
            part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            // Verarbeite mögliche ES1 und/oder ES2 Schlüssel an der Teilenummer
            DIALOGImportHelper.handleESKeysInDataPart(getProject(), part, messageLog, messageLogLanguage);
        }

        // Source
        part.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.PRIMUS.getOrigin(), DBActionOrigin.FROM_EDIT);

        // Zur Sicherheit: Bestellnummer immer setzen
        part.setFieldValue(FIELD_M_BESTNR, part.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);

        part.setFieldValueAsBoolean(FIELD_M_IS_DELETED, false, DBActionOrigin.FROM_EDIT);

        // DAIMLER-14238, Sonderbehandlung für SMART, bei 18-stelligen Q-Sachnummern ES1, ES2 und die M_BASE_MATNR gezielt leer setzen!
        // Alle anderen, möglichen Längen 13/14/15/19 der Q-Sachnummern werden nicht korrigiert!
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        String partNo = part.getAsId().getMatNr();
        // "Q + V + 18 Zeichen"-Prüfung
        if (numberHelper.isSMARTPrintedPartNo(partNo)) {
            part.setFieldValue(FIELD_M_BASE_MATNR, "", DBActionOrigin.FROM_EDIT);
            part.setFieldValue(FIELD_M_AS_ES_1, "", DBActionOrigin.FROM_EDIT);
            part.setFieldValue(FIELD_M_AS_ES_2, "", DBActionOrigin.FROM_EDIT);
        } else {
            part.setFieldValue(FIELD_M_BASE_MATNR, partData.partNo, DBActionOrigin.FROM_EDIT);
            part.setFieldValue(FIELD_M_AS_ES_1, partData.es1, DBActionOrigin.FROM_EDIT);
            part.setFieldValue(FIELD_M_AS_ES_2, partData.es2, DBActionOrigin.FROM_EDIT);
        }
    }

    protected EtkDataPart createDataPart(PrimusBasicPartData partData) {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        PartId partId = new PartId(numberHelper.getPRIMUSPartNoWithEs1AndEs2(partData.partNo,
                                                                             partData.es1,
                                                                             partData.es2), "");
        return EtkDataObjectFactory.createDataPart(getProject(), partId);
    }

    /**
     * Erzeugt ein {@link EtkDataPart} Objekt samt optionalen ES1 und ES2 Schlüssel
     *
     * @param partNumber
     * @return
     */
    protected EtkDataPart createPartFromPrimusPartNo(String partNumber, boolean fillBasicData) {
        PrimusBasicPartData partData = new PrimusBasicPartData();
        if (!formatPrimusPartNo(partNumber, partData)) {
            return null;
        }

        EtkDataPart part = createDataPart(partData);
        if (fillBasicData && !part.existsInDB()) {
            setBasicPartFieldValues(part, partData);
        }
        return part;
    }

    protected boolean formatPrimusPartNo(String memoryPartNo, PrimusBasicPartData partData) {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        if (!numberHelper.isPRIMUSPartNoMemoryFormatValid(memoryPartNo, true)) {
            messageLog.fireMessage(translateForLog("!!Sachnummer \"%1\" liegt nicht wie erwartet im Speicherformat vor",
                                                   memoryPartNo),
                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return false;
        } else {
            String printPartNo = numberHelper.convertPRIMUSPartNoMemoryToPrint(memoryPartNo);
            if (!numberHelper.isPRIMUSPartNoPrintFormatValid(printPartNo, true)) {
                messageLog.fireMessage(translateForLog("!!Fehler bei der Konvertierung der Sachnummer (\"%1\" nach \"%2\")",
                                                       memoryPartNo, printPartNo),
                                       MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                return false;
            } else if (numberHelper.isSMARTPartNo(printPartNo)) {
                // Sonderbehandlung für SMART, nur ES2:
                partData.es2 = numberHelper.getES2FromSMARTPrintedPartNo(printPartNo);
                partData.partNo = numberHelper.getSMARTBasePartNo(printPartNo);
                return true;
            } else {
                // Normale PRIMUS-Teile mit ES1 + ES2
                partData.es1 = numberHelper.getPRIMUSEs1FromPrintPartNo(printPartNo);
                partData.es2 = numberHelper.getPRIMUSEs2FromPrintPartNo(printPartNo);
                partData.partNo = numberHelper.getPRIMUSPartNoFromPrintPartNo(printPartNo);
                return true;
            }
        }
    }

    /**
     * Liefert den übergebenen Übersetzungsschlüssel für die Logsprache zurück inkl. optionaler Platzhaltertexte.
     *
     * @param translationsKey
     * @param placeHolderTexts
     * @return
     */
    protected String translateForLog(String translationsKey, String... placeHolderTexts) {
        return TranslationHandler.translateForLanguage(translationsKey, messageLogLanguage, placeHolderTexts);
    }

    private Map<String, PrimusAction> getActionMapping() {
        return actionMapping;
    }

    public PrimusAction getCurrentAction(Map<String, String> currentRecord) {
        return getActionMapping().get(currentRecord.get(MSG_ACTION));
    }

    protected boolean isValidActionKey(String keyValue) {
        return getActionMapping().containsKey(keyValue);
    }

    @Override
    protected String handleValueOfSpecialField(String sourceField, String value) {
        value = value.trim();

        if (sourceField.equals(MSG_TIMESTAMP)) {
            try {
                if ((value.length() >= 3) && (value.charAt(value.length() - 3) == ':')) {
                    // Fehlerhaftes Datumsformat korrigieren (: in ISO-Zeitzone zwischen Stunden und Minuten)
                    value = value.substring(0, value.length() - 3) + value.substring(value.length() - 2);
                }
                value = SQLStringConvert.calendarToPPDateTimeString(DateUtils.toCalendar_ISODateMillisTimeZone(value));
            } catch (Exception e) {
                messageLog.fireMessage(translateForLog("!!Ungültiger Zeitstempel \"%1\" für XML-Tag \"%2\"",
                                                       value, sourceField),
                                       MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                value = "";
            }
        }

        return value;
    }

    protected boolean checkIfTagExists(Map<String, String> currentRecord, String tag) {
        if (!currentRecord.containsKey(tag)) {
            messageLog.fireMessage(translateForLog("!!Erwarteter Tag \"%1\" wurde nicht gefunden.", tag),
                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return false;
        }
        return true;
    }

    /**
     * Initialisiert das Mapping von String des <ACT>-Tags aus dem XML auf die {@link PrimusAction}, für die der String steht.
     */
    protected abstract void initActionMapping(Map<String, PrimusAction> actionMapping);

    /**
     * Verarbeitet einen einzelnen XML-Tag
     *
     * @param tagPath       XML-Pfad des Tags
     * @param tagContent    Inhalt des Tags
     * @param tagCount      Anzahl der bereits gelesenen Tags
     * @param currentRecord Map mit dem XML-Pfad der bis jetzt gelesenen Tags auf deren Inhalt.
     */
    protected abstract boolean handleCurrentTag(String tagPath, String tagContent, int tagCount, Map<String, String> currentRecord);

    /**
     * Verarbeitet einen Record, nachdem dieser komplett eingelesen wurde
     *
     * @param currentRecord
     * @return Alle Datenobjekte die während der Verarbeitung entstanden sind, {@code null}, falls es einen Fehler gab.
     */
    protected abstract GenericEtkDataObjectList<EtkDataObject> handleRecord(Map<String, String> currentRecord);
}
