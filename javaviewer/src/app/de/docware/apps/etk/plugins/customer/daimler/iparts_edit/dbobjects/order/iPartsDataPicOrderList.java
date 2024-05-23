/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsDataPicOrderModules;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsDataPicOrderModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.iPartsXMLTcObject;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.SQLStringConvert;

/**
 * Liste von {@link iPartsDataPicOrder}.
 */
public class iPartsDataPicOrderList extends EtkDataObjectList<iPartsDataPicOrder> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrder}s, die dem übergebenen Modul zugeordnet sind.
     *
     * @param project
     * @param moduleNo
     * @return
     */
    public static iPartsDataPicOrderList loadPicOrderList(EtkProject project, String moduleNo) {
        iPartsDataPicOrderList list = new iPartsDataPicOrderList();
        list.loadPicOrdersFromDB(project, moduleNo);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrder}s, die einem bestimmten {@link iPartsTransferStates} zugeordnet sind.
     *
     * @param project
     * @param states
     * @return
     */
    public static iPartsDataPicOrderList loadPicOrderListForSpecificStates(EtkProject project, iPartsTransferStates... states) {
        iPartsDataPicOrderList list = new iPartsDataPicOrderList();
        list.loadPicOrdersForStates(project, states);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrder}s, die dem übergebenen TcObject(ASPLM) zugeordnet sind.
     * Die Zuordnung erfolgt über ItemId und ItemRevId.
     *
     * @param project
     * @param tcObject
     * @return
     */
    public static iPartsDataPicOrderList loadPicOrderListForTcObject(EtkProject project, iPartsXMLTcObject tcObject) {
        return loadPicOrderListForTcObject(project, tcObject.getMcItemId(), tcObject.getMcItemRevId());
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrder}s, die der übergebenen ItemId und ItemRevId zugeordnet sind.
     *
     * @param project
     * @param mcItemId
     * @param mcItemRevId
     * @return
     */
    public static iPartsDataPicOrderList loadPicOrderListForTcObject(EtkProject project, String mcItemId, String mcItemRevId) {
        iPartsDataPicOrderList list = new iPartsDataPicOrderList();
        list.loadPicOrdersForTcObjectFromDB(project, mcItemId, mcItemRevId);
        return list;
    }

    public static iPartsDataPicOrderList loadValidPicOrderListForTcObject(EtkProject project, iPartsXMLTcObject tcObject) {
        iPartsDataPicOrderList list = new iPartsDataPicOrderList();
        list.loadValidPicOrdersForTcObjectFromDB(project, tcObject);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller Änderungsaufträge zu dem Bildauftrag
     *
     * @param project
     * @param originalPicOrderGUID
     * @return
     */
    public static iPartsDataPicOrderList loadChangeOrdersForOriginalPicOrder(EtkProject project, String originalPicOrderGUID) {
        iPartsDataPicOrderList list = new iPartsDataPicOrderList();
        list.loadChangeOrderForOriginalPicOrder(project, originalPicOrderGUID);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller Änderungsaufträge zu einer MediaContainer-Nummer. Sortiert wird absteigend nach
     * der Revision
     *
     * @param project
     * @param mcItemId
     * @return
     */
    public static iPartsDataPicOrderList loadPicOrdersForMCItemId(EtkProject project, String mcItemId) {
        iPartsDataPicOrderList list = new iPartsDataPicOrderList();
        list.loadPicOrdersForMCItemIdFromDB(project, mcItemId);
        return list;
    }

    private void loadPicOrdersForMCItemIdFromDB(EtkProject project, String mcItemId) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DA_PO_ORDER_ID_EXTERN };
        String[] whereValues = new String[]{ mcItemId };

        searchSortAndFill(project, TABLE_DA_PICORDER, whereFields, whereValues, new String[]{ FIELD_DA_PO_ORDER_REVISION_EXTERN },
                          LoadType.COMPLETE, true, DBActionOrigin.FROM_DB);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrder}s, die der übergebenen ItemId und ItemRevId zugeordnet
     * sind und nicht als "ungültig" markiert wurden.
     *
     * @param project
     * @param tcObject
     */
    private void loadValidPicOrdersForTcObjectFromDB(EtkProject project, iPartsXMLTcObject tcObject) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DA_PO_ORDER_ID_EXTERN, FIELD_DA_PO_ORDER_REVISION_EXTERN, FIELD_DA_PO_ORDER_INVALID };
        String[] whereValues = new String[]{ tcObject.getMcItemId(), tcObject.getMcItemRevId(), SQLStringConvert.booleanToPPString(false) };

        searchAndFill(project, whereFields, whereValues);
    }


    public iPartsDataPicOrder findById(iPartsPicOrderId id) {
        for (iPartsDataPicOrder dataPicOrder : this) {
            if (dataPicOrder.getAsId().equals(id)) {
                return dataPicOrder;
            }
        }
        return null;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrder}s, die einem bestimmten {@link iPartsTransferStates} zugeordnet sind.
     *
     * @param project
     * @param states
     */
    private void loadPicOrdersForStates(EtkProject project, iPartsTransferStates... states) {
        clear(DBActionOrigin.FROM_DB);
        for (iPartsTransferStates state : states) {
            String[] whereFields = new String[]{ FIELD_DA_PO_STATUS };
            String[] whereValues = new String[]{ state.getDBValue() };

            searchAndFill(project, whereFields, whereValues);
        }
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrder}s, die der übergebenen ItemId und ItemRevId zugeordnet sind.
     *
     * @param project
     * @param mcItemId
     * @param mcItemRevId
     */
    private void loadPicOrdersForTcObjectFromDB(EtkProject project, String mcItemId, String mcItemRevId) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DA_PO_ORDER_ID_EXTERN, FIELD_DA_PO_ORDER_REVISION_EXTERN };
        String[] whereValues = new String[]{ mcItemId, mcItemRevId };

        searchAndFill(project, whereFields, whereValues);
    }

    /**
     * Lädt eine Liste aller Änderungsaufträge zu dem Bildauftrag
     *
     * @param project
     * @param originalPicOrderGuid
     */
    private void loadChangeOrderForOriginalPicOrder(EtkProject project, String originalPicOrderGuid) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DA_PO_ORIGINAL_PICORDER };
        String[] whereValues = new String[]{ originalPicOrderGuid };

        searchAndFill(project, whereFields, whereValues);
    }

    /**
     * Lädt die Liste aller {@link iPartsDataPicOrder}s, die dem übergebenen Modul zugeordnet sind.
     *
     * @param project
     * @param moduleNo
     * @return
     */
    private void loadPicOrdersFromDB(EtkProject project, String moduleNo) {
        clear(DBActionOrigin.FROM_DB);

        iPartsDataPicOrderModulesList picOrderModules = iPartsDataPicOrderModulesList.loadPicOrderModulesListForModule(project, moduleNo);
        for (iPartsDataPicOrderModules picOrderModule : picOrderModules) {
            iPartsPicOrderId id = new iPartsPicOrderId(picOrderModule.getAsId().getOrderGuid());
            iPartsDataPicOrder picOrder = new iPartsDataPicOrder(project, id);
            add(picOrder, DBActionOrigin.FROM_DB);
        }
    }

    private void searchAndFill(EtkProject project, String[] whereFields, String[] whereValues) {
        searchAndFill(project, TABLE_DA_PICORDER, whereFields, whereValues, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataPicOrder getNewDataObject(EtkProject project) {
        return new iPartsDataPicOrder(project, null);
    }
}
