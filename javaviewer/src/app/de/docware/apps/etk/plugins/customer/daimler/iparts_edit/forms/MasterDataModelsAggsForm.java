/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnCreateAttributesEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsModelsAggsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoCodeMasterDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoSAAsModelsDataForm;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.CaseMode;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.InnerJoin;

import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Formular für die Anzeige der Stammdaten von Fahrzeug-Baumuster zu Aggregate-Baumuster (Tabelle DA_MODELS_AGGS).
 */
public class MasterDataModelsAggsForm extends SimpleMasterDataSearchFilterGrid {

    private static final String CONTEXT_VEHICLE = "VEHICLE";
    private static final String CONTEXT_AGGREGATE = "AGGREGATE";
    private static final String CONTEXT_AGGREGATE_PRODUCT = "AGGREGATE_PRODUCT";
    private static final String CONTEXT_DELIMITER = "@";

    private GuiMenuItem codeMasterDataVehicleMenuItem;
    private GuiMenuItem saasModelsDataVehicleMenuItem;
    private GuiMenuItem codeMasterDataAggregateMenuItem;
    private GuiMenuItem saasModelsDataAggregateMenuItem;

    /**
     * Zeigt Fahrzeug-Baumuster zu Aggregate-Baumuster Stammdaten an.
     *
     * @param owner
     */
    public static void showModelsAggsMasterData(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        showModelsAggsMasterData(activeForm.getConnector(), activeForm, null);
    }

    /**
     * Anzeige der Baureihen Tabelle (DA_Series)
     *
     * @param dataConnector
     * @param parentForm
     * @param onEditChangeRecordEvent
     */
    public static void showModelsAggsMasterData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                OnEditChangeRecordEvent onEditChangeRecordEvent) {
        if (onEditChangeRecordEvent == null) {
            // Falls nicht extern definiert: Hier die Callbacks für Edit, Modify und Delete definieren
            onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
                private boolean onEditCreateOrModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                                IdWithType id, DBDataObjectAttributes attributes, boolean createRecord) {
                    EtkProject project = dataConnector.getProject();

                    // toUpperCase() solange es reine Edit-Felder sind
                    iPartsModelsAggsId modelsAggsId = new iPartsModelsAggsId(id.getValue(1).toUpperCase(), id.getValue(2).toUpperCase());
                    for (DBDataObjectAttribute attrib : attributes.getFields()) {
                        attrib.setValueAsString(attrib.getAsString().toUpperCase(), DBActionOrigin.FROM_DB);
                    }

                    iPartsDataModelsAggs dataModelsAggs = new iPartsDataModelsAggs(project, modelsAggsId);
                    if (dataModelsAggs.loadFromDB(modelsAggsId) && createRecord) {
                        String msg = "!!Die Zuordnung Fahrzeugbaumuster / Aggregatebaumuster ist bereits vorhanden und kann nicht neu angelegt werden!";
                        MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                        return true;
                    }
                    if (createRecord) {
                        dataModelsAggs.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    }
                    // Quelle nach Eigenschaft des Benutzers setzen
                    // Sie bleibt leer falls Benutzer beide Eigenschaften hat
                    String source = "";
                    if (iPartsRight.checkCarAndVanInSession() && !iPartsRight.checkTruckAndBusInSession()) {
                        source = iPartsImportDataOrigin.IPARTS_MB.getOrigin();
                    } else if (!iPartsRight.checkCarAndVanInSession() && iPartsRight.checkTruckAndBusInSession()) {
                        source = iPartsImportDataOrigin.IPARTS_TRUCK.getOrigin();
                    }
                    dataModelsAggs.setFieldValue(FIELD_DMA_SOURCE, source, DBActionOrigin.FROM_DB);
                    project.getDbLayer().startTransaction();
                    try {
                        if (iPartsRevisionChangeSet.saveDataObjectWithChangeSet(project, dataModelsAggs, iPartsChangeSetSource.MODEL_AGGS)) {
                            dataModelsAggs.saveToDB();
                            project.getDbLayer().commit();

                            List<iPartsModelId> modelIds = new DwList<iPartsModelId>(1);
                            modelIds.add(new iPartsModelId(modelsAggsId.getModelNumber()));
                            fireEventsForModifiedModelIds(project, modelIds);
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
                public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       IdWithType id, DBDataObjectAttributes attributes) {
                    return onEditCreateOrModifyRecordEvent(dataConnector, tableName, id, attributes, true);
                }

                @Override
                public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       IdWithType id, DBDataObjectAttributes attributes) {
                    return onEditCreateOrModifyRecordEvent(dataConnector, tableName, id, attributes, false);
                }

                @Override
                public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                    if ((attributeList != null) && !attributeList.isEmpty()) {
                        String msg = "!!Wollen Sie die selektierte Zuordnung Fahrzeugbaumuster / Aggregatebaumuster wirklich löschen?";
                        if (attributeList.size() > 1) {
                            msg = "!!Wollen Sie die selektierten Zuordnungen Fahrzeugbaumuster / Aggregatebaumuster wirklich löschen?";
                        }
                        if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                               MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributesList) {
                    if ((attributesList != null) && !attributesList.isEmpty()) {
                        EtkProject project = dataConnector.getProject();
                        EtkDbObjectsLayer dbLayer = project.getDbLayer();
                        dbLayer.startTransaction();
                        dbLayer.startBatchStatement();
                        try {
                            List<iPartsModelId> modelIds = new ArrayList<iPartsModelId>(attributesList.size());
                            for (DBDataObjectAttributes attributes : attributesList) {
                                iPartsModelId modelId = new iPartsModelId(attributes.getField(FIELD_DMA_MODEL_NO).getAsString());
                                modelIds.add(modelId);
                                iPartsModelsAggsId modelsAggsId = new iPartsModelsAggsId(modelId.getModelNumber(), attributes.getField(FIELD_DMA_AGGREGATE_NO).getAsString());
                                iPartsDataModelsAggs dataModelsAggs = new iPartsDataModelsAggs(project, modelsAggsId);
                                if (iPartsRevisionChangeSet.deleteDataObjectWithChangeSet(project, dataModelsAggs, iPartsChangeSetSource.MODEL_AGGS)) {
                                    dataModelsAggs.deleteFromDB(true);
                                } else {
                                    dbLayer.cancelBatchStatement();
                                    dbLayer.rollback();
                                    return false;
                                }
                            }
                            dbLayer.endBatchStatement();
                            dbLayer.commit();
                            fireEventsForModifiedModelIds(project, modelIds);


                            return true;
                        } catch (Exception e) {
                            dbLayer.cancelBatchStatement();
                            dbLayer.rollback();
                            Logger.getLogger().handleRuntimeException(e);
                        }
                    }
                    return false;
                }

                private void fireEventsForModifiedModelIds(EtkProject project, List<iPartsModelId> modelIds) {
                    // Durch die gelöschten Zuordnungen von Aggregatebaumustern zu Fahrzeugbaumustern Events für die veränderten
                    // Fahrzeugbaumuster verteilen
                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<iPartsModelId>(iPartsDataChangedEventByEdit.DataType.MODEL,
                                                                                                                           iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                           modelIds, false));

                    // Durch die gelöschten Zuordnungen von Aggregatebaumustern zu Fahrzeugbaumustern Events für die veränderten
                    // Produkte verteilen, die diese Fahrzeugbaumuster verwenden
                    Set<iPartsProductId> modifiedProducts = new HashSet<>();
                    for (iPartsModelId modelId : modelIds) {
                        // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster ebenfalls berücksichtigen.
                        List<iPartsProduct> productsForModel = iPartsProductHelper.getProductsForModel(project, modelId,
                                                                                                       null,
                                                                                                       iPartsProductHelper.PRODUCTS_FOR_SERIES_MODE.FILTER_ALL,
                                                                                                       false);
                        for (iPartsProduct product : productsForModel) {
                            modifiedProducts.add(product.getAsId());
                        }
                    }
                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                                              iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                              modifiedProducts, false));
                }
            };
        }

        MasterDataModelsAggsForm dlg = new MasterDataModelsAggsForm(dataConnector, parentForm, TABLE_DA_MODELS_AGGS, onEditChangeRecordEvent);
        EtkProject project = dataConnector.getProject();

        // Suchfelder definieren
        EtkDisplayFields searchFields = new EtkDisplayFields();
        // Fahrzeug
        addSearchField(TABLE_DA_MODELS_AGGS, FIELD_DMA_MODEL_NO, null, "!!Fahrzeugbaumusternummer", project, searchFields);
        addSearchField(TABLE_DA_MODEL, FIELD_DM_SERIES_NO, CONTEXT_VEHICLE, "!!Fahrzeugbaureihennummer", project, searchFields);
        addSearchField(TABLE_DA_MODEL, FIELD_DM_CODE, CONTEXT_VEHICLE, "!!Fahrzeug baumusterbildende Code", project, searchFields);

        // Aggregat
        addSearchField(TABLE_DA_MODELS_AGGS, FIELD_DMA_AGGREGATE_NO, null, "!!Aggregatebaumusternummer", project, searchFields);
        addSearchField(TABLE_DA_MODEL, FIELD_DM_SERIES_NO, CONTEXT_AGGREGATE, "!!Aggregatebaureihennummer", project, searchFields);
        addSearchField(TABLE_DA_MODEL, FIELD_DM_CODE, CONTEXT_AGGREGATE, "!!Aggregat baumusterbildende Code", project, searchFields);

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        // Fahrzeug
        EtkDisplayField displayField = addDisplayField(TABLE_DA_MODELS_AGGS, FIELD_DMA_MODEL_NO, null, false, false, "!!Fahrzeugbaumusternummer", project, displayFields);
        displayField.setColumnFilterEnabled(true);
        displayField = addDisplayField(TABLE_DA_MODEL, FIELD_DM_SERIES_NO, CONTEXT_VEHICLE, false, false, "!!Fahrzeugbaureihennummer", project, displayFields);
        displayField.setColumnFilterEnabled(true);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_CODE, CONTEXT_VEHICLE, false, false, "!!Fahrzeug baumusterbildende Code", project, displayFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_SALES_TITLE, CONTEXT_VEHICLE, true, false, "!!Fahrzeug Verkaufsbezeichnung", project, displayFields);

        // Aggregat
        displayField = addDisplayField(TABLE_DA_MODELS_AGGS, FIELD_DMA_AGGREGATE_NO, null, false, false, "!!Aggregatebaumusternummer", project, displayFields);
        displayField.setColumnFilterEnabled(true);
        displayField = addDisplayField(TABLE_DA_MODEL, FIELD_DP_AGGREGATE_TYPE, CONTEXT_AGGREGATE_PRODUCT, false, false, "!!Aggregatebaumusterart", project, displayFields);
        displayField.setColumnFilterEnabled(true);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_SERIES_NO, CONTEXT_AGGREGATE, false, false, "!!Aggregatebaureihennummer", project, displayFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_CODE, CONTEXT_AGGREGATE, false, false, "!!Aggregat baumusterbildende Code", project, displayFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_SALES_TITLE, CONTEXT_AGGREGATE, true, false, "!!Aggregat Verkaufsbezeichnung", project, displayFields);

        //Quelle
        displayField = addDisplayField(TABLE_DA_MODELS_AGGS, FIELD_DMA_SOURCE, null, false, false, "!!Quelle", project, displayFields);
        displayField.setVisible(false);

        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        addEditField(TABLE_DA_MODELS_AGGS, FIELD_DMA_MODEL_NO, false, "!!Fahrzeugbaumusternummer", project, editFields);
        addEditField(TABLE_DA_MODELS_AGGS, FIELD_DMA_AGGREGATE_NO, false, "!!Aggregatebaumusternummer", project, editFields);

        // Benötigte Ergebnisfelder (abgesehen von den Primärschlüsselfeldern) definieren
        EtkDisplayFields requiredResultFields = new EtkDisplayFields();
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_CODE, CONTEXT_VEHICLE, false, false, null, project, requiredResultFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_SERIES_NO, CONTEXT_VEHICLE, false, false, null, project, requiredResultFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_PRODUCT_GRP, CONTEXT_VEHICLE, false, false, null, project, requiredResultFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_DATA, CONTEXT_VEHICLE, false, false, null, project, requiredResultFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_CODE, CONTEXT_AGGREGATE, false, false, null, project, requiredResultFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_SERIES_NO, CONTEXT_AGGREGATE, false, false, null, project, requiredResultFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_PRODUCT_GRP, CONTEXT_AGGREGATE, false, false, null, project, requiredResultFields);
        addDisplayField(TABLE_DA_MODEL, FIELD_DM_DATA, CONTEXT_AGGREGATE, false, false, null, project, requiredResultFields);

        EtkDatabaseTable tableDef = dataConnector.getConfig().getDBDescription().findTable(TABLE_DA_MODELS_AGGS);
        List<String> pkFields = tableDef.getPrimaryKeyFields();
        for (EtkEditField eField : editFields.getFields()) {
            if (pkFields.contains(eField.getKey().getFieldName())) {
                eField.setMussFeld(true);
                eField.setEditierbar(false);  // ist PK-Value => nicht editierbar
            }
        }

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DMA_MODEL_NO, false);
        sortFields.put(FIELD_DMA_AGGREGATE_NO, false);
        dlg.setSortFields(sortFields);

        dlg.setMaxSearchControlsPerRow(3);
        dlg.setDisplayResultFields(displayFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        dlg.setRequiredResultFields(requiredResultFields);
        // falls der Benutzer weder die Eigenschaft Truck/Bus noch Pkw/Van hat darf er nicht editieren
        boolean editVehiclePartsDataAllowed = iPartsRight.ASSIGN_VEHICLE_AGGS_DATA.checkRightInSession() &&
                                              (dlg.isCarAndVanInSession() || dlg.isTruckAndBusInSession());
        dlg.setEditAllowed(editVehiclePartsDataAllowed);
        dlg.setNewAllowed(editVehiclePartsDataAllowed);
        dlg.setModifyAllowed(false);
        dlg.setDeleteAllowed(editVehiclePartsDataAllowed);
        dlg.setTitlePrefix("!!Zuordnung Fahrzeugbaumuster / Aggregatebaumuster");
        dlg.setWindowName("ModelsAggsMasterData");
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);

        dlg.showModal();
    }

    private static String createVirtualFieldName(String virtualFieldContext, String fieldName) {
        return VirtualFieldsUtils.addVirtualFieldMask(virtualFieldContext + CONTEXT_DELIMITER + fieldName);
    }

    private static EtkDisplayField addSearchField(String tableName, String fieldName, String virtualFieldContext, String labelText,
                                                  EtkProject project, EtkDisplayFields searchFields) {
        EtkDisplayField searchField = createSearchField(project, tableName, fieldName, false, false);
        return addField(tableName, fieldName, virtualFieldContext, labelText, searchFields, searchField);
    }

    private static EtkDisplayField addDisplayField(String tableName, String fieldName, String virtualFieldContext, boolean multiLanguage, boolean isArray,
                                                   String labelText, EtkProject project, EtkDisplayFields displayFields) {
        EtkDisplayField displayField = createDisplayField(project, tableName, fieldName, multiLanguage, isArray);
        return addField(tableName, fieldName, virtualFieldContext, labelText, displayFields, displayField);
    }

    private static EtkDisplayField addField(String tableName, String fieldName, String virtualFieldContext, String labelText,
                                            EtkDisplayFields fields, EtkDisplayField field) {
        // Jetzt erst den virtuellen Feldnamen bei vorhandenem virtualFieldContext setzen, weil ansonsten die Felddefinition
        // nicht aus der DBDatabaseDescription geladen werden kann
        if (virtualFieldContext != null) {
            fieldName = createVirtualFieldName(virtualFieldContext, fieldName);
            field.getKey().setName(TableAndFieldName.make(tableName, fieldName));
        }

        if (labelText != null) {
            field.setDefaultText(false);
            field.setText(new EtkMultiSprache(labelText, new String[]{ TranslationHandler.getUiLanguage() }));
        }
        fields.addFeld(field);
        return field;
    }

    private MasterDataModelsAggsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                     String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        setColumnFilterFactory(new SimpleMasterDataSearchFilterModelsAggsFactory(getProject()));
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);

        // Fahrzeugbaumuster aus der aktuellen Selektion vorbelegen (falls vorhanden)
        setOnCreateEvent(new OnCreateAttributesEvent() {
            @Override
            public DBDataObjectAttributes onCreateAttributesEvent() {
                DBDataObjectAttributes selectedAttributes = getSelection();
                if (selectedAttributes != null) {
                    DBDataObjectAttributes initialAttributes = new DBDataObjectAttributes();
                    initialAttributes.addField(FIELD_DMA_MODEL_NO, selectedAttributes.getFieldValue(FIELD_DMA_MODEL_NO), DBActionOrigin.FROM_DB);
                    return initialAttributes;
                }

                return null;
            }
        });
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        // Popup-Menüeintrag für Code-Stammdaten vom Fahrzeug
        codeMasterDataVehicleMenuItem = createCodeMasterDataMenuItem(CONTEXT_VEHICLE);
        codeMasterDataVehicleMenuItem.setText("!!Code-Stammdaten vom Fahrzeugbaumuster");
        contextMenu.addChild(codeMasterDataVehicleMenuItem);

        // Popup-Menüeintrag für SAA-Gültigkeiten vom Fahrzeug
        saasModelsDataVehicleMenuItem = createSAAsModelsDataMenuItem(CONTEXT_VEHICLE);
        saasModelsDataVehicleMenuItem.setText("!!SAA/BK-Gültigkeiten zu Fahrzeugbaumuster");
        contextMenu.addChild(saasModelsDataVehicleMenuItem);

        // Popup-Menüeintrag für Code-Stammdaten vom Aggregat
        codeMasterDataAggregateMenuItem = createCodeMasterDataMenuItem(CONTEXT_AGGREGATE);
        codeMasterDataAggregateMenuItem.setText("!!Code-Stammdaten vom Aggregatebaumuster");
        contextMenu.addChild(codeMasterDataAggregateMenuItem);

        // Popup-Menüeintrag für SAA-Gültigkeiten vom Aggregat
        saasModelsDataAggregateMenuItem = createSAAsModelsDataMenuItem(CONTEXT_AGGREGATE);
        saasModelsDataAggregateMenuItem.setText("!!SAA/BK-Gültigkeiten zu Aggregatebaumuster");
        contextMenu.addChild(saasModelsDataAggregateMenuItem);
    }

    private GuiMenuItem createCodeMasterDataMenuItem(final String virtualFieldContext) {
        GuiMenuItem menuItem = iPartsCodeMatrixDialog.createMenuItem(new iPartsCodeMatrixDialog.CodeMasterDataCallback() {
            @Override
            public iPartsCodeMatrixDialog.CodeMasterDataQuery getCodeMasterDataQuery() {
                DBDataObjectAttributes attributes = getSelectedAttributes();
                if (attributes != null) {
                    return new iPartsCodeMatrixDialog.CodeMasterDataQuery(attributes.getField(createVirtualFieldName(virtualFieldContext, FIELD_DM_CODE)).getAsString(),
                                                                          new iPartsSeriesId(attributes.getField(createVirtualFieldName(virtualFieldContext, FIELD_DM_SERIES_NO)).getAsString()),
                                                                          attributes.getField(createVirtualFieldName(virtualFieldContext, FIELD_DM_PRODUCT_GRP)).getAsString(),
                                                                          attributes.getField(createVirtualFieldName(virtualFieldContext, FIELD_DM_DATA)).getAsString());
                } else {
                    return null;
                }
            }
        }, this);
        menuItem.setName(iPartsRelatedInfoCodeMasterDataForm.IPARTS_MENU_ITEM_SHOW_CODE_MASTER_DATA + virtualFieldContext);
        menuItem.setUserObject(iPartsRelatedInfoCodeMasterDataForm.IPARTS_MENU_ITEM_SHOW_CODE_MASTER_DATA + virtualFieldContext);
        return menuItem;
    }

    private GuiMenuItem createSAAsModelsDataMenuItem(final String virtualFieldContext) {
        GuiMenuItem menuItem = iPartsRelatedInfoSAAsModelsDataForm.createMenuItem(new iPartsRelatedInfoSAAsModelsDataForm.SAAsModelsDataCallback() {
            @Override
            public iPartsRelatedInfoSAAsModelsDataForm.SAAsModelsDataQuery getSAAsModelsDataQuery() {
                DBDataObjectAttributes attributes = getSelectedAttributes();
                if (attributes != null) {
                    String modelNoFieldName = virtualFieldContext.equals(CONTEXT_VEHICLE) ? FIELD_DMA_MODEL_NO : FIELD_DMA_AGGREGATE_NO;
                    return new iPartsRelatedInfoSAAsModelsDataForm.SAAsModelsDataQuery(new iPartsModelId(attributes.getField(modelNoFieldName).getAsString()));
                } else {
                    return null;
                }
            }
        }, this);
        menuItem.setName(iPartsRelatedInfoSAAsModelsDataForm.IPARTS_MENU_ITEM_SHOW_SAAS_MODELS_DATA + virtualFieldContext);
        menuItem.setUserObject(iPartsRelatedInfoSAAsModelsDataForm.IPARTS_MENU_ITEM_SHOW_SAAS_MODELS_DATA + virtualFieldContext);
        return menuItem;
    }

    @Override
    public void setRequiredResultFields(EtkDisplayFields requiredResultFields) {
        super.setRequiredResultFields(requiredResultFields);
        codeMasterDataVehicleMenuItem.setVisible(requiredResultFields.contains(TABLE_DA_MODEL, createVirtualFieldName(CONTEXT_VEHICLE, FIELD_DM_CODE), false));
        codeMasterDataAggregateMenuItem.setVisible(requiredResultFields.contains(TABLE_DA_MODEL, createVirtualFieldName(CONTEXT_AGGREGATE, FIELD_DM_CODE), false));
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();

        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelection = selectionRowCount == 1;
        saasModelsDataVehicleMenuItem.setEnabled(singleSelection);
        saasModelsDataAggregateMenuItem.setEnabled(singleSelection);

        // Code-Stammdaten vom Fahrzeug und Aggregat enablen
        boolean codeMasterDataVehicleMenuItemEnabled = false;
        boolean codeMasterDataAggregateMenuItemEnabled = false;
        if (singleSelection) {
            DBDataObjectAttributes selectedAttributes = getSelection();
            if (selectedAttributes != null) {
                String codeStringVehicle = getVirtualFieldValue(selectedAttributes, createVirtualFieldName(CONTEXT_VEHICLE, FIELD_DM_CODE));
                String codeStringAggregate = getVirtualFieldValue(selectedAttributes, createVirtualFieldName(CONTEXT_AGGREGATE, FIELD_DM_CODE));
                codeMasterDataVehicleMenuItemEnabled = !DaimlerCodes.isEmptyCodeString(codeStringVehicle);
                codeMasterDataAggregateMenuItemEnabled = !DaimlerCodes.isEmptyCodeString(codeStringAggregate);
            }
        }
        codeMasterDataVehicleMenuItem.setEnabled(codeMasterDataVehicleMenuItemEnabled);
        codeMasterDataAggregateMenuItem.setEnabled(codeMasterDataAggregateMenuItemEnabled);
    }

    private String getVirtualFieldValue(DBDataObjectAttributes selectedAttributes, String virtualFieldName) {
        DBDataObjectAttribute attrib = selectedAttributes.getField(virtualFieldName, false);
        if (attrib != null) {
            return attrib.getAsString();
        }
        return "";
    }

    @Override
    protected EtkSqlCommonDbSelect buildQuery() {
        EtkSqlCommonDbSelect sqlSelect = super.buildQuery();

        WildCardSettings wildCardSettings = new WildCardSettings();
        wildCardSettings.load(getProject().getConfig());

        // Nur solche Suchfelder (und dazugehörigen Suchwerte) in die Joins übernehmen, die sich auf das Fahrzeugbaumuster
        // bzw. Aggregatebaumuster beziehen
        List<String> searchFieldsForVehicle = new ArrayList<String>();
        List<String> searchValuesForVehicle = new ArrayList<String>();
        List<String> searchFieldsForAggregate = new ArrayList<String>();
        List<String> searchValuesForAggregate = new ArrayList<String>();
        List<String> searchValues = getSearchValues();
        int index = 0;
        for (EtkDisplayField searchField : searchFields.getFields()) {
            if (searchField.getKey().getTableName().equals(TABLE_DA_MODEL)) {
                String searchValue = searchValues.get(index);
                if (!searchValue.isEmpty()) {
                    searchValue = getProject().getDB().getDBForTable(TABLE_DA_MODEL).sqlToUpperCase(searchValue);
                    if (!searchField.isSearchExact()) {
                        searchValue = wildCardSettings.makeWildCard(searchValue);
                    }

                    // Feldname und Kontext aus dem virtuellen Feldnamen extrahieren und den Fahrzeug- bzw. Aggregatlisten zuordnen
                    String fieldName = VirtualFieldsUtils.removeVirtualFieldMask(searchField.getKey().getFieldName());
                    String context = StrUtils.stringUpToCharacter(fieldName, CONTEXT_DELIMITER);
                    fieldName = StrUtils.stringAfterCharacter(fieldName, CONTEXT_DELIMITER);
                    if (context.equals(CONTEXT_VEHICLE)) {
                        searchFieldsForVehicle.add(fieldName);
                        searchValuesForVehicle.add(searchValue);
                    } else if (context.equals(CONTEXT_AGGREGATE)) {
                        searchFieldsForAggregate.add(fieldName);
                        searchValuesForAggregate.add(searchValue);
                    }
                }
            }
            index++;
        }

        // Join mit Conditions für das Fahrzeugbaumuster hinzufügen
        if (!searchFieldsForVehicle.isEmpty()) {
            addJoinForVirtualModelSearchValues(searchFieldsForVehicle, searchValuesForVehicle, FIELD_DMA_MODEL_NO, sqlSelect);
        }

        // Join mit Conditions für das Aggregatebaumuster hinzufügen
        if (!searchFieldsForAggregate.isEmpty()) {
            addJoinForVirtualModelSearchValues(searchFieldsForAggregate, searchValuesForAggregate, FIELD_DMA_AGGREGATE_NO, sqlSelect);
        }

        return sqlSelect;
    }

    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        //Sichtbarkeit der Datensätze abh. von den Rechten prüfen
        String source = attributes.getFieldValue(FIELD_DMA_SOURCE);
        return iPartsImportDataOrigin.isSourceVisible(source, isCarAndVanInSession(), isTruckAndBusInSession());
    }

    private void addJoinForVirtualModelSearchValues(List<String> searchFieldsForVehicle, List<String> searchValuesForVehicle,
                                                    String modelFieldName, EtkSqlCommonDbSelect sqlSelect) {
        String modelTableAlias = TABLE_DA_MODEL + "_" + modelFieldName;
        Condition[] conditions = new Condition[searchFieldsForVehicle.size() + 1];

        // Condition für den Join
        conditions[0] = new Condition(TableAndFieldName.make(TABLE_DA_MODELS_AGGS, modelFieldName), Condition.OPERATOR_EQUALS,
                                      new Fields(TableAndFieldName.make(modelTableAlias, FIELD_DM_MODEL_NO)));

        EtkDatabaseTable dbTable = null;
        if (getProject().getEtkDbs().getDatabaseType(MAIN).isUseUpperCaseMode()) {
            dbTable = getProject().getEtkDbs().getConfigBase().getDBDescription().findTable(TABLE_DA_MODEL);
        }

        // Conditions pro Suchfeld mit Suchwert (mehrsprachige Felder würden so NICHT funktionieren)
        for (int i = 0; i < searchFieldsForVehicle.size(); i++) {
            String searchField = searchFieldsForVehicle.get(i);
            String searchValue = searchValuesForVehicle.get(i);
            CaseMode caseMode = getProject().getDB().getCaseModeForField(dbTable, searchField); // caseMode je nach DB-Typ für CaseInsensitive Felder auf UPPERCASE setzen
            conditions[i + 1] = new Condition(TableAndFieldName.make(modelTableAlias, searchField),
                                              StrUtils.stringContainsWildcards(searchValue) ? Condition.OPERATOR_LIKE : Condition.OPERATOR_EQUALS, Condition.PARAMETER_SIGN);
            sqlSelect.addParamValue(modelTableAlias, searchField, searchValue, caseMode);
        }
        sqlSelect.getQuery().join(new InnerJoin(TABLE_DA_MODEL + " as " + modelTableAlias, conditions));
    }

    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes createRow(DBDataObjectAttributes attributes) {
        // attributes enthält nur die Attribute für die Suchtabelle TABLE_DA_MODELS_AGGS
        // -> passende Attribute von TABLE_DA_MODEL für Fahrzeug und Aggregat hinzufügen (kann dann auch in den Ergebnissen
        // angezeigt werden)
        addVirtualModelAttributes(FIELD_DMA_MODEL_NO, CONTEXT_VEHICLE, attributes);
        addVirtualModelAttributes(FIELD_DMA_AGGREGATE_NO, CONTEXT_AGGREGATE, attributes);

        // Aggregatebaumusterart hinzufügen
        DBDataObjectAttribute modelAggTypeAttribute = new DBDataObjectAttribute(createVirtualFieldName(CONTEXT_AGGREGATE_PRODUCT,
                                                                                                       FIELD_DP_AGGREGATE_TYPE),
                                                                                DBDataObjectAttribute.TYPE.STRING, true);
        iPartsModelId aggregateModelId = new iPartsModelId(attributes.getField(FIELD_DMA_AGGREGATE_NO).getAsString());

        // Baumusterart vom ersten Produkt anzeigen falls vorhanden
        List<iPartsProduct> products = iPartsProductHelper.getProductsForModel(getProject(), aggregateModelId, null, null, false);
        String aggregateModelType = "";
        if (!products.isEmpty()) {
            aggregateModelType = products.get(0).getAggregateType();
        }
        modelAggTypeAttribute.setValueAsString(aggregateModelType, DBActionOrigin.FROM_DB);
        attributes.addField(modelAggTypeAttribute, DBActionOrigin.FROM_DB);

        return super.createRow(attributes);
    }

    private void addVirtualModelAttributes(String modelFieldName, String virtualFieldContext, DBDataObjectAttributes attributes) {
        iPartsModelId modelId = new iPartsModelId(attributes.getField(modelFieldName).getAsString());
        iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
        if (!dataModel.existsInDB()) {
            dataModel.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
        }
        for (DBDataObjectAttribute attribute : dataModel.getAttributes().values()) {
            // EtkMultiSprache laden falls notwendig, weil es später über den virtuellen Feldnamen nicht mehr geht
            if (attribute.getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                attribute.getAsMultiLanguage(dataModel, false);
            }

            // virtuellen Feldnamen setzen
            attribute.__internal_setName(createVirtualFieldName(virtualFieldContext, attribute.getName()));

            attributes.addField(attribute, DBActionOrigin.FROM_DB);
        }
    }

    public class SimpleMasterDataSearchFilterModelsAggsFactory extends SimpleMasterDataSearchFilterGrid.SimpleMasterDataSearchFilterFactory {

        public SimpleMasterDataSearchFilterModelsAggsFactory(EtkProject project) {
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
