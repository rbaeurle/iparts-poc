/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.db.DBOracleDatabase;
import de.docware.framework.modules.db.DatabaseType;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.StringTokenizer;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.sql.SQLResultSet;
import de.docware.util.sql.SQLStatement;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Dialog, um SQL Performance Tests basierend auf SQL Skripten durchzuführen.
 */
public class SQLPerformanceTestForm {

    private static final String DB_BATCH_STATEMENT_START = "DBBatchStatement_START";
    private static final String DB_BATCH_STATEMENT_END = "DBBatchStatement_END";
    private static final String PERFORMANCE_TEST_START = "PerformanceTest_START";
    private static final String PERFORMANCE_TEST_END = "PerformanceTest_END";

    private EtkProject project;
    private String dboNameInScript;

    /**
     * Erzeugt eine Instanz von SQLPerformanceTestForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public SQLPerformanceTestForm(EtkProject project) {
        this.project = project;
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        DWFile sqlPerformanceTestsDir = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_SQL_PERFORMANCE_TESTS_DIR);
        List<DWFile> sqlScripts = sqlPerformanceTestsDir.listDWFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return DWFile.extractExtension(name, false).equalsIgnoreCase("sql");
            }
        });
        String[] sqlScriptNames = new String[sqlScripts.size()];
        int index = 0;
        for (DWFile sqlScript : sqlScripts) {
            String sqlScriptName = sqlScript.extractFileName(false);
            mainWindow.sqlScriptsList.addItem(sqlScript, sqlScriptName);
            sqlScriptNames[index] = sqlScriptName;
            index++;
        }
        mainWindow.sqlScriptsList.setSelectedItems(sqlScriptNames);
        mainWindow.sqlScriptsList.setContextMenu(sqlScriptsListContextMenu);

        mainWindow.pack();

        // Der Kopieren-Kontextmenüeintrag wird nicht auf synchron umgestellt, da es an dieser Stelle sinnvoll ist
        // dass der Kopieren-Dialog angezeigt wird, weil man hier die konkreten SQL Statemenents sieht.
    }

    public void show() {
        mainWindow.showModal();
    }

    private void sqlScriptsChanged(Event e) {
        mainWindow.startButton.setEnabled(mainWindow.sqlScriptsList.getSelectedIndices().length > 0);
    }

    private void startSQLPerformanceTests(Event e) {
        EtkMessageLogForm messageLogForm = new EtkMessageLogForm(mainWindow.title.getTitle(), "!!Testergebnisse der SQL Skripte", null);
        final EtkMessageLog messageLog = messageLogForm.getMessageLog();
        messageLogForm.showModal(new FrameworkRunnable() {

            private boolean batchStatementStarted;

            @Override
            public void run(FrameworkThread thread) {
                DecimalFormat millisDecimalFormat = new DecimalFormat("#,###,##0.0000");
                List<DWFile> selectedSqlScriptFiles = mainWindow.sqlScriptsList.getSelectedUserObjects();
                int repeatCount = mainWindow.repeatSpinner.getValue();
                int maxProgress = selectedSqlScriptFiles.size() * repeatCount;
                int currentSqlScriptIndex = 0;

                // DBO aus EtkProject bestimmen
                String dboNameInProjectWithDot = "";
                if (project.getDB().getDatabaseType(MAIN) == DatabaseType.ORACLE) {
                    dboNameInProjectWithDot = ((DBOracleDatabase)project.getDB().getDBForDomain(MAIN)).getOracleDBO() + ".";
                }

                // Header vom Log
                messageLog.fireMessage(TranslationHandler.translate("!!Datenbank:") + " " + project.getDB().getDatabaseId());
                messageLog.fireMessage("");
                messageLog.fireMessage(TranslationHandler.translate("!!SQL Skripte:"));
                for (DWFile sqlScriptFile : selectedSqlScriptFiles) {
                    messageLog.fireMessage("- " + sqlScriptFile.extractFileName(false));
                }

                messageLog.fireMessage("");
                messageLog.fireMessage(TranslationHandler.translate("!!Anzahl Wiederholungen:") + " " + String.valueOf(repeatCount));
                messageLog.fireMessage(TranslationHandler.translate("!!SQL Batch Statements:") + " "
                                       + TranslationHandler.translate(mainWindow.batchStatementsCheckbox.isSelected() ? "!!Ja" : "!!Nein"));
                messageLog.fireMessage(TranslationKeys.LINE_SEPARATOR);

                // Alle ausgewählten SQL Skripte ausführen
                for (DWFile sqlScriptFile : selectedSqlScriptFiles) {
                    if (Thread.currentThread().isInterrupted()) {
                        messageLog.fireMessage("!!Thread wurde frühzeitig beendet", MessageLogType.tmlError);
                        return;
                    }

                    messageLog.fireMessage("");
                    messageLog.fireMessage(TranslationHandler.translate("!!SQL Skript:") + " " + sqlScriptFile.extractFileName(false));
                    messageLog.fireProgress(currentSqlScriptIndex * repeatCount, maxProgress, "", false, true);

                    // SQL Skript einlesen
                    try {
                        String sqlScript = sqlScriptFile.readTextFile(DWFileCoding.UTF8);
                        List<String> sqlScriptLines = StrUtils.toStringList(sqlScript, "\n", false, true);
                        if (!sqlScriptLines.isEmpty()) {
                            // Kommentar aus der ersten Zeile
                            String comment = sqlScriptLines.get(0);
                            if (comment.startsWith("--")) {
                                comment = StrUtils.removeFirstCharacterIfCharacterIs(comment, "--").trim();
                                messageLog.fireMessage(TranslationHandler.translate("!!Kommentar:") + " " + comment);
                            }

                            // SQL Statements aufsplitten in PreTest, Test und PostTest
                            List<String> preTestStatements = new ArrayList<String>();
                            List<String> testStatements = new ArrayList<String>();
                            List<String> postTestStatements = new ArrayList<String>();

                            boolean preTest = true;
                            boolean postTest = false;
                            for (String sqlScriptLine : sqlScriptLines) {
                                // Steuer-Kommentare auswerten
                                if (sqlScriptLine.startsWith("--")) {
                                    String sqlScriptLineComment = sqlScriptLine.substring(2).trim();
                                    sqlScriptLine = null;
                                    if (sqlScriptLineComment.equalsIgnoreCase(DB_BATCH_STATEMENT_START)) {
                                        sqlScriptLine = DB_BATCH_STATEMENT_START;
                                    } else if (sqlScriptLineComment.equalsIgnoreCase(DB_BATCH_STATEMENT_END)) {
                                        sqlScriptLine = DB_BATCH_STATEMENT_END;
                                    } else if (sqlScriptLineComment.equalsIgnoreCase(PERFORMANCE_TEST_START)) {
                                        preTest = false;
                                        postTest = false;
                                    } else if (sqlScriptLineComment.equalsIgnoreCase(PERFORMANCE_TEST_END)) {
                                        preTest = false;
                                        postTest = true;
                                    }
                                } else if (sqlScriptLine.trim().equalsIgnoreCase("set define off")) { // set define off entfernen
                                    sqlScriptLine = null;
                                }

                                if (sqlScriptLine != null) { // normale SQL Statements bzw. Batch Statement-Befehle
                                    List<String> sqlStatements = splitSqlScriptLine(sqlScriptLine, new String[]{ "insert into", "update", "delete from" }, 0);
                                    for (String sqlStatement : sqlStatements) {
                                        // DBO aus dem Skript durch DBO vom EtkProject ersetzen
                                        if (dboNameInScript != null) {
                                            sqlStatement = sqlStatement.replace(" " + dboNameInScript + ".", " " + dboNameInProjectWithDot);
                                        }

                                        // SQL Statement zur entsprechenden Liste hinzufügen
                                        if (preTest) {
                                            preTestStatements.add(sqlStatement);
                                        } else if (!postTest) {
                                            testStatements.add(sqlStatement);
                                        } else {
                                            postTestStatements.add(sqlStatement);
                                        }
                                    }
                                }
                            }

                            // SQL Skript überspringen bei fehlenden Steuerkommentaren
                            if (preTest) {
                                messageLog.fireMessage(TranslationHandler.translate("!!Ungültiges SQL Performance Skript! Es fehlt folgender Steuerkommentar: -- %1", PERFORMANCE_TEST_START));
                                continue;
                            } else if (!postTest) {
                                messageLog.fireMessage(TranslationHandler.translate("!!Ungültiges SQL Performance Skript! Es fehlt folgender Steuerkommentar: -- %1", PERFORMANCE_TEST_END));
                                continue;
                            }

                            // SQL Performance Test repeatCount mal durchführen
                            long testDurationMin = Long.MAX_VALUE;
                            long testDurationAverage = 0;
                            long testDurationMax = 0;
                            for (int repeatIndex = 0; repeatIndex < repeatCount; repeatIndex++) {
                                if (Thread.currentThread().isInterrupted()) {
                                    messageLog.fireMessage("!!Thread wurde frühzeitig beendet", MessageLogType.tmlError);
                                    return;
                                }

                                project.getDbLayer().startTransaction();
                                try {
                                    // PreTest SQL Statements ausführen
                                    for (String preTestStatement : preTestStatements) {
                                        executeSqlStatement(preTestStatement);
                                    }

                                    long startTime = System.nanoTime();
                                    for (String testStatement : testStatements) {
                                        executeSqlStatement(testStatement);
                                    }
                                    long endTime = System.nanoTime();

                                    // Minimale, durchschnittliche und maximale Testdauer berechnen
                                    long testDuration = endTime - startTime;
                                    testDurationMin = Math.min(testDurationMin, testDuration);
                                    testDurationAverage = (testDurationAverage * repeatIndex + testDuration) / (repeatIndex + 1);
                                    testDurationMax = Math.max(testDurationMax, testDuration);

                                    for (String postTestStatement : postTestStatements) {
                                        executeSqlStatement(postTestStatement);
                                    }
                                } finally {
                                    project.getDB().cancelBatchStatement();
                                    project.getDbLayer().rollback();
                                }
                                messageLog.fireProgress(currentSqlScriptIndex * repeatCount + repeatIndex + 1, maxProgress, "", false, true);
                            }

                            messageLog.fireMessage("");
                            messageLog.fireMessage(TranslationHandler.translate("!!Minimale Testdauer:") + " "
                                                   + millisDecimalFormat.format(testDurationMin / 1000.0d / 1000.0d) + " ms");
                            messageLog.fireMessage(TranslationHandler.translate("!!Durchschnittliche Testdauer:") + " "
                                                   + millisDecimalFormat.format(testDurationAverage / 1000.0d / 1000.0d) + " ms");
                            messageLog.fireMessage(TranslationHandler.translate("!!Maximale Testdauer:") + " "
                                                   + millisDecimalFormat.format(testDurationMax / 1000.0d / 1000.0d) + " ms");
                            messageLog.fireMessage("");
                            messageLog.fireMessage(TranslationKeys.LINE_SEPARATOR);
                            currentSqlScriptIndex++;
                        }
                    } catch (SQLPerformanceScriptException ex) {
                        messageLog.fireMessage(ex.getMessage(), MessageLogType.tmlError); // Kein Stacktrace
                        messageLog.fireMessage("");
                        messageLog.fireMessage("!!Abbruch");
                        return;
                    } catch (Exception ex) {
                        messageLog.fireMessage(Utils.exceptionToString(ex, false), MessageLogType.tmlError);
                        messageLog.fireMessage("");
                        messageLog.fireMessage("!!Abbruch");
                        return;
                    }
                }

                messageLog.fireMessage("");
                messageLog.fireMessage("!!Fertig");
            }

            private List<String> splitSqlScriptLine(String sqlScriptLine, String[] splitForSqlStatements, int splitForSqlStatementsIndex) {
                String splitForSqlStatement = splitForSqlStatements[splitForSqlStatementsIndex] + " ";
                splitForSqlStatementsIndex++;

                // Mehrere SQL Statements "splitForSqlStatement" in einer Zeile aufplsitten in einzelne Zeilen
                List<String> splittedSqlScriptLine = new DwList<String>();
                if (sqlScriptLine.contains(splitForSqlStatement)) {
                    // StrUtils.toStringList() taugt hier nicht, weil der Delimiter (splitForSqlStatement) erhalten bleiben muss
                    StringTokenizer tokenizer = new StringTokenizer(sqlScriptLine, splitForSqlStatement, true, false);
                    boolean prependDelimiter = false; // Flag, ob der Delimiter (splitForSqlStatement) beim nächsten Token vorne angefügt werden muss
                    while (tokenizer.hasMoreTokens()) {
                        String splittedLine = tokenizer.nextToken();
                        if (splittedLine.equals(splitForSqlStatement)) {
                            prependDelimiter = true;
                        } else {
                            splittedLine = splittedLine.trim();
                            if (prependDelimiter) {
                                // DBO bestimmen
                                String dbo = StrUtils.stringUpToCharacter(splittedLine, '.');
                                if (!dbo.isEmpty()) {
                                    if ((dboNameInScript != null) && !dboNameInScript.equals(dbo)) {
                                        throw new SQLPerformanceScriptException(TranslationHandler.translate("!!SQL Performance Skript enthält verschiedene DBOs: %1 und %2", dboNameInScript, dbo));
                                    }
                                    dboNameInScript = dbo;
                                }

                                splittedLine = splitForSqlStatement + splittedLine;
                                prependDelimiter = false;
                            }

                            if (!splittedLine.isEmpty()) {
                                splittedLine = StrUtils.removeLastCharacterIfCharacterIs(splittedLine, ";");
                                if (splitForSqlStatementsIndex < splitForSqlStatements.length) { // nächstes splitForSqlStatement überprüfen?
                                    splittedSqlScriptLine.addAll(splitSqlScriptLine(splittedLine, splitForSqlStatements, splitForSqlStatementsIndex));
                                } else {
                                    splittedSqlScriptLine.add(splittedLine);
                                }
                            }
                        }
                    }
                } else {
                    // DBO bestimmen für SQL select
                    if (sqlScriptLine.startsWith("select ")) {
                        String dbo = StrUtils.stringAfterCharacter(sqlScriptLine, " from ");
                        if (!dbo.isEmpty()) {
                            dbo = StrUtils.stringUpToCharacter(dbo, '.');
                            if (!dbo.isEmpty()) {
                                if ((dboNameInScript != null) && !dboNameInScript.equals(dbo)) {
                                    throw new SQLPerformanceScriptException(TranslationHandler.translate("!!SQL Performance Skript enthält verschiedene DBOs: %1 und %2", dboNameInScript, dbo));
                                }
                                dboNameInScript = dbo;
                            }
                        }
                    }

                    if (!sqlScriptLine.isEmpty()) {
                        sqlScriptLine = StrUtils.removeLastCharacterIfCharacterIs(sqlScriptLine, ";");
                        if (splitForSqlStatementsIndex < splitForSqlStatements.length) { // nächstes splitForSqlStatement überprüfen?
                            splittedSqlScriptLine.addAll(splitSqlScriptLine(sqlScriptLine, splitForSqlStatements, splitForSqlStatementsIndex));
                        } else {
                            splittedSqlScriptLine.add(sqlScriptLine);
                        }
                    }
                }
                return splittedSqlScriptLine;
            }

            private void executeSqlStatement(String sqlStatement) {
                if (sqlStatement.equals(DB_BATCH_STATEMENT_START)) {
                    if (mainWindow.batchStatementsCheckbox.isSelected()) {
                        if (batchStatementStarted) {
                            throw new SQLPerformanceScriptException(TranslationHandler.translate("!!Ungültiges SQL Performance Skript! Es fehlt folgender Steuerkommentar: -- %1", DB_BATCH_STATEMENT_END));
                        }
                        batchStatementStarted = true;
                        project.getDB().startBatchStatement();
                    }
                } else if (sqlStatement.equals(DB_BATCH_STATEMENT_END)) {
                    if (mainWindow.batchStatementsCheckbox.isSelected()) {
                        if (!batchStatementStarted) {
                            throw new SQLPerformanceScriptException(TranslationHandler.translate("!!Ungültiges SQL Performance Skript! Es fehlt folgender Steuerkommentar: -- %1", DB_BATCH_STATEMENT_START));
                        }
                        batchStatementStarted = false;
                        project.getDB().endBatchStatement();
                    }
                } else { // normales SQL Statement
                    boolean isSelectStatement = sqlStatement.startsWith("select ");
                    if (batchStatementStarted && !isSelectStatement && (project.getDB().getDBForDomain(MAIN).getBatchStatement() != null)) { // keine SQL selects in Batch-Statements aufnehmen
                        project.getDB().getDBForDomain(MAIN).getBatchStatement().addStatement(sqlStatement, null);
                    } else { // SQL Statement ausführen
                        SQLStatement statement = null;
                        try {
                            statement = project.getDB().getDBForDomain(MAIN).getNewStatement();
                            int statementIndex = statement.prepareStatement(sqlStatement);
                            if (!isSelectStatement) {
                                statement.executeUpdate(statementIndex, null);
                            } else {
                                if (batchStatementStarted) { // sicherheitshalber executeBatchStatement() wie bei echten Batch-Statements aufrufen
                                    project.getDB().executeBatchStatement(true);
                                }

                                // Bei SELECT-Statements für realistischere Werte das ResultSet tatsächlich abfragen
                                SQLResultSet resultSet = null;
                                try {
                                    resultSet = statement.executeQuery(statementIndex, null);
                                    ResultSetMetaData metaData = resultSet.getResultSet().getMetaData();

                                    StringBuffer dummyStringBuffer = new StringBuffer(); // damit die Schleife nicht komplett wegoptimiert wird
                                    while (resultSet.next()) {
                                        for (int i = 0; i < metaData.getColumnCount(); i++) {
                                            Object object = resultSet.getObject(i + 1);
                                            dummyStringBuffer.append(object);
                                        }
                                        dummyStringBuffer.setLength(0);
                                    }
                                } finally {
                                    if (resultSet != null) {
                                        try {
                                            resultSet.close();
                                        } catch (SQLException e) {
                                        }
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            Logger.getLogger().throwRuntimeException(e);
                        } finally {
                            if (statement != null) {
                                statement.release();
                            }
                        }
                    }
                }
            }
        });
    }

    private void copySelectedSqlScripts(Event e) {
        List<DWFile> selectedSqlScripts = mainWindow.sqlScriptsList.getSelectedUserObjects();
        StringBuffer sqlScriptsContent = new StringBuffer();
        for (int i = 0; i < selectedSqlScripts.size(); i++) {
            DWFile selectedSqlScript = selectedSqlScripts.get(i);
            sqlScriptsContent.append("-- ");
            sqlScriptsContent.append(TranslationHandler.translate("!!SQL Skript:"));
            sqlScriptsContent.append(" ");
            sqlScriptsContent.append(selectedSqlScript.getAbsolutePath());
            sqlScriptsContent.append("\n\n");
            try {
                sqlScriptsContent.append(selectedSqlScript.readTextFile(DWFileCoding.UTF8));
            } catch (IOException ex) {
                sqlScriptsContent.append(ex.getMessage());
            }

            if (i < selectedSqlScripts.size() - 1) {
                sqlScriptsContent.append('\n');
                sqlScriptsContent.append(TranslationKeys.LINE_SEPARATOR);
                sqlScriptsContent.append("\n\n");
            }
        }
        FrameworkUtils.toClipboard(sqlScriptsContent.toString());
    }

    private static class SQLPerformanceScriptException extends RuntimeException {

        private SQLPerformanceScriptException(String message) {
            super(message);
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu sqlScriptsListContextMenu;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiMenuItem copyMenuItem;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel sqlScriptsLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane sqlScriptsScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiList<de.docware.util.file.DWFile> sqlScriptsList;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel repeatLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner repeatSpinner;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton startButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel batchStatementsLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox batchStatementsCheckbox;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            sqlScriptsListContextMenu = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            sqlScriptsListContextMenu.setName("sqlScriptsListContextMenu");
            sqlScriptsListContextMenu.__internal_setGenerationDpi(96);
            sqlScriptsListContextMenu.registerTranslationHandler(translationHandler);
            sqlScriptsListContextMenu.setScaleForResolution(true);
            sqlScriptsListContextMenu.setMinimumWidth(10);
            sqlScriptsListContextMenu.setMinimumHeight(10);
            copyMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            copyMenuItem.setName("copyMenuItem");
            copyMenuItem.__internal_setGenerationDpi(96);
            copyMenuItem.registerTranslationHandler(translationHandler);
            copyMenuItem.setScaleForResolution(true);
            copyMenuItem.setMnemonicEnabled(true);
            copyMenuItem.setText("!!Kopieren");
            copyMenuItem.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToClipboard"));
            copyMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    copySelectedSqlScripts(event);
                }
            });
            sqlScriptsListContextMenu.addChild(copyMenuItem);
            sqlScriptsListContextMenu.setMenuName("sqlScriptsListContextMenu");
            sqlScriptsListContextMenu.setParentControl(this);
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setMinimumWidth(400);
            this.setMinimumHeight(300);
            this.setVisible(false);
            this.setWidth(400);
            this.setHeight(300);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("!!SQL Performance Tests");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            mainPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanel.setName("mainPanel");
            mainPanel.__internal_setGenerationDpi(96);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            mainPanel.setMinimumWidth(10);
            mainPanel.setMinimumHeight(10);
            mainPanel.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutGridBag mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mainPanelLayout.setCentered(false);
            mainPanel.setLayout(mainPanelLayout);
            sqlScriptsLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            sqlScriptsLabel.setName("sqlScriptsLabel");
            sqlScriptsLabel.__internal_setGenerationDpi(96);
            sqlScriptsLabel.registerTranslationHandler(translationHandler);
            sqlScriptsLabel.setScaleForResolution(true);
            sqlScriptsLabel.setMinimumWidth(10);
            sqlScriptsLabel.setMinimumHeight(10);
            sqlScriptsLabel.setText("!!SQL Skripte:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sqlScriptsLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "ne", "n", 0, 0, 8, 8);
            sqlScriptsLabel.setConstraints(sqlScriptsLabelConstraints);
            mainPanel.addChild(sqlScriptsLabel);
            sqlScriptsScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            sqlScriptsScrollPane.setName("sqlScriptsScrollPane");
            sqlScriptsScrollPane.__internal_setGenerationDpi(96);
            sqlScriptsScrollPane.registerTranslationHandler(translationHandler);
            sqlScriptsScrollPane.setScaleForResolution(true);
            sqlScriptsScrollPane.setMinimumWidth(400);
            sqlScriptsScrollPane.setMinimumHeight(200);
            sqlScriptsList = new de.docware.framework.modules.gui.controls.GuiList<de.docware.util.file.DWFile>();
            sqlScriptsList.setName("sqlScriptsList");
            sqlScriptsList.__internal_setGenerationDpi(96);
            sqlScriptsList.registerTranslationHandler(translationHandler);
            sqlScriptsList.setScaleForResolution(true);
            sqlScriptsList.setMinimumWidth(100);
            sqlScriptsList.setMinimumHeight(50);
            sqlScriptsList.setMultipleSelect(true);
            sqlScriptsList.setMode(de.docware.framework.modules.gui.controls.GuiList.Mode.CHECKBOX);
            sqlScriptsList.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    sqlScriptsChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder sqlScriptsListConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            sqlScriptsList.setConstraints(sqlScriptsListConstraints);
            sqlScriptsScrollPane.addChild(sqlScriptsList);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sqlScriptsScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 8, 0);
            sqlScriptsScrollPane.setConstraints(sqlScriptsScrollPaneConstraints);
            mainPanel.addChild(sqlScriptsScrollPane);
            repeatLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            repeatLabel.setName("repeatLabel");
            repeatLabel.__internal_setGenerationDpi(96);
            repeatLabel.registerTranslationHandler(translationHandler);
            repeatLabel.setScaleForResolution(true);
            repeatLabel.setMinimumWidth(10);
            repeatLabel.setMinimumHeight(10);
            repeatLabel.setText("!!Anzahl Wiederholungen:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag repeatLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 0, 0, 8, 8);
            repeatLabel.setConstraints(repeatLabelConstraints);
            mainPanel.addChild(repeatLabel);
            repeatSpinner = new de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner();
            repeatSpinner.setName("repeatSpinner");
            repeatSpinner.__internal_setGenerationDpi(96);
            repeatSpinner.registerTranslationHandler(translationHandler);
            repeatSpinner.setScaleForResolution(true);
            repeatSpinner.setMinimumWidth(10);
            repeatSpinner.setMinimumHeight(10);
            repeatSpinner.setMinValue(1);
            repeatSpinner.setMaxValue(100000);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag repeatSpinnerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 8, 0);
            repeatSpinner.setConstraints(repeatSpinnerConstraints);
            mainPanel.addChild(repeatSpinner);
            startButton = new de.docware.framework.modules.gui.controls.GuiButton();
            startButton.setName("startButton");
            startButton.__internal_setGenerationDpi(96);
            startButton.registerTranslationHandler(translationHandler);
            startButton.setScaleForResolution(true);
            startButton.setMinimumWidth(100);
            startButton.setMinimumHeight(10);
            startButton.setEnabled(false);
            startButton.setMnemonicEnabled(true);
            startButton.setText("!!Start");
            startButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    startSQLPerformanceTests(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag startButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 0.0, 0.0, "w", "n", 0, 0, 8, 0);
            startButton.setConstraints(startButtonConstraints);
            mainPanel.addChild(startButton);
            batchStatementsLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            batchStatementsLabel.setName("batchStatementsLabel");
            batchStatementsLabel.__internal_setGenerationDpi(96);
            batchStatementsLabel.registerTranslationHandler(translationHandler);
            batchStatementsLabel.setScaleForResolution(true);
            batchStatementsLabel.setMinimumWidth(10);
            batchStatementsLabel.setMinimumHeight(10);
            batchStatementsLabel.setText("!!SQL Batch Statements:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag batchStatementsLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 0, 0, 8, 8);
            batchStatementsLabel.setConstraints(batchStatementsLabelConstraints);
            mainPanel.addChild(batchStatementsLabel);
            batchStatementsCheckbox = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            batchStatementsCheckbox.setName("batchStatementsCheckbox");
            batchStatementsCheckbox.__internal_setGenerationDpi(96);
            batchStatementsCheckbox.registerTranslationHandler(translationHandler);
            batchStatementsCheckbox.setScaleForResolution(true);
            batchStatementsCheckbox.setMinimumWidth(10);
            batchStatementsCheckbox.setMinimumHeight(10);
            batchStatementsCheckbox.setSelected(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag batchStatementsCheckboxConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "w", "n", 0, 0, 8, 0);
            batchStatementsCheckbox.setConstraints(batchStatementsCheckboxConstraints);
            mainPanel.addChild(batchStatementsCheckbox);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            this.addChild(mainPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}