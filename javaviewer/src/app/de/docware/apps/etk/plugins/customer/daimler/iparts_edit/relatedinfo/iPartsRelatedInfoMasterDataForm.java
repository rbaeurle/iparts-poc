/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditMatrixUserControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControlForSAorSAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchResultGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary.DictShowTextKindForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsMatrixEditFields;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Formular für die Stammdaten sowie SA-Stammdaten innerhalb der RelatedInfo
 * <p>
 * Später erweitert zur Aufnahme eines DataObjectGrid in der unteren Dialoghälfte. Mechanismen von AbstractTwoDataObjectGridsForm abgeschaut.
 */
public class iPartsRelatedInfoMasterDataForm extends AbstractRelatedInfoPartlistDataForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_MATERIAL = "iPartsMenuItemShowMaterial";
    public static final String IPARTS_MENU_ITEM_SHOW_MASTER_DATA = "iPartsMenuItemShowMasterData";
    public static final String IPARTS_MENU_ITEM_SHOW_SA_MASTER_DATA = "iPartsMenuItemShowSaMasterData";
    public static final String IPARTS_CONFIG_KEY_SA_MASTERDATA = "Plugin/iPartsEdit/SaMasterdata";
    public static final String IPARTS_CONFIG_KEY_SAA_TO_SA_MASTERDATA = "Plugin/iPartsEdit/SaaToSaMasterdata";
    public static final String IPARTS_CONFIG_KEY_CONSTRUCTION_MODEL_EDS_MASTERDATA = "Plugin/iPartsEdit/ConstructionModelEDSMasterData";
    public static final String IPARTS_CONFIG_KEY_CONSTRUCTION_EDS_SAA_MASTERDATA = "Plugin/iPartsEdit/ConstructionEDSSAAMasterData";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES_FOR_SA = EnumSet.of(iPartsModuleTypes.SA_TU);

    private static String[] FIELDS_FOR_MODIFY = new String[]{ FIELD_DS_DESC };

    private static final String SA_MASTERDATA_FN_ID_ADDITIONAL_FIELD = VirtualFieldsUtils.addVirtualFieldMask("SA_MASTERDATA_FN_ID_ADDITIONAL_FIELD");
    private static final String SA_MASTERDATA_FN_ADDITIONAL_FIELD = VirtualFieldsUtils.addVirtualFieldMask("SA_MASTERDATA_FN_ADDITIONAL_FIELD");
    protected String titleGrid;
    protected SaaDataObjectFilterGrid grid; // Grid in der unteren Dialoghälfte; nicht immer sichtbar
    protected ToolbarButtonMenuHelper.ToolbarMenuHolder workHolder;
    protected boolean isEditAllowed;
    protected double splitPaneDividerRatio = 0.5d;
    protected iPartsSaId externalSAId;
    private EditUserControlContainer eCtrl;

    public iPartsRelatedInfoMasterDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IEtkRelatedInfo relatedInfo) {
        this(dataConnector, parentForm, relatedInfo, null);
    }

    public iPartsRelatedInfoMasterDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           IEtkRelatedInfo relatedInfo, iPartsSaId externalSAId) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
        this.externalSAId = externalSAId;
        createMasterData();
    }

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_MATERIAL, "!!Material anzeigen", DefaultImages.part.getImage(),
                                iPartsConst.CONFIG_KEY_RELATED_INFO_MASTER_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        boolean isValidAssemblyForShowMaterial = false;
        boolean menuItemNameMasterData = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            EtkDataAssembly assembly = connector.getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                if (iPartsAssembly.getAsId().isVirtual()) {
                    List<iPartsVirtualNode> virtualNodesPath = iPartsAssembly.getVirtualNodesPath();
                    if (virtualNodesPath != null) {
                        if (iPartsVirtualNode.isHmMSmNode(virtualNodesPath)
                            || iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)
                            || iPartsVirtualNode.isCTTSaaConstNode(virtualNodesPath)) {
                            isValidAssemblyForShowMaterial = true;
                        } else if (iPartsVirtualNode.isStructureNode(virtualNodesPath)
                                   || iPartsVirtualNode.isProductKgTuNode(virtualNodesPath)
                                   || iPartsVirtualNode.isNodeWithinMBSConstStructure(virtualNodesPath)
                                   || iPartsVirtualNode.isNodeWithinCTTConstStructure(virtualNodesPath)
                                   || iPartsEdsStructureHelper.isEdsStructureNode(virtualNodesPath)) {
                            // in dem Strukturknoten den selektierten Stücklisteneintrag überprüfen, ob dessen Assembly ein
                            // Produkt oder eine Baureihe darstellt
                            List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
                            if ((selectedPartListEntries != null) && !selectedPartListEntries.isEmpty()) {
                                EtkDataPartListEntry partListEntry = selectedPartListEntries.get(0);
                                if (partListEntry instanceof iPartsDataPartListEntry) {
                                    virtualNodesPath = ((iPartsDataPartListEntry)partListEntry).getVirtualNodesPathForDestinationAssembly();
                                    if (virtualNodesPath != null) {
                                        if (iPartsVirtualNode.isProductNode(virtualNodesPath)
                                            || iPartsVirtualNode.isSeriesNode(virtualNodesPath)
                                            || iPartsVirtualNode.isKgSaNode(virtualNodesPath)
                                            || iPartsVirtualNode.isModelNode(virtualNodesPath)
                                            || iPartsVirtualNode.isMBSNode(virtualNodesPath)
                                            || iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)) {
                                            // Überprüfung für virtuelle Stücklisten
                                            isValidAssemblyForShowMaterial = true;
                                            menuItemNameMasterData = true;
                                        } else { // Überprüfung für Retail-Stückliste
                                            EtkDataAssembly destAssembly = EtkDataObjectFactory.createDataAssembly(connector.getProject(),
                                                                                                                   partListEntry.getDestinationAssemblyId());
                                            destAssembly = destAssembly.getLastHiddenSingleSubAssemblyOrThis(null);
                                            if ((destAssembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)destAssembly).isPartListEditable()) {
                                                isValidAssemblyForShowMaterial = true;
                                                menuItemNameMasterData = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    isValidAssemblyForShowMaterial = true;
                }
            } else {
                isValidAssemblyForShowMaterial = true;
            }
        }

        // Im Edit nur Menüeinträge zur Bearbeitung anzeigen
        if (isValidAssemblyForShowMaterial && isEditContext(connector, true)) {
            isValidAssemblyForShowMaterial = false;
        }

        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_MATERIAL, isValidAssemblyForShowMaterial);

        // Text vom Menüeintrag "Material anzeigen" aktualisieren
        for (AbstractGuiControl child : popupMenu.getChildren()) {
            if (child.getUserObject() != null) {
                if (child.getUserObject().equals(IPARTS_MENU_ITEM_SHOW_MATERIAL)) {
                    if (menuItemNameMasterData) {
                        ((GuiMenuItem)child).setText("!!Stammdaten anzeigen");
                    } else {
                        ((GuiMenuItem)child).setText("!!Material anzeigen");
                    }
                }
            }
        }
    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_MASTER_DATA, "!!Stammdaten anzeigen", iPartsConst.CONFIG_KEY_RELATED_INFO_MASTER_DATA);
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        if (assembly != null) {
            assembly = assembly.getLastHiddenSingleSubAssemblyOrThis(null);
            for (AbstractGuiControl item : popupMenu.getChildren()) {
                if ((item.getUserObject() != null) && item.getUserObject().equals(IPARTS_MENU_ITEM_SHOW_MASTER_DATA)) {
                    boolean itemVisible = false;
                    if (assembly instanceof iPartsDataAssembly) {
                        iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                        List<iPartsVirtualNode> virtualNodesPath = iPartsAssembly.getVirtualNodesPath();
                        if (iPartsVirtualNode.isSeriesNode(virtualNodesPath)
                            || iPartsVirtualNode.isProductNode(virtualNodesPath)
                            || iPartsVirtualNode.isKgSaNode(virtualNodesPath)
                            || iPartsVirtualNode.isModelNode(virtualNodesPath)
                            || iPartsAssembly.isPartListEditable()
                            || iPartsVirtualNode.isMBSNode(virtualNodesPath)
                            || iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)) {
                            itemVisible = true;
                        }
                    }
                    item.setVisible(itemVisible);
                    break;
                }
            }
        }
    }

    public static void modifyPartListPopupMenuForSA(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_SA_MASTER_DATA, "!!SA-Stammdaten anzeigen",
                                DefaultImages.part.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_SA_MASTER_DATA);
    }

    public static void updatePartListPopupMenuForSA(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        boolean isValidAssemblyForShowSAMasterData = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            EtkDataAssembly assembly = connector.getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                if (iPartsAssembly.getAsId().isVirtual()) {
                    List<iPartsVirtualNode> virtualNodesPath = iPartsAssembly.getVirtualNodesPath();
                    if (virtualNodesPath != null) {
                        if (iPartsVirtualNode.isProductKgTuNode(virtualNodesPath)) {
                            // in dem KG/TU-Knoten den selektierten Stücklisteneintrag überprüfen, ob dessen Assembly eine SA ist
                            List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
                            if ((selectedPartListEntries != null) && !selectedPartListEntries.isEmpty()) {
                                EtkDataPartListEntry partListEntry = selectedPartListEntries.get(0);
                                if (partListEntry instanceof iPartsDataPartListEntry) {
                                    virtualNodesPath = ((iPartsDataPartListEntry)partListEntry).getVirtualNodesPathForDestinationAssembly();
                                    if (virtualNodesPath != null) {
                                        if (iPartsVirtualNode.isKgSaNode(virtualNodesPath)) {
                                            isValidAssemblyForShowSAMasterData = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_SA_MASTER_DATA, isValidAssemblyForShowSAMasterData);
    }

    public static void modifyTreePopupMenuForSA(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_SA_MASTER_DATA, "!!SA-Stammdaten anzeigen", iPartsConst.CONFIG_KEY_RELATED_INFO_SA_MASTER_DATA);
    }

    public static void updateTreePopupMenuForSA(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_SA_MASTER_DATA, VALID_MODULE_TYPES_FOR_SA);
    }

    public static boolean relatedInfoIsVisibleForSA(EtkDataAssembly assembly) {
        if (assembly != null) {
            return AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(assembly, VALID_MODULE_TYPES_FOR_SA);
        }
        return false;
    }

    public static void showRelatedInfoMasterDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                     iPartsSaId saId, String title) {
        PartId partId = new PartId(saId.getSaNumber(), "");
        dataConnector.getRelatedInfoData().setKatInfosForAssembly(null, null, partId, null,
                                                                  new NavigationPath());
        iPartsRelatedInfoMasterDataForm dlg = new iPartsRelatedInfoMasterDataForm(dataConnector, parentForm, null, saId);
        dlg.setWindowTitle(title, title);
        dlg.doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.SCALE_FROM_PARENT);
        dlg.showModal();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    protected void postCreateGui() {
    }

    protected SaaDataObjectFilterGrid createGrid() {
        return new SaaDataObjectFilterGrid(getConnector(), this);
    }

    @Override
    public GuiPanel getGui() {
        return mainWindow.mainPanel;
    }

    public GuiWindow getWindow() {
        return mainWindow;
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
                    mainWindow.setSize(width - iPartsConst.CASCADING_WINDOW_OFFSET_WIDTH, height - iPartsConst.CASCADING_WINDOW_OFFSET_HEIGHT);
                }
                break;
        }
    }

    /**
     * Zeigt den Dialog als modales Fenster. Vorher sollte {@link #setWindowTitle(String, String)} aufgerufen werden,
     * um den Titel des Fensters und den Untertitel zu setzen.
     */
    public void showModal() {
        mainWindow.showModal();
        close();
    }

    public void setWindowTitle(String windowTitle, String subTitle) {
        mainWindow.setTitle(windowTitle);
        mainWindow.title.setTitle(subTitle);
    }

    private String getTableNameFromId(IdWithType id) {
        String tableName = null;
        if (id.getType().equals(iPartsSeriesId.TYPE)) {
            tableName = iPartsConst.TABLE_DA_SERIES;
        } else if (id.getType().equals(iPartsProductId.TYPE)) {
            tableName = iPartsConst.TABLE_DA_PRODUCT;
        } else if (id.getType().equals(PartId.TYPE) && !new iPartsPartId(id).isVirtual()) {
            tableName = EtkDbConst.TABLE_MAT;
        } else if (id.getType().equals(EdsSaaId.TYPE)) {
            tableName = iPartsConst.TABLE_DA_SAA;
        } else if (id.getType().equals(iPartsSaId.TYPE)) {
            tableName = iPartsConst.TABLE_DA_SA;
        } else if (id.getType().equals(iPartsModelId.TYPE)) {
            tableName = iPartsConst.TABLE_DA_MODEL;
        }
        return tableName;
    }

    private IdWithType modifyId(IdWithType masterId) {
        IdWithType resultId = masterId;
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), getConnector().getRelatedInfoData().getSachAssemblyId());

        // getLastHiddenSingleSubAssemblyOrThis() ist notwendig für ausgeblendete Modul-Knoten im Baugruppenbaum
        assembly = assembly.getLastHiddenSingleSubAssemblyOrThis(null);

        if (assembly instanceof iPartsDataAssembly) {
            List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
            if (iPartsVirtualNode.isProductNode(virtualNodesPath)) {
                resultId = virtualNodesPath.get(0).getId(); // ProductId
            } else if (iPartsVirtualNode.isSeriesNode(virtualNodesPath)) {
                resultId = virtualNodesPath.get(0).getId(); // SeriesId
            } else if (iPartsVirtualNode.isKgSaNode(virtualNodesPath)) { // SA im Baugruppenbaum/Stückliste
                // Aktuell wird nur für die Stammdaten der SAs ein Grid benötigt
                resultId = ((KgSaId)virtualNodesPath.get(1).getId()).getSaId();
                setGridVisible(true);
                setGridTitle("!!SAAs zur SA");
            } else if (assembly.getEbeneName().equals(iPartsModuleTypes.SA_TU.getDbValue())) { // SA in "Modul bearbeiten"
                iPartsDataSAModulesList dataSAModulesList = iPartsDataSAModulesList.loadDataForModule(getProject(), new iPartsModuleId(assembly.getAsId().getKVari()));
                if (!dataSAModulesList.isEmpty()) { // Es handelt sich um ein SA-Modul
                    String saNumber = dataSAModulesList.get(0).getAsId().getSaNumber();
                    resultId = new iPartsSaId(saNumber);
                    setGridVisible(true);
                    setGridTitle("!!SAAs zur SA");
                }
            } else if (iPartsVirtualNode.isModelNode(virtualNodesPath)) {
                resultId = virtualNodesPath.get(0).getId();
            } else if (iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)) {
                resultId = virtualNodesPath.get(2).getId();
            } else if (iPartsVirtualNode.isMBSNode(virtualNodesPath)) {
                resultId = extractResultIdFromMBSNode(virtualNodesPath);
            } else if (resultId.getType().equals(iPartsPartId.TYPE) && !new iPartsPartId(resultId).isVirtual()) {
                resultId = getConnector().getRelatedInfoData().getAsPartId();
            }
        }
        return resultId;
    }

    /**
     * Extrahiert aus einer {@link MBSStructureId} auf Basis des Sachkennbuchstabens die dazugehörige {@link PartId}
     * oder {@link EdsSaaId}
     *
     * @param virtualNodesPath
     * @return
     */
    private IdWithType extractResultIdFromMBSNode(List<iPartsVirtualNode> virtualNodesPath) {
        // Beide Ebenen der MBS Struktur sind prinzipiell GS/SAAs. Check, ob es die obere oder die untere
        // Nummer ist und dann diese als EdsSaaId weitergeben.
        MBSStructureId structureId = (MBSStructureId)virtualNodesPath.get(1).getId();
        String number = null;
        if (structureId.isListNumberNode()) {
            number = structureId.getListNumber();
        } else if (structureId.isConGroupNode()) {
            number = structureId.getConGroup();
        }
        if (StrUtils.isValid(number)) {
            // GS und SAAs haben ihre Stammdaten in DA_SAA. Alle anderen Nummer beziehen ihre Daten aus MAT
            if (number.startsWith(SAA_NUMBER_PREFIX) || number.startsWith(BASE_LIST_NUMBER_PREFIX)) {
                return new EdsSaaId(number);
            } else {
                return new PartId(number, "");
            }
        }
        return null;
    }

    private void createMasterData() {
        setGridVisible(false); // Default: Grid wird nicht angezeigt
        isEditAllowed = false;

        // Stammdaten-Control
        IdWithType id;
        if ((externalSAId != null) && !externalSAId.isEmpty()) {
            // Aufruf auf SA-Stammdaten Dialog
            setGridVisible(true);
            setGridTitle("!!SAAs zur SA");
            id = externalSAId;
        } else {
            // normale Stammdaten Info; Bestimmung der Anzeige durch Navigations Pfad
            id = modifyId(getConnector().getRelatedInfoData().getAsPartId());
        }
        setEditControl(id);
        mainWindow.panelTop.addChildBorderCenter(eCtrl.getGui());
        if (isGridVisible()) {
            isEditAllowed = DictShowTextKindForm.isCurrentUserProductAdministrator();
            mainWindow.labelDataObjectGridTitle.setText(titleGrid);
            grid = createGrid();
            grid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
            mainWindow.panelDataObjectGrid.addChildBorderCenter(grid.getGui());
            dataToGrid(id);
        }
        doEnableButtons();
    }

    /**
     * Erstellt das anzuzeigende EditControl
     *
     * @param id
     */
    public void setEditControl(IdWithType id) {
        String tableName = getTableNameFromId(id);
        EtkDisplayFields displayFields = new EtkDisplayFields();
        // Abhängig von der ID werden die jeweiligen DisplayFields geladen
        if (id.getType().equals(iPartsSaId.TYPE)) {
            displayFields = getDisplayFields(IPARTS_CONFIG_KEY_SA_MASTERDATA);
        } else if (id.getType().equals(iPartsModelId.TYPE)) {
            displayFields = getDisplayFields(IPARTS_CONFIG_KEY_CONSTRUCTION_MODEL_EDS_MASTERDATA);
        } else if (id.getType().equals(EdsSaaId.TYPE)) {
            displayFields = getDisplayFields(IPARTS_CONFIG_KEY_CONSTRUCTION_EDS_SAA_MASTERDATA);
        }
        eCtrl = new EditUserControlContainer(getConnector(), this, tableName, id, displayFields, true);
    }

    public void setGridTitle(String gridBottomTitle) {
        if (StrUtils.isValid(gridBottomTitle)) {
            mainWindow.labelDataObjectGridTitle.setText(gridBottomTitle);
            mainWindow.labelDataObjectGridTitle.setVisible(true);
        } else {
            mainWindow.labelDataObjectGridTitle.setVisible(false);
        }
    }

    protected GuiButtonPanel getButtonPanel() {
        return mainWindow.buttonPanel;
    }

    protected GuiPanel getBottomPanel() {
        return mainWindow.panelBottom;
    }

    protected GuiPanel getTopPanel() {
        return mainWindow.panelTop;
    }

    private boolean isGridVisible() {
        return mainWindow.panelBottom.getParent() != null;
    }

    public void setGridVisible(boolean visible) {
        if (visible != isGridVisible()) {
            if (visible) {
                mainWindow.panelTop.removeFromParent();
                mainWindow.splitPane.removeAllChildren();
                mainWindow.splitPane.addChild(mainWindow.panelTop);
                mainWindow.splitPane.addChild(mainWindow.panelBottom);
                mainWindow.mainPanel.addChild(mainWindow.splitPane);
                mainWindow.splitPane.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.splitPane) {
                    @Override
                    public void fireOnce(Event event) {
                        int height = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                        mainWindow.splitPane.setDividerPosition(height - (int)(height * splitPaneDividerRatio));
                    }
                });
            } else {
                mainWindow.panelBottom.removeFromParent();
                mainWindow.splitPane.removeFromParent();
                mainWindow.panelTop.setConstraints(mainWindow.splitPane.getConstraints());
                mainWindow.mainPanel.addChild(mainWindow.panelTop);
            }
        }
    }

    /**
     * Befüllt das Grid
     *
     * @param id
     */
    protected void dataToGrid(IdWithType id) {

        IdWithType currentId;
        if ((externalSAId != null) && !externalSAId.isEmpty()) {
            setGridVisible(true);
            setGridTitle("!!SAAs zur SA");
            currentId = externalSAId;
        } else {
            currentId = modifyId(getConnector().getRelatedInfoData().getAsPartId());
        }
        if (eCtrl.idIsChanged(currentId)) {
            createMasterData();
        }
        if (isGridVisible()) {
            if (grid.getDisplayFields() == null) {
                grid.setDisplayFields(getDiplayFieldsForGrid(id));
            }

            EtkDataObjectList dataObjectList = retrieveDataForGrid(id);
            if (dataObjectList == null) {
                return;
            }
            fillGrid(grid, dataObjectList);
            mainWindow.splitPane.setResizeWeight(splitPaneDividerRatio);
        }
    }

    /**
     * Liefert die Liste mit den anzuzeigenden {@link EtkDataObject}s zurück
     *
     * @param id
     * @return
     */
    private EtkDataObjectList retrieveDataForGrid(IdWithType id) {
        if (id.getType().equals(iPartsSaId.TYPE)) {
            return retrieveSaMasterDataForGrid(id);
        }
        return null;
    }

    /**
     * Lädt die SAA Daten aus der DB (Tabelle DA_SAA). Fussnotenummer und Texte werden in Platzhalter Spalten angezeigt,
     * da die Daten bearbeitet werden müssen, z.B. mehrere Fussnoten pro SAA -> mehrzeilige Zelle im Grid
     *
     * @param id
     * @return
     */
    private EtkDataObjectList retrieveSaMasterDataForGrid(IdWithType id) {
        final String dbLanguage = getProject().getDBLanguage();
        EtkDisplayFields selectFields = getSelectFieldsForSAAToSAMasterData();
        final iPartsDataSaaList dataObjectList = new iPartsDataSaaList();
        final Map<String, iPartsDataSaa> saaDescriptionMap = new TreeMap<>();

        // Like-Abfrage, da auf den SA-Rumpf im  SAA Feld FIELD_DS_SAA abgefragt wird
        dataObjectList.searchSortAndFillWithJoin(getProject(), dbLanguage, selectFields,
                                                 // WhereFields und WhereValues = SA-Rumpf mit Wildcard und der Datensatztyp in der Tabelle DA_SAA)
                                                 new String[]{ TableAndFieldName.make(TABLE_DA_SAA, FIELD_DS_SAA) },
                                                 new String[]{ id.getValue(1) + "*" },
                                                 false,
                                                 null, // sortFields in Kombination im FoundAttributesCallback sind wirkungslos
                                                 false, true,
                                                 new EtkDataObjectList.FoundAttributesCallback() {
                                                     @Override
                                                     public boolean foundAttributes(DBDataObjectAttributes attributes) {
                                                         String saaNumber = attributes.getField(FIELD_DS_SAA).getAsString();
                                                         iPartsDataSaa saaDescription = saaDescriptionMap.get(saaNumber);
                                                         // Die original Fußnotennummer steht am Ende unserer ELDAS ID -> split und nehme den letzten Wert
                                                         String[] splittedFootnoteId = StrUtils.toStringArray(attributes.getField(FIELD_DFNS_FNID).getAsString(),
                                                                                                              iPartsDataFootNote.FOOTNOTE_ID_DELIMITER, false, false);
                                                         String originalFootNoteId = "";
                                                         if (splittedFootnoteId.length != 0) {
                                                             originalFootNoteId = splittedFootnoteId[splittedFootnoteId.length - 1];
                                                         }

                                                         // Fußnotentext mit Rückfallsprache inkl. Ersetzung von #-Texten bestimmen
                                                         iPartsDataFootNoteContent footnoteContent = new iPartsDataFootNoteContent(getProject(), new iPartsFootNoteContentId(attributes.getField(FIELD_DFNS_FNID).getAsString(), "00001"));
                                                         String footNoteText = footnoteContent.getText(dbLanguage, getProject().getDataBaseFallbackLanguages());

                                                         if (saaDescription == null) {
                                                             // Wenn das Dataobject noch nicht im lokalen Cache liegt, dann muss es erst erzeugt werden
                                                             saaDescription = new iPartsDataSaa(getProject(), new iPartsSaaId(saaNumber));

                                                             // Fußnotentext setzen
                                                             DBDataObjectAttribute footnoteTextForGrid = new DBDataObjectAttribute(SA_MASTERDATA_FN_ADDITIONAL_FIELD,
                                                                                                                                   DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
                                                             EtkMultiSprache footNoteMultiLang = new EtkMultiSprache();
                                                             footNoteMultiLang.setText(dbLanguage, footNoteText);
                                                             footnoteTextForGrid.setValueAsMultiLanguage(footNoteMultiLang, DBActionOrigin.FROM_DB);
                                                             attributes.addField(footnoteTextForGrid, DBActionOrigin.FROM_DB);

                                                             // Fußnoten Nr. setzen
                                                             attributes.addField(SA_MASTERDATA_FN_ID_ADDITIONAL_FIELD, originalFootNoteId, true, DBActionOrigin.FROM_DB);
                                                             saaDescription.setAttributes(attributes, DBActionOrigin.FROM_DB);
                                                             saaDescriptionMap.put(saaNumber, saaDescription);
                                                         } else {
                                                             // mehrere Fußnotentexte in einem Feld -> untereinander darstellen
                                                             footNoteText = saaDescription.getFieldValue(SA_MASTERDATA_FN_ADDITIONAL_FIELD, dbLanguage, false)
                                                                            + "\n" + footNoteText;
                                                             EtkMultiSprache footNoteMultiLang = new EtkMultiSprache();
                                                             footNoteMultiLang.setText(dbLanguage, footNoteText);
                                                             saaDescription.setFieldValueAsMultiLanguage(SA_MASTERDATA_FN_ADDITIONAL_FIELD,
                                                                                                         footNoteMultiLang,
                                                                                                         DBActionOrigin.FROM_DB);

                                                             // die dazugehörigen Fußnotenummern (original und nicht im ELDAS Format) auch via Zeilenumbruch trennen
                                                             saaDescription.setFieldValue(SA_MASTERDATA_FN_ID_ADDITIONAL_FIELD,
                                                                                          saaDescription.getFieldValue(SA_MASTERDATA_FN_ID_ADDITIONAL_FIELD) + "\n" + originalFootNoteId,
                                                                                          DBActionOrigin.FROM_DB);
                                                         }
                                                         // Das Dataobject nicht unnötig in die Liste eintragen
                                                         return false;
                                                     }
                                                 },
                                                 // Left outer Joins über 3 Tabellen DA_SAA -> DA_FN_SAA_REF -> DA_FN_CONTENT
                                                 new EtkDataObjectList.JoinData(TABLE_DA_FN_SAA_REF,
                                                                                new String[]{ FIELD_DS_SAA },
                                                                                new String[]{ FIELD_DFNS_SAA },
                                                                                true,
                                                                                false),
                                                 new EtkDataObjectList.JoinData(TABLE_DA_FN_CONTENT,
                                                                                new String[]{ TableAndFieldName.make(TABLE_DA_FN_SAA_REF, FIELD_DFNS_FNID) },
                                                                                new String[]{ FIELD_DFNC_FNID },
                                                                                true,
                                                                                false)
        );
        // Alle DataObjects aus dem lokalen Cache in die DataObjectList eintragen -> Wird benötigt für das Grid
        if (!saaDescriptionMap.isEmpty()) {
            dataObjectList.addAll(saaDescriptionMap.values(), DBActionOrigin.FROM_DB);
        }
        return dataObjectList;
    }

    /**
     * Abhängig von der übergebenen ID werden die {@link EtkDisplayFields} geladen und zurückgegeben
     * (Standardmechanismus über ConfigKey und DefaultDisplayFields)
     *
     * @param id
     * @return
     */
    protected EtkDisplayFields getDiplayFieldsForGrid(IdWithType id) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        if (id.getType().equals(iPartsSaId.TYPE)) {
            displayFields = getDisplayFields(IPARTS_CONFIG_KEY_SAA_TO_SA_MASTERDATA);
        }
        addAdditionalDisplayFields(displayFields, id);
        return displayFields;
    }

    /**
     * Fügt den übergebenen {@link EtkDisplayFields} zusätzliche Spalten hinzu (abhängig von der übergebenen ID).
     *
     * @param displayFields
     * @param id
     */
    private void addAdditionalDisplayFields(EtkDisplayFields displayFields, IdWithType id) {
        if (displayFields == null) {
            return;
        }
        if (id.getType().equals(iPartsSaId.TYPE)) {
            List<String> languages = getConfig().getViewerLanguages();
            // Spalte für die Fussnotennummern
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_SAA, SA_MASTERDATA_FN_ID_ADDITIONAL_FIELD, false, false);
            displayField.setDefaultText(false);
            displayField.setText(new EtkMultiSprache("!!Fußnoten Nr.", new String[]{ TranslationHandler.getUiLanguage() }));
            displayFields.addFeld(displayField);
            // Spalte für die Fussnotentexte
            displayField = new EtkDisplayField(TABLE_DA_SAA, SA_MASTERDATA_FN_ADDITIONAL_FIELD, true, false);
            displayField.setDefaultText(false);
            displayField.setText(new EtkMultiSprache("!!Fußnotentext", new String[]{ TranslationHandler.getUiLanguage() }));
            displayFields.addFeld(displayField);
        }
    }

    /**
     * Füllt das Grid mit den {@link EtkDataObject}s der übergebenen {@link EtkDataObjectList}
     *
     * @param dataGrid
     * @param dataObjectList
     */
    protected void fillGrid(DataObjectGrid dataGrid, DBDataObjectList<? extends EtkDataObject> dataObjectList) {
        dataGrid.clearGrid();
        for (EtkDataObject dataObject : dataObjectList) {
            // Bei SAA-Stammdaten im Dialog "Freie SAs" die Anzeige nach den Eigenschaften des Benutzers filtern
            if ((externalSAId != null) && !externalSAId.isEmpty()) {
                String source = dataObject.getFieldValue(iPartsConst.FIELD_DS_SOURCE);
                if (iPartsImportDataOrigin.isSourceVisible(source, isCarAndVanInSession(), isTruckAndBusInSession())) {
                    dataGrid.addObjectToGrid(dataObject);
                }
            } else {
                dataGrid.addObjectToGrid(dataObject);
            }
        }
        dataGrid.showNoResultsLabel(dataObjectList.isEmpty());
    }

    /**
     * Liefert die Select-Felder für die SAA Daten
     *
     * @return
     */
    public EtkDisplayFields getSelectFieldsForSAAToSAMasterData() {
        EtkDisplayFields selectFields = getProject().getAllDisplayFieldsForTable(TABLE_DA_SAA);
        selectFields.addFelder(getProject().getAllDisplayFieldsForTable(TABLE_DA_FN_SAA_REF));
        selectFields.addFelder(getProject().getAllDisplayFieldsForTable(TABLE_DA_FN_CONTENT));
        return selectFields;
    }

    /**
     * DataObjectList für das jeweilige Grid bestimmen
     *
     * @param top
     * @return
     */
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList(boolean top) {
        return new DBDataObjectList<EtkDataObject>();
    }

    @Override
    protected final List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        if (configKey.equals(IPARTS_CONFIG_KEY_SAA_TO_SA_MASTERDATA)) {
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_SAA, false, false);
            displayResultFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_DESC, true, false);
            displayResultFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_CONNECTED_SAS, false, false);
            displayResultFields.addFeld(displayField);
        } else if (configKey.equals(IPARTS_CONFIG_KEY_SA_MASTERDATA)) {
            displayResultFields = getProject().getAllDisplayFieldsForTable(TABLE_DA_SA);
        } else if (configKey.equals(IPARTS_CONFIG_KEY_CONSTRUCTION_EDS_SAA_MASTERDATA)) {
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_DESC, true, false);
            displayResultFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_DESC_EXTENDED, true, false);
            displayResultFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_REMARK, true, false);
            displayResultFields.addFeld(displayField);
        }
        if (displayResultFields.size() != 0) {
            displayResultFields.loadStandards(getConfig());
        }
        return displayResultFields.getFields();
    }

    private void changeWorkText(boolean isEditAllowed) {
        String toolTip = EditToolbarButtonAlias.EDIT_WORK.getTooltip();
        if (!isEditAllowed) {
            toolTip = "!!Anzeigen";
        }
        if (workHolder != null && !workHolder.menuItem.getText().equals(toolTip)) {
            workHolder.toolbarButton.setTooltip(toolTip);
            workHolder.menuItem.setText(toolTip);
        }
    }

    private boolean isSingleSelection() {
        int selectionRowCount = grid.getTable().getSelectedRows().size();
        return selectionRowCount == 1;
    }

    /**
     * (De-)Aktiviert die einzelnen Buttons
     */
    private void doEnableButtons() {
        if (isGridVisible()) {
            boolean singleSelectionEnabled = isSingleSelection();

            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, singleSelectionEnabled);
            if (!isEditAllowed) {
                changeWorkText(false);
            }
        }
    }

    private EtkEditFields getEditFieldsForSaaMasterData() {
        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        // ggf Laden einer WB-Konfig
        if (editFields.size() == 0) {
            String tableName = TABLE_DA_SAA;
            EtkEditFieldHelper.getEditFields(getProject(), tableName, editFields, true);
            EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(tableName);

            List<String> editableFields = new DwList<>(FIELDS_FOR_MODIFY);
            List<String> pkFields = tableDef.getPrimaryKeyFields();
            for (EtkEditField eField : editFields.getFields()) {
                if (pkFields.contains(eField.getKey().getFieldName())) {
                    eField.setMussFeld(true);
                    eField.setEditierbar(false);  // ist PK-Value => nicht editierbar
                } else {
                    eField.setEditierbar(editableFields.contains(eField.getKey().getFieldName()));
                }
            }
        }
        return editFields;
    }

    private void doEditOrShowSaaMasterData(Event event) {
        List<IdWithType> idList = grid.getSelectedObjectIds(TABLE_DA_SAA);
        if (!idList.isEmpty() && (idList.size() == 1)) {
            EtkEditFields editFields = getEditFieldsForSaaMasterData();
            IdWithType id = idList.get(0);
            iPartsSaaId saaId = new iPartsSaaId(id.getValue(1));
            EtkProject project = getProject();
            iPartsDataSaa dataSaa = new iPartsDataSaa(project, saaId);
            if (!dataSaa.existsInDB()) {
                // Fehlermeldung?
                return;
            }
            EditUserControlForSAorSAA eCtrl = new EditUserControlForSAorSAA(getConnector(), this, TABLE_DA_SAA,
                                                                            id, dataSaa.getAttributes(), editFields,
                                                                            EnumSet.of(DictTextKindTypes.SAA_NAME));
            eCtrl.setReadOnly(!isEditAllowed);
            String key = "!!Stammdaten für SAA \"%1\" bearbeiten";
            if (!isEditAllowed) {
                key = "!!Stammdaten für SAA \"%1\" anzeigen";
            }
            eCtrl.setTitle(TranslationHandler.translate(key, iPartsNumberHelper.formatPartNo(project, saaId.getSaaNumber())));
            eCtrl.handleSpecialMultiLangFields(FIELD_DS_DESC);

            if (eCtrl.showModal() == ModalResult.OK) {
                // speichern
                dataSaa.assignAttributes(project, eCtrl.getAttributes(), false, DBActionOrigin.FROM_EDIT);
                project.getDbLayer().startTransaction();
                try {
                    // Speichern des technischen ChangeSets und des DataObjects bei Erfolg
                    boolean result = iPartsRevisionChangeSet.saveDataObjectWithChangeSet(project, dataSaa, iPartsChangeSetSource.SAA);
                    if (result) {
                        dataSaa.saveToDB();
                        project.getDbLayer().commit();
                        Set<iPartsSaaId> modifiedSaaIds = new HashSet<>();
                        modifiedSaaIds.add(saaId);
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.SAA,
                                                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                  modifiedSaaIds,
                                                                                                                  false));
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));

                        grid.updateGridDirect(dataSaa);
                    } else {
                        project.getDbLayer().rollback();
                    }
                } catch (Exception e) {
                    project.getDbLayer().rollback();
                    Logger.getLogger().handleRuntimeException(e);
                }
            }
        }
    }

    private class SaaDataObjectFilterGrid extends DataObjectFilterGrid {

        public SaaDataObjectFilterGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            doEnableButtons();
        }

        @Override
        protected void onCellDblClicked(int row, int col, Event event) {
            doEditOrShowSaaMasterData(event);
        }

        @Override
        protected void createToolbarButtons(GuiToolbar toolbar) {
            workHolder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doEditOrShowSaaMasterData(event);
                }
            });
            getContextMenu().addChild(workHolder.menuItem);
        }

        public void updateGridDirect(EtkDataObject currentDataObject) {
            String tableName = currentDataObject.getTableName();
            List<Integer> columnIndices = new DwList<>();
            GuiTableRowWithObjects row = null;
            GuiTableRow tableRow = getTable().getSelectedRow();
            if (tableRow instanceof GuiTableRowWithObjects) {
                row = (GuiTableRowWithObjects)tableRow;
                int column = 0;
                for (EtkDisplayField displayField : grid.getDisplayFields().getVisibleFields()) {
                    if (displayField.getKey().getTableName().equals(tableName)) {
                        String fieldName = displayField.getKey().getFieldName();
                        DBDataObjectAttribute attrib = currentDataObject.getAttribute(fieldName, false);
                        if ((attrib != null) && attrib.isModified()) {
                            String value = getVisualValueOfField(tableName, fieldName, currentDataObject);
                            GuiLabel label = new GuiLabel(value);
                            row.replaceChild(column, label);
                            columnIndices.add(column);
                        }
                    }
                    column++;
                }
            }
            if (!columnIndices.isEmpty() && (row != null)) {
                EtkDataObject dataObject = row.getObjectForTable(tableName);
                if ((dataObject != null) && dataObject.getAsId().equals(currentDataObject.getAsId())) {
                    // neue Daten ins DataObjectGrid schreiben
                    dataObject.assignAttributes(getProject(), currentDataObject.getAttributes(), false, DBActionOrigin.FROM_DB);
                }
                int[] selectedRowNos = getTable().getSelectedRowIndices();
                getTable().updateFilterAndSortForModifiedColumns(columnIndices);
                if (getTable().getRowCount() > 0) {
                    getTable().setSelectedRows(selectedRowNos, false, true);
                }
            } else {
                // Rückfallpostion: Tabelle neu laden
                List<IdWithType> idList = grid.getSelectedObjectIds(tableName);
                Object storageSelected = grid.getFilterAndSortSettings();
                dataToGrid(externalSAId);
                grid.restoreFilterAndSortSettings(storageSelected);
                grid.setSelectedObjectIds(idList, tableName, true, false);
            }
            doEnableButtons();
        }
    }

    private class EditUserControlContainer {

        private boolean isMatrix;
        private EditUserControls editControls;
        private EditMatrixUserControls editMatrixControls;

        public EditUserControlContainer(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                        String tableName, IdWithType id, EtkDisplayFields displayFields,
                                        boolean useRelatedInfoStyle) {
            isMatrix = false; // Matrix-Darstellung nur für PartId
            if (id.getType().equals(PartId.TYPE)) {
                isMatrix = iPartsUserSettingsHelper.isMatrixEdit(dataConnector.getProject());
            }
            if (isMatrix) {
                editControls = null;
                // Falls keine DisplayFields vorhanden sind -> Fallback auf alten Ablauf (Holen aller Felder der Tabelle via DBDescription)
                if (displayFields.size() != 0) {
                    iPartsMatrixEditFields externalMatrixEditFields = new iPartsMatrixEditFields();
                    EtkEditFields editFields = new EtkEditFields();
                    EtkEditFieldHelper.convertDisplayFieldsToEditFields(displayFields, editFields);
                    // einfaches 3spaltiges Layout
                    EtkEditFieldHelper.convertEditFieldsToMatrixEditFields(editFields, externalMatrixEditFields, 3);
                    editMatrixControls = new EditMatrixUserControls(dataConnector, parentForm, tableName, id, null,
                                                                    externalMatrixEditFields, EditMatrixUserControls.MATRIX_LAYOUT.DEFAULT, "");
                } else {
                    editMatrixControls = new EditMatrixUserControls(dataConnector, parentForm, tableName, id);
                }
                editMatrixControls.setReadOnly(true);
            } else {
                editMatrixControls = null;
                // Falls keine DisplayFields vorhanden sind -> Fallback auf alten Ablauf (Holen aller Felder der Tabelle via DBDescription)
                if (displayFields.size() != 0) {
                    EtkEditFields editFields = new EtkEditFields();
                    EtkEditFieldHelper.convertDisplayFieldsToEditFields(displayFields, editFields);
                    editControls = new EditUserControls(dataConnector, parentForm, tableName, id, editFields, useRelatedInfoStyle);
                } else {
                    editControls = new EditUserControls(dataConnector, parentForm, tableName, id);
                }
            }
        }

        public AbstractGuiControl getGui() {
            if (isMatrix) {
                return editMatrixControls.getGui();
            } else {
                return editControls.getGui();
            }
        }

        public boolean idIsChanged(IdWithType testId) {
            if (isMatrix) {
                return editMatrixControls.idIsChanged(testId);
            } else {
                return editControls.idIsChanged(testId);
            }
        }
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
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDataObjectGridTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelDataObjectGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

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
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            mainPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanel.setName("mainPanel");
            mainPanel.__internal_setGenerationDpi(96);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutGridBag mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mainPanel.setLayout(mainPanelLayout);
            splitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitPane.setName("splitPane");
            splitPane.__internal_setGenerationDpi(96);
            splitPane.registerTranslationHandler(translationHandler);
            splitPane.setScaleForResolution(true);
            splitPane.setMinimumWidth(10);
            splitPane.setMinimumHeight(10);
            splitPane.setPaddingBottom(1);
            splitPane.setHorizontal(false);
            splitPane.setDividerPosition(353);
            splitPane.setResizeWeight(0.5);
            panelTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTop.setName("panelTop");
            panelTop.__internal_setGenerationDpi(96);
            panelTop.registerTranslationHandler(translationHandler);
            panelTop.setScaleForResolution(true);
            panelTop.setMinimumHeight(0);
            panelTop.setPaddingTop(4);
            panelTop.setPaddingLeft(8);
            panelTop.setPaddingRight(8);
            panelTop.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelTop.setLayout(panelTopLayout);
            splitPane.addChild(panelTop);
            panelBottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelBottom.setName("panelBottom");
            panelBottom.__internal_setGenerationDpi(96);
            panelBottom.registerTranslationHandler(translationHandler);
            panelBottom.setScaleForResolution(true);
            panelBottom.setMinimumHeight(0);
            panelBottom.setPaddingTop(4);
            panelBottom.setPaddingLeft(8);
            panelBottom.setPaddingRight(8);
            panelBottom.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelBottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelBottom.setLayout(panelBottomLayout);
            labelDataObjectGridTitle = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDataObjectGridTitle.setName("labelDataObjectGridTitle");
            labelDataObjectGridTitle.__internal_setGenerationDpi(96);
            labelDataObjectGridTitle.registerTranslationHandler(translationHandler);
            labelDataObjectGridTitle.setScaleForResolution(true);
            labelDataObjectGridTitle.setMinimumWidth(10);
            labelDataObjectGridTitle.setMinimumHeight(10);
            labelDataObjectGridTitle.setPaddingBottom(4);
            labelDataObjectGridTitle.setText("DataObjectGridBottomTitle");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelDataObjectGridTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "w", "n", 4, 8, 4, 8);
            labelDataObjectGridTitle.setConstraints(labelDataObjectGridTitleConstraints);
            panelBottom.addChild(labelDataObjectGridTitle);
            panelDataObjectGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelDataObjectGrid.setName("panelDataObjectGrid");
            panelDataObjectGrid.__internal_setGenerationDpi(96);
            panelDataObjectGrid.registerTranslationHandler(translationHandler);
            panelDataObjectGrid.setScaleForResolution(true);
            panelDataObjectGrid.setMinimumWidth(10);
            panelDataObjectGrid.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelDataObjectGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelDataObjectGrid.setLayout(panelDataObjectGridLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelDataObjectGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 100.0, "c", "b", 4, 8, 8, 8);
            panelDataObjectGrid.setConstraints(panelDataObjectGridConstraints);
            panelBottom.addChild(panelDataObjectGrid);
            splitPane.addChild(panelBottom);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag splitPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            splitPane.setConstraints(splitPaneConstraints);
            mainPanel.addChild(splitPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            this.addChild(mainPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
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