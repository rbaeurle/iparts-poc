/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.Calendar;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_CHANGE_SET.
 */
public class iPartsDataChangeSet extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCS_GUID };

    public iPartsDataChangeSet(EtkProject project, iPartsChangeSetId id) {
        super(KEYS);
        tableName = TABLE_DA_CHANGE_SET;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsChangeSetId createId(String... idValues) {
        return new iPartsChangeSetId(idValues[0]);
    }

    @Override
    public iPartsChangeSetId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsChangeSetId)id;
    }

    public iPartsChangeSetStatus getStatus() {
        return iPartsChangeSetStatus.getStatusByDbValue(getFieldValue(FIELD_DCS_STATUS));
    }

    public void setStatus(iPartsChangeSetStatus status, DBActionOrigin origin) {
        if (status != null) {
            setFieldValue(FIELD_DCS_STATUS, status.name(), origin);
        } else {
            setFieldValue(FIELD_DCS_STATUS, "", origin);
        }
    }

    public Calendar getCommitDate() {
        return getFieldValueAsDateTime(FIELD_DCS_COMMIT_DATE);
    }

    public void setCommitDate(Calendar commitDate, DBActionOrigin origin) {
        setFieldValueAsDateTime(FIELD_DCS_COMMIT_DATE, commitDate, origin);
    }

    public iPartsChangeSetSource getSource() {
        return iPartsChangeSetSource.getSourceByDbValue(getFieldValue(FIELD_DCS_SOURCE));
    }

    public void setSource(iPartsChangeSetSource source, DBActionOrigin origin) {
        if (source != null) {
            setFieldValue(FIELD_DCS_SOURCE, source.name(), origin);
        } else {
            setFieldValue(FIELD_DCS_SOURCE, "", origin);
        }
    }
}
