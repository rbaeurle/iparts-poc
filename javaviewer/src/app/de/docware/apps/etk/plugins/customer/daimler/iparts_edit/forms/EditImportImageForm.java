/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.drawing.EtkImageSettings;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageEvent;
import de.docware.apps.etk.base.project.base.MessageEventData;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataHotspot;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataHotspotList;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.drawing.ImageVariant;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsSvgOutlineHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.ImageFileImporter;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.filechooser.AbstractGuiFileOpenValidator;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.viewer.*;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.server.HttpServerException;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Import-Dialog für neue Zeichnungen.
 */
public class EditImportImageForm {

    protected EtkProject project;
    protected ImageFileImporter imageFileImporter;
    protected DWFile imageFile;
    protected DWFile hotspotFile;

    protected GuiButtonOnPanel startButton;
    protected GuiViewerImageInterface imagePreview;
    protected EtkImageSettings imageSettings;

    /**
     * Erzeugt eine Instanz von EditImportImageForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditImportImageForm(EtkProject project, ImageFileImporter imageFileImporter) {
        $$internalCreateGui$$(null);
        this.project = project;
        this.imageFileImporter = imageFileImporter;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.imageFileChooser.setPurpose(FileChooserPurpose.OPEN);

        // Validator für eine Zeichnung mit optionaler SEN-Datei
        mainWindow.imageFileChooser.addFileOpenValidator(new AbstractGuiFileOpenValidator() {
            @Override
            protected String isFileSelectionValid(List<DWFile> selectedFiles, boolean uploadStarted) {
                String errorText = "";
                if (selectedFiles.size() > 2) {
                    errorText = TranslationHandler.translate("!!Die Auswahl ist auf eine Zeichnung und eine optionale (X-)SEN-Datei mit identischem Dateinamen für die Hotspots beschränkt.") + '\n';

                    // zunächst alle Dateien außer Zeichnungen und (X-)SEN-Dateien entfernen
                    while (!selectedFiles.isEmpty() && !isValidDrawing(selectedFiles.get(0).getName()) && !MimeTypes.isSenFile(selectedFiles.get(0).getName())) {
                        selectedFiles.remove(0);
                    }

                    // jetzt die Dateiauswahl auf max. 2 Einträge beschränken
                    while (selectedFiles.size() > 2) {
                        selectedFiles.remove(2);
                    }
                }

                String imageFileName = null;
                String senFileName = null;
                List<DWFile> originalSelectedFileNames = new ArrayList<DWFile>(selectedFiles);
                for (DWFile selectedFile : originalSelectedFileNames) {
                    String selectedFileName = selectedFile.getName();
                    if (isValidDrawing(selectedFileName)) {
                        if (imageFileName != null) {
                            errorText = TranslationHandler.translate("!!Die Auswahl ist auf eine Zeichnung und eine optionale (X-)SEN-Datei mit identischem Dateinamen für die Hotspots beschränkt.") + '\n';
                            selectedFiles.remove(selectedFile);
                        } else {
                            imageFileName = selectedFileName;
                        }
                    } else if (MimeTypes.isSenFile(selectedFileName)) {
                        if (senFileName != null) {
                            errorText = TranslationHandler.translate("!!Die Auswahl ist auf eine Zeichnung und eine optionale (X-)SEN-Datei mit identischem Dateinamen für die Hotspots beschränkt.") + '\n';
                            selectedFiles.remove(selectedFile);
                        } else {
                            senFileName = selectedFileName;
                        }
                    } else {
                        errorText += TranslationHandler.translate("!!Die Datei \"%1\" ist keine gültige Zeichnung oder optionale (X-)SEN-Datei mit Hotspots.", selectedFileName) + '\n';
                        selectedFiles.remove(selectedFile);
                    }

                    if ((imageFileName != null) && (senFileName != null)) {
                        boolean invalidSenFile = false;
                        if (MimeTypes.isSvgFile(imageFileName)) {
                            errorText += TranslationHandler.translate("!!SVG-Zeichnungen benötigen keine (X-)SEN-Datei für die Hotspots, da diese direkt aus der SVG-Zeichnung extrahiert werden.") + '\n';
                            invalidSenFile = true;
                        } else if (!DWFile.extractFileName(imageFileName, false).equalsIgnoreCase(DWFile.extractFileName(senFileName, false))) {
                            errorText += TranslationHandler.translate("!!Die (X-)SEN-Datei \"%1\" passt nicht zur Datei \"%2\", da die Dateinamen identisch sein müssen.", senFileName, imageFileName) + '\n';
                            invalidSenFile = true;
                        }

                        if (invalidSenFile) {
                            if (MimeTypes.isSenFile(selectedFileName)) {
                                selectedFiles.remove(selectedFile);
                            } else {
                                selectedFiles.remove(0); // SEN-Datei ist auf Index 0 und selectedFile ist die Zeichnung
                            }
                        }
                    }
                }

                if (uploadStarted && (imageFileName == null) && !selectedFiles.isEmpty()) {
                    errorText += TranslationHandler.translate("!!Es wurde keine Zeichnung ausgewählt.");
                    selectedFiles.clear();
                }

                if (!errorText.isEmpty()) {
                    return errorText.substring(0, errorText.length() - 1); // letztes \n entfernen
                } else {
                    return null;
                }
            }
        });

        mainWindow.usageComboBox.addItem(ImageVariant.iv2D, ImageVariant.iv2D.getDisplayText());
        mainWindow.usageComboBox.addItem(ImageVariant.ivSVG, ImageVariant.ivSVG.getDisplayText());
        mainWindow.usageComboBox.addItem(ImageVariant.iv3D, ImageVariant.iv3D.getDisplayText());
        mainWindow.usageComboBox.addItem(ImageVariant.ivPrint, ImageVariant.ivPrint.getDisplayText());
        mainWindow.usageComboBox.setSelectedIndex(0);

        mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, true);
        startButton = mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.START, true);
        startButton.setModalResult(ModalResult.NONE);
        startButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                importImage();
            }
        });

        // EventListener und explizier Aufruf von imageFileChanged() notwendig, weil Dialog erst jetzt sichtbar ist und somit
        // auch jetzt erst die Zeichnungsvorschau unter JEE eine Größe hat (wichtig für die Anzeige vom Overview-Panel)
        mainWindow.addEventListener(new EventListener(Event.OPENED_EVENT) {
            @Override
            public void fire(Event event) {
                imageFileChanged(null);
            }
        });

        nameChanged(null);

        ThemeManager.get().render(mainWindow);
        mainWindow.pack();
    }

    public GuiWindow getGui() {
        return mainWindow;
    }

    public ModalResult showModal() {
        mainWindow.imageFileChooser.showDialog();
        if (!mainWindow.imageFileChooser.getSelectedFiles().isEmpty()) {
            return mainWindow.showModal();
        } else {
            return ModalResult.CANCEL;
        }
    }

    /**
     * Liefert die ausgewählte Zeichnungsdatei zurück (diese sollte nach dem Import unter JEE gleich wieder gelöscht werden).
     *
     * @return
     */
    public DWFile getImageFile() {
        return imageFile;
    }

    /**
     * Liefert die ausgewählte Hotspotdatei zurück (diese sollte nach dem Import unter JEE gleich wieder gelöscht werden).
     *
     * @return Kann auch {@code null} sein.
     */
    public DWFile getHotspotFile() {
        return hotspotFile;
    }

    /**
     * Liefert die eingegebene Zeichnungsnummer zurück.
     *
     * @return
     */
    public String getImageNumber() {
        return mainWindow.imageNumberTextfield.getText();
    }

    /**
     * Liefert die Verwendung für diese neue Zeichnung zurück.
     *
     * @return
     */
    public ImageVariant getImageUsage() {
        return mainWindow.usageComboBox.getSelectedUserObject();
    }

    protected boolean isValidDrawing(String fileName) {
        return MimeTypes.isFramework2dImageFile(fileName) || MimeTypes.isSvgFile(fileName) || MimeTypes.is3dImageFile(fileName);
    }

    /**
     * Imagesetting ermitteln, falls noch nicht geladen, tue das
     */
    protected EtkImageSettings getImageSettings() {
        if (imageSettings == null) {
            imageSettings = new EtkImageSettings();
            imageSettings.load(project.getConfig());
        }
        return imageSettings;
    }

    protected void fillHotspots(byte[] svgContent) {
        EtkEbenenDaten ebene = project.getConfig().getPartsDescription().getEbene("");

        imagePreview.resetLinks();

        EtkDataHotspotList hotspotList = null;
        boolean isSVG = MimeTypes.isSvgFile(getImageFile().getName());
        if (((getHotspotFile() != null) && !MimeTypes.isExtImageExtension(getImageFile().getName()) && MimeTypes.isSenFile(getHotspotFile().getName()))
            || isSVG) {
            // MessageLog für die Importer-Ausgaben
            EtkMessageLog messageLog = new EtkMessageLog();
            final List<String> warningMessages = new ArrayList<>();
            messageLog.addMessageEventListener(new MessageEvent() {
                @Override
                public void fireEvent(final MessageEventData event) {
                    if ((event.getMessageLogType() == MessageLogType.tmlWarning) || (event.getMessageLogType() == MessageLogType.tmlError)) {
                        warningMessages.add(event.getFormattedMessage(TranslationHandler.getUiLanguage()));
                    }
                }
            });

            // Neue Hotspots aus der Datei auslesen
            DWFile file = null;
            if (isSVG) {
                if (svgContent != null) {
                    // SVG wird durch den iPartsSvgOutlineHelper beim Aufruf von initEtkDataPool() verändert -> ist in svgContent enthalten
                    hotspotList = imageFileImporter.loadHotspotsFromByteArray(svgContent, true, getImageNumber(), "", "",
                                                                              getImageUsage(), messageLog);
                } else {
                    file = getImageFile(); // SVG-Datei
                }
            } else {
                file = getHotspotFile(); // SEN-Datei
            }
            if (file != null) {
                hotspotList = imageFileImporter.loadHotspotsFromFile(file, getImageNumber(), "", "", getImageUsage(), messageLog);
            }

            if (!warningMessages.isEmpty()) {
                MessageDialog.showWarning(warningMessages);
            }
        }

        if (hotspotList != null) {
            for (EtkDataHotspot hotspot : hotspotList.getAsList()) {
                Rectangle linkRect = new Rectangle(hotspot.getLeft(), hotspot.getTop(), hotspot.getRight() - hotspot.getLeft() + 1,
                                                   hotspot.getBottom() - hotspot.getTop() + 1);
                linkRect = imagePreview.modifyLinkRect(linkRect, hotspot.getKey(), hotspot.getKeyVer());
                GuiViewerLink link = AssemblyImageForm.createLinkWithDefaultColorsAndStyle(hotspot.getKey(), hotspot.getKeyVer(),
                                                                                           hotspot.getKey(), hotspot.getHotspotType(),
                                                                                           hotspot.getExtInfo(), linkRect,
                                                                                           ebene);

                imagePreview.addLink(link);
            }
        }
    }

    protected void importImage() {
        // MessageLog für die Importer-Ausgaben
        EtkMessageLog messageLog = new EtkMessageLog();
        final List<String> warningMessages = new DwList<>();
        messageLog.addMessageEventListener(new MessageEvent() {
            @Override
            public void fireEvent(final MessageEventData event) {
                if ((event.getMessageLogType() == MessageLogType.tmlWarning) || (event.getMessageLogType() == MessageLogType.tmlError)) {
                    warningMessages.add(event.getFormattedMessage(TranslationHandler.getUiLanguage()));
                }
            }
        });

        boolean importOK = imageFileImporter.initImport(messageLog);
        PoolId poolId = new PoolId(getImageNumber(), "", "", getImageUsage().getUsage());
        EtkDataPool pool = EtkDataObjectFactory.createDataPool();
        pool.init(project);
        boolean newImagePool = !pool.loadFromDB(poolId);
        if (importOK && !newImagePool) {
            if (MessageDialog.showYesNo(TranslationHandler.translate("!!Für die Zeichnungsnummer \"%1\" existiert bereits eine Zeichnung für die Verwendung %2.",
                                                                     getImageNumber(), TranslationHandler.translate(getImageUsage().getDisplayText())) + '\n' +
                                        TranslationHandler.translate("!!Soll die Zeichnung überschrieben werden?")) != ModalResult.YES) {
                imageFileImporter.cancelImport();
                importOK = false;
            }
        }

        if (importOK) {
            importOK = imageFileImporter.importImageFromFile(getImageFile(), getHotspotFile(), getImageNumber(), "", "",
                                                             getImageUsage(), true, false, true).isSuccessful();
        }
        imageFileImporter.finishImport(false);
        if (!warningMessages.isEmpty()) {
            MessageDialog.showWarning(warningMessages);
        }
        if (importOK) {
            mainWindow.setModalResult(ModalResult.OK);
            mainWindow.setVisible(false);

            // Zukünftig muss hier evtl. auch ein DataChangedEvent gefeuert oder der iPartsDataChangedEventByEdit in
            // Stücklisten und Modulbearbeitung ausgewertet werden
            iPartsDataChangedEventByEdit.Action action = newImagePool ? iPartsDataChangedEventByEdit.Action.NEW : iPartsDataChangedEventByEdit.Action.MODIFIED;
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.DRAWING,
                                                                                                      action, poolId, false));
        }

    }

    protected void imageFileChanged(Event event) {
        // Dialog ist noch nicht sichtbar -> Größe von der Zeichnungsvorschau wäre noch 0 -> wird später explizit nach dem
        // Anzeigen vom Fenster nochmal aufgerufen
        if (!mainWindow.isVisible()) {
            return;
        }

        imageFile = null;
        hotspotFile = null;
        List<DWFile> selectedFiles = mainWindow.imageFileChooser.getSelectedFiles();
        for (DWFile selectedFile : selectedFiles) {
            String fileName = selectedFile.getName();
            if (isValidDrawing(fileName)) {
                imageFile = selectedFile;
            } else if (MimeTypes.isSenFile(fileName)) {
                hotspotFile = selectedFile;
            }
        }

        mainWindow.previewPanel.removeAllChildren();
        if (imagePreview != null) {
            imagePreview.dispose();
            imagePreview = null;
        }

        if (imageFile != null) {
            mainWindow.imageNumberTextfield.setText(imageFile.extractFileName(false));
            String imageFileName = imageFile.getAbsolutePath();
            ImageVariant imageVariant = ImageVariant.iv2D;
            if (MimeTypes.isSvgFile(imageFileName)) {
                imageVariant = ImageVariant.ivSVG;
            } else if (MimeTypes.is3dImageFile(imageFileName)) {
                imageVariant = ImageVariant.iv3D;
            }
            mainWindow.usageComboBox.setSelectedUserObject(imageVariant);

            try {
                imagePreview = GuiViewer.getImageViewerForFilename(imageFile.getAbsolutePath(), -1,
                                                                   false, true);
                if (imagePreview != null) {
                    if (imagePreview instanceof AbstractImageViewer3D) {
                        ((AbstractImageViewer3D)imagePreview).assignSettings(getImageSettings().getImageCommonSettings(),
                                                                             getImageSettings().getImageHotspotSettings(),
                                                                             getImageSettings().getImageSecuritySettings());
                    }

                    AbstractGuiControl imagePreviewGui = imagePreview.getGui();
                    imagePreviewGui.setBorderWidth(8);
                    imagePreviewGui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                    byte[] svgContent = null;
                    if (imagePreview instanceof GuiViewerImageSvgNative) {
                        // SVG Binary wird umgeschrieben und die Outlines ergänzt
                        // Das Ergebnis wieder in eine Datei zu schreiben ist ein Overhead der hier bewusst umgangen wird
                        svgContent = iPartsSvgOutlineHelper.attachOutlineFilter(imageFile.readByteArray()).getPictureByteData();
                        imagePreview.setData(svgContent, MimeTypes.EXTENSION_SVG, GuiViewerImageInterface.MAX_NUMBER_OF_PIXELS_UNLIMITED, true);
                    } else {
                        imagePreview.setFile(imageFile, true);
                    }
                    fillHotspots(svgContent);
                    mainWindow.previewPanel.addChild(imagePreviewGui);
                } else {
                    mainWindow.noImageLabel.setText("!!Diese Zeichnung kann nicht angezeigt werden.");
                    mainWindow.previewPanel.addChild(mainWindow.noImageLabel);
                }
            } catch (HttpServerException e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        } else {
            mainWindow.noImageLabel.setText("!!Noch keine Zeichnung ausgewählt");
            mainWindow.previewPanel.addChild(mainWindow.noImageLabel);
        }

        nameChanged(event);
    }

    protected void nameChanged(Event event) {
        startButton.setEnabled((imageFile != null) && !mainWindow.imageNumberTextfield.getText().isEmpty());
    }

    protected void closeWindow(Event event) {
        mainWindow.setModalResult(ModalResult.CANCEL);
        mainWindow.setVisible(false);
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
        private de.docware.framework.modules.gui.controls.GuiLabel imageFileLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield imageFileChooser;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel XBR33530;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel imageDataPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel imageNumberLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField imageNumberTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel usageLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.base.project.mechanic.drawing.ImageVariant> usageComboBox;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel previewPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel noImageLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setMinimumWidth(400);
            this.setMinimumHeight(500);
            this.setVisible(false);
            this.addEventListener(new de.docware.framework.modules.gui.event.EventListener("closingEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    closeWindow(event);
                }
            });
            this.setTitle("!!Neue Zeichnung importieren");
            de.docware.framework.modules.gui.layout.LayoutGridBag mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mainWindowLayout.setCentered(false);
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("!!Zeichnung auswählen");
            title.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgPlugin_iParts_ImportFiles"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            title.setConstraints(titleConstraints);
            this.addChild(title);
            imageFileLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            imageFileLabel.setName("imageFileLabel");
            imageFileLabel.__internal_setGenerationDpi(96);
            imageFileLabel.registerTranslationHandler(translationHandler);
            imageFileLabel.setScaleForResolution(true);
            imageFileLabel.setMinimumWidth(10);
            imageFileLabel.setMinimumHeight(10);
            imageFileLabel.setText("!!Zeichnungsdatei und optionale (X-)SEN-Datei für die Hotspots:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag imageFileLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "w", "h", 8, 8, 0, 8);
            imageFileLabel.setConstraints(imageFileLabelConstraints);
            this.addChild(imageFileLabel);
            imageFileChooser = new de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield();
            imageFileChooser.setName("imageFileChooser");
            imageFileChooser.__internal_setGenerationDpi(96);
            imageFileChooser.registerTranslationHandler(translationHandler);
            imageFileChooser.setScaleForResolution(true);
            imageFileChooser.setMinimumWidth(10);
            imageFileChooser.setMinimumHeight(10);
            XBR33530 = new de.docware.framework.modules.gui.controls.GuiLabel();
            XBR33530.setName("XBR33530");
            XBR33530.__internal_setGenerationDpi(96);
            XBR33530.registerTranslationHandler(translationHandler);
            XBR33530.setScaleForResolution(true);
            XBR33530.setText("!!Zeichnungsdatei und optionale (X-)SEN-Datei mit identischem Dateinamen für die Hotspots");
            imageFileChooser.setTooltip(XBR33530);
            imageFileChooser.setServerMode(false);
            imageFileChooser.setMultiSelectionMode(true);
            imageFileChooser.setApproveButtonText("!!Zeichnung auswählen");
            imageFileChooser.setEditable(false);
            imageFileChooser.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    imageFileChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag imageFileChooserConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "w", "h", 4, 8, 0, 8);
            imageFileChooser.setConstraints(imageFileChooserConstraints);
            this.addChild(imageFileChooser);
            imageDataPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            imageDataPanel.setName("imageDataPanel");
            imageDataPanel.__internal_setGenerationDpi(96);
            imageDataPanel.registerTranslationHandler(translationHandler);
            imageDataPanel.setScaleForResolution(true);
            imageDataPanel.setMinimumWidth(10);
            imageDataPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag imageDataPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            imageDataPanel.setLayout(imageDataPanelLayout);
            imageNumberLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            imageNumberLabel.setName("imageNumberLabel");
            imageNumberLabel.__internal_setGenerationDpi(96);
            imageNumberLabel.registerTranslationHandler(translationHandler);
            imageNumberLabel.setScaleForResolution(true);
            imageNumberLabel.setMinimumWidth(10);
            imageNumberLabel.setMinimumHeight(10);
            imageNumberLabel.setText("Zeichnungsnummer:");
            imageNumberLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag imageNumberLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 8, 0, 0);
            imageNumberLabel.setConstraints(imageNumberLabelConstraints);
            imageDataPanel.addChild(imageNumberLabel);
            imageNumberTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            imageNumberTextfield.setName("imageNumberTextfield");
            imageNumberTextfield.__internal_setGenerationDpi(96);
            imageNumberTextfield.registerTranslationHandler(translationHandler);
            imageNumberTextfield.setScaleForResolution(true);
            imageNumberTextfield.setMinimumWidth(300);
            imageNumberTextfield.setMinimumHeight(10);
            imageNumberTextfield.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    nameChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag imageNumberTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "w", "h", 8, 8, 0, 8);
            imageNumberTextfield.setConstraints(imageNumberTextfieldConstraints);
            imageDataPanel.addChild(imageNumberTextfield);
            usageLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            usageLabel.setName("usageLabel");
            usageLabel.__internal_setGenerationDpi(96);
            usageLabel.registerTranslationHandler(translationHandler);
            usageLabel.setScaleForResolution(true);
            usageLabel.setMinimumWidth(10);
            usageLabel.setMinimumHeight(10);
            usageLabel.setText("!!Verwendung:");
            usageLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag usageLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 8, 8, 0, 0);
            usageLabel.setConstraints(usageLabelConstraints);
            imageDataPanel.addChild(usageLabel);
            usageComboBox = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.base.project.mechanic.drawing.ImageVariant>();
            usageComboBox.setName("usageComboBox");
            usageComboBox.__internal_setGenerationDpi(96);
            usageComboBox.registerTranslationHandler(translationHandler);
            usageComboBox.setScaleForResolution(true);
            usageComboBox.setMinimumWidth(100);
            usageComboBox.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag usageComboBoxConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "w", "n", 8, 8, 0, 8);
            usageComboBox.setConstraints(usageComboBoxConstraints);
            imageDataPanel.addChild(usageComboBox);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag imageDataPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "n", "h", 0, 0, 0, 0);
            imageDataPanel.setConstraints(imageDataPanelConstraints);
            this.addChild(imageDataPanel);
            previewPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            previewPanel.setName("previewPanel");
            previewPanel.__internal_setGenerationDpi(96);
            previewPanel.registerTranslationHandler(translationHandler);
            previewPanel.setScaleForResolution(true);
            previewPanel.setMinimumWidth(600);
            previewPanel.setMinimumHeight(400);
            previewPanel.setTitle("!!Vorschau");
            de.docware.framework.modules.gui.layout.LayoutBorder previewPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            previewPanel.setLayout(previewPanelLayout);
            noImageLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            noImageLabel.setName("noImageLabel");
            noImageLabel.__internal_setGenerationDpi(96);
            noImageLabel.registerTranslationHandler(translationHandler);
            noImageLabel.setScaleForResolution(true);
            noImageLabel.setMinimumWidth(10);
            noImageLabel.setMinimumHeight(10);
            noImageLabel.setFontSize(14);
            noImageLabel.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            noImageLabel.setPaddingTop(8);
            noImageLabel.setVerticalAlignment(de.docware.framework.modules.gui.controls.AbstractVerticalAlignmentControl.VerticalAlignment.TOP);
            noImageLabel.setText("!!Noch keine Zeichnung ausgewählt");
            noImageLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder noImageLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            noImageLabel.setConstraints(noImageLabelConstraints);
            previewPanel.addChild(noImageLabel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag previewPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 100.0, 100.0, "c", "b", 8, 8, 0, 8);
            previewPanel.setConstraints(previewPanelConstraints);
            this.addChild(previewPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CUSTOM);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 5, 1, 1, 0.0, 0.0, "s", "h", 8, 0, 0, 0);
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}