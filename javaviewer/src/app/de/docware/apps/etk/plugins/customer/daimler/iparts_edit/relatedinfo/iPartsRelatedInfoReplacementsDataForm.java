/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDialogChangesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementKEMHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.RelatedInfoSingleEditHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditSelectReplacementForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

public class iPartsRelatedInfoReplacementsDataForm extends AbstractRelatedInfoReplacementDataForm {

    // Spalten für die aktuellen Daten: [Current Data]
    public static final String CONFIG_KEY_REPLACEMENTS_CURRENT_DATA = "Plugin/iPartsEdit/ReplacementsCData";
    // Spalten für die Vorgänger/Nachfolger [Predecessor/Successor]
    public static final String CONFIG_KEY_REPLACEMENTS_PREDECESSOR_SUCCESSOR_DATA = "Plugin/iPartsEdit/ReplacementsPSData";

    public static final String CONFIG_KEY_REPLACEMENTS_EDIT_PARTLIST = "Plugin/iPartsEdit/ReplacementsPartlist" + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;

    public static final String IPARTS_MENU_ITEM_SHOW_REPLACEMENTS = "iPartsMenuItemShowReplacements";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = iPartsModuleTypes.EDITABLE_MODULE_TYPES;

    private boolean isEditRelatedInfo;
    private boolean isReadOnly;

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_REPLACEMENTS, "!!Ersetzungen anzeigen",
                                EditDefaultImages.edit_rep_predecessor_successor.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_REPLACEMENTS_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste gültig für den gewünschten Modultyp?
        boolean menuItemVisible = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            boolean isEditRelatedInfo = isEditContext(connector, false);
            boolean isFilterActive = !isEditRelatedInfo && iPartsAssemblyListSelectFormConnectorWithFilterSettings.isFilterActive(connector);
            menuItemVisible = relatedInfoIsVisible(connector.getSelectedPartListEntries().get(0),
                                                   isEditContext(connector, true), isFilterActive);
        }

        // Im Edit nur Menüeinträge zur Bearbeitung anzeigen
        if (menuItemVisible && isEditContext(connector, true)) {
            menuItemVisible = false;
        }

        // Separator und Menüeintrag aktualisieren
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_REPLACEMENTS, menuItemVisible);
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry entry, boolean isEditRelatedInfo, boolean isFilterActive) {
        if ((entry != null) && relatedInfoIsVisible(entry.getOwnerAssembly(), VALID_MODULE_TYPES)) {
            if (entry instanceof iPartsDataPartListEntry) {
                if (isEditRelatedInfo) {
                    if (iPartsUserSettingsHelper.isSingleEdit(entry.getEtkProject())) {
                        return true;
                    }
                } else {
                    iPartsDataPartListEntry iPartsEntry = ((iPartsDataPartListEntry)entry);
                    return iPartsEntry.hasPredecessors(isFilterActive) || iPartsEntry.hasSuccessors(isFilterActive);
                }
            }
        }
        return false;
    }

    public static AssemblyListCellContentFromPlugin getRelatedInfoIcon(iPartsDataPartListEntry iPartsEntry, boolean isVirtualPartList,
                                                                       boolean isFilterActive, boolean isEditMode) {
        boolean hasPredecessor;
        boolean hasSuccessor;
        boolean isVirtualInheritedSuccessor = false;
        boolean isVirtualInheritedPredecessor = false;
        String pathName;
        if (!isVirtualPartList) {
            hasPredecessor = iPartsEntry.hasPredecessors(isFilterActive);
            isVirtualInheritedPredecessor = iPartsEntry.isPredecessorVirtualInherited(isFilterActive);

            hasSuccessor = iPartsEntry.hasSuccessors(isFilterActive);
            isVirtualInheritedSuccessor = iPartsEntry.isSuccessorVirtualInherited(isFilterActive);

            pathName = iPartsConst.CONFIG_KEY_RELATED_INFO_REPLACEMENTS_DATA;
            if (isEditMode) {
                pathName = RelatedInfoSingleEditHelper.getActiveRelatedInfo(iPartsEntry.getEtkProject(), pathName);
            }
        } else {
            hasPredecessor = iPartsEntry.hasPredecessorsConst();
            hasSuccessor = iPartsEntry.hasSuccessorsConst();
            pathName = iPartsConst.CONFIG_KEY_RELATED_INFO_REPLACE_CONST_MAT_DATA;
        }
        return getReplaceIcons(hasPredecessor, isVirtualInheritedPredecessor, hasSuccessor, isVirtualInheritedSuccessor, pathName);
    }

    private static AssemblyListCellContentFromPlugin getReplaceIcons(boolean hasPredecessor, boolean isVirtualInheritedPredecessor,
                                                                     boolean hasSuccessor, boolean isVirtualInheritedSuccessor, String configKey) {
        if (hasPredecessor || hasSuccessor) {
            // virtuelle Nachfolger/ Vorgänger bekommen ein graues, normale Nachfolger/ Vorgänger ist farbiges Icon
            AssemblyListCellContentFromPlugin replaceIcon = null;
            if (hasPredecessor && hasSuccessor) {
                if (isVirtualInheritedPredecessor && isVirtualInheritedSuccessor) {
                    replaceIcon = new AssemblyListCellContentFromPlugin(configKey, EditDefaultImages.edit_rep_predecessor_virt_successor_virt.getImage());
                } else {
                    if (isVirtualInheritedPredecessor) {
                        replaceIcon = new AssemblyListCellContentFromPlugin(configKey, EditDefaultImages.edit_rep_predecessor_virt_successor.getImage());
                    } else if (isVirtualInheritedSuccessor) {
                        replaceIcon = new AssemblyListCellContentFromPlugin(configKey, EditDefaultImages.edit_rep_predecessor_successor_virt.getImage());
                    } else {
                        replaceIcon = new AssemblyListCellContentFromPlugin(configKey, EditDefaultImages.edit_rep_predecessor_successor.getImage());
                    }
                }
                replaceIcon.setHint(TranslationHandler.translate("!!Vorgänger und Nachfolger"));
            } else {
                if (hasPredecessor) {
                    if (isVirtualInheritedPredecessor) {
                        replaceIcon = new AssemblyListCellContentFromPlugin(configKey, EditDefaultImages.edit_rep_predecessor_virt.getImage());
                    } else {
                        replaceIcon = new AssemblyListCellContentFromPlugin(configKey, EditDefaultImages.edit_rep_predecessor.getImage());
                    }
                    replaceIcon.setHint(TranslationHandler.translate("!!Vorgänger"));
                }
                if (hasSuccessor) {
                    if (isVirtualInheritedSuccessor) {
                        replaceIcon = new AssemblyListCellContentFromPlugin(configKey, EditDefaultImages.edit_rep_successor_virt.getImage());
                    } else {
                        replaceIcon = new AssemblyListCellContentFromPlugin(configKey, EditDefaultImages.edit_rep_successor.getImage());
                    }
                    replaceIcon.setHint(TranslationHandler.translate("!!Nachfolger"));
                }
            }
            replaceIcon.setCursor(DWCursor.Hand);
            replaceIcon.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
            return replaceIcon;
        }
        return null;
    }


    protected static class ReplaceGridObject extends iPartsDataReplacePart {

        private iPartsReplacement replacement;

        public ReplaceGridObject(EtkProject project, iPartsDataReplacePart dataReplacePart, iPartsReplacement replacement) {
            super(project, dataReplacePart.getAsId());
            this.assignRecursively(project, dataReplacePart, DBActionOrigin.FROM_DB);
            this.replacement = replacement;
        }

        public iPartsReplacement getReplacement() {
            return replacement;
        }
    }


    /**
     * Liefert den Feldnamen für die Quelle eines Datensatzes zurück.
     *
     * @return Bei {@code null} wird die Quelle nicht berücksichtigt
     */
    @Override
    public String getSourceFieldName() {
        return iPartsConst.FIELD_DRP_SOURCE;
    }

    @Override
    public String getStatusFieldName() {
        return iPartsConst.FIELD_DRP_STATUS;
    }

    private class ReplacementDataObjectGrid extends AbstractReplacementDataObjectGrid {

        public ReplacementDataObjectGrid(AbstractJavaViewerFormIConnector dataConnector, final iPartsRelatedInfoReplacementsDataForm parentForm,
                                         REPLACEMENT_GRID_TYPE gridType, String statusTableName, String statusFieldName) {
            super(dataConnector, parentForm, statusTableName, statusFieldName);

            if ((gridType == REPLACEMENT_GRID_TYPE.SUCCESSOR) && isEditRelatedInfo) {
                GuiContextMenu contextMenu = getContextMenu();

                toolbarHelper = getToolbarHelper();
                toolbarManager = toolbarHelper.getToolbarManager();

                ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
                holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        parentForm.addSuccessor(event);
                    }
                });
                holder.menuItem.setText("!!Neuer Nachfolger");
                contextMenu.addChild(holder.menuItem);

                holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        parentForm.editSuccessor(event);
                    }
                });
                holder.menuItem.setText("!!Nachfolger bearbeiten");
                contextMenu.addChild(holder.menuItem);

                holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        parentForm.removeSuccessor(event);
                    }
                });
                holder.menuItem.setText("!!Nachfolger löschen");
                contextMenu.addChild(holder.menuItem);
                // Ab Daimler-8972 kann der Status einer Ersetzung angepasst werden
                contextMenu.addChild(getStatusContextMenu());

                contextMenu.addChild(new GuiSeparator());

                // Initialen Toolbar Zustand setzen
                toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, contextMenu, (parentForm.isEditRelatedInfo && !isReadOnly));
                toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, contextMenu, false);
                toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, contextMenu, false);
            }

            setGridType(gridType);
        }

        private iPartsReplacement getSelectedReplacement() {
            List<EtkDataObject> selectionList = getSelection();
            if ((selectionList != null) && !selectionList.isEmpty()) {
                for (EtkDataObject dataObject : selectionList) {
                    if (dataObject instanceof ReplaceGridObject) {
                        return ((ReplaceGridObject)dataObject).getReplacement();
                    }
                }
            }
            return null;
        }

        /**
         * Liefert die selektierten {@link iPartsDataReplacePart}s zurück.
         *
         * @return
         */
        private iPartsDataReplacePartList getMultiSelectionReplacement() {
            List<List<EtkDataObject>> selection = getMultiSelection();
            iPartsDataReplacePartList result = new iPartsDataReplacePartList();
            if (selection != null) {
                for (List<EtkDataObject> selectedRow : selection) {
                    for (EtkDataObject selectedDataObject : selectedRow) {
                        if (iPartsDataReplacePart.class.isAssignableFrom(selectedDataObject.getClass())) {
                            result.add((iPartsDataReplacePart)selectedDataObject, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }

            return result;
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            super.onTableSelectionChanged(event);
            List<List<EtkDataObject>> selectedRows = getMultiSelection();
            boolean singleSelection = selectedRows.size() == 1;
            if (singleSelection) {
                iPartsDataPartListEntry selectedPartListEntry = getSelectedPartListEntry();
                boolean enabledForPartListEntry = selectedPartListEntry != null;

                boolean isFilterActive = !isEditRelatedInfo && iPartsRelatedInfoFormConnectorWithFilterSettings.isFilterActive(getConnector());
                iPartsReplacement selectedReplacement = getSelectedReplacement();
                boolean partIsFilteredOut = false;
                if (enabledForPartListEntry && isFilterActive) {
                    if ((selectedReplacement != null) && (selectedReplacement.source == iPartsReplacement.Source.PRIMUS)) {
                        partIsFilteredOut = true; // Keine saubere Filterung durch die geleerten Attribute möglich, ist bei PRIMUS aber irrelevant
                    } else {
                        partIsFilteredOut = !iPartsFilter.get().checkFilter(selectedPartListEntry);
                    }
                }

                if ((changeMenuItem != null) && changeMenuItem.isVisible()) {
                    changeMenuItem.setEnabled(enabledForPartListEntry && !partIsFilteredOut);
                }

                gotoMenuItem.setEnabled(enabledForPartListEntry && !partIsFilteredOut);
                if (includePartsMenuItem != null) {
                    if (selectedReplacement != null) {
                        // Kontextmenu nur anzeigen wenn auch Mitlieferteile vorhanden sind bzw. im Edit
                        includePartsMenuItem.setEnabled((isEditRelatedInfo && selectedReplacement.isIncludePartsEditable())
                                                        || selectedReplacement.hasIncludeParts(getProject()));
                    } else {
                        includePartsMenuItem.setEnabled(false);
                    }
                }
                factoryMenuItem.setEnabled(enabledForPartListEntry && selectedPartListEntry.isValidFactoryDataRelevantForEndNumberFilter());
            }
            // Edit Toolbar Sichtbarkeiten
            if (isEditRelatedInfo && !isReadOnly) {
                // Nur für das successor Datagrid, das einzige mit Statuscontextmenü
                GuiMenuItem statusContextMenu = getStatusContextMenu();
                if (statusContextMenu != null) {
                    boolean editEnabled;
                    boolean deleteEnabled;
                    if (selectedRows.size() == 0) {
                        editEnabled = deleteEnabled = false;
                    } else {
                        // Mindestens ein Eintrag ist selektiert.
                        // Edit nur für nicht vererbte Daten.
                        deleteEnabled = true;
                        editEnabled = true;
                        String searchTableName = successorDataGrid.getStatusTableName();
                        for (List<EtkDataObject> dataObjectList : selectedRows) {
                            for (EtkDataObject dataObject : dataObjectList) {
                                if (dataObject.getTableName().equals(searchTableName)) {
                                    String source = dataObject.getFieldValue(getSourceFieldName());
                                    boolean validBcte = isPartListEntryWithValidBCTEKey();
                                    // Löschen nur bei Quelle iParts oder bei Quelle MAD, wenn kein BCTE-Schlüssel
                                    // vorhanden ist. Bei MultiSelect müssen alle Quelle iParts sein oder eben MAD, falls
                                    // kein BCTE Schlüssel vorhanden ist.
                                    if (!source.equals(iPartsImportDataOrigin.IPARTS.getOrigin()) &&
                                        !(source.equals(iPartsImportDataOrigin.MAD.getOrigin()) && !validBcte)) {
                                        deleteEnabled = false;
                                    }

                                    // Vererbte Ersetzungen und PRIMUS-Ersetzungen dürfen nicht bearbeitet werden
                                    boolean inherited = dataObject.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DRP_INHERITED);
                                    if (inherited || source.equals(iPartsImportDataOrigin.PRIMUS.getOrigin())) {
                                        editEnabled = false;
                                    }
                                }
                            }
                        }
                    }
                    toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, successorDataGrid.getContextMenu(),
                                                             singleSelection && editEnabled);
                    toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, successorDataGrid.getContextMenu(),
                                                             editEnabled && deleteEnabled);
                }
                // Neu bleibt im Edit immer singleSelection
            }
        }

        @Override
        protected void statusChanged() {
            iPartsDataReplacePartList selectedReplacements = getMultiSelectionReplacement();
            if (selectedReplacements != null) {
                statusChangedForDataObject(selectedReplacements, false, false);
            }
        }

        /**
         * Setzt den Status für die Ersetzung im Grid und
         * Setzt den Status vom übergebenen {@code dataObject} auf {@code releaseState}.
         *
         * @param dataObject
         * @param releaseState
         * @return {@code true} falls das {@code dataObject} durch das Setzen vom Status verändert wurde
         */
        @Override
        protected boolean setStatus(EtkDataObject dataObject, iPartsDataReleaseState releaseState) {
            if (dataObject instanceof ReplaceGridObject) {
                iPartsReplacement replacement = ((ReplaceGridObject)dataObject).getReplacement();
                replacement.releaseState = releaseState;
            }

            return super.setStatus(dataObject, releaseState);
        }

        protected void showIncludeParts() {
            iPartsReplacement replacement = getSelectedReplacement();
            if (replacement != null) {
                // Neuer RelatedInfoFormConnector ist nicht notwendig, weil lediglich die OwnerAssembly benötigt wird,
                // die für alle Stücklisteneinträge des Moduls ja identisch ist
                iPartsReplacementsIncludePartsForm form = new iPartsReplacementsIncludePartsForm((RelatedInfoFormConnector)getConnector(),
                                                                                                 this, getRelatedInfo(), replacement, false);
                form.showModal();
            }
        }

        protected void addReplacementToGrid(EtkDataObject dataObject, iPartsReplacement replacement) {
            iPartsDataReplacePart dataReplacePart = replacement.getAsDataReplacePart(getProject(), true);
            ReplaceGridObject gridObject = new ReplaceGridObject(getProject(), dataReplacePart, replacement);

            boolean isPrimus = dataReplacePart.getSource().equals(iPartsReplacement.Source.PRIMUS);

            // Bei PRIMUS Ersetzungen sollen, zum besseren Verständnis, alle (!) Inhalte aus der Katalog-Tabelle
            // ausgeblendet werden
            EtkDataObject tempDataObject = dataObject;
            if (isPrimus) {
                if (tempDataObject instanceof EtkDataPartListEntry) {
                    tempDataObject = ((EtkDataPartListEntry)dataObject).cloneMe(getProject());
                    EtkDisplayFields displayFields = getProject().getAllDisplayFieldsForTable(iPartsConst.TABLE_KATALOG);
                    tempDataObject.setLogLoadFieldIfNeeded(false);
                    for (EtkDisplayField displayField : displayFields.getFields()) {
                        DBDataObjectAttribute attribute = tempDataObject.getAttribute(displayField.getKey().getFieldName(), false);
                        if (attribute == null) {
                            attribute = new DBDataObjectAttribute(displayField.getKey().getFieldName(),
                                                                  displayField.isMultiLanguage() ? DBDataObjectAttribute.TYPE.MULTI_LANGUAGE
                                                                                                 : displayField.isArray() ? DBDataObjectAttribute.TYPE.ARRAY
                                                                                                                          : DBDataObjectAttribute.TYPE.STRING,
                                                                  false);
                            tempDataObject.getAttributes().addField(attribute, DBActionOrigin.FROM_DB);
                        }
                        attribute.initWithDefaultValue();
                    }
                }
            }

            // Werte für die berechneten Felder hinzufügen
            addIncludePartsAvailableValue(replacement, gridObject);
            addFactoryDataAvailableValue(tempDataObject, gridObject, isPrimus);
            addInheritedValue(replacement, gridObject);
            // Kontrollieren, ob Zugriff auf Statusänderung verhindert werden muss, weil Ersetzung schon woanders
            // bearbeitet werden muss
            checkStatusValuesForReadOnly(gridObject);

            // In der Workbench können beliebige Felder konfiguriert werden, was ein Nachladen der Felder erfordern kann
            boolean oldLogLoadFieldIfNeeded = tempDataObject.isLogLoadFieldIfNeeded();
            tempDataObject.setLogLoadFieldIfNeeded(false);
            try {
                addObjectToGrid(tempDataObject, gridObject);
            } finally {
                tempDataObject.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
        }

        @Override
        protected iPartsDataReleaseState getStatusFromSelection(List<List<EtkDataObject>> selection) {
            // prüfen ob in der Selektion vererbte Ersetzungen enthalten sind
            String statusTableName = getStatusTableName();
            for (List<EtkDataObject> etkDataObjects : selection) {
                for (EtkDataObject etkDataObject : etkDataObjects) {
                    if (etkDataObject.getTableName().equals(statusTableName)) {
                        // Bei vererbten Ersetzungen und PRIMUS-Ersetzungen darf der Status nicht verändert werden
                        boolean inherited = etkDataObject.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DRP_INHERITED);
                        String source = etkDataObject.getFieldValue(iPartsDataVirtualFieldsDefinition.FIELD_DRP_SOURCE);
                        if (inherited || source.equals(iPartsImportDataOrigin.PRIMUS.getOrigin())) {
                            return null;
                        }
                    }
                }
            }

            iPartsDataReleaseState resultStatus = super.getStatusFromSelection(selection);
            if (resultStatus != null) {
                // Es darf sich nicht um die selbe Ersetzung handeln
                Set<iPartsDataReplacePart> collectedReplacements = new TreeSet<>(new Comparator<iPartsDataReplacePart>() {
                    @Override
                    public int compare(iPartsDataReplacePart o1, iPartsDataReplacePart o2) {
                        // Hier ist es nur wichtig, ob sie gleich sind
                        if (o1.isSameReplacement(o2)) {
                            return 0;
                        }
                        return 1;
                    }
                });
                for (List<EtkDataObject> etkDataObjects : selection) {
                    for (EtkDataObject etkDataObject : etkDataObjects) {
                        if (etkDataObject.getTableName().equals(statusTableName)) {
                            iPartsDataReplacePart replacePart = (iPartsDataReplacePart)etkDataObject;
                            if (!collectedReplacements.add(replacePart)) {
                                return null;
                            }
                        }
                    }
                }
            }
            return resultStatus;
        }
    }


    protected iPartsRelatedInfoReplacementsDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                    IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        isEditRelatedInfo = (getConnector().getMainWindow().getActiveForm() instanceof EditModuleForm) &&
                            AbstractRelatedInfoPartlistDataForm.isEditContext(getConnector(), false);

        // Bei PSK-Modulen mit gültigem BCTE-Schlüssel ist das Löschen und Editieren von Ersetzungen nicht erlaubt
        isReadOnly = !isEditContext(dataConnector, true) || (isPSKAssembly() && isPartListEntryWithValidBCTEKey());
        postCreateGui();
    }

    @Override
    public boolean hasElementsToShow() {
        return (predecessorDataGrid.getTable().getRowCount() > 0) ||
               (successorDataGrid.getTable().getRowCount() > 0);

    }

    @Override
    protected EtkDisplayFields getDisplayFields(String configKey) {
        EtkDisplayFields displayFields = super.getDisplayFields(configKey);
        // Virtuelle Felder für PRIMUS-Hinweis-Codes nur entfernen, wenn die Option am Produkt aktiv ist (also keine
        // Ausgabe der PRIMUS Hinweise gewünscht ist)
        EtkDataPartListEntry relatedInfoPLE = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        if (relatedInfoPLE != null) {
            EtkDataAssembly ownerAssembly = relatedInfoPLE.getOwnerAssembly();
            if (ownerAssembly instanceof iPartsDataAssembly) {
                iPartsProductId productId = ((iPartsDataAssembly)ownerAssembly).getProductIdFromModuleUsage();
                if ((productId != null) && iPartsProduct.getInstance(getProject(), productId).isNoPrimusHints()) {
                    displayFields.removeField(iPartsConst.TABLE_DA_REPLACE_PART, iPartsDataVirtualFieldsDefinition.DRP_PSS_CODE_FORWARD, false);
                    displayFields.removeField(iPartsConst.TABLE_DA_REPLACE_PART, iPartsDataVirtualFieldsDefinition.DRP_PSS_CODE_BACK, false);
                }
            }
        }

        return displayFields;
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        // Die Unterscheidungsmöglichkeit bei createDefaultDisplayFields() für die unterschiedlichen Grids
        // verschiedene Spalten anzeigen zu können.
        if (configKey.equals(CONFIG_KEY_REPLACEMENTS_PREDECESSOR_SUCCESSOR_DATA)) {
            return createPredecessorSuccessorDefaultDisplayFields();
        } else if (configKey.equals(CONFIG_KEY_REPLACEMENTS_CURRENT_DATA)) {
            return createCurrentDataDefaultDisplayFields();
        } else if (configKey.equals(CONFIG_KEY_REPLACEMENTS_EDIT_PARTLIST)) {
            return createEditDefaultDisplayFields();
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

        addDisplayField(list, iPartsConst.TABLE_MAT, iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR, false, false, true); // Teilenummer
        addDisplayField(list, iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR, true, false, true); // Benennung

        // virtuelle Felder:
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO, false, false, false); // virtuelle DIALOG Baureuhe
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_HM, false, false, false); // virtuelle DIALOG HM
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_M, false, false, false); // virtuelle DIALOG M
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SM, false, false, false); // virtuelle DIALOG SM
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE, false, false, false); // virtuelle DIALOG POSE
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV, false, false, false); // virtuelle DIALOG POSV
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_WW, false, false, false); // virtuelles DIALOG Wahlweise
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_ETKZ, false, false, false); // virtueller DIALOG Ersatzteilkennzeichen

        // berechnete Felder:
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_PART, iPartsDataVirtualFieldsDefinition.DRP_FACTORY_DATA_AVAILABLE, false, false, false);
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
        addDisplayField(list, iPartsConst.TABLE_MAT, iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR, true, false, true); // Benennung
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_PART, iPartsConst.FIELD_DRP_REPLACE_RFMEA, false, false, false); // RFMEA
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_PART, iPartsConst.FIELD_DRP_REPLACE_RFMEN, false, false, false); // RFMEN

        // virtuelle Felder:
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE, false, false, false); // virtuelle DIALOG POSE
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV, false, false, false); // virtuelle DIALOG POSV
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_WW, false, false, false); // virtuelles DIALOG Wahlweise
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_ETKZ, false, false, false); // virtueller DIALOG Ersatzteilkennzeichen

        // berechnete Felder:
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_PART, iPartsDataVirtualFieldsDefinition.DRP_INCLUDE_PARTS_AVAILABLE, false, false, false);
        addDisplayField(list, iPartsConst.TABLE_DA_REPLACE_PART, iPartsDataVirtualFieldsDefinition.DRP_FACTORY_DATA_AVAILABLE, false, false, false);
        return list;
    }

    private List<EtkDisplayField> createEditDefaultDisplayFields() {
        List<EtkDisplayField> list = new ArrayList<>();
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_POS, false, false, true); // Hotspot
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_HIERARCHY, false, false, true); // Einrückung
        addDisplayField(list, iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR, false, false, true);// TeileNr.
        addDisplayField(list, iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR, true, false, true);// Benennung
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_MENGE, false, false, true); // Menge
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_AA, false, false, true);// Ausführung
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_STEERING, false, false, true);// Lenkung
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES, false, false, true);// Gültigkeit bei Code
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_DATEFROM, false, false, true);// Datum ab
        addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_DATETO, false, false, true);// Datum bis

        if (Constants.DEVELOPMENT) {
            addDisplayField(list, iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_LFDNR, false, false, true);
        }

        return list;
    }

    @Override
    public void dataChanged() {
        super.dataChanged();

        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            if (((iPartsRelatedInfoEditContext)getConnector().getEditContext()).isUpdateReplacements()) {
                dataToGrid();
            }
        }
    }

    /**
     * Anzeige der Daten
     */
    protected void dataToGrid() {
        clearGrids();

        // Die aktuellen Daten aus der Stückliste in das dafür vorgesehenen Grid übertragen inkl. berechnetem Wert für das
        // virtuelle Feld "Werkseinsatzdaten vorhanden" mit einem Dummy iPartsDataReplacePart-Objekt für das Grid
        iPartsDataReplacePart dummyDataReplacePart = new iPartsDataReplacePart(getProject(), new iPartsReplacePartId());
        dummyDataReplacePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        addFactoryDataAvailableValue(partListEntry, dummyDataReplacePart, false);
        boolean oldLogLoadFieldIfNeeded = partListEntry.isLogLoadFieldIfNeeded();
        partListEntry.setLogLoadFieldIfNeeded(false);
        try {
            currentDataGrid.addObjectToGrid(partListEntry, dummyDataReplacePart);
        } finally {
            partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
        }

        boolean isFilterActive = !isEditRelatedInfo && iPartsRelatedInfoFormConnectorWithFilterSettings.isFilterActive(getConnector());

        if (partListEntry instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;

            List<iPartsReplacement> predecessorReplacementList;
            List<iPartsReplacement> successorReplacementList;
            if (!isEditRelatedInfo) {
                predecessorReplacementList = iPartsPartListEntry.getPredecessors(isFilterActive);
                successorReplacementList = iPartsPartListEntry.getSuccessors(isFilterActive);
            } else {
                predecessorReplacementList = new DwList<>();
                successorReplacementList = new DwList<>();
                iPartsReplacementHelper.loadReplacementsForPartListEntry(null, predecessorReplacementList, successorReplacementList,
                                                                         iPartsPartListEntry, true);
            }

            // Vorgänger-Daten ins Grid übertragen.
            if (predecessorReplacementList != null) {
                for (iPartsReplacement replacementListItem : predecessorReplacementList) {
                    ((ReplacementDataObjectGrid)predecessorDataGrid).addReplacementToGrid(replacementListItem.predecessorEntry,
                                                                                          replacementListItem);
                }
            }

            // Nachfolger-Daten ins Grid übertragen.
            if (successorReplacementList != null) {
                for (iPartsReplacement replacementListItem : successorReplacementList) {
                    EtkDataPartListEntry successorEntry = replacementListItem.successorEntry;

                    // successorEntry kann null sein ==> Teil manuell erzeugen
                    if (successorEntry == null) {
                        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), replacementListItem.successorMappedPartNumber, "");
                        part.setFieldValue(iPartsConst.FIELD_M_BESTNR, replacementListItem.successorMappedPartNumber, DBActionOrigin.FROM_DB);
                        ((ReplacementDataObjectGrid)successorDataGrid).addReplacementToGrid(part, replacementListItem);
                        // Wenn der Eintrag nicht in der Stückliste ist, dann muss er auch nicht separat behandelt werden
                    } else {
                        ((ReplacementDataObjectGrid)successorDataGrid).addReplacementToGrid(successorEntry, replacementListItem);
                    }
                }
            }

            //Nochmal nach Vorgänger/Nachfolger suchen die CHECK_NOT_RELEVANT sind, aber lesend sein müssen
            changeCheckNotRelevantToReadOnly(predecessorDataGrid);
            changeCheckNotRelevantToReadOnly(successorDataGrid);
        }

        predecessorDataGrid.showNoResultsLabel(predecessorDataGrid.getTable().getRowCount() == 0);
        successorDataGrid.showNoResultsLabel(successorDataGrid.getTable().getRowCount() == 0);
    }

    private void changeCheckNotRelevantToReadOnly(AbstractReplacementDataObjectGrid grid) {
        List<iPartsDataReplacePart> replacements = grid.getDataObjectList(iPartsDataReplacePart.class);
        List<EtkDataPartListEntry> partListEntryInfoReplacement = grid.getDataObjectList(EtkDataPartListEntry.class);
        boolean changedReplacement = false;
        for (iPartsDataReplacePart successor : replacements) {
            if (successor.getFieldValue(getStatusFieldName()).equals(iPartsDataReleaseState.CHECK_NOT_RELEVANT.getDbValue())) {
                // Gibt es zum "nicht relevant zur Prüdung" einen Datensatz mit "neu (lesend)", dann setze "nicht relevant zur Prüdung (lesend)"
                iPartsDataReplacePart sameReplacement = findSameReplacementInList(successor, replacements);
                if (sameReplacement != null && sameReplacement.getFieldValue(getStatusFieldName()).equals(iPartsDataReleaseState.NEW_READONLY.getDbValue())) {
                    successor.setFieldValue(getStatusFieldName(), iPartsDataReleaseState.CHECK_NOT_RELEVANT_READONLY.getDbValue(), DBActionOrigin.FROM_EDIT);
                    changedReplacement = true;
                }
            }
        }

        if (changedReplacement) {
            grid.clearGrid();
            for (int i = 0; i < replacements.size(); i++) {
                if (!partListEntryInfoReplacement.isEmpty()) {
                    grid.addObjectToGrid(partListEntryInfoReplacement.get(i), replacements.get(i));
                } else {
                    // Sollte nicht vorkommen
                    grid.addObjectToGrid(replacements.get(i));
                }
            }
        }
    }

    /**
     * Finde die gleiche Ersetzung ohne die Sequenznummer zu berücksichtigen
     *
     * @param toFind
     * @param replaceParts
     * @return
     */
    private iPartsDataReplacePart findSameReplacementInList(iPartsDataReplacePart toFind, List<iPartsDataReplacePart> replaceParts) {
        for (iPartsDataReplacePart replacePart : replaceParts) {
            if (toFind.isSameReplacement(replacePart) && !toFind.isDuplicateOf(replacePart)) {
                return replacePart;
            }
        }
        return null;
    }

    private void statusChangedForDataObject(iPartsDataReplacePart replacePart, boolean isReplacementNew, boolean isReplacementDeleted) {
        iPartsDataReplacePartList replacePartList = new iPartsDataReplacePartList();
        replacePartList.add(replacePart, DBActionOrigin.FROM_EDIT);
        statusChangedForDataObject(replacePartList, isReplacementNew, isReplacementDeleted);
    }

    private void statusChangedForDataObject(iPartsDataReplacePartList replacePartList, boolean isReplacementNew, boolean isReplacementDeleted) {
        List<iPartsDataReplacePart> allReplacementsDataObjectsOfGrid = successorDataGrid.getDataObjectList(iPartsDataReplacePart.class);
        EtkDataObjectList<iPartsDataReplacePart> changedDataObjects = iPartsRelatedEditHelper.updateStatusValuesForReplacements(replacePartList, allReplacementsDataObjectsOfGrid,
                                                                                                                                getStatusFieldName());

        // Bei PSK-Modulen auf keinen Fall Status-Änderungen vererben (Edit dürfte bei DIALOG-Ersetzungen ja gar nicht erst
        // möglich sein)
        if (saveReplacementWithUpdate(changedDataObjects, isReplacementNew, isReplacementDeleted) && !isPSKAssembly()) {
            iPartsRelatedEditHelper.statusChanged(changedDataObjects, this, true);
        }
    }

    /**
     * Die DIALOG_CHANGES IDs finden für den Stücklisteneintrag und seinen Nachfolgern
     *
     * @param dataObject
     * @return
     */
    @Override
    public List<iPartsDataDIALOGChange> getDataDIALOGChanges(EtkDataObject dataObject) {
        List<iPartsDataDIALOGChange> result = new DwList<>();
        if (isRelatedInfoEditContext() && (dataObject instanceof iPartsDataReplacePart)) {
            // Vorgänger
            iPartsDialogBCTEPrimaryKey predecessorBCTE = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
            if (predecessorBCTE != null) {
                iPartsDialogChangesId predecessorDialogChangesId = new iPartsDialogChangesId(iPartsDataDIALOGChange.ChangeType.REPLACEMENT_AS,
                                                                                             dataObject.getAsId(), predecessorBCTE.getHmMSmId().getSeries(),
                                                                                             predecessorBCTE.createDialogGUID(), "",
                                                                                             partListEntry.getAsId().toDBString());
                result.add(new iPartsDataDIALOGChange(getProject(), predecessorDialogChangesId));
            }

            // Nachfolger
            String replaceLfdNr = dataObject.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_LFDNR);
            if (!replaceLfdNr.isEmpty()) {
                EtkDataPartListEntry successor = partListEntry.getOwnerAssembly().getPartListEntryFromKLfdNr(replaceLfdNr);
                iPartsDialogBCTEPrimaryKey successorBCTE = null;
                if (successor != null) {
                    successorBCTE = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(successor);
                }
                if (successorBCTE != null) {
                    iPartsDialogChangesId successorDialogChangesId = new iPartsDialogChangesId(iPartsDataDIALOGChange.ChangeType.REPLACEMENT_AS,
                                                                                               dataObject.getAsId(), successorBCTE.getHmMSmId().getSeries(),
                                                                                               successorBCTE.createDialogGUID(), "",
                                                                                               successor.getAsId().toDBString());
                    result.add(new iPartsDataDIALOGChange(getProject(), successorDialogChangesId));
                }
            }
        }

        return result;
    }

    private void addIncludePartsAvailableValue(iPartsReplacement replacement, EtkDataObject gridObject) {
        addVirtualBooleanValue(gridObject, iPartsDataVirtualFieldsDefinition.DRP_INCLUDE_PARTS_AVAILABLE,
                               replacement.hasIncludeParts(getProject()));
    }

    private void addFactoryDataAvailableValue(EtkDataObject dataObject, EtkDataObject gridObject, boolean isPrimus) {
        if (dataObject instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry entry = (iPartsDataPartListEntry)dataObject;
            if (isPrimus) {
                addVirtualBooleanValue(gridObject, iPartsDataVirtualFieldsDefinition.DRP_FACTORY_DATA_AVAILABLE, false);
            } else {
                addVirtualBooleanValue(gridObject, iPartsDataVirtualFieldsDefinition.DRP_FACTORY_DATA_AVAILABLE,
                                       entry.isValidFactoryDataRelevantForEndNumberFilter());
            }
        }
    }

    private void addInheritedValue(iPartsReplacement replacement, EtkDataObject gridObject) {
        addVirtualBooleanValue(gridObject, iPartsDataVirtualFieldsDefinition.DRP_INHERITED, replacement.isVirtualInherited());
    }

    private void addVirtualBooleanValue(EtkDataObject gridObject, String fieldName, boolean value) {
        gridObject.getAttributes().addField(fieldName, SQLStringConvert.booleanToPPString(value), true, DBActionOrigin.FROM_DB);

    }

    // Edit Funktionen
    private void removeSuccessor(Event event) {
        final iPartsDataReplacePartList selectedReplacements = ((ReplacementDataObjectGrid)successorDataGrid).getMultiSelectionReplacement();

        if (selectedReplacements.isEmpty()) {
            return;
        }

        saveReplacementWithUpdate(selectedReplacements, false, true);

        // Evtl. vorhandene Primärschlüssel-Reservierungen für die gelöschten Ersetzungen ebenfalls löschen beim Speichern
        // der RelatedEdit
        addSaveEditRunnable(new Runnable() {
            @Override
            public void run() {
                for (iPartsDataReplacePart dataReplacePart : selectedReplacements) {
                    iPartsDataReservedPKList.deleteReservedPrimaryKey(getProject(), dataReplacePart.getAsId());
                }
            }
        });
    }

    private void editSuccessor(Event event) {
        EtkDisplayFields displayFields = getDisplayFields(CONFIG_KEY_REPLACEMENTS_EDIT_PARTLIST);
        iPartsReplacement selectedReplacement = ((ReplacementDataObjectGrid)successorDataGrid).getSelectedReplacement();
        if (selectedReplacement == null) {
            return;
        }
        iPartsReplacePartId selectedId = selectedReplacement.getAsReplacePartId();
        iPartsDataReplacePart selectedData = new iPartsDataReplacePart(getProject(), selectedId);
        selectedData.loadFromDB(selectedId);

        List<iPartsDataReplacePart> allReplacementsDataObjectsOfGrid = successorDataGrid.getDataObjectList(iPartsDataReplacePart.class);
        iPartsDataReplacePartList list = new iPartsDataReplacePartList();
        list.addAll(allReplacementsDataObjectsOfGrid, DBActionOrigin.FROM_DB);

        boolean wasIPARTS = selectedData.getFieldValue(getSourceFieldName()).equals(iPartsImportDataOrigin.IPARTS.getOrigin());

        EditModuleFormIConnector editConnector = iPartsRelatedEditHelper.createEditConnector(getConnector(), partListEntry.getOwnerAssembly());
        iPartsDataReplacePart editedReplacement = EditSelectReplacementForm.showSelectReplacementForm(getProject(), editConnector,
                                                                                                      this, selectedData, list,
                                                                                                      false, displayFields);
        if (editConnector != getConnector()) {
            editConnector.dispose();
        }
        if (editedReplacement != null) {
            if (!wasIPARTS) {
                editedReplacement.setFieldValue(getSourceFieldName(), iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);
                statusChangedForDataObject(editedReplacement, true, false);
                addEditRunnablesForReservedPK(editedReplacement.getAsId());
            } else {
                saveReplacementWithUpdate(editedReplacement, false, false);
                // Hier ändert sich die Sequenznummer der Ersetzung nicht -> addEditRunnablesForReservedReplacementPK()
                // ist nicht notwendig
            }
        }
    }

    private void addSuccessor(Event event) {
        EtkDisplayFields displayFields = getDisplayFields(CONFIG_KEY_REPLACEMENTS_EDIT_PARTLIST);

        PartListEntryId partListEntryId = partListEntry.getAsId();

        // Sequenznummer wird in EditSelectReplacementForm.showSelectReplacementForm() bei OK korrekt gesetzt, hier
        // zunächst nur ein Dummy, um keine unnötige PK-Reservierung zu erzeugen, die in EditSelectReplacementForm.showSelectReplacementForm()
        // dann sogar berücksichtigt werden würde
        iPartsReplacePartId newReplacementId = new iPartsReplacePartId(partListEntryId, EtkDbsHelper.formatLfdNr(0));

        iPartsDataReplacePart newReplacement = new iPartsDataReplacePart(getProject(), newReplacementId);
        newReplacement.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

        List<iPartsDataReplacePart> allReplacementsDataObjectsOfGrid = successorDataGrid.getDataObjectList(iPartsDataReplacePart.class);
        iPartsDataReplacePartList list = new iPartsDataReplacePartList();
        list.addAll(allReplacementsDataObjectsOfGrid, DBActionOrigin.FROM_DB);

        EditModuleFormIConnector editConnector = iPartsRelatedEditHelper.createEditConnector(getConnector(), partListEntry.getOwnerAssembly());
        newReplacement = EditSelectReplacementForm.showSelectReplacementForm(getProject(), editConnector, this, newReplacement,
                                                                             list, true, displayFields);
        if (editConnector != getConnector()) {
            editConnector.dispose();
        }
        if (newReplacement != null) {
            newReplacement.setFieldValue(getSourceFieldName(), iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);
            newReplacement.setFieldValue(getStatusFieldName(), iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            statusChangedForDataObject(newReplacement, true, false);
            addEditRunnablesForReservedPK(newReplacement.getAsId());
        }
    }

    private boolean saveReplacementWithUpdate(iPartsDataReplacePart replacements, boolean replacementIsNew, boolean replacementIsDeleted) {
        EtkDataObjectList<iPartsDataReplacePart> list = new GenericEtkDataObjectList();
        list.add(replacements, DBActionOrigin.FROM_EDIT);
        return saveReplacementWithUpdate(list, replacementIsNew, replacementIsDeleted);
    }

    private boolean saveReplacementWithUpdate(EtkDataObjectList<iPartsDataReplacePart> replacements, boolean replacementIsNew,
                                              boolean replacementIsDeleted) {
        if (!(partListEntry instanceof iPartsDataPartListEntry)) {
            return false;
        }

        iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
        boolean seriesIsRelevantForImport = iPartsPartListEntry.isSeriesRelevantForImport();
        iPartsReplacementKEMHelper replacementKEMHelper = null;
        if (seriesIsRelevantForImport) {
            DBDataObjectList<EtkDataPartListEntry> partListEntries = iPartsPartListEntry.getOwnerAssembly().getPartListUnfiltered(null);
            replacementKEMHelper = new iPartsReplacementKEMHelper(partListEntries);
        }

        boolean oldEvalPEMToAtPartListEntry = iPartsPartListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED);
        // Für alle geänderten Datensätze müssen die PEM Flags neu berechnet werden
        // Und jeder muss im ChangeSet gespeichert werden
        for (iPartsDataReplacePart replacement : replacements) {
            EtkDataPartListEntry successorEntry = iPartsPartListEntry.getOwnerAssembly().getPartListEntryFromKLfdNrUnfiltered(replacement.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_LFDNR));
            if (!(successorEntry instanceof iPartsDataPartListEntry)) {
                return false;
            }
            final iPartsDataPartListEntry successor = (iPartsDataPartListEntry)successorEntry;

            // Die Ersetzung (gelöscht oder neu/verändert) zum ChangeSet hinzufügen
            GenericEtkDataObjectList changeSetDataObjectList = new GenericEtkDataObjectList();
            if (replacementIsDeleted) {
                changeSetDataObjectList.delete(replacement, true, DBActionOrigin.FROM_EDIT);
            } else {
                changeSetDataObjectList.add(replacement, DBActionOrigin.FROM_EDIT);
            }

            // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden, aber nur beim selektierten Stücklisteneintrag
            iPartsDataPartListEntry.resetAutoCreatedFlag(iPartsPartListEntry);
            // Vorgänger und Nachfolger müssen auf jeden Fall als geändert ins ChangeSet
            iPartsPartListEntry.getAttributes().markAsModified();
            changeSetDataObjectList.add(iPartsPartListEntry, DBActionOrigin.FROM_EDIT);
            successor.getAttributes().markAsModified();
            changeSetDataObjectList.add(successor, DBActionOrigin.FROM_EDIT);
            List<iPartsDataPartListEntry> previousVersionsOfCurrentEntry = null;
            List<iPartsDataPartListEntry> nextVersionsOfSuccessor = null;

            setModifiedByEdit(true);

            // Bei nicht versorgungsrelevanten Baureihen müssen die "PEM ab/bis auswerten"-Flags angepasst sowie der
            // Nachfolger aktiv verändert werden inkl. Runnable für den Abbruch; bei versorgungsrelevanten Baureihen
            // wird dies jeweils nach dem Schließen der RelatedEdit gemacht, da auch Vorgänger- und Nachfolgerstände
            // betroffen sind
            if (!seriesIsRelevantForImport) {
                final boolean oldEvalPEMFromAtSuccessor = successorEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_FROM);
                if (replacementIsNew && replacement.isNew()) {
                    iPartsReplacementHelper.setEvalPEMToAndPEMFrom(iPartsPartListEntry, successorEntry, replacement, seriesIsRelevantForImport);
                } else {
                    iPartsReplacementHelper.calculateEvalPEMToAndPEMFrom(iPartsPartListEntry, successor, replacement, replacementIsDeleted,
                                                                         seriesIsRelevantForImport);
                }

                if (oldEvalPEMFromAtSuccessor != successor.getFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_FROM)) {

                    // "PEM ab auswerten" am Nachfolger hat sich geändert, also "PEM ab auswerten" an dessen Werkseinsatzdaten korrigieren
                    iPartsFactoryData factoryDataUnfiltered = successor.getFactoryDataForRetailUnfiltered();
                    if (factoryDataUnfiltered != null) {
                        factoryDataUnfiltered.setEvalPemFrom(successor.getFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_FROM));
                        successor.setFactoryDataForRetailUnfiltered(factoryDataUnfiltered); // enthält auch clearFactoryDataForRetail()
                    }

                    // Beim Abbrechen vom Edit muss das Ändern vom Flag "PEM ab auswerten" rückgängig gemacht werden und die Ersetzungen
                    // sowie Retail-Werkseinsatzdaten neu bestimmt werden
                    addCancelEditRunnable(new Runnable() {
                        @Override
                        public void run() {
                            successor.setFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_FROM, oldEvalPEMFromAtSuccessor, DBActionOrigin.FROM_DB);

                            // Flag "PEM ab auswerten" an den Werkseinsatzdaten korrigieren
                            iPartsFactoryData factoryDataUnfiltered = successor.getFactoryDataForRetailUnfiltered();
                            if (factoryDataUnfiltered != null) {
                                factoryDataUnfiltered.setEvalPemFrom(oldEvalPEMFromAtSuccessor);
                                successor.setFactoryDataForRetailUnfiltered(factoryDataUnfiltered); // enthält auch clearFactoryDataForRetail()
                            }

                            successor.clearReplacements();
                        }
                    });
                }
            } else {
                // Alle Vorgängerstände zum aktuellen Stücklisteneintrag sowie Nachfolgerstände zum Nachfolger ermitteln
                // und zum ChangeSet hinzufügen
                previousVersionsOfCurrentEntry = replacementKEMHelper.getEntriesForAllKEMs(iPartsPartListEntry, true, true);
                if (!previousVersionsOfCurrentEntry.isEmpty()) {
                    for (iPartsDataPartListEntry previousVersionOfCurrentEntry : previousVersionsOfCurrentEntry) {
                        previousVersionOfCurrentEntry.getAttributes().markAsModified();
                    }
                    changeSetDataObjectList.addAll(previousVersionsOfCurrentEntry, DBActionOrigin.FROM_EDIT);
                }
                nextVersionsOfSuccessor = replacementKEMHelper.getEntriesForAllKEMs(successor, false, true);
                if (!nextVersionsOfSuccessor.isEmpty()) {
                    for (iPartsDataPartListEntry nextVersionOfSuccessor : nextVersionsOfSuccessor) {
                        nextVersionOfSuccessor.getAttributes().markAsModified();
                    }
                    changeSetDataObjectList.addAll(nextVersionsOfSuccessor, DBActionOrigin.FROM_EDIT);
                }
            }
            addDataObjectListToActiveChangeSetForEdit(changeSetDataObjectList);

            // DAIMLER-10832 Truck: Vererbung von Attributen vom Vorgänger auf den Nachfolger bei Erzeugung von Ersetzungen
            if (replacementIsNew && iPartsPartListEntry.getOwnerAssembly().getDocumentationType().isTruckDocumentationType()) {
                addSaveEditRunnable(() -> {
                    GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();

                    // AS Strukturstufe --> wird "vereinheitlicht"
                    successor.setFieldValue(iPartsConst.FIELD_K_HIERARCHY, iPartsPartListEntry.getFieldValue(iPartsConst.FIELD_K_HIERARCHY),
                                            DBActionOrigin.FROM_EDIT);

                    // kombinierter Text --> wird "vereinheitlicht"
                    iPartsDataCombTextList successorNewCombTextList = iPartsPartListEntry.replaceCombTextAtTarget(successor);

                    // Menge --> wird "vereinheitlicht"
                    successor.setFieldValue(iPartsConst.FIELD_K_MENGE, iPartsPartListEntry.getFieldValue(iPartsConst.FIELD_K_MENGE),
                                            DBActionOrigin.FROM_EDIT);

                    // Coderegel --> wird "vereinheitlicht"
                    successor.setFieldValue(iPartsConst.FIELD_K_CODES, iPartsPartListEntry.getFieldValue(iPartsConst.FIELD_K_CODES),
                                            DBActionOrigin.FROM_EDIT);
                    successor.calculateRetailCodesReducedAndFiltered(iPartsFilter.get());

                    // SAA/BK-Gültigkeit --> Additiv
                    iPartsPartListEntry.addArrayValuesToTarget(successor, iPartsConst.FIELD_K_SA_VALIDITY);

                    // BM-Gültigkeit --> Additiv
                    iPartsPartListEntry.addArrayValuesToTarget(successor, iPartsConst.FIELD_K_MODEL_VALIDITY);

                    // Fußnoten --> Additiv
                    iPartsDataFootNoteCatalogueRefList successorNewFootNoteList = iPartsPartListEntry.addFootNotesToTarget(successor);

                    if (successor.isModifiedWithChildren()) {
                        modifiedDataObjects.add(successor, DBActionOrigin.FROM_EDIT);
                    }

                    boolean combTextModified = (successorNewCombTextList != null) && !successorNewCombTextList.isEmptyIncludingDeletedList();
                    if (combTextModified) {
                        modifiedDataObjects.addAll(successorNewCombTextList, false, true, DBActionOrigin.FROM_EDIT);
                    }

                    if ((successorNewFootNoteList != null) && successorNewFootNoteList.isModifiedWithChildren()) {
                        modifiedDataObjects.addAll(successorNewFootNoteList, DBActionOrigin.FROM_EDIT);
                    }

                    addDataObjectListToActiveChangeSetForEdit(modifiedDataObjects);
                    if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
                        iPartsRelatedInfoEditContext editContext = ((iPartsRelatedInfoEditContext)getConnector().getEditContext());
                        editContext.setUpdateEditAssemblyData(true);
                    }

                    if (combTextModified) {
                        // Kombinierten Text am Nachfolger zurücksetzen
                        successor.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);
                        successor.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT);
                        successor.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL);
                    }
                });
            }

            // Wird gebraucht, damit der Dialog die Änderungen zeigt. Die Stückliste und die Ersetzungen werden dann über den
            // EditContext neu aufgebaut.
            iPartsPartListEntry.clearReplacements();
            iPartsPartListEntry.clearFactoryDataForRetail();
            iPartsPartListEntry.clearFootnotes();

            successor.clearReplacements();
            successor.clearFactoryDataForRetail();

            if (seriesIsRelevantForImport) {
                // Vererbte Ersetzungen für Vorgänger- Nachfolgerstände nur bei versorgungsrelevanten Baureihen aktualisieren
                for (iPartsDataPartListEntry previousVersionOfCurrentEntry : previousVersionsOfCurrentEntry) {
                    // An jedem Vorgängerstand die Liste der Nachfolger und Retail-Werkseinsatzdaten zurücksetzen zur Neubestimmung
                    previousVersionOfCurrentEntry.clearReplacements();
                    previousVersionOfCurrentEntry.clearFactoryDataForRetail();
                }
                for (iPartsDataPartListEntry nextVersionOfSuccessor : nextVersionsOfSuccessor) {
                    // An jedem Nachfolgerstand die Liste der Vorgänger und Retail-Werkseinsatzdaten zurücksetzen zur Neubestimmung
                    nextVersionOfSuccessor.clearReplacements();
                    nextVersionOfSuccessor.clearFactoryDataForRetail();
                }
            }
        }
        iPartsPartListEntry.updatePEMFlagsFromReplacements();

        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            iPartsRelatedInfoEditContext editContext = ((iPartsRelatedInfoEditContext)getConnector().getEditContext());
            editContext.setFireDataChangedEvent(true);
            editContext.setUpdateReplacements(true);
            editContext.setUpdateRetailFactoryData(true); // für vererbte Werkseinsatzdaten benötigt
        }

        boolean newEvalPEMToAtPartListEntry = iPartsPartListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED);
        if (oldEvalPEMToAtPartListEntry != newEvalPEMToAtPartListEntry) {
            // Flag "PEM bis auswerten" an den Werkseinsatzdaten korrigieren
            iPartsFactoryData factoryDataUnfiltered = iPartsPartListEntry.getFactoryDataForRetailUnfiltered();
            if (factoryDataUnfiltered != null) {
                factoryDataUnfiltered.setEvalPemTo(newEvalPEMToAtPartListEntry);
                iPartsPartListEntry.setFactoryDataForRetailUnfiltered(factoryDataUnfiltered); // enthält auch clearFactoryDataForRetail()
            }

            if (!seriesIsRelevantForImport) {
                // Außerdem der iPartsRelatedInfoEditPartListEntryDataForm Bescheid sagen, da sich in ihr das PEM-bis-Auswerten-Flag
                // geändert hat
                setModifiedByEdit(iPartsConst.CONFIG_KEY_RELATED_INFO_EDIT_PART_LIST_ENTRY_DATA, true);
            }

            // Views updaten, damit auch der Stücklisten-Related-Info geupdatet wird, da er sonst nichts mitkriegt falls
            // er vorher schon geöffnet war
            getConnector().updateAllViews(this, true);
        } else {
            // RelatedInfo-Daten updaten
            getConnector().dataChanged(null);
        }

        return true;
    }

    @Override
    protected void addDataGrids(GuiPanel panelPredecessorData, GuiPanel panelCurrentData, GuiPanel panelSuccessorData) {
        // Vorgänger-DataGrid erzeugen, die Position festlegen und auf das Panel pappen.
        predecessorDataGrid = new ReplacementDataObjectGrid(getConnector(), this, REPLACEMENT_GRID_TYPE.PREDECESSOR, null, null);
        predecessorDataGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        panelPredecessorData.addChild(predecessorDataGrid.getGui());

        // Aktuelle Daten-DataGrid erzeugen, die Position festlegen und auf das Panel pappen.
        currentDataGrid = new ReplacementDataObjectGrid(getConnector(), this, REPLACEMENT_GRID_TYPE.CURRENT, null, null);
        currentDataGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        panelCurrentData.addChild(currentDataGrid.getGui());

        // Nachfolger-DataGrid erzeugen, die Position festlegen und auf das Panel pappen.
        successorDataGrid = new ReplacementDataObjectGrid(getConnector(), this, REPLACEMENT_GRID_TYPE.SUCCESSOR, iPartsConst.TABLE_DA_REPLACE_PART,
                                                          iPartsConst.FIELD_DRP_STATUS);
        successorDataGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        successorDataGrid.updateContextMenu(null, isReadOnly);
        panelSuccessorData.addChild(successorDataGrid.getGui());

        // Die anzuzeigenden Spalten der einzelnen Grids setzen.
        predecessorDataGrid.setDisplayFields(getDisplayFields(CONFIG_KEY_REPLACEMENTS_PREDECESSOR_SUCCESSOR_DATA));
        currentDataGrid.setDisplayFields(getDisplayFields(CONFIG_KEY_REPLACEMENTS_CURRENT_DATA));
        successorDataGrid.setDisplayFields(getDisplayFields(CONFIG_KEY_REPLACEMENTS_PREDECESSOR_SUCCESSOR_DATA));
    }
}