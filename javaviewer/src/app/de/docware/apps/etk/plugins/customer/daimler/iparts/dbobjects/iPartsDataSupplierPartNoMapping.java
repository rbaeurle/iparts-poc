/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_SUPPLIER_PARTNO_MAPPING.
 */
public class iPartsDataSupplierPartNoMapping extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DSPM_PARTNO, FIELD_DSPM_SUPPLIER_PARTNO, FIELD_DSPM_SUPPLIER_NO };

    public iPartsDataSupplierPartNoMapping(EtkProject project, iPartsSupplierPartNoMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_SUPPLIER_PARTNO_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSupplierPartNoMappingId createId(String... idValues) {
        return new iPartsSupplierPartNoMappingId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsSupplierPartNoMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsSupplierPartNoMappingId)id;
    }

    public String getFieldValuesToDbString() {
        return getFieldValue(FIELD_DSPM_SUPPLIER_NAME);
    }

    public void setFieldValuesFromDbString(String dbValue, DBActionOrigin origin) {
        setFieldValue(FIELD_DSPM_SUPPLIER_NAME, dbValue, origin);
    }
}
