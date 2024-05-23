/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsReplaceConstPartId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle TABLE_DA_REPLACE_CONST_PART.
 */
public class iPartsDataReplaceConstPart extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DRCP_PART_NO, FIELD_DRCP_SDATA };

    public iPartsDataReplaceConstPart(EtkProject project, iPartsReplaceConstPartId id) {
        super(KEYS);
        tableName = TABLE_DA_REPLACE_CONST_PART;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsReplaceConstPartId createId(String... idValues) {
        return new iPartsReplaceConstPartId(idValues[0], idValues[1]);
    }


    @Override
    public iPartsReplaceConstPartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsReplaceConstPartId)id;
    }
}
