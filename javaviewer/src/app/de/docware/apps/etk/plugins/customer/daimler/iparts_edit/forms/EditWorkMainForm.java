/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChangeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReportConstNodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ldap.LDAPHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.AuthorOrderChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnActivateAuthorOrderEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.AuthorOrderExImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsAuthorOrderAndPicLoader;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.iPartsEditAssemblyListValidationOverlappingEntriesForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.config.db.datatypes.DatatypeUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.db.serialization.*;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.menu.GuiRadioMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.framework.utils.forms.CopyTextWindow;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class EditWorkMainForm extends AbstractJavaViewerMainFormContainer implements iPartsConst {

    private static final String MENU_ITEM_SPECIAL = "SPECIAL";
    private static final FILTER_TYPE DEFAULT_FILTER_TYPE = FILTER_TYPE.ACTIVE_OWN;
    private static final String SESSION_KEY_AUTHOR_ORDER_FILTER_DATE = "sessionKeyAuthorOrderFilterDate";

    private enum FILTER_TYPE {
        ALL("!!Alle Aufträge", false),
        ALL_NOT_MINE("!!Alle Aufträge außer selbsterzeugte", false),
        ACTIVE_OWN("!!Nur aktive eigene Aufträge", false),
        ACTIVE_OWN_USER_GROUPS("!!Nur aktive Aufträge für meine Benutzergruppen", false),
        MINE("!!Nur selbst erzeugte Aufträge", false),
        DELEGATE("!!Nur selbst erzeugte zugewiesene Aufträge", false),
        ONLY_QA("!!Nur QA Aufträge", false),
        ONLY_MY_QA("!!Nur eigene QA Aufträge", false),
        ALL_APPROVED("!!Alle freigegebenen Aufträge", true),
        NONE("", false);

        private String visText;
        private boolean showApprovalDate;

        FILTER_TYPE(String visText, boolean showApprovalDate) {
            this.visText = visText;
            this.showApprovalDate = showApprovalDate;
        }

        public String getVisText() {
            return visText;
        }
    }

    private EditDataObjectFilterGrid grid;
    private boolean isEditAllowed;
    private boolean isInit;
    private boolean reloadHeader;
    private FILTER_TYPE filterType;
    GuiPanel filterPanel;
    GuiPanel filterDatePanel;
    private GuiComboBox<FILTER_TYPE> filterCombobox;
    private GuiMenuItem filterMenu;
    private GuiMenuItem statusMenu;
    private GuiLabel statusLabel;
    private GuiSeparator statusSeparator;
    private ToolbarButtonMenuHelper.ToolbarMenuHolder workHolder;
    private GuiMenuItem menuItemSupplyAOToBST;
    private GuiMenuItem menuExportAuthorOrder;
    private EditChangeSetViewerForm viewerForm;
    private EditConfirmChangesViewerForm confirmForm;
    private boolean isAOActivatable;
    private iPartsDataAuthorOrder activeAuthorOrder;

    /**
     * Erzeugt eine Instanz von EditWorkMainForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditWorkMainForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        isEditAllowed = iPartsRight.EDIT_PARTS_DATA.checkRightInSession();
        isInit = false;
        filterType = DEFAULT_FILTER_TYPE;
        isAOActivatable = false;
        filterPanel = null;
        filterDatePanel = null;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        grid = new EditDataObjectFilterGrid(getConnector(), this) {

            private boolean isLoaded = false;
            private Set<String> activeAuthorOrderGUIDsSet = new HashSet<>();

            {
                setColumnFilterFactory(new VirtualUserGroupDataObjectColumnFilterFactory(getProject(), iPartsDataAuthorOrder.class,
                                                                                         new String[]{ FIELD_DAO_CREATOR_GRP_ID,
                                                                                                       FIELD_DAO_CURRENT_GRP_ID },
                                                                                         null, null, null));
            }

            @Override
            protected void postCreateGui() {
                super.postCreateGui();
                // Vorbereitung für die verschiedenen Filter-Controls
                addBasisFilterPanel();
                addFilterComboboxDisplay();
                if (filterType == FILTER_TYPE.ALL_APPROVED) {
                    addFilterDatePanel();
                }

                // für Sortierung aufheben
                LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
                sortFields.put(TableAndFieldName.make(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CREATION_DATE), true);
                installDefaultResetSortEvent(sortFields);
            }

            @Override
            public void clearGrid() {
                isLoaded = false;
                activeAuthorOrderGUIDsSet.clear();
                super.clearGrid();
            }

            protected boolean isActive(String authorOrderGUID) {
                if (!isLoaded) {
                    activeAuthorOrderGUIDsSet.clear();
                    EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
                    if (revisionsHelper != null) {
                        Collection<AbstractRevisionChangeSet> activeChangeSets = revisionsHelper.getActiveRevisionChangeSets();
                        for (AbstractRevisionChangeSet activeChangeSet : activeChangeSets) {
                            iPartsDataAuthorOrder dataAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByChangeSet(activeChangeSet);
                            if (dataAuthorOrder != null) {
                                activeAuthorOrderGUIDsSet.add(dataAuthorOrder.getAsId().getAuthorGuid());
                            }
                        }
                    }
                    isLoaded = true;
                }
                return activeAuthorOrderGUIDsSet.contains(authorOrderGUID);
            }

            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (tableName.equals(TABLE_DA_AUTHOR_ORDER)) {
                    if (fieldName.equals(FIELD_DAO_CHANGE_SET_ID)) {
                        String changeSetId = objectForTable.getFieldValue(fieldName, getProject().getDBLanguage(), true);
                        if (!changeSetId.isEmpty()) {
                            return DatatypeUtils.makeImgTag(DatatypeUtils.getImageForValue(4, true), true);
                        }
                        return "";
                    } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DAO_ACTIVE)) {
                        if (isActive(objectForTable.getFieldValue(FIELD_DAO_GUID, getProject().getDBLanguage(), true))) {
                            return DatatypeUtils.makeImgTag(DatatypeUtils.getImageForValue(4, true), true);
                        }
                        return "";
                    } else if (fieldName.equals(FIELD_DAO_CREATOR_GRP_ID) || fieldName.equals(FIELD_DAO_CURRENT_GRP_ID)) {
                        String virtualUserGroupId = objectForTable.getFieldValue(fieldName);
                        return iPartsVirtualUserGroup.getVirtualUserGroupName(virtualUserGroupId);
                    }
//                    else if (fieldName.equals(FIELD_DAO_CREATION_USER_ID) || fieldName.equals(FIELD_DAO_CURRENT_USER_ID)) {
//                        return UserAdminUserCache.getUserFullNameByUserName(objectForTable.getFieldValue(fieldName), getProject().getDBLanguage());
//                    }
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                boolean isAuthOrderActive = false;
                if (isSingleSelection()) {
                    GuiTableRow row = getTable().getSelectedRow();
                    if (row instanceof GuiTableRowWithObjects) {
                        EtkDataObject dataObject = ((GuiTableRowWithObjects)row).getObjectForTable(TABLE_DA_AUTHOR_ORDER);
                        if (dataObject != null) {
                            isAuthOrderActive = isActive(dataObject.getFieldValue(FIELD_DAO_GUID, getProject().getDBLanguage(), true));
                        }
                    }
                }
                doEnableButtons();
                fillViewerForm(isAuthOrderActive);
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                if (isSingleSelection()) {
                    if (getToolbarHelper().isToolbarButtonVisibleAndEnabled(EditToolbarButtonAlias.EDIT_AO_ACTIVATE)) {
                        doActivateAuthorOrder();
                    } else if (getToolbarHelper().isToolbarButtonVisibleAndEnabled(EditToolbarButtonAlias.EDIT_AO_DEACTIVATE)) {
                        doDeactivateAuthorOrder();
                    }
                }
            }

            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
                GuiToolButton guiToolButton = getToolbarHelper().addToolbarButton(EditToolbarButtonAlias.IMG_REFRESH, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doRefreshButtonAction(event);
                    }
                });
                guiToolButton.setTooltip("!!Aufträge aktualisieren");

                ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
                boolean isEditPartsDataAllowed = iPartsRight.EDIT_PARTS_DATA.checkRightInSession();
                if (isEditPartsDataAllowed) {
                    holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_NEW, getUITranslationHandler(), new MenuRunnable() {
                        public void run(Event event) {
                            doNewAuthorOrder(event);
                        }
                    });
                    getContextMenu().addChild(holder.menuItem);
                }

                workHolder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doEditAuthorOrder(event);
                    }
                });
                getContextMenu().addChild(workHolder.menuItem);

                if (isEditPartsDataAllowed) {
                    holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                        @Override
                        public void run(Event event) {
                            doDeleteAuthorOrder(event);
                        }
                    });
                    getContextMenu().addChild(holder.menuItem);

                    holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_ORDER_ASSIGN, EditWorkMainForm.this.getUITranslationHandler(), new MenuRunnable() {
                        @Override
                        public void run(Event event) {
                            doAssignAuthorOrder(event);
                        }
                    });
                    getContextMenu().addChild(holder.menuItem);
                }

                ToolbarButtonMenuHelper.ToolbarMenuHolder menuHolder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_AO_ACTIVATE, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doActivateAuthorOrder();
                    }
                });
                getContextMenu().addChild(menuHolder.menuItem);

                menuHolder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_AO_DEACTIVATE, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doDeactivateAuthorOrder();
                    }
                });
                getContextMenu().addChild(menuHolder.menuItem);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                boolean isEditPartsDataAllowed = iPartsRight.EDIT_PARTS_DATA.checkRightInSession();
                if (isEditPartsDataAllowed) {
                    contextMenu.addChild(getToolbarHelper().createMenuSeparator("statusSeparator", getUITranslationHandler()));
                    statusMenu = getToolbarHelper().createMenuEntry("menuStatus", "!!Status...", null, null, getUITranslationHandler());
                    contextMenu.addChild(statusMenu);

                    if (Constants.DEVELOPMENT) {
                        EventListener menuItemListener = new EventListener(Event.MENU_ITEM_EVENT) {
                            @Override
                            public void fire(Event event) {
                                doDeactivateAuthorOrder();
                                changeStatus(iPartsAuthorOrderStatus.APPROVED, true);
                            }
                        };
                        GuiMenuItem menuItem = getToolbarHelper().createMenuEntry("menuCommitImmediately", "!!Sofort abschließen",
                                                                                  DefaultImages.save.getImage(), menuItemListener,
                                                                                  getUITranslationHandler());
                        menuItem.setUserObject(MENU_ITEM_SPECIAL);
                        contextMenu.addChild(menuItem);
                    }
                }

                contextMenu.addChild(getToolbarHelper().createMenuSeparator("filterSeparator", getUITranslationHandler()));
                filterMenu = getToolbarHelper().createMenuEntry("menuFilter", "!!Ansicht...", null, null, getUITranslationHandler());
                contextMenu.addChild(filterMenu);
                for (FILTER_TYPE filterType : FILTER_TYPE.values()) {
                    if (filterType == FILTER_TYPE.NONE) {
                        continue;
                    }
                    final GuiMenuItem menuItem = createMenuEntry("menu" + filterType.name(), filterType.getVisText(), null, null, getUITranslationHandler(), false);
                    menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            changeFilterType((FILTER_TYPE)menuItem.getUserObject(), true);
                        }
                    });
                    menuItem.setUserObject(filterType);
                    filterMenu.addChild(menuItem);
                }

                contextMenu.addChild(getToolbarHelper().createMenuSeparator("showChangesSeparator", getUITranslationHandler()));
                EventListener menuItemListener = new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        showChangeLogInCopyWindow(true);
                    }
                };
                GuiMenuItem menuItem = getToolbarHelper().createMenuEntry("menuShowChanges", "!!Zusammengeführte Änderungen", null, menuItemListener, getUITranslationHandler());
                menuItem.setUserObject(MENU_ITEM_SPECIAL);
                contextMenu.addChild(menuItem);

                ToolbarButtonMenuHelper.ToolbarMenuHolder holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_AO_COMPLETE_HISTORY,
                                                                                                                    EditWorkMainForm.this.getUITranslationHandler(), new MenuRunnable() {
                            @Override
                            public void run(Event event) {
                                showChangeLogInCopyWindow(false);
                            }
                        });
                getContextMenu().addChild(holder.menuItem);

                if (isEditPartsDataAllowed) {
                    holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_AO_UNDO,
                                                                              EditWorkMainForm.this.getUITranslationHandler(), new MenuRunnable() {
                                @Override
                                public void run(Event event) {
                                    deleteChangeSet();
                                }
                            });
                    getContextMenu().addChild(holder.menuItem);
                }

                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_AO_STATUS_HISTORY,
                                                                          EditWorkMainForm.this.getUITranslationHandler(), new MenuRunnable() {
                            @Override
                            public void run(Event event) {
                                showHistory(event);
                            }
                        });
                getContextMenu().addChild(holder.menuItem);

                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_PRE_RELEASE_CHECK,
                                                                          EditWorkMainForm.this.getUITranslationHandler(), new MenuRunnable() {
                            @Override
                            public void run(Event event) {
                                if (!iPartsEditPlugin.startEditing()) {
                                    return;
                                }
                                try {
                                    Map<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> validationForms =
                                            iPartsEditValidationHelper.executePreReleaseCheckMulti(getSelectedDataObject(), getConnector(), false);

                                    if (validationForms != null) {
                                        iPartsAuthorOrderPreReleaseCheckResults.showPrereleaseCheckResults(getConnector(), EditWorkMainForm.this,
                                                                                                           validationForms);

                                        // zuvor erzeugte Forms wieder aufräumen
                                        for (iPartsEditAssemblyListValidationOverlappingEntriesForm value : validationForms.values()) {
                                            value.dispose();
                                        }
                                    }
                                } finally {
                                    iPartsEditPlugin.stopEditing();
                                }
                            }
                        });
                getContextMenu().addChild(holder.menuItem);

                if (iPartsRight.SUPPLY_AUTHOR_ORDER_TO_BST.checkRightInSession()) {
                    menuItemListener = new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            supplyAuthorOrdersToBST(event);
                        }
                    };
                    menuItemSupplyAOToBST = getToolbarHelper().createMenuEntry("menuSupplyAOToBST", "!!Autoren-Auftrag an BST versorgen",
                                                                               null, menuItemListener, getUITranslationHandler());
                    contextMenu.addChild(menuItemSupplyAOToBST);
                }
                // Menüpunkt zum Exportieren von abrechnungsrelevanten Objektinformationen
                if (iPartsRight.EDIT_PARTS_DATA.checkRightInSession()) {
                    menuItemListener = new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            exportAuthorOrder(event);
                        }
                    };
                    menuExportAuthorOrder = getToolbarHelper().createMenuEntry("menuExportAuthorOrder", "!!Objektinformationen exportieren",
                                                                               null, menuItemListener, getUITranslationHandler());
                    contextMenu.addChild(menuExportAuthorOrder);
                }


                if (iPartsRight.EXPORT_IMPORT_AUTHOR_ORDER.checkRightInSession()) {
                    contextMenu.addChild(getToolbarHelper().createMenuSeparator("aoExportImportSeparator", getUITranslationHandler()));

                    menuItem = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.EDIT_AO_EXPORT,
                                                                         EditWorkMainForm.this.getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
                                @Override
                                public void fire(Event event) {
                                    exportAuthorOrderSet(event);
                                }
                            });
                    getContextMenu().addChild(menuItem);

                    menuItem = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.EDIT_AO_IMPORT,
                                                                         EditWorkMainForm.this.getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
                                @Override
                                public void fire(Event event) {
                                    importAuthorOrderSet(event);
                                }
                            });
                    getContextMenu().addChild(menuItem);
                }
            }

            /**
             * Das Basis-Panel für die Filter Controller
             */
            private void addBasisFilterPanel() {
                GuiPanel toolbarPanel = new GuiPanel();
                toolbarPanel.setName("toolbarPanel");
                toolbarPanel.__internal_setGenerationDpi(96);
                toolbarPanel.registerTranslationHandler(getUITranslationHandler());
                toolbarPanel.setScaleForResolution(true);
                toolbarPanel.setMinimumWidth(10);
                toolbarPanel.setMinimumHeight(10);
                LayoutBorder centerPanelLayout = new LayoutBorder();
                toolbarPanel.setLayout(centerPanelLayout);
                AbstractConstraints constraints = getToolBar().getConstraints();
                AbstractGuiControl parent = getToolBar().getParent();
                getToolBar().removeFromParent();
                toolbarPanel.setConstraints(constraints);
                parent.addChild(toolbarPanel);
                toolbarPanel.addChildBorderCenter(getToolBar());

                // Auf diesem Panel liegt das Panel für die Datum-Controls und das Panel für die ComboBox
                filterPanel = new GuiPanel();
                filterPanel.setName("filterPanel");
                filterPanel.__internal_setGenerationDpi(96);
                filterPanel.registerTranslationHandler(getUITranslationHandler());
                filterPanel.setScaleForResolution(true);
                filterPanel.setMinimumWidth(10);
                filterPanel.setMinimumHeight(10);
                filterPanel.setLayout(new LayoutBorder());

                filterPanel.setPaddingTop(4);
                filterPanel.setPaddingBottom(4);
                toolbarPanel.addChildBorderEast(filterPanel);
            }

            /**
             * Panel mit der Ansicht-Filter Combobox
             */
            private void addFilterComboboxDisplay() {
                // Auf diesem Panel liegt das Ansicht Label und die Ansicht ComboBox
                GuiPanel filterComboBoxPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
                filterComboBoxPanel.setName("filterComboBoxPanel");
                filterComboBoxPanel.__internal_setGenerationDpi(96);
                filterComboBoxPanel.registerTranslationHandler(getUITranslationHandler());
                filterComboBoxPanel.setScaleForResolution(true);
                filterComboBoxPanel.setMinimumWidth(10);
                filterComboBoxPanel.setMinimumHeight(10);
                LayoutBorder filterPanelLayout = new LayoutBorder();
                filterComboBoxPanel.setLayout(filterPanelLayout);

                filterComboBoxPanel.setPaddingTop(4);
                filterComboBoxPanel.setPaddingBottom(4);
                filterPanel.addChildBorderEast(filterComboBoxPanel);

                // Ansicht Label
                GuiLabel filterLabel = new GuiLabel();
                filterLabel.setName("filterLabel");
                filterLabel.__internal_setGenerationDpi(96);
                filterLabel.registerTranslationHandler(getUITranslationHandler());
                filterLabel.setScaleForResolution(true);
                filterLabel.setMinimumWidth(10);
                filterLabel.setMinimumHeight(10);
                filterLabel.setText("!!Ansicht");
                filterLabel.setPaddingTop(DWLayoutManager.get().isResponsiveMode() ? 11 : 4);
                filterLabel.setPaddingRight(4);
                filterComboBoxPanel.addChildBorderWest(filterLabel);

                // Ansicht ComboBox
                filterCombobox = new GuiComboBox<>(GuiComboBox.Mode.STANDARD);
                filterCombobox.setMaximumRowCount(20);
                filterCombobox.setName("filterCombobox");
                filterCombobox.__internal_setGenerationDpi(96);
                filterCombobox.registerTranslationHandler(getUITranslationHandler());
                filterCombobox.setScaleForResolution(true);
                filterCombobox.setMinimumWidth(70);
                filterCombobox.setMinimumHeight(10);

                fillFilterComboBox();

                filterCombobox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                    public void fire(Event event) {
                        changeFilterType(filterCombobox.getSelectedUserObject(), false);
                    }
                });

                filterComboBoxPanel.addChildBorderCenter(filterCombobox);
            }
        };

        grid.setDisplayFields(buildDisplayFields());
        grid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.splitpaneAuthorOrder_firstChild.addChild(grid.getGui());

        mainWindow.splitpaneAuthorOrder.addEventListener(new EventListener(Event.ON_RESIZE_EVENT) {
            @Override
            public void fire(Event event) {
                mainWindow.splitpaneAuthorOrder.setDividerPosition(event.getIntParameter(Event.EVENT_PARAMETER_NEWWIDTH) * 2 / 3);

            }
        });

        mainWindow.dockingpanelExtra.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.dockingpanelExtra) {
            @Override
            public boolean isFireOnceValid(Event event) {
                return mainWindow.dockingpanelExtra.isSplitPaneSizeValid();
            }

            @Override
            public void fireOnce(Event event) {
                //mainWindow.dockingpanelExtra.setShowing(false);
            }
        });

        viewerForm = new EditChangeSetViewerForm(getConnector(), this);
        ConstraintsBorder constraints = new ConstraintsBorder();
        viewerForm.getGui().setConstraints(constraints);
        mainWindow.tabbedpaneentryInfoContent.addChild(viewerForm.getGui());
        OnActivateAuthorOrderEvent activateAuthorOrderEvent = new OnActivateAuthorOrderEvent() {
            @Override
            public boolean onActivateAuthorOrder() {
                return doActivateAuthorOrder();
            }
        };
        viewerForm.setOnActivateAuthorOrder(activateAuthorOrderEvent);

        confirmForm = new EditConfirmChangesViewerForm(getConnector(), this);
        constraints = new ConstraintsBorder();
        confirmForm.getGui().setConstraints(constraints);
        mainWindow.splitpaneInfo_secondChild.addChild(confirmForm.getGui());
        confirmForm.setOnActivateAuthorOrder(activateAuthorOrderEvent);
        ThemeManager.get().render(mainWindow);

        //mainWindow.splitpaneInfo_secondChild.setVisible(false);
    }

    /**
     * Filter-DatumsPanel an das Filter-Panel hängen
     * Muss noch erstellt werden, falls es das Panel noch nicht gibt
     */
    private void addFilterDatePanel() {
        if (filterPanel != null) {
            if (filterDatePanel == null) {
                OnChangeEvent changeEventForBoth = new OnChangeEvent() {

                    @Override
                    public void onChange() {
                        reloadGrid(true);
                    }
                };

                filterDatePanel = EditFilterDateGuiHelper.createDefaultFilterDatePanelWithSessionValuesForCurrentMonth(SESSION_KEY_AUTHOR_ORDER_FILTER_DATE, changeEventForBoth, changeEventForBoth);
                reloadGrid(true);
            }
            filterPanel.addChildBorderCenter(filterDatePanel);
        }
    }

    private void removeFilterDatePanel() {
        if (filterDatePanel != null) {
            filterDatePanel.removeFromParent();
        }
    }

    /**
     * Exportiert die abrechnungsrelevanten Objektinformationen aus allen selektierten Autorenaufträgen
     *
     * @param event
     */
    private void exportAuthorOrder(Event event) {
        final List<iPartsDataAuthorOrder> selectedAuthorOrders = getSelectedDataObjects();
        if (!selectedAuthorOrders.isEmpty()) {
            BillableAuthorOrderExport authorOrderExport = new BillableAuthorOrderExport(getProject());
            authorOrderExport.exportBillableData(selectedAuthorOrders);
        }
    }

    private void showChangeLogInCopyWindow(boolean merged) {
        iPartsDataAuthorOrder currentAuthorOrder = getSelectedDataObject();
        if (currentAuthorOrder == null) {
            return;
        }
        iPartsRevisionChangeSet changeSet = new iPartsRevisionChangeSet(currentAuthorOrder.getChangeSetId(), getProject());
        String logTitle = "";
        if (!currentAuthorOrder.getAuthorOrderName().trim().isEmpty()) {
            logTitle = TranslationHandler.translate("!!Autoren-Auftrag: %1", currentAuthorOrder.getAuthorOrderName());
        }
        if (merged) {
            String changeSetLog = iPartsRevisionsLogger.createRevisionChangeSetLog(logTitle, changeSet, true, merged, getProject());
            CopyTextWindow copyTextWindow = new CopyTextWindow(changeSetLog);
            copyTextWindow.maximize();
            copyTextWindow.showModal();
        } else {
            EditAuthorOrderView.showAuthorOrderView(getConnector(), this, getSelectedDataObjects());
        }
    }

    /**
     * Löschen eines ChangeSets
     */
    private void deleteChangeSet() {
        String messageTitle = "!!Alle Änderungen rückgängig machen";
        if (getSelectedDataObjects().size() > 1) {
            MessageDialog.show("!!Mehrere Autoren-Aufträge selektiert.", messageTitle);
            return;
        }
        if (!isOrderDelegatedToLoginUser()) {
            MessageDialog.show("!!Der Autoren-Auftrag ist einem anderen Benutzer zur Bearbeitung zugewiesen.", messageTitle);
            return;
        }
        iPartsDataAuthorOrder currentAuthorOrder = getSelectedDataObject();
        if (currentAuthorOrder == null) {
            MessageDialog.show("!!Kein Autoren-Auftrag zur Selektion gefunden.", messageTitle);
            return;
        }
        if (currentAuthorOrder.getChangeSetId().isEmpty()) {
            MessageDialog.show("!!ChangeSet-ID vom Autoren-Auftrag ist leer.", messageTitle);
            return;
        }
        iPartsChangeSetId changeSetId = currentAuthorOrder.getChangeSetId();
        if (!changeSetId.isValidId()) {
            MessageDialog.show("!!ChangeSet-ID vom Autoren-Auftrag ist ungültig.", messageTitle);
            return;
        }
        if (getRevisionsHelper().isRevisionChangeSetActive(currentAuthorOrder.getChangeSetId())) {
            MessageDialog.show("!!Der Autoren-Auftrag darf beim Löschen des Änderungssets nicht aktiv sein.", messageTitle);
            return;
        }

        EtkProject project = getProject();
        iPartsRevisionChangeSet changeSet = new iPartsRevisionChangeSet(changeSetId, project);
        // Gibt es offene Bildaufträge in diesem Changeset?
        Map<IdWithType, String> picOrderWithUnfinishedState = PictureAndTUValidationHelper.getPicOrderFromChangeSetWithUnfinishedState(changeSet, null,
                                                                                                                                       project);
        if (!picOrderWithUnfinishedState.isEmpty()) {
            if (picOrderWithUnfinishedState.size() > 1) {
                MessageDialog.show("!!Im Autoren-Auftrag befinden sich offene Bildaufträge.", messageTitle);
            } else {
                MessageDialog.show("!!Im Autoren-Auftrag befindet sich ein offener Bildauftrag.", messageTitle);
            }
            if (!Constants.DEVELOPMENT) {
                return;
            }
        }

        if (MessageDialog.showYesNo(TranslationHandler.translate("!!Alle Änderungen im Autoren-Auftrag \"%1\" wirklich rückgängig machen?",
                                                                 currentAuthorOrder.getAuthorOrderName()), messageTitle) == ModalResult.YES) {
            iPartsDataChangeSetEntryList dataChangeSetEntryList = iPartsDataChangeSetEntryList.loadChangeSetEntriesForChangeSetAndDataObjectIdWithType(project, changeSetId, null);
            List<iPartsDataChangeSetEntry> entrySetList = new DwList<>(dataChangeSetEntryList.getAsList());
            GenericEtkDataObjectList dataObjectsToBeSaved = new GenericEtkDataObjectList();
            for (iPartsDataChangeSetEntry dataChangeSetEntry : entrySetList) {
                addAdditionalDataObjectsToBeSaved(dataChangeSetEntry, dataObjectsToBeSaved, project);
                dataChangeSetEntryList.delete(dataChangeSetEntry, DBActionOrigin.FROM_EDIT);
            }
            iPartsDataChangeSet dataChangeSet = new iPartsDataChangeSet(project, changeSetId);
            if (dataChangeSet.existsInDB()) {
                dataChangeSet.setStatus(iPartsChangeSetStatus.NEW, DBActionOrigin.FROM_EDIT);
            } else {
                dataChangeSet = null;
            }

            project.getDbLayer().startTransaction();
            try {
                if (dataChangeSet != null) {
                    dataChangeSet.saveToDB();
                }
                if (!dataObjectsToBeSaved.isEmpty()) {
                    dataObjectsToBeSaved.saveToDB(project, false);
                }
                dataChangeSetEntryList.saveToDB(project);
                currentAuthorOrder.refreshDataChangeSet();

                // Alle Berechnungen für die Auswertung von Teilepositionen löschen für das gelöschte ChangeSet
                iPartsDataReportConstNodeList.deleteAllDataForChangesetGuid(project, changeSetId.getGUID());

                // Alle Primärschlüssel-Reservierungen löschen für das gelöschte ChangeSet
                iPartsDataReservedPKList.deletePrimaryKeysForChangeSet(project, changeSetId);

                // Alle Änderungs-Bestätigungen löschen für das gelöschte ChangeSet
                project.getDbLayer().delete(TABLE_DA_CONFIRM_CHANGES, new String[]{ FIELD_DCC_CHANGE_SET_ID },
                                            new String[]{ changeSetId.getGUID() });

                // Bei allen Einträgen in der Tabelle DA_DIALOG_CHANGES mit dieser ChangeSet-ID die ChangeSet-ID löschen,
                // damit diese Status-Änderungen wieder in einem anderen ChangeSet möglich sind
                iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
                dataDIALOGChangeList.searchAndFill(project, TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_CHANGE_SET_GUID },
                                                   new String[]{ changeSetId.getGUID() }, DBDataObjectList.LoadType.COMPLETE,
                                                   DBActionOrigin.FROM_DB);
                if (!dataDIALOGChangeList.isEmpty()) {
                    for (iPartsDataDIALOGChange dataDIALOGChange : dataDIALOGChangeList) {
                        dataDIALOGChange.setFieldValue(FIELD_DDC_CHANGE_SET_GUID, "", DBActionOrigin.FROM_EDIT);
                    }
                    dataDIALOGChangeList.saveToDB(project);
                }

                project.getDbLayer().commit();
                ClearRetailRelevantCachesEvent.invalidateRetailRelevantCaches(false, false);
                MessageDialog.show("!!Alle Änderungen im Autoren-Auftrag wurden rückgängig gemacht.", messageTitle);
                fillViewerForm(false);
            } catch (Exception e) {
                project.getDbLayer().rollback();
                Logger.getLogger().handleRuntimeException(e);
            }
        }
    }

    private static void addAdditionalDataObjectsToBeSaved(iPartsDataChangeSetEntry dataChangeSetEntry, GenericEtkDataObjectList dataObjectsToBeSaved,
                                                          EtkProject project) {
        // Bereits gespeicherte DIALOG-Änderungen zurücknehmen, falls diese zwischenzeitlich nicht erneut geändert wurden
        if (dataChangeSetEntry.getAsId().getDataObjectType().equals(iPartsDialogId.TYPE)) {
            SerializedDBDataObjectAttribute docuRelAttribute = dataChangeSetEntry.getSerializedDBDataObject().getAttribute(FIELD_DD_DOCU_RELEVANT);
            if (docuRelAttribute != null) {
                iPartsDataDialogData dataDialogData = new iPartsDataDialogData(project, dataChangeSetEntry.getIdFromChangeSetEntry(iPartsDialogId.class,
                                                                                                                                   iPartsDialogId.TYPE));
                if (dataDialogData.existsInDB()) {
                    // Aktueller Wert in DA_DIALOG muss dem neuen Wert im SerializedDBDataObjectAttribute entsprechen
                    if (Utils.objectEquals(dataDialogData.getFieldValue(FIELD_DD_DOCU_RELEVANT), docuRelAttribute.getValue())) {
                        String oldValue = docuRelAttribute.getOldValue();
                        if (oldValue != null) {
                            dataDialogData.setFieldValue(FIELD_DD_DOCU_RELEVANT, oldValue, DBActionOrigin.FROM_EDIT);
                            dataObjectsToBeSaved.add(dataDialogData, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }
        }
    }

    private void showHistory(Event event) {
        EditAoHistoryForm.showAOHistory(getConnector(), this, getSelectedDataObject(), true);
    }

    private void exportAuthorOrderSet(Event event) {
        iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
        if (dataAuthorOrder == null) {
            return;
        }
        AuthorOrderExImporter exporter = new AuthorOrderExImporter(getProject());
        if (!exporter.exportCompleteAuthorOrder(dataAuthorOrder)) {
            if (!exporter.getErrorMessage().isEmpty()) {
                MessageDialog.showError(exporter.getErrorMessage());
            }
        }
    }

    private void importAuthorOrderSet(Event event) {
        AuthorOrderExImporter importer = new AuthorOrderExImporter(getProject());
        boolean result = importer.importCompleteAuthorOrder(null);
        if (result) {
            // Save AuthorOrder und ChangeSet
            result = importer.saveCompleteAuthorOrder();
        }
        if (result) {
            reloadGridAndSelectAuthorOrder(importer.getDataAuthorOrder());
        } else {
            if (StrUtils.isValid(importer.getErrorMessage())) {
                MessageDialog.showError(importer.getErrorMessage());
            }
        }
    }

    private void changeFilterType(FILTER_TYPE filterType, boolean fromMenu) {
        this.filterType = filterType;
        if (fromMenu) {
            filterCombobox.switchOffEventListeners();
            filterCombobox.setSelectedUserObject(this.filterType);
            filterCombobox.switchOnEventListeners();
        }
        for (AbstractGuiControl control : filterMenu.getChildren()) {
            if (control instanceof GuiMenuItem) {
                GuiMenuItem menuItem = (GuiMenuItem)control;
                menuItem.switchOffEventListeners();
                menuItem.setEnabled(menuItem.getUserObject() != filterType);
                menuItem.switchOnEventListeners();
            }
        }

        EtkDisplayFields displayFields = grid.getDisplayFields();
        boolean displayFieldsModified = modifyDisplayFields(displayFields);
        if (displayFieldsModified) {
            grid.setDisplayFields(displayFields);
        }

        if (filterType == FILTER_TYPE.ALL_APPROVED) {
            addFilterDatePanel();
        } else {
            removeFilterDatePanel();
        }

        reloadGridThreadSafe(false);
    }

    private void fillFilterComboBox() {
        filterCombobox.switchOffEventListeners();
        for (FILTER_TYPE filterType : FILTER_TYPE.values()) {
            if (filterType == FILTER_TYPE.NONE) {
                continue;
            }
            filterCombobox.addItem(filterType, filterType.getVisText());
        }
        filterCombobox.switchOnEventListeners();
    }

    private void doAssignAuthorOrder(Event event) {
        // Benutzer für Autoren-Auftrag zuweisen + speichern
        iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
        if (dataAuthorOrder != null) {
            // Prüfen, ob ein anderer Benutzer bereits die aktuelle virtuelle Benutzergruppe und/oder aktuellen Benutzer
            // geändert hat und notfalls abbrechen falls der neue Benutzer nicht dem eingeloggten Benutzer entspricht
            iPartsDataAuthorOrder dataAuthorOrderFromDB = new iPartsDataAuthorOrder(getProject(), dataAuthorOrder.getAsId());
            if ((!dataAuthorOrderFromDB.getCurrentUserGroupId().equals(dataAuthorOrder.getCurrentUserGroupId())
                 || !dataAuthorOrderFromDB.getCurrentUser().equals(dataAuthorOrder.getCurrentUser()))
                && !dataAuthorOrderFromDB.getCurrentUser().equals(iPartsDataAuthorOrder.getLoginAcronym())) {
                reloadGrid(true);
                MessageDialog.showWarning("!!Die Zuweisung für diesen Auftrag wurde inzwischen bereits geändert, so dass keine Zuweisung mehr möglich ist.",
                                          "!!Benutzergruppe oder Benutzer auswählen");
                return;
            }

            // Falls der aktuelle Benutzer im Autoren-Auftrag leer ist, den eingeloggten Benutzer vorschlagen
            // Falls der aktuelle Benutzer im Autoren-Auftrag dem eingeloggten Benutzer entspricht die aktuelle virtuelle
            // Benutzergruppe vorschlagen falls vorhanden
            String selectUserId = null;
            iPartsRight right = iPartsRight.ASSIGN_USER_OR_GROUP;
            if (dataAuthorOrder.getCurrentUser().isEmpty()) {
                selectUserId = iPartsUserAdminDb.getLoginUserIdForSession();
            } else if (dataAuthorOrder.getCurrentUser().equals(iPartsDataAuthorOrder.getLoginAcronym())) {
                if (!dataAuthorOrder.getCurrentUserGroupId().isEmpty()) { // Vorhandene virtuelle Benutzergruppe verwenden
                    selectUserId = dataAuthorOrder.getCurrentUserGroupId();
                } else { // Passende virtuelle Bennutzergruppe suchen
                    String loginUserId = iPartsUserAdminDb.getLoginUserIdForSession();
                    if (loginUserId != null) {
                        String orgId = iPartsUserAdminCache.getInstance(loginUserId).getOrgId();
                        iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(orgId);
                        if (orgCache.hasVirtualUserGroups()) {
                            if (dataAuthorOrder.getStatus() == iPartsAuthorOrderStatus.DOCUMENTED) {
                                selectUserId = orgCache.getVirtualUserGroupIdForQualityInspectors();
                            } else {
                                selectUserId = orgCache.getVirtualUserGroupIdForAuthors();
                            }
                        }
                    }
                }
            } else {
                selectUserId = iPartsUserAdminDb.getLoginUserIdForSession();
                if (iPartsRight.FORCE_ASSIGN_USER_OR_GROUP.checkRightInSession()) {
                    right = iPartsRight.FORCE_ASSIGN_USER_OR_GROUP;
                    if (MessageDialog.showYesNo(TranslationHandler.translate("!!Der Autoren-Auftrag \"%1\" ist bereits dem Benutzer \"%2\" zugewiesen."
                                                                             + "%3Bitte sicherstellen, dass dieser Benutzer den Autoren-Auftrag nicht aktiviert hat bevor ein neuer Benutzer oder Benutzergruppe zugewiesen wird."
                                                                             + "%3%3Fortfahren?",
                                                                             dataAuthorOrder.getAuthorOrderName(),
                                                                             dataAuthorOrder.getCurrentUser(), "\n"),
                                                "!!Benutzergruppe oder Benutzer auswählen") != ModalResult.YES) {
                        return;
                    }
                }
            }

            // Nur beim Status DOCUMENTED die Auswahl für Qualitätsprüfer anzeigen, ansonsten für Autoren
            String userIdOrVirtualUserGroupId;
            if (dataAuthorOrder.getStatus() == iPartsAuthorOrderStatus.DOCUMENTED) {
                userIdOrVirtualUserGroupId = SelectUserDialog.selectQAUser(getConnector(), this, selectUserId, right);
            } else {
                userIdOrVirtualUserGroupId = SelectUserDialog.selectAuthorUser(getConnector(), this, selectUserId, right);
            }

            if (userIdOrVirtualUserGroupId != null) {
                // Prüfen, ob ein anderer Benutzer parallel die aktuelle virtuelle Benutzergruppe und/oder aktuellen Benutzer
                // geändert hat und notfalls abbrechen falls der neue Benutzer nicht dem eingeloggten Benutzer entspricht
                dataAuthorOrderFromDB = new iPartsDataAuthorOrder(getProject(), dataAuthorOrder.getAsId());
                if ((!dataAuthorOrderFromDB.getCurrentUserGroupId().equals(dataAuthorOrder.getCurrentUserGroupId())
                     || !dataAuthorOrderFromDB.getCurrentUser().equals(dataAuthorOrder.getCurrentUser()))
                    && !dataAuthorOrderFromDB.getCurrentUser().equals(iPartsDataAuthorOrder.getLoginAcronym())) {
                    reloadGrid(true);
                    MessageDialog.showWarning("!!Die Zuweisung für diesen Auftrag wurde inzwischen bereits geändert, so dass die gewünschte Zuweisung nicht durchgeführt werden konnte.",
                                              "!!Benutzergruppe oder Benutzer auswählen");
                    return;
                }

                // Zuweisung durchführen
                dataAuthorOrder.setCurrentUserIdOrVirtualUserGroupId(userIdOrVirtualUserGroupId, false);

                // Spezialfall für den Start-Zustand
                if (iPartsAuthorOrderStatus.isStartState(dataAuthorOrder.getStatus())) {
                    // Zustand CREATED -> ORDERED
                    // Beim Wechsel vom StartStatus zum nächsten Status darf die ChangeSet-ID nicht leer sein
                    if (dataAuthorOrder.getChangeSetId().isEmpty()) {
                        iPartsChangeSetId changeSetId = new iPartsChangeSetId(StrUtils.makeGUID());
                        dataAuthorOrder.setChangeSetId(changeSetId);
                    }
                    dataAuthorOrder.changeStatus(iPartsAuthorOrderStatus.getNextState(iPartsAuthorOrderStatus.getStartState()));
                    doSaveToDB(dataAuthorOrder);
                    reloadGridSetFilterAndSelectAuthorOrder(true, FILTER_TYPE.ACTIVE_OWN, dataAuthorOrder);
                } else {
                    doSaveToDB(dataAuthorOrder);
                    reloadGridThreadSafe(true);
                }
            }
        }
    }

    private GuiRadioMenuItem createRadioMenuEntry(String name, String text, FrameworkImage image, EventListener listener,
                                                  TranslationHandler translationHandler) {
        return (GuiRadioMenuItem)createMenuEntry(name, text, image, listener, translationHandler, true);
    }

    private GuiMenuItem createMenuEntry(String name, String text, FrameworkImage image, EventListener listener,
                                        TranslationHandler translationHandler, boolean radioMenuItem) {
        GuiMenuItem menuItem;
        if (radioMenuItem) {
            menuItem = new GuiRadioMenuItem();
        } else {
            menuItem = new GuiMenuItem();
        }
        menuItem.setName(name);
        menuItem.__internal_setGenerationDpi(96);
        menuItem.registerTranslationHandler(translationHandler);
        menuItem.setScaleForResolution(true);
        menuItem.setText(text);
        menuItem.setIcon(image);
        if (listener != null) {
            menuItem.addEventListener(listener);
        }
        return menuItem;
    }

    @Override
    public boolean isSecondToolbarVisible() {
        return false;
    }

    @Override
    public void activeFormChanged(AbstractJavaViewerForm newActiveForm, AbstractJavaViewerForm lastActiveForm) {
        // nur beim ersten Aufruf die Job-Logs einlesen (dann ist dateConfig noch null)
        if (newActiveForm == this) {
            if (!isInit) {
                filterCombobox.setSelectedUserObject(this.filterType);
                fillGrid(false);
                isInit = true;
            } else {
                reloadGrid(true, reloadHeader);
                reloadHeader = false;
            }
        }
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (forceUpdateAll || getConnector().isFlagUserInterfaceLanguageChanged() || getConnector().isFlagDatabaseLanguageChanged()
            || getConnector().isFlagDataChanged()) {
            viewerForm.setObjectTableHeader();
            confirmForm.updateTable();
            if (getConnector().getActiveForm() == this) {
                reloadGrid(true, true);
                reloadHeader = false;
            } else {
                reloadHeader = true;
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
//        iPartsJobsManager.getInstance().removeJobsNotifiactionListener(jobsNotificationListener);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public GuiWindow getMainWindow() {
        return mainWindow;
    }

    private GuiTable getTable() {
        return grid.getTable();
    }

    /**
     * Liefert das aktuelle (im Grid ausgewählte) {@link iPartsDataAuthorOrder} Objekt zurück
     *
     * @return
     */
    private iPartsDataAuthorOrder getSelectedDataObject() {
        GuiTableRow selectedRow = getTable().getSelectedRow();
        if ((selectedRow != null) && (selectedRow instanceof DataObjectGrid.GuiTableRowWithObjects)) {
            DataObjectGrid.GuiTableRowWithObjects row = (DataObjectGrid.GuiTableRowWithObjects)selectedRow;
            EtkDataObject dataObject = row.getObjectForTable(TABLE_DA_AUTHOR_ORDER);
            if ((dataObject != null) && (dataObject instanceof iPartsDataAuthorOrder)) {
                return (iPartsDataAuthorOrder)dataObject;
            }
        }
        return null;
    }

    /**
     * Liefert die aktuellen (im Grid ausgewählten) {@link iPartsDataAuthorOrder} Objekte zurück
     *
     * @return
     */
    private List<iPartsDataAuthorOrder> getSelectedDataObjects() {
        return getSelectedDataObjects((iPartsAuthorOrderStatus[])null);
    }

    private List<iPartsDataAuthorOrder> getSelectedDataObjects(iPartsAuthorOrderStatus... validStates) {
        List<iPartsDataAuthorOrder> result = new DwList<>();
        List<GuiTableRow> rowList = getTable().getSelectedRows();
        for (GuiTableRow selectedRow : rowList) {
            if (selectedRow instanceof DataObjectGrid.GuiTableRowWithObjects) {
                DataObjectGrid.GuiTableRowWithObjects row = (DataObjectGrid.GuiTableRowWithObjects)selectedRow;
                EtkDataObject dataObject = row.getObjectForTable(TABLE_DA_AUTHOR_ORDER);
                if (dataObject instanceof iPartsDataAuthorOrder) {
                    iPartsDataAuthorOrder authorOrder = (iPartsDataAuthorOrder)dataObject;
                    if (hasValidAOState(authorOrder, validStates)) {
                        result.add(authorOrder);
                    }
                }
            }
        }
        return result;
    }

    private boolean hasValidAOState(iPartsDataAuthorOrder authorOrder, iPartsAuthorOrderStatus[] validStates) {
        if ((validStates == null) || (validStates.length == 0)) {
            return true;
        }
        for (iPartsAuthorOrderStatus validState : validStates) {
            if (authorOrder.getStatus() == validState) {
                return true;

            }
        }
        return false;
    }

    public void reloadGridAndSelectAuthorOrder(iPartsDataAuthorOrder dataAuthorOrder) {
        reloadGridSetFilterAndSelectAuthorOrder(true, FILTER_TYPE.NONE, dataAuthorOrder);
    }

    private void reloadGridSetFilterAndSelectAuthorOrder(boolean restoreFilter, FILTER_TYPE newFilterType,
                                                         iPartsDataAuthorOrder dataAuthorOrder) {
        if ((newFilterType != FILTER_TYPE.NONE) && (newFilterType != filterType)) {
            changeFilterType(newFilterType, true);
        } else {
            reloadGridThreadSafe(restoreFilter);
        }
        setSelectedAuthorOrder(dataAuthorOrder);
    }

    private void setSelectedAuthorOrder(iPartsDataAuthorOrder dataAuthorOrder) {
        findAndSelectAuthorOrder(dataAuthorOrder);
    }

    private void findAndSelectAuthorOrder(iPartsDataAuthorOrder dataAuthorOrder) {
        if (dataAuthorOrder != null) {
            for (int rowNo = 0; rowNo < getTable().getRowCount(); rowNo++) {
                GuiTableRow currentRow = getTable().getRow(rowNo);
                if ((currentRow != null) && (currentRow instanceof DataObjectGrid.GuiTableRowWithObjects)) {
                    DataObjectGrid.GuiTableRowWithObjects row = (DataObjectGrid.GuiTableRowWithObjects)currentRow;
                    EtkDataObject dataObject = row.getObjectForTable(TABLE_DA_AUTHOR_ORDER);
                    if ((dataObject != null) && (dataObject instanceof iPartsDataAuthorOrder)) {
                        if (dataObject.getAsId().equals(dataAuthorOrder.getAsId())) {
                            getTable().setSelectedRow(rowNo, true);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setSelectedAuthorOrders(List<iPartsDataAuthorOrder> selectedDataAuthors) {
        if ((selectedDataAuthors != null) && !selectedDataAuthors.isEmpty()) {
            List<IdWithType> idList = new DwList<>();
            for (iPartsDataAuthorOrder dataAuthor : selectedDataAuthors) {
                idList.add(dataAuthor.getAsId());
            }
            List<Integer> selectedRowIndices = new DwList<>();
            for (int rowNo = 0; rowNo < getTable().getRowCount(); rowNo++) {
                GuiTableRow currentRow = getTable().getRow(rowNo);
                if ((currentRow != null) && (currentRow instanceof DataObjectGrid.GuiTableRowWithObjects)) {
                    DataObjectGrid.GuiTableRowWithObjects row = (DataObjectGrid.GuiTableRowWithObjects)currentRow;
                    EtkDataObject dataObject = row.getObjectForTable(TABLE_DA_AUTHOR_ORDER);
                    if ((dataObject != null) && (dataObject.getAsId() instanceof iPartsAuthorOrderId)) {
                        if (idList.contains(dataObject.getAsId())) {
                            selectedRowIndices.add(rowNo);
                        }
                    }
                }
            }
            if (!selectedRowIndices.isEmpty()) {
                getTable().setSelectedRows(Utils.toIntArray(selectedRowIndices), true, true);
            }
        }
    }

    private void fillViewerForm(boolean isAuthOrderActive) {
        iPartsDataAuthorOrder currentAuthorOrder = getSelectedDataObject();
        if (!isSingleSelection() || (currentAuthorOrder == null)) {
            viewerForm.fillGrid(new DwList<>(), null, false, isAOActivatable, true); // Info-Tabelle leeren
            confirmForm.setChangeSetId(null, false, isAOActivatable, true);
            return;
        }

        // Nur die relevanten ChangeSetEntries laden
        // ChangeSet-ID muss passen und der DataObject-Typ muss einer der gewünschten Typen sein
        String[][] whereTableAndFields = new String[2][];
        EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 0,
                                            TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_GUID));
        EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 1,
                                            TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                            TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                            TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE));


        String[][] whereValues = new String[2][];
        EtkDataObjectList.addElemsTo2dArray(whereValues, 0, currentAuthorOrder.getChangeSetId().getGUID());
        EtkDataObjectList.addElemsTo2dArray(whereValues, 1, AssemblyId.TYPE, PartId.TYPE, iPartsPicOrderModulesId.TYPE);

        iPartsDataChangeSetEntryList changeSetEntryList = new iPartsDataChangeSetEntryList();
        changeSetEntryList.searchSortAndFillWithJoin(getProject(), null, null, whereTableAndFields, whereValues, false,
                                                     null, null, false, null, false, false, false, null, false);

        List<iPartsChangeSetViewerElem> changeSetViewerElemList = new DwList<>();
        SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);
        for (iPartsDataChangeSetEntry changeSetEntry : changeSetEntryList) {
            // SerializedDBDataObjectHistory aus dem JSON-String der historischen Daten des serialisierten DBDataObjects erzeugen
            String jsonString = changeSetEntry.getHistoryData();
            if (!jsonString.isEmpty()) {
                SerializedDBDataObjectHistory serializedHistory = serializedDbDataObjectAsJSON.getHistoryFromJSON(jsonString);
                if (!serializedHistory.getHistory().isEmpty()) {
                    SerializedDBDataObject mergedObject = serializedHistory.mergeSerializedDBDataObject(true, null);
                    if ((mergedObject != null) && !mergedObject.isRevertedWithoutKeepState()) {
                        changeSetViewerElemList.add(new iPartsChangeSetViewerElem(mergedObject, getProject()));
                    }
                }
            }
        }

        // Sortieren nach Änderungsdatum
        Collections.sort(changeSetViewerElemList, new Comparator<iPartsChangeSetViewerElem>() {
            @Override
            public int compare(iPartsChangeSetViewerElem o1, iPartsChangeSetViewerElem o2) {
                return o2.getDateTime().compareTo(o1.getDateTime());
            }
        });
        viewerForm.fillGrid(changeSetViewerElemList, currentAuthorOrder.getChangeSetId(), isAuthOrderActive, isAOActivatable,
                            iPartsAuthorOrderStatus.isEndState(currentAuthorOrder.getStatus()));
        confirmForm.setChangeSetId(currentAuthorOrder.getChangeSetId(), isAuthOrderActive, isAOActivatable,
                                   iPartsAuthorOrderStatus.isEndState(currentAuthorOrder.getStatus()));
    }

    private boolean isSingleSelection() {
        int selectionRowCount = getTable().getSelectedRows().size();
        return selectionRowCount == 1;
    }

    /**
     * (De-)Aktiviert die einzelnen Buttons
     */
    private void doEnableButtons() {
        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelectionEnabled = selectionRowCount == 1;
        boolean multiSelectionEnabled = selectionRowCount > 0;

        boolean aoActivatable = false;
        boolean aoDeactivatable = false;
        boolean isDeleteAllowed = multiSelectionEnabled;
        boolean isEditOrderAllowed = false;
        boolean isNormalState = false;

        iPartsDataAuthorOrder selectedAuthorOrder = getSelectedDataObject();
        if (multiSelectionEnabled && (selectedAuthorOrder != null)) {
            if (isEditAllowed) {
                isEditOrderAllowed = selectedAuthorOrder.isCurrentUserEqualLoginUserWithFallbackToCreationUser();
            }
            isEditOrderAllowed &= isEditAllowed;
            isNormalState = !iPartsAuthorOrderStatus.isEndState(selectedAuthorOrder.getStatus());

            if (getRevisionsHelper().isRevisionChangeSetActive(selectedAuthorOrder.getChangeSetId())) {
                // nur ein eigener aktiver Auftrag kann deaktiviert werden
                aoDeactivatable = singleSelectionEnabled && isNormalState;
                // ein aktiver Auftrag kann nicht gelöscht werden
                isDeleteAllowed = false;
            } else {
                // ChangeSet ist nicht aktiv => Aktivieren überprüfen
                if (singleSelectionEnabled && (selectedAuthorOrder != null)) {
                    // Check, ob eine ChangeSetId gesetzt ist
                    aoActivatable = selectedAuthorOrder.isActivatable();
                } else {
                    aoActivatable = false;
                }
            }
            if (isDeleteAllowed) {
                EnumSet<iPartsAuthorOrderStatus> allowedStates = iPartsAuthorOrderStatus.getGoToStatesForGivenState(iPartsAuthorOrderStatus.getStartState());
                // Überprüfung bei MultiSelect: sind nur eigene Aufträge selektiert?
                for (iPartsDataAuthorOrder selectedAO : getSelectedDataObjects()) {
                    if (!selectedAO.isCreationUserEqualLoginUser()) {
                        isDeleteAllowed = false;
                        break;
                    } else if (!selectedAO.getCurrentUser().isEmpty() && !selectedAO.isCurrentUserEqualLoginUser()) {
                        isDeleteAllowed = false;
                        break;
                    } else if (!allowedStates.contains(selectedAO.getStatus())) {
                        isDeleteAllowed = false;
                        break;
                    }
                }
            }
        }

        if (isEditAllowed) {
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, true);
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, singleSelectionEnabled);
            changeWorkText(isEditOrderAllowed && isNormalState);
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, isDeleteAllowed);
            if (singleSelectionEnabled && (selectedAuthorOrder != null)) {
                handleStatusSubMenu();
                statusMenu.setEnabled(!aoDeactivatable && isEditOrderAllowed && isNormalState);
                boolean enabled = !aoDeactivatable && isEditAllowed && iPartsAuthorOrderStatus.isAssignUserAllowed(getSelectedOrderStatus())
                                  && selectedAuthorOrder.isAssignAllowedForCurrentUser(iPartsRight.FORCE_ASSIGN_USER_OR_GROUP.checkRightInSession());
                grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ORDER_ASSIGN, enabled);
            } else {
                statusMenu.setEnabled(false);
                grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ORDER_ASSIGN, false);
            }
            if (singleSelectionEnabled) {
                if ((selectedAuthorOrder != null) && !iPartsAuthorOrderStatus.isEndState(selectedAuthorOrder.getStatus())) {
                    grid.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_AO_UNDO);
                } else {
                    grid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_AO_UNDO);
                }
            }
        } else {
            grid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW);
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, singleSelectionEnabled);
            changeWorkText(false);
            grid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE);
            if (statusMenu != null) {
                statusMenu.setVisible(false);
            }
            grid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ORDER_ASSIGN);
        }

        // (De-)Aktivieren
        boolean isReadOnly = false;
        if (singleSelectionEnabled) {
            if (selectedAuthorOrder == null) {
                isReadOnly = true; // vorsichtshalber nur lesend wenn die Dataobjects der Selektion nicht passen
            } else {
                isReadOnly = !selectedAuthorOrder.isCurrentUserEqualLoginUser();
            }
        }
        handleButtonVisibility(EditToolbarButtonAlias.EDIT_AO_DEACTIVATE, aoDeactivatable, isReadOnly);
        if (!aoDeactivatable) {
            // Sollten beide Optionen
            handleButtonVisibility(EditToolbarButtonAlias.EDIT_AO_ACTIVATE, true, isReadOnly);
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_AO_ACTIVATE, aoActivatable);
        } else {
            handleButtonVisibility(EditToolbarButtonAlias.EDIT_AO_ACTIVATE, aoActivatable, isReadOnly);
        }
        isAOActivatable = aoActivatable;

        // Menüeinträge
        enableMenuItems(singleSelectionEnabled);
        grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_AO_COMPLETE_HISTORY, multiSelectionEnabled);

        // ChangeSet löschen kann nur bei inakivem Autoren-Auftrag gestartet werden
        grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_AO_UNDO, singleSelectionEnabled && aoActivatable
                                                                             && !isReadOnly);

        grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_AO_STATUS_HISTORY, singleSelectionEnabled);

        // Freigabevorprüfung kann nur bei akivem schreibenden Autoren-Auftrag gestartet werden
        grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PRE_RELEASE_CHECK, singleSelectionEnabled && aoDeactivatable
                                                                                       && !isReadOnly);

        grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_AO_EXPORT, singleSelectionEnabled);

        checkIfEndStateMenuItemIfActive(menuItemSupplyAOToBST, selectionRowCount);
        checkIfStartStateMenuItemIfActive(menuExportAuthorOrder, selectionRowCount);
    }

    /**
     * Aktiviert oder deaktiviert den übergebenen Menüeintrag abhängig davon, ob mind. ein selektierten Autorenauftrag
     * abgeschlossen ist.
     *
     * @param menuItem
     * @param selectionRowCount
     */
    private void checkIfEndStateMenuItemIfActive(GuiMenuItem menuItem, int selectionRowCount) {
        if (menuItem != null) {
            boolean enabled = false;
            if (selectionRowCount > 0) {
                for (iPartsDataAuthorOrder dataAuthorOrder : getSelectedDataObjects()) {
                    if (iPartsAuthorOrderStatus.isEndState(dataAuthorOrder.getStatus()) && !dataAuthorOrder.getBstId().isEmpty()) {
                        enabled = true;
                        break;
                    }
                }
            }
            menuItem.setEnabled(enabled);
        }
    }

    /**
     * Aktiviert oder deaktiviert den übergebenen Menüeintrag abhängig davon, ob mind. ein selektierten Autorenauftrag
     * erst angelegt wurde.
     *
     * @param menuItem
     * @param selectionRowCount
     */
    private void checkIfStartStateMenuItemIfActive(GuiMenuItem menuItem, int selectionRowCount) {
        if (menuItem != null) {
            boolean enabled = false;
            if (selectionRowCount > 0) {
                for (iPartsDataAuthorOrder dataAuthorOrder : getSelectedDataObjects()) {
                    if (!iPartsAuthorOrderStatus.isStartState(dataAuthorOrder.getStatus()) && !dataAuthorOrder.getBstId().isEmpty()) {
                        enabled = true;
                        break;
                    }
                }
            }
            menuItem.setEnabled(enabled);
        }
    }

    private void enableMenuItems(boolean enabled) {
        for (AbstractGuiControl guiControl : grid.getContextMenu().getChildren()) {
            if (guiControl instanceof GuiMenuItem) {
                GuiMenuItem menuItem = (GuiMenuItem)guiControl;
                if ((menuItem.getUserObject() != null) && menuItem.getUserObject().equals(MENU_ITEM_SPECIAL)) {
                    menuItem.setEnabled(enabled);
                }
            }
        }
    }

    private void enableSpecialMenuItems(boolean enabled, String text) {
        for (AbstractGuiControl guiControl : grid.getContextMenu().getChildren()) {
            if (guiControl instanceof GuiMenuItem) {
                GuiMenuItem menuItem = (GuiMenuItem)guiControl;
                if ((menuItem.getUserObject() != null) && menuItem.getUserObject().equals(MENU_ITEM_SPECIAL) &&
                    menuItem.getText().equals(text)) {
                    menuItem.setEnabled(enabled);
                }
            }
        }
    }

    private void changeWorkText(boolean isEditAllowed) {
        String toolTip = EditToolbarButtonAlias.EDIT_WORK.getTooltip();
        if (!isEditAllowed) {
            toolTip = "!!Anzeigen";
        }
        if (!workHolder.menuItem.getText().equals(toolTip)) {
            workHolder.toolbarButton.setTooltip(toolTip);
            workHolder.menuItem.setText(toolTip);
        }
    }

    /**
     * Verändert die Sichtbarkeit der Buttons, die mit dem übergebnen {@link iPartsToolbarButtonAlias} verknüpft sind
     *
     * @param aoButtonAlias
     * @param visible
     * @param isReadOnly
     */
    private void handleButtonVisibility(iPartsToolbarButtonAlias aoButtonAlias, boolean visible,
                                        boolean isReadOnly) {
        if (visible) {
            grid.showToolbarButtonAndMenu(aoButtonAlias);
            if (isReadOnly) {
                String tooltip = TranslationHandler.translate(aoButtonAlias.getTooltip()) +
                                 " (" + TranslationHandler.translate("!!nur lesend") + ")";
                grid.changeToolbarButtonAndMenuTooltip(aoButtonAlias, tooltip);
            } else {
                grid.resetToolbarButtonAndMenuTooltip(aoButtonAlias);
            }
        } else {
            grid.hideToolbarButtonAndMenu(aoButtonAlias);
        }
    }

    /**
     * Überprüft, ob der ausgewählte Autorenauftrag dem Login-Benutzer gehört
     *
     * @return
     */
    public boolean isOrderFromCurrentAuthor() {
        iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
        if (dataAuthorOrder != null) {
            // nur die eigenen Aufträge können aktiviert werden
            return dataAuthorOrder.isCreationUserEqualLoginUser();
        }
        return false;

    }

    /**
     * Überprüft, ob der ausgewählte Autorenauftrag an den Login-Benutzer delegiert ist
     *
     * @return
     */
    public boolean isOrderDelegatedToLoginUser() {
        iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
        if (dataAuthorOrder != null) {
            // nur die eigenen Aufträge können aktiviert werden
            return dataAuthorOrder.isCurrentUserEqualLoginUser();
        }
        return false;

    }

    public iPartsAuthorOrderStatus getSelectedOrderStatus() {
        iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
        if (dataAuthorOrder != null) {
            return dataAuthorOrder.getStatus();
        }
        return iPartsAuthorOrderStatus.UNKNOWN;

    }

    /**
     * Passt das Statusmenü den aktuell möglichen Statusübergängne an
     */
    private void handleStatusSubMenu() {
        iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
        if ((dataAuthorOrder != null) && dataAuthorOrder.isCurrentUserEqualLoginUserWithFallbackToCreationUser()) {
            statusMenu.switchOffEventListeners();
            statusMenu.removeAllChildren();
            createStatusSubMenus(statusMenu, dataAuthorOrder.getStatus());
            statusMenu.switchOnEventListeners();
            statusMenu.setEnabled(!statusMenu.getChildren().isEmpty());
        } else {
            statusMenu.setEnabled(false);
        }
    }

    /**
     * Erstellt, abhängig vom aktuellen Status, das anzuzeigenden Statusmenü
     *
     * @param statusMenu
     * @param startStatus
     */
    private void createStatusSubMenus(GuiMenuItem statusMenu, iPartsAuthorOrderStatus startStatus) {
        EnumSet<iPartsAuthorOrderStatus> aoStatusSet = iPartsAuthorOrderStatus.getGoToStatesForGivenState(startStatus);
        boolean isWorkingState = iPartsAuthorOrderStatus.isWorkState(startStatus);
        GuiButtonGroup buttonGroup = new GuiButtonGroup();
        TranslationHandler uiTranslationHandler = getUITranslationHandler();
        for (iPartsAuthorOrderStatus status : aoStatusSet) {
            if (status == iPartsAuthorOrderStatus.UNKNOWN) {
                continue;
            }
            final GuiRadioMenuItem menuItem = createRadioMenuEntry("menu" + status.name(), status.getDescription(getProject()), null, null, uiTranslationHandler);
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    changeStatus((iPartsAuthorOrderStatus)menuItem.getUserObject(), false);
                }
            });
            menuItem.setUserObject(status);
            menuItem.setButtonGroup(buttonGroup);
            menuItem.setSelected(status == startStatus);
            if (isWorkingState && iPartsAuthorOrderStatus.isStateBeforeWorkState(status)) {
                iPartsDataAuthorOrder selectedDataObject = getSelectedDataObject();
                if ((selectedDataObject != null) && selectedDataObject.isChangeSetEmpty()) {
                    statusMenu.addChild(menuItem);
                }
            } else {
                statusMenu.addChild(menuItem);
            }
        }

    }

    /**
     * Ändert den Status des aktuellen Autorenauftrags
     *
     * @param aoStatus
     * @param suppressStatusChangeCheck Sollem evtl. Überprüfungen für den Status-Wechsel unterdrückt werden?
     */
    private void changeStatus(iPartsAuthorOrderStatus aoStatus, boolean suppressStatusChangeCheck) {
        // Status bei getSelectedDataObject() setzen + speichern
        FILTER_TYPE newFilter = FILTER_TYPE.NONE;
        final iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
        if (dataAuthorOrder != null) {
            if (aoStatus == iPartsAuthorOrderStatus.getNextState(iPartsAuthorOrderStatus.getStartState())) {
                // beim Wechsel vom StartStatus zum nächsten Status darf die ChangeSet-ID nicht leer sein
                if (dataAuthorOrder.getChangeSetId().isEmpty()) {
                    iPartsChangeSetId changeSetId = new iPartsChangeSetId(StrUtils.makeGUID());
                    dataAuthorOrder.setChangeSetId(changeSetId);
                }
                dataAuthorOrder.setCurrentUser(dataAuthorOrder.getCreationUser(), DBActionOrigin.FROM_EDIT);
                newFilter = FILTER_TYPE.ACTIVE_OWN;
            } else if (iPartsAuthorOrderStatus.isEndState(aoStatus)) {
                if (!doCommitAuthorOrder(dataAuthorOrder, suppressStatusChangeCheck)) {
                    return;
                }
                dataAuthorOrder.setCurrentUserIdOrVirtualUserGroupId("", true);
            }
            if (iPartsAuthorOrderStatus.isChangeToPreviousState(dataAuthorOrder.getStatus(), aoStatus)) {
                // Status soll 'zurückgesetzt' werden
                if (aoStatus == iPartsAuthorOrderStatus.getStartState()) {
                    // zurück auf CREATED
                    dataAuthorOrder.setCurrentUserIdOrVirtualUserGroupId("", true);
                    newFilter = FILTER_TYPE.MINE;
                } else if (iPartsAuthorOrderStatus.isDocumentedState(dataAuthorOrder.getStatus())) {
                    // von QA zurück zum Bearbeiter mit Vorbelegung für die virtuelle Benutzergruppe für Autoren
                    String selectUserId = null;
                    String loginUserId = iPartsUserAdminDb.getLoginUserIdForSession();
                    if (loginUserId != null) {
                        String orgId = iPartsUserAdminCache.getInstance(loginUserId).getOrgId();
                        iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(orgId);
                        if (orgCache.hasVirtualUserGroups()) {
                            selectUserId = orgCache.getVirtualUserGroupIdForAuthors();
                        }
                    }
                    String userIdOrVirtualUserGroupId = SelectUserDialog.selectAuthorUser(getConnector(), this, selectUserId,
                                                                                          iPartsRight.ASSIGN_USER_OR_GROUP);
                    if (userIdOrVirtualUserGroupId == null) {
                        return;
                    }
                    dataAuthorOrder.setCurrentUserIdOrVirtualUserGroupId(userIdOrVirtualUserGroupId, true);
                }
            } else if (iPartsAuthorOrderStatus.isChangeToNextState(dataAuthorOrder.getStatus(), aoStatus)) {
                // Status soll 'vorwärts' gesetzt werden
                if (iPartsAuthorOrderStatus.isDocumentedState(aoStatus)) {
                    if (!checkStatusChangeAllowed(false)) {
                        return;
                    }

                    // Anzeige für User-Delegierung nach QA mit Vorbelegung für die virtuelle Benutzergruppe für Qualitätsprüfer
                    String selectUserId = null;
                    String loginUserId = iPartsUserAdminDb.getLoginUserIdForSession();
                    if (loginUserId != null) {
                        String orgId = iPartsUserAdminCache.getInstance(loginUserId).getOrgId();
                        iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(orgId);
                        if (orgCache.hasVirtualUserGroups()) {
                            selectUserId = orgCache.getVirtualUserGroupIdForQualityInspectors();
                        }
                    }
                    String userIdOrVirtualUserGroupId = SelectUserDialog.selectQAUser(getConnector(), this, selectUserId,
                                                                                      iPartsRight.ASSIGN_USER_OR_GROUP);
                    if (userIdOrVirtualUserGroupId == null) {
                        return;
                    }
                    dataAuthorOrder.setCurrentUserIdOrVirtualUserGroupId(userIdOrVirtualUserGroupId, true);
                }
            }
            dataAuthorOrder.changeStatus(aoStatus);
            doSaveToDB(dataAuthorOrder);
            if (iPartsAuthorOrderStatus.isEndState(dataAuthorOrder.getStatus())) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS, LogType.INFO, "Author order \"" + dataAuthorOrder.getAsId().getAuthorGuid()
                                                                               + "\" with change set \"" + dataAuthorOrder.getChangeSetId().getGUID()
                                                                               + "\" approved by " + iPartsUserAdminDb.getUserNameForLogging(getProject()));
            }
            reloadGridSetFilterAndSelectAuthorOrder(true, newFilter, dataAuthorOrder);

            // Automatische Versorgung nach BST bei Freigabe vom Autoren-Auftrag
            if (iPartsAuthorOrderStatus.isEndState(dataAuthorOrder.getStatus()) && !dataAuthorOrder.getBstId().isEmpty()
                && iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_ACTIVE)) {
                Session.startChildThreadInSession(thread -> {
                    String bstURI = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_URI_SUPPLY_AUTHOR_ORDERS_TO_BST).trim();
                    Logger.log(iPartsEditPlugin.LOG_CHANNEL_SUPPLY_TO_BST, LogType.DEBUG, "Automatically calling BST webservice "
                                                                                          + bstURI + " for supplying released author order \""
                                                                                          + dataAuthorOrder.getAsId().getAuthorGuid()
                                                                                          + "...");
                    SupplyAuthorOrderToBSTHelper supplyAuthorOrderToBSTHelper = new SupplyAuthorOrderToBSTHelper(getProject());
                    if (supplyAuthorOrderToBSTHelper.supplyAuthorOrderToBST(dataAuthorOrder)) {
                        // Wenn der Autoren-Auftrag versorgt wurde, dann muss die Tabelle aktualisiert werden
                        reloadGridThreadSafe(true);
                    }
                });
            }
        }
    }

    private boolean checkStatusChangeAllowed(boolean isFinalReleaseCheck) {
        boolean statusChangeAllowed = false;
        boolean disableAuthorOrder = false;
        Map<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> validationForms = null;
        try {
            MainWindowClass.showWaitCursorForRootWindow(true);
            if (!doActivateAuthorOrder()) {
                MessageDialog.showWarning("!!Autoren-Auftrag konnte nicht aktiviert werden.", "!!Qualitätsprüfungen");
                return false;
            }
            validationForms = iPartsEditValidationHelper.executePreReleaseCheckMulti(getSelectedDataObject(), getConnector(),
                                                                                     isFinalReleaseCheck);
            if (validationForms == null) {
                return false;
            }
            iPartsEditBaseValidationForm.ValidationResult validationResult = iPartsEditValidationHelper.evaluatePreReleaseCheck(validationForms);

            if (validationResult == iPartsEditBaseValidationForm.ValidationResult.OK) {
                // Prüfung war gut, also Statusübergang zulassen
                statusChangeAllowed = true;
            } else {
                // Es gab Fehler oder Warnungen -> also wird der Ergebnisdialog angezeigt
                ModalResult dialogResult = iPartsAuthorOrderPreReleaseCheckResults.showReleaseCheckResults(
                        getConnector(), EditWorkMainForm.this, validationForms, isFinalReleaseCheck);
                if (dialogResult == ModalResult.IGNORE) {
                    // Trotzdem freigeben / weiterleiten
                    statusChangeAllowed = true;
                } else if (dialogResult == ModalResult.ABORT) {
                    // es wurde auf schließen geklickt, also kann später der Auftrag deaktiviert werden
                    disableAuthorOrder = true;
                }
            }
        } finally {
            // zuvor erzeugte Forms wieder aufräumen
            if (validationForms != null) {
                for (iPartsEditAssemblyListValidationOverlappingEntriesForm value : validationForms.values()) {
                    value.dispose();
                }
            }

            // Auftrag nur deaktivieren, wenn es keine Fehler gab oder Schließen geklickt wurde
            // Wenn auf "Alle fehlerhaften TUs öffnen" geklickt wurde, muss der Auftrag aktiv bleiben
            if (disableAuthorOrder || statusChangeAllowed) {
                doDeactivateAuthorOrder();
            }
            MainWindowClass.showWaitCursorForRootWindow(false);
        }

        // nur weiterschieben wenn die Freigabeprüfung passt oder mit entsprechendem Recht auf "trotzdem weitermachen" geklickt wurde
        return statusChangeAllowed;
    }

    private boolean doCommitAuthorOrder(final iPartsDataAuthorOrder dataAuthorOrder, boolean suppressStatusChangeCheck) {
        if (!isOrderDelegatedToLoginUser()) {
            // Sollte nicht passieren
            return false;
        }

        if (!suppressStatusChangeCheck && !checkStatusChangeAllowed(true)) {
            return false;
        }

        if (!iPartsEditPlugin.startEditing()) {
            return false;
        }
        try {
            final iPartsChangeSetId changeSetId = dataAuthorOrder.getChangeSetId();
            if (!changeSetId.isEmpty()) {
                String authorOrderName = dataAuthorOrder.getAuthorOrderName();
                GuiWindow rootParentWindow = getRootParentWindow();
                if (rootParentWindow != null) {
                    Session.get().setCurrentRootWindow(rootParentWindow);
                }
                if (MessageDialog.showYesNo(TranslationHandler.translate("!!Soll der Autoren-Auftrag \"%1\" wirklich abgeschlossen und damit in die Datenbank übernommen werden?",
                                                                         authorOrderName), "!!Autoren-Auftrag abschließen") != ModalResult.YES) {
                    return false;
                }

                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Autoren-Auftrag abschließen", TranslationHandler.translate("!!Übernahme der Änderungen vom Autoren-Auftrag \"%1\"",
                                                                                                                                             authorOrderName), null, true);
                messageLogForm.disableButtons(false);
                messageLogForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        iPartsRevisionChangeSet changeSet = new iPartsRevisionChangeSet(changeSetId, getProject());
                        setProcuctModificationDate(changeSet);
                        addPicOrderDataToChangeSet(getProject(), changeSet);

                        Runnable afterSaveChangeSetRunnable = () -> {
                            dataAuthorOrder.setCommitDateFromChangeset(getProject(), changeSetId);
                            EditModuleHelper.updateAllModuleSAAValiditiesForFilter(getProject(), changeSet);
                        };
                        if (changeSet.commit(true, true, afterSaveChangeSetRunnable)) {
                            deactivateAuthorOrder(dataAuthorOrder);
                            messageLogForm.closeWindow(ModalResult.OK);
                        } else {
                            messageLogForm.closeWindow(ModalResult.CANCEL);
                        }
                    }

                    /**
                     * Fügt die Daten des Bildauftrags, die BST benötigt, hinzu. Die Daten werden mit dem Status
                     * "COMMITTED" hinzugefügt und somit beim Speichern der Änderungen nicht berücksichtigt.
                     *
                     * @param project
                     * @param changeSet
                     */
                    private void addPicOrderDataToChangeSet(EtkProject project, iPartsRevisionChangeSet changeSet) {
                        // Alle Bildauftrag zu Modul Verknüpfungen laden, da diese immer enthalten sind, wenn ein
                        // Bildauftrag angelegt wird.
                        iPartsDataChangeSetEntryList dataChangeSetEntryList = iPartsDataChangeSetEntryList.loadChangeSetEntriesForChangeSetAndIdType(project, changeSet.getChangeSetId(),
                                                                                                                                                     null, iPartsPicOrderModulesId.TYPE);
                        if (dataChangeSetEntryList.isEmpty()) {
                            return;
                        }
                        List<iPartsDataPicOrder> picOrders = new ArrayList<>();
                        for (iPartsDataChangeSetEntry entry : dataChangeSetEntryList) {
                            IdWithType id = entry.getAsId().getDataObjectIdWithType();
                            picOrders.add(new iPartsDataPicOrder(project, new iPartsPicOrderId(id.getValue(1))));
                        }
                        iPartsDataPicOrderList picOrderList = BillableAuthorOrderExport.filterBillablePicOrders(picOrders, true);

                        if (!picOrderList.isEmpty()) {
                            changeSet.addDataObjectList(picOrderList, true, true);
                        }
                    }
                });

                return messageLogForm.getModalResult() == ModalResult.OK;
            } else {
                return true;
            }
        } finally {
            iPartsEditPlugin.stopEditing();
        }
    }

    private void setProcuctModificationDate(final iPartsRevisionChangeSet changeSet) {

        final Set<iPartsProductId> modifiedProducts = new TreeSet<>();

        // Zuerst das zu übernehmende Changeset temporär ausschalten, falls aktiv. Wichtig bei gelöschten Modulen.
        // Diese wären mit aktivem Changeset sonst schon weg und würden nicht gefunden werden.
        // Es kann allerdings trotzdem passieren, dass ein anderer freigegebener Autorenauftrag das Modul schon
        // gelöscht hat. In diesem Fall würde das Produkt einfach nicht bestimmt werden können und kommt nicht ins Set.
        EtkProject project = getProject();
        project.executeWithoutActiveChangeSets((runSession, runProject) -> {
            try {
                Set<AssemblyId> deletedModuleIds = changeSet.getModuleIdsWithStateAnyOf(SerializedDBDataObjectState.DELETED);
                getProductsForAssemblyIds(deletedModuleIds, modifiedProducts, runProject);

                // Dann das zu übernehmende Changeset aktivieren. Wichtig va. bei neuen Modulen.
                // Diese wären mit deaktiviertem Changeset sonst noch nicht da und würden nicht gefunden werden.
                runProject.getRevisionsHelper().setActiveRevisionChangeSet(changeSet, runProject);
                Set<AssemblyId> changedModuleIds = changeSet.getModuleIdsWithStateAnyOf(SerializedDBDataObjectState.NEW,
                                                                                        SerializedDBDataObjectState.REPLACED,
                                                                                        SerializedDBDataObjectState.MODIFIED);
                getProductsForAssemblyIds(changedModuleIds, modifiedProducts, runProject);
            } finally {
                runProject.getRevisionsHelper().removeActiveRevisionChangeSet(changeSet.getChangeSetId(), runProject);
            }
            for (iPartsProductId productId : modifiedProducts) {
                // Hier das "echte" EtkProject verwenden, weil das veränderte iPartsDataProduct über das ChangeSet auch
                // nach dem executeWithoutActiveChangeSets() noch verwendet wird
                iPartsDataProduct product = new iPartsDataProduct(project, productId);
                if (product.existsInDB()) {
                    product.refreshModificationTimeStamp();
                    changeSet.addDataObject(product);
                }
            }
            return true;
        });
    }

    private void getProductsForAssemblyIds(Set<AssemblyId> moduleIds, Set<iPartsProductId> products, EtkProject project) {
        for (AssemblyId assemblyId : moduleIds) {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                iPartsProductId productId = iPartsAssembly.getProductIdFromModuleUsage();
                if (productId != null) {
                    products.add(productId);
                }
            }
        }
    }

    private void clearGridAndFilter(boolean withHeader) {
        grid.getTable().clearAllFilterValues();
        grid.clearFilteredColumns();
        grid.updateFilters();
        grid.clearGrid();
        if (withHeader) {
            EtkDisplayFields displayFields = grid.getDisplayFields();
            modifyDisplayFields(displayFields);
            grid.setDisplayFields(displayFields);
        }

    }

    private void fillGrid(boolean clearHeader) {
        clearGridAndFilter(clearHeader);
        iPartsDataAuthorOrderList aoList;
        String loginUserName = iPartsDataAuthorOrder.getLoginAcronym();
        EtkProject project = getProject();
        switch (filterType) {
            case ALL:
                aoList = iPartsAuthorOrderAndPicLoader.loadAuthorOrderListAll(project, loginUserName);
                break;
            case ALL_NOT_MINE:
                aoList = iPartsAuthorOrderAndPicLoader.loadAuthorOrderListAllExceptUser(project, loginUserName);
                break;
            case ACTIVE_OWN:
                aoList = iPartsAuthorOrderAndPicLoader.loadAuthorOrderListActiveOwn(project, loginUserName);
                break;
            case ACTIVE_OWN_USER_GROUPS:
                aoList = iPartsAuthorOrderAndPicLoader.loadAuthorOrderListActiveOwnUserGroups(project, loginUserName);
                break;
            case MINE:
                aoList = iPartsAuthorOrderAndPicLoader.loadAuthorOrderListByCreationUser(project, loginUserName);
                break;
            case DELEGATE:
                aoList = iPartsAuthorOrderAndPicLoader.loadAuthorOrderListByDelegate(project, loginUserName);
                break;
            case ONLY_QA:
                aoList = iPartsAuthorOrderAndPicLoader.loadAuthorOrderListAllQA(project, loginUserName);
                break;
            case ONLY_MY_QA:
                aoList = iPartsAuthorOrderAndPicLoader.loadAuthorOrderListMyQA(project, loginUserName);
                break;
            case ALL_APPROVED:
                String dateFrom = "";
                String dateTo = "";
                EditFilterDateObject filterDateObject = EditFilterDateGuiHelper.getDateFilterObjectFromSession(SESSION_KEY_AUTHOR_ORDER_FILTER_DATE);
                if (filterDateObject != null) {
                    dateFrom = filterDateObject.getDateFromForDbSearch();
                    dateTo = filterDateObject.getDateToForDbSearch();
                }
                aoList = iPartsDataAuthorOrderList.loadAllApprovedAuthorOrdersInTimeIntervallFromDB(project, dateFrom, dateTo).
                        filterByUser(getProject(), loginUserName);
                break;
            default:
                aoList = new iPartsDataAuthorOrderList();
        }
        if (aoList != null) {
            getTable().switchOffEventListeners();
            iPartsDataWorkOrder emptyOrder = new iPartsDataWorkOrder(project, null);
            emptyOrder.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            for (iPartsDataAuthorOrder dataAuthorOrder : aoList) {
                iPartsDataWorkOrder currentOrder = emptyOrder;
                if (!dataAuthorOrder.getBstId().isEmpty()) {
                    iPartsWorkOrderId id = new iPartsWorkOrderId(dataAuthorOrder.getBstId());
                    currentOrder = iPartsWorkOrderCache.getInstance(id, project).getDataWorkOrder(project);
                }
                grid.addObjectToGrid(dataAuthorOrder, currentOrder);
            }
            getTable().switchOnEventListeners();
        }
        grid.showNoResultsLabel((aoList == null) || aoList.isEmpty());
        doEnableButtons();
    }

    private void reloadGrid(boolean restoreFilter) {
        reloadGrid(restoreFilter, false);
    }

    private void reloadGrid(boolean restoreFilter, boolean clearHeader) {
        List<iPartsDataAuthorOrder> selectedDataAuthors = getSelectedDataObjects();
        int oldSortColumn = -1;
        boolean isSortAscending = getTable().isSortAscending();
        Map<Integer, Object> columnFilterValuesMap = null; // Map von Spalten-Indizes auf Filtertwert-Objekte
        AbstractGuiTableColumnFilterFactory copyColumnFilterFactory = null;
        if (getTable().getRowCount() > 0) {
            oldSortColumn = getTable().getSortColumn();
            if (restoreFilter) {
                columnFilterValuesMap = new HashMap<Integer, Object>(); // Map von Spalten-Indizes auf Filtertwert-Objekte
                copyColumnFilterFactory = grid.storeFilterFactory(columnFilterValuesMap);
            }
        }

        fillGrid(clearHeader);

        if (copyColumnFilterFactory != null) {
            grid.restoreFilterFactory(copyColumnFilterFactory, columnFilterValuesMap);
        }
        // Sortierung wiederherstellen falls vorher sortiert war
        if (getTable().isSortEnabled() && (oldSortColumn >= 0)) {
            getTable().sortRowsAccordingToColumn(oldSortColumn, isSortAscending);
        }
        if (!selectedDataAuthors.isEmpty()) {
            setSelectedAuthorOrders(selectedDataAuthors);
        }
    }

    private void reloadGridThreadSafe(final boolean restoreFilter) {
        Session session = Session.get();
        if (session != null) {
            session.invokeThreadSafe(new Runnable() {
                @Override
                public void run() {
                    reloadGrid(restoreFilter);
                }
            });
        }
    }

    private void doNewAuthorOrder(Event event) {
        VarParam<Boolean> setActive = new VarParam<>(false);
        iPartsDataAuthorOrder dataAuthorOrder = EditUserControlForAuthorOrder.showCreateAuthorOrder(getConnector(), this, setActive);
        if (doSaveToDB(dataAuthorOrder)) {
            reloadGridSetFilterAndSelectAuthorOrder(true, FILTER_TYPE.ACTIVE_OWN, dataAuthorOrder);
            if (setActive.getValue()) {
                doActivateAuthorOrder();
            }
        }
    }

    private void doEditAuthorOrder(Event event) {
        iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
        if (dataAuthorOrder != null) {
            boolean isActive = getRevisionsHelper().isRevisionChangeSetActive(dataAuthorOrder.getChangeSetId());
            boolean isReadOnly = !isEditAllowed;
            if (!isReadOnly) {
                isReadOnly = !dataAuthorOrder.isCurrentUserEqualLoginUserWithFallbackToCreationUser();
            }
            if (!isReadOnly) {
                isReadOnly = dataAuthorOrder.getStatus() == iPartsAuthorOrderStatus.getEndState();
            }
            dataAuthorOrder = EditUserControlForAuthorOrder.showEditAuthorOrder(getConnector(), this, dataAuthorOrder.getAsId(), isActive, isReadOnly);
            if (doSaveToDB(dataAuthorOrder)) {
                reloadGridThreadSafe(true);
            }
        }
    }

    /**
     * Aktiviert den ausgewählten Autorenauftrag
     */
    private boolean doActivateAuthorOrder() {
        if (getSelectedDataObjects().size() != 1) {
            // Sollte nicht passieren
            return false;
        }
        iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
        if ((dataAuthorOrder == null) || dataAuthorOrder.getChangeSetId().isEmpty()) {
            return false;
        }
        // Ist das aktuelle ChangeSet nicht aktiv, dann setze es aktiv
        EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
        if (revisionsHelper != null) {
            if (!iPartsEditPlugin.startEditing()) {
                return false;
            }
            try {
                if (!revisionsHelper.isRevisionChangeSetActive(dataAuthorOrder.getChangeSetId())) {
                    if (!closeOpenEditableModules()) {
                        return false;
                    }
                    iPartsRevisionChangeSet changeSet = new iPartsRevisionChangeSet(dataAuthorOrder.getChangeSetId(), getProject());
                    boolean isOwnDelegatedOrder = dataAuthorOrder.isCurrentUserEqualLoginUser();
                    if (isOwnDelegatedOrder && iPartsAuthorOrderStatus.isNormalState(dataAuthorOrder.getStatus())) {
                        revisionsHelper.setActiveRevisionChangeSet(changeSet, getProject()); // Edit-ChangeSet
                    } else {
                        List<iPartsRevisionChangeSet> activeChangeSets = new DwList<>(1);
                        activeChangeSets.add(changeSet);
                        revisionsHelper.setActiveRevisionChangeSets(activeChangeSets, null, true, getProject()); // ReadOnly-ChangeSet
                    }
                }

                activeAuthorOrder = dataAuthorOrder;
                getProject().fireProjectEvent(new AuthorOrderChangedEvent(dataAuthorOrder.getAsId()));

                // Statusmeldung setzen
                updateStatusMessageForActiveAuthorOrder();
                reloadGridThreadSafe(true);
                fillViewerForm(true);
                return true;
            } finally {
                iPartsEditPlugin.stopEditing();
            }
        } else {
            return false;
        }
    }

    /**
     * Gibt die {@link AbstractGuiControl}s zurück, die in der Statusleiste zusätzlich angezeigt werden sollen für einen
     * aktiven Autoren-Auftrag.
     */
    public List<AbstractGuiControl> getAdditionalControlsForStatusBar() {
        if ((activeAuthorOrder != null) && isChildOfMasterRootWindow()) {
            List<AbstractGuiControl> additionalControlsForStatusBar = new ArrayList<>();
            if (LDAPHelper.getInstance().isLdapAdminOptionActivated()) {
                additionalControlsForStatusBar.add(getStatusTextSeparator());
            }
            additionalControlsForStatusBar.add(getStatusTextLabel(activeAuthorOrder));
            return additionalControlsForStatusBar;
        }

        return null;
    }

    /**
     * Setzt die {@link AbstractGuiControl}s, die in der Statusleiste zusätzlich angezeigt werden sollen für einen (nicht)
     * aktiven Autoren-Auftrag.
     */
    private void updateStatusMessageForActiveAuthorOrder() {
        List<AbstractGuiControl> additionalControlsForStatusBar = getAdditionalControlsForStatusBar();
        if (additionalControlsForStatusBar != null) {
            for (AbstractGuiControl control : additionalControlsForStatusBar) {
                getConnector().getMainWindow().addAdditionalControlToStatusBar(control, true, false);
            }
        } else {
            if (statusSeparator != null) {
                getConnector().getMainWindow().removeAdditionalControlFromStatusBar(statusSeparator);
            }
            if (statusLabel != null) {
                getConnector().getMainWindow().removeAdditionalControlFromStatusBar(statusLabel);
            }
        }
    }

    private GuiSeparator getStatusTextSeparator() {
        if (statusSeparator == null) {
            statusSeparator = new GuiSeparator();
            statusSeparator.setName("authorOrderStatusLogSeparator");
            statusSeparator.__internal_setGenerationDpi(96);
            statusSeparator.registerTranslationHandler(getUITranslationHandler());
            statusSeparator.setScaleForResolution(true);
            statusSeparator.setMinimumHeight(16);
            statusSeparator.setVisible(true);
            statusSeparator.setOrientation(DWOrientation.VERTICAL);
        }
        return statusSeparator;
    }

    /**
     * Deaktiviert den ausgewählten Autorenauftrag
     */
    private void doDeactivateAuthorOrder() {
        if (getSelectedDataObjects().size() != 1) {
            // Sollte nicht passieren
            return;
        }

        if (!iPartsEditPlugin.startEditing()) {
            return;
        }
        try {
            if (!closeOpenEditableModules()) {
                return;
            }
            iPartsDataAuthorOrder dataAuthorOrder = getSelectedDataObject();
            if (dataAuthorOrder != null) {
                deactivateAuthorOrder(dataAuthorOrder);
                fillViewerForm(false);
            }
        } finally {
            iPartsEditPlugin.stopEditing();
        }
    }

    /**
     * Deaktiviert den übergebenen Autorenauftrag
     *
     * @param dataAuthorOrder
     */
    private void deactivateAuthorOrder(iPartsDataAuthorOrder dataAuthorOrder) {
        EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
        iPartsChangeSetId changeSetId = dataAuthorOrder.getChangeSetId();
        if (revisionsHelper.isRevisionChangeSetActive(changeSetId)) {
            EtkProject project = getProject();
            iPartsRevisionChangeSet.clearCachesForActiveChangeSets(project);
            revisionsHelper.removeActiveRevisionChangeSet(changeSetId, project);
            activeAuthorOrder = null;
            updateStatusMessageForActiveAuthorOrder();
        }
        reloadGridThreadSafe(true);

        getProject().fireProjectEvent(new AuthorOrderChangedEvent(null));
    }

    /**
     * Schließt alle im Edit Mode geöffneten Module.
     *
     * @return <i>true</i> wenn alle editierbaren Module geschlossen werden konnten, sonst <i>false</i>
     */
    public boolean closeOpenEditableModules() {
        JavaViewerMainWindow mainWindow = getConnector().getMainWindow();
        List<AbstractJavaViewerMainFormContainer> editModuleForms = mainWindow.getFormsFromClass(EditModuleForm.class);
        if (!editModuleForms.isEmpty()) {
            EditModuleForm editModuleForm = (EditModuleForm)editModuleForms.get(0);
            // durch den parameter all = false bekommt man nur die editierbaren module
            Collection<EditModuleForm.EditModuleInfo> moduleInfos = editModuleForm.getEditModuleInfoList(false);
            if (!moduleInfos.isEmpty()) {
                // Es wird davon ausgegangen dass alle Änderungen am Modul innerhalb einer Related Info stattfinden, und
                // somit schon gespeichert sind. Die Modulbenennung konnte mal extern bearbeitet werden, ist aber derzeit
                // deaktiviert, soll in Zukunft dann auch über die Stammdaten passieren

                editModuleForm.resetPreparedAndLazyLoadForClose(moduleInfos);
                // Alle im Edit Modus geöffneten Module schließen
                for (EditModuleForm.EditModuleInfo moduleInfo : moduleInfos) {
                    editModuleForm.closeModule(moduleInfo);
                }

                // nochmal prüfen ob die Module tatsächlich geschlossen wurden, wenn nicht darf der Autoren Auftrag nicht deaktiviert werden
                return editModuleForm.getEditModuleInfoList(false).isEmpty();
            }
        }
        return true;
    }

    private AbstractGuiControl getStatusTextLabel(iPartsDataAuthorOrder dataAuthorOrder) {
        if (statusLabel == null) {
            statusLabel = new GuiLabel();
            statusLabel.setName("activeAuthorOrderLabel");
            statusLabel.__internal_setGenerationDpi(96);
            statusLabel.setScaleForResolution(true);
            statusLabel.setFontStyle(DWLayoutManager.get().isResponsiveMode() ? DWFontStyle.SEMI_BOLD : DWFontStyle.BOLD);
            statusLabel.setForegroundColor(JavaViewerApplication.statusForeground.getColor());
        }
        if (dataAuthorOrder != null) {
            AbstractRevisionChangeSet changeSetForEdit = getRevisionsHelper().getActiveRevisionChangeSetForEdit();
            if ((changeSetForEdit != null) && changeSetForEdit.getChangeSetId().equals(dataAuthorOrder.getChangeSetId())) {
                statusLabel.setText(TranslationHandler.translate("!!Aktiver Autoren-Auftrag: %1", dataAuthorOrder.getAuthorOrderName()));
            } else {
                statusLabel.setText(TranslationHandler.translate("!!Autoren-Auftrag zum Anzeigen: %1", dataAuthorOrder.getAuthorOrderName()));
            }
        }
        return statusLabel;

    }

    private void doDeleteAuthorOrder(Event event) {
        List<iPartsDataAuthorOrder> dataAuthorOrderList = getSelectedDataObjects();
        if (!dataAuthorOrderList.isEmpty()) {
            String msg = "!!Wollen Sie den selektierten Autoren Auftrag wirklich löschen?";
            if (dataAuthorOrderList.size() > 1) {
                msg = "!!Wollen Sie die selektierten Autoren Aufträge wirklich löschen?";
            }
            if (MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                   MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES) {
                if (checkAuthorOrdersToDelete(dataAuthorOrderList)) {
                    if (deleteAuthorOrders(dataAuthorOrderList)) {
                        Session.invokeThreadSafeInSession(() -> fillGrid(false));
                    }
                }
            }
        }
    }

    private boolean doSaveToDB(iPartsDataAuthorOrder dataAuthorOrder) {
        if (dataAuthorOrder != null) {
            dataAuthorOrder.createChangeSetInDBIfNotExists();
            getDbLayer().startTransaction();
            try {
                if (dataAuthorOrder.isNew()) {
                    dataAuthorOrder.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
                } else {
                    dataAuthorOrder.saveToDB();
                }
                getDbLayer().commit();
                return true;
            } catch (Exception e) {
                getDbLayer().rollback();
                Logger.getLogger().handleRuntimeException(e);
            }
        }
        return false;
    }

    private boolean deleteAuthorOrder(iPartsDataAuthorOrder dataAuthorOrder) {
        getDbLayer().startTransaction();
        try {
            dataAuthorOrder.deleteFromDB();
            getDbLayer().commit();
            return true;
        } catch (Exception e) {
            getDbLayer().rollback();
            Logger.getLogger().handleRuntimeException(e);
        }
        return false;
    }

    private boolean checkAuthorOrdersToDelete(List<iPartsDataAuthorOrder> dataAuthorOrderList) {
        List<String> warnings = new DwList<String>();
        for (iPartsDataAuthorOrder dataAuthorOrder : dataAuthorOrderList) {
            if (!dataAuthorOrder.isChangeSetEmpty()) {
                warnings.add(TranslationHandler.translate("!!Auftrag \"%1\" besitzt bereits Änderungen und kann nicht gelöscht werden.",
                                                          dataAuthorOrder.getFieldValue(FIELD_DAO_NAME)));
            } else if (!dataAuthorOrder.isCreationUserEqualLoginUser()) {
                warnings.add(TranslationHandler.translate("!!Auftrag \"%1\" ist von einem anderen Benutzer angelegt worden und kann nicht gelöscht werden.",
                                                          dataAuthorOrder.getFieldValue(FIELD_DAO_NAME)));
            }
        }
        if (!warnings.isEmpty()) {
            warnings.add(0, "!!Löschen kann nicht durchgeführt werden");
            MessageDialog.showWarning(StrUtils.stringListToString(warnings, OsUtils.NEWLINE), "!!Warnung");
            return false;
        }
        return true;
    }

    private boolean deleteAuthorOrders(List<iPartsDataAuthorOrder> dataAuthorOrderList) {
        getDbLayer().startTransaction();
        getDbLayer().startBatchStatement();
        try {
            for (iPartsDataAuthorOrder dataAuthorOrder : dataAuthorOrderList) {
                dataAuthorOrder.deleteFromDB();
            }
            return true;
        } catch (Exception e) {
            getDbLayer().cancelBatchStatement();
            getDbLayer().rollback();
            Logger.getLogger().handleRuntimeException(e);
        } finally {
            getDbLayer().endBatchStatement();
            getDbLayer().commit();
        }
        return false;
    }

    private void doRefreshButtonAction(Event event) {
        reloadGridThreadSafe(true);
    }

    /**
     * Erstellt die benötigten Anzeigefelder
     *
     * @return
     */
    private EtkDisplayFields buildDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(getConfig(), iPartsEditConfigConst.iPARTS_EDIT_AUTHOR_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
//            EtkDisplayFields displayFields = new EtkDisplayFields();
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, iPartsDataVirtualFieldsDefinition.DAO_ACTIVE, false, false);
            EtkMultiSprache text = new EtkMultiSprache("!!Aktiv", getProject().getConfig().getDatabaseLanguages());
            displayField.setText(text);
            displayField.setDefaultText(false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CREATOR_GRP_ID, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CREATION_USER_ID, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CURRENT_GRP_ID, false, false);
            displayField.setColumnFilterEnabled(true);
            text = new EtkMultiSprache("!!Aktuelle Benutzergruppe", getProject().getConfig().getDatabaseLanguages());
            displayField.setText(text);
            displayField.setDefaultText(false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CURRENT_USER_ID, false, false);
            text = new EtkMultiSprache("!!Zugewiesen an", getProject().getConfig().getDatabaseLanguages());
            displayField.setText(text);
            displayField.setDefaultText(false);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_STATUS, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_NAME, true, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, iPartsDataVirtualFieldsDefinition.DAO_TOTAL_PICORDERS_STATE, false, false);
            text = new EtkMultiSprache("!!Gesamtstatus Bildaufträge", getProject().getConfig().getDatabaseLanguages());
            displayField.setText(text);
            displayField.setDefaultText(false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CREATION_DATE, false, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_WORKORDER, FIELD_DWO_TITLE, false, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_WORKORDER, FIELD_DWO_RELEASE_NO, false, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_DESC, true, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CHANGE_SET_ID, false, false);
            displayFields.addFeld(displayField);

            displayFields.loadStandards(getConfig());
        } else {
            EtkDisplayField isActiveField = displayFields.getFeldByName(TABLE_DA_AUTHOR_ORDER, iPartsDataVirtualFieldsDefinition.DAO_ACTIVE);
            if (isActiveField == null) {
                EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, iPartsDataVirtualFieldsDefinition.DAO_ACTIVE, false, false);
                EtkMultiSprache text = new EtkMultiSprache("!!Aktiv", getProject().getConfig().getDatabaseLanguages());
                displayField.setText(text);
                displayField.setDefaultText(false);
                displayFields.addFeld(0, displayField);
            } else {
                isActiveField.setColumnFilterEnabled(false); // Filterung macht hier keinen Sinn und funktioniert auch nicht
            }
        }
        modifyDisplayFields(displayFields);
        return displayFields;
    }

    /**
     * Passt die übergebenen Displayfields nachträglich an.
     * Das Freigabedatum des Autorenauftrags soll nur in Abhängigkeit des aktuellen Filters aus- bzw. eingeblendet werden
     *
     * @param displayFields
     * @return <code>true</code> falls die Displayfields angepasst wurden
     */
    private boolean modifyDisplayFields(EtkDisplayFields displayFields) {
        EtkDisplayField releaseDateField = displayFields.getFeldByName(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_RELDATE, false);
        if (releaseDateField != null) {
            if (releaseDateField.isVisible() && !filterType.showApprovalDate) {
                releaseDateField.setVisible(false);
                return true;
            } else if (!releaseDateField.isVisible() && filterType.showApprovalDate) {
                releaseDateField.setVisible(true);
                return true;
            }
        }
        return false;
    }

    private void supplyAuthorOrdersToBST(Event event) {
        Session.startChildThreadInSession(thread -> {
            int validAuthorOrders = 0;
            int suppliedAuthorOrders = 0;
            SupplyAuthorOrderToBSTHelper supplyAuthorOrderToBSTHelper = new SupplyAuthorOrderToBSTHelper(getProject());
            for (iPartsDataAuthorOrder dataAuthorOrder : getSelectedDataObjects()) {
                if (iPartsAuthorOrderStatus.isEndState(dataAuthorOrder.getStatus()) && !dataAuthorOrder.getBstId().isEmpty()) {
                    validAuthorOrders++;
                    if (supplyAuthorOrderToBSTHelper.supplyAuthorOrderToBST(dataAuthorOrder)) {
                        suppliedAuthorOrders++;
                    }
                }
            }

            // Wenn mindestens ein Autoren-Auftrag versorgt wurde, dann muss die Tabelle aktualisiert werden
            if (suppliedAuthorOrders > 0) {
                reloadGridThreadSafe(true);
            }

            MessageDialog.show(TranslationHandler.translate("!!Es wurden %1 von %2 gültigen Autoren-Aufträgen erfolgreich an BST versorgt.",
                                                            String.valueOf(suppliedAuthorOrders), String.valueOf(validAuthorOrders)),
                               "!!Autoren-Auftrag an BST versorgen");
        });
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
        private de.docware.framework.modules.gui.controls.menu.GuiMenuBar menubar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiMenuBarEntry tstMenubarentry;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel SRK25076;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel centerPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneAuthorOrder;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneAuthorOrder_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanelExtra;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneInfo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneInfo_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPane tabbedpaneExtra;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentryInfo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedpaneentryInfoContent;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneInfo_secondChild;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            menubar = new de.docware.framework.modules.gui.controls.menu.GuiMenuBar();
            menubar.setName("menubar");
            menubar.__internal_setGenerationDpi(96);
            menubar.registerTranslationHandler(translationHandler);
            menubar.setScaleForResolution(true);
            menubar.setMinimumWidth(10);
            menubar.setMinimumHeight(10);
            tstMenubarentry = new de.docware.framework.modules.gui.controls.menu.GuiMenuBarEntry();
            tstMenubarentry.setName("tstMenubarentry");
            tstMenubarentry.__internal_setGenerationDpi(96);
            tstMenubarentry.registerTranslationHandler(translationHandler);
            tstMenubarentry.setScaleForResolution(true);
            tstMenubarentry.setMinimumWidth(10);
            tstMenubarentry.setMinimumHeight(10);
            SRK25076 = new de.docware.framework.modules.gui.controls.GuiLabel();
            SRK25076.setName("SRK25076");
            SRK25076.__internal_setGenerationDpi(96);
            SRK25076.registerTranslationHandler(translationHandler);
            SRK25076.setScaleForResolution(true);
            SRK25076.setText("WILL BE REPLACED BY PARENT TOOLTIP");
            tstMenubarentry.setTooltip(SRK25076);
            tstMenubarentry.setMnemonicEnabled(true);
            tstMenubarentry.setText("!!Test");
            menubar.addChild(tstMenubarentry);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder menubarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            menubarConstraints.setPosition("north");
            menubar.setConstraints(menubarConstraints);
            this.addChild(menubar);
            centerPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            centerPanel.setName("centerPanel");
            centerPanel.__internal_setGenerationDpi(96);
            centerPanel.registerTranslationHandler(translationHandler);
            centerPanel.setScaleForResolution(true);
            centerPanel.setMinimumWidth(10);
            centerPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder centerPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            centerPanel.setLayout(centerPanelLayout);
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
            centerPanel.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitpaneAuthorOrder = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneAuthorOrder.setName("splitpaneAuthorOrder");
            splitpaneAuthorOrder.__internal_setGenerationDpi(96);
            splitpaneAuthorOrder.registerTranslationHandler(translationHandler);
            splitpaneAuthorOrder.setScaleForResolution(true);
            splitpaneAuthorOrder.setMinimumWidth(10);
            splitpaneAuthorOrder.setMinimumHeight(10);
            splitpaneAuthorOrder.setDividerPosition(572);
            splitpaneAuthorOrder_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneAuthorOrder_firstChild.setName("splitpaneAuthorOrder_firstChild");
            splitpaneAuthorOrder_firstChild.__internal_setGenerationDpi(96);
            splitpaneAuthorOrder_firstChild.registerTranslationHandler(translationHandler);
            splitpaneAuthorOrder_firstChild.setScaleForResolution(true);
            splitpaneAuthorOrder_firstChild.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneAuthorOrder_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneAuthorOrder_firstChild.setLayout(splitpaneAuthorOrder_firstChildLayout);
            splitpaneAuthorOrder.addChild(splitpaneAuthorOrder_firstChild);
            dockingpanelExtra = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanelExtra.setName("dockingpanelExtra");
            dockingpanelExtra.__internal_setGenerationDpi(96);
            dockingpanelExtra.registerTranslationHandler(translationHandler);
            dockingpanelExtra.setScaleForResolution(true);
            dockingpanelExtra.setMinimumWidth(16);
            dockingpanelExtra.setMinimumHeight(116);
            dockingpanelExtra.setTextHide("!!Extras");
            dockingpanelExtra.setTextShow("!!Extras anzeigen");
            dockingpanelExtra.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelWest"));
            dockingpanelExtra.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelEast"));
            dockingpanelExtra.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            dockingpanelExtra.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            dockingpanelExtra.setButtonFill(true);
            splitpaneInfo = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneInfo.setName("splitpaneInfo");
            splitpaneInfo.__internal_setGenerationDpi(96);
            splitpaneInfo.registerTranslationHandler(translationHandler);
            splitpaneInfo.setScaleForResolution(true);
            splitpaneInfo.setMinimumWidth(0);
            splitpaneInfo.setMinimumHeight(10);
            splitpaneInfo.setHorizontal(false);
            splitpaneInfo.setDividerPosition(393);
            splitpaneInfo_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneInfo_firstChild.setName("splitpaneInfo_firstChild");
            splitpaneInfo_firstChild.__internal_setGenerationDpi(96);
            splitpaneInfo_firstChild.registerTranslationHandler(translationHandler);
            splitpaneInfo_firstChild.setScaleForResolution(true);
            splitpaneInfo_firstChild.setMinimumWidth(0);
            splitpaneInfo_firstChild.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneInfo_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneInfo_firstChild.setLayout(splitpaneInfo_firstChildLayout);
            tabbedpaneExtra = new de.docware.framework.modules.gui.controls.GuiTabbedPane();
            tabbedpaneExtra.setName("tabbedpaneExtra");
            tabbedpaneExtra.__internal_setGenerationDpi(96);
            tabbedpaneExtra.registerTranslationHandler(translationHandler);
            tabbedpaneExtra.setScaleForResolution(true);
            tabbedpaneExtra.setMinimumWidth(0);
            tabbedpaneExtra.setMinimumHeight(10);
            tabbedpaneentryInfo = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentryInfo.setName("tabbedpaneentryInfo");
            tabbedpaneentryInfo.__internal_setGenerationDpi(96);
            tabbedpaneentryInfo.registerTranslationHandler(translationHandler);
            tabbedpaneentryInfo.setScaleForResolution(true);
            tabbedpaneentryInfo.setMinimumWidth(10);
            tabbedpaneentryInfo.setMinimumHeight(10);
            tabbedpaneentryInfo.setTitle("!!Info");
            tabbedpaneentryInfoContent = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedpaneentryInfoContent.setName("tabbedpaneentryInfoContent");
            tabbedpaneentryInfoContent.__internal_setGenerationDpi(96);
            tabbedpaneentryInfoContent.registerTranslationHandler(translationHandler);
            tabbedpaneentryInfoContent.setScaleForResolution(true);
            tabbedpaneentryInfoContent.setPaddingTop(4);
            tabbedpaneentryInfoContent.setPaddingLeft(4);
            tabbedpaneentryInfoContent.setPaddingRight(4);
            tabbedpaneentryInfoContent.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedpaneentryInfoContentLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedpaneentryInfoContent.setLayout(tabbedpaneentryInfoContentLayout);
            tabbedpaneentryInfo.addChild(tabbedpaneentryInfoContent);
            tabbedpaneExtra.addChild(tabbedpaneentryInfo);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tabbedpaneExtraConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tabbedpaneExtra.setConstraints(tabbedpaneExtraConstraints);
            splitpaneInfo_firstChild.addChild(tabbedpaneExtra);
            splitpaneInfo.addChild(splitpaneInfo_firstChild);
            splitpaneInfo_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneInfo_secondChild.setName("splitpaneInfo_secondChild");
            splitpaneInfo_secondChild.__internal_setGenerationDpi(96);
            splitpaneInfo_secondChild.registerTranslationHandler(translationHandler);
            splitpaneInfo_secondChild.setScaleForResolution(true);
            splitpaneInfo_secondChild.setMinimumWidth(0);
            splitpaneInfo_secondChild.setMinimumHeight(0);
            splitpaneInfo_secondChild.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneInfo_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneInfo_secondChild.setLayout(splitpaneInfo_secondChildLayout);
            splitpaneInfo.addChild(splitpaneInfo_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneInfoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneInfo.setConstraints(splitpaneInfoConstraints);
            dockingpanelExtra.addChild(splitpaneInfo);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder dockingpanelExtraConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            dockingpanelExtra.setConstraints(dockingpanelExtraConstraints);
            splitpaneAuthorOrder.addChild(dockingpanelExtra);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneAuthorOrderConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneAuthorOrder.setConstraints(splitpaneAuthorOrderConstraints);
            panelMain.addChild(splitpaneAuthorOrder);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            centerPanel.addChild(panelMain);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder centerPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            centerPanel.setConstraints(centerPanelConstraints);
            this.addChild(centerPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}