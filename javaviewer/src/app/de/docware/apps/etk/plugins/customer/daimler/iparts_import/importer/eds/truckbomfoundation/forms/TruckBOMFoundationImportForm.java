/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.forms;

import de.docware.apps.etk.base.forms.common.EditControlDateTimeEditPanel;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWSWithImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWebserviceException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWebserviceUtils;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.SQLStringConvert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Dialog zum starten des manuellen TruckBOM.foundation Imports
 */
public class TruckBOMFoundationImportForm {

    private static final String DB_RUNNING_KEY = "iparts_manual_truck_bom_foundation_import_running";
    private static final String RESET_BUTTON_TEXT = "!!Zurücksetzen";
    private final EtkProject project;
    private List<GuiCheckbox> checkboxes;
    private final TruckBOMFoundationWSHelper truckBOMFoundationWSHelper;
    private final EditFilterDateGuiHelper dateGuiHelper;

    /**
     * Erzeugt eine Instanz von TruckBOMFoundationImportForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public TruckBOMFoundationImportForm() {
        this.truckBOMFoundationWSHelper = new TruckBOMFoundationWSHelper();
        this.dateGuiHelper = new EditFilterDateGuiHelper(true);
        this.project = iPartsPlugin.getMqProject();
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setText("!!Import starten");
        addDateTimeControls();
        addCustomButtons();
        addWSToLCheckboxList();
        checkButtons();
        mainWindow.pack();
    }

    /**
     * Fügt den Button zum Zurücksetzen der Optionen hinzu
     */
    private void addCustomButtons() {
        GuiButtonOnPanel resetButton = mainWindow.buttonPanel.addCustomButton(RESET_BUTTON_TEXT);
        resetButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                resetInput();
            }
        });
    }

    /**
     * Fügt die Datum-Controls hinzu
     */
    private void addDateTimeControls() {
        OnFilterDateChangeEvent onFilterDateChangeEvent = new OnFilterDateChangeEvent() {
            @Override
            public void onChangeFromDate(EditFilterDateObject filterDateObject) {
                checkButtons();
            }

            @Override
            public void onChangeToDate(EditFilterDateObject filterDateObject) {
                checkButtons();
            }
        };
        GuiPanel datePanel = dateGuiHelper.createFilterDateTimePanel(onFilterDateChangeEvent, null, null);
        mainWindow.dateTimePanel.addChildGridBag(datePanel, 0, 0, 1, 1, 100.0,
                                                 100.0, ConstraintsGridBag.ANCHOR_NORTH, ConstraintsGridBag.FILL_HORIZONTAL,
                                                 8, 8, 8, 8);
    }

    /**
     * Befüllt die linke Seite mit allen TruckBOM.foundation Webservices, die auch Importer besitzen
     */
    private void addWSToLCheckboxList() {
        this.checkboxes = new ArrayList<>();
        Set<String> wsNames = truckBOMFoundationWSHelper.getWebserviceWithImporterURIs();
        int y = 0;
        int maxSize = wsNames.size();
        for (String wsName : wsNames) {
            GuiCheckbox checkbox = new GuiCheckbox(wsName, false);
            checkbox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    checkButtons();
                }
            });
            checkboxes.add(checkbox);
            mainWindow.checkboxPanel.addChildGridBag(checkbox, 0, y, 1, 1, 100.0, (maxSize == (y + 1)) ? 100.0 : 0.0,
                                                     ConstraintsGridBag.ANCHOR_NORTHWEST, ConstraintsGridBag.FILL_NONE,
                                                     4, 0, 4, 0);
            y++;
        }
    }

    /**
     * (De-)Aktiviert die Buttons
     */
    private void checkButtons() {
        // Flag, ob mind. ein WS selektiert wurde
        boolean wsSelected = isAtLeastOneWSSelected();
        EditFilterDateObject filterDateObject = new EditFilterDateObject(dateGuiHelper.getDateTimeCalendarFrom(), dateGuiHelper.getDateTimeCalendarTo());
        // Import nur möglich, wenn mind. ein WS selektiert und beide Datumsangaben gesetzt sind
        mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setEnabled(wsSelected && filterDateObject.isDateFromLessOrEqualDateTo());
        GuiButtonOnPanel resetButton = mainWindow.buttonPanel.getCustomButtonOnPanel(RESET_BUTTON_TEXT);
        // Zurücksetzen nur möglich, wenn irgendetwas verändert wurde
        resetButton.setEnabled(wsSelected || !StrUtils.isEmpty(filterDateObject.getDateFrom(), filterDateObject.getDateTo()));
    }

    private boolean isAtLeastOneWSSelected() {
        return checkboxes.stream().anyMatch(GuiCheckbox::isSelected);
    }


    /**
     * Zeigt den Dialog. Wenn schon ein Import läuft, wird eine Hinweismeldung angezeigt
     */
    public void show() {
        if (isImportBlocked()) {
            MessageDialog.show("!!Ein manueller Import wurde bereits angestoßen! Bitte warten Sie bis dieser abgeschlossen ist.");
        } else {
            mainWindow.showModal();
        }
    }

    /**
     * Startet die Anfragen und Import für jeden ausgewählten WS
     *
     * @param event
     */
    private void startImport(Event event) {
        if (isAtLeastOneWSSelected() && dateTimeValueValid()) {
            FrameworkRunnable runnable = createWSRunnable();
            // MQ Session nutzen, falls der Benutzer den Browser schließt
            Session session = iPartsPlugin.getMqSession();
            if (session != null) {
                session.startChildThread(runnable);
            } else {
                runnable.run(null);
            }
        }
        close();
    }

    private void close() {
        mainWindow.setVisible(false);
    }

    private FrameworkRunnable createWSRunnable() {
        return thread -> {
            // Zur Sicherheit prüfen, ob der Import schon läuft. Kann hier eigentlich nicht sein, da der Dialog bei
            // einem laufenden Import nicht angezeigt wird
            if (isImportBlocked()) {
                return;
            }
            // In der DB vermerken, dass wir einen Import starten
            blockImport();
            try {
                checkboxes.forEach(checkbox -> {
                    if (Thread.currentThread().isInterrupted() || thread.appActionWasCanceled() || thread.wasCanceled()) {
                        return;
                    }
                    // Durchlaufe alle Checkboxen und starte für die selektierten den WS und Import
                    if (checkbox.isSelected()) {
                        String wsName = checkbox.getText();
                        if (StrUtils.isValid(wsName)) {
                            // Helper zum WS Namen bestimmen
                            iPartsTruckBOMFoundationWSWithImporter wsImportHelper = truckBOMFoundationWSHelper.getHelperForWSName(wsName);
                            boolean successfulImport = true;
                            // Sicherheitsabfrage, ob es überhaupt einen Importer bzw Helper für den WS gibt
                            if (wsImportHelper == null) {
                                return;
                            }
                            // Im Log den WS uppercase ausgeben
                            String wsNameForLog = wsName.toUpperCase();
                            // JobLog für den aktuellen WS und Import
                            DWFile jobLogFile = iPartsJobsManager.getInstance().jobRunning("Manual TruckBOM.foundation " + wsNameForLog + " Request and Import");
                            // LogHelper für alle Log-Meldungen außerhalb des Importers
                            ImportExportLogHelper logHelper = new ImportExportLogHelper(jobLogFile);
                            logHelper.addLogMsgWithTranslation("!!Import gestartet für Webservice: %1", wsNameForLog);
                            // Request Body erzeugen mit den eingestellten Datumsangaben
                            String requestPayloadJson = truckBOMFoundationWSHelper.createRequestPayload(wsImportHelper,
                                                                                                        getFromDateTime(),
                                                                                                        getToDateTime(),
                                                                                                        iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION);

                            if (StrUtils.isValid(requestPayloadJson)) {
                                logHelper.addLogMsgWithTranslation("!!Mit dem Ab-Datum \"%1\" und dem " +
                                                                   "Bis-Datum \"%2\" wurde folgende Anfrage erzeugt: %3",
                                                                   getFromDateTime(), getToDateTime(), requestPayloadJson);
                                try {
                                    logHelper.addLogMsgWithTranslation("!!Frage Webservice an...");
                                    // WS anfragen
                                    String response = iPartsTruckBOMFoundationWebserviceUtils.getJsonFromWebservice(wsName, requestPayloadJson, project);
                                    successfulImport = handleResponse(response, wsImportHelper, logHelper, wsNameForLog);
                                } catch (iPartsTruckBOMFoundationWebserviceException e) {
                                    successfulImport = false;
                                    logHelper.addLogErrorWithTranslation(logHelper.translateForLog("!!Fehler beim Webservice-Aufruf. Import abgebrochen:")
                                                                         + " " + wsNameForLog + "\n" + Utils.exceptionToString(e));
                                }
                            } else {
                                logHelper.addLogErrorWithTranslation("!!Es konnte kein Request-Body für die Anfrage erzeugt werden!");
                                successfulImport = false;
                            }

                            logHelper.fireLineSeparator();
                            // Log fertig schreiben und abhängig vom Ergebnis verschieben
                            if (successfulImport) {
                                logHelper.addLogMsgWithTranslation("!!TruckBOM.foundation Import erfolgreich abgeschlossen");
                                iPartsJobsManager.getInstance().jobProcessed(jobLogFile);
                            } else {
                                logHelper.addLogErrorWithTranslation("!!TruckBOM.foundation Import mit Fehlern abgebrochen");
                                iPartsJobsManager.getInstance().jobError(jobLogFile);
                            }
                        }
                    }
                });
            } finally {
                // Nachdem alle Checkboxen durchlaufen wurden, den manuellen Import wieder freigeben
                unblockImport();
            }
        };
    }

    private boolean handleResponse(String response, iPartsTruckBOMFoundationWSWithImporter wsImportHelper, ImportExportLogHelper logHelper, String wsNameForLog) {
        // Wenn die Response gültig ist, Importer starten
        if (!StrUtils.isValid(response)) {
            logHelper.addLogErrorWithTranslation("!!Keine gültige Antwort vom Webservice \"%1\"", wsNameForLog);
            return false;
        } else {
            logHelper.addLogMsgWithTranslation("!!Syntaktisch gültige Antwort von \"%1\" erhalten", wsNameForLog);
            // Über den Helper den Importer starten
            AbstractGenericImporter importer = wsImportHelper.startImportFromResponse(project, response, logHelper.getLogFile());
            if (importer != null) {
                if (importer.getErrorCount() > 0) {
                    logHelper.addLogErrorWithTranslation("!!Fehler beim Importieren der Daten für \"%1\"!", wsNameForLog);
                    return false;
                }
                if (importer.isCancelled()) {
                    logHelper.addLogErrorWithTranslation("!!Import der Daten für \"%1\" wurde abgebrochen!", wsNameForLog);
                    return false;
                }
                if (importer.getWarningCount() > 0) {
                    logHelper.addLogWarning(logHelper.translateForLog("!!Import mit %1 Warnungen beendet:",
                                                                      String.valueOf(importer.getWarningCount()))
                                            + " " + wsNameForLog);
                } else {
                    logHelper.addNewLine();
                    logHelper.addLogMsgWithTranslation("!!Import erfolgreich beendet: %1", wsNameForLog);
                }
            }
        }
        return true;
    }

    private void unblockImport() {
        project.getEtkDbs().setKeyValue(DB_RUNNING_KEY, SQLStringConvert.booleanToPPString(false));
    }

    private void blockImport() {
        project.getEtkDbs().setKeyValue(DB_RUNNING_KEY, SQLStringConvert.booleanToPPString(true));
    }

    private boolean isImportBlocked() {
        return SQLStringConvert.ppStringToBoolean(project.getEtkDbs().getKeyValue(DB_RUNNING_KEY));
    }

    private String getToDateTime() {
        return createWSDate(dateGuiHelper.getDateTimeCalendarTo());
    }

    private String createWSDate(EditControlDateTimeEditPanel dateTimeCalendar) {
        return DateUtils.toISO_DateTime(dateTimeCalendar.getDateTime()) + "Z";
    }

    private String getFromDateTime() {
        return createWSDate(dateGuiHelper.getDateTimeCalendarFrom());
    }

    /**
     * Prüft mit Hilfe von {@link EditFilterDateObject}, ob die Datumsangaben gültig sind
     *
     * @return
     */
    private boolean dateTimeValueValid() {
        EditFilterDateObject filterDateObject = new EditFilterDateObject(dateGuiHelper.getDateTimeCalendarFrom(), dateGuiHelper.getDateTimeCalendarTo());
        return filterDateObject.isDateFromLessOrEqualDateTo();
    }

    /**
     * Setzt die gesetzte Benutzerauswahl zurück
     */
    private void resetInput() {
        // Session von Benutzer kann hier verwendet werden, weil es ja eine Änderung in der aktuellen Oberfläche ist
        Session.get().invokeThreadSafeInSessionThread(() -> {
            checkboxes.forEach(checkbox -> checkbox.setSelected(false));
            dateGuiHelper.resetControls();
        });
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel wsPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel configPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel checkboxPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel rightPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel dateTimePanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
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
            title.setTitle("!!TruckBOM.foundation manueller Import");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            wsPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            wsPanel.setName("wsPanel");
            wsPanel.__internal_setGenerationDpi(96);
            wsPanel.registerTranslationHandler(translationHandler);
            wsPanel.setScaleForResolution(true);
            wsPanel.setMinimumWidth(10);
            wsPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder wsPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            wsPanel.setLayout(wsPanelLayout);
            configPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            configPanel.setName("configPanel");
            configPanel.__internal_setGenerationDpi(96);
            configPanel.registerTranslationHandler(translationHandler);
            configPanel.setScaleForResolution(true);
            configPanel.setMinimumWidth(10);
            configPanel.setMinimumHeight(10);
            configPanel.setBorderWidth(4);
            configPanel.setTitle("!!Webservice");
            de.docware.framework.modules.gui.layout.LayoutGridBag configPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            configPanel.setLayout(configPanelLayout);
            checkboxPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            checkboxPanel.setName("checkboxPanel");
            checkboxPanel.__internal_setGenerationDpi(96);
            checkboxPanel.registerTranslationHandler(translationHandler);
            checkboxPanel.setScaleForResolution(true);
            checkboxPanel.setMinimumWidth(10);
            checkboxPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag checkboxPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            checkboxPanel.setLayout(checkboxPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 100.0, "c", "b", 4, 8, 4, 8);
            checkboxPanel.setConstraints(checkboxPanelConstraints);
            configPanel.addChild(checkboxPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder configPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            configPanel.setConstraints(configPanelConstraints);
            wsPanel.addChild(configPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder wsPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            wsPanelConstraints.setPosition("west");
            wsPanel.setConstraints(wsPanelConstraints);
            this.addChild(wsPanel);
            rightPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            rightPanel.setName("rightPanel");
            rightPanel.__internal_setGenerationDpi(96);
            rightPanel.registerTranslationHandler(translationHandler);
            rightPanel.setScaleForResolution(true);
            rightPanel.setMinimumWidth(10);
            rightPanel.setMinimumHeight(10);
            rightPanel.setBorderWidth(4);
            rightPanel.setTitle("!!Import Zeitraum");
            de.docware.framework.modules.gui.layout.LayoutGridBag rightPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            rightPanel.setLayout(rightPanelLayout);
            dateTimePanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            dateTimePanel.setName("dateTimePanel");
            dateTimePanel.__internal_setGenerationDpi(96);
            dateTimePanel.registerTranslationHandler(translationHandler);
            dateTimePanel.setScaleForResolution(true);
            dateTimePanel.setMinimumWidth(10);
            dateTimePanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag dateTimePanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            dateTimePanel.setLayout(dateTimePanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag dateTimePanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            dateTimePanel.setConstraints(dateTimePanelConstraints);
            rightPanel.addChild(dateTimePanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder rightPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            rightPanel.setConstraints(rightPanelConstraints);
            this.addChild(rightPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    startImport(event);
                }
            });
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