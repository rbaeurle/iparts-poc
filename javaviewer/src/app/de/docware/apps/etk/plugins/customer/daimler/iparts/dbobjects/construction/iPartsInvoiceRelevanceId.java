/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine ID für die Tabelle DA_INVOICE_RELEVANCE im iParts Plug-in.
 */
public class iPartsInvoiceRelevanceId extends IdWithType {

    public static final String TYPE = "DA_iPartsInvoiceRelevance";

    protected enum INDEX {OBJECT_TYPE, TABLE_FIELD_NAME}

    /**
     * Der normale Konstruktor
     *
     * @param objectType
     * @param tableAndFieldName
     */
    public iPartsInvoiceRelevanceId(String objectType, String tableAndFieldName) {
        super(TYPE, new String[]{ objectType, tableAndFieldName });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsInvoiceRelevanceId() {
        this("", "");
    }

    public String getObjectType() {
        return id[INDEX.OBJECT_TYPE.ordinal()];
    }

    public String getTableAndFieldName() {
        return id[INDEX.TABLE_FIELD_NAME.ordinal()];
    }
}
