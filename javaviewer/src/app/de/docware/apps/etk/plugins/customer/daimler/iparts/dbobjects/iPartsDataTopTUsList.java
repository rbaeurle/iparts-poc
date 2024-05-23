/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.Map;
import java.util.TreeMap;

/**
 * Liste von {@link iPartsDataTopTU} aus der Tabelle DA_TOP_TUS.
 */
public class iPartsDataTopTUsList extends EtkDataObjectList<iPartsDataTopTU> implements iPartsConst {

    public static Map<String, Map<String, String>> loadTopNodes(EtkProject project, iPartsProductId productId, String countryCode) {
        iPartsDataTopTUsList topTUsList = loadTopTUsFromDB(project, productId, countryCode.toUpperCase());
        Map<String, Map<String, String>> topNodesMap = new TreeMap<>();
        for (iPartsDataTopTU dataTopTU : topTUsList) {
            Map<String, String> topTuNodesMap = topNodesMap.computeIfAbsent(dataTopTU.getAsId().getKG(), kg -> new TreeMap<>());
            topTuNodesMap.computeIfAbsent(dataTopTU.getAsId().getTU(), tu -> dataTopTU.getRank());
        }
        return topNodesMap;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataTopTU}
     *
     * @param project
     * @return
     */
    public static iPartsDataTopTUsList loadAllTopTUsFromDB(EtkProject project) {
        iPartsDataTopTUsList list = new iPartsDataTopTUsList();
        list.loadTopTUsFromDB(project, null, null, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataTopTU} für ein Produkt.
     *
     * @param project   Das Projekt-Objekt
     * @param productId Die Produkt-ID
     * @return
     */
    public static iPartsDataTopTUsList loadTopTUsFromDB(EtkProject project, iPartsProductId productId) {
        iPartsDataTopTUsList list = new iPartsDataTopTUsList();
        String[] whereFields = new String[]{ FIELD_DTT_PRODUCT_NO };
        String[] whereValues = new String[]{ productId.getProductNumber() };
        list.loadTopTUsFromDB(project, whereFields, whereValues, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataTopTU} für ein Produkt in einem Land.
     *
     * @param project     Das Projekt-Objekt
     * @param productId   Die Produkt-ID
     * @param countryCode Der ISO Country Code
     * @return
     */
    public static iPartsDataTopTUsList loadTopTUsFromDB(EtkProject project, iPartsProductId productId, String countryCode) {
        iPartsDataTopTUsList list = new iPartsDataTopTUsList();
        String[] whereFields = new String[]{ FIELD_DTT_PRODUCT_NO, FIELD_DTT_COUNTRY_CODE };
        String[] whereValues = new String[]{ productId.getProductNumber(), countryCode };
        list.loadTopTUsFromDB(project, whereFields, whereValues, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataTopTU} für ein Produkt in einem Land zu einer KG.
     *
     * @param project     Das Projekt-Objekt
     * @param productId   Die Produkt-ID
     * @param countryCode Der ISO Country Code
     * @param kg          Die Konstruktionsgruppe
     * @return
     */
    public static iPartsDataTopTUsList loadTopTUsFromDB(EtkProject project, iPartsProductId productId, String countryCode, String kg) {
        iPartsDataTopTUsList list = new iPartsDataTopTUsList();
        String[] whereFields = new String[]{ FIELD_DTT_PRODUCT_NO, FIELD_DTT_COUNTRY_CODE, FIELD_DTT_KG };
        String[] whereValues = new String[]{ productId.getProductNumber(), countryCode, kg };
        list.loadTopTUsFromDB(project, whereFields, whereValues, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataTopTU} aus der DB mit den optionalen whereValues.
     *
     * @param project
     * @param whereFields
     * @param whereValues
     * @param loadType
     * @param origin
     */
    private void loadTopTUsFromDB(EtkProject project, String[] whereFields, String[] whereValues, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_TOP_TUS, whereFields, whereValues, loadType, origin);
    }

    @Override
    protected iPartsDataTopTU getNewDataObject(EtkProject project) {
        return new iPartsDataTopTU(project, null);
    }
}
