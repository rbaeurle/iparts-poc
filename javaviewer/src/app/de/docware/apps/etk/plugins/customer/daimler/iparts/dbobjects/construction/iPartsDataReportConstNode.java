/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.Calendar;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_REPORT_CONST_NODES.
 */
public class iPartsDataReportConstNode extends EtkDataObject implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DRCN_SERIES_NO, FIELD_DRCN_NODE_ID, FIELD_DRCN_CHANGESET_GUID };

    public iPartsDataReportConstNode(EtkProject project, iPartsReportConstNodeId id) {
        super(KEYS);
        tableName = TABLE_DA_REPORT_CONST_NODES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsReportConstNodeId createId(String... idValues) {
        return new iPartsReportConstNodeId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsReportConstNodeId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsReportConstNodeId)id;
    }

    @Override
    public iPartsDataReportConstNode cloneMe(EtkProject project) {
        iPartsDataReportConstNode clone = new iPartsDataReportConstNode(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    public String getCalculationDate() {
        return getFieldValue(FIELD_DRCN_CALCULATION_DATE);
    }

    public void setCalculationDate(DBActionOrigin origin) {
        setFieldValueAsDateTime(FIELD_DRCN_CALCULATION_DATE, Calendar.getInstance(), origin);
    }

    public void setOpenEntries(int openEntries, DBActionOrigin origin) {
        setFieldValueAsInteger(FIELD_DRCN_OPEN_ENTRIES, openEntries, origin);
    }

    public void setChangedEntries(int changedEntries, DBActionOrigin origin) {
        setFieldValueAsInteger(FIELD_DRCN_CHANGED_ENTRIES, changedEntries, origin);
    }
}
