/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsIncludeConstMatId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_INCLUDE_CONST_MAT.
 */
public class iPartsDataIncludeConstMat extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DICM_PART_NO, FIELD_DICM_SDATA, FIELD_DICM_INCLUDE_PART_NO };

    public iPartsDataIncludeConstMat(EtkProject project, iPartsIncludeConstMatId id) {
        super(KEYS);
        tableName = TABLE_DA_INCLUDE_CONST_MAT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsIncludeConstMatId createId(String... idValues) {
        return new iPartsIncludeConstMatId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsIncludeConstMatId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsIncludeConstMatId)id;
    }
}
