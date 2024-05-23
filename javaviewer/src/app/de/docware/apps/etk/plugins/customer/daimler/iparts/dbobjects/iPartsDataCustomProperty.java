/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für CUSTPROP.
 */
public class iPartsDataCustomProperty extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_C_KVARI, FIELD_C_KVER, FIELD_C_KLFDNR, FIELD_C_MATNR,
                                                       FIELD_C_MVER, FIELD_C_KEY, FIELD_C_SPRACH };

    public iPartsDataCustomProperty(EtkProject project, iPartsCustomPropertyId id) {
        super(KEYS);
        tableName = TABLE_CUSTPROP;

        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsCustomPropertyId createId(String... idValues) {
        return new iPartsCustomPropertyId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5], idValues[6]);
    }

    @Override
    public iPartsCustomPropertyId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsCustomPropertyId)id;
    }
}
