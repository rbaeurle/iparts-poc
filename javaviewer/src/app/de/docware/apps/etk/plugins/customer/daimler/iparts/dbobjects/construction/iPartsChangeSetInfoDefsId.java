/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

/**
 * Repräsentiert eine ID für die Tabelle DA_CHANGE_SET_INFO_DEFS im iParts Plug-in.
 */
public class iPartsChangeSetInfoDefsId extends IdWithType {

    public static final String TYPE = "DA_iPartsChangeSetInfoDef";

    protected enum INDEX {OBJECT_TYPE, TABLE_FIELD_NAME, AS_RELEVANT}

    /**
     * Der normale Konstruktor
     *
     * @param objectType
     * @param tableAndFieldName
     * @param asRelevant
     */
    public iPartsChangeSetInfoDefsId(String objectType, String tableAndFieldName, String asRelevant) {
        super(TYPE, new String[]{ objectType, tableAndFieldName, asRelevant });
    }

    /**
     * Der normale Konstruktor mit Boolean für {@code asRelevant}
     *
     * @param objectType
     * @param tableAndFieldName
     * @param asRelevant
     */
    public iPartsChangeSetInfoDefsId(String objectType, String tableAndFieldName, boolean asRelevant) {
        super(TYPE, new String[]{ objectType, tableAndFieldName, SQLStringConvert.booleanToPPString(asRelevant) });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsChangeSetInfoDefsId() {
        this("", "", "");
    }

    public String getObjectType() {
        return id[INDEX.OBJECT_TYPE.ordinal()];
    }

    public String getTableAndFieldName() {
        return id[INDEX.TABLE_FIELD_NAME.ordinal()];
    }

    public String getAsRelevant() {
        return id[INDEX.AS_RELEVANT.ordinal()];
    }

    public boolean isAsRelevant() {
        return SQLStringConvert.ppStringToBoolean(getAsRelevant());
    }
}
