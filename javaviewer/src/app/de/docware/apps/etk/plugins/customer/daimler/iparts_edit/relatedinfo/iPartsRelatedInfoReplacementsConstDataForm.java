/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstMat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstMatList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstPartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsReplaceConstMatId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsReplaceConstPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacementConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.util.sql.SQLStringConvert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class iPartsRelatedInfoReplacementsConstDataForm extends AbstractRelatedInfoReplacementDataForm {

    // Spalten für die aktuellen Daten: [Current Data]
    private static final String CONFIG_KEY_REPLACEMENTS_MAT_CURRENT_DATA = "Plugin/iPartsEdit/CurrentConstMat";
    // Spalten für die Vorgänger/Nachfolger [Predecessor/Successor]
    private static final String CONFIG_KEY_REPLACEMENTS_MAT_PREDECESSOR_SUCCESSOR_DATA = "Plugin/iPartsEdit/ReplaceConstMat";

    private static final String IPARTS_MENU_ITEM_SHOW_REPLACEMENTS_MAT = "iPartsMenuItemShowReplacementsMat";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.Dialog_SM_Construction);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_REPLACEMENTS_MAT, "!!Ersetzungen zum Teil anzeigen",
                                EditDefaultImages.edit_rep_predecessor_successor.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_REPLACE_CONST_MAT_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste gültig für den gewünschten Modultyp?
        boolean menuItemVisible = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            menuItemVisible = relatedInfoIsVisible(connector.getSelectedPartListEntries().get(0));
        }

        // Separator und Menüeintrag aktualisieren
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_REPLACEMENTS_MAT, menuItemVisible);
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry entry) {
        if ((entry != null) && relatedInfoIsVisible(entry.getOwnerAssembly(), VALID_MODULE_TYPES)) {
            if (entry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsEntry = ((iPartsDataPartListEntry)entry);
                return iPartsEntry.hasPredecessorsConst() || iPartsEntry.hasSuccessorsConst();
            }
        }
        return false;
    }

    private class ReplacementDataObjectGrid extends AbstractReplacementDataObjectGrid {

        public ReplacementDataObjectGrid(AbstractJavaViewerFormIConnector dataConnector, final iPartsRelatedInfoReplacementsConstDataForm parentForm,
                                         REPLACEMENT_GRID_TYPE gridType) {
            super(dataConnector, parentForm, null, null);

            setGridType(gridType);
        }

        @Override
        protected void statusChanged() {
            // Es gibt keinen Status in DA_REPLACE_CONST_MAT
        }

        @Override
        protected void showIncludeParts() {
            iPartsDataReplaceConstMat replacement = getSelectedReplacement();
            if (replacement != null) {
                // Neuer RelatedInfoFormConnector ist nicht notwendig, weil lediglich die OwnerAssembly benötigt wird,
                // die für alle Stücklisteneinträge des Moduls ja identisch ist
                iPartsReplacementsIncludePartsConstForm form = new iPartsReplacementsIncludePartsConstForm((RelatedInfoFormConnector)getConnector(), this, replacement);
                form.showModal();
            }
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            super.onTableSelectionChanged(event);
            boolean enabled = getTable().getSelectedRows().size() == 1;
            if (enabled) {
                iPartsDataReplaceConstMat selectedReplacement = getSelectedReplacement();

                if (includePartsMenuItem != null) {
                    // Kontextmenu nur anzeigen wenn auch Mitlieferteile vorhanden sind
                    if (selectedReplacement != null) {
                        boolean includePartsAvailable = selectedReplacement.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DRCM_INCLUDE_PARTS_AVAILABLE);
                        includePartsMenuItem.setEnabled(includePartsAvailable);
                    } else {
                        includePartsMenuItem.setEnabled(false);
                    }
                }
            }
        }

        private iPartsDataReplaceConstMat getSelectedReplacement() {
            List<EtkDataObject> selectionList = getSelection();
            if ((selectionList != null) && !selectionList.isEmpty()) {
                for (EtkDataObject dataObject : selectionList) {
                    if (dataObject instanceof iPartsDataReplaceConstMat) {
                        return (iPartsDataReplaceConstMat)dataObject;
                    }
                }
            }
            return null;
        }

    }

    protected iPartsRelatedInfoReplacementsConstDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                         IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        // Die Unterscheidungsmöglichkeit bei createDefaultDisplayFields() für die unterschiedlichen Grids
        // verschiedene Spalten anzeigen zu können.
        if (configKey.equals(CONFIG_KEY_REPLACEMENTS_MAT_PREDECESSOR_SUCCESSOR_DATA)) {
            return createPredecessorSuccessorDefaultDisplayFields();
        } else if (configKey.equals(CONFIG_KEY_REPLACEMENTS_MAT_CURRENT_DATA)) {
            return createCurrentDataDefaultDisplayFields();
        }
        return null;
    }

    /**
     * Erzeugt die Liste der Default-Anzeigespalten für den AKTUELLEN Stücklisteneintrag.
     *
     * @return
     */
    private List<EtkDisplayField> createCurrentDataDefaultDisplayFields() {
        List<EtkDisplayField> list = new ArrayList<>();

        addDisplayField(list, iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR, false, false, true); // Teilenummer
        addDisplayField(list, iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_CONST_DESC, true, false, true); // Konstruktionsbezeichnung

        // virtuelle Felder:
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO, false, false, false); // virtuelle DIALOG Baureuhe
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_HM, false, false, false); // virtuelle DIALOG HM
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_M, false, false, false); // virtuelle DIALOG M
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SM, false, false, false); // virtuelle DIALOG SM
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE, false, false, false); // virtuelle DIALOG POSE
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV, false, false, false); // virtuelle DIALOG POSV
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_WW, false, false, false); // virtuelles DIALOG Wahlweise
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETKZ, false, false, false); // virtueller DIALOG Ersatzteilkennzeichen
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA, false, false, false); // SDATA
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB, false, false, false); // SDATB

        if (Constants.DEVELOPMENT) {
            addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_LFDNR, false, false, true);
        }

        // berechnete Felder:
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_CONST_MAT, iPartsDataVirtualFieldsDefinition.DRCM_FACTORY_DATA_AVAILABLE, false, false, false);
        return list;
    }

    /**
     * Erzeugt die Liste der Default-Anzeigespalten für VORGÄNGER und NACHFOLGER.
     *
     * @return
     */
    private List<EtkDisplayField> createPredecessorSuccessorDefaultDisplayFields() {
        List<EtkDisplayField> list = new ArrayList<>();

        addDisplayField(list, iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR, false, false, true); // Teilenummer
        addDisplayField(list, iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_CONST_DESC, true, false, true); // Benennung
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_CONST_MAT, iPartsConst.FIELD_DRCM_PRE_RFME, false, false, false); // RFMEA
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_CONST_MAT, iPartsConst.FIELD_DRCM_RFME, false, false, false); // RFMEN
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_CONST_PART, iPartsConst.FIELD_DRCP_RFME, false, false, false); // RFME Entwicklung

        // virtuelle Felder:
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE, false, false, false); // virtuelle DIALOG POSE
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV, false, false, false); // virtuelle DIALOG POSV
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_WW, false, false, false); // virtuelles DIALOG Wahlweise
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETKZ, false, false, false); // virtueller DIALOG Ersatzteilkennzeichen
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA, false, false, false); // SDATA
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB, false, false, false); // SDATB

        if (Constants.DEVELOPMENT) {
            addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_LFDNR, false, false, true);
        }

        // berechnete Felder:
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_CONST_MAT, iPartsDataVirtualFieldsDefinition.DRCM_INCLUDE_PARTS_AVAILABLE, false, false, false);
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_CONST_MAT, iPartsDataVirtualFieldsDefinition.DRCM_FACTORY_DATA_AVAILABLE, false, false, false);

        return list;
    }

    @Override
    protected void dataToGrid() {
        int oldPredecessorSortColumn = predecessorDataGrid.getTable().getSortColumn();
        boolean isPredecessorSortAscending = predecessorDataGrid.getTable().isSortAscending();
        int oldCurrentSortColumn = currentDataGrid.getTable().getSortColumn();
        boolean isCurrentSortAscending = currentDataGrid.getTable().isSortAscending();
        int oldSuccessorSortColumn = successorDataGrid.getTable().getSortColumn();
        boolean isSuccessorSortAscending = successorDataGrid.getTable().isSortAscending();

        clearGrids();

        // Die aktuellen Daten aus der Stückliste in das dafür vorgesehenene Grid übertragen inkl. berechnetem Wert für das
        // virtuelle Feld "Werkseinsatzdaten vorhanden" mit einem Dummy iPartsDataReplaceConstMat-Objekt für das Grid
        iPartsDataReplaceConstMat dummyDataReplaceConstMat = new iPartsDataReplaceConstMat(getProject(), new iPartsReplaceConstMatId());
        dummyDataReplaceConstMat.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        if (partListEntry instanceof iPartsDataPartListEntry) {
            addFactoryDataAvailableValue((iPartsDataPartListEntry)partListEntry, dummyDataReplaceConstMat);
        }
        currentDataGrid.addObjectToGrid(partListEntry, dummyDataReplaceConstMat);

        if (partListEntry instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;

            // Vorgänger-Daten ins Grid übertragen.
            Collection<iPartsReplacementConst> predecessorReplacements = iPartsPartListEntry.getPredecessorsConst();
            if (predecessorReplacements != null) {
                for (iPartsReplacementConst predecessorReplacement : predecessorReplacements) {
                    addDataObjectsToGrid(predecessorReplacement, predecessorReplacement.predecessorEntry, predecessorDataGrid);
                }
            }

            // Nachfolger-Daten ins Grid übertragen.
            Collection<iPartsReplacementConst> successorReplacements = iPartsPartListEntry.getSuccessorsConst();
            if (successorReplacements != null) {
                for (iPartsReplacementConst successorReplacement : successorReplacements) {
                    addDataObjectsToGrid(successorReplacement, successorReplacement.successorEntry, successorDataGrid);
                }
            }
        }
        predecessorDataGrid.getTable().sortRowsAccordingToColumn(oldPredecessorSortColumn, isPredecessorSortAscending);
        currentDataGrid.getTable().sortRowsAccordingToColumn(oldCurrentSortColumn, isCurrentSortAscending);
        successorDataGrid.getTable().sortRowsAccordingToColumn(oldSuccessorSortColumn, isSuccessorSortAscending);

        predecessorDataGrid.showNoResultsLabel(predecessorDataGrid.getTable().getRowCount() == 0);
        successorDataGrid.showNoResultsLabel(successorDataGrid.getTable().getRowCount() == 0);
    }

    /**
     * Daten zusammenfassen und ggf. ausgeben.
     *
     * @param replacement
     * @param partListEntry
     * @param dataObjectGrid
     */
    private void addDataObjectsToGrid(iPartsReplacementConst replacement, iPartsDataPartListEntry partListEntry,
                                      AbstractReplacementDataObjectGrid dataObjectGrid) {

        iPartsDataReplaceConstMat dataReplacementVTNV = null;
        iPartsDataReplaceConstPart dataReplacementTS7 = null;

        iPartsReplaceConstMatId replaceConstMatId = replacement.getAsReplaceConstMatId();
        iPartsReplaceConstPartId replaceConstPartId = replacement.getAsReplaceConstPartId();

        // DAIMLER-7551: Wenn Vorgänger, SDATA und Nachfolger der VTNV und der TS7 Ersetzung gleich sind, dann diese in
        // einer Zeile anzeigen. In diesen Fall wird hier jeweils ein Eintrag gefunden und in die selbe Row geschrieben.
        iPartsDataReplaceConstPartList dataReplacementTS7AsList = iPartsDataReplaceConstPartList.loadDataReplacementForPartsAndSDATA(getProject(), replacement.getPredecessorPartNo(),
                                                                                                                                     replacement.sDatA, replacement.getSuccessorPartNo());
        if (!dataReplacementTS7AsList.isEmpty()) {
            dataReplacementTS7 = dataReplacementTS7AsList.get(0);
        } else {
            dataReplacementTS7 = new iPartsDataReplaceConstPart(getProject(), replaceConstPartId);
            dataReplacementTS7.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }

        iPartsDataReplaceConstMatList dataReplacementVTNVAsList = iPartsDataReplaceConstMatList.loadDataReplacementForPartsAndSDATA(getProject(), replacement.getPredecessorPartNo(),
                                                                                                                                    replacement.sDatA, replacement.getSuccessorPartNo());
        if (!dataReplacementVTNVAsList.isEmpty()) {
            dataReplacementVTNV = dataReplacementVTNVAsList.get(0);
        } else {
            dataReplacementVTNV = new iPartsDataReplaceConstMat(getProject(), replaceConstMatId);
            dataReplacementVTNV.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }

        if (replacement.isSourceTS7()) {
            if (!dataReplacementTS7AsList.isEmpty() && !dataReplacementVTNVAsList.isEmpty()) {
                // wenn eine äquivalente TS7 und VTNV Ersetzung existiert, dann unterbinden,
                // dass hier die gleiche Zeile nochmal angezeigt wird
                return;
            }
        }

        addIncludePartsAvailableValue(replacement, dataReplacementVTNV);
        addFactoryDataAvailableValue(partListEntry, dataReplacementVTNV);
        dataObjectGrid.addObjectToGrid(partListEntry, dataReplacementVTNV, dataReplacementTS7);
    }

    private void addIncludePartsAvailableValue(iPartsReplacementConst replacement, EtkDataObject dataReplacement) {
        dataReplacement.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DRCM_INCLUDE_PARTS_AVAILABLE,
                                                 SQLStringConvert.booleanToPPString(replacement.hasIncludeParts(getProject())), true,
                                                 DBActionOrigin.FROM_DB);
    }

    private void addFactoryDataAvailableValue(iPartsDataPartListEntry partListEntry, EtkDataObject dataReplacement) {
        boolean isValid = partListEntry.getFactoryDataValidity().equals(iPartsFactoryData.ValidityType.VALID_FOR_CONSTRUCTION);
        dataReplacement.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DRCM_FACTORY_DATA_AVAILABLE,
                                                 SQLStringConvert.booleanToPPString(isValid), true,
                                                 DBActionOrigin.FROM_DB);
    }

    @Override
    protected void addDataGrids(GuiPanel panelPredecessorData, GuiPanel panelCurrentData, GuiPanel panelSuccessorData) {
        // Vorgänger-DataGrid erzeugen, die Position festlegen und auf das Panel pappen.
        predecessorDataGrid = new ReplacementDataObjectGrid(getConnector(), this, REPLACEMENT_GRID_TYPE.PREDECESSOR);
        predecessorDataGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        panelPredecessorData.addChild(predecessorDataGrid.getGui());

        // Aktuelle Daten-DataGrid erzeugen, die Position festlegen und auf das Panel pappen.
        currentDataGrid = new ReplacementDataObjectGrid(getConnector(), this, REPLACEMENT_GRID_TYPE.CURRENT);
        currentDataGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        panelCurrentData.addChild(currentDataGrid.getGui());

        // Nachfolger-DataGrid erzeugen, die Position festlegen und auf das Panel pappen.
        successorDataGrid = new ReplacementDataObjectGrid(getConnector(), this, REPLACEMENT_GRID_TYPE.SUCCESSOR);
        successorDataGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        panelSuccessorData.addChild(successorDataGrid.getGui());

        // Die anzuzeigenden Spalten der einzelnen Grids setzen.
        predecessorDataGrid.setDisplayFields(getDisplayFields(CONFIG_KEY_REPLACEMENTS_MAT_PREDECESSOR_SUCCESSOR_DATA));
        currentDataGrid.setDisplayFields(getDisplayFields(CONFIG_KEY_REPLACEMENTS_MAT_CURRENT_DATA));
        successorDataGrid.setDisplayFields(getDisplayFields(CONFIG_KEY_REPLACEMENTS_MAT_PREDECESSOR_SUCCESSOR_DATA));
    }

}