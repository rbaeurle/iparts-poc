/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridModule;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class iPartsSelectSearchGridModule extends SelectSearchGridModule {

    private iPartsProductId productId;

    /**
     * Erzeugt eine Instanz von SelectSearchGridMaterial.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param parentForm
     * @param productId
     */
    public iPartsSelectSearchGridModule(AbstractJavaViewerForm parentForm, iPartsProductId productId) {
        super(parentForm);
        // keine Table-Sortierung, den Text vom Suchfeld anpassen und SuchFeld disablen
        getTable().setDefaultSortOrderAndSort(new int[0]);
        setSearchText("!!Suche für Produkt:");
        enableSearchValue(false);

        this.productId = productId;
    }

    public iPartsProductId productId() {
        return productId;
    }

    public void doStopSearch() {
        endSearch();
    }

    public void doStartSearch() {
        String searchValue = getSearchValue();
        if (StrUtils.isEmpty(searchValue)) {
            if ((productId != null) && productId.isValidId()) {
                setSearchValue(productId.getProductNumber());
            }
        }
    }

    @Override
    protected EtkDisplayFields createDisplayResultFields() {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(getProject(), TABLE_DA_MODULE, FIELD_DM_MODULE_NO, "!!Modulnummer", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(getProject(), TABLE_DA_PRODUCT, FIELD_DP_AGGREGATE_TYPE, "!!Aggregatetyp", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(getProject(), TABLE_MAT, FIELD_M_TEXTNR, "!!Benennung", true, false, true));
        return displayResultFields;
    }

    @Override
    protected EtkDisplayFields getSelectFields() {
        EtkDisplayFields selectFields = super.getSelectFields();
        // zur Sicherheit das "fremde" Feld entfernen
        selectFields.removeField(TABLE_DA_PRODUCT, FIELD_DP_AGGREGATE_TYPE, false);
        return selectFields;
    }


    @Override
    protected boolean executeExplicitSearch(String searchValue) {
        // Länge von searchValue auf die maximale Feldlänge beschränken, um DB-Fehler zu vermeiden
        searchValue = getProject().getConfig().getDBDescription().cutValueIfLongerThanFieldLength(searchValue, TABLE_DA_MODULE,
                                                                                                  FIELD_DM_MODULE_NO);

        Map<String, String> moduleToProduct = loadProductForModules(searchValue); // Produkte laden
        VarParam<Integer> processedRecords = new VarParam<>(0);
        Session session = Session.get();

        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                filterAndAddModulesForRightsInSession(attributes, moduleToProduct, processedRecords, session);
                return false;
            }
        };

        // Join mit der Material-Tabelle, damit auch nur real existierende Module aufgelistet werden; bei aktivem ChangeSet
        // kein addOrderBy verwenden, weil dies Pseudo-Transaktionen provozieren würde -> Daten werden in diesem Fall nachgelagert
        // durch die GuiTable sortiert
        iPartsDataModuleList dataModuleList = new iPartsDataModuleList();
        Set<String> products = new LinkedHashSet<>(moduleToProduct.values());
        // die Module alle Produkte (incl Aggs) bestimmen und anzeigen
        for (String productNo : products) {
            dataModuleList.clear(DBActionOrigin.FROM_DB);
            dataModuleList.searchAllModulesForProduct(getProject(), getSelectFields(), productNo, !isRevisionChangeSetActive(), foundAttributesCallback);
        }
        return true;
    }

    @Override
    protected void filterAndAddModulesForRightsInSession(DBDataObjectAttributes attributes, Map<String, String> moduleToProductMap,
                                                         VarParam<Integer> processedRecords, Session session) {
        if (checkMaxResultsExceeded(processedRecords.getValue())) {
            return;
        }
        iPartsDocumentationType documentationType = iPartsDocumentationType.getFromDBValue(attributes.getFieldValue(FIELD_DM_DOCUTYPE));
        String moduleNumber = attributes.getFieldValue(FIELD_DM_MODULE_NO);
        String productNumber = moduleToProductMap.get(moduleNumber);
        iPartsProductId productId = null;
        if (StrUtils.isValid(productNumber)) {
            productId = new iPartsProductId(productNumber);
        }
        boolean valid = iPartsFilterHelper.isModuleVisibleForUserSession(getProject(), documentationType, isPSKAllowed, hasBothCarAndTruckRights,
                                                                         hasNeitherCarNorTruckRights, moduleNumber, productId,
                                                                         productValidityMap);
        if (valid) {
            // den Produkt-AggregateTyp hinzufügern
            attributes.addField(FIELD_DP_AGGREGATE_TYPE, iPartsProduct.getInstance(getProject(), productId).getAggregateType(), DBActionOrigin.FROM_DB);
            // und anzeigen
            session.invokeThreadSafe(() -> processedRecords.setValue(processedRecords.getValue() + processResultAttributes(attributes)));
        }
    }

    @Override
    protected Map<String, String> loadProductForModules(String searchValue) {
        Map<String, String> moduleToProduct = super.loadProductForModules(searchValue);
        Set<String> products = new HashSet<String>(moduleToProduct.values());
        Set<iPartsProduct> aggProducts = new HashSet<>();
        // Aggregate-Produkte bestimmen und hinten anhängen
        for (String productNo : products) {
            aggProducts.addAll(iPartsProduct.getInstance(getProject(), new iPartsProductId(productNo)).getAggregates(getProject()));
        }
        for (iPartsProduct aggProduct : aggProducts) {
            Map<String, String> aggModuleToProduct = super.loadProductForModules(aggProduct.getAsId().getProductNumber() + "*");
            moduleToProduct.putAll(aggModuleToProduct);
        }
        return moduleToProduct;
    }


    @Override
    protected boolean doValidAttributes(DBDataObjectAttributes attributes) {
        // den CarPerspective-TU aussortoieren
        AssemblyId assemblyId = new AssemblyId(attributes.getFieldValue(FIELD_M_MATNR), "");
        return !EditModuleHelper.isCarPerspectiveAssemblyShort(assemblyId);
    }

    @Override
    protected String getVisualValueOfFieldValue(String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
        if (fieldName.equals(FIELD_DP_AGGREGATE_TYPE)) {
            return getVisObject().asHtml(TABLE_DA_PRODUCT, fieldName, fieldValue, getProject().getDBLanguage(), true).getStringResult();
        }
        return super.getVisualValueOfFieldValue(fieldName, fieldValue, isMultiLanguage);
    }


}
