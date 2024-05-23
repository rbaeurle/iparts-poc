/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataES1;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsES1Id;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCountryInvalidPartsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCountryValidSeriesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Anzeige der Alternativ-Materialien in Related Info Dialog (Stichworte: Primus, ES1, ES2)
 */
public class iPartsRelatedInfoAlternativeMaterialsForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_ALTERNATIVE_PARTS = "iPartsMenuItemShowAlternativeMaterials";
    public static final String CONFIG_KEY_ALTERNATIVE_PARTS = "Plugin/iPartsEdit/AlternativeMaterials";

    private PartListEntryId loadedPartListEntryId;
    private EtkDataPartListEntry partListEntry;

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_ALTERNATIVE_PARTS, "!!Alternativteile anzeigen",
                                EditDefaultImages.edit_alternative_materials.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_ALTERNATIVE_PARTS);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        boolean menuItemVisible = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            menuItemVisible = relatedInfoIsVisible(connector.getSelectedPartListEntries().get(0), AbstractRelatedInfoPartlistDataForm.isEditContext(connector, false));
        }

        // Im Edit nur Menüeinträge zur Bearbeitung anzeigen
        if (menuItemVisible && isEditContext(connector, true)) {
            menuItemVisible = false;
        }

        // Separator und Menüeintrag aktualisieren
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_ALTERNATIVE_PARTS, menuItemVisible);
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry entry, boolean editMode) {
        if ((entry != null) && relatedInfoIsVisible(entry.getOwnerAssembly(), iPartsModuleTypes.getRetailModuleTypes())) {
            String modelNumber = null;
            String country = null;
            if (!editMode) {
                // Gültige Länder für den Baumuster-Präfix
                AbstractDataCard dataCard = iPartsFilter.get().getCurrentDataCard();
                if (dataCard instanceof VehicleDataCard) {
                    modelNumber = dataCard.getModelNo();
                    country = ((VehicleDataCard)dataCard).getCountry();
                }
            }
            return ((iPartsDataPartListEntry)entry).hasAlternativeParts(modelNumber, country);
        }
        return false;
    }


    /**
     * Erzeugt ein neues RelatedInfoForm für die Anzeige der Primus Zusatzmaterialien
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsRelatedInfoAlternativeMaterialsForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                     IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, CONFIG_KEY_ALTERNATIVE_PARTS, null);
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        PartListEntryId currentPartListEntryId = getConnector().getRelatedInfoData().getAsPartListEntryId();
        if ((getConnector().getActiveRelatedSubForm() == this) && !Utils.objectEquals(currentPartListEntryId, loadedPartListEntryId)) {
            loadedPartListEntryId = currentPartListEntryId;
            partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());

            dataToGrid();
        }
    }

    protected void dataToGrid() {
        if (grid.getDisplayFields() == null) {
            grid.setDisplayFields(getDisplayFields());
        }

        grid.clearGrid();
        List<DBDataObjectList<? extends EtkDataObject>> dataObjectListList = createMultipleDataObjectList();
        if (dataObjectListList != null) {
            for (DBDataObjectList dataObjectList : dataObjectListList) {
                grid.addObjectToGrid(dataObjectList.getAsList());
            }
        }
    }

    /**
     * Funktion, die die DEFAULT-Spalten des Ergebnis-Grids festlegt, falls niemand die Spalten über die Workbench konfiguriert.
     */
    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<>();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_MAT, iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR, false, false);
        displayField.setColumnFilterEnabled(true);
        displayField.setDefaultText(false); // ansonsten würde ein hier gesetzter Text überschrieben weil weiter unten loadStandards() aufgerufen wird
        displayField.setText(new EtkMultiSprache("!!Teilenummer", new String[]{ TranslationHandler.getUiLanguage() }));
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_1, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_2, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_ES1, FIELD_DES_TYPE, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_ES1, iPartsDataVirtualFieldsDefinition.DES_VALID_COUNTRIES_FOR_MODEL_PREFIX,
                                           false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_ES1, iPartsDataVirtualFieldsDefinition.DES_INVALID_COUNTRIES_FOR_PART,
                                           false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    /**
     * Erzeugt die darzustellenden Daten in Form einer {@link DBDataObjectList} bestehend aus {@link EtkDataObject}s.
     *
     * @return
     */
    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        return null; // siehe createMultipleDataObjectList()
    }

    /**
     * Erzeugt eine Liste von {@link DBDataObjectList}s mit den darzustellenden Daten (jeweils ein Listeneintrag pro Tabelle
     * bzw. {@link EtkDataObject}-Typ). Diese Methode hat Vorrang vor {@link #createDataObjectList()}. Beim Überschreiben
     * dieser Methode muss {@link #createDataObjectList()} trotzdem überschrieben werden (weil abstrakt), kann aber {@code null}
     * zurückliefern, weil diese dann nicht benötigt wird.
     *
     * @return
     */
    @Override
    protected List<DBDataObjectList<? extends EtkDataObject>> createMultipleDataObjectList() {
        if (loadedPartListEntryId != null) {
            // Gültige Länder für den Baumuster-Präfix
            String modelNumber = null;
            String modelNumberForFilter = null;
            String countryForFilter = null;
            AbstractDataCard dataCard = iPartsFilter.get().getCurrentDataCard();
            if (dataCard instanceof VehicleDataCard) {
                modelNumber = dataCard.getModelNo();
                if (!isEditContext(getConnector(), false)) {
                    modelNumberForFilter = modelNumber;
                    countryForFilter = ((VehicleDataCard)dataCard).getCountry();
                }
            }

            List<DBDataObjectList<? extends EtkDataObject>> dataObjectsAllRows = new ArrayList<>();
            Set<EtkDataPart> alternativeParts = ((iPartsDataPartListEntry)partListEntry).getAlternativeParts(modelNumberForFilter,
                                                                                                             countryForFilter, false);
            if (alternativeParts != null) {
                iPartsCountryValidSeriesCache countryValidSeriesCache = iPartsCountryValidSeriesCache.getInstance(getProject());
                iPartsCountryInvalidPartsCache countryInvalidPartsCache = iPartsCountryInvalidPartsCache.getInstance(getProject());
                for (EtkDataPart alternativePart : alternativeParts) {
                    // iPartsDataES1 herauslösen
                    DBDataObjectList dataObjectList = alternativePart.getChildren(TABLE_DA_ES1);
                    iPartsDataES1 dataES1 = null;
                    if (dataObjectList != null) {
                        dataES1 = (iPartsDataES1)dataObjectList.get(0);
                    }
                    if (dataES1 == null) {
                        dataES1 = new iPartsDataES1(getProject(), new iPartsES1Id());
                    }
                    if (!dataES1.existsInDB()) {
                        dataES1.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    }

                    // Virtuelle Felder für StarParts-Gültigkeiten bestimmen und setzen
                    String validCountriesForModelPrefix;
                    String invalidCountriesForPart;
                    if (dataES1.isSecondPart()) {
                        if (StrUtils.isValid(modelNumber)) {
                            Set<String> validCountryCodesForModelPrefix = countryValidSeriesCache.getValidCountryCodesForModelPrefix(modelNumber);
                            validCountriesForModelPrefix = modelNumber + ": " + StrUtils.stringListToString(validCountryCodesForModelPrefix, ", ");
                        } else {
                            validCountriesForModelPrefix = TranslationHandler.translate("!![ Kein Fahrzeug-Baumuster im Filter ]");
                        }

                        // Ungültige Länder für die Teilenummer
                        Set<String> invalidCountryCodesForPart = countryInvalidPartsCache.getInvalidCountryCodesForPart(partListEntry.getPart().getAsId().getMatNr());
                        invalidCountriesForPart = StrUtils.stringListToString(invalidCountryCodesForPart, ", ");
                    } else {
                        validCountriesForModelPrefix = TranslationHandler.translate("!!N/A");
                        invalidCountriesForPart = TranslationHandler.translate("!!N/A");
                    }
                    dataES1.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DES_VALID_COUNTRIES_FOR_MODEL_PREFIX,
                                                     validCountriesForModelPrefix, true, DBActionOrigin.FROM_DB);
                    dataES1.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DES_INVALID_COUNTRIES_FOR_PART,
                                                     invalidCountriesForPart, true, DBActionOrigin.FROM_DB);

                    DBDataObjectList dataObjectsOneRow = new DBDataObjectList();
                    dataObjectsOneRow.add(alternativePart, DBActionOrigin.FROM_DB);
                    dataObjectsOneRow.add(dataES1, DBActionOrigin.FROM_DB);
                    dataObjectsAllRows.add(dataObjectsOneRow);
                }
            }
            return dataObjectsAllRows;
        }
        return null;
    }

    /**
     * Related Info Icon ermitteln
     *
     * @return
     */
    public static AssemblyListCellContentFromPlugin getRelatedInfoIcon() {
        AssemblyListCellContentFromPlugin iconInfo = new AssemblyListCellContentFromPlugin(iPartsConst.CONFIG_KEY_RELATED_INFO_ALTERNATIVE_PARTS,
                                                                                           EditDefaultImages.edit_alternative_materials.getImage());
        iconInfo.setHint(iPartsConst.RELATED_INFO_ALTERNATIVE_PARTS_TEXT);
        iconInfo.setCursor(DWCursor.Hand);
        iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
        return iconInfo;
    }

}