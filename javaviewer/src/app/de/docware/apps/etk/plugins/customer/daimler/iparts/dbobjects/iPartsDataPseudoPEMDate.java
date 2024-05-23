/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link de.docware.apps.etk.base.project.base.EtkDataObject} um iParts-spezifische Methoden und Daten für DA_PSEUDO_PEM_DATE.
 */
public class iPartsDataPseudoPEMDate extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPD_PEM_DATE };

    public iPartsDataPseudoPEMDate(EtkProject project, iPartsPseudoPEMDateId id) {
        super(KEYS);
        tableName = TABLE_DA_PSEUDO_PEM_DATE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPseudoPEMDateId createId(String... idValues) {
        return new iPartsPseudoPEMDateId(idValues[0]);
    }

    @Override
    public iPartsPseudoPEMDateId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsPseudoPEMDateId)id;
    }
}
