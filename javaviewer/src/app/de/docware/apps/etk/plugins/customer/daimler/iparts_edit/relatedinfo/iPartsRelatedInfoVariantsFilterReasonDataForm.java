/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.AbstractRelatedInfoMainForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsColorTable;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

/**
 * Anzeige der ungefilterten Farbvariantentabellen und Farbvarianten mit Filtergrund
 */
public class iPartsRelatedInfoVariantsFilterReasonDataForm extends iPartsRelatedInfoVariantsToPartDataForm {

    private iPartsDataPartListEntry partListEntryForFilterReason;
    private iPartsColorTable colorTableWithFilterReason;

    protected iPartsRelatedInfoVariantsFilterReasonDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                            AbstractRelatedInfoMainForm relatedInfoMainForm,
                                                            IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfoMainForm, relatedInfo,
              CONFIG_KEY_VARIANTS_TO_PART_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              "!!Ungefilterte Variantentabellen:",
              CONFIG_KEY_COLOR_VARIANTS_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              "!!Ungefilterte Varianten zu Variantentabelle:",
              false);
        getCheckboxShowHistory().switchOffEventListeners();
        getCheckboxRetailFilter().switchOffEventListeners();
        setCheckboxRetailFilterVisible(false);
        setCheckboxShowHistoryVisible(false);
        setRetailFilter(true);
        setShowHistory(false);
        getCheckboxShowHistory().switchOnEventListeners();
        getCheckboxRetailFilter().switchOnEventListeners();
        String colorTableText = TranslationHandler.translate("!!Filterabsicherung für Variantentabellen");
        setWindowTitle(colorTableText, colorTableText);
    }

    @Override
    protected EtkDisplayFields getDisplayFields(boolean top) {
        EtkDisplayFields displayFields = new EtkDisplayFields();

        // Virtuelle Felder für den Grund der Ausfilterung von Farbvariantentabellen bzw. Farbvarianten als erste Felder hinzufügen
        String tableName = top ? iPartsConst.TABLE_DA_COLORTABLE_PART : iPartsConst.TABLE_DA_COLORTABLE_CONTENT;
        addVirtualFilterReasonField(tableName, iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, displayFields, 10);
        addVirtualFilterReasonField(tableName, iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTER_NAME, displayFields, 20);
        EtkDisplayField filterReasonDescriptionField = addVirtualFilterReasonField(tableName, iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_DESCRIPTION,
                                                                                   displayFields, 50);
        filterReasonDescriptionField.setGrowColumn(true);

        // Normale Felder hinzufügen
        displayFields.addFelder(super.getDisplayFields(top));
        return displayFields;
    }

    private EtkDisplayField addVirtualFilterReasonField(String tableName, String virtualFieldName, EtkDisplayFields displayFields,
                                                        int width) {
        EtkDisplayField displayField = new EtkDisplayField(tableName, virtualFieldName, false, false);
        displayField.loadStandards(getProject().getConfig());
        displayField.setDefaultWidth(false);
        displayField.setWidth(width);
        displayField.setColumnFilterEnabled(true);
        displayField.setInFlyerAnzeigen(true);
        displayField.setInRelatedInfoAnzeigen(true);
        displayFields.addFeld(displayField);
        return displayField;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (forceUpdateAll) {
            partListEntryForFilterReason = null;
        }
        super.updateData(sender, forceUpdateAll);
    }

    @Override
    protected iPartsColorTable getFilteredColorTableForRetail(iPartsDataPartListEntry partListEntry) {
        if (partListEntryForFilterReason != partListEntry) {
            partListEntryForFilterReason = partListEntry;
            iPartsColorTable colorTableForRetailWithoutFilter = partListEntry.getColorTableForRetailWithoutFilter();
            if (colorTableForRetailWithoutFilter != null) {
                // Farbvariantentabellen mit Filtergrund filtern (falls ein Filter aktiv ist)
                iPartsFilter filterForFilterReason = new iPartsFilter();
                iPartsFilter realFilter = iPartsFilter.get();
                filterForFilterReason.setCurrentDataCard(realFilter.getCurrentDataCard(), getProject());
                filterForFilterReason.setWithFilterReason(true);
                filterForFilterReason.setSwitchboardState(realFilter.getSwitchboardState());
                if (filterForFilterReason.isFilterActive()) {
                    colorTableWithFilterReason = filterForFilterReason.getColorTableForRetailFiltered(colorTableForRetailWithoutFilter.cloneMe(),
                                                                                                      partListEntry);
                } else {
                    colorTableWithFilterReason = colorTableForRetailWithoutFilter;
                }
            } else {
                colorTableWithFilterReason = null;
            }
        }
        return colorTableWithFilterReason;
    }

    @Override
    protected boolean isRetailFilterForFactoryData() {
        return false; // Werkseinsatzdaten immer ungefiltert anzeigen in der Filterabsicherung
    }
}