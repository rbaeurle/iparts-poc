/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.mainview.forms.AbstractMechanicForm;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAuthorOrderView;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiInternalTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.config.db.datatypes.DatatypeUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Formular für die Anzeige der internen Texte zu einem Stücklisteneintrag
 */
public class iPartsRelatedInfoInternalTextForPartDataForm extends AbstractRelatedInfoPartlistDataForm {

    public static final String IPARTS_MENU_ITEM_SHOW_INTERNAL_TEXT_FOR_PART_DATA = "iPartsMenuItemShowInternalTextForPartData";
    public static final String IPARTS_MENU_ITEM_SHOW_PARTLIST_HISTORY = "iPartsMenuItemShowPartListHistory";
    public static final String INTERNAL_TEXT_FOR_PART_DATA_NAME = "/InternalTextForPart";
    public static final String CONFIG_KEY_INTERNAL_TEXT_FOR_PART_DATA = "Plugin/iPartsEdit" + INTERNAL_TEXT_FOR_PART_DATA_NAME;
    private static final boolean DEFAULT_SORT_ASCENDING = false;
    private static final int MAX_TEXT_LENGTH = 100;

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.SA_TU,
                                                                                   iPartsModuleTypes.DialogRetail,
                                                                                   iPartsModuleTypes.EDSRetail,
                                                                                   iPartsModuleTypes.PSK_PKW,
                                                                                   iPartsModuleTypes.PSK_TRUCK,
                                                                                   iPartsModuleTypes.CAR_PERSPECTIVE,
                                                                                   iPartsModuleTypes.Dialog_SM_Construction);
    // todo Auskommentieren, wenn Interner Text in EDS erlaubt ist
    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES_FOR_CONST = EnumSet.of(iPartsModuleTypes.Dialog_SM_Construction/*,
                                                                                             iPartsModuleTypes.EDS_SAA_Construction*/);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        String menuItemText = "!!Internen Text anzeigen";
        if (isEditContext(connector, true) || iPartsRight.EDIT_INTERNAL_TEXT_CONSTRUCTION.checkRightInSession()) {
            menuItemText = "!!Internen Text bearbeiten";
        }
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_INTERNAL_TEXT_FOR_PART_DATA, menuItemText,
                                EditDefaultImages.edit_btn_internal_text.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_INTERNAL_TEXT_FOR_PART_DATA);
        GuiMenuItem menuItem = modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_PARTLIST_HISTORY, "!!Änderungshistorie für Stücklisteneintrag anzeigen",
                                                       EditDefaultImages.edit_history.getImage(), null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                if (connector.getSelectedPartListEntries() != null) {
                    EtkDataPartListEntry selectedPartListEntry = connector.getSelectedPartListEntries().get(0);
                    EditAuthorOrderView.showSMPartListHistoryView(connector, connector.getActiveForm(),
                                                                  connector.getCurrentAssembly(), selectedPartListEntry);
                }
            }
        });

    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        if (isEditContext(connector, false)) {
            updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_INTERNAL_TEXT_FOR_PART_DATA, false);
            updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_PARTLIST_HISTORY, false);
        } else {
            boolean isVisible = false;
            if ((connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
                isVisible = relatedInfoIsVisible(connector.getCurrentAssembly(), VALID_MODULE_TYPES_FOR_CONST);
            }
            updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_INTERNAL_TEXT_FOR_PART_DATA, isVisible);
            updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_PARTLIST_HISTORY, isVisible);
        }
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry partListEntry, RelatedInfoBaseFormIConnector connector) {
        if (isEditContext(connector, false) || ((partListEntry instanceof iPartsDataPartListEntry) && ((iPartsDataPartListEntry)partListEntry).hasInternalText())) {
            return relatedInfoIsVisible(partListEntry.getOwnerAssembly(), VALID_MODULE_TYPES);
        } else {
            return relatedInfoIsVisible(partListEntry.getOwnerAssembly(), VALID_MODULE_TYPES_FOR_CONST);
        }
    }

    /**
     * Related Info Icon wird angezeigt falls interne Texte für die AS-Sicht oder die Konstruktionsstückliste existieren
     *
     * @param entry
     * @param isConstruction
     * @return
     */
    public static AssemblyListCellContentFromPlugin getRelatedInfoIcon(EtkDataPartListEntry entry, boolean isConstruction) {
        if (isConstruction) {
            if (entry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)entry;
                if (partListEntry.hasInternalText()) {
                    AssemblyListCellContentFromPlugin iconInfo = new AssemblyListCellContentFromPlugin(INTERNAL_TEXT_FOR_PART_DATA_NAME,
                                                                                                       EditToolbarButtonAlias.EDIT_INTERNAL_TEXT.getImage());
                    iconInfo.setHint(TranslationHandler.translate("!!Interner Text vorhanden"));
                    iconInfo.setCursor(DWCursor.Hand);
                    iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
                    iconInfo.setCellContent(EtkConfigConst.IMGPREFIX + EditToolbarButtonAlias.EDIT_INTERNAL_TEXT.getAlias());
                    return iconInfo;
                }
            }
        }
        return null;
    }

    private String currentUser;
    private EditDataObjectGrid dataGrid;
    private boolean isEditRelatedInfo;
    private boolean isInConstruction;
    private boolean isEditableInConstruction;
    private boolean isEDSConstructionRelatedInfo;
    private boolean isDeleteAllowed;

    /**
     * Erzeugt eine Instanz von iPartsRelatedInfoInternalTextForPartDataForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    protected iPartsRelatedInfoInternalTextForPartDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {

        // Den angemeldeten Benutzer für die Vergleiche zum Aktivieren der Buttons nur einmalig ermitteln.
        currentUser = iPartsUserAdminDb.getLoginUserName();

        isEditRelatedInfo = (getConnector().getActiveForm() instanceof EditModuleForm) &&
                            AbstractRelatedInfoPartlistDataForm.isEditContext(getConnector(), true);
        isDeleteAllowed = iPartsRight.EDIT_DELETE_INTERNAL_TEXT.checkRightInSession();
        if (!isEditRelatedInfo) {
            AbstractJavaViewerForm activeForm = getConnector().getActiveForm();
            if (activeForm instanceof AbstractMechanicForm) {
                AssemblyId assemblyId = getConnector().getRelatedInfoData().getAsPartListEntryId().getOwnerAssemblyId();
                if (iPartsVirtualNode.isVirtualId(assemblyId)) {
                    List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
                    if (iPartsVirtualNode.isConstructionRelevant(virtualNodes)) {
                        isInConstruction = true;
                        isEditableInConstruction = iPartsRight.EDIT_INTERNAL_TEXT_CONSTRUCTION.checkRightInSession();
                    }
                }
            } else {
                // TU readonly geöffnet
                isDeleteAllowed = false;
            }
        }

        dataGrid = new EditDataObjectGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                doEnableButtons();
            }

            // Doppelklick in eine Zeile, die dem angemeldeten Benutzer gehört, startet die Edit-Funktion.
            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doEditInternalText(null);
            }

            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (tableName.equals(iPartsConst.TABLE_DA_INTERNAL_TEXT)) {
                    if (fieldName.equals(iPartsConst.FIELD_DIT_ATTACHMENT)) {
                        byte[] attachment = objectForTable.getFieldValueAsBlob(iPartsConst.FIELD_DIT_ATTACHMENT);
                        boolean hasAttachments = (attachment != null) && (attachment.length > 0);
                        if (hasAttachments) {
                            return DatatypeUtils.makeImgTag(DatatypeUtils.getImageForValue(4, true), true);
                        }
                        return "";
                    } else if (fieldName.equals(iPartsConst.FIELD_DIT_TEXT)) {
                        if (objectForTable != null) {
                            return getVisObject().asHtml(tableName, fieldName,
                                                         StrUtils.makeAbbreviation(objectForTable.getAttributeForVisObject(fieldName).getAsString(), MAX_TEXT_LENGTH),
                                                         getProject().getDBLanguage()).getStringResult();
                        }
                        return "";
                    }
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }

            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
                boolean isEditable = isEditAllowed();
                ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doCreateInternalText(event);
                    }
                });
                getContextMenuTable().addChild(holder.menuItem);
                if (!isEditable) {
                    getToolbarHelper().hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, getContextMenuTable());
                }

                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doEditInternalText(event);
                    }
                });
                getContextMenuTable().addChild(holder.menuItem);
                if (!isEditable) {
                    holder.menuItem.setText("!!Anzeigen");
                    holder.toolbarButton.setTooltip(TranslationHandler.translate("!!Anzeigen"));
                }

                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doDeleteInternalText(event);
                    }
                });
                getContextMenuTable().addChild(holder.menuItem);
                if (!isEditable && !isDeleteAllowed) {
                    getToolbarHelper().hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, getContextMenuTable());
                }
            }
        };

        dataGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        dataGrid.getGui().setConstraints(constraints);
        panelMain.panelTable.addChild(dataGrid.getGui());
        doEnableButtons();

        // Doppelte Überschrift unterdrücken
        panelMain.labelTableTitel.setVisible(false);
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().getActiveRelatedSubForm() == this) {
            dataGrid.setDisplayFields(null);
            fillDataGrid(false);
        }
    }

    /**
     * Sucht sich aus den DisplayFields die Zeitstempelspalte (zum späteren Sortieren).
     *
     * @return
     */
    private int getDefaultSortColumn() {
        // Die Daten immer initial nach Änderungsdatum sortiert anzeigen, falls die Spalte vorhanden ist
        EtkDisplayFields displayFields = dataGrid.getDisplayFields();
        return displayFields.getIndexOfFeld(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_CHANGE_DATE, false);
    }

    /**
     * Füllt das Grid mit DisplayFields, Daten, Sortierung.
     *
     * @param withUpdatePartListEntry
     */
    private void fillDataGrid(boolean withUpdatePartListEntry) {
        // Die Sortierung merken
        int sortColumn = dataGrid.getTable().getSortColumn();
        boolean isAscending = dataGrid.getTable().isSortAscending();

        // Wenn die DisplayFields noch nicht gesetzt sind, ...
        if (dataGrid.getDisplayFields() == null) {
            // ...  das Grid neu aufbauen ...
            EtkDisplayFields displayFields = getDisplayFields(CONFIG_KEY_INTERNAL_TEXT_FOR_PART_DATA);
            dataGrid.setDisplayFields(displayFields);
            // ... die Default-Sortierung festlegen ...
            sortColumn = getDefaultSortColumn();
            isAscending = false;
            // ... und die Daten aus der Datenbank laden.
        }

        // Sonderfall: leeres Grid!
        if (sortColumn < 0) {
            sortColumn = getDefaultSortColumn();
            isAscending = DEFAULT_SORT_ASCENDING;
        }

        List<iPartsDataInternalText> internalTextList = loadFromDB();
        if (withUpdatePartListEntry) {
            EtkDataPartListEntry etkPartListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
            if (etkPartListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)etkPartListEntry;
                String currentInternalText = null;
                if (!internalTextList.isEmpty()) {
                    currentInternalText = internalTextList.get(0).getText();
                }
                partListEntry.setCurrentInternalText(currentInternalText);

                // Löschen vom virtuellen Feld mit __INTERNAL_FROM_EDIT_DB_DATA_OBJECT, damit der Stücklisteneintrag als
                // geändert markiert wird
                partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_INTERNAL_TEXT, false,
                                                          DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            }
        }
        fillGrid(internalTextList, sortColumn, isAscending);
        doEnableButtons();
    }

    private AssemblyId getCurrentAssemblyId() {
        return getConnector().getRelatedInfoData().getAsPartListEntryId().getOwnerAssemblyId();
    }

    private void fillGrid(List<iPartsDataInternalText> internalTextList, int sortColumn, boolean isAscending) {

        // Nun das Grid leeren und neu befüllen
        dataGrid.clearGrid();
        for (iPartsDataInternalText dataInternalText : internalTextList) {
            dataGrid.addObjectToGrid(dataInternalText);
        }
        dataGrid.showNoResultsLabel(internalTextList.isEmpty());

        // Sortierung wiederherstellen
        if (sortColumn >= 0) {
            dataGrid.getTable().sortRowsAccordingToColumn(sortColumn, isAscending);
        }
    }

    /**
     * Holt die Daten direkt aus der Datenbank.
     *
     * @return
     */
    private List<iPartsDataInternalText> loadFromDB() {
        EtkDataPartListEntry partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        if (isInConstruction) {
            EtkDataAssembly assembly = partListEntry.getOwnerAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                if (iPartsAssembly.isDialogSMConstructionAssembly()) {
                    // DIALOG-Konstruktion
                    String dialogGUID = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
                    iPartsDialogId dialogId = new iPartsDialogId(dialogGUID);
                    return iPartsDataInternalTextList.getAllInternalTextForPartListEntry(getProject(), dialogId).getAsList();
                }
            }
            return new DwList<>();
        } else {
            return iPartsDataInternalTextList.getAllInternalTextForPartListEntry(getProject(), partListEntry.getAsId()).getAsList();
        }
    }

    /**
     * (De-)aktiviert Steuerelemente der Oberfläche in Abhängigkeit der gewählten Einträge,
     * des angemeldeten Benutzers und des Autorenauftrags.
     */
    private void doEnableButtons() {
        boolean isEditAllowed = isEditAllowed();

        // Der Neu-Button ist immer aktiv, wenn ein Autorenauftrag aktiv ist.
        dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, isEditAllowed);

        // Einmalig die selektierte Liste holen und mehrfach darüber iterieren
        List<List<EtkDataObject>> selectedInternalTexts = dataGrid.getMultiSelection();

        // (De-)Aktivieren des Bearbeiten-Buttons.
        // Den Bearbeiten-Button nur aktivieren, wenn genau eine Zeile des aktuellen Benutzers ausgewählt ist.
        boolean editInternalTextAllowed = false;
        if (selectedInternalTexts.size() == 1) {
            if (isEditAllowed) {
                editInternalTextAllowed = true;
                checkEditLoop:
                for (List<EtkDataObject> dataObjectList : selectedInternalTexts) {
                    for (EtkDataObject oneDataObject : dataObjectList) {
                        if (oneDataObject instanceof iPartsDataInternalText) {
                            if (!((iPartsDataInternalText)oneDataObject).getUserId().equals(currentUser)) {
                                editInternalTextAllowed = false;
                                break checkEditLoop;
                            }
                        }
                    }
                }
            }
        }
        // Bei aktivem Auftrag Auftrag den eigenen Text bearbeiten, sonst nur Anzeigen
        dataGrid.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, editInternalTextAllowed
                                                                                     ? EditToolbarButtonAlias.EDIT_WORK.getTooltip()
                                                                                     : "!!Anzeigen");

        dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, selectedInternalTexts.size() == 1);

        // (De-)Aktivieren des Lösch-Buttons.
        boolean setDeleteButtonEnabled = false;
        if (isEditAllowed || isDeleteAllowed) {
            if (!selectedInternalTexts.isEmpty()) {
                setDeleteButtonEnabled = true;
                if (!isDeleteAllowed) {
                    checkDeleteLoop:
                    for (List<EtkDataObject> dataObjectList : selectedInternalTexts) {
                        for (EtkDataObject oneDataObject : dataObjectList) {
                            if (oneDataObject instanceof iPartsDataInternalText) {
                                if (!((iPartsDataInternalText)oneDataObject).getUserId().equals(currentUser)) {
                                    setDeleteButtonEnabled = false;
                                    break checkDeleteLoop;
                                }
                            }
                        }
                    }
                }
            }
        }
        // Lösch-Button nur auf enabled setzen, wenn ein Autorenauftrag aktiv ist.
        dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, setDeleteButtonEnabled);
    }

    /**
     * Checkt den Konfig-Key und gibt ggf. die Default-Display-Fields zurück.
     *
     * @param configKey
     * @return
     */
    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        if (configKey.equals(CONFIG_KEY_INTERNAL_TEXT_FOR_PART_DATA)) {
            return createInternalTextTableDataDefaultDisplayFields();
        }
        return null;
    }

    /**
     * Gibt die Default-Display-Fields zurück.
     *
     * @return
     */
    private List<EtkDisplayField> createInternalTextTableDataDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();

        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_U_ID, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_CHANGE_DATE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_TEXT, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_INTERNAL_TEXT, iPartsConst.FIELD_DIT_ATTACHMENT, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    /**
     * Neu Anlegen eines {@link iPartsDataInternalText} internen Text-Objektes über das ChangeSet.
     *
     * @param event
     */
    private void doCreateInternalText(Event event) {
        iPartsDataInternalText dataInternalText = iPartsGuiInternalTextField.createInternalText(this, getConnector().getRelatedInfoData().getAsPartListEntry(getProject()));
        if (dataInternalText != null) {
            saveDataObjectWithUpdate(dataInternalText);
        }
    }

    /**
     * Editieren {@link iPartsDataInternalText} internen Text-Objektes.
     * Per Definition kann nur (!)EINE(!) Zeile selektiert sein!
     *
     * @param event
     */
    private void doEditInternalText(Event event) {
        boolean isEdit = isEditAllowed();
        // Den gewählten Eintrag editieren, es kann nur einen geben!
        List<EtkDataObject> selectedInternalText = dataGrid.getSelection();
        for (EtkDataObject oneDataObject : selectedInternalText) {
            if (oneDataObject instanceof iPartsDataInternalText) {
                iPartsDataInternalText selectedDataInternalText = (iPartsDataInternalText)oneDataObject;
                iPartsDataInternalText dataInternalText = iPartsGuiInternalTextField.editShowInternalText(this, selectedDataInternalText,
                                                                                                          isEdit && selectedDataInternalText.getUserId().equals(currentUser));
                if (dataInternalText != null) {
                    saveDataObjectWithUpdate(dataInternalText);
                }
                break;
            }
        }
    }

    /**
     * Löschen {@link iPartsDataInternalText} interner Text-Objektes über das ChangeSet.
     * Es kann mehrere selektierte Zeilen geben.
     *
     * @param event
     */
    private void doDeleteInternalText(Event event) {
        // Die gewählten Einträge löschen, bzw. in die delete-Liste hängen.
        iPartsDataInternalTextList deletedDataInternalTexts = new iPartsDataInternalTextList();
        List<List<EtkDataObject>> selectedInternalTexts = dataGrid.getMultiSelection();
        for (List<EtkDataObject> dataObjectList : selectedInternalTexts) {
            for (EtkDataObject oneDataObject : dataObjectList) {
                if (oneDataObject instanceof iPartsDataInternalText) {
                    deletedDataInternalTexts.delete((iPartsDataInternalText)oneDataObject, true, DBActionOrigin.FROM_EDIT);
                }
            }
        }

        saveDataObjectsWithUpdate(deletedDataInternalTexts);
    }

    @Override
    protected boolean saveDataObjectWithUpdate(EtkDataObject dataObject) {
        if (isEditRelatedInfo) {
            return super.saveDataObjectWithUpdate(dataObject);
        }

        // Konstruktions-Stückliste
        GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();
        modifiedDataObjects.add(dataObject, DBActionOrigin.FROM_EDIT);
        return saveDataObjectsWithUpdate(modifiedDataObjects);
    }

    @Override
    protected boolean saveDataObjectsWithUpdate(EtkDataObjectList<? extends EtkDataObject> dataObjectList) {
        if (isEditRelatedInfo) {
            return super.saveDataObjectsWithUpdate(dataObjectList);
        }

        // Konstruktions-Stückliste: geänderte Werte an alle selektierten PartListEntries übergeben und direkt in der DB speichern
        final GenericEtkDataObjectList genericList = new GenericEtkDataObjectList();
        genericList.addAll(dataObjectList, DBActionOrigin.FROM_EDIT);

        final EtkDbObjectsLayer dbLayer = getProject().getDbLayer();

        dbLayer.startTransaction();
        dbLayer.startBatchStatement();
        final VarParam<Boolean> result = new VarParam<>(true);
        // direkt in der Tabelle DA_INTERNAL_TEXT ohne aktive ChangeSets speichern
        getProject().executeWithoutActiveChangeSets(new Runnable() {
            @Override
            public void run() {
                try {
                    genericList.saveToDB(getProject());
                    dbLayer.endBatchStatement();
                    dbLayer.commit();

                    // Im Connector hinterlassen, dass die Zeile mit dem selektierten PartListEntry beim Schließen
                    // der RelatedInfo neu aufgebaut wird
                    getConnector().setIconOrPartListEntryChangedDuringRelatedInfoAction(true);
                } catch (Exception e) {
                    dbLayer.cancelBatchStatement();
                    dbLayer.rollback();
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    MessageDialog.showError("!!Fehler beim Speichern.");
                    result.setValue(false);
                }
            }
        }, false); // fireDataChangedEvent ist hier nicht notwendig, weil der Update auf anderem Wege erfolgt

        if (result.getValue()) {
            // Alle Cluster-Knoten benachrichtigen
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                      iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                      getCurrentAssemblyId(), false));
            fillDataGrid(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return panelMain;
    }

    public boolean isEditAllowed() {
        return isEditRelatedInfo || isEditableInConstruction;
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        panelMain = new PanelMainClass(translationHandler);
        panelMain.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected PanelMainClass panelMain;

    private class PanelMainClass extends de.docware.framework.modules.gui.controls.GuiPanel {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelTableTitel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTable;

        private PanelMainClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(panelMainLayout);
            labelTableTitel = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelTableTitel.setName("labelTableTitel");
            labelTableTitel.__internal_setGenerationDpi(96);
            labelTableTitel.registerTranslationHandler(translationHandler);
            labelTableTitel.setScaleForResolution(true);
            labelTableTitel.setMinimumWidth(10);
            labelTableTitel.setMinimumHeight(10);
            labelTableTitel.setPaddingTop(4);
            labelTableTitel.setPaddingLeft(8);
            labelTableTitel.setPaddingRight(8);
            labelTableTitel.setPaddingBottom(4);
            labelTableTitel.setText("!!Interne Texte");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelTableTitelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelTableTitelConstraints.setPosition("north");
            labelTableTitel.setConstraints(labelTableTitelConstraints);
            this.addChild(labelTableTitel);
            panelTable = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTable.setName("panelTable");
            panelTable.__internal_setGenerationDpi(96);
            panelTable.registerTranslationHandler(translationHandler);
            panelTable.setScaleForResolution(true);
            panelTable.setMinimumWidth(10);
            panelTable.setMinimumHeight(10);
            panelTable.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelTableLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelTable.setLayout(panelTableLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelTableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelTable.setConstraints(panelTableConstraints);
            this.addChild(panelTable);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}