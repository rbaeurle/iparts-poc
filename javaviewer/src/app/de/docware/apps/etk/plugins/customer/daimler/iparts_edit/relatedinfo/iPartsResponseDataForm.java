/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.FactoryDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PEMDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ResponseDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseDataWithSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.AbstractTwoDataObjectGridsEditForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.DataObjectFilterGridWithStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControlsForResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.toolbar.AbstractGuiToolComponent;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Anzeige Rückmeldedaten
 */
public class iPartsResponseDataForm extends AbstractTwoDataObjectGridsEditForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_RESPONSE_SPIKES = "iPartsMenuItemShowResponseSpikes";
    public static final String CONFIG_KEY_RESPONSE_DATA_TOP = "Plugin/iPartsEdit/ResponseDataTop";
    public static final String CONFIG_KEY_RESPONSE_DATA_BOTTOM = "Plugin/iPartsEdit/ResponseDataBottom";

    public static final String AFTER_SALES = "!!After-Sales";
    public static final String PRODUCTION = "!!Produktion";

    private String pem;
    private boolean isPemFrom;
    private boolean retailFilter;
    private EtkDataPartListEntry partListEntry;
    private String factory; // Das nicht-AS Werk von dem die Daten abgerufen werden sollen (wird für korrekte Ermittlung der Ausreißer benötigt)
    private String seriesNo;
    private String aa;
    private boolean isEldasPartlist;
    private Set<String> allWMIs; // für ELDAS EditUserControls alle möglichen WMIs zum Werk

    // Liste der Rückmeldedaten inklusive Ausreißern. Wird im Parent Dialog bestimmt
    private List<iPartsResponseDataWithSpikes> responseDataList;
    private iPartsDataResponseDataList responseDataListAS;    // Rückmeldedaten (After-Sales)
    private iPartsDataResponseDataList responseDataListNonAS; // Rückmeldedaten (Produktion)

    public static GuiMenuItem createResponseSpikesMenuItem(final ResponseSpikesCallback callback, final RelatedInfoBaseForm parentForm) {
        GuiMenuItem menuItem = new GuiMenuItem();
        menuItem.setUserObject(IPARTS_MENU_ITEM_SHOW_RESPONSE_SPIKES);
        menuItem.setName(IPARTS_MENU_ITEM_SHOW_RESPONSE_SPIKES);
        menuItem.setText(TranslationHandler.translate(iPartsConst.RELATED_INFO_RESPONSE_SPIKES_TEXT));
        menuItem.setIcon(EditDefaultImages.edit_history.getImage());
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                ResponseSpikesQuery responseSpikesQuery = callback.getResponseSpikesQuery();
                iPartsResponseSpikesDataForm responseDataForm = new iPartsResponseSpikesDataForm(parentForm, responseSpikesQuery);
                responseDataForm.showModal();
                // damit die Werkseinsatzdaten inkl. der Rückmeldedaten für den Stücklisteneintrag bzw. die Variante oder Variantentabelle neu bestimmt werden
                parentForm.dataChanged();
            }
        });

        return menuItem;
    }

    public iPartsResponseDataForm(AbstractRelatedInfoFactoryDataForm parentForm, AbstractRelatedInfoFactoryDataForm.ResponseDataQuery responseDataQuery) {
        super(parentForm.getConnector(), parentForm, parentForm.getRelatedInfo(),
              CONFIG_KEY_RESPONSE_DATA_TOP + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              TranslationHandler.translate("!!Rückmeldedaten") + " " + TranslationHandler.translate(AFTER_SALES) + ":",
              CONFIG_KEY_RESPONSE_DATA_BOTTOM + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              TranslationHandler.translate("!!Rückmeldedaten") + " " + TranslationHandler.translate(PRODUCTION) + ":",
              responseDataQuery.isEditable());
        this.pem = responseDataQuery.getPem();
        this.isPemFrom = responseDataQuery.isPemFrom();
        this.partListEntry = responseDataQuery.getPartlistEntry();
        this.retailFilter = responseDataQuery.isRetailFilter();
        this.factory = responseDataQuery.getFactory();
        this.aa = responseDataQuery.getAa();
        this.seriesNo = responseDataQuery.getSeriesNo();
        this.isEldasPartlist = responseDataQuery.isEldasPartlist();

        String titel;
        if (isEldasPartlist) {
            titel = TranslationHandler.translate("!!PEM: %1", pem);
        } else {
            titel = TranslationHandler.translate("!!Werk: %1, PEM: %2", factory, pem);
        }
        setWindowTitle(RELATED_INFO_RESPONSE_DATA_TEXT, titel);
        // Rückmeldedaten zu Werksdaten deren Positionen gesperrt sind, dürfen nicht editiert werden
        setReadOnly(retailFilter || iPartsLockEntryHelper.isLockedWithDBCheck(partListEntry) || !(editMode && isEditContext(getConnector(), true)));
        scaleFromParentForm(getWindow());
        if (retailFilter) {
            setGridBottomVisible(false);
        }

        responseDataList = responseDataQuery.getResponseDataList();
        dataToGrid();
        enableButtonsAndMenu();

        allWMIs = new HashSet<>();
        allWMIs.add(""); // Der WHC/WMI darf auch leer sein. Sonst funktioniert die Endnummern Filterung bei Aggregaten nicht.
        if (isEldasPartlist && StrUtils.isValid(factory)) {
            // alle gültigen WMIs zum Werk bestimmen
            iPartsDataFactoryModelList factoryModelList = new iPartsDataFactoryModelList();
            factoryModelList.loadByFactory(getProject(), factory);
            for (iPartsDataFactoryModel factoryModel : factoryModelList) {
                allWMIs.add(factoryModel.getAsId().getWorldManufacturerIdentifier().toUpperCase());
            }
        }
    }

    @Override
    public boolean isRetailFilter() {
        return retailFilter;
    }

    @Override
    protected void doEnableButtonsAndMenu(boolean isTop) {
        super.doEnableButtonsAndMenu(isTop);

        EditToolbarButtonMenuHelper toolbarHelper = getToolbarHelper(isTop);
        if (toolbarHelper != null) {
            GuiMenuItem menuItem = toolbarHelper.findMenuItem(getGrid(isTop).getContextMenu(), EditToolbarButtonAlias.EDIT_WORK);
            AbstractGuiToolComponent editViewButton = toolbarHelper.getToolbarManager().getButton(EditToolbarButtonAlias.EDIT_WORK.getAlias());
            if (isUseReadOnlyControls(getSelection(isTop, iPartsDataResponseData.class))) {
                editViewButton.setTooltip("!!Anzeigen");
                menuItem.setText("!!Anzeigen");
            } else {
                editViewButton.setTooltip("!!Bearbeiten");
                menuItem.setText("!!Bearbeiten");
            }
        }
    }

    /**
     * Baumusterart vom Produkt zum Stücklisteneintrag bestimmen.
     * Wird benötigt um bei ELDAS Rückmeldedaten den DRD_TYPE vorzubelegen
     *
     * @return
     */
    private String getProductAggregateType() {
        iPartsDataAssembly ownerAssembly = (iPartsDataAssembly)partListEntry.getOwnerAssembly();
        if (ownerAssembly.getDocumentationType().isTruckDocumentationType()) {
            iPartsProductId productId = ownerAssembly.getProductIdFromModuleUsage();
            if (productId != null) {
                iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                return product.getAggregateType();
            }
        }
        return "";
    }

    @Override
    protected void doNew(boolean top) {
        String productAggregateType = getProductAggregateType();

        iPartsResponseDataId responseDataId = new iPartsResponseDataId(factory, seriesNo, aa, "", pem, "", "", true);
        iPartsDataResponseData result = EditUserControlsForResponseData.showEditCreateResponseData(getConnector(), this,
                                                                                                   responseDataId, factory, true, isReadOnly,
                                                                                                   isEldasPartlist, allWMIs, productAggregateType);
        if (result != null) {
            saveDataObjectWithUpdate(result);
        }
    }

    @Override
    protected void doEdit(boolean top) {
        iPartsDataResponseData selectedResponseData = getSelection(top, iPartsDataResponseData.class);
        if (selectedResponseData != null) {
            boolean useReadOnlyControls = isUseReadOnlyControls(selectedResponseData);
            String productAggregateType = getProductAggregateType();
            iPartsDataResponseData result = EditUserControlsForResponseData.showEditCreateResponseData(getConnector(), this,
                                                                                                       selectedResponseData.getAsId(),
                                                                                                       factory, false, useReadOnlyControls,
                                                                                                       isEldasPartlist, allWMIs, productAggregateType);
            if (result != null) {
                saveDataObjectWithUpdate(result);
            }
        }
    }

    private boolean isUseReadOnlyControls(iPartsDataResponseData selectedResponseData) {
        boolean useReadOnlyControls = isReadOnly;
        if (!useReadOnlyControls) {
            if (!isEldasPartlist) {
                if ((selectedResponseData != null) && !PEMDataHelper.isIZVPem(selectedResponseData.getAsId().getPem())) {
                    useReadOnlyControls = true;
                }
            }
        }
        return useReadOnlyControls;
    }

    @Override
    protected void doDelete(boolean top) {
        doDeleteDataObjects(top, iPartsDataResponseData.class);
    }

    protected void statusChangedForGrid(boolean top) {
        // Selektion muss VOR doSaveDataObjects() ausgelesen werden, weil diese durch das Update ansonsten verlorengeht
        EtkDataObjectList<iPartsDataResponseData> multiSelection = getMultiSelection(top, iPartsDataResponseData.class);

        List<iPartsDataResponseData> allResponseDataObjects = getDataObjectList(top, iPartsDataResponseData.class);
        EtkDataObjectList<iPartsDataResponseData> changedDataObjects = iPartsRelatedEditHelper.updateStatusValuesResponseAndSpikes(multiSelection,
                                                                                                                                   allResponseDataObjects,
                                                                                                                                   FIELD_DRD_ADAT,
                                                                                                                                   getStatusFieldName());

        if (saveDataObjectsWithUpdate(changedDataObjects)) {
            iPartsRelatedEditHelper.statusChanged(changedDataObjects, this, false);
        }
    }

    @Override
    public List<iPartsDataDIALOGChange> getDataDIALOGChanges(EtkDataObject dataObject) {
        final List<iPartsDataDIALOGChange> result = new DwList<>();
        if (isRelatedInfoEditContext() && (dataObject instanceof iPartsDataResponseData)) {
            iPartsDataResponseData responseData = (iPartsDataResponseData)dataObject;
            final iPartsResponseDataId responseDataId = responseData.getAsId();

            Runnable searchDIALOGChangesRunnable = new Runnable() {
                @Override
                public void run() {
                    String aa = responseDataId.getAusfuehrungsArt();
                    if (!StrUtils.isValid(aa)) {
                        aa = null;
                    }

                    // Bedingung: DDC_DO_TYPE && DFD_FACTORY && DFD_SERIES_NO [&& DFD_AA] && (DFD_PEMA || DFD_PEMB)
                    int aaOffset = (aa != null) ? 1 : 0;
                    String[][] whereFields = createWhereFields(aaOffset);
                    String[][] whereValues = createWhereValues(aaOffset, aa, responseDataId);

                    EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
                        @Override
                        public boolean foundAttributes(DBDataObjectAttributes attributes) {
                            // iPartsDataDIALOGChange aus den Attributen erzeugen und zu den Ergebnissen hinzufügen
                            iPartsDialogChangesId dialogChangesId = new iPartsDialogChangesId(iPartsDataDIALOGChange.ChangeType.RESPONSE_DATA,
                                                                                              responseDataId, responseDataId.getSeriesNo(),
                                                                                              attributes.getFieldValue(FIELD_DDC_BCTE),
                                                                                              "", "");
                            iPartsDataDIALOGChange dataDIALOGChange = new iPartsDataDIALOGChange(getProject(), dialogChangesId);
                            dataDIALOGChange.assignAttributes(getProject(), attributes, true, DBActionOrigin.FROM_DB);
                            result.add(dataDIALOGChange);

                            return false;
                        }
                    };

                    iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
                    dataDIALOGChangeList.searchSortAndFillWithJoin(getProject(), null, null, whereFields, whereValues,
                                                                   false, null, null, false, null, false, false, false,
                                                                   foundAttributesCallback,
                                                                   false, new EtkDataObjectList.JoinData(TABLE_DA_FACTORY_DATA,
                                                                                                         new String[]{ FIELD_DDC_BCTE },
                                                                                                         new String[]{ FIELD_DFD_GUID },
                                                                                                         false, false));
                }

                private String[][] createWhereFields(int aaOffset) {
                    String[][] whereFields = new String[4 + aaOffset][];
                    EtkDataObjectList.addElemsTo2dArray(whereFields, 0, TableAndFieldName.make(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_DO_TYPE));
                    EtkDataObjectList.addElemsTo2dArray(whereFields, 1, TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_FACTORY));
                    EtkDataObjectList.addElemsTo2dArray(whereFields, 2, TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_SERIES_NO));
                    if (aaOffset > 0) {
                        EtkDataObjectList.addElemsTo2dArray(whereFields, 3, TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_AA));
                    }
                    EtkDataObjectList.addElemsTo2dArray(whereFields, 3 + aaOffset,
                                                        TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMA),
                                                        TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMB));
                    return whereFields;
                }

                private String[][] createWhereValues(int aaOffset, String aa, iPartsResponseDataId responseDataId) {
                    String[][] whereValues = new String[4 + aaOffset][];
                    EtkDataObjectList.addElemsTo2dArray(whereValues, 0, iPartsDataDIALOGChange.ChangeType.RESPONSE_DATA.getDbKey());
                    EtkDataObjectList.addElemsTo2dArray(whereValues, 1, responseDataId.getFactory());
                    EtkDataObjectList.addElemsTo2dArray(whereValues, 2, responseDataId.getSeriesNo());
                    if (aa != null) {
                        EtkDataObjectList.addElemsTo2dArray(whereValues, 3, aa);
                    }
                    EtkDataObjectList.addElemsTo2dArray(whereValues, 3 + aaOffset, responseDataId.getPem(), responseDataId.getPem());
                    return whereValues;
                }
            };

            EtkRevisionsHelper revisionsHelper = getProject().getRevisionsHelper();
            if (revisionsHelper != null) {
                revisionsHelper.executeWithoutActiveChangeSets(searchDIALOGChangesRunnable, false, getProject());
            } else {
                searchDIALOGChangesRunnable.run();
            }
        }

        return result;
    }

    @Override
    protected String getStatusFieldName() {
        return iPartsConst.FIELD_DRD_STATUS;
    }

    @Override
    protected String getTableName() {
        return TABLE_DA_RESPONSE_DATA;
    }

    @Override
    public String getSourceFieldName() {
        return iPartsConst.FIELD_DRD_SOURCE;
    }

    @Override
    protected void reloadEditableDataAndUpdateEditContext() {
        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            iPartsRelatedInfoEditContext editContext = ((iPartsRelatedInfoEditContext)getConnector().getEditContext());
            editContext.setFireDataChangedEvent(true);
            editContext.setUpdateRetailFactoryData(true);
            editContext.setUpdateResponseData(true);
            editContext.setUpdateEditAssemblyData(true);
            editContext.setUpdateRetailColortableData(true);
        }
    }

    @Override
    public void setModifiedByEdit(boolean modifiedByEdit) {
        if (parentForm instanceof RelatedInfoBaseForm) {
            ((RelatedInfoBaseForm)parentForm).setModifiedByEdit(modifiedByEdit);
        } else {
            super.setModifiedByEdit(modifiedByEdit);
        }
    }

    @Override
    protected boolean saveDataObjectsWithUpdate(EtkDataObjectList<? extends EtkDataObject> dataObjectList) {
        if (!dataObjectList.isModifiedWithChildren()) {
            return false;
        }

        // Veränderte Daten in den RelatedEditChangeset-Cache speichern (muss vor addDataObjectToActiveChangeSetForEdit()
        // gemacht werden, weil ansonsten die oldId in dataObjectList bereits zurückgesetzt wurde)
        iPartsResponseData responseDataCache = iPartsResponseData.getInstance(getProject());
        for (EtkDataObject dataObject : dataObjectList) {
            if (dataObject instanceof iPartsDataResponseData) {
                responseDataCache.updateCacheForPEM(pem, (iPartsDataResponseData)dataObject);
            }
        }
        for (EtkDataObject dataObject : dataObjectList.getDeletedList()) {
            if (dataObject instanceof iPartsDataResponseData) {
                responseDataCache.deleteCacheForPEM(pem, (iPartsDataResponseData)dataObject);
            }
        }

        if (partListEntry instanceof iPartsDataPartListEntry) {
            iPartsDataAssembly iPartsOwnerAssembly = (iPartsDataAssembly)partListEntry.getOwnerAssembly();
            DBDataObjectList<EtkDataPartListEntry> partListEntries = new DBDataObjectList<>();

            // Werkseinsatzdaten vom Original-Stücklisteneintrag aus der Edit-Stückliste neu laden (partListEntry
            // sollte bereits auf den Original-Stücklisteneintrag zeigen, aber sicher ist sicher, da EtkRelatedInfoData
            // auch eigene Stücklisteneinträge zurückliefern kann)
            partListEntry = iPartsOwnerAssembly.getPartListEntryFromKLfdNrUnfiltered(partListEntry.getAsId().getKLfdnr());
            partListEntries.add(partListEntry, DBActionOrigin.FROM_DB);

            // Dieser Aufruf (mit nur einem Element in der Liste) führt zu einer Sonderbehandlung in der Lade-Funktion,
            // wodurch hier keine Pseudo-Transaktionen gebraucht werden
            iPartsOwnerAssembly.loadAllFactoryDataForRetailForPartList(partListEntries);

            // nur genau die Farb-Werksdaten zur Bearbeiteten MaterialNr neu laden damit die Anzeige innerhalb des Dialogs passt
            // Die Gesamte Stückliste wird neu geladen wenn der Related Edit Dialog komplett verlassen wird
            iPartsOwnerAssembly.loadAllColorTableForRetailForPartList(partListEntries, partListEntry.getPart().getAsId().getMatNr());

            responseDataList = ResponseDataHelper.getResponseDataForPEM(pem, partListEntry, retailFilter, getProject(), isPemFrom);
        }
        return super.saveDataObjectsWithUpdate(dataObjectList);
    }

    /**
     * @param top aktuell keine Erkenntnisse über Unterschiede oben/unten
     * @return
     */
    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(boolean top) {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();

        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_SERIES_NO, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_AA, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_STEERING, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_BMAA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_IDENT, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_TEXT, true, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsDataVirtualFieldsDefinition.DRD_RESPONSE_SPIKES_AVAILABLE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_ADAT, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_AGG_TYPE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_SOURCE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_DATA, iPartsConst.FIELD_DRD_STATUS, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
        createResponseDataLists(retailFilter);

        // Für Retail müssen die Rückmeldedaten gefiltert sein und es sollen die AS-Daten und Produktions-Daten zusammen
        // in einem Grid angezeigt werden wie bei den Werkseinsatzdaten.
        if (top) {
            if (retailFilter) {
                setGridTopTitle(TranslationHandler.translate("!!Rückmeldedaten") + ":");
                addDataObjectListToGrid(top, responseDataListAS);

                // Produktions-Daten hinzufügen falls es nicht schon identische AS-Daten gibt
                for (iPartsDataResponseData responseDataNonAS : responseDataListNonAS) {
                    boolean foundInAS = false;
                    for (iPartsDataResponseData responseDataAS : responseDataListAS) {
                        if (responseDataAS.equalContent(responseDataNonAS, true)) {
                            foundInAS = true;
                            break;
                        }
                    }
                    if (!foundInAS) {
                        addDataObjectToGrid(top, responseDataNonAS);
                    }
                }
            } else {
                addDataObjectListToGrid(top, responseDataListAS);
            }
        } else {
            addDataObjectListToGrid(top, responseDataListNonAS);
        }
    }

    /**
     * Sortiert die Rückmeldedaten nach ID (ohne Adat) und filtert sie nach Adat und Status (pro ID). Abhängig vom Retailfilter
     * werden einzelne Datensätze ausgefiltert.
     *
     * @param responseDataList
     * @param retailFilter
     */
    private void checkStateOfResponseData(iPartsDataResponseDataList responseDataList, boolean retailFilter) {
        if (responseDataList == null) {
            return;
        }
        Map<iPartsResponseDataId, List<iPartsDataResponseData>> responseDataForFactory = new HashMap<>();
        // Datensätze werden nach ihrer ID gruppiert, weil die ADAT- und Statusprüfung auf Basis der "gleichen" Datensätze
        // geschehen muss. Weil das ADAT aber ein Bestandteil des Schlüssels ist, wird hier ein künstlicher Schlüssel
        // - ohne ADAT - erzeugt und als Gruppierungsschlüssel für die einzelnen Datensätze verwendet.
        for (iPartsDataResponseData responseData : responseDataList) {
            iPartsResponseDataId existingId = responseData.getAsId();
            iPartsResponseDataId idWithoutAdat = new iPartsResponseDataId(existingId.getFactory(), existingId.getSeriesNo(),
                                                                          existingId.getAusfuehrungsArt(), existingId.getBmaa(),
                                                                          existingId.getPem(), "", existingId.getIdent(),
                                                                          existingId.getAsData());
            List<iPartsDataResponseData> dataResponsesForIdWithoutAdat = responseDataForFactory.get(idWithoutAdat);
            if (dataResponsesForIdWithoutAdat == null) {
                dataResponsesForIdWithoutAdat = new ArrayList<>();
                responseDataForFactory.put(idWithoutAdat, dataResponsesForIdWithoutAdat);
            }
            dataResponsesForIdWithoutAdat.add(responseData);
        }
        responseDataList.clear(DBActionOrigin.FROM_DB);
        FactoryDataHelper.fillListWithFilteredFactoryData(responseDataList, responseDataForFactory.values(), retailFilter,
                                                          FIELD_DRD_STATUS, FIELD_DRD_ADAT);
    }

    /**
     * Rückmeldedaten in AS und Non-AS aufteilen
     *
     * @param retailFilter
     */
    private void createResponseDataLists(boolean retailFilter) {
        responseDataListAS = new iPartsDataResponseDataList();
        responseDataListNonAS = new iPartsDataResponseDataList();
        if (responseDataList != null) { // kommt aus dem Parent Dialog und enthält auch schon die Ausreißer
            EtkProject project = getProject();
            for (iPartsResponseDataWithSpikes responseDataWithSpikes : responseDataList) {
                iPartsDataResponseData responseData = responseDataWithSpikes.getResponseData().cloneMe(project);
                List<iPartsDataResponseSpike> responseSpikes = responseDataWithSpikes.getResponseSpikes();

                // Flag, ob Ausreißer vorhanden sind berechnen
                boolean responseSpikesAvailable = (responseSpikes != null) && !responseSpikes.isEmpty();
                responseData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DRD_RESPONSE_SPIKES_AVAILABLE,
                                                      SQLStringConvert.booleanToPPString(responseSpikesAvailable), true,
                                                      DBActionOrigin.FROM_DB);

                boolean isASData = responseData.getFieldValueAsBoolean(iPartsConst.FIELD_DRD_AS_DATA);
                if (isASData) {
                    responseDataListAS.add(responseData, DBActionOrigin.FROM_DB);
                } else {
                    responseDataListNonAS.add(responseData, DBActionOrigin.FROM_DB);
                }
            }
            checkStateOfResponseData(responseDataListAS, retailFilter);
            checkStateOfResponseData(responseDataListNonAS, retailFilter);
        }
    }

    @Override
    protected DataObjectGrid createGrid(final boolean top) {
        final VarParam<GuiMenuItem> responseSpikesMenuItem = new VarParam<GuiMenuItem>(null);
        final DataObjectFilterGridWithStatus dataGrid = new DataObjectFilterGridWithStatus(getConnector(), this, getTableName(), getStatusFieldName()) {
            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                super.createContextMenuItems(contextMenu);

                // Popup-Menüeintrag für Anzeige Ausreißer
                responseSpikesMenuItem.setValue(createResponseSpikesMenuItem(new ResponseSpikesCallback() {
                    @Override
                    public ResponseSpikesQuery getResponseSpikesQuery() {
                        List<EtkDataObject> selection = getSelection();
                        if (selection != null) {
                            EtkDataObject dataModel = selection.get(0);
                            String pem = dataModel.getFieldValue(iPartsConst.FIELD_DRD_PEM);
                            boolean isEditable = editMode;
                            return new ResponseSpikesQuery(pem,
                                                           dataModel.getFieldValue(iPartsConst.FIELD_DRD_IDENT),
                                                           factory,
                                                           dataModel.getFieldValue(iPartsConst.FIELD_DRD_SERIES_NO),
                                                           dataModel.getFieldValue(iPartsConst.FIELD_DRD_AA),
                                                           dataModel.getFieldValue(iPartsConst.FIELD_DRD_BMAA),
                                                           partListEntry, retailFilter, isEditable,
                                                           responseDataList, isPemFrom, isEldasPartlist);
                        } else {
                            return null;
                        }
                    }
                }, iPartsResponseDataForm.this));
                contextMenu.addChild(responseSpikesMenuItem.getValue());
            }

            @Override
            protected void statusChanged() {
                statusChangedForGrid(top);
            }
        };

        // Popup-Menüeintrag für Anzeige Ausreißer nach Selektion anpassen
        dataGrid.getTable().addEventListener(new EventListener(de.docware.framework.modules.gui.event.Event.TABLE_SELECTION_EVENT) {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                List<EtkDataObject> selection = dataGrid.getSelection();
                if (selection != null) {
                    EtkDataObject dataModel = selection.get(0);

                    // Enabled-Zustand anpassen
                    if (responseSpikesMenuItem.getValue() != null) {
                        String pemValue = dataModel.getFieldValue(iPartsConst.FIELD_DRD_PEM);
                        String identValue = dataModel.getFieldValue(iPartsConst.FIELD_DRD_IDENT);
                        List<iPartsDataResponseSpike> responseSpikesForPemAndIdent = ResponseDataHelper.
                                getResponseSpikesForPemAndIdent(responseDataList, pemValue, identValue);
                        boolean responseSpikesAvailable = (responseSpikesForPemAndIdent != null) && !responseSpikesForPemAndIdent.isEmpty();
                        responseSpikesMenuItem.getValue().setEnabled((editMode && !pemValue.isEmpty() && !retailFilter)
                                                                     || responseSpikesAvailable);
                    }
                }
            }
        });

        return dataGrid;
    }

    @Override
    protected boolean isContextMenuEntryForNewObjectVisible(boolean top) {
        return top; // Kein "Neu" im unteren Grid
    }

    @Override
    protected boolean isContextMenuEntryForDeleteObjectVisible(boolean top) {
        return top; // Kein "Löschen" im unteren Grid
    }

//    @Override
//    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
//        responseDataList = ResponseDataHelper.getResponseDataForPEM(pem, partListEntry, isRetailFilter(), getProject(), isPemFrom);
//        super.updateData(sender, forceUpdateAll);
//    }

    @Override
    public void dataChanged() {
        responseDataList = ResponseDataHelper.getResponseDataForPEM(pem, partListEntry, isRetailFilter(), getProject(), isPemFrom);
        super.dataChanged();
    }

    /**
     * Callback zur Bestimmung der Daten für die Anzeige der Rückmeldedaten-Ausreißer.
     */
    public interface ResponseSpikesCallback {

        ResponseSpikesQuery getResponseSpikesQuery();
    }


    /**
     * Daten die aus dem Dialog Rückmeldedaten an den Dialog Rückmeldedaten-Ausreißer übertragen werden sollen.
     */
    public static class ResponseSpikesQuery extends iPartsRelatedInfoFactoryDataForm.ResponseDataQuery {

        private String ident;
        private String bmaa;

        public ResponseSpikesQuery(String pem, String ident, String factory, String seriesNo, String aa, String bmaa,
                                   EtkDataPartListEntry partlistEntry, boolean retailFilter, boolean isEditable,
                                   List<iPartsResponseDataWithSpikes> responseData,
                                   boolean isPemFrom, boolean isEldasPartlist) {
            super(pem, factory, seriesNo, aa, partlistEntry, retailFilter, isEditable, responseData, isPemFrom);
            this.ident = ident;
            this.bmaa = bmaa;
            this.isEldasPartlist = isEldasPartlist;
        }

        public String getIdent() {
            return ident;
        }

        public String getBmaa() {
            return bmaa;
        }
    }
}