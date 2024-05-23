/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_CODE.
 */
public class iPartsDataCode extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DC_CODE_ID, FIELD_DC_SERIES_NO, FIELD_DC_PGRP, FIELD_DC_SDATA, FIELD_DC_SOURCE };

    public iPartsDataCode(EtkProject project, iPartsCodeDataId id) {
        super(KEYS);
        tableName = TABLE_DA_CODE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsCodeDataId createId(String... idValues) {
        return new iPartsCodeDataId(idValues[0], idValues[1], idValues[2], idValues[3], iPartsImportDataOrigin.getTypeFromCode(idValues[4]));
    }

    @Override
    public iPartsCodeDataId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsCodeDataId)id;
    }
}
