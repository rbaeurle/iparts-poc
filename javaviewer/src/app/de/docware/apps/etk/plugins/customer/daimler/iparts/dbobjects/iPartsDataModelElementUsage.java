/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_MODEL_ELEMENT_USAGE.
 */
public class iPartsDataModelElementUsage extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DMEU_MODELNO, FIELD_DMEU_MODULE, FIELD_DMEU_SUB_MODULE,
                                                       FIELD_DMEU_POS, FIELD_DMEU_LEGACY_NUMBER, FIELD_DMEU_REVFROM };

    public iPartsDataModelElementUsage(EtkProject project, iPartsModelElementUsageId id) {
        super(KEYS);
        tableName = TABLE_DA_MODEL_ELEMENT_USAGE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsModelElementUsageId createId(String... idValues) {
        return new iPartsModelElementUsageId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsModelElementUsageId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsModelElementUsageId)id;
    }
}
