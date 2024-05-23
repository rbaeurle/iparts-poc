/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für die Tabelle {@link #TABLE_DA_NUTZDOK_REMARK}
 */
public class iPartsDataNutzDokRemark extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DNR_REF_ID, FIELD_DNR_REF_TYPE, FIELD_DNR_ID };

    public iPartsDataNutzDokRemark(EtkProject project, iPartsNutzDokRemarkId id) {
        super(KEYS);
        tableName = TABLE_DA_NUTZDOK_REMARK;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsNutzDokRemarkId createId(String... idValues) {
        return new iPartsNutzDokRemarkId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsNutzDokRemarkId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsNutzDokRemarkId)id;
    }
}
