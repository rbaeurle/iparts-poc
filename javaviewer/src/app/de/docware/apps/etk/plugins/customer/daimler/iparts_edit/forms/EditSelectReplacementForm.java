/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoWWPartsDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

public class EditSelectReplacementForm extends EditSelectPartlistEntryForm {

    public static iPartsDataReplacePart showSelectReplacementForm(EtkProject project, EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                  iPartsDataReplacePart replacePart, iPartsDataReplacePartList allReplacements,
                                                                  boolean isNewReplacement, EtkDisplayFields displayFields) {
        if (replacePart != null) {
            String lfdNr = replacePart.getFieldValue(iPartsConst.FIELD_DRP_LFDNR);
            String successorLfdNr = replacePart.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_LFDNR);
            EtkDataAssembly currentAssembly = dataConnector.getCurrentAssembly();
            EtkDataPartListEntry partListEntry = currentAssembly.getPartListEntryFromKLfdNrUnfiltered(lfdNr);
            EtkDataPartListEntry successorEntry = currentAssembly.getPartListEntryFromKLfdNrUnfiltered(successorLfdNr);

            List<EtkDataPartListEntry> initialSelectedPartlistEntries = new DwList<>(1);
            if (successorEntry != null) {
                initialSelectedPartlistEntries.add(successorEntry);
            }

            String title;
            boolean useExistingData = false;
            if (isNewReplacement) {
                title = "!!Nachfolger anlegen für \"%1\"";
            } else {
                useExistingData = replacePart.getSource() == iPartsReplacement.Source.IPARTS;
                title = "!!Nachfolger bearbeiten für \"%1\"";
            }

            EditSelectReplacementForm dlg = new EditSelectReplacementForm(dataConnector, parentForm, partListEntry, initialSelectedPartlistEntries, displayFields);
            dlg.setTitle(iPartsRelatedInfoWWPartsDataForm.getPartListExtraText(dataConnector.getProject(), title, partListEntry));
            dlg.setReplacement(replacePart);
            dlg.actualDataGrid.setMarkedRow(dlg.getActualGridIndexOfReplacePart(), true);
            if (dlg.showModal() == ModalResult.OK) {
                if (useExistingData) {
                    // Weil das EditControl nur auf den Attributes arbeitet und wir die Beziehung zur alten ID nicht verlieren wollen,
                    // muss hier die ID aus den bearbeiteten Attributes heraus synchronisiert werden.
                    replacePart.updateIdFromPrimaryKeys();
                    return replacePart;
                } else {
                    iPartsDataReplacePart dataReplacement = dlg.getReplacement();
                    dataReplacement.setFieldValue(iPartsConst.FIELD_DRP_SEQNO, iPartsReplacementHelper.getNextReplacementSeqNo(partListEntry.getEtkProject(),
                                                                                                                               partListEntry.getAsId(),
                                                                                                                               allReplacements),
                                                  DBActionOrigin.FROM_EDIT);
                    dataReplacement.updateOldId(); // DRP_SEQNO ist Teil vom Primärschlüssel!
                    iPartsDataReplacePart result = new iPartsDataReplacePart(project, dataReplacement.getAsId());
                    result.setAttributes(dataReplacement.getAttributes(), DBActionOrigin.FROM_EDIT);
                    return result;
                }
            }
        }
        return null;
    }

    private String originalRFMEA;
    private String originalRFMEN;
    private EditRFMEFlagsForm rfmeanFlagsForm;
    private iPartsDataReplacePart replacePart;

    /**
     * Erzeugt eine Instanz von EditSelectReplacementForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param partListEntry
     * @param initialSelectedPartlistEntries
     * @param displayFields
     */
    public EditSelectReplacementForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                     EtkDataPartListEntry partListEntry, List<EtkDataPartListEntry> initialSelectedPartlistEntries,
                                     EtkDisplayFields displayFields) {
        super(dataConnector, parentForm, partListEntry, initialSelectedPartlistEntries, displayFields, displayFields,
              false, true);
        setName("SelectPartlistForReplacements");
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();

        rfmeanFlagsForm = new EditRFMEFlagsForm(getConnector(), this);
        rfmeanFlagsForm.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                refreshOKButton();
            }
        });
        getPanelTop().addChildBorderSouth(rfmeanFlagsForm.getGui());

        setDividerPos(130);
    }

    @Override
    protected void actualDataGridSelectionChanged() {
        super.actualDataGridSelectionChanged();
        updateApplyEnabledState();
    }

    protected void updateApplyEnabledState() {
        List<EtkDataObject> selectedEntries = actualDataGrid.getSelection();
        if ((replacePart != null) && (selectedEntries != null)) {
            boolean enabled = true;
            PartListEntryId originalEntryId = replacePart.getPredecessorPartListEntryId();
            for (EtkDataObject entry : selectedEntries) {
                if (entry.getAsId().equals(originalEntryId)) {
                    enabled = false;
                    break;
                }
            }
            applyMenuItem.setEnabled(enabled);
            applyToolbarButton.setEnabled(enabled);
        }
    }

    @Override
    protected void modifySelection(List<List<EtkDataObject>> highlightedRows, boolean isSelected) {
        List<EtkDataPartListEntry> oldSelection = getSelectedPartListEntries();
        super.modifySelection(highlightedRows, isSelected);
        actualDataGrid.setMarkedRow(getActualGridIndexOfReplacePart(), false);
        List<EtkDataPartListEntry> newSelection = getSelectedPartListEntries();

        // beim hinzufügen auf Kreisbezüge testen
        if (isSelected && (newSelection.size() == 1)) {
            EtkDataPartListEntry selectedEntry = newSelection.get(0);

            // wenn es den neuen Eintrag bereits als Nachfolger gibt, muss nicht mehr auf Kreisbezüge getestet werden
            boolean checkForCycles = true;
            if (currentPartListEntry instanceof iPartsDataPartListEntry) {
                Collection<iPartsReplacement> successors = ((iPartsDataPartListEntry)currentPartListEntry).getSuccessors();
                if (successors != null) {
                    for (iPartsReplacement successor : successors) {
                        if ((successor.successorEntry != null) && successor.successorEntry.getAsId().getKLfdnr().equals(selectedEntry.getAsId().getKLfdnr())) {
                            checkForCycles = false;
                            break;
                        }
                    }
                }
            }

            if (checkForCycles && detectCycles(currentPartListEntry, selectedEntry)) {
                ModalResult modalResult = MessageDialog.showYesNo(TranslationHandler.translate("!!Durch Hinzufügen dieses Eintrags würde eine zyklische Ersetzungskette entstehen.%1Soll der Eintrag trotzdem hinzugefügt werden?", "\n"));
                if (modalResult == ModalResult.NO) {
                    // Selektion zurücksetzen
                    for (EtkDataPartListEntry partListEntry : oldSelection) {
                        selectedIds.add(partListEntry.getAsId());
                    }
                    selectedIds.remove(selectedEntry.getAsId());
                    editedSelection = oldSelection;
                    dataToGrid();
                }
            }
        }
    }

    private boolean detectCycles(EtkDataPartListEntry entry, EtkDataPartListEntry selectedEntry) {
        String oldCycleFound = "!!Bereits existierende zyklische Ersetzungkette gefunden.%3Eintrag (%1) wird mehrfach als %2 verwendet.";

        Stack<EtkDataPartListEntry> stack = new Stack<>();
        Set<String> visitedPredecessors = new HashSet<>();

        // Vorgänger aufsammeln und auf Mehrfachverwendung prüfen
        stack.push(entry);
        while (!stack.isEmpty()) {
            EtkDataPartListEntry currentNode = stack.pop();
            if (visitedPredecessors.add(currentNode.getAsId().getKLfdnr())) {
                if (currentNode instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry currentEntry = (iPartsDataPartListEntry)currentNode;
                    Collection<iPartsReplacement> replacements = currentEntry.getPredecessors();
                    if ((replacements != null) && !replacements.isEmpty()) {
                        for (iPartsReplacement replacement : replacements) {
                            stack.push(replacement.predecessorEntry);
                        }
                    }
                }
            } else {
                // Alte Kette, kann nicht korrigiert werden, daher nur Warnung anzeigen
                MessageDialog.showWarning(TranslationHandler.translate(oldCycleFound,
                                                                       currentNode.getPart().getDisplayValue(iPartsConst.FIELD_M_MATNR, getProject().getViewerLanguage()),
                                                                       "Vorgänger", "\n"));
                return false;
            }
        }

        // Nachfolger aufsammeln und auf Mehrfachverwendung prüfen
        Set<String> visitedSuccessors = new HashSet<>();
        stack.push(entry);
        while (!stack.isEmpty()) {
            EtkDataPartListEntry currentNode = stack.pop();
            if (currentNode != null) {
                if (visitedSuccessors.add(currentNode.getAsId().getKLfdnr())) {
                    if (currentNode instanceof iPartsDataPartListEntry) {
                        iPartsDataPartListEntry currentEntry = (iPartsDataPartListEntry)currentNode;
                        Collection<iPartsReplacement> replacements = currentEntry.getSuccessors();
                        if ((replacements != null) && !replacements.isEmpty()) {
                            for (iPartsReplacement replacement : replacements) {
                                stack.push(replacement.successorEntry);
                            }
                        }
                    }
                } else {
                    // Alte Kette, kann nicht korrigiert werden, daher nur Warnung anzeigen
                    MessageDialog.showWarning(TranslationHandler.translate(oldCycleFound,
                                                                           currentNode.getPart().getDisplayValue(iPartsConst.FIELD_M_MATNR, getProject().getViewerLanguage()),
                                                                           "Nachfolger", "\n"));
                    return false;
                }
            }
        }
        // Die Kette ist erst durch den neuen Eintrag entstanden, kann also wieder korrigiert werden
        return (visitedPredecessors.contains(selectedEntry.getAsId().getKLfdnr()) || visitedSuccessors.contains(selectedEntry.getAsId().getKLfdnr()));
    }

    @Override
    protected boolean buildTransferData() {
        if (isModified()) {
            List<EtkDataPartListEntry> selectedPartListEntries = getSelectedPartListEntries();
            if ((selectedPartListEntries != null) && (!selectedPartListEntries.isEmpty())) {
                EtkDataPartListEntry selectedEntry = selectedPartListEntries.get(0);
                replacePart.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_LFDNR, selectedEntry.getAsId().getKLfdnr(), DBActionOrigin.FROM_EDIT);
                replacePart.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_MATNR, selectedEntry.getPart().getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);
                replacePart.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEA, rfmeanFlagsForm.getSelectedRFMEA(), DBActionOrigin.FROM_EDIT);
                replacePart.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEN, rfmeanFlagsForm.getSelectedRFMEN(), DBActionOrigin.FROM_EDIT);

                replacePart.setFieldValue(iPartsConst.FIELD_DRP_SOURCE, iPartsReplacement.Source.IPARTS.getDbValue(), DBActionOrigin.FROM_EDIT);

                String seqNo = replacePart.getFieldValue(iPartsConst.FIELD_DRP_SEQNO);
                // nächste freie Ersetzungs-Lfdnr ermitteln: DRP_SEQNO
                if (seqNo.isEmpty()) {
                    String nextLfdNr = iPartsReplacementHelper.getNextReplacementSeqNo(getProject(), replacePart.getAsId().getPredecessorPartListEntryId());
                    replacePart.setFieldValue(iPartsConst.FIELD_DRP_SEQNO, nextLfdNr, DBActionOrigin.FROM_EDIT);
                    replacePart.updateOldId(); // DRP_SEQNO ist Teil vom Primärschlüssel!
                }

                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isModified() {
        boolean modified = super.isModified();
        if (!modified) {
            modified = ((originalRFMEA != null) && !rfmeanFlagsForm.getSelectedRFMEA().equals(originalRFMEA));
            if (!modified) {
                modified = ((originalRFMEN != null) && !rfmeanFlagsForm.getSelectedRFMEN().equals(originalRFMEN));
            }
        }
        if (modified) {
            List<EtkDataPartListEntry> selectedPartListEntries = getSelectedPartListEntries();
            modified = ((selectedPartListEntries != null) && (!selectedPartListEntries.isEmpty()));
        }
        return modified;
    }

    private void setReplacement(iPartsDataReplacePart replacement) {
        if (replacement != null) {

            replacePart = replacement;

            this.originalRFMEA = replacement.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEA);
            this.originalRFMEN = replacement.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEN);

            rfmeanFlagsForm.init(this.originalRFMEA, this.originalRFMEN);
        }
        refreshOKButton();
    }

    private int getActualGridIndexOfReplacePart() {
        if ((replacePart == null) || (actualDataGrid == null)) {
            return -1;
        }

        PartListEntryId originalEntryId = new PartListEntryId(replacePart.getPredecessorPartListEntryId());
        int index = 0;
        Iterator<GuiTableRow> iter = actualDataGrid.getTable().getRows().iterator();
        while (iter.hasNext()) {
            GuiTableRow row = iter.next();
            EtkDataObject dataObject = ((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(EtkDbConst.TABLE_KATALOG);
            if ((dataObject != null) && dataObject.getAsId().equals(originalEntryId)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private iPartsDataReplacePart getReplacement() {
        return replacePart;
    }
}