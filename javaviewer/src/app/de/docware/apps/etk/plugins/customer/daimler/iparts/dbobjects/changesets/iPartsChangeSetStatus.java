/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

/**
 * Status für ein Änderungsset {@link iPartsDataChangeSet}.
 */
public enum iPartsChangeSetStatus {
    NEW, IN_PROCESS, COMMITTED;

    public static iPartsChangeSetStatus getStatusByDbValue(String dbValue) {
        try {
            return iPartsChangeSetStatus.valueOf(dbValue);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
