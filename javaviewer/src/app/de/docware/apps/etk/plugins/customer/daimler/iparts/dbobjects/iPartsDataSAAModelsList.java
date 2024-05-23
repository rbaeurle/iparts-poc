/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.sql.TableAndFieldName;

import java.util.Set;
import java.util.TreeSet;

/**
 * DataObjectList für {@link iPartsDataSAAModels}
 */
public class iPartsDataSAAModelsList extends EtkDataObjectList<iPartsDataSAAModels> implements iPartsConst {

    public iPartsDataSAAModelsList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert die Liste der {@link iPartsProductId}s zu den gültigen BM der SAA/BK
     *
     * @param project
     * @param saaId
     * @param includePSKProducts Flag, ob PSK-Produkte auch berücksichtigt werden sollen
     * @return
     */
    public static Set<iPartsProductId> loadAllProductsForSaaBk(EtkProject project, iPartsSaaId saaId, boolean includePSKProducts) {
        iPartsDataSAAModelsList list = new iPartsDataSAAModelsList();
        list.loadProductsDataSAAModelsListForSaaBKFromDB(project, saaId, DBActionOrigin.FROM_DB);
        Set<iPartsProductId> productList = new TreeSet<>();
        for (iPartsDataSAAModels dataSAAModels : list) {
            iPartsProductId productId = new iPartsProductId(dataSAAModels.getFieldValue(FIELD_DPM_PRODUCT_NO));
            if (!includePSKProducts && iPartsProduct.getInstance(project, productId).isPSK()) {
                continue; // PSK-Produkt nicht berücksichtigen
            }
            productList.add(productId);
        }
        return productList;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataSAAModels} für die angegebene SAA
     *
     * @param project
     * @param saaId
     * @return
     */
    public static iPartsDataSAAModelsList loadDataSAAModelsListForSAA(EtkProject project, iPartsSaaId saaId) {
        iPartsDataSAAModelsList list = new iPartsDataSAAModelsList();
        list.loadDataSAAModelsListForSAAFromDB(project, saaId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataSAAModels} für das angegebene Fahrzeugbaumuster
     *
     * @param project
     * @param modelId
     * @return
     */
    public static iPartsDataSAAModelsList loadDataSAAModelsListForModel(EtkProject project, iPartsModelId modelId) {
        iPartsDataSAAModelsList list = new iPartsDataSAAModelsList();
        list.loadDataSAAModelsListForModelFromDB(project, modelId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadProductsDataSAAModelsListForSaaBKFromDB(EtkProject project, iPartsSaaId saaId, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = new EtkDisplayFields();
        EtkDisplayField selectField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO, true, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SOURCE, false, false);
        selectFields.addFeld(selectField);

        selectField = new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_NO, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_VISIBLE, false, false);
        selectFields.addFeld(selectField);

        String[] whereTableAndFields;
        String[] whereValues;
        whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO) };
        whereValues = new String[]{ saaId.getSaaNumber() };

        searchSortAndFillWithJoin(project, project.getDBLanguage(),
                                  selectFields,
                                  new String[]{ FIELD_DA_ESM_MODEL_NO },
                                  TABLE_DA_PRODUCT_MODELS,
                                  new String[]{ FIELD_DPM_MODEL_NO },
                                  false,
                                  false,
                                  whereTableAndFields, whereValues,
                                  false,
                                  new String[]{ FIELD_DPM_PRODUCT_NO }, false);
    }

    /**
     * Lädt alle {@link iPartsDataSAAModels} aus der DB für die angegebene SAA
     *
     * @param project
     * @param saaId
     * @param origin
     */
    private void loadDataSAAModelsListForSAAFromDB(EtkProject project, iPartsSaaId saaId, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_EDS_SAA_MODELS, new String[]{ FIELD_DA_ESM_SAA_NO }, new String[]{ saaId.getSaaNumber() },
                          new String[]{ FIELD_DA_ESM_MODEL_NO }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataSAAModels} aus der DB für das angegebene Fahrzeugbaumuster
     *
     * @param project
     * @param modelId
     * @param origin
     */
    private void loadDataSAAModelsListForModelFromDB(EtkProject project, iPartsModelId modelId, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_EDS_SAA_MODELS, new String[]{ FIELD_DA_ESM_MODEL_NO }, new String[]{ modelId.getModelNumber() },
                          new String[]{ FIELD_DA_ESM_SAA_NO }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle SAAs und BKs zum übergebenen Baumuster. Zusatzinformationen wie z.B. Benennungen werden über Joins aus
     * den jeweiligen Tabellen geladen und in virtuellen Felder abgelegt.
     *
     * @param project
     * @param modelId
     * @return
     */
    public static iPartsDataSAAModelsList loadAllSaasAndBKsForModel(EtkProject project, iPartsModelId modelId) {
        iPartsDataSAAModelsList list = new iPartsDataSAAModelsList();
        list.loadSaasAndBKs(project, modelId);
        return list;
    }

    private void loadSaasAndBKs(final EtkProject project, iPartsModelId modelId) {
        if (modelId != null) {
            final iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            searchSortAndFillWithJoin(project, project.getDBLanguage(), getSelectFieldsForSaaBKJoin(),
                                      new String[]{ TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO) },
                                      new String[]{ modelId.getModelNumber() },
                                      false,
                                      new String[]{ FIELD_DA_ESM_SAA_NO },
                                      false,
                                      new EtkDataObjectList.FoundAttributesCallback() {
                                          @Override
                                          public boolean foundAttributes(DBDataObjectAttributes attributes) {
                                              handleAttributes(attributes, numberHelper);
                                              return true;
                                          }
                                      },
                                      new EtkDataObjectList.JoinData(TABLE_DA_SAA,
                                                                     new String[]{ FIELD_DA_ESM_SAA_NO },
                                                                     new String[]{ FIELD_DS_SAA },
                                                                     true,
                                                                     false),
                                      new EtkDataObjectList.JoinData(TABLE_MAT,
                                                                     new String[]{ FIELD_DA_ESM_SAA_NO },
                                                                     new String[]{ FIELD_M_MATNR },
                                                                     true,
                                                                     false)
            );
        }
    }

    /**
     * Liefert die für den Join auf TABLE_MAT und TABLE_DA_SAA benötigten Select Felder
     *
     * @return
     */
    private EtkDisplayFields getSelectFieldsForSaaBKJoin() {
        EtkDisplayFields selectFields = new EtkDisplayFields();
        // Felder aus der EDS_SAA_MODELS Tabelle
        EtkDisplayField selectField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO, false, false);
        selectFields.addFeld(selectField);
        // Felder aus der DA_SAA Tabelle
        selectField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_SAA, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_DESC, true, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_CONST_DESC, true, false);
        selectFields.addFeld(selectField);
        // Felder aus der MAT Tabelle
        selectField = new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_MAT, FIELD_M_CONST_DESC, true, false);
        selectFields.addFeld(selectField);
        return selectFields;
    }

    /**
     * Verarbeitet die Attribute des aktuellen Datensatzes bei dem JOIN mit TABLE_MAT und TABLE_DA_SAA. Wenn es sich um eine SAA handelt, wird die Benennung
     * aus der TABLE_DA_SAA verwendet. Ansonsten wird die Benennung aus der MAT Tabelle herangezogen (z.B. bei BKs mit A oder N Sachnummern)
     *
     * @param attributes
     * @param numberHelper
     */
    private void handleAttributes(DBDataObjectAttributes attributes, iPartsNumberHelper numberHelper) {
        EtkMultiSprache multiDesc;
        EtkMultiSprache multiDescConst;
        String number = attributes.getField(FIELD_DA_ESM_SAA_NO).getAsString();

        // getAsMultiLanguage(null, false) ist ausreichend, weil alle Sprach-Felder bereits inkl. Rückfallsprachen geladen
        // wurden und das Nachladen der Sprachen pro SA/Baukasten auch viel zu teuer wäre
        if (numberHelper.isValidSaa(number)) {
            multiDesc = attributes.getField(FIELD_DS_DESC).getAsMultiLanguage(null, false);
            multiDescConst = attributes.getField(FIELD_DS_CONST_DESC).getAsMultiLanguage(null, false);
        } else {
            multiDesc = attributes.getField(FIELD_M_TEXTNR).getAsMultiLanguage(null, false);
            multiDescConst = attributes.getField(FIELD_M_CONST_DESC).getAsMultiLanguage(null, false);
        }

        DBDataObjectAttribute attribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
        attribute.setValueAsMultiLanguage(multiDesc, DBActionOrigin.FROM_DB);
        attributes.addField(attribute, DBActionOrigin.FROM_DB);
        attribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION_CONST, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
        attribute.setValueAsMultiLanguage(multiDescConst, DBActionOrigin.FROM_DB);
        attributes.addField(attribute, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataSAAModels getNewDataObject(EtkProject project) {
        return new iPartsDataSAAModels(project, null);
    }
}
