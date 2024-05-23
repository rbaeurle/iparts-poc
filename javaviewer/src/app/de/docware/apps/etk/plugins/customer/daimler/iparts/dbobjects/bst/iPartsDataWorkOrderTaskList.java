package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataWorkOrderTask}.
 */
public class iPartsDataWorkOrderTaskList extends EtkDataObjectList<iPartsDataWorkOrderTask> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataWorkOrderTask}s, die dem Bearbeitungsauftrag mit
     * der übergegenen Id zugeordnet sind.
     *
     * @param project
     * @param bstId
     * @return
     */
    public static iPartsDataWorkOrderTaskList loadWorkOrderTaskList(EtkProject project, String bstId) {
        iPartsDataWorkOrderTaskList list = new iPartsDataWorkOrderTaskList();
        list.loadWorkOrderTasksFromDB(project, bstId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt die Liste aller {@link iPartsDataWorkOrderTask}s, die dem Bearbeitungsauftrag mit
     * der übergegenen Id zugeordnet sind.
     *
     * @param project
     * @param bstId
     * @param origin
     * @return
     */
    private void loadWorkOrderTasksFromDB(EtkProject project, String bstId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DWT_BST_ID };
        String[] whereValues = new String[]{ bstId };

        searchAndFill(project, TABLE_DA_WORKORDER_TASKS, whereFields, whereValues, LoadType.ONLY_IDS, origin);
    }

    @Override
    protected iPartsDataWorkOrderTask getNewDataObject(EtkProject project) {
        return new iPartsDataWorkOrderTask(project, null);
    }
}
