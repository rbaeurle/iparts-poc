/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.cache.ObjectInstanceLRUList;

/**
 * Cache für die {@link iPartsDataWorkOrder}-Objekte der Bearbeitungsaufträge.
 */
public class iPartsWorkOrderCache {

    private static ObjectInstanceLRUList<Object, iPartsWorkOrderCache> instances =
            new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_WORK_ORDER, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private iPartsDataWorkOrder dataWorkOrder;

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsWorkOrderCache getInstance(iPartsWorkOrderId workOrderId, EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsWorkOrderCache.class, workOrderId.getBSTId(),
                                                             false);
        iPartsWorkOrderCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsWorkOrderCache(workOrderId, project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private iPartsWorkOrderCache(iPartsWorkOrderId workOrderId, EtkProject project) {
        load(workOrderId, project);
    }

    private iPartsWorkOrderCache(iPartsDataWorkOrder dataWorkOrder) {
        this.dataWorkOrder = dataWorkOrder;
    }

    private void load(iPartsWorkOrderId workOrderId, EtkProject project) {
        dataWorkOrder = new iPartsDataWorkOrder(project, workOrderId);
        if (!dataWorkOrder.existsInDB()) {
            dataWorkOrder.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            dataWorkOrder.setFieldValue(iPartsConst.FIELD_DWO_TITLE, "<Work order with BST ID \"" + workOrderId.getBSTId()
                                                                     + "\" not found>", DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Liefert einen Klon vom {@link iPartsDataWorkOrder}-Objekt aus dem Cache zurück.
     *
     * @param project
     * @return
     */
    public iPartsDataWorkOrder getDataWorkOrder(EtkProject project) {
        return dataWorkOrder.cloneMe(project);
    }

    /**
     * Liefert den Bearbeitungsauftrag zum aktuellen Edit-ChangeSet
     *
     * @param project
     * @return
     */
    public static iPartsDataWorkOrder getWorkOrderForAuthorOrder(EtkProject project) {
        iPartsDataAuthorOrder authorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSetForEdit(project);
        if (authorOrder == null) {
            return null;
        }
        iPartsWorkOrderId id = new iPartsWorkOrderId(authorOrder.getBstId());
        return iPartsWorkOrderCache.getInstance(id, project).getDataWorkOrder(project);
    }
}