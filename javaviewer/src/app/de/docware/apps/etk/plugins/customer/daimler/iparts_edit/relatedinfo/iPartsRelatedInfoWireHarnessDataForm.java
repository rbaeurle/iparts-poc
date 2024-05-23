/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWireHarness;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWireHarnessList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWireHarnessHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsSPKMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsWireHarness;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.ExtendedRelatedInfoData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.EnumSet;
import java.util.List;

/**
 * Formular für die Anzeige von Leitungssatzbaukästen zu einer Sachnummer innerhalb der RelatedInfo.
 */
public class iPartsRelatedInfoWireHarnessDataForm extends AbstractRelatedInfoPartlistDataForm {

    public static final String IPARTS_MENU_ITEM_SHOW_WIRE_HARNESS = "iPartsMenuItemShowWireHarness";
    public static final String CONFIG_KEY_WIRE_HARNESS_DISPLAYFIELDS = "Plugin/iPartsEdit/WireHarness";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.DialogRetail,
                                                                                   iPartsModuleTypes.PSK_PKW,
                                                                                   iPartsModuleTypes.Dialog_SM_Construction);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_WIRE_HARNESS, "!!Leitungssatzbaukasten anzeigen...",
                                EditDefaultImages.edit_btn_wireHarness.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_WIRE_HARNESS_DATA);
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
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_WIRE_HARNESS, menuItemVisible);
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry partListEntry, AbstractJavaViewerFormIConnector connector) {
        EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
        if (!relatedInfoIsVisible(ownerAssembly, VALID_MODULE_TYPES)) {
            return false;
        }

        iPartsModuleTypes moduleType = iPartsModuleTypes.getType(ownerAssembly.getEbeneName());
        boolean retailMode = !moduleType.isConstructionRelevant() && !(connector.getActiveForm() instanceof EditModuleForm);

        // Retail-Anzeige -> Leitungssatzbaukästen nur dann anzeigen, wenn zu Stücklisten-Eintrag auch ein Baukasten existiert
        boolean isVirtualPartList = iPartsVirtualNode.isVirtualId(ownerAssembly.getAsId());
        if (retailMode) {
            return isProductWithWireHarnessData(connector.getProject(), ownerAssembly) && relatedInfoIsVisibleForRetail(connector, partListEntry);
        } else if (isVirtualPartList) {
            return relatedInfoIsVisibleForConstruction(connector, partListEntry, retailMode);
        } else {
            // Edit -> Leitungssatzbaukästen immer anzeigen, wenn Stücklistentyp passt und nicht ReadOnly ist (wird oben ja schon überprüft)
            if (!AbstractRelatedInfoPartlistDataForm.isEditContext(connector, true)) {
                return relatedInfoIsVisibleForRetail(connector, partListEntry);
            }
        }
        return true;
    }

    /**
     * Check, ob Leitungssätze im Produkt angezeigt werden sollen
     */
    private static boolean isProductWithWireHarnessData(EtkProject project, EtkDataAssembly assembly) {
        if (assembly instanceof iPartsDataAssembly) {
            iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
            if ((productId != null) && productId.isValidId()) {
                return iPartsProduct.getInstance(project, productId).isWireHarnessDataVisible();
            }
        }
        return true;
    }

    /**
     * Überprüft, ob für den übergebenen Stücklisteneintrag ein Leitungssatzbaukasten vorhanden ist und demzufolge
     * die Related Info sichtbar sein bzw. ein Icon in der Stückliste angezeigt werden soll. Zusätzlich fließt der Status
     * des Filters sowie der Edit-Kontext in die Sichtbarkeit der Related Info ein.
     *
     * @param connector
     * @param entry
     * @return
     */
    public static boolean relatedInfoIsVisibleForRetail(AbstractJavaViewerFormIConnector connector, EtkDataPartListEntry entry) {
        if (entry instanceof iPartsDataPartListEntry) {
            EtkDataPart part = entry.getPart();
            // Überprüfung ob Baukasten
            boolean isWireHarnessPart = iPartsWireHarness.getInstance(connector.getProject()).isWireHarness(part.getAsId());
            if (isEditContext(connector, false)) {
                // Im EditContext muss die RelatedInfo immer angezeigt werden (ohne Filterung oder Dummy-Sachnummer)
                return isWireHarnessPart;
            } else if (!iPartsWireHarnessHelper.isWireHarnessFilterActive(iPartsFilter.get(), ((iPartsDataPartListEntry)entry).getOwnerAssembly())) {
                // Wenn der Filter oder die Adminoption deaktiviert wurden, muss die RelatedInfo ebenfalls angezeigt werden
                return isWireHarnessPart;
            } else {
                // Einzelteile eines Leitungssatz-BK dürfen bei aktiven Filter (und Adminoption) nur angezeigt werden,
                // wenn die Stücklistenposition (der eigentliche Leitungssatz) den Wert sonstige-KZ = "LA" gesetzt hat.
                // Deswegen die Prüfung auf sonstige-KZ = "LA". Bei der Dummy-Sachnummer wird sonstige-KZ Wert beim
                // Laden via Cache gesetzt.
                // ET-KZ muss nicht geprüft werde, da hier nur die Positionen reinkommen, die sowieso schon ET-KZ = "E"
                // oder ET-KZ = "K" + sonstige-KZ = "LA" haben.
                return (isWireHarnessPart || iPartsWireHarnessHelper.isWireHarnessDummyPart(part))
                       && part.getFieldValue(iPartsConst.FIELD_M_LAYOUT_FLAG).equals(iPartsWireHarnessHelper.WIRE_HARNESS_ADDITIONAL_CHECK_VALID_FLAG);
            }
        }
        return false;
    }

    /**
     * Überprüft, ob für den übergebenen Konstruktions-Stücklisteneintrag ein Leitungssatzbaukasten vorhanden ist und
     * demzufolge die Related Info sichtbar sein bzw. ein Icon in der Stückliste angezeigt werden soll.
     *
     * @param connector
     * @param entry
     * @param retailMode
     * @return
     */
    private static boolean relatedInfoIsVisibleForConstruction(AbstractJavaViewerFormIConnector connector, EtkDataPartListEntry entry, boolean retailMode) {
        if ((entry instanceof iPartsDataPartListEntry) && !retailMode) {
            // Überprüfung ob Baukasten
            return iPartsWireHarness.getInstance(connector.getProject()).isWireHarness(entry.getPart().getAsId());
        }
        return false;
    }

    /**
     * Related Info Icon wird angezeigt falls Leitungssatzbaukästen für die AS-Sicht oder die Konstruktionsstückliste existieren
     *
     * @param connector
     * @param entry
     * @param isConstruction
     */
    public static AssemblyListCellContentFromPlugin getRelatedInfoIcon(AbstractJavaViewerFormIConnector connector,
                                                                       EtkDataPartListEntry entry, boolean isConstruction) {
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

        // Unterscheidung Retail und Konstruktion
        // Connect Daten im Retail nur anzeigen, wenn das Produkt es zulässt oder wir im Edit sind
        boolean isVisible = isConstruction ? relatedInfoIsVisibleForConstruction(connector, entry, false)
                                           : ((isProductWithWireHarnessData(connector.getProject(), ownerAssembly)
                                               || AbstractRelatedInfoPartlistDataForm.isEditContext(connector, true))
                                              && relatedInfoIsVisibleForRetail(connector, entry));
        if (isVisible) {
            AssemblyListCellContentFromPlugin iconInfo = new AssemblyListCellContentFromPlugin(iPartsConst.CONFIG_KEY_RELATED_INFO_WIRE_HARNESS_DATA,
                                                                                               EditDefaultImages.edit_btn_wireHarness.getImage());
            iconInfo.setHint(iPartsConst.RELATED_INFO_WIRE_HARNESS_TEXT);
            iconInfo.setCursor(DWCursor.Hand);
            iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
            return iconInfo;
        }
        return null;
    }

    private EtkDataPartListEntry currentPartListEntry;
    private DataObjectFilterGrid grid;

    /**
     * Erzeugt eine Instanz von iPartsRelatedInfoConstructionKitDataForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    protected iPartsRelatedInfoWireHarnessDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);

        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        // DataObjectFilterGrid auf panelCenter aufschnappen
        grid = new DataObjectFilterGrid(getConnector(), this);

        grid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        panelMain.panelCenter.addChild(grid.getGui());
    }

    protected void dataToGrid() {
        Object filterAndSortSettings = grid.getFilterAndSortSettings(true);
        grid.clearGrid();
        fillGrid();
        grid.restoreFilterAndSortSettings(filterAndSortSettings);
    }

    protected void fillGrid() {
        if (grid.getDisplayFields() == null) {
            grid.setDisplayFields(getDisplayFields(CONFIG_KEY_WIRE_HARNESS_DISPLAYFIELDS));
        }
        fillTopPanel();
        addWireHarnessComponentsToGrid();
        grid.showNoResultsLabel(grid.getTable().getRowCount() == 0);
    }

    /**
     * Fügt die Einzelteile des aktuellen Leitungssatz-BK dem Grid hinzu
     */
    private void addWireHarnessComponentsToGrid() {
        // Leitungssatzbaukasten laden und dem Grid hinzufügen
        String wireHarnessNumber = currentPartListEntry.getPart().getAsId().getMatNr();
        // Check, ob nur gefilterte angezeigt werden sollen
        List<iPartsDataWireHarness> validWireHarnessContent;
        if (checkFilterWireHarnessComponents()) {
            EtkDisplayFields displayFields = grid.getDisplayFields();
            validWireHarnessContent = iPartsFilterHelper.getFilteredWireHarnessComponent(getProject(), wireHarnessNumber,
                                                                                         displayFields);
            grid.setNoResultsLabelText("!!Keine gültigen Einzelteile!");
        } else {
            validWireHarnessContent = iPartsDataWireHarnessList.loadOneWireHarness(getProject(), wireHarnessNumber,
                                                                                   grid.getDisplayFields()).getAsList();
        }

        // Falls die Option für SPK Mapping Texte aktiv ist, hier den BCTE Key und damit HM/M/SM ermitteln.
        HmMSmId hmMSmId = null;
        iPartsSPKMappingCache spkMappingCache = null; // SPK Mapping Cache für die Baureihe und Lenkung
        String steeringValue = "L";
        if (iPartsPlugin.isUseSPKMapping() && (currentPartListEntry instanceof iPartsDataPartListEntry)) {
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = ((iPartsDataPartListEntry)currentPartListEntry).getDialogBCTEPrimaryKey();
            if (bctePrimaryKey != null) {
                hmMSmId = bctePrimaryKey.getHmMSmId();

                // Lenkung aus den aktuellen Filtereinstellungen auslesen
                // Wenn keine Lenkung angegeben wurde, oder der Lenkungsfilter deaktiviert ist, wird als default "L" verwendet
                iPartsDataAssembly ownerAssembly = ((iPartsDataPartListEntry)currentPartListEntry).getOwnerAssembly();
                if (iPartsFilter.get().isSteeringFilterActive(ownerAssembly)) {
                    String steeringFilterValue = iPartsFilter.get().getSteeringValue();
                    if (!steeringFilterValue.isEmpty()) {
                        steeringValue = steeringFilterValue;
                    }
                }

                spkMappingCache = iPartsSPKMappingCache.getInstance(getProject(), hmMSmId.getSeriesId(), steeringValue);
            }
        }

        // Die Einzelteile als iPartsDataWireHarness und als EtkDataPart Objekte anzeigen
        for (iPartsDataWireHarness dataWireHarness : validWireHarnessContent) {
            // SPK Mapping Texte laden und ersetzen, nur wenn die Option eingeschaltet ist, und es einen gültigen BCTE Schlüssel gibt
            if (hmMSmId != null) {
                String dwhRef = dataWireHarness.getFieldValue(iPartsConst.FIELD_DWH_REF);
                String connectorNo = dataWireHarness.getFieldValue(iPartsConst.FIELD_DWH_CONNECTOR_NO);

                iPartsSPKMappingCache.SPKEntries textEntries = spkMappingCache.getTextEntriesForMapping(hmMSmId, dwhRef, connectorNo, steeringValue);
                if (textEntries != null) {
                    if (textEntries.getLongText() != null) {
                        dataWireHarness.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DWH_CONTACT_ADD_TEXT, textEntries.getLongText(),
                                                                     DBActionOrigin.FROM_DB);
                    }
                    if (textEntries.getShortText() != null) {
                        dataWireHarness.setFieldValue(iPartsConst.FIELD_DWH_REF, textEntries.getShortText(), DBActionOrigin.FROM_DB);
                    }
                }
            }

            EtkDataPart etkDataPart = EtkDataObjectFactory.createDataPart(getProject(), dataWireHarness.getFieldValue(iPartsConst.FIELD_M_MATNR),
                                                                          "");
            dataWireHarness.getAttributes().addField(iPartsConst.FIELD_M_VER, "", DBActionOrigin.FROM_DB);
            etkDataPart.assignAttributes(getProject(), dataWireHarness.getAttributes(), false, DBActionOrigin.FROM_DB);
            etkDataPart.removeForeignTablesAttributes();
            dataWireHarness.removeForeignTablesAttributes();
            grid.addObjectToGrid(dataWireHarness, etkDataPart);
        }
        handleExternalSelection();
    }

    private void handleExternalSelection() {
        if (getConnector().getRelatedInfoData() instanceof ExtendedRelatedInfoData) {
            ExtendedRelatedInfoData relatedInfoData = (ExtendedRelatedInfoData)getConnector().getRelatedInfoData();
            if (StrUtils.isValid(relatedInfoData.getSelectTableAndFieldName()) && !relatedInfoData.getSelectValues().isEmpty()) {
                String fieldName = TableAndFieldName.getFieldName(relatedInfoData.getSelectTableAndFieldName());
                String tableName = TableAndFieldName.getTableName(relatedInfoData.getSelectTableAndFieldName());
                List<IdWithType> selectedIds = new DwList<>();
                for (int rowNo = 0; rowNo < grid.getTable().getRowCount(); rowNo++) {
                    EtkDataObject dataObject = grid.getDataObjectForRowAndTable(rowNo, tableName);
                    if (dataObject != null) {
                        if (dataObject.getAttributes().fieldExists(fieldName)) {
                            String value = dataObject.getFieldValue(fieldName);
                            if (relatedInfoData.getSelectValues().contains(value)) {
                                selectedIds.add(dataObject.getAsId());
                            }
                        }
                    }
                }
                if (!selectedIds.isEmpty()) {
                    grid.setSelectedObjectIds(selectedIds, tableName, true, true);
                }
            }
        }
    }

    /**
     * Check, ob nur die gefilterten Einzelteile des aktuellen Leitungssatz-BK angezeigt werden sollen
     *
     * @return
     */
    private boolean checkFilterWireHarnessComponents() {
        EtkDataAssembly assembly = currentPartListEntry.getOwnerAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly dataAssembly = (iPartsDataAssembly)assembly;
            if (iPartsWireHarnessHelper.isWireHarnessFilterActive(iPartsFilter.get(), dataAssembly)) {
                // In "TU bearbeiten" Einzelteile nicht filtern (egal ob read-only oder nicht)
                return dataAssembly.isRetailPartList() && !isEditContext(getConnector(), false);
            }
        }
        return false;
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
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        List<EtkDisplayField> displayResultFields = new DwList<>();
        EtkDisplayField displayField;
        // Untere Teilenummer
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_WIRE_HARNESS, iPartsConst.FIELD_DWH_SUB_SNR, false, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.add(displayField);
        // Benennung
        displayField = new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR, true, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.add(displayField);
        // ET-KZ
        displayField = new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_ETKZ, false, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.add(displayField);
        // Ref
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_WIRE_HARNESS, iPartsConst.FIELD_DWH_REF, false, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.add(displayField);
        // Stecker
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_WIRE_HARNESS, iPartsConst.FIELD_DWH_CONNECTOR_NO, false, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.add(displayField);
        // Typ
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_WIRE_HARNESS, iPartsConst.FIELD_DWH_SNR_TYPE, false, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.add(displayField);
        // VZK-Text
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_WIRE_HARNESS, iPartsConst.FIELD_DWH_CONTACT_ADD_TEXT, true, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.add(displayField);

        return displayResultFields;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().getActiveRelatedSubForm() == this) {
            setPartlistEntry();
            dataToGrid();
        }
    }

    /**
     * Setzt die aktuelle Stücklistenposition.
     */
    private void setPartlistEntry() {
        currentPartListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getConnector().getProject());
        // Handelt es sich um die Dummy-Sachnummer, muss die echte Position geladen werden (ohne Dummy-Sachnummer)
        if (iPartsWireHarnessHelper.isWireHarnessDummyPart(currentPartListEntry.getPart())) {
            currentPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), currentPartListEntry.getAsId());
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return panelMain;
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