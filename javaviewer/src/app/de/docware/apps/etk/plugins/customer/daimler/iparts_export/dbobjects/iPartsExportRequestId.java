/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects;

import de.docware.util.misc.id.IdWithType;

public class iPartsExportRequestId extends IdWithType {

    public static String TYPE = "DA_iPartsExportRequestId";

    protected enum INDEX {JOB_ID}

    /**
     * Der normale Konstruktor
     *
     * @param jobId
     */
    public iPartsExportRequestId(String jobId) {
        super(TYPE, new String[]{ jobId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsExportRequestId() {
        this("");
    }

    public String getJobId() {
        return id[INDEX.JOB_ID.ordinal()];
    }

}
