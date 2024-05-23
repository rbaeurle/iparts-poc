/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.InplaceEdit.GuiTableInplaceEditorManager;
import de.docware.apps.etk.base.forms.common.InplaceEdit.InplaceEditor;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.relatedinfo.main.forms.AbstractRelatedInfoMainForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDIALOGChangeHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsColorTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor.iPartsGuiCodeTextFieldForColorsInplaceEditor;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.controls.table.GuiTableInPlaceEditorFactoryWithValidationInterface;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.TableSelectionMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Formular für die Farbtabellen (Variantentabellen) zu einer Sachnummer innerhalb der RelatedInfo.
 */
public class iPartsRelatedInfoVariantsToPartDataForm extends AbstractTwoDataObjectGridsEditForm {

    public static final String IPARTS_MENU_ITEM_SHOW_VARIANTS_TO_PART_DATA = "iPartsMenuItemShowVariantsToPartData";
    public static final String CONFIG_KEY_VARIANTS_TO_PART_DATA = "Plugin/iPartsEdit/VariantsToPart";
    public static final String CONFIG_KEY_COLOR_VARIANTS_DATA = "Plugin/iPartsEdit/ColorVariants";
    public static final String[] SPECIAL_TABLEFILTER_FIELDS = new String[]{ iPartsConst.FIELD_DCTC_EVENT_FROM, iPartsConst.FIELD_DCTC_EVENT_TO,
                                                                            iPartsConst.FIELD_DCTC_EVENT_FROM_AS, iPartsConst.FIELD_DCTC_EVENT_TO_AS };
    public static final String EDIT_MULTIPLE_ENTRIES_TEXT = "!!Varianten vereinheitlichen";
    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.SA_TU,
                                                                                   iPartsModuleTypes.WorkshopMaterial,
                                                                                   iPartsModuleTypes.DialogRetail,
                                                                                   iPartsModuleTypes.EDSRetail,
                                                                                   iPartsModuleTypes.PSK_PKW,
                                                                                   iPartsModuleTypes.PSK_TRUCK,
                                                                                   iPartsModuleTypes.Dialog_SM_Construction,
                                                                                   iPartsModuleTypes.EDS_SAA_Construction,
                                                                                   iPartsModuleTypes.CTT_SAA_Construction);


    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_VARIANTS_TO_PART_DATA, "!!Variantenzuordnung zu Teil anzeigen",
                                EditDefaultImages.edit_colorVariants.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_VARIANTS_TO_PART_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste gültig für den gewünschten Modultyp?
        boolean menuItemVisible = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            menuItemVisible = relatedInfoIsVisible(connector.getSelectedPartListEntries().get(0), connector);
        }

        // Im Edit nur Menüeinträge zur Bearbeitung anzeigen
        if (menuItemVisible && isEditContext(connector, true)) {
            menuItemVisible = false;
        }

        // Separator und Menüeintrag aktualisieren
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_VARIANTS_TO_PART_DATA, menuItemVisible);
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry partListEntry, AbstractJavaViewerFormIConnector connector) {
        EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
        if (!relatedInfoIsVisible(ownerAssembly, VALID_MODULE_TYPES)) {
            return false;
        }

        iPartsModuleTypes moduleType = iPartsModuleTypes.getType(ownerAssembly.getEbeneName());
        boolean retailMode = !moduleType.isConstructionRelevant() && !isEditContext(connector, false);

        // Retail-Anzeige -> Farbvarianten nur dann anzeigen, wenn auch Farbvarianten mit aktivem Retail-Filter existieren
        // Konstruktions-Stücklisten -> Farbvarianten nur dann anzeigen, wenn auch Farbvarianten existieren
        boolean isVirtualPartList = iPartsVirtualNode.isVirtualId(ownerAssembly.getAsId());
        if (retailMode) {
            return relatedInfoIsVisibleForRetail(partListEntry);
        } else if (isVirtualPartList && moduleType.isConstructionRelevant()) {
            return relatedInfoIsVisibleForConstruction(partListEntry);
        } else {
            // Edit -> Farbvarianten immer anzeigen
            return true;
        }
    }

    /**
     * Überprüft, ob Farbvarianten mit aktivem Retail-Filter für den übergebenen Stücklisteneintrag vorhanden sind und
     * demzufolge die Related Info sichtbar sein bzw. ein Icon in der Stückliste angezeigt werden soll. Macht in der Konstruktionssicht
     * und im Edit keinen Sinn.
     *
     * @param entry
     * @return
     */
    public static boolean relatedInfoIsVisibleForRetail(EtkDataPartListEntry entry) {
        if (entry instanceof iPartsDataPartListEntry) {
            return ((iPartsDataPartListEntry)entry).getColorTableForRetail() != null;
        }
        return false;
    }

    public static boolean relatedInfoIsVisibleForEdit(EtkDataPartListEntry entry) {
        if (entry instanceof iPartsDataPartListEntry) {
            return ((iPartsDataPartListEntry)entry).getColorTableForRetailWithoutFilter() != null;
        }
        return false;
    }

    /**
     * Überprüft, ob Farbvarianten für den übergebenen Konstruktions-Stücklisteneintrag vorhanden sind und
     * demzufolge die Related Info sichtbar sein bzw. ein Icon in der Stückliste angezeigt werden soll.
     *
     * @param entry
     * @return
     */
    private static boolean relatedInfoIsVisibleForConstruction(EtkDataPartListEntry entry) {
        if (entry instanceof iPartsDataPartListEntry) {
            return ((iPartsDataPartListEntry)entry).getColorTableForConstruction() != null;
        }
        return false;
    }

    /**
     * Related Info Icon wird angezeigt falls Farbvarianten für die AS-Sicht oder die Konstruktionsstückliste existieren
     *
     * @param entry
     * @param isConstruction
     * @return
     */
    public static AssemblyListCellContentFromPlugin getRelatedInfoIcon(EtkDataPartListEntry entry, boolean isConstruction, boolean isEditMode) {
        if (!isConstruction && relatedInfoIsVisibleForRetail(entry) || (isConstruction && relatedInfoIsVisibleForConstruction(entry))
            || (isEditMode && relatedInfoIsVisibleForEdit(entry))) {
            AssemblyListCellContentFromPlugin iconInfo = new AssemblyListCellContentFromPlugin(iPartsConst.CONFIG_KEY_RELATED_INFO_VARIANTS_TO_PART_DATA,
                                                                                               EditDefaultImages.edit_colorVariants.getImage());
            iconInfo.setHint(iPartsConst.RELATED_INFO_VARIANTS_TO_PART_DATA_TEXT);
            iconInfo.setCursor(DWCursor.Hand);
            iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
            return iconInfo;
        } else {
            if (entry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)(entry);
                if (partListEntry.hasColorTablesUnfiltered()) {
                    AssemblyListCellContentFromPlugin iconInfo;
                    if (isEditMode) {
                        iconInfo = new AssemblyListCellContentFromPlugin(iPartsConst.CONFIG_KEY_RELATED_INFO_VARIANTS_TO_PART_DATA,
                                                                         EditDefaultImages.edit_colorVariants_invalid.getImage());
                        iconInfo.setCursor(DWCursor.Hand);
                    } else {
                        iconInfo = new AssemblyListCellContentFromPlugin(null,
                                                                         EditDefaultImages.edit_colorVariants_invalid.getImage());
                        iconInfo.setCursor(DWCursor.Unspecific);
                    }
                    iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
                    iconInfo.setHint(iPartsConst.RELATED_INFO_VARIANTS_RETAILFILTER_INVALID_TEXT);
                    return iconInfo;
                }
            }
        }
        return null;
    }

    private AbstractRelatedInfoMainForm relatedInfoMainForm;
    private boolean isRetailPartList;
    private iPartsProductId filterProductId;
    private iPartsSeriesId filterSeriesId;
    private EtkDataPartListEntry currentPartListEntry;

    private GuiMenuItem codeMasterDataASMenuItem;
    private GuiMenuItem codeMasterDataMenuItem;
    protected GuiMenuItem factoryDataMenuItem;
    private GuiMenuItem variantFactoryDataMenuItem;
    protected GuiMenuItem partsToVariantTableMenuItem;
    private iPartsDataDIALOGChangeList factoryDataDialogChangesForPartNo; // Änderungsobjekte, die für die aktuelle Teilenummer geladen wurden

    protected iPartsRelatedInfoVariantsToPartDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                      AbstractRelatedInfoMainForm relatedInfoMainForm,
                                                      IEtkRelatedInfo relatedInfo, String configKeyTop, String dataObjectGridTopTitle,
                                                      String configKeyBottom, String dataObjectGridBottomTitle, boolean enableEditMode) {
        super(dataConnector, parentForm, relatedInfo, configKeyTop, dataObjectGridTopTitle,
              configKeyBottom, dataObjectGridBottomTitle, enableEditMode);
        this.relatedInfoMainForm = relatedInfoMainForm;
        currentPartListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        iPartsModuleTypes moduleType = iPartsModuleTypes.getType(currentPartListEntry.getOwnerAssembly().getEbeneName());
        isRetailPartList = !moduleType.isConstructionRelevant();
        setCheckboxShowHistoryVisible(!isRetailPartList && !editMode);
        setCheckboxRetailFilterVisible(editMode);
        scaleFromParentForm(getWindow());
    }

    protected iPartsRelatedInfoVariantsToPartDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                      AbstractRelatedInfoMainForm relatedInfoMainForm, IEtkRelatedInfo relatedInfo) {
        this(dataConnector, parentForm, relatedInfoMainForm, relatedInfo,
             CONFIG_KEY_VARIANTS_TO_PART_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
             "!!Variantentabellen:",
             CONFIG_KEY_COLOR_VARIANTS_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
             "!!Varianten zu Variantentabelle:",
             true);
    }

    protected List<EtkDisplayField> createVariantTablesDataDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_DATA, iPartsConst.FIELD_DCTD_TABLE_ID, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_DATA, iPartsConst.FIELD_DCTD_DESC, true, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_DATA, iPartsConst.FIELD_DCTD_FIKZ, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_PART, iPartsConst.FIELD_DCTP_SDATA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_PART, iPartsConst.FIELD_DCTP_SDATB, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_PART, iPartsConst.FIELD_DCTP_ETKZ, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_PART, iPartsConst.FIELD_DCTP_SOURCE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_PART, iPartsConst.FIELD_DCTP_STATUS, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    private List<EtkDisplayField> createVariantsDataDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();

        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_ETKZ, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_POS, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_COLOR_VAR, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLOR_NUMBER, iPartsConst.FIELD_DCN_DESC, true, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_PGRP, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_CODE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_CODE_AS, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_SDATA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_SDATB, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_SOURCE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_STATUS, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    @Override
    protected void postCreateGui() {

        super.postCreateGui();

        final DataObjectGrid variantsGrid = getGrid(false);
        if (editMode) {
            if (!isReadOnly()) {
                EtkDisplayFields displayFields = variantsGrid.getDisplayFields();
                if (displayFields == null) {
                    displayFields = getDisplayFields(CONFIG_KEY_COLOR_VARIANTS_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
                }
                if (displayFields != null) {
                    HashMap<String, String> inplaceEditorMap = new HashMap<>();
                    String codeASTableAndField = TableAndFieldName.make(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_CODE_AS);
                    if (editMode && !isReadOnly()) {
                        inplaceEditorMap.put(codeASTableAndField, iPartsGuiCodeTextFieldForColorsInplaceEditor.class.getName());
                    }

                    final iPartsRelatedInfoVariantsToPartDataForm currentFormInstance = this;
                    variantsGrid.getTable().setInPlaceEditorFactory(new GuiTableInplaceEditorManager(variantsGrid.getTable(), inplaceEditorMap, displayFields.getFields()) {

                        @Override
                        public void initEditor(InplaceEditor editor) {
                            if (editor instanceof iPartsGuiCodeTextFieldForColorsInplaceEditor) {
                                iPartsGuiCodeTextFieldForColorsInplaceEditor codeEditor = (iPartsGuiCodeTextFieldForColorsInplaceEditor)editor;
                                codeEditor.init(getSeriesNoOfPartlistEntry(), getDocumentationTypeOfOwnerAssembly(), getConnector());
                            }
                        }

                        @Override
                        public AbstractGuiControl createInPlaceEditor(int column, int row, Object cellContent, int width) {
                            if (isVariantsEditEnabled()) {
                                return super.createInPlaceEditor(column, row, cellContent, width);
                            } else {
                                return null;
                            }
                        }

                        @Override
                        public void handleSaveEditResult(int column, int row, InplaceEditor editor) {
                            EtkDisplayFieldKeyNormal fieldKey = editor.getDisplayField().getKey();
                            DataObjectGrid.GuiTableRowWithObjects guiTableRow = (DataObjectGrid.GuiTableRowWithObjects)variantsGrid.getTable().getRow(row);
                            EtkDataObject dataObjectForInPlaceEditor = guiTableRow.getObjectForTable(fieldKey.getTableName());
                            if (dataObjectForInPlaceEditor != null) {
                                // Leerzeichen entfernen und ";" anhängen falls nicht vorhanden
                                String editedCode = editor.getEditResult(dataObjectForInPlaceEditor);

                                dataObjectForInPlaceEditor.setFieldValue(fieldKey.getFieldName(), editedCode, DBActionOrigin.FROM_EDIT);
                                addDataObjectToActiveChangeSetForEdit(dataObjectForInPlaceEditor);
                                reloadEditableDataAndUpdateEditContext();

                                GuiLabel editedLabel = new GuiLabel(editedCode);
                                variantsGrid.getTable().getRow(row).replaceChild(column, editedLabel);

                                // Ansicht aktualisieren und als modifiziert markieren
                                setModifiedByEdit(true);
                                getConnector().updateAllViews(currentFormInstance, false);
                            }
                        }
                    });
                }
            }
        }
        getCheckboxShowHistory().switchOffEventListeners();
        setShowHistory(true);
        getCheckboxShowHistory().switchOnEventListeners();
    }

    @Override
    protected DataObjectGrid createGrid(final boolean top) {
        if (top) {
            // Variantentabellen
            DataObjectFilterGridWithStatus variantTablesGrid = new DataObjectFilterGridWithStatus(getConnector(), this,
                                                                                                  iPartsConst.TABLE_DA_COLORTABLE_PART,
                                                                                                  iPartsConst.FIELD_DCTP_STATUS) {
                @Override
                protected void createContextMenuItems(GuiContextMenu contextMenu) {
                    factoryDataMenuItem = getToolbarHelper().createMenuEntry("variantTableFactoryData", "!!Werkseinsatzdaten", null, new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            List<EtkDataObject> selection = getSelection();
                            if (selection != null) {
                                iPartsDataColorTableToPart colorTableToPart = (iPartsDataColorTableToPart)selection.get(1); // EtkDataObject an Index 1 ist iPartsDataColorTableToPart
                                boolean enableEdit = editMode && (colorTableToPart.getDataOrigin() != iPartsImportDataOrigin.PRIMUS)
                                                     && isVariantsEditEnabled();
                                iPartsVariantsToPartFactoryDataForm factoryDataForm = new iPartsVariantsToPartFactoryDataForm(getConnector(),
                                                                                                                              iPartsRelatedInfoVariantsToPartDataForm.this,
                                                                                                                              currentPartListEntry,
                                                                                                                              colorTableToPart.getAsId(),
                                                                                                                              filterSeriesId,
                                                                                                                              enableEdit,
                                                                                                                              isRetailFilterForFactoryData());
                                factoryDataForm.showModal();
                            }
                        }
                    }, getUITranslationHandler());
                    contextMenu.addChild(factoryDataMenuItem);

                    partsToVariantTableMenuItem = getToolbarHelper().createMenuEntry("variantTablePartsToVariantTable", "!!Teile zu Variantentabelle", null, new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            List<EtkDataObject> selection = getSelection();
                            if (selection != null) {
                                iPartsDataColorTableToPart colorTableToPart = (iPartsDataColorTableToPart)selection.get(1); // EtkDataObject an Index 1 ist iPartsDataColorTableToPart
                                iPartsRelatedInfoPartsToVariantTableDataForm partsToVariantTableDataForm =
                                        new iPartsRelatedInfoPartsToVariantTableDataForm((RelatedInfoBaseFormIConnector)getConnector(),
                                                                                         iPartsRelatedInfoVariantsToPartDataForm.this,
                                                                                         getRelatedInfoMainForm(), getRelatedInfo(), colorTableToPart,
                                                                                         isRetailFilter(), isShowHistory());
                                partsToVariantTableDataForm.updateData(iPartsRelatedInfoVariantsToPartDataForm.this, true);
                                partsToVariantTableDataForm.showModal();
                            }
                        }
                    }, getUITranslationHandler());
                    contextMenu.addChild(partsToVariantTableMenuItem);
                }

                @Override
                protected void statusChanged() {
                    handleStatusChanged(this);
                }

                @Override
                protected iPartsDataReleaseState getStatusFromSelection(List<List<EtkDataObject>> selection) {
                    iPartsDataReleaseState releaseState = super.getStatusFromSelection(selection);
                    if ((releaseState != null) && isVariantsEditEnabled()) {
                        return releaseState;
                    }
                    return null;
                }
            };
            variantTablesGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
            variantTablesGrid.getTable().setSelectionMode(TableSelectionMode.SELECTION_MODE_EXACT_SINGLE_SELECTION);
            return variantTablesGrid;
        } else {
            // Varianten
            DataObjectFilterGridWithStatus variantsGrid = new DataObjectFilterGridWithStatus(getConnector(), this, iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_STATUS) {

                @Override
                protected void createContextMenuItems(GuiContextMenu contextMenu) {
                    variantFactoryDataMenuItem = getToolbarHelper().createMenuEntry("variantsFactoryData", "!!Werkseinsatzdaten", null, new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            List<EtkDataObject> selection = getSelection();
                            if (selection != null) {
                                iPartsDataColorTableContent colorTableContent = (iPartsDataColorTableContent)selection.get(1); // EtkDataObject an Index 1 ist iPartsDataColorTableToPart
                                boolean enableEdit = editMode && (colorTableContent.getDataOrigin() != iPartsImportDataOrigin.PRIMUS)
                                                     && isVariantsEditEnabled();
                                int selectedIndex = getTable().getSelectedRowIndex();
                                iPartsVariantFactoryDataForm factoryDataForm = new iPartsVariantFactoryDataForm(getConnector(),
                                                                                                                iPartsRelatedInfoVariantsToPartDataForm.this,
                                                                                                                currentPartListEntry,
                                                                                                                selectedIndex, getAllContentIds(),
                                                                                                                filterSeriesId,
                                                                                                                enableEdit,
                                                                                                                isRetailFilterForFactoryData());
                                factoryDataForm.showModal();
                                // Die aktuelle Selektion setzen
                                int newSelection = factoryDataForm.getSelectedIndex();
                                if (newSelection != selectedIndex) {
                                    getTable().setSelectedRow(newSelection, true);
                                }
                            }
                        }

                        /**
                         * Bestimmt IDs alles {@link iPartsDataColorTableContent} Objekte im Grid
                         * @return
                         */
                        private Map<Integer, iPartsColorTableContentId> getAllContentIds() {
                            Map<Integer, iPartsColorTableContentId> ids = new HashMap<>();
                            for (int index = 0; index < getTable().getRowCount(); index++) {
                                EtkDataObject contentObject = getDataObjectForRowAndTable(index, iPartsConst.TABLE_DA_COLORTABLE_CONTENT);
                                if (contentObject instanceof iPartsDataColorTableContent) {
                                    ids.put(index, (iPartsColorTableContentId)contentObject.getAsId());
                                }
                            }
                            return ids;
                        }
                    }, getUITranslationHandler());
                    contextMenu.addChild(variantFactoryDataMenuItem);

                    codeMasterDataMenuItem = iPartsCodeMatrixDialog.createMenuItem(new iPartsCodeMatrixDialog.CodeMasterDataCallback() {
                        @Override
                        public iPartsCodeMatrixDialog.CodeMasterDataQuery getCodeMasterDataQuery() {
                            GuiTableRow selectedRow = getTable().getSelectedRow();
                            if (selectedRow instanceof GuiTableRowWithObjects) {
                                EtkDataObject colorTablePart = ((GuiTableRowWithObjects)selectedRow).getObjectForTable(iPartsConst.TABLE_DA_COLORTABLE_DATA);
                                EtkDataObject colorTablePartContent = ((GuiTableRowWithObjects)selectedRow).getObjectForTable(iPartsConst.TABLE_DA_COLORTABLE_CONTENT);
                                return new iPartsCodeMatrixDialog.CodeMasterDataQuery(colorTablePartContent.getFieldValue(iPartsConst.FIELD_DCTC_CODE),
                                                                                      ColorTableHelper.getSeriesFromColorTableOrPartListEntry(colorTablePart,
                                                                                                                                              getSeriesNoOfPartlistEntry()),
                                                                                      colorTablePartContent.getFieldValue(iPartsConst.FIELD_DCTC_PGRP),
                                                                                      colorTablePartContent.getFieldValue(iPartsConst.FIELD_DCTC_SDATA));
                            } else {
                                return null;
                            }
                        }
                    }, this);
                    contextMenu.addChild(codeMasterDataMenuItem);

                    codeMasterDataASMenuItem = iPartsCodeMatrixDialog.createMenuItem(new iPartsCodeMatrixDialog.CodeMasterDataCallback() {
                        @Override
                        public iPartsCodeMatrixDialog.CodeMasterDataQuery getCodeMasterDataQuery() {
                            GuiTableRow selectedRow = getTable().getSelectedRow();
                            if (selectedRow instanceof GuiTableRowWithObjects) {
                                EtkDataObject colorTablePart = ((GuiTableRowWithObjects)selectedRow).getObjectForTable(iPartsConst.TABLE_DA_COLORTABLE_DATA);
                                EtkDataObject colorTablePartContent = ((GuiTableRowWithObjects)selectedRow).getObjectForTable(iPartsConst.TABLE_DA_COLORTABLE_CONTENT);
                                return new iPartsCodeMatrixDialog.CodeMasterDataQuery(colorTablePartContent.getFieldValue(iPartsConst.FIELD_DCTC_CODE_AS),
                                                                                      ColorTableHelper.getSeriesFromColorTableOrPartListEntry(colorTablePart,
                                                                                                                                              getSeriesNoOfPartlistEntry()),
                                                                                      colorTablePartContent.getFieldValue(iPartsConst.FIELD_DCTC_PGRP),
                                                                                      colorTablePartContent.getFieldValue(iPartsConst.FIELD_DCTC_SDATA));
                            } else {
                                return null;
                            }
                        }
                    }, this, TranslationHandler.translate("!!Code-Stammdaten AS"));
                    contextMenu.addChild(codeMasterDataASMenuItem);
                }

                @Override
                public void modifyContextMenu(ToolbarButtonMenuHelper toolbarHelper) {
                    // Vor dem Status-Menü und nach dem Löschen-Menü das Vereinheitlichen-Menü einfügen
                    // Und nur im unteren Grid
                    if (editMode) {
                        ToolbarButtonMenuHelper.ToolbarMenuHolder holder =
                                toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MULTIPLE_ENTRIES, EDIT_MULTIPLE_ENTRIES_TEXT,
                                                                            getUITranslationHandler(), new MenuRunnable() {
                                            @Override
                                            public void run(Event event) {
                                                doEditMultipleEntries();
                                            }
                                        });
                        toolbarHelper.insertMenuBeforeTableCopyMenu(gridBottom.getContextMenu(), holder.menuItem, true);
                        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MULTIPLE_ENTRIES, gridBottom.getContextMenu(), false);
                    }
                    super.modifyContextMenu(toolbarHelper);
                }

                @Override
                protected void statusChanged() {
                    handleStatusChanged(this);
                }

                @Override
                protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                    if (tableName.equals(iPartsConst.TABLE_DA_COLORTABLE_CONTENT) && (fieldName.equals(iPartsConst.FIELD_DCTC_EVENT_FROM_AS)
                                                                                      || fieldName.equals(iPartsConst.FIELD_DCTC_EVENT_TO_AS))) {
                        String value = objectForTable.getFieldValue(fieldName);
                        if (iPartsEvent.isNotRelevantEventId(value)) {
                            EtkProject project = getProject();
                            return iPartsEvent.createNotRelevantEvent(project).getTitle().getTextByNearestLanguage(project.getDBLanguage(),
                                                                                                                   project.getDataBaseFallbackLanguages());
                        }
                    }

                    return super.getVisualValueOfField(tableName, fieldName, objectForTable);

                }

                @Override
                protected iPartsDataReleaseState getStatusFromSelection(List<List<EtkDataObject>> selection) {
                    iPartsDataReleaseState releaseState = super.getStatusFromSelection(selection);
                    if ((releaseState != null) && isVariantsEditEnabled()) {
                        return releaseState;
                    }
                    return null;
                }
            };
            variantsGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

            return variantsGrid;
        }
    }

    /**
     * Verarbeitet eine Statusänderung im übergebenen Grid
     *
     * @param grid
     */
    private void handleStatusChanged(DataObjectFilterGridWithStatus grid) {
        GenericEtkDataObjectList<EtkDataObject> modifiedDataObjects = new GenericEtkDataObjectList<>();
        List<GuiTableRow> selectedRows = grid.getTable().getSelectedRows();
        for (GuiTableRow selectedRow : selectedRows) {
            if (selectedRow instanceof DataObjectGrid.GuiTableRowWithObjects) {
                EtkDataObject objectForStatus = ((DataObjectGrid.GuiTableRowWithObjects)selectedRow).getObjectForTable(grid.getStatusTableName());
                if (objectForStatus != null) {
                    modifiedDataObjects.add(objectForStatus, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        saveDataObjectsWithUpdate(modifiedDataObjects);
    }

    private List<iPartsDataColorTableContent> getSelectedDataColorTableContent() {
        List<iPartsDataColorTableContent> selectedList = new ArrayList<>();
        List<GuiTableRow> selectedRows = getGrid(false).getTable().getSelectedRows();
        if ((selectedRows != null) && (selectedRows.size() > 1)) {
            for (GuiTableRow row : selectedRows) {
                if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                    iPartsDataColorTableContent colorTableContent =
                            (iPartsDataColorTableContent)((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(iPartsConst.TABLE_DA_COLORTABLE_CONTENT);
                    selectedList.add(colorTableContent);
                }
            }
        }
        return selectedList;
    }

    @Override
    protected boolean hideRelatedInfoCalled() {
        if (!getConnector().isEditContext()) {
            return true;
        }

        DataObjectGrid variantsGrid = getGrid(false);
        // Falls der InPlaceEditor offen ist erst mal diesen beenden, und danach erst zulassen dass die RelatedInfo geschlossen wird
        if ((variantsGrid.getTable().getInPlaceEditorFactory() instanceof GuiTableInplaceEditorManager) &&
            ((GuiTableInplaceEditorManager)variantsGrid.getTable().getInPlaceEditorFactory()).isInplaceEditorActive()) {
            GuiTableInplaceEditorManager inplaceEditorManager = (GuiTableInplaceEditorManager)variantsGrid.getTable().getInPlaceEditorFactory();
            if ((relatedInfoMainForm == null) || (relatedInfoMainForm.getModalResult() == ModalResult.CANCEL)) {
                // Abbrechen-Button gedrückt => Inplace-Editor abbrechen
                inplaceEditorManager.cancelActiveEditor();
                // den Focus auf die Table setzen, damit ESC-Key wieder beim Button bzw Panel ankommt
                variantsGrid.getTable().requestFocus();
                // RelatedInfo kann nicht geschlossen werden, da zuerst der Inplace-Editor abgebrochen wird
                return false;
            } else {
                // Abspeichern-Button gedrückt
                if (inplaceEditorManager.isInplaceEditorModified()) {
                    GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult validationResult = inplaceEditorManager.validateActiveEditor();
                    if (validationResult.isInvalid()) {
                        // Validation-Result ist INVALID => ggf Anzeige warum
                        inplaceEditorManager.onActiveEditorValidationResult(validationResult);
                        // RelatedInfo kann nicht geschlossen werden
                        return false;
                    }
                    // bei Validation-Result != INVALID kann ReleatedInfo geschlossen werden
                    return true;
                } else {
                    // geöffneter,aber unveränderter Inplace-Editor => zur Sicherheit schließen
                    inplaceEditorManager.cancelActiveEditor();
                    // ReleatedInfo kann geschlossen werden
                    return true;
                }
            }
            // Dieser Code wäre schön, funktioniert aber unter JEE nicht, weil wenn ESC gedrückt wird IMMER ModalResult = OK rauskommt
            // das liegt vermutlich irgendwo ganz tief vergraben im Javascript
            // Unter Swing kommt man garnicht hier an, weil der dort der Inplace Editor selbst des ESC Key Event bekommt
//                boolean setCellContentFromInPlaceEditor = getModalResultOfRelatedInfoMainForm() != ModalResult.CANCEL;
//                return variantsGrid.getTable().closeCurrentInPlaceEditor(setCellContentFromInPlaceEditor);
        } else {
            return super.hideRelatedInfo();
        }
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        // das Grid nur dann neu aufbauen wenn eine andere Instanz updateData geschickt hat. Die Änderungen am eigenen
        // Grid werden anderweitig abgehandelt und brauchen kein dataChanged
        if (forceUpdateAll || ((getConnector().getActiveRelatedSubForm() == parentForm) && (sender != this))) {
            dataChanged();
        }
    }

    @Override
    public void dataChanged() {
        dataToGrid();
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(boolean top) {
        if (top) {
            return createVariantTablesDataDefaultDisplayFields();
        } else {
            return createVariantsDataDefaultDisplayFields();
        }
    }

    @Override
    public boolean isShowHistory() {
        return isRetailPartList || (editMode && super.isRetailFilter()) || super.isShowHistory();
    }

    @Override
    public boolean isRetailFilter() {
        return isRetailPartList && super.isRetailFilter();
    }

    protected boolean isRetailFilterForFactoryData() {
        return isRetailFilter();
    }

    @Override
    protected void reloadEditableDataAndUpdateEditContext() {
        EditFormIConnector editModuleFormConnector = ((iPartsRelatedInfoEditContext)getConnector().getEditContext()).getEditFormConnector();
        if (editModuleFormConnector != null) {
            EtkDataAssembly ownerAssembly = editModuleFormConnector.getCurrentAssembly();
            if (ownerAssembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsOwnerAssembly = (iPartsDataAssembly)ownerAssembly;

                // Farbtabellen für diese Assembly wurden verändert -> komplett neu laden. Dazu werden zuerst die gefilterten
                // Farbtabellen gelöscht und danach die Retail-Farbtabellen neu geladen. Dadurch wird beim nächsten Zugriff
                // die Filterung erneut durchgeführt.
                iPartsOwnerAssembly.clearAllColortableDataForRetailFilteredForPartList();
                iPartsOwnerAssembly.loadAllColorTableForRetailForPartList(iPartsOwnerAssembly.getPartListUnfiltered(null));

                if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
                    iPartsRelatedInfoEditContext editContext = ((iPartsRelatedInfoEditContext)getConnector().getEditContext());
                    editContext.setFireDataChangedEvent(true);
                    editContext.setUpdateRetailColortableData(true);
                    editContext.setUpdateEditAssemblyData(true);
                }
            }
        }
    }

    private void doEditMultipleEntries() {
        final EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
        if (revisionsHelper == null) {
            return;
        }
        AbstractRevisionChangeSet revisionChangeSetForEdit = revisionsHelper.getActiveRevisionChangeSetForEdit();
        if (revisionChangeSetForEdit != null) {
            EtkDataAssembly assembly = getCurrentPartListEntry().getOwnerAssembly();
            List<iPartsDataColorTableContent> selectedDataColorTableContent = getSelectedDataColorTableContent();
            EtkEditFields externalEditFields = new EtkEditFields();
            EtkEditField externalEditField = new EtkEditField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_EVAL_PEM_FROM, false);
            externalEditFields.addField(externalEditField);
            externalEditField = new EtkEditField(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_EVAL_PEM_TO, false);
            externalEditFields.addField(externalEditField);

            DBDataObjectAttributes initValues =
                    EditUserMultiChangeControls.getInitialAttributesForUnify(selectedDataColorTableContent,
                                                                             EditUserMultiChangeControls.getFieldsAndDefaultValuesForUnify(externalEditFields));

            EditUserMultiChangeControlsForVariantsToParts multiControl = new EditUserMultiChangeControlsForVariantsToParts(getConnector(), getConnector().getActiveForm(),
                                                                                                                           externalEditFields, initValues, getCurrentPartListEntry());

            DBDataObjectAttributes attributes = EditUserMultiChangeControls.showEditUserMultiChangeControls(getConnector(), externalEditFields, initValues, multiControl,
                                                                                                            EditUserMultiChangeControls.UnifySource.AFTERSALES);
            if (attributes != null) {
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm(EDIT_MULTIPLE_ENTRIES_TEXT, "!!Bitte warten...",
                                                                               null, true);
                messageLogForm.disableButtons(true);
                messageLogForm.showModal(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        EtkDataObjectList dataObjectList =
                                EditUserMultiChangeControlsForVariantsToParts.createDataObjectListForSelectedEntries(assembly, selectedDataColorTableContent, attributes,
                                                                                                                     multiControl);
                        if ((dataObjectList != null) && (!dataObjectList.isEmpty() || !dataObjectList.getDeletedList().isEmpty())) {
                            // Füge die Objekte zum ChangeSet hinzu
                            Session.invokeThreadSafeInSession(() -> saveDataObjectsWithUpdate(dataObjectList));
                        }
                        // Wartedialog schließen
                        messageLogForm.closeWindowIfNotAutoClose(ModalResult.OK);
                    }
                });
            }
        }
    }

    private void doDeleteVariant() {
        GenericEtkDataObjectList<EtkDataObject> deletedDataObjects = new GenericEtkDataObjectList<>();
        List<GuiTableRow> selectedRows = getGrid(false).getTable().getSelectedRows();
        if ((selectedRows != null) && !selectedRows.isEmpty()) {
            for (GuiTableRow selectedRow : selectedRows) {
                if (selectedRow instanceof DataObjectGrid.GuiTableRowWithObjects) {
                    EtkDataObject colorTableContent = ((DataObjectGrid.GuiTableRowWithObjects)selectedRow).getObjectForTable(iPartsConst.TABLE_DA_COLORTABLE_CONTENT);
                    if (colorTableContent != null) {
                        deletedDataObjects.delete(colorTableContent, true, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
        saveDataObjectsWithUpdate(deletedDataObjects);
    }

    private void doNewVariant() {
        List<GuiTableRow> multiSelection = getGrid(true).getTable().getSelectedRows();
        if ((multiSelection != null) && (multiSelection.size() == 1)) {
            GuiTableRow selectedRow = multiSelection.get(0);
            if (selectedRow instanceof DataObjectGrid.GuiTableRowWithObjects) {
                EtkDataObject colorTablePart = ((DataObjectGrid.GuiTableRowWithObjects)selectedRow).getObjectForTable(iPartsConst.TABLE_DA_COLORTABLE_PART);
                if (colorTablePart instanceof iPartsDataColorTableToPart) {
                    iPartsColorTableToPartId colorTableToPartId = ((iPartsDataColorTableToPart)colorTablePart).getAsId();
                    String productGroup = getProductGroupForColorTable((DataObjectGrid.GuiTableRowWithObjects)selectedRow);
                    iPartsDocumentationType docuType = getDocumentationTypeOfOwnerAssembly();
                    iPartsDataColorTableContent result = EditUserControlsForVariants.showCreateVariantsData(getConnector(), docuType, this, colorTableToPartId, productGroup);
                    if (result != null) {
                        saveDataObjectWithUpdate(result);
                    }
                }
            }
        }
    }

    /**
     * Editiert die AS-Felder für Farbinhalte
     */
    private void doEditASFields() {
        if (editMode && isVariantsEditEnabled()) {
            List<GuiTableRow> selectedRows = getGrid(false).getTable().getSelectedRows();
            if ((selectedRows != null) && (selectedRows.size() == 1)) {
                GuiTableRow selectedRow = selectedRows.get(0);
                if (selectedRow instanceof DataObjectGrid.GuiTableRowWithObjects) {
                    iPartsDataColorTableContent colorTableContent = (iPartsDataColorTableContent)((DataObjectGrid.GuiTableRowWithObjects)selectedRow).getObjectForTable(iPartsConst.TABLE_DA_COLORTABLE_CONTENT);
                    if (colorTableContent != null) {
                        iPartsDocumentationType docuType = getDocumentationTypeOfOwnerAssembly();
                        iPartsDataColorTableContent editedContent = EditUserControlsForVariants.editVariantsData(getConnector(), docuType, this, colorTableContent, isReadOnly());
                        if ((editedContent != null) && editedContent.isModified()) {
                            saveDataObjectWithUpdate(editedContent);
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void enableButtonsAndMenu() {
        // in Superklasse findet der Check statt, dass nur Daten mit Quelle IPARTS gelöscht werden kann
        super.enableButtonsAndMenu();
        enableVariantTablesButtonsAndMenu();
        enableVariantsButtonsAndMenu();
    }

    private String getTableNameForSelection(boolean isTop) {
        if (isTop) {
            return iPartsConst.TABLE_DA_COLORTABLE_PART;
        } else {
            return iPartsConst.TABLE_DA_COLORTABLE_CONTENT;
        }
    }

    /**
     * Stellt die übergebenen Selektionen im angegebenen Grid wieder her, ohne ein TABLE_SELECTION_EVENT zu feuern.
     * Wenn keine der Selektionen wiederhergestellt werden konnte, wird der erste Eintrag selektiert, falls es einen gibt.
     *
     * @param selectedIds
     * @param isTop
     */
    protected void restoreSelectionSilent(List<IdWithType> selectedIds, boolean isTop) {
        if ((selectedIds == null) || selectedIds.isEmpty()) {
            return;
        }
        String tableName = getTableNameForSelection(isTop);
        List<IdWithType> currentSelection = getGrid(isTop).getSelectedObjectIds(tableName);
        if (!currentSelection.containsAll(selectedIds)) {
            getGrid(isTop).setSelectedObjectIds(selectedIds, tableName, false, false);
        }
        currentSelection = getGrid(isTop).getSelectedObjectIds(tableName);
        if (currentSelection.isEmpty() && (getGrid(isTop).getTable().getRowCount() > 0)) {
            getGrid(isTop).getTable().setSelectedRow(0, false, false);
        }
    }

    protected void enableVariantTablesButtonsAndMenu() {
        DataObjectFilterGridWithStatus variantTableGrid = ((DataObjectFilterGridWithStatus)getGrid(true));
        variantTableGrid.handleTableSelectionChangeForStatusContextMenu();
        if (editMode) {
            setReadOnly(isReadOnly() || !isVariantsEditEnabled());
        }
    }

    private void enableVariantsButtonsAndMenu() {
        boolean isSingleSelection = getGrid(false).getMultiSelection().size() == 1;
        variantFactoryDataMenuItem.setEnabled(isSingleSelection);
        if (isSingleSelection) { // nur eine Zeile darf selektiert sein
            iPartsDataColorTableContent colorTableContent = getSelection(false, iPartsDataColorTableContent.class);
            String code = colorTableContent.getFieldValue(iPartsConst.FIELD_DCTC_CODE);
            if (codeMasterDataMenuItem != null) {
                codeMasterDataMenuItem.setEnabled(!DaimlerCodes.isEmptyCodeString(code));
            }
            String codeAS = colorTableContent.getFieldValue(iPartsConst.FIELD_DCTC_CODE_AS);
            if (codeMasterDataASMenuItem != null) {
                codeMasterDataASMenuItem.setEnabled(!DaimlerCodes.isEmptyCodeString(codeAS));
            }
        }

        DataObjectFilterGridWithStatus variantGrid = ((DataObjectFilterGridWithStatus)getGrid(false));
        variantGrid.handleTableSelectionChangeForStatusContextMenu();
        // Falls es keine Variantentabellen gibt, können auch keine Varianten angelegt werden
        setReadOnly(isReadOnly() || !isVariantsEditEnabled() || (getGrid(true).getTable().getRowCount() == 0));
    }

    @Override
    protected void setReadOnly(boolean isReadOnly) {
        super.setReadOnly(isReadOnly);
        boolean isMultiSelection = getGrid(false).getMultiSelection().size() > 1;
        if (toolbarHelperBottom != null) {
            getToolbarHelper(false).enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MULTIPLE_ENTRIES, getGrid(false).getContextMenu(),
                                                               !isReadOnly && isMultiSelection);
        }
    }

    protected boolean isVariantsEditEnabled() {
        // PRIMUS Farbtabellen dürfen nicht bearbeitet werden, also hier das Status Menü disablen
        boolean enableVariantsEdit = false;
        if (editMode && isEditContext(getConnector(), true) && !isReadOnly()) {
            boolean isPSKAssembly = isPSKAssembly();
            for (iPartsDataColorTableToPart selectedObject : getMultiSelection(true, iPartsDataColorTableToPart.class)) {
                String sourceField = selectedObject.getFieldValue(iPartsConst.FIELD_DCTP_SOURCE);
                if (StrUtils.isValid(sourceField)) {
                    if (sourceField.equals(iPartsImportDataOrigin.PRIMUS.getOrigin())) {
                        enableVariantsEdit = false;
                        break;
                    } else if (selectedObject.getAsId().getColorTableId().startsWith(ColorTableHelper.QFT_COLORTABLE_ID_PREFIX)) {
                        // QFT-Farbvariantentabellen dürfen in PSK-Modulen nicht bearbeitet werden
                        enableVariantsEdit = !isPSKAssembly;
                    } else {
                        enableVariantsEdit = true;
                    }
                }
            }
        }
        return enableVariantsEdit;
    }

    @Override
    protected void doNew(boolean top) {
        if (!top) {
            doNewVariant();
        }
    }

    @Override
    protected void doEdit(boolean top) {
        if (top) {
            doEditVariantsToPart();
        } else {
            doEditASFields();
        }
    }

    /**
     * Öffnet den Dialog zum Editieren der Farbtabellen zum Teil
     */
    private void doEditVariantsToPart() {
        if (editMode && isVariantsEditEnabled()) {
            iPartsDataColorTableToPart colorTableToPart = getSelection(true, iPartsDataColorTableToPart.class);
            if (colorTableToPart != null) {
                boolean editSuccessful = EditUserControlsForVariantsToPart.editVariantsToPartData(getConnector(), this,
                                                                                                  colorTableToPart, isReadOnly());
                if (editSuccessful) {
                    saveDataObjectWithUpdate(colorTableToPart);
                }
            }
        }
    }

    @Override
    protected void doDelete(boolean top) {
        if (!top) {
            doDeleteVariant();
        }
    }

    @Override
    protected String getTableName() {
        return iPartsConst.TABLE_DA_COLORTABLE_CONTENT;
    }

    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
        if (top) {
            variantTablesDataToGrid();
        } else {
            variantsDataToGrid();
        }
    }

    /**
     * DAIMLER-5903:
     * (1.) Bei Anlage neuer Varianten ist die Produktgruppe (PG) mit der PG der Baureihe aus der Variantentabelle (QFT) zu belegen.
     * (2.) Ist keine Baureihe bzw. die BR ohne PG vorhanden, dann soll die PG mit der PG der Teileposition vorbelegt werden.
     * (3.) Falls keine PG ermittelt werden kann, wird als Default-Wert "P"=PKW zurückgegeben.
     *
     * @param selectedRow
     * @return
     */
    private String getProductGroupForColorTable(DataObjectGrid.GuiTableRowWithObjects selectedRow) {
        EtkDataObject colorTableData = selectedRow.getObjectForTable(iPartsConst.TABLE_DA_COLORTABLE_DATA);
        if (colorTableData != null) {
            iPartsSeriesId validSeriesForQFTId = new iPartsSeriesId(colorTableData.getFieldValue(iPartsConst.FIELD_DCTD_VALID_SERIES));
            if (validSeriesForQFTId.isValidId()) {
                iPartsDataSeries validSeriesForQFT = new iPartsDataSeries(getProject(), validSeriesForQFTId);
                String productGroup = validSeriesForQFT.getFieldValue(iPartsConst.FIELD_DS_PRODUCT_GRP);
                if (!productGroup.isEmpty()) {
                    return productGroup; // (1.)
                }
            }
        }
        String productGroup = currentPartListEntry.getFieldValue(iPartsConst.FIELD_K_PRODUCT_GRP);
        if (!productGroup.isEmpty()) {
            return currentPartListEntry.getFieldValue(iPartsConst.FIELD_K_PRODUCT_GRP); // (2.)
        }
        return iPartsConst.AS_PRODUCT_CLASS_CAR; // (3.)
    }

    protected void variantTablesDataToGrid() {
        filterProductId = null;
        filterSeriesId = null;
        EtkProject project = getProject();
        List<ColorTableHelper.VariantTablesDataStructure> variantTablesDataStructures = new DwList<>();
        if (isRetailFilter()) {
            // Produkt bestimmen
            if (currentPartListEntry.getOwnerAssembly() instanceof iPartsDataAssembly) {
                filterProductId = ((iPartsDataAssembly)currentPartListEntry.getOwnerAssembly()).getProductIdFromModuleUsage();
            }

            // Baureihe aus dem Original-BCTE-Schlüssel von einer DIALOG-Stückliste auslesen
            iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromDbValue(currentPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_TYPE));
            if (sourceType == iPartsEntrySourceType.DIALOG) {
                String sourceContext = currentPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_CONTEXT);
                HmMSmId hmMSmId = HmMSmId.getHmMSmIdFromDIALOGSourceContext(sourceContext);
                if (hmMSmId != null) {
                    filterSeriesId = new iPartsSeriesId(hmMSmId.getSeries());
                }
            }
            if (currentPartListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsDataPartListEntry = (iPartsDataPartListEntry)currentPartListEntry;
                iPartsColorTable colorTableForRetail = getFilteredColorTableForRetail(iPartsDataPartListEntry);
                if (colorTableForRetail != null) {
                    Map<String, iPartsColorTable.ColorTable> colorTablesMap = colorTableForRetail.getColorTablesMap();
                    for (Map.Entry<String, iPartsColorTable.ColorTable> colorTable : colorTablesMap.entrySet()) {
                        iPartsDataColorTableData tableData = new iPartsDataColorTableData(project, new iPartsColorTableDataId(colorTable.getKey()));
                        for (iPartsColorTable.ColorTableToPart colorTableToPart : colorTable.getValue().colorTableToPartsMap.values()) {
                            iPartsDataColorTableToPart tableToPart = colorTableToPart.getDataColorTableToPart(true, project);
                            ColorTableHelper.VariantTablesDataStructure variantTablesDataStructure = new ColorTableHelper.VariantTablesDataStructure();
                            variantTablesDataStructure.colorTableToPart = tableToPart;
                            variantTablesDataStructure.colorTableData = tableData;
                            variantTablesDataStructures.add(variantTablesDataStructure);
                        }
                    }
                }
            }
        } else {
            variantTablesDataStructures = ColorTableHelper.getVariantTablesDataForPartListEntry(project, currentPartListEntry,
                                                                                                filterProductId, filterSeriesId,
                                                                                                isShowHistory(), isRetailFilter());
        }
        // Änderungsobjekte laden
        initDialogChangeData();
        // Prüfen, ob Werkseinsatzdaten-Änderungen vorhanden sind
        iPartsDIALOGChangeHelper.checkVariantToPartFactoryData(project, variantTablesDataStructures, factoryDataDialogChangesForPartNo);
        for (ColorTableHelper.VariantTablesDataStructure variantTablesDataStructure : variantTablesDataStructures) {
            iPartsDataColorTableToPart colorTableToPart = variantTablesDataStructure.colorTableToPart;
            if (!colorTableToPart.existsInDB()) {
                colorTableToPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            iPartsDataColorTableData colorTableData = variantTablesDataStructure.colorTableData;
            if (!colorTableData.existsInDB()) {
                colorTableData.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            PartId partId = new PartId(colorTableToPart.getPartNumber(), "");
            EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(project, partId);
            if (!dataPart.existsInDB()) {
                dataPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            addDataObjectToGrid(true, colorTableData, colorTableToPart, dataPart);
        }
    }

    protected iPartsColorTable getFilteredColorTableForRetail(iPartsDataPartListEntry partListEntry) {
        return partListEntry.getColorTableForRetail();
    }

    protected void variantsDataToGrid() {
        EtkDisplayFields displayFields = getDisplayFields(false);

        String codeASTableAndField = TableAndFieldName.make(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_CODE_AS);
        int codeAsIndex = displayFields.getIndexOfFeld(codeASTableAndField, false);

        // Kontextmenu für AS Codestamm Dialog aktivieren abhängig davon ob das Feld sichtbar ist
        if (codeMasterDataASMenuItem != null) {
            codeMasterDataASMenuItem.setVisible(codeAsIndex >= 0);
        }

        // Kontextmenu für Codestamm Dialog aktivieren abhängig davon ob das Feld sichtbar ist
        if (codeMasterDataMenuItem != null) {
            boolean enableCodeContextMenu = displayFields.contains(TableAndFieldName.make(iPartsConst.TABLE_DA_COLORTABLE_CONTENT, iPartsConst.FIELD_DCTC_CODE), false);
            codeMasterDataMenuItem.setVisible(enableCodeContextMenu);
        }

        // Wenn nichts ausgewählt ist, passiert nichts
        List<EtkDataObject> selectedVariantTableRow = getGrid(true).getSelection();
        if ((selectedVariantTableRow != null) && (selectedVariantTableRow.size() > 1)) {
            // Das erste DataObject in dieser Zeile muss das iPartsDataColorTableData Object sein
            if (selectedVariantTableRow.get(0) instanceof iPartsDataColorTableData) {
                EtkProject project = getProject();
                iPartsDataColorTableData colorTableData = (iPartsDataColorTableData)selectedVariantTableRow.get(0);
                List<ColorTableHelper.VariantsDataStructure> variantsDataStructures = new DwList<>();
                if (isRetailFilter()) {
                    iPartsDataPartListEntry iPartsDataPartListEntry = null;

                    if (currentPartListEntry instanceof iPartsDataPartListEntry) {
                        iPartsDataPartListEntry = (iPartsDataPartListEntry)currentPartListEntry;
                    }
                    if (iPartsDataPartListEntry != null) {
                        iPartsColorTable colorTableForRetail = getFilteredColorTableForRetail(iPartsDataPartListEntry);
                        if (colorTableForRetail != null) {
                            Map<String, iPartsColorTable.ColorTable> colorTablesMap = colorTableForRetail.getColorTablesMap();
                            iPartsColorTable.ColorTable colorTable = colorTablesMap.get(colorTableData.getAsId().getColorTableId());
                            for (iPartsColorTable.ColorTableContent colorTableContent : colorTable.colorTableContents) {
                                iPartsDataColorTableContent colorTableContentData = colorTableContent.getDataColorTableContent(true, project);
                                iPartsDataColorNumber colorNumber = new iPartsDataColorNumber(project, new iPartsColorNumberId(colorTableContent.colorNumber));

                                // DAIMLER-7068: Manipulierte Flags "PEM ab/bis auswerten" aus den Retail-Farbvarianten übernehmen
                                colorTableContentData.setFieldValueAsBoolean(iPartsConst.FIELD_DCTC_EVAL_PEM_FROM, colorTableContent.isEvalPemFrom(),
                                                                             DBActionOrigin.FROM_DB);
                                colorTableContentData.setFieldValueAsBoolean(iPartsConst.FIELD_DCTC_EVAL_PEM_TO, colorTableContent.isEvalPemTo(),
                                                                             DBActionOrigin.FROM_DB);

                                ColorTableHelper.VariantsDataStructure variantsDataStructure = new ColorTableHelper.VariantsDataStructure();
                                variantsDataStructure.colorNumber = colorNumber;
                                variantsDataStructure.colorTableContent = colorTableContentData;
                                variantsDataStructure.colorTableData = colorTableData;
                                variantsDataStructures.add(variantsDataStructure);
                            }
                        }
                    }
                } else {
                    iPartsColorTableDataId variantTableId = new iPartsColorTableDataId(colorTableData.getFieldValue(iPartsConst.FIELD_DCTD_TABLE_ID));

                    variantsDataStructures = ColorTableHelper.getVariantsDataForVariantTableId(project, currentPartListEntry,
                                                                                               variantTableId, filterProductId, colorTableData,
                                                                                               isShowHistory(), isRetailFilter());
                }
                // Änderungsobjekte laden
                initDialogChangeData();
                // Prüfen, ob Werkseinsatzdaten-Änderungen vorhanden sind
                iPartsDIALOGChangeHelper.checkVariantDataFactoryData(project, variantsDataStructures, factoryDataDialogChangesForPartNo);
                for (ColorTableHelper.VariantsDataStructure variantsDataStructure : variantsDataStructures) {
                    iPartsDataColorTableContent colorTableContent = variantsDataStructure.colorTableContent;
                    if (!colorTableContent.existsInDB()) {
                        colorTableContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }
                    colorTableData = variantsDataStructure.colorTableData;
                    if (!colorTableData.existsInDB()) {
                        colorTableData.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }
                    iPartsDataColorNumber colorNumber = variantsDataStructure.colorNumber;
                    if (!colorNumber.existsInDB()) {
                        colorNumber.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }
                    addDataObjectToGrid(false, colorTableData, colorTableContent, colorNumber);
                }
            }
        }

        DataObjectGrid variantsGrid = getGrid(false);
        boolean isVariantsGridNotEmpty = (variantsGrid.getTable().getRowCount() > 0);
        variantsGrid.showNoResultsLabel(!isVariantsGridNotEmpty);

        // Sortieren nach der Position
        int posIndex = -1;
        int i = 0;
        for (EtkDisplayField field : variantsGrid.getDisplayFields().getFields()) {
            if (field.isVisible() && field.getKey().getFieldName().equals(iPartsConst.FIELD_DCTC_POS)) {
                posIndex = i;
                break;
            }
            i++;
        }
        variantsGrid.getTable().sortRowsAccordingToColumn(posIndex, true);
    }

    /**
     * Lädt alle Werkseinsatzdaten {@link iPartsDataDIALOGChange}s zur Teilenummer des aktuellen {@link iPartsDataPartListEntry}
     * <p>
     * Benötigt werden diese Objekte zum Bestimmen, ob die Werkseinsatzdaten der Farbtabellen/Zuordnung Teil zu Farbtabelle
     * Änderungen beinhalten, die sich der Autor anschauen sollte.
     */
    private void initDialogChangeData() {
        if (factoryDataDialogChangesForPartNo == null) {
            factoryDataDialogChangesForPartNo = iPartsDataDIALOGChangeList.loadForMatNoAndType(getProject(),
                                                                                               getCurrentPartListEntry().getPart().getAsId().getMatNr(),
                                                                                               iPartsDataDIALOGChange.ChangeType.COLORTABLE_FACTORY_DATA);

            // Die DialogChanges zu den Werksdaten zu Farben zusätzlich auf die relevanten Werke zum Produkt einschränken
            iPartsProductId productId = null;
            if (filterProductId != null) {
                // Wenn wir das Produkt für den Retailfilter schonmal bestimmt haben, dann dieses verwenden
                productId = filterProductId;
            } else {
                if ((currentPartListEntry != null) && (currentPartListEntry.getOwnerAssembly() instanceof iPartsDataAssembly)) {
                    productId = ((iPartsDataAssembly)currentPartListEntry.getOwnerAssembly()).getProductIdFromModuleUsage();
                }
            }
            if (productId != null) {
                iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                Set<String> productFactories = new TreeSet<>(product.getProductFactories(getProject()));

                factoryDataDialogChangesForPartNo = factoryDataDialogChangesForPartNo.filterForColorTableFactories(productFactories);
            }
        }
    }

    @Override
    protected void checkboxShowHistoryClicked(Event event) {
        dataToGrid();
    }

    @Override
    protected void checkboxRetailFilterClicked(Event event) {
        dataToGrid();
        // für das untere Grid einen Selection Changed Event provizieren damit die Buttons richtig reagieren
        int selectedRowIndex = getGrid(false).getTable().getSelectedRowIndex();
        getGrid(false).getTable().setSelectedRow(selectedRowIndex, false);
    }

    @Override
    protected void fillGrid(boolean isTop) {
        List<IdWithType> selectedIdsTop = getGrid(true).getSelectedObjectIds(getTableNameForSelection(true));
        List<IdWithType> selectedIdsBottom = getGrid(false).getSelectedObjectIds(getTableNameForSelection(false));

        super.fillGrid(isTop);

        // Bei ersten Durchlauf ist noch keine Selektion vorhanden. Diese auf den ersten Eintrag setzen.
        if (selectedIdsTop.isEmpty() && (getGrid(true).getTable().getRowCount() > 0)) {
            EtkDataObject firstDataObjectTop = getGrid(true).getDataObjectForRowAndTable(0, getTableNameForSelection(true));
            if (firstDataObjectTop != null) {
                selectedIdsTop.add(firstDataObjectTop.getAsId());
            }
        }
        // Selektionen wiederherstellen, die nach dem befüllen der Grids verloren gegangen sind
        restoreSelectionSilent(selectedIdsTop, true);
        restoreSelectionSilent(selectedIdsBottom, false);

        enableButtonsAndMenu();
    }

    public boolean isReadOnly() {
        return isRetailFilter() || !(editMode && isEditContext(getConnector(), true));
    }

    @Override
    protected boolean isContextMenuEntryForNewObjectVisible(boolean top) {
        return !top;
    }

    @Override
    protected boolean isContextMenuEntryForDeleteObjectVisible(boolean top) {
        return !top;
    }

    @Override
    protected void doTableSelectionChanged(boolean top) {
        enableButtonsAndMenu();
        if (top && !getGrid(top).getMultiSelection().isEmpty()) {
            // Wenn sich die Selektion der Farbvariantentabelle ändert, müssen die zugehörigen Varianten angezeigt werden.
            // Wenn das Grid gecleared wurde, wird die Selektion zurückgesetzt und das ein TABLE_SELECTION_EVENT gefeuert.
            // In dem Fall werden später beide Grids befüllt, also hier nicht versuchen ohne Selektion das untere Grid zu füllen.
            dataToGrid(false);
        }
    }

    @Override
    public String getSourceFieldName() {
        return iPartsConst.FIELD_DCTC_SOURCE;
    }

    @Override
    protected String getStatusFieldName(boolean isTop) {
        if (isTop) {
            return iPartsConst.FIELD_DCTP_STATUS;
        } else {
            return iPartsConst.FIELD_DCTC_STATUS;
        }
    }

    private String getSeriesNoOfPartlistEntry() {
        return getCurrentPartListEntry().getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO);
    }

    private iPartsDocumentationType getDocumentationTypeOfOwnerAssembly() {
        EtkDataAssembly ownerAssembly = getCurrentPartListEntry().getOwnerAssembly();
        if (ownerAssembly instanceof iPartsDataAssembly) {
            return ((iPartsDataAssembly)ownerAssembly).getDocumentationType();
        }
        return iPartsDocumentationType.UNKNOWN;
    }

    private EtkDataPartListEntry getCurrentPartListEntry() {
        return getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
    }
}