/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für {@link #TABLE_DA_PRIMUS_WW_PART}.
 */
public class iPartsDataPrimusWWPart extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_PWP_PART_NO, FIELD_PWP_ID, FIELD_PWP_WW_PART_NO };

    public iPartsDataPrimusWWPart(EtkProject project, iPartsPrimusWWPartId id) {
        super(KEYS);
        tableName = TABLE_DA_PRIMUS_WW_PART;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPrimusWWPartId createId(String... idValues) {
        return new iPartsPrimusWWPartId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsPrimusWWPartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsPrimusWWPartId)id;
    }

    @Override
    public iPartsDataPrimusWWPart cloneMe(EtkProject project) {
        iPartsDataPrimusWWPart clone = new iPartsDataPrimusWWPart(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }
}