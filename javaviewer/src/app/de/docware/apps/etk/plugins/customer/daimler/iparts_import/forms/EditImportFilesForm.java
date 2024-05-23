/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileOpenValidatorForExtensions;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.file.DWFile;

import java.io.File;
import java.util.*;

/**
 * Import-Dialog für mehrere Dateilisten.
 */
public class EditImportFilesForm {

    protected EtkProject project;
    protected FilesImporterInterface importerInterface;
    protected FrameworkThread importThread;

    protected Map<FilesImporterFileListType, GuiCheckbox> removeExistingDataCheckBoxesMap = new LinkedHashMap<FilesImporterFileListType, GuiCheckbox>();
    protected Map<FilesImporterFileListType, GuiFileChooserTextfield> fileChoosersMap = new LinkedHashMap<FilesImporterFileListType, GuiFileChooserTextfield>();
    protected Map<FilesImporterFileListType, List<DWFile>> importFilesMap = new LinkedHashMap<FilesImporterFileListType, List<DWFile>>();

    protected GuiButtonOnPanel startButton;

    protected GuiLabel progressLabel;

    /**
     * Erzeugt eine Instanz von EditImportFilesForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditImportFilesForm(EtkProject project, FilesImporterInterface importerInterface) {
        this.project = project;
        this.importerInterface = importerInterface;
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.setTitle(TranslationHandler.translate("!!Importiere %1...", importerInterface.getImportName(TranslationHandler.getUiLanguage())));

        // Controls für die Import-Dateilisten hinzufügen
        int gridy = 1;
        for (FilesImporterFileListType importFileType : importerInterface.getImportFileTypes()) {
            String fileListName = importFileType.getFileListName(TranslationHandler.getUiLanguage());
            GuiPanel importFilesPanel = new GuiPanel(fileListName);
            importFilesPanel.setLayout(new LayoutGridBag());

            // Checkbox für das Löschen vorhandener Daten
            boolean removeExistingDataAllowed = importFileType.isRemoveExistingDataSelectable();
            if (removeExistingDataAllowed) {
                GuiCheckbox removeExistingDataCheckBox = new GuiCheckbox("!!Alle vorhandenen Daten löschen", importFileType.isRemoveExistingDataDefaultValue());
                removeExistingDataCheckBoxesMap.put(importFileType, removeExistingDataCheckBox);
                removeExistingDataCheckBox.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                                                 ConstraintsGridBag.FILL_NONE, 8, 8, 0, 8));
                importFilesPanel.addChild(removeExistingDataCheckBox);
            }

            // FileChooserTextField für die Importdateien
            GuiFileChooserTextfield fileChooserTextfield = new GuiFileChooserTextfield();
            fileChoosersMap.put(importFileType, fileChooserTextfield);
            fileChooserTextfield.setApproveButtonText(TranslationHandler.translate("!!%1 auswählen", fileListName));
            fileChooserTextfield.setPurpose(FileChooserPurpose.OPEN);
            fileChooserTextfield.setEditable(false);
            fileChooserTextfield.setMinimumWidth(200);
            fileChooserTextfield.setMultiSelectionMode(true);
            boolean serverMode = importFileType.isServerMode();
            fileChooserTextfield.setServerMode(serverMode);

            // Dateifilter setzen
            List<String[]> fileFilters = new ArrayList<String[]>();
            for (String validFileExtension : importFileType.getValidFileExtensions()) {
                if (validFileExtension.equals("*")) {
                    fileFilters.add(new String[]{ TranslationHandler.translate("Alle Dateien(*.*)"), "*.*" });
                } else {
                    fileFilters.add(new String[]{ TranslationHandler.translate("%1-Dateien (*.%1)", validFileExtension), "*." + validFileExtension });
                }
            }
            fileChooserTextfield.setChoosableFileFilters(fileFilters);

            if (serverMode) {
                DWFile saveDir = iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_IMPORT_FILE_SERVER_DIR);
                if (!saveDir.isDirectory()) {
                    MessageDialog.showError(TranslationHandler.translate("!!Das Verzeichnis existiert nicht:") + " " + saveDir.getPath());
                } else {
                    Map<String, File> importDirs = new HashMap<String, File>(1);
                    importDirs.put("!!Importverzeichnis für große Dateien auf dem Server", saveDir);
                    fileChooserTextfield.setRootDirectories(importDirs);
                }
            }

            fileChooserTextfield.setConstraints(new ConstraintsGridBag(0, removeExistingDataAllowed ? 1 : 0,
                                                                       1, 1, 100, 0,
                                                                       ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                                                                       removeExistingDataAllowed ? 4 : 8, 8, 8, 8));
            fileChooserTextfield.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    filesChanged(event);
                }
            });

            // Validator für die gültigen Dateiendungen
            String[] validFileExtensions = importFileType.getValidFileExtensions();
            if ((validFileExtensions != null) && (validFileExtensions.length > 0)) {
                GuiFileOpenValidatorForExtensions fileExtensionsValidator = new GuiFileOpenValidatorForExtensions(validFileExtensions);
                fileChooserTextfield.addFileOpenValidator(fileExtensionsValidator);
            }

            importFilesPanel.addChild(fileChooserTextfield);

            importFilesPanel.setConstraints(new ConstraintsGridBag(0, gridy, 1, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                                   ConstraintsGridBag.FILL_HORIZONTAL, 8, 8, 8, 8));
            filesPanel.filesImporterPanel.addChild(importFilesPanel);
            gridy++;
        }

        filesPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        messagesPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.addChild(filesPanel);

        final GuiButtonOnPanel cancelButton = mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, true);

        startButton = mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.START, true);
        startButton.setModalResult(ModalResult.NONE);
        startButton.setEnabled(false);
        startButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                mainWindow.removeChild(filesPanel);
                mainWindow.addChild(messagesPanel);
                mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.START, false);

                // Import in einem Thread durchführen
                final Session session = Session.get();
                importThread = session.startChildThread(new FrameworkRunnable() {
                    @Override
                    public void run(final FrameworkThread thread) {
                        // Abbrechen-Button bricht nun den Import ab
                        session.invokeThreadSafe(() -> cancelButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                            @Override
                            public void fire(Event event) {
                                if (thread.isRunning()) {
                                    importerInterface.cancelImport("!!Abbruch durch Benutzer!");
                                    thread.cancel();
                                }
                                mainWindow.setVisible(false);
                            }
                        }));

                        // Aktive Änderungssets temporär deaktivieren
                        project.executeWithoutActiveChangeSets(new Runnable() {
                            @Override
                            public void run() {
                                importFiles();
                            }
                        }, true);

                        session.invokeThreadSafe(new Runnable() {
                            @Override
                            public void run() {
                                mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, false);
                                mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CLOSE, true);
                            }
                        });
                    }
                });
            }
        });

        // bei vielen FilesImporterFileListTypes das Fenster vertikal vergrößern (+140px für den Overhead durch Fenstertitel usw.)
        mainWindow.setHeight(Math.max(mainWindow.getHeight(), filesPanel.filesImporterPanel.getPreferredHeight() + 140));
    }

    public GuiWindow getGui() {
        return mainWindow;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        if (importThread != null) { // Warten bis der Import-Thread beendet ist
            if (!importThread.waitFinished() || !importerInterface.isFinished()) {
                importerInterface.cancelImport("!!Import-Thread wurde frühzeitig beendet");
            }
        }
        return modalResult;
    }

    /**
     * Liefert alle Importdateien für den übergebenen Import-Dateilisten-Typ zurück.
     *
     * @param importFileType
     * @return Kann auch {@code null} sein
     */
    public List<DWFile> getImportFiles(FilesImporterFileListType importFileType) {
        return importFilesMap.get(importFileType);
    }

    protected void importFiles() {
        // MessageLog für die Importer-Ausgaben
        EtkMessageLog messageLog = new EtkMessageLog();
        final Session session = Session.get();
        messageLog.addMessageEventListener(new MessageEvent() {
            @Override
            public void fireEvent(final MessageEventData event) {
                session.invokeThreadSafe(new Runnable() {
                    @Override
                    public void run() {
                        if (!event.isOnlyForLogFile()) {
                            messagesPanel.messagesTextArea.appendLine(event.getFormattedMessage(TranslationHandler.getUiLanguage()));
                            String message = messagesPanel.messagesTextArea.getText();
                            int cursorPosition = message.length();
                            int lastNewLineIndex = message.lastIndexOf('\n');
                            if (lastNewLineIndex >= 0) {
                                cursorPosition = Math.min(cursorPosition, lastNewLineIndex + 1);
                            }
                            messagesPanel.messagesTextArea.setCursorPosition(cursorPosition);
                        }
                    }
                });
            }
        });

        messageLog.addProgressEventListener(new ProgressEvent() {
            @Override
            public void fireEvent(final ProgressEventData event) {
                session.invokeThreadSafe(new Runnable() {
                    @Override
                    public void run() {
                        messagesPanel.progressbar.setVisible(event.getMaxPosition() > 0);
                        if (messagesPanel.progressbar.isVisible()) {
                            if (!messagesPanel.panelProgressbar.getChildren().contains(messagesPanel.progressbar)) {
                                messagesPanel.panelProgressbar.removeAllChildren();
                                messagesPanel.panelProgressbar.addChild(messagesPanel.progressbar);
                            }
                            messagesPanel.progressbar.setMaxValue(event.getMaxPosition());
                            messagesPanel.progressbar.setValue(event.getPosition());
                        } else {
                            if (!event.getMessage().isEmpty()) {
                                if (messagesPanel.panelProgressbar.getChildren().contains(messagesPanel.progressbar)) {
                                    progressLabel = new GuiLabel(event.getMessage());
                                    ConstraintsBorder constraints = new ConstraintsBorder();
                                    progressLabel.setConstraints(constraints);
                                    messagesPanel.panelProgressbar.removeAllChildren();
                                    messagesPanel.panelProgressbar.addChild(progressLabel);
                                } else {
                                    progressLabel.setText(event.getMessage());
                                    if (!progressLabel.isVisible()) {
                                        progressLabel.setVisible(true);
                                    }
                                }
                            } else {
                                if (progressLabel != null) {
                                    progressLabel.setVisible(false);
                                }
                            }
                        }
                    }
                });
            }
        });

        boolean success = importerInterface.initImport(messageLog);
        if(!success) {
            importerInterface.cancelImport(TranslationHandler.translate("!!Import abgebrochen"));
        } else {

            try {
                for (Map.Entry<FilesImporterFileListType, GuiFileChooserTextfield> importFilesEntry : fileChoosersMap.entrySet()) {
                    FilesImporterFileListType importFileType = importFilesEntry.getKey();
                    boolean removeAllExistingData = importFileType.isRemoveExistingDataDefaultValue();
                    GuiCheckbox removeAllExistingDataCheckBox = removeExistingDataCheckBoxesMap.get(importFileType);
                    if (removeAllExistingDataCheckBox != null) {
                        removeAllExistingData = removeAllExistingDataCheckBox.isSelected();
                    }

                    if (!importerInterface.importFiles(importFileType, importFilesEntry.getValue().getSelectedFiles(), removeAllExistingData)) {
                        break;
                    }
                }
            } finally {
                success = importerInterface.finishImport();
            }
        }

        // Titel ändern, je nach Status, ob es erfogreich war
        final String resultTitle;
        if (success) {
            resultTitle = TranslationHandler.translate("!!Import erfolgreich abgeschlossen");
        } else {
            resultTitle = TranslationHandler.translate("!!Import abgebrochen");
        }

        session.invokeThreadSafe(new Runnable() {
            @Override
            public void run() {
                messagesPanel.messagesTitle.setTitle(resultTitle);
            }
        });
    }

    /**
     * Wenn das Eingabefeld einen Wert enthalten muss, aber leer ist, darf der Start-Button nicht aktiviert werden.
     * Zusätzlich muss auch im Falle: "alle können leer sein" mindestens eine Datei ausgewählt sein, damit etwas gestartet werden kann.
     *
     * @param event
     */
    protected void filesChanged(Event event) {
        boolean importFilesEmpty = false;
        Integer selectedCounter = 0;

        for (Map.Entry<FilesImporterFileListType, GuiFileChooserTextfield> fileType : fileChoosersMap.entrySet()) {
            if (fileType.getValue().getSelectedFiles().isEmpty()) {
                if (fileType.getKey().getMustContainValue()) {
                    importFilesEmpty = true;
                }
            } else {
                selectedCounter++;
            }
        }

        boolean startButtonEnabled = !importFilesEmpty && (selectedCounter >= 1);
        startButton.setEnabled(startButtonEnabled);
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
        filesPanel = new FilesPanelClass(translationHandler);
        filesPanel.__internal_setGenerationDpi(96);
        messagesPanel = new MessagesPanelClass(translationHandler);
        messagesPanel.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setMinimumWidth(300);
            this.setMinimumHeight(300);
            this.setVisible(false);
            this.setName("EditImportFilesDialog");
            this.setWidth(600);
            this.setHeight(500);
            this.setTitle("!!Importiere %1...");
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainWindowLayout.setVerticalGap(8);
            this.setLayout(mainWindowLayout);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CUSTOM);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected FilesPanelClass filesPanel;

    private class FilesPanelClass extends de.docware.framework.modules.gui.controls.GuiPanel {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle filesTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane filesScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel filesImporterPanel;

        private FilesPanelClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutGridBag filesPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            filesPanelLayout.setCentered(false);
            this.setLayout(filesPanelLayout);
            filesTitle = new de.docware.framework.modules.gui.controls.GuiTitle();
            filesTitle.setName("filesTitle");
            filesTitle.__internal_setGenerationDpi(96);
            filesTitle.registerTranslationHandler(translationHandler);
            filesTitle.setScaleForResolution(true);
            filesTitle.setMinimumWidth(10);
            filesTitle.setMinimumHeight(50);
            filesTitle.setTitle("!!Dateien auswählen...");
            filesTitle.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgPlugin_iParts_ImportFiles"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag filesTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            filesTitle.setConstraints(filesTitleConstraints);
            this.addChild(filesTitle);
            filesScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            filesScrollPane.setName("filesScrollPane");
            filesScrollPane.__internal_setGenerationDpi(96);
            filesScrollPane.registerTranslationHandler(translationHandler);
            filesScrollPane.setScaleForResolution(true);
            filesScrollPane.setMinimumWidth(10);
            filesScrollPane.setMinimumHeight(10);
            filesImporterPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            filesImporterPanel.setName("filesImporterPanel");
            filesImporterPanel.__internal_setGenerationDpi(96);
            filesImporterPanel.registerTranslationHandler(translationHandler);
            filesImporterPanel.setScaleForResolution(true);
            filesImporterPanel.setMinimumWidth(10);
            filesImporterPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag filesImporterPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            filesImporterPanelLayout.setCentered(false);
            filesImporterPanel.setLayout(filesImporterPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder filesImporterPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            filesImporterPanel.setConstraints(filesImporterPanelConstraints);
            filesScrollPane.addChild(filesImporterPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag filesScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 100.0, "c", "b", 0, 0, 0, 0);
            filesScrollPane.setConstraints(filesScrollPaneConstraints);
            this.addChild(filesScrollPane);
        }

    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MessagesPanelClass messagesPanel;

    private class MessagesPanelClass extends de.docware.framework.modules.gui.controls.GuiPanel {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle messagesTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel messagesLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane messagesScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea messagesTextArea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelProgressbar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiProgressBar progressbar;

        private MessagesPanelClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutGridBag messagesPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            messagesPanelLayout.setCentered(false);
            this.setLayout(messagesPanelLayout);
            messagesTitle = new de.docware.framework.modules.gui.controls.GuiTitle();
            messagesTitle.setName("messagesTitle");
            messagesTitle.__internal_setGenerationDpi(96);
            messagesTitle.registerTranslationHandler(translationHandler);
            messagesTitle.setScaleForResolution(true);
            messagesTitle.setMinimumWidth(10);
            messagesTitle.setMinimumHeight(50);
            messagesTitle.setTitle("!!Import läuft...");
            messagesTitle.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgPlugin_iParts_ImportFiles"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag messagesTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            messagesTitle.setConstraints(messagesTitleConstraints);
            this.addChild(messagesTitle);
            messagesLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            messagesLabel.setName("messagesLabel");
            messagesLabel.__internal_setGenerationDpi(96);
            messagesLabel.registerTranslationHandler(translationHandler);
            messagesLabel.setScaleForResolution(true);
            messagesLabel.setMinimumWidth(10);
            messagesLabel.setMinimumHeight(10);
            messagesLabel.setText("!!Meldungen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag messagesLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "w", "n", 8, 8, 0, 8);
            messagesLabel.setConstraints(messagesLabelConstraints);
            this.addChild(messagesLabel);
            messagesScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            messagesScrollPane.setName("messagesScrollPane");
            messagesScrollPane.__internal_setGenerationDpi(96);
            messagesScrollPane.registerTranslationHandler(translationHandler);
            messagesScrollPane.setScaleForResolution(true);
            messagesScrollPane.setMinimumWidth(10);
            messagesScrollPane.setMinimumHeight(10);
            messagesScrollPane.setBorderWidth(1);
            messagesScrollPane.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            messagesTextArea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            messagesTextArea.setName("messagesTextArea");
            messagesTextArea.__internal_setGenerationDpi(96);
            messagesTextArea.registerTranslationHandler(translationHandler);
            messagesTextArea.setScaleForResolution(true);
            messagesTextArea.setMinimumWidth(200);
            messagesTextArea.setMinimumHeight(100);
            messagesTextArea.setEditable(false);
            messagesTextArea.setScrollToVisible(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder messagesTextAreaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            messagesTextArea.setConstraints(messagesTextAreaConstraints);
            messagesScrollPane.addChild(messagesTextArea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag messagesScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 100.0, 100.0, "c", "b", 8, 8, 0, 8);
            messagesScrollPane.setConstraints(messagesScrollPaneConstraints);
            this.addChild(messagesScrollPane);
            panelProgressbar = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelProgressbar.setName("panelProgressbar");
            panelProgressbar.__internal_setGenerationDpi(96);
            panelProgressbar.registerTranslationHandler(translationHandler);
            panelProgressbar.setScaleForResolution(true);
            panelProgressbar.setMinimumWidth(10);
            panelProgressbar.setMinimumHeight(16);
            panelProgressbar.setMaximumHeight(16);
            de.docware.framework.modules.gui.layout.LayoutBorder panelProgressbarLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelProgressbar.setLayout(panelProgressbarLayout);
            progressbar = new de.docware.framework.modules.gui.controls.GuiProgressBar();
            progressbar.setName("progressbar");
            progressbar.__internal_setGenerationDpi(96);
            progressbar.registerTranslationHandler(translationHandler);
            progressbar.setScaleForResolution(true);
            progressbar.setMinimumWidth(10);
            progressbar.setMinimumHeight(10);
            progressbar.setVisible(false);
            progressbar.setFractionDigits(2);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder progressbarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            progressbar.setConstraints(progressbarConstraints);
            panelProgressbar.addChild(progressbar);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelProgressbarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 5, 1, 1, 100.0, 0.0, "c", "b", 0, 8, 0, 8);
            panelProgressbar.setConstraints(panelProgressbarConstraints);
            this.addChild(panelProgressbar);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
//</editor-fold>
}