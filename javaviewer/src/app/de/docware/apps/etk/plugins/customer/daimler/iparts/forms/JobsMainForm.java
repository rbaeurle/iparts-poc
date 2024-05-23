/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.about.form.AboutForm;
import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.AbstractJobsNotificationListener;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiDockingPanel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.filechooser.MultipleInputToOutputStream;
import de.docware.framework.modules.gui.controls.filechooser.filefilter.DWFileFilterEnum;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.table.*;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonType;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.file.DWInputStream;
import de.docware.util.os.OsUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hauptformular für die iParts Jobs. Statusinformation und anzeige der Logs
 */
public class JobsMainForm extends AbstractJavaViewerMainFormContainer {

    private static final int THRESHOLD_FOR_MESSAGE_LOG_FORM = 200;
    private static final String RUNNING_JOBS_TITLE = "!!Laufende Jobs";
    private static final String Running_JOBS_TITLE_LIMIT_REACHED = "!!Laufende Jobs (mehr als %1 Log-Dateien vorhanden)";
    private static final String ERROR_JOBS_TITLE = "!!Fehlerhafte Jobs";
    private static final String ERROR_JOBS_TITLE_LIMIT_REACHED = "!!Fehlerhafte Jobs (mehr als %1 Log-Dateien vorhanden)";
    private static final String PROCESSED_JOBS_TITLE = "!!Durchgeführte Jobs";
    private static final String PROCESSED_JOBS_TITLE_LIMIT_REACHED = "!!Durchgeführte Jobs (mehr als %1 Log-Dateien vorhanden)";
    // Lock Objekte für die einzelnen Tabellen
    private static final Object PROCESSED_JOBS_LOCK = new Object();
    private static final Object ERROR_JOBS_LOCK = new Object();
    private static final Object RUNNING_JOBS_LOCK = new Object();


    private final AbstractJobsNotificationListener jobsNotificationListener;
    private DateConfig dateConfig;
    private GuiTableRowWithJobLogFile selectedJobTableRow;

    /**
     * Erzeugt eine Instanz von JobsMainForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public JobsMainForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();

        jobsNotificationListener = new AbstractJobsNotificationListener() {
            @Override
            public void jobRunning(DWFile runningLogFile) {
                boolean addRunningJobLogFileToTable = true;
                // Hier ohne Form laden, da das erzeugen und setzen der Job-Logs außerhalb der JobsMainForm passiert
                if (refreshJobLogsIfNecessary(false)) {
                    // In der Tabelle der laufenden Jobs die übergebene neue Log-Datei suchen. Wenn diese nicht gefunden
                    // wird (weil sie in der Regel erst NACH dem Aufruf des jobsNotificationListener überhaupt erzeugt wird),
                    // dann muss sie ebenfalls explizit zur Tabelle der laufenden Jobs hinzugefügt werden.
                    synchronized (RUNNING_JOBS_LOCK) {
                        for (int rowNo = 0; rowNo < mainWindow.runningJobsTable.getRowCount(); rowNo++) {
                            GuiTableRow row = mainWindow.runningJobsTable.getRow(rowNo);
                            if (row instanceof GuiTableRowWithJobLogFile) {
                                if (runningLogFile.equals(((GuiTableRowWithJobLogFile)row).getJobLogFile())) {
                                    addRunningJobLogFileToTable = false;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (addRunningJobLogFileToTable) {
                    addRunningJobLogFile(runningLogFile);
                }

                StaticConnectionUpdater.updateBrowser(getRootParentWindow(parentForm));
            }

            @Override
            public void jobCancelled(DWFile runningLogFile) {
                removeRunningJobLogFile(runningLogFile);
                StaticConnectionUpdater.updateBrowser(getRootParentWindow(parentForm));
            }

            @Override
            public void jobProcessed(DWFile runningLogFile, DWFile processedLogFile) {
                if (!refreshJobLogsIfNecessary(false)) {
                    removeRunningJobLogFile(runningLogFile);
                    addProcessedJobLogFile(processedLogFile);
                }
                StaticConnectionUpdater.updateBrowser(getRootParentWindow(parentForm));
            }

            @Override
            public void jobError(DWFile runningLogFile, DWFile errorLogFile) {
                if (!refreshJobLogsIfNecessary(false)) {
                    removeRunningJobLogFile(runningLogFile);
                    addErrorJobLogFile(errorLogFile);
                }
                StaticConnectionUpdater.updateBrowser(getRootParentWindow(parentForm));
            }
        };
        iPartsJobsManager.getInstance().addJobsNotifiactionListener(jobsNotificationListener);
    }

    private GuiWindow getRootParentWindow(AbstractJavaViewerForm parentForm) {
        if (parentForm != null) {
            return parentForm.getRootParentWindow();
        } else {
            return mainWindow.getRootWindow();
        }
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.runningJobsTable.setContextMenu(runningJobsContextMenu);
        mainWindow.processedJobsTable.setContextMenu(finishedJobsContextMenu);
        mainWindow.errorJobsTable.setContextMenu(cloneFinishedJobsContextMenu());

        // Toolbar
        if (iPartsPlugin.isImportPluginActive()) {
            mainWindow.logJobsToolbar.addChild(new GuiToolButton(ToolButtonType.SEPARATOR));
            mainWindow.logJobsToolbar.addChild(de.docware.apps.etk.plugins.customer.daimler.iparts_import.forms.AutoImportSettingsForm.createAutoImportSettingsToolButton(getConnector(), this));
        }
        setHeaders();

        // Sortierung
        GuiTableColumnWithDateTimeSortConverter jobsTableColumnSortConverter = new GuiTableColumnWithDateTimeSortConverter();
        mainWindow.runningJobsTable.setColumnSortConverter(0, jobsTableColumnSortConverter);
        mainWindow.runningJobsTable.setColumnSortConverter(1, jobsTableColumnSortConverter);
        mainWindow.runningJobsTable.setColumnSortConverter(2, jobsTableColumnSortConverter);

        mainWindow.processedJobsTable.setColumnSortConverter(0, jobsTableColumnSortConverter);
        mainWindow.processedJobsTable.setColumnSortConverter(1, jobsTableColumnSortConverter);
        mainWindow.processedJobsTable.setColumnSortConverter(2, jobsTableColumnSortConverter);

        mainWindow.errorJobsTable.setColumnSortConverter(0, jobsTableColumnSortConverter);
        mainWindow.errorJobsTable.setColumnSortConverter(1, jobsTableColumnSortConverter);
        mainWindow.errorJobsTable.setColumnSortConverter(2, jobsTableColumnSortConverter);

        mainWindow.runningJobsTable.sortRowsAccordingToColumn(1, false);
        mainWindow.processedJobsTable.sortRowsAccordingToColumn(2, false);
        mainWindow.errorJobsTable.sortRowsAccordingToColumn(2, false);

        // bei der eingebetteten GUI erst bei activeFormChanged() die Job-Logs laden
        if (iPartsPlugin.isImportPluginActive()) {
            if (de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.isImportPluginStandalone()) {
                refreshAllLogsWithMessageForm();
            }
        }
        ThemeManager.get().render(mainWindow);
    }

    private void setHeaders() {
        // TableHeader
        setHeader(mainWindow.runningJobsTable);
        setHeader(mainWindow.processedJobsTable);
        setHeader(mainWindow.errorJobsTable);
    }

    private void setHeader(GuiTable jobsTable) {
        if (jobsTable == mainWindow.runningJobsTable) {
            GuiTableHeader tableHeaderForRunning = makeStartTableHeader();
            jobsTable.setHeader(tableHeaderForRunning);
        } else if ((jobsTable == mainWindow.processedJobsTable) || (jobsTable == mainWindow.errorJobsTable)) {
            GuiTableHeader tableHeaderWithEndTime = makeStartTableHeader();
            tableHeaderWithEndTime.addChild(TranslationHandler.translate("!!Ende"));
            jobsTable.setHeader(tableHeaderWithEndTime);
        }
    }

    private GuiTableHeader makeStartTableHeader() {
        GuiTableHeader tableHeader = new GuiTableHeader();
        tableHeader.addChild(TranslationHandler.translate("!!Typ"));
        tableHeader.addChild(TranslationHandler.translate("!!Start"));
        return tableHeader;
    }

    private GuiContextMenu cloneFinishedJobsContextMenu() {
        GuiContextMenu clonedJobsContextMenu = new GuiContextMenu();
        for (int i = 0; i < finishedJobsContextMenu.getChildren().size(); i++) {
            AbstractGuiControl control = finishedJobsContextMenu.getChildren().get(i);
            AbstractGuiControl clone = control.cloneMe();
            clone.setUserObject(control.getUserObject());
            control.copyEventListeners(clone, Event.MENU_ITEM_EVENT);
            clonedJobsContextMenu.addChild(clone);
        }
        return clonedJobsContextMenu;
    }

    @Override
    public boolean isSecondToolbarVisible() {
        return false;
    }

    @Override
    public void activeFormChanged(AbstractJavaViewerForm newActiveForm, AbstractJavaViewerForm lastActiveForm) {
        // nur beim ersten Aufruf die Job-Logs einlesen (dann ist dateConfig noch null)
        if (newActiveForm == this) {
            // Beim ersten Aufruf des JobsMainForms die Jobs mit Ladeanzeige laden
            refreshJobLogsIfNecessary(true);
        }
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().isFlagUserInterfaceLanguageChanged()) {
            // Hier ohne Form laden, weil das updateData() aus einem anderen Form kommen könnte
            refreshAllLogs(true);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        iPartsJobsManager.getInstance().removeJobsNotifiactionListener(jobsNotificationListener);
    }

    @Override
    public GuiPanel getGui() {
        return mainWindow.mainPanel;
    }

    public GuiWindow getMainWindow() {
        return mainWindow;
    }

    public boolean refreshJobLogsIfNecessary(boolean withMessageForm) {
        if (dateConfig == null) {
            if (withMessageForm) {
                refreshAllLogsWithMessageForm();
            } else {
                refreshAllLogs(false);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Lädt Jobs mit einer Ladeanzeige
     *
     * @param runnable
     * @param formTitle
     */
    private void loadWithMessageForm(FrameworkRunnable runnable, String formTitle) {
        EtkMessageLogForm logForm = new EtkMessageLogForm("!!Job-Logs", formTitle, null, true);
        logForm.disableButtons(true);
        logForm.showModal(runnable);
    }

    private void refreshAllLogsWithMessageForm() {
        loadWithMessageForm(thread -> refreshAllLogs(false), "!!Aktualisiere alle Jobs...");
    }

    /**
     * Lädt alle Jobs zu allen Tabelle neu
     *
     * @param withRefreshHeader
     */
    private void refreshAllLogs(boolean withRefreshHeader) {
        dateConfig = DateConfig.getInstance(getConfig());
        DWFile oldSelectedJobLogFile = getSelectedJobLogFile();
        Session.invokeThreadSafeInSession(() -> {
            setSelectedJobTableRow(null);

            // Running Jobs Log Dateien werden in einem separaten Verzeichnis gespeichert
            refreshRunningJobsTable(oldSelectedJobLogFile, withRefreshHeader);
            // Processed und Error Logs liegen im gleichen "Base" Verzeichnis
            refreshProcessedJobsTable(oldSelectedJobLogFile, withRefreshHeader, false);
            refreshErrorJobsTable(oldSelectedJobLogFile, withRefreshHeader, false);
        });
    }

    /**
     * Lädt alle laufenden Jobs neu
     *
     * @param oldSelectedJobLogFile
     * @param withRefreshHeader
     */
    private void refreshRunningJobsTable(DWFile oldSelectedJobLogFile, boolean withRefreshHeader) {
        refreshJobLogsForTable(mainWindow.runningJobsTable, iPartsJobsManager.getInstance().getRunningJobLogs(),
                               oldSelectedJobLogFile, false, withRefreshHeader, RUNNING_JOBS_TITLE,
                               Running_JOBS_TITLE_LIMIT_REACHED);
    }

    /**
     * Lädt alle fehlerhaften Jobs neu. Optional mit einer Ladeanzeige
     *
     * @param oldSelectedJobLogFile
     * @param withRefreshHeader
     * @param withOwnLogForm
     */
    private void refreshErrorJobsTable(DWFile oldSelectedJobLogFile, boolean withRefreshHeader, boolean withOwnLogForm) {
        if (withOwnLogForm) {
            refreshJobLogsForTableWithLogForm(mainWindow.errorJobsTable, iPartsJobsManager.getInstance().getErrorJobLogs(),
                                              oldSelectedJobLogFile, true, withRefreshHeader, ERROR_JOBS_TITLE,
                                              ERROR_JOBS_TITLE_LIMIT_REACHED);
        } else {
            refreshJobLogsForTable(mainWindow.errorJobsTable, iPartsJobsManager.getInstance().getErrorJobLogs(),
                                   oldSelectedJobLogFile, true, withRefreshHeader, ERROR_JOBS_TITLE,
                                   ERROR_JOBS_TITLE_LIMIT_REACHED);
        }
    }

    /**
     * Lädt alle durchgeführten Jobs neu. Optional mit einer Ladeanzeige
     *
     * @param oldSelectedJobLogFile
     * @param withRefreshHeader
     * @param withOwnLogForm
     */
    private void refreshProcessedJobsTable(DWFile oldSelectedJobLogFile, boolean withRefreshHeader, boolean withOwnLogForm) {
        if (withOwnLogForm) {
            refreshJobLogsForTableWithLogForm(mainWindow.processedJobsTable, iPartsJobsManager.getInstance().getProcessedJobLogs(),
                                              oldSelectedJobLogFile, true, withRefreshHeader, PROCESSED_JOBS_TITLE,
                                              PROCESSED_JOBS_TITLE_LIMIT_REACHED);
        } else {
            refreshJobLogsForTable(mainWindow.processedJobsTable, iPartsJobsManager.getInstance().getProcessedJobLogs(),
                                   oldSelectedJobLogFile, true, withRefreshHeader, PROCESSED_JOBS_TITLE,
                                   PROCESSED_JOBS_TITLE_LIMIT_REACHED);
        }
    }

    /**
     * Lädt die übergebene {@link GuiTable} neu. Ab {@link JobsMainForm#THRESHOLD_FOR_MESSAGE_LOG_FORM} Jobs wird eine
     * Ladeanzeige angezeigt.
     *
     * @param jobsTable
     * @param jobLogFiles
     * @param oldSelectedJobLogFile
     * @param withEndDateTime
     * @param withRefreshHeader
     * @param title
     * @param titleLimitReached
     */
    private void refreshJobLogsForTableWithLogForm(GuiTable jobsTable, List<DWFile> jobLogFiles, DWFile oldSelectedJobLogFile,
                                                   boolean withEndDateTime, boolean withRefreshHeader, String title, String titleLimitReached) {
        if (jobLogFiles.size() > THRESHOLD_FOR_MESSAGE_LOG_FORM) {
            String formTitle = getLoadTitleForTable(jobsTable);
            loadWithMessageForm(thread -> refreshJobLogsForTable(jobsTable, jobLogFiles, oldSelectedJobLogFile,
                                                                 withEndDateTime, withRefreshHeader, title, titleLimitReached),
                                formTitle);
        } else {
            refreshJobLogsForTable(jobsTable, jobLogFiles, oldSelectedJobLogFile, withEndDateTime, withRefreshHeader, title, titleLimitReached);
        }
    }

    /**
     * Liefert Titel zur Anzeige beim Laden der Jobs zu einer Tabelle
     *
     * @param jobsTable
     * @return
     */
    private String getLoadTitleForTable(GuiTable jobsTable) {
        if (jobsTable == mainWindow.processedJobsTable) {
            return TranslationHandler.translate("!!Aktualisiere alle durchgeführten Jobs...");
        } else if (jobsTable == mainWindow.errorJobsTable) {
            return TranslationHandler.translate("!!Aktualisiere alle fehlerhaften Jobs...");
        } else if (jobsTable == mainWindow.runningJobsTable) {
            return TranslationHandler.translate("!!Aktualisiere alle laufenden Jobs...");
        }
        return null;
    }

    /**
     * Lädt die Jobs zur übergebenen Tabelle neu
     *
     * @param jobsTable
     * @param jobLogFiles
     * @param oldSelectedJobLogFile
     * @param withEndDateTime
     * @param withRefreshHeader
     * @param title
     * @param titleLimitReached
     */
    private void refreshJobLogsForTable(GuiTable jobsTable, List<DWFile> jobLogFiles, DWFile oldSelectedJobLogFile,
                                        boolean withEndDateTime, boolean withRefreshHeader, String title,
                                        String titleLimitReached) {
        synchronized (RUNNING_JOBS_LOCK) {
            int jobsSortColumn = jobsTable.getSortColumn();
            jobsTable.removeRows();
            if (withRefreshHeader) {
                setHeader(jobsTable);
            }

            // laufende Importe
            int rowCounter = 0;
            int selectedRow = -1;
            // Sortieren und die eingestellte Anzahl Logs holen
            List<LastModifiedSortFile> test = jobLogFiles.stream()
                    .map(LastModifiedSortFile::new)
                    .sorted(Comparator.comparing(LastModifiedSortFile::getStartDateTime).reversed()).limit(iPartsPlugin.getJobsLogLimitPerTable())
                    .collect(Collectors.toList());
            for (LastModifiedSortFile fileWithLastModified : test) {
                jobsTable.addRow(new GuiTableRowWithJobLogFile(fileWithLastModified.file, withEndDateTime));
                if ((oldSelectedJobLogFile != null) && oldSelectedJobLogFile.equals(fileWithLastModified.file)) {
                    selectedRow = rowCounter;
                }
                rowCounter++;
            }
            checkTitle(jobsTable, jobLogFiles.size(), title, titleLimitReached);
            if ((selectedRow != -1) && (jobsSortColumn == -1)) {
                jobsTable.setSelectedRow(selectedRow, true);
            }
            jobsTable.sortRowsAccordingToColumn(jobsSortColumn, jobsTable.isSortAscending());
            // wenn sortiert wurde, die mögliche Selektion nachziehen
            if ((jobsSortColumn != -1) && (oldSelectedJobLogFile != null)) {
                for (int rowNo = 0; rowNo < jobsTable.getRowCount(); rowNo++) {
                    GuiTableRow row = jobsTable.getRow(rowNo);
                    if (row instanceof GuiTableRowWithJobLogFile) {
                        if (oldSelectedJobLogFile.equals(((GuiTableRowWithJobLogFile)row).getJobLogFile())) {
                            jobsTable.setSelectedRow(rowNo, true);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Setzt den Titel der übergebenen Tabelle abhängig von der Anzahl der enthaltenen Jobs
     *
     * @param jobsTable
     * @param jobsSize
     * @param title
     * @param titleLimitReached
     */
    private void checkTitle(GuiTable jobsTable, int jobsSize, String title, String titleLimitReached) {
        if (!StrUtils.isValid(title, titleLimitReached)) {
            return;
        }
        GuiDockingPanel panel = getPanelForTable(jobsTable);
        if (panel == null) {
            return;
        }
        int jobLogsLimit = iPartsPlugin.getJobsLogLimitPerTable();
        if (jobsSize > jobLogsLimit) {
            String translatedText = TranslationHandler.translate(titleLimitReached, String.valueOf(jobLogsLimit));
            panel.setTextShow(translatedText);
            panel.setTextHide(translatedText);
        } else {
            panel.setTextShow(title);
            panel.setTextHide(title);
        }
    }

    /**
     * Liefert das Panel zur übergebenen {@link GuiTable}
     *
     * @param jobsTable
     * @return
     */
    private GuiDockingPanel getPanelForTable(GuiTable jobsTable) {
        if (jobsTable == mainWindow.processedJobsTable) {
            return mainWindow.processedJobsDockingPanel;
        } else if (jobsTable == mainWindow.errorJobsTable) {
            return mainWindow.errorJobsDockingPanel;
        } else if (jobsTable == mainWindow.runningJobsTable) {
            return mainWindow.runningJobsDockingPanel;
        }
        return null;
    }

    public void addRunningJobLogFile(DWFile jobLogFile) {
        synchronized (RUNNING_JOBS_LOCK) {
            int runningJobsSortColumn = mainWindow.runningJobsTable.getSortColumn();
            mainWindow.runningJobsTable.addRow(new GuiTableRowWithJobLogFile(jobLogFile, false));
            mainWindow.runningJobsTable.sortRowsAccordingToColumn(runningJobsSortColumn, mainWindow.runningJobsTable.isSortAscending());
        }
    }

    public void removeRunningJobLogFile(DWFile jobLogFile) {
        synchronized (RUNNING_JOBS_LOCK) {
            for (int i = 0; i < mainWindow.runningJobsTable.getRowCount(); i++) {
                if (((GuiTableRowWithJobLogFile)mainWindow.runningJobsTable.getRow(i)).getJobLogFile().equals(jobLogFile)) {
                    mainWindow.runningJobsTable.removeRow(i);
                    break;
                }
            }
        }
    }

    public void addProcessedJobLogFile(DWFile jobLogFile) {
        synchronized (PROCESSED_JOBS_LOCK) {
            addJobLogWithChecks(mainWindow.processedJobsDockingPanel, mainWindow.processedJobsTable, jobLogFile,
                                PROCESSED_JOBS_TITLE, PROCESSED_JOBS_TITLE_LIMIT_REACHED);
        }
    }

    private void addJobLogWithChecks(GuiDockingPanel jobsDockingPanel, GuiTable jobsTable, DWFile jobLogFile,
                                     String jobsTitle, String jobsTitleLimitReached) {
        String text;
        // Check, ob in der Tabelle mehr Logs sind als die festgesetzte Grenze
        if (addLogFileWithMaxRowCheck(jobsTable, jobLogFile)) {
            text = TranslationHandler.translate(jobsTitleLimitReached, String.valueOf(iPartsPlugin.getJobsLogLimitPerTable()));
        } else {
            text = TranslationHandler.translate(jobsTitle);
        }
        if (!jobsDockingPanel.getTextShow().equals(text)) {
            jobsDockingPanel.setTextShow(text);
            jobsDockingPanel.setTextHide(text);
        }
    }

    /**
     * Überprüft, ob in der übergebenen Tabelle mehr Einträge enthalten sind als die festgesetze Grenze
     *
     * @param tableWithLogs
     * @param jobLogFile
     * @return
     */
    private boolean addLogFileWithMaxRowCheck(GuiTable tableWithLogs, DWFile jobLogFile) {
        // Eintrag hinzufügen
        int sortColumn = tableWithLogs.getSortColumn();
        tableWithLogs.addRow(new GuiTableRowWithJobLogFile(jobLogFile, true));
        // Sortieren
        tableWithLogs.sortRowsAccordingToColumn(sortColumn, tableWithLogs.isSortAscending());
        // Check, ob schon mehr oder genauso viele Logs existieren, wie in der Admin-Option eingestellt ist
        int jobLogsLimit = iPartsPlugin.getJobsLogLimitPerTable();
        int additionalRows = tableWithLogs.getRowCount() - jobLogsLimit;
        boolean result = false;
        if (additionalRows > 0) {
            tableWithLogs.sortByDefaultSortOrder();
            for (int count = 1; count <= additionalRows; count++) {
                tableWithLogs.removeRow((jobLogsLimit + additionalRows) - count);
            }
            result = true;
        }
        return result;
    }

    public void removeProcessedJobLogFile(DWFile jobLogFile) {
        synchronized (PROCESSED_JOBS_LOCK) {
            for (int i = 0; i < mainWindow.processedJobsTable.getRowCount(); i++) {
                if (((GuiTableRowWithJobLogFile)mainWindow.processedJobsTable.getRow(i)).getJobLogFile().equals(jobLogFile)) {
                    mainWindow.processedJobsTable.removeRow(i);
                    break;
                }
            }
        }
    }

    public void addErrorJobLogFile(DWFile jobLogFile) {
        synchronized (ERROR_JOBS_LOCK) {
            addJobLogWithChecks(mainWindow.errorJobsDockingPanel, mainWindow.errorJobsTable, jobLogFile, ERROR_JOBS_TITLE,
                                ERROR_JOBS_TITLE_LIMIT_REACHED);
        }
    }

    public void removeErrorJobLogFile(DWFile jobLogFile) {
        synchronized (ERROR_JOBS_LOCK) {
            for (int i = 0; i < mainWindow.errorJobsTable.getRowCount(); i++) {
                if (((GuiTableRowWithJobLogFile)mainWindow.errorJobsTable.getRow(i)).getJobLogFile().equals(jobLogFile)) {
                    mainWindow.errorJobsTable.removeRow(i);
                    break;
                }
            }
        }
    }

    private void setSelectedJobTableRow(GuiTableRowWithJobLogFile selectedJobTableRow) {
        this.selectedJobTableRow = selectedJobTableRow;
        if (selectedJobTableRow != null) {
            DWFile jobLogFile = selectedJobTableRow.getJobLogFile();
            if (jobLogFile.isFile()) {
                try {
                    String logFileContent = jobLogFile.readTextFile(DWFileCoding.UTF8);
                    mainWindow.logContentTextArea.setText(logFileContent);
                    mainWindow.downloadJobLogButton.setEnabled(true);
                    mainWindow.refreshLogContentButton.setEnabled(selectedJobTableRow.getTable() == mainWindow.runningJobsTable);
                } catch (IOException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    MessageDialog.showError("!!Die Datei kann nicht geladen werden!");
                }
            } else {
                mainWindow.logContentTextArea.setText(TranslationHandler.translate("!!Logdatei '%s' nicht vorhanden.", jobLogFile.getAbsolutePath()));
            }
        } else {
            mainWindow.logContentTextArea.setText(TranslationHandler.translate("!!Kein Job ausgewählt."));
            mainWindow.downloadJobLogButton.setEnabled(false);
            mainWindow.refreshLogContentButton.setEnabled(false);
        }
    }

    private DWFile getSelectedJobLogFile() {
        if (selectedJobTableRow != null) {
            return selectedJobTableRow.getJobLogFile();
        } else {
            return null;
        }
    }

    private void showMQTestDialog(Event e) {
        iPartsPlugin.showMQTestDialog().run(this);
    }

    private void deleteAllCaches(Event e) {
        iPartsPlugin.clearCaches(false, null).run(this);
    }

    private void showInterAppComStatus(Event e) {
        iPartsPlugin.showInterAppComStatus().run(this);
    }

    private void testSpecialCharactersInFileNames(Event e) {
        DWFile importLogsDir = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_JOB_LOGS_DIR);
        importLogsDir.mkDirsWithRepeat();
        DWFile testFile1 = importLogsDir.getChild("Test1 äöüß áà€.test");
        DWFile testFile2 = importLogsDir.getChild("Test2 Unicode \u00e4\u00f6\u00fc\u00df.test");
        try {
            testFile1.createNewFile();
        } catch (IOException ex) {
            MessageDialog.show("Datei (\"" + testFile1.getAbsolutePath() + "\") konnte nicht erzeugt werden!");
        }
        try {
            testFile2.createNewFile();
        } catch (IOException ex) {
            MessageDialog.show("Datei (\"" + testFile2.getAbsolutePath() + "\") konnte nicht erzeugt werden!");
        }

        List<DWFile> testFiles = importLogsDir.listDWFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".test");
            }
        });

        String testFileNames = "";
        for (DWFile testFile : testFiles) {
            testFileNames += "\n- " + testFile;
        }

        DWInputStream inputStream = testFile1.getInputStream();
        String inputStreamEncoding = "";
        if (inputStream != null) {
            inputStreamEncoding = "\nEncoding vom InputStreamHandler (Test1): " + new InputStreamReader(inputStream).getEncoding();
        } else {
            inputStream = testFile2.getInputStream();
            if (inputStream != null) {
                inputStreamEncoding = "\nEncoding vom InputStreamHandler (Test2): " + new InputStreamReader(inputStream).getEncoding();
            }
        }
        MessageDialog.show("Gefundene Testdateien: " + testFileNames + "\n\nFile Encoding: " + System.getProperty("file.encoding")
                           + "\njava.nio.Charset: " + Charset.defaultCharset().name() + inputStreamEncoding);
        if (inputStream != null) {
            inputStream.closeSilently();
        }

        testFile1.delete();
        testFile2.delete();
    }

    private void executeSQLPerformanceTests(Event e) {
        iPartsPlugin.executeSQLPerformanceTests().run(this);
    }

    private void showAboutDialog(Event e) {
        AboutForm aboutForm = new AboutForm(getConnector(), this);
        aboutForm.showModal();
    }

    private void changeAutoImportSettingsEvent(Event e) {
        if (iPartsPlugin.isImportPluginActive()) {
            de.docware.apps.etk.plugins.customer.daimler.iparts_import.forms.AutoImportSettingsForm.showAutoImportSettingsForm(this);
        }
    }

    private void runningJobLogSelected(Event e) {
        GuiTableRow selectedRow = mainWindow.runningJobsTable.getSelectedRow();
        if (selectedRow != null) {
            mainWindow.processedJobsTable.clearSelection();
            mainWindow.errorJobsTable.clearSelection();
            setSelectedJobTableRow((GuiTableRowWithJobLogFile)selectedRow);
        } else {
            setSelectedJobTableRow(null);
        }
    }

    private void processedJobLogSelected(Event e) {
        GuiTableRow selectedRow = mainWindow.processedJobsTable.getSelectedRow();
        if (selectedRow != null) {
            mainWindow.runningJobsTable.clearSelection();
            mainWindow.errorJobsTable.clearSelection();
            setSelectedJobTableRow((GuiTableRowWithJobLogFile)selectedRow);
        } else {
            setSelectedJobTableRow(null);
        }
    }

    private void errorJobLogSelected(Event e) {
        GuiTableRow selectedRow = mainWindow.errorJobsTable.getSelectedRow();
        if (selectedRow != null) {
            mainWindow.runningJobsTable.clearSelection();
            mainWindow.processedJobsTable.clearSelection();
            setSelectedJobTableRow((GuiTableRowWithJobLogFile)selectedRow);
        } else {
            setSelectedJobTableRow(null);
        }
    }

    private void refreshJobLogs(Event e) {
        refreshAllLogsWithMessageForm();
    }

    private void downloadJobLog(Event e) {
        DWFile selectedJobLogFile = getSelectedJobLogFile();
        if (selectedJobLogFile == null) {
            return;
        }

        GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(getGui(), FileChooserPurpose.SAVE, GuiFileChooserDialog.FILE_MODE_FILES,
                                                                          null, false);
        fileChooserDialog.setServerMode(false);
        fileChooserDialog.addChoosableFileFilter(DWFileFilterEnum.LOGFILES.getDescription(), DWFileFilterEnum.LOGFILES.getExtensions());
        fileChooserDialog.setActiveFileFilter(DWFileFilterEnum.LOGFILES.getDescription());
        try {
            fileChooserDialog.setVisible(new MultipleInputToOutputStream(selectedJobLogFile, selectedJobLogFile.getName()));
        } catch (IOException e1) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e1);
            MessageDialog.showError("!!Fehler beim Speichern.");
        }
    }

    private void refreshLogContent(Event e) {
        setSelectedJobTableRow(selectedJobTableRow);
    }

    private void archiveJobLog(Event e) {
        if (selectedJobTableRow == null) {
            return;
        }

        if (MessageDialog.showYesNo(TranslationHandler.translate("!!Soll die Log-Datei für den Job \"%1\" wirklich archiviert werden?",
                                                                 selectedJobTableRow.getJobType()),
                                    "!!Log-Datei archivieren") == ModalResult.YES) {
            DWFile jobLogFile = selectedJobTableRow.getJobLogFile();
            DWFile logsDir = DWFile.get(iPartsPlugin.getPluginConfig().getConfigValueAsFile(iPartsPlugin.CONFIG_JOB_LOGS_DIR));
            DWFile logsArchiveDir = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_JOB_LOGS_ARCHIVE_DIR);

            // Relativen Pfad der Log-Datei zum Log-Verzeichnis bestimmen und auf das Archiv-Verzeichnis anwenden inkl.
            // Erzeugen des neuen Verzeichnisses, in dem sich die archivierte Log-Datei befinden wird
            String relativePath;
            try {
                relativePath = jobLogFile.getRelativePath(logsDir);
            } catch (IOException ex) {
                relativePath = jobLogFile.extractFileName(true);
            }
            DWFile archiveFile = logsArchiveDir.getChild(relativePath);
            archiveFile.getParentDWFile().mkDirsWithRepeat();

            GuiTable jobsTable = selectedJobTableRow.getTable();
            if (jobLogFile.move(archiveFile, true)) {
                jobsTable.removeRow(selectedJobTableRow);
                setSelectedJobTableRow(null);
            } else {
                MessageDialog.showError(TranslationHandler.translate("!!Fehler beim Archivieren der Log-Datei:%1",
                                                                     "\n" + jobLogFile.extractFileName(true)));
            }
            Session.invokeThreadSafeInSession(() -> {
                if (jobsTable == mainWindow.processedJobsTable) {
                    refreshProcessedJobsTable(null, false, true);
                } else {
                    refreshErrorJobsTable(null, false, true);
                }
            });
        }
    }

    private void archiveAllJobLogs(Event e) {
        if (selectedJobTableRow == null) {
            return;
        }

        GuiTable jobsTable = selectedJobTableRow.getTable();
        String jobTableType;
        if (jobsTable == mainWindow.processedJobsTable) {
            jobTableType = "!!durchgeführten";
        } else {
            jobTableType = "!!fehlerhaften";
        }
        if (MessageDialog.showYesNo(TranslationHandler.translate("!!Sollen alle Log-Dateien für die %1 Jobs wirklich archiviert werden?",
                                                                 TranslationHandler.translate(jobTableType)),
                                    "!!Alle Log-Dateien archivieren") == ModalResult.YES) {
            final DWFile logsDir = DWFile.get(iPartsPlugin.getPluginConfig().getConfigValueAsFile(iPartsPlugin.CONFIG_JOB_LOGS_DIR));
            final DWFile logsArchiveDir = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_JOB_LOGS_ARCHIVE_DIR);

            // Zu archivierende Dateien bestimmten (alle in Verzeichnis liegenden Log Dateien)
            List<DWFile> jobLogs;
            if (jobsTable == mainWindow.processedJobsTable) {
                jobLogs = iPartsJobsManager.getInstance().getProcessedJobLogs();
            } else {
                jobLogs = iPartsJobsManager.getInstance().getErrorJobLogs();
            }
            final List<DWFile> jobLogFiles = jobLogs;
            final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Alle Log-Dateien archivieren", "!!Fortschritt", null);
            messageLogForm.setMessagesTitle("");
            messageLogForm.getGui().setSize(600, 300);
            final EtkMessageLog messageLog = messageLogForm.getMessageLog();
            messageLogForm.showModal(new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    int maxPos = jobLogFiles.size();
                    messageLog.fireMessage(TranslationHandler.translate("!!Archiviere %1 Log-Dateien...", String.valueOf(maxPos)));

                    boolean hasErrors = false;
                    int pos = 0;
                    messageLog.fireProgress(pos, maxPos, "", false, true);
                    for (DWFile jobLogFile : jobLogFiles) {
                        if (thread.wasCanceled()) {
                            messageLogForm.getGui().setVisible(false);
                            return;
                        }

                        pos++;

                        // Relativen Pfad der Log-Datei zum Log-Verzeichnis bestimmen und auf das Archiv-Verzeichnis anwenden inkl.
                        // Erzeugen des neuen Verzeichnisses, in dem sich die archivierte Log-Datei befinden wird
                        String relativePath;
                        try {
                            relativePath = jobLogFile.getRelativePath(logsDir);
                        } catch (IOException ex) {
                            relativePath = jobLogFile.extractFileName(true);
                        }
                        DWFile archiveFile = logsArchiveDir.getChild(relativePath);
                        archiveFile.getParentDWFile().mkDirsWithRepeat();

                        if (!jobLogFile.move(archiveFile, true)) {
                            hasErrors = true;
                            messageLog.fireMessage(TranslationHandler.translate("!!Fehler beim Archivieren der Log-Datei:%1",
                                                                                " " + jobLogFile.extractFileName(true)),
                                                   MessageLogType.tmlError);
                        }
                        messageLog.fireProgress(pos, maxPos, "", false, true);
                    }

                    messageLog.hideProgress();

                    if (!hasErrors) {
                        messageLogForm.getGui().setVisible(false);
                    }
                }
            });

            // Alle Jobs aktualisieren, um die Tabelle zu aktualisieren
            refreshAllLogsWithMessageForm();
        }
    }

    private void markJobAsError(Event e) {
        if (selectedJobTableRow == null) {
            return;
        }


        if (MessageDialog.showYesNo(TranslationHandler.translate("!!Soll der Job \"%1\" wirklich als fehlerhaft markiert werden?",
                                                                 selectedJobTableRow.getJobType()),
                                    "!!Job als fehlerhaft markieren") == ModalResult.YES) {
            DWFile errorJobLogFile = iPartsJobsManager.getInstance().jobError(selectedJobTableRow.getJobLogFile());

            // Text für manuelles Markieren als fehlerhaft ans Ende der Datei schreiben
            String cancelledText = OsUtils.NEWLINE + TranslationKeys.LINE_SEPARATOR
                                   + OsUtils.NEWLINE + TranslationHandler.translateForLanguage("Der Job wurde manuell als fehlerhaft markiert.",
                                                                                               iPartsConst.LOG_FILES_LANGUAGE)
                                   + OsUtils.NEWLINE + TranslationKeys.LINE_SEPARATOR;
            try {
                errorJobLogFile.appendTextFile(cancelledText.getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
            } catch (IOException ex) {
                // nichts machen, dann fehlt der Text am Ende halt
            }
        }
    }


    class GuiTableRowWithJobLogFile extends GuiTableRow {

        private final DWFile jobLogFile;
        private String jobType = TranslationHandler.translate("!!Unbekannt");
        private String jobStartDateTime = "";
        private String jobEndDateTime = "";

        GuiTableRowWithJobLogFile(DWFile jobLogFile, boolean withEndDateTime) {
            this.jobLogFile = jobLogFile;
            String jobLogFileName = DWFile.extractFileName(jobLogFile.getAbsolutePath(), false);
            String[] jobLogFileNameParts = jobLogFileName.split(iPartsJobsManager.JOB_LOG_FILE_SEPARATOR);
            int requiredPartsCount = 2;
            if (withEndDateTime) {
                requiredPartsCount = 3;
            }
            if (jobLogFileNameParts.length >= requiredPartsCount) {
                jobType = jobLogFileNameParts[0];
                jobStartDateTime = jobLogFileNameParts[1];
                if (withEndDateTime) {
                    jobEndDateTime = jobLogFileNameParts[2];
                }
            } else {
                jobType = jobLogFileNameParts[0];
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Invalid file name format for log file: "
                                                                          + jobLogFile.getAbsolutePath());
            }

            addChild(jobType);
            if (!jobStartDateTime.isEmpty()) {
                addChild(dateConfig.formatDateTime(getProject().getViewerLanguage(), jobStartDateTime));
            } else {
                addChild(TranslationHandler.translate("!!Unbekannt"));
            }
            if (withEndDateTime) {
                if (!jobEndDateTime.isEmpty()) {
                    addChild(dateConfig.formatDateTime(getProject().getViewerLanguage(), jobEndDateTime));
                } else {
                    addChild(TranslationHandler.translate("!!Unbekannt"));
                }
            }
        }

        public DWFile getJobLogFile() {
            return jobLogFile;
        }

        public String getJobType() {
            return jobType;
        }

        public String getJobStartDateTime() {
            return jobStartDateTime;
        }

        public String getJobEndDateTime() {
            return jobEndDateTime;
        }
    }

    /**
     * Hilfsklasse um Jobs Dateien nach ihrem Datum zu sortieren
     */
    private static class LastModifiedSortFile {

        private DWFile file;
        private String startDateTime;

        private LastModifiedSortFile(DWFile file) {
            setFile(file);
        }

        private void setFile(DWFile file) {
            this.file = file;
            String[] jobLogFileNameParts = file.extractFileName(false).split(iPartsJobsManager.JOB_LOG_FILE_SEPARATOR);
            if (jobLogFileNameParts.length > 1) {
                startDateTime = jobLogFileNameParts[1];
            } else {
                startDateTime = String.valueOf(Long.MAX_VALUE);
            }
        }

        public DWFile getFile() {
            return file;
        }

        public String getStartDateTime() {
            return startDateTime;
        }
    }


    class GuiTableColumnWithDateTimeSortConverter implements TableColumnSortConverter {

        @Override
        public String getSortValue(TableInterface table, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return ((GuiTableRowWithJobLogFile)table.getRow(rowIndex)).getJobType();
                case 1:
                    return ((GuiTableRowWithJobLogFile)table.getRow(rowIndex)).getJobStartDateTime();
                case 2:
                    return ((GuiTableRowWithJobLogFile)table.getRow(rowIndex)).getJobEndDateTime();
            }
            return "";
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu runningJobsContextMenu;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiMenuItem downloadRunningJobLogMenuItem;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiMenuItem refreshLogContentMenuItem;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.GuiSeparator separatorForError;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiMenuItem markJobAsErrorMenuItem;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu finishedJobsContextMenu;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiMenuItem downloadFinishedJobLogMenuItem;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.GuiSeparator separatorForArchive;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiMenuItem archiveJobLogMenuItem;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiMenuItem archiveAllJobLogsMenuItem;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuBar menuBar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuBarEntry testMenu;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuItem mqTestMenuItem;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuItem testSpecialCharactersInFileNamesMenuItem;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuItem sqlPerformanceTestsMenuItem;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuItem deleteCachesMenuItem;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuItem testInterAppComMenuItem;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuBarEntry importMenu;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuItem menuItemAutoImportSettings;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuBarEntry helpMenu;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuItem aboutMenuItem;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel centerPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane logsSplitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel logJobsPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane topJobsSplitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel runningJobsDockingPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane runningJobsScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable runningJobsTable;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane bottomJobsSplitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel processedJobsDockingPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane processedJobsScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable processedJobsTable;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel errorJobsDockingPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane errorJobsScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable errorJobsTable;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel logContentPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane logContentScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea logContentTextArea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar logJobsToolbar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolButton refreshJobLogsButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolButton toolbarSeparator;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolButton downloadJobLogButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolButton refreshLogContentButton;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            runningJobsContextMenu = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            runningJobsContextMenu.setName("runningJobsContextMenu");
            runningJobsContextMenu.__internal_setGenerationDpi(96);
            runningJobsContextMenu.registerTranslationHandler(translationHandler);
            runningJobsContextMenu.setScaleForResolution(true);
            runningJobsContextMenu.setMinimumWidth(10);
            runningJobsContextMenu.setMinimumHeight(10);
            downloadRunningJobLogMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            downloadRunningJobLogMenuItem.setName("downloadRunningJobLogMenuItem");
            downloadRunningJobLogMenuItem.__internal_setGenerationDpi(96);
            downloadRunningJobLogMenuItem.registerTranslationHandler(translationHandler);
            downloadRunningJobLogMenuItem.setScaleForResolution(true);
            downloadRunningJobLogMenuItem.setMnemonicEnabled(true);
            downloadRunningJobLogMenuItem.setText("!!Log-Datei herunterladen...");
            downloadRunningJobLogMenuItem.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgApp_javaviewer_Save"));
            downloadRunningJobLogMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    downloadJobLog(event);
                }
            });
            runningJobsContextMenu.addChild(downloadRunningJobLogMenuItem);
            refreshLogContentMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            refreshLogContentMenuItem.setName("refreshLogContentMenuItem");
            refreshLogContentMenuItem.__internal_setGenerationDpi(96);
            refreshLogContentMenuItem.registerTranslationHandler(translationHandler);
            refreshLogContentMenuItem.setScaleForResolution(true);
            refreshLogContentMenuItem.setMnemonicEnabled(true);
            refreshLogContentMenuItem.setText("!!Log aktualisieren");
            refreshLogContentMenuItem.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgApp_javaviewer_ToolbarRefresh"));
            refreshLogContentMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    refreshLogContent(event);
                }
            });
            runningJobsContextMenu.addChild(refreshLogContentMenuItem);
            separatorForError = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separatorForError.setName("separatorForError");
            separatorForError.__internal_setGenerationDpi(96);
            separatorForError.registerTranslationHandler(translationHandler);
            separatorForError.setScaleForResolution(true);
            runningJobsContextMenu.addChild(separatorForError);
            markJobAsErrorMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            markJobAsErrorMenuItem.setName("markJobAsErrorMenuItem");
            markJobAsErrorMenuItem.__internal_setGenerationDpi(96);
            markJobAsErrorMenuItem.registerTranslationHandler(translationHandler);
            markJobAsErrorMenuItem.setScaleForResolution(true);
            markJobAsErrorMenuItem.setMnemonicEnabled(true);
            markJobAsErrorMenuItem.setText("!!Job als fehlerhaft markieren");
            markJobAsErrorMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    markJobAsError(event);
                }
            });
            runningJobsContextMenu.addChild(markJobAsErrorMenuItem);
            runningJobsContextMenu.setMenuName("runningJobsContextMenu");
            runningJobsContextMenu.setParentControl(this);
            finishedJobsContextMenu = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            finishedJobsContextMenu.setName("finishedJobsContextMenu");
            finishedJobsContextMenu.__internal_setGenerationDpi(96);
            finishedJobsContextMenu.registerTranslationHandler(translationHandler);
            finishedJobsContextMenu.setScaleForResolution(true);
            finishedJobsContextMenu.setMinimumWidth(10);
            finishedJobsContextMenu.setMinimumHeight(10);
            downloadFinishedJobLogMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            downloadFinishedJobLogMenuItem.setName("downloadFinishedJobLogMenuItem");
            downloadFinishedJobLogMenuItem.__internal_setGenerationDpi(96);
            downloadFinishedJobLogMenuItem.registerTranslationHandler(translationHandler);
            downloadFinishedJobLogMenuItem.setScaleForResolution(true);
            downloadFinishedJobLogMenuItem.setMnemonicEnabled(true);
            downloadFinishedJobLogMenuItem.setText("!!Log-Datei herunterladen...");
            downloadFinishedJobLogMenuItem.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgApp_javaviewer_Save"));
            downloadFinishedJobLogMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    downloadJobLog(event);
                }
            });
            finishedJobsContextMenu.addChild(downloadFinishedJobLogMenuItem);
            separatorForArchive = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separatorForArchive.setName("separatorForArchive");
            separatorForArchive.__internal_setGenerationDpi(96);
            separatorForArchive.registerTranslationHandler(translationHandler);
            separatorForArchive.setScaleForResolution(true);
            finishedJobsContextMenu.addChild(separatorForArchive);
            archiveJobLogMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            archiveJobLogMenuItem.setName("archiveJobLogMenuItem");
            archiveJobLogMenuItem.__internal_setGenerationDpi(96);
            archiveJobLogMenuItem.registerTranslationHandler(translationHandler);
            archiveJobLogMenuItem.setScaleForResolution(true);
            archiveJobLogMenuItem.setMnemonicEnabled(true);
            archiveJobLogMenuItem.setText("!!Log-Datei archivieren");
            archiveJobLogMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    archiveJobLog(event);
                }
            });
            finishedJobsContextMenu.addChild(archiveJobLogMenuItem);
            archiveAllJobLogsMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            archiveAllJobLogsMenuItem.setName("archiveAllJobLogsMenuItem");
            archiveAllJobLogsMenuItem.__internal_setGenerationDpi(96);
            archiveAllJobLogsMenuItem.registerTranslationHandler(translationHandler);
            archiveAllJobLogsMenuItem.setScaleForResolution(true);
            archiveAllJobLogsMenuItem.setMnemonicEnabled(true);
            archiveAllJobLogsMenuItem.setText("!!Alle Log-Dateien archivieren");
            archiveAllJobLogsMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    archiveAllJobLogs(event);
                }
            });
            finishedJobsContextMenu.addChild(archiveAllJobLogsMenuItem);
            finishedJobsContextMenu.setMenuName("runningJobsContextMenu");
            finishedJobsContextMenu.setParentControl(this);
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setTitle("!!iParts Jobs");
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            menuBar = new de.docware.framework.modules.gui.controls.menu.GuiMenuBar();
            menuBar.setName("menuBar");
            menuBar.__internal_setGenerationDpi(96);
            menuBar.registerTranslationHandler(translationHandler);
            menuBar.setScaleForResolution(true);
            menuBar.setMinimumWidth(10);
            menuBar.setMinimumHeight(10);
            testMenu = new de.docware.framework.modules.gui.controls.menu.GuiMenuBarEntry();
            testMenu.setName("testMenu");
            testMenu.__internal_setGenerationDpi(96);
            testMenu.registerTranslationHandler(translationHandler);
            testMenu.setScaleForResolution(true);
            testMenu.setMinimumWidth(10);
            testMenu.setMinimumHeight(10);
            testMenu.setMnemonicEnabled(true);
            testMenu.setText("!!Test");
            mqTestMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            mqTestMenuItem.setName("mqTestMenuItem");
            mqTestMenuItem.__internal_setGenerationDpi(96);
            mqTestMenuItem.registerTranslationHandler(translationHandler);
            mqTestMenuItem.setScaleForResolution(true);
            mqTestMenuItem.setMnemonicEnabled(true);
            mqTestMenuItem.setText("!!MQ Test...");
            mqTestMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    showMQTestDialog(event);
                }
            });
            testMenu.addChild(mqTestMenuItem);
            testSpecialCharactersInFileNamesMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            testSpecialCharactersInFileNamesMenuItem.setName("testSpecialCharactersInFileNamesMenuItem");
            testSpecialCharactersInFileNamesMenuItem.__internal_setGenerationDpi(96);
            testSpecialCharactersInFileNamesMenuItem.registerTranslationHandler(translationHandler);
            testSpecialCharactersInFileNamesMenuItem.setScaleForResolution(true);
            testSpecialCharactersInFileNamesMenuItem.setMnemonicEnabled(true);
            testSpecialCharactersInFileNamesMenuItem.setText("!!Teste Umlaute in Dateinamen...");
            testSpecialCharactersInFileNamesMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    testSpecialCharactersInFileNames(event);
                }
            });
            testMenu.addChild(testSpecialCharactersInFileNamesMenuItem);
            sqlPerformanceTestsMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            sqlPerformanceTestsMenuItem.setName("sqlPerformanceTestsMenuItem");
            sqlPerformanceTestsMenuItem.__internal_setGenerationDpi(96);
            sqlPerformanceTestsMenuItem.registerTranslationHandler(translationHandler);
            sqlPerformanceTestsMenuItem.setScaleForResolution(true);
            sqlPerformanceTestsMenuItem.setMnemonicEnabled(true);
            sqlPerformanceTestsMenuItem.setText("!!SQL Performance Tests...");
            sqlPerformanceTestsMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    executeSQLPerformanceTests(event);
                }
            });
            testMenu.addChild(sqlPerformanceTestsMenuItem);
            deleteCachesMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            deleteCachesMenuItem.setName("deleteCachesMenuItem");
            deleteCachesMenuItem.__internal_setGenerationDpi(96);
            deleteCachesMenuItem.registerTranslationHandler(translationHandler);
            deleteCachesMenuItem.setScaleForResolution(true);
            deleteCachesMenuItem.setMnemonicEnabled(true);
            deleteCachesMenuItem.setText("!!Caches löschen");
            deleteCachesMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    deleteAllCaches(event);
                }
            });
            testMenu.addChild(deleteCachesMenuItem);
            testInterAppComMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            testInterAppComMenuItem.setName("testInterAppComMenuItem");
            testInterAppComMenuItem.__internal_setGenerationDpi(96);
            testInterAppComMenuItem.registerTranslationHandler(translationHandler);
            testInterAppComMenuItem.setScaleForResolution(true);
            testInterAppComMenuItem.setMnemonicEnabled(true);
            testInterAppComMenuItem.setText("!!Kommunikation zwischen WebApps...");
            testInterAppComMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    showInterAppComStatus(event);
                }
            });
            testMenu.addChild(testInterAppComMenuItem);
            menuBar.addChild(testMenu);
            importMenu = new de.docware.framework.modules.gui.controls.menu.GuiMenuBarEntry();
            importMenu.setName("importMenu");
            importMenu.__internal_setGenerationDpi(96);
            importMenu.registerTranslationHandler(translationHandler);
            importMenu.setScaleForResolution(true);
            importMenu.setMinimumWidth(10);
            importMenu.setMinimumHeight(10);
            importMenu.setMnemonicEnabled(true);
            importMenu.setText("!!&Import");
            menuItemAutoImportSettings = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            menuItemAutoImportSettings.setName("menuItemAutoImportSettings");
            menuItemAutoImportSettings.__internal_setGenerationDpi(96);
            menuItemAutoImportSettings.registerTranslationHandler(translationHandler);
            menuItemAutoImportSettings.setScaleForResolution(true);
            menuItemAutoImportSettings.setMnemonicEnabled(true);
            menuItemAutoImportSettings.setText("!!Einstellungen für automatische Importe...");
            menuItemAutoImportSettings.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    changeAutoImportSettingsEvent(event);
                }
            });
            importMenu.addChild(menuItemAutoImportSettings);
            menuBar.addChild(importMenu);
            helpMenu = new de.docware.framework.modules.gui.controls.menu.GuiMenuBarEntry();
            helpMenu.setName("helpMenu");
            helpMenu.__internal_setGenerationDpi(96);
            helpMenu.registerTranslationHandler(translationHandler);
            helpMenu.setScaleForResolution(true);
            helpMenu.setMinimumWidth(10);
            helpMenu.setMinimumHeight(10);
            helpMenu.setMnemonicEnabled(true);
            helpMenu.setText("!!&Hilfe");
            aboutMenuItem = new de.docware.framework.modules.gui.controls.menu.GuiMenuItem();
            aboutMenuItem.setName("aboutMenuItem");
            aboutMenuItem.__internal_setGenerationDpi(96);
            aboutMenuItem.registerTranslationHandler(translationHandler);
            aboutMenuItem.setScaleForResolution(true);
            aboutMenuItem.setMnemonicEnabled(true);
            aboutMenuItem.setText("!!Info &über...");
            aboutMenuItem.addEventListener(new de.docware.framework.modules.gui.event.EventListener("menuItemEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    showAboutDialog(event);
                }
            });
            helpMenu.addChild(aboutMenuItem);
            menuBar.addChild(helpMenu);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder menuBarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            menuBarConstraints.setPosition("north");
            menuBar.setConstraints(menuBarConstraints);
            this.addChild(menuBar);
            centerPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            centerPanel.setName("centerPanel");
            centerPanel.__internal_setGenerationDpi(96);
            centerPanel.registerTranslationHandler(translationHandler);
            centerPanel.setScaleForResolution(true);
            centerPanel.setMinimumWidth(10);
            centerPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder centerPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            centerPanel.setLayout(centerPanelLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("!!iParts Jobs");
            title.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgPlugin_iParts_JobsButton"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            centerPanel.addChild(title);
            mainPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanel.setName("mainPanel");
            mainPanel.__internal_setGenerationDpi(96);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            mainPanel.setMinimumWidth(10);
            mainPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanel.setLayout(mainPanelLayout);
            logsSplitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            logsSplitPane.setName("logsSplitPane");
            logsSplitPane.__internal_setGenerationDpi(96);
            logsSplitPane.registerTranslationHandler(translationHandler);
            logsSplitPane.setScaleForResolution(true);
            logsSplitPane.setMinimumWidth(10);
            logsSplitPane.setMinimumHeight(10);
            logsSplitPane.setDividerPosition(500);
            logJobsPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            logJobsPanel.setName("logJobsPanel");
            logJobsPanel.__internal_setGenerationDpi(96);
            logJobsPanel.registerTranslationHandler(translationHandler);
            logJobsPanel.setScaleForResolution(true);
            logJobsPanel.setMinimumWidth(10);
            logJobsPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder logJobsPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            logJobsPanel.setLayout(logJobsPanelLayout);
            topJobsSplitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            topJobsSplitPane.setName("topJobsSplitPane");
            topJobsSplitPane.__internal_setGenerationDpi(96);
            topJobsSplitPane.registerTranslationHandler(translationHandler);
            topJobsSplitPane.setScaleForResolution(true);
            topJobsSplitPane.setMinimumWidth(10);
            topJobsSplitPane.setMinimumHeight(10);
            topJobsSplitPane.setHorizontal(false);
            topJobsSplitPane.setDividerPosition(97);
            runningJobsDockingPanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            runningJobsDockingPanel.setName("runningJobsDockingPanel");
            runningJobsDockingPanel.__internal_setGenerationDpi(96);
            runningJobsDockingPanel.registerTranslationHandler(translationHandler);
            runningJobsDockingPanel.setScaleForResolution(true);
            runningJobsDockingPanel.setMinimumWidth(180);
            runningJobsDockingPanel.setMinimumHeight(19);
            runningJobsDockingPanel.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonEnabledBackgroundGradient1"));
            runningJobsDockingPanel.setForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clWhite"));
            runningJobsDockingPanel.setTextHide("!!Laufende Jobs");
            runningJobsDockingPanel.setTextShow("!!Laufende Jobs");
            runningJobsDockingPanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            runningJobsDockingPanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            runningJobsDockingPanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            runningJobsDockingPanel.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            runningJobsDockingPanel.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            runningJobsDockingPanel.setButtonFill(true);
            runningJobsScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            runningJobsScrollPane.setName("runningJobsScrollPane");
            runningJobsScrollPane.__internal_setGenerationDpi(96);
            runningJobsScrollPane.registerTranslationHandler(translationHandler);
            runningJobsScrollPane.setScaleForResolution(true);
            runningJobsScrollPane.setMinimumWidth(10);
            runningJobsScrollPane.setMinimumHeight(0);
            runningJobsTable = new de.docware.framework.modules.gui.controls.table.GuiTable();
            runningJobsTable.setName("runningJobsTable");
            runningJobsTable.__internal_setGenerationDpi(96);
            runningJobsTable.registerTranslationHandler(translationHandler);
            runningJobsTable.setScaleForResolution(true);
            runningJobsTable.setMinimumWidth(10);
            runningJobsTable.setMinimumHeight(10);
            runningJobsTable.setHtmlTablePageSplitMode(de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode.NO_SPLIT);
            runningJobsTable.addEventListener(new de.docware.framework.modules.gui.event.EventListener("tableSelectionEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    runningJobLogSelected(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder runningJobsTableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            runningJobsTable.setConstraints(runningJobsTableConstraints);
            runningJobsScrollPane.addChild(runningJobsTable);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder runningJobsScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            runningJobsScrollPane.setConstraints(runningJobsScrollPaneConstraints);
            runningJobsDockingPanel.addChild(runningJobsScrollPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder runningJobsDockingPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            runningJobsDockingPanel.setConstraints(runningJobsDockingPanelConstraints);
            topJobsSplitPane.addChild(runningJobsDockingPanel);
            bottomJobsSplitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            bottomJobsSplitPane.setName("bottomJobsSplitPane");
            bottomJobsSplitPane.__internal_setGenerationDpi(96);
            bottomJobsSplitPane.registerTranslationHandler(translationHandler);
            bottomJobsSplitPane.setScaleForResolution(true);
            bottomJobsSplitPane.setMinimumWidth(10);
            bottomJobsSplitPane.setMinimumHeight(10);
            bottomJobsSplitPane.setHorizontal(false);
            bottomJobsSplitPane.setDividerPosition(336);
            processedJobsDockingPanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            processedJobsDockingPanel.setName("processedJobsDockingPanel");
            processedJobsDockingPanel.__internal_setGenerationDpi(96);
            processedJobsDockingPanel.registerTranslationHandler(translationHandler);
            processedJobsDockingPanel.setScaleForResolution(true);
            processedJobsDockingPanel.setMinimumWidth(124);
            processedJobsDockingPanel.setMinimumHeight(21);
            processedJobsDockingPanel.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonEnabledBackgroundGradient1"));
            processedJobsDockingPanel.setForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clWhite"));
            processedJobsDockingPanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            processedJobsDockingPanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            processedJobsDockingPanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            processedJobsDockingPanel.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            processedJobsDockingPanel.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            processedJobsDockingPanel.setButtonFill(true);
            processedJobsScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            processedJobsScrollPane.setName("processedJobsScrollPane");
            processedJobsScrollPane.__internal_setGenerationDpi(96);
            processedJobsScrollPane.registerTranslationHandler(translationHandler);
            processedJobsScrollPane.setScaleForResolution(true);
            processedJobsScrollPane.setMinimumWidth(10);
            processedJobsScrollPane.setMinimumHeight(0);
            processedJobsTable = new de.docware.framework.modules.gui.controls.table.GuiTable();
            processedJobsTable.setName("processedJobsTable");
            processedJobsTable.__internal_setGenerationDpi(96);
            processedJobsTable.registerTranslationHandler(translationHandler);
            processedJobsTable.setScaleForResolution(true);
            processedJobsTable.setMinimumWidth(10);
            processedJobsTable.setMinimumHeight(10);
            processedJobsTable.addEventListener(new de.docware.framework.modules.gui.event.EventListener("tableSelectionEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    processedJobLogSelected(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder processedJobsTableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            processedJobsTable.setConstraints(processedJobsTableConstraints);
            processedJobsScrollPane.addChild(processedJobsTable);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder processedJobsScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            processedJobsScrollPane.setConstraints(processedJobsScrollPaneConstraints);
            processedJobsDockingPanel.addChild(processedJobsScrollPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder processedJobsDockingPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            processedJobsDockingPanel.setConstraints(processedJobsDockingPanelConstraints);
            bottomJobsSplitPane.addChild(processedJobsDockingPanel);
            errorJobsDockingPanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            errorJobsDockingPanel.setName("errorJobsDockingPanel");
            errorJobsDockingPanel.__internal_setGenerationDpi(96);
            errorJobsDockingPanel.registerTranslationHandler(translationHandler);
            errorJobsDockingPanel.setScaleForResolution(true);
            errorJobsDockingPanel.setMinimumWidth(191);
            errorJobsDockingPanel.setMinimumHeight(19);
            errorJobsDockingPanel.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonEnabledBackgroundGradient1"));
            errorJobsDockingPanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            errorJobsDockingPanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            errorJobsDockingPanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            errorJobsDockingPanel.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            errorJobsDockingPanel.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            errorJobsDockingPanel.setButtonFill(true);
            errorJobsScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            errorJobsScrollPane.setName("errorJobsScrollPane");
            errorJobsScrollPane.__internal_setGenerationDpi(96);
            errorJobsScrollPane.registerTranslationHandler(translationHandler);
            errorJobsScrollPane.setScaleForResolution(true);
            errorJobsScrollPane.setMinimumWidth(10);
            errorJobsScrollPane.setMinimumHeight(0);
            errorJobsTable = new de.docware.framework.modules.gui.controls.table.GuiTable();
            errorJobsTable.setName("errorJobsTable");
            errorJobsTable.__internal_setGenerationDpi(96);
            errorJobsTable.registerTranslationHandler(translationHandler);
            errorJobsTable.setScaleForResolution(true);
            errorJobsTable.setMinimumWidth(10);
            errorJobsTable.setMinimumHeight(10);
            errorJobsTable.addEventListener(new de.docware.framework.modules.gui.event.EventListener("tableSelectionEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    errorJobLogSelected(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder errorJobsTableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            errorJobsTable.setConstraints(errorJobsTableConstraints);
            errorJobsScrollPane.addChild(errorJobsTable);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder errorJobsScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            errorJobsScrollPane.setConstraints(errorJobsScrollPaneConstraints);
            errorJobsDockingPanel.addChild(errorJobsScrollPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder errorJobsDockingPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            errorJobsDockingPanel.setConstraints(errorJobsDockingPanelConstraints);
            bottomJobsSplitPane.addChild(errorJobsDockingPanel);
            topJobsSplitPane.addChild(bottomJobsSplitPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder topJobsSplitPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            topJobsSplitPane.setConstraints(topJobsSplitPaneConstraints);
            logJobsPanel.addChild(topJobsSplitPane);
            logsSplitPane.addChild(logJobsPanel);
            logContentPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            logContentPanel.setName("logContentPanel");
            logContentPanel.__internal_setGenerationDpi(96);
            logContentPanel.registerTranslationHandler(translationHandler);
            logContentPanel.setScaleForResolution(true);
            logContentPanel.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutBorder logContentPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            logContentPanel.setLayout(logContentPanelLayout);
            logContentScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            logContentScrollPane.setName("logContentScrollPane");
            logContentScrollPane.__internal_setGenerationDpi(96);
            logContentScrollPane.registerTranslationHandler(translationHandler);
            logContentScrollPane.setScaleForResolution(true);
            logContentScrollPane.setMinimumWidth(10);
            logContentScrollPane.setMinimumHeight(10);
            logContentScrollPane.setBorderWidth(1);
            logContentScrollPane.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            logContentTextArea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            logContentTextArea.setName("logContentTextArea");
            logContentTextArea.__internal_setGenerationDpi(96);
            logContentTextArea.registerTranslationHandler(translationHandler);
            logContentTextArea.setScaleForResolution(true);
            logContentTextArea.setMinimumWidth(200);
            logContentTextArea.setMinimumHeight(100);
            logContentTextArea.setBorderWidth(4);
            logContentTextArea.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            logContentTextArea.setText("Kein Job ausgewählt.");
            logContentTextArea.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder logContentTextAreaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            logContentTextArea.setConstraints(logContentTextAreaConstraints);
            logContentScrollPane.addChild(logContentTextArea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder logContentScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            logContentScrollPane.setConstraints(logContentScrollPaneConstraints);
            logContentPanel.addChild(logContentScrollPane);
            logsSplitPane.addChild(logContentPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder logsSplitPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            logsSplitPane.setConstraints(logsSplitPaneConstraints);
            mainPanel.addChild(logsSplitPane);
            logJobsToolbar = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            logJobsToolbar.setName("logJobsToolbar");
            logJobsToolbar.__internal_setGenerationDpi(96);
            logJobsToolbar.registerTranslationHandler(translationHandler);
            logJobsToolbar.setScaleForResolution(true);
            logJobsToolbar.setMinimumWidth(10);
            logJobsToolbar.setMinimumHeight(10);
            logJobsToolbar.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            logJobsToolbar.setButtonLayout(de.docware.framework.modules.gui.controls.toolbar.ToolButtonLayout.IMAGE_WEST);
            refreshJobLogsButton = new de.docware.framework.modules.gui.controls.toolbar.GuiToolButton();
            refreshJobLogsButton.setName("refreshJobLogsButton");
            refreshJobLogsButton.__internal_setGenerationDpi(96);
            refreshJobLogsButton.registerTranslationHandler(translationHandler);
            refreshJobLogsButton.setScaleForResolution(true);
            refreshJobLogsButton.setMinimumWidth(10);
            refreshJobLogsButton.setMinimumHeight(10);
            refreshJobLogsButton.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            refreshJobLogsButton.setText("!!Jobs aktualisieren");
            refreshJobLogsButton.setGlyph(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgApp_javaviewer_ToolbarRefresh"));
            refreshJobLogsButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    refreshJobLogs(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag refreshJobLogsButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 100.0, "w", "v", 2, 2, 2, 2);
            refreshJobLogsButton.setConstraints(refreshJobLogsButtonConstraints);
            logJobsToolbar.addChild(refreshJobLogsButton);
            toolbarSeparator = new de.docware.framework.modules.gui.controls.toolbar.GuiToolButton();
            toolbarSeparator.setName("toolbarSeparator");
            toolbarSeparator.__internal_setGenerationDpi(96);
            toolbarSeparator.registerTranslationHandler(translationHandler);
            toolbarSeparator.setScaleForResolution(true);
            toolbarSeparator.setMinimumWidth(10);
            toolbarSeparator.setMinimumHeight(10);
            toolbarSeparator.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            toolbarSeparator.setButtonType(de.docware.framework.modules.gui.controls.toolbar.ToolButtonType.SEPARATOR);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag toolbarSeparatorConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 100.0, "w", "v", 2, 2, 2, 2);
            toolbarSeparator.setConstraints(toolbarSeparatorConstraints);
            logJobsToolbar.addChild(toolbarSeparator);
            downloadJobLogButton = new de.docware.framework.modules.gui.controls.toolbar.GuiToolButton();
            downloadJobLogButton.setName("downloadJobLogButton");
            downloadJobLogButton.__internal_setGenerationDpi(96);
            downloadJobLogButton.registerTranslationHandler(translationHandler);
            downloadJobLogButton.setScaleForResolution(true);
            downloadJobLogButton.setMinimumWidth(10);
            downloadJobLogButton.setMinimumHeight(10);
            downloadJobLogButton.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            downloadJobLogButton.setEnabled(false);
            downloadJobLogButton.setText("!!Log-Datei herunterladen...");
            downloadJobLogButton.setGlyph(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgApp_javaviewer_Save"));
            downloadJobLogButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    downloadJobLog(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag downloadJobLogButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 100.0, "w", "v", 2, 2, 2, 2);
            downloadJobLogButton.setConstraints(downloadJobLogButtonConstraints);
            logJobsToolbar.addChild(downloadJobLogButton);
            refreshLogContentButton = new de.docware.framework.modules.gui.controls.toolbar.GuiToolButton();
            refreshLogContentButton.setName("refreshLogContentButton");
            refreshLogContentButton.__internal_setGenerationDpi(96);
            refreshLogContentButton.registerTranslationHandler(translationHandler);
            refreshLogContentButton.setScaleForResolution(true);
            refreshLogContentButton.setMinimumWidth(10);
            refreshLogContentButton.setMinimumHeight(10);
            refreshLogContentButton.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            refreshLogContentButton.setEnabled(false);
            refreshLogContentButton.setText("!!Log aktualisieren");
            refreshLogContentButton.setGlyph(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgApp_javaviewer_ToolbarRefresh"));
            refreshLogContentButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    refreshLogContent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag refreshLogContentButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 0, 1, 1, 0.0, 100.0, "w", "v", 2, 2, 2, 2);
            refreshLogContentButton.setConstraints(refreshLogContentButtonConstraints);
            logJobsToolbar.addChild(refreshLogContentButton);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder logJobsToolbarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            logJobsToolbarConstraints.setPosition("north");
            logJobsToolbar.setConstraints(logJobsToolbarConstraints);
            mainPanel.addChild(logJobsToolbar);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            centerPanel.addChild(mainPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder centerPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            centerPanel.setConstraints(centerPanelConstraints);
            this.addChild(centerPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}
