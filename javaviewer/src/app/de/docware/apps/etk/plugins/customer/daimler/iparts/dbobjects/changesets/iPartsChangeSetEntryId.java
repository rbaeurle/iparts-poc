/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.db.ChangeSetEntryId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert einen Änderungsseteintrag (Änderungsstand von einem {@link de.docware.framework.modules.db.DBDataObject})
 * für die Tabelle DA_CHANGE_SET_ENTRY im iParts Plug-in.
 */
public class iPartsChangeSetEntryId extends ChangeSetEntryId {

    public static final String TYPE = "DA_iPartsChangeSetEntry";

    /**
     * Der normale Konstruktor
     *
     * @param guid
     * @param dataObjectType
     * @param dataObjectId
     */
    public iPartsChangeSetEntryId(String guid, String dataObjectType, String dataObjectId) {
        super(guid, dataObjectType, dataObjectId);
        idType = TYPE;
    }

    /**
     * Konstruktor für eine Änderungsset-ID und ID mit Typ von einem {@link de.docware.framework.modules.db.DBDataObject}.
     *
     * @param changeSetId
     * @param dataObjectIdWithType
     */
    public iPartsChangeSetEntryId(iPartsChangeSetId changeSetId, IdWithType dataObjectIdWithType) {
        super(changeSetId, dataObjectIdWithType);
        idType = TYPE;
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsChangeSetEntryId() {
        this("", "", "");
    }
}