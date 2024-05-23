/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_WB_SUPPLIER_MAPPING.
 */
public class iPartsDataWbSupplierMapping extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DWSM_MODEL_TYPE_ID, FIELD_DWSM_PRODUCT_NO, FIELD_DWSM_KG_FROM };

    public iPartsDataWbSupplierMapping(EtkProject project, iPartsWbSupplierMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_WB_SUPPLIER_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsWbSupplierMappingId createId(String... idValues) {
        return new iPartsWbSupplierMappingId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsWbSupplierMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsWbSupplierMappingId)id;
    }

}
