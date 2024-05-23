/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.text;

import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.*;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * Klasse mit allen Konverter für die DIALOG Direct Importer
 */
public class iPartsTextToDIALOGConverter {

    private static final String END_WORKER_PREFIX = "ANZAHL=";
    private static final Map<String, Map<String, TextToDIALOGValueConverter>> CONVERTER_FOR_DIALOG_TYPES = new HashMap<>();

    static {
        // DIALOG Typen und Felder bei denen die numerischen Werte nach der vierten Position getrennt werden -> DEC(7,3) -> 7 Zeichen insgesamt davon 3 Zeichen Nachkommastellen
        FieldsWithSameConverter fieldsWithSameConverter = new FieldsWithSameConverter();
        // DIALOG Typen mit einzelnen Felder
        fieldsWithSameConverter.addDialogField(PartListDataImporter.BRTE_PREFIX, iPartsTextToDIALOGMapper.addPrefix(PartListDataImporter.MG, PartListDataImporter.BRTE_PREFIX));
        fieldsWithSameConverter.addDialogField(ZBVEDataImporter.DIALOG_TABLENAME, ZBVEDataImporter.ZBVE_MG);
        fieldsWithSameConverter.addDialogField(MasterDataDialogImporter.DIALOG_TABLENAME_TS1, MasterDataDialogImporter.TS1_FGW1);
        // GEWS hat zwei Felder bei denen der Konverter angewandt werden soll
        fieldsWithSameConverter.addDialogFields(MasterDataDialogImporter.DIALOG_TABLENAME_GEWS,
                                                MasterDataDialogImporter.GEWS_PROGEW,
                                                MasterDataDialogImporter.GEWS_GEWGEW);
        addConverter(fieldsWithSameConverter, value -> convertValueWithLength(value, 7, 3));

        // DIALOG Typen und Felder bei denen die numerischen Werte nach der vierten Position getrennt werden -> DEC(3,1) -> 3 Zeichen insgesamt davon 1 Zeichen Nachkommastelle
        fieldsWithSameConverter = new FieldsWithSameConverter();
        // DIALOG Typen mit einzelnen Felder
        fieldsWithSameConverter.addDialogField(KemMasterDataImporter.DIALOG_TABLENAME, KemMasterDataImporter.KES_DAUER);
        addConverter(fieldsWithSameConverter, value -> convertValueWithLength(value, 3, 1));

        // ENDE Nachricht
        fieldsWithSameConverter = new FieldsWithSameConverter();
        fieldsWithSameConverter.addDialogField(DialogEndMessageWorker.DIALOG_TABLENAME, DialogEndMessageWorker.DIALOG_DIRECT_ANZAHL);
        addConverter(fieldsWithSameConverter, value -> {
            String result = value;
            // Präfix entfernen
            if (result.startsWith(END_WORKER_PREFIX)) {
                result = StrUtils.copySubString(result, END_WORKER_PREFIX.length(), result.length() - END_WORKER_PREFIX.length());
            }
            return result;
        });
    }


    /**
     * Fügt den aufgesammelten Feldern {@link FieldsWithSameConverter} den gleichen {@link TextToDIALOGValueConverter} hinzu
     *
     * @param fieldsWithSameConverter
     * @param converter
     */
    private static void addConverter(FieldsWithSameConverter fieldsWithSameConverter, TextToDIALOGValueConverter converter) {
        if ((fieldsWithSameConverter != null) && fieldsWithSameConverter.isValid() && (converter != null)) {
            fieldsWithSameConverter.getDialogTypesAndFields().forEach((dialogType, fieldNames) -> {
                fieldNames.forEach(fieldName -> {
                    Map<String, TextToDIALOGValueConverter> converterForFields = CONVERTER_FOR_DIALOG_TYPES.computeIfAbsent(dialogType, k -> new HashMap<>());
                    converterForFields.put(fieldName, converter);
                });
            });
        }
    }

    /**
     * Zahlenkonverter für DIALOG Direct Daten bei denen ein Zahlenwert in einen komma-separierten Wert konvertiert wird
     *
     * @param value
     * @param length       - Länge des gesamten Strings
     * @param suffixLength - Länge der Nachkommastellen
     * @return
     */
    private static String convertValueWithLength(String value, int length, int suffixLength) {
        if (StrUtils.isValid(value)) {
            if (value.length() != length) {
                return null;
            }
            if (suffixLength > length) {
                return null;
            }
            int position = length - suffixLength;
            // Builder ist nur ein Objekt auf dem die Array-Operationen durchgeführt werden
            StringBuilder builder = new StringBuilder(value);
            // Den Punkt hinzufügen
            builder.insert(position, '.');
            // Führende "0" entfernen, bis keine 0 vorne steht (es muss irgendwann der Punkt kommen)
            while ((builder.length() > 0) && (builder.charAt(0) == '0')) {
                builder.deleteCharAt(0);
            }
            // Wenn vor dem Punkt nur "0" standen, dann muss hier die "0" hinzugefügt werden
            if (builder.charAt(0) == '.') {
                builder.insert(0, '0');
            }
            return builder.toString();
        }
        return "";
    }

    public static Map<String, TextToDIALOGValueConverter> getConvertersForDIALOGType(String dialogType) {
        return CONVERTER_FOR_DIALOG_TYPES.get(dialogType);
    }

    /**
     * Hilfsklasse um Felder zu gruppieren, die den gleichen Konverter nutzen
     */
    private static class FieldsWithSameConverter {

        private final Map<String, List<String>> dialogTypesAndFields;

        public FieldsWithSameConverter() {
            this.dialogTypesAndFields = new HashMap<>();
        }

        /**
         * Füge das übergebene Feld dem übergebenen DIALOG Typ hinzu
         *
         * @param dialogType
         * @param fieldName
         */
        public void addDialogField(String dialogType, String fieldName) {
            if (StrUtils.isValid(dialogType, fieldName)) {
                List<String> fieldNamesForType = dialogTypesAndFields.computeIfAbsent(dialogType, k -> new ArrayList<>());
                fieldNamesForType.add(fieldName);
            }
        }

        /**
         * Füge die übergebenen Felder dem übergebenen DIALOG Typ hinzu
         *
         * @param dialogType
         * @param fieldNames
         */
        public void addDialogFields(String dialogType, String... fieldNames) {
            if ((fieldNames != null) && (fieldNames.length > 0)) {
                Arrays.stream(fieldNames).forEach(fieldName -> addDialogField(dialogType, fieldName));
            }
        }

        public Map<String, List<String>> getDialogTypesAndFields() {
            return dialogTypesAndFields;
        }

        public boolean isValid() {
            return !dialogTypesAndFields.isEmpty();
        }
    }


}
