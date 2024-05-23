/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.interfaces.OnMoveDataObjectsEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.DIALOGModelsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelPropertiesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EditSelectConstModelsForm extends EditSelectModelsForm implements iPartsConst {

    private static final String[] MANDATORY_CONST_DISPLAY_FIELDS = new String[]{ FIELD_DMA_AS_RELEVANT };
    private static final String[] MANDATORY_AS_DISPLAY_FIELDS = new String[]{ FIELD_DM_CONST_MODEL_NO, FIELD_DM_DATA };

    /**
     * Zeigt den Baumusterauswahldialog mit den übergebenen Daten, wobei <i>productId</i> und <i>partialModelNumberWithWildCard</i>
     * auch kombiniert werden können.
     *
     * @param parentForm
     * @param productId                      {@link iPartsProductId} für eine Liste der verfügbaren Baumuster zu diesem Produkt
     * @param partialModelNumberWithWildCard (Partielle) Baumusternummer, die auch Wildcards enthalten kann, für die
     *                                       Suche/Vorfilterung der verfügbaren Baumuster
     * @param modelNumberSearchFieldVisible  Flag, ob das Suchfeld für die Baumusternummer angezeigt werden soll
     * @param modelList                      Liste der bisher ausgewählten Baumuster
     * @param asModelList
     * @param constModelList
     * @return Liste der ausgewählten Baumuster
     */
    public static Collection<String> showSelectionConstModels(AbstractJavaViewerForm parentForm, iPartsProductId productId,
                                                              String partialModelNumberWithWildCard, boolean modelNumberSearchFieldVisible,
                                                              Collection<String> modelList, iPartsDataModelList asModelList,
                                                              iPartsDataModelPropertiesList constModelList) {
        EditSelectConstModelsForm dlg = new EditSelectConstModelsForm(parentForm.getConnector(), parentForm, modelList,
                                                                      asModelList, constModelList);
        dlg.setProductId(productId);
        dlg.setModelNumberSearchFieldVisible(modelNumberSearchFieldVisible);
        dlg.setModelNumberSearchValue(partialModelNumberWithWildCard, true); // befüllt die verfügbaren Baumuster

        if (dlg.showModal() == ModalResult.OK) {
            dlg.setASModelList(asModelList);
            dlg.setConstModelList(constModelList);
            return dlg.getSelectedModelsList();
        }

        return null;
    }

    private iPartsDataModelList asModelList;
    private iPartsDataModelPropertiesList constModelList;
    private GuiMenuItem editMenuItem;
    private boolean markLines;

    /**
     * Erzeugt eine Instanz von EditSelectModelsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param modelList
     */
    public EditSelectConstModelsForm(final AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                     Collection<String> modelList, iPartsDataModelList asModelList,
                                     iPartsDataModelPropertiesList constModelList) {
        super(dataConnector, parentForm, iPartsConst.TABLE_DA_MODEL_PROPERTIES, null,
              iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_CONSTRUCTION_ASSIGNMENT_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_ASSIGNMENT_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        this.asModelList = new iPartsDataModelList();
        for (EtkDataObject dataObject : asModelList) {
            this.asModelList.add((iPartsDataModel)dataObject, dataObject.isModifiedWithChildren() ? DBActionOrigin.FROM_EDIT : DBActionOrigin.FROM_DB);
        }
        //this.asModelList.addAll(asModelList, DBActionOrigin.FROM_DB);
        this.constModelList = new iPartsDataModelPropertiesList();
        this.constModelList.addAll(constModelList, DBActionOrigin.FROM_DB);
        setName("SelectConstModelsForm");
        setAvailableEntriesTitle("!!Verfügbare Konstruktions-Baumuster:");
        setSelectedEntriesTitle("!!Übernommene AS-Baumuster:");
        setWithDeleteEntry(false);
        setWithSetSelection(true);
        fireTableSelectionEventBySetSelection = true;
        setToolbarButtonVisible(EditToolbarButtonAlias.IMG_RIGHT_ALL, false);
        setAvailableEntriesMultiSelect(false);
        setSelectedTableName(iPartsConst.TABLE_DA_MODEL);
        setModelNumberSearchTextEditable(false);
        setModelNumberSearchLabelText("!!Baureihe");
        setOnMoveDataObjectsEvent(new OnMoveDataObjectsEvent() {
            @Override
            public boolean doBeforeAddEntries(List<EtkDataObject> selectedList) {
                return handleBeforeAddEntry(selectedList);
            }

            @Override
            public boolean doBeforeRemoveEntries(List<EtkDataObject> selectedList) {
                availableEntriesGrid.clearMarkedRows();
                return true;
            }
        });
        this.markLines = true;

        // Initialisierung beim erstmaligen Aufruf
        List<EtkDataObject> selectedList = new DwList<>();
        if (this.asModelList.isEmpty()) {
            if (!modelList.isEmpty()) {
                this.modelList = modelList;
                for (String model : modelList) {
                    iPartsModelId modelId = new iPartsModelId(model);
                    iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
                    selectedList.add(dataModel);
                    this.asModelList.add(dataModel, DBActionOrigin.FROM_DB);
                }
            }
        } else {
            for (EtkDataObject dataObject : this.asModelList) {
                selectedList.add(dataObject);
                this.modelList.add(((iPartsDataModel)dataObject).getAsId().getModelNumber());
            }
        }
        if (!selectedList.isEmpty()) {
            initSelectedEntries(selectedList);
        }
        List<AbstractGuiControl> menuList = new DwList<>();
        GuiSeparator separator = new GuiSeparator();
        menuList.add(separator);
        editMenuItem = new GuiMenuItem();
        editMenuItem.setName("editemenu");
        editMenuItem.setText("!!Bearbeiten");
        editMenuItem.setIcon(EditToolbarButtonAlias.EDIT_WORK.getImage());
        editMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doEdit(event);
            }
        });
        menuList.add(editMenuItem);
        addContextMenuEntriesToSelectedEntriesGrid(menuList);
    }

    public void setASModelList(iPartsDataModelList asModelList) {
        asModelList.clear(DBActionOrigin.FROM_DB);
        for (EtkDataObject dataObject : getCompleteSelectedList()) {
            if (dataObject instanceof iPartsDataModel) {
                asModelList.add((iPartsDataModel)dataObject, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    public void setConstModelList(iPartsDataModelPropertiesList currentConstModelList) {
        currentConstModelList.clear(DBActionOrigin.FROM_DB);
        currentConstModelList.addAll(constModelList, DBActionOrigin.FROM_DB);
    }

    @Override
    protected void setProductId(iPartsProductId productId) {
        // productId ist absichtlich null, da EditSelectModelsForm.fillAvailableModelsList() nur bei null
        // Alle Konstruktionsbaumuster lädt und nicht nur die dem Produkt zugeordneten.
        super.setProductId(null);
    }

    @Override
    protected boolean areEntriesChanged() {
        boolean result = super.areEntriesChanged();
        if (!result) {
            result = asModelList.isModifiedWithChildren();
        }
        return result;
    }

    @Override
    protected void doEnableButtons() {
        super.doEnableButtons();
        if (editMenuItem == null) {
            return;
        }
        List<EtkDataObject> selectedList = getSelectedList(selectedEntriesGrid);
        boolean enabled = selectedList.size() == 1;
        editMenuItem.setEnabled(enabled);
    }

    protected iPartsDataModel getSelectedASModel() {
        return getSelectedASModel(getSelectedList(selectedEntriesGrid));
    }

    protected iPartsDataModelProperties getSelectedConstModel() {
        return getSelectedConstructionModel(getSelectedList(availableEntriesGrid));
    }

    protected iPartsDataModel getSelectedASModel(List<EtkDataObject> selectedList) {
        if ((selectedList != null) && (selectedList.size() == 1)) {
            EtkDataObject dataObject = selectedList.get(0);
            if (dataObject instanceof iPartsDataModel) {
                return (iPartsDataModel)dataObject;
            }
        }
        return null;
    }

    protected iPartsDataModelProperties getSelectedConstructionModel(List<EtkDataObject> selectedList) {
        if ((selectedList != null) && (selectedList.size() == 1)) {
            EtkDataObject dataObject = selectedList.get(0);
            if (dataObject instanceof iPartsDataModelProperties) {
                return (iPartsDataModelProperties)dataObject;
            }
        }
        return null;
    }

    @Override
    protected void doSelectionChangedSelectedGrid(Event event) {
        iPartsDataModel asDataModel = getSelectedASModel();
        if (asDataModel != null) {
            iPartsModelPropertiesId modelPropertiesId = asDataModel.getRelatedConstructionId();
            if (modelPropertiesId.isValidId()) {
                int rowNo = getConnectedConstModelRowNo(modelPropertiesId);
                if (rowNo == -1) {
                    availableEntriesGrid.getTable().clearSelection();
                    availableEntriesGrid.clearMarkedRows();
                } else {
                    int[] selectedRows = new int[]{ rowNo };
                    availableEntriesGrid.getTable().setSelectedRows(selectedRows, false, true);
                    if (markLines) {
                        List<Integer> rowList = getConstModelKEMVariantListRowNos(asDataModel.getAsId());
                        availableEntriesGrid.setMarkedRows(rowList, false);
                    }
                }
            } else {
                availableEntriesGrid.getTable().clearSelection();
                availableEntriesGrid.clearMarkedRows();
            }
        }
    }

    @Override
    protected void doSelectionChangedAvailableGrid(Event event) {
        iPartsDataModelProperties selectedConstModel = getSelectedConstModel();
        if (selectedConstModel != null) {
            iPartsModelPropertiesId modelPropertiesId = selectedConstModel.getAsId();
            if (modelPropertiesId.isValidId()) {
                int rowNo = getASModelRowNoForConstId(modelPropertiesId);
                if (rowNo == -1) {
                    selectedEntriesGrid.getTable().clearSelection();
                    availableEntriesGrid.clearMarkedRows();
                } else {
                    int[] selectedRows = new int[]{ rowNo };
                    selectedEntriesGrid.getTable().setSelectedRows(selectedRows, false, true);
                    doSelectionChangedSelectedGrid(event);
                }
            }
        }
    }

    @Override
    protected boolean doCellDblClickedSelectedGrid(Event event) {
        // dieser Dialog wird nur für die DocuTypen DIALOG und DIALOG iParts aufgerufen. In diesen Fällen soll nur editiert werden
        doEdit(event);
        return false; // damit nicht gelöscht wird
    }

    private void doEdit(Event event) {
        iPartsDataModel asDataModel = getSelectedASModel();
        if (asDataModel != null) {
            iPartsDataModelProperties dataModelProperties;
            iPartsModelPropertiesId modelPropertiesId = asDataModel.getRelatedConstructionId();
            boolean constModelNotFound = false;
            if (modelPropertiesId.isValidId()) {
                dataModelProperties = getConnectedConstModel(modelPropertiesId);
                if (dataModelProperties == null) {
                    dataModelProperties = new iPartsDataModelProperties(getProject(), modelPropertiesId);
                    if (!dataModelProperties.existsInDB()) {
                        dataModelProperties = new iPartsDataModelProperties(getProject(), new iPartsModelPropertiesId());
                        constModelNotFound = true;
                    }
                }
            } else {
                dataModelProperties = new iPartsDataModelProperties(getProject(), new iPartsModelPropertiesId());
            }
            iPartsDataModel editedDataModel = EditUserControlForModelMapping.editASSelectedModelData(getConnector(), this,
                                                                                                     asDataModel,
                                                                                                     dataModelProperties,
                                                                                                     constModelNotFound);
            if (editedDataModel != null) {
                asDataModel.assign(getProject(), editedDataModel, DBActionOrigin.FROM_EDIT);
                int[] selectedRows = selectedEntriesGrid.getTable().getSelectedRowIndices();
                initSelectedEntries(getCompleteSelectedList());
                selectedEntriesGrid.getTable().setSelectedRows(selectedRows, false, true);
                doEnableButtons();
                doEnableOKButton();
            }
        }
    }

    private boolean handleBeforeAddEntry(List<EtkDataObject> selectedList) {
        iPartsDataModelProperties sourceConstructionModel = getSelectedConstructionModel(selectedList);
        if (sourceConstructionModel != null) {
            iPartsDataModelProperties connectedConstructionModel;
            boolean doAdd = false;
            boolean isNew = false;

            // Suche mit der AS-Baumusternummer nach einem Eintrag im rechten Grid
            iPartsModelId extractedASModelId = DIALOGModelsHelper.getAfterSalesModelId(new iPartsModelDataId(sourceConstructionModel.getAsId().getModelNumber()));
            iPartsDataModel existingASDataModel = findASModel(extractedASModelId);
            if (existingASDataModel != null) {
                // AS-Baumuster im rechten Grid gefunden
                connectedConstructionModel = determineConnectedConstModel(existingASDataModel, sourceConstructionModel);
            } else {
                // Mit der extrahierten AS-Baumusternummer wurde kein Eintrag im rechten Grid gefunden. Prüfe, ob das
                // AS-Baumuster schon in der DB existiert.
                doAdd = true;
                existingASDataModel = new iPartsDataModel(getProject(), extractedASModelId);
                if (existingASDataModel.existsInDB()) {
                    // AS-Baumuster existiert in der DB -> Bestimme das verknüpfte Konstruktionsbaumuster
                    connectedConstructionModel = determineConnectedConstModel(existingASDataModel, sourceConstructionModel);
                } else {
                    // Das AS-Baumuster existiert nicht in der DB und muss angelegt werden
                    // Neu no Connection
                    existingASDataModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    connectedConstructionModel = null;
                    isNew = true;
                }
            }

            iPartsDataModel afterSalesModel = EditUserControlForModelMapping.editASModelData(getConnector(),
                                                                                             this,
                                                                                             sourceConstructionModel,
                                                                                             connectedConstructionModel,
                                                                                             existingASDataModel,
                                                                                             isNew);
            if (afterSalesModel != null) {
                // Verwaltung des modifizierten DataObjects im linken Grid anpassen
                modifyConstructionModelList(afterSalesModel);

                selectedList.clear();
                if (!doAdd) {
                    // Modifikation einer bestehenden Zuordnung
                    existingASDataModel.assign(getProject(), afterSalesModel, DBActionOrigin.FROM_EDIT);
                    initSelectedEntries(getCompleteSelectedList());
                    doEnableButtons();
                    doEnableOKButton();
                    return false;
                } else {
                    // neue Zuordnung: Filter relevant Flag setzen
                    afterSalesModel.setFieldValueAsBoolean(FIELD_DM_FILTER_RELEVANT, true, DBActionOrigin.FROM_EDIT);
                    selectedList.add(afterSalesModel);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doAdd(Event event) {
        // Sortierung merken
        Object storageSelected = selectedEntriesGrid.getFilterAndSortSettings();
        super.doAdd(event);
        // Sortierung wieder herstellen
        selectedEntriesGrid.restoreFilterAndSortSettings(storageSelected);
        refreshAvailableGrid();
    }

    @Override
    protected void doRemove(Event event) {
        // Sortierung merken
        Object storageSelected = selectedEntriesGrid.getFilterAndSortSettings();
        super.doRemove(event);
        // Sortierung wieder herstellen
        selectedEntriesGrid.restoreFilterAndSortSettings(storageSelected);
        refreshAvailableGrid();
    }

    /**
     * Bestimmt den Konstruktionsbaumusterdatensatz zum übergebenen AS-Baumuster und selektierten Konstruktionsbaumuster
     *
     * @param existingASDataModel
     * @param sourceConstructionModel
     * @return
     */
    private iPartsDataModelProperties determineConnectedConstModel(iPartsDataModel existingASDataModel, iPartsDataModelProperties sourceConstructionModel) {
        iPartsModelPropertiesId relatedDataModelId = existingASDataModel.getRelatedConstructionId();
        // Check, ob am AS-Baumuster ein gültiges Konstruktionsbaumuster hängt
        if (relatedDataModelId.isValidId()) {
            // Am AS-Baumuster hängt ein Konstruktionsbaumuster
            // Check, ob das ausgewählte und das am AS-baumuster hängende Konstruktionsbaumuster die gleiche ID haben
            if (relatedDataModelId.equals(sourceConstructionModel.getAsId())) {
                // Gleiche IDs -> Editiere diese Verbindung
                return sourceConstructionModel;
            } else {
                // override connected
                iPartsDataModelProperties connectedConstructionModel = getConnectedConstModel(relatedDataModelId);
                if (connectedConstructionModel == null) {
                    // AS-Baumuster hat ein verknüpftes Konstruktionsbaumuster, das zu einem früheren Zeitpunkt verknüpft
                    // wurde und nun nicht mehr im linken Grid vorkommt (z.B. Änderung der referenzierten Baureihe)
                    connectedConstructionModel = new iPartsDataModelProperties(getProject(), relatedDataModelId);
                    // Check, ob es in der DB noch existiert
                    if (connectedConstructionModel.existsInDB()) {
                        return connectedConstructionModel;
                    }
                } else {
                    return connectedConstructionModel;
                }
            }
        } else {
            // Am AS-Baumuster hängt kein Konstruktionsbaumuster
            return new iPartsDataModelProperties(getProject(), new iPartsModelPropertiesId());
        }
        return null;
    }

    /**
     * Setzt das verknüpfte Konstruktions-Baumuster auf "AS relevant" und alle anderen konstruktiven Baumuster zum
     * AS-Baumuster auf "nicht AS relevant".
     *
     * @param afterSalesModel
     * @return
     */
    private boolean modifyConstructionModelList(iPartsDataModel afterSalesModel) {
        boolean result = false;
        iPartsModelPropertiesId connectedModelPropertiesId = afterSalesModel.getRelatedConstructionId();
        if (connectedModelPropertiesId != null) {
            List<iPartsDataModelProperties> constModelVariantList = getConstModelKEMVariantList(afterSalesModel.getAsId(),
                                                                                                connectedModelPropertiesId);
            if (!constModelVariantList.isEmpty()) {
                for (iPartsDataModelProperties dataModelProperties : constModelVariantList) {
                    if (dataModelProperties.getAsId().equals(connectedModelPropertiesId)) {
                        if (!dataModelProperties.getFieldValueAsBoolean(FIELD_DMA_AS_RELEVANT)) {
                            result = true;
                        }
                        dataModelProperties.setFieldValueAsBoolean(FIELD_DMA_AS_RELEVANT, true, DBActionOrigin.FROM_EDIT);
                    } else {
                        if (dataModelProperties.getFieldValueAsBoolean(FIELD_DMA_AS_RELEVANT)) {
                            result = true;
                        }
                        dataModelProperties.setFieldValueAsBoolean(FIELD_DMA_AS_RELEVANT, false, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
        return result;
    }

    private List<iPartsDataModelProperties> getConstModelKEMVariantList(iPartsModelId asModelId, iPartsModelPropertiesId modelPropertiesId) {
        List<iPartsDataModelProperties> result = new DwList<>();
        if (modelPropertiesId != null) {
            for (iPartsDataModelProperties dataModelProperties : constModelList) {
                if (dataModelProperties.getAsId().getModelNumber().startsWith(asModelId.getModelNumber())) {
                    result.add(dataModelProperties);
                }
            }
        }
        return result;
    }

    private iPartsDataModelProperties getConnectedConstModel(iPartsModelPropertiesId modelPropertiesId) {
        if ((modelPropertiesId != null) && modelPropertiesId.isValidId()) {
            for (iPartsDataModelProperties dataModelProperties : constModelList) {
                if (dataModelProperties.getAsId().equals(modelPropertiesId)) {
                    return dataModelProperties;
                }
            }
        }
        return null;
    }

    private int getConnectedConstModelRowNo(iPartsModelPropertiesId modelPropertiesId) {
        int rowNo = 0;
        if ((modelPropertiesId != null) && modelPropertiesId.isValidId()) {
            for (EtkDataObject dataObject : getCompleteAvailableList()) {
                if (dataObject.getAsId().equals(modelPropertiesId)) {
                    return rowNo;
                }
                rowNo++;
            }
        }
        return -1;
    }

    private List<Integer> getConstModelKEMVariantListRowNos(iPartsModelId asModelId) {
        List<Integer> result = new DwList<>();
        int rowNo = 0;
        for (EtkDataObject dataObject : getCompleteAvailableList()) {
            if (dataObject instanceof iPartsDataModelProperties) {
                iPartsDataModelProperties dataModelProperties = (iPartsDataModelProperties)dataObject;
                if (dataModelProperties.getAsId().getModelNumber().startsWith(asModelId.getModelNumber())) {
                    result.add(rowNo);
                }
            }
            rowNo++;
        }
        return result;
    }

    /**
     * Liefert den Index des Konstruktionsbaumusters basierend auf der Referenz zum Konstruktionsbaumuster am AS-Baumuster
     *
     * @param modelPropertiesId
     * @return
     */
    private int getASModelRowNoForConstId(iPartsModelPropertiesId modelPropertiesId) {
        int rowNo = 0;
        if ((modelPropertiesId != null) && modelPropertiesId.isValidId()) {
            for (EtkDataObject dataModel : getCompleteSelectedList()) {
                if ((dataModel instanceof iPartsDataModel) && ((iPartsDataModel)dataModel).getRelatedConstructionId().equals(modelPropertiesId)) {
                    return rowNo;
                }
                rowNo++;
            }
        }
        return -1;
    }

    private iPartsDataModel findConnectedAsModel(iPartsDataModelProperties sourceConstructionModel) {
        iPartsModelPropertiesId searchDataModelId = sourceConstructionModel.getAsId();
        String searchModelNo = searchDataModelId.getModelNumber();
        for (EtkDataObject dataObject : getCompleteSelectedList()) {
            if (dataObject instanceof iPartsDataModel) {
                iPartsDataModel dataModel = (iPartsDataModel)dataObject;
                iPartsModelPropertiesId relatedDataModelId = dataModel.getRelatedConstructionId();
                if (relatedDataModelId.isValidId()) {
                    // überprüfe relatedId
                    if (relatedDataModelId.equals(searchDataModelId) || relatedDataModelId.getModelNumber().equals(searchModelNo)) {
                        return dataModel;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extrahiert aus dem Konstruktionsbaumuster das AS-Baumuster und sucht mit Hilfe der Baumusternummer nach dem
     * AS-Baumuster im rechten Grid (AS-Baumuster-Grid).
     *
     * @param extractedASModelId
     * @return
     */
    private iPartsDataModel findASModel(iPartsModelId extractedASModelId) {
        for (EtkDataObject dataObject : getCompleteSelectedList()) {
            if (dataObject instanceof iPartsDataModel) {
                // prüfe AS-BM-Nummer mit konstruktiver, konvertierter BM-Nummer
                if (((iPartsDataModel)dataObject).getAsId().getModelNumber().equals(extractedASModelId.getModelNumber())) {
                    return (iPartsDataModel)dataObject;
                }
            }
        }
        return null;
    }

    private void refreshAvailableGrid() {
        int[] selection = availableEntriesGrid.getTable().getSelectedRowIndices();
        fillAvailableModelsList(constModelList.getAsList());
        availableEntriesGrid.getTable().setSelectedRows(selection, false, true);
    }

    private void refreshSelectedGrid(List<EtkDataObject> selectedList) {
        int[] selection = selectedEntriesGrid.getTable().getSelectedRowIndices();
        initSelectedEntries(selectedList);
        selectedEntriesGrid.getTable().setSelectedRows(selection, false, true);
    }

    @Override
    protected EtkDisplayFields buildDisplayFields(boolean forSelectedEntries) {
        EtkDisplayFields displayFields = super.buildDisplayFields(forSelectedEntries);
        checkDisplayFields(displayFields, forSelectedEntries);
        return displayFields;
    }

    /**
     * überprüft DisplayFields, ob bestimmte Felder vorhanden sind und fügt sie ggf hinzu
     *
     * @param displayFields
     * @param forSelectedEntries
     */
    protected void checkDisplayFields(EtkDisplayFields displayFields, boolean forSelectedEntries) {
        String tableName;
        String[] mandatoryFields;
        if (forSelectedEntries) {
            // überprüfe DA_MODEL-Data
            tableName = TABLE_DA_MODEL;
            mandatoryFields = MANDATORY_AS_DISPLAY_FIELDS;
        } else {
            // überprüfe DA_MODEL_PROPERTIES-Data
            tableName = searchTable;
            mandatoryFields = MANDATORY_CONST_DISPLAY_FIELDS;
        }
        EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(tableName);
        for (String fieldName : mandatoryFields) {
            EtkDisplayField displayField = displayFields.getFeldByName(tableName, fieldName);
            if (displayField == null) {
                displayField = createDisplayField(tableDef, tableName, fieldName);
                displayField.loadStandards(getConfig());
                displayFields.addFeld(displayField);
            }
        }
    }

    @Override
    protected EtkDisplayFields buildDefaultDisplayFields(boolean forSelectedEntries) {
        if (forSelectedEntries) {
            // Default DisplayFields für DA_MODEL-Data
            EtkDisplayFields displayFields = new EtkDisplayFields();
            EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(TABLE_DA_MODEL);
            displayFields.addFeld(createDisplayField(tableDef, TABLE_DA_MODEL, FIELD_DM_MODEL_NO));
            displayFields.addFeld(createDisplayField(tableDef, TABLE_DA_MODEL, FIELD_DM_SERIES_NO));
            displayFields.addFeld(createDisplayField(tableDef, TABLE_DA_MODEL, FIELD_DM_SALES_TITLE));
            displayFields.addFeld(createDisplayField(tableDef, TABLE_DA_MODEL, FIELD_DM_DEVELOPMENT_TITLE));
            displayFields.addFeld(createDisplayField(tableDef, TABLE_DA_MODEL, FIELD_DM_CODE));
            displayFields.addFeld(createDisplayField(tableDef, TABLE_DA_MODEL, FIELD_DM_MODEL_VISIBLE));
            displayFields.addFeld(createDisplayField(tableDef, TABLE_DA_MODEL, FIELD_DM_MODEL_TYPE));
            displayFields.addFeld(createDisplayField(tableDef, TABLE_DA_MODEL, FIELD_DM_AA));
            displayFields.addFeld(createDisplayField(tableDef, TABLE_DA_MODEL, FIELD_DM_STEERING));
            displayFields.loadStandards(getConfig());
            return displayFields;
        } else {
            // Default DisplayFields für DA_MODEL_PROPERTIES-Data
            EtkDisplayFields displayFields = new EtkDisplayFields();
            EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(searchTable);
            displayFields.addFeld(createDisplayField(tableDef, FIELD_DMA_MODEL_NO));
            displayFields.addFeld(createDisplayField(tableDef, FIELD_DMA_SERIES_NO));
            displayFields.addFeld(createDisplayField(tableDef, FIELD_DMA_DATA));
            displayFields.addFeld(createDisplayField(tableDef, FIELD_DMA_DATB));
            displayFields.addFeld(createDisplayField(tableDef, FIELD_DMA_CODE));
            displayFields.addFeld(createDisplayField(tableDef, FIELD_DMA_AA));
            displayFields.addFeld(createDisplayField(tableDef, FIELD_DMA_STEERING));
            displayFields.loadStandards(getConfig());
            return displayFields;
        }
    }

    @Override
    public void fillAvailableModelsList(String partialModelNumberWithWildCard) {
        setProductId(null);
        if (StrUtils.isValid(partialModelNumberWithWildCard)) {
            String seriesNo = StrUtils.replaceSubstring(partialModelNumberWithWildCard, "*", "");
            if (constModelList.isEmpty()) {
                constModelList =
                        iPartsDataModelPropertiesList.loadDataModelPropertiesListForSeries(getProject(),
                                                                                           seriesNo,
                                                                                           DBDataObjectList.LoadType.COMPLETE);
            } else {
                if (!constModelListContains(seriesNo)) {
                    iPartsDataModelPropertiesList extraConstModelList =
                            iPartsDataModelPropertiesList.loadDataModelPropertiesListForSeries(getProject(),
                                                                                               seriesNo,
                                                                                               DBDataObjectList.LoadType.COMPLETE);
                    constModelList.addAll(extraConstModelList.getAsList(), DBActionOrigin.FROM_DB);

                }
            }
            fillAvailableModelsList(constModelList.getAsList());
        } else {
            fillAvailableModelsList(new DwList<iPartsDataModelProperties>());
        }
    }

    private boolean constModelListContains(String seriesNo) {
        for (iPartsDataModelProperties dataModelProperties : constModelList) {
            if (dataModelProperties.getFieldValue(FIELD_DMA_SERIES_NO).equals(seriesNo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void fillAvailableModelsList(Collection<String> modelList) {

    }

    public void fillAvailableModelsList(List<iPartsDataModelProperties> modelList) {
        List<EtkDataObject> availableList = new DwList<>();
        // Sortierung merken
        int oldSortColumn = availableEntriesGrid.getSortColumnOfTable();
        boolean isSortAscending = availableEntriesGrid.isSortingOfTableAscending();
        if (oldSortColumn < 0) {
            Collections.sort(modelList, new Comparator<iPartsDataModelProperties>() {
                @Override
                public int compare(iPartsDataModelProperties o1, iPartsDataModelProperties o2) {
                    String m1 = o1.getAsId().getModelNumber();
                    String m2 = o2.getAsId().getModelNumber();
                    int result = m1.compareTo(m2);
                    if (result == 0) {
                        String sDatA1 = o1.getFieldValue(iPartsConst.FIELD_DMA_DATA);
                        String sDatA2 = o2.getFieldValue(iPartsConst.FIELD_DMA_DATA);
                        result = sDatA2.compareTo(sDatA1);
                    }
                    return result;
                }
            });
        }
        availableList.addAll(modelList);

        // Berechnung für das virtuelle Feld das anzeigt ob das Konstruktionsbaumuster schon über ein zugehöriges
        // AS-Baumuster diesem Produkt zugeordnet ist
        List<IdWithType> selectedIdList = getCompleteSelectedIdList();
        if (isASModelInProductFieldVisible() && !selectedIdList.isEmpty()) {
            List<String> asModelNumbers = new DwList<>();
            for (IdWithType idWithType : selectedIdList) {
                if (idWithType instanceof iPartsModelId) {
                    asModelNumbers.add(((iPartsModelId)(idWithType)).getModelNumber());
                }
            }

            for (iPartsDataModelProperties modelProperties : modelList) {
                DBDataObjectAttribute assignedAttribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DMA_AS_IN_PRODUCT, DBDataObjectAttribute.TYPE.STRING, true);
                modelProperties.getAttributes().addField(assignedAttribute, DBActionOrigin.FROM_DB);
                String afterSalesModelNumber = DIALOGModelsHelper.getAfterSalesModelNumber(modelProperties.getAsId());
                assignedAttribute.setValueAsBoolean(asModelNumbers.contains(afterSalesModelNumber), DBActionOrigin.FROM_DB);

            }
        }

        List<Integer> markedList = null;
        if (markLines && availableEntriesGrid.isSomethingMarked()) {
            markedList = availableEntriesGrid.getMarkedRowIndexList();
        }
        fillAvailableEntries(availableList);

        //Hintergrund Farbe alternierend nach After-Sales-Baumuster setzen
        Color back1 = Colors.clDesignTableContentBackground.getColor();
        Color back2 = Colors.clDesignTableContentBackgroundAlternating.getColor();
        Color currentBackground = back2;
        String afterSalesModelNumber = "";
        for (int rowIndex = 0; rowIndex < availableEntriesGrid.getTable().getRowCount(); rowIndex++) {
            EtkDataObject dataObject = availableEntriesGrid.getDataObjectForRowAndTable(rowIndex, TABLE_DA_MODEL_PROPERTIES);
            if (dataObject instanceof iPartsDataModelProperties) {
                iPartsDataModelProperties modelProperties = (iPartsDataModelProperties)(dataObject);
                String currentAfterSalesModelNumber = DIALOGModelsHelper.getAfterSalesModelNumber(modelProperties.getAsId());
                if (!afterSalesModelNumber.equals(currentAfterSalesModelNumber)) {
                    if (currentBackground.equals(back1)) {
                        currentBackground = back2;
                    } else {
                        currentBackground = back1;
                    }
                    afterSalesModelNumber = currentAfterSalesModelNumber;
                }
                availableEntriesGrid.getTable().getRow(rowIndex).setBackgroundColor(currentBackground);
            }
        }

        if (markedList != null) {
            availableEntriesGrid.setMarkedRows(markedList, false);
        }

        // Bei leerer Liste und vorhandenem Suchtext für die Baumuster das Label für keine Baumuster anzeigen
        if (availableList.isEmpty() && !getModelNumberSearchText().isEmpty()) {
            availableEntriesGrid.showNoResultsLabel(true);
        }

        // Sortierung wieder herstellen
        availableEntriesGrid.sortTableAfterColumn(oldSortColumn, isSortAscending);

        doEnableOKButton();
    }

    private boolean isASModelInProductFieldVisible() {
        EtkDisplayFields displayFields = availableEntriesGrid.getDisplayFields();
        if (displayFields != null) {
            // Berechnung nur durchführen wenn das Feld sichtbar ist, und die Baumuster zum Produkt zuvor schon bestimmt wurden
            EtkDisplayField fieldAsInProduct = displayFields.getFeldByName(iPartsConst.TABLE_DA_MODEL_PROPERTIES, iPartsDataVirtualFieldsDefinition.DMA_AS_IN_PRODUCT, false);
            return (fieldAsInProduct != null) && fieldAsInProduct.isVisible();
        }
        return false;
    }
}
