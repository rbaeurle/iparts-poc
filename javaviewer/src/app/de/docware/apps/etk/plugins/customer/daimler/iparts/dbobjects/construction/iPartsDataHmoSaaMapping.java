/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_HMO_SAA_MAPPING.
 */
public class iPartsDataHmoSaaMapping extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DHSM_HMO };

    public iPartsDataHmoSaaMapping(EtkProject project, iPartsHmoSaaMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_HMO_SAA_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsHmoSaaMappingId createId(String... idValues) {
        return new iPartsHmoSaaMappingId(idValues[0]);
    }

    @Override
    public iPartsHmoSaaMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsHmoSaaMappingId)id;
    }

    public String getHMONumber() {
        return getAsId().getHMONumber();
    }

    public String getSAANumber() {
        return getFieldValue(FIELD_DHSM_SAA);
    }
}
