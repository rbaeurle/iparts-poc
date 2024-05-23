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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_UM_GROUPS.
 */
public class iPartsDataASPLMGroup extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_G_GUID };

    /**
     * Liefert die {@link iPartsDataASPLMGroup} zurück basierend auf der übergebenen Gruppen ID (Feld {@code DA_G_ID}).
     *
     * @param project
     * @param groupId
     * @return {@code null} falls keine oder mehrere Gruppen mit der übergebenen Gruppen ID gefunden wurden
     */
    public static iPartsDataASPLMGroup getGroupByGroupId(EtkProject project, String groupId) {
        DBDataObjectAttributesList groupAttributesList = project.getDbLayer().getAttributesList(TABLE_DA_UM_GROUPS,
                                                                                                new String[]{ FIELD_DA_G_ID },
                                                                                                new String[]{ groupId });
        if (groupAttributesList.size() == 1) { // es darf nur genau einen Treffer geben
            iPartsDataASPLMGroup group = new iPartsDataASPLMGroup(project, null);
            group.assignAttributes(project, groupAttributesList.get(0), true, DBActionOrigin.FROM_DB);
            return group;
        } else {
            return null;
        }
    }

    public iPartsDataASPLMGroup(EtkProject project, iPartsASPLMGroupId id) {
        super(KEYS);
        tableName = TABLE_DA_UM_GROUPS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsASPLMGroupId createId(String... idValues) {
        return new iPartsASPLMGroupId(idValues[0]);
    }

    @Override
    public iPartsASPLMGroupId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsASPLMGroupId)id;
    }

    // Convenience Method
    public String getASPLMGroupId() {
        return getFieldValue(FIELD_DA_G_ID);
    }

    public String getGroupAlias() {
        return getFieldValue(FIELD_DA_G_ALIAS);
    }
}
