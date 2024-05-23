/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_AO_HISTORY.
 */
public class iPartsDataAOHistory extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DAH_GUID, FIELD_DAH_SEQNO };

    public iPartsDataAOHistory(EtkProject project, iPartsAOHistoryId id) {
        super(KEYS);
        tableName = TABLE_DA_AO_HISTORY;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsAOHistoryId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsAOHistoryId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsAOHistoryId)id;
    }

    // Convenience Method
    public String getAction() {
        return getFieldValue(FIELD_DAH_ACTION);
    }

    public void setAction(String action) {
        setFieldValue(FIELD_DAH_ACTION, action, DBActionOrigin.FROM_EDIT);
    }
}
