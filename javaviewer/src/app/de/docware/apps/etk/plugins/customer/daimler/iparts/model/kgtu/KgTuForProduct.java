/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSales;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSalesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.util.AbstractCacheWithChangeSets;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.Collection;
import java.util.Map;

/**
 * Singleton Object pro Projekt, das die After-Sales KG/TU-Struktur pro Produkt komplett enthält.
 */
public class KgTuForProduct implements EtkDbConst, iPartsConst {

    // Lebensdauer für die ChangeSets ist iPartsPlugin.getCachesLifeTime(); Lebensdauer im Cache ebenfalls, da die
    // KG/TU-Strukturen pro Produkt relativ viel Speicher benötigen und nur dann gebraucht werden, wenn das Produkt
    // gerade in iParts angezeigt oder in den Webservices verwendet wird
    private static AbstractCacheWithChangeSets<ObjectInstanceLRUList<Object, KgTuForProduct>> cacheWithChangeSets =
            new AbstractCacheWithChangeSets<ObjectInstanceLRUList<Object, KgTuForProduct>>(MAX_CACHE_SIZE_CHANGE_SETS, iPartsPlugin.getCachesLifeTime()) {
                @Override
                protected ObjectInstanceLRUList<Object, KgTuForProduct> createNewCache() {
                    return new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_PRODUCT, iPartsPlugin.getCachesLifeTime());
                }
            };

    // Knoten der ersten Ebene
    protected KgTuNodes nodes = new KgTuNodes();
    private iPartsProductId productId;

    public static synchronized void clearCache() {
        synchronized (cacheWithChangeSets) {
            cacheWithChangeSets.clearCache();
        }
    }

    /**
     * Entfernt den Cache-Eintrag für die gerade aktiven {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s.
     *
     * @param project
     */
    public static void removeCacheForActiveChangeSets(EtkProject project) {
        synchronized (cacheWithChangeSets) {
            cacheWithChangeSets.removeCacheForActiveChangeSets(project);
        }
    }

    public static void removeKgTuForProductFromCache(EtkProject project, iPartsProductId productId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), KgTuForProduct.class, productId.getProductNumber(), false);
        synchronized (cacheWithChangeSets) {
            cacheWithChangeSets.removeCacheKey(hashObject);
        }
    }

    /**
     * Ersetzt den Cache, der mit {@code destinationCacheKey} referenziert wurde, durch den Cache der aktuell aktiven
     * {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s ohne den Cache dort zu entfernen. Wird z.B. verwendet,
     * um vor Edit-Aktionen den Cache als reine Referenz vollständig für den {@code destinationCacheKey} zu übernehmen.
     *
     * @param project
     * @param destinationCacheKey
     */
    public static void useCacheForActiveChangeSets(EtkProject project, String destinationCacheKey) {
        synchronized (cacheWithChangeSets) {
            cacheWithChangeSets.useActiveChangeSetCache(project, destinationCacheKey);
        }
    }

    public static KgTuForProduct getInstance(EtkProject project, iPartsProductId productId) {
        ObjectInstanceLRUList<Object, KgTuForProduct> cache;
        Object hashObject;
        KgTuForProduct result;
        synchronized (KgTuForProduct.class) {
            synchronized (cacheWithChangeSets) {
                cache = cacheWithChangeSets.getCacheInstance(project);
            }
            // Hier nun keine ChangeSets mehr verwenden für das hashObject, weil dies ja bereits über cacheWithChangeSets
            // gelöst wurde
            hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), KgTuForProduct.class, productId.getProductNumber(), false);
            result = cache.get(hashObject);
        }

        // Synchronisierung ab hier nicht mehr notwendig (notfalls werden halt zwei Instanzen von KgTuForProduct für eine
        // bestimmte productId erzeugt und die letzte davon bleibt im Cache), was durch die Pseudo-Transaktion aufgrund
        // von searchSortAndFillWithMultiLangValueForAllLanguages() in load() auch zu Deadlocks führen kann
        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new KgTuForProduct(productId);
            result.load(project);
            cache.put(hashObject, result);
        }

        return result;
    }

    private KgTuForProduct(iPartsProductId productId) {
        this.productId = productId;
    }

    private void load(EtkProject project) {
        if (!productId.isValidId()) { // ohne gütlige productId können wir nichts laden
            return;
        }

        // KG/TU-Knoten für ALLE Sprachen laden
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_KGTU_AS, FIELD_DA_DKM_KG, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_KGTU_AS, FIELD_DA_DKM_TU, false, false));

        iPartsDataKgTuAfterSalesList kgTuAfterSalesList = new iPartsDataKgTuAfterSalesList();
        kgTuAfterSalesList.searchSortAndFillWithMultiLangValueForAllLanguages(project, selectFields, FIELD_DA_DKM_DESC,
                                                                              new String[]{ TableAndFieldName.make(TABLE_DA_KGTU_AS, FIELD_DA_DKM_PRODUCT) },
                                                                              new String[]{ productId.getProductNumber() },
                                                                              false, new String[]{ FIELD_DA_DKM_KG, FIELD_DA_DKM_TU },
                                                                              false);

        // Passendes KG/TU-Template bestimmen
        Map<String, KgTuTemplate> asProductClassToKgTuTemplateMap = KgTuTemplate.getInstance(productId, project);
        KgTuTemplate kgTuTemplate = null;
        if (!asProductClassToKgTuTemplateMap.isEmpty()) {
            kgTuTemplate = asProductClassToKgTuTemplateMap.get(AS_PRODUCT_CLASS_CAR);
            if (kgTuTemplate == null) {
                kgTuTemplate = asProductClassToKgTuTemplateMap.values().iterator().next();
            }
        }

        for (iPartsDataKgTuAfterSales dataKgTu : kgTuAfterSalesList) {
            KgTuId kgTuId = new KgTuId(dataKgTu.getFieldValue(FIELD_DA_DKM_KG), dataKgTu.getFieldValue(FIELD_DA_DKM_TU));
            KgTuNode node = getOrCreateNode(kgTuId, productId);
            if (node != null) {
                node.setTitle(dataKgTu.getFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC));

                if (kgTuTemplate != null) {
                    // Zusatzgrafik aus dem KG/TU-Template setzen
                    KgTuNode templateNode = kgTuTemplate.getNode(node.getId());
                    if (templateNode != null) {
                        node.setPictureName(templateNode.getPictureName());
                    }
                }
            }
        }
    }

    private KgTuNode getOrCreateNode(KgTuId kgTuId, iPartsProductId productId) {
        // Alle Knotenebenen suchen und falls nicht vorhanden erzeugen
        KgTuNode kgNode = nodes.get(kgTuId.getKg());
        if (kgNode == null) {
            //Bei einem neuen TU-Knoten muss das Produkt noch eingefügt werden
            kgNode = createNewKgNode(kgTuId.getKg(), productId);
        }

        if (kgTuId.isKgNode()) {
            return kgNode;
        } else {
            KgTuNode tuNode = kgNode.getChild(kgTuId.getTu());
            if (tuNode == null) {
                //Bei einem neuen TU-Knoten muss das Produkt noch eingefügt werden
                tuNode = createNewTuNode(kgNode, kgTuId.getTu(), productId);
            }
            return tuNode;
        }
    }

    // Erstellt einen neuen KG Knoten und fügt ihn zu den Knoten der ersten Ebene hinzu
    private KgTuNode createNewKgNode(String kg, iPartsProductId productId) {
        KgTuNode kgNode = nodes.createNewNode(KgTuType.KG, kg, null);
        nodes.add(kg, kgNode);
        return kgNode;
    }

    // Erstellt einen neuen TU Knoten und hängt ihn an den übergebenen KG Knoten
    private KgTuNode createNewTuNode(KgTuNode kgNode, String tu, iPartsProductId productId) {
        KgTuNode tuNodeNew = kgNode.getOrCreateChild(KgTuType.TU, tu, kgNode);
        return tuNodeNew;
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

}
