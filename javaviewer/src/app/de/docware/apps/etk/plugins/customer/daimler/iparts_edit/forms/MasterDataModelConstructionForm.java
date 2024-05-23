/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.DIALOGModelsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Formular für die Anzeige der Stammdaten von Baumustern aus der Konstruktion (Tabellen DA_MODEL_PROPERTIES und DA_MODEL_DATA).
 */
public class MasterDataModelConstructionForm extends SimpleMasterDataSearchFilterGrid {

    private GuiCheckbox checkboxShowHistory;
    private GuiMenuItem codeMasterDataMenuItem;
    private GuiMenuItem unifyMenuItem;

    /**
     * Zeigt Baumusterstammdaten an. Wenn ein Baumusterknoten im Navigationsbaum selektiert ist, wird er als
     * Suchkriterium übernommen. Falls eine Baureihe selektiert ist, werden alle dazugehörigen Baumuster angezeigt.
     * Falls keiner selektiert ist, wird eine leere Stammdatenliste angezeigt.
     *
     * @param owner
     */
    public static void showModelConstructionMasterData(AbstractJavaViewerForm owner) {
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
        showModelConstructionMasterData(activeForm.getConnector(), activeForm, seriesId, modelId, null);
    }

    private static void showModelConstructionMasterData(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                        iPartsSeriesId seriesId, iPartsModelId modelId,
                                                        OnEditChangeRecordEvent onEditChangeRecordEvent) {
        if (onEditChangeRecordEvent == null) {
            // Falls nicht extern definiert: Hier die Callbacks für Edit, Modify und Delete definieren
            onEditChangeRecordEvent = new OnEditChangeRecordEvent() {
                private boolean onEditCreateOrModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector,
                                                                IdWithType id, DBDataObjectAttributes attributes, boolean createRecord) {
                    iPartsModelPropertiesId modelPropertiesId = new iPartsModelPropertiesId(id.getValue(1), id.getValue(2));
                    iPartsDataModelProperties dataModelProperties = new iPartsDataModelProperties(dataConnector.getProject(), modelPropertiesId);
                    iPartsModelDataId modelDataId = new iPartsModelDataId(modelPropertiesId.getModelNumber());
                    iPartsDataModelData dataModelData = new iPartsDataModelData(dataConnector.getProject(), modelDataId);
                    if (dataModelProperties.loadFromDB(modelPropertiesId) && createRecord) {
                        String msg = "!!Das Baumuster ist bereits vorhanden und kann nicht neu angelegt werden!";
                        MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                        return true;
                    }
                    if (createRecord) {
                        dataModelProperties.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        // Setze den Status und die Quelle
                        dataModelProperties.setFieldValue(FIELD_DMA_SOURCE, iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);
                        dataModelProperties.setFieldValue(FIELD_DMA_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
                    }
                    if (!dataModelData.loadFromDB(modelDataId) || createRecord) { // Eintrag in DA_MODEL_DATA könnte fehlen -> neu anlegen
                        dataModelData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    }

                    // Attributes aufsplitten in Attribute für dataModelProperties und dataModelData
                    DBDataObjectAttributes modelPropertiesAttributes = new DBDataObjectAttributes();
                    DBDataObjectAttributes modelDataAttributes = new DBDataObjectAttributes();
                    for (DBDataObjectAttribute attribute : attributes.values()) {
                        if (dataModelProperties.attributeExists(attribute.getName())) {
                            modelPropertiesAttributes.addField(attribute, DBActionOrigin.FROM_DB);
                        } else if (dataModelData.attributeExists(attribute.getName())) {
                            modelDataAttributes.addField(attribute, DBActionOrigin.FROM_DB);
                        }
                    }
                    dataModelProperties.assignAttributesValues(dataConnector.getProject(), modelPropertiesAttributes, true, DBActionOrigin.FROM_EDIT);
                    dataModelData.assignAttributesValues(dataConnector.getProject(), modelDataAttributes, true, DBActionOrigin.FROM_EDIT);

                    dataConnector.getProject().getDbLayer().startTransaction();
                    try {
                        dataModelProperties.saveToDB();
                        dataModelData.saveToDB();
                        dataConnector.getProject().getDbLayer().commit();
                        return true;
                    } catch (Exception e) {
                        dataConnector.getProject().getDbLayer().rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }
                    return false;
                }

                @Override
                public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       IdWithType id, DBDataObjectAttributes attributes) {
                    return onEditCreateOrModifyRecordEvent(dataConnector, id, attributes, true);
                }

                @Override
                public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                       IdWithType id, DBDataObjectAttributes attributes) {
                    return onEditCreateOrModifyRecordEvent(dataConnector, id, attributes, false);
                }

                @Override
                public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                    if ((attributeList != null) && !attributeList.isEmpty()) {
                        List<String> noDelList = new DwList<>();
                        for (int lfdNr = attributeList.size() - 1; lfdNr >= 0; lfdNr--) {
                            DBDataObjectAttributes attributes = attributeList.get(lfdNr);
                            String constructionModelId = attributes.getField(FIELD_DMA_MODEL_NO).getAsString();
                            iPartsModelId afterSalesModelId = DIALOGModelsHelper.getAfterSalesModelId(new iPartsModelDataId(constructionModelId));

                            // Verwendung wird für After-Sales Baumuster überprüft
                            iPartsDataModel dataModel = new iPartsDataModel(dataConnector.getProject(), afterSalesModelId);
                            if (dataModel.hasDependencies()) {
                                noDelList.add(afterSalesModelId.getModelNumber());
                                attributeList.remove(lfdNr);
                            }
                        }
                        if (noDelList.size() > 0) {
                            String msg = "!!Die selektierten Baumuster werden noch verwendet und können nicht gelöscht werden!";
                            if (noDelList.size() == 1) {
                                msg = "!!Das selektierte Baumuster wird noch verwendet und kann nicht gelöscht werden!";
                            }
                            MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.WARNING, MessageDialogButtons.OK);

                        } else {
                            String msg = "!!Wollen Sie das selektierte Baumuster wirklich löschen?";
                            if (attributeList.size() > 1) {
                                msg = "!!Wollen Sie die selektierten Baumuster wirklich löschen?";
                            }
                            if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                                   MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                                return true;
                            }
                        }
                    }
                    return false;
                }

                @Override
                public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                    if ((attributeList != null) && !attributeList.isEmpty()) {
                        dataConnector.getProject().getDbLayer().startTransaction();
                        try {
                            for (DBDataObjectAttributes attributes : attributeList) {
                                iPartsModelPropertiesId modelId = new iPartsModelPropertiesId(attributes.getField(FIELD_DMA_MODEL_NO).getAsString(),
                                                                                              attributes.getField(FIELD_DMA_DATA).getAsString());
                                iPartsDataModelProperties dataModel = new iPartsDataModelProperties(dataConnector.getProject(), modelId);
                                dataModel.deleteFromDB(true);
                            }
                            dataConnector.getProject().getDbLayer().commit();
                            return true;
                        } catch (Exception e) {
                            dataConnector.getProject().getDbLayer().rollback();
                            Logger.getLogger().handleRuntimeException(e);
                        }
                    }
                    return false;
                }
            };
        }

        final MasterDataModelConstructionForm dlg = new MasterDataModelConstructionForm(dataConnector, parentForm, TABLE_DA_MODEL_PROPERTIES, onEditChangeRecordEvent);

        // Suchfelder definieren
        EtkDisplayFields searchFields = new EtkDisplayFields();
        searchFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_CONSTRUCTION_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS,
                          dataConnector.getConfig().getCurrentDatabaseLanguage());
        if (searchFields.size() == 0) {
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_MODEL_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_CODE, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_SERIES_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_AS_RELEVANT, false, false));
        }

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_CONSTRUCTION_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkProject project = dataConnector.getProject();
            EtkDisplayField displayField = addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_MODEL_NO, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_SERIES_NO, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_AA, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_STEERING, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_NAME, true, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_DEVELOPMENT_TITLE, true, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_SALES_TITLE, true, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_PRODUCT_GRP, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_CODE, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_DATA, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_DATB, false, false, null, project, displayFields);
            displayField = addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_DRIVE_SYSTEM, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_ENGINE_CONCEPT, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_CYLINDER_COUNT, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_ENGINE_KIND, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_HORSEPOWER, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_KILOWATTS, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_DATA, FIELD_DMD_MODEL_INVALID, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_AS_RELEVANT, false, false, null, project, displayFields);
            addDisplayField(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_STATUS, false, false, null, project, displayFields);
        }

        // Benötigte Ergebnisfelder (abgesehen von den Primärschlüsselfeldern) definieren
        EtkDisplayFields requiredResultFields = new EtkDisplayFields();
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_CODE, false, false));
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_SERIES_NO, false, false));
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_PRODUCT_GRP, false, false));
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_DATA, false, false));
        requiredResultFields.addFeld(createDisplayField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_DATB, false, false));

        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_CONSTRUCTION_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_MODEL_NO, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_SERIES_NO, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_AA, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_STEERING, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_NAME, true));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_DEVELOPMENT_TITLE, true));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_SALES_TITLE, true));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_PRODUCT_GRP, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_CODE, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_DATA, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_DATB, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_DRIVE_SYSTEM, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_ENGINE_CONCEPT, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_CYLINDER_COUNT, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_ENGINE_KIND, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_HORSEPOWER, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_KILOWATTS, false));
            editFields.addFeld(createEditField(dataConnector.getProject(), TABLE_DA_MODEL_DATA, FIELD_DMD_MODEL_INVALID, false));
            EtkEditField asRelevantEditField = createEditField(dataConnector.getProject(), TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_AS_RELEVANT, false);
            // Flag nur zur Anzeige, da Änderungen daran nur automatisiert in Importern und BM-Auswahl gemacht werden
            asRelevantEditField.setEditierbar(false);
            editFields.addFeld(asRelevantEditField);
        } else {
            // Es muss das richtige Feld für die Baumusternummer im Edit verwendet werden, weil die iPartsModelPropertiesId
            // ansonsten nicht korrekt ermittelt werden kann
            EtkEditField wrongModelNumberField = editFields.getFeldByName(TABLE_DA_MODEL_DATA, FIELD_DMD_MODEL_NO);
            if (wrongModelNumberField != null) {
                // Check, ob beide Baumusterfelder konfiguriert wurden. Falls ja, nur das von TABLE_DA_MODEL_PROPERTIES
                // anzeigen
                EtkEditField rightModelNumberField = editFields.getFeldByName(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_MODEL_NO);
                if (rightModelNumberField == null) {
                    wrongModelNumberField.setKey(new EtkDisplayFieldKeyNormal(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_MODEL_NO));
                } else {
                    wrongModelNumberField.setVisible(false);
                }
            }
        }

        EtkDatabaseTable tableDef = dataConnector.getConfig().getDBDescription().findTable(TABLE_DA_MODEL_PROPERTIES);
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
        sortFields.put(FIELD_DMA_DATA, true);
        dlg.setSortFields(sortFields);

        dlg.setDisplayResultFields(displayFields);
        dlg.setRequiredResultFields(requiredResultFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        dlg.setEditAllowed(editMasterDataAllowed);
        dlg.setNewAllowed(editMasterDataAllowed);
        dlg.setModifyAllowed(editMasterDataAllowed);
        dlg.setDeleteAllowed(false);
        dlg.setTitlePrefix("!!Baumuster (Konstruktion)");
        dlg.setWindowName("ModelConstructionMasterData");
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);

        DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
        if ((seriesId != null) && seriesId.isValidId()) {
            // Suchwerte setzen und Suche starten
            searchAttributes.addField(FIELD_DMA_SERIES_NO, seriesId.getSeriesNumber(), DBActionOrigin.FROM_DB);
        }
        if ((modelId != null) && modelId.isValidId()) {
            // Suchwerte setzen und Suche starten
            searchAttributes.addField(FIELD_DMA_MODEL_NO, modelId.getModelNumber(), DBActionOrigin.FROM_DB);
        }
        if (!searchAttributes.isEmpty()) {
            dlg.setSearchValues(searchAttributes);
        }
        dlg.showModal();
    }

    private MasterDataModelConstructionForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        setColumnFilterFactory(new SimpleMasterDataSearchFilterModelConstructionFactory(getProject()));
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();

        // Checkbox für "Alle Stände"
        checkboxShowHistory = new GuiCheckbox("!!Alle Stände", false);
        checkboxShowHistory.setConstraints(new ConstraintsGridBag(0, 2, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL, 8, 4, 8, 4));
        getPanelMain().addChild(checkboxShowHistory);
        checkboxShowHistory.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                DBDataObjectAttributes selection = getSelection();
                if (selection != null) {
                    setSelectionAfterSearch(selection);
                }
                startSearch();
            }
        });
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        unifyMenuItem = toolbarHelper.createMenuEntry("unify", "!!Vereinheitlichen...", null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                endSearch();
                doUnifySeriesConstructionValues();
            }
        }, getUITranslationHandler());
        unifyMenuItem.setVisible(false);  // TODO Freischalten wenn Werte vereinheitlicht werden sollen
        contextMenu.addChild(unifyMenuItem);

        // Popup-Menüeintrag für Code-Stammdaten
        codeMasterDataMenuItem = iPartsCodeMatrixDialog.createMenuItem(new iPartsCodeMatrixDialog.CodeMasterDataCallback() {
            @Override
            public iPartsCodeMatrixDialog.CodeMasterDataQuery getCodeMasterDataQuery() {
                endSearch();
                DBDataObjectAttributes attributes = getSelectedAttributes();
                if (attributes != null) {
                    return new iPartsCodeMatrixDialog.CodeMasterDataQuery(attributes.getField(FIELD_DMA_CODE).getAsString(),
                                                                          new iPartsSeriesId(attributes.getField(FIELD_DMA_SERIES_NO).getAsString()),
                                                                          attributes.getField(FIELD_DMA_PRODUCT_GRP).getAsString(),
                                                                          attributes.getField(FIELD_DMA_DATA).getAsString());
                } else {
                    return null;
                }
            }
        }, this);

        contextMenu.addChild(new GuiSeparator());
        contextMenu.addChild(codeMasterDataMenuItem);
    }

    @Override
    protected void doEditOrView(Event event) {
        endSearch();
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            IdWithType id = buildIdFromAttributes(attributes);
            EditUserControlForConstModel eCtrl = new EditUserControlForConstModel(getConnector(), this, searchTable, id, attributes, editFields);
            boolean editAndModifyAllowed = isEditAllowed() && isModifyAllowed();
            eCtrl.setReadOnly(!editAndModifyAllowed);
            eCtrl.setTitle(editAndModifyAllowed ? titleForEdit : titleForView);
            eCtrl.setWindowName(editControlsWindowName);
            if (eCtrl.showModal() == ModalResult.OK) {
                if (onEditChangeRecordEvent != null) {
                    if (onEditChangeRecordEvent.onEditModifyRecordEvent(getConnector(), searchTable, id, eCtrl.getAttributes())) {
                        // Suche nochmals starten als Refresh für Table
                        setSelectionAfterSearch(eCtrl.getAttributes());
                        startSearch(true);
                    }
                }
            }
        }
    }

    private void doUnifySeriesConstructionValues() {
        DBDataObjectAttributesList attributeList = getSelectedAttributesList();
        if (attributeList != null) {
            iPartsDataModelPropertiesList dataModelPropertiesList = new iPartsDataModelPropertiesList();
            for (DBDataObjectAttributes attributes : attributeList) {
                iPartsModelPropertiesId modelPropertiesId = new iPartsModelPropertiesId(attributes.getFieldValue(FIELD_DMA_MODEL_NO),
                                                                                        attributes.getFieldValue(FIELD_DMA_DATA));
                iPartsDataModelProperties dataModelProperties = new iPartsDataModelProperties(getProject(), modelPropertiesId);
                dataModelProperties.existsInDB();
                dataModelPropertiesList.add(dataModelProperties, DBActionOrigin.FROM_DB);
            }
            EtkEditFields editFields = new EtkEditFields();

            // Anzeige des Unify-Dialogs
            DBDataObjectAttributes attributes =
                    EditUserMultiChangeControls.showEditUserMultiChangeControlsForConstModels(getConnector(), editFields,
                                                                                              dataModelPropertiesList);
            if (attributes != null) {
                for (EtkEditField editField : editFields.getFields()) {
                    String fieldName = editField.getKey().getFieldName();
                    String fieldValue = attributes.getFieldValue(fieldName);
                    for (iPartsDataModelProperties dataModelProperties : dataModelPropertiesList) {
                        dataModelProperties.setFieldValue(fieldName, fieldValue, DBActionOrigin.FROM_DB);
                    }
                }
                // TODO Ergebnisse im ChangeSet abspeichern
            }
        }
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();

        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelection = selectionRowCount == 1;

        boolean enabled = false;
        if (singleSelection) {
            DBDataObjectAttributes selectedAttributes = getSelectedAttributes();
            if (selectedAttributes != null) {
                String codeString = selectedAttributes.getFieldValue(FIELD_DMA_CODE);
                enabled = !DaimlerCodes.isEmptyCodeString(codeString);
            }
        }
        codeMasterDataMenuItem.setEnabled(enabled);
    }

    @Override
    protected boolean useMaxResultsForSQLHitLimit() {
        return checkboxShowHistory.isSelected(); // Aufgrund der Filterung in doValidateAttributes()
    }

    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        if (!checkboxShowHistory.isSelected()) { // historische Stände ausblenden (dort steht SDATB nicht auf unendlich)
            String dateTo = attributes.getField(FIELD_DMA_DATB).getAsString();
            iPartsDialogDateTimeHandler dialogDateTimeHandler = new iPartsDialogDateTimeHandler(dateTo);
            if (!dialogDateTimeHandler.isFinalStateDateTime()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes createRow(DBDataObjectAttributes attributes) {
        // attributes enthält nur die Attribute für die Suchtabelle TABLE_DA_MODEL_PROPERTIES
        // -> passende Attribute von TABLE_DA_MODEL_DATA hinzufügen
        iPartsModelDataId modelId = new iPartsModelDataId(attributes.getField(FIELD_DMA_MODEL_NO).getAsString());
        iPartsDataModelData modelData = new iPartsDataModelData(getProject(), modelId);
        if (modelData.loadFromDB(modelId)) {
            attributes.addFields(modelData.getAttributes(), DBActionOrigin.FROM_DB);
        }

        return super.createRow(attributes);
    }

    public class SimpleMasterDataSearchFilterModelConstructionFactory extends SimpleMasterDataSearchFilterGrid.SimpleMasterDataSearchFilterFactory {

        public SimpleMasterDataSearchFilterModelConstructionFactory(EtkProject project) {
            super(project);
        }

        @Override
        protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> calculateFilteredList() {
            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries;
            if (getAssemblyListFilter().isDBFilterActive()) {
                entries = new DwList<>();
                Set<String> tableNames = new TreeSet<>();
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
                entries = new DwList<>(getEntries());
            }
            return entries;
        }

    }
}
