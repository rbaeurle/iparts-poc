/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle: [DA_FN_POS], Fußnoten zur
 * Teileposition aus DIALOG, (VBFN)
 */
public class iPartsDataFootNotePosRef extends AbstractFootnoteRef implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DFNP_GUID, FIELD_DFNP_SESI, FIELD_DFNP_POSP, FIELD_DFNP_FN_NO };

    public iPartsDataFootNotePosRef(EtkProject project, iPartsFootNotePosRefId id) {
        super(KEYS);
        tableName = TABLE_DA_FN_POS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsFootNotePosRefId createId(String... idValues) {
        return new iPartsFootNotePosRefId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsFootNotePosRefId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsFootNotePosRefId)id;
    }

    @Override
    protected String getFootnoteId() {
        return getAsId().getFnNo();
    }
}
