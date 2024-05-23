/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.psk_partlist;

import de.docware.apps.etk.base.importer.base.model.ArrayFileImporter;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstrakte Klasse für PSK-Import. Mit Überprüfung der Kopfzeile, Zeilenweises Lesen der Datei und
 * Aufruf von importRecord() (analog zu den Importern)
 */
public abstract class AbstractPskReader {

    private ArrayFileImporter importer;
    private List<String> mustHeaderList;
    private List<String> optionalHeaderList;

    public AbstractPskReader() {

    }

    protected boolean open(DWFile file) {
        importer = new ArrayFileImporter();
        return importer.open(file.getPath());
    }

    protected int getRowCount() {
        return importer.getRowCount();
    }

    protected int getColCount() {
        return importer.getColCount();
    }

    public boolean readFile(DWFile file, String[] headerEntries) {
        return readFile(file, headerEntries, new String[]{});
    }

    /**
     * Hauptroutine zum Lesen der Excel-Datei.
     * Öffnet die Excel-Datei, sucht nach der Kopfzeile (@param headerEntries)
     * und ruft, wenn gefunden, für jede Zeile importRecord() auf
     *
     * @param file
     * @param headerEntries
     * @return
     */
    public boolean readFile(DWFile file, String[] headerEntries, String[] optionalEntries) {
        if (!open(file)) {
            showError("!!Kann Datei %1 nicht öffnen", file.getName());
            return false;
        }
        try {
            mustHeaderList = new DwList(headerEntries);
            optionalHeaderList = new DwList<>(optionalEntries);
            // Header erwartet -> also beginne bei 0
            int iRow = 0;
            int maxRow = importer.getRowCount();
            boolean headerFound = false;
            Map<String, Integer> headerNameToIndex = null;

            for (int row = iRow; row < maxRow; row++) {
                if (isEmptyLine(row)) {
                    // Alle Zeilen leer => mach weiter
                    continue;
                }
                if (!headerFound) {
                    headerNameToIndex = readHeaderLine(row);
                    headerFound = !headerNameToIndex.isEmpty();
                } else {
                    Map<String, String> data = readCompleteLine(headerNameToIndex, row);
                    if (!data.isEmpty()) {
                        importRecord(data, row);
                    }
                }
                doProgress(row, maxRow);
            }
            doProgress(maxRow, maxRow);
            if (!headerFound) {
                showError("!!Keine Kopfzeile gefunden.");
                return false;
            }
        } finally {
            doHideProgress();
        }
        return true;
    }

    protected abstract void importRecord(Map<String, String> importRec, int recordNo);

    /**
     * Eine Zeile, nach den Vorgaben aus der Kopfzeile lesen
     *
     * @param headerNameToIndex
     * @param row
     * @return
     */
    protected Map<String, String> readCompleteLine(Map<String, Integer> headerNameToIndex, int row) {
        Map<String, String> data = new HashMap<>();
        if (headerNameToIndex != null) {
            for (String key : mustHeaderList) {
                Integer index = headerNameToIndex.get(key);
                if ((index != null) && (index != -1)) {
                    String value = importer.getAt(index, row);
                    data.put(key, value);
                } else {
                    data.put(key, "");
                }
            }
            if (!optionalHeaderList.isEmpty()) {
                for (String key : optionalHeaderList) {
                    Integer index = headerNameToIndex.get(key);
                    if ((index != null) && (index != -1)) {
                        String value = importer.getAt(index, row);
                        data.put(key, value);
                    } else {
                        data.put(key, "");
                    }
                }
            }
        }
        return data;
    }

    /**
     * Suche nach der Kopfzeile: Lesen der gesamten Zeile und Überprüfung der Kopfzeilen-Einträge
     *
     * @param row
     * @return
     */
    protected Map<String, Integer> readHeaderLine(int row) {
        Map<String, Integer> headerNameToIndex = new HashMap<>();
        for (int col = 0; col < importer.getColCount(); col++) {
            headerNameToIndex.put(importer.getAt(col, row), col);
        }
        // checkHeader
        if (!checkHeader(headerNameToIndex)) {
            headerNameToIndex.clear();
        }
        return headerNameToIndex;
    }

    /**
     * Überprüfung der gelesenen Zeile, ob die Einträge der Kopfzeile vorhanden sind
     *
     * @param headerNameToIndex
     * @return
     */
    protected boolean checkHeader(Map<String, Integer> headerNameToIndex) {
        Set<String> currentValues = headerNameToIndex.keySet();
        if (currentValues.size() >= mustHeaderList.size()) {
            for (String currentValue : currentValues) {
                if (!mustHeaderList.contains(currentValue) && !optionalHeaderList.contains(currentValue)) {
                    headerNameToIndex.put(currentValue, -1);
                }
            }
            boolean isValid = false;
            for (Integer col : headerNameToIndex.values()) {
                if (col != -1) {
                    isValid = true;
                    break;
                }
            }
            if (isValid) {
                StringBuilder str = new StringBuilder();
                for (String hdr : mustHeaderList) {
                    Integer col = headerNameToIndex.get(hdr);
                    if ((col == null) || (col == -1)) {
                        if (str.length() > 0) {
                            str.append(", ");
                        }
                        str.append(hdr);
                    }
                }
                if (str.length() > 0) {
                    str.insert(0, TranslationHandler.translate("!!Folgende Einträge fehlen im Header:") + " (");
                    str.append(")");
                    showWarning(str.toString());
                }
            }
            return isValid;
        }
        return false;
    }

    /**
     * Überprüfung, ob der gesamte Zeile leer ist
     *
     * @param row
     * @return
     */
    protected boolean isEmptyLine(int row) {
        boolean allEmpty = true;
        for (int col = 0; col < importer.getColCount(); col++) {
            if (!importer.getAt(col, row).isEmpty()) {
                allEmpty = false;
                break;
            }
        }
        return allEmpty;
    }

    protected boolean isAlphaNumeric(String value) {
        if (StrUtils.isEmpty(value)) {
            return true;
        }
        for (int lfdNr = 0; lfdNr < value.length(); lfdNr++) {
            char ch = value.charAt(lfdNr);
            if (Character.isLetterOrDigit(ch)) {
                // .isLetter(ch) lässt Umlaute etc durch, deswegen nochmal gezielt abgefragt
                if (!(((ch >= '0') && (ch <= '9')) || ((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z')))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }


    protected abstract void showMessage(String key, String... placeHolderTexts);

    protected abstract void showWarning(String key, String... placeHolderTexts);

    protected abstract void showError(String key, String... placeHolderTexts);

    protected abstract void doProgress(int pos, int maxPos);

    protected abstract void doHideProgress();

}
