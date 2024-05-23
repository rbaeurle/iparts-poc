/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractTwoDataObjectGridsForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.FactoryDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

/**
 * Formular für die Anzeige der DIALOG mehrstufigen Retail-Ersetzungskette innerhalb der Related Info (inkl. Mitlieferdaten)
 */
public class iPartsRelatedInfoDialogMultistageRetailReplacementChainForm extends AbstractTwoDataObjectGridsForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_DIALOG_RETAIL_REPLACEMENT_CHAIN = "iPartsMenuItemShowDialogRetailReplacementChain";
    public static final String CONFIG_KEY_DIALOG_RETAIL_REPLACE_PARTS_CHAIN = "Plugin/iPartsEdit/DialogRetailReplacePartsChain";
    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.DialogRetail, iPartsModuleTypes.PSK_PKW);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_DIALOG_RETAIL_REPLACEMENT_CHAIN,
                                iPartsConst.RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN_TEXT,
                                EditDefaultImages.edit_rep_chain_both.getImage(),
                                iPartsConst.CONFIG_KEY_RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu) {
        boolean menuItemVisible = false;
        // Soll nur im TU-Edit gezeigt werden. Der Menüeintrag wird als menuItem in EditAssemblyListForm hinzugefügt
        // Dort wird auch geregelt, ob der Menüpunkt zu sehen ist oder nicht
        // Separator und Menüeintrag aktualisieren
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_DIALOG_RETAIL_REPLACEMENT_CHAIN, menuItemVisible);
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry entry) {
        if ((entry != null) && relatedInfoIsVisible(entry.getOwnerAssembly(), VALID_MODULE_TYPES)) {
            if (entry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsEntry = ((iPartsDataPartListEntry)entry);
                // Es können keine Filter aktiv sein, da die Form nur im Edit aktiv ist
                return iPartsEntry.hasPredecessors(false) || iPartsEntry.hasSuccessors(false);
            }
        }
        return false;
    }

    protected EtkDataPartListEntry partListEntry;
    private GuiMenuItem includePartsMenuItem;
    // Wurde der Retailfilter gewählt, bleibt bis auf die Werkseindatzdaten der Inhalt der Zeilen gleich
    // Damit die Ersatzkette nicht immer wieder neu geladen werden muss, müssen die Daten pro Zeile gespeichert werden
    private List<DataObjectContainer> rowsOfDataObjectsWithoutFactoryData = new ArrayList<>();
    private boolean isRetailFilterOld = false;
    private final String NO_FACTORYDATA_ID = "noFactoryDataId";

    protected iPartsRelatedInfoDialogMultistageRetailReplacementChainForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, CONFIG_KEY_DIALOG_RETAIL_REPLACE_PARTS_CHAIN, RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN_TEXT, "", "", false);
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();
        setGridBottomVisible(false);
        setCheckboxRetailFilterVisible(true);
        gridTop.setMarkColor(iPartsEditPlugin.clPlugin_iPartsEdit_MultistageReplacementChainBackgroundColor.getColor());
    }

    @Override
    protected DataObjectFilterGrid createGrid(boolean top) {
        if (top) {
            DataObjectFilterGrid myGrid = new DataObjectFilterGrid(getConnector(), this) {
                @Override
                protected void createContextMenuItems(GuiContextMenu contextMenu) {
                    includePartsMenuItem = new GuiMenuItem();
                    includePartsMenuItem.setText(AbstractRelatedInfoReplacementDataForm.INCLUDE_IPARTSMENU_TEXT);
                    includePartsMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            iPartsReplacement replacement = getSelectedReplacement();
                            if (replacement != null) {
                                iPartsReplacementsIncludePartsForm form = new iPartsReplacementsIncludePartsForm((RelatedInfoFormConnector)parentForm.getConnector(),
                                                                                                                 parentForm, relatedInfo, replacement, true);
                                form.showModal();
                            }
                        }
                    });
                    contextMenu.addChild(includePartsMenuItem);
                    includePartsMenuItem.setEnabled(false);
                }

                @Override
                protected void onTableSelectionChanged(Event event) {
                    boolean isSingleSelected = isSingleSelected();
                    iPartsReplacement replacement = getSelectedReplacement();
                    if (replacement != null) {
                        includePartsMenuItem.setEnabled(isSingleSelected && replacement.hasIncludeParts(getProject()));
                    }
                }
            };
            // speziell für Sortierung aufheben
            myGrid.getTable().addEventListener(new EventListener(Event.TABLE_COLUMN_SORTED_EVENT) {
                @Override
                public void fire(Event event) {
                    // nur wenn das Grid Elemente enthält und "Sortierung aufheben" gewählt wurde
                    if ((myGrid.getTable().getRowCount() > 0) && (myGrid.getTable().getSortColumn() == -1)) {
                        myGrid.getTable().removeEventListener(this);
                        dataToGrid();
                        myGrid.getTable().addEventListener(this);
                    }
                }
            });
            return myGrid;
        }
        return new DataObjectFilterGrid(getConnector(), this);
    }

    @Override
    protected void checkboxRetailFilterClicked(Event event) {
        gridTop.setDisplayFields(null); // Damit die angezeigten Felder neu bestimmt werden
        updateView();
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(boolean top) {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<>();
        if (top) {
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_FACTORY, false, false);
            displayField.setColumnFilterEnabled(true);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMA, true, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTA, false, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMB, false, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTB, false, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_KATALOG, FIELD_K_POS, true, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, false, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_RFMEA, false, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_RFMEN, false, false);
            defaultDisplayFields.add(displayField);

            displayField = new EtkDisplayField(TABLE_DA_REPLACE_PART, iPartsDataVirtualFieldsDefinition.DRP_INCLUDE_PARTS_AVAILABLE, false, false);
            defaultDisplayFields.add(displayField);

            if (isRetailFilterSet()) {
                displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_DATA_ID, false, false);
                defaultDisplayFields.add(displayField);

                displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsDataVirtualFieldsDefinition.DFD_INHERITED_FACTORY_DATA, false, false);
                defaultDisplayFields.add(displayField);

                // Das gleiche für die Filter-Informationen, die aber nur im DEVELOPMENT-Modus standardmäßig angezeigt werden sollen
                if (Constants.DEVELOPMENT) {
                    displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsDataVirtualFieldsDefinition.DFD_FILTER_INFO,
                                                       false, false);
                    defaultDisplayFields.add(displayField);
                }
            }
        }
        return defaultDisplayFields;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (forceUpdateAll || (getConnector().getActiveRelatedSubForm() == this)) {
            partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());

            dataToGrid();
        }
    }

    @Override
    protected void dataToGrid() {
        Object storageSelected = null;
        if (gridTop instanceof DataObjectFilterGrid) {
            storageSelected = ((DataObjectFilterGrid)gridTop).getFilterAndSortSettings(true);
        }
        List<IdWithType> selectedIds = gridTop.getSelectedObjectIds(TABLE_DA_FACTORY_DATA);

        if (gridTop.getDisplayFields() == null) {
            gridTop.setDisplayFields(getDisplayFields(CONFIG_KEY_DIALOG_RETAIL_REPLACE_PARTS_CHAIN));
        }

        gridTop.clearGrid();
        // Die Zeilen werden nach Werksnummer gruppiert und sortiert
        Map<String, List<DataObjectContainer>> factoryIdToRows = createSortedMapForFactoryData();
        iPartsDataPartListEntry selectedPartListEntry = null;
        if (partListEntry instanceof iPartsDataPartListEntry) {
            selectedPartListEntry = (iPartsDataPartListEntry)partListEntry;
        }

        // Falls der vorherige Wert für das RetailFilter-Flag anders als der jetzige ist, dann wurde nur der Retailfilter aktiviert/deaktiviert
        // Deswegen muss die Ersetzungskette nicht neu geladen werden, sondern nur die Werkseinsatzdaten für jeden Stücklisteneintrag
        // in der Ersetzungskette
        if (isRetailFilterOld == isRetailFilterSet()) {
            rowsOfDataObjectsWithoutFactoryData.clear();
            Set<String> validStates = new HashSet<>();
            validStates.add(iPartsDataReleaseState.RELEASED.getDbValue());
            validStates.add(iPartsDataReleaseState.RELEASED_READONLY.getDbValue());
            List<iPartsReplacement> predecessorChain = new ArrayList<>();
            List<iPartsReplacement> successorChain = new ArrayList<>();

            if (selectedPartListEntry != null) {
                // Alle Ersetzungen in beide Richtungen bestimmen
                boolean foundSomething = iPartsReplacementHelper.findAllReplacementsInBothDirections(validStates, predecessorChain, successorChain, selectedPartListEntry, false);
                if (foundSomething) {
                    // Die erste Ersetzung der kompletten Kette bestimmen
                    iPartsReplacement firstReplacementInChain = getFirstReplacementInChain(successorChain, predecessorChain);
                    // Check, ob es sich um einen Zyklus handelt. Falls ja, Info ausgeben
                    if (handleInfiniteReplacementCycle(firstReplacementInChain, successorChain, predecessorChain, selectedPartListEntry)) {
                        addToGridTopTitle("!!Zyklische Ersetzungskette erkannt");
                    } else {
                        setGridTopTitle(RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN_TEXT);
                    }

                    // Der erste in der Kette muss per Hand erstellt werden, da hier auf den Vorgänger Eintrag der Ersetzung gegangen wird
                    // Da der Eintrag entweder der selektierte oder ein Vorgänger ist, sind hier keine RFME Flags erwünscht
                    addDisplayObjectForGrid(selectedPartListEntry, firstReplacementInChain, factoryIdToRows, true, false);

                    // Die ganze Ersetzungskette
                    predecessorChain.addAll(successorChain);
                    for (iPartsReplacement replacement : predecessorChain) {
                        addDisplayObjectForGrid(selectedPartListEntry, replacement, factoryIdToRows, false, true);
                    }
                }
            }
        } else {
            // Die Ersetzungskette wurde schon geladen und die Teilepositions-, Ersetzungs- und Materialstammdaten für
            // die Ersetzungskette sind gleich geblieben. Daraus den Stücklisteneintrag pro Zeile lesen und
            // damit die Werkseinsatzdaten nachladen.
            iPartsDataPartListEntry replacementPartListEntry;
            for (DataObjectContainer row : rowsOfDataObjectsWithoutFactoryData) {
                replacementPartListEntry = row.getPartListEntry();
                if (replacementPartListEntry != null) {
                    // Nur noch Werksdaten sammeln
                    addFactoryDataToDataObjectListToDisplay(replacementPartListEntry, row.getDataObjects(), factoryIdToRows);
                }
            }
        }

        // Muss gesetzt werden, um eine Retailfilteränderung mitzubekommen
        isRetailFilterOld = isRetailFilterSet();

        if (!factoryIdToRows.isEmpty()) {
            List<Integer> indicesForMarkedRows = getIndicesForMarkedRows(factoryIdToRows, selectedPartListEntry);
            gridTop.setMarkedRows(indicesForMarkedRows, false);
        }
        if (storageSelected != null) {
            ((DataObjectFilterGrid)gridTop).restoreFilterAndSortSettings(storageSelected);

        }
        gridTop.setSelectedObjectIds(selectedIds, TABLE_DA_FACTORY_DATA);
        gridTop.showNoResultsLabel(gridTop.getTable().getRowCount() == 0);
    }

    /**
     * Liefert die Indizes für die markierten Zeilen
     *
     * @param factoryIdToRows
     * @param selectedPartListEntry
     * @return
     */
    private List<Integer> getIndicesForMarkedRows(Map<String, List<DataObjectContainer>> factoryIdToRows,
                                                  iPartsDataPartListEntry selectedPartListEntry) {
        int rowCounter = 0;
        List<Integer> indicesForMarkedRows = new ArrayList<>();
        for (List<DataObjectContainer> rows : factoryIdToRows.values()) {
            for (DataObjectContainer row : rows) {
                iPartsDataPartListEntry replacementPartListEntry = row.getPartListEntry();
                getGrid(true).addObjectToGrid(row.dataObjects);
                if ((replacementPartListEntry != null) && (selectedPartListEntry != null)) {
                    if (replacementPartListEntry.getAsId().equals(selectedPartListEntry.getAsId())) {
                        indicesForMarkedRows.add(rowCounter);
                    }
                }
                rowCounter++;
            }
        }
        return indicesForMarkedRows;
    }

    /**
     * Überprüft, ob es sich um einen Zyklus handelt. Falls ja, wird die Ersetzungskette aufgebrochen.
     *
     * @param firstReplacementInChain
     * @param successorChain
     * @param predecessorChain
     * @param selectedPartListEntry
     * @return
     */
    private boolean handleInfiniteReplacementCycle(iPartsReplacement
                                                           firstReplacementInChain, List<iPartsReplacement> successorChain, List<iPartsReplacement> predecessorChain, iPartsDataPartListEntry
                                                           selectedPartListEntry) {
        iPartsReplacement lastReplacementInChain = getLastReplacementInChain(successorChain, predecessorChain);
        if (firstReplacementInChain.predecessorEntry.getAsId().equals(lastReplacementInChain.successorEntry.getAsId())) {
            // Bei einer zyklischen Ersetzung sind der Vorgänger der ersten Ersetzung und der Nachfolger der letzten Ersetzung gleich
            // Die Kette auf trennen
            splitCycleInPredecessorAndSuccessorChain(selectedPartListEntry, predecessorChain, successorChain);
            // Den Letzten in der Ersetzungskette löschen, sonst gibt es den Ersten und den Letzten doppelt
            // der Erste muss manuell angelegt werden, sonst ist die Reihenfolge falsch
            successorChain.remove(lastReplacementInChain);
            // Es handelt sich um einen Zyklus
            return true;
        }
        return false;
    }

    /**
     * Liefert die erste Ersetzung der Ersetzungskette
     *
     * @param successorChain
     * @param predecessorChain
     * @return
     */
    private iPartsReplacement getFirstReplacementInChain
    (List<iPartsReplacement> successorChain, List<iPartsReplacement> predecessorChain) {
        iPartsReplacement firstReplacementInChain = new iPartsReplacement();
        if (predecessorChain.isEmpty() && !successorChain.isEmpty()) {
            firstReplacementInChain = successorChain.get(0);
        } else if (!predecessorChain.isEmpty()) {
            firstReplacementInChain = predecessorChain.get(0);
        }
        return firstReplacementInChain;
    }

    /**
     * LIefert die letzte Ersetzung der Ersetzungskette
     *
     * @param successorChain
     * @param predecessorChain
     * @return
     */
    private iPartsReplacement getLastReplacementInChain
    (List<iPartsReplacement> successorChain, List<iPartsReplacement> predecessorChain) {
        iPartsReplacement lastReplacementInChain = new iPartsReplacement();
        if (successorChain.isEmpty() && !predecessorChain.isEmpty()) {
            lastReplacementInChain = predecessorChain.get(predecessorChain.size() - 1);
        } else if (!successorChain.isEmpty()) {
            lastReplacementInChain = successorChain.get(successorChain.size() - 1);
        }
        return lastReplacementInChain;
    }

    private Map<String, List<DataObjectContainer>> createSortedMapForFactoryData() {
        // Daten über die Werksnummer in die richtige Reihenfolge bringen. Erst nach Länge dann nach Inhalt sortieren
        return new TreeMap<>((o1, o2) -> {
            if (o1.length() < o2.length()) {
                return -1;
            } else if (o1.length() > o2.length()) {
                return 1;
            }
            return o1.compareTo(o2);
        });
    }

    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
    }

    /**
     * Bei der zyklischen Ersetzungskette befindet sich die ganze Kette in der Vorgängerkette
     * Diese anhand des selektierten Stücklistenauftrags in Vorgänger-und Nachfolgerkette trennen
     *
     * @param selectedPartListEntry
     * @param predecessorChain
     * @param successorChain
     */
    private void splitCycleInPredecessorAndSuccessorChain(iPartsDataPartListEntry
                                                                  selectedPartListEntry, List<iPartsReplacement> predecessorChain, List<iPartsReplacement> successorChain) {
        List<iPartsReplacement> tempPredecessorChain = new ArrayList<>(predecessorChain);
        boolean afterSelectedEntry = false;
        for (iPartsReplacement replacement : tempPredecessorChain) {
            if (!afterSelectedEntry) {
                // In der zyklischen Ersetzungskette muss auf den Vorgänger gegangen werden, um die Reihenfolge beibehalten zu können.
                iPartsDataPartListEntry replacementEntry = createPartListEntryForReplaceChain(replacement, true);
                if (replacementEntry != null) {
                    if (replacementEntry.equals(selectedPartListEntry)) {
                        afterSelectedEntry = true;
                        predecessorChain.remove(replacement);
                        successorChain.add(replacement);
                    }
                }
            } else {
                predecessorChain.remove(replacement);
                successorChain.add(replacement);
            }

        }
    }

    /**
     * Objekt zur Anzeige zusammenbauen, dass je nach Vorhandensein von Daten anders befüllt wird
     *
     * @param replacement
     * @param factoryIdToRows
     * @param firstReplacementOfChain
     * @param withRFME
     */
    private void addDisplayObjectForGrid(iPartsDataPartListEntry selectedPartListEntry, iPartsReplacement
            replacement,
                                         Map<String, List<DataObjectContainer>> factoryIdToRows,
                                         boolean firstReplacementOfChain, boolean withRFME) {
        List<DataObjectContainer> rows = new ArrayList<>();
        if (replacement != null) {
            DataObjectContainer row = new DataObjectContainer();
            // Ersetzungsdaten
            iPartsRelatedInfoReplacementsDataForm.ReplaceGridObject dataReplacePart = createReplacePartFromReplacementChain(replacement, withRFME);
            row.add(dataReplacePart);
            // Teilepositionsdaten
            iPartsDataPartListEntry replacementIPartsDataPartlistEntry = createPartListEntryForReplaceChain(replacement, firstReplacementOfChain);
            if (replacementIPartsDataPartlistEntry != null) {
                String selectedPartListEntryHotSpot = selectedPartListEntry.getFieldValue(FIELD_K_POS);
                String replacementIPartsDataPartlistEntryHotSpot = replacementIPartsDataPartlistEntry.getFieldValue(FIELD_K_POS);
                // Falls Hotspot nicht gleich ist, nichts ausgeben
                if (!selectedPartListEntryHotSpot.equals(replacementIPartsDataPartlistEntryHotSpot)) {
                    return;
                }
                row.add(replacementIPartsDataPartlistEntry);
                // Materialstamm
                EtkDataPart part = createAndSetPartForReplacementChain(getProject(), replacementIPartsDataPartlistEntry.getFieldValue(FIELD_K_MATNR));
                row.add(part);
                //Werksdaten
                addFactoryDataToDataObjectListToDisplay(replacementIPartsDataPartlistEntry, row.getDataObjects(), factoryIdToRows);
                // Für den Cache
                rows.add(row);
            } else {
                // Ohne Teilepos, Materialstamm und Werksdaten
                rows.add(row);
                factoryIdToRows.putIfAbsent(NO_FACTORYDATA_ID, new ArrayList<>());
                factoryIdToRows.get(NO_FACTORYDATA_ID).addAll(rows);
            }
        }
        rowsOfDataObjectsWithoutFactoryData.addAll(rows);

    }

    /**
     * Für einen Stücklisteneintrag aus der Ersetzungskette die Werkseinsatzdaten laden
     * Dies restlichen Objekte zur Anzeige an die Werksdaten hängen und nach
     * Werknummer gruppiert speichern
     *
     * @param replacementIPartsDataPartlistEntry
     * @param dataObjectList
     * @param factoryIdToRows
     */
    private void addFactoryDataToDataObjectListToDisplay(iPartsDataPartListEntry
                                                                 replacementIPartsDataPartlistEntry,
                                                         List<EtkDataObject> dataObjectList,
                                                         Map<String, List<DataObjectContainer>> factoryIdToRows) {
        iPartsDataFactoryDataList filteredFactoryList = getFactoryDataListFilteredByStateAndASData(replacementIPartsDataPartlistEntry);
        if (!filteredFactoryList.isEmpty()) {
            for (iPartsDataFactoryData factoryData : filteredFactoryList) {
                if (!factoryData.existsInDB()) {
                    factoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                }
                // dataObjectList ist für jede Zeile pro Werksdaten gleich
                // und kann wieder verwendet werden. Deswegen die Werksdaten nur an eine temporäre Liste hinzufügen
                DataObjectContainer row = new DataObjectContainer(dataObjectList);
                row.add(factoryData);
                String factoryId = factoryData.getAsId().getFactory();
                //Hier ist eine Zeile komplett gefüllt
                factoryIdToRows.putIfAbsent(factoryId, new ArrayList<>());
                factoryIdToRows.get(factoryId).add(row);
            }
        } else {
            // Ohne Werksdaten
            DataObjectContainer row = new DataObjectContainer(dataObjectList);
            factoryIdToRows.putIfAbsent(NO_FACTORYDATA_ID, new ArrayList<>());
            factoryIdToRows.get(NO_FACTORYDATA_ID).add(row);
        }
    }

    /**
     * Lädt alle Werkseinsatzdaten zum Stücklisteneintrag
     * Filtert nach freigegebenen Datensätze und bevorzugt Daten aus AS
     * Vorsichtshalber noch pro Werk den neuesten Datensatz ermitteln
     *
     * @param partListEntry
     * @return
     */
    private iPartsDataFactoryDataList getFactoryDataListFilteredByStateAndASData(iPartsDataPartListEntry
                                                                                         partListEntry) {
        iPartsDataFactoryDataList factoryData;
        if (isRetailFilterSet()) {
            factoryData = FactoryDataHelper.getFactoryDataList(partListEntry, true, true, getProject());
        } else {
            iPartsDataFactoryDataList factoryDataAS = FactoryDataHelper.getFactoryDataList(partListEntry, false, true, getProject());
            factoryData = FactoryDataHelper.getFactoryDataList(partListEntry, false, false, getProject());
            factoryData.addAll(factoryDataAS, DBActionOrigin.FROM_DB);
            if (!factoryData.isEmpty()) {
                // Filtert die Daten nach freigegeben Datensätzen und bevorzugt Daten aus AS
                iPartsDataFactoryDataList filteredFactoryDataList = FactoryDataHelper.getDataForCopy(factoryData);
                // Filtert Daten nach dem neuesten ADAT. Eigentlich sollte nur ein freigegebener Werkseinsatzdatensatz pro Werk übrig bleiben
                // Zur Sicherheit aber nochmal nach dem jüngsten Datensatz filtern
                FactoryDataHelper.filterListByNewestFactoryData(filteredFactoryDataList, FIELD_DFD_ADAT, "");
                return filteredFactoryDataList;
            }
        }
        // leere Liste zurückgeben
        return factoryData;
    }

    /**
     * Teilepositionsdaten
     * Handelt es sich um die erste Ersetzung in der Kette muss der Vorgänger betrachtet werden
     *
     * @param replacement
     * @return
     */
    private iPartsDataPartListEntry createPartListEntryForReplaceChain(iPartsReplacement replacement,
                                                                       boolean isFirstReplacementInChain) {
        if (isFirstReplacementInChain) {
            EtkDataPartListEntry predecessorEntry = replacement.predecessorEntry;
            if (predecessorEntry != null) {
                if (predecessorEntry instanceof iPartsDataPartListEntry) {
                    return (iPartsDataPartListEntry)predecessorEntry;
                }
            }
        } else {
            EtkDataPartListEntry successorEntry = replacement.successorEntry;
            if (successorEntry != null) {
                if (successorEntry instanceof iPartsDataPartListEntry) {
                    return (iPartsDataPartListEntry)successorEntry;
                }
            }
        }
        return null;
    }

    /**
     * Ersetzungsdaten
     *
     * @param replacement
     * @return
     */
    private iPartsRelatedInfoReplacementsDataForm.ReplaceGridObject createReplacePartFromReplacementChain
    (iPartsReplacement replacement, boolean withRFME) {
        iPartsDataReplacePart dataReplacePart = replacement.getAsDataReplacePart(getProject(), true);
        if (dataReplacePart != null) {
            if (withRFME) {
                dataReplacePart.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DRP_INCLUDE_PARTS_AVAILABLE,
                                                         SQLStringConvert.booleanToPPString(replacement.hasIncludeParts(getProject())), DBActionOrigin.FROM_DB);
            } else {
                dataReplacePart.setFieldValue(FIELD_DRP_REPLACE_RFMEA, "", DBActionOrigin.FROM_DB);
                dataReplacePart.setFieldValue(FIELD_DRP_REPLACE_RFMEN, "", DBActionOrigin.FROM_DB);
            }
            return new iPartsRelatedInfoReplacementsDataForm.ReplaceGridObject(getProject(), dataReplacePart, replacement);
        }
        return null;
    }

    /**
     * Materialstammdaten
     *
     * @param project
     * @param matNo
     * @return
     */
    private EtkDataPart createAndSetPartForReplacementChain(EtkProject project, String matNo) {
        EtkDataPart part = EtkDataObjectFactory.createDataPart(project, matNo, "");
        if (!part.existsInDB()) {
            part.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            part.setFieldValue(iPartsConst.FIELD_M_BESTNR, matNo, DBActionOrigin.FROM_DB);
        }
        return part;
    }

    private iPartsReplacement getSelectedReplacement() {
        List<EtkDataObject> etkDataObjectList = gridTop.getSelection();
        if (etkDataObjectList != null) {
            for (EtkDataObject etkDataObject : etkDataObjectList) {
                if (etkDataObject instanceof iPartsRelatedInfoReplacementsDataForm.ReplaceGridObject) {
                    return ((iPartsRelatedInfoReplacementsDataForm.ReplaceGridObject)etkDataObject).getReplacement();
                }
            }
        }
        return null;
    }

    /**
     * Container anlog zu GuiTableRowWithObjects zum Speichern der DataObjects für eine Grid-Zeile
     */
    private static class DataObjectContainer {

        // Für den Inhalt einer Zeile können mehrere Objekte verantwortlich sein -> z.B. Entry und Part, deshalb eine Liste von Objekten
        private List<EtkDataObject> dataObjects;

        public DataObjectContainer() {
            this.dataObjects = new ArrayList<>();
        }

        public DataObjectContainer(List<EtkDataObject> dataObjects) {
            this();
            addAll(dataObjects);
        }

        public void addAll(List<EtkDataObject> dataObjects) {
            if (dataObjects != null) {
                this.dataObjects.addAll(dataObjects);
            }
        }

        public void add(EtkDataObject dataObject) {
            if (dataObject != null) {
                this.dataObjects.add(dataObject);
            }
        }

        public void clear() {
            this.dataObjects.clear();
        }

        public List<EtkDataObject> getDataObjects() {
            return dataObjects;
        }

        /**
         * Suche welches Objekt in der Row für diese Tabelle zuständig ist
         *
         * @param tableName
         * @return
         */
        public EtkDataObject getObjectForTable(String tableName) {
            for (EtkDataObject value : dataObjects) {
                EtkDataObject result = value.getDataObjectByTableName(tableName, false);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        private iPartsDataPartListEntry getPartListEntry() {
            EtkDataObject etkDataObject = getObjectForTable(TABLE_KATALOG);
            if (etkDataObject instanceof iPartsDataPartListEntry) {
                return (iPartsDataPartListEntry)etkDataObject;
            }
            return null;
        }
    }

}
