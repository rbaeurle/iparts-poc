/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für die Tabelle {@link #TABLE_DA_KEM_RESPONSE_DATA}
 * für die Ident-Rückmeldungen aus ePEP (elektronischer ProduktionsEinsatzProzess)
 */
public class iPartsDataKemResponse extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_KRD_FACTORY, FIELD_KRD_KEM, FIELD_KRD_FIN };

    public iPartsDataKemResponse(EtkProject project, iPartsKemResponseId id) {
        super(KEYS);
        tableName = TABLE_DA_KEM_RESPONSE_DATA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsKemResponseId createId(String... idValues) {
        return new iPartsKemResponseId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsKemResponseId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsKemResponseId)id;
    }
}
