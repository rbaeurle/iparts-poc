/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiPanelWithCheckbox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.RowContentForAutoTransferToAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.TransferToASElement;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.collections.dwlist.DwList;

import java.util.ArrayList;
import java.util.List;

// Code analog zu EditTransferPartlistPredictionGrid, nur stark vereinfacht
public class EditAutoTransferPartlistGrid extends SimpleMasterDataSearchFilterGrid implements iPartsConst {

    public static String TABLE_PSEUDO = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER");
    public static String FIELD_PSEUDO_ASSEMBLY_ID = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-ASSEMBLY_ID");
    public static String FIELD_PSEUDO_ASSEMBLY_NAME = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-ASSEMBLY_NAME");
    public static String FIELD_PSEUDO_TRANSFER = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-TO_TRANSFER");

    private static String TRANSFER_UNIFY_ALIAS = "transferUnify";
    private static String TRANSFER_TEXT_ALL = "!!Alle markierten übernehmen";
    private static String TRANSFER_TEXT_NON = "!!Alle markierten nicht übernehmen";
    private static String SHOW_TRANSFER_PARTLIST_SEPARATOR = "showTransferPartlistSeparator";
    private static String SHOW_TRANSFER_PARTLIST = "showTransferPartlist";
    private static String SHOW_TRANSFER_PARTLIST_TEXT = "!!Anzeige der Konstruktions-Positionen";

    private boolean eventListenersActive;
    private boolean contextMenuTransferAll;

    /**
     * Erzeugt eine Instanz von EditAutoTransferPartlistGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     */
    public EditAutoTransferPartlistGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                        String tableName) {
        super(dataConnector, parentForm, tableName, null);
        setEditFields(null);
        setNewAllowed(false);
        setDeleteAllowed(false);
        setModifyAllowed(false);
        setEditAllowed(false);
        setMaxResults(J2EEHandler.isJ2EE() ? iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
        showSearchFields(false);
        showSelectCount(false);
        showToolbar(false);

        setDisplayResultFields(createDisplayFields(), false);
        getTable().setResizeColumnIndex(1); // Modul-Benennung

        eventListenersActive = true;
        getGui().setName("AutoTransferToAS");
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        GuiMenuItem transferUnify = toolbarHelper.createMenuEntry(TRANSFER_UNIFY_ALIAS, TRANSFER_TEXT_ALL, null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doTransferUnify(event);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(transferUnify);

        // Separator
        contextMenu.addChild(toolbarHelper.createMenuSeparator(SHOW_TRANSFER_PARTLIST_SEPARATOR, getUITranslationHandler()));

        GuiMenuItem showPartList = toolbarHelper.createMenuEntry(SHOW_TRANSFER_PARTLIST, SHOW_TRANSFER_PARTLIST_TEXT, null, new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doShowPartList(event);
            }
        }, getUITranslationHandler());
        contextMenu.addChild(showPartList);
    }

    @Override
    protected synchronized void startSearch() {
        // sonst wird bei ENTER die Suche ausgelöst
    }

    @Override
    protected RowWithAttributesForAutoTransfer createRow(final DBDataObjectAttributes attributes) {
        RowWithAttributesForAutoTransfer row = new RowWithAttributesForAutoTransfer();
        row.attributes = attributes;
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
    protected void enableButtons() {
        int enabledCount = 0;
        AbstractGuiControl transferUnifyMenuItem = toolbarHelper.findSeparator(getTable().getContextMenu(), TRANSFER_UNIFY_ALIAS);
        if (transferUnifyMenuItem instanceof GuiMenuItem) {
            int selectedEnabledCount = 0;
            for (RowWithAttributesForAutoTransfer selectedRow : getSelectedRows()) {
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

        boolean singleSelected = getTable().getSelectedRows().size() == 1;
        toolbarHelper.enableMenu(SHOW_TRANSFER_PARTLIST, getTable().getContextMenu(), singleSelected);
    }

    private void doTransferUnify(Event event) {
        for (RowWithAttributesForAutoTransfer rowWithAttributesForAutoTransfer : getSelectedRows()) {
            rowWithAttributesForAutoTransfer.selectCheckbox(contextMenuTransferAll);
        }
    }

    private void doShowPartList(Event event) {
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();
        if (selectedRows.isEmpty()) {
            return;
        }
        GuiTableRow row = selectedRows.get(0);
        if (row instanceof RowWithAttributesForAutoTransfer) {
            RowContentForAutoTransferToAS rowContent = ((RowWithAttributesForAutoTransfer)row).rowContent;
            if (rowContent != null) {
                List<TransferToASElement> transferElements = rowContent.getTransferElements();
                if (!transferElements.isEmpty()) {
                    AssemblyId assemblyId = rowContent.getAssemblyId();
                    EtkDataAssembly assembly = transferElements.get(0).getSelectedPartlistEntry().getOwnerAssembly();
                    List<EtkDataObject> showList = new DwList<>();
                    for (TransferToASElement transferElement : transferElements) {
                        showList.add(transferElement.getSelectedPartlistEntry());
                    }
                    iPartsShowDataObjectsDialog.showDIALOGTransferToAS(getConnector(), this, showList, assemblyId, assembly.getEbene().getFields(), true);
                }
            }
        }
    }

    @Override
    protected void doEditOrView(Event event) {
        doShowPartList(event);
    }

    protected EtkDisplayFields createDisplayFields() {
        List<String> dbLanguages = getProject().getConfig().getDatabaseLanguages();
        EtkDisplayField displayField;
        EtkDisplayFields displayFields = new EtkDisplayFields();

        // TU
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_ASSEMBLY_ID, false, false);
        displayField.setText(new EtkMultiSprache("!!TU", dbLanguages));
        displayField.setDefaultText(false);
        displayField.setColumnFilterEnabled(true);
        displayFields.addFeld(displayField);

        // Benennung
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_ASSEMBLY_NAME, true, false);
        displayField.setText(new EtkMultiSprache("!!Benennung", dbLanguages));
        displayField.setDefaultText(false);
        displayField.setColumnFilterEnabled(true);
        displayFields.addFeld(displayField);

        // Übernehmen CheckBox
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_TRANSFER, false, false);
        displayField.setText(new EtkMultiSprache("!!Übernehmen", dbLanguages));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        displayField.setGrowColumn(true);

        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    public void switchOffEventListeners() {
        getTable().switchOffEventListeners();
        eventListenersActive = false;
    }

    public void switchOnEventListeners() {
        getTable().switchOnEventListeners();
        eventListenersActive = true;
    }

    protected GuiPanel createTextCell(String text) {
        return createTextCell(text, null);
    }

    protected GuiPanel createTextCell(FrameworkImage icon) {
        return createTextCell("", icon);
    }

    private GuiPanel createTextCell(String text, FrameworkImage icon) {
        GuiPanel panel = new GuiPanel(new LayoutGridBag(false));
        panel.setBackgroundColor(Colors.clTransparent.getColor());
        GuiLabel label = new GuiLabel(text, icon);
        label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.CENTER);
        label.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 1, LayoutGridBag.ANCHOR_CENTER, LayoutGridBag.FILL_HORIZONTAL, 0, 2, 0, 0));
        panel.addChild(label);
        return panel;
    }

    public void addToGrid(final RowContentForAutoTransferToAS rowContent) {
        if (rowContent != null) {
            final DBDataObjectAttributes attributes = rowContent.getAsAttributes(getProject());
            final RowWithAttributesForAutoTransfer newRow = new RowWithAttributesForAutoTransfer();
            newRow.attributes = attributes;
            newRow.rowContent = rowContent;

            int index = 0;
            for (EtkDisplayField field : displayResultFields.getVisibleFields()) {
                String fieldName = field.getKey().getFieldName();
                AbstractGuiControl control = getControlFromDisplayFields(fieldName, index, rowContent, newRow, attributes);
                newRow.addChild(control, () -> control.getTextRepresentation(true));
                index++;
            }
            getTable().addRow(newRow);
        }
    }

    protected AbstractGuiControl getControlFromDisplayFields(String fieldName, int index, RowContentForAutoTransferToAS rowContent,
                                                             RowWithAttributesForAutoTransfer newRow,
                                                             final DBDataObjectAttributes attributes) {
        AbstractGuiControl control;
        if (fieldName.equals(FIELD_PSEUDO_TRANSFER)) {
            // Setze die Checkbox an der vorgegebene Stelle
            OnChangeEvent changeEvent = () -> {
                if (eventListenersActive) {
                    newRow.updateTransferMarkFromGUI();
                    // bewirkt enableButtons() im ParentForm
                    parentForm.updateData(null, true);
                }
            };
            control = new iPartsGuiPanelWithCheckbox(rowContent.isTransferMark(), changeEvent);
            newRow.checkBoxIndex = index;
        } else {
            control = createTextCell(attributes.getFieldValue(fieldName));
        }
        return control;
    }

    /**
     * Alle sichtbaren Einträge die zum Übernehmen markiert sind (Checkbox) ermitteln.
     * Hier sind die aktuell ausgefilterten Einträge nicht enthalten
     *
     * @return Die {@link TransferToASElement}s aller Zeilen, bei denen die Checkbox angehakt ist
     */
    public List<TransferToASElement> getAllVisibleTransferItems() {
        List<TransferToASElement> selectedElements = new ArrayList<>();
        for (int i = 0; i < getTable().getRowCount(); i++) {
            GuiTableRow row = getTable().getRow(i);
            if ((row instanceof RowWithAttributesForAutoTransfer)) {
                RowContentForAutoTransferToAS rowContent = ((RowWithAttributesForAutoTransfer)row).rowContent;
                if ((rowContent != null) && rowContent.isTransferMark()) {
                    selectedElements.addAll(rowContent.getTransferElements());
                }
            }
        }
        return selectedElements;
    }

    /**
     * Alle Einträge, die zum Übernehmen markiert sind (Checkbox) ermitteln.
     * Hier werden auch aktuell ausgefilterte Einträge berücksichtigt
     *
     * @return Die {@link TransferToASElement}s aller Zeilen, bei denen die Checkbox angehakt ist
     */
    public List<TransferToASElement> getAllTransferItems() {
        List<TransferToASElement> selectedElements = new ArrayList<>();
        for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row : getEntries()) {
            if ((row instanceof RowWithAttributesForAutoTransfer)) {
                RowContentForAutoTransferToAS rowContent = ((RowWithAttributesForAutoTransfer)row).rowContent;
                if ((rowContent != null) && rowContent.isTransferMark()) {
                    selectedElements.addAll(rowContent.getTransferElements());
                }
            }
        }
        return selectedElements;
    }

    public List<RowWithAttributesForAutoTransfer> getSelectedRows() {
        List<RowWithAttributesForAutoTransfer> result = new DwList<>();
        List<GuiTableRow> selectedRows = getTable().getSelectedRows();
        for (GuiTableRow selectedRow : selectedRows) {
            if (selectedRow instanceof RowWithAttributesForAutoTransfer) {
                result.add((RowWithAttributesForAutoTransfer)selectedRow);
            }
        }
        return result;
    }


    static class RowWithAttributesForAutoTransfer extends SimpleSelectSearchResultGrid.GuiTableRowWithAttributes {

        protected RowContentForAutoTransferToAS rowContent;
        protected int checkBoxIndex = -1;

        public void selectCheckbox(boolean selected) {
            iPartsGuiPanelWithCheckbox checkbox = getCheckbox();
            if (checkbox != null) {
                checkbox.setSelected(selected);
            }
        }

        public void updateTransferMarkFromGUI() {
            iPartsGuiPanelWithCheckbox checkbox = getCheckbox();
            if ((checkbox != null) && (rowContent != null)) {
                rowContent.setTransferMark(checkbox.isSelected());
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
}