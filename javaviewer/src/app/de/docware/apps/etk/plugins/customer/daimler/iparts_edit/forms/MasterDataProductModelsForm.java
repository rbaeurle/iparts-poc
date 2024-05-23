/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnStartSearchEvent;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.collections4.map.LRUMap;

import java.util.*;

/**
 * Formular für die Anzeige der Stammdaten von Baumustern zu Produkten (Tabelle TABLE_DA_PRODUCT_MODELS).
 */
public class MasterDataProductModelsForm extends SimpleMasterDataSearchFilterGrid {

    public static void showProductModelsMasterData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        iPartsProductId productId = new iPartsProductId(getIdFromTreeSelectionForType(activeForm, iPartsProductId.TYPE));
        if (!productId.isValidId()) {
            productId = null;
        }
        showProductModelsMasterData(activeForm.getConnector(), activeForm, productId, null);
    }

    public static void showProductModelsMasterData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   iPartsProductId productId, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        // keine Edit/New Anzeige
        if (onEditChangeRecordEvent == null) {
//            // Falls nicht extern definiert: Hier die Callbacks für Edit, Modify und Delete definieren
//            onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
//                private boolean onEditCreateOrModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
//                                                                IdWithType id, DBDataObjectAttributes attributes, boolean createRecord) {
//                    EtkProject project = dataConnector.getProject();
//                    iPartsSAAModelsId saaModelsId = new iPartsSAAModelsId(id.getValue(1), id.getValue(2));
//                    iPartsDataSAAModels dataSAAModels = new iPartsDataSAAModels(project, saaModelsId);
//                    if (dataSAAModels.loadFromDB(saaModelsId) && createRecord) {
//                        String msg = "!!Die SAA/BK-Gültigkeit zu Baumuster ist bereits vorhanden und kann nicht neu angelegt werden!";
//                        MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
//                        return true;
//                    }
//                    if (createRecord) {
//                        dataSAAModels.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
//                    }
//                    dataSAAModels.assignAttributesValues(project, attributes, true, DBActionOrigin.FROM_EDIT);
//                    project.getDbLayer().startTransaction();
//                    try {
//                        dataSAAModels.saveToDB();
//                        project.getDbLayer().commit();
//                        return true;
//                    } catch (Exception e) {
//                        project.getDbLayer().rollback();
//                        Logger.getLogger().handleRuntimeException(e);
//                    }
//                    return false;
//                }
//
//                @Override
//                public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
//                    return onEditCreateOrModifyRecordEvent(dataConnector, tableName, id, attributes, true);
//                }
//
//                @Override
//                public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
//                    return onEditCreateOrModifyRecordEvent(dataConnector, tableName, id, attributes, false);
//                }
//
//                @Override
//                public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
//                    if ((attributeList != null) && !attributeList.isEmpty()) {
//                        String msg = "!!Wollen Sie die selektierte SAA/BK-Gültigkeit zu Baumuster wirklich löschen?";
//                        if (attributeList.size() > 1) {
//                            msg = "!!Wollen Sie die selektierten SAA/BK-Gültigkeiten zu Baumuster wirklich löschen?";
//                        }
//                        if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
//                                               MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
//                            return true;
//                        }
//                    }
//                    return false;
//                }
//
//                @Override
//                public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributesList) {
//                    if ((attributesList != null) && !attributesList.isEmpty()) {
//                        EtkProject project = dataConnector.getProject();
//                        EtkDbObjectsLayer dbLayer = project.getDbLayer();
//                        dbLayer.startTransaction();
//                        dbLayer.startBatchStatement();
//                        try {
//                            for (DBDataObjectAttributes attributes : attributesList) {
//                                iPartsSAAModelsId saaModelsId = new iPartsSAAModelsId(attributes.getField(FIELD_DA_ESM_SAA_NO).getAsString(),
//                                                                                      attributes.getField(FIELD_DA_ESM_MODEL_NO).getAsString());
//                                iPartsDataSAAModels dataSAAModels = new iPartsDataSAAModels(project, saaModelsId);
//                                dataSAAModels.deleteFromDB(true);
//                            }
//                            dbLayer.endBatchStatement();
//                            dbLayer.commit();
//                            return true;
//                        } catch (Exception e) {
//                            dbLayer.cancelBatchStatement();
//                            dbLayer.rollback();
//                            Logger.getLogger().handleRuntimeException(e);
//                        }
//                    }
//                    return false;
//                }
//            };
        }

        MasterDataProductModelsForm dlg = new MasterDataProductModelsForm(dataConnector, parentForm, TABLE_DA_MODEL, onEditChangeRecordEvent);
        EtkProject project = dataConnector.getProject();

        // Suchfelder laden
        EtkDisplayFields searchFields = new EtkDisplayFields();
        // SearchFields sind fest und können nicht konfiguriert werden
        // searchFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_PRODUCT_MODEL_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS);
        if (searchFields.size() == 0) {
            //Suchfelder definieren
            addSearchField(TABLE_DA_MODEL, FIELD_DM_MODEL_NO, "!!Baumusternummer", project, searchFields);
            addSearchField(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO, "!!Produkt", project, searchFields);
        }

        // Anzeigefelder laden
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_PRODUCT_MODEL_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            // Anzeigefelder definieren
            EtkDisplayField displayField = addDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_NO, false, false, "!!Baumusternummer", project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField = addDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_VISIBLE, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_MODEL, FIELD_DM_SALES_TITLE, true, false, "!!Baumusterbenennung", project, displayFields);
            displayField = addDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, false, "!!Produkt", project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_STRUCTURING_TYPE, false, false, null, project, displayFields);
            displayField = addDisplayField(TABLE_DA_PRODUCT, FIELD_DP_SERIES_REF, false, false, "!!Baureihe", project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_VALID_FROM, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_VALID_TO, false, false, null, project, displayFields);
        }

        // Benötigte Ergebnisfelder (abgesehen von den Primärschlüsselfeldern) definieren
        EtkDisplayFields requiredResultFields = new EtkDisplayFields();
        requiredResultFields.addFeld(createDisplayField(project, TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_NO, false, false));
        requiredResultFields.addFeld(createDisplayField(project, TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, false));
        requiredResultFields.addFeld(createDisplayField(project, TABLE_DA_MODEL, FIELD_DM_MODEL_NO, false, false));
        requiredResultFields.addFeld(createDisplayField(project, TABLE_DA_MODEL, FIELD_DM_SOURCE, false, false));
        requiredResultFields.addFeld(createDisplayField(project, TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO, false, false));

        // Editfelder fürs Editieren laden
        EtkEditFields editFields = new EtkEditFields();
        // EditFields sind im Augenblick nicht freigegeben => kein Laden der Konfig
        //editFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_PRODUCT_MODEL_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            // Editfelder fürs Editieren festlegen
            addEditField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_NO, false, "!!Baumusternummer", project, editFields);
            addEditField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, "!!Produkt", project, editFields);
            addEditField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_STEERING, false, "!!Lenkung", project, editFields);
            addEditField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_TEXTNR, true, "!!Benennung", project, editFields);
            addEditField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_VALID_FROM, false, "!!Gültig ab", project, editFields);
            addEditField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_VALID_TO, false, "!!Gültig bis", project, editFields);
        }
        EtkDatabaseTable tableDef = dataConnector.getConfig().getDBDescription().findTable(TABLE_DA_PRODUCT_MODELS);
        List<String> pkFields = tableDef.getPrimaryKeyFields();
        for (EtkEditField eField : editFields.getFields()) {
            if (pkFields.contains(eField.getKey().getFieldName())) {
                eField.setMussFeld(true);
                eField.setEditierbar(false);  // ist PK-Value => nicht editierbar
            }
        }

        // Sortierung erfolgt erst in buildQuery abhängig von den Suchfeldern

        // keine Edit/New/Del Anzeige
        dlg.setEditAllowed(false);
        dlg.setMaxSearchControlsPerRow(3);
        dlg.setDisplayResultFields(displayFields);
        dlg.setRequiredResultFields(requiredResultFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        dlg.setAdditionalFields();
        dlg.setTitlePrefix("!!Produkt zu Baumuster");
        dlg.setWindowName("ProductModelsMasterData");
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);

        if ((productId != null) && productId.isValidId()) {
            // Suchwerte setzen und Suche starten
            DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
            searchAttributes.addField(FIELD_DP_PRODUCT_NO, productId.getProductNumber(), DBActionOrigin.FROM_DB);
            dlg.setSearchValues(searchAttributes);
        }
        dlg.showModal();
    }

    private Map<IdWithType, EtkDataObject> dataObjectCache;
    private Map<String, Set<String>> additionalFields;
    private Set<String> additionalVirtualFields;
    private Map<String, String> virtualFieldMapping;
    private iPartsDataProductModelsList productModelsList;
    private iPartsDataProductModels dataProductModels;
    private boolean carAndVanInSession = iPartsRight.checkCarAndVanInSession();
    private boolean truckAndBusInSession = iPartsRight.checkTruckAndBusInSession();
    private boolean isPSKAllowed = iPartsRight.checkPSKInSession();
    private Map<iPartsProductId, Boolean> productVisibleInSessionMap = new HashMap<>();

    private GuiMenuItem showModelMenuItem;
    private GuiMenuItem showProductsMenuItem;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public MasterDataProductModelsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
        dataObjectCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_MASTER_DATA);
        additionalFields = new LinkedHashMap<>();
        additionalVirtualFields = new HashSet<>();
        setColumnFilterFactory(new SimpleMasterDataSearchFilterProductModelFactory(getProject()));
        setOnStartSearchEvent(new OnStartSearchEvent() {
            @Override
            public void onStartSearch() {
                dataObjectCache.clear();
            }
        });
        initVirtualMapping();
    }

    /**
     * Mapping für virtuelle Felder (Abhängigkeit bezüglich BK und SAA)
     */
    private void initVirtualMapping() {
        virtualFieldMapping = new HashMap<String, String>();
//        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_DA_EDS_SAAD, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION), FIELD_EDS_DESC);
//        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_DA_EDS_SAAD, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION_CONST), FIELD_EDS_SAAD);
//        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION), FIELD_M_TEXTNR);
//        virtualFieldMapping.put(makeVirtualMappinKey(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION_CONST), FIELD_M_CONST_DESC);

    }

    private String makeVirtualMappinKey(String tablename, String virtualField) {
        return tablename + "||" + virtualField;
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        GuiSeparator separator = new de.docware.framework.modules.gui.controls.GuiSeparator();
        separator.setName("menuSeparator1");
        contextMenu.addChild(separator);

        showModelMenuItem = toolbarHelper.createMenuEntry("bmanzeige", "!!Baumuster anzeigen...", DefaultImages.module.getImage(), new de.docware.framework.modules.gui.event.EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doShowModels(event);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(showModelMenuItem);

        showProductsMenuItem = toolbarHelper.createMenuEntry("productanzeige", "!!Produkte anzeigen...", EditDefaultImages.edit_product.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doShowProducts(event);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(showProductsMenuItem);
    }

    private void doShowModels(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            iPartsProductId productId = new iPartsProductId(attributes.getField(FIELD_DP_PRODUCT_NO, false).getAsString());
            MasterDataModelAfterSalesForm.showModelAfterSalesForProductData(getConnector(), this, productId);
        }
    }

    private void doShowProducts(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            iPartsModelId modelId = new iPartsModelId(attributes.getField(FIELD_DM_MODEL_NO, false).getAsString());
            MasterDataProductForm.showProductMasterDataForModel(getConnector(), this, modelId);
        }
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();

        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelection = selectionRowCount == 1;

        showModelMenuItem.setEnabled(singleSelection);
        showProductsMenuItem.setEnabled(singleSelection);
    }

    private void setAdditionalFields() {
        additionalFields.clear();

        // Zusätzliche Felder aus den Display und Edit Fields bestimmen
        List<String> allFieldNames = new DwList<>();
        for (EtkDisplayField field : displayResultFields.getFields()) {
            allFieldNames.add(field.getKey().getName());
        }
        for (EtkEditField field : editFields.getFields()) {
            allFieldNames.add(field.getKey().getName());
        }

        for (String name : allFieldNames) {
            String tablename = TableAndFieldName.getTableName(name);
            String fieldname = TableAndFieldName.getFieldName(name);
            if (!tablename.equals(searchTable)) {
                Set<String> fieldNames = additionalFields.get(tablename);
                if (fieldNames == null) {
                    fieldNames = new HashSet<>();
                    additionalFields.put(tablename, fieldNames);
                }
                fieldNames.add(fieldname);
            } else if (VirtualFieldsUtils.isVirtualField(fieldname)) {
                addVirtualDisplayField(fieldname);
            }
        }
    }

    public void addVirtualDisplayField(String... virtFields) {
        for (String field : virtFields) {
            additionalVirtualFields.add(field);
        }
    }

    @Override
    protected EtkSqlCommonDbSelect buildQuery() {
        Set<String> tableNames = new TreeSet<>();
        for (int lfdNr = 0; lfdNr < searchFields.size(); lfdNr++) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(lfdNr).getEditControl();
            String value = ctrl.getText();
            if (!StrUtils.isEmpty(value)) {
                tableNames.add(ctrl.getTableName());
            }
        }
        if (!tableNames.isEmpty()) {
            if (tableNames.contains(TABLE_DA_MODEL)) {
                searchTable = TABLE_DA_MODEL;
            } else if (tableNames.contains(TABLE_DA_PRODUCT)) {
                searchTable = TABLE_DA_PRODUCT;
            }
        }
        setAdditionalFields();
        sortFields = null;

        EtkSqlCommonDbSelect sqlSelect = super.buildQuery();

        List<String> sortKeys = new ArrayList<>(searchFields.size());
        for (EtkDisplayField searchField : searchFields.getFields()) {
            if (searchField.getKey().getTableName().equals(searchTable)) {
                sortKeys.add(searchField.getKey().getFieldName());
            }
        }
        sqlSelect.getQuery().orderBy(sortKeys.toArray(new String[sortKeys.size()]));
        sortFields = new LinkedHashMap<>();
        for (String fieldName : sortKeys) {
            sortFields.put(fieldName, false);
        }
        return sqlSelect;
    }

    private String getSearchValue(String tableName, String fieldName) {
        for (int lfdNr = 0; lfdNr < searchFields.size(); lfdNr++) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(lfdNr).getEditControl();
            if (ctrl.getTableFieldName().equals(TableAndFieldName.make(tableName, fieldName))) {
                return ctrl.getText();
            }
        }
        return "";
    }

    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        String modelNo;
        String productNo;
        if (searchTable.equals(TABLE_DA_MODEL)) {
            modelNo = attributes.getField(FIELD_DM_MODEL_NO).getAsString();
            productNo = getSearchValue(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO) + "*";
            productNo = productNo.toUpperCase();
        } else {
            modelNo = getSearchValue(TABLE_DA_MODEL, FIELD_DM_MODEL_NO) + "*";
            productNo = attributes.getField(FIELD_DP_PRODUCT_NO).getAsString();
        }
        productModelsList = iPartsDataProductModelsList.loadDataProductModelsListByLike(getProject(), modelNo, productNo);
//        return !productModelsList.isEmpty();
        return true;
    }

    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes addAttributesToGrid(DBDataObjectAttributes attributes) {
        SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = null;
        if (productModelsList.isEmpty()) {
            dataProductModels = null;
            if (getSearchValue(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO).isEmpty()) {
                if (isModelValidForUserInSession(attributes)) {
                    row = createRow(attributes);
                    if (row != null) {
                        getTable().addRow(row);
                    }
                }
            }
        } else {
            DBDataObjectAttributes holdedAttributes = attributes.cloneMe(DBActionOrigin.FROM_DB);
            int lfdNr = 0;
            while (lfdNr < productModelsList.size()) {
                dataProductModels = productModelsList.get(lfdNr);
                iPartsProductId productId = new iPartsProductId(dataProductModels.getAsId().getProductNumber());
                if (productVisibleInSessionMap.computeIfAbsent(productId, id -> isProductValidForUserInSession(id))) {
                    attributes = holdedAttributes.cloneMe(DBActionOrigin.FROM_DB);
                    attributes.addField(FIELD_DM_MODEL_NO, dataProductModels.getAsId().getModelNumber(), DBActionOrigin.FROM_DB);
                    attributes.addField(FIELD_DP_PRODUCT_NO, dataProductModels.getAsId().getProductNumber(), DBActionOrigin.FROM_DB);
                    row = createRow(attributes);
                    if (row != null) {
                        getTable().addRow(row);
                    }
                }
                lfdNr++;
            }
        }

        return row;
    }

    private boolean isModelValidForUserInSession(DBDataObjectAttributes attributes) {
        if (carAndVanInSession && truckAndBusInSession) {
            return true;
        }
        String source = attributes.getFieldValue(FIELD_DM_SOURCE);
        return iPartsFilterHelper.isDataObjectSourceValidForUserProperties(source, carAndVanInSession, truckAndBusInSession);
    }

    private boolean isProductValidForUserInSession(iPartsProductId productId) {
        iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
        return product.isProductVisibleForUserProperties(carAndVanInSession, truckAndBusInSession, isPSKAllowed);
    }

    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes createRow(DBDataObjectAttributes attributes) {
        // attributes enthält nur die Attribute für die Suchtabelle TABLE_DA_PRODUCT_MODELS
        // -> passende Attribute von TABLE_DA_MODEL und TABLE_DA_PRODUCT hinzufügen (kann dann auch in den Ergebnissen
        // angezeigt werden)
        for (String tableName : additionalFields.keySet()) {
            Set<String> fieldNames = additionalFields.get(tableName);
            for (String fieldName : fieldNames) {
                addExtraAttributes(tableName, fieldName, attributes);
            }
        }
        calculateVirtualFieldValues(attributes);

        return super.createRow(attributes);
    }

    private void addExtraAttributes(String tableName, String fieldName, DBDataObjectAttributes attributes) {
        addExtraAttributes(tableName, fieldName, "", attributes);
    }

    private void addExtraAttributes(String tableName, String fieldName, String virtFieldName, DBDataObjectAttributes attributes) {
        IdWithType id = getIdForTable(tableName, attributes);
        if (id == null) {
            return;
        }
        EtkDataObject dataObject = dataObjectCache.get(id);
        if (dataObject == null) {
            if (tableName.equals(TABLE_DA_MODEL) && id.getType().equals(iPartsModelId.TYPE)) {
                dataObject = new iPartsDataModel(getProject(), (iPartsModelId)id);
            } else if (tableName.equals(TABLE_DA_PRODUCT) && id.getType().equals(iPartsProductId.TYPE)) {
                dataObject = new iPartsDataProduct(getProject(), (iPartsProductId)id);
            } else if (tableName.equals(TABLE_DA_PRODUCT_MODELS) && id.getType().equals(iPartsProductModelsId.TYPE)) {
                if (dataProductModels != null) {
                    dataObject = dataProductModels;
                    id = dataObject.getAsId();
                    if (dataProductModels.getAsId().getModelNumber().isEmpty()) {
                        assignAttributeValues(dataObject, FIELD_DPM_MODEL_NO, FIELD_DPM_MODEL_NO, attributes);
                    } else {
                        assignAttributeValues(dataObject, FIELD_DPM_PRODUCT_NO, FIELD_DPM_PRODUCT_NO, attributes);
                    }
                } else {
                    dataObject = new iPartsDataProductModels(getProject(), (iPartsProductModelsId)id);
                }
            }
            dataObjectCache.put(id, dataObject);
        }
        loadFieldValue(dataObject, fieldName);
        assignAttributeValues(dataObject, fieldName, (StrUtils.isEmpty(virtFieldName) ? fieldName : virtFieldName), attributes);
    }

    /**
     * Gibt in Abhängigkeit der übergebenen Tabelle die zugehörige {@link IdWithType} zurück
     *
     * @param tableName
     * @param attributes
     * @return
     */
    private IdWithType getIdForTable(String tableName, DBDataObjectAttributes attributes) {
        if (tableName.equals(TABLE_DA_MODEL)) {
            return new iPartsModelId(attributes.getField(FIELD_DM_MODEL_NO).getAsString());
        } else if (tableName.equals(TABLE_DA_PRODUCT)) {
            return new iPartsProductId(attributes.getField(FIELD_DP_PRODUCT_NO).getAsString());
        } else if (tableName.equals(TABLE_DA_PRODUCT_MODELS)) {
            if (searchTable.equals(TABLE_DA_MODEL)) {
                return new iPartsProductModelsId("", attributes.getField(FIELD_DM_MODEL_NO).getAsString());
            } else {
                return new iPartsProductModelsId(attributes.getField(FIELD_DPM_PRODUCT_NO).getAsString(), "");
            }
        }
        return null;
    }

    /**
     * Lädt die Werte für das übergebene Feld
     *
     * @param dataObject
     * @param fieldName
     */
    private void loadFieldValue(EtkDataObject dataObject, String fieldName) {
        if (dataObject == null) {
            return;
        }

        if (dataObject.existsInDB()) {
            DBDataObjectAttribute attribute = dataObject.getAttributes().getField(fieldName, false);
            if (attribute != null) {
                if (attribute.getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                    attribute.getAsMultiLanguage(dataObject, false);
                } else if (attribute.getType() == DBDataObjectAttribute.TYPE.ARRAY) {
                    attribute.getAsArray(dataObject);
                }
            }
        } else {
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Weist das vorgegeben Attribut aus {@link EtkDataObject} der übergebenen Liste von Attributen zu ({@link DBDataObjectAttributes}).
     * Ist das Zielfeld ein virtuelles Feld, dann muss der Feldname des virtuelle Feldes ebenfalls übergeben werden
     *
     * @param dataObject
     * @param fieldName
     * @param virtFieldName
     * @param attributes
     */
    private void assignAttributeValues(EtkDataObject dataObject, String fieldName, String virtFieldName, DBDataObjectAttributes attributes) {
        if (dataObject != null) {
            String destField = StrUtils.isEmpty(virtFieldName) ? fieldName : virtFieldName;
            DBDataObjectAttribute attribute = dataObject.getAttributes().getField(fieldName, false);
            DBDataObjectAttribute destAttribute = attributes.getField(destField, false);
            if ((attribute != null) && (destAttribute != null)) {
                destAttribute.assign(attribute);
            }
        }
    }

    private void calculateVirtualFieldValues(DBDataObjectAttributes attributes) {
        EtkDisplayFields displayFields = getDisplayResultFields();
        if (displayFields != null) {
            EtkDisplayField modelTypesField = displayFields.getFeldByName(TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_MODEL_TYPES, false);
            if ((modelTypesField != null) && modelTypesField.isVisible()) {
                iPartsDataProduct dataProduct = new iPartsDataProduct(getProject(), (iPartsProductId)getIdForTable(TABLE_DA_PRODUCT, attributes));
                attributes.addField(dataProduct.getModelTypeAttribute(), DBActionOrigin.FROM_DB);
            }
        }
    }

    public class SimpleMasterDataSearchFilterProductModelFactory extends SimpleMasterDataSearchFilterFactory {

        public SimpleMasterDataSearchFilterProductModelFactory(EtkProject project) {
            super(project);
        }

        @Override
        protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> calculateFilteredList() {
            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries;
            if (getAssemblyListFilter().isDBFilterActive()) {
                entries = new DwList<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes>();
                Set<String> tableNames = new TreeSet<String>();
                Collection<EtkFilterTyp> activeFilters = getAssemblyListFilter().getActiveFilter();
                for (EtkFilterTyp filterTyp : activeFilters) {
                    if (filterTyp.getFilterTypus() == EtkFilterTyp.FilterTypus.GRIDFILTER) {
                        String tableName = filterTyp.getName();
                        tableName = tableName.substring(tableName.indexOf('_') + 1);
                        tableNames.add(TableAndFieldName.getTableName(tableName));
                    }
                }

                String language = getProject().getViewerLanguage();
                //filtern
                for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes entry : getEntries()) {
                    boolean addEntry = true;
                    for (String tableName : tableNames) {
                        if (!getAssemblyListFilter().checkFilter(tableName, entry.attributes, language)) {
                            addEntry = false;
                            break;
                        }
                    }

                    if (addEntry) { // Eintrag wurde nicht ausgefiltert
                        entries.add(entry);
                    }
                }
            } else {
                entries = new DwList<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes>(getEntries());
            }
            return entries;
        }

    }
}
