/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketSaaStatesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Einfache Klasse zur Verwaltung der externen Docu-Rel States für den EDS-Arbeitsvorrat
 */
public class iPartsWBSaaStatesManagement {

    private EtkProject project;
    private iPartsImportDataOrigin source;
    private Set<String> loadedModelsSet;
    private Map<String, Map<String, Map<String, iPartsDataWorkBasketSaaStates>>> statesMap; // Baumuster -> Produkt -> SAA -> Status

//    public iPartsWBSaaStatesManagement(EtkProject project) {
//        this(project, iPartsImportDataOrigin.EDS);
//    }

    public iPartsWBSaaStatesManagement(EtkProject project, iPartsImportDataOrigin source) {
        this.project = project;
        if (((source != iPartsImportDataOrigin.EDS) && (source != iPartsImportDataOrigin.SAP_MBS) && (source != iPartsImportDataOrigin.SAP_CTT))) {
            source = iPartsImportDataOrigin.EDS;
        }
        this.source = source;
        this.loadedModelsSet = Collections.synchronizedSet(new HashSet<>());
        this.statesMap = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Interne Verwaltung löschen
     */
    public void clear() {
        loadedModelsSet.clear();
        statesMap.clear();
    }

    public iPartsImportDataOrigin getSource() {
        return source;
    }

    /**
     * Für die übergebenen PK-Werte eine DocuRel-Status bestimmen
     *
     * @param modelNo
     * @param productNo
     * @param saaNo
     * @return
     */
    public iPartsDocuRelevantTruck getManualDocuRel(String modelNo, String productNo, String saaNo) {
        // Sicherheitsabfrage
        if (statesMap.get(modelNo) == null) {
            loadedModelsSet.add(modelNo);
            addModel(modelNo);
        }

        iPartsDataWorkBasketSaaStates dataWBSStates = getStateSaaDataObject(modelNo, productNo, saaNo);
        if (dataWBSStates != null) {
            return iPartsDocuRelevantTruck.getFromDBValue(dataWBSStates.getFieldValue(iPartsConst.FIELD_WBS_DOCU_RELEVANT));
        }
        return iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED;
    }

    private iPartsDataWorkBasketSaaStates getStateSaaDataObject(String modelNo, String productNo, String saaNo) {
        Map<String, iPartsDataWorkBasketSaaStates> saaList = getSaaList(modelNo, productNo);
        if (saaList != null) {
            return saaList.get(saaNo);
        }
        return null;
    }

    /**
     * Alle Elemente einer Liste von Baumuster zur Verwaltung hinzufügen
     *
     * @param modelNoList
     */
    public void addModels(Set<String> modelNoList) {
        for (String modelNo : modelNoList) {
            if (loadedModelsSet.add(modelNo)) {
                addModel(modelNo);
            }
        }
    }

    /**
     * Alle Elemente eines Baumusters zur Verwaltung hinzufügen
     *
     * @param modelNo
     */
    public void addModel(String modelNo) {
        iPartsDataWorkBasketSaaStatesList list = iPartsDataWorkBasketSaaStatesList.loadSaaStatesByModel(project, modelNo, source);
        addElementsToMap(list);
    }

    /**
     * Vorbereitung für Vereinheitlichen
     *
     * @param idList
     * @return
     */
    public List<iPartsDataWorkBasketSaaStates> getWBSStateListForUnify(List<iPartsWorkBasketSaaStatesId> idList) {
        List<iPartsDataWorkBasketSaaStates> dataWBSStateList = new DwList<>();
        for (iPartsWorkBasketSaaStatesId id : idList) {
            iPartsDataWorkBasketSaaStates dataWBSState = getStateSaaDataObject(id.getModelNo(),
                                                                               id.getProductNo(),
                                                                               id.getSAANo());
            if (dataWBSState == null) {
                dataWBSState = new iPartsDataWorkBasketSaaStates(project, id);
                if (!dataWBSState.existsInDB()) {
                    dataWBSState.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
                    dataWBSState.setFieldValue(iPartsConst.FIELD_WBS_DOCU_RELEVANT,
                                               iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED.getDbValue(),
                                               DBActionOrigin.FROM_DB);
                }
            }
            dataWBSStateList.add(dataWBSState);
        }
        return dataWBSStateList;
    }

    /**
     * Nachbereitung Vereinheitlichen - Vorbereitung für Speichern in techn. ChangeSet
     *
     * @param dataWBSStateList
     * @param attributes
     * @return
     */
    public GenericEtkDataObjectList addUnifyResult(List<iPartsDataWorkBasketSaaStates> dataWBSStateList, DBDataObjectAttributes attributes) {
        GenericEtkDataObjectList genericList = new GenericEtkDataObjectList();
        if (attributes != null) {
            String fieldName = iPartsConst.FIELD_WBS_DOCU_RELEVANT;
            String fieldValue = attributes.getFieldValue(fieldName);
            iPartsDocuRelevantTruck docuRel = iPartsDocuRelevantTruck.getFromDBValue(fieldValue);
            boolean isToDelete = (docuRel == iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED);

            for (iPartsDataWorkBasketSaaStates dataWBSState : dataWBSStateList) {
                if (isToDelete) {
                    genericList.delete(dataWBSState, true, DBActionOrigin.FROM_EDIT);
                    removeElementFromMap(dataWBSState);
                } else {
                    dataWBSState.setFieldValue(iPartsConst.FIELD_WBS_DOCU_RELEVANT, docuRel.getDbValue(), DBActionOrigin.FROM_EDIT);
                    genericList.add(dataWBSState, DBActionOrigin.FROM_EDIT);
                    addElementToMap(dataWBSState);
                }
            }
        }
        return genericList;
    }

    /**
     * -
     * Liste von Records zur Verwaltung hinzufügen
     *
     * @param list
     */
    private void addElementsToMap(iPartsDataWorkBasketSaaStatesList list) {
        for (iPartsDataWorkBasketSaaStates dataWBSStates : list) {
            addElementToMap(dataWBSStates);
        }
    }

    /**
     * Einen Record zur Verwaltung hinzufügen
     *
     * @param dataWBSStates
     */
    private void addElementToMap(iPartsDataWorkBasketSaaStates dataWBSStates) {
        Map<String, Map<String, iPartsDataWorkBasketSaaStates>> productList = statesMap.get(dataWBSStates.getAsId().getModelNo());
        if (productList == null) {
            productList = Collections.synchronizedMap(new HashMap<>());
            statesMap.put(dataWBSStates.getAsId().getModelNo(), productList);
        }
        Map<String, iPartsDataWorkBasketSaaStates> saaList = productList.get(dataWBSStates.getAsId().getProductNo());
        if (saaList == null) {
            saaList = Collections.synchronizedMap(new HashMap<>());
            productList.put(dataWBSStates.getAsId().getProductNo(), saaList);
        }
        saaList.put(dataWBSStates.getAsId().getSAANo(), dataWBSStates);
    }

    /**
     * Aus Model- und ProductNo die SaaMap bestimmen
     *
     * @param modelNo
     * @param productNo
     * @return
     */
    private Map<String, iPartsDataWorkBasketSaaStates> getSaaList(String modelNo, String productNo) {
        Map<String, Map<String, iPartsDataWorkBasketSaaStates>> productList = statesMap.get(modelNo);
        if (productList != null) {
            return productList.get(productNo);
        }
        return null;
    }

    /**
     * Aus DataObject die SaaMap bestimmen
     *
     * @param dataWBSStates
     * @return
     */
    private Map<String, iPartsDataWorkBasketSaaStates> getSaaList(iPartsDataWorkBasketSaaStates dataWBSStates) {
        return getSaaList(dataWBSStates.getAsId().getModelNo(), dataWBSStates.getAsId().getProductNo());
    }

    /**
     * Ein DataObject aus der Verwaltung entfernen
     * #
     *
     * @param dataWBSStates
     */
    private void removeElementFromMap(iPartsDataWorkBasketSaaStates dataWBSStates) {
        Map<String, iPartsDataWorkBasketSaaStates> saaList = getSaaList(dataWBSStates);
        if (saaList != null) {
            saaList.remove(dataWBSStates.getAsId().getSAANo());
        }
    }
}
