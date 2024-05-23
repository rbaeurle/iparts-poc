/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributesList;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_UM_USERS.
 */
public class iPartsDataASPLMUser extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_U_GUID };

    /**
     * Liefert den {@link iPartsDataASPLMUser} zurück basierend auf der übergebenen Benutzer ID (Feld {@code DA_U_ID}).
     *
     * @param project
     * @param userId
     * @return {@code null} falls kein oder mehrere Benutzer mit der übergebenen Benutzer ID gefunden wurden
     */
    public static iPartsDataASPLMUser getUserByUserId(EtkProject project, String userId) {
        DBDataObjectAttributesList userAttributesList = project.getDbLayer().getAttributesList(TABLE_DA_UM_USERS,
                                                                                               new String[]{ FIELD_DA_U_ID },
                                                                                               new String[]{ userId });
        if (userAttributesList.size() == 1) { // es darf nur genau einen Treffer geben
            iPartsDataASPLMUser user = new iPartsDataASPLMUser(project, null);
            user.assignAttributes(project, userAttributesList.get(0), true, DBActionOrigin.FROM_DB);
            return user;
        } else {
            return null;
        }
    }

    public iPartsDataASPLMUser(EtkProject project, iPartsASPLMUserId id) {
        super(KEYS);
        tableName = TABLE_DA_UM_USERS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsASPLMUserId createId(String... idValues) {
        return new iPartsASPLMUserId(idValues[0]);
    }

    @Override
    public iPartsASPLMUserId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsASPLMUserId)id;
    }

    // Convenience Method
    public String getUserASPLMId() {
        return getFieldValue(FIELD_DA_U_ID);
    }

    public String getUserAlias() {
        return getFieldValue(FIELD_DA_U_ALIAS);
    }

    public String getUserASPLMFirstName() {
        return getFieldValue(FIELD_DA_U_FIRSTNAME);
    }

    public String getUserASPLMLastName() {
        return getFieldValue(FIELD_DA_U_LASTNAME);
    }

}
