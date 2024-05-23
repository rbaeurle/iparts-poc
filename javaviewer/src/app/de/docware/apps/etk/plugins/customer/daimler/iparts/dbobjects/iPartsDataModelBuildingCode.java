/*
 * Copyright (c) 2019 Docware GmbH
 *
 * Tabelle [DA_MODEL_BUILDING_CODE], Klasse für ein Element einer weiteren Liste mit bm-bildende Codes.
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_MODEL_BUILDING_CODE.
 */
public class iPartsDataModelBuildingCode extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DMBC_SERIES_NO, FIELD_DMBC_AA, FIELD_DMBC_CODE };

    public iPartsDataModelBuildingCode(EtkProject project, iPartsModelBuildingCodeId id) {
        super(KEYS);
        tableName = TABLE_DA_MODEL_BUILDING_CODE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsModelBuildingCodeId createId(String... idValues) {
        return new iPartsModelBuildingCodeId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsModelBuildingCodeId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsModelBuildingCodeId)id;
    }
}
