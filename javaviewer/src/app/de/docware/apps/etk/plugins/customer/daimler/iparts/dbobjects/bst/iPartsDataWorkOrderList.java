/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.SQLStringConvert;

import java.util.Calendar;
import java.util.Iterator;

/**
 * Liste von {@link iPartsDataWorkOrder}.
 */
public class iPartsDataWorkOrderList extends EtkDataObjectList<iPartsDataWorkOrder> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataWorkOrder}s mit optionaler Filterung aufgrund des aktuellen Datums
     * ("Leistungsbeginn" <= "aktuelles Datum" <= "Geplantes Lieferdatum").
     *
     * @param project
     * @param filterByCurrentDate
     * @return
     */
    public static iPartsDataWorkOrderList loadAllWorkOrderList(EtkProject project, boolean filterByCurrentDate) {
        iPartsDataWorkOrderList list = new iPartsDataWorkOrderList();
        list.loadAllWorkOrdersFromDB(project, DBActionOrigin.FROM_DB);
        if (filterByCurrentDate) {
            list.filterByCurrentDate();
        }
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataWorkOrder}s mit der übergebenen BST Supplier-ID (Lieferant) mit
     * optionaler Filterung aufgrund des aktuellen Datums ("Leistungsbeginn" <= "aktuelles Datum" <= "Geplantes Lieferdatum").
     *
     * @param project
     * @param bstSupplierId
     * @param filterByCurrentDate
     * @return
     */
    public static iPartsDataWorkOrderList loadWorkOrderList(EtkProject project, String bstSupplierId, boolean filterByCurrentDate) {
        iPartsDataWorkOrderList list = new iPartsDataWorkOrderList();
        list.loadWorkOrdersFromDB(project, bstSupplierId, DBActionOrigin.FROM_DB);
        if (filterByCurrentDate) {
            list.filterByCurrentDate();
        }
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller internen {@link iPartsDataWorkOrder}s mit optionaler Filterung aufgrund des aktuellen
     * Datums ("Leistungsbeginn" <= "aktuelles Datum" <= "Geplantes Lieferdatum").
     *
     * @param project
     * @param filterByCurrentDate
     * @return
     */
    public static iPartsDataWorkOrderList loadInternalWorkOrderList(EtkProject project, boolean filterByCurrentDate) {
        iPartsDataWorkOrderList list = new iPartsDataWorkOrderList();
        list.loadInternalWorkOrdersFromDB(project, DBActionOrigin.FROM_DB);
        if (filterByCurrentDate) {
            list.filterByCurrentDate();
        }
        return list;
    }

    /**
     * Filtert die Liste der {@link iPartsDataWorkOrder}s nach "Leistungsbeginn" <= "aktuelles Datum" <= "Geplantes Lieferdatum".
     */
    public void filterByCurrentDate() {
        String currentDate = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
        Iterator<iPartsDataWorkOrder> iterator = list.iterator();
        while (iterator.hasNext()) {
            iPartsDataWorkOrder dataWorkOrder = iterator.next();
            if (!dataWorkOrder.isValidForDate(currentDate)) {
                iterator.remove();
            }
        }
    }

    /**
     * Lädt die Liste aller {@link iPartsDataWorkOrder}s.
     *
     * @param project
     * @param origin
     * @return
     */
    private void loadAllWorkOrdersFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchAndFill(project, TABLE_DA_WORKORDER, null, null, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt die Liste aller {@link iPartsDataWorkOrder}s mit der übergebenen BST Supplier-ID (Lieferant).
     *
     * @param project
     * @param bstSupplierId
     * @param origin
     * @return
     */
    private void loadWorkOrdersFromDB(EtkProject project, String bstSupplierId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DWO_SUPPLIER_NO };
        String[] whereValues = new String[]{ bstSupplierId };

        searchAndFill(project, TABLE_DA_WORKORDER, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt die Liste aller internen {@link iPartsDataWorkOrder}s.
     *
     * @param project
     * @param origin
     * @return
     */
    private void loadInternalWorkOrdersFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DWO_INTERNAL_ORDER };
        String[] whereValues = new String[]{ SQLStringConvert.booleanToPPString(true) };

        searchAndFill(project, TABLE_DA_WORKORDER, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataWorkOrder getNewDataObject(EtkProject project) {
        return new iPartsDataWorkOrder(project, null);
    }
}