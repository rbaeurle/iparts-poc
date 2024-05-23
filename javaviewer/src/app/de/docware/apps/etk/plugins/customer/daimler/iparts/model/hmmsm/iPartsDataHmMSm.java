/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle TABLE_DA_HMMSM.
 */
public class iPartsDataHmMSm extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DH_SERIES_NO, FIELD_DH_HM, FIELD_DH_M, FIELD_DH_SM };

    public iPartsDataHmMSm(EtkProject project, HmMSmId id) {
        super(KEYS);
        tableName = TABLE_DA_HMMSM;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public HmMSmId createId(String... idValues) {
        return new HmMSmId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public HmMSmId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (HmMSmId)id;
    }
}
