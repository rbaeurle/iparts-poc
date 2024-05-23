/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_COLORTABLE_DATA.
 */
public class iPartsDataColorTableData extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCTD_TABLE_ID };

    public iPartsDataColorTableData(EtkProject project, iPartsColorTableDataId id) {
        super(KEYS);
        tableName = TABLE_DA_COLORTABLE_DATA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsColorTableDataId createId(String... idValues) {
        return new iPartsColorTableDataId(idValues[0]);
    }

    @Override
    public iPartsColorTableDataId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsColorTableDataId)id;
    }

    public String getValidSeries() {
        return getFieldValue(FIELD_DCTD_VALID_SERIES);
    }
}
