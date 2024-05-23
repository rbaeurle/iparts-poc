/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpike;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpikeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsResponseSpikeId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.FactoryDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ResponseDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts.images.iPartsDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseDataWithSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.AbstractTwoDataObjectGridsEditForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.DataObjectFilterGridWithStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControlsForResponseSpike;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.ArrayList;
import java.util.List;

/**
 * Anzeige Rückmeldedaten-Ausreißer (Vorläufer und Nachzügler)
 */
public class iPartsResponseSpikesDataForm extends AbstractTwoDataObjectGridsEditForm implements iPartsConst {

    public static final String CONFIG_KEY_RESPONSE_SPIKES_TOP = "Plugin/iPartsEdit/ResponseSpikesTop";
    public static final String CONFIG_KEY_RESPONSE_SPIKES_BOTTOM = "Plugin/iPartsEdit/ResponseSpikesBottom";

    public static final iPartsToolbarButtonAlias EDIT_NEW_RANGE = new iPartsToolbarButtonAlias("buttonNewRange", iPartsDefaultImages.edit_btn_new_multiple,
                                                                                               "", "!!Neuer Ident-Bereich", false);

    private String factory;       //Werksnummer
    private String seriesNo;      //Baureihe
    private String aa;            //Ausführungsart
    private String bmaa;          //Baumusterausführungsart
    private String pem;           //Produktionseinsatzmeldung
    private boolean isPemFrom;
    private String ident;         //Endnummer
    private boolean isEldasPartlist;
    private boolean retailFilter; //Zeige die Retailansicht
    private EtkDataPartListEntry partListEntry;

    private List<iPartsDataResponseSpike> responseSpikesList;    // Liste der Ausreißer. Wird im Parent Dialog bestimmt
    private iPartsDataResponseSpikeList responseSpikesListAS;    // Ausreißer (After-Sales)
    private iPartsDataResponseSpikeList responseSpikesListNonAS; // Ausreißer (Produktion)

    public iPartsResponseSpikesDataForm(RelatedInfoBaseForm parentForm, iPartsResponseDataForm.ResponseSpikesQuery responseSpikesQuery) {
        super(parentForm.getConnector(), parentForm, parentForm.getRelatedInfo(),
              CONFIG_KEY_RESPONSE_SPIKES_TOP + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              TranslationHandler.translate("!!Vorläufer und Nachzügler") + " " + TranslationHandler.translate(iPartsResponseDataForm.AFTER_SALES) + ":",
              CONFIG_KEY_RESPONSE_SPIKES_BOTTOM + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              TranslationHandler.translate("!!Vorläufer und Nachzügler") + " " + TranslationHandler.translate(iPartsResponseDataForm.PRODUCTION) + ":",
              responseSpikesQuery.isEditable());

        this.factory = responseSpikesQuery.getFactory();
        this.seriesNo = responseSpikesQuery.getSeriesNo();
        this.aa = responseSpikesQuery.getAa();
        this.bmaa = responseSpikesQuery.getBmaa();
        this.pem = responseSpikesQuery.getPem();
        this.isPemFrom = responseSpikesQuery.isPemFrom();
        this.ident = responseSpikesQuery.getIdent();
        this.partListEntry = responseSpikesQuery.getPartlistEntry();
        this.retailFilter = responseSpikesQuery.isRetailFilter();
        this.isEldasPartlist = responseSpikesQuery.isEldasPartlist();

        String titel;
        if (isEldasPartlist) {
            titel = TranslationHandler.translate("!!PEM: %1", pem) + "\n"
                    + TranslationHandler.translate("!!Fahrzeugidentnummer:") + " " + ident;
        } else {
            titel = TranslationHandler.translate("!!Werk: %1, PEM: %2", factory, pem) + "\n"
                    + TranslationHandler.translate("!!Fahrzeugidentnummer:") + " " + ident;
        }
        setWindowTitle(RELATED_INFO_RESPONSE_SPIKES_TEXT, titel);
        setReadOnly(retailFilter || iPartsLockEntryHelper.isLockedWithDBCheck(partListEntry) || !(editMode && isEditContext(getConnector(), true)));
        scaleFromParentForm(getWindow());

        if (retailFilter) {
            setGridBottomVisible(false);
        }

        this.responseSpikesList = ResponseDataHelper.getResponseSpikesForPemAndIdent(responseSpikesQuery.getResponseDataList(), pem, ident);
        dataToGrid();
    }

    @Override
    public boolean isRetailFilter() {
        return retailFilter;
    }

    @Override
    protected void setReadOnly(boolean isReadOnly) {
        super.setReadOnly(isReadOnly);

        if (toolbarHelperTop != null) {
            toolbarHelperTop.enableToolbarButtonAndMenu(EDIT_NEW_RANGE, gridTop.getContextMenu(), !isReadOnly);
        }
    }

    /**
     * @param top aktuell keine Erkenntnisse über Unterschiede oben/unten
     * @return
     */
    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(boolean top) {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();

        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_SPIKES, iPartsConst.FIELD_DRS_SPIKE_IDENT, false, false);
        displayField.setColumnFilterEnabled(true);
        displayField.setDefaultText(false);
        displayField.setText(new EtkMultiSprache("!!Vorläufer / Nachzügler", new String[]{ TranslationHandler.getUiLanguage() }));
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_SPIKES, iPartsConst.FIELD_DRS_SERIES_NO, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_SPIKES, iPartsConst.FIELD_DRS_AA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_SPIKES, iPartsConst.FIELD_DRS_STEERING, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_SPIKES, iPartsConst.FIELD_DRS_BMAA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_SPIKES, iPartsConst.FIELD_DRS_SOURCE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_RESPONSE_SPIKES, iPartsConst.FIELD_DRS_STATUS, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
        createResponseSpikesLists(retailFilter);

        // Für Retail müssen die Ausreißer gefiltert sein und es sollen die AS-Daten und Produktions-Daten zusammen
        // in einem Grid angezeigt werden wie bei den Werkseinsatzdaten.
        if (top) {
            if (retailFilter) {
                setGridTopTitle(TranslationHandler.translate("!!Vorläufer und Nachzügler") + ":");
                addDataObjectListToGrid(top, responseSpikesListAS);

                // Produktions-Daten hinzufügen falls es nicht schon identische AS-Daten gibt
                for (iPartsDataResponseSpike responseSpikeNonAS : responseSpikesListNonAS) {
                    boolean foundInAS = false;
                    for (iPartsDataResponseSpike responseSpikeAS : responseSpikesListAS) {
                        if (responseSpikeAS.equalContent(responseSpikeNonAS, true)) {
                            foundInAS = true;
                            break;
                        }
                    }
                    if (!foundInAS) {
                        addDataObjectToGrid(top, responseSpikeNonAS);
                    }
                }
            } else {
                addDataObjectListToGrid(top, responseSpikesListAS);
            }
        } else {
            addDataObjectListToGrid(top, responseSpikesListNonAS);
        }
    }

    /**
     * Ausreißer in AS und Non-AS aufteilen
     *
     * @param retailFilter
     */
    private void createResponseSpikesLists(boolean retailFilter) {
        responseSpikesListAS = new iPartsDataResponseSpikeList();
        responseSpikesListNonAS = new iPartsDataResponseSpikeList();
        if (responseSpikesList != null) { // kommt aus dem Parent Dialog
            EtkProject project = getProject();
            for (iPartsDataResponseSpike responseSpike : responseSpikesList) {
                boolean isASData = responseSpike.getFieldValueAsBoolean(iPartsConst.FIELD_DRS_AS_DATA);
                if (isASData) {
                    responseSpikesListAS.add(responseSpike.cloneMe(project), DBActionOrigin.FROM_DB);
                } else {
                    responseSpikesListNonAS.add(responseSpike.cloneMe(project), DBActionOrigin.FROM_DB);
                }
            }

            responseSpikesListAS = FactoryDataHelper.getFilteredResponseSpikes(responseSpikesListAS, retailFilter);
            responseSpikesListNonAS = FactoryDataHelper.getFilteredResponseSpikes(responseSpikesListNonAS, retailFilter);
        }
    }

    @Override
    protected DataObjectGrid createGrid(final boolean top) {
        final DataObjectFilterGridWithStatus dataGrid = new DataObjectFilterGridWithStatus(getConnector(), this, getTableName(),
                                                                                           getStatusFieldName()) {
            @Override
            protected void statusChanged() {
                statusChangedForGrid(top);
            }
        };

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

    @Override
    protected void addSpecialToolbarAndMenuEntries(ToolbarButtonAlias insertAfterButtonAlias, ToolbarButtonMenuHelper toolbarHelper,
                                                   DataObjectGrid gridTop, ToolbarButtonMenuHelper toolbarHelperBottom,
                                                   DataObjectGrid gridBottom) {
        if (insertAfterButtonAlias == EditToolbarButtonAlias.EDIT_NEW) {
            ToolbarButtonMenuHelper.ToolbarMenuHolder holder = toolbarHelper.addToolbarButtonAndCreateMenu(EDIT_NEW_RANGE,
                                                                                                           getUITranslationHandler(), new MenuRunnable() {
                        @Override
                        public void run(Event event) {
                            doNew(true, true);
                        }
                    });
            gridTop.getContextMenu().addChild(holder.menuItem);
        }
    }

    @Override
    protected void reloadEditableDataAndUpdateEditContext() {
        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            iPartsRelatedInfoEditContext editContext = ((iPartsRelatedInfoEditContext)getConnector().getEditContext());
            editContext.setFireDataChangedEvent(true);
            editContext.setUpdateRetailFactoryData(true);
            editContext.setUpdateResponseSpikes(true);
            editContext.setUpdateEditAssemblyData(true);
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
        iPartsResponseSpikes responseSpikesCache = iPartsResponseSpikes.getInstance(getProject());
        for (EtkDataObject dataObject : dataObjectList) {
            if (dataObject instanceof iPartsDataResponseSpike) {
                responseSpikesCache.updateCacheForPEMAndIdent(pem, ident, (iPartsDataResponseSpike)dataObject);
            }
        }
        for (EtkDataObject dataObject : dataObjectList.getDeletedList()) {
            if (dataObject instanceof iPartsDataResponseSpike) {
                responseSpikesCache.deleteCacheForPEMAndIdent(pem, ident, (iPartsDataResponseSpike)dataObject);
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
        }

        // Rückmeldedaten
        List<iPartsResponseDataWithSpikes> responseDataForPEM = ResponseDataHelper.getResponseDataForPEM(pem, partListEntry, isRetailFilter(), getProject(), isPemFrom);
        responseSpikesList = ResponseDataHelper.getResponseSpikesForPemAndIdent(responseDataForPEM, pem, ident);

        return super.saveDataObjectsWithUpdate(dataObjectList);
    }

    @Override
    protected void doNew(boolean top) {
        doNew(top, false);
    }

    protected void doNew(boolean top, boolean identRange) {
        iPartsResponseSpikeId responseSpikeId = new iPartsResponseSpikeId(factory, seriesNo, aa, bmaa, ident, "", pem, "", true);
        iPartsDataResponseSpikeList result = EditUserControlsForResponseSpike.showEditCreateResponseSpike(getConnector(), this,
                                                                                                          responseSpikeId, factory,
                                                                                                          identRange, true, isEldasPartlist);
        if (result != null) {
            saveDataObjectsWithUpdate(result);
        }
    }

    @Override
    protected void doEdit(boolean top) {
        iPartsDataResponseSpike selectedResponseSpike = getSelection(top, iPartsDataResponseSpike.class);
        if (selectedResponseSpike != null) {
            iPartsDataResponseSpikeList result = EditUserControlsForResponseSpike.showEditCreateResponseSpike(getConnector(), this,
                                                                                                              selectedResponseSpike.getAsId(),
                                                                                                              factory, false, false,
                                                                                                              isEldasPartlist);
            if (result != null) {
                saveDataObjectsWithUpdate(result);
            }
        }
    }

    @Override
    protected void doDelete(boolean top) {
        doDeleteDataObjects(top, iPartsDataResponseSpike.class);
    }

    private void statusChangedForGrid(boolean top) {
        // Selektion muss VOR doSaveDataObjects() ausgelesen werden, weil diese durch das Update ansonsten verlorengeht
        EtkDataObjectList<iPartsDataResponseSpike> multiSelection = getMultiSelection(top, iPartsDataResponseSpike.class);

        List<iPartsDataResponseSpike> allResponseSpikeDataObjects = getDataObjectList(top, iPartsDataResponseSpike.class);
        EtkDataObjectList<iPartsDataResponseSpike> changedDataObjects = iPartsRelatedEditHelper.updateStatusValuesResponseAndSpikes(multiSelection,
                                                                                                                                    allResponseSpikeDataObjects,
                                                                                                                                    FIELD_DRS_ADAT,
                                                                                                                                    getStatusFieldName());
        saveDataObjectsWithUpdate(changedDataObjects);
        // iPartsRelatedEditHelper.statusChanged(changedDataObjects, this) nicht nötig, weil der RMID Importer Ausreißer immer
        // mit Status freigeben importiert. Deswegen wird kein Eintrag in DA_DIALOG_CHANGES angelegt und somit kann der Änderung
        // auch keine Changeset-GUID zugewiesen werden. Freigegebene Datensätze müssen nicht von Autoren überprüft werden.
    }

    @Override
    protected String getStatusFieldName() {
        return iPartsConst.FIELD_DRS_STATUS;
    }

    @Override
    protected String getTableName() {
        return TABLE_DA_RESPONSE_SPIKES;
    }

    @Override
    public String getSourceFieldName() {
        return iPartsConst.FIELD_DRS_SOURCE;
    }
}