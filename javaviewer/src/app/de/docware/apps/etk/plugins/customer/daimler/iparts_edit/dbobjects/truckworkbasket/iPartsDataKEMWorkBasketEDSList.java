/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsKEMWorkBasketEDSId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ASUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Liste von {@link iPartsDataKEMWorkBasketEDS}s.
 */
public class iPartsDataKEMWorkBasketEDSList extends EtkDataObjectList<iPartsDataKEMWorkBasketEDS> implements iPartsConst {

    public iPartsDataKEMWorkBasketEDSList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataKEMWorkBasketEDSList loadAllEntries(EtkProject project) {
        iPartsDataKEMWorkBasketEDSList list = new iPartsDataKEMWorkBasketEDSList();
        list.loadAllEntriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert die Liste der AssemblyIds zu einer {@param kemNo} und {@param saaBkNo} mit gültigem Docu-Rel
     *
     * @param project
     * @param kemNo
     * @param saaBkNo
     * @param withDocuRelCheck
     * @return
     */
    public static List<AssemblyId> loadModulesByKEMAndSaaBk(EtkProject project, String kemNo, String saaBkNo,
                                                            boolean withDocuRelCheck) {
        List<AssemblyId> result = new DwList<>();
        iPartsDataKEMWorkBasketEDSList list = new iPartsDataKEMWorkBasketEDSList();
        list.loadModulesByKEMAndSaaBkFromDB(project, kemNo, saaBkNo, DBActionOrigin.FROM_DB);
        for (iPartsDataKEMWorkBasketEDS dataKEMWorkBasket : list) {
            String moduleNo = dataKEMWorkBasket.getAsId().getModuleNo();
            boolean doAdd = StrUtils.isValid(moduleNo);
            if (doAdd && withDocuRelCheck && !iPartsDocuRelevantTruck.canBeTransferredToEdit(dataKEMWorkBasket.getDocuRelevant())) {
                doAdd = false;
            }
            if (doAdd) {
                result.add(dataKEMWorkBasket.getAssemblyId());
            }
        }
        return result;
    }

    /**
     * Überprüft zu einer KemNo-SaaList Map die Verwendung im Retail und erzeugt eine Liste von
     * {@link iPartsDataKEMWorkBasketEDS}s, die in der Tabelle DA_KEM_WORK_BASKET gespeichert werden können
     *
     * @param project
     * @param kemNoSaaMap
     * @return
     */
    public static iPartsDataKEMWorkBasketEDSList checkUsageInRetail(EtkProject project, Map<String, Set<String>> kemNoSaaMap) {
        iPartsDataKEMWorkBasketEDSList list = new iPartsDataKEMWorkBasketEDSList();
        if (!kemNoSaaMap.isEmpty()) {
            kemNoSaaMap.forEach((kemNo, saaList) -> {
                iPartsDataKEMWorkBasketEDSList singleKemlist = checkUsageInRetail(project, kemNo, saaList);
                list.addAll(singleKemlist, DBActionOrigin.FROM_EDIT);
            });
        }
        return list;
    }


    /**
     * Überprüft für eine SAA-Liste der Verwendung im Retail und erzeugt eine Liste von
     * {@link iPartsDataKEMWorkBasketEDS}s, die in der Tabelle DA_KEM_WORK_BASKET gespeichert werden können
     *
     * @param project
     * @param saaList
     * @return
     */
    public static iPartsDataKEMWorkBasketEDSList checkUsageInRetail(EtkProject project, String kemNo, Set<String> saaList) {
        iPartsDataKEMWorkBasketEDSList list = new iPartsDataKEMWorkBasketEDSList();
        if (!saaList.isEmpty()) {
            ASUsageHelper usageHelper = new ASUsageHelper(project);
            for (String saaNumber : saaList) {
                Map<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> kgTuSuggestionCheck3 =
                        usageHelper.getEDSUsageBySaaASPartList(saaNumber);
                if (!kgTuSuggestionCheck3.isEmpty()) {
                    for (iPartsProductId productId : kgTuSuggestionCheck3.keySet()) {
                        List<ASUsageHelper.EDSUsageContainer> containers = kgTuSuggestionCheck3.get(productId);
                        for (ASUsageHelper.EDSUsageContainer container : containers) {
                            iPartsKEMWorkBasketEDSId kemWorkBasketId =
                                    new iPartsKEMWorkBasketEDSId(kemNo, saaNumber, productId.getProductNumber(),
                                                                 container.getKgTuId().getKg(),
                                                                 container.getFirstModuleNumber());
                            addToList(project, kemWorkBasketId, list);
                        }
                    }
                } else {
                    iPartsKEMWorkBasketEDSId kemWorkBasketId = new iPartsKEMWorkBasketEDSId(kemNo, saaNumber);
                    addToList(project, kemWorkBasketId, list);
                }
            }
        } else {
            iPartsKEMWorkBasketEDSId kemWorkBasketId = new iPartsKEMWorkBasketEDSId(kemNo);
            addToList(project, kemWorkBasketId, list);
        }
        return list;
    }

    private static void addToList(EtkProject project, iPartsKEMWorkBasketEDSId kemWorkBasketId, iPartsDataKEMWorkBasketEDSList list) {
        iPartsDataKEMWorkBasketEDS dataKEMWorkBasket = new iPartsDataKEMWorkBasketEDS(project, kemWorkBasketId);
        if (!dataKEMWorkBasket.existsInDB()) {
            dataKEMWorkBasket.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        }
        list.add(dataKEMWorkBasket, DBActionOrigin.FROM_EDIT);
    }

    private void loadModulesByKEMAndSaaBkFromDB(EtkProject project, String kemNo, String saaBkNo, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DKWB_KEM, FIELD_DKWB_SAA };
        String[] whereValues = new String[]{ kemNo, saaBkNo };
        String[] sortFields = new String[]{ FIELD_DKWB_MODULE_NO };

        searchSortAndFill(project, TABLE_DA_KEM_WORK_BASKET, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);
    }

    private void loadAllEntriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        String[] sortFields = new String[]{ FIELD_DKWB_KEM, FIELD_DKWB_SAA, FIELD_DKWB_PRODUCT_NO };

        searchSortAndFill(project, TABLE_DA_KEM_WORK_BASKET, null, null, sortFields,
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataKEMWorkBasketEDS getNewDataObject(EtkProject project) {
        return new iPartsDataKEMWorkBasketEDS(project, null);
    }
}
