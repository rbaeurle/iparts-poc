/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket.iPartsDataWorkBasketCalc;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket.iPartsDataWorkBasketCalcList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAAModelsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataSetCancelable;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.util.CanceledException;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.InnerJoin;

import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Helfer für CTT spezifische Daten
 */
public class iPartsCTTHelper implements iPartsConst {

    /**
     * Check, ob es sich um ein valides CTT Baumuster handelt. Prüfung, ob zum BM mind. eine SAA inkl. Stücklistendaten existiert
     *
     * @param project
     * @param modelId
     * @return
     */
    public static boolean isValidModel(EtkProject project, iPartsModelId modelId) {
        DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        query.selectDistinct(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO));
        query.from(TABLE_DA_EDS_SAA_MODELS);
        query.where(new Condition(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO),
                                  Condition.OPERATOR_EQUALS, modelId.getModelNumber()));
        query.join(new InnerJoin((TABLE_DA_HMO_SAA_MAPPING).toLowerCase(),
                                 new Condition(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO).toLowerCase(),
                                               Condition.OPERATOR_EQUALS,
                                               new Fields(TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_SAA).toLowerCase()))));
        query.join(new InnerJoin((TABLE_DA_EDS_CONST_KIT).toLowerCase(),
                                 new Condition(TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_HMO).toLowerCase(),
                                               Condition.OPERATOR_EQUALS,
                                               new Fields(TableAndFieldName.make(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_SNR).toLowerCase()))));

        query.limit(1);
        try (DBDataSetCancelable dataSet = query.executeQueryCancelable()) { // Query ausführen
            if ((dataSet != null) && dataSet.next()) {
                return true;
            }
        } catch (CanceledException ignored) {
        }
        return false;
    }

    /**
     * Liefert alle möglichen CTT Baumuster, die ein HMO Mapping besitzen
     *
     * @param project
     * @return
     */
    public static Set<iPartsModelId> getAllPossibleModels(EtkProject project) {
        DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        query.selectDistinct(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO));
        query.from(TABLE_DA_EDS_SAA_MODELS);
        query.where(new Condition(FIELD_DA_ESM_MODEL_NO, Condition.OPERATOR_LIKE, SQLUtils.wildcardExpressionToSQLLike(MODEL_NUMBER_PREFIX_AGGREGATE, false, true, false)));
        query.or(new Condition(FIELD_DA_ESM_MODEL_NO, Condition.OPERATOR_LIKE, SQLUtils.wildcardExpressionToSQLLike(MODEL_NUMBER_PREFIX_CAR, false, true, false)));
        query.join(new InnerJoin((TABLE_DA_HMO_SAA_MAPPING).toLowerCase(),
                                 new Condition(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO).toLowerCase(),
                                               Condition.OPERATOR_EQUALS,
                                               new Fields(TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_SAA).toLowerCase()))));

        Set<iPartsModelId> result = new TreeSet<>();
        try (DBDataSetCancelable dataSet = query.executeQueryCancelable()) {
            if (dataSet != null) {
                while (dataSet.next()) {
                    String modelNumber = dataSet.getStringList().get(0);
                    result.add(new iPartsModelId(modelNumber));
                }
            }
        } catch (CanceledException ignored) {
        }
        // Verbindung schließen
        return result;
    }

    /**
     * Lädt alle CTT Baumuster für die übergebene SAA.
     *
     * @param project
     * @param saaNumber
     * @return
     */
    public static DBDataObjectAttributesList loadCTTModelsForSaa(EtkProject project, String saaNumber) {
        String[] fields = new String[]{ FIELD_DA_ESM_MODEL_NO };
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_EDS_SAA_MODELS, fields,
                                                                                           new String[]{ FIELD_DA_ESM_SAA_NO },
                                                                                           new String[]{ saaNumber },
                                                                                           ExtendedDataTypeLoadType.NONE,
                                                                                           false, true);
        attributesList.sort(fields);
        return attributesList;
    }

    /**
     * Lädt alle CTT Baumuster für die übergebene SAA mit Rückversicherung via HMO-Mapping.
     *
     * @param project
     * @param saaNumber
     * @param onlyCarModels true: es werden nur Fahrzeiugbaumuster geliefert
     * @return
     */
    public static DBDataObjectAttributesList loadCTTModelsForSaaWithHmoCheck(EtkProject project, String saaNumber, boolean onlyCarModels) {
        iPartsDataSAAModelsList list = new iPartsDataSAAModelsList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO) };
        String[] whereValues = new String[]{ saaNumber };
        String[] sortFields = new String[]{ FIELD_DA_ESM_MODEL_NO };
        DBDataObjectAttributesList attributesList = new DBDataObjectAttributesList();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                boolean doAdd = true;
                if (onlyCarModels) {
                    String modelNo = attributes.getFieldValue(FIELD_DA_ESM_MODEL_NO);
                    doAdd = modelNo.startsWith(MODEL_NUMBER_PREFIX_CAR);
                }
                if (doAdd) {
                    attributesList.add(attributes);
                }
                return false;
            }
        };
        EtkDataObjectList.JoinData joinData = new EtkDataObjectList.JoinData(TABLE_DA_HMO_SAA_MAPPING, new String[]{ TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO) },
                                                                             new String[]{ FIELD_DHSM_SAA }, false, false);
        list.searchSortAndFillWithJoin(project, null, selectFields, whereTableAndFields, whereValues,
                                       false, null, false,
                                       foundAttributesCallback, joinData);

        // da keine DataObjects erzeugt wurden => Sort nachholen
        attributesList.sort(sortFields);
        return attributesList;
    }

    /**
     * Mapping SAA-Nummer auf HMO
     * Für eine SAA-Nummer kann es mehrere HMO-Nummern geben
     *
     * @param project
     * @return
     */
    public static Map<String, List<String>> getSaaToHmoMapping(EtkProject project) {
        Map<String, List<String>> saaToHmoMapping = new HashMap<>();
        DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        query.select(TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_HMO),
                     TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_SAA));
        query.from(TABLE_DA_HMO_SAA_MAPPING);
        try (DBDataSetCancelable dataSet = query.executeQueryCancelable()) {
            if (dataSet != null) {
                while (dataSet.next()) {
                    String hmoNumber = dataSet.getStringList().get(0);
                    String saaNumber = dataSet.getStringList().get(1);
                    List<String> hmoList = saaToHmoMapping.computeIfAbsent(saaNumber, hmoNumer -> new DwList<>());
                    hmoList.add(hmoNumber);
                }
            }
        } catch (CanceledException ignored) {
        }

        return saaToHmoMapping;
    }

    /**
     * Ermittelt alle SAAs aus der Vorverdichtungstabelle DA_WB_SAA_CALCULATION mit Quelle "CTT"
     *
     * @param project
     * @return
     */
    public static Set<String> getPrecalculatedCTTSaas(EtkProject project) {
        iPartsDataWorkBasketCalcList allPrecalculatedEntries = iPartsDataWorkBasketCalcList.loadWorkBasketBySource(project, iPartsImportDataOrigin.SAP_CTT); //loadAllEntries(project);
        Set<String> precalculatedIds = new HashSet<>();
        for (iPartsDataWorkBasketCalc precalculatedEntry : allPrecalculatedEntries) {
            precalculatedIds.add(precalculatedEntry.getAsId().getSaa());
        }
        return precalculatedIds;
    }

}
