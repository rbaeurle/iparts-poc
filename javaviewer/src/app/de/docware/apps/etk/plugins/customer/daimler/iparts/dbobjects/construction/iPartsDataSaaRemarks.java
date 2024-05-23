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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_EDS_SAA_REMARKS.
 */
public class iPartsDataSaaRemarks extends EtkDataObject implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DESR_SAA, FIELD_DESR_REV_FROM, FIELD_DESR_REMARK_NO };

    public iPartsDataSaaRemarks(EtkProject project, iPartsSaaRemarksId id) {
        super(KEYS);
        tableName = TABLE_DA_EDS_SAA_REMARKS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSaaRemarksId createId(String... idValues) {
        return new iPartsSaaRemarksId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsSaaRemarksId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSaaRemarksId)id;
    }

}
