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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_EDS_SAA_WW_FLAGS.
 */
public class iPartsDataSaaWWFlags extends EtkDataObject implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DESW_SAA, FIELD_DESW_REV_FROM, FIELD_DESW_FLAG };

    public iPartsDataSaaWWFlags(EtkProject project, iPartsSaaWWFlagsId id) {
        super(KEYS);
        tableName = TABLE_DA_EDS_SAA_WW_FLAGS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSaaWWFlagsId createId(String... idValues) {
        return new iPartsSaaWWFlagsId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsSaaWWFlagsId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSaaWWFlagsId)id;
    }

}
