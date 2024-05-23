/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_SAA.
 */
public class iPartsDataSaa extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DS_SAA };

    public iPartsDataSaa(EtkProject project, iPartsSaaId id) {
        super(KEYS);
        tableName = TABLE_DA_SAA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSaaId createId(String... idValues) {
        return new iPartsSaaId(idValues[0]);
    }

    @Override
    public iPartsSaaId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSaaId)id;
    }

}
