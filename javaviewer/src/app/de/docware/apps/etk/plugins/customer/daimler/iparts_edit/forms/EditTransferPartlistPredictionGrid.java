/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EnumCheckRComboBox;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.forms.events.OnValidateAttributesEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.filter.EtkFilterItem;
import de.docware.apps.etk.base.project.filter.EtkFilterItems;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiPanelWithCheckbox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PartListEntryReferenceKeyByAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.RowContentForTransferToAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.TransferToASElement;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonType;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.TableAndFieldName;

import java.awt.event.KeyEvent;
import java.util.*;

public class EditTransferPartlistPredictionGrid extends SimpleMasterDataSearchFilterGrid implements iPartsConst {

    public static String TABLE_PSEUDO = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER");
    public static String FIELD_PSEUDO_TRANSFER = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-TO_TRANSFER");
    public static String FIELD_PSEUDO_ASSIGNED = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-ASSIGNED");
    public static String FIELD_PSEUDO_PRODUCT = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-PRODUCT");
    public static String FIELD_PSEUDO_KG = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-KG");
    public static String FIELD_PSEUDO_TU = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-TU");
    public static String FIELD_PSEUDO_SA = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-SA");

    private static String KGTU_COPY_ALIAS = "kgtuCopy";
    private static String KGTU_PASTE_ALIAS = "kgtuPaste";
    private static String HOTSPOT_UNIFY_ALIAS = "hotspotUnify";
    private static String TRANSFER_UNIFY_ALIAS = "transferUnify";
    private static String KGTU_COPY_TEXT = "!!KG/TU kopieren";
    private static String KGTU_PASTE_TEXT = "!!KG/TU einfügen";
    private static String TRANSFER_TEXT_ALL = "!!Alle markierten übernehmen";
    private static String TRANSFER_TEXT_NON = "!!Alle markierten nicht übernehmen";

    public static final String CONFIG_KEY_PARTLIST_PREDICTION_DISPLAYFIELDS_DIALOG =
            "Plugin/iPartsEdit/AS_Transfer" + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;
    public static final String CONFIG_KEY_PARTLIST_PREDICTION_DISPLAYFIELDS_EDS =
            "Plugin/iPartsEdit/AS_Transfer_EDS" + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;
    public static final String CONFIG_KEY_PARTLIST_PREDICTION_DISPLAYFIELDS_MBS =
            "Plugin/iPartsEdit/AS_Transfer_MBS" + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;

    private List<String> INLINE_EDIT_FIELDS;
    private List<MustFieldDescription> PSEUDO_MUST_FIELDS;

    private int defaultLabelHeight = 0;
    private boolean eventListenersActive;
    private String tooltip_noKGTU;
    private String tooltip_Assigned;
    private KgTuId kgTuSave;
    private SimpleMasterDataSearchFilterFactoryPrediction filterFactory;
    private boolean contextMenuTransferAll;
    private boolean enableEditAssignedRow;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     * @param onEditChangeRecordEvent
     */
    public EditTransferPartlistPredictionGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, EditTransferPartlistEntriesWithPredictionForm.Mode formMode,
                                              EditTransferPartlistEntriesWithPredictionForm.TransferMode transferMode, String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        this.enableEditAssignedRow = ((formMode == EditTransferPartlistEntriesWithPredictionForm.Mode.EDS) || (formMode == EditTransferPartlistEntriesWithPredictionForm.Mode.CTT));
        // "Neu mit Produktauswahl" nur bei Truck und nicht bei freien SA anzeigen
        boolean isNewWithProductSelectAllowed = (((formMode == EditTransferPartlistEntriesWithPredictionForm.Mode.EDS)
                                                  || (formMode == EditTransferPartlistEntriesWithPredictionForm.Mode.CTT)
                                                  || (formMode == EditTransferPartlistEntriesWithPredictionForm.Mode.MBS)) &&
                                                 (transferMode != EditTransferPartlistEntriesWithPredictionForm.TransferMode.SA));
        PSEUDO_MUST_FIELDS = new DwList<>();
        PSEUDO_MUST_FIELDS.add(new MustFieldDescription(TABLE_PSEUDO, FIELD_PSEUDO_PRODUCT, "!!Produktnummer"));
        PSEUDO_MUST_FIELDS.add(new MustFieldDescription(TABLE_PSEUDO, FIELD_PSEUDO_KG, "!!KG"));
        PSEUDO_MUST_FIELDS.add(new MustFieldDescription(TABLE_PSEUDO, FIELD_PSEUDO_TU, "!!TU"));
        PSEUDO_MUST_FIELDS.add(new MustFieldDescription(TABLE_KATALOG, FIELD_K_POS, "!!Hotspot"));
        PSEUDO_MUST_FIELDS.add(new MustFieldDescription(TABLE_PSEUDO, FIELD_PSEUDO_TRANSFER, "!!Übernehmen"));
        INLINE_EDIT_FIELDS = new DwList<>(new String[]{ FIELD_PSEUDO_KG, FIELD_PSEUDO_TU, FIELD_K_POS });
        // Editfelder fürs Editieren festlegen
        setEditFields(new EtkEditFields());
        setNewAllowed(true);
        setDeleteAllowed(true);
        setModifyAllowed(false);
//        setEditAllowed(true);

        GuiMenuItem newWithProductMenuItem = null;
        if (isNewWithProductSelectAllowed) {
            // Zusätzlicher Toolbar Button und Kontextmenü für Neu mit Auswahl eines Produkts einhängen (von Hand, da vor Löschen und Copy Menü)
            iPartsToolbarButtonAlias newWithProduct = iPartsToolbarButtonAlias.EDIT_NEW_WITH_PRODUCT_SELECT;
            GuiToolButton newWithProductButton = new GuiToolButton(ToolButtonType.BUTTON, newWithProduct.getText(), newWithProduct.getImages());
            newWithProductButton.setTooltip(TranslationHandler.translate(newWithProduct.getTooltip()));
            newWithProductButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    doNewWithProductSelection();
                }
            });
            toolbarManager.insertButtonAfter(newWithProductButton, newWithProduct.getAlias(), EditToolbarButtonAlias.EDIT_NEW.getAlias());

            newWithProductMenuItem = toolbarHelper.createContextMenuEntry(newWithProduct, newWithProduct.getTooltip(), getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doNewWithProductSelection();
                }
            });
        }

        // Zusätzlichen Toolbar Button und Kontextmenü für Duplizieren einhängen (von Hand, da vor Löschen und Copy Menü)
        iPartsToolbarButtonAlias duplicate = iPartsToolbarButtonAlias.EDIT_DUPLICATE;
        GuiToolButton duplicateButton = new GuiToolButton(ToolButtonType.BUTTON, duplicate.getText(), duplicate.getImages());
        duplicateButton.setTooltip(TranslationHandler.translate(duplicate.getTooltip()));
        duplicateButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doDuplicate(event);
            }
        });
        // Den Duplizieren Button vor dem evtl. ausgeblendeten Löschen Button einfügen
        // und nach Neu bzw Neu mit Porduktauswahl bei Truck
        if (!isNewWithProductSelectAllowed) {
            toolbarManager.insertButtonAfter(duplicateButton, duplicate.getAlias(), EditToolbarButtonAlias.EDIT_NEW.getAlias());
        } else {
            toolbarManager.insertButtonAfter(duplicateButton, duplicate.getAlias(), EditToolbarButtonAlias.EDIT_NEW_WITH_PRODUCT_SELECT.getAlias());
        }

        GuiMenuItem duplicateMenuItem = toolbarHelper.createContextMenuEntry(duplicate, duplicate.getTooltip(), getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doDuplicate(event);
            }
        });

        GuiMenuItem kgtuSave = toolbarHelper.createMenuEntry(KGTU_COPY_ALIAS, KGTU_COPY_TEXT, null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doSaveKgTU(event);
            }
        }, getUITranslationHandler());
        GuiMenuItem kgtuInsert = toolbarHelper.createMenuEntry(KGTU_PASTE_ALIAS, KGTU_PASTE_TEXT, null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doInsertKgTu(event);
            }
        }, getUITranslationHandler());
        String msg = TranslationHandler.translate("!!Hotspot") + " " + TranslationHandler.translate("!!vereinheitlichen");
        GuiMenuItem hotspotUnify = toolbarHelper.createMenuEntry(HOTSPOT_UNIFY_ALIAS, msg, null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doUnifyHotspot(event);
            }
        }, getUITranslationHandler());

        GuiMenuItem transferUnify = toolbarHelper.createMenuEntry(TRANSFER_UNIFY_ALIAS, TRANSFER_TEXT_ALL, null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doTransferAll(event);
            }
        }, getUITranslationHandler());


        // Das Kontextmenü muss zuerst kopiert werden, damit dann der Duplzieren Eintrag vor dem Löschen Eintrag eingefügt werden kann
        List<AbstractGuiControl> originalContextmenuChildren = getTable().getContextMenu().getChildren();
        List<AbstractGuiControl> contextMenuCopy = new ArrayList<AbstractGuiControl>(originalContextmenuChildren.size());

        for (int i = 0; i < originalContextmenuChildren.size(); i++) {
            AbstractGuiControl originalChild = originalContextmenuChildren.get(i);
            if ((originalChild instanceof GuiMenuItem) && originalChild.getUserObject().equals(EditToolbarButtonAlias.EDIT_DELETE)) {
                if (isNewWithProductSelectAllowed) {
                    if (newWithProductMenuItem != null) {
                        contextMenuCopy.add(newWithProductMenuItem);
                    }
                }
                contextMenuCopy.add((duplicateMenuItem));
            }
            contextMenuCopy.add(originalChild);
            if ((originalChild instanceof GuiMenuItem) && originalChild.getUserObject().equals(EditToolbarButtonAlias.EDIT_DELETE)) {
                contextMenuCopy.add(toolbarHelper.createMenuSeparator("kgtuSeparator", getUITranslationHandler()));
                contextMenuCopy.add(kgtuSave);
                contextMenuCopy.add(kgtuInsert);
                contextMenuCopy.add(hotspotUnify);
                contextMenuCopy.add(transferUnify);
            }
        }

        getTable().getContextMenu().removeAllChildren();
        for (AbstractGuiControl guiControl : contextMenuCopy) {
            getTable().getContextMenu().addChild(guiControl);
        }

        setMaxResults(J2EEHandler.isJ2EE() ? iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
        showSearchFields(false);
        showSelectCount(false);
        showToolbar(true);

        setLabelNotFoundText("!!Es liegen keine Ergebnisse vor.");
        eventListenersActive = true;
        getGui().setName("TransferToASPredictionGrid");
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();
        getTable().setAutoResize(false);
    }

    @Override
    protected synchronized void startSearch() {
        // sonst wird bei ENTER die Suche ausgelöst
    }

    @Override
    protected RowWithAttributesForTransfer createRow(final DBDataObjectAttributes attributes) {
        RowWithAttributesForTransfer row = new RowWithAttributesForTransfer();
        row.attributes = attributes;
        return row;
    }

    protected RowWithAttributesForTransfer addCustomAttributesToGrid(DBDataObjectAttributes attributes) {
        RowWithAttributesForTransfer row = createRow(attributes);
        if (row != null) {
            getTable().addRow(row);
        }
        return row;
    }

    @Override
    protected int processResultAttributes(DBDataObjectAttributes attributes) {
        showNoResultsLabel(false, false);

        if (doValidateAttributes(attributes)) {
            addAttributesToGrid(attributes);
            return 1;
        }
        return 0;
    }

    @Override
    protected String getVisualValueOfFieldValue(String tableName, String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
        return super.getVisualValueOfFieldValue(tableName, fieldName, fieldValue, isMultiLanguage);
    }

    @Override
    protected void enableButtons() {
        int selectionRowCount = getTable().getSelectedRows().size();
        boolean isGridSelected = selectionRowCount > 0;
        toolbarHelper.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, getTable().getContextMenu());
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, getTable().getContextMenu(), isEditAllowed && (selectionRowCount == 1));
        if (toolbarHelper.isToolbarButtonVisible(EditToolbarButtonAlias.EDIT_NEW_WITH_PRODUCT_SELECT)) {
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW_WITH_PRODUCT_SELECT, getTable().getContextMenu(), isEditAllowed);
        }
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DUPLICATE, getTable().getContextMenu(), isEditAllowed && (selectionRowCount == 1));
        boolean enableDelete = false;
        if (selectionRowCount == 1) {
            enableDelete = getSelectedRowContent().isCreated();
        }
        if (enableDelete) {
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, getTable().getContextMenu(), isEditAllowed);
        } else {
            toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, getTable().getContextMenu(), false);
        }
        toolbarHelper.enableMenu(KGTU_COPY_ALIAS, getTable().getContextMenu(), selectionRowCount == 1);
        boolean kgTuInsertEnabled = (selectionRowCount >= 1) && (kgTuSave != null);
        if (kgTuInsertEnabled) {
            kgTuInsertEnabled = false;
            for (RowWithAttributesForTransfer selectedRow : getSelectedRows()) {
                if (isRowValidForPasteKgTu(selectedRow)) {
                    kgTuInsertEnabled = true;
                    break;
                }
            }
        }
        toolbarHelper.enableMenu(KGTU_PASTE_ALIAS, getTable().getContextMenu(), kgTuInsertEnabled);
        toolbarHelper.enableMenu(HOTSPOT_UNIFY_ALIAS, getTable().getContextMenu(), isGridSelected);

        int enabledCount = 0;
        AbstractGuiControl transferUnifyMenuItem = toolbarHelper.findSeparator(getTable().getContextMenu(), TRANSFER_UNIFY_ALIAS);
        if (transferUnifyMenuItem instanceof GuiMenuItem) {
            int selectedEnabledCount = 0;
            for (RowWithAttributesForTransfer selectedRow : getSelectedRows()) {
                iPartsGuiPanelWithCheckbox checkbox = selectedRow.getCheckbox();
                if (checkbox != null) {
                    if (checkbox.isEnabled()) {
                        enabledCount++;
                        if (checkbox.isSelected()) {
                            selectedEnabledCount++;
                        }
                    }
                }
            }

            if ((selectedEnabledCount == enabledCount) && (enabledCount > 0)) {
                ((GuiMenuItem)transferUnifyMenuItem).setText(TRANSFER_TEXT_NON);
                contextMenuTransferAll = false;
            } else {
                ((GuiMenuItem)transferUnifyMenuItem).setText(TRANSFER_TEXT_ALL);
                contextMenuTransferAll = true;
            }
        }
        toolbarHelper.enableMenu(TRANSFER_UNIFY_ALIAS, getTable().getContextMenu(), (enabledCount > 0));
    }

    protected void doDuplicate(Event event) {
        if (onEditChangeRecordEvent != null) {
            if (onEditChangeRecordEvent.onEditModifyRecordEvent(getConnector(), searchTable, null, null)) {

            }
        }
    }

    @Override
    protected void doNew(Event event) {
        if (onEditChangeRecordEvent != null) {
            if (onEditChangeRecordEvent.onEditCreateRecordEvent(getConnector(), searchTable, null, null)) {

            }
        }
    }

    private void doNewWithProductSelection() {
        // nur Truck-Produkte zulassen
        OnValidateAttributesEvent onValidateAttributesEvent = attributes -> {
            String docuMethodDB = attributes.getFieldValue(iPartsConst.FIELD_DP_DOCU_METHOD);
            iPartsDocumentationType docuType = iPartsDocumentationType.getFromDBValue(docuMethodDB);
            return docuType.isTruckDocumentationType();
        };

        SelectSearchGridProduct selectSearchGridProduct = new SelectSearchGridProduct(getConnector().getActiveForm());
        selectSearchGridProduct.setOnValidateAttributesEvent(onValidateAttributesEvent);
        int maxResults = SelectSearchGridProduct.getMaxSelectResultSize(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE, false);
        selectSearchGridProduct.setMaxResults(maxResults);
        // Dokumentationstyp zu den Ausgabefeldern noch hinzufügen
        EtkDisplayFields displayFields = selectSearchGridProduct.getDisplayResultFields();
        if (displayFields != null) {
            displayFields.addFeld(EtkDisplayFieldsHelper.createField(getProject(), iPartsConst.TABLE_DA_PRODUCT, iPartsConst.FIELD_DP_DOCU_METHOD, "!!Dokumentationsmethode", false, false, true));
            selectSearchGridProduct.setDisplayResultFields(displayFields);
            String product = selectSearchGridProduct.showGridSelectionDialog("");
            if (StrUtils.isValid(product)) {
                iPartsProductId productId = new iPartsProductId(product);
                onEditChangeRecordEvent.onEditCreateRecordEvent(getConnector(), searchTable, productId, null);
            }
        }
    }

    private void doSaveKgTU(Event event) {
        int selectionRowCount = getTable().getSelectedRows().size();
        if (selectionRowCount == 1) {
            RowContentForTransferToAS rowContent = getSelectedRowContent();
            String text = KGTU_COPY_TEXT;
            if (rowContent.getKgTuId().isValidId()) {
                kgTuSave = rowContent.getKgTuId();
                text = text + " (" + kgTuSave.getKg() + "/" + kgTuSave.getTu() + ")";
            } else {
                kgTuSave = null;
            }
            AbstractGuiControl guiControl = toolbarHelper.findSeparator(getTable().getContextMenu(), KGTU_COPY_ALIAS);
            if ((guiControl != null) && (guiControl instanceof GuiMenuItem)) {
                ((GuiMenuItem)guiControl).setText(text);
            }
        }
    }

    private void doInsertKgTu(Event event) {
        if (kgTuSave != null) {
            for (RowWithAttributesForTransfer selectedRow : getSelectedRows()) {
                if (isRowValidForPasteKgTu(selectedRow)) {
                    boolean kgFound = false;
                    for (KgTuListItem kgTuListItem : selectedRow.rowContent.getKGItems()) {
                        if ((kgTuListItem.getKgTuNode().getType() == KgTuType.KG) && (kgTuListItem.getKgTuNode().getId().getKg().equals(kgTuSave.getKg()))) {
                            selectedRow.kgEditor.setSelectedUserObject(kgTuListItem.getKgTuNode());
                            selectedRow.rowContent.setKgTuId(kgTuSave.getKg(), "");
                            kgFound = true;
                            break;
                        }
                    }
                    if (kgFound) {
                        for (KgTuListItem kgTuListItem : selectedRow.rowContent.getTUItems()) {
                            if ((kgTuListItem.getKgTuNode().getType() == KgTuType.TU) && (kgTuListItem.getKgTuNode().getId().getTu().equals(kgTuSave.getTu()))) {
                                selectedRow.tuEditor.setSelectedUserObject(kgTuListItem.getKgTuNode());
                                selectedRow.rowContent.setKgTuId(kgTuSave);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void doTransferAll(Event event) {
        if (contextMenuTransferAll) {
            for (RowWithAttributesForTransfer rowWithAttributesForTransfer : getSelectedRows()) {
                rowWithAttributesForTransfer.selectCheckbox(true);
            }
        } else {
            for (RowWithAttributesForTransfer rowWithAttributesForTransfer : getSelectedRows()) {
                rowWithAttributesForTransfer.selectCheckbox(false);
            }
        }
    }

    private void doUnifyHotspot(Event event) {
        DBDataObjectAttributes initialValues = new DBDataObjectAttributes();
        String initialValue = null;
        // Initialwert für K_POS aus den selektierten Zeilen bestimmen
        for (RowWithAttributesForTransfer selectedRow : getSelectedRows()) {
            if ((initialValue == null) && (StrUtils.isValid(selectedRow.rowContent.getHotspotForAttribute()))) {
                initialValue = selectedRow.rowContent.getHotspotForAttribute();
            } else {
                if (StrUtils.isValid(selectedRow.rowContent.getHotspotForAttribute()) && !initialValue.equals(selectedRow.rowContent.getHotspotForAttribute())) {
                    initialValue = "";
                    break;
                }
            }
        }
        if (initialValue == null) {
            initialValue = "";
        }
        initialValues.addField(FIELD_K_POS, initialValue, DBActionOrigin.FROM_DB);

        EtkEditFields externalEditFields = new EtkEditFields();
        EtkDisplayFields displayFields = getDisplayResultFields();
        // Suche in den DisplayFields nach FIELD_K_POS
        EtkDisplayField currentDisplayField = null;
        int hotspotColumn = -1;
        int column = 0;
        for (EtkDisplayField displayField : displayFields.getVisibleFields()) {
            if (displayField.isVisible()) {
                if (displayField.getKey().getTableName().equals(TABLE_KATALOG) && displayField.getKey().getFieldName().equals(FIELD_K_POS)) {
                    currentDisplayField = displayField;
                    hotspotColumn = column;
                    break;
                }
                column++;
            }
        }
        EtkEditField editField;
        if (currentDisplayField != null) {
            // FIELD_K_POS wird angezeigt => editField aus DisplayField zusammenbauen
            String fieldName = currentDisplayField.getKey().getFieldName();
            editField = new EtkEditField(currentDisplayField.getKey().getTableName(), fieldName, currentDisplayField.isMultiLanguage());
            editField.setArray(currentDisplayField.isArray());
        } else {
            // FIELD_K_POS wird nicht angezeigt => editField für Anzeige zusammenbauen
            editField = new EtkEditField(iPartsConst.TABLE_KATALOG, FIELD_K_POS, false);
        }
        editField.setText(new EtkMultiSprache("!!Hotspot", getProject().getConfig().getDatabaseLanguages()));
        editField.setDefaultText(false);
        externalEditFields.addField(editField);
        externalEditFields.loadStandards(getConfig());

        // Vereinheitlichen aufrufen
        DBDataObjectAttributes attributes = EditUserMultiChangeControls.showEditUserMultiChangeControls(getConnector(), externalEditFields,
                                                                                                        initialValues, null,
                                                                                                        EditUserMultiChangeControls.UnifySource.CONSTURCTION);
        if (attributes != null) {
            if (hotspotColumn != -1) {
                // Wert soll gesetzt werden und FIELD_K_POS wird angezeigt
                initialValue = attributes.getField(FIELD_K_POS).getAsString();
                for (RowWithAttributesForTransfer selectedRow : getSelectedRows()) {
                    if (selectedRow.hotspotEditor != null) {
                        selectedRow.rowContent.setHotspot(initialValue);
                        // HotSpot Wert ist editiert
                        selectedRow.rowContent.setHotspotEdited(true);
                        selectedRow.attributes.addField(FIELD_K_POS, initialValue, DBActionOrigin.FROM_DB);
                        selectedRow.hotspotEditor.setText(initialValue);
                        updateCheckbox(selectedRow, true);
                    }
                }
            }
        }
    }

    private boolean isRowValidForPasteKgTu(RowWithAttributesForTransfer row) {
        return (row.kgEditor != null) && row.rowContent.isRowContentValidForPasteKgTu(!enableEditAssignedRow);
    }

    @Override
    protected void showNoResultsLabel(boolean showNoResults, boolean showMinChar) {
        super.showNoResultsLabel(showNoResults, showMinChar);
    }

    @Override
    protected void doEditOrView(Event event) {
        // damit Doppelklick auf der Tabelle ins leere geht
    }

    private EtkDisplayFields modifyDisplayFields(EtkDisplayFields displayResultFields,
                                                 EditTransferPartlistEntriesWithPredictionForm.TransferMode transferMode) {
        // PSEUDO_MUST_FIELDS müssen vorhanden und sichtbar sein und dürfen keinen Tabellenfilter besitzen
        List<String> dbLanguages = getProject().getConfig().getDatabaseLanguages();
        for (MustFieldDescription mustFieldDescription : PSEUDO_MUST_FIELDS) {
            EtkDisplayField displayField = displayResultFields.getFeldByName(mustFieldDescription.tableName, mustFieldDescription.fieldName);
            if (displayField != null) {
                displayField.setVisible(true);
                displayField.setColumnFilterEnabled(false);
            } else {
                displayField = new EtkDisplayField(TABLE_PSEUDO, mustFieldDescription.fieldName, false, false);
                displayField.setText(new EtkMultiSprache(mustFieldDescription.description, dbLanguages));
                displayField.setDefaultText(false);
                displayField.loadStandards(getConfig());
                displayResultFields.addFeld(displayField);
            }
        }
        // Tabellenfilter für INLINE_EDIT_FIELDS ausschalten
        for (String fieldName : INLINE_EDIT_FIELDS) {
            EtkDisplayField displayField = displayResultFields.getFeldByName(TABLE_PSEUDO, fieldName);
            if (displayField != null) {
                displayField.setColumnFilterEnabled(false);
            }
        }

        EtkDisplayField saField = displayResultFields.getFeldByName(TABLE_PSEUDO, FIELD_PSEUDO_SA);
        EtkDisplayField productField = displayResultFields.getFeldByName(TABLE_PSEUDO, FIELD_PSEUDO_PRODUCT);
        if (transferMode == EditTransferPartlistEntriesWithPredictionForm.TransferMode.SA) {

            // Produktfeld falls konfiguriert entfernen
            if (productField != null) {
                productField.setVisible(false);
            }

            // KG und TU Felder auch entfernen
            EtkDisplayField kgField = displayResultFields.getFeldByName(TABLE_PSEUDO, FIELD_PSEUDO_KG);
            if (kgField != null) {
                kgField.setVisible(false);
            }
            EtkDisplayField tuField = displayResultFields.getFeldByName(TABLE_PSEUDO, FIELD_PSEUDO_TU);
            if (tuField != null) {
                tuField.setVisible(false);
            }

            //Tabellenfilter für die SAs aktivieren
            if (saField != null) {
                saField.setColumnFilterEnabled(true);
            }
        } else {
            // SA Feld falls konfiguriert entfernen
            if (saField != null) {
                saField.setVisible(false);
            }

            //Tabellenfilter für die Produkte aktivieren
            if (productField != null) {
                productField.setColumnFilterEnabled(true);
            }
        }
        return displayResultFields;
    }

    private EtkDisplayFields modifyDisplayFields(EtkDisplayFields displayResultFields) {
        return modifyDisplayFields(displayResultFields, EditTransferPartlistEntriesWithPredictionForm.TransferMode.PARTLIST);
    }

    private EtkDisplayFields getDisplayFieldsDIALOG() {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.load(getConfig(), CONFIG_KEY_PARTLIST_PREDICTION_DISPLAYFIELDS_DIALOG);
        if (displayResultFields.size() == 0) {
            displayResultFields = createDefaultDisplayFieldsDIALOG();
        }
        return modifyDisplayFields(displayResultFields);
    }

    private EtkDisplayFields createDefaultDisplayFieldsDIALOG() {
        List<String> dbLanguages = getProject().getConfig().getDatabaseLanguages();
        EtkDisplayField displayField;
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_PRODUCT, false, false);
        displayField.setText(new EtkMultiSprache("!!Produktnummer", dbLanguages));
        displayField.setDefaultText(false);
        displayField.setColumnFilterEnabled(true);
        displayFields.addFeld(displayField);
        // AA
        displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, false, false);
        displayField.setText(new EtkMultiSprache("!!AA", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);

        // Aufschlüsselung des DIALOG Felder
        displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE, false, false);
        displayField.setText(new EtkMultiSprache("!!POS", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);

        displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV, false, false);
        displayField.setText(new EtkMultiSprache("!!PV", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);

        displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_WW, false, false);
        displayField.setText(new EtkMultiSprache("!!WW", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);

        displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETZ, false, false);
        displayField.setText(new EtkMultiSprache("!!ETZ", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);

        displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_STEERING, false, false);
        displayField.setText(new EtkMultiSprache("!!LK", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);

        displayField = new EtkDisplayField(TABLE_KATALOG, FIELD_K_MATNR, false, false);
        displayFields.addFeld(displayField);

        displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA, false, false);
        displayField.setText(new EtkMultiSprache("!!KEM Datum ab", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);

        // KG (edit)
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_KG, false, false);
        displayField.setWidth(50);
        displayField.setText(new EtkMultiSprache("!!KG", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        // TU (edit)
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_TU, false, false);
        displayField.setWidth(50);
        displayField.setText(new EtkMultiSprache("!!TU", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        // Hotspot (edit)
        displayField = new EtkDisplayField(TABLE_KATALOG, FIELD_K_POS, false, false);
        displayField.setText(new EtkMultiSprache("!!Hotspot", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        // zugeordnet
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_ASSIGNED, false, false);
        displayField.setText(new EtkMultiSprache("!!Zugeordnet", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        // übernehmen checkbox
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_TRANSFER, false, false);
        displayField.setText(new EtkMultiSprache("!!Übernehmen", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        displayField.setGrowColumn(true);

        // AA
        displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE, false, false);
//        displayField.setText(new EtkMultiSprache("!!AA*", dbLanguages));
//        displayField.setDefaultText(false);
        displayField.setColumnFilterEnabled(true);
        displayFields.addFeld(displayField);

        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    private EtkDisplayFields getDisplayFieldsEDSandMBS(boolean isMBS, EditTransferPartlistEntriesWithPredictionForm.TransferMode transferMode) {
        String configKey;
        if (isMBS) {
            configKey = CONFIG_KEY_PARTLIST_PREDICTION_DISPLAYFIELDS_MBS;
        } else {
            configKey = CONFIG_KEY_PARTLIST_PREDICTION_DISPLAYFIELDS_EDS;
        }
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.load(getConfig(), configKey);
        if (displayResultFields.size() == 0) {
            displayResultFields = createDefaultDisplayFieldsEDSandMBS(isMBS, transferMode);
        }
        return modifyDisplayFields(displayResultFields, transferMode);
    }

    private EtkDisplayFields createDefaultDisplayFieldsEDSandMBS(boolean isMBS,
                                                                 EditTransferPartlistEntriesWithPredictionForm.TransferMode transferMode) {
        List<String> dbLanguages = getProject().getConfig().getDatabaseLanguages();
        EtkDisplayField displayField;
        EtkDisplayFields displayFields = new EtkDisplayFields();
        if (transferMode == EditTransferPartlistEntriesWithPredictionForm.TransferMode.SA) {
            displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_SA, false, false);
            displayField.setText(new EtkMultiSprache("!!SA-Modul", dbLanguages));
            displayField.setDefaultText(false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
        } else {
            displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_PRODUCT, false, false);
            displayField.setText(new EtkMultiSprache("!!Produktnummer", dbLanguages));
            displayField.setDefaultText(false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
        }

        // Teilenummer
        displayField = new EtkDisplayField(TABLE_KATALOG, FIELD_K_MATNR, false, false);
        displayFields.addFeld(displayField);

        if (isMBS) {
            // Benennung (MBS only)
            displayField = new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false);
            displayFields.addFeld(displayField);
        } else {
            // KEM ab (EDS only)
            displayField = new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.EDS_KEMFROM, false, false);
            displayField.setText(new EtkMultiSprache("!!KEM ab", dbLanguages));
            displayField.setDefaultText(false);
            displayFields.addFeld(displayField);
        }

        if (transferMode == EditTransferPartlistEntriesWithPredictionForm.TransferMode.PARTLIST) {
            // KG (edit)
            displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_KG, false, false);
            displayField.setWidth(50);
            displayField.setText(new EtkMultiSprache("!!KG", dbLanguages));
            displayField.setDefaultText(false);
            displayFields.addFeld(displayField);
            // TU (edit)
            displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_TU, false, false);
            displayField.setWidth(50);
            displayField.setText(new EtkMultiSprache("!!TU", dbLanguages));
            displayField.setDefaultText(false);
            displayFields.addFeld(displayField);
        }
        // Hotspot (edit)
        displayField = new EtkDisplayField(TABLE_KATALOG, FIELD_K_POS, false, false);
        displayField.setText(new EtkMultiSprache("!!Hotspot", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        // zugeordnet
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_ASSIGNED, false, false);
        displayField.setText(new EtkMultiSprache("!!Zugeordnet", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        // übernehmen checkbox
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_TRANSFER, false, false);
        displayField.setText(new EtkMultiSprache("!!Übernehmen", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        displayField.setGrowColumn(true);

        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    public void initDIALOG() {
        EtkDisplayFields displayFields = getDisplayFieldsDIALOG();
        setDisplayResultFields(displayFields, true);
        tooltip_noKGTU = TranslationHandler.translate("!!Es konnten keine passenden KG/TU Werte ermittelt werden. AS-Produktklasse und Aggregatetyp am Produkt prüfen.");
        init();
    }

    public void initSaaPartsListAndMBS(boolean isMBS, EditTransferPartlistEntriesWithPredictionForm.TransferMode transferMode) {
        if (transferMode == EditTransferPartlistEntriesWithPredictionForm.TransferMode.SA) {
            PSEUDO_MUST_FIELDS.clear();
            INLINE_EDIT_FIELDS.clear();
            PSEUDO_MUST_FIELDS.add(new MustFieldDescription(TABLE_PSEUDO, FIELD_PSEUDO_SA, "!!SA-Modul"));
            PSEUDO_MUST_FIELDS.add(new MustFieldDescription(TABLE_KATALOG, FIELD_K_POS, "!!Hotspot"));
            PSEUDO_MUST_FIELDS.add(new MustFieldDescription(TABLE_PSEUDO, FIELD_PSEUDO_TRANSFER, "!!Übernehmen"));
            INLINE_EDIT_FIELDS = new DwList<>(new String[]{ FIELD_K_POS });
        }
        EtkDisplayFields displayFields = getDisplayFieldsEDSandMBS(isMBS, transferMode);
        setDisplayResultFields(displayFields, true);
        tooltip_noKGTU = TranslationHandler.translate("!!Es konnten keine passenden KG/TU Werte ermittelt werden.");
        init();
    }

    public void init() {
        tooltip_Assigned = null;
        EtkDisplayField assignedDisplayField = displayResultFields.getFeldByName(TABLE_PSEUDO, FIELD_PSEUDO_ASSIGNED, false);
        if (assignedDisplayField != null) {
            tooltip_Assigned = assignedDisplayField.getText().getTextByNearestLanguage(getProject().getDBLanguage(), getProject().getDataBaseFallbackLanguages());
        }
        filterFactory = new SimpleMasterDataSearchFilterFactoryPrediction(getProject());
        setColumnFilterFactory(filterFactory);
        getTable().setColumnFilterFactory(filterFactory);
        RComboBox<KgTuListItem> dummyCombobox = new RComboBox<KgTuListItem>();
        defaultLabelHeight = dummyCombobox.getPreferredHeight();
    }

    public void switchOffEventListeners() {
        getTable().switchOffEventListeners();
        eventListenersActive = false;
    }

    public void switchOnEventListeners() {
        getTable().switchOnEventListeners();
        eventListenersActive = true;
    }

    private GuiPanel createTextCell(String text) {
        GuiPanel panel = new GuiPanel(new LayoutGridBag(false));
        panel.setMinimumHeight(defaultLabelHeight);
        panel.setBackgroundColor(Colors.clTransparent.getColor());
        GuiLabel label = new GuiLabel(text);
        label.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 1, LayoutGridBag.ANCHOR_CENTER, LayoutGridBag.FILL_HORIZONTAL, 0, 2, 0, 0));
        panel.addChild(label);
        return panel;
    }

    public void addToGrid(final RowContentForTransferToAS rowContent) {
        if ((rowContent != null) && ((rowContent.getProductId() != null) || (rowContent.getSaModuleNumber() != null))) {
            final DBDataObjectAttributes attributes = rowContent.getAsAttributes(getProject());
            final RowWithAttributesForTransfer newRow = new RowWithAttributesForTransfer();
            newRow.attributes = attributes;
            newRow.rowContent = rowContent;

            String noMasterDataText = "";
            for (EtkDisplayField field : displayResultFields.getVisibleFields()) {
                if (field.getKey().getTableName().equals(TABLE_MAT)) {
                    DBDataObjectAttribute attribute = attributes.getField(field.getKey().getFieldName(), false);
                    if (attribute == null) {
                        noMasterDataText = TranslationHandler.translate("!!Keine Stammdaten zur Teilenummer \"%1\" vorhanden",
                                                                        getVisObject().asString(TABLE_MAT, FIELD_M_BESTNR,
                                                                                                rowContent.getSelectedPartlistEntry().getFieldValue(FIELD_K_MATNR),
                                                                                                getProject().getDBLanguage()));
                        break;
                    }
                }
            }
            boolean isEditable = rowContent.isEditable();
            int index = 0;
            for (EtkDisplayField field : displayResultFields.getVisibleFields()) {
                String tableName = field.getKey().getTableName();
                String fieldName = field.getKey().getFieldName();

                AbstractGuiControl control = null;
                boolean showNoMasterDataTooltip = false;
                // Edit Felder
                if (isEditable && INLINE_EDIT_FIELDS.contains(fieldName)) {
                    if (fieldName.equals(FIELD_K_POS)) {

                        newRow.hotspotEditor = new iPartsGuiHotSpotTextField(rowContent.getHotspotForAttribute(), true);
                        newRow.hotspotEditor.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                            @Override
                            public void fire(Event event) {
                                if (eventListenersActive) {
                                    if (!rowContent.getHotspotForAttribute().equals(newRow.hotspotEditor.getTrimmedText())) {
                                        rowContent.setHotspot(newRow.hotspotEditor.getTrimmedText());
                                        rowContent.setHotspotEdited(true);
                                        updateCheckbox(newRow, true);
                                    }
                                }
                            }
                        });
                        control = newRow.hotspotEditor;

                    } else if (fieldName.equals(FIELD_PSEUDO_KG)) {
                        if (rowContent.hasKGItems()) {
                            RComboBox<KgTuNode> kgCombobox = createCombobox(newRow, true);
                            control = kgCombobox;
                        } else {
                            control = createTextCell("");
                            control.setTooltip(tooltip_noKGTU);
                        }
                    } else if (fieldName.equals(FIELD_PSEUDO_TU)) {
                        if (rowContent.hasKGItems()) {
                            RComboBox<KgTuNode> tuCombobox = createCombobox(newRow, false);
                            control = tuCombobox;
                        } else {
                            control = createTextCell("");
                            control.setTooltip(tooltip_noKGTU);
                        }
                    }
                } else {
                    // Sonderfall Checkbox
                    if (fieldName.equals(FIELD_PSEUDO_TRANSFER)) {
                        // Setze die Checkbox an der vorgegebene Stelle
                        OnChangeEvent changeEvent = new OnChangeEvent() {
                            @Override
                            public void onChange() {
                                if (eventListenersActive) {
                                    newRow.updateTransferMarkFromGUI();
//                                    onEditChangeRecordEvent.onEditModifyRecordEvent(getConnector(), tableName, null, attributes);
                                    // bewirkt enableButtons() im ParentForm
                                    parentForm.updateData(null, true);
                                }
                            }
                        };
                        final iPartsGuiPanelWithCheckbox panelWithCheckbox = new iPartsGuiPanelWithCheckbox(false, changeEvent);
                        panelWithCheckbox.addEventListener(new EventListener(Event.KEY_TYPED_EVENT) {
                            @Override
                            public void fire(Event event) {
                                int keyCode = event.getIntParameter(Event.EVENT_PARAMETER_KEY_CODE);
                                if ((keyCode == KeyEvent.VK_ENTER)) {
                                    // toggle
                                    panelWithCheckbox.setSelected(!panelWithCheckbox.isSelected());
                                }
                            }
                        });
                        boolean transferValue = rowContent.getTransferMark();
                        panelWithCheckbox.setEnabled(!transferValue);
                        panelWithCheckbox.setSelected(transferValue);
                        newRow.checkBoxIndex = index;
                        control = panelWithCheckbox;
                    } else {
                        // Alle NICHT Edit Felder
                        DBDataObjectAttribute attribute = attributes.getField(fieldName, false);
                        String visTableName = tableName;
                        String visFieldName = fieldName;
                        if (tableName.equals(TABLE_MAT) && (attribute == null)) {
                            showNoMasterDataTooltip = true;

                            // Es existieren keine Teilestammdaten zur Stücklistenposition --> Tooltip für Spalten anzeigen,
                            // die auf Teilestamm zugreifen
                            if (fieldName.equals(FIELD_M_MATNR) || fieldName.equals(FIELD_M_BESTNR)) {
                                attribute = rowContent.getSelectedPartlistEntry().getAttribute(FIELD_K_MATNR);
                                visTableName = TABLE_KATALOG;
                                visFieldName = FIELD_K_MATNR;
                            }
                        }
                        String value = getVisualValueOfFieldValue(visTableName, visFieldName, attribute, field.isMultiLanguage());
                        control = createTextCell(value);
                    }
                }

                if (control == null) {
                    control = createTextCell("");
                }
                // Tooltips setzen
                if (fieldName.equals(FIELD_K_POS) || fieldName.equals(FIELD_PSEUDO_KG) || fieldName.equals(FIELD_PSEUDO_TU)) {
                    if ((fieldName.equals(FIELD_PSEUDO_KG) || fieldName.equals(FIELD_PSEUDO_TU)) && !rowContent.hasKGItems()) {
                        control.setTooltip(tooltip_noKGTU);
                    } else {
                        DBDataObjectAttribute assignedField = attributes.getField(FIELD_PSEUDO_ASSIGNED);
                        if ((assignedField != null) && (tooltip_Assigned != null)) {
                            if (!(control instanceof RComboBox)) {
                                control.setTooltip(createTooltipForPosField(rowContent, tooltip_Assigned + ": " + assignedField.getAsString()));
                            }
                        }
                    }
                }
                if (showNoMasterDataTooltip) {
                    control.setTooltip(noMasterDataText);
                }
                final AbstractGuiControl controlFinal = control;
                newRow.addChild(controlFinal, () -> controlFinal.getTextRepresentation(true));
                index++;
            }
            updateCheckbox(newRow, false);
            getTable().addRow(newRow);
        }
    }

    private String createTooltipForPosField(RowContentForTransferToAS rowContent, String starterText) {
        StringBuilder str = new StringBuilder(starterText);
        if (!rowContent.isAlternateKgTuListValid()) {
            if (rowContent.getFallBackProductId() != null) {
                if (str.length() > 0) {
                    str.append(OsUtils.NEWLINE);
                }
                if (rowContent.getFallBackProductId() != null) {
                    str.append(TranslationHandler.translate("!!KG/TU-Vorschlag wurde aus Product \"%1\" übernommen.",
                                                            rowContent.getFallBackProductId().getProductNumber()));
                }
            }
            return str.toString();
        }
        int moduloValue = 4;  // 4 KG/TU Einträge pro Zeile
        int maxLines = 4;     // maximale Anzahl von Zeilen
        if (rowContent.getFallBackProductId() != null) {
            if (str.length() > 0) {
                str.append(OsUtils.NEWLINE);
            }
            str.append(TranslationHandler.translate("!!KG/TU-Vorschlag wurde aus Product \"%1\" übernommen.",
                                                    rowContent.getFallBackProductId().getProductNumber()));
            str.append(OsUtils.NEWLINE);
        }
        int count = 0;
        int maxCount = rowContent.getAlternativeKgTuList().size();
        if (str.length() > 0) {
            str.append(OsUtils.NEWLINE);
        }
        String msg = "!!Weitere KG/TU Vorschläge:";
        if (maxCount == 1) {
            msg = "!!Weiterer KG/TU Vorschlag:";
        }
        str.append(TranslationHandler.translate(msg));
        str.append(OsUtils.NEWLINE);
        str.append("  ");
        for (KgTuId kgTuId : rowContent.getAlternativeKgTuList()) {
            str.append(kgTuId.toString("/"));
            count++;
            if (count >= maxLines * moduloValue) {
                if (maxCount > maxLines * moduloValue) {
                    str.append("...");
                }
                break;
            }
            if ((count % moduloValue) == 0) {
                str.append(OsUtils.NEWLINE);
                str.append("  ");
            } else {
                if (count < maxCount) {
                    str.append(", ");
                }
            }
        }
        return str.toString();
    }

    public RComboBox<KgTuNode> createCombobox(final RowWithAttributesForTransfer row, final boolean isKG) {
        final RowContentForTransferToAS rowContent = row.rowContent;
        final RComboBox<KgTuNode> comboBox = new RComboBox<KgTuNode>(RComboBox.Mode.STANDARD);
        if (isKG) {
            row.kgEditor = comboBox;
            updateKgCombobox(row, false);
        } else {
            row.tuEditor = comboBox;
            updateTuCombobox(row, false);
        }
        comboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                if (eventListenersActive) {
                    KgTuNode selectedNode = comboBox.getSelectedUserObject();
                    // nur den KG oder TU Wert setzen. Dadurch könnten eventuell temporär inkonsistente KgTuIds entstehen.
                    // Diese werden dann beim Beenden des Dialogs nochmal geprüft und ggf. korrigiert
                    if (selectedNode != null) {
                        if (isKG) {
                            rowContent.setKgTuId(selectedNode.getId().getKg(), rowContent.getKgTuId().getTu());
                            updateTuCombobox(row, true);
                        } else {
                            rowContent.setKgTuId(rowContent.getKgTuId().getKg(), selectedNode.getId().getTu());
                        }
                        clearHotspot(row);
                        updateCheckbox(row, true);
                    }
                }
            }
        });
        return comboBox;
    }

    public void updateTuCombobox(RowWithAttributesForTransfer row, boolean clearSelection) {
        if ((row != null) && (row.rowContent != null) && (row.tuEditor != null)) {
            row.tuEditor.removeAllItems();
            for (KgTuListItem kgTuListItem : row.rowContent.getTUItems()) {
                if (kgTuListItem.getKgTuNode().getType() == KgTuType.TU) {
                    String text = KgTuHelper.buildKgTuComboText(kgTuListItem, getProject().getViewerLanguage(), getProject().getDataBaseFallbackLanguages());
                    row.tuEditor.addItem(kgTuListItem.getKgTuNode(), text);
                }
            }

            if (clearSelection) {
                if (row.tuEditor.getItemCount() == 1) {
                    row.tuEditor.setSelectedIndex(0);
                    row.rowContent.setKgTuId(row.rowContent.getKgTuId().getKg(), row.tuEditor.getSelectedUserObject().getId().getTu());
                } else {
                    row.tuEditor.setSelectedIndex(-1);
                    row.rowContent.setKgTuId(row.rowContent.getKgTuId().getKg(), "");
                }
            } else {
                String tu = row.rowContent.getKgTuId().getTu();
                for (int i = 0; i < row.tuEditor.getItemCount(); i++) {
                    if (row.tuEditor.getUserObject(i).getId().getTu().equals(tu)) {
                        row.tuEditor.setSelectedIndex(i);
                        return;
                    }
                }

                // Keine Selektion gefunden -> auf -1 setzen, da ansonsten der erste Eintrag selektiert wäre
                row.tuEditor.setSelectedIndex(-1);
            }
        }
    }

    public void updateKgCombobox(RowWithAttributesForTransfer row, boolean clearSelection) {
        if ((row != null) && (row.rowContent != null) && (row.kgEditor != null)) {
            row.kgEditor.removeAllItems();
            for (KgTuListItem kgTuListItem : row.rowContent.getKGItems()) {
                if (kgTuListItem.getKgTuNode().getType() == KgTuType.KG) {
                    String text = KgTuHelper.buildKgTuComboText(kgTuListItem, getProject().getViewerLanguage(), getProject().getDataBaseFallbackLanguages());
                    row.kgEditor.addItem(kgTuListItem.getKgTuNode(), text);
                }
            }
            if (clearSelection) {
                row.kgEditor.setSelectedIndex(-1);
                row.rowContent.setKgTuId("", "");
            } else {
                String kg = row.rowContent.getKgTuId().getKg();
                for (int i = 0; i < row.kgEditor.getItemCount(); i++) {
                    if (row.kgEditor.getUserObject(i).getId().getKg().equals(kg)) {
                        row.kgEditor.setSelectedIndex(i);
                        return;
                    }
                }

                // Keine Selektion gefunden -> auf -1 setzen, da ansonsten der erste Eintrag selektiert wäre
                row.kgEditor.setSelectedIndex(-1);
            }
        }
    }

    public void updateCheckbox(RowWithAttributesForTransfer row, boolean setSelected) {
        if ((row != null) && (row.rowContent != null)) {
            boolean enabled = row.rowContent.isEditable();
            if (enabled) {
                if ((row.rowContent.getProductId() != null) && (row.rowContent.getKgTuId() != null)) {
                    enabled = row.rowContent.isKGTUValidForTransfer();
                }
            }
            if (enabled && setSelected) {
                row.setCheckbox(true, true);
            } else {
                row.enableCheckbox(enabled);
            }
        }
    }

    public void clearHotspot(RowWithAttributesForTransfer row) {
        // Der Hotspot darf nur zurückgesetzt werden wenn der Benutzer ihn nicht zuvor schon bearbeitet hat
        if ((row != null) && (row.rowContent != null) && !row.rowContent.isHotspotEdited() &&
            row.rowContent.isEditable() && (row.hotspotEditor != null)) {
            row.rowContent.setHotspot("");
            row.hotspotEditor.setText("");
        }
    }

    public List<RowContentForTransferToAS> getAllItems() {
        List<RowContentForTransferToAS> allRows = new DwList<RowContentForTransferToAS>();
        for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes rowWithAttributes : getEntries()) {
            if (rowWithAttributes instanceof RowWithAttributesForTransfer) {
                RowContentForTransferToAS rowContent = ((RowWithAttributesForTransfer)rowWithAttributes).rowContent;
                if (rowContent != null) {
                    allRows.add(rowContent);
                }
            }
        }
        return allRows;
    }

    public List<RowContentForTransferToAS> getAllVisibleItems() {
        List<RowContentForTransferToAS> allRows = new DwList<RowContentForTransferToAS>();
        for (int i = 0; i < getTable().getRowCount(); i++) {
            RowContentForTransferToAS rowContent = getRowContent(i);
            if (rowContent != null) {
                allRows.add(rowContent);
            }
        }
        return allRows;
    }

    /**
     * Alle sichtbaren Einträge die zum Übernehmen markiert sind (Checkbox) ermitteln.
     * Hier sind die aktuell ausgefilterten Einträge nicht enthalten
     *
     * @return Den RowContent aller aktuell sichtbaren Zeilen bei denen die Checkbox angehakt ist
     */
    public List<RowContentForTransferToAS> getAllVisibleTransferItems() {
        List<RowContentForTransferToAS> selectedRows = new ArrayList<RowContentForTransferToAS>();
        for (int i = 0; i < getTable().getRowCount(); i++) {
            GuiTableRow row = getTable().getRow(i);
            if ((row instanceof RowWithAttributesForTransfer) && (((RowWithAttributesForTransfer)row).rowContent != null)) {
                if (((RowWithAttributesForTransfer)row).rowContent.getTransferMark()) {
                    selectedRows.add(((RowWithAttributesForTransfer)row).rowContent);
                }
            }
        }
        return selectedRows;
    }

    /**
     * Alle Einträge die zum Übernehmen markiert sind (Checkbox) ermitteln.
     * Hier werden auch aktuell ausgefilterte Einträge berücksichtigt
     *
     * @return Den RowContent aller Zeilen bei denen die Checkbox angehakt ist
     */
    public List<TransferToASElement> getAllTransferItems() {
        List<TransferToASElement> selectedRows = new ArrayList<>();
        for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row : getEntries()) {
            if ((row instanceof RowWithAttributesForTransfer)) {
                RowContentForTransferToAS rowContent = ((RowWithAttributesForTransfer)row).rowContent;
                if ((rowContent != null) && rowContent.getTransferMark() && (rowContent.getTransferElement() != null)) {
                    selectedRows.add(rowContent.getTransferElement());
                }
            }
        }
        return selectedRows;
    }


//    /**
//     * Alle Einträge die zum Übernehmen markiert sind (Checkbox) ermitteln.
//     * Hier werden auch aktuell ausgefilterte Einträge berücksichtigt
//     *
//     * @return Den RowContent aller Zeilen bei denen die Checkbox angehakt ist
//     */
//    public List<RowContent> getAllTransferItems() {
//        List<RowContent> selectedRows = new ArrayList<RowContent>();
//        for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row : getEntries()) {
//            if ((row instanceof RowWithAttributesForTransfer) && (((RowWithAttributesForTransfer)row).rowContent != null)) {
//                if (((RowWithAttributesForTransfer)row).rowContent.getTransferMark()) {
//                    selectedRows.add(((RowWithAttributesForTransfer)row).rowContent);
//                }
//            }
//        }
//        return selectedRows;
//    }

    private RowContentForTransferToAS getRowContent(int rowIndex) {
        GuiTableRow row = getTable().getRow(rowIndex);
        if (row instanceof RowWithAttributesForTransfer) {
            return ((RowWithAttributesForTransfer)row).rowContent;
        }
        return null;
    }

    public RowContentForTransferToAS getSelectedRowContent() {
        GuiTableRow selectedRow = getTable().getSelectedRow();
        if ((selectedRow != null) && (selectedRow instanceof RowWithAttributesForTransfer)) {
            return ((RowWithAttributesForTransfer)selectedRow).rowContent;
        }
        return null;
    }

    public List<RowWithAttributesForTransfer> getSelectedRows() {
        List<RowWithAttributesForTransfer> result = new DwList<RowWithAttributesForTransfer>();
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();
        for (GuiTableRow selectedRow : selectedRows) {
            if (selectedRow instanceof RowWithAttributesForTransfer) {
                result.add((RowWithAttributesForTransfer)selectedRow);
            }
        }
        return result;
    }

    static class RowWithAttributesForTransfer extends SimpleSelectSearchResultGrid.GuiTableRowWithAttributes {

        protected RowContentForTransferToAS rowContent;
        protected int checkBoxIndex;
        protected RComboBox<KgTuNode> kgEditor;
        protected RComboBox<KgTuNode> tuEditor;
        protected GuiTextField hotspotEditor;

        public RowWithAttributesForTransfer() {
            super();
            rowContent = new RowContentForTransferToAS();
        }

//        public RowWithAttributesForTransfer(iPartsDialogBCTEPrimaryKey bctePrimaryKey, EtkDataPartListEntry selectedPartlistEntry,
//                                            iPartsProduct product, List<iPartsProduct> productList) {
//            this();
//            rowContent = new RowContent(bctePrimaryKey, selectedPartlistEntry, product, productList);
//        }

        public void setContent(RowContentForTransferToAS content) {
            rowContent = content;
        }

        public void enableCheckbox(boolean enabled) {
            iPartsGuiPanelWithCheckbox checkbox = getCheckbox();
            if (checkbox != null) {
                checkbox.setEnabled(enabled);
                if (!enabled && checkbox.isSelected()) {
                    if (rowContent != null) {
                        rowContent.setTransferMark(false);
                    }
                    checkbox.setSelected(false);
                }
            }
        }

        public void setCheckbox(boolean selected, boolean enabled) {
            iPartsGuiPanelWithCheckbox checkbox = getCheckbox();
            if (checkbox != null) {
                if (rowContent != null) {
                    rowContent.setTransferMark(selected);
                }
                checkbox.setSelected(selected);
                checkbox.setEnabled(enabled);
            }
        }

        public void selectCheckbox(boolean selected) {
            iPartsGuiPanelWithCheckbox checkbox = getCheckbox();
            if (checkbox != null) {
                if (selected && checkbox.isEnabled()) {
                    checkbox.setSelected(true);
                } else {
                    checkbox.setSelected(false);
                }
            }
        }

        public void updateTransferMarkFromGUI() {
            if ((checkBoxIndex > -1) && (rowContent != null)) {
                Object childForColumn = getChildForColumn(checkBoxIndex);
                if (childForColumn instanceof iPartsGuiPanelWithCheckbox) {
                    iPartsGuiPanelWithCheckbox checkbox = (iPartsGuiPanelWithCheckbox)childForColumn;
                    rowContent.setTransferMark(checkbox.isSelected());
                }
            }
        }

        public iPartsGuiPanelWithCheckbox getCheckbox() {
            if ((checkBoxIndex > -1) && (rowContent != null)) {
                Object childForColumn = getChildForColumn(checkBoxIndex);
                if (childForColumn instanceof iPartsGuiPanelWithCheckbox) {
                    return (iPartsGuiPanelWithCheckbox)childForColumn;
                }
            }
            return null;
        }
    }

    public AbstractGuiTableColumnFilterFactory storeFilterFactory(Map<Integer, Object> columnFilterValuesMap) {

        SimpleMasterDataSearchFilterFactoryPrediction copyColumnFilterFactory = null;

        if ((columnFilterValuesMap != null) && !filterFactory.getColumnFilterEditControlsMap().isEmpty()) {
            columnFilterValuesMap.clear();
            columnFilterValuesMap.putAll(filterFactory.getColumnFilterEditControlsMap());
            copyColumnFilterFactory = new SimpleMasterDataSearchFilterFactoryPrediction(null);
            copyColumnFilterFactory.assign(filterFactory);
        }
        return copyColumnFilterFactory;
    }

    public void restoreFilterFactory(AbstractGuiTableColumnFilterFactory copyColumnFilterFactory, Map<Integer, Object> columnFilterValuesMap) {
        if ((copyColumnFilterFactory != null) && (columnFilterValuesMap != null) && (copyColumnFilterFactory instanceof SimpleMasterDataSearchFilterFactoryPrediction)) {
            filterFactory.assign(copyColumnFilterFactory);
            for (Map.Entry<Integer, Object> entry : columnFilterValuesMap.entrySet()) {
                if (entry.getValue() instanceof EditControlFactory) {
                    EditControlFactory controlFactory = (EditControlFactory)entry.getValue();
                    getTable().setFilterValueForColumn(entry.getKey(), controlFactory.getControl(), false, true);
                }
            }
            filterFactory.doFilterTableEntries();
        }
    }


    private class SimpleMasterDataSearchFilterFactoryPrediction extends SimpleMasterDataSearchFilterFactory {

        public SimpleMasterDataSearchFilterFactoryPrediction(EtkProject project) {
            super(project);
        }

        @Override
        public AbstractGuiControl createFilterControl(int column, Object currentFilterValue) {
            if (currentFilterValue != null) {
                return super.createFilterControl(column, currentFilterValue);
            }
            int counter = 0;
            int realColumn = column - doCalculateStartColumnIndexForDisplayFields();
            for (EtkDisplayField field : getDisplayFieldList()) {
                if (!field.isVisible()) {
                    realColumn++;
                }
                counter++;
                if (counter > realColumn) {
                    break;
                }
            }
            EtkDisplayField field = getDisplayFieldList().get(realColumn);
            if (!field.getKey().getFieldName().equals(FIELD_PSEUDO_PRODUCT)) {
                return super.createFilterControl(column, currentFilterValue);
            }
            String columnTableAndFieldName = TableAndFieldName.make(field.getKey().getName(), field.isUsageField());
            String filterName = getFilterName(column, columnTableAndFieldName);
            getAssemblyListFilter().setLoaded(true);
            if (getFilterTypByName(filterName) == null) {
                EtkFilterTyp filterTyp = buildInternalFilter(filterName, columnTableAndFieldName);
                getAssemblyListFilter().getFilterList().add(filterTyp);
            }
            EditControlFactory editControl = EditControlFactory.createForTableColumnFilter(getProject(),
                                                                                           TableAndFieldName.getTableName(columnTableAndFieldName),
                                                                                           TableAndFieldName.getFieldName(columnTableAndFieldName),
                                                                                           getProject().getDBLanguage(), TranslationHandler.getUiLanguage(),
                                                                                           "", null, false);
            editControl.setUsageField(field.isUsageField());
            EnumCheckRComboBox rEnumComboBox = new EnumCheckRComboBox();
            rEnumComboBox.setParseAsSetOfEnum(false);
            rEnumComboBox.setIgnoreBlankTexts(false);
            rEnumComboBox.setName(columnTableAndFieldName);

            List<RowContentForTransferToAS> allItems = getAllItems();
            Set<String> productNames = new LinkedHashSet<String>();
            for (RowContentForTransferToAS rowContent : allItems) {
                productNames.add(rowContent.getProductId().getProductNumber());
            }
            rEnumComboBox.setEnumTexte(new DwList<String>(productNames));
            if (rEnumComboBox.getActToken().isEmpty()) {
                rEnumComboBox.removeItem(0);
                rEnumComboBox.setSelectedIndex(-1);
            }
            editControl.setControl(rEnumComboBox);
            editControl.setWidth(200);
            getColumnFilterEditControlsMap().put(column, editControl);
            //Filtere die Einträge die bereits global gefiltert sind raus
            return filterItems(editControl, TableAndFieldName.getTableName(columnTableAndFieldName),
                               TableAndFieldName.getFieldName(columnTableAndFieldName)).getControl();

        }

        @Override
        protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> calculateFilteredList() {
            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries;
            if (getAssemblyListFilter().isDBFilterActive()) {
                entries = new DwList<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes>();
                String language = getProject().getViewerLanguage();
                //filtern
                for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes entry : getEntries()) {
                    if (checkExtraFilter(searchTable, entry.attributes, language)) {
                        entries.add(entry); // Eintrag wurde nicht ausgefiltert
                    }
                }
            } else {
                entries = new DwList<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes>(getEntries());
            }
            return entries;
        }

        protected boolean checkExtraFilter(String tableName, DBDataObjectAttributes attributes, String language) {
            if (!getAssemblyListFilter().isDBFilterActive()) {
                return true; // Eintrag wurde nicht ausgefiltert
            }
            // Liste der TabellenNamen, die gefiltert werden sollen bestimmen
            // und feststellen, ob der spezielle Produkt-Filter aktiv ist
            Set<String> filterTableNames = new HashSet<String>();
            EtkFilterItem pseudoProductFilterItem = null;
            EtkFilterTyp pseudoProductFilterTyp = null;
            if (getAssemblyListFilter().isTableFilter()) {
                Collection<EtkFilterTyp> activeFilter = getAssemblyListFilter().getActiveFilter();
                for (EtkFilterTyp filterTyp : activeFilter) {
                    if (filterTyp.getFilterTypus() == EtkFilterTyp.FilterTypus.GRIDFILTER) {
                        for (EtkFilterItem filterItem : filterTyp.getFilterItems()) {
                            filterTableNames.add(filterItem.getTableName());
                            if (filterItem.getLowFieldName().equals(FIELD_PSEUDO_PRODUCT)) {
                                pseudoProductFilterItem = filterItem;
                                pseudoProductFilterTyp = filterTyp;
                            }
                        }
                    }
                }
            }

            if (pseudoProductFilterItem != null) {
                // Spezial-Filter für Produkt-Spalte
                Collection<EtkFilterTyp> activeFilter = getAssemblyListFilter().getActiveFilter();
                // Produkt-Filter inaktiv setzen
                pseudoProductFilterTyp.setActive(false);
                // die aktuelle Pseudo-Tabelle aus der Tabellenliste löschen
                filterTableNames.remove(tableName);

                //Filterung für Tabellenfilter
                for (EtkFilterTyp filterTyp : activeFilter) {
                    if (filterTyp.getFilterTypus() == EtkFilterTyp.FilterTypus.GRIDFILTER) {
                        EtkFilterItems filterItems = filterTyp.getFilterItems().getItemsByTable(tableName);
                        if (!filterItems.isEmpty()) {
                            if (filterTyp == pseudoProductFilterTyp) {
                                // hier der Spezial-Filter für Produkte
                                List<String> filterValues = filterTyp.getFilterValues();
                                if (!filterValues.isEmpty()) {
                                    String realValue = attributes.getField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_PRODUCT).getAsString();
                                    List<String> soeValues = SetOfEnumDataType.parseSetofEnum(filterValues.get(0), false, false);
                                    for (String soeValue : soeValues) {
                                        if (!soeValue.equals("*")) {
                                            if (soeValue.equals(realValue)) {
                                                // Wert (Zeile) soll erhalten bleiben => restliche Filter durchspielen
                                                for (String filteredTableName : filterTableNames) {
                                                    if (!getAssemblyListFilter().checkFilter(filteredTableName, attributes, language)) {
                                                        // Produkt-Filter wieder aktiv setzen
                                                        pseudoProductFilterTyp.setActive(true);
                                                        return false;
                                                    }
                                                }
                                                // Produkt-Filter wieder aktiv setzen
                                                pseudoProductFilterTyp.setActive(true);
                                                return true;
                                            }
                                        }
                                    }
                                    pseudoProductFilterTyp.setActive(true);
                                    return false;
                                }
                            } else {
                                // ein anderer Filter aus der Pseudo-Tabelle => nur diesen Tabellen-Filter durchspielen
                                if (!getAssemblyListFilter().checkFilter(tableName, attributes, language)) {
                                    // Produkt-Filter wieder aktiv setzen
                                    pseudoProductFilterTyp.setActive(true);
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            // spezieller Podukt-Filter ist nicht aktiv, oder die Zeile ist nicht gefiltert
            // restliche Filter für alle Tabellen durchspielen
            for (String filteredTableName : filterTableNames) {
                if (!getAssemblyListFilter().checkFilter(filteredTableName, attributes, language)) {
                    return false;
                }
            }
            return true;
        }
    }


    private static class MustFieldDescription {

        public String tableName;
        public String fieldName;
        public String description;

        public MustFieldDescription(String tableName, String fieldName, String description) {
            this.tableName = tableName;
            this.fieldName = fieldName;
            this.description = description;
        }
    }


    public static class PartListEntryReferenceKey {

        private PartListEntryReferenceKeyByAA referenceKeyByAA;
        private String bcteKeyWithoutSData;
        private long dateFrom;
        private String textId;

        public PartListEntryReferenceKey(EtkDataPartListEntry partListEntry, boolean isConstruction, String textId) {
            String guid;
            if (isConstruction) {
                guid = partListEntry.getAsId().getKLfdnr();
            } else {
                guid = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
            }

            iPartsDialogBCTEPrimaryKey completeBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(guid);
            if (completeBCTEKey != null) {
                dateFrom = StrUtils.strToLongDef(completeBCTEKey.sData, -1); // "Datum ab" aus dem BCTE-Schlüssel bestimmen
                // Datum ab im BCTE-Schlüssel leeren, weil es bei der Referenzsuche anders berücksichtigt wird
                // (Referenzteileposition mit dem nächst kleineren Datum ab zur neuen Teileposition)
                bcteKeyWithoutSData = completeBCTEKey.getPositionBCTEPrimaryKeyWithoutSDA().toString();
                referenceKeyByAA = new PartListEntryReferenceKeyByAA(completeBCTEKey);

            } else { // BCTE-Schlüssel kann nicht bestimmt werden
                if (isConstruction) {
                    // Meldung nur in Konstruktion (im Retail kann das jederzeit passieren siehe Excel-Import)
                    Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Transfer part list entry to AS: BCTE key for part list entry \""
                                                                              + partListEntry.getAsId().toStringForLogMessages()
                                                                              + "\" with GUID \"" + guid + "\" can't be calculated");
                }
                dateFrom = -1;
                bcteKeyWithoutSData = partListEntry.getAsId().toString(); // Eindeutiger Dummy-String
            }
            this.textId = textId;
        }

        public String getBcteKeyWithoutSData() {
            return bcteKeyWithoutSData;
        }

        public long getDateFrom() {
            return dateFrom;
        }

        public String getTextId() {
            return textId;
        }

        public PartListEntryReferenceKeyByAA getReferenceKeyByAA() {
            return referenceKeyByAA;
        }
    }
}