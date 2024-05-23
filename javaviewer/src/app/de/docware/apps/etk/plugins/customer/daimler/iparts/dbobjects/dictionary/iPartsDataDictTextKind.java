/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.sql.TableAndFieldName;

import java.util.GregorianCalendar;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_DICT_TXTKIND.
 */
public class iPartsDataDictTextKind extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_DICT_TK_TXTKIND_ID };

    public static final String CHILDREN_NAME_USAGES = "iPartsDataDictTextKind.usages";

    protected iPartsDataDictTextKindUsageList usagesList;

    public iPartsDataDictTextKind(EtkProject project, iPartsDictTextKindId id) {
        super(KEYS);
        tableName = TABLE_DA_DICT_TXTKIND;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public void setChildren(String childrenName, DBDataObjectList<? extends DBDataObject> children) {
        super.setChildren(childrenName, children);
        if (childrenName.equals(CHILDREN_NAME_USAGES)) {
            usagesList = (iPartsDataDictTextKindUsageList)children;
        }
    }

    @Override
    public iPartsDataDictTextKind cloneMe(EtkProject project) {
        iPartsDataDictTextKind clone = new iPartsDataDictTextKind(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsDictTextKindId createId(String... idValues) {
        return new iPartsDictTextKindId(idValues[0]);
    }

    @Override
    public iPartsDictTextKindId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDictTextKindId)id;
    }

    @Override
    public void clear(DBActionOrigin origin) {
        super.clear(origin);
        setChildren(CHILDREN_NAME_USAGES, null);
    }

    protected synchronized void loadUsages() {
        if (usagesList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_USAGES, iPartsDataDictTextKindUsageList.loadTextKindUsageList(getEtkProject(), getAsId()));
    }

    public iPartsDataDictTextKindUsageList getUsages() {
        loadUsages();
        return usagesList;
    }

    public synchronized void addUsage(String tableName, String fieldName, String userId, DBActionOrigin origin) {
        addUsage(TableAndFieldName.make(tableName, fieldName), userId, origin);
    }

    public synchronized void addUsage(String tableDotFieldName, String userId, DBActionOrigin origin) {
        iPartsDictTextKindUsageId dictTextKindUsageId = new iPartsDictTextKindUsageId(getAsId().getTextKindId(), tableDotFieldName);
        iPartsDataDictTextKindUsage dataDictTextKindUsage = new iPartsDataDictTextKindUsage(getEtkProject(), dictTextKindUsageId);
        dataDictTextKindUsage.initAttributesWithEmptyValues(origin);
        dataDictTextKindUsage.setFieldValue(FIELD_DA_DICT_TKU_USERID, userId, origin);
        dataDictTextKindUsage.setFieldValueAsDate(FIELD_DA_DICT_TKU_CHANGE, GregorianCalendar.getInstance(), origin);
        getUsages().add(dataDictTextKindUsage, origin);
    }


    // Convenience Method
    public String getName(String language) {
        return getFieldValue(FIELD_DA_DICT_TK_NAME, language, true);
    }

    public DictTextKindTypes getForeignTextKindType() {
        return DictTextKindTypes.getType(getFieldValue(FIELD_DA_DICT_TK_FOREIGN_TKIND));
    }
}
