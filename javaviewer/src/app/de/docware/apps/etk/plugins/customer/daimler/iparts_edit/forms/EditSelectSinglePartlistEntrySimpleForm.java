package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;


import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Form zum Auswählen eines einzelnen Stücklisteneintrages per Doppelklick oder OK-Button
 */
public class EditSelectSinglePartlistEntrySimpleForm extends EditSelectPartlistEntryForm {

    public static PartListEntryId showSelectPartlistPosition(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                             EtkDataPartListEntry partListEntry, String filteredSelectedHotSpot) {
        EditSelectSinglePartlistEntrySimpleForm dlg = new EditSelectSinglePartlistEntrySimpleForm(dataConnector, parentForm, partListEntry, new DwList<EtkDataPartListEntry>(), null, filteredSelectedHotSpot);
        String title = "!!Stücklisteneintrag auswählen hinter dem eingefügt werden soll";
        if (StrUtils.isValid(filteredSelectedHotSpot)) {
            title = "!!Stücklisteneintrag auswählen hinter dem eingefügt werden soll (nur innerhalb des gleichen Hotspots)";
        }
        dlg.setTitle(title);
        if (dlg.showModal() == ModalResult.OK) {
            List<EtkDataPartListEntry> selectedPartListEntries = dlg.getSelectedPartListEntries();
            if ((selectedPartListEntries != null) && (selectedPartListEntries.size() == 1)) {
                return selectedPartListEntries.get(0).getAsId();
            }
        }
        return null;
    }

    private String filteredSelectedHotSpot;

    public EditSelectSinglePartlistEntrySimpleForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm, EtkDataPartListEntry partListEntry,
                                                   List<EtkDataPartListEntry> initialSelectedPartlistEntries, EtkDisplayFields displayFields) {
        this(dataConnector, parentForm, partListEntry, initialSelectedPartlistEntries, displayFields, null);
    }

    /**
     * Erzeugt eine Instanz von EditSelectPartlistEntryForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector                  Edit Connector über den u.a. die Stückliste übertragen wird
     * @param parentForm
     * @param partListEntry
     * @param initialSelectedPartlistEntries Welche Einträge sollen bei Dialogstart bereits ausgewählt werden, {@code null} für leer
     * @param displayFields                  wenn {@code null} werden die Default Felder der Stückliste über den Connector ermittelt
     */
    public EditSelectSinglePartlistEntrySimpleForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm, EtkDataPartListEntry partListEntry,
                                                   List<EtkDataPartListEntry> initialSelectedPartlistEntries, EtkDisplayFields displayFields,
                                                   String filteredSelectedHotSpot) {
        super(dataConnector, parentForm, partListEntry, initialSelectedPartlistEntries, displayFields, displayFields,
              false, true);
        // Dialog so modifizieren dass nur das untere Panel angezeigt wird
        hideTopPanel();
        setToolbarVisible(false);
        setPanelTitle(false, "");
        this.filteredSelectedHotSpot = filteredSelectedHotSpot;
        if (StrUtils.isValid(filteredSelectedHotSpot)) {
            initSelectionLists(initialSelectedPartlistEntries, true);
            dataToGrid();
        }
        scaleWindowOffset(20, 20);
        actualDataGrid.scrollToIdIfExists(partListEntry.getAsId(), iPartsConst.TABLE_KATALOG);
    }

    @Override
    protected List<EtkDataPartListEntry> getCurrentPartListEntries() {
        List<EtkDataPartListEntry> list = getConnector().getCurrentPartListEntries();
        if (StrUtils.isEmpty(filteredSelectedHotSpot)) {
            return list;
        }
        List<EtkDataPartListEntry> filteredList = new DwList<>();
        for (EtkDataPartListEntry partListEntry : list) {
            if (partListEntry.getFieldValue(iPartsConst.FIELD_K_POS).equals(filteredSelectedHotSpot)) {
                filteredList.add(partListEntry);
            }
        }
        return filteredList;
    }

    @Override
    protected void modifySelection(List<List<EtkDataObject>> highlightedRows, boolean isSelected) {
    }

    @Override
    protected void setupGrids() {
        selectedDataGrid = new EditDataObjectGrid(getConnector(), this);
        actualDataGrid = new EditDataObjectFilterGrid(getConnector(), this) {

            protected boolean updateSelection() {
                List<EtkDataObject> selection = getSelection();
                if (((selection != null) && (selection.size() == 1)) && (selection.get(0) instanceof EtkDataPartListEntry)) {
                    EtkDataPartListEntry partListEntry = (EtkDataPartListEntry)selection.get(0);
                    editedSelection = new DwList<EtkDataPartListEntry>(1);
                    editedSelection.add(partListEntry);
                    return true;
                }
                return false;
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                if (updateSelection()) {
                    setOKButtonEnabled(true);
                } else {
                    setOKButtonEnabled(false);
                }
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                if (updateSelection()) {
                    closeWithModalResult(ModalResult.OK);
                }
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
            }
        };
    }
}
