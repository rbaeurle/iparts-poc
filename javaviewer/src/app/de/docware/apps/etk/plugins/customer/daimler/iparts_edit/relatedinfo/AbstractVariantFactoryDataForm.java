/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableFactoryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsEtkDataObjectFactoryDataInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.FactoryDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ResponseDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseDataWithSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.CopyAndPasteData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstraktes Formular für die Werkseinsatzdaten bei Varianten.
 */
public abstract class AbstractVariantFactoryDataForm extends AbstractRelatedInfoFactoryDataForm implements iPartsConst {

    private iPartsSeriesId filterSeriesId;
    private boolean retailFilter;
    protected List<iPartsEtkDataObjectFactoryDataInterface> listOfUnfilteredFactoryData = new ArrayList<>();

    public AbstractVariantFactoryDataForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          String configKeyTop, String configKeyBottom, EtkDataPartListEntry partListEntry, iPartsSeriesId filterSeriesId,
                                          boolean enableEditMode, boolean retailFilter) {
        super(dataConnector, parentForm, configKeyTop, "!!Werkseinsatzdaten After-Sales:",
              configKeyBottom, "!!Werkseinsatzdaten Produktion:", true, enableEditMode);
        setPartListEntry(partListEntry);
        this.filterSeriesId = filterSeriesId;
        this.retailFilter = retailFilter;
        if (!enableEditMode) {
            setReadOnly(true);
        }

        // Bei den Werksdaten für Farbtabellen sollen die nicht relevanten Daten sichtbar gemacht werden können. Aber nur
        // im TU Edit
        boolean showHistoryVisible = isShowHistoryVisible();
        setCheckboxShowHistoryVisible(showHistoryVisible);
        if (retailFilter) {
            setGridTopTitle("!!Werkseinsatzdaten:");
            setGridBottomVisible(false);
            if (showHistoryVisible) {
                // Ist der Retailfilter gesetzt, darf die "Alle Stände" Checkbox nicht editierbar sein
                getCheckboxShowHistory().setEnabled(false);
            }
        }

    }

    private boolean isShowHistoryVisible() {
        // editMode kann nicht verwendet werden, weil false, wenn enableEditMode false ist (um das generelle Bearbeiten zu verhindern)
        return isRetailPartList && isEditContext(getConnector(), false);
    }

    @Override
    public String getSourceFieldName() {
        return FIELD_DCCF_SOURCE;
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(boolean top) {
        // DisplayFields sind für Werkseinsatzdaten After-Sales und Produktion identisch
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_FACTORY, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_FACTORY, iPartsDataVirtualFieldsDefinition.DCCF_FACTORY_SIGNS, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, iPartsDataVirtualFieldsDefinition.DCCF_PEMA_RESPONSE_DATA_AVAILABLE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMB, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTB, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, iPartsDataVirtualFieldsDefinition.DCCF_PEMB_RESPONSE_DATA_AVAILABLE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCB, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_SOURCE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STATUS, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    // diese Funktion wird nur für den Fall !retailFilter aufgerufen, d.h. hier werden nur die ungefilterten Daten bestimmt
    protected void addVirtualFieldsForResponseData(iPartsDataColorTableFactoryList dataColorTableFactoryList) {
        // Werte für virtuelle Felder "Response-Daten für PEM verfügbar" setzen
        for (iPartsDataColorTableFactory dataColorTableFactory : dataColorTableFactoryList) {
            boolean responseDataAvailable = false;
            String pem = dataColorTableFactory.getAttribute(iPartsConst.FIELD_DCCF_PEMA).getAsString();
            if (StrUtils.isValid(pem)) {
                List<iPartsResponseDataWithSpikes> responseDataForPEM = ResponseDataHelper.getResponseDataForPEM(pem, getProject());
                if (!responseDataForPEM.isEmpty()) {
                    responseDataForPemUnfiltered.put(pem, responseDataForPEM);
                    responseDataAvailable = true;
                }
            }
            dataColorTableFactory.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DCCF_PEMA_RESPONSE_DATA_AVAILABLE,
                                                           SQLStringConvert.booleanToPPString(responseDataAvailable), true,
                                                           DBActionOrigin.FROM_DB);

            pem = dataColorTableFactory.getAttribute(iPartsConst.FIELD_DCCF_PEMB).getAsString();
            responseDataAvailable = false;
            if (StrUtils.isValid(pem)) {
                List<iPartsResponseDataWithSpikes> responseDataForPEM = ResponseDataHelper.getResponseDataForPEM(pem, getProject());
                if (!responseDataForPEM.isEmpty()) {
                    responseDataForPemUnfiltered.put(pem, responseDataForPEM);
                    responseDataAvailable = true;
                }
            }
            dataColorTableFactory.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DCCF_PEMB_RESPONSE_DATA_AVAILABLE,
                                                           SQLStringConvert.booleanToPPString(responseDataAvailable), true,
                                                           DBActionOrigin.FROM_DB);
        }
    }

    @Override
    protected String getFactoryDataTableName() {
        return iPartsConst.TABLE_DA_COLORTABLE_FACTORY;
    }

    @Override
    protected String getFactoryFieldName() {
        return iPartsConst.FIELD_DCCF_FACTORY;
    }

    @Override
    protected String getPemFromFieldName() {
        return iPartsConst.FIELD_DCCF_PEMA;
    }

    @Override
    protected String getPemToFieldName() {
        return iPartsConst.FIELD_DCCF_PEMB;
    }

    @Override
    protected String getSeriesNoFieldName() {
        return null;
    }

    @Override
    protected String getAAFieldName() {
        return null;
    }

    @Override
    protected boolean isEditable() {
        return false;
    }

    @Override
    protected boolean isELDASPartList() {
        return false;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public iPartsSeriesId getFilterSeriesId() {
        return filterSeriesId;
    }

    @Override
    public boolean isRetailFilter() {
        return retailFilter;
    }

    /**
     * Liefert den {@link EditFormIConnector} über den {@link iPartsRelatedInfoEditContext} der OwnerAssembly
     *
     * @return
     */
    public EditFormIConnector getEditModuleFormConnectorFromEditContext() {
        RelatedInfoEditContext editContextFromOwnerConnector = getEditContextFromOwnerConnector();
        if ((editContextFromOwnerConnector != null) && (editContextFromOwnerConnector instanceof iPartsRelatedInfoEditContext)) {
            return ((iPartsRelatedInfoEditContext)editContextFromOwnerConnector).getEditFormConnector();
        }

        return null;
    }

    public RelatedInfoEditContext getEditContextFromOwnerConnector() {
        AbstractJavaViewerFormIConnector connector = getConnector().getOwnerConnector();
        if (connector instanceof RelatedInfoBaseFormIConnector) {
            RelatedInfoFormConnector relatedInfoFormConnector = (RelatedInfoFormConnector)connector;
            return relatedInfoFormConnector.getEditContext();
        }
        return null;
    }

    /**
     * Liefert basierend auf der aktuellen Selektion im Grid das {@link iPartsDataColorTableFactory} Objekt
     *
     * @param top
     * @return
     */
    protected iPartsDataColorTableFactory getColorTableFactoryFromSelection(boolean top) {
        return getSelection(top, iPartsDataColorTableFactory.class);
    }

    protected EtkDataObjectList<iPartsDataColorTableFactory> getColorTableFactoryFromMultiSelection(boolean top) {
        return getMultiSelection(top, iPartsDataColorTableFactory.class);
    }

    @Override
    protected void reloadEditableDataAndUpdateEditContext() {
        EditFormIConnector connector = getEditModuleFormConnectorFromEditContext();
        if (connector != null) {
            EtkDataAssembly ownerAssembly = connector.getCurrentAssembly();
            if (ownerAssembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsOwnerAssembly = (iPartsDataAssembly)ownerAssembly;

                // Werkseinsatzdaten für eine Farbtabelle oder deren Inhalt wurden verändert -> komplett neu laden.
                // Dazu werden zuerst die gefilterten Farbtabellen gelöscht und danach die Retail-Farbtabellen neu geladen.
                // Dadurch wird beim nächsten Zugriff die Filterung erneut durchgeführt.
                iPartsOwnerAssembly.clearAllColortableDataForRetailFilteredForPartList();
                iPartsOwnerAssembly.loadAllColorTableForRetailForPartList(iPartsOwnerAssembly.getPartListUnfiltered(null));

                // Hier muss der EditContext des OwnerConnectors genommen werden
                RelatedInfoEditContext editContext = getEditContextFromOwnerConnector();
                if ((editContext != null) && (editContext instanceof iPartsRelatedInfoEditContext)) {
                    iPartsRelatedInfoEditContext iPartsEditContext = ((iPartsRelatedInfoEditContext)editContext);
                    iPartsEditContext.setFireDataChangedEvent(true);
                    iPartsEditContext.setUpdateRetailColortableData(true);
                    iPartsEditContext.setUpdateEditAssemblyData(true);
                }
            }
        }
    }

    @Override
    protected void doDelete(boolean top) {
        doDeleteDataObjects(top, iPartsDataColorTableFactory.class);
    }

    @Override
    protected void statusChangedForGrid(boolean top) {
        // Selektion muss VOR doSaveDataObjects() ausgelesen werden, weil diese durch das Update ansonsten verloren geht
        EtkDataObjectList<iPartsDataColorTableFactory> multiSelection = getColorTableFactoryFromMultiSelection(top);
        statusChangedForDataObjects(multiSelection, top);
    }

    protected void statusChangedForDataObject(iPartsDataColorTableFactory factoryData, boolean top) {
        EtkDataObjectList<iPartsDataColorTableFactory> list = new iPartsDataColorTableFactoryList();
        list.add(factoryData, DBActionOrigin.FROM_EDIT);
        statusChangedForDataObjects(list, top);
    }

    protected void statusChangedForDataObjects(EtkDataObjectList<iPartsDataColorTableFactory> factoryDataList, boolean top) {
        List<iPartsDataColorTableFactory> allFactoryDataObjectsOfGrid = getDataObjectList(top, iPartsDataColorTableFactory.class);
        EtkDataObjectList<iPartsDataColorTableFactory> changedDataObjects = iPartsRelatedEditHelper.updateStatusValuesVariants(factoryDataList,
                                                                                                                               allFactoryDataObjectsOfGrid,
                                                                                                                               iPartsConst.FIELD_DCCF_ADAT,
                                                                                                                               getStatusFieldName(), !top);

        if (saveDataObjectsWithUpdate(changedDataObjects)) {
            iPartsRelatedEditHelper.statusChanged(changedDataObjects, this, false);
        }
    }

    @Override
    protected String getStatusFieldName() {
        return iPartsConst.FIELD_DCCF_STATUS;
    }


    @Override
    public void setModifiedByEdit(boolean modifiedByEdit) {
        if (parentForm instanceof iPartsRelatedInfoVariantsToPartDataForm) {
            ((iPartsRelatedInfoVariantsToPartDataForm)parentForm).setModifiedByEdit(modifiedByEdit);
        } else {
            super.setModifiedByEdit(modifiedByEdit);
        }
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        listOfUnfilteredFactoryData = new ArrayList<>();
        super.updateData(sender, forceUpdateAll);
    }

    /**
     * Filtert und sortiert die Werksdaten. Im TU Edit werden abhängig von der Einstellung "nicht relevante" Daten
     * gefiltert.
     *
     * @param factoryDataList
     */
    protected void filterFactoryData(iPartsDataColorTableFactoryList factoryDataList) {
        if (!isShowHistoryVisible() || isShowHistory()) {
            ColorTableHelper.filterAndSortColorFactoryData(factoryDataList);
        } else {
            ColorTableHelper.filterAndSortOnlyAssemblyRelevantColorFactoryData(factoryDataList, getPartListEntry().getOwnerAssembly());
        }
    }

    @Override
    protected void checkboxShowHistoryClicked(Event event) {
        // Grids leeren und neu setzen. Durch die Änderung der Checkbox werden die Daten beim Laden gefiltert
        // Nur relevant beim TU Edit
        if (isShowHistoryVisible()) {
            dataToGrid();
        }
    }

    protected boolean copyFactoryData(boolean withUpdate) {
        List<? extends iPartsEtkDataObjectFactoryDataInterface> filteredData = FactoryDataHelper.getDataForCopy(listOfUnfilteredFactoryData);

        if (filteredData.isEmpty()) {
            if (withUpdate) {
                MessageDialog.show("!!Die Auswahl enthält keine relevanten Werksdaten zum Kopieren.",
                                   EditToolbarButtonAlias.EDIT_COPY_FACTORY_DATA.getTooltip());
            }
            return false;
        } else {
            CopyAndPasteData.copyFactoryDataOfColortable(filteredData);
            if (withUpdate) {
                toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_FACTORY_DATA, !iPartsLockEntryHelper.isLockedWithDBCheck(partListEntry));
            }
            return true;
        }
    }

    @Override
    protected void enableButtonsAndMenu() {
        super.enableButtonsAndMenu();
        // Copy/ Paste Buttons behandeln
        if (toolbarHelperTop != null) {
            boolean copyButtonActive = false;
            if (editMode && !isReadOnly) {
                copyButtonActive = !listOfUnfilteredFactoryData.isEmpty();
            } else {
                if (!isRetailPartList) {
                    // in der Konstruktion soll der Copy Button aktiv sein wenn ein Changeset aktiv ist
                    EtkRevisionsHelper revisionsHelper = getProject().getRevisionsHelper();
                    if (revisionsHelper != null) {
                        if (revisionsHelper.getActiveRevisionChangeSetForEdit() != null) {
                            copyButtonActive = !listOfUnfilteredFactoryData.isEmpty();
                        }
                    }
                }
            }
            // Copy nur enablen:
            // Konstruktion mit aktivem Autorenauftrag und Werksdaten vorhanden
            // TU bearbeiten und Werksdaten vorhanden
            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_COPY_FACTORY_DATA, copyButtonActive);

            // Paste erst enablen, wenn die Position nicht gesperrt ist und etwas im Zwischenspeicher liegt
            boolean isCopyCacheFilled = CopyAndPasteData.isCopyCacheFilled();
            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_FACTORY_DATA, (editMode && !isReadOnly && isCopyCacheFilled));
        }

    }
}