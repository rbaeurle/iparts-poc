/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_SAA_HISTORY.
 */
public class iPartsDataSaaHistory extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DSH_SAA, FIELD_DSH_REV_FROM };
    public static final String CHILDREN_NAME_REMARKS = "iPartsDataSaaHistory.remarks";
    public static final String CHILDREN_NAME_WW_FLAGS = "iPartsDataSaaHistory.wwFlags";

    protected iPartsDataSaaRemarksList remarksList;
    protected iPartsDataSaaWWFlagsList wwFlagsList;

    public iPartsDataSaaHistory(EtkProject project, iPartsSaaHistoryId id) {
        super(KEYS);
        tableName = TABLE_DA_SAA_HISTORY;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSaaHistoryId createId(String... idValues) {
        return new iPartsSaaHistoryId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsSaaHistoryId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsSaaHistoryId)id;
    }

    protected synchronized void loadRemarks() {
        if (remarksList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_REMARKS, iPartsDataSaaRemarksList.loadAllRemarksForSAARevision(getEtkProject(), getAsId().getSaaNumber(), getAsId().getRevFrom()));
    }

    protected synchronized void loadWWFlags() {
        if (wwFlagsList != null) {
            return;
        }
        setChildren(CHILDREN_NAME_WW_FLAGS, iPartsDataSaaWWFlagsList.loadAllWWFlagsForSAARevision(getEtkProject(), getAsId().getSaaNumber(), getAsId().getRevFrom()));
    }

    public iPartsDataSaaRemarksList getRemarks() {
        loadRemarks();
        return remarksList;
    }

    public iPartsDataSaaWWFlagsList getWWFlags() {
        loadWWFlags();
        return wwFlagsList;
    }

    public synchronized void addRemark(iPartsDataSaaRemarks remarkForSaa, DBActionOrigin origin) {
        if (remarkForSaa != null) {
            getRemarks().add(remarkForSaa, origin);
        }
    }

    public synchronized void addWWFlag(iPartsDataSaaWWFlags wwFlagForSaa, DBActionOrigin origin) {
        if (wwFlagForSaa != null) {
            getWWFlags().add(wwFlagForSaa, origin);
        }
    }

    @Override
    public void setChildren(String childrenName, DBDataObjectList<? extends DBDataObject> children) {
        super.setChildren(childrenName, children);
        if (childrenName.equals(CHILDREN_NAME_REMARKS)) {
            remarksList = (iPartsDataSaaRemarksList)children;
        } else if (childrenName.equals(CHILDREN_NAME_WW_FLAGS)) {
            wwFlagsList = (iPartsDataSaaWWFlagsList)children;
        }
    }

    @Override
    public void clear(DBActionOrigin origin) {
        super.clear(origin);
        setChildren(CHILDREN_NAME_REMARKS, null);
        setChildren(CHILDREN_NAME_WW_FLAGS, null);
    }

}
