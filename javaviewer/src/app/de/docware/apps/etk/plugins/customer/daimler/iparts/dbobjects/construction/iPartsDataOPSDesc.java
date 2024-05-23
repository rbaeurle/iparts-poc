/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_OPSDESC.
 * Deprecated, weil die OPS Struktur nun in zwei verschiedenen Tabellen gehalten wird (DA_OPS_GROUP und DA_OPS_SCOPE)
 */
@Deprecated
public class iPartsDataOPSDesc extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_OPS_GROUP, FIELD_OPS_SCOPE };

    public iPartsDataOPSDesc(EtkProject project, iPartsOPSDescId id) {
        super(KEYS);
        tableName = TABLE_DA_OPSDESC;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsOPSDescId createId(String... idValues) {
        return new iPartsOPSDescId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsOPSDescId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsOPSDescId)id;
    }
}
