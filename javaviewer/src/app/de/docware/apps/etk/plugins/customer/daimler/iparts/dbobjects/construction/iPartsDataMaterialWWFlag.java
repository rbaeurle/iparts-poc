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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_EDS_MAT_WW_FLAGS.
 */
public class iPartsDataMaterialWWFlag extends EtkDataObject implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DEMW_PART_NO, FIELD_DEMW_REV_FROM, FIELD_DEMW_FLAG };

    public iPartsDataMaterialWWFlag(EtkProject project, iPartsMaterialWWFlagsId id) {
        super(KEYS);
        tableName = TABLE_DA_EDS_MAT_WW_FLAGS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsMaterialWWFlagsId createId(String... idValues) {
        return new iPartsMaterialWWFlagsId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsMaterialWWFlagsId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsMaterialWWFlagsId)id;
    }

}
