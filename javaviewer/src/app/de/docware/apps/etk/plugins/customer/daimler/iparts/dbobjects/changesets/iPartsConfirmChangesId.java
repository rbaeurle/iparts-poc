/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine ID zum Bestätigen von Änderungen in der Tabelle DA_CONFIRM_CHANGES im iParts Plug-in.
 */
public class iPartsConfirmChangesId extends IdWithType {

    public static String TYPE = "DA_iPartsConfirmChangesId";

    protected enum INDEX {CHANGE_SET_ID, DO_TYPE, DO_ID, PARTLIST_ENTRY_ID}

    /**
     * Der normale Konstruktor
     *
     * @param changeSetId
     * @param dataObjectType
     * @param dataObjectId
     * @param partListEntryId
     */
    public iPartsConfirmChangesId(String changeSetId, String dataObjectType, String dataObjectId, String partListEntryId) {
        super(TYPE, new String[]{ changeSetId, dataObjectType, dataObjectId, partListEntryId });
    }

    /**
     * Konstruktor für eine beliebige ID.
     *
     * @param changeSetId
     * @param id
     * @param partListEntryId
     */
    public iPartsConfirmChangesId(String changeSetId, IdWithType id, PartListEntryId partListEntryId) {
        this(changeSetId, id.getType(), id.toDBString(), partListEntryId.toDBString());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsConfirmChangesId() {
        this("", "", "", "");
    }

    public String getChangeSetId() {
        return id[INDEX.CHANGE_SET_ID.ordinal()];
    }

    public String getDataObjectType() {
        return id[INDEX.DO_TYPE.ordinal()];
    }

    public String getDataObjectId() {
        return id[INDEX.DO_ID.ordinal()];
    }

    public String getPartListEntryId() {
        return id[INDEX.PARTLIST_ENTRY_ID.ordinal()];
    }

    /**
     * Liefert die referenzierte {@link PartListEntryId} dieser Änderungs-Bestätigung zurück.
     *
     * @return {@code null} falls keine {@link PartListEntryId} referenziert ist
     */
    public PartListEntryId getAsPartListEntryId() {
        String partListEntryId = getPartListEntryId();
        if (!partListEntryId.isEmpty()) {
            IdWithType id = IdWithType.fromDBString(PartListEntryId.TYPE, partListEntryId);
            return new PartListEntryId(id.toStringArrayWithoutType());
        } else {
            return null;
        }
    }
}
