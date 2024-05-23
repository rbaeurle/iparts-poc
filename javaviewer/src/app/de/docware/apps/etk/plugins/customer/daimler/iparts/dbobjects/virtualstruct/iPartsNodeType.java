/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;

/**
 * Typ für einen (virtuellen) Baugruppen-Knoten im iParts Plug-in.
 */
public enum iPartsNodeType {
    // virtuelle Rootknoten
    ROOT("ROOT", false),
    STRUCTURE("STRUCTURE", false), // Struktur
    PRODUCT_KGTU("PRODUCT_KGTU", false), // KG/TU-Fahrzeug-Produkt ohne dazugemischter Aggregate
    PRODUCT_KGTU_AGGS("PRODUCT_KGTU_AGGS", false), // KG/TU-Fahrzeug-Produkt inkl. dazugemischter Aggregate
    PRODUCT_KGTU_COMMON("PRODUCT_KGTU_COMMON", false), // Allgemeingültiges KG/TU-Produkt (unabhängig von dazugemischten Aggregaten)
    PRODUCT_EINPAS("PRODUCT_EINPAS", false), // EinPAS-Fahrzeug-Produkt ohne dazugemischter Aggregate
    PRODUCT_EINPAS_AGGS("PRODUCT_EINPAS_AGGS", false), // EinPAS-Fahrzeug-Produkt inkl. dazugemischter Aggregate
    PRODUCT_EINPAS_COMMON("PRODUCT_EINPAS_COMMON", false), // Allgemeingültiges EinPAS-Produkt (unabhängig von dazugemischten Aggregaten)
    DIALOG_HMMSM("DIALOG_HMMSM", true),
    DIALOG_EINPAS("DIALOG_EINPAS", true),
    EDS_OPS("EDS_OPS", true),
    EDS_MODEL_ELEMENT_USAGE("EDS_MODEL_ELEMENT_USAGE", true),
    CTT_MODEL("CTT_MODEL", true),
    EDS_EINPAS("EDS_EINPAS", true),
    MBS_STRUCTURE("MBS_STRUCTURE", true),

    // virtuelle Unterknoten (constructionRootNode ist hier immer false, weil es keine Rootknoten sind)
    EINPAS("EINPAS", false),
    KGTU("KGTU", false),
    KGSA("KGSA", false),
    HMMSM("HMMSM", false),
    OPS("OPS", false),
    MODEL_ELEMENT_USAGE("MODEL_ELEMENT_USAGE", false),
    MBS("MBS", false),
    CTT("CTT", false),
    EDS_SAA("EDS_SAA", false);

    private String alias;
    private boolean constructionRootNode;

    iPartsNodeType(String alias, boolean constructionRootNode) {
        this.alias = alias;
        this.constructionRootNode = constructionRootNode;
    }

    public String getAlias() {
        return alias;
    }

    /**
     * Handelt es sich um einen Konstruktions-Rootknoten? Kind-Knoten in der Konstruktion (z.B. HM/M/SM) liefern hier {@code false}
     * zurück. Die Abfrage muss daher immer auf dem ersten virtuellen Knoten einer virtuellen Baugruppen-ID erfolgen.
     *
     * @return
     */
    public boolean isConstructionRootNode() {
        return constructionRootNode;
    }

    public boolean isStructureType() {
        return this == iPartsNodeType.STRUCTURE;
    }

    public boolean isProductKgTuType() {
        return (this == iPartsNodeType.PRODUCT_KGTU) || (this == iPartsNodeType.PRODUCT_KGTU_AGGS) || (this == iPartsNodeType.PRODUCT_KGTU_COMMON);
    }

    public boolean isProductEinPASType() {
        return (this == iPartsNodeType.PRODUCT_EINPAS) || (this == iPartsNodeType.PRODUCT_EINPAS_AGGS) || (this == iPartsNodeType.PRODUCT_EINPAS_COMMON);
    }

    public boolean isProductType() {
        return isProductKgTuType() || isProductEinPASType();
    }

    public boolean isProductStructureWithAggregates() {
        return (this == iPartsNodeType.PRODUCT_KGTU_AGGS) || (this == iPartsNodeType.PRODUCT_EINPAS_AGGS);
    }

    public static iPartsNodeType getFromAlias(String alias) {
        for (iPartsNodeType result : values()) {
            if (result.alias.equals(alias)) {
                return result;
            }
        }
        return null;
    }

    public static iPartsNodeType getProductKgTuType(boolean isCommonProduct, boolean isProductStructureWithAggregates) {
        if (isCommonProduct) {
            return PRODUCT_KGTU_COMMON;
        } else if (isProductStructureWithAggregates) {
            return PRODUCT_KGTU_AGGS;
        } else {
            return PRODUCT_KGTU;
        }
    }

    public static iPartsNodeType getProductKgTuType(iPartsProduct product, EtkProject project) {
        return getProductKgTuType(product.isCommonProduct(project), product.isStructureWithAggregates());
    }

    public static iPartsNodeType getProductEinPASType(boolean isCommonProduct, boolean isProductStructureWithAggregates) {
        if (isCommonProduct) {
            return PRODUCT_EINPAS_COMMON;
        } else if (isProductStructureWithAggregates) {
            return PRODUCT_EINPAS_AGGS;
        } else {
            return PRODUCT_EINPAS;
        }
    }

    public static iPartsNodeType getProductEinPASType(iPartsProduct product, EtkProject project) {
        return getProductEinPASType(product.isCommonProduct(project), product.isStructureWithAggregates());
    }
}