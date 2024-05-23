/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

/**
 * Status Optionen für einen Anhang zu einem Bildauftrag
 */
public enum iPartsAttachmentStatus {
    NEW("Neu", "ATTACHMENT_NEW"),
    SENT("Verschickt", "ATTACHMENT_SENT"),
    DENIED("Abgelehnt", "ATTACHMENT_DENIED"),
    ACCEPTED("Akzeptiert", "ATTACHMENT_ACCEPTED");

    private String statusValue;
    private String dbStatusValue;

    iPartsAttachmentStatus(String statusValue, String dbStatusValue) {
        this.statusValue = statusValue;
        this.dbStatusValue = dbStatusValue;
    }

    public String getStatus() {
        return statusValue;
    }

    public String getDBStatus() {
        return dbStatusValue;
    }


    public static iPartsAttachmentStatus getFromAlias(String alias) {
        for (iPartsAttachmentStatus result : values()) {
            if (result.statusValue.equals(alias)) {
                return result;
            }
        }
        return null;
    }

    public static iPartsAttachmentStatus getFromDBAlias(String dbAlias) {
        for (iPartsAttachmentStatus result : values()) {
            if (result.dbStatusValue.equals(dbAlias)) {
                return result;
            }
        }
        return null;
    }
}
