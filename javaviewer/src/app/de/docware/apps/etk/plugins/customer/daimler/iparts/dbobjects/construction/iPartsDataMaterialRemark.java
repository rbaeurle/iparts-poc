/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_EDS_MAT_REMARKS.
 */

public class iPartsDataMaterialRemark extends EtkDataObject implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DEMR_PART_NO, FIELD_DEMR_REV_FROM, FIELD_DEMR_REMARK_NO };

    public iPartsDataMaterialRemark(EtkProject project, iPartsMaterialRemarksId id) {
        super(KEYS);
        tableName = TABLE_DA_EDS_MAT_REMARKS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsMaterialRemarksId createId(String... idValues) {
        return new iPartsMaterialRemarksId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsMaterialRemarksId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsMaterialRemarksId)id;
    }
}
