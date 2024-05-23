/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsKEMWorkBasketMBSId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ASUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Liste von {@link iPartsDataKEMWorkBasketMBS}.
 */
public class iPartsDataKEMWorkBasketMBSList extends EtkDataObjectList<iPartsDataKEMWorkBasketMBS> implements iPartsConst {

    private final static boolean SHOW_PRODUCT_NY_SAA_WITHOUT_USAGE = true;  // true: Suche und Anzeige des Produkts, wenn keine Verwendung in Retail

    public iPartsDataKEMWorkBasketMBSList() {
        setSearchWithoutActiveChangeSets(true);
    }


    /**
     * Erzeugt und lädt eine Liste ALLER {@link iPartsDataKEMWorkBasketMBS} aus DA_KEM_WORK_BASKET_MBS.
     *
     * @param project Das Projekt
     */
    public static iPartsDataKEMWorkBasketMBSList loadAllEntries(EtkProject project) {
        iPartsDataKEMWorkBasketMBSList list = new iPartsDataKEMWorkBasketMBSList();
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

        iPartsDataKEMWorkBasketMBSList list = new iPartsDataKEMWorkBasketMBSList();
        list.loadModulesByKEMAndSaaBkFromDB(project, kemNo, saaBkNo, DBActionOrigin.FROM_DB);
        for (iPartsDataKEMWorkBasketMBS dataKEMWorkBasket : list) {
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
    public static iPartsDataKEMWorkBasketMBSList checkUsageInRetail(EtkProject project, Map<String, Set<MBSStructureId>> kemNoSaaMap) {
        iPartsDataKEMWorkBasketMBSList list = new iPartsDataKEMWorkBasketMBSList();
        Map<String, Set<String>> modelProductMap = new HashMap<>();
        if (!kemNoSaaMap.isEmpty()) {
            kemNoSaaMap.forEach((kemNo, saaList) -> {
                iPartsDataKEMWorkBasketMBSList singleKemlist = checkUsageInRetail(project, kemNo, saaList, modelProductMap);
                list.addAll(singleKemlist, DBActionOrigin.FROM_EDIT);
            });
        }
        return list;
    }

    /**
     * Überprüft für eine SAA-Liste der Verwendung im Retail und erzeugt eine Liste von
     * {@link iPartsDataKEMWorkBasketMBS}s, die in der Tabelle DA_KEM_WORK_BASKET gespeichert werden können
     *
     * @param project
     * @param saaList
     * @param modelProductMap
     * @return
     */
    public static iPartsDataKEMWorkBasketMBSList checkUsageInRetail(EtkProject project, String kemNo, Set<MBSStructureId> saaList,
                                                                    Map<String, Set<String>> modelProductMap) {
        iPartsDataKEMWorkBasketMBSList list = new iPartsDataKEMWorkBasketMBSList();
        if (!saaList.isEmpty()) {
            ASUsageHelper usageHelper = new ASUsageHelper(project);
            for (MBSStructureId structId : saaList) {
                if (structId.getListNumber().isEmpty()) {
                    iPartsKEMWorkBasketMBSId kemWorkBasketId = new iPartsKEMWorkBasketMBSId(kemNo, "", structId.getConGroup());
                    // todo wirklich zum Arbeitsvorrat hinzufügen?
                    addToList(project, kemWorkBasketId, list);
                    continue;
                }
                if (structId.isBasePartlistId() || (!structId.getConGroup().isEmpty() && structId.getConGroup().startsWith(iPartsConst.BASE_LIST_NUMBER_PREFIX))) {
                    // Grundstücklisten müssen nicht im Retail in der SAA_VALIDITY gesucht werden
                    // aus GS noch BM bestimmen und daraus die Produkte
                    Set<String> productNoSet = searchModelAndProducts(project, structId, modelProductMap);
                    if (productNoSet.isEmpty()) {
                        iPartsKEMWorkBasketMBSId kemWorkBasketId = new iPartsKEMWorkBasketMBSId(kemNo, structId.getListNumber(), structId.getConGroup());
                        addToList(project, kemWorkBasketId, list);
                    } else {
                        for (String productNo : productNoSet) {
                            iPartsKEMWorkBasketMBSId kemWorkBasketId = new iPartsKEMWorkBasketMBSId(kemNo, structId.getListNumber(), structId.getConGroup(),
                                                                                                    productNo, "", "");
                            addToList(project, kemWorkBasketId, list);
                        }
                    }
                    continue;
                }
                String saaNumber = structId.getListNumber();
                Map<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> kgTuSuggestionCheck3 =
                        usageHelper.getEDSUsageBySaaASPartList(saaNumber);
                if (!kgTuSuggestionCheck3.isEmpty()) {
                    Set<String> retailProductNoSet = new HashSet<>();
                    for (iPartsProductId productId : kgTuSuggestionCheck3.keySet()) {
                        List<ASUsageHelper.EDSUsageContainer> containers = kgTuSuggestionCheck3.get(productId);
                        for (ASUsageHelper.EDSUsageContainer container : containers) {
                            iPartsKEMWorkBasketMBSId kemWorkBasketId =
                                    new iPartsKEMWorkBasketMBSId(kemNo, saaNumber, structId.getConGroup(), productId.getProductNumber(),
                                                                 container.getKgTuId().getKg(),
                                                                 container.getFirstModuleNumber());
                            addToList(project, kemWorkBasketId, list);
                        }
                        retailProductNoSet.add(productId.getProductNumber());
                    }
                    if (SHOW_PRODUCT_NY_SAA_WITHOUT_USAGE) {
                        // bei gefundenen SAAs mit Retail-Verwendung das BM suchen und überprüfen, ob es eine BM-Produkt-Beziehung gibt
                        Set<String> productNoSet = searchModelAndProducts(project, structId, modelProductMap);
                        if (!productNoSet.isEmpty()) {
                            for (String productNo : productNoSet) {
                                // wenn Produkt bereits im Retail gefunden => überspringen
                                if (!retailProductNoSet.contains(productNo)) {
                                    iPartsKEMWorkBasketMBSId kemWorkBasketId = new iPartsKEMWorkBasketMBSId(kemNo, structId.getListNumber(), structId.getConGroup(),
                                                                                                            productNo, "", "");
                                    addToList(project, kemWorkBasketId, list);
                                }
                            }
                        }

                    }
                } else {
                    if (SHOW_PRODUCT_NY_SAA_WITHOUT_USAGE) {
                        // bei gefundenen SAAs ohne Retail-Verwendung das BM suchen und überprüfen, ob es eine BM-Produkt-Beziehung gibt
                        Set<String> productNoSet = searchModelAndProducts(project, structId, modelProductMap);
                        if (productNoSet.isEmpty()) {
                            iPartsKEMWorkBasketMBSId kemWorkBasketId = new iPartsKEMWorkBasketMBSId(kemNo, structId.getListNumber(), structId.getConGroup());
                            addToList(project, kemWorkBasketId, list);
                        } else {
                            for (String productNo : productNoSet) {
                                iPartsKEMWorkBasketMBSId kemWorkBasketId = new iPartsKEMWorkBasketMBSId(kemNo, structId.getListNumber(), structId.getConGroup(),
                                                                                                        productNo, "", "");
                                addToList(project, kemWorkBasketId, list);
                            }
                        }
                    } else {
                        // ohne Suche BM-Produkt-Beziehung
                        iPartsKEMWorkBasketMBSId kemWorkBasketId = new iPartsKEMWorkBasketMBSId(kemNo, saaNumber, structId.getConGroup());
                        addToList(project, kemWorkBasketId, list);
                    }
                }
            }
        } else {
            iPartsKEMWorkBasketMBSId kemWorkBasketId = new iPartsKEMWorkBasketMBSId(kemNo);
            addToList(project, kemWorkBasketId, list);
        }
        return list;
    }

    /**
     * In der STRUCTRUE_MBS eine Ebene höher gehgen (structId.getListNumber() als SUB_SNR), die SNR ist dann das Baumuster
     * Mit dem Baumuster in der PRODUCT_MODELS nach Produkten suchen und zurückliefern
     *
     * @param project
     * @param structId
     * @param modelProductMap
     * @return
     */
    private static Set<String> searchModelAndProducts(EtkProject project, MBSStructureId structId,
                                                      Map<String, Set<String>> modelProductMap) {
        Set<String> productNoSet = new HashSet<>();
        DBDataObjectAttributesList mbsAttributesList = project.getDbLayer().getAttributesList(
                TABLE_DA_STRUCTURE_MBS, new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR },
                new String[]{ FIELD_DSM_SUB_SNR }, new String[]{ structId.getListNumber() });
        if (!mbsAttributesList.isEmpty()) {
            Set<String> modelNoSet = new HashSet<>();
            for (DBDataObjectAttributes attributes : mbsAttributesList) {
                String modelNo = attributes.getFieldValue(FIELD_DSM_SNR);
                if (StrUtils.isValid(modelNo) && iPartsModel.isModelNumberValid(modelNo)) {
                    modelNoSet.add(modelNo);
                }
            }
            if (!modelNoSet.isEmpty()) {
                for (String modelNo : modelNoSet) {
                    productNoSet.addAll(getProductListFromModel(project, modelNo, modelProductMap));
                }
            }
        }
        return productNoSet;
    }

    /**
     * Suche mit einem Baumuster {@param modelNo} ind der PRODUCT_MODELS nach Verwendungen und die Liste der Product-Nummer zurückliefern
     *
     * @param project
     * @param modelNo
     * @param modelProductMap
     * @return
     */
    private static Set<String> getProductListFromModel(EtkProject project, String modelNo, Map<String, Set<String>> modelProductMap) {
        Set<String> productNoSet = modelProductMap.get(modelNo);
        if (productNoSet == null) {
            productNoSet = new HashSet<>();
            Set<iPartsDataProductModels> productModelsSet = iPartsProductModels.getInstance(project).getProductModelsByModel(project, modelNo);
            if (productModelsSet != null) {
                for (iPartsDataProductModels dataProductModels : productModelsSet) {
                    productNoSet.add(dataProductModels.getAsId().getProductNumber());
                }
            }
            modelProductMap.put(modelNo, productNoSet);
        }
        return productNoSet;
    }

    private static void addToList(EtkProject project, iPartsKEMWorkBasketMBSId kemWorkBasketId, iPartsDataKEMWorkBasketMBSList list) {
        iPartsDataKEMWorkBasketMBS dataKEMWorkBasket = new iPartsDataKEMWorkBasketMBS(project, kemWorkBasketId);
        if (!dataKEMWorkBasket.existsInDB()) {
            dataKEMWorkBasket.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        }
        list.add(dataKEMWorkBasket, DBActionOrigin.FROM_EDIT);
    }


    private void loadModulesByKEMAndSaaBkFromDB(EtkProject project, String kemNo, String saaBkNo, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DKWM_KEM, FIELD_DKWM_SAA };
        String[] whereValues = new String[]{ kemNo, saaBkNo };
        String[] sortFields = new String[]{ FIELD_DKWM_MODULE_NO };

        searchSortAndFill(project, TABLE_DA_KEM_WORK_BASKET_MBS, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);
    }

    /**
     * Zentrale Methode, die die Sortierfelder zurückliefert.
     * Prinzipiell sollen alle in dieser Klasse zusammengestellten Ergebnislisten gleich sortiert sein.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DKWM_KEM, FIELD_DKWM_SAA, FIELD_DKWM_GROUP, FIELD_DKWM_PRODUCT_NO };
    }

    private void loadAllEntriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_KEM_WORK_BASKET_MBS, null, null, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataKEMWorkBasketMBS getNewDataObject(EtkProject project) {
        return new iPartsDataKEMWorkBasketMBS(project, null);
    }
}
