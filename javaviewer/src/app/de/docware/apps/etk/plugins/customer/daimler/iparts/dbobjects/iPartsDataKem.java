/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_KEM_MASTERDATA.
 */

public class iPartsDataKem extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DKM_KEM, FIELD_DKM_SDA };

    public iPartsDataKem(EtkProject project, iPartsKemId id) {
        super(KEYS);
        tableName = TABLE_DA_KEM_MASTERDATA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsKemId createId(String... idValues) {
        return new iPartsKemId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsKemId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsKemId)id;
    }
}
