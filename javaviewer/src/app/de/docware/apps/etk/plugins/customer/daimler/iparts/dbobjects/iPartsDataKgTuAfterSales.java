/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_KGTU_AS.
 */
public class iPartsDataKgTuAfterSales extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_DKM_PRODUCT, FIELD_DA_DKM_KG, FIELD_DA_DKM_TU };

    public iPartsDataKgTuAfterSales(EtkProject project, iPartsDataKgTuAfterSalesId id) {
        super(KEYS);
        tableName = TABLE_DA_KGTU_AS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataKgTuAfterSales cloneMe(EtkProject project) {
        iPartsDataKgTuAfterSales clone = new iPartsDataKgTuAfterSales(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsDataKgTuAfterSalesId createId(String... idValues) {
        return new iPartsDataKgTuAfterSalesId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsDataKgTuAfterSalesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDataKgTuAfterSalesId)id;
    }
}
