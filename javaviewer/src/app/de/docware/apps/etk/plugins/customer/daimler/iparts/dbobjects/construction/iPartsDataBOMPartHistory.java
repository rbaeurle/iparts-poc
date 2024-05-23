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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_BOM_MAT_HISTORY.
 */
public class iPartsDataBOMPartHistory extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DBMH_PART_NO, FIELD_DBMH_PART_VER, FIELD_DBMH_REV_FROM };

    public static final String CHILDREN_NAME_REMARKS = "iPartsDataBOMPartHistory.remarks";
    public static final String CHILDREN_NAME_WW_FLAGS = "iPartsDataBOMPartHistory.wwFlags";

    // Teilestammdaten Kinder, die in eigenen Tabellen gehalten werden
    protected iPartsDataMaterialRemarkList remarksList;
    protected iPartsDataMaterialWWFlagList wwFlagsList;

    public iPartsDataBOMPartHistory(EtkProject project, iPartsBOMPartHistoryId id) {
        super(KEYS);
        tableName = TABLE_DA_BOM_MAT_HISTORY;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    protected synchronized void loadRemarks() {
        if (remarksList != null) {
            return;
        }
        setChildren(CHILDREN_NAME_REMARKS, iPartsDataMaterialRemarkList.loadAllRemarksForMaterialAndRevisionFromDB(getEtkProject(), getAsId().getPartNo(), getAsId().getRevFrom()));
    }

    protected synchronized void loadWWFlags() {
        if (wwFlagsList != null) {
            return;
        }
        setChildren(CHILDREN_NAME_WW_FLAGS, iPartsDataMaterialWWFlagList.loadAllWWFlagsForMaterialAndRevisionFromDB(getEtkProject(), getAsId().getPartNo(), getAsId().getRevFrom()));
    }

    public iPartsDataMaterialRemarkList getRemarks() {
        loadRemarks();
        return remarksList;
    }

    public iPartsDataMaterialWWFlagList getWWFlags() {
        loadWWFlags();
        return wwFlagsList;
    }

    public synchronized void addRemark(iPartsDataMaterialRemark remarkForPartNo, DBActionOrigin origin) {
        if (remarkForPartNo != null) {
            getRemarks().add(remarkForPartNo, origin);
        }
    }

    public synchronized void addWWFlag(iPartsDataMaterialWWFlag wwFlagForPartNo, DBActionOrigin origin) {
        if (wwFlagForPartNo != null) {
            getWWFlags().add(wwFlagForPartNo, origin);
        }
    }

    @Override
    public iPartsBOMPartHistoryId createId(String... idValues) {
        return new iPartsBOMPartHistoryId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsBOMPartHistoryId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsBOMPartHistoryId)id;
    }

    @Override
    public void setChildren(String childrenName, DBDataObjectList<? extends DBDataObject> children) {
        super.setChildren(childrenName, children);
        if (childrenName.equals(CHILDREN_NAME_REMARKS)) {
            remarksList = (iPartsDataMaterialRemarkList)children;
        } else if (childrenName.equals(CHILDREN_NAME_WW_FLAGS)) {
            wwFlagsList = (iPartsDataMaterialWWFlagList)children;
        }
    }

    @Override
    public void clear(DBActionOrigin origin) {
        super.clear(origin);
        setChildren(CHILDREN_NAME_REMARKS, null);
        setChildren(CHILDREN_NAME_WW_FLAGS, null);
    }
}
