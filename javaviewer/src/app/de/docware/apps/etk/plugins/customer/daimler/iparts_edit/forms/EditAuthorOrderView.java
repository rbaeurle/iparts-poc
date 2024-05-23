/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsChangeSetInfoDefinitions;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsFactoryDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsCombTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsIncludePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleEinPASId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.ChangeSetShowTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.serialization.*;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class EditAuthorOrderView extends AbstractJavaViewerForm implements iPartsConst {

    protected static final String IPARTS_MENU_ITEM_SHOW_MODULE_HISTORY = "iPartsMenuItemShowModuleHistory";
    public static final String IPARTS_MENU_ITEM_SHOW_MODULE_HISTORY_TEXT = "!!Änderungshistorie für TU anzeigen";
    protected static final String IPARTS_MENU_ITEM_SHOW_PARTLISTENTRY_HISTORY = "iPartsMenuItemShowPartListEntryHistory";
    protected static final String IPARTS_MENU_ITEM_SHOW_PARTLISTENTRY_HISTORY_TEXT = "!!Änderungshistorie für Teileposition anzeigen";
    protected static final String IPARTS_MENU_ITEM_SHOW_SM_HISTORY = "iPartsMenuItemShowSMHistory";
    public static final String IPARTS_MENU_ITEM_SHOW_SM_HISTORY_TEXT = "!!Änderungshistorie für Submodul anzeigen";

    private static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = iPartsModuleTypes.EDITABLE_MODULE_TYPES;
    private static final EnumSet<iPartsModuleTypes> VALID_SM_TYPES = EnumSet.of(iPartsModuleTypes.Dialog_SM_Construction);
    private static final Set<String> UNDOABLE_DATA_TYPES = new HashSet<>();
    private static final Set<String> AO_HISTORY_FOR_TU_DATA_TYPES = new HashSet<>();

    static {
        UNDOABLE_DATA_TYPES.add(PartListEntryId.TYPE);
        UNDOABLE_DATA_TYPES.add(iPartsFactoryDataId.TYPE);
        UNDOABLE_DATA_TYPES.add(AssemblyId.TYPE);
        UNDOABLE_DATA_TYPES.add(iPartsModuleId.TYPE);
        UNDOABLE_DATA_TYPES.add(iPartsModuleEinPASId.TYPE);

        AO_HISTORY_FOR_TU_DATA_TYPES.add(PartListEntryId.TYPE);
        AO_HISTORY_FOR_TU_DATA_TYPES.add(iPartsReplacePartId.TYPE);
        AO_HISTORY_FOR_TU_DATA_TYPES.add(iPartsIncludePartId.TYPE);
        AO_HISTORY_FOR_TU_DATA_TYPES.add(iPartsDataInternalTextId.TYPE);
        AO_HISTORY_FOR_TU_DATA_TYPES.add(iPartsFootNoteCatalogueRefId.TYPE);
        AO_HISTORY_FOR_TU_DATA_TYPES.add(iPartsCombTextId.TYPE);
        AO_HISTORY_FOR_TU_DATA_TYPES.add(AssemblyId.TYPE);
        AO_HISTORY_FOR_TU_DATA_TYPES.add(iPartsModuleEinPASId.TYPE);
        AO_HISTORY_FOR_TU_DATA_TYPES.add(iPartsPicOrderModulesId.TYPE);
    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        GuiMenuItem menuItemShowTUHistory = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_MODULE_HISTORY,
                                                                                                    IPARTS_MENU_ITEM_SHOW_MODULE_HISTORY_TEXT, null);
        if (menuItemShowTUHistory != null) {
            menuItemShowTUHistory.setIcon(EditDefaultImages.edit_history.getImage());
            menuItemShowTUHistory.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    if (formWithTree instanceof AbstractAssemblyTreeForm) {
                        EditAuthorOrderView.showModuleHistoryView(formWithTree.getConnector(), formWithTree,
                                                                  ((AbstractAssemblyTreeForm)formWithTree).getCurrentAssembly());
                    }
                }
            });
        }

        GuiMenuItem menuItemShowSMHistory = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_SM_HISTORY,
                                                                                                    IPARTS_MENU_ITEM_SHOW_SM_HISTORY_TEXT, null);
        if (menuItemShowSMHistory != null) {
            menuItemShowSMHistory.setIcon(EditDefaultImages.edit_history.getImage());
            menuItemShowSMHistory.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    if (formWithTree instanceof AbstractAssemblyTreeForm) {
                        EditAuthorOrderView.showSMHistoryView(formWithTree.getConnector(), formWithTree,
                                                              ((AbstractAssemblyTreeForm)formWithTree).getCurrentAssembly());
                    }
                }
            });
        }
    }

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        // Menüeintrag "Änderungshistorie für TU anzeigen" hinzufügen
        GuiMenuItem menuItemShowTUHistory = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector,
                                                                                                        IPARTS_MENU_ITEM_SHOW_MODULE_HISTORY,
                                                                                                        IPARTS_MENU_ITEM_SHOW_MODULE_HISTORY_TEXT,
                                                                                                        EditDefaultImages.edit_history.getImage(),
                                                                                                        null);
        menuItemShowTUHistory.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
                if ((selectedPartListEntries != null) && selectedPartListEntries.size() == 1) {
                    EtkDataPartListEntry selectedPartListEntry = selectedPartListEntries.get(0);
                    AssemblyId destAssemblyId = selectedPartListEntry.getDestinationAssemblyId();
                    if (destAssemblyId.isValidId()) {
                        EtkDataAssembly destAssembly = EtkDataObjectFactory.createDataAssembly(connector.getProject(), destAssemblyId);
                        destAssembly = destAssembly.getLastHiddenSingleSubAssemblyOrThis(null);
                        EditAuthorOrderView.showModuleHistoryView(connector, connector.getActiveForm(), destAssembly);
                    }
                }
            }
        });

        // Menüeintrag "Änderungshistorie für Teileposition anzeigen" hinzufügen
        GuiMenuItem menuItemShowPLEHistory = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector,
                                                                                                         IPARTS_MENU_ITEM_SHOW_PARTLISTENTRY_HISTORY,
                                                                                                         IPARTS_MENU_ITEM_SHOW_PARTLISTENTRY_HISTORY_TEXT,
                                                                                                         EditDefaultImages.edit_history.getImage(),
                                                                                                         null);
        menuItemShowPLEHistory.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
                if ((selectedPartListEntries != null) && selectedPartListEntries.size() == 1) {
                    EtkDataPartListEntry selectedPartListEntry = selectedPartListEntries.get(0);
                    EditAuthorOrderView.showPartListHistoryView(connector, connector.getActiveForm(), selectedPartListEntry);
                }
            }
        });
        popupMenu.addChild(menuItemShowPLEHistory);
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        AbstractRelatedInfoPartlistDataForm.updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_MODULE_HISTORY,
                                                                VALID_MODULE_TYPES);
        AbstractRelatedInfoPartlistDataForm.updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_SM_HISTORY,
                                                                VALID_SM_TYPES);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        boolean isValidAssemblyForShowTUHistory = false;
        boolean isValidAssemblyForShowPLEHistory = false;
        boolean isMenuShowPLEHistoryEnabled = true;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            // TU-Historie sichtbar für alle Retail-TUs des selektierten Stücklisteneintrags
            EtkDataPartListEntry selectedPartListEntry = connector.getSelectedPartListEntries().get(0);
            AssemblyId destAssemblyId = selectedPartListEntry.getDestinationAssemblyId();
            if (destAssemblyId.isValidId()) {
                EtkDataAssembly destAssembly = EtkDataObjectFactory.createDataAssembly(connector.getProject(), destAssemblyId);
                destAssembly = destAssembly.getLastHiddenSingleSubAssemblyOrThis(null);
                isValidAssemblyForShowTUHistory = !destAssembly.isVirtual();
            }

            EtkDataAssembly assembly = connector.getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                // Stücklisteneintrags-Historie sichtbar für alle Retail- und Edit-TUs
                if (!iPartsAssembly.getAsId().isVirtual()) {
                    isValidAssemblyForShowPLEHistory = true;
                    // benötigt zuviel Zeit
                    // hier wäre ein LRU-Cache angebracht um Enabled zu aktualisieren
//                    PartListEntryId partListEntryId = connector.getSelectedPartListEntries().get(0).getAsId();
//                    partListEntryId = new PartListEntryId(partListEntryId.getKVari(), partListEntryId.getKVer(), "*");
//                    isMenuShowPLEHistoryEnabled = iPartsDataChangeSetEntryList.hasDataObjectIdWithTypeAndChangeSetStatus(connector.getProject(),
//                                                                                                                              partListEntryId,
//                                                                                                                              iPartsChangeSetStatus.COMMITTED);
                }
            }
        }

        // Sichtbarkeit vom Menüeintrag "Änderungshistorie für TU anzeigen" aktualisieren
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_MODULE_HISTORY, isValidAssemblyForShowTUHistory);

        // Sichtbarkeit vom Menüeintrag "Änderungshistorie für Teilepositionen anzeigen" aktualisieren
        GuiMenuItem menuItemShowPLEHistory = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_PARTLISTENTRY_HISTORY,
                                                                                                         isValidAssemblyForShowPLEHistory);
        if (menuItemShowPLEHistory != null) {
            menuItemShowPLEHistory.setEnabled(isMenuShowPLEHistoryEnabled);
        }
    }

    public static void showModuleHistoryView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             EtkDataAssembly assembly) {
        EditAuthorOrderView dlg = new EditAuthorOrderView(dataConnector, parentForm, assembly);
        dlg.setTitleBy(assembly);
        dlg.showModal();
    }

    public static void showPartListHistoryView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               PartListEntryId partListEntryId) {
        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(dataConnector.getProject(), partListEntryId);
        if (!partListEntry.existsInDB()) {
            partListEntry.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
        }
        showPartListHistoryView(dataConnector, parentForm, partListEntry);
    }

    public static void showPartListHistoryView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               EtkDataPartListEntry partListEntry) {
        EditAuthorOrderView dlg = new EditAuthorOrderView(dataConnector, parentForm, partListEntry);
        dlg.setTitleBy(partListEntry);
        dlg.showModal();
    }

    public static void showTechChangeSetHistoryView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                    List<iPartsChangeSetId> techChangeSetGUIDList) {
        EditAuthorOrderView dlg = new EditAuthorOrderView(dataConnector, techChangeSetGUIDList, parentForm);
        dlg.setTitleByTechnicalChangeSets(techChangeSetGUIDList);
        dlg.showModal();
    }

    public static void showAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           iPartsDataAuthorOrder authorOrder) {
        List<iPartsDataAuthorOrder> authorOrderList = new DwList<>();
        authorOrderList.add(authorOrder);
        showAuthorOrderView(dataConnector, parentForm, authorOrderList);
    }

    public static void showAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           List<iPartsDataAuthorOrder> authorOrderList) {
        EditAuthorOrderView dlg = new EditAuthorOrderView(dataConnector, parentForm, authorOrderList);
        dlg.setTitleBy(authorOrderList);
        dlg.showModal();
    }

    public static void showAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           iPartsDataAuthorOrder authorOrder, AssemblyId assemblyId) {
        List<iPartsDataAuthorOrder> authorOrderList = new DwList<>();
        authorOrderList.add(authorOrder);
        EditAuthorOrderView dlg = new EditAuthorOrderView(dataConnector, parentForm, authorOrderList);
        dlg.setTitleBy(authorOrderList);
        dlg.setObjectTypeFilter(assemblyId);
        dlg.showModal();
    }

    public static void showSMHistoryView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                         EtkDataAssembly assembly) {
        HmMSmId hmMSmId = iPartsVirtualNode.getHmMSmIdForAssemblyId(assembly.getAsId());
        if (hmMSmId != null) {
            EditAuthorOrderView dlg = new EditAuthorOrderView(dataConnector, parentForm, hmMSmId);
            dlg.setTitle(dlg.showTypeHelper.getTitleForSM(assembly));
            dlg.showModal();
        }
    }

    public static void showSMPartListHistoryView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 EtkDataAssembly assembly, EtkDataPartListEntry selectedPartListEntry) {
        iPartsDialogBCTEPrimaryKey primaryBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(selectedPartListEntry);
        if (primaryBCTEKey != null) {
            EditAuthorOrderView dlg = new EditAuthorOrderView(dataConnector, parentForm, primaryBCTEKey);
            dlg.setTitle(dlg.showTypeHelper.getTitleForSMPartListEntry(dataConnector.getProject(), assembly, selectedPartListEntry));
            dlg.showModal();
        }
    }

    public static final String DUMMY_TABLE_NAME = "MYTABLE";

    public enum OBJECT_COLS {
        OBJECT_TYPE("!!Objekttyp"),
        OBJECT_ID("!!Objekt-ID"),
        RELEASE_DATE("!!Änderungsdatum"),
        COMMIT_DATE("!!Freigabedatum"),
        AUTHOR_ORDER("!!Autoren-Auftrag"),
        CURRENT_USER("!!Bearbeiter"),
        CHANGE_TYPE("!!Änderungsart"),
        AUTHOR_GUID(""),
        CHANGESET_GUID("");

        private String description;

        OBJECT_COLS(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String getFieldName() {
            return name();
        }

        public static OBJECT_COLS getColByDisplayField(EtkDisplayField displayField) {
            String fieldName = displayField.getKey().getFieldName();
            for (OBJECT_COLS objCol : OBJECT_COLS.values()) {
                if (objCol.getFieldName().equals(fieldName)) {
                    return objCol;
                }
            }
            return null;
        }
    }

    protected static EnumSet<OBJECT_COLS> hasColumnFilter = EnumSet.of(OBJECT_COLS.OBJECT_TYPE, OBJECT_COLS.OBJECT_ID, OBJECT_COLS.AUTHOR_ORDER,
                                                                       OBJECT_COLS.CURRENT_USER, OBJECT_COLS.CHANGE_TYPE);
    protected static EnumSet<OBJECT_COLS> hiddenColumns = EnumSet.of(OBJECT_COLS.COMMIT_DATE, OBJECT_COLS.AUTHOR_GUID,
                                                                     OBJECT_COLS.CHANGESET_GUID);

    private enum EXTENDED_OBJECT_COLS {
        OBJECT_TYPE("!!Objekttyp"),
        OBJECT_ID("!!Objekt-ID"),
        RELEASE_DATE("!!Änderungsdatum"),
        AUTHOR_ORDER("!!Autoren-Auftrag"),
        CURRENT_USER("!!Bearbeiter"),
        CHANGE_TYPE("!!Änderungsart"),
        CHANGE_CONTENT("!!Änderungsinhalt"),
        CHANGE_OLD_VALUE("!!alt"),
        CHANGE_NEW_VALUE("!!neu"),
        AUTHOR_GUID(""),
        CHANGESET_GUID("");

        private String description;

        EXTENDED_OBJECT_COLS(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String getFieldName() {
            return name();
        }

        public static EXTENDED_OBJECT_COLS getExtendedColByDisplayField(EtkDisplayField displayField) {
            String fieldName = displayField.getKey().getFieldName();
            for (EXTENDED_OBJECT_COLS objCol : EXTENDED_OBJECT_COLS.values()) {
                if (objCol.getFieldName().equals(fieldName)) {
                    return objCol;
                }
            }
            return null;
        }
    }

    private enum ViewType {AUTHOR_ORDER, PARTIAL_UNDO, ASSEMBLY, PARTS_LIST_ENTRY, TECH_CHANGESET, DIALOG_SUBMODULE, DIALOG_PART_LIST_ENTRY}

    protected static EnumSet<EXTENDED_OBJECT_COLS> basicExtendedCols = EnumSet.of(EXTENDED_OBJECT_COLS.OBJECT_TYPE, EXTENDED_OBJECT_COLS.OBJECT_ID,
                                                                                  EXTENDED_OBJECT_COLS.RELEASE_DATE, EXTENDED_OBJECT_COLS.AUTHOR_ORDER,
                                                                                  EXTENDED_OBJECT_COLS.CURRENT_USER, /*EXTENDED_OBJECT_COLS.CHANGE_TYPE,*/
                                                                                  EXTENDED_OBJECT_COLS.AUTHOR_GUID, EXTENDED_OBJECT_COLS.CHANGESET_GUID);

    protected static EnumSet<EXTENDED_OBJECT_COLS> extraExtendedCols = EnumSet.of(EXTENDED_OBJECT_COLS.CHANGE_TYPE, EXTENDED_OBJECT_COLS.CHANGE_CONTENT,
                                                                                  EXTENDED_OBJECT_COLS.CHANGE_OLD_VALUE,
                                                                                  EXTENDED_OBJECT_COLS.CHANGE_NEW_VALUE/*,
                                                                                EXTENDED_OBJECT_COLS.RELEASE_DATE, EXTENDED_OBJECT_COLS.AUTHOR_ORDER,
                                                                                EXTENDED_OBJECT_COLS.CURRENT_USER*/);

    protected static EnumSet<EXTENDED_OBJECT_COLS> hasExtendedColumnFilter = EnumSet.of(EXTENDED_OBJECT_COLS.OBJECT_TYPE, EXTENDED_OBJECT_COLS.AUTHOR_ORDER,
                                                                                        EXTENDED_OBJECT_COLS.CURRENT_USER, EXTENDED_OBJECT_COLS.CHANGE_TYPE);
    protected static EnumSet<EXTENDED_OBJECT_COLS> hiddenExtendedColumns = EnumSet.of(EXTENDED_OBJECT_COLS.AUTHOR_GUID,
                                                                                      EXTENDED_OBJECT_COLS.CHANGESET_GUID);


    private ChangeSetHistoryGridForm grid;
    private ChangeSetHistoryGridForm extendedGrid;
    private String tableName;
    private Map<iPartsAuthorOrderId, iPartsDataAuthorOrder> authorOrderMap;
    private Map<iPartsAuthorOrderId, iPartsDataChangeSetList> changeSetListMap;
    private Map<iPartsChangeSetId, iPartsRevisionChangeSet> changeSetMap;
    private Map<iPartsChangeSetId, Collection<SerializedDBDataObjectHistory>> changeSetHistoryMap;
    private boolean useAlternateLayout = false;
    private AssemblyId viewingAssemblyId;
    protected ChangeSetShowTypes showTypeHelper;
    private ViewType viewType;

    protected List<String> basicExtendedFieldNames;
    protected List<String> extraExtendedFieldNames;

    /**
     * Erzeugt eine Instanz von EditAuthorOrderView.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */

    protected EditAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.tableName = DUMMY_TABLE_NAME;
        this.authorOrderMap = new HashMap<>();
        this.changeSetListMap = new HashMap<>();
        this.changeSetMap = new HashMap<>();
        this.changeSetHistoryMap = new HashMap<>();
        this.showTypeHelper = new ChangeSetShowTypes(getProject());
        mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE).requestFocus();
    }

    public EditAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                               List<iPartsDataAuthorOrder> authorOrderList) {
        this(dataConnector, parentForm);
        viewType = ViewType.AUTHOR_ORDER;
        fillAuthorOrderMap(authorOrderList);
        fillExtendedLists();
        postCreateGui();

        fillDataGrid();
        doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.MAXIMIZE);
    }

    public EditAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                               iPartsAuthorOrderId authorOrderId, Collection<SerializedDBDataObject> serializedDBDataObjects) {
        this(dataConnector, parentForm);
        viewType = ViewType.PARTIAL_UNDO;
        mainWindow.title.setTitle("Änderungen für die folgenden Einträge zurücknehmen?");
        mainWindow.buttonpanel.setDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
        authorOrderMap.put(authorOrderId, new iPartsDataAuthorOrder(getProject(), authorOrderId));
        fillExtendedLists();
        postCreateGui();
        mainWindow.checkboxHistory.setVisible(false);
        mainWindow.checkboxLayoutChange.setVisible(false);

        fillAttributes(authorOrderId, serializedDBDataObjects);
        sortGrid();
        showNoResultLabelGrid();
        showNoResultLabelExtendedGrid();
        doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.SCALE_FROM_PARENT);
    }

    public EditAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                               EtkDataAssembly assembly) {
        this(dataConnector, parentForm);
        viewType = ViewType.ASSEMBLY;
        fillExtendedLists();

        postCreateGui();
        mainWindow.checkboxHistory.setVisible(false);
        mainWindow.checkboxLayoutChange.setVisible(false);

        fillGridForAssembly(assembly);
        doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.MAXIMIZE);
    }

    public EditAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                               EtkDataPartListEntry partListEntry) {
        this(dataConnector, parentForm);
        viewType = ViewType.PARTS_LIST_ENTRY;
        fillExtendedLists();

        postCreateGui();
        mainWindow.checkboxHistory.setVisible(false);
        mainWindow.checkboxLayoutChange.setVisible(false);

        fillGridForPartListEntry(partListEntry);
        doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.MAXIMIZE);
    }

    /**
     * Konstruktor für die Anzeige von mehreren Technischen ChangeSets
     * Aufrufparameter wurden gedreht, damit er sich vom Konstruktor
     * EditAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
     * List<iPartsDataAuthorOrder> authorOrderList)
     * unterscheidet
     *
     * @param dataConnector
     * @param techChangeSetGUIDList
     * @param parentForm
     */
    public EditAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, List<iPartsChangeSetId> techChangeSetGUIDList,
                               AbstractJavaViewerForm parentForm) {
        this(dataConnector, parentForm);
        this.viewType = ViewType.TECH_CHANGESET;
        fillChangeSetMap(techChangeSetGUIDList);
        fillExtendedLists();
        showTypeHelper.setExtendedValidTypes();

        postCreateGui();
        mainWindow.checkboxHistory.setVisible(false);
        mainWindow.checkboxLayoutChange.setVisible(false);

        fillDataGrid();
        doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.MAXIMIZE);
    }

    public EditAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                               HmMSmId hmMSmId) {
        this(dataConnector, parentForm);
        viewType = ViewType.DIALOG_SUBMODULE;
        fillExtendedLists();

        postCreateGui();
        mainWindow.checkboxHistory.setVisible(false);
        mainWindow.checkboxLayoutChange.setVisible(false);

        fillGridForHmMSMId(hmMSmId);
        doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.MAXIMIZE);
    }

    public EditAuthorOrderView(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                               iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
        this(dataConnector, parentForm);
        viewType = ViewType.DIALOG_PART_LIST_ENTRY;
        fillExtendedLists();

        postCreateGui();
        mainWindow.checkboxHistory.setVisible(false);
        mainWindow.checkboxLayoutChange.setVisible(false);

        fillGridForBcteKey(primaryBCTEKey);
        doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.MAXIMIZE);
    }

    /**
     * Umbau der {@code techChangeSetGUIDList} in Pseudo-{@link iPartsDataAuthorOrder}-Objekte.
     * Damit funktioniert die Verwaltung und Anzeige von mehreren ChangeSets (analog zu Autoren-Aufträgen)
     *
     * @param techChangeSetGUIDList
     */
    private void fillChangeSetMap(List<iPartsChangeSetId> techChangeSetGUIDList) {
        if (techChangeSetGUIDList != null) {
            EtkProject project = getProject();
            for (iPartsChangeSetId tcsId : techChangeSetGUIDList) {
                iPartsAuthorOrderId id = new iPartsAuthorOrderId(tcsId.getGUID());
                iPartsDataAuthorOrder authorOrder = new iPartsDataAuthorOrder(project, id);
                authorOrder.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                authorOrder.setFieldValue(iPartsConst.FIELD_DAO_CHANGE_SET_ID, tcsId.getGUID(), DBActionOrigin.FROM_DB);
                this.authorOrderMap.put(authorOrder.getAsId(), authorOrder);
            }
        }
    }

    private void fillAuthorOrderMap(List<iPartsDataAuthorOrder> authorOrderList) {
        if (authorOrderList != null) {
            for (iPartsDataAuthorOrder authorOrder : authorOrderList) {
                if (!authorOrder.isChangeSetIdEmpty()) {
                    this.authorOrderMap.put(authorOrder.getAsId(), authorOrder);
                }
            }
        }
    }

    private void fillExtendedLists() {
        basicExtendedFieldNames = new DwList<>();
        for (EXTENDED_OBJECT_COLS objCol : basicExtendedCols) {
            basicExtendedFieldNames.add(objCol.getFieldName());
        }
        extraExtendedFieldNames = new DwList<>();
        for (EXTENDED_OBJECT_COLS objCol : extraExtendedCols) {
            extraExtendedFieldNames.add(objCol.getFieldName());
        }
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        grid = createGrid(true);
        List<OBJECT_COLS> hiddenColumnsToSet = new DwList<>(hiddenColumns);
        List<OBJECT_COLS> hasColumnFilterToSet = new DwList<>(hasColumnFilter);
        if (viewType != ViewType.AUTHOR_ORDER) {
            if ((viewType != ViewType.PARTIAL_UNDO) && (viewType != ViewType.DIALOG_SUBMODULE) && (viewType != ViewType.DIALOG_PART_LIST_ENTRY)) {
                hiddenColumnsToSet.remove(OBJECT_COLS.COMMIT_DATE);
                hiddenColumnsToSet.add(OBJECT_COLS.RELEASE_DATE);
            }
            if (viewType == ViewType.TECH_CHANGESET) {
                hiddenColumnsToSet.add(OBJECT_COLS.AUTHOR_ORDER);
            }
        }
        grid.setDisplayResultFields(getDefaultDisplayResultFields(hasColumnFilterToSet, hiddenColumnsToSet));
        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        grid.getGui().setConstraints(constraints);
        mainWindow.splitpane_firstChild.addChild(grid.getGui());
        grid.getTable().addEventListener(new EventListener(Event.TABLE_SELECTION_EVENT) {
            public void fire(Event event) {
                onSelectionChanged(event);
            }
        });

        extendedGrid = createGrid(false);
        List<EXTENDED_OBJECT_COLS> hiddenExtendedColumnsToSet = new DwList<>(hiddenExtendedColumns);
        List<EXTENDED_OBJECT_COLS> hasExtendedColumnFilterToSet = new DwList<>(hasExtendedColumnFilter);
        if (viewType == ViewType.TECH_CHANGESET) {
            hiddenExtendedColumnsToSet.add(EXTENDED_OBJECT_COLS.AUTHOR_ORDER);
        }
        extendedGrid.setDisplayResultFields(getDefaultExtendedDisplayResultFields(hasExtendedColumnFilterToSet, hiddenExtendedColumnsToSet));
        constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        extendedGrid.getGui().setConstraints(constraints);
        mainWindow.panelExtendedGrid.addChild(extendedGrid.getGui());

        if (J2EEHandler.isJ2EE()) {
            mainWindow.dockingpanel.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.dockingpanel) {
                @Override
                public boolean isFireOnceValid(Event event) {
                    return mainWindow.dockingpanel.isSplitPaneSizeValid();
                }

                @Override
                public void fireOnce(Event event) {
                    mainWindow.dockingpanel.setShowing(false);
                }
            });
        } else {
            mainWindow.dockingpanel.addEventListener(new EventListenerFireOnce(Event.OPENED_EVENT, mainWindow.dockingpanel) {
                @Override
                public void fireOnce(Event event) {
                    mainWindow.dockingpanel.setShowing(false);
                }
            });
        }
    }

    private ChangeSetHistoryGridForm createGrid(boolean top) {
        List<String> specialHeaderFilterNames = new DwList<>();
        if (top) {
            for (OBJECT_COLS cols : hasColumnFilter) {
                specialHeaderFilterNames.add(cols.getFieldName());
            }
        } else {
            for (EXTENDED_OBJECT_COLS cols : hasExtendedColumnFilter) {
                specialHeaderFilterNames.add(cols.getFieldName());
            }
        }
        ChangeSetHistoryGridForm resultGrid = new ChangeSetHistoryGridForm(getConnector(), this, tableName, specialHeaderFilterNames) {
            @Override
            protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
                // Selektives Undo nur im oberen Grid für Autoren-Aufträge anzeigen
                if (top && (viewType == ViewType.AUTHOR_ORDER) && (getRevisionsHelper().getActiveRevisionChangeSetForEdit() != null)) {
                    GuiMenuItem partialUndoMenuItem = toolbarHelper.createContextMenuEntry(EditToolbarButtonAlias.EDIT_AO_PARTIAL_UNDO, getUITranslationHandler(),
                                                                                           new EventListener(Event.MENU_ITEM_EVENT) {
                                                                                               @Override
                                                                                               public void fire(Event event) {
                                                                                                   doPartialUndo();
                                                                                               }
                                                                                           });
                    contextMenu.addChild(partialUndoMenuItem);
                }

                if (top && (viewType == ViewType.ASSEMBLY)) { // Aktuell explizit nur für ViewType.ASSEMBLY
                    GuiMenuItem showHistoryForTUMenuItem = toolbarHelper.createContextMenuEntry(EditToolbarButtonAlias.EDIT_AO_COMPLETE_HISTORY_FOR_TU, getUITranslationHandler(),
                                                                                           new EventListener(Event.MENU_ITEM_EVENT) {
                                                                                               @Override
                                                                                               public void fire(Event event) {
                                                                                                   doShowAuthorOrderHistoryForTU();
                                                                                               }
                                                                                           });
                    contextMenu.addChild(showHistoryForTUMenuItem);
                }

                super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);
            }
        };
        Map<String, String> exchangeFieldsForDisplay = new HashMap<>();
        if (top) {
            OnDblClickEvent onDblClickEvent = () -> showDetails(true);
            resultGrid.setOnDblClickEvent(onDblClickEvent);
            // Datum richtig formatieren
            exchangeFieldsForDisplay.put(TableAndFieldName.make(tableName, OBJECT_COLS.RELEASE_DATE.getFieldName()), TableAndFieldName.make(TABLE_DA_MODEL, FIELD_DM_DATA));
            exchangeFieldsForDisplay.put(TableAndFieldName.make(tableName, OBJECT_COLS.COMMIT_DATE.getFieldName()), TableAndFieldName.make(TABLE_DA_MODEL, FIELD_DM_DATA));
        } else {
            // Datum richtig formatieren
            exchangeFieldsForDisplay.put(TableAndFieldName.make(tableName, EXTENDED_OBJECT_COLS.RELEASE_DATE.getFieldName()), TableAndFieldName.make(TABLE_DA_MODEL, FIELD_DM_DATA));
        }
        resultGrid.setExchangeFieldsForDisplay(exchangeFieldsForDisplay);
        return resultGrid;
    }

    private AssemblyId getAssemblyIdForAuthorOrderView() {
        if (viewType == ViewType.ASSEMBLY) {
            return viewingAssemblyId;
        } else {
            SerializedDBDataObject serializedDataObject = getSelectedElement();
            if (serializedDataObject != null) {
                return showTypeHelper.calculateAssemblyIdFromObjectId(serializedDataObject);
            }
        }
        return null;
    }

    private void doShowAuthorOrderHistoryForTU() {
        iPartsAuthorOrderId authorOrderId = getSelectedAuthorId();
        if (authorOrderId.isValidId()) {
            AssemblyId assemblyId = getAssemblyIdForAuthorOrderView();
            iPartsDataAuthorOrder authorOrder = new iPartsDataAuthorOrder(getProject(), authorOrderId);
            if (authorOrder.existsInDB()) {
                // Die assemblyId wird erst beim Setzen des Spaltenfilters getestet => zumindest die "Gesamte Historie" ist offen
                showAuthorOrderView(getConnector(), this, authorOrder, assemblyId);
            }
        }
    }

    private void clearGrid() {
        grid.clearGrid();
        grid.clearEntries();
    }

    private void sortGrid() {
        String fieldName = OBJECT_COLS.RELEASE_DATE.getFieldName();
        if ((viewType != ViewType.AUTHOR_ORDER) && (viewType != ViewType.PARTIAL_UNDO) && (viewType != ViewType.DIALOG_SUBMODULE) &&
            (viewType != ViewType.DIALOG_PART_LIST_ENTRY)) {
            fieldName = OBJECT_COLS.COMMIT_DATE.getFieldName();
        }
        sortTableByCol(grid, fieldName);
    }

    private void showNoResultLabelGrid() {
        grid.showNoResultsLabel(grid.getTable().getRowCount() == 0, false);
    }

    private void addAttributesToGrid(DBDataObjectAttributes attributes) {
        grid.addAttributesToGrid(attributes);
    }

    private void clearExtendedGrid() {
        extendedGrid.clearGrid();
        extendedGrid.clearEntries();
    }

    private void sortExtendedGrid() {
        sortTableByCol(extendedGrid, EXTENDED_OBJECT_COLS.RELEASE_DATE.getFieldName());
    }

    private void showNoResultLabelExtendedGrid() {
        extendedGrid.showNoResultsLabel(extendedGrid.getTable().getRowCount() == 0, false);
    }

    private void addAttributesToExtendedGrid(DBDataObjectAttributes attributes) {
        extendedGrid.addAttributesToGrid(attributes);
    }

    private void onSelectionChanged(Event event) {
        boolean isUndoEnabled = false;
        boolean isShowAuthorOrderTuHistoryEnabled = false;
        if (grid.getSelection() != null) {
            SerializedDBDataObject serializedDataObject = getSelectedElement();
            if (serializedDataObject != null) {
                iPartsAuthorOrderId authorOrderId = getSelectedAuthorId();
                if (!useAlternateLayout) {
                    fillExtendedAttributes(authorOrderId, serializedDataObject);
                } else {
                    if (mainWindow.checkboxHistory.isSelected()) {
                        IdWithType selectedId = serializedDataObject.createId();
                        fillExtendedAlternateHistoricAttributes(authorOrderId, selectedId);
                    } else {
                        clearExtendedGrid();
                        fillExtendedAttributes(authorOrderId, serializedDataObject);
                        sortExtendedGrid();
                    }
                }
                for (SerializedDBDataObject serializedObject : getSelectedElements()) {
                    String type = serializedObject.getType();
                    if (UNDOABLE_DATA_TYPES.contains(type)) {
                        // Änderungen an modul-bezogenen Datentypen können nur rückgängig gemacht werden, wenn der Zustand
                        // verändert ist
                        if ((type.equals(AssemblyId.TYPE) || type.equals(iPartsModuleId.TYPE) || type.equals(iPartsModuleEinPASId.TYPE))
                            && (serializedObject.getState() != SerializedDBDataObjectState.MODIFIED)) {
                            continue;
                        }
                        isUndoEnabled = true;
                        break;
                    }
                }

                if (getSelectedElements().size() == 1) {
                    if (viewType == ViewType.ASSEMBLY) {
                        isShowAuthorOrderTuHistoryEnabled = true;
                    } else {
                        // gültigen Eintrag überprüfen via AO_HISTORY_FOR_TU_DATA_TYPES
                        serializedDataObject = getSelectedElement();
                        if (serializedDataObject != null) {
                            isShowAuthorOrderTuHistoryEnabled = AO_HISTORY_FOR_TU_DATA_TYPES.contains(serializedDataObject.getType());
                        }
                    }
                }
            }
        } else {
            clearExtendedGrid();
        }
        grid.toolbarHelper.enableMenu(EditToolbarButtonAlias.EDIT_AO_PARTIAL_UNDO, grid.getTable().getContextMenu(),
                                      isUndoEnabled);
        grid.toolbarHelper.enableMenu(EditToolbarButtonAlias.EDIT_AO_COMPLETE_HISTORY_FOR_TU, grid.getTable().getContextMenu(),
                                      isShowAuthorOrderTuHistoryEnabled);

    }

    protected EtkDisplayFields getDefaultDisplayResultFields(List<OBJECT_COLS> hasColumnFilter, List<OBJECT_COLS> hiddenColumns) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        if ((viewType == ViewType.AUTHOR_ORDER) || (viewType == ViewType.PARTIAL_UNDO)) {
            hiddenColumns.add(OBJECT_COLS.COMMIT_DATE);
        }
        for (OBJECT_COLS objCol : OBJECT_COLS.values()) {
            if ((viewType == ViewType.TECH_CHANGESET) && (objCol == OBJECT_COLS.AUTHOR_ORDER)) {
                continue;
            }
            EtkDisplayField displayField = MasterDataProductForm.addDisplayField(tableName, objCol.getFieldName(), false, false, null, getProject(), displayFields);
            EtkMultiSprache multi = new EtkMultiSprache();
            multi.setText(getProject().getViewerLanguage(), TranslationHandler.translate(objCol.getDescription()));
            displayField.setText(multi);
            displayField.setDefaultText(false);
            displayField.setColumnFilterEnabled(hasColumnFilter.contains(objCol));
            if (hiddenColumns.contains(objCol)) {
                displayField.setVisible(false);
            }
        }
        return displayFields;
    }

    protected EtkDisplayFields getDefaultExtendedDisplayResultFields(List<EXTENDED_OBJECT_COLS> hasColumnFilter, List<EXTENDED_OBJECT_COLS> hiddenColumns) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        for (EXTENDED_OBJECT_COLS objCol : EXTENDED_OBJECT_COLS.values()) {
            EtkDisplayField displayField = MasterDataProductForm.addDisplayField(tableName, objCol.getFieldName(), false, false, null, getProject(), displayFields);
            EtkMultiSprache multi = new EtkMultiSprache();
            multi.setText(getProject().getViewerLanguage(), objCol.getDescription());
            displayField.setText(multi);
            displayField.setDefaultText(false);
            displayField.setColumnFilterEnabled(hasColumnFilter.contains(objCol));
            if (hiddenColumns.contains(objCol)) {
                displayField.setVisible(false);
            }
        }
        return displayFields;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        close();
        return modalResult;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setTitleBy(List<iPartsDataAuthorOrder> authorOrderList) {
        setTitle(showTypeHelper.getTitleBy(authorOrderList));
    }

    public void setTitleBy(EtkDataAssembly assembly) {
        setTitle(showTypeHelper.getTitleBy(assembly));
    }

    public void setTitleBy(EtkDataPartListEntry partListEntry) {
        setTitle(showTypeHelper.getTitleBy(partListEntry));
    }

    public void setTitleByTechnicalChangeSets(List<iPartsChangeSetId> techChangeSetGUIDList) {
        setTitle(showTypeHelper.getTitleByTechnicalChangeSets(techChangeSetGUIDList));
    }

    public void doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES kind) {
        switch (kind) {
            case MAXIMIZE:
                Dimension screenSize = FrameworkUtils.getScreenSize();
                mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
                break;
            case SCALE_FROM_PARENT:
                if (parentForm != null) {
                    int height = parentForm.getGui().getParentWindow().getHeight();
                    int width = parentForm.getGui().getParentWindow().getWidth();
                    mainWindow.setSize(width - CASCADING_WINDOW_OFFSET_WIDTH, height - CASCADING_WINDOW_OFFSET_HEIGHT);
                }
                break;
        }
    }

    /**
     * Setzen des Spaltenfilters bei OBJECT_ID
     *
     * @param assemblyId
     */
    protected void setObjectTypeFilter(AssemblyId assemblyId) {
        if ((assemblyId == null) || !assemblyId.isValidId()) {
            return;
        }
        ChangeSetHistoryGridForm.AuthorOrderDataSearchFilterFactory sff = null;
        if (grid.getTable().getColumnFilterFactory() instanceof ChangeSetHistoryGridForm.AuthorOrderDataSearchFilterFactory) {
            sff = (ChangeSetHistoryGridForm.AuthorOrderDataSearchFilterFactory)grid.getTable().getColumnFilterFactory();
        }
        if (sff == null) {
            return;
        }
        String filterValue = assemblyId.getKVari();
        // zur Sicherheit
        grid.getTable().clearAllFilterValues();
        int column = grid.getColByFieldName(OBJECT_COLS.OBJECT_ID.getFieldName());
        if (column >= 0) {
            AbstractGuiControl filterControl = sff.restoreFilterEditControl(column, filterValue);
            grid.getTable().setFilterValueForColumnFromControl(column, filterControl, true, true);
        }
    }

    private void doPartialUndo() {
        List<SerializedDBDataObject> selectedSerializedDBDataObjects = getSelectedElements();
        AbstractRevisionChangeSet activeRevisionChangeSetForEdit = getRevisionsHelper().getActiveRevisionChangeSetForEdit();
        if (activeRevisionChangeSetForEdit instanceof iPartsRevisionChangeSet) {
            iPartsRevisionChangeSet activeIPartsRevisionChangeSetForEdit = (iPartsRevisionChangeSet)activeRevisionChangeSetForEdit;
            Collection<SerializedDBDataObject> relevantSerializedDBDataObjects = activeIPartsRevisionChangeSetForEdit.calculateRelevantSerializedDBDataObjects(selectedSerializedDBDataObjects,
                                                                                                                                                               UNDOABLE_DATA_TYPES);
            if (!Utils.isValid(relevantSerializedDBDataObjects)) {
                MessageDialog.showWarning("!!Keine relevanten Daten für das Zurücknehmen von Änderungen gefunden.",
                                          EditToolbarButtonAlias.EDIT_AO_PARTIAL_UNDO.getTooltip());
            } else {
                EditAuthorOrderView partialUndoConfirmDialog = new EditAuthorOrderView(getConnector(), this, getSelectedAuthorId(),
                                                                                       relevantSerializedDBDataObjects);
                if (partialUndoConfirmDialog.showModal() == ModalResult.OK) {
                    // ChangeSetEntries der relevanten SerializedDBDataObjects löschen
                    iPartsChangeSetId changeSetId = activeIPartsRevisionChangeSetForEdit.getChangeSetId();
                    GenericEtkDataObjectList toBeDeletedDataObjectsList = new GenericEtkDataObjectList();
                    EtkProject project = getProject();
                    for (SerializedDBDataObject relevantSerializedDBDataObject : relevantSerializedDBDataObjects) {
                        IdWithType relevantDBDataObjectId = relevantSerializedDBDataObject.createId();
                        iPartsChangeSetEntryId relevantChangeSetEntryId = new iPartsChangeSetEntryId(changeSetId, relevantDBDataObjectId);
                        toBeDeletedDataObjectsList.delete(new iPartsDataChangeSetEntry(project, relevantChangeSetEntryId),
                                                          true, DBActionOrigin.FROM_EDIT);

                        // Primärschlüsselreservierung löschen bei neuen SerializedDBDataObjects
                        if (relevantSerializedDBDataObject.getState() == SerializedDBDataObjectState.NEW) {
                            iPartsReservedPKId relevantReservePKId = new iPartsReservedPKId(relevantDBDataObjectId);
                            toBeDeletedDataObjectsList.delete(new iPartsDataReservedPK(project, relevantReservePKId),
                                                              true, DBActionOrigin.FROM_EDIT);
                        }
                    }
                    toBeDeletedDataObjectsList.saveToDB(project);

                    // Updates der Caches und der GUI durchführen
                    activeIPartsRevisionChangeSetForEdit.loadFromDB();
                    EtkDataAssembly.removeCacheForActiveChangeSets(project);
                    changeSetListMap.clear();
                    changeSetMap.clear();
                    changeSetHistoryMap.clear();
                    updateDataGrid();
                    project.fireProjectEvent(new DataChangedEvent(null), true);
                    iPartsEditPlugin.reloadAllModulesInEdit(getConnector());
                }
            }
        }
    }

    private void fillAttributes(iPartsAuthorOrderId authorOrderId, iPartsDataChangeSetList changeSetList) {
        for (iPartsDataChangeSet dataChangeSet : changeSetList) {
            iPartsRevisionChangeSet changeSet = getChangeSet(dataChangeSet.getAsId());
            fillAttributes(authorOrderId, changeSet.getSerializedDataObjectsMap().values());
        }
    }

    private void fillHistoricAttributes(iPartsAuthorOrderId authorOrderId, iPartsDataChangeSetList changeSetList) {
        for (iPartsDataChangeSet dataChangeSet : changeSetList) {
            for (SerializedDBDataObjectHistory historyEntry : getChangeSetHistoryList(dataChangeSet.getAsId())) {
                fillAttributes(authorOrderId, historyEntry.getHistory());
            }
        }
    }

    private void fillAttributes(iPartsAuthorOrderId authorOrderId, Collection<SerializedDBDataObject> values) {
        for (SerializedDBDataObject serializedDataObject : values) {
            if (showTypeHelper.filterSerializedObject(serializedDataObject)) {
                addGridRow(authorOrderId, serializedDataObject);
            }
        }
    }

    private void addGridRow(iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedDataObject) {
        if ((viewType == ViewType.DIALOG_SUBMODULE) || (viewType == ViewType.DIALOG_PART_LIST_ENTRY)) {
            // Änderungsarten korrigieren für bessere Lesbarkeit (DIALOG-Änderungen werden ja immer direkt in der DB gespeichert)
            if (serializedDataObject.getState() == SerializedDBDataObjectState.DELETED_COMMITTED) {
                serializedDataObject.setState(SerializedDBDataObjectState.DELETED);
            } else if (serializedDataObject.getState() == SerializedDBDataObjectState.COMMITTED) {
                // Unterscheidung zwischen verändert und neu anhand von mindestens einem Attribute mit altem Wert erkennen
                boolean oldValueFound = false;
                if (serializedDataObject.getAttributes() != null) {
                    for (SerializedDBDataObjectAttribute attribute : serializedDataObject.getAttributes()) {
                        if (attribute.getOldValue() != null) {
                            oldValueFound = true;
                            break;
                        }
                    }
                }
                serializedDataObject.setState(oldValueFound ? SerializedDBDataObjectState.MODIFIED : SerializedDBDataObjectState.NEW);
            }
        }

        DBDataObjectAttributes attributes = fillGridRowAttributes(authorOrderId, serializedDataObject);
        addAttributesToGrid(attributes);
        GuiTableRow row = grid.getTable().getRow(grid.getTable().getRowCount() - 1);
        if (row instanceof ChangeSetHistoryGridForm.RowWithAttributesAndSerialized) {
            ((ChangeSetHistoryGridForm.RowWithAttributesAndSerialized)row).serializedObject = serializedDataObject;
        }
    }

    private DBDataObjectAttributes fillGridRowAttributes(iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedDBDataObject) {
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        // hier alle Felder (nicht nur die sichtbaren) füllen
        for (EtkDisplayField displayField : grid.getDisplayResultFields().getFields()) {
            DBDataObjectAttribute attrib = fillAttribute(displayField, authorOrderId, serializedDBDataObject);
            attributes.addField(attrib, DBActionOrigin.FROM_DB);
        }
        return attributes;
    }


    private DBDataObjectAttribute fillAttribute(EtkDisplayField displayField, iPartsAuthorOrderId authorOrderId, SerializedDBDataObject mergedObject) {
        DBDataObjectAttribute attrib = new DBDataObjectAttribute(displayField.getKey().getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
        OBJECT_COLS objCol = OBJECT_COLS.getColByDisplayField(displayField);
        String value = "";
        if (objCol != null) {
            switch (objCol) {
                case OBJECT_TYPE:
                    value = showTypeHelper.getObjectTypeDescriptionTranslated(mergedObject);
                    break;
                case OBJECT_ID:
                    value = showTypeHelper.calculateObjectId(mergedObject);
                    break;
                case RELEASE_DATE:
                    value = mergedObject.getDateTime();
                    break;
                case AUTHOR_ORDER:
                    value = getAuthorOrderFromId(authorOrderId).getAuthorOrderName();
                    break;
                case COMMIT_DATE:
                    if (viewType == ViewType.TECH_CHANGESET) {
                        value = mergedObject.getDateTime();
                    } else {
                        value = getAuthorOrderFromId(authorOrderId).getCommitDate();
                    }
                    break;
                case CURRENT_USER:
                    value = mergedObject.getUserIdWithFallback();
                    break;
                case CHANGE_TYPE:
                    value = showTypeHelper.getStateDescriptionTranslated(mergedObject);
                    break;
                case AUTHOR_GUID:
                    value = authorOrderId.getAuthorGuid();
                    break;
                case CHANGESET_GUID:
                    value = getAuthorOrderFromId(authorOrderId).getChangeSetValue();
                    break;
            }
        }
        attrib.setValueAsString(value, DBActionOrigin.FROM_DB);
        return attrib;
    }

    private void fillExtendedAttributes(iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedDBDataObject) {
        if (!useAlternateLayout) {
            clearExtendedGrid();
        }
        if (showTypeHelper.filterSerializedObject(serializedDBDataObject)) {
            DBDataObjectAttributes basicAttributes = fillBasicExtendedAttributes(authorOrderId, serializedDBDataObject);

            // Sonderfall für M_ETKZ Änderungen bestätigt
            ChangeSetShowTypes.SHOW_TYPES showType = showTypeHelper.getShowType(serializedDBDataObject);
            if (showType == ChangeSetShowTypes.SHOW_TYPES.MAT_ETKZ_APPROVED) {
                // alle FeldNamen aus den Attributen bestimmen
                Map<String, SerializedDBDataObjectAttribute> serializedDBDataObjectAttributeMap = new LinkedHashMap<>();
                calculateAndAddAttribute(authorOrderId, serializedDBDataObject, basicAttributes, null, "",
                                         serializedDBDataObjectAttributeMap, false);
            } else {
                String tableName = serializedDBDataObject.getTableName();
                EtkDatabaseTable table = getConfig().getDBDescription().findTable(tableName);
                iPartsChangeSetInfoDefinitions.ChangeSetObjectIdInfoDefinitions infoDefinitions = getInfoDefinitions(serializedDBDataObject);
                if (serializedDBDataObject.getAttributes() != null) {
                    fillExtendedAttributesByInfoDefinition(authorOrderId, serializedDBDataObject, basicAttributes, infoDefinitions, table);
                    basicAttributes = null;
                }
                if (serializedDBDataObject.getCompositeChildren() != null) {
                    if (basicAttributes != null) {
                        addDefaultValues(basicAttributes, authorOrderId, serializedDBDataObject);
                        addAttributesToExtendedGrid(basicAttributes);
                    }
                    for (SerializedDBDataObjectList<SerializedDBDataObject> serializedDBDataObjectList : serializedDBDataObject.getCompositeChildren()) {
                        for (SerializedDBDataObject serializedDBDataObjectComposite : serializedDBDataObjectList.getList()) {
                            if (showTypeHelper.filterSerializedObject(serializedDBDataObjectComposite)) {
                                basicAttributes = fillBasicExtendedAttributes(authorOrderId, serializedDBDataObjectComposite);
                                tableName = serializedDBDataObjectComposite.getTableName();
                                table = getConfig().getDBDescription().findTable(tableName);
                                infoDefinitions = getInfoDefinitions(serializedDBDataObjectComposite);
                                fillExtendedAttributesByInfoDefinition(authorOrderId, serializedDBDataObjectComposite, basicAttributes, infoDefinitions, table);
                            }
                        }
                    }
                    basicAttributes = null;
                }
                if (basicAttributes != null) {
                    if (serializedDBDataObject.getState() == SerializedDBDataObjectState.NEW) {
                        addNewValues(basicAttributes, authorOrderId, serializedDBDataObject);
                    } else {
                        addDefaultValues(basicAttributes, authorOrderId, serializedDBDataObject);
                    }
                }
                if (basicAttributes != null) {
                    addAttributesToExtendedGrid(basicAttributes);
                }
                if (!useAlternateLayout) {
                    sortExtendedGrid();
                }
            }
        }
    }

    private void fillExtendedAttributesByInfoDefinition(iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedDBDataObject, DBDataObjectAttributes basicAttributes, iPartsChangeSetInfoDefinitions.ChangeSetObjectIdInfoDefinitions infoDefinitions, EtkDatabaseTable table) {
        // alle FeldNamen aus den Attributen bestiimen
        Map<String, SerializedDBDataObjectAttribute> serializedDBDataObjectAttributeMap = new LinkedHashMap<>();
        List<String> tableFieldNames = null;
        if (table != null) {
            tableFieldNames = table.getFieldListAsStringList(true);
        }
        for (SerializedDBDataObjectAttribute serializedDBDataObjectAttribute : serializedDBDataObject.getAttributes()) {
            if (showTypeHelper.filterAllowedAttributes(serializedDBDataObjectAttribute.getName())) {
                if ((tableFieldNames == null) || tableFieldNames.contains(serializedDBDataObjectAttribute.getName())) {
                    serializedDBDataObjectAttributeMap.put(serializedDBDataObjectAttribute.getName(), serializedDBDataObjectAttribute);
                }
            }
        }
        if (infoDefinitions != null) {
            // isNotModified = true entfernen
            List<String> serializedFieldNames = new DwList<>(serializedDBDataObjectAttributeMap.keySet());
            for (String fieldName : serializedFieldNames) {
                SerializedDBDataObjectAttribute serializedDBDataObjectAttribute = serializedDBDataObjectAttributeMap.get(fieldName);
                if ((serializedDBDataObjectAttribute != null) && serializedDBDataObjectAttribute.isNotModified()) {
                    if (!infoDefinitions.getMustFields().contains(fieldName) && !infoDefinitions.getFields().contains(fieldName)) {
                        serializedDBDataObjectAttributeMap.remove(fieldName);
                    }
                }
            }
            // alle Must-Felder aus InfoDefinition ausgeben
            for (String fieldName : infoDefinitions.getMustFields()) {
                calculateAndAddAttribute(authorOrderId, serializedDBDataObject, basicAttributes, table, fieldName,
                                         serializedDBDataObjectAttributeMap, false);
            }
            // restliche Felder aus InfoDefinition ausgeben
            for (String fieldName : infoDefinitions.getFields()) {
                if (serializedDBDataObjectAttributeMap.get(fieldName) != null) {
                    calculateAndAddAttribute(authorOrderId, serializedDBDataObject, basicAttributes, table, fieldName,
                                             serializedDBDataObjectAttributeMap, serializedDBDataObject.getState() == SerializedDBDataObjectState.NEW);
                }
            }
        }
        // übriggebliebene Felder des serializedDBDataObject ausgeben
        List<String> serializedFieldNames = new DwList<>(serializedDBDataObjectAttributeMap.keySet());
        for (String fieldName : serializedFieldNames) {
            calculateAndAddAttribute(authorOrderId, serializedDBDataObject, basicAttributes, table, fieldName,
                                     serializedDBDataObjectAttributeMap, serializedDBDataObject.getState() == SerializedDBDataObjectState.NEW);
        }
    }

    private void calculateAndAddAttribute(iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedDBDataObject,
                                          DBDataObjectAttributes basicAttributes, EtkDatabaseTable table, String fieldName,
                                          Map<String, SerializedDBDataObjectAttribute> serializedDBDataObjectAttributeMap,
                                          boolean checkEmptyValue) {
        SerializedDBDataObjectAttribute serializedDBDataObjectAttribute = serializedDBDataObjectAttributeMap.get(fieldName);
        DBDataObjectAttributes workAttributes = new DBDataObjectAttributes();
        workAttributes.assign(basicAttributes, DBActionOrigin.FROM_DB);
        addExtraValues(workAttributes, serializedDBDataObjectAttribute, authorOrderId, serializedDBDataObject, table, fieldName);
        if (checkEmptyValue) {
            String valueNew = workAttributes.getField(EXTENDED_OBJECT_COLS.CHANGE_NEW_VALUE.getFieldName()).getAsString();
            if (!valueNew.isEmpty() || ((serializedDBDataObjectAttribute != null) && !serializedDBDataObjectAttribute.isEmpty())) {
                addAttributesToExtendedGrid(workAttributes);
            }
        } else {
            addAttributesToExtendedGrid(workAttributes);
        }
        if ((serializedDBDataObjectAttribute != null) && (serializedDBDataObjectAttributeMap != null)) {
            serializedDBDataObjectAttributeMap.remove(serializedDBDataObjectAttribute.getName());
        }
    }

    private iPartsChangeSetInfoDefinitions.ChangeSetObjectIdInfoDefinitions getInfoDefinitions(SerializedDBDataObject serializedDBDataObject) {
        return showTypeHelper.getInfoDefinitions(serializedDBDataObject);
    }

    private void fillExtendedAlternateHistoricAttributes(iPartsAuthorOrderId authorOrderId, IdWithType selectedId) {
        clearExtendedGrid();
        if ((authorOrderId != null) && authorOrderId.isValidId()) {
            fillExtendedAlternateHistoricAttributesForAuthorId(authorOrderId, selectedId);
        } else {
            for (iPartsAuthorOrderId currentAuthorOrderId : authorOrderMap.keySet()) {
                fillExtendedAlternateHistoricAttributesForAuthorId(currentAuthorOrderId, selectedId);
            }
        }
        sortExtendedGrid();
        showNoResultLabelExtendedGrid();
    }

    private void fillExtendedAlternateHistoricAttributesForAuthorId(iPartsAuthorOrderId authorOrderId, IdWithType selectedId) {
        iPartsDataChangeSetList changeSetList = getChangeSetListFromAuthorOrderId(authorOrderId);
        if (changeSetList != null) {
            for (iPartsDataChangeSet dataChangeSet : changeSetList) {
//                    if (dataChangeSet.getAsId().equals(changeSetId)) {
                for (SerializedDBDataObjectHistory<SerializedDBDataObject> historyEntry : getChangeSetHistoryList(dataChangeSet.getAsId())) {
                    for (SerializedDBDataObject serializedDBDataObject : historyEntry.getHistory()) {
                        if (serializedDBDataObject.createId().equals(selectedId)) {
                            fillExtendedAttributes(authorOrderId, serializedDBDataObject);
                        }
                    }
                }
//                    }
            }
        }
    }

    private DBDataObjectAttributes fillBasicExtendedAttributes(iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedDBDataObject) {
        DBDataObjectAttributes basicAttributes = new DBDataObjectAttributes();
        for (EtkDisplayField displayField : extendedGrid.getDisplayResultFields().getFields()) {
            if (basicExtendedFieldNames.contains(displayField.getKey().getFieldName())) {
                DBDataObjectAttribute attrib = fillExtendedAttribute(displayField, authorOrderId, serializedDBDataObject);
                basicAttributes.addField(attrib, DBActionOrigin.FROM_DB);
            }
        }
        return basicAttributes;
    }

    private void addNewValues(DBDataObjectAttributes attributes, iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedDBDataObject) {
        for (String fieldName : extraExtendedFieldNames) {
            EtkDisplayField displayField = extendedGrid.getDisplayResultFields().getFeldByName(tableName, fieldName);
            DBDataObjectAttribute attrib = null;
            if (displayField != null) {
                attrib = fillNewOrDefaultExtendedAttribute(displayField, authorOrderId, serializedDBDataObject, false);
            } else {
                attrib = new DBDataObjectAttribute(fieldName, DBDataObjectAttribute.TYPE.STRING, false);
            }
            if (attrib != null) {
                attributes.addField(attrib, DBActionOrigin.FROM_DB);
            }
        }
    }

    private void addDefaultValues(DBDataObjectAttributes attributes, iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedDBDataObject) {
        for (String fieldName : extraExtendedFieldNames) {
            EtkDisplayField displayField = extendedGrid.getDisplayResultFields().getFeldByName(tableName, fieldName);
            DBDataObjectAttribute attrib;
            if (displayField != null) {
                attrib = fillNewOrDefaultExtendedAttribute(displayField, authorOrderId, serializedDBDataObject, true);
            } else {
                attrib = new DBDataObjectAttribute(fieldName, DBDataObjectAttribute.TYPE.STRING, false);
            }
            if (attrib != null) {
                attributes.addField(attrib, DBActionOrigin.FROM_DB);
            }
        }
    }

    private void addExtraValues(DBDataObjectAttributes attributes, SerializedDBDataObjectAttribute serializedDBDataObjectAttribute,
                                iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedDBDataObject,
                                EtkDatabaseTable table, String fieldName) {
        for (EtkDisplayField displayField : extendedGrid.getDisplayResultFields().getVisibleFields()) {
            if (extraExtendedFieldNames.contains(displayField.getKey().getFieldName())) {
                DBDataObjectAttribute attrib;
                if (serializedDBDataObjectAttribute != null) {
                    attrib = fillExtraExtendedAttribute(displayField, serializedDBDataObjectAttribute, authorOrderId, serializedDBDataObject, table);
                } else {
                    attrib = fillExtraExtendedAttribute(displayField, fieldName, authorOrderId, serializedDBDataObject, table);
                }
                if (attrib != null) {
                    attributes.addField(attrib, DBActionOrigin.FROM_DB);
                }
            }
        }
    }

    private DBDataObjectAttribute fillExtendedAttribute(EtkDisplayField displayField, iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedObject) {
        DBDataObjectAttribute attrib = new DBDataObjectAttribute(displayField.getKey().getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
        EXTENDED_OBJECT_COLS objCol = EXTENDED_OBJECT_COLS.getExtendedColByDisplayField(displayField);
        String value = "";
        if (objCol != null) {
            switch (objCol) {
                case OBJECT_TYPE:
                    value = showTypeHelper.getObjectTypeDescriptionTranslated(serializedObject);
                    break;
                case OBJECT_ID:
                    value = showTypeHelper.calculateObjectId(serializedObject);
                    break;
                case RELEASE_DATE:
                    value = serializedObject.getDateTime();
                    break;
                case AUTHOR_ORDER:
                    value = getAuthorOrderFromId(authorOrderId).getAuthorOrderName();
                    break;
                case CURRENT_USER:
                    value = serializedObject.getUserIdWithFallback();
                    break;
                case CHANGE_TYPE:
                    value = showTypeHelper.getStateDescriptionTranslated(serializedObject);
                    break;
                case AUTHOR_GUID:
                    value = authorOrderId.getAuthorGuid();
                    break;
                case CHANGESET_GUID:
                    value = getAuthorOrderFromId(authorOrderId).getChangeSetValue();
                    break;
            }
        }
        attrib.setValueAsString(value, DBActionOrigin.FROM_DB);
        return attrib;
    }

    private DBDataObjectAttribute fillNewOrDefaultExtendedAttribute(EtkDisplayField displayField, iPartsAuthorOrderId authorOrderId,
                                                                    SerializedDBDataObject serializedObject, boolean isDefault) {
        DBDataObjectAttribute attrib = new DBDataObjectAttribute(displayField.getKey().getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
        EXTENDED_OBJECT_COLS objCol = EXTENDED_OBJECT_COLS.getExtendedColByDisplayField(displayField);
        String value = "";
        if (objCol != null) {
            switch (objCol) {
                case RELEASE_DATE:
                    value = serializedObject.getDateTime();
                    break;
                case AUTHOR_ORDER:
                    value = getAuthorOrderFromId(authorOrderId).getAuthorOrderName();
                    break;
                case CURRENT_USER:
                    value = serializedObject.getUserIdWithFallback();
                    break;
                case CHANGE_TYPE:
                    value = showTypeHelper.getStateDescriptionTranslated(serializedObject);
                    break;
                case CHANGE_CONTENT:
                    value = TranslationHandler.translate(showTypeHelper.getObjectTypeDescription(serializedObject));
                    break;
                case CHANGE_OLD_VALUE:
                    // value bleibt leer
                    break;
                case CHANGE_NEW_VALUE:
                    if (serializedObject.getType().equals(iPartsReplacePartId.TYPE) && (serializedObject.getAttributes() != null)) {
                        String matNr = "";
                        String lfdNr = "";
                        for (SerializedDBDataObjectAttribute serializedDBDataObjectAttribute : serializedObject.getAttributes()) {
                            if (serializedDBDataObjectAttribute.getName().equals(FIELD_DRP_REPLACE_MATNR)) {
                                matNr = serializedDBDataObjectAttribute.getValue();
                            } else if (serializedDBDataObjectAttribute.getName().equals(FIELD_DRP_REPLACE_LFDNR)) {
                                lfdNr = serializedDBDataObjectAttribute.getValue();
                            }
                        }
                        value = showTypeHelper.getFormatter().formatMatAndLfdNo(matNr, lfdNr);
                    } else {
                        if (isDefault) {
                            // value bleibt leer
                        } else {
                            value = TranslationHandler.translate(SerializedDBDataObjectState.NEW.getDescription());
                        }
                    }
                    break;
                default:
                    attrib = null;
                    break;
            }
            if (attrib != null) {
                attrib.setValueAsString(value, DBActionOrigin.FROM_DB);
            }
        } else {
            attrib = null;
        }
        return attrib;
    }

    private DBDataObjectAttribute fillExtraExtendedAttribute(EtkDisplayField displayField, SerializedDBDataObjectAttribute serializedDBDataObjectAttribute,
                                                             iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedObject,
                                                             EtkDatabaseTable table) {
        DBDataObjectAttribute attrib = new DBDataObjectAttribute(displayField.getKey().getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
        EXTENDED_OBJECT_COLS objCol = EXTENDED_OBJECT_COLS.getExtendedColByDisplayField(displayField);
        String value = "";
        if (objCol != null) {
            String chgSetFieldName = serializedDBDataObjectAttribute.getName();
            String dbLanguage = getProject().getDBLanguage();
            boolean isAttribValueSet = false;
            switch (objCol) {
                case RELEASE_DATE:
                    value = serializedObject.getDateTime();
                    break;
                case AUTHOR_ORDER:
                    value = getAuthorOrderFromId(authorOrderId).getAuthorOrderName();
                    break;
                case CURRENT_USER:
                    value = serializedObject.getUserIdWithFallback();
                    break;
                case CHANGE_TYPE:
                    if (serializedDBDataObjectAttribute.isNotModified()) {
                        // value bleibt leer
                    } else {
                        value = showTypeHelper.getStateDescriptionTranslated(serializedObject);
                    }
                    break;
                case CHANGE_CONTENT:
                    value = ChangeSetShowTypes.getDisplayName(getProject(), table, chgSetFieldName);
                    break;
                case CHANGE_OLD_VALUE:
                    // Bei geladenen und neuen SerializedDBDataObjects keinen alten Wert anzeigen
                    if ((serializedObject.getState() == SerializedDBDataObjectState.LOADED) || (serializedObject.getState() == SerializedDBDataObjectState.NEW)) {
                        // value bleibt leer
                    } else if ((serializedObject.getState() == SerializedDBDataObjectState.DELETED) || serializedDBDataObjectAttribute.isNotModified()) {
                        // gelöschte oder nicht modifizierte Werte in die OLD-Spalte
                        isAttribValueSet = true;
                        if (viewType == ViewType.TECH_CHANGESET) {
                            showTypeHelper.setAttributeValueExtended(serializedDBDataObjectAttribute, serializedObject.getTableName(), chgSetFieldName,
                                                                     dbLanguage, attrib, false, serializedObject);
                        } else {
                            showTypeHelper.setAttributeValue(serializedDBDataObjectAttribute, serializedObject.getTableName(), chgSetFieldName,
                                                             dbLanguage, attrib, false);
                        }
                    } else if (serializedObject.getState() == SerializedDBDataObjectState.DELETED_COMMITTED) {
                        isAttribValueSet = true;
                        showTypeHelper.setAttributeValue(serializedDBDataObjectAttribute, serializedObject.getTableName(), chgSetFieldName,
                                                         dbLanguage, attrib, false);
                    } else {
                        isAttribValueSet = true;
                        showTypeHelper.setAttributeValue(serializedDBDataObjectAttribute, serializedObject.getTableName(), chgSetFieldName,
                                                         dbLanguage, attrib, true);
                    }
                    break;
                case CHANGE_NEW_VALUE:
                    if ((serializedObject.getState() != SerializedDBDataObjectState.DELETED) && !serializedDBDataObjectAttribute.isNotModified()) {
                        if (serializedObject.getState() != SerializedDBDataObjectState.DELETED_COMMITTED) {
                            isAttribValueSet = true;
                            if (viewType == ViewType.TECH_CHANGESET) {
                                showTypeHelper.setAttributeValueExtended(serializedDBDataObjectAttribute, serializedObject.getTableName(), chgSetFieldName,
                                                                         dbLanguage, attrib, false, serializedObject);
                            } else {
                                showTypeHelper.setAttributeValue(serializedDBDataObjectAttribute, serializedObject.getTableName(), chgSetFieldName,
                                                                 dbLanguage, attrib, false);
                            }
                        }
                    }
                    break;
                default:
                    attrib = null;
                    break;
            }
            if ((attrib != null) && !isAttribValueSet) {
                attrib.setValueAsString(value, DBActionOrigin.FROM_DB);
            }
        } else {
            attrib = null;
        }
        return attrib;
    }

    private DBDataObjectAttribute fillExtraExtendedAttribute(EtkDisplayField displayField, String changeSetFieldName,
                                                             iPartsAuthorOrderId authorOrderId, SerializedDBDataObject serializedObject,
                                                             EtkDatabaseTable table) {
        DBDataObjectAttribute attrib = new DBDataObjectAttribute(displayField.getKey().getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
        EXTENDED_OBJECT_COLS objCol = EXTENDED_OBJECT_COLS.getExtendedColByDisplayField(displayField);
        String value = "";
        if (objCol != null) {
            boolean isAttribValueSet = false;
            String dbLanguage = getProject().getDBLanguage();
            switch (objCol) {
                case RELEASE_DATE:
                    value = serializedObject.getDateTime();
                    break;
                case AUTHOR_ORDER:
                    value = getAuthorOrderFromId(authorOrderId).getAuthorOrderName();
                    break;
                case CURRENT_USER:
                    value = serializedObject.getUserIdWithFallback();
                    break;
                case CHANGE_TYPE:
                    if (showTypeHelper.getShowType(serializedObject) == ChangeSetShowTypes.SHOW_TYPES.MAT_ETKZ_APPROVED) {
                        value = TranslationHandler.translate(SerializedDBDataObjectState.COMMITTED.getDescription());
                    }
                    break;
                case CHANGE_CONTENT:
                    ChangeSetShowTypes.SHOW_TYPES showType = showTypeHelper.getShowType(serializedObject);
                    if (showTypeHelper.getShowType(serializedObject) == ChangeSetShowTypes.SHOW_TYPES.MAT_ETKZ_APPROVED) {
                        value = "ETKZ";
                    } else {
                        value = ChangeSetShowTypes.getDisplayName(getProject(), table, changeSetFieldName);
                    }
                    break;
                case CHANGE_OLD_VALUE:
                    if (table != null) {
                        if (!table.getName().equals(TABLE_DA_INTERNAL_TEXT)) {
                            int index = table.getPrimaryKeyFields().indexOf(changeSetFieldName);
                            String[] serializedObjectPkValues = serializedObject.getPkValues();
                            if ((index != -1) && (index < serializedObjectPkValues.length)) {
                                value = serializedObjectPkValues[index];
                                value = showTypeHelper.modifyValue(table.getName(), changeSetFieldName, value, dbLanguage);
                                isAttribValueSet = true;
                                showTypeHelper.setAttributeValueAsString(value, table.getName(), changeSetFieldName, dbLanguage, attrib);
                            }
                        }
                    }
                    break;
                case CHANGE_NEW_VALUE:
                    // value bleibt leer
                    break;
                default:
                    attrib = null;
                    break;

            }
            if ((attrib != null) && !isAttribValueSet) {
                attrib.setValueAsString(value, DBActionOrigin.FROM_DB);
            }
        } else {
            attrib = null;
        }
        return attrib;
    }

    private String findInAttributes(List<SerializedDBDataObjectAttribute> serializedDBDataObjectAttributes, String fieldName) {
        if (serializedDBDataObjectAttributes != null) {
            for (SerializedDBDataObjectAttribute serializedDBDataObjectAttribute : serializedDBDataObjectAttributes) {
                if (serializedDBDataObjectAttribute.getName().equals(fieldName)) {
                    return serializedDBDataObjectAttribute.getValue();
                }
            }
        }
        return "";
    }

    private iPartsDataAuthorOrder getAuthorOrderFromId(iPartsAuthorOrderId authorOrderId) {
        return authorOrderMap.computeIfAbsent(authorOrderId, id -> {
            iPartsDataAuthorOrder authorData = new iPartsDataAuthorOrder(getProject(), id);
            // nachladen verhindern (iPartsDataAuthorOrder muss vorher schon in der Map liegen falls in der DB vorhanden)
            authorData.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            return authorData;
        });
    }

    private iPartsDataChangeSetList getChangeSetListFromAuthorOrderId(iPartsAuthorOrderId authorOrderId) {
        iPartsDataChangeSetList changeSetList = changeSetListMap.get(authorOrderId);
        if (changeSetList == null) {
            if (viewType == ViewType.TECH_CHANGESET) {
                String techChangeSetGUID = authorOrderId.getAuthorGuid();
                if (StrUtils.isValid(techChangeSetGUID)) {
                    iPartsDataChangeSet dataChangeSet = new iPartsDataChangeSet(getProject(), new iPartsChangeSetId(techChangeSetGUID));
                    if (dataChangeSet.existsInDB()) {
                        changeSetList = iPartsDataChangeSetList.loadChangeSetsForChangeSetAndSourceAndStatus(getProject(), dataChangeSet.getAsId(),
                                                                                                             dataChangeSet.getSource(),
                                                                                                             dataChangeSet.getStatus());
                        changeSetListMap.put(authorOrderId, changeSetList);
                    }
                }
            } else {
                changeSetList = iPartsDataChangeSetList.loadChangeSetsForChangeSetAndSourceAndStatus(getProject(), getAuthorOrderFromId(authorOrderId).getChangeSetId(),
                                                                                                     iPartsChangeSetSource.AUTHOR_ORDER,
                                                                                                     iPartsChangeSetStatus.IN_PROCESS);
                iPartsDataChangeSetList comittedChangeSetList = iPartsDataChangeSetList.loadChangeSetsForChangeSetAndSourceAndStatus(getProject(), getAuthorOrderFromId(authorOrderId).getChangeSetId(),
                                                                                                                                     iPartsChangeSetSource.AUTHOR_ORDER,
                                                                                                                                     iPartsChangeSetStatus.COMMITTED);
                for (iPartsDataChangeSet changeSet : comittedChangeSetList) {
                    changeSetList.add(changeSet, DBActionOrigin.FROM_DB);
                }
                changeSetListMap.put(authorOrderId, changeSetList);
            }
        }
        return changeSetList;
    }

    private iPartsRevisionChangeSet getChangeSet(iPartsChangeSetId changeSetId) {
        iPartsRevisionChangeSet changeSet = changeSetMap.get(changeSetId);
        if (changeSet == null) {
            changeSet = new iPartsRevisionChangeSet(changeSetId, getProject());
            changeSetMap.put(changeSetId, changeSet);
        }
        return changeSet;
    }

    private Collection<SerializedDBDataObjectHistory> getChangeSetHistoryList(iPartsChangeSetId changeSetId) {
        Collection<SerializedDBDataObjectHistory> changeSetHistoryList = changeSetHistoryMap.get(changeSetId);
        if (changeSetHistoryList == null) {
            iPartsRevisionChangeSet changeSet = getChangeSet(changeSetId);
            changeSetHistoryList = changeSet.loadHistoryFromDB().values();
            changeSetHistoryMap.put(changeSetId, changeSetHistoryList);
        }
        return changeSetHistoryList;
    }

    private void fillDataGrid() {
        fillGrid(false);
    }

    private void fillHistoricDataGrid() {
        if (!useAlternateLayout) {
            fillGrid(true);
        } else {
            SerializedDBDataObject currentSerializedObject = getSelectedElement();
            if (currentSerializedObject == null) {
                return;
            }
            iPartsAuthorOrderId authorOrderId = getSelectedAuthorId();
            IdWithType selectedId = currentSerializedObject.createId();
            fillExtendedAlternateHistoricAttributes(authorOrderId, selectedId);
        }
    }

    private void fillGridForAssembly(EtkDataAssembly assembly) {
        EtkProject project = getProject();
        Map<String, ChangeSetData> changeSetDataMap = new HashMap<>();
        if (assembly != null) {
            // ChangeSetEntries laden für:
            // Modul-Kopfdaten
            AssemblyId assemblyId = assembly.getAsId();
            this.viewingAssemblyId = assemblyId;
            loadCommittedChangeSetEntriesForPartialId(assemblyId, changeSetDataMap, project);

            // Modul-Metadaten
            iPartsModuleId moduleId = new iPartsModuleId(assemblyId.getKVari());
            loadCommittedChangeSetEntriesForPartialId(moduleId, changeSetDataMap, project);

            // Modul-Verwendungen inkl. Gültigkeiten
            iPartsModuleEinPASId moduleEinPASId = new iPartsModuleEinPASId("*", assemblyId.getKVari(), "*");
            loadCommittedChangeSetEntriesForPartialId(moduleEinPASId, changeSetDataMap, project);

            // Bildaufträge (mit dem Umweg über Bildauftrag zu Modul mittels iPartsPicOrderModulesId)
            iPartsPicOrderModulesId picOrderModulesId = new iPartsPicOrderModulesId("*", assemblyId.getKVari());
            Map<String, ChangeSetData> picOrdersChangeSetDataMap = new HashMap<>();
            loadCommittedChangeSetEntriesForPartialId(picOrderModulesId, picOrdersChangeSetDataMap, project);
            for (Map.Entry<String, ChangeSetData> changeSetDataEntry : picOrdersChangeSetDataMap.entrySet()) {
                ChangeSetData changeSetData = changeSetDataEntry.getValue();
                for (iPartsDataChangeSetEntry changeSetEntry : changeSetData.changeSetEntries) {
                    String picOrderModulesIdString = changeSetEntry.getAsId().getDataObjectId();
                    IdWithType id = IdWithType.fromDBString(iPartsPicOrderModulesId.TYPE, picOrderModulesIdString);
                    iPartsPicOrderModulesId picOrderModulesIdFromCSE = new iPartsPicOrderModulesId(id.toStringArrayWithoutType());

                    // Alle ChangeSetEntries für diese Bildauftrags-GUID suchen und hinzufügen
                    iPartsPicOrderId picOrderId = new iPartsPicOrderId(picOrderModulesIdFromCSE.getOrderGuid());
                    loadCommittedChangeSetEntriesForPartialId(picOrderId, changeSetDataMap, project);
                }
            }

            // Gelöschte Stücklisteneinträge
            PartListEntryId partListEntryId = new PartListEntryId(assemblyId.getKVari(), assemblyId.getKVer(), "*");
            Map<String, ChangeSetData> deletedPLEsChangeSetDataMap = new HashMap<>();
            loadCommittedChangeSetEntriesForPartialId(partListEntryId, deletedPLEsChangeSetDataMap, project);
            if (!deletedPLEsChangeSetDataMap.isEmpty()) {
                SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);
                for (Map.Entry<String, ChangeSetData> changeSetDataEntry : deletedPLEsChangeSetDataMap.entrySet()) {
                    ChangeSetData deletedPLEsChangeSetData = null;
                    ChangeSetData changeSetData = changeSetDataEntry.getValue();
                    for (iPartsDataChangeSetEntry changeSetEntry : changeSetData.changeSetEntries) {
                        String jsonString = changeSetEntry.getCurrentData();
                        if (!jsonString.isEmpty()) {
                            SerializedDBDataObject serializedDBDataObject = serializedDbDataObjectAsJSON.getFromJSON(jsonString);

                            // Nur gelöschte Stücklisteneinträge
                            if (serializedDBDataObject.getState() == SerializedDBDataObjectState.DELETED) {
                                if (deletedPLEsChangeSetData == null) {
                                    deletedPLEsChangeSetData = new ChangeSetData();
                                    deletedPLEsChangeSetData.commitDate = changeSetData.commitDate;
                                    deletedPLEsChangeSetData.source = changeSetData.source;
                                    deletedPLEsChangeSetData.changeSetEntries = new iPartsDataChangeSetEntryList();
                                }
                                deletedPLEsChangeSetData.changeSetEntries.add(changeSetEntry, DBActionOrigin.FROM_DB);
                            }
                        }
                    }

                    if (deletedPLEsChangeSetData != null) {
                        // Die ChangeSetEntries der gelöschten Stücklisteneinträge zu den anderen hinzufügen
                        String changeSetID = changeSetDataEntry.getKey();
                        ChangeSetData existingChangeSetData = changeSetDataMap.get(changeSetID);
                        if (existingChangeSetData != null) {
                            existingChangeSetData.changeSetEntries.addAll(deletedPLEsChangeSetData.changeSetEntries, DBActionOrigin.FROM_DB);
                        } else {
                            changeSetDataMap.put(changeSetID, deletedPLEsChangeSetData);
                        }
                    }
                }
            }
        }

        fillGridForChangeSetDataMap(changeSetDataMap, project);
    }

    private void fillGridForPartListEntry(EtkDataPartListEntry partListEntry) {
        EtkProject project = getProject();
        Map<String, ChangeSetData> changeSetDataMap = new HashMap<>();
        if (partListEntry != null) {
            // ChangeSetEntries laden für:
            // Stücklisteneintrag selbst
            PartListEntryId partListEntryId = partListEntry.getAsId();
            loadCommittedChangeSetEntriesForPartialId(partListEntryId, changeSetDataMap, project);

            // kombinierte Texte
            iPartsCombTextId combTextPartialId = new iPartsCombTextId(partListEntryId, "*");
            loadCommittedChangeSetEntriesForPartialId(combTextPartialId, changeSetDataMap, project);

            // Fußnoten
            iPartsFootNoteCatalogueRefId footNotePartialId = new iPartsFootNoteCatalogueRefId(partListEntryId, "*");
            loadCommittedChangeSetEntriesForPartialId(footNotePartialId, changeSetDataMap, project);

            // Ersetzungen
            iPartsReplacePartId replacePartPartialId = new iPartsReplacePartId(partListEntryId, "*");
            loadCommittedChangeSetEntriesForPartialId(replacePartPartialId, changeSetDataMap, project);

            // Werkseinsatzdaten über K_SOURCE_GUID ermitteln
            String sourceGUID = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
            if (!sourceGUID.isEmpty()) {
                iPartsFactoryDataId factoryDataPartialId = new iPartsFactoryDataId(sourceGUID, "*", "*", "*", "*", "*");
                loadCommittedChangeSetEntriesForPartialId(factoryDataPartialId, changeSetDataMap, project);
            }
        }

        fillGridForChangeSetDataMap(changeSetDataMap, project);
    }

    /**
     * Befüllt die Tabelle mit den Inhalten der übergebenen {@code changeSetDataMap}.
     *
     * @param changeSetDataMap Map von ChangeSet-ID auf {@link ChangeSetData}
     * @param project
     */
    private void fillGridForChangeSetDataMap(Map<String, ChangeSetData> changeSetDataMap, EtkProject project) {
        clearGrid();
        clearExtendedGrid();
        if (!changeSetDataMap.isEmpty()) {
            // Für alle aufgesammelten ChangeSetEntries eines ChangeSets ein dazugehöriges ChangeSet mit diesen Daten erzeugen
            // und SerializedDBDataObjects daraus erzeugen, sowie die Autoren-Aufträge (real und Dunmmy) speichern
            Map<iPartsChangeSetSource, Integer> tcsNumbers = new HashMap<>();
            for (Map.Entry<String, ChangeSetData> changeSetDataEntry : changeSetDataMap.entrySet()) {
                iPartsRevisionChangeSet changeSet = new iPartsRevisionChangeSet(new iPartsChangeSetId(changeSetDataEntry.getKey()),
                                                                                project, false);
                ChangeSetData changeSetData = changeSetDataEntry.getValue();
                changeSet.addDataChangeSetEntryList(changeSetData.changeSetEntries, null);

                iPartsDataAuthorOrder authorOrder;
                if (StrUtils.isValid(changeSetData.authorOrderId)) {
                    // realen AutorenAuftrag anlegen
                    authorOrder = new iPartsDataAuthorOrder(project, new iPartsAuthorOrderId(changeSetData.authorOrderId));
                    if (!authorOrder.existsInDB()) { // Kann eigentlich nicht passieren
                        authorOrder.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }
                } else {
                    // Dummy-Autoren-Auftrag anlegen
                    iPartsAuthorOrderId aoId = new iPartsAuthorOrderId(changeSet.getChangeSetId().getGUID());
                    authorOrder = new iPartsDataAuthorOrder(project, aoId);
                    authorOrder.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    authorOrder.setChangeSetId(new iPartsChangeSetId(changeSet.getChangeSetId().getGUID()));
                }

                // CommitDate setzen (ist bei älteren Autoren-Aufträgen nicht am Autoren-Auftrag abgespeichert)
                authorOrder.setCommitDateForHistory(changeSetData.commitDate);

                if (changeSetData.source != iPartsChangeSetSource.AUTHOR_ORDER) {
                    // Kennzeichnung (via Autoren-Auftrags-Namen) welche Technischen ChangeSets zusammengehören
                    Integer lfdNr = tcsNumbers.get(changeSetData.source);
                    if (lfdNr == null) {
                        lfdNr = 1;
                    }
                    authorOrder.setAuthorOrderName(showTypeHelper.getTechChangeSetName(changeSetData.source, lfdNr));
                    lfdNr++;
                    tcsNumbers.put(changeSetData.source, lfdNr);
                }
                authorOrderMap.put(authorOrder.getAsId(), authorOrder);

                fillAttributes(authorOrder.getAsId(), changeSet.getSerializedDataObjectsMap().values());
            }

            sortGrid();
        }
        showNoResultLabelGrid();
        showNoResultLabelExtendedGrid();
    }

    private void fillGridForHmMSMId(HmMSmId hmMSmId) {
        EtkProject project = getProject();
        Map<String, ChangeSetData> changeSetDataMap = new HashMap<>();
        if (hmMSmId != null) {
            // ChangeSetEntries laden für HmMSmId -> HmMSmId ist Präfix von iPartsDialogId mit Wildcard am Ende
            iPartsDialogId dialogId = new iPartsDialogId(hmMSmId.toString(iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER)
                                                         + iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER + "*");

            // DIALOG-Änderungen werden direkt in der DB gespeichert und sind somit auch schon bei nicht freigegebenen Autoren-Aufträgen relevant
            loadChangeSetEntriesForPartialId(dialogId, changeSetDataMap, false, project);
        }

        fillGridForChangeSetDataMap(changeSetDataMap, project);
    }

    private void fillGridForBcteKey(iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
        EtkProject project = getProject();
        Map<String, ChangeSetData> changeSetDataMap = new HashMap<>();
        if (primaryBCTEKey != null) {
            // ChangeSetEntries laden für BCTEKey -> iPartsDialogId
            iPartsDialogId dialogId = new iPartsDialogId(primaryBCTEKey.toString(iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER, false));

            // DIALOG-Änderungen werden direkt in der DB gespeichert und sind somit auch schon bei nicht freigegebenen Autoren-Aufträgen relevant
            loadChangeSetEntriesForPartialId(dialogId, changeSetDataMap, false, project);
        }

        fillGridForChangeSetDataMap(changeSetDataMap, project);
    }

    private void loadChangeSetEntriesForPartialId(IdWithType partialId, Map<String, ChangeSetData> changeSetDataMap,
                                                  boolean onlyCommittedChanges, EtkProject project) {
        iPartsDataChangeSetEntryList changeSetEntryList = new iPartsDataChangeSetEntryList();
        changeSetEntryList.loadForDataObjectIdWithTypeAndChangeSetStatus(project, partialId, onlyCommittedChanges ? iPartsChangeSetStatus.COMMITTED : null);
        for (iPartsDataChangeSetEntry dataChangeSetEntry : changeSetEntryList) {
            ChangeSetData changeSetData = changeSetDataMap.computeIfAbsent(dataChangeSetEntry.getAsId().getGUID(), changeSetGUID -> {
                ChangeSetData newChangeSetData = new ChangeSetData();
                newChangeSetData.commitDate = dataChangeSetEntry.getFieldValue(FIELD_DCS_COMMIT_DATE);
                newChangeSetData.source = iPartsChangeSetSource.getSourceByDbValue(dataChangeSetEntry.getFieldValue(FIELD_DCS_SOURCE));
                newChangeSetData.authorOrderId = dataChangeSetEntry.getFieldValue(FIELD_DAO_GUID);
                newChangeSetData.changeSetEntries = new iPartsDataChangeSetEntryList();
                return newChangeSetData;
            });
            changeSetData.changeSetEntries.add(dataChangeSetEntry, DBActionOrigin.FROM_DB);
        }
    }

    private void loadCommittedChangeSetEntriesForPartialId(IdWithType partialId, Map<String, ChangeSetData> changeSetDataMap,
                                                           EtkProject project) {
        loadChangeSetEntriesForPartialId(partialId, changeSetDataMap, true, project);
    }

    private SerializedDBDataObject getSerializedObjectFromRow(GuiTableRow row) {
        if ((row != null) && row instanceof ChangeSetHistoryGridForm.RowWithAttributesAndSerialized) {
            return ((ChangeSetHistoryGridForm.RowWithAttributesAndSerialized)row).serializedObject;
        }
        return null;
    }

    private SerializedDBDataObject getSelectedElement() {
        return getSerializedObjectFromRow(grid.getTable().getSelectedRow());
    }

    private List<SerializedDBDataObject> getSelectedElements() {
        List<GuiTableRow> selectedRows = grid.getTable().getSelectedRows();
        if (!Utils.isValid(selectedRows)) {
            return null;
        }
        return selectedRows.stream()
            .map(tableRow -> getSerializedObjectFromRow(tableRow))
            .collect(Collectors.toList());
    }

    private iPartsAuthorOrderId getSelectedAuthorId() {
        if (getSelectedElement() != null) {
            return new iPartsAuthorOrderId(grid.getSelectedAttributes().getField(OBJECT_COLS.AUTHOR_GUID.getFieldName(), false).getAsString());
        }
        return new iPartsAuthorOrderId("");
    }

    private void setSelectedElement(SerializedDBDataObject selectedDataObject) {
        if (selectedDataObject == null) {
            return;
        }
        for (int rowNo = 0; rowNo < grid.getTable().getRowCount(); rowNo++) {
            SerializedDBDataObject currentDataObject = getSerializedObjectFromRow(grid.getTable().getRow(rowNo));
            if ((currentDataObject != null) && currentDataObject.createId().equals(selectedDataObject.createId())) {
                grid.getTable().setSelectedRow(rowNo, true, true);
                break;
            }
        }
    }

    private void fillGrid(boolean isHistoric) {
        SerializedDBDataObject selectedDataObject = getSelectedElement();

        // Spalten-Filter merken
        Map<Integer, Object> columnFilterValuesMap = new HashMap<>(); // Map von Spalten-Indizes auf Filtertwert-Objekte
        AbstractGuiTableColumnFilterFactory storedFilterFactory = grid.storeFilterFactory(columnFilterValuesMap);
        int sortColumn = grid.getTable().getSortColumn();
        boolean isSortAscending = grid.getTable().isSortAscending();

        // Danach alle Filter deaktivieren und sämtliche Inhalte löschen
        grid.getTable().clearAllFilterValues();
        clearGrid();
        clearExtendedGrid();
        for (iPartsAuthorOrderId authorOrderId : authorOrderMap.keySet()) {
            iPartsDataChangeSetList changeSetList = getChangeSetListFromAuthorOrderId(authorOrderId);
            if (isHistoric) {
                fillHistoricAttributes(authorOrderId, changeSetList);
            } else {
                fillAttributes(authorOrderId, changeSetList);
            }
        }

        // Gemerkte Spalten-Filter wieder aktivieren
        if (!columnFilterValuesMap.isEmpty()) {
            grid.restoreFilterFactory(storedFilterFactory, columnFilterValuesMap);
        }

        // Sortierung wiederherstellen, falls vorher sortiert war
        if (grid.getTable().isSortEnabled() && (sortColumn >= 0)) {
            grid.getTable().sortRowsAccordingToColumn(sortColumn, isSortAscending);
        } else {
            sortGrid();
        }

        showNoResultLabelGrid();
        setSelectedElement(selectedDataObject);
        showNoResultLabelExtendedGrid();

        // Selektives Undo nur ohne Historie anzeigen
        grid.toolbarHelper.setToolbarButtonAndMenuVisible(EditToolbarButtonAlias.EDIT_AO_PARTIAL_UNDO, grid.getTable().getContextMenu(),
                                                          !isHistoric);
    }

    private void sortTableByCol(ChangeSetHistoryGridForm grid, String fieldName) {
        grid.sortTableByCol(fieldName);
    }

    private void onShowHistoryChanged(Event event) {
        updateDataGrid();
    }

    private void updateDataGrid() {
        if (mainWindow.checkboxHistory.isSelected()) {
            fillHistoricDataGrid();
        } else {
            fillDataGrid();
        }
    }

    private void onLayoutChanged(Event event) {
        boolean isSelected = mainWindow.checkboxLayoutChange.isSelected();
        if (isSelected != useAlternateLayout) {
            fillGrid(false);
            useAlternateLayout = isSelected;
            onShowHistoryChanged(event);
        }
    }

    public void showDetails(boolean showDetails) {
        mainWindow.dockingpanel.setShowing(showDetails);
    }


    private class ChangeSetData {

        String commitDate;
        iPartsChangeSetSource source;
        String authorOrderId;
        iPartsDataChangeSetEntryList changeSetEntries;
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelExtendedGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelExtra;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxHistory;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxLayoutChange;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            panelMain.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitpane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpane.setName("splitpane");
            splitpane.__internal_setGenerationDpi(96);
            splitpane.registerTranslationHandler(translationHandler);
            splitpane.setScaleForResolution(true);
            splitpane.setMinimumWidth(10);
            splitpane.setMinimumHeight(10);
            splitpane.setHorizontal(false);
            splitpane.setDividerPosition(344);
            splitpane_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_firstChild.setName("splitpane_firstChild");
            splitpane_firstChild.__internal_setGenerationDpi(96);
            splitpane_firstChild.registerTranslationHandler(translationHandler);
            splitpane_firstChild.setScaleForResolution(true);
            splitpane_firstChild.setMinimumWidth(0);
            splitpane_firstChild.setMinimumHeight(0);
            splitpane_firstChild.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_firstChildLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_firstChild.setLayout(splitpane_firstChildLayout);
            splitpane.addChild(splitpane_firstChild);
            dockingpanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanel.setName("dockingpanel");
            dockingpanel.__internal_setGenerationDpi(96);
            dockingpanel.registerTranslationHandler(translationHandler);
            dockingpanel.setScaleForResolution(true);
            dockingpanel.setMinimumWidth(196);
            dockingpanel.setMinimumHeight(19);
            dockingpanel.setTextHide("!!Änderungsdetails ausblenden");
            dockingpanel.setTextShow("!!Anzeige Änderungsdetails");
            dockingpanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            dockingpanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            dockingpanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            dockingpanel.setButtonFill(true);
            panelExtendedGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelExtendedGrid.setName("panelExtendedGrid");
            panelExtendedGrid.__internal_setGenerationDpi(96);
            panelExtendedGrid.registerTranslationHandler(translationHandler);
            panelExtendedGrid.setScaleForResolution(true);
            panelExtendedGrid.setMinimumWidth(10);
            panelExtendedGrid.setMinimumHeight(0);
            panelExtendedGrid.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelExtendedGridLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelExtendedGrid.setLayout(panelExtendedGridLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelExtendedGridConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelExtendedGrid.setConstraints(panelExtendedGridConstraints);
            dockingpanel.addChild(panelExtendedGrid);
            splitpane.addChild(dockingpanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpane.setConstraints(splitpaneConstraints);
            panelMain.addChild(splitpane);
            panelExtra = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelExtra.setName("panelExtra");
            panelExtra.__internal_setGenerationDpi(96);
            panelExtra.registerTranslationHandler(translationHandler);
            panelExtra.setScaleForResolution(true);
            panelExtra.setMinimumWidth(10);
            panelExtra.setMinimumHeight(10);
            panelExtra.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelExtraLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelExtra.setLayout(panelExtraLayout);
            checkboxHistory = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxHistory.setName("checkboxHistory");
            checkboxHistory.__internal_setGenerationDpi(96);
            checkboxHistory.registerTranslationHandler(translationHandler);
            checkboxHistory.setScaleForResolution(true);
            checkboxHistory.setMinimumWidth(10);
            checkboxHistory.setMinimumHeight(10);
            checkboxHistory.setText("!!Änderungs-Historie anzeigen");
            checkboxHistory.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onShowHistoryChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder checkboxHistoryConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            checkboxHistoryConstraints.setPosition("west");
            checkboxHistory.setConstraints(checkboxHistoryConstraints);
            panelExtra.addChild(checkboxHistory);
            checkboxLayoutChange = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxLayoutChange.setName("checkboxLayoutChange");
            checkboxLayoutChange.__internal_setGenerationDpi(96);
            checkboxLayoutChange.registerTranslationHandler(translationHandler);
            checkboxLayoutChange.setScaleForResolution(true);
            checkboxLayoutChange.setMinimumWidth(10);
            checkboxLayoutChange.setMinimumHeight(10);
            checkboxLayoutChange.setText("!!Alternative Auswertung");
            checkboxLayoutChange.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onLayoutChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder checkboxLayoutChangeConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            checkboxLayoutChangeConstraints.setPosition("east");
            checkboxLayoutChange.setConstraints(checkboxLayoutChangeConstraints);
            panelExtra.addChild(checkboxLayoutChange);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelExtraConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelExtraConstraints.setPosition("south");
            panelExtra.setConstraints(panelExtraConstraints);
            panelMain.addChild(panelExtra);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}