/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.db.ChangeSetId;

/**
 * Repräsentiert ein Änderungsset für die Tabelle DA_CHANGE_SET im iParts Plug-in.
 */
public class iPartsChangeSetId extends ChangeSetId {

    public static final String TYPE = "DA_iPartsChangeSet";

    /**
     * Der normale Konstruktor
     *
     * @param guid
     */
    public iPartsChangeSetId(String guid) {
        super(guid);
        idType = TYPE;
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsChangeSetId() {
        this("");
    }
}