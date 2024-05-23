/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseDataWithSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.RelatedInfoSingleEditHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.DataObjectFilterGridWithStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControlsForFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.CopyAndPasteData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.controls.toolbar.AbstractGuiToolComponent;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.os.OsUtils;

import java.util.*;

import static de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder.POSITION_CENTER;
import static de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder.POSITION_NORTH;

/**
 * Formular für die Werkseinsatzdaten innerhalb der RelatedInfo.
 */
public class iPartsRelatedInfoFactoryDataForm extends AbstractRelatedInfoFactoryDataForm {

    public static final String IPARTS_MENU_ITEM_SHOW_FACTORY_DATA = "iPartsMenuItemShowFactoryData";
    public static final String CONFIG_KEY_PRODUCTION_DATA = "Plugin/iPartsEdit/FactoryData";
    public static final String CONFIG_KEY_PRODUCTION_DATA_AS = "Plugin/iPartsEdit/FactoryDataAfterSales";

    private static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.DialogRetail,
                                                                                    iPartsModuleTypes.Dialog_SM_Construction,
                                                                                    iPartsModuleTypes.EDSRetail,
                                                                                    iPartsModuleTypes.SA_TU,
                                                                                    iPartsModuleTypes.PSK_PKW,
                                                                                    iPartsModuleTypes.PSK_TRUCK);

    // für Anzeige von nicht interpretierten ELDAS Einsatzfußnoten
    private GuiPanel panelFootNote;
    private GuiLabel labelFootnote;
    private GuiTextArea textareaFootnote;
    private GuiToolbar footNoteToolbar;
    private EditToolbarButtonMenuHelper footnoteToolbarHelper;
    private List<iPartsDataFootNote> dataFootNoteList = new ArrayList<>(); // Liste der nicht interpretierten Einsatzfußnoten
    private iPartsDataFactoryDataList footNoteFactoryDataToDelete;         // Liste der Werkseinsatzdaten, die gelöscht werden müssen, falls die Fußnoten gelöscht werden
    private boolean textAreaFootNoteIsVisible = false;
    private boolean isUsedInUnify;
    private OnChangeEvent onChangeEvent;

    // Für Edit Funktion
    private EditFormIConnector editModuleFormConnector;

    private iPartsDataFactoryDataList listOfUnfilteredFactoryDataInForm = new iPartsDataFactoryDataList();
    private EtkDataObjectList<iPartsDataFactoryData> factoryDataListFromCopy;

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_FACTORY_DATA, "!!Werkseinsatzdaten anzeigen",
                                EditDefaultImages.edit_factoryData.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_FACTORY_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste gültig für den gewünschten Modultyp?
        boolean menuItemVisible = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            menuItemVisible = relatedInfoIsVisible(connector.getSelectedPartListEntries().get(0), isEditContext(connector, true),
                                                   connector);
        }

        // Im Edit nur Menüeinträge zur Bearbeitung anzeigen
        if (menuItemVisible && isEditContext(connector, true)) {
            menuItemVisible = false;
        }

        // Separator und Menüeintrag aktualisieren
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_FACTORY_DATA, menuItemVisible);
    }

    /**
     * Ist die Werkseinsatzdaten-Funktion im Related Info Dialog verfügbar?
     * Dies hat nichts mit dem Vorhandensein oder dem Zustand des Stücklisten-Icons zum Aufruf dieser Funktion zu tun.
     *
     * @param partListEntry
     * @param isEditRelatedInfo
     * @param connector
     * @return
     */
    public static boolean relatedInfoIsVisible(EtkDataPartListEntry partListEntry, boolean isEditRelatedInfo, AbstractJavaViewerFormIConnector connector) {
        EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
        if (!relatedInfoIsVisible(ownerAssembly, VALID_MODULE_TYPES)) {
            return false;
        }

        iPartsModuleTypes moduleType = iPartsModuleTypes.getType(ownerAssembly.getEbeneName());
        boolean retailMode = !moduleType.isConstructionRelevant() && !isEditContext(connector, false);

        // Retail-Anzeige -> Werkseinsatzdaten nur dann anzeigen, wenn auch Werkseinsatzdaten mit aktivem Retail-Filter existieren
        // Konstruktions-Stücklisten -> Werkseinsatzdaten nur dann anzeigen, wenn auch Werkseinsatzdaten existieren
        // Edit -> Werkseinsatzdaten immer anzeigen wenn Stücklistentyp passt (wird oben ja schon überprüft)
        iPartsFactoryData.ValidityType validity = iPartsFactoryData.ValidityType.NOT_AVAILABLE;
        if (partListEntry instanceof iPartsDataPartListEntry) {
            validity = ((iPartsDataPartListEntry)partListEntry).getFactoryDataValidity();
        }
        boolean isVirtualPartList = iPartsVirtualNode.isVirtualId(ownerAssembly.getAsId());
        if (isVirtualPartList) {
            return validity == iPartsFactoryData.ValidityType.VALID_FOR_CONSTRUCTION;
        } else if (retailMode) {
            return validity == iPartsFactoryData.ValidityType.VALID;
        } else if (isEditRelatedInfo) {
            return iPartsUserSettingsHelper.isSingleEdit(connector.getProject());
        } else {
            return validity == iPartsFactoryData.ValidityType.VALID; // gilt auch für den Menüpunkt bei ReadOnly TU bearbeiten
        }
    }

    public iPartsRelatedInfoFactoryDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                            IEtkRelatedInfo relatedInfo, boolean scaleFromParent) {
        super(dataConnector, parentForm, relatedInfo, CONFIG_KEY_PRODUCTION_DATA_AS + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS, "!!Werkseinsatzdaten After-Sales:",
              CONFIG_KEY_PRODUCTION_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS, "!!Werkseinsatzdaten Produktion:", scaleFromParent, true);

        // Bei PSK-Modulen mit gültigem BCTE-Schlüssel ist kein Edit erlaubt
        setReadOnly(isRetailFilter() || !(editMode && isEditContext(dataConnector, true)) || (isPSKAssembly() && isPartListEntryWithValidBCTEKey()));

        if (dataConnector.getEditContext() instanceof iPartsRelatedInfoEditContext) {
            editModuleFormConnector = ((iPartsRelatedInfoEditContext)getConnector().getEditContext()).getEditFormConnector();
        }
        getAdditionalTopPanel().setVisible(false);
    }

    public boolean isUsedInUnify() {
        return isUsedInUnify;
    }

    public void setUsedInUnify(boolean usedInUnify) {
        isUsedInUnify = usedInUnify;
        if (isUsedInUnify) {
            toolbarHelperTop.hideSeparatorInToolbarAndMenu(LINK_SEPARATOR_NAME, null);
            toolbarHelperTop.hideToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD);
            toolbarHelperTop.hideToolbarButton(EditToolbarButtonAlias.EDIT_GO_TO_BCTE);
            toolbarHelperTop.hideToolbarButton(EditToolbarButtonAlias.EDIT_UNLINK_FD);
            if (gridTop instanceof DataObjectFilterGridWithStatus) {
                ((DataObjectFilterGridWithStatus)gridTop).getStatusContextMenu().setVisible(false);
            }
            // Rückmeldedaten bearbeiten ausblenden
            hideEditResponseData();

        } else {
            toolbarHelperTop.showSeparatorInToolbarAndMenu(LINK_SEPARATOR_NAME, null);
            toolbarHelperTop.showToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD);
            toolbarHelperTop.showToolbarButton(EditToolbarButtonAlias.EDIT_GO_TO_BCTE);
            toolbarHelperTop.showToolbarButton(EditToolbarButtonAlias.EDIT_UNLINK_FD);
        }
    }

    public OnChangeEvent getOnChangeEvent() {
        return onChangeEvent;
    }

    public void setOnChangeEvent(OnChangeEvent onChangeEvent) {
        this.onChangeEvent = onChangeEvent;
    }

    protected void handleOnChangeEvent() {
        if (onChangeEvent != null) {
            onChangeEvent.onChange();
        }
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    @Override
    protected void postCreateGui() {
        super.postCreateGui();
        createFootnotePanel();
        setCheckboxRetailFilterVisible(editMode);
        createCopyPasteToolbarbuttons();
        createLinkUnlinkToolbarbuttons();

        if (editMode) {
            // Nach Eldas Stückliste abfragen. Ob die Fußnoten sichtbar sind, ist hier noch nicht gesetzt
            // Falls sie nicht sichtbar sind, wird das ganze Panel samt Toolbar ausgeblendet
            if (isELDASPartList()) {
                footnoteToolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), footNoteToolbar);
                footnoteToolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_DELETE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doDeleteFootNotes();
                    }
                });
            }
        }

        getGui().addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, getGui()) {
            @Override
            public boolean isFireOnceValid(Event event) {
                //gibt es "nicht interpretierten ELDAS Einsatzfußnoten"?
                return isTextAreaFootNoteVisible();
            }

            @Override
            public void fireOnce(Event event) {
                // Höhe des TopPanels
                int height = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                // 1/3 der Gesamthöhe von TopPanel wird verglichen mit der Höhe, die die Textarea braucht
                textareaFootnote.setMinimumHeight(81); // kleinste Höhe ca 5 Zeilen
                height = Math.min(height / 3, textareaFootnote.getPreferredHeight());
                height = Math.max(height, textareaFootnote.getMinimumHeight() + 2 + labelFootnote.getPreferredHeight()
                                          + (editMode ? footNoteToolbar.getPreferredHeight() : 0)); // + 2 für Ränder der ScrollPane
                panelFootNote.setMinimumHeight(height);
            }
        });
    }

    public boolean copyFactoryData(boolean withUpdate) {
        boolean isDialogPart;
        boolean isLinkedSource;
        iPartsDataFactoryDataList filteredData = FactoryDataHelper.getDataForCopy(listOfUnfilteredFactoryDataInForm);
        if (isELDASPartList()) {
            isDialogPart = false;
            isLinkedSource = false;
        } else {
            isDialogPart = true;
            isLinkedSource = isSomethingLinkedInGrid();
        }

        String guid;
        if (isRetailPartList) {
            guid = partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
        } else {
            guid = partListEntry.getFieldValue(iPartsConst.FIELD_K_LFDNR);
        }

        boolean result = (filteredData != null) && !filteredData.isEmpty();
        if (withUpdate) {
            if (result) {
                CopyAndPasteData.copyFactoryDataOfPart(filteredData, isDialogPart, isLinkedSource, guid);
                toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_FACTORY_DATA, !iPartsLockEntryHelper.isLockedWithDBCheck(partListEntry));
                // Richtigen Text anzeigen, aber Button bleibt disabled, da es keinen Sinn macht sich selber zu koppeln
                if (editMode) {
                    toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD, false);
                    AbstractGuiToolComponent pasteAndLinkButton = getToolbarHelper(true).getToolbarManager().getButton(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD.getAlias());
                    pasteAndLinkButton.setTooltip(TranslationHandler.translate(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD.getTooltip()));
                }
            } else {
                MessageDialog.show("!!Die Auswahl enthält keine relevanten Werksdaten zum Kopieren.",
                                   EditToolbarButtonAlias.EDIT_COPY_FACTORY_DATA.getTooltip());
            }
        } else if (result) {
            CopyAndPasteData.copyFactoryDataOfPart(filteredData, isDialogPart, isLinkedSource, guid);
        }

        return result;
    }

    private void setDataChangedInEditContext() {
        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)getConnector().getEditContext();
            editContext.setFireDataChangedEvent(true);
        }
    }

    @Override
    protected void pasteFactoryData() {
        EtkDataObjectList<iPartsDataFactoryData> results = CopyAndPasteData.pasteFactoryDataOfPart(partListEntry, listOfUnfilteredFactoryDataInForm, getProject());
        if (results != null) {
            // bei Statusänderungen diese Abhandeln und den aktuellen Eintrag im Changeset speichern
            // Werkseinsatzdaten werden als AS-Werkseinsatzdaten eingefügt
            statusChangedForDataObjects(results, true);
            handleOnChangeEvent();
            setDataChangedInEditContext();
        }
    }

    @Override
    protected void pasteAndLinkFactoryData() {
        String msg = TranslationHandler.translate("!!Wollen Sie die Kopplung der Werkseinsatzdaten speichern?");
        ModalResult linkingWanted = MessageDialog.showYesNo(msg, "!!Kopplung");
        if (linkingWanted == ModalResult.YES) {
            GuiWindow.showWaitCursorForCurrentWindow(true);
            EtkProject project = getProject();
            iPartsDataFactoryDataList afterOverwrite = new iPartsDataFactoryDataList();
            iPartsDataFactoryDataList changedDataInChangeSetToCommit = new iPartsDataFactoryDataList();
            EtkDataObjectList changedDataWithoutChangeSet = new GenericEtkDataObjectList();
            // Falls schon eine Kopplung an der Ziel-Teilepos besteht, muss die Kopplung mit der urpsrünglichen Quelle aufgehoben werden
            // und die Kopplung mit der neuen Quelle erstellt werden
            if (CopyAndPasteData.overwriteLinkFlag(afterOverwrite, getDataObjectList(true, iPartsDataFactoryData.class))) {
                // GUID wird später überschrieben, Flags jetzt entfernen
                CopyAndPasteData.addWarningMessageUnlink("!!Mit dem Ziel-Stücklisteneintrag bestand schon eine Kopplung. Diese wurde aufgelöst.");
            }

            boolean hasChanges = CopyAndPasteData.pasteAndLink(partListEntry, listOfUnfilteredFactoryDataInForm, project,
                                                               changedDataInChangeSetToCommit, changedDataWithoutChangeSet,
                                                               true);


            if (hasChanges) {
                iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)getConnector().getEditContext();
                editContext.addModifiedAssemblyIds(Collections.singletonList(partListEntry.getOwnerAssemblyId()));

                saveChangesForLinking(changedDataWithoutChangeSet, changedDataInChangeSetToCommit, afterOverwrite);

                List<String> warnings = CopyAndPasteData.getWarningMessageUnlink();
                StringBuilder collectedWarnings = new StringBuilder();
                boolean hasWarnings = !warnings.isEmpty();
                if (hasWarnings) {
                    for (String warning : warnings) {
                        collectedWarnings.append(warning);
                        collectedWarnings.append(OsUtils.NEWLINE);
                    }

                    MessageDialog.show(collectedWarnings.toString(), "!!Kopplung", MessageDialogIcon.INFORMATION, MessageDialogButtons.OK);
                    CopyAndPasteData.getWarningMessageUnlink().clear();
                }

                // Wechsel zur Konstruktionsstückliste erst enablen, wenn es eine Guid am partlistEntry gibt
                toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_GO_TO_BCTE, !hasWarnings);
                toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_UNLINK_FD, !hasWarnings);

            }
            GuiWindow.showWaitCursorForCurrentWindow(false);
            handleOnChangeEvent();
            setDataChangedInEditContext();
        }
    }

    @Override
    protected void unlinkFactoryData() {
        unlinkDataAndSave();
        handleOnChangeEvent();
    }

    @Override
    protected void gotoLinkedConstructionEntry() {
        EtkDataPartListEntry clone = partListEntry.cloneMe(getProject());
        clone.setFieldValue(iPartsConst.FIELD_K_SOURCE_GUID, partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_DD_LINKED_FACTORY_DATA_GUID), DBActionOrigin.FROM_EDIT);
        iPartsGotoHelper.gotoDialogConstructionPartList(clone, parentForm.getConnector());
    }

    /**
     * Führt das entkoppeln durch und speichert die Änderungen
     */
    public void unlinkDataAndSave() {
        String msg = TranslationHandler.translate("!!Wollen Sie die Entkopplung der Werkseinsatzdaten speichern?");
        ModalResult linkingWanted = MessageDialog.showYesNo(msg, "!!Kopplung");
        if (linkingWanted == ModalResult.YES) {
            GuiWindow.showWaitCursorForCurrentWindow(true);
            List<iPartsDataFactoryData> dataInTopGrid = getDataObjectList(true, iPartsDataFactoryData.class);
            iPartsDataFactoryDataList overwriteLink = new iPartsDataFactoryDataList();
            EtkDataObjectList changedDataWithoutChangeSet = new GenericEtkDataObjectList();
            EtkDataObjectList changedDataInChangeSetToCommit = new GenericEtkDataObjectList();
            if (!dataInTopGrid.isEmpty()) {
                String guidOfLinkDestination = partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                boolean hasChanges = CopyAndPasteData.overwriteLinkFlag(overwriteLink, dataInTopGrid);

                boolean hasChangesUnlink = CopyAndPasteData.unlink(guidOfLinkDestination, getProject(), changedDataWithoutChangeSet);
                if (hasChanges && hasChangesUnlink) {
                    saveChangesForLinking(changedDataWithoutChangeSet, changedDataInChangeSetToCommit, overwriteLink);
                    toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_UNLINK_FD, false);
                    toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_GO_TO_BCTE, false);
                }
            }
            GuiWindow.showWaitCursorForCurrentWindow(false);
        }
    }

    /**
     * Speichert die Änderungen, die durch das Koppeln entstanden sind
     *
     * @param changedDataWithoutChangeSet    Änderungen in DA-DIALOG
     * @param changedDataInChangeSetToCommit Der Datensatz der Committed wird, der durch das Koppeln entstanden ist
     * @param afterOverwrite                 Der Datensatz der Committed wird, der durch das Entkoppeln entstanden ist
     */
    private void saveChangesForLinking(final EtkDataObjectList changedDataWithoutChangeSet,
                                       final EtkDataObjectList changedDataInChangeSetToCommit,
                                       final iPartsDataFactoryDataList afterOverwrite) {
        final EtkProject project = getProject();
        boolean fireDataChangedEvent = false;
        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            AbstractRevisionChangeSet abstractRevisionChangeSet =
                    ((iPartsRelatedInfoEditContext)getConnector().getEditContext()).getAuthorOrderChangeSetForEdit();
            fireDataChangedEvent = CopyAndPasteData.saveChangesForLinkingRunnable(project,
                                                                                  changedDataWithoutChangeSet,
                                                                                  changedDataInChangeSetToCommit,
                                                                                  afterOverwrite,
                                                                                  abstractRevisionChangeSet,
                                                                                  getStatusFieldName(),
                                                                                  getSeriesNoFieldName());
        }

        // virtuelles Feld entfernen, damit es später wieder neu geladen wird
        DBDataObjectAttributes attributes = partListEntry.getAttributes();
        if (attributes.fieldExists(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_DD_LINKED_FACTORY_DATA_GUID)) {
            partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_DD_LINKED_FACTORY_DATA_GUID, DBActionOrigin.FROM_DB);
        }

        EtkDataAssembly.removeDataAssemblyFromAllCaches(project, partListEntry.getOwnerAssemblyId());
        if (!fireDataChangedEvent) {
            clearAffectedCaches();
        }
    }

    /**
     * Hier wird die gleiche Routine aufgerufen, die durchlaufen wird, falls die RelatedInfo mit OK bestätigt wird
     * bei Werkeinsatzdatenänderungen
     */
    private void clearAffectedCaches() {
        editModuleFormConnector.clearFilteredEditPartListEntries();
        EtkDataAssembly.removeCacheForActiveChangeSets(getProject());
        EtkDataAssembly currentAssembly = editModuleFormConnector.getCurrentAssembly();
        if (currentAssembly != null) {
            for (EtkDataPartListEntry editPartListEntry : currentAssembly.getPartListUnfiltered(null)) {
                if (editPartListEntry instanceof iPartsDataPartListEntry) {
                    ((iPartsDataPartListEntry)editPartListEntry).clearDIALOGChangesAttributes();
                }
            }
        }


        // "PEM ab/bis auswerten"-Flags neu berechnen
        if (partListEntry != null) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsDataPartListEntry = (iPartsDataPartListEntry)partListEntry;
                iPartsDataPartListEntry.updatePEMFlagsFromReplacements();

                // Alle Werkseinsatzdaten für den Retail MIT Berücksichtigung von Ersetzungen müssen für
                // alle Stücklisteneinträge neu berechnet werden
                EtkDataAssembly editAssembly = editModuleFormConnector.getCurrentAssembly();
                if (editAssembly instanceof iPartsDataAssembly) {
                    ((iPartsDataAssembly)editAssembly).clearAllFactoryDataForRetailForPartList();
                }

                iPartsDataPartListEntry.clearDIALOGChangesAttributes();

            }

            // Alle betroffenen Module aus dem Cache entfernen, da Änderungen an den Retail-Werkseinsatzdaten
            // sich über den BCTE-Schlüssel an mehreren Modulen auswirken kann
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(getConnector().getRelatedInfoData().getAsPartListEntry(getProject()).getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID));
            if (bctePrimaryKey != null) {
                ASUsageHelper usageHelper = new ASUsageHelper(getProject());
                List<PartListEntryId> partListEntryIds = usageHelper.getPartListEntryIdsUsedInAS(bctePrimaryKey);
                if (partListEntryIds != null) {
                    Set<AssemblyId> assemblyIdsToClear = new HashSet<>();
                    for (PartListEntryId partListEntryId : partListEntryIds) {
                        assemblyIdsToClear.add(partListEntryId.getOwnerAssemblyId());
                    }
                    for (AssemblyId assemblyId : assemblyIdsToClear) {
                        EtkDataAssembly.removeDataAssemblyFromCache(getProject(), assemblyId);
                    }
                }
            }

        }
        EtkDataAssembly.removeDataAssemblyFromCache(getProject(), editModuleFormConnector.getCurrentAssembly().getAsId());
        // Veränderte Module müssen aus dem Cache gelöscht und im Edit befindliche Module müssen neu
        // geladen werden
        iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)getConnector().getEditContext();
        Set<AssemblyId> modifiedAssemblyIds = editContext.getModifiedAssemblyIds();
        if (!modifiedAssemblyIds.isEmpty()) {
            for (AssemblyId modifiedAssemblyId : modifiedAssemblyIds) {
                EtkDataAssembly.removeDataAssemblyFromCache(getProject(), modifiedAssemblyId);
            }
            iPartsEditPlugin.reloadModulesInEdit(modifiedAssemblyIds, editModuleFormConnector);
        }
        getProject().fireProjectEvent(new DataChangedEvent(null), true);
    }

    @Override
    protected void enableButtonsAndMenu() {
        super.enableButtonsAndMenu();
        // Check, ob die Position zu den Werksdaten gesperrt ist
        boolean isLocked = iPartsLockEntryHelper.isLockedWithDBCheck(getPartListEntry());
        if (!isELDASPartList()) {
            // bei DIALOG und einer ungültigem BCTE-Schlüssel => tu so, als wäre Position gesperrt
            isLocked |= !isPartListEntryWithValidBCTEKey();
        }
        // Status darf nicht gesetzt werden, wenn die Position gesperrt ist
        setStatusMenuEnabled(!isLocked && !isReadOnly);
        // Copy/ Paste Buttons behandeln
        if (toolbarHelperTop != null) {
            boolean copyButtonActive = false;
            if (editMode && !isReadOnly) {
                copyButtonActive = !listOfUnfilteredFactoryDataInForm.isEmpty();
            } else {
                if (!isRetailPartList) {
                    // in der Konstruktion soll der Copy Button aktiv sein wenn ein Changeset aktiv ist
                    EtkRevisionsHelper revisionsHelper = getProject().getRevisionsHelper();
                    if (revisionsHelper != null) {
                        if (revisionsHelper.getActiveRevisionChangeSetForEdit() != null) {
                            copyButtonActive = !listOfUnfilteredFactoryDataInForm.isEmpty();
                        }
                    }
                }
            }
            // Copy nur enablen:
            // Konstruktion mit aktivem Autorenauftrag und Werksdaten vorhanden
            // TU bearbeiten und Werksdaten vorhanden
            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_COPY_FACTORY_DATA, copyButtonActive);

            // Paste erst enablen, wenn die Position nicht gesperrt ist und etwas im Zwischenspeicher liegt
            boolean isCopyCacheFilled = !isLocked && CopyAndPasteData.isCopyCacheFilled();
            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_FACTORY_DATA, (editMode && !isReadOnly && isCopyCacheFilled));
            // Wenn Ziel-Teilepos schon die Quelle einer Kopplung ist, kann sie nicht nochmal gekoppelt werden
            // Wenn Quell-Teilepos schon die Ziel-Teilepos einer anderen Teilepos ist, kann nicht gekoppelt werden
            boolean isSourceAlreadyATarget = false;
            boolean isTargetAlreadyASource = false;
            boolean isSourceTheTarget = false;
            if (isCopyCacheFilled && editMode) {
                String guidOfLinkTarget = partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                isSourceAlreadyATarget = CopyAndPasteData.isSourceAlreadyATarget();
                isTargetAlreadyASource = CopyAndPasteData.isTargetAlreadyASource(getProject(), guidOfLinkTarget);
                isSourceTheTarget = CopyAndPasteData.isSourceTheTarget(guidOfLinkTarget);
                AbstractGuiToolComponent pasteAndLinkButton = getToolbarHelper(true).getToolbarManager().getButton(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD.getAlias());
                if (isSourceAlreadyATarget || isTargetAlreadyASource) {
                    pasteAndLinkButton.setTooltip(TranslationHandler.translate("!!Koppeln führt zu einer Kette!"));
                } else {
                    pasteAndLinkButton.setTooltip(TranslationHandler.translate(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD.getTooltip()));
                }
            }
            // Quelle darf nicht gleichzeitig das Ziel sein
            // Ziel darf nicht die Quelle einer anderen Kopplung sein,
            // Quelle darf sich nicht mit sich selber verkoppeln
            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD, (editMode && !isReadOnly && isCopyCacheFilled && !isELDASPartList()
                                                                                                 && !isSourceAlreadyATarget && !isTargetAlreadyASource && !isSourceTheTarget));

            // Nur Entkopplen, wenn die Position nicht gesperrt ist
            boolean isSomethingLinkedInGrid = !isLocked && isSomethingLinkedInGrid();
            boolean validGuidToGo = StrUtils.isValid(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_DD_LINKED_FACTORY_DATA_GUID));
            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_UNLINK_FD, isSomethingLinkedInGrid);
            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_GO_TO_BCTE, validGuidToGo);

            // Falls die selektierten Werkseinsatzdaten verkoppelt sind oder gesperrt, darf nicht gelöscht oder editiert werden
            boolean deleteButtonEnabled = toolbarHelperTop.isToolbarButtonEnabled(EditToolbarButtonAlias.EDIT_DELETE);
            boolean editButtonEnabled = toolbarHelperTop.isToolbarButtonEnabled(EditToolbarButtonAlias.EDIT_WORK);
            boolean isLinked = isSomethingLinkedInSelection();
            if (deleteButtonEnabled && (isLinked || isLocked)) {
                toolbarHelperTop.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, getGrid(true).getContextMenu(), false);
            }
            if (editButtonEnabled && (isLinked || isLocked)) {
                toolbarHelperTop.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, getGrid(true).getContextMenu(), false);
            }

            // Wenn die Position gesperrt ist, darf nichts angelegt werden
            boolean newButtonEnabled = toolbarHelperTop.isToolbarButtonEnabled(EditToolbarButtonAlias.EDIT_NEW);
            if (newButtonEnabled && isLocked) {
                toolbarHelperTop.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, getGrid(true).getContextMenu(), false);
            }
        }
        // Ist die Position gesperrt, dürfen die Produktionsdaten nicht editiert werden (weil daraus neue AS-Daten entstehen)
        if (toolbarHelperBottom != null) {
            boolean editButtonEnabled = toolbarHelperBottom.isToolbarButtonEnabled(EditToolbarButtonAlias.EDIT_WORK);
            if (editButtonEnabled && isLocked) {
                toolbarHelperBottom.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, getGrid(false).getContextMenu(), false);
            }
        }

        if (footnoteToolbarHelper != null) {
            footnoteToolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, footNoteToolbar.getContextMenu(), !isReadOnly);
        }
    }

    private void setStatusMenuEnabled(boolean isEnabled) {
        setStausMenuEnabledForGrid(gridTop, isEnabled);
        setStausMenuEnabledForGrid(gridBottom, isEnabled);
    }

    private void setStausMenuEnabledForGrid(DataObjectGrid grid, boolean isEnabled) {
        if (grid instanceof DataObjectFilterGridWithStatus) {
            ((DataObjectFilterGridWithStatus)grid).getStatusContextMenu().setEnabled(isEnabled);
        }
    }

    @Override
    public void onDoubleClick(boolean isTop) {
        if (isTop) {
            // Selektion darf nicht verlinkt sein
            if (!isSomethingLinkedInSelection()) {
                super.onDoubleClick(isTop);
            }
        } else {
            super.onDoubleClick(isTop);
        }
    }

    @Override
    protected void doEdit(boolean top) {
        iPartsDataFactoryData selectedFactoryData = getSelection(top, iPartsDataFactoryData.class);
        if (selectedFactoryData != null) {
            iPartsDataFactoryData result;
            if (isUsedInUnify()) {
                result = EditUserControlsForFactoryData.showEditFactoryData(getConnector(), this, partListEntry,
                                                                            selectedFactoryData, listOfUnfilteredFactoryDataInForm, isReadOnly);

            } else {
                result = EditUserControlsForFactoryData.showEditFactoryData(getConnector(), this, partListEntry,
                                                                            selectedFactoryData.getAsId(), listOfUnfilteredFactoryDataInForm, isReadOnly);
            }
            if (result != null) {
                // Falls die Quelle iParts ist, dann wird kein neuer Werkseinsatzdatensatz angelegt und der Status muss demnach auch nicht
                // angepasst werden
                if (selectedFactoryData.getSource() != iPartsImportDataOrigin.IPARTS) {
                    statusChangedForDataObject(result, top);
                } else {
                    if (isUsedInUnify() && top) {
                        statusChangedForDataObject(result, top);
                    } else {
                        saveDataObjectWithUpdate(result);
                    }
                }

                enableButtonsAndMenu();
                handleOnChangeEvent();
            }
        }
    }

    @Override
    protected void doNew(boolean top) {
        iPartsDataFactoryData result = EditUserControlsForFactoryData.showCreateFactoryData(getConnector(), this, partListEntry, listOfUnfilteredFactoryDataInForm, isReadOnly);
        if (result != null) {
            // handelt eventuelle Statusänderungen ab inkl. dem speichern des aktuellen Eintrags im Changeset
            statusChangedForDataObject(result, top);
            enableButtonsAndMenu();
            handleOnChangeEvent();
        }
    }

    @Override
    protected void doDelete(boolean top) {
        if (!isUsedInUnify()) {
            EtkDataObjectList deletedList = doDeleteDataObjects(top, iPartsDataFactoryData.class);
            EtkDataObjectList factoryDataWithChangeEntries = new GenericEtkDataObjectList();
            for (Object factoryData : deletedList.getDeletedList()) {
                if (factoryData instanceof iPartsDataFactoryData) {
                    iPartsDataFactoryData iPartsDataFactoryData = (iPartsDataFactoryData)factoryData;
                    List<iPartsDataDIALOGChange> dialogChangesOfFactoryData = getDataDIALOGChanges(iPartsDataFactoryData);
                    for (iPartsDataDIALOGChange dialogChange : dialogChangesOfFactoryData) {
                        if (dialogChange.existsInDB()) {
                            iPartsDataFactoryData.setFieldValue(iPartsConst.FIELD_DFD_STATUS, iPartsDataReleaseState.NOT_RELEVANT.getDbValue(), DBActionOrigin.FROM_EDIT);
                            factoryDataWithChangeEntries.add(iPartsDataFactoryData, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }
            if (!factoryDataWithChangeEntries.isEmpty()) {
                iPartsRelatedEditHelper.statusChanged(factoryDataWithChangeEntries, this, false);
            }
        } else if (top) {
            List<iPartsDataFactoryData> workingList = new DwList<>(listOfUnfilteredFactoryDataInForm.getAsList());
            listOfUnfilteredFactoryDataInForm.clear(DBActionOrigin.FROM_DB);
            for (List<EtkDataObject> selectedDataObjectList : getGrid(top).getMultiSelection()) {
                for (EtkDataObject selectedDataObject : selectedDataObjectList) {
                    if (selectedDataObject instanceof iPartsDataFactoryData) {
                        workingList.remove(selectedDataObject);
                    }
                }
            }
            iPartsDataFactoryDataList helpList = new iPartsDataFactoryDataList();
            helpList.addAll(workingList, DBActionOrigin.FROM_DB);
            statusChangedForDataObjects(helpList, top);
            handleOnChangeEvent();
        }
    }

    private void doDeleteFootNotes() {
        saveDataObjectsWithUpdate(footNoteFactoryDataToDelete);
    }

    @Override
    protected String getStatusFieldName() {
        return iPartsConst.FIELD_DFD_STATUS;
    }

    protected String getSeqNoFieldName() {
        return iPartsConst.FIELD_DFD_SEQ_NO;
    }

    @Override
    public String getSourceFieldName() {
        return iPartsConst.FIELD_DFD_SOURCE;
    }

    // Retail-Werkseinsatzdaten für den aktuellen Stücklisteneintrag neu laden und für alle Stücklisteneinträge neu berechnen
    @Override
    protected void reloadEditableDataAndUpdateEditContext() {
        if (editModuleFormConnector != null) {
            EtkDataAssembly ownerAssembly = editModuleFormConnector.getCurrentAssembly();
            if (ownerAssembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsOwnerAssembly = (iPartsDataAssembly)ownerAssembly;
                DBDataObjectList<EtkDataPartListEntry> partListEntries = new DBDataObjectList<>();

                // Werkseinsatzdaten vom Original-Stücklisteneintrag aus der Edit-Stückliste neu laden (partListEntry
                // sollte bereits auf den Original-Stücklisteneintrag zeigen, aber sicher ist sicher, da EtkRelatedInfoData
                // auch eigene Stücklisteneinträge zurückliefern kann)
                partListEntry = ownerAssembly.getPartListEntryFromKLfdNrUnfiltered(partListEntry.getAsId().getKLfdnr());
                partListEntries.add(partListEntry, DBActionOrigin.FROM_DB);

                // Dieser Aufruf (mit nur einem Element in der Liste) führt zu einer Sonderbehandlung in der Lade-Funktion,
                // wodurch hier keine Pseudo-Transaktionen gebraucht werden
                iPartsOwnerAssembly.loadAllFactoryDataForRetailForPartList(partListEntries);

                if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
                    iPartsRelatedInfoEditContext editContext = ((iPartsRelatedInfoEditContext)getConnector().getEditContext());
                    editContext.setFireDataChangedEvent(true);
                    editContext.setUpdateRetailFactoryData(true);
                    editContext.setUpdateEditAssemblyData(true);
                }
            }
        }
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(boolean top) {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<>();
        EtkDisplayField displayField;

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_FACTORY, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsDataVirtualFieldsDefinition.DFD_FACTORY_SIGNS, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_PEMA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_PEMTA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsDataVirtualFieldsDefinition.DFD_PEMA_RESPONSE_DATA_AVAILABLE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_STCA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_PEMB, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_PEMTB, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsDataVirtualFieldsDefinition.DFD_PEMB_RESPONSE_DATA_AVAILABLE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_STCB, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_SPKZ, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_CRN, false, false);
        defaultDisplayFields.add(displayField);

        if (top && isRetailFilter() && !isELDASPartList()) {
            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_DATA_ID, false, false);
            defaultDisplayFields.add(displayField);

            // Nachdem es bei ELDAS Ersetzungen keine RFME Flags gibt kann es dort auch keine virtuellen Werksdaten geben,
            // also wird die Spalte nur im Retailfilter bei nicht ELDAS Dokumentation angezeigt
            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsDataVirtualFieldsDefinition.DFD_INHERITED_FACTORY_DATA, false, false);
            defaultDisplayFields.add(displayField);

            // Das gleiche für die Filter-Informationen, die aber nur im DEVELOPMENT-Modus standardmäßig angezeigt werden sollen
            if (Constants.DEVELOPMENT) {
                displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsDataVirtualFieldsDefinition.DFD_FILTER_INFO,
                                                   false, false);
                defaultDisplayFields.add(displayField);
            }
        }

        if (!top) { // Freigabedatum Produktion nur bei Werkseinsatzdaten Produktion anzeigen
            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_ADAT, false, false);
            defaultDisplayFields.add(displayField);
        }

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_SOURCE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_STATUS, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    @Override
    protected List<IdWithType> getSelectedObjectIds(boolean top) {
        return getGrid(top).getSelectedObjectIds(iPartsConst.TABLE_DA_FACTORY_DATA);
    }

    @Override
    protected void setSelectedObjectIds(boolean top, List<IdWithType> selectedIds) {
        getGrid(top).setSelectedObjectIds(selectedIds, iPartsConst.TABLE_DA_FACTORY_DATA);
    }


    /**
     * DataObjectList für das jeweilige Grid bestimmen
     *
     * @param top
     * @return
     */
    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        if (isRetailPartList) {
            if (isUsedInUnify()) {
                if ((factoryDataListFromCopy != null) && !factoryDataListFromCopy.isEmpty()) {
                    list.addAll(factoryDataListFromCopy, DBActionOrigin.FROM_DB);
                }
            } else {
                list = FactoryDataHelper.getFactoryDataList(partListEntry, isRetailFilter(), top, getProject());
            }

            // Falls im Edit der Retailfilter angeschalten wird, dann die Daten nicht fürs Kopieren verwenden, da sie gefiltert sind
            // Die Daten verwenden, die beim Öffnen der Related Edit gespeichert wurden
            if (!isRetailFilter() || isUsedInUnify()) {
                listOfUnfilteredFactoryDataInForm.addAll(list, DBActionOrigin.FROM_DB);
                if (isUsedInUnify()) {
                    list = new iPartsDataFactoryDataList();
                    list.addAll(listOfUnfilteredFactoryDataInForm, DBActionOrigin.FROM_DB);
                }
            }

            // Der Retailfilter ist nicht gesetzt, wir sind nicht beim Vereinheitlichen und es sollen nur relevante
            // Daten angezeigt werden -> nicht relevante Daten ausfiltern
            if (!isRetailFilter() && !isUsedInUnify() && !isShowHistory()) {
                EtkDataAssembly assembly = partListEntry.getOwnerAssembly();
                if (assembly instanceof iPartsDataAssembly) {
                    iPartsDataFactoryDataList listWithRelevantData = new iPartsDataFactoryDataList();
                    list.forEach(factoryData -> {
                        if (FactoryDataHelper.isRelevantFactoryData((iPartsDataAssembly)assembly, factoryData,
                                                                    iPartsConst.FIELD_DFD_STATUS,
                                                                    iPartsConst.FIELD_DFD_FACTORY)) {
                            listWithRelevantData.add(factoryData, DBActionOrigin.FROM_DB);
                        }
                    });
                    list = listWithRelevantData;
                }
            }

            // Einträge ohne PEM werden nicht mehr entfernt, damit auch Löschsätze angezeigt werden. (DAIMLER-8887)
            // Fußnoten haben ebenfalls keine PEM, diese werden aufgesammelt und aus der Anzeige entfernt
            dataFootNoteList.clear();
            footNoteFactoryDataToDelete = new iPartsDataFactoryDataList();
            Iterator<iPartsDataFactoryData> iter = list.iterator();
            while (iter.hasNext()) {
                iPartsDataFactoryData factoryData = iter.next();
                String pemA = factoryData.getFieldValue(iPartsConst.FIELD_DFD_PEMA);
                String pemB = factoryData.getFieldValue(iPartsConst.FIELD_DFD_PEMB);
                DBDataObject dataObject = factoryData.getAggregateObject(iPartsDataFactoryData.AGGREGATE_ELDAS_FOOTNOTE);
                if ((dataObject instanceof iPartsDataFootNote) && (pemA.isEmpty() && pemB.isEmpty())) {
                    iPartsDataFootNote dataFootNote = (iPartsDataFootNote)dataObject;
                    dataFootNoteList.add(dataFootNote);
                    iter.remove();

                    // footNoteFactoryDataToDelete kann später zum Löschen der Fußnoten genutzt werden
                    footNoteFactoryDataToDelete.delete(factoryData, true, DBActionOrigin.FROM_EDIT);
                }
            }

            // Fußnoten wurden aus Liste der Werkseinsatzdaten entfernt
        } else {
            iPartsDialogBCTEPrimaryKey dialogPrimKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
            if (dialogPrimKey != null) {
                if (top) {
                    // Unfiltered List für das Kopieren
                    list = iPartsDataFactoryDataList.loadAfterSalesFactoryDataListForDialogPositionsVariant(getProject(), dialogPrimKey, true);
                    listOfUnfilteredFactoryDataInForm.addAll(list, DBActionOrigin.FROM_DB);
                } else {
                    // Unfiltered List für das Kopieren
                    list = iPartsDataFactoryDataList.loadConstructionFactoryDataListForDialogPositionsVariant(getProject(), dialogPrimKey, true);
                    listOfUnfilteredFactoryDataInForm.addAll(list, DBActionOrigin.FROM_DB);
                }
            }

            // Konstruktion: Wenn "alle Stände" nicht aktiviert wurde, dann soll nur der Datensatz mit dem höchsten ADAT+Sequenznummer
            // angezeigt werden (pro Werk)
            // Laut DAIMLER-5757 soll das auch für AS Daten in der konstruktiven Sicht gelten.
            if (!isShowHistory()) {
                FactoryDataHelper.filterListByNewestFactoryData(list, iPartsConst.FIELD_DFD_ADAT, iPartsConst.FIELD_DFD_SEQ_NO);
            }
        }

        //Rückmeldedaten bestimmen
        for (iPartsDataFactoryData factoryData : list) {
            String pemFromValue = factoryData.getFieldValue(getPemFromFieldName());
            List<iPartsResponseDataWithSpikes> responseData;
            if (StrUtils.isValid(pemFromValue)) {
                if (isRetailPartList) {
                    responseData = ResponseDataHelper.getResponseDataForPEM(pemFromValue, partListEntry, true, getProject(), true);
                    responseDataForPemRetailFilter.put(pemFromValue, responseData);
                }
                responseData = ResponseDataHelper.getResponseDataForPEM(pemFromValue, getProject());
                responseDataForPemUnfiltered.put(pemFromValue, responseData);
            }
            String pemToValue = factoryData.getFieldValue(getPemToFieldName());
            if (StrUtils.isValid(pemToValue)) {
                if (isRetailPartList) {
                    responseData = ResponseDataHelper.getResponseDataForPEM(pemToValue, partListEntry, true, getProject(), false);
                    responseDataForPemRetailFilter.put(pemToValue, responseData);
                }
                responseData = ResponseDataHelper.getResponseDataForPEM(pemToValue, getProject());
                responseDataForPemUnfiltered.put(pemToValue, responseData);
            }
            // Setze die Info, ob Rückmeldedaten-Änderungen vorhanden sind
            iPartsDIALOGChangeHelper.checkPartListFactoryData(getProject(), factoryData, pemFromValue, pemToValue);
        }

        // Setze die Flags für "Rückmeldedaten vorhanden"
        if (isRetailPartList) {
            if ((partListEntry instanceof iPartsDataPartListEntry)) {
                iPartsDataPartListEntry iPartsEntry = (iPartsDataPartListEntry)partListEntry;
                FactoryDataHelper.setResponseDataFlags(list, iPartsEntry, isRetailFilter(), getResponseDataMap(isRetailFilter()));
            }
        } else {
            FactoryDataHelper.setResponseDataFlags(list, null, false, getResponseDataMap(false));
        }

        addDataObjectListToGrid(top, list);
    }

    @Override
    protected void dataToGrid(boolean top) {
        super.dataToGrid(top);
        if (top) {
            updateTextAreaFootNote();
        }

        // Die Daten immer initial nach Werk sortiert anzeigen, falls die Spalte vorhanden ist
        EtkDisplayFields diplayFieldsTop = getDisplayFields(top);
        int indexOfFeld = diplayFieldsTop.getIndexOfFeld(iPartsConst.TABLE_DA_FACTORY_DATA, iPartsConst.FIELD_DFD_FACTORY, false);
        if (indexOfFeld >= 0) {
            if (top) {
                gridTop.getTable().setDefaultSortOrderAndSort(new int[]{ indexOfFeld });
            } else {
                gridBottom.getTable().setDefaultSortOrderAndSort(new int[]{ indexOfFeld });
            }
        }
    }


    /**
     * Related Info Icon wird angezeigt falls Werkseinsatzdaten mit aktivem Retail-Filter existieren.
     *
     * @param entry
     * @param retailMode
     * @param isConstruction
     * @return
     */
    public static AssemblyListCellContentFromPlugin getRelatedInfoIcon(EtkDataPartListEntry entry, boolean retailMode, boolean isConstruction) {
        if (!(entry instanceof iPartsDataPartListEntry)) {
            return null;
        }
        AssemblyListCellContentFromPlugin iconInfo = null;
        String pathName = iPartsConst.CONFIG_KEY_RELATED_INFO_FACTORY_DATA;
        iPartsDataPartListEntry iPartsDataPartListEntry = (iPartsDataPartListEntry)entry;
        iPartsFactoryData.ValidityType factoryDataValidity = iPartsDataPartListEntry.getFactoryDataValidity();
        if ((factoryDataValidity == iPartsFactoryData.ValidityType.VALID) || (factoryDataValidity == iPartsFactoryData.ValidityType.VALID_FOR_CONSTRUCTION)) {
            if (!retailMode && !isConstruction) {
                pathName = RelatedInfoSingleEditHelper.getActiveRelatedInfo(entry.getEtkProject(), pathName);
            }
            iconInfo = new AssemblyListCellContentFromPlugin(pathName,
                                                             EditDefaultImages.edit_factoryData.getImage());
            iconInfo.setHint(iPartsConst.RELATED_INFO_FACTORY_DATA_TEXT);
            iconInfo.setCursor(DWCursor.Hand);
            iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
        } else if (factoryDataValidity == iPartsFactoryData.ValidityType.VALID_NOT_ENDNUMBER_FILTER_RELEVANT) {
            if (retailMode) {
                // Dieses Icon soll nicht klickbar sein. Es dient nur der Anzeige, dass keine Endnummern-Filter relevanten
                // Werkseinsatzdaten vorhanden sind
                iconInfo = new AssemblyListCellContentFromPlugin(null, EditDefaultImages.edit_factoryData_notEndnumberFilterRelevant.getImage());
                iconInfo.setCursor(DWCursor.Unspecific);
            } else {
                pathName = RelatedInfoSingleEditHelper.getActiveRelatedInfo(entry.getEtkProject(), pathName);
                // Im Edit Mode soll das Icon klickbar sein, und auch zu den Werkseinsatzdaten führen
                iconInfo = new AssemblyListCellContentFromPlugin(pathName,
                                                                 EditDefaultImages.edit_factoryData_notEndnumberFilterRelevant.getImage());
                iconInfo.setCursor(DWCursor.Hand);
            }
            iconInfo.setHint(iPartsConst.RELATED_INFO_NOT_ENDNUMBER_FILTER_RELEVANT_FACTORY_DATA_TEXT);
            iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
        } else if (factoryDataValidity == iPartsFactoryData.ValidityType.INVALID) {
            if (retailMode) {
                // Dieses Icon soll nicht klickbar sein. Es dient nur der Anzeige, dass ungültige Werkseinsatzdaten vorhanden sind
                iconInfo = new AssemblyListCellContentFromPlugin(null, EditDefaultImages.edit_factoryData_invalid.getImage());
                iconInfo.setCursor(DWCursor.Unspecific);
            } else {
                pathName = RelatedInfoSingleEditHelper.getActiveRelatedInfo(entry.getEtkProject(), pathName);
                // Im Edit Mode soll das Icon klickbar sein, und auch zu den Werkseinsatzdaten führen
                iconInfo = new AssemblyListCellContentFromPlugin(pathName,
                                                                 EditDefaultImages.edit_factoryData_invalid.getImage());
                iconInfo.setCursor(DWCursor.Hand);
            }
            iconInfo.setHint(iPartsConst.RELATED_INFO_INVALID_FACTORY_DATA_TEXT);
            iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
        }
        return iconInfo;
    }

    /**
     * Soll die Retail- bzw. AS-Ansicht gezeigt werden?
     * - für DIALOG müssen AS- und Produktionsdaten im oberen Grid verdichtet werden
     * - das untere Grid wird ausgeblendet
     *
     * @return
     */
    @Override
    public boolean isRetailFilter() {
        return isRetailPartList && super.isRetailFilter();
    }

    @Override
    protected boolean isELDASPartList() {
        return EditModuleHelper.getDocumentationTypeFromPartListEntry(partListEntry).isTruckDocumentationType();
    }

    private boolean isSomethingLinkedInGrid() {
        List<iPartsDataFactoryData> dataInGrid = getGrid(true).getDataObjectList(iPartsDataFactoryData.class);
        for (iPartsDataFactoryData dataObject : dataInGrid) {
            if (CopyAndPasteData.isFactoryDataAlreadyLinked(dataObject)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSomethingLinkedInSelection() {
        List<List<EtkDataObject>> multiSelection = getGrid(true).getMultiSelection();
        for (List<EtkDataObject> dataObjectList : multiSelection) {
            for (EtkDataObject dataObject : dataObjectList) {
                if (CopyAndPasteData.isFactoryDataAlreadyLinked(dataObject)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        PartListEntryId currentPartListEntryId = getConnector().getRelatedInfoData().getAsPartListEntryId();
        if (forceUpdateAll || ((getConnector().getActiveRelatedSubForm() == this) && !Utils.objectEquals(currentPartListEntryId, loadedPartListEntryId))) {
            dataChanged();
        }
    }

    @Override
    public boolean hasElementsToShow() {
        return (gridTop.getTable().getRowCount() > 0) ||
               (gridBottom.getTable().getRowCount() > 0) ||
               (!dataFootNoteList.isEmpty());
    }

    @Override
    public void dataChanged() {
        super.dataChanged();

        // Festlegen was angezeigt wird
        if (isRetailFilter() || isELDASPartList() || isUsedInUnify()) {
            setGridTopTitle(null);
            setGridBottomVisible(false);
        } else {
            setGridTopTitle("!!Werkseinsatzdaten After-Sales:");
            setGridBottomVisible(true);
        }

        // Retailfilter Checkbox für DIALOG und ELDAS aber nur im Edit anzeigen
        setCheckboxRetailFilterVisible(editMode && !isUsedInUnify());
        // Wir sind nicht beim vereinheitlichen und entweder im Retail-Edit oder der DIALOG-Konstruktion
        setCheckboxShowHistoryVisible(!isUsedInUnify() && ((isRetailPartList && editMode) || (!isRetailFilter() && !isELDASPartList())));

        listOfUnfilteredFactoryDataInForm = new iPartsDataFactoryDataList();
        dataToGrid();

        // Bei ELDAS den Bereich für Fußnoten einblenden
        if (isELDASPartList() && !isUsedInUnify()) {
            setTextAreaFootNoteVisible(!dataFootNoteList.isEmpty());
            textAreaFootNoteIsVisible = !dataFootNoteList.isEmpty();
        } else {
            setTextAreaFootNoteVisible(false);
            textAreaFootNoteIsVisible = false;
        }

        enableButtonsAndMenu();
    }

    private boolean isTextAreaFootNoteVisible() {
        return textAreaFootNoteIsVisible;
    }

    private void setTextAreaFootNoteVisible(boolean visible) {
        if (visible) {
            getTopPanel().addChild(panelFootNote);
        } else {
            panelFootNote.removeFromParent();
        }
    }

    @Override
    protected void checkboxShowHistoryClicked(Event event) {
        if (isRetailPartList) {
            if (isShowHistory()) {
                setRetailFilter(false);
            }
        } else {
            listOfUnfilteredFactoryDataInForm = new iPartsDataFactoryDataList();
        }
        dataToGrid();
        iPartsRelatedInfoSuperEditDataForm.refreshSuperEdit(this);
    }

    @Override
    protected void checkboxRetailFilterClicked(Event event) {
        if (isRetailPartList && isRetailFilterSet()) {
            setShowHistory(false);
        }
        //Bei Eldas is der Retail Filter immer aktiv, aber Button zu Bearbeitung sollen aktiv bleiben
        if (!isELDASPartList()) {
            setReadOnly(isRetailFilter() || !(editMode && isEditContext(getConnector(), true)) || (isPSKAssembly() && isPartListEntryWithValidBCTEKey()));
        }
        gridTop.setDisplayFields(null); // Damit die angezeigten Felder neu bestimmt werden
        enableButtonsAndMenu();
        updateView();
        iPartsRelatedInfoSuperEditDataForm.refreshSuperEdit(this);
    }

    @Override
    protected String getFactoryDataTableName() {
        return iPartsConst.TABLE_DA_FACTORY_DATA;
    }

    @Override
    protected String getFactoryFieldName() {
        return iPartsConst.FIELD_DFD_FACTORY;
    }

    @Override
    protected String getPemFromFieldName() {
        return iPartsConst.FIELD_DFD_PEMA;
    }

    @Override
    protected String getPemToFieldName() {
        return iPartsConst.FIELD_DFD_PEMB;
    }

    @Override
    protected String getSeriesNoFieldName() {
        return iPartsConst.FIELD_DFD_SERIES_NO;
    }

    @Override
    protected String getAAFieldName() {
        return iPartsConst.FIELD_DFD_AA;
    }

    @Override
    protected boolean isEditable() {
        return editMode && !isReadOnly;
    }

    @Override
    protected void statusChangedForGrid(boolean top) {
        // Selektion muss VOR doSaveDataObjects() ausgelesen werden, weil diese durch das Update ansonsten verlorengeht
        EtkDataObjectList<iPartsDataFactoryData> multiSelection = getMultiSelection(top, iPartsDataFactoryData.class);
        statusChangedForDataObjects(multiSelection, top);
    }

    private void statusChangedForDataObject(iPartsDataFactoryData factoryData, boolean top) {
        EtkDataObjectList<iPartsDataFactoryData> list = new iPartsDataFactoryDataList();
        if (isUsedInUnify()) {
            // da in statusChangedForDataObjects() kein Speichern erfolgt
            factoryData.getAttributes().setLoaded(true);
        }
        list.add(factoryData, DBActionOrigin.FROM_EDIT);
        statusChangedForDataObjects(list, top);
    }

    private void statusChangedForDataObjects(EtkDataObjectList<iPartsDataFactoryData> factoryDataList, boolean top) {
        List<iPartsDataFactoryData> allFactoryDataObjectsOfGrid = getDataObjectList(top, iPartsDataFactoryData.class);
        List<iPartsDataFactoryData> factoryDataForStatusAutomation;
        EtkDataObjectList<iPartsDataFactoryData> changedDataObjectsWithoutFactory = null;
        if (isELDASPartList()) {
            factoryDataForStatusAutomation = new DwList<>();
            changedDataObjectsWithoutFactory = new GenericEtkDataObjectList<>();

            // Prüfen, ob in den neuen Daten ein Datensatz mit Quelle iParts ist
            boolean containsIpartsData = false;
            for (iPartsDataFactoryData factoryData : factoryDataList) {
                iPartsImportDataOrigin source = factoryData.getSource();
                if (source == iPartsImportDataOrigin.IPARTS) {
                    containsIpartsData = true;
                    break;
                }
            }

            // Wenn es einen neuen iParts Datensatz gibt, dann sollen alle migrierten Daten auf nicht relevant gesetzt werden
            if (containsIpartsData) {
                for (iPartsDataFactoryData factoryData : allFactoryDataObjectsOfGrid) {
                    if (factoryData.getAsId().getFactory().isEmpty() && // migrierte Daten haben kein Werk
                        (factoryData.getSource() == iPartsImportDataOrigin.ELDAS)) {
                        // Status auf "nicht relevant" setzen
                        factoryData.setFieldValue(getStatusFieldName(), iPartsDataReleaseState.NOT_RELEVANT.getDbValue(),
                                                  DBActionOrigin.FROM_EDIT);
                        changedDataObjectsWithoutFactory.add(factoryData, DBActionOrigin.FROM_EDIT);
                    } else {
                        factoryDataForStatusAutomation.add(factoryData);
                    }
                }
            }
        } else {
            factoryDataForStatusAutomation = allFactoryDataObjectsOfGrid;
        }

        if (!isUsedInUnify()) {
            EtkDataObjectList<iPartsDataFactoryData> changedDataObjects = iPartsRelatedEditHelper.updateStatusValuesFactoryData(factoryDataList,
                                                                                                                                factoryDataForStatusAutomation,
                                                                                                                                iPartsConst.FIELD_DFD_ADAT,
                                                                                                                                getSeqNoFieldName(),
                                                                                                                                getStatusFieldName());

            if (changedDataObjectsWithoutFactory != null) {
                changedDataObjects.addAll(changedDataObjectsWithoutFactory, DBActionOrigin.FROM_EDIT);
            }

            if (saveDataObjectsWithUpdate(changedDataObjects)) {
                iPartsRelatedEditHelper.statusChanged(changedDataObjects, this, false);
            }
        } else {
            iPartsDataFactoryDataList validDatas = new iPartsDataFactoryDataList();
            factoryDataList.forEach((data) -> {
                data.setFieldValue(iPartsConst.FIELD_DFD_ADAT, "", DBActionOrigin.FROM_DB);
                data.updateIdFromPrimaryKeys();
                data.updateOldId();

                if (!listOfUnfilteredFactoryDataInForm.containsId(data.getAsId())) {
                    validDatas.add(data, DBActionOrigin.FROM_DB);
                }
            });

            factoryDataListFromCopy = validDatas;
            dataToGrid(top);
        }
    }

    @Override
    public List<iPartsDataDIALOGChange> getDataDIALOGChanges(EtkDataObject dataObject) {
        List<iPartsDataDIALOGChange> result = new ArrayList<>(1);
        if (isRelatedInfoEditContext() && (dataObject instanceof iPartsDataFactoryData)) {
            iPartsDataDIALOGChange factoryDataDIALOGChanges = FactoryDataHelper.getFactoryDataDIALOGChanges(getProject(), dataObject, getSeriesNoFieldName());
            if (factoryDataDIALOGChanges != null) {
                result.add(factoryDataDIALOGChanges);
            }
        }
        return result;
    }

    private void createFootnotePanel() {
        panelFootNote = new de.docware.framework.modules.gui.controls.GuiPanel();
        panelFootNote.setName("panelFootNote");
        panelFootNote.__internal_setGenerationDpi(96);
        panelFootNote.setScaleForResolution(true);
        panelFootNote.setMinimumWidth(10);
        panelFootNote.setMinimumHeight(10);
        panelFootNote.setLayout(new LayoutBorder());
        panelFootNote.setMaximumHeight(300);

        labelFootnote = new de.docware.framework.modules.gui.controls.GuiLabel();
        labelFootnote.setName("labelFootnote");
        labelFootnote.__internal_setGenerationDpi(96);
        labelFootnote.setScaleForResolution(true);
        labelFootnote.setMinimumWidth(10);
        labelFootnote.setMinimumHeight(10);
        labelFootnote.setConstraints(new ConstraintsBorder(POSITION_NORTH));
        labelFootnote.setText("!!Nicht interpretierte Einsatzfußnote:");
        panelFootNote.addChild(labelFootnote);

        GuiPanel panelText = new de.docware.framework.modules.gui.controls.GuiPanel();
        panelText.setName("panelText");
        panelText.__internal_setGenerationDpi(96);
        panelText.setScaleForResolution(true);
        panelText.setMinimumWidth(10);
        panelText.setMinimumHeight(10);
        panelText.setLayout(new LayoutBorder());
        panelText.setConstraints(new ConstraintsBorder(POSITION_CENTER));
        panelText.setMaximumHeight(300);
        panelFootNote.addChild(panelText);

        footNoteToolbar = new GuiToolbar();
        footNoteToolbar.setName("footNoteToolbar");
        footNoteToolbar.__internal_setGenerationDpi(96);
        footNoteToolbar.setScaleForResolution(true);
        footNoteToolbar.setMinimumWidth(10);
        footNoteToolbar.setMinimumHeight(0);
        footNoteToolbar.setConstraints(new ConstraintsBorder(POSITION_NORTH));
        panelText.addChild(footNoteToolbar);

        GuiScrollPane scrollPane = new GuiScrollPane();
        scrollPane.setName("scrollpane");
        scrollPane.__internal_setGenerationDpi(96);
        scrollPane.setScaleForResolution(true);
        scrollPane.setMinimumWidth(10);
        scrollPane.setMinimumHeight(10);
        scrollPane.setConstraints(new ConstraintsBorder(POSITION_CENTER));
        panelText.addChild(scrollPane);

        textareaFootnote = new de.docware.framework.modules.gui.controls.GuiTextArea();
        textareaFootnote.setName("textareaFootnote");
        textareaFootnote.__internal_setGenerationDpi(96);
        textareaFootnote.setScaleForResolution(true);
        textareaFootnote.setMinimumWidth(200);
        textareaFootnote.setEditable(false);
        textareaFootnote.setLineWrap(true);

        textareaFootnote.setConstraints(new ConstraintsBorder());
        scrollPane.addChild(textareaFootnote);

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_SOUTH);
        panelFootNote.setConstraints(constraints);

    }

    private void updateTextAreaFootNote() {
        StringBuilder sb = new StringBuilder();
        for (iPartsDataFootNote dataFootNote : dataFootNoteList) {
            DBDataObjectList<iPartsDataFootNoteContent> contentList = dataFootNote.getFootNoteList();
            for (iPartsDataFootNoteContent dataFootNoteContent : contentList) {
                String contentText = dataFootNoteContent.getText(getProject().getDBLanguage(), getProject().getDataBaseFallbackLanguages());
                if (!contentText.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(contentText);
                }
            }
        }
        textareaFootnote.setText(sb.toString());
    }
}