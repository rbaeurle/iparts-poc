/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_GENERIC_INSTALL_LOCATION.
 */
public class iPartsDataGenInstallLocation extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DGIL_SERIES, FIELD_DGIL_HM, FIELD_DGIL_M, FIELD_DGIL_SM,
                                                       FIELD_DGIL_POSE, FIELD_DGIL_SDA };

    public iPartsDataGenInstallLocation(EtkProject project, iPartsGenInstallLocationId id) {
        super(KEYS);
        tableName = TABLE_DA_GENERIC_INSTALL_LOCATION;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsGenInstallLocationId createId(String... idValues) {
        return new iPartsGenInstallLocationId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsGenInstallLocationId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsGenInstallLocationId)id;
    }

    public String getGenVo() {
        return getFieldValue(iPartsConst.FIELD_DGIL_GEN_INSTALL_LOCATION);
    }

    public String getSplitSign() {
        return getFieldValue(iPartsConst.FIELD_DGIL_SPLIT_SIGN);
    }
}
