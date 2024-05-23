/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ASUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiCombTextCompleteEditControl;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditEqualizeFieldsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.*;
import java.util.stream.Collectors;

public class iPartsLockEntryHelper implements iPartsConst {

    public static final Set<String> VALID_INPLACE_EDITS_FOR_LOCKED_ENTRIES = new HashSet<>();

    static {
        VALID_INPLACE_EDITS_FOR_LOCKED_ENTRIES.add(iPartsGuiCombTextCompleteEditControl.TYPE);
    }

    public static boolean isValidInplaceEditor(String inplaceEditorType) {
        return VALID_INPLACE_EDITS_FOR_LOCKED_ENTRIES.contains(inplaceEditorType);
    }

    /**
     * Überprüft, ob in der Liste der übergebenen Stücklistenpositionen gesperrte Positionen vorhanden sind. Optional
     * kann ein generischer Hinweis ausgegeben werden, dass die weiteren Edit-Operationen nicht möglich sind.
     *
     * @param selectedPartListEntries
     * @param withMessage
     * @return
     */
    public static boolean checkLockedEntries(List<EtkDataPartListEntry> selectedPartListEntries, boolean withMessage) {
        if ((selectedPartListEntries == null) || selectedPartListEntries.isEmpty()) {
            return false;
        }
        List<EtkDataPartListEntry> lockedEntries = getLockedEntries(selectedPartListEntries);
        if ((lockedEntries != null) && !lockedEntries.isEmpty()) {
            if (withMessage) {
                String text;
                int size = lockedEntries.size();
                if (size == 1) {
                    text = TranslationHandler.translate("!!Die Selektion enthält eine Stücklistenposition, " +
                                                        "die für den Edit gesperrt ist.");
                } else {
                    text = TranslationHandler.translate("!!Die Selektion enthält %1 Stücklistenpositionen, " +
                                                        "die für den Edit gesperrt sind.", String.valueOf(size));
                }
                text += "\n\n";
                text += TranslationHandler.translate("!!Die Edit Operation wird abgebrochen!");
                MessageDialog.show(text);
            }
            return true;
        }
        return false;
    }

    public static List<EtkDataPartListEntry> getLockedEntries(List<EtkDataPartListEntry> selectedPartListEntries) {
        if ((selectedPartListEntries == null) || selectedPartListEntries.isEmpty()) {
            return null;
        }
        return selectedPartListEntries.stream()
                .filter(iPartsLockEntryHelper::isLockedWithDBCheck)
                .collect(Collectors.toList());

    }

    /**
     * Überprüft, ob in der übergebenen Liste gesperrte Positionen vorhanden sind. Sind alle Positionen
     * gesperrt, darf das Löschen nicht fortgesetzt werden und es werden keine Positionen zurückgeliefert.
     * Ist keine Position gesperrt wird das Löschen wie gewohnt fortgesetzt. Ist mind. eine gesperrt, wird
     * ein Hinweis angezeigt und der Benutzer muss sich entscheiden, ob nur die nicht gesperrten Positionen
     * gelöscht werden sollen oder der komplette Löschvorgang abgebrochen werden soll.
     *
     * @param selectedList
     * @return
     */
    public static Optional<List<EtkDataPartListEntry>> checkLockedEntriesForDeletion(List<EtkDataPartListEntry> selectedList) {
        // Nicht gesperrte Positionen bestimmen
        List<EtkDataPartListEntry> notLockedEntries = selectedList.stream().filter(entry -> !iPartsLockEntryHelper.isLockedWithDBCheck(entry)).collect(Collectors.toList());
        // Es gibt nur gesperrte Positionen
        if (notLockedEntries.isEmpty()) {
            if (selectedList.size() == 1) {
                MessageDialog.show("!!Die selektierte Position ist für den Edit und somit das Löschen gesperrt!");
            } else {
                MessageDialog.show("!!Die selektierten Positionen sind für den Edit und somit das Löschen gesperrt!");
            }
            return Optional.empty();
        }
        // Alle Positionen sind nicht gesperrt
        if (notLockedEntries.size() == selectedList.size()) {
            return Optional.of(notLockedEntries);
        }

        // Es gibt gesperrte Positionen -> Fragen, ob die nicht gesperrten gelöscht werden sollen
        String message = TranslationHandler.translate("!!Die Selektion enthält Stücklistenpositionen, die für " +
                                                      "den Edit und somit das Löschen gesperrt sind.")
                         + "\n\n"
                         + TranslationHandler.translate("!!Soll der Löschvorgang für die nicht gesperrten " +
                                                        "Positionen fortgesetzt werden?");
        if (MessageDialog.showYesNo(message, "!!Stücklistenpositionen löschen") == ModalResult.NO) {
            return Optional.empty();
        }
        return Optional.of(notLockedEntries);
    }

    /**
     * Setzt den Kenner, ob eine Position editiert werden darf oder nicht
     *
     * @param connector
     * @param selectedEntries
     * @param lockValue       true, darf editiert werden; false, darf nicht editiert werden
     */
    public static void setPartListEntryLockValue(EditModuleFormIConnector connector,
                                                 List<EtkDataPartListEntry> selectedEntries, boolean lockValue) {
        if (iPartsRight.LOCK_PART_LIST_ENTRIES_FOR_EDIT.checkRightInSession()) {
            if ((selectedEntries == null) || selectedEntries.isEmpty()) {
                return;
            }

            String title;
            if (lockValue) {
                title = TranslationHandler.translate("!!Selektierte Stückistenpositionen werden gesperrt...");
            } else {
                title = TranslationHandler.translate("!!Selektierte Stückistenpositionen werden entsperrt...");
            }

            EntryLockData lockData = new EntryLockData(connector, lockValue);
            EtkMessageLogForm logForm = new EtkMessageLogForm("!!Stücklistenpositionen sperren", title,
                                                              null, true);
            logForm.disableButtons(true);
            logForm.showModal(thread -> {
                selectedEntries.forEach(partListEntry -> {
                    if (partListEntry instanceof iPartsDataPartListEntry) {
                        iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                        if (EditEqualizeFieldsHelper.isDIALOGPartListEntry(partListEntry)) {
                            // Es handelt sich um eine DIALOG-BCTE_Position -> Unterscheidung im Text anzeigen
                            lockData.setShowAdditionalMessageText(true);
                            if (iPartsPartListEntry.getDialogBCTEPrimaryKey() == null) {
                                // über Excel-Import hereingekommene TeilePos => behandeln wie nicht DIALOG-TeilePos
                                if (!checkNonDIALOGPartListEntry(iPartsPartListEntry, lockData)) {
                                    return;
                                }
                            } else {
                                // Es handelt sich um eine DIALOG Position, die nicht in einem PSK Produkt vorkommt
                                if (checkDIALOGPartListEntry(iPartsPartListEntry, lockData)) {
                                    return;
                                }
                            }
                        } else {
                            // Es handelt sich um eine Nicht-DIALOG Position oder eine DIALOG Position in einem PSK Produkt
                            if (!checkNonDIALOGPartListEntry(iPartsPartListEntry, lockData)) {
                                return;
                            }
                        }
                        // Die eigene Stückliste entfernen
                        lockData.getModifiedAssemblies().remove(partListEntry.getOwnerAssemblyId());
                    }
                });
                storeEntries(lockData);
            });
            showLockingMessage(lockData);
        }
    }

    /**
     * Überprüft, ob man nicht-DIALOG Positionen oder DIALOG Positionen, die in einem PSK Produkt vorkommen, sperren kann
     *
     * @param iPartsPartListEntry
     * @param lockData
     * @return
     */
    private static boolean checkNonDIALOGPartListEntry(iPartsDataPartListEntry iPartsPartListEntry, EntryLockData lockData) {
        if (lockData.getLockValue()) {
            // Unterscheidung PSK und Nicht-PSK
            iPartsDataAssembly assembly = iPartsPartListEntry.getOwnerAssembly();
            if (assembly.isPSKAssembly()) {
                if (assembly.getDocumentationType().isPKWDocumentationType()) {
                    // Check für Einträge, die einen BCTE Schlüssel haben und in PSK Produkten vorkommen (PSK PKW)
                    if (!checkPSKCarEntry(iPartsPartListEntry, lockData)) {
                        return false;
                    }
                } else {
                    // Check für Einträge, die keinen BCTE Schlüssel haben und in PSK Produkten vorkommen (PSK Truck)
                    if (!checkPSKTruckEntry(iPartsPartListEntry, lockData)) {
                        return false;
                    }
                }
            } else {
                // Check für Einträge, die keinen BCTE Schlüssel haben und nicht in PSK Produkten vorkommen
                if (!checkNonDIALOGAndNonPSKEntry(iPartsPartListEntry, lockData)) {
                    return false;
                }
            }
        }
        // Nicht DIALOG Positionen bzw. PSK Positionen
        addEntryIfLockValueChanged(iPartsPartListEntry, lockData);
        return true;
    }

    /**
     * Überprüft Positionen, die keinen BCTE Schlüssel haben und nicht in PSK Produkten vorkommen
     *
     * @param iPartsPartListEntry
     * @param lockData
     * @return
     */
    private static boolean checkNonDIALOGAndNonPSKEntry(iPartsDataPartListEntry iPartsPartListEntry, EntryLockData lockData) {
        iPartsDataChangeSetList activeChangeSets
                = iPartsRevisionsHelper.getActiveChangeSetsContainingSourceGUID(PartListEntryId.TYPE,
                                                                                iPartsPartListEntry.getFieldValue(FIELD_K_SOURCE_GUID),
                                                                                lockData.getProject());
        if (!activeChangeSets.isEmpty()) {
            lockData.addNotLockableEntry(iPartsPartListEntry.getAsId());
            return false;
        }
        return true;
    }

    /**
     * Überprüft Positionen, die keinen BCTE Schlüssel haben und in PSK Produkten vorkommen (PSK Truck)
     *
     * @param iPartsPartListEntry
     * @param lockData
     * @return
     */
    private static boolean checkPSKTruckEntry(iPartsDataPartListEntry iPartsPartListEntry, EntryLockData lockData) {
        iPartsDataChangeSetEntryList activeChangeSetEntries
                = iPartsRevisionsHelper.getActiveChangeSetEntriesContainingSourceGUID(PartListEntryId.TYPE,
                                                                                      iPartsPartListEntry.getFieldValue(FIELD_K_SOURCE_GUID),
                                                                                      lockData.getProject());
        Map<PartListEntryId, EtkDataPartListEntry> partListEntryMap = new HashMap<>();
        for (iPartsDataChangeSetEntry dataChangeSetEntry : activeChangeSetEntries) {
            String dataObjectId = dataChangeSetEntry.getAsId().getDataObjectId();
            // Hier die PartListEntryIds zum ChangeSet zur Baureihe aufsammeln
            if (StrUtils.isValid(dataObjectId)) {
                IdWithType objectId = IdWithType.fromDBString(PartListEntryId.TYPE, dataObjectId);
                if (objectId != null) {
                    PartListEntryId partListEntryId = IdWithType.fromStringArrayWithTypeFromClass(PartListEntryId.class,
                                                                                                  objectId.toStringArrayWithoutType());
                    EtkDataPartListEntry entry = lockData.getUsageHelper().getEntryForId(partListEntryId, partListEntryMap, false);
                    if ((entry instanceof iPartsDataPartListEntry) && ((iPartsDataPartListEntry)entry).getOwnerAssembly().isPSKAssembly()) {
                        lockData.addNotLockableEntry(iPartsPartListEntry.getAsId());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Überprüft Positionen, die einen BCTE Schlüssel haben und in PSK Produkten vorkommen (PSK PKW)
     *
     * @param iPartsPartListEntry
     * @param lockData
     * @return
     */
    private static boolean checkPSKCarEntry(iPartsDataPartListEntry iPartsPartListEntry, EntryLockData lockData) {
        iPartsDialogBCTEPrimaryKey bcteKey = iPartsPartListEntry.getDialogBCTEPrimaryKey();
        // Check, ob der PSK BCTE Schlüssel in mind. einem aktiven Autorenauftrag liegt
        // (innerhalb eines PSK Moduls)
        if ((bcteKey == null) || lockData.getUsageHelper().isUsedInActiveChangeSetsCheckOnlyPSK(bcteKey)) {
            lockData.addNotLockableEntry(iPartsPartListEntry.getAsId());
            return false;
        }
        return true;
    }

    /**
     * Überprüft, ob man DIALOG Positionen, die nicht in PSK Produkten vorkommen, sperren kann
     *
     * @param iPartsPartListEntry
     * @param lockData
     * @return
     */
    private static boolean checkDIALOGPartListEntry(iPartsDataPartListEntry iPartsPartListEntry, EntryLockData lockData) {
        // Nur beim Sperren prüfen, ob die Position in einem Autorenauftrag liegt. Beim Entsperren könnte
        // die Position in einem Autorenauftrag liegen, weil indirekte Daten geändert wurden
        // (Werksdaten, Ersetzungen, WW, kombinierte Texte, usw). Die direkten Attribute wären nicht
        // betroffen, weil die Position ja gesperrt war.
        boolean lockValue = lockData.getLockValue();
        ASUsageHelper usageHelper = lockData.getUsageHelper();
        iPartsDialogBCTEPrimaryKey bcteKey = iPartsPartListEntry.getDialogBCTEPrimaryKey();
        if (lockValue && usageHelper.isUsedInActiveChangeSetsWithoutPSK(bcteKey)) {
            lockData.addNotLockableEntry(iPartsPartListEntry.getAsId());
            return false;
        }
        List<EtkDataPartListEntry> relatedEntriesWithBCTEWithoutAA
                = usageHelper.getPartListEntriesUsedInAsForEqualizeWithoutPSKEntries(iPartsPartListEntry,
                                                                                     true);
        // Nur beim Sperren prüfen, ob die verknüpften Positionen in offenen Autorenaufträgen liegen
        if (lockValue) {
            boolean relatedEntryInChangeSet = checkDIALOGRelatedEntries(bcteKey, relatedEntriesWithBCTEWithoutAA,
                                                                        iPartsPartListEntry, usageHelper);
            if (!relatedEntryInChangeSet) {
                addModifiedEntryIncludingRelatedEntries(iPartsPartListEntry, relatedEntriesWithBCTEWithoutAA, lockData);
            } else {
                lockData.addNotLockableEntryWithRelatedEntriesInAS(iPartsPartListEntry.getAsId());
                return false;
            }
        } else {
            addModifiedEntryIncludingRelatedEntries(iPartsPartListEntry, relatedEntriesWithBCTEWithoutAA, lockData);
        }
        return true;
    }

    /**
     * Überprüft, ob die über den BCTE Schlüssel verknüpften Positionen in offenen Autorenaufträgen vorkommen. PSK Stücklisten
     * werden nicht berücksichtigt
     *
     * @param bcteKey
     * @param relatedEntriesWithBCTEWithoutAA
     * @param iPartsPartListEntry
     * @param usageHelper
     * @return
     */
    private static boolean checkDIALOGRelatedEntries(iPartsDialogBCTEPrimaryKey bcteKey,
                                                     List<EtkDataPartListEntry> relatedEntriesWithBCTEWithoutAA,
                                                     iPartsDataPartListEntry iPartsPartListEntry,
                                                     ASUsageHelper usageHelper) {
        if ((bcteKey == null) || relatedEntriesWithBCTEWithoutAA.isEmpty()) {
            return false;
        }
        Set<String> usedBCTEKeys = new HashSet<>();
        usedBCTEKeys.add(bcteKey.toString());
        return relatedEntriesWithBCTEWithoutAA.stream()
                .anyMatch(relatedEntry -> {
                    iPartsDialogBCTEPrimaryKey bcteKeyRelatedEntry = ((iPartsDataPartListEntry)relatedEntry).getDialogBCTEPrimaryKey();
                    // Der BCTE Schlüssel, der Original-Position kann in anderen Stücklisten
                    // mehrfach vorkommen. Um einen spezifischen BCTE Schlüssel nicht mehrmals
                    // checken zu müssen, merken wir uns hier die benutzten BCTE Schlüssel
                    if (usedBCTEKeys.contains(bcteKeyRelatedEntry.toString())) {
                        return false;
                    }
                    // Die selektierte Position braucht man nicht zu checken, da wir das oben
                    // schon gemacht haben.
                    if (relatedEntry.getAsId().equals(iPartsPartListEntry.getAsId())) {
                        return false;
                    }
                    // Wurde zum aktuellen BCTE Schlüssel kein offener Autorenauftrag gefunden,
                    // BCET Schlüssel merken
                    if (!usageHelper.isUsedInActiveChangeSetsWithoutPSK(bcteKeyRelatedEntry)) {
                        usedBCTEKeys.add(bcteKey.toString());
                        return false;
                    }
                    return true;
                });
    }

    /**
     * Fügt die Position und die zum BCTE Schlüssel verknüpften Positionen hinzu
     *
     * @param partListEntry
     * @param relatedEntriesWithBCTEWithoutAA
     * @param lockData
     */
    private static void addModifiedEntryIncludingRelatedEntries(EtkDataPartListEntry partListEntry,
                                                                List<EtkDataPartListEntry> relatedEntriesWithBCTEWithoutAA,
                                                                EntryLockData lockData) {
        addEntryIfLockValueChanged(partListEntry, lockData);
        relatedEntriesWithBCTEWithoutAA.forEach(relatedEntry -> addEntryIfLockValueChanged(relatedEntry,
                                                                                           lockData));
    }

    /**
     * Speichert die ent- bzw. gesperrten Positionen
     *
     * @param lockData
     */
    private static void storeEntries(EntryLockData lockData) {
        EtkDataPartListEntryList partListEntryList = lockData.getPartListEntryList();
        if (!partListEntryList.isEmpty()) {
            EtkProject project = lockData.getProject();
            if (iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(project, partListEntryList,
                                                                        iPartsChangeSetSource.LOCK_ENTRIES)) {
                project.getDbLayer().startTransaction();
                try {
                    partListEntryList.saveToDB(project);
                    project.getDbLayer().commit();
                    // Wenn Positionen in anderen
                    Set<AssemblyId> modifiedAssemblies = lockData.getModifiedAssemblies();
                    if (!modifiedAssemblies.isEmpty()) {
                        iPartsDataChangedEventByEdit<AssemblyId> modulesChangedEvent
                                = new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                     iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                     modifiedAssemblies, false);
                        ApplicationEvents.fireEventInAllProjectsAndClusters(modulesChangedEvent, false,
                                                                            true, true,
                                                                            null, null);
                    }
                    ApplicationEvents.fireEventInAllProjectsAndClusters(new DataChangedEvent(null),
                                                                        false, true,
                                                                        true, null,
                                                                        null);
                    iPartsEditPlugin.reloadModulesInEdit(modifiedAssemblies, lockData.getConnector(), true);
                } catch (Exception e) {
                    project.getDbLayer().rollback();
                    Logger.getLogger().handleRuntimeException(e);
                }

            }
        }
    }

    /**
     * Setzt das übergebene Gesperrt-Flag, falls es sich geändert hat
     *
     * @param partListEntry
     * @param lockData
     */
    private static void addEntryIfLockValueChanged(EtkDataPartListEntry partListEntry,
                                                   EntryLockData lockData) {
        if (partListEntry != null) {
            boolean currentValue = isLockedWithDBCheck(partListEntry);
            boolean lockValue = lockData.getLockValue();
            if (currentValue == lockValue) {
                return;
            }
            partListEntry.setFieldValueAsBoolean(FIELD_K_ENTRY_LOCKED, lockValue, DBActionOrigin.FROM_EDIT);
            lockData.addModifiedEntryAndAssembly(partListEntry);
        }
    }

    /**
     * Fügt dem übergebenen Panel den Hinweis hinzu, dass die Position für den Edit gesperrt ist
     *
     * @param partListEditPanel
     * @param partListEntryForEdit
     */
    public static void addLockInfoToPartListEditPanel(GuiPanel partListEditPanel, EtkDataPartListEntry partListEntryForEdit) {
        if (iPartsLockEntryHelper.isLockedWithDBCheck(partListEntryForEdit)) {
            GuiLabel entryLockedLabel = new GuiLabel("!!Die Attribute und Werksdaten der Stücklistenposition sind für den Edit gesperrt!");
            entryLockedLabel.setName("lockedEntryLabel");
            entryLockedLabel.setForegroundColor(Colors.clRed);
            entryLockedLabel.setConstraints(new ConstraintsGridBag(partListEditPanel.getChildren().size(),
                                                                   0, 1, 1, 0, 0,
                                                                   ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE,
                                                                   4, 8, 0, 0));
            partListEditPanel.addChild(entryLockedLabel);
        }
    }

    /**
     * Zeigt einen Dialog an, falls die selektierten Positionen nicht (ent-)sperrt werden dürfen, inkl. Hinweis, welche
     * Positionen in offenen Autorenaufträgen enthalten sind
     *
     * @param lockData
     */
    private static void showLockingMessage(EntryLockData lockData) {
        Set<PartListEntryId> notLockableEntries = lockData.getNotLockableEntries();
        Set<PartListEntryId> notLockableEntriesWithRelatedEntriesInAS = lockData.getNotLockableEntriesWithRelatedEntriesInAS();
        if (!notLockableEntries.isEmpty() || !notLockableEntriesWithRelatedEntriesInAS.isEmpty()) {
            StringBuilder builder = new StringBuilder(TranslationHandler.translate("!!Folgende Stücklisteneinträge " +
                                                                                   "konnten nicht gesperrt werden:"));
            if (!notLockableEntries.isEmpty()) {
                // Nur ausgeben, wenn der Zusatztext gewünscht ist (Unterscheidung macht nur bei DIALOG-BCTE-Positionen Sinn)
                if (lockData.isShowAdditionalMessageText()) {
                    builder.append("\n\n");
                    builder.append(TranslationHandler.translate("!!Positionen mit gleicher Ausführungsart in offenen Autorenaufträgen:"));
                }
                notLockableEntries.forEach(entryId -> {
                    builder.append("\n");
                    builder.append(entryId.getKVari()).append(" / ").append(entryId.getKLfdnr());
                });
            }
            if (!notLockableEntriesWithRelatedEntriesInAS.isEmpty()) {
                // Nur ausgeben, wenn der Zusatztext gewünscht ist (Unterscheidung macht nur bei DIALOG-BCTE-Positionen Sinn)
                if (lockData.isShowAdditionalMessageText()) {
                    builder.append("\n\n");
                    builder.append(TranslationHandler.translate("!!Positionen mit anderer Ausführungsart in offenen Autorenaufträgen:"));
                }
                notLockableEntriesWithRelatedEntriesInAS.forEach(entryId -> {
                    builder.append("\n");
                    builder.append(entryId.getKVari()).append(" / ").append(entryId.getKLfdnr());
                });
            }
            MessageDialog.show(builder.toString(), TranslationHandler.translate("!!Stücklistenpositionen sperren"));
        }
    }

    /**
     * Überprüft, ob die Stücklistenposition gesperrt ist. Befindet sich eine nicht aktualisiert Änderung in der Session
     * wird der Wert der Position direkt über die DB bestimmt.
     *
     * @param partListEntry
     * @return
     */
    public static boolean isLockedWithDBCheck(EtkDataPartListEntry partListEntry) {
        if (partListEntry == null) {
            return false;
        }
        EtkProject project = partListEntry.getEtkProject();
        Boolean delayedDataChangedEvent = (Boolean)Session.get().getAttribute(iPartsPlugin.SESSION_KEY_DELAYED_DATA_CHANGED_EVENT);
        if ((delayedDataChangedEvent != null) && delayedDataChangedEvent) {
            EtkDataPartListEntry partListEntryClone = EtkDataObjectFactory.createDataPartListEntry(project, partListEntry.getAsId());
            if (partListEntryClone.existsInDB()) {
                return partListEntryClone.getFieldValueAsBoolean(FIELD_K_ENTRY_LOCKED);
            }
        }
        return partListEntry.getFieldValueAsBoolean(FIELD_K_ENTRY_LOCKED);
    }

    /**
     * Hilfsklasse zum Halten aller Informationen während dem Sperren bzw Entsperren einer Stücklistenposition
     */
    private static class EntryLockData {

        private final EditModuleFormIConnector connector;
        private final boolean lockValue;
        private final ASUsageHelper usageHelper;
        private final EtkDataPartListEntryList partListEntryList;
        private final Set<AssemblyId> modifiedAssemblies;
        private final Set<PartListEntryId> notLockableEntries;
        private final Set<PartListEntryId> notLockableEntriesWithRelatedEntriesInAS;
        private boolean showAdditionalMessageText;

        public EntryLockData(EditModuleFormIConnector connector, boolean lockValue) {
            this.connector = connector;
            this.lockValue = lockValue;
            this.usageHelper = new ASUsageHelper(connector.getProject());
            this.partListEntryList = new EtkDataPartListEntryList();
            this.modifiedAssemblies = new HashSet<>();
            this.notLockableEntries = new TreeSet<>();
            this.notLockableEntriesWithRelatedEntriesInAS = new TreeSet<>();
        }

        public EditModuleFormIConnector getConnector() {
            return connector;
        }

        public EtkProject getProject() {
            return connector.getProject();
        }

        public ASUsageHelper getUsageHelper() {
            return usageHelper;
        }

        public EtkDataPartListEntryList getPartListEntryList() {
            return partListEntryList;
        }

        public Set<AssemblyId> getModifiedAssemblies() {
            return modifiedAssemblies;
        }

        public Set<PartListEntryId> getNotLockableEntries() {
            return notLockableEntries;
        }

        public Set<PartListEntryId> getNotLockableEntriesWithRelatedEntriesInAS() {
            return notLockableEntriesWithRelatedEntriesInAS;
        }

        public boolean isShowAdditionalMessageText() {
            return showAdditionalMessageText;
        }

        public void setShowAdditionalMessageText(boolean showAdditionalMessageText) {
            this.showAdditionalMessageText = showAdditionalMessageText;
        }

        public boolean getLockValue() {
            return lockValue;
        }

        public void addNotLockableEntry(PartListEntryId entryId) {
            getNotLockableEntries().add(entryId);
        }

        public void addEntryToPartListEntryList(EtkDataPartListEntry partListEntry) {
            getPartListEntryList().add(partListEntry, DBActionOrigin.FROM_EDIT);
        }

        public void addModifiedAssembly(AssemblyId assemblyId) {
            getModifiedAssemblies().add(assemblyId);
        }

        public void addModifiedEntryAndAssembly(EtkDataPartListEntry partListEntry) {
            addEntryToPartListEntryList(partListEntry);
            addModifiedAssembly(partListEntry.getOwnerAssemblyId());
        }

        public void addNotLockableEntryWithRelatedEntriesInAS(PartListEntryId entryId) {
            getNotLockableEntriesWithRelatedEntriesInAS().add(entryId);
        }
    }
}
