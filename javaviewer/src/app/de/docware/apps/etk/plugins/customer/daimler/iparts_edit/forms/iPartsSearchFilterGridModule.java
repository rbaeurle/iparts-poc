/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Klasse für die Suche von TUs innerhalb eines Produkts (doStartSearch - einmalig) und aller zugehörigen
 * Aggregate-Produkte.
 * Der Suchbegriff kann nicht geändert werden und via doStartSearch wird die Suche einmalig angestoßen
 * Bei der Ausgabe wird zusätzlich der Aggregate-Typ des Produkts verwendet (virtuelles Feld, damit TableFilter funktioniert)
 */
public class iPartsSearchFilterGridModule extends SimpleMasterDataSearchFilterGrid {

    private final iPartsProductId productId;
    private final HashMap<iPartsProductId, Boolean> productValidityMap;

    public iPartsSearchFilterGridModule(AbstractJavaViewerForm parentForm, iPartsProductId productId) {
        super(parentForm.getConnector(), parentForm, TABLE_DA_MODULE, null);
        // keine Table-Sortierung, den Text vom Suchfeld anpassen und SuchFeld deaktivieren
        getTable().setDefaultSortOrderAndSort(new int[0]);
        this.productId = productId;
        this.productValidityMap = new HashMap<>();

        setDisplayResultFields(createDisplayResultFields());
        setSearchFields(getDefaultSearchFields());
        setEditControlEnabled(FIELD_DM_MODULE_NO, false);
        setButtonStartStopVisible(false);
        setEditAllowed(false);
        setNewAllowed(false);
        setModifyAllowed(false);
        setDeleteAllowed(false);
        showSearchFields(true);
        setMaxResults(-1);
        setMultiSelect(false);
        setTitlePrefix("!!Modulsuche");
        setWindowName("iPartsSearchFilterGridModule");
    }

    public iPartsProductId productId() {
        return productId;
    }

    public void doStartSearch() {
        String searchValue = getSearchValue(TABLE_DA_MODULE, FIELD_DM_MODULE_NO, false);
        if (StrUtils.isEmpty(searchValue)) {
            if ((productId != null) && productId.isValidId()) {
                // Suchwerte setzen und Suche starten
                DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
                searchAttributes.addField(FIELD_DM_MODULE_NO, productId.getProductNumber(), DBActionOrigin.FROM_DB);
                setSearchValues(searchAttributes);
            }
        }
    }

    public void doStopSearch() {
        endSearch();
    }

    protected EtkDisplayFields createDisplayResultFields() {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        EtkDisplayField displayField = EtkDisplayFieldsHelper.createField(getProject(), TABLE_DA_MODULE, FIELD_DM_MODULE_NO, "!!Modulnummer", false, false, true);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.addFeld(displayField);
        displayField = EtkDisplayFieldsHelper.createField(getProject(), TABLE_DA_MODULE, iPartsDataVirtualFieldsDefinition.AGGREGATE_TYPE, "!!Aggregatetyp", false, false, true);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.addFeld(displayField);
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(getProject(), TABLE_MAT, FIELD_M_TEXTNR, "!!Benennung", true, false, true));
        return displayResultFields;
    }

    protected EtkDisplayFields getDefaultSearchFields() {
        EtkDisplayFields searchFields = new EtkDisplayFields();
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            EtkDisplayField searchField = createSearchField(getProject(), TABLE_DA_MODULE, FIELD_DM_MODULE_NO, false, false);
            searchField.setDefaultText(false);
            searchField.setText(new EtkMultiSprache("!!Suche für Produkt:", new String[]{ TranslationHandler.getUiLanguage() }));
            searchFields.addFeld(searchField);
        }
        return searchFields;
    }

    protected void beforeSearchStarted() {
        productValidityMap.clear();
    }

    protected EtkDisplayFields getSelectFields() {
        EtkDisplayFields selectFields = super.getSelectFields();
        selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_DA_MODULE, FIELD_DM_DOCUTYPE, false, false));
        selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false));
        selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_MAT, FIELD_M_VER, false, false));
        selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false));
        return selectFields;
    }

    @Override
    protected boolean executeExplicitSearch() {
        beforeSearchStarted();
        searchModulesForAllProducts();
        return true;
    }

    @Override
    protected void onTableSelectionChanged(Event event) {
        if (onChangeEvent != null) {
            onChangeEvent.onChange();
        }
        super.onTableSelectionChanged(event);
    }

    protected void searchModulesForAllProducts() {
        // Alle Produkte bestimmen: Fahrzeugprodukt mit dem der Dialog geöffnet wurde + alle Aggregate-Produkte zum Fahrzeug
        Set<String> products = new LinkedHashSet<>();
        products.add(productId.getProductNumber());
        products.addAll(iPartsProduct.getInstance(getProject(), productId).getAggregates(getProject()).stream().map(product -> product.getAsId().getProductNumber()).collect(Collectors.toSet()));

        // Die Module, die gefunden werden, müssen zu den Rechten des Benutzers passen. Daher wird das aktuelle
        // Such-Produkt in den CallBack durchgereicht.
        VarParam<String> currentProduct = new VarParam<>();
        VarParam<Integer> processedRecords = new VarParam<>(0);
        Session session = Session.get();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                filterAndAddModulesForRightsInSession(attributes, currentProduct, processedRecords, session);
                return false;
            }
        };
        // Pro Produkt wird eine DB Abfrage gemacht. Es werden alle Module zum Produkt bestimmt und pro Modul wird
        // geprüft, ob das Modul gültig ist.
        iPartsDataModuleList dataModuleList = new iPartsDataModuleList();
        for (String productNo : products) {
            currentProduct.setValue(productNo);
            dataModuleList.clear(DBActionOrigin.FROM_DB);
            dataModuleList.searchAllModulesForProduct(getProject(), getSelectFields(), productNo,
                                                      !isRevisionChangeSetActive(), foundAttributesCallback);
        }
    }

    protected void filterAndAddModulesForRightsInSession(DBDataObjectAttributes attributes, VarParam<String> currentProduct,
                                                         VarParam<Integer> processedRecords, Session session) {
        if (checkMaxResultsExceeded(processedRecords.getValue())) {
            return;
        }
        iPartsDocumentationType documentationType = iPartsDocumentationType.getFromDBValue(attributes.getFieldValue(FIELD_DM_DOCUTYPE));
        String moduleNumber = attributes.getFieldValue(FIELD_DM_MODULE_NO);
        iPartsProductId productId = new iPartsProductId(currentProduct.getValue());
        boolean valid = iPartsFilterHelper.isModuleVisibleForUserSession(getProject(), documentationType, false, false,
                                                                         false, moduleNumber, productId,
                                                                         productValidityMap);
        if (valid) {
            // den Produkt-AggregateTyp hinzufügern
            attributes.addField(iPartsDataVirtualFieldsDefinition.AGGREGATE_TYPE, iPartsProduct.getInstance(getProject(), productId).getAggregateType(), DBActionOrigin.FROM_DB);
            // und anzeigen
            session.invokeThreadSafe(() -> processedRecords.setValue(processedRecords.getValue() + processResultAttributes(attributes)));
        }
    }

    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        // den CarPerspective-TU aussortieren
        AssemblyId assemblyId = new AssemblyId(attributes.getFieldValue(FIELD_M_MATNR), "");
        return !EditModuleHelper.isCarPerspectiveAssemblyShort(assemblyId);
    }
}
