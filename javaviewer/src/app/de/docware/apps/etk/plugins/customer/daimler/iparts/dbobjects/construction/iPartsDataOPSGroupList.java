/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.HashMap;
import java.util.Map;

/**
 * Liste von {@link iPartsDataOPSGroup}.
 */
public class iPartsDataOPSGroupList extends EtkDataObjectList<iPartsDataOPSGroup> implements iPartsConst {

    private Map<String, iPartsDataOPSGroup> groupsAsMap;

    public iPartsDataOPSGroupList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle {@link iPartsDataOPSGroup}s, die zum übergebenen Baumuster gehören inkl. Benennung in allen Sprachen
     * (sortiert nach Baumuster und Gruppe)
     *
     * @param project
     * @param modelId
     * @return
     */
    public static iPartsDataOPSGroupList loadOPSGroupsForModel(EtkProject project, iPartsModelId modelId) {
        iPartsDataOPSGroupList list = new iPartsDataOPSGroupList();
        list.loadGroupsForModelFromDB(project, modelId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadGroupsForModelFromDB(EtkProject project, iPartsModelId modelId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DOG_MODEL_NO };
        String[] whereValues = new String[]{ modelId.getModelNumber() };
        String[] sortFields = new String[]{ FIELD_DOG_MODEL_NO, FIELD_DOG_GROUP };
        searchSortAndFillWithMultiLangValueForAllLanguages(project, null, FIELD_DOG_DESC, whereFields, whereValues, false,
                                                           sortFields, false);
    }

    @Override
    protected iPartsDataOPSGroup getNewDataObject(EtkProject project) {
        return new iPartsDataOPSGroup(project, null);
    }

    public Map<String, iPartsDataOPSGroup> getAsGroupMap() {
        if (groupsAsMap == null) {
            groupsAsMap = new HashMap<String, iPartsDataOPSGroup>(list.size());
        }
        for (iPartsDataOPSGroup groupDBObject : list) {
            groupsAsMap.put(groupDBObject.getAsId().getGroup(), groupDBObject);
        }
        return groupsAsMap;
    }

    @Override
    public void clear(DBActionOrigin origin) {
        groupsAsMap = null;
        super.clear(origin);
    }
}
