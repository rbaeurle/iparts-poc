/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBExtendedDataTypeProvider;
import de.docware.framework.modules.db.serialization.*;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.os.OsUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hilfsklasse für die Erstellung von Änderungs-Logs von Änderungssets ({@link iPartsRevisionChangeSet}).
 */
public class iPartsRevisionsLogger {

    /**
     * Erzeugt für das übergebene {@link iPartsRevisionChangeSet} ein Änderungs-Log.
     *
     * @param logTitle
     * @param changeSet
     * @param historyData Flag, ob die Daten aus dem Feld {@code DCE_CURRENT_DATA} im Änderungs-Log angezeigt werden sollen
     *                    oder die Änderungen aus dem Feld {@code DCE_HISTORY_DATA}.
     * @param merged      Flag, ob das Änderungs-Log zusammengeführte Änderungen anzeigen soll oder jede einzelne historische
     *                    Änderung. Wird nur ausgewertet falls {@code historyData == true}.
     * @param project
     * @return
     */
    public static String createRevisionChangeSetLog(String logTitle, iPartsRevisionChangeSet changeSet, boolean historyData,
                                                    boolean merged, EtkProject project) {
        StringBuffer stringBuffer = new StringBuffer();
        if (StrUtils.isValid(logTitle)) {
            stringBuffer.append(TranslationHandler.translate(logTitle) + OsUtils.NEWLINE);
        } else {
            stringBuffer.append(TranslationHandler.translate("!!Änderungsset: %1", changeSet.getChangeSetId().getGUID())
                                + OsUtils.NEWLINE);
        }

        if (historyData) {
            for (SerializedDBDataObjectHistory<SerializedDBDataObject> historyEntry : changeSet.loadHistoryFromDB().values()) {
                if (merged) {
                    logChangeSetEntry(historyEntry.mergeSerializedDBDataObject(true, null), stringBuffer, project);
                } else {
                    for (SerializedDBDataObject serializedDBDataObject : historyEntry.getHistory()) {
                        logChangeSetEntry(serializedDBDataObject, stringBuffer, project);
                    }
                }
            }
        } else {
            for (SerializedDBDataObject serializedDBDataObject : changeSet.getSerializedDataObjectsMap().values()) {
                logChangeSetEntry(serializedDBDataObject, stringBuffer, project);
            }
        }
        return stringBuffer.toString();
    }

    public static void logChangeSetEntry(SerializedDBDataObject serializedDBDataObject, StringBuffer stringBuffer, EtkProject project) {
        if (serializedDBDataObject == null) {
            return;
        }

        if (stringBuffer.length() != 0) {
            stringBuffer.append(OsUtils.NEWLINE);
            stringBuffer.append(TranslationKeys.LINE_SEPARATOR + OsUtils.NEWLINE);
            stringBuffer.append(OsUtils.NEWLINE);
        }

        String dateTime = serializedDBDataObject.getDateTime();
        if (StrUtils.isValid(dateTime)) {
            DateConfig dateConfig = DateConfig.getInstance(project.getConfig());
            dateTime = dateConfig.formatDateTime(project.getDBLanguage(), dateTime);
        }
        stringBuffer.append(TranslationHandler.translate("!!Änderungen von Benutzer \"%1\" am %2:",
                                                         serializedDBDataObject.getUserIdWithFallback(), dateTime) + OsUtils.NEWLINE);
        logSerializedDBDataObject(serializedDBDataObject, "", stringBuffer, serializedDBDataObject.getUserIdWithFallback(),
                                  serializedDBDataObject.getDateTime(), project);
    }

    /**
     * Loggt das übergebene {@link SerializedDBDataObject} in den übergebenen {@link StringBuffer} mit der angegebenen Einrückung.
     *
     * @param serializedDBDataObject
     * @param indent
     * @param stringBuffer
     * @param parentUserId           Benutzer vom Vater-{@link SerializedDBDataObject}
     * @param parentDateTime         Änderungsdatum vom Vater-{@link SerializedDBDataObject}
     * @param project
     */
    public static void logSerializedDBDataObject(SerializedDBDataObject serializedDBDataObject, String indent, StringBuffer stringBuffer,
                                                 String parentUserId, String parentDateTime, EtkProject project) {
        // Innerhalb vom Änderungsset neu erzeugte und wieder gelöschte DBDataObjects inkl. Kind-DBDataObjects sind irrelevant
        if (serializedDBDataObject.isRevertedWithoutKeepState()) {
            return;
        }

        String tableName = serializedDBDataObject.getTableName();
        String tableString = tableName;
        EtkDatabaseTable table = project.getConfig().getDBDescription().findTable(tableName);
        if (table != null) {
            String tableDescription = table.getTableDescription();
            if (!tableDescription.isEmpty() && !tableDescription.equals(tableString)) {
                tableString = "\"" + TranslationHandler.translate(tableDescription) + "\" (" + tableName + ")";
            }
        }

        String primaryKeys = "?";
        String[] pkValues = serializedDBDataObject.getPkValues();
        if (pkValues != null) {
            primaryKeys = StrUtils.stringArrayToString(", ", pkValues);
        }

        // Wenn sich der Benutzer oder das Änderungsdatum vom Vater-SerializedDBDataObject unterscheiden, diese loggen
        String dataObjectUserIdAndDateTime;
        String dataObjectUserId = serializedDBDataObject.getUserIdWithFallback();
        String dataObjectDateTime = serializedDBDataObject.getDateTime();
        DateConfig dateConfig = null;
        String dbLanguage = project.getDBLanguage();
        if ((StrUtils.isValid(dataObjectUserId) && !Utils.objectEquals(parentUserId, dataObjectUserId))
            || (StrUtils.isValid(dataObjectDateTime) && !Utils.objectEquals(parentDateTime, dataObjectDateTime))) {
            String dateTime = dataObjectDateTime;
            if (!dateTime.isEmpty()) {
                if (dateConfig == null) {
                    dateConfig = DateConfig.getInstance(project.getConfig());
                }
                dateTime = dateConfig.formatDateTime(dbLanguage, dateTime);
            }

            dataObjectUserIdAndDateTime = " - " + TranslationHandler.translate("!!Benutzer \"%1\" am %2", dataObjectUserId,
                                                                               dateTime);
        } else {
            dataObjectUserIdAndDateTime = "";
        }

        stringBuffer.append(indent + TranslationHandler.translate("!!* Datensatz vom Typ \"%1\" für Tabelle %2 mit Primärschlüssel \"%3\"%4",
                                                                  serializedDBDataObject.getType(), tableString, primaryKeys,
                                                                  dataObjectUserIdAndDateTime)
                            + OsUtils.NEWLINE);
        String dataObjectIndent = indent + "  ";
        String[] oldPkValues = serializedDBDataObject.getOldPkValues();
        boolean isPrimaryKeyChanged = serializedDBDataObject.arePkValuesChanged();

        // Zustand loggen
        String state;
        String valueTranslationKey;
        String textIdTranslationKey;
        String blobTranslationKey;
        String arrayTranslationKey;
        switch (serializedDBDataObject.getState()) {
            case NEW:
                if (isPrimaryKeyChanged) {
                    state = "!!neu mit Primärschlüsseländerung";
                    valueTranslationKey = "!!+ Neuer Wert: %1";
                    textIdTranslationKey = "!!+ Neue Text-ID: %1";
                    blobTranslationKey = "!!+ Neue Binärdaten";
                    arrayTranslationKey = "!!+ Neues Array: %1";
                } else {
                    state = "!!neu";
                    valueTranslationKey = "!!* Wert: %1";
                    textIdTranslationKey = "!!* Text-ID: %1";
                    blobTranslationKey = "!!* Binärdaten";
                    arrayTranslationKey = "!!* Array: %1";
                }
                break;
            case REPLACED: // Fallthrough ist hier korrekt, da identische Behandlung
            case COMMITTED: // Fallthrough ist hier korrekt, da fast identische Behandlung
            case MODIFIED:
                if (serializedDBDataObject.getState() == SerializedDBDataObjectState.COMMITTED) {
                    if (isPrimaryKeyChanged) {
                        state = "!!bereits gespeichert mit Primärschlüsseländerung";
                    } else {
                        state = "!!bereits gespeichert";
                    }
                } else {
                    if (isPrimaryKeyChanged) {
                        state = "!!geändert mit Primärschlüsseländerung";
                    } else {
                        state = "!!geändert";
                    }
                }
                valueTranslationKey = "!!+ Neuer Wert: %1";
                textIdTranslationKey = "!!+ Neue Text-ID: %1";
                blobTranslationKey = "!!+ Neue Binärdaten";
                arrayTranslationKey = "!!+ Neues Array: %1";
                break;
            case DELETED_COMMITTED: // Fallthrough ist hier korrekt, da fast identische Behandlung
            case DELETED:
                if (serializedDBDataObject.getState() == SerializedDBDataObjectState.DELETED_COMMITTED) {
                    state = "!!bereits gelöscht";
                } else {
                    state = "!!gelöscht";
                }
                valueTranslationKey = "!!- Gelöschter Wert: %1";
                textIdTranslationKey = "!!- Gelöschte Text-ID: %1";
                blobTranslationKey = "!!- Gelöschte Binärdaten";
                arrayTranslationKey = "!!- Gelöschtes Array: %1";
                break;
            case REVERTED:
                state = "!!wieder gelöscht";
                valueTranslationKey = "!!- Gelöschter Wert: %1";
                textIdTranslationKey = "!!- Gelöschte Text-ID: %1";
                blobTranslationKey = "!!- Gelöschte Binärdaten";
                arrayTranslationKey = "!!- Gelöschtes Array: %1";
                break;
            default:
                state = "!!unverändert";
                valueTranslationKey = "!!* Wert: %1";
                textIdTranslationKey = "!!* Text-ID: %1";
                blobTranslationKey = "!!* Binärdaten";
                arrayTranslationKey = "!!* Array: %1";
                break;
        }

        stringBuffer.append(dataObjectIndent + TranslationHandler.translate("!!* Zustand: %1", TranslationHandler.translate(state))
                            + OsUtils.NEWLINE);

        // Alten Primärschlüssel loggen
        if (isPrimaryKeyChanged) {
            stringBuffer.append(dataObjectIndent + TranslationHandler.translate("!!- Alter Primärschlüssel: %1", StrUtils.stringArrayToString(", ", oldPkValues))
                                + OsUtils.NEWLINE);
        }

        // Attribute loggen
        Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
        if (attributes != null) {
            String attributeIndent = dataObjectIndent + "  ";

            for (SerializedDBDataObjectAttribute attribute : attributes) {
                String fieldName = attribute.getName();
                if (fieldName.equals(DBConst.FIELD_STAMP)) { // Zeitstempel ignorieren
                    continue;
                }

                // Wenn sich der Benutzer oder das Änderungsdatum vom SerializedDBDataObject unterscheiden, diese loggen
                String attributeUserIdAndDateTime;
                String attributeUserId = attribute.getUserId();
                String attributeDateTime = attribute.getDateTime();
                if ((StrUtils.isValid(attributeUserId) && !Utils.objectEquals(dataObjectUserId, attributeUserId))
                    || (StrUtils.isValid(attributeDateTime) && !Utils.objectEquals(dataObjectDateTime, attributeDateTime))) {
                    if (!attributeDateTime.isEmpty()) {
                        if (dateConfig == null) {
                            dateConfig = DateConfig.getInstance(project.getConfig());
                        }
                        attributeDateTime = dateConfig.formatDateTime(dbLanguage, attributeDateTime);
                    }

                    attributeUserIdAndDateTime = " - " + TranslationHandler.translate("!!Benutzer \"%1\" am %2", attributeUserId,
                                                                                      attributeDateTime);
                } else {
                    attributeUserIdAndDateTime = "";
                }

                String fieldString = fieldName;
                if (table != null) {
                    EtkDatabaseField field = table.getField(fieldName);
                    if (field != null) {
                        String fieldDescription = field.getDisplayName();
                        EtkMultiSprache userDescription = field.getUserDescription();
                        if ((userDescription != null) && !userDescription.isEmpty()) {
                            fieldDescription = userDescription.getTextByNearestLanguage(dbLanguage, project.getDataBaseFallbackLanguages());
                        }
                        if (StrUtils.isValid(fieldDescription)) {
                            fieldString = TranslationHandler.translate(fieldDescription) + " (" + fieldName + ")";
                        }
                    }
                }
                stringBuffer.append(dataObjectIndent + TranslationHandler.translate("!!# Feld: %1", fieldString) + attributeUserIdAndDateTime
                                    + (attribute.isNotModified() ? " " + TranslationHandler.translate("!!(nicht vom Benutzer verändert)") : "")
                                    + OsUtils.NEWLINE);

                DBDataObjectAttribute.TYPE type = attribute.getType();
                if (type == null) {
                    type = DBDataObjectAttribute.TYPE.STRING;
                }
                switch (type) {
                    case STRING:
                        String value = attribute.getValue();
                        if (value == null) {
                            value = "";
                        }
                        value = project.getVisObject().asText(tableName, fieldName, value, dbLanguage);
                        stringBuffer.append(attributeIndent + TranslationHandler.translate(valueTranslationKey, value)
                                            + OsUtils.NEWLINE);

                        String oldValue = attribute.getOldValue();
                        if (oldValue != null) {
                            oldValue = project.getVisObject().asText(tableName, fieldName, oldValue, dbLanguage);
                            stringBuffer.append(attributeIndent + TranslationHandler.translate("!!- Alter Wert: %1", oldValue)
                                                + OsUtils.NEWLINE);
                        }
                        break;
                    case MULTI_LANGUAGE:
                        // Die Textnummer interessiert eigentlich keinen...
//                        String textNr = attribute.getValue();
//                        if (textNr == null) {
//                            textNr = "";
//                        }
//
//                        String oldTextNr = attribute.getOldValue();
//                        if (!Utils.objectEquals(textNr, oldTextNr)) {
//                            stringBuffer.append(attributeIndent + TranslationHandler.translate("!!+ Neue Textnummer: %1", textNr)
//                                                + OsUtils.NEWLINE);
//                            if (oldTextNr != null) {
//                                stringBuffer.append(attributeIndent + TranslationHandler.translate("!!- Alte Textnummer: %1", oldTextNr)
//                                                    + OsUtils.NEWLINE);
//                            }
//                        }

                        logSerializedMultiLanguageDiff(attribute.getMultiLanguage(), attribute.getOldMultiLanguage(), attributeIndent + "  ",
                                                       textIdTranslationKey, isPrimaryKeyChanged, stringBuffer, project);
                        break;
                    case BLOB:
                        stringBuffer.append(attributeIndent + TranslationHandler.translate(blobTranslationKey)
                                            + OsUtils.NEWLINE);

                        oldValue = attribute.getOldValue();
                        if (oldValue != null) {
                            stringBuffer.append(attributeIndent + TranslationHandler.translate("!!- Alte Binärdaten vorhanden")
                                                + OsUtils.NEWLINE);
                        }
                        break;
                    case ARRAY:
                        String arrayIndent = attributeIndent + "  ";
                        String arrayId = attribute.getValue();
                        if (arrayId == null) {
                            arrayId = "";
                        }
                        stringBuffer.append(attributeIndent + TranslationHandler.translate(arrayTranslationKey, arrayId)
                                            + OsUtils.NEWLINE);
                        logSerializedDataArray(attribute.getArray(), arrayIndent, stringBuffer);

                        String oldArrayId = attribute.getOldValue();

                        // Array-ID hat sich nicht geändert, es gibt aber alte Array-Werte -> oldArrayId auf arrayId setzen
                        if ((oldArrayId == null) && (attribute.getOldArray() != null)) {
                            oldArrayId = arrayId;
                        }

                        if (oldArrayId != null) {
                            stringBuffer.append(attributeIndent + TranslationHandler.translate("!!- Altes Array: %1", oldArrayId)
                                                + OsUtils.NEWLINE);
                            logSerializedDataArray(attribute.getOldArray(), arrayIndent, stringBuffer);
                        }
                        break;
                }
            }
        }

        // Kind-SerializedDBDataObjects loggen
        List<SerializedDBDataObjectList<SerializedDBDataObject>> compositeChildren = serializedDBDataObject.getCompositeChildren();
        if (compositeChildren != null) {
            String childIndent = dataObjectIndent + "  ";
            for (SerializedDBDataObjectList compositeChild : compositeChildren) {
                stringBuffer.append(dataObjectIndent + TranslationHandler.translate("!!* Untergeordnete Datensätze für \"%1\":",
                                                                                    compositeChild.getChildName()) + OsUtils.NEWLINE);

                List<SerializedDBDataObject> childList = compositeChild.getList();
                if (childList != null) {
                    for (SerializedDBDataObject childSerializedDBDataObject : childList) {
                        logSerializedDBDataObject(childSerializedDBDataObject, childIndent, stringBuffer, dataObjectUserId,
                                                  dataObjectDateTime, project);
                    }
                }
            }
        }
    }

    private static void logSerializedMultiLanguageDiff(SerializedEtkMultiSprache serializedMultiLanguage, SerializedEtkMultiSprache oldSerializedMultiLanguage,
                                                       String indent, String textIdTranslationKey, boolean isPrimaryKeyChanged,
                                                       StringBuffer stringBuffer, EtkProject project) {
        DBExtendedDataTypeProvider extendedDataTypeProviderForTextIds = EtkDataObject.getExtendedDataTypeProviderForTextIds(project);
        EtkMultiSprache multiLanguage = serializedMultiLanguage.createMultiLanguage(extendedDataTypeProviderForTextIds);

        EtkMultiSprache oldMultiLanguage = null;
        if (oldSerializedMultiLanguage != null) {
            oldMultiLanguage = oldSerializedMultiLanguage.createMultiLanguage(extendedDataTypeProviderForTextIds);
        }

        // Text-ID loggen falls diese sich von der alten Text-ID unterscheidet bzw. es keine alte EtkMultiSprache gibt
        boolean isOldTextIdDifferent = (oldMultiLanguage != null) && !Utils.objectEquals(multiLanguage.getTextId(), oldMultiLanguage.getTextId());
        if (isOldTextIdDifferent || ((oldMultiLanguage == null) && StrUtils.isValid(multiLanguage.getTextId()))) {
            stringBuffer.append(indent + TranslationHandler.translate(textIdTranslationKey, multiLanguage.getTextId())
                                + OsUtils.NEWLINE);
        }

        // Alte Text-ID loggen falls diese sich von der neuen Text-ID unterscheidet
        if (isOldTextIdDifferent) {
            stringBuffer.append(indent + TranslationHandler.translate("!!- Alte Text-ID: %1", oldMultiLanguage.getTextId())
                                + OsUtils.NEWLINE);
        }

        if (serializedMultiLanguage.isAlreadyExistsInDB()) {
            stringBuffer.append(indent + "  " + TranslationHandler.translate("!!(Text existiert bereits in der Datenbank)")
                                + OsUtils.NEWLINE);
        }

        String textsIndent = indent + "  ";

        // Neue und veränderte Texte bestimmen
        Map<String, String> newLanguagesAndTexts = new LinkedHashMap<>();
        Map<String, String> modifiedLanguagesAndTexts = new LinkedHashMap<>();
        for (Map.Entry<String, String> languageAndText : multiLanguage.getLanguagesAndTexts().entrySet()) {
            String languageCode = languageAndText.getKey();
            String text = languageAndText.getValue();

            // Vergleich vom Text mit dem alten Text (sofern vorhanden)
            if (oldMultiLanguage != null) {
                if (oldMultiLanguage.spracheExists(languageCode)) {
                    if (!oldMultiLanguage.getText(languageCode).equals(text)) {
                        modifiedLanguagesAndTexts.put(languageCode, text);
                    }
                } else {
                    newLanguagesAndTexts.put(languageCode, text);
                }
            } else {
                newLanguagesAndTexts.put(languageCode, text);
            }
        }

        // Neue Texte loggen
        if (!newLanguagesAndTexts.isEmpty()) {
            String newLanguageTranslationKey;
            if (isPrimaryKeyChanged) {
                stringBuffer.append(indent + TranslationHandler.translate("!!+ Neue Texte:") + OsUtils.NEWLINE);
                newLanguageTranslationKey = "!![+] %1: %2";
            } else {
                stringBuffer.append(indent + TranslationHandler.translate("!!* Texte:") + OsUtils.NEWLINE);
                newLanguageTranslationKey = "!![*] %1: %2";
            }

            for (Map.Entry<String, String> newLanguageAndText : newLanguagesAndTexts.entrySet()) {
                logLanguageAndTextDiff(newLanguageAndText.getKey(), newLanguageAndText.getValue(), newLanguageTranslationKey,
                                       textsIndent, stringBuffer);
            }
        }

        // Veränderte Texte loggen
        if (!modifiedLanguagesAndTexts.isEmpty()) {
            stringBuffer.append(indent + TranslationHandler.translate("!!* Veränderte Texte:") + OsUtils.NEWLINE);

            for (Map.Entry<String, String> modifiedLanguageAndText : modifiedLanguagesAndTexts.entrySet()) {
                logLanguageAndTextDiff(modifiedLanguageAndText.getKey(), modifiedLanguageAndText.getValue(), "!![+] %1 neuer Text: %2",
                                       textsIndent, stringBuffer);
                logLanguageAndTextDiff(modifiedLanguageAndText.getKey(), oldMultiLanguage.getText(modifiedLanguageAndText.getKey()),
                                       "!![-] %1 alter Text: %2", textsIndent, stringBuffer);
            }
        }

        // Gelöschte Texte bestimmen
        Map<String, String> deletedLanguagesAndTexts = new LinkedHashMap<String, String>();
        if (oldMultiLanguage != null) {
            for (Map.Entry<String, String> languageAndText : oldMultiLanguage.getLanguagesAndTexts().entrySet()) {
                String languageCode = languageAndText.getKey();
                String text = languageAndText.getValue();

                // Überprüfen, ob die Sprache noch vorhanden ist
                if (!multiLanguage.spracheExists(languageCode)) {
                    deletedLanguagesAndTexts.put(languageCode, text);
                }
            }
        }

        // Gelöschte Texte loggen
        if (!deletedLanguagesAndTexts.isEmpty()) {
            stringBuffer.append(indent + TranslationHandler.translate("!!- Gelöschte Texte:") + OsUtils.NEWLINE);

            for (Map.Entry<String, String> deletedLanguageAndText : deletedLanguagesAndTexts.entrySet()) {
                logLanguageAndTextDiff(deletedLanguageAndText.getKey(), deletedLanguageAndText.getValue(), "!!- %1: %2",
                                       textsIndent, stringBuffer);
            }
        }
    }

    private static void logLanguageAndTextDiff(String languageCode, String text, String translationKey, String indent,
                                               StringBuffer stringBuffer) {
        Language language = Language.findLanguage(languageCode);
        if (language.getCode().equalsIgnoreCase(languageCode)) {
            languageCode = TranslationHandler.translate(language.getDisplayName())
                           + " (" + languageCode + ")";
        }
        stringBuffer.append(indent + TranslationHandler.translate(translationKey, languageCode, text) + OsUtils.NEWLINE);
    }

    private static void logSerializedDataArray(SerializedEtkDataArray dataArray, String indent, StringBuffer stringBuffer) {
        if (dataArray != null) {
            List<String> arrayValues = dataArray.getValues();
            if (arrayValues != null) {
                int index = 0;
                for (String arrayValue : arrayValues) {
                    stringBuffer.append(indent + TranslationHandler.translate("!![%1]: %2", String.valueOf(index),
                                                                              arrayValue) + OsUtils.NEWLINE);
                    index++;
                }
            }
        }
    }
}