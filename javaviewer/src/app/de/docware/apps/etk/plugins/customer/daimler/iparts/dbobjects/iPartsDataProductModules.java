/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModulesId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PRODUCT_MODULES.
 */
public class iPartsDataProductModules extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPM_PRODUCT_NO, FIELD_DPM_MODULE_NO };

    public iPartsDataProductModules(EtkProject project, iPartsProductModulesId id) {
        super(KEYS);
        tableName = TABLE_DA_PRODUCT_MODULES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    public void setPKValues(String productNo, String moduleNo, DBActionOrigin origin) {
        clear(origin);
        setId(createId(productNo, moduleNo), origin);
    }

    @Override
    public iPartsProductModulesId createId(String... idValues) {
        return new iPartsProductModulesId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsProductModulesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsProductModulesId)id;
    }
}
