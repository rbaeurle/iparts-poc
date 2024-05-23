/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataIcon;
import de.docware.apps.etk.base.project.mechanic.ids.IconId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.viewer.GuiViewer;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerImageInterface;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerInterface;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.server.HttpServerException;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.file.DWFile;

public class EditPictureLoadEditForm extends AbstractJavaViewerForm {

    public static IconId showEditPictureLoadForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IconId iconId) {
        EditPictureLoadEditForm dlg = new EditPictureLoadEditForm(dataConnector, parentForm, iconId);
        dlg.setTitle("!!Zusatzgrafiken ändern");
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getIconId();
        }
        return null;
    }

    public static IconId showCreatePictureLoadForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        EditPictureLoadEditForm dlg = new EditPictureLoadEditForm(dataConnector, parentForm, null);
        dlg.setTitle("!!Zusatzgrafiken einfügen");
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getIconId();
        }
        return null;
    }

    public static void showPictureLoadForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IconId iconId) {
        EditPictureLoadEditForm dlg = new EditPictureLoadEditForm(dataConnector, parentForm, iconId);
        dlg.setTitle("!!Zusatzgrafiken anzeigen");
        dlg.setViewingOnly(true);
        dlg.showModal();
    }

    protected IconId iconId;
    protected EtkDataIcon iconData;
    protected GuiViewerImageInterface imageGraphicPreview;
    protected GuiViewerImageInterface imagePrintPreview;
    protected boolean isEdit;
    protected boolean isViewingOnly;

    /**
     * Erzeugt eine Instanz von EditPictureLoadEditForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditPictureLoadEditForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IconId iconId) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.iconId = iconId;
        this.isEdit = (iconId != null) && iconId.isValidId();
        this.isViewingOnly = false;
        this.imageGraphicPreview = null;
        this.imagePrintPreview = null;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
        prepareIconData();
    }

    private void setPreviewPrint() {
        setPreview(EtkDbConst.FIELD_I_DATAPRT);
    }

    private void setPreviewGraphic() {
        setPreview(EtkDbConst.FIELD_I_DATA);
    }

    private void setPreview(String fieldName) {
        GuiPanel parentPanel = mainWindow.panelGraphicPreview;
        GuiLabel noGraphicLabel = mainWindow.labelNoGraphic;
        GuiViewerImageInterface imagePreview = imageGraphicPreview;
        boolean isPrint = fieldName.equals(EtkDbConst.FIELD_I_DATAPRT);
        if (isPrint) {
            parentPanel = mainWindow.panelPrintPreview;
            noGraphicLabel = mainWindow.labelNoPrint;
            imagePreview = imagePrintPreview;
        }
        parentPanel.removeAllChildren();
        if (imagePreview != null) {
            imagePreview.dispose();
            imagePreview = null;
        }
        byte[] byteArray = null;
        if (iconData != null) {
            byteArray = iconData.getFieldValueAsBlob(fieldName);
        }
        if ((byteArray != null) && (byteArray.length > 0)) {
            try {
                GuiViewerInterface viewer = GuiViewer.getViewerForByteArray(byteArray,
                                                                            -1, null, true, "x." + MimeTypes.EXTENSION_PNG);
                if (viewer instanceof GuiViewerImageInterface) {
                    imagePreview = (GuiViewerImageInterface)viewer;
                    AbstractGuiControl imagePreviewGui = imagePreview.getGui();
                    imagePreviewGui.setBorderWidth(8);
                    imagePreviewGui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                    parentPanel.addChild(imagePreviewGui);
                } else {
                    noGraphicLabel.setText("!!Diese Zusatzgrafik kann nicht angezeigt werden.");
                    parentPanel.addChild(noGraphicLabel);
                }
            } catch (HttpServerException e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        } else {
            noGraphicLabel.setText("!!Keine Zusatzgrafik");
            parentPanel.addChild(noGraphicLabel);
        }
        if (isPrint) {
            imagePrintPreview = imagePreview;
        } else {
            imageGraphicPreview = imagePreview;
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    //===== Getter and Setter =====
    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public boolean isEdit() {
        return isEdit;
    }

    public boolean isViewingOnly() {
        return isViewingOnly;
    }

    public void setViewingOnly(boolean viewingOnly) {
        mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, !viewingOnly);
        mainWindow.textfieldName.setEnabled(!viewingOnly);
        mainWindow.multilangeditZusatz.setReadOnly(viewingOnly);
        mainWindow.buttonGraphicAdd.setEnabled(!viewingOnly);
        mainWindow.buttonGraphicDelete.setEnabled(!viewingOnly);
        mainWindow.buttonPrintAdd.setEnabled(!viewingOnly);
        mainWindow.buttonPrintDelete.setEnabled(!viewingOnly);
        isViewingOnly = viewingOnly;
        enableButtons();
    }

    public IconId getIconId() {
        return iconId;
    }

    public void setIconId(IconId iconId) {
        this.iconId = iconId;
        prepareIconData();
    }

//===== Getter and Setter End =====

    private String getIconName() {
        return iconData.getFieldValue(EtkDbConst.FIELD_I_ICON);
    }

    private void setIconName(String name) {
        iconData.setFieldValue(EtkDbConst.FIELD_I_ICON, name, DBActionOrigin.FROM_EDIT);
    }

    private void enableButtons() {
        boolean isModified = iconData.isModified();
        if (isModified) {
            isModified = !getIconName().isEmpty();
        }
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isViewingOnly || isModified);
        mainWindow.buttonGraphicDelete.setEnabled(!isViewingOnly && iconData.getAttribute(EtkDbConst.FIELD_I_DATA).getValue() != null);
        mainWindow.buttonPrintDelete.setEnabled(!isViewingOnly && iconData.getAttribute(EtkDbConst.FIELD_I_DATAPRT).getValue() != null);
    }

    private void prepareIconData() {
        iconData = EtkDataObjectFactory.createDataIcon();
        iconData.init(getProject());
        if (iconId != null) {
            iconData.setId(iconId, DBActionOrigin.FROM_DB);
            if (!iconData.loadFromDB(iconId)) {
                iconData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
        } else {
            iconData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        mainWindow.textfieldName.setText(getIconName());
        EtkMultiSprache multiLanguage = iconData.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_I_HINT);
        if (multiLanguage.isEmpty()) {
            for (String language : getConfig().getDatabaseLanguages()) {
                multiLanguage.setText(language, "");
            }
        }
        mainWindow.multilangeditZusatz.setMultiLanguage(multiLanguage);
        mainWindow.multilangeditZusatz.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onMultiLangChangedEvent(event);
            }
        });
        setPreviewGraphic();
        setPreviewPrint();
        enableButtons();
    }

    private void loadGraphic(String fieldName) {
        DWFile importFile = EditImportGraphicForm.loadGraphicFile(getProject());
        if (importFile != null) {
            iconData.setFieldValueAsBlob(fieldName, importFile.readByteArray(), DBActionOrigin.FROM_EDIT);
            if (J2EEHandler.isJ2EE()) {
                importFile.delete();
            }
            setPreview(fieldName);
            enableButtons();
        }
    }

    private void deleteGraphic(String fieldName) {
        iconData.setFieldValueAsBlob(fieldName, null, DBActionOrigin.FROM_EDIT);
        setPreview(fieldName);
        enableButtons();
    }

    private void onNameChangedEvent(Event event) {
        setIconName(mainWindow.textfieldName.getText());
        enableButtons();
    }

    private void onMultiLangChangedEvent(Event event) {
        EtkMultiSprache multi = mainWindow.multilangeditZusatz.getMultiLanguage();
        iconData.getAttribute(EtkDbConst.FIELD_I_HINT).setValueAsMultiLanguage(multi, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        enableButtons();
    }

    private void loadGraphicEvent(Event event) {
        loadGraphic(EtkDbConst.FIELD_I_DATA);
    }

    private void loadPrintEvent(Event event) {
        loadGraphic(EtkDbConst.FIELD_I_DATAPRT);
    }

    private void deleteGraphicEvent(Event event) {
        deleteGraphic(EtkDbConst.FIELD_I_DATA);
    }

    private void deletePrintEvent(Event event) {
        deleteGraphic(EtkDbConst.FIELD_I_DATAPRT);
    }

    private boolean saveData() {
        getProject().getDbLayer().startTransaction();
        try {
            iconData.saveToDB();
            getProject().getDbLayer().commit();
            return true;
        } catch (Exception e) {
            getProject().getDbLayer().rollback();
            Logger.getLogger().handleRuntimeException(e);
        }
        return false;
    }

    private boolean checkIconExists() {
        EtkDataIcon dataIconTest = EtkDataObjectFactory.createDataIcon();
        dataIconTest.init(getProject());
        dataIconTest.setId(iconData.getAsId(), DBActionOrigin.FROM_DB);
        return dataIconTest.existsInDB();
    }

    private boolean checkData() {
        boolean doCheck = false;
        if (!isViewingOnly) {
            if (isEdit) {
                //bei Edit überprüfen, on PK-Value modifiziert
                if (!iconId.equals(iconData.getAsId())) {
                    //wenn ja, dann überprüfen ob neuer PK-Value bereits existiert
                    doCheck = true;
                }
            } else {
                //bei Neuanlage: überprüfen, ob PK-Value bereits existiert
                doCheck = true;
            }
            if (doCheck) {
                if (!checkIconExists()) {
                    return true;
                } else {
                    String msg = TranslationHandler.translate("!!Die Zusatzgrafik '%1' existiert bereits!", getIconName());
                    MessageDialog.showError(msg);
                    return false;
                }
            }
        }
        return true;
    }

    private void buttonOKEvent(Event event) {
        if (checkData()) {
            if (!isViewingOnly) {
                if (saveData()) {
                    mainWindow.setModalResult(ModalResult.OK);
                } else {
                    mainWindow.setModalResult(ModalResult.CANCEL);
                }
            } else {
                mainWindow.setModalResult(ModalResult.CANCEL);
            }
            iconId = iconData.getAsId();
            mainWindow.setVisible(false);
        }
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelZusatz;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelZusatz;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiMultiLangEdit multilangeditZusatz;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpane_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_0_firstChild_Graphic;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separatorGraphic;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGraphic;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelGraphicText;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGraphicPreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelNoGraphic;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGraphicButtons;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonGraphicAdd;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonGraphicDelete;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxGraphicTransparent;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_0_secondChild_Print;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separatorPrint;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPrint;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelPrintText;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPrintPreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelNoPrint;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPrintButtons;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonPrintAdd;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonPrintDelete;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxPrintTransparent;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setHeight(700);
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
            panelZusatz = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelZusatz.setName("panelZusatz");
            panelZusatz.__internal_setGenerationDpi(96);
            panelZusatz.registerTranslationHandler(translationHandler);
            panelZusatz.setScaleForResolution(true);
            panelZusatz.setMinimumWidth(10);
            panelZusatz.setMinimumHeight(80);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelZusatzLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelZusatz.setLayout(panelZusatzLayout);
            labelName = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelName.setName("labelName");
            labelName.__internal_setGenerationDpi(96);
            labelName.registerTranslationHandler(translationHandler);
            labelName.setScaleForResolution(true);
            labelName.setMinimumWidth(10);
            labelName.setMinimumHeight(10);
            labelName.setText("!!Zusatzgrafik Name");
            labelName.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            labelName.setConstraints(labelNameConstraints);
            panelZusatz.addChild(labelName);
            textfieldName = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldName.setName("textfieldName");
            textfieldName.__internal_setGenerationDpi(96);
            textfieldName.registerTranslationHandler(translationHandler);
            textfieldName.setScaleForResolution(true);
            textfieldName.setMinimumWidth(200);
            textfieldName.setMinimumHeight(10);
            textfieldName.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onNameChangedEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 4);
            textfieldName.setConstraints(textfieldNameConstraints);
            panelZusatz.addChild(textfieldName);
            labelZusatz = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelZusatz.setName("labelZusatz");
            labelZusatz.__internal_setGenerationDpi(96);
            labelZusatz.registerTranslationHandler(translationHandler);
            labelZusatz.setScaleForResolution(true);
            labelZusatz.setMinimumWidth(10);
            labelZusatz.setMinimumHeight(10);
            labelZusatz.setText("!!Zusatzgrafik Hinweis");
            labelZusatz.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelZusatzConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            labelZusatz.setConstraints(labelZusatzConstraints);
            panelZusatz.addChild(labelZusatz);
            multilangeditZusatz = new de.docware.framework.modules.gui.controls.GuiMultiLangEdit();
            multilangeditZusatz.setName("multilangeditZusatz");
            multilangeditZusatz.__internal_setGenerationDpi(96);
            multilangeditZusatz.registerTranslationHandler(translationHandler);
            multilangeditZusatz.setScaleForResolution(true);
            multilangeditZusatz.setMinimumWidth(10);
            multilangeditZusatz.setMinimumHeight(10);
            multilangeditZusatz.setBackgroundColor(new java.awt.Color(255, 255, 255, 0));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag multilangeditZusatzConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 4);
            multilangeditZusatz.setConstraints(multilangeditZusatzConstraints);
            panelZusatz.addChild(multilangeditZusatz);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelZusatzConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "n", "h", 0, 0, 0, 0);
            panelZusatz.setConstraints(panelZusatzConstraints);
            panelMain.addChild(panelZusatz);
            splitpane_0 = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpane_0.setName("splitpane_0");
            splitpane_0.__internal_setGenerationDpi(96);
            splitpane_0.registerTranslationHandler(translationHandler);
            splitpane_0.setScaleForResolution(true);
            splitpane_0.setMinimumWidth(10);
            splitpane_0.setMinimumHeight(10);
            splitpane_0.setHorizontal(false);
            splitpane_0.setDividerPosition(700);
            splitpane_0.setResizeWeight(1.0);
            splitpane_0_firstChild_Graphic = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_0_firstChild_Graphic.setName("splitpane_0_firstChild_Graphic");
            splitpane_0_firstChild_Graphic.__internal_setGenerationDpi(96);
            splitpane_0_firstChild_Graphic.registerTranslationHandler(translationHandler);
            splitpane_0_firstChild_Graphic.setScaleForResolution(true);
            splitpane_0_firstChild_Graphic.setMinimumWidth(0);
            splitpane_0_firstChild_Graphic.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_0_firstChild_GraphicLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_0_firstChild_Graphic.setLayout(splitpane_0_firstChild_GraphicLayout);
            separatorGraphic = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separatorGraphic.setName("separatorGraphic");
            separatorGraphic.__internal_setGenerationDpi(96);
            separatorGraphic.registerTranslationHandler(translationHandler);
            separatorGraphic.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder separatorGraphicConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            separatorGraphicConstraints.setPosition("north");
            separatorGraphic.setConstraints(separatorGraphicConstraints);
            splitpane_0_firstChild_Graphic.addChild(separatorGraphic);
            panelGraphic = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGraphic.setName("panelGraphic");
            panelGraphic.__internal_setGenerationDpi(96);
            panelGraphic.registerTranslationHandler(translationHandler);
            panelGraphic.setScaleForResolution(true);
            panelGraphic.setMinimumWidth(10);
            panelGraphic.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelGraphicLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelGraphic.setLayout(panelGraphicLayout);
            labelGraphicText = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelGraphicText.setName("labelGraphicText");
            labelGraphicText.__internal_setGenerationDpi(96);
            labelGraphicText.registerTranslationHandler(translationHandler);
            labelGraphicText.setScaleForResolution(true);
            labelGraphicText.setMinimumWidth(10);
            labelGraphicText.setMinimumHeight(10);
            labelGraphicText.setText("!!Grafik");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelGraphicTextConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 2, 1, 100.0, 0.0, "c", "h", 4, 8, 4, 0);
            labelGraphicText.setConstraints(labelGraphicTextConstraints);
            panelGraphic.addChild(labelGraphicText);
            panelGraphicPreview = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGraphicPreview.setName("panelGraphicPreview");
            panelGraphicPreview.__internal_setGenerationDpi(96);
            panelGraphicPreview.registerTranslationHandler(translationHandler);
            panelGraphicPreview.setScaleForResolution(true);
            panelGraphicPreview.setMinimumWidth(10);
            panelGraphicPreview.setMinimumHeight(10);
            panelGraphicPreview.setPaddingTop(4);
            panelGraphicPreview.setPaddingLeft(8);
            panelGraphicPreview.setPaddingRight(4);
            panelGraphicPreview.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelGraphicPreviewLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelGraphicPreview.setLayout(panelGraphicPreviewLayout);
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
            labelNoGraphic.setText("!!Keine Zusatzgrafik");
            labelNoGraphic.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelNoGraphicConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelNoGraphic.setConstraints(labelNoGraphicConstraints);
            panelGraphicPreview.addChild(labelNoGraphic);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelGraphicPreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 100.0, "w", "b", 0, 0, 0, 0);
            panelGraphicPreview.setConstraints(panelGraphicPreviewConstraints);
            panelGraphic.addChild(panelGraphicPreview);
            panelGraphicButtons = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGraphicButtons.setName("panelGraphicButtons");
            panelGraphicButtons.__internal_setGenerationDpi(96);
            panelGraphicButtons.registerTranslationHandler(translationHandler);
            panelGraphicButtons.setScaleForResolution(true);
            panelGraphicButtons.setMinimumWidth(100);
            panelGraphicButtons.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelGraphicButtonsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelGraphicButtons.setLayout(panelGraphicButtonsLayout);
            buttonGraphicAdd = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonGraphicAdd.setName("buttonGraphicAdd");
            buttonGraphicAdd.__internal_setGenerationDpi(96);
            buttonGraphicAdd.registerTranslationHandler(translationHandler);
            buttonGraphicAdd.setScaleForResolution(true);
            buttonGraphicAdd.setMinimumWidth(100);
            buttonGraphicAdd.setMinimumHeight(10);
            buttonGraphicAdd.setMnemonicEnabled(true);
            buttonGraphicAdd.setText("!!Grafik laden...");
            buttonGraphicAdd.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    loadGraphicEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonGraphicAddConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            buttonGraphicAdd.setConstraints(buttonGraphicAddConstraints);
            panelGraphicButtons.addChild(buttonGraphicAdd);
            buttonGraphicDelete = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonGraphicDelete.setName("buttonGraphicDelete");
            buttonGraphicDelete.__internal_setGenerationDpi(96);
            buttonGraphicDelete.registerTranslationHandler(translationHandler);
            buttonGraphicDelete.setScaleForResolution(true);
            buttonGraphicDelete.setMinimumWidth(100);
            buttonGraphicDelete.setMinimumHeight(10);
            buttonGraphicDelete.setMnemonicEnabled(true);
            buttonGraphicDelete.setText("!!Grafik entfernen");
            buttonGraphicDelete.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    deleteGraphicEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonGraphicDeleteConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            buttonGraphicDelete.setConstraints(buttonGraphicDeleteConstraints);
            panelGraphicButtons.addChild(buttonGraphicDelete);
            checkboxGraphicTransparent = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxGraphicTransparent.setName("checkboxGraphicTransparent");
            checkboxGraphicTransparent.__internal_setGenerationDpi(96);
            checkboxGraphicTransparent.registerTranslationHandler(translationHandler);
            checkboxGraphicTransparent.setScaleForResolution(true);
            checkboxGraphicTransparent.setMinimumWidth(10);
            checkboxGraphicTransparent.setMinimumHeight(10);
            checkboxGraphicTransparent.setVisible(false);
            checkboxGraphicTransparent.setText("!!Transparent");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxGraphicTransparentConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 0, 4);
            checkboxGraphicTransparent.setConstraints(checkboxGraphicTransparentConstraints);
            panelGraphicButtons.addChild(checkboxGraphicTransparent);
            label_0 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_0.setName("label_0");
            label_0.__internal_setGenerationDpi(96);
            label_0.registerTranslationHandler(translationHandler);
            label_0.setScaleForResolution(true);
            label_0.setMinimumWidth(10);
            label_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 100.0, "c", "n", 0, 0, 0, 0);
            label_0.setConstraints(label_0Constraints);
            panelGraphicButtons.addChild(label_0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelGraphicButtonsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 100.0, "c", "b", 0, 0, 0, 0);
            panelGraphicButtons.setConstraints(panelGraphicButtonsConstraints);
            panelGraphic.addChild(panelGraphicButtons);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelGraphicConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelGraphic.setConstraints(panelGraphicConstraints);
            splitpane_0_firstChild_Graphic.addChild(panelGraphic);
            splitpane_0.addChild(splitpane_0_firstChild_Graphic);
            splitpane_0_secondChild_Print = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_0_secondChild_Print.setName("splitpane_0_secondChild_Print");
            splitpane_0_secondChild_Print.__internal_setGenerationDpi(96);
            splitpane_0_secondChild_Print.registerTranslationHandler(translationHandler);
            splitpane_0_secondChild_Print.setScaleForResolution(true);
            splitpane_0_secondChild_Print.setMinimumWidth(0);
            splitpane_0_secondChild_Print.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_0_secondChild_PrintLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_0_secondChild_Print.setLayout(splitpane_0_secondChild_PrintLayout);
            separatorPrint = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separatorPrint.setName("separatorPrint");
            separatorPrint.__internal_setGenerationDpi(96);
            separatorPrint.registerTranslationHandler(translationHandler);
            separatorPrint.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder separatorPrintConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            separatorPrintConstraints.setPosition("north");
            separatorPrint.setConstraints(separatorPrintConstraints);
            splitpane_0_secondChild_Print.addChild(separatorPrint);
            panelPrint = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPrint.setName("panelPrint");
            panelPrint.__internal_setGenerationDpi(96);
            panelPrint.registerTranslationHandler(translationHandler);
            panelPrint.setScaleForResolution(true);
            panelPrint.setMinimumWidth(10);
            panelPrint.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelPrintLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelPrint.setLayout(panelPrintLayout);
            labelPrintText = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelPrintText.setName("labelPrintText");
            labelPrintText.__internal_setGenerationDpi(96);
            labelPrintText.registerTranslationHandler(translationHandler);
            labelPrintText.setScaleForResolution(true);
            labelPrintText.setMinimumWidth(10);
            labelPrintText.setMinimumHeight(10);
            labelPrintText.setText("!!Grafik mit höherer Auflösung für den Ausdruck");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelPrintTextConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "w", "b", 4, 8, 4, 0);
            labelPrintText.setConstraints(labelPrintTextConstraints);
            panelPrint.addChild(labelPrintText);
            panelPrintPreview = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPrintPreview.setName("panelPrintPreview");
            panelPrintPreview.__internal_setGenerationDpi(96);
            panelPrintPreview.registerTranslationHandler(translationHandler);
            panelPrintPreview.setScaleForResolution(true);
            panelPrintPreview.setMinimumWidth(10);
            panelPrintPreview.setMinimumHeight(10);
            panelPrintPreview.setPaddingTop(4);
            panelPrintPreview.setPaddingLeft(8);
            panelPrintPreview.setPaddingRight(4);
            panelPrintPreview.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelPrintPreviewLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelPrintPreview.setLayout(panelPrintPreviewLayout);
            labelNoPrint = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelNoPrint.setName("labelNoPrint");
            labelNoPrint.__internal_setGenerationDpi(96);
            labelNoPrint.registerTranslationHandler(translationHandler);
            labelNoPrint.setScaleForResolution(true);
            labelNoPrint.setMinimumWidth(10);
            labelNoPrint.setMinimumHeight(10);
            labelNoPrint.setFontSize(14);
            labelNoPrint.setFontStyle(1);
            labelNoPrint.setPaddingTop(8);
            labelNoPrint.setText("!!Keine Zusatzgrafik");
            labelNoPrint.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelNoPrintConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelNoPrint.setConstraints(labelNoPrintConstraints);
            panelPrintPreview.addChild(labelNoPrint);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelPrintPreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 100.0, "w", "b", 0, 0, 0, 0);
            panelPrintPreview.setConstraints(panelPrintPreviewConstraints);
            panelPrint.addChild(panelPrintPreview);
            panelPrintButtons = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPrintButtons.setName("panelPrintButtons");
            panelPrintButtons.__internal_setGenerationDpi(96);
            panelPrintButtons.registerTranslationHandler(translationHandler);
            panelPrintButtons.setScaleForResolution(true);
            panelPrintButtons.setMinimumWidth(100);
            panelPrintButtons.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelPrintButtonsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelPrintButtons.setLayout(panelPrintButtonsLayout);
            buttonPrintAdd = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonPrintAdd.setName("buttonPrintAdd");
            buttonPrintAdd.__internal_setGenerationDpi(96);
            buttonPrintAdd.registerTranslationHandler(translationHandler);
            buttonPrintAdd.setScaleForResolution(true);
            buttonPrintAdd.setMinimumWidth(100);
            buttonPrintAdd.setMinimumHeight(10);
            buttonPrintAdd.setMnemonicEnabled(true);
            buttonPrintAdd.setText("!!Grafik laden...");
            buttonPrintAdd.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    loadPrintEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonPrintAddConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            buttonPrintAdd.setConstraints(buttonPrintAddConstraints);
            panelPrintButtons.addChild(buttonPrintAdd);
            buttonPrintDelete = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonPrintDelete.setName("buttonPrintDelete");
            buttonPrintDelete.__internal_setGenerationDpi(96);
            buttonPrintDelete.registerTranslationHandler(translationHandler);
            buttonPrintDelete.setScaleForResolution(true);
            buttonPrintDelete.setMinimumWidth(100);
            buttonPrintDelete.setMinimumHeight(10);
            buttonPrintDelete.setMnemonicEnabled(true);
            buttonPrintDelete.setText("!!Grafik entfernen");
            buttonPrintDelete.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    deletePrintEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonPrintDeleteConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            buttonPrintDelete.setConstraints(buttonPrintDeleteConstraints);
            panelPrintButtons.addChild(buttonPrintDelete);
            checkboxPrintTransparent = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxPrintTransparent.setName("checkboxPrintTransparent");
            checkboxPrintTransparent.__internal_setGenerationDpi(96);
            checkboxPrintTransparent.registerTranslationHandler(translationHandler);
            checkboxPrintTransparent.setScaleForResolution(true);
            checkboxPrintTransparent.setMinimumWidth(10);
            checkboxPrintTransparent.setMinimumHeight(10);
            checkboxPrintTransparent.setVisible(false);
            checkboxPrintTransparent.setText("!!Transparent");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxPrintTransparentConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            checkboxPrintTransparent.setConstraints(checkboxPrintTransparentConstraints);
            panelPrintButtons.addChild(checkboxPrintTransparent);
            label_1 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_1.setName("label_1");
            label_1.__internal_setGenerationDpi(96);
            label_1.registerTranslationHandler(translationHandler);
            label_1.setScaleForResolution(true);
            label_1.setMinimumWidth(10);
            label_1.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_1Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 100.0, "c", "n", 0, 0, 0, 0);
            label_1.setConstraints(label_1Constraints);
            panelPrintButtons.addChild(label_1);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelPrintButtonsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 100.0, "c", "b", 0, 0, 0, 0);
            panelPrintButtons.setConstraints(panelPrintButtonsConstraints);
            panelPrint.addChild(panelPrintButtons);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelPrintConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelPrint.setConstraints(panelPrintConstraints);
            splitpane_0_secondChild_Print.addChild(panelPrint);
            splitpane_0.addChild(splitpane_0_secondChild_Print);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag splitpane_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            splitpane_0.setConstraints(splitpane_0Constraints);
            panelMain.addChild(splitpane_0);
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
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    buttonOKEvent(event);
                }
            });
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