/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests;

import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hilfsklasse, um ein SQL-Script für Performance-Tests an geänderte DB-Felder anzupassen.
 */
public class UpdateSQLPerformanceTestScript {

    private static String fileName = "DAIMLER-2167_Import_MAD_Teilestamm_Oracle.sql";
    private static DWFile baseDir = DWFile.get("\\\\s-daimler\\Daimler_Develop\\Performance");

    private static final int[] fieldIndicesToDelete = null;
    private static final int fieldsToAdd = 0;

    public static void main(String[] args) {
        if (args.length > 0) {
            String fileNameWithDir = args[0];
            fileName = DWFile.extractFileName(fileNameWithDir, true);
            baseDir = DWFile.get(fileNameWithDir).getParentDWFile();
        }

        DWFile file = baseDir.getChild(fileName);
        try {
            List<String> lines = file.readTextFileToStringList();
            List<String> updatedLines = new ArrayList<String>();
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("insert into ")) {
                    // Aktuell werden nur nicht mehr vorhandene Felder aus den Werten entfernt, da die Feldnamen vorne
                    // im insert-Statement bereits durch Suchen&Ersetzen korrigiert wurden
                    StringBuffer updatedLineBuffer = new StringBuffer(StrUtils.stringUpToAndIncludingCharacter(line, "values("));
                    String valuesString = StrUtils.stringAfterLastCharacter(line, "values(");
                    String[] values = StrUtils.toStringArray(valuesString, "', ", false, false);
                    for (int i = 0; i < values.length; i++) {
                        if ((fieldIndicesToDelete == null) || (Arrays.binarySearch(fieldIndicesToDelete, i) < 0)) { // Feld soll nicht gelöscht werden -> übernehmen
                            updatedLineBuffer.append(values[i]);
                            if (i < values.length - 1) { // letzter Wert endet mit "')"
                                updatedLineBuffer.append("', ");
                            }
                        }
                    }
                    String updatedLine = updatedLineBuffer.toString();

                    // Wenn das letzte Feld entfernt wurde, muss "')" (evtl. mit ;) am Ende stehen anstatt "', "
                    if (updatedLine.endsWith("', ")) {
                        updatedLine = StrUtils.stringUpToLastCharacter(updatedLine, "', ") + "')";
                        if (trimmedLine.endsWith(";")) {
                            updatedLine += ";";
                        }
                    }

                    if (fieldsToAdd > 0) {
                        boolean endsWithSemicolon = trimmedLine.endsWith(";");
                        updatedLineBuffer = new StringBuffer(updatedLine);
                        updatedLineBuffer.delete(updatedLine.length() - (endsWithSemicolon ? 2 : 1), updatedLine.length());
                        for (int i = 0; i < fieldsToAdd; i++) {
                            updatedLineBuffer.append(", ' '");
                        }
                        updatedLineBuffer.append(")");
                        if (endsWithSemicolon) {
                            updatedLineBuffer.append(";");
                        }
                        updatedLine = updatedLineBuffer.toString();
                    }

                    updatedLines.add(updatedLine);
                } else {
                    updatedLines.add(line);
                }
            }
            file = baseDir.getChild(DWFile.extractFileName(fileName, false) + "_updated.sql");
            file.writeTextFileFromStringList(updatedLines);
        } catch (IOException e) {
            Logger.getLogger().throwRuntimeException(e);
        }
    }
}
