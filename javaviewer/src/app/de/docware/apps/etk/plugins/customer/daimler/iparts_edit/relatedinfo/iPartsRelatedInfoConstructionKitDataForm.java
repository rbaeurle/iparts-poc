/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsConstructionKits;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Formular für die Anzeige von Baukästen (ConstructionKit) zu einer Sachnummer innerhalb der RelatedInfo.
 */
public class iPartsRelatedInfoConstructionKitDataForm extends AbstractRelatedInfoPartlistDataForm {

    public static final String IPARTS_MENU_ITEM_SHOW_CONSTRUCTION_KIT_DATA = "iPartsMenuItemShowConstructionKitData";
    public static final String CONFIG_KEY_CONSTRUCTION_KIT_DATA_DISPLAYFIELDS = "Plugin/iPartsEdit/ConstructionKit";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.DialogRetail,
                                                                                   iPartsModuleTypes.PSK_PKW,
                                                                                   iPartsModuleTypes.Dialog_SM_Construction);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_CONSTRUCTION_KIT_DATA, "!!Baukasten zu Teil anzeigen...",
                                EditDefaultImages.edit_btn_constructKits_new.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_CONSTRUCTION_KITS_DATA);
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
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_CONSTRUCTION_KIT_DATA, menuItemVisible);
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry partListEntry, AbstractJavaViewerFormIConnector connector) {
        EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
        if (!relatedInfoIsVisible(ownerAssembly, VALID_MODULE_TYPES)) {
            return false;
        }

        iPartsModuleTypes moduleType = iPartsModuleTypes.getType(ownerAssembly.getEbeneName());
        boolean retailMode = !moduleType.isConstructionRelevant() && !(connector.getActiveForm() instanceof EditModuleForm);

        // Retail-Anzeige -> Baukästen nur dann anzeigen, wenn zu Stücklisten-Eintrag auch ein Baukasten existiert
        boolean isVirtualPartList = iPartsVirtualNode.isVirtualId(ownerAssembly.getAsId());
        if (retailMode) {
            return relatedInfoIsVisibleForRetail(connector, partListEntry);
        } else if (isVirtualPartList) {
            return relatedInfoIsVisibleForConstruction(connector, partListEntry, retailMode);
        } else {
            // Edit -> Baukästen immer anzeigen wenn Stücklistentyp passt und nicht ReadOnly (wird oben ja schon überprüft)
            if (!AbstractRelatedInfoPartlistDataForm.isEditContext(connector, true)) {
                // Bei ReadOnly die Farbvarianten nur anzeigen, wenn es Variantentabellen gibt
                return relatedInfoIsVisibleForRetail(connector, partListEntry);
            }
        }
        return true;
    }

    /**
     * Überprüft, ob für den übergebenen Stücklisteneintrag ein Baukasten vorhanden ist und
     * demzufolge die Related Info sichtbar sein bzw. ein Icon in der Stückliste angezeigt werden soll. Macht in der Konstruktionssicht
     * und im Edit keinen Sinn.
     *
     * @param entry
     * @return
     */
    public static boolean relatedInfoIsVisibleForRetail(AbstractJavaViewerFormIConnector connector, EtkDataPartListEntry entry) {
        if (entry instanceof iPartsDataPartListEntry) {
            // Überprüfung ob Baukasten
            return iPartsConstructionKits.getInstance(connector.getProject()).isConstructionKit(entry.getPart().getAsId());
        }
        return false;
    }

    /**
     * Überprüft, ob für den übergebenen Konstruktions-Stücklisteneintrag ein Baukasten vorhanden ist und
     * demzufolge die Related Info sichtbar sein bzw. ein Icon in der Stückliste angezeigt werden soll.
     *
     * @param entry
     * @return
     */
    private static boolean relatedInfoIsVisibleForConstruction(AbstractJavaViewerFormIConnector connector, EtkDataPartListEntry entry, boolean retailMode) {
        if ((entry instanceof iPartsDataPartListEntry) && !retailMode) {
            // Überprüfung ob Baukasten
            return iPartsConstructionKits.getInstance(connector.getProject()).isConstructionKit(entry.getPart().getAsId());
        }
        return false;
    }

    /**
     * Related Info Icon wird angezeigt falls Baukasten für die AS-Sicht oder die Konstruktionsstückliste existieren
     *
     * @param entry
     */
    public static AssemblyListCellContentFromPlugin getRelatedInfoIconForRetail(AbstractJavaViewerFormIConnector connector, EtkDataPartListEntry entry) {
        EtkDataAssembly ownerAssembly = entry.getOwnerAssembly();
        if (!relatedInfoIsVisible(ownerAssembly, VALID_MODULE_TYPES)) {
            return null;
        }

        // Handelt es sich überhaupt um eine Stückliste, die Teile enthalten kann?
        if (ownerAssembly instanceof iPartsDataAssembly) {
            if (!iPartsModuleTypes.isModuleTypeWithParts(((iPartsDataAssembly)ownerAssembly).getModuleType())) {
                return null;
            }
        }

        // relatedInfoIsVisibleForRetail() prüft sowohl für Retail als auch Konstruktion, weil nur die Materialnummer betrachtet wird
        if (relatedInfoIsVisibleForRetail(connector, entry)) {
            AssemblyListCellContentFromPlugin iconInfo = new AssemblyListCellContentFromPlugin(iPartsConst.CONFIG_KEY_RELATED_INFO_CONSTRUCTION_KITS_DATA,
                                                                                               EditDefaultImages.edit_btn_constructKits_new.getImage());
            iconInfo.setHint(iPartsConst.RELATED_INFO_CONSTRUCTIONS_KITS_TEXT);
            iconInfo.setCursor(DWCursor.Hand);
            iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
            return iconInfo;
        }
        return null;
    }

    private static final String FIELD_LEVEL = "DummyLevel";
    private static final int START_LEVEL = 1;
    private static final int END_LEVEL = 30;

    private boolean editMode;
    private boolean isReadOnly;
    private boolean isRetailPartList;
    private EtkDataPartListEntry currentPartListEntry;
    private DataObjectFilterGrid grid;
    private EtkDisplayFields selectFields;
    private boolean allVersions;
    private int maxLevels;

    /**
     * Erzeugt eine Instanz von iPartsRelatedInfoConstructionKitDataForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    protected iPartsRelatedInfoConstructionKitDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                       IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        editMode = isEditContext(dataConnector, false);
        allVersions = false;

        $$internalCreateGui$$(null);
        postCreateGui();
        setReadOnly(!(editMode && isRevisionChangeSetActiveForEdit()));
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        // DataObjectFilterGrid auf panelCenter aufschnappen
        grid = new DataObjectFilterGrid(getConnector(), this) {
            @Override
            protected EtkDataObject getDataObjectForRowAndTable(GuiTableRowWithObjects row, String tableName) {
                // Alle Felder (auch von MAT) befinden sich durch den Join im DataObject der Tabelle DA_CONST_KIT_CONTENT
                return super.getDataObjectForRowAndTable(row, iPartsConst.TABLE_DA_CONST_KIT_CONTENT);
            }
        };

        grid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        panelMain.panelCenter.addChild(grid.getGui());

        panelMain.intspinnerLevel.setMinValue(START_LEVEL);
        panelMain.intspinnerLevel.setMaxValue(END_LEVEL);
        panelMain.intspinnerLevel.setValue(START_LEVEL);
        maxLevels = START_LEVEL;
    }

    protected void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    protected void dataToGrid() {
        int oldSortColumn = -1;
        boolean isSortAscending = grid.getTable().isSortAscending();
        List<iPartsConstKitContentId> selectedConstKits = new DwList<iPartsConstKitContentId>();
        Map<Integer, Object> columnFilterValuesMap = null; // Map von Spalten-Indizes auf Filtertwert-Objekte
        AbstractGuiTableColumnFilterFactory copyColumnFilterFactory = null;
        if (grid.getTable().getRowCount() > 0) {
            oldSortColumn = grid.getTable().getSortColumn();
            columnFilterValuesMap = new HashMap<Integer, Object>(); // Map von Spalten-Indizes auf Filtertwert-Objekte
            copyColumnFilterFactory = grid.storeFilterFactory(columnFilterValuesMap);
            List<GuiTableRow> rows = grid.getTable().getSelectedRows();
            for (GuiTableRow row : rows) {
                if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                    iPartsConstKitContentId id = buildIdFromDataObject(((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(iPartsConst.TABLE_DA_CONST_KIT_CONTENT));
                    if (id != null) {
                        selectedConstKits.add(id);
                    }
                }
            }
        }

        grid.clearGrid();
        fillGrid();

        if (copyColumnFilterFactory != null) {
            grid.restoreFilterFactory(copyColumnFilterFactory, columnFilterValuesMap);
        }
        // Sortierung wiederherstellen falls vorher sortiert war
        if (grid.getTable().isSortEnabled() && (oldSortColumn >= 0)) {
            grid.getTable().sortRowsAccordingToColumn(oldSortColumn, isSortAscending);
        }
        if (!selectedConstKits.isEmpty()) {
            List<Integer> rowIndices = new DwList<Integer>();
            int cnt = 0;
            int rowCnt = 0;
            for (GuiTableRow row : grid.getTable().getAllRows()) {
                if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                    iPartsConstKitContentId id = buildIdFromDataObject(((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(iPartsConst.TABLE_DA_CONST_KIT_CONTENT));
                    if (id != null) {
                        if (selectedConstKits.contains(id)) {
                            rowIndices.add(rowCnt);
                            cnt++;
                        }
                    }
                }
                rowCnt++;
            }
            grid.getTable().setSelectedRows(Utils.toIntArray(rowIndices), true, true, true);
        }
    }

    private iPartsConstKitContentId buildIdFromDataObject(EtkDataObject dataObject) {
        if (dataObject != null) {
            return new iPartsConstKitContentId(dataObject.getFieldValue(iPartsConst.FIELD_DCKC_SUB_PART_NO),
                                               dataObject.getFieldValue(iPartsConst.FIELD_DCKC_DCKC_POSE),
                                               dataObject.getFieldValue(iPartsConst.FIELD_DCKC_WW),
                                               dataObject.getFieldValue(iPartsConst.FIELD_DCKC_SDA));
        }
        return null;
    }

    protected void fillGrid() {
        if (grid.getDisplayFields() == null) {
            grid.setDisplayFields(getDisplayFields(CONFIG_KEY_CONSTRUCTION_KIT_DATA_DISPLAYFIELDS));
        }
        fillTopPanel();
        boolean hasQuantity = grid.getDisplayFields().contains(TableAndFieldName.make(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_M_QUANTUNIT), false);
        Stack<StackElem> constructionKitStack = new Stack<StackElem>();
        iPartsDataConstKitContentList dataConstKitContentList = iPartsDataConstKitContentList.loadForPartWithJoin(getProject(),
                                                                                                                  currentPartListEntry.getPart().getAsId().getMatNr(),
                                                                                                                  selectFields,
                                                                                                                  allVersions);
        StackElem stackElem = new StackElem(0, 0, dataConstKitContentList);
        constructionKitStack.push(stackElem);

        while (!constructionKitStack.isEmpty()) {
            StackElem currentStackElem = constructionKitStack.pop();
            iPartsConstructionKits constructionKits = iPartsConstructionKits.getInstance(getProject());
            for (int index = currentStackElem.lfdNr; index < currentStackElem.dataConstKitContentList.size(); index++) {
                iPartsDataConstKitContent dataConstKitContent = currentStackElem.dataConstKitContentList.get(index);
                DBDataObjectAttributes attributes = dataConstKitContent.getAttributes();
                attributes.addField(FIELD_LEVEL, currentStackElem.getEbeneValue(), DBActionOrigin.FROM_DB);
                if (hasQuantity) {
                    String value = getVisObject().asHtml(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_QUANTUNIT,
                                                         attributes.getFieldValue(iPartsConst.FIELD_M_QUANTUNIT), getProject().getDBLanguage()).getStringResult();
                    attributes.addField(iPartsConst.FIELD_M_QUANTUNIT, value, DBActionOrigin.FROM_DB);
                }

                grid.addObjectToGrid(dataConstKitContent);
                if (constructionKits.isConstructionKit(dataConstKitContent.getSubPartNo()) &&
                    ((currentStackElem.ebene + 1) < maxLevels)) {
                    currentStackElem.lfdNr = index + 1;
                    constructionKitStack.push(currentStackElem);
                    dataConstKitContentList = iPartsDataConstKitContentList.loadForPartWithJoin(getProject(),
                                                                                                dataConstKitContent.getSubPartNo(),
                                                                                                selectFields,
                                                                                                allVersions);
                    stackElem = new StackElem(currentStackElem.ebene + 1, 0, dataConstKitContentList);
                    constructionKitStack.push(stackElem);
                    break;
                }
            }
        }
        grid.showNoResultsLabel(grid.getTable().getRowCount() == 0);
        refreshGridColors();
    }

    private void refreshGridColors() {
        int rowCount = grid.getTable().getRowCount();
        if (rowCount > 1) {
            Color back1 = Colors.clDesignTableContentBackground.getColor();
            Color back2 = Colors.clDesignTableContentBackgroundAlternating.getColor();
            Color backgroundColor = back1;
            DataObjectGrid.GuiTableRowWithObjects tableRow = (DataObjectGrid.GuiTableRowWithObjects)grid.getTable().getRow(0);
            EtkDataObject dataObject = tableRow.getObjectForTable(iPartsConst.TABLE_DA_CONST_KIT_CONTENT);
            String startLevel = dataObject.getFieldValue(FIELD_LEVEL);
            for (int rowNo = 1; rowNo < rowCount; rowNo++) {
                tableRow = (DataObjectGrid.GuiTableRowWithObjects)grid.getTable().getRow(rowNo);
                dataObject = tableRow.getObjectForTable(iPartsConst.TABLE_DA_CONST_KIT_CONTENT);
                String currentLevel = dataObject.getFieldValue(FIELD_LEVEL);
                if (!currentLevel.equals(startLevel)) {
                    if (backgroundColor.equals(back1)) {
                        backgroundColor = back2;
                    } else {
                        backgroundColor = back1;
                    }
                    startLevel = currentLevel;
                }
                tableRow.setBackgroundColor(backgroundColor);
            }
        }
    }

    private void fillTopPanel() {
        EtkDataPart dataPart = currentPartListEntry.getPart();
        boolean oldLogLoadFieldIfNeeded = dataPart.isLogLoadFieldIfNeeded();
        try {
            dataPart.setLogLoadFieldIfNeeded(false);
            String part = dataPart.getDisplayValue(iPartsConst.FIELD_M_BESTNR, getProject().getDBLanguage());
            panelMain.textfieldPart.setText(part);
            String name = dataPart.getDisplayValue(iPartsConst.FIELD_M_TEXTNR, getProject().getDBLanguage());
            panelMain.textfieldName.setText(name);
        } finally {
            dataPart.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
        }
    }

    @Override
    protected EtkDisplayFields getDisplayFields(String configKey) {
        EtkDisplayFields displayFields = super.getDisplayFields(configKey);
        selectFields = new EtkDisplayFields();
        selectFields.assign(displayFields);

        int index = displayFields.getIndexOfFeld(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, FIELD_LEVEL, false);
        if (index == -1) {
            // Ebene
            EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, FIELD_LEVEL, false, false);
            EtkMultiSprache text = new EtkMultiSprache("!!Ebene", getProject().getConfig().getDatabaseLanguages());
            displayField.setText(text);
            displayField.setDefaultText(false);
            displayField.loadStandards(getConfig());
            displayFields.addFeld(0, displayField);
        }
        // weitere Manipulationen
        for (EtkDisplayField displayField : displayFields.getFields()) {
            // Spaltenfilterung auf jeden Fall ausschalten
            displayField.setColumnFilterEnabled(false);
        }
        return displayFields;
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        List<EtkDisplayField> displayResultFields = new DwList<EtkDisplayField>();
        EtkDisplayField displayField;
        // POS
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_DCKC_POSE, false, false);
        EtkMultiSprache text = new EtkMultiSprache("!!POS", getProject().getConfig().getDatabaseLanguages());
        displayField.setText(text);
        displayField.setDefaultText(false);
        displayResultFields.add(displayField);
        // WW
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_WW, false, false);
        displayResultFields.add(displayField);
        // ET-KZ ?
        displayField = new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_ETKZ, false, false);
        displayResultFields.add(displayField);
        // Sachnummer
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_SUB_PART_NO, false, false);
        text = new EtkMultiSprache("!!Sachnummer", getProject().getConfig().getDatabaseLanguages());
        displayField.setText(text);
        displayField.setDefaultText(false);
        displayResultFields.add(displayField);
        // Benennung
        displayField = new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR, true, false);
        text = new EtkMultiSprache("!!Benennung", getProject().getConfig().getDatabaseLanguages());
        displayField.setText(text);
        displayField.setDefaultText(false);
        displayResultFields.add(displayField);
        // Menge
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_QUANTITY, false, false);
        text = new EtkMultiSprache("!!Menge", getProject().getConfig().getDatabaseLanguages());
        displayField.setText(text);
        displayField.setDefaultText(false);
        displayResultFields.add(displayField);
        // ME ??
        displayField = new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_QUANTUNIT, false, false);
        displayResultFields.add(displayField);
        // KEM ab
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_KEM_FROM, false, false);
        displayResultFields.add(displayField);
        // Datum ab
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_SDA, false, false);
        displayResultFields.add(displayField);
        // KEM bis
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_KEM_TO, false, false);
        displayResultFields.add(displayField);
        // Datum bis
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_SDB, false, false);
        displayResultFields.add(displayField);

        return displayResultFields;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().getActiveRelatedSubForm() == this) {
            currentPartListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getConnector().getProject());
            dataToGrid();
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return panelMain;
    }

    private void onLevelChangedEvent(Event event) {
        maxLevels = panelMain.intspinnerLevel.getValue();
        dataToGrid();
    }

    private class StackElem {

        int ebene;
        int lfdNr;
        iPartsDataConstKitContentList dataConstKitContentList;

        public StackElem(int ebene, int lfdNr, iPartsDataConstKitContentList dataConstKitContentList) {
            this.ebene = ebene;
            this.lfdNr = lfdNr;
            this.dataConstKitContentList = dataConstKitContentList;
        }

        public String getEbeneValue() {
            return StrUtils.prefixStringWithCharsUpToLength(Integer.toString(ebene + 1), '0', 2, false);
        }
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelNorth;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelPart;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldPart;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelMaxLevel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner intspinnerLevel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelCenter;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelSouth;

        private PanelMainClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(panelMainLayout);
            panelNorth = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelNorth.setName("panelNorth");
            panelNorth.__internal_setGenerationDpi(96);
            panelNorth.registerTranslationHandler(translationHandler);
            panelNorth.setScaleForResolution(true);
            panelNorth.setMinimumWidth(10);
            panelNorth.setMinimumHeight(10);
            panelNorth.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelNorthLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelNorth.setLayout(panelNorthLayout);
            labelPart = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelPart.setName("labelPart");
            labelPart.__internal_setGenerationDpi(96);
            labelPart.registerTranslationHandler(translationHandler);
            labelPart.setScaleForResolution(true);
            labelPart.setMinimumWidth(10);
            labelPart.setMinimumHeight(10);
            labelPart.setText("!!Teil");
            labelPart.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelPartConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 4, 4);
            labelPart.setConstraints(labelPartConstraints);
            panelNorth.addChild(labelPart);
            textfieldPart = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldPart.setName("textfieldPart");
            textfieldPart.__internal_setGenerationDpi(96);
            textfieldPart.registerTranslationHandler(translationHandler);
            textfieldPart.setScaleForResolution(true);
            textfieldPart.setMinimumWidth(100);
            textfieldPart.setMinimumHeight(10);
            textfieldPart.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldPartConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 4, 4, 4);
            textfieldPart.setConstraints(textfieldPartConstraints);
            panelNorth.addChild(textfieldPart);
            labelName = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelName.setName("labelName");
            labelName.__internal_setGenerationDpi(96);
            labelName.registerTranslationHandler(translationHandler);
            labelName.setScaleForResolution(true);
            labelName.setMinimumWidth(10);
            labelName.setMinimumHeight(10);
            labelName.setText("!!Benennung");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 4, 4, 4);
            labelName.setConstraints(labelNameConstraints);
            panelNorth.addChild(labelName);
            textfieldName = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldName.setName("textfieldName");
            textfieldName.__internal_setGenerationDpi(96);
            textfieldName.registerTranslationHandler(translationHandler);
            textfieldName.setScaleForResolution(true);
            textfieldName.setMinimumWidth(200);
            textfieldName.setMinimumHeight(10);
            textfieldName.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 4, 4);
            textfieldName.setConstraints(textfieldNameConstraints);
            panelNorth.addChild(textfieldName);
            labelMaxLevel = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelMaxLevel.setName("labelMaxLevel");
            labelMaxLevel.__internal_setGenerationDpi(96);
            labelMaxLevel.registerTranslationHandler(translationHandler);
            labelMaxLevel.setScaleForResolution(true);
            labelMaxLevel.setMinimumWidth(10);
            labelMaxLevel.setMinimumHeight(10);
            labelMaxLevel.setText("!!maximale Ebene");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelMaxLevelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "n", 4, 0, 4, 4);
            labelMaxLevel.setConstraints(labelMaxLevelConstraints);
            panelNorth.addChild(labelMaxLevel);
            intspinnerLevel = new de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner();
            intspinnerLevel.setName("intspinnerLevel");
            intspinnerLevel.__internal_setGenerationDpi(96);
            intspinnerLevel.registerTranslationHandler(translationHandler);
            intspinnerLevel.setScaleForResolution(true);
            intspinnerLevel.setMinimumWidth(10);
            intspinnerLevel.setMinimumHeight(10);
            intspinnerLevel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onLevelChangedEvent(event);
                }
            });
            intspinnerLevel.setMinValue(1);
            intspinnerLevel.setMaxValue(10);
            intspinnerLevel.setValue(4);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag intspinnerLevelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            intspinnerLevel.setConstraints(intspinnerLevelConstraints);
            panelNorth.addChild(intspinnerLevel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelNorthConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelNorthConstraints.setPosition("north");
            panelNorth.setConstraints(panelNorthConstraints);
            this.addChild(panelNorth);
            panelCenter = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelCenter.setName("panelCenter");
            panelCenter.__internal_setGenerationDpi(96);
            panelCenter.registerTranslationHandler(translationHandler);
            panelCenter.setScaleForResolution(true);
            panelCenter.setMinimumWidth(10);
            panelCenter.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelCenterLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelCenter.setLayout(panelCenterLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelCenterConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelCenter.setConstraints(panelCenterConstraints);
            this.addChild(panelCenter);
            panelSouth = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelSouth.setName("panelSouth");
            panelSouth.__internal_setGenerationDpi(96);
            panelSouth.registerTranslationHandler(translationHandler);
            panelSouth.setScaleForResolution(true);
            panelSouth.setMinimumWidth(10);
            panelSouth.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelSouthLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelSouth.setLayout(panelSouthLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelSouthConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelSouthConstraints.setPosition("south");
            panelSouth.setConstraints(panelSouthConstraints);
            this.addChild(panelSouth);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}