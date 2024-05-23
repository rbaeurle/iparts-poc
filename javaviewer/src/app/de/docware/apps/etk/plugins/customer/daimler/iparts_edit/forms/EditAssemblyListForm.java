/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.InplaceEdit.GuiTableInplaceEditorManager;
import de.docware.apps.etk.base.forms.common.InplaceEdit.InplaceEditor;
import de.docware.apps.etk.base.forms.common.SimpleDoubleListSelectForm;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.events.OnExtendFormEvent;
import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.base.mechanic.listview.forms.*;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkHotspotLinkHelper;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkHotspotDestination;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChangeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsRetailUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPas;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacementConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuListItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.RelatedInfoSingleEditHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderPartsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder.PicOrdersToModuleGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.EditTransferToASHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoDialogMultistageRetailReplacementChainForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoInternalTextForPartDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoPicOrdersToPartData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoPrimusReplacementChainForm;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.misc.DWPoint;
import de.docware.framework.modules.gui.controls.table.*;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerUtils;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.inputdialog.InputDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.NotImplementedCode;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sort.SortBetweenHelper;
import de.docware.util.sort.SortStringCache;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Form zur Darstellung der StüLi für Edit
 */
public class EditAssemblyListForm extends AbstractJavaViewerForm implements iPartsConst {

    // Konfigurationen zum Konfigurieren der EditFelder beim Vereinheitlichen von Stücklistenpositionen im Edit
    private static final String CONFIG_KEY_MULTIPLE_CHANGES_DIALOG = "Plugin/iPartsEdit/MasterData/MultipleChangesDIALOG/EditFields";
    private static final String CONFIG_KEY_MULTIPLE_CHANGES_ELDAS = "Plugin/iPartsEdit/MasterData/MultipleChangesELDAS/EditFields";
    private static final String INTERNAL_TEXT_WORKING_TEXT = "!!Internen Text bearbeiten";
    private static final String MODIFIED_PARTLISTENTRY_FILTER_TEXT = "!!Nur neue Stücklisteneinträge anzeigen";
    private static final String MODIFIED_PARTLISTENTRY_FILTER_REVERT_TEXT = "!!Alle Stücklisteneinträge anzeigen";
    private static final String TEMPLATE_TEXT_COPY_ENTRY_TO_OTHER_PARTLIST = "!!%1 in anderen TU kopieren";
    private static final String TEMPLATE_TEXT_RELOCATE_ENTRY_TO_OTHER_PARTLIST = "!!%1 in anderen TU verschieben";
    private static final String TEMPLATE_TEXT_COPY_ENTRY_TO_OTHER_SA_PARTLIST = "!!%1 in anderen SA TU kopieren";

    private boolean updatePictureOrderIcons;
    private EditAssemblyListView assemblyListForm;
    private final boolean isEditAllowed;
    private boolean isFiltered;
    private boolean isSorted;
    private String filteredSelectedHotSpot;
    private EditToolbarButtonMenuHelper toolbarHelper;

    /**
     * Das Icon für neuen/modifizierten Stücklisteneintrag soll als letztes Icon angezeigt werden.
     * Deswegen hier Zugriffsfunktion für {@link iPartsEditPlugin#getRelatedInfoIcons(AbstractJavaViewerFormIConnector, EtkDataPartListEntry)}.
     *
     * @param partListEntry
     * @return
     */
    public static AssemblyListCellContentFromPlugin getPartListEntryModifiedIcon(iPartsDataPartListEntry partListEntry) {
        if (partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_MODIFIED_STATE).equals(ENUM_MODIFIED_STATE_NEW)) {
            AssemblyListCellContentFromPlugin info = new AssemblyListCellContentFromPlugin(null,
                                                                                           EditToolbarButtonAlias.EDIT_PARTLISTENTRY_MODIFIED.getImage());
            info.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
            String hint = EditToolbarButtonAlias.EDIT_PARTLISTENTRY_MODIFIED.getTooltip();
            info.setHint(TranslationHandler.translate(hint));
            info.setCursor(DWCursor.Unspecific);
            info.setCellContent(EtkConfigConst.IMGPREFIX + EditToolbarButtonAlias.EDIT_PARTLISTENTRY_MODIFIED.getAlias());
            return info;
        }
        return null;
    }

    /**
     * Erzeugt eine Instanz von EditAssemblyListForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditAssemblyListForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String windowTitle) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        mainWindow.title.setTitle(windowTitle);
        this.isEditAllowed = getConnector().isAuthorOrderValid(); // muss hier schon wegen postCreateGui() gesetzt werden
        this.isFiltered = false;
        this.isSorted = false;

        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbarAssemblyEdit);
        toolbarManager = toolbarHelper.getToolbarManager();

        assemblyListForm = new EditAssemblyListView(getConnector(), this);
        assemblyListForm.setName("editModuleForm");

        mainWindow.panelGrid.addChild(assemblyListForm.getGui());
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, false);
        assemblyListForm.setShowTooltips(false);
        assemblyListForm.updateView();
        getConnector().setAssemblyListDisplayFields(assemblyListForm.getDisplayFields());
        createToolbarButtons();
        enableButtons();

        // Ohne Einträge in picOrderPartListEntryIdsForIcons muss beim nächsten updateData() eine Aktualisierung stattfinden
        updatePictureOrderIcons = (assemblyListForm.picOrderPartListEntryIdsForIcons == null) || assemblyListForm.picOrderPartListEntryIdsForIcons.isEmpty();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assemblyListForm != null) {
            assemblyListForm.dispose();
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public GuiButtonPanel getButtonPanel() {
        return mainWindow.buttonPanel;
    }

    public AssemblyListForm getAssemblyListForm() {
        return assemblyListForm;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public boolean isUpdatePictureOrderIcons() {
        return updatePictureOrderIcons;
    }

    public void setUpdatePictureOrderIcons(boolean updatePictureOrderIcons) {
        this.updatePictureOrderIcons = updatePictureOrderIcons;
    }

    public EtkDataPartListEntry getFirstPartListEntryForLink(EtkHotspotDestination link) {
        return assemblyListForm.getFirstPartListEntryForLink(link);
    }

    public void gotoPartListEntry(EtkDataPartListEntry partListEntry) {
        gotoPartListEntry(partListEntry.getFieldValue(EtkDbConst.FIELD_K_LFDNR));
    }

    public void gotoPartListEntry(String kLfdNr) {
        assemblyListForm.gotoPartListEntry(kLfdNr, false);
    }

    private void createToolbarButtons() {
        boolean isCurrentAssemblyCarPerspective = isCarPerspectiveModule();

        contextMenuTable.addChild(new GuiSeparator());

        ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doDelete(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_SELECTALL, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doSelectAll(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        toolbarHelper.addSeparatorToToolbarAndMenu("ToolbarSeparatorTools", contextMenuTable, getUITranslationHandler());

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_SORT_BY_HOTSPOT, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doSortPartlistEntriesByHotspot(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MOVE_UP, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doMovePartlistEntriesUp(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MOVE_DOWN, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doMovePartlistEntriesDown(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MOVE_TO_POSITION, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doMovePartlistEntriesTo(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        if (!isSaModule()) {
            holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_COPY_ENTRY, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doCopyEntryToOtherPartList(event);
                }
            });
            contextMenuTable.addChild(holder.menuItem);
            holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_RELOCATE_ENTRY, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doRelocateEntryToOtherPartlist(event);
                }
            });
            contextMenuTable.addChild(holder.menuItem);
        } else {
            holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_COPY_ENTRY_TO_SA, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doCopyEntryToOtherSaModule(event);
                }
            });
            contextMenuTable.addChild(holder.menuItem);
        }

        toolbarHelper.addSeparatorToToolbarAndMenu("ToolbarSeparatorMove", contextMenuTable, getUITranslationHandler());

        // Usage bereits in AssemblyListForm Menu vorhanden
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_USAGE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            public void fire(Event event) {
                assemblyListForm.menuItemUsageClick(event);
            }
        });

        toolbarHelper.addSeparatorToToolbarAndMenu("ToolbarSeparatorEdit", null, getUITranslationHandler());

        // Stammdaten: CONFIG_KEY_RELATED_INFO_MASTER_DATA, CONFIG_KEY_RELATED_INFO_SA_MASTER_DATA
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MAT, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doEditMat(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        // Stücklistendaten (zusätzlich enthalten: Fußnoten, Wahlweise-Teile):  CONFIG_KEY_RELATED_INFO_EDIT_DETAILS_DATA
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_PARTLISTENTRY, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doEditPartListEntry(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        // Fußnoten: CONFIG_KEY_RELATED_INFO_FOOT_NOTE_DATA
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_FOOTNOTES, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doFootNotesEdit(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        // Wahlweise-Teile: CONFIG_KEY_RELATED_INFO_WW_PARTS_DATA
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_OPTIONALPARTS, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doOptionalPartsEdit(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        // Ersetzungen: CONFIG_KEY_RELATED_INFO_REPLACEMENTS_DATA
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_REPLACEMENTS, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doReplacements(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        // Ersetzungen/Werksdaten: CONFIG_KEY_RELATED_INFO_SUPER_EDIT_DATA
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_FACTORY_DATA, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doFactoryDataEdit(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        // Variantenzuordnung zu Teil: CONFIG_KEY_RELATED_INFO_VARIANTS_TO_PART_DATA
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_COLORTABLES, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doColortablesEdit(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        // Baukasten zu Teil: CONFIG_KEY_RELATED_INFO_CONSTRUCTION_KITS_DATA -> hierfür gibt es kein Toolbar-Icon (DAIMLER-7902)

        // Interner Text: CONFIG_KEY_RELATED_INFO_INTERNAL_TEXT_FOR_PART_DATA
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_INTERNAL_TEXT, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doInternalTextEdit(event);
            }
        });
        holder.toolbarButton.setTooltip(INTERNAL_TEXT_WORKING_TEXT);
        holder.menuItem.setText(INTERNAL_TEXT_WORKING_TEXT);
        contextMenuTable.addChild(holder.menuItem);

        // Alternativteile: CONFIG_KEY_RELATED_INFO_ALTERNATIVE_PARTS  -> hierfür gibt es kein Toolbar-Icon (DAIMLER-7902)

        // Code-Erklärung: CONFIG_KEY_RELATED_INFO_CODE_MASTER_DATA
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_CODE_MATRIX, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doShowCodeMatrix(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        holder.menuItem = toolbarHelper.createMenuEntry(EditToolbarButtonAlias.EDIT_PRIMUS_REPLACEMENT_CHAIN.getAlias(),
                                                        iPartsConst.RELATED_INFO_PRIMUS_REPLACE_CHAIN_DATA_TEXT,
                                                        EditDefaultImages.edit_rep_chain.getImage(),
                                                        new EventListener(Event.MENU_ITEM_EVENT) {

                                                            @Override
                                                            public void fire(Event event) {
                                                                doShowPrimusReplaceChainData(event);
                                                            }
                                                        }, getUITranslationHandler());
        holder.menuItem.setUserObject(EditToolbarButtonAlias.EDIT_PRIMUS_REPLACEMENT_CHAIN);
        contextMenuTable.addChild(holder.menuItem);

        holder.menuItem = toolbarHelper.createMenuEntry(EditToolbarButtonAlias.EDIT_DIALOG_RETAIL_REPLACEMENT_CHAIN.getAlias(),
                                                        iPartsConst.RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN_TEXT,
                                                        EditDefaultImages.edit_rep_chain_both.getImage(),
                                                        new EventListener(Event.MENU_ITEM_EVENT) {

                                                            @Override
                                                            public void fire(Event event) {
                                                                doShowDialogMultistageRetailReplacementChain(event);
                                                            }
                                                        }, getUITranslationHandler());
        holder.menuItem.setUserObject(EditToolbarButtonAlias.EDIT_DIALOG_RETAIL_REPLACEMENT_CHAIN);
        contextMenuTable.addChild(holder.menuItem);

        toolbarHelper.addSeparatorToToolbarAndMenu("ToolbarSeparatorExtra", contextMenuTable, getUITranslationHandler());

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.IMG_ADD_TO_PICORDER, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doAddPartListEntriesToPicOrder(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);
        holder.toolbarButton.setVisible(!isCurrentAssemblyCarPerspective);
        holder.menuItem.setVisible(!isCurrentAssemblyCarPerspective);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_PARTLISTENTRY_MODIFIED, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doFilterPartListEntryModified(event);
            }
        });
        holder.toolbarButton.setTooltip(MODIFIED_PARTLISTENTRY_FILTER_TEXT);
        holder.menuItem.setText(MODIFIED_PARTLISTENTRY_FILTER_TEXT);
        contextMenuTable.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MULTIPLE_ENTRIES, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doEditMultipleEntries(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MERGING_ENTRIES, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doEditMergingEntries(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_ACCEPT_MATERIAL_CHANGES, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doAcceptMaterialChanges(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_ACCEPT_PARTLISTENTRY_CHANGES, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doAcceptPartlistentryChanges(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        if (isEditAllowed) {
            toolbarHelper.addSeparatorToToolbarAndMenu("ToolbarSeparatorEdit", contextMenuTable, getUITranslationHandler());
        }

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_CLEARSORT, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doClearSort(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        toolbarHelper.addSeparatorToToolbarAndMenu("ToolbarSeparatorLockEntries", contextMenuTable, getUITranslationHandler());

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_LOCK_PART_LIST_ENTRY, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doLockPartListEntriesForEdit(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_UNLOCK_PART_LIST_ENTRY, getUITranslationHandler(), new MenuRunnable() {
            @Override
            public void run(Event event) {
                doUnlockPartListEntriesForEdit(event);
            }
        });
        contextMenuTable.addChild(holder.menuItem);

        GuiToolButton helper = toolbarHelper.addToolbarButton(ToolbarButtonAlias.FILTER, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            public void fire(Event event) {
                doContainerFilter(event);
            }
        });
        helper.setText("");
        GuiContextMenu assemblyContextMenu = assemblyListForm.getContextMenu();
        GuiWindow rootWindow = assemblyContextMenu.getRootWindow();
        if (rootWindow != null) {
            rootWindow.beforeContextMenuChange();
        }

        //updateGUI();    // immer ausführen
        List<AbstractGuiControl> belowControls = new ArrayList<>();
        // da es kein addChild(child, pos) gibt, alle nachfolgenden entfernen und später wieder hinzufügen
        for (int i = assemblyContextMenu.getChildren().size() - 1; i >= 0; i--) {
            AbstractGuiControl child = assemblyContextMenu.getChildren().get(i);
            if (child.getUserObject() == null) { // nicht geklonte Menüeinträge
                belowControls.add(child);
            }
            assemblyContextMenu.removeChild(child);
            if (child.isOfType(GuiSeparator.TYPE)) {
                break;
            }
        }
        for (AbstractGuiControl control : contextMenuTable.getChildren()) {
            AbstractGuiControl clone = control.cloneMe();
            clone.setUserObject(control.getUserObject());
            control.copyEventListeners(clone, Event.MENU_ITEM_EVENT);
            assemblyContextMenu.addChild(clone);
        }
        for (int i = belowControls.size() - 1; i >= 0; i--) {
            assemblyContextMenu.addChild(belowControls.get(i));
        }
        boolean isDialog = ((iPartsDataAssembly)getConnector().getCurrentAssembly()).getDocumentationType().isPKWDocumentationType();
        if (isDialog) {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MERGING_ENTRIES, assemblyListForm.getContextMenu());
        } else {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MERGING_ENTRIES, assemblyListForm.getContextMenu());
        }
    }

    /**
     * @param deleteList
     * @param deleteType
     * @param withMessageLogForm
     * @return
     */
    private boolean deletePartListEntries(List<EtkDataPartListEntry> deleteList,
                                          DELETE_TYPE deleteType, boolean withMessageLogForm) {
        if ((deleteList != null) && !deleteList.isEmpty()) {
            final EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
            DBDataObjectList<EtkDataPartListEntry> currentPartList = currentAssembly.getPartListUnfiltered(null);
            Set<iPartsRetailUsageId> deletedRetailUsageIds = new HashSet<>();
            Set<AssemblyId> otherModifiedAssemblyIds = new HashSet<>();
            final EtkProject project = getProject();
            final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Löschen", "!!Stücklisteneinträge löschen",
                                                                           iPartsToolbarButtonAlias.EDIT_DELETE.getImage(),
                                                                           false);
            messageLogForm.disableButtons(true);
            messageLogForm.getGui().setHeight(250);

            FrameworkRunnable deleteRunnable = new FrameworkRunnable() {
                private final List<EtkDataPartListEntry> deleteListWithOtherPLEs = new ArrayList<>(deleteList);
                private int maxProgress;
                private int currentProgress;

                @Override
                public void run(FrameworkThread thread) {
                    boolean deleteOtherPLEsWithSourceGUID = (deleteType == DELETE_TYPE.ALL_TU_GUID) || (deleteType == DELETE_TYPE.ALL_TU_AA_GUID);
                    if (deleteOtherPLEsWithSourceGUID) {
                        showStartMessage();
                    } else {
                        showDeleteMessage(deleteList.size());
                    }

                    // damit der Progressbar nicht bereits nach der 1. Hälfte auf 100% steht
                    maxProgress = 2 * deleteList.size();
                    currentProgress = 0;
                    GenericEtkDataObjectList changeSetDataObjectList = new GenericEtkDataObjectList();
                    startPseudoTransactionForActiveChangeSet(true);
                    try {
                        calculateOtherPLEs(deleteOtherPLEsWithSourceGUID);
                        if (deleteOtherPLEsWithSourceGUID) {
                            showDeleteMessage(deleteListWithOtherPLEs.size());
                        }

                        // Wahlweise-Sets in allen betroffenen Modulen korrigieren
                        changeSetDataObjectList.addAll(deleteWWSets(deleteListWithOtherPLEs, getConnector().getCurrentAssembly(),
                                                                    project), DBActionOrigin.FROM_EDIT);

                        maxProgress += (deleteListWithOtherPLEs.size() - deleteList.size());
                        fireProgress(currentProgress, maxProgress);

                        if (getConnector().getCurrentAssembly() instanceof iPartsDataAssembly) {
                            ((iPartsDataAssembly)getConnector().getCurrentAssembly()).loadAllDataCombTextListsForPartList(); // Für bessere Performance beim Löschen
                        }
                        for (EtkDataPartListEntry partListEntry : deleteListWithOtherPLEs) {
                            // Entfernte Retail-Verwendungen für den Event aufsammeln
                            iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromDbValue(partListEntry.getFieldValue(FIELD_K_SOURCE_TYPE));
                            if (sourceType != iPartsEntrySourceType.NONE) {
                                String sourceGUID = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
                                if (!sourceGUID.isEmpty()) {
                                    deletedRetailUsageIds.add(new iPartsRetailUsageId(sourceType.getDbValue(), sourceGUID));
                                }
                            }

                            // Lädt auch alle Ersetzungen und "PEM ab/bis auswerten"-Flags an allen Vorgängern und Nachfolgern neu
                            EtkDataObjectList deletedReferencedData = partListEntry.deleteReferencedData(deleteListWithOtherPLEs);
                            if (deletedReferencedData == null) {
                                deletedReferencedData = new GenericEtkDataObjectList();
                            }

                            // Das Flag "automatisch erzeugt" am Stücklisteneintrag zurücksetzen und den Stücklisteneintrag löschen
                            partListEntry.setFieldValueAsBoolean(FIELD_K_AUTO_CREATED, false, DBActionOrigin.FROM_EDIT);
                            deletedReferencedData.delete(partListEntry, true, DBActionOrigin.FROM_EDIT);

                            // Jetzt schon im ChangeSet speichern, damit beim Löschen von mehreren Stücklisteneinträgen
                            // dies von den anderen Stücklisteneinträgen z.B. bei Ersetzungen bereits berücksichtigt werden
                            // kann. Theoretisch sollte dies zwar weiter unten in der Transaktion stattfinden, aber dies
                            // ist leider nicht möglich, da die Änderungen ansonsten eben nicht berücksichtigt werden
                            // könnten beim Löschen von mehreren Stücklisteneinträgen. Die Transaktion schon vor dieser
                            // Schleife zu starten funktioniert leider ebenfalls nicht, weil im Worst Case Pseudo-Transaktionen
                            // benötigt werden könnten beim Laden von Daten für das Löschen der Stücklisteneinträge.
                            addDataObjectListToActiveChangeSetForEdit(deletedReferencedData);

                            currentProgress++;
                            fireProgress(currentProgress, maxProgress);
                        }
                    } finally {
                        stopPseudoTransactionForActiveChangeSet();
                        if (getConnector().getCurrentAssembly() instanceof iPartsDataAssembly) {
                            ((iPartsDataAssembly)getConnector().getCurrentAssembly()).clearAllDataCombTextListsForPartList();
                        }
                    }

                    // Änderungen sollen sofort ins Changeset gespeichert werden
                    EtkDbObjectsLayer dbLayer = project.getDbLayer();
                    dbLayer.startTransaction();
                    dbLayer.startBatchStatement();
                    try {
                        addDataObjectListToActiveChangeSetForEdit(changeSetDataObjectList);

                        // Andere veränderte Module und das aktuelle Modul als geändert markieren im ChangeSet
                        for (AssemblyId otherModifiedAssemblyId : otherModifiedAssemblyIds) {
                            EtkDataAssembly otherAssembly = EtkDataObjectFactory.createDataAssembly(project, otherModifiedAssemblyId);
                            otherAssembly.markAssemblyInChangeSetAsChanged();
                        }
                        currentAssembly.markAssemblyInChangeSetAsChanged();

                        dbLayer.endBatchStatement();
                        dbLayer.commit();
                    } catch (Exception e) {
                        dbLayer.cancelBatchStatement();
                        dbLayer.rollback();
                        MessageDialog.showError("!!Fehler beim Speichern", "!!Löschen");
                        messageLogForm.closeWindow(ModalResult.ABORT);
                        return;
                    }

                    // Stücklisteneinträge aus aktuellem Modul entfernen
                    for (EtkDataPartListEntry partListEntry : deleteList) {
                        // Hier nur DBActionOrigin.FROM_DB, weil das Löschen mit Eintrag ins ChangeSet über changeSetDataObjectList
                        // stattfindet und hier nur die aktuell angezeigte Stückliste aktualisiert werden soll
                        currentPartList.delete(partListEntry, DBActionOrigin.FROM_DB);
                    }
                    currentPartList.resetModifiedFlags();

                    // DAIMLER-9240: Die DELETE-Liste nochmal außerhalb des ChangeSets durchgehen und direkt aus DA_RESERVED_PK löschen
                    for (EtkDataPartListEntry partListEntry : deleteListWithOtherPLEs) {
                        if (partListEntry.getAttributes().fieldExists(iPartsDataVirtualFieldsDefinition.RETAIL_MODIFIED_STATE)) {
                            if (partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_MODIFIED_STATE).equals(iPartsConst.ENUM_MODIFIED_STATE_NEW)) {
                                iPartsDataReservedPKList.deleteReservedPrimaryKey(project, partListEntry.getAsId());
                            }
                        } else {
                            // zur Sicherheit
                            iPartsDataReservedPKList.deleteReservedPrimaryKey(project, partListEntry.getAsId());
                        }
                    }
                    showEndMessage();
                    messageLogForm.closeWindow(ModalResult.OK);
                }

                private void calculateOtherPLEs(boolean deleteOtherPLEsWithSourceGUID) {
                    fireProgress(currentProgress, maxProgress);

                    Map<AssemblyId, Boolean> assemblyIsPSKMap = new HashMap<>();
                    Set<PartListEntryId> extraDeleteEntrySet = new HashSet<>();
                    // Die selektierten Einträge zum Löschen
                    Set<PartListEntryId> deleteListIds = new HashSet<>();
                    deleteList.forEach(deleteEntry -> deleteListIds.add(deleteEntry.getAsId()));

                    for (EtkDataPartListEntry partListEntry : deleteList) {
                        // Entfernte Retail-Verwendungen für den Event aufsammeln
                        iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromDbValue(partListEntry.getFieldValue(FIELD_K_SOURCE_TYPE));
                        if (sourceType != iPartsEntrySourceType.NONE) {
                            String sourceGUID = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
                            if (!sourceGUID.isEmpty()) {
                                deletedRetailUsageIds.add(new iPartsRetailUsageId(sourceType.getDbValue(), sourceGUID));

                                // BCTE-Schlüssel in allen TUs löschen?
                                if (deleteOtherPLEsWithSourceGUID) {
                                    List<EtkDataPartListEntry> otherPLEs;
                                    switch (deleteType) {
                                        case ALL_TU_GUID:
                                            otherPLEs = getRetailSourceGuidPartListEntries(sourceGUID);
                                            for (EtkDataPartListEntry otherPLE : otherPLEs) {
                                                // partListEntry soll nicht doppelt in der Liste sein
                                                if (!otherPLE.getAsId().equals(partListEntry.getAsId())) {
                                                    AssemblyId otherAssemblyId = otherPLE.getOwnerAssemblyId();
                                                    boolean isPSKAssembly = ASUsageHelper.isPSKAssembly(project, otherAssemblyId, assemblyIsPSKMap);
                                                    if (!isPSKAssembly) {
                                                        deleteListWithOtherPLEs.add(otherPLE);
                                                        otherModifiedAssemblyIds.add(otherAssemblyId);
                                                    }
                                                }
                                            }
                                            break;
                                        case ALL_TU_AA_GUID:
                                            otherPLEs = getRetailSourceGuidWithoutAAPartListEntries(sourceGUID);
                                            for (EtkDataPartListEntry otherPLE : otherPLEs) {
                                                // partListEntry soll nicht doppelt in der Liste sein
                                                AssemblyId otherAssemblyId = otherPLE.getOwnerAssemblyId();
                                                if (!otherAssemblyId.equals(partListEntry.getAsId().getOwnerAssemblyId())) {
                                                    boolean isPSKAssembly = ASUsageHelper.isPSKAssembly(project, otherAssemblyId, assemblyIsPSKMap);
                                                    if (!isPSKAssembly) {
                                                        deleteListWithOtherPLEs.add(otherPLE);
                                                        otherModifiedAssemblyIds.add(otherAssemblyId);
                                                    }
                                                } else {
                                                    if (!otherPLE.getAsId().equals(partListEntry.getAsId())) {
                                                        // noch ein Eintrag in currentAssembly
                                                        // Falls mehrere Einträge zum Löschen selektiert waren und diese Einträge auch in
                                                        // otherPLEs vorhanden sind, dann nicht hinzufügen, sonst stimmt die Zählung am Ende nicht
                                                        if (!deleteListIds.contains(otherPLE.getAsId())) {
                                                            deleteListWithOtherPLEs.add(otherPLE);
                                                            extraDeleteEntrySet.add(otherPLE.getAsId());
                                                        }
                                                    }
                                                }
                                            }
                                            break;
                                    }
                                }
                            }
                        }

                        currentProgress++;
                        fireProgress(currentProgress, maxProgress);
                    }
                    if (deleteType == DELETE_TYPE.ALL_TU_AA_GUID) {
                        if (!extraDeleteEntrySet.isEmpty()) {
                            // zusätzliche TeilePos im currentAssembly zur deleteList hinzufügen
                            // falls anschließend in deleteList Einträge doppelt sind, ist das halb so schlimm,
                            // da bei currentPartList.delete() nicht forced benutzt wird
                            for (EtkDataPartListEntry partListEntry : currentPartList) {
                                if (extraDeleteEntrySet.contains(partListEntry.getAsId())) {
                                    deleteList.add(partListEntry);
                                }
                            }
                        }
                    }
                }

                private List<EtkDataPartListEntry> getRetailSourceGuidPartListEntries(String sourceGUID) {
                    return EditConstructionToRetailHelper.getRetailSourceGuidPartListEntries(iPartsEntrySourceType.DIALOG,
                                                                                             sourceGUID, null, project);
                }

                private List<EtkDataPartListEntry> getRetailSourceGuidWithoutAAPartListEntries(String sourceGUID) {
                    List<EtkDataPartListEntry> retailPartListEntries;
                    iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);
                    // zur Sicherheit
                    if (bcteKey != null) {
                        bcteKey.aa = "*";
                        DBDataObjectAttributesList retailPLEsAttributesList =
                                EditConstructionToRetailHelper.getRetailSourceGuidAttributeList(iPartsEntrySourceType.DIALOG,
                                                                                                bcteKey.createDialogGUID(),
                                                                                                bcteKey.getHmMSmId().getDIALOGSourceContext(),
                                                                                                null, false,
                                                                                                project);
                        retailPartListEntries = new ArrayList<>(retailPLEsAttributesList.size());
                        for (DBDataObjectAttributes attributes : retailPLEsAttributesList) {
                            EtkDataPartListEntry retailPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, attributes);
                            retailPartListEntries.add(retailPartListEntry);
                        }
                    } else {
                        retailPartListEntries = new ArrayList<>();
                    }
                    return retailPartListEntries;
                }

                private void showStartMessage() {
                    String extraText = deleteType.getMsgText();
                    if (StrUtils.isValid(extraText)) {
                        extraText = " " + TranslationHandler.translate(extraText);
                    }
                    extraText += "...";
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Suche Stücklisteneinträge")
                                                               + extraText);
                }

                private void showDeleteMessage(int numberOfPLEsToDelete) {
                    String extraText = deleteType.getMsgText();
                    if (StrUtils.isValid(extraText)) {
                        extraText = " " + TranslationHandler.translate(extraText);
                    }
                    extraText += "...";
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lösche %1 Stücklisteneinträge",
                                                                                            String.valueOf(numberOfPLEsToDelete))
                                                               + extraText);
                }

                private void showEndMessage() {
                    fireProgress(maxProgress, maxProgress);
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Fertig"));
                }

                private void fireProgress(int pos, int maxPos) {
                    messageLogForm.getMessageLog().fireProgress(pos, maxPos, "", false, true);
                }
            };

            if (withMessageLogForm) {
                messageLogForm.showModal(getRootParentWindow(), deleteRunnable);
            } else {
                deleteRunnable.run(null);
            }

            if (messageLogForm.getModalResult() == ModalResult.ABORT) {
                return false;
            }

            // Den Event für das Entfernen der Retail-Verwendungen feuern
            if (!deletedRetailUsageIds.isEmpty()) {
                project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.RETAIL_USAGE,
                                                                            iPartsDataChangedEventByEdit.Action.DELETED,
                                                                            deletedRetailUsageIds, false));
            }

            currentAssembly.clearPartListEntriesForEdit();
            getConnector().clearFilteredEditPartListEntries();
            getConnector().posNumberChanged();

            // Den AssemblyCache für das aktuelle Changeset für das aktuelle Modul und alle anderen veränderten Module löschen
            // sowie einen DataChangedEvent feuern, damit das Übernommen-Icon in der Edit-Stückliste richtig aktualisiert wird
            EtkDataAssembly.removeDataAssemblyFromCache(project, currentAssembly.getAsId());
            for (AssemblyId otherModifiedAssemblyId : otherModifiedAssemblyIds) {
                EtkDataAssembly.removeDataAssemblyFromCache(project, otherModifiedAssemblyId);
            }

            final AbstractJavaViewerForm parentForm = this;
            Session.invokeThreadSafeInSession(() -> {
                startPseudoTransactionForActiveChangeSet(true);
                try {
                    project.fireProjectEvent(new DataChangedEvent(parentForm), true);

                    // Damit der Hotspot richtig gezeichnet wird
                    List<EtkDataPartListEntry> dummyList = new DwList<>();
                    getConnector().setSelectedPartListEntries(dummyList);
                    getConnector().updateAllViews(parentForm, false);

                    if (!otherModifiedAssemblyIds.isEmpty()) {
                        iPartsEditPlugin.reloadModulesInEdit(otherModifiedAssemblyIds, getConnector());
                    }
                } finally {
                    stopPseudoTransactionForActiveChangeSet();
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Löscht überflüssige Wahlweise-Sets aufgrund der übergebenen gelöschten Stücklisteneinträge.
     *
     * @param deleteList
     * @param currentAssembly
     * @param project
     * @return
     */
    public static List<EtkDataPartListEntry> deleteWWSets(List<EtkDataPartListEntry> deleteList, EtkDataAssembly currentAssembly,
                                                          EtkProject project) {
        Set<PartListEntryId> deletedPLEIds = new HashSet<>();
        Map<AssemblyId, Set<String>> wwSetNoMap = new HashMap<>(); // Map von AssemblyId auf Set von WW-Set-Nummern
        Map<AssemblyId, List<EtkDataPartListEntry>> specialPartListEntriesMap = new HashMap<>(); // Map von AssemblyId auf Liste von Stücklisteneinträgen
        for (EtkDataPartListEntry partListEntry : deleteList) {
            deletedPLEIds.add(partListEntry.getAsId());
            if (!partListEntry.getFieldValue(iPartsConst.FIELD_K_WW).isEmpty()) {
                AssemblyId ownerAssemblyId = partListEntry.getAsId().getOwnerAssemblyId();
                Set<String> wwSetNoSet = wwSetNoMap.computeIfAbsent(ownerAssemblyId, assemblyId -> new TreeSet<>());
                wwSetNoSet.add(partListEntry.getFieldValue(iPartsConst.FIELD_K_WW));

                List<EtkDataPartListEntry> specialPartListEntries = specialPartListEntriesMap.computeIfAbsent(ownerAssemblyId,
                                                                                                              assemblyId -> new ArrayList<>());
                specialPartListEntries.add(partListEntry);
            }
        }

        List<EtkDataPartListEntry> modifiedOtherPartListEntries = new ArrayList<>();
        for (Map.Entry<AssemblyId, Set<String>> wwSetEntry : wwSetNoMap.entrySet()) {
            AssemblyId assemblyId = wwSetEntry.getKey();
            EtkDataAssembly assembly;
            if (assemblyId.equals(currentAssembly.getAsId())) {
                assembly = currentAssembly; // Neuladen vom aktuellen Modul vermeiden
            } else {
                assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
            }
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;

                // Damit werden auch die Wahlweise-Sets geladen durch das Laden der Stückliste
                Map<String, EtkDataPartListEntry> pleMap = iPartsAssembly.getFieldValueToPartListEntryMap(EtkDbConst.FIELD_K_LFDNR);

                List<EtkDataPartListEntry> specialPartListEntries = specialPartListEntriesMap.get(assemblyId);
                Map<String, List<EtkDataPartListEntry>> wwSetMap = new HashMap<>();
                for (String wwGuid : wwSetEntry.getValue()) {
                    List<EtkDataPartListEntry> wwPartListEntries = iPartsAssembly.getWWPartListEntries(wwGuid);
                    for (EtkDataPartListEntry partListEntry : specialPartListEntries) {
                        if (wwGuid.equals(partListEntry.getFieldValue(iPartsConst.FIELD_K_WW))) {
                            // Echter Stücklisteneintrag aus iPartsAssembly muss neu bestimmt werden über die Map, da der
                            // Stücklisteneintrag aus specialPartListEntries eine andere Instanz ist (wurde vorher schon
                            // unabhängig von iPartsAssembly geladen)
                            EtkDataPartListEntry pleInAssembly = pleMap.get(partListEntry.getAsId().getKLfdnr());
                            if (pleInAssembly != null) {
                                wwPartListEntries.remove(pleInAssembly);
                            }
                        }
                    }
                    wwSetMap.put(wwGuid, wwPartListEntries);
                }

                // WW-Set an anderen Stücklisteneinträgen korrigieren, wenn das WW-Set nun nur noch aus einem Stücklisteneintrag
                // bestehen würde
                for (List<EtkDataPartListEntry> wwPartListEntries : wwSetMap.values()) {
                    if (wwPartListEntries.size() == 1) {
                        for (EtkDataPartListEntry partListEntry : wwPartListEntries) {
                            if (!deletedPLEIds.contains(partListEntry.getAsId())) {
                                partListEntry.setFieldValue(iPartsConst.FIELD_K_WW, "", DBActionOrigin.FROM_EDIT);
                                modifiedOtherPartListEntries.add(partListEntry);
                            }
                        }
                    }
                }
            }
        }

        return modifiedOtherPartListEntries;
    }

    private enum DELETE_TYPE {
        ONLY_TU("!!Ja, nur selektierte Positionen", ""),
        ALL_TU_GUID("!!Ja, in allen TUs zur selben Ausführungsart", "!!in allen TUs zur selben Ausführungsart"),
        ALL_TU_AA_GUID("!!Ja, in allen TUs Ausführungsart übergreifend", "!!in allen TUs Ausführungsart übergreifend"),
        NO_BUTTON("!!Nein", "");

        private final String buttonText;
        private final String msgText;

        DELETE_TYPE(String buttonText, String msgText) {
            this.buttonText = buttonText;
            this.msgText = msgText;
        }

        public String getButtonText() {
            return buttonText;
        }

        public String getMsgText() {
            return msgText;
        }
    }

    private void doDelete(Event event) {
        List<EtkDataPartListEntry> selectedList = getConnector().getSelectedPartListEntries();
        if ((selectedList != null) && !selectedList.isEmpty()) {
            Optional<List<EtkDataPartListEntry>> selectedListWithoutLockedEntries = iPartsLockEntryHelper.checkLockedEntriesForDeletion(selectedList);
            // Es gibt keine gesperrten Positionen oder die nicht gesperrten sollen gelöscht werden
            selectedListWithoutLockedEntries.ifPresent(selectedEntries -> {
                String msg;
                if (selectedEntries.size() > 1) {
                    msg = TranslationHandler.translate("!!Wollen Sie die selektierten %1 Stücklisteneinträge wirklich löschen?",
                                                       String.valueOf(selectedEntries.size()));
                } else {
                    msg = "!!Wollen Sie den selektierten Stücklisteneintrag wirklich löschen?";
                }

                // Bei DIALOG in Nicht-PSK-Produkten sollen auch die Stücklisteneinträge aus anderen Modulen gelöscht werden
                // bei identischem BCTE-Schlüssel
                iPartsDocumentationType documentationType = iPartsDocumentationType.UNKNOWN;
                boolean isPSKAssembly = false;
                boolean isCurrentAssemblyCarPerspective = isCarPerspectiveModule();
                EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
                if (currentAssembly instanceof iPartsDataAssembly) {
                    iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)currentAssembly;
                    documentationType = iPartsAssembly.getDocumentationType();
                    isPSKAssembly = iPartsAssembly.isPSKAssembly();
                }
                EtkDataPartListEntry nextSelectedPartlistEntry = calcNextSelectRowAfterDelete();

                DELETE_TYPE deleteType = DELETE_TYPE.NO_BUTTON;
                if (documentationType.isDIALOGDocumentationType() && !isPSKAssembly && !isCurrentAssemblyCarPerspective) { // Nur bei echten DIALOG-Stücklisten (nicht PSK!) auch optional in anderen TUs löschen
                    String result = MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION.getImage(),
                                                       DELETE_TYPE.ONLY_TU.getButtonText(), DELETE_TYPE.ALL_TU_GUID.getButtonText(),
                                                       DELETE_TYPE.ALL_TU_AA_GUID.getButtonText(), DELETE_TYPE.NO_BUTTON.getButtonText());
                    if (Utils.objectEquals(result, DELETE_TYPE.ONLY_TU.getButtonText())) {
                        deleteType = DELETE_TYPE.ONLY_TU;
                    } else if (Utils.objectEquals(result, DELETE_TYPE.ALL_TU_GUID.getButtonText())) {
                        deleteType = DELETE_TYPE.ALL_TU_GUID;
                    } else if (Utils.objectEquals(result, DELETE_TYPE.ALL_TU_AA_GUID.getButtonText())) {
                        deleteType = DELETE_TYPE.ALL_TU_AA_GUID;
                    }
                } else {
                    if (MessageDialog.showYesNo(msg, "!!Löschen") == ModalResult.YES) {
                        deleteType = DELETE_TYPE.ONLY_TU;
                    }
                }
                if (deleteType != DELETE_TYPE.NO_BUTTON) {
                    if (deletePartListEntries(selectedEntries, deleteType, true)) {
                        assemblyListForm.gotoPartListEntry(nextSelectedPartlistEntry, true);
                    }
                }
            });

        }
    }

    private EtkDataPartListEntry calcNextSelectRowAfterDelete() {
        EtkDataPartListEntry nextSelectedPartlistEntry = null;
        GuiTable table = assemblyListForm.getPartListTable();
        int[] rowIndices = table.getSelectedRowIndices();
        List<Integer> rowList = new DwList<>(rowIndices);
        if (!rowList.isEmpty()) {
            int startIndex = rowList.get(0);
            int nextIndex = -1;
            for (int rowNo = startIndex + 1; rowNo < table.getRowCount(); rowNo++) {
                if (!rowList.contains(rowNo)) {
                    nextIndex = rowNo;
                    break;
                }
            }
            if (nextIndex == -1) {
                for (int rowNo = startIndex - 1; rowNo >= 0; rowNo--) {
                    if (!rowList.contains(rowNo)) {
                        nextIndex = rowNo;
                        break;
                    }
                }
            }
            if (nextIndex != -1) {
                Object userObject = table.getRow(nextIndex).getAdditionalData();
                if (userObject instanceof PartListEntryUserObjectForTableRow) {
                    nextSelectedPartlistEntry = ((PartListEntryUserObjectForTableRow)userObject).getPartListEntry();

                }
            }
        }
        return nextSelectedPartlistEntry;
    }

    private void doSelectAll(Event event) {
        assemblyListForm.selectAll();
        enableButtons();
    }

    private void doReplacements(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_REPLACEMENTS_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    /**
     * Editiere die Attribute mehrerer Stücklistenpositionen
     *
     * @param event
     */
    private void doEditMultipleEntries(Event event) {
        final EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
        if (revisionsHelper == null) {
            return;
        }
        AbstractRevisionChangeSet revisionChangeSetForEdit = revisionsHelper.getActiveRevisionChangeSetForEdit();
        if (revisionChangeSetForEdit != null) {
            final List<EtkDataPartListEntry> selectedPartListEntries = getConnector().getSelectedPartListEntries();
            // Check, ob Positionen gesperrt sind. Falls ja, Meldung ausgeben und abbrechen
            if (iPartsLockEntryHelper.checkLockedEntries(selectedPartListEntries, true)) {
                return;
            }
            EtkEditFields editFields = new EtkEditFields();
            final EtkDataAssembly assembly = getConnector().getCurrentAssembly();
            String rootKey = "";
            boolean isPSK = false;
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly dataAssembly = (iPartsDataAssembly)assembly;
                if (dataAssembly.getDocumentationType().isPKWDocumentationType()) {
                    rootKey = CONFIG_KEY_MULTIPLE_CHANGES_DIALOG;
                } else if (dataAssembly.getDocumentationType().isTruckDocumentationType()) {
                    rootKey = CONFIG_KEY_MULTIPLE_CHANGES_ELDAS;
                }
                isPSK = iPartsRight.checkPSKInSession() && dataAssembly.isPSKAssembly();
            }
            final EtkProject project = getProject();
            editFields.load(project.getConfig(), rootKey);
            if (editFields.size() == 0) {
                EtkEditField editField = new EtkEditField(TABLE_KATALOG, FIELD_K_POS, false);
                editFields.addFeld(editField);
                editField = new EtkEditField(TABLE_KATALOG, FIELD_K_HIERARCHY, false);
                editFields.addFeld(editField);
                editField = EtkEditFieldHelper.getEditFieldForVirtualField(getConnector(), iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT,
                                                                           "!!Kombinierter Ergänzungstext");
                editFields.addFeld(editField);
            }

            // Falls es sich nicht um ein PSK-Produkt handelt oder der Autor keine PSK-Rechte besitzt, darf das PSK-Variantengültigkeit Feld nicht
            // zu sehen sein
            if (!isPSK) {
                EtkEditField pskVariantEditField = editFields.getFeldByName(TABLE_KATALOG, iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY);
                if (pskVariantEditField != null) {
                    pskVariantEditField.setVisible(false);
                }
            }
            // Falls es sich nicht um einen Spezial-TU handelt => SpecValidity-Feld ausblenden
            EtkEditField specValidityField = editFields.getFeldByName(TABLE_KATALOG, iPartsConst.FIELD_K_SPEC_VALIDITY);
            if (specValidityField != null) {
                if (assembly instanceof iPartsDataAssembly) {
                    if (!iPartsSpecType.isSpecTypeRelevant(((iPartsDataAssembly)assembly).getSpecType())) {
                        specValidityField.setVisible(false);
                    }
                }
            }

            // wenn K_ETKZ = S bei einem der selektierten Einträge, dann ist eine Bearbeitung nur mit dem Recht EDIT_OMIT_FOR_SPECIAL_PARTS erlaubt
            EtkEditField omitField = editFields.getFeldByName(TABLE_KATALOG, iPartsConst.FIELD_K_OMIT);
            if (omitField != null) {
                boolean disableOmit = false;
                if (!iPartsRight.EDIT_OMIT_FOR_SPECIAL_PARTS.checkRightInSession()) {
                    for (EtkDataPartListEntry selectedPartListEntry : selectedPartListEntries) {
                        if ((selectedPartListEntry instanceof iPartsDataPartListEntry) && ((iPartsDataPartListEntry)selectedPartListEntry).isSpecialprotectionETKZ()) {
                            disableOmit = true;
                            break;
                        }
                    }
                }
                if (disableOmit) {
                    omitField.setEditierbar(false);
                }
            }

            if ((selectedPartListEntries != null) && !selectedPartListEntries.isEmpty()) {
                // Alle betroffenen Hotspots aufsammeln
                final Set<String> hotspots = new HashSet<>();
                for (EtkDataPartListEntry selectedPartListEntry : selectedPartListEntries) {
                    hotspots.add(selectedPartListEntry.getFieldValue(FIELD_K_POS));
                }

                final EditUserMultiChangeControlsForASPartlistEntry multiControl = new EditUserMultiChangeControlsForASPartlistEntry(getConnector(),
                                                                                                                                     getConnector().getActiveForm(),
                                                                                                                                     editFields,
                                                                                                                                     selectedPartListEntries);

                // Liefert die Attribute, die verändert wurden
                final DBDataObjectAttributes editedValues = EditUserMultiChangeControlsForASPartlistEntry.showEditUserMultiChangeControls(getConnector(),
                                                                                                                                          editFields,
                                                                                                                                          null,
                                                                                                                                          multiControl,
                                                                                                                                          EditUserMultiChangeControls.UnifySource.AFTERSALES);
                if (editedValues != null) {
                    final EtkMessageLogForm messageLogForm = new EtkMessageLogForm(EditToolbarButtonAlias.EDIT_MULTIPLE_ENTRIES.getTooltip(),
                                                                                   "!!Bitte warten...", null, true);
                    messageLogForm.disableButtons(true);
                    messageLogForm.showModal(new FrameworkRunnable() {
                        @Override
                        public void run(FrameworkThread thread) {
                            final Set<AssemblyId> modifiedAssemblyIds = new HashSet<>();

                            // Liefert die Datenobjekte, die im Changeset gespeichert werden sollen
                            EtkDataObjectList objectsToStore = EditUserMultiChangeControlsForASPartlistEntry.createDataObjectListForSelectedEntries(getConnector(),
                                                                                                                                                    selectedPartListEntries,
                                                                                                                                                    editedValues,
                                                                                                                                                    multiControl,
                                                                                                                                                    modifiedAssemblyIds);
                            if ((objectsToStore != null) && (!objectsToStore.isEmpty() || !objectsToStore.getDeletedList().isEmpty())) {
                                // Füge die Objekte zum Changeset hinzu
                                revisionChangeSetForEdit.addDataObjectList(objectsToStore);
                                // Nur weitermachen, wenn die zurückgelieferten Objekte auch gespeichert werden solllen
                                if (!revisionChangeSetForEdit.isEmpty()) {
                                    // Bestimmte virtuelle Felder leeren, damit sie beim Aufbau neu brechnet werden
                                    for (EtkDataPartListEntry partlistEntry : selectedPartListEntries) {
                                        iPartsEditUserControlsHelper.clearVirtualFieldsForReload(partlistEntry);

                                    }

                                    // Die Ersetzungen von allen Stücklisteneinträgen der betroffenen Hotspots (alte Werte
                                    // und neuer Wert) müssen aktualisiert werden
                                    boolean hotspotChanged = editedValues.containsKey(FIELD_K_POS);
                                    if (hotspotChanged) {
                                        getConnector().posNumberChanged();
                                        hotspots.add(editedValues.getFieldValue(FIELD_K_POS));
                                        EditModuleHelper.updateReplacementsAndFailLocationsForPLEsForHotspots(getConnector(),
                                                                                                              hotspots);
                                    }

                                    if (multiControl.isPasteCheckboxSelected()) {
                                        // Werkseinsatzdaten neu laden an den betroffenen Stücklisteneinträgen
                                        DBDataObjectList<EtkDataPartListEntry> selectedEntriesAsDBDataObjectList = new DBDataObjectList<>();
                                        selectedEntriesAsDBDataObjectList.addAll(selectedPartListEntries, DBActionOrigin.FROM_DB);
                                        if (assembly instanceof iPartsDataAssembly) {
                                            ((iPartsDataAssembly)assembly).loadAllFactoryDataForRetailForPartList(selectedEntriesAsDBDataObjectList);
                                        }
                                    }

                                    // Assembly aus dem Cache für das aktive ChangeSet löschen, da modifiziert
                                    EtkDataAssembly.removeDataAssemblyFromCache(project, assembly.getAsId());

                                    // Veränderte Module müssen aus dem Cache gelöscht und im Edit befindliche Module müssen
                                    // threadSafe neu geladen werden
                                    if (modifiedAssemblyIds != null) {
                                        Session.invokeThreadSafeInSession(() -> assemblyListForm.updateModifiedAssemblies(project,
                                                                                                                          modifiedAssemblyIds,
                                                                                                                          false));
                                    }

                                    project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                assembly.getAsId(), false));
                                    project.fireProjectEvent(new DataChangedEvent());
                                }
                            }
                            // Wartedialog schließen
                            messageLogForm.closeWindowIfNotAutoClose(ModalResult.OK);
                        }
                    });
                }
            }
        }
    }

    /**
     * EDS: Teilepositionen zusammenlegen
     *
     * @param event
     */
    private void doEditMergingEntries(Event event) {
        List<EtkDataPartListEntry> selectedPartListEntries = assemblyListForm.getSelectedEntries();
        if ((selectedPartListEntries != null) && (selectedPartListEntries.size() == 1)) {
            long lockedEntries = iPartsLockEntryHelper.getLockedEntries(selectedPartListEntries).size();
            // Mind. eine gesperrte Position und es gibt mind eine nicht gesperrte Position
            if (lockedEntries != 0) {
                MessageDialog.show("!!Zusammenlegen von Stücklistenpositionen ist nicht möglich, da die selektierte " +
                                   "Position für den Edit gesperrt ist.");
                return;
            }
            EtkDataPartListEntry masterPartListEntry = selectedPartListEntries.get(0);
            List<EtkDataPartListEntry> partListEntriesToMergeList = EditMergePartlistEntryForm.showMergePartListEntries(getConnector(), this, masterPartListEntry);
            if (partListEntriesToMergeList != null) {
                // SAA-Gültigkeiten aufsummieren
                EtkDataArray saaValidityArray = masterPartListEntry.getFieldValueAsArray(FIELD_K_SA_VALIDITY);
                Set<String> saaValiditySet = new TreeSet<>(saaValidityArray.getArrayAsStringList());
                for (EtkDataPartListEntry partListEntry : partListEntriesToMergeList) {
                    saaValiditySet.addAll(partListEntry.getFieldValueAsArray(FIELD_K_SA_VALIDITY).getArrayAsStringList());
                }
                saaValidityArray.clear(false);
                saaValidityArray.add(saaValiditySet);

                // SAA-Gültigkeit setzen und speichern
                masterPartListEntry.setFieldValueAsArray(FIELD_K_SA_VALIDITY, saaValidityArray, DBActionOrigin.FROM_EDIT);
                getProject().addDataObjectToActiveChangeSetForEdit(masterPartListEntry);

                // K_SOURCE_CONTEXT setzen laut DAIMLER-10936: "Es wird der Inhalt der gewählten TeilePos für das
                // Zusammenlegen an die selektierte TeilePos übernommen - sind mehrere TeilePos für das Zusammenlegen
                // ausgewählt, gewinnt der Erste."
                // Gewählte = erste in "partListEntriesToMergeList"; selektierte = "masterPartListEntry"
                // Es soll explizit der Context der ersten Position verwendet werden. Auch wenn er leer ist und die
                // anderen nicht
                String sourceContext = partListEntriesToMergeList.get(0).getFieldValue(FIELD_K_SOURCE_CONTEXT);
                masterPartListEntry.setFieldValue(FIELD_K_SOURCE_CONTEXT, sourceContext, DBActionOrigin.FROM_EDIT);

                // partListEntriesToMergeList aus TU löschen (incl ChangeSet)
                deletePartListEntries(partListEntriesToMergeList, DELETE_TYPE.ONLY_TU, false);
            }
        }
    }

    /**
     * Material-Änderungen (aktuell nur ET-KZ) bestätigen
     *
     * @param event
     */
    private void doAcceptMaterialChanges(Event event) {
        // Nur die selektierten Einträge, die auch wirklich eine ETKZ-Änderung haben an die Form übergeben
        List<EtkDataPartListEntry> selectedEntriesWithETKZChanges = new ArrayList<>();
        List<EtkDataPartListEntry> selectedEntries = assemblyListForm.getSelectedEntries();
        if (selectedEntries == null) {
            return;
        }
        for (EtkDataPartListEntry partListEntry : selectedEntries) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                iPartsDataDIALOGChangeList dialogChangesForBCTE = iPartsPartListEntry.getDIALOGChangesForBCTE();
                if (dialogChangesForBCTE != null) {
                    for (iPartsDataDIALOGChange dataDIALOGChange : dialogChangesForBCTE) {
                        if (dataDIALOGChange.getAsId().getDoType().equals(iPartsDataDIALOGChange.ChangeType.MAT_ETKZ.getDbKey())) {
                            selectedEntriesWithETKZChanges.add(partListEntry);
                        }
                    }
                }
            }
        }
        List<EtkDataPartListEntry> acceptedChanges = iPartsAcceptETKZChangesForm.showAcceptETKZChanges(getConnector(), this, selectedEntriesWithETKZChanges);
        if ((acceptedChanges != null) && !acceptedChanges.isEmpty()) {
            EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
            if (revisionsHelper == null) { // Kann eigentlich nicht passieren
                return;
            }
            AbstractRevisionChangeSet activeChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
            if (activeChangeSet == null) { // Kann eigentlich nicht passieren
                return;
            }

            // Speichern im ChangeSet muss auch innerhalb der Transaktion stattfinden
            final EtkDbObjectsLayer dbLayer = getProject().getDbLayer();
            dbLayer.startTransaction();
            dbLayer.startBatchStatement();
            try {
                ASUsageHelper asUsageHelper = new ASUsageHelper(getProject());
                String activeChangeSetGUID = activeChangeSet.getChangeSetId().getGUID();

                // Dialog-Changes Einträge pro BCTE-Schlüssel löschen
                Set<iPartsDialogBCTEPrimaryKey> acceptedBCTEKeys = new HashSet<>();
                iPartsDataDIALOGChangeList deletedDIALOGChangeList = new iPartsDataDIALOGChangeList();
                for (EtkDataPartListEntry partListEntry : acceptedChanges) {
                    iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                    iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
                    if (bctePrimaryKey != null) {
                        iPartsDataDIALOGChangeList dialogChangesForBCTE = iPartsPartListEntry.getDIALOGChangesForBCTE();
                        if (dialogChangesForBCTE != null) {
                            for (iPartsDataDIALOGChange dataDIALOGChange : dialogChangesForBCTE) {
                                // Aktuelles ChangeSet als Bearbeiter in DA_DIALOG_CHANGES eintragen und den Datensatz
                                // aus DA_DIALOG_CHANGES im ChangeSet löschen
                                dataDIALOGChange.setFieldValue(iPartsConst.FIELD_DDC_CHANGE_SET_GUID, activeChangeSetGUID,
                                                               DBActionOrigin.FROM_EDIT);
                                dataDIALOGChange.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
                                deletedDIALOGChangeList.delete(dataDIALOGChange, true, DBActionOrigin.FROM_EDIT);
                            }
                            iPartsPartListEntry.clearDIALOGChangesAttributes();
                        }
                        acceptedBCTEKeys.add(bctePrimaryKey);
                    }
                }

                final List<EtkDataPartListEntry> acceptedASPartListEntries = new DwList<>();
                for (final iPartsDialogBCTEPrimaryKey acceptedBCTEKey : acceptedBCTEKeys) {
                    List<EtkDataPartListEntry> partListEntriesUsedInAS = asUsageHelper.getPartListEntriesUsedInAS(acceptedBCTEKey, true);
                    if (partListEntriesUsedInAS != null) {
                        acceptedASPartListEntries.addAll(partListEntriesUsedInAS);
                    }
                }

                GenericEtkDataObjectList<EtkDataPartListEntry> modifiedPartListEntryList = new GenericEtkDataObjectList<>();
                setOmitFlagOnETKZChange(acceptedASPartListEntries, modifiedPartListEntryList);

                // Gelöschte Datensätze aus DA_DIALOG_CHANGES und geänderte Stücklisteneinträge speichern
                revisionsHelper.addDataObjectListToActiveChangeSetForEdit(modifiedPartListEntryList);
                revisionsHelper.addDataObjectListToActiveChangeSetForEdit(deletedDIALOGChangeList);

                // Als verändert markierte Module im ChangeSet speichern
                HashMap<AssemblyId, EtkDataAssembly> modifiedAssemblies = new HashMap<>();
                for (EtkDataPartListEntry modifiedPartListEntry : modifiedPartListEntryList) {
                    modifiedAssemblies.put(modifiedPartListEntry.getOwnerAssemblyId(), modifiedPartListEntry.getOwnerAssembly());
                }
                for (EtkDataAssembly modifiedAssembly : modifiedAssemblies.values()) {
                    modifiedAssembly.markAssemblyInChangeSetAsChanged();
                }

                setOmitFlagOnETKZChangeInChangeSets(asUsageHelper, activeChangeSet, acceptedBCTEKeys);

                dbLayer.endBatchStatement();
                dbLayer.commit();

                // Veränderte Module müssen aus dem Cache gelöscht und im Edit befindliche Module müssen neu geladen werden
                for (EtkDataAssembly modifiedAssembly : modifiedAssemblies.values()) {
                    EtkDataAssembly.removeDataAssemblyFromCache(getProject(), modifiedAssembly.getAsId());
                }
                // Auch das aktuell geöffnete Modul reloaden, damit sich der Änderungsgrund anpasst.
                iPartsEditPlugin.reloadModulesInEdit(modifiedAssemblies.keySet(), getConnector());

                getProject().fireProjectEvent(new DataChangedEvent());
            } catch (Exception e) {
                dbLayer.cancelBatchStatement();
                dbLayer.rollback();
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                MessageDialog.showError("!!Fehler beim Speichern.");
            }
        }
    }

    /**
     * führt {@link #setOmitFlagOnETKZChange} für alle Stücklisteneinträge mit dem übergebenen BCTE-Schlüsseln aus,
     * die nur innerhalb eines ChangeSets existieren, also dort neu angelegt wurden.
     *
     * @param asUsageHelper
     * @param activeChangeSet
     * @param acceptedBCTEKeys
     */
    private void setOmitFlagOnETKZChangeInChangeSets(ASUsageHelper asUsageHelper, AbstractRevisionChangeSet activeChangeSet,
                                                     Set<iPartsDialogBCTEPrimaryKey> acceptedBCTEKeys) {

        // andere ChangeSets suchen, die angepasst werden müssen.
        String activeChangeSetGUID = activeChangeSet.getChangeSetId().getGUID();
        Set<String> relevantChangeSetIds = new HashSet<>();
        for (iPartsDialogBCTEPrimaryKey acceptedBCTEKey : acceptedBCTEKeys) {
            Set<String> changeSetIds = asUsageHelper.getChangeSetIdsForPartListEntriesUsedInActiveChangeSets(acceptedBCTEKey);
            if (changeSetIds != null) {
                relevantChangeSetIds.addAll(changeSetIds);
            }
        }
        // das eigene Changeset ist bereits aktuell
        relevantChangeSetIds.remove(activeChangeSetGUID);

        List<ChangeSetModificator.ChangeSetModificationTask> changeSetModificationTasks = new DwList<>();
        for (final iPartsDialogBCTEPrimaryKey acceptedBCTEKey : acceptedBCTEKeys) {
            for (String relevantChangeSetId : relevantChangeSetIds) {
                ChangeSetModificator.ChangeSetModificationTask changeSetModificationTask = new ChangeSetModificator.ChangeSetModificationTask(relevantChangeSetId) {
                    @Override
                    public void modifyChangeSet(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                GenericEtkDataObjectList dataObjectListForChangeSet) {
                        List<EtkDataPartListEntry> newPartListEntries = ChangeSetModificator.getNewASPartListEntriesForBCTEKey(projectForChangeSet,
                                                                                                                               authorOrderChangeSet,
                                                                                                                               acceptedBCTEKey);
                        setOmitFlagOnETKZChange(newPartListEntries, dataObjectListForChangeSet);
                    }
                };
                changeSetModificationTasks.add(changeSetModificationTask);
            }
        }
        ChangeSetModificator changeSetModificator = new ChangeSetModificator(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS);
        changeSetModificator.executeChangesInAllChangeSets(changeSetModificationTasks, false,
                                                           activeChangeSet.getExplicitUser());
    }

    /**
     * DAIMLER-9188: Passt das Unterdrückt-Kennzeichen an allen übergebenen Stücklisteneinträgen an,
     * je nach Änderung des ET-KZ an deren Teil.
     *
     * @param acceptedASPartListEntries
     * @param modifiedPartListEntryList alle Stücklisteneinträge die modifiziert wurden.
     */
    private void setOmitFlagOnETKZChange(List<EtkDataPartListEntry> acceptedASPartListEntries,
                                         GenericEtkDataObjectList modifiedPartListEntryList) {
        for (EtkDataPartListEntry partListEntry : acceptedASPartListEntries) {
            String oldETKZ = partListEntry.getPart().getFieldValue(FIELD_M_ETKZ_OLD);
            String newETKZ = partListEntry.getPart().getFieldValue(FIELD_M_ETKZ);

            // DAIMLER-16375, Bei ET-KZ-Bestätigung wird bei Leitungssatz-BK und sonstiges KZ = 'LA' nicht automatisch "unterdrückt" gesetzt, falls das neue ET-KZ = 'K' ist
            if ((iPartsWireHarnessHelper.isWireHarnessPartListEntry(getProject(), partListEntry))
                && (iPartsWireHarnessHelper.hasValidAdditionalWireHarnessFlag(partListEntry)) // Sonstiges KZ == FIELD_M_LAYOUT_FLAG == "LA"
                && (newETKZ.equals("K"))) {
                continue;
            }

            if (oldETKZ.equals("E") && !newETKZ.equals("E")) {
                partListEntry.setFieldValueAsBoolean(FIELD_K_OMIT, true, DBActionOrigin.FROM_EDIT);
            } else if (!oldETKZ.equals("E") && newETKZ.equals("E")) {
                partListEntry.setFieldValueAsBoolean(FIELD_K_OMIT, false, DBActionOrigin.FROM_EDIT);
            }
            if (partListEntry.isModified()) {
                // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden
                iPartsDataPartListEntry.resetAutoCreatedFlag(partListEntry);
                modifiedPartListEntryList.add(partListEntry, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    /**
     * Teilepositions-Änderungen (aktuell nur ET-KZ) bestätigen
     *
     * @param event
     */
    private void doAcceptPartlistentryChanges(Event event) {
        // Prüfen, ob die Freigabe überhaupt zulässig ist
        EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
        if (revisionsHelper == null) { // Kann eigentlich nicht passieren
            return;
        }
        AbstractRevisionChangeSet activeChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
        if (activeChangeSet == null) { // Kann eigentlich nicht passieren
            return;
        }
        String changeSetGUID = activeChangeSet.getChangeSetId().getGUID();

        Map<EtkDataPartListEntry, List<iPartsDataDIALOGChange>> relevantPartlistEntriesLfdNr = new HashMap<>();
        List<String> partlistEntriesWithOtherAuthorOrder = new DwList<>();

        List<EtkDataPartListEntry> selectedEntries = assemblyListForm.getSelectedEntries();
        if (selectedEntries == null) {
            return;
        }
        for (EtkDataPartListEntry partListEntry : selectedEntries) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                iPartsDataDIALOGChangeList dialogChangesForBCTE = iPartsPartListEntry.getDIALOGChangesForBCTE();
                if (dialogChangesForBCTE != null) {
                    for (iPartsDataDIALOGChange dialogChange : dialogChangesForBCTE) {
                        if (dialogChange.getAsId().getDoType().equals(iPartsDataDIALOGChange.ChangeType.PARTLISTENTRY_ETKZ.getDbKey())) {
                            String dialogChangeChangeset = dialogChange.getFieldValue(FIELD_DDC_CHANGE_SET_GUID);
                            // Der Eintrag darf nur bearbeitet werden, wenn noch kein ChangeSet oder das aktuelle eingetragen wurde
                            if (dialogChangeChangeset.isEmpty() || dialogChangeChangeset.equals(changeSetGUID)) {
                                List<iPartsDataDIALOGChange> relevantChanges = relevantPartlistEntriesLfdNr.get(partListEntry);
                                if (relevantChanges == null) {
                                    relevantChanges = new DwList<>();
                                    relevantPartlistEntriesLfdNr.put(partListEntry, relevantChanges);
                                }
                                relevantChanges.add(dialogChange);
                            } else {
                                partlistEntriesWithOtherAuthorOrder.add(iPartsPartListEntry.getAsId().toStringForLogMessages());
                            }
                        }
                    }
                }
            }
        }

        String message = "";
        if (!partlistEntriesWithOtherAuthorOrder.isEmpty()) {
            message = TranslationHandler.translate("!!Folgende Stücklisteneinträge werden bereits in anderen Autorenaufträgen bearbeitet:%1%2",
                                                   "\n- ", StrUtils.stringListToString(partlistEntriesWithOtherAuthorOrder, "\n- "));
        }

        if (!message.isEmpty()) {
            message += "\n\n";
        }
        if (relevantPartlistEntriesLfdNr.isEmpty()) {
            // Nur Meldungen bringen
            MessageDialog.showWarning(message + TranslationHandler.translate("!!Bestätigen von ETK-Änderungen nicht möglich."),
                                      EditToolbarButtonAlias.EDIT_ACCEPT_PARTLISTENTRY_CHANGES.getTooltip());
        } else {
            List<EtkDataPartListEntry> keySetAsList = new ArrayList<>(relevantPartlistEntriesLfdNr.keySet());
            // Meldungen bringen (aber mit Ja/Nein Dialog, weil doch auch was getan werden kann)
            List<EtkDataPartListEntry> acceptedChanges = iPartsAcceptETKZChangesForm.showAcceptETKChanges(getConnector(), this, keySetAsList);
            if ((acceptedChanges != null) && !acceptedChanges.isEmpty()) {
                EtkDataAssembly currentAssembly = assemblyListForm.getConnector().getCurrentAssembly();

                // Hier startet der eigentliche Freigabe-Prozess
                GenericEtkDataObjectList dataListForChangeset = new GenericEtkDataObjectList();
                List<AssemblyId> constructionAssemblyIds = new DwList<>();

                getProject().getDbLayer().startTransaction();
                try {
                    for (EtkDataPartListEntry selectedEntry : acceptedChanges) {
                        if ((selectedEntry instanceof iPartsDataPartListEntry)) {
                            iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)selectedEntry;

                            // suche den zugehörigen Eintrag aus der Konstruktion
                            iPartsDataDialogData dialogData = new iPartsDataDialogData(getProject(), new iPartsDialogId(iPartsPartListEntry.getFieldValue(FIELD_K_SOURCE_GUID)));

                            // setzte den Status auf freigegeben
                            if (dialogData.existsInDB()) {
                                dialogData.setFieldValue(FIELD_DD_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
                                dataListForChangeset.add(dialogData, DBActionOrigin.FROM_EDIT);

                                // übernehme den ET-KZ Wert aus der Konstruktion an den AS-Stücklisteneintrag
                                String dialogETKZ = dialogData.getFieldValue(FIELD_DD_ETKZ);

                                // wenn sich der ET-KZ von "leer" auf "K" geändert hat K_OMIT setzen
                                String oldETKZ = iPartsPartListEntry.getFieldValue(FIELD_K_ETKZ);
                                if (oldETKZ.isEmpty() && dialogETKZ.equals("K")) {
                                    iPartsPartListEntry.setFieldValueAsBoolean(FIELD_K_OMIT, true, DBActionOrigin.FROM_EDIT);
                                }

                                // Feld K_ETKZ könnte in der Stückliste bisher fehlen -> setLogLoadFieldIfNeeded(false),
                                // um unnötige Logausgaben zu vermeiden
                                boolean oldLogLoadFieldIfNeeded = iPartsPartListEntry.isLogLoadFieldIfNeeded();
                                iPartsPartListEntry.setLogLoadFieldIfNeeded(false);
                                try {
                                    iPartsPartListEntry.setFieldValue(FIELD_K_ETKZ, dialogETKZ, DBActionOrigin.FROM_EDIT);
                                } finally {
                                    iPartsPartListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
                                }

                                dataListForChangeset.add(iPartsPartListEntry, DBActionOrigin.FROM_EDIT);
                            }

                            // Aktuelles ChangeSet als Bearbeiter in DA_DIALOG_CHANGES eintragen und den Datensatz im ChangeSet löschen
                            List<iPartsDataDIALOGChange> dialogChanges = relevantPartlistEntriesLfdNr.get(selectedEntry);
                            for (iPartsDataDIALOGChange dialogChange : dialogChanges) {
                                dialogChange.setFieldValue(iPartsConst.FIELD_DDC_CHANGE_SET_GUID, changeSetGUID,
                                                           DBActionOrigin.FROM_EDIT);
                                dialogChange.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
                                dataListForChangeset.delete(dialogChange, true, DBActionOrigin.FROM_EDIT);
                            }

                            // Cache für DIALOG-Änderungen am Stücklisteneintrag muss geleert werden
                            iPartsPartListEntry.clearDIALOGChangesAttributes();

                            // Konstruktions AssemblyId merken damit diese nachher aus dem Cache gelöscht werden kann
                            AssemblyId constructionAssembly = EditConstructionToRetailHelper.getVirtualConstructionAssemblyIdFromRetailPartlistEntry(selectedEntry);
                            if (constructionAssembly != null) {
                                constructionAssemblyIds.add(constructionAssembly);
                            }
                        }
                    }
                    activeChangeSet.addDataObjectList(dataListForChangeset);

                    // Assembly als geändert markieren
                    currentAssembly.markAssemblyInChangeSetAsChanged();
                    getProject().getDbLayer().commit();
                } catch (Exception e) {
                    getProject().getDbLayer().rollback();
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    MessageDialog.showError("!!Fehler beim Speichern.");
                    return;
                }

                // Veränderte AS-Stückliste im Edit neu laden
                assemblyListForm.loadCurrentAssembly();

                // AS-Stücklisten auch neu laden und vorher die AS-Stückliste aus dem Cache entfernen
                EtkDataAssembly.removeDataAssemblyFromCache(getProject(), currentAssembly.getAsId());
                // Konstruktionsstückliste auch aus dem Cache entfernen damit der Status richtig dargestellt wird
                for (AssemblyId constructionId : constructionAssemblyIds) {
                    EtkDataAssembly.removeDataAssemblyFromCache(getProject(), constructionId);
                }
                getProject().fireProjectEvent(new DataChangedEvent(), true);
            }
        }
    }

    private void doEditMat(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_MASTER_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doShowCodeMatrix(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_CODE_MASTER_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doShowPrimusReplaceChainData(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_PRIMUS_REPLACE_CHAIN_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doShowDialogMultistageRetailReplacementChain(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doEditPartListEntry(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_EDIT_DETAILS_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doFootNotesEdit(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_FOOT_NOTE_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doOptionalPartsEdit(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_WW_PARTS_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doColortablesEdit(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_VARIANTS_TO_PART_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doInternalTextEdit(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_INTERNAL_TEXT_FOR_PART_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doFactoryDataEdit(Event event) {
        if (assemblyListForm.getSelectionCount() == 1) {
            JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(assemblyListForm.getConnector(),
                                                                        RelatedInfoSingleEditHelper.getActiveRelatedInfo(getProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_FACTORY_DATA),
                                                                        iPartsRelatedInfoEditContext.createEditContext(assemblyListForm.getConnector(), isEditAllowed));
        }
    }

    private void doFilterPartListEntryModified(Event event) {
        GuiToolButton button = (GuiToolButton)toolbarManager.getButton(EditToolbarButtonAlias.EDIT_PARTLISTENTRY_MODIFIED.getAlias());
        if (button == null) {
            return;
        }
        button.setPressed(!button.isPressed());
        GuiMenuItem menuItem = toolbarHelper.findMenuItem(assemblyListForm.getContextMenu(), EditToolbarButtonAlias.EDIT_PARTLISTENTRY_MODIFIED);
        String text = MODIFIED_PARTLISTENTRY_FILTER_TEXT;
        if (button.isPressed()) {
            text = MODIFIED_PARTLISTENTRY_FILTER_REVERT_TEXT;
        }
        button.setTooltip(text);
        if (menuItem != null) {
            menuItem.setText(text);
        }
        ((EditModuleFormConnector)getConnector()).setShowOnlyModifiedPartListEntries(button.isPressed());
        for (AbstractJavaViewerForm viewForm : getConnector().getConnectedViews()) {
            if (viewForm instanceof AssemblyListForm) {
                viewForm.updateView();
            }
        }
    }

    private void doAddPartListEntriesToPicOrder(Event event) {
        List<EtkDataPartListEntry> selectedPartListEntries = getConnector().getSelectedPartListEntries();
        if ((selectedPartListEntries != null) && !selectedPartListEntries.isEmpty()) {
            List<iPartsDataPicOrder> picOrderList = null;
            // Finde die EditShowPictureOrderForm und hole die Bild-Aufträge, die noch modifiziert werden können
            for (AbstractJavaViewerForm viewForm : getConnector().getConnectedViews()) {
                if (viewForm instanceof PicOrdersToModuleGridForm) {
                    picOrderList = ((PicOrdersToModuleGridForm)viewForm).getPicOrdersForEdit();
                    break;
                }
            }
            if (picOrderList != null) {
                EditAssemblyImageForm assemblyImageForm = null;
                // Finde die EditAssemblyImageForm für Anzeige des Bildauftrags
                for (AbstractJavaViewerForm viewForm : getConnector().getConnectedViews()) {
                    if (viewForm instanceof EditAssemblyImageForm) {
                        assemblyImageForm = ((EditAssemblyImageForm)viewForm);
                        break;
                    }
                }
                if (assemblyImageForm != null) {
                    if (picOrderList.isEmpty()) {
                        // kein Bildauftrag kann mehr editiert werden, oder keiner vorhanden
                        // => erzeuge neuen Bildauftrag, setze selectedPartListEntries und Anzeige
                        assemblyImageForm.doCreatePicOrder(selectedPartListEntries, true);
                    } else {
                        iPartsDataPicOrder currentPicOrder = null;
                        if (picOrderList.size() > 1) {
                            // mehrere Bildaufträge vorhanden => Auswahl
                            GuiPanel panel = new GuiPanel();
                            panel.setName("picorderpanel");
                            panel.setLayout(new LayoutBorder());
                            panel.setMaximumHeight(23);
                            GuiComboBox<iPartsDataPicOrder> comboBox = new GuiComboBox<>();
                            comboBox.setName("picordercombobox");
                            comboBox.setConstraints(new ConstraintsBorder());
                            panel.addChild(comboBox);
                            for (iPartsDataPicOrder picOrder : picOrderList) {
                                comboBox.addItem(picOrder, picOrder.getProposedName());
                            }
                            if (InputDialog.show("!!Auswahl Bildauftrag", "!!Wählen Sie einen Bildauftrag", panel, null) == ModalResult.OK) {
                                currentPicOrder = comboBox.getSelectedUserObject();
                            }
                        } else {
                            // nru ein Bildauftrag vorhanden
                            currentPicOrder = picOrderList.get(0);
                        }
                        if (currentPicOrder != null) {
                            // füge selectedPartListEntries zum Bild-Auftrag hinzu + (nur für Development) Anzeige, sonst abspeichern
                            iPartsDataPicOrder.fillDataPicOrderNoDoubles(getProject(), currentPicOrder, selectedPartListEntries);
//                            if (Constants.DEVELOPMENT) {
//                                assemblyImageForm.showPictureOrder(currentPicOrder);
//                            } else {
                            assemblyImageForm.updatePictureOrder(currentPicOrder);
//                            }
                        }
                    }
                }
            }
        }
    }

    private void doClearSort(Event event) {
        assemblyListForm.getPartListTable().clearSort();
    }

    private void doContainerFilter(Event event) {
        showNotImplemented("ContainerFilter");
    }

    /**
     * Bestimmt den Index der übergeben PartlistEntryId in der ungefilterten Stückliste
     *
     * @param partListEntryId
     * @param unfilteredPartListEntries
     * @return {@code -1} wenn die partlistEntryId nicht in der Stückliste vorkommt
     */
    private int findPartlistEntryIndexInPartlistUnfiltered(PartListEntryId partListEntryId, List<EtkDataPartListEntry> unfilteredPartListEntries) {
        if (unfilteredPartListEntries != null) {
            for (int i = 0; i < unfilteredPartListEntries.size(); i++) {
                EtkDataPartListEntry currentPartListEntry = unfilteredPartListEntries.get(i);
                if (currentPartListEntry.getAsId().equals(partListEntryId)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Übergebene Stücklisteneinträge nach der Sequenznummer sortieren, damit z.B. beim Hoch-Runter-Verschieben
     * die Reihenfolge, in der die Einträge angeklickt wurden, irrelevant ist.
     *
     * @param partlistEntries
     */
    private void sortPartlistEntriesBySeqNr(List<EtkDataPartListEntry> partlistEntries) {
        if (partlistEntries != null) {
            Collections.sort(partlistEntries, new Comparator<EtkDataPartListEntry>() {
                @Override
                public int compare(EtkDataPartListEntry o1, EtkDataPartListEntry o2) {
                    return o1.getFieldValue(iPartsConst.FIELD_K_SEQNR).compareTo(o2.getFieldValue(iPartsConst.FIELD_K_SEQNR));
                }
            });
        }
    }

    /**
     * Bestimmt die SeqenceNumber des Stücklisteneintrags an {@code index} in der ungefilterten Stückliste
     *
     * @param index
     * @param unfilteredPartListEntries
     * @return {@code ""} wenn der Index ausserhalb der Stückliste liegt
     */
    private String getSeqNrAtPartlistUnfilteredIndex(int index, List<EtkDataPartListEntry> unfilteredPartListEntries) {
        if (unfilteredPartListEntries != null) {
            if ((index >= 0) && (index < unfilteredPartListEntries.size())) {
                return unfilteredPartListEntries.get(index).getFieldValue(iPartsConst.FIELD_K_SEQNR);
            }
        }
        return "";
    }

    /**
     * Fügt die übergebenen Stücklisteneinträge nach {@code index} in der Stückliste ein. {@code index} bezieht sich
     * auf die position in der ungefilterten Stückliste
     *
     * @param partListEntries           Stücklisteneinträge, die verschoben werden sollen
     * @param index                     Neuer Index für die zu verschiebenden Stücklisteneinträge
     * @param unfilteredPartListEntries Alle ungefilterten Stücklisteneinträge
     */
    private void insertPartlistEntriesAfterIndex(List<EtkDataPartListEntry> partListEntries, int index, List<EtkDataPartListEntry> unfilteredPartListEntries) {
        if (index < -1) {
            return;
        }
        String seqNrStart = getSeqNrAtPartlistUnfilteredIndex(index, unfilteredPartListEntries);
        String seqNrEnd = getSeqNrAtPartlistUnfilteredIndex(index + 1, unfilteredPartListEntries);

        if (seqNrEnd.equals(seqNrStart)) {
            // hier soll zwischen zwei gleiche Sequenznummern eingefügt werden. Das kann nur schief gehen
            fixPartlistEntriesSameSequenceNumber(index + 1);
            seqNrEnd = getSeqNrAtPartlistUnfilteredIndex(index + 1, unfilteredPartListEntries);
        }

        for (EtkDataPartListEntry partListEntry : partListEntries) {
            String seqNrBetween = SortBetweenHelper.getSortBetween(seqNrStart, seqNrEnd);
            partListEntry.setFieldValue(iPartsConst.FIELD_K_SEQNR, seqNrBetween, DBActionOrigin.FROM_EDIT);
            seqNrStart = seqNrBetween;
        }
    }

    private void fixPartlistEntriesSameSequenceNumber(int endIndex) {
        // vom aktuellen Stücklisteneintrag nach unten den ersten Eintrag suchen, der eine andere Sequenznummer hat
        // dann bei allen dazwischen liegenden Einträgen die Sequenznummer korrigieren, so dass wieder Platz dazwischen ist
        if ((getConnector() != null) && (getConnector().getCurrentPartListEntries() != null)) {
            List<EtkDataPartListEntry> partListEntries = getConnector().getCurrentPartListEntries();
            if (endIndex < partListEntries.size()) {
                String endIndexSeqNr = partListEntries.get(endIndex).getFieldValue(iPartsConst.FIELD_K_SEQNR);
                int index = endIndex;
                String seqNr = endIndexSeqNr;
                while ((index < (partListEntries.size() - 1)) && seqNr.equals(endIndexSeqNr)) {
                    index++;
                    seqNr = partListEntries.get(index).getFieldValue(iPartsConst.FIELD_K_SEQNR);
                }

                String finalSeqNr;
                //sind wir am Ende der Stückliste angekommen, dann muss die finalSeqNr leer sein
                if (seqNr.equals(endIndexSeqNr)) {
                    finalSeqNr = "";
                } else {
                    finalSeqNr = seqNr;
                    index--; // der zuletzt betrachtete Stücklisteneintrag behält seine bisher schon eindeutige Sequenznummer
                }

                String startSeqNr = endIndexSeqNr;
                for (int i = endIndex; i <= index; i++) {
                    String correctedSeqNr = SortBetweenHelper.getSortBetween(startSeqNr, finalSeqNr);
                    partListEntries.get(i).setFieldValue(iPartsConst.FIELD_K_SEQNR, correctedSeqNr, DBActionOrigin.FROM_EDIT);
                    startSeqNr = correctedSeqNr;
                }
            }
        }
    }


    private void savePartlistToChangesetAfterEntryMoved() {
        getConnector().getCurrentAssembly().clearPartListEntriesForEdit();

        // Den AssemblyCache für das aktuelle Changeset und Assembly löschen
        EtkDataAssembly.removeDataAssemblyFromCache(getProject(), getConnector().getCurrentAssembly().getAsId());

        // Änderungen ins Changeset gespeichert werden
        getConnector().savePartListEntries(assemblyListForm, true);

        // dataChanged()-Aufruf ist nur nötig, wenn die Sequenznummer sichtbar ist, sonst reicht auch das günstigere Sortieren der Tabelle
        if (assemblyListForm.getDisplayFields().contains(TABLE_KATALOG, EtkDbConst.FIELD_K_SEQNR, false)) {
            getConnector().dataChanged(null);
        } else {
            assemblyListForm.getPartListTable().sortRowsAccordingToColumn(-1, true, true);
        }

        // Scrolle zu der ersten Selektion, aber zeige eine Row vorher noch an, damit man den Eintrag vor dem Eingefügten sieht
        // Besser wäre, wenn nur gescrollt wird, wenn die Selektion nicht mehr sichtbar ist
        GuiTableRow row = assemblyListForm.getPartListTable().getSelectedRow();
        if (row != null) {
            int index = assemblyListForm.getPartListTable().getRowIndex(row);
            index = Math.max(0, index - 1);
            assemblyListForm.getPartListTable().scrollToCell(index, 0);
            if (!assemblyListForm.getPartListTable().isRowCurrentlyDisplayed(row)) {
                // Es ist nicht sichtbar, wahrschinlich weil wir beim Übergang zu einer anderen Seite sind und dann dürfen wir
                // nicht auf die Zeile davor scrollen, weil unsere 100 z.B. schon auf der nächsten Seite ist, wir aber auf 99 gescrollt haben
                // Deshalb einfach nochmal auf unsere direkt selektierte scrollen
                index = assemblyListForm.getPartListTable().getRowIndex(row);
                assemblyListForm.getPartListTable().scrollToCell(index, 0);
            }
        }
    }

    /**
     * Eine Stücklistenposition für den Edit entsperren
     *
     * @param event
     */
    private void doUnlockPartListEntriesForEdit(Event event) {
        if (!isEditAllowed()) {
            iPartsLockEntryHelper.setPartListEntryLockValue(getConnector(), assemblyListForm.getSelectedEntries(), false);
            enableButtons();
        }
    }

    /**
     * Eine Stücklistenposition für den Edit sperren
     *
     * @param event
     */
    private void doLockPartListEntriesForEdit(Event event) {
        if (!isEditAllowed()) {
            iPartsLockEntryHelper.setPartListEntryLockValue(getConnector(), assemblyListForm.getSelectedEntries(), true);
            enableButtons();
        }
    }

    private void doCopyEntryToOtherSaModule(Event event) {
        duplicateAndMoveEntriesToOtherPartList(false);
    }

    private void doRelocateEntryToOtherPartlist(Event event) {
        duplicateAndMoveEntriesToOtherPartList(true);
    }

    private void doCopyEntryToOtherPartList(Event event) {
        duplicateAndMoveEntriesToOtherPartList(false);
    }

    /**
     * Dupliziert die selektierten Stücklistenpositionen und legt sie in einer vom Benutzer ausgewählten Stückliste ab
     *
     * @param deleteOriginalEntries true/false
     */
    private void duplicateAndMoveEntriesToOtherPartList(final boolean deleteOriginalEntries) {
        if (!isRevisionChangeSetActiveForEdit()) {
            // Kann eigentlich nicht passieren, weil die Buttons im Nicht-Edit verriegelt sind
            MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
            return;
        }

        // Das aktuelle Modul
        final EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
        // Die ausgewählte Stücklistenpositionen
        final List<EtkDataPartListEntry> selectedPartListEntries = getConnector().getSelectedPartListEntries();

        // Liste mit allen Objekten, die am Ende gespeichert werden sollen
        final EtkDataObjectList objectsToStore = new GenericEtkDataObjectList();
        final Set<AssemblyId> modifiedAssemblies = new HashSet<>();
        final boolean deleteExistingEntries = deleteOriginalEntries && !selectedPartListEntries.isEmpty();
        if ((currentAssembly instanceof iPartsDataAssembly) && !selectedPartListEntries.isEmpty()) {
            iPartsDataAssembly dataAssembly = (iPartsDataAssembly)currentAssembly;
            iPartsDocumentationType documentationType = dataAssembly.getDocumentationType();
            // Handelt es sich um eine DIALOG (bzw. konkreter PKW) oder eine nicht-DIALOG Stückliste
            final boolean isDialogAssembly = documentationType.isPKWDocumentationType();
            // 1. Ziel-Stückliste bestimmen
            ResultGetOrCreateTargetAssembly resultGetOrCreateTargetAssembly;
            // Aus einer freien SA kann man bisher nur in eine freie SA Positionen kopieren
            if (dataAssembly.isSAAssembly()) {
                resultGetOrCreateTargetAssembly = getOrCreateTargetSAAssembly(modifiedAssemblies);
            } else {
                resultGetOrCreateTargetAssembly = getOrCreateTargetAssembly(dataAssembly, modifiedAssemblies, documentationType,
                                                                            selectedPartListEntries, deleteOriginalEntries);
            }
            if (resultGetOrCreateTargetAssembly == null) {
                return;
            }
            EtkDataPartListEntry nextSelectedPartlistEntry = null;
            EtkDataAssembly targetDataAssembly = resultGetOrCreateTargetAssembly.getTargetAssembly();
            if (targetDataAssembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly targetAssembly = (iPartsDataAssembly)targetDataAssembly;
                if (targetAssembly.getAsId().equals(dataAssembly.getAsId())) {
                    // wenn im gleichen TU kopiert/gelöscht wird, muss TU nicht mehr geladen werden
                    resultGetOrCreateTargetAssembly.resetIsTUtoOpen();
                } else {
                    if (deleteExistingEntries) {
                        nextSelectedPartlistEntry = calcNextSelectRowAfterDelete();
                    }
                }
                String placeHolder = (selectedPartListEntries.size() > 1) ? TranslationHandler.translate("!!Teilepositionen")
                                                                          : TranslationHandler.translate("!!Teileposition");
                String textForWaitingDialog = deleteOriginalEntries ? TranslationHandler.translate("!!Verschiebe %1", placeHolder) : TranslationHandler.translate("!!Kopiere %1", placeHolder);
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm(textForWaitingDialog,
                                                                               "!!Bitte warten...", null, true);
                messageLogForm.disableButtons(true);
                RELOCATE_ACTION finalRelocateAction = deleteOriginalEntries ? RELOCATE_ACTION.MOVE : RELOCATE_ACTION.COPY;
                messageLogForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        try {
                            // 2. Positionen kopieren
                            dataAssembly.loadAllDataCombTextListsForPartList(); // Für bessere Performance beim Kopieren
                            // Konfiguration für das Verlagern erstellen
                            RelocateEntriesConfig relocateEntriesConfig
                                    = RelocateEntriesConfig.createConfig(!modifiedAssemblies.contains(targetAssembly.getAsId()), isDialogAssembly)
                                    .setCorrectSAAValidities(true);
                            if (relocateEntries(targetAssembly, selectedPartListEntries, objectsToStore,
                                                relocateEntriesConfig, finalRelocateAction, getProject())) {
                                // Erzeugte Objekte dem Edit ChangeSet hinzufügen
                                if (!objectsToStore.isEmpty() && isRevisionChangeSetActiveForEdit()) {
                                    addDataObjectListToActiveChangeSetForEdit(objectsToStore);
                                }
                                // 3. Positionen löschen
                                if (deleteExistingEntries) {
                                    deletePartListEntries(selectedPartListEntries, DELETE_TYPE.ONLY_TU, false);
                                }

                                // 4. Geöffnete Module neu laden (ThreadSafe laden)
                                Session.invokeThreadSafeInSession(() -> {
                                    if (!modifiedAssemblies.isEmpty()) {
                                        iPartsEditPlugin.reloadModulesInEdit(modifiedAssemblies, getConnector());
                                    }
                                    if (resultGetOrCreateTargetAssembly.isTUtoOpen()) {
                                        String kvari = targetAssembly.getAsId().getKVari();
                                        EditModuleForm editModuleForm = EditTransferToASHelper.getEditModuleForm(getConnector());
                                        if ((editModuleForm != null) && (!editModuleForm.isModuleLoaded(kvari) ||
                                                                         !modifiedAssemblies.contains(targetAssembly.getAsId()))) {
                                            GuiWindow.showWaitCursorForRootWindow(true);
                                            try {
                                                editModuleForm.loadModule(kvari, null, false, false);
                                            } finally {
                                                GuiWindow.showWaitCursorForRootWindow(false);
                                            }
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Error while copying/relocating objects from partlist \""
                                                                                      + ((currentAssembly != null) ? currentAssembly.getAsId().getKVari() : "?")
                                                                                      + "\" to other partlist \""
                                                                                      + ((targetAssembly != null) ? targetAssembly.getAsId().getKVari() : "?")
                                                                                      + "\"");
                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                            MessageDialog.showError("!!Es ist ein Fehler aufgetreten.", textForWaitingDialog);
                        } finally {
                            dataAssembly.clearAllDataCombTextListsForPartList();
                        }
                        messageLogForm.closeWindowIfNotAutoClose(ModalResult.OK);
                    }
                });
                if (deleteExistingEntries) {
                    modifiedAssemblies.add(currentAssembly.getAsId());
                }

                // Andere EtkProjects und Cluster benachrichtigen
                if (!modifiedAssemblies.isEmpty()) {
                    iPartsDataChangedEventByEdit<AssemblyId> modulesChangedEvent = new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                                      iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                      modifiedAssemblies, false);
                    if (isRevisionChangeSetActiveForEdit()) { // Events nur im aktuellen Projekt feuern
                        getProject().fireProjectEvent(modulesChangedEvent, true);
                        getProject().fireProjectEvent(new DataChangedEvent(null), true);
                    } else { // Events in allen Projekten und Clustern feuern
                        ApplicationEvents.fireEventInAllProjectsAndClusters(modulesChangedEvent, false, true, true, null, null);
                        ApplicationEvents.fireEventInAllProjectsAndClusters(new DataChangedEvent(null), false, true, true, null, null);
                    }
                }
                if (nextSelectedPartlistEntry != null) {
                    assemblyListForm.gotoPartListEntry(nextSelectedPartlistEntry, true);
                }
            }
        }
    }

    /**
     * Liefert das {@link ResultGetOrCreateTargetAssembly} für eine Ziel-Stückliste in einer freien SA
     *
     * @param modifiedAssemblies
     * @return
     */
    private ResultGetOrCreateTargetAssembly getOrCreateTargetSAAssembly(Set<AssemblyId> modifiedAssemblies) {
        // Dialog erzeugen und anzeigen
        SelectSearchGridSA selectSearchGridSA = new SelectSearchGridSA(this);
        EtkDisplayFields displayFields = selectSearchGridSA.getDisplayResultFields();
        // Das Modul als Extra-Feld hinzufügen
        if (displayFields.contains(TABLE_DA_SA_MODULES, FIELD_DSM_MODULE_NO, false)) {
            EtkDisplayField moduleField = new EtkDisplayField(TABLE_DA_SA_MODULES, FIELD_DSM_MODULE_NO, false, false);
            moduleField.loadStandards(getConfig());
            displayFields.addFeld(1, moduleField);
        }
        selectSearchGridSA.setDisplayResultFields(displayFields);
        selectSearchGridSA.setTitle("!!SA-TU Verortung festlegen");
        // Checkbox zum Öffnen des TUs als extra GUI Anzeige implementieren
        OnExtendSaSelection extendSaSelection = new OnExtendSaSelection();
        selectSearchGridSA.setAutoSelectSingleSearchResult(true);
        selectSearchGridSA.setOnExtendFormEvent(extendSaSelection);
        String saNumber = selectSearchGridSA.showGridSelectionDialog(null);
        if (StrUtils.isValid(saNumber)) {
            // Modulnummer über den Cache bestimmen
            iPartsSA sa = iPartsSA.getInstance(getProject(), new iPartsSAId(saNumber));
            if ((sa.getModuleId() != null) && sa.getModuleId().isValidId()) {
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), sa.getModuleId());
                if (assembly.existsInDB()) {
                    // Zielmodul neu laden, wenn es im Edit manuell geöffnet wurde
                    modifiedAssemblies.add(assembly.getAsId());
                    return new ResultGetOrCreateTargetAssembly(assembly, extendSaSelection.getOpenSaCheckbox().isSelected());
                }
            }
        }
        return null;
    }

    /**
     * Hilfsklasse um den Auswahldialog für freie SAs zu erweitern, z.B. die Checkbox, ob der
     * TU geöffnet werden soll
     */
    private static class OnExtendSaSelection implements OnExtendFormEvent {

        private GuiCheckbox openSaCheckbox;

        @Override
        public AbstractGuiControl getExtensionElement(AbstractJavaViewerForm parentForm) {
            openSaCheckbox = new GuiCheckbox();
            openSaCheckbox.__internal_setGenerationDpi(120);
            openSaCheckbox.registerTranslationHandler(TranslationHandler.getUiTranslationHandler());
            openSaCheckbox.setScaleForResolution(true);
            openSaCheckbox.setMinimumWidth(10);
            openSaCheckbox.setMinimumHeight(10);
            openSaCheckbox.setVisible(true);
            openSaCheckbox.setName("checkbox_open_SA");
            openSaCheckbox.setText("!!Nach dem Speichern freie SA zur Bearbeitung öffnen");
            openSaCheckbox.setSelected(true);
            openSaCheckbox.setPadding(8);
            return openSaCheckbox;
        }

        @Override
        public boolean checkOkButtonResult(boolean enabled) {
            return enabled;
        }

        @Override
        public void selectionChanged(DBDataObjectAttributesList newSelection) {

        }

        public GuiCheckbox getOpenSaCheckbox() {
            return openSaCheckbox;
        }
    }

    // Für die Steuerung, ob die Positionen kopiert oder verschoben wurden.
    // Unterscheidung im Edit zwischen "Stücklisteneintrag in anderen TU kopieren" oder "... verschieben".
    public enum RELOCATE_ACTION {
        COPY, MOVE;
    }

    /**
     * Kopiert die selektierten Einträge (samt referenzierten DataObjects) in die übergebene Ziel-Stückliste
     *
     * @param targetAssembly
     * @param selectedPartListEntries
     * @param objectsToStore
     * @param project
     * @return War das Kopieren erfolgreich?
     */
    public static boolean relocateEntries(iPartsDataAssembly targetAssembly, List<EtkDataPartListEntry> selectedPartListEntries,
                                          EtkDataObjectList objectsToStore, RelocateEntriesConfig relocateEntriesConfig,
                                          RELOCATE_ACTION relocateAction, EtkProject project) {
        if (project.isRevisionChangeSetActiveForEdit()) {
            project.startPseudoTransactionForActiveChangeSet(true);
        }
        try {
            boolean assemblyIsNew = relocateEntriesConfig.isAssemblyIsNew();
            boolean isPKWAssembly = relocateEntriesConfig.isPKWAssembly();
            DBDataObjectList<EtkDataPartListEntry> destPartList = EditModuleHelper.getDestPartList(targetAssembly, assemblyIsNew);
            if (destPartList == null) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Error while determining parts list for target assembly \""
                                                                          + ((targetAssembly != null) ? targetAssembly.getAsId().toStringForLogMessages() : "")
                                                                          + "\"!");

                return false;
            }
            // die Ziel-Stückliste für schnelleren Zugriff als Map mit der sourceGUID als Schlüssel ablegen
            // wird unter anderem verwendet um zu prüfen ob der Eintrag schon übernommen wurde
            HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap = new HashMap<>();
            // Map mit für unterschiedliche HotSpot-Vorschläge
            HashMap<String, String> hotspotSuggestions = new HashMap<>();

            // Berechnete Werte für die Sequenznummer, den maximalen Hotspot und die laufender Nr (Stückliste)
            VarParam<String> calculatedSeqNo = new VarParam<>("");
            VarParam<String> calculatedMaxHotSpot = new VarParam<>("");
            VarParam<Integer> calculatedDestLfdNr = new VarParam<>(0);

            // Map von GenVO nummer auf Hotspot der Zielstückliste anlegen
            Map<String, String> genVoHotspots = new HashMap<>();

            // Vorverarbeitung der Ziel- sowie der aktuellen Stückliste
            EditModuleHelper.preprocessDestPartListEntries(destPartList, calculatedDestLfdNr, calculatedSeqNo, calculatedMaxHotSpot,
                                                           destPartListSourceGUIDMap, hotspotSuggestions, null, null,
                                                           genVoHotspots);

            // Map mit allen Ersetzungen, die zwischen den Positionen in der Liste der selektierten Positionen erzeugt
            // werden können bzw. aus der Konstruktion kommen
            Collection<iPartsReplacement> validReplacements = checkRelocateReplacements(selectedPartListEntries);

            // Bei der Bestimmung von maxHotspot müssen auch die zu übernehmenden neuen Stücklisteneinträge berücksichtigt werden
            for (EtkDataPartListEntry selectedEntry : selectedPartListEntries) {
                String pos = selectedEntry.getFieldValue(FIELD_K_POS);
                if (StrUtils.isInteger(pos) && SortBetweenHelper.isGreater(pos, calculatedMaxHotSpot.getValue())) {
                    calculatedMaxHotSpot.setValue(pos);
                }
            }
            // maxHotspot um den Default-Faktor erhöhen
            calculatedMaxHotSpot.setValue(EditModuleHelper.incrementMaxHotspotWithDefaultIncrement(calculatedMaxHotSpot.getValue()));
            // Liste mit allen erzeugten Stücklistenpositionen
            List<EtkDataPartListEntry> createdPartListEntries = new ArrayList<>();
            // Liste mit allen erzeugten kombinierten Texte
            iPartsDataCombTextList combTextObjectsToStore = new iPartsDataCombTextList();
            // Liste mit allen erzeugten Werksdaten
            iPartsDataFactoryDataList factoryDataObjectsToStore = new iPartsDataFactoryDataList();
            // Liste mit allen Fußnotenreferenzen
            iPartsDataFootNoteCatalogueRefList fnReferencesToStore = new iPartsDataFootNoteCatalogueRefList();
            // Map mit Original-ID aus der Ausgangsstückliste auf neue Stücklistenposition in der Ziel-Stückliste
            Map<PartListEntryId, EtkDataPartListEntry> replacementMapping = new HashMap<>();

            iPartsProductId sourceProductId = null;
            iPartsProductId targetProductId = targetAssembly.getProductIdFromModuleUsage();

            Set<String> allTargetProductModelSAAValidities = null;

            // Durchlaufe alle selektierten Stücklistenpositionen
            for (EtkDataPartListEntry selectedEntry : selectedPartListEntries) {
                // Hotspot der selektierten Position (später Hotspot der neuen Position)
                String hotSpot = selectedEntry.getFieldValue(FIELD_K_POS);
                // BCTE Schlüssel der selektierten Position (später BCTE Schlüssel der neuen Position)
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(selectedEntry);
                // Bei DIALOG: Wenn der Hotspot leer ist, einen Vorschlag ermitteln
                String genVo = null;
                if (StrUtils.isEmpty(hotSpot) && isPKWAssembly) {
                    genVo = selectedEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO);
                    hotSpot = EditModuleHelper.getSuggestedHotSpot(hotspotSuggestions, bctePrimaryKey, calculatedMaxHotSpot, project, genVoHotspots, genVo);
                }
                // Source GUID der selektierten Position (später Source GUID der neuen Position)
                String sourceGUID = selectedEntry.getFieldValue(FIELD_K_SOURCE_GUID);
                // Check, ob die Position bezüglich Hotspot und BCTE Schlüssel schon in der Zielstückliste existiert. Bei neu
                // erzeugten oder nicht-DIALOG Stücklisten muss der Check nicht gemacht werden.
                if (!assemblyIsNew && isPKWAssembly && EditModuleHelper.partListEntryAlreadyExistInDestPartList(destPartListSourceGUIDMap, sourceGUID, hotSpot)) {
                    continue;
                }

                // In der Map sourcePLEIdToTargetPLEIdMap nach einer zum Ziel-Modul passenden laufenden Nummer im Ziel-Modul
                // suchen für den Quell-Stücklisteneintrag
                Integer destLfdNr = null;
                if (relocateEntriesConfig.hasSourcePLEIdToTargetPLEIdMap()) {
                    List<PartListEntryId> partListEntryIds = relocateEntriesConfig.getSourcePLEIdToTargetPLEIdMap().get(selectedEntry.getAsId());
                    if (partListEntryIds != null) {
                        for (PartListEntryId partListEntryId : partListEntryIds) {
                            if (partListEntryId.getOwnerAssemblyId().equals(targetAssembly.getAsId())) {
                                // Prüfen, ob die laufende Nummer noch frei ist im Ziel-Modul
                                if (targetAssembly.getPartListEntryFromKLfdNrUnfiltered(partListEntryId.getKLfdnr()) == null) {
                                    // Laufende Nummer reservieren
                                    int reservedLfdNr = iPartsDataReservedPKList.tryToReserveKLfdNr(project, partListEntryId);
                                    if (reservedLfdNr != -1) {
                                        destLfdNr = reservedLfdNr;

                                        // Aktuell höchste laufende Nummer calculatedDestLfdNr im Ziel-Modul korrigieren falls notwendig
                                        if (destLfdNr > calculatedDestLfdNr.getValue()) {
                                            calculatedDestLfdNr.setValue(destLfdNr);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }

                if (destLfdNr == null) {
                    // DAIMLER-9238: laufende Nummer hochzählen und reservieren
                    calculatedDestLfdNr.setValue(iPartsDataReservedPKList.getAndReserveNextKLfdNr(project, targetAssembly.getAsId(), calculatedDestLfdNr.getValue()));
                    destLfdNr = calculatedDestLfdNr.getValue();
                }

                // ID der neuen Stücklistenposition
                PartListEntryId destPartListEntryId = new PartListEntryId(targetAssembly.getAsId().getKVari(), targetAssembly.getAsId().getKVer(),
                                                                          EtkDbsHelper.formatLfdNr(destLfdNr));
                // Objekt erzeugen
                EtkDataPartListEntry newEntry = EtkDataObjectFactory.createDataPartListEntry(project, destPartListEntryId);
                // Zur Sicherheit. Kann aber nicht passieren, da wir ja schon eine neue laufende Nummer bestimmt haben
                if (newEntry.existsInDB()) {
                    continue;
                } else {
                    newEntry.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                }

                // Attribute der selektierten Position übernehmen
                newEntry.assignAttributesValues(project, selectedEntry.getAttributes(), false, DBActionOrigin.FROM_EDIT);

                // Flag "Automatsch erzeugt" setzen
                if (isPKWAssembly) {
                    // Nur noch beim Kopieren auf 'true' setzen, beim Verschieben soll das Flag auf 'false' gesetzt werden.
                    if (relocateAction.equals(RELOCATE_ACTION.COPY)) {
                        newEntry.setFieldValueAsBoolean(iPartsConst.FIELD_K_AUTO_CREATED, true, DBActionOrigin.FROM_EDIT);
                        newEntry.setFieldValueAsBoolean(iPartsConst.FIELD_K_WAS_AUTO_CREATED, true, DBActionOrigin.FROM_EDIT);
                    } else { // RELOCATE_ACTION.MOVE
                        // K_WAS_AUTO_CREATED wird beim assignAttributesValues() übernommen und beim Verschieben nicht mehr geändert.
                        // Auf alle Fälle K_AUTO_CREATED auf false setzten
                        iPartsDataPartListEntry.resetAutoCreatedFlag(newEntry);
                    }
                } else {
                    // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden
                    iPartsDataPartListEntry.resetAutoCreatedFlag(newEntry);
                }

                // richtige ID setzen sonst bleibt die übernommene Id erhalten
                newEntry.setId(destPartListEntryId, DBActionOrigin.FROM_EDIT);
                newEntry.updateOldId();

                // Bei NICHT-DIALOG-Stücklisten muss die K_SOURCE_GUID auf das neue Modul+LfdNr geändert werden
                if (!isPKWAssembly) {
                    newEntry.setFieldValue(FIELD_K_SOURCE_GUID, EditConstructionToRetailHelper.createNonDIALOGSourceGUID(destPartListEntryId),
                                           DBActionOrigin.FROM_EDIT);
                }

                if (!validReplacements.isEmpty()) {
                    replacementMapping.put(selectedEntry.getAsId(), newEntry);
                }
                // Hotspot und Sequenznummer setzen
                EditModuleHelper.setHotSpotAndNextSequenceNumber(newEntry, hotSpot, sourceGUID, destPartList, calculatedSeqNo.getValue(), createdPartListEntries);

                // wenn hier ein Hotspot Vorschlag ermittelt wurde muss dieser auch zur GenVo Liste hinzugefügt werden
                EditModuleHelper.addToGenVoHotspotSuggestions(genVoHotspots, hotSpot, genVo);

                // Ist das Ziel-Produkt ein anderes Produkt als das Quell-Produkt?
                if ((sourceProductId == null) && (selectedEntry.getOwnerAssembly() instanceof iPartsDataAssembly)) {
                    sourceProductId = ((iPartsDataAssembly)selectedEntry.getOwnerAssembly()).getProductIdFromModuleUsage();
                }
                boolean copyToOtherProduct = !Utils.objectEquals(sourceProductId, targetProductId);

                // Kopiere alle referenzierten Objekte (kombinierte Texte und Fußnoten) inkl. echtes Klonen für alle Array-Attribute
                // Bei Nicht-DIALOG-Stücklisten und demselben Ziel-Produkt wie die Quelle sowie bei PSK-Ziel-Produkten die
                // Werkseinsatzdaten auch kopieren
                iPartsProduct targetProduct = null;
                boolean isPSKTargetProduct = false;
                // Ist ein Zielprodukt nicht vorhanden, muss das Kopieren trotzdem möglich sein, z.B. in freie SAs
                if (targetProductId != null) {
                    targetProduct = iPartsProduct.getInstance(project, targetProductId);
                    isPSKTargetProduct = targetProduct.isPSK();
                }
                boolean copyFactoryData = !isPKWAssembly && (!copyToOtherProduct || isPSKTargetProduct);
                copyReferencedObjects(selectedEntry, newEntry, combTextObjectsToStore, fnReferencesToStore, factoryDataObjectsToStore,
                                      copyFactoryData, project);

                // Baumuster-Gültigkeiten entfernen falls das Ziel-Produkt ein anderes ist als das Quell-Produkt und das
                // Ziel-Produkt kein PSK-Produkt ist
                if (copyToOtherProduct && !isPSKTargetProduct) {
                    newEntry.setFieldValueAsArray(FIELD_K_MODEL_VALIDITY, null, DBActionOrigin.FROM_EDIT);
                }
                if (targetAssembly.isSAAssembly()) {
                    // Beim Kopieren von Stücklistenpositionen in eine freie SA müssen die SAA/BK Gültigkeiten geleert werden
                    newEntry.setFieldValueAsArray(FIELD_K_SA_VALIDITY, null, DBActionOrigin.FROM_EDIT);
                } else if (relocateEntriesConfig.isCorrectSAAValidities() && copyToOtherProduct && !isPSKTargetProduct) {
                    // SAA-Gültigkeiten bzgl. aller SAA-Gültigkeiten der Baumuster vom Ziel-Produkt korrigieren (nur beim Kopieren
                    // in ein anderes Produkt und nicht beim Kopieren in ein PSK-Produkt)
                    EtkDataArray saaValidities = EtkDataArray.getNullForEmptyArray(newEntry.getFieldValueAsArray(FIELD_K_SA_VALIDITY));
                    if (saaValidities != null) {
                        if ((allTargetProductModelSAAValidities == null) && (targetProduct != null)) {
                            allTargetProductModelSAAValidities = targetProduct.getAllModelSAAValidities(project);
                        }
                        List<String> saaValiditiesAsList = saaValidities.getArrayAsStringList();
                        List<String> validSAAValidities = new ArrayList<>(saaValiditiesAsList.size());
                        for (String saaValidity : saaValiditiesAsList) {
                            if (allTargetProductModelSAAValidities.contains(saaValidity)) {
                                validSAAValidities.add(saaValidity);
                            }
                        }

                        // Mussten die SAA-Gültigkeiten korrigiert werden?
                        if (validSAAValidities.size() != saaValiditiesAsList.size()) {
                            saaValidities.clear(false);
                            saaValidities.add(validSAAValidities);
                            newEntry.setFieldValueAsArray(FIELD_K_SA_VALIDITY, saaValidities, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }

                // Referenz zur Quelle inkl. Zeitstempel setzen?
                if (relocateEntriesConfig.isSetSourceReferenceAndTimeStamp()) {
                    // Das Feld T_STAMP ist sicherlich nicht in den Attributen vom selectedEntry enthalten -> Loggen von
                    // loadFieldIfNeeded temporär deaktivieren
                    boolean oldLogLoadFieldIfNeeded = selectedEntry.isLogLoadFieldIfNeeded();
                    selectedEntry.setLogLoadFieldIfNeeded(false);
                    try {
                        newEntry.setFieldValue(FIELD_K_COPY_DATE, selectedEntry.getFieldValue(FIELD_STAMP), DBActionOrigin.FROM_EDIT);
                        newEntry.setFieldValue(FIELD_K_COPY_VARI, selectedEntry.getAsId().getKVari(), DBActionOrigin.FROM_EDIT);
                        newEntry.setFieldValue(FIELD_K_COPY_LFDNR, selectedEntry.getAsId().getKLfdnr(), DBActionOrigin.FROM_EDIT);
                    } finally {
                        selectedEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
                    }
                }

                // Frühere Variantengültigkeit beibehalten
                if (relocateEntriesConfig.hasSourcePLEIdToTargetVariantValidityMap()) {
                    EtkDataArray variantValidity = relocateEntriesConfig.getSourcePLEIdToTargetVariantValidityMap().get(selectedEntry.getAsId());
                    setArrayValuesAndId(newEntry, FIELD_K_PSK_VARIANT_VALIDITY, variantValidity, newEntry.getAsId().toString("|"),
                                        project);
                }

                // Früheres Unterdrückt-Flag beibehalten (falls das Unterdrückt-Flag nicht sowieso schon aus der Quelle
                // gesetzt wurde)
                if (relocateEntriesConfig.hasSourcePLEIdToTargetOmitFlagSet()) {
                    if (relocateEntriesConfig.getSourcePLEIdToTargetOmitFlagSet().contains(selectedEntry.getAsId())
                        && !newEntry.getFieldValueAsBoolean(FIELD_K_OMIT)) {
                        newEntry.setFieldValueAsBoolean(FIELD_K_OMIT, true, DBActionOrigin.FROM_EDIT);
                    }
                }
                // Das "für Edit gesperrt Flag" soll nur für die Positionen gesetzt werden, die in der Zielstückliste
                // schon gesperrt sind. Neue Sperrungen aus dem Ziel sollen nicht übernommen werden. Existiert kein
                // mit gesperrten Zielpositionen, dann müssen alle Sperrungen übernommen werden, außer wenn ein TU in
                // ein PSK Produkt übernommen wird. Bei PSK soll initial nichts gesperrt werden.
                if (relocateEntriesConfig.hasSourcePLEIdToTargetEditLockFlagSet()) {
                    Set<PartListEntryId> sourceLockEntryValues = relocateEntriesConfig.getSourcePLEIdToTargetEditLockFlagSet();
                    newEntry.setFieldValueAsBoolean(FIELD_K_ENTRY_LOCKED, sourceLockEntryValues.contains(selectedEntry.getAsId()), DBActionOrigin.FROM_EDIT);
                } else if (relocateEntriesConfig.isAssemblyIsNew() && isPSKTargetProduct) {
                    // Neue Stücklisten in einem PSK Produkt
                    newEntry.setFieldValueAsBoolean(FIELD_K_ENTRY_LOCKED, false, DBActionOrigin.FROM_EDIT);
                }


                // Erzeugung der neuen Position abschließen
                EditModuleHelper.finishPartListEntryCreation(destPartListSourceGUIDMap, sourceGUID, newEntry,
                                                             hotSpot, bctePrimaryKey,
                                                             hotspotSuggestions, createdPartListEntries,
                                                             calculatedDestLfdNr, calculatedSeqNo);

            }

            // Wurden keine neuen Positionen angelegt, kann hier abgebrochen werden
            if (createdPartListEntries.isEmpty()) {
                return true;
            }

            // Setze die WW Kenner bei den verbleibenden Positionen
            checkWWForNewPartList(createdPartListEntries, targetAssembly);

            // Jetzt die neuen Einträge zur Stückliste hinzufügen
            for (EtkDataPartListEntry partListEntry : createdPartListEntries) {
                destPartList.add(partListEntry, DBActionOrigin.FROM_EDIT);
            }

            if (assemblyIsNew || (relocateEntriesConfig.hasSourcePLEIdToTargetPLEIdMap())) {
                // Bei einem neuen Ziel-Modul oder bei vorhandener Map sourcePLEIdToTargetPLEIdMap für den Abgleich von
                // PSK-Modulen mit Serien-Modulen die Sequenznummern sauber neu bestimmen, da diese durch das Einsortieren
                // ansonsten schon sehr unregelmäßig sein können; dafür die Stücklisteneinträge zunächst nach ihrer
                // Sequenznummer sortieren und danach die Sequenznummern neu berechnen
                List<EtkDataPartListEntry> sortedPartListEntries = destPartList.getAsList()
                        .stream()
                        .sorted((o1, o2) -> {
                            String o1SeqNr = o1.getFieldValue(FIELD_K_SEQNR);
                            String o2SeqNr = o2.getFieldValue(FIELD_K_SEQNR);
                            if (o1SeqNr.equals(o2SeqNr)) {
                                return 0;
                            }
                            return SortBetweenHelper.isGreater(o1SeqNr, o2SeqNr) ? 1 : -1;
                        })
                        .collect(Collectors.toList());
                EditDatabaseHelper.reorgSeqenceNumbers(sortedPartListEntries);
            }

            // Retail-Ersetzungen aus Konstruktions-Ersetzungen für neue Stückliste erzeugen
            iPartsDataReplacePartList destDataReplacementsRetail = null;
            iPartsDataIncludePartList destDataIncludePartsRetail = null;
            if (targetAssembly.isSeriesRelevantForImport()) {
                iPartsDIALOGPositionsHelper missingDialogPositionsHelper = new iPartsDIALOGPositionsHelper(null);
                ObjectInstanceLRUList<HmMSmId, iPartsDIALOGPositionsHelper> dialogPositionsHelperMap = new ObjectInstanceLRUList<>(500,
                                                                                                                                   iPartsConst.MAX_CACHE_LIFE_TIME_CORE);
                Set<iPartsReplacementConst> replacementsConstToTransfer = new HashSet<>();
                for (EtkDataPartListEntry partListEntry : selectedPartListEntries) {
                    // DIALOG-Stückliste für den BCTE-Schlüssel des Stücklisteneintrags ermitteln
                    String dialogGUID = partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                    iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogGUID);
                    if (bctePrimaryKey != null) {
                        HmMSmId hmMSmId = bctePrimaryKey.getHmMSmId();
                        iPartsDIALOGPositionsHelper dialogPositionsHelper = dialogPositionsHelperMap.get(hmMSmId);
                        if (dialogPositionsHelper == null) {
                            PartListEntryId constructionPLEId = EditConstructionToRetailHelper.getVirtualConstructionPartlistEntryIdFromRetailPartlistEntry(partListEntry);
                            EtkDataAssembly constructionAssembly = EtkDataObjectFactory.createDataAssembly(project,
                                                                                                           constructionPLEId.getOwnerAssemblyId(),
                                                                                                           false);
                            if (constructionAssembly.existsInDB()) {
                                dialogPositionsHelper = new iPartsDIALOGPositionsHelper(constructionAssembly.getPartListUnfiltered(null));
                                dialogPositionsHelperMap.put(hmMSmId, dialogPositionsHelper);
                            } else {
                                dialogPositionsHelper = missingDialogPositionsHelper;
                                dialogPositionsHelperMap.put(hmMSmId, missingDialogPositionsHelper);
                            }
                        }

                        // Konstruktions-Stücklisteneinträge der KEM-Kette bestimmen
                        if (dialogPositionsHelper != missingDialogPositionsHelper) {
                            EtkDataPartListEntry constructionPLE = dialogPositionsHelper.getPositionVariantByBCTEKey(bctePrimaryKey);
                            if (constructionPLE != null) {
                                Set<EtkDataPartListEntry> constPLEsForKemChain = EditConstructionToRetailHelper.calculateMinMaxKEMDatesWithoutCache(constructionPLE,
                                                                                                                                                    dialogPositionsHelper);
                                replacementsConstToTransfer.addAll(EditTransferToASHelper.getConstructionReplacementsForKemChain(constPLEsForKemChain,
                                                                                                                                 dialogPositionsHelper));
                            }
                        }
                    }
                }

                // Retail-Ersetzungen, die später aus den Konstruktions-Ersetzungen generiert werden
                // Vorbesetzt mit allen Ersetzungen, die schon in der Assembly existieren, um später nicht existierende
                // Ersetzungen zu generieren
                destDataReplacementsRetail = iPartsDataReplacePartList.loadReplacementsForAssembly(project, targetAssembly.getAsId());

                // Retail-Mitlieferteile, die später aus den Konstruktions-Mitlieferteilen generiert werden
                destDataIncludePartsRetail = iPartsDataIncludePartList.loadIncludePartsForAssembly(project, targetAssembly.getAsId());

                // Konstruktions-Ersetzungen als Retail-Ersetzungen zur Stückliste hinzufügen
                EditTransferToASHelper.addConstReplacementsToRetailPartList(replacementsConstToTransfer, destPartList,
                                                                            destPartListSourceGUIDMap, destDataReplacementsRetail,
                                                                            destDataIncludePartsRetail, project);
            }


            // Kopierte Retail-Ersetzungen für neue Stückliste erzeugen
            if (!validReplacements.isEmpty()) {
                createReplacePartsForTargetAssembly(validReplacements, destDataReplacementsRetail, replacementMapping, destPartList,
                                                    objectsToStore, project);
            }

            if (project.isRevisionChangeSetActiveForEdit()) {
                // Wurde die Stückliste im aktuellen Changeset schon erstellt oder modifiziert, dann brauchen wir sie nicht
                // noch einmal als "modifiziert" markieren
                EtkRevisionsHelper revisionsHelper = project.getRevisionsHelper();
                boolean markAssemblyAsModified = !revisionsHelper.checkIfObjectCreatedInActiveEditChangeSet(targetAssembly.getAsId())
                                                 && !revisionsHelper.checkSerializedObjectStateInChangeSet(revisionsHelper.getActiveRevisionChangeSetForEdit(),
                                                                                                           targetAssembly.getAsId(),
                                                                                                           SerializedDBDataObjectState.MODIFIED);
                // Das Kopieren in eine andere Stückliste abschließen
                EditModuleHelper.finishModuleModification(objectsToStore, destPartList, assemblyIsNew,
                                                          targetAssembly, destDataReplacementsRetail, destDataIncludePartsRetail,
                                                          combTextObjectsToStore, fnReferencesToStore, markAssemblyAsModified);
                // Kopierte Werksdaten hinzufügen
                EditModuleHelper.addObjectsIfExist(objectsToStore, factoryDataObjectsToStore);
            } else {
                // Ziel-Stückliste abspeichern und aus dem Cache löschen
                destPartList.saveToDB(project, false);
                EtkDataAssembly.removeDataAssemblyFromCache(project, targetAssembly.getAsId());
            }
        } finally {
            if (project.isRevisionChangeSetActiveForEdit()) {
                project.stopPseudoTransactionForActiveChangeSet();
            }
        }
        return true;
    }

    /**
     * Setzt die Wahlweise Kenner bei den neuen Positionen in der Ziel-Stückliste.
     *
     * @param createdPartListEntries
     * @param targetAssembly
     */
    private static void checkWWForNewPartList(List<EtkDataPartListEntry> createdPartListEntries, EtkDataAssembly targetAssembly) {
        Map<String, List<EtkDataPartListEntry>> wwToEntriesMap = new HashMap<>();
        for (EtkDataPartListEntry entry : createdPartListEntries) {
            if (entry instanceof iPartsDataPartListEntry) {
                String wwSign = entry.getFieldValue(FIELD_K_WW);
                if (StrUtils.isValid(wwSign)) {
                    // Unter den neuen Positionen, die bestimmen, die den gleichen WW Kenner haben
                    List<EtkDataPartListEntry> entriesForWW = wwToEntriesMap.get(wwSign);
                    if (entriesForWW == null) {
                        entriesForWW = new ArrayList<>();
                        wwToEntriesMap.put(wwSign, entriesForWW);
                    }
                    entriesForWW.add(entry);
                }
            }
        }
        if (targetAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly dataAssembly = (iPartsDataAssembly)targetAssembly;
            // Über die Ziel-Stückliste den nächsten Kenner bestimmen
            String nextWWSign = dataAssembly.getNextUnusedWWGUID();
            for (List<EtkDataPartListEntry> entriesWithSameWW : wwToEntriesMap.values()) {
                // Wird für einen WW Kenner nur eine Position unter den neuen Positionen gefunden, dann darf
                // die daraus erzeugte Position in der Zeil-Stückliste den aktuellen Kenner nicht übernehmen.
                // Hier explizit einen leeren Kenner setzen
                if (entriesWithSameWW.size() == 1) {
                    entriesWithSameWW.get(0).setFieldValue(FIELD_K_WW, "", DBActionOrigin.FROM_EDIT);
                    continue;
                }
                // Wurden für einen WW Kenner mehrere Positionen gefunden, dann müssen die daraus erzeugten Positionen
                // in der Ziel-Stückliste einen neuen und für die Ziel-Stückliste gültigen WW Kenner erhalten.
                for (EtkDataPartListEntry entry : entriesWithSameWW) {
                    entry.setFieldValue(FIELD_K_WW, nextWWSign, DBActionOrigin.FROM_EDIT);
                }
                nextWWSign = Integer.toString(Integer.valueOf(nextWWSign) + 1);
            }
        }

    }

    /**
     * Überprüft, ob Ersetzungen aus der Ausgangsstückliste in die Ziel-Stückliste übernommen werden können
     *
     * @param selectedEntries
     * @return
     */
    private static Collection<iPartsReplacement> checkRelocateReplacements(List<EtkDataPartListEntry> selectedEntries) {
        Map<String, iPartsReplacement> validReplacements = new HashMap<>();
        Set<PartListEntryId> selectedEntryIds = new HashSet<>();
        for (EtkDataPartListEntry entry : selectedEntries) {
            if (entry instanceof iPartsDataPartListEntry) {
                selectedEntryIds.add(entry.getAsId());
            }
        }
        // Alle Ersetzungen, die möglich sind, übernehmen
        for (EtkDataPartListEntry entry : selectedEntries) {
            collectReplacementsToRelocate(entry, selectedEntryIds, validReplacements);
        }
        return validReplacements.values();
    }

    /**
     * Erzeugt die Ersetzungen für die neuen Stücklistenpositionen auf Basis der Ersetzungen aus der Konstruktions-Stückliste
     * sowie aus der Ausgangsstückliste.
     *
     * @param validSourceReplacements    Gültige Ersetzungen aus der Quell-Stückliste
     * @param destDataReplacementsRetail Alle existierenden sowie neuen/veränderten Ersetzungen in der Ziel-Stückliste
     * @param replacementMapping
     * @param destPartList
     * @param objectsToStore
     * @param project
     */
    private static void createReplacePartsForTargetAssembly(Collection<iPartsReplacement> validSourceReplacements,
                                                            iPartsDataReplacePartList destDataReplacementsRetail,
                                                            Map<PartListEntryId, EtkDataPartListEntry> replacementMapping,
                                                            DBDataObjectList<EtkDataPartListEntry> destPartList,
                                                            EtkDataObjectList objectsToStore, EtkProject project) {
        if (!replacementMapping.isEmpty()) {
            Map<String, String> destLfdnrToHotspotMap = null;
            iPartsDataReplacePartList newDataReplacementsRetail = new iPartsDataReplacePartList();
            for (iPartsReplacement sourceReplacement : validSourceReplacements) {
                EtkDataPartListEntry successorEntry = sourceReplacement.successorEntry;
                if (!sourceReplacement.isCreatedFromConstructionReplacement() && !sourceReplacement.isVirtual()) { // Manuell angelegte Ersetzungen
                    // Vorgänger aus der Original-Ersetzung
                    EtkDataPartListEntry predecessor = replacementMapping.get(sourceReplacement.predecessorEntry.getAsId());
                    // Hat ein Vorgänger in der Ziel-Stückliste existiert, dann kommt er im Mapping nicht mehr vor.
                    // In diesem Fall kann die Ersetzung in der Ziel-Stückliste nicht erzeugt werden.
                    if (predecessor == null) {
                        continue;
                    }

                    EtkDataPartListEntry successor = null;
                    if (successorEntry != null) {
                        // Nachfolger aus der Original-Ersetzung
                        successor = replacementMapping.get(successorEntry.getAsId());
                        // Hat ein Nachfolger in der Ziel-Stückliste existiert, dann kommt er im Mapping nicht mehr vor.
                        // In diesem Fall kann die Ersetzung in der Ziel-Stückliste nicht erzeugt werden.
                        if (successor == null) {
                            continue;
                        }
                    }

                    // Datenobjekt für die Original-Ersetzung
                    iPartsDataReplacePart existingReplacePart = sourceReplacement.getAsDataReplacePart(project);
                    // ID für die neue Ersetzung (in der Ziel-Stückliste)
                    iPartsReplacePartId replacePartId = new iPartsReplacePartId(predecessor.getAsId(), existingReplacePart.getAsId().getSeqNo());
                    // Datenobjekt für die neue Ersetzung
                    iPartsDataReplacePart newReplacePart = new iPartsDataReplacePart(project, replacePartId);
                    initObjectWithExistingAttributes(newReplacePart, existingReplacePart.getAttributes(), objectsToStore,
                                                     project);
                    String seqNumberSuccessor;
                    if (successor != null) {
                        seqNumberSuccessor = successor.getAsId().getKLfdnr();
                        // Die laufende Nummer des Nachfolgers ist kein Schlüsselattribut, muss daher von Hand gesetzt werden
                        newReplacePart.setFieldValue(FIELD_DRP_REPLACE_LFDNR, seqNumberSuccessor, DBActionOrigin.FROM_EDIT);
                    } else {
                        seqNumberSuccessor = "";
                    }

                    // Mitlieferteile berücksichtigen
                    createIncludePartsForTargetAssembly(sourceReplacement, replacePartId, seqNumberSuccessor, objectsToStore,
                                                        project);
                } else if (sourceReplacement.isCreatedFromConstructionReplacement() && (successorEntry != null)
                           && (destDataReplacementsRetail != null) && !destDataReplacementsRetail.isEmpty()) { // (Veränderte) Konstruktions-Ersetzungen
                    // Vergleiche die Ersetzungen ohne konkrete IDs der Stücklisteneinträge sowie Quelle und Status primär
                    // anhand der BCTE-Schlüssel von Vorgänger und Nachfolger und der RFME-Flags
                    String sourceReplacementCompareKey = sourceReplacement.getCompareKey(false, false, false);
                    for (iPartsDataReplacePart destDataReplacePart : destDataReplacementsRetail) {
                        PartListEntryId successorPartListEntryId = destDataReplacePart.getSuccessorPartListEntryId();
                        if ((successorPartListEntryId != null) && destDataReplacePart.isModified()) {
                            String destReplacementCompareKey = destDataReplacePart.getCompareKey(false, false, false);
                            if (sourceReplacementCompareKey.equals(destReplacementCompareKey)) { // Evtl. passende Ersetzung gefunden
                                if (destLfdnrToHotspotMap == null) {
                                    // Map von laufende Nummer auf Hotspot der Ziel-Stückliste erstellen
                                    destLfdnrToHotspotMap = new HashMap<>();
                                    for (EtkDataPartListEntry partListEntry : destPartList) {
                                        destLfdnrToHotspotMap.put(partListEntry.getAsId().getKLfdnr(), partListEntry.getFieldValue(FIELD_K_POS));
                                    }
                                }

                                // Hotspots von Vorgänger bzw. Nachfolger müssen identisch sein bei den beiden Ersetzungen
                                String sourcePredecessorHotspot = sourceReplacement.predecessorEntry.getFieldValue(FIELD_K_POS);
                                String sourceSuccessorHotspot = successorEntry.getFieldValue(FIELD_K_POS);
                                String destPredecessorHotspot = destLfdnrToHotspotMap.get(destDataReplacePart.getPredecessorPartListEntryId().getKLfdnr());
                                String destSuccessorHotspot = destLfdnrToHotspotMap.get(successorPartListEntryId.getKLfdnr());
                                if (sourcePredecessorHotspot.isEmpty() && sourceSuccessorHotspot.isEmpty()) {
                                    // Spezialfall für leere Hotspots in der Quelle -> diese müssen im Ziel dann identische
                                    // Hotspots haben
                                    if (!Utils.objectEquals(destPredecessorHotspot, destSuccessorHotspot)) {
                                        continue;
                                    }
                                } else {
                                    if (!Utils.objectEquals(sourcePredecessorHotspot, destPredecessorHotspot)) {
                                        continue; // Hotspots vom Vorgänger in Quelle und Ziel unterschiedlich
                                    }
                                    if (!Utils.objectEquals(sourceSuccessorHotspot, destSuccessorHotspot)) {
                                        continue; // Hotspots vom Nachfolger in Quelle und Ziel unterschiedlich
                                    }
                                }

                                // Je nach Quelle eine neue Ersetzung anlegen bzw. den Status der Ersetzung anpassen
                                if (sourceReplacement.source == iPartsReplacement.Source.IPARTS) {
                                    // Neue Ersetzung für die veränderte Konstruktions-Ersetzung erzeugen mit dem Vorgängerstand
                                    // und Nachfolgerstand der dazugehörigen echten Konstruktions-Ersetzung in der Ziel-Stückliste
                                    iPartsDataReplacePart newDataReplacePart = sourceReplacement.getAsDataReplacePart(project);
                                    iPartsReplacePartId newReplacePartId = new iPartsReplacePartId(destDataReplacePart.getPredecessorPartListEntryId(),
                                                                                                   ""); // Zunächst leere Sequenznummer
                                    newDataReplacePart.setId(newReplacePartId, DBActionOrigin.FROM_EDIT);
                                    newDataReplacePart.setFieldValue(FIELD_DRP_REPLACE_LFDNR, successorPartListEntryId.getKLfdnr(),
                                                                     DBActionOrigin.FROM_EDIT);

                                    // Prüfen, ob die identische Ersetzung nicht bereits neu hinzugefügt wurde bzw. sogar
                                    // schon in der Ziel-Stückliste existiert
                                    boolean alreadyExists = false;
                                    for (iPartsDataReplacePart addedDataReplacePart : newDataReplacementsRetail) {
                                        if (newDataReplacePart.isDuplicateOf(addedDataReplacePart)) {
                                            alreadyExists = true;
                                            break;
                                        }
                                    }
                                    if (!alreadyExists) {
                                        for (iPartsDataReplacePart existingDataReplacePart : destDataReplacementsRetail) {
                                            if (newDataReplacePart.isDuplicateOf(existingDataReplacePart)) {
                                                alreadyExists = true;
                                                break;
                                            }
                                        }
                                    }

                                    if (!alreadyExists) {
                                        // Ersetzung mit echter Sequenznummer hinzufügen
                                        String replacementSeqNo = iPartsReplacementHelper.getNextReplacementSeqNo(project, destDataReplacePart.getPredecessorPartListEntryId());
                                        newDataReplacePart.setFieldValue(FIELD_DRP_SEQNO, replacementSeqNo, DBActionOrigin.FROM_EDIT);
                                        newDataReplacePart.updateOldId();
                                        newDataReplacePart.__internal_setNew(true);
                                        newDataReplacementsRetail.add(newDataReplacePart, DBActionOrigin.FROM_EDIT);
                                    }
                                } else if (sourceReplacement.rfmeaFlags.equals(destDataReplacePart.getFieldValue(FIELD_DRP_REPLACE_RFMEA))
                                           && sourceReplacement.rfmenFlags.equals(destDataReplacePart.getFieldValue(FIELD_DRP_REPLACE_RFMEN))
                                           && (destDataReplacePart.getSource() != iPartsReplacement.Source.IPARTS)) {
                                    // Status der identischen Konstruktions-Ersetzung anpassen (RFME-Flags sind identisch
                                    // und Quelle ist nicht iParts)
                                    destDataReplacePart.setFieldValue(FIELD_DRP_STATUS, sourceReplacement.releaseState.getDbValue(),
                                                                      DBActionOrigin.FROM_EDIT);
                                }
                            }
                        }
                    }
                }
            }

            if (destDataReplacementsRetail != null) {
                destDataReplacementsRetail.addAll(newDataReplacementsRetail, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    /**
     * Erzeugt Mitlieferteile Objekte für die neuen Stücklistenpositionen auf Basis der Mitlieferteile aus Ersetzungen
     * aus der Ausgangsstückliste
     *
     * @param replacement
     * @param replacePartId
     * @param seqNumberSuccessor
     * @param objectsToStore
     * @param project
     */
    private static void createIncludePartsForTargetAssembly(iPartsReplacement replacement, iPartsReplacePartId replacePartId,
                                                            String seqNumberSuccessor, EtkDataObjectList objectsToStore,
                                                            EtkProject project) {
        if (replacement.hasIncludeParts(project)) {
            iPartsDataIncludePartList includeParts = replacement.getIncludePartsAsDataIncludePartList(project);
            // Alle Mitlieferteile durchlaufen
            for (iPartsDataIncludePart includePart : includeParts) {
                iPartsIncludePartId existingIncludeId = includePart.getAsId();
                iPartsIncludePartId includePartId
                        = new iPartsIncludePartId(replacePartId.getReplaceVari(), replacePartId.getReplaceVer(),
                                                  replacePartId.getPredecessorLfdNr(), existingIncludeId.getIncludeReplaceMatNr(),
                                                  seqNumberSuccessor, existingIncludeId.getIncludeSeqNo());
                iPartsDataIncludePart newIncludePart = new iPartsDataIncludePart(project, includePartId);
                initObjectWithExistingAttributes(newIncludePart, includePart.getAttributes(), objectsToStore, project);
            }
        }
    }

    /**
     * Sammelt alle Ersetzungen zu den Vorgängern und Nachfolgern des übergebenen zu kopierenden/verschiebenden Stücklisteneintrags auf.
     *
     * @param entry
     * @param selectedEntryIds
     * @param validReplacements
     */
    private static void collectReplacementsToRelocate(EtkDataPartListEntry entry, Set<PartListEntryId> selectedEntryIds,
                                                      Map<String, iPartsReplacement> validReplacements) {
        if (entry instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)entry;

            // Es müssen alle Ersetzungen aus der DB geladen werden, weil auch nicht freigegebene Ersetzungen berücksichtigt
            // werden müssen
            List<iPartsReplacement> allPredecessors = new DwList<>();
            List<iPartsReplacement> allSuccessors = new DwList<>();
            iPartsReplacementHelper.loadReplacementsForPartListEntry(null, allPredecessors, allSuccessors, partListEntry, false);

            if (allPredecessors.isEmpty() && allSuccessors.isEmpty()) {
                return;
            }

            for (iPartsReplacement replacement : allPredecessors) {
                String replacementKey = replacement.getCompareKey(true, true, true);
                if (StrUtils.isEmpty(replacementKey) || validReplacements.containsKey(replacementKey)) {
                    continue;
                }
                if (replacement.isCreatedFromConstructionReplacement()) {
                    // Konstruktions-Ersetzungen müssen für den späteren Abgleich immer in die Map übernommen werden
                    validReplacements.put(replacementKey, replacement);
                } else if (replacement.predecessorEntry instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry predecessor = (iPartsDataPartListEntry)replacement.predecessorEntry;
                    if (selectedEntryIds.contains(predecessor.getAsId())) {
                        validReplacements.put(replacementKey, replacement);
                    }
                }
            }

            for (iPartsReplacement replacement : allSuccessors) {
                String replacementKey = replacement.getCompareKey(true, true, true);
                if (StrUtils.isEmpty(replacementKey) || validReplacements.containsKey(replacementKey)) {
                    continue;
                }
                if (replacement.isCreatedFromConstructionReplacement()) {
                    // Konstruktions-Ersetzungen müssen für den späteren Abgleich immer in die Map übernommen werden
                    validReplacements.put(replacementKey, replacement);
                } else if (replacement.successorEntry == null) {
                    // Ersetzung ohne Nachfolger nur mit Materialnummer müssen ebenfalls in die Map übernommen werden
                    validReplacements.put(replacementKey, replacement);
                } else if (replacement.successorEntry instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry successor = (iPartsDataPartListEntry)replacement.successorEntry;
                    if (selectedEntryIds.contains(successor.getAsId())) {
                        validReplacements.put(replacementKey, replacement);
                    }
                }
            }
        }
    }

    /**
     * Kopiert die referenzierten Objekte eine Stücklistenposition
     *
     * @param selectedEntry
     * @param newEntry
     * @param combTextObjectsToStore
     * @param fnReferencesToStore
     * @param factoryDataObjectsToStore
     * @param copyFactoryData
     * @param project
     */
    private static void copyReferencedObjects(EtkDataPartListEntry selectedEntry, EtkDataPartListEntry newEntry,
                                              iPartsDataCombTextList combTextObjectsToStore,
                                              iPartsDataFootNoteCatalogueRefList fnReferencesToStore,
                                              iPartsDataFactoryDataList factoryDataObjectsToStore,
                                              boolean copyFactoryData, EtkProject project) {
        // Fußnoten
        iPartsDataFootNoteCatalogueRefList fnReferences
                = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntry(project, selectedEntry.getAsId());
        for (iPartsDataFootNoteCatalogueRef fnReference : fnReferences) {
            iPartsFootNoteCatalogueRefId fnRefId = new iPartsFootNoteCatalogueRefId(newEntry.getAsId().getKVari(),
                                                                                    newEntry.getAsId().getKVer(),
                                                                                    newEntry.getAsId().getKLfdnr(),
                                                                                    fnReference.getAsId().getFootNoteId());
            iPartsDataFootNoteCatalogueRef newFnReference = new iPartsDataFootNoteCatalogueRef(project, fnRefId);
            initObjectWithExistingAttributes(newFnReference, fnReference.getAttributes(), fnReferencesToStore, project);
        }

        // kombinierte Texte
        if (selectedEntry instanceof iPartsDataPartListEntry) {
            iPartsDataCombTextList combTexts = ((iPartsDataPartListEntry)selectedEntry).getDataCombTextList();
            for (iPartsDataCombText combText : combTexts) {
                iPartsCombTextId combTextId = new iPartsCombTextId(newEntry.getAsId().getKVari(), newEntry.getAsId().getKVer(),
                                                                   newEntry.getAsId().getKLfdnr(), combText.getAsId().getTextSeqNo());
                iPartsDataCombText newCombText = new iPartsDataCombText(project, combTextId);
                initObjectWithExistingAttributes(newCombText, combText.getAttributes(), combTextObjectsToStore, project);
            }
        }

        // Arrays müssen explizit für die neue Position erzeugt werden
        if (newEntry.getAttributes() != null) {
            Set<String> arrayAttributes = new HashSet<>();
            for (Map.Entry<String, DBDataObjectAttribute> arrayAttribute : selectedEntry.getAttributes().entrySet()) {
                if (arrayAttribute.getValue().getType() == DBDataObjectAttribute.TYPE.ARRAY) {
                    arrayAttributes.add(arrayAttribute.getKey());
                }
            }

            if (!arrayAttributes.isEmpty()) {
                String arrayIdStartValue = newEntry.getAsId().toString("|");
                for (String arrayFieldName : arrayAttributes) {
                    EtkDataArray array = selectedEntry.getFieldValueAsArray(arrayFieldName);
                    setArrayValuesAndId(newEntry, arrayFieldName, array, arrayIdStartValue, project);
                }
            }
        }

        // Werksdaten für Nicht-DIALOG-Stücklisteneinträge müssen für die individuellen nicht-DIALOG-GUIDs kopiert werden
        // falls Quell- und Ziel-Produkt identisch sind bzw. das Ziel-Produkt ein PSK-Produkt ist
        if (copyFactoryData) {
            String sourceGuid = selectedEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
            iPartsDataFactoryDataList factoryData = iPartsDataFactoryDataList.loadFactoryDataGUID(project, sourceGuid);
            if (!factoryData.isEmpty()) {
                iPartsDataFactoryDataList factoryDataList = CopyAndPasteData.copyNonDIALOGFactoryDataOfPartListEntry(newEntry,
                                                                                                                     factoryData,
                                                                                                                     project);
                if (!factoryDataList.isEmpty()) {
                    factoryDataObjectsToStore.addAll(factoryDataList, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    private static void setArrayValuesAndId(EtkDataPartListEntry partListEntry, String arrayFieldName, EtkDataArray array,
                                            String arrayIdStartValue, EtkProject project) {
        if (array == null) {
            return;
        }

        List<String> values = array.getArrayAsStringList();
        if (!values.isEmpty()) {
            // Neue Array-ID bestimmen und setzen (ignoreChangeSets kann hier auf true gesetzt werden, was
            // schneller ist, weil es sich ja um neue Stücklisteneinträge handelt, die in diesem ChangeSet
            // noch nicht existieren können, weswegen es auch die Array-ID noch nicht geben kann)
            String arrayId = project.getDbLayer().getNewArrayNo(TableAndFieldName.make(TABLE_KATALOG, arrayFieldName),
                                                                arrayIdStartValue, true);

            EtkDataArray newArray = new EtkDataArray(arrayId);
            newArray.add(values);
            partListEntry.setFieldValueAsArray(arrayFieldName, newArray, DBActionOrigin.FROM_EDIT);
            partListEntry.setIdForArray(arrayFieldName, arrayId, DBActionOrigin.FROM_EDIT);
        } else {
            partListEntry.setFieldValueAsArray(arrayFieldName, null, DBActionOrigin.FROM_EDIT);
            partListEntry.setIdForArray(arrayFieldName, "", DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Initialisiert das übergebene Objekt mit den übergebenen Werten
     *
     * @param newObject
     * @param attributes
     * @param objectToStoreList
     * @param project
     */
    private static void initObjectWithExistingAttributes(EtkDataObject newObject, DBDataObjectAttributes attributes,
                                                         EtkDataObjectList objectToStoreList, EtkProject project) {
        if (newObject.existsInDB()) {
            return;
        }
        newObject.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        IdWithType id = newObject.getAsId();
        newObject.assignAttributesValues(project, attributes, false, DBActionOrigin.FROM_EDIT);
        newObject.setId(id, DBActionOrigin.FROM_EDIT);
        newObject.updateOldId();
        objectToStoreList.add(newObject, DBActionOrigin.FROM_EDIT);

    }

    /**
     * Holt oder erzeugt die Zielstückliste, in der die ausgewählten Stücklistenpositionen abgelegt werden sollen.
     *
     * @param dataAssembly
     * @param modifiedAssemblies
     * @param sourceAssemblyDocType
     * @param selectedPartListEntries
     * @param deleteOriginalEntries
     * @return
     */
    private ResultGetOrCreateTargetAssembly getOrCreateTargetAssembly(iPartsDataAssembly dataAssembly, Set<AssemblyId> modifiedAssemblies,
                                                                      iPartsDocumentationType sourceAssemblyDocType, List<EtkDataPartListEntry> selectedPartListEntries,
                                                                      boolean deleteOriginalEntries) {
        EinPasId targetEinPAS = null;
        KgTuListItem targetKgTu = null;
        // Produkt bestimmen
        iPartsProductId productId = dataAssembly.getProductIdFromModuleUsage();
        if (productId != null) {
            boolean isTUtoOpen = false;
            iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
            // Produktstruktur über Produkt-Cache bestimmen
            iPartsConst.PRODUCT_STRUCTURING_TYPE productType = product.getProductStructuringType();
            // Um den Dialog für die neue Struktur (EinPAS oder KgTu) vorbelegen zu können, müssen erst die aktuellen
            // Strukturknoten bestimmt werden (EinPAS: HG/G/TU - KGTU: KG/TU). Dafür wird mit dem Produkt und
            // AssemblyId in DA_MODULE_EINPAS nachgeschaut. Bei einem Treffer kann abhängig vom Strukturtyp ein
            // Ausgangsmodul ermittelt werden.
            iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForProductAndModule(getProject(), productId, dataAssembly.getAsId());
            if (!moduleEinPASList.isEmpty()) {
                String moduleNumber = "";
                EtkMultiSprache moduleName = null;
                iPartsDataModuleEinPAS moduleEinPAS = moduleEinPASList.get(0);
                // Wird sind in einem EinPAS Modul
                if (productType == iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS) {
                    String hg = moduleEinPAS.getFieldValue(FIELD_DME_EINPAS_HG);
                    String g = moduleEinPAS.getFieldValue(FIELD_DME_EINPAS_G);
                    if (StrUtils.isValid(hg, g)) {
                        EinPasId einPasId = new EinPasId(hg, g, "");
                        targetEinPAS = EditEinPas2Dialog.showEinPasDialog(getConnector(), this, einPasId);
                        if (targetEinPAS != null) {
                            // Neue Modulnummer genrieren
                            moduleNumber = EditModuleHelper.buildEinPasModuleNumberForLatestModule(productId, targetEinPAS, getProject());
                            // Benennung via EinPAS Cache bestimmen
                            EinPasNode einPASNode = EinPas.getInstance(getProject()).getNode(targetEinPAS);
                            if (einPASNode != null) {
                                moduleName = einPASNode.getTitle().cloneMe();
                            }
                        }
                    }
                } else if (productType == PRODUCT_STRUCTURING_TYPE.KG_TU) {
                    // Wird sind in einem KGTU Modul
                    String kg = moduleEinPAS.getFieldValue(FIELD_DME_SOURCE_KG);
                    if (StrUtils.isValid(kg)) {
                        EditKGTUDialog.OnVerifyEvent doVerify = null;
                        if (ONLY_SINGLE_MODULE_PER_KGTU) {
                            doVerify = dialog -> {
                                List<String> errors = new DwList<>();
                                KgTuId kgTuId = dialog.getKgTuId();
                                if (EditModuleHelper.isStandardModuleInReservedPK(getProject(), dialog.getProductId(),
                                                                                  kgTuId, false, errors)) {
                                    if (kgTuId != null) {
                                        errors.add("\n");
                                        errors.add(TranslationHandler.translate("!!Dieser KG/TU-Knoten (%1/%2) ist damit belegt und kann nicht gewählt werden!",
                                                                                kgTuId.getKg(), kgTuId.getTu()));
                                    }
                                    MessageDialog.showError(errors);
                                    return false;
                                }
                                return true;
                            };
                        }
                        KgTuId kgTuId = new KgTuId(kg, "");

                        // Produkte für Produktauswahl bestimmen
                        Collection<iPartsProductId> availableProducts = null;
                        if (!deleteOriginalEntries && !dataAssembly.isSAAssembly()) { // Nur beim Kopieren und nicht für freie SAs die Produktauswahl anzeigen
                            availableProducts = new TreeSet<>();
                            availableProducts.add(productId);
                            if (sourceAssemblyDocType.isPKWDocumentationType()) {
                                // Alle Ausführungsarten der selektierten Stücklisteneinträge bestimmen
                                Set<String> aaSet = new HashSet<>();
                                for (EtkDataPartListEntry partListEntry : selectedPartListEntries) {
                                    aaSet.add(partListEntry.getFieldValue(FIELD_K_AA));
                                }

                                if (aaSet.size() == 1) { // Alle Stücklisteneinträge haben dieselbe Ausführungsart -> Produktauswahl möglich
                                    iPartsSeriesId referencedSeries = product.getReferencedSeries();
                                    if (referencedSeries != null) {
                                        String aa = aaSet.iterator().next();
                                        List<iPartsProduct> otherProducts = iPartsProduct.getAllProductsForReferencedSeries(getProject(),
                                                                                                                            referencedSeries);
                                        for (iPartsProduct otherProduct : otherProducts) {
                                            // Über die Stücklisteneinträge ausgewählte Ausführungsart muss in dem anderen
                                            // Produkt auch vorhanden sein
                                            if ((otherProduct != product) && otherProduct.getAAsFromModels(getProject()).contains(aa)) {
                                                availableProducts.add(otherProduct.getAsId());
                                            }
                                        }
                                    }
                                }
                            } else { // Nicht-DIALOG-Stückliste
                                List<iPartsProduct> allProductsList = iPartsProduct.getAllProducts(getProject());
                                for (iPartsProduct otherProduct : allProductsList) {
                                    if ((otherProduct != product) && !otherProduct.getDocumentationType().isPKWDocumentationType()) {
                                        availableProducts.add(otherProduct.getAsId());
                                    }
                                }
                            }
                        }

                        EditKGTUDialog.ProductKgTuSelection selection = EditKGTUDialog.showKgTuDialog(getConnector(), this,
                                                                                                      productId, kgTuId, doVerify,
                                                                                                      availableProducts);
                        if (selection != null) {
                            productId = selection.getProductId();
                            targetKgTu = selection.getKgTuListItem();
                            isTUtoOpen = selection.isOpenTU();
                            // Neue Modulnummer genrieren
                            moduleNumber = EditModuleHelper.buildKgTuModuleNumberForLatestModule(productId, targetKgTu.getKgTuId(), getProject());
                            // Benennung via KgTuListItem bestimmen
                            moduleName = targetKgTu.getKgTuNode().getTitle().cloneMe();
                        }
                    }
                }
                // Es wurde eine neue Modulnummer bestimmt
                if (StrUtils.isValid(moduleNumber)) {
                    AssemblyId assemblyId = new AssemblyId(moduleNumber, "");
                    EtkDataAssembly targetAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
                    boolean assemblyCreated = false;
                    // Wenn das Zielmodul noch nicht existiert, muss es vorher angelegt werden
                    if (!targetAssembly.existsInDB()) {
                        // Ziel-Assembly existiert noch nicht -> Neu anlegen
                        iPartsDocumentationType documentationType = product.getDocumentationType();
                        // Passen Ziel- und Ausgangsdokumentationstyp nicht zusammen, dann keine neue Assembly anlegen
                        if (!checkDocuType(sourceAssemblyDocType, documentationType)) {
                            return null;
                        }
                        iPartsModuleTypes moduleType = documentationType.getModuleType(false);
                        if (moduleName != null) {
                            moduleName.completeWithLanguages(getProject().getConfig().getDatabaseLanguages());
                        }
                        targetAssembly = EditModuleForm.generateNewModule(getProject(), assemblyId, productType, moduleType, moduleName,
                                                                          productId, targetEinPAS, (targetKgTu != null) ? targetKgTu.getKgTuId() : null,
                                                                          documentationType, targetKgTu);
                        if (targetAssembly == null) {
                            return null;
                        }
                        assemblyCreated = true;
                    } else {
                        // Check, ob Ausgangsdokumentationstyp zum Dokumentationstyp der bestehenden Stückliste passt
                        if (targetAssembly instanceof iPartsDataAssembly) {
                            if (!checkDocuType(sourceAssemblyDocType, ((iPartsDataAssembly)targetAssembly).getDocumentationType())) {
                                return null;
                            }
                        }
                    }
                    // Zielmodul neu laden, wenn es im Edit manuell geöffnet wurde
                    if (!assemblyCreated) {
                        modifiedAssemblies.add(targetAssembly.getAsId());
                    }
                    return new ResultGetOrCreateTargetAssembly(targetAssembly, isTUtoOpen);
                }
            }
        }
        return null;
    }

    /**
     * Datenklasse für die Ziel-Baugruppe und dem Flag, ob der TU geöffnet werden soll.
     */
    private class ResultGetOrCreateTargetAssembly {

        private EtkDataAssembly targetAssembly;
        private boolean isTUtoOpen;

        public ResultGetOrCreateTargetAssembly(EtkDataAssembly targetAssembly, boolean isTUtoOpen) {
            this.targetAssembly = targetAssembly;
            this.isTUtoOpen = isTUtoOpen;
        }

        public EtkDataAssembly getTargetAssembly() {
            return targetAssembly;
        }

        public boolean isTUtoOpen() {
            return isTUtoOpen;
        }

        public void resetIsTUtoOpen() {
            isTUtoOpen = false;
        }
    }

    /**
     * Überprüft, ob die Dokumentationsmethode der Ausgangsstückliste zur Dokumentationsmethode der Zielstückliste passt.
     * Falls nicht, wird eine Meldung ausgegeben.
     *
     * @param sourceAssemblyDocType
     * @param documentationType
     * @return
     */
    private boolean checkDocuType(iPartsDocumentationType sourceAssemblyDocType, iPartsDocumentationType documentationType) {
        if ((sourceAssemblyDocType == null) || !sourceAssemblyDocType.isSameOverallDocType(documentationType)) {
            String text = TranslationHandler.translate("!!Vorgang abgebrochen. Dokumentationsmethoden beider Stücklisten passen nicht zusammen!")
                          + "\n\n"
                          + TranslationHandler.translate("!!Ausgangsstückliste: %1", (sourceAssemblyDocType != null) ? sourceAssemblyDocType.getExportValue() : "")
                          + "\n"
                          + TranslationHandler.translate("!!Zielstückliste: %1", (documentationType != null) ? documentationType.getExportValue() : "");
            MessageDialog.show(text);
            return false;
        }
        return true;
    }

    public void doSortPartlistEntriesByHotspot(Event event) {
        if (MessageDialog.showYesNo("!!Soll der TU nach Hotspot sortiert werden?", EditToolbarButtonAlias.EDIT_SORT_BY_HOTSPOT.getTooltip()) != ModalResult.YES) {
            return;
        }

        // sortedPartListEntries ist initial bereits sortiert nach Sequenznummer
        List<EtkDataPartListEntry> sortedPartListEntries = new ArrayList<>(getConnector().getUnfilteredPartListEntries());

        SortStringCache sortStringCache = new SortStringCache();
        boolean sorted = false;
        boolean hotspotFound = false;

        // Idee: Gehe die ungefilterte Stückliste von oben nach unten durch und sortiere jeden Stücklisteneintrag nach seinem
        // Hotspot in die (zukünftig sortierte) Stückliste ein. Jeder Stücklisteintrag wandert also bildlich gesprochen
        // von ganz oben in der zukünftig sortierten Stückliste soweit nach unten bis der erste größere Hotspot gefunden
        // wird. Um die Reihenfolge bei mehreren Stücklisteneinträgen mit gleichem Hotspot zu erhalten, muss dann wieder
        // solange nach oben gesucht werden, bis ein anderer Hotspot oder mein eigener Index kommt. Dorthin dann verschieben,
        // falls der neue Index ein anderer ist als der alte.
        // Es gibt Sonderregeln für das Einfügen ganz am Ende der Stückliste für die größte Hotspot-Nummer sowie für leere
        // Hotspots. Leere Hotspots am Anfang der Stückliste bleiben an Ort und Stelle. Leere Hotspots zwischendrin bzw.
        // v.a. auch am Ende der Stückliste werden ans Ende der Stückliste verschoben unter Beibehaltung der bisherigen
        // Sortierung.
        partListEntryLoop:
        for (EtkDataPartListEntry partListEntry : getConnector().getUnfilteredPartListEntries()) {
            String hotspot = partListEntry.getFieldValue(FIELD_K_POS).trim();
            if (!hotspot.isEmpty()) {
                hotspotFound = true;
                String hotspotSortString = sortStringCache.getSortString(hotspot, false);
                int oldIndex = -1;
                int newIndex = -1;
                int index = 0;
                for (EtkDataPartListEntry otherPartListEntry : sortedPartListEntries) {
                    if ((oldIndex == -1) && otherPartListEntry.getAsId().equals(partListEntry.getAsId())) {
                        oldIndex = index;
                    }

                    if (newIndex == -1) {
                        String otherHotspot = otherPartListEntry.getFieldValue(FIELD_K_POS).trim();
                        // Wenn der andere Hotspot nicht leer ist und sein Wert größer ist als mein eigener, dann den aktuellen
                        // Index als neue Position merken, weil ich mich davor einsortieren muss
                        if (!otherHotspot.isEmpty() && (hotspotSortString.compareTo(sortStringCache.getSortString(otherHotspot, false)) < 0)) {
                            // Bisherige Sortierung beibehalten falls ich in der Stückliste vor dem newIndex einsortiert
                            // war (also schon ein oldIndex bekannt ist) -> wieder nach oben in der sortierten Stückliste
                            // suchen solange der Hotspot noch identisch ist mit meinem bzw. ich meinen eigenen Index treffe
                            if (oldIndex != -1) {
                                int sortedNewIndex = searchUpwardsFirstPartListEntryWithOtherHotspot(hotspot, oldIndex, index - 1,
                                                                                                     sortedPartListEntries);
                                if (sortedNewIndex != oldIndex) {
                                    // Position hat sich verändert -> zum aktuellen Index verschieben, damit die Reihenfolge
                                    // innerhalb eines Hotspots erhalten bleibt (sortedNewIndex würde die Reihenfolge umdrehen)
                                    newIndex = index;
                                } else {
                                    newIndex = oldIndex; // Position nach Sortierung innerhalb vom Hotspot bleibt erhalten
                                }
                            } else {
                                newIndex = index;
                            }
                        }
                    }

                    // Frühzeitiger Abbruch
                    if ((oldIndex != -1) && (newIndex != -1)) {
                        break;
                    }

                    index++;
                }

                if (oldIndex == -1) {
                    // Darf eigentlich gar nicht passieren, weil der Stücklisteneintrag ja in der Stückliste vorhanden sein muss
                    continue partListEntryLoop;
                }

                // Stücklisteneintrag hat den größten Hotspot aller Stücklisteneinträge -> ganz hinten in der Stückliste einsortieren
                if (newIndex == -1) {
                    // Spezialfall falls der Stücklisteneintrag bereits der letzte Eintrag ist
                    if (oldIndex == (sortedPartListEntries.size() - 1)) {
                        continue partListEntryLoop;
                    }

                    // Index vor dem ersten Stücklisteneintrag mit leerem Hotspot beginnend am Ende der Stückliste suchen
                    newIndex = searchUpwardsFirstPartListEntryWithOtherHotspot("", oldIndex, sortedPartListEntries.size() - 1,
                                                                               sortedPartListEntries);

                    if (newIndex != oldIndex) {
                        // Bisherige Sortierung innerhalb des Hotspots beibehalten -> nach oben in der sortierten Stückliste
                        // suchen solange der Hotspot noch identisch ist mit meinem bzw. ich meinen eigenen Index treffe
                        int sortedNewIndex = searchUpwardsFirstPartListEntryWithOtherHotspot(hotspot, oldIndex, newIndex - 1, sortedPartListEntries);
                        if (sortedNewIndex == oldIndex) {
                            newIndex = oldIndex; // Position nach Sortierung innerhalb vom Hotspot bleibt erhalten
                        }
                    }
                }

                if (fixPartListEntrySortOrder(partListEntry, oldIndex, newIndex, sortedPartListEntries)) {
                    sorted = true;
                }
            } else { // Hotspot leer
                // Falls schon ein gültiger Hotspot gefunden wurde, dann diesen Stücklisteneintrag mit leerem Hotspot ganz
                // ans Ende der Stückliste verschieben (unter Beibehaltung der Reihenfolge von anderen Stücklisteneinträgen
                // mit leerem Hotspot)
                if (hotspotFound) {
                    int oldIndex = findPartlistEntryIndexInPartlistUnfiltered(partListEntry.getAsId(), sortedPartListEntries);
                    if (oldIndex == -1) {
                        // Darf eigentlich gar nicht passieren, weil der Stücklisteneintrag ja in der Stückliste vorhanden sein muss
                        continue partListEntryLoop;
                    }

                    int newIndex = sortedPartListEntries.size();
                    int sortedNewIndex = searchUpwardsFirstPartListEntryWithOtherHotspot(hotspot, oldIndex, newIndex - 1,
                                                                                         sortedPartListEntries);
                    if (sortedNewIndex == oldIndex) {
                        newIndex = oldIndex; // Position nach Sortierung innerhalb vom Hotspot bleibt erhalten
                    }

                    if (fixPartListEntrySortOrder(partListEntry, oldIndex, newIndex, sortedPartListEntries)) {
                        sorted = true;
                    }
                }
            }
        }

        if (sorted) {
            savePartlistToChangesetAfterEntryMoved();
        }
    }

    private int searchUpwardsFirstPartListEntryWithOtherHotspot(String hotspot, int oldIndex, int newIndex, List<EtkDataPartListEntry> sortedPartListEntries) {
        while (newIndex >= 0) {
            if (newIndex == oldIndex) {
                // Der bisherige Index ist korrekt
                return oldIndex;
            }
            String searchHotspot = sortedPartListEntries.get(newIndex).getFieldValue(FIELD_K_POS).trim();
            if (searchHotspot.equals(hotspot)) {
                newIndex--;
            } else {
                break;
            }
        }
        return newIndex + 1; // Nach dem ersten Stücklisteneintrag mit anderem Hotspot einsortieren
    }

    private boolean fixPartListEntrySortOrder(EtkDataPartListEntry partListEntry, int oldIndex, int newIndex, List<EtkDataPartListEntry> sortedPartListEntries) {
        if (newIndex != oldIndex) { // Sortierung passt nicht -> an neuen Index verschieben
            List<EtkDataPartListEntry> partListEntriesToBeMoved = new ArrayList<>(1);
            partListEntriesToBeMoved.add(partListEntry);

            // newIndex -1, weil insertPartlistEntriesAfterIndex() NACH dem Index einsortiert, wir ab an (also vor)
            // diesem Index einsortiert werden müssen
            insertPartlistEntriesAfterIndex(partListEntriesToBeMoved, newIndex - 1, sortedPartListEntries);

            // newIndex korrigieren, falls oldIndex kleiner ist, da sich die Stücklisteneinträge in sortedPartListEntries
            // dann nach oben verschieben
            if (oldIndex < newIndex) {
                newIndex--;
            }

            // Sortierte Stückliste anpassen
            sortedPartListEntries.remove(oldIndex);
            sortedPartListEntries.add(newIndex, partListEntry);
            return true;
        } else {
            return false;
        }
    }

    private void doMovePartlistEntriesTo(Event event) {
        if (isMovePossible()) {
            List<EtkDataPartListEntry> selectedPartListEntries = getConnector().getSelectedPartListEntries();
            if ((selectedPartListEntries != null) && (!selectedPartListEntries.isEmpty())) {
                sortPartlistEntriesBySeqNr(selectedPartListEntries); // Selektion sortieren damit die Reihenfolge nach dem Verschieben erhalten bleibt
                PartListEntryId partListEntryId = EditSelectSinglePartlistEntrySimpleForm.showSelectPartlistPosition(getConnector(), this,
                                                                                                                     selectedPartListEntries.get(0), filteredSelectedHotSpot);
                if (partListEntryId != null) {
                    List<EtkDataPartListEntry> unfilteredPartListEntries = getConnector().getUnfilteredPartListEntries();
                    int indexAfter = findPartlistEntryIndexInPartlistUnfiltered(partListEntryId, unfilteredPartListEntries);
                    insertPartlistEntriesAfterIndex(selectedPartListEntries, indexAfter, unfilteredPartListEntries);
                    savePartlistToChangesetAfterEntryMoved();
                    enableButtons();
                }
            }
        }
    }

    public void doMovePartlistEntriesUp(Event event) {
        if (isMovePossible() && toolbarHelper.isToolbarButtonEnabled(EditToolbarButtonAlias.EDIT_MOVE_UP)) {
            List<EtkDataPartListEntry> selectedPartListEntries = getConnector().getSelectedPartListEntries();
            if ((selectedPartListEntries != null) && (!selectedPartListEntries.isEmpty())) {
                sortPartlistEntriesBySeqNr(selectedPartListEntries); // Selektion sortieren damit die Reihenfolge nach dem Verschieben erhalten bleibt
                List<EtkDataPartListEntry> unfilteredPartListEntries = getConnector().getUnfilteredPartListEntries();
                int index = findPartlistEntryIndexInPartlistUnfiltered(selectedPartListEntries.get(0).getAsId(), unfilteredPartListEntries);
                int targetIndex = index - 2;
                if (targetIndex >= -1) {
                    insertPartlistEntriesAfterIndex(selectedPartListEntries, targetIndex, unfilteredPartListEntries);
                    savePartlistToChangesetAfterEntryMoved();

                    // Der IE verliert nach der Aktion den Focus, deshalb hier wieder setzen
                    assemblyListForm.getPartListTable().requestFocus();
                    enableButtons();
                }
            }
        }
    }

    public void doMovePartlistEntriesDown(Event event) {
        if (isMovePossible() && toolbarHelper.isToolbarButtonEnabled(EditToolbarButtonAlias.EDIT_MOVE_DOWN)) {
            List<EtkDataPartListEntry> selectedPartListEntries = getConnector().getSelectedPartListEntries();
            if ((selectedPartListEntries != null) && (!selectedPartListEntries.isEmpty())) {
                sortPartlistEntriesBySeqNr(selectedPartListEntries); // Selektion sortieren damit die Reihenfolge nach dem Verschieben erhalten bleibt
                List<EtkDataPartListEntry> unfilteredPartListEntries = getConnector().getUnfilteredPartListEntries();
                if (unfilteredPartListEntries == null) {
                    return;
                }
                int lastIndex = findPartlistEntryIndexInPartlistUnfiltered(selectedPartListEntries.get(selectedPartListEntries.size() - 1).getAsId(),
                                                                           unfilteredPartListEntries);
                int targetIndex = lastIndex + 1;
                if (targetIndex < unfilteredPartListEntries.size()) {
                    insertPartlistEntriesAfterIndex(selectedPartListEntries, targetIndex, unfilteredPartListEntries);
                    savePartlistToChangesetAfterEntryMoved();

                    // Der IE verliert nach der Aktion den Focus, deshalb hier wieder setzen
                    assemblyListForm.getPartListTable().requestFocus();
                    enableButtons();
                }
            }
        }
    }

    private void showNotImplemented(String extraText) {
        NotImplementedCode.execute(NotImplementedCode.IPARTS_EDITOR + " " + extraText);
        MessageDialog.show("!!Funktion noch nicht implementiert.");
    }

    public void enableButtons() {
        GuiContextMenu assemblyContextMenu = assemblyListForm.getContextMenu();
        isSorted = assemblyListForm.getPartListTable().isSortEnabled() && (assemblyListForm.getPartListTable().getSortColumn() != -1);
        if (isEditAllowed) {
            // Der TU wurde zum Editieren geöffnet (Autorenauftrag aktiv usw.)
            handleButtonsEditAllowed(assemblyContextMenu);
        } else {
            // Der TU wurde nicht im Edit-Modus geöffnet (Autorenauftrag nicht aktiv)
            handleButtonsEditNotAllowed(assemblyContextMenu);
        }
        // Bei einer PSK Stückliste dürfen Änderungen nicht akzeptiert werden
        if (iPartsPSKHelper.isPSKAssembly(getConnector().getCurrentAssembly())) {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_MATERIAL_CHANGES, assemblyContextMenu);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_PARTLISTENTRY_CHANGES, assemblyContextMenu);
        }

        toolbarHelper.setToolbarButtonAndMenuVisible(EditToolbarButtonAlias.EDIT_PARTLISTENTRY_MODIFIED, assemblyContextMenu,
                                                     isRevisionChangeSetActive());

        handleButtonsForLockingEntries(assemblyContextMenu);
    }

    /**
     * Setzt den Zustand der Buttons und Menüeinträge, wenn der Edit-Modus nicht aktiv ist
     *
     * @param assemblyContextMenu
     */
    private void handleButtonsEditNotAllowed(GuiContextMenu assemblyContextMenu) {
        //im Viewing-Mode (Modul is readonly)
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CUT, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COPY, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PASTE, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, assemblyContextMenu);
        toolbarHelper.hideSeparatorInToolbarAndMenu("ToolbarSeparatorExtra", assemblyContextMenu);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_USAGE, assemblyContextMenu, (assemblyListForm.getSelectionCount() == 1)
                                                                                                         && assemblyListForm.isWithUsage());
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_REPLACEMENTS, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_FACTORY_DATA, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOMAT, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOASSEMBLY, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAT, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PARTLISTENTRY, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAKEFLAT, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CONSTKITS, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_FOOTNOTES, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_OPTIONALPARTS, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_HIERARCHY, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NUMPOS, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_ADD_TO_PICORDER, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MULTIPLE_ENTRIES, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MERGING_ENTRIES, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_MATERIAL_CHANGES, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_PARTLISTENTRY_CHANGES, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COMPRESS, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PRINT, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(ToolbarButtonAlias.FILTER, assemblyContextMenu);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CLEARSORT, assemblyContextMenu, isSorted);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COLORTABLES, assemblyContextMenu);

        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_SORT_BY_HOTSPOT, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MOVE_UP, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MOVE_DOWN, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MOVE_TO_POSITION, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COPY_ENTRY, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_RELOCATE_ENTRY, assemblyContextMenu);

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CODE_MATRIX, assemblyContextMenu, assemblyListForm.getSelectionCount() == 1);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_INTERNAL_TEXT, assemblyContextMenu);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PRIMUS_REPLACEMENT_CHAIN, assemblyContextMenu, assemblyListForm.getSelectionCount() == 1);
        boolean primusIsVisible = false;
        if (assemblyListForm.getSelectionCount() == 1) {
            primusIsVisible = iPartsRelatedInfoPrimusReplacementChainForm.relatedInfoIsVisible(assemblyListForm.getSelectedEntry(), getProject());
        }
        toolbarHelper.visibleMenu(EditToolbarButtonAlias.EDIT_PRIMUS_REPLACEMENT_CHAIN, assemblyContextMenu, primusIsVisible);

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DIALOG_RETAIL_REPLACEMENT_CHAIN, assemblyContextMenu, assemblyListForm.getSelectionCount() == 1);
        boolean isDialogRetailReplacementChain = false;
        if (assemblyListForm.getSelectionCount() == 1) {
            isDialogRetailReplacementChain = iPartsRelatedInfoDialogMultistageRetailReplacementChainForm.relatedInfoIsVisible(assemblyListForm.getSelectedEntry());
        }
        toolbarHelper.visibleMenu(EditToolbarButtonAlias.EDIT_DIALOG_RETAIL_REPLACEMENT_CHAIN, assemblyContextMenu, isDialogRetailReplacementChain);

        toolbarHelper.hideSeparatorInToolbarAndMenu("ToolbarSeparatorMove", assemblyContextMenu);
    }

    /**
     * Setzt den Zustand der Buttons/Menüpunkte zum (Ent-)Sperren von Stücklistenpositionen
     *
     * @param assemblyContextMenu
     */
    private void handleButtonsForLockingEntries(GuiContextMenu assemblyContextMenu) {
        if (!isEditAllowed() && iPartsRight.LOCK_PART_LIST_ENTRIES_FOR_EDIT.checkRightInSession()) {
            // Edit ist nicht aktiv und das Recht ist vorhanden ->
            List<EtkDataPartListEntry> selectedEntries = assemblyListForm.getSelectedEntries();
            boolean enableLocking = false;
            boolean enableUnlocking = false;
            if ((selectedEntries != null) && !selectedEntries.isEmpty()) {
                long lockedEntriesCount = iPartsLockEntryHelper.getLockedEntries(selectedEntries).size();
                enableLocking = lockedEntriesCount != selectedEntries.size();
                enableUnlocking = lockedEntriesCount > 0;
            }
            toolbarHelper.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.EDIT_LOCK_PART_LIST_ENTRY, assemblyContextMenu, enableLocking);
            toolbarHelper.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.EDIT_UNLOCK_PART_LIST_ENTRY, assemblyContextMenu, enableUnlocking);
        } else {
            /// Fehlt das Recht oder der Edit ist aktiv, darf man nicht (ent-)sperren
            toolbarHelper.hideToolbarButtonAndMenu(iPartsToolbarButtonAlias.EDIT_LOCK_PART_LIST_ENTRY, assemblyContextMenu);
            toolbarHelper.hideToolbarButtonAndMenu(iPartsToolbarButtonAlias.EDIT_UNLOCK_PART_LIST_ENTRY, assemblyContextMenu);
            toolbarHelper.hideSeparatorInToolbarAndMenu("ToolbarSeparatorLockEntries", assemblyContextMenu);
        }
    }

    /**
     * Setzt den Zustand der Buttons und Menüeinträge in Abhängigkeit des aktuellen Edit-Zustands und den selektierten
     * Einträgen.
     *
     * @param assemblyContextMenu
     */
    private void handleButtonsEditAllowed(GuiContextMenu assemblyContextMenu) {
        toolbarHelper.showSeparatorInToolbarAndMenu("ToolbarSeparatorExtra", assemblyContextMenu);
        toolbarHelper.showSeparatorInToolbarAndMenu("ToolbarSeparatorMove", assemblyContextMenu);
        EtkDataPartListEntry selectedEntry = assemblyListForm.getSelectedEntry();
        boolean enabled = (selectedEntry != null);
        isFiltered = assemblyListForm.getPartListTable().hasFilterValueForAnyColumn() || ((EditModuleFormConnector)getConnector()).isShowOnlyModifiedPartListEntries();
        boolean isNotFilteredOrSorted = !isFiltered && !isSorted;
        boolean isInplaceEditActive = false;
        if (!isFiltered) {
            filteredSelectedHotSpot = "";
        }

        // Check, ob ein Inplace-Editor aktiv ist
        if ((assemblyListForm != null) && (assemblyListForm.getPartListTable().getInPlaceEditorFactory() instanceof GuiTableInplaceEditorManager)) {
            GuiTableInplaceEditorManager editorFactory = (GuiTableInplaceEditorManager)assemblyListForm.getPartListTable().getInPlaceEditorFactory();
            isInplaceEditActive = editorFactory.isInplaceEditorActive();
        }

        if (enabled && !isInplaceEditActive) {
            // Edit allowed and at least one entry selected
            handleButtonsEditAllowedAtLeastOneSelected(selectedEntry, assemblyContextMenu, isNotFilteredOrSorted);
        } else {
            // Edit allowed and nothing selected
            handleButtonsEditAllowedNothingSelected(assemblyContextMenu, isInplaceEditActive);
        }

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_SORT_BY_HOTSPOT, assemblyContextMenu,
                                                 !isInplaceEditActive && (assemblyListForm.getPartListTable().getRowCount() > 1));
        handleButtonsForMovableEntries(assemblyContextMenu, isInplaceEditActive);
    }

    /**
     * Bestimmt den Zustand der Buttons und Menüeinträge, die für das Verschieben der Positionen zuständig sind
     *
     * @param assemblyContextMenu
     * @param isInplaceEditActive
     */
    private void handleButtonsForMovableEntries(GuiContextMenu assemblyContextMenu, boolean isInplaceEditActive) {
        // Bestimmung, ob Verschieben möglich ist erfolgt separat, da hier auch auf Lücken in der Selektion geprüft wird
        boolean movePossible = isMovePossible();
        boolean moveUpPossible = false;
        boolean moveDownPossible = false;
        if (movePossible && !isInplaceEditActive) {
            int[] selectedRowIndices = assemblyListForm.getPartListTable().getSelectedRowIndices();
            if (selectedRowIndices.length > 0) {
                if (selectedRowIndices[0] > 0) {
                    if (StrUtils.isValid(filteredSelectedHotSpot)) {
                        EtkDataPartListEntry previousPartListEntry = assemblyListForm.getPreviousPartListEntry(selectedRowIndices[0]);
                        String previousHotSpot = previousPartListEntry.getFieldValue(FIELD_K_POS);
                        if (previousHotSpot.equals(filteredSelectedHotSpot)) {
                            moveUpPossible = true;
                        }
                    } else {
                        moveUpPossible = true;
                    }
                }
                if (selectedRowIndices[selectedRowIndices.length - 1] < (assemblyListForm.getPartListTable().getRowCount() - 1)) {
                    if (StrUtils.isValid(filteredSelectedHotSpot)) {
                        EtkDataPartListEntry nextListEntry = assemblyListForm.getNextPartListEntry(selectedRowIndices[selectedRowIndices.length - 1]);
                        String nextHotSpot = nextListEntry.getFieldValue(FIELD_K_POS);
                        if (nextHotSpot.equals(filteredSelectedHotSpot)) {
                            moveDownPossible = true;
                        }
                    } else {
                        moveDownPossible = true;
                    }
                }
            }

        }
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MOVE_UP, assemblyContextMenu, movePossible && moveUpPossible);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MOVE_DOWN, assemblyContextMenu, movePossible && moveDownPossible);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MOVE_TO_POSITION, assemblyContextMenu,
                                                 movePossible && (assemblyListForm.getPartListTable().getRowCount() > 1));
    }

    /**
     * Setzt den Zustand der Buttons und Menüeinträge im Edit Modus, wenn mind. eine Position selektiert wurde
     *
     * @param selectedEntry
     * @param assemblyContextMenu
     * @param isNotFilteredOrSorted
     */
    private void handleButtonsEditAllowedAtLeastOneSelected(EtkDataPartListEntry selectedEntry, GuiContextMenu assemblyContextMenu, boolean isNotFilteredOrSorted) {
        // One or more entries selected
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CUT, assemblyContextMenu, true);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COPY, assemblyContextMenu, true);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PASTE, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, assemblyContextMenu, true);

        boolean isSingleSelected = assemblyListForm.getSelectionCount() == 1;
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_SELECTALL, assemblyContextMenu, !assemblyListForm.isAllSelected());
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CODE_MATRIX, assemblyContextMenu, isSingleSelected);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COPY_ENTRY, assemblyContextMenu, !isSaModule());
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_RELOCATE_ENTRY, assemblyContextMenu, !isSaModule());
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COPY_ENTRY_TO_SA, assemblyContextMenu, isSaModule());

        if (isSingleSelected) {
            // Edit allowed and one entry selected
            handleButtonsEditAllowedSingleSelect(selectedEntry, assemblyContextMenu, isNotFilteredOrSorted);
        } else {
            // Edit allowed and multiselect
            handleButtonsEditAllowedMultiSelect(assemblyContextMenu);
        }

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CONSTKITS, assemblyContextMenu, true);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_HIERARCHY, assemblyContextMenu, true);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NUMPOS, assemblyContextMenu, isNotFilteredOrSorted);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PRINT, assemblyContextMenu, true);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CLEARSORT, assemblyContextMenu, isSorted);
        //hängt von Modultyp ab
        toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_COMPRESS);
        toolbarHelper.hideToolbarButton(ToolbarButtonAlias.FILTER);

        if (!iPartsEditPlugin.isMaterialEditable()) { // Material bearbeiten ist nicht erlaubt
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAT, assemblyContextMenu);
        }

        // Check, ob es Änderungen gibt, die bestätigt werden müssen
        handleButtonsEditAllowedChangesFoundInSelectedEntries(assemblyContextMenu);
    }

    /**
     * Bestimmt im Edit-Modus den Zustand der Buttons und Menüeinträge, die für das Akzeptieren von Änderungen am Material oder
     * Stücklistenpositionen zuständig sind
     *
     * @param assemblyContextMenu
     */
    private void handleButtonsEditAllowedChangesFoundInSelectedEntries(GuiContextMenu assemblyContextMenu) {
        // Liegen an mindestens einem selektierten Stücklisteneintrag Änderungen am Material (ET-KZ) vor?
        boolean changedMaterialFound = false;
        boolean changedPartlistentryFound = false;
        List<EtkDataPartListEntry> selectedEntries = assemblyListForm.getSelectedEntries();
        if (selectedEntries == null) {
            return;
        }
        selectedEntriesLoop:
        for (EtkDataPartListEntry partListEntry : selectedEntries) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataDIALOGChangeList dialogChangesForBCTE = ((iPartsDataPartListEntry)partListEntry).getDIALOGChangesForBCTE();
                if (dialogChangesForBCTE != null) {
                    for (iPartsDataDIALOGChange dataDIALOGChange : dialogChangesForBCTE) {
                        if (dataDIALOGChange.getAsId().getDoType().equals(iPartsDataDIALOGChange.ChangeType.MAT_ETKZ.getDbKey())) {
                            changedMaterialFound = true;
                        }
                        if (dataDIALOGChange.getAsId().getDoType().equals(iPartsDataDIALOGChange.ChangeType.PARTLISTENTRY_ETKZ.getDbKey())) {
                            changedPartlistentryFound = true;
                        }
                        if (changedMaterialFound && changedPartlistentryFound) {
                            break selectedEntriesLoop;
                        }
                    }
                }
            }
        }

        if (changedMaterialFound) {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_MATERIAL_CHANGES, assemblyContextMenu);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_MATERIAL_CHANGES, assemblyContextMenu, true);
        } else {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_MATERIAL_CHANGES, assemblyContextMenu);
        }

        if (changedPartlistentryFound) {
            toolbarHelper.showToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_PARTLISTENTRY_CHANGES, assemblyContextMenu);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_PARTLISTENTRY_CHANGES, assemblyContextMenu, true);
        } else {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_PARTLISTENTRY_CHANGES, assemblyContextMenu);
        }
    }

    /**
     * Setzt im Edit-Modus den Zustand der Buttons und Menüeinträge, wenn genau eine Position selektiert wurde
     *
     * @param assemblyContextMenu
     * @param isNotFilteredOrSorted
     * @param selectedEntry
     */
    private void handleButtonsEditAllowedSingleSelect(EtkDataPartListEntry selectedEntry, GuiContextMenu assemblyContextMenu, boolean isNotFilteredOrSorted) {
        boolean primusIsVisible = iPartsRelatedInfoPrimusReplacementChainForm.relatedInfoIsVisible(selectedEntry, getProject());
        boolean isCurrentAssemblyCarPerspective = isCarPerspectiveModule();

        toolbarHelper.visibleMenu(EditToolbarButtonAlias.EDIT_PRIMUS_REPLACEMENT_CHAIN, assemblyContextMenu, primusIsVisible);
        boolean dialogRetailReplacementChainIsVisible = iPartsRelatedInfoDialogMultistageRetailReplacementChainForm.relatedInfoIsVisible(selectedEntry);
        toolbarHelper.visibleMenu(EditToolbarButtonAlias.EDIT_DIALOG_RETAIL_REPLACEMENT_CHAIN, assemblyContextMenu, dialogRetailReplacementChainIsVisible);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_USAGE, assemblyContextMenu, assemblyListForm.isWithUsage());
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_REPLACEMENTS, assemblyContextMenu, true);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_ADD_TO_PICORDER, assemblyContextMenu, getConnector().isAuthorOrderValid());
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MULTIPLE_ENTRIES, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MERGING_ENTRIES, assemblyContextMenu, getConnector().isAuthorOrderValid() &&
                                                                                                                   EditMergePartlistEntryForm.hasOtherEntriesForMerging(getConnector().getCurrentPartListEntries(), selectedEntry));
        if (selectedEntry.isAssembly()) {
            //Selected is Assembly
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOMAT, assemblyContextMenu, true);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOASSEMBLY, assemblyContextMenu, false);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAT, assemblyContextMenu, isCurrentAssemblyCarPerspective);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PARTLISTENTRY, assemblyContextMenu, isCurrentAssemblyCarPerspective);
        } else {
            //Selected is Part
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOMAT, assemblyContextMenu, false);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOASSEMBLY, assemblyContextMenu, true);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAT, assemblyContextMenu, true);
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PARTLISTENTRY, assemblyContextMenu, true);
        }
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAKEFLAT, assemblyContextMenu, isNotFilteredOrSorted);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_FOOTNOTES, assemblyContextMenu, true);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_OPTIONALPARTS, assemblyContextMenu, true);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COLORTABLES, assemblyContextMenu, !isCurrentAssemblyCarPerspective);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_FACTORY_DATA, assemblyContextMenu, !isCurrentAssemblyCarPerspective);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_INTERNAL_TEXT, assemblyContextMenu, true);
        if (isCurrentAssemblyCarPerspective) {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COPY_ENTRY, assemblyContextMenu);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_RELOCATE_ENTRY, assemblyContextMenu);
        } else {
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_COPY_ENTRY, assemblyContextMenu,
                                                            TranslationHandler.translate(TEMPLATE_TEXT_COPY_ENTRY_TO_OTHER_PARTLIST,
                                                                                         TranslationHandler.translate("!!Stücklisteneintrag")));
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_RELOCATE_ENTRY, assemblyContextMenu,
                                                            TranslationHandler.translate(TEMPLATE_TEXT_RELOCATE_ENTRY_TO_OTHER_PARTLIST,
                                                                                         TranslationHandler.translate("!!Stücklisteneintrag")));
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_COPY_ENTRY_TO_SA, assemblyContextMenu,
                                                            TranslationHandler.translate(TEMPLATE_TEXT_COPY_ENTRY_TO_OTHER_SA_PARTLIST,
                                                                                         TranslationHandler.translate("!!Stücklisteneintrag")));
        }
    }

    /**
     * Setzt im Edit-Modus den Zustand der Buttons und Menüeinträge, wenn mehr als eine Position selektiert wurden
     *
     * @param assemblyContextMenu
     */
    private void handleButtonsEditAllowedMultiSelect(GuiContextMenu assemblyContextMenu) {
        boolean isCurrentAssemblyCarPerspective = isCarPerspectiveModule();

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_USAGE, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CODE_MATRIX, assemblyContextMenu, false);
        toolbarHelper.visibleMenu(EditToolbarButtonAlias.EDIT_PRIMUS_REPLACEMENT_CHAIN, assemblyContextMenu, false);
        toolbarHelper.visibleMenu(EditToolbarButtonAlias.EDIT_DIALOG_RETAIL_REPLACEMENT_CHAIN, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_REPLACEMENTS, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOMAT, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOASSEMBLY, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAT, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PARTLISTENTRY, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAKEFLAT, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_FOOTNOTES, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_OPTIONALPARTS, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_ADD_TO_PICORDER, assemblyContextMenu, getConnector().isAuthorOrderValid());
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COLORTABLES, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_FACTORY_DATA, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MULTIPLE_ENTRIES, assemblyContextMenu, getConnector().isAuthorOrderValid());
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MERGING_ENTRIES, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_INTERNAL_TEXT, assemblyContextMenu, false);
        if (isCurrentAssemblyCarPerspective) {
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COPY_ENTRY, assemblyContextMenu);
            toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_RELOCATE_ENTRY, assemblyContextMenu);
        } else {
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_COPY_ENTRY, assemblyContextMenu,
                                                            TranslationHandler.translate(TEMPLATE_TEXT_COPY_ENTRY_TO_OTHER_PARTLIST,
                                                                                         TranslationHandler.translate("!!Stücklisteneinträge")));
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_RELOCATE_ENTRY, assemblyContextMenu,
                                                            TranslationHandler.translate(TEMPLATE_TEXT_RELOCATE_ENTRY_TO_OTHER_PARTLIST,
                                                                                         TranslationHandler.translate("!!Stücklisteneinträge")));
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_COPY_ENTRY_TO_SA, assemblyContextMenu,
                                                            TranslationHandler.translate(TEMPLATE_TEXT_COPY_ENTRY_TO_OTHER_SA_PARTLIST,
                                                                                         TranslationHandler.translate("!!Stücklisteneinträge")));
        }
    }

    /**
     * Setzt im Edit-Modus den Zustand der Buttons und Menüeinträge, wenn keine Position selektiert wurde
     *
     * @param assemblyContextMenu
     * @param isInplaceEditActive
     */
    private void handleButtonsEditAllowedNothingSelected(GuiContextMenu assemblyContextMenu, boolean isInplaceEditActive) {
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CUT, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COPY, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PASTE, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, assemblyContextMenu, false);

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_SELECTALL, assemblyContextMenu, (assemblyListForm.getRowCount() > 0) && !isInplaceEditActive);

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CODE_MATRIX, assemblyContextMenu, assemblyListForm.getSelectionCount() == 1);
        toolbarHelper.visibleMenu(EditToolbarButtonAlias.EDIT_PRIMUS_REPLACEMENT_CHAIN, assemblyContextMenu, assemblyListForm.getSelectionCount() == 1);
        toolbarHelper.visibleMenu(EditToolbarButtonAlias.EDIT_DIALOG_RETAIL_REPLACEMENT_CHAIN, assemblyContextMenu, assemblyListForm.getSelectionCount() == 1);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_USAGE, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_REPLACEMENTS, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOMAT, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CHANGETOASSEMBLY, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAT, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PARTLISTENTRY, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAKEFLAT, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CONSTKITS, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_FOOTNOTES, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_OPTIONALPARTS, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_HIERARCHY, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NUMPOS, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.IMG_ADD_TO_PICORDER, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COLORTABLES, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_FACTORY_DATA, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MULTIPLE_ENTRIES, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MERGING_ENTRIES, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_COPY_ENTRY, assemblyContextMenu, false);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_RELOCATE_ENTRY, assemblyContextMenu, false);

        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_MATERIAL_CHANGES, assemblyContextMenu);
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_ACCEPT_PARTLISTENTRY_CHANGES, assemblyContextMenu);

        //hängt von Modultyp ab
        toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_COMPRESS);

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_PRINT, assemblyContextMenu, true);
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_CLEARSORT, assemblyContextMenu, isSorted);

        //hängt von Modultyp ab
        toolbarHelper.hideToolbarButton(ToolbarButtonAlias.FILTER);

        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_INTERNAL_TEXT, assemblyContextMenu, false);
    }

    private boolean isSaModule() {
        return iPartsModuleTypes.isModuleFromType(getConnector().getCurrentAssembly().getEbeneName(), iPartsModuleTypes.SA_TU);
    }

    private boolean isCarPerspectiveModule() {
        return iPartsModuleTypes.isModuleFromType(getConnector().getCurrentAssembly().getEbeneName(), iPartsModuleTypes.CAR_PERSPECTIVE);
    }

    private void btnCancelActionPerformedEvent(Event event) {
        close();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    private void updateFilterButton() {
        GuiToolButton buttonFilter = (GuiToolButton)toolbarManager.getButton(ToolbarButtonAlias.FILTER.getAlias());
        if (getFilter().isDisplayFilterActive()) {
            buttonFilter.setText("!!Aktiv");
            buttonFilter.setFontStyle(DWFontStyle.BOLD);
            buttonFilter.setForegroundColor(Colors.clRed.getColor());
        } else {
            buttonFilter.setText("");
        }
        //JAVA_VIEWER-1262: Filterbutton nicht anzeigen wenn es keine Filter gibt oder alle Filter hidden sind
        if ((getFilter().getFilterList().isEmpty()) || (getFilter().hasOnlyHiddenFilter())) {
            buttonFilter.setVisible(false);
            toolbarManager.hideButton("filterSeparator");
        }
    }

    protected boolean isMovePossible() {
        filteredSelectedHotSpot = null;
        if ((assemblyListForm != null) && (assemblyListForm.getSelectedEntry() != null)) {
            // Bei aktiver Filterung oder Sortierung sollte nicht verschoben werden können, selbst wenn nur ein Eintrag
            // ausgewählt wurde, da sich das Verschieben auf die originale ungefilterte und unsortierte Stückliste auswirkt.
            if (isFiltered || isSorted) {
                if (!isSorted) {
                    // Überprüfe, ob nur der HotSpot-Spaltenfilter gesetzt ist
                    if (assemblyListForm.isColumnFilterActiveOnlyForField(FIELD_K_POS)) {
                        if (checkSelectionGaps()) {
                            filteredSelectedHotSpot = checkSelectionHasSameHotspot();
                            return StrUtils.isValid(filteredSelectedHotSpot);
                        }
                    }
                }
                return false;
            }
            if (assemblyListForm.getSelectionCount() == 1) {
                return true;
            } else {
                // Bei Multiselect auf Lücken in der Selection prüfen
                return checkSelectionGaps();
            }
        }
        return false;
    }

    private boolean checkSelectionGaps() {
        // Bei Multiselect auf Lücken in der Selection prüfen
        int[] selectedRowIndices = assemblyListForm.getPartListTable().getSelectedRowIndices();
        if (selectedRowIndices.length > 1) { // zur Sicherheit nochmal prüfen
            int previousSelectedIndex = selectedRowIndices[0];
            for (int i = 1; i < selectedRowIndices.length; i++) {
                int currentSelectedIndex = selectedRowIndices[i];
                if ((previousSelectedIndex + 1) != currentSelectedIndex) {
                    return false;
                }
                previousSelectedIndex = currentSelectedIndex;
            }
        }
        return true;
    }

    /**
     * Überprüft ob alle selektierten TeilePos den gleichen HotSpot besitzen
     * Liefert nur richtige Ergebnisse, wenn nur SpaltenFilter für Hotspot aktiv ist und keine Lücke in der Selektion vorkommt
     *
     * @return HotSpot; sonst null
     */
    private String checkSelectionHasSameHotspot() {
        String currentHotSpot = assemblyListForm.getSelectedEntry().getFieldValue(FIELD_K_POS);
        if (assemblyListForm.getSelectionCount() == 1) {
            return currentHotSpot;
        }
        List<EtkDataPartListEntry> selectedList = assemblyListForm.getSelectedEntries();
        for (int lfdNr = 1; lfdNr < selectedList.size(); lfdNr++) {
            if (!currentHotSpot.equals(selectedList.get(lfdNr).getFieldValue(FIELD_K_POS))) {
                return null;
            }
        }
        return currentHotSpot;
    }

    /**
     * Darf die Form geschlossen werden?
     *
     * @return <code>true</code> bedeutet dass die Form NICHT geschlossen werden darf
     */
    public boolean askForClose() {
        if (assemblyListForm != null) {
            GuiTableInPlaceEditorFactoryInterface inPlaceEditorFactory = assemblyListForm.getPartListTable().getInPlaceEditorFactory();
            if (inPlaceEditorFactory instanceof GuiTableInplaceEditorManager) {
                GuiTableInplaceEditorManager editorFactory = (GuiTableInplaceEditorManager)inPlaceEditorFactory;
                if (editorFactory.isInplaceEditorModified()) {
                    EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
                    String assemblyName = "";
                    if (currentAssembly != null) {
                        assemblyName = " \"" + currentAssembly.getPart().getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR).getText(getProject().getDBLanguage()) + "\"";
                    }
                    ModalResult modalResult = MessageDialog.showYesNo(TranslationHandler.translate(
                            "!!Zur Stückliste%1 gibt es ungespeicherte Änderungen.%2Sollen diese verworfen werden?",
                            assemblyName, "\n"));
                    if (modalResult == ModalResult.YES) {
                        editorFactory.cancelActiveEditor();
                        return editorFactory.isInplaceEditorActive();
                    } else if (modalResult == ModalResult.NO) {
                        setFocusOnActiveEditor(); // den Focus wieder in den noch weiterhin aktiven Editor setzen
                        return true; // Veto zum schließen
                    }
                }
                // Jetzt könnte es noch einen offenen Editor geben, der darf aber keine Änderung haben, kann also abgebrochen werden
                editorFactory.cancelActiveEditor();
            }
        }
        return false;
    }

    public void setFocusOnActiveEditor() {
        if (assemblyListForm != null) {
            GuiTableInPlaceEditorFactoryInterface inPlaceEditorFactory = assemblyListForm.getPartListTable().getInPlaceEditorFactory();
            if (inPlaceEditorFactory instanceof GuiTableInplaceEditorManager) {
                GuiTableInplaceEditorManager editorFactory = (GuiTableInplaceEditorManager)inPlaceEditorFactory;
                editorFactory.setFocusOnActiveEditor();
            }
        }
    }


    public static class EditAssemblyListView extends AssemblyListForm {

        protected Map<PartListEntryId, Boolean> picOrderPartListEntryIdsForIcons;
        protected Map<PartListEntryId, List<iPartsDataInternalText>> internalTextMap;

        /**
         * Erzeugt eine Instanz von AssemblyListView.
         * Den $$internalCreateGui$$() Aufruf nicht ändern!
         */
        public EditAssemblyListView(AssemblyListFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
            picOrderPartListEntryIdsForIcons = new HashMap<PartListEntryId, Boolean>();
            internalTextMap = new HashMap<>();
            hideEmptyPlaceHolder = true;
        }

        public EtkDisplayFields getDisplayFields() {
            EtkDisplayFields displayFields = new EtkDisplayFields();
            for (EtkDisplayField df : super.getTableHelper().getDesktopDisplayList()) {
                displayFields.addFeld(df);
            }
            return displayFields;
        }

        protected EditAssemblyListForm getParentEditForm() {
            if (parentForm instanceof EditAssemblyListForm) {
                return (EditAssemblyListForm)parentForm;
            }
            return null;
        }

        protected boolean isEditAllowed() {
            EditAssemblyListForm parentForm = getParentEditForm();
            if (parentForm != null) {
                return parentForm.isEditAllowed;
            }
            return false;
        }

        protected GuiButtonPanel getGuiButtonPanel() {
            EditAssemblyListForm parentForm = getParentEditForm();
            if (parentForm != null) {
                return parentForm.getButtonPanel();
            }
            return null;
        }

        protected void enableButtons() {
            EditAssemblyListForm parentForm = getParentEditForm();
            if (parentForm != null) {
                parentForm.enableButtons();
            }
        }

        public EtkDataPartListEntry getPreviousPartListEntry(int index) {
            if (index > 0) {
                PartListEntryUserObjectForTableRow row = getPartListEntryUserObject(table.getRow(index - 1));
                return row.getPartListEntry();
            }
            return null;
        }

        public EtkDataPartListEntry getNextPartListEntry(int index) {
            if (index < (getPartListTable().getRowCount() - 1)) {
                PartListEntryUserObjectForTableRow row = getPartListEntryUserObject(table.getRow(index + 1));
                return row.getPartListEntry();
            }
            return null;
        }

        public boolean isColumnFilterActiveOnlyForField(String fieldName) {
            int colIndex = getColumnIndexFromFieldName(TableAndFieldName.make(TABLE_KATALOG, fieldName));
            if (colIndex != -1) {
                // fieldName ist konfiguriert
                GuiTable table = getPartListTable();
                return ((table.getColumnFilterValuesMap().size() == 1) &&
                        table.hasFilterForColumn(colIndex) &&
                        (table.getColumnFilterValuesMap().get(colIndex) != null));
            }
            return false;
        }

        protected void doMovePartlistEntriesDown(Event event) {
            EditAssemblyListForm parentForm = getParentEditForm();
            if (parentForm != null) {
                parentForm.doMovePartlistEntriesDown(event);
            }
        }

        protected void doMovePartlistEntriesUp(Event event) {
            EditAssemblyListForm parentForm = getParentEditForm();
            if (parentForm != null) {
                parentForm.doMovePartlistEntriesUp(event);
            }
        }

        protected boolean isUpdatePictureOrderIcons() {
            EditAssemblyListForm parentForm = getParentEditForm();
            if (parentForm != null) {
                return parentForm.isUpdatePictureOrderIcons();
            }
            return false;
        }

        protected void setUpdatePictureOrderIcons(boolean value) {
            EditAssemblyListForm parentForm = getParentEditForm();
            if (parentForm != null) {
                parentForm.setUpdatePictureOrderIcons(value);
            }
        }

        @Override
        protected RelatedInfoFormConnector createRelatedInfoConnector() {
            RelatedInfoFormConnector relatedInfoConnector = super.createRelatedInfoConnector();
            if (isEditAllowed()) {
                // EditContext darf nur einmal gesetzt werden, weil ansonsten z.B. innerhalb einer RelatedEdit die gesetzten
                // Informationen wie aktives Edit-ChangeSet verlorengehen können beispielweise beim Löschen aller Caches
                if (relatedInfoConnector.getEditContext() == null) {
                    relatedInfoConnector.setEditContext(iPartsRelatedInfoEditContext.createEditContext(relatedInfoConnector, isEditAllowed()));
                }
            } else {
                relatedInfoConnector.setEditContext(null);
            }
            return relatedInfoConnector;
        }

        @Override
        public EditModuleFormIConnector getConnector() {
            return (EditModuleFormIConnector)super.getConnector();
        }

        @Override
        protected void partsListEntryDblClick(EtkDataPartListEntry partListEntry) {
            GuiButtonPanel guiButtonPanel = getGuiButtonPanel();
            if (guiButtonPanel != null) {
                guiButtonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).doClick();
            }
        }

        @Override
        protected void partlistSelectionChanged() {
            super.partlistSelectionChanged();
            enableButtons();
        }

        @Override
        protected String getEbeneNameForSessionSave() {
            return TableAndFieldName.make(getCurrentAssembly().getEbeneName(), SimpleDoubleListSelectForm.PARTLIST_SOURCE_EDIT);
        }

        @Override
        public GuiTable getPartListTable() {
            TableInterface partListTable = super.getPartListTable();
            if (partListTable != null) {
                if (!(partListTable.getTableGui() instanceof GuiTable)) {
                    throw new WrongTableClassException(this.getClass().getName());
                }
                return (GuiTable)partListTable.getTableGui();
            }
            return null;
        }

        public void gotoPartListEntries(List<EtkDataPartListEntry> partListEntries, boolean scrollToFirst) {
            gotoPartListEntries(partListEntries, scrollToFirst, false);
        }

        public void gotoPartListEntry(EtkDataPartListEntry partListEntry, boolean scrollToFirst) {
            if (partListEntry == null) {
                return;
            }
            List<EtkDataPartListEntry> partListEntries = new DwList<>();
            partListEntries.add(partListEntry);
            gotoPartListEntries(partListEntries, scrollToFirst);
        }

        protected void loadPicOrderListForIcons() {
            //siehe {@link iPartsRelatedInfoPicOrdersToPartData} .isBlocked
            if (!iPartsRelatedInfoPicOrdersToPartData.isBlocked) {
                picOrderPartListEntryIdsForIcons = iPartsDataPicOrderPartsList.loadPicOrderPartsListByModulForIcons(getProject(), getConnector().getCurrentAssembly().getAsId(), getConnector().getPictureOrderList());
            }
        }

        protected void loadInternalTextSet() {
            internalTextMap = iPartsDataInternalTextList.loadInternalTextsForAssembly(getProject(), getConnector().getCurrentAssembly().getAsId());
        }

        @Override
        protected List<EtkDisplayField> getDisplayFieldsForTableHelper() {
            List<EtkDisplayField> fields = super.getDisplayFieldsForTableHelper();
            // Spalten, die bei PSK Modulen nicht angezeigt werden dürfen, müssen hier entfernt werden
            iPartsPSKHelper.handlePSKDisplayFields(getCurrentAssembly(), fields);
            return fields;
        }

        @Override
        public void loadCurrentAssembly() {
            loadPicOrderListForIcons();
            loadInternalTextSet();
            super.loadCurrentAssembly();

            // InPlace-Editor für die Positionsnummer nur bei erlaubtem Edit aktivieren
            if (isEditAllowed()) {

                // in loadCurrentAssembly(); wird die Table neu erzeugt, deshalb muss die Tastatursteuerung immernach super.loadCurrentAssembly()
                // an die Tabelle angefügt werden
                getPartListTable().addEventListener(new EventListener(Event.KEY_RELEASED_EVENT, EventListenerOptions.buildEnumSet(true, true)) {
                    @Override
                    public void fire(Event event) {
                        if (event.getBooleanParameter(Event.EVENT_PARAMETER_STRG_KEY_PRESSED)) {
                            int keyCode = event.getIntParameter(Event.EVENT_PARAMETER_KEY_CODE);
                            if (keyCode == KeyEvent.VK_DOWN) {
                                doMovePartlistEntriesDown(event);
                            }
                            if (keyCode == KeyEvent.VK_UP) {
                                doMovePartlistEntriesUp(event);
                            }
                        }
                    }
                });

                // Die anderen Key-Events nur leer einhängen, damit die Default-Keybehandlung im Browser nicht ausgeführt wird
                getPartListTable().addEventListener(new EventListener(Event.KEY_PRESSED_EVENT, EventListenerOptions.buildEnumSet(true, true)) {
                    @Override
                    public void fire(Event event) {
                        // Tu nix
                    }
                });

                getPartListTable().addEventListener(new EventListener(Event.KEY_TYPED_EVENT, EventListenerOptions.buildEnumSet(true, true)) {
                    @Override
                    public void fire(Event event) {
                        // Tu nix
                    }
                });

                HashMap<String, String> inplaceEditorMap = new HashMap<>();
                inplaceEditorMap.put(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, getConnector().getPosFieldName()), iPartsGuiPosAlphaNumInplaceEditor.class.getName());
                inplaceEditorMap.put(iPartsGuiCodeTextFieldForPartlistInplaceEditor.DEFAULT_TABLE_AND_FIELD_NAME, iPartsGuiCodeTextFieldForPartlistInplaceEditor.class.getName());
                inplaceEditorMap.put(iPartsGuiHierarchyInplaceEditor.DEFAULT_TABLE_AND_FIELD_NAME, iPartsGuiHierarchyInplaceEditor.class.getName());
                inplaceEditorMap.put(iPartsGuiAddTextInplaceEditor.DEFAULT_TABLE_AND_FIELD_NAME, iPartsGuiAddTextInplaceEditor.class.getName());
                inplaceEditorMap.put(iPartsGuiNeutralTextInplaceEditor.DEFAULT_TABLE_AND_FIELD_NAME, iPartsGuiNeutralTextInplaceEditor.class.getName());

                // Menge nur bei Truck-Modulen per Inplace-Editor editierbar machen (deswegen auch keine Vererbung der Menge
                // an andere Module notwendig)
                if (getCurrentAssembly() instanceof iPartsDataAssembly) {
                    if (((iPartsDataAssembly)getCurrentAssembly()).getDocumentationType().isTruckDocumentationType()) {
                        inplaceEditorMap.put(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, EtkDbConst.FIELD_K_MENGE),
                                             iPartsGuiTextFieldInplaceEditor.class.getName());
                    }
                }

                // Die Editoren für den kombinierten Text müssen über ein gemeinsames Objekt kommunizieren
                iPartsGuiCombTextCompleteEditControl combTextCompleteEditControl = new iPartsGuiCombTextCompleteEditControl();
                combTextCompleteEditControl.setConnector(getParentEditForm().getConnector());

                getPartListTable().setInPlaceEditorFactory(new GuiTableInplaceEditorManager(getPartListTable(), inplaceEditorMap) {
                    @Override
                    public int setDisplayfieldForEditor(List<EtkDisplayField> displayFields, String tableAndFieldname, InplaceEditor editor) {
                        int colIndex = getColumnIndexFromFieldName(tableAndFieldname);
                        if (colIndex != -1) {
                            EtkDisplayField field = getDisplayFieldFromFieldName(tableAndFieldname);
                            if (field.isEditierbar()) {
                                editor.setDisplayField(field);
                                return colIndex;
                            }
                        }
                        return -1;
                    }

                    @Override
                    public void initEditor(InplaceEditor editor) {
                        EditAssemblyListForm parentForm = getParentEditForm();
                        if (parentForm != null) {
                            editor.init(parentForm.getConnector());
                            // Beim Initialisieren, das Control für den kombinierten Text an beide Editoren übergeben
                            if (editor instanceof iPartsGuiAddTextInplaceEditor) {
                                ((iPartsGuiAddTextInplaceEditor)editor).setCompleteControl(combTextCompleteEditControl);
                            } else if (editor instanceof iPartsGuiNeutralTextInplaceEditor) {
                                ((iPartsGuiNeutralTextInplaceEditor)editor).setCompleteControl(combTextCompleteEditControl);
                            }
                        }
                    }

                    @Override
                    public void handleSaveEditResult(int column, int row, InplaceEditor editor) {
                        EtkDisplayField displayField = editor.getDisplayField();
                        GuiTableRow tableRow = getPartListTable().getRow(row);
                        EtkDataPartListEntry partListEntry = getPartListEntryUserObject(tableRow).getPartListEntry();
                        String oldValue = partListEntry.getFieldValue(displayField.getKey().getFieldName());
                        String newValue = editor.getEditResult(partListEntry);
                        partListEntry.setAttributeValue(displayField.getKey().getFieldName(), newValue, DBActionOrigin.FROM_EDIT);
                        // Wurde der Ergänzungstext und/oder der sprachneutrale Text geändert, muss der kombinierte Text angepasst werden
                        boolean isCombTextInplaceEditor = (editor instanceof iPartsGuiAddTextInplaceEditor) || (editor instanceof iPartsGuiNeutralTextInplaceEditor);
                        if (isCombTextInplaceEditor && partListEntry.attributeExists(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT)) {
                            iPartsGuiCombTextElementInplaceEditor combTextElementInplaceEditor = (iPartsGuiCombTextElementInplaceEditor)editor;
                            partListEntry.setAttributeValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, combTextElementInplaceEditor.getCombTextCompleteEditControl().getText(), DBActionOrigin.FROM_EDIT);
                        }
                        // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden
                        iPartsDataPartListEntry.resetAutoCreatedFlag(partListEntry);
                        EtkProject project = getProject();
                        Set<AssemblyId> modifiedAssemblyIds = editor.saveAdditionalDataFromInplaceControl(project, partListEntry);

                        // Änderungen sollen sofort ins Changeset gespeichert werden
                        getConnector().savePartListEntries(EditAssemblyListView.this/*Ist hier das übergeordnete ListForm*/,
                                                           false);

                        // Klammer für eine evtl. notwendige Pseudo-Transaktion, um meherere einzelne Pseudo-Transaktionen
                        // zu vermeiden
                        project.startPseudoTransactionForActiveChangeSet(true);
                        try {
                            addOrReplaceCellContent(tableRow, partListEntry, displayField, column);

                            // Veränderte Module müssen aus dem Cache gelöscht und im Edit befindliche Module müssen neu
                            // geladen werden
                            if (modifiedAssemblyIds != null) {
                                updateModifiedAssemblies(project, modifiedAssemblyIds, isCombTextInplaceEditor);
                            }

                            boolean fireDataChangedEvent = false;
                            if (isCombTextInplaceEditor || (editor instanceof iPartsGuiCodeTextFieldForPartlistInplaceEditor)) {
                                fireDataChangedEvent = true;
                            } else if (!oldValue.equals(newValue)) {
                                if (editor instanceof iPartsGuiPosAlphaNumInplaceEditor) {
                                    getConnector().posNumberChanged();

                                    // Die Ersetzungen von allen Stücklisteneinträgen der betroffenen Hotspots (alter
                                    // und neuer Wert) müssen aktualisiert werden
                                    Set<String> hotspots = new HashSet<>();
                                    hotspots.add(oldValue);
                                    hotspots.add(newValue);
                                    EditModuleHelper.updateReplacementsAndFailLocationsForPLEsForHotspots(getConnector(), hotspots);
                                }
                                getConnector().updateAllViews(null, false);
                                fireDataChangedEvent = true;
                            }

                            if (fireDataChangedEvent) {
                                getConnector().getProject().fireProjectEvent(new DataChangedEvent(), true);
                            }
                        } finally {
                            project.stopPseudoTransactionForActiveChangeSet();
                        }
                    }

                    @Override
                    public List<EtkDataObject> getDataObjectsForRow(int row) {
                        GuiTableRow tableRow = getPartListTable().getRow(row);
                        if (tableRow == null) {
                            return null;
                        }
                        EtkDataPartListEntry partListEntry = getPartListEntryUserObject(tableRow).getPartListEntry();
                        List<EtkDataObject> dataObjects = new DwList<>();
                        dataObjects.add(partListEntry);
                        return dataObjects;
                    }

                    @Override
                    public AbstractGuiControl createInPlaceEditor(int column, int row, Object cellContent, int width) {
                        AbstractGuiControl control = super.createInPlaceEditor(column, row, cellContent, width);
                        List<EtkDataPartListEntry> selectedEntries = getSelectedEntries();
                        if ((selectedEntries != null) && (selectedEntries.size() == 1) && iPartsLockEntryHelper.isLockedWithDBCheck(selectedEntries.get(0))) {
                            // Ist die Position gesperrt, darf kein InPlaceEditor aktiv sein, außer der mit den
                            // kombinierten Texten. Kombinierte Texte sind keine direkten Attribute und dürfen daher
                            // verändert werden.
                            if (!iPartsLockEntryHelper.isValidInplaceEditor(control.getType())) {
                                // Den aktiven InPlaceEditor in der Tabelle entfernen
                                cancelActiveEditor();
                                // Den aktiven InPlaceEditor im Manager entfernen
                                cancelInPlaceEditor(-1, -1, null);
                                return null;
                            }
                        }
                        return control;
                    }

                    @Override
                    public void onInplaceEditorActive(boolean isActive) {
                        // jedes mal wenn ein Editor aktiv bzw. inaktiv wird muss die Sichtbarkeit Edit Buttons in der
                        // Stückliste neu bestimmt werden
                        enableButtons();
                    }
                });
            }

            getPartListTable().setSortByPopupMenu(true);
        }

        private void updateModifiedAssemblies(EtkProject project, Set<AssemblyId> modifiedAssemblyIds, boolean isCombTextInplaceEditor) {
            for (AssemblyId equalizedAssemblyId : modifiedAssemblyIds) {
                EtkDataAssembly.removeDataAssemblyFromCache(project, equalizedAssemblyId);
            }

            // Wenn das aktuelle Modul auch verändert wurde, dann dieses separat aktualisieren und nicht
            // iPartsEditPlugin.reloadModulesInEdit() dafür verwenden für bessere Performance
            if (modifiedAssemblyIds.remove(getCurrentAssembly().getAsId())) {
                getCurrentAssembly().clearPartListEntriesForEdit();

                // Beim Inplace-Editor für kombinierte Texte reicht es, das virtuelle Feld für die
                // kombinierten Texte bei allen Stücklisteneinträgen neu zu berechnen; bei allen anderen
                // Inplace-Editor muss die Stückliste auch im Edit neu geladen werden
                if (getCurrentAssembly() instanceof iPartsDataAssembly) {
                    if (isCombTextInplaceEditor) {
                        EtkDisplayFields fields = new EtkDisplayFields();
                        fields.addFeld(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT, false, false, null, project);
                        fields.addFeld(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL, false, false, null, project);
                        ((iPartsDataAssembly)getCurrentAssembly()).addVirtualCombinedTextFieldToPartlist(getCurrentAssembly().getPartListUnfiltered(null), fields);
                    } else {
                        getConnector().setCurrentAssembly(EtkDataObjectFactory.createDataAssembly(project,
                                                                                                  getCurrentAssembly().getAsId()));
                    }
                }
            }

            iPartsEditPlugin.reloadModulesInEdit(modifiedAssemblyIds, getConnector());
        }

        /**
         * Wird die RTable oder die GuiTable verwendet
         *
         * @return
         */
        @Override
        protected boolean useRTable() {
            // Funktioniert noch nicht mit dem RTable
            return false;
        }


        @Override
        protected void refreshPartListEntriesColors() {
            if (getPartListTable() == null) {
                // Die Stückliste wurde bisher noch nie geladen, weil das Form noch nicht angezeigt wurde
                return;
            }

            Color backgroundColor = iPartsEditPlugin.clPlugin_iPartsEdit_TableNotLinkedBackgroundColor.getColor();
            super.refreshPartListEntriesColors();
            Set<String> hotspotSet = getConnector().getHotspotSet();
            String fieldName = getConnector().getPosFieldName();

            for (int rowNo = 0; rowNo < getPartListTable().getRowCount(); rowNo++) {
                boolean setColor = false;
                EtkDataPartListEntry partListEntry = getPartListEntryUserObject(getPartListTable().getRow(rowNo)).getPartListEntry();
                if (!partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT)) {
                    String kPos = partListEntry.getFieldValue(fieldName);
                    if (!kPos.isEmpty()) {
                        List<String> posNumberList = GuiViewerUtils.splitPosNumber(kPos);
                        for (String posNumber : posNumberList) {
                            if (!getConnector().isAlphaNumHotspotAllowed() && !StrUtils.isInteger(posNumber)) {
                                setColor = true;
                                break;
                            } else if (!hotspotSet.contains(posNumber)) {
                                setColor = true;
                                break;
                            }
                        }
                    }
                }
                if (setColor) {
                    getPartListTable().getRow(rowNo).setBackgroundColor(backgroundColor);
                }
            }
        }

        @Override
        public List<AssemblyListCellContent> buildIconColumn(EtkDataPartListEntry partListEntry) {
            List<AssemblyListCellContent> iconColumn = super.buildIconColumn(partListEntry);
            //hier weitere Modifikationen für Icons
            //Abfrage, ob partListEntry einem Bildauftrag zugeordnet ist
            // entfällt mit DAIMLER-7902
//            boolean isPicOrderRelated = picOrderPartListEntryIdsForIcons.containsKey(partListEntry.getAsId());
//            if (isPicOrderRelated) {
//                EditToolbarButtonAlias toolbarButtonAlias = EditToolbarButtonAlias.IMG_IMGORDER;
//                if (!picOrderPartListEntryIdsForIcons.get(partListEntry.getAsId())) {
//                    toolbarButtonAlias = EditToolbarButtonAlias.IMG_IMGORDER_CLOSED;
//                }
//                AssemblyListCellContentFromPlugin info = new AssemblyListCellContentFromPlugin(iPartsRelatedInfoPicOrdersToPartDataForm.PICORDERS_TO_PART_DATA_NAME,
//                                                                                               toolbarButtonAlias.getImage());
//                info.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
//                String hint = "!!einem Bildauftrag zugeordnet";
//                if (toolbarButtonAlias == EditToolbarButtonAlias.IMG_IMGORDER_CLOSED) {
//                    hint = "!!einem geschlossenen Bildauftrag zugeordnet";
//                }
//                info.setHint(TranslationHandler.translate(hint));
//                info.setCursor(DWCursor.Hand);
//                info.setCellContent(EtkConfigConst.IMGPREFIX + toolbarButtonAlias.getAlias());
//                iconColumn.add(info);
//            }
            if (partListEntry instanceof iPartsDataPartListEntry) {
                List<iPartsDataInternalText> internalTexts = internalTextMap.get(partListEntry.getAsId());
                String currentInternalText = null;
                if ((internalTexts != null) && !internalTexts.isEmpty()) {
                    currentInternalText = internalTexts.get(0).getText();
                }
                iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                iPartsPartListEntry.setCurrentInternalText(currentInternalText);

                if (iPartsPartListEntry.hasInternalText()) {
                    AssemblyListCellContentFromPlugin info = new AssemblyListCellContentFromPlugin(iPartsRelatedInfoInternalTextForPartDataForm.INTERNAL_TEXT_FOR_PART_DATA_NAME,
                                                                                                   EditToolbarButtonAlias.EDIT_INTERNAL_TEXT.getImage());
                    info.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
                    String hint = "!!Interner Text vorhanden";
                    info.setHint(TranslationHandler.translate(hint));
                    info.setCursor(DWCursor.Hand);
                    info.setCellContent(EtkConfigConst.IMGPREFIX + EditToolbarButtonAlias.EDIT_INTERNAL_TEXT.getAlias());
                    iconColumn.add(info);
                }
            }
            return iconColumn;
        }

        @Override
        public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
            // Gefilterte Stücklistendaten löschen (speziell filteredWithModelNumber), weil im Edit die Stückliste nicht
            // zwangsweise neugeladen wird (nur notwendig, wenn die Stückliste schon mal geladen wurde, also bei table != null)
            if ((table != null) && (forceUpdateAll || getConnector().isFlagFilterChanged() || getConnector().isFlagDataChanged())) {
                EtkDataAssembly assembly = getConnector().getCurrentAssembly();
                if (assembly != null) {
                    assembly.clearFilteredPartLists();
                    getConnector().clearFilteredEditPartListEntries();
                    assembly.ensureAllAdditionalDataIsLoaded(assembly.getPartListUnfiltered(getEbene()), getEbene());
                }
            }

            // Position des Scrollers merken, damit sie nach dem updateData()-Aufruf wiederhergestellt werden kann
            int scrollPosX = -1;
            if (getPartListTable() != null) {
                DWPoint scrollPos = getPartListTable().getCurrentScrollPos();
                if (scrollPos != null) {
                    scrollPosX = scrollPos.getX();
                }
            }

            super.updateData(sender, forceUpdateAll);

            getPartListTable().scrollToHorizontal(scrollPosX);

            if (!forceUpdateAll && getConnector().isFlagPosNumberChanged()) {
                clearPartListEntriesForNormalLinksCache();
                refreshPartListEntriesColors();
            }

            // Bei forceUpdateAll wird sowieso die gesamte Stückliste neu geladen
            if (!forceUpdateAll && getConnector().isFlagPictureOrderChanged()) {
                // Die erste Aktualisierung nach dem Laden des Moduls mit vorhandenen Einträgen ignorieren, um doppelte
                // Updates zu vermeiden
                if (!isUpdatePictureOrderIcons()) {
                    setUpdatePictureOrderIcons(true);
                } else {
                    loadPicOrderListForIcons();
                    updateIconCell(table, null, table.getFirstDisplayedRowIndex(), table.getLastDisplayedRowIndex() + 1, true);
                }
            }

            enableButtons();
        }

        /**
         * Weil wir im Edit sind, muss das {@link EtkHotspotLinkHelper.PartsStateResult} auf Basis der ungefilterten
         * Bilder berechnet werden.
         *
         * @param hotspotLinkHelper
         * @param partListEntry
         * @return
         */
        @Override
        protected EtkHotspotLinkHelper.PartsStateResult getImageIndexState(EtkHotspotLinkHelper
                                                                                   hotspotLinkHelper, EtkDataPartListEntry partListEntry) {
            return hotspotLinkHelper.getPartsState(partListEntry, getConnector().isMultiImageViewActive(), getConnector().getImageIndex(), getConnector().isImageIs3D(), false);
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextMenuTable;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel toolbarHolderPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbarAssemblyEdit;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextMenuTable = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextMenuTable.setName("contextMenuTable");
            contextMenuTable.__internal_setGenerationDpi(96);
            contextMenuTable.registerTranslationHandler(translationHandler);
            contextMenuTable.setScaleForResolution(true);
            contextMenuTable.setMinimumWidth(10);
            contextMenuTable.setMinimumHeight(10);
            contextMenuTable.setMenuName("contextMenuTable");
            contextMenuTable.setParentControl(this);
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            toolbarHolderPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            toolbarHolderPanel.setName("toolbarHolderPanel");
            toolbarHolderPanel.__internal_setGenerationDpi(96);
            toolbarHolderPanel.registerTranslationHandler(translationHandler);
            toolbarHolderPanel.setScaleForResolution(true);
            toolbarHolderPanel.setMinimumWidth(10);
            toolbarHolderPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder toolbarHolderPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            toolbarHolderPanel.setLayout(toolbarHolderPanelLayout);
            toolbarAssemblyEdit = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbarAssemblyEdit.setName("toolbarAssemblyEdit");
            toolbarAssemblyEdit.__internal_setGenerationDpi(96);
            toolbarAssemblyEdit.registerTranslationHandler(translationHandler);
            toolbarAssemblyEdit.setScaleForResolution(true);
            toolbarAssemblyEdit.setMinimumWidth(10);
            toolbarAssemblyEdit.setMinimumHeight(10);
            toolbarAssemblyEdit.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarAssemblyEditConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarAssemblyEdit.setConstraints(toolbarAssemblyEditConstraints);
            toolbarHolderPanel.addChild(toolbarAssemblyEdit);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarHolderPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarHolderPanelConstraints.setPosition("north");
            toolbarHolderPanel.setConstraints(toolbarHolderPanelConstraints);
            panelMain.addChild(toolbarHolderPanel);
            panelGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGrid.setName("panelGrid");
            panelGrid.__internal_setGenerationDpi(96);
            panelGrid.registerTranslationHandler(translationHandler);
            panelGrid.setScaleForResolution(true);
            panelGrid.setMinimumWidth(10);
            panelGrid.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelGrid.setLayout(panelGridLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelGrid.setConstraints(panelGridConstraints);
            panelMain.addChild(panelGrid);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCancelActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    btnCancelActionPerformedEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}