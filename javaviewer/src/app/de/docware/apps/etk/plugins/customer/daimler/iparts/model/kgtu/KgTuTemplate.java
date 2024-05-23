/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.SearchMatchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.SortType;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Singleton Object pro Projekt, das den Vorlagenkatalog der KG/TU-Struktur komplett enthält.
 * Daten kommen über einen benutzerdefinierten Import der Tabelle TABLE_DA_KGTU_TEMPLATE.
 * Wird im Edit ein neues Modul angelegt, oder aus der Konstruktion in AS übernommen, und es existiert eine AS-Benennung
 * in {@link KgTuForProduct} dann wird diese angezeigt. Falls nicht, dann wird der hier hinterlegte Vorlagentext
 * angezeigt.
 */
public class KgTuTemplate implements EtkDbConst, iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, KgTuTemplate> instances =
            new ObjectInstanceStrongLRUList<>(10 * MAX_CACHE_SIZE_STRUCTURE, MAX_CACHE_LIFE_TIME_CORE); // Faktor 10 wegen Matrix aus Aggregatetyp und AS-Produltklasse
    // Knoten der ersten Ebene
    protected KgTuNodes nodes = new KgTuNodes();

    public static synchronized void warmUpCache(EtkProject project) {
//        getInstance(project);  // warm-up macht m.E. keinen Sinn mehr oder man müsste die kpl. Tabelle in eine geeignete Map einlesen
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    /**
     * KG-TU Template(s) zu Produkt ermitteln.
     * Wenn das Produkt mehrere AS Produktklassen besitzt entstehen mehrere Templates, sonst nur eines.
     * Bei mehreren Produktklassen werden die Templates bei der Verwendung nacheinander durchgegangen. Damit wir hier eine definierte
     * Reihenfolge habe verwenden wir eine SortedMap wo die Templates nach AS Produktklassen sortiert sind. Vermutlich ist aber auch
     * das willkürlich.
     * Siehe auch die Diskussion in DAIMLER-5521.
     *
     * @param productId
     * @param project
     * @return Map AS-Produkt-Klasse -> Template
     */
    public static synchronized Map<String, KgTuTemplate> getInstance(iPartsProductId productId, EtkProject project) {
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        String aggregateType = product.getAggregateType();
        Set<String> asProductClasses = product.getAsProductClasses();
        Map<String, KgTuTemplate> kgTuTemplateMap = new TreeMap<String, KgTuTemplate>();
        for (String asProductClass : asProductClasses) {
            KgTuTemplate kgTuTemplate = KgTuTemplate.getInstance(project, aggregateType, asProductClass);
            if (!kgTuTemplate.getKgNodeList().isEmpty()) {
                kgTuTemplateMap.put(asProductClass, kgTuTemplate);
            }
        }
        return kgTuTemplateMap;
    }

    /**
     * KG-TU Template zu Bamusterart und AS Produktklasse bestimmen
     *
     * @param project
     * @param aggregateType  Baumusterart (Name früher: Aggregatetyp)
     * @param asProductClass
     * @return
     */
    private static synchronized KgTuTemplate getInstance(EtkProject project, String aggregateType, String asProductClass) {
        String cacheKey = "KGTUTemplate" + aggregateType + "_" + asProductClass;
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), KgTuTemplate.class, cacheKey, false);
        KgTuTemplate result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new KgTuTemplate();
            result.load(project, aggregateType, asProductClass);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project, String aggregateType, String asProductClass) {
        nodes = new KgTuNodes();

        String[] kgtuFields = new String[]{ FIELD_DA_DKT_AGGREGATE_TYPE, FIELD_DA_DKT_AS_PRODUCT_CLASS, FIELD_DA_DKT_KG, FIELD_DA_DKT_TU, FIELD_DA_DKT_DESC, FIELD_DA_DKT_PICTURE };

        // Wir importieren fix das Default Template. Spezielle Templates, die seit DAIMLER-5366 importiert werden können, werden in einer späteren Story behandelt.
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_KGTU_TEMPLATE, kgtuFields,
                                                                                           new String[]{ FIELD_DA_DKT_AGGREGATE_TYPE, FIELD_DA_DKT_AS_PRODUCT_CLASS },
                                                                                           new String[]{ aggregateType, asProductClass },
                                                                                           ExtendedDataTypeLoadType.LOAD, false, true);
        attributesList.sort(kgtuFields, SortType.AUTOMATIC);

        for (DBDataObjectAttributes attributes : attributesList) {
            String kg = attributes.getField(FIELD_DA_DKT_KG).getAsString();
            String tu = attributes.getField(FIELD_DA_DKT_TU).getAsString();

            // getAsMultiLanguage(null, false) ist ausreichend, weil durch ExtendedDataTypeLoadType.LOAD alle mehrsprachigen
            // Felder bereits vollständig geladen wurden
            EtkMultiSprache description = attributes.getField(FIELD_DA_DKT_DESC).getAsMultiLanguage(null, false);

            String picturename = attributes.getField(FIELD_DA_DKT_PICTURE).getAsString();

            // Alle Knotenebenen suchen und falls nicht vorhanden erzeugen
            KgTuNode kgTuNode = nodes.getOrCreate(KgTuType.KG, kg, null);
            if (StrUtils.isValid(tu)) {
                kgTuNode = kgTuNode.getOrCreateChild(KgTuType.TU, tu, kgTuNode);
            }
            kgTuNode.setPictureName(picturename);
            kgTuNode.setTitle(description);
        }
    }

    public Collection<KgTuNode> getKgNodeList() {
        return nodes.getValues();
    }

    public Collection<KgTuNode> getTuNodeList(String kg) {
        KgTuNode node = nodes.get(kg);
        if (node != null) {
            return node.getChildren();
        }
        return null;
    }

    public KgTuNode getKgNode(String kg) {
        return nodes.get(kg);
    }

    public KgTuNode getTuNode(String kg, String tu) {
        KgTuNode node = nodes.get(kg);
        if (node != null) {
            return node.getChild(tu);
        }
        return null;
    }

    public KgTuNode getNode(KgTuId id) {
        if (id.getTu().isEmpty()) {
            return getKgNode(id.getKg());
        }

        return getTuNode(id.getKg(), id.getTu());
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
    public List<KgTuNode> search(EtkDisplayFields selectFields,
                                 List<String> selectValues,
                                 EtkDisplayFields whereFields,
                                 List<String> andOrWhereValues,
                                 boolean andOrSearch,
                                 WildCardSettings wildCardSettings,
                                 String language) {
        List<KgTuNode> result = new ArrayList<KgTuNode>();

        internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, nodes.getValues());

        return result;
    }

    private void internSearch(List<KgTuNode> result, EtkDisplayFields selectFields,
                              List<String> selectValues,
                              EtkDisplayFields whereFields,
                              List<String> andOrWhereValues,
                              boolean andOrSearch,
                              WildCardSettings wildCardSettings,
                              String language, Collection<KgTuNode> nodes) {
        for (KgTuNode node : nodes) {
            Map<String, String> fieldsAndValues = new HashMap<String, String>();

            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR), node.getNumber());
            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR), node.getTitle().getText(language));

            if (SearchMatchHelper.fieldsMatchSearchOptions(fieldsAndValues, andOrSearch, selectFields, selectValues, whereFields, andOrWhereValues, wildCardSettings)) {
                result.add(node);
            }

            internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, node.getChildren());
        }
    }
}
