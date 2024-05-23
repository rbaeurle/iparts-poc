/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsIncludePartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Auswahl-Dialog für neue Mitlieferteile mit Stücklistenpositionen der aktuellen Stückliste
 */
public class EditSelectIncludePartsForm extends EditSelectPartlistEntryForm {

    private static final String PREFIX_FOR_MAT_KLFDNR = "MAT_";
    private static final String PREFIX_FOR_NOT_DELETEABLE_MAT_KLFDNR = "inc";
    // true: Mat in den Mitlieferteilen wird beim Löschen an die StüLi angehängt
    // false: eingefügtes Mat erscheint beim Löschen nicht in der StüLi
    private static final boolean USE_NOT_DELETEABLE_MAT = true;

    public static iPartsDataIncludePartList showEditSelectIncludePartsForm(EditModuleFormIConnector editConnector, AbstractJavaViewerForm parentForm,
                                                                           EtkDataPartListEntry partListEntry, iPartsReplacement replacement,
                                                                           List<EtkDataPartListEntry> initialSelectedPartlistEntries,
                                                                           EtkDisplayFields displayFieldsTop) {
        EtkProject project = editConnector.getProject();
        iPartsDataReplacePart dataReplacement = replacement.getAsDataReplacePart(project);
        EditSelectIncludePartsForm dlg = new EditSelectIncludePartsForm(editConnector, parentForm, partListEntry, dataReplacement,
                                                                        initialSelectedPartlistEntries, displayFieldsTop);
        dlg.setTitle(replacement.getAsStringForTitle(project.getDBLanguage()));

        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getUpdatedIncludePartList();
        }
        return null;
    }

    private final iPartsDataReplacePart dataReplacement;
    private iPartsDataIncludePartList updatedIncludePartList;

    private int indexForMatInsert = 0;
    private int indexForNotDeleteableMatInsert;

    private EditSelectIncludePartsForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                       EtkDataPartListEntry partListEntry, iPartsDataReplacePart dataReplacement,
                                       List<EtkDataPartListEntry> initialSelectedPartlistEntries,
                                       EtkDisplayFields displayFieldsTop) {
        super(dataConnector, parentForm, partListEntry, initialSelectedPartlistEntries, displayFieldsTop, null, true, false);
        this.dataReplacement = dataReplacement;

        // An den Super-Konstruktor muss multiselect = true übergeben werden, damit es mehrere selektierte Mitlieferteile
        // geben kann; beim Hinzufügen darf es aber nur Einfachselektion geben
        actualDataGrid.setMultiSelect(false);

        // Zum Hotspot des Stücklisteneintrags scrollen
        String partListEntryPos = partListEntry.getFieldValue(iPartsConst.FIELD_K_POS);
        for (EtkDataPartListEntry entry : allPartListEntries) {
            if (entry.getFieldValue(iPartsConst.FIELD_K_POS).equals(partListEntryPos)) {
                DwList<IdWithType> ids = new DwList<>();
                ids.add(entry.getAsId());
                actualDataGrid.setSelectedObjectIds(ids, iPartsConst.TABLE_KATALOG);
                break;
            }
        }

        setName("EditSelectIncludePartsForm");
    }

    @Override
    protected void createToolbarButtons() {
        super.createToolbarButtons();
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_MAT, "!!Auswahl Material", new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doLoadMat(event);
            }
        });
    }

    @Override
    protected void modifySelection(List<List<EtkDataObject>> highlightedRows, boolean isSelected) {
        super.modifySelection(highlightedRows, isSelected);
        if (!isSelected) {
            removeEntriesFromActualGrid(highlightedRows);
        }
    }

    /**
     * Entfernt die übergebenen Stücklisteneinträge aus der Liste aller Einträge, sofern es sich um einen künstlichen
     * Stücklisteneintrag handelt
     *
     * @param highlightedRows
     */
    private void removeEntriesFromActualGrid(List<List<EtkDataObject>> highlightedRows) {
        // Beim Entfernen die künstlich hinzugefügten MAT-PartListEntries entfernen
        List<PartListEntryId> partListEntryIds = new DwList<>();
        for (List<EtkDataObject> dataObjectList : highlightedRows) {
            for (EtkDataObject dataObject : dataObjectList) {
                if (isMatKLfdNr(((EtkDataPartListEntry)dataObject).getAsId())) {
                    partListEntryIds.add(((EtkDataPartListEntry)dataObject).getAsId());
                }
            }
        }
        if (!partListEntryIds.isEmpty()) {
            allPartListEntries.removeIf((entry) -> partListEntryIds.contains(entry.getAsId()));
        }
    }

    private String buildMatKLfdNr() {
        if (USE_NOT_DELETEABLE_MAT) {
            return buildNotDeleteableMatKLfdNr();
        } else {
            indexForMatInsert++;
            return PREFIX_FOR_MAT_KLFDNR + indexForMatInsert;
        }
    }

    private String buildNotDeleteableMatKLfdNr() {
        indexForNotDeleteableMatInsert++;
        return PREFIX_FOR_NOT_DELETEABLE_MAT_KLFDNR + indexForNotDeleteableMatInsert;
    }

    private boolean isMatKLfdNr(PartListEntryId id) {
        return isMatKLfdNr(id.getKLfdnr());
    }

    private boolean isMatKLfdNr(String kLfdNr) {
        return kLfdNr.startsWith(PREFIX_FOR_MAT_KLFDNR);
    }

    private void doLoadMat(Event event) {
        EtkDataPartListEntry partListEntry = EditMaterialEditIncludePartForm.showEditMaterialEditIncludePartForm(getConnector(), this);
        if (partListEntry != null) {
            // keine doppelten Teile
            if (isAlreadySelected(partListEntry)) {
                return;
            }
            partListEntry.setFieldValue(EtkDbConst.FIELD_K_LFDNR, buildMatKLfdNr(), DBActionOrigin.FROM_DB);
            partListEntry.updateIdFromPrimaryKeys(); // ID wird bei DBActionOrigin.FROM_DB nicht aktualisiert

            List<List<EtkDataObject>> highlightedRows = new DwList<>();
            List<EtkDataObject> objList = new DwList<>();
            objList.add(partListEntry);
            highlightedRows.add(objList);
            // MAT-PartListEntry künstlich hinzugefügen
            allPartListEntries.add(partListEntry);
            modifySelection(highlightedRows, true);
        }
    }

    /**
     * Prüft, ob der übergebene Stücklisteneintrag schon einmal selektiert wurde
     *
     * @param partListEntry
     * @return
     */
    private boolean isAlreadySelected(EtkDataPartListEntry partListEntry) {
        for (String partNo : getSelectedPartNumbers()) {
            if (partListEntry.getPart().getAsId().getMatNr().equals(partNo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void addFurtherContextMenusToActualGrid(GuiContextMenu contextMenu, EditToolbarButtonMenuHelper toolbarHelper) {
        GuiMenuItem menuItem = toolbarHelper.createContextMenuEntry(EditToolbarButtonAlias.EDIT_MAT, "!!Auswahl Material",
                                                                    getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doLoadMat(event);
                    }
                });
        contextMenu.addChild(menuItem);
    }

    @Override
    protected void initSelectionLists(List<EtkDataPartListEntry> initialSelectedPartlistEntries, boolean includeOriginalEntry) {
        super.initSelectionLists(initialSelectedPartlistEntries, includeOriginalEntry);

        // Initiale Selektion könnte ein Teil enthalten, zu dem es gar keinen Eintrag in der Stückliste gibt
        // (migrierte Daten oder der Stücklisteneintrag wurde nachträglich gelöscht)
        indexForNotDeleteableMatInsert = 0;
        for (EtkDataPartListEntry initialSelectedPartlistEntry : initialSelectedPartlistEntries) {
            if (initialSelectedPartlistEntry.getAsId().getKLfdnr().isEmpty()) {
                initialSelectedPartlistEntry.setFieldValue(EtkDbConst.FIELD_K_LFDNR, buildNotDeleteableMatKLfdNr(), DBActionOrigin.FROM_DB);
                initialSelectedPartlistEntry.updateIdFromPrimaryKeys(); // ID wird bei DBActionOrigin.FROM_DB nicht aktualisiert
                selectedIds.add(initialSelectedPartlistEntry.getAsId());
                allPartListEntries.add(initialSelectedPartlistEntry);
            }
        }
    }

    @Override
    protected boolean buildTransferData() {
        if (!isModified()) {
            return false;
        }
        updatedIncludePartList = new iPartsDataIncludePartList();
        List<EtkDataPartListEntry> selectedPartListEntries = getSelectedPartListEntries();
        for (EtkDataPartListEntry selectedPartListEntry : selectedPartListEntries) {
            iPartsDataIncludePart newIncludePart = iPartsIncludePartsHelper.convertPartListEntryToIncludePart(getProject(),
                                                                                                              dataReplacement,
                                                                                                              selectedPartListEntry);
            updatedIncludePartList.add(newIncludePart, DBActionOrigin.FROM_EDIT);
        }
        return true;
    }

    private iPartsDataIncludePartList getUpdatedIncludePartList() {
        return updatedIncludePartList;
    }

    @Override
    protected List<EtkDataPartListEntry> getSelectedPartListEntries() {
        if (editedSelection == null) {
            editedSelection = new DwList<>();
            Set<PartListEntryId> selectedIdsSet = new LinkedHashSet<>(selectedIds);

            // Zuerst alle noch selektierten IDs aus initialSelection suchen (notwendig für die korrekte Reihenfolge auch
            // bei Einträgen nur mit Materialnummer sowie für die ursprünglichen Werte vom IncludePart wie z.B. Menge)
            for (EtkDataPartListEntry partListEntry : initialSelection) {
                PartListEntryId partListEntryId = partListEntry.getAsId();
                if (selectedIdsSet.contains(partListEntryId)) {
                    editedSelection.add(partListEntry);
                    selectedIdsSet.remove(partListEntryId);
                }
            }

            // Dann die ID in allPartListEntries suchen
            for (PartListEntryId selectedId : selectedIdsSet) {
                for (EtkDataPartListEntry partListEntry : allPartListEntries) {
                    if (partListEntry.getAsId().equals(selectedId)) {
                        editedSelection.add(partListEntry);
                        break;
                    }
                }
            }
        }
        return editedSelection;
    }

    @Override
    protected void fillGrids() {
        for (EtkDataPartListEntry selectedEntry : getSelectedPartListEntries()) {
            selectedDataGrid.addObjectToGrid(selectedEntry);
        }
        Set<String> selectedPartNumbers = getSelectedPartNumbers();
        for (EtkDataPartListEntry partListEntry : allPartListEntries) {
            if (!selectedPartNumbers.contains(partListEntry.getPart().getAsId().getMatNr())) {
                if (!isMatKLfdNr(partListEntry.getAsId())) {
                    actualDataGrid.addObjectToGrid(partListEntry);
                }
            }
        }
    }

    private Set<String> getSelectedPartNumbers() {
        Set<String> alreadySelectedPartNumbers = new HashSet<>();
        for (EtkDataPartListEntry selectedPartListEntry : getSelectedPartListEntries()) {
            alreadySelectedPartNumbers.add(selectedPartListEntry.getPart().getAsId().getMatNr());
        }
        return alreadySelectedPartNumbers;
    }

}
