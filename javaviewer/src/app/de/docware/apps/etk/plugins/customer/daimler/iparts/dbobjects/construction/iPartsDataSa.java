/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_SA.
 */
public class iPartsDataSa extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DS_SA };

    public iPartsDataSa(EtkProject project, iPartsSaId id) {
        super(KEYS);
        tableName = TABLE_DA_SA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSaId createId(String... idValues) {
        return new iPartsSaId(idValues[0]);
    }

    @Override
    public iPartsSaId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSaId)id;
    }

    public boolean isDocuRelevant() {
        return !getFieldValueAsBoolean(FIELD_DS_NOT_DOCU_RELEVANT);
    }

    public void setDocuRelevant(boolean isDocuRel, DBActionOrigin origin) {
        setFieldValueAsBoolean(FIELD_DS_NOT_DOCU_RELEVANT, !isDocuRel, origin);
    }
}
