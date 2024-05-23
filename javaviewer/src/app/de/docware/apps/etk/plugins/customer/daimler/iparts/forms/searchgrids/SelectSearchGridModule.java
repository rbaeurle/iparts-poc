/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Suchformular für Suche nach Modulen in der Tabelle {@link iPartsConst#TABLE_DA_MODULE} (iParts spezifisch)
 */
public class SelectSearchGridModule extends SimpleSelectSearchResultGrid implements iPartsConst {

    protected final boolean isPSKAllowed = iPartsRight.checkPSKInSession();
    protected final boolean hasBothCarAndTruckRights = iPartsRight.checkUserHasBothVehicleTypeRightsInSession();
    protected final boolean hasNeitherCarNorTruckRights = !iPartsRight.checkCarAndVanInSession() && !iPartsRight.checkTruckAndBusInSession();
    protected final HashMap<iPartsProductId, Boolean> productValidityMap;

    public SelectSearchGridModule(AbstractJavaViewerForm parentForm) {
        super(parentForm.getConnector(), parentForm, TABLE_DA_MODULE, FIELD_DM_MODULE_NO);
        setTitle("!!Modul auswählen");
        setDisplayResultFields(createDisplayResultFields());
        setSortTableByField(TableAndFieldName.make(TABLE_DA_MODULE, FIELD_DM_MODULE_NO));
        setAutoSelectSingleSearchResult(true);
        setMinCharForSearch(2);
        productValidityMap = new HashMap<>();
    }

    protected EtkDisplayFields createDisplayResultFields() {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(getProject(), TABLE_DA_MODULE, FIELD_DM_MODULE_NO, "!!Modulnummer", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(getProject(), TABLE_MAT, FIELD_M_TEXTNR, "!!Benennung", true, false, true));
        return displayResultFields;
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
        dataModuleList.searchSortAndFillWithJoin(getProject(), null, getSelectFields(),
                                                 new String[]{ TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR),
                                                               TableAndFieldName.make(TABLE_MAT, FIELD_M_ASSEMBLY) },
                                                 new String[]{ searchValue, SQLStringConvert.booleanToPPString(true) },
                                                 false, new String[]{ FIELD_DM_MODULE_NO }, !isRevisionChangeSetActive(),
                                                 new boolean[]{ true, false }, false, true, false, foundAttributesCallback, true,
                                                 new EtkDataObjectList.JoinData(TABLE_MAT,
                                                                                new String[]{ FIELD_DM_MODULE_NO },
                                                                                new String[]{ FIELD_M_MATNR },
                                                                                false, false));
        return true;
    }

    /**
     * Filtert das gefundene Modul in Abhängigkeit zu den Benutzereigenschaften in der aktuellen Session und fügt es
     * bei Erfolg zur Ergebnisliste hinzu.
     *
     * @param attributes
     * @param moduleToProductMap
     * @param processedRecords
     * @param session
     * @return
     */
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
            session.invokeThreadSafe(() -> processedRecords.setValue(processedRecords.getValue() + processResultAttributes(attributes)));
        }
    }

    /**
     * Bestimmt die Produkte zu den übergebenen Modulen aus der Datenbank und gibt eine Map mit Modul auf Produkt zurück
     *
     * @param searchValue
     * @return
     */
    protected Map<String, String> loadProductForModules(String searchValue) {
        EtkDataPartList dataParts = new EtkDataPartList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_MODULE_NO, false, false));
        selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PRODUCT_NO, false, false));
        dataParts.searchSortAndFillWithJoin(getProject(), null, selectFields,
                                            new String[]{ TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR),
                                                          TableAndFieldName.make(TABLE_MAT, FIELD_M_ASSEMBLY) },
                                            new String[]{ searchValue, SQLStringConvert.booleanToPPString(true) },
                                            false, new String[]{ FIELD_M_BESTNR }, false, new boolean[]{ true, false },
                                            false, true, false, null, true,
                                            new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT_MODULES,
                                                                           new String[]{ FIELD_M_BESTNR },
                                                                           new String[]{ FIELD_DPM_MODULE_NO },
                                                                           false, false));

        return dataParts.getAsList()
                .stream()
                .collect(Collectors.toMap(key -> key.getFieldValue(FIELD_DPM_MODULE_NO),
                                          value -> value.getFieldValue(FIELD_DPM_PRODUCT_NO),
                                          (o1, o2) -> o1, HashMap::new));
    }

    @Override
    protected EtkDisplayFields getSelectFields() {
        // Doku-Methode wird für die PSK-Abfrage benötigt
        EtkDisplayFields selectFields = new EtkDisplayFields(getDisplayResultFields());
        selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_DA_MODULE, FIELD_DM_DOCUTYPE, false, false));
        selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false));
        selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_MAT, FIELD_M_VER, false, false));
        return selectFields;
    }

    @Override
    protected void beforeSearchStarted() {
        super.beforeSearchStarted();
        productValidityMap.clear();
    }

    /**
     * spezielle Abfrage für Benennung aus Table Material
     *
     * @param fieldName
     * @param fieldValue
     * @param isMultiLanguage
     * @return
     */
    @Override
    protected String getVisualValueOfFieldValue(String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
        if (isMultiLanguage) {
            setAttributeToMultiLang(fieldValue, TABLE_MAT); // Modulbenennungen werden aus der Material-Tabelle heraus referenziert
        }
        return getVisObject().asHtml(searchTable, fieldName, fieldValue, getProject().getDBLanguage(), true).getStringResult();
    }

    @Override
    protected void endSearch() {
        super.endSearch();
    }
}
