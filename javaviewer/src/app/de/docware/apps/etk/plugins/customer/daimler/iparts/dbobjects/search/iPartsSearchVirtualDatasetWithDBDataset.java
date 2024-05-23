/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.search.model.EtkSearchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPartlistTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleEinPASId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductSAsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.*;
import de.docware.util.CanceledException;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.weak.WeakKeysMap;

import java.util.List;

/**
 * Virtueller Datensatz für die Suche in iParts mit einem {@link DBDataSet} als Ergebnis.
 */
public abstract class iPartsSearchVirtualDatasetWithDBDataset extends iPartsSearchVirtualDataset {

    private static String RESULT_DATA_MODULE_EINPAS = "iPartsSearchVirtualDatasetWithDBDataset.resultDataModuleEinPAS";
    private static String RESULT_DATA_PRODUCT_SAS = "iPartsSearchVirtualDatasetWithDBDataset.resultDataProductSAs";
    private static String RESULT_DATA_PRODUCT = "iPartsSearchVirtualDatasetWithDBDataset.resultDataProduct";

    protected DBDataSetCancelable ds;
    private ObjectInstanceLRUList<PartListEntryId, EtkDataPartListEntry> partListEntriesCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_SEARCH_PART_LIST_ENTRIES,
                                                                                                                            iPartsPlugin.getCachesLifeTime());

    /**
     * Liefert das {@link iPartsDataModuleEinPAS} Objekt mit der Verortung des Moduls, welches vom Suchtreffer an den
     * übergebenen Stücklisteneintrag gehängt wurde.
     *
     * @param partListEntry
     * @return Kann auch {@code null} sein, wenn keine Verortung gefunden wurde
     */
    public static iPartsDataModuleEinPAS getResultDataModuleEinPAS(EtkDataPartListEntry partListEntry) {
        return (iPartsDataModuleEinPAS)partListEntry.getAggregateObject(iPartsSearchVirtualDatasetWithDBDataset.RESULT_DATA_MODULE_EINPAS);
    }

    /**
     * Liefert das {@link iPartsDataProductSAs} Objekt mit der Verortung der SA im Produkt inkl. KG, welches vom Suchtreffer
     * an den übergebenen Stücklisteneintrag gehängt wurde.
     *
     * @param partListEntry
     * @return Kann auch {@code null} sein, wenn keine Verortung gefunden wurde
     */
    public static iPartsDataProductSAs getResultDataProductSAs(EtkDataPartListEntry partListEntry) {
        return (iPartsDataProductSAs)partListEntry.getAggregateObject(iPartsSearchVirtualDatasetWithDBDataset.RESULT_DATA_PRODUCT_SAS);
    }

    /**
     * Liefert das {@link iPartsDataProduct} Objekt mit dem Produkt des Moduls, welches vom Suchtreffer an den
     * übergebenen Stücklisteneintrag gehängt wurde.
     *
     * @param partListEntry
     * @return Kann auch {@code null} sein, wenn kein Produkt für das Modul bestimmt wurde
     */
    public static iPartsDataProduct getResultDataProduct(EtkDataPartListEntry partListEntry) {
        return (iPartsDataProduct)partListEntry.getAggregateObject(iPartsSearchVirtualDatasetWithDBDataset.RESULT_DATA_PRODUCT);
    }

    public iPartsSearchVirtualDatasetWithDBDataset(EtkDisplayFields dsSelectFields, EtkProject project, WeakKeysMap<String, String> multiLanguageCache) {
        super(dsSelectFields, project, multiLanguageCache);
    }

    @Override
    public void create() throws CanceledException {
        ds = createDBDataSet();
    }

    @Override
    public EtkDataPartListEntry[] get() {
        EtkDataPartListEntry partListEntry = EtkSearchHelper.getNextPartListEntryForNativeDataSet(ds, dsSelectFields, project,
                                                                                                  multiLanguageCache, project.getDBLanguage(), "");

        // Klonen vom Stücklisteneintrag aus dem Cache ist notwendig, damit die unten gesetzten AggregatedDataObjects nicht
        // von neuen Ergebnissen überschrieben werden z.B. in derselben SA (aber unterschiedlichem Produkt und KG)
        // DAIMLER-7521: Cache nicht mehr verwenden, weil die Gesamt-Performance v.a. bei SearchPartsWOContext dadurch
        // je nach Suchtext in der Regel leider erheblich schlechter ist als mit Cache
//        partListEntry = getPartListEntryFromCache(partListEntry, true);

        DBDataObjectAttribute dataObjectAttribute;
        List<String> values = ds.getStringList();

        /**
         * ggf DA_MODULES_EINPAS als Data Object hinzufügen
         */
        DBDataObjectAttributes attributesModulesEinpas = EtkSearchHelper.getAttributesByTable(iPartsConst.TABLE_DA_MODULES_EINPAS,
                                                                                              dsSelectFields, values, project.getDBLanguage(),
                                                                                              project, multiLanguageCache, "");
        dataObjectAttribute = attributesModulesEinpas.getField(iPartsConst.FIELD_DME_EINPAS_HG, false);
        if (dataObjectAttribute != null) { // ist nur für Suche per SearchParts WS enthalten
            String productNo = attributesModulesEinpas.getField(iPartsConst.FIELD_DME_PRODUCT_NO, false).getAsString();
            String moduleNo = attributesModulesEinpas.getField(iPartsConst.FIELD_DME_MODULE_NO, false).getAsString();
            String lfdNr = attributesModulesEinpas.getField(iPartsConst.FIELD_DME_LFDNR, false).getAsString();
            iPartsModuleEinPASId moduleEinPASId = new iPartsModuleEinPASId(productNo, moduleNo, lfdNr);
            iPartsDataModuleEinPAS dataModuleEinPAS = new iPartsDataModuleEinPAS(project, moduleEinPASId);
            dataModuleEinPAS.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);

            String einpasHG = attributesModulesEinpas.getField(iPartsConst.FIELD_DME_EINPAS_HG, false).getAsString();
            String einpasG = attributesModulesEinpas.getField(iPartsConst.FIELD_DME_EINPAS_G, false).getAsString();
            String einpasTU = attributesModulesEinpas.getField(iPartsConst.FIELD_DME_EINPAS_TU, false).getAsString();
            String kgtuKG = attributesModulesEinpas.getField(iPartsConst.FIELD_DME_SOURCE_KG, false).getAsString();
            String kgtuTU = attributesModulesEinpas.getField(iPartsConst.FIELD_DME_SOURCE_TU, false).getAsString();
            dataModuleEinPAS.setFieldValue(iPartsConst.FIELD_DME_EINPAS_HG, einpasHG, DBActionOrigin.FROM_DB);
            dataModuleEinPAS.setFieldValue(iPartsConst.FIELD_DME_EINPAS_G, einpasG, DBActionOrigin.FROM_DB);
            dataModuleEinPAS.setFieldValue(iPartsConst.FIELD_DME_EINPAS_TU, einpasTU, DBActionOrigin.FROM_DB);
            dataModuleEinPAS.setFieldValue(iPartsConst.FIELD_DME_SOURCE_KG, kgtuKG, DBActionOrigin.FROM_DB);
            dataModuleEinPAS.setFieldValue(iPartsConst.FIELD_DME_SOURCE_TU, kgtuTU, DBActionOrigin.FROM_DB);

            // Verortung wird als aggregiertes DBDataObject an den Stücklisteneintrag gehängt
            partListEntry.setAggregatedDataObject(RESULT_DATA_MODULE_EINPAS, dataModuleEinPAS);
        }

        /**
         * ggf DA_PRODUCT_SAS als Data Object hinzufügen
         */
        DBDataObjectAttributes attributesProductSAs = EtkSearchHelper.getAttributesByTable(iPartsConst.TABLE_DA_PRODUCT_SAS,
                                                                                           dsSelectFields, values, project.getDBLanguage(),
                                                                                           project, multiLanguageCache, "");
        dataObjectAttribute = attributesProductSAs.getField(iPartsConst.FIELD_DPS_PRODUCT_NO, false);
        if (dataObjectAttribute != null) { // ist nur für Suche per SearchParts WS enthalten
            String productNo = dataObjectAttribute.getAsString();
            String saNumber = attributesProductSAs.getFieldValue(iPartsConst.FIELD_DPS_SA_NO);
            String kg = attributesProductSAs.getFieldValue(iPartsConst.FIELD_DPS_KG);
            iPartsProductSAsId productSAsId = new iPartsProductSAsId(productNo, saNumber, kg);
            iPartsDataProductSAs dataProductSAs = new iPartsDataProductSAs(project, productSAsId);
            dataProductSAs.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);

            // SA-Verortung wird als aggregiertes DBDataObject an den Stücklisteneintrag gehängt
            partListEntry.setAggregatedDataObject(RESULT_DATA_PRODUCT_SAS, dataProductSAs);
        }

        /**
         * ggf DA_PRODUCT als Data Object hinzufügen
         */
        DBDataObjectAttributes attributesProduct = EtkSearchHelper.getAttributesByTable(iPartsConst.TABLE_DA_PRODUCT,
                                                                                        dsSelectFields, values, project.getDBLanguage(),
                                                                                        project, multiLanguageCache, "");
        dataObjectAttribute = attributesProduct.getField(iPartsConst.FIELD_DP_PRODUCT_NO, false);
        if (dataObjectAttribute != null) { // ist nur für Suche per SearchParts WS enthalten
            String productNo = dataObjectAttribute.getAsString();
            iPartsProductId productId = new iPartsProductId(productNo);
            iPartsDataProduct dataProduct = new iPartsDataProduct(project, productId);
            dataProduct.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);

            String aggregateType = attributesProduct.getField(iPartsConst.FIELD_DP_AGGREGATE_TYPE).getAsString();
            dataProduct.setFieldValue(iPartsConst.FIELD_DP_AGGREGATE_TYPE, aggregateType, DBActionOrigin.FROM_DB);

            // Produkt wird als aggregiertes DBDataObject an den Stücklisteneintrag gehängt
            partListEntry.setAggregatedDataObject(RESULT_DATA_PRODUCT, dataProduct);
        }

        return new EtkDataPartListEntry[]{ partListEntry };
    }

    /**
     * Liefert den vollständig geladenen Stücklisteneintrag aus dem Cache zurück und befüllt den Cache mit der gesamten
     * dazugehörigen Stückliste falls notwendig.
     * Das macht wahrscheinlich Sinn weil für die Filterung auch PVs des aktuellen Eintrags benötigt werden, die sonst nachgeladen werden müssten.
     *
     * @param partListEntry
     * @param cloneCachedPartListEntry Flag, ob der Stücklisteneintrag aus dem Cache geklont werde soll
     * @return
     */
    protected EtkDataPartListEntry getPartListEntryFromCache(EtkDataPartListEntry partListEntry, boolean cloneCachedPartListEntry) {
        PartListEntryId partListEntryId = partListEntry.getAsId();
        EtkDataPartListEntry cachedPartListEntry = partListEntriesCache.get(partListEntryId);
        if (cachedPartListEntry == null) {
            EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
            for (EtkDataPartListEntry partListEntryInAssembly : ownerAssembly.getPartListUnfiltered(iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS)) {
                partListEntriesCache.put(partListEntryInAssembly.getAsId(), partListEntryInAssembly);
            }

            // Erneute Bestimmung vom cachedPartListEntry (muss jetzt eigentlich einen Treffer geben)
            cachedPartListEntry = partListEntriesCache.get(partListEntryId);
        }

        if (cachedPartListEntry != null) {
            if (cloneCachedPartListEntry) {
                partListEntry = cachedPartListEntry.cloneMe(project);
                partListEntry.setOwnerAssembly(cachedPartListEntry.getOwnerAssembly());
            } else {
                partListEntry = cachedPartListEntry;
            }
        }

        return partListEntry; // Fallback auf ursprünglichen Stücklisteneintrag (dürfte eigentlich nie passieren)
    }

    @Override
    public boolean next() throws CanceledException {
        return EtkSearchHelper.hasNextPartListEntryForNativeDataSet(ds, dsSelectFields, project, multiLanguageCache);
    }

    @Override
    public void close() {
        if (ds != null) {
            ds.close();
            ds = null;
        }
    }

    protected DBDataSetCancelable getDBDataSet() {
        return ds;
    }

    /**
     * Wird aufgerufen, um das {@link DBDataSet} zu erzeugen.
     *
     * @return
     */
    protected abstract DBDataSetCancelable createDBDataSet() throws CanceledException;
}
