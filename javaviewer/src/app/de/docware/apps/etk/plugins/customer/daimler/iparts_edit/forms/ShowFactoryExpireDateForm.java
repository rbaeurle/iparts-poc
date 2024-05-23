/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeriesExpireDate;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSeriesExpireDateId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Dialog zur Darstellung der Auslauftermine zur Ausführungsart einer Baureihe
 */
public class ShowFactoryExpireDateForm extends AbstractJavaViewerForm implements iPartsConst {

    public static void showFactoryExpireDate(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             iPartsSeriesId seriesId, String seriesAA, List<iPartsDataSeriesExpireDate> seriesExpireDateList) {
        ShowFactoryExpireDateForm dlg = new ShowFactoryExpireDateForm(dataConnector, parentForm, seriesId, seriesAA);
        dlg.setTitle("!!Anzeige Auslauftermine");
        dlg.init(seriesExpireDateList);
        dlg.showModal();
    }

    private final iPartsSeriesId seriesId;
    private final String seriesAA;
    private DataObjectFilterGrid grid;

    /**
     * Erzeugt eine Instanz von ShowFactoryExpireDateForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public ShowFactoryExpireDateForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                     iPartsSeriesId seriesId, String seriesAA) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.seriesId = seriesId;
        this.seriesAA = seriesAA;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        grid = new DataObjectFilterGrid(getConnector(), this);

        grid.setDisplayFields(buildDisplayFields());
        grid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelMain.addChild(grid.getGui());
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        close();
        return modalResult;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setSubTitle(String subTitle) {
        mainWindow.title.setSubtitle(subTitle);
    }

    /**
     * Initialisierung mit {@link iPartsDataAuthorOrder}
     *
     * @param seriesExpireDateList
     */
    public void init(List<iPartsDataSeriesExpireDate> seriesExpireDateList) {
        fillGrid(seriesExpireDateList);
    }

    /**
     * Grid füllen
     *
     * @param seriesExpireDateList
     */
    private void fillGrid(List<iPartsDataSeriesExpireDate> seriesExpireDateList) {
        grid.clearGrid();
        calcSubTitle(seriesExpireDateList);
        for (iPartsDataSeriesExpireDate dataSeriesExpireDate : seriesExpireDateList) {
            iPartsDataFactories dataFactories = iPartsFactories.getInstance(getProject()).getDataFactoryByFactoryNumber(dataSeriesExpireDate.getAsId().getSeriesFactoryNo());
            if (dataFactories == null) {
                dataFactories = new iPartsDataFactories(getProject(), null);
                dataFactories.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            }
            grid.addObjectToGrid(dataSeriesExpireDate, dataFactories);
        }
        grid.showNoResultsLabel(grid.getTable().getRowCount() == 0);
    }

    private void calcSubTitle(List<iPartsDataSeriesExpireDate> seriesExpireDateList) {
        StringBuilder subTitle = new StringBuilder();
        String currentSeriesNo = "";
        String currentSeriesAA = "";
        if (seriesId == null) {
            if (!seriesExpireDateList.isEmpty()) {
                iPartsSeriesExpireDateId seriesExpireDateId = seriesExpireDateList.get(0).getAsId();
                currentSeriesNo = seriesExpireDateId.getSeriesNumber();
                currentSeriesAA = seriesExpireDateId.getSeriesAA();
            }
        } else {
            currentSeriesNo = seriesId.getSeriesNumber();
            currentSeriesAA = seriesAA;
        }
        if (StrUtils.isValid(currentSeriesAA)) {
            EtkDisplayFields displayFields = grid.getDisplayFields();
            addToSubTitle(subTitle, displayFields, FIELD_DSED_SERIES_NO, currentSeriesNo);
            addToSubTitle(subTitle, displayFields, FIELD_DSED_AA, currentSeriesAA);
        }
        setSubTitle(subTitle.toString());
    }

    private void addToSubTitle(StringBuilder subTitle, EtkDisplayFields displayFields, String fieldName, String value) {
        if (!displayFields.contains(TABLE_DA_SERIES_EXPDATE, fieldName, false)) {
            if (subTitle.toString().length() > 0) {
                subTitle.append(" - ");
            }
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_SERIES_EXPDATE, fieldName, false, false);
            displayField.loadStandards(getConfig());
            subTitle.append(TranslationHandler.translate(displayField.getText()));
            subTitle.append(": ");
            subTitle.append(value);
        }
    }

    /**
     * Display-Fields bestimmen
     *
     * @return
     */
    private EtkDisplayFields buildDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_SERIES_EXPDATE, FIELD_DSED_FACTORY_NO, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_FACTORIES, FIELD_DF_DESC, true, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_SERIES_EXPDATE, FIELD_DSED_EXP_DATE, false, false);
        displayFields.addFeld(displayField);

        displayFields.loadStandards(getConfig());
        return displayFields;
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