/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.picture.PictureDataType;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.framework.modules.config.db.datatypes.DataTypesUsageType;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableHeader;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;

import java.util.List;

/**
 * Zeigt eine Zusatzgrafik als HTML in einem Grid an
 */
public class EditPicturePreviewForm extends AbstractJavaViewerForm {

    protected String pictureIds;
    protected boolean showPicture;
    protected String tableName;
    protected PictureDataType pDataType;
    protected boolean showSelectButton;

    /**
     * Erzeugt eine Instanz von EditPicturePreviewForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditPicturePreviewForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, boolean showPicture) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.tableName = EtkDbConst.TABLE_ICONS;
        this.pictureIds = ""; //pictureIds;
        pDataType = new PictureDataType(tableName, EtkDbConst.FIELD_I_ICON);
        pDataType.loadConfig(getConfig(), "");
        setShowPicture(showPicture);
        setShowSelectButton(false);
        setPreviewTextVisible(false);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        GuiTableHeader tableHeader = new GuiTableHeader();
        GuiLabel label = new GuiLabel("x");
        tableHeader.addChild(label);
        getPreviewTable().setHeader(tableHeader);
        getPreviewTable().setShowHeader(false);
        showPreview();
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
    public GuiTable getPreviewTable() {
        return mainWindow.tablePreview;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public PictureDataType getPictureDataType() {
        return pDataType;
    }

    //Liste aller Pic-Ids
    public List<String> makePicIdList(String strValue) {
        return pDataType.makePicIdList(strValue);
    }

    //aus der Liste aller Pic-Ids neuen DB-Value bilden
    public String makePicIds(List<String> picIdList) {
        return pDataType.makePidIds(picIdList);
    }

    public boolean isShowPicture() {
        return showPicture;
    }

    public void setShowPicture(boolean showPicture) {
        if (this.showPicture != showPicture) {
            this.showPicture = showPicture;
            showPreview();
        }
    }

    public String getPictureIds() {
        return pictureIds;
    }

    public void setPictureIds(String pictureIds) {
        this.pictureIds = pictureIds;
        showPreview();
    }

    public void setShowSubTitle(boolean value) {
        mainWindow.labelPreview.setVisible(value);
    }

    public boolean isShowSelectButton() {
        return showSelectButton;
    }

    public void setShowSelectButton(boolean showSelectButton) {
        this.showSelectButton = showSelectButton;
        mainWindow.panelForChange.setVisible(showSelectButton);
    }

    public boolean isPreviewTextVisible() {
        return mainWindow.labelPreview.isVisible();
    }

    public void setPreviewTextVisible(boolean value) {
        mainWindow.labelPreview.setVisible(value);
    }

    public void setReadOnly(boolean readOnly) {
        mainWindow.buttonChange.setEnabled(!readOnly);
    }
//===== Getter and Setter End =====

    public String getHTMLPictureValue(String pictures) {
        if (showPicture) {
            return pDataType.formatPictureOutputForHtml(getProject(), pictures, DataTypesUsageType.ORIGINAL).getStringResult();
        } else {
            return pDataType.formatPictureOutputForHtml(getProject(), pictures, DataTypesUsageType.PRINT).getStringResult();
        }
    }

    public void showPreview() {
        GuiLabel label;
        getPreviewTable().removeRows();
        if (!pictureIds.isEmpty()) {
            GuiTableRow row = new GuiTableRow();
            String value = getHTMLPictureValue(pictureIds);
            label = new GuiLabel(value);
            row.addChild(label);
            getPreviewTable().addRow(row);
        }
    }

    private void onButtonChangeClicked(Event event) {
        if (isShowSelectButton()) {
            String newPicturesIds = EditPictureField.showPictureIds(getConnector(), null, getPictureIds());
            if (!newPicturesIds.equals(getPictureIds())) {
                setPictureIds(newPicturesIds);
                mainWindow.panelMain.fireEvent(EventCreator.createOnChangeEvent(event.getSource(), event.getReceiverId()));
            }
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
        private de.docware.framework.modules.gui.controls.GuiLabel labelPreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpanePreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable tablePreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelForChange;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonChange;

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
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            labelPreview = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelPreview.setName("labelPreview");
            labelPreview.__internal_setGenerationDpi(96);
            labelPreview.registerTranslationHandler(translationHandler);
            labelPreview.setScaleForResolution(true);
            labelPreview.setMinimumWidth(10);
            labelPreview.setMinimumHeight(10);
            labelPreview.setPaddingTop(4);
            labelPreview.setPaddingLeft(8);
            labelPreview.setPaddingBottom(4);
            labelPreview.setText("!!Vorschau");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelPreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelPreviewConstraints.setPosition("north");
            labelPreview.setConstraints(labelPreviewConstraints);
            panelMain.addChild(labelPreview);
            scrollpanePreview = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpanePreview.setName("scrollpanePreview");
            scrollpanePreview.__internal_setGenerationDpi(96);
            scrollpanePreview.registerTranslationHandler(translationHandler);
            scrollpanePreview.setScaleForResolution(true);
            scrollpanePreview.setMinimumWidth(10);
            scrollpanePreview.setMinimumHeight(10);
            scrollpanePreview.setBorderWidth(1);
            scrollpanePreview.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            tablePreview = new de.docware.framework.modules.gui.controls.table.GuiTable();
            tablePreview.setName("tablePreview");
            tablePreview.__internal_setGenerationDpi(96);
            tablePreview.registerTranslationHandler(translationHandler);
            tablePreview.setScaleForResolution(true);
            tablePreview.setMinimumWidth(10);
            tablePreview.setMinimumHeight(10);
            tablePreview.addEventListener(new de.docware.framework.modules.gui.event.EventListener("mouseDoubleClickedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonChangeClicked(event);
                }
            });
            tablePreview.setSelectionMode(de.docware.framework.modules.gui.controls.table.TableSelectionMode.SELECTION_MODE_NONE);
            tablePreview.setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tablePreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tablePreview.setConstraints(tablePreviewConstraints);
            scrollpanePreview.addChild(tablePreview);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpanePreviewConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpanePreview.setConstraints(scrollpanePreviewConstraints);
            panelMain.addChild(scrollpanePreview);
            panelForChange = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelForChange.setName("panelForChange");
            panelForChange.__internal_setGenerationDpi(96);
            panelForChange.registerTranslationHandler(translationHandler);
            panelForChange.setScaleForResolution(true);
            panelForChange.setMinimumWidth(10);
            panelForChange.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelForChangeLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelForChangeLayout.setCentered(false);
            panelForChange.setLayout(panelForChangeLayout);
            buttonChange = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonChange.setName("buttonChange");
            buttonChange.__internal_setGenerationDpi(96);
            buttonChange.registerTranslationHandler(translationHandler);
            buttonChange.setScaleForResolution(true);
            buttonChange.setMinimumWidth(10);
            buttonChange.setMinimumHeight(10);
            buttonChange.setMaximumWidth(21);
            buttonChange.setMaximumHeight(21);
            buttonChange.setMnemonicEnabled(true);
            buttonChange.setText("...");
            buttonChange.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonChangeClicked(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonChangeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "n", "n", 0, 1, 8, 0);
            buttonChange.setConstraints(buttonChangeConstraints);
            panelForChange.addChild(buttonChange);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelForChangeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelForChangeConstraints.setPosition("east");
            panelForChange.setConstraints(panelForChangeConstraints);
            panelMain.addChild(panelForChange);
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