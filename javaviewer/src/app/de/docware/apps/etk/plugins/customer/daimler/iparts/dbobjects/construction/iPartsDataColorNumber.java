/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_COLOR_NUMBER.
 */
public class iPartsDataColorNumber extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCN_COLOR_NO };

    public iPartsDataColorNumber(EtkProject project, iPartsColorNumberId id) {
        super(KEYS);
        tableName = TABLE_DA_COLOR_NUMBER;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsColorNumberId createId(String... idValues) {
        return new iPartsColorNumberId(idValues[0]);
    }

    @Override
    public iPartsColorNumberId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsColorNumberId)id;
    }

}
