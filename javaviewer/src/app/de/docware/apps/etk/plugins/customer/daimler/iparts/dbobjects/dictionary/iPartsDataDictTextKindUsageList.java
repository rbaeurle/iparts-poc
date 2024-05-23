/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Liste von {@link iPartsDataDictTextKindUsage}s (Liste der DB-Felder zu einer {@link iPartsDictTextKindId}).
 */
public class iPartsDataDictTextKindUsageList extends EtkDataObjectList<iPartsDataDictTextKindUsage> implements iPartsConst {

    public iPartsDataDictTextKindUsageList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictTextKindUsage}s, die der übergebenen <i>textKindId</i> zugeordnet sind.
     *
     * @param project
     * @param textKindId
     * @return
     */
    public static iPartsDataDictTextKindUsageList loadTextKindUsageList(EtkProject project, iPartsDictTextKindId textKindId) {
        iPartsDataDictTextKindUsageList list = new iPartsDataDictTextKindUsageList();
        list.loadTextKindUsagesFromDB(project, textKindId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictTextKindUsage}s, die dem übergebenen Feld (tableDotFieldName) zugeordnet sind.
     *
     * @param project
     * @param tableDotFieldName
     * @return
     */
    public static iPartsDataDictTextKindUsageList loadTextKindUsageForFieldList(EtkProject project, String tableDotFieldName) {
        iPartsDataDictTextKindUsageList list = new iPartsDataDictTextKindUsageList();
        list.loadTextKindUsagesForFieldFromDB(project, tableDotFieldName, DBActionOrigin.FROM_DB);
        return list;
    }

    public boolean containsField(String tableDotFiledName) {
        for (iPartsDataDictTextKindUsage dataDictTextKindUsage : this) {
            if (dataDictTextKindUsage.getFeld().equals(tableDotFiledName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictTextKindUsage}s, die der übergebenen {@link iPartsDictTextKindId} zugeordnet sind.
     *
     * @param project
     * @param textKindId
     * @param origin
     */
    private void loadTextKindUsagesFromDB(EtkProject project, iPartsDictTextKindId textKindId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_TKU_TXTKIND_ID };
        String[] whereValues = new String[]{ textKindId.getTextKindId() };

        searchAndFill(project, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictTextKindUsage}s, die dem übergebenen Feld (tableDotFieldName) zugeordnet sind.
     *
     * @param project
     * @param tableDotFieldName
     * @param origin
     */
    private void loadTextKindUsagesForFieldFromDB(EtkProject project, String tableDotFieldName, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_TKU_FELD };
        String[] whereValues = new String[]{ tableDotFieldName };

        searchAndFill(project, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    private void searchAndFill(EtkProject project, String[] whereFields, String[] whereValues, DBDataObjectList.LoadType loadType, DBActionOrigin origin) {
        searchAndFill(project, TABLE_DA_DICT_TXTKIND_USAGE, whereFields, whereValues, loadType, origin);
    }

    /**
     * Überprüft in der Liste, ob eine TextId benutzt ist
     *
     * @param textId
     * @return
     */
    public boolean isTextIdUsed(String textId) {
        for (iPartsDataDictTextKindUsage textKindUsage : this) {
            if (textKindUsage.isInUsage(textId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected iPartsDataDictTextKindUsage getNewDataObject(EtkProject project) {
        return new iPartsDataDictTextKindUsage(project, null);
    }
}
