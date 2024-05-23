/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.viewer.GuiViewer;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerImageInterface;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.server.HttpServerException;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.file.DWFile;

import java.util.List;

/**
 * Import-Dialog für neue Zusatzgrafik.
 */
public class EditImportGraphicForm {

    public static DWFile loadGraphicFile(EtkProject project) {
        EditImportGraphicForm dlg = new EditImportGraphicForm(project);
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getImageFile();
        }
        return null;
    }

    protected EtkProject project;
    protected DWFile imageFile;

    protected GuiButtonOnPanel startButton;
    protected GuiViewerImageInterface imagePreview;

    /**
     * Erzeugt eine Instanz von EditImportGraphicForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditImportGraphicForm(EtkProject project) {
        $$internalCreateGui$$(null);
        this.project = project;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.filechooserGraphicFile.setPurpose(FileChooserPurpose.OPEN);

        mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, true);
        startButton = mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.START, true);
        startButton.setModalResult(ModalResult.OK);

        // EventListener und explizier Aufruf von imageFileChanged() notwendig, weil Dialog erst jetzt sichtbar ist und somit
        // auch jetzt erst die Zeichnungsvorschau unter JEE eine Größe hat (wichtig für die Anzeige vom Overview-Panel)
        mainWindow.addEventListener(new EventListener(Event.OPENED_EVENT) {
            @Override
            public void fire(Event event) {
                graphicFileChanged(null);
            }
        });

        nameChanged(null);

        mainWindow.pack();
    }

    public GuiWindow getGui() {
        return mainWindow;
    }

    public ModalResult showModal() {
        mainWindow.filechooserGraphicFile.showDialog();
        if (!mainWindow.filechooserGraphicFile.getSelectedFiles().isEmpty()) {
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


    protected boolean isValidDrawing(String fileName) {
        return MimeTypes.isFramework2dImageFile(fileName) || MimeTypes.isSvgFile(fileName);
    }

    protected void graphicFileChanged(Event event) {
        // Dialog ist noch nicht sichtbar -> Größe von der Zeichnungsvorschau wäre noch 0 -> wird später explizit nach dem
        // Anzeigen vom Fenster nochmal aufgerufen
        if (!mainWindow.isVisible()) {
            return;
        }

        imageFile = null;
        List<DWFile> selectedFiles = mainWindow.filechooserGraphicFile.getSelectedFiles();
        for (DWFile selectedFile : selectedFiles) {
            String fileName = selectedFile.getName();
            if (isValidDrawing(fileName)) {
                imageFile = selectedFile;
            }
        }

        mainWindow.panelPreview.removeAllChildren();
        if (imagePreview != null) {
            imagePreview.dispose();
            imagePreview = null;
        }

        if (imageFile != null) {
            try {
                imagePreview = GuiViewer.getImageViewerForFilename(imageFile.getAbsolutePath(), -1,
                                                                   false, true);
                if (imagePreview != null) {
                    AbstractGuiControl imagePreviewGui = imagePreview.getGui();
                    imagePreviewGui.setBorderWidth(8);
                    imagePreviewGui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                    imagePreview.setFile(imageFile, true);
                    mainWindow.panelPreview.addChild(imagePreviewGui);
                } else {
                    mainWindow.labelNoGraphic.setText("!!Diese Zusatzgrafik kann nicht angezeigt werden.");
                    mainWindow.panelPreview.addChild(mainWindow.labelNoGraphic);
                }
            } catch (HttpServerException e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        } else {
            mainWindow.labelNoGraphic.setText("!!Noch keine Zusatzgrafik ausgewählt");
            mainWindow.panelPreview.addChild(mainWindow.labelNoGraphic);
        }

        nameChanged(event);
    }

    protected void nameChanged(Event event) {
        startButton.setEnabled((imageFile != null));
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelGraphicFile;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield filechooserGraphicFile;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel JHN18710;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelNoGraphic;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setMinimumWidth(400);
            this.setMinimumHeight(500);
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
            title.setTitle("!!Zusatzgrafik auswählen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            labelGraphicFile = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelGraphicFile.setName("labelGraphicFile");
            labelGraphicFile.__internal_setGenerationDpi(96);
            labelGraphicFile.registerTranslationHandler(translationHandler);
            labelGraphicFile.setScaleForResolution(true);
            labelGraphicFile.setMinimumWidth(10);
            labelGraphicFile.setMinimumHeight(10);
            labelGraphicFile.setText("!!Zusatzgrafikdatei:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelGraphicFileConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "h", 8, 8, 0, 8);
            labelGraphicFile.setConstraints(labelGraphicFileConstraints);
            panelMain.addChild(labelGraphicFile);
            filechooserGraphicFile = new de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield();
            filechooserGraphicFile.setName("filechooserGraphicFile");
            filechooserGraphicFile.__internal_setGenerationDpi(96);
            filechooserGraphicFile.registerTranslationHandler(translationHandler);
            filechooserGraphicFile.setScaleForResolution(true);
            filechooserGraphicFile.setMinimumWidth(10);
            filechooserGraphicFile.setMinimumHeight(10);
            JHN18710 = new de.docware.framework.modules.gui.controls.GuiLabel();
            JHN18710.setName("JHN18710");
            JHN18710.__internal_setGenerationDpi(96);
            JHN18710.registerTranslationHandler(translationHandler);
            JHN18710.setScaleForResolution(true);
            JHN18710.setText("!!Zusatzgrafikdatei (BMP, GIF, JPG, JPEG, PNG, SMG, SVG, SVGZ, TIF, TIFF)");
            filechooserGraphicFile.setTooltip(JHN18710);
            filechooserGraphicFile.setServerMode(false);
            filechooserGraphicFile.setApproveButtonText("!!Zusatzgrafik auswählen");
            filechooserGraphicFile.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag filechooserGraphicFileConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "w", "h", 4, 8, 4, 8);
            filechooserGraphicFile.setConstraints(filechooserGraphicFileConstraints);
            panelMain.addChild(filechooserGraphicFile);
            panelPreview = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPreview.setName("panelPreview");
            panelPreview.__internal_setGenerationDpi(96);
            panelPreview.registerTranslationHandler(translationHandler);
            panelPreview.setScaleForResolution(true);
            panelPreview.setMinimumWidth(10);
            panelPreview.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelPreviewLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelPreview.setLayout(panelPreviewLayout);
            labelNoGraphic = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelNoGraphic.setName("labelNoGraphic");
            labelNoGraphic.__internal_setGenerationDpi(96);
            labelNoGraphic.registerTranslationHandler(translationHandler);
            labelNoGraphic.setScaleForResolution(true);
            labelNoGraphic.setMinimumWidth(10);
            labelNoGraphic.setMinimumHeight(10);
            labelNoGraphic.setFontSize(14);
            labelNoGraphic.setFontStyle(1);
            labelNoGraphic.setPaddingTop(8);
            labelNoGraphic.setVerticalAlignment(de.docware.framework.modules.gui.controls.AbstractVerticalAlignmentControl.VerticalAlignment.TOP);
            labelNoGraphic.setText("!!Noch keine Zusatzgrafik ausgewählt");
            labelNoGraphic.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelNoGraphicConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelNoGraphic.setConstraints(labelNoGraphicConstraints);
            panelPreview.addChild(labelNoGraphic);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelPreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 100.0, 100.0, "c", "b", 4, 8, 4, 8);
            panelPreview.setConstraints(panelPreviewConstraints);
            panelMain.addChild(panelPreview);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CUSTOM);
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