/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkSectionInfo;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.search.model.EtkPartsSearch;
import de.docware.apps.etk.base.search.model.EtkPartsSearchDataset;
import de.docware.apps.etk.base.search.model.EtkSearchModel;
import de.docware.apps.etk.base.search.model.SearchResultPostProcessOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.iPartsDataCardRetrievalHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.util.CanceledException;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * Suche nur in iParts Retail Stücklisten (wird aktuell nur für {@link de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts.iPartsWSSearchPartsEndpoint}
 * verwendet.
 */
public class iPartsRetailPartSearch extends EtkPartsSearch {

    private KgTuId optionalKgTuId;
    private EinPasId optionalEinPasId;
    private String optionalAggregateType;
    private boolean includeAggregates; // auch in Aggregaten suchen? (nur relevant für Fahrzeug-Produkte)
    private boolean includeSAs; // auch in freien SAs suchen?
    private boolean withHierarchyCheck; // Hierarchie überprüfen?
    private boolean fixingPartsLoaded;
    private Collection<iPartsProduct> additionalAggregates; // Liste der zusätzlichen Aggregate, die mit durchsucht werden sollen

    /**
     * Erzeugt eine Retail-Suche mit den angegebenen Suchparametern.
     *
     * @param model
     * @param andOrSearch
     * @param optionalKgTuId
     * @param optionalEinPasId
     * @param optionalAggregateType
     * @param includeAggregates
     * @param includeSAs
     * @param withHierarchyCheck
     * @param additionalAggregates  Nur gültig bei {@code includeAggregates == true}: Optionale Liste der zusätzlichen
     *                              Aggregate, die mit durchsucht werden sollen; bei {@code null} wird in allen Aggregate-Produkte
     *                              gesucht, die für das Fahrzeug-Produkt gültig sind (sofern in einem Fahrzeug-Produkt
     *                              gesucht wird)
     */
    public iPartsRetailPartSearch(EtkSearchModel model, boolean andOrSearch, KgTuId optionalKgTuId, EinPasId optionalEinPasId,
                                  String optionalAggregateType, boolean includeAggregates, boolean includeSAs, boolean withHierarchyCheck,
                                  Collection<iPartsProduct> additionalAggregates) {
        super(model, andOrSearch);
        this.optionalKgTuId = optionalKgTuId;
        this.optionalEinPasId = optionalEinPasId;
        this.optionalAggregateType = optionalAggregateType;
        this.includeAggregates = includeAggregates;
        this.includeSAs = includeSAs;
        this.withHierarchyCheck = withHierarchyCheck;
        this.additionalAggregates = additionalAggregates;
    }

    /**
     * Suche über die Tabellenfelder soll sich nur auf Retailstücklisten erstrecken
     */
    @Override
    protected void searchWithFields(EtkPartsSearchDataset pds) throws CanceledException {
        iPartsSearchDataset ds = (iPartsSearchDataset)pds;
        // pro Eintrag in localSelectFields muss auch ein leerer Eintrag in localSelectValues hinzugefügt werden, da es
        // ansonsten bei !andOrSearch kracht

        // benötigte Felder aus der KATALOG-Tabelle
        addSelectFieldIfNotExists(localSelectFields, localSelectValues,
                                  new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_SA_VALIDITY, false, true));

        // benötigte Felder aus der MAT-Tabelle
        addSelectFieldIfNotExists(localSelectFields, localSelectValues,
                                  new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR, true, false));
        addSelectFieldIfNotExists(localSelectFields, localSelectValues,
                                  new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_BESTFLAG, false, false));

        // benötigte Felder für Suche in Aggregaten
        if (includeAggregates) {
            addSelectFieldIfNotExists(localSelectFields, localSelectValues,
                                      new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT, iPartsConst.FIELD_DP_PRODUCT_NO, false, false));
            addSelectFieldIfNotExists(localSelectFields, localSelectValues,
                                      new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT, iPartsConst.FIELD_DP_AGGREGATE_TYPE, false, false));
        }

        // evtl. zusätzlich benötigte Ergebnisfelder hinzufügen
        for (EtkSectionInfo sectionInfo : model.getGridResultFields().getFields()) {
            addSelectFieldIfNotExists(localSelectFields, localSelectValues, sectionInfo);
        }

        // benötigte Felder für die Bestimmung vom Navigationskontext
        EtkDisplayFields localSelectFieldsWithNavContext = new EtkDisplayFields(localSelectFields);
        List<String> localSelectValuesWithNavContext = new DwList<String>(localSelectValues);
        addSelectFieldIfNotExists(localSelectFieldsWithNavContext, localSelectValuesWithNavContext,
                                  new EtkDisplayField(iPartsConst.TABLE_DA_MODULES_EINPAS, iPartsConst.FIELD_DME_PRODUCT_NO, false, false));
        addSelectFieldIfNotExists(localSelectFieldsWithNavContext, localSelectValuesWithNavContext,
                                  new EtkDisplayField(iPartsConst.TABLE_DA_MODULES_EINPAS, iPartsConst.FIELD_DME_MODULE_NO, false, false));
        addSelectFieldIfNotExists(localSelectFieldsWithNavContext, localSelectValuesWithNavContext,
                                  new EtkDisplayField(iPartsConst.TABLE_DA_MODULES_EINPAS, iPartsConst.FIELD_DME_LFDNR, false, false));
        addSelectFieldIfNotExists(localSelectFieldsWithNavContext, localSelectValuesWithNavContext,
                                  new EtkDisplayField(iPartsConst.TABLE_DA_MODULES_EINPAS, iPartsConst.FIELD_DME_EINPAS_HG, false, false));
        addSelectFieldIfNotExists(localSelectFieldsWithNavContext, localSelectValuesWithNavContext,
                                  new EtkDisplayField(iPartsConst.TABLE_DA_MODULES_EINPAS, iPartsConst.FIELD_DME_EINPAS_G, false, false));
        addSelectFieldIfNotExists(localSelectFieldsWithNavContext, localSelectValuesWithNavContext,
                                  new EtkDisplayField(iPartsConst.TABLE_DA_MODULES_EINPAS, iPartsConst.FIELD_DME_EINPAS_TU, false, false));
        addSelectFieldIfNotExists(localSelectFieldsWithNavContext, localSelectValuesWithNavContext,
                                  new EtkDisplayField(iPartsConst.TABLE_DA_MODULES_EINPAS, iPartsConst.FIELD_DME_SOURCE_KG, false, false));
        addSelectFieldIfNotExists(localSelectFieldsWithNavContext, localSelectValuesWithNavContext,
                                  new EtkDisplayField(iPartsConst.TABLE_DA_MODULES_EINPAS, iPartsConst.FIELD_DME_SOURCE_TU, false, false));

        ds.createSearchInRetailPartlists(((PartListEntryId)getRoot()).getOwnerAssemblyId(), optionalKgTuId, optionalEinPasId,
                                         optionalAggregateType, false, localSelectFieldsWithNavContext, localSelectValuesWithNavContext,
                                         whereFields, whereValues, OrSearch, model.getWildCardSettings(), includeAggregates,
                                         additionalAggregates);

        // Suche in SAs
        if (includeSAs) {
            EtkDisplayFields localSelectFieldsForSAs = new EtkDisplayFields(localSelectFields);
            List<String> localSelectValuesForSAs = new DwList<String>(localSelectValues);
            addSelectFieldIfNotExists(localSelectFieldsForSAs, localSelectValuesForSAs,
                                      new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT_SAS, iPartsConst.FIELD_DPS_PRODUCT_NO, false, false));
            addSelectFieldIfNotExists(localSelectFieldsForSAs, localSelectValuesForSAs,
                                      new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT_SAS, iPartsConst.FIELD_DPS_SA_NO, false, false));
            addSelectFieldIfNotExists(localSelectFieldsForSAs, localSelectValuesForSAs,
                                      new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT_SAS, iPartsConst.FIELD_DPS_KG, false, false));

            ds.createSearchInRetailPartlistsForSAs(((PartListEntryId)getRoot()).getOwnerAssemblyId(), false, localSelectFieldsForSAs,
                                                   localSelectValuesForSAs, whereFields, whereValues, OrSearch, model.getWildCardSettings(),
                                                   includeAggregates, additionalAggregates);
        }

        // try-finally mit ds.close() genügt für diesen kurzen Block, weil die eigentliche Suche erst durch deliverAllResults()
        // gestartet wird
        try {
            EnumSet<SearchResultPostProcessOption> postProcessOptions;
            if (withHierarchyCheck) {
                postProcessOptions = EnumSet.of(SearchResultPostProcessOption.ppoFilterRecords, SearchResultPostProcessOption.ppoModuleCheck,
                                                SearchResultPostProcessOption.ppoModuleCheckWithFilter);
            } else {
                postProcessOptions = EnumSet.of(SearchResultPostProcessOption.ppoFilterRecords);
            }
            deliverAllResults(ds, localSelectFields, localSelectValues, postProcessOptions, OrSearch);
        } finally {
            ds.close();
        }
    }

    private void addSelectFieldIfNotExists(EtkDisplayFields selectFields, List<String> selectValues, EtkDisplayField selectField) {
        if (selectFields.addFeldIfNotExists(selectField)) {
            if (selectField.isMultiLanguage()) { // bei mehrsprachigen Feldern muss die Suchsprache explizit gesetzt werden
                selectField.setLanguage(explicitSearchLanguageCode);
            }
            selectValues.add("");
        }
    }

    @Override
    protected boolean doPostProcessing(EnumSet<SearchResultPostProcessOption> postProcessOptions, EtkDataPartListEntry entry, boolean isAndOr,
                                       EtkDisplayFields selectFields, List<String> selectValues, EtkDisplayFields andOrWhereFields, List<String> andOrValues) {
        if (!fixingPartsLoaded) {
            AbstractDataCard datacard = iPartsFilter.get().getCurrentDataCard();
            if (datacard instanceof VehicleDataCard) {
                // Nachladen der Befestigungsteile macht nur bei einer echten Datenkarte mit FIN Sinn
                VehicleDataCard vehicleDataCard = (VehicleDataCard)datacard;
                if (vehicleDataCard.isDataCardLoaded() && vehicleDataCard.getFinId().isValidId()) {
                    // Prüfen, ob Befestigungsteile per Webservice nachgeladen werden müssen.
                    // Das ist nur dann der Fall, wenn es mindestens einen Eintrag für einen Baukasten (Nicht-Z-Sachnummer) gibt.
                    if (entry instanceof iPartsDataPartListEntry) {
                        if (((iPartsDataPartListEntry)entry).hasConstructionKits()) {
                            iPartsDataCardRetrievalHelper.loadFixingParts(vehicleDataCard, getClass().getSimpleName());
                            fixingPartsLoaded = true;
                        }
                    }
                } else {
                    fixingPartsLoaded = true;
                }
            }
        }

        return super.doPostProcessing(postProcessOptions, entry, isAndOr, selectFields, selectValues, andOrWhereFields, andOrValues);
    }
}
