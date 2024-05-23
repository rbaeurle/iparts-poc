/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.AbstractCondition;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.ConditionList;

import java.util.ArrayList;
import java.util.List;

/**
 * Liste mit {@link iPartsDataColorTableFactory} Objekten
 * (Werkseinsatzdaten für Farbvariantentabellen bzw. Farbvarianteninhalte)
 */
public class iPartsDataColorTableFactoryList extends EtkDataObjectList<iPartsDataColorTableFactory> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller Werkseinsatzdaten für eine {@link iPartsColorTableToPartId} ABER ohne Berücksichtigung des
     * ADAT-Primärschlüsselfeldes. Damit werden also alle Stände der Werkseinsatzdaten geladen
     */
    public static iPartsDataColorTableFactoryList loadAllColorTableFactoryForColorTableFactoryId(EtkProject project, iPartsColorTableFactoryId colorTableFactoryId) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadAllColorTableFactory(project, colorTableFactoryId.getTableId(), colorTableFactoryId.getPos(), colorTableFactoryId.getFactory(),
                                      colorTableFactoryId.getDataId(), colorTableFactoryId.getSdata(), null, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataColorTableFactoryList loadAllColorTableFactoryForColorTableFactoryId(EtkProject project, iPartsColorTableFactoryId colorTableFactoryId, iPartsDataReleaseState state) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadAllColorTableFactory(project, colorTableFactoryId.getTableId(), colorTableFactoryId.getPos(), colorTableFactoryId.getFactory(),
                                      colorTableFactoryId.getDataId(), colorTableFactoryId.getSdata(), state, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableFactory}s für die übergebene Baureihe und Quelle
     *
     * @param project
     * @param seriesNo
     * @param dataOrigin
     * @return
     */
    public static iPartsDataColorTableFactoryList loadColorTableFactoryForSeriesAndSource(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadAllForSeriesAndOrigin(project, seriesNo, dataOrigin, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllForSeriesAndOrigin(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = { FIELD_DCCF_TABLE_ID, FIELD_DCCF_SOURCE };
        String[] whereValues = { ColorTableHelper.makeWhereValueForColorTableWithSeries(new iPartsSeriesId(seriesNo)) + "*", dataOrigin.getOrigin() };

        searchAndFillWithLike(project, TABLE_DA_COLORTABLE_FACTORY, null, whereFields, whereValues, LoadType.ONLY_IDS, false, DBActionOrigin.FROM_DB);
    }


    public static iPartsDataColorTableFactoryList loadColorTableFactoryASForSeriesAndPEM(EtkProject project, String seriesNo, String pem) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadAllASForSeriesAndPem(project, seriesNo, pem, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllASForSeriesAndPem(EtkProject project, String seriesNo, String pem, DBActionOrigin origin) {
        DBDataObjectAttributesList attributesList;
        DBDataSet dbSet = null;
        try {
            String[] selectFieldNames;

            // Alle Felder der Tabelle bestimmen
            EtkDatabaseTable tableDef = project.getConfig().getDBDescription().findTable(TABLE_DA_COLORTABLE_FACTORY);
            if (tableDef != null) {
                selectFieldNames = ArrayUtil.toStringArray(tableDef.getAllFieldsNoBlob());
            } else {
                Logger.getLogger().throwRuntimeException("No table definition for '" + TABLE_DA_COLORTABLE_FACTORY + "'");
                return;
            }

            List<AbstractCondition> conditionList = new ArrayList<>();
            conditionList.add(new Condition(FIELD_DCCF_PEMA, Condition.OPERATOR_EQUALS, pem));
            conditionList.add(new Condition(FIELD_DCCF_PEMB, Condition.OPERATOR_EQUALS, pem));
            AbstractCondition pemCondition = new ConditionList(conditionList, true);

            // AS Datensätze können den Kenner "VX9" oder "VX10" haben -> or-Condition
            conditionList = new ArrayList<>();
            conditionList.add(new Condition(FIELD_DCCF_DATA_ID, Condition.OPERATOR_EQUALS, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDbValue()));
            conditionList.add(new Condition(FIELD_DCCF_DATA_ID, Condition.OPERATOR_EQUALS, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDbValue()));
            AbstractCondition dataTypeCondition = new ConditionList(conditionList, true);

            String colorTableIdFromSeries = SQLUtils.wildcardExpressionToSQLLike(ColorTableHelper.makeWhereValueForColorTableWithSeries(new iPartsSeriesId(seriesNo)), false, false, false);

            DBSQLQuery query = project.getDB().getDBForTable(TABLE_DA_COLORTABLE_FACTORY).getNewQuery();
            query.select(selectFieldNames)
                    .from(TABLE_DA_COLORTABLE_FACTORY)
                    .where(new Condition(FIELD_DCCF_TABLE_ID, Condition.OPERATOR_LIKE, colorTableIdFromSeries))
                    .and(pemCondition)
                    .and(dataTypeCondition);

            attributesList = new DBDataObjectAttributesList();
            dbSet = query.executeQuery();
            while (dbSet.next()) {
                List<String> strList = dbSet.getStringList();
                DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                for (int iField = 0; iField < selectFieldNames.length; iField++) {
                    attributes.addField(selectFieldNames[iField], strList.get(iField), DBActionOrigin.FROM_DB);
                }
                attributesList.add(attributes);
            }
        } finally {
            if (dbSet != null) {
                dbSet.close();
            }
        }

        fillAndAddDataObjectsFromAttributesList(project, attributesList, LoadType.COMPLETE, origin);
    }

    private void loadAllColorTableFactory(EtkProject project, String tableId, String pos, String factory, String dataId,
                                          String sdata, iPartsDataReleaseState state, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = { FIELD_DCCF_TABLE_ID, FIELD_DCCF_POS, FIELD_DCCF_FACTORY, FIELD_DCCF_DATA_ID, FIELD_DCCF_SDATA };
        String[] whereValues = { tableId, pos, factory, dataId, sdata };
        if (state != null) {
            whereFields = StrUtils.mergeArrays(whereFields, FIELD_DCCF_STATUS);
            whereValues = StrUtils.mergeArrays(whereValues, state.getDbValue());
        }
        searchSortAndFill(project, TABLE_DA_COLORTABLE_FACTORY, whereFields, whereValues, new String[]{ FIELD_DCCF_ADAT }, LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableFactory}s mit Status "NEW" für Farbtabelleninhalte.
     * Dabei werden alle Felder geladen.
     *
     * @param project
     * @return
     */
    public static iPartsDataColorTableFactoryList loadNewColorTableContentFactoryData(EtkProject project) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadNewColorTableContentFactoryDataFromDB(project);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableFactory}s mit Status "NEW" für Teil zu Farbtabellen
     * Beziehungen.
     * Dabei werden alle Felder geladen.
     *
     * @param project
     * @return
     */
    public static iPartsDataColorTableFactoryList loadNewColorTableToPartFactoryData(EtkProject project) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadNewColorTableToPartFactoryDataFromDB(project);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableFactory}s für eine Variantentabelle mit den Produktions-Werkseinsatzdaten.
     *
     * @param project
     * @param colorTableToPartId Farbtabelle-GUID
     * @return
     */
    public static iPartsDataColorTableFactoryList loadColorTableFactoryListForColorTableToPartId(EtkProject project, iPartsColorTableToPartId colorTableToPartId) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadColorTableFactoryListForColorTableIdFromDB(project, colorTableToPartId.getColorTableId(), colorTableToPartId.getPosition(),
                                                            colorTableToPartId.getSDATA(), iPartsFactoryDataTypes.COLORTABLE_PART, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataColorTableFactoryList loadColorTableFactoryListForSpecificFields(EtkProject project, String[] whereFields, String[] whereValues) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadColorTableFactoryListForSpecificFieldsFromDB(project, whereFields, whereValues, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadColorTableFactoryListForSpecificFieldsFromDB(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_COLORTABLE_FACTORY, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    public static iPartsDataColorTableFactoryList loadAllColortableFactoryDataForColortableId(EtkProject project, iPartsColorTableDataId colortableId, iPartsImportDataOrigin dataOrigin) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        String[] whereFields = new String[]{ FIELD_DCCF_TABLE_ID, FIELD_DCCF_SOURCE };
        String[] whereValues = new String[]{ colortableId.getColorTableId(), dataOrigin.getOrigin() };
        list.loadColorTableFactoryListForSpecificFieldsFromDB(project, whereFields, whereValues, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableFactory}s für eine Variantentabelle mit den After-Sales-Werkseinsatzdaten.
     *
     * @param project
     * @param colorTableToPartId Farbtabelle-GUID
     * @return
     */
    public static iPartsDataColorTableFactoryList loadColorTableFactoryListForColorTableToPartIdForAS(EtkProject project, iPartsColorTableToPartId colorTableToPartId) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadColorTableFactoryListForColorTableIdFromDB(project, colorTableToPartId.getColorTableId(), colorTableToPartId.getPosition(),
                                                            colorTableToPartId.getSDATA(), iPartsFactoryDataTypes.COLORTABLE_PART_AS, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableFactory}s für eine Variante mit den Produktions-Werkseinsatzdaten.
     *
     * @param project
     * @param colorTableContentId Farbtabelle-GUID
     * @return
     */
    public static iPartsDataColorTableFactoryList loadColorTableFactoryListForColorTableContentId(EtkProject project, iPartsColorTableContentId colorTableContentId) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadColorTableFactoryListForColorTableIdFromDB(project, colorTableContentId.getColorTableId(), colorTableContentId.getPosition(),
                                                            colorTableContentId.getSDATA(), iPartsFactoryDataTypes.COLORTABLE_CONTENT, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableFactory}s für eine Variante mit den After-Sales-Werkseinsatzdaten.
     *
     * @param project
     * @param colorTableContentId Farbtabelle-GUID
     * @return
     */
    public static iPartsDataColorTableFactoryList loadColorTableFactoryListForColorTableContentIdForAS(EtkProject project, iPartsColorTableContentId colorTableContentId) {
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        list.loadColorTableFactoryListForColorTableIdFromDB(project, colorTableContentId.getColorTableId(), colorTableContentId.getPosition(),
                                                            colorTableContentId.getSDATA(), iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadNewColorTableContentFactoryDataFromDB(EtkProject project) {
        JoinData joinData = new JoinData(TABLE_DA_COLORTABLE_CONTENT,
                                         new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_TABLE_ID),
                                                       TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_POS),
                                                       TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_SDATA) },
                                         new String[]{ FIELD_DCTC_TABLE_ID,
                                                       FIELD_DCTC_POS,
                                                       FIELD_DCTC_SDATA },
                                         false, false);


        loadNewColorFactoryDataFromDB(project, joinData);
    }

    private void loadNewColorTableToPartFactoryDataFromDB(EtkProject project) {
        // Join auf die Teil zu Farbtabellen Tabelle
        JoinData joinDataToPart = new JoinData(TABLE_DA_COLORTABLE_PART,
                                               new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_TABLE_ID),
                                                             TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_POS),
                                                             TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_SDATA) },
                                               new String[]{ FIELD_DCTP_TABLE_ID,
                                                             FIELD_DCTP_POS,
                                                             FIELD_DCTP_SDATA },
                                               false, false);
        // Join auf die Farbtabellenstammdaten
        JoinData joinDataData = new EtkDataObjectList.JoinData(TABLE_DA_COLORTABLE_DATA,
                                                               new String[]{ FIELD_DCCF_TABLE_ID },
                                                               new String[]{ FIELD_DCTD_TABLE_ID },
                                                               false, false);


        loadNewColorFactoryDataFromDB(project, joinDataToPart, joinDataData);
    }

    private void loadNewColorFactoryDataFromDB(EtkProject project, EtkDataObjectList.JoinData... additionalJoinData) {
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_COLORTABLE_FACTORY));
        if (additionalJoinData != null) {
            for (JoinData joinData : additionalJoinData) {
                selectFields.addFelder(project.getAllDisplayFieldsForTable(joinData.joinTable));
            }
        }
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DCCF_STATUS },
                                  new String[]{ iPartsDataReleaseState.NEW.getDbValue() },
                                  false, null,
                                  false, null,
                                  additionalJoinData);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataColorTableFactory}s
     *
     * @param project
     * @param colorTableId Farbtabellen-ID vom {@link iPartsDataColorTableFactory}-Datensatz
     * @param position     Position vom {@link iPartsDataColorTableFactory}-Datensatz
     * @param sdata        "Datum ab" vom {@link iPartsDataColorTableFactory}-Datensatz
     * @param type         Typ der Werkseinsatzdaten
     * @param origin
     */
    public void loadColorTableFactoryListForColorTableIdFromDB(EtkProject project, String colorTableId, String position,
                                                               String sdata, iPartsFactoryDataTypes type, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_COLORTABLE_FACTORY,
                          new String[]{ FIELD_DCCF_TABLE_ID,
                                        FIELD_DCCF_POS,
                                        FIELD_DCCF_SDATA,
                                        FIELD_DCCF_DATA_ID },
                          new String[]{ colorTableId,
                                        position,
                                        sdata,
                                        type.getDbValue() },
                          new String[]{ FIELD_DCCF_FACTORY, FIELD_DCCF_SDATA },
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataColorTableFactory getNewDataObject(EtkProject project) {
        return new iPartsDataColorTableFactory(project, null);
    }
}