/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumDataType;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.PictureAndTUValidationEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.PictureAndTUValidationEntryList;
import de.docware.framework.modules.config.db.datatypes.DatatypeHtmlResult;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasse zum Erzeugen eines SimpleMasterDataSearchFilterGrid für die Ergebnisse der Qualitätsprüfung
 */
public class PictureAndTUGrid extends SimpleMasterDataSearchFilterGrid {

    public static final String DUMMY_TABLE_NAME = "PictureAndTUValidation";
    public static final String DISPLAY_NAME_OBJECT = "OBJECT";
    public static final String DISPLAY_NAME_MESSAGE = "MESSAGE";
    private static final String FIELD_NAME_ADD_INFO = "ADD_INFO";

    private EnumDataType enumDataType;
    private iPartsValidationPictureAndTUForm validationPictureAndTUForm;
    private Map<GuiTableRow, IdWithType> objectIdsMap;

    GuiMenuItem gotoMenuItem;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public PictureAndTUGrid(iPartsValidationPictureAndTUForm validationPictureAndTUForm,
                            EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, DUMMY_TABLE_NAME, null);
        this.validationPictureAndTUForm = validationPictureAndTUForm;
        this.objectIdsMap = new HashMap<>();

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = getDisplayFields();

        this.setDisplayResultFields(displayFields);
        this.setEditAllowed(false);
        this.setNewAllowed(false);
        this.setModifyAllowed(false);
        this.setDeleteAllowed(false);
        this.showSearchFields(false);
        this.showToolbar(false);
        this.showSelectCount(false);
        this.setLabelNotFoundText("!!Es liegen keine Ergebnisse vor");
        // Seperator vor dem Kopieren Menüpunkt entfernen
        toolbarHelper.hideSeparatorInToolbarAndMenu(SEPERATOR_ALIAS, getTable().getContextMenu());
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    private EtkDisplayFields getDisplayFields() {
        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(searchTable, DISPLAY_NAME_OBJECT, getConfig());
        displayField.setText(new EtkMultiSprache("!!Objekt", getConfig().getViewerLanguages()));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(searchTable, iPartsDataVirtualFieldsDefinition.DA_PICTURE_AND_TU_QUALITY_CHECK, getConfig());
        displayField.setText(new EtkMultiSprache("!!Prüfung", getConfig().getViewerLanguages()));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(searchTable, DISPLAY_NAME_MESSAGE, getConfig());
        displayField.setText(new EtkMultiSprache("!!Meldung", getConfig().getViewerLanguages()));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);

        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        gotoMenuItem = toolbarHelper.createMenuEntry(iPartsEditAssemblyListValidationForm.IPARTS_MENU_ITEM_OPEN_EDIT_RETAIL_WINDOW,
                                                     iPartsEditAssemblyListValidationForm.IPARTS_MENU_TEXT_OPEN_EDIT_RETAIL_WINDOW,
                                                     DefaultImages.module.getImage(), new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        gotoEditRetail();
                    }
                }, getUITranslationHandler());
        contextMenu.addChild(gotoMenuItem);

    }

    @Override
    protected String getVisualValueOfFieldValue(String tableName, String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
        if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DA_PICTURE_AND_TU_QUALITY_CHECK)) {
            DatatypeHtmlResult datatypeHtmlResult = enumDataType.asHtml(getProject(), fieldValue, getProject().getDBLanguage(), false, true);
            return datatypeHtmlResult.getStringResult();
        }
        return super.getVisualValueOfFieldValue(tableName, fieldName, fieldValue, isMultiLanguage);
    }

    public void fillGrid(PictureAndTUValidationEntryList validationEntries) {
        clearGrid();
        addToGrid(validationEntries);
    }

    @Override
    protected void clearGrid() {
        super.clearGrid();
        objectIdsMap.clear();
    }


    public void addToGrid(PictureAndTUValidationEntryList validationEntries) {
        // EnumDataType erstellen für das DA_PICTURE_AND_TU_QUALITY_CHECK Feld, da dieses später Icon zeigen soll
        enumDataType = new EnumDataType(iPartsPlugin.TABLE_FOR_EVALUATION_RESULTS, iPartsDataVirtualFieldsDefinition.DA_PICTURE_AND_TU_QUALITY_CHECK);
        clearGrid();
        for (PictureAndTUValidationEntry entry : validationEntries) {
            SimpleSelectSearchResultGrid.GuiTableRowWithAttributes row = fillAttribute(entry.getValidationObjectVisValue(),
                                                                                       entry.getValidation_resultAsDBValue(),
                                                                                       entry.getValidationMessage(),
                                                                                       entry.getAdditionalInformation());
            objectIdsMap.put(row, entry.getValidationObject()); // Zusätzlich noch die ID merken, um später dorthin springen zu können
        }
        enumDataType = null;
        showNoResultsLabel(validationEntries.isEmpty(), false);
    }

    /**
     * Bei der Selektion des Grids für die validationPictureAndTUForm, die AdditionalInfoTextArea updaten
     *
     * @param event
     */
    @Override
    public void onTableSelectionChanged(Event event) {
        super.onTableSelectionChanged(event);
        DBDataObjectAttributes selection = getSelection();
        List<GuiTableRow> rows = getTable().getSelectedRows();
        if (!rows.isEmpty() && rows.size() == 1) {
            String text = selection.getFieldValue(FIELD_NAME_ADD_INFO);
            validationPictureAndTUForm.getAdditionalInfoTextArea().clear();
            if (StrUtils.isValid(text)) {
                validationPictureAndTUForm.getAdditionalInfoTextArea().setText(text);
            }
        } else {
            validationPictureAndTUForm.getAdditionalInfoTextArea().clear();
        }
    }

    // Sprung zur Assembly, bzw. falls möglich zum konkreten Stücklisteneintrag im Edit
    private void gotoEditRetail() {
        GuiTableRow selectedRow = getTable().getSelectedRow();
        if (selectedRow != null) {
            PartListEntryId partListEntryId = null;
            IdWithType id = objectIdsMap.get(selectedRow);
            if (id == null) {
                return;
            }
            if (id.getType().equals(PartListEntryId.TYPE)) {
                // Falls an der Zeile ein Stücklisteneintrag hinterlegt ist, dorthin springen
                partListEntryId = new PartListEntryId(id.toStringArrayWithoutType());
            } else {
                // sonst einfach nur die passende Assembly öffnen
                EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
                if (currentAssembly != null) {
                    partListEntryId = new PartListEntryId(currentAssembly.getAsId());
                }
            }
            if (partListEntryId != null) {
                validationPictureAndTUForm.closeRelatedFormForNonModalShow();
                iPartsGotoHelper.loadAndGotoEditRetail(getConnector(), getParentForm(), partListEntryId);
            }
        }
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();
        if (gotoMenuItem != null) {
            List<GuiTableRow> selectedRows = getTable().getSelectedRows();
            boolean isSingleSelect = (selectedRows.size() == 1);
            gotoMenuItem.setVisible(isSingleSelect);
        }
    }

    private SimpleSelectSearchResultGrid.GuiTableRowWithAttributes fillAttribute(String fieldObject, String fieldValidation, String fieldMessage, String addInfo) {
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        addAttribute(attributes, DISPLAY_NAME_OBJECT, fieldObject);
        addAttribute(attributes, iPartsDataVirtualFieldsDefinition.DA_PICTURE_AND_TU_QUALITY_CHECK, fieldValidation);
        addAttribute(attributes, DISPLAY_NAME_MESSAGE, fieldMessage);
        addAttribute(attributes, FIELD_NAME_ADD_INFO, addInfo);

        return addAttributesToGrid(attributes);
    }

    private void addAttribute(DBDataObjectAttributes attributes, String fieldName, String value) {
        DBDataObjectAttribute attribute = new DBDataObjectAttribute(fieldName, DBDataObjectAttribute.TYPE.STRING, true);
        attribute.setValueAsString(value, DBActionOrigin.FROM_DB);
        attributes.addField(attribute, DBActionOrigin.FROM_DB);
    }
}
