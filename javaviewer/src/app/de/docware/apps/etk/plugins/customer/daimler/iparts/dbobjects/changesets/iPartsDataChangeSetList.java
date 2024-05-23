/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.util.StrUtils;

/**
 * Liste von {@link iPartsDataChangeSet}s.
 */
public class iPartsDataChangeSetList extends EtkDataObjectList<iPartsDataChangeSet> implements iPartsConst {

    public iPartsDataChangeSetList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle Änderungssets mit der übergebenen Quelle und Status, wobei beide jeweils auch {@code null} sein können,
     * wenn diese egal sind.
     *
     * @param project
     * @param source
     * @param status
     * @return
     */
    public static iPartsDataChangeSetList loadChangeSetsForSourceAndStatus(EtkProject project, iPartsChangeSetSource source,
                                                                           iPartsChangeSetStatus status) {
        return loadChangeSetsForChangeSetAndSourceAndStatus(project, null, source, status);
    }

    /**
     * Lädt alle Änderungssets für ein ChangeSet mit der übergebenen Quelle und Status, wobei beide jeweils auch {@code null} sein können,
     * wenn diese egal sind.
     *
     * @param project
     * @param changeSetId
     * @param source
     * @param status
     * @return
     */
    public static iPartsDataChangeSetList loadChangeSetsForChangeSetAndSourceAndStatus(EtkProject project, iPartsChangeSetId changeSetId,
                                                                                       iPartsChangeSetSource source, iPartsChangeSetStatus status) {
        iPartsDataChangeSetList list = new iPartsDataChangeSetList();
        list.loadForChangesetSourceAndStatus(project, changeSetId, source, status);
        return list;
    }

    public iPartsDataChangeSet fillAndAddObjectFromAttributes(EtkProject project, DBDataObjectAttributes attributes, DBActionOrigin origin) {
        return fillAndAddDataObjectFromAttributes(project, attributes, LoadType.COMPLETE, true, origin);
    }

    /**
     * Füllen der aktuellen Liste mit der Trefferliste
     *
     * @param project
     * @param attributesList
     * @param loadType
     * @param origin
     */
    protected void fillAndAddDataObjectsFromAttributesList(EtkProject project, DBDataObjectAttributesList attributesList, LoadType loadType, DBActionOrigin origin) {
        if (attributesList != null) {
            for (DBDataObjectAttributes attributes : attributesList) {
                fillAndAddDataObjectFromAttributes(project, attributes, loadType, true, origin);
            }
        }
    }


    private void loadForChangesetSourceAndStatus(EtkProject project, iPartsChangeSetId changeSetId, iPartsChangeSetSource source, iPartsChangeSetStatus status) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields = null;
        String[] whereValues = null;
        if (source != null) {
            if (status != null) {
                whereFields = new String[]{ FIELD_DCS_SOURCE, FIELD_DCS_STATUS };
                whereValues = new String[]{ source.name(), status.name() };
            } else {
                whereFields = new String[]{ FIELD_DCS_SOURCE };
                whereValues = new String[]{ source.name() };
            }
        } else if (status != null) {
            whereFields = new String[]{ FIELD_DCS_STATUS };
            whereValues = new String[]{ status.name() };
        }
        if (changeSetId != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DCS_GUID });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ changeSetId.getGUID() });
        }

        searchAndFill(project, TABLE_DA_CHANGE_SET, whereFields, whereValues, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataChangeSet getNewDataObject(EtkProject project) {
        return new iPartsDataChangeSet(project, null);
    }
}