/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.SearchMatchHelper;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataSet;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.utils.SortType;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.LeftOuterJoin;
import de.docware.util.sql.terms.Tables;

import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Singleton Object pro Projekt, das die EinPAS-Struktur enthält.
 */
public class EinPas implements EtkDbConst, iPartsConst {

    private static ObjectInstanceLRUList<Object, EinPas> instances = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS,
                                                                                                 MAX_CACHE_LIFE_TIME_CORE);

    // Knoten der ersten EinPAS-Ebene
    protected EinPasNodes nodes = new EinPasNodes();

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized EinPas getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), EinPas.class, "EinPas", false);
        EinPas result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new EinPas();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        nodes = new EinPasNodes();

        // Die Liste der EinPas-Knoten auf einen Schlag laden und aus dem Tabelleninhalt die Knoten erzeugen
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_EINPAS);
        attributesList.sort(new String[]{ FIELD_EP_HG, FIELD_EP_G, FIELD_EP_TU },
                            new SortType[]{ SortType.AUTOMATIC, SortType.AUTOMATIC, SortType.AUTOMATIC });

        for (DBDataObjectAttributes attributes : attributesList) {
            String hg = attributes.getField(FIELD_EP_HG).getAsString();
            String g = attributes.getField(FIELD_EP_G).getAsString();
            String tu = attributes.getField(FIELD_EP_TU).getAsString();

            // Alle Knotenebenen suchen und falls nicht vorhanden erzeugen
            EinPasNode hgNode = nodes.getOrCreate(EinPasType.HG, hg, null);
            EinPasNode gNode = hgNode.getOrCreateChild(EinPasType.G, g, hgNode);
            gNode.getOrCreateChild(EinPasType.TU, tu, gNode);
        }

        loadTexts(project);
    }


    private void loadTexts(EtkProject project) {
        DBSQLQuery query = project.getDB().getDBForDomain(MAIN).getNewQuery();

        List<String> resultFields = new ArrayList<String>();
        resultFields.add(FIELD_EP_HG.toLowerCase());
        resultFields.add(FIELD_EP_G.toLowerCase());
        resultFields.add(FIELD_EP_TU.toLowerCase());
        resultFields.add(FIELD_EP_PICTURE.toLowerCase());
        resultFields.add(EtkDbConst.FIELD_S_SPRACH.toLowerCase());
        resultFields.add(EtkDbConst.FIELD_S_BENENN.toLowerCase());
        resultFields.add(EtkDbConst.FIELD_S_TEXTID.toLowerCase());
        if (project.getDB().configBase.getDataBaseVersion() >= 6.2) {
            resultFields.add(EtkDbConst.FIELD_S_BENENN_LANG.toLowerCase());
        }

        query.select(new Fields(resultFields)).from(new Tables(TABLE_DA_EINPASDSC.toLowerCase()));

        query.join(new LeftOuterJoin((TABLE_SPRACHE).toLowerCase(),
                                     new Condition(TableAndFieldName.make(TABLE_DA_EINPASDSC, FIELD_EP_DESC).toLowerCase(), "=",
                                                   new Fields(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTNR).toLowerCase())).
                                             and(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_FELD).toLowerCase(), "=",
                                                               TableAndFieldName.make(TABLE_DA_EINPASDSC, FIELD_EP_DESC)))));

        DBDataSet dbSet = query.executeQuery();

        while (dbSet.next()) {

            EtkRecord rec = dbSet.getRecord(resultFields.toArray(new String[resultFields.size()]));

            EinPasNode node = getNode(new EinPasId(rec.getField(FIELD_EP_HG).getAsString(), rec.getField(FIELD_EP_G).getAsString(),
                                                   rec.getField(FIELD_EP_TU).getAsString()));
            if (node != null) {
                node.setTitle(rec.getField(EtkDbConst.FIELD_S_SPRACH).getAsString(), project.getEtkDbs().getLongTextFromRecord(rec), rec.getField(EtkDbConst.FIELD_S_TEXTID).getAsString());
                node.setPictureName(rec.getField(FIELD_EP_PICTURE).getAsString());
            }
        }
        dbSet.close();
    }

    public EinPasNode getHGNode(String hg) {
        return nodes.get(hg);
    }

    public EinPasNode getGNode(String hg, String g) {
        EinPasNode hgNode = nodes.get(hg);
        if (hgNode != null) {
            return hgNode.getChild(g);
        }
        return null;
    }

    public EinPasNode getTuNode(String hg, String g, String tu) {
        EinPasNode hgNode = nodes.get(hg);
        if (hgNode != null) {
            EinPasNode gNode = hgNode.getChild(g);
            if (gNode != null) {
                return gNode.getChild(tu);
            }
        }
        return null;
    }

    public EinPasNode getNode(EinPasId id) {
        if (id.getG().isEmpty()) {
            return getHGNode(id.getHg());
        }
        if (id.getTu().isEmpty()) {
            return getGNode(id.getHg(), id.getG());
        }

        return getTuNode(id.getHg(), id.getG(), id.getTu());
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
    public List<EinPasNode> search(EtkDisplayFields selectFields,
                                   List<String> selectValues,
                                   EtkDisplayFields whereFields,
                                   List<String> andOrWhereValues,
                                   boolean andOrSearch,
                                   WildCardSettings wildCardSettings,
                                   String language,
                                   List<String> fallbackLanguages) {
        List<EinPasNode> result = new ArrayList<EinPasNode>();

        internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, nodes.getValues());

        return result;
    }

    private void internSearch(List<EinPasNode> result,
                              EtkDisplayFields selectFields,
                              List<String> selectValues,
                              EtkDisplayFields whereFields,
                              List<String> andOrWhereValues,
                              boolean andOrSearch,
                              WildCardSettings wildCardSettings,
                              String language, List<String> fallbackLanguages, Collection<EinPasNode> nodes) {
        for (EinPasNode node : nodes) {
            Map<String, String> fieldsAndValues = new HashMap<String, String>();

            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR), node.getNumber());
            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR), node.getTitle().getTextByNearestLanguage(language, fallbackLanguages));

            if (SearchMatchHelper.fieldsMatchSearchOptions(fieldsAndValues, andOrSearch, selectFields, selectValues, whereFields, andOrWhereValues, wildCardSettings)) {
                result.add(node);
            }

            internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, node.getChildren());
        }
    }

    public Collection<EinPasNode> getHGNodeList() {
        return nodes.getValues();
    }

    public Collection<EinPasNode> getGNodeList(String hg) {
        EinPasNode hgNode = nodes.get(hg);
        if (hgNode != null) {
            return hgNode.getChildren();
        }
        return null;
    }

    public Collection<EinPasNode> getTUNodeList(String hg, String g) {
        EinPasNode hgNode = nodes.get(hg);
        if (hgNode != null) {
            EinPasNode gNode = hgNode.getChild(g);
            if (gNode != null) {
                return gNode.getChildren();
            }
        }
        return null;
    }

}
