/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_GENVO_PAIRING.
 */
public class iPartsDataGenVoPairing extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DGP_GENVO_L, FIELD_DGP_GENVO_R };

    public iPartsDataGenVoPairing(EtkProject project, iPartsGenVoPairingId id) {
        super(KEYS);
        tableName = TABLE_DA_GENVO_PAIRING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsGenVoPairingId createId(String... idValues) {
        return new iPartsGenVoPairingId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsGenVoPairingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsGenVoPairingId)id;
    }

    public String getGenVoLeft() {
        return getAsId().getGenVoLeft();
    }

    public String getGenVoRight() {
        return getAsId().getGenVoRight();
    }
}
