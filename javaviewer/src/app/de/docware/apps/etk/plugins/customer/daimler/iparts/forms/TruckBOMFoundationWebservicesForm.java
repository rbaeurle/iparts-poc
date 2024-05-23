/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;


import com.owlike.genson.Genson;
import com.owlike.genson.JsonBindingException;
import com.owlike.genson.stream.JsonStreamException;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TruckBOMFoundationWSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWSWithImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWebserviceException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWebserviceUtils;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileOpenValidatorForExtensions;
import de.docware.framework.modules.gui.controls.filechooser.filefilter.DWFileFilterEnum;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

/**
 * Dialog zur Abfrage von TruckBOM.foundation Webservices
 */
public class TruckBOMFoundationWebservicesForm {

    private static final String SESSION_KEY_TRUCK_BOM_TEST_SELECTED_SERVICE = "iparts_truck_bom_test_selected_service";
    private static final String SESSION_KEY_TRUCK_BOM_TEST_IMPORT_RESPONSE = "iparts_truck_bom_test_import_response";
    private static final Object LOCK = new Object();
    private final EtkProject project;
    private final TruckBOMFoundationWSHelper truckBOMFoundationWSHelper;
    private String currentResponseText;

    /**
     * Erzeugt eine Instanz von TbfRequestEngineForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public TruckBOMFoundationWebservicesForm(EtkProject project) {
        this.project = project;
        this.truckBOMFoundationWSHelper = new TruckBOMFoundationWSHelper();
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        // Größe auf mindestens 80% des Bildschirms/Browsers festlegen
        mainWindow.setMinimumHeight((int)(0.8 * screenSize.getHeight()));
        mainWindow.setMinimumWidth((int)(0.8 * screenSize.getWidth()));
        // Scrollpanes mit den Textareas an das Fenster anpassen (abzüglich Randabstände)
        mainWindow.requestScrollpane.setMinimumWidth((mainWindow.getMinimumWidth() / 2) - 32);

        mainWindow.responseScrollpane.setMinimumWidth((mainWindow.getMinimumWidth() / 2) - 32);

        // WICHTIG: Die Webservice-Optionen müssen EXAKT der Ziel-Webservice-URI entsprechen
        mainWindow.serviceComboBox.switchOffEventListeners();
        mainWindow.serviceComboBox.addItem(null, "");
        truckBOMFoundationWSHelper.getWebserviceURIs().forEach(mainWindow.serviceComboBox::addItem);
        mainWindow.serviceComboBox.setSelectedUserObject(Session.get().getAttribute(SESSION_KEY_TRUCK_BOM_TEST_SELECTED_SERVICE));
        onServiceSelected(null);
        mainWindow.serviceComboBox.switchOnEventListeners();

        mainWindow.importResponseCheckbox.switchOffEventListeners();
        Object importResponseSessionValue = Session.get().getAttribute(SESSION_KEY_TRUCK_BOM_TEST_IMPORT_RESPONSE);
        mainWindow.importResponseCheckbox.setSelected((importResponseSessionValue == null) || Utils.objectEquals(importResponseSessionValue,
                                                                                                                 Boolean.TRUE));
        mainWindow.importResponseCheckbox.switchOnEventListeners();

        mainWindow.pack();
    }

    /**
     * Startet den Import eines JSON Response, der aus einer Datei gelesen wird
     *
     * @param event
     */
    private void doFileImport(Event event) {
        DWFile file = getSelectedFile();
        if (file != null) {
            String response = "";
            try {
                response = file.readTextFile(DWFileCoding.UTF8);
            } catch (IOException e) {
                MessageDialog.showError(TranslationHandler.translate("!!Fehler beim Einlesen der Datei: %1", e.getMessage()));
                Logger.log(iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION, LogType.DEBUG, "Error while reading the input file \""
                                                                                         + file.extractFileName(false) + "\": "
                                                                                         + " - " + e.getMessage());
                return;
            }
            String selectedWebservice = mainWindow.serviceComboBox.getSelectedItem();
            handleResponse(selectedWebservice, response);
        }
    }

    private DWFile getSelectedFile() {
        GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(mainWindow, FileChooserPurpose.OPEN,
                                                                          GuiFileChooserDialog.FILE_MODE_FILES, null, false);
        fileChooserDialog.setServerMode(false);
        fileChooserDialog.setMultiSelectionMode(false);
        fileChooserDialog.addChoosableFileFilter(DWFileFilterEnum.JSON_FILES.getDescription(), DWFileFilterEnum.XMLFILES.getExtensions());
        fileChooserDialog.setActiveFileFilter(DWFileFilterEnum.JSON_FILES.getDescription());
        fileChooserDialog.addFileOpenValidator(new GuiFileOpenValidatorForExtensions(MimeTypes.EXTENSION_JSON));
        fileChooserDialog.setVisible(true);
        return fileChooserDialog.getSelectedFile();
    }

    private void onImportResponseCheckboxChangeEvent(Event event) {
        // Zustand der Checkbox merken
        Session.get().setAttribute(SESSION_KEY_TRUCK_BOM_TEST_IMPORT_RESPONSE, mainWindow.importResponseCheckbox.isSelected());
    }


    /**
     * Methode zum Absenden eines ausgewählten Webservices
     *
     * @param event
     */
    private void sendRequest(Event event) {
        // Evtl. bestehende Response bereinigen
        mainWindow.responseTextarea.clear();
        currentResponseText = null;
        mainWindow.copyToClipboardButton.setEnabled(false);

        String selectedWebservice = mainWindow.serviceComboBox.getSelectedItem();
        if (StrUtils.isEmpty(selectedWebservice)) {
            MessageDialog.show("!!Bitte einen Webservice auswählen.");
        } else {
            String requestPayload = mainWindow.requestTextarea.getText();
            // POST-Requests benötigen in diesem Kontext zwingend einen Request Body im JSON-Format
            if (truckBOMFoundationWSHelper.isPostRequestWebservice(selectedWebservice)) {
                if (requestPayload.isEmpty()) {
                    MessageDialog.show("!!Der ausgewählte Webservice benötigt einen Request Body im JSON-Format.");
                } else {
                    try {
                        // JSON-Validität der String-Eingabe prüfen
                        Genson genson = new Genson();
                        genson.deserialize(requestPayload, Map.class);
                        // Wenn Eingabe valide, dann Request absenden
                        executeTruckBOMFoundationRequest(selectedWebservice, requestPayload);
                    } catch (JsonStreamException | JsonBindingException e) {
                        MessageDialog.show("!!Ungültiges JSON-Format. Bitte Eingabe überprüfen.");
                    }
                }
            } else {
                // Alle anderen Request-Methoden werden hier behandelt. Wichtig für die Zukunft: Sollten andere Methoden wie GET verwendet werden,
                // müssen diese hier berücksichtigt werden, da unter Umständen ein Request Body benötigt wird (z.B. PUT)
                // Bei GET-Methoden wird kein Request Body benötigt
                if (!requestPayload.isEmpty()) {
                    MessageDialog.show("!!Der ausgewählte Webservice benötigt keinen Request Body im JSON-Format. Bitte Eingabe entfernen.");
                } else {
                    executeTruckBOMFoundationRequest(selectedWebservice, requestPayload);
                }
            }
        }
    }

    /**
     * Generische Methode zum Ausführen eines TruckBOM.foundation Webservices
     *
     * @param webserviceName
     * @param requestPayload
     */
    private void executeTruckBOMFoundationRequest(String webserviceName, String requestPayload) {
        VarParam<String> response = new VarParam<>();
        doRequestWithMessageForm(response, webserviceName, requestPayload);
        // Ergebnis verarbeiten
        if (response.getValue() != null) {
            // Check, ob es einen Fehler bei der Anfrage gab. Falls ja, wurde schon ein Hinweis angezeigt. Also soll hier
            // nichts gemacht werden
            if (!response.getValue().equals(LogType.ERROR.name())) {
                handleResponse(webserviceName, response.getValue());
            }
        } else {
            MessageDialog.showError("!!Die angeforderten Daten wurden vom Webservice nicht gefunden (HTTP Status-Code 404 NOT FOUND bzw. fehlender Response Body).");
        }
    }

    /**
     * Sendet die Anfrage an den TruckBOM.foundation Webservice und zeigt für die Dauer der Anfrage einen Ladebalken an
     *
     * @param response
     * @param webserviceName
     * @param requestPayload
     */
    private void doRequestWithMessageForm(VarParam<String> response, String webserviceName, String requestPayload) {
        // Form für den Ladebalken, solange die Anfrage läuft
        EtkMessageLogForm logForm = new EtkMessageLogForm("!!TruckBOM.foundation", TranslationHandler.translate("!!Frage Webservice \"%1\" an...", webserviceName), null, true);
        logForm.disableButtons(true);
        logForm.showModal(thread -> {
            try {
                // Info: Bei leerem Request Body (JSON) wird bei der HttpClient-Erstellung die GET-Methode verwendet.
                response.setValue(iPartsTruckBOMFoundationWebserviceUtils.getJsonFromWebservice(webserviceName, requestPayload, project));
            } catch (iPartsTruckBOMFoundationWebserviceException e) {
                MessageDialog.showError(TranslationHandler.translate("!!Fehler beim Aufruf des Webservices: %1 - %2", String.valueOf(e.getHttpResponseCode()),
                                                                     e.getMessage()));
                Logger.log(iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION, LogType.DEBUG, "Error while performing TruckBOM.foundation webservice \""
                                                                                         + webserviceName + "\": " + e.getHttpResponseCode()
                                                                                         + " - " + e.getMessage());
                // Einfach einen Kenner setzen, dass es bei der Anfrage einen Fehler gab
                response.setValue(LogType.ERROR.name());
            }
        });
    }

    /**
     * Verarbeitet den übergebenen JSON Response (in der Oberfläche setzen und Importer antriggern)
     *
     * @param webserviceName
     * @param response
     */
    private void handleResponse(String webserviceName, String response) {
        // Bei Erfolg: Response ausgeben und Button zum Kopieren aktivieren, falls Response nicht leer
        mainWindow.responseTextarea.setText(response);
        currentResponseText = response;
        mainWindow.copyToClipboardButton.setEnabled(!response.isEmpty());
        if (!iPartsPlugin.isImportPluginActive()) {
            MessageDialog.showError("!!Das Import Plugin ist nicht aktiv. Der Import kann nicht durchgeführt werden!");
            return;
        }
        if (mainWindow.importResponseCheckbox.isSelected()) {
            Session.startChildThreadInSession(thread -> {
                iPartsTruckBOMFoundationWSWithImporter helper = truckBOMFoundationWSHelper.getHelperForWSName(webserviceName);
                if (helper != null) {
                    synchronized (LOCK) {
                        helper.startImportFromResponse(project, response, null);
                    }
                }
            });
        }
    }

    /**
     * Methode zum Kopieren der vollständigen Response in die Zwischenablage
     *
     * @param event
     */
    private void copyResponseToClipboard(Event event) {
        if (StrUtils.isValid(currentResponseText)) {
            FrameworkUtils.toClipboard(currentResponseText);
        }
    }

    public void show() {
        mainWindow.showModal();
    }

    private void onServiceSelected(Event event) {
        String selectedWebservice = mainWindow.serviceComboBox.getSelectedItem();
        mainWindow.sendButton.setEnabled(StrUtils.isValid(selectedWebservice));
        mainWindow.fileImportButton.setEnabled(StrUtils.isValid(selectedWebservice));
        mainWindow.importResponseCheckbox.setVisible(StrUtils.isValid(selectedWebservice) && !truckBOMFoundationWSHelper.isNonImportWebservice(selectedWebservice));
        Session.get().setAttribute(SESSION_KEY_TRUCK_BOM_TEST_SELECTED_SERVICE, mainWindow.serviceComboBox.getSelectedUserObject());
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
        private de.docware.framework.modules.gui.controls.GuiTitle tbfTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel leftplanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel configPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel servicePanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel serviceLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> serviceComboBox;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox importResponseCheckbox;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel rightPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel sendRequestLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel receivedAnswerLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane requestScrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea requestTextarea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane responseScrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea responseTextarea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel sendButtonPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton sendButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton fileImportButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel copyToClipboardButtonPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton copyToClipboardButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            tbfTitle = new de.docware.framework.modules.gui.controls.GuiTitle();
            tbfTitle.setName("tbfTitle");
            tbfTitle.__internal_setGenerationDpi(96);
            tbfTitle.registerTranslationHandler(translationHandler);
            tbfTitle.setScaleForResolution(true);
            tbfTitle.setMinimumWidth(10);
            tbfTitle.setMinimumHeight(50);
            tbfTitle.setTitle("TruckBOM.foundation Webservices");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tbfTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tbfTitleConstraints.setPosition("north");
            tbfTitle.setConstraints(tbfTitleConstraints);
            this.addChild(tbfTitle);
            leftplanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            leftplanel.setName("leftplanel");
            leftplanel.__internal_setGenerationDpi(96);
            leftplanel.registerTranslationHandler(translationHandler);
            leftplanel.setScaleForResolution(true);
            leftplanel.setMinimumWidth(10);
            leftplanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder leftplanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            leftplanel.setLayout(leftplanelLayout);
            configPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            configPanel.setName("configPanel");
            configPanel.__internal_setGenerationDpi(96);
            configPanel.registerTranslationHandler(translationHandler);
            configPanel.setScaleForResolution(true);
            configPanel.setBorderWidth(4);
            configPanel.setTitle("Konfiguration");
            de.docware.framework.modules.gui.layout.LayoutGridBag configPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            configPanel.setLayout(configPanelLayout);
            servicePanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            servicePanel.setName("servicePanel");
            servicePanel.__internal_setGenerationDpi(96);
            servicePanel.registerTranslationHandler(translationHandler);
            servicePanel.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutGridBag servicePanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            servicePanelLayout.setCentered(false);
            servicePanel.setLayout(servicePanelLayout);
            serviceLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            serviceLabel.setName("serviceLabel");
            serviceLabel.__internal_setGenerationDpi(96);
            serviceLabel.registerTranslationHandler(translationHandler);
            serviceLabel.setScaleForResolution(true);
            serviceLabel.setMinimumWidth(10);
            serviceLabel.setMinimumHeight(10);
            serviceLabel.setText("Webservice auswählen:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag serviceLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "n", "b", 0, 0, 4, 0);
            serviceLabel.setConstraints(serviceLabelConstraints);
            servicePanel.addChild(serviceLabel);
            serviceComboBox = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            serviceComboBox.setName("serviceComboBox");
            serviceComboBox.__internal_setGenerationDpi(96);
            serviceComboBox.registerTranslationHandler(translationHandler);
            serviceComboBox.setScaleForResolution(true);
            serviceComboBox.setMinimumWidth(10);
            serviceComboBox.setMinimumHeight(10);
            serviceComboBox.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onServiceSelected(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag serviceComboBoxConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 0.0, "nw", "b", 0, 0, 0, 0);
            serviceComboBox.setConstraints(serviceComboBoxConstraints);
            servicePanel.addChild(serviceComboBox);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag servicePanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "n", "h", 4, 4, 4, 4);
            servicePanel.setConstraints(servicePanelConstraints);
            configPanel.addChild(servicePanel);
            importResponseCheckbox = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            importResponseCheckbox.setName("importResponseCheckbox");
            importResponseCheckbox.__internal_setGenerationDpi(96);
            importResponseCheckbox.registerTranslationHandler(translationHandler);
            importResponseCheckbox.setScaleForResolution(true);
            importResponseCheckbox.setMinimumWidth(10);
            importResponseCheckbox.setMinimumHeight(10);
            importResponseCheckbox.setText("!!Import der Response");
            importResponseCheckbox.setSelected(true);
            importResponseCheckbox.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onImportResponseCheckboxChangeEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag importResponseCheckboxConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 100.0, "n", "h", 8, 4, 4, 4);
            importResponseCheckbox.setConstraints(importResponseCheckboxConstraints);
            configPanel.addChild(importResponseCheckbox);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder configPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            configPanel.setConstraints(configPanelConstraints);
            leftplanel.addChild(configPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder leftplanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            leftplanelConstraints.setPosition("west");
            leftplanel.setConstraints(leftplanelConstraints);
            this.addChild(leftplanel);
            rightPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            rightPanel.setName("rightPanel");
            rightPanel.__internal_setGenerationDpi(96);
            rightPanel.registerTranslationHandler(translationHandler);
            rightPanel.setScaleForResolution(true);
            rightPanel.setMinimumWidth(10);
            rightPanel.setBorderWidth(4);
            rightPanel.setTitle("Webservice Test");
            de.docware.framework.modules.gui.layout.LayoutGridBag rightPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            rightPanel.setLayout(rightPanelLayout);
            sendRequestLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            sendRequestLabel.setName("sendRequestLabel");
            sendRequestLabel.__internal_setGenerationDpi(96);
            sendRequestLabel.registerTranslationHandler(translationHandler);
            sendRequestLabel.setScaleForResolution(true);
            sendRequestLabel.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            sendRequestLabel.setText("Request Body (JSON) [optional]");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sendRequestLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "h", 0, 8, 0, 0);
            sendRequestLabel.setConstraints(sendRequestLabelConstraints);
            rightPanel.addChild(sendRequestLabel);
            receivedAnswerLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            receivedAnswerLabel.setName("receivedAnswerLabel");
            receivedAnswerLabel.__internal_setGenerationDpi(96);
            receivedAnswerLabel.registerTranslationHandler(translationHandler);
            receivedAnswerLabel.setScaleForResolution(true);
            receivedAnswerLabel.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            receivedAnswerLabel.setText("Response");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag receivedAnswerLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "w", "h", 0, 8, 0, 0);
            receivedAnswerLabel.setConstraints(receivedAnswerLabelConstraints);
            rightPanel.addChild(receivedAnswerLabel);
            requestScrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            requestScrollpane.setName("requestScrollpane");
            requestScrollpane.__internal_setGenerationDpi(96);
            requestScrollpane.registerTranslationHandler(translationHandler);
            requestScrollpane.setScaleForResolution(true);
            requestScrollpane.setMinimumWidth(10);
            requestScrollpane.setMinimumHeight(10);
            requestScrollpane.setBorderWidth(1);
            requestScrollpane.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            requestTextarea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            requestTextarea.setName("requestTextarea");
            requestTextarea.__internal_setGenerationDpi(96);
            requestTextarea.registerTranslationHandler(translationHandler);
            requestTextarea.setScaleForResolution(true);
            requestTextarea.setPaddingTop(8);
            requestTextarea.setPaddingLeft(8);
            requestTextarea.setPaddingRight(8);
            requestTextarea.setPaddingBottom(8);
            requestTextarea.setLineWrap(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder requestTextareaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            requestTextarea.setConstraints(requestTextareaConstraints);
            requestScrollpane.addChild(requestTextarea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag requestScrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 100.0, "w", "b", 0, 4, 4, 2);
            requestScrollpane.setConstraints(requestScrollpaneConstraints);
            rightPanel.addChild(requestScrollpane);
            responseScrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            responseScrollpane.setName("responseScrollpane");
            responseScrollpane.__internal_setGenerationDpi(96);
            responseScrollpane.registerTranslationHandler(translationHandler);
            responseScrollpane.setScaleForResolution(true);
            responseScrollpane.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            responseScrollpane.setBorderWidth(1);
            responseScrollpane.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            responseTextarea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            responseTextarea.setName("responseTextarea");
            responseTextarea.__internal_setGenerationDpi(96);
            responseTextarea.registerTranslationHandler(translationHandler);
            responseTextarea.setScaleForResolution(true);
            responseTextarea.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            responseTextarea.setPaddingTop(8);
            responseTextarea.setPaddingLeft(8);
            responseTextarea.setPaddingRight(8);
            responseTextarea.setPaddingBottom(8);
            responseTextarea.setEditable(false);
            responseTextarea.setLineWrap(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder responseTextareaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            responseTextarea.setConstraints(responseTextareaConstraints);
            responseScrollpane.addChild(responseTextarea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag responseScrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 100.0, "c", "b", 0, 2, 4, 0);
            responseScrollpane.setConstraints(responseScrollpaneConstraints);
            rightPanel.addChild(responseScrollpane);
            sendButtonPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            sendButtonPanel.setName("sendButtonPanel");
            sendButtonPanel.__internal_setGenerationDpi(96);
            sendButtonPanel.registerTranslationHandler(translationHandler);
            sendButtonPanel.setScaleForResolution(true);
            sendButtonPanel.setMinimumWidth(10);
            sendButtonPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag sendButtonPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            sendButtonPanel.setLayout(sendButtonPanelLayout);
            sendButton = new de.docware.framework.modules.gui.controls.GuiButton();
            sendButton.setName("sendButton");
            sendButton.__internal_setGenerationDpi(96);
            sendButton.registerTranslationHandler(translationHandler);
            sendButton.setScaleForResolution(true);
            sendButton.setEnabled(false);
            sendButton.setMnemonicEnabled(true);
            sendButton.setText("Request senden");
            sendButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    sendRequest(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sendButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 8, 8, 8, 4);
            sendButton.setConstraints(sendButtonConstraints);
            sendButtonPanel.addChild(sendButton);
            fileImportButton = new de.docware.framework.modules.gui.controls.GuiButton();
            fileImportButton.setName("fileImportButton");
            fileImportButton.__internal_setGenerationDpi(96);
            fileImportButton.registerTranslationHandler(translationHandler);
            fileImportButton.setScaleForResolution(true);
            fileImportButton.setMinimumWidth(100);
            fileImportButton.setMinimumHeight(10);
            fileImportButton.setEnabled(false);
            fileImportButton.setMnemonicEnabled(true);
            fileImportButton.setText("!!Response aus Datei importieren");
            fileImportButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    doFileImport(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag fileImportButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 4, 8, 8);
            fileImportButton.setConstraints(fileImportButtonConstraints);
            sendButtonPanel.addChild(fileImportButton);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sendButtonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 0);
            sendButtonPanel.setConstraints(sendButtonPanelConstraints);
            rightPanel.addChild(sendButtonPanel);
            copyToClipboardButtonPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            copyToClipboardButtonPanel.setName("copyToClipboardButtonPanel");
            copyToClipboardButtonPanel.__internal_setGenerationDpi(96);
            copyToClipboardButtonPanel.registerTranslationHandler(translationHandler);
            copyToClipboardButtonPanel.setScaleForResolution(true);
            copyToClipboardButtonPanel.setMinimumWidth(10);
            copyToClipboardButtonPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag copyToClipboardButtonPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            copyToClipboardButtonPanel.setLayout(copyToClipboardButtonPanelLayout);
            copyToClipboardButton = new de.docware.framework.modules.gui.controls.GuiButton();
            copyToClipboardButton.setName("copyToClipboardButton");
            copyToClipboardButton.__internal_setGenerationDpi(96);
            copyToClipboardButton.registerTranslationHandler(translationHandler);
            copyToClipboardButton.setScaleForResolution(true);
            copyToClipboardButton.setEnabled(false);
            copyToClipboardButton.setMnemonicEnabled(true);
            copyToClipboardButton.setText("Response in Zwischenablage kopieren");
            copyToClipboardButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    copyResponseToClipboard(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag copyToClipboardButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "h", 8, 8, 8, 8);
            copyToClipboardButton.setConstraints(copyToClipboardButtonConstraints);
            copyToClipboardButtonPanel.addChild(copyToClipboardButton);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag copyToClipboardButtonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 0);
            copyToClipboardButtonPanel.setConstraints(copyToClipboardButtonPanelConstraints);
            rightPanel.addChild(copyToClipboardButtonPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder rightPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            rightPanel.setConstraints(rightPanelConstraints);
            this.addChild(rightPanel);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}
