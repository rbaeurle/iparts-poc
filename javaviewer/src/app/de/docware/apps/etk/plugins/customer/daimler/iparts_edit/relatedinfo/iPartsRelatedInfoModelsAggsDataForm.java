/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsCodeMatrixDialog;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * RelatedInfoForm für die Anzeige der zugeordneten After-Sales Aggregate-/Fahrzeug-Baumuster zu einem Fahrzeug-/Aggregate-Baumuster.
 */

public class iPartsRelatedInfoModelsAggsDataForm extends AbstractRelatedInfoPartlistDataForm {

    public static final String IPARTS_MENU_ITEM_SHOW_MODELS_AGGS_DATA = "iPartsMenuItemShowModelsAggsData";

    private GuiMenuItem codeMasterDataMenuItem;
    private GuiMenuItem saasModelsDataMenuItem;
    private DataObjectFilterGrid dataGrid;
    private iPartsModelId modelId;

    public static GuiMenuItem createMenuItem(final ModelAggsDataCallback callback, final AbstractJavaViewerForm parentForm) {
        GuiMenuItem menuItem = new GuiMenuItem();
        menuItem.setUserObject(IPARTS_MENU_ITEM_SHOW_MODELS_AGGS_DATA);
        menuItem.setName(IPARTS_MENU_ITEM_SHOW_MODELS_AGGS_DATA);
        menuItem.setText("!!Zugeordnete Baumuster anzeigen");
        menuItem.setIcon(DefaultImages.module.getImage());
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                ModelsAggsDataQuery modelsAggsDataQuery = callback.getModelsAggsDataQuery();
                if (modelsAggsDataQuery == null) {
                    modelsAggsDataQuery = new ModelsAggsDataQuery();
                }
                RelatedInfoFormConnector relatedInfoFormConnector = new RelatedInfoFormConnector(parentForm.getConnector());
                iPartsRelatedInfoModelsAggsDataForm modelAggsDataForm = new iPartsRelatedInfoModelsAggsDataForm(modelsAggsDataQuery,
                                                                                                                relatedInfoFormConnector,
                                                                                                                parentForm);
                modelAggsDataForm.addOwnConnector(relatedInfoFormConnector);
                modelAggsDataForm.showModal();
            }
        });

        return menuItem;
    }

    public static void updateMenuItem(GuiMenuItem menuItem, iPartsModelId modelId) {
        if (modelId.isAggregateModel()) {
            menuItem.setText("!!Fahrzeugbaumuster");
        } else {
            menuItem.setText("!!Aggregatebaumuster");
        }
    }

    /**
     * Erzeugt ein neues RelatedInfoForm für die Anzeige der zugeordneten Aggregate-/Fahrzeug-Baumuster zu einem Fahrzeug-/Aggregate-Baumuster.
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsRelatedInfoModelsAggsDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Erzeugt einen neuen Dialog für die Anzeige der zugeordneten Aggregate-/Fahrzeug-Baumuster zu einem Fahrzeug-/Aggregate-Baumuster
     * basierend auf dem Baumuster aus der übergebenen {@link ModelsAggsDataQuery}.
     *
     * @param modelsAggsDataQuery
     * @param dataConnector
     * @param parentForm
     */
    public iPartsRelatedInfoModelsAggsDataForm(ModelsAggsDataQuery modelsAggsDataQuery, RelatedInfoBaseFormIConnector dataConnector,
                                               AbstractJavaViewerForm parentForm) {
        this(dataConnector, parentForm, null);
        setModelId(modelsAggsDataQuery.getModelId());
        setTitleVisible(true);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        dataGrid = new DataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                super.createContextMenuItems(contextMenu);

                // Popup-Menüeintrag für Code-Stammdaten
                codeMasterDataMenuItem = iPartsCodeMatrixDialog.createMenuItem(new iPartsCodeMatrixDialog.CodeMasterDataCallback() {
                    @Override
                    public iPartsCodeMatrixDialog.CodeMasterDataQuery getCodeMasterDataQuery() {
                        List<EtkDataObject> selection = dataGrid.getSelection();
                        if (selection != null) {
                            EtkDataObject dataModel = selection.get(0);
                            return new iPartsCodeMatrixDialog.CodeMasterDataQuery(dataModel.getFieldValue(iPartsConst.FIELD_DM_CODE),
                                                                                  new iPartsSeriesId(dataModel.getFieldValue(iPartsConst.FIELD_DM_SERIES_NO)),
                                                                                  dataModel.getFieldValue(iPartsConst.FIELD_DM_PRODUCT_GRP),
                                                                                  dataModel.getFieldValue(iPartsConst.FIELD_DM_DATA));
                        } else {
                            return null;
                        }
                    }
                }, this);

                contextMenu.addChild(codeMasterDataMenuItem);

                // Popup-Menüeintrag für SAA-Gültigkeiten
                saasModelsDataMenuItem = iPartsRelatedInfoSAAsModelsDataForm.createMenuItem(new iPartsRelatedInfoSAAsModelsDataForm.SAAsModelsDataCallback() {

                    @Override
                    public iPartsRelatedInfoSAAsModelsDataForm.SAAsModelsDataQuery getSAAsModelsDataQuery() {
                        List<EtkDataObject> selection = getSelection();
                        if (selection != null) {
                            EtkDataObject dataModel = selection.get(0);
                            return new iPartsRelatedInfoSAAsModelsDataForm.SAAsModelsDataQuery(new iPartsModelId(dataModel.getFieldValue(iPartsConst.FIELD_DM_MODEL_NO)));
                        } else {
                            return null;
                        }
                    }
                }, this);

                contextMenu.addChild(saasModelsDataMenuItem);
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                boolean singleSelection = getTable().getSelectedRows().size() == 1;
                saasModelsDataMenuItem.setEnabled(singleSelection);

                boolean enabled = false;
                if (singleSelection && dataGrid.getSelection().size() > 0) { // nur eine Zeile darf selektiert sein
                    String codeString = dataGrid.getSelection().get(0).getFieldValue(iPartsConst.FIELD_DM_CODE);
                    enabled = !DaimlerCodes.isEmptyCodeString(codeString);
                }
                codeMasterDataMenuItem.setEnabled(enabled);
            }
        };
        dataGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        dataGrid.getGui().setConstraints(constraints);
        mainWindow.gridPanel.addChild(dataGrid.getGui());
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.gridPanel;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if ((getConnector().getActiveRelatedSubForm() == this)) {
            dataToGrid(modelId); // die modelId müsste bei Verwendung als RelatedInfo aus der aktuellen Selektion ermittelt werden
        }
    }

    public void setModelId(iPartsModelId modelId) {
        dataToGrid(modelId);
    }

    public String getTitleText() {
        if (modelId == null) {
            return "!!Kein Baumuster selektiert";
        } else if (modelId.isAggregateModel()) {
            return TranslationHandler.translate("!!Gültige Fahrzeug-Baumuster zu Aggregate-Baumuster \"%1\"", modelId.getModelNumber());
        } else {
            return TranslationHandler.translate("!!Gültige Aggregate-Baumuster zu Fahrzeug-Baumuster \"%1\"", modelId.getModelNumber());
        }
    }

    public void setTitleVisible(boolean visible) {
        mainWindow.title.setVisible(visible);
    }

    protected void dataToGrid(iPartsModelId modelId) {
        if (dataGrid.getDisplayFields() == null) {
            dataGrid.setDisplayFields(getDisplayFields(iPartsRelatedInfoModelMasterDataForm.CONFIG_KEY_MODEL_MASTER_DATA));
        }
        fillGrid(modelId);
    }

    /**
     * Füllt das Grid mit den Baumustern für das Produkt der aktuellen Assembly
     *
     * @param modelId
     */
    private void fillGrid(iPartsModelId modelId) {
        if (!hasIdChanged(modelId)) {
            return;
        }
        this.modelId = modelId;

        dataGrid.clearGrid();

        boolean isAggregateModel = modelId.isAggregateModel();
        iPartsDataModelsAggsList modelsAggsList;
        if (isAggregateModel) {
            modelsAggsList = iPartsDataModelsAggsList.loadDataModelsAggsListForAggregateModel(getProject(), modelId.getModelNumber());
        } else {
            modelsAggsList = iPartsDataModelsAggsList.loadDataModelsAggsListForModel(getProject(), modelId.getModelNumber());
        }
        for (iPartsDataModelsAggs dataModelAgg : modelsAggsList) {
            if (iPartsImportDataOrigin.isSourceVisible(dataModelAgg.getSource(), isCarAndVanInSession(), isTruckAndBusInSession())) {
                dataGrid.addObjectToGrid(new iPartsDataModel(getProject(), new iPartsModelId(isAggregateModel ? dataModelAgg.getAsId().getModelNumber()
                                                                                                              : dataModelAgg.getAsId().getAggregateModelNumber())));
            }
        }
        dataGrid.showNoResultsLabel(dataGrid.getTable().getRowCount() == 0);

        mainWindow.title.setTitle(getTitleText());
    }

    /**
     * Abfrage, ob die übergebene ID identisch ist zur aktuellen ID
     *
     * @param modelId
     * @return
     */
    private boolean hasIdChanged(iPartsModelId modelId) {
        return !Utils.objectEquals(modelId, this.modelId);
    }

    /**
     * Gibt eine Liste mit default Anzeigefelder zurück
     *
     * @return
     */
    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_MODEL_NO, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_CODE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_AA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_SALES_TITLE, true, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_DATA, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    private void closeWindow(Event event) {
        close();
        mainWindow.setVisible(false);
    }


    /**
     * Callback zur Bestimmung der Daten für die Anzeige der verwendeten Aggregate-/Fahrzeug-Baumuster zu einem Fahrzeug-/Aggregate-Baumuster.
     */
    public static abstract interface ModelAggsDataCallback {

        ModelsAggsDataQuery getModelsAggsDataQuery();
    }


    /**
     * Abfrageinformationen zur Bestimmung der Daten für die Anzeige der verwendeten Aggregate-/Fahrzeug-Baumuster zu einem
     * Fahrzeug-/Aggregate-Baumuster.
     */
    public static class ModelsAggsDataQuery {

        iPartsModelId modelId;

        public ModelsAggsDataQuery(iPartsModelId modelId) {
            this.modelId = modelId;
        }

        public ModelsAggsDataQuery() {
            this(new iPartsModelId());
        }

        public iPartsModelId getModelId() {
            return modelId;
        }

        public void setModelId(iPartsModelId modelId) {
            this.modelId = modelId;
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
        private de.docware.framework.modules.gui.controls.GuiPanel gridPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(1000);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(0);
            title.setMinimumHeight(50);
            title.setVisible(false);
            title.setBorderWidth(0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            gridPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            gridPanel.setName("gridPanel");
            gridPanel.__internal_setGenerationDpi(96);
            gridPanel.registerTranslationHandler(translationHandler);
            gridPanel.setScaleForResolution(true);
            gridPanel.setMinimumWidth(10);
            gridPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder gridPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            gridPanel.setLayout(gridPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder gridPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            gridPanel.setConstraints(gridPanelConstraints);
            this.addChild(gridPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCloseActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    closeWindow(event);
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