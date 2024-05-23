/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeriesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.SearchMatchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.utils.SortType;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.sql.SQLParameterList;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.LeftOuterJoin;
import de.docware.util.sql.terms.Tables;

import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Singleton Object pro Projekt, das die HM/M/SM (Dialog)-Struktur komplett enthält.
 */
public class HmMSm implements EtkDbConst, iPartsConst {

    // Baureihen-abhängige HMMSM Knoten
    private static ObjectInstanceLRUList<Object, HmMSm> instancesWithSeries = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_SERIES,
                                                                                                          MAX_CACHE_LIFE_TIME_CORE);
    // Alle HMMSM Knoten für das übergebene {@link EtkProject}
    private static ObjectInstanceLRUList<Object, List<HmMSm>> instances = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS,
                                                                                                      MAX_CACHE_LIFE_TIME_CORE);

    // Knoten der ersten Ebene
    protected HmMSmNodes nodes = new HmMSmNodes();
    protected iPartsSeriesId seriesId;

    public static synchronized void clearCache() {
        instances.clear();
        instancesWithSeries.clear();
    }

    /**
     * Hole ein HMMSM Objekt, das zu einer Baureihe und einem {@link EtkProject} gehört
     *
     * @param project
     * @param seriesId
     * @return
     */
    public static synchronized HmMSm getInstance(EtkProject project, iPartsSeriesId seriesId) {
        // Eindeutige HashObjekte für baureihenspezifische und EtkProjekt-spezifische HMMSM Objekte
        Object hashObjectForSeriesInstances = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), HmMSm.class, seriesId.getSeriesNumber(), false);
        // Hole das zur Baureihe gehörende HMMSM Objekt
        HmMSm result = instancesWithSeries.get(hashObjectForSeriesInstances);
        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new HmMSm(seriesId);
            result.load(project);
            // Nicht speichern, wenn keine HMMSM Daten zu dieser Baureihe existieren
            if (!result.nodes.getValues().isEmpty()) {
                instancesWithSeries.put(hashObjectForSeriesInstances, result);

                // Projektspezifischen baureihenunabhängigen Cache löschen, da nicht mehr gültig
                Object hashObjectInstances = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), HmMSm.class, null, false);
                instances.removeKey(hashObjectInstances);
            }
        }

        return result;
    }

    /**
     * Hole alle HMMSM Objekte die zu einem {@link EtkProject} gehören
     *
     * @param project
     * @return
     */
    public static synchronized List<HmMSm> getALLHmMSmInstances(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), HmMSm.class, null, false);
        List<HmMSm> result = instances.get(hashObject);
        if (result == null) {
            // Alle HMMSM Knoten durchsuchen:  1.Möglichkeit: Alle Baureihen bestimmen und zu jeder Baureihe die HMMSM laden
            //                                 2.Möglichkeit: direkt via statements alle HMMSM auslesen
            //                                 3.Möglichkeit?
            List<HmMSm> listOfAllHmMSm = new ArrayList<HmMSm>();
            // Hole alle Baureihen
            iPartsDataSeriesList list = iPartsDataSeriesList.loadDataSeriesList(project, DBDataObjectList.LoadType.ONLY_IDS);
            // F+ür jede Baureihe: Erzeuge ein HashObject und Speicher das HMMSM Objekt zur spezifischen Baureihe;
            // Zusätzlich werden die erzeugten HMMSM Objekte allgemein (projektspezifisch) gespeichert -> für die Suche
            for (iPartsDataSeries series : list.getAsList()) {
                Object hashObjectForSeriesInstances = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), HmMSm.class, series.getAsId().getSeriesNumber(), false);
                HmMSm hmMSm = new HmMSm(series.getAsId());
                hmMSm.load(project);
                // Nicht speichern, wenn keine HMMSM Daten zu dieser Baureihe existieren
                if (!hmMSm.nodes.getValues().isEmpty()) {
                    instancesWithSeries.put(hashObjectForSeriesInstances, hmMSm);
                    listOfAllHmMSm.add(hmMSm);
                }
            }
            instances.put(hashObject, listOfAllHmMSm);
            result = listOfAllHmMSm;
        }
        return result;
    }

    public HmMSm(iPartsSeriesId seriesId) {
        this.seriesId = seriesId;
    }

    private void load(EtkProject project) {
        nodes = new HmMSmNodes();

        // Die Liste der HmMSm-Knoten auf einen Schlag laden und aus dem Tabelleninhalt die Knoten erzeugen
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_HMMSM, new String[]{ FIELD_DH_SERIES_NO }, new String[]{ seriesId.getSeriesNumber() });
        attributesList.sort(new String[]{ FIELD_DH_HM, FIELD_DH_M, FIELD_DH_SM },
                            SortType.AUTOMATIC);

        for (DBDataObjectAttributes attributes : attributesList) {
            String hm = attributes.getFieldValue(FIELD_DH_HM);
            String m = attributes.getFieldValue(FIELD_DH_M);
            String sm = attributes.getFieldValue(FIELD_DH_SM);

            // Alle Knotenebenen suchen und falls nicht vorhanden erzeugen
            HmMSmNode hmNode = nodes.getOrCreate(HmMSmType.HM, hm, null);
            hmNode.setSeriesId(seriesId);
            if (m.isEmpty()) { // Es handelt sich um den Datensatz für einen HM-Knoten
                setNodeBooleanAttributes(hmNode, attributes);
            } else {
                HmMSmNode mNode = hmNode.getOrCreateChild(HmMSmType.M, m, hmNode);
                if (sm.isEmpty()) { // Es handelt sich um den Datensatz für einen M-Knoten
                    setNodeBooleanAttributes(mNode, attributes);
                } else { // Es handelt sich um den Datensatz für einen SM-Knoten
                    HmMSmNode smNode = mNode.getOrCreateChild(HmMSmType.SM, sm, mNode);
                    setNodeBooleanAttributes(smNode, attributes);
                }
            }
        }

        loadTexts(project);
    }

    /**
     * Setzt die Attribute, die hierarchisch vererbt werden
     *
     * @param node
     * @param attributes
     */
    private void setNodeBooleanAttributes(HmMSmNode node, DBDataObjectAttributes attributes) {
        node.setHidden(attributes.getField(FIELD_DH_HIDDEN).getAsBoolean());
        node.setNoCalc(attributes.getField(FIELD_DH_NO_CALCULATION).getAsBoolean());
        node.setChangeDocuRelOmittedPart(attributes.getField(FIELD_DH_SPECIAL_CALC_OMITTED_PARTS).getAsBoolean());
    }

    private void loadTexts(EtkProject project) {
        DBSQLQuery query = project.getDB().getDBForDomain(MAIN).getNewQuery();
        SQLParameterList params = new SQLParameterList();

        List<String> resultFields = new ArrayList<String>();
        //
        resultFields.add(FIELD_DH_SERIES_NO);
        resultFields.add(FIELD_DH_HM);
        resultFields.add(FIELD_DH_M);
        resultFields.add(FIELD_DH_SM);
        resultFields.add(FIELD_DH_PICTURE);
        resultFields.add(EtkDbConst.FIELD_S_SPRACH);
        resultFields.add(EtkDbConst.FIELD_S_BENENN);
        resultFields.add(EtkDbConst.FIELD_S_TEXTID);
        // Erst ab der neuen Datenbankversion ist das folgende Feld vorhanden:
        if (project.getEtkDbs().configBase.getDataBaseVersion() >= 6.2) {
            resultFields.add(EtkDbConst.FIELD_S_BENENN_LANG);
        }

        query.select(new Fields(resultFields)).from(new Tables(TABLE_DA_HMMSMDESC)).where(new Condition(params, FIELD_DH_SERIES_NO, "=", seriesId.getSeriesNumber()));

        query.join(new LeftOuterJoin(TABLE_SPRACHE,
                                     new Condition(TableAndFieldName.make(TABLE_DA_HMMSMDESC, FIELD_DH_DESC).toLowerCase(), "=",
                                                   new Fields(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTNR))).
                                             and(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_FELD), "=",
                                                               TableAndFieldName.make(TABLE_DA_HMMSMDESC, FIELD_DH_DESC)))));


        DBDataSet dbSet = query.executeQuery(params);
        while (dbSet.next()) {

            EtkRecord rec = dbSet.getRecord(resultFields.toArray(new String[resultFields.size()]));

            HmMSmId hmMSmId = new HmMSmId(rec.getField(FIELD_DH_SERIES_NO).getAsString(), rec.getField(FIELD_DH_HM).getAsString(), rec.getField(FIELD_DH_M).getAsString(),
                                          rec.getField(FIELD_DH_SM).getAsString());

            HmMSmNode node = getNode(hmMSmId);

            if (node != null) {
                node.setTitle(rec.getField(EtkDbConst.FIELD_S_SPRACH).getAsString(), project.getEtkDbs().getLongTextFromRecord(rec), rec.getField(EtkDbConst.FIELD_S_TEXTID).getAsString());
                node.setPictureName(rec.getField(FIELD_DH_PICTURE).getAsString());
            }
        }
        dbSet.close();
    }

    public HmMSmNode getHmNode(String hm) {
        return nodes.get(hm);
    }

    public HmMSmNode getMNode(String hm, String m) {
        HmMSmNode hmNode = nodes.get(hm);
        if (hmNode != null) {
            return hmNode.getChild(m);
        }
        return null;
    }

    public HmMSmNode getSmNode(String hm, String m, String sm) {
        HmMSmNode hmNode = nodes.get(hm);
        if (hmNode != null) {
            HmMSmNode mNode = hmNode.getChild(m);
            if (mNode != null) {
                return mNode.getChild(sm);
            }
        }
        return null;
    }

    public HmMSmNode getNode(HmMSmId id) {
        if (id.getM().isEmpty()) {
            return getHmNode(id.getHm());
        }
        if (id.getSm().isEmpty()) {
            return getMNode(id.getHm(), id.getM());
        }

        return getSmNode(id.getHm(), id.getM(), id.getSm());
    }

    public Collection<HmMSmNode> getHMNodeList() {
        return nodes.getValues();
    }

    public Collection<HmMSmNode> getMNodeList(String hm) {
        HmMSmNode hmNode = nodes.get(hm);
        if (hmNode != null) {
            return hmNode.getChildren();
        }
        return null;
    }

    public Collection<HmMSmNode> getSMNodeList(String hm, String m) {
        HmMSmNode hmNode = nodes.get(hm);
        if (hmNode != null) {
            HmMSmNode mNode = hmNode.getChild(m);
            if (mNode != null) {
                return mNode.getChildren();
            }
        }
        return null;
    }

    /**
     * Suche in allen Unterknoten nach einer Nummer und oder einem Text
     *
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param andOrWhereValues
     * @param andOrSearch
     * @param wildCardSettings
     * @param language
     * @return
     */
    public List<HmMSmNode> search(EtkDisplayFields selectFields,
                                  List<String> selectValues,
                                  EtkDisplayFields whereFields,
                                  List<String> andOrWhereValues,
                                  boolean andOrSearch,
                                  WildCardSettings wildCardSettings,
                                  String language,
                                  List<String> fallbackLanguages) {
        List<HmMSmNode> result = new ArrayList<HmMSmNode>();

        internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, nodes.getValues());

        return result;
    }

    private void internSearch(List<HmMSmNode> result, EtkDisplayFields selectFields,
                              List<String> selectValues,
                              EtkDisplayFields whereFields,
                              List<String> andOrWhereValues,
                              boolean andOrSearch,
                              WildCardSettings wildCardSettings, String language, List<String> fallbackLanguages, Collection<HmMSmNode> nodes) {
        for (HmMSmNode node : nodes) {
            Map<String, String> fieldsAndValues = new HashMap<String, String>();

            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR), node.getNumber());
            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR), node.getTitle().getTextByNearestLanguage(language, fallbackLanguages));

            if (SearchMatchHelper.fieldsMatchSearchOptions(fieldsAndValues, andOrSearch, selectFields, selectValues, whereFields, andOrWhereValues, wildCardSettings)) {
                result.add(node);
            }

            internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, node.getChildren());
        }
    }

}
