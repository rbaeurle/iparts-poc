/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für die Tabelle DA_INVOICE_RELEVANCE
 */
public class iPartsDataInvoiceRelevance extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DIR_DO_TYPE, FIELD_DIR_FIELD };

    protected iPartsDataInvoiceRelevance(EtkProject project, iPartsInvoiceRelevanceId id) {
        super(KEYS);
        tableName = TABLE_DA_INVOICE_RELEVANCE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsInvoiceRelevanceId createId(String... idValues) {
        return new iPartsInvoiceRelevanceId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsInvoiceRelevanceId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsInvoiceRelevanceId)id;
    }
}
