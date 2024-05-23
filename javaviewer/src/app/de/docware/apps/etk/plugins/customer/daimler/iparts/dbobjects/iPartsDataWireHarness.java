/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_WIRE_HARNESS.
 * Leitungssatzbaukästen.
 */
public class iPartsDataWireHarness extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DWH_SNR, FIELD_DWH_REF, FIELD_DWH_CONNECTOR_NO, FIELD_DWH_SUB_SNR,
                                                       FIELD_DWH_POS };

    public iPartsDataWireHarness(EtkProject project, iPartsWireHarnessId id) {
        super(KEYS);
        tableName = TABLE_DA_WIRE_HARNESS;

        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsWireHarnessId createId(String... idValues) {
        return new iPartsWireHarnessId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4]);
    }

    @Override
    public iPartsWireHarnessId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsWireHarnessId)id;
    }

    @Override
    public iPartsDataWireHarness cloneMe(EtkProject project) {
        iPartsDataWireHarness clone = new iPartsDataWireHarness(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }
}
