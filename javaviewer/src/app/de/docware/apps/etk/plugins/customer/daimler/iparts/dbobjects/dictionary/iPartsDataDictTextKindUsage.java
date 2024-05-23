/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.util.sql.TableAndFieldName;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_DICT_TXTKIND_USAGE.
 */
public class iPartsDataDictTextKindUsage extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_DICT_TKU_TXTKIND_ID, FIELD_DA_DICT_TKU_FELD };

    public iPartsDataDictTextKindUsage(EtkProject project, iPartsDictTextKindUsageId id) {
        super(KEYS);
        tableName = TABLE_DA_DICT_TXTKIND_USAGE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataDictTextKindUsage cloneMe(EtkProject project) {
        iPartsDataDictTextKindUsage clone = new iPartsDataDictTextKindUsage(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsDictTextKindUsageId createId(String... idValues) {
        return new iPartsDictTextKindUsageId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsDictTextKindUsageId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDictTextKindUsageId)id;
    }

    // Convenience Method
    public String getFeld() {
        return getFieldValue(FIELD_DA_DICT_TKU_FELD);
    }

    /**
     * Überprüft, ob eine TextId in der Tabelle TableDotFieldName (FIELD_DA_DICT_TKU_FELD) benutzt wird
     *
     * @param textId
     * @return
     */
    public boolean isInUsage(String textId) {
        String dbField = getFeld();
        String[] whereFields = new String[]{ TableAndFieldName.getFieldName(dbField) };
        String[] whereValues = new String[]{ textId };

        DBDataObjectAttributesList attributesList = ((EtkProject)project).getDbLayer().getAttributesList(TableAndFieldName.getTableName(dbField),
                                                                                                         whereFields, whereValues);
        return !attributesList.isEmpty();
    }
}
