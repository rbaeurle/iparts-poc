/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link de.docware.apps.etk.base.project.base.EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PICORDER_MODULES.
 */
public class iPartsDataPicOrderModules extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_POM_ORDER_GUID, FIELD_DA_POM_MODULE_NO };

    public iPartsDataPicOrderModules(EtkProject project, iPartsPicOrderModulesId id) {
        super(KEYS);
        tableName = TABLE_DA_PICORDER_MODULES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPicOrderModulesId createId(String... idValues) {
        return new iPartsPicOrderModulesId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsPicOrderModulesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsPicOrderModulesId)id;
    }

    @Override
    public iPartsDataPicOrderModules cloneMe(EtkProject project) {
        iPartsDataPicOrderModules clone = new iPartsDataPicOrderModules(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }
}
