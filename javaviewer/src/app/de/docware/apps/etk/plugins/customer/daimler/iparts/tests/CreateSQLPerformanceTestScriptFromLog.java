/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests;

import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hilfsklasse, um aus einem Log mit SQL-Einträgen inkl. Batch-Statements ein SQL-Script für Performance-Tests zu erzeugen.
 * Die Aufzeichnung sollte mit demselben DB-Typ (z.B. Oracle) erfolgen wie es später für das SQL-Script notwendig ist.
 */
public class CreateSQLPerformanceTestScriptFromLog {

    private static String fileName = "DAIMLER-2168";
    private static DWFile baseDir = DWFile.get("\\\\s-db2\\projekte\\Daimler_Develop\\Performance");

    public static void main(String[] args) {
        if (args.length > 0) {
            String fileNameWithDir = args[0];
            fileName = DWFile.extractFileName(fileNameWithDir, false);
            fileName = StrUtils.removeLastCharacterIfCharacterIs(fileName, "_log");
            baseDir = DWFile.get(fileNameWithDir).getParentDWFile();
        }

        DWFile file = baseDir.getChild(fileName + "_log.txt");
        try {
            List<String> lines = file.readTextFileToStringList();
            List<String> filteredLines = new ArrayList<String>();
            filteredLines.add("-- PerformanceTest_START");
            for (String line : lines) {
                String[] splittedLines = null;
                if (line.contains("####### SQL ######:") && !line.contains("where 1=2")) {
                    line = StrUtils.stringAfterLastCharacter(line, "#: ");

                    // begin und end von Oracle entfernen
                    line = StrUtils.removeFirstCharacterIfCharacterIs(line, "begin ");
                    line = StrUtils.removeLastCharacterIfCharacterIs(line, "; end;");

                    splittedLines = StrUtils.toStringArray(line, "; ");
                    for (int i = 0; i < splittedLines.length; i++) {
                        splittedLines[i] = splittedLines[i] + ";";
                    }
                } else if (line.contains("Starting new DBBatchStatement")) {
                    splittedLines = new String[1];
                    splittedLines[0] = "-- DBBatchStatement_START";
                } else if (line.contains("DBBatchStatement executed for batch")) {
                    splittedLines = new String[2];
                    splittedLines[0] = "-- DBBatchStatement_END";
                    splittedLines[1] = "-- DBBatchStatement_START";
                } else if (line.contains("DBBatchStatement executed and finished")) {
                    splittedLines = new String[1];
                    splittedLines[0] = "-- DBBatchStatement_END";
                }
                if (splittedLines != null) {
                    for (String splittedLine : splittedLines) {
                        filteredLines.add(splittedLine);
                    }
                }
            }
            filteredLines.add("-- PerformanceTest_END");
            file = baseDir.getChild(fileName + "_generated.sql");
            file.writeTextFileFromStringList(filteredLines);
        } catch (IOException e) {
            Logger.getLogger().throwRuntimeException(e);
        }
    }
}
