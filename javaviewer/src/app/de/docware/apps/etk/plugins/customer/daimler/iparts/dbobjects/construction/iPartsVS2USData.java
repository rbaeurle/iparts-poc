/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_VS2US_RELATION
 * .
 */
public class iPartsVS2USData extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_VUR_VEHICLE_SERIES, FIELD_VUR_VS_POS, FIELD_VUR_VS_POSV, FIELD_VUR_AA, FIELD_VUR_UNIT_SERIES, FIELD_VUR_DATA };

    public iPartsVS2USData(EtkProject project, iPartsVS2USDataId id) {
        super(KEYS);
        tableName = TABLE_DA_VS2US_RELATION;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsVS2USDataId createId(String... idValues) {
        return new iPartsVS2USDataId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsVS2USDataId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsVS2USDataId)id;
    }


}
