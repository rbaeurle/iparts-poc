/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractTwoDataObjectGridsForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.MasterDataProductForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsCodeMatrixDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * RelatedInfoForm für die Anzeige der Baumuster zu einem Produkt bzw. Baureihe
 */

public class iPartsRelatedInfoModelMasterDataForm extends AbstractTwoDataObjectGridsForm {

    public static final String IPARTS_MENU_ITEM_SHOW_MODELS = "iPartsMenuItemShowModels";
    public static final String IPARTS_MENU_ITEM_SHOW_MODEL_TO_PRODUCT = "iPartsMenuItemShowModelToProduct";
    public static final String CONFIG_KEY_MODEL_MASTER_DATA = "Plugin/iPartsEdit/ModelMasterData";
    public static final String CONFIG_KEY_MODEL_MASTER_AGGREGAT_DATA = "Plugin/iPartsEdit/ModelMasterAggregatData";

    private static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.PRODUCT_MODEL,
                                                                                    iPartsModuleTypes.CONSTRUCTION_SERIES);

    private AssemblyId currentAssemblyId;

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_MODELS, "!!Baumuster", DefaultImages.module.getImage(),
                                iPartsConst.CONFIG_KEY_RELATED_INFO_MODEL_MASTER_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        updatePartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_MODELS, VALID_MODULE_TYPES);
    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_MODEL_TO_PRODUCT, iPartsConst.RELATED_INFO_MODEL_MASTER_DATA_TEXT,
                            iPartsConst.CONFIG_KEY_RELATED_INFO_MODEL_MASTER_DATA);
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_MODEL_TO_PRODUCT, VALID_MODULE_TYPES);
    }

    public iPartsRelatedInfoModelMasterDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, CONFIG_KEY_MODEL_MASTER_DATA, "!!Baumuster", CONFIG_KEY_MODEL_MASTER_AGGREGAT_DATA,
              "!!Zugeordnete Baumuster", true);
    }

    @Override
    protected DataObjectGrid createGrid(final boolean top) {
        DataObjectFilterGrid dataGrid = new DataObjectFilterGrid(getConnector(), this) {

            private GuiMenuItem codeMasterDataMenuItem;
            private GuiMenuItem saasModelsDataMenuItem;
            private GuiMenuItem showProductsMenuItem;


            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                super.createContextMenuItems(contextMenu);

                // Popup-Menüeintrag für Code-Stammdaten
                codeMasterDataMenuItem = iPartsCodeMatrixDialog.createMenuItem(new iPartsCodeMatrixDialog.CodeMasterDataCallback() {
                    @Override
                    public iPartsCodeMatrixDialog.CodeMasterDataQuery getCodeMasterDataQuery() {
                        List<EtkDataObject> selection = getSelection();
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

                showProductsMenuItem = toolbarHelper.createMenuEntry("productanzeige", "!!Produkte anzeigen...", EditDefaultImages.edit_product.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doShowProducts(event);
                    }
                }, getUITranslationHandler());
                contextMenu.addChild(showProductsMenuItem);
            }

            private void doShowProducts(Event event) {
                List<EtkDataObject> selection = getSelection();
                if (selection != null) {
                    EtkDataObject dataModel = selection.get(0);
                    iPartsModelId modelId = new iPartsModelId(dataModel.getAsId().toStringArrayWithoutType()[0]);
                    MasterDataProductForm.showProductMasterDataForModel(getConnector(), this, modelId);
                }
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                boolean singleSelection = getTable().getSelectedRows().size() == 1;
                boolean enabled = false;
                if (singleSelection && getSelection().size() > 0) { // nur eine Zeile darf selektiert sein
                    String codeString = getSelection().get(0).getFieldValue(iPartsConst.FIELD_DM_CODE);
                    enabled = !DaimlerCodes.isEmptyCodeString(codeString);
                }
                codeMasterDataMenuItem.setEnabled(enabled);

                saasModelsDataMenuItem.setEnabled(singleSelection);
                showProductsMenuItem.setEnabled(singleSelection);

                // Bei Selektionsänderung in der oberen Baumustertabelle die zugeordneten Baumuster aktualisieren
                if (top) {
                    dataToGrid(false);
                }
            }
        };
        return dataGrid;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if ((getConnector().getActiveRelatedSubForm() == this)) {
            dataToGrid();
        }
    }

    @Override
    protected void dataToGrid() {
        AssemblyId assemblyId = getConnector().getRelatedInfoData().getSachAssemblyId();
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            // ist virtuell, also iPartsAssembly erstellen
            assemblyId = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId).getAsId();
            if (hasIdChanged(assemblyId)) {
                currentAssemblyId = assemblyId;
                super.dataToGrid();
            }
        }
    }

    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
        DBDataObjectList<iPartsDataModel> modelList = new DBDataObjectList<iPartsDataModel>();
        if (top) {
            boolean isAggregate;
            String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(currentAssemblyId);
            if ((productNumber != null) && !productNumber.isEmpty()) {
                iPartsProduct product = iPartsProduct.getInstance(getProject(), new iPartsProductId(productNumber));
                for (String model : product.getModelNumbers(getProject())) {
                    modelList.add(new iPartsDataModel(getProject(), new iPartsModelId(model)), DBActionOrigin.FROM_DB);
                }
                isAggregate = product.isAggregateProduct(getProject());
            } else {
                String seriesNumber = iPartsVirtualNode.getSeriesNumberFromAssemblyId(currentAssemblyId);
                if ((seriesNumber != null) && !seriesNumber.isEmpty()) {
                    iPartsDataModelList dataObjects = iPartsDataModelList.loadDataModelList(getProject(), seriesNumber, DBDataObjectList.LoadType.COMPLETE);
                    for (iPartsDataModel dataModel : dataObjects) {
                        modelList.add(dataModel, DBActionOrigin.FROM_DB);
                    }
                }
                isAggregate = iPartsDialogSeries.getInstance(getProject(), new iPartsSeriesId(seriesNumber)).isAggregateSeries();

            }
            if (isAggregate) {
                setGridTopTitle("!!Aggregatebaumuster:");
                setGridBottomTitle("!!Gültige Fahrzeugbaumuster:");
            } else {
                setGridTopTitle("!!Fahrzeugbaumuster:");
                setGridBottomTitle("!!Gültige Aggregatebaumuster:");
            }
            for (EtkDataObject factoryData : modelList) {
                if (!StrUtils.isEmpty(productNumber)) {
                    iPartsProductModelsId id = new iPartsProductModelsId(productNumber, factoryData.getAsId().getValue(1));
                    iPartsDataProductModels dataProductModels = new iPartsDataProductModels(getProject(), id);
                    addDataObjectToGrid(top, factoryData, dataProductModels);
                } else {
                    addDataObjectToGrid(top, factoryData);
                }
            }
        } else {
            List<EtkDataObject> selectedData = gridTop.getSelection();
            if (selectedData != null) {
                iPartsDataModel dataModel = (iPartsDataModel)selectedData.get(0);
                iPartsModel model = iPartsModel.getInstance(getProject(), dataModel.getAsId());

                iPartsDataModelsAggsList modelsAggsList;
                boolean isAggregateModel = model.isAggregateModel();
                if (isAggregateModel) {
                    modelsAggsList = iPartsDataModelsAggsList.loadDataModelsAggsListForAggregateModel(getProject(), dataModel.getAsId().getModelNumber());
                } else {
                    modelsAggsList = iPartsDataModelsAggsList.loadDataModelsAggsListForModel(getProject(), dataModel.getAsId().getModelNumber());
                }
                for (iPartsDataModelsAggs dataModelAgg : modelsAggsList) {
                    iPartsModelId linkedModelId = new iPartsModelId(isAggregateModel ? dataModelAgg.getAsId().getModelNumber()
                                                                                     : dataModelAgg.getAsId().getAggregateModelNumber());
                    modelList.add(new iPartsDataModel(getProject(), linkedModelId), DBActionOrigin.FROM_DB);
                }
            }
            addDataObjectListToGrid(top, modelList);
        }
    }

    /**
     * Abfrage, ob die übergebene ID identisch ist zur aktuellen ID
     *
     * @param assemblyId
     * @return
     */
    private boolean hasIdChanged(AssemblyId assemblyId) {
        return !Utils.objectEquals(assemblyId, currentAssemblyId);
    }

    /**
     * Gibt eine Liste mit default Anzeigefelder zurück
     *
     * @return
     */
    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(boolean top) {
        // DefaultDisplayFields sind für top und bottom identisch
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_MODEL_NO, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_CODE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_AA, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_SALES_TITLE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_DATA, false, false);
        defaultDisplayFields.add(displayField);

        if (top) {
            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT_MODELS, iPartsConst.FIELD_DPM_STEERING, false, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT_MODELS, iPartsConst.FIELD_DPM_TEXTNR, true, false);
            displayField.setColumnFilterEnabled(true);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT_MODELS, iPartsConst.FIELD_DPM_VALID_FROM, false, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT_MODELS, iPartsConst.FIELD_DPM_VALID_TO, false, false);
            defaultDisplayFields.add(displayField);
        }
        return defaultDisplayFields;
    }
}