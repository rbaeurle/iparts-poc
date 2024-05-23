/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_RESERVED_PK.
 */
public class iPartsDataReservedPK extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DRP_DO_TYPE, FIELD_DRP_DO_ID };

    public iPartsDataReservedPK(EtkProject project, iPartsReservedPKId id) {
        super(KEYS);
        tableName = TABLE_DA_RESERVED_PK;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsReservedPKId createId(String... idValues) {
        return new iPartsReservedPKId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsReservedPKId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsReservedPKId)id;
    }

    public String getChangeSetId() {
        return getFieldValue(FIELD_DRP_CHANGE_SET_ID);
    }

    public void setChangeSetId(String changeSetId, DBActionOrigin origin) {
        setFieldValue(FIELD_DRP_CHANGE_SET_ID, changeSetId, origin);
    }
}