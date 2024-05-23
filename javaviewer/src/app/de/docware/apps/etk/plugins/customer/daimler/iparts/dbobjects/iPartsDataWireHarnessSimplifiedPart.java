/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_WH_SIMPLIFIED_PARTS.
 * Vereinfachte Teile zu Einzelteilen eines Leitungssatzbaukasten.
 */
public class iPartsDataWireHarnessSimplifiedPart extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DWHS_PARTNO, FIELD_DWHS_SUCCESSOR_PARTNO };

    public iPartsDataWireHarnessSimplifiedPart(EtkProject project, iPartsWireHarnessId id) {
        super(KEYS);
        tableName = TABLE_DA_WH_SIMPLIFIED_PARTS;

        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsWireHarnessSimplifiedPartId createId(String... idValues) {
        return new iPartsWireHarnessSimplifiedPartId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsWireHarnessSimplifiedPartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsWireHarnessSimplifiedPartId)id;
    }

}
