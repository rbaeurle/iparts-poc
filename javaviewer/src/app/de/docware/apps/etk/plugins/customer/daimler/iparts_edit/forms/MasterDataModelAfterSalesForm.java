/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModelsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.DIALOGModelsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsProductModelHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoModelsAggsDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoSAAsModelsDataForm;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Formular für die Anzeige der Stammdaten von Baumustern im After-Sales (Tabelle DA_MODEL).
 */
public class MasterDataModelAfterSalesForm extends SimpleMasterDataSearchFilterGrid {

    private GuiMenuItem codeMasterDataMenuItem;
    private GuiMenuItem linkedModelsMenuItem;
    private GuiMenuItem saasModelsDataMenuItem;
    private GuiMenuItem showProductsMenuItem;
    private GuiMenuItem unifyMenuItem;

    /**
     * Zeigt Baumusterstammdaten an. Wenn ein Baumusterknoten im Navigationsbaum selektiert ist, wird er als
     * Suchkriterium übernommen. Falls eine Baureihe selektiert ist, werden alle dazugehörigen Baumuster angezeigt.
     * Falls keiner selektiert ist, wird eine leere Stammdatenliste angezeigt.
     *
     * @param owner
     */
    public static void showModelAfterSalesMasterData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        iPartsSeriesId seriesId = null;

        // Produkt?
        iPartsProductId productId = new iPartsProductId(getIdFromTreeSelectionForType(activeForm, iPartsProductId.TYPE));
        if (productId.isValidId()) {
            EtkProject project = owner.getConnector().getProject();
            iPartsProduct product = iPartsProduct.getInstance(project, productId);
            seriesId = product.getReferencedSeries();
        }

        if (seriesId == null) {
            // Baureihe?
            seriesId = new iPartsSeriesId(getIdFromTreeSelectionForType(activeForm, iPartsSeriesId.TYPE));
            if (!seriesId.isValidId()) {
                seriesId = null;
            }
        }

        // Baumuster?
        iPartsModelId modelId = new iPartsModelId(getIdFromTreeSelectionForType(activeForm, iPartsModelId.TYPE));
        if (!modelId.isValidId()) {
            modelId = null;
        }
        showModelAfterSalesMasterData(activeForm.getConnector(), activeForm, seriesId, modelId, null);
    }

    /**
     * Formular anzeigen mit Vorgabe Baureihe und Baumuster.
     * Nur Editieren bestehender Record erlaubt, keine Neuanlage oder Löschen.
     *
     * @param dataConnector
     * @param parentForm
     * @param seriesId
     * @param modelId
     * @param onEditChangeRecordEvent Enthält Callbacks für Create, Modify und Delete; oder null
     */
    public static void showModelAfterSalesMasterData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                     iPartsSeriesId seriesId, iPartsModelId modelId,
                                                     OnEditChangeRecordEvent onEditChangeRecordEvent) {
        if (onEditChangeRecordEvent == null) {
            // Falls nicht extern definiert: Hier die Callbacks für Edit, Modify und Delete definieren
            onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
                @Override
                public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
//                     Achtung! Wenn diese Funktion wieder verwendet wird, muss ein geändertes bzw. gelöschtes iPartsModel aus dem Cache gelöscht werden!

//                    iPartsModelId modelId = new iPartsModelId(id.getValue(1));
//                    iPartsDataModel dataModel = new iPartsDataModel(dataConnector.getProject(), modelId);
//                    if (dataModel.loadFromDB(modelId)) {
//                        String msg = "!!Das Baumuster ist bereits vorhanden und kann nicht neu angelegt werden!";
//                        MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
//                        return true;
//                    }
//                    dataModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
//                    dataModel.assignAttributesValues(dataConnector.getProject(), attributes, true, DBActionOrigin.FROM_EDIT);
//                    dataConnector.getProject().getDbLayer().startTransaction();
//                    try {
//                        dataModel.saveToDB();
//                        dataConnector.getProject().getDbLayer().commit();
//                        return true;
//                    } catch (Exception e) {
//                        dataConnector.getProject().getDbLayer().rollback();
//                        Logger.getLogger().handleRuntimeException(e);
//                    }
                    return false;
                }

                /**
                 * Mit den Angaben aus tableName, Id und Attributen einen bestehenden Record modifizieren
                 *
                 * @param dataConnector
                 * @param tableName
                 * @param id
                 * @param attributes
                 * @return true: Record wurde gespeichert
                 */
                @Override
                public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                    EtkProject project = dataConnector.getProject();
                    iPartsModelId modelId = new iPartsModelId(id.getValue(1));
                    iPartsDataModel dataModel = new iPartsDataModel(project, modelId);
                    dataModel.loadFromDB(modelId);
                    dataModel.assignAttributesValues(project, attributes, true, DBActionOrigin.FROM_EDIT);

                    // Flag setzen dass dieser Record künftig von Importern nicht mehr überschrieben werden darf
                    dataModel.setFieldValueAsBoolean(FIELD_DM_MANUAL_CHANGE, true, DBActionOrigin.FROM_EDIT);


                    iPartsRevisionChangeSet changeSet = iPartsRevisionChangeSet.createTempChangeSet(project, iPartsChangeSetSource.MODEL_AS);
                    project.getDbLayer().startTransaction();
                    try {
                        changeSet.addDataObject(dataModel, false, false, false);

                        // Wenn der Autor das Baumuster editiert haben die manuell angepassten Daten Vorrang vor den Daten
                        // der Migration. Daher muss hier ein Sync der Stammdaten aus DA_PRODUCT_MODEL laufen. Durch die
                        // Synchronisation werden die DA_PRODUCT_MODEL Felder, die auch in DA_MODEL vorkommen, geleert.
                        iPartsDataProductModelsList productModelList = iPartsDataProductModelsList.loadDataProductModelsList(project, dataModel.getAsId());
                        iPartsProductModelHelper.syncProductModelsWithModel(dataModel, productModelList);
                        changeSet.addDataObjectList(productModelList, false, false);

                        if (changeSet.commit()) {
                            dataModel.saveToDB();
                            productModelList.saveToDB(project);
                            project.getDbLayer().commit();

                            // Geändertes Baummuster sowie alle betroffenen Produkte aus dem Cache löschen falls dort vorhanden
                            DIALOGModelsHelper.removeModelAndProductsFromCache(modelId, project, iPartsDataChangedEventByEdit.Action.MODIFIED);

                            // Daten haben sich geändert -> Filterung muss z.B. neu durchgeführt werden
                            project.fireProjectEvent(new DataChangedEvent());
                            return true;
                        } else {
                            project.getDbLayer().rollback();
                            return false;
                        }
                    } catch (Exception e) {
                        project.getDbLayer().rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }
                    return false;
                }

                @Override
                public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                    return false;
                }

                @Override
                public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
//                     Achtung! Wenn diese Funktion wieder verwendet wird, muss ein geändertes bzw. gelöschtes iPartsModel aus dem Cache gelöscht werden!

                    return false;
                }
            };
        }

        MasterDataModelAfterSalesForm dlg = new MasterDataModelAfterSalesForm(dataConnector, parentForm, TABLE_DA_MODEL, onEditChangeRecordEvent);
        // Suchfelder definieren
        EtkDisplayFields searchFields = getSearchFields(dataConnector);
        // Anzeigefelder definieren
        EtkDisplayFields displayFields = getDisplayFields(dataConnector);
        // Benötigte Ergebnisfelder (abgesehen von den Primärschlüsselfeldern) definieren
        EtkDisplayFields requiredResultFields = getRequiredResultFields(dataConnector);
        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = getEditFields(dataConnector);

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DM_MODEL_NO, false);
        dlg.setSortFields(sortFields);

        dlg.setDisplayResultFields(displayFields);
        dlg.setRequiredResultFields(requiredResultFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        dlg.setEditAllowed(editMasterDataAllowed);
        dlg.setNewAllowed(false);
        dlg.setModifyAllowed(editMasterDataAllowed);
        dlg.setDeleteAllowed(false);
        dlg.setTitlePrefix("!!Baumuster (After-Sales)");
        dlg.setWindowName("ModelAfterSalesMasterData");
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);

        DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
        if ((seriesId != null) && seriesId.isValidId()) {
            // Suchwerte setzen und Suche starten
            searchAttributes.addField(FIELD_DM_SERIES_NO, seriesId.getSeriesNumber(), DBActionOrigin.FROM_DB);
        }
        if ((modelId != null) && modelId.isValidId()) {
            // Suchwerte setzen und Suche starten
            searchAttributes.addField(FIELD_DM_MODEL_NO, modelId.getModelNumber(), DBActionOrigin.FROM_DB);
        }
        if (!searchAttributes.isEmpty()) {
            dlg.setSearchValues(searchAttributes);
        }
        dlg.showModal();
    }

    public static void showModelAfterSalesForProductData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                         iPartsProductId productId) {
        MasterDataModelAfterSalesForm dlg = new MasterDataModelAfterSalesForm(dataConnector, parentForm, TABLE_DA_MODEL, null);

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = getDisplayFields(dataConnector);

        // Benötigte Ergebnisfelder (abgesehen von den Primärschlüsselfeldern) definieren
        EtkDisplayFields requiredResultFields = getRequiredResultFields(dataConnector);

        dlg.setDisplayResultFields(displayFields);
        dlg.setRequiredResultFields(requiredResultFields);
        dlg.setSearchFields(null);
        dlg.setEditFields(null);
        dlg.setTitlePrefix("!!Produkt-Baumuster (After-Sales)");
        dlg.setWindowName("ProductModelsAfterSalesMasterData");
        dlg.setTitle(TranslationHandler.translate("!!Produkt-Baumuster (After-Sales) Liste für Produkt \"%1\"", productId.getProductNumber()));
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
        dlg.setEditAllowed(false);
        dlg.showSearchFields(false);
        dlg.doResizeWindow(SCREEN_SIZES.SCALE_FROM_PARENT);

        List<DBDataObjectAttributes> attributesList = new ArrayList<>();
        iPartsProduct product = iPartsProduct.getInstance(dataConnector.getProject(), productId);
        if (product != null) {
            Set<String> models = product.getModelNumbers(dataConnector.getProject());
            for (String model : models) {
                iPartsDataModel dataModel = new iPartsDataModel(dataConnector.getProject(), new iPartsModelId(model));
                if (dataModel.existsInDB()) {
                    attributesList.add(dataModel.getAttributes());
                }
            }
        }
        dlg.fillByAttributesList(attributesList);
        dlg.showModal();
    }

    private static EtkEditFields getEditFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_MODEL_NO, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_SERIES_NO, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_SALES_TITLE, true));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_DEVELOPMENT_TITLE, true));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_CODE, false));
        }
        EtkDatabaseTable tableDef = dataConnector.getConfig().getDBDescription().findTable(TABLE_DA_MODEL);
        List<String> pkFields = tableDef.getPrimaryKeyFields();
        for (EtkEditField eField : editFields.getFields()) {
            if (pkFields.contains(eField.getKey().getFieldName())) {
                eField.setMussFeld(true);
                eField.setEditierbar(false);  // ist PK-Value => nicht editierbar
            }
        }
        return editFields;
    }

    private static EtkDisplayFields getRequiredResultFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Benötigte Ergebnisfelder (abgesehen von den Primärschlüsselfeldern) definieren
        EtkDisplayFields requiredResultFields = new EtkDisplayFields();
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_CODE, false, false));
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_SERIES_NO, false, false));
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_PRODUCT_GRP, false, false));
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_DATA, false, false));
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_SOURCE, false, false));
        return requiredResultFields;
    }

    private static EtkDisplayFields getDisplayFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkProject project = dataConnector.getProject();
        displayFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkDisplayField displayField = addDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_NO, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField = addDisplayField(TABLE_DA_MODEL, FIELD_DM_SERIES_NO, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_MODEL, FIELD_DM_SALES_TITLE, true, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL, FIELD_DM_DEVELOPMENT_TITLE, true, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL, FIELD_DM_CODE, false, false, null, project, displayFields);
        }
        return displayFields;
    }

    private static EtkDisplayFields getSearchFields(AbstractJavaViewerFormIConnector dataConnector) {
        // Suchfelder definieren
        EtkDisplayFields searchFields = new EtkDisplayFields();
        searchFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS,
                          dataConnector.getConfig().getCurrentDatabaseLanguage());
        if (searchFields.size() == 0) {
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_MODEL_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_CODE, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_MODEL, FIELD_DM_SERIES_NO, false, false));
        }
        return searchFields;
    }


    private MasterDataModelAfterSalesForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        contextMenu.addChild(new GuiSeparator());

        unifyMenuItem = toolbarHelper.createMenuEntry("unify", "!!Vereinheitlichen...", null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                endSearch();
                doUnifyNodelVisibleValues();
            }
        }, getUITranslationHandler());
        unifyMenuItem.setVisible(false);  // todo Freischalten wenn DAIMLER-7095
        contextMenu.addChild(unifyMenuItem);

        // Popup-Menüeintrag für Code-Stammdaten
        codeMasterDataMenuItem = iPartsCodeMatrixDialog.createMenuItem(new iPartsCodeMatrixDialog.CodeMasterDataCallback() {
            @Override
            public iPartsCodeMatrixDialog.CodeMasterDataQuery getCodeMasterDataQuery() {
                DBDataObjectAttributes attributes = getSelectedAttributes();
                if (attributes != null) {
                    return new iPartsCodeMatrixDialog.CodeMasterDataQuery(attributes.getField(FIELD_DM_CODE).getAsString(),
                                                                          new iPartsSeriesId(attributes.getField(FIELD_DM_SERIES_NO).getAsString()),
                                                                          attributes.getField(FIELD_DM_PRODUCT_GRP).getAsString(),
                                                                          attributes.getField(FIELD_DM_DATA).getAsString());
                } else {
                    return null;
                }
            }
        }, this);

        contextMenu.addChild(codeMasterDataMenuItem);

        // Popup-Menüeintrag für verknüpfte Baumuster
        linkedModelsMenuItem = iPartsRelatedInfoModelsAggsDataForm.createMenuItem(new iPartsRelatedInfoModelsAggsDataForm.ModelAggsDataCallback() {
            @Override
            public iPartsRelatedInfoModelsAggsDataForm.ModelsAggsDataQuery getModelsAggsDataQuery() {
                DBDataObjectAttributes attributes = getSelectedAttributes();
                if (attributes != null) {
                    return new iPartsRelatedInfoModelsAggsDataForm.ModelsAggsDataQuery(new iPartsModelId(attributes.getField(FIELD_DM_MODEL_NO).getAsString()));
                } else {
                    return null;
                }
            }
        }, this);

        contextMenu.addChild(linkedModelsMenuItem);

        // Popup-Menüeintrag für SAA-Gültigkeiten
        saasModelsDataMenuItem = iPartsRelatedInfoSAAsModelsDataForm.createMenuItem(new iPartsRelatedInfoSAAsModelsDataForm.SAAsModelsDataCallback() {

            @Override
            public iPartsRelatedInfoSAAsModelsDataForm.SAAsModelsDataQuery getSAAsModelsDataQuery() {
                DBDataObjectAttributes attributes = getSelectedAttributes();
                if (attributes != null) {
                    return new iPartsRelatedInfoSAAsModelsDataForm.SAAsModelsDataQuery(new iPartsModelId(attributes.getField(iPartsConst.FIELD_DM_MODEL_NO).getAsString()));
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

    protected void doUnifyNodelVisibleValues() {
        DBDataObjectAttributes selectedAttributes = getSelectedAttributes();
        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if (attributeList != null) {
            List<iPartsDataModel> modelList = new DwList<>(attributeList.size());
            for (DBDataObjectAttributes attrib : attributeList) {
                iPartsModelId id = new iPartsModelId(attrib.getFieldValue(iPartsConst.FIELD_DM_MODEL_NO));
                iPartsDataModel dataModel = new iPartsDataModel(getProject(), id);
                dataModel.assignAttributes(getProject(), attrib, true, DBActionOrigin.FROM_DB);
                modelList.add(dataModel);
            }

            EtkEditFields editFields = new EtkEditFields();
            boolean singleSelection = selectedAttributes.size() == 1;
            addEditField(searchTable, FIELD_DM_MODEL_VISIBLE, false, null, getProject(), editFields);
            // Anzeige des Unify-Dialogs
            DBDataObjectAttributes attributes =
                    EditUserMultiChangeControls.showEditUserMultiChangeControlsForModels(getConnector(), editFields,
                                                                                         modelList);
            if (attributes != null) {
                for (EtkEditField editField : editFields.getFields()) {
                    String fieldName = editField.getKey().getFieldName();
                    String fieldValue = attributes.getFieldValue(fieldName);
                    for (iPartsDataModel dataModel : modelList) {
                        dataModel.setFieldValue(fieldName, fieldValue, DBActionOrigin.FROM_EDIT);
                    }
                }
                // todo DAIMLER-7095: im ChangeSet abspeichern
            }
        }
    }

    private void doShowProducts(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            IdWithType id = buildIdFromAttributes(attributes);
            iPartsModelId modelId = new iPartsModelId(id.toStringArrayWithoutType()[0]);
            MasterDataProductForm.showProductMasterDataForModel(getConnector(), this, modelId);
        }
    }

    @Override
    public void setRequiredResultFields(EtkDisplayFields requiredResultFields) {
        super.setRequiredResultFields(requiredResultFields);
        codeMasterDataMenuItem.setVisible(requiredResultFields.contains(TABLE_DA_MODEL, FIELD_DM_CODE, false));
    }


    @Override
    protected void enableButtons() {
        super.enableButtons();

        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelection = selectionRowCount == 1;
        linkedModelsMenuItem.setEnabled(singleSelection);
        saasModelsDataMenuItem.setEnabled(singleSelection);
        showProductsMenuItem.setEnabled(singleSelection);

        boolean enabled = false;
        if (singleSelection) {
            DBDataObjectAttributes selectedAttributes = getSelection();
            if (selectedAttributes != null) {
                iPartsModelId modelId = new iPartsModelId(selectedAttributes.getField(FIELD_DM_MODEL_NO).getAsString());
                iPartsRelatedInfoModelsAggsDataForm.updateMenuItem(linkedModelsMenuItem, modelId);
                if (codeMasterDataMenuItem != null) {
                    String codeString = selectedAttributes.getFieldValue(FIELD_DM_CODE);
                    enabled = !DaimlerCodes.isEmptyCodeString(codeString);
                }
            }
        }
        if (codeMasterDataMenuItem != null) {
            codeMasterDataMenuItem.setEnabled(enabled);
        }
    }

    @Override
    protected EditUserControls createUserControlForEditOrView(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                                              String searchTable, IdWithType id, DBDataObjectAttributes attributes, EtkEditFields editFields) {
        return new EditUserControlForAfterSalesModel(connector, parentForm, searchTable, id, attributes, editFields);
    }


    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        String source = attributes.getFieldValue(FIELD_DM_SOURCE);
        String modelNo = attributes.getFieldValue(FIELD_DM_MODEL_NO);
        return iPartsFilterHelper.isASModelVisibleForUserInSession(modelNo, source,
                                                                   isCarAndVanInSession(), isTruckAndBusInSession(),
                                                                   getProject());
    }
}
